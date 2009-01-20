/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License: GPLv3
 */

package com.alfray.timeriffic;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UtilsTest {

    private Calendar mCal;
    private SimpleDateFormat mSf;

    @Before
    public void setUp() throws Exception {
        mCal = Calendar.getInstance();
        mSf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    }

    @After
    public void tearDown() throws Exception {
    }

    private long timeMs(String time) throws ParseException {
        return mSf.parse(time).getTime();
    }

    // ----
    
    @Test
    public void testFormatTime_Now() throws ParseException {

        long ref = timeMs("2010.01.01 01:01:01");
        
        assertEquals("now",
                Utils.formatTime(mCal, ref, ref /*now*/));
    }

    @Test
    public void testFormatTime_Sec() throws ParseException {

        long ref = timeMs("2010.01.01 01:01:01");
        
        assertEquals("1 sec. ago",
                Utils.formatTime(mCal,
                        ref,
                        timeMs("2010.01.01 01:01:02")));
        
        assertEquals("59 sec. ago",
                Utils.formatTime(mCal,
                        ref,
                        timeMs("2010.01.01 01:02:00")));
        
        assertEquals("1 min. ago",  // not 60 seconds ago
                Utils.formatTime(mCal,
                        ref,
                        timeMs("2010.01.01 01:02:01")));
        
        assertEquals("1 sec. later",
                Utils.formatTime(mCal,
                        ref,
                        timeMs("2010.01.01 01:01:00")));
    }

    @Test
    public void testFormatTime_Min() throws ParseException {

        long ref = timeMs("2010.01.01 01:01:01");
        
        assertEquals("1 min. ago",
                Utils.formatTime(mCal,
                        ref,
                        timeMs("2010.01.01 01:02:01")));
        
        assertEquals("59 min. ago",
                Utils.formatTime(mCal,
                        ref,
                        timeMs("2010.01.01 02:00:01")));
        
        assertEquals("1 hour ago",  // not 60 minutes ago
                Utils.formatTime(mCal,
                        ref,
                        timeMs("2010.01.01 02:01:01")));
        
        assertEquals("1 min. later",
                Utils.formatTime(mCal,
                        ref,
                        timeMs("2010.01.01 01:00:01")));
    }

    @Test
    public void testFormatTime_Hour() throws ParseException {

        long ref = timeMs("2010.02.02 01:01:01");
        
        assertEquals("1 hour ago",
                Utils.formatTime(mCal,
                        ref,
                        timeMs("2010.02.02 02:01:01")));
        
        assertEquals("23 hours ago",
                Utils.formatTime(mCal,
                        ref,
                        timeMs("2010.02.03 01:01:00")));
        
        assertEquals("1 day ago",  // not 24 hours ago
                Utils.formatTime(mCal,
                        ref,
                        timeMs("2010.02.03 01:01:01")));
        
        assertEquals("1 day later",
                Utils.formatTime(mCal,
                        ref,
                        timeMs("2010.02.01 01:01:01")));
    }

    @Test
    public void testFormatTime_Day() throws ParseException {

        long ref = timeMs("2010.02.02 01:01:01");
        
        assertEquals("1 day ago",
                Utils.formatTime(mCal,
                        ref,
                        timeMs("2010.02.03 01:01:01")));
        
        assertEquals("1 day later",
                Utils.formatTime(mCal,
                        ref,
                        timeMs("2010.02.01 01:01:01")));

        assertEquals("2 days ago",
                Utils.formatTime(mCal,
                        ref,
                        timeMs("2010.02.04 01:01:01")));

        assertEquals("7 days ago",
                Utils.formatTime(mCal,
                        ref,
                        timeMs("2010.02.09 01:01:01")));

        assertEquals("Feb 2",  // not 8 days ago
                Utils.formatTime(mCal,
                        ref,
                        timeMs("2010.02.10 01:01:01")));

        assertEquals("Feb 2",
                Utils.formatTime(mCal,
                        ref,
                        timeMs("2010.03.02 01:01:01")));

        assertEquals("2010-02-02",  // not 8 days ago
                Utils.formatTime(mCal,
                        ref,
                        timeMs("2040.04.02 01:01:01")));

        assertEquals("2010-02-02",
                Utils.formatTime(mCal,
                        ref,
                        timeMs("1971.04.02 01:01:01")));
    }

}
