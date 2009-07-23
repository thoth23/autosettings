/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License GPLv3
 */

package com.alfray.timeriffic.profiles;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AbsListView.RecyclerListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.app.AutoReceiver;
import com.alfray.timeriffic.app.IntroDialogActivity;
import com.alfray.timeriffic.app.TimerifficApp;
import com.alfray.timeriffic.prefs.PrefsActivity;
import com.alfray.timeriffic.prefs.PrefsValues;

public class ProfilesUI extends Activity {

    private static final String TAG = "Tmrfc-ListProfilesUI";
    private static final boolean DEBUG = true;

    private static final int DATA_CHANGED = 42;
    private static final int SETTINGS_UPDATED = 43;

    private static final int DIALOG_RESET_CHOICES = 0;
    public static final int DIALOG_DELETE_ACTION = 1;
    public static final int DIALOG_DELETE_PROFILE = 2;


    private ListView mProfilesList;
    private ProfileCursorAdapter mAdapter;
    private LayoutInflater mLayoutInflater;
    private ProfilesDB mProfilesDb;

    private int mIdColIndex;
    private int mTypeColIndex;
    private int mDescColIndex;
    private int mEnableColIndex;
    private int mProfIdColIndex;
    private PrefsValues mPrefsValues;
    private Drawable mGreenDot;
    private Drawable mGrayDot;
    private Drawable mCheckOn;
    private Drawable mCheckOff;

    private ToggleButton mGlobalToggle;

    private long mTempDialogRowId;
    private String mTempDialogTitle;

    private Cursor mCursor;


    /**
     * Called when the activity is created.
     * <p/>
     * Initializes row indexes and buttons.
     * Profile list & db is initialized in {@link #onResume()}.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String version = "??";
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            // pass
        }
        Log.d(TAG, String.format("Started %s v%s",
                        getClass().getSimpleName(),
                        version));

        setContentView(R.layout.profiles_screen);
        mLayoutInflater = getLayoutInflater();

        mPrefsValues = new PrefsValues(this);
        mGreenDot = getResources().getDrawable(R.drawable.dot_green);
        mGrayDot = getResources().getDrawable(R.drawable.dot_gray);
        mCheckOn = getResources().getDrawable(R.drawable.btn_check_on);
        mCheckOff = getResources().getDrawable(R.drawable.btn_check_off);

        initButtons();
        showIntro(false);
    }

    private void showIntro(boolean force) {
        boolean hideControls = force;
        if (!force) {
            TimerifficApp tapp = getApp();
            if (tapp != null &&
                    !tapp.isIntroDisplayed() &&
                    !mPrefsValues.isIntroDismissed()) {
                tapp.setIntroDisplayed(true);
                force = true;
            }
        }

        if (force) {
            Intent i = new Intent(this, IntroDialogActivity.class);
            if (hideControls) i.putExtra(IntroDialogActivity.EXTRA_NO_CONTROLS, true);
            startActivity(i);
        }
    }

    private TimerifficApp getApp() {
        Application app = getApplication();
        if (app instanceof TimerifficApp) return (TimerifficApp) app;
        return null;
    }

    /**
     * Initializes the profile list widget with a cursor adapter.
     * Creates a db connection.
     */
    private void initProfileList() {

        Log.d(TAG, "init profile list");

        if (mProfilesList == null) {
            mProfilesList = (ListView) findViewById(R.id.profilesList);
            mProfilesList.setRecyclerListener(new ProfileRecyclerListener());
            mProfilesList.setEmptyView(findViewById(R.id.empty));

            mProfilesList.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View clickedView, int position, long id) {
                    if (DEBUG) Log.d(TAG, String.format("onItemClick: pos %d, id %d", position, id));
                    BaseHolder h = null;
                    h = getHolder(null, clickedView);
                    if (h != null) h.onItemSelected();
                }
            });

            mProfilesList.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View listview, ContextMenuInfo menuInfo) {
                    if (DEBUG) Log.d(TAG, "onCreateContextMenu");
                    BaseHolder h = null;
                    h = getHolder(menuInfo, null);
                    if (h != null) h.onCreateContextMenu(menu);
                }
            });
        }

        if (mProfilesDb == null) {
            mProfilesDb = new ProfilesDB();
            mProfilesDb.onCreate(this);
            if (updateOldPrefs()) {
                if (mCursor != null) {
                    mCursor.close();
                    mCursor = null;
                }
                mAdapter = null;
            }
        }

        if (mAdapter == null) {
            if (mCursor != null) mCursor.close();
            mCursor = mProfilesDb.query(
                    -1, //id
                    new String[] {
                        Columns._ID,
                        Columns.TYPE,
                        Columns.DESCRIPTION,
                        Columns.IS_ENABLED,
                        Columns.PROFILE_ID,
                        // enable these only if they are actually used here
                        //Columns.HOUR_MIN,
                        //Columns.DAYS,
                        //Columns.ACTIONS,
                        //Columns.NEXT_MS
                    } , //projection
                    null, //selection
                    null, //selectionArgs
                    null //sortOrder
                    );

            mIdColIndex = mCursor.getColumnIndexOrThrow(Columns._ID);
            mTypeColIndex = mCursor.getColumnIndexOrThrow(Columns.TYPE);
            mDescColIndex = mCursor.getColumnIndexOrThrow(Columns.DESCRIPTION);
            mEnableColIndex = mCursor.getColumnIndexOrThrow(Columns.IS_ENABLED);
            mProfIdColIndex = mCursor.getColumnIndexOrThrow(Columns.PROFILE_ID);

            mAdapter = new ProfileCursorAdapter(this, mCursor);
            mProfilesList.setAdapter(mAdapter);

            Log.d(TAG, String.format("adapter count: %d", mProfilesList.getCount()));
        }
    }

    /**
     * Update old prefs
     *
     * @return Return true if old prefs was imported, false if nothing changed.
     */
    private boolean updateOldPrefs() {

        // Version:
        // 0=old 1.0
        // 1=1.2-1.3.3 that tried to import old one and was broken, disabled
        // 2=once auto-import disable
        mPrefsValues.setVersion();

        // All this is disabled because it creates something weird
        // and the generated profile header is treaded as a timed action.
        // TODO fix and debug then reactivate or drop it.

//        int v = mPrefsValues.getVersion();
//        switch(v) {
//            case Oldv1PrefsValues.VERSION:
//                Log.d(TAG, String.format("Update old prefs: %s to %s", v, PrefsValues.VERSION));
//
//                try {
//                    Oldv1PrefsValues old = new Oldv1PrefsValues(this);
//                    int startHourMin = old.startHourMin();
//                    int stopHourMin = old.stopHourMin();
//
//                    // need profile headers?
//
//                    long prof_index = -1;
//                    if (startHourMin >= 0 || stopHourMin >= 0) {
//                        prof_index = mProfilesDb.insertProfile(
//                                        -1 /*beforeProfileIndex*/,
//                                        "Old Timeriffic Profile" /*title*/,
//                                        true /*isEnabled*/);
//                    }
//
//                    long action_index = 0;
//                    if (prof_index > 0 && startHourMin >= 0) {
//
//                        StringBuilder actions = new StringBuilder();
//                        actions.append(Columns.ACTION_RINGER).append(old.startMute() ? 'M' : 'R');
//                        actions.append(',');
//                        actions.append(Columns.ACTION_VIBRATE).append(old.startVibrate() ? 'V' : 'N');
//
//                        action_index = mProfilesDb.insertTimedAction(
//                                        prof_index,
//                                        0 /*afterActionIndex*/,
//                                        true /*isActive*/,
//                                        startHourMin /*hourMin*/,
//                                        Columns.MONDAY | Columns.TUESDAY | Columns.WEDNESDAY |
//                                        Columns.THURSDAY | Columns.FRIDAY | Columns.SATURDAY |
//                                        Columns.SUNDAY,
//                                        actions.toString(),
//                                        0 /*nextMs*/);
//                    }
//
//                    if (prof_index > 0 && stopHourMin >= 0) {
//
//                        StringBuilder actions = new StringBuilder();
//                        actions.append(Columns.ACTION_RINGER).append(old.stopMute() ? 'M' : 'R');
//                        actions.append(',');
//                        actions.append(Columns.ACTION_VIBRATE).append(old.stopVibrate() ? 'V' : 'N');
//
//                        mProfilesDb.insertTimedAction(
//                                        prof_index,
//                                        action_index /*afterActionIndex*/,
//                                        true /*isActive*/,
//                                        stopHourMin /*hourMin*/,
//                                        Columns.MONDAY | Columns.TUESDAY | Columns.WEDNESDAY |
//                                        Columns.THURSDAY | Columns.FRIDAY | Columns.SATURDAY |
//                                        Columns.SUNDAY,
//                                        actions.toString(),
//                                        0 /*nextMs*/);
//                    }
//
//                } catch (Exception e) {
//                    Log.e(TAG, "Failed update old prefs", e);
//                } finally {
//                    mPrefsValues.setVersion();
//                }
//
//                return true;
//
//            case PrefsValues.VERSION:
//                // pass
//                break;
//        }

        return false;
    }

    /**
     * Called when activity is resumed, or just after creation.
     * <p/>
     * Initializes the profile list & db.
     */
    @Override
    protected void onResume() {
        super.onResume();
        initProfileList();
        setDataListener();
    }

    /**
     * Called when the activity is getting paused. It might get destroyed
     * at any point.
     * <p/>
     * Reclaim all views (so that they tag's cursor can be cleared).
     * Destroys the db connection.
     */
    @Override
    protected void onPause() {
        super.onPause();
        removeDataListener();
    }

    private void setDataListener() {
        TimerifficApp app = getApp();
        if (app != null) {
            app.setDataListener(new Runnable() {
                @Override
                public void run() {
                    onDataChanged();
                }
            });
        }
    }

    private void removeDataListener() {
        TimerifficApp app = getApp();
        if (app != null) {
            app.setDataListener(null);
        }
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mTempDialogRowId = savedInstanceState.getLong("dlg_rowid");
        mTempDialogTitle = savedInstanceState.getString("dlg_title");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong("dlg_rowid", mTempDialogRowId);
        outState.putString("dlg_title", mTempDialogTitle);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.changeCursor(null);
            mAdapter = null;
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        if (mProfilesDb != null) {
            mProfilesDb.onDestroy();
            mProfilesDb = null;
        }
        if (mProfilesList != null) {
            ArrayList<View> views = new ArrayList<View>();
            mProfilesList.reclaimViews(views);
            mProfilesList.setAdapter(null);
            mProfilesList = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
        case DATA_CHANGED:
            onDataChanged();
            requestSettingsCheck(AutoReceiver.TOAST_IF_CHANGED);
            break;
        case SETTINGS_UPDATED:
            updateGlobalToggleFromPrefs();
            requestSettingsCheck(AutoReceiver.TOAST_IF_CHANGED);
            break;
        }
    }

    private void onDataChanged() {
        if (mCursor != null) mCursor.requery();
        mAdapter = null;
        initProfileList();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
        case DIALOG_RESET_CHOICES:
            return createDialogResetChoices();
        case DIALOG_DELETE_PROFILE:
            return createDeleteProfileDialog();
        case DIALOG_DELETE_ACTION:
            return createDialogDeleteTimedAction();
        default:
            return null;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenuInfo info = item.getMenuInfo();
        BaseHolder h = getHolder(info, null);
        if (h != null) {
            h.onContextMenuSelected(item);
            return true;
        }

        return super.onContextItemSelected(item);
    }

    private BaseHolder getHolder(ContextMenuInfo menuInfo, View selectedView) {
        if (selectedView == null && menuInfo instanceof AdapterContextMenuInfo) {
            selectedView = ((AdapterContextMenuInfo) menuInfo).targetView;
        }

        Object tag = selectedView.getTag();
        if (tag instanceof BaseHolder) {
            return (BaseHolder) tag;
        }

        Log.d(TAG, "Holder missing");
        return null;
    }

    /**
     * Initializes the list-independent buttons: global toggle, check now.
     */
    private void initButtons() {
        mGlobalToggle = (ToggleButton) findViewById(R.id.global_toggle);

        mGlobalToggle.setChecked(mPrefsValues.isServiceEnabled());

        mGlobalToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPrefsValues.setServiceEnabled(mGlobalToggle.isChecked());
                requestSettingsCheck(AutoReceiver.TOAST_ALWAYS);
            }
        });

        Button b = (Button) findViewById(R.id.check_now);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSettingsCheck(AutoReceiver.TOAST_ALWAYS);
            }
        });

        b.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Display a toast with the last status msg
                Toast t = Toast.makeText(ProfilesUI.this,
                        mPrefsValues.getStatusMsg(),
                        Toast.LENGTH_LONG);
                t.show();

                return true; // we consumed the long view
            }
        });
    }

    private void updateGlobalToggleFromPrefs() {
        mGlobalToggle.setChecked(mPrefsValues.isServiceEnabled());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, R.string.append_profile,  0, R.string.append_profile).setIcon(R.drawable.ic_menu_add);
        menu.add(0, R.string.about,  0, R.string.about).setIcon(R.drawable.ic_menu_help);
        menu.add(0, R.string.settings,  0, R.string.settings).setIcon(R.drawable.ic_menu_preferences);
        menu.add(0, R.string.check_now,  0, R.string.check_now).setIcon(R.drawable.ic_menu_rotate);
        menu.add(0, R.string.reset,  0, R.string.reset).setIcon(R.drawable.ic_menu_revert);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.string.settings:
            showPrefs();
            break;
        case R.string.check_now:
            requestSettingsCheck(AutoReceiver.TOAST_ALWAYS);
            break;
        case R.string.about:
            showIntro(true /*force*/);
            break;
        case R.string.reset:
            showResetChoices();
            break;
        case R.string.append_profile:
            appendNewProfile();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPrefs() {
        startActivityForResult(new Intent(this, PrefsActivity.class), SETTINGS_UPDATED);
    }

    private void requestSettingsCheck(int displayToast) {
        if (DEBUG) Log.d(TAG, "Request settings check");
        Intent i = new Intent(AutoReceiver.ACTION_AUTO_CHECK_STATE);
        i.putExtra(AutoReceiver.EXTRA_TOAST_NEXT_EVENT, displayToast);
        sendBroadcast(i);
    }

    protected void showResetChoices() {
        showDialog(DIALOG_RESET_CHOICES);
    }

    private Dialog createDialogResetChoices() {
        Builder d = new AlertDialog.Builder(this);

        d.setCancelable(true);
        d.setTitle(R.string.resetprofiles_msg_confirm_delete);
        d.setIcon(R.drawable.timeriffic_icon);
        //d.setMessage("Are you sure you want to delete all profiles?");
        d.setItems(mProfilesDb.getResetLabels(),
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mProfilesDb.resetProfiles(which);
                    removeDialog(DIALOG_RESET_CHOICES);
                    onDataChanged();
                    requestSettingsCheck(AutoReceiver.TOAST_IF_CHANGED);
                }
        });

        d.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                removeDialog(DIALOG_RESET_CHOICES);
            }
        });

        d.setNegativeButton(R.string.resetprofiles_button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeDialog(DIALOG_RESET_CHOICES);
            }
        });

        return d.create();
    }


    //--------------

    /**
     * A custom {@link CursorAdapter} that can provide the two views we
     * need: the profile header and the timed action entry.
     * <p/>
     * For each new view, the tag is set to either {@link ProfileHeaderHolder}
     * or {@link TimedActionHolder}, a subclass of {@link BaseHolder}.
     * <p/>
     * When a view is reused, it's tag is reused with a new cursor by using
     * {@link BaseHolder#setUiData(Cursor)}. This also updates the view
     * with the data from the cursor.
     * <p/>
     * When a view is recycled/reclaimed, it's tag is cleared by the
     * {@link ProfileRecyclerListener}.
     */
    private class ProfileCursorAdapter extends CursorAdapter {

        /** View type is a profile header. */
        private final static int TYPE_PROFILE = 0;
        /** View type is a timed action item. */
        private final static int TYPE_TIMED_ACTION = 1;


        /**
         * Creates a new {@link ProfileCursorAdapter} for that cursor
         * and context.
         */
        public ProfileCursorAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        /**
         * All items are always enabled in this view.
         */
        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        /**
         * All items are always enabled in this view.
         */
        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        /**
         * This adapter can serve 2 view types.
         */
        @Override
        public int getViewTypeCount() {
            return 2;
        }

        /**
         * View types served are either {@link #TYPE_PROFILE} or
         * {@link #TYPE_TIMED_ACTION}. This is based on the value of
         * {@link Columns#TYPE} in the cursor.
         */
        @Override
        public int getItemViewType(int position) {
            Cursor c = (Cursor) getItem(position);
            int type = c.getInt(mTypeColIndex);
            if (type == Columns.TYPE_IS_PROFILE)
                return TYPE_PROFILE;
            if (type == Columns.TYPE_IS_TIMED_ACTION)
                return TYPE_TIMED_ACTION;

            return IGNORE_ITEM_VIEW_TYPE;
        }

        // ---

        /**
         * Depending on the value of {@link Columns#TYPE} in the cursor,
         * this inflates either a profile_header or a timed_action resource.
         * <p/>
         * It then associates the tag with a new {@link ProfileHeaderHolder}
         * or {@link TimedActionHolder} and initializes the holder using
         * {@link BaseHolder#setUiData(Cursor)}.
         *
         */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            View v = null;
            BaseHolder h = null;

            int type = cursor.getInt(mTypeColIndex);
            if (type == Columns.TYPE_IS_PROFILE) {
                v = mLayoutInflater.inflate(R.layout.profile_header, null);
                h = new ProfileHeaderHolder(v);
            } else if (type == Columns.TYPE_IS_TIMED_ACTION) {
                v = mLayoutInflater.inflate(R.layout.timed_action, null);
                h = new TimedActionHolder(v);
            }
            if (v != null) {
                v.setTag(h);
                h.setUiData(cursor);
            }
            return v;
        }

        /**
         * To recycle a view, we just re-associate its tag using
         * {@link BaseHolder#setUiData(Cursor)}.
         */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            int type = cursor.getInt(mTypeColIndex);
            if (type == Columns.TYPE_IS_PROFILE ||
                    type == Columns.TYPE_IS_TIMED_ACTION) {
                BaseHolder h = (BaseHolder) view.getTag();
                h.setUiData(cursor);
            }
        }
    }

    //--------------

    /**
     * This {@link RecyclerListener} is attached to the profile list to
     * call {@link BaseHolder#clearCursor()} of the tags of the reclaimed
     * views. This should ensure that not dangling cursor reference exists.
     */
    private class ProfileRecyclerListener implements RecyclerListener {
        @Override
        public void onMovedToScrapHeap(View view) {
            Object tag = view.getTag();
            if (tag instanceof BaseHolder) {
                ((BaseHolder) tag).clearCursor();
            }
        }
    }

    //--------------

    /**
     * A base holder class that keeps tracks of the current cursor
     * and the common widgets of the two derived holders.
     */
    private abstract class BaseHolder {

        /**
         * The current cursor associated with that holder.
         * It is null if the view is not associated with a cursor anymore.
         */
        private Cursor mCursor;

        /**
         * The text view that holds the title or description as well
         * as the "check box".
         */
        private final TextView mDescription;

        public BaseHolder(View view) {
            mDescription = view != null ? (TextView) view.findViewById(R.id.description) : null;
        }

        public Cursor getCursor() {
            return mCursor;
        }

        public void clearCursor() {
            mCursor = null;
        }

        protected void setUiData(Cursor cursor,
                String description,
                Drawable state) {
            mCursor = cursor;
            if (description != null) mDescription.setText(description);
            if (state != null) mDescription.setCompoundDrawablesWithIntrinsicBounds(
                    state /*left*/, null /*top*/, null /*right*/, null /*bottom*/);
        }

        public abstract void setUiData(Cursor cursor);
        public abstract void onItemSelected();
        public abstract void onCreateContextMenu(ContextMenu menu);
        public abstract void onContextMenuSelected(MenuItem item);


        // --- profile actions ---

        private void startEditActivity(Class<?> activity, String extra_id, long extra_value) {
            if (getCursor() != null) getCursor().requery();

            Intent intent = new Intent(ProfilesUI.this, activity);
            intent.putExtra(extra_id, extra_value);

            startActivityForResult(intent, DATA_CHANGED);
        }

        protected void deleteProfile(Cursor cursor) {
            final long row_id = cursor.getLong(mIdColIndex);
            String title = cursor.getString(mDescColIndex);

            mTempDialogRowId = row_id;
            mTempDialogTitle = title;
            showDialog(DIALOG_DELETE_PROFILE);
        }

        protected void insertNewProfile(Cursor beforeCursor) {
            long prof_index = 0;
            if (beforeCursor != null) {
                prof_index = beforeCursor.getLong(mProfIdColIndex) >> Columns.PROFILE_SHIFT;
            }

            prof_index = mProfilesDb.insertProfile(prof_index,
                            getString(R.string.insertprofile_new_profile_title),
                            true /*isEnabled*/);

            startEditActivity(EditProfileUI.class,
                    EditProfileUI.EXTRA_PROFILE_ID, prof_index << Columns.PROFILE_SHIFT);
        }

        protected void editProfile(Cursor cursor) {
            long prof_id = cursor.getLong(mProfIdColIndex);

            startEditActivity(EditProfileUI.class, EditProfileUI.EXTRA_PROFILE_ID, prof_id);
        }

        // --- timed actions ----


        protected void deleteTimedAction(Cursor cursor) {

            final long row_id = cursor.getLong(mIdColIndex);
            String description = cursor.getString(mDescColIndex);

            mTempDialogRowId = row_id;
            mTempDialogTitle = description;
            showDialog(DIALOG_DELETE_ACTION);
        }

        protected void insertNewAction(Cursor beforeCursor) {
            long prof_index = 0;
            long action_index = 0;
            if (beforeCursor != null) {
                prof_index = beforeCursor.getLong(mProfIdColIndex);
                action_index = prof_index & Columns.ACTION_MASK;
                prof_index = prof_index >> Columns.PROFILE_SHIFT;
            }

            Calendar c = new GregorianCalendar();
            c.setTimeInMillis(System.currentTimeMillis());
            int hourMin = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);

            int day = TimedActionUtils.calendarDayToActionDay(c);

            action_index = mProfilesDb.insertTimedAction(
                    prof_index,
                    action_index,
                    false,      // isActive
                    hourMin,    // hourMin
                    day,        // days
                    "",         // actions
                    0           // nextMs
                    );

            long action_id = (prof_index << Columns.PROFILE_SHIFT) + action_index;

            startEditActivity(EditActionUI.class, EditActionUI.EXTRA_ACTION_ID, action_id);
        }

        protected void editAction(Cursor cursor) {
            long action_id = cursor.getLong(mProfIdColIndex);

            startEditActivity(EditActionUI.class, EditActionUI.EXTRA_ACTION_ID, action_id);
        }

    }

    private Dialog createDeleteProfileDialog() {
        final long row_id = mTempDialogRowId;
        final String title = mTempDialogTitle;

        Builder d = new AlertDialog.Builder(ProfilesUI.this);

        d.setCancelable(true);
        d.setTitle(R.string.deleteprofile_title);
        d.setIcon(R.drawable.timeriffic_icon);
        d.setMessage(String.format(
                getString(R.string.deleteprofile_msgbody), title));

        d.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                removeDialog(DIALOG_DELETE_PROFILE);
            }
        });

        d.setNegativeButton(R.string.deleteprofile_button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeDialog(DIALOG_DELETE_PROFILE);
            }
        });

        d.setPositiveButton(R.string.deleteprofile_button_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int count = mProfilesDb.deleteProfile(row_id);
                if (count > 0) {
                    mAdapter.notifyDataSetChanged();
                    onDataChanged();
                }
                removeDialog(DIALOG_DELETE_PROFILE);
            }
        });

        return d.create();
    }

    private Dialog createDialogDeleteTimedAction() {

        final long row_id = mTempDialogRowId;
        final String description = mTempDialogTitle;

        Builder d = new AlertDialog.Builder(ProfilesUI.this);

        d.setCancelable(true);
        d.setTitle(R.string.deleteaction_title);
        d.setIcon(R.drawable.timeriffic_icon);
        d.setMessage(getString(R.string.deleteaction_msgbody, description));

        d.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                removeDialog(DIALOG_DELETE_ACTION);
            }
        });

        d.setNegativeButton(R.string.deleteaction_button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeDialog(DIALOG_DELETE_ACTION);
            }
        });

        d.setPositiveButton(R.string.deleteaction_button_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int count = mProfilesDb.deleteAction(row_id);
                if (count > 0) {
                    mAdapter.notifyDataSetChanged();
                    onDataChanged();
                }
                removeDialog(DIALOG_DELETE_ACTION);
            }
        });

        return d.create();
    }


    public void appendNewProfile() {
        long prof_index = mProfilesDb.insertProfile(0,
                        getString(R.string.insertprofile_new_profile_title),
                        true /*isEnabled*/);

        Intent intent = new Intent(ProfilesUI.this, EditProfileUI.class);
        intent.putExtra(EditProfileUI.EXTRA_PROFILE_ID, prof_index << Columns.PROFILE_SHIFT);

        startActivityForResult(intent, DATA_CHANGED);
    }

    //--------------

    /**
     * The holder for a profile header row.
     */
    private class ProfileHeaderHolder extends BaseHolder {

        public ProfileHeaderHolder(View view) {
            super(view);
        }

        @Override
        public void setUiData(Cursor cursor) {
            super.setUiData(cursor,
                    cursor.getString(mDescColIndex),
                    cursor.getInt(mEnableColIndex) != 0 ? mCheckOn : mCheckOff);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu) {
            menu.setHeaderTitle(R.string.profilecontextmenu_title);

            menu.add(0, R.string.insert_profile, 0, R.string.insert_profile);
            menu.add(0, R.string.insert_action, 0, R.string.insert_action);
            menu.add(0, R.string.delete, 0, R.string.delete);
            menu.add(0, R.string.rename, 0, R.string.rename);
        }

        @Override
        public void onItemSelected() {
            Cursor cursor = getCursor();
            if (cursor == null) return;

            boolean enabled = cursor.getInt(mEnableColIndex) != 0;
            enabled = !enabled;

            mProfilesDb.updateProfile(
                    cursor.getLong(mProfIdColIndex),
                    null, // name
                    enabled);

            // update ui
            cursor.requery();
            setUiData(cursor, null, enabled ? mCheckOn : mCheckOff);
        }

        @Override
        public void onContextMenuSelected(MenuItem item) {
            switch (item.getItemId()) {
            case R.string.insert_profile:
                if (DEBUG) Log.d(TAG, "profile - insert_profile");
                insertNewProfile(getCursor());
                break;
            case R.string.insert_action:
                if (DEBUG) Log.d(TAG, "profile - insert_action");
                insertNewAction(getCursor());
                break;
            case R.string.delete:
                if (DEBUG) Log.d(TAG, "profile - delete");
                deleteProfile(getCursor());
                break;
            case R.string.rename:
                if (DEBUG) Log.d(TAG, "profile - rename");
                editProfile(getCursor());
                break;
            default:
                break;
            }
        }

    }

    //--------------

    /**
     * The holder for a timed action row.
     */
    private class TimedActionHolder extends BaseHolder {

        public TimedActionHolder(View view) {
            super(view);
        }

        @Override
        public void setUiData(Cursor cursor) {
            super.setUiData(cursor,
                    cursor.getString(mDescColIndex),
                    cursor.getInt(mEnableColIndex) != 0 ? mGreenDot : mGrayDot);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu) {
            menu.setHeaderTitle(R.string.timedactioncontextmenu_title);

            menu.add(0, R.string.insert_action, 0, R.string.insert_action);
            menu.add(0, R.string.delete, 0, R.string.delete);
            menu.add(0, R.string.edit, 0, R.string.edit);
        }

        @Override
        public void onItemSelected() {
            // trigger edit
            if (DEBUG) Log.d(TAG, "action - edit");
            editAction(getCursor());
        }

        @Override
        public void onContextMenuSelected(MenuItem item) {
            switch (item.getItemId()) {
            case R.string.insert_action:
                if (DEBUG) Log.d(TAG, "action - insert_action");
                insertNewAction(getCursor());
                break;
            case R.string.delete:
                if (DEBUG) Log.d(TAG, "action - delete");
                deleteTimedAction(getCursor());
                break;
            case R.string.edit:
                if (DEBUG) Log.d(TAG, "action - edit");
                editAction(getCursor());
                break;
            default:
                break;
            }
        }
    }

    //--------------
}
