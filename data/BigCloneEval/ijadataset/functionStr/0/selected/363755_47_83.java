public class Test {    public MyMidiRenderer(AudioMixer mixer, FrinikaSequencer sequencer, long startTick, int ticks, float sampleRate) {
        this.mixer = mixer;
        this.sampleRate = sampleRate;
        this.sequence = (FrinikaSequence) sequencer.getSequence();
        this.sequencer = sequencer;
        this.tickPosition = startTick;
        sequencer.setTickPosition(startTick);
        startTimeInMicros = sequencer.getMicrosecondPosition();
        this.available = (int) (getNumberOfSamples(ticks) * 4);
        tickSamples = getNumberOfSamples(1);
        AudioProcess audioProcess = new AudioProcess() {

            public void close() {
            }

            public void open() {
            }

            public int processAudio(AudioBuffer buffer) {
                if (byteBuffer == null) byteBuffer = new byte[buffer.getSampleCount() * 2 * 2];
                int i = 0;
                for (int n = 0; n < buffer.getSampleCount(); n++) {
                    float floatSample = buffer.getChannel(0)[n];
                    short sample;
                    if (floatSample >= 1.0f) sample = 0x7fff; else if (floatSample <= -1.0f) sample = -0x8000; else sample = (short) (floatSample * 0x8000);
                    byteBuffer[i++] = (byte) ((sample & 0xff00) >> 8);
                    byteBuffer[i++] = (byte) (sample & 0xff);
                    floatSample = buffer.getChannel(1)[n];
                    if (floatSample >= 1.0f) sample = 0x7fff; else if (floatSample <= -1.0f) sample = -0x8000; else sample = (short) (floatSample * 0x8000);
                    byteBuffer[i++] = (byte) ((sample & 0xff00) >> 8);
                    byteBuffer[i++] = (byte) (sample & 0xff);
                }
                return AUDIO_OK;
            }
        };
        mixer.getMainBus().setOutputProcess(audioProcess);
    }
}