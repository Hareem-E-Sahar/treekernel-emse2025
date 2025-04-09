package net.laubenberger.bogatyr.helper.launcher;

import java.awt.Desktop;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import net.laubenberger.bogatyr.helper.HelperEnvironment;
import net.laubenberger.bogatyr.helper.HelperIO;
import net.laubenberger.bogatyr.helper.HelperLog;
import net.laubenberger.bogatyr.helper.HelperString;
import net.laubenberger.bogatyr.misc.exception.RuntimeExceptionIsEmpty;
import net.laubenberger.bogatyr.misc.exception.RuntimeExceptionIsNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This launcher opens, edits and prints files with the default system application.
 *
 * @author Stefan Laubenberger
 * @version 0.9.4 (20101119)
 * @since 0.7.0
 */
public abstract class LauncherFile {

    private static final Logger log = LoggerFactory.getLogger(LauncherFile.class);

    private static final File PATH = HelperEnvironment.getOsTempDirectory();

    static final String IDENTIFIER = LauncherFile.class.getSimpleName();

    static {
        try {
            deleteTemporaryFiles();
        } catch (IOException ex) {
            if (log.isWarnEnabled()) log.warn("Could not delete temporary files", ex);
        }
    }

    /**
	 * Open a byte-array data with the default system application.
	 *
	 * @param data		as byte array
	 * @param extension of the file (e.g. ".pdf")
	 * @throws IOException
	 * @since 0.7.0
	 */
    public static void open(final byte[] data, final String extension) throws IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(data, extension));
        if (null == data) {
            throw new RuntimeExceptionIsNull("data");
        }
        if (null == extension) {
            throw new RuntimeExceptionIsNull("extension");
        }
        if (!HelperString.isValid(extension)) {
            throw new RuntimeExceptionIsEmpty("extension");
        }
        open(createTemporaryFile(data, extension));
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    /**
	 * Open a {@link File} with the default system application.
	 *
	 * @param file data as file
	 * @throws IOException
	 * @see File
	 * @since 0.7.0
	 */
    public static void open(final File file) throws IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(file));
        if (null == file) {
            throw new RuntimeExceptionIsNull("file");
        }
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        } else {
            throw new RuntimeException("Default system viewer application not supported by your machine");
        }
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    /**
	 * Open an {@link InputStream} with the default system application.
	 *
	 * @param is		  data as stream
	 * @param extension of the file (e.g. ".pdf")
	 * @throws IOException
	 * @see InputStream
	 * @since 0.7.0
	 */
    public static void open(final InputStream is, final String extension) throws IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(is, extension));
        if (null == is) {
            throw new RuntimeExceptionIsNull("is");
        }
        open(HelperIO.readStream(is), extension);
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    /**
	 * Edit a byte-array data with the default system application.
	 *
	 * @param data		as byte array
	 * @param extension of the file (e.g. ".html")
	 * @throws IOException
	 * @since 0.7.0
	 */
    public static void edit(final byte[] data, final String extension) throws IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(data, extension));
        if (null == data) {
            throw new RuntimeExceptionIsNull("data");
        }
        if (null == extension) {
            throw new RuntimeExceptionIsNull("extension");
        }
        if (!HelperString.isValid(extension)) {
            throw new RuntimeExceptionIsEmpty("extension");
        }
        edit(createTemporaryFile(data, extension));
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    /**
	 * Edit a {@link File} with the default system application.
	 *
	 * @param file data as file
	 * @throws IOException
	 * @see File
	 * @since 0.7.0
	 */
    public static void edit(final File file) throws IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(file));
        if (null == file) {
            throw new RuntimeExceptionIsNull("file");
        }
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().edit(file);
        } else {
            throw new RuntimeException("Default system editor application not supported by your machine");
        }
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    /**
	 * Edit an {@link InputStream} with the default system application.
	 *
	 * @param is		  data as stream
	 * @param extension of the file (e.g. ".html")
	 * @throws IOException
	 * @see InputStream
	 * @since 0.7.0
	 */
    public static void edit(final InputStream is, final String extension) throws IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(is, extension));
        if (null == is) {
            throw new RuntimeExceptionIsNull("is");
        }
        edit(HelperIO.readStream(is), extension);
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    /**
	 * Print a byte-array data with the default system application.
	 *
	 * @param data		as byte array
	 * @param extension of the file (e.g. ".html")
	 * @throws IOException
	 * @since 0.7.0
	 */
    public static void print(final byte[] data, final String extension) throws IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(data, extension));
        if (null == data) {
            throw new RuntimeExceptionIsNull("data");
        }
        if (null == extension) {
            throw new RuntimeExceptionIsNull("extension");
        }
        if (!HelperString.isValid(extension)) {
            throw new RuntimeExceptionIsEmpty("extension");
        }
        print(createTemporaryFile(data, extension));
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    /**
	 * Print a {@link File} with the default system application.
	 *
	 * @param file data as file
	 * @throws IOException
	 * @see File
	 * @since 0.7.0
	 */
    public static void print(final File file) throws IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(file));
        if (null == file) {
            throw new RuntimeExceptionIsNull("file");
        }
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().print(file);
        } else {
            throw new RuntimeException("Default system print application not supported by your machine");
        }
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    /**
	 * Print an {@link InputStream} with the default system application.
	 *
	 * @param is		  data as stream
	 * @param extension of the file (e.g. ".html")
	 * @throws IOException
	 * @see InputStream
	 * @since 0.7.0
	 */
    public static void print(final InputStream is, final String extension) throws IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(is, extension));
        if (null == is) {
            throw new RuntimeExceptionIsNull("is");
        }
        print(HelperIO.readStream(is), extension);
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    /**
	 * Deletes the created temporary files.
	 *
	 * @throws IOException
	 * @since 0.8.0
	 */
    public static void deleteTemporaryFiles() throws IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart());
        final FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(final File file) {
                return file.getName().contains(IDENTIFIER);
            }
        };
        for (final File file : HelperIO.getFiles(PATH, filter)) {
            HelperIO.delete(file);
        }
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    private static File createTemporaryFile(final byte[] data, final String extension) throws IOException {
        if (log.isTraceEnabled()) log.trace(HelperLog.methodStart(data, extension));
        final File result = extension.startsWith(HelperString.PERIOD) ? new File(PATH, IDENTIFIER + System.currentTimeMillis() + extension) : new File(PATH, IDENTIFIER + System.currentTimeMillis() + HelperString.PERIOD + extension);
        HelperIO.writeFile(result, data, false);
        if (log.isTraceEnabled()) log.trace(HelperLog.methodExit(result));
        return result;
    }
}
