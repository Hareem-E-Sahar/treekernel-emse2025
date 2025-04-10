package net.sf.jannot.tabix;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Encapsulates file representation of various primitive data types. Forces
 * little-endian disk representation. Note that this class is currently not very
 * efficient. There are plans to increase the size of the ByteBuffer, and move
 * data between the ByteBuffer and the underlying input or output stream in
 * larger chunks.
 * 
 * All the read methods throw RuntimeEOFException if the input stream is
 * exhausted before the required number of bytes are read.
 * 
 * @author Dave Tefft
 */
public class BinaryCodec {

    private OutputStream outputStream;

    private String outputFileName;

    private InputStream inputStream;

    private String inputFileName;

    private boolean isWriting;

    /**
	 * For byte swapping.
	 */
    private ByteBuffer byteBuffer;

    /**
	 * For reading Strings of known length, this can reduce object creation
	 */
    private final byte[] scratchBuffer = new byte[16];

    private static final ByteOrder LITTLE_ENDIAN = ByteOrder.LITTLE_ENDIAN;

    private static final byte NULL_BYTE[] = { 0 };

    private static final long MAX_UBYTE = (Byte.MAX_VALUE * 2) + 1;

    private static final long MAX_USHORT = (Short.MAX_VALUE * 2) + 1;

    private static final long MAX_UINT = ((long) Integer.MAX_VALUE * 2) + 1;

    private static final int MAX_BYTE_BUFFER = 8;

    /**
	 * Constructs BinaryCodec from a file and set it's mode to writing or not
	 * 
	 * @param file
	 *            file to be written to or read from
	 * @param writing
	 *            whether the file is being written to
	 */
    public BinaryCodec(final File file, final boolean writing) {
        this();
        try {
            this.isWriting = writing;
            if (this.isWriting) {
                this.outputStream = new FileOutputStream(file);
                this.outputFileName = file.getName();
            } else {
                this.inputStream = new FileInputStream(file);
                this.inputFileName = file.getName();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeIOException("File not found: " + file, e);
        }
    }

    /**
	 * Constructs BinaryCodec from a file name and set it's mode to writing or
	 * not
	 * 
	 * @param fileName
	 *            name of the file to be written to or read from
	 * @param writing
	 *            writing whether the file is being written to
	 */
    public BinaryCodec(final String fileName, final boolean writing) {
        this(new File(fileName), writing);
    }

    /**
	 * Constructs BinaryCodec from an output stream
	 * 
	 * @param outputStream
	 *            Stream to write to, since it's an output stream we know that
	 *            isWriting should be set to true
	 */
    public BinaryCodec(final OutputStream outputStream) {
        this();
        setOutputStream(outputStream);
    }

    /**
	 * Constructs BinaryCodec from an input stream
	 * 
	 * @param inputStream
	 *            Stream to read from, since we are reading isWriting is set to
	 *            false
	 */
    public BinaryCodec(final InputStream inputStream) {
        this();
        setInputStream(inputStream);
    }

    /**
	 * Ambiguous whether reading or writing until set{In,Out}putStream is called
	 */
    public BinaryCodec() {
        initByteBuffer();
    }

    /**
	 * Shared among ctors. Note that if endianness is changed, all the unsigned
	 * methods must also be changed.
	 */
    private void initByteBuffer() {
        byteBuffer = ByteBuffer.allocate(MAX_BYTE_BUFFER);
        byteBuffer.order(LITTLE_ENDIAN);
    }

    /**
	 * Write whatever has been put into the byte buffer
	 * 
	 * @param numBytes
	 *            -- how much to write. Note that in case of writing an unsigned
	 *            value, more bytes were put into the ByteBuffer than will get
	 *            written out.
	 */
    private void writeByteBuffer(final int numBytes) {
        assert (numBytes <= byteBuffer.limit());
        writeBytes(byteBuffer.array(), 0, numBytes);
    }

    /**
	 * Writes a byte to the output buffer
	 * 
	 * @param bite
	 *            byte array to write
	 */
    public void writeByte(final byte bite) {
        byteBuffer.clear();
        byteBuffer.put(bite);
        writeByteBuffer(1);
    }

    public void writeByte(final int b) {
        writeByte((byte) b);
    }

    /**
	 * Writes a byte array to the output buffer
	 * 
	 * @param bytes
	 *            value to write
	 */
    public void writeBytes(final byte[] bytes) {
        writeBytes(bytes, 0, bytes.length);
    }

    public void writeBytes(final byte[] bytes, final int startOffset, final int numBytes) {
        if (!isWriting) {
            throw new IllegalStateException("Calling write method on BinaryCodec open for read.");
        }
        try {
            outputStream.write(bytes, startOffset, numBytes);
        } catch (IOException e) {
            throw new RuntimeIOException(constructErrorMessage("Write error"), e);
        }
    }

    /**
	 * Write a 32-bit int to the output stream
	 * 
	 * @param value
	 *            int to write
	 */
    public void writeInt(final int value) {
        byteBuffer.clear();
        byteBuffer.putInt(value);
        writeByteBuffer(4);
    }

    /**
	 * Write a double (8 bytes) to the output stream
	 * 
	 * @param value
	 *            double to write
	 */
    public void writeDouble(final double value) {
        byteBuffer.clear();
        byteBuffer.putDouble(value);
        writeByteBuffer(8);
    }

    /**
	 * Write a 64-bit long to the output stream
	 * 
	 * @param value
	 *            long to write
	 */
    public void writeLong(final long value) {
        byteBuffer.clear();
        byteBuffer.putLong(value);
        writeByteBuffer(8);
    }

    /**
	 * Write a 16-bit short to output stream
	 */
    public void writeShort(final short value) {
        byteBuffer.clear();
        byteBuffer.putShort(value);
        writeByteBuffer(2);
    }

    /**
	 * Write a float (4 bytes) to the output stream
	 * 
	 * @param value
	 *            float to write
	 */
    public void writeFloat(final float value) {
        byteBuffer.clear();
        byteBuffer.putFloat(value);
        writeByteBuffer(4);
    }

    /**
	 * Writes a boolean (1 byte) to the output buffer
	 * 
	 * @param value
	 *            boolean to write
	 */
    public void writeBoolean(final boolean value) {
        byteBuffer.clear();
        byteBuffer.put(value ? (byte) 1 : (byte) 0);
        writeByteBuffer(1);
    }

    /**
	 * Writes a string to the buffer as ASCII bytes
	 * 
	 * @param value
	 *            string to write to buffer
	 * @param writeLength
	 *            prefix the string with the length as a 32-bit int
	 * @param appendNull
	 *            add a null byte to the end of the string
	 */
    public void writeString(final String value, final boolean writeLength, final boolean appendNull) {
        if (writeLength) {
            int lengthToWrite = value.length();
            if (appendNull) lengthToWrite++;
            writeInt(lengthToWrite);
        }
        writeString(value);
        if (appendNull) writeBytes(NULL_BYTE);
    }

    /**
	 * Write a string to the buffer as ASCII bytes
	 * 
	 * @param value
	 *            string to write
	 */
    private void writeString(final String value) {
        writeBytes(value.getBytes());
    }

    /**
	 * Write an 8-bit unsigned byte. NOTE: This method will break if we change
	 * to big-endian.
	 */
    public void writeUByte(final short val) {
        if (val < 0) {
            throw new IllegalArgumentException("Negative value (" + val + ") passed to unsigned writing method.");
        }
        if (val > MAX_UBYTE) {
            throw new IllegalArgumentException("Value (" + val + ") to large to be written as ubyte.");
        }
        byteBuffer.clear();
        byteBuffer.putShort(val);
        writeByteBuffer(1);
    }

    /**
	 * Write a 16-bit unsigned short. NOTE: This method will break if we change
	 * to big-endian.
	 */
    public void writeUShort(final int val) {
        if (val < 0) {
            throw new IllegalArgumentException("Negative value (" + val + ") passed to unsigned writing method.");
        }
        if (val > MAX_USHORT) {
            throw new IllegalArgumentException("Value (" + val + ") to large to be written as ushort.");
        }
        byteBuffer.clear();
        byteBuffer.putInt(val);
        writeByteBuffer(2);
    }

    /**
	 * Write a 32-bit unsigned int. NOTE: This method will break if we change to
	 * big-endian.
	 */
    public void writeUInt(final long val) {
        if (val < 0) {
            throw new IllegalArgumentException("Negative value (" + val + ") passed to unsigned writing method.");
        }
        if (val > MAX_UINT) {
            throw new IllegalArgumentException("Value (" + val + ") to large to be written as uint.");
        }
        byteBuffer.clear();
        byteBuffer.putLong(val);
        writeByteBuffer(4);
    }

    /**
	 * Read a byte array from the input stream.
	 * 
	 * @throws net.sf.samtools.util.RuntimeEOFException
	 *             if fewer than buffer.length bytes to read
	 */
    public void readBytes(final byte[] buffer) {
        readBytes(buffer, 0, buffer.length);
    }

    /**
	 * Read a byte array from the input stream
	 * 
	 * @param buffer
	 *            where to put bytes read
	 * @param offset
	 *            offset to start putting bytes into buffer
	 * @param length
	 *            number of bytes to read
	 * @throws RuntimeEOFException
	 *             if fewer than length bytes to read
	 */
    public void readBytes(final byte[] buffer, final int offset, final int length) {
        int totalNumRead = 0;
        do {
            final int numRead = readBytesOrFewer(buffer, offset + totalNumRead, length - totalNumRead);
            if (numRead < 0) {
                throw new RuntimeEOFException(constructErrorMessage("Premature EOF"));
            } else {
                totalNumRead += numRead;
            }
        } while (totalNumRead < length);
    }

    /**
	 * Reads a byte array from the input stream.
	 * 
	 * @param buffer
	 *            where to put bytes read
	 * @param offset
	 *            offset to start putting bytes into buffer
	 * @param length
	 *            number of bytes to read. Fewer bytes may be read if EOF is
	 *            reached before length bytes have been read.
	 * @return the total number of bytes read into the buffer, or -1 if there is
	 *         no more data because the end of the stream has been reached.
	 */
    public int readBytesOrFewer(final byte[] buffer, final int offset, final int length) {
        if (isWriting) {
            throw new IllegalStateException("Calling read method on BinaryCodec open for write.");
        }
        try {
            return inputStream.read(buffer, offset, length);
        } catch (IOException e) {
            throw new RuntimeIOException(constructErrorMessage("Read error"), e);
        }
    }

    /**
	 * @return a single byte read from the input stream.
	 */
    public byte readByte() {
        if (isWriting) {
            throw new IllegalStateException("Calling read method on BinaryCodec open for write.");
        }
        try {
            final int ret = inputStream.read();
            if (ret == -1) {
                throw new RuntimeEOFException(constructErrorMessage("Premature EOF"));
            }
            return (byte) ret;
        } catch (IOException e) {
            throw new RuntimeIOException(constructErrorMessage("Read error"), e);
        }
    }

    /**
	 * @return true if it is possible to know for sure if at EOF, and it is
	 *         known for sure. If the input stream is a ByteArrayInputStream,
	 *         this is faster than causing a RuntimeEOFException to be thrown.
	 */
    public boolean knownAtEof() {
        if (isWriting) {
            throw new IllegalStateException("Calling knownAtEof method on BinaryCodec open for write.");
        }
        try {
            return inputStream instanceof ByteArrayInputStream && inputStream.available() == 0;
        } catch (IOException e) {
            throw new RuntimeIOException(constructErrorMessage("available() error"), e);
        }
    }

    /**
	 * Read a string off the input stream, as ASCII bytes
	 * 
	 * @param length
	 *            length of string to read
	 * @return String read from stream
	 */
    public String readString(final int length) {
        final byte[] buffer;
        if (length <= scratchBuffer.length) {
            buffer = scratchBuffer;
        } else {
            buffer = new byte[length];
        }
        readBytes(buffer, 0, length);
        return new String(buffer, 0, length);
    }

    /**
	 * Read ASCII bytes from the input stream until a null byte is read
	 * 
	 * @return String constructed from the ASCII bytes read
	 */
    public String readNullTerminatedString() {
        return readNullTerminatedString(this);
    }

    private static String readNullTerminatedString(final BinaryCodec binaryCodec) {
        final StringBuilder ret = new StringBuilder();
        for (byte b = binaryCodec.readByte(); b != 0; b = binaryCodec.readByte()) {
            ret.append((char) (b & 0xff));
        }
        return ret.toString();
    }

    /**
	 * Read an int length, and then a String of that length
	 * 
	 * @param devourNull
	 *            if true, the length include a null terminator, which is read
	 *            and discarded
	 */
    public String readLengthAndString(final boolean devourNull) {
        int length = readInt();
        if (devourNull) {
            --length;
        }
        final String ret = readString(length);
        if (devourNull) {
            readByte();
        }
        return ret;
    }

    private void readByteBuffer(final int numBytes) {
        assert (numBytes <= byteBuffer.capacity());
        readBytes(byteBuffer.array(), 0, numBytes);
        byteBuffer.limit(byteBuffer.capacity());
        byteBuffer.position(numBytes);
    }

    /**
	 * Read an int off the input stream
	 * 
	 * @return int from input stream
	 */
    public int readInt() {
        readByteBuffer(4);
        byteBuffer.flip();
        return byteBuffer.getInt();
    }

    /**
	 * Reads a double off the input stream
	 * 
	 * @return double
	 */
    public double readDouble() {
        readByteBuffer(8);
        byteBuffer.flip();
        return byteBuffer.getDouble();
    }

    /**
	 * Reads a long off the input stream
	 * 
	 * @return long
	 */
    public long readLong() {
        readByteBuffer(8);
        byteBuffer.flip();
        long out = byteBuffer.getLong();
        byte[] arr = byteBuffer.array();
        return out;
    }

    public short readShort() {
        readByteBuffer(2);
        byteBuffer.flip();
        return byteBuffer.getShort();
    }

    /**
	 * Reads a float off the input stream
	 * 
	 * @return float
	 */
    public float readFloat() {
        readByteBuffer(4);
        byteBuffer.flip();
        return byteBuffer.getFloat();
    }

    /**
	 * Reads a boolean off the input stream, represented as a byte with value 1
	 * or 0
	 * 
	 * @return boolean
	 */
    public boolean readBoolean() {
        return (((int) readByte()) == 1);
    }

    /**
	 * Reads an 8-bit unsigned byte from the input stream. This method assumes
	 * little-endianness.
	 */
    public short readUByte() {
        readByteBuffer(1);
        byteBuffer.put((byte) 0);
        byteBuffer.flip();
        return byteBuffer.getShort();
    }

    /**
	 * Reads a 16-bit unsigned short from the input stream. This method assumes
	 * little-endianness.
	 */
    public int readUShort() {
        readByteBuffer(2);
        byteBuffer.putShort((short) 0);
        byteBuffer.flip();
        return byteBuffer.getInt();
    }

    /**
	 * Reads a 32-bit unsigned int from the input stream. This method assumes
	 * little-endianness.
	 */
    public long readUInt() {
        readByteBuffer(4);
        byteBuffer.putInt(0);
        byteBuffer.flip();
        return byteBuffer.getLong();
    }

    /**
	 * Reads a 64-bit unsigned long from the input stream. This method assumes
	 * little-endianness and will truncate for large values.
	 */
    public long readULong() {
        readByteBuffer(8);
        byteBuffer.putInt(0);
        byteBuffer.flip();
        return byteBuffer.getLong();
    }

    /**
	 * Close the appropriate stream
	 */
    public void close() {
        try {
            if (this.isWriting) this.outputStream.close(); else this.inputStream.close();
        } catch (IOException e) {
            throw new RuntimeIOException(e.getMessage(), e);
        }
    }

    private String constructErrorMessage(final String msg) {
        final StringBuilder sb = new StringBuilder(msg);
        sb.append("; BinaryCodec in ");
        sb.append(isWriting ? "write" : "read");
        sb.append("mode; ");
        final String filename = isWriting ? outputFileName : inputFileName;
        if (filename != null) {
            sb.append("file: ");
            sb.append(filename);
        } else {
            sb.append("streamed file (filename not available)");
        }
        return sb.toString();
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(final String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public void setInputFileName(final String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public boolean isWriting() {
        return isWriting;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(final InputStream is) {
        isWriting = false;
        this.inputStream = is;
    }

    public void setOutputStream(final OutputStream os) {
        isWriting = true;
        this.outputStream = os;
    }
}
