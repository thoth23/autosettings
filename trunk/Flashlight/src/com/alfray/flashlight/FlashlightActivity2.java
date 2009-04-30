/*
 * (c) ralfoide gmail com, 2009
 * Project: Flashlight
 * License: GPLv3
 */

package com.alfray.flashlight;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class FlashlightActivity2 extends Activity {

    private final String mTag;

    private PrefsValues mPrefs;

    private TextView mCurrentLabel;

    private ImageView mCurrentIcon;

    private static final String sColorNames[] = {
        "White",
        "Red",
        "Orange",
        "Yellow",
        "Green",
        "Cyan",
        "Blue",
        "Purple"
    };

    private static final int sColorHex[] = {
        0xFFFFFFFF,       // white
        0xFFFF0000,       // red
        0xFFFF9000,       // orange
        0xFFFFFF00,       // yellow
        0xFF00FF00,       // green
        0xFF00FFFF,       // cyan
        0xFF0000FF,       // blue
        0xFFFF00FF        // purple
    };

    public FlashlightActivity2(String tag) {
        mTag = tag;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mPrefs = new PrefsValues(this);
        applyColor(mPrefs.getColorIndex());

        findViewById(R.id.GoBright).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                goBright();
            }
        });

        findViewById(R.id.GoDark).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                goDark();
            }
        });

        mCurrentLabel = (TextView) findViewById(R.id.Label);
        mCurrentIcon = (ImageView) findViewById(R.id.CentralIcon);

        applyCurrentSetting();
        // DEBUG -- Log.d(mTag, "onCreate");
    }

    // ---- android workflow ----

    @Override
    protected void onResume() {
        super.onResume();

        // DEBUG -- Log.d(mTag, "onResume");
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();

        // DEBUG -- Log.d(mTag, "onUserLeaveHint");
    }

    @Override
    protected void onPause() {
        super.onPause();

        // DEBUG -- Log.d(mTag, "onPause");
    }

    // ----- menu ------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(0, R.string.menu_bright, 0, R.string.menu_bright).setIcon(R.drawable.bright_icon);
        menu.add(0, R.string.menu_dark  , 0, R.string.menu_dark  ).setIcon(R.drawable.dark_icon);
        menu.add(0, R.string.menu_about , 0, R.string.menu_about ).setIcon(R.drawable.ic_menu_help);
        menu.add(0, R.string.menu_color , 0, R.string.menu_color ).setIcon(R.drawable.color_icon);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.string.menu_bright:
            goBright();
            break;
        case R.string.menu_dark:
            goDark();
            break;
        case R.string.menu_about:
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
            break;
        case R.string.menu_color:
            showDialog(R.string.menu_color);
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(final int id) {

        if (id == R.string.menu_color) {
            Builder b = new AlertDialog.Builder(this);

            b.setTitle("Choose color");

            b.setSingleChoiceItems(sColorNames,
                    mPrefs.getColorIndex(),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            changeColor(which);
                            dismissDialog(R.string.menu_color);
                        }
            });

            return b.create();
        }

        return super.onCreateDialog(id);
    }

    // ---- internal stuff ------

    private void goBright() {
        mPrefs.setBrightness(1.0f);
        applyCurrentSetting();
    }

    private void goDark() {
        mPrefs.setBrightness(0.1f);
        applyCurrentSetting();
    }

    private void applyCurrentSetting() {
        float f = mPrefs.getBrightness();
        setBrightness(f);
        mCurrentLabel.setText(f < 0.5 ? "Dark" : "Bright");
        mCurrentIcon.setImageResource(f < 0.5 ? R.drawable.dark_icon : R.drawable.bright_icon);
    }

    private void setBrightness(float value) {
        // Make the screen full bright for this activity using the new
        // cupcake brightness API.
        //
        // Reference:
        //   ./development/apps/Development/src/com/android/development/PointerLocation.java
        // or
        //   http://android.git.kernel.org/?p=platform/development.git;a=blob;f=apps/Development/src/com/android/development/PointerLocation.java;h=668e9ba167f590c97481e348ced5f97d45f307c9;hb=HEAD

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = value;
        getWindow().setAttributes(lp);
        Log.d(mTag, "Set brightness to " + Float.toString(value));
    }

    private void changeColor(int colorIndex) {
        mPrefs.setColorIndex(colorIndex);
        applyColor(colorIndex);
    }

    private void applyColor(int colorIndex) {
        if (colorIndex < 0 || colorIndex >= sColorHex.length) colorIndex = 0;
        getWindow().setBackgroundDrawable(new ColorDrawable(sColorHex[colorIndex]));
    }
}
