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

package com.alfray.timeriffic;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alfray.timeriffic.R;

public class PPD extends AlertDialog
    implements DialogInterface.OnDismissListener, DialogInterface.OnClickListener,
               SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    private final int mInitialValue;
    private final PP mPP;
    private SeekBar mSeekBar;
    private TextView mPercentLabel;
    private _A m_A;
    private RadioButton mRadioNoChange;
    private RadioButton mRadioChange;

    public interface _A {
        public int getPercent();
        public void changePercent(int percent);
    }

    protected PPD(Context context, PP pP) {
        super(context);
        mPP = pP;

        if (mPP.getIconResId() != 0) setIcon(mPP.getIconResId());
        if (mPP.getDialogTitle() != null) setTitle(mPP.getDialogTitle());

        View content = getLayoutInflater().inflate(R.layout.percent_alert, null/* root */);
        setView(content);

        m_A = mPP.getAccessor();
        mInitialValue = m_A == null ? -1 : m_A.getPercent();

        mRadioNoChange = (RadioButton) content.findViewById(R.id.radio_nochange);
        mRadioNoChange.setOnClickListener(this);
        mRadioChange   = (RadioButton) content.findViewById(R.id.radio_change);
        mRadioChange.setOnClickListener(this);

        mSeekBar = (SeekBar) content.findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setMax(100);

        mPercentLabel = (TextView) content.findViewById(R.id.percent);

        setOnDismissListener(this);

        setButton(context.getResources().getString(R.string.percent_button_accept), this);

        // set initial value
        int percent = mPP.getCurrentValue();
        if (percent >= 0) {
            if (m_A != null) m_A.changePercent(percent);
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
        if (m_A != null) m_A.changePercent(mInitialValue);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        if (fromTouch) {
            progress = roundup(progress);
            mSeekBar.setProgress(progress);
            updatePercentLabel(progress);
            if (m_A != null) m_A.changePercent(progress);
        }
    }

    /**
     * If progress is > 10%, round up to nearest 5%, otherwise use 1%.
     */
    private int roundup(int progress) {
        if (progress > 10) {
            progress -= 10;
            progress = 10 + (int) (5.0 * Math.round(((double) progress) / 5.0));
        }
        return progress;
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
            mPP.setValue(mSeekBar.getProgress());
        } else {
            mPP.setValue(-1);
        }
        dismiss();
    }

    @Override
    public void onClick(View toggle) {
        mSeekBar.setEnabled(mRadioChange.isChecked());
    }
}
