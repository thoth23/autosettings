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
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

/**
 * @author ralf
 *
 */
public class ToggleActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getCurrentBrightness() > 50) {
            setCurrentBrightness(0.1f);
        } else {
            setCurrentBrightness(0.75f);
        }

        finish();
    }

    private void setCurrentBrightness(float f) {
        Intent i = new Intent(this, ChangeBrightnessActivity.class);
        i.putExtra(ChangeBrightnessActivity.INTENT_EXTRA_BRIGHTNESS, f);
        startActivity(i);
    }

    private static int MIN_BRIGHTNESS = 10;  // android.os.Power.BRIGHTNESS_DIM + 10;
    private static int MAX_BRIGHTNESS = 255; // android.os.Power.BRIGHTNESS_ON;

    /**
     * Returns screen brightness in range 0..100%.
     * <p/>
     * See comments in {@link #changeBrightness(int)}. The real range is 0..255
     * but 10..255 is only usable (to avoid a non-readable screen). So map 10..255
     * to 0..100%.
     */
    public int getCurrentBrightness() {
        try {
            int v = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
            // transform 10..255 into 0..100
            v = (v - MIN_BRIGHTNESS) * 100 / (MAX_BRIGHTNESS - MIN_BRIGHTNESS);
            // clip to 0..100
            return Math.min(100, Math.max(0, v));
        } catch (SettingNotFoundException e) {
            // If not found, return max
            return MAX_BRIGHTNESS;
        }
    }
}
