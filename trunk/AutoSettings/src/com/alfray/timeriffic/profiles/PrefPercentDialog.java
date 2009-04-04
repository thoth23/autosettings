/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License TBD
 */

package com.alfray.timeriffic.profiles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.alfray.timeriffic.R;

public class PrefPercentDialog extends AlertDialog
    implements DialogInterface.OnDismissListener, DialogInterface.OnClickListener,
               SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    private final int mInitialValue;
    private final PrefPercent[] mPrefPercentOutWrapper;
    private final PrefPercent mPrefPercent;
    private SeekBar mSeekBar;
    private TextView mPercentLabel;
    private Accessor mAccessor;
    private RadioButton mRadioNoChange;
    private RadioButton mRadioChange;

    public interface Accessor {
        public int getPercent();
        public void changePercent(int percent);
    }

    protected PrefPercentDialog(Context context, PrefPercent[] prefPercentOutWrapper) {
        super(context);
        mPrefPercentOutWrapper = prefPercentOutWrapper;
        mPrefPercent = prefPercentOutWrapper[0];

        if (mPrefPercent.getIconResId() != 0) setIcon(mPrefPercent.getIconResId());
        if (mPrefPercent.getDialogTitle() != null) setTitle(mPrefPercent.getDialogTitle());

        View content = getLayoutInflater().inflate(R.layout.percent_alert, null/* root */);
        setView(content);

        mAccessor = mPrefPercent.getAccessor();
        mInitialValue = mAccessor == null ? -1 : mAccessor.getPercent();

        mRadioNoChange = (RadioButton) content.findViewById(R.id.radio_nochange);
        mRadioNoChange.setOnClickListener(this);
        mRadioChange   = (RadioButton) content.findViewById(R.id.radio_change);
        mRadioChange.setOnClickListener(this);

        mSeekBar = (SeekBar) content.findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setMax(100);

        mPercentLabel = (TextView) content.findViewById(R.id.percent);

        setOnDismissListener(this);

        setButton("Accept", this);

        // set initial value
        int percent = mPrefPercent.getCurrentValue();
        if (percent >= 0) {
            if (mAccessor != null) mAccessor.changePercent(percent);
            mRadioChange.setChecked(true);
            mRadioNoChange.setChecked(false);
            mSeekBar.setProgress(percent);
            mSeekBar.setEnabled(true);
        } else {
            mRadioChange.setChecked(false);
            mRadioNoChange.setChecked(true);
            mSeekBar.setProgress(mInitialValue);
            mSeekBar.setEnabled(false);
        }

        updatePercentLabel(-1);
    }

    private void updatePercentLabel(int percent) {
        if (percent < 0) percent = mSeekBar.getProgress();
        mPercentLabel.setText(String.format("%d%%", percent));
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (mAccessor != null) mAccessor.changePercent(mInitialValue);
        mPrefPercentOutWrapper[0] = null;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        updatePercentLabel(progress);
        if (mAccessor != null) mAccessor.changePercent(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // pass
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // pass
    }

    /** DialogInterface.OnClickListener callback, when dialog is accepted */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        // Update button with percentage selected
        if (mRadioChange.isChecked()) {
            mPrefPercent.setValue(mSeekBar.getProgress());
        } else {
            mPrefPercent.setValue(-1);
        }
        dismiss();
    }

    @Override
    public void onClick(View toggle) {
        mSeekBar.setEnabled(mRadioChange.isChecked());
    }
}
