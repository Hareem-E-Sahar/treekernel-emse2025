public class Test {    public NokiaWavePlayer(final WaveDataProducer producer) {
        boolean isWavSupported = false;
        final int[] formats = com.nokia.mid.sound.Sound.getSupportedFormats();
        for (int i = 0; !isWavSupported && i < formats.length; ++i) {
            isWavSupported |= formats[i] == com.nokia.mid.sound.Sound.FORMAT_WAV;
        }
        if (!isWavSupported) {
            throw new RuntimeException("Wav format not supported by this player!");
        }
        this.producer = producer;
        WAV_HEADER[22] = (short) producer.getChannels();
        WAV_HEADER[32] = (short) producer.getChannels();
        WAV_HEADER[34] = (short) producer.getBitsPerSample();
        final int defaultBufferSize = producer.getSampleRate() / 10;
        final int bs = defaultBufferSize + WAV_HEADER.length;
        for (int i = 0, shift = 0; i < 4; ++i, shift += 8) {
            WAV_HEADER[4 + i] = (short) ((bs >> shift) & 0xff);
            WAV_HEADER[40 + i] = (short) ((defaultBufferSize >> shift) & 0xff);
            WAV_HEADER[24 + i] = (short) ((producer.getSampleRate() >> shift) & 0xff);
            WAV_HEADER[28 + i] = (short) (((producer.getSampleRate() * producer.getChannels() * producer.getBitsPerSample() / 8) >> shift) & 0xff);
        }
        for (int i = 0; i < this.buffers.length; ++i) {
            this.buffers[i] = new byte[bs];
            initBuffer(this.buffers[i]);
            this.players[i] = new com.nokia.mid.sound.Sound(this.buffers[i], com.nokia.mid.sound.Sound.FORMAT_WAV);
        }
    }
}