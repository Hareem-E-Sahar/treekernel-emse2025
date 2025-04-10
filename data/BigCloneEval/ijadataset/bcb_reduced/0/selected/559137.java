package synthdrivers.RolandXV5080;

import java.io.UnsupportedEncodingException;
import core.BankDriver;
import core.ErrorMsg;
import core.Patch;
import core.SysexHandler;
import core.Utility;

public class RolandXV5080PerfBankDriver extends BankDriver {

    static final SysexHandler SYSEX_REQUEST_DUMP = new SysexHandler("F0 41 10 00 10 11 20 *patchNum* 00 00 00 40 00 00 00 F7");

    public RolandXV5080PerfBankDriver() {
        super("PerfBank", "Phil Shepherd", RolandXV5080PerfDriver.PATCH_NUMBERS.length, 4);
        sysexID = "F0411000101220000000";
        sysexRequestDump = SYSEX_REQUEST_DUMP;
        patchSize = 64 * RolandXV5080PerfDriver.PATCH_SIZE;
        deviceIDoffset = 0;
        singleSysexID = "F0411000101220**0000";
        singleSize = RolandXV5080PerfDriver.PATCH_SIZE;
        bankNumbers = RolandXV5080PerfDriver.BANK_NUMBERS;
        patchNumbers = RolandXV5080PerfDriver.PATCH_NUMBERS;
    }

    public void setBankNum(int bankNum) {
        try {
            send(0xB0 + (getChannel() - 1), 0x00, 0x65);
            send(0xB0 + (getChannel() - 1), 0x20, 0);
        } catch (Exception e) {
        }
        ;
    }

    public String getPatchName(Patch ip) {
        return getNumPatches() + " patches";
    }

    public String getPatchName(Patch p, int patchNum) {
        try {
            return new String(((Patch) p).sysex, RolandXV5080PerfDriver.PATCH_SIZE * patchNum + RolandXV5080PerfDriver.PATCH_NAME_START, RolandXV5080PerfDriver.PATCH_NAME_SIZE, "US-ASCII");
        } catch (UnsupportedEncodingException ex) {
            return "-??????-";
        }
    }

    public void setPatchName(Patch bank, int patchNum, String name) {
        Patch p = getPatch(bank, patchNum);
        p.setName(name);
        p.calculateChecksum();
        putPatch(bank, p, patchNum);
    }

    public Patch getPatch(Patch bank, int patchNum) {
        try {
            byte[] sysex = new byte[RolandXV5080PerfDriver.PATCH_SIZE];
            System.arraycopy(((Patch) bank).sysex, RolandXV5080PerfDriver.PATCH_SIZE * patchNum, sysex, 0, RolandXV5080PerfDriver.PATCH_SIZE);
            return new Patch(sysex, getDevice());
        } catch (Exception ex) {
            ErrorMsg.reportError("Error", "Error in XV5080 Perf Bank Driver", ex);
            return null;
        }
    }

    public void putPatch(Patch bank, Patch p, int patchNum) {
        Patch pInsert = new Patch(((Patch) p).sysex);
        RolandXV5080PerfDriver singleDriver = (RolandXV5080PerfDriver) pInsert.getDriver();
        singleDriver.updatePatchNum(pInsert, patchNum);
        singleDriver.calculateChecksum(pInsert);
        ((Patch) bank).sysex = Utility.byteArrayReplace(((Patch) bank).sysex, RolandXV5080PerfDriver.PATCH_SIZE * patchNum, RolandXV5080PerfDriver.PATCH_SIZE, pInsert.sysex, 0, RolandXV5080PerfDriver.PATCH_SIZE);
    }

    public void requestPatchDump(int bankNum, int patchNum) {
        byte[] sysex = SYSEX_REQUEST_DUMP.toByteArray(getChannel(), patchNum);
        RolandXV5080PatchDriver.calculateChecksum(sysex, 6, sysex.length - 3, sysex.length - 2);
        send(sysex);
    }
}
