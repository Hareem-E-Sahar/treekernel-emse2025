package org.apache.harmony.luni.tests.java.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Permission;
import junit.framework.TestCase;
import tests.support.Support_PlatformFile;

public class FileInputStreamTest extends TestCase {

    public String fileName;

    private java.io.InputStream is;

    byte[] ibuf = new byte[4096];

    public String fileString = "Test_All_Tests\nTest_java_io_BufferedInputStream\nTest_java_io_BufferedOutputStream\nTest_java_io_ByteArrayInputStream\nTest_java_io_ByteArrayOutputStream\nTest_java_io_DataInputStream\nTest_java_io_File\nTest_java_io_FileDescriptor\nTest_FileInputStream\nTest_java_io_FileNotFoundException\nTest_java_io_FileOutputStream\nTest_java_io_FilterInputStream\nTest_java_io_FilterOutputStream\nTest_java_io_InputStream\nTest_java_io_IOException\nTest_java_io_OutputStream\nTest_java_io_PrintStream\nTest_java_io_RandomAccessFile\nTest_java_io_SyncFailedException\nTest_java_lang_AbstractMethodError\nTest_java_lang_ArithmeticException\nTest_java_lang_ArrayIndexOutOfBoundsException\nTest_java_lang_ArrayStoreException\nTest_java_lang_Boolean\nTest_java_lang_Byte\nTest_java_lang_Character\nTest_java_lang_Class\nTest_java_lang_ClassCastException\nTest_java_lang_ClassCircularityError\nTest_java_lang_ClassFormatError\nTest_java_lang_ClassLoader\nTest_java_lang_ClassNotFoundException\nTest_java_lang_CloneNotSupportedException\nTest_java_lang_Double\nTest_java_lang_Error\nTest_java_lang_Exception\nTest_java_lang_ExceptionInInitializerError\nTest_java_lang_Float\nTest_java_lang_IllegalAccessError\nTest_java_lang_IllegalAccessException\nTest_java_lang_IllegalArgumentException\nTest_java_lang_IllegalMonitorStateException\nTest_java_lang_IllegalThreadStateException\nTest_java_lang_IncompatibleClassChangeError\nTest_java_lang_IndexOutOfBoundsException\nTest_java_lang_InstantiationError\nTest_java_lang_InstantiationException\nTest_java_lang_Integer\nTest_java_lang_InternalError\nTest_java_lang_InterruptedException\nTest_java_lang_LinkageError\nTest_java_lang_Long\nTest_java_lang_Math\nTest_java_lang_NegativeArraySizeException\nTest_java_lang_NoClassDefFoundError\nTest_java_lang_NoSuchFieldError\nTest_java_lang_NoSuchMethodError\nTest_java_lang_NullPointerException\nTest_java_lang_Number\nTest_java_lang_NumberFormatException\nTest_java_lang_Object\nTest_java_lang_OutOfMemoryError\nTest_java_lang_RuntimeException\nTest_java_lang_SecurityManager\nTest_java_lang_Short\nTest_java_lang_StackOverflowError\nTest_java_lang_String\nTest_java_lang_StringBuffer\nTest_java_lang_StringIndexOutOfBoundsException\nTest_java_lang_System\nTest_java_lang_Thread\nTest_java_lang_ThreadDeath\nTest_java_lang_ThreadGroup\nTest_java_lang_Throwable\nTest_java_lang_UnknownError\nTest_java_lang_UnsatisfiedLinkError\nTest_java_lang_VerifyError\nTest_java_lang_VirtualMachineError\nTest_java_lang_vm_Image\nTest_java_lang_vm_MemorySegment\nTest_java_lang_vm_ROMStoreException\nTest_java_lang_vm_VM\nTest_java_lang_Void\nTest_java_net_BindException\nTest_java_net_ConnectException\nTest_java_net_DatagramPacket\nTest_java_net_DatagramSocket\nTest_java_net_DatagramSocketImpl\nTest_java_net_InetAddress\nTest_java_net_NoRouteToHostException\nTest_java_net_PlainDatagramSocketImpl\nTest_java_net_PlainSocketImpl\nTest_java_net_Socket\nTest_java_net_SocketException\nTest_java_net_SocketImpl\nTest_java_net_SocketInputStream\nTest_java_net_SocketOutputStream\nTest_java_net_UnknownHostException\nTest_java_util_ArrayEnumerator\nTest_java_util_Date\nTest_java_util_EventObject\nTest_java_util_HashEnumerator\nTest_java_util_Hashtable\nTest_java_util_Properties\nTest_java_util_ResourceBundle\nTest_java_util_tm\nTest_java_util_Vector\n";

    /**
     * @tests java.io.FileInputStream#FileInputStream(java.io.File)
     */
    public void test_ConstructorLjava_io_File() throws IOException {
        java.io.File f = new File(fileName);
        is = new FileInputStream(f);
        is.close();
    }

    /**
     * @tests java.io.FileInputStream#FileInputStream(java.io.FileDescriptor)
     */
    public void test_ConstructorLjava_io_FileDescriptor() throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        FileInputStream fis = new FileInputStream(fos.getFD());
        fos.close();
        fis.close();
    }

    /**
     * @tests java.io.FileInputStream#FileInputStream(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() throws IOException {
        is = new FileInputStream(fileName);
        is.close();
    }

    /**
     * @tests java.io.FileInputStream#FileInputStream(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String_I() throws IOException {
        try {
            is = new FileInputStream("");
            fail("should throw FileNotFoundException.");
        } catch (FileNotFoundException e) {
        } finally {
            if (is != null) {
                is.close();
            }
        }
        try {
            is = new FileInputStream(new File(""));
            fail("should throw FileNotFoundException.");
        } catch (FileNotFoundException e) {
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * @tests java.io.FileInputStream#available()
     */
    public void test_available() throws IOException {
        try {
            is = new FileInputStream(fileName);
            assertTrue("Returned incorrect number of available bytes", is.available() == fileString.length());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * @tests java.io.FileInputStream#close()
     */
    public void test_close() throws IOException {
        is = new FileInputStream(fileName);
        is.close();
        try {
            is.read();
            fail("Able to read from closed stream");
        } catch (IOException e) {
        }
    }

    /**
     * @tests java.io.FileInputStream#getFD()
     */
    public void test_getFD() throws IOException {
        FileInputStream fis = new FileInputStream(fileName);
        assertTrue("Returned invalid fd", fis.getFD().valid());
        fis.close();
        assertTrue("Returned invalid fd", !fis.getFD().valid());
    }

    /**
     * @tests java.io.FileInputStream#read()
     */
    public void test_read() throws IOException {
        InputStreamReader isr = new InputStreamReader(new FileInputStream(fileName));
        int c = isr.read();
        isr.close();
        assertTrue("read returned incorrect char", c == fileString.charAt(0));
    }

    /**
     * @tests java.io.FileInputStream#read(byte[])
     */
    public void test_read$B() throws IOException {
        byte[] buf1 = new byte[100];
        is = new FileInputStream(fileName);
        is.skip(3000);
        is.read(buf1);
        is.close();
        assertTrue("Failed to read correct data", new String(buf1, 0, buf1.length).equals(fileString.substring(3000, 3100)));
    }

    /**
     * @tests java.io.FileInputStream#read(byte[], int, int)
     */
    public void test_read$BII() throws IOException {
        byte[] buf1 = new byte[100];
        is = new FileInputStream(fileName);
        is.skip(3000);
        is.read(buf1, 0, buf1.length);
        is.close();
        assertTrue("Failed to read correct data", new String(buf1, 0, buf1.length).equals(fileString.substring(3000, 3100)));
        File file = new File("FileInputStream.tmp");
        file.createNewFile();
        file.deleteOnExit();
        FileInputStream in = new FileInputStream(file);
        try {
            in.read(null, 0, 0);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        } finally {
            in.close();
            file.delete();
        }
    }

    /**
     * @tests java.io.FileInputStream#read(byte[], int, int)
     */
    public void test_read_$BII_IOException() throws IOException {
        byte[] buf = new byte[1000];
        try {
            is = new FileInputStream(fileName);
            is.read(buf, -1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        } finally {
            is.close();
        }
        try {
            is = new FileInputStream(fileName);
            is.read(buf, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        } finally {
            is.close();
        }
        try {
            is = new FileInputStream(fileName);
            is.read(buf, -1, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        } finally {
            is.close();
        }
        try {
            is = new FileInputStream(fileName);
            is.read(buf, 0, 1001);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        } finally {
            is.close();
        }
        try {
            is = new FileInputStream(fileName);
            is.read(buf, 1001, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        } finally {
            is.close();
        }
        try {
            is = new FileInputStream(fileName);
            is.read(buf, 500, 501);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        } finally {
            is.close();
        }
        try {
            is = new FileInputStream(fileName);
            is.close();
            is.read(buf, 0, 100);
            fail("should throw IOException");
        } catch (IOException e) {
        } finally {
            is.close();
        }
        try {
            is = new FileInputStream(fileName);
            is.close();
            is.read(buf, 0, 0);
        } finally {
            is.close();
        }
    }

    /**
     * @tests java.io.FileInputStream#read(byte[], int, int)
     */
    public void test_read_$BII_NullPointerException() throws IOException {
        byte[] buf = null;
        try {
            is = new FileInputStream(fileName);
            is.read(buf, -1, 0);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
        } finally {
            is.close();
        }
    }

    /**
     * @tests java.io.FileInputStream#read(byte[], int, int)
     */
    public void test_read_$BII_IndexOutOfBoundsException() throws IOException {
        byte[] buf = new byte[1000];
        try {
            is = new FileInputStream(fileName);
            is.close();
            is.read(buf, -1, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        } finally {
            is.close();
        }
    }

    /**
     * @tests java.io.FileInputStream#skip(long)
     */
    public void test_skipJ() throws IOException {
        byte[] buf1 = new byte[10];
        is = new FileInputStream(fileName);
        is.skip(1000);
        is.read(buf1, 0, buf1.length);
        is.close();
        assertTrue("Failed to skip to correct position", new String(buf1, 0, buf1.length).equals(fileString.substring(1000, 1010)));
    }

    /**
     * @tests java.io.FileInputStream#read(byte[], int, int))
     */
    public void test_regressionNNN() throws IOException {
        FileInputStream fis = new FileInputStream(fileName);
        try {
            fis.read(new byte[1], -1, 1);
            fail("IndexOutOfBoundsException must be thrown if off <0");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            fis.read(new byte[1], 0, -1);
            fail("IndexOutOfBoundsException must be thrown if len <0");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            fis.read(new byte[1], 0, 5);
            fail("IndexOutOfBoundsException must be thrown if off+len > b.length");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            fis.read(new byte[10], Integer.MAX_VALUE, 5);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            fis.read(new byte[10], 5, Integer.MAX_VALUE);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException e) {
        }
        fis.close();
    }

    /**
     * @tests java.io.FileInputStream#FileInputStream(String)
     */
    public void test_Constructor_LString_WithSecurityManager() throws IOException {
        SecurityManager old = System.getSecurityManager();
        try {
            MockSecurityManager msm = new MockSecurityManager();
            System.setSecurityManager(msm);
            new FileInputStream((String) null);
            fail("should throw SecurityException");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(old);
        }
    }

    /**
     * @tests java.io.FileInputStream#skip(long)
     */
    public void test_skipNegativeArgumentJ() throws IOException {
        FileInputStream fis = new FileInputStream(fileName);
        try {
            fis.skip(-5);
            fail("IOException must be thrown if number of bytes to skip <0");
        } catch (IOException e) {
        } finally {
            fis.close();
        }
    }

    public void test_getChannel() throws Exception {
        FileInputStream fis = new FileInputStream(fileName);
        assertEquals(0, fis.getChannel().position());
        int r;
        int count = 1;
        while ((r = fis.read()) != -1) {
            assertEquals(count++, fis.getChannel().position());
        }
        fis.close();
        try {
            fis.getChannel().position();
            fail("should throw ClosedChannelException");
        } catch (java.nio.channels.ClosedChannelException e) {
        }
        fis = new FileInputStream(fileName);
        assertEquals(0, fis.getChannel().position());
        byte[] bs = new byte[10];
        r = fis.read(bs);
        assertEquals(10, fis.getChannel().position());
        fis.close();
        fis = new FileInputStream(fileName);
        assertEquals(0, fis.getChannel().position());
        bs = new byte[10];
        fis.skip(100);
        assertEquals(100, fis.getChannel().position());
        r = fis.read(bs);
        assertEquals(110, fis.getChannel().position());
        fis.close();
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() throws IOException {
        fileName = System.getProperty("user.dir");
        String separator = System.getProperty("file.separator");
        if (fileName.charAt(fileName.length() - 1) == separator.charAt(0)) fileName = Support_PlatformFile.getNewPlatformFile(fileName, "input.tst"); else fileName = Support_PlatformFile.getNewPlatformFile(fileName + separator, "input.tst");
        java.io.OutputStream fos = new java.io.FileOutputStream(fileName);
        fos.write(fileString.getBytes());
        fos.close();
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
        new File(fileName).delete();
    }
}

class MockSecurityManager extends SecurityManager {

    public void checkPermission(Permission permission) {
        if (permission instanceof FilePermission) {
            if (permission.getActions().indexOf("read") == 0) throw new SecurityException();
        }
    }

    public void checkRead(String file) {
        if (null == file) {
            file = "";
        }
        checkPermission(new FilePermission(file, "read"));
    }
}
