package jsync.helpers;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import jFileLib.text.logging.LogWriter;

public class LogHelper {

    private static LogWriter logWriter = null;

    public static void initLogWriter() {
        if (ConfigHelper.useLogging() == false) return;
        if (logWriter == null) logWriter = new LogWriter(LogHelper.getLogFilePath());
    }

    public static boolean createLogFolder() {
        return FileHelper.createDestinationFolder(ConfigHelper.logFolder());
    }

    public static synchronized void writeWarningFormat(String key, Object... args) {
        String message = String.format(LocalesHelper.getText(key), args);
        LogHelper.writeWarning(message);
    }

    public static synchronized void writeInfoFormat(String key, Object... args) {
        String message = String.format(LocalesHelper.getText(key), args);
        LogHelper.writeInfo(message);
    }

    public static synchronized void writeSuccessFormat(String key, Object... args) {
        String message = String.format(LocalesHelper.getText(key), args);
        LogHelper.writeSuccess(message);
    }

    public static synchronized void writeErrorFormat(String key, Object... args) {
        String message = String.format(LocalesHelper.getText(key), args);
        LogHelper.writeError(message);
    }

    public static synchronized void writeWarning(String message) {
        System.out.println(message);
        if (ConfigHelper.useLogging() == false) return;
        if (logWriter == null) return;
        logWriter.writeWarning(message);
    }

    public static synchronized void writeInfo(String message) {
        System.out.println(message);
        if (ConfigHelper.useLogging() == false) return;
        if (logWriter == null) return;
        logWriter.writeInfo(message);
    }

    public static synchronized void writeSuccess(String message) {
        System.out.println(message);
        if (ConfigHelper.useLogging() == false) return;
        if (logWriter == null) return;
        logWriter.writeSuccess(message);
    }

    public static synchronized void writeError(String message) {
        System.out.println(message);
        if (ConfigHelper.useLogging() == false) return;
        if (logWriter == null) return;
        logWriter.writeError(message);
    }

    private static synchronized String getDateTimeString(String format) {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(date);
    }

    private static String getLogFilePath() {
        String logFolderPath = jFileLib.common.Path.getCorrectPath(ConfigHelper.logFolder());
        return String.format("%s%s.log", logFolderPath, getDateTimeString("dd.MM.yyyy"));
    }

    public static void clearLogFolder() {
        boolean deleteOldLogFiles = ConfigHelper.deleteOldLogs();
        boolean zipOldLogFiles = !deleteOldLogFiles;
        File logFolder = new File(ConfigHelper.logFolder());
        File[] logFiles = logFolder.listFiles();
        if (logFiles == null) return;
        int logSaveDays = ConfigHelper.logSaveDays();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -logSaveDays);
        Date lastDate = calendar.getTime();
        for (File logFile : logFiles) {
            if (logFile.isFile() == false || logFile.getName().endsWith(".log") == false) continue;
            String fileName = logFile.getName();
            int index = fileName.indexOf(".log");
            if (index > -1) {
                try {
                    String dateString = fileName.substring(0, index);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                    Date date = sdf.parse(dateString);
                    if (date.before(lastDate)) {
                        if (deleteOldLogFiles) {
                            if (logFile.delete() == true) {
                                if (ConfigHelper.isDebugMode() == true) LogHelper.writeSuccessFormat("fileRemoved", logFile.getAbsolutePath());
                            }
                        } else if (zipOldLogFiles) {
                            LogHelper.zipLogFile(logFile.getAbsolutePath());
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public static synchronized void writeDefaultValue(String section, String key, String value) {
        String message = String.format(LocalesHelper.getText("setDefaultValue"), section, key, value);
        LogHelper.writeInfo(message);
    }

    public static synchronized void writeValueNotValid(String section, String key, String value, String newValue) {
        String message = String.format(LocalesHelper.getText("valueNotValid"), value, key, section, newValue);
        LogHelper.writeInfo(message);
    }

    private static synchronized void writeStackTrace(StackTraceElement[] traces) {
        if (traces == null) return;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < traces.length; i++) {
            StackTraceElement trace = traces[i];
            builder.append(trace);
        }
        LogHelper.writeError(builder.toString());
    }

    public static synchronized void writeException(Exception e) {
        if (e == null) return;
        String localizedMessage = e.getLocalizedMessage();
        String message = e.getMessage();
        if (localizedMessage != null) LogHelper.writeError(localizedMessage); else if (message != null) LogHelper.writeError(message);
        LogHelper.writeStackTrace(e.getStackTrace());
    }

    public static void zipLogFile(String logFilePath) {
        try {
            File logFile = new File(logFilePath);
            if (logFile.exists() == false) return;
            String zipFilePath = logFilePath + ".zip";
            File zipFile = new File(zipFilePath);
            if (zipFile.exists()) {
                LogHelper.writeErrorFormat("logZipFileExistsError", zipFilePath);
                if (zipFile.delete() == false) {
                    LogHelper.writeErrorFormat("logZipFileDeleteError", zipFilePath);
                    return;
                }
            }
            FileOutputStream fileOutputStream = new FileOutputStream(zipFilePath);
            BufferedOutputStream out = new BufferedOutputStream(fileOutputStream);
            ZipOutputStream zipOutputStream = new ZipOutputStream(out);
            FileInputStream fileInputStream = new FileInputStream(logFile);
            BufferedInputStream in = new BufferedInputStream(fileInputStream);
            ZipEntry entry = new ZipEntry(logFile.getName());
            zipOutputStream.putNextEntry(entry);
            int bufferSize = 1024;
            if (logFile.length() < Integer.MAX_VALUE) bufferSize = (int) logFile.length(); else bufferSize = Integer.MAX_VALUE;
            byte[] buffer = new byte[bufferSize];
            int length = 0;
            while ((length = in.read(buffer)) != -1) {
                zipOutputStream.write(buffer, 0, length);
            }
            in.close();
            zipOutputStream.close();
            if (logFile.delete() == false) {
                LogHelper.writeErrorFormat("fileNotRemoved", logFilePath);
            }
        } catch (Exception e) {
            LogHelper.writeErrorFormat("logZippingError", logFilePath, e.getLocalizedMessage());
            LogHelper.writeStackTrace(e.getStackTrace());
        }
    }
}
