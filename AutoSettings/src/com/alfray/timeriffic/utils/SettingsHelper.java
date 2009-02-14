/*
 * (c) ralfoide gmail com, 2008
 * Project: Timeriffic
 * License: GPLv3
 */

package com.alfray.timeriffic.utils;

import com.alfray.timeriffic.prefs.PrefsValues;

import android.content.Context;
import android.media.AudioManager;
import android.net.wifi.WifiManager;

/**
 * Helper class that changes settings.
 * <p/>
 * Methods here directly correspond to something available in the UI.
 * Currently the different cases are:
 * <ul>
 * <li> Ringer: normal, silent, vibrate only.
 * <li> Ringer Vibrate: on, off, only when silent.
 * <li> Wifi: on/off
 * <li> Brightness: on/off
 * <li>  
 * </ul>
 */
public class SettingsHelper {
    
    private final Context mContext;

    public enum RingerMode {
        NORMAL,
        SILENT,
        VIBRATE
    }
    
    public enum VibrateRingerMode {
        WHEN_POSSIBLE,
        NEVER,
        ONLY_WHEN_SILENT
    }
    
    public SettingsHelper(Context context) {
        mContext = context;
    }
    
    public void changeRinger(RingerMode ringer) {
        AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        switch (ringer) {
            case NORMAL:
                manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                break;
            case SILENT:
                manager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                break;
            case VIBRATE:
                manager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                break;
        }
    }
    
    public void changeRingerVibrate(VibrateRingerMode vib) {
        AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        switch(vib) {
            case WHEN_POSSIBLE:
                manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
                break;
            case NEVER:
                manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
                break;
            case ONLY_WHEN_SILENT:
                manager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ONLY_SILENT);
                break;
        }
    }
    
    public void changeRingerVolume(int percent) {
        AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        int max = manager.getStreamMaxVolume(AudioManager.STREAM_RING);
        int vol = (max * percent) / 100;
        manager.setStreamVolume(AudioManager.STREAM_RING, vol, 0 /*flags*/);
    }

    public void changeWifi(boolean enabled) {
        WifiManager manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        
        manager.setWifiEnabled(enabled);
    }
}
