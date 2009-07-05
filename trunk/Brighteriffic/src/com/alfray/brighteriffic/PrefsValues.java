/*
 * Copyright 2008 (c) ralfoide gmail com, 2008
 * Project: auto settings
 * License: GPL version 3 or any later version
 */

package com.alfray.brighteriffic;


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

	public int getMinBrightness() {
	    return mPrefs.getInt("minBrightness", 10);
	}

	/** Returns true if value was successfully changed.
	 * This seems to always return false. */
	public boolean setMinBrightness(int minBrightness) {
        return mPrefs.edit().putInt("minBrightness", minBrightness).commit();
	}

    public int getMaxBrightness() {
        return mPrefs.getInt("maxBrightness", 75);
    }

    /** Returns true if value was successfully changed.
    * This seems to always return false. */
    public boolean setMaxBrightness(int maxBrightness) {
        return mPrefs.edit().putInt("maxBrightness", maxBrightness).commit();
    }

}
