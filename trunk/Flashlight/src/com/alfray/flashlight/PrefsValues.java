/*
 * Copyright 2009 (c) ralfoide gmail com, 2009
 * Project: flashlight
 * License: GPL version 3 or any later version
 */

package com.alfray.flashlight;


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

    public int getColorIndex() {
        return mPrefs.getInt("colorIndex", -1);
    }

    public void setColorIndex(int colorIndex) {
        mPrefs.edit().putInt("colorIndex", colorIndex).commit();
    }
}
