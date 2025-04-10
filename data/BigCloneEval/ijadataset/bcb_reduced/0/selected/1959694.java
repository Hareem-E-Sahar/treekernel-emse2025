package org.apache.james.mime4j.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.util.CharsetUtil;
import org.apache.james.mime4j.util.TempPath;
import org.apache.james.mime4j.util.TempStorage;

/**
 * Text body backed by a {@link org.apache.james.mime4j.util.TempFile}.
 *
 * 
 * @version $Id: TempFileTextBody.java,v 1.3 2004/10/25 07:26:46 ntherning Exp $
 */
class MemoryTextBody extends AbstractBody implements TextBody {

    private static Log log = LogFactory.getLog(MemoryTextBody.class);

    private String mimeCharset = null;

    private byte[] tempFile = null;

    public MemoryTextBody(InputStream is) throws IOException {
        this(is, null);
    }

    public MemoryTextBody(InputStream is, String mimeCharset) throws IOException {
        this.mimeCharset = mimeCharset;
        TempPath tempPath = TempStorage.getInstance().getRootTempPath();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(is, out);
        out.close();
        tempFile = out.toByteArray();
    }

    /**
     * @see org.apache.james.mime4j.message.TextBody#getReader()
     */
    public Reader getReader() throws UnsupportedEncodingException, IOException {
        String javaCharset = null;
        if (mimeCharset != null) {
            javaCharset = CharsetUtil.toJavaCharset(mimeCharset);
        }
        if (javaCharset == null) {
            javaCharset = "ISO-8859-1";
            if (log.isWarnEnabled()) {
                if (mimeCharset == null) {
                    log.warn("No MIME charset specified. Using " + javaCharset + " instead.");
                } else {
                    log.warn("MIME charset '" + mimeCharset + "' has no " + "corresponding Java charset. Using " + javaCharset + " instead.");
                }
            }
        }
        return new InputStreamReader(new ByteArrayInputStream(tempFile), javaCharset);
    }

    /**
     * @see org.apache.james.mime4j.message.Body#writeTo(java.io.OutputStream)
     */
    public void writeTo(OutputStream out) throws IOException {
        IOUtils.copy(new ByteArrayInputStream(tempFile), out);
    }
}
