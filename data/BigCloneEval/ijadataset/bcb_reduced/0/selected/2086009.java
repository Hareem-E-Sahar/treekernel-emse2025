package synthdrivers.Line6BassPod;

import core.*;
import java.io.UnsupportedEncodingException;
import javax.swing.*;

/** Line6 Single Driver. Used for Line6 program patch.
* @author Jeff Weber
*/
public class Line6BassPodSingleDriver extends Driver {

    /** Single Program Dump Request
    */
    private static final SysexHandler SYS_REQ = new SysexHandler(Constants.SIGL_DUMP_REQ_ID);

    /** Size of the sysex header for a single patch dump
    */
    private static int hdrSize = Constants.PDMP_HDR_SIZE;

    /** Offset of the patch name in the sysex record, not including the sysex header.*/
    private static int nameStart = Constants.PATCH_NAME_START;

    /** Constructs a Line6BassPodSingleDriver.
    */
    public Line6BassPodSingleDriver() {
        super(Constants.SIGL_PATCH_TYP_STR, Constants.AUTHOR);
        sysexID = Constants.SIGL_SYSEX_MATCH_ID;
        patchSize = Constants.PDMP_HDR_SIZE + Constants.SIGL_SIZE + 1;
        patchNameStart = Constants.PDMP_HDR_SIZE + Constants.PATCH_NAME_START;
        patchNameSize = Constants.PATCH_NAME_SIZE;
        deviceIDoffset = Constants.DEVICE_ID_OFFSET;
        bankNumbers = Constants.PRGM_BANK_LIST;
        patchNumbers = Constants.PRGM_PATCH_LIST;
    }

    /** Constructs a Line6BassPodSingleDriver. Called by Line6BassPodEdBufDriver
        */
    public Line6BassPodSingleDriver(String patchType, String authors) {
        super(patchType, authors);
    }

    /** Null method. Line6 devices do not use checksum.
        */
    protected void calculateChecksum(Patch p) {
    }

    /** Null method. Line6 devices do not use checksum.
        */
    protected void calculateChecksum(Patch p, int start, int end, int ofs) {
    }

    /** Gets the name of the program patch.
        * Patch p is the target program patch.
        */
    protected String getPatchName(Patch p) {
        char c[] = new char[patchNameSize];
        for (int i = 0; i < patchNameSize; i++) {
            c[i] = (char) PatchBytes.getSysexByte(p.sysex, Constants.PDMP_HDR_SIZE, Constants.PDMP_HDR_SIZE + i + nameStart);
        }
        return new String(c);
    }

    /** Sets the name of the program patch.
        * Patch p is the target program patch. String name
        * contains the name to be assigned to the patch.
        */
    protected void setPatchName(Patch p, String name) {
        if (name.length() < patchNameSize) name = name + "                ";
        byte nameBytes[] = new byte[patchNameSize];
        try {
            nameBytes = name.getBytes("US-ASCII");
            for (int i = 0; i < patchNameSize; i++) {
                PatchBytes.setSysexByte(p, Constants.PDMP_HDR_SIZE, Constants.PDMP_HDR_SIZE + i + nameStart, nameBytes[i]);
            }
        } catch (UnsupportedEncodingException ex) {
            return;
        }
    }

    /** Converts a single program patch to an edit buffer patch and sends it to
        * the edit buffer. Patch p is the patch to be sent.
        */
    protected void sendPatch(Patch p) {
        byte[] saveSysex = p.sysex;
        int newSysexLength = p.sysex.length - 1;
        byte newSysex[] = new byte[newSysexLength];
        System.arraycopy(Constants.EDIT_DUMP_HDR_BYTES, 0, newSysex, 0, Constants.EDMP_HDR_SIZE);
        System.arraycopy(p.sysex, Constants.PDMP_HDR_SIZE, newSysex, Constants.EDMP_HDR_SIZE, newSysexLength - Constants.EDMP_HDR_SIZE);
        p.sysex = newSysex;
        sendPatchWorker(p);
        p.sysex = saveSysex;
    }

    /** Sends a a single program patch to a set patch location in the device.
        * bankNum is a user bank number in the range 0 to 9.
        * patchNum is a patch number within the bank, in the range 0 to 3.
        */
    protected void storePatch(Patch p, int bankNum, int patchNum) {
        int progNum = bankNum * 4 + patchNum;
        p.sysex[0] = (byte) 0xF0;
        p.sysex[7] = (byte) progNum;
        sendPatchWorker(p);
        try {
            Thread.sleep(Constants.PATCH_SEND_INTERVAL);
        } catch (Exception e) {
        }
    }

    /** Presents a dialog instructing the user to play his instrument.
        * Line6 Pod devices do not "Play" patches, so a dialog is presented instead.
        */
    protected void playPatch(Patch p) {
        ErrorMsg.reportStatus(getPatchName(p) + "  Header -- " + "  " + Utility.hexDump(p.sysex, 0, Constants.PDMP_HDR_SIZE, 16) + "  Data -- " + "  " + Utility.hexDump(p.sysex, Constants.PDMP_HDR_SIZE, -1, 16));
        JFrame frame = new JFrame();
        JOptionPane.showMessageDialog(frame, Constants.PLAY_CMD_MSG);
    }

    /** Creates a new program patch with default values.
        */
    protected Patch createNewPatch() {
        Patch p = new Patch(Constants.NEW_SYSEX, this);
        setPatchName(p, "NewPatch        ");
        return p;
    }

    /** Requests a dump of a single program patch.
        * Even though, from an operational standpoint, the POD has nine banks
        * (numbered 1 through 9) of four patches each (numbered A, B, C, and D),
        * internally there is only a single bank of 36 patch locations,
        * referenced by program change numbers 0-35. By assigning the numbers 0
        * through 8 for the banks and 0 through 3 for the patches,
        * the conversion is as follows:
        * program number = (bank number * 4) + patch number 
        */
    public void requestPatchDump(int bankNum, int patchNum) {
        int progNum = bankNum * 4 + patchNum;
        send(SYS_REQ.toSysexMessage(getChannel(), new SysexHandler.NameValue("progNum", progNum)));
    }

    /** Opens an edit window on the specified patch.
        */
    protected JSLFrame editPatch(Patch p) {
        return new Line6BassPodSingleEditor((Patch) p);
    }
}
