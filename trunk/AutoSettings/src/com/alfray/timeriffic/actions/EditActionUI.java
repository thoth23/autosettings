/*
 * Project: Timeriffic
 * Copyright (C) 2008 ralfoide gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alfray.timeriffic.actions;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.actions.PrefPercentDialog.Accessor;
import com.alfray.timeriffic.profiles.Columns;
import com.alfray.timeriffic.profiles.ProfilesDB;
import com.alfray.timeriffic.utils.SettingsHelper;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TimePicker;

public class EditActionUI extends Activity {

    private static boolean DEBUG = false;
    private static String TAG = "Timerfc-EditActionUI";

    /** Extra long with the action prof_id (not index) to edit. */
    public static final String EXTRA_ACTION_ID = "action_id";

    /*package*/ static final int DIALOG_EDIT_PERCENT = 42;
    private long mActionId;

    private TimePicker mTimePicker;
    private SettingsHelper mSettingsHelper;

    private PrefEnum mPrefRingerMode;
    private PrefEnum mPrefRingerVibrate;
    private PrefPercent mPrefRingerVolume;
    private PrefPercent mPrefNotifVolume;
    private PrefPercent mPrefBrightness;
    private PrefToggle mPrefAirplane;
    private PrefToggle mPrefWifi;
    private PrefToggle mPrefBluetooh;

    /**
     * Day checkboxes, in the same index order than {@link Columns#MONDAY_BIT_INDEX}
     * to {@link Columns#SUNDAY_BIT_INDEX}.
     */
    private CheckBox[] mCheckDays;

    private View mCurrentContextMenuView;
    private PrefPercent[] mPrefPercentOutWrapper = new PrefPercent[1];
    private int mRestoreHourMinValue = -1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_action);
        setTitle(R.string.editaction_title);

        Intent intent = getIntent();
        mActionId = intent.getExtras().getLong(EXTRA_ACTION_ID);

        if (DEBUG) Log.d(TAG, String.format("edit prof_id: %08x", mActionId));

        if (mActionId == 0) {
            Log.e(TAG, "action id not found in intent.");
            finish();
            return;
        }

        mSettingsHelper = new SettingsHelper(this);

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
            if (DEBUG) Log.d(TAG, String.format("Edit Action=%s", actions_str));

            String[] actions = actions_str != null ? actions_str.split(",") : null;

            // get UI widgets
            mTimePicker = (TimePicker) findViewById(R.id.timePicker);

            mPrefRingerMode = new PrefEnum(this,
                    R.id.ringerModeButton,
                    SettingsHelper.RingerMode.values(),
                    actions,
                    Columns.ACTION_RINGER,
                    getString(R.string.editaction_ringer));
            mPrefRingerMode.setEnabled(mSettingsHelper.canControlAudio(),
                    getString(R.string.setting_not_supported));

            mPrefRingerVibrate = new PrefEnum(this,
                    R.id.ringerVibButton,
                    SettingsHelper.VibrateRingerMode.values(),
                    actions,
                    Columns.ACTION_VIBRATE,
                    getString(R.string.editaction_vibrate));
            mPrefRingerVibrate.setEnabled(mSettingsHelper.canControlAudio(),
                    getString(R.string.setting_not_supported));

            mPrefRingerVolume = new PrefPercent(this,
                    mPrefPercentOutWrapper,
                    R.id.ringerVolButton,
                    actions,
                    Columns.ACTION_RING_VOLUME,
                    getString(R.string.editaction_volume),
                    0,
                    new Accessor() {
                        @Override
                        public void changePercent(int percent) {
                            mSettingsHelper.changeRingerVolume(percent);
                        }

                        @Override
                        public int getPercent() {
                            return mSettingsHelper.getRingerVolume();
                        }
                    });
            mPrefRingerVolume.setEnabled(mSettingsHelper.canControlAudio(),
                    getString(R.string.setting_not_supported));

            mPrefNotifVolume = new PrefPercent(this,
                    mPrefPercentOutWrapper,
                    R.id.notifVolButton,
                    actions,
                    Columns.ACTION_NOTIF_VOLUME,
                    getString(R.string.editaction_notif_volume),
                    0,
                    new Accessor() {
                        @Override
                        public void changePercent(int percent) {
                            mSettingsHelper.changeNotificationVolume(percent);
                        }

                        @Override
                        public int getPercent() {
                            return mSettingsHelper.getNotificationVolume();
                        }
                    });
            mPrefNotifVolume.setEnabled(mSettingsHelper.canControlNotificationVolume(),
                    getString(R.string.setting_not_supported));

            mPrefBrightness = new PrefPercent(this,
                    mPrefPercentOutWrapper,
                    R.id.brightnessButton,
                    actions,
                    Columns.ACTION_BRIGHTNESS,
                    getString(R.string.editaction_brightness),
                    R.drawable.ic_menu_view_brightness,
                    new Accessor() {
                        @Override
                        public void changePercent(int percent) {
                            // disable the immediate slider feedback, it flickers too much and is very slow.
                            // mSettingsHelper.changeBrightness(percent, false /*persist*/);
                        }

                        @Override
                        public int getPercent() {
                            return mSettingsHelper.getCurrentBrightness();
                        }
                    });
            mPrefBrightness.setEnabled(mSettingsHelper.canControlBrigthness(),
                    getString(R.string.setting_not_supported));

            mPrefBluetooh = new PrefToggle(this,
                            R.id.bluetoothButton,
                            actions,
                            Columns.ACTION_BLUETOOTH,
                            getString(R.string.editaction_bluetooth));
            mPrefBluetooh.setEnabled(mSettingsHelper.canControlBluetooth(),
                    getString(R.string.setting_not_supported));

            mPrefWifi = new PrefToggle(this,
                            R.id.wifiButton,
                            actions,
                            Columns.ACTION_WIFI,
                            getString(R.string.editaction_wifi));
            mPrefWifi.setEnabled(mSettingsHelper.canControlWifi(),
                    getString(R.string.setting_not_supported));

            mPrefAirplane = new PrefToggle(this,
                            R.id.airplaneButton,
                            actions,
                            Columns.ACTION_AIRPLANE,
                            getString(R.string.editaction_airplane));
            mPrefAirplane.setEnabled(mSettingsHelper.canControlAirplaneMode(),
                    getString(R.string.setting_not_supported));

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

            // Update the time picker.
            // BUG WORKAROUND: when updating the timePicker here in onCreate, the timePicker
            // might override some values when it redisplays in onRestoreInstanceState so
            // we'll update there instead.
            mRestoreHourMinValue = c.getInt(hourMinColIndex);
            setTimePickerValue(mTimePicker, mRestoreHourMinValue);

            // Update days checked
            int days = c.getInt(daysColIndex);
            for (int i = Columns.MONDAY_BIT_INDEX; i <= Columns.SUNDAY_BIT_INDEX; i++) {
                mCheckDays[i].setChecked((days & (1<<i)) != 0);
                // TODO change label depending on Locale
            }

            mPrefRingerMode.requestFocus();
            ScrollView sv = (ScrollView) findViewById(R.id.scroller);
            sv.scrollTo(0, 0);

        } finally {
            c.close();
            profilesDb.onDestroy();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Bug workaround. See mRestoreHourMinValue in onCreate.
        if (mRestoreHourMinValue >= 0) {
            setTimePickerValue(mTimePicker, mRestoreHourMinValue);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        mCurrentContextMenuView = null;

        Object tag = view.getTag();
        if (tag instanceof PrefBase) {
            ((PrefBase) tag).onCreateContextMenu(menu);
            mCurrentContextMenuView = view;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (mCurrentContextMenuView instanceof View) {
            Object tag = mCurrentContextMenuView.getTag();
            if (tag instanceof PrefBase) {
                ((PrefBase) tag).onContextItemSelected(item);
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
        mCurrentContextMenuView = null;
    }

    @Override
    protected Dialog onCreateDialog(final int id) {

        if (id == DIALOG_EDIT_PERCENT && mPrefPercentOutWrapper[0] != null) {
            PrefPercentDialog d = new PrefPercentDialog(this, mPrefPercentOutWrapper);

            // We need to make sure to remove the dialog once it gets dismissed
            // otherwise the next use of the same dialog might reuse the previous
            // dialog from another setting!
            d.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    removeDialog(id);
                }
            });
            return d;
        }

        return super.onCreateDialog(id);
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

            mPrefRingerMode.collectResult(actions);
            mPrefRingerVibrate.collectResult(actions);
            mPrefRingerVolume.collectResult(actions);
            mPrefNotifVolume.collectResult(actions);
            mPrefBluetooh.collectResult(actions);
            mPrefWifi.collectResult(actions);
            mPrefAirplane.collectResult(actions);
            mPrefBrightness.collectResult(actions);

            if (DEBUG) Log.d(TAG, "new actions: " + actions.toString());

            String description = TimedActionUtils.computeDescription(
                    this, hourMin, days, actions.toString());

            int count = profilesDb.updateTimedAction(mActionId,
                    hourMin,
                    days,
                    actions.toString(),
                    description);

            if (DEBUG) Log.d(TAG, "written rows: " + Integer.toString(count));

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
