package com.alfray.brighteriffic;

import android.app.Activity;
import android.os.Bundle;
import android.os.IHardwareService;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class BrighterifficUI extends Activity {

    private static final String TAG = "BrighterifficUI";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        findViewById(R.id.Button01).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBrightness(50, true);
            }
        });
    }

    private static int MIN_BRIGHTNESS = 10;  // android.os.Power.BRIGHTNESS_DIM + 10;
    private static int MAX_BRIGHTNESS = 255; // android.os.Power.BRIGHTNESS_ON;

    /**
     * @param percent The new value in 0..100 range (will get mapped to adequate OS values)
     * @param persistent True if the setting should be made persistent, e.g. written to system pref.
     *  If false, only the current hardware value is changed.
     */
    public void changeBrightness(int percent, boolean persistent) {
        // Reference:
        // http://android.git.kernel.org/?p=platform/packages/apps/Settings.git;a=blob;f=src/com/android/settings/BrightnessPreference.java
        // The source indicates
        // - Backlight range is 0..255
        // - Must not set to 0 (user would see nothing) so they use 10 as minimum
        // - All constants are in android.os.Power which is hidden from the SDK in 1.0
        //   yet available in 1.1
        // - To get value: Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        // - To set value: Settings.System.putInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, v);

        Log.d(TAG, "changeBrightness: " + Integer.toString(percent));

        int v = MIN_BRIGHTNESS + percent * (MAX_BRIGHTNESS - MIN_BRIGHTNESS) / 100;

        if (persistent) {
            Settings.System.putInt(this.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    v);
        }

        try {
            IHardwareService hs = IHardwareService.Stub.asInterface(ServiceManager.getService("hardware"));
            if (hs != null) hs.setBacklights(v);
        } catch (Throwable t) {
            Log.e(TAG, "Failed to set brightness to " + Integer.toString(v), t);
        }
    }

}