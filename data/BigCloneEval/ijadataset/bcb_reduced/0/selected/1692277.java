package org.exmaralda.folker.experiment;

import javax.sound.sampled.*;
import java.io.*;

/**
 *
 * @author thomas
 */
public class Experiment {

    /** Creates a new instance of Experiment */
    public Experiment(String soundFilename) throws Exception {
        File file = new File(soundFilename);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        int frameLength = (int) audioInputStream.getFrameLength();
        int frameSize = (int) audioInputStream.getFormat().getFrameSize();
        int numChannels = audioInputStream.getFormat().getChannels();
        int sampleSize = audioInputStream.getFormat().getSampleSizeInBits();
        System.out.println("Frame length: " + frameLength);
        System.out.println("Frame size: " + frameSize);
        System.out.println("Channels: " + numChannels);
        System.out.println("Sample size: " + sampleSize);
        System.out.println("DONE!");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            String fn = "T:\\TP-E5\\Mitarbeiter\\Hatice\\Digitalisierungsarbeit\\auch auf der externen Festplatte\\EFE10\\EFE10tk_Akin_0663\\EFE10tk_Akin_0663a_f_SKO_240201.wav";
            Experiment e = new Experiment(fn);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
