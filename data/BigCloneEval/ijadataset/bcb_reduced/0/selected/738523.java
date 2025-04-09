package sound;

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.*;

/**
 * This class was designed in order to manage the WAVE's type files. 
 * @author gustavo
 */
public class WavSoundManager extends SoundManager {

    private Clip clip;

    private FloatControl volumeControl;

    /**
     * Informes to the upper class the theme and the effects.
     * @param se informes the effect sound.
     * @param st informes the theme sound.
     */
    public WavSoundManager(soundEffects se, soundTheme st) {
        super(se, st);
    }

    /**
     * Informes to the upper class the theme, the effects and the volume of a sound.
     * @param se informes the sound's effect.
     * @param st informes the sound's theme.
     * @param volume informes the sound's volume.
     */
    public WavSoundManager(soundEffects se, soundTheme st, int volume) {
        super(se, st, volume);
    }

    @Override
    public void setVolume(int newVolume) {
        if (volumeControl == null) return;
        float volume;
        volume = (float) (volumeControl.getMaximum() - volumeControl.getMinimum());
        volume *= (float) Math.log(2 * newVolume) / Math.log(2 * 100);
        volumeControl.setValue(volume + volumeControl.getMinimum());
    }

    @Override
    public void play() {
        if (volumeControl == null) return;
        if (volumeControl.getValue() != volumeControl.getMinimum()) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        }
    }

    @Override
    public void setLoop() {
        if (clip == null) return;
        clip.loop(clip.LOOP_CONTINUOUSLY);
    }

    @Override
    public void stopSound() {
        if (clip == null) return;
        clip.stop();
    }

    @Override
    protected void setUp(soundTheme theme, soundEffects effect, int volume) {
        try {
            URL url = this.getClass().getResource("tracks/" + theme + "_" + effect + ".wav");
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            setVolume(volume);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.err.println("It was not possible to start the sound");
        } catch (Exception e) {
            System.err.println("It was not possible to start the sound");
        }
    }
}
