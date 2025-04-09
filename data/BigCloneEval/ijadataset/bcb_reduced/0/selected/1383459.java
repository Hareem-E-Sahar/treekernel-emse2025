package markgame2d.engine;

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * The SimpleClip class makes it easy to load and play audio clips
 */
public class MarkSimpleClip {

    protected URL url;

    /** load the clip into memory. */
    public MarkSimpleClip(String soundName) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if (null == soundName) throw new java.io.FileNotFoundException("Sound not found: (null)");
        url = getClass().getClassLoader().getResource(soundName);
        if (null == url) throw new java.io.FileNotFoundException("Sound not found: \"" + soundName + "\"");
    }

    /** play the clip (can call more than once) */
    public void play() {
        try {
            Clip clip = AudioSystem.getClip();
            AudioInputStream in = AudioSystem.getAudioInputStream(url);
            clip.open(in);
            clip.start();
        } catch (Exception e) {
        }
    }
}
