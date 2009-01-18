/*
 * (c) ralfoide gmail com, 2008
 * Project: AutoSettings
 * License TBD
 */


package com.alfray.timeriffic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class Utils {

    public static CharSequence formatTime(Object caller, long time_ms, long now) {
        /* The following method is not visible anymore in SDK 1.0 but still present,
           just use reflection to do the equivalent of:
            
        CharSequence mod = android.pim.DateUtils.getRelativeTimeSpanString(
                time_ms,
                now,
                0); // minresolution
        */
        
        ClassLoader cl = caller.getClass().getClassLoader();
        Class<?> du_class;
        try {
            du_class = cl.loadClass("android.pim.DateUtils");
            Method method = du_class.getDeclaredMethod("getRelativeTimeSpanString",
                    new Class[] { long.class, long.class, long.class });
            Object v = method.invoke(null /*receiver*/, new Object[] { time_ms, now, (long)0 });

            if (v instanceof CharSequence) {
                return (CharSequence) v;
            }
        } catch (ClassNotFoundException e) {
            // pass
        } catch (SecurityException e) {
            // pass
        } catch (NoSuchMethodException e) {
            // pass
        } catch (IllegalArgumentException e) {
            // pass
        } catch (IllegalAccessException e) {
            // pass
        } catch (InvocationTargetException e) {
            // pass
        }
        
        return null;
    }
    
}
