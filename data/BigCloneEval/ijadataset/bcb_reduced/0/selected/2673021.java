package pl.otros.logview.io;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.RandomAccessContent;
import org.apache.commons.vfs.util.RandomAccessMode;
import pl.otros.logview.importer.LogImporter;
import pl.otros.logview.parser.ParsingContext;
import pl.otros.logview.reader.ProxyLogDataCollector;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class Utils {

    private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());

    private static final int GZIP_MIN_SIZE = 26;

    private static final int GZIP_CHECK_BUFFER_SIZE = 8 * 1024;

    private static final int DETECT_LOAD_SIZE = 8 * 1024;

    public static boolean checkIfIsGzipped(FileObject fileObject) throws IOException {
        boolean gziped = false;
        if (fileObject.getContent().getSize() == 0) {
            LOGGER.fine("File object " + fileObject.getName() + " is empty, can't detect gzip compression");
            return false;
        }
        InputStream inputStream = fileObject.getContent().getInputStream();
        byte[] loadProbe = loadProbe(inputStream, GZIP_CHECK_BUFFER_SIZE);
        if (loadProbe.length < GZIP_MIN_SIZE) {
            LOGGER.info("Loaded probe is too small to check if it is gziped");
            return false;
        }
        try {
            ByteArrayInputStream bin = new ByteArrayInputStream(loadProbe);
            int available = bin.available();
            byte[] b = new byte[available < GZIP_CHECK_BUFFER_SIZE ? available : GZIP_CHECK_BUFFER_SIZE];
            int read = bin.read(b);
            gziped = checkIfIsGzipped(b, read);
        } catch (IOException e) {
            LOGGER.fine(fileObject.getName() + " is not gzip");
        }
        return gziped;
    }

    public static boolean checkIfIsGzipped(byte[] buffer, int lenght) throws IOException {
        boolean gziped = false;
        try {
            ByteArrayInputStream bin = new ByteArrayInputStream(buffer, 0, lenght);
            GZIPInputStream gzipInputStream = new GZIPInputStream(bin);
            gzipInputStream.read(new byte[buffer.length], 0, bin.available());
            gziped = true;
        } catch (IOException e) {
            gziped = false;
        }
        return gziped;
    }

    public static byte[] loadProbe(InputStream in, int buffSize) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buff = new byte[buffSize];
        int read = in.read(buff);
        if (read > 0) {
            bout.write(buff, 0, read);
        }
        return bout.toByteArray();
    }

    public static LoadingInfo openFileObject(FileObject fileObject) throws Exception {
        return openFileObject(fileObject, false);
    }

    public static LoadingInfo openFileObject(FileObject fileObject, boolean tailing) throws Exception {
        LoadingInfo loadingInfo = new LoadingInfo();
        loadingInfo.setFileObject(fileObject);
        loadingInfo.setFriendlyUrl(fileObject.getName().getFriendlyURI());
        InputStream httpInputStream = fileObject.getContent().getInputStream();
        byte[] buff = Utils.loadProbe(httpInputStream, 10000);
        loadingInfo.setGziped(checkIfIsGzipped(buff, buff.length));
        ByteArrayInputStream bin = new ByteArrayInputStream(buff);
        SequenceInputStream sequenceInputStream = new SequenceInputStream(bin, httpInputStream);
        ObservableInputStreamImpl observableInputStreamImpl = new ObservableInputStreamImpl(sequenceInputStream);
        if (loadingInfo.isGziped()) {
            loadingInfo.setContentInputStream(new GZIPInputStream(observableInputStreamImpl));
            loadingInfo.setInputStreamBufferedStart(ungzip(buff));
        } else {
            loadingInfo.setContentInputStream(observableInputStreamImpl);
            loadingInfo.setInputStreamBufferedStart(buff);
        }
        loadingInfo.setObserableInputStreamImpl(observableInputStreamImpl);
        loadingInfo.setTailing(tailing);
        return loadingInfo;
    }

    private static byte[] ungzip(byte[] buff) throws IOException {
        GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(buff));
        byte[] ungzippedRead = new byte[buff.length];
        int read = gzipInputStream.read(ungzippedRead);
        byte[] ungzipped = new byte[read];
        System.arraycopy(ungzippedRead, 0, ungzipped, 0, read);
        return ungzipped;
    }

    public static void reloadFileObject(LoadingInfo loadingInfo) throws IOException {
        loadingInfo.getFileObject().refresh();
        long lastFileSize = loadingInfo.getLastFileSize();
        long currentSize = loadingInfo.getFileObject().getContent().getSize();
        if (currentSize > lastFileSize) {
            IOUtils.closeQuietly(loadingInfo.getObserableInputStreamImpl());
            RandomAccessContent randomAccessContent = loadingInfo.getFileObject().getContent().getRandomAccessContent(RandomAccessMode.READ);
            randomAccessContent.seek(lastFileSize);
            loadingInfo.setLastFileSize(currentSize);
            ObservableInputStreamImpl observableStream = new ObservableInputStreamImpl(randomAccessContent.getInputStream());
            loadingInfo.setObserableInputStreamImpl(observableStream);
            if (loadingInfo.isGziped()) {
                loadingInfo.setContentInputStream(new GZIPInputStream(observableStream));
            } else {
                loadingInfo.setContentInputStream(observableStream);
            }
        } else if (currentSize < lastFileSize) {
            IOUtils.closeQuietly(loadingInfo.getObserableInputStreamImpl());
            InputStream inputStream = loadingInfo.getFileObject().getContent().getInputStream();
            ObservableInputStreamImpl observableStream = new ObservableInputStreamImpl(inputStream);
            loadingInfo.setObserableInputStreamImpl(observableStream);
            if (loadingInfo.isGziped()) {
                loadingInfo.setContentInputStream(new GZIPInputStream(observableStream));
            } else {
                loadingInfo.setContentInputStream(observableStream);
            }
            loadingInfo.setLastFileSize(loadingInfo.getFileObject().getContent().getSize());
        }
    }

    public static LogImporter detectLogImporter(Collection<LogImporter> importers, byte[] buff) {
        LogImporter highestImporter = null;
        int messageImported = 0;
        for (LogImporter logImporter : importers) {
            ByteArrayInputStream bin = new ByteArrayInputStream(buff);
            ProxyLogDataCollector logCollector = new ProxyLogDataCollector();
            ParsingContext parsingContext = new ParsingContext();
            try {
                logImporter.initParsingContext(parsingContext);
            } catch (Exception e) {
                LOGGER.warning(String.format("Exception when initializing parinsg context for logger %s: %s", logImporter.getName(), e.getMessage()));
            }
            try {
                logImporter.importLogs(bin, logCollector, parsingContext);
            } catch (Exception e1) {
            }
            int currentLogMessageImported = logCollector.getLogData().length;
            if (messageImported < currentLogMessageImported) {
                messageImported = currentLogMessageImported;
                highestImporter = logImporter;
            }
        }
        return highestImporter;
    }

    public static void closeQuietly(FileObject fileObject) {
        if (fileObject != null) {
            String friendlyURI = fileObject.getName().getFriendlyURI();
            try {
                LOGGER.info(String.format("Closing file %s", friendlyURI));
                fileObject.close();
                LOGGER.info(String.format("File %s closed", friendlyURI));
            } catch (FileSystemException ignore) {
                LOGGER.info(String.format("File %s is not closed: %s", friendlyURI, ignore.getMessage()));
            }
        }
    }

    /**
   * Get short name for URL
   * 
   * @param fileObject
   * @return scheme://hostWithoutDomain/fileBaseName
   */
    public static String getFileObjectShortName(FileObject fileObject) {
        StringBuilder sb = new StringBuilder();
        try {
            URI uri = new URI(fileObject.getName().getURI());
            String scheme = fileObject.getName().getScheme();
            sb.append(scheme);
            sb.append("://");
            if (!"file".equals(scheme)) {
                String host = uri.getHost();
                if (!Pattern.matches("(\\d+\\.){3}\\d+", host)) {
                    host = host.split("\\.")[0];
                }
                sb.append(host).append('/');
            }
            sb.append(fileObject.getName().getBaseName());
        } catch (URISyntaxException e) {
            LOGGER.warning("Problem with preparing short name of fileobject: " + e.getMessage());
            sb.setLength(0);
            sb.append(fileObject.getName().getScheme()).append("://").append(fileObject.getName().getBaseName());
        }
        return sb.toString();
    }
}
