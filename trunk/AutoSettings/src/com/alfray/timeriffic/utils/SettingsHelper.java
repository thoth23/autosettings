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
    
    public void applySettings(boolean mute, boolean vibrate) {
        AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        if (mute) {
            if (vibrate) {
                if (manager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER) == AudioManager.VIBRATE_SETTING_OFF) {
                    manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
                }
                manager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            } else {
                manager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }
        } else {
            manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
    }
}
