package model;

import java.applet.*;
import java.io.File;
import java.io.IOException;
import java.net.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * The Class Sound
 * 
 * @author Arjan Frans, Alwin Rombout, Jelte Verbree, Brendan Kanters
 * @version 1.0
 */
public class Sound {

    private AudioClip sound;

    public Sound() {
    }

    /**
   * play the sound
   */
    public void playSound() {
        try {
            URL url = this.getClass().getClassLoader().getResource("bomb.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
