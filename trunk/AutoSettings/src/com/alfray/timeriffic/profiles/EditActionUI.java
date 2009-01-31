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
import android.widget.CheckBox;
import android.widget.EditText;

public class EditActionUI extends Activity {

    private static String TAG = "EditActionUI";
    
    /** Extra long with the action prof_id (not index) to edit. */
    public static final String EXTRA_ACTION_ID = "action_id";
    private long mActionId;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.edit_action);
        setTitle("Edit Timed Action");
        
        Intent intent = getIntent();
        mActionId = intent.getExtras().getLong(EXTRA_ACTION_ID);
        
        Log.d(TAG, String.format("edit prof_id: %08x", mActionId));
        
        if (mActionId == 0) {
            Log.e(TAG, "action id not found in intent.");
            finish();
            return;
        }
        
        String action_id_select = String.format("%s=%d",
                Columns.PROFILE_ID, mActionId);
        
        ProfilesDB profilesDb = new ProfilesDB();
        profilesDb.onCreate(this);
        Cursor c = profilesDb.query(
                -1, // id
                new String[] {
                        Columns.PROFILE_ID,
                        Columns.DESCRIPTION,
                        Columns.IS_ENABLED
                },
                action_id_select, // selection
                null, // selectionArgs
                null // sortOrder
                );
        try {
            /*
            int descColIndex = c.getColumnIndexOrThrow(Columns.DESCRIPTION);
            int enColIndex = c.getColumnIndexOrThrow(Columns.IS_ENABLED);

            if (c.moveToFirst()) {
                mNameField = (EditText) findViewById(R.id.name);
                mEnabledCheck = (CheckBox) findViewById(R.id.enabled);
                
                mNameField.setText(c.getString(descColIndex));
                mEnabledCheck.setChecked(c.getInt(enColIndex) != 0);
            } else {
                Log.e(TAG, "cursor is empty: " + action_id_select);
                finish();
                return;
            }
            */
        } finally {
            c.close();
            profilesDb.onDestroy();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();

        ProfilesDB profilesDb = new ProfilesDB();
        try {
            /*
            profilesDb.onCreate(this);
            profilesDb.updateProfile(
                    mActionId,
                    mNameField.getText().toString(),
                    mEnabledCheck.isChecked());
            */
        } finally {
            profilesDb.onDestroy();
        }
    }
}
