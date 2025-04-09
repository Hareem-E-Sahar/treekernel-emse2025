package org.openXpertya.tools;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;

/**
 * Descripción de Clase
 *
 *
 * @version    2.2, 12.10.07
 * @author     Equipo de Desarrollo de openXpertya    
 */
public class ClassVersionLimiter {

    /**
     * Descripción de Método
     *
     *
     * @param args
     */
    public static void main(String[] args) {
        ClassVersionLimiter classVersionLimiter = new ClassVersionLimiter("D:/j2sdk1.4b3p", 46, 0);
    }

    /** Descripción de Campos */
    private long _maxAllowedVersion;

    /** Descripción de Campos */
    private PrintWriter m_out = null;

    /**
     * Constructor de la clase ...
     *
     *
     * @param rootDirectory
     * @param mainVersion
     * @param minorVersion
     */
    public ClassVersionLimiter(String rootDirectory, int mainVersion, int minorVersion) {
        _maxAllowedVersion = mainVersion * 65536 + minorVersion;
        try {
            File logFile = new File(rootDirectory + File.separator + "patchJar.log");
            m_out = new PrintWriter(new FileOutputStream(logFile), true);
            System.out.println("Log=" + logFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Cannot write log");
            e.printStackTrace(System.err);
            System.exit(1);
        }
        File root = new File(rootDirectory);
        search(root);
    }

    /**
     * Descripción de Método
     *
     *
     * @param dir
     */
    private void search(File dir) {
        dir.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".jar")) {
                    checkJar(pathname);
                }
                if (pathname.isDirectory()) {
                    search(pathname);
                }
                return false;
            }
        });
    }

    /**
     * Descripción de Método
     *
     *
     * @param file
     */
    private void checkJar(File file) {
        byte[] header = new byte[8];
        long maxVersion = 0;
        boolean foundClassFiles = false;
        try {
            JarFile jarFile = new JarFile(file);
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    foundClassFiles = true;
                    InputStream stream = jarFile.getInputStream(entry);
                    stream.read(header);
                    int mainVersion = header[6] * 256 + header[7];
                    int minorVersion = header[4] * 256 + header[5];
                    long version = mainVersion * 65536 + minorVersion;
                    if (version > maxVersion) {
                        maxVersion = version;
                    }
                }
            }
            jarFile.close();
            if (foundClassFiles && (maxVersion > _maxAllowedVersion)) {
                patchJar(file);
            }
        } catch (Exception e) {
            log("checkJar", e);
        }
    }

    /**
     * Descripción de Método
     *
     *
     * @param file
     */
    private void patchJar(File file) {
        log("Patching " + file, null);
        try {
            final int MAX_ENTRY_SIZE = 1024 * 1024;
            byte[] classBytes = new byte[MAX_ENTRY_SIZE];
            File newFile = new File(file.getAbsolutePath() + ".NEW");
            CRC32 crc32 = new CRC32();
            JarFile oldJarFile = new JarFile(file);
            Manifest manifest = oldJarFile.getManifest();
            FileOutputStream outputStream = new FileOutputStream(newFile);
            JarOutputStream jarOutput = new JarOutputStream(outputStream, manifest);
            Enumeration entries = oldJarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry oldEntry = (JarEntry) entries.nextElement();
                JarEntry newEntry = (JarEntry) oldEntry.clone();
                System.out.print(".");
                if (oldEntry.getName().equals("META-INF/MANIFEST.MF")) {
                    continue;
                }
                InputStream in = oldJarFile.getInputStream(oldEntry);
                int totalLen = 0;
                int readLen = 0;
                do {
                    totalLen += readLen;
                    readLen = in.read(classBytes, totalLen, MAX_ENTRY_SIZE - totalLen);
                } while (readLen > 0);
                if (totalLen == MAX_ENTRY_SIZE) {
                    log("ERROR= patchJAR: MAX_ENTRY_SIZE too small - " + MAX_ENTRY_SIZE, null);
                    return;
                }
                if ((totalLen > 0) && oldEntry.getName().endsWith(".class")) {
                    int mainVersion = classBytes[6] * 256 + classBytes[7];
                    int minorVersion = classBytes[4] * 256 + classBytes[5];
                    long version = mainVersion * 65536 + minorVersion;
                    if (version > _maxAllowedVersion) {
                        log(" - " + oldEntry.getName(), null);
                        mainVersion = (int) _maxAllowedVersion / 65536;
                        minorVersion = (int) _maxAllowedVersion % 65536;
                        classBytes[4] = (byte) (minorVersion / 256);
                        classBytes[5] = (byte) (minorVersion % 256);
                        classBytes[6] = (byte) (mainVersion / 256);
                        classBytes[7] = (byte) (mainVersion % 256);
                        crc32.reset();
                        crc32.update(classBytes, 0, totalLen);
                        newEntry.setCrc(crc32.getValue());
                        newEntry.setCompressedSize(-1);
                    }
                }
                jarOutput.putNextEntry(newEntry);
                if (totalLen > 0) {
                    jarOutput.write(classBytes, 0, totalLen);
                }
                jarOutput.closeEntry();
            }
            jarOutput.close();
            outputStream.close();
            oldJarFile.close();
            log("", null);
            File bakFile = new File(file.getAbsolutePath() + ".BAK");
            if (!file.renameTo(bakFile)) {
                log("ERROR= Cannot rename " + file + " to " + bakFile + ". EXITING.", null);
                System.exit(99);
            }
            if (!newFile.renameTo(file)) {
                log("ERROR= Cannot rename " + newFile + " to " + file + ". EXITING.", null);
                System.exit(99);
            }
        } catch (Exception e) {
            log("patchJar", e);
        }
    }

    /**
     * Descripción de Método
     *
     *
     * @param message
     * @param e
     */
    private void log(String message, Exception e) {
        if (e == null) {
            System.out.println(message);
            m_out.println(message);
        } else {
            System.err.print("ERROR= ");
            System.err.println(message);
            e.printStackTrace(System.err);
            m_out.print("ERROR= ");
            m_out.println(message);
            e.printStackTrace(m_out);
        }
    }
}
