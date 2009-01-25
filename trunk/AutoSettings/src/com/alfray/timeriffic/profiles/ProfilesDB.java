/*
 * Copyright 2008 (c) ralfoide gmail com, 2008
 * Project: timeriffic
 * License: GPL version 3 or any later version
 */

package com.alfray.timeriffic.profiles;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

//-----------------------------------------------

/*
 * Debug Tip; to view content of database, use:
 * $ cd /cygdrive/c/.../android-sdk_..._windows/tools
 * $ ./adb shell 'echo ".dump" | sqlite3 data/data/com.alfray.asqare/databases/games.db'
 */

/**
 * Helper to access the profiles database.
 * <p/>
 * The interface is similar to a {@link ContentProvider}, which should make it
 * easy to use only later.
 */
public class ProfilesDB {
    
    private static final String TAG = "ProfilesDB";

    private static final String TABLE_NAME = "profiles";
    private static final String DB_NAME = "profiles.db";
    private static final int DB_VERSION = 1 * 100 + 1; // major*100 + minor

    private SQLiteDatabase mDb;
    private DatabaseHelper mDbHelper;

    // ----------------------------------

    /** Call this after creating this object. */
    public boolean onCreate(Context context) {
        mDbHelper = new DatabaseHelper(context, DB_NAME, DB_VERSION);
        mDb = mDbHelper.getWritableDatabase();
        boolean created = mDb != null;
        return created;
    }

    /** Call this when the database is no longer needed. */
    public void onDestroy() {
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    // ----------------------------------

    public int delete(long _id) {
        String whereClause = String.format("%s=%d",
                Columns._ID,
                _id);
        
        int count = mDb.delete(TABLE_NAME, whereClause, null);
        return count;
    }

    public long insertProfile(String title, boolean isEnabled) {

        ContentValues values = new ContentValues(2);

        values.put(Columns.TYPE, Columns.TYPE_IS_PROFILE);
        values.put(Columns.DESCRIPTION, title);
        values.put(Columns.IS_ENABLED, isEnabled);
        
        long id = mDb.insert(TABLE_NAME, Columns.TYPE, values);
        if (id < 0) throw new SQLException("insert row failed");
        return id;
    }

    public long insertTimedAction(long profileId,
            String description,
            boolean isActive,
            int hourMin,
            int days,
            String actions,
            long nextMs) {

        ContentValues values = new ContentValues(2);

        values.put(Columns.TYPE, Columns.TYPE_IS_TIMED_ACTION);
        values.put(Columns.PROFILE_ID, profileId);
        values.put(Columns.DESCRIPTION, description);
        values.put(Columns.IS_ENABLED, isActive);
        values.put(Columns.HOUR_MIN, hourMin);
        values.put(Columns.DAYS, days);
        values.put(Columns.ACTIONS, actions);
        values.put(Columns.NEXT_MS, nextMs);
        
        long id = mDb.insert(TABLE_NAME, Columns.TYPE, values);
        if (id < 0) throw new SQLException("insert row failed");
        return id;
    }

    /** id is used if >= 0 */
    public Cursor query(long id,
            String[] projection,
            String selection, 
            String[] selectionArgs, 
            String sortOrder) {
    	
    	SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    	qb.setTables(TABLE_NAME);

    	if (id >= 0) {
        	qb.appendWhere(String.format("%1$s=%2$ld", Columns._ID, id));
        }
        
        if (sortOrder == null || sortOrder.length() == 0) sortOrder = Columns.DEFAULT_SORT_ORDER;
        
        Cursor c = qb.query(mDb, projection, selection, selectionArgs,
        		null, // groupBy
        		null, // having,
        		sortOrder);
        return c;
    }

    /** id is used if >= 0 */
    public int update(long id, ContentValues values, String whereClause, String[] whereArgs) {
        if (id >= 0) {
        	whereClause = addWhereId(id, whereClause);
        }
    	int count = mDb.update(TABLE_NAME, values, whereClause, whereArgs);
        return count;
    }

    
    /**
     * Helper that returns a where clause "_id=NN" where NN is the last segment of
     * the input URI. If there's an existing whereClause, it is rewritten using
     * "_id=NN AND ( whereClause )".
     */
    private String addWhereId(long id, String whereClause) {
        if (whereClause != null && whereClause.length() > 0) {
            whereClause = "AND (" + whereClause + ")";
        } else {
            whereClause = "";
        }
        whereClause = String.format("%1$s=%2$ld %3$s",
                Columns._ID,
                id,
                whereClause);
        return whereClause;
    }

    
    // ----------------------------------    

    /** Convenience helper to open/create/update the database */
    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context,
                String db_name,
                int version) {
			super(context, db_name, null /* cursor factory */, version);
		}

		@Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(String.format("CREATE TABLE %s "
                    + "(%s INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "%s INTEGER, "
                    + "%s TEXT, "
                    + "%s INTEGER, "
                    + "%s INTEGER, "
                    + "%s INTEGER, "
                    + "%s INTEGER, "
                    + "%s TEXT, "
                    + "%s INTEGER);" ,
                    TABLE_NAME,
                    Columns._ID,
                    Columns.TYPE,
                    Columns.DESCRIPTION,
                    Columns.IS_ENABLED,
                    Columns.PROFILE_ID,
                    Columns.HOUR_MIN,
                    Columns.DAYS,
                    Columns.ACTIONS,
                    Columns.NEXT_MS));
            
            SQLiteDatabase old_mDb = mDb;
            mDb = db;
            onInitialize();
            mDb = old_mDb;
        }
		
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, String.format("Upgrading database from version %1$d to %2$d.",
                    oldVersion, newVersion));
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
        
        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            // pass
        }        
    }

    /**
     * Called by {@link DatabaseHelper} when the database has just been
     * created to initialize it with initial data. It's safe to use
     * {@link ProfilesDB#insertProfile(String, boolean)} or
     * {@link ProfilesDB#insertTimedAction(String, boolean, int, int, String, long)}
     * at that point.
     */
    private void onInitialize() {
        long pid = insertProfile("Weekdaze", true /*isEnabled*/);
        insertTimedAction(pid,
                "7am Mon - Thu, Ringer on, Vibrate",
                true,               //isActive
                7*60+0,             //hourMin
                Columns.MONDAY + Columns.TUESDAY + Columns.WEDNESDAY + Columns.THURSDAY,
                "M0,V1",            //actions
                0                   //nextMs
                );
        insertTimedAction(pid,
                "8pm Mon - Thu, Mute, vibrate",
                false,              //isActive
                20*60+0,             //hourMin
                Columns.MONDAY + Columns.TUESDAY + Columns.WEDNESDAY + Columns.THURSDAY,
                "M1,V1",            //actions
                0                   //nextMs
                );

        pid = insertProfile("Party Time", true /*isEnabled*/);
        insertTimedAction(pid,
                "9am Fri - Sat, Ringer on",
                false,              //isActive
                9*60+0,             //hourMin
                Columns.FRIDAY + Columns.SATURDAY,
                "M0",               //actions
                0                   //nextMs
                );
        insertTimedAction(pid,
                "10pm Fri - Sat, Mute, vibrate",
                false,               //isActive
                22*60+0,             //hourMin
                Columns.FRIDAY + Columns.SATURDAY,
                "M1,V1",            //actions
                0                   //nextMs
                );

        pid = insertProfile("Sleeping-In", true /*isEnabled*/);
        insertTimedAction(pid,
                "10:30am Sun, Ringer on",
                false,               //isActive
                10*60+30,            //hourMin
                Columns.SUNDAY,
                "M0",               //actions
                0                   //nextMs
                );
        insertTimedAction(pid,
                "9pm Sun, Mute, vibrate",
                false,               //isActive
                21*60+0,             //hourMin
                Columns.SUNDAY,
                "M1,V1",            //actions
                0                   //nextMs
                );
    }
}
