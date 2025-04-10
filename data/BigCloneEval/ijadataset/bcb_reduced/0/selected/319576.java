package synthdrivers.KorgWavestation;

import core.Driver;
import core.ErrorMsg;
import core.Patch;
import core.SysexHandler;

/** Driver for Korg Wavestation Single Performances
 *
 * Be carefull: Untested, because I only have access to
 * a file containing some WS patches....
 *
 * @author Gerrit Gehnen
 * @version $Id: KorgWavestationSinglePerformanceDriver.java 939 2005-03-03 04:05:40Z hayashi $
 */
public class KorgWavestationSinglePerformanceDriver extends Driver {

    public KorgWavestationSinglePerformanceDriver() {
        super("Single Performance", "Gerrit Gehnen");
        sysexID = "F0423*2849";
        sysexRequestDump = new SysexHandler("F0 42 @@ 28 19 *bankNum* *patchNum* F7");
        trimSize = 1085;
        patchNameStart = 0;
        patchNameSize = 0;
        deviceIDoffset = 0;
        checksumStart = 7;
        checksumEnd = 1082;
        checksumOffset = 1083;
        bankNumbers = new String[] { "RAM1", "RAM2", "ROM1", "CARD", "RAM3" };
        patchNumbers = new String[] { "01-", "02-", "03-", "04-", "05-", "06-", "07-", "08-", "09-", "10-", "11-", "12-", "13-", "14-", "15-", "16-", "17-", "18-", "19-", "20-", "21-", "22-", "23-", "24-", "25-", "26-", "27-", "28-", "29-", "30-", "31-", "32-", "33-", "34-", "35-", "36-", "37-", "38-", "39-", "40-", "41-", "42-", "43-", "44-", "45-", "46-", "47-", "48-", "49-", "50-" };
    }

    public void storePatch(Patch p, int bankNum, int patchNum) {
        setBankNum(bankNum);
        setPatchNum(patchNum);
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
        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }
        byte[] sysex = new byte[8];
        sysex[0] = (byte) 0xF0;
        sysex[1] = (byte) 0x42;
        sysex[2] = (byte) (0x30 + getChannel() - 1);
        sysex[3] = (byte) 0x28;
        sysex[4] = (byte) 0x1A;
        sysex[5] = (byte) (bankNum);
        sysex[6] = (byte) (patchNum);
        sysex[7] = (byte) 0xF7;
        try {
            send(sysex);
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
        byte[] sysex = new byte[1085];
        sysex[0] = (byte) 0xF0;
        sysex[1] = (byte) 0x42;
        sysex[2] = (byte) (0x30 + getChannel() - 1);
        sysex[3] = (byte) 0x28;
        sysex[4] = (byte) 0x49;
        sysex[5] = (byte) 0;
        sysex[6] = (byte) 0;
        sysex[1084] = (byte) 0xF7;
        Patch p = new Patch(sysex, this);
        setPatchName(p, "New Patch");
        calculateChecksum(p);
        return p;
    }

    protected void calculateChecksum(Patch p, int start, int end, int ofs) {
        int i;
        int sum = 0;
        System.out.println("Checksum was" + p.sysex[ofs]);
        for (i = start; i <= end; i++) {
            sum += p.sysex[i];
        }
        p.sysex[ofs] = (byte) (sum % 128);
        System.out.println("Checksum new is" + p.sysex[ofs]);
    }

    public void setPatchNum(int patchNum) {
        try {
            send(0xC0 + (getChannel() - 1), patchNum);
        } catch (Exception e) {
        }
        ;
    }

    public void requestPatchDump(int bankNum, int patchNum) {
        SysexHandler.NameValue nv[] = new SysexHandler.NameValue[2];
        nv[0] = new SysexHandler.NameValue("bankNum", bankNum);
        nv[1] = new SysexHandler.NameValue("patchNum", patchNum);
        send(sysexRequestDump.toSysexMessage(getChannel(), nv));
    }
}
