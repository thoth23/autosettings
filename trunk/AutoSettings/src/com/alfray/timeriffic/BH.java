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

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.PUI._CI;

abstract class BH {

    private final TextView mD;

    protected final PUI mA;

    public BH(PUI activity, View view) {
        mA = activity;
        mD = view != null ? (TextView) view.findViewById(R.id.description) : null;
    }

    protected void _uid(String description, Drawable state) {
        if (description != null) mD.setText(description);
        if (state != null) mD.setCompoundDrawablesWithIntrinsicBounds(
                state /*left*/, null /*top*/, null /*right*/, null /*bottom*/);
    }

    public abstract void _uid();
    public abstract void onIS();
    public abstract void onCCM(ContextMenu menu);
    public abstract void onCMS(MenuItem item);


    // --- profile actions ---

    private void sea(Class<?> activity, String extra_id, long extra_value) {
        Intent intent = new Intent(mA, activity);
        intent.putExtra(extra_id, extra_value);

        mA.startActivityForResult(intent, PUI.__DC);
    }

    protected void dp(Cursor cursor) {
        _CI _CI = mA.ci();
        final long row_id = cursor.getLong(_CI.mICi);
        String title = cursor.getString(_CI.mDCi);

        mA.std(row_id, title, PUI.__DDP);
    }

    protected void inp(Cursor beforeCursor) {
        long prof_index = 0;
        if (beforeCursor != null) {
            _CI _CI = mA.ci();
            prof_index = beforeCursor.getLong(_CI.mPICi) >> C.PS;
        }

        PDB profDb = mA.getProfilesDb();
        prof_index = profDb.insertProfile(prof_index,
                        mA.getString(R.string.insertprofile_new_profile_title),
                        true /*isEnabled*/);

        sea(EPUI.class,
                EPUI.EXTRA_PROFILE_ID, prof_index << C.PS);
    }

    protected void ep(Cursor cursor) {
        _CI _CI = mA.ci();
        long prof_id = cursor.getLong(_CI.mPICi);

        sea(EPUI.class, EPUI.EXTRA_PROFILE_ID, prof_id);
    }

    // --- timed actions ----


    protected void dta(Cursor cursor) {
        _CI _CI = mA.ci();
        final long row_id = cursor.getLong(_CI.mICi);
        String description = cursor.getString(_CI.mDCi);

        mA.std(row_id, description, PUI.__DDA);
    }

    protected void ina(Cursor beforeCursor) {
        long prof_index = 0;
        long action_index = 0;
        if (beforeCursor != null) {
            _CI _CI = mA.ci();
            prof_index = beforeCursor.getLong(_CI.mPICi);
            action_index = prof_index & C.AMk;
            prof_index = prof_index >> C.PS;
        }

        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(System.currentTimeMillis());
        int hourMin = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);

        int day = TAU.calendarDayToActionDay(c);

        PDB profDb = mA.getProfilesDb();
        action_index = profDb.insertTimedAction(
                prof_index,
                action_index,
                hourMin,    // hourMin
                day,        // days
                "",         // actions
                0           // nextMs
                );

        long action_id = (prof_index << C.PS) + action_index;

        sea(EAUI.class, EAUI._EAI, action_id);
    }

    protected void ea(Cursor cursor) {
        _CI _CI = mA.ci();
        long action_id = cursor.getLong(_CI.mPICi);

        sea(EAUI.class, EAUI._EAI, action_id);
    }

}
