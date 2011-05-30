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

    public int getMinBrightness() {
        return mPrefs.getInt("minBrightness", 10);
    }

    public void setMinBrightness(int brightness) {
        apply(mPrefs.edit().putInt("minBrightness", brightness));
    }

    public int getMaxBrightness() {
        return mPrefs.getInt("maxBrightness", 75);
    }

    public void setMaxBrightness(int brightness) {
        apply(mPrefs.edit().putInt("maxBrightness", brightness));
    }

    public int getCarBrightness() {
        return mPrefs.getInt("carBrightness", 90);
    }

    public void setCarBrightness(int brightness) {
        apply(mPrefs.edit().putInt("carBrightness", brightness));
    }

    public boolean getUseCarBrightness() {
        return mPrefs.getBoolean("useCarBrightness", false);
    }

    public void setUseCarBrightness(boolean use) {
        apply(mPrefs.edit().putBoolean("useCarBrightness", use));
    }

    public int getDeskBrightness() {
        return mPrefs.getInt("deskBrightness", 50);
    }

    public void setDeskBrightness(int brightness) {
        apply(mPrefs.edit().putInt("deskBrightness", brightness));
    }

    public boolean getUseDeskBrightness() {
        return mPrefs.getBoolean("useDeskBrightness", false);
    }

    public void setUseDeskBrightness(boolean use) {
        apply(mPrefs.edit().putBoolean("useDeskBrightness", use));
    }

    public int getSavedBrightness() {
        return mPrefs.getInt("savedBrightness", -1);
    }

    public void setSavedBrightness(int brightness) {
        apply(mPrefs.edit().putInt("savedBrightness", brightness));
    }

    public boolean isIntroDismissed() {
        return mPrefs.getBoolean("dismiss_intro", false);
    }

    /**
     * Sets the dismiss_intro boolean value.
     * @return true if value was successfully changed if the prefs
     */
    public void setIntroDismissed(boolean dismiss) {
        apply(mPrefs.edit().putBoolean("dismiss_intro", dismiss));
    }

    // ---

    private void apply(Editor editor) {
        if (Utils.getApiLevel() >= 9) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

}
