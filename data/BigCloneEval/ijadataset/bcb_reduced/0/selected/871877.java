package socksviahttp.core.net;

import java.io.*;
import socksviahttp.core.util.*;

public class DataPacket implements java.io.Serializable {

    public int type = socksviahttp.core.consts.Const.CONNECTION_UNSPECIFIED_TYPE;

    public String id = "";

    public byte[] tab = socksviahttp.core.consts.Const.TAB_EMPTY;

    public boolean isConnClosed = false;

    public boolean zipData = false;

    public boolean encryptData = false;

    public byte[] encryptionKey = null;

    public int errorCode = 0;

    public byte[] saveToByteArray() {
        boolean isThisPacketZipped = false;
        boolean isThisPacketEncrypted = false;
        byte[] workTab = tab;
        if (zipData) {
            try {
                byte[] zipTab = ByteUtils.packRaw(workTab);
                if (zipTab.length < workTab.length) {
                    workTab = zipTab;
                    isThisPacketZipped = true;
                } else isThisPacketZipped = false;
            } catch (IOException ioe) {
                isThisPacketZipped = false;
            }
        }
        if (encryptData) {
            try {
                workTab = ByteUtils.encryptRaw(encryptionKey, workTab);
                isThisPacketEncrypted = true;
            } catch (IOException e) {
                isThisPacketEncrypted = false;
            }
        }
        byte[] extensions = new byte[0];
        byte[] array = new byte[1 + 1 + 1 + id.length() + 2 + workTab.length + 8 + 2 + extensions.length + 1];
        array[0] = ByteUtils.i2b(type);
        array[1] = ByteUtils.byteFromBooleans(false, false, false, isThisPacketEncrypted, isThisPacketZipped, encryptData, zipData, isConnClosed);
        array[2] = ByteUtils.i2b(id.length());
        byte[] tabId = id.getBytes();
        System.arraycopy(tabId, 0, array, 3, tabId.length);
        array[tabId.length + 3] = ByteUtils.i2b(workTab.length / 256);
        array[tabId.length + 4] = ByteUtils.i2b(workTab.length % 256);
        System.arraycopy(workTab, 0, array, tabId.length + 5, workTab.length);
        java.util.zip.CRC32 crcComputer = new java.util.zip.CRC32();
        crcComputer.update(tab);
        long crc = crcComputer.getValue();
        byte[] bCrc = ByteUtils.bytesFromLong(crc);
        System.arraycopy(bCrc, 0, array, tabId.length + workTab.length + 5, 8);
        array[tabId.length + workTab.length + 13] = ByteUtils.i2b(extensions.length / 256);
        array[tabId.length + workTab.length + 14] = ByteUtils.i2b(extensions.length % 256);
        System.arraycopy(extensions, 0, array, tabId.length + workTab.length + 15, extensions.length);
        array[tabId.length + workTab.length + extensions.length + 15] = 0;
        return (array);
    }

    public int loadFromByteArray(byte[] array) {
        type = ByteUtils.b2i(array[0]);
        boolean[] flags = ByteUtils.booleansFromByte(array[1]);
        isConnClosed = flags[0];
        zipData = flags[1];
        encryptData = flags[2];
        boolean isThisPacketZipped = flags[3];
        boolean isThisPacketEncrypted = flags[4];
        int idLen = ByteUtils.b2i(array[2]);
        id = new String(array, 3, idLen);
        int dataLen = 256 * ByteUtils.b2i(array[3 + idLen]) + ByteUtils.b2i(array[4 + idLen]);
        byte[] workTab = new byte[dataLen];
        System.arraycopy(array, 5 + idLen, workTab, 0, dataLen);
        long crc = ByteUtils.longFromBytes(array[12 + idLen + dataLen], array[11 + idLen + dataLen], array[10 + idLen + dataLen], array[9 + idLen + dataLen], array[8 + idLen + dataLen], array[7 + idLen + dataLen], array[6 + idLen + dataLen], array[5 + idLen + dataLen]);
        int extLen = 256 * ByteUtils.b2i(array[13 + idLen + dataLen]) + ByteUtils.b2i(array[14 + idLen + dataLen]);
        byte[] extensions = new byte[extLen];
        System.arraycopy(array, 15 + idLen + dataLen, extensions, 0, extLen);
        byte nullByte = array[15 + idLen + dataLen + extLen];
        if (nullByte != 0) {
        }
        if (isThisPacketEncrypted) {
            try {
                workTab = ByteUtils.decryptRaw(encryptionKey, workTab);
            } catch (IOException e) {
                tab = workTab;
                errorCode = 3;
                return (3);
            }
        }
        if (isThisPacketZipped) {
            try {
                workTab = ByteUtils.unpackRaw(workTab);
            } catch (IOException ioe) {
                tab = workTab;
                errorCode = 2;
                return (2);
            }
        }
        tab = workTab;
        java.util.zip.CRC32 crcComputer = new java.util.zip.CRC32();
        crcComputer.update(tab);
        long computedCrc = crcComputer.getValue();
        if (computedCrc != crc) {
            errorCode = 1;
            return (1);
        }
        errorCode = 0;
        return (0);
    }
}
