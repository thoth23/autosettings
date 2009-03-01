/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License TBD
 */

package com.alfray.timeriffic.profiles;

import java.util.ArrayList;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

import com.alfray.timeriffic.profiles.PrefBase.ShowMenuClickListener;
import com.alfray.timeriffic.utils.SettingsHelper.RingerMode;

//-----------------------------------------------

class PrefEnum extends PrefBase {

    private ArrayList<String[]> mChoices;
    private final char mActionPrefix;
    private Button mButton;

    public PrefEnum(Activity activity,
                    int buttonResId,
                    RingerMode[] values,
                    String[] actions,
                    char actionPrefix) {
        super(activity);
        mActionPrefix = actionPrefix;

        setupButtonEnum(buttonResId, values, actions, actionPrefix);
    }

    private Button setupButtonEnum(int res_id, Object[] values, String[] actions, char prefix) {
        mChoices = new ArrayList<String[]>();
        mChoices.add(new String[] { "-", "Unchanged" });

        String currentValue = getActionValue(actions, prefix);
        String currentChoice = null;
        
        for (Object value : values) {
            String s = value.toString();
            String p = s.substring(0, 1);
            mChoices.add(new String[] { p, s });
            
            if (currentValue != null &&
                    currentValue.length() >= 1 &&
                    currentValue.charAt(0) == p.charAt(0)) {
                currentChoice = s;
            }
        }

        mButton = setupButton(res_id);
        mButton.setTag(this);

        if (currentChoice != null) {
            mButton.setText(currentChoice);
        } else {
            mButton.setText("Unchanged");
        }
        
        return mButton;
    }

    private Button setupButton(int res_id) {
        Button b = (Button) mActivity.findViewById(res_id);

        mActivity.registerForContextMenu(b);
        b.setOnClickListener(new ShowMenuClickListener());
        return b;
    }
    
    private class ShowMenuClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (view.getTag() instanceof String[]) {
                mActivity.openContextMenu(view);
            }
        }
    }

    public void collectEnum(StringBuilder actions) {
        String t = button.getText().toString();
        
        String[] choices = (String[]) button.getTag();
        for (String choice : choices) {
            String[] vals = choice.split(",");
            if (vals[1].equals(t)) {
                if (!vals[0].equals("-")) {
                    if (actions.length() > 0) actions.append(",");
                    actions.append(prefix);
                    actions.append(vals[0]);
                }
                break;
            }
        }
    }


}


