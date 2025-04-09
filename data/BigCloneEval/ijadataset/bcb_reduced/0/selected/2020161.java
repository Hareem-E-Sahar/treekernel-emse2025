package uk.co.thirstybear.hectorj.sounds;

import javax.sound.sampled.*;
import java.net.URL;
import java.io.File;
import java.util.logging.Logger;

public class SoundPlayerConcrete implements SoundPlayer {

    private static final Logger logger = Logger.getLogger("uk.co.thirstybear.hectorj.sounds");

    public void playClip(String clip) {
        if (clip != null && !clip.equals("")) {
            logger.info("Playing clip: " + clip);
            try {
                File soundFile = new File(clip);
                URL clipURL = soundFile.toURI().toURL();
                final Clip clickClip = AudioSystem.getClip();
                clickClip.addLineListener(new LineListener() {

                    public void update(LineEvent evt) {
                        if (evt.getType() == LineEvent.Type.STOP) {
                            clickClip.close();
                        }
                    }
                });
                AudioInputStream ais = AudioSystem.getAudioInputStream(clipURL);
                clickClip.open(ais);
                clickClip.start();
            } catch (Exception e) {
                System.err.println("There was a problem playing the file:" + clip);
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SoundPlayerConcrete player = new SoundPlayerConcrete();
        player.playClip("myclip");
    }
}
