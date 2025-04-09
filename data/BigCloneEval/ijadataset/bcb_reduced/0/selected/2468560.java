package org.cantaloop.tools.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.jar.Attributes;
import org.cantaloop.tools.misc.StringUtils;

public class ZipWriter {

    public static final int DEFAULT_BUFFER_SIZE = 1024;

    public static final int MAX_BUFFER_SIZE = 16 * 1024;

    public static final ProcessIndicator DEFAULT_PROCESS_INDICATOR = new DefaultProcessIndicator();

    public static final ProcessIndicator SILENT_PROCESS_INDICATOR = new SilentProcessIndicator();

    private static final StreamAndEntryProvider ZIP_STREAM_PROVIDER = new StreamAndEntryProvider() {

        public ZipEntry createEntry(String name, File sourceFile) {
            return new ZipEntry(name);
        }

        public ZipOutputStream createStream(OutputStream stream, File baseDir, List files) {
            return new ZipOutputStream(stream);
        }
    };

    private static final StreamAndEntryProvider JAR_STREAM_PROVIDER = new DefaultJarStreamAndEntryProvider();

    public static interface ProcessIndicator {

        public int getNotificationGranularity();

        public void startZipFile(File targetZipFile, File baseDir, List files);

        public void startFile(File targetZipFile, ZipEntry zipEntry, File file);

        public void progress(File targetZipFile, ZipEntry zipEntry, File file, int len);

        public void endFile(File targetZipFile, ZipEntry zipEntry, File file);

        public void endZipFile(File targetZipFile, File baseDir, List files);

        public void startDirectory(File targetZipFile, ZipEntry zipEntry, File dir);

        public void endDirectory(File targetZipFile, ZipEntry zipEntry, File dir);
    }

    public static interface StreamAndEntryProvider {

        public ZipOutputStream createStream(OutputStream stream, File baseDir, List files) throws IOException;

        public ZipEntry createEntry(String name, File sourceFile);
    }

    public static class SilentProcessIndicator implements ProcessIndicator {

        public void endDirectory(File targetZipFile, ZipEntry zipEntry, File dir) {
        }

        public void endFile(File targetZipFile, ZipEntry zipEntry, File file) {
        }

        public void endZipFile(File targetZipFile, File baseDir, List files) {
        }

        public int getNotificationGranularity() {
            return Integer.MAX_VALUE;
        }

        public void progress(File targetZipFile, ZipEntry zipEntry, File file, int len) {
        }

        public void startDirectory(File targetZipFile, ZipEntry zipEntry, File dir) {
        }

        public void startFile(File targetZipFile, ZipEntry zipEntry, File file) {
        }

        public void startZipFile(File targetZipFile, File baseDir, List files) {
        }
    }

    public static class DefaultJarStreamAndEntryProvider implements StreamAndEntryProvider {

        private String m_mainClass;

        private Manifest m_manifest;

        public DefaultJarStreamAndEntryProvider() {
        }

        public DefaultJarStreamAndEntryProvider(String mainClass) {
            m_mainClass = mainClass;
        }

        public DefaultJarStreamAndEntryProvider(Manifest manifest) {
            m_manifest = manifest;
        }

        public ZipEntry createEntry(String name, File sourceFile) {
            return new JarEntry(name);
        }

        public ZipOutputStream createStream(OutputStream stream, File baseDir, List files) throws IOException {
            Manifest m;
            if (m_manifest != null) {
                m = m_manifest;
            } else {
                m = new Manifest();
                m.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
                if (m_mainClass != null) {
                    m.getMainAttributes().put(Attributes.Name.MAIN_CLASS, m_mainClass);
                }
            }
            return new JarOutputStream(stream, m);
        }
    }

    public static class DefaultProcessIndicator implements ProcessIndicator {

        private static class ZipEntryDescriptor {

            long totalSize;

            long writtenBytes;

            String lastStringScrubber = "";
        }

        private Map m_tgtFileToZedMap = new Hashtable();

        public void endFile(File targetZipFile, ZipEntry zipEntry, File file) {
            m_tgtFileToZedMap.remove(targetZipFile);
            System.out.println();
        }

        public void endZipFile(File targetZipFile, File baseDir, List files) {
        }

        public int getNotificationGranularity() {
            return -1;
        }

        public void progress(File targetZipFile, ZipEntry zipEntry, File file, int len) {
            ZipEntryDescriptor zed = (ZipEntryDescriptor) m_tgtFileToZedMap.get(targetZipFile);
            zed.writtenBytes += len;
            String written = "" + zed.writtenBytes;
            String total = "" + zed.totalSize;
            long percent = (zed.writtenBytes * 100) / zed.totalSize;
            String percentStr = StringUtils.padLeft(3, ' ', "" + percent);
            String statusStr = percentStr + "% (" + written + "/" + total + ")";
            System.out.print(zed.lastStringScrubber);
            System.out.print(statusStr);
            System.out.flush();
            zed.lastStringScrubber = StringUtils.fill('\b', statusStr.length());
        }

        public void startDirectory(File targetZipFile, ZipEntry zipEntry, File dir) {
        }

        public void endDirectory(File targetZipFile, ZipEntry zipEntry, File dir) {
        }

        public void startFile(File targetZipFile, ZipEntry zipEntry, File file) {
            String fullName = zipEntry.getName();
            String name = fullName;
            if (fullName.length() > 40) {
                name = "..." + StringUtils.lastXCharacters(fullName, 37);
            }
            String fill = StringUtils.fill(' ', 40 - name.length());
            System.out.print("inflating " + name + fill + "  ");
            System.out.flush();
            ZipEntryDescriptor zed = new ZipEntryDescriptor();
            zed.totalSize = file.length();
            zed.writtenBytes = 0;
            m_tgtFileToZedMap.put(targetZipFile, zed);
        }

        public void startZipFile(File targetZipFile, File baseDir, List files) {
        }
    }

    public static void writeZip(File targetFile, File baseDir, List files) throws IOException {
        writeZip(targetFile, baseDir, files, ZIP_STREAM_PROVIDER, DEFAULT_PROCESS_INDICATOR);
    }

    public static void write(File targetFile, File baseDir, List files, StreamAndEntryProvider sp) throws IOException {
        writeZip(targetFile, baseDir, files, sp, DEFAULT_PROCESS_INDICATOR);
    }

    public static void write(File targetFile, File baseDir, List files, StreamAndEntryProvider sp, ProcessIndicator pi) throws IOException {
        writeZip(targetFile, baseDir, files, sp, pi);
    }

    public static void writeJar(File targetFile, File baseDir, List files) throws IOException {
        writeZip(targetFile, baseDir, files, JAR_STREAM_PROVIDER, DEFAULT_PROCESS_INDICATOR);
    }

    public static void writeZip(File targetFile, File baseDir, List files, StreamAndEntryProvider pr, ProcessIndicator pi) throws IOException {
        ZipOutputStream jos = pr.createStream(new FileOutputStream(targetFile), baseDir, files);
        String baseDirPath = baseDir.getCanonicalPath();
        for (Iterator it = files.iterator(); it.hasNext(); ) {
            File classFile = (File) it.next();
            if (!classFile.exists()) {
                throw new FileNotFoundException(classFile.getAbsolutePath());
            }
            String fullFilePath = classFile.getCanonicalPath();
            if (!fullFilePath.startsWith(baseDirPath)) {
                throw new IllegalArgumentException("Expected file '" + fullFilePath + "' to be " + "located under " + baseDir.getAbsolutePath());
            }
            String tmpFileName = fullFilePath.substring(baseDirPath.length() + 1);
            String fileName = tmpFileName.replace(File.separatorChar, '/');
            if (classFile.isFile()) {
                ZipEntry entry = pr.createEntry(fileName, classFile);
                entry.setTime(classFile.lastModified());
                jos.putNextEntry(entry);
                pi.startFile(targetFile, entry, classFile);
                int notificationGranularity = pi.getNotificationGranularity();
                if (notificationGranularity < 1 || notificationGranularity > MAX_BUFFER_SIZE) notificationGranularity = DEFAULT_BUFFER_SIZE;
                FileInputStream fis = new FileInputStream(classFile);
                byte[] buffer = new byte[notificationGranularity];
                int i = 0;
                while (true) {
                    int len = fis.read(buffer);
                    if (len < 1) break;
                    jos.write(buffer, 0, len);
                    pi.progress(targetFile, entry, classFile, len);
                }
                fis.close();
                jos.closeEntry();
                pi.endFile(targetFile, entry, classFile);
            } else {
                if (!classFile.isDirectory()) {
                    throw new IOException("Ups. File-Object that is neither file nor directory? " + classFile.getAbsolutePath());
                }
                ZipEntry entry = pr.createEntry(fileName + "/", classFile);
                jos.putNextEntry(entry);
                pi.startDirectory(targetFile, entry, classFile);
                pi.endDirectory(targetFile, entry, classFile);
                jos.closeEntry();
            }
        }
        pi.endZipFile(targetFile, baseDir, files);
        jos.close();
    }

    private static void printUsage() {
        System.out.println("ZipWriter. Usage:");
        System.out.println("java org.cantaloop.tools.zip.ZipWriter zipfile file1 [ file2 ... ]");
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            printUsage();
        }
        File baseDir = new File(".");
        File targetFile = new File(args[0]);
        List files = new ArrayList();
        for (int i = 1; i < args.length; i++) {
            files.add(new File(args[i]));
        }
        writeZip(targetFile, baseDir, files);
    }
}
