package com.android.camera.stress;

import com.android.camera.Camera;
import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.KeyEvent;
import java.io.FileWriter;
import java.io.Writer;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import android.content.Intent;

/**
 * Junit / Instrumentation test case for camera test
 *
 * Running the test suite:
 *
 * adb shell am instrument \
 *    -e class com.android.camera.stress.ImageCapture \
 *    -w com.android.camera.tests/com.android.camera.CameraStressTestRunner
 *
 */
public class ImageCapture extends ActivityInstrumentationTestCase2<Camera> {

    private String TAG = "ImageCapture";

    private static final int TOTAL_NUMBER_OF_IMAGECAPTURE = 100;

    private static final int TOTAL_NUMBER_OF_VIDEOCAPTURE = 100;

    private static final long WAIT_FOR_IMAGE_CAPTURE_TO_BE_TAKEN = 1500;

    private static final long WAIT_FOR_VIDEO_CAPTURE_TO_BE_TAKEN = 20000;

    private static final long WAIT_FOR_PREVIEW = 1500;

    private static final long WAIT_FOR_STABLE_STATE = 2000;

    private static final int NO_OF_LOOPS_TAKE_MEMORY_SNAPSHOT = 10;

    private static final String CAMERA_MEM_OUTPUTFILE = "/sdcard/ImageCaptureMemOut.txt";

    private static final int MAX_ACCEPTED_MEMORY_LEAK_KB = 150;

    private static int mStartMemory = 0;

    private static int mEndMemory = 0;

    private static int mStartPid = 0;

    private static int mEndPid = 0;

    private static final String CAMERA_TEST_OUTPUT_FILE = "/sdcard/mediaStressOut.txt";

    private BufferedWriter mOut;

    private FileWriter mfstream;

    public ImageCapture() {
        super("com.android.camera", Camera.class);
    }

    @Override
    protected void setUp() throws Exception {
        getActivity();
        prepareOutputFile();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        closeOutputFile();
        super.tearDown();
    }

    private void prepareOutputFile() {
        try {
            mfstream = new FileWriter(CAMERA_TEST_OUTPUT_FILE, true);
            mOut = new BufferedWriter(mfstream);
        } catch (Exception e) {
            assertTrue("ImageCapture open output", false);
        }
    }

    private void closeOutputFile() {
        try {
            mOut.write("\n");
            mOut.close();
            mfstream.close();
        } catch (Exception e) {
            assertTrue("ImageCapture close output", false);
        }
    }

    public void getMemoryWriteToLog(Writer output) {
        String memusage = null;
        memusage = captureMediaserverInfo();
        Log.v(TAG, memusage);
        try {
            output.write(memusage);
        } catch (Exception e) {
            e.toString();
        }
    }

    public String captureMediaserverInfo() {
        String cm = "ps mediaserver";
        String memoryUsage = null;
        int ch;
        try {
            Process p = Runtime.getRuntime().exec(cm);
            InputStream in = p.getInputStream();
            StringBuffer sb = new StringBuffer(512);
            while ((ch = in.read()) != -1) {
                sb.append((char) ch);
            }
            memoryUsage = sb.toString();
        } catch (IOException e) {
            Log.v(TAG, e.toString());
        }
        String[] poList = memoryUsage.split("\r|\n|\r\n");
        String memusage = poList[1].concat("\n");
        return memusage;
    }

    public int getMediaserverPid() {
        String memoryUsage = null;
        int pidvalue = 0;
        memoryUsage = captureMediaserverInfo();
        String[] poList2 = memoryUsage.split("\t|\\s+");
        String pid = poList2[1];
        pidvalue = Integer.parseInt(pid);
        Log.v(TAG, "PID = " + pidvalue);
        return pidvalue;
    }

    public int getMediaserverVsize() {
        String memoryUsage = captureMediaserverInfo();
        String[] poList2 = memoryUsage.split("\t|\\s+");
        String vsize = poList2[3];
        int vsizevalue = Integer.parseInt(vsize);
        Log.v(TAG, "VSIZE = " + vsizevalue);
        return vsizevalue;
    }

    public boolean validateMemoryResult(int startPid, int startMemory, Writer output) throws Exception {
        Thread.sleep(20000);
        mEndPid = getMediaserverPid();
        mEndMemory = getMediaserverVsize();
        output.write("Start Memory = " + startMemory + "\n");
        output.write("End Memory = " + mEndMemory + "\n");
        Log.v(TAG, "End memory :" + mEndMemory);
        output.write("The total diff = " + (mEndMemory - startMemory));
        output.write("\n\n");
        if (startPid != mEndPid) {
            output.write("mediaserver died. Test failed\n");
            return false;
        }
        if ((mEndMemory - startMemory) > MAX_ACCEPTED_MEMORY_LEAK_KB) return false;
        return true;
    }

    @LargeTest
    public void testImageCapture() {
        boolean memoryResult = false;
        Instrumentation inst = getInstrumentation();
        File imageCaptureMemFile = new File(CAMERA_MEM_OUTPUTFILE);
        mStartPid = getMediaserverPid();
        mStartMemory = getMediaserverVsize();
        Log.v(TAG, "start memory : " + mStartMemory);
        try {
            Writer output = new BufferedWriter(new FileWriter(imageCaptureMemFile, true));
            output.write("Camera Image capture\n");
            output.write("No of loops : " + TOTAL_NUMBER_OF_VIDEOCAPTURE + "\n");
            getMemoryWriteToLog(output);
            mOut.write("Camera Image Capture\n");
            mOut.write("No of loops :" + TOTAL_NUMBER_OF_VIDEOCAPTURE + "\n");
            mOut.write("loop: ");
            for (int i = 0; i < TOTAL_NUMBER_OF_IMAGECAPTURE; i++) {
                Thread.sleep(WAIT_FOR_IMAGE_CAPTURE_TO_BE_TAKEN);
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
                Thread.sleep(WAIT_FOR_IMAGE_CAPTURE_TO_BE_TAKEN);
                if ((i % NO_OF_LOOPS_TAKE_MEMORY_SNAPSHOT) == 0) {
                    Log.v(TAG, "value of i :" + i);
                    getMemoryWriteToLog(output);
                }
                if (Camera.mMediaServerDied) {
                    mOut.write("\nmedia server died\n");
                    mOut.flush();
                    output.close();
                    Camera.mMediaServerDied = false;
                    assertTrue("Camera Image Capture", false);
                }
                mOut.write(" ," + i);
                mOut.flush();
            }
            Thread.sleep(WAIT_FOR_STABLE_STATE);
            memoryResult = validateMemoryResult(mStartPid, mStartMemory, output);
            Log.v(TAG, "End memory : " + getMediaserverVsize());
            output.close();
            assertTrue("Camera image capture memory test", memoryResult);
        } catch (Exception e) {
            Log.v(TAG, e.toString());
            assertTrue("testImageCapture", false);
        }
        assertTrue("testImageCapture", true);
    }

    @LargeTest
    public void testVideoCapture() {
        boolean memoryResult = false;
        Instrumentation inst = getInstrumentation();
        File imageCaptureMemFile = new File(CAMERA_MEM_OUTPUTFILE);
        mStartPid = getMediaserverPid();
        mStartMemory = getMediaserverVsize();
        Log.v(TAG, "start memory : " + mStartMemory);
        try {
            Writer output = new BufferedWriter(new FileWriter(imageCaptureMemFile, true));
            output.write("Camera Video capture\n");
            output.write("No of loops : " + TOTAL_NUMBER_OF_VIDEOCAPTURE + "\n");
            getMemoryWriteToLog(output);
            mOut.write("Camera Video Capture\n");
            mOut.write("No of loops :" + TOTAL_NUMBER_OF_VIDEOCAPTURE + "\n");
            mOut.write("loop: ");
            Intent intent = new Intent();
            intent.setClassName("com.android.camera", "com.android.camera.VideoCamera");
            getActivity().startActivity(intent);
            for (int i = 0; i < TOTAL_NUMBER_OF_VIDEOCAPTURE; i++) {
                Thread.sleep(WAIT_FOR_PREVIEW);
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
                Thread.sleep(WAIT_FOR_VIDEO_CAPTURE_TO_BE_TAKEN);
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
                Thread.sleep(WAIT_FOR_PREVIEW);
                mOut.write(" ," + i);
                mOut.flush();
            }
            Thread.sleep(WAIT_FOR_STABLE_STATE);
            memoryResult = validateMemoryResult(mStartPid, mStartMemory, output);
            Log.v(TAG, "End memory : " + getMediaserverVsize());
            output.close();
            assertTrue("Camera video capture memory test", memoryResult);
        } catch (Exception e) {
            Log.v(TAG, e.toString());
            fail("Fails to capture video");
        }
    }
}
