package com.hadeslee.audiotag.tag.id3;

import com.hadeslee.audiotag.FileConstants;
import com.hadeslee.audiotag.audio.mp3.MP3File;
import com.hadeslee.audiotag.tag.EmptyFrameException;
import com.hadeslee.audiotag.tag.FieldDataInvalidException;
import com.hadeslee.audiotag.tag.InvalidFrameException;
import com.hadeslee.audiotag.tag.InvalidFrameIdentifierException;
import com.hadeslee.audiotag.tag.KeyNotFoundException;
import com.hadeslee.audiotag.tag.TagException;
import com.hadeslee.audiotag.tag.TagField;
import com.hadeslee.audiotag.tag.TagFieldKey;
import com.hadeslee.audiotag.tag.TagNotFoundException;
import com.hadeslee.audiotag.tag.TagOptionSingleton;
import com.hadeslee.audiotag.tag.id3.framebody.AbstractFrameBodyTextInfo;
import com.hadeslee.audiotag.tag.id3.framebody.FrameBodyTDRC;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.*;
import java.util.logging.Level;

/**
 * Represents an ID3v2.2 tag.
 * 
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id: ID3v22Tag.java,v 1.31 2007/11/13 14:24:30 paultaylor Exp $
 */
public class ID3v22Tag extends AbstractID3v2Tag {

    protected static final String TYPE_COMPRESSION = "compression";

    protected static final String TYPE_UNSYNCHRONISATION = "unsyncronisation";

    /**
     * ID3v2.2 Header bit mask
     */
    public static final int MASK_V22_UNSYNCHRONIZATION = FileConstants.BIT7;

    /**
     * ID3v2.2 Header bit mask
     */
    public static final int MASK_V22_COMPRESSION = FileConstants.BIT7;

    /**
     * The tag is compressed
     */
    protected boolean compression = false;

    /**
     * All frames in the tag uses unsynchronisation
     */
    protected boolean unsynchronization = false;

    public static final byte RELEASE = 2;

    public static final byte MAJOR_VERSION = 2;

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
     * Creates a new empty ID3v2_2 tag.
     */
    public ID3v22Tag() {
        frameMap = new LinkedHashMap();
    }

    /**
     * Copy primitives applicable to v2.2
     */
    protected void copyPrimitives(AbstractID3v2Tag copyObj) {
        logger.info("Copying primitives");
        super.copyPrimitives(copyObj);
        if (copyObj instanceof ID3v22Tag) {
            ID3v22Tag copyObject = (ID3v22Tag) copyObj;
            this.compression = copyObject.compression;
            this.unsynchronization = copyObject.unsynchronization;
        } else if (copyObj instanceof ID3v23Tag) {
            ID3v23Tag copyObject = (ID3v23Tag) copyObj;
            this.compression = copyObject.compression;
            this.unsynchronization = copyObject.unsynchronization;
        } else if (copyObj instanceof ID3v24Tag) {
            ID3v24Tag copyObject = (ID3v24Tag) copyObj;
            this.compression = false;
            this.unsynchronization = copyObject.unsynchronization;
        }
    }

    /**
     * Copy frames from one tag into a v2.2 tag
     */
    protected void copyFrames(AbstractID3v2Tag copyObject) {
        logger.info("Copying Frames,there are:" + copyObject.frameMap.keySet().size());
        frameMap = new LinkedHashMap();
        Iterator iterator = copyObject.frameMap.keySet().iterator();
        AbstractID3v2Frame frame;
        ID3v22Frame newFrame = null;
        while (iterator.hasNext()) {
            String id = (String) iterator.next();
            Object o = copyObject.frameMap.get(id);
            if (o instanceof AbstractID3v2Frame) {
                frame = (AbstractID3v2Frame) o;
                if ((frame.getIdentifier().equals(ID3v24Frames.FRAME_ID_YEAR)) && (frame.getBody() instanceof FrameBodyTDRC)) {
                    translateFrame(frame);
                } else {
                    try {
                        newFrame = new ID3v22Frame(frame);
                        frameMap.put(newFrame.getIdentifier(), newFrame);
                    } catch (InvalidFrameException ife) {
                        logger.log(Level.SEVERE, "Unable to convert frame:" + frame.getIdentifier(), ife);
                    }
                }
            } else if (o instanceof ArrayList) {
                ArrayList multiFrame = new ArrayList();
                for (ListIterator li = ((ArrayList) o).listIterator(); li.hasNext(); ) {
                    frame = (AbstractID3v2Frame) li.next();
                    try {
                        newFrame = new ID3v22Frame(frame);
                        multiFrame.add(newFrame);
                    } catch (InvalidFrameException ife) {
                        logger.log(Level.SEVERE, "Unable to convert frame:" + frame.getIdentifier(), ife);
                    }
                }
                if (newFrame != null) {
                    frameMap.put(newFrame.getIdentifier(), multiFrame);
                }
            }
        }
    }

    /**
     * Copy Constructor, creates a new ID3v2_2 Tag based on another ID3v2_2 Tag
     */
    public ID3v22Tag(ID3v22Tag copyObject) {
        super(copyObject);
        logger.info("Creating tag from another tag of same type");
        copyPrimitives(copyObject);
        copyFrames(copyObject);
    }

    /**
     * Constructs a new tag based upon another tag of different version/type
     */
    public ID3v22Tag(AbstractTag mp3tag) {
        frameMap = new LinkedHashMap();
        logger.info("Creating tag from a tag of a different version");
        if (mp3tag != null) {
            ID3v24Tag convertedTag;
            if ((mp3tag instanceof ID3v23Tag == false) && (mp3tag instanceof ID3v22Tag == true)) {
                throw new UnsupportedOperationException("Copy Constructor not called. Please type cast the argument");
            } else if (mp3tag instanceof ID3v24Tag) {
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
     * Creates a new ID3v2_2 datatype.
     *
     * @param buffer
     * @param loggingFilename
     * @throws TagException
     */
    public ID3v22Tag(ByteBuffer buffer, String loggingFilename) throws TagException {
        setLoggingFilename(loggingFilename);
        this.read(buffer);
    }

    /**
     * Creates a new ID3v2_2 datatype.
     *
     * @param buffer
     * @throws TagException
     *
     * @deprecated use {@link #ID3v22Tag(ByteBuffer,String)} instead
     */
    public ID3v22Tag(ByteBuffer buffer) throws TagException {
        this(buffer, "");
    }

    /**
     *
     *
     * @return an indentifier of the tag type
     */
    public String getIdentifier() {
        return "ID3v2_2.20";
    }

    /**
     * Return frame size based upon the sizes of the frames rather than the size
     * including padding recorded in the tag header
     *
     * @return size
     */
    public int getSize() {
        int size = TAG_HEADER_LENGTH;
        size += super.getSize();
        return size;
    }

    /**
     * 
     *
     * @param obj 
     * @return equality
     */
    public boolean equals(Object obj) {
        if ((obj instanceof ID3v22Tag) == false) {
            return false;
        }
        ID3v22Tag object = (ID3v22Tag) obj;
        if (this.compression != object.compression) {
            return false;
        }
        if (this.unsynchronization != object.unsynchronization) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Read tag from the ByteBuffer
     *
     * @param byteBuffer to read the tag from
     * @throws TagException
     * @throws TagNotFoundException
     */
    public void read(ByteBuffer byteBuffer) throws TagException {
        int size;
        if (seek(byteBuffer) == false) {
            throw new TagNotFoundException("ID3v2.20 tag not found");
        }
        logger.info(getLoggingFilename() + ":" + "Reading tag from file");
        byte flags = byteBuffer.get();
        unsynchronization = (flags & MASK_V22_UNSYNCHRONIZATION) != 0;
        compression = (flags & MASK_V22_COMPRESSION) != 0;
        if (unsynchronization) {
            logger.warning(getLoggingFilename() + ":" + "ID3v22 Tag is unsynchronized");
        }
        if (compression) {
            logger.warning(getLoggingFilename() + ":" + "ID3v22 Tag is compressed");
        }
        size = ID3SyncSafeInteger.bufferToValue(byteBuffer);
        ByteBuffer bufferWithoutHeader = byteBuffer.slice();
        if (unsynchronization == true) {
            bufferWithoutHeader = ID3Unsynchronization.synchronize(bufferWithoutHeader);
        }
        readFrames(bufferWithoutHeader, size);
        logger.info(getLoggingFilename() + ":" + "Loaded Frames,there are:" + frameMap.keySet().size());
    }

    /**
     * Read frames from tag
     */
    protected void readFrames(ByteBuffer byteBuffer, int size) {
        ID3v22Frame next;
        frameMap = new LinkedHashMap();
        this.fileReadSize = size;
        logger.finest(getLoggingFilename() + ":" + "Start of frame body at:" + byteBuffer.position() + ",frames sizes and padding is:" + size);
        while (byteBuffer.position() < size) {
            try {
                logger.finest(getLoggingFilename() + ":" + "looking for next frame at:" + byteBuffer.position());
                next = new ID3v22Frame(byteBuffer, getLoggingFilename());
                String id = next.getIdentifier();
                loadFrameIntoMap(id, next);
            } catch (EmptyFrameException ex) {
                logger.warning(getLoggingFilename() + ":" + "Empty Frame:" + ex.getMessage());
                this.emptyFrameBytes += ID3v22Frame.FRAME_HEADER_SIZE;
            } catch (InvalidFrameIdentifierException ifie) {
                logger.info(getLoggingFilename() + ":" + "Invalid Frame Identifier:" + ifie.getMessage());
                this.invalidFrameBytes++;
                break;
            } catch (InvalidFrameException ife) {
                logger.warning(getLoggingFilename() + ":" + "Invalid Frame:" + ife.getMessage());
                this.invalidFrameBytes++;
                break;
            }
            ;
        }
    }

    /**
     * This is used when we need to translate a single frame into multiple frames,
     * currently required for TDRC frames.
     */
    protected void translateFrame(AbstractID3v2Frame frame) {
        FrameBodyTDRC tmpBody = (FrameBodyTDRC) frame.getBody();
        ID3v22Frame newFrame;
        if (tmpBody.getYear().length() != 0) {
            newFrame = new ID3v22Frame(ID3v22Frames.FRAME_ID_V2_TYER);
            ((AbstractFrameBodyTextInfo) newFrame.getBody()).setText(tmpBody.getYear());
            frameMap.put(newFrame.getIdentifier(), newFrame);
        }
        if (tmpBody.getTime().length() != 0) {
            newFrame = new ID3v22Frame(ID3v22Frames.FRAME_ID_V2_TIME);
            ((AbstractFrameBodyTextInfo) newFrame.getBody()).setText(tmpBody.getTime());
            frameMap.put(newFrame.getIdentifier(), newFrame);
        }
    }

    /**
     * Write the ID3 header to the ByteBuffer.
     *
     * TODO compression support required.
     *
     * @return ByteBuffer 
     * @throws IOException
     */
    private ByteBuffer writeHeaderToBuffer(int padding, int size) throws IOException {
        compression = false;
        ByteBuffer headerBuffer = ByteBuffer.allocate(TAG_HEADER_LENGTH);
        headerBuffer.put(TAG_ID);
        headerBuffer.put(getMajorVersion());
        headerBuffer.put(getRevision());
        byte flags = (byte) 0;
        if (unsynchronization == true) {
            flags |= (byte) MASK_V22_UNSYNCHRONIZATION;
        }
        if (compression == true) {
            flags |= (byte) MASK_V22_COMPRESSION;
        }
        headerBuffer.put(flags);
        headerBuffer.put(ID3SyncSafeInteger.valueToBuffer(padding + size));
        headerBuffer.flip();
        return headerBuffer;
    }

    /**
     * Write tag to file
     *
     * @param file The file to write to
     * @throws IOException
     */
    public void write(File file, long audioStartLocation) throws IOException {
        logger.info("Writing tag to file");
        byte[] bodyByteBuffer = writeFramesToBuffer().toByteArray();
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

    public void createStructure() {
        MP3File.getStructureFormatter().openHeadingElement(TYPE_TAG, getIdentifier());
        super.createStructureHeader();
        MP3File.getStructureFormatter().openHeadingElement(TYPE_HEADER, "");
        MP3File.getStructureFormatter().addElement(TYPE_COMPRESSION, this.compression);
        MP3File.getStructureFormatter().addElement(TYPE_UNSYNCHRONISATION, this.unsynchronization);
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

    /**
     *
     * @return is tag compressed
     */
    public boolean isCompression() {
        return compression;
    }

    protected String getArtistId() {
        return ID3v22Frames.FRAME_ID_V2_ARTIST;
    }

    protected String getAlbumId() {
        return ID3v22Frames.FRAME_ID_V2_ALBUM;
    }

    protected String getTitleId() {
        return ID3v22Frames.FRAME_ID_V2_TITLE;
    }

    protected String getTrackId() {
        return ID3v22Frames.FRAME_ID_V2_TRACK;
    }

    protected String getYearId() {
        return ID3v22Frames.FRAME_ID_V2_TYER;
    }

    protected String getCommentId() {
        return ID3v22Frames.FRAME_ID_V2_COMMENT;
    }

    protected String getGenreId() {
        return ID3v22Frames.FRAME_ID_V2_GENRE;
    }

    /**
     * Create Frame
     * @param id frameid
     * @return
     */
    public ID3v22Frame createFrame(String id) {
        return new ID3v22Frame(id);
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
    public TagField createTagField(ID3v22FieldKey id3Key, String value) throws KeyNotFoundException, FieldDataInvalidException {
        if (id3Key == null) {
            throw new KeyNotFoundException();
        }
        return super.doCreateTagField(new FrameAndSubId(id3Key.getFrameId(), id3Key.getSubId()), value);
    }

    /**
     * Retrieve the first value that exists for this id3v22key
     *
     * @param id3v22FieldKey
     * @return
     */
    public String getFirst(ID3v22FieldKey id3v22FieldKey) throws KeyNotFoundException {
        if (id3v22FieldKey == null) {
            throw new KeyNotFoundException();
        }
        return super.doGetFirst(new FrameAndSubId(id3v22FieldKey.getFrameId(), id3v22FieldKey.getSubId()));
    }

    /**
     * Delete fields with this id3v22FieldKey
     *
     * @param id3v22FieldKey
     */
    public void deleteTagField(ID3v22FieldKey id3v22FieldKey) throws KeyNotFoundException {
        if (id3v22FieldKey == null) {
            throw new KeyNotFoundException();
        }
        super.doDeleteTagField(new FrameAndSubId(id3v22FieldKey.getFrameId(), id3v22FieldKey.getSubId()));
    }

    protected FrameAndSubId getFrameAndSubIdFromGenericKey(TagFieldKey genericKey) {
        ID3v22FieldKey id3v22FieldKey = ID3v22Frames.getInstanceOf().getId3KeyFromGenericKey(genericKey);
        if (id3v22FieldKey == null) {
            throw new KeyNotFoundException();
        }
        return new FrameAndSubId(id3v22FieldKey.getFrameId(), id3v22FieldKey.getSubId());
    }

    protected ID3Frames getID3Frames() {
        return ID3v22Frames.getInstanceOf();
    }
}
