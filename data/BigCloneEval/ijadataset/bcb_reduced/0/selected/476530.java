package org.jsynthlib.synthdrivers.YamahaDX7II;

import org.jsynthlib.core.Device;
import org.jsynthlib.core.Patch;
import org.jsynthlib.synthdrivers.YamahaDX7.common.DX7FamilyDevice;
import org.jsynthlib.synthdrivers.YamahaDX7.common.DX7FamilyVoiceBankDriver;

public class YamahaDX7IIVoiceBankDriver extends DX7FamilyVoiceBankDriver {

    public YamahaDX7IIVoiceBankDriver(final Device device) {
        super(device, YamahaDX7IIVoiceConstants.INIT_VOICE, YamahaDX7IIVoiceConstants.BANK_VOICE_PATCH_NUMBERS, YamahaDX7IIVoiceConstants.BANK_VOICE_BANK_NUMBERS);
    }

    public void storePatch(Patch p, int bankNum, int patchNum) {
        if ((((DX7FamilyDevice) (getDevice())).getSwOffMemProtFlag() & 0x01) == 1) {
            YamahaDX7IISysexHelpers.swOffMemProt(this, (byte) (getChannel() + 0x10), (byte) (0));
        } else {
            if ((((DX7FamilyDevice) (getDevice())).getTipsMsgFlag() & 0x01) == 1) {
                YamahaDX7IIStrings.dxShowInformation(toString(), YamahaDX7IIStrings.MEMORY_PROTECTION_STRING);
            }
        }
        YamahaDX7IISysexHelpers.chRcvBlock(this, (byte) (getChannel() + 0x10), (byte) (bankNum));
        sendPatchWorker(p);
    }

    ;

    public void requestPatchDump(int bankNum, int patchNum) {
        YamahaDX7IISysexHelpers.chXmitBlock(this, (byte) (getChannel() + 0x10), (byte) (bankNum));
        send(sysexRequestDump.toSysexMessage(getChannel() + 0x20));
    }
}
