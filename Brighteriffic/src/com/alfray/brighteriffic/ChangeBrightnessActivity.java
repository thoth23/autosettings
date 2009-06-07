/*
 * (c) ralfoide gmail com, 2009
 * Project: Brighteriffic
 * License TBD
 */

/**
 *
 */
package com.alfray.brighteriffic;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * @author ralf
 *
 */
public class ChangeBrightnessActivity extends Activity {

    public static final String INTENT_SET_BRIGHTNESS = "set";
    public static final String INTENT_TOGGLE_BRIGHTNESS = "toggle";
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

        // Have the system blur any windows behind this one.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                             WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        setContentView(R.layout.empty);

        Intent i = getIntent();
        float f = i.getFloatExtra(INTENT_SET_BRIGHTNESS, -1);

        if (f >= 0.1) {
            setCurrentBrightness(f);

        } else if (i.getBooleanExtra(INTENT_TOGGLE_BRIGHTNESS, false)) {
            if (getCurrentBrightness() > 50) {
                setCurrentBrightness(0.1f);
            } else {
                setCurrentBrightness(0.75f);
            }

        }

        Message msg = mHandler.obtainMessage(42);
        mHandler.sendMessageDelayed(msg, 1000); // this makes it all work
    }

    private void setCurrentBrightness(float f) {
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

    /**
     * Returns screen brightness in range 0..1%.
     */
    public float getCurrentBrightness() {
        try {
            int v = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);

            return v / 255.0f;
        } catch (SettingNotFoundException e) {
            // If not found, return default
            return 0.75f;
        }
    }

}
