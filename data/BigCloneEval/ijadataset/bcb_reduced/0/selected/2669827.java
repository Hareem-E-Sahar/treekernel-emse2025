package com.thyante.thelibrarian.util;

import java.text.BreakIterator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import com.thyante.thelibrarian.Resources;

public class DrawText {

    public static final int NO_HEIGHT_LIMIT = Integer.MAX_VALUE / 2;

    public static final Pattern PATTERN_LINEBREAK = Pattern.compile("<[bB][rR]/?>");

    public static final Pattern PATTERN_TAG = Pattern.compile("</?[a-zA-Z]+/?>");

    private static final DrawText DRAW_TEXT = new DrawText();

    /**
	 * Returns an instance of the DrawText object.
	 * @return A DrawText instance
	 */
    public static DrawText getInstance() {
        return DRAW_TEXT;
    }

    protected static final int MAX_ITERATIONS = 10;

    /**
	 * The break iterator responsible for breaking the text at line boundaries
	 */
    protected BreakIterator m_breakIterator;

    /**
	 * Map containing links from texts to URLs present in the text represented
	 * by {@link URLText} objects
	 */
    protected Map<String, List<URLText>> m_mapURLs;

    /**
	 * Class drawing a text that is too long to be breakable into several lines by the
	 * BreakIterator policy.
	 * Line breaks are simply inserted when the characters pass the maximum width
	 * provided to the constructor.
	 */
    protected class DrawMultiline {

        /**
		 * The current y coordinate
		 */
        private int m_nCurrentY;

        /**
		 * The string remaining to draw after only complete lines of text have been drawn
		 */
        private String m_strRemainingString;

        /**
		 * Draws the text <code>strTextToDraw</code> on the graphics context <code>gc</code>.
		 * The text is broken into multiple lines; the line breaks are inserted such that
		 * the lines are maximally long, no longer than <code>nMaxWidth</code> pixels.
		 * @param gc The graphics context to draw on
		 * @param strTextToDraw The text to draw
		 * @param x The x coordinate at which the text is drawn and aligned
		 * @param nCurrentY The y coordinate
		 * @param nMaxWidth The maximum width
		 * @param listURLs List of URLs
		 * @param bDraw Specifies whether text is actually drawn or only measured
		 */
        public DrawMultiline(final GC gc, final String strTextToDraw, int nStartIdx, final int x, final int nCurrentY, final int nMaxWidth, final int nMaxHeight, final List<URLText> listURLs, final IUrlDetectionListener listenerURL, final boolean bDraw) {
            m_strRemainingString = strTextToDraw;
            m_nCurrentY = nCurrentY;
            Point pt = gc.textExtent(m_strRemainingString);
            int nExtentOfTextToDraw = pt.x;
            while (nExtentOfTextToDraw > nMaxWidth) {
                if (m_nCurrentY > nCurrentY + nMaxHeight - pt.y) return;
                int nNumChars = DrawText.findNumCharsForWidth(gc, m_strRemainingString, nMaxWidth);
                if (bDraw) {
                    drawLine(gc, m_strRemainingString.substring(0, nNumChars), nStartIdx, nStartIdx + nNumChars, x, m_nCurrentY, listURLs, listenerURL);
                }
                m_strRemainingString = m_strRemainingString.substring(nNumChars);
                nStartIdx += nNumChars;
                nExtentOfTextToDraw = gc.textExtent(m_strRemainingString).x;
                m_nCurrentY += pt.y;
            }
        }

        /**
		 * Returns the y coordinate where new text could be drawn after drawing the text.
		 * @return The y coordinate
		 */
        public int getCurrentY() {
            return m_nCurrentY;
        }

        /**
		 * The text that hasn't been drawn yet.
		 * @return The remaining text that hasn't been drawn yet
		 */
        public String getRemainingText() {
            return m_strRemainingString;
        }
    }

    /**
	 *  
	 */
    protected class URLText {

        private String m_strURL;

        private int m_nIdxStart;

        private int m_nIdxEnd;

        public URLText(String strURL, int nIdxStart, int nIdxEnd) {
            m_strURL = strURL;
            m_nIdxStart = nIdxStart;
            m_nIdxEnd = nIdxEnd;
        }

        /**
		 * Draws the URL on the graphics context <code>gc</code> within the line of text
		 * <code>strLine</code> starting at the coordinates (<code>nLineStartX</code>, <code>y</code>).
		 * @param gc The graphics context to draw on
		 * @param strLine The entire line of text in which the URL is contained
		 * @param nLineStartX The x coordinate at which the line starts
		 * @param y The current y coordinate
		 */
        public void drawURL(GC gc, String strLine, int nStartIdx, int nLineStartX, int y, IUrlDetectionListener listenerURL) {
            Color colOldForeground = gc.getForeground();
            gc.setForeground(Resources.getColor(Resources.COLOR_NIGHTBLUE));
            int nStartIdxURL = Math.max(0, m_nIdxStart - nStartIdx);
            int nEndIdxURL = m_nIdxEnd - nStartIdx;
            int nStartX = nStartIdxURL == 0 ? 0 : gc.textExtent(strLine.substring(0, m_nIdxStart - nStartIdx)).x;
            String strTextToDraw = strLine.length() > nEndIdxURL ? strLine.substring(nStartIdxURL, nEndIdxURL) : strLine.substring(nStartIdxURL);
            Point ptExtent = gc.textExtent(strTextToDraw);
            gc.drawText(strTextToDraw, nLineStartX + nStartX, y, true);
            if (listenerURL != null) listenerURL.onUrlDetected(m_strURL, nLineStartX + nStartX, y, ptExtent.x, ptExtent.y);
            gc.setForeground(colOldForeground);
        }

        public String getURL() {
            return m_strURL;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer("URL: ");
            sb.append(m_strURL);
            sb.append(", Start: ");
            sb.append(m_nIdxStart);
            sb.append(", End: ");
            sb.append(m_nIdxEnd);
            return sb.toString();
        }
    }

    private DrawText() {
        m_mapURLs = new HashMap<String, List<URLText>>();
    }

    /**
	 * Finds the maximum number of characters of <code>strText</code> such that the width of the
	 * substring from the start of <code>strText</code> and containing this number of characters
	 * isn't greater than <code>nWidth</code>.
	 */
    public static int findNumCharsForWidth(GC gc, String strText, int nWidth) {
        int nEndHigh = strText.length();
        int nEndLow = 0;
        Point pt = gc.textExtent(strText);
        if (pt.x < nWidth) return nEndHigh;
        while (nEndLow + 1 < nEndHigh) {
            int nEndMiddle = (nEndLow + nEndHigh) / 2;
            pt = gc.textExtent(strText.substring(0, nEndMiddle));
            if (pt.x < nWidth) nEndLow = nEndMiddle; else nEndHigh = nEndMiddle;
        }
        return nEndLow;
    }

    /**
	 * Draws a line of text. URLs are drawn in a different color.
	 * @param gc The graphics context to draw on
	 * @param strLine The line of text to draw
	 * @param x The x coordinate at which the text is drawn
	 * @param y The y coordinate
	 * @param listURLs The list of {@link URLText} objects representing the URLs to draw
	 */
    protected void drawLine(GC gc, String strLine, int nStartIdx, int nEndIdx, int x, int y, List<URLText> listURLs, IUrlDetectionListener listenerURL) {
        gc.drawText(strLine, x, y, true);
        if (listURLs == null) return;
        for (URLText url : listURLs) {
            if ((url.m_nIdxStart >= nStartIdx && url.m_nIdxEnd <= nEndIdx) || (url.m_nIdxStart >= nStartIdx && url.m_nIdxStart <= nEndIdx) || (url.m_nIdxEnd >= nStartIdx && url.m_nIdxEnd <= nEndIdx) || (url.m_nIdxStart < nStartIdx && url.m_nIdxEnd > nEndIdx)) {
                url.drawURL(gc, strLine, nStartIdx, x, y, listenerURL);
            }
        }
    }

    /**
	 * Draws the text <code>strText</code> at location (<code>x</code>, <code>y</code>).
	 * @param gc The graphics context to draw on
	 * @param strText The text to draw
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param nMaxWidth The maximum horizontal space that is available
	 * @return The y coordinate at which the next line of text could be drawn
	 */
    public int drawText(GC gc, String strText, int x, int y, int nMaxWidth) {
        return drawText(gc, strText, x, y, nMaxWidth, NO_HEIGHT_LIMIT, false, null, null, true);
    }

    /**
	 * Draws the text <code>strText</code> at location (<code>x</code>, <code>y</code>).
	 * @param gc The graphics context to draw on
	 * @param strText The text to draw
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param nMaxWidth The maximum horizontal space that is available
	 * @param bDraw Flag specifying whether the text is actually drawn. If <code>false</code>, the text
	 * 	is only measured.
	 * @return The y coordinate at which the next line of text could be drawn
	 */
    public int drawText(GC gc, String strText, int x, int y, int nMaxWidth, boolean bDraw) {
        return drawText(gc, strText, x, y, nMaxWidth, NO_HEIGHT_LIMIT, false, null, null, bDraw);
    }

    /**
	 * Draws the text <code>strText</code> at location (<code>x</code>, <code>y</code>).
	 * @param gc The graphics context to draw on
	 * @param strText The text to draw
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param nMaxWidth The maximum horizontal space that is available
	 * @param bDetectURLs Specifies whether URLs should be detected and rendered
	 * @param listenerURL Listener that listens for URL detections. Might be <code>null</code>
	 * 	if URL detection or URL detection feedback is not needed.
	 * @return The y coordinate at which the next line of text could be drawn
	 */
    public int drawText(GC gc, String strText, int x, int y, int nMaxWidth, boolean bDetectURLs, IUrlDetectionListener listenerURL) {
        return drawText(gc, strText, x, y, nMaxWidth, NO_HEIGHT_LIMIT, bDetectURLs, listenerURL, null, true);
    }

    /**
	 * Draws the text <code>strText</code> at location (<code>x</code>, <code>y</code>).
	 * @param gc The graphics context to draw on
	 * @param strText The text to draw
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param nMaxWidth The maximum horizontal space that is available
	 * @param bDetectURLs Specifies whether URLs should be detected and rendered
	 * @param listenerURL Listener that listens for URL detections. Might be <code>null</code>
	 * 	if URL detection or URL detection feedback is not needed.
	 * @param bDraw Flag specifying whether the text is actually drawn. If <code>false</code>, the text
	 * 	is only measured.
	 * @return The y coordinate at which the next line of text could be drawn
	 */
    public int drawText(GC gc, String strText, int x, int y, int nMaxWidth, boolean bDetectURLs, IUrlDetectionListener listenerURL, boolean bDraw) {
        return drawText(gc, strText, x, y, nMaxWidth, NO_HEIGHT_LIMIT, bDetectURLs, listenerURL, null, bDraw);
    }

    /**
	 * Draws the text <code>strText</code> at location (<code>x</code>, <code>y</code>).
	 * @param gc The graphics context to draw on
	 * @param strText The text to draw
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param nMaxWidth The maximum horizontal space that is available
	 * @param bDetectURLs Specifies whether URLs should be detected and rendered
	 * @param listenerURL Listener that listens for URL detections. Might be <code>null</code>
	 * 	if URL detection or URL detection feedback is not needed.
	 * @param bDraw Flag specifying whether the text is actually drawn. If <code>false</code>, the text
	 * 	is only measured.
	 * @return The y coordinate at which the next line of text could be drawn
	 */
    public int drawText(GC gc, String strText, int x, int y, int nMaxWidth, int nMaxHeight, StringBuffer sbRemainingText, boolean bDraw) {
        return drawText(gc, strText, x, y, nMaxWidth, nMaxHeight, false, null, sbRemainingText, bDraw);
    }

    /**
	 * Draws the text <code>strText</code> at location (<code>x</code>, <code>y</code>).
	 * @param gc The graphics context to draw on
	 * @param strText The text to draw
	 * @param x The x coordinate
	 * @param y The y coordinate
	 * @param nMaxWidth The maximum horizontal space that is available
	 * @param bDetectURLs Specifies whether URLs should be detected and rendered
	 * @param listenerURL Listener that listens for URL detections. Might be <code>null</code>
	 * 	if URL detection or URL detection feedback is not needed.
	 * @param bDraw Flag specifying whether the text is actually drawn. If <code>false</code>, the text
	 * 	is only measured.
	 * @return The y coordinate at which the next line of text could be drawn
	 */
    public int drawText(final GC gc, String strText, final int x, final int y, final int nMaxWidth, final int nMaxHeight, final boolean bDetectURLs, final IUrlDetectionListener listenerURL, final StringBuffer sbRemainingText, final boolean bDraw) {
        if (sbRemainingText != null) sbRemainingText.setLength(0);
        strText = PATTERN_LINEBREAK.matcher(strText).replaceAll("\n");
        strText = PATTERN_TAG.matcher(strText).replaceAll("");
        List<URLText> listURLs = null;
        if (bDetectURLs && bDraw) {
            listURLs = m_mapURLs.get(strText);
            if (listURLs == null) {
                m_mapURLs.put(strText, listURLs = new LinkedList<URLText>());
                Matcher matcher = URLUtil.PATTERN_URL.matcher(strText);
                while (matcher.find()) listURLs.add(new URLText(matcher.group(1), matcher.start(), matcher.end()));
            }
        }
        Point pt = gc.textExtent(strText);
        if (pt.x < nMaxWidth) {
            if (bDraw) drawLine(gc, strText, 0, strText.length(), x, y, listURLs, listenerURL);
            return y + pt.y;
        }
        if (m_breakIterator == null) m_breakIterator = BreakIterator.getLineInstance();
        m_breakIterator.setText(strText);
        int nCurrentY = y;
        int nStart = m_breakIterator.first();
        int nLastEnd = nStart;
        String strLast = "";
        for (int nEnd = m_breakIterator.next(); nEnd != BreakIterator.DONE; nEnd = m_breakIterator.next()) {
            String s = strText.substring(nStart, nEnd);
            pt = gc.textExtent(s);
            if (pt.x > nMaxWidth) {
                if (gc.textExtent(strText.substring(nLastEnd, nEnd)).x > nMaxWidth) {
                    DrawMultiline dm = new DrawMultiline(gc, s, nStart, x, nCurrentY, nMaxWidth, nMaxHeight, listURLs, listenerURL, bDraw);
                    nCurrentY = dm.getCurrentY();
                    if (nCurrentY > y + nMaxHeight) {
                        if (sbRemainingText != null) {
                            sbRemainingText.append(dm.getRemainingText());
                            sbRemainingText.append(strText.substring(nEnd));
                        }
                        return nCurrentY;
                    }
                    nStart += s.length() - dm.getRemainingText().length();
                    strLast = dm.getRemainingText();
                } else {
                    if (bDraw) drawLine(gc, strLast, nStart, nLastEnd, x, nCurrentY, listURLs, listenerURL);
                    nCurrentY += pt.y;
                    if (nCurrentY > y + nMaxHeight) {
                        if (sbRemainingText != null) sbRemainingText.append(strText.substring(nEnd));
                        return nCurrentY;
                    }
                    nStart = nLastEnd;
                    strLast = strText.substring(nLastEnd, nEnd);
                }
            } else strLast = s;
            nLastEnd = nEnd;
        }
        if (nCurrentY > y + nMaxHeight) {
            if (sbRemainingText != null) sbRemainingText.append(strText.substring(nStart));
            return nCurrentY;
        }
        String s = strText.substring(nStart);
        pt = gc.textExtent(s);
        if (pt.x < nMaxWidth) {
            if (bDraw) drawLine(gc, s, nStart, strText.length(), x, nCurrentY, listURLs, listenerURL);
        } else {
            DrawMultiline dm = new DrawMultiline(gc, s, nStart, x, nCurrentY, nMaxWidth, nMaxHeight, listURLs, listenerURL, bDraw);
            nCurrentY = dm.getCurrentY();
            if (nCurrentY > y + nMaxHeight) {
                if (sbRemainingText != null) sbRemainingText.append(dm.getRemainingText());
                return nCurrentY;
            }
            if (!"".equals(dm.getRemainingText())) {
                int nLen = strText.length();
                if (bDraw) drawLine(gc, dm.getRemainingText(), nLen - dm.getRemainingText().length(), nLen, x, nCurrentY, listURLs, listenerURL);
            }
        }
        nCurrentY += pt.y;
        return nCurrentY;
    }
}
