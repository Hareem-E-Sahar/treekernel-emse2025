package uk.org.toot.synth;

import uk.org.toot.control.CompoundControl;

/**
 * This class represents SynthControls that are comprised of global and
 * per channel CompoundControls.
 * @author st
 */
public class ChannelledSynthControls extends SynthControls {

    private CompoundControl globalControls;

    private SynthChannelControls[] channelControls = new SynthChannelControls[16];

    public ChannelledSynthControls(int id, String name) {
        super(id, name);
    }

    public CompoundControl getGlobalControls() {
        return globalControls;
    }

    public SynthChannelControls getChannelControls(int chan) {
        return channelControls[chan];
    }

    protected void setGlobalControls(CompoundControl controls) {
        globalControls = controls;
        add(controls);
    }

    protected void setChannelControls(int chan, SynthChannelControls controls) {
        channelControls[chan] = controls;
        add(controls);
    }
}
