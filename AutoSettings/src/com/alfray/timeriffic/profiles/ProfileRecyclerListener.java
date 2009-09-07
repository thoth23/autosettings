/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License TBD
 */

/**
 *
 */
package com.alfray.timeriffic.profiles;

import android.view.View;
import android.widget.AbsListView.RecyclerListener;

/**
 * This {@link RecyclerListener} is attached to the profile list to
 * call {@link BaseHolder#clearCursor()} of the tags of the reclaimed
 * views. This should ensure that not dangling cursor reference exists.
 */
class ProfileRecyclerListener implements RecyclerListener {
    @Override
    public void onMovedToScrapHeap(View view) {
        Object tag = view.getTag();
        if (tag instanceof BaseHolder) {
            ((BaseHolder) tag).clearCursor();
        }
    }
}
