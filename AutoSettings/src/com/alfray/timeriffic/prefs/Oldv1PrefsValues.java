/*
 * Copyright 2008 (c) ralfoide gmail com, 2008
 * Project: auto settings
 * License: GPL version 3 or any later version
 */

package com.alfray.timeriffic.prefs;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Oldv1PrefsValues {

    public static final String KEY_START_HOUR = "start_hour";
    public static final String KEY_END_HOUR = "end_hour";

    public static final int VERSION = 0;
    
    private SharedPreferences mPrefs;
    
    public Oldv1PrefsValues(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    public SharedPreferences getPrefs() {
        return mPrefs;
    }

    public boolean isServiceEnabled() {
        return mPrefs.getBoolean("enable_serv", true);
    }
    
    /**
     * Sets the dismiss_intro boolean value.
     * @return true if value was successfully changed if the prefs
     */
    public boolean setServiceEnabled(boolean checked) {
        return mPrefs.edit().putBoolean("enable_serv", checked).commit();
    }

    /** Returns the start hour-min or -1 if not present. */
    public int startHourMin() {
        try {
            return mPrefs.getInt(KEY_START_HOUR, -1);
        } catch (ClassCastException e) {
            // The field used to be a String, so it could fail here
            String s = mPrefs.getString(KEY_START_HOUR, null);
            return s == null ? -1 : parseHoursMin(s);
        }
    }

    /** Returns the stop hour-min or -1 if not present. */
    public int stopHourMin() {
        try {
            return mPrefs.getInt(KEY_END_HOUR, -1);
        } catch (ClassCastException e) {
            // The field used to be a String, so it could fail here
            String s = mPrefs.getString(KEY_END_HOUR, null);
            return s == null ? -1 : parseHoursMin(s);
        }
    }

    private int parseHoursMin(String text) {
        int hours = 0;
        int minutes = 0;
        
        String[] numbers = text.trim().split(":");
        if (numbers.length >= 1) hours = parseNumber(numbers[0], 23);
        if (numbers.length >= 2) minutes = parseNumber(numbers[1], 59);

        return hours*60 + minutes;
    }

    private static int parseNumber(String string, int maxValue) {
        try {
            int n = Integer.parseInt(string);
            if (n < 0) return 0;
            if (n > maxValue) return maxValue;
            return n;
        } catch (Exception e) {
            // ignore
        }
        return 0;
    }
    
    public boolean startMute() {
        return mPrefs.getBoolean("start_mute", true);
    }

    public boolean startVibrate() {
        return mPrefs.getBoolean("start_vibrate", true);
    }

    public boolean stopMute() {
        return mPrefs.getBoolean("end_mute", false);
    }

    public boolean stopVibrate() {
        return mPrefs.getBoolean("end_vibrate", false);
    }
}
