package com.anotherbigidea.flash.sound;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import com.anotherbigidea.flash.SWFConstants;
import com.anotherbigidea.flash.interfaces.SWFActionBlock;
import com.anotherbigidea.flash.interfaces.SWFActions;
import com.anotherbigidea.flash.interfaces.SWFTagTypes;
import com.anotherbigidea.flash.movie.Frame;
import com.anotherbigidea.flash.movie.Movie;
import com.anotherbigidea.flash.movie.Sound;
import com.anotherbigidea.flash.structs.Color;
import com.anotherbigidea.flash.writers.SWFWriter;
import com.anotherbigidea.flash.writers.TagWriter;
import com.anotherbigidea.io.OutStream;

/**
 * ADPCM Utilities
 */
public class ADPCMHelper {

    public class ADPCMPacket {

        public int initialLeftSample = 0;

        public int initialLeftIndex = 0;

        public int initialRightSample = 0;

        public int initialRightIndex = 0;

        public int[] leftData;

        public int[] rightData;

        public int sampleCount;
    }

    protected AudioInputStream audioIn;

    protected boolean isStereo;

    protected boolean is16Bit;

    protected int sampleRate;

    protected int rate;

    protected int samplesPerFrame;

    protected boolean isSigned;

    protected int sampleCount = 0;

    protected ADPCMEncodeStream leftEncoder;

    protected ADPCMEncodeStream rightEncoder;

    protected ADPCMPacket currentPacket;

    public ADPCMHelper(InputStream audioFile, int framesPerSecond) throws IOException, UnsupportedAudioFileException {
        audioIn = AudioSystem.getAudioInputStream(new BufferedInputStream(audioFile));
        AudioFormat format = audioIn.getFormat();
        int frameSize = format.getFrameSize();
        isStereo = format.getChannels() == 2;
        is16Bit = format.getSampleSizeInBits() > 8;
        sampleRate = (int) format.getSampleRate();
        isSigned = format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED;
        if (sampleRate >= 44000) sampleRate = 44000; else if (sampleRate >= 22000) sampleRate = 22000; else if (sampleRate >= 11000) sampleRate = 11000; else sampleRate = 5500;
        rate = SWFConstants.SOUND_FREQ_5_5KHZ;
        if (sampleRate == 44000) rate = SWFConstants.SOUND_FREQ_44KHZ; else if (sampleRate == 22000) rate = SWFConstants.SOUND_FREQ_22KHZ; else if (sampleRate == 11000) rate = SWFConstants.SOUND_FREQ_11KHZ;
        samplesPerFrame = sampleRate / framesPerSecond;
        FramedInputStream frameIn = new FramedInputStream(audioIn, frameSize);
        leftEncoder = new ADPCMEncodeStream(frameIn, is16Bit, isSigned);
        if (isStereo) {
            rightEncoder = new ADPCMEncodeStream(frameIn, is16Bit, isSigned);
        }
    }

    public Sound getSoundDefinition() throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        OutStream out = new OutStream(bout);
        sampleCount = 0;
        boolean first = true;
        for (ADPCMPacket packet = readPacket(ADPCMConstants.PACKET_SIZE); packet != null; packet = readPacket(ADPCMConstants.PACKET_SIZE)) {
            sampleCount += packet.sampleCount + 1;
            writePacket(packet, out, first);
            first = false;
        }
        out.flush();
        byte[] soundData = bout.toByteArray();
        return new Sound(SWFConstants.SOUND_FORMAT_ADPCM, rate, true, isStereo, sampleCount, soundData);
    }

    /**
     * @return null if no more packets are available
     */
    public ADPCMPacket readPacket(int packetSize) throws IOException {
        ADPCMPacket packet = new ADPCMPacket();
        packet.initialLeftSample = leftEncoder.getFirstPacketSample();
        if (leftEncoder.isDone()) return null;
        int count = 0;
        if (isStereo) {
            packet.initialRightSample = rightEncoder.getFirstPacketSample();
        }
        packet.initialLeftIndex = leftEncoder.setIndex(packet.initialLeftSample);
        if (isStereo) {
            packet.initialRightIndex = rightEncoder.setIndex(packet.initialRightSample);
        }
        packet.leftData = new int[packetSize - 1];
        ;
        if (isStereo) packet.rightData = new int[packetSize - 1];
        ;
        for (int i = 0; i < packetSize - 1; i++) {
            packet.leftData[i] = leftEncoder.getDelta();
            if (!leftEncoder.isDone()) count++;
            if (isStereo) packet.rightData[i] = rightEncoder.getDelta();
        }
        packet.sampleCount = count;
        return packet;
    }

    /**
     * Streaming block
     * @return null if no more blocks
     */
    public byte[] getBlockData(boolean firstBlock) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        OutStream os = new OutStream(bout);
        currentPacket = readPacket(samplesPerFrame);
        if (currentPacket == null) return null;
        writePacket(currentPacket, os, true);
        os.flushBits();
        return bout.toByteArray();
    }

    public void writePacket(ADPCMPacket packet, OutStream out, boolean includeBitCount) throws IOException {
        if (packet == null) return;
        int sample = packet.initialLeftSample;
        if (includeBitCount) out.writeUBits(2, 2);
        out.writeUBits(16, sample);
        out.writeUBits(6, packet.initialLeftIndex);
        if (isStereo) {
            sample = packet.initialRightSample;
            out.writeUBits(16, sample);
            out.writeUBits(6, packet.initialRightIndex);
        }
        for (int i = 0; i < packet.sampleCount; i++) {
            out.writeUBits(4, packet.leftData[i]);
            if (isStereo) out.writeUBits(4, packet.rightData[i]);
        }
    }

    public SoundStreamHead getStreamHeader() {
        return new SoundStreamHead(rate, true, isStereo, SWFConstants.SOUND_FORMAT_ADPCM, rate, true, isStereo, samplesPerFrame);
    }

    /**
     * InputStream wrapper that ensures AudioInputStream is read on a frame-by-frame basis
     */
    public static class FramedInputStream extends InputStream {

        protected InputStream in;

        protected byte[] frameData;

        protected int dataPtr;

        protected int frameSize;

        protected boolean done = false;

        public FramedInputStream(InputStream in, int frameSize) {
            this.in = in;
            this.frameSize = frameSize;
            frameData = new byte[frameSize];
            dataPtr = frameSize;
        }

        @Override
        public int read() throws IOException {
            if (dataPtr < frameData.length) {
                int val = frameData[dataPtr++];
                if (val < 0) val = val + 0x100;
                return val;
            }
            if (done) return -1;
            dataPtr = 0;
            int read = 0;
            while (dataPtr < frameSize && (read = in.read(frameData, dataPtr, frameSize - dataPtr)) >= 0) {
                dataPtr += read;
            }
            if (dataPtr == 0) {
                done = true;
                return -1;
            }
            while (dataPtr < frameData.length) frameData[dataPtr++] = 0;
            dataPtr = 0;
            return read();
        }
    }

    /**
     * Makes a non-streaming SWF from a Java Sound compatible audio file.
     * args[0] = audio in filename
     * args[1] = SWF out filename
     */
    public static void main(String[] args) throws Exception {
        InputStream audioFile = new FileInputStream(args[0]);
        Movie movie = new Movie();
        movie.setFrameRate(30);
        ADPCMHelper helper = new ADPCMHelper(audioFile, 30);
        Frame frame = movie.appendFrame();
        Sound sound = helper.getSoundDefinition();
        int frames = frame.startSound(sound, 30);
        while (frames-- > 0) frame = movie.appendFrame();
        frame.stop();
        audioFile.close();
        movie.write(args[1]);
    }

    /**
     * Makes a streaming SWF from a Java Sound compatible audio file.
     * args[0] = audio in filename
     * args[1] = SWF out filename
     */
    public static void main2(String[] args) throws Exception {
        InputStream audioFile = new FileInputStream(args[0]);
        SWFWriter swfwriter = new SWFWriter(args[1]);
        SWFTagTypes tags = new TagWriter(swfwriter);
        tags.header(5, -1, 200, 200, 12, -1);
        tags.tagSetBackgroundColor(new Color(255, 255, 255));
        ADPCMHelper helper = new ADPCMHelper(audioFile, 12);
        SoundStreamHead header = helper.getStreamHeader();
        header.write(tags);
        byte[] block = helper.getBlockData(true);
        while (block != null) {
            tags.tagSoundStreamBlock(block);
            tags.tagShowFrame();
            block = helper.getBlockData(false);
        }
        SWFActions acts = tags.tagDoAction();
        SWFActionBlock actblock = acts.start(0);
        actblock.stop();
        actblock.end();
        acts.done();
        tags.tagShowFrame();
        tags.tagEnd();
        audioFile.close();
    }
}
