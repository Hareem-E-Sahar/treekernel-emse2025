package com.googlecode.jsendnsca.core;

import java.util.zip.CRC32;
import com.googlecode.jsendnsca.core.utils.ByteArrayUtils;

class PassiveCheckBytesBuilder {

    private static final short NSCA_VERSION = 3;

    private static final int PLUGIN_OUTPUT_SIZE = 512;

    private static final int HOST_NAME_SIZE = 64;

    private static final int SERVICE_NAME_SIZE = 128;

    private byte[] bytes;

    private int currentOffset = 0;

    public PassiveCheckBytesBuilder() {
        bytes = new byte[16 + HOST_NAME_SIZE + SERVICE_NAME_SIZE + PLUGIN_OUTPUT_SIZE];
        ByteArrayUtils.writeShort(bytes, NSCA_VERSION, currentOffset);
        currentOffset += 8;
    }

    public PassiveCheckBytesBuilder withLevel(int value) {
        ByteArrayUtils.writeShort(bytes, (short) value, currentOffset);
        currentOffset += 2;
        return this;
    }

    public PassiveCheckBytesBuilder withTimeStamp(int value) {
        ByteArrayUtils.writeInteger(bytes, value, currentOffset);
        currentOffset += 4;
        return this;
    }

    public PassiveCheckBytesBuilder withHostname(String hostname) {
        writeFixedString(hostname, HOST_NAME_SIZE - 1);
        skipBytes(1);
        return this;
    }

    public PassiveCheckBytesBuilder withServiceName(String serviceName) {
        writeFixedString(serviceName, SERVICE_NAME_SIZE - 1);
        skipBytes(1);
        return this;
    }

    public PassiveCheckBytesBuilder withMessage(String message) {
        writeFixedString(message, PLUGIN_OUTPUT_SIZE - 1);
        skipBytes(1);
        return this;
    }

    private void writeFixedString(String value, int fixedSize) {
        ByteArrayUtils.writeFixedString(bytes, value, currentOffset, fixedSize);
        currentOffset += fixedSize;
    }

    public PassiveCheckBytesBuilder skipBytes(int numberToSkip) {
        currentOffset += numberToSkip;
        return this;
    }

    public PassiveCheckBytesBuilder writeCRC() {
        final CRC32 crc = new CRC32();
        crc.update(bytes);
        ByteArrayUtils.writeInteger(bytes, (int) crc.getValue(), 4);
        return this;
    }

    public byte[] toByteArray() {
        return bytes;
    }

    public PassiveCheckBytesBuilder encrypt(byte[] initVector, NagiosSettings nagiosSettings) {
        Encryption.getEncryptor(nagiosSettings.getEncryptionMethod()).encrypt(bytes, initVector, nagiosSettings.getPassword());
        return this;
    }
}
