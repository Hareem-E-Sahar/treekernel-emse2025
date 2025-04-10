package jaco.mp3.player.resources;

import jaco.mp3.player.resources.player.AudioDeviceBase;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * The <code>JavaSoundAudioDevice</code> implements an audio
 * device by using the JavaSound API.
 *
 * @since 0.0.8
 * @author Mat McGowan
 */
public class SoundDevice extends AudioDeviceBase {

    private SourceDataLine source = null;

    private AudioFormat fmt = null;

    private byte[] byteBuf = new byte[4096];

    protected void setAudioFormat(AudioFormat fmt0) {
        fmt = fmt0;
    }

    protected AudioFormat getAudioFormat() {
        if (fmt == null) {
            Decoder decoder = getDecoder();
            fmt = new AudioFormat(decoder.getOutputFrequency(), 16, decoder.getOutputChannels(), true, false);
        }
        return fmt;
    }

    protected DataLine.Info getSourceLineInfo() {
        AudioFormat fmt = getAudioFormat();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
        return info;
    }

    public void open(AudioFormat fmt) throws JavaLayerException {
        if (!isOpen()) {
            setAudioFormat(fmt);
            openImpl();
            setOpen(true);
        }
    }

    protected void openImpl() throws JavaLayerException {
    }

    protected void createSource() throws JavaLayerException {
        Throwable t = null;
        try {
            Line line = AudioSystem.getLine(getSourceLineInfo());
            if (line instanceof SourceDataLine) {
                source = (SourceDataLine) line;
                source.open(fmt);
                source.start();
            }
        } catch (RuntimeException ex) {
            t = ex;
        } catch (LinkageError ex) {
            t = ex;
        } catch (LineUnavailableException ex) {
            t = ex;
        }
        if (source == null) throw new JavaLayerException("cannot obtain source audio line", t);
    }

    public int millisecondsToBytes(AudioFormat fmt, int time) {
        return (int) (time * (fmt.getSampleRate() * fmt.getChannels() * fmt.getSampleSizeInBits()) / 8000.0);
    }

    protected void closeImpl() {
        if (source != null) {
            source.close();
        }
    }

    private int volume = 100;

    public void setVolume(int volume) {
        this.volume = volume;
    }

    protected void writeImpl(short[] samples, int offs, int len) throws JavaLayerException {
        if (source == null) {
            createSource();
        }
        try {
            FloatControl gainControl = (FloatControl) source.getControl(FloatControl.Type.MASTER_GAIN);
            BooleanControl muteControl = (BooleanControl) source.getControl(BooleanControl.Type.MUTE);
            if (volume == 0) {
                muteControl.setValue(true);
            } else {
                muteControl.setValue(false);
                gainControl.setValue((float) (Math.log(volume / 100d) / Math.log(10.0) * 20.0));
            }
        } catch (Exception e) {
        }
        source.write(toByteArray(samples, offs, len), 0, len * 2);
    }

    protected byte[] getByteArray(int length) {
        if (byteBuf.length < length) {
            byteBuf = new byte[length + 1024];
        }
        return byteBuf;
    }

    protected byte[] toByteArray(short[] samples, int offs, int len) {
        byte[] b = getByteArray(len * 2);
        int idx = 0;
        short s;
        while (len-- > 0) {
            s = samples[offs++];
            b[idx++] = (byte) s;
            b[idx++] = (byte) (s >>> 8);
        }
        return b;
    }

    protected void flushImpl() {
        if (source != null) {
            source.drain();
        }
    }

    public int getPosition() {
        int pos = 0;
        if (source != null) {
            pos = (int) (source.getMicrosecondPosition() / 1000);
        }
        return pos;
    }

    /**
	 * Runs a short test by playing a short silent sound.
	 */
    public void test() throws JavaLayerException {
        try {
            open(new AudioFormat(22050, 16, 1, true, false));
            short[] data = new short[22050 / 10];
            write(data, 0, data.length);
            flush();
            close();
        } catch (RuntimeException ex) {
            throw new JavaLayerException("Device test failed: " + ex);
        }
    }
}
