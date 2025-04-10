package com.golden.gamedev.engine.audio;

import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.Mixer;
import com.golden.gamedev.engine.BaseAudioRenderer;

/**
 * Play wave sound (*.wav, *.au).
 */
public class WaveRenderer extends BaseAudioRenderer implements LineListener {

    /** ************************* WAVE PROPERTIES ******************************* */
    private Clip clip;

    /** ********************* VALIDATING WAVE RENDERER ************************** */
    private static boolean available;

    private static boolean volumeSupported;

    private static Mixer mixer;

    private static final int UNINITIALIZED = 0;

    private static final int INITIALIZING = 1;

    private static final int INITIALIZED = 2;

    private static int rendererStatus = WaveRenderer.UNINITIALIZED;

    /**
   * Creates new wave audio renderer.
   */
    public WaveRenderer() {
        if (WaveRenderer.rendererStatus == WaveRenderer.UNINITIALIZED) {
            WaveRenderer.rendererStatus = WaveRenderer.INITIALIZING;
            Thread thread = new Thread() {

                public final void run() {
                    try {
                        URL sample = WaveRenderer.class.getResource("Sample.wav");
                        AudioInputStream ain = AudioSystem.getAudioInputStream(sample);
                        AudioFormat format = ain.getFormat();
                        DataLine.Info info = new DataLine.Info(Clip.class, ain.getFormat(), ((int) ain.getFrameLength() * format.getFrameSize()));
                        Clip clip = (Clip) AudioSystem.getLine(info);
                        clip.open(ain);
                        WaveRenderer.volumeSupported = clip.isControlSupported(FloatControl.Type.VOLUME) || clip.isControlSupported(FloatControl.Type.MASTER_GAIN);
                        clip.drain();
                        clip.close();
                        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
                        for (int i = 0; i < mixers.length; i++) {
                            if ("Java Sound Audio Engine".equals(mixers[i].getName())) {
                                WaveRenderer.mixer = AudioSystem.getMixer(mixers[i]);
                            }
                        }
                        WaveRenderer.available = true;
                    } catch (Throwable e) {
                        System.err.println("WARNING: Wave audio playback is not available!");
                        WaveRenderer.available = false;
                    }
                    WaveRenderer.rendererStatus = WaveRenderer.INITIALIZED;
                }
            };
            thread.setDaemon(true);
            thread.start();
        }
    }

    public boolean isAvailable() {
        if (WaveRenderer.rendererStatus != WaveRenderer.INITIALIZED) {
            int i = 0;
            while (WaveRenderer.rendererStatus != WaveRenderer.INITIALIZED && i++ < 50) {
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                }
            }
            if (WaveRenderer.rendererStatus != WaveRenderer.INITIALIZED) {
                WaveRenderer.rendererStatus = WaveRenderer.INITIALIZED;
                WaveRenderer.available = false;
            }
        }
        return WaveRenderer.available;
    }

    /** ************************************************************************* */
    protected void playSound(URL audiofile) {
        try {
            if (this.clip != null) {
                this.clip.drain();
                this.clip.close();
            }
            AudioInputStream ain = AudioSystem.getAudioInputStream(this.getAudioFile());
            AudioFormat format = ain.getFormat();
            if ((format.getEncoding() == AudioFormat.Encoding.ULAW) || (format.getEncoding() == AudioFormat.Encoding.ALAW)) {
                AudioFormat temp = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(), format.getSampleSizeInBits() * 2, format.getChannels(), format.getFrameSize() * 2, format.getFrameRate(), true);
                ain = AudioSystem.getAudioInputStream(temp, ain);
                format = temp;
            }
            DataLine.Info info = new DataLine.Info(Clip.class, ain.getFormat(), ((int) ain.getFrameLength() * format.getFrameSize()));
            if (WaveRenderer.mixer != null) {
                this.clip = (Clip) WaveRenderer.mixer.getLine(info);
            } else {
                this.clip = (Clip) AudioSystem.getLine(info);
            }
            this.clip.addLineListener(this);
            this.clip.open(ain);
            this.clip.start();
            if (this.volume != 1.0f) {
                this.setSoundVolume(this.volume);
            }
        } catch (Exception e) {
            this.status = BaseAudioRenderer.ERROR;
            System.err.println("ERROR: Can not playing " + this.getAudioFile() + " caused by: " + e);
        }
    }

    protected void replaySound(URL audiofile) {
        this.clip.setMicrosecondPosition(0);
        this.clip.start();
        this.clip.addLineListener(this);
    }

    protected void stopSound() {
        this.clip.stop();
        this.clip.setMicrosecondPosition(0);
        this.clip.removeLineListener(this);
    }

    /**
   * Notified when the sound is stopped externally.
   */
    public void update(LineEvent e) {
        if (e.getType() == LineEvent.Type.STOP) {
            this.status = BaseAudioRenderer.END_OF_SOUND;
            this.clip.stop();
            this.clip.setMicrosecondPosition(0);
            this.clip.removeLineListener(this);
        }
    }

    /** ************************************************************************* */
    protected void setSoundVolume(float volume) {
        if (this.clip == null) {
            return;
        }
        Control.Type vol1 = FloatControl.Type.VOLUME, vol2 = FloatControl.Type.MASTER_GAIN;
        if (this.clip.isControlSupported(vol1)) {
            FloatControl volumeControl = (FloatControl) this.clip.getControl(vol1);
            volumeControl.setValue(volume);
        } else if (this.clip.isControlSupported(vol2)) {
            FloatControl gainControl = (FloatControl) this.clip.getControl(vol2);
            float dB = (float) (Math.log(((volume == 0.0) ? 0.0001 : volume)) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        }
    }

    public boolean isVolumeSupported() {
        return WaveRenderer.volumeSupported;
    }
}
