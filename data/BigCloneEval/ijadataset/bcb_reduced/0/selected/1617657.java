package org.jaudiotagger.tag.id3;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * This is the abstract base class for all ID3v1 tags.
 *
 * @author : Eric Farng
 * @author : Paul Taylor
 */
public abstract class AbstractID3v1Tag extends AbstractID3Tag {

    public static Logger logger = Logger.getLogger("org.jaudiotagger.tag.id3");

    public AbstractID3v1Tag() {
    }

    public AbstractID3v1Tag(AbstractID3v1Tag copyObject) {
        super(copyObject);
    }

    protected static final byte END_OF_FIELD = (byte) 0;

    protected static Pattern endofStringPattern = Pattern.compile("\\x00");

    protected static final byte[] TAG_ID = { (byte) 'T', (byte) 'A', (byte) 'G' };

    protected static final int TAG_LENGTH = 128;

    protected static final int TAG_DATA_LENGTH = 125;

    protected static final int FIELD_TAGID_LENGTH = 3;

    protected static final int FIELD_TITLE_LENGTH = 30;

    protected static final int FIELD_ARTIST_LENGTH = 30;

    protected static final int FIELD_ALBUM_LENGTH = 30;

    protected static final int FIELD_YEAR_LENGTH = 4;

    protected static final int FIELD_GENRE_LENGTH = 1;

    protected static final int FIELD_TAGID_POS = 0;

    protected static final int FIELD_TITLE_POS = 3;

    protected static final int FIELD_ARTIST_POS = 33;

    protected static final int FIELD_ALBUM_POS = 63;

    protected static final int FIELD_YEAR_POS = 93;

    protected static final int FIELD_GENRE_POS = 127;

    protected static final String TYPE_TITLE = "title";

    protected static final String TYPE_ARTIST = "artist";

    protected static final String TYPE_ALBUM = "album";

    protected static final String TYPE_YEAR = "year";

    protected static final String TYPE_GENRE = "genre";

    /**
     * Return the size of this tag, the size is fixed for tags of this type
     *
     * @return size of this tag in bytes
     */
    public int getSize() {
        return TAG_LENGTH;
    }

    /**
     * Does a v1tag or a v11tag exist
     *
     * @return whether tag exists within the byteBuffer
     */
    public static boolean seekForV1OrV11Tag(ByteBuffer byteBuffer) {
        byte[] buffer = new byte[FIELD_TAGID_LENGTH];
        byteBuffer.get(buffer, 0, FIELD_TAGID_LENGTH);
        return (Arrays.equals(buffer, TAG_ID));
    }

    /**
     * Delete tag from file
     * Looks for tag and if found lops it off the file.
     *
     * @param file to delete the tag from
     * @throws IOException if there was a problem accessing the file
     */
    public void delete(RandomAccessFile file) throws IOException {
        logger.info("Deleting ID3v1 from file if exists");
        FileChannel fc;
        ByteBuffer byteBuffer;
        fc = file.getChannel();
        if (file.length() < TAG_LENGTH) {
            throw new IOException("File not not appear large enough to contain a tag");
        }
        fc.position(file.length() - TAG_LENGTH);
        byteBuffer = ByteBuffer.allocate(TAG_LENGTH);
        fc.read(byteBuffer);
        byteBuffer.rewind();
        if (AbstractID3v1Tag.seekForV1OrV11Tag(byteBuffer)) {
            logger.config("Deleted ID3v1 tag");
            file.setLength(file.length() - TAG_LENGTH);
        } else {
            logger.config("Unable to find ID3v1 tag to deleteField");
        }
    }
}
