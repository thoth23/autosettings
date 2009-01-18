/*
 * (c) ralfoide gmail com, 2008
 * Project: Timeriffic
 * License: GPLv3
 */

package com.alfray.timeriffic;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class AutoReceiver extends BroadcastReceiver {

    public final static String ACTION_AUTO_CHECK_STATE = "com.alfray.intent.action.AUTO_CHECK_STATE";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        
        boolean isBoot = Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction());
        
        PrefsValues prefs = new PrefsValues(context);

        prefs.appendToLog("Checking "
                + (prefs.enableService() ? "enabled" : "disabled")
                + (isBoot ? " (boot)" : ""));
        
        if (!prefs.enableService()) return;

        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(System.currentTimeMillis());
        int hour = c.get(Calendar.HOUR_OF_DAY);
        
        int start = prefs.startHour();
        int stop = prefs.stopHour();
        
        boolean inRange;
        if (start <= stop) {
            inRange = hour >= start && hour < stop;
        } else {
            inRange = hour < stop || hour >= start;
        }
        
        SettingsHelper helper = new SettingsHelper(context);
        if (inRange) {
            prefs.appendToLog("Apply START settings");
            helper.applyStartSettings();
            scheduleAlarm(context, prefs, stop); // schedule alarm at stop time
        } else {
            prefs.appendToLog("Apply STOP settings");
            helper.applyStopSettings();
            scheduleAlarm(context, prefs, start); // schedule alarm at start time
        }
    }

    private void scheduleAlarm(Context context, PrefsValues prefs, int hour) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Intent intent = new Intent(ACTION_AUTO_CHECK_STATE);
        PendingIntent op = PendingIntent.getBroadcast(context, 0 /*requestCode*/, intent, PendingIntent.FLAG_ONE_SHOT);

        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(System.currentTimeMillis());
        int now_hour = c.get(Calendar.HOUR_OF_DAY);
        
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        if (hour > now_hour) {
            c.add(Calendar.HOUR_OF_DAY, hour - now_hour);
        } else if (hour < now_hour) {
            c.add(Calendar.HOUR_OF_DAY, 24 - now_hour + hour);
        }

        long timeMs = c.getTimeInMillis();
        
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy HH:mm:ss");
        sdf.setCalendar(c);
        String s2 = sdf.format(c.getTime());
        prefs.appendToLog(String.format("Alarm for %s", s2));

        manager.set(AlarmManager.RTC_WAKEUP, timeMs, op);
    }
}
