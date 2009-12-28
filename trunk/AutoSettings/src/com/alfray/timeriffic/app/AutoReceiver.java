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
import android.util.Log;
import android.widget.Toast;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.prefs.PrefsValues;
import com.alfray.timeriffic.utils.ExceptionHandler;


public class AutoReceiver extends BroadcastReceiver {

    private final static boolean DEBUG = true;
    private final static String TAG = "TFC-Receiver";

    /** Name of intent to broadcast to activate this receiver. */
    public final static String ACTION_AUTO_CHECK_STATE = "com.alfray.intent.action.AUTO_CHECK_STATE";

    /** Name of an extra int: how we should display a toast for next event. */
    public final static String EXTRA_TOAST_NEXT_EVENT = "toast-next";

    public final static int TOAST_NONE = 0;
    public final static int TOAST_IF_CHANGED = 1;
    public final static int TOAST_ALWAYS = 2;

    private void showToast(Context context, int id, int duration) {
        try {
            Toast.makeText(context, id, duration).show();
        } catch (Throwable t) {
            Log.w(TAG, "Toast.show crashed", t);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ExceptionHandler handler = new ExceptionHandler(context);
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TimerifficReceiver");
            try {
                wl.acquire();

                int displayToast = TOAST_NONE;
                Bundle extras = intent.getExtras();
                if (extras != null) displayToast = extras.getInt(EXTRA_TOAST_NEXT_EVENT, TOAST_NONE);

                PrefsValues prefs = new PrefsValues(context);

                if (!prefs.isServiceEnabled()) {
                    if (DEBUG) Log.d(TAG, "Checking disabled");
                    if (displayToast == TOAST_ALWAYS) {
                        showToast(context,
                                R.string.globalstatus_disabled,
                                Toast.LENGTH_LONG);
                    }
                    return;
                }

                ApplySettings as = new ApplySettings();
                as.apply(context, displayToast, prefs);

            } finally {
                wl.release();
            }
        } finally {
            handler.detach();
        }
    }
}
