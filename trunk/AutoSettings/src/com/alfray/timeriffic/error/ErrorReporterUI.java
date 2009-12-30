/*
 * Project: Timeriffic
 * Copyright (C) 2009 ralfoide gmail com,
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

package com.alfray.timeriffic.error;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Locale;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.actions.EditActionUI;
import com.alfray.timeriffic.app.ApplySettings;
import com.alfray.timeriffic.app.AutoReceiver;
import com.alfray.timeriffic.app.IntroActivity;
import com.alfray.timeriffic.prefs.PrefsValues;
import com.alfray.timeriffic.profiles.EditProfileUI;
import com.alfray.timeriffic.profiles.ProfileHeaderHolder;
import com.alfray.timeriffic.profiles.ProfilesDB;
import com.alfray.timeriffic.profiles.ProfilesUI;
import com.alfray.timeriffic.profiles.TimedActionHolder;
import com.alfray.timeriffic.utils.AgentWrapper;
import com.alfray.timeriffic.utils.ChangeBrightnessActivity;
import com.alfray.timeriffic.utils.SettingsHelper;

/**
 * Screen to generate an error report.
 */
public class ErrorReporterUI extends ExceptionHandlerActivity {

    private static final boolean DEBUG = true;
    public static final String TAG = "TFC-ErrorUI";

    /**
     * Mailto address. %s is app name. Address is naively obfuscated
     * since this code will end up open sourced, to prevent address harvesting.
     */
    private static final String MAILTO = "and_roid - mar_ket + %s";
    /** domain part of mailto. */
    private static final String DOMTO = "al_fr_ay / c_om";
    /** Email subject. %s is app name. */
    private static final String SUBJECT = "%s Error Report";

    private static final int MSG_REPORT_COMPLETE = 1;

    private AgentWrapper mAgentWrapper;
    private Handler mHandler;
    private boolean mAbortReport;
    private String mAppName;
    private String mAppVersion;

    private class JSErrorInfo {

        private final int mNumExceptions;
        private final int mNumActions;

        public JSErrorInfo(int numExceptions, int numActions) {
            mNumExceptions = numExceptions;
            mNumActions = numActions;
        }

        @SuppressWarnings("unused")
        public int getNumExceptions() {
            return mNumExceptions;
        }

        @SuppressWarnings("unused")
        public int getNumActions() {
            return mNumActions;
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.error_reporter);
        setTitle(R.string.errorreport_title);

        PackageManager pm = getPackageManager();
        if (pm != null) {
            PackageInfo pi;
            try {
                pi = pm.getPackageInfo(getPackageName(), 0);

                mAppName = getPackageName();
                if (pi.applicationInfo != null) {
                    try {
                        mAppName = getString(pi.applicationInfo.labelRes);
                    } catch (Exception ignore) {
                        // getString typically returns NotFoundException. Ignore it.
                    }
                }
                mAppVersion = pi.versionName;
            } catch (Exception ignore) {
                // getPackageName typically throws NameNotFoundException
            }
        }

        final WebView wv = (WebView) findViewById(R.id.web);
        if (wv == null) {
            if (DEBUG) Log.d(TAG, "Missing web view");
            finish();
        }

        // Make the webview transparent (for background gradient)
        wv.setBackgroundColor(0x00000000);

        String file = selectFile("error_report");
        loadFile(wv, file);
        setupJavaScript(wv);
        setupProgressBar(wv);
        setupButtons();
        setupHandler();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAgentWrapper = new AgentWrapper();
        mAgentWrapper.start(this);
        mAgentWrapper.event(AgentWrapper.Event.OpenIntroUI);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // If the generator thread is still running, just set the abort
        // flag and let the thread terminate itself.
        mAbortReport = true;

        mAgentWrapper.stop(this);
    }

    private String selectFile(String baseName) {
        // Compute which file we want to display, i.e. try to select
        // one that matches baseName-LocaleCountryName.html or default
        // to intro.html
        String file = baseName + ".html";
        Locale lo = Locale.getDefault();
        String lang = lo.getLanguage();
        if (lang != null && lang.length() == 2) {
            InputStream is = null;
            String file2 = baseName + "-" + lang + ".html";
            try {
                AssetManager am = getResources().getAssets();

                is = am.open(file2);
                if (is != null) {
                    file = file2;
                }

            } catch (IOException e) {
                if (DEBUG) Log.d(TAG, "Asset not found: " + lang);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // pass
                    }
                }
            }
        }
        return file;
    }

    private void loadFile(final WebView wv, String file) {
        wv.loadUrl("file:///android_asset/" + file);
        wv.setFocusable(true);
        wv.setFocusableInTouchMode(true);
        wv.requestFocus();
    }

    private void setupJavaScript(final WebView wv) {

        // TODO get numbers
        int num_ex = ExceptionHandler.getNumExceptionsInLog(this);
        int num_act = ApplySettings.getNumActionsInLog(this);

        // Inject a JS method to set the version
        JSErrorInfo js = new JSErrorInfo(num_ex, num_act);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.addJavascriptInterface(js, "JSErrorInfo");
    }

    private void setupProgressBar(final WebView wv) {
        final ProgressBar progress = (ProgressBar) findViewById(R.id.progress);
        if (progress != null) {
            wv.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    progress.setProgress(newProgress);
                    progress.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
                }
            });
        }
    }

    private void setupButtons() {
        final Button gen = (Button) findViewById(R.id.generate);
        if (gen != null) {
            gen.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Start inderterminate progress bar
                    ProgressBar progress = (ProgressBar) findViewById(R.id.progress);
                    if (progress != null) {
                        progress.setVisibility(View.VISIBLE);
                        progress.setIndeterminate(true);
                    }

                    // Gray generate button (to avoid user repeasting it)
                    gen.setEnabled(false);

                    mAbortReport = false;

                    try {
                        Thread t = new Thread(new ReportGenerator(), "ReportGenerator");
                        t.start();
                    } catch (Throwable t) {
                        // We can possibly get a VerifyError from Dalvik here
                        // if the thread can't link (e.g. because it's using
                        // an unsupported API.). Normally we wouldn't care but
                        // we don't want the error reporter to crash itself.
                        Toast.makeText(ErrorReporterUI.this,
                                "Failed to generate report: " + t.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void setupHandler() {
        mHandler = new Handler(new Callback() {

            @Override
            public boolean handleMessage(Message msg) {

                if (msg.what == MSG_REPORT_COMPLETE) {

                    try {
                        // Get the report associated with the message
                        String report = (String) msg.obj;

                        // Stop inderterminate progress bar
                        ProgressBar progress = (ProgressBar) findViewById(R.id.progress);
                        if (progress != null) {
                            progress.setIndeterminate(false);
                            progress.setVisibility(View.GONE);
                        }

                        if (report != null) {

                            // Prepare mailto and subject.
                            String to = String.format(MAILTO, mAppName).trim();
                            to += "@";
                            to += DOMTO.replace("/", ".");
                            to = to.replaceAll("[ _]", "").toLowerCase();

                            String sub = String.format(SUBJECT, mAppName).trim();

                            // Generate the intent to send an email
                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.putExtra(Intent.EXTRA_EMAIL, new String[] { to });
                            i.putExtra(Intent.EXTRA_SUBJECT, sub);
                            i.putExtra(Intent.EXTRA_TEXT, report);
                            i.setType("message/rfc822");

                            try {
                                startActivity(i);
                            } catch (ActivityNotFoundException e) {
                                // This is likely to happen if there's no mail app.
                                Toast.makeText(getApplicationContext(),
                                        R.string.errorreport_nomailapp,
                                        Toast.LENGTH_LONG).show();
                                Log.d(TAG, "No email/gmail app found", e);
                            } catch (Exception e) {
                                // This is unlikely to happen.
                                Toast.makeText(getApplicationContext(),
                                        "Send email activity failed: " + e.toString(),
                                        Toast.LENGTH_LONG).show();
                                Log.d(TAG, "Send email activity failed", e);
                            }

                            // Finish this activity.
                            finish();
                        }
                    } finally {
                        // We're not supposed to get there since there's a finish
                        // above. So maybe something failed and we should let the
                        // user retry, so in any case, ungray generate button.

                        Button gen = (Button) findViewById(R.id.generate);
                        if (gen != null) {
                            gen.setEnabled(true);
                        }
                    }
                }
                return false;
            }
        });
    }

    /**
     * Generates the error report, with the following sections:
     * - Request user to enter some information (translated, rest is not)
     * - Device information (nothing user specific)
     * - Profile list summary
     * - Recent Exceptions
     * - Recent actions
     * - Recent logcat
     */
    private class ReportGenerator implements Runnable {
        @Override
        public void run() {

            Context c = ErrorReporterUI.this.getApplicationContext();
            PrefsValues pv = new PrefsValues(c);

            StringBuilder sb = new StringBuilder();

            addHeader(sb, c);
            addAppInfo(sb);
            addAndroidBuildInfo(sb);

            if (!mAbortReport) addProfiles(sb, c);
            if (!mAbortReport) addLastExceptions(sb, pv);
            if (!mAbortReport) addLastActions(sb, pv);
            if (!mAbortReport) addLogcat(sb);

            // -- Report complete

            // We're done with the report. Ping back the activity using
            // a message to get back to the UI thread.
            if (!mAbortReport) {
                Message msg = mHandler.obtainMessage(MSG_REPORT_COMPLETE, sb.toString());
                mHandler.sendMessage(msg);
            }
        }

        private void addHeader(StringBuilder sb, Context c) {
            try {
                String s = c.getString(R.string.errorreport_emailheader);
                sb.append(s.trim().replace('/', '\n')).append("\n");
            } catch (Exception ignore) {
            }
        }

        private void addAppInfo(StringBuilder sb) {
            sb.append(String.format("\n## App: %s %s\n",
                    mAppName,
                    mAppVersion));
        }

        private void addAndroidBuildInfo(StringBuilder sb) {

            sb.append("\n## Android Device ##\n\n");

            try {
                // Build.MANUFACTURER is only available starting at API 4
                String manufacturer = "--";
                try {
                    Field f = Build.class.getField("MANUFACTURER");
                    manufacturer = (String) f.get(null /*static*/);
                } catch (Exception ignore) {
                }

                sb.append(String.format("Device : %s (%s %s)\n",
                        Build.MODEL,
                        manufacturer,
                        Build.DEVICE));

                sb.append(String.format("Android: %s (SDK %s)\n",
                        Build.VERSION.RELEASE, Build.VERSION.SDK));

                sb.append(String.format("Build  : %s\n",
                        Build.FINGERPRINT));
            } catch (Exception ignore) {
            }
        }

        private void addLastExceptions(StringBuilder sb, PrefsValues prefs) {
            sb.append("\n## Recent Exceptions ##\n\n");

            String s = prefs.getLastExceptions();
            if (s == null) {
                sb.append("None\n");
            } else {
                sb.append(s);
            }
        }

        private void addLastActions(StringBuilder sb, PrefsValues prefs) {
            sb.append("\n## Recent Actions ##\n\n");

            String s = prefs.getLastActions();
            if (s == null) {
                sb.append("None\n");
            } else {
                sb.append(s);
            }
        }

        private void addProfiles(StringBuilder sb, Context c) {
            sb.append("\n## Profiles Summary ##\n\n");

            try {
                ProfilesDB profilesDb = new ProfilesDB();
                try {
                    profilesDb.onCreate(c);

                    String[] p = profilesDb.getProfilesDump();
                    for (String s : p) {
                        sb.append(s);
                    }

                } finally {
                    profilesDb.onDestroy();
                }
            } catch (Exception ignore) {
                // ignore
            }
        }

        private void addLogcat(StringBuilder sb) {
            sb.append(String.format("\n## %s Logcat ##\n\n", mAppName));

            String[] cmd = new String[] {
                    "logcat",
                    "-d",       // dump log and exits

                    ProfilesUI.TAG + ":D",
                    EditProfileUI.TAG + ":D",
                    EditActionUI.TAG + ":D",
                    IntroActivity.TAG + ":D",
                    ErrorReporterUI.TAG + ":D",
                    ChangeBrightnessActivity.TAG + ":D",

                    ProfilesDB.TAG + ":D",
                    ProfileHeaderHolder.TAG + ":D",
                    TimedActionHolder.TAG + ":D",

                    ApplySettings.TAG + ":D",
                    AutoReceiver.TAG + ":D",
                    SettingsHelper.TAG + ":D",
                    ExceptionHandler.TAG + ":D",
                    AgentWrapper.TAG + ":D",

                    "WindowManager:D",
                    "FlurryAgent:W",

                    "*:S",      // silence all tags we don't want
            };

            try {
                Process p = Runtime.getRuntime().exec(cmd);

                InputStreamReader isr = null;
                BufferedReader br = null;

                try {
                    InputStream is = p.getInputStream();
                    isr = new InputStreamReader(is);
                    br = new BufferedReader(isr);

                    String line = null;

                    // Make sure this doesn't take forever.
                    // We cut after 30 seconds which is already very long.
                    long maxMs = System.currentTimeMillis() + 30*1000;
                    int count = 0;

                    while (!mAbortReport && (line = br.readLine()) != null) {
                        sb.append(line).append("\n");

                        // check time limit once in a while
                        count++;
                        if (count > 50) {
                            if (System.currentTimeMillis() > maxMs) {
                                // This may or may not work.
                                // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4485742
                                p.destroy();
                                break;
                            }
                            count = 0;
                        }
                    }

                } finally {
                    if (br != null) {
                        br.close();
                    }
                    if (isr != null) {
                        isr.close();
                    }
                }

            } catch (IOException e) {
                Log.d(TAG, "Logcat exec failed", e);
            } catch (Throwable ignore) {
            }
        }

    }
}
