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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class BrighterifficUI extends Activity {

    //private static final String TAG = "BrighterifficUI";

    private interface IMinMaxActions {
        /** Returns the current pref. */
        public int getPrefValue();
        /** Returns true if pref was correctly saved. */
        public boolean setPrefValue(int percent);
        /** Changes the brightness to the one from the current pref. */
        public void applyValue();
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        final PrefsValues prefValues = new PrefsValues(this);

        TextView desc = ((TextView) findViewById(R.id.introText));
        desc.setText(getString(R.string.intro_text, longVersion()));

        View v = findViewById(R.id.toggleButton);
        v.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                installToggleShortcut();
            }
        });

        initMinMaxPart(R.id.part_min,
                getString(R.string.min_title),
                getString(R.string.min_button_percent),
                new IMinMaxActions() {
                    public int getPrefValue() {
                        return prefValues.getMinBrightness();
                    }
                    public boolean setPrefValue(int percent) {
                        return prefValues.setMinBrightness(percent);
                    }
                    public void applyValue() {
                        Intent i = new Intent(BrighterifficUI.this, ChangeBrightnessActivity.class);
                        i.putExtra(ChangeBrightnessActivity.INTENT_SET_BRIGHTNESS,
                                        getPrefValue() / 100.0f);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    }
                }
        );

        initMinMaxPart(R.id.part_max,
                getString(R.string.max_title),
                getString(R.string.max_button_percent),
                new IMinMaxActions() {
                    public int getPrefValue() {
                        return prefValues.getMaxBrightness();
                    }
                    public boolean setPrefValue(int percent) {
                        return prefValues.setMaxBrightness(percent);
                    }
                    public void applyValue() {
                        Intent i = new Intent(BrighterifficUI.this, ChangeBrightnessActivity.class);
                        i.putExtra(ChangeBrightnessActivity.INTENT_SET_BRIGHTNESS,
                                        getPrefValue() / 100.0f);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    }
                }
        );
    }

    private void initMinMaxPart(
            int id,
            String title,
            final String buttonLabel,
            final IMinMaxActions actions) {

        View group = findViewById(id);

        TextView tv = (TextView) group.findViewById(R.id.minMaxTitle);
        tv.setText(title);

        final Button button = (Button) group.findViewById(R.id.minMaxSetButton);
        button.setText(String.format(buttonLabel, actions.getPrefValue()));
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                actions.applyValue();
            }
        });

        SeekBar seekBar = (SeekBar) group.findViewById(R.id.minMaxSeekBar);
        seekBar.setProgress(actions.getPrefValue());
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            public void onStopTrackingTouch(SeekBar seekBar) {
                // pass
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // pass
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                actions.setPrefValue(progress);
                button.setText(String.format(buttonLabel, actions.getPrefValue()));
            }
        });
    }

    public String longVersion() {
        PackageManager pm = getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(getPackageName(), 0);
            return pi.versionName;
        } catch (NameNotFoundException e) {
            return "??"; // failed, ignored
        }
    }

    private void installToggleShortcut() {

        // The intent we generate
        Intent intent = new Intent("com.alfray.brighteriffic.TOGGLE_BRIGHTNESS");
        intent.putExtra(ChangeBrightnessActivity.INTENT_TOGGLE_BRIGHTNESS, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent result = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        result.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);

        // Set the name of the activity
        result.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.home_shortcut_label));

        // Build the icon info for the activity
        Drawable drawable = getResources().getDrawable(R.drawable.icon96);

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) drawable;
            Bitmap bitmap = bd.getBitmap();
            result.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
        }

        sendBroadcast(result);
    }
}