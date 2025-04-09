package com.peterhi.media;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Audio {

    private static ExecutorService threadPool = Executors.newFixedThreadPool(2);

    public static void play(final InputStream in) {
        threadPool.execute(new Runnable() {

            public void run() {
                try {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream ais = AudioSystem.getAudioInputStream(in);
                    clip.open(ais);
                    clip.start();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
