package synthdrivers.KorgWavestation;

import core.Driver;
import core.ErrorMsg;
import core.Patch;
import core.SysexHandler;

/** Driver for Korg Wavestation Micro Tuning Scales
 *
 * Be carefull: Untested, because I only have access to
 * a file containing some WS patches....
 *
 * @author Gerrit Gehnen
 * @version $Id: KorgWavestationMicroTuneScaleDriver.java 939 2005-03-03 04:05:40Z hayashi $
 */
public class KorgWavestationMicroTuneScaleDriver extends Driver {

    public KorgWavestationMicroTuneScaleDriver() {
        super("Micro Tune Scale", "Gerrit Gehnen");
        sysexID = "F0423*285A";
        sysexRequestDump = new SysexHandler("F0 42 @@ 28 08 F7");
        trimSize = 297;
        patchNameStart = 0;
        patchNameSize = 0;
        deviceIDoffset = 0;
        checksumStart = 5;
        checksumEnd = 294;
        checksumOffset = 295;
    }

    public void storePatch(Patch p, int bankNum, int patchNum) {
        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }
        ((Patch) p).sysex[2] = (byte) (0x30 + getChannel() - 1);
        try {
            send(((Patch) p).sysex);
        } catch (Exception e) {
            ErrorMsg.reportStatus(e);
        }
    }

    public void sendPatch(Patch p) {
        ((Patch) p).sysex[2] = (byte) (0x30 + getChannel() - 1);
        try {
            send(((Patch) p).sysex);
        } catch (Exception e) {
            ErrorMsg.reportStatus(e);
        }
    }

    public Patch createNewPatch() {
        byte[] sysex = new byte[297];
        sysex[00] = (byte) 0xF0;
        sysex[01] = (byte) 0x42;
        sysex[2] = (byte) (0x30 + getChannel() - 1);
        sysex[03] = (byte) 0x28;
        sysex[04] = (byte) 0x5A;
        sysex[296] = (byte) 0xF7;
        Patch p = new Patch(sysex, this);
        setPatchName(p, "New Patch");
        calculateChecksum(p);
        return p;
    }

    protected void calculateChecksum(Patch p, int start, int end, int ofs) {
        int i;
        int sum = 0;
        for (i = start; i <= end; i++) {
            sum += p.sysex[i];
        }
        p.sysex[ofs] = (byte) (sum % 128);
    }

    public void requestPatchDump(int bankNum, int patchNum) {
        send(sysexRequestDump.toSysexMessage(getChannel(), 0));
    }
}
