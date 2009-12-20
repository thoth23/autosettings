/*
 * Project: Timeriffic
 * Copyright (C) 2008 ralfoide gmail com,
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

package com.alfray.timeriffic.prefs;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefsValues {

    public static final int VERSION = 2;

    private SharedPreferences mPrefs;

	public PrefsValues(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public SharedPreferences getPrefs() {
        return mPrefs;
    }

	/** Returns pref version or 0 if not present. */
	public int getVersion() {
	    return mPrefs.getInt("version", 0);
	}

	public void setVersion() {
	    mPrefs.edit().putInt("version", VERSION).commit();
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

    public int getLastIntroVersion() {
        return mPrefs.getInt("last_intro_vers", 0);
    }

    public boolean setLastIntroVersion(int lastIntroVers) {
        return mPrefs.edit().putInt("last_intro_vers", lastIntroVers).commit();
    }

    public boolean getCheckService() {
        return mPrefs.getBoolean("check_service", false);
    }

    public boolean setCheckService(boolean check) {
        return mPrefs.edit().putBoolean("check_service", check).commit();
    }

    public String getStatusLastTS() {
        return mPrefs.getString("last_ts", null);
    }

    public void setStatusLastTS(String lastTS) {
        mPrefs.edit().putString("last_ts", lastTS).commit();
    }

    public String getStatusLastAction() {
        return mPrefs.getString("last_msg", null);
    }
    public void setStatusLastAction(String summary) {
        mPrefs.edit().putString("last_msg", summary).commit();
    }

    public String getStatusNextTS() {
        return mPrefs.getString("next_ts", null);
    }

    public void setStatusNextTS(String nextTS) {
        mPrefs.edit().putString("next_ts", nextTS).commit();
    }

    public String getStatusNextAction() {
        return mPrefs.getString("next_msg", null);
    }
    public void setStatusNextAction(String summary) {
        mPrefs.edit().putString("next_msg", summary).commit();
    }

    public long getLastScheduledAlarm() {
        return mPrefs.getLong("last_alarm", 0);
    }

    public void setLastScheduledAlarm(long timeMs) {
        mPrefs.edit().putLong("last_alarm", timeMs).commit();
    }

    public enum GlobalToggleAnimMode {
        NO_ANIM,
        SLOW,
        FAST
    }

    public GlobalToggleAnimMode getGlobalToggleAnim() {
        // "fast" is the default
        String s = mPrefs.getString("globaltoggle_anim", "fast");
        if ("no_anim".equals(s)) {
            return GlobalToggleAnimMode.NO_ANIM;
        } else if ("slow".equals(s)) {
            return GlobalToggleAnimMode.SLOW;
        }
        return GlobalToggleAnimMode.FAST;
    }
}
