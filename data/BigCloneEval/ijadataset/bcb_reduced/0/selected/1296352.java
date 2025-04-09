package de.jaret.util.swt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Simple utility class for rendering a multiline text.
 * 
 * @author Peter Kliem
 * @version $Id: TextRenderer.java 623 2007-11-01 15:23:45Z kliem $
 */
public class TextRenderer {

    public static final int LEFT = 0;

    public static final int RIGHT = 1;

    public static final int CENTER = 2;

    public static final int TOP = 0;

    public static final int BOTTOM = 1;

    public static void renderText(GC gc, Rectangle rect, boolean wrap, boolean ellipsis, String text) {
        renderText(gc, rect, wrap, ellipsis, text, LEFT, TOP);
    }

    public static void renderText(GC gc, Rectangle rect, boolean wrap, boolean ellipsis, String text, int halign, int valign) {
        Rectangle clipSave = gc.getClipping();
        gc.setClipping(rect.intersection(clipSave));
        List<String> lines = breakInLines(gc, rect.width, wrap, text);
        int height = getHeight(gc, rect.width, wrap, lines);
        int offy = 0;
        if (height < rect.height) {
            if (valign == CENTER) {
                offy = (rect.height - height) / 2;
            } else if (valign == BOTTOM) {
                offy = rect.height - height;
            }
        }
        int lineheight = SwtGraphicsHelper.getStringDrawingHeight(gc, "WgyAqQ");
        int lineSpacing = 3;
        for (int row = 0; row < lines.size(); row++) {
            int y = rect.y + row * lineheight + row * lineSpacing;
            drawLine(gc, rect.x, y + offy, rect.width, lines.get(row), halign);
        }
        gc.setClipping(clipSave);
    }

    public static int getHeight(GC gc, int width, boolean wrap, String text) {
        List<String> lines = breakInLines(gc, width, wrap, text);
        return getHeight(gc, width, wrap, lines);
    }

    private static int getHeight(GC gc, int width, boolean wrap, List<String> lines) {
        if (lines.size() == 0) {
            return 0;
        }
        int lineheight = SwtGraphicsHelper.getStringDrawingHeight(gc, lines.get(0));
        int lineSpacing = 3;
        int height = lines.size() * lineheight + ((lines.size() - 1) * lineSpacing);
        return height;
    }

    private static void drawLine(GC gc, int x, int y, int width, String string, int halign) {
        int xx;
        int textWidth = SwtGraphicsHelper.getStringDrawingWidth(gc, string);
        switch(halign) {
            case LEFT:
                xx = x;
                break;
            case RIGHT:
                xx = x + (width - textWidth);
                break;
            case CENTER:
                xx = x + (width - textWidth) / 2;
                break;
            default:
                throw new RuntimeException("illegal alignment");
        }
        gc.drawText(string, xx, y, true);
    }

    public static List<String> breakInLines(GC gc, int width, boolean wrap, String text) {
        List<String> result = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(text, "\n", false);
        while (tokenizer.hasMoreTokens()) {
            result.add(tokenizer.nextToken());
        }
        if (wrap) {
            List<String> brokenLines = new ArrayList<String>();
            Iterator it = result.iterator();
            while (it.hasNext()) {
                String line = (String) it.next();
                List<String> brLines = wrapLines(gc, width, line);
                brokenLines.addAll(brLines);
            }
            return brokenLines;
        } else {
            return result;
        }
    }

    /**
     * @param gc
     * @param width
     * @param text
     * @return
     */
    private static List<String> wrapLines(GC gc, int width, String text) {
        List<String> result = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(text, " ", true);
        StringBuffer buf = new StringBuffer();
        int count = 0;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (SwtGraphicsHelper.getStringDrawingWidth(gc, buf.toString() + token) < width) {
                buf.append(token);
                count++;
            } else if (count == 0) {
                List<String> brWord = breakWord(gc, width, token);
                for (int i = 0; i < brWord.size() - 1; i++) {
                    result.add(brWord.get(i));
                }
                buf.append(brWord.get(brWord.size() - 1));
                count = 1;
            } else {
                result.add(buf.toString());
                buf = new StringBuffer();
                buf.append(token);
                count = 1;
            }
        }
        if (buf.length() > 0) {
            result.add(buf.toString());
        }
        return result;
    }

    /**
     * break a word into strings fitting into width.
     * 
     * @param gc
     * @param width
     * @param word
     * @return
     */
    private static List<String> breakWord(GC gc, int width, String word) {
        List<String> result = new ArrayList<String>();
        int bidx = 0;
        int eidx = 0;
        while (eidx < word.length()) {
            while (SwtGraphicsHelper.getStringDrawingWidth(gc, word.substring(bidx, eidx)) < width && eidx < word.length()) {
                eidx++;
            }
            if (eidx == 0) {
                result.add(word);
                return result;
            }
            result.add(word.substring(bidx, eidx));
            bidx = eidx;
        }
        return result;
    }
}
