package org.lwjgl.test.openal;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

/**
 *
 * This is a basic play test
 * Yes, over zealous use of getError ;)
 *
 * @author Brian Matzon <brian@matzon.dk>
 * @version $Revision: 2983 $
 * $Id: PlayTest.java 2983 2008-04-07 18:36:09Z matzon $
 */
public class PlayTest extends BasicTest {

    private boolean usingVorbis;

    /**
     * Creates an instance of PlayTest
     */
    public PlayTest() {
        super();
    }

    /**
     * Runs the actual test, using supplied arguments
     */
    protected void execute(String[] args) {
        if (args.length < 1) {
            System.out.println("no argument supplied, assuming Footsteps.wav");
            args = new String[] { "Footsteps.wav" };
        }
        if (args[0].endsWith(".ogg")) {
            System.out.print("Attempting to load Ogg Vorbis file, checking for extension...");
            if (AL10.alIsExtensionPresent("AL_EXT_vorbis")) {
                System.out.println("found");
                usingVorbis = true;
            } else {
                System.out.println("not supported");
                alExit();
                System.exit(-1);
            }
        }
        int lastError;
        IntBuffer buffers = BufferUtils.createIntBuffer(1);
        IntBuffer sources = BufferUtils.createIntBuffer(1);
        buffers.position(0).limit(1);
        AL10.alGenBuffers(buffers);
        if ((lastError = AL10.alGetError()) != AL10.AL_NO_ERROR) {
            exit(lastError);
        }
        sources.position(0).limit(1);
        AL10.alGenSources(sources);
        if ((lastError = AL10.alGetError()) != AL10.AL_NO_ERROR) {
            exit(lastError);
        }
        if (usingVorbis) {
            ByteBuffer filebuffer = getData(args[0]);
            AL10.alBufferData(buffers.get(0), AL10.AL_FORMAT_VORBIS_EXT, filebuffer, -1);
            filebuffer.clear();
        } else {
            WaveData wavefile = WaveData.create(args[0]);
            AL10.alBufferData(buffers.get(0), wavefile.format, wavefile.data, wavefile.samplerate);
            wavefile.dispose();
        }
        if ((lastError = AL10.alGetError()) != AL10.AL_NO_ERROR) {
            exit(lastError);
        }
        AL10.alSourcei(sources.get(0), AL10.AL_BUFFER, buffers.get(0));
        if ((lastError = AL10.alGetError()) != AL10.AL_NO_ERROR) {
            exit(lastError);
        }
        AL10.alSourcei(sources.get(0), AL10.AL_LOOPING, AL10.AL_TRUE);
        if ((lastError = AL10.alGetError()) != AL10.AL_NO_ERROR) {
            exit(lastError);
        }
        AL10.alSourcePlay(sources.get(0));
        if ((lastError = AL10.alGetError()) != AL10.AL_NO_ERROR) {
            exit(lastError);
        }
        try {
            System.out.println("Waiting 5 seconds for sound to complete");
            Thread.sleep(5000);
        } catch (InterruptedException inte) {
        }
        AL10.alSourceStop(sources.get(0));
        if ((lastError = AL10.alGetError()) != AL10.AL_NO_ERROR) {
            exit(lastError);
        }
        sources.position(0).limit(1);
        AL10.alDeleteSources(sources);
        if ((lastError = AL10.alGetError()) != AL10.AL_NO_ERROR) {
            exit(lastError);
        }
        buffers.position(0).limit(1);
        AL10.alDeleteBuffers(buffers);
        if ((lastError = AL10.alGetError()) != AL10.AL_NO_ERROR) {
            exit(lastError);
        }
        alExit();
    }

    /**
     * Reads the file into a ByteBuffer
     *
     * @param filename Name of file to load
     * @return ByteBuffer containing file data
     */
    protected ByteBuffer getData(String filename) {
        ByteBuffer buffer = null;
        System.out.println("Attempting to load: " + filename);
        try {
            BufferedInputStream bis = new BufferedInputStream(WaveData.class.getClassLoader().getResourceAsStream(filename));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bufferLength = 4096;
            byte[] readBuffer = new byte[bufferLength];
            int read = -1;
            while ((read = bis.read(readBuffer, 0, bufferLength)) != -1) {
                baos.write(readBuffer, 0, read);
            }
            bis.close();
            buffer = ByteBuffer.allocateDirect(baos.size());
            buffer.order(ByteOrder.nativeOrder());
            buffer.put(baos.toByteArray());
            buffer.rewind();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
        return buffer;
    }

    /**
     * main entry point
     *
     * @param args String array containing arguments
     */
    public static void main(String[] args) {
        PlayTest playTest = new PlayTest();
        playTest.execute(args);
        System.exit(0);
    }
}
