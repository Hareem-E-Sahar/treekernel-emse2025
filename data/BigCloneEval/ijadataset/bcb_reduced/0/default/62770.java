import java.io.*;

public class RandomAccessFileTest {

    public static void runReadTest(String filename, int seq, String testname) {
        try {
            System.out.println("Test " + seq + ": " + testname);
            RandomAccessFile ras = new RandomAccessFile(filename, "r");
            boolean passed = true;
            boolean b = ras.readBoolean();
            if (b != true) {
                passed = false;
                System.out.println("Failed to read boolean. Expected true and got false");
            }
            b = ras.readBoolean();
            if (b != false) {
                passed = false;
                System.out.println("Failed to read boolean. Expected false and got true");
            }
            byte bt = ras.readByte();
            if (bt != 8) {
                passed = false;
                System.out.println("Failed to read byte. Expected 8 and got " + bt);
            }
            bt = ras.readByte();
            if (bt != -122) {
                passed = false;
                System.out.println("Failed to read byte. Expected -122 and got " + bt);
            }
            char c = ras.readChar();
            if (c != 'a') {
                passed = false;
                System.out.println("Failed to read char. Expected a and got " + c);
            }
            c = ras.readChar();
            if (c != '') {
                passed = false;
                System.out.println("Failed to read char. Expected \\uE2D2 and got " + c);
            }
            short s = ras.readShort();
            if (s != 32000) {
                passed = false;
                System.out.println("Failed to read short. Expected 32000 and got " + s);
            }
            int i = ras.readInt();
            if (i != 8675309) {
                passed = false;
                System.out.println("Failed to read int. Expected 8675309 and got " + i);
            }
            long l = ras.readLong();
            if (l != 696969696969L) {
                passed = false;
                System.out.println("Failed to read long. Expected 696969696969 and got " + l);
            }
            float f = ras.readFloat();
            if (!Float.toString(f).equals("3.1415")) {
                passed = false;
                System.out.println("Failed to read float. Expected 3.1415 and got " + f);
            }
            double d = ras.readDouble();
            if (d != 999999999.999) {
                passed = false;
                System.out.println("Failed to read double.  Expected 999999999.999 and got " + d);
            }
            String str = ras.readUTF();
            if (!str.equals("Testing code is such a boring activity but it must be done")) {
                passed = false;
                System.out.println("Read unexpected String: " + str);
            }
            str = ras.readUTF();
            if (!str.equals("a-->ǿꀀ晦ȀRRR")) {
                passed = false;
                System.out.println("Read unexpected String: " + str);
            }
            if (passed) System.out.println("PASSED: " + testname + " read test"); else System.out.println("FAILED: " + testname + " read test");
        } catch (IOException e) {
            System.out.println("FAILED: " + testname + " read test: " + e);
        }
    }

    public static void main(String[] argv) {
        System.out.println("Started test of RandomAccessFile");
        System.out.println("Test 1: RandomAccessFile write test");
        try {
            RandomAccessFile raf = new RandomAccessFile("dataoutput.out", "rw");
            raf.writeBoolean(true);
            raf.writeBoolean(false);
            raf.writeByte((byte) 8);
            raf.writeByte((byte) -122);
            raf.writeChar((char) 'a');
            raf.writeChar((char) '');
            raf.writeShort((short) 32000);
            raf.writeInt((int) 8675309);
            raf.writeLong((long) 696969696969L);
            raf.writeFloat((float) 3.1415);
            raf.writeDouble((double) 999999999.999);
            raf.writeUTF("Testing code is such a boring activity but it must be done");
            raf.writeUTF("a-->ǿꀀ晦ȀRRR");
            raf.close();
            System.out.println("PASSED: RandomAccessFile write test");
        } catch (IOException e) {
            System.out.println("FAILED: RandomAccessFile write test: " + e);
        }
        runReadTest("dataoutput.out", 2, "Read of JCL written data file");
        runReadTest("dataoutput-jdk.out", 3, "Read of JDK written data file");
        System.out.println("Test 2: Seek Test");
        try {
            RandomAccessFile raf = new RandomAccessFile("/etc/services", "r");
            System.out.println("Length: " + raf.length());
            raf.skipBytes(24);
            if (raf.getFilePointer() != 24) throw new IOException("Unexpected file pointer value " + raf.getFilePointer());
            raf.seek(0);
            if (raf.getFilePointer() != 0) throw new IOException("Unexpected file pointer value " + raf.getFilePointer());
            raf.seek(100);
            if (raf.getFilePointer() != 100) throw new IOException("Unexpected file pointer value " + raf.getFilePointer());
            System.out.println("PASSED: Seek Test");
        } catch (IOException e) {
            System.out.println("FAILED: Seek Test: " + e);
        }
        System.out.println("Test 3: Validation Test");
        boolean failed = false;
        try {
            new RandomAccessFile("/vmlinuz", "rwx");
            System.out.println("Did not detect invalid mode");
            failed = true;
        } catch (IllegalArgumentException e) {
            ;
        } catch (IOException e) {
            ;
        }
        try {
            new RandomAccessFile("/vmlinuz", "rw");
            System.out.println("Did not detect read only file opened for write");
            failed = true;
        } catch (IOException e) {
            ;
        }
        try {
            new RandomAccessFile("/sherlockholmes", "r");
            System.out.println("Did not detect non-existent file");
            failed = true;
        } catch (IOException e) {
            ;
        }
        try {
            RandomAccessFile raf = new RandomAccessFile("/etc/services", "r");
            raf.seek(raf.length());
            raf.write('\n');
            System.out.println("Did not detect invalid write operation on read only file");
            failed = true;
        } catch (IOException e) {
            ;
        }
        if (failed) System.out.println("FAILED: Validation Test"); else System.out.println("PASSED: Validation Test");
        System.out.println("Finished test of RandomAccessFile");
    }
}
