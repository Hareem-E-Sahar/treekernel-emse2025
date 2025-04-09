package freefret;

import java.util.Random;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;

public abstract class FretboardMidiModule extends FretboardModule {

    protected Sequencer sequencer;

    protected Sequence mainSeq = null;

    protected Synthesizer synth;

    protected Track mainTrack;

    protected boolean gotMidi = true;

    protected int noteVolume = 95;

    protected ShortMessage noteOne = new ShortMessage();

    protected ShortMessage noteOneOff = new ShortMessage();

    protected ShortMessage noteTwo = new ShortMessage();

    protected ShortMessage noteTwoOff = new ShortMessage();

    int rootMidi;

    int intervalMidi;

    protected int currentInterval = 4;

    public FretboardMidiModule() {
        super();
        try {
            sequencer = MidiSystem.getSequencer();
            synth = MidiSystem.getSynthesizer();
            sequencer.open();
        } catch (MidiUnavailableException e) {
            sequencer = null;
            gotMidi = false;
            System.out.println("midi unavailable!");
        }
        if (gotMidi != false) {
            try {
                mainSeq = new Sequence(Sequence.PPQ, 10);
                mainTrack = mainSeq.createTrack();
                sequencer.setSequence(mainSeq);
            } catch (InvalidMidiDataException e) {
                gotMidi = false;
                mainSeq = null;
                mainTrack = null;
                System.out.println("midi invalid data!");
            }
        }
    }

    /**
	 * Plays the two given notes, first one, then the other, then together
	 * @param note1 - first midi note to be played
	 * @param note2 - second midi note to be played
	 */
    protected void playNotes(int note1, int note2) {
        sequencer.stop();
        sequencer.setMicrosecondPosition(0);
        emptyTrack();
        try {
            noteOne.setMessage(ShortMessage.NOTE_ON, 0, note1, noteVolume);
            noteOneOff.setMessage(ShortMessage.NOTE_OFF, 0, note1, noteVolume);
            noteTwo.setMessage(ShortMessage.NOTE_ON, 0, note2, noteVolume);
            noteTwoOff.setMessage(ShortMessage.NOTE_OFF, 0, note2, noteVolume);
        } catch (InvalidMidiDataException e) {
        }
        if (gotMidi) {
            mainTrack.add(new MidiEvent(noteOne, 0));
            mainTrack.add(new MidiEvent(noteTwo, 20));
            mainTrack.add(new MidiEvent(noteOneOff, 25));
            mainTrack.add(new MidiEvent(noteTwoOff, 45));
            mainTrack.add(new MidiEvent(noteOne, 50));
            mainTrack.add(new MidiEvent(noteTwo, 50));
            mainTrack.add(new MidiEvent(noteOneOff, 75));
            mainTrack.add(new MidiEvent(noteTwoOff, 75));
            sequencer.start();
        }
    }

    /**
	 * empties the events from the track
	 *
	 */
    protected void emptyTrack() {
        try {
            int i = 0;
            for (i = 0; i < mainTrack.size(); i++) {
                mainTrack.remove(mainTrack.get(i));
            }
        } catch (NullPointerException e) {
            System.out.println("Unable to empty track, null pointer");
        }
    }

    /**
	 * creates a random note and adds a relevent interval, then playing them both
	 *
	 */
    protected void makeNewInterval() {
        Random randNumber = new Random();
        int string = randNumber.nextInt(6);
        int fret = randNumber.nextInt(13);
        rootMidi = FretboardObject.convertPostionToMidiNote(fret, string);
        intervalMidi = 0;
        while ((rootMidi + currentInterval) > 76) {
            string = randNumber.nextInt(6);
            fret = randNumber.nextInt(13);
            rootMidi = FretboardObject.convertPostionToMidiNote(fret, string);
        }
        switch(string) {
            case 0:
                fretboardG.positionDot(FretboardObject.convertIntToFret(fret), FretboardObject.convertIntToString(string));
                fretboardG.positionSecondDot(FretboardObject.convertIntToFret(fret + currentInterval), FretboardObject.convertIntToString(string));
                intervalMidi = rootMidi + currentInterval;
                break;
            default:
                int string2 = randNumber.nextInt(string + 1);
                String intervalNote = FretboardObject.convertPostionToString(fret + currentInterval, string);
                int fret2 = 0;
                while (!(FretboardObject.convertPostionToString(fret2, string2).equals(intervalNote)) || (FretboardObject.convertPostionToMidiNote(fret2, string2) < rootMidi)) {
                    fret2++;
                    if (fret2 > 12) {
                        string2--;
                        fret2 = 0;
                    }
                }
                intervalMidi = FretboardObject.convertPostionToMidiNote(fret2, string2);
                fretboardG.positionDot(FretboardObject.convertIntToFret(fret), FretboardObject.convertIntToString(string));
                fretboardG.positionSecondDot(FretboardObject.convertIntToFret(fret2), FretboardObject.convertIntToString(string2));
                break;
        }
        playNotes(rootMidi, intervalMidi);
    }
}
