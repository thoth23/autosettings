package com.alfray.flashlight;

import android.widget.ImageView;
import android.widget.TextView;

public class DarklightActivity extends FlashlightActivity {

    public DarklightActivity() {
        super("Dark");
    }
    
    @Override
    protected void initializeOnCreate(TextView label, ImageView icon) {
        setBrightness(0.1f);
        label.setText("Dark");
        icon.setImageResource(R.drawable.dark_icon);
    }
}
