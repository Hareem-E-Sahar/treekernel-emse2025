package au.vermilion.PC;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A log handler for the PC version, which prints to stderr and a .log file.
 * The log files are zipped and rotated so only the last 10 are kept.
 */
public final class PCLogHandler extends Handler {

    /**
     * The debug console output stream we will write to.
     */
    private final PrintStream consoleStream;

    /**
     * The permanent storage output stream we will write to.
     */
    private final PrintStream fileStream;

    /**
     * Creates the handler, which opens the file we are writing to.
     */
    public PCLogHandler() {
        PrintStream fStream = null;
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("vermilion" + System.currentTimeMillis() + ".log.zip"));
            zos.putNextEntry(new ZipEntry("log.log"));
            fStream = new PrintStream(zos);
            killOldLogs();
        } catch (IOException ex) {
            fStream = null;
        }
        fileStream = fStream;
        consoleStream = System.err;
    }

    /**
     * Writes a log record to the error stream and to the file if it was opened.
     * @param record The message we must format and write to the streams.
     */
    @Override
    public void publish(LogRecord record) {
        MessageFormat mf = new MessageFormat(record.getMessage());
        StringBuffer sb = new StringBuffer(record.getLevel().getName());
        sb.append(": ");
        sb.append(trimClassName(record.getSourceClassName()));
        sb.append(".");
        sb.append(record.getSourceMethodName());
        while (sb.length() < 40) sb.append(" ");
        sb.append(": ");
        if (record.getThrown() != null) {
            mf.format(record.getParameters(), sb, new FieldPosition(0));
            Throwable t = record.getThrown();
            sb.append("\r\n   EXCEPTION: ");
            sb.append(t.toString());
            int count = 0;
            for (StackTraceElement ste : t.getStackTrace()) {
                if (count < 10) {
                    sb.append("\r\n      ");
                    sb.append(trimClassName(ste.getClassName()));
                    sb.append(".");
                    sb.append(ste.getMethodName());
                    sb.append(":");
                    sb.append(ste.getLineNumber());
                }
                count++;
            }
        } else {
            mf.format(record.getParameters(), sb, new FieldPosition(0));
        }
        consoleStream.println(sb.toString());
        if (fileStream != null) {
            try {
                fileStream.println(sb.toString());
            } catch (Exception ex) {
            }
        }
    }

    /**
     * Flushes the log stream(s), as required by the Logger API.
     */
    @Override
    public void flush() {
        try {
            fileStream.flush();
        } catch (Exception ex) {
        }
    }

    /**
     * Closes the log stream(s), as required by the Logger API.
     */
    @Override
    public void close() {
        try {
            fileStream.close();
        } catch (Exception ex) {
        }
    }

    /**
     * Finds the actual class name in a fully qualified class name.
     * @param className A full classname including the package name.
     * @return  The 'leaf' classname, without the package name.
     */
    private String trimClassName(String className) {
        if (className.length() == 0) return "";
        int index = className.lastIndexOf('.');
        if (index >= className.length() - 2) index = className.length() - 2;
        if (index < 0) index = 0;
        return className.substring(index + 1);
    }

    /**
     * Rotates the log files by deleting the oldest ones.
     */
    private void killOldLogs() {
        try {
            File f = new File(".");
            File[] logs = f.listFiles();
            ArrayList<String> logNames = new ArrayList<String>(11);
            for (File logFile : logs) {
                if (logFile.isFile() && logFile.getName().endsWith(".log.zip")) logNames.add(logFile.getName());
            }
            Object[] sorted = logNames.toArray();
            Arrays.sort(sorted);
            if (sorted.length > 10) {
                for (int x = 0; x < sorted.length - 10; x++) {
                    new File((String) sorted[x]).delete();
                }
            }
        } catch (Exception ex) {
        }
    }
}
