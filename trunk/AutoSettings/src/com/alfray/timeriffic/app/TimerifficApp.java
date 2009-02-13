/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License: GPLv3
 */

/**
 * 
 */
package com.alfray.timeriffic.app;

import android.app.Application;


public class TimerifficApp extends Application {
    private boolean mIntroDisplayed;
    private Runnable mDataListener;

    @Override
    public void onCreate() {
        super.onCreate();
    }
    
    public boolean isIntroDisplayed() {
        return mIntroDisplayed;
    }
    
    public void setIntroDisplayed(boolean introDisplayed) {
        mIntroDisplayed = introDisplayed;
    }
    
    //---------------------
    
    public void setDataListener(Runnable listener) {
        mDataListener = listener;
    }
    
    public void invokeDataListener() {
        if (mDataListener != null) mDataListener.run();
    }
}
