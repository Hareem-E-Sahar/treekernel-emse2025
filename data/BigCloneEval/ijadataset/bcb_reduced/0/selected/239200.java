package com.lb.trac.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.LoggingEvent;
import com.ice.tar.TarArchive;
import com.ice.tar.TarEntry;

/**
 * DailyRollingFileAppender extends {@link FileAppender} so that the underlying
 * file is rolled over at a user chosen frequency.
 * 
 * DailyRollingFileAppender has been observed to exhibit synchronization issues
 * and data loss. The log4j extras companion includes alternatives which should
 * be considered for new deployments and which are discussed in the
 * documentation for org.apache.log4j.rolling.RollingFileAppender.
 * 
 * <p>
 * The rolling schedule is specified by the <b>DatePattern</b> option. This
 * pattern should follow the {@link SimpleDateFormat} conventions. In
 * particular, you <em>must</em> escape literal text within a pair of single
 * quotes. A formatted version of the date pattern is used as the suffix for the
 * rolled file name.
 * 
 * <p>
 * For example, if the <b>File</b> option is set to <code>/foo/bar.log</code>
 * and the <b>DatePattern</b> set to <code>'.'yyyy-MM-dd</code>, on 2001-02-16
 * at midnight, the logging file <code>/foo/bar.log</code> will be copied to
 * <code>/foo/bar.log.2001-02-16</code> and logging for 2001-02-17 will continue
 * in <code>/foo/bar.log</code> until it rolls over the next day.
 * 
 * <p>
 * Is is possible to specify monthly, weekly, half-daily, daily, hourly, or
 * minutely rollover schedules.
 * 
 * <p>
 * <table border="1" cellpadding="2">
 * <tr>
 * <th>DatePattern</th>
 * <th>Rollover schedule</th>
 * <th>Example</th>
 * 
 * <tr>
 * <td><code>'.'yyyy-MM</code>
 * <td>Rollover at the beginning of each month</td>
 * 
 * <td>At midnight of May 31st, 2002 <code>/foo/bar.log</code> will be copied to
 * <code>/foo/bar.log.2002-05</code>. Logging for the month of June will be
 * output to <code>/foo/bar.log</code> until it is also rolled over the next
 * month.
 * 
 * <tr>
 * <td><code>'.'yyyy-ww</code>
 * 
 * <td>Rollover at the first day of each week. The first day of the week depends
 * on the locale.</td>
 * 
 * <td>Assuming the first day of the week is Sunday, on Saturday midnight, June
 * 9th 2002, the file <i>/foo/bar.log</i> will be copied to
 * <i>/foo/bar.log.2002-23</i>. Logging for the 24th week of 2002 will be output
 * to <code>/foo/bar.log</code> until it is rolled over the next week.
 * 
 * <tr>
 * <td><code>'.'yyyy-MM-dd</code>
 * 
 * <td>Rollover at midnight each day.</td>
 * 
 * <td>At midnight, on March 8th, 2002, <code>/foo/bar.log</code> will be copied
 * to <code>/foo/bar.log.2002-03-08</code>. Logging for the 9th day of March
 * will be output to <code>/foo/bar.log</code> until it is rolled over the next
 * day.
 * 
 * <tr>
 * <td><code>'.'yyyy-MM-dd-a</code>
 * 
 * <td>Rollover at midnight and midday of each day.</td>
 * 
 * <td>At noon, on March 9th, 2002, <code>/foo/bar.log</code> will be copied to
 * <code>/foo/bar.log.2002-03-09-AM</code>. Logging for the afternoon of the 9th
 * will be output to <code>/foo/bar.log</code> until it is rolled over at
 * midnight.
 * 
 * <tr>
 * <td><code>'.'yyyy-MM-dd-HH</code>
 * 
 * <td>Rollover at the top of every hour.</td>
 * 
 * <td>At approximately 11:00.000 o'clock on March 9th, 2002,
 * <code>/foo/bar.log</code> will be copied to
 * <code>/foo/bar.log.2002-03-09-10</code>. Logging for the 11th hour of the 9th
 * of March will be output to <code>/foo/bar.log</code> until it is rolled over
 * at the beginning of the next hour.
 * 
 * 
 * <tr>
 * <td><code>'.'yyyy-MM-dd-HH-mm</code>
 * 
 * <td>Rollover at the beginning of every minute.</td>
 * 
 * <td>At approximately 11:23,000, on March 9th, 2001, <code>/foo/bar.log</code>
 * will be copied to <code>/foo/bar.log.2001-03-09-10-22</code>. Logging for the
 * minute of 11:23 (9th of March) will be output to <code>/foo/bar.log</code>
 * until it is rolled over the next minute.
 * 
 * </table>
 * 
 * <p>
 * Do not use the colon ":" character in anywhere in the <b>DatePattern</b>
 * option. The text before the colon is interpeted as the protocol specificaion
 * of a URL which is probably not what you want.
 * 
 * @author Eirik Lygre
 * @author Ceki G&uuml;lc&uuml;
 */
public class SetupFileAppender extends FileAppender {

    private class InfoRolling {

        private File[] files;

        private int backRolling;

        public File[] getFiles() {
            return files;
        }

        public void setFiles(File[] files) {
            this.files = files;
        }

        public int getBackRolling() {
            return backRolling;
        }

        public void setBackRolling(int backRolling) {
            this.backRolling = backRolling;
        }
    }

    public class ZipLogFiles implements Runnable {

        public void run() {
            int backRolling = -1;
            File[] logFiles = null;
            InfoRolling info = new InfoRolling();
            while (info.getFiles() == null || info.getFiles().length == 0) {
                info.setBackRolling(backRolling--);
                info = getLogFilesList(info);
            }
            zipLog(info);
        }

        private InfoRolling getLogFilesList(final InfoRolling infoRolling) {
            final String prefix = getFile().substring(getFile().lastIndexOf(File.separator) + 1);
            File[] logFiles = new File(getFile().substring(0, getFile().lastIndexOf(File.separator))).listFiles(new FileFilter() {

                public boolean accept(File f) {
                    GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
                    gc.roll(Calendar.DAY_OF_MONTH, infoRolling.getBackRolling());
                    return f.getName().indexOf(prefix + sdf.format(gc.getTime())) != -1 && f.getName().indexOf(".tar") == -1 && f.getName().indexOf(".zip") == -1;
                }
            });
            infoRolling.setFiles(logFiles);
            return infoRolling;
        }

        private void zipLog(InfoRolling info) {
            boolean zipped = false;
            File[] logFiles = info.getFiles();
            try {
                GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
                gc.roll(Calendar.DAY_OF_MONTH, info.getBackRolling());
                final String prefixFileName = logFileName.substring(0, logFileName.indexOf("."));
                final String date = sdf.format(gc.getTime());
                String tarName = new StringBuffer(prefixFileName).append(date).append(".tar").toString();
                String gzipFileName = new StringBuffer(tarName).append(".zip").toString();
                String tarPath = new StringBuffer(logDir).append(File.separator).append(tarName).toString();
                TarArchive ta = new TarArchive(new FileOutputStream(tarPath));
                for (int i = 0; i < logFiles.length; i++) {
                    File file = logFiles[i];
                    TarEntry te = new TarEntry(file);
                    ta.writeEntry(te, true);
                }
                ta.closeArchive();
                ZipEntry zipEntry = new ZipEntry(tarName);
                zipEntry.setMethod(ZipEntry.DEFLATED);
                ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(new StringBuffer(logDir).append(File.separator).append(gzipFileName).toString()));
                zout.putNextEntry(zipEntry);
                InputStream in = new FileInputStream(tarPath);
                byte[] buffer = new byte[2048];
                int ch = 0;
                while ((ch = in.read(buffer)) >= 0) {
                    zout.write(buffer, 0, ch);
                }
                zout.flush();
                zout.close();
                in.close();
                logFiles = new File(getFile().substring(0, getFile().lastIndexOf(File.separator))).listFiles(new FileFilter() {

                    public boolean accept(File file) {
                        return file.getName().endsWith(".tar");
                    }
                });
                for (int i = 0; i < logFiles.length; i++) {
                    File file = logFiles[i];
                    System.out.println("cancello : " + file.getAbsolutePath() + " : " + file.delete());
                }
                logFiles = new File(getFile().substring(0, getFile().lastIndexOf(File.separator))).listFiles(new FileFilter() {

                    public boolean accept(File file) {
                        return file.getName().indexOf(prefixFileName + ".log" + date) != -1 && !file.getName().endsWith(".zip");
                    }
                });
                for (int i = 0; i < logFiles.length; i++) {
                    File file = logFiles[i];
                    System.out.println("cancello : " + file.getAbsolutePath() + " : " + file.delete());
                }
                zipped = true;
            } catch (FileNotFoundException ex) {
                LogLog.error("Filenotfound: " + ex.getMessage(), ex);
            } catch (IOException ex) {
                LogLog.error("IOException: " + ex.getMessage(), ex);
            } finally {
                if (zipped) {
                    for (int i = 0; i < logFiles.length; i++) {
                        File file = logFiles[i];
                        file.delete();
                    }
                }
            }
        }
    }

    public class RenameLogFiles implements Runnable {

        private File rollFile;

        private File rollTarget;

        private String rollFileName;

        private File[] logFiles;

        private boolean renameSucceeded = true;

        public RenameLogFiles(File[] logFiles, File rollFile, File rollTarget, String rollFileName) {
            this.rollFile = rollFile;
            this.rollTarget = rollTarget;
            this.rollFileName = rollFileName;
            this.logFiles = logFiles;
        }

        public void run() {
            for (int i = logFiles.length + 1; i >= 1 && renameSucceeded; i--) {
                rollFile = new File(rollFileName + "." + i);
                if (rollFile.exists()) {
                    rollTarget = new File(rollFileName + '.' + (i + 1));
                    LogLog.debug("Renaming file " + rollFile + " to " + rollTarget);
                    renameSucceeded = rollFile.renameTo(rollTarget);
                }
            }
        }

        public boolean isRenameSucceeded() {
            return renameSucceeded;
        }

        public void setRenameSucceeded(boolean renameSucceeded) {
            this.renameSucceeded = renameSucceeded;
        }
    }

    public final class LogFilesComparator implements Comparator<File> {

        public static final int ASC = 1;

        public static final int DESC = -1;

        private int orderType = 1;

        public LogFilesComparator() {
            this(ASC);
        }

        public LogFilesComparator(int orderType) {
            this.orderType = orderType;
        }

        public int compare(File o1, File o2) {
            String nameA = o1.getAbsolutePath().substring(o1.getAbsolutePath().lastIndexOf(".") + 1);
            String nameB = o2.getAbsolutePath().substring(o2.getAbsolutePath().lastIndexOf(".") + 1);
            int a = Integer.parseInt(nameA);
            int b = Integer.parseInt(nameB);
            return (a - b) * this.orderType;
        }
    }

    /**
	 * The default maximum file size is 10MB.
	 */
    protected long maxFileSize = 10 * 1024 * 1024;

    /**
	 * There is one backup file by default.
	 */
    protected int maxBackupIndex = 1;

    private long nextRollover = 0;

    static final int TOP_OF_TROUBLE = -1;

    static final int TOP_OF_MINUTE = 0;

    static final int TOP_OF_HOUR = 1;

    static final int HALF_DAY = 2;

    static final int TOP_OF_DAY = 3;

    static final int TOP_OF_WEEK = 4;

    static final int TOP_OF_MONTH = 5;

    /**
	 * The date pattern. By default, the pattern is set to "'.'yyyy-MM-dd"
	 * meaning daily rollover.
	 */
    private String datePattern = "'.'yyyy-MM-dd";

    /**
	 * Determina se comprimere i log del giorno prima in un unico tar
	 */
    private boolean compress = false;

    /**
	 * The log file will be renamed to the value of the scheduledFilename
	 * variable when the next interval is entered. For example, if the rollover
	 * period is one hour, the log file will be renamed to the value of
	 * "scheduledFilename" at the beginning of the next hour.
	 * 
	 * The precise time when a rollover occurs depends on logging activity.
	 */
    private String scheduledFilename;

    /**
	 * Cancella i file piu' vecchi una volta raggiunto il limite impostato da
	 * {@link #maxBackupIndex}
	 * 
	 */
    private boolean deleteOld = true;

    /**
	 * The next time we estimate a rollover should occur.
	 */
    private long nextCheck = System.currentTimeMillis() - 1;

    Date now = new Date();

    SimpleDateFormat sdf;

    RollingCalendar rc = new RollingCalendar();

    int checkPeriod = TOP_OF_TROUBLE;

    private String logDir = "";

    private String logFileName = "";

    static final TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");

    private static final int ROLL = 0;

    private static final int DAILY = 1;

    /**
	 * The default constructor does nothing.
	 */
    public SetupFileAppender() {
    }

    /**
	 * Instantiate a <code>DailyRollingFileAppender</code> and open the file
	 * designated by <code>filename</code>. The opened filename will become the
	 * ouput destination for this appender.
	 */
    public SetupFileAppender(Layout layout, String filename, String datePattern) throws IOException {
        super(layout, filename, true);
        this.datePattern = datePattern;
        activateOptions();
    }

    /**
	 * The <b>DatePattern</b> takes a string in the same format as expected by
	 * {@link SimpleDateFormat}. This options determines the rollover schedule.
	 */
    public void setDatePattern(String pattern) {
        datePattern = pattern;
    }

    /** Returns the value of the <b>DatePattern</b> option. */
    public String getDatePattern() {
        return datePattern;
    }

    public void activateOptions() {
        super.activateOptions();
        if (datePattern != null && fileName != null) {
            now.setTime(System.currentTimeMillis());
            sdf = new SimpleDateFormat(datePattern);
            int type = computeCheckPeriod();
            printPeriodicity(type);
            rc.setType(type);
            File file = new File(fileName);
            scheduledFilename = fileName + sdf.format(new Date(file.lastModified()));
        } else {
            LogLog.error("Either File or DatePattern options are not set for appender [" + name + "].");
        }
        LogLog.debug("Delete old files: " + deleteOld);
        if (deleteOld) {
            LogLog.debug("Max backup files " + maxBackupIndex);
        }
    }

    void printPeriodicity(int type) {
        switch(type) {
            case TOP_OF_MINUTE:
                LogLog.debug("Appender [" + name + "] to be rolled every minute.");
                break;
            case TOP_OF_HOUR:
                LogLog.debug("Appender [" + name + "] to be rolled on top of every hour.");
                break;
            case HALF_DAY:
                LogLog.debug("Appender [" + name + "] to be rolled at midday and midnight.");
                break;
            case TOP_OF_DAY:
                LogLog.debug("Appender [" + name + "] to be rolled at midnight.");
                break;
            case TOP_OF_WEEK:
                LogLog.debug("Appender [" + name + "] to be rolled at start of week.");
                break;
            case TOP_OF_MONTH:
                LogLog.debug("Appender [" + name + "] to be rolled at start of every month.");
                break;
            default:
                LogLog.warn("Unknown periodicity for appender [" + name + "].");
        }
    }

    int computeCheckPeriod() {
        RollingCalendar rollingCalendar = new RollingCalendar(gmtTimeZone, Locale.getDefault());
        Date epoch = new Date(0);
        if (datePattern != null) {
            for (int i = TOP_OF_MINUTE; i <= TOP_OF_MONTH; i++) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
                simpleDateFormat.setTimeZone(gmtTimeZone);
                String r0 = simpleDateFormat.format(epoch);
                rollingCalendar.setType(i);
                Date next = new Date(rollingCalendar.getNextCheckMillis(epoch));
                String r1 = simpleDateFormat.format(next);
                if (r0 != null && r1 != null && !r0.equals(r1)) {
                    return i;
                }
            }
        }
        return TOP_OF_TROUBLE;
    }

    /**
	 * Rollover the current file to a new file.
	 */
    void rollOver(int rule) throws IOException {
        switch(rule) {
            case DAILY:
                dailyRollOver();
                break;
            case ROLL:
                overSizeRollover();
                break;
        }
    }

    private void overSizeRollover() {
        File rollTarget = null;
        File rollFile = null;
        String rollFileName = fileName + sdf.format(now);
        if (qw != null) {
            long size = ((CountingQuietWriter) qw).getCount();
            LogLog.debug("rolling over count=" + size);
            nextRollover = size + maxFileSize;
        }
        LogLog.debug("maxBackupIndex=" + maxBackupIndex);
        boolean renameSucceeded = true;
        if (maxBackupIndex > 0) {
            rollFile = new File(rollFileName + '.' + maxBackupIndex);
            if (rollFile.exists() && deleteOld) {
                renameSucceeded = rollFile.delete();
            }
            initLogPath(rollFileName);
            File[] logFiles = getLogFiles(logFileName);
            Arrays.sort(logFiles, new LogFilesComparator());
            RenameLogFiles renameLogFiles = new RenameLogFiles(logFiles, rollFile, rollTarget, rollFileName);
            Thread renameLogs = new Thread(renameLogFiles);
            renameLogFiles.run();
            if (renameSucceeded) {
                rollTarget = new File(rollFileName + "." + 1);
                this.closeFile();
                rollFile = new File(fileName);
                renameSucceeded = rollFile.renameTo(rollTarget);
                if (!renameSucceeded) {
                    try {
                        this.setFile(fileName, true, bufferedIO, bufferSize);
                    } catch (IOException e) {
                        if (e instanceof InterruptedIOException) {
                            Thread.currentThread().interrupt();
                        }
                        LogLog.error("setFile(" + fileName + ", true) call failed.", e);
                    }
                }
            }
        }
        if (renameSucceeded) {
            try {
                this.setFile(fileName, false, bufferedIO, bufferSize);
                nextRollover = 0;
            } catch (IOException e) {
                if (e instanceof InterruptedIOException) {
                    Thread.currentThread().interrupt();
                }
                LogLog.error("setFile(" + fileName + ", false) call failed.", e);
            }
        }
    }

    private void initLogPath(String rollFileName) {
        if (getFile().indexOf(File.separator) != -1) {
            logDir = getFile().substring(0, getFile().lastIndexOf(File.separator));
            logFileName = rollFileName.substring(rollFileName.lastIndexOf(File.separator) + 1);
        }
        logDir = logDir.equals("") ? "." + File.separator : logDir;
        logFileName = logFileName.equals("") ? rollFileName : logFileName;
    }

    private File[] getLogFiles(final String theLog) {
        File[] logFiles = new File(logDir).listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.getName().indexOf(theLog + ".") != -1;
            }
        });
        return logFiles;
    }

    private void dailyRollOver() {
        if (datePattern == null) {
            errorHandler.error("Missing DatePattern option in rollOver().");
            return;
        }
        String datedFilename = fileName + sdf.format(now);
        initLogPath(datedFilename);
        if (scheduledFilename.equals(datedFilename)) {
            return;
        }
        this.closeFile();
        if (compress) {
            Thread thZip = new Thread(new ZipLogFiles());
            thZip.start();
        }
        File target = new File(scheduledFilename);
        if (target.exists()) {
            target.delete();
        }
        File file = new File(fileName);
        boolean result = file.renameTo(target);
        if (result) {
            LogLog.debug(fileName + " -> " + scheduledFilename);
        } else {
            LogLog.error("Failed to rename [" + fileName + "] to [" + scheduledFilename + "].");
        }
        try {
            this.setFile(fileName, true, this.bufferedIO, this.bufferSize);
        } catch (IOException e) {
            errorHandler.error("setFile(" + fileName + ", true) call failed.");
        }
        scheduledFilename = datedFilename;
    }

    /**
	 * This method differentiates DailyRollingFileAppender from its super class.
	 * 
	 * <p>
	 * Before actually logging, this method will check whether it is time to do
	 * a rollover. If it is, it will schedule the next rollover time and then
	 * rollover.
	 * */
    protected void subAppend(LoggingEvent event) {
        long n = System.currentTimeMillis();
        if (fileName != null && qw != null) {
            long size = ((CountingQuietWriter) qw).getCount();
            if (size >= maxFileSize && size >= nextRollover) {
                try {
                    rollOver(ROLL);
                } catch (IOException ioe) {
                    if (ioe instanceof InterruptedIOException) {
                        Thread.currentThread().interrupt();
                    }
                    LogLog.error("rollOver() failed.", ioe);
                }
            }
        }
        if (n >= nextCheck) {
            now.setTime(n);
            nextCheck = rc.getNextCheckMillis(now);
            try {
                rollOver(DAILY);
            } catch (IOException ioe) {
                if (ioe instanceof InterruptedIOException) {
                    Thread.currentThread().interrupt();
                }
                LogLog.error("rollOver() failed.", ioe);
            }
        }
        super.subAppend(event);
    }

    /**
	 * Set the maximum size that the output file is allowed to reach before
	 * being rolled over to backup files.
	 * 
	 * <p>
	 * This method is equivalent to {@link #setMaxFileSize} except that it is
	 * required for differentiating the setter taking a <code>long</code>
	 * argument from the setter taking a <code>String</code> argument by the
	 * JavaBeans {@link java.beans.Introspector Introspector}.
	 * 
	 * @see #setMaxFileSize(String)
	 */
    public void setMaximumFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    /**
	 * Set the maximum size that the output file is allowed to reach before
	 * being rolled over to backup files.
	 * 
	 * <p>
	 * In configuration files, the <b>MaxFileSize</b> option takes an long
	 * integer in the range 0 - 2^63. You can specify the value with the
	 * suffixes "KB", "MB" or "GB" so that the integer is interpreted being
	 * expressed respectively in kilobytes, megabytes or gigabytes. For example,
	 * the value "10KB" will be interpreted as 10240.
	 */
    public void setMaxFileSize(String value) {
        maxFileSize = OptionConverter.toFileSize(value, maxFileSize + 1);
    }

    protected void setQWForFiles(Writer writer) {
        this.qw = new CountingQuietWriter(writer, errorHandler);
    }

    public int getMaxBackupIndex() {
        return maxBackupIndex;
    }

    public void setMaxBackupIndex(int maxBackupIndex) {
        this.maxBackupIndex = maxBackupIndex;
    }

    public boolean isDeleteOld() {
        return deleteOld;
    }

    public void setDeleteOld(boolean deleteOld) {
        this.deleteOld = deleteOld;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }
}

/**
 * RollingCalendar is a helper class to DailyRollingFileAppender. Given a
 * periodicity type and the current time, it computes the start of the next
 * interval.
 * */
class RollingCalendar extends GregorianCalendar {

    private static final long serialVersionUID = -3560331770601814177L;

    int type = SetupFileAppender.TOP_OF_TROUBLE;

    RollingCalendar() {
        super();
    }

    RollingCalendar(TimeZone tz, Locale locale) {
        super(tz, locale);
    }

    void setType(int type) {
        this.type = type;
    }

    public long getNextCheckMillis(Date now) {
        return getNextCheckDate(now).getTime();
    }

    public Date getNextCheckDate(Date now) {
        this.setTime(now);
        switch(type) {
            case SetupFileAppender.TOP_OF_MINUTE:
                this.set(Calendar.SECOND, 0);
                this.set(Calendar.MILLISECOND, 0);
                this.add(Calendar.MINUTE, 1);
                break;
            case SetupFileAppender.TOP_OF_HOUR:
                this.set(Calendar.MINUTE, 0);
                this.set(Calendar.SECOND, 0);
                this.set(Calendar.MILLISECOND, 0);
                this.add(Calendar.HOUR_OF_DAY, 1);
                break;
            case SetupFileAppender.HALF_DAY:
                this.set(Calendar.MINUTE, 0);
                this.set(Calendar.SECOND, 0);
                this.set(Calendar.MILLISECOND, 0);
                int hour = get(Calendar.HOUR_OF_DAY);
                if (hour < 12) {
                    this.set(Calendar.HOUR_OF_DAY, 12);
                } else {
                    this.set(Calendar.HOUR_OF_DAY, 0);
                    this.add(Calendar.DAY_OF_MONTH, 1);
                }
                break;
            case SetupFileAppender.TOP_OF_DAY:
                this.set(Calendar.HOUR_OF_DAY, 0);
                this.set(Calendar.MINUTE, 0);
                this.set(Calendar.SECOND, 0);
                this.set(Calendar.MILLISECOND, 0);
                this.add(Calendar.DATE, 1);
                break;
            case SetupFileAppender.TOP_OF_WEEK:
                this.set(Calendar.DAY_OF_WEEK, getFirstDayOfWeek());
                this.set(Calendar.HOUR_OF_DAY, 0);
                this.set(Calendar.MINUTE, 0);
                this.set(Calendar.SECOND, 0);
                this.set(Calendar.MILLISECOND, 0);
                this.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case SetupFileAppender.TOP_OF_MONTH:
                this.set(Calendar.DATE, 1);
                this.set(Calendar.HOUR_OF_DAY, 0);
                this.set(Calendar.MINUTE, 0);
                this.set(Calendar.SECOND, 0);
                this.set(Calendar.MILLISECOND, 0);
                this.add(Calendar.MONTH, 1);
                break;
            default:
                throw new IllegalStateException("Unknown periodicity type.");
        }
        return getTime();
    }
}
