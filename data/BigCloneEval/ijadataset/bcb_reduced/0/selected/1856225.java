package org.spantus.demo.audio;

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.spantus.logger.Logger;

public class DemoAudioManager implements AudioManager {

    Logger log = Logger.getLogger(getClass());

    public void play(URL fileURL) {
        AudioInputStream stream = createInput(fileURL);
        play(stream, 0, getTotalTime(stream));
    }

    public void play(URL fileURL, float from, float length) {
        AudioInputStream stream = createInput(fileURL);
        if (from == 0 && length == 0) {
            length = getTotalTime(stream);
        }
        play(stream, from, length);
    }

    private void play(AudioInputStream stream, float from, float length) {
        log.debug("[play] from: " + from + "; length=" + length);
        double totalTime = getTotalTime(stream);
        long size = stream.getFrameLength();
        double byteTimeRate = totalTime / size;
        double ends = from + length;
        if (from > totalTime || ends > totalTime) {
            return;
        }
        long startsBytes = (long) ((from * 2) / byteTimeRate);
        long endBytes = (long) ((ends * 2) / byteTimeRate);
        long lengthBytes = endBytes - startsBytes;
        Playback pl = new Playback(stream, startsBytes, lengthBytes);
        pl.start();
    }

    /**
	 * 
	 * @return
	 */
    protected float getTotalTime(AudioInputStream stream) {
        float totalTime = (stream.getFrameLength() / stream.getFormat().getFrameRate());
        return totalTime;
    }

    private AudioInputStream createInput(URL fileURL) {
        AudioInputStream stream = null;
        try {
            stream = AudioSystem.getAudioInputStream(fileURL);
            AudioFormat format = stream.getFormat();
            if ((format.getEncoding() == AudioFormat.Encoding.ULAW) || (format.getEncoding() == AudioFormat.Encoding.ALAW)) {
                AudioFormat newFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(), format.getSampleSizeInBits() * 2, format.getChannels(), format.getFrameSize() * 2, format.getFrameRate(), true);
                stream = AudioSystem.getAudioInputStream(newFormat, stream);
                format = newFormat;
            }
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stream;
    }

    private class Playback extends Thread {

        private long starts;

        private long length;

        private AudioInputStream stream;

        private boolean playing;

        /**
		 * 
		 */
        public Playback(AudioInputStream stream, long starts, long length) {
            this.stream = stream;
            this.starts = starts;
            this.length = length;
        }

        public AudioInputStream getStream() {
            return stream;
        }

        public void run() {
            playback(stream, starts, length);
        }

        /**
		 * set up the SourceDataLine going to the JVM's mixer
		 * 
		 */
        private SourceDataLine createOutput(AudioFormat format) {
            SourceDataLine line = null;
            try {
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                log.debug("[createOutput] opened output line: " + info.toString());
                if (!AudioSystem.isLineSupported(info)) {
                    log.error("[createOutput]Line does not support: " + format);
                }
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format);
            } catch (Exception e) {
                log.error("create output throwed exception" + e);
            }
            return line;
        }

        /**
		 * 
		 * @param stream
		 * @param starts
		 * @param length
		 */
        private void playback(AudioInputStream stream, long starts, long length) {
            SourceDataLine line = createOutput(stream.getFormat());
            byte buffer[] = new byte[line.getBufferSize()];
            line.start();
            try {
                int byteCount;
                long totalByte = 0;
                long skiped = 0;
                long readSize = Math.min(starts, buffer.length);
                while ((skiped = stream.skip(readSize)) > 0 && totalByte < starts && isPlaying()) {
                    totalByte += skiped;
                    if ((starts - (totalByte + buffer.length)) < buffer.length) {
                        readSize = starts - totalByte;
                    }
                }
                totalByte = 0;
                readSize = Math.min(length, buffer.length);
                setPlaying(true);
                while ((byteCount = stream.read(buffer, 0, (int) readSize)) > 0 && totalByte < length && isPlaying()) {
                    byte[] proceedBuf = preprocessSamples(buffer, byteCount);
                    if (byteCount > 0) {
                        line.write(proceedBuf, 0, byteCount);
                    }
                    totalByte += byteCount;
                    readSize = Math.min((length - totalByte), readSize);
                }
                line.drain();
                line.stop();
                line.close();
            } catch (IOException e) {
                setPlaying(false);
                e.printStackTrace();
                log.error("[playback(long,long)]: " + e.getMessage());
            }
        }

        private byte[] preprocessSamples(byte[] samples, int numBytes) {
            return samples;
        }

        public boolean isPlaying() {
            return playing;
        }

        public void setPlaying(boolean playing) {
            this.playing = playing;
        }
    }
}
