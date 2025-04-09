package com.sun.java.util.jar.pack;

import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
import java.io.*;
import sun.util.logging.PlatformLogger;

class Utils {

    static final String COM_PREFIX = "com.sun.java.util.jar.pack.";

    static final String METAINF = "META-INF";

    static final String DEBUG_VERBOSE = Utils.COM_PREFIX + "verbose";

    static final String DEBUG_DISABLE_NATIVE = COM_PREFIX + "disable.native";

    static final String PACK_DEFAULT_TIMEZONE = COM_PREFIX + "default.timezone";

    static final String UNPACK_MODIFICATION_TIME = COM_PREFIX + "unpack.modification.time";

    static final String UNPACK_STRIP_DEBUG = COM_PREFIX + "unpack.strip.debug";

    static final String UNPACK_REMOVE_PACKFILE = COM_PREFIX + "unpack.remove.packfile";

    static final String NOW = "now";

    static final String PACK_KEEP_CLASS_ORDER = COM_PREFIX + "keep.class.order";

    static final String PACK_ZIP_ARCHIVE_MARKER_COMMENT = "PACK200";

    static final ThreadLocal currentInstance = new ThreadLocal();

    static PropMap currentPropMap() {
        Object obj = currentInstance.get();
        if (obj instanceof PackerImpl) return ((PackerImpl) obj)._props;
        if (obj instanceof UnpackerImpl) return ((UnpackerImpl) obj)._props;
        return null;
    }

    static final boolean nolog = Boolean.getBoolean(Utils.COM_PREFIX + "nolog");

    static class Pack200Logger {

        private final String name;

        private PlatformLogger log;

        Pack200Logger(String name) {
            this.name = name;
        }

        private synchronized PlatformLogger getLogger() {
            if (log == null) {
                log = PlatformLogger.getLogger(name);
            }
            return log;
        }

        public void warning(String msg, Object param) {
            int verbose = currentPropMap().getInteger(DEBUG_VERBOSE);
            if (verbose > 0) {
                getLogger().warning(msg, param);
            }
        }

        public void warning(String msg) {
            warning(msg, null);
        }

        public void info(String msg) {
            int verbose = currentPropMap().getInteger(DEBUG_VERBOSE);
            if (verbose > 0) {
                if (nolog) {
                    System.out.println(msg);
                } else {
                    getLogger().info(msg);
                }
            }
        }

        public void fine(String msg) {
            int verbose = currentPropMap().getInteger(DEBUG_VERBOSE);
            if (verbose > 0) {
                System.out.println(msg);
            }
        }
    }

    static final Pack200Logger log = new Pack200Logger("java.util.jar.Pack200");

    static String getVersionString() {
        return "Pack200, Vendor: Sun Microsystems, Version: " + Constants.JAVA6_PACKAGE_MAJOR_VERSION + "." + Constants.JAVA6_PACKAGE_MINOR_VERSION;
    }

    static void markJarFile(JarOutputStream out) throws IOException {
        out.setComment(PACK_ZIP_ARCHIVE_MARKER_COMMENT);
    }

    static void copyJarFile(JarInputStream in, JarOutputStream out) throws IOException {
        if (in.getManifest() != null) {
            ZipEntry me = new ZipEntry(JarFile.MANIFEST_NAME);
            out.putNextEntry(me);
            in.getManifest().write(out);
            out.closeEntry();
        }
        byte[] buffer = new byte[1 << 14];
        for (JarEntry je; (je = in.getNextJarEntry()) != null; ) {
            out.putNextEntry(je);
            for (int nr; 0 < (nr = in.read(buffer)); ) {
                out.write(buffer, 0, nr);
            }
        }
        in.close();
        markJarFile(out);
    }

    static void copyJarFile(JarFile in, JarOutputStream out) throws IOException {
        byte[] buffer = new byte[1 << 14];
        for (Enumeration e = in.entries(); e.hasMoreElements(); ) {
            JarEntry je = (JarEntry) e.nextElement();
            out.putNextEntry(je);
            InputStream ein = in.getInputStream(je);
            for (int nr; 0 < (nr = ein.read(buffer)); ) {
                out.write(buffer, 0, nr);
            }
        }
        in.close();
        markJarFile(out);
    }

    static void copyJarFile(JarInputStream in, OutputStream out) throws IOException {
        out = new BufferedOutputStream(out);
        out = new NonCloser(out);
        JarOutputStream jout = new JarOutputStream(out);
        copyJarFile(in, jout);
        jout.close();
    }

    static void copyJarFile(JarFile in, OutputStream out) throws IOException {
        out = new BufferedOutputStream(out);
        out = new NonCloser(out);
        JarOutputStream jout = new JarOutputStream(out);
        copyJarFile(in, jout);
        jout.close();
    }

    private static class NonCloser extends FilterOutputStream {

        NonCloser(OutputStream out) {
            super(out);
        }

        public void close() throws IOException {
            flush();
        }
    }

    static String getJarEntryName(String name) {
        if (name == null) return null;
        return name.replace(File.separatorChar, '/');
    }

    static String zeString(ZipEntry ze) {
        int store = (ze.getCompressedSize() > 0) ? (int) ((1.0 - ((double) ze.getCompressedSize() / (double) ze.getSize())) * 100) : 0;
        return (long) ze.getSize() + "\t" + ze.getMethod() + "\t" + ze.getCompressedSize() + "\t" + store + "%\t" + new Date(ze.getTime()) + "\t" + Long.toHexString(ze.getCrc()) + "\t" + ze.getName();
    }

    static byte[] readMagic(BufferedInputStream in) throws IOException {
        in.mark(4);
        byte[] magic = new byte[4];
        for (int i = 0; i < magic.length; i++) {
            if (1 != in.read(magic, i, 1)) break;
        }
        in.reset();
        return magic;
    }

    static boolean isJarMagic(byte[] magic) {
        return (magic[0] == (byte) 'P' && magic[1] == (byte) 'K' && magic[2] >= 1 && magic[2] < 8 && magic[3] == magic[2] + 1);
    }

    static boolean isPackMagic(byte[] magic) {
        return (magic[0] == (byte) 0xCA && magic[1] == (byte) 0xFE && magic[2] == (byte) 0xD0 && magic[3] == (byte) 0x0D);
    }

    static boolean isGZIPMagic(byte[] magic) {
        return (magic[0] == (byte) 0x1F && magic[1] == (byte) 0x8B && magic[2] == (byte) 0x08);
    }

    private Utils() {
    }
}
