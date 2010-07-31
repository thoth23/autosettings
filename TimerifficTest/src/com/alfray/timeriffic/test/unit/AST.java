/*
 * (c) ralfoide gmail com, 2009
 * Project: TimerifficTest
 * License GPLv3
 */

/**
 *
 */
package com.alfray.timeriffic.test.unit;

import android.content.Context;
import android.test.AndroidTestCase;

import com.alfray.timeriffic.AS;
import com.alfray.timeriffic.PV;

public class AST extends AndroidTestCase {

    private static class MockAS extends AS {

        private final PV _mPrefs;

        public MockAS(Context context, PV prefs) {
            super(context, prefs);
            _mPrefs = prefs;
        }

        public static final String _SEP_START = SEP_START;
        public static final String _SEP_END = SEP_END;

        public String _addToDebugLog(String time, String logActions) {
            super.addToDebugLog(time, logActions);
            return _mPrefs.getLastActions();
        }
    }

    private MockAS m;
    private PV mPrefs;

    @Override
    protected void setUp() throws Exception {
        mPrefs = new PV(getContext());
        // make sure to clear the strings we'll be testing
        mPrefs.setLastActions(null);

        m = new MockAS(getContext(), mPrefs);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        m = null;
        super.tearDown();
    }

    public void testAddToDebugLog_set() throws Exception {
        String time = "12:34";
        String actions = "A0,B12,31145";
        String expected = time + MockAS._SEP_START +
                          actions + MockAS._SEP_END;

        String result = m._addToDebugLog(time, actions);

        assertEquals(expected, result);
    }

    public void testAddToDebugLog_4096() throws Exception {
        String time = "12:34";
        String actions = "1234567890";
        actions = actions + actions + actions + actions + actions + actions + actions + actions;
        actions = actions + actions + actions + actions + actions + actions + actions + actions;
        String expected = time + MockAS._SEP_START +
                          actions + MockAS._SEP_END;
        int len = expected.length();
        int limit = 4096;

        // The buffer limit is 4096.
        // Add just enough strings to be right under the buffer size.

        int count = 0;
        while (count + len < limit) {
            String result = m._addToDebugLog(time, actions);
            assertTrue(result.length() > count);
            count = result.length();
        }
        assertTrue(count < limit);

        // add one more
        String result = m._addToDebugLog(time, actions);
        assertTrue(result.length() < limit);
        assertTrue(result.startsWith(expected));

        // now add a string larger than 4096
        String time2 = "45:67";
        String actions2 = actions;
        while (actions2.length() < limit) {
            actions2 += actions;
        }
        String expected2 = time2 + MockAS._SEP_START +
                           actions2 + MockAS._SEP_END;

        assertFalse(result.startsWith(expected2));

        result = m._addToDebugLog(time2, actions2);

        assertTrue(result.length() > limit);
        assertEquals(expected2, result);

        // and add the shorter string again... the excess one should go away
        result = m._addToDebugLog(time, actions);
        assertEquals(expected, result);
    }
}
