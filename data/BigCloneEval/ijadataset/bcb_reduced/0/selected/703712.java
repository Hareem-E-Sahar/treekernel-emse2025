package com.goodcodeisbeautiful.archtea.io.vfs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import junit.framework.TestSuite;
import com.goodcodeisbeautiful.test.util.CommonTestCase;

/**
 * @author hata
 *
 */
public class CommonsVFSDataContainerAdapterTestCase extends CommonTestCase {

    private static final String FILE_TEST_1_TAR_GZ = "test-1.0.tar.gz";

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static TestSuite suite() {
        return new TestSuite(CommonsVFSDataContainerAdapterTestCase.class);
    }

    private CommonsVFSDataContainerAdapter m_adapter;

    protected List getSetupFilenames() {
        List l = new LinkedList();
        l.add(FILE_TEST_1_TAR_GZ);
        return l;
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     * @throws Exception
     */
    protected void setUp() throws Exception {
        super.setUp();
        String path = getWorkDir() + File.separator + FILE_TEST_1_TAR_GZ;
        FileSystemManager mgr = VFS.getManager();
        FileObject o = mgr.resolveFile(new File(path).toURL().toExternalForm());
        m_adapter = new CommonsVFSDataContainerAdapter(o);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        m_adapter = null;
        cleanWorkFiles();
    }

    public CommonsVFSDataContainerAdapterTestCase(String name) {
        super(name);
    }

    public void testCommonsVFSDataContainerAdapter() throws Exception {
        assertNotNull(m_adapter);
    }

    public void testGetName() throws Exception {
        assertNotNull(m_adapter);
        assertEquals(FILE_TEST_1_TAR_GZ, m_adapter.getName());
    }

    public void testGetInputStream() throws Exception {
        ByteArrayOutputStream desireOut = new ByteArrayOutputStream();
        ByteArrayOutputStream adapterOut = new ByteArrayOutputStream();
        File f = new File(getWorkDir() + File.separator + FILE_TEST_1_TAR_GZ);
        assertNotNull(m_adapter);
        InputStream in = null;
        byte[] buff = new byte[512];
        try {
            in = new FileInputStream(f);
            int len = in.read(buff);
            while (len != -1) {
                desireOut.write(buff, 0, len);
                len = in.read(buff);
            }
            in.close();
            in = null;
            in = new FileInputStream(f);
            len = in.read(buff);
            while (len != -1) {
                adapterOut.write(buff, 0, len);
                len = in.read(buff);
            }
            in.close();
            in = null;
            byte[] desireBytes = desireOut.toByteArray();
            byte[] adapterBytes = adapterOut.toByteArray();
            assertEquals(desireBytes.length, adapterBytes.length);
            for (int i = 0; i < desireBytes.length; i++) {
                assertEquals("Byte[" + i + "]", desireBytes[i], adapterBytes[i]);
            }
        } finally {
            if (in != null) in.close();
        }
    }
}
