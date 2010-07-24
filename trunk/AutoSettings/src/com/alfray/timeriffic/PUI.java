/*
 * Project: Timeriffic
 * Copyright (C) 2009 ralfoide gmail com,
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

package com.alfray.timeriffic;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
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
import android.view.ViewTreeObserver;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.alfray.timeriffic.R;

public class PUI extends EHA {

    private static final boolean DEBUG = true;
    public static final String TAG = "TFC-PUI";

    static final int DATA_CHANGED = 42;
    static final int SETTINGS_UPDATED = 43;
    static final int CHECK_SERVICES   = 44;

    static final int DIALOG_RESET_CHOICES = 0;
    static final int DIALOG_DELETE_ACTION  = 1;
    static final int DIALOG_DELETE_PROFILE = 2;
    static final int DIALOG_CHECK_SERVICES = 3;

    private ListView mProfilesList;
    private PCA mAdapter;
    private LayoutInflater mLayoutInflater;
    private PDB mProfilesDb;

    private AW mAW;
    private PV mPV;
    private Drawable mGrayDot;
    private Drawable mGreenDot;
    private Drawable mPurpleDot;
    private Drawable mCheckOn;
    private Drawable mCheckOff;

    private GT mGT;
    private GS mGS;

    private long mTempDialogRowId;
    private String mTempDialogTitle;

    private Cursor mCursor;

    public static class ColIndexes {
        int mIdColIndex;
        int mTypeColIndex;
        int mDescColIndex;
        int mEnableColIndex;
        int mProfIdColIndex;
    };

    private ColIndexes mColIndexes = new ColIndexes();

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

        mPV = new PV(this);
        mGrayDot = getResources().getDrawable(R.drawable.dot_gray);
        mGreenDot = getResources().getDrawable(R.drawable.dot_green);
        mPurpleDot = getResources().getDrawable(R.drawable.dot_purple);
        mCheckOn = getResources().getDrawable(R.drawable.btn_check_on);
        mCheckOff = getResources().getDrawable(R.drawable.btn_check_off);

        initButtons();
        showIntroAtStartup();

        mAW = new AW();
        mAW.start(this);
        mAW.event(AW.Event.OpenProfileUI);
    }

    private void showIntroAtStartup() {
        final TA tapp = getApp();
        if (tapp.isFirstStart() && mGT != null) {
            final Runnable action = new Runnable() {
                @Override
                public void run() {
                    showIntro(false, true);
                    tapp.setFirstStart(false);
                }
            };

            final ViewTreeObserver obs = mGT.getViewTreeObserver();
            obs.addOnPreDrawListener(new OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mGT.postDelayed(action, 200 /*delayMillis*/);
                    ViewTreeObserver obs2 = mGT.getViewTreeObserver();
                    obs2.removeOnPreDrawListener(this);
                    return true;
                }
            });
        }
    }

    private void showIntro(boolean force, boolean checkServices) {

        // force is set when this comes from Menu > About
        boolean showIntro = force;

        // if not forcing, does the user wants to see the intro?
        // true by default, unless disabled in the prefs
        if (!showIntro) {
            showIntro = !mPV.isIntroDismissed();
        }

        // user doesn't want to see it... but we force it anyway if this is
        // a version upgrade
        int currentVersion = -1;
        try {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            // the version number is in format n.m.kk where n.m is the
            // actual version number, incremented for features and kk is
            // a sub-minor index of minor fixes. We clear these last digits
            // out and don't force to see the intro for these minor fixes.
            currentVersion = (currentVersion / 100) * 100;
        } catch (NameNotFoundException e) {
            // ignore. should not happen.
        }
        if (!showIntro && currentVersion > 0) {
            showIntro = currentVersion > mPV.getLastIntroVersion();
        }

        if (showIntro) {
            // mark it as seen
            if (currentVersion > 0) {
                mPV.setLastIntroVersion(currentVersion);
            }

            Intent i = new Intent(this, IA.class);
            if (force) i.putExtra(IA.EXTRA_NO_CONTROLS, true);
            startActivityForResult(i, CHECK_SERVICES);
            return;
        }

        if (checkServices) {
            onCheckServices();
        }
    }

    private TA getApp() {
        Application app = getApplication();
        if (app instanceof TA) return (TA) app;
        return null;
    }

    public Cursor getCursor() {
        return mCursor;
    };

    ColIndexes getColIndexes() {
        return mColIndexes;
    }

    PDB getProfilesDb() {
        return mProfilesDb;
    }

    Drawable getGrayDot() {
        return mGrayDot;
    }

    Drawable getGreenDot() {
        return mGreenDot;
    }

    Drawable getPurpleDot() {
        return mPurpleDot;
    }

    Drawable getCheckOff() {
        return mCheckOff;
    }

    Drawable getCheckOn() {
        return mCheckOn;
    }

    /**
     * Initializes the profile list widget with a cursor adapter.
     * Creates a db connection.
     */
    private void initProfileList() {

        Log.d(TAG, "init profile list");

        if (mProfilesList == null) {
            mProfilesList = (ListView) findViewById(R.id.profilesList);
            mProfilesList.setRecyclerListener(new PRL());
            mProfilesList.setEmptyView(findViewById(R.id.empty));

            mProfilesList.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View clickedView, int position, long id) {
                    if (DEBUG) Log.d(TAG, String.format("onItemClick: pos %d, id %d", position, id));
                    BH h = null;
                    h = getHolder(null, clickedView);
                    if (h != null) h.onItemSelected();
                }
            });

            mProfilesList.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View listview, ContextMenuInfo menuInfo) {
                    if (DEBUG) Log.d(TAG, "onCreateContextMenu");
                    BH h = null;
                    h = getHolder(menuInfo, null);
                    if (h != null) h.onCreateContextMenu(menu);
                }
            });
        }

        if (mProfilesDb == null) {
            mProfilesDb = new PDB();
            mProfilesDb.onCreate(this);

            String next = mPV.getStatusNextTS();
            if (next == null) {
                // schedule a profile check to initialize the last/next status
                requestSettingsCheck(UR.TOAST_NONE);
            }
        }

        if (mAdapter == null) {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
            mCursor = mProfilesDb.query(
                    -1, //id
                    new String[] {
                        C._ID,
                        C.TYPE,
                        C.DESCRIPTION,
                        C.IS_ENABLED,
                        C.PROFILE_ID,
                        // enable these only if they are actually used here
                        //C.HOUR_MIN,
                        //C.DAYS,
                        //C.ACTIONS,
                        //C.NEXT_MS
                    } , //projection
                    null, //selection
                    null, //selectionArgs
                    null //sortOrder
                    );

            mColIndexes.mIdColIndex = mCursor.getColumnIndexOrThrow(C._ID);
            mColIndexes.mTypeColIndex = mCursor.getColumnIndexOrThrow(C.TYPE);
            mColIndexes.mDescColIndex = mCursor.getColumnIndexOrThrow(C.DESCRIPTION);
            mColIndexes.mEnableColIndex = mCursor.getColumnIndexOrThrow(C.IS_ENABLED);
            mColIndexes.mProfIdColIndex = mCursor.getColumnIndexOrThrow(C.PROFILE_ID);

            mAdapter = new PCA(this, mColIndexes, mLayoutInflater);
            mProfilesList.setAdapter(mAdapter);

            Log.d(TAG, String.format("adapter count: %d", mProfilesList.getCount()));
        }
    }

    /**
     * Called when activity is resumed, or just after creation.
     * <p/>
     * Initializes the profile list & db.
     */
    @Override
    protected void onResume() {
        super.onResume();
        initOnResume();
    }

    private void initOnResume() {
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

    @Override
    protected void onStop() {
        super.onStop();
        mAW.stop(this);
    }

    private void setDataListener() {
        TA app = getApp();
        if (app != null) {
            app.setDataListener(new Runnable() {
                @Override
                public void run() {
                    onDataChanged();
                }
            });
            onDataChanged();
        }
    }

    private void removeDataListener() {
        TA app = getApp();
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
            requestSettingsCheck(UR.TOAST_IF_CHANGED);
            break;
        case SETTINGS_UPDATED:
            updateGlobalState();
            requestSettingsCheck(UR.TOAST_IF_CHANGED);
            break;
        case CHECK_SERVICES:
            onCheckServices();
        }
    }

    private void onDataChanged() {
        if (mCursor != null) mCursor.requery();
        mAdapter = null;
        initProfileList();
        updateGlobalState();
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        // In case of configuration change (e.g. screen rotation),
        // the activity is restored but onResume hasn't been called yet
        // so we do it now.
        initOnResume();

        switch(id) {
        case DIALOG_RESET_CHOICES:
            return createDialogResetChoices();
        case DIALOG_DELETE_PROFILE:
            return createDeleteProfileDialog();
        case DIALOG_DELETE_ACTION:
            return createDialogDeleteTimedAction();
        case DIALOG_CHECK_SERVICES:
            return createDialogCheckServices();
        default:
            return null;
        }
    }


    private void onCheckServices() {
        String msg = getCheckServicesMessage();
        if (DEBUG) Log.d(TAG, "Check Services: " + msg == null ? "null" : msg);
        if (msg.length() > 0 && mPV.getCheckService()) {
            showDialog(DIALOG_CHECK_SERVICES);
        }
    }

    private String getCheckServicesMessage() {
        SH sh = new SH(this);
        StringBuilder sb = new StringBuilder();

        if (!sh.canControlAudio()) {
            sb.append("\n- ").append(getString(R.string.checkservices_miss_audio_service));
        }
        if (!sh.canControlWifi()) {
            sb.append("\n- ").append(getString(R.string.checkservices_miss_wifi_service));
        }
        if (!sh.canControlAirplaneMode()) {
            sb.append("\n- ").append(getString(R.string.checkservices_miss_airplane));
        }
        if (!sh.canControlBrigthness()) {
            sb.append("\n- ").append(getString(R.string.checkservices_miss_brightness));
        }

        if (sb.length() > 0) {
            sb.insert(0, getString(R.string.checkservices_warning));
        }

        return sb.toString();
    }

    private Dialog createDialogCheckServices() {
        Builder b = new AlertDialog.Builder(this);

        b.setTitle(R.string.checkservices_dlg_title);
        b.setMessage(getCheckServicesMessage());
        b.setPositiveButton(R.string.checkservices_ok_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeDialog(DIALOG_CHECK_SERVICES);
            }
        });
        b.setNegativeButton(R.string.checkservices_skip_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPV.setCheckService(false);
                removeDialog(DIALOG_CHECK_SERVICES);
            }
        });


        b.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                removeDialog(DIALOG_CHECK_SERVICES);
            }
        });

        return b.create();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenuInfo info = item.getMenuInfo();
        BH h = getHolder(info, null);
        if (h != null) {
            h.onContextMenuSelected(item);
            return true;
        }

        return super.onContextItemSelected(item);
    }

    private BH getHolder(ContextMenuInfo menuInfo, View selectedView) {
        if (selectedView == null && menuInfo instanceof AdapterContextMenuInfo) {
            selectedView = ((AdapterContextMenuInfo) menuInfo).targetView;
        }

        Object tag = selectedView.getTag();
        if (tag instanceof BH) {
            return (BH) tag;
        }

        Log.d(TAG, "Holder missing");
        return null;
    }

    /**
     * Initializes the list-independent buttons: global toggle, check now.
     */
    private void initButtons() {
        mGT = (GT) findViewById(R.id.global_toggle);
        mGS = (GS) findViewById(R.id.global_status);

        updateGlobalState();

        mGT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPV.setServiceEnabled(!mPV.isServiceEnabled());
                updateGlobalState();
                requestSettingsCheck(UR.TOAST_ALWAYS);
            }
        });

        mGS.setWindowVisibilityChangedCallback(new Runnable() {
            @Override
            public void run() {
                updateGlobalState();
            }
        });
    }

    private void updateGlobalState() {
        boolean isEnabled = mPV.isServiceEnabled();
        mGT.setActive(isEnabled);

        mGS.setTextLastTs(mPV.getStatusLastTS());
        if (isEnabled) {
            mGS.setTextNextTs(mPV.getStatusNextTS());
            mGS.setTextNextDesc(mPV.getStatusNextAction());
        } else {
            mGS.setTextNextTs(getString(R.string.globalstatus_disabled));
            mGS.setTextNextDesc("");
        }
        mGS.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, R.string.append_profile,  0, R.string.append_profile).setIcon(R.drawable.ic_menu_add);
        menu.add(0, R.string.settings,  0, R.string.settings).setIcon(R.drawable.ic_menu_preferences);
        menu.add(0, R.string.about,  0, R.string.about).setIcon(R.drawable.ic_menu_help);
        menu.add(0, R.string.report_error,  0, R.string.report_error).setIcon(R.drawable.ic_menu_report);
        menu.add(0, R.string.check_now,  0, R.string.check_now).setIcon(R.drawable.ic_menu_rotate);
        menu.add(0, R.string.reset,  0, R.string.reset).setIcon(R.drawable.ic_menu_revert);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.string.settings:
            mAW.event(AW.Event.MenuSettings);
            showPrefs();
            break;
        case R.string.check_now:
            requestSettingsCheck(UR.TOAST_ALWAYS);
            break;
        case R.string.about:
            mAW.event(AW.Event.MenuAbout);
            showIntro(true /*force*/, false /* checkService */);
            break;
        case R.string.reset:
            mAW.event(AW.Event.MenuReset);
            showResetChoices();
            break;
        case R.string.append_profile:
            appendNewProfile();
            break;
        case R.string.report_error:
            showErrorReport();
            break;
        default:
            return super.onOptionsItemSelected(item);
        }
        return true; // handled
    }

    private void showPrefs() {
        startActivityForResult(new Intent(this, PA.class), SETTINGS_UPDATED);
    }

    private void showErrorReport() {
        startActivity(new Intent(this, ERUI.class));
    }

    /**
     * Requests a setting check.
     *
     * @param displayToast Must be one of {@link UR#TOAST_ALWAYS},
     *                     {@link UR#TOAST_IF_CHANGED} or {@link UR#TOAST_NONE}
     */
    private void requestSettingsCheck(int displayToast) {
        if (DEBUG) Log.d(TAG, "Request settings check");
        Intent i = new Intent(UR.ACTION_UI_CHECK);
        i.putExtra(UR.EXTRA_TOAST_NEXT_EVENT, displayToast);
        sendBroadcast(i);
    }

    protected void showResetChoices() {
        showDialog(DIALOG_RESET_CHOICES);
    }

    private Dialog createDialogResetChoices() {
        Builder d = new AlertDialog.Builder(this);

        d.setCancelable(true);
        d.setTitle(R.string.resetprofiles_msg_confirm_delete);
        d.setIcon(R.drawable.app_icon);
        //d.setMessage("Are you sure you want to delete all profiles?");
        d.setItems(mProfilesDb.getResetLabels(),
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mProfilesDb.resetProfiles(which);
                    removeDialog(DIALOG_RESET_CHOICES);
                    onDataChanged();
                    requestSettingsCheck(UR.TOAST_IF_CHANGED);
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

    public void showTempDialog(long row_id, String title, int dlg_id) {
        mTempDialogRowId = row_id;
        mTempDialogTitle = title;
        showDialog(dlg_id);
    }

    //--------------

    private Dialog createDeleteProfileDialog() {
        final long row_id = mTempDialogRowId;
        final String title = mTempDialogTitle;

        Builder d = new AlertDialog.Builder(PUI.this);

        d.setCancelable(true);
        d.setTitle(R.string.deleteprofile_title);
        d.setIcon(R.drawable.app_icon);
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

        Builder d = new AlertDialog.Builder(PUI.this);

        d.setCancelable(true);
        d.setTitle(R.string.deleteaction_title);
        d.setIcon(R.drawable.app_icon);
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

        Intent intent = new Intent(PUI.this, EPUI.class);
        intent.putExtra(EPUI.EXTRA_PROFILE_ID, prof_index << C.PROFILE_SHIFT);

        startActivityForResult(intent, DATA_CHANGED);
    }

}
