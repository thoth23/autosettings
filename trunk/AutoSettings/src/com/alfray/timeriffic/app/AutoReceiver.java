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

package com.alfray.timeriffic.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.error.ExceptionHandler;
import com.alfray.timeriffic.prefs.PrefsValues;


public class AutoReceiver extends BroadcastReceiver {

    private final static boolean DEBUG = true;
    public final static String TAG = "TFC-Receiver";

    /** Name of intent to broadcast to activate this receiver. */
    public final static String ACTION_AUTO_CHECK_STATE = "com.alfray.intent.action.AUTO_CHECK_STATE";

    /** Name of an extra int: how we should display a toast for next event. */
    public final static String EXTRA_TOAST_NEXT_EVENT = "toast-next";

    /** Name of an extra bool: true if the update request comes from a manual user
     * intervention. In this case, we want to skip some logging or checks. */
    public final static String EXTRA_FROM_UI = "from-ui";

    public final static int TOAST_NONE = 0;
    public final static int TOAST_IF_CHANGED = 1;
    public final static int TOAST_ALWAYS = 2;

    private void showToast(Context context, PrefsValues pv, int id, int duration) {
        try {
            Toast.makeText(context, id, duration).show();
        } catch (Throwable t) {
            Log.w(TAG, "Toast.show crashed", t);
            ExceptionHandler.addToLog(pv, t);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "TimerifficReceiver");
        try {
            wl.acquire();
            ExceptionHandler handler = new ExceptionHandler(context);
            try {
                PrefsValues prefs = new PrefsValues(context);
                ApplySettings as = new ApplySettings(context, prefs);

                Bundle extras = intent.getExtras();
                int alarmCount = extras.getInt(Intent.EXTRA_ALARM_COUNT, 0);

                String action = intent.getAction();

                int displayToast = TOAST_NONE;
                boolean fromUI = false;
                if (extras != null) {
                    displayToast = extras.getInt(EXTRA_TOAST_NEXT_EVENT, TOAST_NONE);
                    fromUI = extras.getBoolean(EXTRA_FROM_UI, false);
                }

                if (DEBUG) Log.d(TAG, "From UI: " + Boolean.toString(fromUI));

                String debug = String.format("AutoReceiver count:%d, ui:%s, action: %s",
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
                    if (displayToast == TOAST_ALWAYS) {
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
        } finally {
            wl.release();
        }
    }
}
