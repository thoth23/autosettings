/*
 * (c) ralfoide gmail com, 2008
 * Project: Timeriffic
 * License: GPLv3
 */

package com.alfray.timeriffic.utils;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;

/**
 * Helper class that changes settings.
 * <p/>
 * Methods here directly correspond to something available in the UI.
 * Currently the different cases are:
 * <ul>
 * <li> Ringer: normal, silent..
 * <li> Ringer Vibrate: on, off.
 * <li> Ringer volume: percent.
 * <li> Wifi: on/off.
 * <li> Brightness: percent (disabled due to API)
 * </ul>
 */
public class SettingsHelper {

    private static final String TAG = "Tmrfc-Settings";
    private static final boolean DEBUG = true;

    private final Context mContext;

    public boolean canControlWifi() {
        return true;
    }

    public boolean canControlBrigthness() {
        return true;
    }

    public boolean canControlAirplaneMode() {
        return true;
    }

    public enum RingerMode {
        /** Normal ringer: actually rings. */
        RING,
        /** Muted ringed. */
        MUTE;

        /** Capitalizes the string */
        @Override
        public String toString() {
            return (this == RING) ? "Ring" : "Mute";
        }
    }

    public enum VibrateRingerMode {
        /** Vibrate is on */
        VIBRATE,
        /** Vibrate is off */
        NO_VIBRATE;

        /** Capitalizes the string */
        @Override
        public String toString() {
            return (this == VIBRATE) ? "Vibrate" : "No vibrate";
        }
    }

    public SettingsHelper(Context context) {
        mContext = context;
    }

    // --- ringer: vibrate & volume ---

    public void changeRingerVibrate(RingerMode ringer, VibrateRingerMode vib) {
        AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        if (manager == null) {
            Log.w(TAG, "changeRingerMode: AUDIO_SERVICE missing!");
            return;
        }

        if (DEBUG) Log.d(TAG, "changeRingerMode: " + ringer.toString());
        if (DEBUG) Log.d(TAG, String.format("changeRingerVibrate: %s + %s",
                        ringer != null ? ringer.toString() : "ringer-null",
                        vib != null ? vib.toString() : "vib-null"));

        if (vib != null) {
            switch(vib) {
                case VIBRATE:
                    manager.setVibrateSetting(
                            AudioManager.VIBRATE_TYPE_RINGER,
                            AudioManager.VIBRATE_SETTING_ON);
                    break;
                case NO_VIBRATE:
                    manager.setVibrateSetting(
                            AudioManager.VIBRATE_TYPE_RINGER,
                            AudioManager.VIBRATE_SETTING_OFF);
                    break;
            }
        }

        if (ringer != null) {
            switch (ringer) {
                case RING:
                    // normal may or may not vibrate, cf setting above
                    manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    break;
                case MUTE:
                    if (vib != null && vib == VibrateRingerMode.VIBRATE) {
                        manager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    } else {
                        // this turns of the vibrate, which unfortunately doesn't respect
                        // the case where vibrate should not be changed when going silent.
                        // TODO read the system pref for the default "vibrate" mode and use
                        // when vib==null.
                        manager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    }
                    break;
            }
        }
    }

    public void changeRingerVolume(int percent) {
        AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        if (manager == null) {
            Log.w(TAG, "changeRingerVolume: AUDIO_SERVICE missing!");
            return;
        }

        if (DEBUG) Log.d(TAG, "changeRingerVolume: " + Integer.toString(percent));

        int max = manager.getStreamMaxVolume(AudioManager.STREAM_RING);
        int vol = (max * percent) / 100;
        manager.setStreamVolume(AudioManager.STREAM_RING, vol, 0 /*flags*/);
    }

    public int getRingerVolume() {
        AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        if (manager == null) {
            if (DEBUG) Log.d(TAG, "getRingerVolume: AUDIO_SERVICE missing!");
            return 50;
        }

        int vol = manager.getStreamVolume(AudioManager.STREAM_RING);
        int max = manager.getStreamMaxVolume(AudioManager.STREAM_RING);

        return (vol * 100 / max);
    }

    // --- global brightness --

    /**
     * @param percent The new value in 0..100 range (will get mapped to adequate OS values)
     * @param persistent True if the setting should be made persistent, e.g. written to system pref.
     *  If false, only the current hardware value is changed.
     */
    public void changeBrightness(int percent, boolean persistent) {
        if (canControlBrigthness()) {
            // Reference:
            // http://android.git.kernel.org/?p=platform/packages/apps/Settings.git;a=blob;f=src/com/android/settings/BrightnessPreference.java
            // The source indicates
            // - Backlight range is 0..255
            // - Must not set to 0 (user would see nothing) so they use 10 as minimum
            // - All constants are in android.os.Power which is hidden from the SDK in 1.0
            //   yet available in 1.1
            // - To get value: Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            // - To set value: Settings.System.putInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, v);

            Log.d(TAG, "changeBrightness: " + Integer.toString(percent));

            Intent i = new Intent(mContext, ChangeBrightnessActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra(ChangeBrightnessActivity.INTENT_SET_BRIGHTNESS, percent / 100.0f);
            mContext.startActivity(i);
        }
    }

    /**
     * Returns screen brightness in range 0..100%.
     * <p/>
     * See comments in {@link #changeBrightness(int)}. The real range is 0..255,
     * maps it 0..100.
     */
    public int getCurrentBrightness() {
        return (int) (100 * ChangeBrightnessActivity.getCurrentBrightness(mContext));
    }

    // --- wifi ---

    public void changeWifi(boolean enabled) {
        // This requires two permissions:
        //     android.permission.ACCESS_WIFI_STATE
        // and android.permission.CHANGE_WIFI_STATE

        if (canControlWifi()) {
            WifiManager manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

            if (manager == null) {
                if (DEBUG) Log.d(TAG, "changeWifi: WIFI_SERVICE missing!");
                return;
            }

            if (DEBUG) Log.d(TAG, "changeWifi: " + (enabled ? "on" : "off"));

            manager.setWifiEnabled(enabled);
        }
    }

    // --- airplane mode ---

    /** Changes the airplane mode */
    public void changeAirplaneMode(boolean turnOn) {
        // Reference: settings source is in the cupcake gitweb tree at
        //   packages/apps/Settings/src/com/android/settings/AirplaneModeEnabler.java
        // http://android.git.kernel.org/?p=platform/packages/apps/Settings.git;a=blob;f=src/com/android/settings/AirplaneModeEnabler.java;h=f105712260fd7b2d7804460dd180d1d6cea01afa;hb=HEAD

        if (canControlAirplaneMode()) {
            // Change the system setting
            Settings.System.putInt(
                            mContext.getContentResolver(),
                            Settings.System.AIRPLANE_MODE_ON,
                            turnOn ? 1 : 0);

            // Post the intent
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", turnOn);
            mContext.sendBroadcast(intent);

            if (DEBUG) Log.d(TAG, "changeAirplaneMode: " + (turnOn ? "on" : "off"));
        }
    }
}
