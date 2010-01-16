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

package com.alfray.timeriffic.app;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.error.ExceptionHandler;
import com.alfray.timeriffic.prefs.PrefsValues;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class UpdateService extends Service {

    private static final String TAG = "TFC-UpdServ";
    private static final boolean DEBUG = true;

    private static final String EXTRA_RELEASE_WL = "releaseWL";

    private static WakeLock sWakeLock = null;

    @Override
    public IBinder onBind(Intent intent) {
        // pass
        return null;
    }

    public static void update(Context context, Intent intent , WakeLock wakeLock) {
        Intent i = new Intent(context, UpdateService.class);

        i.setAction(intent.getAction());
        i.putExtras(intent.getExtras());

        if (wakeLock != null) {
            i.putExtra(EXTRA_RELEASE_WL, true);

            // if there's a current wake lock, release it first
            synchronized (UpdateService.class) {
                WakeLock oldWL = sWakeLock;
                if (oldWL != wakeLock) {
                    sWakeLock  = wakeLock;
                    if (oldWL != null) oldWL.release();
                }
            }
        }

        context.startService(i);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        try {
            super.onStart(intent, startId);

            applyUpdate(this, intent);

        } finally {

            Bundle extras = intent.getExtras();
            if (extras != null && extras.getBoolean(EXTRA_RELEASE_WL)) {
                releaseWakeLock();
            }
        }
    }

    @Override
    public void onDestroy() {
        releaseWakeLock();
        super.onDestroy();
    }

    // ---

    private void releaseWakeLock() {
        synchronized (UpdateService.class) {
            WakeLock oldWL = sWakeLock;
            if (oldWL != null) {
                sWakeLock  = null;
                oldWL.release();
            }
        }
    }

    private void applyUpdate(Context context, Intent intent) {
        ExceptionHandler handler = new ExceptionHandler(context);
        try {
            PrefsValues prefs = new PrefsValues(context);
            ApplySettings as = new ApplySettings(context, prefs);

            Bundle extras = intent.getExtras();
            int alarmCount = extras.getInt(Intent.EXTRA_ALARM_COUNT, 0);

            String action = intent.getAction();

            int displayToast = UpdateReceiver.TOAST_NONE;
            boolean fromUI = false;
            if (extras != null) {
                displayToast = extras.getInt(UpdateReceiver.EXTRA_TOAST_NEXT_EVENT, UpdateReceiver.TOAST_NONE);
                fromUI = extras.getBoolean(UpdateReceiver.EXTRA_FROM_UI, false);
            }

            if (DEBUG) Log.d(TAG, "From UI: " + Boolean.toString(fromUI));

            String debug = String.format("UpdateService count:%d, ui:%s, action: %s",
                    alarmCount,
                    Boolean.toString(fromUI),
                    action);
            as.addToDebugLog(debug);
            Log.d(TAG, debug);

            // If we get called because of android.permission.READ_PHONE_STATE
            // we do NOT want to apply all the settings.
            // TODO later what we want is to:
            // - prevent starting airplane mode when in call mode
            // - have a whitelist of phone entries that should never be muted
            if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
                return;
            }

            if (!prefs.isServiceEnabled()) {
                if (DEBUG) Log.d(TAG, "Checking disabled");
                if (displayToast == UpdateReceiver.TOAST_ALWAYS) {
                    showToast(context, prefs,
                            R.string.globalstatus_disabled,
                            Toast.LENGTH_LONG);
                }
                return;
            }

            if (!fromUI) {
                as.addToDebugLog("Check profiles");
            }

            as.apply(displayToast);

        } finally {
            handler.detach();
        }
    }


    private void showToast(Context context, PrefsValues pv, int id, int duration) {
        try {
            Toast.makeText(context, id, duration).show();
        } catch (Throwable t) {
            Log.w(TAG, "Toast.show crashed", t);
            ExceptionHandler.addToLog(pv, t);
        }
    }

}
