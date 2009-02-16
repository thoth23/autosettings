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
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TimePicker;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.utils.SettingsHelper;

public class EditActionUI extends Activity {

    private static String TAG = "EditActionUI";
    
    /** Extra long with the action prof_id (not index) to edit. */
    public static final String EXTRA_ACTION_ID = "action_id";
    private long mActionId;

    private TimePicker mTimePicker;

    private Button mButtonRingerMode;
    private Button mButtonRingerVibrate;
    private Button mButtonRingerVolume;
    private Button mButtonWifi;
    private Button mButtonBrightness;

    /**
     * Day checkboxes, in the same index order than {@link Columns#MONDAY_BIT_INDEX}
     * to {@link Columns#SUNDAY_BIT_INDEX}.
     */
    private CheckBox[] mCheckDays;
    private CheckBox mCheckRinger;
    private CheckBox mCheckVib;

    private View mCurrentContextMenuView;

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
            
            // get column indexes
            int hourMinColIndex = c.getColumnIndexOrThrow(Columns.HOUR_MIN);
            int daysColIndex = c.getColumnIndexOrThrow(Columns.DAYS);
            int actionsColIndex = c.getColumnIndexOrThrow(Columns.ACTIONS);

            String actions_str = c.getString(actionsColIndex);
            String[] actions = actions_str != null ? actions_str.split(",") : null;

            // get UI widgets
            mTimePicker = (TimePicker) findViewById(R.id.timePicker);
            
            mButtonRingerMode = setupButtonEnum(R.id.ringerModeButton, 
                            SettingsHelper.RingerMode.class,
                            actions,
                            Columns.ACTION_RINGER);
            mButtonRingerVibrate = setupButtonEnum(R.id.ringerVibButton, 
                            SettingsHelper.VibrateRingerMode.class,
                            actions,
                            Columns.ACTION_VIBRATE);
            mButtonRingerVolume = setupButtonPercent(R.id.ringerVolButton,
                            actions,
                            Columns.ACTION_RING_VOLUME);
            mButtonBrightness = setupButtonPercent(R.id.brightnessButton,
                            actions,
                            Columns.ACTION_BRIGHTNESS);
            mButtonWifi = setupButtonEnabled(R.id.wifiButton,
                            actions,
                            Columns.ACTION_WIFI);
            
            mCheckDays = new CheckBox[] {
                    (CheckBox) findViewById(R.id.dayMon),
                    (CheckBox) findViewById(R.id.dayTue),
                    (CheckBox) findViewById(R.id.dayWed),
                    (CheckBox) findViewById(R.id.dayThu),
                    (CheckBox) findViewById(R.id.dayFri),
                    (CheckBox) findViewById(R.id.daySat),
                    (CheckBox) findViewById(R.id.daySun)
            };

            // fill in UI from cursor data
            setTimePickerValue(mTimePicker, c.getInt(hourMinColIndex));
            
            int days = c.getInt(daysColIndex);
            for (int i = Columns.MONDAY_BIT_INDEX; i <= Columns.SUNDAY_BIT_INDEX; i++) {
                mCheckDays[i].setChecked((days & (1<<i)) != 0);
            }
            
        } finally {
            c.close();
            profilesDb.onDestroy();
        }
    }
    
    private Button setupButtonEnum(int res_id, Class<? extends Enum<?>> classEnum,
                    String[] actions, char prefix) {
        Button b = (Button) findViewById(res_id);
        
        return b;
    }

    private Button setupButtonPercent(int res_id, String[] actions, char prefix) {
        Button b = (Button) findViewById(res_id);
        return b;
    }

    private Button setupButtonEnabled(int res_id, String[] actions, char prefix) {
        Button b = setupButton(res_id,
                    new String[] {
                        "-,Unchanged",
                        "1,Enabled",
                        "0,Disabled"
                    });

        return b;
    }

    private Button setupButton(int res_id, String[] choices) {
        Button b = (Button) findViewById(res_id);
        b.setTag(choices);

        registerForContextMenu(b);
        b.setOnClickListener(new ShowMenuClickListener());
        return b;
    }
    
    private class ShowMenuClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (view.getTag() instanceof String[]) {
                openContextMenu(view);
            }
        }
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        
        mCurrentContextMenuView = null;
        
        Object tag = view.getTag();
        if (tag instanceof String[]) {
            String[] choices = (String[]) tag;
            for (String choice : choices) {
                String c[] = choice.split(",");
                if (c.length >= 2) menu.add(c[1]);
            }
            mCurrentContextMenuView = view;
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (mCurrentContextMenuView instanceof Button) {
            ((Button) mCurrentContextMenuView).setText(item.getTitle());
        }
        return super.onContextItemSelected(item);
    }
    
    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
        mCurrentContextMenuView = null;
    }

    @Override
    protected void onPause() {
        super.onPause();

        return ; // =======================================
        /*
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
        */
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
