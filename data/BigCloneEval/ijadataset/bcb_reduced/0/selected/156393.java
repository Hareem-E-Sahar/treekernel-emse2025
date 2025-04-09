package com.mgensystems.util;

import static org.junit.Assert.fail;
import java.io.File;
import java.io.IOException;

/**
 * <b>Title:</b> <br />
 * <b>Description:</b> <br />
 * <b>Changes:</b><li></li>
 * @author raykroeker@gmail.com
 */
public final class FileUtil {

    /** The parent for all files/directories. */
    private static final File parentDirectory;

    static {
        parentDirectory = new File(System.getProperty("java.io.tmpdir"), System.getProperty("user.name") + "-jarindexer");
        if (parentDirectory.exists()) {
            try {
                deleteTree(parentDirectory);
            } catch (final IOException iox) {
                Assert.fail("Cannot delete tree:" + parentDirectory, iox);
            }
        }
        if (false == parentDirectory.mkdir()) {
            fail("Cannot create: " + parentDirectory);
        }
    }

    /**
	 * Create a directory.
	 * 
	 * @param name
	 *            A <code>String</code>.
	 * @return A <code>File</code>.
	 * @throws IOException
	 *             if the directory cannot be created
	 */
    public static File createDirectory(final String name) throws IOException {
        final File directory = new File(parentDirectory, name);
        if (false == directory.mkdir()) {
            throw new IOException("Cannot create:" + directory);
        }
        return directory;
    }

    /**
	 * Create a file.
	 * 
	 * @param name
	 *            A <code>String</code>.
	 * @return A <code>File</code>.
	 * @throws IOException
	 *             if the file cannot be created
	 */
    public static File createFile(final String name) throws IOException {
        final File file = new File(parentDirectory, name);
        if (false == file.createNewFile()) {
            throw new IOException("Cannot create:" + file);
        }
        return file;
    }

    /**
	 * Delete a directory tree.
	 * 
	 * @param directory A <code>File</code>.
	 * @throws IOException
	 *             if an io error occurs
	 */
    public static void deleteTree(final File directory) throws IOException {
        if (false == directory.exists()) {
            fail("Cannot delete tree.  Does not exist:" + directory);
        }
        if (false == directory.isDirectory()) {
            fail("Cannot delete tree.  Not a directory:" + directory);
        }
        if (false == directory.canRead()) {
            if (false == directory.setReadable(true)) {
                fail("Cannot delete tree.  Cannot read:" + directory);
            }
        }
        if (false == directory.canWrite()) {
            if (false == directory.setWritable(true)) {
                fail("Cannot delete tree.  Cannot write:" + directory);
            }
        }
        final File[] files = directory.listFiles();
        for (final File file : files) {
            if (file.isFile()) {
                if (false == file.delete()) {
                    throw new IOException("Cannot delete:" + file);
                }
            }
            if (file.isDirectory()) {
                deleteTree(file);
            }
        }
        if (false == directory.delete()) {
            throw new IOException("Cannot delete:" + directory);
        }
    }

    /**
	 * Obtain the parent directory.
	 * 
	 * @return A <code>File</code>.
	 */
    public static File getParentDirectory() {
        return parentDirectory;
    }

    /**
	 * Create FileUtil.
	 * 
	 */
    private FileUtil() {
        super();
    }
}
