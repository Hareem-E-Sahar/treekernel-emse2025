package org.lwjgl.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.lwjgl.openal.AL10;
import com.sun.media.sound.WaveFileReader;

/**
 *
 * Utitlity class for loading wavefiles.
 *
 * @author Brian Matzon <brian@matzon.dk>
 * @version $Revision: 3438 $
 * $Id: WaveData.java 3438 2010-10-12 15:34:26Z matzon $
 */
public class WaveData {

    /** actual wave data */
    public final ByteBuffer data;

    /** format type of data */
    public final int format;

    /** sample rate of data */
    public final int samplerate;

    /**
	 * Creates a new WaveData
	 * 
	 * @param data actual wavedata
	 * @param format format of wave data
	 * @param samplerate sample rate of data
	 */
    private WaveData(ByteBuffer data, int format, int samplerate) {
        this.data = data;
        this.format = format;
        this.samplerate = samplerate;
    }

    /**
	 * Disposes the wavedata
	 */
    public void dispose() {
        data.clear();
    }

    /**
	 * Creates a WaveData container from the specified url
	 * 
	 * @param path URL to file 
	 * @return WaveData containing data, or null if a failure occured
	 */
    public static WaveData create(URL path) {
        try {
            WaveFileReader wfr = new WaveFileReader();
            return create(wfr.getAudioInputStream(new BufferedInputStream(path.openStream())));
        } catch (Exception e) {
            org.lwjgl.LWJGLUtil.log("Unable to create from: " + path + ", " + e.getMessage());
            return null;
        }
    }

    /**
	 * Creates a WaveData container from the specified in the classpath
	 * 
	 * @param path path to file (relative, and in classpath) 
	 * @return WaveData containing data, or null if a failure occured
	 */
    public static WaveData create(String path) {
        return create(Thread.currentThread().getContextClassLoader().getResource(path));
    }

    /**
	 * Creates a WaveData container from the specified inputstream
	 * 
	 * @param is InputStream to read from 
	 * @return WaveData containing data, or null if a failure occured
	 */
    public static WaveData create(InputStream is) {
        try {
            return create(AudioSystem.getAudioInputStream(is));
        } catch (Exception e) {
            org.lwjgl.LWJGLUtil.log("Unable to create from inputstream, " + e.getMessage());
            return null;
        }
    }

    /**	
	 * Creates a WaveData container from the specified bytes
	 *
	 * @param buffer array of bytes containing the complete wave file
	 * @return WaveData containing data, or null if a failure occured
	 */
    public static WaveData create(byte[] buffer) {
        try {
            return create(AudioSystem.getAudioInputStream(new BufferedInputStream(new ByteArrayInputStream(buffer))));
        } catch (Exception e) {
            org.lwjgl.LWJGLUtil.log("Unable to create from byte array, " + e.getMessage());
            return null;
        }
    }

    /**	
	 * Creates a WaveData container from the specified ByetBuffer.
	 * If the buffer is backed by an array, it will be used directly, 
	 * else the contents of the buffer will be copied using get(byte[]).
	 *
	 * @param buffer ByteBuffer containing sound file
	 * @return WaveData containing data, or null if a failure occured
	 */
    public static WaveData create(ByteBuffer buffer) {
        try {
            byte[] bytes = null;
            if (buffer.hasArray()) {
                bytes = buffer.array();
            } else {
                bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
            }
            return create(bytes);
        } catch (Exception e) {
            org.lwjgl.LWJGLUtil.log("Unable to create from ByteBuffer, " + e.getMessage());
            return null;
        }
    }

    /**
	 * Creates a WaveData container from the specified stream
	 * 
	 * @param ais AudioInputStream to read from
	 * @return WaveData containing data, or null if a failure occured
	 */
    public static WaveData create(AudioInputStream ais) {
        AudioFormat audioformat = ais.getFormat();
        int channels = 0;
        if (audioformat.getChannels() == 1) {
            if (audioformat.getSampleSizeInBits() == 8) {
                channels = AL10.AL_FORMAT_MONO8;
            } else if (audioformat.getSampleSizeInBits() == 16) {
                channels = AL10.AL_FORMAT_MONO16;
            } else {
                assert false : "Illegal sample size";
            }
        } else if (audioformat.getChannels() == 2) {
            if (audioformat.getSampleSizeInBits() == 8) {
                channels = AL10.AL_FORMAT_STEREO8;
            } else if (audioformat.getSampleSizeInBits() == 16) {
                channels = AL10.AL_FORMAT_STEREO16;
            } else {
                assert false : "Illegal sample size";
            }
        } else {
            assert false : "Only mono or stereo is supported";
        }
        ByteBuffer buffer = null;
        try {
            int available = ais.available();
            if (available <= 0) {
                available = ais.getFormat().getChannels() * (int) ais.getFrameLength() * ais.getFormat().getSampleSizeInBits() / 8;
            }
            byte[] buf = new byte[ais.available()];
            int read = 0, total = 0;
            while ((read = ais.read(buf, total, buf.length - total)) != -1 && total < buf.length) {
                total += read;
            }
            buffer = convertAudioBytes(buf, audioformat.getSampleSizeInBits() == 16);
        } catch (IOException ioe) {
            return null;
        }
        WaveData wavedata = new WaveData(buffer, channels, (int) audioformat.getSampleRate());
        try {
            ais.close();
        } catch (IOException ioe) {
        }
        return wavedata;
    }

    private static ByteBuffer convertAudioBytes(byte[] audio_bytes, boolean two_bytes_data) {
        ByteBuffer dest = ByteBuffer.allocateDirect(audio_bytes.length);
        dest.order(ByteOrder.nativeOrder());
        ByteBuffer src = ByteBuffer.wrap(audio_bytes);
        src.order(ByteOrder.LITTLE_ENDIAN);
        if (two_bytes_data) {
            ShortBuffer dest_short = dest.asShortBuffer();
            ShortBuffer src_short = src.asShortBuffer();
            while (src_short.hasRemaining()) dest_short.put(src_short.get());
        } else {
            while (src.hasRemaining()) dest.put(src.get());
        }
        dest.rewind();
        return dest;
    }
}
