public class Test {    public void run() {
        try {
            if (url == null) throw new Exception("URL uninitialized.");
            if (url.getFile().endsWith(".mp3")) {
                testPlay(url);
            } else if (url.getFile().endsWith(".wav") || url.getFile().endsWith(".aiff") || url.getFile().endsWith(".au")) {
                url.openConnection();
                AudioInputStream soundIn = AudioSystem.getAudioInputStream(new BufferedInputStream(url.openStream()));
                AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 16, 2, 4, AudioSystem.NOT_SPECIFIED, true);
                DataLine.Info info = new DataLine.Info(Clip.class, format);
                clip = (Clip) AudioSystem.getLine(info);
                clip.open(soundIn);
                clip.start();
            } else {
                throw new Exception("Unsupported File Type");
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}