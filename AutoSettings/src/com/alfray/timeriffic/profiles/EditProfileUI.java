/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License TBD
 */

package com.alfray.timeriffic.profiles;

import com.alfray.timeriffic.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class EditProfileUI extends Activity {

    private static String TAG = "EditProfileUI";
    
    /** Extra long with the profile id (not index) to edit. */
    public static final String EXTRA_PROFILE_ID = "prof_id";
    private EditText mNameField;
    private CheckBox mEnabledCheck;
    private long mProfId;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.edit_profile);
        setTitle("Edit Profile");
        
        Intent intent = getIntent();
        mProfId = intent.getExtras().getLong(EXTRA_PROFILE_ID);
        
        Log.d(TAG, String.format("edit prof_id: %08x", mProfId));
        
        if (mProfId == 0) {
            Log.e(TAG, "profile id not found in intent.");
            finish();
            return;
        }
        
        
        // get profiles db helper
        ProfilesDB profilesDb = new ProfilesDB();
        profilesDb.onCreate(this);
        
        // get cursor
        String prof_id_select = String.format("%s=%d", Columns.PROFILE_ID, mProfId);
        Cursor c = profilesDb.query(
                -1, // id
                // projection, a.k.a. the list of columns to retrieve from the db
                new String[] {
                        Columns.PROFILE_ID,
                        Columns.DESCRIPTION,
                        Columns.IS_ENABLED
                },
                prof_id_select, // selection
                null, // selectionArgs
                null // sortOrder
                );
        try {
            if (!c.moveToFirst()) {
                Log.e(TAG, "cursor is empty: " + prof_id_select);
                finish();
                return;
            }

            // get UI widgets
            mNameField = (EditText) findViewById(R.id.name);
            mEnabledCheck = (CheckBox) findViewById(R.id.enabled);
            
            // get column indexes
            int descColIndex = c.getColumnIndexOrThrow(Columns.DESCRIPTION);
            int enColIndex = c.getColumnIndexOrThrow(Columns.IS_ENABLED);

            // fill in UI from cursor data
            mNameField.setText(c.getString(descColIndex));
            mEnabledCheck.setChecked(c.getInt(enColIndex) != 0);
        } finally {
            c.close();
            profilesDb.onDestroy();
        }
        
        Button accept = (Button) findViewById(R.id.ok);
        accept.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                accept();
            }
        });
    }
    
    private void accept() {
        ProfilesDB profilesDb = new ProfilesDB();
        try {
            profilesDb.onCreate(this);
            profilesDb.updateProfile(
                    mProfId,
                    mNameField.getText().toString(),
                    mEnabledCheck.isChecked());
        } finally {
            profilesDb.onDestroy();
        }
        finish();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // do nothing, discard changes
    }
}
