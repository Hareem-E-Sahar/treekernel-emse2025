public class Test {    public static final AudioInputStream createAudioInputStream(URL fileURL) throws UnsupportedAudioFileException, IOException {
        AudioInputStream stream = null;
        stream = AudioSystem.getAudioInputStream(fileURL);
        AudioFormat format = stream.getFormat();
        if ((format.getEncoding() == AudioFormat.Encoding.ULAW) || (format.getEncoding() == AudioFormat.Encoding.ALAW)) {
            AudioFormat newFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(), format.getSampleSizeInBits() * 2, format.getChannels(), format.getFrameSize() * 2, format.getFrameRate(), true);
            stream = AudioSystem.getAudioInputStream(newFormat, stream);
            format = newFormat;
        }
        return stream;
    }
}