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

import java.util.ArrayList;

import android.app.Activity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.alfray.timeriffic.R;
import com.alfray.timeriffic.utils.SettingsHelper.RingerMode;
import com.alfray.timeriffic.utils.SettingsHelper.VibrateRingerMode;

//-----------------------------------------------

class PrefEnum extends PrefBase
    implements View.OnClickListener {

    protected static final char UNCHANGED_KEY = '-';
    private final char mActionPrefix;

    protected static class Choice {
        public final char mKey;
        public final String mUiName;
        public Choice(char key, String uiName) {
            mKey = key;
            mUiName = uiName;
        }
    }

    protected ArrayList<Choice> mChoices = new ArrayList<Choice>();
    protected Choice mCurrentChoice;
    private Button mButton;
    private final String mMenuTitle;

    public PrefEnum(Activity activity,
                    int buttonResId,
                    Object[] values,
                    String[] actions,
                    char actionPrefix,
                    String menuTitle) {
        super(activity);
        mActionPrefix = actionPrefix;
        mMenuTitle = menuTitle;

        mButton = (Button) mActivity.findViewById(buttonResId);
        mActivity.registerForContextMenu(mButton);
        mButton.setOnClickListener(this);
        mButton.setTag(this);

        Choice c = new Choice(UNCHANGED_KEY,
                              activity.getResources().getString(R.string.enum_unchanged));
        mChoices.add(c);
        mCurrentChoice = c;

        initChoices(values, actions, actionPrefix);

        mButton.setText(mCurrentChoice.mUiName);
    }

    public void setEnabled(boolean enable) {
        mButton.setEnabled(enable);
    }

    public boolean isEnabled() {
        return mButton.isEnabled();
    }

    public void requestFocus() {
        mButton.requestFocus();
    }

    protected void initChoices(Object[] values, String[] actions, char prefix) {

        String currentValue = getActionValue(actions, prefix);

        for (Object value : values) {
            String s = "#PrefEnum: Error Unknown Setting#";
            char p = 0;
            if (value instanceof RingerMode) {
                p = ((RingerMode) value).getActionLetter();
                s = ((RingerMode) value).toUiString(getActivity());
            } else if (value instanceof VibrateRingerMode) {
                p = ((VibrateRingerMode) value).getActionLetter();
                s = ((VibrateRingerMode) value).toUiString(getActivity());
            }

            Choice c = new Choice(p, s);
            mChoices.add(c);

            if (currentValue != null &&
                    currentValue.length() >= 1 &&
                    currentValue.charAt(0) == p) {
                mCurrentChoice = c;
            }
        }
    }

    @Override
    public void onClick(View view) {
        mActivity.openContextMenu(mButton);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu) {

        menu.setHeaderTitle(mMenuTitle);

        for (Choice choice : mChoices) {
            menu.add(choice.mUiName);
        }
    }

    @Override
    public void onContextItemSelected(MenuItem item) {

        CharSequence title = item.getTitle();

        for (Choice choice : mChoices) {
            if (choice.mUiName.equals(title)) {
                mCurrentChoice = choice;
                mButton.setText(title);
                break;
            }
        }
    }

    public void collectResult(StringBuilder actions) {
        if (mCurrentChoice != null && mCurrentChoice.mKey != UNCHANGED_KEY) {
            appendAction(actions, mActionPrefix, Character.toString(mCurrentChoice.mKey));
        }
    }
}


