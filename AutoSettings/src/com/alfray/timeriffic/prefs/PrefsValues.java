/*
 * Copyright 2008 (c) ralfoide gmail com, 2008
 * Project: auto settings
 * License: GPL version 3 or any later version
 */

package com.alfray.timeriffic.prefs;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PrefsValues {

    public static final String KEY_START_HOUR = "start_hour";
    public static final String KEY_END_HOUR = "end_hour";

    private SharedPreferences mPrefs;
	
	public PrefsValues(Context context) {
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

    public boolean isIntroDismissed() {
        return mPrefs.getBoolean("dismiss_intro", false);
    }
    
    /**
     * Sets the dismiss_intro boolean value.
     * @return true if value was successfully changed if the prefs
     */
    public boolean setIntroDismissed(boolean dismiss) {
        return mPrefs.edit().putBoolean("dismiss_intro", dismiss).commit();
    }

    public int startHourMin() {
        try {
            return mPrefs.getInt(KEY_START_HOUR, 10*60);
        } catch (ClassCastException e) {
            // The field used to be a String, so it could fail here
            String s = mPrefs.getString(KEY_START_HOUR, "10");
            return TimePreference.parseHoursMin(s);
        }
    }

    public int stopHourMin() {
        try {
            return mPrefs.getInt(KEY_END_HOUR, 14*60);
        } catch (ClassCastException e) {
            // The field used to be a String, so it could fail here
            String s = mPrefs.getString(KEY_END_HOUR, "14");
            return TimePreference.parseHoursMin(s);
        }
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
    
    public String getLog() {
        return mPrefs.getString("log", "");
    }
    
    public void appendToLog(String msg) {
        long now = System.currentTimeMillis();
        msg = String.format("%d:%s\n", now, msg);
        
        String current = mPrefs.getString("log", "");
        
        Editor editor = mPrefs.edit();
        
        try {
            editor.putString("log", current + msg);
        } finally {
            editor.commit();
        }
    }
}
