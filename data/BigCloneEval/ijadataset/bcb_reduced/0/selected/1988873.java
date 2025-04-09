package org.caleigo.toolkit.tunnel;

import java.io.*;
import java.util.zip.*;
import org.caleigo.toolkit.log.*;
import org.caleigo.toolkit.util.CircularByteBuffer;

public class ZipPacker implements ITunnelPacker {

    public ZipPacker() {
    }

    /**
     * Creates and returns an InputStream that wrapps the provided InputStream.
     * The returned InputStream can then be used to unpack messages. The returned
     * InputStream must be stateless.
     */
    public InputStream createInputStream(InputStream in) {
        return new ZipPackerInputStream(in, 1024 * 64);
    }

    /**
     * Creates and returns an OutputStream that wrapps the provided OutputStream.
     * The returned OutputStream can then be used to pack messages. The returned
     * OutputStream must be stateless.
     */
    public OutputStream createOutputStream(OutputStream out) {
        return new ZipPackerOutputStream(out, 1024 * 64);
    }

    protected class ZipPackerOutputStream extends FilterOutputStream {

        protected byte[] mByteBuffer;

        protected int mBufferSize;

        protected int mCurrentPosition;

        protected ZipOutputStream mZipOutputStream;

        protected int mZipEntryID;

        /**
         * Creates a ZipPackerOutputStream that wrapps an OutputStream.
         *
         * @param out   the OutputStream that should be used to write the zipped
         *              data to.
         * @param bufferSize    the initial size of the internal buffer. The size
         *                      of the buffer is increased as needed. Each time
         *                      the buffer needs to be resized its size is increased
         *                      by bufferSize / 2 bytes.
         */
        public ZipPackerOutputStream(OutputStream out, int bufferSize) {
            super(out);
            mByteBuffer = new byte[bufferSize];
            mBufferSize = bufferSize;
            mZipOutputStream = new ZipOutputStream(out);
            mZipOutputStream.setLevel(Deflater.BEST_COMPRESSION);
        }

        public void write(int b) throws IOException {
            if (mCurrentPosition >= mByteBuffer.length) {
                byte[] newBuffer = new byte[mByteBuffer.length + mBufferSize / 2];
                System.arraycopy(mByteBuffer, 0, newBuffer, 0, mByteBuffer.length);
                mByteBuffer = newBuffer;
            }
            mByteBuffer[mCurrentPosition] = (byte) b;
            mCurrentPosition++;
        }

        public void flush() throws IOException {
            Log.print(this, "flush(): " + mCurrentPosition + " bytes");
            ZipEntry entry = new ZipEntry(Integer.toString(this.getNextZipEntryID()));
            mZipOutputStream.putNextEntry(entry);
            mZipOutputStream.write(mByteBuffer, 0, mCurrentPosition);
            mZipOutputStream.closeEntry();
            mZipOutputStream.flush();
            mCurrentPosition = 0;
        }

        protected int getNextZipEntryID() {
            mZipEntryID = (mZipEntryID < Integer.MAX_VALUE ? mZipEntryID + 1 : 0);
            return mZipEntryID;
        }
    }

    /**
     * Uses a ZipInputStream to read and unzip data from an InputStream. It uses
     * a circular buffer to store the unziped data.
     */
    protected class ZipPackerInputStream extends FilterInputStream {

        protected byte[] mInByteBuffer;

        protected byte[] mSingleByteBuffer = new byte[1];

        protected CircularByteBuffer mDecompressedByteBuffer;

        protected ZipInputStream mZipInputStream;

        /**
         * Creates a ZipPackerInputStream that wrapps an InputStream.
         *
         * @param in   the InputStream that should be used to read the zipped
         *              data to from.
         * @param bufferSize    the initial size of the internal buffer. The size
         *                      of the buffer is increased as needed. Each time
         *                      the buffer needs to be resized its size is increased
         *                      by bufferSize / 2 bytes.
         */
        public ZipPackerInputStream(InputStream in, int bufferSize) {
            super(in);
            mInByteBuffer = new byte[bufferSize];
            mDecompressedByteBuffer = new CircularByteBuffer(bufferSize);
            mZipInputStream = new ZipInputStream(in);
        }

        public int available() throws IOException {
            Log.print(this, "return from available(): " + mDecompressedByteBuffer.getBufferSize());
            return mDecompressedByteBuffer.getBufferSize();
        }

        public int read() throws IOException {
            this.read(mSingleByteBuffer, 0, 1);
            return mSingleByteBuffer[0];
        }

        public int read(byte[] b, int off, int len) throws IOException {
            while (mDecompressedByteBuffer.getBufferSize() == 0) this.readFromZipStream();
            return mDecompressedByteBuffer.getFromBuffer(b, off, len);
        }

        protected void readFromZipStream() throws IOException {
            ZipEntry zipEntry = mZipInputStream.getNextEntry();
            if (zipEntry == null) throw new IOException("Couldn't read zip entry");
            int nbrOfBytesRead = 0;
            while ((nbrOfBytesRead = mZipInputStream.read(mInByteBuffer, 0, mInByteBuffer.length)) > -1) {
                mDecompressedByteBuffer.addToBuffer(mInByteBuffer, 0, nbrOfBytesRead);
            }
            Log.print(this, "Unzip message successfully: " + zipEntry.getSize() + "(" + zipEntry.getCompressedSize() + ")");
            mZipInputStream.closeEntry();
        }
    }
}
