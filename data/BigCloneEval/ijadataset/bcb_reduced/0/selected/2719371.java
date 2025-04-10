package org.apache.harmony.security.tests.java.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.apache.harmony.security.tests.support.MDGoldenData;
import junit.framework.TestCase;

/**
 * Tests for fields and methods of class <code>DigestInputStream</code>
 * 
 */
public class DigestInputStreamTest extends TestCase {

    /**
     * Message digest algorithm name used during testing
     */
    private static final String algorithmName[] = { "SHA-1", "SHA", "SHA1", "SHA-256", "SHA-384", "SHA-512", "MD5" };

    /**
     * Chunk size for read(byte, off, len) tests
     */
    private static final int CHUNK_SIZE = 32;

    /**
     * Test message for digest computations
     */
    private static final byte[] myMessage = MDGoldenData.getMessage();

    /**
     * The length of test message
     */
    private static final int MY_MESSAGE_LEN = myMessage.length;

    /**
     * Constructor for DigestInputStreamTest.
     * @param name
     */
    public DigestInputStreamTest(String name) {
        super(name);
    }

    /**
     * Test #1 for <code>DigestInputStream</code> constructor<br>
     * 
     * Assertion: creates new <code>DigestInputStream</code> instance
     * using valid parameters (both non <code>null</code>)
     *
     * @throws NoSuchAlgorithmException
     */
    public final void testDigestInputStream01() {
        for (int i = 0; i < algorithmName.length; i++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[i]);
                InputStream is = new ByteArrayInputStream(myMessage);
                InputStream dis = new DigestInputStream(is, md);
                assertTrue(dis instanceof DigestInputStream);
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test #2 for <code>DigestInputStream</code> constructor<br>
     * 
     * Assertion: creates new <code>DigestInputStream</code> instance
     * using valid parameters (both <code>null</code>)
     */
    public final void testDigestInputStream02() {
        InputStream dis = new DigestInputStream(null, null);
        assertTrue(dis instanceof DigestInputStream);
    }

    /**
     * Test #1 for <code>read()</code> method<br>
     * 
     * Assertion: returns the byte read<br>
     * Assertion: updates associated digest<br>
     */
    public final void testRead01() throws IOException {
        for (int ii = 0; ii < algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                InputStream is = new ByteArrayInputStream(myMessage);
                DigestInputStream dis = new DigestInputStream(is, md);
                for (int i = 0; i < MY_MESSAGE_LEN; i++) {
                    assertTrue("retval", ((byte) dis.read() == myMessage[i]));
                }
                assertTrue("update", Arrays.equals(dis.getMessageDigest().digest(), MDGoldenData.getDigest(algorithmName[ii])));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test #2 for <code>read()</code> method<br>
     * 
     * Assertion: returns -1 if EOS had been
     * reached but not read before method call<br>
     * 
     * Assertion: must not update digest if EOS had been
     * reached but not read before method call<br>
     */
    public final void testRead02() throws IOException {
        for (int ii = 0; ii < algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                InputStream is = new ByteArrayInputStream(myMessage);
                DigestInputStream dis = new DigestInputStream(is, md);
                for (int i = 0; i < MY_MESSAGE_LEN; i++) {
                    dis.read();
                }
                assertEquals("retval1", -1, dis.read());
                assertEquals("retval2", -1, dis.read());
                assertEquals("retval3", -1, dis.read());
                assertTrue("update", Arrays.equals(dis.getMessageDigest().digest(), MDGoldenData.getDigest(algorithmName[ii])));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test #3 for <code>read()</code> method<br>
     * Test #1 for <code>on(boolean)</code> method<br>
     * 
     * Assertion: <code>read()</code> must not update digest if it is off<br>
     * Assertion: <code>on(boolean)</code> turns digest functionality on
     * (if <code>true</code> passed as a parameter) or off (if <code>false</code>
     *  passed)
     */
    public final void testRead03() throws IOException {
        for (int ii = 0; ii < algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                InputStream is = new ByteArrayInputStream(myMessage);
                DigestInputStream dis = new DigestInputStream(is, md);
                dis.on(false);
                for (int i = 0; i < MY_MESSAGE_LEN; i++) {
                    dis.read();
                }
                assertTrue(Arrays.equals(dis.getMessageDigest().digest(), MDGoldenData.getDigest(algorithmName[ii] + "_NU")));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test #4 for <code>read()</code> method<br>
     * 
     * Assertion: broken <code>DigestInputStream</code>instance: 
     * <code>InputStream</code> not set. <code>read()</code> must
     * not work
     */
    public final void testRead04() throws IOException {
        for (int ii = 0; ii < algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                DigestInputStream dis = new DigestInputStream(null, md);
                try {
                    for (int i = 0; i < MY_MESSAGE_LEN; i++) {
                        dis.read();
                    }
                } catch (Exception e) {
                    return;
                }
                fail("InputStream not set. read() must not work");
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test #5 for <code>read()</code> method<br>
     * 
     * Assertion: broken <code>DigestInputStream</code>instance: 
     * associated <code>MessageDigest</code> not set.
     * <code>read()</code> must not work when digest
     * functionality is on
     */
    public final void testRead05() {
        InputStream is = new ByteArrayInputStream(myMessage);
        DigestInputStream dis = new DigestInputStream(is, null);
        try {
            for (int i = 0; i < MY_MESSAGE_LEN; i++) {
                dis.read();
            }
            fail("read() must not work when digest functionality is on");
        } catch (Exception e) {
        }
    }

    /**
     * Test #6 for <code>read()</code> method<br>
     * Test #2 for <code>on(boolean)</code> method<br>
     * 
     * Assertion: broken <code>DigestInputStream</code>instance:
     * associated <code>MessageDigest</code> not set.
     * <code>read()</code> must work when digest
     * functionality is off
     */
    public final void testRead06() throws IOException {
        InputStream is = new ByteArrayInputStream(myMessage);
        DigestInputStream dis = new DigestInputStream(is, null);
        dis.on(false);
        for (int i = 0; i < MY_MESSAGE_LEN; i++) {
            assertTrue((byte) dis.read() == myMessage[i]);
        }
    }

    /**
     * Test #1 for <code>read(byte[],int,int)</code> method<br>
     * 
     * Assertion: returns the number of bytes read<br>
     * 
     * Assertion: put bytes read into specified array at specified offset<br>
     * 
     * Assertion: updates associated digest<br>
     */
    public final void testReadbyteArrayintint01() throws IOException {
        for (int ii = 0; ii < algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                InputStream is = new ByteArrayInputStream(myMessage);
                DigestInputStream dis = new DigestInputStream(is, md);
                byte[] bArray = new byte[MY_MESSAGE_LEN];
                assertTrue("retval", dis.read(bArray, 0, bArray.length) == MY_MESSAGE_LEN);
                assertTrue("bArray", Arrays.equals(myMessage, bArray));
                assertTrue("update", Arrays.equals(dis.getMessageDigest().digest(), MDGoldenData.getDigest(algorithmName[ii])));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test #2 for <code>read(byte[],int,int)</code> method<br>
     * 
     * Assertion: returns the number of bytes read<br>
     * 
     * Assertion: put bytes read into specified array at specified offset<br>
     * 
     * Assertion: updates associated digest<br>
     */
    public final void testReadbyteArrayintint02() throws IOException {
        assertEquals(0, MY_MESSAGE_LEN % CHUNK_SIZE);
        for (int ii = 0; ii < algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                InputStream is = new ByteArrayInputStream(myMessage);
                DigestInputStream dis = new DigestInputStream(is, md);
                byte[] bArray = new byte[MY_MESSAGE_LEN];
                for (int i = 0; i < MY_MESSAGE_LEN / CHUNK_SIZE; i++) {
                    assertTrue("retval", dis.read(bArray, i * CHUNK_SIZE, CHUNK_SIZE) == CHUNK_SIZE);
                }
                assertTrue("bArray", Arrays.equals(myMessage, bArray));
                assertTrue("update", Arrays.equals(dis.getMessageDigest().digest(), MDGoldenData.getDigest(algorithmName[ii])));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test #3 for <code>read(byte[],int,int)</code> method<br>
     * 
     * Assertion: returns the number of bytes read<br>
     * 
     * Assertion: put bytes read into specified array at specified offset<br>
     * 
     * Assertion: updates associated digest<br>
     */
    public final void testReadbyteArrayintint03() throws IOException {
        assertTrue(MY_MESSAGE_LEN % (CHUNK_SIZE + 1) != 0);
        for (int ii = 0; ii < algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                InputStream is = new ByteArrayInputStream(myMessage);
                DigestInputStream dis = new DigestInputStream(is, md);
                byte[] bArray = new byte[MY_MESSAGE_LEN];
                for (int i = 0; i < MY_MESSAGE_LEN / (CHUNK_SIZE + 1); i++) {
                    assertTrue("retval1", dis.read(bArray, i * (CHUNK_SIZE + 1), CHUNK_SIZE + 1) == CHUNK_SIZE + 1);
                }
                assertTrue("retval2", dis.read(bArray, MY_MESSAGE_LEN / (CHUNK_SIZE + 1) * (CHUNK_SIZE + 1), MY_MESSAGE_LEN % (CHUNK_SIZE + 1)) == (MY_MESSAGE_LEN % (CHUNK_SIZE + 1)));
                assertTrue("bArray", Arrays.equals(myMessage, bArray));
                assertTrue("update", Arrays.equals(dis.getMessageDigest().digest(), MDGoldenData.getDigest(algorithmName[ii])));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test #4 for <code>read(byte[],int,int)</code> method<br>
     * 
     * Assertion: returns the number of bytes read<br>
     * 
     * Assertion: updates associated digest<br>
     */
    public final void testReadbyteArrayintint04() throws IOException {
        for (int ii = 0; ii < algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                InputStream is = new ByteArrayInputStream(myMessage);
                DigestInputStream dis = new DigestInputStream(is, md);
                byte[] bArray = new byte[MY_MESSAGE_LEN];
                dis.read(bArray, 0, bArray.length);
                assertEquals("retval1", -1, dis.read(bArray, 0, 1));
                assertEquals("retval2", -1, dis.read(bArray, 0, bArray.length));
                assertEquals("retval3", -1, dis.read(bArray, 0, 1));
                assertTrue("update", Arrays.equals(dis.getMessageDigest().digest(), MDGoldenData.getDigest(algorithmName[ii])));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test #5 for <code>read(byte[],int,int)</code> method<br>
     * 
     * Assertion: returns the number of bytes read<br>
     * 
     * Assertion: put bytes read into specified array at specified offset<br>
     * 
     * Assertion: does not update associated digest if
     * digest functionality is off<br>
     */
    public final void testReadbyteArrayintint05() throws IOException {
        assertEquals(0, MY_MESSAGE_LEN % CHUNK_SIZE);
        for (int ii = 0; ii < algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                InputStream is = new ByteArrayInputStream(myMessage);
                DigestInputStream dis = new DigestInputStream(is, md);
                byte[] bArray = new byte[MY_MESSAGE_LEN];
                dis.on(false);
                for (int i = 0; i < MY_MESSAGE_LEN / CHUNK_SIZE; i++) {
                    dis.read(bArray, i * CHUNK_SIZE, CHUNK_SIZE);
                }
                assertTrue(Arrays.equals(dis.getMessageDigest().digest(), MDGoldenData.getDigest(algorithmName[ii] + "_NU")));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test for <code>getMessageDigest()</code> method<br>
     * 
     * Assertion: returns associated message digest<br>
     */
    public final void testGetMessageDigest() {
        for (int ii = 0; ii < algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                DigestInputStream dis = new DigestInputStream(null, md);
                assertTrue(dis.getMessageDigest() == md);
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test for <code>setMessageDigest()</code> method<br>
     * 
     * Assertion: set associated message digest<br>
     */
    public final void testSetMessageDigest() {
        for (int ii = 0; ii < algorithmName.length; ii++) {
            try {
                DigestInputStream dis = new DigestInputStream(null, null);
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                dis.setMessageDigest(md);
                assertTrue(dis.getMessageDigest() == md);
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test for <code>on()</code> method<br>
     * Assertion: turns digest functionality on or off
     */
    public final void testOn() throws IOException {
        for (int ii = 0; ii < algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                InputStream is = new ByteArrayInputStream(myMessage);
                DigestInputStream dis = new DigestInputStream(is, md);
                dis.on(false);
                for (int i = 0; i < MY_MESSAGE_LEN - 1; i++) {
                    dis.read();
                }
                dis.on(true);
                dis.read();
                byte[] digest = dis.getMessageDigest().digest();
                assertFalse(Arrays.equals(digest, MDGoldenData.getDigest(algorithmName[ii])) || Arrays.equals(digest, MDGoldenData.getDigest(algorithmName[ii] + "_NU")));
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }

    /**
     * Test for <code>toString()</code> method<br>
     * Assertion: returns <code>String</code> representation of this object
     */
    public final void testToString() {
        for (int ii = 0; ii < algorithmName.length; ii++) {
            try {
                MessageDigest md = MessageDigest.getInstance(algorithmName[ii]);
                InputStream is = new ByteArrayInputStream(myMessage);
                DigestInputStream dis = new DigestInputStream(is, md);
                assertNotNull(dis.toString());
                return;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        fail(getName() + ": no MessageDigest algorithms available - test not performed");
    }
}
