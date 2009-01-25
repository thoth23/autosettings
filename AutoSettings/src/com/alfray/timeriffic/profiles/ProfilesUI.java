/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License TBD
 */

package com.alfray.timeriffic.profiles;

import java.util.ArrayList;

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
    private ArrayList<Profile> mProfiles;
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
        
        mProfiles = new ArrayList<Profile>();
        addDummyData();

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
        
        /*
        mAdapter = new ProfileListAdapter();
        */
    }

    private void addDummyData() {
        Profile p = new Profile();
        mProfiles.add(p);
        p.getActions().add(new TimedAction(p));
        p = new Profile();
        mProfiles.add(p);
        p.getActions().add(new TimedAction(p));
        p.getActions().add(new TimedAction(p));
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
            // throw?
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
                View v = mLayoutInflater.inflate(R.layout.timed_action, null);
                TimedActionHolder h = ( TimedActionHolder) view.getTag();
                h.setUiData(cursor);
            }
        }
    }
    
    //--------------

    /*
    private class ProfileListAdapter implements ListAdapter {

        private final static int TYPE_PROFILE = 0;
        private final static int TYPE_TIMED_ACTION = 1;
        
        private final HashSet<DataSetObserver> mObservers = new HashSet<DataSetObserver>();
        
        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public int getCount() {
            int n = 0;
            for(Profile p : mProfiles) {
                // header + number of actions
                n += 1 + p.getActions().size();
            }
            return n;
        }

        @Override
        public Object getItem(int position) {
            int n = 0;
            for(Profile p : mProfiles) {
                // is it the header?
                if (n == position) return p;
                n++;
                for (TimedAction a : p.getActions()) {
                    if (n == position) return a;
                    n++;
                }
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            Object o = getItem(position);
            if (o instanceof TimedAction)
                return ((TimedAction)o).getId();
            if (o instanceof Profile)
                return ((Profile)o).getId();
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            return mProfiles.size() == 0;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            mObservers.add(observer);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            mObservers.remove(observer);
        }

        @Override
        public int getItemViewType(int position) {
            Object o = getItem(position);
            if (o instanceof TimedAction)
                return TYPE_TIMED_ACTION;
            if (o instanceof Profile)
                return TYPE_PROFILE;
            return IGNORE_ITEM_VIEW_TYPE;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Object o = getItem(position);
            View v = null;

            if (o instanceof Profile) {
                if (convertView == null ||
                        !(convertView.getTag() instanceof ProfileHeaderHolder)) {
                    v = mLayoutInflater.inflate(R.layout.profile_header, null);
                    v.setTag(new ProfileHeaderHolder(v));
                }
                ((ProfileHeaderHolder)v.getTag()).setUiData((Profile) o);
            }

            if (o instanceof TimedAction) {
                if (convertView == null ||
                        !(convertView.getTag() instanceof TimedActionHolder)) {
                    v = mLayoutInflater.inflate(R.layout.timed_action, null);
                    v.setTag(new TimedActionHolder(v));
                }
                ((TimedActionHolder)v.getTag()).setUiData((TimedAction) o);
            }
            
            return v;
        }
        
    }
    */

    private class ProfileHeaderHolder {
        
        /** @deprecated */ public Profile mProfile;

        public final CheckBox mCheckName;
        public final ImageButton mButton;

        public ProfileHeaderHolder(Cursor cursor, View view) {
            
            mCheckName = (CheckBox) view.findViewById(R.id.profileTitle);
            mButton = (ImageButton) view.findViewById(R.id.profileButton);
        }
        
        public void setUiData(Profile profile) {
            mProfile = profile;
            mCheckName.setText(profile.getName());
            mCheckName.setEnabled(profile.isEnabled());
        }

        public void setUiData(Cursor cursor) {
            mCheckName.setText(cursor.getString(mDescColIndex));
            mCheckName.setChecked(cursor.getInt(mEnableColIndex) != 0);
        }
    }
    
    private class TimedActionHolder {
        
        /** @deprecated */ public TimedAction mTimedAction;

        public final TextView mDescription;

        public TimedActionHolder(Cursor cursor, View view) {
            mDescription = (TextView) view.findViewById(R.id.timedActionTitle);
        }

        public void setUiData(TimedAction timedAction) {
            mTimedAction = timedAction;
            mDescription.setText(timedAction.toString());
        }

        public void setUiData(Cursor cursor) {
            mDescription.setText(cursor.getString(mDescColIndex));
        }
    }
}
