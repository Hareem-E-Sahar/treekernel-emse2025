package com.chatcliente.principal;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 *
 * @author cracker
 */
public class Alerta implements Runnable {

    public void run() {
        try {
            Clip sonido = AudioSystem.getClip();
            sonido.open(AudioSystem.getAudioInputStream(getClass().getResource("/com/chatcliente/resources/alert.wav")));
            sonido.start();
            while (sonido.isRunning()) Thread.sleep(1000);
            sonido.close();
        } catch (Exception e) {
            System.out.println("" + e);
        }
        Thread.currentThread().interrupt();
    }
}
