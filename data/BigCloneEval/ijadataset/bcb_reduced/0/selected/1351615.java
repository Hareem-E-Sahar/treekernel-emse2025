package antirashka.sound;

import antirashka.util.Log;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class Sounds {

    private static final Sounds INSTANCE = new Sounds();

    private final Map<Sound, Clip> clips = new HashMap<Sound, Clip>();

    private boolean soundAvailable = true;

    private synchronized void doPlay(Sound sound) {
        if (!soundAvailable) return;
        try {
            Clip clip = clips.get(sound);
            if (clip == null) {
                URL url = getClass().getResource("resources/" + sound.getFile());
                if (url != null) {
                    AudioInputStream is = AudioSystem.getAudioInputStream(url);
                    clip = AudioSystem.getClip();
                    clips.put(sound, clip);
                    clip.open(is);
                }
            }
            if (clip != null) {
                clip.setFramePosition(0);
                clip.start();
            }
        } catch (Exception ex) {
            soundAvailable = false;
            Log.error(ex);
        }
    }

    public static void play(Sound sound) {
        INSTANCE.doPlay(sound);
    }
}
