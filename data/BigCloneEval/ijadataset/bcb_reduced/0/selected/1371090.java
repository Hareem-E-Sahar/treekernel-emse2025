package org.m4eclipse.eclipse.launch.console;

/**
 * Simple circular buffer that stores a fix number of lines.
 */
public class ConsoleDocument {

    public static final int COMMAND = 0;

    public static final int MESSAGE = 1;

    public static final int ERROR = 2;

    public static final int STATUS = 3;

    public static final int DELIMITER = 4;

    private int[] lineTypes;

    private String[] lines;

    private int writeIndex = 0;

    private int readIndex = 0;

    private static final int BUFFER_SIZE = 200;

    protected static class ConsoleLine {

        public String line;

        public int type;

        ConsoleLine(String line, int type) {
            this.line = line;
            this.type = type;
        }
    }

    /**
     * Creates an empty console document.
     */
    public ConsoleDocument() {
    }

    /**
     * Clears the console document.
     */
    public void clear() {
        lineTypes = null;
        lines = null;
        writeIndex = 0;
        readIndex = 0;
    }

    /**
     * Appends a line of the specified type to the end of the console.
     */
    public void appendConsoleLine(int type, String line) {
        if (lines == null) {
            lines = new String[BUFFER_SIZE];
            lineTypes = new int[BUFFER_SIZE];
        }
        lines[writeIndex] = line;
        lineTypes[writeIndex] = type;
        if (++writeIndex >= BUFFER_SIZE) {
            writeIndex = 0;
        }
        if (writeIndex == readIndex) {
            if (++readIndex >= BUFFER_SIZE) {
                readIndex = 0;
            }
        }
    }

    public ConsoleLine[] getLines() {
        if (isEmpty()) return new ConsoleLine[0];
        ConsoleLine[] docLines = new ConsoleLine[readIndex > writeIndex ? BUFFER_SIZE : writeIndex];
        int index = readIndex;
        for (int i = 0; i < docLines.length; i++) {
            docLines[i] = new ConsoleLine(lines[index], lineTypes[index]);
            if (++index >= BUFFER_SIZE) {
                index = 0;
            }
        }
        return docLines;
    }

    public boolean isEmpty() {
        return writeIndex == readIndex;
    }
}
