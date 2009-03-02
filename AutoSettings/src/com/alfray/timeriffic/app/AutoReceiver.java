/*
 * (c) ralfoide gmail com, 2008
 * Project: Timeriffic
 * License: GPLv3
 */

package com.alfray.timeriffic.app;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alfray.timeriffic.prefs.PrefsValues;
import com.alfray.timeriffic.profiles.Columns;
import com.alfray.timeriffic.profiles.ProfilesDB;
import com.alfray.timeriffic.profiles.TimedActionUtils;
import com.alfray.timeriffic.profiles.ProfilesDB.ActionInfo;
import com.alfray.timeriffic.utils.SettingsHelper;
import com.alfray.timeriffic.utils.SettingsHelper.RingerMode;
import com.alfray.timeriffic.utils.SettingsHelper.VibrateRingerMode;


public class AutoReceiver extends BroadcastReceiver {

    private final static boolean DEBUG = true;
    private final static String TAG = "AutoReceiver";
    
    public final static String ACTION_AUTO_CHECK_STATE = "com.alfray.intent.action.AUTO_CHECK_STATE";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        
        // boolean isBoot = Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction());

        PrefsValues prefs = new PrefsValues(context);

        if (!prefs.isServiceEnabled()) {
            Log.d(TAG, "Checking disabled");
            return;
        }

        Log.d(TAG, "Checking enabled");

        ProfilesDB profilesDb = new ProfilesDB();
        try {
            profilesDb.onCreate(context);
            
            profilesDb.removeAllActionExecFlags();
            
            long[] prof_indexes = profilesDb.getEnabledProfiles();

            Calendar c = new GregorianCalendar();
            c.setTimeInMillis(System.currentTimeMillis());
            int hourMin = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
            int day = TimedActionUtils.calendarDayToActionDay(c);
            
            ArrayList<ActionInfo> actions = profilesDb.getActivableActions(hourMin, day, prof_indexes);
            if (actions != null && actions.size() > 0) {
                SettingsHelper settings = new SettingsHelper(context);
                performActions(settings, actions);
                profilesDb.markActionsEnabled(actions);
            }
            
            // TODO compute next
            
        } finally {
            profilesDb.onDestroy();
        }
        
        notifyDataChanged(context);
    }

    private void performActions(SettingsHelper settings, ArrayList<ActionInfo> actions) {
        for (ActionInfo info : actions) {
            performAction(settings, info.mActions);
        }
    }

    private void performAction(SettingsHelper settings, String actions) {
        if (actions == null) return;
        
        for (String action : actions.split(",")) {
            int value = -1;
            if (action.length() > 1) {
                char code = action.charAt(0);
                char v = action.charAt(1);
                
                switch(code) {
                case Columns.ACTION_RINGER:
                    for (RingerMode mode : RingerMode.values()) {
                        String name = mode.toString();
                        if (name.charAt(0) == v) {
                            settings.changeRingerMode(mode);
                            break;
                        }
                    }
                    break;
                case Columns.ACTION_VIBRATE:
                    for (VibrateRingerMode mode : VibrateRingerMode.values()) {
                        String name = mode.toString();
                        if (name.charAt(0) == v) {
                            settings.changeRingerVibrate(mode);
                            break;
                        }
                    }
                    break;
                default:
                    try {
                        value = Integer.parseInt(action.substring(1));

                        switch(code) {
                        case Columns.ACTION_WIFI:
                            settings.changeWifi(value > 0);
                            break;
                        case Columns.ACTION_BRIGHTNESS:
                            settings.changeBrightness(value, true /*persist*/);
                            break;
                        case Columns.ACTION_RING_VOLUME:
                            settings.changeRingerVolume(value);
                            break;
                        }
                        
                    } catch (NumberFormatException e) {
                        // pass
                    }
                }
            }
        }
    }

    /** Notify UI to update */
    private void notifyDataChanged(Context context) {
        Context c = context.getApplicationContext();
        if (c instanceof TimerifficApp) {
            ((TimerifficApp) c).invokeDataListener();
        }
    }

    private void scheduleAlarm(Context context, PrefsValues prefs, int hourMin) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Intent intent = new Intent(ACTION_AUTO_CHECK_STATE);
        PendingIntent op = PendingIntent.getBroadcast(context, 0 /*requestCode*/, intent, PendingIntent.FLAG_ONE_SHOT);

        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(System.currentTimeMillis());
        int now_hour = c.get(Calendar.HOUR_OF_DAY);
        int now_min = c.get(Calendar.MINUTE);
        int now_hourMin = now_hour * 60 + now_min;
        
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        if (hourMin > now_hourMin) {
            c.add(Calendar.MINUTE, hourMin - now_hourMin);
        } else if (hourMin < now_hourMin) {
            c.add(Calendar.MINUTE, 24*60 - now_hourMin + hourMin);
        }

        long timeMs = c.getTimeInMillis();

        manager.set(AlarmManager.RTC_WAKEUP, timeMs, op);

        if (DEBUG) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            sdf.setCalendar(c);
            String s2 = sdf.format(c.getTime());
            Log.d(TAG, String.format("Next Alarm: %s", s2));
        }
    }
}
