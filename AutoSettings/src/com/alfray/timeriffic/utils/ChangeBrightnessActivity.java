/*
 * (c) ralfoide gmail com, 2009
 * Project: Brighteriffic
 * License TBD
 */

/**
 *
 */
package com.alfray.timeriffic.utils;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
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
public class ChangeBrightnessActivity extends Activity {

    public static final String INTENT_EXTRA_BRIGHTNESS = "brightness";
    Handler mHandler;

    public ChangeBrightnessActivity() {
        mHandler = new Handler() {
          @Override
            public void handleMessage(Message msg) {
                if (msg.what == 42) {
                    ChangeBrightnessActivity.this.finish();
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

        if (f >= 0) {
            if (f < 0.05f) f = 0.05f; // don't set display too dark

            Settings.System.putInt(this.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    (int)(255 * f));

            int sdk = Integer.parseInt(Build.VERSION.SDK);
            if (sdk >= 3) {
                Window win = getWindow();
                LayoutParams attr = win.getAttributes();

                Field field;
                try {
                    field = attr.getClass().getField("screenBrightness");
                    field.setFloat(attr, f);
                } catch (SecurityException e) {
                } catch (NoSuchFieldException e) {
                } catch (IllegalArgumentException e) {
                } catch (IllegalAccessException e) {
                }

                win.setAttributes(attr);
            }
        }

        Message msg = mHandler.obtainMessage(42);
        mHandler.sendMessageDelayed(msg, 1000);
    }

}
