package com.hadeslee.audiotag.tag.id3;

import com.hadeslee.audiotag.FileConstants;
import com.hadeslee.audiotag.audio.mp3.MP3File;
import com.hadeslee.audiotag.tag.EmptyFrameException;
import com.hadeslee.audiotag.tag.FieldDataInvalidException;
import com.hadeslee.audiotag.tag.InvalidFrameException;
import com.hadeslee.audiotag.tag.InvalidFrameIdentifierException;
import com.hadeslee.audiotag.tag.InvalidTagException;
import com.hadeslee.audiotag.tag.KeyNotFoundException;
import com.hadeslee.audiotag.tag.TagException;
import com.hadeslee.audiotag.tag.TagField;
import com.hadeslee.audiotag.tag.TagFieldKey;
import com.hadeslee.audiotag.tag.TagNotFoundException;
import com.hadeslee.audiotag.tag.TagOptionSingleton;
import com.hadeslee.audiotag.tag.id3.framebody.FrameBodyTDAT;
import com.hadeslee.audiotag.tag.id3.framebody.FrameBodyTDRC;
import com.hadeslee.audiotag.tag.id3.framebody.FrameBodyTIME;
import com.hadeslee.audiotag.tag.id3.framebody.FrameBodyTYER;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.*;
import java.util.logging.Level;

/**
 * Represents an ID3v2.3 tag.
 * 
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id: ID3v23Tag.java,v 1.31 2007/11/13 14:24:31 paultaylor Exp $
 */
public class ID3v23Tag extends AbstractID3v2Tag {

    protected static final String TYPE_CRCDATA = "crcdata";

    protected static final String TYPE_EXPERIMENTAL = "experimental";

    protected static final String TYPE_EXTENDED = "extended";

    protected static final String TYPE_PADDINGSIZE = "paddingsize";

    protected static final String TYPE_UNSYNCHRONISATION = "unsyncronisation";

    protected static int TAG_EXT_HEADER_LENGTH = 10;

    protected static int TAG_EXT_HEADER_CRC_LENGTH = 4;

    protected static int FIELD_TAG_EXT_SIZE_LENGTH = 4;

    protected static int TAG_EXT_HEADER_DATA_LENGTH = TAG_EXT_HEADER_LENGTH - FIELD_TAG_EXT_SIZE_LENGTH;

    /**
     * ID3v2.3 Header bit mask
     */
    public static final int MASK_V23_UNSYNCHRONIZATION = FileConstants.BIT7;

    /**
     * ID3v2.3 Header bit mask
     */
    public static final int MASK_V23_EXTENDED_HEADER = FileConstants.BIT6;

    /**
     * ID3v2.3 Header bit mask
     */
    public static final int MASK_V23_EXPERIMENTAL = FileConstants.BIT5;

    /**
     * ID3v2.3 Extended Header bit mask
     */
    public static final int MASK_V23_CRC_DATA_PRESENT = FileConstants.BIT7;

    /**
     * ID3v2.3 RBUF frame bit mask
     */
    public static final int MASK_V23_EMBEDDED_INFO_FLAG = FileConstants.BIT1;

    /**
     * CRC Checksum calculated
     */
    protected boolean crcDataFlag = false;

    /**
     * Experiemntal tag
     */
    protected boolean experimental = false;

    /**
     * Contains extended header
     */
    protected boolean extended = false;

    /**
     * CRC Checksum
     */
    protected int crcData = 0;

    /**
     * Tag padding
     */
    protected int paddingSize = 0;

    /**
     * All frames in the tag uses unsynchronisation
     */
    protected boolean unsynchronization = false;

    /**
     * The tag is compressed
     */
    protected boolean compression = false;

    public static final byte RELEASE = 2;

    public static final byte MAJOR_VERSION = 3;

    public static final byte REVISION = 0;

    /**
     * Retrieve the Release
     */
    public byte getRelease() {
        return RELEASE;
    }

    /**
     * Retrieve the Major Version
     */
    public byte getMajorVersion() {
        return MAJOR_VERSION;
    }

    /**
     * Retrieve the Revision
     */
    public byte getRevision() {
        return REVISION;
    }

    /**
     * Creates a new empty ID3v2_3 datatype.
     */
    public ID3v23Tag() {
        frameMap = new LinkedHashMap();
    }

    /**
     * Copy primitives applicable to v2.3
     */
    protected void copyPrimitives(AbstractID3v2Tag copyObj) {
        logger.info("Copying primitives");
        super.copyPrimitives(copyObj);
        if (copyObj instanceof ID3v23Tag) {
            ID3v23Tag copyObject = (ID3v23Tag) copyObj;
            this.crcDataFlag = copyObject.crcDataFlag;
            this.experimental = copyObject.experimental;
            this.extended = copyObject.extended;
            this.crcData = copyObject.crcData;
            this.paddingSize = copyObject.paddingSize;
        }
    }

    /**
    * Copy frames from one tag into a v2.3 tag
    *
    * @param copyObject
    */
    protected void copyFrames(AbstractID3v2Tag copyObject) {
        logger.info("Copying Frames,there are:" + copyObject.frameMap.keySet().size() + " different types");
        frameMap = new LinkedHashMap();
        Iterator iterator = copyObject.frameMap.keySet().iterator();
        AbstractID3v2Frame frame;
        ID3v23Frame newFrame = null;
        while (iterator.hasNext()) {
            String id = (String) iterator.next();
            Object o = copyObject.frameMap.get(id);
            if (o instanceof AbstractID3v2Frame) {
                frame = (AbstractID3v2Frame) o;
                logger.info("Frame is:" + frame.getIdentifier());
                if ((frame.getIdentifier().equals(ID3v24Frames.FRAME_ID_YEAR)) && (frame.getBody() instanceof FrameBodyTDRC)) {
                    translateFrame(frame);
                } else {
                    try {
                        newFrame = new ID3v23Frame(frame);
                        logger.info("Adding Frame:" + newFrame.getIdentifier());
                        frameMap.put(newFrame.getIdentifier(), newFrame);
                    } catch (InvalidFrameException ife) {
                        logger.log(Level.SEVERE, "Unable to convert frame:" + frame.getIdentifier());
                    }
                }
            } else if (o instanceof ArrayList) {
                ArrayList multiFrame = new ArrayList();
                for (ListIterator li = ((ArrayList) o).listIterator(); li.hasNext(); ) {
                    frame = (AbstractID3v2Frame) li.next();
                    logger.info("Frame is MultiFrame:" + frame.getIdentifier());
                    try {
                        newFrame = new ID3v23Frame(frame);
                        multiFrame.add(newFrame);
                    } catch (InvalidFrameException ife) {
                        logger.log(Level.SEVERE, "Unable to convert frame:" + frame.getIdentifier(), ife);
                    }
                }
                if (newFrame != null) {
                    logger.info("Adding MultiFrame:" + newFrame.getIdentifier());
                    frameMap.put(newFrame.getIdentifier(), multiFrame);
                }
            }
        }
    }

    /**
     * This is used when we need to translate a single frame into multiple frames,
     * currently required for v24 TDRC frames.
     */
    protected void translateFrame(AbstractID3v2Frame frame) {
        FrameBodyTDRC tmpBody = (FrameBodyTDRC) frame.getBody();
        ID3v23Frame newFrame;
        if (!tmpBody.getYear().equals("")) {
            newFrame = new ID3v23Frame(ID3v23Frames.FRAME_ID_V3_TYER);
            ((FrameBodyTYER) newFrame.getBody()).setText(tmpBody.getYear());
            logger.info("Adding Frame:" + newFrame.getIdentifier());
            frameMap.put(newFrame.getIdentifier(), newFrame);
        }
        if (!tmpBody.getDate().equals("")) {
            newFrame = new ID3v23Frame(ID3v23Frames.FRAME_ID_V3_TDAT);
            ((FrameBodyTDAT) newFrame.getBody()).setText(tmpBody.getDate());
            logger.info("Adding Frame:" + newFrame.getIdentifier());
            frameMap.put(newFrame.getIdentifier(), newFrame);
        }
        if (!tmpBody.getTime().equals("")) {
            newFrame = new ID3v23Frame(ID3v23Frames.FRAME_ID_V3_TIME);
            ((FrameBodyTIME) newFrame.getBody()).setText(tmpBody.getTime());
            logger.info("Adding Frame:" + newFrame.getIdentifier());
            frameMap.put(newFrame.getIdentifier(), newFrame);
        }
    }

    /**
     * Copy Constructor, creates a new ID3v2_3 Tag based on another ID3v2_3 Tag
     */
    public ID3v23Tag(ID3v23Tag copyObject) {
        super(copyObject);
        logger.info("Creating tag from another tag of same type");
        copyPrimitives(copyObject);
        copyFrames(copyObject);
    }

    /**
     * Constructs a new tag based upon another tag of different version/type
     */
    public ID3v23Tag(AbstractTag mp3tag) {
        logger.info("Creating tag from a tag of a different version");
        frameMap = new LinkedHashMap();
        if (mp3tag != null) {
            ID3v24Tag convertedTag;
            if ((mp3tag instanceof ID3v24Tag == false) && (mp3tag instanceof ID3v23Tag == true)) {
                throw new UnsupportedOperationException("Copy Constructor not called. Please type cast the argument");
            }
            if (mp3tag instanceof ID3v24Tag) {
                convertedTag = (ID3v24Tag) mp3tag;
            } else {
                convertedTag = new ID3v24Tag(mp3tag);
            }
            copyPrimitives(convertedTag);
            copyFrames(convertedTag);
            logger.info("Created tag from a tag of a different version");
        }
    }

    /**
     * Creates a new ID3v2_3 datatype.
     *
     * @param buffer
     * @param loggingFilename
     * @throws TagException
     */
    public ID3v23Tag(ByteBuffer buffer, String loggingFilename) throws TagException {
        setLoggingFilename(loggingFilename);
        this.read(buffer);
    }

    /**
     * Creates a new ID3v2_3 datatype.
     *
     * @param buffer
     * @throws TagException
     *
     * @deprecated use {@link #ID3v23Tag(ByteBuffer,String)} instead
     */
    public ID3v23Tag(ByteBuffer buffer) throws TagException {
        this(buffer, "");
    }

    /**
     * 
     *
     * @return textual tag identifier
     */
    public String getIdentifier() {
        return "ID3v2.30";
    }

    /**
     * Return frame size based upon the sizes of the tags rather than the physical
     * no of bytes between start of ID3Tag and start of Audio Data.
     *
     * TODO this is incorrect, because of subclasses
     * 
     * @return size of tag
     */
    public int getSize() {
        int size = TAG_HEADER_LENGTH;
        if (extended) {
            size += this.TAG_EXT_HEADER_LENGTH;
            if (crcDataFlag) {
                size += this.TAG_EXT_HEADER_CRC_LENGTH;
            }
        }
        size += super.getSize();
        return size;
    }

    /**
     * Is Tag Equivalent to another tag
     *
     * @param obj 
     * @return true if tag is equivalent to another
     */
    public boolean equals(Object obj) {
        if ((obj instanceof ID3v23Tag) == false) {
            return false;
        }
        ID3v23Tag object = (ID3v23Tag) obj;
        if (this.crcData != object.crcData) {
            return false;
        }
        if (this.crcDataFlag != object.crcDataFlag) {
            return false;
        }
        if (this.experimental != object.experimental) {
            return false;
        }
        if (this.extended != object.extended) {
            return false;
        }
        if (this.paddingSize != object.paddingSize) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Read tag from File
     *
     * @param buffer The buffer to read the ID3v23 Tag from
     *
     */
    public void read(ByteBuffer buffer) throws TagException {
        int size;
        if (seek(buffer) == false) {
            throw new TagNotFoundException(getIdentifier() + " tag not found");
        }
        logger.info(getLoggingFilename() + ":" + "Reading tag");
        byte flags = buffer.get();
        unsynchronization = (flags & MASK_V23_UNSYNCHRONIZATION) != 0;
        extended = (flags & MASK_V23_EXTENDED_HEADER) != 0;
        experimental = (flags & MASK_V23_EXPERIMENTAL) != 0;
        if (isUnsynchronization()) {
            logger.warning(getLoggingFilename() + ":" + "ID3v23 Tag is unsynchronized");
        }
        if (extended) {
            logger.warning(getLoggingFilename() + ":" + "ID3v23 Tag is extended");
        }
        if (experimental) {
            logger.warning(getLoggingFilename() + ":" + "ID3v23 Tag is experimental");
        }
        size = ID3SyncSafeInteger.bufferToValue(buffer);
        logger.info(getLoggingFilename() + ":Tag size is:" + size + " according to header (does not include header size, add 10)");
        if (extended == true) {
            int extendedHeaderSize = buffer.getInt();
            if (extendedHeaderSize == TAG_EXT_HEADER_DATA_LENGTH) {
                byte extFlag = buffer.get();
                crcDataFlag = (extFlag & MASK_V23_CRC_DATA_PRESENT) != 0;
                if (crcDataFlag == true) {
                    throw new InvalidTagException(getLoggingFilename() + ":CRC Data flag not set correctly.");
                }
                buffer.get();
                size = size - (buffer.getInt() + TAG_EXT_HEADER_LENGTH);
            } else if (extendedHeaderSize == TAG_EXT_HEADER_DATA_LENGTH + TAG_EXT_HEADER_CRC_LENGTH) {
                byte extFlag = buffer.get();
                crcDataFlag = (extFlag & MASK_V23_CRC_DATA_PRESENT) != 0;
                if (crcDataFlag == false) {
                    throw new InvalidTagException(getLoggingFilename() + ":CRC Data flag not set correctly.");
                }
                buffer.get();
                size = size - (buffer.getInt() + TAG_EXT_HEADER_LENGTH + TAG_EXT_HEADER_CRC_LENGTH);
                crcData = buffer.getInt();
            } else {
                throw new InvalidTagException("Invalid Extended Header Size.");
            }
            logger.info(getLoggingFilename() + ":has Extended Header so adjusted Tag size is:" + size);
        }
        ByteBuffer bufferWithoutHeader = buffer.slice();
        if (isUnsynchronization() == true) {
            bufferWithoutHeader = ID3Unsynchronization.synchronize(bufferWithoutHeader);
        }
        readFrames(bufferWithoutHeader, size);
        logger.info(getLoggingFilename() + ":Loaded Frames,there are:" + frameMap.keySet().size());
    }

    /**
     * Read the frames
     *
     * Read from byteBuffer upto size
     *
     * @param byteBuffer
     * @param size
     */
    protected void readFrames(ByteBuffer byteBuffer, int size) {
        ID3v23Frame next;
        frameMap = new LinkedHashMap();
        this.fileReadSize = size;
        logger.finest(getLoggingFilename() + ":Start of frame body at:" + byteBuffer.position() + ",frames data size is:" + size);
        while (byteBuffer.position() < size) {
            String id;
            try {
                logger.finest(getLoggingFilename() + ":Looking for next frame at:" + byteBuffer.position());
                next = new ID3v23Frame(byteBuffer, getLoggingFilename());
                id = next.getIdentifier();
                loadFrameIntoMap(id, next);
            } catch (EmptyFrameException ex) {
                logger.warning(getLoggingFilename() + ":Empty Frame:" + ex.getMessage());
                this.emptyFrameBytes += ID3v23Frame.FRAME_HEADER_SIZE;
            } catch (InvalidFrameIdentifierException ifie) {
                logger.info(getLoggingFilename() + ":Invalid Frame Identifier:" + ifie.getMessage());
                this.invalidFrameBytes++;
                break;
            } catch (InvalidFrameException ife) {
                logger.warning(getLoggingFilename() + ":Invalid Frame:" + ife.getMessage());
                this.invalidFrameBytes++;
                break;
            }
            ;
        }
    }

    /**
     * Write the ID3 header to the ByteBuffer.
     *
     *  TODO Calculate the CYC Data Check
     *  TODO Reintroduce Extended Header
     *
     * @param padding is the size of the padding portion of the tag
     * @param size is the size of the body data
     *
     * @return ByteBuffer
     * @throws IOException
     */
    private ByteBuffer writeHeaderToBuffer(int padding, int size) throws IOException {
        extended = false;
        experimental = false;
        crcDataFlag = false;
        ByteBuffer headerBuffer = ByteBuffer.allocate(TAG_HEADER_LENGTH + TAG_EXT_HEADER_LENGTH + TAG_EXT_HEADER_CRC_LENGTH);
        headerBuffer.put(TAG_ID);
        headerBuffer.put(getMajorVersion());
        headerBuffer.put(getRevision());
        byte flagsByte = 0;
        if (isUnsynchronization() == true) {
            flagsByte |= MASK_V23_UNSYNCHRONIZATION;
        }
        if (extended == true) {
            flagsByte |= MASK_V23_EXTENDED_HEADER;
        }
        if (experimental == true) {
            flagsByte |= MASK_V23_EXPERIMENTAL;
        }
        headerBuffer.put(flagsByte);
        int additionalHeaderSize = 0;
        if (extended) {
            additionalHeaderSize += this.TAG_EXT_HEADER_LENGTH;
            if (crcDataFlag) {
                additionalHeaderSize += this.TAG_EXT_HEADER_CRC_LENGTH;
            }
        }
        headerBuffer.put(ID3SyncSafeInteger.valueToBuffer(padding + size + additionalHeaderSize));
        if (extended == true) {
            byte extFlagsByte1 = 0;
            byte extFlagsByte2 = 0;
            if (crcDataFlag == true) {
                headerBuffer.putInt(TAG_EXT_HEADER_DATA_LENGTH + TAG_EXT_HEADER_CRC_LENGTH);
                extFlagsByte1 |= MASK_V23_CRC_DATA_PRESENT;
                headerBuffer.put(extFlagsByte1);
                headerBuffer.put(extFlagsByte2);
                headerBuffer.putInt(paddingSize);
                headerBuffer.putInt(crcData);
            } else {
                headerBuffer.putInt(TAG_EXT_HEADER_DATA_LENGTH);
                headerBuffer.put(extFlagsByte1);
                headerBuffer.put(extFlagsByte2);
                headerBuffer.putInt(padding);
            }
        }
        headerBuffer.flip();
        return headerBuffer;
    }

    /**
     * Write tag to file
     *
     * TODO:we currently never write the Extended header , but if we did the size calculation in this
     * method would be slightly incorrect
     *
     * @param file The file to write to
     * @throws IOException 
     */
    public void write(File file, long audioStartLocation) throws IOException {
        logger.info(getLoggingFilename() + ":Writing tag to file");
        byte[] bodyByteBuffer = writeFramesToBuffer().toByteArray();
        logger.info(getLoggingFilename() + ":bodybytebuffer:sizebeforeunsynchronisation:" + bodyByteBuffer.length);
        if (TagOptionSingleton.getInstance().isUnsyncTags()) {
            unsynchronization = ID3Unsynchronization.requiresUnsynchronization(bodyByteBuffer);
        } else {
            unsynchronization = false;
        }
        if (isUnsynchronization()) {
            bodyByteBuffer = ID3Unsynchronization.unsynchronize(bodyByteBuffer);
            logger.info(getLoggingFilename() + ":bodybytebuffer:sizeafterunsynchronisation:" + bodyByteBuffer.length);
        }
        int sizeIncPadding = calculateTagSize(bodyByteBuffer.length + TAG_HEADER_LENGTH, (int) audioStartLocation);
        int padding = sizeIncPadding - (bodyByteBuffer.length + TAG_HEADER_LENGTH);
        logger.info(getLoggingFilename() + ":Current audiostart:" + audioStartLocation);
        logger.info(getLoggingFilename() + ":Size including padding:" + sizeIncPadding);
        logger.info(getLoggingFilename() + ":Padding:" + padding);
        ByteBuffer headerBuffer = writeHeaderToBuffer(padding, bodyByteBuffer.length);
        if (sizeIncPadding > audioStartLocation) {
            logger.info(getLoggingFilename() + ":Adjusting Padding");
            adjustPadding(file, sizeIncPadding, audioStartLocation);
        }
        FileChannel fc = null;
        try {
            fc = new RandomAccessFile(file, "rw").getChannel();
            fc.write(headerBuffer);
            fc.write(ByteBuffer.wrap(bodyByteBuffer));
            fc.write(ByteBuffer.wrap(new byte[padding]));
        } finally {
            if (fc != null) {
                fc.close();
            }
        }
    }

    /**
     * Write tag to channel
     * 
     * @param channel
     * @throws IOException
     */
    public void write(WritableByteChannel channel) throws IOException {
        logger.info(getLoggingFilename() + ":Writing tag to channel");
        byte[] bodyByteBuffer = writeFramesToBuffer().toByteArray();
        logger.info(getLoggingFilename() + ":bodybytebuffer:sizebeforeunsynchronisation:" + bodyByteBuffer.length);
        if (TagOptionSingleton.getInstance().isUnsyncTags()) {
            unsynchronization = ID3Unsynchronization.requiresUnsynchronization(bodyByteBuffer);
        } else {
            unsynchronization = false;
        }
        if (isUnsynchronization()) {
            bodyByteBuffer = ID3Unsynchronization.unsynchronize(bodyByteBuffer);
            logger.info(getLoggingFilename() + ":bodybytebuffer:sizeafterunsynchronisation:" + bodyByteBuffer.length);
        }
        ByteBuffer headerBuffer = writeHeaderToBuffer(0, bodyByteBuffer.length);
        channel.write(headerBuffer);
        channel.write(ByteBuffer.wrap(bodyByteBuffer));
    }

    /**
     * For representing the MP3File in an XML Format
     *
     */
    public void createStructure() {
        MP3File.getStructureFormatter().openHeadingElement(TYPE_TAG, getIdentifier());
        super.createStructureHeader();
        MP3File.getStructureFormatter().openHeadingElement(TYPE_HEADER, "");
        MP3File.getStructureFormatter().addElement(TYPE_UNSYNCHRONISATION, this.isUnsynchronization());
        MP3File.getStructureFormatter().addElement(TYPE_EXTENDED, this.extended);
        MP3File.getStructureFormatter().addElement(TYPE_EXPERIMENTAL, this.experimental);
        MP3File.getStructureFormatter().addElement(TYPE_CRCDATA, this.crcData);
        MP3File.getStructureFormatter().addElement(TYPE_PADDINGSIZE, this.paddingSize);
        MP3File.getStructureFormatter().closeHeadingElement(TYPE_HEADER);
        super.createStructureBody();
        MP3File.getStructureFormatter().closeHeadingElement(TYPE_TAG);
    }

    /**
    *
    * @return is tag unsynchronized
    */
    public boolean isUnsynchronization() {
        return unsynchronization;
    }

    protected String getArtistId() {
        return ID3v23Frames.FRAME_ID_V3_ARTIST;
    }

    protected String getAlbumId() {
        return ID3v23Frames.FRAME_ID_V3_ALBUM;
    }

    protected String getTitleId() {
        return ID3v23Frames.FRAME_ID_V3_TITLE;
    }

    protected String getTrackId() {
        return ID3v23Frames.FRAME_ID_V3_TRACK;
    }

    protected String getYearId() {
        return ID3v23Frames.FRAME_ID_V3_TYER;
    }

    protected String getCommentId() {
        return ID3v23Frames.FRAME_ID_V3_COMMENT;
    }

    protected String getGenreId() {
        return ID3v23Frames.FRAME_ID_V3_GENRE;
    }

    public ID3v23Frame createFrame(String id) {
        return new ID3v23Frame(id);
    }

    /**
     * Create Frame for Id3 Key
     * <p/>
     * Only textual data supported at the moment, should only be used with frames that
     * support a simple string argument.
     *
     * @param id3Key
     * @param value
     * @return
     * @throws KeyNotFoundException
     * @throws FieldDataInvalidException
     */
    public TagField createTagField(ID3v23FieldKey id3Key, String value) throws KeyNotFoundException, FieldDataInvalidException {
        if (id3Key == null) {
            throw new KeyNotFoundException();
        }
        return super.doCreateTagField(new FrameAndSubId(id3Key.getFrameId(), id3Key.getSubId()), value);
    }

    /**
     * Retrieve the first value that exists for this id3v23key
     *
     * @param id3v23FieldKey
     * @return
     */
    public String getFirst(ID3v23FieldKey id3v23FieldKey) throws KeyNotFoundException {
        if (id3v23FieldKey == null) {
            throw new KeyNotFoundException();
        }
        return super.doGetFirst(new FrameAndSubId(id3v23FieldKey.getFrameId(), id3v23FieldKey.getSubId()));
    }

    /**
     * Delete fields with this id3v23FieldKey
     *
     * @param id3v23FieldKey
     */
    public void deleteTagField(ID3v23FieldKey id3v23FieldKey) throws KeyNotFoundException {
        if (id3v23FieldKey == null) {
            throw new KeyNotFoundException();
        }
        super.doDeleteTagField(new FrameAndSubId(id3v23FieldKey.getFrameId(), id3v23FieldKey.getSubId()));
    }

    protected FrameAndSubId getFrameAndSubIdFromGenericKey(TagFieldKey genericKey) {
        ID3v23FieldKey id3v23FieldKey = ID3v23Frames.getInstanceOf().getId3KeyFromGenericKey(genericKey);
        if (id3v23FieldKey == null) {
            throw new KeyNotFoundException();
        }
        return new FrameAndSubId(id3v23FieldKey.getFrameId(), id3v23FieldKey.getSubId());
    }

    protected ID3Frames getID3Frames() {
        return ID3v23Frames.getInstanceOf();
    }
}
