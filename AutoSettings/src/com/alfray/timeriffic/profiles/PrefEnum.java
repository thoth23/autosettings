/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License TBD
 */

package com.alfray.timeriffic.profiles;

import java.util.ArrayList;

import android.app.Activity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

//-----------------------------------------------

class PrefEnum extends PrefBase
    implements View.OnClickListener {

    protected static final char UNCHANGED_KEY = '-';
    protected static final String UNCHANGED_UI_NAME = "Unchanged";
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

        Choice c = new Choice(UNCHANGED_KEY, UNCHANGED_UI_NAME);
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
            String s = value.toString();
            char p = s.charAt(0);
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
        if (mCurrentChoice != null &&
                mCurrentChoice.mKey != UNCHANGED_KEY) {
            appendAction(actions, mActionPrefix, Character.toString(mCurrentChoice.mKey));
        }
    }
}


