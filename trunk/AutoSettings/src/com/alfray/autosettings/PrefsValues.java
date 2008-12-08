/*
 * Copyright 2008 (c) ralfoide gmail com, 2008
 * Project: auto settigs
 * License: GPL version 3 or any later version
 */

package com.alfray.autosettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PrefsValues {

    private SharedPreferences mPrefs;
	
	public PrefsValues(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public SharedPreferences getPrefs() {
        return mPrefs;
    }

	public boolean enableService() {
	    return mPrefs.getBoolean("enable_serv", true);
	}

    public int startHour() {
        return mPrefs.getInt("start_hour", 21);
    }

    public int stopdHour() {
        return mPrefs.getInt("end_hour", 21);
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
