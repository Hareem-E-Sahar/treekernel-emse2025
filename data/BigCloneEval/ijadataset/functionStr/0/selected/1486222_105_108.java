public class Test {    private int getAudioDataByteLength(int nPhase) {
        AudioFormat format = getAudioFormat();
        return getAudioDataSampleCount(nPhase) * ((format.getSampleSizeInBits() + 7) / 8) * format.getChannels();
    }
}