package Core.PSIP;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import API.BitOutputStream;
import API.MyUTIL;
import API.Section;
import API.SectionFactory;
import API.TableID;
import API.Descriptor.Descriptor;
import API.PSIP.CVCT;
import API.PSIP.CVCTChannel;
import Core.SITableAbstractImpl;

/**
 * @author SungHun Park (dr.superchamp@gmail.com)
 *
 */
public class CVCTDefaultImpl extends SITableAbstractImpl implements CVCT {

    protected int version_number = 0;

    protected int transport_stream_id = 0;

    protected List<CVCTChannel> channels = new Vector<CVCTChannel>();

    protected List<Descriptor> descs = new Vector<Descriptor>();

    protected long interval_millis = 400;

    public CVCTDefaultImpl() {
    }

    @Override
    public boolean addChannel(CVCTChannel channel) {
        channels.add(channel);
        return true;
    }

    @Override
    public boolean addChannelAt(int index, CVCTChannel channel) {
        if (index < 0 || index > channels.size()) return false;
        channels.add(index, channel);
        return true;
    }

    @Override
    public CVCTChannel getChannelAt(int index) {
        if (index < 0 || index >= channels.size()) return null;
        return channels.get(index);
    }

    @Override
    public Iterator<CVCTChannel> getChannels() {
        return channels.iterator();
    }

    @Override
    public int getNumChannels() {
        return channels.size();
    }

    @Override
    public int getTransportStreamId() {
        return transport_stream_id;
    }

    @Override
    public int getVersionNumber() {
        return version_number;
    }

    @Override
    public boolean setChannelAt(int index, CVCTChannel channel) {
        if (index < 0 || index >= channels.size()) return false;
        channels.set(index, channel);
        return true;
    }

    @Override
    public void setTransportStreamId(int tsid) {
        transport_stream_id = tsid;
    }

    @Override
    public void setVersionNumber(int version) {
        version_number = version;
    }

    @Override
    public TableID getTableID() {
        return TableID.CABLE_VIRTUAL_CHANNEL_TABLE;
    }

    @Override
    public int getTablePID() {
        return 0x1FFB;
    }

    @Override
    public int getTableVersion() {
        return getVersionNumber();
    }

    @Override
    public long getIntervalMillis() {
        return interval_millis;
    }

    @Override
    public void setIntervalMillis(long millisec) {
        interval_millis = millisec;
    }

    @Override
    public String toXMLString(int base_space) {
        String str = new String();
        str += (MyUTIL.whiteSpaceStr(base_space) + "<CVCT>\n");
        str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<table_id>" + TableID.CABLE_VIRTUAL_CHANNEL_TABLE.getValue() + "</table_id>\n");
        str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<transport_stream_id>" + transport_stream_id + "</transport_stream_id>\n");
        str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<version_number>" + version_number + "</version_number>\n");
        if (getNumChannels() > 0) {
            str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<CVCTChannelLoop>\n");
            Iterator<CVCTChannel> it = getChannels();
            while (it.hasNext()) str += it.next().toXMLString(base_space + 4);
            str += (MyUTIL.whiteSpaceStr(base_space + 2) + "</CVCTChannelLoop>\n");
        }
        if (getDescriptorSize() > 0) {
            Iterator<Descriptor> it = getDescriptors();
            str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<DescriptorLoop>\n");
            while (it.hasNext()) str += it.next().toXMLString(base_space + 4);
            str += (MyUTIL.whiteSpaceStr(base_space + 2) + "</DescriptorLoop>\n");
        }
        str += (MyUTIL.whiteSpaceStr(base_space) + "</CVCT>\n");
        return str;
    }

    @Override
    public int getTotalSectionNumber() {
        int max_stream_size_in_section = 1021 - (13 + getDescriptorsLength());
        int total_section = 0;
        for (int stream_index = 0; stream_index < getNumChannels(); ) {
            int stream_size = 0;
            while (stream_index < getNumChannels() && (stream_size + getChannelAt(stream_index).getSizeInBytes()) < max_stream_size_in_section) stream_size += getChannelAt(stream_index++).getSizeInBytes();
            total_section++;
        }
        return total_section;
    }

    @Override
    public boolean isMultiSection() {
        if (getTotalSectionNumber() > 1) return true;
        return false;
    }

    @Override
    public Section[] toSection() {
        Section[] sections;
        int max_stream_size_in_section = 1021 - (13 + getDescriptorsLength());
        int total_section = getTotalSectionNumber(), write_from_idx = 0;
        sections = new Section[total_section];
        for (int sn = 0; sn < sections.length; sn++) {
            sections[sn] = SectionFactory.createCVCTSection(this, transport_stream_id, sn, total_section - 1);
            int write_to_idx = write_from_idx;
            int stream_size = 0;
            while (write_to_idx < getNumChannels() && (stream_size + getChannelAt(write_to_idx).getSizeInBytes()) < max_stream_size_in_section) stream_size += getChannelAt(write_to_idx++).getSizeInBytes();
            int total_bits = (2 + stream_size + 2 + getDescriptorsLength()) * Byte.SIZE;
            BitOutputStream os = new BitOutputStream(total_bits);
            os.writeFromLSB(0, 8);
            os.writeFromLSB(getNumChannels(), 8);
            for (int n = write_from_idx; n < write_to_idx; n++) os.write(getChannelAt(n).toByteArray());
            os.writeFromLSB(0xFF, 6);
            os.writeFromLSB(getDescriptorsLength(), 10);
            if (getDescriptorSize() > 0) {
                Iterator<Descriptor> it = getDescriptors();
                while (it.hasNext()) os.write(it.next().toByteArray());
            }
            sections[sn].setPrivateData(os.toByteArray());
            write_from_idx = write_to_idx;
        }
        return sections;
    }

    @Override
    public boolean addDescriptor(Descriptor desc) {
        return false;
    }

    @Override
    public boolean addDescriptorAt(int index, Descriptor desc) {
        return false;
    }

    @Override
    public Descriptor getDescriptorAt(int index) {
        return null;
    }

    @Override
    public int getDescriptorSize() {
        return 0;
    }

    @Override
    public Iterator<Descriptor> getDescriptors() {
        return null;
    }

    @Override
    public int getDescriptorsLength() {
        return 0;
    }

    @Override
    public void removeAllDescriptors() {
    }

    @Override
    public boolean removeDescriptor(Descriptor desc) {
        return false;
    }

    @Override
    public boolean removeDescriptorAt(int index) {
        return false;
    }

    @Override
    public boolean setDescriptorAt(int index, Descriptor desc) {
        return false;
    }
}
