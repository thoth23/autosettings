package com.alfray.alarmTest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.TimePicker;

public class AlarmTestUI extends Activity {
    private static final String TAG = "AlarmTestUI";
    private static final String ALARM_ACTION = "com.alfray.alarmtest.myreceiver";
    private SharedPreferences mPrefs;
    private TextView mTextView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        final Context context = this;

        mTextView = (TextView) findViewById(R.id.status);
        final TimePicker t = (TimePicker) findViewById(R.id.time);

        log(null); // init text

        findViewById(R.id.start_rtc).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                Intent intent = new Intent(getApplicationContext(), MyReceiver.class);
                intent.setAction(ALARM_ACTION);
                PendingIntent op = PendingIntent.getBroadcast(
                                context,
                                0 /*requestCode*/,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                int h = Integer.valueOf(t.getCurrentHour());
                int m = Integer.valueOf(t.getCurrentMinute());

                Calendar c = new GregorianCalendar();
                c.setTimeInMillis(System.currentTimeMillis());
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                c.set(Calendar.MINUTE, m);
                c.set(Calendar.HOUR_OF_DAY, h);

                long timeMs = c.getTimeInMillis();

                manager.set(AlarmManager.RTC_WAKEUP, timeMs, op);

                // verify time
                SimpleDateFormat sdf = new SimpleDateFormat();
                String st = sdf.format(timeMs);
                long now = System.currentTimeMillis();
                log(String.format("Set alarm to RTC %d (%+d) : %s",
                                timeMs, timeMs - now, st));
            }
        });

        findViewById(R.id.start_elapsed).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                Intent intent = new Intent(getApplicationContext(), MyReceiver.class);
                intent.setAction(ALARM_ACTION);
                PendingIntent op = PendingIntent.getBroadcast(
                                context,
                                0 /*requestCode*/,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                int h = Integer.valueOf(t.getCurrentHour());
                int m = Integer.valueOf(t.getCurrentMinute());

                Calendar c = new GregorianCalendar();
                c.setTimeInMillis(System.currentTimeMillis());
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                c.set(Calendar.MINUTE, m);
                c.set(Calendar.HOUR_OF_DAY, h);

                long timeMs = c.getTimeInMillis() - System.currentTimeMillis();
                long bootMs = SystemClock.elapsedRealtime();
                timeMs += bootMs;

                manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeMs, op);

                log(String.format("Set alarm to Elapsed %d (%+d)",
                                timeMs, timeMs - bootMs));
            }
        });

        findViewById(R.id.clear).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPrefs.edit().putString("log", "").commit();
                mTextView.setText("");
            }
        });
    }

    private void log(String msg) {
        String s = mPrefs.getString("log", "");
        if (msg != null) {
            Log.d(TAG, msg);

            SimpleDateFormat sdf = new SimpleDateFormat();
            String t = sdf.format(new Date());

            s += String.format("[%s] %s\n", t, msg);

            mPrefs.edit().putString("log", s).commit();
        }

        mTextView.setText(s);
    }
}