package org.exist.util.io;

import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

/**
 * @author Adam Retter <adam.retter@googlemail.com>
 */
public class ByteBufferInputStreamTest {

    @Test
    public void available() throws IOException {
        final byte testData[] = "test data".getBytes();
        final ByteBuffer buf = ByteBuffer.wrap(testData);
        InputStream is = new ByteBufferInputStream(new TestableByteBufferAccessor(buf));
        assertEquals(testData.length, is.available());
    }

    @Test
    public void availableIsZeroAfterClose() throws IOException {
        final byte testData[] = "test data".getBytes();
        final ByteBuffer buf = ByteBuffer.wrap(testData);
        InputStream is = new ByteBufferInputStream(new TestableByteBufferAccessor(buf));
        is.close();
        assertEquals(0, is.available());
    }

    @Test
    public void availableAfterRead() throws IOException {
        final byte testData[] = "test data".getBytes();
        final ByteBuffer buf = ByteBuffer.wrap(testData);
        InputStream is = new ByteBufferInputStream(new TestableByteBufferAccessor(buf));
        is.read();
        is.read();
        assertEquals(testData.length - 2, is.available());
    }

    @Test
    public void readByteByByteCorrectAndThenReturnMinus1AtEndOfStream() throws IOException {
        final byte testData[] = "test data".getBytes();
        final ByteBuffer buf = ByteBuffer.wrap(testData);
        InputStream is = new ByteBufferInputStream(new TestableByteBufferAccessor(buf));
        for (int i = 0; i < testData.length; i++) {
            assertEquals(testData[i], is.read());
        }
        assertEquals(-1, is.read());
    }

    @Test
    public void readMultipleBytesCorrectAndThenReturnMinus1AtEndOfStream() throws IOException {
        final byte testData[] = "test data".getBytes();
        final ByteBuffer buf = ByteBuffer.wrap(testData);
        InputStream is = new ByteBufferInputStream(new TestableByteBufferAccessor(buf));
        byte readData[] = new byte[testData.length];
        int read = is.read(readData);
        assertEquals(testData.length, read);
        assertArrayEquals(testData, readData);
        assertEquals(-1, is.read(readData));
    }

    @Test
    public void readMultipleBytesPastAvailable() throws IOException {
        final byte testData[] = "test data".getBytes();
        final ByteBuffer buf = ByteBuffer.wrap(testData);
        InputStream is = new ByteBufferInputStream(new TestableByteBufferAccessor(buf));
        byte readData[] = new byte[testData.length + 2];
        int read = is.read(readData);
        assertEquals(testData.length, read);
        assertArrayEquals(testData, subArray(readData, testData.length));
        assertArrayEquals(new byte[] { 0, 0 }, subArray(readData, testData.length, 2));
    }

    @Test
    public void readMultipleBytesSpecificCorrectAndThenReturnMinus1AtEndOfStream() throws IOException {
        final byte testData[] = "test data".getBytes();
        final ByteBuffer buf = ByteBuffer.wrap(testData);
        InputStream is = new ByteBufferInputStream(new TestableByteBufferAccessor(buf));
        byte readData[] = new byte[testData.length];
        is.read(readData, 0, testData.length);
        assertArrayEquals(testData, readData);
        assertEquals(-1, is.read(readData, 0, testData.length));
    }

    @Test
    public void readMultipleBytesSpecificPastAvailable() throws IOException {
        final byte testData[] = "test data".getBytes();
        final ByteBuffer buf = ByteBuffer.wrap(testData);
        InputStream is = new ByteBufferInputStream(new TestableByteBufferAccessor(buf));
        byte readData[] = new byte[testData.length + 2];
        int read = is.read(readData, 0, readData.length);
        assertEquals(testData.length, read);
        assertArrayEquals(testData, subArray(readData, testData.length));
        assertArrayEquals(new byte[] { 0, 0 }, subArray(readData, testData.length, 2));
    }

    @Test(expected = IOException.class)
    public void readSingleByteAfterCloseThrowsException() throws IOException {
        final byte testData[] = "test data".getBytes();
        final ByteBuffer buf = ByteBuffer.wrap(testData);
        InputStream is = new ByteBufferInputStream(new TestableByteBufferAccessor(buf));
        try {
            is.close();
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }
        is.read();
    }

    @Test(expected = IOException.class)
    public void readMultipleBytesAfterCloseThrowsException() throws IOException {
        final byte testData[] = "test data".getBytes();
        final ByteBuffer buf = ByteBuffer.wrap(testData);
        InputStream is = new ByteBufferInputStream(new TestableByteBufferAccessor(buf));
        try {
            is.close();
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }
        byte readBuf[] = new byte[2];
        is.read(readBuf);
    }

    @Test(expected = IOException.class)
    public void readMultipleBytesSpecificAfterCloseThrowsException() throws IOException {
        final byte testData[] = "test data".getBytes();
        final ByteBuffer buf = ByteBuffer.wrap(testData);
        InputStream is = new ByteBufferInputStream(new TestableByteBufferAccessor(buf));
        try {
            is.close();
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }
        byte readBuf[] = new byte[2];
        is.read(readBuf, 0, 2);
    }

    @Test
    public void readMultipleBytesInLoop() throws IOException {
        Random random = new Random();
        byte testData[] = new byte[1024];
        random.nextBytes(testData);
        final ByteBuffer buf = ByteBuffer.wrap(testData);
        InputStream is = new ByteBufferInputStream(new TestableByteBufferAccessor(buf));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte readBuf[] = new byte[56];
        int read = -1;
        while ((read = is.read(readBuf)) > -1) {
            assertLessThanOrEqual(readBuf.length, read);
            baos.write(readBuf, 0, read);
        }
        assertArrayEquals(testData, baos.toByteArray());
    }

    @Test
    public void readMultipleBytesSpecificInLoop() throws IOException {
        Random random = new Random();
        byte testData[] = new byte[1024];
        random.nextBytes(testData);
        final ByteBuffer buf = ByteBuffer.wrap(testData);
        InputStream is = new ByteBufferInputStream(new TestableByteBufferAccessor(buf));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte readBuf[] = new byte[56];
        int read = -1;
        while ((read = is.read(readBuf, 0, readBuf.length)) > -1) {
            assertLessThanOrEqual(readBuf.length, read);
            baos.write(readBuf, 0, read);
        }
        assertArrayEquals(testData, baos.toByteArray());
    }

    @Test
    public void markReturnsTrue() {
        final byte testData[] = "test data".getBytes();
        final ByteBuffer buf = ByteBuffer.wrap(testData);
        InputStream is = new ByteBufferInputStream(new TestableByteBufferAccessor(buf));
        assertTrue(is.markSupported());
    }

    public class TestableByteBufferAccessor implements ByteBufferAccessor {

        private final ByteBuffer buf;

        public TestableByteBufferAccessor(ByteBuffer buf) {
            this.buf = buf;
        }

        @Override
        public ByteBuffer getBuffer() {
            return buf;
        }
    }

    private byte[] subArray(byte data[], int len) {
        byte newData[] = new byte[len];
        System.arraycopy(data, 0, newData, 0, len);
        return newData;
    }

    private byte[] subArray(byte data[], int offset, int len) {
        byte newData[] = new byte[len];
        System.arraycopy(data, offset, newData, 0, len);
        return newData;
    }

    private static void assertLessThanOrEqual(int expectedMax, int actual) {
        if (actual > expectedMax) {
            fail("Expected actual value" + actual + " to be less than or equal to " + expectedMax);
        }
    }
}
