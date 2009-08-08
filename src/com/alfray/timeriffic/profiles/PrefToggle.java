/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License TBD
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


