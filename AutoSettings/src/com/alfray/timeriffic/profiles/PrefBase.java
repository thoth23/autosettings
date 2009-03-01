/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License TBD
 */

package com.alfray.timeriffic.profiles;

import android.app.Activity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;

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
    
    protected String getActionValue(String[] actions, char prefix) {
        for (String action : actions) {
            if (action.length() > 1 && action.charAt(0) == prefix) {
                return action.substring(1);
            }
        }
        
        return null;
    }

    protected class ShowMenuClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (view.getTag() instanceof String[]) {
                mActivity.openContextMenu(view);
            }
        }
    }

    protected void appendAction(StringBuilder actions, char prefix, String value) {
        if (actions.length() > 0) actions.append(",");
        actions.append(prefix);
        actions.append(value);
    }

    public abstract void onCreateContextMenu(ContextMenu menu);

    public abstract void onContextItemSelected(MenuItem item);
}


