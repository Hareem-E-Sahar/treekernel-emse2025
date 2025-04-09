package org.charvolant.tmsnet.client;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.CRC32;
import org.charvolant.tmsnet.model.AbstractFile;
import org.charvolant.tmsnet.model.Directory;
import org.charvolant.tmsnet.model.FileEntry;
import org.charvolant.tmsnet.model.FileInfo;
import org.charvolant.tmsnet.model.TestOpenFile;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for the {@link DeleteTimerTransaction} class.
 *
 * @author Doug Palmer &lt;doug@charvolant.org&gt;
 *
 */
public class FetchFileTransactionTest extends TransactionTest<FetchFileTransaction> {

    private static final String LOCAL_NAME = "ATestName.mpg";

    /** The data files directory */
    private Directory dataFiles;

    /** The recording to delete */
    private FileEntry recording;

    /** The plain file to get */
    private FileEntry file;

    /** The temporary directory for storage */
    private File temp;

    /** The local file name */
    private File local;

    /**
   * @throws java.lang.Exception
   */
    @Before
    public void setUp() throws Exception {
        this.setUpVerboseLogging();
        this.temp = File.createTempFile("TMSNetTest", null);
        if (!this.temp.delete()) throw new IllegalStateException("Failed to delete temp file");
        if (!this.temp.mkdir()) throw new IllegalStateException("Failed to create temp dir");
        this.local = new File(this.temp, this.LOCAL_NAME);
        this.transactable = new TestTransactable();
        this.dataFiles = (Directory) this.transactable.getPvr().getState().getRoot().find("/DataFiles");
        for (AbstractFile<?> f : this.dataFiles.getContents()) {
            if (f.isRecording()) {
                FileEntry fe = (FileEntry) f;
                fe.getFile().setSize(131000 * 100 + 465);
                fe.getNavigation().setSize(131430);
                this.recording = new FileEntry(this.dataFiles, (FileInfo) fe.getFile().clone());
                this.recording.setInfo((FileInfo) fe.getInfo().clone());
                this.recording.setNavigation((FileInfo) fe.getNavigation().clone());
            } else if (f instanceof FileEntry) this.file = (FileEntry) f.clone();
        }
        Assert.assertNotNull(this.recording);
        Assert.assertNotNull(this.file);
        this.transaction = new FetchFileTransaction(this.recording, this.local);
        this.transaction.setClient(this.transactable);
        this.transactable.setTransaction(this.transaction);
        this.transactable.start();
    }

    /**
   * @throws java.lang.Exception
   */
    @After
    public void tearDown() throws Exception {
        this.transactable.stop();
        for (File sub : this.temp.listFiles()) sub.delete();
        this.temp.delete();
    }

    private void checkLocalFiles() throws Exception {
        File file;
        CRC32 crc;
        byte[] buffer = new byte[1024];
        int n;
        FileInputStream is;
        Assert.assertTrue(this.temp.listFiles().length > 0);
        for (TestOpenFile of : this.transactable.getPvr().getFiles().values()) {
            Assert.assertFalse(of.isOpen());
            file = new File(this.temp, this.LOCAL_NAME + of.getFile().getName().substring(this.recording.getName().length()));
            Assert.assertTrue(file.exists());
            Assert.assertEquals("Bad size for " + file, of.getFile().getSize(), file.length());
            crc = new CRC32();
            is = new FileInputStream(file);
            while ((n = is.read(buffer)) > 0) crc.update(buffer, 0, n);
            is.close();
            Assert.assertEquals(of.getCheckSum().getValue(), crc.getValue());
        }
    }

    /**
   * Test method for {@link org.charvolant.tmsnet.client.Transaction#execute()}.
   */
    @Test
    public void testExecute1() throws Exception {
        this.transaction.execute();
        this.waitForEnd();
        Assert.assertEquals("Committed", this.transaction.getStateName());
        Assert.assertFalse(this.transaction.isIdle());
        Assert.assertTrue(this.transaction.isStopped());
        this.checkLocalFiles();
    }

    /**
   * Test method for {@link org.charvolant.tmsnet.client.Transaction#execute()}.
   */
    @Test
    public void testCancel() throws Exception {
        this.transaction.execute();
        this.waitForEnd(20);
        this.transaction.cancel();
        this.waitForEnd();
        Assert.assertEquals("Cancelled", this.transaction.getStateName());
        Assert.assertFalse(this.transaction.isIdle());
        Assert.assertTrue(this.transaction.isStopped());
    }
}
