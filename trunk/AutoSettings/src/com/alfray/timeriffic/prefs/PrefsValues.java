/*
 * Copyright 2008 (c) ralfoide gmail com, 2008
 * Project: auto settings
 * License: GPL version 3 or any later version
 */

package com.alfray.timeriffic.prefs;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefsValues {

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
    
    public String getStatusMsg() {
        return mPrefs.getString("status_msg", "N/A");
    }
    
    public void setStatusMsg(String status) {
        mPrefs.edit().putString("status_msg", status).commit();
    }
    
    public long getLastScheduledAlarm() {
        return mPrefs.getLong("last_alarm", 0);
    }
    
    public void setLastScheduledAlarm(long timeMs) {
        mPrefs.edit().putLong("last_alarm", timeMs).commit();
    }
}
