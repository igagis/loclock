package loclock.net.loclock;

import android.Manifest;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by ivan on 11/17/15.
 */
public class BackgroundService extends IntentService {

    public static final int SHARE_PERIOD_MILLIS = 15 * 60 * 1000;

    public static  final int LOCATION_TIMEOUT = 5 * 60 * 1000;

    public BackgroundService() {
        super("loclock background service");
    }

    protected Location loc;

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("loclock", "service invoked");

        final LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        final LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location loc) {
                BackgroundService.this.loc = loc;
                Log.d("loclock", "location found: lon= " + loc.getLongitude() + " lat = " + loc.getLatitude() + " acc = " + loc.getAccuracy());
                Looper.myLooper().quit();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("loclock", "status changed");
            }

            public void onProviderEnabled(String provider) {
                Log.d("loclock", "provider enabled");
            }

            public void onProviderDisabled(String provider) {
                Log.d("loclock", "provider disabled");
            }
        };

        Thread thread = new Thread(){
            @Override
            public void run() {
                Looper.myLooper().prepare();

                Timer timer = new Timer();

                try {
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
//                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            BackgroundService.this.loc = null;
                            Looper.myLooper().quit();
                        }
                    }, LOCATION_TIMEOUT);

                    Looper.myLooper().loop();

                    lm.removeUpdates(locationListener);
                }catch(SecurityException e){
                }finally {
                    timer.cancel();
                }
            }
        };
        thread.start();
        try {
            thread.join();
        }catch(InterruptedException e){
            return;
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if(loc != null) {
            Log.d("loclock", "sending location to server: lon= " + loc.getLongitude() + " lat = " + loc.getLatitude() + " acc = " + loc.getAccuracy());

            String json = "{\"lat\":" +loc.getLatitude() + ", \"lng\":" + loc.getLongitude() + "}";

            //share location to server
            HttpURLConnection connection = null;
            try {
                //Create connection
                URL url = new URL("http://loclock.net/api/users/" + sp.getString(MainActivity.KEY_USERNAME, ""));
                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Length", "" + Integer.toString(json.getBytes().length));
                connection.setRequestProperty("Content-Language", "en-US");

                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                //Send request
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(json);
                wr.flush();
                wr.close();

                //Get Response	
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();

                Log.d("loclock", "server response: " + response.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                if(connection != null){
                    connection.disconnect();
                }
            }
        }else{
            Log.d("loclock", "failed to get location in 5 minutes");
        }

        //Set the timer for next execution if location sharing is enabled
        if(sp.getBoolean(MainActivity.KEY_SHARE_ENABLED, false)) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            am.set(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + SHARE_PERIOD_MILLIS,
                    PendingIntent.getService(
                            getApplicationContext(),
                            0,
                            new Intent(getApplicationContext(), BackgroundService.class),
                            PendingIntent.FLAG_ONE_SHOT
                    )
            );
        }
    }
}
