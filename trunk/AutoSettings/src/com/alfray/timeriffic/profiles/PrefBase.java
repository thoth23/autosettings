/*
 * (c) ralfoide gmail com, 2008
 * Project: Timeriffic
 * License TBD
 */

package com.alfray.timeriffic.profiles;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

//-----------------------------------------------

/**
 * The class  does...
 *
 */
public abstract class PrefBase {

    protected final Activity mActivity;

    public PrefBase(Activity activity) {
        mActivity = activity;
    }
    
    public Activity getActivity() {
        return mActivity;
    }
    
    protected String getActionValue(String[] actions, char prefix) {
        for (String action : actions) {
            if (action.length() > 1 && action.charAt(0) == prefix) {
                return action.substring(1);
            }
        }
        
        return null;
    }
}


