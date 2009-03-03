/*
 * (c) ralfoide gmail com, 2008
 * Project: Timeriffic
 * License: GPLv3
 */

package com.alfray.timeriffic.utils;

import android.content.Context;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

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

    private static final String TAG = "SettingsHelper";
    
    private final Context mContext;

    public boolean canControlWifi() {
        return false;
    }
    
    public boolean canControlBrigthness() {
        return false;
    }
    
    public enum RingerMode {
        NORMAL,
        SILENT,
        VIBRATE;

        /** Capitalizes the string */
        @Override
        public String toString() {
            String s = super.toString();
            return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
        }
    }
    
    public enum VibrateRingerMode {
        WHEN_POSSIBLE,
        NEVER,
        ONLY_WHEN_SILENT;

        /** Capitalizes the string */
        @Override
        public String toString() {
            String s = super.toString();
            return s.substring(0, 1).toUpperCase() + 
                s.substring(1).toLowerCase().replace('_', ' ');
        }
    }
    
    public SettingsHelper(Context context) {
        mContext = context;
    }
    
    public void changeRingerMode(RingerMode ringer) {
        AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        Log.d(TAG, "changeRingerMode: " + ringer.toString());
        
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

        Log.d(TAG, "changeRingerVibrate: " + vib.toString());

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

        Log.d(TAG, "changeRingerVolume: " + Integer.toString(percent));

        int max = manager.getStreamMaxVolume(AudioManager.STREAM_RING);
        int vol = (max * percent) / 100;
        manager.setStreamVolume(AudioManager.STREAM_RING, vol, 0 /*flags*/);
    }
    
    public int getRingerVolume() {
        AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        int vol = manager.getStreamVolume(AudioManager.STREAM_RING);
        int max = manager.getStreamMaxVolume(AudioManager.STREAM_RING);
        
        return (vol * 100 / max);
    }

    public void changeWifi(boolean enabled) {
        WifiManager manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        Log.d(TAG, "changeWifi: " + (enabled ? "on" : "off"));

        manager.setWifiEnabled(enabled);
    }
    
    private static int MIN_BRIGHTNESS = android.os.Power.BRIGHTNESS_DIM + 10;
    private static int MAX_BRIGHTNESS = android.os.Power.BRIGHTNESS_ON;

    /**
     * @param percent The new value in 0..100 range (will get mapped to adequate OS values)
     * @param persistent True if the setting should be made persistent, e.g. written to system pref.
     *  If false, only the current hardware value is changed.
     */
    public void changeBrightness(int percent, boolean persistent) {
        // Reference:
        // http://android.git.kernel.org/?p=platform/packages/apps/Settings.git;a=blob;f=src/com/android/settings/BrightnessPreference.java
        // The source indicates
        // - Backlight range is 0..255
        // - Must not set to 0 (user would see nothing) so they use 10 as minimum
        // - All constants are in android.os.Power which is hidden from the SDK in 1.0
        //   yet available in 1.1
        // - To get value: Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        // - To set value: Settings.System.putInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, v);

//        Log.d(TAG, "changeBrightness: " + Integer.toString(percent));
//        
//        int v = MIN_BRIGHTNESS + percent * (MAX_BRIGHTNESS - MIN_BRIGHTNESS) / 100;
//        
//        if (persistent) {
//            Settings.System.putInt(mContext.getContentResolver(),
//                    Settings.System.SCREEN_BRIGHTNESS,
//                    v);
//        }
//        
//        try {
//            IHardwareService hs = IHardwareService.Stub.asInterface(ServiceManager.getService("hardware"));
//            if (hs != null) hs.setScreenBacklight(v);
//        } catch (Throwable t) {
//            Log.e(TAG, "Failed to set brightness to " + Integer.toString(v), t);
//        }
    }

    /**
     * Returns screen brightness in range 0..100%.
     * <p/>
     * See comments in {@link #changeBrightness(int)}. The real range is 0..255
     * but 10..255 is only usable (to avoid a non-readable screen). So map 10..255
     * to 0..100%.
     */
    public int getCurrentBrightness() {
        try {
            int v = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
            // transform 10..255 into 0..100
            v = (v - MIN_BRIGHTNESS) * 100 / (MAX_BRIGHTNESS - MIN_BRIGHTNESS);
            // clip to 0..100
            return Math.min(100, Math.max(0, v));
        } catch (SettingNotFoundException e) {
            // If not found, return max
            return MAX_BRIGHTNESS;
        }
    }
}
