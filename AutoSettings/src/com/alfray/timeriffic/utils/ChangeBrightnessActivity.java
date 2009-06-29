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
import java.lang.reflect.Method;

import com.alfray.timeriffic.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IHardwareService;
import android.os.Message;
import android.os.ServiceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager.LayoutParams;


public class ChangeBrightnessActivity extends Activity {

    private static final String TAG = "ChangeBrightness";

    public static final String INTENT_SET_BRIGHTNESS = "set";
    public static final String INTENT_TOGGLE_BRIGHTNESS = "toggle";

    private Handler mHandler;

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
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
        //                     WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        setContentView(R.layout.empty);

        Intent i = getIntent();
        float f = i.getFloatExtra(INTENT_SET_BRIGHTNESS, -1);

        if (f >= 0) {
            setCurrentBrightness(f);

        } else if (i.getBooleanExtra(INTENT_TOGGLE_BRIGHTNESS, false)) {
            if (getCurrentBrightness(this) > 0.5f) {
                setCurrentBrightness(0.1f);
            } else {
                setCurrentBrightness(0.75f);
            }

        }

        Message msg = mHandler.obtainMessage(42);
        mHandler.sendMessageDelayed(msg, 1000); // this makes it all work
    }

    private void setCurrentBrightness(float f) {

        int v = (int) (255 * f);
        if (v < 10) {
            // never set backlight too dark
            v = 10;
            f = v / 255.f;
        }

        Settings.System.putInt(this.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,
                v);

        int sdk = Integer.parseInt(Build.VERSION.SDK);
        if (sdk >= 3) {

            try {
                Window win = getWindow();
                LayoutParams attr = win.getAttributes();
                Field field = attr.getClass().getField("screenBrightness");
                field.setFloat(attr, f);

                win.setAttributes(attr);

                Log.i(TAG, String.format("Changed brightness to %.2f [SDK 3+]", f));

            } catch (Throwable t) {
                Log.e(TAG, String.format("Failed to set brightness to %.2f [SDK 3+]", f), t);
            }

        } else {
            // Older SDKs
            try {
                IHardwareService hs = IHardwareService.Stub.asInterface(
                        ServiceManager.getService("hardware"));
                if (hs != null) {
                    Method m = hs.getClass().getMethod("setScreenBacklight", new Class[] { int.class });
                    if (m != null) {
                        m.invoke(hs, new Object[] { v });
                        Log.i(TAG, String.format("Changed brightness to %d [SDK<3]", v));
                    }
                }
            } catch (Throwable t) {
                Log.e(TAG, String.format("Failed to set brightness to %d [SDK<3]", v), t);
            }
        }
    }

    /**
     * Returns screen brightness in range 0..1%.
     */
    public static float getCurrentBrightness(Context context) {
        try {
            int v = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);

            return v / 255.0f;
        } catch (SettingNotFoundException e) {
            // If not found, return default
            return 0.75f;
        }
    }

}
