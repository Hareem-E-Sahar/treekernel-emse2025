package com.dksoft.model;

import com.dksoft.controller.RunScript;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;

/**
 *
 * Global Calendar method shared among classes
 * @author Dickson
 */
public class CalendarMethod {

    private static Logger logger = Logger.getLogger(CalendarMethod.class.getName());

    public static Dimension dimension;

    /**
     * Get 12 hour format value when pass in 24 hour format value as parameter
     * @param int hour
     * @return String hour value
     */
    public static String getRealHour(int hour) {
        String realHour = null;
        if (hour > 0 && hour <= 11) {
            realHour = String.valueOf(hour);
        } else if (hour == 12) {
            realHour = String.valueOf(hour);
        } else if (hour > 12 && hour <= 24) {
            realHour = String.valueOf(hour - 12);
        }
        return realHour;
    }

    /**
     * Get 12 hour format value when pass in 24 hour format value as parameter
     * @param int hour
     * @return String hour format
     */
    public static String getRealHourFormat(int hour) {
        String hourFormat = null;
        if (hour > 0 && hour <= 11) {
            hourFormat = "a.m";
        } else if (hour == 24) {
            hourFormat = "a.m";
        } else if (hour > 11 && hour < 24) {
            hourFormat = "p.m";
        }
        return hourFormat;
    }

    /**
    * @param propertiesKey
    * @return Color
    */
    public static Color getColor(String propertiesKey) {
        return Color.decode(ResourceBundle.getBundle("label").getString(propertiesKey));
    }

    /**
     * @param objectFont
     * @param propertiesKey
     * @param style
     * @param size
     * @return Font
     */
    public static Font getFont(Font objectFont, String propertiesKey, int style, int size) {
        Font tmpFont = objectFont;
        if (tmpFont == null) {
            tmpFont = new Font(ResourceBundle.getBundle("label").getString(propertiesKey), style, size);
        }
        return tmpFont;
    }

    /**
     * Get month value in integer which start from 0 = January
     * @param month
     * @return int month;
     */
    public static int getMonth(String month) {
        if (month.equalsIgnoreCase("Jan") || month.equalsIgnoreCase("January")) return 0;
        if (month.equalsIgnoreCase("Feb") || month.equalsIgnoreCase("February")) return 1;
        if (month.equalsIgnoreCase("Mar") || month.equalsIgnoreCase("March")) return 2;
        if (month.equalsIgnoreCase("Apr") || month.equalsIgnoreCase("April")) return 3;
        if (month.equalsIgnoreCase("May")) return 4;
        if (month.equalsIgnoreCase("Jun") || month.equalsIgnoreCase("June")) return 5;
        if (month.equalsIgnoreCase("Jul") || month.equalsIgnoreCase("July")) return 6;
        if (month.equalsIgnoreCase("Aug") || month.equalsIgnoreCase("August")) return 7;
        if (month.equalsIgnoreCase("Sep") || month.equalsIgnoreCase("September")) return 8;
        if (month.equalsIgnoreCase("Oct") || month.equalsIgnoreCase("October")) return 9;
        if (month.equalsIgnoreCase("Nov") || month.equalsIgnoreCase("November")) return 10;
        if (month.equalsIgnoreCase("Dec") || month.equalsIgnoreCase("December")) return 11;
        return -1;
    }

    /**
     * Get this year's year
     * @return int year
     */
    public static int getThisYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    /**
     * Get this month
     * @return int month
     */
    public static int getThisMonth() {
        return Calendar.getInstance().get(Calendar.MONTH);
    }

    /**
     * Get today in month
     * @return int today's day
     */
    public static String getToday() {
        return (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) < 10) ? "0" + String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) : String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Copy file by full path file name to specific destination
     * @param srFile eg: "C:/Database/data.db"
     * @param dtFile eg: "D:/Database/data.db"
     */
    public static void copyFileByFullPath(String srFile, String dtFile) {
        try {
            File sourceFile = new File(srFile);
            File destFile = new File(dtFile);
            InputStream in = new FileInputStream(sourceFile);
            OutputStream out = new FileOutputStream(destFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            logger.info("[copyFileByFileName]: file copied to " + dtFile);
        } catch (FileNotFoundException ex) {
            logger.debug("[copyFileByFileName]: " + ex.getMessage() + " in the specified directory.");
            System.exit(0);
        } catch (IOException e) {
            logger.debug("[copyFileByFileName]: " + e.getMessage());
        }
    }

    /**
     * Copy file by file name which is searchable by class loader to specific destination
     * @param srFile eg: /Database/data.db
     * @param dtFile eg: D:/Database/data.db
     */
    public static void copyFileByClassLoaderPath(String srFile, String dtFile) {
        try {
            File destFile = new File(dtFile);
            InputStream in = CalendarMethod.class.getResourceAsStream(srFile);
            OutputStream out = new FileOutputStream(destFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            logger.info("[copyFileByInputStream]: file copied to " + dtFile);
        } catch (FileNotFoundException ex) {
            logger.debug("[copyFileByInputStream]: " + ex.getMessage() + " in the specified directory.");
            System.exit(0);
        } catch (IOException e) {
            logger.debug("[copyFileByInputStream]: " + e.getMessage());
        }
    }

    /**
     * Copy directory to another destination
     * @param srcPath eg : "C:/Database"
     * @param dstPath eg : "D:/Database"
     */
    public static void copyDirectory(File srcPath, File dstPath) {
        if (srcPath.isDirectory()) {
            if (!dstPath.exists()) {
                dstPath.mkdir();
            }
            String files[] = srcPath.list();
            for (int i = 0; i < files.length; i++) {
                copyDirectory(new File(srcPath, files[i]), new File(dstPath, files[i]));
            }
        } else {
            if (!srcPath.exists()) {
                logger.debug("[copyDirectory]: Source File or directory does not exist.");
                System.exit(0);
            } else {
                try {
                    InputStream in = new FileInputStream(srcPath);
                    OutputStream out = new FileOutputStream(dstPath);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                } catch (IOException e) {
                    logger.debug("[copyDirectory]: " + e.getMessage());
                }
            }
        }
        logger.info("[copyDirectory]: File copied to " + dstPath);
    }

    /**
     * Create new directory by mkdir
     * @param dtFile eg: C:/Database/data/
     */
    public static void createDirectory(String dtFile) {
        File destFile = new File(dtFile);
        if (destFile.exists() == false) {
            destFile.mkdirs();
            logger.info("[Database folder created]: " + dtFile);
        }
    }

    /**
     * Get database path from properties file, default is user home directory
     * @return database path
     */
    public static String getDBPath() {
        String dbPath = System.getProperty("user.home") + "/nageIT/Database/";
        if (!ResourceBundle.getBundle("config").getString("database.folder").equals("")) dbPath = ResourceBundle.getBundle("label").getString("database.folder");
        return dbPath;
    }

    /**
     * Check file existency
     * @param fileName
     * @return boolean
     */
    public static boolean isFileExist(String fileName) {
        File destFile = new File(fileName);
        return destFile.exists();
    }

    /**
     * Initialize database file if not exist
     * @param dbPath
     */
    public static void createDatabase(String dbPath) {
        createDirectory(dbPath);
        String[] sourceFiles = { "Calendar.data.db", "Calendar.index.db", "Calendar.trace.db" };
        for (String sourceName : sourceFiles) {
            CalendarMethod.copyFileByClassLoaderPath("/Database/" + sourceName, dbPath + sourceName);
        }
    }

    public static boolean runDatabaseScript() {
        RunScript runScript = new RunScript();
        return runScript.execute();
    }

    public static void main(String[] args) throws URISyntaxException {
        System.out.println("= " + CalendarMethod.getDBPath() + "Calendar.data.db");
        System.out.println("=== " + ClassLoader.getSystemResource("Database"));
    }
}
