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
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.utils.AgentWrapper;
import com.alfray.timeriffic.utils.ExceptionHandler;

/**
 * Screen to generate an error report.
 */
public class ErrorReporterUI extends Activity {

    private static final boolean DEBUG = true;
    private static final String TAG = "TFC-ErrorUI";

    private AgentWrapper mAgentWrapper;

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
                    progress.setProgress(newProgress);
                    progress.setVisibility(newProgress == 100 ? View.GONE : View.VISIBLE);
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
        Button gen = (Button) findViewById(R.id.generate);
        if (gen != null) {
            gen.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // close activity
                    finish();
                }
            });
        }
    }
}
