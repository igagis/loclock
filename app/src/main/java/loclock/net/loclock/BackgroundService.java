package loclock.net.loclock;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by ivan on 11/17/15.
 */
public class BackgroundService extends IntentService {

    public BackgroundService(){
        super("loclock background service");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        //TODO: share location to server

        Log.d("loclock", "service invoked");


        //Set the timer for next execution if location sharing is enabled
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if(sp.getBoolean(MainActivity.KEY_SHARE_ENABLED, false)) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            am.set(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 1000,
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
