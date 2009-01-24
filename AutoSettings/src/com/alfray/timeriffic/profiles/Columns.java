/*
 * Copyright 2008 (c) ralfoide gmail com, 2008
 * Project: timeriffic
 * License: GPL version 3 or any later version
 */

package com.alfray.timeriffic.profiles;

import android.provider.BaseColumns;

//-----------------------------------------------

/**
 * Column names for the profiles/timed_actions table.
 */
public class Columns implements BaseColumns {

    /** The default sort order for this table, _ID ASC */
    public static final String DEFAULT_SORT_ORDER = _ID + " ASC";

    /** The type of this row.
     * Enum: {@link #TYPE_IS_PROFILE} or {@link #TYPE_IS_TIMED_ACTION}.
     * <p/>
     * Note: 0 is not a valid value. Makes it easy to identify a non-initialized row.
     * <p/>
     * Type: INTEGER
     */
    public static final String TYPE = "type";

    /** type TYPE_IS_PROFILE: the row is a profile definition */
    public static final int TYPE_IS_PROFILE = 1;
    /** type TYPE_IS_TIMED_ACTION: the row is a timed action definition */
    public static final int TYPE_IS_TIMED_ACTION = 2;

    // --- fields common to both a profile definition and a timed action
    
    /** Description:
     * - Profile: user title.
     * - Time action: pre-computed summary description.
     * <p/>
     * Type: TEXT
     */
    public static final String DESCRIPTION = "descrip";

    /** Is Enabled:
     * - Profile: user-selected enabled toggle.
     * - Timed action: is executing (pre-computed value).
     * <p/>
     * Type: INTEGER (boolean) 0 or 1 
     */
    public static final String IS_ENABLED = "enable";

    // --- fields for a profile definition

    
    
    // --- fields for a timed action
    
    /** Profile ID. Type: INTEGER */
    public static final String PROFILE_ID = "prof_id";

    /** Hour-Min Time, computed as hour*60+min in a day (from 0 to 23*60+59)
     * <p/>
     * Type: INTEGER
     */
    public static final String HOUR_MIN = "hourMin";

    /** Day(s) of the week.
     * This is a bitfield: {@link #MONDAY} thru {@link #SUNDAY}.
     * <p/>
     * Type: INTEGER
     */
    public static final String DAYS = "days";

    public static final int MONDAY    =  1;
    public static final int TUESDAY   =  2;
    public static final int WEDNESDAY =  4;
    public static final int THURSDAY  =  8;
    public static final int FRIDAY    = 16;
    public static final int SATURDAY  = 32;
    public static final int SUNDAY    = 64;

    /** Actions to execute.
     * This is an encoded string:
     * - action letter
     * - digits for parameter (optional)
     * - comma (except for last).
     * Example: "M0,V1,R50"
     * <p/>
     * Type: STRING
     */
    public static final String ACTIONS = "actions";

    /** Mute. Boolean: 0=Unmute, 1=Mute */
    public static final String ACTION_MUTE        = "M";
    /** Vibrate. Boolean: 0=No vib, 1=Vib */
    public static final String ACTION_VIBRATE     = "V";
    /** Rig volume. Integer: 0..99 */
    public static final String ACTION_RING_VOLUME = "R";

    /**
     * The precomputed System.currentTimeMillis timestamp of the next event for this action.
     * Type: INTEGER (long)
     */
    public static final String NEXT_MS = "next_ms";
}
