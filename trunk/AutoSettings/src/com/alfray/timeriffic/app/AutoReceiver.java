/*
 * Project: Timeriffic
 * Copyright (C) 2008 ralfoide gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

import com.alfray.timeriffic.R;
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
                    Toast.makeText(context,
                                    R.string.globalstatus_disabled,
                                    Toast.LENGTH_LONG)
                         .show();
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

    private void checkProfiles(Context context, int displayToast, PrefsValues prefs) {
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

                ArrayList<ActionInfo> actions =
                    profilesDb.getWeekActivableActions(hourMin, day, prof_indexes);

                if (actions != null && actions.size() > 0) {
                    performActions(context, actions, prefs);
                    profilesDb.markActionsEnabled(actions);
                }

                // Compute next event and set an alarm for it
                StringBuilder nextActions = new StringBuilder();
                int nextEventMin = profilesDb.getWeekNextEvent(hourMin, day, prof_indexes, nextActions);
                if (nextEventMin > 0) {
                    scheduleAlarm(context, prefs, c, nextEventMin, nextActions, displayToast);
                }
            }

        } finally {
            profilesDb.onDestroy();
        }
    }

    private void performActions(Context context, ArrayList<ActionInfo> actions, PrefsValues prefs) {

        String lastAction = null;
        SettingsHelper settings = null;

        for (ActionInfo info : actions) {

            if (settings == null) settings = new SettingsHelper(context);

            if (performAction(settings, info.mActions)) {
                lastAction = info.mActions;
            }
        }

        if (lastAction != null) {
            // Format the timestamp of the last action to be "now"
            SimpleDateFormat sdf = new SimpleDateFormat(context.getString(R.string.globalstatus_nextlast_date_time));
            Calendar c = new GregorianCalendar();
            c.setTimeInMillis(System.currentTimeMillis());
            sdf.setCalendar(c);
            String s = sdf.format(c.getTime());
            prefs.setStatusLastTS(s);

            // Format the action description
            s = TimedActionUtils.computeActions(context, lastAction);
            prefs.setStatusNextAction(s);
        }
    }

    private boolean performAction(SettingsHelper settings, String actions) {
        if (actions == null) return false;
        boolean didSomething = false;

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
                        if (mode.getActionLetter() == v) {
                            ringerMode = mode;
                            break;
                        }
                    }
                    break;
                case Columns.ACTION_VIBRATE:
                    for (VibrateRingerMode mode : VibrateRingerMode.values()) {
                        if (mode.getActionLetter() == v) {
                            vibRingerMode = mode;
                            break;
                        }
                    }
                    break;
                default:
                    try {
                        value = Integer.parseInt(action.substring(1));

                        switch(code) {
                        case Columns.ACTION_BRIGHTNESS:
                            settings.changeBrightness(value, true /*persist*/);
                            didSomething = true;
                            break;
                        case Columns.ACTION_RING_VOLUME:
                            settings.changeRingerVolume(value);
                            didSomething = true;
                            break;
                        case Columns.ACTION_WIFI:
                            settings.changeWifi(value > 0);
                            didSomething = true;
                            break;
                        case Columns.ACTION_AIRPLANE:
                            settings.changeAirplaneMode(value > 0);
                            didSomething = true;
                            break;
                        }

                    } catch (NumberFormatException e) {
                        // pass
                    }
                }
            }
        }

        if (ringerMode != null || vibRingerMode != null) {
            didSomething = true;
            settings.changeRingerVibrate(ringerMode, vibRingerMode);
        }

        return didSomething;
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
     * @param nextActions
     * @param displayToast One of {@link #TOAST_NONE}, {@link #TOAST_IF_CHANGED} or {@link #TOAST_ALWAYS}
     */
    private void scheduleAlarm(Context context,
            PrefsValues prefs,
            Calendar now,
            int nextEventMin,
            StringBuilder nextActions,
            int displayToast) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(ACTION_AUTO_CHECK_STATE);
        PendingIntent op = PendingIntent.getBroadcast(
                        context,
                        0 /*requestCode*/,
                        intent,
                        PendingIntent.FLAG_ONE_SHOT);

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

        // The DateFormat usage is commented out for 2 reasons:
        // 1- It gives weird results in French. Isolate code and file a bug report.
        // 2- We have a translatable time format that is used in the catch section only.
        //    It gives a false sense that the resource string might be used when it's not.
//            try {
//                Configuration config = new Configuration();
//                Settings.System.getConfiguration(context.getContentResolver(), config);
//
//                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, config.locale);
//                String s2 = df.format(now.getTime());
//                s2 = context.getString(R.string.toast_next_change_at_datetime, s2);
//
//                prefs.setStatusMsg(s2);
//                if (shouldDisplayToast) Toast.makeText(context, s2, Toast.LENGTH_LONG).show();
//                if (DEBUG) Log.d(TAG, s2);
//
//            } catch (Throwable t) {
//                Log.w(TAG, t);
//          }

        SimpleDateFormat sdf = new SimpleDateFormat(context.getString(R.string.toast_next_alarm_date_time));
        sdf.setCalendar(now);
        String s2 = sdf.format(now.getTime());

        prefs.setStatusNextTS(s2);
        prefs.setStatusNextAction(TimedActionUtils.computeActions(context, nextActions.toString()));

        s2 = context.getString(R.string.toast_next_change_at_datetime, s2);


        if (shouldDisplayToast) Toast.makeText(context, s2, Toast.LENGTH_LONG).show();
        if (DEBUG) Log.d(TAG, s2);
    }
}
