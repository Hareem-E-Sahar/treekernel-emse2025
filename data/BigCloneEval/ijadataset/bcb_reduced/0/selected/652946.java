package synthdrivers.RolandXV5080;

import core.Driver;
import core.Patch;
import core.SysexHandler;

public class RolandXV5080PatchDriver extends Driver {

    static final int[] PATCH_SYSEX_START = new int[] { 0, 91, 248, 312, 407, 460, 609, 758, 907 };

    static final int[] PATCH_SYSEX_SIZE = new int[] { 91, 157, 64, 95, 53, 149, 149, 149, 149 };

    static final int PATCH_SIZE = 1056;

    static final int PATCH_NUMBER_OFFSET = 7;

    static final int PATCH_NAME_START = 10;

    static final int PATCH_NAME_SIZE = 12;

    static final int CHECKSUM_START = 6;

    static final String[] BANK_NUMBERS = new String[] { "User" };

    static final String[] PATCH_NUMBERS = new String[] { "01-", "02-", "03-", "04-", "05-", "06-", "07-", "08-", "09-", "10-", "11-", "12-", "13-", "14-", "15-", "16-", "17-", "18-", "19-", "20-", "21-", "22-", "23-", "24-", "25-", "26-", "27-", "28-", "29-", "30-", "31-", "32-", "33-", "34-", "35-", "36-", "37-", "38-", "39-", "40-", "41-", "42-", "43-", "44-", "45-", "46-", "47-", "48-", "49-", "50-", "51-", "52-", "53-", "54-", "55-", "56-", "57-", "58-", "59-", "60-", "61-", "62-", "63-", "64-", "65-", "66-", "67-", "68-", "69-", "70-", "71-", "72-", "73-", "74-", "75-", "76-", "77-", "78-", "79-", "80-", "81-", "82-", "83-", "84-", "85-", "86-", "87-", "88-", "89-", "90-", "91-", "92-", "93-", "94-", "95-", "96-", "97-", "98-", "99-", "100-", "101-", "102-", "103-", "104-", "105-", "106-", "107-", "108-", "109-", "110-", "111-", "112-", "113-", "114-", "115-", "116-", "117-", "118-", "119-", "120-", "121-", "122-", "123-", "124-", "125-", "126-", "127-", "128-" };

    static final SysexHandler SYSEX_REQUEST_DUMP = new SysexHandler("F0 41 10 00 10 11 30 *patchNum* 00 00 00 01 00 00 00 F7");

    public RolandXV5080PatchDriver() {
        super("Patch", "Phil Shepherd");
        sysexID = "F0411000101230**0000";
        patchSize = PATCH_SIZE;
        patchNameStart = PATCH_NAME_START;
        patchNameSize = PATCH_NAME_SIZE;
        deviceIDoffset = 0;
        bankNumbers = BANK_NUMBERS;
        patchNumbers = PATCH_NUMBERS;
    }

    public void storePatch(Patch p, int bankNum, int patchNum) {
        updatePatchNum((Patch) p, patchNum);
        sendPatchWorker(p);
        try {
            Thread.sleep(300);
        } catch (Exception e) {
        }
        setBankNum(bankNum);
        setPatchNum(patchNum);
    }

    public void sendPatch(Patch p) {
        storePatch(p, 0, 0);
    }

    public void setBankNum(int bankNum) {
        try {
            send(0xB0 + (getChannel() - 1), 0x00, 87);
            send(0xB0 + (getChannel() - 1), 0x20, 0);
        } catch (Exception e) {
        }
        ;
    }

    public void updatePatchNum(Patch p, int patchNum) {
        for (int i = 0; i < PATCH_SYSEX_START.length; i++) p.sysex[PATCH_SYSEX_START[i] + PATCH_NUMBER_OFFSET] = (byte) (patchNum);
    }

    public void calculateChecksum(Patch p) {
        for (int i = 0; i < PATCH_SYSEX_START.length; i++) {
            int checksumStart = PATCH_SYSEX_START[i] + CHECKSUM_START;
            int checksumEnd = PATCH_SYSEX_START[i] + PATCH_SYSEX_SIZE[i] - 3;
            int checksumOffset = checksumEnd + 1;
            calculateChecksum(((Patch) p).sysex, checksumStart, checksumEnd, checksumOffset);
        }
    }

    public void requestPatchDump(int bankNum, int patchNum) {
        byte[] sysex = SYSEX_REQUEST_DUMP.toByteArray(getChannel(), patchNum);
        calculateChecksum(sysex, 6, sysex.length - 3, sysex.length - 2);
        send(sysex);
    }

    public static void calculateChecksum(byte[] sysex, int start, int end, int ofs) {
        int i;
        int sum = 0;
        for (i = start; i <= end; i++) sum += sysex[i];
        int remainder = sum % 128;
        sysex[ofs] = (byte) (128 - remainder);
    }
}
