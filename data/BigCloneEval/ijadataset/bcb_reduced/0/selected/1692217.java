package org.gjt.sp.jedit.buffer;

import org.gjt.sp.jedit.syntax.*;
import org.gjt.sp.jedit.Debug;
import org.gjt.sp.util.IntegerArray;
import org.gjt.sp.util.Log;

/**
 * A class internal to jEdit's document model. You should not use it
 * directly. To improve performance, none of the methods in this class
 * check for out of bounds access, nor are they thread-safe. The
 * <code>Buffer</code> class, through which these methods must be
 * called through, implements such protection.
 *
 * @author Slava Pestov
 * @version $Id: LineManager.java 12504 2008-04-22 23:12:43Z ezust $
 * @since jEdit 4.2pre3
 */
public class LineManager {

    public LineManager() {
        endOffsets = new int[1];
        endOffsets[0] = 1;
        foldLevels = new short[1];
        lineContext = new TokenMarker.LineContext[1];
        lineCount = 1;
    }

    public final int getLineCount() {
        return lineCount;
    }

    public int getLineOfOffset(int offset) {
        int start = 0;
        int end = lineCount - 1;
        for (; ; ) {
            switch(end - start) {
                case 0:
                    if (getLineEndOffset(start) <= offset) return start + 1; else return start;
                case 1:
                    if (getLineEndOffset(start) <= offset) {
                        if (getLineEndOffset(end) <= offset) return end + 1; else return end;
                    } else return start;
                default:
                    int pivot = (end + start) / 2;
                    int value = getLineEndOffset(pivot);
                    if (value == offset) return pivot + 1; else if (value < offset) start = pivot + 1; else end = pivot - 1;
                    break;
            }
        }
    }

    public final int getLineEndOffset(int line) {
        if (gapLine != -1 && line >= gapLine) return endOffsets[line] + gapWidth; else return endOffsets[line];
    }

    public final int getFoldLevel(int line) {
        return foldLevels[line];
    }

    public final void setFoldLevel(int line, int level) {
        if (level > 0xffff) {
            level = 0xffff;
        }
        foldLevels[line] = (short) level;
    }

    public void setFirstInvalidFoldLevel(int firstInvalidFoldLevel) {
        this.firstInvalidFoldLevel = firstInvalidFoldLevel;
    }

    public int getFirstInvalidFoldLevel() {
        return firstInvalidFoldLevel;
    }

    public final TokenMarker.LineContext getLineContext(int line) {
        return lineContext[line];
    }

    public final void setLineContext(int line, TokenMarker.LineContext context) {
        lineContext[line] = context;
    }

    public void setFirstInvalidLineContext(int firstInvalidLineContext) {
        this.firstInvalidLineContext = firstInvalidLineContext;
    }

    public int getFirstInvalidLineContext() {
        return firstInvalidLineContext;
    }

    public void _contentInserted(IntegerArray endOffsets) {
        gapLine = -1;
        gapWidth = 0;
        firstInvalidLineContext = firstInvalidFoldLevel = 0;
        lineCount = endOffsets.getSize();
        this.endOffsets = endOffsets.getArray();
        foldLevels = new short[lineCount];
        lineContext = new TokenMarker.LineContext[lineCount];
    }

    public void contentInserted(int startLine, int offset, int numLines, int length, IntegerArray endOffsets) {
        int endLine = startLine + numLines;
        if (numLines > 0) {
            lineCount += numLines;
            if (this.endOffsets.length <= lineCount) {
                int[] endOffsetsN = new int[(lineCount + 1) * 2];
                System.arraycopy(this.endOffsets, 0, endOffsetsN, 0, this.endOffsets.length);
                this.endOffsets = endOffsetsN;
            }
            if (foldLevels.length <= lineCount) {
                short[] foldLevelsN = new short[(lineCount + 1) * 2];
                System.arraycopy(foldLevels, 0, foldLevelsN, 0, foldLevels.length);
                foldLevels = foldLevelsN;
            }
            if (lineContext.length <= lineCount) {
                TokenMarker.LineContext[] lineContextN = new TokenMarker.LineContext[(lineCount + 1) * 2];
                System.arraycopy(lineContext, 0, lineContextN, 0, lineContext.length);
                lineContext = lineContextN;
            }
            System.arraycopy(this.endOffsets, startLine, this.endOffsets, endLine, lineCount - endLine);
            System.arraycopy(foldLevels, startLine, foldLevels, endLine, lineCount - endLine);
            System.arraycopy(lineContext, startLine, lineContext, endLine, lineCount - endLine);
            if (startLine <= gapLine) gapLine += numLines; else if (gapLine != -1) offset -= gapWidth;
            if (startLine < firstInvalidLineContext) firstInvalidLineContext += numLines;
            for (int i = 0; i < numLines; i++) {
                this.endOffsets[startLine + i] = (offset + endOffsets.get(i));
                foldLevels[startLine + i] = 0;
            }
        }
        if (firstInvalidFoldLevel == -1 || firstInvalidFoldLevel > startLine) firstInvalidFoldLevel = startLine;
        moveGap(endLine, length, "contentInserted");
    }

    public void contentRemoved(int startLine, int offset, int numLines, int length) {
        int endLine = startLine + numLines;
        if (numLines > 0) {
            if (startLine + numLines < gapLine) gapLine -= numLines; else if (startLine < gapLine) gapLine = startLine;
            if (startLine + numLines < firstInvalidLineContext) firstInvalidLineContext -= numLines; else if (startLine < firstInvalidLineContext) firstInvalidLineContext = startLine - 1;
            lineCount -= numLines;
            System.arraycopy(endOffsets, endLine, endOffsets, startLine, lineCount - startLine);
            System.arraycopy(foldLevels, endLine, foldLevels, startLine, lineCount - startLine);
            System.arraycopy(lineContext, endLine, lineContext, startLine, lineCount - startLine);
        }
        if (firstInvalidFoldLevel == -1 || firstInvalidFoldLevel > startLine) firstInvalidFoldLevel = startLine;
        moveGap(startLine, -length, "contentRemoved");
    }

    private int[] endOffsets;

    private short[] foldLevels;

    private TokenMarker.LineContext[] lineContext;

    private int lineCount;

    /**
	 * If -1, then there is no gap.
	 * Otherwise, all lines from this line onwards need to have gapWidth
	 * added to their end offsets.
	 */
    private int gapLine;

    private int gapWidth;

    /**
	 * If -1, all contexts are valid. Otherwise, all lines after this have
	 * an invalid context.
	 */
    private int firstInvalidLineContext;

    /**
	 * If -1, all fold levels are valid. Otherwise, all lines after this
	 * have an invalid fold level.
	 */
    private int firstInvalidFoldLevel;

    private final void setLineEndOffset(int line, int end) {
        endOffsets[line] = end;
    }

    private final void moveGap(int newGapLine, int newGapWidth, String method) {
        if (gapLine == -1) gapWidth = newGapWidth; else if (newGapLine == -1) {
            if (gapWidth != 0) {
                if (Debug.OFFSET_DEBUG && gapLine != lineCount) Log.log(Log.DEBUG, this, method + ": update from " + gapLine + " to " + lineCount + " width " + gapWidth);
                for (int i = gapLine; i < lineCount; i++) setLineEndOffset(i, getLineEndOffset(i));
            }
            gapWidth = newGapWidth;
        } else if (newGapLine < gapLine) {
            if (gapWidth != 0) {
                if (Debug.OFFSET_DEBUG && newGapLine != gapLine) Log.log(Log.DEBUG, this, method + ": update from " + newGapLine + " to " + gapLine + " width " + gapWidth);
                for (int i = newGapLine; i < gapLine; i++) setLineEndOffset(i, getLineEndOffset(i) - gapWidth);
            }
            gapWidth += newGapWidth;
        } else {
            if (gapWidth != 0) {
                if (Debug.OFFSET_DEBUG && gapLine != newGapLine) Log.log(Log.DEBUG, this, method + ": update from " + gapLine + " to " + newGapLine + " width " + gapWidth);
                for (int i = gapLine; i < newGapLine; i++) setLineEndOffset(i, getLineEndOffset(i));
            }
            gapWidth += newGapWidth;
        }
        if (newGapLine == lineCount) gapLine = -1; else gapLine = newGapLine;
    }
}
