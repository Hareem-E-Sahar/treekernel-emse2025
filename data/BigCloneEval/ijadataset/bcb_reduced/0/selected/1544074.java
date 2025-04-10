package uk.org.toot.audio.mixer;

import uk.org.toot.audio.core.SimpleAudioProcess;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.ChannelFormat;

/**
 * A MixProcess mixes a portion of the audio signal that it processes to
 * a particular named bus. Levels are exponentially smoothed.
 */
public class MixProcess extends SimpleAudioProcess {

    /**
     * @link aggregation
     * @supplierCardinality 1 
     */
    protected MixVariables vars;

    /**
     * @link aggregation
     * @supplierCardinality 1 
     * @label routed
     */
    protected AudioMixerStrip routedStrip;

    private float gain = 1;

    private float[] channelGains;

    private float[] smoothedChannelGains;

    public MixProcess(AudioMixerStrip strip, MixVariables vars) {
        if (strip == null) {
            throw new IllegalArgumentException("null strip to route to");
        }
        routedStrip = strip;
        this.vars = vars;
        ChannelFormat format = vars.getChannelFormat();
        channelGains = new float[format.getCount()];
        smoothedChannelGains = new float[format.getCount()];
    }

    private float factor = 0.05f;

    protected AudioMixerStrip getRoutedStrip() {
        return routedStrip;
    }

    public int processAudio(AudioBuffer buffer) {
        if (!vars.isEnabled() && vars.isMaster()) {
            buffer.makeSilence();
        } else if (vars.isEnabled()) {
            gain = vars.getGain();
            if (gain > 0f || vars.isMaster()) {
                vars.getChannelGains(channelGains);
                for (int c = 0; c < channelGains.length; c++) {
                    smoothedChannelGains[c] += factor * (channelGains[c] - smoothedChannelGains[c]);
                }
                getRoutedStrip().mix(buffer, smoothedChannelGains);
            }
        }
        return AUDIO_OK;
    }
}
