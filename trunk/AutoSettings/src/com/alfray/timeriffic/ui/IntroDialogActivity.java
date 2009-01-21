/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License: GPLv3
 */

package com.alfray.timeriffic.ui;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.R.id;
import com.alfray.timeriffic.R.layout;
import com.alfray.timeriffic.R.string;
import com.alfray.timeriffic.prefs.PrefsValues;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 */
public class IntroDialogActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.intro);
        setTitle(R.string.intro_title);
        
        WebView wv = (WebView) findViewById(R.id.web);
        if (wv != null) {
            wv.loadUrl("file:///android_asset/intro.html");
            wv.setFocusable(true);
            wv.setFocusableInTouchMode(true);
            wv.requestFocus();
        }
        
        CheckBox dismiss = (CheckBox) findViewById(R.id.dismiss);
        if (dismiss != null) {
            final PrefsValues pv = new PrefsValues(this);
            dismiss.setChecked(pv.dismissIntro());
            
            dismiss.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                        boolean isChecked) {
                    pv.setDismissIntro(isChecked);
                }
            });
        }
    }
}
