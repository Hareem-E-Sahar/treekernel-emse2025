package org.sac.crosspather.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.sac.crosspather.common.listener.WorkListener;
import org.sac.crosspather.common.log.AppLogger;

public class Compressor implements Runnable {

    private static AppLogger logger = new AppLogger(Compressor.class.getName());

    private static final int BUFFER = 1000000;

    private static final String ZIP_SEPERATOR = "\n";

    private List<String> fileList;

    private String sourceDirectory;

    private boolean fileListPopulated = false;

    WorkListener jobListener;

    public Compressor(WorkListener jobListener) {
        this.jobListener = jobListener;
    }

    public Compressor() {
        jobListener = new WorkListener() {

            public void jobFailed(Throwable t) {
            }

            public void updateMainStatus(String status) {
            }

            public void updateSubStatus(String status) {
            }

            public void appendMainStatus(String status) {
            }

            public void appendSubStatus(String status) {
            }

            public void updateRequester(String requesterCode) {
            }

            public void updateServerStatus(int status) {
            }

            public void activateSleepMode(long sleepDuration) {
            }

            public void changeMode(int mode) {
            }
        };
    }

    public void unzip(InputStream in, String destDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(in));
        ZipEntry entry;
        logger.doLog(AppLogger.DEBUG, "Extracting to " + destDirectory, null);
        BufferedOutputStream dest = null;
        while ((entry = zis.getNextEntry()) != null) {
            logger.doLog(AppLogger.FINEST, "Extracting: " + entry, null);
            jobListener.updateSubStatus("Extracting: " + entry);
            int count;
            byte data[] = new byte[BUFFER];
            if (entry.getName().endsWith("/")) {
                File file = new File(destDirectory + entry.getName());
                file.mkdir();
            } else {
                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(destDirectory + entry.getName());
                } catch (FileNotFoundException e) {
                    File parent = new File(destDirectory + entry.getName()).getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    fos = new FileOutputStream(destDirectory + entry.getName());
                }
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
        }
        logger.doLog(AppLogger.DEBUG, "Extraction complete.", null);
    }

    public void zip(final String sourceDirectory, OutputStream dest) throws FileNotFoundException, IOException {
        logger.doLog(AppLogger.DEBUG, "Starting compression...", null);
        this.sourceDirectory = sourceDirectory;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        fileList = new ArrayList<String>();
        fileListPopulated = false;
        boolean processingComplete = false;
        Thread thread = new Thread(this);
        thread.start();
        Object[] files = null;
        while (!processingComplete) {
            synchronized (fileList) {
                if (fileList.size() == 0) {
                    logger.doLog(AppLogger.DEBUG, "Sleeping for 1 second", null);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        logger.doLog(AppLogger.DEBUG, "Thread interrupted. Resuming.", null);
                    }
                } else {
                    processingComplete = fileListPopulated;
                    files = fileList.toArray();
                    fileList.clear();
                }
            }
            if (files != null && files.length > 0) {
                logger.doLog(AppLogger.DEBUG, "Compressing " + files.length + " files (and empty directories)", null);
                byte[] tmpBuf = new byte[BUFFER];
                for (int i = 0; i < files.length; i++) {
                    String fullFileName = (String) files[i];
                    ZipEntry entry = new ZipEntry(fullFileName.substring(new File(sourceDirectory).getAbsolutePath().length()));
                    out.putNextEntry(entry);
                    if (new File(fullFileName).isFile()) {
                        jobListener.updateSubStatus("Compressing " + fullFileName + "...");
                        FileInputStream in = new FileInputStream(fullFileName);
                        int len;
                        try {
                            while ((len = in.read(tmpBuf)) > 0) {
                                out.write(tmpBuf, 0, len);
                            }
                        } catch (IOException e) {
                            logger.doLog(AppLogger.WARN, "Exception while zipping " + fullFileName, e);
                            if ("The process cannot access the file because another process has locked a portion of the file".equals(e.getMessage())) {
                                logger.doLog(AppLogger.INFO, "Another process has locked this file for modification. If the file is updated while it is being compressed, the entire archive may be corrupt." + "\nContinuing.", null);
                            }
                        }
                        in.close();
                    }
                    out.closeEntry();
                }
                out.flush();
                files = null;
            }
        }
        out.close();
        logger.doLog(AppLogger.DEBUG, "Compression complete.", null);
        jobListener.updateMainStatus("Compression complete.");
    }

    /**
	 * All files and empty directories under path, are recursively added into list.<BR/>
	 * This list is intended to be used for compression (with java.util.zip)<BR/>
	 * Note: The list starts with the seperator.
	 * @param path
	 * @param list
	 */
    private static void getFileList(String path, StringBuffer list) {
        File current = new File(path);
        if (current.isDirectory()) {
            File[] files = current.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    if (file.list().length == 0) {
                        list.append(ZIP_SEPERATOR).append(file.getAbsolutePath() + "/");
                    } else {
                        getFileList(file.getAbsolutePath(), list);
                    }
                } else {
                    list.append(ZIP_SEPERATOR).append(file.getAbsolutePath());
                }
            }
        }
    }

    private void populateFileList(String path) {
        File current = new File(path);
        if (current.isDirectory()) {
            File[] files = current.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    if (file.listFiles().length == 0) {
                        synchronized (fileList) {
                            fileList.add(file.getAbsolutePath() + "/");
                        }
                    } else {
                        populateFileList(file.getAbsolutePath());
                    }
                } else {
                    jobListener.updateSubStatus("Adding file " + file.getName() + " to " + fileList.size() + " files.");
                    synchronized (fileList) {
                        fileList.add(file.getAbsolutePath());
                    }
                }
            }
        }
    }

    public void run() {
        long startTime = System.currentTimeMillis();
        populateFileList(sourceDirectory);
        fileListPopulated = true;
        long endTime1 = System.currentTimeMillis();
        logger.doLog(AppLogger.DEBUG, "Time for getting file list: " + (endTime1 - startTime) + "ms", null);
    }
}
