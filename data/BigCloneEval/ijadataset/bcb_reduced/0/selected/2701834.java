package harvestmars;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author d_frEak
 */
public class backsound {

    File soundFile;

    AudioInputStream audioIn;

    Clip clip;

    public void play(int a) throws LineUnavailableException, UnsupportedAudioFileException, IOException {
        if (a == 0) {
            soundFile = new File("sound\\title.wav");
        } else if (a == 1) {
            soundFile = new File("sound\\1.wav");
        } else if (a == 2) {
            soundFile = new File("sound\\2.wav");
        } else if (a == 3) {
            soundFile = new File("sound\\3.wav");
        }
        audioIn = AudioSystem.getAudioInputStream(soundFile);
        clip = AudioSystem.getClip();
        clip.open(audioIn);
        clip.start();
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop() {
    }
}
