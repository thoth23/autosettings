/*
 * (c) ralfoide gmail com, 2008
 * Project: Timeriffic
 * License: GPLv3
 */

package com.alfray.timeriffic.utils;

import com.alfray.timeriffic.prefs.PrefsValues;

import android.content.Context;
import android.media.AudioManager;

public class SettingsHelper {
    
    private final Context mContext;
    private PrefsValues mPrefs;

    public SettingsHelper(Context context) {
        mContext = context;
        mPrefs = new PrefsValues(context);
    }

    public void applyStartSettings() {
        applySettings(mPrefs.startMute(), mPrefs.startVibrate());
    }

    public void applyStopSettings() {
        applySettings(mPrefs.stopMute(), mPrefs.stopVibrate());
    }
        
    private void applySettings(boolean mute, boolean vibrate) {
        AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        if (mute) {
            if (vibrate) {
                manager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            } else {
                manager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }
        } else {
            manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
    }
}
