public class Test {    protected void createLine() throws LineUnavailableException {
        log.debug("Create Line");
        if (m_line == null) {
            AudioFormat sourceFormat = m_audioInputStream.getFormat();
            log.debug("Create Line : Source format : " + sourceFormat.toString());
            int nSampleSizeInBits = sourceFormat.getSampleSizeInBits();
            if (nSampleSizeInBits <= 0) nSampleSizeInBits = 16;
            if ((sourceFormat.getEncoding() == AudioFormat.Encoding.ULAW) || (sourceFormat.getEncoding() == AudioFormat.Encoding.ALAW)) nSampleSizeInBits = 16;
            if (nSampleSizeInBits != 8) nSampleSizeInBits = 16;
            AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), nSampleSizeInBits, sourceFormat.getChannels(), sourceFormat.getChannels() * (nSampleSizeInBits / 8), sourceFormat.getSampleRate(), false);
            log.debug("Create Line : Target format: " + targetFormat);
            m_encodedaudioInputStream = m_audioInputStream;
            try {
                encodedLength = m_encodedaudioInputStream.available();
            } catch (IOException e) {
                log.error("Cannot get m_encodedaudioInputStream.available()", e);
            }
            m_audioInputStream = AudioSystem.getAudioInputStream(targetFormat, m_audioInputStream);
            AudioFormat audioFormat = m_audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, AudioSystem.NOT_SPECIFIED);
            Mixer mixer = getMixer(m_mixerName);
            if (mixer != null) {
                log.debug("Mixer : " + mixer.getMixerInfo().toString());
                m_line = (SourceDataLine) mixer.getLine(info);
            } else {
                m_line = (SourceDataLine) AudioSystem.getLine(info);
                m_mixerName = null;
            }
            log.debug("Line : " + m_line.toString());
            log.debug("Line Info : " + m_line.getLineInfo().toString());
            log.debug("Line AudioFormat: " + m_line.getFormat().toString());
        }
    }
}