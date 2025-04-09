package com.mgensystems.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <b>Title:</b>Jar File Util <br />
 * <b>Description:</b>A utiltiy to write jar files containing class files. <br />
 * <b>Changes:</b><li></li>
 * 
 * @author raykroeker@gmail.com
 */
public final class JarFileUtil {

    /**
	 * <b>Title:</b>Jar File Class <br />
	 * <b>Description:</b>Used to define a jar file entry representing a class. <br />
	 * <b>Changes:</b><li></li>
	 * 
	 * @author raykroeker@gmail.com
	 */
    public static class JarClass {

        /** A name. */
        private final String name;

        /** An input stream. */
        private final InputStream stream;

        /**
		 * Create JarClass.
		 * 
		 * @param name
		 *            A <code>String</code>.
		 */
        public JarClass(final String name) {
            this(name, new ByteArrayInputStream(new byte[0]));
        }

        /**
		 * Create JarFileClass.
		 * 
		 * @param name
		 *            A <code>String</code>.
		 * @param stream
		 *            An <code>InputStream</code>.
		 */
        public JarClass(final String name, final InputStream stream) {
            super();
            this.name = name;
            this.stream = stream;
        }

        /**
		 * Obtain the jar class name.
		 * 
		 * @return A <code>String</code>.
		 */
        public String getName() {
            return name;
        }
    }

    /** A buffer used to write jar file contents. */
    private static byte[] buffer;

    /** A buffer synchronization lock. */
    private static final Object bufferLock;

    static {
        bufferLock = new Object();
    }

    /**
	 * Write a jar file.
	 * 
	 * @param jarFile
	 *            A <code>File</code>.
	 * @param jarFileClasses
	 *            A <code>List<JarFileClass></code>.
	 * @throws IOException
	 *             if an io error occurs
	 */
    public static void write(final File jarFile, final List<JarClass> jarClasses) throws IOException {
        final JarOutputStream jarStream = new JarOutputStream(new FileOutputStream(jarFile));
        try {
            for (final JarClass jarClass : jarClasses) {
                writeZipEntry(jarStream, jarClass.name, jarClass.stream);
            }
        } finally {
            jarStream.close();
        }
    }

    /**
	 * Write a zip entry.
	 * 
	 * @param target
	 *            A <code>ZipOutputStream</code>.
	 * @param name
	 *            A <code>String</code>.
	 * @param source
	 *            An <code>InputStream</code>.
	 * @throws IOException
	 *             if an io error occurs
	 */
    private static void writeZipEntry(final ZipOutputStream target, final String name, final InputStream source) throws IOException {
        final ZipEntry entry = new ZipEntry(name);
        target.putNextEntry(entry);
        try {
            synchronized (bufferLock) {
                buffer = new byte[1024 * 1024];
                try {
                    int bytes = source.read(buffer);
                    while (-1 != bytes) {
                        target.write(buffer, 0, bytes);
                        target.flush();
                        bytes = source.read(buffer);
                    }
                } finally {
                    buffer = null;
                }
            }
        } finally {
            target.closeEntry();
        }
    }

    /**
	 * Create JarFileUtil.
	 * 
	 */
    private JarFileUtil() {
        super();
    }
}
