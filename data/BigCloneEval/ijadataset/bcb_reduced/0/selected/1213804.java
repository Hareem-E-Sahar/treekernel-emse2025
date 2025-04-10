package org.jfugue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import javax.sound.midi.*;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import org.jfugue.parsers.MidiParser;
import org.jfugue.parsers.MusicStringParser;
import org.jfugue.parsers.Parser;

/**
 * Prepares a pattern to be turned into music by the Renderer.  This class
 * also handles saving the sequence derived from a pattern as a MIDI file.
 *
 *@see MidiRenderer
 *@see Pattern
 *@author David Koelle
 *@version 2.0
 */
public class Player {

    private Sequencer sequencer;

    private Synthesizer synth;

    private MusicStringParser parser;

    private MidiRenderer renderer;

    private float sequenceTiming = Sequence.PPQ;

    private int resolution = 128;

    private volatile boolean paused = false;

    private volatile boolean started = false;

    private volatile boolean finished = false;

    private boolean softClose = false;

    /**
     * Instantiates a new Player object, which is used for playing music.
     */
    public Player() {
        this(true);
    }

    /**
     * Add the softClose parameter to the constructor.  This option changes
     * how sequences are processed.  If true sequences are not closed and then
     * reopened, they are stopped and repositioned to the beginning.  These solves the
     * problem of sound stopping after 17 iterations.
     * @param connected
     * @param softClose
     */
    public Player(boolean connected, boolean softClose) {
        this(connected);
        this.softClose = softClose;
    }

    /**
     * Instantiates a new Player object, which is used for playing music.
     * The <code>connected</code> parameter is passed directly to MidiSystem.getSequencer.
     * Pass false when you do not want to copy a live synthesizer - for example,
     * if your Player is on a server, and you don't want to create new synthesizers every time
     * the constructor is called. 
     */
    public Player(boolean connected) {
        try {
            setSequencer(MidiSystem.getSequencer(connected));
        } catch (MidiUnavailableException e) {
            throw new JFugueException(JFugueException.SEQUENCER_DEVICE_NOT_SUPPORTED_WITH_EXCEPTION + e.getMessage());
        }
        initParser();
    }

    /**
     * Creates a new Player instance using a Sequencer that you have provided.
     * @param sequencer The Sequencer to send the MIDI events
     */
    public Player(Sequencer sequencer) {
        setSequencer(sequencer);
        initParser();
    }

    /**
     * Creates a new Player instance using a Sequencer obtained from the Synthesizer that you have provided.
     * @param synth The Synthesizer you want to use for this Player.
     */
    public Player(Synthesizer synth) throws MidiUnavailableException {
        this(Player.getSequencerConnectedToSynthesizer(synth));
        this.synth = synth;
    }

    private void initParser() {
        this.parser = new MusicStringParser();
        this.renderer = new MidiRenderer(sequenceTiming, resolution);
        this.parser.addParserListener(this.renderer);
    }

    private void initSequencer() {
        int[] controllers = new int[128];
        for (int x = 0; x < 128; x++) controllers[x] = x;
        ControllerEventListener cel = new ControllerEventListener() {

            public void controlChange(javax.sound.midi.ShortMessage event) {
                System.out.println("got controllerEvent " + event.toString());
            }
        };
        getSequencer().addControllerEventListener(cel, controllers);
        getSequencer().addMetaEventListener(new MetaEventListener() {

            public void meta(MetaMessage event) {
                if (event.getType() == 47) {
                    close();
                }
            }
        });
    }

    private void openSequencer() {
        if (getSequencer() == null) {
            throw new JFugueException(JFugueException.SEQUENCER_DEVICE_NOT_SUPPORTED);
        }
        if (!getSequencer().isOpen()) {
            try {
                getSequencer().open();
            } catch (MidiUnavailableException e) {
                throw new JFugueException(JFugueException.SEQUENCER_DEVICE_NOT_SUPPORTED_WITH_EXCEPTION + e.getMessage());
            }
        }
    }

    /** 
     * Returns the instance of the MusicStringParser that the Player uses to parse 
     * music strings.  You can attach additional ParserListeners to this Parser to
     * know exactly when musical events are taking place.  (Similarly, you could
     * create an Anticipator with a delay of 0, but this is much more direct).
     * 
     * You could also remove the Player's default MidiRenderer from this Parser. 
     * 
     * @return instance of this Player's MusicStringParser
     */
    public Parser getParser() {
        return this.parser;
    }

    /**
     * Allows you to set the MusicStringParser for this Player.  You shouldn't use this unless 
     * you definitely want to hijack the Player's MusicStringParser.  You might decide to do this
     * if you're creating a new subclass of MusicStringParser to test out some new functionality.
     * Or, you might have set up a MusicStringParser with an assortment of ParserListeners and
     * you want to use those listeners instead of Player's default listener (although really, you
     * should call getParser() and add your listeners to that parser).
     * 
     * @param parser Your new instance of a MusicStringParser
     */
    public void setParser(MusicStringParser parser) {
        this.parser = parser;
    }

    /**
     * Returns the MidiRenderer that this Player will use to play MIDI events.  
     * @return the MidiRenderer that this Player will use to play MIDI events
     */
    public ParserListener getParserListener() {
        return this.renderer;
    }

    /**
     * Plays a pattern by setting up a Renderer and feeding the pattern to it.
     * @param pattern the pattern to play
     * @see MidiRenderer
     */
    public void play(PatternInterface pattern) {
        Sequence sequence = getSequence(pattern);
        play(sequence);
    }

    /**
     * Appends together and plays all of the patterns passed in.
     * @param patterns the patterns to play
     * @see MidiRenderer
     */
    public void play(Pattern... patterns) {
        PatternInterface allPatterns = new Pattern();
        for (Pattern p : patterns) {
            allPatterns.add(p);
        }
        play(allPatterns);
    }

    /**
     * Replaces pattern identifiers with patterns from the map.
     * @param context A map of pattern identifiers to Pattern objects
     * @param pattern The pattern to play
     */
    public void play(Map<String, Pattern> context, PatternInterface pattern) {
        PatternInterface contextPattern = Pattern.createPattern(context, pattern);
        play(contextPattern);
    }

    /**
     * Appends together and plays all of the patterns passed in, replacing
     * pattern identifiers with actual patterns from the map.
     * @param context A map of pattern identifiers to Pattern objects
     * @param patterns The patterns to play
     */
    public void play(Map<String, Pattern> context, Pattern... patterns) {
        PatternInterface contextPattern = Pattern.createPattern(context, patterns);
        play(contextPattern);
    }

    /**
     * Plays a {@link Rhythm} by setting up a Renderer and feeding the {@link Rhythm} to it.
     * @param rhythm the {@link Rhythm} to play
     * @see MidiRenderer
     */
    public void play(Rhythm rhythm) {
        PatternInterface pattern = rhythm.getPattern();
        Sequence sequence = getSequence(pattern);
        play(sequence);
    }

    /**
     * Plays a MIDI Sequence
     * @param sequence the Sequence to play
     * @throws JFugueException if there is a problem playing the music
     * @see MidiRenderer
     */
    public void play(Sequence sequence) {
        openSequencer();
        try {
            getSequencer().setSequence(sequence);
        } catch (Exception e) {
            throw new JFugueException(JFugueException.ERROR_PLAYING_MUSIC + e.getMessage());
        }
        setStarted(true);
        getSequencer().start();
        if (softClose) {
            long msToSleep = getSequencer().getMicrosecondLength() / 1000;
            try {
                Thread.sleep(msToSleep);
            } catch (InterruptedException e) {
                System.out.println("Exception");
            }
        } else {
            while (isOn()) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    System.out.println("interrupted sleep");
                    try {
                        stop();
                    } catch (Exception e2) {
                        System.out.println("exception stopping sequencer");
                    }
                    throw new JFugueException(JFugueException.ERROR_SLEEP);
                }
            }
        }
        if (softClose == false) getSequencer().close();
        setStarted(false);
        setFinished(true);
    }

    private synchronized boolean isOn() {
        return isPlaying() || isPaused();
    }

    /**
     * Plays a string of music.  Be sure to call player.close() after play() has returned.
     * @param musicString the MusicString (JFugue-formatted string) to play
     */
    public void play(String musicString) {
        if (musicString.indexOf(".mid") > 0) {
            throw new JFugueException(JFugueException.PLAYS_STRING_NOT_FILE_EXC);
        }
        PatternInterface pattern = new Pattern(musicString);
        play(pattern);
    }

    /**
     * Plays a MIDI file, without doing any conversions to MusicStrings.
     * Be sure to call player.close() after play() has returned.
     * @param file the MIDI file to play
     * @throws IOException
     * @throws InvalidMidiDataException
     */
    public void playMidiDirectly(File file) throws IOException, InvalidMidiDataException {
        Sequence sequence = MidiSystem.getSequence(file);
        play(sequence);
    }

    /**
     * Plays a URL that contains a MIDI sequence.  Be sure to call player.close() after play() has returned.
     * @param url the URL to play
     * @throws IOException
     * @throws InvalidMidiDataException
     */
    public void playMidiDirectly(URL url) throws IOException, InvalidMidiDataException {
        Sequence sequence = MidiSystem.getSequence(url);
        play(sequence);
    }

    public void play(Anticipator anticipator, PatternInterface pattern, long offset) {
        Sequence sequence = getSequence(pattern);
        Sequence sequence2 = getSequence(pattern);
        play(anticipator, sequence, sequence2, offset);
    }

    public void play(Anticipator anticipator, Sequence sequence, Sequence sequence2, long offset) {
        anticipator.play(sequence);
        if (offset > 0) {
            try {
                Thread.sleep(offset);
            } catch (InterruptedException e) {
                throw new JFugueException(JFugueException.ERROR_SLEEP);
            }
        }
        play(sequence2);
    }

    /** 
     * Convenience method for playing multiple patterns simultaneously.  Assumes that
     * the patterns do not contain Voice tokens (such as "V0").  Assigns each pattern
     * a voice, from 0 through 15, except 9 (which is the percussion track).  For this
     * reason, if more than 15 patterns are passed in, an IllegalArgumentException is thrown.
     *  
     * @param patterns The patterns to play in harmony
     * @return The combined pattern, including voice tokens
     */
    public PatternInterface playInHarmony(Pattern... patterns) {
        if (patterns.length > 15) {
            throw new IllegalArgumentException("playInHarmony no more than 15 patterns; " + patterns.length + " were passed in");
        }
        PatternInterface retVal = new Pattern();
        int voice = 0;
        for (int i = 0; i < patterns.length; i++) {
            retVal.add("V" + voice);
            retVal.add(patterns[i]);
            voice++;
            if (voice == 9) {
                voice = 10;
            }
        }
        play(retVal);
        return retVal;
    }

    /**
     * Closes MIDI resources - be sure to call this after play() has returned.
     */
    public void close() {
        if (softClose == true) {
            Sequencer seq = getSequencer();
            seq.stop();
            seq.setTickPosition(0);
            seq.start();
            return;
        }
        try {
            getSequencer().getTransmitter().close();
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
        getSequencer().close();
        try {
            if (synth != null) {
                synth.close();
            } else if (MidiSystem.getSynthesizer() != null) {
                MidiSystem.getSynthesizer().close();
            }
        } catch (MidiUnavailableException e) {
            throw new JFugueException(JFugueException.GENERAL_ERROR + e.getMessage());
        }
    }

    private void setStarted(boolean started) {
        this.started = started;
    }

    private void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isStarted() {
        return this.started;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public boolean isPlaying() {
        return getSequencer().isRunning();
    }

    public boolean isPaused() {
        return paused;
    }

    public synchronized void pause() {
        paused = true;
        if (isPlaying()) {
            getSequencer().stop();
        }
    }

    public synchronized void resume() {
        paused = false;
        getSequencer().start();
    }

    public synchronized void stop() {
        paused = false;
        getSequencer().stop();
        getSequencer().setMicrosecondPosition(0);
    }

    public void jumpTo(long microseconds) {
        getSequencer().setMicrosecondPosition(microseconds);
    }

    public long getSequenceLength(Sequence sequence) {
        return sequence.getMicrosecondLength();
    }

    public long getSequencePosition() {
        return getSequencer().getMicrosecondPosition();
    }

    /**
     * Saves the MIDI data from a pattern into a file.
     * @param pattern the pattern to save
     * @param file the File to save the pattern to.  Should include file extension, such as .mid
     */
    public void saveMidi(PatternInterface pattern, File file) throws IOException {
        Sequence sequence = getSequence(pattern);
        int[] writers = MidiSystem.getMidiFileTypes(sequence);
        if (writers.length == 0) return;
        MidiSystem.write(sequence, writers[0], file);
    }

    /**
     * Saves the MIDI data from a MusicString into a file.
     * @param musicString the MusicString to save
     * @param file the File to save the MusicString to.  Should include file extension, such as .mid
     */
    public void saveMidi(String musicString, File file) throws IOException {
        PatternInterface pattern = new Pattern(musicString);
        saveMidi(pattern, file);
    }

    /**
     * Parses a MIDI file and returns a Pattern representing the MIDI file.  
     * This is an excellent example of JFugue's Parser-Renderer architecture:
     *
     * <pre>
     *  MidiParser parser = new MidiParser();
     *  MusicStringRenderer renderer = new MusicStringRenderer();
     *  parser.addParserListener(renderer);
     *  parser.parse(sequence);
     * </pre>
     *
     * @param file The name of the MIDI file
     * @return a Pattern containing the MusicString representing the MIDI music
     * @throws IOException If there is a problem opening the MIDI file
     * @throws InvalidMidiDataException If there is a problem obtaining MIDI resources
     */
    public PatternInterface loadMidi(File file) throws IOException, InvalidMidiDataException {
        MidiFileFormat format = MidiSystem.getMidiFileFormat(file);
        this.sequenceTiming = format.getDivisionType();
        this.resolution = format.getResolution();
        MidiParser parser = new MidiParser();
        MusicStringRenderer renderer = new MusicStringRenderer();
        parser.addParserListener(renderer);
        parser.parse(MidiSystem.getSequence(file));
        PatternInterface pattern = new Pattern(renderer.getPattern().getMusicString());
        return pattern;
    }

    /**
     * Stops all notes from playing on all MIDI channels.
     * Uses the synthesizer provided by MidiSystem.getSynthesizer().
     */
    public static void allNotesOff() {
        try {
            allNotesOff(MidiSystem.getSynthesizer());
        } catch (MidiUnavailableException e) {
            throw new JFugueException(JFugueException.GENERAL_ERROR);
        }
    }

    /**
     * Stops all notes from playing on all MIDI channels.
     * Uses the synthesizer provided to the method. 
     */
    public static void allNotesOff(Synthesizer synth) {
        try {
            if (!synth.isOpen()) {
                synth.open();
            }
            MidiChannel[] channels = synth.getChannels();
            for (int i = 0; i < channels.length; i++) {
                channels[i].allNotesOff();
            }
        } catch (MidiUnavailableException e) {
            throw new JFugueException(JFugueException.GENERAL_ERROR);
        }
    }

    /**
     * Returns the sequencer containing the MIDI data from a pattern that has been parsed.
     * @return the Sequencer from the pattern that was recently parsed
     */
    public Sequencer getSequencer() {
        return this.sequencer;
    }

    private void setSequencer(Sequencer sequencer) {
        this.sequencer = sequencer;
        initSequencer();
    }

    /**
     * Returns the sequence containing the MIDI data from the given pattern.
     * @return the Sequence from the given pattern
     */
    public Sequence getSequence(PatternInterface pattern) {
        this.renderer.reset();
        this.parser.parse(pattern);
        Sequence sequence = this.renderer.getSequence();
        return sequence;
    }

    /**
     * Returns an instance of a Sequencer that uses the provided Synthesizer as its receiver.
     * This is useful when you have made changes to a specific Synthesizer--for example, you've
     * loaded in new patches--that you want the Sequencer to use.  You can then pass the Sequencer
     * to the Player constructor.
     *
     * @param synth The Synthesizer to use as the receiver for the returned Sequencer
     * @return a Sequencer with the provided Synthesizer as its receiver
     * @throws MidiUnavailableException
     */
    public static Sequencer getSequencerConnectedToSynthesizer(Synthesizer synth) throws MidiUnavailableException {
        Sequencer sequencer = MidiSystem.getSequencer(false);
        sequencer.open();
        if (!synth.isOpen()) {
            synth.open();
        }
        sequencer.getTransmitter().setReceiver(synth.getReceiver());
        return sequencer;
    }
}
