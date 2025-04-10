package jmri.jmrit.sound;

import java.io.ByteArrayOutputStream;
import java.io.File;
import javax.sound.sampled.*;

/**
 * Provide simple way to load and play Java 2 sounds in JMRI.
 * <P>
 * This is placed in the jmri.jmrit.sound package by process of
 * elimination.  It doesn't belong in the base jmri package, as
 * it's not a basic interface.  Nor is it a specific implementation
 * of a basic interface, which would put it in jmri.jmrix.  
 *
 *
 * @author	Bob Jacobsen  Copyright (C) 2004, 2006
 * @version	$Revision: 1.3 $
 */
public class SoundUtil {

    /**
     * Play a sound from a buffer
     *
     */
    public static void playSoundBuffer(byte[] wavData) {
        jmri.jmrit.sound.WavBuffer wb = new jmri.jmrit.sound.WavBuffer(wavData);
        float sampleRate = wb.getSampleRate();
        int sampleSizeInBits = wb.getSampleSizeInBits();
        int channels = wb.getChannels();
        boolean signed = wb.getSigned();
        boolean bigEndian = wb.getBigEndian();
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
        SourceDataLine line;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            log.error("line not supported: " + info);
            return;
        }
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
        } catch (LineUnavailableException ex) {
            log.error("error opening line: " + ex);
            return;
        }
        line.start();
        line.write(wavData, 0, wavData.length);
    }

    private static final int BUFFER_LENGTH = 4096;

    public static byte[] bufferFromFile(String filename, float sampleRate, int sampleSizeInBits, int channels, boolean signed, boolean bigEndian) throws java.io.IOException, javax.sound.sampled.UnsupportedAudioFileException {
        File sourceFile = new File(filename);
        AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(sourceFile);
        AudioFormat audioFormat = fileFormat.getFormat();
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
        AudioInputStream stream = AudioSystem.getAudioInputStream(sourceFile);
        AudioInputStream inputAIS = AudioSystem.getAudioInputStream(format, stream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int nBufferSize = BUFFER_LENGTH * audioFormat.getFrameSize();
        byte[] abBuffer = new byte[nBufferSize];
        while (true) {
            if (log.isDebugEnabled()) {
                log.debug("trying to read (bytes): " + abBuffer.length);
            }
            int nBytesRead = inputAIS.read(abBuffer);
            if (log.isDebugEnabled()) {
                log.debug("read (bytes): " + nBytesRead);
            }
            if (nBytesRead == -1) {
                break;
            }
            baos.write(abBuffer, 0, nBytesRead);
        }
        byte[] abAudioData = baos.toByteArray();
        return abAudioData;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SoundUtil.class.getName());
}
