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

package com.alfray.timeriffic.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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
import com.alfray.timeriffic.utils.AgentWrapper;
import com.alfray.timeriffic.utils.ExceptionHandler;

/**
 * Screen to generate an error report.
 */
public class ErrorReporterUI extends Activity {

    private static final boolean DEBUG = true;
    private static final String TAG = "TFC-ErrorUI";

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
        setupWebViewClient(wv);
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
                    // Only change the progress bar when its in "determinate" mode.
                    // It starts this way, and we use this to indicate loading
                    // of the web view.
                    // Later the generator thread will change it to indeterminate
                    // mode, in which case we don't override it.
                    if (!progress.isIndeterminate()) {
                        progress.setProgress(newProgress);
                        progress.setVisibility(newProgress == 100 ? View.GONE : View.VISIBLE);
                    }
                }
            });
        }
    }

    private void setupWebViewClient(final WebView wv) {
        /* -- not needed in this webview
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.endsWith("/#new")) {
                    wv.loadUrl("javascript:location.href=\"#new\"");
                    return true;

                } else if (url.endsWith("/#known")) {
                    wv.loadUrl("javascript:location.href=\"#known\"");
                    return true;

                } else if (url.startsWith("market://")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        // ignore. just means this device has no Market app
                        // so maybe it's an emulator.
                    }
                    return true;
                }
                return false;
            }
        });
        */
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

                    Thread t = new Thread(new ReportGenerator(), "ReportGenerator");
                    t.start();
                }
            });
        }
    }

    private void setupHandler() {
        mHandler = new Handler(new Callback() {

            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MSG_REPORT_COMPLETE) {

                    // Get the report associated with the message
                    String report = (String) msg.obj;

                    // Stop inderterminate progress bar
                    ProgressBar progress = (ProgressBar) findViewById(R.id.progress);
                    if (progress != null) {
                        progress.setIndeterminate(false);
                        progress.setVisibility(View.GONE);
                    }

                    // Gray generate button (to avoid user repeating it)
                    Button gen = (Button) findViewById(R.id.generate);
                    if (gen != null) {
                        gen.setEnabled(true);
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

                        //-- i = Intent.createChooser(i, "Send Error Email");

                        try {
                            startActivity(i);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(getApplicationContext(),
                                    R.string.errorreport_nomailapp,
                                    Toast.LENGTH_LONG).show();
                            Log.d(TAG, "No email/gmail app found", e);
                        } catch (Exception e) {
                            Log.d(TAG, "Send email activity failed", e);
                        }

                        // Finish this activity.
                        finish();
                    }
                }
                return false;
            }
        });
    }

    private class ReportGenerator implements Runnable {
        @Override
        public void run() {

            // Context c = ErrorReporterUI.this.getApplicationContext();

            StringBuilder sb = new StringBuilder();

            // App info
            sb.append(String.format("App: %s %s\n",
                    mAppName,
                    mAppVersion));

            // Android OS information
            int sdk = 0;
            try {
                sdk = Integer.parseInt(Build.VERSION.SDK);
            } catch (Exception ignore) {
            }

            String manufacturer = "--";
            if (sdk >= 4) {
                manufacturer = Build.MANUFACTURER;
            }

            sb.append(String.format("Device: %s (%s %s)\n Build: %s",
                    Build.MODEL,
                    manufacturer,
                    Build.DEVICE));

            sb.append(String.format("Android: %s (SDK %s)\n",
                    Build.VERSION.RELEASE, Build.VERSION.SDK));


            sb.append(String.format("Build: %s",
                    Build.FINGERPRINT));

            // We're done with the report. Ping back the activity using
            // a message to get back to the UI thread.
            if (!mAbortReport) {
                Message msg = mHandler.obtainMessage(MSG_REPORT_COMPLETE, sb.toString());
                mHandler.sendMessage(msg);
            }
        }
    }
}
