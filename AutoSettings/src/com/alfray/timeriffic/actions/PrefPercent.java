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

package com.alfray.timeriffic.actions;

import android.app.Activity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.actions.PrefPercentDialog.Accessor;

//-----------------------------------------------

class PrefPercent extends PrefBase implements View.OnClickListener {

    private char mActionPrefix;
    private Button mButton;
    /** -1 if unchanged, or 0..100 */
    private int mCurrentValue;
    private final PrefPercent[] mPrefPercentOutWrapper;

    private final String mDialogTitle;
    private final int mIconResId;

    private final Accessor mAccessor;

    public PrefPercent(Activity activity,
                    PrefPercent[] prefPercentOutWrapper,
                    int buttonResId,
                    String[] actions,
                    char actionPrefix,
                    String dialogTitle,
                    int iconResId,
                    PrefPercentDialog.Accessor accessor) {
        super(activity);
        mPrefPercentOutWrapper = prefPercentOutWrapper;
        mActionPrefix = actionPrefix;
        mDialogTitle = dialogTitle;
        mIconResId = iconResId;
        mAccessor = accessor;

        mButton = (Button) mActivity.findViewById(buttonResId);
        mButton.setOnClickListener(this);
        mButton.setTag(this);

        mCurrentValue = -1;
        initValue(actions, actionPrefix);
        updateButtonText();
    }

    public void setEnabled(boolean enabled) {
        mButton.setEnabled(enabled);
    }

    public boolean isEnabled() {
        return mButton.isEnabled();
    }

    public void requestFocus() {
        mButton.requestFocus();
    }

    public String getDialogTitle() {
        return mDialogTitle;
    }

    public int getIconResId() {
        return mIconResId;
    }

    public Accessor getAccessor() {
        return mAccessor;
    }

    /** -1 if unchanged, or 0..100 */
    public int getCurrentValue() {
        return mCurrentValue;
    }

    public void setValue(int percent) {
        mCurrentValue = percent;
        updateButtonText();
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
            mButton.setText(R.string.percent_button_unchanged);
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
        mPrefPercentOutWrapper[0] = this;
        mActivity.showDialog(EditActionUI.DIALOG_EDIT_PERCENT);
    }
}


