/*
 * Project: Brighteriffic
 * Copyright (C) 2011 ralfoide gmail com,
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

package com.alfray.brighteriffic;

import android.os.Build;
import android.os.Build.VERSION;

public class Utils {

    private static int sApiLevel = 0;

    /**
     * Return {@link VERSION#SDK} as an int.
     * Value is cached locally.
     */
    public static int getApiLevel() {
        if (sApiLevel == 0) {
            try {
                // SDK_INT only exists since API 4 so let's use the string version.
                sApiLevel = Integer.parseInt(Build.VERSION.SDK);
            } catch (Exception e) {
                // This app doesn't run below 3 anyway
                sApiLevel = 3;
            }
        }

        return sApiLevel;
    }

}
