package com.monad.homerun.log.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Observer;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.monad.homerun.config.ConfigContext;
import com.monad.homerun.core.GlobalProps;
import com.monad.homerun.core.LogTag;
import com.monad.homerun.log.LogService;
import com.monad.homerun.util.TimeUtil;

/**
 * LogMgr sets up the runtime logging environment. Upon a logging request from a client,
 * it checks the config info, and if enabled, creates a log for the category. LogMgr is
 * designed to work with the Java 1.4 logging API and XML-based configuration files.
 */
public class LogMgr implements LogService {

    public static final String LOG_DIR = "logs";

    private static final String defaultCategory = "server";

    private static Map<String, Log> logMap = new HashMap<String, Log>();

    public LogMgr() {
    }

    public Logger getLogger() {
        return getLogger(defaultCategory);
    }

    public Logger getLogger(String categoryName) {
        Logger logger = null;
        Log log = logMap.get(categoryName);
        if (log == null) {
            ConfigContext ctx = null;
            try {
                ctx = Activator.config.getContext("platform/log", "categories/@" + categoryName);
            } catch (IOException ioe) {
                sysLog("No config for log category: " + categoryName);
                ctx = Activator.config.getEmptyContext();
            }
            if (!ctx.isEmpty() && ctx.isEnabled()) {
                log = new Log(categoryName);
                log.start();
                logMap.put(categoryName, log);
            } else {
                sysLog("Invalid log category: " + categoryName);
            }
        }
        if (log != null) {
            logger = log.getLogger();
        }
        if (logger == null) {
            logger = Logger.getAnonymousLogger();
            logger.setLevel(Level.OFF);
        }
        return (logger);
    }

    public void sysLog(String message) {
        System.err.println(message);
    }

    public void startLog(String category) {
        Log log = logMap.get(category);
        if (log != null) {
            log.start();
        }
    }

    public void stopLog(String category) {
        Log log = logMap.get(category);
        if (log != null) {
            log.stop();
        }
    }

    public void setLogLevel(String category, String action, String level) {
        Log log = logMap.get(category);
        if (log != null) {
            log.setLogLevel(action, level);
        }
    }

    public void addObserver(String category, Observer observer) {
        Log log = logMap.get(category);
        if (log != null) {
            log.addObserver(observer);
        }
    }

    public void removeLogs(LogTag[] tags) {
        for (int j = 0; j < tags.length; j++) {
            String logPath = GlobalProps.getHomeDir() + File.separator + LOG_DIR + File.separator + tags[j].logType + File.separator + tags[j].logName;
            new File(logPath).delete();
        }
    }

    public boolean archiveLogs(LogTag[] tags) {
        String logBaseDir = GlobalProps.getHomeDir() + File.separator + LOG_DIR;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (int i = 0; i < tags.length; i++) {
            long time = tags[i].logTime;
            if (time < min) {
                min = time;
            }
            if (time > max) {
                max = time;
            }
        }
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(min);
        int minMonth = cal.get(Calendar.MONTH);
        int minYear = cal.get(Calendar.YEAR);
        cal.setTimeInMillis(max);
        int maxMonth = cal.get(Calendar.MONTH);
        int maxYear = cal.get(Calendar.YEAR);
        String nameStr = null;
        if (minYear == maxYear && minMonth == maxMonth) {
            nameStr = TimeUtil.monthNameStr(min);
        } else {
            nameStr = TimeUtil.monthNameStr(min) + "-" + TimeUtil.monthNameStr(max);
        }
        File archiveDir = new File(logBaseDir + File.separator + "archive");
        if (!archiveDir.isDirectory()) {
            archiveDir.mkdirs();
        }
        String archPath = getUniqueLogName(logBaseDir + File.separator + "archive", nameStr, ".zip");
        File archiveFile = new File(archPath);
        byte[] buffer = new byte[2048];
        try {
            ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(archiveFile));
            zout.setLevel(Deflater.DEFAULT_COMPRESSION);
            for (int i = 0; i < tags.length; i++) {
                String logPath = logBaseDir + File.separator + tags[i].logType + File.separator + tags[i].logName;
                FileInputStream in = new FileInputStream(new File(logPath));
                ZipEntry entry = new ZipEntry(tags[i].logType.charAt(0) + tags[i].logName);
                entry.setComment(tags[i].logType);
                zout.putNextEntry(entry);
                int len = 0;
                while ((len = in.read(buffer)) > 0) {
                    zout.write(buffer, 0, len);
                }
                zout.closeEntry();
                in.close();
            }
            zout.close();
            return true;
        } catch (IOException ioe) {
            if (GlobalProps.DEBUG) {
                System.out.println("Error archiving logs");
                ioe.printStackTrace();
            }
            return false;
        }
    }

    private static String getUniqueLogName(String logPath, String baseName, String suffix) {
        String uLogName = logPath + File.separator + baseName + suffix;
        if (new File(uLogName).exists()) {
            int extension = 1;
            uLogName = logPath + File.separator + baseName + "." + extension + suffix;
            while (new File(uLogName).exists()) {
                ++extension;
                uLogName = logPath + File.separator + baseName + "." + extension + suffix;
            }
        }
        return uLogName;
    }
}
