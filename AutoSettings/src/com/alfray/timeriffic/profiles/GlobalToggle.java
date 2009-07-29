/*
 * (c) ralfoide gmail com, 2008
 * Project: Timeriffic
 * License TBD
 */

package com.alfray.timeriffic.profiles;

import java.util.Arrays;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

//-----------------------------------------------

/**
 * The class  does...
 *
 */
public class GlobalToggle extends ImageButton {

    private static final int[] ACTIVE_STATE = { android.R.attr.state_active };

    private boolean mActive;

    public GlobalToggle(Context context, AttributeSet attrs) {
        super(context, attrs);
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
        if (mActive) extraSpace += 1;
        int[] result = super.onCreateDrawableState(extraSpace);
        if (mActive) result = mergeDrawableStates(result, ACTIVE_STATE);

        return result;
    }
}


