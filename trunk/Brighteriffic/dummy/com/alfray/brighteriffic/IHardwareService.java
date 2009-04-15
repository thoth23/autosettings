/*
 * (c) ralfoide gmail com, 2009
 * Project: Brighteriffic
 * License TBD
 */

/**
 *
 */
package com.alfray.brighteriffic;

import android.os.IBinder;

/**
 * @author ralf
 *
 */
public interface IHardwareService {

    IHardwareService Stub = null;

    IHardwareService asInterface(IBinder service);

    void setScreenBacklight(int v);

}
