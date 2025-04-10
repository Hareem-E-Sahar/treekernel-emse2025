package com.ibm.icu.dev.tool.rbbi;

import com.ibm.icu.util.CompactByteArray;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.impl.Utility;
import java.io.*;
import java.util.Vector;

public class BuildDictionaryFile {

    public static void main(String args[]) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        String filename = args[0];
        String encoding = "";
        String outputFile = "";
        String listingFile = "";
        if (args.length >= 2) encoding = args[1];
        if (args.length >= 3) outputFile = args[2];
        if (args.length >= 4) listingFile = args[3];
        BuildDictionaryFile dictionary = new BuildDictionaryFile();
        dictionary.build(filename, encoding);
        DataOutputStream out = null;
        if (outputFile.length() != 0) {
            out = new DataOutputStream(new FileOutputStream(outputFile));
            dictionary.writeDictionaryFile(out);
        }
        PrintWriter listing = null;
        if (listingFile.length() != 0) {
            listing = new PrintWriter(new OutputStreamWriter(new FileOutputStream(listingFile), "UnicodeLittle"));
            dictionary.printWordList("", 0, listing);
            listing.close();
        }
    }

    public BuildDictionaryFile() {
    }

    public void build(String filename, String encoding) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        FileInputStream file = new FileInputStream(filename);
        InputStreamReader in;
        if (encoding.length() == 0) in = new InputStreamReader(file); else in = new InputStreamReader(file, encoding);
        buildColumnMap(in);
        file = new FileInputStream(filename);
        if (encoding.length() == 0) in = new InputStreamReader(file); else in = new InputStreamReader(file, encoding);
        buildStateTable(in);
    }

    public void buildColumnMap(InputStreamReader in) throws IOException {
        System.out.println("Building column map...");
        UnicodeSet charsInFile = new UnicodeSet();
        int c = in.read();
        int totalChars = 0;
        while (c >= 0) {
            ++totalChars;
            if (totalChars > 0 && totalChars % 5000 == 0) System.out.println("Read " + totalChars + " characters...");
            if (c > ' ') charsInFile.add((char) c);
            c = in.read();
        }
        StringBuffer tempReverseMap = new StringBuffer();
        tempReverseMap.append(' ');
        columnMap = new CompactByteArray();
        int n = charsInFile.getRangeCount();
        byte p = 1;
        for (int i = 0; i < n; ++i) {
            char start = (char) charsInFile.getRangeStart(i);
            char end = (char) charsInFile.getRangeEnd(i);
            for (char ch = start; ch <= end; ch++) {
                if (columnMap.elementAt(Character.toLowerCase(ch)) == 0) {
                    columnMap.setElementAt(Character.toUpperCase(ch), Character.toUpperCase(ch), p);
                    columnMap.setElementAt(Character.toLowerCase(ch), Character.toLowerCase(ch), p);
                    ++p;
                    tempReverseMap.append(ch);
                }
            }
        }
        columnMap.compact();
        reverseColumnMap = new char[p];
        Utility.getChars(tempReverseMap, 0, p, reverseColumnMap, 0);
        System.out.println("total columns = " + p);
        numCols = p;
        numColGroups = (numCols >> 5) + 1;
    }

    public void buildStateTable(InputStreamReader in) throws IOException {
        Vector tempTable = new Vector();
        tempTable.addElement(new int[numCols + 1]);
        int state = 0;
        int c = in.read();
        int[] row = null;
        int charsInWord = 0;
        while (c >= 0) {
            charsInWord++;
            short column = columnMap.elementAt((char) c);
            row = (int[]) (tempTable.elementAt(state));
            if (column != 0) {
                if (row[column] == 0) {
                    row[column] = tempTable.size();
                    ++row[numCols];
                    state = (tempTable.size());
                    tempTable.addElement(new int[numCols + 1]);
                } else state = row[column];
            } else if (state != 0) {
                if (row[0] != -1) {
                    row[0] = -1;
                    ++row[numCols];
                    uniqueWords++;
                    totalUniqueWordChars += charsInWord;
                }
                totalWords++;
                if (totalWords % 5000 == 0) System.out.println("Read " + totalWords + " words, " + tempTable.size() + " rows...");
                charsInWord = 0;
                state = 0;
            }
            c = in.read();
        }
        if (state != 0) {
            row = (int[]) (tempTable.elementAt(state));
            if (row[0] != -1) {
                row[0] = -1;
                uniqueWords++;
                totalUniqueWordChars += charsInWord;
            }
            totalWords++;
        }
        compress(tempTable);
        table = new short[numCols * tempTable.size()];
        for (int i = 0; i < tempTable.size(); i++) {
            row = (int[]) tempTable.elementAt(i);
            for (int j = 0; j < numCols; j++) table[i * numCols + j] = (short) row[j];
        }
    }

    private void compress(Vector tempTable) {
        System.out.println("Before compression:");
        System.out.println("  Number of rows = " + tempTable.size());
        System.out.println("  Number of columns = " + numCols);
        System.out.println("  Number of cells = " + tempTable.size() * numCols);
        deleteDuplicateRows(tempTable);
        System.out.println("After removing duplicate rows:");
        System.out.println("  Number of rows = " + tempTable.size());
        System.out.println("  Number of columns = " + numCols);
        System.out.println("  Number of cells = " + tempTable.size() * numCols);
        stackRows(tempTable);
        if (tempTable.size() > 32767) throw new IllegalArgumentException("Too many rows in table!");
        System.out.println("After doubling up on rows:");
        System.out.println("  Number of rows = " + tempTable.size());
        System.out.println("  Number of columns = " + numCols);
        System.out.println("  Number of cells = " + tempTable.size() * numCols);
    }

    private void deleteDuplicateRows(Vector tempTable) {
        Vector work = (Vector) (tempTable.clone());
        boolean didDeleteRow = true;
        Vector tempMapping = new Vector(work.size());
        int[] mapping = new int[work.size()];
        for (int i = 0; i < mapping.length; i++) {
            mapping[i] = i;
            tempMapping.addElement(new Integer(i));
        }
        boolean[] tbd = new boolean[work.size()];
        while (didDeleteRow) {
            System.out.println(" " + work.size() + " rows...");
            int deletedRows = 0;
            didDeleteRow = false;
            sortTable(work, tempMapping, mapping, 1, work.size());
            for (int i = 0; i < work.size() - 1; ) {
                System.out.print("Deleting, inspecting row " + i + ", deleted " + deletedRows + " rows...\r");
                int rowToDelete = ((Integer) (tempMapping.elementAt(i + 1))).intValue();
                int rowToMapTo = ((Integer) (tempMapping.elementAt(i))).intValue();
                if (compareRows((int[]) work.elementAt(i), (int[]) work.elementAt(i + 1), mapping) == 0) {
                    tbd[rowToDelete] = true;
                    tempTable.setElementAt(null, rowToDelete);
                    while (tbd[mapping[rowToMapTo]]) mapping[rowToMapTo] = mapping[mapping[rowToMapTo]];
                    mapping[rowToDelete] = mapping[rowToMapTo];
                    didDeleteRow = true;
                    deletedRows++;
                    work.removeElementAt(i + 1);
                    tempMapping.removeElementAt(i + 1);
                } else i++;
            }
            for (int i = 0; i < mapping.length; i++) {
                if (tbd[i] && tbd[mapping[i]]) mapping[i] = mapping[mapping[i]];
            }
        }
        int decrementBy = 0;
        for (int i = 0; i < mapping.length; i++) {
            if (tbd[i]) decrementBy++; else mapping[i] -= decrementBy;
        }
        for (int i = 0; i < mapping.length; i++) {
            if (tbd[i]) mapping[i] = mapping[mapping[i]];
        }
        for (int i = tempTable.size() - 1; i >= 0; i--) {
            if (tbd[i]) tempTable.removeElementAt(i); else {
                int[] row = (int[]) tempTable.elementAt(i);
                for (int j = 0; j < numCols; j++) row[j] = (row[j] == -1) ? -1 : mapping[row[j]];
            }
        }
    }

    private void sortTable(Vector table, Vector tempMapping, int[] mapping, int start, int end) {
        System.out.print("Sorting (" + start + ", " + end + ")...\r");
        if (start + 1 >= end) return; else if (start + 10 >= end) {
            for (int i = start + 1; i < end; i++) {
                int[] row = (int[]) table.elementAt(i);
                Integer tempMap = (Integer) tempMapping.elementAt(i);
                int j;
                for (j = i - 1; j >= start; j--) {
                    if (compareRows((int[]) table.elementAt(j), row, mapping) > 0) {
                        table.setElementAt((int[]) table.elementAt(j), j + 1);
                        tempMapping.setElementAt((Integer) tempMapping.elementAt(j), j + 1);
                    } else {
                        table.setElementAt(row, j + 1);
                        tempMapping.setElementAt(tempMap, j + 1);
                        break;
                    }
                }
                if (j < start) {
                    table.setElementAt(row, start);
                    tempMapping.setElementAt(tempMap, start);
                }
            }
        } else {
            int boundaryPos = (start + end) / 2;
            int i;
            boolean allTheSame = true;
            int firstDifferent = 0;
            do {
                int[] boundary = (int[]) table.elementAt(boundaryPos);
                i = start;
                int j = end - 1;
                int[] row = null;
                byte compResult;
                while (i < j) {
                    row = (int[]) table.elementAt(i);
                    while (i <= j && compareRows(row, boundary, mapping) < 0) {
                        i++;
                        row = (int[]) table.elementAt(i);
                    }
                    row = (int[]) table.elementAt(j);
                    compResult = compareRows(row, boundary, mapping);
                    while (i <= j && (compResult >= 0)) {
                        if (compResult != 0) {
                            allTheSame = false;
                            firstDifferent = j;
                        }
                        j--;
                        row = (int[]) table.elementAt(j);
                        compResult = compareRows(row, boundary, mapping);
                    }
                    if (i <= j) {
                        row = (int[]) table.elementAt(j);
                        table.setElementAt(table.elementAt(i), j);
                        table.setElementAt(row, i);
                        Object temp = tempMapping.elementAt(j);
                        tempMapping.setElementAt(tempMapping.elementAt(i), j);
                        tempMapping.setElementAt(temp, i);
                    }
                }
                if (i <= start) {
                    if (allTheSame) return; else boundaryPos = firstDifferent;
                }
            } while (i <= start);
            sortTable(table, tempMapping, mapping, start, i);
            sortTable(table, tempMapping, mapping, i, end);
        }
    }

    private byte compareRows(int[] row1, int[] row2, int[] mapping) {
        for (int i = 0; i < numCols; i++) {
            int c1 = (row1[i] == -1) ? -1 : mapping[row1[i]];
            int c2 = (row2[i] == -1) ? -1 : mapping[row2[i]];
            if (c1 < c2) return -1; else if (c1 > c2) return 1;
        }
        return 0;
    }

    private int[] buildRowIndex(Vector tempTable) {
        int[] tempRowIndex = new int[tempTable.size()];
        rowIndexFlagsIndex = new short[tempTable.size()];
        Vector tempRowIndexFlags = new Vector();
        rowIndexShifts = new byte[tempTable.size()];
        for (int i = 0; i < tempTable.size(); i++) {
            tempRowIndex[i] = i;
            int[] row = (int[]) tempTable.elementAt(i);
            if (row[numCols] == 1 && row[0] == 0) {
                int j = 0;
                while (row[j] == 0) ++j;
                rowIndexFlagsIndex[i] = (short) (-j);
            } else {
                int[] flags = new int[numColGroups];
                int nextFlag = 1;
                int colGroup = 0;
                for (int j = 0; j < numCols; j++) {
                    if (row[j] != 0) flags[colGroup] |= nextFlag;
                    nextFlag <<= 1;
                    if (nextFlag == 0) {
                        ++colGroup;
                        nextFlag = 1;
                    }
                }
                colGroup = 0;
                int j = 0;
                while (j < tempRowIndexFlags.size()) {
                    if (((Integer) tempRowIndexFlags.elementAt(j)).intValue() == flags[colGroup]) {
                        ++colGroup;
                        ++j;
                        if (colGroup >= numColGroups) break;
                    } else if (colGroup != 0) colGroup = 0; else ++j;
                }
                rowIndexFlagsIndex[i] = (short) (j - colGroup);
                while (colGroup < numColGroups) {
                    tempRowIndexFlags.addElement(new Integer(flags[colGroup]));
                    ++colGroup;
                }
            }
        }
        rowIndexFlags = new int[tempRowIndexFlags.size()];
        for (int i = 0; i < rowIndexFlags.length; i++) rowIndexFlags[i] = ((Integer) tempRowIndexFlags.elementAt(i)).intValue();
        System.out.println("Number of column groups = " + numColGroups);
        System.out.println("Size of rowIndexFlags = " + rowIndexFlags.length);
        return tempRowIndex;
    }

    private void stackRows(Vector tempTable) {
        int[] tempRowIndex = buildRowIndex(tempTable);
        boolean[] tbd = new boolean[tempTable.size()];
        for (int i = 0; i < tempTable.size(); i++) {
            if (tbd[i]) continue;
            System.out.print("Stacking, inspecting row " + i + "...\r");
            int[] destRow = (int[]) tempTable.elementAt(i);
            boolean[] tempFlags = new boolean[numCols];
            boolean[] filledCells = new boolean[numCols];
            for (int j = 0; j < numCols; j++) filledCells[j] = destRow[j] != 0;
            for (int j = i + 1; destRow[numCols] < numCols && j < tempTable.size(); j++) {
                if (tbd[j]) continue;
                int[] srcRow = (int[]) tempTable.elementAt(j);
                if (srcRow[numCols] + destRow[numCols] > numCols) continue;
                int maxLeftShift = -999;
                int maxRightShift = 0;
                for (int k = 0; k < numCols; k++) {
                    tempFlags[k] = srcRow[k] != 0;
                    if (tempFlags[k]) {
                        if (maxLeftShift == -999) maxLeftShift = -k;
                        maxRightShift = (numCols - 1) - k;
                    }
                }
                int shift;
                for (shift = maxLeftShift; shift <= maxRightShift; shift++) {
                    int k;
                    for (k = 0; k < numCols; k++) {
                        if (tempFlags[k] && filledCells[k + shift]) break;
                    }
                    if (k >= numCols) break;
                }
                if (shift <= maxRightShift) {
                    for (int k = 0; k < numCols; k++) {
                        if (tempFlags[k]) {
                            filledCells[k + shift] = true;
                            destRow[k + shift] = srcRow[k];
                            ++destRow[numCols];
                        }
                    }
                    tbd[j] = true;
                    tempRowIndex[j] = i;
                    rowIndexShifts[j] = (byte) shift;
                }
            }
        }
        int decrementBy = 0;
        for (int i = 0; i < tempRowIndex.length; i++) {
            if (!tbd[i]) tempRowIndex[i] -= decrementBy; else ++decrementBy;
        }
        rowIndex = new short[tempRowIndex.length];
        for (int i = tempRowIndex.length - 1; i >= 0; i--) {
            if (tbd[i]) {
                rowIndex[i] = (short) (tempRowIndex[tempRowIndex[i]]);
                tempTable.removeElementAt(i);
            } else rowIndex[i] = (short) tempRowIndex[i];
        }
    }

    private void printTable() {
        short cell;
        int populatedCells = 0;
        System.out.println();
        System.out.println("Conceptual table:");
        System.out.print(" Row:");
        for (int i = 0; i < reverseColumnMap.length; i++) {
            System.out.print("   " + reverseColumnMap[i]);
        }
        for (int i = 0; i < rowIndex.length; i++) {
            System.out.println();
            printNumber(i, 4);
            System.out.print(":");
            for (int j = 0; j < numCols; j++) printNumber(at(i, j), 4);
        }
        System.out.println('\n');
        System.out.println();
        System.out.println("Internally stored table:");
        System.out.print(" Row:");
        for (int i = 0; i < reverseColumnMap.length; i++) {
            System.out.print("   " + reverseColumnMap[i]);
        }
        for (int i = 0; i < table.length; i++) {
            if (i % numCols == 0) {
                System.out.println();
                printNumber(i / numCols, 4);
                System.out.print(":");
            }
            cell = table[i];
            if (cell != 0) populatedCells++;
            printNumber(cell, 4);
        }
        System.out.println('\n');
        System.out.println("Row index:");
        for (int i = 0; i < rowIndex.length; i++) {
            System.out.print("   " + i + " -> " + rowIndex[i]);
            if (rowIndexFlagsIndex[i] < 0) System.out.print(", flags = " + Integer.toBinaryString((1 << (-rowIndexFlagsIndex[i]))) + " (" + rowIndexFlagsIndex[i]); else System.out.print(", flags = " + Integer.toBinaryString(rowIndexFlags[rowIndexFlagsIndex[i]]) + " (" + rowIndexFlagsIndex[i]);
            System.out.println("), shift = " + rowIndexShifts[i]);
        }
    }

    private void printConceptualTable(String initialString, int state, boolean[] flags) {
        if (initialString.length() == 0) System.out.println("root:"); else System.out.println(initialString + ':');
        if (!flags[state]) {
            flags[state] = true;
            printNumber(state, 4);
            System.out.print(":");
            for (int i = 0; i < numCols; i++) printNumber(at(state, i), 4);
            System.out.println();
        }
        int nextState;
        for (int i = 0; i < numCols; i++) {
            nextState = at(state, i);
            if (nextState > 0 && !flags[nextState]) {
                printNumber(nextState, 4);
                System.out.print(":");
                for (int j = 0; j < numCols; j++) printNumber(at(nextState, j), 4);
                System.out.println();
            }
        }
        for (int i = 0; i < numCols; i++) {
            nextState = at(state, i);
            if (nextState > 0 && !flags[nextState]) {
                char nextChar;
                if (nextState == 27) nextChar = ' '; else if (nextState == 26) nextChar = '\''; else nextChar = (char) (i + 'a');
                flags[nextState] = true;
                printConceptualTable(initialString + nextChar, nextState, flags);
            }
        }
    }

    private void printWordList(String partialWord, int state, PrintWriter out) throws IOException {
        if (state == -1) {
            System.out.println(partialWord);
            if (out != null) out.println(partialWord);
        } else {
            for (int i = 0; i < numCols; i++) {
                if (at(state, i) != 0) printWordList(partialWord + reverseColumnMap[i], at(state, i), out);
            }
        }
    }

    private void writeDictionaryFile(DataOutputStream out) throws IOException {
        out.writeInt(0);
        char[] columnMapIndexes = columnMap.getIndexArray();
        out.writeInt(columnMapIndexes.length);
        for (int i = 0; i < columnMapIndexes.length; i++) out.writeShort((short) columnMapIndexes[i]);
        byte[] columnMapValues = columnMap.getValueArray();
        out.writeInt(columnMapValues.length);
        for (int i = 0; i < columnMapValues.length; i++) out.writeByte((byte) columnMapValues[i]);
        out.writeInt(numCols);
        out.writeInt(numColGroups);
        out.writeInt(rowIndex.length);
        for (int i = 0; i < rowIndex.length; i++) out.writeShort(rowIndex[i]);
        out.writeInt(rowIndexFlagsIndex.length);
        for (int i = 0; i < rowIndexFlagsIndex.length; i++) out.writeShort(rowIndexFlagsIndex[i]);
        out.writeInt(rowIndexFlags.length);
        for (int i = 0; i < rowIndexFlags.length; i++) out.writeInt(rowIndexFlags[i]);
        out.writeInt(rowIndexShifts.length);
        for (int i = 0; i < rowIndexShifts.length; i++) out.writeByte(rowIndexShifts[i]);
        out.writeInt(table.length);
        for (int i = 0; i < table.length; i++) out.writeShort(table[i]);
        out.close();
    }

    private void printNumber(int x, int width) {
        String s = String.valueOf(x);
        if (width > s.length()) System.out.print(spaces.substring(0, width - s.length()));
        if (x != 0) System.out.print(s); else System.out.print('.');
    }

    public final short at(int row, char ch) {
        int col = columnMap.elementAt(ch);
        return at(row, col);
    }

    public final short at(int row, int col) {
        if (cellIsPopulated(row, col)) return internalAt(rowIndex[row], col + rowIndexShifts[row]); else return 0;
    }

    private final boolean cellIsPopulated(int row, int col) {
        if (rowIndexFlagsIndex[row] < 0) return col == -rowIndexFlagsIndex[row]; else {
            int flags = rowIndexFlags[rowIndexFlagsIndex[row] + (col >> 5)];
            return (flags & (1 << (col & 0x1f))) != 0;
        }
    }

    private final short internalAt(int row, int col) {
        return table[row * numCols + col];
    }

    private CompactByteArray columnMap = null;

    private char[] reverseColumnMap = null;

    private int numCols;

    private int numColGroups;

    private short[] table = null;

    private short[] rowIndex = null;

    private int[] rowIndexFlags = null;

    private short[] rowIndexFlagsIndex = null;

    private byte[] rowIndexShifts = null;

    private int totalWords = 0;

    private int uniqueWords = 0;

    private int totalUniqueWordChars = 0;

    private static final String spaces = "      ";
}
