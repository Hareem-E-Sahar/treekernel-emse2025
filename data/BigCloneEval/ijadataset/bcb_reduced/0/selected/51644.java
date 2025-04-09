package src.backend;

import java.io.*;
import java.util.zip.CRC32;
import java.util.ArrayList;
import java.lang.Character;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import src.gui.*;
import src.backend.*;
import src.backend.file.AbstractWriter;
import src.backend.file.MarathonFileTool;

public class MapWriter extends AbstractWriter implements Serializable {

    private RandomAccessFile rf;

    String err = "File IO error.";

    private boolean isMacBinary = false;

    private boolean isValidMapFile = false;

    private int extraOffset = 0;

    private String inFileString;

    private MacBinaryHeader macBinaryHeader;

    private byte[] resourceFork;

    private long bytesChecked = 0;

    private long bytesToCheck = 0;

    public MapWriter(String inFileString) {
        super();
        try {
            this.inFileString = inFileString;
            File inFile = new File(inFileString);
            this.rf = new RandomAccessFile(inFile, "rw");
            this.isMacBinary = MarathonFileTool.macBinTest(rf);
            if (isMacBinary) {
                setupMacBinary();
            }
            this.isValidMapFile = verifyValidMapFile();
        } catch (FileNotFoundException fnfEx) {
            System.out.println("File not found.");
        } catch (IOException e) {
            System.out.println(err);
        }
    }

    public void read(byte[] data) {
        try {
            this.rf.read(data);
        } catch (IOException e) {
            pErr(e);
        }
    }

    public void write(byte[] data) {
        try {
            this.rf.write(data);
        } catch (IOException e) {
            pErr(e);
        }
    }

    public void close() {
        try {
            this.rf.close();
        } catch (Exception e) {
            pErr(e);
        }
    }

    public void newRF(String path, MapData md) {
        int len = md.getMapLength() + 128;
        if (this.isMacBinary) {
            MacBinaryHeader mbh = this.macBinaryHeader;
            len += mbh.getDataLength() + mbh.getResourceLength() + 128;
        }
        try {
            this.rf.close();
        } catch (IOException e) {
            pErr(e);
        }
        try {
            this.rf = new RandomAccessFile(path, "rw");
            this.rf.setLength(len);
        } catch (IOException e) {
            pErr(e);
        }
    }

    public void setFileSize(MapData md) {
        int len = md.getMapLength() + 128;
        if (this.isMacBinary) {
            MacBinaryHeader mbh = this.macBinaryHeader;
            len += mbh.getDataLength() + mbh.getResourceLength() + 128;
        }
        try {
            this.rf.setLength(len);
        } catch (IOException e) {
            pErr(e);
        }
    }

    public long getBytesChecked() {
        return this.bytesChecked;
    }

    public void incrementBytesChecked() {
        this.bytesChecked++;
    }

    public void resetBytesChecked() {
        this.bytesChecked = 0;
    }

    public void setBytesToCheck(long bytes) {
        this.bytesToCheck = bytes;
    }

    public long getBytesToCheck() {
        return this.bytesToCheck;
    }

    public long generateChecksum(int len, int trailer_len) {
        long cksum = -1;
        try {
            int map_file_length = (len + trailer_len);
            setBytesToCheck(map_file_length);
            seek(0);
            byte[] mapData = new byte[map_file_length];
            this.rf.read(mapData);
            CRC32 crc32 = new CRC32();
            crc32.update(mapData);
            cksum = crc32.getValue();
            resetBytesChecked();
            setBytesToCheck(0);
        } catch (IOException e) {
            pErr(e);
        }
        return cksum;
    }

    public RandomAccessFile getReadFile() {
        return this.rf;
    }

    public boolean getFileLoaded() {
        return this.rf != null;
    }

    public void writeByte(byte b) {
        try {
            this.rf.writeByte(b);
        } catch (IOException e) {
            pErr(e);
        }
    }

    public void writeByte(char c) {
        try {
            this.rf.writeByte(c);
        } catch (IOException e) {
            pErr(e);
        }
    }

    public void writeByte(int i) {
        try {
            this.rf.writeByte((int) i);
        } catch (IOException e) {
            pErr(e);
        }
    }

    public void writeShort(short s) {
        try {
            this.rf.writeShort(s);
        } catch (IOException e) {
            pErr(e);
        }
    }

    public void writeInt(int i) {
        try {
            this.rf.writeInt(i);
        } catch (IOException e) {
            pErr(e);
        }
    }

    public void writeLong(long l) {
        try {
            this.rf.writeLong(l);
        } catch (IOException e) {
            pErr(e);
        }
    }

    public byte readByte() {
        byte b = -1;
        try {
            b = this.rf.readByte();
        } catch (IOException e) {
            pErr(e);
        }
        return b;
    }

    public short readShort() {
        short s = -1;
        try {
            s = this.rf.readShort();
        } catch (IOException e) {
            pErr(e);
        }
        return s;
    }

    public int readUnsignedShort() {
        int s = -1;
        try {
            s = this.rf.readUnsignedShort();
        } catch (IOException e) {
            pErr(e);
        }
        return s;
    }

    public int readInt() {
        int i = -1;
        try {
            i = this.rf.readInt();
        } catch (IOException e) {
            pErr(e);
        }
        return i;
    }

    public long readLong() {
        long l = -1;
        try {
            l = this.rf.readLong();
        } catch (IOException e) {
            pErr(e);
        }
        return l;
    }

    public void skipBytes(int i) {
        try {
            rf.skipBytes(i);
        } catch (IOException e) {
            pErr(e);
        }
    }

    public void seek(long l) {
        try {
            this.rf.seek(l + this.extraOffset);
        } catch (IOException e) {
            pErr(e);
        }
    }

    public long getFilePointer() {
        long point = -1;
        try {
            point = this.rf.getFilePointer() - this.extraOffset;
        } catch (IOException e) {
            pErr(e);
        }
        return point;
    }

    public long length() {
        long len = -1;
        try {
            len = this.rf.length();
        } catch (IOException e) {
            pErr(e);
        }
        return len;
    }

    public void setLength(long length) {
        try {
            this.rf.setLength(length);
        } catch (IOException e) {
            pErr(e);
        }
    }

    public void adjust(int len) {
        setLength(length() + len);
        if (this.isMacBinary) {
            MacBinaryHeader header = this.macBinaryHeader;
            header.setDataLength(header.getDataLength() + len);
        }
    }

    public static void pErr(Exception e) {
        System.out.println("IOException in MapWriter");
        e.printStackTrace();
    }

    protected void setupMacBinary() throws IOException {
        this.extraOffset = 128;
        System.out.println("MacBinary Detected.");
        this.macBinaryHeader = new MacBinaryHeader(this);
        this.resourceFork = new byte[this.macBinaryHeader.getResourceLength()];
        int resOffset = this.macBinaryHeader.getDataLength() + 127;
        resOffset &= ~(0x7f);
        seek(resOffset);
        read(this.resourceFork);
    }

    private boolean verifyValidMapFile() {
        seek(0);
        int firstShort = readShort();
        int secondShort = readShort();
        if ((firstShort != 0x0002 && firstShort != 0x0004) || (secondShort != 0x0001 && secondShort != 0x0002)) {
            System.out.println("The file specified is not a valid map file");
            return false;
        }
        return true;
    }

    public MacBinaryHeader getMacBinaryHeader() {
        return this.macBinaryHeader;
    }

    public boolean isMacBinary() {
        return this.isMacBinary;
    }

    public boolean isValidMapFile() {
        return this.isValidMapFile;
    }

    public void writeMacBinary() throws IOException {
        if (this.isMacBinary) {
            this.macBinaryHeader.write();
            seek(this.macBinaryHeader.getDataLength());
            int mod = this.macBinaryHeader.getDataLength() % 128;
            int pad = 0;
            if (mod != 0) {
                pad = 128 - mod;
                byte[] padding = new byte[pad];
                write(padding);
            }
            System.out.println("seeking to end of data: " + this.macBinaryHeader.getDataLength());
            write(this.resourceFork);
        }
    }

    public void zeroMap() {
        int fileLength = (int) this.length();
        byte[] zero = new byte[fileLength];
        seek(0 - extraOffset);
        write(zero);
    }
}
