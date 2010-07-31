/*
 * (c) ralfoide gmail com, 2009
 * Project: TimerifficTest
 * License GPLv3
 */

/**
 *
 */
package com.alfray.timeriffic.test.unit;

import java.util.Locale;

import junit.framework.TestCase;

import com.alfray.timeriffic.TAU;

public class TAUT extends TestCase {

    private static class MockTAU extends TAU {

        public String[] _getDaysNames() {
            TAU.sDaysNames = null;
            return TAU.getDaysNames();
        }

    }

    private MockTAU m;
    private Locale mDefaultLocale;

    @Override
    protected void setUp() throws Exception {
        mDefaultLocale = Locale.getDefault();
        m = new MockTAU();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        Locale.setDefault(mDefaultLocale);
        m = null;
        super.tearDown();
    }

    public void testInitDaysNames_en() throws Exception {

        Locale.setDefault(new Locale("en"));

        String[] days = m._getDaysNames();
        assertNotNull(days);
        assertEquals(7, days.length);

        String[] expected = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };

        for (int i = 0; i < 7; i++) {
            assertEquals(expected[i], days[i]);
        }
    }

    public void testInitDaysNames_fr() throws Exception {

        Locale.setDefault(new Locale("fr"));

        String[] days = m._getDaysNames();
        assertNotNull(days);
        assertEquals(7, days.length);

        String[] expected = { "lun.", "mar.", "mer.", "jeu.", "ven.", "sam.", "dim." };

        for (int i = 0; i < 7; i++) {
            assertEquals(expected[i], days[i]);
        }
    }
}
