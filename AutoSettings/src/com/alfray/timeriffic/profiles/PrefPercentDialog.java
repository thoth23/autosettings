/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License TBD
 */

/**
 * 
 */
package com.alfray.timeriffic.profiles;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.utils.SettingsHelper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.ToggleButton;

public class PrefPercentDialog extends AlertDialog
    implements DialogInterface.OnDismissListener, DialogInterface.OnClickListener,
               SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    private SettingsHelper mHelper;
    private final int mInitialValue;
    private final PrefPercent mPrefPercent;
    private SeekBar mSeekBar;
    private ToggleButton mToggleButton;

    protected PrefPercentDialog(Context context, PrefPercent prefPercent) {
        super(context);
        mPrefPercent = prefPercent;

        setIcon(prefPercent.getIconResId());
        setTitle(prefPercent.getDialogTitle());

        View content = getLayoutInflater().inflate(R.layout.brigthness_alert, null/* root */);
        setView(content);

        mHelper = new SettingsHelper(getContext());
        mInitialValue = mHelper.getCurrentBrightness();

        mSeekBar = (SeekBar) content.findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setMax(100);

        mToggleButton = (ToggleButton) content.findViewById(R.id.toggle);
        mToggleButton.setOnClickListener(this);

        setOnDismissListener(this);

        setButton("Accept", this);

        // set initial value
        if (mPrefPercent.getTag() instanceof String) {
            String tag = (String) mPrefPercent.getTag();
            try {
                int percent = Integer.parseInt(tag);
                mHelper.changeBrightness(percent);
                mToggleButton.setChecked(true);
                mSeekBar.setProgress(percent);
                mSeekBar.setEnabled(true);
                mToggleButton.setTextOn(String.format("Set to %d%%", percent));
            } catch (Exception e) {
                mToggleButton.setChecked(false);
                mSeekBar.setProgress(mInitialValue);
                mSeekBar.setEnabled(false);
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mHelper.changeBrightness(mInitialValue);
        mCurrentPercentButton = null;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromTouch) {
        mHelper.changeBrightness(progress);
        mToggleButton.setTextOn(String.format("Set to %d%%", progress));
        // force the toggle button to update its text
        mToggleButton.setChecked(mToggleButton.isChecked());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // pass
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // pass
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // Update button with percentage selected
        if (mPrefPercent instanceof Button) {
            if (mToggleButton.isChecked()) {
                int percent = mSeekBar.getProgress();
                mPrefPercent.setTag(percent);
                ((Button) mPrefPercent).setText(String
                        .format("%d%%", percent));
            } else {
                mPrefPercent.setTag(null);
                ((Button) mPrefPercent).setText("Unchanged");
            }
        }
        dismiss();
    }

    @Override
    public void onClick(View toggle) {
        mSeekBar.setEnabled(((ToggleButton) toggle).isChecked());
    }
}
