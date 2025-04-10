package org.apache.harmony.security.tests.java.security;

import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;
import org.apache.harmony.security.tests.support.MyMessageDigest1;
import org.apache.harmony.security.tests.support.MyMessageDigest2;
import junit.framework.TestCase;

/**
 * Tests for <code>MessageDigest</code> constructor and methods
 * 
 */
public class MessageDigest_Impl2Test extends TestCase {

    /**
	 * Provider
	 */
    Provider p;

    protected void setUp() throws Exception {
        super.setUp();
        p = new MyProvider();
        Security.insertProviderAt(p, 1);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        Security.removeProvider(p.getName());
    }

    public void testGetInstanceString1() throws Exception {
        MessageDigest md1 = MessageDigest.getInstance("ABC");
        checkMD1(md1, p);
    }

    public void testGetInstanceString2() throws Exception {
        MessageDigest md2 = MessageDigest.getInstance("CBA");
        checkMD2(md2, p);
    }

    public void testGetInstanceStringString1() throws Exception {
        MessageDigest md1 = MessageDigest.getInstance("ABC", "MyProvider");
        checkMD1(md1, p);
    }

    public void testGetInstanceStringString2() throws Exception {
        MessageDigest md2 = MessageDigest.getInstance("CBA", "MyProvider");
        checkMD2(md2, p);
    }

    public void testGetInstanceStringProvider1() throws Exception {
        Provider p = new MyProvider();
        MessageDigest md1 = MessageDigest.getInstance("ABC", p);
        checkMD1(md1, p);
    }

    public void testGetInstanceStringProvider2() throws Exception {
        Provider p = new MyProvider();
        MessageDigest md2 = MessageDigest.getInstance("CBA", p);
        checkMD2(md2, p);
    }

    public void testToString() {
        MyMessageDigest1 md = new MyMessageDigest1("ABC");
        assertEquals("incorrect result", "MESSAGE DIGEST ABC", md.toString());
    }

    private void checkMD1(MessageDigest md1, Provider p) throws Exception {
        byte[] b = { 1, 2, 3, 4, 5 };
        assertTrue("getInstance() failed", md1 instanceof MyMessageDigest1);
        assertEquals("getProvider() failed", p, md1.getProvider());
        assertEquals("getAlgorithm() failed", "ABC", md1.getAlgorithm());
        md1.update((byte) 1);
        md1.update(b, 1, 4);
        assertTrue("update failed", ((MyMessageDigest1) md1).runEngineUpdate1);
        assertTrue("update failed", ((MyMessageDigest1) md1).runEngineUpdate2);
        assertEquals("incorrect digest result", 0, md1.digest().length);
        assertEquals("getProvider() failed", 0, md1.digest(b, 2, 3));
        assertTrue("digest failed", ((MyMessageDigest1) md1).runEngineDigest);
        md1.reset();
        assertTrue("reset failed", ((MyMessageDigest1) md1).runEngineReset);
        assertEquals("getDigestLength() failed", 0, md1.getDigestLength());
        try {
            md1.clone();
            fail("No expected CloneNotSupportedException");
        } catch (CloneNotSupportedException e) {
        }
    }

    private void checkMD2(MessageDigest md2, Provider p) throws Exception {
        byte[] b = { 1, 2, 3, 4, 5 };
        assertEquals("getProvider() failed", p, md2.getProvider());
        assertEquals("getAlgorithm() failed", "CBA", md2.getAlgorithm());
        md2.update((byte) 1);
        md2.update(b, 1, 3);
        assertTrue("update failed", MyMessageDigest2.runEngineUpdate1);
        assertTrue("update failed", MyMessageDigest2.runEngineUpdate2);
        assertEquals("incorrect digest result", 0, md2.digest().length);
        assertEquals("getProvider() failed", 0, md2.digest(b, 2, 3));
        assertTrue("digest failed", MyMessageDigest2.runEngineDigest);
        md2.reset();
        assertTrue("reset failed", MyMessageDigest2.runEngineReset);
        assertEquals("getDigestLength() failed", 0, md2.getDigestLength());
        try {
            md2.clone();
            fail("No expected CloneNotSupportedException");
        } catch (CloneNotSupportedException e) {
        }
    }

    private class MyProvider extends Provider {

        MyProvider() {
            super("MyProvider", 1.0, "Provider for testing");
            put("MessageDigest.ABC", "org.apache.harmony.security.tests.support.MyMessageDigest1");
            put("MessageDigest.CBA", "org.apache.harmony.security.tests.support.MyMessageDigest2");
        }

        MyProvider(String name, double version, String info) {
            super(name, version, info);
        }
    }
}
