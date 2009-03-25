package com.alfray.flashlight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class FlashlightActivity extends Activity {

    private final String mTag;

    public FlashlightActivity(String tag) {
        mTag = tag;
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

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

        TextView tv = (TextView) findViewById(R.id.Label);
        ImageView ib = (ImageView) findViewById(R.id.CentralIcon);
        
        initializeOnCreate(tv, ib);
        Log.d(mTag, "onCreate");
    }

    protected abstract void initializeOnCreate(TextView label, ImageView icon);
    
    @Override
    protected void onResume() {
        super.onResume();

        Log.d(mTag, "onResume");
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();

        Log.d(mTag, "onUserLeaveHint");
    }
    
    @Override
    protected void onPause() {
        super.onPause();

        Log.d(mTag, "onPause");
    }

    protected void setBrightness(float value) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        menu.add(0, R.string.menu_bright, 0, R.string.menu_bright).setIcon(R.drawable.bright_icon);
        menu.add(0, R.string.menu_dark  , 0, R.string.menu_dark  ).setIcon(R.drawable.dark_icon);
        menu.add(0, R.string.menu_about , 0, R.string.menu_about ).setIcon(R.drawable.ic_menu_help);
        
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
        }
        return super.onOptionsItemSelected(item);
    }

    private void goBright() {
        Intent i = new Intent(this, BrightlightActivity.class);
        startActivity(i);
        finish();
    }

    private void goDark() {
        Intent i = new Intent(this, DarklightActivity.class);
        startActivity(i);
        finish();
    }
}
