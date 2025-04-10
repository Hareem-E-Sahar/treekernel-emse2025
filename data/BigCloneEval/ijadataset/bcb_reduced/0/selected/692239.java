package jpcsp.util;

import static java.lang.System.arraycopy;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpcsp.Emulator;
import jpcsp.Memory;
import jpcsp.MemoryMap;
import jpcsp.Allegrex.CpuState;
import jpcsp.HLE.Modules;
import jpcsp.filesystems.SeekableDataInput;
import jpcsp.filesystems.SeekableRandomFile;
import jpcsp.memory.IMemoryReader;
import jpcsp.memory.IMemoryWriter;
import jpcsp.memory.MemoryReader;
import jpcsp.memory.MemoryWriter;

public class Utilities {

    public static final Charset charset = Charset.forName("UTF-8");

    public static String formatString(String type, String oldstring) {
        int counter = 0;
        if (type.equals("byte")) {
            counter = 2;
        }
        if (type.equals("short")) {
            counter = 4;
        }
        if (type.equals("long")) {
            counter = 8;
        }
        int len = oldstring.length();
        StringBuilder sb = new StringBuilder();
        while (len++ < counter) {
            sb.append('0');
        }
        oldstring = sb.append(oldstring).toString();
        return oldstring;
    }

    public static String integerToBin(int value) {
        return Long.toBinaryString(0x0000000100000000L | ((value) & 0x00000000FFFFFFFFL)).substring(1);
    }

    public static String integerToHex(int value) {
        return Integer.toHexString(0x100 | value).substring(1).toUpperCase();
    }

    public static String integerToHexShort(int value) {
        return Integer.toHexString(0x10000 | value).substring(1).toUpperCase();
    }

    public static long readUWord(SeekableDataInput f) throws IOException {
        long l = (f.readUnsignedByte() | (f.readUnsignedByte() << 8) | (f.readUnsignedByte() << 16) | (f.readUnsignedByte() << 24));
        return (l & 0xFFFFFFFFL);
    }

    public static int readUByte(SeekableDataInput f) throws IOException {
        return f.readUnsignedByte();
    }

    public static int readUHalf(SeekableDataInput f) throws IOException {
        return f.readUnsignedByte() | (f.readUnsignedByte() << 8);
    }

    public static int readWord(SeekableDataInput f) throws IOException {
        return (f.readUnsignedByte() | (f.readUnsignedByte() << 8) | (f.readUnsignedByte() << 16) | (f.readUnsignedByte() << 24));
    }

    public static void skipUnknown(ByteBuffer buf, int length) throws IOException {
        buf.position(buf.position() + length);
    }

    public static String readStringZ(ByteBuffer buf) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte b;
        for (; buf.position() < buf.limit(); ) {
            b = (byte) readUByte(buf);
            if (b == 0) break;
            sb.append((char) b);
        }
        return sb.toString();
    }

    public static String readStringNZ(ByteBuffer buf, int n) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte b;
        for (; n > 0; n--) {
            b = (byte) readUByte(buf);
            if (b != 0) sb.append((char) b);
        }
        return sb.toString();
    }

    /**
      * Read a string from memory.
      * The string ends when the maximal length is reached or a '\0' byte is found.
      * The memory bytes are interpreted as UTF-8 bytes to form the string.
      *
      * @param mem     the memory
      * @param address the address of the first byte of the string
      * @param n       the maximal string length
      * @return        the string converted to UTF-8
      */
    public static String readStringNZ(Memory mem, int address, int n) {
        address &= Memory.addressMask;
        if (address + n > MemoryMap.END_RAM) {
            n = MemoryMap.END_RAM - address + 1;
        }
        byte[] bytes = new byte[Math.min(n, 10000)];
        int length = 0;
        IMemoryReader memoryReader = MemoryReader.getMemoryReader(address, n, 1);
        for (; n > 0; n--) {
            int b = memoryReader.readNext();
            if (b == 0) {
                break;
            }
            if (length >= bytes.length) {
                byte[] newBytes = new byte[bytes.length + 10000];
                System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
                bytes = newBytes;
            }
            bytes[length] = (byte) b;
            length++;
        }
        return new String(bytes, 0, length, charset);
    }

    public static String readStringZ(Memory mem, int address) {
        address &= Memory.addressMask;
        return readStringNZ(mem, address, MemoryMap.END_RAM - address + 1);
    }

    public static String readStringZ(int address) {
        return readStringZ(Memory.getInstance(), address);
    }

    public static String readStringNZ(int address, int n) {
        return readStringNZ(Memory.getInstance(), address, n);
    }

    public static void writeStringNZ(Memory mem, int address, int n, String s) {
        int offset = 0;
        IMemoryWriter memoryWriter = MemoryWriter.getMemoryWriter(address, n, 1);
        if (s != null) {
            byte[] bytes = s.getBytes(charset);
            while (offset < bytes.length && offset < n) {
                memoryWriter.writeNext(bytes[offset]);
                offset++;
            }
        }
        while (offset < n) {
            memoryWriter.writeNext(0);
            offset++;
        }
        memoryWriter.flush();
    }

    public static void writeStringZ(Memory mem, int address, String s) {
        writeStringNZ(mem, address, s.length() + 1, s);
    }

    public static void writeStringZ(ByteBuffer buf, String s) {
        buf.put(s.getBytes());
        buf.put((byte) 0);
    }

    public static short getUnsignedByte(ByteBuffer bb) throws IOException {
        return ((short) (bb.get() & 0xff));
    }

    public static void putUnsignedByte(ByteBuffer bb, int value) {
        bb.put((byte) (value & 0xFF));
    }

    public static short readUByte(ByteBuffer buf) throws IOException {
        return getUnsignedByte(buf);
    }

    public static int readUHalf(ByteBuffer buf) throws IOException {
        return getUnsignedByte(buf) | (getUnsignedByte(buf) << 8);
    }

    public static long readUWord(ByteBuffer buf) throws IOException {
        long l = (getUnsignedByte(buf) | (getUnsignedByte(buf) << 8) | (getUnsignedByte(buf) << 16) | (getUnsignedByte(buf) << 24));
        return (l & 0xFFFFFFFFL);
    }

    public static int readWord(ByteBuffer buf) throws IOException {
        return (getUnsignedByte(buf) | (getUnsignedByte(buf) << 8) | (getUnsignedByte(buf) << 16) | (getUnsignedByte(buf) << 24));
    }

    public static void writeWord(ByteBuffer buf, long value) {
        putUnsignedByte(buf, (int) (value >> 0));
        putUnsignedByte(buf, (int) (value >> 8));
        putUnsignedByte(buf, (int) (value >> 16));
        putUnsignedByte(buf, (int) (value >> 24));
    }

    public static void writeHalf(ByteBuffer buf, int value) {
        putUnsignedByte(buf, value >> 0);
        putUnsignedByte(buf, value >> 8);
    }

    public static void writeByte(ByteBuffer buf, int value) {
        putUnsignedByte(buf, value);
    }

    public static int parseAddress(String value) throws NumberFormatException {
        int address = 0;
        if (value == null) {
            return address;
        }
        value = value.trim();
        if (value.startsWith("0x")) {
            value = value.substring(2);
        }
        if (value.length() == 8 && value.charAt(0) >= '8') {
            address = (int) Long.parseLong(value, 16);
        } else {
            address = Integer.parseInt(value, 16);
        }
        return address;
    }

    /**
     * Parse the string as a number and returns its value.
     * If the string starts with "0x", the number is parsed
     * in base 16, otherwise base 10.
     *
     * @param s the string to be parsed
     * @return the numeric value represented by the string.
     */
    public static long parseLong(String s) {
        long value = 0;
        if (s == null) {
            return value;
        }
        if (s.startsWith("0x")) {
            value = Long.parseLong(s.substring(2), 16);
        } else {
            value = Long.parseLong(s);
        }
        return value;
    }

    /**
     * Parse the string as a number and returns its value.
     * The number is always parsed in base 16.
     * The string can start as an option with "0x".
     *
     * @param s the string to be parsed in base 16
     * @return the numeric value represented by the string.
     */
    public static long parseHexLong(String s) {
        long value = 0;
        if (s == null) {
            return value;
        }
        if (s.startsWith("0x")) {
            s = s.substring(2);
        }
        value = Long.parseLong(s, 16);
        return value;
    }

    public static int makePow2(int n) {
        --n;
        n = (n >> 1) | n;
        n = (n >> 2) | n;
        n = (n >> 4) | n;
        n = (n >> 8) | n;
        n = (n >> 16) | n;
        return ++n;
    }

    public static void readFully(SeekableDataInput input, int address, int length) throws IOException {
        final int blockSize = 1024 * 1024;
        while (length > 0) {
            int size = Math.min(length, blockSize);
            byte[] buffer = new byte[size];
            input.readFully(buffer);
            Memory.getInstance().copyToMemory(address, ByteBuffer.wrap(buffer), size);
            address += size;
            length -= size;
        }
    }

    public static void write(SeekableRandomFile output, int address, int length) throws IOException {
        Buffer buffer = Memory.getInstance().getBuffer(address, length);
        if (buffer instanceof ByteBuffer) {
            output.getChannel().write((ByteBuffer) buffer);
        } else if (length > 0) {
            byte[] bytes = new byte[length];
            IMemoryReader memoryReader = MemoryReader.getMemoryReader(address, length, 1);
            for (int i = 0; i < length; i++) {
                bytes[i] = (byte) memoryReader.readNext();
            }
            output.write(bytes);
        }
    }

    public static void bytePositionBuffer(Buffer buffer, int bytePosition) {
        buffer.position(bytePosition / bufferElementSize(buffer));
    }

    public static int bufferElementSize(Buffer buffer) {
        if (buffer instanceof IntBuffer) {
            return 4;
        }
        return 1;
    }

    public static String stripNL(String s) {
        if (s != null && s.endsWith("\n")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public static void putBuffer(ByteBuffer destination, Buffer source, ByteOrder sourceByteOrder) {
        ByteOrder order = destination.order();
        destination.order(sourceByteOrder);
        if (source instanceof IntBuffer) {
            destination.asIntBuffer().put((IntBuffer) source);
        } else if (source instanceof ShortBuffer) {
            destination.asShortBuffer().put((ShortBuffer) source);
        } else if (source instanceof ByteBuffer) {
            destination.put((ByteBuffer) source);
        } else if (source instanceof FloatBuffer) {
            destination.asFloatBuffer().put((FloatBuffer) source);
        } else {
            Modules.log.error("Utilities.putBuffer: Unsupported Buffer type " + source.getClass().getName());
            Emulator.PauseEmuWithStatus(Emulator.EMU_STATUS_UNIMPLEMENTED);
        }
        destination.order(order);
    }

    public static void putBuffer(ByteBuffer destination, Buffer source, ByteOrder sourceByteOrder, int lengthInBytes) {
        ByteOrder order = destination.order();
        destination.order(sourceByteOrder);
        int srcLimit = source.limit();
        if (source instanceof IntBuffer) {
            destination.asIntBuffer().put((IntBuffer) source.limit(source.position() + (lengthInBytes >> 2)));
        } else if (source instanceof ShortBuffer) {
            destination.asShortBuffer().put((ShortBuffer) source.limit(source.position() + (lengthInBytes >> 1)));
        } else if (source instanceof ByteBuffer) {
            destination.put((ByteBuffer) source.limit(source.position() + lengthInBytes));
        } else if (source instanceof FloatBuffer) {
            destination.asFloatBuffer().put((FloatBuffer) source.limit(source.position() + (lengthInBytes >> 2)));
        } else {
            Modules.log.error("Utilities.putBuffer: Unsupported Buffer type " + source.getClass().getName());
            Emulator.PauseEmuWithStatus(Emulator.EMU_STATUS_UNIMPLEMENTED);
        }
        destination.order(order);
        source.limit(srcLimit);
    }

    /**
     * Reads inputstream i into a String with the UTF-8 charset
     * until the inputstream is finished (don't use with infinite streams).
     * @param inputStream to read into a string
     * @param close if true, close the inputstream
     * @return a string
     * @throws java.io.IOException if thrown on reading the stream
     * @throws java.lang.NullPointerException if the given inputstream is null
     */
    public static String toString(InputStream inputStream, boolean close) throws IOException {
        if (inputStream == null) {
            throw new NullPointerException("null inputstream");
        }
        String string;
        StringBuilder outputBuilder = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while (null != (string = reader.readLine())) {
                outputBuilder.append(string).append('\n');
            }
        } finally {
            if (close) {
                close(inputStream);
            }
        }
        return outputBuilder.toString();
    }

    /**
     * Close closeables. Use this in a finally clause.
     */
    public static void close(Closeable... closeables) {
        for (Closeable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ex) {
                    Logger.getLogger(Utilities.class.getName()).log(Level.WARNING, "Couldn't close Closeable", ex);
                }
            }
        }
    }

    public static long makeValue64(int low32, int high32) {
        return (((long) high32) << 32) | ((low32) & 0xFFFFFFFFL);
    }

    public static void storeRegister64(CpuState cpu, int register, long value) {
        cpu.gpr[register] = (int) (value);
        cpu.gpr[register + 1] = (int) (value >> 32);
    }

    public static void returnRegister64(CpuState cpu, long value) {
        storeRegister64(cpu, 2, value);
    }

    public static long getRegister64(CpuState cpu, int register) {
        return makeValue64(cpu.gpr[register], cpu.gpr[register + 1]);
    }

    public static int getSizeKb(long sizeByte) {
        return (int) ((sizeByte + 1023) / 1024);
    }

    private static void addAsciiDump(StringBuilder dump, IMemoryReader charReader, int bytesPerLine) {
        dump.append("  >");
        for (int i = 0; i < bytesPerLine; i++) {
            char c = (char) charReader.readNext();
            if (c < ' ' || c > '~') {
                c = '.';
            }
            dump.append(c);
        }
        dump.append("<");
    }

    public static String getMemoryDump(int address, int length, int step, int bytesPerLine) {
        if (!Memory.isAddressGood(address) || length <= 0 || bytesPerLine <= 0 || step <= 0) {
            return "";
        }
        StringBuilder dump = new StringBuilder();
        if (length < bytesPerLine) {
            bytesPerLine = length;
        }
        IMemoryReader memoryReader = MemoryReader.getMemoryReader(address, length, step);
        IMemoryReader charReader = MemoryReader.getMemoryReader(address, length, 1);
        String format = String.format(" %%0%dX", step * 2);
        boolean startOfLine = true;
        for (int i = 0; i < length; i += step) {
            if ((i % bytesPerLine) < step) {
                if (i > 0) {
                    addAsciiDump(dump, charReader, bytesPerLine);
                }
                dump.append("\n");
                startOfLine = true;
            }
            if (startOfLine) {
                dump.append(String.format("0x%08X", address + i));
                startOfLine = false;
            }
            int value = memoryReader.readNext();
            if (length - i >= step) {
                dump.append(String.format(format, value));
            } else {
                switch(length - i) {
                    case 3:
                        dump.append(String.format(" %06X", value & 0x00FFFFFF));
                        break;
                    case 2:
                        dump.append(String.format(" %04X", value & 0x0000FFFF));
                        break;
                    case 1:
                        dump.append(String.format(" %02X", value & 0x000000FF));
                        break;
                }
            }
        }
        int lengthLastLine = length % bytesPerLine;
        if (lengthLastLine > 0) {
            for (int i = lengthLastLine; i < bytesPerLine; i++) {
                dump.append("  ");
                if ((i % step) == 0) {
                    dump.append(" ");
                }
            }
            addAsciiDump(dump, charReader, lengthLastLine);
        } else {
            addAsciiDump(dump, charReader, bytesPerLine);
        }
        return dump.toString();
    }

    public static String getMemoryDump(int[] values, int offset, int length, int entriesPerLine) {
        StringBuilder dump = new StringBuilder();
        boolean startOfLine = true;
        for (int i = 0; i < length; i++) {
            if ((i % entriesPerLine) == 0) {
                dump.append("\n");
                startOfLine = true;
            }
            if (startOfLine) {
                dump.append(String.format("0x%08X", (offset + i) << 2));
                startOfLine = false;
            }
            int value = values[offset + i];
            dump.append(String.format(" %08X", value));
        }
        return dump.toString();
    }

    public static int alignUp(int value, int alignment) {
        return alignDown(value + alignment, alignment);
    }

    public static int alignDown(int value, int alignment) {
        return value & ~alignment;
    }

    public static int endianSwap32(int x) {
        return Integer.reverseBytes(x);
    }

    public static int readUnaligned32(Memory mem, int address) {
        switch(address & 3) {
            case 0:
                return mem.read32(address);
            case 2:
                return mem.read16(address) | (mem.read16(address + 2) << 16);
            default:
                return (mem.read8(address + 3) << 24) | (mem.read8(address + 2) << 16) | (mem.read8(address + 1) << 8) | (mem.read8(address));
        }
    }

    public static void writeUnaligned32(Memory mem, int address, int data) {
        switch(address & 3) {
            case 0:
                mem.write32(address, data);
                break;
            case 2:
                mem.write16(address, (short) data);
                mem.write16(address + 2, (short) (data >> 16));
                break;
            default:
                mem.write8(address, (byte) data);
                mem.write8(address + 1, (byte) (data >> 8));
                mem.write8(address + 2, (byte) (data >> 16));
                mem.write8(address + 3, (byte) (data >> 24));
        }
    }

    public static int min(int a, int b) {
        return Math.min(a, b);
    }

    public static float min(float a, float b) {
        return Math.min(a, b);
    }

    public static int max(int a, int b) {
        return Math.max(a, b);
    }

    public static float max(float a, float b) {
        return Math.max(a, b);
    }

    /**
	 * Minimum value rounded down.
	 *
	 * @param a   first float value
	 * @param b   second float value
	 * @return    the largest int value that is less than or equal to both parameters
	 */
    public static int minInt(float a, float b) {
        return floor(min(a, b));
    }

    /**
	 * Minimum value rounded down.
	 *
	 * @param a   first int value
	 * @param b   second float value
	 * @return    the largest int value that is less than or equal to both parameters
	 */
    public static int minInt(int a, float b) {
        return min(a, floor(b));
    }

    /**
	 * Maximum value rounded up.
	 *
	 * @param a   first float value
	 * @param b   second float value
	 * @return    the smallest int value that is greater than or equal to both parameters
	 */
    public static int maxInt(float a, float b) {
        return ceil(max(a, b));
    }

    /**
	 * Maximum value rounded up.
	 *
	 * @param a   first float value
	 * @param b   second float value
	 * @return    the smallest int value that is greater than or equal to both parameters
	 */
    public static int maxInt(int a, float b) {
        return max(a, ceil(b));
    }

    public static int min(int a, int b, int c) {
        return Math.min(a, Math.min(b, c));
    }

    public static int max(int a, int b, int c) {
        return Math.max(a, Math.max(b, c));
    }

    public static void sleep(int micros) {
        sleep(micros / 1000, micros % 1000);
    }

    public static void sleep(int millis, int micros) {
        try {
            if (micros <= 0) {
                Thread.sleep(millis);
            } else {
                Thread.sleep(millis, micros * 1000);
            }
        } catch (InterruptedException e) {
        }
    }

    public static void matrixMult(float[] result, final float[] m1, final float[] m2) {
        float[] origResult = null;
        if (result == m1 || result == m2) {
            origResult = result;
            result = new float[16];
        }
        int i = 0;
        int j = 0;
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                result[i] = m1[x] * m2[j] + m1[x + 4] * m2[j + 1] + m1[x + 8] * m2[j + 2] + m1[x + 12] * m2[j + 3];
                i++;
            }
            j += 4;
        }
        if (origResult != null) {
            arraycopy(result, 0, origResult, 0, result.length);
        }
    }

    public static void vectorMult(final float[] result, final float[] m, final float[] v) {
        for (int i = 0; i < result.length; i++) {
            float s = v[0] * m[i];
            int k = i + 4;
            for (int j = 1; j < v.length; j++) {
                s += v[j] * m[k];
                k += 4;
            }
            result[i] = s;
        }
    }

    public static void vectorMult33(final float[] result, final float[] m, final float[] v) {
        result[0] = v[0] * m[0] + v[1] * m[4] + v[2] * m[8];
        result[1] = v[0] * m[1] + v[1] * m[5] + v[2] * m[9];
        result[2] = v[0] * m[2] + v[1] * m[6] + v[2] * m[10];
    }

    public static void vectorMult34(final float[] result, final float[] m, final float[] v) {
        result[0] = v[0] * m[0] + v[1] * m[4] + v[2] * m[8] + v[3] * m[12];
        result[1] = v[0] * m[1] + v[1] * m[5] + v[2] * m[9] + v[3] * m[13];
        result[2] = v[0] * m[2] + v[1] * m[6] + v[2] * m[10] + v[3] * m[14];
    }

    public static void vectorMult44(final float[] result, final float[] m, final float[] v) {
        result[0] = v[0] * m[0] + v[1] * m[4] + v[2] * m[8] + v[3] * m[12];
        result[1] = v[0] * m[1] + v[1] * m[5] + v[2] * m[9] + v[3] * m[13];
        result[2] = v[0] * m[2] + v[1] * m[6] + v[2] * m[10] + v[3] * m[14];
        result[3] = v[0] * m[3] + v[1] * m[7] + v[2] * m[11] + v[3] * m[15];
    }

    public static int round(float n) {
        return (int) (n + .5f);
    }

    public static int floor(float n) {
        return (int) Math.floor(n);
    }

    public static int ceil(float n) {
        return (int) Math.ceil(n);
    }

    public static int getPower2(int n) {
        return Integer.numberOfTrailingZeros(makePow2(n));
    }

    public static void copy(float[] to, float[] from) {
        arraycopy(from, 0, to, 0, to.length);
    }

    public static float dot3(float[] a, float[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    public static float dot3(float[] a, float x, float y, float z) {
        return a[0] * x + a[1] * y + a[2] * z;
    }

    public static float length3(float[] a) {
        return (float) Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]);
    }

    public static float invertedLength3(float[] a) {
        float length = length3(a);
        if (length == 0.f) {
            return 0.f;
        }
        return 1.f / length;
    }

    public static void normalize3(float[] result, float[] a) {
        float invertedLength = invertedLength3(a);
        result[0] = a[0] * invertedLength;
        result[1] = a[1] * invertedLength;
        result[2] = a[2] * invertedLength;
    }

    public static float pow(float a, float b) {
        return (float) Math.pow(a, b);
    }

    public static float clamp(float n, float minValue, float maxValue) {
        return max(minValue, min(n, maxValue));
    }

    /**
     * Invert a 3x3 matrix.
     * 
     * Based on
     *     http://en.wikipedia.org/wiki/Invert_matrix#Inversion_of_3.C3.973_matrices
     * 
     * @param result   the inverted matrix (stored as a 4x4 matrix, but only 3x3 is returned)
     * @param m        the matrix to be inverted (stored as a 4x4 matrix, but only 3x3 is used)
     * @return         true if the matrix could be inverted
     *                 false if the matrix could not be inverted
     */
    public static boolean invertMatrix3x3(float[] result, float[] m) {
        float A = m[5] * m[10] - m[6] * m[9];
        float B = m[6] * m[8] - m[4] * m[10];
        float C = m[4] * m[9] - m[5] * m[8];
        float det = m[0] * A + m[1] * B + m[2] * C;
        if (det == 0.f) {
            return false;
        }
        float invertedDet = 1.f / det;
        result[0] = A * invertedDet;
        result[1] = (m[2] * m[9] - m[1] * m[10]) * invertedDet;
        result[2] = (m[1] * m[6] - m[2] * m[5]) * invertedDet;
        result[4] = B * invertedDet;
        result[5] = (m[0] * m[10] - m[2] * m[8]) * invertedDet;
        result[6] = (m[2] * m[4] - m[0] * m[6]) * invertedDet;
        result[8] = C * invertedDet;
        result[9] = (m[8] * m[1] - m[0] * m[9]) * invertedDet;
        result[10] = (m[0] * m[5] - m[1] * m[4]) * invertedDet;
        return true;
    }

    public static void transposeMatrix3x3(float[] result, float[] m) {
        for (int i = 0, j = 0; i < 3; i++, j += 4) {
            result[i] = m[j];
            result[i + 4] = m[j + 1];
            result[i + 8] = m[j + 2];
        }
    }

    public static boolean sameColor(float[] c1, float[] c2, float[] c3) {
        for (int i = 0; i < 4; i++) {
            if (c1[i] != c2[i] || c1[i] != c3[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean sameColor(float[] c1, float[] c2, float[] c3, float[] c4) {
        for (int i = 0; i < 4; i++) {
            if (c1[i] != c2[i] || c1[i] != c3[i] || c1[i] != c4[i]) {
                return false;
            }
        }
        return true;
    }

    /**
	 * Transform a pixel coordinate (floating-point value "u" or "v") into
	 * a texel coordinate (integer value to access the texture).
	 *
	 * The texel coordinate is calculated by truncating the floating point value,
	 * not by rounding it. Otherwise transition problems occur at the borders.
	 * E.g. if a texture has a width of 64, valid texel coordinates range
	 * from 0 to 63. 64 is already outside of the texture and should not be
	 * generated when approaching the border to the texture.
	 *
	 * @param coordinate     the pixel coordinate
	 * @return               the texel coordinate
	 */
    public static final int pixelToTexel(float coordinate) {
        return (int) coordinate;
    }

    /**
	 * Wrap the value to the range [0..1[ (1 is excluded).
	 *
	 * E.g.
	 *    value == 4.0 -> return 0.0
	 *    value == 4.1 -> return 0.1
	 *    value == 4.9 -> return 0.9
	 *    value == -4.0 -> return 0.0
	 *    value == -4.1 -> return 0.9 (and not 0.1)
	 *    value == -4.9 -> return 0.1 (and not 0.9)
	 *
	 * @param value   the value to be wrapped
	 * @return        the wrapped value in the range [0..1[ (1 is excluded)
	 */
    public static float wrap(float value) {
        if (value >= 0.f) {
            return value - (int) value;
        }
        float wrappedValue = value - (float) Math.floor(value);
        if (wrappedValue >= 1.f) {
            wrappedValue -= 1.f;
        }
        return wrappedValue;
    }

    public static int wrap(float value, int valueMask) {
        return pixelToTexel(value) & valueMask;
    }
}
