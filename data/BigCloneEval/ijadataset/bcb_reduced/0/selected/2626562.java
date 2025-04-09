package com.trapezium.edit;

import com.trapezium.util.ProgressIndicator;
import com.trapezium.parse.TokenEnumerator;
import java.io.*;
import com.trapezium.parse.TokenFactory;
import com.trapezium.parse.LineError;
import com.trapezium.edit.Lines;
import com.trapezium.edit.LineInfo;
import com.trapezium.edit.Hilite;

public class TokenEditor extends TokenEnumerator implements Lines {

    LineError lineError;

    public TokenEditor(InputStream is, String inFile) throws FileNotFoundException, IOException {
        super(is, inFile);
    }

    public TokenEditor(InputStream is, String inFile, ProgressIndicator frl, File fileSource) throws FileNotFoundException, IOException {
        super(is, inFile, frl, fileSource);
    }

    /** used when recreating token enumerator based on old one */
    public TokenEditor() {
        super();
    }

    public TokenEditor(int byteArraySize, int tokenArraySize, int lineArraySize) {
        super(byteArraySize, tokenArraySize, lineArraySize);
    }

    public TokenEditor(TokenEnumerator source) {
        this(source.getByteArraySize(), source.getTokenArraySize(), source.getLineArraySize());
    }

    public void setLineError(LineError lineError) {
        this.lineError = lineError;
    }

    /** part of Lines interface, get the line number of the line with
     *  the next error after the "lineNo" parameter.
     *
     *  @param lineNo line number after which search for next error begins
     *
     *  @return line number of the next line with the error, or -1 if none
     */
    public int getNextError(int lineNo) {
        if (lineError != null) {
            return (lineError.getNextError(lineNo));
        }
        return (-1);
    }

    public int getPrevError(int lineNo) {
        if (lineError != null) {
            return (lineError.getPrevError(lineNo));
        }
        return (-1);
    }

    public int getErrorCount(int lineNo) {
        if (lineError == null) {
            return (0);
        } else {
            return (lineError.getErrorCount(lineNo, true));
        }
    }

    /** Get an error string associated with a line.
     *
     *  @param lineNo the line number to check for an error association
     *  @param errorStringNo the offset of a particular error, used for
     *     case where line is associated with several errors.
     */
    public String getErrorViewerString(int lineNo, int errorStringNo) {
        if (lineError == null) {
            return (null);
        } else {
            return (lineError.getErrorViewerString(lineNo, errorStringNo));
        }
    }

    public String getErrorStatusString(int lineNo, int errorStringNo) {
        if (lineError == null) {
            return (null);
        } else {
            return (lineError.getErrorStatusString(lineNo, errorStringNo));
        }
    }

    public void setLine(String line, int lineNumber) {
        int scanner = lineIdx[lineNumber];
        int len = line.length();
        expandSize(lineNumber, len * 2);
        for (int i = 0; i < len; i++) {
            fileData[scanner++] = (byte) line.charAt(i);
        }
        fileData[scanner] = 0;
        dirtyFileData = true;
    }

    /** Split a line at a particular offset */
    public void splitLine(int lineNumber, int offset) {
        doInsertChar(lineNumber, offset, (char) 0);
        insertLine(lineNumber + 1, lineIdx[lineNumber] + offset + 1);
    }

    public void joinLine(int lineNumber, boolean addSpace) {
        if (lineNumber == (numberLines - 1)) {
            return;
        }
        int nextLine = lineNumber + 1;
        if (nextLine >= numberLines) {
            return;
        }
        int scanner = lineIdx[lineNumber];
        while (fileData[scanner] != 0) {
            scanner++;
        }
        int actualLineSize = scanner - lineIdx[lineNumber];
        if (lineIdx[nextLine] == (lineIdx[lineNumber] + actualLineSize + 1)) {
            fileData[lineIdx[nextLine] - 1] = (byte) ' ';
        } else {
            int nextLineSize = 0;
            scanner = lineIdx[nextLine];
            while (fileData[scanner] != 0) {
                scanner++;
            }
            nextLineSize = scanner - lineIdx[nextLine] + 1;
            expandSize(lineNumber, nextLineSize);
            scanner = lineIdx[nextLine];
            int scannerDest = lineIdx[lineNumber] + actualLineSize;
            if (addSpace) {
                fileData[scannerDest++] = (byte) ' ';
            }
            while (fileData[scanner] != 0) {
                fileData[scannerDest++] = fileData[scanner++];
            }
            fileData[scannerDest] = 0;
        }
        for (int i = lineNumber + 1; i < numberLines; i++) {
            lineIdx[i] = lineIdx[i + 1];
        }
        numberLines--;
    }

    public void insertString(String s, int insertOffset) {
        System.out.println("InsertString '" + s);
        addLineCapacity();
        int nbytes = s.length() + 1;
        ensureByteCapacity(nbytes);
        for (int i = numberLines - 1; i >= insertOffset; i--) {
            lineIdx[i + 1] = lineIdx[i];
        }
        numberLines++;
        int len = nbytes - 1;
        lineIdx[insertOffset] = fileDataIdx;
        for (int i = 0; i < len; i++) {
            fileData[fileDataIdx++] = (byte) s.charAt(i);
        }
        fileData[fileDataIdx++] = 0;
        incLineNumbers(insertOffset);
    }

    public void deleteCharacters(int len, int lineNumber, int lineOffset) {
        for (int i = 0; i < len; i++) {
            if (getLineChar(lineNumber, lineOffset) == 0) {
                if (lineOffset == 0) {
                    deleteLine(lineNumber);
                }
            } else {
                doDeleteChar(lineNumber, lineOffset);
            }
        }
    }

    public void deleteLines(String s, int lineNumber, int lineOffset) {
        int len = s.length();
        int numberLines = 1;
        int firstLineEnd = -1;
        for (int i = 0; i < len; i++) {
            int x = s.charAt(i);
            if (x == 10) {
                if (firstLineEnd == -1) {
                    firstLineEnd = i;
                }
                numberLines++;
            }
        }
        if (firstLineEnd == -1) {
            firstLineEnd = len;
        }
        deleteCharacters(firstLineEnd, lineNumber, lineOffset);
        int scanner = firstLineEnd + 1;
        for (int i = 1; i < numberLines; i++) {
            joinLine(lineNumber, false);
            while (true) {
                if (scanner >= len) {
                    break;
                }
                char x = s.charAt(scanner);
                scanner++;
                if (x == 10) {
                    break;
                }
                doDeleteChar(lineNumber, firstLineEnd);
            }
        }
    }

    /** Lines interface */
    public void insertElementAt(String s, int i) {
        insertLines(s, i, 0);
    }

    /** Lines interface */
    public String getLine(int offset) {
        return (getLineAt(offset, 100));
    }

    /** Lines interface, split line at a particular offset */
    public void split_line(int tokenLine, int visualTokenLineOffset) {
        System.out.println("split line " + tokenLine + " at " + visualTokenLineOffset);
        int lineStart = lineIdx[tokenLine];
        int byteTokenLineOffset = visualTokenLineOffset;
        if ((fileData[lineStart] & 128) != 0) {
            int rleCount = fileData[lineStart] & 127;
            byteTokenLineOffset -= (rleCount - 1);
        }
        split_line(tokenLine, byteTokenLineOffset, false);
        int tokenOffset = numberTokens;
        for (int i = 0; i < numberTokens; i++) {
            if (lineNumberArray[i] > tokenLine) {
                tokenOffset = i;
                break;
            }
        }
        for (int i = tokenOffset; i < numberTokens; i++) {
            lineNumberArray[i] += 1;
        }
    }

    /** Insert one or more lines into TokenEditor */
    public void insertLines(String s, int lineNumber, int lineOffset) {
        int len = s.length();
        int nLines = 1;
        int firstLineEnd = -1;
        for (int i = 0; i < len; i++) {
            int x = s.charAt(i);
            if (x == 10) {
                if (firstLineEnd == -1) {
                    firstLineEnd = i;
                }
                nLines++;
            }
        }
        if (firstLineEnd == -1) {
            firstLineEnd = len;
        }
        String firstLine = s;
        if (nLines > 1) {
            firstLine = s.substring(0, firstLineEnd);
        }
        for (int i = 0; i < firstLineEnd; i++) {
            doInsertChar(lineNumber, i + lineOffset, s.charAt(i));
        }
        if (lineNumber == (numberLines - 1)) {
            fileDataIdx += firstLineEnd + 1;
        }
        int scanner = firstLineEnd + 1;
        int lineSize = firstLineEnd + lineOffset;
        for (int i = 1; i <= nLines; i++) {
            splitLine(lineNumber + i - 1, lineSize);
            lineSize = 0;
            while (true) {
                if (scanner >= len) {
                    break;
                }
                char x = s.charAt(scanner);
                scanner++;
                if (x == 10) {
                    break;
                }
                doInsertChar(lineNumber + i, lineSize, x);
                lineSize++;
            }
            if ((lineNumber + i) == (numberLines - 1)) {
                fileDataIdx += lineSize;
            }
        }
    }

    public void doDeleteChar(int lineNumber, int offset) {
        dirtyFileData = true;
        int lineByteOffset = lineIdx[lineNumber];
        int fileDataLocation = lineByteOffset + offset;
        if ((fileData[lineByteOffset] & 128) != 0) {
            int rleSpace = fileData[lineByteOffset] & 127;
            if (offset < rleSpace) {
                rleSpace--;
                if (rleSpace == 0) {
                    fileDataLocation = 0;
                } else if (rleSpace == 1) {
                    fileData[lineByteOffset] = (byte) ' ';
                    return;
                } else {
                    fileData[lineByteOffset] = (byte) (rleSpace | 128);
                    return;
                }
            } else {
                fileDataLocation = fileDataLocation - rleSpace + 1;
            }
        }
        while ((fileData[fileDataLocation] != 0) && (fileDataLocation < fileDataIdx)) {
            fileData[fileDataLocation] = fileData[fileDataLocation + 1];
            fileDataLocation++;
        }
    }

    /** remove rle encoding from start of line, space already available */
    void unrleLine(int lineNumber) {
        int lineByteOffset = lineIdx[lineNumber];
        if ((fileData[lineByteOffset] & 128) != 0) {
            int rleSpace = (fileData[lineByteOffset] & 127) - 1;
            int lastByteOffset = lineByteOffset;
            while (fileData[lastByteOffset] != 0) {
                lastByteOffset++;
            }
            while (lastByteOffset >= lineByteOffset) {
                fileData[lastByteOffset + rleSpace] = fileData[lastByteOffset];
                lastByteOffset--;
            }
            rleSpace++;
            for (int i = 0; i < rleSpace; i++) {
                fileData[lineByteOffset + i] = (byte) ' ';
            }
            rleSpace--;
            for (int i = 0; i < numberTokens; i++) {
                if (getLineNumber(i) == lineNumber) {
                    lineOffsetArray[i] += rleSpace;
                }
            }
        }
    }

    public void doInsertChar(int lineNumber, int offset, char cval) {
        int expansionSize = 20;
        int lineByteOffset = lineIdx[lineNumber];
        if ((fileData[lineByteOffset] & 128) != 0) {
            int rleSpace = fileData[lineByteOffset] & 127;
            expansionSize += rleSpace;
        }
        expandSize(lineNumber, expansionSize);
        unrleLine(lineNumber);
        int endScanner = lineIdx[lineNumber] + offset;
        int startScanner = endScanner;
        while (fileData[startScanner] != 0) {
            startScanner++;
        }
        if (fileData[startScanner] == 0) {
            startScanner++;
            while (startScanner > endScanner) {
                fileData[startScanner] = fileData[startScanner - 1];
                startScanner--;
            }
            fileData[endScanner] = (byte) cval;
        }
        dirtyFileData = true;
    }

    /** Lines interface */
    public void removeElementAt(int lineNumber) {
        deleteLine(lineNumber);
    }

    /** Lines interface, dump to a file */
    public void dumpLines(String fileName) {
        detailDump(fileName);
    }

    /** Lines interface */
    public void setString(String s, int lineNumber) {
        clearLine(lineNumber);
        setLine(s, lineNumber);
    }

    /** Lines interface */
    public int size() {
        return (getNumberLines());
    }

    boolean noLineInfo = false;

    public void disableLineInfo() {
        noLineInfo = true;
    }

    /** Lines interface */
    public LineInfo getLineInfo(int lineNumber) {
        if (noLineInfo) {
            return (null);
        }
        LineInfo li = new LineInfo(getLineAt(lineNumber));
        li.inComment = false;
        li.inLiteral = false;
        li.keyCt = 0;
        int firstToken = -1;
        int lastToken = -1;
        int leftscan = 0;
        int rightscan = numberTokens - 1;
        while ((firstToken == -1) && (lastToken == -1)) {
            int scan = (leftscan + rightscan) / 2;
            int lno = getLineNumber(scan);
            if (lno > lineNumber) {
                if (rightscan == scan) {
                    return (li);
                }
                rightscan = scan;
            } else if (lno < lineNumber) {
                if (leftscan == scan) {
                    return (li);
                }
                leftscan = scan;
            } else {
                while (lno == lineNumber) {
                    firstToken = scan;
                    if (scan == 0) {
                        break;
                    }
                    scan--;
                    lno = getLineNumber(scan);
                }
                scan = firstToken;
                lno = lineNumber;
                while (lno == lineNumber) {
                    lastToken = scan;
                    scan++;
                    if (scan == numberTokens) {
                        break;
                    }
                    lno = getLineNumber(scan);
                }
            }
        }
        li.inComment = false;
        li.inLiteral = false;
        li.keyCt = lastToken - firstToken + 1;
        if (firstToken == -1) {
            li.keyCt = 0;
        } else {
            li.keyStarts = new short[li.keyCt];
            li.keyEnds = new short[li.keyCt];
            li.keyTypes = new byte[li.keyCt];
            if (firstToken != -1) {
                for (int i = firstToken; i <= lastToken; i++) {
                    li.keyStarts[i - firstToken] = (short) getLineOffset(i);
                    int size = getSize(i);
                    li.keyEnds[i - firstToken] = (short) (getSize(i) + li.keyStarts[i - firstToken]);
                    li.keyTypes[i - firstToken] = getEditType(i);
                }
            }
        }
        return (li);
    }

    public byte getEditType(int tokenOffset) {
        int type = getType(tokenOffset);
        if ((type == QuotedString) || (type == QuotedStringContinuation)) {
            return (Hilite.QUOTE);
        } else if (type == Keyword1Token) {
            return (Hilite.KEYWORD);
        } else if (type == Keyword2Token) {
            return (Hilite.KEYWORD2);
        } else if (type == CommentToken) {
            return (Hilite.COMMENT);
        } else {
            return (Hilite.PLAIN);
        }
    }

    /** Delete a particular line */
    public void deleteLine(int lineNumber) {
        for (int i = lineNumber; i < (numberLines - 1); i++) {
            lineIdx[i] = lineIdx[i + 1];
        }
        numberLines--;
        dirtyFileData = true;
    }

    /** Make an existing line empty */
    public void clearLine(int lineNumber) {
        int fileDataOffset = lineIdx[lineNumber];
        fileData[fileDataOffset] = 0;
    }
}
