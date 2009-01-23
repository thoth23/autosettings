/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License TBD
 */

package com.alfray.timeriffic.profiles;

import com.alfray.timeriffic.prefs.TimePreference;

public class TimedAction {

    private int mTime;
    private String mActionsName;
    private String mDaysName;
    private final Profile mProfile;
    private long mId;
    
    private static Object sLock = new Object();
    private static long sNextId = 1;

    public TimedAction(Profile profile) {
        synchronized (sLock) {
            mId = sNextId;
            sNextId += 2;
        }

        mProfile = profile;

        mTime = 7*60+30;
        mDaysName = "Mon-Sat";
        mActionsName = "Mute, Vibrate";
        
    }
    
    public Profile getProfile() {
        return mProfile;
    }
    
    public long getId() {
        return mId;
    }
    
    @Override
    public String toString() {
        return String.format("%s, %s, %s",
                TimePreference.toHourMinStr(mTime),
                mDaysName,
                mActionsName);
    }
    
}
