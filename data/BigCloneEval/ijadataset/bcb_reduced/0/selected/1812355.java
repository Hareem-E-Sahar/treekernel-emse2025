package Core.PSIP;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import API.BitOutputStream;
import API.MyUTIL;
import API.ServiceType;
import API.Descriptor.Descriptor;
import API.PSIP.CVCTChannel;

/**
 * @author SungHun Park (dr.superchamp@gmail.com)
 *
 */
public class CVCTChannelDefaultImpl implements CVCTChannel {

    protected char[] short_name = new char[7];

    protected int major_channel_number = 0;

    protected int minor_channel_number = 0;

    protected int modulation_mode = 0;

    protected long carrier_frequency = 0;

    protected int channel_TSID = 0;

    protected int program_number = 0;

    protected int ETM_location = 0;

    protected int access_controlled = 0;

    protected int hidden = 0;

    protected int path_select = 0;

    protected int out_of_band = 0;

    protected int hide_guide = 0;

    protected ServiceType service_type = ServiceType.RESERVED;

    protected int source_id = 0;

    protected List<Descriptor> descs = new Vector<Descriptor>();

    @Override
    public int getAccessControlled() {
        return access_controlled;
    }

    @Override
    public long getCarrierFrequency() {
        return carrier_frequency;
    }

    @Override
    public int getChannelTSID() {
        return channel_TSID;
    }

    @Override
    public int getETMLocation() {
        return ETM_location;
    }

    @Override
    public int getHidden() {
        return hidden;
    }

    @Override
    public int getHideGuide() {
        return hide_guide;
    }

    @Override
    public int getMajorChannelNumber() {
        return major_channel_number;
    }

    @Override
    public int getMinorChannelNumber() {
        return minor_channel_number;
    }

    @Override
    public int getModulationMode() {
        return modulation_mode;
    }

    @Override
    public int getOutOfBand() {
        return out_of_band;
    }

    @Override
    public int getPathSelect() {
        return path_select;
    }

    @Override
    public int getProgramNumber() {
        return program_number;
    }

    @Override
    public ServiceType getServiceType() {
        return service_type;
    }

    @Override
    public char[] getShortName() {
        return short_name;
    }

    @Override
    public int getSourceId() {
        return source_id;
    }

    @Override
    public void setAccessControlled(int ac) {
        access_controlled = ac;
    }

    @Override
    public void setCarrierFrequency(long freq) {
        carrier_frequency = freq;
    }

    @Override
    public void setChannelTSID(int tsid) {
        channel_TSID = tsid;
    }

    @Override
    public void setETMLocation(int etm_loc) {
        ETM_location = etm_loc;
    }

    @Override
    public void setHidden(int hidden) {
        this.hidden = hidden;
    }

    @Override
    public void setHideGuide(int hideguide) {
        hide_guide = hideguide;
    }

    @Override
    public void setMajorChannelNumber(int number) {
        major_channel_number = number;
    }

    @Override
    public void setMinorChannelNumber(int number) {
        minor_channel_number = number;
    }

    @Override
    public void setModulationMode(int mode) {
        modulation_mode = mode;
    }

    @Override
    public void setOutOfBand(int oob) {
        out_of_band = oob;
    }

    @Override
    public void setPathSelect(int path_select) {
        this.path_select = path_select;
    }

    @Override
    public void setProgramNumber(int program_num) {
        program_number = program_num;
    }

    @Override
    public void setServiceType(ServiceType type) {
        service_type = type;
    }

    @Override
    public void setShortName(char[] name) {
        short_name = name;
    }

    @Override
    public void setSourceId(int source_id) {
        this.source_id = source_id;
    }

    @Override
    public int getSizeInBytes() {
        return 32 + getDescriptorsLength();
    }

    @Override
    public byte[] toByteArray() {
        BitOutputStream os = new BitOutputStream(getSizeInBytes() * Byte.SIZE);
        int n;
        for (n = 0; n < short_name.length && n < 7; n++) os.write(short_name[n]);
        for (; n < 7; n++) os.write((char) 0);
        os.writeFromLSB(0xFF, 4);
        os.writeFromLSB(major_channel_number, 10);
        os.writeFromLSB(minor_channel_number, 10);
        os.writeFromLSB(modulation_mode, 8);
        os.writeFromLSB((int) carrier_frequency, 32);
        os.writeFromLSB(channel_TSID, 16);
        os.writeFromLSB(program_number, 16);
        os.writeFromLSB(ETM_location, 2);
        os.writeFromLSB(access_controlled, 1);
        os.writeFromLSB(hidden, 1);
        os.writeFromLSB(path_select, 1);
        os.writeFromLSB(out_of_band, 1);
        os.writeFromLSB(hide_guide, 1);
        os.writeFromLSB(0xFF, 3);
        os.writeFromLSB(service_type.getValue(), 6);
        os.writeFromLSB(source_id, 16);
        os.writeFromLSB(0xFF, 6);
        os.writeFromLSB(getDescriptorsLength(), 10);
        if (getDescriptorSize() > 0) {
            Iterator<Descriptor> it = getDescriptors();
            while (it.hasNext()) os.write(it.next().toByteArray());
        }
        return os.toByteArray();
    }

    @Override
    public String toXMLString(int base_space) {
        String str = new String();
        str += (MyUTIL.whiteSpaceStr(base_space) + "<CVCTChannel>\n");
        str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<short_name>" + (new String(short_name)) + "</short_name>\n");
        str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<major_channel_number>" + major_channel_number + "</major_channel_number>\n");
        str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<minor_channel_number>" + minor_channel_number + "</minor_channel_number>\n");
        str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<modulation_mode>" + modulation_mode + "</modulation_mode>\n");
        str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<carrier_frequency>" + carrier_frequency + "</carrier_frequency>\n");
        str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<channel_TSID>" + channel_TSID + "</channel_TSID>\n");
        str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<program_number>" + program_number + "</program_number>\n");
        str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<ETM_location>" + ETM_location + "</ETM_location>\n");
        str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<access_controlled>" + access_controlled + "</access_controlled>\n");
        str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<hidden>" + hidden + "</hidden>\n");
        str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<path_select>" + path_select + "</path_select>\n");
        str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<out_of_band>" + out_of_band + "</out_of_band>\n");
        str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<hidden>" + hide_guide + "</hidden>\n");
        str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<service_type>" + service_type.getValue() + "</service_type>\n");
        str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<source_id>" + source_id + "</source_id>\n");
        if (getDescriptorSize() > 0) {
            Iterator<Descriptor> it = getDescriptors();
            str += (MyUTIL.whiteSpaceStr(base_space + 2) + "<DescriptorLoop>\n");
            while (it.hasNext()) str += it.next().toXMLString(base_space + 4);
            str += (MyUTIL.whiteSpaceStr(base_space + 2) + "</DescriptorLoop>\n");
        }
        str += (MyUTIL.whiteSpaceStr(base_space) + "</CVCTChannel>\n");
        return str;
    }

    @Override
    public boolean addDescriptor(Descriptor desc) {
        descs.add(desc);
        return true;
    }

    @Override
    public boolean addDescriptorAt(int index, Descriptor desc) {
        if (index < 0 || index > descs.size()) return false;
        descs.add(index, desc);
        return true;
    }

    @Override
    public Descriptor getDescriptorAt(int index) {
        if (index < 0 || index >= descs.size()) return null;
        return descs.get(index);
    }

    @Override
    public Iterator<Descriptor> getDescriptors() {
        return descs.iterator();
    }

    @Override
    public int getDescriptorSize() {
        return descs.size();
    }

    @Override
    public int getDescriptorsLength() {
        int desc_length = 0;
        Iterator<Descriptor> it = descs.iterator();
        while (it.hasNext()) desc_length += it.next().getSizeInBytes();
        return desc_length;
    }

    @Override
    public void removeAllDescriptors() {
        descs.removeAll(descs);
    }

    @Override
    public boolean removeDescriptor(Descriptor desc) {
        return descs.remove(desc);
    }

    @Override
    public boolean removeDescriptorAt(int index) {
        if (index < 0 || index >= descs.size()) return false;
        descs.remove(index);
        return true;
    }

    @Override
    public boolean setDescriptorAt(int index, Descriptor desc) {
        if (index < 0 || index >= descs.size()) return false;
        descs.set(index, desc);
        return true;
    }
}
