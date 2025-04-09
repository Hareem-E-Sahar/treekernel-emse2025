package org.openintents.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * ColorCircle.
 * 
 * @author Peli, based on API demo code.
 * 
 */
public class ColorCircle extends View {

    private float center_radius;

    private static final float CENTER_RADIUS_SCALE = 0.4f;

    private Paint mPaint;

    private Paint mCenterPaint;

    private int[] mColors;

    private OnColorChangedListener mListener;

    /**
	 * Constructor. This version is only needed for instantiating the object
	 * manually (not from a layout XML file).
	 * 
	 * @param context
	 */
    public ColorCircle(Context context) {
        super(context);
        init();
    }

    /**
	 * Construct object, initializing with any attributes we understand from a
	 * layout file.
	 * 
	 * These attributes are defined in res/values/attrs.xml .
	 * 
	 * @see android.view.View#View(android.content.Context,
	 *      android.util.AttributeSet, java.util.Map)
	 */
    public ColorCircle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
	 * Initializes variables.
	 */
    void init() {
        mColors = new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };
        Shader s = new SweepGradient(0, 0, mColors, null);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setShader(s);
        mPaint.setStyle(Paint.Style.STROKE);
        mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterPaint.setStrokeWidth(5);
    }

    private boolean mTrackingCenter;

    private boolean mHighlightCenter;

    @Override
    protected void onDraw(Canvas canvas) {
        float outer_radius = Math.min(getWidth(), getHeight()) / 2;
        float touch_feedback_ring = center_radius + 2 * mCenterPaint.getStrokeWidth();
        float r = (outer_radius + touch_feedback_ring) / 2;
        canvas.translate(getWidth() / 2, getHeight() / 2);
        mPaint.setStrokeWidth(outer_radius - touch_feedback_ring);
        canvas.drawCircle(0, 0, r, mPaint);
        canvas.drawCircle(0, 0, center_radius, mCenterPaint);
        if (mTrackingCenter) {
            int c = mCenterPaint.getColor();
            mCenterPaint.setStyle(Paint.Style.STROKE);
            if (mHighlightCenter) {
                mCenterPaint.setAlpha(0xFF);
            } else {
                mCenterPaint.setAlpha(0x80);
            }
            canvas.drawCircle(0, 0, center_radius + mCenterPaint.getStrokeWidth(), mCenterPaint);
            mCenterPaint.setStyle(Paint.Style.FILL);
            mCenterPaint.setColor(c);
        }
    }

    /**
	 * @see android.view.View#measure(int, int)
	 */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int max_width = MeasureSpec.getSize(widthMeasureSpec);
        int max_height = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(max_width, max_height);
        this.center_radius = CENTER_RADIUS_SCALE * size / 2;
        setMeasuredDimension(size, size);
    }

    public void setColor(int color) {
        mCenterPaint.setColor(color);
        invalidate();
    }

    public int getColor() {
        return mCenterPaint.getColor();
    }

    public void setOnColorChangedListener(OnColorChangedListener colorListener) {
        mListener = colorListener;
    }

    private int ave(int s, int d, float p) {
        return s + java.lang.Math.round(p * (d - s));
    }

    private int interpColor(int colors[], float unit) {
        if (unit <= 0) {
            return colors[0];
        }
        if (unit >= 1) {
            return colors[colors.length - 1];
        }
        float p = unit * (colors.length - 1);
        int i = (int) p;
        p -= i;
        int c0 = colors[i];
        int c1 = colors[i + 1];
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0), Color.red(c1), p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0), Color.blue(c1), p);
        return Color.argb(a, r, g, b);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - getWidth() / 2;
        float y = event.getY() - getHeight() / 2;
        boolean inCenter = PointF.length(x, y) <= center_radius;
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTrackingCenter = inCenter;
                if (inCenter) {
                    mHighlightCenter = true;
                    invalidate();
                    break;
                }
            case MotionEvent.ACTION_MOVE:
                if (mTrackingCenter) {
                    if (mHighlightCenter != inCenter) {
                        mHighlightCenter = inCenter;
                        invalidate();
                    }
                } else {
                    float angle = (float) java.lang.Math.atan2(y, x);
                    float unit = angle / (2 * (float) Math.PI);
                    if (unit < 0) {
                        unit += 1;
                    }
                    int newcolor = interpColor(mColors, unit);
                    mCenterPaint.setColor(newcolor);
                    if (mListener != null) {
                        mListener.onColorChanged(this, newcolor);
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTrackingCenter) {
                    if (inCenter) {
                        if (mListener != null) {
                            mListener.onColorPicked(this, mCenterPaint.getColor());
                        }
                    }
                    mTrackingCenter = false;
                    invalidate();
                }
                break;
        }
        return true;
    }
}
