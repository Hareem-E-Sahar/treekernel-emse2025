import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JApplet;

public class SoundTest {

    public SoundTest() {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File("media\\REMINDER.WAV"));
            try {
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                try {
                    clip.start();
                    clip.drain();
                } finally {
                    clip.close();
                }
            } catch (LineUnavailableException ex) {
                Logger.getLogger(SoundTest.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                ais.close();
            }
        } catch (UnsupportedAudioFileException ex) {
            Logger.getLogger(SoundTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SoundTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String args[]) {
        new SoundTest();
    }
}
