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
    private boolean mFirstStart = true;
    private Runnable mDataListener;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public boolean isFirstStart() {
        return mFirstStart;
    }

    public void setFirstStart(boolean firstStart) {
        mFirstStart = firstStart;
    }

    //---------------------

    public void setDataListener(Runnable listener) {
        mDataListener = listener;
    }

    public void invokeDataListener() {
        if (mDataListener != null) mDataListener.run();
    }
}
