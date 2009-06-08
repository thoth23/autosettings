package com.alfray.brighteriffic;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class BrighterifficUI extends Activity {

    //private static final String TAG = "BrighterifficUI";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        ((TextView) findViewById(R.id.introText)).setText(
                getResources().getString(R.string.hello, longVersion()));

        findViewById(R.id.Button01).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BrighterifficUI.this, ChangeBrightnessActivity.class);
                i.putExtra(ChangeBrightnessActivity.INTENT_SET_BRIGHTNESS, 0.1f);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });

        findViewById(R.id.Button02).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BrighterifficUI.this, ChangeBrightnessActivity.class);
                i.putExtra(ChangeBrightnessActivity.INTENT_SET_BRIGHTNESS, 0.75f);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

    public String longVersion() {
        PackageManager pm = getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(getPackageName(), 0);
            return pi.versionName;
        } catch (NameNotFoundException e) {
            return "??"; // failed, ignored
        }
    }

    private void installToggleShortcut() {

        // The intent we generate
        Intent intent = new Intent("com.alfray.brighteriffic.TOGGLE_BRIGHTNESS");
        intent.putExtra(ChangeBrightnessActivity.INTENT_TOGGLE_BRIGHTNESS, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent result = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        result.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);

        // Set the name of the activity
        result.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Toggle Brightness");

        // Build the icon info for the activity
        Drawable drawable = getResources().getDrawable(R.drawable.icon96);

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) drawable;
            Bitmap bitmap = bd.getBitmap();
            result.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
        }

        sendBroadcast(result);
    }
}