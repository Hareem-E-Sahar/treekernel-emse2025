package Core;

import API.BitOutputStream;
import API.SITable;
import API.Section;
import API.TableID;
import CRC32.Crc32Mpeg2;

/**
 * @author SungHun Park (dr.superchamp@gmail.com)
 *
 */
public class SectionDefaultImpl implements Section {

    protected SITable correspondent_table = null;

    protected TableID table_id = TableID.FORBIDDEN;

    int section_syntax_indicator = 0;

    int private_indicator = 0;

    int reserved1 = 0;

    int table_id_extension = 0;

    int reserved2 = 0;

    int version_number = 0;

    int current_next_indicator = 0;

    int section_number = 0;

    int last_section_number = 0;

    byte[] private_data_bytes = null;

    public SectionDefaultImpl(SITable table, int ssi) {
        if (table == null) throw new NullPointerException();
        correspondent_table = table;
        table_id = correspondent_table.getTableID();
        section_syntax_indicator = ssi;
    }

    @Override
    public long getCRC32() {
        if (getSectionSyntaxIndicator() == 0) throw new UnsupportedOperationException();
        BitOutputStream os = new BitOutputStream(getSectionSizeInByte() * Byte.SIZE);
        os.writeFromLSB(table_id.getValue(), 8);
        os.writeFromLSB(section_syntax_indicator, 1);
        os.writeFromLSB(private_indicator, 1);
        os.writeFromLSB(reserved1, 2);
        os.writeFromLSB(getPrivateSectionLength(), 12);
        os.writeFromLSB(table_id_extension, 16);
        os.writeFromLSB(reserved2, 2);
        os.writeFromLSB(version_number, 5);
        os.writeFromLSB(current_next_indicator, 1);
        os.writeFromLSB(section_number, 8);
        os.writeFromLSB(last_section_number, 8);
        os.write(getPrivateDataBytes());
        Crc32Mpeg2 crc = new Crc32Mpeg2();
        byte[] byte_array = os.toByteArray();
        crc.update(byte_array, 0, byte_array.length - 4);
        return crc.getValue();
    }

    @Override
    public int getCurrentNextIndicator() {
        if (getSectionSyntaxIndicator() == 0) throw new UnsupportedOperationException();
        return current_next_indicator;
    }

    @Override
    public int getLastSectionNumber() {
        if (getSectionSyntaxIndicator() == 0) throw new UnsupportedOperationException();
        return last_section_number;
    }

    @Override
    public byte[] getPrivateDataBytes() {
        return private_data_bytes;
    }

    @Override
    public int getPrivateIndicator() {
        return private_indicator;
    }

    @Override
    public int getPrivateSectionLength() {
        int length = 0;
        if (section_syntax_indicator != 0) length += 9;
        if (private_data_bytes != null) length += private_data_bytes.length;
        return length;
    }

    @Override
    public int getReserved1() {
        return reserved1;
    }

    @Override
    public int getReserved2() {
        return reserved2;
    }

    @Override
    public SITable getSITable() {
        return correspondent_table;
    }

    @Override
    public byte[] getSectionBytes() {
        BitOutputStream os = new BitOutputStream(getSectionSizeInByte() * Byte.SIZE);
        os.writeFromLSB(table_id.getValue(), 8);
        os.writeFromLSB(section_syntax_indicator, 1);
        os.writeFromLSB(private_indicator, 1);
        os.writeFromLSB(reserved1, 2);
        os.writeFromLSB(getPrivateSectionLength(), 12);
        if (getSectionSyntaxIndicator() != 0) {
            os.writeFromLSB(table_id_extension, 16);
            os.writeFromLSB(reserved2, 2);
            os.writeFromLSB(version_number, 5);
            os.writeFromLSB(current_next_indicator, 1);
            os.writeFromLSB(section_number, 8);
            os.writeFromLSB(last_section_number, 8);
        }
        os.write(getPrivateDataBytes());
        if (getSectionSyntaxIndicator() != 0) {
            Crc32Mpeg2 crc = new Crc32Mpeg2();
            byte[] byte_array = os.toByteArray();
            crc.update(byte_array, 0, byte_array.length - 4);
            os.writeFromLSB((int) (crc.getValue() & 0x00000000FFFFFFFF), 32);
        }
        return os.toByteArray();
    }

    @Override
    public int getSectionNumber() {
        if (getSectionSyntaxIndicator() == 0) throw new UnsupportedOperationException();
        return section_number;
    }

    @Override
    public int getSectionSizeInByte() {
        return getPrivateSectionLength() + 3;
    }

    @Override
    public int getSectionSyntaxIndicator() {
        return section_syntax_indicator;
    }

    @Override
    public TableID getTableID() {
        return table_id;
    }

    @Override
    public int getTableIdExtension() {
        if (getSectionSyntaxIndicator() == 0) throw new UnsupportedOperationException();
        return table_id_extension;
    }

    @Override
    public int getVersionNumber() {
        if (getSectionSyntaxIndicator() == 0) throw new UnsupportedOperationException();
        return version_number;
    }

    @Override
    public void setCurrentNextIndicator(int cur_nxt_ind) {
        if (getSectionSyntaxIndicator() == 0) throw new UnsupportedOperationException();
        current_next_indicator = cur_nxt_ind;
    }

    @Override
    public void setLastSectionNumber(int last_sec_num) {
        if (getSectionSyntaxIndicator() == 0) throw new UnsupportedOperationException();
        last_section_number = last_sec_num;
    }

    @Override
    public void setPrivateData(byte[] data) {
        private_data_bytes = data;
    }

    @Override
    public void setPrivateIndicator(int value) {
        private_indicator = value;
    }

    @Override
    public void setReserved1(int value) {
        reserved1 = value;
    }

    @Override
    public void setReserved2(int value) {
        reserved2 = value;
    }

    @Override
    public void setSectionNumber(int sec_num) {
        if (getSectionSyntaxIndicator() == 0) throw new UnsupportedOperationException();
        section_number = sec_num;
    }

    @Override
    public void setTableIdExtension(int table_id_ext) {
        if (getSectionSyntaxIndicator() == 0) throw new UnsupportedOperationException();
        table_id_extension = table_id_ext;
    }

    @Override
    public void setVersionNumber(int version_num) {
        if (getSectionSyntaxIndicator() == 0) throw new UnsupportedOperationException();
        version_number = version_num;
    }
}
