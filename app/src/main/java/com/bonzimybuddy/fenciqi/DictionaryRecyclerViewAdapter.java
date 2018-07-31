package com.bonzimybuddy.fenciqi;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DictionaryRecyclerViewAdapter
        extends CursorRecyclerViewAdapter<DictionaryRecyclerViewAdapter.ViewHolder> {

    private Context mContext;

    public DictionaryRecyclerViewAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;

        public TextView mNameView;
        public TextView mEntryCountView;
        public ImageView mFavoriteView;

        public long mContentId;

        public ViewHolder(CardView view) {
            super(view);
            mCardView = view;

            mNameView = view.findViewById(R.id.dictionary_name);
            mEntryCountView = view.findViewById(R.id.dictionary_entry_count);
            mFavoriteView = view.findViewById(R.id.dictionary_favorite_image);
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView view = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_dictionary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, Cursor cursor) {
        holder.mContentId = cursor.getLong(0);
        holder.mNameView.setText(cursor.getString(1)); // query currently uses a triple column projection (_id, name, count)
        holder.mEntryCountView.setText(cursor.getString(2) + " 詞"); // TODO: FIX

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        holder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mContext, holder.mCardView);
                popupMenu.getMenuInflater().inflate(R.menu.long_click_dictionary_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        boolean handled = false;

                        switch(item.getItemId()) {
                            case R.id.delete_dictionary:
                                Uri contentUri = ContentUris.withAppendedId(CatalogContract.Lexica.CONTENT_URI, holder.mContentId);
                                if(mContext.getContentResolver().delete(contentUri, null, null) > 0)
                                    handled = true;
                                break;
                            default:
                                break;
                        }

                        return handled;
                    }
                });

                popupMenu.show();

                return true;
            }
        });
    }

}
