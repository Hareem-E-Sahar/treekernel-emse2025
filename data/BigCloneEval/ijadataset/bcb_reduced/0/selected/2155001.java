package compressionFilters;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Implementation of <b>HttpServletResponseWrapper</b> that works with
 * the CompressionServletResponseStream implementation..
 *
 * @author Amy Roh
 * @author Dmitri Valdin
 * @version $Id: CompressionServletResponseWrapper.java 987920 2010-08-22 15:34:34Z markt $
 */
public class CompressionServletResponseWrapper extends HttpServletResponseWrapper {

    /**
     * Calls the parent constructor which creates a ServletResponse adaptor
     * wrapping the given response object.
     */
    public CompressionServletResponseWrapper(HttpServletResponse response) {
        super(response);
        origResponse = response;
        if (debug > 1) {
            System.out.println("CompressionServletResponseWrapper constructor gets called");
        }
    }

    /**
     * Original response
     */
    protected HttpServletResponse origResponse = null;

    /**
     * Descriptive information about this Response implementation.
     */
    protected static final String info = "CompressionServletResponseWrapper";

    /**
     * The ServletOutputStream that has been returned by
     * <code>getOutputStream()</code>, if any.
     */
    protected ServletOutputStream stream = null;

    /**
     * The PrintWriter that has been returned by
     * <code>getWriter()</code>, if any.
     */
    protected PrintWriter writer = null;

    /**
     * The threshold number to compress
     */
    protected int threshold = 0;

    /**
     * Debug level
     */
    private int debug = 0;

    /**
     * Content type
     */
    protected String contentType = null;

    /**
     * Set content type
     */
    @Override
    public void setContentType(String contentType) {
        if (debug > 1) {
            System.out.println("setContentType to " + contentType);
        }
        this.contentType = contentType;
        origResponse.setContentType(contentType);
    }

    /**
     * Set threshold number
     */
    public void setCompressionThreshold(int threshold) {
        if (debug > 1) {
            System.out.println("setCompressionThreshold to " + threshold);
        }
        this.threshold = threshold;
    }

    /**
     * Set debug level
     */
    public void setDebugLevel(int debug) {
        this.debug = debug;
    }

    /**
     * Create and return a ServletOutputStream to write the content
     * associated with this Response.
     *
     * @exception IOException if an input/output error occurs
     */
    public ServletOutputStream createOutputStream() throws IOException {
        if (debug > 1) {
            System.out.println("createOutputStream gets called");
        }
        CompressionResponseStream compressedStream = new CompressionResponseStream(origResponse);
        compressedStream.setDebugLevel(debug);
        compressedStream.setBuffer(threshold);
        return compressedStream;
    }

    /**
     * Finish a response.
     */
    public void finishResponse() {
        try {
            if (writer != null) {
                writer.close();
            } else {
                if (stream != null) stream.close();
            }
        } catch (IOException e) {
        }
    }

    /**
     * Flush the buffer and commit this response.
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    public void flushBuffer() throws IOException {
        if (debug > 1) {
            System.out.println("flush buffer @ CompressionServletResponseWrapper");
        }
        ((CompressionResponseStream) stream).flush();
    }

    /**
     * Return the servlet output stream associated with this Response.
     *
     * @exception IllegalStateException if <code>getWriter</code> has
     *  already been called for this response
     * @exception IOException if an input/output error occurs
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) throw new IllegalStateException("getWriter() has already been called for this response");
        if (stream == null) stream = createOutputStream();
        if (debug > 1) {
            System.out.println("stream is set to " + stream + " in getOutputStream");
        }
        return (stream);
    }

    /**
     * Return the writer associated with this Response.
     *
     * @exception IllegalStateException if <code>getOutputStream</code> has
     *  already been called for this response
     * @exception IOException if an input/output error occurs
     */
    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer != null) return (writer);
        if (stream != null) throw new IllegalStateException("getOutputStream() has already been called for this response");
        stream = createOutputStream();
        if (debug > 1) {
            System.out.println("stream is set to " + stream + " in getWriter");
        }
        String charEnc = origResponse.getCharacterEncoding();
        if (debug > 1) {
            System.out.println("character encoding is " + charEnc);
        }
        if (charEnc != null) {
            writer = new PrintWriter(new OutputStreamWriter(stream, charEnc));
        } else {
            writer = new PrintWriter(stream);
        }
        return (writer);
    }

    @Override
    public void setContentLength(int length) {
    }
}
