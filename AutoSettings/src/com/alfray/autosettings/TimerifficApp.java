/*
 * (c) ralfoide gmail com, 2009
 * Project: AutoSettings
 * License TBD
 */

/**
 * 
 */
package com.alfray.autosettings;

import android.app.Application;


public class TimerifficApp extends Application {
    private boolean mIntroDisplayed;

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
}
