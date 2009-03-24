package com.alfray.flashlight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
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
                Intent i = new Intent(FlashlightActivity.this, BrightlightActivity.class);
                startActivity(i);
                finish();
            }
        });

        findViewById(R.id.GoDark).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(FlashlightActivity.this, DarklightActivity.class);
                startActivity(i);
                finish();
            }
        });

        TextView tv = (TextView) findViewById(R.id.Label);
        initializeOnCreate(tv);
        Log.d(mTag, "onCreate");
    }

    protected abstract void initializeOnCreate(TextView tv);
    
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
}
