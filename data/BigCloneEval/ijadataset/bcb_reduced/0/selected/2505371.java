package org.jsynthlib.synthdrivers.YamahaDX7s;

import org.jsynthlib.core.Device;
import org.jsynthlib.core.Patch;
import org.jsynthlib.synthdrivers.YamahaDX7.common.DX7FamilyDevice;
import org.jsynthlib.synthdrivers.YamahaDX7.common.DX7FamilyMicroTuningBankDriver;

public class YamahaDX7sMicroTuningBankDriver extends DX7FamilyMicroTuningBankDriver {

    public YamahaDX7sMicroTuningBankDriver(final Device device) {
        super(device, YamahaDX7sMicroTuningConstants.INIT_MICRO_TUNING, YamahaDX7sMicroTuningConstants.BANK_MICRO_TUNING_PATCH_NUMBERS, YamahaDX7sMicroTuningConstants.BANK_MICRO_TUNING_BANK_NUMBERS);
    }

    public void storePatch(Patch p, int bankNum, int patchNum) {
        if ((((DX7FamilyDevice) (getDevice())).getTipsMsgFlag() & 0x01) == 1) {
            YamahaDX7sStrings.dxShowInformation(toString(), YamahaDX7sStrings.MICRO_TUNING_CARTRIDGE_STRING);
        }
        if ((((DX7FamilyDevice) (getDevice())).getSwOffMemProtFlag() & 0x01) == 1) {
            YamahaDX7sSysexHelpers.swOffMemProt(this, (byte) (getChannel() + 0x10), (byte) 0);
        } else {
            if ((((DX7FamilyDevice) (getDevice())).getTipsMsgFlag() & 0x01) == 1) {
                YamahaDX7sStrings.dxShowInformation(toString(), YamahaDX7sStrings.MEMORY_PROTECTION_STRING);
            }
        }
        sendPatchWorker(p);
    }

    public void requestPatchDump(int bankNum, int patchNum) {
        if ((((DX7FamilyDevice) (getDevice())).getTipsMsgFlag() & 0x01) == 1) {
            YamahaDX7sStrings.dxShowInformation(toString(), YamahaDX7sStrings.MICRO_TUNING_CARTRIDGE_STRING);
        }
        send(sysexRequestDump.toSysexMessage(getChannel() + 0x20));
    }
}
