package com.hadeslee.audiotag.audio.mp3;

import com.hadeslee.audiotag.audio.AudioHeader;
import com.hadeslee.audiotag.audio.exceptions.InvalidAudioFrameException;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the audio header of an MP3 File
 *
 * <p>The audio header consists of a number of
 * audio frames. Because we are not trying to play the audio but only extract some information
 * regarding the audio we only need to read the first  audio frames to ensure that we have correctly
 * identified them as audio frames and extracted the metadata we reuire.

 * <p>Start of Audio id 0xFF (11111111) and then second byte anded with 0xE0(11100000).
 * For example 2nd byte doesnt have to be 0xE0 is just has to have the top 3 signicant
 * bits set. For example 0xFB (11111011) is a common occurence of the second match. The 2nd byte
 * defines flags to indicate various mp3 values.
 *
 * <p>Having found these two values we then read the header which comprises these two bytes plus a further
 * two to ensure this really is a MP3Header, sometimes the first frame is actually a dummy frame with summary information
 * held within about the whole file, typically using a Xing Header or LAme Header. This is most useful when the file
 * is variable bit rate, if the file is variable bit rate but does not use a summary header it will not be correctly
 * identified as a VBR frame and the track length will be incorreclty calculated. Strictly speaking MP3 means an MPEG-1,
 * Layer III file but MP2 (MPEG-1,Layer II), MP1 (MPEG-1,Layer I) and MPEG-2 files are sometimes used and named with
 * the .mp3 suffix so this library attempts to supports all these formats.
*/
public final class MP3AudioHeader implements AudioHeader {

    private MPEGFrameHeader mp3FrameHeader;

    private XingFrame mp3XingFrame;

    private long fileSize;

    private long startByte;

    private double timePerFrame;

    private double trackLength;

    private long numberOfFrames;

    private long numberOfFramesEstimate;

    private long bitrate;

    private String encoder = "";

    private static final SimpleDateFormat timeInFormat = new SimpleDateFormat("ss");

    private static final SimpleDateFormat timeOutFormat = new SimpleDateFormat("mm:ss");

    private static final char isVbrIdentifier = '~';

    private static final int CONVERT_TO_KILOBITS = 1000;

    private static final String TYPE_MP3 = "mp3";

    private static final int CONVERTS_BYTE_TO_BITS = 8;

    public static Logger logger = Logger.getLogger("com.hadeslee.jaudiotagger.audio.mp3");

    /** After testing the average location of the first MP3Header bit was at 5000 bytes so this is
     *  why chosen as a default.
     */
    private static final int FILE_BUFFER_SIZE = 5000;

    private static final int MIN_BUFFER_REMAINING_REQUIRED = MPEGFrameHeader.HEADER_SIZE + XingFrame.MAX_BUFFER_SIZE_NEEDED_TO_READ_XING;

    /**
     * Search for the first MP3Header in the file
     *
     * The search starts from the start of the file, it is usually safer to use the alternative constructor that
     * allows you to provide the length of the tag header as a parameter so the tag can be skipped over.
     *
     * @param seekFile
     * @throws IOException
     * @throws InvalidAudioFrameException
     */
    public MP3AudioHeader(final File seekFile) throws IOException, InvalidAudioFrameException {
        if (seek(seekFile, 0) == false) {
            throw new InvalidAudioFrameException("No audio header found within" + seekFile.getName());
        }
    }

    /**
     * Search for the first MP3Header in the file
     *
     * Starts searching from location startByte, this is because there is likely to be an ID3TagHeader
     * before the start of the audio. If this tagHeader contains unsynchronized information there is a
     * possibility that it might be inaccurately identified as the start of the Audio data. Various checks
     * are done in this code to prevent this happening but it cannot be guaranteed.
     *
     * Of course if the startByte provided overstates the length og the tag header, this could mean the
     * start of the MP3AudioHeader is missed, further checks are done within the MP3 class to recognize
     * if this has occurred and take appropriate action.
     *
     * @param seekFile
     * @param startByte
     * @throws IOException
     * @throws InvalidAudioFrameException
     */
    public MP3AudioHeader(final File seekFile, long startByte) throws IOException, InvalidAudioFrameException {
        if (seek(seekFile, startByte) == false) {
            throw new InvalidAudioFrameException("No audio header found within" + seekFile.getName());
        }
    }

    /**
     * Returns true if the first MP3 frame can be found for the MP3 file
     *
     * This is the first byte of  music data and not the ID3 Tag Frame.     *
     *
     * @param seekFile MP3 file to seek
     * @param startByte if there is an ID3v2tag we dont want to start reading from the start of the tag
     * @return true if the first MP3 frame can be found
     * @throws IOException on any I/O error
     * @noinspection NestedTryStatement
     */
    public boolean seek(final File seekFile, long startByte) throws IOException {
        long filePointerCount;
        final FileInputStream fis = new FileInputStream(seekFile);
        final FileChannel fc = fis.getChannel();
        ByteBuffer bb = ByteBuffer.allocateDirect(FILE_BUFFER_SIZE);
        fc.position(startByte);
        filePointerCount = startByte;
        fc.read(bb, startByte);
        bb.flip();
        boolean syncFound = false;
        try {
            do {
                if (bb.remaining() <= MIN_BUFFER_REMAINING_REQUIRED) {
                    bb.clear();
                    fc.position(filePointerCount);
                    fc.read(bb, fc.position());
                    bb.flip();
                    if (bb.limit() <= MIN_BUFFER_REMAINING_REQUIRED) {
                        return false;
                    }
                }
                if (MPEGFrameHeader.isMPEGFrame(bb)) {
                    try {
                        if (MP3AudioHeader.logger.isLoggable(Level.FINEST)) {
                            MP3AudioHeader.logger.finest("Found Possible header at:" + filePointerCount);
                        }
                        mp3FrameHeader = MPEGFrameHeader.parseMPEGHeader(bb);
                        syncFound = true;
                        if (XingFrame.isXingFrame(bb, mp3FrameHeader)) {
                            if (MP3AudioHeader.logger.isLoggable(Level.FINEST)) {
                                MP3AudioHeader.logger.finest("Found Possible XingHeader");
                            }
                            try {
                                mp3XingFrame = XingFrame.parseXingFrame();
                            } catch (InvalidAudioFrameException ex) {
                            }
                            break;
                        } else {
                            syncFound = isNextFrameValid(seekFile, filePointerCount, bb, fc);
                            if (syncFound == true) {
                                break;
                            }
                        }
                    } catch (InvalidAudioFrameException ex) {
                    }
                }
                bb.position(bb.position() + 1);
                filePointerCount++;
            } while (!syncFound);
        } catch (EOFException ex) {
            MP3AudioHeader.logger.log(Level.WARNING, "Reached end of file without finding sync match", ex);
            syncFound = false;
        } catch (IOException iox) {
            MP3AudioHeader.logger.log(Level.SEVERE, "IOException occurred whilst trying to find sync", iox);
            syncFound = false;
            throw iox;
        } finally {
            if (fc != null) {
                fc.close();
            }
            if (fis != null) {
                fis.close();
            }
        }
        if (MP3AudioHeader.logger.isLoggable(Level.FINEST)) {
            MP3AudioHeader.logger.finer("Return found matching mp3 header starting at" + filePointerCount);
        }
        setFileSize(seekFile.length());
        setMp3StartByte(filePointerCount);
        setTimePerFrame();
        setNumberOfFrames();
        setTrackLength();
        setBitRate();
        setEncoder();
        return syncFound;
    }

    /** Called in some circumstances to check the next frame to ensure we have the correct audio header
     *
     * @return  true if frame is valid
     */
    private boolean isNextFrameValid(File seekFile, long filePointerCount, ByteBuffer bb, FileChannel fc) throws IOException {
        if (MP3AudioHeader.logger.isLoggable(Level.FINEST)) {
            MP3AudioHeader.logger.finer("Checking next frame" + seekFile.getName() + ":fpc:" + filePointerCount + "skipping to:" + (filePointerCount + mp3FrameHeader.getFrameLength()));
        }
        boolean result = false;
        int currentPosition = bb.position();
        if (mp3FrameHeader.getFrameLength() > (FILE_BUFFER_SIZE - MIN_BUFFER_REMAINING_REQUIRED)) {
            MP3AudioHeader.logger.finer("Frame size is too large to be a frame:" + mp3FrameHeader.getFrameLength());
            return false;
        }
        if (bb.remaining() <= MIN_BUFFER_REMAINING_REQUIRED + mp3FrameHeader.getFrameLength()) {
            MP3AudioHeader.logger.finer("Buffer too small, need to reload, buffer size:" + bb.remaining());
            bb.clear();
            fc.position(filePointerCount);
            fc.read(bb, fc.position());
            bb.flip();
            currentPosition = 0;
            if (bb.limit() <= MIN_BUFFER_REMAINING_REQUIRED) {
                MP3AudioHeader.logger.finer("Nearly at end of file, no header found:");
                return false;
            }
        }
        bb.position(bb.position() + mp3FrameHeader.getFrameLength());
        if (MPEGFrameHeader.isMPEGFrame(bb)) {
            try {
                MPEGFrameHeader.parseMPEGHeader(bb);
                MP3AudioHeader.logger.finer("Check next frame confirms is an audio header ");
                result = true;
            } catch (InvalidAudioFrameException ex) {
                MP3AudioHeader.logger.finer("Check next frame has identified this is not an audio header");
                result = false;
            }
        }
        bb.position(currentPosition);
        return result;
    }

    /**
     * Set the location of where the Audio file begins in the file
     *
     * @param startByte
     */
    private void setMp3StartByte(final long startByte) {
        this.startByte = startByte;
    }

    /**
     * Returns the byte position of the first MP3 Frame that the
     * <code>file</code> arguement refers to. This is the first byte of music
     * data and not the ID3 Tag Frame.
     *
     * @return the byte position of the first MP3 Frame
     */
    public long getMp3StartByte() {
        return startByte;
    }

    /**
     *  Set number of frames in this file, use Xing if exists otherwise ((File Size - Non Audio Part)/Frame Size)
     */
    private void setNumberOfFrames() {
        numberOfFramesEstimate = (fileSize - startByte) / mp3FrameHeader.getFrameLength();
        if (mp3XingFrame != null && mp3XingFrame.isFrameCountEnabled() == true) {
            numberOfFrames = mp3XingFrame.getFrameCount();
        } else {
            numberOfFrames = numberOfFramesEstimate;
        }
    }

    /**
     *
     * @return The number of frames within the Audio File, calculated as accurrately as possible
     */
    public long getNumberOfFrames() {
        return numberOfFrames;
    }

    /**
     *
     * @return The number of frames within the Audio File, calculated by dividing the filesize by
     * the number of frames, this may not be the most accurate method available.
     */
    public long getNumberOfFramesEstimate() {
        return numberOfFramesEstimate;
    }

    /**
     * Set the time each frame contributes to the audio in fractions of seconds, the higher
     * the sampling rate the shorter the audio segment provided by the frame,
     * the number of samples is fixed by the MPEG Version and Layer
     */
    private void setTimePerFrame() {
        timePerFrame = mp3FrameHeader.getNoOfSamples() / mp3FrameHeader.getSamplingRate().doubleValue();
        if ((mp3FrameHeader.getVersion() == MPEGFrameHeader.VERSION_2) || (mp3FrameHeader.getVersion() == MPEGFrameHeader.VERSION_2_5)) {
            if ((mp3FrameHeader.getLayer() == MPEGFrameHeader.LAYER_II) || (mp3FrameHeader.getLayer() == MPEGFrameHeader.LAYER_III)) {
                timePerFrame = timePerFrame / 2;
            }
        }
    }

    /**
     *
     * @return the the time each frame contributes to the audio in fractions of seconds
     */
    private double getTimePerFrame() {
        return timePerFrame;
    }

    /**
     * Estimate the length of the audio track in seconds
     * Calculation is Number of frames multiplied by the Time Per Frame using the first frame as a prototype
     * Time Per Frame is the number of samples in the frame (which is defined by the MPEGVersion/Layer combination)
     * divided by the sampling rate, i.e the higher the sampling rate the shorter the audio represented by the frame is going
     * to be.
     */
    private void setTrackLength() {
        trackLength = numberOfFrames * getTimePerFrame();
    }

    /**
     *
     * @return Track Length in seconds
     */
    public double getPreciseTrackLength() {
        return trackLength;
    }

    public int getTrackLength() {
        return (int) getPreciseTrackLength();
    }

    /**
     * Return the length in user friendly format
     */
    public String getTrackLengthAsString() {
        try {
            final long lengthInSecs = (long) getTrackLength();
            final Date timeIn = timeInFormat.parse(String.valueOf(lengthInSecs));
            return timeOutFormat.format(timeIn);
        } catch (ParseException pe) {
            return "";
        }
    }

    /**
     *
     * @return the audio file type
     */
    public String getEncodingType() {
        return TYPE_MP3;
    }

    /**
     * Set bitrate in kbps, if Vbr use Xingheader if possible
     */
    private void setBitRate() {
        if (mp3XingFrame != null && mp3XingFrame.isVbr()) {
            if (mp3XingFrame.isAudioSizeEnabled() && mp3XingFrame.getAudioSize() > 0) {
                bitrate = (long) ((mp3XingFrame.getAudioSize() * CONVERTS_BYTE_TO_BITS) / (timePerFrame * getNumberOfFrames() * CONVERT_TO_KILOBITS));
            } else {
                bitrate = (long) (((fileSize - startByte) * CONVERTS_BYTE_TO_BITS) / (timePerFrame * getNumberOfFrames() * CONVERT_TO_KILOBITS));
            }
        } else {
            bitrate = mp3FrameHeader.getBitRate();
        }
    }

    private void setEncoder() {
        if (mp3XingFrame != null) {
            if (mp3XingFrame.getLameFrame() != null) {
                encoder = mp3XingFrame.getLameFrame().getEncoder();
                return;
            }
        }
    }

    /**
     *
     * @return bitrate in kbps, no indicator is provided as to whether or not it is vbr
     */
    public long getBitRateAsNumber() {
        return bitrate;
    }

    /**
     *
     * @return the BitRate of the Audio, to distinguish cbr from vbr we add a '~'
     * for vbr.
     */
    public String getBitRate() {
        if (mp3XingFrame != null && mp3XingFrame.isVbr() == true) {
            return isVbrIdentifier + String.valueOf(bitrate);
        } else {
            return String.valueOf(bitrate);
        }
    }

    /**
     *
     * @return the sampling rate in Hz
     */
    public int getSampleRateAsNumber() {
        return mp3FrameHeader.getSamplingRate();
    }

    /**

     * @return  the sampling rate as string
     */
    public String getSampleRate() {
        return String.valueOf(mp3FrameHeader.getSamplingRate());
    }

    /**
     *
     * @return MPEG Version (1-3)
     */
    public String getMpegVersion() {
        return mp3FrameHeader.getVersionAsString();
    }

    /**
     *
     * @return  MPEG Layer (1-3)
     */
    public String getMpegLayer() {
        return mp3FrameHeader.getLayerAsString();
    }

    /**
     *
     * @return the format of the audio (i.e. MPEG-1 Layer3)
     */
    public String getFormat() {
        return mp3FrameHeader.getVersionAsString() + " " + mp3FrameHeader.getLayerAsString();
    }

    /**
     *
     * @return the Channel Mode such as Stero or Mono
     */
    public String getChannels() {
        return mp3FrameHeader.getChannelModeAsString();
    }

    /**
     *
     * @return Emphasis
     */
    public String getEmphasis() {
        return mp3FrameHeader.getEmphasisAsString();
    }

    /**
     *
     * @return if the bitrate is variable, Xing header takes precedence if we have one
     */
    public boolean isVariableBitRate() {
        if (mp3XingFrame != null) {
            return mp3XingFrame.isVbr();
        } else {
            return mp3FrameHeader.isVariableBitRate();
        }
    }

    public boolean isProtected() {
        return mp3FrameHeader.isProtected();
    }

    public boolean isPrivate() {
        return mp3FrameHeader.isPrivate();
    }

    public boolean isCopyrighted() {
        return mp3FrameHeader.isCopyrighted();
    }

    public boolean isOriginal() {
        return mp3FrameHeader.isOriginal();
    }

    public boolean isPadding() {
        return mp3FrameHeader.isPadding();
    }

    /**
     *
     * @return encoder
     */
    public String getEncoder() {
        return encoder;
    }

    /**
     * Set the size of the file, required in some calculations
     * @param fileSize
     */
    private void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     *
     * @return a string represntation
     */
    public String toString() {
        String s = "fileSize:" + fileSize + " encoder:" + encoder + " startByte:" + startByte + " numberOfFrames:" + numberOfFrames + " numberOfFramesEst:" + numberOfFramesEstimate + " timePerFrame:" + timePerFrame + " bitrate:" + bitrate + " trackLength:" + getTrackLengthAsString();
        if (this.mp3FrameHeader != null) {
            s += mp3FrameHeader.toString();
        }
        if (this.mp3XingFrame != null) {
            s += mp3XingFrame.toString();
        }
        return s;
    }

    public static void main(String[] args) throws Exception {
        MP3AudioHeader head = new MP3AudioHeader(new File("D:\\执着.flac"));
        System.out.println(head.getSampleRate());
    }
}
