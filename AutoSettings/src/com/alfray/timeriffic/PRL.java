/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License TBD
 */

/**
 *
 */
package com.alfray.timeriffic;

import android.view.View;
import android.widget.AbsListView.RecyclerListener;

/**
 * This {@link RecyclerListener} is attached to the profile list to
 * call {@link BH#clearCursor()} of the tags of the reclaimed
 * views. This should ensure that not dangling cursor reference exists.
 */
class PRL implements RecyclerListener {
    @Override
    public void onMovedToScrapHeap(View view) {
        Object tag = view.getTag();
        // pass... not doing anything anymore since BH doesn't
        // hold a cursor anymore. TODO remove later.
    }
}
