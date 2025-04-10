package org.spantus.android.audio;

public class RecordFormat {

    int sampleRate;

    int channelConfiguration;

    int audioEncoding;

    private int bufferSize;

    private int channels;

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int frequency) {
        this.sampleRate = frequency;
    }

    public int getChannelConfiguration() {
        return channelConfiguration;
    }

    public void setChannelConfiguration(int channelConfiguration) {
        this.channelConfiguration = channelConfiguration;
    }

    public int getAudioEncoding() {
        return audioEncoding;
    }

    public void setAudioEncoding(int audioEncoding) {
        this.audioEncoding = audioEncoding;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public String toString() {
        return "RecordFormat [frequency=" + sampleRate + ", channelConfiguration=" + channelConfiguration + ", audioEncoding=" + audioEncoding + ", bufferSize=" + bufferSize + "]";
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }
}
