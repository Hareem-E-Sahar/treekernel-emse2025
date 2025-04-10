public class Test {    public MultiMidiSynth(final MultiSynthControls controls) {
        super(controls.getName());
        controls.addObserver(new Observer() {

            public void update(Observable obs, Object obj) {
                if (obj instanceof Integer) {
                    int chan = ((Integer) obj).intValue();
                    if (chan < 0 || chan > 15) return;
                    SynthChannelControls channelControls = controls.getChannelControls(chan);
                    if (channelControls != null) {
                        SynthChannel synthChannel = SynthChannelServices.createSynthChannel(channelControls);
                        if (synthChannel == null) {
                            System.err.println("No SynthChannel for SynthControls " + channelControls.getName());
                        } else {
                            synthChannel.setLocation(MultiMidiSynth.this.getLocation() + " Channel " + (1 + chan));
                            synthChannel.addObserver(channelControls);
                        }
                        setChannel(chan, synthChannel);
                    } else {
                        setChannel(chan, null);
                    }
                }
            }
        });
    }
}