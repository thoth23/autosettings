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

package com.alfray.timeriffic.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.app.ErrorReporterUI;
import com.alfray.timeriffic.prefs.PrefsValues;

public class ExceptionHandler {

    /** Exception Notification ID. 'ExcH' as an int. */
    private static final int EXCEPTION_NOTIF_ID = 'E' << 24 + 'x' << 16 + 'c' << 8 + 'H';

    private static final String TAG = "TFC-Exception";

    public static final String SEP_START = "{[ ";
    public static final String SEP_END = "} /*end*/ \n";

    private Context mAppContext;
    private DateFormat mDateFormat;
    private UncaughtExceptionHandler mOldHanlder;

    // -----

    public static int getNumExceptionsInLog(Context context) {
        try {
            PrefsValues pv = new PrefsValues(context);
            String curr = pv.getLastExceptions();

            if (curr == null) {
                return 0;
            }

            int count = -1;
            int pos = -1;
            do {
                count++;
                pos = curr.indexOf(SEP_END, pos + 1);
            } while (pos >= 0);

            return count;

        } catch (Exception e) {
            Log.d(TAG, "getNumExceptionsInLog failed", e);
        }

        return 0;
    }


    // -----

    public ExceptionHandler(Context context) {
        // Context is never supposed to be null
        if (context == null) return;

        // We only set our handler if there's no current handler or it is
        // not our -- we don't override our own handler.
        UncaughtExceptionHandler h = Thread.currentThread().getUncaughtExceptionHandler();
        if (h == null || !(h instanceof Handler)) {
            mAppContext = context.getApplicationContext();
            mDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
            mOldHanlder = h;
            Thread.currentThread().setUncaughtExceptionHandler(new Handler());

        }
    }

    public void detach() {
        if (mAppContext != null) {
            Thread.currentThread().setUncaughtExceptionHandler(mOldHanlder);
            mOldHanlder = null;
            mAppContext = null;
        }
    }

    private class Handler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {

            try {
                // No need to log it here, it's done by the default old handler.
                // Log.e(TAG, "Exception caught in Timeriffic", e);

                // get a trace of the exception
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                pw.flush();

                // store the exception
                PrefsValues pv = new PrefsValues(mAppContext);
                String currEx = pv.getLastExceptions();
                if (currEx == null) currEx = "";

                // trim the string if it gets too big.
                if (currEx.length() > 4096) {
                    int pos = currEx.indexOf(SEP_END);
                    if (pos > 0) {
                        currEx = currEx.substring(pos + SEP_END.length());
                    }
                }

                String d = mDateFormat.format(new Date(System.currentTimeMillis()));

                currEx += SEP_START + d + " ]\n";
                currEx += sw.toString() + "\n";
                currEx += SEP_END;
                pv.setLastExceptions(currEx);

                // create a notification
                NotificationManager ns =
                    (NotificationManager) mAppContext.getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notif = new Notification(
                        R.drawable.timeriffic_icon, // icon
                        "Timeriffic Crashed!",      // tickerText
                        System.currentTimeMillis()  // when
                        );
                notif.flags |= Notification.FLAG_AUTO_CANCEL;
                notif.defaults = Notification.DEFAULT_ALL;

                Intent i = new Intent(mAppContext, ErrorReporterUI.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pi = PendingIntent.getActivity(mAppContext, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

                notif.setLatestEventInfo(mAppContext,
                        "Oh no! Timeriffic Crashed!",               // contentTitle
                        "Please click here to report this error.",  // contentText
                        pi                                          // contentIntent
                        );

                ns.notify(EXCEPTION_NOTIF_ID, notif);

            } catch (Throwable t2) {
                // ignore, or we'll get into an infinite loop
            }

            try {
                // chain the calls if the old handler is not one of ours
                if (mOldHanlder != null && !(mOldHanlder instanceof Handler)) {
                    mOldHanlder.uncaughtException(t, e);
                }
            } catch (Throwable t3) {
                // ignore
            }
        }
    };

}
