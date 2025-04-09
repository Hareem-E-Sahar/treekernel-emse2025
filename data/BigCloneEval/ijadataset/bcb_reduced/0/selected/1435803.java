package com.phosco.pgui.robot;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * <h1>PhOSCo GUI Test Robot</h1>
 * 
 * <pre>
 * PGUI - PhOSCo GUI Testing
 *     Copyright (C) 2000 Mike Calder-Smith, mike@gmot.demon.co.uk
 *     Guillemot Design Ltd - www.PhOSCo.com
 * 
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * </pre>
 * 
 * 
 * <p>
 * The PhOSCo GUI Test Robot is a very easy to use tool for testing the
 * Graphical User Interface (GUI) behaviour of Java applications. It consists of
 * two programs and one helper class:
 * <ul>
 * <li>The PhOSCo GUI Capture tool <b><i>PGUICapture</b></i>,
 * <li>The PhOSCo GUI Robot program <b><i>PGUIRobot</b></i>, and
 * <li>The PhOSCo GUI Robot Slave helper class
 * <b><i>PGUIRobotSlave.class</b></i>.
 * </ul>
 * 
 * <p>
 * For documentation, see the <A HREF="PGUIUser.html">PGUI user documentation
 * </A>.
 * 
 * <p>
 * This is a very initial release with minimal testing, but does work. Please
 * let the author know of any problems. Improvements and adaptations welcomed.
 * 
 * <h2>PhOSCo Graphical User Interface Tester Robot</h2>
 * 
 * <p>
 * This code requires Java 1.3 to compile, and the availability of the
 * com.sun.image.codec.jpeg extensions from the Sun JDK 1.3 or JRE.
 * 
 * <p>
 * This routine is used to test the GUI interface of Java programs in the PhOSCo
 * suite.
 */
public class PGUIRobot extends Object {

    /**
	 * Version
	 */
    static String version = "1.0";

    /**
	 * Copyright
	 */
    static String copyr = "Copyright (c) 2000 Guillemot Design Ltd. See 'PhOSCo Open Source Licence' document.";

    /**
	 * Test Code
	 */
    static final int COMMENT = 0;

    /**
	 * Test Code
	 */
    static final int DELAY = 1;

    /**
	 * Test Code
	 */
    static final int MOUSECLICK = 2;

    /**
	 * Test Code
	 */
    static final int MOUSEDBLCLICK = 3;

    /**
	 * Test Code
	 */
    static final int MOUSEMOVE = 4;

    /**
	 * Test Code
	 */
    static final int DRAGMOUSE = 5;

    /**
	 * Test Code
	 */
    static final int SETAUTODELAY = 6;

    /**
	 * Test Code
	 */
    static final int SETMODEKEY = 7;

    /**
	 * Test Code
	 */
    static final int TYPEKEY = 8;

    /**
	 * Test Code
	 */
    static final int UNSETMODEKEY = 9;

    /**
	 * Test Code
	 */
    static final int INVOKED = 10;

    /**
	 * Test Code
	 */
    static final int SCREENCAPTURE = 11;

    /**
	 * Test Code
	 */
    static final int PROCESS = 12;

    /**
	 * Test Code
	 */
    static final int FILECOMPARE = 13;

    /**
	 * Test Code
	 */
    static final int FILECOPY = 14;

    /**
	 * Test Code
	 */
    static final int TERMINATE = 23;

    /**
	 * Test parameter - auto delay interval
	 */
    int autodelayint;

    /**
	 * Test parameter - mouse button mask
	 */
    int buttonmask;

    /**
	 * Test parameter - Key code
	 */
    int keycode;

    /**
	 * Test parameter - delay interval
	 */
    int sleeptime;

    /**
	 * Test parameter - mouse x coordinate
	 */
    int x;

    /**
	 * Test parameter - mouse y coordinate
	 */
    int y;

    /**
	 * Test parameter - mouse x coordinate at start of drag
	 */
    int dragStartX;

    /**
	 * Test parameter - mouse y coordinate at start of drag
	 */
    int dragStartY;

    /**
	 * Shift lock
	 */
    boolean shiftlock;

    /**
	 * Log file
	 */
    protected static PrintStream log;

    /**
	 * The Robot
	 */
    protected Robot robbie;

    /**
	 * PGUI Capture/Robot Instrumentation Datagram Socket
	 */
    DatagramSocket dsPGUI;

    /**
	 * PGUI Capture/Robot Instrumentation Datagram Packet Buffer
	 */
    byte[] bufPGUI;

    /**
	 * PGUI Capture/Robot Instrumentation Datagram Packet
	 */
    DatagramPacket dpcktPGUI;

    /**
	 * The tested process
	 */
    static Process thisProc;

    /**
	 * Tested Process Screen X Origin
	 */
    private int sox;

    /**
	 * Tested Process Screen Y Origin
	 */
    private int soy;

    /**
	 * Tested Process Screen Width
	 */
    private int swidth;

    /**
	 * Tested Process Screen Height
	 */
    private int sheight;

    /**
	 * Tested Process Active Flag
	 */
    private boolean testeeActive = false;

    /**
	 * Tested Process Invocation string
	 */
    private String invocationString;

    /**
	 * Test Case suite file
	 */
    private BufferedReader tcs;

    /**
	 * Error code
	 */
    private int errcode;

    /**
	 * Pass/Fail Flag
	 */
    private boolean testpass;

    /**
	 * Number of test cases
	 */
    private int testcases;

    /**
	 * Number of passes
	 */
    private int numpasses;

    /**
	 * Image counter
	 */
    private int imageCounter;

    /**
	 * Verbosity Flag
	 */
    boolean verbose;

    /**
	 * Error Output Stream produced by the tested process
	 */
    BufferedInputStream processErrors;

    /**
	 * Standard Output Stream produced by the tested process
	 */
    BufferedInputStream processOutput;

    /**
	 * Class to write an output stream of the tested process to a file.
	 */
    private final class WriteProcessOutput implements Runnable {

        BufferedInputStream outp;

        FileOutputStream output;

        boolean keepgoing;

        /**
		 * Constructor.
		 * 
		 * @param BufferedInputStream
		 *            Tested Process Output Stream
		 * @param String
		 *            Output file name
		 */
        public WriteProcessOutput(BufferedInputStream b1, String filename) {
            outp = b1;
            keepgoing = true;
            try {
                output = new FileOutputStream(filename, true);
            } catch (Exception e) {
                System.out.println("Process output file creation failure on file: " + filename + " Exception: " + e);
                keepgoing = false;
            }
        }

        public void run() {
            int i;
            if (!keepgoing) return;
            try {
                i = 0;
                while (i != -1) {
                    if (!keepgoing) break;
                    i = outp.read();
                    if (i == 10) output.write(13);
                    output.write(i);
                }
                output.close();
            } catch (Exception e) {
                System.out.println("Process output stream failure: " + e);
            }
            return;
        }
    }

    /**
	 * Mainline
	 * 
	 * @param String
	 *            [] Runtime arguments:
	 *            <ul>
	 *            <li>Test script file name
	 *            <li>[optional] 'q' for quiet running
	 *            </ul>
	 */
    public static void main(String args[]) {
        int i;
        String testfilename = "";
        String s;
        PGUIRobot thisPGUI;
        thisPGUI = new PGUIRobot();
        thisPGUI.verbose = true;
        for (i = 0; i < args.length; i++) {
            if (i == 0) testfilename = args[0];
            if (i == 1) {
                if ((args[1].startsWith("q")) || (args[1].startsWith("Q"))) {
                    thisPGUI.verbose = false;
                }
            }
        }
        System.out.println("\r\nPhOSCo GUI Robot");
        System.out.println("\r\n\r\nCopyright (c) 2000 Guillemot Design Ltd. See www.phosco.com for details.\r\n");
        System.out.println("\r\nLicenced materials subject to the PhOSCo General Licence");
        int ok = 1;
        if (testfilename.length() == 0) {
            ok = 0;
        }
        if (ok == 0) {
            System.out.println("\r\nInvoke program by: java PGUIRobot testfilename [q] ");
            System.out.println("\r\n   testfilename = Test File Path");
            System.out.println("\r\n   q = 'Quiet' mode - warning dialogs off\n\n\n");
            System.out.println("\r\n   Test File may be a Test Case Suite (.GTS) ");
            System.out.println("\r\n   or a single test case (any other extension) \n\n\n");
            System.exit(-1);
        }
        try {
            log = new PrintStream(new FileOutputStream("PGUIROBOT.LOG"), true);
        } catch (IOException e) {
            thisPGUI.errcode = -1;
            System.out.println("Unable to open log file - logging inoperative");
        }
        String ext = testfilename.substring(testfilename.lastIndexOf("."));
        thisPGUI.testcases = 1;
        thisPGUI.numpasses = 0;
        try {
            thisPGUI.dsPGUI = new DatagramSocket(9877);
            thisPGUI.bufPGUI = new byte[256];
            thisPGUI.dpcktPGUI = new DatagramPacket(thisPGUI.bufPGUI, 256, InetAddress.getLocalHost(), 9876);
        } catch (Exception e) {
            String msg = new String("Failed to open socket or send Datagram: " + e);
            System.out.println(msg);
            thisPGUI.logPrint(msg);
        }
        if (ext.toUpperCase().equals(".GTS")) {
            try {
                thisPGUI.tcs = new BufferedReader(new FileReader(testfilename));
                testfilename = thisPGUI.getTestFilename();
                while (testfilename.length() != 0) {
                    thisPGUI.runTests(testfilename);
                    if (thisPGUI.testpass) {
                        thisPGUI.numpasses++;
                        thisPGUI.logPrint("Test Case Passed.\n\n");
                    } else {
                        thisPGUI.logPrint("Test Case Failed.\n\n");
                    }
                    testfilename = thisPGUI.getTestFilename();
                }
            } catch (Exception e) {
                s = new String("Test File read Failure " + e);
                thisPGUI.errcode = -4;
                thisPGUI.logPrint(s);
                System.out.println(s);
            }
        } else {
            thisPGUI.runTests(testfilename);
            if (thisPGUI.testpass) thisPGUI.numpasses++;
        }
        if (thisPGUI.errcode != 0) {
            s = new String("Program Error Code " + thisPGUI.errcode);
            thisPGUI.logPrint(s);
            System.out.println(s);
        } else {
            s = new String("Test Cases Passed = " + thisPGUI.numpasses + " out of " + thisPGUI.testcases);
            thisPGUI.logPrint(s);
            System.out.println(s);
        }
        log.close();
        thisPGUI.sendSlave("23");
        System.exit(thisPGUI.errcode);
    }

    /**
	 * Constructor
	 */
    public PGUIRobot() {
        errcode = 0;
        testpass = true;
        imageCounter = 0;
        try {
            robbie = new Robot();
        } catch (Exception e) {
            System.out.println("System does not allow robot creation; exception " + e);
            return;
        }
    }

    /**
	 * Invoke application to be tested and get screen window datagram from it.
	 * 
	 * @param String
	 *            Invocation string.
	 */
    private void runRobot(String invoc) {
        Runtime thisRT;
        try {
            if (processErrors != null) processErrors.close();
            if (processOutput != null) processOutput.close();
        } catch (Exception e) {
        }
        try {
            thisRT = Runtime.getRuntime();
            thisProc = thisRT.exec(invoc);
            testeeActive = true;
            processErrors = new BufferedInputStream(thisProc.getErrorStream());
            WriteProcessOutput wpe = new WriteProcessOutput(processErrors, "PGUIRSTE.DAT");
            new Thread(wpe).start();
            processOutput = new BufferedInputStream(thisProc.getInputStream());
            WriteProcessOutput wpo = new WriteProcessOutput(processOutput, "PGUIRSTO.DAT");
            new Thread(wpo).start();
        } catch (Exception e) {
            System.out.println("System cannot run " + invoc + "; exception " + e);
            errcode = -2;
            return;
        }
        return;
    }

    /**
	 * Get next Test Case Filename.
	 * <p>
	 * The Test Case Suite file must be an ASCII file with no empty lines with a
	 * test case file name on each line.
	 * 
	 * @return String Test Case filename from Test Case Suite file
	 */
    private String getTestFilename() {
        String s;
        try {
            s = tcs.readLine();
            if (s == null) {
                s = new String("");
                tcs.close();
            }
        } catch (Exception e) {
            s = new String("");
        }
        return s;
    }

    /**
	 * Run GUI Tests from file against current application.
	 * 
	 * @param String
	 *            Test File name
	 */
    private void runTests(String tfn) {
        BufferedReader bfr;
        testpass = false;
        try {
            bfr = new BufferedReader(new FileReader(tfn));
        } catch (Exception e) {
            System.out.println("Unable to open Test file " + tfn + ": exception " + e);
            return;
        }
        String ins;
        int cmdcode;
        shiftlock = false;
        testpass = true;
        robbie.setAutoWaitForIdle(true);
        logPrint("Test Case filename " + tfn + "\n");
        try {
            ins = bfr.readLine();
            while (ins != null) {
                ins = ins.trim();
                if (errcode != 0) {
                    return;
                }
                if (!ins.startsWith("*")) {
                    cmdcode = cmdParse(ins);
                    switch(cmdcode) {
                        case COMMENT:
                            break;
                        case DELAY:
                            logAction(cmdcode, sleeptime);
                            robbie.delay(sleeptime);
                            break;
                        case DRAGMOUSE:
                            if (!testeeActive) return;
                            logAction(cmdcode, buttonmask);
                            robbie.mouseMove(dragStartX, dragStartY);
                            robbie.mousePress(buttonmask);
                            robbie.mouseMove(x, y);
                            robbie.mouseRelease(buttonmask);
                            break;
                        case FILECOPY:
                            fileCopy(invocationString);
                            logPrint("File Copy " + invocationString);
                            break;
                        case FILECOMPARE:
                            logPrint("File Compare " + invocationString);
                            fileCompare(invocationString);
                            break;
                        case INVOKED:
                            runRobot(invocationString);
                            if (!testeeActive) {
                                logPrint("Invocation Failed: " + invocationString);
                                bfr.close();
                                return;
                            }
                            logPrint("Invoked " + invocationString);
                            dsPGUI.receive(dpcktPGUI);
                            sendSlave("Now then");
                            robbie.delay(10000);
                            break;
                        case MOUSECLICK:
                            if (!testeeActive) return;
                            logAction(cmdcode, buttonmask);
                            robbie.setAutoDelay(0);
                            robbie.mousePress(buttonmask);
                            robbie.mouseRelease(buttonmask);
                            robbie.setAutoDelay(autodelayint);
                            break;
                        case MOUSEDBLCLICK:
                            if (!testeeActive) return;
                            logAction(cmdcode, buttonmask);
                            robbie.setAutoDelay(0);
                            robbie.mousePress(buttonmask);
                            robbie.mouseRelease(buttonmask);
                            robbie.mousePress(buttonmask);
                            robbie.mouseRelease(buttonmask);
                            robbie.setAutoDelay(autodelayint);
                            break;
                        case MOUSEMOVE:
                            if (!testeeActive) return;
                            logAction(cmdcode, x);
                            robbie.mouseMove(x, y);
                            break;
                        case PROCESS:
                            logPrint("Process run " + invocationString);
                            runProcess(invocationString);
                            break;
                        case SCREENCAPTURE:
                            if (!testeeActive) return;
                            logPrint("Compare Screen with " + invocationString);
                            captureScreen(invocationString);
                            break;
                        case SETAUTODELAY:
                            logAction(cmdcode, autodelayint);
                            robbie.setAutoDelay(autodelayint);
                            break;
                        case SETMODEKEY:
                            if (!testeeActive) return;
                            logAction(cmdcode, keycode);
                            robbie.keyPress(keycode);
                            break;
                        case TYPEKEY:
                            if (!testeeActive) return;
                            logAction(cmdcode, keycode);
                            robbie.keyPress(keycode);
                            robbie.keyRelease(keycode);
                            break;
                        case UNSETMODEKEY:
                            if (!testeeActive) return;
                            logAction(cmdcode, keycode);
                            robbie.keyRelease(keycode);
                            break;
                        case TERMINATE:
                            logAction(cmdcode, 0);
                            buttonmask = InputEvent.BUTTON1_MASK;
                            robbie.mouseMove((sox + swidth - 10), (soy + 10));
                            robbie.mousePress(buttonmask);
                            robbie.mouseRelease(buttonmask);
                            break;
                        default:
                    }
                }
                ins = bfr.readLine();
            }
        } catch (IOException e) {
            try {
                bfr.close();
            } catch (Exception ex) {
            }
        }
        return;
    }

    /**
	 * Parse test command string.
	 * <p>
	 * This routine may also amend the global test parameters.
	 * 
	 * @param String
	 *            Test command
	 * @return int Test Code
	 */
    public int cmdParse(String cmdstring) {
        String cmd;
        String err = "Error in command: ";
        String keyword;
        String parm;
        StringTokenizer st;
        char c;
        int rc;
        cmd = cmdstring.trim();
        st = new StringTokenizer(cmd);
        rc = COMMENT;
        if (st.hasMoreTokens()) {
            keyword = st.nextToken();
            if (keyword.equals("CM")) {
                rc = MOUSECLICK;
                if (st.hasMoreTokens()) {
                    parm = st.nextToken();
                    c = parm.charAt(0);
                    if (c == '1') {
                        buttonmask = InputEvent.BUTTON1_MASK;
                    } else if (c == '2') {
                        buttonmask = InputEvent.BUTTON3_MASK;
                    } else if (c == '3') {
                        buttonmask = InputEvent.BUTTON2_MASK;
                    } else if (c == '4') {
                        buttonmask = InputEvent.BUTTON1_MASK + InputEvent.BUTTON3_MASK;
                    } else {
                        rc = COMMENT;
                        logPrint(err + cmdstring);
                    }
                } else {
                    rc = COMMENT;
                    keycode = KeyEvent.CHAR_UNDEFINED;
                    logPrint(err + cmdstring);
                }
            } else if (keyword.equals("COMPARE")) {
                rc = FILECOMPARE;
                if (st.hasMoreTokens()) {
                    invocationString = st.nextToken("");
                }
            } else if (keyword.equals("DCM")) {
                rc = MOUSEDBLCLICK;
            } else if (keyword.equals("SC")) {
                rc = SCREENCAPTURE;
                if (st.hasMoreTokens()) {
                    invocationString = st.nextToken("");
                }
            } else if (keyword.equals("FC")) {
                rc = FILECOPY;
                if (st.hasMoreTokens()) {
                    invocationString = st.nextToken("");
                }
            } else if (keyword.equals("PI")) {
                rc = PROCESS;
                if (st.hasMoreTokens()) {
                    invocationString = st.nextToken("");
                }
            } else if (keyword.equals("INVOKED")) {
                rc = INVOKED;
                if (st.hasMoreTokens()) {
                    invocationString = st.nextToken("");
                }
            } else if (keyword.equals("KEY")) {
                rc = TYPEKEY;
                if (st.hasMoreTokens()) {
                    parm = st.nextToken();
                    keycode = parseKeyCode(parm);
                    if (keycode == KeyEvent.CHAR_UNDEFINED) {
                        rc = COMMENT;
                        logPrint(err + cmdstring);
                    }
                } else {
                    rc = COMMENT;
                    keycode = KeyEvent.CHAR_UNDEFINED;
                    logPrint(err + cmdstring);
                }
            } else if (keyword.equals("DLY")) {
                rc = DELAY;
                if (st.hasMoreTokens()) {
                    parm = st.nextToken();
                    try {
                        sleeptime = Integer.parseInt(parm);
                    } catch (Exception e) {
                        rc = COMMENT;
                        logPrint(err + cmdstring);
                    }
                } else {
                    rc = COMMENT;
                    logPrint(err + cmdstring);
                }
            } else if (keyword.equals("MM")) {
                rc = MOUSEMOVE;
                if (st.hasMoreTokens()) {
                    parm = st.nextToken();
                    try {
                        x = Integer.parseInt(parm);
                    } catch (Exception e) {
                        rc = COMMENT;
                        logPrint(err + cmdstring);
                    }
                } else {
                    rc = COMMENT;
                    logPrint(err + cmdstring);
                }
                if (x < sox) x = sox;
                if (x > (sox + swidth)) x = sox + swidth;
                if (st.hasMoreTokens()) {
                    parm = st.nextToken();
                    try {
                        y = Integer.parseInt(parm);
                    } catch (Exception e) {
                        rc = COMMENT;
                        logPrint(err + cmdstring);
                    }
                } else {
                    rc = COMMENT;
                    logPrint(err + cmdstring);
                }
                if (y < soy) y = soy;
                if (y > (soy + sheight)) y = soy + sheight;
            } else if (keyword.equals("DM")) {
                rc = DRAGMOUSE;
                dragStartX = x;
                dragStartY = y;
                if (st.hasMoreTokens()) {
                    parm = st.nextToken();
                    try {
                        x = Integer.parseInt(parm);
                    } catch (Exception e) {
                        rc = COMMENT;
                        logPrint(err + cmdstring);
                    }
                } else {
                    rc = COMMENT;
                    logPrint(err + cmdstring);
                }
                if (x < sox) x = sox;
                if (x > (sox + swidth)) x = sox + swidth;
                if (st.hasMoreTokens()) {
                    parm = st.nextToken();
                    try {
                        y = Integer.parseInt(parm);
                    } catch (Exception e) {
                        rc = COMMENT;
                        logPrint(err + cmdstring);
                    }
                } else {
                    rc = COMMENT;
                    logPrint(err + cmdstring);
                }
                if (y < soy) y = soy;
                if (y > (soy + sheight)) y = soy + sheight;
                if (st.hasMoreTokens()) {
                    parm = st.nextToken();
                    c = parm.charAt(0);
                    if (c == '1') {
                        buttonmask = InputEvent.BUTTON1_MASK;
                    } else if (c == '2') {
                        buttonmask = InputEvent.BUTTON3_MASK;
                    } else if (c == '3') {
                        buttonmask = InputEvent.BUTTON2_MASK;
                    } else if (c == '4') {
                        buttonmask = InputEvent.BUTTON1_MASK + InputEvent.BUTTON3_MASK;
                    }
                }
                if (Math.abs(dragStartX - x) < 3) {
                    if (Math.abs(dragStartY - y) < 3) {
                        rc = MOUSECLICK;
                    }
                }
            } else if (keyword.equals("SET")) {
                rc = SETMODEKEY;
                if (st.hasMoreTokens()) {
                    parm = st.nextToken();
                    keycode = parseKeyCode(parm);
                    if (keycode == KeyEvent.CHAR_UNDEFINED) {
                        rc = COMMENT;
                        logPrint(err + cmdstring);
                    }
                } else {
                    rc = COMMENT;
                    logPrint(err + cmdstring);
                }
            } else if (keyword.equals("USING")) {
                if (st.countTokens() == 4) {
                    sox = Integer.parseInt(st.nextToken());
                    soy = Integer.parseInt(st.nextToken());
                    swidth = Integer.parseInt(st.nextToken());
                    sheight = Integer.parseInt(st.nextToken().trim());
                    logPrint("Using Screen x " + sox + " y " + soy + " width " + swidth + " height " + sheight);
                    sendSlave("Move " + sox + " " + soy + " " + swidth + " " + sheight);
                }
            } else if (keyword.equals("UNS")) {
                rc = UNSETMODEKEY;
                if (st.hasMoreTokens()) {
                    parm = st.nextToken();
                    keycode = parseKeyCode(parm);
                    if (keycode == KeyEvent.CHAR_UNDEFINED) {
                        rc = COMMENT;
                        logPrint(err + cmdstring);
                    }
                } else {
                    rc = COMMENT;
                    logPrint(err + cmdstring);
                }
            } else if (keyword.equals("AD")) {
                rc = SETAUTODELAY;
                if (st.hasMoreTokens()) {
                    parm = st.nextToken();
                    try {
                        autodelayint = Integer.parseInt(parm);
                    } catch (Exception e) {
                        rc = COMMENT;
                        logPrint(err + cmdstring);
                    }
                } else {
                    rc = COMMENT;
                    logPrint(err + cmdstring);
                }
            } else if (keyword.equals("TERMINATE")) {
                rc = TERMINATE;
            }
        }
        return rc;
    }

    /**
	 * Parse string representation of a key code into the correct KeyEvent
	 * value.
	 * <p>
	 * The String may be a single character in which case it represents the
	 * character which is required (including single digit characters); it may
	 * be a numeric string, in which case this is a string representation of the
	 * key code itself; or it may be a key name such as "Alt", "Ctrl", and
	 * "Shift".
	 * 
	 * @param String
	 *            String key code
	 * @return int KeyEvent Key Code or CHAR_UNDEFINED
	 */
    private int parseKeyCode(String s) {
        char c;
        int rc;
        rc = KeyEvent.VK_UNDEFINED;
        logPrint("Key string received >" + s + "<");
        if (s.length() == 1) {
            s = s.toUpperCase();
            c = s.charAt(0);
            switch(c) {
                case '0':
                    return KeyEvent.VK_0;
                case '1':
                    return KeyEvent.VK_1;
                case '2':
                    return KeyEvent.VK_2;
                case '3':
                    return KeyEvent.VK_3;
                case '4':
                    return KeyEvent.VK_4;
                case '5':
                    return KeyEvent.VK_5;
                case '6':
                    return KeyEvent.VK_6;
                case '7':
                    return KeyEvent.VK_7;
                case '8':
                    return KeyEvent.VK_8;
                case '9':
                    return KeyEvent.VK_9;
                case 'A':
                    return KeyEvent.VK_A;
                case 'B':
                    return KeyEvent.VK_B;
                case 'C':
                    return KeyEvent.VK_C;
                case 'D':
                    return KeyEvent.VK_D;
                case 'E':
                    return KeyEvent.VK_E;
                case 'F':
                    return KeyEvent.VK_F;
                case 'G':
                    return KeyEvent.VK_G;
                case 'H':
                    return KeyEvent.VK_H;
                case 'I':
                    return KeyEvent.VK_I;
                case 'J':
                    return KeyEvent.VK_J;
                case 'K':
                    return KeyEvent.VK_K;
                case 'L':
                    return KeyEvent.VK_L;
                case 'M':
                    return KeyEvent.VK_M;
                case 'N':
                    return KeyEvent.VK_N;
                case 'O':
                    return KeyEvent.VK_O;
                case 'P':
                    return KeyEvent.VK_P;
                case 'Q':
                    return KeyEvent.VK_Q;
                case 'R':
                    return KeyEvent.VK_R;
                case 'S':
                    return KeyEvent.VK_S;
                case 'T':
                    return KeyEvent.VK_T;
                case 'U':
                    return KeyEvent.VK_U;
                case 'V':
                    return KeyEvent.VK_V;
                case 'W':
                    return KeyEvent.VK_W;
                case 'X':
                    return KeyEvent.VK_X;
                case 'Y':
                    return KeyEvent.VK_Y;
                case 'Z':
                    return KeyEvent.VK_Z;
                case '&':
                    return KeyEvent.VK_AMPERSAND;
                case '*':
                    return KeyEvent.VK_ASTERISK;
                case '@':
                    return KeyEvent.VK_AT;
                case '\\':
                    return KeyEvent.VK_BACK_SLASH;
                case '{':
                    return KeyEvent.VK_BRACELEFT;
                case '}':
                    return KeyEvent.VK_BRACERIGHT;
                case '^':
                    return KeyEvent.VK_CIRCUMFLEX;
                case ']':
                    return KeyEvent.VK_CLOSE_BRACKET;
                case ':':
                    return KeyEvent.VK_COLON;
                case ',':
                    return KeyEvent.VK_COMMA;
                case '$':
                    return KeyEvent.VK_DOLLAR;
                case '=':
                    return KeyEvent.VK_EQUALS;
                case '!':
                    return KeyEvent.VK_EXCLAMATION_MARK;
                case '>':
                    return KeyEvent.VK_GREATER;
                case '(':
                    return KeyEvent.VK_LEFT_PARENTHESIS;
                case '<':
                    return KeyEvent.VK_LESS;
                case '-':
                    return KeyEvent.VK_MINUS;
                case '#':
                    return KeyEvent.VK_NUMBER_SIGN;
                case '[':
                    return KeyEvent.VK_OPEN_BRACKET;
                case '.':
                    return KeyEvent.VK_PERIOD;
                case '+':
                    return KeyEvent.VK_PLUS;
                case '\'':
                    return KeyEvent.VK_QUOTE;
                case '"':
                    return KeyEvent.VK_QUOTEDBL;
                case ')':
                    return KeyEvent.VK_RIGHT_PARENTHESIS;
                case ';':
                    return KeyEvent.VK_SEMICOLON;
                case '/':
                    return KeyEvent.VK_SLASH;
                case '_':
                    return KeyEvent.VK_UNDERSCORE;
            }
            return rc;
        }
        if (s.startsWith("VK")) {
            try {
                return Integer.parseInt(s.substring(2, 3));
            } catch (Exception e) {
                return KeyEvent.VK_PERIOD;
            }
        }
        try {
            rc = Integer.parseInt(s);
            if (rc == 32) return KeyEvent.VK_SPACE;
        } catch (Exception e) {
            s = s.toLowerCase();
            if (s.equals("alt")) {
                rc = KeyEvent.VK_ALT;
            } else if (s.equals("ctrl")) {
                rc = KeyEvent.VK_CONTROL;
            } else if (s.equals("delete")) {
                rc = KeyEvent.VK_DELETE;
            } else if (s.equals("down")) {
                rc = KeyEvent.VK_DOWN;
            } else if (s.equals("end")) {
                rc = KeyEvent.VK_END;
            } else if (s.equals("enter")) {
                rc = KeyEvent.VK_ENTER;
            } else if (s.equals("escape")) {
                rc = KeyEvent.VK_ESCAPE;
            } else if (s.equals("help")) {
                rc = KeyEvent.VK_HELP;
            } else if (s.equals("home")) {
                rc = KeyEvent.VK_HOME;
            } else if (s.equals("insert")) {
                rc = KeyEvent.VK_INSERT;
            } else if (s.equals("left")) {
                rc = KeyEvent.VK_LEFT;
            } else if (s.equals("pagedown")) {
                rc = KeyEvent.VK_PAGE_DOWN;
            } else if (s.equals("pageup")) {
                rc = KeyEvent.VK_PAGE_UP;
            } else if (s.equals("right")) {
                rc = KeyEvent.VK_RIGHT;
            } else if (s.equals("shift")) {
                rc = KeyEvent.VK_SHIFT;
            } else if (s.equals("tab")) {
                rc = KeyEvent.VK_TAB;
            } else if (s.equals("up")) {
                rc = KeyEvent.VK_UP;
            } else {
                rc = KeyEvent.CHAR_UNDEFINED;
                logPrint("Key code " + s + " invalid");
            }
        }
        return rc;
    }

    /**
	 * Write line in log file
	 * 
	 * @param String
	 *            Line to be written
	 */
    private void logPrint(String s) {
        log.println(s);
    }

    /**
	 * Write action message to log file
	 * 
	 * @param int Command code
	 * @param int First parameter
	 */
    private void logAction(int cmd, int parm) {
        try {
            switch(cmd) {
                case DELAY:
                    log.println("Delay  parm " + parm);
                    break;
                case DRAGMOUSE:
                    log.println("Drag Mouse from " + dragStartX + ", " + dragStartY + " to " + x + ", " + y + " key " + buttonmask);
                    break;
                case MOUSECLICK:
                    log.println("Click Mouse at " + x + ", " + y + " key " + buttonmask);
                    break;
                case MOUSEDBLCLICK:
                    log.println("DoubleClick Mouse at " + x + ", " + y + " key " + buttonmask);
                    break;
                case MOUSEMOVE:
                    log.println("Move Mouse to " + x + ", " + y);
                    break;
                case SETAUTODELAY:
                    log.println("SAD " + parm);
                    break;
                case SETMODEKEY:
                    log.println("Mode " + parm);
                    break;
                case TYPEKEY:
                    log.println("Key " + parm + " char " + (char) parm);
                    break;
                case UNSETMODEKEY:
                    log.println("Mode Off " + parm);
                    break;
                case TERMINATE:
                    log.println("Normal End of test");
                    break;
                default:
                    log.println("Unrecognised command " + cmd + " " + parm);
            }
        } catch (Exception e) {
            System.out.println("Log file error - logging inoperative");
        }
    }

    /**
	 * Capture and compare Screen Window
	 * 
	 * @param String
	 *            Invocation string
	 */
    private void captureScreen(String invoc) {
        String parm;
        String oldImageFilename;
        String newImageFilename;
        StringTokenizer st;
        int x;
        int y;
        int w;
        int h;
        BufferedImage newBufferedImage;
        Rectangle rect;
        imageCounter++;
        st = new StringTokenizer(invoc);
        if (st.countTokens() < 5) {
            logPrint("Invalid Screen Capture command " + invoc);
            errcode = SCREENCAPTURE;
            return;
        }
        try {
            parm = st.nextToken();
            x = Integer.parseInt(parm);
            parm = st.nextToken();
            y = Integer.parseInt(parm);
            parm = st.nextToken();
            w = Integer.parseInt(parm);
            parm = st.nextToken();
            h = Integer.parseInt(parm);
            oldImageFilename = st.nextToken();
        } catch (Exception e) {
            logPrint("Invalid Screen Capture command " + invoc);
            errcode = SCREENCAPTURE;
            return;
        }
        try {
            robbie.delay(3000);
            rect = new Rectangle(x, y, w, h);
            newBufferedImage = robbie.createScreenCapture(rect);
            newImageFilename = new String("Testimage" + imageCounter + ".JPG");
            writeJPEG(newBufferedImage, newImageFilename);
            logPrint("Test Screen image saved as " + newImageFilename);
            parm = oldImageFilename + " " + newImageFilename;
            fileCompare(parm);
        } catch (Exception e) {
            String s = new String("System cannot compare image; exception " + e);
            System.out.println(s);
            if (verbose) {
                JOptionPane.showMessageDialog(null, s, "SevereError", JOptionPane.WARNING_MESSAGE);
            }
            errcode = SCREENCAPTURE;
        }
    }

    /**
	 * File Compare
	 * 
	 * @param String
	 *            Invocation string
	 */
    private void fileCompare(String invoc) {
        int ct;
        String file1;
        String file2;
        int b1;
        int b2;
        StringTokenizer st;
        st = new StringTokenizer(invoc);
        if (st.countTokens() < 2) {
            logPrint("Invalid File Copy command " + invoc);
            errcode = FILECOPY;
            return;
        }
        file1 = st.nextToken();
        file2 = st.nextToken();
        try {
            FileInputStream bfr1 = new FileInputStream(file1);
            FileInputStream bfr2 = new FileInputStream(file2);
            b1 = bfr1.read();
            b2 = bfr2.read();
            ct = 0;
            try {
                while (b1 != -1) {
                    if (b1 == b2) {
                        b1 = bfr1.read();
                        b2 = bfr2.read();
                        ct++;
                    } else {
                        testpass = false;
                        logPrint("Buffers fail comparison at " + ct + "; file1 has >" + b1 + "< fil2 has >" + b2 + "<");
                        break;
                    }
                }
                bfr1.close();
                bfr2.close();
                if (testpass) {
                    logPrint("Files " + file1 + " and " + file2 + " compare OK");
                } else {
                    logPrint("Files " + file1 + " and " + file2 + " do not compare");
                }
            } catch (Exception e2) {
                logPrint("Exception at count " + ct + " is " + e2);
                logPrint("Files " + file1 + " and " + file2 + " do not compare");
            }
        } catch (Exception e) {
            String s = new String("System cannot compare " + file1 + " with " + file2 + "; exception " + e);
            System.out.println(s);
            if (verbose) {
                JOptionPane.showMessageDialog(null, s, "SevereError", JOptionPane.WARNING_MESSAGE);
            }
            errcode = FILECOMPARE;
        }
    }

    /**
	 * File Copy
	 * 
	 * @param String
	 *            Invocation string
	 */
    private void fileCopy(String invoc) {
        int i;
        String fileFrom;
        String fileTo;
        StringTokenizer st;
        st = new StringTokenizer(invoc);
        if (st.countTokens() < 2) {
            logPrint("Invalid File Copy command " + invoc);
            errcode = FILECOPY;
            return;
        }
        fileFrom = st.nextToken();
        fileTo = st.nextToken();
        try {
            BufferedReader bfr;
            BufferedWriter bfw;
            bfr = new BufferedReader(new FileReader(fileFrom));
            bfw = new BufferedWriter(new FileWriter(fileTo));
            char[] cbuffer = new char[8192];
            i = bfr.read(cbuffer, 0, 8192);
            while (i != -1) {
                bfw.write(cbuffer, 0, i);
                i = bfr.read(cbuffer, 0, 8192);
            }
            bfr.close();
            bfw.close();
            logPrint("File " + fileFrom + " copied to " + fileTo);
        } catch (Exception e) {
            String s = new String("System cannot copy " + fileFrom + " to " + fileTo + "; exception " + e);
            System.out.println(s);
            if (verbose) {
                JOptionPane.showMessageDialog(null, s, "SevereError", JOptionPane.WARNING_MESSAGE);
            }
            errcode = FILECOPY;
        }
    }

    /**
	 * Run Process
	 * 
	 * @param String
	 *            Invocation string
	 */
    private void runProcess(String invoc) {
        try {
            Runtime thisRT = Runtime.getRuntime();
            thisRT.exec(invoc);
            logPrint("Process Invocation " + invoc);
        } catch (Exception e) {
            String s = new String("System cannot run process command " + invoc + "; exception " + e);
            System.out.println(s);
            if (verbose) {
                JOptionPane.showMessageDialog(null, s, "SevereError", JOptionPane.WARNING_MESSAGE);
            }
            errcode = PROCESS;
        }
    }

    /**
	 * Save an Image to a JPEG file.
	 * 
	 * @param BufferedImage
	 *            Image to be saved to JPEG file
	 * @param String
	 *            Pathname to save file under
	 */
    private void writeJPEG(BufferedImage bi, String filename) {
        String s;
        try {
            File f = new File(filename);
            FileOutputStream fout = new FileOutputStream(f);
            JPEGEncodeParam jep = JPEGCodec.getDefaultJPEGEncodeParam(bi);
            jep.setQuality((float) 1.0, true);
            JPEGImageEncoder jimi = JPEGCodec.createJPEGEncoder(fout, jep);
            jimi.encode(bi);
            fout.close();
        } catch (Exception e) {
            s = new String("Failed to write " + filename + " exception " + e);
            logPrint(s);
            if (verbose) {
                JOptionPane.showMessageDialog(null, s, "SevereError", JOptionPane.WARNING_MESSAGE);
            }
            errcode = SCREENCAPTURE;
        }
        return;
    }

    /**
	 * Send Datagram Packet to PGUIRobotSlave.
	 * 
	 * @param String
	 *            Message to send in packet
	 */
    public void sendSlave(String msg) {
        int i;
        int l;
        String s;
        try {
            for (i = 0; i < 256; i++) bufPGUI[i] = ' ';
            l = msg.length();
            if (l > 255) l = 255;
            System.arraycopy(msg.getBytes(), 0, bufPGUI, 0, l);
            dpcktPGUI.setData(bufPGUI);
            dsPGUI.send(dpcktPGUI);
            s = new String("Sent Slave " + msg);
            logPrint(s);
        } catch (Exception e) {
            s = new String("Failed to send Message " + msg + " Exception " + e);
            System.out.println(s);
            logPrint(s);
        }
        return;
    }
}
