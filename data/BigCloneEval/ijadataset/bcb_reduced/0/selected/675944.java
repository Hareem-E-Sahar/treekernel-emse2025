package org.apache.harmony.nio.tests.java.nio.channels;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import tests.support.Support_PortManager;
import junit.framework.TestCase;

/**
 * Note: the test case uses a temp text file named "test" which contains 31
 * characters : "P@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]"
 * 
 */
public class ChannelsTest extends TestCase {

    private static final String CODE_SET = "GB2312";

    private static final String BAD_CODE_SET = "GB2313";

    private FileInputStream fins;

    private FileOutputStream fouts;

    private final int writebufSize = 60;

    private final int testNum = 10;

    private final int fileSize = 31;

    private File tmpFile;

    protected void setUp() throws Exception {
        super.setUp();
        tmpFile = File.createTempFile("test", "tmp");
        tmpFile.deleteOnExit();
        this.writeFileSame();
    }

    protected void tearDown() throws Exception {
        if (null != this.fins) {
            this.fins.close();
            this.fins = null;
        }
        if (null != this.fouts) {
            this.fouts.close();
            this.fouts = null;
        }
        tmpFile.delete();
        super.tearDown();
    }

    private void writeFileSame() throws IOException {
        this.fouts = new FileOutputStream(tmpFile);
        byte[] bit = new byte[1];
        bit[0] = 80;
        this.fouts.write(bit);
        this.fouts.flush();
        String writebuf = "";
        for (int val = 0; val < this.writebufSize / 2; val++) {
            writebuf = writebuf + ((char) (val + 64));
        }
        this.fouts.write(writebuf.getBytes());
    }

    private void assertFileSizeSame(File fileToTest, int compareNumber) throws IOException {
        FileInputStream file = new FileInputStream(fileToTest);
        assertEquals(file.available(), compareNumber);
        file.close();
    }

    public void testNewChannelInputStream_InputNull() throws IOException {
        ByteBuffer byteBuf = ByteBuffer.allocate(this.testNum);
        this.fins = null;
        int readres = this.testNum;
        ReadableByteChannel rbChannel = Channels.newChannel(this.fins);
        assertNotNull(rbChannel);
        try {
            readres = rbChannel.read(byteBuf);
            fail();
        } catch (NullPointerException e) {
        }
        assertEquals(this.testNum, readres);
    }

    public void testNewChannelInputStream_BufferNull() throws IOException {
        ByteBuffer byteBuf = ByteBuffer.allocate(this.testNum);
        int readres = this.testNum;
        this.fins = new FileInputStream(tmpFile);
        ReadableByteChannel rbChannel = Channels.newChannel(this.fins);
        assertNotNull(rbChannel);
        try {
            readres = rbChannel.read(null);
            fail();
        } catch (NullPointerException e) {
        }
        assertEquals(this.testNum, readres);
        readres = 0;
        try {
            readres = rbChannel.read(byteBuf);
        } catch (NullPointerException e) {
            fail();
        }
        assertEquals(this.testNum, readres);
    }

    public void testNewChannelInputStream() throws IOException {
        int bufSize = 10;
        int readres = 0;
        byte[] byteArray = new byte[bufSize];
        ByteBuffer byteBuf = ByteBuffer.allocate(bufSize);
        this.fins = new FileInputStream(tmpFile);
        readres = this.fins.read(byteArray);
        assertEquals(bufSize, readres);
        assertFalse(0 == this.fins.available());
        ReadableByteChannel rbChannel = Channels.newChannel(this.fins);
        assertFalse(0 == this.fins.available());
        readres = this.fins.read(byteArray);
        assertEquals(bufSize, readres);
        assertNotNull(rbChannel);
        readres = rbChannel.read(byteBuf);
        assertEquals(bufSize, readres);
        InputStream ins = Channels.newInputStream(rbChannel);
        assertNotNull(ins);
        assertEquals(0, ins.available());
    }

    public void testNewChannelOutputStream_inputNull() throws IOException {
        int writeres = this.testNum;
        ByteBuffer writebuf = ByteBuffer.allocate(this.writebufSize);
        for (int val = 0; val < this.writebufSize / 2; val++) {
            writebuf.putChar((char) (val + 64));
        }
        this.fouts = null;
        WritableByteChannel rbChannel = Channels.newChannel(this.fouts);
        writeres = rbChannel.write(writebuf);
        assertEquals(0, writeres);
        writebuf.flip();
        try {
            writeres = rbChannel.write(writebuf);
            fail("Should throw NPE.");
        } catch (NullPointerException e) {
        }
    }

    public void testNewChannelOutputStream_BufNull() throws IOException {
        int writeres = this.testNum;
        ByteBuffer writebuf = null;
        try {
            this.fouts = new FileOutputStream(tmpFile);
        } catch (FileNotFoundException e) {
            fail();
        }
        WritableByteChannel rbChannel = Channels.newChannel(this.fouts);
        try {
            writeres = rbChannel.write(writebuf);
            fail();
        } catch (NullPointerException e) {
        }
        assertEquals(this.testNum, writeres);
    }

    public void testNewChannelOutputStream() throws IOException {
        int writeNum = 0;
        ByteBuffer writebuf = ByteBuffer.allocateDirect(this.writebufSize);
        for (int val = 0; val < this.writebufSize / 2; val++) {
            writebuf.putChar((char) (val + 64));
        }
        this.fouts = new FileOutputStream(tmpFile);
        WritableByteChannel testChannel = this.fouts.getChannel();
        WritableByteChannel rbChannel = Channels.newChannel(this.fouts);
        assertTrue(testChannel.isOpen());
        assertTrue(rbChannel.isOpen());
        byte[] bit = new byte[1];
        bit[0] = 80;
        this.fouts.write(bit);
        this.fouts.flush();
        this.fins = new FileInputStream(tmpFile);
        assertEquals(this.fins.available(), 1);
        this.fins.close();
        writeNum = rbChannel.write(writebuf);
        assertEquals(0, writeNum);
        this.fouts.close();
        writeNum = rbChannel.write(writebuf);
        assertEquals(0, writeNum);
        try {
            writeNum = testChannel.write(writebuf);
            fail();
        } catch (ClosedChannelException e) {
        }
        assertEquals(0, writeNum);
        rbChannel.close();
        try {
            writeNum = testChannel.write(writebuf);
            fail();
        } catch (ClosedChannelException e) {
        }
    }

    public void testNewInputStreamReadableByteChannel_InputNull() throws Exception {
        byte[] readbuf = new byte[this.testNum];
        this.fins = new FileInputStream(tmpFile);
        ReadableByteChannel readbc = this.fins.getChannel();
        assertEquals(this.fileSize, this.fins.available());
        assertTrue(readbc.isOpen());
        InputStream testins = Channels.newInputStream(null);
        assertNotNull(testins);
        try {
            testins.read(readbuf);
            fail();
        } catch (NullPointerException e) {
        }
        assertEquals(0, testins.available());
        try {
            testins.close();
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testNewInputStreamReadableByteChannel() throws Exception {
        ByteBuffer readbcbuf = ByteBuffer.allocateDirect(this.testNum);
        byte[] readbuf = new byte[this.testNum];
        this.fins = new FileInputStream(tmpFile);
        ReadableByteChannel readbc = this.fins.getChannel();
        assertEquals(this.fileSize, this.fins.available());
        assertTrue(readbc.isOpen());
        InputStream testins = Channels.newInputStream(readbc);
        testins.read(readbuf);
        assertEquals(this.fins.available(), this.fileSize - this.testNum);
        int readNum = readbc.read(readbcbuf);
        assertEquals(readNum, this.testNum);
        assertEquals(this.fins.available(), this.fileSize - this.testNum * 2);
        testins.read(readbuf);
        assertEquals(this.fins.available(), this.fileSize - this.testNum * 3);
        readbc.close();
        assertFalse(readbc.isOpen());
        try {
            testins.read(readbuf);
            fail();
        } catch (ClosedChannelException e) {
        }
    }

    public void testNewOutputStreamWritableByteChannel_InputNull() throws Exception {
        byte[] writebuf = new byte[this.testNum];
        OutputStream testouts = Channels.newOutputStream(null);
        assertNotNull(testouts);
        try {
            testouts.write(writebuf);
            fail();
        } catch (NullPointerException e) {
        }
        testouts.flush();
        try {
            testouts.close();
            fail();
        } catch (NullPointerException e) {
        }
        WritableByteChannel writebc = Channels.newChannel((OutputStream) null);
        assertTrue(writebc.isOpen());
        OutputStream testoutputS = Channels.newOutputStream(writebc);
        try {
            testoutputS.write(writebuf);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testNewOutputStreamWritableByteChannel() throws Exception {
        byte[] writebuf = new byte[this.testNum];
        ByteBuffer writebcbuf = ByteBuffer.allocateDirect(this.testNum);
        this.fouts = new FileOutputStream(tmpFile);
        WritableByteChannel writebc = this.fouts.getChannel();
        assertTrue(writebc.isOpen());
        OutputStream testouts = Channels.newOutputStream(writebc);
        testouts.write(writebuf);
        this.assertFileSizeSame(tmpFile, this.testNum);
        writebc.write(writebcbuf);
        this.assertFileSizeSame(tmpFile, this.testNum * 2);
        testouts.write(writebuf);
        this.assertFileSizeSame(tmpFile, this.testNum * 3);
        writebc.close();
        assertFalse(writebc.isOpen());
        try {
            testouts.write(writebuf);
            fail();
        } catch (ClosedChannelException e) {
        }
    }

    public void testnewReaderCharsetError() throws Exception {
        this.fins = new FileInputStream(tmpFile);
        ReadableByteChannel rbChannel = Channels.newChannel(this.fins);
        try {
            Channels.newReader(rbChannel, Charset.forName(BAD_CODE_SET).newDecoder(), -1);
            fail();
        } catch (UnsupportedCharsetException e) {
        }
    }

    public void testnewWriterCharsetError() throws Exception {
        this.fouts = new FileOutputStream(tmpFile);
        WritableByteChannel wbChannel = Channels.newChannel(this.fouts);
        try {
            Channels.newWriter(wbChannel, Charset.forName(BAD_CODE_SET).newEncoder(), -1);
            fail();
        } catch (UnsupportedCharsetException e) {
        }
    }

    public void testNewReaderReadableByteChannelString_InputNull() throws IOException {
        int bufSize = this.testNum;
        int readres = 0;
        CharBuffer charBuf = CharBuffer.allocate(bufSize);
        this.fins = new FileInputStream(tmpFile);
        Reader testReader = Channels.newReader(null, Charset.forName(CODE_SET).newDecoder(), -1);
        assertNotNull(testReader);
        assertFalse(testReader.ready());
        try {
            readres = testReader.read((CharBuffer) null);
            fail();
        } catch (NullPointerException e) {
        }
        assertEquals(0, readres);
        try {
            readres = testReader.read(charBuf);
            fail();
        } catch (NullPointerException e) {
        }
        this.fins = null;
        ReadableByteChannel rbChannel = Channels.newChannel(this.fins);
        testReader = Channels.newReader(rbChannel, Charset.forName(CODE_SET).newDecoder(), -1);
        assertNotNull(testReader);
        assertFalse(testReader.ready());
        try {
            readres = testReader.read(charBuf);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testNewReaderReadableByteChannelString_internalBufferZero() throws IOException {
        int bufSize = this.testNum;
        int readres = 0;
        CharBuffer charBuf = CharBuffer.allocate(bufSize);
        this.fins = new FileInputStream(tmpFile);
        Reader testReader = Channels.newReader(null, Charset.forName(CODE_SET).newDecoder(), 0);
        assertNotNull(testReader);
        assertFalse(testReader.ready());
        try {
            readres = testReader.read((CharBuffer) null);
            fail();
        } catch (NullPointerException e) {
        }
        assertEquals(0, readres);
        try {
            readres = testReader.read(charBuf);
            fail();
        } catch (NullPointerException e) {
        }
        this.fins = null;
        ReadableByteChannel rbChannel = Channels.newChannel(this.fins);
        testReader = Channels.newReader(rbChannel, Charset.forName(CODE_SET).newDecoder(), -1);
        assertNotNull(testReader);
        assertFalse(testReader.ready());
        try {
            readres = testReader.read(charBuf);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testNewReaderReadableByteChannelString() throws IOException {
        int bufSize = this.testNum;
        int readres = 0;
        CharBuffer charBuf = CharBuffer.allocate(bufSize);
        this.fins = new FileInputStream(tmpFile);
        ReadableByteChannel rbChannel = Channels.newChannel(this.fins);
        Reader testReader = Channels.newReader(rbChannel, Charset.forName(CODE_SET).newDecoder(), -1);
        Reader testReader_s = Channels.newReader(rbChannel, CODE_SET);
        assertEquals(this.fileSize, this.fins.available());
        assertFalse(testReader.ready());
        assertFalse(testReader_s.ready());
        readres = testReader.read(charBuf);
        assertEquals(bufSize, readres);
        assertEquals(0, this.fins.available());
        try {
            readres = testReader.read((CharBuffer) null);
            fail();
        } catch (NullPointerException e) {
        }
        readres = testReader_s.read(charBuf);
        assertEquals(0, readres);
        assertTrue(testReader.ready());
        assertFalse(testReader_s.ready());
    }

    public void testNewWriterWritableByteChannelString_internalBufZero() throws IOException {
        String writebuf = "";
        for (int val = 0; val < this.writebufSize / 2; val++) {
            writebuf = writebuf + ((char) (val + 64));
        }
        Writer testWriter = Channels.newWriter(null, Charset.forName(CODE_SET).newEncoder(), -1);
        testWriter.write(writebuf);
        try {
            testWriter.flush();
            fail();
        } catch (NullPointerException e) {
        }
        try {
            testWriter.close();
            fail();
        } catch (NullPointerException e) {
        }
        this.fouts = null;
        WritableByteChannel wbChannel = Channels.newChannel(this.fouts);
        testWriter = Channels.newWriter(wbChannel, Charset.forName(CODE_SET).newEncoder(), -1);
        testWriter.write(writebuf);
        try {
            testWriter.flush();
            fail();
        } catch (NullPointerException e) {
        }
        try {
            testWriter.close();
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testNewWriterWritableByteChannelString_InputNull() throws IOException {
        this.fouts = new FileOutputStream(tmpFile);
        WritableByteChannel wbChannel = Channels.newChannel(this.fouts);
        Writer testWriter = Channels.newWriter(wbChannel, Charset.forName(CODE_SET).newEncoder(), 1);
        String writebuf = "";
        for (int val = 0; val < this.writebufSize / 2; val++) {
            writebuf = writebuf + ((char) (val + 64));
        }
        testWriter.write(writebuf);
        testWriter.flush();
        testWriter.close();
    }

    public void testNewWriterWritableByteChannelString() throws IOException {
        this.fouts = new FileOutputStream(tmpFile);
        WritableByteChannel wbChannel = Channels.newChannel(this.fouts);
        Writer testWriter = Channels.newWriter(wbChannel, CODE_SET);
        Writer testWriter_s = Channels.newWriter(wbChannel, Charset.forName(CODE_SET).newEncoder(), -1);
        String writebuf = "";
        for (int val = 0; val < this.writebufSize / 2; val++) {
            writebuf = writebuf + ((char) (val + 64));
        }
        byte[] bit = new byte[1];
        bit[0] = 80;
        this.fouts.write(bit);
        this.assertFileSizeSame(tmpFile, 1);
        testWriter.write(writebuf);
        testWriter.flush();
        this.assertFileSizeSame(tmpFile, this.writebufSize / 2 + 1);
        testWriter_s.write(writebuf);
        testWriter.flush();
        this.assertFileSizeSame(tmpFile, this.writebufSize / 2 + 1);
        testWriter_s.write(writebuf);
        testWriter.flush();
        this.assertFileSizeSame(tmpFile, this.writebufSize / 2 + 1);
        for (int val = 0; val < this.writebufSize; val++) {
            writebuf = writebuf + ((char) (val + 64));
        }
        this.fouts.close();
        testWriter_s.write(writebuf);
        testWriter.flush();
        this.assertFileSizeSame(tmpFile, this.writebufSize / 2 + 1);
    }

    /**
	 * @tests java.nio.channels.Channels#newReader(ReadableByteChannel channel,
	 *        String charsetName)
	 */
    public void test_newReader_LReadableByteChannel_LString() throws IOException {
        InetSocketAddress localAddr = new InetSocketAddress("127.0.0.1", Support_PortManager.getNextPort());
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(localAddr);
        SocketChannel sc = SocketChannel.open();
        sc.connect(localAddr);
        sc.configureBlocking(false);
        assertFalse(sc.isBlocking());
        ssc.accept().close();
        ssc.close();
        assertFalse(sc.isBlocking());
        Reader reader = Channels.newReader(sc, "UTF16");
        int i = reader.read();
        assertEquals(-1, i);
        try {
            Channels.newInputStream(sc).read();
            fail("should throw IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
        }
        sc.close();
    }
}
