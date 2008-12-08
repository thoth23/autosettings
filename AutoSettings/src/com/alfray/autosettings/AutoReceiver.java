/*
 * (c) ralfoide gmail com, 2008
 * Project: AutoSettings
 * License TBD
 */

package com.alfray.autosettings;

import java.util.Calendar;
import java.util.GregorianCalendar;

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
        int stop = prefs.stopdHour();
        
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
        } else {
            prefs.appendToLog("Apply STOP settings");
            helper.applyStopSettings();
        }
    }
}
