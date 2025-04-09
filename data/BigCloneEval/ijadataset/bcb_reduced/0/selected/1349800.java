package com.ibm.tuningfork.infra.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;
import com.ibm.tuningfork.infra.Logging;

/**
 * A collection of file-related utility functions.
 */
public class FileUtility {

    protected static String cacheDirectory;

    protected static String socketDirectory;

    protected static String importDirectory;

    protected static String sharedDirectory;

    protected static String userSpecificDirectory;

    private static final String tempFilePrefix = "temp-";

    private static final long OLD_TEMP_THRESHOLD_HOURS = 24;

    private static final long MILLIS_PER_HOUR = 1000 * 60 * 60;

    public static boolean cacheAvailable() {
        return cacheDirectory != null;
    }

    public static boolean socketAvailable() {
        return socketDirectory != null;
    }

    public static boolean importAvailable() {
        return importDirectory != null;
    }

    public static boolean sharedAvailable() {
        return sharedDirectory != null;
    }

    public static String toUserIndependentIfPossible(String s) {
        return userSpecificDirectory != null && s.startsWith(userSpecificDirectory) ? s.substring(userSpecificDirectory.length()) : s;
    }

    public static String fromUserIndependentIfPossible(String s) {
        String otherSeparator = java.io.File.separator.equals("/") ? "\\" : "/";
        String withMySeparators = s.replace(otherSeparator, java.io.File.separator);
        return s.startsWith(java.io.File.separator) ? withMySeparators : userSpecificDirectory == null ? withMySeparators : userSpecificDirectory + withMySeparators;
    }

    public static File getTempFile() {
        if (!cacheAvailable()) {
            return null;
        }
        while (true) {
            long timeMillis = System.currentTimeMillis();
            long timeNano = System.nanoTime();
            long microDelta = (timeNano / 1000) % 1000000;
            Date date = new Date(timeMillis);
            String dateStr = date.toString().replace(' ', '_');
            dateStr = dateStr.toString().replace(':', '_');
            String fileName = tempFilePrefix + dateStr + "-" + microDelta + ".data";
            File file = getCacheFile(fileName);
            if (!file.exists()) {
                return file;
            }
        }
    }

    public static File getCacheFile(String fileName) {
        if (!cacheAvailable()) {
            return null;
        }
        String fullFileName = cacheDirectory + java.io.File.separator + fileName;
        return new File(fullFileName);
    }

    public static File getSocketFile(String fileName) {
        if (!socketAvailable()) {
            return null;
        }
        String fullFileName = socketDirectory + java.io.File.separator + fileName;
        return new File(fullFileName);
    }

    public static File getSharedTraceFile(String fileName) {
        if (!sharedAvailable()) {
            return null;
        }
        String fullFileName = sharedDirectory + java.io.File.separator + fileName;
        return new File(fullFileName);
    }

    public static File getImportFile(String fileName) {
        if (!importAvailable()) {
            return null;
        }
        String fullFileName = importDirectory + java.io.File.separator + fileName;
        return new File(fullFileName);
    }

    public static void setUserSpecificDirectory(String dir) {
        userSpecificDirectory = dir.endsWith(java.io.File.separator) ? dir : dir + java.io.File.separator;
    }

    public static void setSocketDirectory(String dir) {
        socketDirectory = dir;
        File d = new File(socketDirectory);
        try {
            d.mkdirs();
        } catch (SecurityException se) {
        }
        if (!(d.exists() && d.isDirectory())) {
            Logging.errorln(socketDirectory + " is not a directory or does not exist.");
        }
    }

    public static void setSharedDirectory(String dir) {
        sharedDirectory = dir;
        File d = new File(sharedDirectory);
        try {
            d.mkdirs();
        } catch (SecurityException se) {
        }
        if (!(d.exists() && d.isDirectory())) {
            Logging.errorln(sharedDirectory + " is not a directory or does not exist.");
        }
    }

    private static void cleanCache() {
        File cache = new File(cacheDirectory);
        File[] files = cache.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String filename) {
                return filename.startsWith(tempFilePrefix);
            }
        });
        Logging.msgln("There are " + files.length + " temp files in the cache directory.");
        long nowTime = System.currentTimeMillis();
        int deleted = 0;
        for (File f : files) {
            long modTime = f.lastModified();
            if (modTime == 0) {
                continue;
            }
            long diffTime = nowTime - modTime;
            if (diffTime > OLD_TEMP_THRESHOLD_HOURS * MILLIS_PER_HOUR) {
                if (f.delete()) {
                    deleted++;
                }
            }
        }
        Logging.msgln("Deleted " + deleted + " old temp files in the cache directory.");
    }

    public static void setCacheDirectory(String dir) {
        cacheDirectory = dir;
        File d = new File(cacheDirectory);
        try {
            d.mkdirs();
        } catch (SecurityException se) {
        }
        if (!(d.exists() && d.isDirectory())) {
            Logging.errorln(cacheDirectory + " is not a directory or does not exist.");
        } else {
            new Thread() {

                public void run() {
                    cleanCache();
                }
            }.start();
        }
    }

    public static void setImportDirectory(String dir) {
        importDirectory = dir;
        File d = new File(importDirectory);
        try {
            d.mkdirs();
        } catch (SecurityException se) {
        }
        if (!(d.exists() && d.isDirectory())) {
            Logging.errorln(importDirectory + " is not a directory or does not exist.");
        }
    }

    public static void eraseDirectory(String dir) {
        File d = new File(dir);
        try {
            File[] files = d.listFiles();
            for (File f : files) {
                f.delete();
            }
            d.delete();
        } catch (SecurityException se) {
        }
    }

    public static void eraseCache() {
        eraseDirectory(cacheDirectory);
    }

    public static long sampledCRC(File file) throws FileNotFoundException {
        long fileSize = file.length();
        RandomAccessFile fs = new RandomAccessFile(file, "r");
        long startTime = System.currentTimeMillis();
        CRC32 crc = new CRC32();
        for (int i = 7; i >= 0; i--) {
            crc.update((int) (fileSize >> (8 * i)));
        }
        final int CHUNK_SIZE = 4096;
        final int MINIMUM_CHUNK_SKIP = 1;
        final int MAXIMUM_CHUNK_SKIP = 1000;
        final double CHUNK_SKIP_MULTIPLIER = 1.2;
        int totalRead = 0;
        byte[] data = new byte[CHUNK_SIZE];
        for (int position = 0, chunkSkip = 0; ; ) {
            try {
                fs.seek(position);
                int read = fs.read(data, 0, data.length);
                if (read == -1) {
                    break;
                }
                position += read + chunkSkip * CHUNK_SIZE;
                if (position > 64 << 10) {
                    if (chunkSkip == 0) {
                        chunkSkip = MINIMUM_CHUNK_SKIP;
                    } else {
                        chunkSkip = Math.min((int) Math.ceil(chunkSkip * CHUNK_SKIP_MULTIPLIER), MAXIMUM_CHUNK_SKIP);
                    }
                }
                totalRead += read;
                crc.update(data, 0, read);
            } catch (IOException ioe) {
            }
            if ((totalRead % (100 * CHUNK_SIZE)) == 0) {
                Logging.verboseln(1, "Processed " + (totalRead / 1024) + "KBytes.  CRC32 = " + crc.getValue());
            }
        }
        long endTime = System.currentTimeMillis();
        Logging.msgln("Sampled " + (totalRead / 1024) + " of " + (fileSize / 1024) + " KBytes. CRC took " + (endTime - startTime) + "ms.  CRC32 = " + crc.getValue());
        return crc.getValue();
    }

    public static File gunzip(File fileToBeDecompressed) {
        String compressedName = fileToBeDecompressed.getAbsolutePath();
        if (!compressedName.endsWith(".gz")) {
            return null;
        }
        String uncompressedName = compressedName.substring(0, compressedName.length() - 3);
        try {
            GZIPInputStream inStream = new GZIPInputStream(new FileInputStream(compressedName));
            OutputStream outStream = new FileOutputStream(uncompressedName);
            byte[] buf = new byte[65536];
            int len;
            while ((len = inStream.read(buf)) > 0) {
                outStream.write(buf, 0, len);
            }
            inStream.close();
            outStream.close();
            fileToBeDecompressed.delete();
        } catch (IOException e) {
            Logging.errorln("IOException occurred in FileUtility.gunzip: " + e);
            return null;
        }
        return new File(uncompressedName);
    }
}
