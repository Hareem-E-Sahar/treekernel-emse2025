package net.sourceforge.javautil.common.encode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Simple a wrapper for {@link ZipInputStream} and {@link ZipOutputStream}.
 * 
 * @author elponderador
 * @author $Author$
 * @version $Id$
 */
public class ZipEncoding extends EncodingAlgorithmAbstract {

    protected final String entryName;

    public ZipEncoding(String entryName) {
        this.entryName = entryName;
    }

    @Override
    public OutputStream getEncoderStream(OutputStream target) throws IOException {
        ZipOutputStream output = new ZipOutputStream(target);
        output.putNextEntry(new ZipEntry(entryName));
        return output;
    }

    @Override
    public InputStream getDecoderStream(InputStream source) throws IOException {
        ZipInputStream zipped = new ZipInputStream(source);
        zipped.getNextEntry();
        return zipped;
    }
}
