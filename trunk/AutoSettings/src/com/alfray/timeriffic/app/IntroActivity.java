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

package com.alfray.timeriffic.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.prefs.PrefsValues;

/**
 */
public class IntroActivity extends Activity {

    private static final String TAG = "Timerfc-IntroDialog";
    public static final String EXTRA_NO_CONTROLS = "no-controls";

    private class JSTimerifficVersion {

        private String mVersion;

        public String longVersion() {
            if (mVersion == null) {
                PackageManager pm = getPackageManager();
                PackageInfo pi;
                try {
                    pi = pm.getPackageInfo(getPackageName(), 0);
                    mVersion = pi.versionName;
                } catch (NameNotFoundException e) {
                    mVersion = ""; // failed, ignored
                }
            }
            return mVersion;
        }

        public String shortVersion() {
            String v = longVersion();
            v = v.substring(0, v.lastIndexOf('.'));
            return v;
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.intro);

        JSTimerifficVersion jsVersion = new JSTimerifficVersion();

        String title = getString(R.string.intro_title, jsVersion.shortVersion());
        setTitle(title);

        WebView wv = (WebView) findViewById(R.id.web);
        if (wv != null) {

            // Make the webview transparent (for background gradient)
            wv.setBackgroundColor(0x00000000);

            // Inject a JS method to set the version
            wv.getSettings().setJavaScriptEnabled(true);
            wv.addJavascriptInterface(jsVersion, "JSTimerifficVersion");

            // Compute which file we want to display, i.e. try to select
            // one that matches intro-LocaleCountryName.html or default
            // to intro.html
            String file = "intro.html";
            Locale lo = Locale.getDefault();
            String lang = lo.getLanguage();
            if (lang != null && lang.length() == 2) {
                InputStream is = null;
                String file2 = "intro-" + lang + ".html";
                try {
                    AssetManager am = getResources().getAssets();

                    is = am.open(file2);
                    if (is != null) {
                        file = file2;
                    }

                } catch (IOException e) {
                    Log.d(TAG, "Asset not found: " + lang);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            // pass
                        }
                    }
                }
            }

            wv.loadUrl("file:///android_asset/" + file);
            wv.setFocusable(true);
            wv.setFocusableInTouchMode(true);
            wv.requestFocus();
        }

        boolean hideControls = false;
        Intent i = getIntent();
        if (i != null) {
            Bundle e = i.getExtras();
            if (e != null) hideControls = e.getBoolean(EXTRA_NO_CONTROLS);
        }

        CheckBox dismiss = (CheckBox) findViewById(R.id.dismiss);
        if (dismiss != null) {
            if (hideControls) {
                dismiss.setVisibility(View.GONE);
            } else {
                final PrefsValues pv = new PrefsValues(this);
                dismiss.setChecked(pv.isIntroDismissed());

                dismiss.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                            boolean isChecked) {
                        pv.setIntroDismissed(isChecked);
                    }
                });
            }
        }

        Button cont = (Button) findViewById(R.id.cont);
        if (cont != null) {
            if (hideControls) {
                cont.setVisibility(View.GONE);
            } else {
                cont.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // close activity
                        finish();
                    }
                });
            }
        }
    }
}
