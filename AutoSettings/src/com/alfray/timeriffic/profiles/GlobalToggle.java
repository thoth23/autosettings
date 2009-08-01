/*
 * (c) ralfoide gmail com, 2008
 * Project: Timeriffic
 * License TBD
 */

package com.alfray.timeriffic.profiles;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.prefs.PrefsValues;

//-----------------------------------------------

/**
 * The class  does...
 *
 */
public class GlobalToggle extends ImageButton {

    private final int[] ACTIVE_STATE = {
        android.R.attr.state_active,
        R.attr.state_gt_fast_anim
    };

    private boolean mActive;
    private PrefsValues mPrefsValues;

    public GlobalToggle(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPrefsValues = new PrefsValues(context);
    }

    public void setActive(boolean active) {
        mActive = active;
        invalidateDrawable(getDrawable());
    }

    public boolean isActive() {
        return mActive;
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        if (mActive) extraSpace += 2;
        int[] result = super.onCreateDrawableState(extraSpace);
        if (mActive) {
            // Replace second item of our state array by the desired
            // animation state based on the prefs.
            switch(mPrefsValues.getGlobalToggleAnim()) {
                case NO_ANIM:
                    ACTIVE_STATE[1] = R.attr.state_gt_no_anim;
                    break;
                case SLOW:
                    ACTIVE_STATE[1] = R.attr.state_gt_slow_anim;
                    break;
                case FAST:
                    ACTIVE_STATE[1] = R.attr.state_gt_fast_anim;
                    break;
            }
            result = mergeDrawableStates(result, ACTIVE_STATE);
        }

        return result;
    }
}


