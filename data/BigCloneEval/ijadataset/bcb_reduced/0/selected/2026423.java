package uk.org.toot.audio.mixer;

import java.util.Observer;
import java.util.Observable;
import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.audio.core.ChannelFormat;
import uk.org.toot.audio.meter.MeterControls;
import uk.org.toot.control.BooleanControl;
import static uk.org.toot.misc.Localisation.*;

/**
 * BusControls are the composite Controls for a bus.
 */
public class BusControls extends AudioControls implements Observer {

    /**
     * @supplierCardinality 1
     * @link aggregationByValue 
     */
    private SoloIndicator soloIndicator;

    private MeterControls meterControls;

    private int soloCount = 0;

    private ChannelFormat channelFormat;

    public BusControls(int id, String name, ChannelFormat format) {
        super(id, name);
        channelFormat = format;
        soloIndicator = new SoloIndicator();
        meterControls = new MeterControls(channelFormat, "Meter");
        add(meterControls);
    }

    public SoloIndicator getSoloIndicator() {
        return soloIndicator;
    }

    public MeterControls getMeterControls() {
        return meterControls;
    }

    public boolean hasSolo() {
        return soloCount > 0;
    }

    public ChannelFormat getChannelFormat() {
        return channelFormat;
    }

    public void update(Observable obs, Object arg) {
        if (obs instanceof BooleanControl) {
            BooleanControl c = (BooleanControl) obs;
            if (c.getName().equals(getString("Solo"))) {
                soloCount += c.getValue() ? 1 : -1;
                soloIndicator.setValue(hasSolo());
            }
        }
    }

    public boolean canBypass() {
        return false;
    }

    public String toString() {
        return getName() + " Bus";
    }

    public static class SoloIndicator extends BooleanControl {

        public SoloIndicator() {
            super(-MixControlIds.SOLO, getString("Solo"), false);
            indicator = true;
            setAnnotation("S");
            setStateColor(true, java.awt.Color.green);
        }
    }
}
