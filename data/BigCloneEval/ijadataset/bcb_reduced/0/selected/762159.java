package org.apache.harmony.luni.tests.java.io;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class PipedInputStreamTest extends junit.framework.TestCase {

    static class PWriter implements Runnable {

        PipedOutputStream pos;

        public byte bytes[];

        public void run() {
            try {
                pos.write(bytes);
                synchronized (this) {
                    notify();
                }
            } catch (IOException e) {
                e.printStackTrace(System.out);
                System.out.println("Could not write bytes");
            }
        }

        public PWriter(PipedOutputStream pout, int nbytes) {
            pos = pout;
            bytes = new byte[nbytes];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) (System.currentTimeMillis() % 9);
            }
        }
    }

    Thread t;

    PWriter pw;

    PipedInputStream pis;

    PipedOutputStream pos;

    /**
	 * @tests java.io.PipedInputStream#PipedInputStream()
	 */
    public void test_Constructor() {
    }

    /**
	 * @tests java.io.PipedInputStream#PipedInputStream(java.io.PipedOutputStream)
	 */
    public void test_ConstructorLjava_io_PipedOutputStream() throws Exception {
        pis = new PipedInputStream(new PipedOutputStream());
        pis.available();
    }

    /**
     * @test java.io.PipedInputStream#read()
     */
    public void test_readException() throws IOException {
        pis = new PipedInputStream();
        pos = new PipedOutputStream();
        try {
            pis.connect(pos);
            t = new Thread(pw = new PWriter(pos, 1000));
            t.start();
            assertTrue(t.isAlive());
            while (true) {
                pis.read();
                t.interrupt();
            }
        } catch (IOException e) {
            if (!e.getMessage().contains("Write end dead")) {
                throw e;
            }
        } finally {
            try {
                pis.close();
                pos.close();
            } catch (IOException ee) {
            }
        }
    }

    /**
     * @tests java.io.PipedInputStream#available()
     */
    public void test_available() throws Exception {
        pis = new PipedInputStream();
        pos = new PipedOutputStream();
        pis.connect(pos);
        t = new Thread(pw = new PWriter(pos, 1000));
        t.start();
        synchronized (pw) {
            pw.wait(10000);
        }
        assertTrue("Available returned incorrect number of bytes: " + pis.available(), pis.available() == 1000);
        PipedInputStream pin = new PipedInputStream();
        PipedOutputStream pout = new PipedOutputStream(pin);
        for (int i = 0; i < 1024; i++) {
            pout.write(i);
        }
        assertEquals("Incorrect available count", 1024, pin.available());
    }

    /**
	 * @tests java.io.PipedInputStream#close()
	 */
    public void test_close() throws IOException {
        pis = new PipedInputStream();
        pos = new PipedOutputStream();
        pis.connect(pos);
        pis.close();
        try {
            pos.write((byte) 127);
            fail("Failed to throw expected exception");
        } catch (IOException e) {
            return;
        }
    }

    /**
	 * @tests java.io.PipedInputStream#connect(java.io.PipedOutputStream)
	 */
    public void test_connectLjava_io_PipedOutputStream() throws Exception {
        pis = new PipedInputStream();
        pos = new PipedOutputStream();
        assertEquals("Non-conected pipe returned non-zero available bytes", 0, pis.available());
        pis.connect(pos);
        t = new Thread(pw = new PWriter(pos, 1000));
        t.start();
        synchronized (pw) {
            pw.wait(10000);
        }
        assertEquals("Available returned incorrect number of bytes", 1000, pis.available());
    }

    /**
	 * @tests java.io.PipedInputStream#read()
	 */
    public void test_read() throws Exception {
        pis = new PipedInputStream();
        pos = new PipedOutputStream();
        pis.connect(pos);
        t = new Thread(pw = new PWriter(pos, 1000));
        t.start();
        synchronized (pw) {
            pw.wait(10000);
        }
        assertEquals("Available returned incorrect number of bytes", 1000, pis.available());
        assertEquals("read returned incorrect byte", pw.bytes[0], (byte) pis.read());
    }

    /**
	 * @tests java.io.PipedInputStream#read(byte[], int, int)
	 */
    public void test_read$BII() throws Exception {
        pis = new PipedInputStream();
        pos = new PipedOutputStream();
        pis.connect(pos);
        t = new Thread(pw = new PWriter(pos, 1000));
        t.start();
        byte[] buf = new byte[400];
        synchronized (pw) {
            pw.wait(10000);
        }
        assertTrue("Available returned incorrect number of bytes: " + pis.available(), pis.available() == 1000);
        pis.read(buf, 0, 400);
        for (int i = 0; i < 400; i++) {
            assertEquals("read returned incorrect byte[]", pw.bytes[i], buf[i]);
        }
    }

    /**
     * @tests java.io.PipedInputStream#read(byte[], int, int)
     * Regression for HARMONY-387
     */
    public void test_read$BII_2() throws IOException {
        PipedInputStream obj = new PipedInputStream();
        try {
            obj.read(new byte[0], 0, -1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException t) {
            assertEquals("IndexOutOfBoundsException rather than a subclass expected", IndexOutOfBoundsException.class, t.getClass());
        }
    }

    /**
     * @tests java.io.PipedInputStream#read(byte[], int, int)
     */
    public void test_read$BII_3() throws IOException {
        PipedInputStream obj = new PipedInputStream();
        try {
            obj.read(new byte[0], -1, 0);
            fail("IndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException t) {
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException t) {
        }
    }

    /**
     * @tests java.io.PipedInputStream#read(byte[], int, int)
     */
    public void test_read$BII_4() throws IOException {
        PipedInputStream obj = new PipedInputStream();
        try {
            obj.read(new byte[0], -1, -1);
            fail("IndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException t) {
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException t) {
        }
    }

    /**
     * @tests java.io.PipedInputStream#receive(int)
     */
    public void test_receive() throws IOException {
        pis = new PipedInputStream();
        pos = new PipedOutputStream();
        pis.connect(pos);
        class WriteRunnable implements Runnable {

            boolean pass = false;

            volatile boolean readerAlive = true;

            public void run() {
                try {
                    pos.write(1);
                    while (readerAlive) {
                        ;
                    }
                    try {
                        pos.write(1);
                    } catch (IOException e) {
                        pass = true;
                    }
                } catch (IOException e) {
                }
            }
        }
        WriteRunnable writeRunnable = new WriteRunnable();
        Thread writeThread = new Thread(writeRunnable);
        class ReadRunnable implements Runnable {

            boolean pass;

            public void run() {
                try {
                    pis.read();
                    pass = true;
                } catch (IOException e) {
                }
            }
        }
        ;
        ReadRunnable readRunnable = new ReadRunnable();
        Thread readThread = new Thread(readRunnable);
        writeThread.start();
        readThread.start();
        while (readThread.isAlive()) {
            ;
        }
        writeRunnable.readerAlive = false;
        assertTrue("reader thread failed to read", readRunnable.pass);
        while (writeThread.isAlive()) {
            ;
        }
        assertTrue("writer thread failed to recognize dead reader", writeRunnable.pass);
        pis = new PipedInputStream();
        pos = new PipedOutputStream();
        pis.connect(pos);
        class MyRunnable implements Runnable {

            boolean pass;

            public void run() {
                try {
                    pos.write(1);
                } catch (IOException e) {
                    pass = true;
                }
            }
        }
        MyRunnable myRun = new MyRunnable();
        synchronized (pis) {
            t = new Thread(myRun);
            t.start();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            pos.close();
        }
        while (t.isAlive()) {
            ;
        }
        assertTrue("write failed to throw IOException on closed PipedOutputStream", myRun.pass);
    }

    /**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
    protected void tearDown() throws Exception {
        try {
            if (t != null) {
                t.interrupt();
            }
        } catch (Exception ignore) {
        }
        super.tearDown();
    }

    /**
     * @tests java.io.PipedInputStream#PipedInputStream(java.io.PipedOutputStream,
     *        int)
     * @since 1.6
     */
    public void test_Constructor_LPipedOutputStream_I() throws Exception {
        MockPipedInputStream mpis = new MockPipedInputStream(new PipedOutputStream(), 100);
        int bufferLength = mpis.bufferLength();
        assertEquals(100, bufferLength);
        try {
            pis = new PipedInputStream(null, -1);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            pis = new PipedInputStream(null, 0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * @tests java.io.PipedInputStream#PipedInputStream(int)
     * @since 1.6
     */
    public void test_Constructor_I() throws Exception {
        MockPipedInputStream mpis = new MockPipedInputStream(100);
        int bufferLength = mpis.bufferLength();
        assertEquals(100, bufferLength);
        try {
            pis = new PipedInputStream(-1);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            pis = new PipedInputStream(0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    static class MockPipedInputStream extends PipedInputStream {

        public MockPipedInputStream(java.io.PipedOutputStream src, int bufferSize) throws IOException {
            super(src, bufferSize);
        }

        public MockPipedInputStream(int bufferSize) {
            super(bufferSize);
        }

        public int bufferLength() {
            return super.buffer.length;
        }
    }
}
