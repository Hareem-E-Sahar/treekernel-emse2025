package ssg.tools.common.structuredFile.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;
import ssg.tools.common.structuredFile.CompositeNamedElement;
import ssg.tools.common.structuredFile.NamedElement;
import ssg.tools.common.structuredFile.NamedElement.Category.ArchiveFile;
import ssg.tools.common.structuredFile.NamedElement.Category.ArchiveMetadata;
import ssg.tools.common.structuredFile.NamedElement.Category.BrokenArea;
import ssg.tools.common.structuredFile.NamedElement.Category.FileInArchive;

/**
 * Re-make of RAR reader to follow elements-based approach.
 * 
 * @author ssg
 */
public class RAR extends CompositeNamedElement {

    public static enum HEAD_TYPE {

        MARK, MAIN, FILE, COMM, AV, SUB, PROTECT, COMMENT, EOF
    }

    ;

    public static Map<HEAD_TYPE, Byte> head2code = new HashMap<HEAD_TYPE, Byte>() {

        {
            put(HEAD_TYPE.MARK, (byte) 0x72);
            put(HEAD_TYPE.MAIN, (byte) 0x73);
            put(HEAD_TYPE.FILE, (byte) 0x74);
            put(HEAD_TYPE.COMM, (byte) 0x75);
            put(HEAD_TYPE.AV, (byte) 0x76);
            put(HEAD_TYPE.SUB, (byte) 0x77);
            put(HEAD_TYPE.PROTECT, (byte) 0x78);
            put(HEAD_TYPE.COMMENT, (byte) 0x7a);
            put(HEAD_TYPE.EOF, (byte) 0x7b);
        }
    };

    public static Map<Byte, HEAD_TYPE> code2head = new HashMap<Byte, HEAD_TYPE>() {

        {
            for (HEAD_TYPE ht : head2code.keySet()) {
                Byte htc = head2code.get(ht);
                put(htc, ht);
            }
        }
    };

    public static byte HEADER_FILE = head2code.get(HEAD_TYPE.FILE);

    public static byte HEADER_EOF = head2code.get(HEAD_TYPE.EOF);

    long length;

    public RAR(String name, InputStream is) throws IOException {
        super(name, null, 0, 0);
        int offset = getBaseOffset();
        try {
            offset += addChild(new RARMark(is));
            for (int i = 1; i > 0; i++) {
                RARHeader rh = null;
                try {
                    rh = new RARHeader(is);
                    if (rh != null && rh.getType() == HEADER_FILE) {
                        rh = new RARFile(rh.getData(), offset);
                    }
                } catch (RARCRCException crcex) {
                    long brokenLength = 0;
                    while (rh == null) {
                        try {
                            is.skip(1);
                            brokenLength++;
                            rh = new RARHeader(is);
                        } catch (RARCRCException crcex2) {
                        }
                    }
                    if (rh != null && rh.getType() == HEADER_FILE) {
                        offset += addChild(new RARBrokenArea("broken area at " + offset, offset, brokenLength));
                        rh = new RARFile(rh.getData(), offset + brokenLength);
                    }
                }
                if (rh.getType() == HEADER_FILE) {
                    rh.setName(rh.getName() + "@" + offset);
                    rh.addChild(new EAOfflineData("File data", is, ((RARFile) rh).getPackedSize()));
                } else {
                    rh.setName(rh.getName() + "@" + offset);
                }
                offset += addChild(rh);
                if (rh.getType() == HEADER_EOF) {
                    break;
                }
            }
        } catch (IOException ioex) {
        }
        this.length = getCompositeSize();
        setCategory(new ArchiveFile());
    }

    @Override
    public String getValue() {
        return getName();
    }

    @Override
    public long getSize() {
        return length;
    }

    public static class RARHeader extends CompositeNamedElement {

        long length;

        /**
         * Override to ensure file type is valid by comparing with getType() value.
         * 
         * @return
         */
        public byte getHeaderType() {
            return -1;
        }

        /**
         * Basic header loading constructor
         * @param is
         * @throws IOException
         */
        public RARHeader(InputStream is) throws IOException {
            super("RH", null, 0, 0);
            if (is.markSupported()) {
                is.mark(1024);
            }
            try {
                {
                    byte[] d = new byte[7];
                    int size = is.read(d);
                    if (size == 0) {
                        throw new IOException("Reached undetected EOF.");
                    }
                    setData(d);
                }
                if (getHeaderSize() > getData().length) {
                    byte[] d = new byte[getHeaderSize()];
                    is.read(d, getData().length, (int) (d.length - getData().length));
                    System.arraycopy(getData(), 0, d, 0, getData().length);
                    setData(d);
                }
                if (getCRC() != calculateCRC()) {
                    throw new RARCRCException("Invalid RAR header: CRC error.");
                }
            } catch (IOException ioex) {
                if (is.markSupported()) {
                    try {
                        is.reset();
                    } catch (Throwable th) {
                    }
                }
                throw ioex;
            }
            initHeaderCommons();
        }

        /**
         * Evaluated header type reloading constructor: copies provided header
         * to desired class type instance.
         *
         * @param data
         * @throws IOException
         */
        public RARHeader(byte[] data) throws IOException {
            super("RH", data, 0, data.length);
            if (getCRC() != calculateCRC()) {
                throw new RARCRCException("Invalid RAR header: CRC error.");
            }
            initHeaderCommons();
        }

        private void initHeaderCommons() throws IOException {
            int offset = getBaseOffset();
            offset += addChild(new NamedElement.EAShort("HeaderCRC", getData(), offset));
            offset += addChild(new NamedElement.EAByte("HeaderType", getData(), offset));
            offset += addChild(new NamedElement.EAShort("HeaderFlags", getData(), offset));
            offset += addChild(new NamedElement.EAShort("HeaderSize", getData(), offset));
            length = getCompositeSize();
            setCategory(new ArchiveMetadata());
            HEAD_TYPE ht = code2head.get(getType());
            if (ht != null) {
                setName("RAR." + ht.toString());
            }
        }

        public int getCRC() {
            return getShort(0);
        }

        public byte getType() {
            return getByte(2);
        }

        public int getFlags() {
            return getShort(3);
        }

        public int getHeaderSize() {
            return getShort(5);
        }

        public int calculateCRC() {
            CRC32 crc = new CRC32();
            crc.reset();
            crc.update(getData(), 2, getData().length - 2);
            return (short) (crc.getValue());
        }

        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public long getSize() {
            return length;
        }

        @Override
        public boolean isValid() {
            return getCRC() == calculateCRC() && getType() == getHeaderType() && getHeaderType() != -1;
        }
    }

    public static class RARMark extends RARHeader {

        private static final byte[][] marks = new byte[][] { { 0x52, 0x45, 0x7e, 0x5e }, { 0x52, 0x61, 0x72, 0x21, 0x1a, 0x07, 0x00 }, { 0x55, 0x6e, 0x69, 0x71, 0x75, 0x45, 0x21 } };

        private String value;

        /**
         * Provides type always match. Actual checks are done in  calculateCRC only!
         * 
         * @return
         */
        @Override
        public byte getHeaderType() {
            return getType();
        }

        public RARMark(InputStream is) throws IOException {
            super(is);
            initMark();
        }

        public RARMark(byte[] data) throws IOException {
            super(data);
            initMark();
        }

        private void initMark() {
            getChildren().clear();
            length = getData().length;
            setName("RAR.Mark");
            setCategory(new ArchiveMetadata());
        }

        /**
         * Mark header does not match header structure and does not have CRC for it.
         * If header bytes match 1 of known signatures - return calculated CRC same as getCRC()
         * thus allowing header validation.
         *
         * @return
         */
        @Override
        public int calculateCRC() {
            boolean knownMark = false;
            for (int i = 0; i < marks.length; i++) {
                boolean match = true;
                for (int j = 0; j < Math.min(getData().length, marks[i].length); j++) {
                    if (getByte(j) != marks[i][j]) {
                        match = false;
                    }
                }
                if (match) {
                    knownMark = true;
                    if (getValue() == null) value = getString(0, marks[i].length, Charset.defaultCharset());
                    break;
                }
            }
            if (knownMark) {
                return (short) getCRC();
            } else {
                return (short) (getCRC() - 1);
            }
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    /**
    File header:
     * 7   4    PackSize
     * 11  4    UnpSize
     * 15  1    HostOS
     * 16  4    FileCRC
     * 20  4    FileTime
     * 24  1    UnpVer
     * 25  1    Method
     * 26  2    NameSize
     * 28  4    FileAttr
     */
    public static class RARFile extends RARHeader {

        long fileOffset;

        @Override
        public byte getHeaderType() {
            return HEADER_FILE;
        }

        public RARFile(InputStream is, long fileOffset) throws IOException {
            super(is);
            this.fileOffset = fileOffset;
            initFields();
        }

        public RARFile(byte[] data, long fileOffset) throws IOException {
            super(data);
            this.fileOffset = fileOffset;
            initFields();
        }

        private void initFields() throws IOException {
            int offset = 7;
            offset += addChild(new NamedElement.EAInteger("PackSize", getData(), offset));
            offset += addChild(new NamedElement.EAInteger("UnpSize", getData(), offset));
            offset += addChild(new NamedElement.EAByte("HostOS", getData(), offset));
            offset += addChild(new NamedElement.EAInteger("FileCRC", getData(), offset));
            offset += addChild(new NamedElement.EADate4("FileTime", getData(), offset));
            offset += addChild(new NamedElement.EAByte("UnpVer", getData(), offset));
            offset += addChild(new NamedElement.EAByte("Method", getData(), offset));
            offset += addChild(new NamedElement.EAShort("NameSize", getData(), offset));
            offset += addChild(new NamedElement.EAInteger("FileAttr", getData(), offset));
            offset += addChild(new NamedElement.EAString("FileName", getData(), offset, ((EAShort) getChild("NameSize")).getValue()));
            length = getCompositeSize();
            length += getPackedSize();
            setCategory(new FileInArchive());
        }

        @Override
        public String getValue() {
            return getFileName();
        }

        public int getPackedSize() {
            return getInt(7);
        }

        public int getOriginalSize() {
            return getInt(11);
        }

        public byte getHostOS() {
            return getByte(15);
        }

        public int getFileCRC() {
            return getInt(16);
        }

        public int getFileTime() {
            return getInt(20);
        }

        public byte getUnpackVersion() {
            return getByte(24);
        }

        public byte getMethod() {
            return getByte(25);
        }

        public int getNameSize() {
            return getShort(26);
        }

        public int getFileAttributes() {
            return getInt(28);
        }

        public String getFileName() {
            byte[] s = new byte[getNameSize()];
            for (int i = 0; i < s.length; i++) {
                s[i] = getByte(32 + i);
            }
            return new String(s);
        }
    }

    public static class RARBrokenArea extends EAOfflineData {

        long areaOffset;

        public RARBrokenArea(String name, long offset, long size) throws IOException {
            super(name, (byte[]) null, size);
            areaOffset = offset;
            setCategory(new BrokenArea());
        }

        public long getOffset() {
            return areaOffset;
        }
    }

    /**
     * Used to separate RAR CRC problem from other errors.
     */
    public static class RARCRCException extends IOException {

        public RARCRCException(String message) {
            super(message);
        }
    }
}
