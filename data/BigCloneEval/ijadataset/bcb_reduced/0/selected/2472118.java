package org.jaudiotagger.audio.mp4.atom;

import org.jaudiotagger.audio.exceptions.InvalidBoxHeaderException;
import org.jaudiotagger.audio.exceptions.NullBoxIdException;
import org.jaudiotagger.audio.generic.Utils;
import org.jaudiotagger.logging.ErrorMessage;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * Everything in MP4s are held in boxes (formally known as atoms), they are held as a hierachial tree within the MP4.
 * <p/>
 * We are most interested in boxes that are used to hold metadata, but we have to know about some other boxes
 * as well in order to find them.
 * <p/>
 * All boxes consist of a 4 byte box length (big Endian), and then a 4 byte identifier, this is the header
 * which is model in this class.
 * <p/>
 * The length includes the length of the box including the identifier and the length itself.
 * Then they may contain data and/or sub boxes, if they contain subboxes they are known as a parent box. Parent boxes
 * shouldn't really contain data, but sometimes they do.
 * <p/>
 * Parent boxes length includes the length of their immediate sub boxes
 * <p/>
 * This class is normally used by instantiating with the empty constructor, then use the update method
 * to pass the header data which is used to read the identifier and the the size of the box
 */
public class Mp4BoxHeader {

    public static Logger logger = Logger.getLogger("org.jaudiotagger.audio.mp4.atom");

    public static final int OFFSET_POS = 0;

    public static final int IDENTIFIER_POS = 4;

    public static final int OFFSET_LENGTH = 4;

    public static final int IDENTIFIER_LENGTH = 4;

    public static final int HEADER_LENGTH = OFFSET_LENGTH + IDENTIFIER_LENGTH;

    private String id;

    protected int length;

    private long filePos;

    protected ByteBuffer dataBuffer;

    public static final String CHARSET_UTF_8 = "UTF-8";

    /**
     * Construct empty header
     * <p/>
     * Can be populated later with update method
     */
    public Mp4BoxHeader() {
    }

    /**
     * Construct header to allow manual creation of header for writing to file
     * <p/>
      * @param id
      */
    public Mp4BoxHeader(String id) {
        if (id.length() != IDENTIFIER_LENGTH) {
            throw new RuntimeException("Invalid length:atom idenifier should always be 4 characters long");
        }
        dataBuffer = ByteBuffer.allocate(HEADER_LENGTH);
        try {
            this.id = id;
            dataBuffer.put(4, id.getBytes("ISO-8859-1")[0]);
            dataBuffer.put(5, id.getBytes("ISO-8859-1")[1]);
            dataBuffer.put(6, id.getBytes("ISO-8859-1")[2]);
            dataBuffer.put(7, id.getBytes("ISO-8859-1")[3]);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
    }

    /**
     * Construct header
     * <p/>
     * Create header using headerdata, expected to find header at headerdata current position
     * <p/>
     * Note after processing adjusts position to immediately after header
     *
     * @param headerData
     */
    public Mp4BoxHeader(ByteBuffer headerData) {
        update(headerData);
    }

    /**
     * Create header using headerdata, expected to find header at headerdata current position
     * <p/>
     * Note after processing adjusts position to immediately after header
     *
     * @param headerData
     */
    public void update(ByteBuffer headerData) {
        byte[] b = new byte[HEADER_LENGTH];
        headerData.get(b);
        dataBuffer = ByteBuffer.wrap(b);
        this.length = Utils.getIntBE(b, OFFSET_POS, OFFSET_LENGTH - 1);
        this.id = Utils.getString(b, IDENTIFIER_POS, IDENTIFIER_LENGTH, "ISO-8859-1");
        logger.finest("Mp4BoxHeader id:" + id + ":length:" + length);
        if (id.equals("\0\0\0\0")) {
            throw new NullBoxIdException(ErrorMessage.MP4_UNABLE_TO_FIND_NEXT_ATOM_BECAUSE_IDENTIFIER_IS_INVALID.getMsg(id));
        }
        if (length < HEADER_LENGTH) {
            throw new InvalidBoxHeaderException(ErrorMessage.MP4_UNABLE_TO_FIND_NEXT_ATOM_BECAUSE_IDENTIFIER_IS_INVALID.getMsg(id, length));
        }
    }

    /**
     * @return the box identifier
     */
    public String getId() {
        return id;
    }

    /**
     * @return the length of the boxes data (includes the header size)
     */
    public int getLength() {
        return length;
    }

    /**
     * Set the length.
     * <p/>
     * This will modify the databuffer accordingly
     *
     * @param length
     */
    public void setLength(int length) {
        byte[] headerSize = Utils.getSizeBEInt32(length);
        dataBuffer.put(0, headerSize[0]);
        dataBuffer.put(1, headerSize[1]);
        dataBuffer.put(2, headerSize[2]);
        dataBuffer.put(3, headerSize[3]);
        this.length = length;
    }

    /**
     * Set the Id.
     * <p/>
     * Allows you to manully create a header
     * This will modify the databuffer accordingly
     *
     * @param length
     */
    public void setId(int length) {
        byte[] headerSize = Utils.getSizeBEInt32(length);
        dataBuffer.put(5, headerSize[0]);
        dataBuffer.put(6, headerSize[1]);
        dataBuffer.put(7, headerSize[2]);
        dataBuffer.put(8, headerSize[3]);
        this.length = length;
    }

    /**
     * @return the 8 byte header buffer
     */
    public ByteBuffer getHeaderData() {
        dataBuffer.rewind();
        return dataBuffer;
    }

    /**
     * @return the length of the data only (does not include the header size)
     */
    public int getDataLength() {
        return length - HEADER_LENGTH;
    }

    public String toString() {
        return "Box " + id + ":length" + length + ":filepos:" + filePos;
    }

    /**
     * @return UTF_8 (always used by Mp4)
     */
    public String getEncoding() {
        return CHARSET_UTF_8;
    }

    /**
     * Seek for box with the specified id starting from the current location of filepointer,
     * <p/>
     * Note it wont find the box if it is contained with a level below the current level, nor if we are
     * at a parent atom that also contains data and we havent yet processed the data. It will work
     * if we are at the start of a child box even if it not the required box as long as the box we are
     * looking for is the same level (or the level above in some cases).
     *
     * @param raf
     * @param id
     * @throws java.io.IOException
     * @return
     */
    public static Mp4BoxHeader seekWithinLevel(RandomAccessFile raf, String id) throws IOException {
        logger.finer("Started searching for:" + id + " in file at:" + raf.getChannel().position());
        Mp4BoxHeader boxHeader = new Mp4BoxHeader();
        ByteBuffer headerBuffer = ByteBuffer.allocate(HEADER_LENGTH);
        int bytesRead = raf.getChannel().read(headerBuffer);
        if (bytesRead != HEADER_LENGTH) {
            return null;
        }
        headerBuffer.rewind();
        boxHeader.update(headerBuffer);
        while (!boxHeader.getId().equals(id)) {
            logger.finer("Found:" + boxHeader.getId() + " Still searching for:" + id + " in file at:" + raf.getChannel().position());
            if (boxHeader.getLength() < Mp4BoxHeader.HEADER_LENGTH) {
                return null;
            }
            int noOfBytesSkipped = raf.skipBytes(boxHeader.getDataLength());
            logger.finer("Skipped:" + noOfBytesSkipped);
            if (noOfBytesSkipped < boxHeader.getDataLength()) {
                return null;
            }
            headerBuffer.rewind();
            bytesRead = raf.getChannel().read(headerBuffer);
            logger.finer("Header Bytes Read:" + bytesRead);
            headerBuffer.rewind();
            if (bytesRead == Mp4BoxHeader.HEADER_LENGTH) {
                boxHeader.update(headerBuffer);
            } else {
                return null;
            }
        }
        return boxHeader;
    }

    /**
     * Seek for box with the specified id starting from the current location of filepointer,
     * <p/>
     * Note it won't find the box if it is contained with a level below the current level, nor if we are
     * at a parent atom that also contains data and we havent yet processed the data. It will work
     * if we are at the start of a child box even if it not the required box as long as the box we are
     * looking for is the same level (or the level above in some cases).
     *
     * @param data
     * @param id
     * @throws java.io.IOException
     * @return
     */
    public static Mp4BoxHeader seekWithinLevel(ByteBuffer data, String id) throws IOException {
        logger.finer("Started searching for:" + id + " in bytebuffer at" + data.position());
        Mp4BoxHeader boxHeader = new Mp4BoxHeader();
        if (data.remaining() >= Mp4BoxHeader.HEADER_LENGTH) {
            boxHeader.update(data);
        } else {
            return null;
        }
        while (!boxHeader.getId().equals(id)) {
            logger.finer("Found:" + boxHeader.getId() + " Still searching for:" + id + " in bytebuffer at" + data.position());
            if (boxHeader.getLength() < Mp4BoxHeader.HEADER_LENGTH) {
                return null;
            }
            if (data.remaining() < (boxHeader.getLength() - HEADER_LENGTH)) {
                return null;
            }
            data.position(data.position() + (boxHeader.getLength() - HEADER_LENGTH));
            if (data.remaining() >= Mp4BoxHeader.HEADER_LENGTH) {
                boxHeader.update(data);
            } else {
                return null;
            }
        }
        logger.finer("Found:" + id + " in bytebuffer at" + data.position());
        return boxHeader;
    }

    /**
     * @return location in file of the start of file header (i.e where the 4 byte length field starts)
     */
    public long getFilePos() {
        return filePos;
    }

    /**
     * Set location in file of the start of file header (i.e where the 4 byte length field starts)
     *
     * @param filePos
     */
    public void setFilePos(long filePos) {
        this.filePos = filePos;
    }
}
