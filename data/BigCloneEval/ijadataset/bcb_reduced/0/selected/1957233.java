package com.ams.io;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import com.ams.server.ByteBufferFactory;

public class RandomAccessFileReader implements IByteBufferReader {

    private RandomAccessFile file;

    private FileChannel channel = null;

    private ByteBuffer buffer = null;

    private long position = 0;

    private boolean eof = false;

    private RandomAccessFile openFile(String fileName) throws IOException {
        RandomAccessFile raFile = null;
        try {
            raFile = new RandomAccessFile(new File(fileName), "r");
        } catch (Exception e) {
            if (raFile != null) {
                raFile.close();
                throw new IOException("Corrupted File '" + fileName + "'");
            }
            throw new IOException("File not found '" + fileName + "'");
        }
        return raFile;
    }

    public RandomAccessFileReader(String fileName, long startPosition) throws IOException {
        this.file = openFile(fileName);
        this.channel = file.getChannel();
        file.seek(startPosition);
        position = startPosition;
    }

    public int readByte() throws IOException {
        byte[] one = new byte[1];
        int amount = read(one, 0, 1);
        return (amount < 0) ? -1 : one[0] & 0xff;
    }

    public synchronized int read(byte[] data, int offset, int size) throws IOException {
        if (eof) {
            throw new EOFException("stream is eof");
        }
        int amount = 0;
        int length = size;
        while (length > 0) {
            if (buffer != null && buffer.hasRemaining()) {
                int remain = buffer.remaining();
                if (length >= remain) {
                    buffer.get(data, offset, remain);
                    buffer = null;
                    length -= remain;
                    position += remain;
                    offset += remain;
                    amount += remain;
                } else {
                    buffer.get(data, offset, length);
                    position += length;
                    offset += length;
                    amount += length;
                    length = 0;
                }
            } else {
                this.buffer = ByteBufferFactory.allocate(8 * 1024);
                int len = channel.read(buffer);
                if (len < 0) {
                    eof = true;
                    this.buffer = null;
                    throw new EOFException("stream is eof");
                }
                buffer.flip();
            }
        }
        return amount;
    }

    public synchronized ByteBuffer[] read(int size) throws IOException {
        if (eof) {
            throw new EOFException("stream is eof");
        }
        ArrayList<ByteBuffer> list = new ArrayList<ByteBuffer>();
        int length = size;
        while (length > 0) {
            if (buffer != null && buffer.hasRemaining()) {
                int remain = buffer.remaining();
                if (length >= remain) {
                    list.add(buffer);
                    buffer = null;
                    length -= remain;
                    position += remain;
                } else {
                    ByteBuffer slice = buffer.slice();
                    slice.limit(length);
                    buffer.position(buffer.position() + length);
                    list.add(slice);
                    position += length;
                    length = 0;
                }
            } else {
                this.buffer = ByteBufferFactory.allocate(32 * 1024);
                int len = channel.read(buffer);
                if (len < 0) {
                    eof = true;
                    this.buffer = null;
                    throw new EOFException("stream is eof");
                }
                buffer.flip();
            }
        }
        return list.toArray(new ByteBuffer[list.size()]);
    }

    public synchronized void seek(long startPosition) throws IOException {
        if (this.buffer != null) {
            int bufferPosition = buffer.position();
            if (startPosition >= position - bufferPosition && startPosition < position + buffer.remaining()) {
                buffer.position((int) (startPosition - position + bufferPosition));
                position = startPosition;
                return;
            }
        }
        position = startPosition;
        file.seek(position);
        this.buffer = null;
        this.eof = false;
    }

    public void skip(long bytes) throws IOException {
        seek(position + bytes);
    }

    public boolean isEof() {
        return eof;
    }

    public void close() throws IOException {
        file.close();
    }

    public long getPosition() {
        return position;
    }
}
