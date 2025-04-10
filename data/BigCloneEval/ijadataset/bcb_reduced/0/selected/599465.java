package org.apache.harmony.luni.tests.java.io;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import junit.framework.TestCase;

public class FileOutputStreamTest extends TestCase {

    public String fileName;

    FileOutputStream fos;

    FileInputStream fis;

    File f;

    byte[] ibuf = new byte[4096];

    public String fileString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_java_io_FileInputStream\nTest_java_io_FileNotFoundException\nTest_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n";

    byte[] bytes;

    /**
     * @tests java.io.FileOutputStream#FileOutputStream(java.io.File)
     */
    public void test_ConstructorLjava_io_File() throws IOException {
        f = new File(fileName = System.getProperty("user.home"), "fos.tst");
        fos = new FileOutputStream(f);
    }

    /**
     * @tests java.io.FileOutputStream#FileOutputStream(java.io.FileDescriptor)
     */
    public void test_ConstructorLjava_io_FileDescriptor() throws IOException {
        f = new File(fileName = System.getProperty("user.home"), "fos.tst");
        fileName = f.getAbsolutePath();
        fos = new FileOutputStream(fileName);
        fos.write('l');
        fos.close();
        fis = new FileInputStream(fileName);
        fos = new FileOutputStream(fis.getFD());
        fos.close();
        fis.close();
    }

    /**
     * @tests java.io.FileOutputStream#FileOutputStream(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() throws IOException {
        f = new File(fileName = System.getProperty("user.home"), "fos.tst");
        fileName = f.getAbsolutePath();
        fos = new FileOutputStream(fileName);
        new FileOutputStream("nul");
    }

    /**
     * @tests java.io.FileOutputStream#FileOutputStream(java.lang.String,
     *        boolean)
     */
    public void test_ConstructorLjava_lang_StringZ() throws IOException {
        f = new File(System.getProperty("user.home"), "fos.tst");
        fos = new FileOutputStream(f.getPath(), false);
        fos.write("HI".getBytes(), 0, 2);
        fos.close();
        fos = new FileOutputStream(f.getPath(), true);
        fos.write(fileString.getBytes());
        fos.close();
        byte[] buf = new byte[fileString.length() + 2];
        fis = new FileInputStream(f.getPath());
        fis.read(buf, 0, buf.length);
        assertTrue("Failed to create appending stream", new String(buf, 0, buf.length).equals("HI" + fileString));
    }

    /**
     * @tests java.io.FileOutputStream#FileOutputStream(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String_I() throws IOException {
        try {
            fos = new FileOutputStream("");
            fail("should throw FileNotFoundException.");
        } catch (FileNotFoundException e) {
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
        try {
            fos = new FileOutputStream(new File(""));
            fail("should throw FileNotFoundException.");
        } catch (FileNotFoundException e) {
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * @tests java.io.FileOutputStream#close()
     */
    public void test_close() throws IOException {
        f = new File(System.getProperty("user.home"), "output.tst");
        fos = new FileOutputStream(f.getPath());
        fos.close();
        try {
            fos.write(fileString.getBytes());
            fail("Close test failed - wrote to closed stream");
        } catch (IOException e) {
        }
    }

    /**
     * @tests java.io.FileOutputStream#getFD()
     */
    public void test_getFD() throws IOException {
        f = new File(fileName = System.getProperty("user.home"), "testfd");
        fileName = f.getAbsolutePath();
        fos = new FileOutputStream(f);
        assertTrue("Returned invalid fd", fos.getFD().valid());
        fos.close();
        assertTrue("Returned invalid fd", !fos.getFD().valid());
    }

    /**
     * @tests java.io.FileOutputStream#write(byte[])
     */
    public void test_write$B() throws IOException {
        f = new File(System.getProperty("user.home"), "output.tst");
        fos = new FileOutputStream(f.getPath());
        fos.write(fileString.getBytes());
        fis = new FileInputStream(f.getPath());
        byte rbytes[] = new byte[4000];
        fis.read(rbytes, 0, fileString.length());
        assertTrue("Incorrect string returned", new String(rbytes, 0, fileString.length()).equals(fileString));
    }

    /**
     * @tests java.io.FileOutputStream#write(byte[], int, int)
     */
    public void test_write$BII() throws IOException {
        f = new File(System.getProperty("user.home"), "output.tst");
        fos = new FileOutputStream(f.getPath());
        fos.write(fileString.getBytes(), 0, fileString.length());
        fis = new FileInputStream(f.getPath());
        byte rbytes[] = new byte[4000];
        fis.read(rbytes, 0, fileString.length());
        assertTrue("Incorrect bytes written", new String(rbytes, 0, fileString.length()).equals(fileString));
        File file = new File("FileOutputStream.tmp");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        try {
            out.write(null, 0, 0);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        } finally {
            out.close();
            file.delete();
        }
    }

    /**
     * @tests java.io.FileOutputStream#write(int)
     */
    public void test_writeI() throws IOException {
        f = new File(System.getProperty("user.home"), "output.tst");
        fos = new FileOutputStream(f.getPath());
        fos.write('t');
        fis = new FileInputStream(f.getPath());
        assertEquals("Incorrect char written", 't', fis.read());
    }

    /**
     * @tests java.io.FileOutputStream#write(byte[], int, int)
     */
    public void test_write$BII2() throws IOException {
        f = new File(System.getProperty("user.home"), "output.tst");
        fos = new FileOutputStream(f.getPath());
        try {
            fos.write(null, 1, 1);
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {
        }
        try {
            fos.write(new byte[1], -1, 1);
            fail("IndexOutOfBoundsException must be thrown if off <0");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            fos.write(new byte[1], 0, -1);
            fail("IndexOutOfBoundsException must be thrown if len <0");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            fos.write(new byte[1], 0, 5);
            fail("IndexOutOfBoundsException must be thrown if off+len > b.length");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            fos.write(new byte[10], Integer.MAX_VALUE, 5);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            fos.write(new byte[10], 5, Integer.MAX_VALUE);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
        }
        fos.close();
    }

    /**
     * @tests java.io.FileOutputStream#write(byte[], int, int)
     */
    public void test_write$BII3() throws IOException {
        new FileOutputStream(new FileDescriptor()).write(new byte[1], 0, 0);
    }

    /**
     * @tests java.io.FileOutputStream#getChannel()
     */
    public void test_getChannel() throws IOException {
        File tmpfile = File.createTempFile("FileOutputStream", "tmp");
        tmpfile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmpfile);
        fos.write(bytes);
        fos.flush();
        fos.close();
        FileOutputStream f = new FileOutputStream(tmpfile, true);
        assertEquals(10, f.getChannel().position());
    }

    public void test_getChannel_Append() throws IOException {
        File tmpfile = File.createTempFile("FileOutputStream", "tmp");
        tmpfile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmpfile, true);
        assertEquals(0, fos.getChannel().position());
        fos.write(bytes);
        assertEquals(10, fos.getChannel().position());
        fos.write(bytes);
        assertEquals(20, fos.getChannel().position());
        fos.write(bytes);
        assertEquals(30, fos.getChannel().position());
        fos.close();
        try {
            fos.getChannel().position();
            fail("should throw ClosedChannelException");
        } catch (java.nio.channels.ClosedChannelException e) {
        }
    }

    public void test_getChannel_UnAppend() throws IOException {
        File tmpfile = File.createTempFile("FileOutputStream", "tmp");
        tmpfile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmpfile, false);
        assertEquals(0, fos.getChannel().position());
        fos.write(bytes);
        assertEquals(10, fos.getChannel().position());
        fos.write(bytes);
        assertEquals(20, fos.getChannel().position());
        fos.write(bytes);
        assertEquals(30, fos.getChannel().position());
        fos.close();
        try {
            fos.getChannel().position();
            fail("should throw ClosedChannelException");
        } catch (java.nio.channels.ClosedChannelException e) {
        }
    }

    public void test_getChannel_Unappend_Unappend() throws IOException {
        File tmpfile = File.createTempFile("FileOutputStream", "tmp");
        tmpfile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmpfile, false);
        assertEquals(0, fos.getChannel().position());
        fos.write(bytes);
        assertEquals(10, fos.getChannel().position());
        fos.close();
        fos = new FileOutputStream(tmpfile, false);
        assertEquals(0, fos.getChannel().position());
        fos.close();
    }

    public void test_getChannel_Unappend_Append() throws IOException {
        File tmpfile = File.createTempFile("FileOutputStream", "tmp");
        tmpfile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmpfile, false);
        assertEquals(0, fos.getChannel().position());
        fos.write(bytes);
        assertEquals(10, fos.getChannel().position());
        fos.close();
        fos = new FileOutputStream(tmpfile, true);
        assertEquals(10, fos.getChannel().position());
        fos.close();
    }

    public void test_getChannel_Append_Unappend() throws IOException {
        File tmpfile = File.createTempFile("FileOutputStream", "tmp");
        tmpfile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmpfile, true);
        assertEquals(0, fos.getChannel().position());
        fos.write(bytes);
        assertEquals(10, fos.getChannel().position());
        fos.close();
        fos = new FileOutputStream(tmpfile, false);
        assertEquals(0, fos.getChannel().position());
        fos.close();
    }

    public void test_getChanne_Append_Append() throws IOException {
        File tmpfile = File.createTempFile("FileOutputStream", "tmp");
        tmpfile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmpfile, true);
        assertEquals(0, fos.getChannel().position());
        fos.write(bytes);
        assertEquals(10, fos.getChannel().position());
        fos.close();
        fos = new FileOutputStream(tmpfile, true);
        assertEquals(10, fos.getChannel().position());
        fos.close();
    }

    protected void setUp() {
        bytes = new byte[10];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (f != null) {
            f.delete();
        }
        if (fis != null) {
            fis.close();
        }
        if (fos != null) {
            fos.close();
        }
    }
}
