package util;

import java.io.IOException;
import jflac.ChannelData;
import frame.Frame;
import metadata.StreamInfo;

/**
 * This class is a PCM FLAC decoder.
 * @author kc7bfi
 */
public class PCMDecoder {

    private long totalSamples;

    private int channels;

    private int bps;

    private int sampleRate;

    private int samplesProcessed = 0;

    private int frameCounter = 0;

    private ByteData buf;

    /**
     * The constructor.
     * @param streamInfo    The FLAC stream info
     */
    public PCMDecoder(StreamInfo streamInfo) {
        this.totalSamples = streamInfo.getTotalSamples();
        this.channels = streamInfo.getChannels();
        this.bps = streamInfo.getBitsPerSample();
        this.sampleRate = streamInfo.getSampleRate();
        this.buf = new ByteData(streamInfo.getMaxFrameSize());
    }

    /**
     * Write a WAV frame record.
     * @param frame         The FLAC frame
     * @param channelData   The decoded channel data
     * @return returns the decoded buffer data
     * @throws IOException  Thrown if error writing to output channel
     */
    public ByteData getFrame(Frame frame, ChannelData[] channelData) throws IOException {
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
                        buf.append((byte) (channelData[channel].getOutput()[wideSample] + 0x80));
                    }
                } else {
                    for (sample = wideSample = 0; wideSample < wideSamples; wideSample++) for (channel = 0; channel < channels; channel++) buf.append((byte) (channelData[channel].getOutput()[wideSample]));
                }
            } else if (bps == 16) {
                if (isUnsignedSamples) {
                    for (sample = wideSample = 0; wideSample < wideSamples; wideSample++) for (channel = 0; channel < channels; channel++) {
                        short val = (short) (channelData[channel].getOutput()[wideSample] + 0x8000);
                        buf.append((byte) (val & 0xff));
                        buf.append((byte) ((val >> 8) & 0xff));
                    }
                } else {
                    for (sample = wideSample = 0; wideSample < wideSamples; wideSample++) for (channel = 0; channel < channels; channel++) {
                        short val = (short) (channelData[channel].getOutput()[wideSample]);
                        buf.append((byte) (val & 0xff));
                        buf.append((byte) ((val >> 8) & 0xff));
                    }
                }
            } else if (bps == 24) {
                if (isUnsignedSamples) {
                    for (sample = wideSample = 0; wideSample < wideSamples; wideSample++) for (channel = 0; channel < channels; channel++) {
                        int val = (channelData[channel].getOutput()[wideSample] + 0x800000);
                        buf.append((byte) (val & 0xff));
                        buf.append((byte) ((val >> 8) & 0xff));
                        buf.append((byte) ((val >> 16) & 0xff));
                    }
                } else {
                    for (sample = wideSample = 0; wideSample < wideSamples; wideSample++) for (channel = 0; channel < channels; channel++) {
                        int val = (channelData[channel].getOutput()[wideSample]);
                        buf.append((byte) (val & 0xff));
                        buf.append((byte) ((val >> 8) & 0xff));
                        buf.append((byte) ((val >> 16) & 0xff));
                    }
                }
            }
        }
        return buf;
    }
}
