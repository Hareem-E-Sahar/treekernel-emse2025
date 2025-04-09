package ru.spbspu.staub.util;

import static org.jboss.seam.ScopeType.APPLICATION;
import org.jboss.seam.annotations.Install;
import static org.jboss.seam.annotations.Install.FRAMEWORK;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;
import org.jboss.seam.servlet.ContextualHttpServletRequest;
import org.jboss.seam.web.AbstractResource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Seam component for dowmnloading byte data from session to file (as attachment to response).
 *
 * @author Konstantin Grigoriev
 */
@Scope(APPLICATION)
@Name("ru.spbspu.staub.downloadResource")
@Install(precedence = FRAMEWORK)
@BypassInterceptors
public class DownloadResource extends AbstractResource {

    @Logger
    private Log logger;

    public static final String RESOURCE_PATH = "/download";

    /**
     * Http Session key for storing data to download.
     */
    public static final String SESSION_KEY = "STAUB-DOWNLOAD-DATA";

    /**
     * Http Session key for storing file name to download data.
     */
    public static final String FILENAME_KEY = "STAUB-DOWNLOAD-FILENAME";

    /**
     * Http Session key for storing content type of download data.
     */
    public static final String CONTENT_TYPE_KEY = "STAUB-DOWNLOAD-CONTENT-TYPE";

    /**
     * Http Session key for storing compress flag, which shows need to compress data or not.
     */
    public static final String COMPRESS_KEY = "STAUB-DOWNLOAD-COMPRESS";

    /**
     * Defines supported content types.
     */
    private static enum ContentType {

        TEXT("text/html; charset=utf-8", ".txt"), EXCEL("application/vnd.ms-excel; charset=utf-8", ".xls"), ZIP("application/zip; charset=utf-8", ".zip");

        private String realContentType;

        private String fileExtension;

        /**
         * Creates content type with specific real type.
         *
         * @param realContentType real type
         * @param fileExtension   file extension
         */
        ContentType(String realContentType, String fileExtension) {
            this.realContentType = realContentType;
            this.fileExtension = fileExtension;
        }

        public String getRealContentType() {
            return this.realContentType;
        }

        public String getFileExtension() {
            return fileExtension;
        }
    }

    /**
     * Whitespace code in ASCII in hex.
     */
    private static final int WHITESPACE_ASCII_CODE = 0x20;

    /**
     * Default encoding for the servlet output.
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    @Override
    public String getResourcePath() {
        return RESOURCE_PATH;
    }

    @Override
    public void getResource(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        new ContextualHttpServletRequest(request) {

            @Override
            public void process() throws IOException {
                doWork(request, response);
            }
        }.run();
    }

    private void doWork(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.debug("Downloading resource...");
        String contentTypeName = request.getParameter(CONTENT_TYPE_KEY);
        ContentType contentType = ContentType.valueOf(contentTypeName);
        logger.debug("  content type is: " + contentType);
        String filename = (String) Contexts.getSessionContext().get(FILENAME_KEY);
        logger.debug("  file name is: " + filename);
        byte[] downloadData = (byte[]) Contexts.getSessionContext().get(SESSION_KEY);
        logger.debug("  download data is: " + String.valueOf(downloadData));
        if (downloadData == null) {
            logger.error("download failed, data is null");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            logger.debug(" writing to output stream... ");
            boolean compress = Boolean.valueOf(request.getParameter(COMPRESS_KEY));
            if (compress) {
                logger.debug(" zip flag setted -> using compression");
                response.setContentType(ContentType.ZIP.getRealContentType());
                response.setHeader("Content-Disposition", "attachment; filename=\"" + makeUTF8Filename(filename + ContentType.ZIP.getFileExtension()) + "\"");
                ZipOutputStream stream = new ZipOutputStream(response.getOutputStream());
                stream.putNextEntry(new ZipEntry(makeUTF8Filename(filename + contentType.getFileExtension())));
                stream.write(downloadData);
                stream.close();
            } else {
                response.setContentType(contentType.getRealContentType());
                response.setHeader("Content-Disposition", "attachment; filename=\"" + makeUTF8Filename(filename + contentType.getFileExtension()) + "\"");
                response.setContentLength(downloadData.length);
                response.getOutputStream().write(downloadData);
            }
            response.flushBuffer();
            response.getOutputStream().flush();
            response.getOutputStream().close();
            Contexts.getSessionContext().set(SESSION_KEY, null);
            Contexts.getSessionContext().set(FILENAME_KEY, null);
            logger.debug("Downloading resource...Ok");
        }
    }

    /**
     * Converts source file name to UTF-8 encoding.
     *
     * @param filename file name
     * @return converted file name
     */
    private String makeUTF8Filename(String filename) {
        try {
            byte[] bytes = filename.getBytes(DEFAULT_ENCODING);
            StringBuffer result = new StringBuffer();
            for (byte aByte : bytes) {
                if (aByte >= WHITESPACE_ASCII_CODE) {
                    result.append((char) aByte);
                } else {
                    result.append("%").append(Long.toHexString(aByte < 0 ? (long) Math.pow(2, Byte.SIZE) + aByte : aByte));
                }
            }
            return result.toString();
        } catch (Exception e) {
            logger.error("Error during converting file name to UTF-8 -> using source file name", e);
            return filename;
        }
    }
}
