package raptor.sound;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import raptor.Raptor;
import raptor.util.RaptorLogger;

/**
 * Uses Clips to play sounds. Ignores the encoding.
 */
public class JavaxSampledSoundPlayer implements SoundPlayer {

    private static final RaptorLogger LOG = RaptorLogger.getLog(JavaxSampledSoundPlayer.class);

    protected Map<String, Boolean> soundsPlaying = new HashMap<String, Boolean>();

    public void dispose() {
        LOG.info("Disposing Sounds");
        soundsPlaying.clear();
    }

    /**
	 * I have tried caching the Clips. However i ran out of lines. So now i just
	 * create a new clip each time.
	 */
    public void init() {
    }

    /**
	 * Specify the name of a file in resources/sounds/bughouse without the .wav
	 * to play the sound i.e. "+".
	 */
    public void play(final String pathToSound) {
        Boolean isPlaying = soundsPlaying.get(pathToSound);
        if (isPlaying == null || !isPlaying) {
            soundsPlaying.put(pathToSound, true);
            try {
                File soundFile = new File(pathToSound);
                AudioFileFormat inFileFormat = AudioSystem.getAudioFileFormat(soundFile);
                AudioFileFormat.Type fileType = inFileFormat.getType();
                final AudioInputStream stream = AudioSystem.getAudioInputStream(soundFile);
                if (AudioSystem.isFileTypeSupported(fileType, stream)) {
                    final Clip clip = AudioSystem.getClip();
                    clip.addLineListener(new LineListener() {

                        public void update(LineEvent arg0) {
                            LineEvent.Type type = arg0.getType();
                            if (type == LineEvent.Type.STOP || type == LineEvent.Type.CLOSE) {
                                try {
                                    soundsPlaying.put(pathToSound, false);
                                    clip.drain();
                                    clip.close();
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                } finally {
                                    try {
                                        clip.close();
                                    } catch (Throwable t) {
                                        t.printStackTrace();
                                    }
                                    try {
                                        stream.close();
                                    } catch (Throwable t) {
                                        t.printStackTrace();
                                    }
                                }
                            }
                        }
                    });
                    clip.open(stream);
                    clip.start();
                }
            } catch (Throwable t) {
                Raptor.getInstance().onError("Error playing sound " + pathToSound, t);
                soundsPlaying.put(pathToSound, false);
            }
        }
    }

    public void playBughouseSound(final String sound) {
        play(Raptor.RESOURCES_DIR + "sounds/bughouse/" + sound + ".wav");
    }

    public void playSound(final String sound) {
        play(Raptor.RESOURCES_DIR + "sounds/" + sound + ".wav");
    }
}
