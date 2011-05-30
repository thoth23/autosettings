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
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class BrighterifficUI extends Activity {

    @SuppressWarnings("unused")
    private static final String TAG = BrighterifficUI.class.getSimpleName();


    private interface IMinMaxActions {
        /** Returns the current pref. */
        public int getPrefValue();
        /** Set new pref value. */
        public void setPrefValue(int percent);
        /** Changes the brightness to the one from the current pref. */
        public void applyValue();
    }

    private View mToggleButton;
    private PrefsValues mPrefsValues;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        mPrefsValues = new PrefsValues(this);

        initUi();
        showIntroAtStartup();
    }

    private void initUi() {
        TextView desc = ((TextView) findViewById(R.id.introText));
        desc.setText(getString(R.string.intro_text, longVersion()));

        mToggleButton = findViewById(R.id.toggleButton);
        mToggleButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                installToggleShortcut();
            }
        });

        initMinMaxPart(R.id.part_min,
                getString(R.string.min_title),
                getString(R.string.min_button_percent),
                new IMinMaxActions() {
                    public int getPrefValue() {
                        return mPrefsValues.getMinBrightness();
                    }
                    public void setPrefValue(int percent) {
                        mPrefsValues.setMinBrightness(percent);
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
                        return mPrefsValues.getMaxBrightness();
                    }
                    public void setPrefValue(int percent) {
                        mPrefsValues.setMaxBrightness(percent);
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

        // Dock events are only for API 5+
        if (Utils.getApiLevel() >= 5) {
            final View carView = initMinMaxPart(R.id.part_car,
                    getString(R.string.car_title),
                    getString(R.string.car_button_percent),
                    new IMinMaxActions() {
                        public int getPrefValue() {
                            return mPrefsValues.getCarBrightness();
                        }
                        public void setPrefValue(int percent) {
                            mPrefsValues.setCarBrightness(percent);
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

            CheckBox cb = (CheckBox) findViewById(R.id.check_car);
            cb.setChecked(mPrefsValues.getUseCarBrightness());
            enableView(carView, cb.isChecked());
            cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mPrefsValues.setUseCarBrightness(isChecked);
                    enableView(carView, isChecked);
                }
            });

            final View deskView = initMinMaxPart(R.id.part_desk,
                    getString(R.string.desk_title),
                    getString(R.string.desk_button_percent),
                    new IMinMaxActions() {
                        public int getPrefValue() {
                            return mPrefsValues.getDeskBrightness();
                        }
                        public void setPrefValue(int percent) {
                            mPrefsValues.setDeskBrightness(percent);
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

            cb = (CheckBox) findViewById(R.id.check_desk);
            cb.setChecked(mPrefsValues.getUseDeskBrightness());
            enableView(deskView, cb.isChecked());
            cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mPrefsValues.setUseDeskBrightness(isChecked);
                    enableView(deskView, isChecked);
                }
            });
        }
    }

    private void enableView(View v, boolean enabled) {
        v.setEnabled(enabled);
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int n = vg.getChildCount() - 1; n >= 0; n--) {
                enableView(vg.getChildAt(n), enabled);
            }
        }
    }

    private BrighterifficApp getApp() {
        Application app = getApplication();
        if (app instanceof BrighterifficApp) return (BrighterifficApp) app;
        return null;
    }

    private void showIntroAtStartup() {
        final BrighterifficApp tapp = getApp();
        if (tapp.isFirstStart() && mToggleButton != null) {
            final Runnable action = new Runnable() {
                @Override
                public void run() {
                    showIntro(false);
                    tapp.setFirstStart(false);
                }
            };

            final ViewTreeObserver obs = mToggleButton.getViewTreeObserver();
            obs.addOnPreDrawListener(new OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mToggleButton.postDelayed(action, 200 /*delayMillis*/);
                    ViewTreeObserver obs2 = mToggleButton.getViewTreeObserver();
                    obs2.removeOnPreDrawListener(this);
                    return true;
                }
            });
        }
    }

    private void showIntro(boolean force) {
        if (force || !mPrefsValues.isIntroDismissed()) {
            Intent i = new Intent(this, IntroActivity.class);
            if (force) i.putExtra(IntroActivity.EXTRA_NO_CONTROLS, true);
            startActivity(i);
        }
    }

    private View initMinMaxPart(
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

        return group;
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
        Intent intent = new Intent(ChangeBrightnessActivity.ACTION_TOGGLE_BRIGHTNESS);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, R.string.about,  0, R.string.about).setIcon(R.drawable.ic_menu_help);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.string.about:
            showIntro(true /*force*/);
            break;
        }
        return super.onOptionsItemSelected(item);
    }
}