package org.dreamspeak.lib.helper;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public final class CRC32Checker {

    private CRC32Checker() {
    }

    public static final int getCRC32(byte[] data, int length) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return (int) crc.getValue();
    }

    public static final boolean checkCRC32(ByteBuffer buffer, int crc32Position, int length) throws IndexOutOfBoundsException {
        int crc32 = buffer.getInt(crc32Position);
        buffer.putInt(crc32Position, 0x00);
        boolean checkOkay = getCRC32(buffer.array(), length) == crc32;
        buffer.putInt(crc32Position, crc32);
        return checkOkay;
    }
}
