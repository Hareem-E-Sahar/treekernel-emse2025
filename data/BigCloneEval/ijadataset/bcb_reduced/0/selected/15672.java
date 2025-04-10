package net.sf.smartcrib.dmx;

import java.util.Comparator;

/**
 * A DMX devices comparator based on their addresses.
 */
public class DMXDeviceAddressComparator implements Comparator<DMXDevice> {

    public int compare(DMXDevice o1, DMXDevice o2) {
        return o1.getChannel() - o2.getChannel();
    }
}
