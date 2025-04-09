package com.endfocus.utilities;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Dimension;
import javax.swing.SwingConstants;

public class Align {

    public static final int LEFT = 0x1;

    public static final int RIGHT = 0x2;

    public static final int HCENTER = 0x3;

    public static final int TOP = 0x4;

    public static final int BOTTOM = 0x8;

    public static final int VCENTER = 0xC;

    public static final int HORIZONTAL = 0x3;

    public static final int VERTICAL = 0xC;

    /**
        Align the coordinate within the given bounds.
        e.g. a piece of text might be aligned in a bounding box (x,y,width, height).
        If we want to align the text vertically:-
            int pos = align(Align.VERTICAL, y, height, textHeight);

        @param align the type of alignment
        @param origin the point arounds which we are aligning
        @param size the size of the item within which we are aligned
        @param itemsize the size of the item to be aligned
     */
    public static final int align(int iAlign, int origin, int size, int itemsize) {
        int iOrigin = origin;
        switch(iAlign) {
            case LEFT:
            case TOP:
                break;
            case RIGHT:
            case BOTTOM:
                iOrigin = origin + (size - itemsize);
                break;
            case HCENTER:
            case VCENTER:
                iOrigin = origin + (size - itemsize) / 2;
                break;
        }
        return iOrigin;
    }

    /**
        Aligns the item within a given bounding box. The point returned is coordinates
        of the aligned item RELATIVE to the origin of the bounding box.

        @see #align
     */
    public static final Point align(int iAlign, int width, int height, int itemLength, int itemHeight) {
        int xAdjust = 0;
        int yAdjust = 0;
        switch(iAlign & HORIZONTAL) {
            case LEFT:
                break;
            case RIGHT:
                xAdjust = width - itemLength;
                break;
            case HCENTER:
                xAdjust = (width - itemLength) / 2;
                break;
        }
        switch(iAlign & VERTICAL) {
            case TOP:
                yAdjust = itemHeight;
                break;
            case BOTTOM:
                yAdjust = height;
                break;
            case VCENTER:
                yAdjust = itemHeight + (height - itemHeight) / 2;
                break;
        }
        return new Point(xAdjust, yAdjust);
    }

    public static Point translateAlign(int align) {
        Point pt = new Point();
        switch(align) {
            case SwingConstants.NORTH_WEST:
                pt.x = LEFT;
                pt.y = TOP;
                break;
            case SwingConstants.NORTH:
                pt.x = HCENTER;
                pt.y = TOP;
                break;
            case SwingConstants.NORTH_EAST:
                pt.x = RIGHT;
                pt.y = TOP;
                break;
            case SwingConstants.WEST:
                pt.x = LEFT;
                pt.y = VCENTER;
                break;
            case SwingConstants.CENTER:
                pt.x = HCENTER;
                pt.y = VCENTER;
                break;
            case SwingConstants.EAST:
                pt.x = RIGHT;
                pt.y = VCENTER;
                break;
            case SwingConstants.SOUTH_WEST:
                pt.x = LEFT;
                pt.y = BOTTOM;
                break;
            case SwingConstants.SOUTH:
                pt.x = HCENTER;
                pt.y = BOTTOM;
                break;
            case SwingConstants.SOUTH_EAST:
                pt.x = RIGHT;
                pt.y = BOTTOM;
                break;
        }
        return pt;
    }

    public static final void alignBoxesVertically(Rectangle r1, Rectangle r2, int alignment, int gap) {
        switch(alignment) {
            case SwingConstants.NORTH:
            case SwingConstants.NORTH_WEST:
            case SwingConstants.NORTH_EAST:
                r1.y = 0;
                r2.y = r1.height + gap;
                break;
            case SwingConstants.SOUTH:
            case SwingConstants.SOUTH_WEST:
            case SwingConstants.SOUTH_EAST:
                r1.y = r2.height + gap;
                r2.y = 0;
                break;
            case SwingConstants.CENTER:
            case SwingConstants.EAST:
            case SwingConstants.WEST:
                if (r1.height > r2.height) {
                    r1.y = 0;
                    r2.y = (r1.height - r2.height) / 2;
                } else {
                    r2.y = 0;
                    r1.y = (r2.height - r1.height) / 2;
                }
                break;
        }
        switch(alignment) {
            case SwingConstants.NORTH:
            case SwingConstants.SOUTH:
            case SwingConstants.CENTER:
                if (r1.width > r2.width) {
                    r1.x = 0;
                    r2.x = (r1.width - r2.width) / 2;
                } else {
                    r1.x = (r2.width - r1.width) / 2;
                    r2.x = 0;
                }
                break;
            case SwingConstants.EAST:
            case SwingConstants.NORTH_EAST:
            case SwingConstants.SOUTH_EAST:
                r1.x = 0;
                r2.x = 0;
                break;
            case SwingConstants.WEST:
            case SwingConstants.NORTH_WEST:
            case SwingConstants.SOUTH_WEST:
                if (r1.width > r2.width) {
                    r1.x = 0;
                    r2.x = r1.width - r2.width;
                } else {
                    r1.x = r2.width - r1.width;
                    r2.x = 0;
                }
                break;
        }
    }

    public static final void alignBoxesHorizontally(Rectangle r1, Rectangle r2, int alignment, int gap) {
        switch(alignment) {
            case SwingConstants.WEST:
            case SwingConstants.NORTH_WEST:
            case SwingConstants.SOUTH_WEST:
                r1.x = 0;
                r2.x = r1.width + gap;
                break;
            case SwingConstants.EAST:
            case SwingConstants.NORTH_EAST:
            case SwingConstants.SOUTH_EAST:
                r1.x = r2.width + gap;
                r2.x = 0;
                break;
            case SwingConstants.CENTER:
            case SwingConstants.NORTH:
            case SwingConstants.SOUTH:
                if (r1.width > r2.width) {
                    r1.x = 0;
                    r2.x = (r1.width - r2.width) / 2;
                } else {
                    r2.x = 0;
                    r1.x = (r2.width - r1.width) / 2;
                }
                break;
        }
        switch(alignment) {
            case SwingConstants.EAST:
            case SwingConstants.WEST:
            case SwingConstants.CENTER:
                if (r1.height > r2.height) {
                    r1.y = 0;
                    r2.y = (r1.height - r2.height) / 2;
                } else {
                    r1.y = (r2.height - r1.height) / 2;
                    r2.y = 0;
                }
                break;
            case SwingConstants.NORTH_EAST:
            case SwingConstants.NORTH:
            case SwingConstants.NORTH_WEST:
                r1.y = 0;
                r2.y = 0;
                break;
            case SwingConstants.SOUTH_WEST:
            case SwingConstants.SOUTH:
            case SwingConstants.SOUTH_EAST:
                if (r1.height > r2.height) {
                    r1.y = 0;
                    r2.y = r1.height - r2.height;
                } else {
                    r1.y = r2.height - r1.height;
                    r2.y = 0;
                }
                break;
        }
    }

    public static final void alignRectangle(Rectangle r, Dimension bounds, int alignment) {
        switch(alignment) {
            case SwingConstants.NORTH_WEST:
                r.x = 0;
                r.y = 0;
                break;
            case SwingConstants.NORTH:
                r.y = 0;
                r.x = (bounds.width - r.width) / 2;
                break;
            case SwingConstants.NORTH_EAST:
                r.y = 0;
                r.x = (bounds.width - r.width);
                break;
            case SwingConstants.WEST:
                r.x = 0;
                r.y = (bounds.height - r.height) / 2;
                break;
            case SwingConstants.CENTER:
                r.x = (bounds.width - r.width) / 2;
                r.y = (bounds.height - r.height) / 2;
                break;
            case SwingConstants.EAST:
                r.x = (bounds.width - r.width);
                r.y = (bounds.height - r.height) / 2;
                break;
            case SwingConstants.SOUTH_WEST:
                r.x = 0;
                r.y = bounds.height - r.height;
                break;
            case SwingConstants.SOUTH:
                r.x = (bounds.width - r.width) / 2;
                r.y = bounds.height - r.height;
                break;
            case SwingConstants.SOUTH_EAST:
                r.x = (bounds.width - r.width);
                r.y = bounds.height - r.height;
                break;
        }
    }
}
