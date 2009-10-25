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

package com.alfray.timeriffic.profiles;

import android.database.Cursor;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.profiles.ProfilesUI.ColIndexes;

/**
 * The holder for a timed action row.
 */
class TimedActionHolder extends BaseHolder {

    private static boolean DEBUG = true;
    private static String TAG = "Timerfc-TimedActionHolder";

    public TimedActionHolder(ProfilesUI activity, View view) {
        super(activity, view);
    }

    @Override
    public void setUiData(Cursor cursor) {
        ColIndexes colIndexes = mActivity.getColIndexes();
        super.setUiData(cursor,
                cursor.getString(colIndexes.mDescColIndex),
                cursor.getInt(colIndexes.mEnableColIndex) != 0 ?
                        mActivity.getGreenDot() :
                        mActivity.getGrayDot());
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
