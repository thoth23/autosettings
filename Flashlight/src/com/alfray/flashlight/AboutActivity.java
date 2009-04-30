/*
 * (c) ralfoide gmail com, 2009
 * Project: Flashlight
 * License: GPLv3
 */

package com.alfray.flashlight;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AboutActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        Button goProject = (Button) findViewById(R.id.GoProject);
        goProject.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://code.google.com/p/autosettings/wiki/Flashlight"));
                startActivity(intent);
            }
        });
    }

}
