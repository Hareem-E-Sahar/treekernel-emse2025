package ggc.meter.device.onetouch;

import ggc.meter.data.MeterValuesEntry;
import ggc.meter.device.AbstractSerialMeter;
import ggc.meter.manager.MeterDevicesIds;
import ggc.meter.manager.company.LifeScan;
import ggc.meter.util.DataAccessMeter;
import ggc.plugin.device.DeviceIdentification;
import ggc.plugin.device.PlugInBaseException;
import ggc.plugin.manager.DeviceImplementationStatus;
import ggc.plugin.manager.company.AbstractDeviceCompany;
import ggc.plugin.output.AbstractOutputWriter;
import ggc.plugin.output.OutputUtil;
import ggc.plugin.output.OutputWriter;
import ggc.plugin.protocol.SerialProtocol;
import ggc.plugin.util.DataAccessPlugInBase;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import com.atech.utils.data.ATechDate;
import com.atech.utils.data.HexUtils;
import com.atech.utils.data.TimeZoneUtil;

/**
 *  Application:   GGC - GNU Gluco Control
 *  Plug-in:       Meter Tool (support for Meter devices)
 *
 *  See AUTHORS for copyright information.
 * 
 *  This program is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free Software
 *  Foundation; either version 2 of the License, or (at your option) any later
 *  version.
 * 
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 *  details.
 * 
 *  You should have received a copy of the GNU General Public License along with
 *  this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 *  Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 *  Filename:  ###---###  
 *  Description:
 * 
 *  Author: Andy {andy@atech-software.com}
 */
public abstract class OneTouchMeter2 extends AbstractSerialMeter {

    protected boolean device_running = true;

    protected TimeZoneUtil tzu = TimeZoneUtil.getInstance();

    private int entries_max = 0;

    private int entries_current = 0;

    private int reading_status = 0;

    SimpleTimeZone empty_tzi;

    /**
     * Constructor
     */
    public OneTouchMeter2() {
    }

    /**
     * Constructor for device manager
     * 
     * @param cmp
     */
    public OneTouchMeter2(AbstractDeviceCompany cmp) {
        super(cmp);
    }

    /**
     * Constructor
     * 
     * @param comm_parameters
     * @param writer
     * @param da 
     */
    public OneTouchMeter2(String comm_parameters, OutputWriter writer) {
        this(comm_parameters, writer, DataAccessMeter.getInstance());
    }

    /**
     * Constructor
     * 
     * @param comm_parameters
     * @param writer
     * @param da 
     */
    public OneTouchMeter2(String comm_parameters, OutputWriter writer, DataAccessPlugInBase da) {
        super(comm_parameters, writer, da);
        empty_tzi = new SimpleTimeZone(0, "Europe/Empty", 0, 0, 0, 0, 0, 0, 0, 0);
        this.setCommunicationSettings(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE, SerialPort.FLOWCONTROL_NONE, SerialProtocol.SERIAL_EVENT_BREAK_INTERRUPT | SerialProtocol.SERIAL_EVENT_OUTPUT_EMPTY);
        this.output_writer = writer;
        this.output_writer.getOutputUtil().setMaxMemoryRecords(this.getMaxMemoryRecords());
        this.setMeterType("LifeScan", this.getName());
        this.setDeviceCompany(new LifeScan());
        try {
            this.setSerialPort(comm_parameters);
            if (!this.open()) {
                this.m_status = 1;
                deviceDisconnected();
                return;
            }
            this.output_writer.writeHeader();
        } catch (Exception ex) {
            System.out.println("OneTouchMeter2 -> Exception: " + ex);
            ex.printStackTrace();
        }
    }

    /**
     * Constructor
     * 
     * @param n
     */
    public OneTouchMeter2(boolean n) {
    }

    /** 
     * getComment
     */
    public String getComment() {
        return null;
    }

    /** 
     * getImplementationStatus
     */
    public int getImplementationStatus() {
        return DeviceImplementationStatus.IMPLEMENTATION_TESTING;
    }

    HexUtils hex_utils = new HexUtils();

    String rec_bef = "02" + "0A" + "00" + "05" + "1F";

    String rec_af = "03" + "38" + "AA";

    String ack_pc = "02" + "06" + "07" + "03" + "FC" + "72";

    /** 
     * readDeviceDataFull
     */
    public void readDeviceDataFull() {
        this.output_writer.setBGOutputType(OutputUtil.BG_MMOL);
        try {
            byte[] reta;
            cmdDisconnectAcknowledge();
            this.output_writer.setSpecialProgress(1);
            if (!readDeviceInfo()) {
                deviceDisconnected();
                return;
            }
            this.output_writer.setSubStatus(ic.getMessage("READING_DATA_COUNTER"));
            System.out.println("PC-> read record 501 to receive nr");
            write(hex_utils.reconvert(rec_bef + "F5" + "01" + rec_af));
            reta = this.readLineBytes();
            reta = hex_utils.getByteSubArray(reta, 6 + 5, 3, 2);
            int nr = (reta[1] * 255) + reta[0];
            this.entries_max = nr;
            this.output_writer.setSpecialProgress(6);
            this.cmdAcknowledge();
            for (int i = 1; i <= nr; i++) {
                if (!readEntry(i)) {
                    deviceDisconnected();
                    break;
                }
            }
            cmdDisconnectAcknowledge();
            this.output_writer.setSubStatus(null);
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
            ex.printStackTrace();
        }
        if (this.isDeviceFinished()) {
            this.output_writer.endOutput();
        }
        System.out.println("Reading finsihed");
    }

    private boolean readDeviceInfo() throws Exception {
        DeviceIdentification di = this.output_writer.getDeviceIdentification();
        String read_sw_ver_create = getCommand(OneTouchMeter2.COMMAND_READ_SW_VERSION_AND_CREATE);
        this.output_writer.setSubStatus(ic.getMessage("READING_SW_VERSION"));
        write(hex_utils.reconvert(read_sw_ver_create));
        String sw_dd = tryToConvert(this.readLineBytes(), 6 + 6, 3, false);
        int idx = sw_dd.indexOf("/") - 2;
        if (idx == -3) {
            return false;
        }
        this.output_writer.setSpecialProgress(2);
        cmdAcknowledge();
        di.device_hardware_version = sw_dd.substring(0, idx) + ", " + sw_dd.substring(idx);
        this.output_writer.setSubStatus(ic.getMessage("READING_SERIAL_NR"));
        String read_serial_nr = getCommand(OneTouchMeter2.COMMAND_READ_SERIAL_NUMBER);
        write(hex_utils.reconvert(read_serial_nr));
        String sn = tryToConvert(this.readLineBytes(), 6 + 5, 3, false);
        di.device_serial_number = sn;
        this.cmdAcknowledge();
        this.output_writer.setSpecialProgress(4);
        this.output_writer.writeDeviceIdentification();
        return true;
    }

    /**
     * Command: Read Software Version And Create
     */
    public static final int COMMAND_READ_SW_VERSION_AND_CREATE = 1;

    /**
     * Command: Read Serial Number
     */
    public static final int COMMAND_READ_SERIAL_NUMBER = 2;

    /**
     * Get Command
     * 
     * @param command
     * @return
     */
    public abstract String getCommand(int command);

    private void cmdDisconnectAcknowledge() throws IOException {
        System.out.println("PC-> Disconnect");
        String disconect = "02" + "06" + "08" + "03" + "C2" + "62";
        write(hex_utils.reconvert(disconect));
        System.out.println("Disconected and acknowledged: " + this.readLine());
    }

    private void cmdAcknowledge() throws IOException {
        write(hex_utils.reconvert(ack_pc));
    }

    private boolean readEntry(int number) throws IOException {
        this.output_writer.setSubStatus(ic.getMessage("READING_PROCESSING_ENTRY") + number);
        int num_nr = number - 1;
        String nr1 = "";
        String nr2 = "00";
        if (num_nr > 255) {
            nr2 = "01";
            num_nr = num_nr - 255;
        }
        nr1 = Integer.toHexString(num_nr);
        if (nr1.length() == 1) nr1 = "0" + nr1;
        System.out.println(nr1 + " " + nr2);
        System.out.println("PC-> read record #" + number);
        char[] for_crc = new char[8];
        for_crc[0] = 0x02;
        for_crc[1] = 0x0A;
        if (number % 2 == 0) for_crc[2] = 0x00; else for_crc[2] = 0x03;
        for_crc[3] = 0x05;
        for_crc[4] = 0x1F;
        for_crc[5] = (char) (num_nr);
        if (nr2.equals("01")) for_crc[6] = 0x01; else for_crc[6] = 0x00;
        for_crc[7] = 0x03;
        String crc_ret = Integer.toHexString(this.calculateCrc(for_crc));
        for (int i = crc_ret.length(); i < 4; i++) {
            crc_ret = "0" + crc_ret;
        }
        String create_question = "02" + "0A";
        if (number % 2 == 0) create_question += "00"; else create_question += "03";
        create_question += "05" + "1F";
        create_question += nr1 + nr2;
        create_question += "03";
        create_question += crc_ret.substring(2).toUpperCase();
        create_question += crc_ret.substring(0, 2).toUpperCase();
        write(hex_utils.reconvert(create_question));
        byte[] reta = this.readLineBytes();
        if ((reta == null) || (reta.length == 0)) {
            this.setDeviceStopped();
            return false;
        }
        hex_utils.showByteArrayHex(reta);
        byte[] dt_bg = hex_utils.getByteSubArray(reta, 6 + 5, 3, 8);
        int bg_val = Integer.parseInt(hex_utils.getCorrectHexValue(dt_bg[7]) + hex_utils.getCorrectHexValue(dt_bg[6]) + hex_utils.getCorrectHexValue(dt_bg[5]) + hex_utils.getCorrectHexValue(dt_bg[4]), 16);
        long dt_val = Integer.parseInt(hex_utils.getCorrectHexValue(dt_bg[3]) + hex_utils.getCorrectHexValue(dt_bg[2]) + hex_utils.getCorrectHexValue(dt_bg[1]) + hex_utils.getCorrectHexValue(dt_bg[0]), 16);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(dt_val * 1000);
        gc.setTimeZone(empty_tzi);
        MeterValuesEntry mve = new MeterValuesEntry();
        mve.setBgUnit(OutputUtil.BG_MGDL);
        mve.setBgValue("" + bg_val);
        mve.setDateTimeObject(tzu.getCorrectedDateTime(new ATechDate(ATechDate.FORMAT_DATE_AND_TIME_MIN, gc)));
        this.output_writer.writeData(mve);
        this.entries_current = number;
        readingEntryStatus();
        return true;
    }

    private void readingEntryStatus() {
        float proc_read = ((this.entries_current * 1.0f) / this.entries_max);
        float proc_total = 6 + (94 * proc_read);
        System.out.println("proc_read: " + proc_read + ", proc_total: " + proc_total);
        this.output_writer.setSpecialProgress((int) proc_total);
    }

    boolean debug_crc = false;

    private int calculateCrc(char[] buffer) {
        return this.calculateCrc((short) 0xffff, buffer);
    }

    private int calculateCrc(int initial_crc, char[] buffer) {
        int crc = initial_crc;
        if (buffer != null) {
            for (int index = 0; index < buffer.length; index++) {
                crcDebug("-- Round " + index + "  [" + Integer.toHexString(crc) + "]");
                crc = (char) ((char) (crc >> 8) | (short) (crc << 8));
                crcDebug("Crc 1: " + crc + " " + Integer.toHexString(crc));
                crc ^= buffer[index];
                crcDebug("Crc 2: " + crc + " " + Integer.toHexString(crc));
                crc ^= (char) (crc & 0xff) >> 4;
                crcDebug("Crc 3: " + crc + " " + Integer.toHexString(crc));
                crc ^= (char) ((crc << 8) << 4);
                crcDebug("Crc 4: " + crc + " " + Integer.toHexString(crc));
                crc ^= (int) (((crc & 0xff) << 4) << 1);
                crcDebug("Crc 5: " + crc + " " + Integer.toHexString(crc) + "\n");
            }
        }
        return crc;
    }

    private void crcDebug(String val) {
        if (this.debug_crc) System.out.println(val);
    }

    /**
     * hasSpecialProgressStatus - in most cases we read data directly from device, in this case we have 
     *    normal progress status, but with some special devices we calculate progress through other means.
     * @return true is progress status is special
     */
    public boolean hasSpecialProgressStatus() {
        return true;
    }

    private boolean isDeviceFinished() {
        return (this.entries_current == this.entries_max);
    }

    private String tryToConvert(byte[] arr, int start, int end, boolean display) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < (arr.length - end); i++) {
            sb.append((char) arr[i]);
        }
        String ret = sb.toString();
        if (display) System.out.println(ret);
        return ret;
    }

    /**
     * This is method for reading partitial data from device. All reading from actual device should be done from 
     * here. Reading can be done directly here, or event can be used to read data.
     */
    public void readDeviceDataPartitial() throws PlugInBaseException {
    }

    /** 
     * This is method for reading configuration
     * 
     * @throws PlugInBaseException
     */
    public void readConfiguration() throws PlugInBaseException {
    }

    /**
     * This is for reading device information. This should be used only if normal dump doesn't retrieve this
     * information (most dumps do). 
     * @throws PlugInBaseException
     */
    public void readInfo() throws PlugInBaseException {
    }

    @SuppressWarnings("unused")
    private boolean isDeviceStopped(String vals) {
        if ((vals == null) || ((this.reading_status == 1) && (vals.length() == 0)) || (!this.device_running) || (this.output_writer.isReadingStopped())) return true;
        return false;
    }

    /**
     * 
     */
    public void setDeviceStopped() {
        this.device_running = false;
        this.output_writer.endOutput();
    }

    protected String getParameterValue(String val) {
        String d = val.substring(1, val.length() - 1);
        return d.trim();
    }

    /**
     * Returns short name for meter (for example OT Ultra, would return "Ultra")
     * 
     * @return short name of meter
     */
    public abstract String getShortName();

    /**
     * We don't use serial event for reading data, because process takes too long, we use serial event just 
     * to determine if device is stopped (interrupted) 
     */
    @Override
    public void serialEvent(SerialPortEvent event) {
        switch(event.getEventType()) {
            case SerialPortEvent.BI:
                System.out.println("recievied break");
                this.output_writer.setStatus(AbstractOutputWriter.STATUS_STOPPED_DEVICE);
                break;
            case SerialPortEvent.CD:
                System.out.println("recievied cd");
                break;
            case SerialPortEvent.CTS:
                System.out.println("recievied cts");
                break;
            case SerialPortEvent.DSR:
                System.out.println("recievied dsr");
                break;
            case SerialPortEvent.FE:
                System.out.println("recievied fe");
                break;
            case SerialPortEvent.OE:
                System.out.println("recievied oe");
                System.out.println("Output Empty");
                break;
            case SerialPortEvent.PE:
                System.out.println("recievied pe");
                break;
            case SerialPortEvent.RI:
                System.out.println("recievied ri");
                break;
        }
    }

    /**
     * getCompanyId - Get Company Id 
     * 
     * @return id of company
     */
    public int getCompanyId() {
        return MeterDevicesIds.COMPANY_LIFESCAN;
    }
}
