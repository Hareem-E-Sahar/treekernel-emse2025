package com.riseOfPeople.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Singleton audio player
 * @author Roland
 */
public class SoundPlayer extends AudioPlayer implements Runnable {

    private static SoundPlayer soundPlayer = null;

    private int maxNrOfSounds = 32;

    private String[] soundQueue = new String[maxNrOfSounds];

    private int soundQueueIndex = 0;

    /**
	 * Private constructor to prevent creating new instances
	 * of itself outside the class
	 */
    private SoundPlayer() {
    }

    /**
	 * Get the player
	 * @return the audio player
	 */
    public static SoundPlayer getInstance() {
        if (soundPlayer == null) {
            soundPlayer = new SoundPlayer();
            soundPlayer.initializeAudio();
        }
        return soundPlayer;
    }

    /**
	 * Initialize all audio files
	 */
    @Override
    public void initializeAudio() {
        audioLibrary.put("thunderstorm", sourceLocation + "thunderstorm.wav");
        audioLibrary.put("explosion", sourceLocation + "explosion.wav");
        audioLibrary.put("siren", sourceLocation + "siren.wav");
        audioLibrary.put("attack", sourceLocation + "explosion.wav");
        audioLibrary.put("clicky", sourceLocation + "clicky.wav");
    }

    /**
	 * Play an audio file
	 * @param soundName
	 */
    public synchronized void play(String soundName) {
        if (soundQueueIndex + 1 < maxNrOfSounds) {
            soundQueue[soundQueueIndex] = soundName;
            increaseSoundQueue();
            Thread t = new Thread(soundPlayer);
            t.setName("Sound player");
            t.start();
        }
    }

    /**
	 * A function to get the audio file name, which is synchronized
	 * to prevent locks
	 * @param soundName
	 * @return fileName
	 */
    private String getAudioFile(String soundName) {
        synchronized (audioLibrary) {
            return audioLibrary.get(soundName);
        }
    }

    /**
	 * Increase the sound queue
	 */
    private synchronized void increaseSoundQueue() {
        soundQueueIndex += 1;
    }

    /**
	 * Lower the Sound queue
	 */
    private synchronized void decreaseSoundQueue() {
        soundQueueIndex -= 1;
    }

    /**
	 * Thread to run different sounds
	 */
    @Override
    public void run() {
        if (soundQueueIndex > 0) {
            decreaseSoundQueue();
            try {
                Clip audioClip = AudioSystem.getClip();
                String filePath = getAudioFile(soundQueue[soundQueueIndex]);
                if (filePath == null) {
                    System.out.println("Null detected @ soundplayer");
                }
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(super.getClass().getResourceAsStream(filePath));
                audioClip.open(audioStream);
                audioClip.start();
                while (audioClip.getFramePosition() < audioClip.getFrameLength()) {
                    Thread.sleep(500);
                }
                audioStream.close();
                audioClip.drain();
                audioClip.flush();
                audioClip.close();
                audioClip = null;
                audioStream = null;
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}
