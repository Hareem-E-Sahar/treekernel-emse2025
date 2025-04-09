package com.android.contacts;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.util.Log;
import android.util.SparseIntArray;
import android.widget.SectionIndexer;

/**
 * SectionIndexer which is for "phonetically sortable" String. This class heavily depends on the
 * algorithm of the SQL function "GET_PHONETICALLY_SORTABLE_STRING", whose implementation
 * is written in C++.
 */
public final class JapaneseContactListIndexer extends DataSetObserver implements SectionIndexer {

    private static String TAG = "JapaneseContactListIndexer";

    private static final String[] sSections = { " ", "あ", "か", "さ", "た", "な", "は", "ま", "や", "ら", "わ", "Ａ", "Ｂ", "Ｃ", "Ｄ", "Ｅ", "Ｆ", "Ｇ", "Ｈ", "Ｉ", "Ｊ", "Ｋ", "Ｌ", "Ｍ", "Ｎ", "Ｏ", "Ｐ", "Ｑ", "Ｒ", "Ｓ", "Ｔ", "Ｕ", "Ｖ", "Ｗ", "Ｘ", "Ｙ", "｀", "数", "記" };

    private static final int sSectionsLength = sSections.length;

    private int mColumnIndex;

    private Cursor mDataCursor;

    private SparseIntArray mStringMap;

    public JapaneseContactListIndexer(Cursor cursor, int columnIndex) {
        int len = sSections.length;
        mColumnIndex = columnIndex;
        mDataCursor = cursor;
        mStringMap = new SparseIntArray(sSectionsLength);
        if (cursor != null) {
            cursor.registerDataSetObserver(this);
        }
    }

    public void setCursor(Cursor cursor) {
        if (mDataCursor != null) {
            mDataCursor.unregisterDataSetObserver(this);
        }
        mDataCursor = cursor;
        if (cursor != null) {
            mDataCursor.registerDataSetObserver(this);
        }
    }

    private int getSectionCodePoint(int index) {
        if (index < sSections.length - 2) {
            return sSections[index].codePointAt(0);
        } else if (index == sSections.length - 2) {
            return 0xFF66;
        } else {
            return 0xFF70;
        }
    }

    public int getPositionForSection(int sectionIndex) {
        final SparseIntArray stringMap = mStringMap;
        final Cursor cursor = mDataCursor;
        if (cursor == null || sectionIndex <= 0) {
            return 0;
        }
        if (sectionIndex >= sSectionsLength) {
            sectionIndex = sSectionsLength - 1;
        }
        int savedCursorPos = cursor.getPosition();
        String targetLetter = sSections[sectionIndex];
        int key = targetLetter.codePointAt(0);
        {
            int tmp = stringMap.get(key, Integer.MIN_VALUE);
            if (Integer.MIN_VALUE != tmp) {
                return tmp;
            }
        }
        int end = cursor.getCount();
        int pos = 0;
        {
            int prevLetter = sSections[sectionIndex - 1].codePointAt(0);
            int prevLetterPos = stringMap.get(prevLetter, Integer.MIN_VALUE);
            if (prevLetterPos != Integer.MIN_VALUE) {
                pos = prevLetterPos;
            }
        }
        while (end - pos > 100) {
            int tmp = (end + pos) / 2;
            cursor.moveToPosition(tmp);
            String sort_name;
            do {
                sort_name = cursor.getString(mColumnIndex);
                if (sort_name == null || sort_name.length() == 0) {
                    Log.e(TAG, "sort_name is null or its length is 0. index: " + tmp);
                    cursor.moveToNext();
                    tmp++;
                    continue;
                }
                break;
            } while (tmp < end);
            if (tmp == end) {
                break;
            }
            int codePoint = sort_name.codePointAt(0);
            if (codePoint < getSectionCodePoint(sectionIndex)) {
                pos = tmp;
            } else {
                end = tmp;
            }
        }
        for (cursor.moveToPosition(pos); !cursor.isAfterLast(); ++pos, cursor.moveToNext()) {
            String sort_name = cursor.getString(mColumnIndex);
            if (sort_name == null || sort_name.length() == 0) {
                Log.e(TAG, "sort_name is null or its length is 0. index: " + pos);
                continue;
            }
            int codePoint = sort_name.codePointAt(0);
            if (codePoint >= getSectionCodePoint(sectionIndex)) {
                break;
            }
        }
        stringMap.put(key, pos);
        cursor.moveToPosition(savedCursorPos);
        return pos;
    }

    public int getSectionForPosition(int position) {
        return 0;
    }

    public Object[] getSections() {
        return sSections;
    }

    @Override
    public void onChanged() {
        super.onChanged();
        mStringMap.clear();
    }

    @Override
    public void onInvalidated() {
        super.onInvalidated();
        mStringMap.clear();
    }
}
