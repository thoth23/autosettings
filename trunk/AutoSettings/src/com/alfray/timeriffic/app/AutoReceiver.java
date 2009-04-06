/*
 * (c) ralfoide gmail com, 2008
 * Project: Timeriffic
 * License: GPLv3
 */

package com.alfray.timeriffic.app;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

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
    private final static String TAG = "Tmrfc-Receiver";
    
    /** Name of intent to broadcast to activate this receiver. */
    public final static String ACTION_AUTO_CHECK_STATE = "com.alfray.intent.action.AUTO_CHECK_STATE";
    
    /** Name of an extra int: how we should display a toast for next event. */
    public final static String EXTRA_TOAST_NEXT_EVENT = "toast-next";
    
    public final static int TOAST_NONE = 0;
    public final static int TOAST_IF_CHANGED = 1;
    public final static int TOAST_ALWAYS = 2;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TimerifficReceiver");
        try {
            wl.acquire();

            // boolean isBoot = Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction());
            
            int displayToast = TOAST_NONE;
            Bundle extras = intent.getExtras();
            if (extras != null) displayToast = extras.getInt(EXTRA_TOAST_NEXT_EVENT, TOAST_NONE);

            PrefsValues prefs = new PrefsValues(context);

            if (!prefs.isServiceEnabled()) {
                Log.d(TAG, "Checking disabled");
                if (displayToast == TOAST_ALWAYS) {
                    Toast.makeText(context, "Timeriffic Disabled", Toast.LENGTH_LONG).show();
                }
                return;
            }

            Log.d(TAG, "Checking enabled");

            checkProfiles(context, displayToast, prefs);
            
            notifyDataChanged(context);

        } finally {
            wl.release();
        }
    }

    private void checkProfiles(Context context, int displayToast,
                    PrefsValues prefs) {
        ProfilesDB profilesDb = new ProfilesDB();
        try {
            profilesDb.onCreate(context);
            
            profilesDb.removeAllActionExecFlags();
            
            // Only do something if at least one profile is enabled.
            long[] prof_indexes = profilesDb.getEnabledProfiles();
            if (prof_indexes != null && prof_indexes.length != 0) {

                Calendar c = new GregorianCalendar();
                c.setTimeInMillis(System.currentTimeMillis());
                int hourMin = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
                int day = TimedActionUtils.calendarDayToActionDay(c);
                
                ArrayList<ActionInfo> actions = profilesDb.getWeekActivableActions(hourMin, day, prof_indexes);

                if (actions != null && actions.size() > 0) {
                    SettingsHelper settings = new SettingsHelper(context);
                    performActions(settings, actions);
                    profilesDb.markActionsEnabled(actions);
                }

                // Compute next event and set an alarm for it
                int nextEventMin = profilesDb.getWeekNextEvent(hourMin, day, prof_indexes);
                if (nextEventMin > 0) {
                    scheduleAlarm(context, prefs, c, nextEventMin, displayToast);
                }
            }
            
        } finally {
            profilesDb.onDestroy();
        }
    }

    private void performActions(SettingsHelper settings, ArrayList<ActionInfo> actions) {
        for (ActionInfo info : actions) {
            performAction(settings, info.mActions);
        }
    }

    private void performAction(SettingsHelper settings, String actions) {
        if (actions == null) return;
        
        RingerMode ringerMode = null;
        VibrateRingerMode vibRingerMode = null;
        
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
                            ringerMode = mode;
                            break;
                        }
                    }
                    break;
                case Columns.ACTION_VIBRATE:
                    for (VibrateRingerMode mode : VibrateRingerMode.values()) {
                        String name = mode.toString();
                        if (name.charAt(0) == v) {
                            vibRingerMode = mode;
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

        if (ringerMode != null || vibRingerMode != null) {
            settings.changeRingerVibrate(ringerMode, vibRingerMode);
        }

    }

    /** Notify UI to update */
    private void notifyDataChanged(Context context) {
        Context c = context.getApplicationContext();
        if (c instanceof TimerifficApp) {
            ((TimerifficApp) c).invokeDataListener();
        }
    }

    /**
     * Schedule an alarm to happen at nextEventMin minutes from now.
     * 
     * @param context App context to get alarm service.
     * @param prefs Access to prefs (for status update)
     * @param now The time that was used at the beginning of the update.
     * @param nextEventMin The number of minutes ( > 0) after "now" where to set the alarm.
     * @param displayToast One of {@link #TOAST_NONE}, {@link #TOAST_IF_CHANGED} or {@link #TOAST_ALWAYS}
     */
    private void scheduleAlarm(Context context,
            PrefsValues prefs,
            Calendar now,
            int nextEventMin,
            int displayToast) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Intent intent = new Intent(ACTION_AUTO_CHECK_STATE);
        PendingIntent op = PendingIntent.getBroadcast(context, 0 /*requestCode*/, intent, PendingIntent.FLAG_ONE_SHOT);

        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        now.add(Calendar.MINUTE, nextEventMin);

        long timeMs = now.getTimeInMillis();

        manager.set(AlarmManager.RTC_WAKEUP, timeMs, op);

        boolean shouldDisplayToast = displayToast != TOAST_NONE;
        if (displayToast == TOAST_IF_CHANGED) {
            shouldDisplayToast = timeMs != prefs.getLastScheduledAlarm();
        }
        
        prefs.setLastScheduledAlarm(timeMs);
        
        if (DEBUG || shouldDisplayToast) {
            try {
                Configuration config = new Configuration();
                Settings.System.getConfiguration(context.getContentResolver(), config);
                
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, config.locale);
                String s2 = df.format(now.getTime());
                s2 = "Next Change: " + s2;

                prefs.setStatusMsg(s2);
                if (shouldDisplayToast) Toast.makeText(context, s2, Toast.LENGTH_LONG).show();
                if (DEBUG) Log.d(TAG, s2);

            } catch (Throwable t) {
                Log.w(TAG, t);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                sdf.setCalendar(now);
                String s2 = sdf.format(now.getTime());
                s2 = String.format("Next Change: %s", s2);

                prefs.setStatusMsg(s2);
                if (shouldDisplayToast) Toast.makeText(context, s2, Toast.LENGTH_LONG).show();
                if (DEBUG) Log.d(TAG, s2);
            }
        }
    }
}
