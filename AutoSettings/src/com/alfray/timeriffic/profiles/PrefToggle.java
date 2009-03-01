/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License TBD
 */

package com.alfray.timeriffic.profiles;

import android.app.Activity;

//-----------------------------------------------

class PrefToggle extends PrefEnum {

    public PrefToggle(Activity activity,
                    int buttonResId,
                    String[] actions,
                    char actionPrefix) {
        super(activity, buttonResId, null /*values*/, actions, actionPrefix);
    }

    @Override
    protected void initChoices(Object[] values, String[] actions, char prefix) {

        Choice c1 = new Choice('1', "Enabled");
        Choice c0 = new Choice('0', "Disabled");
        
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


