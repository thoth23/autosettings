package com.alfray.brighteriffic;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
                Intent i = new Intent(BrighterifficUI.this, ChangeBrightnessActivity.class);
                i.putExtra(ChangeBrightnessActivity.INTENT_EXTRA_BRIGHTNESS, 0.1f);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });

        findViewById(R.id.Button02).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BrighterifficUI.this, ChangeBrightnessActivity.class);
                i.putExtra(ChangeBrightnessActivity.INTENT_EXTRA_BRIGHTNESS, 0.75f);
                startActivity(i);
            }
        });

        findViewById(R.id.Button03).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                installToggleShortcut();
            }
        });
    }

    private void installToggleShortcut() {

        // The intent we generate
        Intent intent = new Intent("com.alfray.brighteriffic.TOGGLE_BRIGHTNESS");

        Intent result = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        result.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);

        // Set the name of the activity
        result.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Toggle Brightness");

        // Build the icon info for the activity
        Drawable drawable = getResources().getDrawable(R.drawable.icon);

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) drawable;
            Bitmap bitmap = bd.getBitmap();
            result.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
        }

        sendBroadcast(result);
    }
}