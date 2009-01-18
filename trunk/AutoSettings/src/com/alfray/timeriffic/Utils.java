/*
 * (c) ralfoide gmail com, 2008
 * Project: AutoSettings
 * License TBD
 */


package com.alfray.timeriffic;

import java.util.Calendar;


public class Utils {

    /**
     * Returns a string describing time_ns compared to now.
     * Examples:
     * - "2 sec.s ago"
     * - "2 min. ago"
     * - "2 hours ago"
     * - "2 days ago"
     * 
     * If the delta is > 7 days, simply formats a short date ("Dec 21")
     * If the delta is > 31 days, formats an YYYY/MM/DD date.
     * 
     * "ago" is used if time_ms < now (the typical case).
     * If time_ms > now, uses "later"
     * If time_ns == now or if delta < 1 second, uses "now".
     */
    public static String formatTime(Calendar cal, long time_ms, long now) {
        long delta = now - time_ms;

        String qualifier = delta > 0 ? "ago" : "later";
        if (delta < 0) delta = 0 - delta;
        
        int second = 1000;
        if (delta < second) {
            // less than 1 sec
            return "now";
        }

        int minute = 60 * second;
        if (delta < minute) {
            // less than 1 minute
            return String.format("%d sec. %s",
                    delta / second,
                    qualifier);
        }
        
        int hour = 60 * minute;
        if (delta < hour) {
            // less than 1 hour
            return String.format("%d min. %s",
                    delta / minute,
                    qualifier);
        }

        long day = 24 * hour;
        if (delta < day) {
            // less than 1 day
            return String.format("%d hour%s %s",
                    delta / hour,
                    delta <= hour ? "" : "s",
                    qualifier);
        }

        if (cal == null) cal = Calendar.getInstance();
        cal.setTimeInMillis(time_ms);

        if (delta > 30 * day) {
            return String.format("%1$tF", cal);
        }
        
        if (delta > 7 * day) {
            return String.format("%1$tb %1$te", cal);
        }

        // less than 7 days
        return String.format("%d day%s %s",
                delta / day,
                delta <= day ? "" : "s",
                qualifier);
    }
    
}
