/*
 * Copyright 2008 (c) ralfoide gmail com, 2008
 * Project: a2
 * License: GPL version 3 or any later version
 */

package com.alfray.timeriffic;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.WindowManager;

/**
 * Displays preferences
 */
public class PrefsActivity extends PreferenceActivity {

    private PrefsValues mPrefValues;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Have the system blur any windows behind this one.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

		setTitle(R.string.prefs_title);
		addPreferencesFromResource(R.xml.prefs);

		mPrefValues = new PrefsValues(this);
		
		Preference p = findPreference(PrefsValues.KEY_START_HOUR);
		if (p != null) {
		    p.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference pref, Object newValue) {
                    setHourMinLabel(pref, newValue);
                    return true;
                }
		    });
		    setHourMinLabel(p, null);
		}

        p = findPreference(PrefsValues.KEY_END_HOUR);
        if (p != null) {
            p.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference pref, Object newValue) {
                    setHourMinLabel(pref, newValue);
                    return true;
                }
            });
            setHourMinLabel(p, null);
        }
	}

    protected void setHourMinLabel(Preference pref, Object value) {
        int hourMin = 0;
        if (value instanceof Integer) {
            hourMin = ((Integer) value).intValue();
        } else if (pref.getKey().equals(PrefsValues.KEY_START_HOUR)) {
            hourMin = mPrefValues.startHourMin();
        } else if (pref.getKey().equals(PrefsValues.KEY_END_HOUR)) {
            hourMin = mPrefValues.stopHourMin();
        } else {
            // nothing applicable, give up
            pref.setSummary("--");
            return;
        }

        pref.setSummary(TimePreference.toHourMinStr(hourMin));
    }
}
