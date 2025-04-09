package com.phloc.webbasics.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.phloc.commons.io.streams.StreamUtils;
import com.phloc.commons.microdom.IMicroDocument;
import com.phloc.commons.microdom.serialize.MicroWriter;
import com.phloc.commons.mime.IMimeType;
import com.phloc.commons.xml.EXMLIncorrectCharacterHandling;
import com.phloc.commons.xml.serialize.EXMLSerializeFormat;
import com.phloc.commons.xml.serialize.IXMLWriterSettings;
import com.phloc.commons.xml.serialize.XMLWriterSettings;
import com.phloc.scopes.web.domain.IRequestWebScope;

/**
 * Some HTTP utility methods
 * 
 * @author philip
 */
public final class HTTPResponseHelper {

    private static final Logger s_aLogger = LoggerFactory.getLogger(HTTPResponseHelper.class);

    private static final IXMLWriterSettings XML_WRITER_SETTINGS = new XMLWriterSettings().setFormat(EXMLSerializeFormat.HTML).setIncorrectCharacterHandling(EXMLIncorrectCharacterHandling.DO_NOT_WRITE_LOG_WARNING);

    private HTTPResponseHelper() {
    }

    @Nonnull
    public static OutputStream getBestSuitableOutputStream(@Nonnull final HttpServletRequest aHttpRequest, @Nonnull final HttpServletResponse aHttpResponse) throws IOException {
        final String sAcceptEncoding = aHttpRequest.getHeader("Accept-Encoding");
        if (sAcceptEncoding.contains("gzip")) {
            aHttpResponse.setHeader("Content-Encoding", "gzip");
            return new GZIPOutputStream(aHttpResponse.getOutputStream());
        }
        if (sAcceptEncoding.contains("deflate")) {
            aHttpResponse.setHeader("Content-Encoding", "deflate");
            final ZipOutputStream aOS = new ZipOutputStream(aHttpResponse.getOutputStream());
            aOS.putNextEntry(new ZipEntry("dummy name"));
            return aOS;
        }
        return aHttpResponse.getOutputStream();
    }

    public static void createResponse(@Nonnull final IRequestWebScope aRequestScope, @Nonnull final IMicroDocument aDoc, @Nonnull final IMimeType aMimeType) throws ServletException {
        try {
            final HttpServletRequest aHttpRequest = aRequestScope.getRequest();
            final HttpServletResponse aHttpResponse = aRequestScope.getResponse();
            final String sXMLCode = MicroWriter.getNodeAsString(aDoc, XML_WRITER_SETTINGS);
            final String sCharset = XML_WRITER_SETTINGS.getCharset();
            aHttpResponse.setContentType(aMimeType.getAsStringWithEncoding(sCharset));
            aHttpResponse.setCharacterEncoding(sCharset);
            final OutputStream aOS = getBestSuitableOutputStream(aHttpRequest, aHttpResponse);
            aOS.write(sXMLCode.getBytes(sCharset));
            aOS.flush();
            aOS.close();
        } catch (final Throwable t) {
            if (!StreamUtils.isKnownEOFException(t)) {
                s_aLogger.error("Error running application", t);
                if (t instanceof ServletException) throw (ServletException) t;
                throw new ServletException(t);
            }
        }
    }
}
