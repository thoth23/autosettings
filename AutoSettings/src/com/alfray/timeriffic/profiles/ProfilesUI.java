/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License GPLv3
 */

package com.alfray.timeriffic.profiles;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.alfray.timeriffic.R;

public class ProfilesUI extends Activity {

    private ListView mProfilesList;
    private ProfileCursorAdapter mAdapter;
    private LayoutInflater mLayoutInflater;
    private ProfilesDB mProfilesDb;

    private int mTypeColIndex;
    private int mDescColIndex;
    private int mEnableColIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.profiles_screen);
        mLayoutInflater = getLayoutInflater();
        
        mProfilesList = (ListView) findViewById(R.id.profilesList);
        
        mProfilesDb = new ProfilesDB();
        mProfilesDb.onCreate(this);
        Cursor cursor = mProfilesDb.query(
                -1, //id
                new String[] { 
                    Columns._ID,
                    Columns.TYPE, 
                    Columns.DESCRIPTION,
                    Columns.IS_ENABLED,
                    // enable these only if they are actually used here
                    //Columns.HOUR_MIN,
                    //Columns.DAYS,
                    //Columns.ACTIONS,
                    //Columns.NEXT_MS
                } , //projection
                null, //selection
                null, //selectionArgs
                null //sortOrder
                );

        mTypeColIndex = cursor.getColumnIndexOrThrow(Columns.TYPE);
        mDescColIndex = cursor.getColumnIndexOrThrow(Columns.DESCRIPTION);
        mEnableColIndex = cursor.getColumnIndexOrThrow(Columns.IS_ENABLED);

        mAdapter = new ProfileCursorAdapter(this, cursor);
        mProfilesList.setAdapter(mAdapter);
    }

    //--------------

    private class ProfileCursorAdapter extends CursorAdapter {

        private final static int TYPE_PROFILE = 0;
        private final static int TYPE_TIMED_ACTION = 1;
        

        public ProfileCursorAdapter(Context context, Cursor c) {
            super(context, c);
        }
        
        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }
        
        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            Cursor c = (Cursor) getItem(position);
            int type = c.getInt(mTypeColIndex);
            if (type == Columns.TYPE_IS_PROFILE)
                return TYPE_PROFILE;
            if (type == Columns.TYPE_IS_TIMED_ACTION)
                return TYPE_TIMED_ACTION;

            return IGNORE_ITEM_VIEW_TYPE;
        }

        // ---

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            int type = cursor.getInt(mTypeColIndex);
            if (type == Columns.TYPE_IS_PROFILE) {
                View v = mLayoutInflater.inflate(R.layout.profile_header, null);
                ProfileHeaderHolder h = new ProfileHeaderHolder(cursor, v);
                v.setTag(h);
                h.setUiData(cursor);
                return v;
            }

            if (type == Columns.TYPE_IS_TIMED_ACTION) {
                View v = mLayoutInflater.inflate(R.layout.timed_action, null);
                TimedActionHolder h = new TimedActionHolder(cursor, v);
                v.setTag(h);
                h.setUiData(cursor);
                return v;
            }

            return null;
        }
        
        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            int type = cursor.getInt(mTypeColIndex);
            if (type == Columns.TYPE_IS_PROFILE) {
                ProfileHeaderHolder h = (ProfileHeaderHolder) view.getTag();
                h.setUiData(cursor);
            } else if (type == Columns.TYPE_IS_TIMED_ACTION) {
                TimedActionHolder h = ( TimedActionHolder) view.getTag();
                h.setUiData(cursor);
            }
        }
    }
    
    //--------------


    private class ProfileHeaderHolder {
        
        public final CheckBox mCheckName;
        public final ImageButton mButton;

        public ProfileHeaderHolder(Cursor cursor, View view) {
            
            mCheckName = (CheckBox) view.findViewById(R.id.profileTitle);
            mButton = (ImageButton) view.findViewById(R.id.profileButton);
        }
        
        public void setUiData(Cursor cursor) {
            mCheckName.setText(cursor.getString(mDescColIndex));
            mCheckName.setChecked(cursor.getInt(mEnableColIndex) != 0);
        }
    }
    
    private class TimedActionHolder {
        
        public final TextView mDescription;

        public TimedActionHolder(Cursor cursor, View view) {
            mDescription = (TextView) view.findViewById(R.id.timedActionTitle);
        }

        public void setUiData(Cursor cursor) {
            mDescription.setText(cursor.getString(mDescColIndex));
        }
    }
}
