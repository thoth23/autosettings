/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License TBD
 */


package com.alfray.timeriffic.profiles;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.Context;

import com.alfray.timeriffic.utils.SettingsHelper;
import com.alfray.timeriffic.utils.SettingsHelper.RingerMode;
import com.alfray.timeriffic.utils.SettingsHelper.VibrateRingerMode;


public class TimedActionUtils {

    static private final int[] CALENDAR_DAYS = { 
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
            Calendar.SUNDAY
    };

    static private final String[] DAYS_NAMES = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };

    static public int calendarDayToActionDay(Calendar c) {
        int day = c.get(Calendar.DAY_OF_WEEK);
        for (int i = Columns.MONDAY_BIT_INDEX; i <= Columns.SUNDAY_BIT_INDEX; i++) {
            if (day == CALENDAR_DAYS[i]) {
                day = 1<<i;
                break;
            }
        }
        return day;
    }

    static public String computeDescription(
            Context context, int hourMin, int days, String actions) {
        
        SettingsHelper sh = new SettingsHelper(context);
        
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY, hourMin / 60);
        int min = hourMin % 60;
        c.set(Calendar.MINUTE, min);
        String desc_time = null;
        if (min != 0) {
            desc_time = String.format("%1$tl:%1$tM %1$tp", c);
        } else {
            desc_time = String.format("%1$tl %1$tp", c);
        }
            
        int start = -2;
        int count = 0;
        StringBuilder desc_days = new StringBuilder();

        for (int i = Columns.MONDAY_BIT_INDEX; i <= Columns.SUNDAY_BIT_INDEX; i++) {
            if ((days & (1<<i)) != 0 ) {
                
                if (start == i-1) {
                    // continue range
                    start = i;
                    count++;
                } else {
                    // start new range
                    if (desc_days.length() > 0) desc_days.append(", ");
                    desc_days.append(DAYS_NAMES[i]);
                    start = i;
                    count = 0;
                }
            } else {
                if (start >= 0 && count > 0) {
                    // close range
                    desc_days.append(" - ");
                    desc_days.append(DAYS_NAMES[start]);
                }
                start = -2;
                count = 0;
            }
        }
        if (start >= 0 && count > 0) {
            // close range
            desc_days.append(" - ");
            desc_days.append(DAYS_NAMES[start]);
        }
        if (desc_days.length() == 0) desc_days.append("Never");

        
        ArrayList<String> actions_names = new ArrayList<String>();

        if (actions != null) {
            for (String action : actions.split(",")) {
                int value = -1;
                if (action.length() > 1) {
                    char code = action.charAt(0);
                    char v = action.charAt(1);
                    
                    switch(code) {
                    case Columns.ACTION_RINGER:
                        for (RingerMode mode : RingerMode.values()) {
                            String name = mode.toString();
                            if (name.charAt(0) == v) {
                                actions_names.add(name);   // ringer name
                                break;
                            }
                        }
                        break;
                    case Columns.ACTION_VIBRATE:
                        for (VibrateRingerMode mode : VibrateRingerMode.values()) {
                            String name = mode.toString();
                            if (name.charAt(0) == v) {
                                actions_names.add(name);   // vibrate name
                                break;
                            }
                        }
                        break;
                    default:
                        try {
                            value = Integer.parseInt(action.substring(1));

                            switch(code) {
                            case Columns.ACTION_WIFI:
                                if (sh.canControlWifi()) {
                                    actions_names.add(value > 0 ? "Wifi on" : "Wifi off");
                                }
                                break;
                            case Columns.ACTION_BRIGHTNESS:
                                if (sh.canControlBrigthness()) {
                                    actions_names.add(String.format("Brightness %d%%", value));
                                }
                                break;
                            case Columns.ACTION_RING_VOLUME:
                                actions_names.add(String.format("Ringer %d%%", value));
                                break;
                            }
                            
                        } catch (NumberFormatException e) {
                            // pass
                        }
                    }
                }
            }
        }
        
        StringBuilder desc_actions = new StringBuilder();

        if (actions_names.size() == 0) {
            desc_actions.append("No action");
        } else {
            for (String name : actions_names) {
                if (desc_actions.length() > 0) desc_actions.append(", ");
                desc_actions.append(name);
            }
        }

        String description = String.format("%s %s, %s", desc_time, desc_days, desc_actions);
        return description;
    }
    
}
