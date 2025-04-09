package org.apache.log4j.compression.impl;

import org.apache.log4j.compression.AbstractBufferedOutputStreamCompressor;
import org.apache.log4j.compression.CompressionAlgorithm;
import org.apache.log4j.compression.Compressor;
import org.apache.log4j.helpers.LogLog;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ZIP implementation of {@link CompressionAlgorithm}. Support "compressionLevel" param.
 *
 * @author Dmitry Kozlov
 */
public class ZipCompressionAlgorithm extends CompressionAlgorithm {

    /**
     * ZIP implementation of {@link Compressor}.
     *
     * @author Dmitry Kozlov
     */
    public static class ZipCompressor extends AbstractBufferedOutputStreamCompressor<ZipOutputStream> {

        private int compressionLevel = Deflater.DEFAULT_COMPRESSION;

        public ZipCompressor(Map<String, Object> params) {
            super(params);
            Object levelObj = (params == null ? null : params.get(CompressionAlgorithm.COMPRESSION_LEVEL_PARAM));
            if (levelObj != null) {
                try {
                    compressionLevel = new Integer(levelObj.toString());
                } catch (NumberFormatException e) {
                    LogLog.warn("Couldn't convert specified value [" + levelObj + "] to compressionLevel", e);
                }
            }
        }

        protected ZipOutputStream createDeflaterStream(BufferedOutputStream out) {
            ZipOutputStream deflaterStream = new ZipOutputStream(out);
            deflaterStream.setLevel(compressionLevel);
            return deflaterStream;
        }

        @Override
        protected void preCompress(File in, ZipOutputStream deflaterStream) throws IOException {
            ZipEntry entry = new ZipEntry(in.getName());
            entry.setTime(in.lastModified());
            deflaterStream.putNextEntry(entry);
        }

        @Override
        protected void postCompress(File in, ZipOutputStream deflaterStream) throws IOException {
            deflaterStream.closeEntry();
        }
    }

    public static final String ALGORITHM_NAME = "ZIP";

    public static final String EXTENSION_SUFFIX = ".zip";

    public ZipCompressionAlgorithm() {
        super(ALGORITHM_NAME, EXTENSION_SUFFIX);
    }

    @Override
    public Compressor getConfiguredCompressor(Map<String, Object> params) {
        return new ZipCompressor(params);
    }
}
