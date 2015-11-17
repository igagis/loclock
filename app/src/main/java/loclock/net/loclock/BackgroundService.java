package loclock.net.loclock;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by ivan on 11/17/15.
 */
public class BackgroundService extends IntentService {

    public static final int SHARE_PERIOD_MILLIS = 1000;

    public BackgroundService(){
        super("loclock background service");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("loclock", "service invoked");

        LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);

        Location loc;
        try {
            loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }catch (SecurityException e){
            Log.d("loclock", "no permissions to obtain location");
            return;
        }

        if(loc == null){
            Log.d("loclock", "last location is unknown");
            return;
        }

        Log.d("loclock", "last known location: lon= " + loc.getLongitude() + " lat = " + loc.getLatitude());

        //TODO: share location to server





        //Set the timer for next execution if location sharing is enabled
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

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
