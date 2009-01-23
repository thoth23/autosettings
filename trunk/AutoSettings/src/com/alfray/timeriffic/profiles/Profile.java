/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License TBD
 */

package com.alfray.timeriffic.profiles;

import java.util.ArrayList;

public class Profile {
    
    private String mName;
    private ArrayList<TimedAction> mActions;
    private long mId;
    private boolean mEnabled;

    private static Object sLock = new Object();
    private static long sNextId = 0;

    public Profile() {
        synchronized (sLock) {
            mId = sNextId;
            sNextId += 2;
        }
        
        mEnabled = true;
        mName = "Test";
        mActions = new ArrayList<TimedAction>();
    }
    
    public boolean isEnabled() {
        return mEnabled;
    }
    
    public long getId() {
        return mId;
    }
    
    public String getName() {
        return mName;
    }

    public ArrayList<TimedAction> getActions() {
        return mActions;
    }
}
