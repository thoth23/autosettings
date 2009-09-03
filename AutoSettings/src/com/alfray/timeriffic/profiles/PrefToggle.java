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

import android.app.Activity;
import com.alfray.timeriffic.R;

//-----------------------------------------------

class PrefToggle extends PrefEnum {

    public PrefToggle(Activity activity,
                    int buttonResId,
                    String[] actions,
                    char actionPrefix,
                    String menuTitle) {
        super(activity,
              buttonResId,
              null /*values*/,
              actions,
              actionPrefix,
              menuTitle);
    }

    @Override
    protected void initChoices(Object[] values, String[] actions, char prefix) {

        Choice c1 = new Choice('1', getActivity().getResources().getString(R.string.toggle_turn_on));
        Choice c0 = new Choice('0', getActivity().getResources().getString(R.string.toggle_turn_off));

        mChoices.add(c1);
        mChoices.add(c0);

        String currentValue = getActionValue(actions, prefix);

        if ("1".equals(currentValue)) {
            mCurrentChoice = c1;
        } else if ("0".equals(currentValue)) {
            mCurrentChoice = c0;
        }
    }
}


