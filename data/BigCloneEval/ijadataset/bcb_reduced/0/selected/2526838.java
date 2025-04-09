package org.mitre.mrald.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import org.mitre.mrald.util.Config;
import org.mitre.mrald.util.MraldException;

/**
 *  Provides several utilities for the Administration of an MRALD installation.
 *
 *@author     jchoyt
 *@created    October 14, 2003
 */
public class AdminUtil {

    public static enum BackupType {

        CONFIG, FORMS, JARS, LOGS
    }

    static int basePathLength = 0;

    /**
     *  Removes the logs files passed in
     *
     *@param  pc                    Description of the Parameter
     *@exception  ServletException  Description of the Exception
     *@exception  IOException       Description of the Exception
     */
    public static void removeErrFiles(PageContext pc) throws ServletException, IOException {
        String key;
        ServletRequest req = pc.getRequest();
        for (Enumeration e = req.getParameterNames(); e.hasMoreElements(); ) {
            key = (String) e.nextElement();
            if (key.startsWith("errFileDelete")) {
                File toDelete = new File(Config.getProperty("LOGPATH"), req.getParameter(key));
                toDelete.delete();
            }
        }
        ((HttpServletResponse) pc.getResponse()).sendRedirect(Config.getProperty("AdminUrl"));
    }

    /**
     *  Renames the LOGFILE and DBLOGFILE (as contained in the current
     *  config.preoperties) files to prepend the current date in yyyymmdd
     *  format.
     *
     *@param  pc                    Description of the Parameter
     *@exception  MraldException    Description of the Exception
     *@exception  ServletException  Description of the Exception
     *@exception  IOException       Description of the Exception
     */
    public static void rotateLogs(PageContext pc) throws MraldException, ServletException, IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String prefix = formatter.format(new Date());
        File logfile = new File(Config.getProperty("LOGPATH"), Config.getProperty("LOGFILE"));
        boolean successfulRename;
        if (logfile.exists()) {
            successfulRename = logfile.renameTo(new File(Config.getProperty("LOGPATH"), prefix + Config.getProperty("LOGFILE")));
            if (!successfulRename) {
                throw new MraldException("Couldn't rename " + logfile.getAbsolutePath());
            }
        }
        logfile = new File(Config.getProperty("LOGPATH"), Config.getProperty("DBLOGFILE"));
        if (logfile.exists()) {
            successfulRename = logfile.renameTo(new File(Config.getProperty("LOGPATH"), prefix + Config.getProperty("DBLOGFILE")));
            if (!successfulRename) {
                throw new MraldException("Couldn't rename " + logfile.getAbsolutePath());
            }
        }
        ((HttpServletResponse) pc.getResponse()).sendRedirect(Config.getProperty("AdminUrl"));
    }

    /**
     * Creates backup zip files in the root directory of the web application.  These
     * files will be accessible via http.
     */
    public static void createBackups() {
        Date date;
        try {
            File installDT = new File(Config.getProperty("BasePath"), "logs/installDT.txt");
            if (!installDT.exists()) {
                throw new RuntimeException(installDT.getPath() + " does not exist - it is not possible to know what files to backup");
            }
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(installDT));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            String line = reader.readLine();
            while (line.equals(Config.EMPTY_STR)) {
                line = reader.readLine();
            }
            String dateToBeat = line;
            DateFormat df = DateFormat.getDateTimeInstance();
            date = df.parse(dateToBeat);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        String basePath = Config.getProperty("BasePath");
        if (basePath.endsWith("/") || basePath.endsWith("\\")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        basePathLength = basePath.length();
        File dir = new File(basePath);
        String zipFileName = getBackupFile(BackupType.CONFIG);
        List<File> newerFiles = getFiles(dir, new ConfigFilter(), date);
        createZipFile(zipFileName, newerFiles);
        zipFileName = getBackupFile(BackupType.FORMS);
        newerFiles = getFiles(dir, new FormsFilter(), date);
        createZipFile(zipFileName, newerFiles);
        zipFileName = getBackupFile(BackupType.JARS);
        newerFiles = getFiles(dir, new JarsFilter(), date);
        createZipFile(zipFileName, newerFiles);
        zipFileName = getBackupFile(BackupType.LOGS);
        newerFiles = getFiles(dir, new LogsFilter(), date);
        createZipFile(zipFileName, newerFiles);
    }

    public static String getBackupFile(BackupType type) {
        String basePath = Config.getProperty("BasePath");
        switch(type) {
            case CONFIG:
                return basePath + File.separator + "config.zip";
            case FORMS:
                return basePath + File.separator + "forms.zip";
            case JARS:
                return basePath + File.separator + "jars.zip";
            case LOGS:
                return basePath + File.separator + "logs.zip";
            default:
                return "";
        }
    }

    protected static List<File> getFiles(File dir, FilenameFilter filter, Date date) {
        List<File> newerFiles = new ArrayList<File>();
        if (!dir.isDirectory()) {
            throw new RuntimeException(dir + " is not a directory.  Bad, program!  BAD!!");
        }
        File[] children = dir.listFiles(filter);
        for (int i = 0; i < children.length; i++) {
            if (children[i].isDirectory()) {
                newerFiles.addAll(getFiles(children[i], filter, date));
            } else if (date.before(new Date(children[i].lastModified())) && !children[i].getName().endsWith(".lck")) {
                newerFiles.add(children[i]);
            }
        }
        return newerFiles;
    }

    protected static void createZipFile(String zipName, List<File> files) {
        byte[] buf = new byte[1024];
        try {
            if (files.size() > 0) {
                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipName));
                int len;
                for (File file : files) {
                    FileInputStream in = new FileInputStream(file);
                    ZipEntry entry = new ZipEntry(file.getPath());
                    out.putNextEntry(entry);
                    while ((len = in.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                    in.close();
                }
                out.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static String getRelativePath(File file) {
        String ret = file.getPath().substring(basePathLength);
        ret = ret.replaceAll("^/*", "");
        ret = ret.replaceAll("^\\*", "");
        return ret;
    }
}

class ConfigFilter implements FilenameFilter {

    public boolean accept(File dir, String name) {
        if (dir.getPath().length() < AdminUtil.basePathLength) {
            return false;
        }
        String dirPath = AdminUtil.getRelativePath(dir);
        boolean ret = false;
        ret = ret || name.equals("WEB-INF") || name.equals("props");
        ret = ret || dirPath.contains("WEB-INF/props");
        ret = ret || name.startsWith("admin");
        return ret;
    }
}

class JarsFilter implements FilenameFilter {

    public boolean accept(File dir, String name) {
        if (dir.getPath().length() < AdminUtil.basePathLength) {
            return false;
        }
        String dirPath = AdminUtil.getRelativePath(dir);
        boolean ret = false;
        ret = ret || name.equals("WEB-INF") || name.equals("lib");
        ret = ret || dirPath.contains("WEB-INF/lib");
        ret = ret || dirPath.contains("WEB-INF\\lib");
        return ret;
    }
}

class LogsFilter implements FilenameFilter {

    public boolean accept(File dir, String name) {
        if (dir.getPath().length() < AdminUtil.basePathLength) {
            return false;
        }
        String dirPath = AdminUtil.getRelativePath(dir);
        boolean ret = false;
        ret = ret || (dirPath.startsWith("logs") || name.equals("logs")) && !name.endsWith(".err");
        return ret;
    }
}

class FormsFilter implements FilenameFilter {

    public boolean accept(File dir, String name) {
        if (dir.getPath().length() < AdminUtil.basePathLength) {
            return false;
        }
        String dirPath = AdminUtil.getRelativePath(dir);
        boolean ret = false;
        ret = ret || name.equals("custom") || name.equals("forms");
        ret = ret || dirPath.startsWith("custom");
        ret = ret || dirPath.startsWith("forms");
        return ret;
    }
}
