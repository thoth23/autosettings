/*
 * (c) ralfoide gmail com, 2009
 * Project: Flashlight
 * License: GPLv3
 */

package com.alfray.flashlight;

import android.widget.ImageView;
import android.widget.TextView;

public class BrightlightActivity extends FlashlightActivity {

    public BrightlightActivity() {
        super("Bright");
    }

    @Override
    protected void initializeOnCreate(TextView label, ImageView icon) {
        setBrightness(1.0f);
        label.setText("Bright");
        icon.setImageResource(R.drawable.bright_icon);
    }
}
