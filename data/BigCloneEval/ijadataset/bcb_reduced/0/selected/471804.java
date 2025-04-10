package android.widget;

import com.android.internal.R;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;

/**
 *
 * Displays a list of tab labels representing each page in the parent's tab
 * collection. The container object for this widget is
 * {@link android.widget.TabHost TabHost}. When the user selects a tab, this
 * object sends a message to the parent container, TabHost, to tell it to switch
 * the displayed page. You typically won't use many methods directly on this
 * object. The container TabHost is used to add labels, add the callback
 * handler, and manage callbacks. You might call this object to iterate the list
 * of tabs, or to tweak the layout of the tab list, but most methods should be
 * called on the containing TabHost object.
 */
public class TabWidget extends LinearLayout implements OnFocusChangeListener {

    private OnTabSelectionChanged mSelectionChangedListener;

    private int mSelectedTab = 0;

    private Drawable mBottomLeftStrip;

    private Drawable mBottomRightStrip;

    private boolean mStripMoved;

    private Drawable mDividerDrawable;

    private boolean mDrawBottomStrips = true;

    public TabWidget(Context context) {
        this(context, null);
    }

    public TabWidget(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.tabWidgetStyle);
    }

    public TabWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        initTabWidget();
        TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.TabWidget, defStyle, 0);
        a.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mStripMoved = true;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (i == childCount - 1) {
            return mSelectedTab;
        } else if (i >= mSelectedTab) {
            return i + 1;
        } else {
            return i;
        }
    }

    private void initTabWidget() {
        setOrientation(LinearLayout.HORIZONTAL);
        mGroupFlags |= FLAG_USE_CHILD_DRAWING_ORDER;
        final Context context = mContext;
        final Resources resources = context.getResources();
        if (context.getApplicationInfo().targetSdkVersion <= Build.VERSION_CODES.DONUT) {
            mBottomLeftStrip = resources.getDrawable(com.android.internal.R.drawable.tab_bottom_left_v4);
            mBottomRightStrip = resources.getDrawable(com.android.internal.R.drawable.tab_bottom_right_v4);
        } else {
            mBottomLeftStrip = resources.getDrawable(com.android.internal.R.drawable.tab_bottom_left);
            mBottomRightStrip = resources.getDrawable(com.android.internal.R.drawable.tab_bottom_right);
        }
        setFocusable(true);
        setOnFocusChangeListener(this);
    }

    /**
     * Returns the tab indicator view at the given index.
     *
     * @param index the zero-based index of the tab indicator view to return
     * @return the tab indicator view at the given index
     */
    public View getChildTabViewAt(int index) {
        if (mDividerDrawable != null) {
            index *= 2;
        }
        return getChildAt(index);
    }

    /**
     * Returns the number of tab indicator views.
     * @return the number of tab indicator views.
     */
    public int getTabCount() {
        int children = getChildCount();
        if (mDividerDrawable != null) {
            children = (children + 1) / 2;
        }
        return children;
    }

    /**
     * Sets the drawable to use as a divider between the tab indicators.
     * @param drawable the divider drawable
     */
    public void setDividerDrawable(Drawable drawable) {
        mDividerDrawable = drawable;
    }

    /**
     * Sets the drawable to use as a divider between the tab indicators.
     * @param resId the resource identifier of the drawable to use as a
     * divider.
     */
    public void setDividerDrawable(int resId) {
        mDividerDrawable = mContext.getResources().getDrawable(resId);
    }

    /**
     * Controls whether the bottom strips on the tab indicators are drawn or
     * not.  The default is to draw them.  If the user specifies a custom
     * view for the tab indicators, then the TabHost class calls this method
     * to disable drawing of the bottom strips.
     * @param drawBottomStrips true if the bottom strips should be drawn.
     */
    void setDrawBottomStrips(boolean drawBottomStrips) {
        mDrawBottomStrips = drawBottomStrips;
    }

    @Override
    public void childDrawableStateChanged(View child) {
        if (child == getChildTabViewAt(mSelectedTab)) {
            invalidate();
        }
        super.childDrawableStateChanged(child);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (!mDrawBottomStrips) {
            return;
        }
        View selectedChild = getChildTabViewAt(mSelectedTab);
        mBottomLeftStrip.setState(selectedChild.getDrawableState());
        mBottomRightStrip.setState(selectedChild.getDrawableState());
        if (mStripMoved) {
            Rect selBounds = new Rect();
            selBounds.left = selectedChild.getLeft();
            selBounds.right = selectedChild.getRight();
            final int myHeight = getHeight();
            mBottomLeftStrip.setBounds(Math.min(0, selBounds.left - mBottomLeftStrip.getIntrinsicWidth()), myHeight - mBottomLeftStrip.getIntrinsicHeight(), selBounds.left, getHeight());
            mBottomRightStrip.setBounds(selBounds.right, myHeight - mBottomRightStrip.getIntrinsicHeight(), Math.max(getWidth(), selBounds.right + mBottomRightStrip.getIntrinsicWidth()), myHeight);
            mStripMoved = false;
        }
        mBottomLeftStrip.draw(canvas);
        mBottomRightStrip.draw(canvas);
    }

    /**
     * Sets the current tab.
     * This method is used to bring a tab to the front of the Widget,
     * and is used to post to the rest of the UI that a different tab
     * has been brought to the foreground.
     *
     * Note, this is separate from the traditional "focus" that is
     * employed from the view logic.
     *
     * For instance, if we have a list in a tabbed view, a user may be
     * navigating up and down the list, moving the UI focus (orange
     * highlighting) through the list items.  The cursor movement does
     * not effect the "selected" tab though, because what is being
     * scrolled through is all on the same tab.  The selected tab only
     * changes when we navigate between tabs (moving from the list view
     * to the next tabbed view, in this example).
     *
     * To move both the focus AND the selected tab at once, please use
     * {@link #setCurrentTab}. Normally, the view logic takes care of
     * adjusting the focus, so unless you're circumventing the UI,
     * you'll probably just focus your interest here.
     *
     *  @param index The tab that you want to indicate as the selected
     *  tab (tab brought to the front of the widget)
     *
     *  @see #focusCurrentTab
     */
    public void setCurrentTab(int index) {
        if (index < 0 || index >= getTabCount()) {
            return;
        }
        getChildTabViewAt(mSelectedTab).setSelected(false);
        mSelectedTab = index;
        getChildTabViewAt(mSelectedTab).setSelected(true);
        mStripMoved = true;
    }

    /**
     * Sets the current tab and focuses the UI on it.
     * This method makes sure that the focused tab matches the selected
     * tab, normally at {@link #setCurrentTab}.  Normally this would not
     * be an issue if we go through the UI, since the UI is responsible
     * for calling TabWidget.onFocusChanged(), but in the case where we
     * are selecting the tab programmatically, we'll need to make sure
     * focus keeps up.
     *
     *  @param index The tab that you want focused (highlighted in orange)
     *  and selected (tab brought to the front of the widget)
     *
     *  @see #setCurrentTab
     */
    public void focusCurrentTab(int index) {
        final int oldTab = mSelectedTab;
        setCurrentTab(index);
        if (oldTab != index) {
            getChildTabViewAt(index).requestFocus();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        int count = getTabCount();
        for (int i = 0; i < count; i++) {
            View child = getChildTabViewAt(i);
            child.setEnabled(enabled);
        }
    }

    @Override
    public void addView(View child) {
        if (child.getLayoutParams() == null) {
            final LinearLayout.LayoutParams lp = new LayoutParams(0, ViewGroup.LayoutParams.FILL_PARENT, 1.0f);
            lp.setMargins(0, 0, 0, 0);
            child.setLayoutParams(lp);
        }
        child.setFocusable(true);
        child.setClickable(true);
        if (mDividerDrawable != null && getTabCount() > 0) {
            ImageView divider = new ImageView(mContext);
            final LinearLayout.LayoutParams lp = new LayoutParams(mDividerDrawable.getIntrinsicWidth(), LayoutParams.FILL_PARENT);
            lp.setMargins(0, 0, 0, 0);
            divider.setLayoutParams(lp);
            divider.setBackgroundDrawable(mDividerDrawable);
            super.addView(divider);
        }
        super.addView(child);
        child.setOnClickListener(new TabClickListener(getTabCount() - 1));
        child.setOnFocusChangeListener(this);
    }

    /**
     * Provides a way for {@link TabHost} to be notified that the user clicked on a tab indicator.
     */
    void setTabSelectionListener(OnTabSelectionChanged listener) {
        mSelectionChangedListener = listener;
    }

    public void onFocusChange(View v, boolean hasFocus) {
        if (v == this && hasFocus) {
            getChildTabViewAt(mSelectedTab).requestFocus();
            return;
        }
        if (hasFocus) {
            int i = 0;
            int numTabs = getTabCount();
            while (i < numTabs) {
                if (getChildTabViewAt(i) == v) {
                    setCurrentTab(i);
                    mSelectionChangedListener.onTabSelectionChanged(i, false);
                    break;
                }
                i++;
            }
        }
    }

    private class TabClickListener implements OnClickListener {

        private final int mTabIndex;

        private TabClickListener(int tabIndex) {
            mTabIndex = tabIndex;
        }

        public void onClick(View v) {
            mSelectionChangedListener.onTabSelectionChanged(mTabIndex, true);
        }
    }

    /**
     * Let {@link TabHost} know that the user clicked on a tab indicator.
     */
    static interface OnTabSelectionChanged {

        /**
         * Informs the TabHost which tab was selected. It also indicates
         * if the tab was clicked/pressed or just focused into.
         *
         * @param tabIndex index of the tab that was selected
         * @param clicked whether the selection changed due to a touch/click
         * or due to focus entering the tab through navigation. Pass true
         * if it was due to a press/click and false otherwise.
         */
        void onTabSelectionChanged(int tabIndex, boolean clicked);
    }
}
