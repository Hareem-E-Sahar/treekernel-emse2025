package org.catacombae.hfsexplorer.partitioning;

import org.catacombae.csjc.structelements.Dictionary;
import org.catacombae.hfsexplorer.Util;
import java.io.PrintStream;
import java.util.zip.CRC32;
import org.catacombae.csjc.StructElements;
import org.catacombae.csjc.structelements.ByteArrayField;
import org.catacombae.csjc.structelements.DictionaryBuilder;
import org.catacombae.csjc.structelements.IntegerField;

public class GPTHeader implements StructElements {

    public static final long GPT_SIGNATURE = 0x4546492050415254L;

    protected final byte[] signature = new byte[8];

    protected final byte[] revision = new byte[4];

    protected final byte[] headerSize = new byte[4];

    protected final byte[] crc32Checksum = new byte[4];

    protected final byte[] reserved1 = new byte[4];

    protected final byte[] primaryLBA = new byte[8];

    protected final byte[] backupLBA = new byte[8];

    protected final byte[] firstUsableLBA = new byte[8];

    protected final byte[] lastUsableLBA = new byte[8];

    protected final byte[] diskGUID = new byte[16];

    protected final byte[] partitionEntryLBA = new byte[8];

    protected final byte[] numberOfPartitionEntries = new byte[4];

    protected final byte[] sizeOfPartitionEntry = new byte[4];

    protected final byte[] partitionEntryArrayCRC32 = new byte[4];

    protected final byte[] reserved2 = new byte[420];

    protected int blockSize;

    private final CRC32 crc = new CRC32();

    public GPTHeader(byte[] data, int offset, int blockSize) {
        this.blockSize = blockSize;
        System.arraycopy(data, offset + 0, signature, 0, 8);
        System.arraycopy(data, offset + 8, revision, 0, 4);
        System.arraycopy(data, offset + 12, headerSize, 0, 4);
        System.arraycopy(data, offset + 16, crc32Checksum, 0, 4);
        System.arraycopy(data, offset + 20, reserved1, 0, 4);
        System.arraycopy(data, offset + 24, primaryLBA, 0, 8);
        System.arraycopy(data, offset + 32, backupLBA, 0, 8);
        System.arraycopy(data, offset + 40, firstUsableLBA, 0, 8);
        System.arraycopy(data, offset + 48, lastUsableLBA, 0, 8);
        System.arraycopy(data, offset + 56, diskGUID, 0, 16);
        System.arraycopy(data, offset + 72, partitionEntryLBA, 0, 8);
        System.arraycopy(data, offset + 80, numberOfPartitionEntries, 0, 4);
        System.arraycopy(data, offset + 84, sizeOfPartitionEntry, 0, 4);
        System.arraycopy(data, offset + 88, partitionEntryArrayCRC32, 0, 4);
        System.arraycopy(data, offset + 92, reserved2, 0, 420);
    }

    protected GPTHeader(int blockSize) {
        this.blockSize = blockSize;
        System.arraycopy(Util.toByteArrayBE(GPT_SIGNATURE), 0, signature, 0, 8);
        Util.zero(reserved1);
        Util.zero(reserved2);
    }

    public GPTHeader(GPTHeader source) {
        setFieldsInternal(source);
    }

    protected void setFieldsInternal(GPTHeader source) {
        this.blockSize = source.blockSize;
        System.arraycopy(source.signature, 0, signature, 0, signature.length);
        System.arraycopy(source.revision, 0, revision, 0, revision.length);
        System.arraycopy(source.headerSize, 0, headerSize, 0, headerSize.length);
        System.arraycopy(source.crc32Checksum, 0, crc32Checksum, 0, crc32Checksum.length);
        System.arraycopy(source.reserved1, 0, reserved1, 0, reserved1.length);
        System.arraycopy(source.primaryLBA, 0, primaryLBA, 0, primaryLBA.length);
        System.arraycopy(source.backupLBA, 0, backupLBA, 0, backupLBA.length);
        System.arraycopy(source.firstUsableLBA, 0, firstUsableLBA, 0, firstUsableLBA.length);
        System.arraycopy(source.lastUsableLBA, 0, lastUsableLBA, 0, lastUsableLBA.length);
        System.arraycopy(source.diskGUID, 0, diskGUID, 0, diskGUID.length);
        System.arraycopy(source.partitionEntryLBA, 0, partitionEntryLBA, 0, partitionEntryLBA.length);
        System.arraycopy(source.numberOfPartitionEntries, 0, numberOfPartitionEntries, 0, numberOfPartitionEntries.length);
        System.arraycopy(source.sizeOfPartitionEntry, 0, sizeOfPartitionEntry, 0, sizeOfPartitionEntry.length);
        System.arraycopy(source.partitionEntryArrayCRC32, 0, partitionEntryArrayCRC32, 0, partitionEntryArrayCRC32.length);
        System.arraycopy(source.reserved2, 0, reserved2, 0, reserved2.length);
    }

    public static int getSize() {
        return 512;
    }

    public long getSignature() {
        return Util.readLongBE(signature);
    }

    public int getRevision() {
        return Util.readIntLE(revision);
    }

    public int getHeaderSize() {
        return Util.readIntLE(headerSize);
    }

    public int getCRC32Checksum() {
        return Util.readIntLE(crc32Checksum);
    }

    public int getReserved1() {
        return Util.readIntBE(reserved1);
    }

    public long getPrimaryLBA() {
        return Util.readLongLE(primaryLBA);
    }

    public long getBackupLBA() {
        return Util.readLongLE(backupLBA);
    }

    public long getFirstUsableLBA() {
        return Util.readLongLE(firstUsableLBA);
    }

    public long getLastUsableLBA() {
        return Util.readLongLE(lastUsableLBA);
    }

    public byte[] getDiskGUID() {
        return Util.createCopy(diskGUID);
    }

    public long getPartitionEntryLBA() {
        return Util.readLongLE(partitionEntryLBA);
    }

    public int getNumberOfPartitionEntries() {
        return Util.readIntLE(numberOfPartitionEntries);
    }

    public int getSizeOfPartitionEntry() {
        return Util.readIntLE(sizeOfPartitionEntry);
    }

    public int getPartitionEntryArrayCRC32() {
        return Util.readIntLE(partitionEntryArrayCRC32);
    }

    public byte[] getReserved2() {
        return Util.createCopy(reserved2);
    }

    public boolean isValid() {
        return getSignature() == GPT_SIGNATURE && calculateCRC32() == getCRC32Checksum();
    }

    /** Calculates the CRC32 as it should appear in the crc32Checksum field in the header. */
    public int calculateCRC32() {
        crc.reset();
        crc.update(signature);
        crc.update(revision);
        crc.update(headerSize);
        crc.update(0);
        crc.update(0);
        crc.update(0);
        crc.update(0);
        crc.update(reserved1);
        crc.update(primaryLBA);
        crc.update(backupLBA);
        crc.update(firstUsableLBA);
        crc.update(lastUsableLBA);
        crc.update(diskGUID);
        crc.update(partitionEntryLBA);
        crc.update(numberOfPartitionEntries);
        crc.update(sizeOfPartitionEntry);
        crc.update(partitionEntryArrayCRC32);
        return (int) (crc.getValue() & 0xFFFFFFFF);
    }

    public void printFields(PrintStream ps, String prefix) {
        ps.println(prefix + " signature: 0x" + Util.toHexStringBE(getSignature()));
        ps.println(prefix + " revision: 0x" + Util.toHexStringBE(getRevision()));
        ps.println(prefix + " headerSize: " + getHeaderSize());
        ps.println(prefix + " crc32Checksum: 0x" + Util.toHexStringBE(getCRC32Checksum()));
        ps.println(prefix + " reserved1: 0x" + Util.toHexStringBE(getReserved1()));
        ps.println(prefix + " primaryLBA: " + getPrimaryLBA());
        ps.println(prefix + " backupLBA: " + getBackupLBA());
        ps.println(prefix + " firstUsableLBA: " + getFirstUsableLBA());
        ps.println(prefix + " lastUsableLBA: " + getLastUsableLBA());
        ps.println(prefix + " diskGUID: " + GPTEntry.getGUIDAsString(getDiskGUID()));
        ps.println(prefix + " partitionEntryLBA: " + getPartitionEntryLBA());
        ps.println(prefix + " sizeOfPartitionEntry: " + getSizeOfPartitionEntry());
        ps.println(prefix + " numberOfPartitionEntries: " + getNumberOfPartitionEntries());
        ps.println(prefix + " partitionEntryArrayCRC32: 0x" + Util.toHexStringBE(getPartitionEntryArrayCRC32()));
        ps.println(prefix + " reserved2: [too much data to display...]");
    }

    public void print(PrintStream ps, String prefix) {
        ps.println(prefix + "GPTHeader:");
        printFields(ps, prefix);
    }

    public byte[] getBytes() {
        byte[] result = new byte[512];
        int offset = 0;
        System.arraycopy(signature, 0, result, offset, signature.length);
        offset += 8;
        System.arraycopy(revision, 0, result, offset, revision.length);
        offset += 4;
        System.arraycopy(headerSize, 0, result, offset, headerSize.length);
        offset += 4;
        System.arraycopy(crc32Checksum, 0, result, offset, crc32Checksum.length);
        offset += 4;
        System.arraycopy(reserved1, 0, result, offset, reserved1.length);
        offset += 4;
        System.arraycopy(primaryLBA, 0, result, offset, primaryLBA.length);
        offset += 8;
        System.arraycopy(backupLBA, 0, result, offset, backupLBA.length);
        offset += 8;
        System.arraycopy(firstUsableLBA, 0, result, offset, firstUsableLBA.length);
        offset += 8;
        System.arraycopy(lastUsableLBA, 0, result, offset, lastUsableLBA.length);
        offset += 8;
        System.arraycopy(diskGUID, 0, result, offset, diskGUID.length);
        offset += 16;
        System.arraycopy(partitionEntryLBA, 0, result, offset, partitionEntryLBA.length);
        offset += 8;
        System.arraycopy(numberOfPartitionEntries, 0, result, offset, numberOfPartitionEntries.length);
        offset += 4;
        System.arraycopy(sizeOfPartitionEntry, 0, result, offset, sizeOfPartitionEntry.length);
        offset += 4;
        System.arraycopy(partitionEntryArrayCRC32, 0, result, offset, partitionEntryArrayCRC32.length);
        offset += 4;
        System.arraycopy(reserved2, 0, result, offset, reserved2.length);
        offset += 420;
        return result;
    }

    public boolean equals(Object obj) {
        if (obj instanceof GPTHeader) {
            GPTHeader gpth = (GPTHeader) obj;
            return Util.arraysEqual(getBytes(), gpth.getBytes());
        } else return false;
    }

    /**
     * Checks that if supplied GPTHeader is a valid backup header to this header (or the reverse,
     * as a primary header is always a valid backup to the backup header).<br>
     * Note that this method does not check the validity of the fields in the header, it just
     * checks if the fields that should be equal are equal. Use isValid() to check the validity of
     * the field data.
     *
     * @param backupHeader the backup header to check for validity againt this header.
     * @return true if the headers complement each other as backups, or false otherwise.
     */
    public boolean isValidBackup(GPTHeader backupHeader) {
        if (!Util.arraysEqual(this.signature, backupHeader.signature)) return false;
        if (!Util.arraysEqual(this.revision, backupHeader.revision)) return false;
        if (!Util.arraysEqual(this.headerSize, backupHeader.headerSize)) return false;
        if (Util.arraysEqual(this.crc32Checksum, backupHeader.crc32Checksum)) return false;
        if (!Util.arraysEqual(this.reserved1, backupHeader.reserved1)) return false;
        if (!Util.arraysEqual(this.primaryLBA, backupHeader.backupLBA)) return false;
        if (!Util.arraysEqual(this.backupLBA, backupHeader.primaryLBA)) return false;
        if (!Util.arraysEqual(this.firstUsableLBA, backupHeader.firstUsableLBA)) return false;
        if (!Util.arraysEqual(this.lastUsableLBA, backupHeader.lastUsableLBA)) return false;
        if (!Util.arraysEqual(this.diskGUID, backupHeader.diskGUID)) return false;
        if (Util.arraysEqual(this.partitionEntryLBA, backupHeader.partitionEntryLBA)) return false;
        if (!Util.arraysEqual(this.numberOfPartitionEntries, backupHeader.numberOfPartitionEntries)) return false;
        if (!Util.arraysEqual(this.sizeOfPartitionEntry, backupHeader.sizeOfPartitionEntry)) return false;
        if (!Util.arraysEqual(this.partitionEntryArrayCRC32, backupHeader.partitionEntryArrayCRC32)) return false;
        if (!Util.arraysEqual(this.reserved2, backupHeader.reserved2)) return false;
        return true;
    }

    public GPTHeader createValidBackupHeader() {
        GPTHeader newHeader = new GPTHeader(this);
        byte[] primaryLBA = Util.createCopy(newHeader.primaryLBA);
        byte[] backupLBA = Util.createCopy(newHeader.backupLBA);
        Util.arrayCopy(primaryLBA, newHeader.backupLBA);
        Util.arrayCopy(backupLBA, newHeader.primaryLBA);
        long peByteLen = (newHeader.getNumberOfPartitionEntries() * newHeader.getSizeOfPartitionEntry());
        long peLBALen = peByteLen / blockSize + ((peByteLen % blockSize != 0) ? 1 : 0);
        long pePos = newHeader.getPrimaryLBA() - peLBALen;
        byte[] pePosBytes = Util.toByteArrayLE(pePos);
        if (pePosBytes.length != newHeader.partitionEntryLBA.length) throw new RuntimeException("Assertion pePosBytes.length(" + pePosBytes.length + ") == newHeader.partitionEntryLBA.length(" + newHeader.partitionEntryLBA.length + ") failed.");
        System.arraycopy(pePosBytes, 0, newHeader.partitionEntryLBA, 0, newHeader.partitionEntryLBA.length);
        int crc = newHeader.calculateCRC32();
        byte[] crcBytes = Util.toByteArrayLE(crc);
        if (crcBytes.length != newHeader.crc32Checksum.length) throw new RuntimeException("Assertion crcBytes.length(" + crcBytes.length + ") == newHeader.crc32Checksum.length(" + newHeader.crc32Checksum.length + ") failed.");
        System.arraycopy(crcBytes, 0, newHeader.crc32Checksum, 0, newHeader.crc32Checksum.length);
        return newHeader;
    }

    public Dictionary getStructElements() {
        DictionaryBuilder dbStruct = new DictionaryBuilder(getClass().getSimpleName());
        dbStruct.add("signature", new IntegerField(signature, BITS_64, UNSIGNED, BIG_ENDIAN));
        dbStruct.add("revision", new IntegerField(revision, BITS_32, UNSIGNED, LITTLE_ENDIAN));
        dbStruct.add("headerSize", new IntegerField(headerSize, BITS_32, UNSIGNED, LITTLE_ENDIAN));
        dbStruct.add("crc32Checksum", new IntegerField(crc32Checksum, BITS_32, UNSIGNED, LITTLE_ENDIAN));
        dbStruct.add("reserved1", new IntegerField(reserved1, BITS_32, UNSIGNED, BIG_ENDIAN));
        dbStruct.add("primaryLBA", new IntegerField(primaryLBA, BITS_64, UNSIGNED, LITTLE_ENDIAN));
        dbStruct.add("backupLBA", new IntegerField(backupLBA, BITS_64, UNSIGNED, LITTLE_ENDIAN));
        dbStruct.add("firstUsableLBA", new IntegerField(firstUsableLBA, BITS_64, UNSIGNED, LITTLE_ENDIAN));
        dbStruct.add("lastUsableLBA", new IntegerField(lastUsableLBA, BITS_64, UNSIGNED, LITTLE_ENDIAN));
        dbStruct.add("diskGUID", new ByteArrayField(diskGUID));
        dbStruct.add("partitionEntryLBA", new IntegerField(partitionEntryLBA, BITS_64, UNSIGNED, LITTLE_ENDIAN));
        dbStruct.add("numberOfPartitionEntries", new IntegerField(numberOfPartitionEntries, BITS_32, UNSIGNED, LITTLE_ENDIAN));
        dbStruct.add("sizeOfPartitionEntry", new IntegerField(sizeOfPartitionEntry, BITS_32, UNSIGNED, LITTLE_ENDIAN));
        dbStruct.add("partitionEntryArrayCRC32", new IntegerField(partitionEntryArrayCRC32, BITS_32, UNSIGNED, LITTLE_ENDIAN));
        dbStruct.add("reserved2", new ByteArrayField(reserved2));
        return dbStruct.getResult();
    }
}
