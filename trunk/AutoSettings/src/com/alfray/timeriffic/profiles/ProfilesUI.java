/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License TBD
 */

package com.alfray.timeriffic.profiles;

import java.util.ArrayList;
import java.util.HashSet;

import android.app.Activity;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alfray.timeriffic.R;

public class ProfilesUI extends Activity {

    private ListView mProfilesList;
    private ArrayList<Profile> mProfiles;
    private ProfileListAdapter mAdapter;
    private LayoutInflater mLayoutInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.profiles_screen);
        mLayoutInflater = getLayoutInflater();
        
        mProfiles = new ArrayList<Profile>();
        addDummyData();

        mProfilesList = (ListView) findViewById(R.id.profilesList);
        mAdapter = new ProfileListAdapter();
        mProfilesList.setAdapter(mAdapter);
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
            /*
            Object o = getItem(position);
            if (o instanceof TimedAction) o = ((TimedAction)o).getProfile();
            if (o instanceof Profile) {
                return ((Profile)o).isEnabled();
            }
            return false;
            */
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
                ((ProfileHeaderHolder)v.getTag()).setProfile((Profile) o);
            }

            if (o instanceof TimedAction) {
                if (convertView == null ||
                        !(convertView.getTag() instanceof TimedActionHolder)) {
                    v = mLayoutInflater.inflate(R.layout.timed_action, null);
                    v.setTag(new TimedActionHolder(v));
                }
                ((TimedActionHolder)v.getTag()).setProfile((TimedAction) o);
            }
            
            return v;
        }
        
    }
    
    private static class ProfileHeaderHolder {
        
        public Profile mProfile;
        public CheckBox mCheckName;
        public ImageButton mButton;

        public ProfileHeaderHolder(View view) {
            mCheckName = (CheckBox) view.findViewById(R.id.profileTitle);
            mButton = (ImageButton) view.findViewById(R.id.profileButton);
        }
        
        public void setProfile(Profile profile) {
            mProfile = profile;
            mCheckName.setText(profile.getName());
            mCheckName.setEnabled(profile.isEnabled());
        }
        
    }
    
    private static class TimedActionHolder {
        
        public TimedAction mTimedAction;
        public TextView mDescription;

        public TimedActionHolder(View view) {
            mDescription = (TextView) view.findViewById(R.id.timedActionTitle);
        }

        public void setProfile(TimedAction timedAction) {
            mTimedAction = timedAction;
            mDescription.setText(timedAction.toString());
        }
    }
}
