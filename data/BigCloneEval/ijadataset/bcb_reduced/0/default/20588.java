import javax.sound.sampled.*;
import java.io.*;

public class Audio {

    /****************************************
	NAME: playSound
	DESCRIPTION: Play a sound file
	INPUTS:
			String soundFile - the name of the sound/audio file
	ALGORITHM:
		CREATE new THREAD
			Inside the run() method... which contains the code that executse when the thread runs.
			TRY
				Initiate Clip object for playing audio
				GET audio input stream from sound file
				OPEN the sound file
				START playing the sound file
			CATCH exception
				PRINT error
			END TRY-CATCH
		
		START the thread
	HISTORY: 
	RESOURCE:
	// http://stackoverflow.com/questions/26305/how-can-i-play-sound-in-java
	***************************************/
    public static synchronized void playSound(final String soundFile) {
        Thread soundThread = new Thread(new Runnable() {

            public void run() {
                try {
                    String url = Config.SOUNDS_DIR + soundFile;
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(url));
                    Clip clip = AudioSystem.getClip();
                    clip.open(inputStream);
                    clip.stop();
                    clip.setFramePosition(0);
                    clip.start();
                    url = null;
                    inputStream = null;
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
            }
        });
        soundThread.setName("Sound: " + soundFile);
        soundThread.start();
    }

    /****************************************
	NAME: playSound
	DESCRIPTION: Play a sound file
	INPUTS:
			String soundFile - the name of the sound/audio file
	ALGORITHM:
		TRY
			Create Clip object for playing audio
			GET audio input stream from sound file
			OPEN the sound file
		CATCH exception
			PRINT error
		END TRY-CATCH
		RETURN the Clip object
	HISTORY: 
	RESOURCE:
	// http://stackoverflow.com/questions/26305/how-can-i-play-sound-in-java
	***************************************/
    public static Clip loadSound(final String soundFile) {
        Clip clip = null;
        try {
            String url = Config.SOUNDS_DIR + soundFile;
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(url));
            clip = AudioSystem.getClip();
            clip.open(inputStream);
            url = null;
            inputStream = null;
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
        return clip;
    }

    /****************************************
	NAME: playSound
	DESCRIPTION: Play a sound file
	INPUTS:
			Clip clip - The clip object of the sound/audio file.
	ALGORITHM:
		CREATE new THREAD
			Inside the run() method... which contains the code that executes when the thread runs.
			START playing the sound file
		
		START the thread
	HISTORY: 
	RESOURCE:
	// http://stackoverflow.com/questions/26305/how-can-i-play-sound-in-java
	***************************************/
    public static synchronized void playSound(final Clip clip) {
        Thread soundThread = new Thread(new Runnable() {

            public void run() {
                clip.stop();
                clip.setFramePosition(0);
                clip.start();
            }
        });
        soundThread.setName("Sound");
        soundThread.start();
    }
}
