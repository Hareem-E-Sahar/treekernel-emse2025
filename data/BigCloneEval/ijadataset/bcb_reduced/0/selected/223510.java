package org.apache.axis.attachments;

import org.apache.axis.components.logger.LogFactory;
import org.apache.axis.utils.Messages;
import org.apache.commons.logging.Log;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Holds one attachment DIME part.
 */
public class DimeBodyPart {

    protected static Log log = LogFactory.getLog(DimeBodyPart.class.getName());

    protected Object data = null;

    protected DimeTypeNameFormat dtnf = null;

    protected byte[] type = null;

    protected byte[] id = null;

    static final byte POSITION_FIRST = (byte) 0x04;

    static final byte POSITION_LAST = (byte) 0x02;

    private static final byte CHUNK = 0x01;

    private static final byte CHUNK_NEXT = 0x2;

    private static final byte ONLY_CHUNK = -1;

    private static final byte LAST_CHUNK = (byte) 0;

    private static int MAX_TYPE_LENGTH = (1 << 16) - 1;

    private static int MAX_ID_LENGTH = (1 << 16) - 1;

    static final long MAX_DWORD = 0xffffffffL;

    protected DimeBodyPart() {
    }

    /**
     * Create a DIME Attachment Part.
     * @param data a byte array containing the data as the attachment.
     * @param format the type format for the data.
     * @param type the type of the data
     * @param id  the ID for the DIME part.
     *
     */
    public DimeBodyPart(byte[] data, DimeTypeNameFormat format, String type, String id) {
        System.arraycopy(data, 0, this.data = new byte[data.length], 0, data.length);
        this.dtnf = format;
        this.type = type.getBytes();
        if (this.type.length > MAX_TYPE_LENGTH) throw new IllegalArgumentException(Messages.getMessage("attach.dimetypeexceedsmax", "" + this.type.length, "" + MAX_TYPE_LENGTH));
        this.id = id.getBytes();
        if (this.id.length > MAX_ID_LENGTH) throw new IllegalArgumentException(Messages.getMessage("attach.dimelengthexceedsmax", "" + this.id.length, "" + MAX_ID_LENGTH));
    }

    /**
     * Create a DIME Attachment Part.
     * @param dh the data for the attachment as a JAF datahadler.
     * @param format the type format for the data.
     * @param type the type of the data
     * @param id  the ID for the DIME part.
     *
     */
    public DimeBodyPart(DataHandler dh, DimeTypeNameFormat format, String type, String id) {
        this.data = dh;
        this.dtnf = format;
        if (type == null || type.length() == 0) type = "application/octet-stream";
        this.type = type.getBytes();
        if (this.type.length > MAX_TYPE_LENGTH) throw new IllegalArgumentException(Messages.getMessage("attach.dimetypeexceedsmax", "" + this.type.length, "" + MAX_TYPE_LENGTH));
        this.id = id.getBytes();
        if (this.id.length > MAX_ID_LENGTH) throw new IllegalArgumentException(Messages.getMessage("attach.dimelengthexceedsmax", "" + this.id.length, "" + MAX_ID_LENGTH));
    }

    /**
     * Create a DIME Attachment Part.
     * @param dh the data for the attachment as a JAF datahadler.
     *    The type and foramt is derived from the DataHandler.
     * @param id  the ID for the DIME part.
     *
     */
    public DimeBodyPart(DataHandler dh, String id) {
        this(dh, DimeTypeNameFormat.MIME, dh.getContentType(), id);
        String ct = dh.getContentType();
        if (ct != null) {
            ct = ct.trim();
            if (ct.toLowerCase().startsWith("application/uri")) {
                StringTokenizer st = new StringTokenizer(ct, " \t;");
                String t = st.nextToken(" \t;");
                if (t.equalsIgnoreCase("application/uri")) {
                    for (; st.hasMoreTokens(); ) {
                        t = st.nextToken(" \t;");
                        if (t.equalsIgnoreCase("uri")) {
                            t = st.nextToken("=");
                            if (t != null) {
                                t = t.trim();
                                if (t.startsWith("\"")) t = t.substring(1);
                                if (t.endsWith("\"")) t = t.substring(0, t.length() - 1);
                                this.type = t.getBytes();
                                this.dtnf = DimeTypeNameFormat.URI;
                            }
                            return;
                        } else if (t.equalsIgnoreCase("uri=")) {
                            t = st.nextToken(" \t;");
                            if (null != t && t.length() != 0) {
                                t = t.trim();
                                if (t.startsWith("\"")) t = t.substring(1);
                                if (t.endsWith("\"")) t = t.substring(0, t.length() - 1);
                                this.type = t.getBytes();
                                this.dtnf = DimeTypeNameFormat.URI;
                                return;
                            }
                        } else if (t.toLowerCase().startsWith("uri=")) {
                            if (-1 != t.indexOf('=')) {
                                t = t.substring(t.indexOf('=')).trim();
                                if (t.length() != 0) {
                                    t = t.trim();
                                    if (t.startsWith("\"")) t = t.substring(1);
                                    if (t.endsWith("\"")) t = t.substring(0, t.length() - 1);
                                    this.type = t.getBytes();
                                    this.dtnf = DimeTypeNameFormat.URI;
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Write to stream the data using maxchunk for the largest junk.
     *
     * @param os        the <code>OutputStream</code> to write to
     * @param position  the position to write
     * @param maxchunk  the maximum length of any one chunk
     * @throws IOException if there was a problem writing data to the stream
     */
    void write(java.io.OutputStream os, byte position, long maxchunk) throws java.io.IOException {
        if (maxchunk < 1) throw new IllegalArgumentException(Messages.getMessage("attach.dimeMaxChunkSize0", "" + maxchunk));
        if (maxchunk > MAX_DWORD) throw new IllegalArgumentException(Messages.getMessage("attach.dimeMaxChunkSize1", "" + maxchunk));
        if (data instanceof byte[]) {
            send(os, position, (byte[]) data, maxchunk);
        } else if (data instanceof DynamicContentDataHandler) {
            send(os, position, (DynamicContentDataHandler) data, maxchunk);
        } else if (data instanceof DataHandler) {
            DataSource source = ((DataHandler) data).getDataSource();
            DynamicContentDataHandler dh2 = new DynamicContentDataHandler(source);
            send(os, position, dh2, maxchunk);
        }
    }

    /**
     * Write to stream the data using the default largest chunk size.
     *
     * @param os  the <code>OutputStream</code> to write to
     * @param position  the position to write
     * @throws IOException if there was a problem writing data to the stream
     */
    void write(java.io.OutputStream os, byte position) throws java.io.IOException {
        write(os, position, MAX_DWORD);
    }

    private static final byte[] pad = new byte[4];

    void send(java.io.OutputStream os, byte position, byte[] data, final long maxchunk) throws java.io.IOException {
        send(os, position, data, 0, data.length, maxchunk);
    }

    void send(java.io.OutputStream os, byte position, byte[] data, int offset, final int length, final long maxchunk) throws java.io.IOException {
        byte chunknext = 0;
        do {
            int sendlength = (int) Math.min(maxchunk, length - offset);
            sendChunk(os, position, data, offset, sendlength, (byte) ((sendlength < (length - offset) ? CHUNK : 0) | chunknext));
            offset += sendlength;
            chunknext = CHUNK_NEXT;
        } while (offset < length);
    }

    void send(java.io.OutputStream os, byte position, DataHandler dh, final long maxchunk) throws java.io.IOException {
        java.io.InputStream in = null;
        try {
            long dataSize = getDataSize();
            in = dh.getInputStream();
            byte[] readbuf = new byte[64 * 1024];
            int bytesread;
            sendHeader(os, position, dataSize, (byte) 0);
            long totalsent = 0;
            do {
                bytesread = in.read(readbuf);
                if (bytesread > 0) {
                    os.write(readbuf, 0, bytesread);
                    totalsent += bytesread;
                }
            } while (bytesread > -1);
            os.write(pad, 0, dimePadding(totalsent));
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Special case for dynamically generated content. 
     * maxchunk is currently ignored since the default is 2GB.
     * The chunk size is retrieved from the DynamicContentDataHandler
     * 
     * @param os
     * @param position
     * @param dh
     * @param maxchunk
     * @throws java.io.IOException
     */
    void send(java.io.OutputStream os, byte position, DynamicContentDataHandler dh, final long maxchunk) throws java.io.IOException {
        BufferedInputStream in = new BufferedInputStream(dh.getInputStream());
        final int myChunkSize = dh.getChunkSize();
        byte[] buffer1 = new byte[myChunkSize];
        byte[] buffer2 = new byte[myChunkSize];
        int bytesRead1 = 0, bytesRead2 = 0;
        bytesRead1 = in.read(buffer1);
        if (bytesRead1 < 0) {
            sendHeader(os, position, 0, ONLY_CHUNK);
            os.write(pad, 0, dimePadding(0));
            return;
        }
        byte chunkbyte = CHUNK;
        do {
            bytesRead2 = in.read(buffer2);
            if (bytesRead2 < 0) {
                if (chunkbyte == CHUNK) {
                    chunkbyte = ONLY_CHUNK;
                } else {
                    chunkbyte = LAST_CHUNK;
                }
                sendChunk(os, position, buffer1, 0, bytesRead1, chunkbyte);
                break;
            }
            sendChunk(os, position, buffer1, 0, bytesRead1, chunkbyte);
            chunkbyte = CHUNK_NEXT;
            System.arraycopy(buffer2, 0, buffer1, 0, myChunkSize);
            bytesRead1 = bytesRead2;
        } while (bytesRead2 > 0);
    }

    protected void sendChunk(java.io.OutputStream os, final byte position, byte[] data, byte chunk) throws java.io.IOException {
        sendChunk(os, position, data, 0, data.length, chunk);
    }

    protected void sendChunk(java.io.OutputStream os, final byte position, byte[] data, int offset, int length, byte chunk) throws java.io.IOException {
        sendHeader(os, position, length, chunk);
        os.write(data, offset, length);
        os.write(pad, 0, dimePadding(length));
    }

    static final byte CURRENT_OPT_T = (byte) 0;

    protected void sendHeader(java.io.OutputStream os, final byte position, long length, byte chunk) throws java.io.IOException {
        byte[] fixedHeader = new byte[12];
        boolean isFirstChunk = ((chunk == CHUNK) || (chunk == ONLY_CHUNK));
        if (chunk == CHUNK_NEXT) {
            chunk = CHUNK;
        } else if (chunk == ONLY_CHUNK) {
            chunk = LAST_CHUNK;
        }
        fixedHeader[0] = (byte) ((DimeMultiPart.CURRENT_VERSION << 3) & 0xf8);
        fixedHeader[0] |= (byte) ((position & (byte) 0x6) & ((chunk & CHUNK) != 0 ? ~POSITION_LAST : ~0) & ((chunk & CHUNK_NEXT) != 0 ? ~POSITION_FIRST : ~0));
        fixedHeader[0] |= (chunk & CHUNK);
        boolean MB = 0 != (0x4 & fixedHeader[0]);
        if (MB || isFirstChunk) {
            fixedHeader[1] = (byte) ((dtnf.toByte() << 4) & 0xf0);
        } else {
            fixedHeader[1] = (byte) 0x00;
        }
        fixedHeader[1] |= (byte) (CURRENT_OPT_T & 0xf);
        fixedHeader[2] = (byte) 0;
        fixedHeader[3] = (byte) 0;
        if ((MB || isFirstChunk) && (id != null && id.length > 0)) {
            fixedHeader[4] = (byte) ((id.length >>> 8) & 0xff);
            fixedHeader[5] = (byte) ((id.length) & 0xff);
        } else {
            fixedHeader[4] = (byte) 0;
            fixedHeader[5] = (byte) 0;
        }
        if (MB || isFirstChunk) {
            fixedHeader[6] = (byte) ((type.length >>> 8) & 0xff);
            fixedHeader[7] = (byte) ((type.length) & 0xff);
        } else {
            fixedHeader[6] = (byte) 0;
            fixedHeader[7] = (byte) 0;
        }
        fixedHeader[8] = (byte) ((length >>> 24) & 0xff);
        fixedHeader[9] = (byte) ((length >>> 16) & 0xff);
        fixedHeader[10] = (byte) ((length >>> 8) & 0xff);
        fixedHeader[11] = (byte) (length & 0xff);
        os.write(fixedHeader);
        if ((MB || isFirstChunk) && (id != null && id.length > 0)) {
            os.write(id);
            os.write(pad, 0, dimePadding(id.length));
        }
        if (MB || isFirstChunk) {
            os.write(type);
            os.write(pad, 0, dimePadding(type.length));
        }
    }

    static final int dimePadding(long l) {
        return (int) ((4L - (l & 0x3L)) & 0x03L);
    }

    long getTransmissionSize(long chunkSize) {
        long size = 0;
        size += id.length;
        size += dimePadding(id.length);
        size += type.length;
        size += dimePadding(type.length);
        long dataSize = getDataSize();
        if (0 == dataSize) {
            size += 12;
        } else {
            long fullChunks = dataSize / chunkSize;
            long lastChunkSize = dataSize % chunkSize;
            if (0 != lastChunkSize) size += 12;
            size += 12 * fullChunks;
            size += fullChunks * dimePadding(chunkSize);
            size += dimePadding(lastChunkSize);
            size += dataSize;
        }
        return size;
    }

    long getTransmissionSize() {
        return getTransmissionSize(MAX_DWORD);
    }

    protected long getDataSize() {
        if (data instanceof byte[]) return ((byte[]) (data)).length;
        if (data instanceof DataHandler) return getDataSize((DataHandler) data);
        return -1;
    }

    protected long getDataSize(DataHandler dh) {
        long dataSize = -1L;
        try {
            DataSource ds = dh.getDataSource();
            if (ds instanceof javax.activation.FileDataSource) {
                javax.activation.FileDataSource fdh = (javax.activation.FileDataSource) ds;
                java.io.File df = fdh.getFile();
                if (!df.exists()) {
                    throw new RuntimeException(Messages.getMessage("noFile", df.getAbsolutePath()));
                }
                dataSize = df.length();
            } else {
                dataSize = 0;
                java.io.InputStream in = ds.getInputStream();
                byte[] readbuf = new byte[64 * 1024];
                int bytesread;
                do {
                    bytesread = in.read(readbuf);
                    if (bytesread > 0) dataSize += bytesread;
                } while (bytesread > -1);
                if (in.markSupported()) {
                    in.reset();
                } else {
                    in.close();
                }
            }
        } catch (Exception e) {
            log.error(Messages.getMessage("exception00"), e);
        }
        return dataSize;
    }
}
