package org.ccnx.ccn.test.io;

import java.io.IOException;
import junit.framework.Assert;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.KeyManager;
import org.ccnx.ccn.impl.support.DataUtils;
import org.ccnx.ccn.io.CCNInputStream;
import org.ccnx.ccn.io.CCNVersionedInputStream;
import org.ccnx.ccn.profiles.SegmentationProfile;
import org.ccnx.ccn.profiles.VersioningProfile;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.ContentObject;
import org.ccnx.ccn.test.CCNTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class PipelineTest {

    public static CCNTestHelper testHelper = new CCNTestHelper(PipelineTest.class);

    public static ContentName testName;

    public static CCNHandle readHandle;

    public static CCNHandle writeHandle;

    private static CCNInputStream istream;

    private static CCNVersionedInputStream vistream;

    private static long segments = 20;

    private static long bytesWritten;

    private static byte[] firstDigest = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        readHandle = CCNHandle.open();
        writeHandle = CCNHandle.open();
        ContentName namespace = testHelper.getTestNamespace("pipelineTest");
        testName = ContentName.fromNative(namespace, "PipelineSegments");
        testName = VersioningProfile.addVersion(testName);
        try {
            putSegments();
        } catch (Exception e) {
            System.err.println("failed to put objects for pipeline test: " + e.getMessage());
            Assert.fail();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        readHandle.close();
        writeHandle.close();
        KeyManager.closeDefaultKeyManager();
    }

    private static void putSegments() throws Exception {
        ContentName segment;
        ContentObject object;
        long lastMarkerTest = 1;
        bytesWritten = 0;
        byte[] toWrite;
        for (int i = 0; i < segments; i++) {
            if (i > 0) lastMarkerTest = segments - 1;
            segment = SegmentationProfile.segmentName(testName, i);
            toWrite = ("this is segment " + i + " of " + segments).getBytes();
            bytesWritten = bytesWritten + toWrite.length;
            object = ContentObject.buildContentObject(segment, toWrite, null, null, SegmentationProfile.getSegmentNumberNameComponent(lastMarkerTest));
            if (i == 0) {
                firstDigest = object.digest();
            }
            writeHandle.put(object);
        }
        System.out.println("wrote " + bytesWritten + " bytes");
    }

    @Test
    public void testGetAllSegmentsFromPipeline() {
        long received = 0;
        byte[] bytes = new byte[1024];
        try {
            istream = new CCNInputStream(testName, readHandle);
        } catch (IOException e1) {
            System.err.println("failed to open stream for pipeline test: " + e1.getMessage());
            Assert.fail();
        }
        while (!istream.eof()) {
            try {
                received += istream.read(bytes);
            } catch (IOException e) {
                System.err.println("failed to read segments: " + e.getMessage());
                Assert.fail();
            }
        }
        System.out.println("read " + received + " from stream");
        Assert.assertTrue(received == bytesWritten);
    }

    @Test
    public void testSkipWithPipeline() {
        long received = 0;
        byte[] bytes = new byte[100];
        boolean skipDone = false;
        try {
            istream = new CCNInputStream(testName, readHandle);
        } catch (IOException e1) {
            System.err.println("Failed to get new stream: " + e1.getMessage());
            Assert.fail();
        }
        while (!istream.eof()) {
            try {
                System.out.println("Read so far: " + received);
                if (received > 0 && received < 250 && !skipDone) {
                    istream.skip(100);
                    skipDone = true;
                }
                received += istream.read(bytes);
            } catch (IOException e) {
                System.err.println("failed to read segments: " + e.getMessage());
                Assert.fail();
            }
        }
        System.out.println("read " + received + " from stream");
        Assert.assertTrue(received == bytesWritten - 100);
    }

    @Test
    public void testSeekWithPipeline() {
        long received = 0;
        byte[] bytes = new byte[100];
        boolean seekDone = false;
        long skipped = 0;
        try {
            istream = new CCNInputStream(testName, readHandle);
        } catch (IOException e1) {
            System.err.println("Failed to seek stream to byte 0: " + e1.getMessage());
            Assert.fail();
        }
        while (!istream.eof()) {
            try {
                System.out.println("Read so far: " + received);
                if (received > 0 && received < 150 && !seekDone) {
                    istream.seek(200);
                    seekDone = true;
                    skipped = 200 - received;
                }
                received += istream.read(bytes);
            } catch (IOException e) {
                System.err.println("failed to read segments: " + e.getMessage());
                Assert.fail();
            }
        }
        System.out.println("read " + received + " from stream");
        Assert.assertTrue(received == bytesWritten - skipped);
    }

    @Test
    public void testResetWithPipeline() {
        long received = 0;
        byte[] bytes = new byte[100];
        boolean resetDone = false;
        long resetBytes = 0;
        try {
            istream = new CCNInputStream(testName, readHandle);
            istream.mark(0);
        } catch (IOException e1) {
            System.err.println("Failed to get new stream: " + e1.getMessage());
            Assert.fail();
        }
        while (!istream.eof()) {
            try {
                System.out.println("Read so far: " + received);
                if (received > 0 && received < 250 && !resetDone) {
                    istream.reset();
                    resetDone = true;
                    resetBytes = received;
                }
                received += istream.read(bytes);
            } catch (IOException e) {
                System.err.println("failed to read segments: " + e.getMessage());
                Assert.fail();
            }
        }
        System.out.println("read " + received + " from stream");
        Assert.assertTrue(received == bytesWritten + resetBytes);
    }

    @Test
    public void testVersionedNameWithPipeline() {
        long received = 0;
        byte[] bytes = new byte[1024];
        try {
            vistream = new CCNVersionedInputStream(VersioningProfile.cutLastVersion(testName), readHandle);
        } catch (IOException e1) {
            System.err.println("failed to open stream for pipeline test: " + e1.getMessage());
            Assert.fail();
        }
        while (!vistream.eof()) {
            try {
                received += vistream.read(bytes);
            } catch (IOException e) {
                System.err.println("failed to read segments: " + e.getMessage());
                Assert.fail();
            }
        }
        System.out.println("read " + received + " from stream");
        Assert.assertTrue(received == bytesWritten);
    }

    @Test
    public void testGetFirstDigest() {
        long received = 0;
        byte[] bytes = new byte[1024];
        try {
            istream = new CCNInputStream(testName, readHandle);
        } catch (IOException e1) {
            System.err.println("failed to open stream for pipeline test: " + e1.getMessage());
            Assert.fail();
        }
        try {
            Assert.assertTrue(DataUtils.arrayEquals(firstDigest, istream.getFirstDigest()));
        } catch (IOException e3) {
            System.err.println("failed to get first digest for pipeline test:");
            Assert.fail();
        }
        try {
            istream.close();
        } catch (IOException e2) {
            System.err.println("failed to close stream for pipeline test: " + e2.getMessage());
            Assert.fail();
        }
        System.out.println("start first segment digest " + firstDigest);
        try {
            istream = new CCNInputStream(testName, readHandle);
        } catch (IOException e1) {
            System.err.println("failed to open stream for pipeline test: " + e1.getMessage());
            Assert.fail();
        }
        while (!istream.eof()) {
            try {
                received += istream.read(bytes);
            } catch (IOException e) {
                System.err.println("failed to read segments: " + e.getMessage());
                Assert.fail();
            }
        }
        Assert.assertTrue(received == bytesWritten);
        try {
            Assert.assertTrue(DataUtils.arrayEquals(firstDigest, istream.getFirstDigest()));
        } catch (IOException e) {
            System.err.println("failed to get first digest after reading in pipeline test:");
            Assert.fail();
        }
        System.out.println("end first segment digest " + firstDigest);
    }
}
