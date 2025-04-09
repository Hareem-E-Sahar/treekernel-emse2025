package org.fao.waicent.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileSystem {

    public FileSystem() {
    }

    public static void copyDirectory(File source, File dest) throws Exception {
        if (!dest.exists()) {
            dest.mkdirs();
        }
        if (source.exists() && dest.exists()) {
            String source_list[] = source.list();
            for (int i = 0; i < source_list.length; i++) {
                File sourceF = new File(source, source_list[i]);
                File destF = new File(dest, source_list[i]);
                if (sourceF.isDirectory()) {
                    if (!sourceF.exists()) {
                        sourceF.mkdirs();
                    }
                    if (destF.exists() && destF.isDirectory() || destF.mkdirs()) {
                        copyDirectory(sourceF, destF);
                    }
                } else {
                    copyFile(sourceF.getPath(), destF.getPath());
                }
            }
        }
    }

    public static void copyDirectory(File source, String dest) throws Exception {
        copyDirectory(source, new File(dest));
    }

    public static void copyDirectory(String source, File dest) throws Exception {
        copyDirectory(new File(source), dest);
    }

    public static void copyDirectory(String source, String dest) throws Exception {
        copyDirectory(new File(source), new File(dest));
    }

    public static void copyFile(File source, File target) throws Exception {
        if (target.isDirectory()) {
            target = new File(target, source.getName());
        }
        FileOutputStream fileoutputstream = new FileOutputStream(target);
        copyFileToStream(source, fileoutputstream);
        fileoutputstream.close();
    }

    public static void copyFile(File source, String targetfilename) throws Exception {
        copyFile(source, new File(targetfilename));
    }

    public static void copyFile(String sourcefilename, File dest) throws Exception {
        copyFile(new File(sourcefilename), dest);
    }

    public static void copyFile(String source, String dest) throws Exception {
        copyFile(new File(source), new File(dest));
    }

    public static void copyFileToStream(File source, OutputStream sourceoutputstream) throws Exception {
        FileInputStream fileinputstream = new FileInputStream(source);
        copyStreams(fileinputstream, sourceoutputstream);
        fileinputstream.close();
    }

    public static void copyFileToStream(String source, OutputStream outputstream) throws Exception {
        copyFileToStream(new File(source), outputstream);
    }

    public static void copyFileToZipStream(File zipsource, String name, File rel_path, ZipOutputStream zipoutputstream) throws Exception {
        if (zipsource.isDirectory()) {
            File zipfile = zipsource;
            String source_list[] = zipfile.list();
            for (int i = 0; i < source_list.length; i++) {
                String sourcefilename = source_list[i];
                File sourcefile = new File(zipfile, sourcefilename);
                String relativePathname = getRelativePathname(rel_path, sourcefile);
                copyFileToZipStream(sourcefile, relativePathname, rel_path, zipoutputstream);
            }
        } else {
            FileInputStream fileinputstream = new FileInputStream(zipsource);
            String s1 = name.replace(File.separatorChar, '/');
            ZipEntry zipentry = new ZipEntry(s1);
            zipentry.setSize(zipsource.length());
            zipoutputstream.putNextEntry(zipentry);
            copyStreams(fileinputstream, zipoutputstream);
            zipoutputstream.closeEntry();
            fileinputstream.close();
        }
    }

    public static void copyFileToZipStream(File file, String s, ZipOutputStream zipoutputstream) throws Exception {
        File file1 = null;
        copyFileToZipStream(file, s, file1, zipoutputstream);
    }

    public static void copyFileToZipStream(String s, ZipOutputStream zipoutputstream) throws Exception {
        File file = new File(s);
        copyFileToZipStream(file, file.getPath(), zipoutputstream);
    }

    public static void copyStreamToFile(InputStream inputstream, File file) throws Exception {
        FileOutputStream fileoutputstream = new FileOutputStream(file);
        copyStreams(inputstream, fileoutputstream);
        fileoutputstream.close();
    }

    public static void copyStreamToFile(InputStream inputstream, String s) throws Exception {
        copyStreamToFile(inputstream, new File(s));
    }

    public static void copyStreams(InputStream inputstream, OutputStream outputstream) throws Exception {
        int i = 50000;
        byte buffer[] = new byte[50000];
        for (int j = read(inputstream, buffer); j > 0; ) {
            outputstream.write(buffer, 0, j);
            j = read(inputstream, buffer);
            Thread.yield();
        }
    }

    public static void copyStreams(InputStream inputstream, OutputStream outputstream, long offs) throws Exception {
        int limit = 50000;
        byte buffer[] = new byte[limit];
        int offset = (int) offs;
        int capacity = offset;
        if (capacity > limit) {
            capacity = limit;
        }
        for (int got = read(inputstream, buffer, capacity); got > 0; ) {
            outputstream.write(buffer, 0, got);
            int percent = (int) (((offs - (long) offset) * 100L) / offs);
            offset -= got;
            int i = offset;
            if (i > limit) {
                i = limit;
            }
            got = read(inputstream, buffer, i);
            Thread.yield();
        }
    }

    public static long getCrc(File file) throws Exception {
        CRC32 crc32 = new CRC32();
        FileInputStream fileinputstream = new FileInputStream(file);
        int i = 50000;
        byte buffer[] = new byte[50000];
        long l = file.length();
        int j = (int) l;
        int k = j;
        if (k > 50000) {
            k = 50000;
        }
        for (int j1 = read(fileinputstream, buffer, k); j1 > 0; ) {
            crc32.update(buffer, 0, j1);
            int k1 = (int) (((l - (long) j) * 100L) / l);
            j -= j1;
            int i1 = j;
            if (i1 > 50000) {
                i1 = 50000;
            }
            j1 = read(fileinputstream, buffer, i1);
            Thread.yield();
        }
        fileinputstream.close();
        return crc32.getValue();
    }

    public static long getSpaceOccupiedBy(File file) {
        if (file.isDirectory()) {
            long total = 0;
            File lista[] = file.listFiles();
            for (int i = 0; i < lista.length; i++) {
                total += getSpaceOccupiedBy(lista[i]);
            }
            return total;
        } else {
            return file.length();
        }
    }

    public static String getRelativePathname(File path, File file) throws Exception {
        String s = file.getPath();
        if (path != null) {
            String s1 = path.getCanonicalPath();
            s = file.getCanonicalPath();
            if (s.startsWith(s1)) {
                s = s.substring(s1.length());
            }
            if (s.startsWith(File.separator)) {
                s = s.substring(File.separator.length());
            }
        }
        return s;
    }

    public static boolean isRootDirectory(File file) {
        return isRootDirectory(file.getPath());
    }

    public static boolean isRootDirectory(String s) {
        return File.separator.equals(s) || Utilities.isWindows() && s.endsWith(":\\");
    }

    public static int read(InputStream inputstream) throws IOException {
        int i;
        if (inputstream.available() > 0) {
            i = inputstream.read();
        } else {
            i = -1;
        }
        return i;
    }

    public static int read(InputStream inputstream, byte buffer[]) throws IOException {
        int i = inputstream.read(buffer);
        return i;
    }

    public static int read(InputStream inputstream, byte buffer[], int i) throws IOException {
        int j = inputstream.read(buffer, 0, i);
        return j;
    }

    public static boolean removeDirectory(File file) {
        boolean flag = true;
        if (file.isDirectory() && !isRootDirectory(file)) {
            flag = removeDirectoryContents(file);
            if (file.exists()) {
                flag = file.delete();
            }
        }
        return flag;
    }

    public static boolean removeDirectory(String s) {
        return removeDirectory(new File(s));
    }

    public static boolean removeDirectoryContents(File file) {
        boolean flag = false;
        if (file.isDirectory() && !isRootDirectory(file)) {
            flag = true;
            String as[] = file.list();
            for (int i = 0; i < as.length; i++) {
                File file1 = new File(file, as[i]);
                if (!file1.isDirectory() && !file1.delete()) {
                    flag = false;
                }
            }
            for (int j = 0; j < as.length; j++) {
                if (!as[j].equals(".") && !as[j].equals("..")) {
                    File file2 = new File(file, as[j]);
                    if (file2.isDirectory() && !removeDirectory(file2.getAbsolutePath())) {
                        flag = false;
                    }
                }
            }
            if (!file.delete()) {
                flag = false;
            }
        }
        return flag;
    }

    public static boolean removeDirectoryContents(String s) {
        return removeDirectoryContents(new File(s));
    }

    public static boolean removeFile(String s) {
        boolean flag;
        try {
            File file = new File(s);
            flag = file.delete();
        } catch (Exception _ex) {
            flag = false;
        }
        return flag;
    }

    public static String normalizeFileName(String s) {
        return normalizeFileName(s, File.separatorChar);
    }

    public static String normalizeFileName(String s, char c) {
        if (s == null) {
            throw new NullPointerException("fileName cannot be null");
        } else {
            return s.replace('/', c).replace('\\', c);
        }
    }

    public static String getParent(String s) {
        String s1 = normalizeFileName(s);
        if (s1.endsWith(File.separator)) {
            int i = s1.length();
            s1 = s1.substring(0, i - 1);
        }
        return (new File(s1)).getParent();
    }

    public static boolean comparePaths(String s, String s1) {
        if (s == null || s1 == null) {
            return false;
        }
        String c1 = canonizePath(s);
        String c2 = canonizePath(s1);
        if (!isFileSystemCaseSensitive) {
            return c1.equalsIgnoreCase(c2);
        } else {
            return c1.equals(c2);
        }
    }

    public static String canonizePath(String s) {
        String s1 = normalizeFileName(s);
        String s2 = File.separator + File.separator;
        for (int i = s1.lastIndexOf(s2); i != -1 && i != 0; i = s1.lastIndexOf(s2)) {
            s1 = s1.substring(0, i + 1) + s1.substring(i + 2);
        }
        boolean flag = true;
        while (flag) {
            flag = false;
            StringBuffer stringbuffer = new StringBuffer();
            for (int j = 0; j < s1.length(); j++) {
                if (s1.charAt(j) != File.separatorChar) {
                    stringbuffer.append(s1.charAt(j));
                } else if (s1.length() != j + 1 && s1.charAt(j + 1) == '.') {
                    if (s1.length() == j + 2 || s1.charAt(j + 2) == File.separatorChar) {
                        j++;
                    } else if (s1.charAt(j + 2) == '.') {
                        String s3 = stringbuffer.toString();
                        String s4 = getParent(s3);
                        if (s4 == null) {
                            s4 = s3;
                        }
                        if (s4.endsWith(File.separator)) {
                            s4 = s4.substring(0, s4.length() - 1);
                        }
                        stringbuffer = new StringBuffer(s4);
                        j += 2;
                        flag = true;
                    }
                } else {
                    stringbuffer.append(s1.charAt(j));
                }
            }
            s1 = stringbuffer.toString();
        }
        if (s1.endsWith(File.separator)) {
            s1 = s1.substring(0, s1.length() - 1);
        }
        return s1;
    }

    public static String createFileName(String s, String s1) {
        if (s == null) {
            s = "";
        }
        if (s1 == null) {
            return normalizeFileName(s);
        }
        s1 = normalizeFileName(s1);
        if (isAbsolute(s1)) {
            return s1;
        }
        s = normalizeFileName(s);
        if (s.endsWith(File.separator)) {
            return s + s1;
        } else {
            return s + File.separator + s1;
        }
    }

    public static boolean isAbsolute(String s) {
        s = normalizeFileName(s);
        File file = new File(s);
        return s.startsWith(File.separator) || file.isAbsolute();
    }

    private static final boolean isFileSystemCaseSensitive;

    public static final String systemLF;

    static {
        String s = System.getProperty("os.name");
        if (Utilities.isWindows() || Utilities.getOperatingSystem() == Utilities.OS_OS2) {
            systemLF = "\r\n";
            isFileSystemCaseSensitive = false;
        } else if (Utilities.getOperatingSystem() == Utilities.OS_OS400) {
            systemLF = "\n";
            isFileSystemCaseSensitive = false;
        } else {
            systemLF = "\n";
            isFileSystemCaseSensitive = true;
        }
    }
}
