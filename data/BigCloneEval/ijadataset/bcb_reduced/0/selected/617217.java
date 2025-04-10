package org.jaudiotagger.audio.asf;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.asf.data.AsfHeader;
import org.jaudiotagger.audio.asf.data.AudioStreamChunk;
import org.jaudiotagger.audio.asf.data.MetadataContainer;
import org.jaudiotagger.audio.asf.data.MetadataDescriptor;
import org.jaudiotagger.audio.asf.io.*;
import org.jaudiotagger.tag.asf.AsfTag;
import org.jaudiotagger.audio.asf.util.TagConverter;
import org.jaudiotagger.audio.asf.util.Utils;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.generic.AudioFileReader;
import org.jaudiotagger.audio.generic.GenericAudioHeader;
import org.jaudiotagger.logging.ErrorMessage;
import org.jaudiotagger.tag.TagException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This reader can read ASF files containing any content (stream type). <br>
 * 
 * @author Christian Laireiter
 */
public class AsfFileReader extends AudioFileReader {

    /**
     * Logger instance
     */
    private static final Logger LOGGER = Logger.getLogger("org.jaudiotagger.audio.asf");

    /**
     * This reader will be configured to read tag and audio header information.<br>
     */
    private static final AsfHeaderReader HEADER_READER;

    static {
        final List<Class<? extends ChunkReader>> readers = new ArrayList<Class<? extends ChunkReader>>();
        readers.add(ContentDescriptionReader.class);
        readers.add(ContentBrandingReader.class);
        readers.add(MetadataReader.class);
        readers.add(LanguageListReader.class);
        final AsfExtHeaderReader extReader = new AsfExtHeaderReader(readers, true);
        readers.add(FileHeaderReader.class);
        readers.add(StreamChunkReader.class);
        HEADER_READER = new AsfHeaderReader(readers, true);
        HEADER_READER.setExtendedHeaderReader(extReader);
    }

    /**
     * Determines if the &quot;isVbr&quot; field is set in the extended content
     * description.<br>
     * 
     * @param header
     *            the header to look up.
     * @return <code>true</code> if &quot;isVbr&quot; is present with a
     *         <code>true</code> value.
     */
    private boolean determineVariableBitrate(final AsfHeader header) {
        assert header != null;
        boolean result = false;
        final MetadataContainer extDesc = header.findExtendedContentDescription();
        if (extDesc != null) {
            final List<MetadataDescriptor> descriptors = extDesc.getDescriptorsByName("IsVBR");
            if (descriptors != null && !descriptors.isEmpty()) {
                result = Boolean.TRUE.toString().equals(descriptors.get(0).getString());
            }
        }
        return result;
    }

    /**
     * Creates a generic audio header instance with provided data from header.
     * 
     * @param header
     *            ASF header which contains the information.
     * @return generic audio header representation.
     * @throws CannotReadException
     *             If header does not contain mandatory information. (Audio
     *             stream chunk and file header chunk)
     */
    private GenericAudioHeader getAudioHeader(final AsfHeader header) throws CannotReadException {
        final GenericAudioHeader info = new GenericAudioHeader();
        if (header.getFileHeader() == null) {
            throw new CannotReadException("Invalid ASF/WMA file. File header object not available.");
        }
        if (header.getAudioStreamChunk() == null) {
            throw new CannotReadException("Invalid ASF/WMA file. No audio stream contained.");
        }
        info.setBitrate(header.getAudioStreamChunk().getKbps());
        info.setChannelNumber((int) header.getAudioStreamChunk().getChannelCount());
        info.setEncodingType("ASF (audio): " + header.getAudioStreamChunk().getCodecDescription());
        info.setLossless(header.getAudioStreamChunk().getCompressionFormat() == AudioStreamChunk.WMA_LOSSLESS);
        info.setPreciseLength(header.getFileHeader().getPreciseDuration());
        info.setSamplingRate((int) header.getAudioStreamChunk().getSamplingRate());
        info.setVariableBitRate(determineVariableBitrate(header));
        return info;
    }

    /**
     * (overridden)
     * 
     * @see org.jaudiotagger.audio.generic.AudioFileReader#getEncodingInfo(java.io.RandomAccessFile)
     */
    @Override
    protected GenericAudioHeader getEncodingInfo(final RandomAccessFile raf) throws CannotReadException, IOException {
        raf.seek(0);
        GenericAudioHeader info;
        try {
            final AsfHeader header = AsfHeaderReader.readInfoHeader(raf);
            if (header == null) {
                throw new CannotReadException("Some values must have been " + "incorrect for interpretation as asf with wma content.");
            }
            info = getAudioHeader(header);
        } catch (final Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            } else if (e instanceof CannotReadException) {
                throw (CannotReadException) e;
            } else {
                throw new CannotReadException("Failed to read. Cause: " + e.getMessage(), e);
            }
        }
        return info;
    }

    /**
     * Creates a tag instance with provided data from header.
     * 
     * @param header
     *            ASF header which contains the information.
     * @return generic audio header representation.
     */
    private AsfTag getTag(final AsfHeader header) {
        return TagConverter.createTagOf(header);
    }

    /**
     * (overridden)
     * 
     * @see org.jaudiotagger.audio.generic.AudioFileReader#getTag(java.io.RandomAccessFile)
     */
    @Override
    protected AsfTag getTag(final RandomAccessFile raf) throws CannotReadException, IOException {
        raf.seek(0);
        AsfTag tag;
        try {
            final AsfHeader header = AsfHeaderReader.readTagHeader(raf);
            if (header == null) {
                throw new CannotReadException("Some values must have been " + "incorrect for interpretation as asf with wma content.");
            }
            tag = TagConverter.createTagOf(header);
        } catch (final Exception e) {
            logger.severe(e.getMessage());
            if (e instanceof IOException) {
                throw (IOException) e;
            } else if (e instanceof CannotReadException) {
                throw (CannotReadException) e;
            } else {
                throw new CannotReadException("Failed to read. Cause: " + e.getMessage());
            }
        }
        return tag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AudioFile read(final File f) throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException {
        if (!f.canRead()) {
            throw new CannotReadException(ErrorMessage.GENERAL_READ_FAILED_DO_NOT_HAVE_PERMISSION_TO_READ_FILE.getMsg(f.getAbsolutePath()));
        }
        InputStream stream = null;
        try {
            stream = new FullRequestInputStream(new BufferedInputStream(new FileInputStream(f)));
            final AsfHeader header = HEADER_READER.read(Utils.readGUID(stream), stream, 0);
            if (header == null) {
                throw new CannotReadException(ErrorMessage.ASF_HEADER_MISSING.getMsg(f.getAbsolutePath()));
            }
            if (header.getFileHeader() == null) {
                throw new CannotReadException(ErrorMessage.ASF_FILE_HEADER_MISSING.getMsg(f.getAbsolutePath()));
            }
            if (header.getFileHeader().getFileSize().longValue() != f.length()) {
                logger.warning(ErrorMessage.ASF_FILE_HEADER_SIZE_DOES_NOT_MATCH_FILE_SIZE.getMsg(f.getAbsolutePath(), header.getFileHeader().getFileSize().longValue(), f.length()));
            }
            return new AudioFile(f, getAudioHeader(header), getTag(header));
        } catch (final CannotReadException e) {
            throw e;
        } catch (final Exception e) {
            throw new CannotReadException("\"" + f + "\" :" + e, e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (final Exception ex) {
                LOGGER.severe("\"" + f + "\" :" + ex);
            }
        }
    }
}
