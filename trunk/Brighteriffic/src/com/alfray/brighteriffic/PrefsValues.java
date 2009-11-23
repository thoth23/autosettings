/*
 * Project: Brighteriffic
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

}
