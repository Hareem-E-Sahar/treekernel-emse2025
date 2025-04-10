package util;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import jflac.ChannelData;
import jflac.Constants;
import frame.Frame;
import metadata.StreamInfo;

/**
 * Utility class for writing WAV files.
 * @author kc7bfi
 */
public class WavWriter {

    private static final int MAX_BLOCK_SIZE = 65535;

    private long totalSamples;

    private int channels;

    private int bps;

    private int sampleRate;

    private byte[] s8buffer = new byte[MAX_BLOCK_SIZE * Constants.MAX_CHANNELS * 4];

    private int samplesProcessed = 0;

    private int frameCounter = 0;

    private boolean needsFixup = false;

    private long riffOffset;

    private long dataOffset;

    private DataOutput os;

    private LittleEndianDataOutput osLE;

    /**
     * The constructor.
     * @param os            The output sream
     * @param streamInfo    The FLAC stream info
     */
    public WavWriter(DataOutput os, StreamInfo streamInfo) {
        this.os = os;
        this.osLE = new LittleEndianDataOutput(os);
        this.totalSamples = streamInfo.getTotalSamples();
        this.channels = streamInfo.getChannels();
        this.bps = streamInfo.getBitsPerSample();
        this.sampleRate = streamInfo.getSampleRate();
    }

    /**
     * The constructor.
     * @param os            The output sream
     */
    public WavWriter(DataOutput os) {
        this.os = os;
        this.osLE = new LittleEndianDataOutput(os);
    }

    /**
     * The constructor.
     * @param os            The output sream
     * @param streamInfo    The FLAC stream info
     */
    public WavWriter(OutputStream os, StreamInfo streamInfo) {
        this.os = new DataOutputStream(os);
        this.osLE = new LittleEndianDataOutput(this.os);
        this.totalSamples = streamInfo.getTotalSamples();
        this.channels = streamInfo.getChannels();
        this.bps = streamInfo.getBitsPerSample();
        this.sampleRate = streamInfo.getSampleRate();
    }

    /**
     * The constructor.
     * @param os            The output sream
     */
    public WavWriter(OutputStream os) {
        this.os = new DataOutputStream(os);
        this.osLE = new LittleEndianDataOutput(this.os);
    }

    /**
     * Write a WAV file header.
     * @throws IOException  Thrown if error writing to output string.
     */
    public void writeHeader() throws IOException {
        long dataSize = totalSamples * channels * ((bps + 7) / 8);
        if (totalSamples == 0) {
            if (!(os instanceof RandomAccessFile)) throw new IOException("Cannot seek in output stream");
            needsFixup = true;
        }
        os.write("RIFF".getBytes());
        if (needsFixup) riffOffset = ((RandomAccessFile) os).getFilePointer();
        osLE.writeInt((int) dataSize + 36);
        os.write("WAVEfmt ".getBytes());
        os.write(new byte[] { 0x10, 0x00, 0x00, 0x00 });
        os.write(new byte[] { 0x01, 0x00 });
        osLE.writeShort(channels);
        osLE.writeInt(sampleRate);
        osLE.writeInt(sampleRate * channels * ((bps + 7) / 8));
        osLE.writeShort(channels * ((bps + 7) / 8));
        osLE.writeShort(bps);
        os.write("data".getBytes());
        if (needsFixup) dataOffset = ((RandomAccessFile) os).getFilePointer();
        osLE.writeInt((int) dataSize);
    }

    /**
     * Write a WAV file header.
     * @param streamInfo    The FLAC stream info
     * @throws IOException  Thrown if error writing to output string.
     */
    public void writeHeader(StreamInfo streamInfo) throws IOException {
        this.totalSamples = streamInfo.getTotalSamples();
        this.channels = streamInfo.getChannels();
        this.bps = streamInfo.getBitsPerSample();
        this.sampleRate = streamInfo.getSampleRate();
        writeHeader();
    }

    /**
     * Write a WAV frame record.
     * @param frame         The FLAC frame
     * @param channelData   The decoded channel data
     * @throws IOException  Thrown if error writing to output channel
     */
    public void writeFrame(Frame frame, ChannelData[] channelData) throws IOException {
        boolean isUnsignedSamples = (bps <= 8);
        int wideSamples = frame.header.blockSize;
        int wideSample;
        int sample;
        int channel;
        if (wideSamples > 0) {
            samplesProcessed += wideSamples;
            frameCounter++;
            if (bps == 8) {
                if (isUnsignedSamples) {
                    for (sample = wideSample = 0; wideSample < wideSamples; wideSample++) for (channel = 0; channel < channels; channel++) {
                        s8buffer[sample++] = (byte) (channelData[channel].getOutput()[wideSample] + 0x80);
                    }
                } else {
                    for (sample = wideSample = 0; wideSample < wideSamples; wideSample++) for (channel = 0; channel < channels; channel++) s8buffer[sample++] = (byte) (channelData[channel].getOutput()[wideSample]);
                }
                os.write(s8buffer, 0, sample);
            } else if (bps == 16) {
                if (isUnsignedSamples) {
                    for (sample = wideSample = 0; wideSample < wideSamples; wideSample++) for (channel = 0; channel < channels; channel++) {
                        short val = (short) (channelData[channel].getOutput()[wideSample] + 0x8000);
                        s8buffer[sample++] = (byte) (val & 0xff);
                        s8buffer[sample++] = (byte) ((val >> 8) & 0xff);
                    }
                } else {
                    for (sample = wideSample = 0; wideSample < wideSamples; wideSample++) for (channel = 0; channel < channels; channel++) {
                        short val = (short) (channelData[channel].getOutput()[wideSample]);
                        s8buffer[sample++] = (byte) (val & 0xff);
                        s8buffer[sample++] = (byte) ((val >> 8) & 0xff);
                    }
                }
                os.write(s8buffer, 0, sample);
            } else if (bps == 24) {
                if (isUnsignedSamples) {
                    for (sample = wideSample = 0; wideSample < wideSamples; wideSample++) for (channel = 0; channel < channels; channel++) {
                        int val = (channelData[channel].getOutput()[wideSample] + 0x800000);
                        s8buffer[sample++] = (byte) (val & 0xff);
                        s8buffer[sample++] = (byte) ((val >> 8) & 0xff);
                        s8buffer[sample++] = (byte) ((val >> 16) & 0xff);
                    }
                } else {
                    for (sample = wideSample = 0; wideSample < wideSamples; wideSample++) for (channel = 0; channel < channels; channel++) {
                        int val = (channelData[channel].getOutput()[wideSample]);
                        s8buffer[sample++] = (byte) (val & 0xff);
                        s8buffer[sample++] = (byte) ((val >> 8) & 0xff);
                        s8buffer[sample++] = (byte) ((val >> 16) & 0xff);
                    }
                }
                os.write(s8buffer, 0, sample);
            }
        }
    }

    /**
     * Write the PCM data.
     * @param space The PCM data
     * @throws IOException  Thrown if error writing to file
     */
    public void writePCM(ByteData space) throws IOException {
        os.write(space.getData(), 0, space.getLen());
    }
}
