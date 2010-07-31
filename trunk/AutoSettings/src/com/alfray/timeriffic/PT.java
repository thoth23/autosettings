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

import android.app.Activity;
import com.alfray.timeriffic.R;

//-----------------------------------------------

class PT extends PE {

    public PT(Activity activity,
                    int buttonResId,
                    String[] actions,
                    char actionPrefix,
                    String menuTitle) {
        super(activity,
              buttonResId,
              null /*values*/,
              actions,
              actionPrefix,
              menuTitle,
              null /*uiStrings*/);
    }

    /**
     * Special constructor that lets the caller override the on/off strings.
     * uiStrings[0]==on string, uiStrings[1]==off string.
     */
    public PT(Activity activity,
            int buttonResId,
            String[] actions,
            char actionPrefix,
            String menuTitle,
            String[] uiStrings) {
        super(activity,
                buttonResId,
                null /*values*/,
                actions,
                actionPrefix,
                menuTitle,
                uiStrings);
    }

    @Override
    protected void initChoices(Object[] values,
            String[] actions,
            char prefix,
            String[] uiStrings) {

        String on  = getActivity().getResources().getString(R.string.toggle_turn_on);
        String off = getActivity().getResources().getString(R.string.toggle_turn_off);

        if (uiStrings != null && uiStrings.length >= 2) {
            on = uiStrings[0];
            off = uiStrings[1];
        }

        _C c1 = new _C(
                '1',
                on,
                ID_DOT_STATE_ON);
        _C c0 = new _C(
                '0',
                off,
                ID_DOT_STATE_OFF);

        m_Cs.add(c1);
        m_Cs.add(c0);

        String currentValue = getActionValue(actions, prefix);

        if ("1".equals(currentValue)) {
            mCurrentChoice = c1;
        } else if ("0".equals(currentValue)) {
            mCurrentChoice = c0;
        }
    }
}


