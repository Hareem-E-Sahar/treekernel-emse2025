package org.devtcg.five.music.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Comparator;
import iCua.UI.Windows.R;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewInflate;
import android.widget.*;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;

/**
 * Special ListAdapter wrapper that sections the list by alphabet.  Special
 * separators indicating the alphabetic letter will be injected into the list 
 * as non-clickable entities.
 * 
 * {@see SimpleCursorAdapter}
 */
public class AlphabetSectionedAdapter extends BaseAdapter {

    public static final String TAG = "AlphabetSectionedAdapter";

    protected SimpleCursorAdapter mInternal;

    protected Context mContext;

    protected Cursor mCursor;

    protected int mLetterColumn;

    protected int mLayoutSep;

    protected ViewInflate mInflate;

    protected int mCount;

    private boolean mCursorOK;

    public static int ITEM_TYPE_SEPARATOR = 0;

    public static int ITEM_TYPE_CURSOR_ROW = 1;

    /**
	 * Efficient mapping of list position to cursor position.
	 */
    private SortedSearchTree<Integer> mSepPositions = new SortedSearchTree<Integer>();

    /**
	 * Maps alphabet letters to the list position of the label separator.  
	 * Eventually this should be merged into the SortedSearchTree but at
	 * present the API is too weak to support this.
	 */
    private HashMap<Character, Integer> mSepLabels = new HashMap<Character, Integer>();

    public AlphabetSectionedAdapter(Context ctx, int layout, int layout_sep, Cursor c, String[] from, int[] to, int sigCol) {
        super();
        mContext = ctx;
        mCursor = c;
        mCursorOK = true;
        c.registerDataSetObserver(mDataSetObserver);
        mLayoutSep = layout_sep;
        mInflate = ViewInflate.from(ctx);
        mLetterColumn = sigCol;
        mInternal = new SimpleCursorAdapter(ctx, layout, c, from, to);
        mInternal.setViewBinder(mBinderFixBraindeadBehaviour);
        recountRows();
    }

    private final SimpleCursorAdapter.ViewBinder mBinderFixBraindeadBehaviour = new SimpleCursorAdapter.ViewBinder() {

        public boolean setViewValue(View v, Cursor c, int col) {
            if (v instanceof ImageView) {
                String value = c.getString(col);
                ImageView iv = (ImageView) v;
                if (value == null) iv.setImageURI(null); else iv.setImageURI(Uri.parse(value));
                return true;
            }
            return false;
        }
    };

    private final DataSetObserver mDataSetObserver = new DataSetObserver() {

        public void onChanged() {
            mCursorOK = true;
            recountRows();
        }

        public void onInvalidated() {
            mCursorOK = false;
            recountRows();
        }
    };

    private void recountRows() {
        char last = 0;
        mCount = 0;
        mSepPositions.clear();
        if (mCursorOK == false) return;
        Log.v(TAG, "recountRows() started...");
        mCursor.moveTo(-1);
        ArrayList<Integer> positions = new ArrayList<Integer>(27);
        while (mCursor.next() == true) {
            String text;
            text = mCursor.getString(mLetterColumn);
            if (text == null || text.length() == 0) throw new IllegalArgumentException("This adapter cannot support empty row text");
            char letter = Character.toUpperCase(text.charAt(0));
            assert letter != 0;
            if (Character.isLetter(letter) == false) letter = '#';
            if (letter != last) {
                last = letter;
                mSepLabels.put(letter, mCount);
                positions.add(mCount++);
            }
            mCount++;
        }
        mSepPositions.populate(positions);
        Log.v(TAG, "recountRows() done, finally!");
    }

    /**
	 * Access the position of the alphabet letter separator for the supplied letter.
	 * 
	 * @param letter
	 *   Separator letter.
	 * @return
	 *   If found, the list position of the separator; otherwise, -1.
	 */
    public int getLabelPosition(char letter) {
        Integer ret = mSepLabels.get(letter);
        if (ret == null) return -1;
        return ret;
    }

    public View getView(int pos, View convert, ViewGroup parent) {
        View ret;
        if (mCursorOK == false) throw new IllegalStateException("this should only be called when the cursor is valid");
        assert mSepPositions != null;
        SortedSearchTree.Node<Integer> posNode = mSepPositions.findLargestNear(pos);
        if (posNode.getValue() == pos) {
            TextView label;
            if (convert != null && (Integer) convert.getTag() == ITEM_TYPE_SEPARATOR) ret = convert; else {
                ret = mInflate.inflate(mLayoutSep, parent, false, null);
                ret.setTag(Integer.valueOf(ITEM_TYPE_SEPARATOR));
            }
            label = (TextView) ret.findViewById(R.id.alphabet_letter);
            mCursor.moveTo(pos - posNode.getOrder());
            char letter = mCursor.getString(mLetterColumn).charAt(0);
            if (Character.isLetter(letter) == false) letter = '#';
            label.setText(String.valueOf(letter));
            return ret;
        } else {
            int row = pos - posNode.getOrder() - 1;
            if (convert != null && (Integer) convert.getTag() == ITEM_TYPE_CURSOR_ROW) ret = mInternal.getView(row, convert, parent); else {
                ret = mInternal.getView(row, null, parent);
                ret.setTag(Integer.valueOf(ITEM_TYPE_CURSOR_ROW));
            }
        }
        return ret;
    }

    public void setViewBinder(SimpleCursorAdapter.ViewBinder binder) {
        mInternal.setViewBinder(binder);
    }

    public SimpleCursorAdapter.ViewBinder getViewBinder() {
        return mInternal.getViewBinder();
    }

    protected final int getDatabaseRow(int listRow) {
        SortedSearchTree.Node<Integer> node = mSepPositions.findLargestNear(listRow);
        if (node.getValue() == listRow) return -1;
        return listRow - node.getOrder() - 1;
    }

    public boolean isSelectable(int pos) {
        if (getDatabaseRow(pos) == -1) return false;
        return true;
    }

    public int getCount() {
        return mCount;
    }

    public Object getItem(int pos) {
        int row = getDatabaseRow(pos);
        if (row == -1) return null;
        return mInternal.getItem(row);
    }

    public long getItemId(int pos) {
        int row = getDatabaseRow(pos);
        if (row == -1) return -1;
        return mInternal.getItemId(row);
    }

    public boolean stableIds() {
        return mInternal.stableIds();
    }

    public void registerDataSetObserver(DataSetObserver o) {
        mInternal.registerDataSetObserver(o);
    }

    public void unregisterDataSetObserver(DataSetObserver o) {
        mInternal.unregisterDataSetObserver(o);
    }

    /**
	 * Specialized binary search tree that takes only sorted input and is
	 * designed to find near matches.  The tree accepts sorted input to
	 * efficiently construct a binary tree by recursively dichotomizes the list
	 * at its median.  Once constructed, lookups will yield either an exact
	 * match or the largest element in the tree not to exceed the search key.
	 *
	 * Be aware that searches which do not match exactly are the worst-case
	 * performance for this data structure, requiring exactly log n lookup
	 * time.
	 */
    private static class SortedSearchTree<E> {

        private Node<E> mRoot;

        public SortedSearchTree() {
            mRoot = null;
        }

        public SortedSearchTree(List<E> sorted) {
            super();
            populate(sorted);
        }

        public void clear() {
            mRoot = null;
        }

        @SuppressWarnings("unchecked")
        private static <T> Comparable<T> toComparable(T obj) {
            return (Comparable) obj;
        }

        public Node<E> findLargestNear(E needle) {
            Node<E> n = mRoot;
            Node<E> t = n;
            while (t != null) {
                int diff = toComparable(needle).compareTo(t.value);
                if (diff < 0) t = t.left; else if (diff > 0) {
                    n = t;
                    t = t.right;
                } else return t;
            }
            return n;
        }

        public void populate(List<E> sorted) {
            assert mRoot == null;
            int size = sorted.size();
            if (size == 0) return;
            mRoot = buildTree(sorted, 0, size - 1);
        }

        private Node<E> buildTree(List<E> sorted, int l, int r) {
            Node<E> n;
            assert l >= 0;
            assert l <= r;
            if (l == r) n = new Node<E>(sorted.get(l), l); else {
                int median = (l + r) / 2;
                n = new Node<E>(sorted.get(median), median);
                if (l < median) n.left = buildTree(sorted, l, median - 1);
                if (r > median) n.right = buildTree(sorted, median + 1, r);
            }
            return n;
        }

        public static class Node<E> {

            E value;

            int pos;

            Node<E> left;

            Node<E> right;

            public Node(E x, int p) {
                value = x;
                pos = p;
            }

            public E getValue() {
                return value;
            }

            public int getOrder() {
                return pos;
            }
        }
    }
}
