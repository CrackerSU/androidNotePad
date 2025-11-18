package com.example.android.notepad;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

// NoteListAdapter.java
public class NoteListAdapter extends BaseAdapter {
    private Context mContext;
    private Cursor mCursor;

    public NoteListAdapter(Context context, Cursor cursor) {
        mContext = context;
        changeCursor(cursor);
    }

    public void changeCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    @Override
    public Object getItem(int position) {
        if (mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor;
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.noteslist_item, parent, false);
        }

        if (mCursor != null && mCursor.moveToPosition(position)) {
//            TextView titleView = (TextView)convertView.findViewById(R.id.title_text);
//            TextView dateView = (TextView)convertView.findViewById(R.id.date_text);
            // 获取分类颜色指示器视图
            View colorIndicator = convertView.findViewById(R.id.category_color_indicator);

            @SuppressLint("Range") String title = mCursor.getString(mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TITLE));
            @SuppressLint("Range") String date = mCursor.getString(mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE));
            @SuppressLint("Range") String category = mCursor.getString(mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_CATEGORY));

//            titleView.setText(title);
//            dateView.setText(date);

            // 设置分类颜色
            if (colorIndicator != null) {
                int color = getCategoryColor(category);
                colorIndicator.setBackgroundColor(color);
            }
        }

        return convertView;
    }
    private int getCategoryColor(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            return mContext.getResources().getColor(R.color.colorPrimary);
        }

        int hash = categoryName.hashCode();
        int colorIndex = Math.abs(hash) % 5;

        switch (colorIndex) {
            case 0:
                return mContext.getResources().getColor(R.color.category_red);
            case 1:
                return mContext.getResources().getColor(R.color.category_blue);
            case 2:
                return mContext.getResources().getColor(R.color.category_green);
            case 3:
                return mContext.getResources().getColor(R.color.category_yellow);
            case 4:
                return mContext.getResources().getColor(R.color.category_purple);
            default:
                return mContext.getResources().getColor(R.color.colorPrimary);
        }
    }


}
