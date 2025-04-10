package uk.org.toot.synth;

import java.util.Observable;
import javax.sound.midi.MidiChannel;
import static uk.org.toot.midi.misc.Controller.*;

/**
 * A SynthChannel is a MidiChannel.
 * 
 * @author st
 *
 */
public abstract class SynthChannel extends Observable implements MidiChannel {

    protected int sampleRate = 44100;

    protected float inverseNyquist = 2f / sampleRate;

    private int rawBend = 8192;

    private int bendRange = 2;

    private float bendFactor = 1;

    private static final double ONE_SEMITONE = 1.0594630943592952645618252949463;

    private int pressure = 0;

    private byte[] polyPressure = new byte[128];

    private byte[] controller = new byte[128];

    private static float[] freqTable = new float[140];

    static {
        createFreqTable();
    }

    public SynthChannel() {
    }

    public abstract void setLocation(String location);

    private static void createFreqTable() {
        for (int i = 0; i < freqTable.length; i++) {
            freqTable[i] = midiFreqImpl(i);
        }
    }

    public static float midiFreq(float pitch) {
        if (pitch < 0) return freqTable[0];
        if (pitch >= freqTable.length - 1) return freqTable[freqTable.length - 2];
        int idx = (int) pitch;
        float frac = pitch - idx;
        return freqTable[idx] * (1 - frac) + freqTable[idx + 1] * frac;
    }

    private static float midiFreqImpl(int pitch) {
        return (float) (440.0 * Math.pow(2.0, ((double) pitch - 69.0) / 12.0));
    }

    protected void setSampleRate(int rate) {
        sampleRate = rate;
        inverseNyquist = 2f / sampleRate;
    }

    public abstract void noteOn(int pitch, int velocity);

    public abstract void noteOff(int pitch);

    public void noteOff(int pitch, int velocity) {
        noteOff(pitch);
    }

    public abstract void allNotesOff();

    public abstract void allSoundOff();

    public void controlChange(int arg0, int arg1) {
        controller[arg0] = (byte) arg1;
        if (arg0 < 0x20) controller[arg0 + 0x20] = 0;
        setChanged();
        notifyObservers(new ControlChange(arg0, arg1));
    }

    public int getController(int arg0) {
        return controller[arg0];
    }

    public void resetAllControllers() {
        for (int i = 0; i < controller.length; i++) {
            controller[i] = 0;
        }
        controller[EXPRESSION] = 127;
        controller[EXPRESSION + 0x20] = 127;
        controller[VOLUME] = 100;
        controller[PAN] = 64;
        controller[PAN + 0x20] = 64;
        pressure = 0;
        setPitchBend(8192);
    }

    public int getProgram() {
        return 0;
    }

    public void programChange(int arg0) {
    }

    public void programChange(int arg0, int arg1) {
    }

    public int getChannelPressure() {
        return pressure;
    }

    public void setChannelPressure(int arg0) {
        pressure = arg0;
    }

    public int getPolyPressure(int arg0) {
        return polyPressure[arg0];
    }

    public void setPolyPressure(int arg0, int arg1) {
        polyPressure[arg0] = (byte) arg1;
    }

    public boolean getSolo() {
        return false;
    }

    public boolean getMute() {
        return false;
    }

    public boolean getMono() {
        return false;
    }

    public boolean getOmni() {
        return false;
    }

    public void setSolo(boolean arg0) {
    }

    public void setMute(boolean arg0) {
    }

    public void setMono(boolean mono) {
    }

    public void setOmni(boolean arg0) {
    }

    public boolean localControl(boolean arg0) {
        return false;
    }

    public void setPitchBend(int bend) {
        rawBend = bend;
        bend -= 8192;
        float b = (float) bendRange * bend / 8192;
        bendFactor = (float) Math.pow(ONE_SEMITONE, b);
    }

    public int getPitchBend() {
        return rawBend;
    }

    public float getBendFactor() {
        return bendFactor;
    }
}
