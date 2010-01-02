/*
 * Project: Timeriffic
 * Copyright (C) 2009 ralfoide gmail com,
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
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.actions.TimedActionUtils;
import com.alfray.timeriffic.error.ExceptionHandler;
import com.alfray.timeriffic.prefs.PrefsValues;
import com.alfray.timeriffic.profiles.Columns;
import com.alfray.timeriffic.profiles.ProfilesDB;
import com.alfray.timeriffic.profiles.ProfilesDB.ActionInfo;
import com.alfray.timeriffic.utils.AgentWrapper;
import com.alfray.timeriffic.utils.SettingsHelper;
import com.alfray.timeriffic.utils.SettingsHelper.RingerMode;
import com.alfray.timeriffic.utils.SettingsHelper.VibrateRingerMode;


public class ApplySettings {

    private final static boolean DEBUG = true;
    public final static String TAG = "TFC-ApplySettings";
    private final Context mContext;
    private SimpleDateFormat mDateFormat;
    private final PrefsValues mPrefs;

    public ApplySettings(Context context, PrefsValues prefs) {
        mContext = context;
        mPrefs = prefs;
        mDateFormat = new SimpleDateFormat(context.getString(R.string.globalstatus_nextlast_date_time));
    }

    private void showToast(String s, int duration) {
        try {
            Toast.makeText(mContext, s, duration).show();
        } catch (Throwable t) {
            ExceptionHandler.addToLog(mPrefs, t);
            Log.w(TAG, "Toast.show crashed", t);
        }
    }

    public void apply(int displayToast) {
        Log.d(TAG, "Checking enabled");

        AgentWrapper agentWrapper = new AgentWrapper();
        try {
            agentWrapper.start(mContext);
            agentWrapper.event(AgentWrapper.Event.CheckProfiles);

            checkProfiles(displayToast);
            notifyDataChanged();
        } finally{} {
            agentWrapper.stop(mContext);
        }
    }

    private void checkProfiles(int displayToast) {
        ProfilesDB profilesDb = new ProfilesDB();
        try {
            profilesDb.onCreate(mContext);

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
                    performActions(actions);
                    profilesDb.markActionsEnabled(actions);
                }

                // Compute next event and set an alarm for it
                StringBuilder nextActions = new StringBuilder();
                int nextEventMin = profilesDb.getWeekNextEvent(hourMin, day, prof_indexes, nextActions);
                if (nextEventMin > 0) {
                    scheduleAlarm(c, nextEventMin, nextActions, displayToast);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "checkProfiles failed", e);
            ExceptionHandler.addToLog(mPrefs, e);

        } finally {
            profilesDb.onDestroy();
        }
    }

    private void performActions(ArrayList<ActionInfo> actions) {

        String logActions = null;
        String lastAction = null;
        SettingsHelper settings = null;

        for (ActionInfo info : actions) {
            try {
                if (settings == null) settings = new SettingsHelper(mContext);

                if (performAction(settings, info.mActions)) {
                    lastAction = info.mActions;
                    if (logActions == null) {
                        logActions = lastAction;
                    } else {
                        logActions += " | " + lastAction;
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to apply setting", e);
                ExceptionHandler.addToLog(mPrefs, e);
            }
        }

        if (lastAction != null) {
            // Format the timestamp of the last action to be "now"
            String time = mDateFormat.format(new Date(System.currentTimeMillis()));
            mPrefs.setStatusLastTS(time);

            // Format the action description
            String a = TimedActionUtils.computeActions(mContext, lastAction);
            mPrefs.setStatusNextAction(a);

            addToDebugLog(time, logActions);
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
                        case Columns.ACTION_NOTIF_VOLUME:
                            settings.changeNotificationVolume(value);
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
                        case Columns.ACTION_BLUETOOTH:
                            settings.changeBluetooh(value > 0);
                            didSomething = true;
                            break;
                        case Columns.ACTION_APN_DROID:
                            settings.changeApnDroid(value > 0);
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
    private void notifyDataChanged() {
        Context c = mContext.getApplicationContext();
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
    private void scheduleAlarm(
            Calendar now,
            int nextEventMin,
            StringBuilder nextActions,
            int displayToast) {
        AlarmManager manager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(AutoReceiver.ACTION_AUTO_CHECK_STATE);
        PendingIntent op = PendingIntent.getBroadcast(
                        mContext,
                        0 /*requestCode*/,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        now.add(Calendar.MINUTE, nextEventMin);

        long timeMs = now.getTimeInMillis();

        manager.set(AlarmManager.RTC_WAKEUP, timeMs, op);

        boolean shouldDisplayToast = displayToast != AutoReceiver.TOAST_NONE;
        if (displayToast == AutoReceiver.TOAST_IF_CHANGED) {
            shouldDisplayToast = timeMs != mPrefs.getLastScheduledAlarm();
        }

        mPrefs.setLastScheduledAlarm(timeMs);

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
//                if (shouldDisplayToast) showToast(context, s2, Toast.LENGTH_LONG);
//                if (DEBUG) Log.d(TAG, s2);
//
//            } catch (Throwable t) {
//                Log.w(TAG, t);
//          }

        SimpleDateFormat sdf = new SimpleDateFormat(mContext.getString(R.string.toast_next_alarm_date_time));
        sdf.setCalendar(now);
        String s2 = sdf.format(now.getTime());

        mPrefs.setStatusNextTS(s2);
        mPrefs.setStatusNextAction(TimedActionUtils.computeActions(mContext, nextActions.toString()));

        s2 = mContext.getString(R.string.toast_next_change_at_datetime, s2);


        if (shouldDisplayToast) showToast(s2, Toast.LENGTH_LONG);
        if (DEBUG) Log.d(TAG, s2);
        addToDebugLog(s2);
    }


    protected static final String SEP_START = " [ ";
    protected static final String SEP_END = " ]\n";

    /** Add debug log for now. */
    /* package */ void addToDebugLog(String message) {
        String time = mDateFormat.format(new Date(System.currentTimeMillis()));
        addToDebugLog(time, message);
    }

    /** Add time:action specific log. */
    protected synchronized void addToDebugLog(String time, String logActions) {

        logActions = time + SEP_START + logActions + SEP_END;
        int len = logActions.length();

        // We try to keep only 4k in the buffer
        int max = 4096;

        String a = null;

        if (logActions.length() < max) {
            a = mPrefs.getLastActions();
            if (a != null) {
                int lena = a.length();
                if (lena + len > max) {
                    int extra = lena + len - max;
                    int pos = -1;
                    int p = -1;
                    do {
                        pos = a.indexOf(SEP_END, pos + 1);
                        p = pos + SEP_END.length();
                    } while (pos >= 0 && p < extra);

                    if (pos < 0 || p >= lena) {
                        a = null;
                    } else {
                        a = a.substring(p);
                    }
                }
            }
        }

        if (a == null) {
            mPrefs.setLastActions(logActions);
        } else {
            a += logActions;
            mPrefs.setLastActions(a);
        }
    }

    public static synchronized int getNumActionsInLog(Context context) {
        try {
            PrefsValues pv = new PrefsValues(context);
            String curr = pv.getLastActions();

            if (curr == null) {
                return 0;
            }

            int count = -1;
            int pos = -1;
            do {
                count++;
                pos = curr.indexOf(" ]", pos + 1);
            } while (pos >= 0);

            return count;

        } catch (Exception e) {
            Log.d(TAG, "getNumActionsInLog failed", e);
            ExceptionHandler.addToLog(new PrefsValues(context), e);
        }

        return 0;
    }

}
