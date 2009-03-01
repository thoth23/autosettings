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
import android.widget.Button;

import com.alfray.timeriffic.profiles.PrefEnum.Choice;

//-----------------------------------------------

class PrefPercent extends PrefBase
    implements View.OnClickListener {

    private static final String UNCHANGED_UI_NAME = "Unchanged";

    private char mActionPrefix;
    private Button mButton;
    /** -1 if unchanged, or 0..100 */
    private int mCurrentValue;
    private final PrefPercent[] mCurrentPrefPercent;

    private final String mDialogTitle;
    private final String mIconResId;

    public PrefPercent(Activity activity,
                    PrefPercent[] currentPrefPercent, int buttonResId,
                    String[] actions,
                    char actionPrefix,
                    String dialogTitle,
                    String iconResId) {
        super(activity);
        mCurrentPrefPercent = currentPrefPercent;
        mActionPrefix = actionPrefix;
        mDialogTitle = dialogTitle;
        mIconResId = iconResId;

        mButton = (Button) mActivity.findViewById(buttonResId);
        mButton.setOnClickListener(this);
        mButton.setTag(this);

        mCurrentValue = -1;
        initValue(actions, actionPrefix);
        updateButtonText();
    }
    
    public String getDialogTitle() {
        return mDialogTitle;
    }
    
    public String getIconResId() {
        return mIconResId;
    }

    private void initValue(String[] actions, char prefix) {

        String currentValue = getActionValue(actions, prefix);

        try {
            mCurrentValue = Integer.parseInt(currentValue);
        } catch (Exception e) {
            mCurrentValue = -1;
        }
    }
    
    private void updateButtonText() {
        if (mCurrentValue < 0) {
            mButton.setText(UNCHANGED_UI_NAME);
        } else {
            mButton.setText(String.format("%d%%", mCurrentValue));
        }
    }

    public void collectResult(StringBuilder actions) {
        if (mCurrentValue >= 0) {
            appendAction(actions, mActionPrefix, Integer.toString(mCurrentValue));
        }
    }
    
    @Override
    public void onContextItemSelected(MenuItem item) {
        // from PrefBase, not used here
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu) {
        // from PrefBase, not used here
    }

    @Override
    public void onClick(View v) {
        mCurrentPrefPercent[0] = this;
        mActivity.showDialog(EditActionUI.DIALOG_EDIT_PERCENT);
    }
}


