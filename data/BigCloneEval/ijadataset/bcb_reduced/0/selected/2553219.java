package org.h2.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.h2.compress.CompressDeflate;
import org.h2.compress.CompressLZF;
import org.h2.compress.CompressNo;
import org.h2.compress.Compressor;
import org.h2.compress.LZFInputStream;
import org.h2.compress.LZFOutputStream;
import org.h2.constant.ErrorCode;
import org.h2.engine.Constants;
import org.h2.message.Message;
import org.h2.util.MemoryUtils;
import org.h2.util.StringUtils;

/**
 * A tool to losslessly compress data, and expand the compressed data again.
 */
public class CompressTool {

    private static final CompressTool INSTANCE = new CompressTool();

    private static final int MAX_BUFFER_SIZE = 3 * Constants.IO_BUFFER_SIZE_COMPRESS;

    private byte[] cachedBuffer;

    private CompressTool() {
    }

    private byte[] getBuffer(int min) {
        if (min > MAX_BUFFER_SIZE) {
            return MemoryUtils.newBytes(min);
        }
        if (cachedBuffer == null || cachedBuffer.length < min) {
            cachedBuffer = MemoryUtils.newBytes(min);
        }
        return cachedBuffer;
    }

    /**
     * Get the singleton.
     *
     * @return the singleton
     */
    public static CompressTool getInstance() {
        return INSTANCE;
    }

    /**
     * Compressed the data using the specified algorithm. If no algorithm is
     * supplied, LZF is used
     *
     * @param in the byte array with the original data
     * @param algorithm the algorithm (LZF, DEFLATE)
     * @return the compressed data
     * @throws SQLException if a error occurs
     */
    public byte[] compress(byte[] in, String algorithm) throws SQLException {
        int len = in.length;
        if (in.length < 5) {
            algorithm = "NO";
        }
        Compressor compress = getCompressor(algorithm);
        byte[] buff = getBuffer((len < 100 ? len + 100 : len) * 2);
        int newLen = compress(in, in.length, compress, buff);
        byte[] out = MemoryUtils.newBytes(newLen);
        System.arraycopy(buff, 0, out, 0, newLen);
        return out;
    }

    /**
     * INTERNAL
     */
    public synchronized int compress(byte[] in, int len, Compressor compress, byte[] out) {
        int newLen = 0;
        out[0] = (byte) compress.getAlgorithm();
        int start = 1 + writeInt(out, 1, len);
        newLen = compress.compress(in, len, out, start);
        if (newLen > len + start || newLen <= 0) {
            out[0] = Compressor.NO;
            System.arraycopy(in, 0, out, start, len);
            newLen = len + start;
        }
        return newLen;
    }

    /**
     * Expands the compressed  data.
     *
     * @param in the byte array with the compressed data
     * @return the uncompressed data
     * @throws SQLException if a error occurs
     */
    public byte[] expand(byte[] in) throws SQLException {
        int algorithm = in[0];
        Compressor compress = getCompressor(algorithm);
        try {
            int len = readInt(in, 1);
            int start = 1 + getLength(len);
            byte[] buff = MemoryUtils.newBytes(len);
            compress.expand(in, start, in.length - start, buff, 0, len);
            return buff;
        } catch (Exception e) {
            throw Message.getSQLException(ErrorCode.COMPRESSION_ERROR, e);
        }
    }

    /**
     * INTERNAL
     */
    public void expand(byte[] in, byte[] out, int outPos) throws SQLException {
        int algorithm = in[0];
        Compressor compress = getCompressor(algorithm);
        try {
            int len = readInt(in, 1);
            int start = 1 + getLength(len);
            compress.expand(in, start, in.length - start, out, outPos, len);
        } catch (Exception e) {
            throw Message.getSQLException(ErrorCode.COMPRESSION_ERROR, e);
        }
    }

    private int readInt(byte[] buff, int pos) {
        int x = buff[pos++] & 0xff;
        if (x < 0x80) {
            return x;
        }
        if (x < 0xc0) {
            return ((x & 0x3f) << 8) + (buff[pos] & 0xff);
        }
        if (x < 0xe0) {
            return ((x & 0x1f) << 16) + ((buff[pos++] & 0xff) << 8) + (buff[pos] & 0xff);
        }
        if (x < 0xf0) {
            return ((x & 0xf) << 24) + ((buff[pos++] & 0xff) << 16) + ((buff[pos++] & 0xff) << 8) + (buff[pos] & 0xff);
        }
        return ((buff[pos++] & 0xff) << 24) + ((buff[pos++] & 0xff) << 16) + ((buff[pos++] & 0xff) << 8) + (buff[pos] & 0xff);
    }

    private int writeInt(byte[] buff, int pos, int x) {
        if (x < 0) {
            buff[pos++] = (byte) 0xf0;
            buff[pos++] = (byte) (x >> 24);
            buff[pos++] = (byte) (x >> 16);
            buff[pos++] = (byte) (x >> 8);
            buff[pos] = (byte) x;
            return 5;
        } else if (x < 0x80) {
            buff[pos] = (byte) x;
            return 1;
        } else if (x < 0x4000) {
            buff[pos++] = (byte) (0x80 | (x >> 8));
            buff[pos] = (byte) x;
            return 2;
        } else if (x < 0x200000) {
            buff[pos++] = (byte) (0xc0 | (x >> 16));
            buff[pos++] = (byte) (x >> 8);
            buff[pos] = (byte) x;
            return 3;
        } else if (x < 0x10000000) {
            buff[pos++] = (byte) (0xe0 | (x >> 24));
            buff[pos++] = (byte) (x >> 16);
            buff[pos++] = (byte) (x >> 8);
            buff[pos] = (byte) x;
            return 4;
        } else {
            buff[pos++] = (byte) 0xf0;
            buff[pos++] = (byte) (x >> 24);
            buff[pos++] = (byte) (x >> 16);
            buff[pos++] = (byte) (x >> 8);
            buff[pos] = (byte) x;
            return 5;
        }
    }

    private int getLength(int x) {
        if (x < 0) {
            return 5;
        } else if (x < 0x80) {
            return 1;
        } else if (x < 0x4000) {
            return 2;
        } else if (x < 0x200000) {
            return 3;
        } else if (x < 0x10000000) {
            return 4;
        } else {
            return 5;
        }
    }

    private Compressor getCompressor(String algorithm) throws SQLException {
        if (algorithm == null) {
            algorithm = "LZF";
        }
        int idx = algorithm.indexOf(' ');
        String options = null;
        if (idx > 0) {
            options = algorithm.substring(idx + 1);
            algorithm = algorithm.substring(0, idx);
        }
        int a = getCompressAlgorithm(algorithm);
        Compressor compress = getCompressor(a);
        compress.setOptions(options);
        return compress;
    }

    /**
     * INTERNAL
     */
    public int getCompressAlgorithm(String algorithm) throws SQLException {
        algorithm = StringUtils.toUpperEnglish(algorithm);
        if ("NO".equals(algorithm)) {
            return Compressor.NO;
        } else if ("LZF".equals(algorithm)) {
            return Compressor.LZF;
        } else if ("DEFLATE".equals(algorithm)) {
            return Compressor.DEFLATE;
        } else {
            throw Message.getSQLException(ErrorCode.UNSUPPORTED_COMPRESSION_ALGORITHM_1, algorithm);
        }
    }

    private Compressor getCompressor(int algorithm) throws SQLException {
        switch(algorithm) {
            case Compressor.NO:
                return new CompressNo();
            case Compressor.LZF:
                return new CompressLZF();
            case Compressor.DEFLATE:
                return new CompressDeflate();
            default:
                throw Message.getSQLException(ErrorCode.UNSUPPORTED_COMPRESSION_ALGORITHM_1, "" + algorithm);
        }
    }

    /**
     * INTERNAL
     */
    public static OutputStream wrapOutputStream(OutputStream out, String compressionAlgorithm, String entryName) throws SQLException {
        try {
            if ("GZIP".equals(compressionAlgorithm)) {
                out = new GZIPOutputStream(out);
            } else if ("ZIP".equals(compressionAlgorithm)) {
                ZipOutputStream z = new ZipOutputStream(out);
                z.putNextEntry(new ZipEntry(entryName));
                out = z;
            } else if ("DEFLATE".equals(compressionAlgorithm)) {
                out = new DeflaterOutputStream(out);
            } else if ("LZF".equals(compressionAlgorithm)) {
                out = new LZFOutputStream(out);
            } else if (compressionAlgorithm != null) {
                throw Message.getSQLException(ErrorCode.UNSUPPORTED_COMPRESSION_ALGORITHM_1, compressionAlgorithm);
            }
            return out;
        } catch (IOException e) {
            throw Message.convertIOException(e, null);
        }
    }

    /**
     * INTERNAL
     */
    public static InputStream wrapInputStream(InputStream in, String compressionAlgorithm, String entryName) throws SQLException {
        try {
            if ("GZIP".equals(compressionAlgorithm)) {
                in = new GZIPInputStream(in);
            } else if ("ZIP".equals(compressionAlgorithm)) {
                ZipInputStream z = new ZipInputStream(in);
                while (true) {
                    ZipEntry entry = z.getNextEntry();
                    if (entry == null) {
                        return null;
                    }
                    if (entryName.equals(entry.getName())) {
                        break;
                    }
                }
                in = z;
            } else if ("DEFLATE".equals(compressionAlgorithm)) {
                in = new InflaterInputStream(in);
            } else if ("LZF".equals(compressionAlgorithm)) {
                in = new LZFInputStream(in);
            } else if (compressionAlgorithm != null) {
                throw Message.getSQLException(ErrorCode.UNSUPPORTED_COMPRESSION_ALGORITHM_1, compressionAlgorithm);
            }
            return in;
        } catch (IOException e) {
            throw Message.convertIOException(e, null);
        }
    }
}
