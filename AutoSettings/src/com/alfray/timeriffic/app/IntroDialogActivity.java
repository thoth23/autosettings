/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License: GPLv3
 */

package com.alfray.timeriffic.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
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
public class IntroDialogActivity extends Activity {

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

            wv.getSettings().setJavaScriptEnabled(true);
            wv.addJavascriptInterface(jsVersion, "JSTimerifficVersion");

            wv.loadUrl("file:///android_asset/intro.html");
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
