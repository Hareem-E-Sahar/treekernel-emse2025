public class Test {    public JSLFrame editPatch(Patch p) {
        if ((((DX7FamilyDevice) (getDevice())).getSwOffMemProtFlag() & 0x01) == 1) {
            YamahaDX7SysexHelper.swOffMemProt(this, (byte) (getChannel() + 0x10), (byte) (0x21), (byte) (0x25));
        } else {
            if ((((DX7FamilyDevice) (getDevice())).getTipsMsgFlag() & 0x01) == 1) YamahaDX7Strings.dxShowInformation(toString(), YamahaDX7Strings.MEMORY_PROTECTION_STRING);
        }
        if ((((DX7FamilyDevice) (getDevice())).getSPBPflag() & 0x01) == 1) {
            YamahaDX7SysexHelper.mkSysInfoAvail(this, (byte) (getChannel() + 0x10));
        } else {
            if ((((DX7FamilyDevice) (getDevice())).getTipsMsgFlag() & 0x01) == 1) YamahaDX7Strings.dxShowInformation(toString(), YamahaDX7Strings.RECEIVE_STRING);
        }
        return super.editPatch(p);
    }
}