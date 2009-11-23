/*
 * Project: Timeriffic
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

/**
 * Changes the global brightness, either setting an actual value.
 *
 * This <en>ensures</en> you can't shoot yourself in the foot by never
 * actually setting the brightness to zero. The minimun used is 10/255
 * which matches the code from the hidden PowerManager class.
 *
 * This is an ugly hack:
 * - for pre-Cupcake (sdk 3), uses the IHardwareTest hack.
 * - for Cupcake, uses the Window brightness mixed with a global setting
 *   that works for some obscure reason (it's actually a bug, which means it
 *   will be fixed.)
 *
 * Requires the following permissions:
 * - android.permission.HARDWARE_TEST for the pre-Cupcake hack
 * - android.permission.WRITE_SETTINGS to set the global setting
 */
public class ChangeBrightnessActivity extends Activity {

    private static final String TAG = "Timerfc-ChangeBrightness";

    /** Using 0 will actually turn the screen off! */
    private static final int BR_MIN = 1;
    /** Max brightness from the API (c.f. PowerManager source, constant
     *  is not public.) */
    private static final int BR_MAX = 255;

    public static final String INTENT_SET_BRIGHTNESS = "set";

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

        setContentView(R.layout.empty);

        Intent i = getIntent();
        float f = i.getFloatExtra(INTENT_SET_BRIGHTNESS, -1);

        if (f >= 0) {
            setCurrentBrightness(f);
        }

        Message msg = mHandler.obtainMessage(42);
        mHandler.sendMessageDelayed(msg, 1000); // this makes it all work
    }

    private void setCurrentBrightness(float f) {

        int v = (int) (BR_MAX * f);
        if (v < BR_MIN) {
            // never set backlight too dark
            v = BR_MIN;
            f = (float)v / BR_MAX;
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
     * Returns screen brightness in range 0..1.
     */
    public static float getCurrentBrightness(Context context) {
        try {
            int v = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);

            return (float)v / BR_MAX;
        } catch (SettingNotFoundException e) {
            // If not found, return some acceptable default
            return 0.75f;
        }
    }

}
