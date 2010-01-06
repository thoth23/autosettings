/*
 * (c) ralfoide gmail com, 2008
 * Project: AlarmTest
 * License TBD
 */

package com.alfray.alarmTest;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;

//-----------------------------------------------

/**
 * The class  does...
 *
 */
public class MyReceiver extends BroadcastReceiver {

    private static final String TAG = "MyReceiver";

    private SharedPreferences mPrefs;

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MyReceiver");
        try {
            wl.acquire();

            mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            log("Receiver: " + intent.getAction());

        } finally {
            wl.release();
        }
    }

    private void log(String msg) {
        if (msg != null) {

            Log.d(TAG, msg);

            String s = mPrefs.getString("log", "");

            SimpleDateFormat sdf = new SimpleDateFormat();
            String t = sdf.format(new Date());

            s += String.format("[%s] %s\n", t, msg);
            mPrefs.edit().putString("log", s).commit();
        }
    }

}


