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
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.PUI._CI;

public class PHH extends BH {

    private static boolean __D = false;
    public static String __T = "TFC-PHHolder";

    public PHH(PUI activity, View view) {
        super(activity, view);
    }

    @Override
    public void _uid() {
        _CI _CI = mA.ci();
        Cursor cursor = mA.c();
        super._uid(cursor.getString(_CI.mDCi),
                        cursor.getInt(_CI.mECi) != 0 ?
                                mA.co1() :
                                mA.co0());
    }

    @Override
    public void onCCM(ContextMenu menu) {
        menu.setHeaderTitle(R.string.profilecontextmenu_title);

        menu.add(0, R.string.insert_profile, 0, R.string.insert_profile);
        menu.add(0, R.string.insert_action, 0, R.string.insert_action);
        menu.add(0, R.string.delete, 0, R.string.delete);
        menu.add(0, R.string.rename, 0, R.string.rename);
    }

    @Override
    public void onIS() {
        Cursor cursor = mA.c();
        if (cursor == null) return;

        _CI _CI = mA.ci();

        boolean enabled = cursor.getInt(_CI.mECi) != 0;
        enabled = !enabled;

        PDB profDb = mA.getProfilesDb();
        profDb.updateProfile(
                cursor.getLong(_CI.mPICi),
                null,
                enabled);

        cursor.requery();
        _uid(null,
                  enabled ? mA.co1() : mA.co0());
    }

    @Override
    public void onCMS(MenuItem item) {
        switch (item.getItemId()) {
        case R.string.insert_profile:
            if (__D) Log.d(__T, "profile - insert_profile");
            inp(mA.c());
            break;
        case R.string.insert_action:
            if (__D) Log.d(__T, "profile - insert_action");
            ina(mA.c());
            break;
        case R.string.delete:
            if (__D) Log.d(__T, "profile - delete");
            dp(mA.c());
            break;
        case R.string.rename:
            if (__D) Log.d(__T, "profile - rename");
            ep(mA.c());
            break;
        default:
            break;
        }
    }

}
