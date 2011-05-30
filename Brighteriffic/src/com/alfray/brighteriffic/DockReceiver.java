/*
 * Project: Brighteriffic
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

package com.alfray.brighteriffic;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

/*
 * For debugging:
 * $ adb -d shell am broadcast android.intent.action.DOCK_EVENT
 */
public class DockReceiver extends BroadcastReceiver {

    private static boolean DEBUG = false;
    private static String TAG = DockReceiver.class.getSimpleName();

    public DockReceiver() {
        // Nothing here. This is constructed for *each* call.
    }

    public void onUpdate(Context context,
            AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {

        // set the remove view
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

        Intent intent = new Intent(ChangeBrightnessActivity.ACTION_TOGGLE_BRIGHTNESS);
        intent.putExtra(ChangeBrightnessActivity.INTENT_TOGGLE_BRIGHTNESS, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.icon, pi);

        // update it
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_DOCK_EVENT.equals(intent.getAction())) {
            if (DEBUG) Log.d(TAG, "Unknown intent: " + intent.getAction());
            return;
        }

        int state = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, -1);

        if (state == -1) {
            if (DEBUG) Log.d(TAG, "Invalid state: " + Integer.toString(state));
            return;
        }

        PrefsValues pv = new PrefsValues(context);
        int value = -1;
        boolean save = false;

        switch (state) {
        case Intent.EXTRA_DOCK_STATE_CAR:
            // I'm in a car
            if (pv.getUseCarBrightness()) {
                value = pv.getCarBrightness();
                save = true;
                if (DEBUG) Log.d(TAG, "Use car brightness: " + Integer.toString(value));
            }
            break;
        case Intent.EXTRA_DOCK_STATE_DESK:
            // I'm at a desk
            if (pv.getUseDeskBrightness()) {
                value = pv.getDeskBrightness();
                save = true;
                if (DEBUG) Log.d(TAG, "Use desk brightness: " + Integer.toString(value));
            }
            break;
        case Intent.EXTRA_DOCK_STATE_UNDOCKED:
            // I'm free!
            value = pv.getSavedBrightness();
            if (value != -1) {
                pv.setSavedBrightness(-1);
                if (DEBUG) Log.d(TAG, "Reset saved brightness: " + Integer.toString(value));
            }
            break;
        }

        if (value >= 0 && value <= 100) {
            Intent i = new Intent(ChangeBrightnessActivity.ACTION_TOGGLE_BRIGHTNESS);
            i.putExtra(ChangeBrightnessActivity.INTENT_SET_BRIGHTNESS, value / 100.0f);
            i.putExtra(ChangeBrightnessActivity.INTENT_SAVE_BRIGHTNESS, save);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } else if (DEBUG) {
            Log.d(TAG, "Brightness out of bounds: " + Integer.toString(value));
        }

    }
}
