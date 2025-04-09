package csiebug.util.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;

/**
 * @author George_Tsai
 * @version 2010/7/30
 */
public class ZipUtility {

    private static final int BUFFER = 2048;

    private static final String SEPARATOR = System.getProperty("file.separator");

    private static Logger logger = Logger.getLogger(ZipUtility.class);

    private long totalSpace = 0;

    private long finishSpace = 0;

    private String currentMessage = "";

    private boolean printToConsoleFlag;

    private ZipMonitor zipMonitor;

    public ZipUtility() {
        printToConsoleFlag = true;
    }

    public ZipUtility(boolean printToConsoleFlag) {
        this.printToConsoleFlag = printToConsoleFlag;
    }

    /**
	 * 設定印出壓縮/解壓縮的訊息到console上
	 * @param printToConsoleFlag
	 */
    public void setPrintToConsole(boolean printToConsoleFlag) {
        this.printToConsoleFlag = printToConsoleFlag;
    }

    public void setMonitor(ZipMonitor zipMonitor) {
        this.zipMonitor = zipMonitor;
        this.zipMonitor.setZipUtility(this);
    }

    public String getCurrentMessage() {
        return currentMessage;
    }

    public long getFinishSpace() {
        return finishSpace;
    }

    public long getTotalSpace() {
        return totalSpace;
    }

    public long getZipPercetage() {
        return finishSpace * 100 / totalSpace;
    }

    /**
	 * 將多個檔案壓縮成destinationZipFile
	 * @param destinationZipFile
	 * @param files
	 * @throws IOException
	 */
    public void zip(File destinationZipFile, File[] files) throws IOException {
        if (files.length == 1 && files[0].isDirectory()) {
            zip(destinationZipFile, files[0].getName(), files[0].listFiles());
        } else {
            zip(destinationZipFile, "", files);
        }
    }

    private void addFile(ZipOutputStream out, String basePath, File file) throws IOException {
        BufferedInputStream origin = null;
        FileInputStream fi = null;
        if (!basePath.trim().equals("")) {
            basePath = basePath + SEPARATOR;
        }
        try {
            byte data[] = new byte[BUFFER];
            fi = new FileInputStream(file);
            origin = new BufferedInputStream(fi, BUFFER);
            ZipEntry entry = new ZipEntry(basePath + file.getName());
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            currentMessage = "Add File: " + file;
            finishSpace = finishSpace + file.getTotalSpace();
            if (zipMonitor != null) {
                zipMonitor.printMessage();
            }
            if (printToConsoleFlag) {
                System.out.println(currentMessage);
                System.out.println("complete: " + getZipPercetage() + "%");
            }
        } finally {
            if (origin != null) {
                origin.close();
            }
            if (fi != null) {
                fi.close();
            }
        }
    }

    private void addFolder(ZipOutputStream out, String basePath, File dir) throws IOException {
        File[] files = dir.listFiles();
        String newBasePath;
        if (basePath.trim().equals("")) {
            newBasePath = dir.getName();
        } else {
            newBasePath = basePath + SEPARATOR + dir.getName();
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                addFolder(out, newBasePath, files[i]);
            } else {
                addFile(out, newBasePath, files[i]);
            }
        }
        if (files.length == 0) {
            ZipEntry entry = new ZipEntry(newBasePath + SEPARATOR);
            out.putNextEntry(entry);
        }
        currentMessage = "Add Folder: " + dir;
        if (zipMonitor != null) {
            zipMonitor.printMessage();
        }
        if (printToConsoleFlag) {
            System.out.println(currentMessage);
        }
    }

    private long getTotalSpace(File[] files) {
        long space = 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                space = space + getTotalSpace(files[i].listFiles());
            } else {
                space = space + files[i].getTotalSpace();
            }
        }
        return space;
    }

    private void zip(File destinationZipFile, String basePath, File[] files) throws IOException {
        FileOutputStream dest = null;
        ZipOutputStream out = null;
        try {
            totalSpace = getTotalSpace(files);
            finishSpace = 0;
            dest = new FileOutputStream(destinationZipFile);
            out = new ZipOutputStream(new BufferedOutputStream(dest));
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    addFolder(out, basePath, files[i]);
                } else {
                    addFile(out, basePath, files[i]);
                }
            }
        } finally {
            if (out != null) {
                out.close();
            }
            if (dest != null) {
                dest.close();
            }
        }
    }

    private long getTotalSpace(Enumeration<? extends ZipEntry> e) {
        long space = 0;
        while (e.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) e.nextElement();
            space = space + entry.getSize();
        }
        return space;
    }

    /**
	 * 解壓縮
	 * @param zipFile
	 * @throws ZipException
	 * @throws IOException
	 */
    public void unzip(File zipFile) throws ZipException, IOException {
        unzip(zipFile, zipFile.getParent());
    }

    /**
	 * 解壓縮到basePath
	 * @param zipFile
	 * @throws ZipException
	 * @throws IOException
	 */
    public void unzip(File zipFile, String basePath) throws ZipException, IOException {
        BufferedOutputStream dest = null;
        BufferedInputStream is = null;
        FileOutputStream fos = null;
        try {
            ZipFile zipfile = new ZipFile(zipFile);
            totalSpace = getTotalSpace(zipfile.entries());
            finishSpace = 0;
            Enumeration<? extends ZipEntry> e = zipfile.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                is = new BufferedInputStream(zipfile.getInputStream(entry));
                int count;
                byte data[] = new byte[BUFFER];
                File file = new File(basePath + SEPARATOR + entry.getName());
                if (!file.exists()) {
                    if (entry.getName().endsWith(SEPARATOR)) {
                        boolean flag = file.mkdirs();
                        if (!flag) {
                            logger.info("Can not create all necessary parent directories!");
                        }
                    } else {
                        File dir = file.getParentFile();
                        if (!dir.exists()) {
                            boolean flag = dir.mkdirs();
                            if (!flag) {
                                logger.info("Can not create all necessary parent directories!");
                            }
                        }
                        boolean flag = file.createNewFile();
                        if (!flag) {
                            logger.info("The named file already exists!");
                        }
                        fos = new FileOutputStream(file);
                        dest = new BufferedOutputStream(fos, BUFFER);
                        while ((count = is.read(data, 0, BUFFER)) != -1) {
                            dest.write(data, 0, count);
                        }
                        dest.flush();
                        dest.close();
                        is.close();
                        fos.close();
                    }
                } else {
                    if (!entry.getName().endsWith(SEPARATOR)) {
                        fos = new FileOutputStream(file);
                        dest = new BufferedOutputStream(fos, BUFFER);
                        while ((count = is.read(data, 0, BUFFER)) != -1) {
                            dest.write(data, 0, count);
                        }
                        dest.flush();
                        dest.close();
                        is.close();
                        fos.close();
                    }
                }
                currentMessage = "Extracting: " + entry;
                finishSpace = finishSpace + entry.getSize();
                if (zipMonitor != null) {
                    zipMonitor.printMessage();
                }
                if (printToConsoleFlag) {
                    System.out.println(currentMessage);
                    System.out.println("complete: " + getZipPercetage() + "%");
                }
            }
            zipfile.close();
        } finally {
            if (dest != null) {
                dest.close();
            }
            if (is != null) {
                is.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }
}
