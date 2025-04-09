package org.jbubblebreaker;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Play sounds
 * @author Sven Strickroth
 */
public class PlaySound extends Thread {

    /**
	 * Cache for the sound-clips
	 */
    private static Clip[] clip = new Clip[Sounds.values().length];

    private int soundID;

    /**
	 *Plays the sound(file) sound
	 * @param sound to play
	 */
    public PlaySound(Sounds sound) {
        super();
        if (JBubbleBreaker.getUserProperty("enableSound", "true").equalsIgnoreCase("true")) {
            soundID = sound.ordinal();
            start();
        }
    }

    @Override
    public void run() {
        try {
            if (clip[soundID] == null) {
                AudioInputStream stream = AudioSystem.getAudioInputStream(getClass().getResource(Sounds.values()[soundID].getFilename()));
                clip[soundID] = AudioSystem.getClip();
                clip[soundID].open(stream);
                clip[soundID].loop(0);
            }
            clip[soundID].setMicrosecondPosition(0);
            clip[soundID].start();
            clip[soundID].drain();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
