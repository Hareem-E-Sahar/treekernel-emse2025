package uk.org.toot.synth.synths.vsti;

import java.util.Arrays;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.core.ChannelFormat;
import uk.org.toot.audio.system.AudioOutput;
import uk.org.toot.misc.Tempo;
import uk.org.toot.misc.TimeSignature;
import com.synthbot.audioplugin.vst.vst2.VstPinProperties;

/**
 * This class has a single audio output, for mono and stereo vstis.
 * @author st
 *
 */
public class SimpleVstiSynth extends VstiSynth implements AudioOutput {

    private int nOutChan;

    private int nInChan;

    private float[][] inSamples;

    private float[][] outSamples;

    private int nsamples;

    private int sampleRate;

    private AudioBuffer.MetaInfo info;

    private ChannelFormat format;

    private boolean mustClear = false;

    private VstiSynthControls controls;

    private Tempo.Listener tempoListener;

    private TimeSignature.Listener timeSignatureListener;

    private String location;

    public SimpleVstiSynth(final VstiSynthControls controls) {
        super(controls);
        this.controls = controls;
        addAudioOutput(this);
        nsamples = vsti.getBlockSize();
        sampleRate = (int) vsti.getSampleRate();
        nOutChan = vsti.numOutputs();
        VstPinProperties props;
        for (int i = 0; i < nOutChan; i++) {
            props = vsti.getOutputProperties(i);
            if (!props.isValid()) continue;
        }
        outSamples = new float[nOutChan][nsamples];
        if (nOutChan >= 2) {
            nOutChan = 2;
            format = ChannelFormat.STEREO;
        } else if (nOutChan == 1) {
            format = ChannelFormat.MONO;
        }
        nInChan = vsti.numInputs();
        inSamples = new float[nInChan][nsamples];
        mustClear = vsti.getVendorName().indexOf("Steinberg") >= 0;
        tempoListener = new Tempo.Listener() {

            public void tempoChanged(float newTempo) {
                vsti.setTempo(newTempo);
            }
        };
        timeSignatureListener = new TimeSignature.Listener() {

            public void timeSignatureChanged(int numerator, int denominator) {
                vsti.setTimeSignature(numerator, denominator);
            }
        };
    }

    public void setLocation(String location) {
        this.location = location;
        info = new AudioBuffer.MetaInfo(getName(), location);
    }

    public String getLocation() {
        return location;
    }

    public void open() throws Exception {
        System.out.print("Opening audio: " + controls.getName() + " ... ");
        vsti.turnOn();
        Tempo.addTempoListener(tempoListener);
        TimeSignature.addTimeSignatureListener(timeSignatureListener);
        System.out.println("opened");
    }

    public int processAudio(AudioBuffer buffer) {
        buffer.setMetaInfo(info);
        buffer.setChannelFormat(format);
        int ns = buffer.getSampleCount();
        if (ns != nsamples) {
            vsti.turnOff();
            nsamples = ns;
            outSamples = new float[vsti.numOutputs()][nsamples];
            inSamples = new float[nInChan][nsamples];
            vsti.setBlockSize(nsamples);
            vsti.turnOn();
        }
        int sr = (int) buffer.getSampleRate();
        if (sr != sampleRate) {
            sampleRate = sr;
            vsti.setSampleRate(sampleRate);
        }
        if (mustClear) {
            for (int i = 0; i < nOutChan; i++) {
                Arrays.fill(outSamples[i], 0f);
            }
        }
        vsti.processReplacing(inSamples, outSamples, nsamples);
        for (int i = 0; i < nOutChan; i++) {
            float[] from = outSamples[i];
            float[] to = buffer.getChannel(i);
            System.arraycopy(from, 0, to, 0, nsamples);
        }
        return AudioProcess.AUDIO_OK;
    }

    public void close() throws Exception {
        System.out.print("Closing audio: " + controls.getName() + " ... ");
        vsti.turnOffAndUnloadPlugin();
        Tempo.removeTempoListener(tempoListener);
        TimeSignature.removeTimeSignatureListener(timeSignatureListener);
        System.out.println("closed");
    }
}
