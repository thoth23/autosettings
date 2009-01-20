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
        int hourMin = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        
        int start = prefs.startHourMin();
        int stop = prefs.stopHourMin();
        
        boolean inRange;
        if (start <= stop) {
            inRange = hourMin >= start && hourMin < stop;
        } else {
            inRange = hourMin < stop || hourMin >= start;
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
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        sdf.setCalendar(c);
        String s2 = sdf.format(c.getTime());
        prefs.appendToLog(String.format("Alarm for %s", s2));

        manager.set(AlarmManager.RTC_WAKEUP, timeMs, op);
    }
}