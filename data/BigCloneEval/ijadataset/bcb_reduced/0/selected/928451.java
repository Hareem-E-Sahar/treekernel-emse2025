package org.spantus.core.wav;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.spantus.logger.Logger;

/**
 * 
 * @author Mindaugas Greibus
 *
 * @since 0.0.1
 * 
 * Created 2008.02.29
 * 
 * @deprecated it should be used Audio Factory Instead
 */
@SuppressWarnings("dep-ann")
public class WavReader {

    static Logger log = Logger.getLogger(WavReader.class);

    URL fileURL;

    int samplingFrq = 0;

    int bytesPerSample = 0;

    DataInputStream data;

    byte[] fact = new byte[4];

    String chunk = "";

    int skip;

    boolean ch = false;

    short channel, bsize;

    byte[] rbit = new byte[2];

    boolean eof = false;

    public WavReader(URL fileURL) throws UnsupportedAudioFileException, IOException {
        log.debug("[WavReader]++++");
        this.fileURL = fileURL;
        log.debug("[WavReader]----");
    }

    /**
	 * 
	 * @throws IOException
	 */
    public void init() throws IOException {
        try {
            data = new DataInputStream(new BufferedInputStream(fileURL.openStream()));
        } catch (FileNotFoundException e) {
            log.error("File not found! ");
            return;
        } catch (IOException ex) {
            log.error("[readData] IO error.");
            return;
        }
        data.skip(12);
        data.read(fact);
        chunk = new String(fact);
        data.read(fact);
        skip = WavUtils.btoi(fact);
        if (!chunk.equals("fmt ")) {
            data.skip(skip + 8);
            ch = true;
        }
        data.skip(2);
        data.read(rbit);
        channel = WavUtils.btos(rbit);
        if (channel == 2) ch = true;
        log.debug("[readData] channel " + channel);
        data.read(fact);
        samplingFrq = WavUtils.btoi(fact);
        log.debug("[readData] channel " + samplingFrq);
        data.skip(6);
        data.read(rbit);
        bsize = WavUtils.btos(rbit);
        bytesPerSample = bsize / 8;
        if (skip == 18) data.skip(2);
        data.read(fact);
        chunk = new String(fact);
        if (chunk.equals("fact")) {
            data.skip(12);
        } else if (!chunk.equals("data")) {
            log.error("<" + chunk + "> ?");
            return;
        }
        if (bsize != 16) {
            log.error("Sorry. 16bit only.");
            return;
        }
        log.debug("[readData] bsize " + bsize);
        data.read(fact);
        int readSamplesSize = WavUtils.btoi(fact);
        if (ch || bytesPerSample == 2) readSamplesSize = readSamplesSize >> 1;
        log.debug("[readData] readSamplesSize " + (readSamplesSize / 2));
    }

    public boolean hasNext() {
        return eof;
    }

    /**
	 * 
	 * @return
	 * @throws IOException
	 */
    public float readFloat() {
        try {
            float readSample;
            if (ch) {
                int left = WavUtils.bgtolt(data.readShort());
                int right = WavUtils.bgtolt(data.readShort());
                readSample = (float) ((left + right) / 2 * WavUtils.MIN_POSITIVE_SHORT);
                log.debug("[] stereo:  " + readSample);
            } else {
                Short readShort = new Short(data.readShort());
                float sample1 = (float) (WavUtils.bgtolt(readShort.shortValue()) * WavUtils.MIN_POSITIVE_SHORT);
                readShort = new Short(data.readShort());
                float sample2 = (float) (WavUtils.bgtolt(readShort.shortValue()) * WavUtils.MIN_POSITIVE_SHORT);
                readSample = (sample1 + sample2) / 2;
            }
            return readSample;
        } catch (IOException e) {
            eof = true;
        }
        return 0;
    }

    public void close() {
        try {
            data.close();
        } catch (IOException e) {
            log.error(e);
        }
    }

    public int getSamplingFrq() {
        return samplingFrq;
    }

    public void setSamplingFrq(int samplingFrq) {
        this.samplingFrq = samplingFrq;
    }
}
