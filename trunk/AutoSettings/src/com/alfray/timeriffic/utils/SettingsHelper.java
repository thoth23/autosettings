/*
 * Project: Timeriffic
 * Copyright (C) 2008 ralfoide gmail com,
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

package com.alfray.timeriffic.utils;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import com.alfray.timeriffic.R;

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

    private static final String TAG = "Timerfc-Settings";
    private static final boolean DEBUG = true;

    private final Context mContext;

    public SettingsHelper(Context context) {
        mContext = context;
    }

    public boolean canControlWifi() {
        WifiManager manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        return manager != null;
    }

    public boolean canControlBrigthness() {
        return true;
    }

    public boolean canControlAirplaneMode() {
        return true;
    }

    public boolean canControlAudio() {
        AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        return manager != null;
    }

    public boolean canControlNotificationVolume() {
        AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (manager == null) return false;

        return checkMinApiLevel(3);
    }

    public boolean canControlBluetooth() {
        AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (manager == null) return false;

        if (!checkMinApiLevel(5)) return false;

        // Is a bluetooth adapter actually available?
        try {
            Class<?> btaClass = Class.forName("android.bluetooth.BluetoothAdapter");

            Method getter = btaClass.getMethod("getDefaultAdapter");
            Object result = getter.invoke(null);
            return result != null;

        } catch (Exception e) {
            if (DEBUG) Log.d(TAG, "Missing BTA API", e);
        }

        return false;
    }

    private boolean checkMinApiLevel(int minApiLevel) {
        // Build.SDK_INT is only in API 4 and we're still compatible with API 3
        try {
            int n = Integer.parseInt(Build.VERSION.SDK);
            return n >= 3;
        } catch (Exception e) {
            Log.d(TAG, "Failed to parse Build.VERSION.SDK=" + Build.VERSION.SDK, e);
        }
        return false;
    }

    public enum RingerMode {
        /** Normal ringer: actually rings. */
        RING,
        /** Muted ringed. */
        MUTE;

        public char getActionLetter() {
            return (this == RING) ? 'R' : 'M';
        }

        /** Capitalizes the string */
        public String toUiString(Context context) {
            return (this == RING) ?
                context.getString(R.string.ringermode_ring) :
                context.getString(R.string.ringermode_mute);
        }
    }

    public enum VibrateRingerMode {
        /** Vibrate is on (Ringer & Notification) */
        VIBRATE,
        /** Vibrate is off, both ringer & notif */
        NO_VIBRATE_ALL,
        /** Ringer vibrate is off but notif is on */
        NO_RINGER_VIBRATE;

        public char getActionLetter() {
            if (this == NO_VIBRATE_ALL) return 'N';
            if (this == NO_RINGER_VIBRATE) return 'R';
            assert this == VIBRATE;
            return 'V';
        }

        /** Capitalizes the string */
        public String toUiString(Context context) {
            if (this == NO_VIBRATE_ALL) {
                return context.getString(R.string.vibrateringermode_no_vibrate);
            }
            if (this == NO_RINGER_VIBRATE) {
                return context.getString(R.string.vibrateringermode_no_ringer_vibrate);
            }
            assert this == VIBRATE;
            return context.getString(R.string.vibrateringermode_vibrate);
        }
    }

    // --- ringer: vibrate & volume ---

    public void changeRingerVibrate(RingerMode ringer, VibrateRingerMode vib) {
        AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        if (manager == null) {
            Log.w(TAG, "changeRingerMode: AUDIO_SERVICE missing!");
            return;
        }

        if (DEBUG) Log.d(TAG, String.format("changeRingerVibrate: %s + %s",
                        ringer != null ? ringer.toString() : "ringer-null",
                        vib != null ? vib.toString() : "vib-null"));

        if (vib != null) {
            switch(vib) {
                case VIBRATE:
                    // set both ringer & notification vibrate modes to on
                    manager.setVibrateSetting(
                            AudioManager.VIBRATE_TYPE_RINGER,
                            AudioManager.VIBRATE_SETTING_ON);
                    manager.setVibrateSetting(
                            AudioManager.VIBRATE_TYPE_NOTIFICATION,
                            AudioManager.VIBRATE_SETTING_ON);
                    break;
                case NO_VIBRATE_ALL:
                    // set both ringer & notification vibrate modes to off
                    manager.setVibrateSetting(
                            AudioManager.VIBRATE_TYPE_RINGER,
                            AudioManager.VIBRATE_SETTING_OFF);
                    manager.setVibrateSetting(
                            AudioManager.VIBRATE_TYPE_NOTIFICATION,
                            AudioManager.VIBRATE_SETTING_OFF);
                    break;
                case NO_RINGER_VIBRATE:
                    // ringer vibrate off, notification vibrate on
                    manager.setVibrateSetting(
                            AudioManager.VIBRATE_TYPE_RINGER,
                            AudioManager.VIBRATE_SETTING_OFF);
                    manager.setVibrateSetting(
                            AudioManager.VIBRATE_TYPE_NOTIFICATION,
                            AudioManager.VIBRATE_SETTING_ON);
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

    public void changeNotificationVolume(int percent) {
        AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        if (manager == null) {
            Log.w(TAG, "changeNotificationVolume: AUDIO_SERVICE missing!");
            return;
        } else if (!canControlNotificationVolume()) {
            if (DEBUG) Log.w(TAG, "changeNotificationVolume: API too low.");
            changeRingerVolume(percent);
            return;
        }

        if (DEBUG) Log.d(TAG, "changeNotificationVolume: " + Integer.toString(percent));

        int max = manager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        int vol = (max * percent) / 100;
        manager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, vol, 0 /*flags*/);
    }

    public int getNotificationVolume() {
        AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        if (manager == null) {
            if (DEBUG) Log.d(TAG, "getNotificationVolume: AUDIO_SERVICE missing!");
            return 50;
        } else if (!canControlNotificationVolume()) {
            if (DEBUG) Log.w(TAG, "changeNotificationVolume: API too low.");
            return getRingerVolume();
        }

        int vol = manager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        int max = manager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);

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

    // --- bluetooh ---

    public void changeBluetooh(boolean enabled) {
        // This requires permission android.permission.BLUETOOTH_ADMIN

        if (canControlBluetooth()) {
            try {
                Class<?> btaClass = Class.forName("android.bluetooth.BluetoothAdapter");

                Method getter = btaClass.getMethod("getDefaultAdapter");
                Object bt = getter.invoke(null);

                if (bt == null) {
                    if (DEBUG) Log.d(TAG, "changeBluetooh: BluetoothAdapter null!");
                    return;
                }

                if (DEBUG) Log.d(TAG, "changeBluetooh: " + (enabled ? "on" : "off"));

                if (enabled) {
                    bt.getClass().getMethod("enable").invoke(bt);
                } else {
                    bt.getClass().getMethod("disable").invoke(bt);
                }

            } catch (Exception e) {
                if (DEBUG) Log.d(TAG, "Missing BTA API", e);
            }

        }
    }
}
