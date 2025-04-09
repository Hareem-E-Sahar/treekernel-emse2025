package org.smslib.smsserver.interfaces.test;

import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import org.smslib.InboundMessage;
import org.smslib.smsserver.InterfaceTypes;
import org.smslib.smsserver.interfaces.Xml;
import junit.framework.TestCase;

public class TestXml extends TestCase {

    private static final String infId = "testXml";

    private Xml xml;

    private File baseDir;

    private File inDir;

    private File outDir;

    protected void tearDown() throws Exception {
        super.tearDown();
        xml.stop();
        deleteDirectory(baseDir);
    }

    /**
	 * Deletes the given file or directory (recursivley if needed)
	 * 
	 * @param path
	 *            The file/directory to delete
	 * @return
	 */
    private boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    protected void setUp() throws Exception {
        super.setUp();
        Properties props = new Properties();
        baseDir = new File(System.getProperty("java.io.tmpdir"), "xml-interface-test" + String.valueOf((int) (Math.random() * 1000 + 1000)));
        inDir = new File(baseDir, "in");
        inDir.mkdirs();
        outDir = new File(baseDir, "out");
        outDir.mkdirs();
        props.setProperty(infId + ".in", inDir.getAbsolutePath());
        props.setProperty(infId + ".out", outDir.getAbsolutePath());
        xml = new Xml(infId, props, null, InterfaceTypes.INOUTBOUND);
    }

    public void testMessagesReceived1() {
        List l = new ArrayList();
        l.add(new InboundMessage(new java.util.Date(), "orginator123", "test text 123", 0, "ML"));
        try {
            xml.MessagesReceived(l);
            assertEquals(2, inDir.list().length);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testMessagesReceived2() {
        List l = new ArrayList();
        l.add(new InboundMessage(new java.util.Date(), "orginator123", "test text 123", 0, "ML"));
        l.add(new InboundMessage(new java.util.Date(), "123orginator123", "123 test text 123", 0, "ML"));
        try {
            xml.MessagesReceived(l);
            assertEquals(3, inDir.list().length);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testGetMessagesToSend1() {
        try {
            assertEquals(0, xml.getMessagesToSend().size());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testGetMessagesToSend2() {
        try {
            File brokenFile = new File(outDir, "broken.xml");
            assertTrue(brokenFile.createNewFile());
            assertEquals(0, xml.getMessagesToSend().size());
            assertEquals(1, new File(outDir, Xml.sOutBrokenDirectory).list().length);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testGetMessagesToSend3() {
        try {
            File brokenFile1 = new File(outDir, "broken1.xml");
            assertTrue(brokenFile1.createNewFile());
            File brokenFile2 = new File(outDir, "broken1.xml");
            assertTrue(brokenFile2.createNewFile());
            assertEquals(0, xml.getMessagesToSend().size());
            assertEquals(2, new File(outDir, Xml.sOutBrokenDirectory).list().length);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testMarkMessage1() {
        try {
            xml.markMessage(null);
            assertTrue(true);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
