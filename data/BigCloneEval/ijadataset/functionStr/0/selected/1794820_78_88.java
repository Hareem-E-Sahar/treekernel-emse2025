public class Test {    public static AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
        InputStream inputStream = new BufferedInputStream(url.openStream());
        try {
            if (DEBUG == true) {
                System.err.println("Using AppletMpegSPIWorkaround to get codec (AudioFileFormat:url)");
            }
            return getAudioFileFormat(inputStream);
        } finally {
            inputStream.close();
        }
    }
}