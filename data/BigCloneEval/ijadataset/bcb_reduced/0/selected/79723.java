package net.laubenberger.bogatyr.helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFileFormat.Type;
import net.laubenberger.bogatyr.misc.exception.RuntimeExceptionIsNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a helper class for sound operations.
 *
 * @author Stefan Laubenberger
 * @version 0.9.4 (20101227)
 * @since 0.5.0
 */
public abstract class HelperSound {

    private static final Logger log = LoggerFactory.getLogger(HelperSound.class);

    /**
	 * Returns an audio {@link Clip} from a {@link File} (allowed types are wav, au, aifc, snd and aiff).
	 *
	 * @param file for audio clip
	 * @return Audio {@link Clip}
	 * @throws IOException
	 * @throws LineUnavailableException
	 * @throws UnsupportedAudioFileException
	 * @see File
	 * @see Clip
	 * @since 0.5.0
	 */
    public static Clip getClip(final File file) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(file));
        if (null == file) {
            throw new RuntimeExceptionIsNull("file");
        }
        final Clip result = getClip(AudioSystem.getAudioInputStream(file));
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit(result));
        return result;
    }

    /**
	 * Returns an audio {@link Clip} from a {@link InputStream}.
	 *
	 * @param is {@link InputStream} for audio clip
	 * @return Audio {@link Clip}
	 * @throws IOException
	 * @throws LineUnavailableException
	 * @throws UnsupportedAudioFileException
	 * @see InputStream
	 * @see Clip
	 * @since 0.5.0
	 */
    public static Clip getClip(final InputStream is) throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(is));
        if (null == is) {
            throw new RuntimeExceptionIsNull("is");
        }
        final Clip result = getClip(AudioSystem.getAudioInputStream(is));
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit(result));
        return result;
    }

    /**
	 * Returns a {@link Sequence} from a {@link File} (e.g. "mid").
	 *
	 * @param file for sequence
	 * @return Audio {@link Sequence}
	 * @throws IOException
	 * @throws InvalidMidiDataException
	 * @see File
	 * @see Sequence
	 * @since 0.5.0
	 */
    public static Sequence getSequence(final File file) throws InvalidMidiDataException, IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(file));
        if (null == file) {
            throw new RuntimeExceptionIsNull("file");
        }
        final Sequence result = MidiSystem.getSequence(file);
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit(result));
        return result;
    }

    /**
	 * Returns a {@link Sequence} from a {@link InputStream}.
	 *
	 * @param is {@link InputStream} for sequence
	 * @return Audio {@link Sequence}
	 * @throws IOException
	 * @throws InvalidMidiDataException
	 * @see InputStream
	 * @see Sequence
	 * @since 0.5.0
	 */
    public static Sequence getSequence(final InputStream is) throws InvalidMidiDataException, IOException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(is));
        if (null == is) {
            throw new RuntimeExceptionIsNull("is");
        }
        final Sequence result = MidiSystem.getSequence(is);
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit(result));
        return result;
    }

    /**
	 * Returns a {@link Sequencer} to play a {@link Sequence}.
	 *
	 * @param sequence for {@link Sequencer}
	 * @return MIDI {@link Sequencer}
	 * @throws MidiUnavailableException
	 * @throws InvalidMidiDataException
	 * @see Sequence
	 * @see Sequencer
	 * @since 0.5.0
	 */
    public static Sequencer getSequencer(final Sequence sequence) throws MidiUnavailableException, InvalidMidiDataException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(sequence));
        if (null == sequence) {
            throw new RuntimeExceptionIsNull("sequence");
        }
        final Sequencer result = MidiSystem.getSequencer();
        result.open();
        final Synthesizer synth = MidiSystem.getSynthesizer();
        synth.open();
        final Transmitter transmitter = result.getTransmitter();
        final Receiver receiver = synth.getReceiver();
        transmitter.setReceiver(receiver);
        result.setSequence(sequence);
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit(result));
        return result;
    }

    /**
	 * Plays a whole audio {@link Clip} (if no player thread is available).
	 *
	 * @param clip to play
	 * @see Clip
	 * @since 0.5.0
	 */
    public static void play(final Clip clip) {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(clip));
        if (null == clip) {
            throw new RuntimeExceptionIsNull("clip");
        }
        clip.start();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                clip.stop();
            }
        }, clip.getMicrosecondLength() / HelperNumber.NUMBER_1000.longValue());
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    /**
	 * Plays a whole {@link Sequence} (if no player thread is available).
	 *
	 * @param sequence to play
	 * @throws InvalidMidiDataException
	 * @throws MidiUnavailableException
	 * @see Sequence
	 * @since 0.5.0
	 */
    public static void play(final Sequence sequence) throws MidiUnavailableException, InvalidMidiDataException {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart(sequence));
        if (null == sequence) {
            throw new RuntimeExceptionIsNull("sequence");
        }
        final Sequencer sequencer = getSequencer(sequence);
        sequencer.start();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                sequencer.stop();
            }
        }, sequence.getTickLength());
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit());
    }

    /**
	 * Returns a {@link List} of all available clip formats {@link Type} of the current machine (e.g. "aiff", "wave").
	 *
	 * @return {@link List} containing all available clip formats {@link Type} of the current machine
	 * @see Type
	 * @since 0.5.0
	 */
    public static List<Type> getAvailableClipFormats() {
        if (log.isDebugEnabled()) log.debug(HelperLog.methodStart());
        final List<Type> result = Arrays.asList(AudioSystem.getAudioFileTypes());
        if (log.isDebugEnabled()) log.debug(HelperLog.methodExit(result));
        return result;
    }

    private static Clip getClip(final AudioInputStream ais) throws LineUnavailableException, IOException {
        if (log.isTraceEnabled()) log.trace(HelperLog.methodStart(ais));
        try {
            final Info info = new DataLine.Info(Clip.class, ais.getFormat());
            final Clip result = (Clip) AudioSystem.getLine(info);
            result.open(ais);
            if (log.isTraceEnabled()) log.trace(HelperLog.methodExit(result));
            return result;
        } finally {
            ais.close();
        }
    }
}
