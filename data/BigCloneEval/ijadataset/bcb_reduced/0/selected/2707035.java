package org.jsynthlib.synthdrivers.KorgER1;

import org.jsynthlib.core.Device;
import org.jsynthlib.core.Driver;
import org.jsynthlib.core.Logger;
import org.jsynthlib.core.Patch;
import org.jsynthlib.core.SysexHandler;

public class KorgER1SingleDriver extends Driver {

    public KorgER1SingleDriver(final Device device) {
        super(device, "Single", "Yves Lefebvre");
        sysexID = "F0423*5140";
        sysexRequestDump = new SysexHandler("F0 42 @@ 51 10 F7");
        patchSize = 1085;
        patchNameStart = 0;
        patchNameSize = 0;
        deviceIDoffset = 0;
        checksumStart = 0;
        checksumEnd = 0;
        checksumOffset = 0;
        bankNumbers = new String[] { "Bank A", "Bank B", "Bank C", "Bank D" };
        patchNumbers = new String[] { "01-", "02-", "03-", "04-", "05-", "06-", "07-", "08-", "09-", "10-", "11-", "12-", "13-", "14-", "15-", "16-", "17-", "18-", "19-", "20-", "21-", "22-", "23-", "24-", "25-", "26-", "27-", "28-", "29-", "30-", "31-", "32-", "33-", "34-", "35-", "36-", "37-", "38-", "39-", "40-", "41-", "42-", "43-", "44-", "45-", "46-", "47-", "48-", "49-", "50-", "51-", "52-", "53-", "54-", "55-", "56-", "57-", "58-", "59-", "60-", "61-", "62-", "63-", "64-" };
    }

    public void storePatch(Patch p, int bankNum, int patchNum) {
        int patchValue = patchNum;
        int bankValue = 0;
        if (bankNum == 1 || bankNum == 3) {
            patchValue += 64;
        }
        if (bankNum > 1) {
            bankValue = 1;
        }
        setBankNum(bankValue);
        setPatchNum(patchValue);
        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }
        p.sysex[2] = (byte) (0x30 + getChannel() - 1);
        try {
            send(p.sysex);
        } catch (Exception e) {
            Logger.reportStatus(e);
        }
        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }
        byte[] sysex = new byte[8];
        sysex[0] = (byte) 0xF0;
        sysex[1] = (byte) 0x42;
        sysex[2] = (byte) (0x30 + getChannel() - 1);
        sysex[3] = (byte) 0x51;
        sysex[4] = (byte) 0x11;
        sysex[5] = (byte) (bankValue);
        sysex[6] = (byte) (patchValue);
        sysex[7] = (byte) 0xF7;
        try {
            send(sysex);
        } catch (Exception e) {
            Logger.reportStatus(e);
        }
    }

    public void sendPatch(Patch p) {
        p.sysex[2] = (byte) (0x30 + getChannel() - 1);
        try {
            send(p.sysex);
        } catch (Exception e) {
            Logger.reportStatus(e);
        }
    }

    public Patch createNewPatch() {
        byte[] sysex = new byte[1085];
        sysex[0] = (byte) 0xF0;
        sysex[1] = (byte) 0x42;
        sysex[2] = (byte) (0x30 + getChannel() - 1);
        sysex[3] = (byte) 0x51;
        sysex[4] = (byte) 0x40;
        sysex[1084] = (byte) 0xF7;
        Patch p = new Patch(sysex, this);
        setPatchName(p, "New Patch");
        calculateChecksum(p);
        return p;
    }

    protected void calculateChecksum(Patch p, int start, int end, int ofs) {
    }

    public void setPatchNum(int patchNum) {
        try {
            send(0xC0 + (getChannel() - 1), patchNum);
        } catch (Exception e) {
        }
    }

    public void requestPatchDump(int bankNum, int patchNum) {
        send(sysexRequestDump.toSysexMessage(getChannel(), patchNum + 0x30));
    }
}
