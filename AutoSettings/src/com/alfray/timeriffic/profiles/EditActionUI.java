/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License TBD
 */

package com.alfray.timeriffic.profiles;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.alfray.timeriffic.R;

public class EditActionUI extends Activity {

    private static String TAG = "EditActionUI";
    
    /** Extra long with the action prof_id (not index) to edit. */
    public static final String EXTRA_ACTION_ID = "action_id";
    private long mActionId;

    private TimePicker mTimePicker;

    private ToggleButton mToggleRinger;

    private ToggleButton mToggleVib;

    /**
     * Day checkboxes, in the same index order than {@link Columns#MONDAY_BIT_INDEX}
     * to {@link Columns#SUNDAY_BIT_INDEX}.
     */
    private CheckBox[] mCheckDays;

    private CheckBox mCheckRinger;

    private CheckBox mCheckVib;

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
        
        // get profiles db helper
        ProfilesDB profilesDb = new ProfilesDB();
        profilesDb.onCreate(this);
        
        // get cursor
        String prof_id_select = String.format("%s=%d", Columns.PROFILE_ID, mActionId);
        Cursor c = profilesDb.query(
                -1, // id
                // projection, a.k.a. the list of columns to retrieve from the db
                new String[] {
                        Columns.PROFILE_ID,
                        Columns.HOUR_MIN,
                        Columns.DAYS,
                        Columns.ACTIONS
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
            mTimePicker = (TimePicker) findViewById(R.id.timePicker);
            mToggleRinger = (ToggleButton) findViewById(R.id.ringerToggle);
            mToggleVib = (ToggleButton) findViewById(R.id.vibToggle);
            mCheckRinger = (CheckBox) findViewById(R.id.ringerCheck);
            mCheckVib = (CheckBox) findViewById(R.id.vibCheck);
            mCheckDays = new CheckBox[] {
                    (CheckBox) findViewById(R.id.dayMon),
                    (CheckBox) findViewById(R.id.dayTue),
                    (CheckBox) findViewById(R.id.dayWed),
                    (CheckBox) findViewById(R.id.dayThu),
                    (CheckBox) findViewById(R.id.dayFri),
                    (CheckBox) findViewById(R.id.daySat),
                    (CheckBox) findViewById(R.id.daySun)
            };

            OnCheckedChangeListener ringerChanged = new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mToggleRinger.setEnabled(buttonView.isChecked());
                }
            };
            mCheckRinger.setOnCheckedChangeListener(ringerChanged);
            ringerChanged.onCheckedChanged(mCheckRinger, false);
            
            OnCheckedChangeListener vibChanged = new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mToggleVib.setEnabled(buttonView.isChecked());
                }
            };
            mCheckVib.setOnCheckedChangeListener(vibChanged);
            vibChanged.onCheckedChanged(mCheckVib, false);
            
            // get column indexes
            int hourMinColIndex = c.getColumnIndexOrThrow(Columns.HOUR_MIN);
            int daysColIndex = c.getColumnIndexOrThrow(Columns.DAYS);
            int actionsColIndex = c.getColumnIndexOrThrow(Columns.ACTIONS);

            // fill in UI from cursor data
            setTimePickerValue(mTimePicker, c.getInt(hourMinColIndex));
            
            int days = c.getInt(daysColIndex);
            for (int i = Columns.MONDAY_BIT_INDEX; i <= Columns.SUNDAY_BIT_INDEX; i++) {
                mCheckDays[i].setChecked((days & (1<<i)) != 0);
            }
            
            String actions = c.getString(actionsColIndex);
            if (actions != null) {
                for (String action : actions.split(",")) {
                    int value = -1;
                    if (action.length() > 1) {
                        try {
                            value = Integer.parseInt(action.substring(1));
                        } catch (NumberFormatException e) {
                            Log.e(TAG, String.format("Invalid action '%s' in '%s", action, actions));
                            
                        }
                    }
                    // TODO
                    /*
                    if (action.startsWith(Columns.ACTION_RINGER)) {
                        mCheckRinger.setChecked(value >= 0);
                        mToggleRinger.setChecked(value > 0);
                    }
                    if (action.startsWith(Columns.ACTION_VIBRATE)) {
                        mCheckVib.setChecked(value >= 0);
                        mToggleVib.setChecked(value > 0);
                    }
                    */
                }
            }
            
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
            profilesDb.onCreate(this);
            
            int hourMin = getTimePickerHourMin(mTimePicker);
            
            int days = 0;

            for (int i = Columns.MONDAY_BIT_INDEX; i <= Columns.SUNDAY_BIT_INDEX; i++) {
                if (mCheckDays[i].isChecked()) {
                    days |= 1<<i;
                }
            }

            StringBuilder actions = new StringBuilder();
            if (mCheckRinger.isChecked()) {
                actions.append(Columns.ACTION_RINGER);
                actions.append(mToggleRinger.isChecked() ? "1" : "0");
            }
            if (mCheckVib.isChecked()) {
                if (actions.length() > 0) actions.append(",");
                actions.append(Columns.ACTION_VIBRATE);
                actions.append(mToggleVib.isChecked() ? "1" : "0");
            }
            
            String description = TimedActionUtils.computeDescription(hourMin, days, actions.toString());
            
            int count = profilesDb.updateTimedAction(mActionId,
                    hourMin,
                    days,
                    actions.toString(),
                    description);
            
            Log.d(TAG, "written rows: " + Integer.toString(count));
            
        } finally {
            profilesDb.onDestroy();
        }
    }
    
    // -----------


    private int getTimePickerHourMin(TimePicker timePicker) {
        int hours = timePicker.getCurrentHour();
        int minutes = timePicker.getCurrentMinute();

        return hours*60 + minutes;
    }

    private void setTimePickerValue(TimePicker timePicker, int hourMin) {
        if (hourMin < 0) hourMin = 0;
        if (hourMin >= 24*60) hourMin = 24*60-1;
        int hours = hourMin / 60;
        int minutes = hourMin % 60;
        
        timePicker.setCurrentHour(hours);
        timePicker.setCurrentMinute(minutes);
    }

}
