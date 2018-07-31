package com.bonzimybuddy.fenciqi;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class CursorRecyclerViewAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private Context mContext;
    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIdColumn;
    private DataSetObserver mDataSetObserver;

    public CursorRecyclerViewAdapter(Context context, Cursor cursor) {
        boolean cursorPresent = cursor != null;

        mContext = context;
        mCursor = cursor;
        mDataValid = cursorPresent;
        mRowIdColumn = cursorPresent ? cursor.getColumnIndexOrThrow("_id") : -1;
        mDataSetObserver = new CRDataSetObserver();
        if(cursorPresent)
            mCursor.registerDataSetObserver(mDataSetObserver);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public int getItemCount() {
        if(mDataValid && mCursor != null)
            return mCursor.getCount();
        else
            return 0;
    }

    @Override
    public long getItemId(int position) {
        if(mDataValid && mCursor != null && mCursor.moveToPosition(position))
            return mCursor.getLong(mRowIdColumn);
        else
            return 0;
    }

    public Cursor swapCursor(Cursor newCursor) {
        Cursor oldCursor = mCursor;
        mCursor = newCursor;

        if(mCursor == oldCursor)
            return null;

        if(oldCursor != null && mDataSetObserver != null)
            oldCursor.unregisterDataSetObserver(mDataSetObserver);

        if(mCursor != null) {
            if(mDataSetObserver != null)
                mCursor.registerDataSetObserver(mDataSetObserver);
            mRowIdColumn = mCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            notifyDataSetChanged();
        } else {
            mRowIdColumn = -1;
            mDataValid = false;
            notifyDataSetChanged();
        }

        return oldCursor;
    }

    @Override
    public void onBindViewHolder(VH viewHolder, int position) {
        mCursor.moveToPosition(position);
        onBindViewHolder(viewHolder, mCursor);
    }

    public abstract void onBindViewHolder(VH viewHolder, Cursor cursor);

    private class CRDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            mDataValid = false;
            notifyDataSetChanged();
        }
    }
}
