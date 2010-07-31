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

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.PUI._CI;

/**
 * The holder for a timed action row.
 */
public class TAH extends BH {

    private static boolean __D = false;
    public static String __T = "TFC-TAHolder";

    public TAH(PUI activity, View view) {
        super(activity, view);
    }

    @Override
    public void _uid() {
        _CI _CI = mA.ci();
        Cursor cursor = mA.c();
        super._uid(cursor.getString(_CI.mDCi),
                        getDotColor(cursor.getInt(_CI.mECi)));
    }

    private Drawable getDotColor(int actionMark) {
        switch (actionMark) {
        case C.AMkP:
            return mA.gd2();
        case C.AMkN:
            return mA.getPurpleDot();
        default:
            return mA.gd1();
        }
    }

    @Override
    public void onCCM(ContextMenu menu) {
        menu.setHeaderTitle(R.string.timedactioncontextmenu_title);

        menu.add(0, R.string.insert_action, 0, R.string.insert_action);
        menu.add(0, R.string.delete, 0, R.string.delete);
        menu.add(0, R.string.edit, 0, R.string.edit);
    }

    @Override
    public void onIS() {
        // trigger edit
        if (__D) Log.d(__T, "action - edit");
        ea(mA.c());
    }

    @Override
    public void onCMS(MenuItem item) {
        switch (item.getItemId()) {
        case R.string.insert_action:
            if (__D) Log.d(__T, "action - insert_action");
            ina(mA.c());
            break;
        case R.string.delete:
            if (__D) Log.d(__T, "action - delete");
            dta(mA.c());
            break;
        case R.string.edit:
            if (__D) Log.d(__T, "action - edit");
            ea(mA.c());
            break;
        default:
            break;
        }
    }
}
