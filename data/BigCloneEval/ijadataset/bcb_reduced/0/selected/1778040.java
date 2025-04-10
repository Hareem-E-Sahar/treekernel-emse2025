package org.jfugue.test;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;

/**
 * This class is a reference point for my sanity. It is a simple MIDI player
 * that writes data directly to the channels. The StreamingMidiEventManager
 * should work in the same way.
 */
public class SimpleMidiTest {

    public static void main(String[] args) {
        int[] notes = new int[] { 60, 62, 64, 65, 67, 69, 71, 72, 72, 71, 69, 67, 65, 64, 62, 60 };
        try {
            Sequencer sequencer = MidiSystem.getSequencer();
            Synthesizer synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            if (synthesizer.getDefaultSoundbank() == null) {
                System.out.println("snd null");
                sequencer.getTransmitter().setReceiver(MidiSystem.getReceiver());
            } else {
                sequencer.getTransmitter().setReceiver(synthesizer.getReceiver());
            }
            MidiChannel channel = synthesizer.getChannels()[0];
            for (int note : notes) {
                channel.noteOn(note, 120);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    break;
                } finally {
                    channel.noteOff(note);
                }
            }
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }
}
