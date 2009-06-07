/*
 * (c) ralfoide gmail com, 2009
 * Project: Brighteriffic
 * License TBD
 */

/**
 *
 */
package com.alfray.brighteriffic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

/**
 * @author ralf
 *
 */
public class BrightnessActivity extends Activity {

    public static final String INTENT_EXTRA_BRIGHTNESS = "brightness";
    Handler mHandler;

    public BrightnessActivity() {
        mHandler = new Handler() {
          @Override
            public void handleMessage(Message msg) {
                if (msg.what == 42) {
                    BrightnessActivity.this.finish();
                }
                super.handleMessage(msg);
            }
        };
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        float f = i.getFloatExtra(INTENT_EXTRA_BRIGHTNESS, -1);

        if (f >= 0.1) {
            Settings.System.putInt(this.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    (int)(255 * f));


            Window win = getWindow();
            LayoutParams attr = win.getAttributes();
            attr.screenBrightness = f;
            win.setAttributes(attr);
        }

        Message msg = mHandler.obtainMessage(42);
        mHandler.sendMessageDelayed(msg, 1000);
    }

}
