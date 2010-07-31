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

    private static final boolean __D = true;
    public static final String __T = "TFC-PUI";

    static final int __DC = 42;
    static final int __SU = 43;
    static final int __CS   = 44;

    static final int __DRC = 0;
    static final int __DDA  = 1;
    static final int __DDP = 2;
    static final int __DCS = 3;

    private ListView mPL;
    private PCA mA;
    private LayoutInflater mLI;
    private PDB mPD;

    private AW mAW;
    private PV mPV;
    private Drawable mGD1;
    private Drawable mGD2;
    private Drawable mPD3;
    private Drawable mCO1;
    private Drawable mCO0;

    private GT mGT;
    private GS mGS;

    private long mTDRI;
    private String mTDT;

    private Cursor mC;

    public static class _CI {
        int mICi;
        int mTCi;
        int mDCi;
        int mECi;
        int mPICi;
    };

    private _CI m_CI = new _CI();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String version = "??";
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
        }
        Log.d(__T, String.format("Started %s v%s",
                        getClass().getSimpleName(),
                        version));

        setContentView(R.layout.profiles_screen);
        mLI = getLayoutInflater();

        mPV = new PV(this);
        mGD1 = getResources().getDrawable(R.drawable.dot_gray);
        mGD2 = getResources().getDrawable(R.drawable.dot_green);
        mPD3 = getResources().getDrawable(R.drawable.dot_purple);
        mCO1 = getResources().getDrawable(R.drawable.btn_check_on);
        mCO0 = getResources().getDrawable(R.drawable.btn_check_off);

        ib();
        sias();

        mAW = new AW();
        mAW.start(this);
        mAW._E(AW._E.OpenProfileUI);
    }

    private void sias() {
        final TA tapp = a();
        if (tapp.isFirstStart() && mGT != null) {
            final Runnable action = new Runnable() {
                @Override
                public void run() {
                    si(false, true);
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

    private void si(boolean force, boolean checkServices) {

        boolean showIntro = force;

        if (!showIntro) {
            showIntro = !mPV.isIntroDismissed();
        }

        int currentVersion = -1;
        try {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;




            currentVersion = (currentVersion / 100) * 100;
        } catch (NameNotFoundException e) {

        }
        if (!showIntro && currentVersion > 0) {
            showIntro = currentVersion > mPV.getLastIntroVersion();
        }

        if (showIntro) {

            if (currentVersion > 0) {
                mPV.setLastIntroVersion(currentVersion);
            }

            Intent i = new Intent(this, IA.class);
            if (force) i.putExtra(IA.EXTRA_NO_CONTROLS, true);
            startActivityForResult(i, __CS);
            return;
        }

        if (checkServices) {
            ocs();
        }
    }

    private TA a() {
        Application app = getApplication();
        if (app instanceof TA) return (TA) app;
        return null;
    }

    public Cursor c() {
        return mC;
    };

    _CI ci() {
        return m_CI;
    }

    PDB getProfilesDb() {
        return mPD;
    }

    Drawable gd1() {
        return mGD1;
    }

    Drawable gd2() {
        return mGD2;
    }

    Drawable getPurpleDot() {
        return mPD3;
    }

    Drawable co0() {
        return mCO0;
    }

    Drawable co1() {
        return mCO1;
    }

    private void ipl() {

        Log.d(__T, "init profile list");

        if (mPL == null) {
            mPL = (ListView) findViewById(R.id.profilesList);
            mPL.setRecyclerListener(new PRL());
            mPL.setEmptyView(findViewById(R.id.empty));

            mPL.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View clickedView, int position, long id) {
                    if (__D) Log.d(__T, String.format("onItemClick: pos %d, id %d", position, id));
                    BH h = null;
                    h = h(null, clickedView);
                    if (h != null) h.onIS();
                }
            });

            mPL.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View listview, ContextMenuInfo menuInfo) {
                    if (__D) Log.d(__T, "onCreateContextMenu");
                    BH h = null;
                    h = h(menuInfo, null);
                    if (h != null) h.onCCM(menu);
                }
            });
        }

        if (mPD == null) {
            mPD = new PDB();
            mPD.onCreate(this);

            String next = mPV.getStatusNextTS();
            if (next == null) {

                rsc(UR.TOAST_NONE);
            }
        }

        if (mA == null) {
            if (mC != null) {
                mC.close();
                mC = null;
            }
            mC = mPD.query(
                    -1,
                    new String[] {
                        C._ID,
                        C.T,
                        C.Dsc,
                        C.EN,
                        C.PID,





                    } ,
                    null,
                    null,
                    null
                    );

            m_CI.mICi = mC.getColumnIndexOrThrow(C._ID);
            m_CI.mTCi = mC.getColumnIndexOrThrow(C.T);
            m_CI.mDCi = mC.getColumnIndexOrThrow(C.Dsc);
            m_CI.mECi = mC.getColumnIndexOrThrow(C.EN);
            m_CI.mPICi = mC.getColumnIndexOrThrow(C.PID);

            mA = new PCA(this, m_CI, mLI);
            mPL.setAdapter(mA);

            Log.d(__T, String.format("adapter count: %d", mPL.getCount()));
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
        ior();
    }

    private void ior() {
        ipl();
        _dl();
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
        rdl();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAW.stop(this);
    }

    private void _dl() {
        TA app = a();
        if (app != null) {
            app.setDataListener(new Runnable() {
                @Override
                public void run() {
                    odc();
                }
            });
            odc();
        }
    }

    private void rdl() {
        TA app = a();
        if (app != null) {
            app.setDataListener(null);
        }
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mTDRI = savedInstanceState.getLong("dlg_rowid");
        mTDT = savedInstanceState.getString("dlg_title");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong("dlg_rowid", mTDRI);
        outState.putString("dlg_title", mTDT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mA != null) {
            mA.changeCursor(null);
            mA = null;
        }
        if (mC != null) {
            mC.close();
            mC = null;
        }
        if (mPD != null) {
            mPD.onDestroy();
            mPD = null;
        }
        if (mPL != null) {
            ArrayList<View> views = new ArrayList<View>();
            mPL.reclaimViews(views);
            mPL.setAdapter(null);
            mPL = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
        case __DC:
            odc();
            rsc(UR.TOAST_IF_CHANGED);
            break;
        case __SU:
            ugs();
            rsc(UR.TOAST_IF_CHANGED);
            break;
        case __CS:
            ocs();
        }
    }

    private void odc() {
        if (mC != null) mC.requery();
        mA = null;
        ipl();
        ugs();
    }

    @Override
    protected Dialog onCreateDialog(int id) {




        ior();

        switch(id) {
        case __DRC:
            return cdrc();
        case __DDP:
            return cdpd();
        case __DDA:
            return cddta();
        case __DCS:
            return cdcs();
        default:
            return null;
        }
    }


    private void ocs() {
        String msg = csm();
        if (__D) Log.d(__T, "Check Services: " + msg == null ? "null" : msg);
        if (msg.length() > 0 && mPV.getCheckService()) {
            showDialog(__DCS);
        }
    }

    private String csm() {
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

    private Dialog cdcs() {
        Builder b = new AlertDialog.Builder(this);

        b.setTitle(R.string.checkservices_dlg_title);
        b.setMessage(csm());
        b.setPositiveButton(R.string.checkservices_ok_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeDialog(__DCS);
            }
        });
        b.setNegativeButton(R.string.checkservices_skip_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPV.setCheckService(false);
                removeDialog(__DCS);
            }
        });


        b.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                removeDialog(__DCS);
            }
        });

        return b.create();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenuInfo info = item.getMenuInfo();
        BH h = h(info, null);
        if (h != null) {
            h.onCMS(item);
            return true;
        }

        return super.onContextItemSelected(item);
    }

    private BH h(ContextMenuInfo menuInfo, View selectedView) {
        if (selectedView == null && menuInfo instanceof AdapterContextMenuInfo) {
            selectedView = ((AdapterContextMenuInfo) menuInfo).targetView;
        }

        Object tag = selectedView.getTag();
        if (tag instanceof BH) {
            return (BH) tag;
        }

        Log.d(__T, "Holder missing");
        return null;
    }

    /**
     * Initializes the list-independent buttons: global toggle, check now.
     */
    private void ib() {
        mGT = (GT) findViewById(R.id.global_toggle);
        mGS = (GS) findViewById(R.id.global_status);

        ugs();

        mGT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPV.setServiceEnabled(!mPV.isServiceEnabled());
                ugs();
                rsc(UR.TOAST_ALWAYS);
            }
        });

        mGS.setWindowVisibilityChangedCallback(new Runnable() {
            @Override
            public void run() {
                ugs();
            }
        });
    }

    private void ugs() {
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
            mAW._E(AW._E.MenuSettings);
            sp();
            break;
        case R.string.check_now:
            rsc(UR.TOAST_ALWAYS);
            break;
        case R.string.about:
            mAW._E(AW._E.MenuAbout);
            si(true /*force*/, false /* checkService */);
            break;
        case R.string.reset:
            mAW._E(AW._E.MenuReset);
            src();
            break;
        case R.string.append_profile:
            anp();
            break;
        case R.string.report_error:
            ser();
            break;
        default:
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void sp() {
        startActivityForResult(new Intent(this, PA.class), __SU);
    }

    private void ser() {
        startActivity(new Intent(this, ERUI.class));
    }

    /**
     * Requests a setting check.
     *
     * @param displayToast Must be one of {@link UR#TOAST_ALWAYS},
     *                     {@link UR#TOAST_IF_CHANGED} or {@link UR#TOAST_NONE}
     */
    private void rsc(int displayToast) {
        if (__D) Log.d(__T, "Request settings check");
        Intent i = new Intent(UR.ACTION_UI_CHECK);
        i.putExtra(UR.EXTRA_TOAST_NEXT_EVENT, displayToast);
        sendBroadcast(i);
    }

    protected void src() {
        showDialog(__DRC);
    }

    private Dialog cdrc() {
        Builder d = new AlertDialog.Builder(this);

        d.setCancelable(true);
        d.setTitle(R.string.resetprofiles_msg_confirm_delete);
        d.setIcon(R.drawable.app_icon);

        d.setItems(mPD.getResetLabels(),
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mPD.resetProfiles(which);
                    removeDialog(__DRC);
                    odc();
                    rsc(UR.TOAST_IF_CHANGED);
                }
        });

        d.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                removeDialog(__DRC);
            }
        });

        d.setNegativeButton(R.string.resetprofiles_button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeDialog(__DRC);
            }
        });

        return d.create();
    }


    //--------------

    public void std(long row_id, String title, int dlg_id) {
        mTDRI = row_id;
        mTDT = title;
        showDialog(dlg_id);
    }

    //--------------

    private Dialog cdpd() {
        final long row_id = mTDRI;
        final String title = mTDT;

        Builder d = new AlertDialog.Builder(PUI.this);

        d.setCancelable(true);
        d.setTitle(R.string.deleteprofile_title);
        d.setIcon(R.drawable.app_icon);
        d.setMessage(String.format(
                getString(R.string.deleteprofile_msgbody), title));

        d.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                removeDialog(__DDP);
            }
        });

        d.setNegativeButton(R.string.deleteprofile_button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeDialog(__DDP);
            }
        });

        d.setPositiveButton(R.string.deleteprofile_button_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int count = mPD.deleteProfile(row_id);
                if (count > 0) {
                    mA.notifyDataSetChanged();
                    odc();
                }
                removeDialog(__DDP);
            }
        });

        return d.create();
    }

    private Dialog cddta() {

        final long row_id = mTDRI;
        final String description = mTDT;

        Builder d = new AlertDialog.Builder(PUI.this);

        d.setCancelable(true);
        d.setTitle(R.string.deleteaction_title);
        d.setIcon(R.drawable.app_icon);
        d.setMessage(getString(R.string.deleteaction_msgbody, description));

        d.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                removeDialog(__DDA);
            }
        });

        d.setNegativeButton(R.string.deleteaction_button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeDialog(__DDA);
            }
        });

        d.setPositiveButton(R.string.deleteaction_button_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int count = mPD.deleteAction(row_id);
                if (count > 0) {
                    mA.notifyDataSetChanged();
                    odc();
                }
                removeDialog(__DDA);
            }
        });

        return d.create();
    }


    public void anp() {
        long prof_index = mPD.insertProfile(0,
                        getString(R.string.insertprofile_new_profile_title),
                        true /*isEnabled*/);

        Intent intent = new Intent(PUI.this, EPUI.class);
        intent.putExtra(EPUI.EXTRA_PROFILE_ID, prof_index << C.PS);

        startActivityForResult(intent, __DC);
    }

}
