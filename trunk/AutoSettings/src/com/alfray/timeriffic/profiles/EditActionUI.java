/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License TBD
 */

package com.alfray.timeriffic.profiles;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.SeekBar;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.utils.SettingsHelper;

public class EditActionUI extends Activity {

    private static String TAG = "EditActionUI";
    
    /** Extra long with the action prof_id (not index) to edit. */
    public static final String EXTRA_ACTION_ID = "action_id";

    private static final int DIALOG_EDIT_PERCENT = 42;
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

    private View mCurrentContextMenuView;

    private View mCurrentPercentButton;

    private PrefEnum mPrefRingerMode;

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
            
            mPrefRingerMode = new PrefEnum(R.id.ringerModeButton, 
                            SettingsHelper.RingerMode.values(),
                            actions,
                            Columns.ACTION_RINGER);
            
            mButtonRingerVibrate = setupButtonEnum(R.id.ringerVibButton, 
                            SettingsHelper.VibrateRingerMode.values(),
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
    
    private Button setupButtonPercent(int res_id, String[] actions, char prefix) {
        Button b = (Button) findViewById(res_id);

        String currentValue = getActionValue(actions, prefix);
        Object tag = null;

        try {
            int percent = Integer.parseInt(currentValue);
            b.setText(String.format("%d%%", percent));
            tag = percent;
        } catch (Exception e) {
            b.setText("Unchanged");
        }

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentPercentButton = v;
                showDialog(DIALOG_EDIT_PERCENT);
            }
        });
        b.setTag(tag);

        return b;
    }

    private void collectPercent(Button button, StringBuilder actions, char prefix) {
        Object tag = button.getTag();

        if (tag instanceof Integer) {
            if (actions.length() > 0) actions.append(",");
            actions.append(prefix);
            actions.append(((Integer) tag).toString());
        }
    }
    
    private Button setupButtonEnabled(int res_id, String[] actions, char prefix) {

        Button b = setupButton(res_id,
                    new String[] {
                        "-,Unchanged",
                        "1,Enabled",
                        "0,Disabled"
                    });

        String currentValue = getActionValue(actions, prefix);
        if ("1".equals(currentValue)) {
            b.setText("Enabled");
        } else if ("0".equals(currentValue)) {
            b.setText("Disabled");
        } else {
            b.setText("Unchanged");
        }
        
        return b;
    }

    private void collectEnabled(Button button, StringBuilder actions, char prefix) {
        String t = button.getText().toString();
        
        String[] choices = (String[]) button.getTag();
        for (String choice : choices) {
            String[] vals = choice.split(",");
            if (vals[1].equals(t)) {
                if (!vals[0].equals("-")) {
                    if (actions.length() > 0) actions.append(",");
                    actions.append(prefix);
                    actions.append(vals[0]);
                }
                break;
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
    protected Dialog onCreateDialog(int id) {
        
        if (id == DIALOG_EDIT_PERCENT && mCurrentPercentButton == mButtonBrightness) {
            return new BrightnessDialog(mCurrentPercentButton);
        }
        
        return super.onCreateDialog(id);
    }

    private class BrightnessDialog extends AlertDialog
        implements DialogInterface.OnDismissListener,
                   DialogInterface.OnClickListener,
                   SeekBar.OnSeekBarChangeListener,
                   View.OnClickListener {

        private SettingsHelper mHelper;
        /** Initial brightness of the string, so that we can restore it */
        private final int mInitialBrightness;
        /** The button being changed */
        private final View mPercentButton;
        private SeekBar mSeekBar;
        private ToggleButton mToggleButton;

        protected BrightnessDialog(View percentButton) {
            super(EditActionUI.this);
            mPercentButton = percentButton;
            
            setIcon(R.drawable.brightness);
            setTitle("Brightness");
            
            View content = getLayoutInflater().inflate(R.layout.brigthness_alert, null/*root*/);
            setView(content);

            mHelper = new SettingsHelper(getContext());
            mInitialBrightness = mHelper.getCurrentBrightness();

            mSeekBar = (SeekBar) content.findViewById(R.id.seekbar);
            mSeekBar.setOnSeekBarChangeListener(this);
            mSeekBar.setMax(100);

            mToggleButton = (ToggleButton) content.findViewById(R.id.toggle);
            mToggleButton.setOnClickListener(this);

            setOnDismissListener(this);
            
            setButton("Accept", this);
            
            // set initial value
            if (mPercentButton.getTag() instanceof String) {
                String tag = (String)mPercentButton.getTag();
                try {
                    int percent = Integer.parseInt(tag);
                    mHelper.changeBrightness(percent);
                    mToggleButton.setChecked(true);
                    mSeekBar.setProgress(percent);
                    mSeekBar.setEnabled(true);
                    mToggleButton.setTextOn(String.format("Set to %d%%", percent));
                } catch (Exception e) {
                    mToggleButton.setChecked(false);
                    mSeekBar.setProgress(mInitialBrightness);
                    mSeekBar.setEnabled(false);
                }
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            mHelper.changeBrightness(mInitialBrightness);
            mCurrentPercentButton = null;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
            mHelper.changeBrightness(progress);
            mToggleButton.setTextOn(String.format("Set to %d%%", progress));
            // force the toggle button to update its text
            mToggleButton.setChecked(mToggleButton.isChecked());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // pass
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // pass
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            // Update button with percentage selected
            if (mPercentButton instanceof Button) {
                if (mToggleButton.isChecked()) {
                    int percent = mSeekBar.getProgress();
                    mPercentButton.setTag(percent);
                    ((Button) mPercentButton).setText(String.format("%d%%", percent));
                } else {
                    mPercentButton.setTag(null);
                    ((Button) mPercentButton).setText("Unchanged");
                }
            }
            dismiss();
        }

        @Override
        public void onClick(View toggle) {
            mSeekBar.setEnabled(((ToggleButton)toggle).isChecked());
        }
    }

    // -----------


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

            collectEnum(mButtonRingerMode, actions, Columns.ACTION_RINGER);
            collectEnum(mButtonRingerVibrate, actions, Columns.ACTION_VIBRATE);
            collectPercent(mButtonRingerVolume, actions, Columns.ACTION_RING_VOLUME);
            collectPercent(mButtonBrightness, actions, Columns.ACTION_BRIGHTNESS);
            collectEnabled(mButtonWifi, actions, Columns.ACTION_WIFI);

            Log.d(TAG, "new actions: " + actions.toString());

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
