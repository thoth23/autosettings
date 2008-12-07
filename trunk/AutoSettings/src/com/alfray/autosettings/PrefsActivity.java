/*
 * Copyright 2008 (c) ralfoide gmail com, 2008
 * Project: a2
 * License: GPL version 3 or any later version
 */

package com.alfray.autosettings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.WindowManager;

/**
 * Displays preferences
 */
public class PrefsActivity extends PreferenceActivity {

	public static final String EXTRA_ENABLE_VISUALS = "enable-visuals";

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		boolean enable_themes = intent.getBooleanExtra(EXTRA_ENABLE_VISUALS, true);

        // Have the system blur any windows behind this one.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

		setTitle(R.string.prefs_title);
		addPreferencesFromResource(R.xml.prefs);
	}
}
