public class Test {        private void openJavaSound(IStreamCoder aAudioCoder) {
            AudioFormat audioFormat = new AudioFormat(aAudioCoder.getSampleRate(), (int) IAudioSamples.findSampleBitDepth(aAudioCoder.getSampleFormat()), aAudioCoder.getChannels(), true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            try {
                mLine = (SourceDataLine) AudioSystem.getLine(info);
                mLine.open(audioFormat);
                mLine.start();
                Thread t = new Thread(new AudioFeeder());
                t.setName("Audio linefeeder thread");
                t.start();
            } catch (LineUnavailableException e) {
                throw new RuntimeException("could not open audio line");
            }
        }
}