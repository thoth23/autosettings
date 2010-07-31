/*
 * Project: NerdkillAndroid
 * Copyright (C) 2010 ralfoide gmail com,
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

package com.alfray.timeriffic.test.functional;

import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.alfray.timeriffic.ERUI;
import com.alfray.timeriffic.IA;
import com.alfray.timeriffic.PA;
import com.alfray.timeriffic.PUI;
import com.alfray.timeriffic.PV;
import com.alfray.timeriffic.R;
import com.alfray.timeriffic.TA;

//-----------------------------------------------

public class PUIT extends ActivityInstrumentationTestCase2<PUI> {

    private static final String TAG = "PUIT";

    private TA mApplication;
    private PV mPV;

    public PUIT() {
        super(PUI.class.getPackage().getName(), PUI.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sa(false /* show auto-intro */);
    }

    @Override
    protected void tearDown() throws Exception {

        if (mApplication != null) {
            mApplication.onTerminate();
            mApplication = null;
        }

        super.tearDown();
    }

    private TA sa(boolean showAutoIntro) {

        if (mApplication != null) {
            Log.d(TAG, String.format("Reuse application: %s=%s",
                    mApplication.getClass().getSimpleName(),
                    mApplication.toString()));
        }

        if (mApplication == null) {
            Context ac = getInstrumentation().getTargetContext().getApplicationContext();

            if (ac instanceof TA) {
                mApplication = (TA) ac;
                Log.d(TAG, String.format("Reuse context application: %s=%s",
                        mApplication.getClass().getSimpleName(),
                        mApplication.toString()));
            }
        }


        try {
            if (mApplication == null) {
                mApplication = (TA) Instrumentation.newApplication(
                        TA.class,
                                getInstrumentation().getTargetContext());
                Log.d(TAG, String.format("Start application: %s=%s",
                        mApplication.getClass().getSimpleName(),
                        mApplication.toString()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to create Application", e);
        }

        assertNotNull(mApplication);

        // always reset is-first-start
        mApplication.setFirstStart(true);

        // disable auto-start of IA, activated as needed below.
        mPV = new PV(mApplication);
        mPV.setIntroDismissed(!showAutoIntro);

        return mApplication;
    }

    // ------------

    public void tc_noi() throws Exception {

        ActivityMonitor monitor = new ActivityMonitor(
                IA.class.getName(),
                null, // result
                true); // block
        getInstrumentation().addMonitor(monitor);
        assertEquals(0, monitor.getHits());

        PUI a = getActivity();
        assertNotNull(a);

        getInstrumentation().waitForIdleSync();
        getInstrumentation().waitForMonitorWithTimeout(monitor, 1000);
        assertEquals(0, monitor.getHits());
    }

    /** Starts the activity from cold, with auto-intro. */
    public void tc_wi() throws Exception {

        sa(true /* show auto-intro */);

        ActivityMonitor monitor = new ActivityMonitor(
                IA.class.getName(),
                null, // result
                true); // block
        getInstrumentation().addMonitor(monitor);
        assertEquals(0, monitor.getHits());

        PUI a = getActivity();
        assertNotNull(a);

        getInstrumentation().waitForIdleSync();
        getInstrumentation().waitForMonitorWithTimeout(monitor, 1000);
        assertEquals(1, monitor.getHits());
    }

    /** Starting the application changes its isFirstStart flag. */
    public void testIntro_IsFirstStart() throws Exception {

        TA app = sa(false);
        app.setFirstStart(true);

        // onCreate will check app.isFirstStart and set it to false
        PUI a = getActivity();
        assertNotNull(a);

        assertTrue(app.isFirstStart());
    }

    /** Menu>About/Help starts the IA. */
    public void testMenu_Help() throws Exception {
        PUI a = getActivity();
        assertNotNull(a);

        ActivityMonitor monitor = new ActivityMonitor(
                        IA.class.getName(),
                        null, // result
                        true); // block
        getInstrumentation().addMonitor(monitor);
        assertEquals(0, monitor.getHits());

        boolean ok = getInstrumentation().invokeMenuActionSync(a, R.string.about, 0);
        assertTrue(ok);
        getInstrumentation().waitForMonitorWithTimeout(monitor, 1000);

        assertEquals(1, monitor.getHits());
    }

    /** Menu>Settings starts the PrefsActivity. */
    public void testMenu_Settings() throws Exception {
        PUI a = getActivity();
        assertNotNull(a);

        ActivityMonitor monitor = new ActivityMonitor(
                        PA.class.getName(),
                        null, // result
                        true); // block
        getInstrumentation().addMonitor(monitor);
        assertEquals(0, monitor.getHits());

        boolean ok = getInstrumentation().invokeMenuActionSync(a, R.string.settings, 0);
        assertTrue(ok);
        getInstrumentation().waitForMonitorWithTimeout(monitor, 1000);
        assertEquals(1, monitor.getHits());
    }

    /** Menu>About starts the IA. */
    public void testMenu_ShowIntro() throws Exception {
        ActivityMonitor monitor = new ActivityMonitor(
                IA.class.getName(),
                        null, // result
                        true); // block
        getInstrumentation().addMonitor(monitor);
        assertEquals(0, monitor.getHits());

        getInstrumentation().addMonitor(monitor);

        PUI a = getActivity();
        assertNotNull(a);

        boolean ok = getInstrumentation().invokeMenuActionSync(a, R.string.about, 0);
        assertTrue(ok);
        getInstrumentation().waitForIdleSync();
        getInstrumentation().waitForMonitorWithTimeout(monitor, 1000);
        assertEquals(1, monitor.getHits());
    }

    /** Menu>ErroReport starts the ErrorReportActivity. */
    public void testMenu_ErrorReport() throws Exception {
        ActivityMonitor monitor = new ActivityMonitor(
                        ERUI.class.getName(),
                        null, // result
                        true); // block
        getInstrumentation().addMonitor(monitor);
        assertEquals(0, monitor.getHits());

        getInstrumentation().addMonitor(monitor);

        PUI a = getActivity();
        assertNotNull(a);

        boolean ok = getInstrumentation().invokeMenuActionSync(a, R.string.report_error, 0);
        assertTrue(ok);
        getInstrumentation().waitForIdleSync();
        getInstrumentation().waitForMonitorWithTimeout(monitor, 1000);
        assertEquals(1, monitor.getHits());
    }
}


