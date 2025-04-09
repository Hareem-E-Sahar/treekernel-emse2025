package com.jecelyin.widget;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import com.jecelyin.colorschemes.ColorScheme;
import com.jecelyin.editor.JecEditor;
import com.jecelyin.editor.R;
import com.jecelyin.editor.UndoParcel;
import com.jecelyin.editor.UndoParcel.TextChange;
import com.jecelyin.highlight.Highlight;
import com.jecelyin.util.FileUtil;
import com.jecelyin.util.TextUtil;
import com.jecelyin.util.TimeUtil;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.MetaKeyKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.text.method.Touch;
import android.text.style.ParagraphStyle;
import android.text.style.TabStopSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Scroller;
import android.widget.Toast;

public class JecEditText extends EditText {

    private Paint mWhiteSpacePaint;

    private Paint mLineNumberPaint;

    private boolean mShowWhiteSpace = false;

    private boolean mShowLineNum = true;

    private Path mLineBreakPath = new Path();

    private Path mTabPath = new Path();

    private Path[] mWhiteSpacePaths = new Path[] { mTabPath, mLineBreakPath };

    private TextPaint mTextPaint;

    private TextPaint mWorkPaint;

    /** 缩放比例 */
    private int paddingLeft = 0;

    private int lastPaddingLeft = 0;

    private int realLineNum = 0;

    private boolean hasNewline = true;

    private static float TAB_INCREMENT = 20F;

    private static Rect sTempRect = new Rect();

    private FastScroller mFastScroller;

    private Layout mLayout;

    private Editable mText = null;

    private UndoParcel mUndoParcel = new UndoParcel();

    private UndoParcel mRedoParcel = new UndoParcel();

    private boolean mUndoRedo = false;

    private boolean mAutoIndent = false;

    private HashMap<Integer, String> mLineStr = new HashMap<Integer, String>();

    private int mLineNumber = 0;

    private int mLineNumberWidth = 0;

    private int mLineNumberLength = 0;

    private ArrayList<Integer> mLastEditBuffer = new ArrayList<Integer>();

    private static final int LAST_EDIT_DISTANCE_LIMIT = 20;

    private int mLastEditIndex = -1;

    private static final String TAG = "JecEditText";

    private VelocityTracker mVelocityTracker;

    private FlingRunnable mFlingRunnable;

    private String current_encoding = "UTF-8";

    private String current_path = "";

    private String current_ext = "";

    private String current_title = "";

    private int current_linebreak = 0;

    private int src_text_length;

    private long src_text_crc32;

    private CRC32 mCRC32;

    private boolean mNoWrapMode = false;

    private int mLineNumX = 0;

    private Highlight mHighlight;

    /**
     * Touch mode
     */
    public static boolean TOUCH_ZOOM_ENABLED = true;

    private static final int TOUCH_DRAG_START_MODE = 2;

    private static final int TOUCH_DONE_MODE = 7;

    private int mTouchMode = TOUCH_DONE_MODE;

    /** 记录按下第二个点距第一个点的距离 */
    private float oldDist;

    /** 最小字体 */
    private static final float MIN_TEXT_SIZE = 10f;

    /** 最大字体 */
    private static final float MAX_TEXT_SIZE = 32.0f;

    /** 缩放比例 */
    private float scale = 0.5f;

    /** 设置字体大小 */
    private float mTextSize;

    private boolean mSupportMultiTouch;

    private static boolean mHideSoftKeyboard;

    public JecEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private static class JecSaveState extends BaseSavedState {

        UndoParcel mRedoParcelState;

        UndoParcel mUndoParcelState;

        JecSaveState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeParcelable(mUndoParcelState, 0);
            out.writeParcelable(mRedoParcelState, 0);
        }

        private JecSaveState(Parcel in) {
            super(in);
            mUndoParcelState = in.readParcelable(UndoParcel.class.getClassLoader());
            mRedoParcelState = in.readParcelable(UndoParcel.class.getClassLoader());
        }
    }

    /**
     * 保存文本各个操作状态
     */
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        JecSaveState mJecSaveState = new JecSaveState(superState);
        mJecSaveState.mUndoParcelState = mUndoParcel;
        mJecSaveState.mRedoParcelState = mRedoParcel;
        return mJecSaveState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof JecSaveState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        JecSaveState mJecSaveState = (JecSaveState) state;
        super.onRestoreInstanceState(mJecSaveState.getSuperState());
        mUndoParcel = mJecSaveState.mUndoParcelState;
        mRedoParcel = mJecSaveState.mRedoParcelState;
        setUndoRedoButtonStatus();
    }

    public void init() {
        mCRC32 = new CRC32();
        mHighlight = new Highlight();
        mWorkPaint = new TextPaint();
        mTextPaint = getPaint();
        mLineNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWhiteSpacePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        paddingLeft = getPaddingLeft();
        mFastScroller = new FastScroller(getContext(), this);
        addTextChangedListener(mUndoWatcher);
        clearFocus();
        mTextSize = mTextPaint.getTextSize();
        mLineNumberPaint.setTextSize(mTextSize - 2);
        mLineNumberPaint.setTypeface(Typeface.MONOSPACE);
        mLineNumberPaint.setStrokeWidth(1);
        mLineNumberPaint.setColor(Color.parseColor(ColorScheme.color_font));
        mWhiteSpacePaint.setStrokeWidth(0.75F);
        mWhiteSpacePaint.setStyle(Paint.Style.STROKE);
        mWhiteSpacePaint.setColor(Color.GRAY);
        float textHeight;
        mLineBreakPath.reset();
        float width = mTextPaint.measureText("L");
        float mDescent = mTextPaint.descent();
        float mAscent = mTextPaint.ascent();
        textHeight = mDescent - mAscent;
        mLineBreakPath.moveTo(width * 0.6F, 0);
        mLineBreakPath.lineTo(width * 0.6F, -textHeight * 0.7F);
        mLineBreakPath.moveTo(width * 0.6F, 0);
        mLineBreakPath.lineTo(width * 0.25F, -textHeight * 0.3F);
        mLineBreakPath.moveTo(width * 0.6F, 0);
        mLineBreakPath.lineTo(width * 0.95F, -textHeight * 0.3F);
        mTabPath.reset();
        width = mTextPaint.measureText("\t\t");
        textHeight = mTextPaint.descent() - mTextPaint.ascent();
        mTabPath.moveTo(0, -textHeight * 0.5F);
        mTabPath.lineTo(width * 0.1F, -textHeight * 0.35F);
        mTabPath.lineTo(0, -textHeight * 0.2F);
        mTabPath.moveTo(width * 0.15F, -textHeight * 0.5F);
        mTabPath.lineTo(width * 0.25F, -textHeight * 0.35F);
        mTabPath.lineTo(width * 0.15F, -textHeight * 0.2F);
        PackageManager pm = getContext().getPackageManager();
        mSupportMultiTouch = pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);
    }

    private OnTextChangedListener mOnTextChangedListener = null;

    public interface OnTextChangedListener {

        void onTextChanged(JecEditText mEditText);
    }

    public void setOnTextChangedListener(OnTextChangedListener l) {
        mOnTextChangedListener = l;
    }

    private TextWatcher mUndoWatcher = new TextWatcher() {

        TextChange lastChange;

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (JecEditor.isLoading) return;
            mHighlight.redraw();
            if (lastChange != null) {
                if (count < UndoParcel.MAX_SIZE) {
                    lastChange.newtext = s.subSequence(start, start + count);
                    if (start == lastChange.start && (lastChange.oldtext.length() > 0 || lastChange.newtext.length() > 0) && !equalsCharSequence(lastChange.newtext, lastChange.oldtext)) {
                        mUndoParcel.push(lastChange);
                        mRedoParcel.removeAll();
                    }
                    setUndoRedoButtonStatus();
                } else {
                    mUndoParcel.removeAll();
                    mRedoParcel.removeAll();
                }
                lastChange = null;
            }
            int bufSize = mLastEditBuffer.size();
            int lastLoc = 0;
            if (bufSize != 0) {
                lastLoc = mLastEditBuffer.get(bufSize - 1);
            }
            if (Math.abs(start - lastLoc) > LAST_EDIT_DISTANCE_LIMIT) {
                mLastEditBuffer.add(start);
                mLastEditIndex = mLastEditBuffer.size() - 1;
                if (mOnTextChangedListener != null) mOnTextChangedListener.onTextChanged(JecEditText.this);
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (JecEditor.isLoading) return;
            if (mUndoRedo) {
                mUndoRedo = false;
            } else {
                if (count < UndoParcel.MAX_SIZE) {
                    lastChange = new TextChange();
                    lastChange.start = start;
                    lastChange.oldtext = s.subSequence(start, start + count);
                } else {
                    mUndoParcel.removeAll();
                    mRedoParcel.removeAll();
                    lastChange = null;
                }
            }
        }

        public void afterTextChanged(Editable s) {
        }
    };

    private boolean equalsCharSequence(CharSequence s1, CharSequence s2) {
        if (s1 == null || s2 == null) {
            return false;
        }
        if (s1.length() != s2.length()) {
            return false;
        }
        return s1.toString().equals(s2.toString());
    }

    private void setUndoRedoButtonStatus() {
        if (mOnTextChangedListener != null) mOnTextChangedListener.onTextChanged(this);
    }

    public boolean canUndo() {
        return mUndoParcel.canUndo();
    }

    public boolean canRedo() {
        return mRedoParcel.canUndo();
    }

    public void show() {
        setVisibility(View.VISIBLE);
        if (mOnTextChangedListener != null) mOnTextChangedListener.onTextChanged(this);
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    /**
     * 撤销
     */
    public void unDo() {
        TextChange textchange = mUndoParcel.pop();
        if (textchange != null) {
            Editable text = getText();
            mUndoRedo = true;
            text.replace(textchange.start, textchange.start + textchange.newtext.length(), textchange.oldtext);
            Selection.setSelection(text, textchange.start + textchange.oldtext.length());
            mRedoParcel.push(textchange);
            setUndoRedoButtonStatus();
        }
    }

    /**
     * 重做
     */
    public void reDo() {
        TextChange textchange = mRedoParcel.pop();
        if (textchange != null) {
            Editable text = getText();
            mUndoRedo = true;
            text.replace(textchange.start, textchange.start + textchange.oldtext.length(), textchange.newtext);
            Selection.setSelection(text, textchange.start + textchange.newtext.length());
            mUndoParcel.push(textchange);
            setUndoRedoButtonStatus();
        }
    }

    /**
     * 重置撤销，重做状态
     */
    public void resetUndoStatus() {
        mRedoParcel.clean();
        mUndoParcel.clean();
        setUndoRedoButtonStatus();
        mLastEditBuffer.clear();
    }

    private void setLineNumberWidth(int lastline) {
        mLineNumberWidth = (int) mLineNumberPaint.measureText(lastline + "|");
        mLineNumber = lastline;
        mLineNumberLength = Integer.toString(lastline).length();
        setShowLineNum(mShowLineNum);
    }

    public void setShowLineNum(boolean b) {
        mShowLineNum = b;
        int left;
        if (!mShowLineNum) {
            left = paddingLeft;
        } else {
            left = paddingLeft + mLineNumberWidth;
        }
        setPaddingLeft(left);
    }

    public void setShowWhitespace(boolean b) {
        mShowWhiteSpace = b;
    }

    public void setText2(CharSequence text) {
        try {
            super.setText(text);
        } catch (OutOfMemoryError e) {
            Toast.makeText(getContext(), R.string.out_of_memory, Toast.LENGTH_SHORT).show();
        }
    }

    public String getString() {
        return getText().toString();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mLayout = getLayout();
        mText = (Editable) getText();
        super.onDraw(canvas);
        drawView(canvas);
        if (mFastScroller != null) {
            mFastScroller.draw(canvas);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        if (mFastScroller != null) {
            boolean intercepted;
            intercepted = mFastScroller.onTouchEvent(event);
            if (intercepted) {
                return true;
            }
            intercepted = mFastScroller.onInterceptTouchEvent(event);
            if (intercepted) {
                return true;
            }
        }
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (TOUCH_ZOOM_ENABLED) {
                    mTouchMode = TOUCH_DRAG_START_MODE;
                    oldDist = calc_spacing(event);
                }
                if (mFlingRunnable != null) {
                    mFlingRunnable.endFling();
                    cancelLongPress();
                }
                break;
            case MotionEvent.ACTION_UP:
                mTouchMode = TOUCH_DONE_MODE;
                int mMinimumVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
                int mMaximumVelocity = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                final int initialVelocity = (int) mVelocityTracker.getYVelocity();
                if (Math.abs(initialVelocity) > mMinimumVelocity) {
                    try {
                        if (mFlingRunnable == null) {
                            mFlingRunnable = new FlingRunnable(getContext());
                        }
                        mHighlight.stop();
                        mFlingRunnable.start(this, -initialVelocity);
                    } catch (Exception e) {
                    }
                } else {
                    mHighlight.redraw();
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (TOUCH_ZOOM_ENABLED && mTouchMode == TOUCH_DRAG_START_MODE && mSupportMultiTouch && event.getPointerCount() >= 2) {
                    cancelLongPress();
                    float newDist = calc_spacing(event);
                    if (Math.abs(newDist - oldDist) > 10f) {
                        if (newDist > oldDist) {
                            zoomOut();
                        } else if (newDist < oldDist) {
                            zoomIn();
                        }
                        oldDist = newDist;
                    }
                }
                break;
        }
        boolean res;
        try {
            res = super.onTouchEvent(event);
        } catch (Exception e) {
            res = true;
        }
        return res;
    }

    /**
     * 求出2个触点间的 距离
     * 
     * @param event
     * @return
     */
    private float calc_spacing(MotionEvent event) {
        if (event.getPointerCount() < 2) return 0;
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /**
     * 放大
     */
    protected void zoomOut() {
        mTextSize += scale;
        if (mTextSize > MAX_TEXT_SIZE) {
            mTextSize = MAX_TEXT_SIZE;
        }
        setTextSize(mTextSize);
        mLineNumberPaint.setTextSize(mTextSize - 2);
    }

    /**
     * 缩小
     */
    protected void zoomIn() {
        mTextSize -= scale;
        if (mTextSize < MIN_TEXT_SIZE) {
            mTextSize = MIN_TEXT_SIZE;
        }
        setTextSize(mTextSize);
        mLineNumberPaint.setTextSize(mTextSize - 2);
    }

    /**
     * Responsible for fling behavior. Use {@link #start(int)} to initiate a
     * fling. Each frame of the fling is handled in {@link #run()}. A
     * FlingRunnable will keep re-posting itself until the fling is done.
     * 
     */
    private static class FlingRunnable implements Runnable {

        static final int TOUCH_MODE_REST = -1;

        static final int TOUCH_MODE_FLING = 3;

        int mTouchMode = TOUCH_MODE_REST;

        /**
         * Tracks the decay of a fling scroll
         */
        private final Scroller mScroller;

        /**
         * Y value reported by mScroller on the previous fling
         */
        private int mLastFlingY;

        private JecEditText mWidget = null;

        FlingRunnable(Context context) {
            mScroller = new Scroller(context);
        }

        void start(JecEditText parent, int initialVelocity) {
            mWidget = parent;
            int initialX = parent.getScrollX();
            int initialY = parent.getScrollY();
            mLastFlingY = initialY;
            mScroller.fling(initialX, initialY, 0, initialVelocity, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            mTouchMode = TOUCH_MODE_FLING;
            mWidget.post(this);
        }

        private void endFling() {
            mTouchMode = TOUCH_MODE_REST;
            if (mWidget != null) {
                try {
                    mWidget.removeCallbacks(this);
                    mWidget.moveCursorToVisibleOffset();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mWidget = null;
            }
        }

        public void run() {
            switch(mTouchMode) {
                default:
                    return;
                case TOUCH_MODE_FLING:
                    {
                        final Scroller scroller = mScroller;
                        boolean more = scroller.computeScrollOffset();
                        int x = scroller.getCurrX();
                        int y = scroller.getCurrY();
                        Layout layout = mWidget.getLayout();
                        if (layout == null) break;
                        int padding;
                        try {
                            padding = mWidget.getTotalPaddingTop() + mWidget.getTotalPaddingBottom();
                        } catch (Exception e) {
                            padding = 0;
                        }
                        y = Math.min(y, layout.getHeight() - (mWidget.getHeight() - padding));
                        y = Math.max(y, 0);
                        Touch.scrollTo(mWidget, layout, x, y);
                        int delta = mLastFlingY - y;
                        if (Math.abs(delta) <= 3) {
                            mWidget.mHighlight.redraw();
                        }
                        if (more && delta != 0) {
                            mWidget.invalidate();
                            mLastFlingY = y;
                            mWidget.post(this);
                        } else {
                            endFling();
                        }
                        break;
                    }
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mFastScroller != null) {
            mFastScroller.onSizeChanged(w, h, oldw, oldh);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mFastScroller != null && mLayout != null) {
            int h = getVisibleHeight();
            int h2 = mLayout.getHeight();
            mFastScroller.onScroll(this, t, h, h2);
        }
    }

    public int getVisibleHeight() {
        int b = getBottom();
        int t = getTop();
        int pb = getExtendedPaddingBottom();
        int pt = getExtendedPaddingTop();
        return b - t - pb - pt;
    }

    /**
     * Draw this Layout on the specified canvas, with the highlight path drawn
     * between the background and the text.
     * 
     * @param c
     *            the canvas
     * @param highlight
     *            the path of the highlight or cursor; can be null
     * @param highlightPaint
     *            the paint for the highlight
     * @param cursorOffsetVertical
     *            the amount to temporarily translate the canvas while rendering
     *            the highlight
     */
    public void drawView(Canvas c) {
        int dtop, dbottom;
        synchronized (sTempRect) {
            if (!c.getClipBounds(sTempRect)) {
                return;
            }
            dtop = sTempRect.top;
            dbottom = sTempRect.bottom;
        }
        if (mLayout == null) return;
        int textLength = mText.length();
        int top = 0;
        int lineCount = mLayout.getLineCount();
        int bottom = mLayout.getLineTop(lineCount);
        if (dtop > top) {
            top = dtop;
        }
        if (dbottom < bottom) {
            bottom = dbottom;
        }
        int first = mLayout.getLineForVertical(top);
        int last = mLayout.getLineForVertical(bottom);
        int previousLineBottom = mLayout.getLineTop(first);
        int previousLineEnd = mLayout.getLineStart(first);
        TextPaint paint = mTextPaint;
        ParagraphStyle[] spans = NO_PARA_SPANS;
        int previousLineEnd2 = mLayout.getLineStart(first >= 3 ? first - 3 : 0);
        mHighlight.render(mText, previousLineEnd2, mLayout.getLineStart(last + 3 > lineCount ? lineCount : last + 3));
        if (!mShowLineNum && !mShowWhiteSpace) {
            return;
        }
        int lastline = lineCount < 1 ? 1 : lineCount;
        if (lastline != mLineNumber) {
            setLineNumberWidth(lastline);
        }
        if (mNoWrapMode) {
            mLineNumX = mLineNumberWidth + getScrollX();
        } else {
            mLineNumX = mLineNumberWidth;
        }
        int right = getWidth();
        int left = getPaddingLeft();
        if (previousLineEnd > 1) {
            if (previousLineEnd >= mText.length()) return;
            realLineNum = TextUtil.countMatches(mText, '\n', 0, previousLineEnd);
            if (mText.charAt(previousLineEnd) != '\n') {
                realLineNum++;
            }
        } else {
            realLineNum = 1;
        }
        hasNewline = true;
        if (last == 0) {
            c.drawLine(mLineNumX, top, mLineNumX, mTextPaint.getTextSize(), mLineNumberPaint);
            if (hasNewline) {
                String lineString = mLineStr.get(realLineNum);
                if (lineString == null) {
                    lineString = "      " + realLineNum;
                    mLineStr.put(realLineNum, lineString);
                }
                c.drawText(lineString, lineString.length() - mLineNumberLength, lineString.length(), mLineNumX - mLineNumberWidth, mTextPaint.getTextSize(), mLineNumberPaint);
            }
            return;
        }
        for (int i = first; i <= last; i++) {
            int start = previousLineEnd;
            previousLineEnd = mLayout.getLineStart(i + 1);
            int end = getLineVisibleEnd(i, start, previousLineEnd);
            int ltop = previousLineBottom;
            int lbottom = mLayout.getLineTop(i + 1);
            previousLineBottom = lbottom;
            int lbaseline = lbottom;
            int dir = mLayout.getParagraphDirection(i);
            int x;
            if (dir == DIR_LEFT_TO_RIGHT) {
                x = left;
            } else {
                x = right;
            }
            Directions directions = DIRS_ALL_LEFT_TO_RIGHT;
            boolean hasTab = mLayout.getLineContainsTab(i);
            drawText(c, start, end, dir, directions, x, ltop, lbaseline, lbottom, paint, mWorkPaint, hasTab, spans, textLength, i + 1 == last);
        }
    }

    private void drawText(Canvas canvas, int start, int end, int dir, Directions directions, final float x, int top, int y, int bottom, TextPaint paint, TextPaint workPaint, boolean hasTabs, Object[] parspans, int textLength, boolean islastline) {
        if (mShowLineNum) {
            canvas.drawLine(mLineNumX, top, mLineNumX, islastline ? bottom + (bottom - top) : bottom, mLineNumberPaint);
            if (hasNewline) {
                String lineString = mLineStr.get(realLineNum);
                if (lineString == null) {
                    lineString = "      " + realLineNum;
                    mLineStr.put(realLineNum, lineString);
                }
                canvas.drawText(lineString, lineString.length() - mLineNumberLength, lineString.length(), mLineNumX - mLineNumberWidth + 1, y - 2, mLineNumberPaint);
                realLineNum++;
                hasNewline = false;
            }
        }
        float h = 0;
        int here = 0;
        for (int i = 0; i < directions.mDirections.length; i++) {
            int there = here + directions.mDirections[i];
            if (there > end - start) there = end - start;
            int segstart = here;
            for (int j = hasTabs ? here : there; j <= there; j++) {
                if (start + j > end) break;
                char at = start + j == end ? 0 : mText.charAt(start + j);
                if (j == there || at == '\t') {
                    h += Styled.drawText(null, mText, start + segstart, start + j, dir, (i & 1) != 0, x + h, top, y, bottom, paint, workPaint, (start + j == end) || hasTabs);
                    if (j != there && at == '\t' && mShowWhiteSpace) {
                        if (x + h > mLineNumX) {
                            canvas.translate(x + h, y);
                            canvas.drawPath(mWhiteSpacePaths[0], mWhiteSpacePaint);
                            canvas.translate(-x - h, -y);
                        }
                        h = dir * nextTabPos(mText, start, end, h * dir, parspans);
                    } else if (j == there) {
                        if (end < textLength && mText.charAt(end) == '\n') {
                            if (mShowWhiteSpace && x + h > mLineNumX) {
                                canvas.translate(x + h, y);
                                canvas.drawPath(mWhiteSpacePaths[1], mWhiteSpacePaint);
                                canvas.translate(-x - h, -y);
                            }
                            hasNewline = true;
                            break;
                        }
                    }
                    segstart = j + 1;
                }
            }
            here = there;
        }
    }

    static float nextTabPos(CharSequence text, int start, int end, float h, Object[] tabs) {
        float nh = Float.MAX_VALUE;
        boolean alltabs = false;
        if (text instanceof Spanned) {
            if (tabs == null) {
                tabs = ((Spanned) text).getSpans(start, end, TabStopSpan.class);
                alltabs = true;
            }
            for (int i = 0; i < tabs.length; i++) {
                if (!alltabs) {
                    if (!(tabs[i] instanceof TabStopSpan)) continue;
                }
                int where = ((TabStopSpan) tabs[i]).getTabStop();
                if (where < nh && where > h) nh = where;
            }
            if (nh != Float.MAX_VALUE) return nh;
        }
        return ((int) ((h + TAB_INCREMENT) / TAB_INCREMENT)) * TAB_INCREMENT;
    }

    /**
     * Stores information about bidirectional (left-to-right or right-to-left)
     * text within the layout of a line. TODO: This work is not complete or
     * correct and will be fleshed out in a later revision.
     */
    public static class Directions {

        private short[] mDirections;

        Directions(short[] dirs) {
            mDirections = dirs;
        }
    }

    private static final ParagraphStyle[] NO_PARA_SPANS = new ParagraphStyle[] {};

    static final Directions DIRS_ALL_LEFT_TO_RIGHT = new Directions(new short[] { 32767 });

    static final Directions DIRS_ALL_RIGHT_TO_LEFT = new Directions(new short[] { 0, 32767 });

    public static final int DIR_LEFT_TO_RIGHT = 1;

    public static final int DIR_RIGHT_TO_LEFT = -1;

    /**
     * Return the text offset after the last visible character (so whitespace is
     * not counted) on the specified line.
     */
    public int getLineVisibleEnd(int line) {
        return getLineVisibleEnd(line, mLayout.getLineStart(line), mLayout.getLineStart(line + 1));
    }

    private int getLineVisibleEnd(int line, int start, int end) {
        CharSequence text = getText();
        char ch;
        if (line == getLineCount() - 1) {
            return end;
        }
        if (end < 1) return 0;
        for (; end > start; end--) {
            try {
                ch = text.charAt(end - 1);
            } catch (Exception e) {
                return end;
            }
            if (ch == '\n') {
                return end - 1;
            }
            if (ch != ' ' && ch != '\t') {
                break;
            }
        }
        return end;
    }

    public boolean gotoLine(int line) {
        if (line < 1) return false;
        int count = 0;
        int strlen = mText.length();
        for (int index = 0; index < strlen; index++) {
            if (mText.charAt(index) == '\n') {
                count++;
                if (count == line) {
                    Selection.setSelection((Spannable) mText, index, index);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean gotoBackEditLocation() {
        if (mLastEditIndex < 1) return false;
        mLastEditIndex--;
        int offset = mLastEditBuffer.get(mLastEditIndex);
        setSelection(offset, offset);
        return true;
    }

    public boolean gotoForwardEditLocation() {
        if (mLastEditIndex >= mLastEditBuffer.size()) return false;
        mLastEditIndex++;
        int offset = mLastEditBuffer.get(mLastEditIndex);
        setSelection(offset, offset);
        return true;
    }

    public boolean isCanBackEditLocation() {
        if (mLastEditIndex < 1) return false;
        return mLastEditIndex < mLastEditBuffer.size();
    }

    public boolean isCanForwardEditLocation() {
        if (mLastEditIndex >= mLastEditBuffer.size() - 1) return false;
        return true;
    }

    public void setAutoIndent(boolean open) {
        mAutoIndent = open;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean result = super.onKeyDown(keyCode, event);
        if (mAutoIndent && keyCode == KeyEvent.KEYCODE_ENTER) {
            Editable mEditable = (Editable) mText;
            if (mEditable == null) return result;
            int start = getSelectionStart();
            int end = getSelectionEnd();
            if (start == end) {
                int prev = start - 2;
                while (prev >= 0 && mEditable.charAt(prev) != '\n') {
                    prev--;
                }
                prev++;
                int pos = prev;
                while (mEditable.charAt(pos) == ' ' || mEditable.charAt(pos) == '\t' || mEditable.charAt(pos) == '　') {
                    pos++;
                }
                int len = pos - prev;
                if (len > 0) {
                    try {
                        char[] dest = new char[len];
                        mEditable.getChars(prev, pos, dest, 0);
                        mEditable.replace(start, end, new String(dest));
                        setSelection(start + len);
                    } catch (Exception e) {
                    }
                }
            }
        }
        return result;
    }

    public void setEncoding(String encoding) {
        current_encoding = encoding;
    }

    public void setPath(String path) {
        if ("".equals(path)) return;
        current_path = path;
        File f = new File(current_path);
        long fsize = f.length() / 1024;
        if (fsize > Highlight.getLimitFileSize()) {
            Toast.makeText(getContext(), getResources().getString(R.string.highlight_stop_msg), Toast.LENGTH_LONG).show();
            return;
        }
        setCurrentFileExt(FileUtil.getExt(path));
    }

    public void setTitle(String title) {
        current_title = title;
    }

    public void setCurrentFileExt(String ext) {
        current_ext = ext;
        mHighlight.redraw();
        mHighlight.setSyntaxType(current_ext);
    }

    public String getCurrentFileExt() {
        return current_ext;
    }

    public String getEncoding() {
        return current_encoding;
    }

    public String getPath() {
        return current_path;
    }

    public String getTitle() {
        return current_title;
    }

    public void setTextFinger() {
        src_text_length = getText().length();
        byte bytes[] = getString().getBytes();
        mCRC32.update(bytes, 0, bytes.length);
        src_text_crc32 = mCRC32.getValue();
    }

    public boolean isTextChanged() {
        CharSequence text = getText();
        int hash = text.length();
        if (src_text_length != hash) {
            return true;
        }
        byte bytes[] = getString().getBytes();
        mCRC32.update(bytes, 0, bytes.length);
        return src_text_crc32 == mCRC32.getValue();
    }

    public void setHorizontallyScrolling(boolean whether) {
        mNoWrapMode = whether;
        super.setHorizontallyScrolling(whether);
    }

    public void setPaddingLeft(int padding) {
        if (lastPaddingLeft == padding) return;
        if (padding < paddingLeft) padding = paddingLeft;
        lastPaddingLeft = padding;
        setPadding(padding, 0, getPaddingRight(), getPaddingBottom());
    }

    public void setLineBreak(int linebreak) {
        current_linebreak = linebreak;
    }

    public int getLineBreak() {
        return current_linebreak;
    }

    public void showIME(boolean show) {
        JecEditText.setHideKeyboard(!show);
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getResources().getConfiguration().hardKeyboardHidden != Configuration.HARDKEYBOARDHIDDEN_YES) {
            show = false;
        }
        if (show) {
            int type = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
            setInputType(type);
            if (imm != null) {
                imm.showSoftInput(this, 0);
            }
        } else {
            setRawInputType(0);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }

    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_A:
                if (canSelectAll()) {
                    return onTextContextMenuItem(ID_SELECT_ALL);
                }
                break;
            case KeyEvent.KEYCODE_X:
                if (canCut()) {
                    return onTextContextMenuItem(ID_CUT);
                }
                break;
            case KeyEvent.KEYCODE_C:
                if (canCopy()) {
                    return onTextContextMenuItem(ID_COPY);
                }
                break;
            case KeyEvent.KEYCODE_V:
                if (canPaste()) {
                    return onTextContextMenuItem(ID_PASTE);
                }
                break;
        }
        return super.onKeyShortcut(keyCode, event);
    }

    private boolean canSelectAll() {
        if (mText instanceof Spannable && mText.length() != 0 && getMovementMethod() != null && getMovementMethod().canSelectArbitrarily()) {
            return true;
        }
        return false;
    }

    private boolean canSelectText() {
        if (mText instanceof Spannable && mText.length() != 0 && getMovementMethod() != null && getMovementMethod().canSelectArbitrarily()) {
            return true;
        }
        return false;
    }

    private boolean canCut() {
        if (getTransformationMethod() instanceof PasswordTransformationMethod) {
            return false;
        }
        if (mText.length() > 0 && getSelectionStart() >= 0) {
            if (mText instanceof Editable && getKeyListener() != null) {
                return true;
            }
        }
        return false;
    }

    private boolean canCopy() {
        if (getTransformationMethod() instanceof PasswordTransformationMethod) {
            return false;
        }
        if (mText.length() > 0 && getSelectionStart() >= 0) {
            return true;
        }
        return false;
    }

    private boolean canPaste() {
        if (mText instanceof Editable && getKeyListener() != null && getSelectionStart() >= 0 && getSelectionEnd() >= 0) {
            ClipboardManager clip = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clip.hasText()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        if (!isFocused()) {
            if (isFocusable() && getKeyListener() != null) {
                if (canCopy()) {
                    MenuHandler handler = new MenuHandler();
                    int name = R.string.copyAll;
                    menu.add(0, ID_COPY, 0, name).setOnMenuItemClickListener(handler).setAlphabeticShortcut('c');
                    menu.setHeaderTitle(R.string.editTextMenuTitle);
                }
            }
            return;
        }
        MenuHandler handler = new MenuHandler();
        if (canSelectAll()) {
            menu.add(0, ID_SELECT_ALL, 0, R.string.selectAll).setOnMenuItemClickListener(handler).setAlphabeticShortcut('a');
        }
        boolean selection = getSelectionStart() != getSelectionEnd();
        if (canSelectText()) {
            if (MetaKeyKeyListener.getMetaState(mText, META_SELECTING) != 0) {
                menu.add(0, ID_STOP_SELECTING_TEXT, 0, R.string.stopSelectingText).setOnMenuItemClickListener(handler);
            } else {
                menu.add(0, ID_START_SELECTING_TEXT, 0, R.string.selectText).setOnMenuItemClickListener(handler);
            }
        }
        if (canCut()) {
            int name;
            if (selection) {
                name = R.string.cut;
            } else {
                name = R.string.cutAll;
            }
            menu.add(0, ID_CUT, 0, name).setOnMenuItemClickListener(handler).setAlphabeticShortcut('x');
        }
        if (canCopy()) {
            int name;
            if (selection) {
                name = R.string.copy;
            } else {
                name = R.string.copyAll;
            }
            menu.add(0, ID_COPY, 0, name).setOnMenuItemClickListener(handler).setAlphabeticShortcut('c');
        }
        if (canPaste()) {
            menu.add(0, ID_PASTE, 0, R.string.paste).setOnMenuItemClickListener(handler).setAlphabeticShortcut('v');
        }
        if (mText instanceof Spanned) {
            int selStart = getSelectionStart();
            int selEnd = getSelectionEnd();
            int min = Math.min(selStart, selEnd);
            int max = Math.max(selStart, selEnd);
            URLSpan[] urls = ((Spanned) mText).getSpans(min, max, URLSpan.class);
            if (urls.length == 1) {
                menu.add(0, ID_COPY_URL, 0, R.string.copyUrl).setOnMenuItemClickListener(handler);
            }
        }
        menu.add(0, R.id.duplicate_line, 0, R.string.duplicate_line).setOnMenuItemClickListener(handler);
        menu.add(0, R.id.to_lower, 0, R.string.to_lower).setOnMenuItemClickListener(handler);
        menu.add(0, R.id.to_upper, 0, R.string.to_upper).setOnMenuItemClickListener(handler);
        menu.add(0, R.id.go_to_begin, 0, R.string.go_to_begin).setOnMenuItemClickListener(handler);
        menu.add(0, R.id.go_to_end, 0, R.string.go_to_end).setOnMenuItemClickListener(handler);
        menu.add(0, R.id.goto_line, 0, R.string.goto_line).setOnMenuItemClickListener(handler);
        menu.add(0, R.id.insert_datetime, 0, getResources().getString(R.string.insert_datetime) + TimeUtil.getDate()).setOnMenuItemClickListener(handler);
        if (mHideSoftKeyboard) {
            menu.add(0, R.id.show_ime, 0, R.string.show_ime).setOnMenuItemClickListener(handler);
        } else {
            menu.add(0, R.id.hide_ime, 0, R.string.hide_ime).setOnMenuItemClickListener(handler);
        }
        menu.add(0, R.id.doc_stat, 0, R.string.doc_stat).setOnMenuItemClickListener(handler);
        menu.setHeaderTitle(R.string.editTextMenuTitle);
    }

    /**
     * Called when a context menu option for the text view is selected.  Currently
     * this will be one of: {@link android.R.id#selectAll},
     * {@link android.R.id#startSelectingText}, {@link android.R.id#stopSelectingText},
     * {@link android.R.id#cut}, {@link android.R.id#copy},
     * {@link android.R.id#paste}, {@link android.R.id#copyUrl},
     * or {@link android.R.id#switchInputMethod}.
     */
    public boolean onTextContextMenuItem(int id) {
        int selStart = getSelectionStart();
        int selEnd = getSelectionEnd();
        if (!isFocused()) {
            selStart = 0;
            selEnd = mText.length();
        }
        int min = Math.min(selStart, selEnd);
        int max = Math.max(selStart, selEnd);
        if (min < 0) {
            min = 0;
        }
        if (max < 0) {
            max = 0;
        }
        ClipboardManager clip = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        switch(id) {
            case ID_SELECT_ALL:
                Selection.setSelection((Spannable) mText, 0, mText.length());
                return true;
            case ID_CUT:
                if (min == max) {
                    min = 0;
                    max = mText.length();
                }
                clip.setText(mText.subSequence(min, max));
                ((Editable) mText).delete(min, max);
                return true;
            case ID_COPY:
                if (min == max) {
                    min = 0;
                    max = mText.length();
                }
                clip.setText(mText.subSequence(min, max));
                return true;
            case ID_PASTE:
                CharSequence paste = clip.getText();
                if (paste != null) {
                    Selection.setSelection((Spannable) mText, max);
                    ((Editable) mText).replace(min, max, paste);
                }
                return true;
            case ID_COPY_URL:
                URLSpan[] urls = ((Spanned) mText).getSpans(min, max, URLSpan.class);
                if (urls.length == 1) {
                    clip.setText(urls[0].getURL());
                }
                return true;
            case R.id.show_ime:
                showIME(true);
                break;
            case R.id.hide_ime:
                showIME(false);
                break;
            case R.id.to_lower:
            case R.id.to_upper:
                int start = getSelectionStart();
                int end = getSelectionEnd();
                if (start == end) break;
                try {
                    Editable mText2 = getText();
                    char[] dest = new char[end - start];
                    mText2.getChars(start, end, dest, 0);
                    if (id == R.id.to_lower) {
                        mText2.replace(start, end, (new String(dest)).toLowerCase());
                    } else {
                        mText2.replace(start, end, (new String(dest)).toUpperCase());
                    }
                } catch (Exception e) {
                }
                break;
            case R.id.goto_line:
                final EditText lineEditText = new EditText(getContext());
                lineEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.goto_line).setView(lineEditText).setNegativeButton(android.R.string.cancel, null);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            CharSequence lineCharSequence = lineEditText.getText();
                            int line = Integer.valueOf(lineCharSequence.toString());
                            if (!gotoLine(line)) {
                                Toast.makeText(getContext(), R.string.can_not_gotoline, Toast.LENGTH_LONG).show();
                            } else {
                                dialog.dismiss();
                            }
                        } catch (Exception e) {
                        }
                    }
                });
                builder.show();
            case R.id.go_to_begin:
                setSelection(0, 0);
                break;
            case R.id.go_to_end:
                int len = getText().length();
                setSelection(len, len);
                break;
            case R.id.insert_datetime:
                String text = TimeUtil.getDate();
                getText().replace(min, max, text, 0, text.length());
                break;
            case R.id.duplicate_line:
                CharSequence text2;
                int offset;
                if (selStart == selEnd) {
                    int s = selStart, e = selEnd;
                    for (; --s >= 0; ) {
                        if (mText.charAt(s) == '\n') {
                            break;
                        }
                    }
                    int textlen = mText.length();
                    for (; e++ < textlen; ) {
                        if (mText.charAt(e) == '\n') {
                            break;
                        }
                    }
                    if (s < 0) s = 0;
                    if (e >= textlen) e = textlen - 1;
                    text2 = mText.subSequence(s, e);
                    offset = e;
                } else {
                    text2 = mText.subSequence(min, max);
                    offset = max;
                }
                getText().replace(offset, offset, text2, 0, text2.length());
                break;
            case R.id.doc_stat:
                Context context = getContext();
                StringBuilder sb = new StringBuilder();
                Matcher m = Pattern.compile("\\w+").matcher(mText);
                int i = 0;
                while (m.find()) {
                    i++;
                }
                sb.append(context.getString(R.string.filename)).append("\t\t").append(getPath()).append("\n\n").append(context.getString(R.string.total_chars)).append("\t\t").append(mText.length()).append("\n").append(context.getString(R.string.total_words)).append("\t\t").append(i).append("\n").append(context.getString(R.string.total_lines)).append("\t\t").append(TextUtil.countMatches(mText, '\n', 0, mText.length() - 1) + 1);
                new AlertDialog.Builder(context).setTitle(R.string.doc_stat).setMessage(sb.toString()).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
                sb = null;
        }
        return super.onTextContextMenuItem(id);
    }

    private static final int META_SELECTING = 1 << 16;

    private static final int ID_SELECT_ALL = android.R.id.selectAll;

    private static final int ID_START_SELECTING_TEXT = android.R.id.startSelectingText;

    private static final int ID_STOP_SELECTING_TEXT = android.R.id.stopSelectingText;

    private static final int ID_CUT = android.R.id.cut;

    private static final int ID_COPY = android.R.id.copy;

    private static final int ID_PASTE = android.R.id.paste;

    private static final int ID_COPY_URL = android.R.id.copyUrl;

    private class MenuHandler implements MenuItem.OnMenuItemClickListener {

        public boolean onMenuItemClick(MenuItem item) {
            return onTextContextMenuItem(item.getItemId());
        }
    }

    public static void setHideKeyboard(boolean bool) {
        mHideSoftKeyboard = bool;
    }
}
