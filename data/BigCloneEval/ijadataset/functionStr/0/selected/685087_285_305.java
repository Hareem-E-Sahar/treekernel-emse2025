public class Test {    private Clip createClip() throws LineUnavailableException, UnsupportedAudioFileException, IOException {
        Clip newClip = null;
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(getAudioData());
            AudioFormat format = ais.getFormat();
            AudioFormat.Encoding encoding = format.getEncoding();
            if ((encoding == AudioFormat.Encoding.ULAW) || (encoding == AudioFormat.Encoding.ALAW)) {
                AudioFormat tmp = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(), format.getSampleSizeInBits() * 2, format.getChannels(), format.getFrameSize() * 2, format.getFrameRate(), true);
                ais = AudioSystem.getAudioInputStream(tmp, ais);
                format = tmp;
            }
            LOG.debug("Using AudioFormat " + format);
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            newClip = (Clip) AudioSystem.getLine(info);
            newClip.open(ais);
            ais.close();
        } catch (IllegalArgumentException iae) {
            throw new VBoxException(iae.getMessage());
        }
        return newClip;
    }
}