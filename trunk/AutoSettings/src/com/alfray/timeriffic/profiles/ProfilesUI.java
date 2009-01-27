/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License GPLv3
 */

package com.alfray.timeriffic.profiles;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.AbsListView.RecyclerListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.prefs.PrefsValues;

public class ProfilesUI extends Activity {

    private static final String TAG = "ProfilesUI";
    
    private ListView mProfilesList;
    private ProfileCursorAdapter mAdapter;
    private LayoutInflater mLayoutInflater;
    private ProfilesDB mProfilesDb;

    private int mIdColIndex;
    private int mTypeColIndex;
    private int mDescColIndex;
    private int mEnableColIndex;
    private PrefsValues mPrefsValues;
    private Drawable mGreenDot;
    private Drawable mGrayDot;
    private Drawable mCheckOn;
    private Drawable mCheckOff;

    private SparseArray<AlertDialog.Builder> mTempDialogList = new SparseArray<AlertDialog.Builder>();
    private int mNextTempDialogId = 0;

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
        mCheckOn = getResources().getDrawable(R.drawable.btn_check_on);
        mCheckOff = getResources().getDrawable(R.drawable.btn_check_off);
        
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

    @Override
    protected Dialog onCreateDialog(int id) {
        return mTempDialogList.get(id).create();
    }
    
    private void removeTempDialog(int index) {
        mTempDialogList.remove(index);
        removeDialog(index);
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

        mIdColIndex = cursor.getColumnIndexOrThrow(Columns._ID);
        mTypeColIndex = cursor.getColumnIndexOrThrow(Columns.TYPE);
        mDescColIndex = cursor.getColumnIndexOrThrow(Columns.DESCRIPTION);
        mEnableColIndex = cursor.getColumnIndexOrThrow(Columns.IS_ENABLED);

        mAdapter = new ProfileCursorAdapter(this, cursor);
        mProfilesList.setAdapter(mAdapter);
        
        mProfilesList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, String.format("onItemClick: pos %d, id %d", position, id));
                BaseHolder h = null;
                h = getHolderAtPosition(null, position);
                if (h != null) h.onItemSelected();
            }
        });

        mProfilesList.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
                Log.d(TAG, "onCreateContextMenu");
                BaseHolder h = null;
                h = getHolderAtPosition(menuInfo, -1);
                if (h != null) h.onCreateContextMenu(menu);
            }
        });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenuInfo info = item.getMenuInfo();
        BaseHolder h = getHolderAtPosition(info, -1);
        if (h != null) {
            h.onContextMenuSelected(item);
            return true;
        }
        
        return super.onContextItemSelected(item);
    }

    private BaseHolder getHolderAtPosition(ContextMenuInfo menuInfo, int position) {
        if (menuInfo instanceof AdapterContextMenuInfo) {
            position = ((AdapterContextMenuInfo) menuInfo).position;
        }
        if (position >= 0 && position < mProfilesList.getChildCount()) {
            Object item = mProfilesList.getChildAt(position);
            if (item instanceof View) {
                Object tag = ((View) item).getTag();
                if (tag instanceof BaseHolder) {
                    return (BaseHolder) tag;
                }
            }
        }
        return null;
    }

    /**
     * Initializes the list-independent buttons: global toggle, check now.
     */
    private void initButtons() {
        final ToggleButton globalToggle = (ToggleButton) findViewById(R.id.global_toggle);

        globalToggle.setChecked(mPrefsValues.enableService());

        globalToggle.setOnClickListener(new View.OnClickListener() {
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
     * or {@link TimedActionHolder}, a subclass of {@link BaseHolder}.
     * <p/>
     * When a view is reused, it's tag is reused with a new cursor by using
     * {@link BaseHolder#setUiData(Cursor)}. This also updates the view
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
         * {@link BaseHolder#setUiData(Cursor)}.
         * 
         */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            View v = null;
            BaseHolder h = null;
            
            int type = cursor.getInt(mTypeColIndex);
            if (type == Columns.TYPE_IS_PROFILE) {
                v = mLayoutInflater.inflate(R.layout.profile_header, null);
                h = new ProfileHeaderHolder(v);
            } else if (type == Columns.TYPE_IS_TIMED_ACTION) {
                v = mLayoutInflater.inflate(R.layout.timed_action, null);
                h = new TimedActionHolder(v);
            }
            if (v != null) {
                v.setTag(h);
                h.setUiData(cursor);
            }
            return v;
        }
        
        /**
         * To recycle a view, we just re-associate its tag using
         * {@link BaseHolder#setUiData(Cursor)}.
         */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            int type = cursor.getInt(mTypeColIndex);
            if (type == Columns.TYPE_IS_PROFILE ||
                    type == Columns.TYPE_IS_TIMED_ACTION) {
                BaseHolder h = (BaseHolder) view.getTag();
                h.setUiData(cursor);
            }
        }
    }
    
    //--------------

    /**
     * This {@link RecyclerListener} is attached to the profile list to
     * call {@link BaseHolder#clearCursor()} of the tags of the reclaimed
     * views. This should ensure that not dangling cursor reference exists.
     */
    private class ProfileRecyclerListener implements RecyclerListener {
        @Override
        public void onMovedToScrapHeap(View view) {
            Object tag = view.getTag();
            if (tag instanceof BaseHolder) {
                ((BaseHolder) tag).clearCursor(); 
            }
        }
    }

    //--------------

    /**
     * A base holder class that keeps tracks of the current cursor
     * and the common widgets of the two derived holders.
     */
    private abstract class BaseHolder {
        
        /**
         * The current cursor associated with that holder.
         * It is null if the view is not associated with a cursor anymore.
         */
        private Cursor mCursor;

        /**
         * The text view that holds the title or description as well
         * as the "check box".
         */
        private final TextView mDescription;

        public BaseHolder(View view) {
            mDescription = (TextView) view.findViewById(R.id.description);
        }
        
        public Cursor getCursor() {
            return mCursor;
        }
        
        public void clearCursor() {
            mCursor = null;
        }
        
        protected void setUiData(Cursor cursor,
                String description,
                Drawable state) {
            mCursor = cursor;
            if (description != null) mDescription.setText(description);
            if (state != null) mDescription.setCompoundDrawablesWithIntrinsicBounds(
                    state /*left*/, null /*top*/, null /*right*/, null /*bottom*/);
        }

        public abstract void setUiData(Cursor cursor);
        public abstract void onItemSelected();
        public abstract void onCreateContextMenu(ContextMenu menu);
        public abstract void onContextMenuSelected(MenuItem item);
    }

    //--------------

    /**
     * The holder for a profile header row.
     */
    private class ProfileHeaderHolder extends BaseHolder {
        
        public ProfileHeaderHolder(View view) {
            super(view);
        }
        
        @Override
        public void setUiData(Cursor cursor) {
            super.setUiData(cursor,
                    cursor.getString(mDescColIndex),
                    cursor.getInt(mEnableColIndex) != 0 ? mCheckOn : mCheckOff);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu) {
            menu.setHeaderTitle("Profile");

            menu.add(0, R.string.insert_new, 0, R.string.insert_new);
            menu.add(0, R.string.delete, 0, R.string.delete);
            menu.add(0, R.string.rename, 0, R.string.rename);
        }

        @Override
        public void onItemSelected() {
            Cursor cursor = getCursor();
            if (cursor == null) return;

            boolean enabled = cursor.getInt(mEnableColIndex) != 0;
            enabled = !enabled;
            
            long id = cursor.getLong(mIdColIndex);
            ContentValues cv = new ContentValues(1);
            cv.put(Columns.IS_ENABLED, enabled);
            mProfilesDb.update(id, cv, null/*whereClause*/, null/*whereArgs*/);

            // update ui
            cursor.requery();
            setUiData(cursor, null, enabled ? mCheckOn : mCheckOff);
        }

        @Override
        public void onContextMenuSelected(MenuItem item) {
            switch (item.getItemId()) {
            case R.string.insert_new:
                Log.d(TAG, "profile - insert_new");
                break;
            case R.string.delete:
                Log.d(TAG, "profile - delete");
                deleteProfile(
                        getCursor().getInt(mIdColIndex),
                        getCursor().getString(mDescColIndex));

                break;
            case R.string.rename:
                Log.d(TAG, "profile - rename");
                break;
            default:
                break;
            }
        }

        private void deleteProfile(final int row_id, String title) {
            Builder d = new AlertDialog.Builder(ProfilesUI.this);
            final int index = mNextTempDialogId++;
            mTempDialogList.put(index, d);

            d.setCancelable(true);
            d.setTitle("Delete profile");
            d.setIcon(R.drawable.timeriffic_icon);
            d.setMessage(String.format(
                    "Are you sure you want to delete profile '%s' and all its actions?", title));
            
            d.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    removeTempDialog(index);
                }
            });
            
            d.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    removeTempDialog(index);
                }
            });
            
            d.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int count = mProfilesDb.deleteProfile(row_id);
                    if (count > 0) {
                        getCursor().requery();
                        mAdapter.notifyDataSetChanged();
                    }
                    removeTempDialog(index);
                }
            });
            
            showDialog(index);
        }
    }

    //--------------

    /**
     * The holder for a timed action row.
     */
    private class TimedActionHolder extends BaseHolder {
        
        public TimedActionHolder(View view) {
            super(view);
        }

        @Override
        public void setUiData(Cursor cursor) {
            super.setUiData(cursor,
                    cursor.getString(mDescColIndex),
                    cursor.getInt(mEnableColIndex) != 0 ? mGreenDot : mGrayDot);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu) {
            menu.setHeaderTitle("Timed Action");

            menu.add(0, R.string.insert_new, 0, R.string.insert_new);
            menu.add(0, R.string.delete, 0, R.string.delete);
            menu.add(0, R.string.edit, 0, R.string.edit);
        }

        @Override
        public void onItemSelected() {
            // pass (or trigger edit?)
        }

        @Override
        public void onContextMenuSelected(MenuItem item) {
            switch (item.getItemId()) {
            case R.string.insert_new:
                Log.d(TAG, "profile - insert_new");
                break;
            case R.string.delete:
                Log.d(TAG, "profile - delete");
                deleteTimedAction(
                        getCursor().getInt(mIdColIndex),
                        getCursor().getString(mDescColIndex));
                break;
            case R.string.edit:
                Log.d(TAG, "profile - edit");
                break;
            default:
                break;
            }
        }

        private void deleteTimedAction(final int row_id, String description) {
            Builder d = new AlertDialog.Builder(ProfilesUI.this);
            final int index = mNextTempDialogId++;
            mTempDialogList.put(index, d);

            d.setCancelable(true);
            d.setTitle("Delete action");
            d.setIcon(R.drawable.timeriffic_icon);
            d.setMessage(String.format(
                    "Are you sure you want to delete action '%s'?", description));
            
            d.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    removeTempDialog(index);
                }
            });
            
            d.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    removeTempDialog(index);
                }
            });
            
            d.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int count = mProfilesDb.deleteAction(row_id);
                    if (count > 0) {
                        getCursor().requery();
                        mAdapter.notifyDataSetChanged();
                    }
                    removeTempDialog(index);
                }
            });
            
            showDialog(index);
        }
    }

    //--------------
}
