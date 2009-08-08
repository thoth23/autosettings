/*
 * Copyright 2008 (c) ralfoide gmail com, 2008
 * Project: timeriffic
 * License: GPL version 3 or any later version
 */

package com.alfray.timeriffic.prefs;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.WindowManager;

import com.alfray.timeriffic.R;

/**
 * Displays preferences
 */
public class PrefsActivity extends PreferenceActivity {

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Have the system blur any windows behind this one.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

		setTitle(R.string.prefs_title);
		addPreferencesFromResource(R.xml.prefs);
	}
}
