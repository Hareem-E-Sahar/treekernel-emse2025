package com.thyante.thelibrarian.components;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Item;

/**
 * A gallery item.
 * @author Matthias-M. Christen
 */
public class MediaGalleryItem extends Item {

    protected MediaGalleryGroup m_group;

    protected boolean m_bIsSelected;

    protected Rectangle m_rectBounds;

    public MediaGalleryItem(MediaGalleryGroup group, int nStyle) {
        super(group.getControl(), nStyle);
        m_group = group;
        m_group.addItem(this);
        m_bIsSelected = false;
        m_rectBounds = new Rectangle(0, 0, 0, 0);
    }

    public MediaGalleryGroup getGroup() {
        return m_group;
    }

    public void setSelected(boolean bSelected) {
        m_bIsSelected = bSelected;
    }

    public boolean isSelected() {
        return m_bIsSelected;
    }

    public void setBounds(int x, int y, int nWidth, int nHeight) {
        m_rectBounds.x = x;
        m_rectBounds.y = y;
        m_rectBounds.width = nWidth;
        m_rectBounds.height = nHeight;
    }

    public Rectangle getBounds() {
        return m_rectBounds;
    }

    /**
	 * Draws the item.
	 * @param gc
	 * @param x
	 * @param y
	 * @param nWidth
	 * @param nHeight
	 * @param nFontHeight
	 * @param colForeground
	 * @param colBackground
	 * @param colSelectionForeground
	 * @param colSelectionBackground
	 */
    void draw(GC gc, int x, int y, int nWidth, int nHeight, int nFontHeight, Color colForeground, Color colBackground, Color colSelectionForeground, Color colSelectionBackground) {
        boolean bHasText = getText() != null && !"".equals(getText());
        if (m_bIsSelected) {
            gc.setBackground(colSelectionBackground);
            gc.setForeground(colSelectionBackground);
            gc.fillRoundRectangle(x, y, nWidth, nHeight - nFontHeight - 1, 15, 15);
            if (bHasText) gc.fillRoundRectangle(x, y + nHeight - nFontHeight, nWidth, nFontHeight, 15, 15);
        }
        Image img = getImage();
        if (img != null) {
            Rectangle rectImageBounds = img.getBounds();
            gc.drawImage(img, x + (nWidth - rectImageBounds.width) / 2, y + (nHeight - nFontHeight - rectImageBounds.height) / 2);
        }
        if (bHasText) {
            gc.setForeground(m_bIsSelected ? colSelectionForeground : colForeground);
            gc.setBackground(m_bIsSelected ? colSelectionBackground : colBackground);
            String strText = createLabel(gc, getText(), nWidth - nFontHeight);
            int nTextWidth = gc.textExtent(strText).x;
            gc.drawText(strText, x + (nWidth - nTextWidth) / 2, y + nHeight - nFontHeight);
        }
    }

    private static final String ELLIPSIS = "...";

    /**
	 * Shorten the given text <code>text</code> so that its length doesn't
	 * exceed the given width. The default implementation replaces characters in
	 * the center of the original string with an ellipsis ("..."). Override if
	 * you need a different strategy.
	 * 
	 * Note: Code originally from org.eclipse.cwt.CLabel
	 * 
	 * @param gc
	 *            the graphics context to use for text measurement
	 * @param strText
	 *            the text to shorten
	 * @param nWidth
	 *            the width to shorten the text to, in pixels
	 * @return the shortened text
	 */
    protected static String createLabel(GC gc, String strText, int nWidth) {
        if (strText == null) return null;
        final int nExtent = gc.textExtent(strText).x;
        if (nExtent > nWidth) {
            final int w = gc.textExtent(ELLIPSIS).x;
            if (nWidth <= w) return strText;
            final int l = strText.length();
            int nMax = l / 2;
            int nMin = 0;
            int nAvg = (nMax + nMin) / 2 - 1;
            if (nAvg <= 0) return strText;
            while (nMin < nAvg && nAvg < nMax) {
                final String s1 = strText.substring(0, nAvg);
                final String s2 = strText.substring(l - nAvg, l);
                final int l1 = gc.textExtent(s1).x;
                final int l2 = gc.textExtent(s2).x;
                if (l1 + w + l2 > nWidth) {
                    nMax = nAvg;
                    nAvg = (nMax + nMin) / 2;
                } else if (l1 + w + l2 < nWidth) {
                    nMin = nAvg;
                    nAvg = (nMax + nMin) / 2;
                } else nMin = nMax;
            }
            if (nAvg == 0) return strText;
            return strText.substring(0, nAvg) + ELLIPSIS + strText.substring(l - nAvg, l);
        }
        return strText;
    }
}
