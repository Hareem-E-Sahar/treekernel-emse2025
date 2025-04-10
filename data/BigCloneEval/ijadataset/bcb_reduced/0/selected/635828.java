package org.gjt.sp.jedit.buffer;

import javax.swing.text.*;
import org.gjt.sp.jedit.syntax.*;
import org.gjt.sp.jedit.Buffer;
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
 * @version $Id: OffsetManager.java,v 1.26 2002/03/17 03:05:24 spestov Exp $
 * @since jEdit 4.0pre1
 */
public class OffsetManager {

    public OffsetManager(Buffer buffer) {
        this.buffer = buffer;
        lineInfo = new long[1];
        lineInfo[0] = 1L | (0xffL << VISIBLE_SHIFT);
        lineContext = new TokenMarker.LineContext[1];
        lineCount = 1;
        positions = new PosBottomHalf[100];
        virtualLineCounts = new int[8];
        for (int i = 0; i < 8; i++) virtualLineCounts[i] = 1;
    }

    public final int getLineCount() {
        return lineCount;
    }

    public final int getVirtualLineCount(int index) {
        return virtualLineCounts[index];
    }

    public final void setVirtualLineCount(int index, int lineCount) {
        virtualLineCounts[index] = lineCount;
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
        return (int) (lineInfo[line] & END_MASK);
    }

    public final boolean isFoldLevelValid(int line) {
        return (lineInfo[line] & FOLD_LEVEL_VALID_MASK) != 0;
    }

    public final int getFoldLevel(int line) {
        return (int) ((lineInfo[line] & FOLD_LEVEL_MASK) >> FOLD_LEVEL_SHIFT);
    }

    public final void setFoldLevel(int line, int level) {
        lineInfo[line] = ((lineInfo[line] & ~FOLD_LEVEL_MASK) | ((long) level << FOLD_LEVEL_SHIFT) | FOLD_LEVEL_VALID_MASK);
    }

    public final boolean isLineVisible(int line, int index) {
        long mask = 1L << (index + VISIBLE_SHIFT);
        return (lineInfo[line] & mask) != 0;
    }

    public final void setLineVisible(int line, int index, boolean visible) {
        long mask = 1L << (index + VISIBLE_SHIFT);
        if (visible) lineInfo[line] = (lineInfo[line] | mask); else lineInfo[line] = (lineInfo[line] & ~mask);
    }

    public final int getScreenLineCount(int line) {
        return (int) ((lineInfo[line] & SCREEN_LINES_MASK) >> SCREEN_LINES_SHIFT);
    }

    public final void setScreenLineCount(int line, int count) {
        lineInfo[line] = ((lineInfo[line] & ~SCREEN_LINES_MASK) | ((long) count << SCREEN_LINES_SHIFT));
    }

    public final boolean isLineContextValid(int line) {
        return (lineInfo[line] & CONTEXT_VALID_MASK) != 0;
    }

    public final TokenMarker.LineContext getLineContext(int line) {
        return lineContext[line];
    }

    public final void setLineContext(int line, TokenMarker.LineContext context) {
        lineContext[line] = context;
        lineInfo[line] |= CONTEXT_VALID_MASK;
    }

    public synchronized Position createPosition(int offset) {
        PosBottomHalf bh = null;
        for (int i = 0; i < positionCount; i++) {
            PosBottomHalf _bh = positions[i];
            if (_bh.offset == offset) {
                bh = _bh;
                break;
            } else if (_bh.offset > offset) {
                bh = new PosBottomHalf(offset);
                growPositionArray();
                System.arraycopy(positions, i, positions, i + 1, positionCount - i);
                positionCount++;
                positions[i] = bh;
                break;
            }
        }
        if (bh == null) {
            bh = new PosBottomHalf(offset);
            growPositionArray();
            positions[positionCount++] = bh;
        }
        return new PosTopHalf(bh);
    }

    /**
	 * Like <code>FoldVisibilityManager.expandFolds()</code>, but does
	 * it for all fold visibility managers viewing this buffer. Should
	 * only be called after loading.
	 */
    public void expandFolds(int foldLevel) {
        int newVirtualLineCount = 0;
        if (foldLevel == 0) {
            newVirtualLineCount = lineCount;
        } else {
            foldLevel = (foldLevel - 1) * buffer.getIndentSize() + 1;
            boolean seenVisibleLine = false;
            for (int i = 0; i < lineCount; i++) {
                if (!seenVisibleLine || buffer.getFoldLevel(i) < foldLevel) {
                    seenVisibleLine = true;
                    lineInfo[i] |= VISIBLE_MASK;
                    newVirtualLineCount++;
                } else lineInfo[i] &= ~VISIBLE_MASK;
            }
        }
        for (int i = 0; i < virtualLineCounts.length; i++) {
            virtualLineCounts[i] = newVirtualLineCount;
        }
    }

    public void contentInserted(int startLine, int offset, int numLines, int length, IntegerArray endOffsets) {
        int endLine = startLine + numLines;
        if (numLines > 0) {
            lineCount += numLines;
            if (lineInfo.length <= lineCount) {
                long[] lineInfoN = new long[(lineCount + 1) * 2];
                System.arraycopy(lineInfo, 0, lineInfoN, 0, lineInfo.length);
                lineInfo = lineInfoN;
                TokenMarker.LineContext[] lineContextN = new TokenMarker.LineContext[(lineCount + 1) * 2];
                System.arraycopy(lineContext, 0, lineContextN, 0, lineContext.length);
                lineContext = lineContextN;
            }
            System.arraycopy(lineInfo, startLine, lineInfo, endLine, lineCount - endLine);
            System.arraycopy(lineContext, startLine, lineContext, endLine, lineCount - endLine);
            int foldLevel = buffer.getFoldLevel(startLine);
            long visible = (0xffL << VISIBLE_SHIFT);
            if (startLine != 0) {
                for (int i = startLine; i > 0; i--) {
                    if (buffer.getFoldLevel(i) <= foldLevel) {
                        visible = (lineInfo[i] & VISIBLE_MASK);
                        break;
                    }
                }
            }
            for (int i = 0; i < numLines; i++) {
                lineInfo[startLine + i] = (((offset + endOffsets.get(i) + 1) & ~(FOLD_LEVEL_VALID_MASK | CONTEXT_VALID_MASK)) | visible);
            }
            if ((visible & (1L << (VISIBLE_SHIFT + 0))) != 0) virtualLineCounts[0] += numLines;
            if ((visible & (1L << (VISIBLE_SHIFT + 1))) != 0) virtualLineCounts[1] += numLines;
            if ((visible & (1L << (VISIBLE_SHIFT + 2))) != 0) virtualLineCounts[2] += numLines;
            if ((visible & (1L << (VISIBLE_SHIFT + 3))) != 0) virtualLineCounts[3] += numLines;
            if ((visible & (1L << (VISIBLE_SHIFT + 4))) != 0) virtualLineCounts[4] += numLines;
            if ((visible & (1L << (VISIBLE_SHIFT + 5))) != 0) virtualLineCounts[5] += numLines;
            if ((visible & (1L << (VISIBLE_SHIFT + 6))) != 0) virtualLineCounts[6] += numLines;
            if ((visible & (1L << (VISIBLE_SHIFT + 7))) != 0) virtualLineCounts[7] += numLines;
        }
        for (int i = endLine; i < lineCount; i++) {
            setLineEndOffset(i, getLineEndOffset(i) + length);
        }
        updatePositionsForInsert(offset, length);
    }

    public void contentRemoved(int startLine, int offset, int numLines, int length) {
        for (int i = 0; i < numLines; i++) {
            long info = lineInfo[startLine + i];
            if ((info & (1L << (VISIBLE_SHIFT + 0))) != 0) virtualLineCounts[0]--;
            if ((info & (1L << (VISIBLE_SHIFT + 1))) != 0) virtualLineCounts[1]--;
            if ((info & (1L << (VISIBLE_SHIFT + 2))) != 0) virtualLineCounts[2]--;
            if ((info & (1L << (VISIBLE_SHIFT + 3))) != 0) virtualLineCounts[3]--;
            if ((info & (1L << (VISIBLE_SHIFT + 4))) != 0) virtualLineCounts[4]--;
            if ((info & (1L << (VISIBLE_SHIFT + 5))) != 0) virtualLineCounts[5]--;
            if ((info & (1L << (VISIBLE_SHIFT + 6))) != 0) virtualLineCounts[6]--;
            if ((info & (1L << (VISIBLE_SHIFT + 7))) != 0) virtualLineCounts[7]--;
        }
        if (numLines > 0) {
            lineCount -= numLines;
            System.arraycopy(lineInfo, startLine + numLines, lineInfo, startLine, lineCount - startLine);
            System.arraycopy(lineContext, startLine + numLines, lineContext, startLine, lineCount - startLine);
        }
        for (int i = startLine; i < lineCount; i++) {
            setLineEndOffset(i, getLineEndOffset(i) - length);
        }
        updatePositionsForRemove(offset, length);
    }

    public void lineInfoChangedFrom(int startLine) {
        for (int i = startLine; i < lineCount; i++) {
            lineInfo[i] &= ~(FOLD_LEVEL_VALID_MASK | CONTEXT_VALID_MASK);
            lineContext[i] = null;
        }
    }

    private static final long END_MASK = 0x00000000ffffffffL;

    private static final long FOLD_LEVEL_MASK = 0x0000ffff00000000L;

    private static final int FOLD_LEVEL_SHIFT = 32;

    private static final long VISIBLE_MASK = 0x00ff000000000000L;

    private static final int VISIBLE_SHIFT = 48;

    private static final long FOLD_LEVEL_VALID_MASK = (1L << 56);

    private static final long CONTEXT_VALID_MASK = (1L << 57);

    private static final long SCREEN_LINES_MASK = 0x7c00000000000000L;

    private static final long SCREEN_LINES_SHIFT = 58;

    private Buffer buffer;

    private long[] lineInfo;

    private TokenMarker.LineContext[] lineContext;

    private int lineCount;

    private PosBottomHalf[] positions;

    private int positionCount;

    private int[] virtualLineCounts;

    private final void setLineEndOffset(int line, int end) {
        lineInfo[line] = ((lineInfo[line] & ~(END_MASK | FOLD_LEVEL_VALID_MASK | CONTEXT_VALID_MASK)) | end);
    }

    private void growPositionArray() {
        if (positions.length < positionCount + 1) {
            PosBottomHalf[] newPositions = new PosBottomHalf[(positionCount + 1) * 2];
            System.arraycopy(positions, 0, newPositions, 0, positionCount);
            positions = newPositions;
        }
    }

    private synchronized void removePosition(PosBottomHalf bh) {
        int index = -1;
        for (int i = 0; i < positionCount; i++) {
            if (positions[i] == bh) {
                index = i;
                break;
            }
        }
        System.arraycopy(positions, index + 1, positions, index, positionCount - index - 1);
        positions[--positionCount] = null;
    }

    private void updatePositionsForInsert(int offset, int length) {
        if (positionCount == 0) return;
        int start = getPositionAtOffset(offset);
        for (int i = start; i < positionCount; i++) {
            PosBottomHalf bh = positions[i];
            if (bh.offset < offset) Log.log(Log.ERROR, this, "Screwed up: " + bh.offset); else bh.offset += length;
        }
    }

    private void updatePositionsForRemove(int offset, int length) {
        if (positionCount == 0) return;
        int start = getPositionAtOffset(offset);
        for (int i = start; i < positionCount; i++) {
            PosBottomHalf bh = positions[i];
            if (bh.offset < offset) Log.log(Log.ERROR, this, "Screwed up: " + bh.offset); else if (bh.offset < offset + length) bh.offset = offset; else bh.offset -= length;
        }
    }

    private int getPositionAtOffset(int offset) {
        int start = 0;
        int end = positionCount - 1;
        PosBottomHalf bh;
        loop: for (; ; ) {
            switch(end - start) {
                case 0:
                    bh = positions[start];
                    if (bh.offset < offset) start++;
                    break loop;
                case 1:
                    bh = positions[end];
                    if (bh.offset < offset) {
                        start = end + 1;
                    } else {
                        bh = positions[start];
                        if (bh.offset < offset) {
                            start++;
                        }
                    }
                    break loop;
                default:
                    int pivot = (start + end) / 2;
                    bh = positions[pivot];
                    if (bh.offset > offset) end = pivot - 1; else start = pivot + 1;
                    break;
            }
        }
        return start;
    }

    static class PosTopHalf implements Position {

        PosBottomHalf bh;

        PosTopHalf(PosBottomHalf bh) {
            this.bh = bh;
            bh.ref();
        }

        public int getOffset() {
            return bh.offset;
        }

        public void finalize() {
            bh.unref();
        }
    }

    class PosBottomHalf {

        int offset;

        int ref;

        PosBottomHalf(int offset) {
            this.offset = offset;
        }

        void ref() {
            ref++;
        }

        void unref() {
            if (--ref == 0) removePosition(this);
        }
    }
}
