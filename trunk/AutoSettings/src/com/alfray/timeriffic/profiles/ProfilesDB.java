/*
 * Copyright 2008 (c) ralfoide gmail com, 2008
 * Project: timeriffic
 * License: GPL version 3 or any later version
 */

package com.alfray.timeriffic.profiles;

import java.security.InvalidParameterException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

//-----------------------------------------------

/*
 * Debug Tip; to view content of database, use:
 * $ cd /cygdrive/c/.../android-sdk_..._windows/tools
 * $ ./adb shell 'echo ".dump" | sqlite3 data/data/com.alfray.timeriffic/databases/games.db'
 */

/**
 * Helper to access the profiles database.
 * <p/>
 * The interface is similar to a {@link ContentProvider}, which should make it
 * easy to use only later.
 */
public class ProfilesDB {
    
    private static final String TAG = "ProfilesDB";

    private static final String PROFILES_TABLE = "profiles";
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
            mDbHelper = null;
        }
    }

    // ----------------------------------

    /**
     * @see SQLiteDatabase#beginTransaction()
     */
    public void beginTransaction() {
        mDb.beginTransaction();
    }

    /**
     * @see SQLiteDatabase#setTransactionSuccessful()
     */
    public void setTransactionSuccessful() {
        mDb.setTransactionSuccessful();
    }

    /**
     * @see SQLiteDatabase#endTransaction()
     */
    public void endTransaction() {
        mDb.endTransaction();
    }

    // ----------------------------------
    
    public long getProfileIdForRowId(long row_id) {
        try {
            SQLiteStatement sql = mDb.compileStatement(
                    String.format("SELECT %s FROM %s WHERE %s=%d;",
                            Columns.PROFILE_ID,
                            PROFILES_TABLE,
                            Columns._ID, row_id));
            return sql.simpleQueryForLong();
        } catch (SQLiteDoneException e) {
            // no profiles
            return 0;
        }
    }
    
    /**
     * Returns the max profile index (not id!).
     * 
     * If maxProfileIndex == 0, returns the absolute max profile index,
     * i.e. the very last one.
     * If maxProfileIndex > 0, returns the max profile index that is smaller
     * than the given index (so basically the profile just before the one
     * given).
     * Returns 0 if there is no such index.
     */
    public long getMaxProfileIndex(long maxProfileIndex) {
        try {
            String testMaxProfId = "";
            if (maxProfileIndex > 0) {
                testMaxProfId = String.format("AND %s<%d",
                        Columns.PROFILE_ID, maxProfileIndex << Columns.PROFILE_SHIFT);
            }
            // e.g. SELECT MAX(prof_id) FROM profiles WHERE type=1 [ AND prof_id < 65536 ]
            SQLiteStatement sql = mDb.compileStatement(
                    String.format("SELECT MAX(%s) FROM %s WHERE %s=%d %s;",
                            Columns.PROFILE_ID,
                            PROFILES_TABLE,
                            Columns.TYPE, Columns.TYPE_IS_PROFILE,
                            testMaxProfId));

            return sql.simpleQueryForLong() >> Columns.PROFILE_SHIFT;
        } catch (SQLiteDoneException e) {
            // no profiles
            return 0;
        }
    }

    /**
     * Returns the min action index (not id!) that is greater than the
     * requested minActionIndex.
     * <p/>
     * So for a given index (including 0), returns the next one used.
     * If there's no such index (i.e. not one used after the given index)
     * returns -1.
     */
    public long getMinActionIndex(long profileIndex, long minActionIndex) {
        try {
            long pid = (profileIndex << Columns.PROFILE_SHIFT) + minActionIndex;
            long maxPid = (profileIndex + 1) << Columns.PROFILE_SHIFT;

            // e.g. SELECT MAX(prof_id) FROM profiles WHERE type=2 AND prof_id > 32768+256 AND prof_id < 65536
            SQLiteStatement sql = mDb.compileStatement(
                    String.format("SELECT MIN(%s) FROM %s WHERE %s=%d AND %s>%d AND %s<%d;",
                            Columns.PROFILE_ID,
                            PROFILES_TABLE,
                            Columns.TYPE, Columns.TYPE_IS_TIMED_ACTION,
                            Columns.PROFILE_ID, pid, 
                            Columns.PROFILE_ID, maxPid));

            return sql.simpleQueryForLong() & Columns.ACTION_MASK;
        } catch (SQLiteDoneException e) {
            // no actions
            return -1;
        }
    }

    // ----------------------------------

    /**
     * Inserts a new profile before the given profile index.
     * If beforeProfileIndex is <= 0, insert at the end.
     * 
     * @return the profile index (not the row id)
     */
    public long insertProfile(long beforeProfileIndex,
            String title, boolean isEnabled) {

        beginTransaction();
        try {
            long index = getMaxProfileIndex(beforeProfileIndex);
            if (beforeProfileIndex <= 0) {
                long max = Long.MAX_VALUE >> Columns.PROFILE_SHIFT;
                if (index >= max - 1) {
                    // TODO repack
                    throw new UnsupportedOperationException("Profile index at maximum.");
                } else if (index < max - Columns.PROFILE_GAP) {
                    index += Columns.PROFILE_GAP;
                } else {
                    index += (max - index) / 2;
                }
            } else {
                if (index == beforeProfileIndex - 1) {
                    // TODO repack
                    throw new UnsupportedOperationException("No space left to insert profile before profile.");
                } else {
                    index = (index + beforeProfileIndex) / 2; // get middle offset
                }
            }
            
            long id = index << Columns.PROFILE_SHIFT;
            
            ContentValues values = new ContentValues(2);
            values.put(Columns.PROFILE_ID, id);
            values.put(Columns.TYPE, Columns.TYPE_IS_PROFILE);
            values.put(Columns.DESCRIPTION, title);
            values.put(Columns.IS_ENABLED, isEnabled);
            
            id = mDb.insert(PROFILES_TABLE, Columns.TYPE, values);

            Log.d(TAG, String.format("Insert profile: %d => row %d", index, id));

            if (id < 0) throw new SQLException("insert profile row failed");
            setTransactionSuccessful();
            return index;
        } finally {
            endTransaction();
        }
    }

    /**
     * Inserts a new action for the given profile index.
     * If afterActionIndex is == 0, inserts at the beginning of these actions.
     * 
     * @return the action index (not the row id)
     */
    public long insertTimedAction(long profileIndex,
            long afterActionIndex,
            boolean isActive,
            int hourMin,
            int days,
            String actions,
            long nextMs) {

        beginTransaction();
        try {
            long pid = profileIndex << Columns.PROFILE_SHIFT;

            long index = getMinActionIndex(profileIndex, afterActionIndex);

            if (index < 0) index = Columns.ACTION_MASK - 1;
            
            if (index - afterActionIndex < 2) {
                // TODO repack
                throw new UnsupportedOperationException("No space left to insert action.");
            }

            index = (index + afterActionIndex) / 2; // get middle offset
            
            pid += index;

            String description = TimedActionUtils.computeDescription(hourMin, days, actions);
            
            ContentValues values = new ContentValues(2);

            values.put(Columns.TYPE, Columns.TYPE_IS_TIMED_ACTION);
            values.put(Columns.PROFILE_ID, pid);
            values.put(Columns.DESCRIPTION, description);
            values.put(Columns.IS_ENABLED, isActive);
            values.put(Columns.HOUR_MIN, hourMin);
            values.put(Columns.DAYS, days);
            values.put(Columns.ACTIONS, actions);
            values.put(Columns.NEXT_MS, nextMs);
            
            long id = mDb.insert(PROFILES_TABLE, Columns.TYPE, values);

            Log.d(TAG, String.format("Insert profile %d, action: %d => row %d", profileIndex, index, id));

            if (id < 0) throw new SQLException("insert action row failed");
            setTransactionSuccessful();
            return index;
        } finally {
            endTransaction();
        }
    }

    // ----------------------------------

    /** id is used if >= 0 */
    public Cursor query(long id,
            String[] projection,
            String selection, 
            String[] selectionArgs, 
            String sortOrder) {
    	
    	SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    	qb.setTables(PROFILES_TABLE);

    	if (id >= 0) {
        	qb.appendWhere(String.format("%s=%d", Columns._ID, id));
        }
        
        if (sortOrder == null || sortOrder.length() == 0) sortOrder = Columns.DEFAULT_SORT_ORDER;
        
        Cursor c = qb.query(mDb, projection, selection, selectionArgs,
        		null, // groupBy
        		null, // having,
        		sortOrder);
        return c;
    }

    // ----------------------------------

    /**
     * @param name Profile name to update, if not null.
     * @param isEnabled Profile is enable flag to update.
     * @return Number of rows affected. 1 on success, 0 on failure.
     */
    public int updateProfile(long prof_id, String name, boolean isEnabled) {

        String where = String.format("%s=%d AND %s=%d",
                Columns.TYPE, Columns.TYPE_IS_PROFILE,
                Columns.PROFILE_ID, prof_id);

        ContentValues cv = new ContentValues();
        if (name != null) cv.put(Columns.DESCRIPTION, name);
        cv.put(Columns.IS_ENABLED, isEnabled);
        
        int count = mDb.update(PROFILES_TABLE, cv, where, null);
        return count;
    }

    /**
     * @param isEnabled Timed action is enable flag to update.
     * @return Number of rows affected. 1 on success, 0 on failure.
     */
    public int updateTimedAction(long action_id, boolean isEnabled) {

        String where = String.format("%s=%d AND %s=%d",
                Columns.TYPE, Columns.TYPE_IS_TIMED_ACTION,
                Columns.PROFILE_ID, action_id);

        ContentValues cv = new ContentValues();
        cv.put(Columns.IS_ENABLED, isEnabled);
        
        int count = mDb.update(PROFILES_TABLE, cv, where, null);
        return count;
    }

    /**
     * @param name Timed action description to update, if not null.
     * @return Number of rows affected. 1 on success, 0 on failure.
     */
    public int updateTimedAction(long action_id, int hourMin, int days,
            String actions, String description) {

        String where = String.format("%s=%d AND %s=%d",
                Columns.TYPE, Columns.TYPE_IS_TIMED_ACTION,
                Columns.PROFILE_ID, action_id);

        ContentValues cv = new ContentValues();
        cv.put(Columns.HOUR_MIN, hourMin);
        cv.put(Columns.DAYS, days);
        cv.put(Columns.ACTIONS, actions);
        if (description != null) cv.put(Columns.DESCRIPTION, description);
        
        int count = mDb.update(PROFILES_TABLE, cv, where, null);
        return count;
    }

    // ----------------------------------

    /** 
     * @param row_id The SQL row id, NOT the prof_id
     * @return The number of deleted rows, >= 1 on success, 0 on failure.
     */
    public int deleteProfile(long row_id) {
        
        beginTransaction();
        try {
            long pid = getProfileIdForRowId(row_id);
            if (pid == 0) throw new InvalidParameterException("No profile id for this row id.");

            pid = pid & (~Columns.ACTION_MASK);
            
            // DELETE FROM profiles WHERE prof_id >= 65536 AND prof_id < 65536+65535
            String where = String.format("%s>=%d AND %s<%d",
                    Columns.PROFILE_ID, pid,
                    Columns.PROFILE_ID, pid + Columns.ACTION_MASK);
            
            int count = mDb.delete(PROFILES_TABLE, where, null);
            setTransactionSuccessful();
            return count;
        } finally {
            endTransaction();
        }
    }

    /** 
     * @param row_id The SQL row id, NOT the prof_id
     * @return The number of deleted rows, 1 on success, 0 on failure.
     */
    public int deleteAction(long row_id) {
        
        beginTransaction();
        try {
            // DELETE FROM profiles WHERE TYPE=2 AND _id=65537
            String where = String.format("%s=%d AND %s=%d",
                    Columns.TYPE, Columns.TYPE_IS_TIMED_ACTION,
                    Columns._ID, row_id);
            
            int count = mDb.delete(PROFILES_TABLE, where, null);
            setTransactionSuccessful();
            return count;
        } finally {
            endTransaction();
        }
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
                    PROFILES_TABLE,
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
            onInitializeProfiles();
            mDb = old_mDb;
        }
		
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, String.format("Upgrading database from version %1$d to %2$d.",
                    oldVersion, newVersion));
            db.execSQL("DROP TABLE IF EXISTS " + PROFILES_TABLE);
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
    private void onInitializeProfiles() {
        long pindex = insertProfile(0, "Weekdaze", true /*isEnabled*/);
        long action = insertTimedAction(pindex, 0,
                true,               //isActive
                7*60+0,             //hourMin
                Columns.MONDAY + Columns.TUESDAY + Columns.WEDNESDAY + Columns.THURSDAY,
                "M1,V1",            //actions
                0                   //nextMs
                );
        insertTimedAction(pindex, action,
                false,              //isActive
                20*60+0,             //hourMin
                Columns.MONDAY + Columns.TUESDAY + Columns.WEDNESDAY + Columns.THURSDAY,
                "M0,V1",            //actions
                0                   //nextMs
                );

        pindex = insertProfile(0, "Party Time", true /*isEnabled*/);
        action = insertTimedAction(pindex, 0,
                false,              //isActive
                9*60+0,             //hourMin
                Columns.FRIDAY + Columns.SATURDAY,
                "M1",               //actions
                0                   //nextMs
                );
        insertTimedAction(pindex, action,
                false,               //isActive
                22*60+0,             //hourMin
                Columns.FRIDAY + Columns.SATURDAY,
                "M0,V1",            //actions
                0                   //nextMs
                );

        pindex = insertProfile(0, "Sleeping-In", true /*isEnabled*/);
        action = insertTimedAction(pindex, 0,
                false,               //isActive
                10*60+30,            //hourMin
                Columns.SUNDAY,
                "M1",               //actions
                0                   //nextMs
                );
        insertTimedAction(pindex, action,
                false,               //isActive
                21*60+0,             //hourMin
                Columns.SUNDAY,
                "M0,V1",            //actions
                0                   //nextMs
                );
    }
}
