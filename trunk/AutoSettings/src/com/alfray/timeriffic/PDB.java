/*
 * Project: Timeriffic
 * Copyright (C) 2008 ralfoide gmail com,
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

package com.alfray.timeriffic;

import java.security.InvalidParameterException;
import java.util.ArrayList;


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
import android.widget.Toast;

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
public class PDB {

    private static final boolean DEBUG = true;
    public static final String TAG = "TFC-ProfDB";

    private static final String PROFILES_TABLE = "profiles";
    private static final String DB_NAME = "profiles.db";
    private static final int DB_VERSION = 1 * 100 + 1; // major*100 + minor

    private SQLiteDatabase mDb;
    private _DBH mDbHelper;

    private Context mContext;


    // ----------------------------------

    /** Call this after creating this object. */
    public boolean onCreate(Context context) {
        mContext = context;
        mDbHelper = new _DBH(context, DB_NAME, DB_VERSION);
        mDb = mDbHelper.getWritableDatabase();
        boolean created = mDb != null;
        return created;
    }

    /** Call this when the database is no longer needed. */
    public void onDestroy() {
        mContext = null;
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
                            C.PID,
                            PROFILES_TABLE,
                            C._ID, row_id));
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
                        C.PID, maxProfileIndex << C.PS);
            }
            // e.g. SELECT MAX(prof_id) FROM profiles WHERE type=1 [ AND prof_id < 65536 ]
            SQLiteStatement sql = mDb.compileStatement(
                    String.format("SELECT MAX(%s) FROM %s WHERE %s=%d %s;",
                            C.PID,
                            PROFILES_TABLE,
                            C.T, C.TiP,
                            testMaxProfId));

            return sql.simpleQueryForLong() >> C.PS;
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
            long pid = (profileIndex << C.PS) + minActionIndex;
            long maxPid = (profileIndex + 1) << C.PS;

            // e.g. SELECT MIN(prof_id) FROM profiles WHERE type=2 AND prof_id > 32768+256 AND prof_id < 65536
            SQLiteStatement sql = mDb.compileStatement(
                    String.format("SELECT MIN(%s) FROM %s WHERE %s=%d AND %s>%d AND %s<%d;",
                            C.PID,
                            PROFILES_TABLE,
                            C.T, C.TiTA,
                            C.PID, pid,
                            C.PID, maxPid));

            long result = sql.simpleQueryForLong();
            if (result > pid && result < maxPid) return result & C.AMk;
        } catch (SQLiteDoneException e) {
            // no actions
        }
        return -1;
    }

    /**
     * Returns the max action index (not id!) for this profile.
     * Returns -1 if there are not actions.
     */
    public long getMaxActionIndex(long profileIndex) {
        try {
            long pid = (profileIndex << C.PS);
            long maxPid = (profileIndex + 1) << C.PS;

            // e.g. SELECT MAX(prof_id) FROM profiles WHERE type=2 AND prof_id > 32768 AND prof_id < 65536
            SQLiteStatement sql = mDb.compileStatement(
                    String.format("SELECT MAX(%s) FROM %s WHERE %s=%d AND %s>%d AND %s<%d;",
                            C.PID,
                            PROFILES_TABLE,
                            C.T, C.TiTA,
                            C.PID, pid,
                            C.PID, maxPid));

            long result = sql.simpleQueryForLong();
            if (result > pid && result < maxPid) return result & C.AMk;
        } catch (SQLiteDoneException e) {
            // no actions
        }
        return -1;
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
                long max = Long.MAX_VALUE >> C.PS;
                if (index >= max - 1) {
                    // TODO repack
                    throw new UnsupportedOperationException("Profile index at maximum.");
                } else if (index < max - C.PG) {
                    index += C.PG;
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

            long id = index << C.PS;

            ContentValues values = new ContentValues(2);
            values.put(C.PID, id);
            values.put(C.T, C.TiP);
            values.put(C.Dsc, title);
            values.put(C.EN, isEnabled);

            id = mDb.insert(PROFILES_TABLE, C.T, values);

            if (DEBUG) Log.d(TAG, String.format("Insert profile: %d => row %d", index, id));

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
     * NOTE: currently ignore afterActionIndex and always add at the end.
     *
     * @return the action index (not the row id)
     */
    public long insertTimedAction(long profileIndex,
            long afterActionIndex,
            int hourMin,
            int days,
            String actions,
            long nextMs) {

        beginTransaction();
        try {
            long pid = profileIndex << C.PS;

            long maxIndex = getMaxActionIndex(profileIndex);

            if (maxIndex >= C.AMk) {
                // Last index is used. Try to repack the action list.
                maxIndex = repackTimeActions(profileIndex);

                if (maxIndex == C.AMk) {
                    // definitely full... too bad.
                    Toast.makeText(mContext,
                            "No space left to insert action. Please delete some first.",
                            Toast.LENGTH_LONG).show();
                    return -1;
                }
            }

            if (maxIndex < 0) {
                maxIndex = 0;
            }

            long index = maxIndex + 1;
            pid += index;

            String description = TAU.computeDescription(
                    mContext, hourMin, days, actions);

            ContentValues values = new ContentValues(2);

            values.put(C.T, C.TiTA);
            values.put(C.PID, pid);
            values.put(C.Dsc, description);
            values.put(C.EN, 0);
            values.put(C.HM, hourMin);
            values.put(C.D, days);
            values.put(C.A, actions);
            values.put(C.NMS, nextMs);

            long id = mDb.insert(PROFILES_TABLE, C.T, values);

            if (DEBUG) Log.d(TAG, String.format("Insert profile %d, action: %d => row %d", profileIndex, index, id));

            if (id < 0) throw new SQLException("insert action row failed");
            setTransactionSuccessful();
            return index;
        } finally {
            endTransaction();
        }
    }

    /**
     * Called by insertTimedAction within an existing transaction.
     *
     * Returns the new highest action index (not id) used.
     * Returns 0 if there were no actions.
     * @return
     */
    private long repackTimeActions(long profileIndex) {

        long pid = (profileIndex << C.PS);
        long maxPid = (profileIndex + 1) << C.PS;

        // Generates query with WHERE type=2 AND prof_id > 32768 AND prof_id < 65536
        String where = String.format("%s=%d AND (%s>%d) AND (%s<%d)",
                            C.T, C.TiTA,
                            C.PID, pid,
                            C.PID, maxPid);

        Cursor c = null;
        try {
            c = mDb.query(
                    PROFILES_TABLE,                         // table
                    new String[] { C.PID } ,   // columns
                    where,                                  // selection
                    null,                                   // selectionArgs
                    null,                                   // groupBy
                    null,                                   // having
                    C.PID                      // orderBy
                    );

            int numActions = c.getCount();

            if (DEBUG) Log.d(TAG, String.format("Repacking %d action", numActions));

            if (numActions == 0 || numActions == C.AMk) {
                // we know the table is empty or full, no need to repack.
                return numActions;
            }

            int colProfId = c.getColumnIndexOrThrow(C.PID);

            if (c.moveToFirst()) {
                int i = 1;
                do {
                    long profId = c.getLong(colProfId);

                    long newId = pid + (i++);

                    if (profId != newId) {
                        // generates update with WHERE type=2 AND prof_id=id
                        where = String.format("%s=%d AND %s=%d",
                                C.T, C.TiTA,
                                C.PID, profId);

                        ContentValues values = new ContentValues(1);
                        values.put(C.PID, newId);

                        mDb.update(
                                PROFILES_TABLE, // table
                                values,         // values
                                where,          // whereClause
                                null            // whereArgs
                                );
                    }

                } while (c.moveToNext());
            }

            // new highest index is numActions
            return numActions;
        } finally {
            if (c != null) c.close();
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
        	qb.appendWhere(String.format("%s=%d", C._ID, id));
        }

        if (sortOrder == null || sortOrder.length() == 0) sortOrder = C.DSO;

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
                C.T, C.TiP,
                C.PID, prof_id);

        ContentValues cv = new ContentValues();
        if (name != null) cv.put(C.Dsc, name);
        cv.put(C.EN, isEnabled);

        beginTransaction();
        try {
            int count = mDb.update(PROFILES_TABLE, cv, where, null);
            setTransactionSuccessful();
            return count;
        } finally {
            endTransaction();
        }
    }

    /**
     * @param isEnabled Timed action is enable flag to update.
     * @return Number of rows affected. 1 on success, 0 on failure.
     */
    public int updateTimedAction(long action_id, boolean isEnabled) {

        String where = String.format("%s=%d AND %s=%d",
                C.T, C.TiTA,
                C.PID, action_id);

        ContentValues cv = new ContentValues();
        cv.put(C.EN, isEnabled);

        beginTransaction();
        try {
            int count = mDb.update(PROFILES_TABLE, cv, where, null);
            setTransactionSuccessful();
            return count;
        } finally {
            endTransaction();
        }
    }

    /**
     * @param name Timed action description to update, if not null.
     * @return Number of rows affected. 1 on success, 0 on failure.
     */
    public int updateTimedAction(long action_id, int hourMin, int days,
            String actions, String description) {

        String where = String.format("%s=%d AND %s=%d",
                C.T, C.TiTA,
                C.PID, action_id);

        ContentValues cv = new ContentValues();
        cv.put(C.HM, hourMin);
        cv.put(C.D, days);
        cv.put(C.A, actions);
        if (description != null) cv.put(C.Dsc, description);

        beginTransaction();
        try {
            int count = mDb.update(PROFILES_TABLE, cv, where, null);
            setTransactionSuccessful();
            return count;
        } finally {
            endTransaction();
        }
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

            pid = pid & (~C.AMk);

            // DELETE FROM profiles WHERE prof_id >= 65536 AND prof_id < 65536+65535
            String where = String.format("%s>=%d AND %s<%d",
                    C.PID, pid,
                    C.PID, pid + C.AMk);

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
            // DELETE FROM profiles WHERE T=2 AND _id=65537
            String where = String.format("%s=%d AND %s=%d",
                    C.T, C.TiTA,
                    C._ID, row_id);

            int count = mDb.delete(PROFILES_TABLE, where, null);
            setTransactionSuccessful();
            return count;
        } finally {
            endTransaction();
        }
    }


    // ----------------------------------

    /** Convenience helper to open/create/update the database */
    private class _DBH extends SQLiteOpenHelper {

        public _DBH(Context context,
                String db_name,
                int version) {
			super(context, db_name, null /* cursor factory */, version);
		}

		@Override
        public void onCreate(SQLiteDatabase db) {
            SQLiteDatabase old_mDb = mDb;
            mDb = db;
            onResetTables();
            initDefaultProfiles();
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
     * Called by {@link _DBH} to reset the tables.
     */
    private void onResetTables() {
        // hand over that chocolate and nobody gets hurt!
        mDb.execSQL(String.format("DROP TABLE IF EXISTS %s;", PROFILES_TABLE));

        mDb.execSQL(String.format("CREATE TABLE %s "
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
                C._ID,
                C.T,
                C.Dsc,
                C.EN,
                C.PID,
                C.HM,
                C.D,
                C.A,
                C.NMS));
    }

    /**
     * Called by {@link _DBH} when the database has just been
     * created to initialize it with initial data. It's safe to use
     * {@link PDB#insertProfile(String, boolean)} or
     * {@link PDB#insertTimedAction(String, boolean, int, int, String, long)}
     * at that point.
     */
    private void initDefaultProfiles() {
        long pindex = insertProfile(0, "Weekdaze", true /*isEnabled*/);
        long action = insertTimedAction(pindex, 0,
                7*60+0,             //hourMin
                C.MO + C.TU + C.WE + C.TH,
                "RR,VV",            //actions
                0                   //nextMs
                );
        insertTimedAction(pindex, action,
                20*60+0,             //hourMin
                C.MO + C.TU + C.WE + C.TH,
                "RM,VV",            //actions
                0                   //nextMs
                );

        pindex = insertProfile(0, "Party Time", true /*isEnabled*/);
        action = insertTimedAction(pindex, 0,
                9*60+0,             //hourMin
                C.FR + C.SA,
                "RR",               //actions
                0                   //nextMs
                );
        insertTimedAction(pindex, action,
                22*60+0,             //hourMin
                C.FR + C.SA,
                "RM,VV",            //actions
                0                   //nextMs
                );

        pindex = insertProfile(0, "Sleeping-In", true /*isEnabled*/);
        action = insertTimedAction(pindex, 0,
                10*60+30,            //hourMin
                C.SU,
                "RR",               //actions
                0                   //nextMs
                );
        insertTimedAction(pindex, action,
                21*60+0,             //hourMin
                C.SU,
                "RM,VV",            //actions
                0                   //nextMs
                );
    }

    /**
     * Some simple profiles for me
     */
    private void initRalfProfiles() {
        long pindex = insertProfile(0, "Ralf Week", true /*isEnabled*/);
        long action = insertTimedAction(pindex, 0,
                9*60+0,             //hourMin
                C.MO + C.TU + C.WE + C.TH + C.FR + C.SA + C.SU,
                "RR,VV,B75,U1",     //actions
                0                   //nextMs
                );
        insertTimedAction(pindex, action,
                21*60+0,             //hourMin
                C.MO + C.TU + C.WE + C.TH + C.FR + C.SA + C.SU,
                "RM,VN,B1,U0",      //actions
                0                   //nextMs
                );

        pindex = insertProfile(0, "Ring Volume", true /*isEnabled*/);
        action = insertTimedAction(pindex, 0,
                13*60+30,           //hourMin
                C.SA + C.SU,
                "G25",              //actions
                0                   //nextMs
                );
        action = insertTimedAction(pindex, action,
                16*60+0,            //hourMin
                C.SA + C.SU,
                "G85",              //actions
                0                   //nextMs
                );
        insertTimedAction(pindex, action,
                10*60+0,            //hourMin
                C.MO,
                "G100",             //actions
                0                   //nextMs
                );

    }

    // --------------

    /**
     * Labels of the reset profiles choices.
     * Default is index 0.
     */
    public String[] getResetLabels() {
        return new String[] {
            "Fun Profiles",
            "Empty Profiles",
            "Ralf Profiles"
        };
    }

    /**
     * Reset profiles according to choices.
     *
     * @param labelIndex An index from the {@link #getResetLabels()} array.
     */
    public void resetProfiles(int labelIndex) {

        if (DEBUG) Log.d(TAG, "Reset profiles: " + Integer.toString(labelIndex));

        beginTransaction();
        try {
            // empty tables
            onResetTables();

            switch(labelIndex) {
            case 0:
                initDefaultProfiles();
                break;
            case 1:
                // empty profiles list, already done.
                // pass
                break;
            case 2:
                initRalfProfiles();
                break;
            }

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    // --------------

    public void removeAllActionExecFlags() {
        // generates WHERE type=2 (aka action) AND enable=1
        String where = String.format("%s=%d AND %s=%d",
                C.T, C.TiTA,
                C.EN, 1);

        ContentValues values = new ContentValues(1);
        values.put(C.EN, false);

        beginTransaction();
        try {
            mDb.update(
                    PROFILES_TABLE, // table
                    values,         // values
                    where,          // whereClause
                    null            // whereArgs
                    );

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

    /**
     * Returns the list of all profiles, dumped in a structure that is
     * mostly for debugging. It is not designed to be read back for restoring
     * although we could change it to be later.
     */
    public String[] getProfilesDump() {
        Cursor c = null;
        try {
            c = mDb.query(
                    PROFILES_TABLE,                         // table
                    null,                                   // *ALL* columns
                    null,                                   // selection
                    null,                                   // selectionArgs
                    null,                                   // groupBy
                    null,                                   // having
                    null                                    // orderBy
                    );

            int colType      = c.getColumnIndexOrThrow(C.T);
            int colDesc      = c.getColumnIndexOrThrow(C.Dsc);
            int colIsEnabled = c.getColumnIndexOrThrow(C.EN);
            int colProfId    = c.getColumnIndexOrThrow(C.PID);
            int colHourMin   = c.getColumnIndexOrThrow(C.HM);
            int colDays      = c.getColumnIndexOrThrow(C.D);
            int colActions   = c.getColumnIndexOrThrow(C.A);
            int colNextMs    = c.getColumnIndexOrThrow(C.NMS);

            String[] summaries = new String[c.getCount()];
            StringBuilder sb = new StringBuilder();

            if (c.moveToFirst()) {
                int i = 0;
                do {
                    String desc = c.getString(colDesc);
                    String actions = c.getString(colActions);
                    int enable  = c.getInt(colIsEnabled);
                    int type    = c.getInt (colType);
                    long profId = c.getLong(colProfId);
                    int hourMin = c.getInt (colHourMin);
                    int days    = c.getInt (colDays);
                    long nextMs = c.getLong(colNextMs);

                    sb.setLength(0);

                    if (type == C.TiTA) {
                        sb.append("- ");
                    }

                    // Format: { profile/action prof-index:action-index enable/active }
                    sb.append(String.format("{ %1$s 0x%2$04x:%3$04x %4$s } ",
                            type == C.TiP ? "P" :
                                type == C.TiTA ? "A" :
                                    Integer.toString(type),
                            profId >> C.PS,
                            profId & C.AMk,
                            type == C.TiP ?
                                    (enable == 0 ? "D" : /*1*/ "E") : // profile: enable/disabled
                                    (enable == 0 ? "I" :              // action: inactive/prev/next
                                        (enable == 1 ? "P" : /*2*/ "N"))
                            ));

                    // Description profile:user name, action: display summary
                    sb.append(desc);

                    if (type == C.TiTA) {
                        // Format: [ d:days-bitfield, hm:hour*60+min, a:actions/-, n:next MS ]
                        sb.append(String.format(" [ d:%1$01x, hm:%2$04d, a:'%3$s', n:%d ]",
                                days,
                                hourMin,
                                actions == null ? "-" : actions,
                                nextMs
                                ));
                    }

                    sb.append("\n");

                    summaries[i++] = sb.toString();

                } while (c.moveToNext());
            }

            return summaries;
        } finally {
            if (c != null) c.close();
        }
    }

    /**
     * Returns the list of all enabled profiles.
     * This is a list of profiles indexes.
     * Can return an empty list, but not null.
     */
    public long[] getEnabledProfiles() {

        // generates WHERE type=1 (aka profile) AND enable=1
        String where = String.format("%s=%d AND %s=%d",
                C.T, C.TiP,
                C.EN, 1);

        Cursor c = null;
        try {
            c = mDb.query(
                    PROFILES_TABLE,                         // table
                    new String[] { C.PID },    // columns
                    where,                                  // selection
                    null,                                   // selectionArgs
                    null,                                   // groupBy
                    null,                                   // having
                    null                                    // orderBy
                    );

            int profIdColIndex = c.getColumnIndexOrThrow(C.PID);

            long[] indexes = new long[c.getCount()];

            if (c.moveToFirst()) {
                int i = 0;
                do {
                    indexes[i++] = c.getLong(profIdColIndex) >> C.PS;
                } while (c.moveToNext());
            }

            return indexes;
        } finally {
            if (c != null) c.close();
        }
    }

    /**
     * Returns the list of timed actions that should be activated "now",
     * as defined by the given day and hour/minute and limited to the
     * given list of profiles.
     * <p/>
     * Returns a list of action prof_id (ids, not indexes) or null.
     * prof_indexes is a list of profile indexes (not ids) that are currently
     * enabled.
     * <p/>
     * Synopsis:
     * - If there are no profiles activated, abort. That is we support prof_ids
     *   being an empty list.
     * - Selects actions which action_day & given_day != 0 (bitfield match)
     * - Selects all actions for that day, independently of the hourMin.
     *   The hourMin check is done after on the result.
     * - Can return an empty list, but not null.
     */
    public _AI[] getDayActivableActions(int hourMin, int day, long[] prof_indexes) {
        if (prof_indexes.length < 1) return null;

        StringBuilder profList = new StringBuilder();
        for (long prof_index : prof_indexes) {
            if (profList.length() > 0) profList.append(",");
            profList.append(Long.toString(prof_index));
        }

        // generates WHERE type=2 (aka action)
        //           AND hourMin <= targetHourMin
        //           AND days & MASK != 0
        //           AND prof_id >> SHIFT IN (profList)
        String where = String.format("%s=%d AND (%s <= %d) AND (%s & %d) != 0 AND (%s >> %d) IN (%s)",
                C.T, C.TiTA,
                C.HM, hourMin,
                C.D, day,
                C.PID, C.PS, profList);

        // ORDER BY hourMin DESC
        String orderBy = String.format("%s DESC", C.HM);

        if (DEBUG) Log.d(TAG, "Get actions: WHERE " + where + " ORDER BY " + orderBy);


        Cursor c = null;
        try {
            c = mDb.query(
                    PROFILES_TABLE,                         // table
                    new String[] { C._ID, C.HM, C.A },    // columns
                    where,                                  // selection
                    null,                                   // selectionArgs
                    null,                                   // groupBy
                    null,                                   // having
                    orderBy
                    );

            int rowIdColIndex = c.getColumnIndexOrThrow(C._ID);
            int hourMinColIndex = c.getColumnIndexOrThrow(C.HM);
            int actionsColInfo = c.getColumnIndexOrThrow(C.A);

            // Above we got the list of all actions for the requested day
            // that happen before the requested hourMin, in descending time
            // order, e.g. the most recent action is first in the list.
            //
            // We want to return the first action found. There might be more
            // than one action with the same time, so return them all.

            ArrayList<_AI> infos = new ArrayList<_AI>();
            if (c.moveToFirst()) {
                int firstHourMin = c.getInt(hourMinColIndex);
                do {
                    infos.add(new _AI(
                            c.getLong(rowIdColIndex),
                            firstHourMin,  // all actions have the same time
                            c.getString(actionsColInfo)));

                    if (DEBUG) Log.d(TAG, String.format("ActivableAction: day %d, hourMin %04d", day, firstHourMin));

                } while (c.moveToNext() && c.getInt(hourMinColIndex) == firstHourMin);
            }

            return infos.toArray(new _AI[infos.size()]);
        } finally {
            if (c != null) c.close();
        }
    }

    /**
     * Invokes {@link #getDayActivableActions(int, int, long[])} for the current
     * day. If nothing is found, look at the 6 previous days to see if we can
     * find an action.
     */
    public _AI[] getWeekActivableActions(int hourMin, int day, long[] prof_indexes) {
        _AI[] actions = null;

        // Look for the last enabled action for day.
        // If none was found, loop up to 6 days before and check the last
        // action before 24:00.
        for (int k = 0; k < 7; k++) {
            actions = getDayActivableActions(hourMin, day, prof_indexes);

            if (actions != null && actions.length > 0) {
                break;
            }

            // Switch to previous day and loop from monday to sunday as needed.
            day = day >> 1;
            if (day == 0) day = C.SU;

            // Look for anything "before the end of the day". Since we
            // want to match 23:59 we need to add one minute thus 24h00
            // is our target.
            hourMin = 24*60 + 0;
        }

        return actions;
    }

    /**
     * Given a day and an hourMin time, try to find the first event that happens
     * after that timestamp. If nothing if found on the day, look up to 6 days
     * ahead.
     *
     * prof_indexes is a list of profile indexes (not ids) that are currently
     * enabled.
     *
     * @return The number of minutes from the given timestamp to the next event
     *  or 0 if there's no such event ("now" is not a valid next event)
     */
    public int getWeekNextEvent(int hourMin, int day, long[] prof_indexes,
            _AI[] out_actions) {
        // First try to find something today that is past the requested time.
        _AI found = getDayNextEvent(hourMin, day, prof_indexes);
        if (found != null) {
            out_actions[0] = found;
            int delta = found.mHourMin - hourMin;
            if (delta > 0) {
                return delta;
            }
        }

        // Otherwise look for the 6 days of events
        int minutes = 24*60 - hourMin;
        for(int k = 1; k < 7; k++, minutes += 24*60) {
            // Switch to next day. Loop from sunday back to monday.
            day = day << 1;
            if (day > C.SU) day = C.MO;

            found = getDayNextEvent(-1 /*One minute before 00:00*/, day, prof_indexes);
            if (found != null) {
                out_actions[0] = found;
                return minutes + found.mHourMin;
            }
        }

        return 0;
    }

    /**
     * Given a day and an hourMin time, try to find the first event that happens
     * after that timestamp on the singular day.
     *
     * prof_indexes is a list of profile indexes (not ids) that are currently
     * enabled.
     *
     * @return The hourMin of the event found (hourMin..23:59) or -1 if nothing found.
     *  If the return value is not -1, it is guaranteed to be greater than the
     *  given hourMin since we look for an event *past* this time.
     */
    private _AI getDayNextEvent(int hourMin, int day, long[] prof_indexes) {
        if (prof_indexes.length < 1) return null;

        StringBuilder profList = new StringBuilder();
        for (long prof_index : prof_indexes) {
            if (profList.length() > 0) profList.append(",");
            profList.append(Long.toString(prof_index));
        }

        // generates WHERE type=2 (aka action)
        //           AND hourMin > targetHourMin
        //           AND days & MASK != 0
        //           AND prof_id >> SHIFT IN (profList)
        String hourTest;
        if (hourMin == -1) {
            hourTest = String.format("%s >= 0", C.HM);
        } else {
            hourTest = String.format("%s > (%d)", C.HM, hourMin);
        }
        String where = String.format("%s=%d AND (%s) AND (%s & %d) != 0 AND (%s >> %d) IN (%s)",
                C.T, C.TiTA,
                hourTest,
                C.D, day,
                C.PID, C.PS, profList);

        // ORDER BY hourMin ASC
        String orderBy = String.format("%s ASC", C.HM);

        // LIMIT 1 (we only want the first result)
        String limit = "1";

        if (DEBUG) Log.d(TAG, "Get actions: WHERE " + where + " ORDER BY " + orderBy + " LIMIT " + limit);


        Cursor c = null;
        try {
            c = mDb.query(
                    PROFILES_TABLE,                         // table
                    new String[] { C._ID, C.HM, C.A },    // columns
                    where,                                  // selection
                    null,                                   // selectionArgs
                    null,                                   // groupBy
                    null,                                   // having
                    orderBy,
                    limit
                    );

            int rowIdColIndex = c.getColumnIndexOrThrow(C._ID);
            int hourMinColIndex = c.getColumnIndexOrThrow(C.HM);
            int actionColIndex = c.getColumnIndexOrThrow(C.A);

            if (c.moveToFirst()) {
                hourMin = c.getInt(hourMinColIndex);

                if (DEBUG) Log.d(TAG, String.format("NextEvent: day %d, hourMin %04d", day, hourMin));

                return new _AI(
                        c.getLong(rowIdColIndex),
                        hourMin,
                        c.getString(actionColIndex));
            }
        } finally {
            if (c != null) c.close();
        }

        return null;
    }


    /**
     * Struct that describes a Timed Action returned by
     * {@link PDB#getDayActivableActions(int, int, long[])}
     */
    public static class _AI {

        public final long mRowId;
        private final int mHourMin;
        public final String mActions;

        public _AI(long rowId, int hourMin, String actions) {
            mRowId = rowId;
            mHourMin = hourMin;
            mActions = actions;
        }

        @Override
        public String toString() {
            return String.format("Action<#.%d @%04d: %s>", mRowId, mActions);
        }
    }

    /**
     * Mark all the given actions as enabled for the given state.
     * Any previous actions with the given state are cleared.
     */
    public void markActionsEnabled(_AI[] actions, int state) {

        StringBuilder rowList = new StringBuilder();
        for (_AI info : actions) {
            if (rowList.length() > 0) rowList.append(",");
            rowList.append(Long.toString(info.mRowId));
        }

        // generates WHERE type=2 (aka action) AND _id in (profList)
        String where_set = String.format("%s=%d AND %s IN (%s)",
                C.T, C.TiTA,
                C._ID, rowList);

        ContentValues values_set = new ContentValues(1);
        values_set.put(C.EN, state);

        if (DEBUG) Log.d(TAG, "Mark actions: WHERE " + where_set);


        // generates WHERE type=2 (aka action) AND is_enabled == state
        String where_clear = String.format("%s=%d AND %s == %d",
                C.T, C.TiTA,
                C.EN, state);

        ContentValues values_clear = new ContentValues(1);
        values_clear.put(C.EN, C.AMD);

        beginTransaction();
        try {
            // clear previous marks
            mDb.update(
                    PROFILES_TABLE, // table
                    values_clear,   // values
                    where_clear,    // whereClause
                    null            // whereArgs
                    );

            // set new ones
            mDb.update(
                    PROFILES_TABLE, // table
                    values_set,     // values
                    where_set,      // whereClause
                    null            // whereArgs
                    );

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

}
