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

package com.alfray.timeriffic;

import android.provider.BaseColumns;

//-----------------------------------------------

public class C implements BaseColumns {

    public static final String T = "t";
    public static final int TiP = 1;
    public static final int TiTA = 2;
    public static final String Dsc = "d";
    public static final String EN = "e";
    public static final int P1 = 0;
    public static final int P0 = 1;
    public static final int AMD = 0;
    public static final int AMkP = 1;
    public static final int AMkN = 2;
    public static final String PID = "p";
    public static final int PS = 16;
    public static final int AMk = (1<<PS)-1;
    public static final int PG = 256;
    public static final int TAG = 256;
    public static final String HM = "h";
    public static final String D = "d";
    public static final int MOi = 0;
    public static final int SUi = 6;
    public static final int MO    = 1 << MOi;
    public static final int TU   = 1 << 1;
    public static final int WE = 1 << 2;
    public static final int TH  = 1 << 3;
    public static final int FR    = 1 << 4;
    public static final int SA  = 1 << 5;
    public static final int SU    = 1 << SUi;
    public static final String A = "a";
    public static final char AR      = 'R';
    public static final char AV     = 'V';
    public static final char ARV = 'G';
    public static final char ANV = 'N';
    public static final char AMV = 'M';
    public static final char AAV = 'L';
    public static final char ABR  = 'B';
    public static final char AW        = 'W';
    public static final char AA    = 'A';
    public static final char ABT   = 'U';
    public static final char AAD   = 'D';
    public static final String NMS = "n";
    public static final String DSO = PID + " ASC";
}
