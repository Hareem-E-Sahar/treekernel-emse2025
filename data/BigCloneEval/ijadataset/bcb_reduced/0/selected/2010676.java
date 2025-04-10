package org.gudy.azureus2.ui.swt.shells;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.SystemTime;
import com.aelitis.azureus.ui.swt.imageloader.ImageLoader;

/**
 * @author Olivier Chalouhi
 * @author TuxPaper (rewrite)
 */
public class GCStringPrinter {

    private static final boolean DEBUG = false;

    private static final String GOOD_STRING = "(/|,jI~`gy";

    public static final int FLAG_SKIPCLIP = 1;

    public static final int FLAG_FULLLINESONLY = 2;

    public static final int FLAG_NODRAW = 4;

    public static final int FLAG_KEEP_URL_INFO = 8;

    private static final Pattern patHREF = Pattern.compile("<\\s*?a\\s.*?href\\s*?=\\s*?\"(.+?)\".*?>(.*?)<\\s*?/a\\s*?>", Pattern.CASE_INSENSITIVE);

    private static final Pattern patAHREF_TITLE = Pattern.compile("title=\\\"([^\\\"]+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern patAHREF_TARGET = Pattern.compile("target=\\\"([^\\\"]+)", Pattern.CASE_INSENSITIVE);

    private static final int MAX_LINE_LEN = 4000;

    private static final int MAX_WORD_LEN = 4000;

    private boolean cutoff;

    private GC gc;

    private String string;

    private Rectangle printArea;

    private int swtFlags;

    private int printFlags;

    private Point size;

    private Color urlColor;

    private List listUrlInfo;

    private Image[] images;

    private float[] imageScales;

    private int iCurrentHeight;

    private boolean wrap;

    public static class URLInfo {

        public String url;

        public String text;

        public Color urlColor;

        public Color dropShadowColor;

        int relStartPos;

        public List hitAreas = null;

        int titleLength;

        public String fullString;

        public String title;

        public String target;

        public boolean urlUnderline;

        public String toString() {
            return super.toString() + ": relStart=" + relStartPos + ";url=" + url + ";title=" + text + ";hit=" + (hitAreas == null ? 0 : hitAreas.size());
        }
    }

    private class LineInfo {

        public int width;

        String originalLine;

        String lineOutputed;

        int excessPos;

        public int relStartPos;

        public int height;

        public int imageIndexes[];

        public LineInfo(String originalLine, int relStartPos) {
            this.originalLine = originalLine;
            this.relStartPos = relStartPos;
        }

        public String toString() {
            return super.toString() + ": relStart=" + relStartPos + ";xcess=" + excessPos + ";orig=" + originalLine + ";output=" + lineOutputed;
        }
    }

    public static boolean printString(GC gc, String string, Rectangle printArea) {
        return printString(gc, string, printArea, false, false);
    }

    public static boolean printString(GC gc, String string, Rectangle printArea, boolean skipClip, boolean fullLinesOnly) {
        return printString(gc, string, printArea, skipClip, fullLinesOnly, SWT.WRAP | SWT.TOP);
    }

    /**
	 * 
	 * @param gc GC to print on
	 * @param string Text to print
	 * @param printArea Area of GC to print text to
	 * @param skipClip Don't set any clipping on the GC.  Text may overhang 
	 *                 printArea when this is true
	 * @param fullLinesOnly If bottom of a line will be chopped off, do not display it
	 * @param swtFlags SWT flags.  SWT.CENTER, SWT.BOTTOM, SWT.TOP, SWT.WRAP
	 * @return whether it fit
	 */
    public static boolean printString(GC gc, String string, Rectangle printArea, boolean skipClip, boolean fullLinesOnly, int swtFlags) {
        try {
            GCStringPrinter sp = new GCStringPrinter(gc, string, printArea, skipClip, fullLinesOnly, swtFlags);
            return sp.printString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean _printString() {
        boolean b = false;
        try {
            boolean wasAdvanced = gc.getAdvanced();
            Rectangle clipping = null;
            if (gc.getAdvanced() && gc.getTextAntialias() == SWT.DEFAULT && gc.getAlpha() == 255) {
                clipping = gc.getClipping();
                gc.setAdvanced(false);
                gc.setClipping(clipping);
            }
            b = __printString();
            if (wasAdvanced) {
                gc.setAdvanced(true);
                gc.setClipping(clipping);
            }
        } catch (Throwable t) {
            Debug.out(t);
        }
        if (DEBUG) {
            System.out.println("");
        }
        return b;
    }

    /**
	 * @param gc
	 * @param string
	 * @param printArea
	 * @param printFlags
	 * @param swtFlags
	 * @return
	 *
	 * @since 3.0.4.3
	 */
    private boolean __printString() {
        size = new Point(0, 0);
        if (string == null) {
            return false;
        }
        if (printArea == null || printArea.isEmpty()) {
            return false;
        }
        ArrayList<LineInfo> lines = new ArrayList<LineInfo>(1);
        while (string.indexOf('\t') >= 0) {
            string = string.replace('\t', ' ');
        }
        if (string.indexOf("  ") > 0) {
            string = string.replaceAll("  +", " ");
        }
        boolean hasSlashR = string.indexOf('\r') > 0;
        boolean fullLinesOnly = (printFlags & FLAG_FULLLINESONLY) > 0;
        boolean skipClip = (printFlags & FLAG_SKIPCLIP) > 0;
        boolean noDraw = (printFlags & FLAG_NODRAW) > 0;
        wrap = (swtFlags & SWT.WRAP) > 0;
        if (string.indexOf('<') >= 0) {
            if ((printFlags & FLAG_KEEP_URL_INFO) == 0) {
                Matcher htmlMatcher = patHREF.matcher(string);
                boolean hasURL = htmlMatcher.find();
                if (hasURL) {
                    listUrlInfo = new ArrayList(1);
                    while (hasURL) {
                        URLInfo urlInfo = new URLInfo();
                        urlInfo.fullString = htmlMatcher.group();
                        urlInfo.relStartPos = htmlMatcher.start(0);
                        urlInfo.url = string.substring(htmlMatcher.start(1), htmlMatcher.end(1));
                        urlInfo.text = string.substring(htmlMatcher.start(2), htmlMatcher.end(2));
                        urlInfo.titleLength = urlInfo.text.length();
                        Matcher matcherTitle = patAHREF_TITLE.matcher(urlInfo.fullString);
                        if (matcherTitle.find()) {
                            urlInfo.title = string.substring(urlInfo.relStartPos + matcherTitle.start(1), urlInfo.relStartPos + matcherTitle.end(1));
                        }
                        Matcher matcherTarget = patAHREF_TARGET.matcher(urlInfo.fullString);
                        if (matcherTarget.find()) {
                            urlInfo.target = string.substring(urlInfo.relStartPos + matcherTarget.start(1), urlInfo.relStartPos + matcherTarget.end(1));
                        }
                        string = htmlMatcher.replaceFirst(urlInfo.text.replaceAll("\\$", "\\\\\\$"));
                        listUrlInfo.add(urlInfo);
                        htmlMatcher = patHREF.matcher(string);
                        hasURL = htmlMatcher.find(urlInfo.relStartPos);
                    }
                }
            } else {
                Matcher htmlMatcher = patHREF.matcher(string);
                string = htmlMatcher.replaceAll("$2");
            }
        }
        Rectangle rectDraw = new Rectangle(printArea.x, printArea.y, printArea.width, printArea.height);
        Rectangle oldClipping = null;
        try {
            if (!skipClip && !noDraw) {
                oldClipping = gc.getClipping();
                gc.setClipping(printArea);
            }
            iCurrentHeight = 0;
            int currentCharPos = 0;
            int posNewLine = string.indexOf('\n');
            if (hasSlashR) {
                int posR = string.indexOf('\r');
                if (posR == -1) {
                    posR = posNewLine;
                }
                posNewLine = Math.min(posNewLine, posR);
            }
            if (posNewLine < 0) {
                posNewLine = string.length();
            }
            int posLastNewLine = 0;
            while (posNewLine >= 0 && posLastNewLine < string.length()) {
                String sLine = string.substring(posLastNewLine, posNewLine);
                do {
                    LineInfo lineInfo = new LineInfo(sLine, currentCharPos);
                    lineInfo = processLine(gc, lineInfo, printArea, fullLinesOnly, false);
                    String sProcessedLine = (String) lineInfo.lineOutputed;
                    if (sProcessedLine != null && sProcessedLine.length() > 0) {
                        if (lineInfo.width == 0 || lineInfo.height == 0) {
                            Point gcExtent = gc.stringExtent(sProcessedLine);
                            if (lineInfo.width == 0) {
                                lineInfo.width = gcExtent.x;
                            }
                            if (lineInfo.height == 0) {
                                lineInfo.height = gcExtent.y;
                            }
                        }
                        Point extent = new Point(lineInfo.width, lineInfo.height);
                        iCurrentHeight += extent.y;
                        boolean isOverY = iCurrentHeight > printArea.height;
                        if (DEBUG) {
                            System.out.println("Adding Line: [" + sProcessedLine + "]" + sProcessedLine.length() + "; h=" + iCurrentHeight + "(" + printArea.height + "). fullOnly?" + fullLinesOnly + ". Excess: " + lineInfo.excessPos);
                        }
                        if (isOverY && !fullLinesOnly) {
                            lines.add(lineInfo);
                        } else if (isOverY && fullLinesOnly && lines.size() > 0) {
                            String excess = lineInfo.excessPos >= 0 ? sLine.substring(lineInfo.excessPos) : null;
                            if (excess != null) {
                                if (fullLinesOnly) {
                                    if (lines.size() > 0) {
                                        lineInfo = lines.remove(lines.size() - 1);
                                        sProcessedLine = lineInfo.originalLine.length() > MAX_LINE_LEN ? lineInfo.originalLine.substring(0, MAX_LINE_LEN) : lineInfo.originalLine;
                                        extent = gc.stringExtent(sProcessedLine);
                                    } else {
                                        if (DEBUG) {
                                            System.out.println("No PREV!?");
                                        }
                                        return false;
                                    }
                                } else {
                                    sProcessedLine = sProcessedLine.length() > MAX_LINE_LEN ? sProcessedLine.substring(0, MAX_LINE_LEN) : sProcessedLine;
                                }
                                if (excess.length() > MAX_LINE_LEN) {
                                    excess = excess.substring(0, MAX_LINE_LEN);
                                }
                                StringBuffer outputLine = new StringBuffer(sProcessedLine);
                                lineInfo.width = extent.x;
                                wrap = false;
                                int newExcessPos = processWord(gc, sProcessedLine, " " + excess, printArea, lineInfo, outputLine, new StringBuffer());
                                if (DEBUG) {
                                    System.out.println("  with word [" + excess + "] len is " + lineInfo.width + "(" + printArea.width + ") w/excess " + newExcessPos);
                                }
                                lineInfo.lineOutputed = outputLine.toString();
                                lines.add(lineInfo);
                                if (DEBUG) {
                                    System.out.println("replace prev line with: " + outputLine.toString());
                                }
                            } else {
                                if (DEBUG) {
                                    System.out.println("No Excess");
                                }
                            }
                            cutoff = true;
                            return false;
                        } else {
                            lines.add(lineInfo);
                        }
                        sLine = lineInfo.excessPos >= 0 && wrap ? sLine.substring(lineInfo.excessPos) : null;
                    } else {
                        if (DEBUG) {
                            System.out.println("Line process resulted in no text: " + sLine);
                        }
                        lines.add(lineInfo);
                        currentCharPos++;
                        break;
                    }
                    currentCharPos += lineInfo.excessPos >= 0 ? lineInfo.excessPos : lineInfo.lineOutputed.length();
                } while (sLine != null);
                if (string.length() > posNewLine && string.charAt(posNewLine) == '\r' && string.charAt(posNewLine + 1) == '\n') {
                    posNewLine++;
                }
                posLastNewLine = posNewLine + 1;
                currentCharPos = posLastNewLine;
                posNewLine = string.indexOf('\n', posLastNewLine);
                if (hasSlashR) {
                    int posR = string.indexOf('\r', posLastNewLine);
                    if (posR == -1) {
                        posR = posNewLine;
                    }
                    posNewLine = Math.min(posNewLine, posR);
                }
                if (posNewLine < 0) {
                    posNewLine = string.length();
                }
            }
        } finally {
            if (lines.size() > 0) {
                for (LineInfo lineInfo : lines) {
                    size.x = Math.max(lineInfo.width, size.x);
                    size.y += lineInfo.height;
                }
                if ((swtFlags & (SWT.BOTTOM)) != 0) {
                    rectDraw.y = rectDraw.y + rectDraw.height - size.y;
                } else if ((swtFlags & SWT.TOP) == 0) {
                    rectDraw.y = rectDraw.y + (rectDraw.height - size.y) / 2;
                }
                if (!noDraw || listUrlInfo != null) {
                    for (LineInfo lineInfo : lines) {
                        try {
                            drawLine(gc, lineInfo, swtFlags, rectDraw, noDraw);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
            }
            if (!skipClip && !noDraw) {
                gc.setClipping(oldClipping);
            }
        }
        cutoff |= size.y > printArea.height;
        return !cutoff;
    }

    /**
	 * @param hasMoreElements 
	 * @param line
	 *
	 * @since 3.0.0.7
	 */
    private LineInfo processLine(final GC gc, final LineInfo lineInfo, final Rectangle printArea, final boolean fullLinesOnly, boolean hasMoreElements) {
        if (lineInfo.originalLine.length() == 0) {
            lineInfo.lineOutputed = "";
            lineInfo.height = gc.stringExtent(GOOD_STRING).y;
            return lineInfo;
        }
        StringBuffer outputLine = null;
        int excessPos = -1;
        if (images != null || lineInfo.originalLine.length() > MAX_LINE_LEN || gc.stringExtent(lineInfo.originalLine).x > printArea.width) {
            outputLine = new StringBuffer();
            if (DEBUG) {
                System.out.println("Line to process: " + lineInfo.originalLine);
            }
            StringBuffer space = new StringBuffer(1);
            if (!wrap && images == null) {
                if (DEBUG) {
                    System.out.println("No Wrap.. doing all in one line");
                }
                String sProcessedLine = lineInfo.originalLine.length() > MAX_LINE_LEN ? lineInfo.originalLine.substring(0, MAX_LINE_LEN) : lineInfo.originalLine;
                excessPos = processWord(gc, lineInfo.originalLine, sProcessedLine, printArea, lineInfo, outputLine, space);
            } else {
                int posLastWordStart = 0;
                int posWordStart = lineInfo.originalLine.indexOf(' ');
                while (posWordStart == 0) {
                    posWordStart = lineInfo.originalLine.indexOf(' ', posWordStart + 1);
                }
                if (posWordStart < 0) {
                    posWordStart = lineInfo.originalLine.length();
                }
                int curPos = 0;
                while (posWordStart >= 0 && posLastWordStart < lineInfo.originalLine.length()) {
                    String word = lineInfo.originalLine.substring(posLastWordStart, posWordStart);
                    if (word.length() == 0) {
                        excessPos = -1;
                        outputLine.append(' ');
                    }
                    for (int i = 0; i < word.length(); i += MAX_WORD_LEN) {
                        String subWord;
                        int endPos = i + MAX_WORD_LEN;
                        if (endPos > word.length()) {
                            subWord = word.substring(i);
                        } else {
                            subWord = word.substring(i, endPos);
                        }
                        excessPos = processWord(gc, lineInfo.originalLine, subWord, printArea, lineInfo, outputLine, space);
                        if (DEBUG) {
                            System.out.println("  with word [" + subWord + "] len is " + lineInfo.width + "(" + printArea.width + ") w/excess " + excessPos);
                        }
                        if (excessPos >= 0) {
                            excessPos += curPos;
                            break;
                        }
                        if (endPos <= word.length()) {
                            space.setLength(0);
                        }
                        curPos += subWord.length() + 1;
                    }
                    if (excessPos >= 0) {
                        break;
                    }
                    posLastWordStart = posWordStart + 1;
                    posWordStart = lineInfo.originalLine.indexOf(' ', posLastWordStart);
                    if (posWordStart < 0) {
                        posWordStart = lineInfo.originalLine.length();
                    }
                }
            }
        }
        if (!wrap && hasMoreElements && excessPos >= 0) {
            if (outputLine == null) {
                outputLine = new StringBuffer(lineInfo.originalLine);
            }
            int len = outputLine.length();
            if (len > 2) {
                len -= 2;
            }
            outputLine.setLength(len);
            outputLine.append("…");
            cutoff = true;
        }
        lineInfo.excessPos = excessPos;
        lineInfo.lineOutputed = outputLine == null ? lineInfo.originalLine : outputLine.toString();
        return lineInfo;
    }

    /**
	 * @param int Position of part of word that didn't fit
	 *
	 * @since 3.0.0.7
	 */
    private int processWord(final GC gc, final String sLine, String word, final Rectangle printArea, final LineInfo lineInfo, StringBuffer outputLine, final StringBuffer space) {
        if (word.length() == 0) {
            space.append(' ');
            return -1;
        }
        if (images != null && word.length() >= 2 && word.charAt(0) == '%') {
            int imgIdx = word.charAt(1) - '0';
            if (images.length > imgIdx && imgIdx >= 0 && images[imgIdx] != null) {
                Image img = images[imgIdx];
                Rectangle bounds = img.getBounds();
                if (imageScales != null && imageScales.length > imgIdx) {
                    bounds.width = (int) (bounds.width * imageScales[imgIdx]);
                    bounds.height = (int) (bounds.height * imageScales[imgIdx]);
                }
                Point spaceExtent = gc.stringExtent(space.toString());
                int newWidth = lineInfo.width + bounds.width + spaceExtent.x;
                if (newWidth > printArea.width) {
                    if (bounds.width + spaceExtent.x < printArea.width || lineInfo.width > 0) {
                        return 0;
                    }
                }
                if (lineInfo.imageIndexes == null) {
                    lineInfo.imageIndexes = new int[] { imgIdx };
                }
                int targetWidth = lineInfo.width + newWidth;
                lineInfo.width = newWidth;
                lineInfo.height = Math.max(bounds.height, lineInfo.height);
                Point ptWordSize = gc.stringExtent(word.substring(2) + " ");
                if (lineInfo.width + ptWordSize.x > printArea.width) {
                    outputLine.append(space);
                    outputLine.append(word.substring(0, 2));
                    return 2;
                }
                outputLine.append(space);
                space.setLength(0);
                outputLine.append(word.substring(0, 2));
                word = word.substring(2);
            }
        }
        Point ptLineAndWordSize = gc.stringExtent(outputLine + word + " ");
        if (ptLineAndWordSize.x > printArea.width) {
            Point ptWordSize2 = gc.stringExtent(word + " ");
            boolean bWordLargerThanWidth = ptWordSize2.x > printArea.width;
            if (bWordLargerThanWidth && lineInfo.width > 0) {
                return 0;
            }
            int endIndex = word.length();
            long diff = endIndex;
            while (ptLineAndWordSize.x != printArea.width) {
                diff = (diff >> 1) + (diff % 2);
                if (diff <= 0) {
                    diff = 1;
                }
                if (ptLineAndWordSize.x > printArea.width) {
                    endIndex -= diff;
                    if (endIndex < 1) {
                        endIndex = 1;
                    }
                } else {
                    endIndex += diff;
                    if (endIndex > word.length()) {
                        endIndex = word.length();
                    }
                }
                ptLineAndWordSize = gc.stringExtent(outputLine + word.substring(0, endIndex) + " ");
                if (diff <= 1) {
                    break;
                }
            }
            boolean nothingFit = endIndex == 0;
            if (nothingFit) {
                endIndex = 1;
            }
            if (ptLineAndWordSize.x > printArea.width && endIndex > 1) {
                endIndex--;
                ptLineAndWordSize = gc.stringExtent(outputLine + word.substring(0, endIndex) + " ");
            }
            if (DEBUG) {
                System.out.println("excess starts at " + endIndex + " of " + word.length() + ". " + "wrap?" + wrap);
            }
            if (wrap && (printFlags & FLAG_FULLLINESONLY) > 0) {
                int nextLineHeight = gc.stringExtent(GOOD_STRING).y;
                if (iCurrentHeight + ptLineAndWordSize.y + nextLineHeight > printArea.height) {
                    if (DEBUG) {
                        System.out.println("turn off wrap");
                    }
                    wrap = false;
                }
            }
            if (endIndex > 0 && outputLine.length() > 0 && !nothingFit) {
                outputLine.append(space);
            }
            int w = ptLineAndWordSize.x - lineInfo.width;
            if (wrap && !nothingFit && !bWordLargerThanWidth) {
                return 0;
            }
            outputLine.append(word.substring(0, endIndex));
            if (!wrap) {
                int len = outputLine.length();
                if (len == 0) {
                    if (word.length() > 0) {
                        outputLine.append(word.charAt(0));
                    } else if (sLine.length() > 0) {
                        outputLine.append(sLine.charAt(0));
                    }
                } else {
                    if (len > 2) {
                        len -= 2;
                    }
                    outputLine.setLength(len);
                    outputLine.append("…");
                    cutoff = true;
                }
            }
            if (DEBUG) {
                System.out.println("excess " + word.substring(endIndex));
            }
            return endIndex;
        }
        lineInfo.width = ptLineAndWordSize.x;
        if (lineInfo.width > printArea.width) {
            if (space.length() > 0) {
                space.delete(0, space.length());
            }
            if (!wrap) {
                int len = outputLine.length();
                if (len == 0) {
                    if (word.length() > 0) {
                        outputLine.append(word.charAt(0));
                    } else if (sLine.length() > 0) {
                        outputLine.append(sLine.charAt(0));
                    }
                } else {
                    if (len > 2) {
                        len -= 2;
                    }
                    outputLine.setLength(len);
                    outputLine.append("…");
                    cutoff = true;
                }
                return -1;
            } else {
                return 0;
            }
        }
        if (outputLine.length() > 0) {
            outputLine.append(space);
        }
        outputLine.append(word);
        if (space.length() > 0) {
            space.delete(0, space.length());
        }
        space.append(' ');
        return -1;
    }

    /**
	 * printArea is updated to the position of the next row
	 * 
	 * @param gc
	 * @param outputLine
	 * @param swtFlags
	 * @param printArea
	 * @param noDraw 
	 */
    private void drawLine(GC gc, LineInfo lineInfo, int swtFlags, Rectangle printArea, boolean noDraw) {
        String text = lineInfo.lineOutputed;
        if (lineInfo.width == 0 || lineInfo.height == 0) {
            Point gcExtent = gc.stringExtent(text);
            ;
            if (lineInfo.width == 0) {
                lineInfo.width = gcExtent.x;
            }
            if (lineInfo.height == 0) {
                lineInfo.height = gcExtent.y;
            }
        }
        Point drawSize = new Point(lineInfo.width, lineInfo.height);
        int x0;
        if ((swtFlags & SWT.RIGHT) > 0) {
            x0 = printArea.x + printArea.width - drawSize.x;
        } else if ((swtFlags & SWT.CENTER) > 0) {
            x0 = printArea.x + (printArea.width - drawSize.x) / 2;
        } else {
            x0 = printArea.x;
        }
        int y0 = printArea.y;
        int lineInfoRelEndPos = lineInfo.relStartPos + lineInfo.lineOutputed.length();
        int relStartPos = lineInfo.relStartPos;
        int lineStartPos = 0;
        URLInfo urlInfo = null;
        boolean drawURL = hasHitUrl();
        if (drawURL) {
            URLInfo[] hitUrlInfo = getHitUrlInfo();
            int nextHitUrlInfoPos = 0;
            while (drawURL) {
                drawURL = false;
                for (int i = nextHitUrlInfoPos; i < hitUrlInfo.length; i++) {
                    urlInfo = hitUrlInfo[i];
                    drawURL = (urlInfo.relStartPos < lineInfoRelEndPos) && (urlInfo.relStartPos + urlInfo.titleLength > relStartPos) && (relStartPos >= lineInfo.relStartPos) && (relStartPos < lineInfoRelEndPos);
                    if (drawURL) {
                        nextHitUrlInfoPos = i + 1;
                        break;
                    }
                }
                if (!drawURL) {
                    break;
                }
                int i = lineStartPos + urlInfo.relStartPos - relStartPos;
                if (i > 0 && i > lineStartPos && i <= text.length()) {
                    String s = text.substring(lineStartPos, i);
                    x0 += drawText(gc, s, x0, y0, lineInfo.height, null, noDraw, true).x;
                    relStartPos += (i - lineStartPos);
                    lineStartPos += (i - lineStartPos);
                }
                int end = i + urlInfo.titleLength;
                if (i < 0) {
                    i = 0;
                }
                if (end > text.length()) {
                    end = text.length();
                }
                String s = text.substring(i, end);
                relStartPos += (end - i);
                lineStartPos += (end - i);
                Point pt = null;
                Color fgColor = null;
                if (!noDraw) {
                    fgColor = gc.getForeground();
                    if (urlInfo.dropShadowColor != null) {
                        gc.setForeground(urlInfo.dropShadowColor);
                        drawText(gc, s, x0 + 1, y0 + 1, lineInfo.height, null, noDraw, false);
                    }
                    if (urlInfo.urlColor != null) {
                        gc.setForeground(urlInfo.urlColor);
                    } else if (urlColor != null) {
                        gc.setForeground(urlColor);
                    }
                }
                if (urlInfo.hitAreas == null) {
                    urlInfo.hitAreas = new ArrayList(1);
                }
                pt = drawText(gc, s, x0, y0, lineInfo.height, urlInfo.hitAreas, noDraw, true);
                if (!noDraw) {
                    if (urlInfo.urlUnderline) {
                        gc.drawLine(x0, y0 + pt.y - 1, x0 + pt.x - 1, y0 + pt.y - 1);
                    }
                    gc.setForeground(fgColor);
                }
                if (urlInfo.hitAreas == null) {
                    urlInfo.hitAreas = new ArrayList(1);
                }
                x0 += pt.x;
            }
        }
        if (lineStartPos < text.length()) {
            String s = text.substring(lineStartPos);
            if (!noDraw) {
                drawText(gc, s, x0, y0, lineInfo.height, null, noDraw, false);
            }
        }
        printArea.y += drawSize.y;
    }

    private Point drawText(GC gc, String s, int x, int y, int height, List hitAreas, boolean nodraw, boolean calcExtent) {
        Point textExtent;
        if (images != null) {
            int pctPos = s.indexOf('%');
            int lastPos = 0;
            int w = 0;
            int h = 0;
            while (pctPos >= 0) {
                if (pctPos >= 0 && s.length() > pctPos + 1) {
                    int imgIdx = s.charAt(pctPos + 1) - '0';
                    if (imgIdx >= images.length || imgIdx < 0 || images[imgIdx] == null) {
                        String sStart = s.substring(lastPos, pctPos + 1);
                        textExtent = gc.textExtent(sStart);
                        int centerY = y + (height / 2 - textExtent.y / 2);
                        if (hitAreas != null) {
                            hitAreas.add(new Rectangle(x, centerY, textExtent.x, textExtent.y));
                        }
                        if (!nodraw) {
                            gc.drawText(sStart, x, centerY, true);
                        }
                        x += textExtent.x;
                        w += textExtent.x;
                        h = Math.max(h, textExtent.y);
                        lastPos = pctPos + 1;
                        pctPos = s.indexOf('%', pctPos + 1);
                        continue;
                    }
                    String sStart = s.substring(lastPos, pctPos);
                    textExtent = gc.textExtent(sStart);
                    int centerY = y + (height / 2 - textExtent.y / 2);
                    if (!nodraw) {
                        gc.drawText(sStart, x, centerY, true);
                    }
                    x += textExtent.x;
                    w += textExtent.x;
                    h = Math.max(h, textExtent.y);
                    if (hitAreas != null) {
                        hitAreas.add(new Rectangle(x, centerY, textExtent.x, textExtent.y));
                    }
                    Rectangle imgBounds = images[imgIdx].getBounds();
                    float scale = 1.0f;
                    if (imageScales != null && imageScales.length > imgIdx) {
                        scale = imageScales[imgIdx];
                    }
                    int scaleImageWidth = (int) (imgBounds.width * scale);
                    int scaleImageHeight = (int) (imgBounds.height * scale);
                    centerY = y + (height / 2 - scaleImageHeight / 2);
                    if (hitAreas != null) {
                        hitAreas.add(new Rectangle(x, centerY, scaleImageWidth, scaleImageHeight));
                    }
                    if (!nodraw) {
                        gc.drawImage(images[imgIdx], 0, 0, imgBounds.width, imgBounds.height, x, centerY, scaleImageWidth, scaleImageHeight);
                    }
                    x += scaleImageWidth;
                    w += scaleImageWidth;
                    h = Math.max(h, scaleImageHeight);
                }
                lastPos = pctPos + 2;
                pctPos = s.indexOf('%', lastPos);
            }
            if (s.length() >= lastPos) {
                String sEnd = s.substring(lastPos);
                textExtent = gc.textExtent(sEnd);
                int centerY = y + (height / 2 - textExtent.y / 2);
                if (hitAreas != null) {
                    hitAreas.add(new Rectangle(x, centerY, textExtent.x, textExtent.y));
                }
                if (!nodraw) {
                    gc.drawText(sEnd, x, centerY, true);
                }
                x += textExtent.x;
                w += textExtent.x;
                h = Math.max(h, textExtent.y);
            }
            return new Point(w, h);
        }
        if (!nodraw) {
            gc.drawText(s, x, y, true);
        }
        if (!calcExtent && hitAreas == null) {
            return null;
        }
        textExtent = gc.textExtent(s);
        if (hitAreas != null) {
            hitAreas.add(new Rectangle(x, y, textExtent.x, textExtent.y));
        }
        return textExtent;
    }

    public static void main(String[] args) {
        final Display display = Display.getDefault();
        final Shell shell = new Shell(display, SWT.SHELL_TRIM);
        ImageLoader imageLoader = ImageLoader.getInstance();
        final Image[] images = { imageLoader.getImage("azureus32"), imageLoader.getImage("azureus64"), imageLoader.getImage("azureus"), imageLoader.getImage("azureus128") };
        final String text = "Apple <A HREF=\"aa\">Banana</a>, Cow <A HREF=\"ss\">Dug Ergo</a>, Flip Only. test of the string printer averlongwordthisisyesindeed";
        shell.setSize(500, 500);
        GridLayout gridLayout = new GridLayout(2, false);
        shell.setLayout(gridLayout);
        Composite cButtons = new Composite(shell, SWT.NONE);
        GridData gridData = new GridData(SWT.NONE, SWT.FILL, false, true);
        cButtons.setLayoutData(gridData);
        final Canvas cPaint = new Canvas(shell, SWT.DOUBLE_BUFFERED);
        gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        gridData.heightHint = 40;
        cPaint.setLayoutData(gridData);
        cButtons.setLayout(new RowLayout(SWT.VERTICAL));
        Listener l = new Listener() {

            public void handleEvent(Event event) {
                cPaint.redraw();
            }
        };
        final Text txtText = new Text(cButtons, SWT.WRAP | SWT.MULTI | SWT.BORDER);
        txtText.setText(text);
        txtText.addListener(SWT.Modify, l);
        txtText.setLayoutData(new RowData(100, 200));
        txtText.addKeyListener(new KeyListener() {

            public void keyReleased(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
                if (e.keyCode == 'a' && e.stateMask == SWT.CONTROL) {
                    txtText.selectAll();
                }
            }
        });
        final Button btnSkipClip = new Button(cButtons, SWT.CHECK);
        btnSkipClip.setText("Skip Clip");
        btnSkipClip.setSelection(true);
        btnSkipClip.addListener(SWT.Selection, l);
        final Button btnFullOnly = new Button(cButtons, SWT.CHECK);
        btnFullOnly.setText("Full Lines Only");
        btnFullOnly.setSelection(true);
        btnFullOnly.addListener(SWT.Selection, l);
        final Combo cboVAlign = new Combo(cButtons, SWT.READ_ONLY);
        cboVAlign.add("Top");
        cboVAlign.add("Bottom");
        cboVAlign.add("None");
        cboVAlign.addListener(SWT.Selection, l);
        cboVAlign.select(0);
        final Combo cboHAlign = new Combo(cButtons, SWT.READ_ONLY);
        cboHAlign.add("Left");
        cboHAlign.add("Center");
        cboHAlign.add("Right");
        cboHAlign.add("None");
        cboHAlign.addListener(SWT.Selection, l);
        cboHAlign.select(0);
        final Button btnWrap = new Button(cButtons, SWT.CHECK);
        btnWrap.setText("Wrap");
        btnWrap.setSelection(true);
        btnWrap.addListener(SWT.Selection, l);
        final Label lblInfo = new Label(shell, SWT.WRAP);
        lblInfo.setText("Welcome");
        Listener l2 = new Listener() {

            URLInfo lastHitInfo = null;

            public void handleEvent(Event event) {
                GC gc = event.gc;
                boolean ourGC = gc == null;
                if (ourGC) {
                    gc = new GC(cPaint);
                }
                try {
                    GCStringPrinter sp = buildSP(gc);
                    Color colorURL = gc.getDevice().getSystemColor(SWT.COLOR_RED);
                    Color colorURL2 = gc.getDevice().getSystemColor(SWT.COLOR_DARK_MAGENTA);
                    if (event.type == SWT.MouseMove) {
                        Point pt = cPaint.toControl(display.getCursorLocation());
                        URLInfo hitUrl = sp.getHitUrl(pt.x, pt.y);
                        String url1 = hitUrl == null || hitUrl.url == null ? "" : hitUrl.url;
                        String url2 = lastHitInfo == null || lastHitInfo.url == null ? "" : lastHitInfo.url;
                        if (url1.equals(url2)) {
                            return;
                        }
                        cPaint.redraw();
                        lastHitInfo = hitUrl;
                        return;
                    }
                    Rectangle bounds = cPaint.getClientArea();
                    Color colorBox = gc.getDevice().getSystemColor(SWT.COLOR_YELLOW);
                    Color colorText = gc.getDevice().getSystemColor(SWT.COLOR_BLACK);
                    gc.setForeground(colorText);
                    Point pt = cPaint.toControl(display.getCursorLocation());
                    sp.setUrlColor(colorURL);
                    URLInfo hitUrl = sp.getHitUrl(pt.x, pt.y);
                    if (hitUrl != null) {
                        shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
                        hitUrl.urlColor = colorURL2;
                    } else {
                        shell.setCursor(null);
                    }
                    boolean fit = sp.printString();
                    lblInfo.setText(fit ? "fit" : "no fit");
                    bounds.width--;
                    bounds.height--;
                    gc.setForeground(colorBox);
                    gc.drawRectangle(bounds);
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    if (ourGC) {
                        gc.dispose();
                    }
                }
            }

            private GCStringPrinter buildSP(GC gc) {
                Rectangle bounds = cPaint.getClientArea();
                int style = btnWrap.getSelection() ? SWT.WRAP : 0;
                if (cboVAlign.getSelectionIndex() == 0) {
                    style |= SWT.TOP;
                } else if (cboVAlign.getSelectionIndex() == 1) {
                    style |= SWT.BOTTOM;
                }
                if (cboHAlign.getSelectionIndex() == 0) {
                    style |= SWT.LEFT;
                } else if (cboHAlign.getSelectionIndex() == 1) {
                    style |= SWT.CENTER;
                } else if (cboHAlign.getSelectionIndex() == 2) {
                    style |= SWT.RIGHT;
                }
                String text = txtText.getText();
                text = text.replaceAll("\r\n", "\n");
                GCStringPrinter sp = new GCStringPrinter(gc, text, bounds, btnSkipClip.getSelection(), btnFullOnly.getSelection(), style);
                sp.setImages(images);
                sp.calculateMetrics();
                return sp;
            }
        };
        cPaint.addListener(SWT.Paint, l2);
        cPaint.addListener(SWT.MouseMove, l2);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
	 * 
	 */
    public GCStringPrinter(GC gc, String string, Rectangle printArea, boolean skipClip, boolean fullLinesOnly, int swtFlags) {
        this.gc = gc;
        this.string = string;
        this.printArea = printArea;
        this.swtFlags = swtFlags;
        printFlags = 0;
        if (skipClip) {
            printFlags |= FLAG_SKIPCLIP;
        }
        if (fullLinesOnly) {
            printFlags |= FLAG_FULLLINESONLY;
        }
    }

    public GCStringPrinter(GC gc, String string, Rectangle printArea, int printFlags, int swtFlags) {
        this.gc = gc;
        this.string = string;
        this.printArea = printArea;
        this.swtFlags = swtFlags;
        this.printFlags = printFlags;
    }

    public boolean printString() {
        return _printString();
    }

    public boolean printString(int printFlags) {
        int oldPrintFlags = this.printFlags;
        printFlags |= printFlags;
        boolean b = _printString();
        this.printFlags = oldPrintFlags;
        return b;
    }

    public void calculateMetrics() {
        int oldPrintFlags = printFlags;
        printFlags |= FLAG_NODRAW;
        _printString();
        printFlags = oldPrintFlags;
    }

    /**
	 * @param rectangle
	 *
	 * @since 3.0.4.3
	 */
    public void printString(GC gc, Rectangle rectangle, int swtFlags) {
        this.gc = gc;
        int printFlags = this.printFlags;
        if (printArea.width == rectangle.width) {
            printFlags |= FLAG_KEEP_URL_INFO;
        }
        printArea = rectangle;
        this.swtFlags = swtFlags;
        printString(printFlags);
    }

    public Point getCalculatedSize() {
        return size;
    }

    public Color getUrlColor() {
        return urlColor;
    }

    public void setUrlColor(Color urlColor) {
        this.urlColor = urlColor;
    }

    public URLInfo getHitUrl(int x, int y) {
        if (listUrlInfo == null || listUrlInfo.size() == 0) {
            return null;
        }
        for (Iterator iter = listUrlInfo.iterator(); iter.hasNext(); ) {
            URLInfo urlInfo = (URLInfo) iter.next();
            if (urlInfo.hitAreas != null) {
                for (Iterator iter2 = urlInfo.hitAreas.iterator(); iter2.hasNext(); ) {
                    Rectangle r = (Rectangle) iter2.next();
                    if (r.contains(x, y)) {
                        return urlInfo;
                    }
                }
            }
        }
        return null;
    }

    public URLInfo[] getHitUrlInfo() {
        if (listUrlInfo == null) {
            return new URLInfo[0];
        }
        return (URLInfo[]) listUrlInfo.toArray(new URLInfo[0]);
    }

    public boolean hasHitUrl() {
        return listUrlInfo != null && listUrlInfo.size() > 0;
    }

    public boolean isCutoff() {
        return cutoff;
    }

    public void setImages(Image[] images) {
        this.images = images;
    }

    public float[] getImageScales() {
        return imageScales;
    }

    public void setImageScales(float[] imageScales) {
        this.imageScales = imageScales;
    }

    /**
	 * @return
	 *
	 * @since 4.0.0.1
	 */
    public String getText() {
        return string;
    }
}
