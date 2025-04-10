package org.jsynthlib.synthdrivers.AlesisQS;

import org.jsynthlib.core.Device;
import org.jsynthlib.core.Driver;
import org.jsynthlib.core.Logger;
import org.jsynthlib.core.JSLFrame;
import org.jsynthlib.core.Patch;
import org.jsynthlib.core.SysexHandler;

/**
 * AlesisQSGlobalDriver.java
 *
 * Global program driver for Alesis QS series synths
 * Feb 2002
 * @author Zellyn Hunter (zjh, zellyn@zellyn.com)
 * @version $Id: AlesisQSGlobalDriver.java 1186 2011-12-19 14:11:46Z chriswareham $
 * GPL v2
 */
public class AlesisQSGlobalDriver extends Driver {

    public AlesisQSGlobalDriver(final Device device) {
        super(device, "Global", "Zellyn Hunter");
        sysexID = "F000000E0E**";
        sysexRequestDump = new SysexHandler("F0 00 00 0E 0E 0B F7");
        patchSize = QSConstants.PATCH_SIZE_GLOBAL;
        deviceIDoffset = 0;
        checksumStart = 0;
        checksumEnd = 0;
        checksumOffset = 0;
        bankNumbers = QSConstants.GLOBAL_BANK_NAME_DUMMY;
        patchNumbers = QSConstants.GLOBAL_PATCH_NUMBERS_DUMMY;
    }

    /**
         * Print a byte in binary, for debugging packing/unpacking code
         **/
    public String toBinaryStr(byte b) {
        String output = new String();
        for (int i = 7; i >= 0; i--) {
            output += ((b >> i) & 1);
        }
        return output;
    }

    /**
         * Override the checksum and do nothing - the Alesis does not use checksums
         * @param p the ignored
         * @param start ignored
         * @param end ignored
         * @param ofs ignored
         */
    protected void calculateChecksum(Patch p, int start, int end, int ofs) {
    }

    /**
         * Create a new global patch
         * @return the new Patch
         */
    public Patch createNewPatch() {
        byte[] sysex = new byte[patchSize];
        for (int i = 0; i < QSConstants.GENERIC_HEADER.length; i++) {
            sysex[i] = QSConstants.GENERIC_HEADER[i];
        }
        sysex[QSConstants.POSITION_OPCODE] = QSConstants.OPCODE_MIDI_GLOBAL_DATA_DUMP;
        return new Patch(sysex, this);
    }

    public JSLFrame editPatch(Patch p) {
        return new GlobalEditor((Patch) p);
    }

    /**
         * Copied from Driver.java by zjh.  Requests a global data dump.
         * Use opcode 0B - MIDI Global Data Dump Request.
         * @param bankNum not used
         * @param patchNum not used
         */
    public void requestPatchDump(int bankNum, int patchNum) {
        send(sysexRequestDump.toSysexMessage(getChannel()));
    }

    /**
         * Sends a patch to the synth's global data buffer.
         * @param p the patch to send to the global data area
         */
    public void sendPatch(Patch p) {
        storePatch(p, 0, 0);
    }

    /**
         * Sends global data.  Location is irrelevant
         * @param p the patch to send
         * @param bankNum ignored
         * @param patchNum ignored
         */
    public void storePatch(Patch p, int bankNum, int patchNum) {
        byte opcode = QSConstants.OPCODE_MIDI_GLOBAL_DATA_DUMP;
        byte oldOpcode = ((Patch) p).sysex[QSConstants.POSITION_OPCODE];
        ((Patch) p).sysex[QSConstants.POSITION_OPCODE] = opcode;
        Logger.reportStatus(((Patch) p).sysex);
        sendPatchWorker(p);
        ((Patch) p).sysex[QSConstants.POSITION_OPCODE] = oldOpcode;
    }
}
