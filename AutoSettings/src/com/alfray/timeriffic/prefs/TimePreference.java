/*
 * (c) ralfoide gmail com, 2008
 * Project: Timeriffic
 * 
 * Note on license: Timeriffic is GPLv3. This is however based on the EditTextPreference
 * source for the Android 1.0 SDK r2 so it must respect the original Apache v2 license.
 * --------------------
 *
 * Copyright (C) 2009 Alfray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.alfray.timeriffic.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.TimePicker;

import com.alfray.timeriffic.R;

//-----------------------------------------------

/**
 * The {@link TimePreference} class is a preference that allows for time
 * input.
 * <p>
 * This is based on the source code of {@link EditTextPreference}.
 * <p>
 * It is a subclass of {@link DialogPreference} and shows the {@link EditText}
 * in a dialog. This {@link EditText} can be modified either programmatically
 * via {@link #getEditText()}, or through XML by setting any EditText
 * attributes on the TimePreference.
 * <p>
 * This preference will store an integer into the SharedPreferences that
 * represents the number of minutes since midnight. So 0=midnight, 60=1 AM,
 * etc till the max value which is 23*60+59 (1440-1=1439).
 * <p>
 * See {@link android.R.styleable#EditText EditText Attributes}.
 */
public class TimePreference extends DialogPreference {
    
    private static final String TAG = "TimePref";
    
    /**
     * The edit mHourMin shown in the dialog.
     */
    private TimePicker mTimePicker;
    
    private int mHourMin;
    
    /**
     * Sets the context to our custom theme.
     */
    private static Context changeTheme(Context context) {
        context.setTheme(R.style.TimeTheme);
        return context;
    }
    
    public TimePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        Log.d("TimePref", "init");
        
        mTimePicker = new TimePicker(context, attrs);
        
        // Give it an ID so it can be saved/restored
        mTimePicker.setId(R.id.time);
        
        /*
         * The preference framework and view framework both have an 'enabled'
         * attribute. Most likely, the 'enabled' specified in this XML is for
         * the preference framework, but it was also given to the view framework.
         * We reset the enabled state.
         */
        mTimePicker.setEnabled(true);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        this(changeTheme(context), attrs, R.attr.timePreferenceStyle);
    }

    public TimePreference(Context context) {
        this(context, null);
    }
    
    /**
     * Saves the mHourMin to the {@link SharedPreferences}.
     * 
     * @param hourMin The mHourMin to save
     */
    public void setTime(int hourMin) {
        final boolean wasBlocking = shouldDisableDependents();
        
        mHourMin = hourMin;
        
        persistInt(hourMin);
        
        final boolean isBlocking = shouldDisableDependents(); 
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
    }
    
    /**
     * Gets the mHourMin from the {@link SharedPreferences}.
     * 
     * @return The current preference value.
     */
    public int getTime() {
        return mHourMin;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        TimePicker timePicker = mTimePicker;
        setTimePickerValue(timePicker, getTime());
        
        ViewParent oldParent = timePicker.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(timePicker);
            }
            addPickerToDialogView(view, timePicker);
        }
    }

    /**
     * Adds the EditText widget of this preference to the dialog's view.
     * 
     * @param dialogView The dialog view.
     */
    private void addPickerToDialogView(View dialogView, TimePicker timePicker) {
        ViewGroup container = (ViewGroup) dialogView.findViewById(R.id.time_container);
        if (container != null) {
            container.addView(timePicker, ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        
        if (positiveResult) {
            int hourMin = getTimePickerHourMin(mTimePicker);
            if (callChangeListener(hourMin)) {
                setTime(hourMin);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        try {
            if (restoreValue) {
                try {
                    setTime(getPersistedInt(mHourMin));
                } catch (Exception e) {
                    // This may fail because the field used to be a String and now
                    // is an int, so try again.
                    int hourMin = parseHoursMin(getPersistedString(toHourMinStr(mHourMin)));
                    
                    // setTime calls persistInt() which will fail if trying to persist
                    // an int where a String is stored. Manually emove the pref first.
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    Editor editor = prefs.edit();
                    editor.remove(getKey());
                    editor.commit();
                    
                    setTime(hourMin);
                }
            } else {
                setTime(parseHoursMin((String) defaultValue));
            }
        } catch (Exception e) {
            Log.e(TAG, "onSetInitialValue failed", e);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }
        
        final SavedState myState = new SavedState(superState);
        myState.mHourMin = getTime();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }
         
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setTime(myState.mHourMin);
    }
    
    // ------------------

    private static class SavedState extends BaseSavedState {
        int mHourMin;
        
        public SavedState(Parcel source) {
            super(source);
            mHourMin = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mHourMin);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    // ------------------
    
    private void setTimePickerValue(TimePicker timePicker, int hourMin) {
        if (hourMin < 0) hourMin = 0;
        if (hourMin >= 24*60) hourMin = 24*60-1;
        int hours = hourMin / 60;
        int minutes = hourMin % 60;
        
        timePicker.setCurrentHour(hours);
        timePicker.setCurrentMinute(minutes);
    }
    
    public static int parseHoursMin(String text) {
        int hours = 0;
        int minutes = 0;
        
        String[] numbers = text.trim().split(":");
        if (numbers.length >= 1) hours = parseNumber(numbers[0], 23);
        if (numbers.length >= 2) minutes = parseNumber(numbers[1], 59);

        return hours*60 + minutes;
    }

    private static int parseNumber(String string, int maxValue) {
        try {
            int n = Integer.parseInt(string);
            if (n < 0) return 0;
            if (n > maxValue) return maxValue;
            return n;
        } catch (Exception e) {
            // ignore
        }
        return 0;
    }
    
    public static String toHourMinStr(int hourMin) {
        if (hourMin < 0) hourMin = 0;
        if (hourMin >= 24*60) hourMin = 24*60-1;
        int hours = hourMin / 60;
        int minutes = hourMin % 60;

        return String.format("%02d:%02d", hours, minutes);
    }

    private int getTimePickerHourMin(TimePicker timePicker) {
        int hours = timePicker.getCurrentHour();
        int minutes = timePicker.getCurrentMinute();

        return hours*60 + minutes;
    }
    
}
