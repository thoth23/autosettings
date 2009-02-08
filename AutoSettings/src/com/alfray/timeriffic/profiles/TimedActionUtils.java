/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License TBD
 */


package com.alfray.timeriffic.profiles;

import java.util.Calendar;
import java.util.GregorianCalendar;


public class TimedActionUtils {

    static public String computeDescription(int hourMin, int days, String actions) {

        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR, hourMin / 60);
        int min = hourMin % 60;
        c.set(Calendar.MINUTE, min);
        String desc_time = null;
        if (min != 0) {
            desc_time = String.format("%1$tl:%1$tM %1$tp", c);
        } else {
            desc_time = String.format("%1$tl %1$tp", c);
        }
            
        String[] days_names = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
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
                    desc_days.append(days_names[i]);
                    start = i;
                    count = 0;
                }
            } else {
                if (start >= 0 && count > 0) {
                    // close range
                    desc_days.append(" - ");
                    desc_days.append(days_names[start]);
                }
                start = -2;
                count = 0;
            }
        }
        if (start >= 0 && count > 0) {
            // close range
            desc_days.append(" - ");
            desc_days.append(days_names[start]);
        }
        if (desc_days.length() == 0) desc_days.append("Never");

        
        StringBuilder desc_actions = new StringBuilder();

        if (actions != null) {
            for (String action : actions.split(",")) {
                int value = -1;
                if (action.length() > 1) {
                    try {
                        value = Integer.parseInt(action.substring(1));
                    } catch (NumberFormatException e) {
                        // pass
                    }
                }
                if (action.startsWith(Columns.ACTION_RINGER) && value >= 0) {
                    desc_actions.append(value > 0 ? "Ringer on" : "Mute");
                }
                if (action.startsWith(Columns.ACTION_VIBRATE) && value >= 0) {
                    if (desc_actions.length() > 0) desc_actions.append(", ");
                    desc_actions.append(value > 0 ? "Vibrate" : "No vibrate");
                }
            }
        }
        
        if (desc_actions.length() == 0) desc_actions.append("No action");

        String description = String.format("%s %s, %s", desc_time, desc_days, desc_actions);
        return description;
    }
    
}
