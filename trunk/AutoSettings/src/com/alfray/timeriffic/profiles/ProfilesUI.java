/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License GPLv3
 */

package com.alfray.timeriffic.profiles;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.AbsListView.RecyclerListener;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.prefs.PrefsValues;

public class ProfilesUI extends Activity {

    private ListView mProfilesList;
    private ProfileCursorAdapter mAdapter;
    private LayoutInflater mLayoutInflater;
    private ProfilesDB mProfilesDb;

    private int mTypeColIndex;
    private int mDescColIndex;
    private int mEnableColIndex;
    private PrefsValues mPrefsValues;
    private Drawable mGreenDot;
    private Drawable mGrayDot;

    /**
     * Called when the activity is created.
     * <p/>
     * Initializes row indexes and buttons.
     * Profile list & db is initialized in {@link #onResume()}.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.profiles_screen);
        mLayoutInflater = getLayoutInflater();

        mPrefsValues = new PrefsValues(this);
        mGreenDot = getResources().getDrawable(R.drawable.green_dot);
        mGrayDot = getResources().getDrawable(R.drawable.gray_dot);
        
        initButtons();
    }

    /**
     * Called when activity is resume, or just after creation.
     * <p/>
     * Initializes the profile list & db.
     */
    @Override
    protected void onResume() {
        super.onResume();
        initProfileList();
    }

    /**
     * Called when the activity is getting paused. It might get destroyed
     * at any point.
     * <p/>
     * Reclaim all views (so that they tag's cursor can be cleared).
     * Destroys the db connection.
     */
    @Override
    protected void onPause() {
        super.onPause();
        ArrayList<View> views = new ArrayList<View>();
        mProfilesList.reclaimViews(views);
        mProfilesDb.onDestroy();
    }

    /**
     * Initializes the profile list widget with a cursor adapter.
     * Creates a db connection.
     */
    private void initProfileList() {
        mProfilesList = (ListView) findViewById(R.id.profilesList);
        mProfilesList.setRecyclerListener(new ProfileRecyclerListener());
        
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

    /**
     * Initializes the list-independent buttons: global toggle, check now.
     */
    private void initButtons() {
        final ToggleButton globalToggle = (ToggleButton) findViewById(R.id.global_toggle);

        globalToggle.setChecked(mPrefsValues.enableService());

        globalToggle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mPrefsValues.setEnabledService(globalToggle.isChecked());
            }
            
        });
        
        
    }

    //--------------

    /**
     * A custom {@link CursorAdapter} that can provide the two views we
     * need: the profile header and the timed action entry.
     * <p/>
     * For each new view, the tag is set to either {@link ProfileHeaderHolder}
     * or {@link TimedActionHolder}, a subclass of {@link CursorHolder}.
     * <p/>
     * When a view is reused, it's tag is reused with a new cursor by using
     * {@link CursorHolder#setUiData(Cursor)}. This also updates the view
     * with the data from the cursor.
     * <p/>
     * When a view is recycled/reclaimed, it's tag is cleared by the
     * {@link ProfileRecyclerListener}.
     */
    private class ProfileCursorAdapter extends CursorAdapter {

        /** View type is a profile header. */
        private final static int TYPE_PROFILE = 0;
        /** View type is a timed action item. */
        private final static int TYPE_TIMED_ACTION = 1;
        

        /**
         * Creates a new {@link ProfileCursorAdapter} for that cursor
         * and context.
         */
        public ProfileCursorAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }
        
        /**
         * All items are always enabled in this view.
         */
        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        /**
         * All items are always enabled in this view.
         */
        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        /**
         * This adapter can serve 2 view types.
         */
        @Override
        public int getViewTypeCount() {
            return 2;
        }

        /**
         * View types served are either {@link #TYPE_PROFILE} or
         * {@link #TYPE_TIMED_ACTION}. This is based on the value of
         * {@link Columns#TYPE} in the cursor.
         */
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

        /**
         * Depending on the value of {@link Columns#TYPE} in the cursor,
         * this inflates either a profile_header or a timed_action resource.
         * <p/>
         * It then associates the tag with a new {@link ProfileHeaderHolder}
         * or {@link TimedActionHolder} and initializes the holder using
         * {@link CursorHolder#setUiData(Cursor)}.
         * 
         */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            View v = null;
            CursorHolder h = null;
            
            int type = cursor.getInt(mTypeColIndex);
            if (type == Columns.TYPE_IS_PROFILE) {
                v = mLayoutInflater.inflate(R.layout.profile_header, null);
                h = new ProfileHeaderHolder(cursor, v);
            } else if (type == Columns.TYPE_IS_TIMED_ACTION) {
                v = mLayoutInflater.inflate(R.layout.timed_action, null);
                h = new TimedActionHolder(cursor, v);
            }
            if (v != null) {
                v.setTag(h);
                h.setUiData(cursor);
            }
            return v;
        }
        
        /**
         * To recycle a view, we just re-associate its tag using
         * {@link CursorHolder#setUiData(Cursor)}.
         */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            int type = cursor.getInt(mTypeColIndex);
            if (type == Columns.TYPE_IS_PROFILE ||
                    type == Columns.TYPE_IS_TIMED_ACTION) {
                CursorHolder h = (CursorHolder) view.getTag();
                h.setUiData(cursor);
            }
        }
    }
    
    //--------------

    /**
     * This {@link RecyclerListener} is attached to the profile list to
     * call {@link CursorHolder#clearCursor()} of the tags of the reclaimed
     * views. This should ensure that not dangling cursor reference exists.
     */
    private class ProfileRecyclerListener implements RecyclerListener {
        @Override
        public void onMovedToScrapHeap(View view) {
            Object tag = view.getTag();
            if (tag instanceof CursorHolder) {
                ((CursorHolder) tag).clearCursor(); 
            }
        }
    }

    /**
     * A base holder class that keeps tracks of the current cursor.
     */
    private abstract class CursorHolder {
        
        /** The current cursor associated with that holder.
         * It is null if the view is not associated with a cursor anymore.
         */
        public Cursor mCursor;

        public void clearCursor() {
            mCursor = null;
        }
        
        public void setUiData(Cursor cursor) {
            mCursor = cursor;
        }
    }

    /**
     * The holder for a profile header row.
     */
    private class ProfileHeaderHolder extends CursorHolder {
        
        public final CheckBox mCheckName;

        public ProfileHeaderHolder(Cursor cursor, View view) {
            mCheckName = (CheckBox) view.findViewById(R.id.profileTitle);
        }
        
        @Override
        public void setUiData(Cursor cursor) {
            super.setUiData(cursor);
            mCheckName.setText(cursor.getString(mDescColIndex));
            mCheckName.setChecked(cursor.getInt(mEnableColIndex) != 0);
        }
    }

    /**
     * The holder for a timed action row.
     */
    private class TimedActionHolder extends CursorHolder {
        
        public final TextView mDescription;

        public TimedActionHolder(Cursor cursor, View view) {
            mDescription = (TextView) view.findViewById(R.id.timedActionTitle);
        }

        @Override
        public void setUiData(Cursor cursor) {
            super.setUiData(cursor);
            mDescription.setText(cursor.getString(mDescColIndex));
            mDescription.setCompoundDrawablesWithIntrinsicBounds(
                    cursor.getInt(mEnableColIndex) == 0 ? mGrayDot : mGreenDot,
                    null, //top
                    null, //right
                    null //bottom
                    );
        }
    }
}
