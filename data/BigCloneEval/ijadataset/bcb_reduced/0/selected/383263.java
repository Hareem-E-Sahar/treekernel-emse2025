package uk.org.toot.synth;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.core.ChannelFormat;
import uk.org.toot.audio.system.AudioOutput;

/**
 * A SynthChannel which is monophonic.
 * Supports glide, i.e. fingered portamento
 * @author st
 */
public abstract class MonophonicSynthChannel extends SynthChannel implements AudioOutput {

    private AudioBuffer.MetaInfo info;

    private String location;

    private String name;

    private int noteCount = 0;

    private float frequency;

    private float targetFrequency;

    private boolean gliding = false;

    private float glideFactor;

    private int sampleCount = 256;

    protected float amplitude;

    protected float semitones;

    private float targetSemitones;

    private float semitoneIncrement;

    public MonophonicSynthChannel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setLocation(String location) {
        this.location = location;
        info = new AudioBuffer.MetaInfo(name, location);
    }

    public String getLocation() {
        return location;
    }

    @Override
    public void noteOn(int pitch, int velocity) {
        noteCount += 1;
        targetFrequency = midiFreq(pitch);
        targetSemitones = pitch;
        amplitude = velocity / 128f;
        if (noteCount == 1 || !isGlideEnabled()) {
            frequency = targetFrequency;
            semitones = targetSemitones;
            trigger(amplitude);
        } else {
            int nblocks = getGlideMilliseconds() * sampleRate / 1000 / sampleCount;
            glideFactor = (float) Math.pow(targetFrequency / frequency, 1.0 / nblocks);
            semitoneIncrement = (targetSemitones - semitones) / nblocks;
            gliding = true;
        }
    }

    @Override
    public void noteOff(int pitch) {
        noteCount -= 1;
        if (noteCount == 0) {
            release();
        }
    }

    public void open() {
    }

    public int processAudio(AudioBuffer buffer) {
        buffer.setMetaInfo(info);
        buffer.setChannelFormat(ChannelFormat.MONO);
        if (isComplete()) {
            buffer.makeSilence();
            return AudioProcess.AUDIO_SILENCE;
        }
        sampleCount = buffer.getSampleCount();
        int sr = (int) buffer.getSampleRate();
        if (sr != sampleRate) {
            setSampleRate(sr);
            sampleRate = sr;
        }
        if (gliding) glide();
        update(frequency);
        float[] samples = buffer.getChannel(0);
        int nsamples = buffer.getSampleCount();
        for (int i = 0; i < nsamples; i++) {
            samples[i] += getSample();
        }
        return AudioProcess.AUDIO_OK;
    }

    public void close() {
    }

    private void glide() {
        semitones += semitoneIncrement;
        frequency *= glideFactor;
        if (glideFactor < 1f) {
            if (frequency <= targetFrequency) {
                frequency = targetFrequency;
                semitones = targetSemitones;
                gliding = false;
            }
        } else {
            if (frequency >= targetFrequency) {
                frequency = targetFrequency;
                semitones = targetSemitones;
                gliding = false;
            }
        }
    }

    @Override
    public void allNotesOff() {
        release();
        noteCount = 0;
    }

    @Override
    public void allSoundOff() {
    }

    protected abstract boolean isComplete();

    protected abstract void trigger(float amp);

    protected abstract void release();

    protected abstract void update(float frequency);

    protected abstract float getSample();

    protected abstract boolean isGlideEnabled();

    protected abstract int getGlideMilliseconds();
}
