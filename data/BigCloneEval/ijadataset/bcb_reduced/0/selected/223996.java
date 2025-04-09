package com.phosco.pgui.robot;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.StringTokenizer;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
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
 * <h2>PhOSCo Graphical User Interface Tester Capture Tool</h2>
 * 
 * <p>
 * This code requires Java 1.3 to compile, and the availability of the
 * com.sun.image.codec.jpeg extensions from the Sun JDK 1.3 or JRE.
 * 
 * <p>
 * This is the helper class for inclusion in instrumented applications that are
 * to be tested using PGUIRobot. This classs traps AWTEvents and sends Datagrams
 * describing them and user test commands to the PGUICapture tool.
 */
public class PGUIRobotSlave extends Component implements AWTEventListener {

    /**
	 * Version
	 */
    static String version = "1.0";

    /**
	 * Copyright
	 */
    static String copyr = new String("Copyright (c) 2000 Guillemot Design Ltd");

    /**
	 * for serializing
	 */
    private static final long serialVersionUID = -5456923747404331501L;

    /**
	 * Screen Dimension
	 */
    Dimension screenSize;

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
	 * PGUI Capture/Robot Datagram Ready Flag
	 */
    boolean readyPGUI = false;

    /**
	 * AWTEvent listening flag
	 */
    boolean listeningPGUI = true;

    /**
	 * Parent Frame
	 */
    JFrame upThere;

    /**
	 * Activity flag
	 */
    public boolean goforit;

    /**
	 * Robot Commmand flag
	 */
    public boolean beTold;

    /**
	 * Last Key flag
	 */
    int lastKeyEvent;

    /**
	 * Key Event Type
	 */
    static final int RELEASED = 1;

    /**
	 * Key Event Type
	 */
    static final int NOTRELEASED = 2;

    /**
	 * Robot Listener Thread
	 */
    ListenToRobbie ltr;

    /**
	 * Test Application Screen Window top left x
	 */
    int sox;

    /**
	 * Test Application Screen Window top left y
	 */
    int soy;

    /**
	 * Test Application Screen Window width
	 */
    int width;

    /**
	 * Test Application Screen Window height
	 */
    int height;

    /**
	 * Log file
	 */
    protected static PrintStream log;

    /**
	 * Class to listen for messages from Robot
	 */
    private final class ListenToRobbie implements Runnable {

        /**
		 * Thread activity flag
		 */
        boolean keepgoing = true;

        /**
		 * Constructor.
		 */
        public ListenToRobbie() {
        }

        /**
		 * Thread mainline
		 */
        public void run() {
            String cmd;
            while (keepgoing) {
                cmd = getRobbie();
                if (cmd.startsWith("23")) {
                    break;
                } else {
                    trilby(cmd);
                }
            }
            return;
        }
    }

    /**
	 * Constructor
	 * 
	 * @param JFrame
	 *            Parent Frame
	 */
    public PGUIRobotSlave(JFrame parent) {
        Point p;
        Dimension d;
        upThere = parent;
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
        try {
            log = new PrintStream(new FileOutputStream("PGUISlave.log"), true);
            dsPGUI = new DatagramSocket(9876);
            bufPGUI = new byte[256];
            dpcktPGUI = new DatagramPacket(bufPGUI, 256, InetAddress.getLocalHost(), 9877);
            readyPGUI = true;
            sendPGUI("Are you there, mother?");
            dsPGUI.setSoTimeout(3000);
            goforit = false;
            beTold = false;
            try {
                byte[] recbuf = new byte[256];
                DatagramPacket recdpckt = new DatagramPacket(recbuf, 256);
                dsPGUI.receive(recdpckt);
                recbuf = recdpckt.getData();
                String s = new String(recbuf);
                if (s.startsWith("All agog")) {
                    goforit = true;
                } else if (s.startsWith("Now then")) {
                    beTold = true;
                    dpcktPGUI = new DatagramPacket(recbuf, 256);
                    dsPGUI.setSoTimeout(0);
                }
            } catch (Exception e) {
            }
            if (goforit) {
                p = parent.getLocation();
                d = parent.getSize();
                sendPGUI("USING " + p.x + " " + p.y + " " + d.width + " " + d.height);
            } else if (beTold) {
                ltr = new ListenToRobbie();
                new Thread(ltr).start();
                return;
            } else {
                return;
            }
        } catch (Exception e) {
            String msg = new String("Failed to open socket or send Datagram: " + e);
            log.println(msg);
        }
        parent.addComponentListener(new ComponentAdapter() {

            public void componentMoved(ComponentEvent ce) {
                screenChange(upThere);
            }

            public void componentResized(ComponentEvent ce) {
                screenChange(upThere);
            }
        });
        parent.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent event) {
                sendPGUI("!!EOP");
            }
        });
    }

    /**
	 * Inspect AWTEvent and send appropriate Datagram
	 * 
	 * @param AWTEvent
	 */
    public void eventDispatched(AWTEvent e) {
        if (!goforit) return;
        String s;
        Point p;
        Component c;
        int kc;
        if (listeningPGUI) {
            s = e.paramString();
            c = (Component) e.getSource();
            if (s.startsWith("KEY")) {
                if (readyPGUI) {
                    if (s.startsWith("KEY_RELEASED")) {
                        if (lastKeyEvent != RELEASED) {
                            kc = getKeyCode(s);
                            if (kc == KeyEvent.VK_F1) {
                                listeningPGUI = false;
                                captureDialog(e);
                                listeningPGUI = true;
                            } else {
                                sendPGUI(s);
                            }
                        }
                        lastKeyEvent = RELEASED;
                    } else {
                        lastKeyEvent = NOTRELEASED;
                    }
                }
            } else {
                if (readyPGUI) {
                    if (c != upThere) {
                        try {
                            p = c.getLocationOnScreen();
                            ((MouseEvent) e).translatePoint(p.x, p.y);
                            s = e.paramString();
                            ((MouseEvent) e).translatePoint(-p.x, -p.y);
                        } catch (Exception e2) {
                        }
                    }
                    sendPGUI(s);
                }
            }
        }
        return;
    }

    /**
	 * Get Keycode from Datagram message
	 * 
	 * @param String
	 *            Datagram message
	 * @return int Keycode
	 */
    private int getKeyCode(String dmsg) {
        int start;
        int end;
        int kc;
        String s;
        start = dmsg.indexOf("keyCode=") + 8;
        end = dmsg.indexOf(',', start);
        s = dmsg.substring(start, end);
        try {
            kc = Integer.parseInt(s);
        } catch (Exception e) {
            kc = -1;
        }
        return kc;
    }

    /**
	 * Send Datagram Packet to PGUICapture.
	 * 
	 * @param String
	 *            Message to send in packet
	 */
    public void sendPGUI(String msg) {
        int i;
        int l;
        try {
            for (i = 0; i < 256; i++) bufPGUI[i] = ' ';
            l = msg.length();
            if (l > 255) l = 255;
            System.arraycopy(msg.getBytes(), 0, bufPGUI, 0, l);
            dpcktPGUI.setData(bufPGUI);
            dsPGUI.send(dpcktPGUI);
        } catch (Exception e) {
            log.println("Failed to send Message " + msg);
            log.println("Exception " + e);
        }
        return;
    }

    /**
	 * Provide a user dialog which can be used to save areas of the screen, save
	 * files, run setup processes, and so on.
	 * 
	 * @param AWTEvent
	 *            Triggering event
	 */
    private void captureDialog(AWTEvent trigger) {
        String pass = "Pass F1 Keystroke for Replay";
        String sAS = "Save Application Window";
        String cF = "Copy File";
        String rP = "Run process";
        String comm = "Add a Comment";
        String cmdstring;
        String savedfilename;
        int i;
        int rc;
        int x;
        int y;
        int w;
        int h;
        Robot robbie;
        BufferedImage newBufferedImage;
        Rectangle rect;
        String[] bgrp = new String[5];
        bgrp[0] = pass;
        bgrp[1] = sAS;
        bgrp[2] = cF;
        bgrp[3] = rP;
        bgrp[4] = comm;
        cmdstring = (String) JOptionPane.showInputDialog(this, "Choose option required", "PGUI Capture Program Function Selection", JOptionPane.PLAIN_MESSAGE, null, bgrp, bgrp[0]);
        if (cmdstring.equals(pass)) {
            sendPGUI(trigger.paramString());
        } else if (cmdstring.equals(sAS)) {
            try {
                robbie = new Robot();
                Point p = upThere.getLocation();
                Dimension d = upThere.getSize();
                x = p.x;
                y = p.y;
                w = (int) d.width;
                h = (int) d.height;
                rect = new Rectangle(x, y, w, h);
                newBufferedImage = robbie.createScreenCapture(rect);
                savedfilename = writeJPEG(newBufferedImage);
                sendPGUI("SC " + x + " " + y + " " + w + " " + h + " " + savedfilename);
            } catch (Exception e) {
                String s = new String("System cannot save image; exception " + e);
                log.println(s);
                JOptionPane.showMessageDialog(null, s, "SevereError", JOptionPane.WARNING_MESSAGE);
            }
        } else if (cmdstring.equals(comm)) {
            cmdstring = (String) JOptionPane.showInputDialog(this, "Comment Text", "Add a Comment at current position in script", JOptionPane.PLAIN_MESSAGE, null, null, "");
            if (cmdstring.length() != 0) {
                sendPGUI("* " + cmdstring);
            }
        } else if (cmdstring.equals(cF)) {
            cmdstring = getFileName("File to be Saved");
            savedfilename = getFileName("Name to be saved as");
            rc = JOptionPane.showConfirmDialog(null, "Save " + cmdstring + " as file " + savedfilename, "Please Confirm File Copy Function", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (rc == JOptionPane.YES_OPTION) {
                try {
                    BufferedReader bfr;
                    BufferedWriter bfw;
                    bfr = new BufferedReader(new FileReader(cmdstring));
                    bfw = new BufferedWriter(new FileWriter(savedfilename));
                    char[] cbuffer = new char[8192];
                    i = bfr.read(cbuffer, 0, 8192);
                    while (i != -1) {
                        bfw.write(cbuffer, 0, i);
                        i = bfr.read(cbuffer, 0, 8192);
                    }
                    bfr.close();
                    bfw.close();
                    sendPGUI("FC " + cmdstring + " " + savedfilename);
                } catch (Exception e) {
                    String s = new String("System cannot copy " + cmdstring + " to " + savedfilename + "; exception " + e);
                    log.println(s);
                    JOptionPane.showMessageDialog(null, s, "SevereError", JOptionPane.WARNING_MESSAGE);
                }
            }
        } else if (cmdstring.equals(rP)) {
            cmdstring = (String) JOptionPane.showInputDialog(null, "Enter Process Invocation ", "PGUI Capture Program - Run Process ", JOptionPane.PLAIN_MESSAGE, null, null, "");
            try {
                Runtime thisRT = Runtime.getRuntime();
                thisRT.exec(cmdstring);
                sendPGUI("PI " + cmdstring);
            } catch (Exception e) {
                String s = new String("System cannot run application " + cmdstring + "; exception " + e);
                log.println(s);
                JOptionPane.showMessageDialog(null, s, "SevereError", JOptionPane.WARNING_MESSAGE);
            }
        }
        return;
    }

    /**
	 * Save an Image in a JPEG file.
	 * 
	 * @param BufferedImage
	 *            Image to be saved to JPEG file
	 * @result String Pathname saved under
	 */
    private String writeJPEG(BufferedImage bi) {
        JFileChooser chooser;
        String filename;
        String s;
        File dir;
        int rc;
        chooser = new JFileChooser();
        chooser.setDialogTitle("Save Image As");
        chooser.setCurrentDirectory(new File(System.getProperty("userdir", "")));
        rc = chooser.showOpenDialog(this);
        if (rc == JFileChooser.APPROVE_OPTION) {
            filename = chooser.getSelectedFile().getName();
            dir = chooser.getCurrentDirectory();
            try {
                File f = new File(dir.getPath(), filename);
                FileOutputStream fout = new FileOutputStream(f);
                JPEGEncodeParam jep = JPEGCodec.getDefaultJPEGEncodeParam(bi);
                jep.setQuality((float) 1.0, true);
                JPEGImageEncoder jimi = JPEGCodec.createJPEGEncoder(fout);
                jimi.encode(bi);
                fout.close();
                return new String(dir + "\\" + filename);
            } catch (Exception e) {
                s = new String("Failed to write " + filename + " exception " + e);
                JOptionPane.showMessageDialog(null, s, "SevereError", JOptionPane.WARNING_MESSAGE);
                return s;
            }
        }
        return new String("");
    }

    /**
	 * Get a file name
	 * 
	 * @param String
	 *            Dialog Title
	 * @result String Pathname of file
	 */
    private String getFileName(String dlgHdr) {
        JFileChooser chooser;
        String filename;
        File dir;
        int rc;
        chooser = new JFileChooser();
        chooser.setDialogTitle(dlgHdr);
        chooser.setCurrentDirectory(new File(System.getProperty("userdir", "")));
        rc = chooser.showOpenDialog(this);
        if (rc == JFileChooser.APPROVE_OPTION) {
            filename = chooser.getSelectedFile().getName();
            dir = chooser.getCurrentDirectory();
            return new String(dir + "\\" + filename);
        }
        return new String("");
    }

    public void screenChange(JFrame parent) {
        Point p;
        Dimension d;
        p = parent.getLocation();
        d = parent.getSize();
        sendPGUI("USING " + p.x + " " + p.y + " " + d.width + " " + d.height);
        return;
    }

    /**
	 * Listen for datagram from Robot
	 * 
	 * @return String Dtaagram message
	 */
    public String getRobbie() {
        String s;
        try {
            dsPGUI.receive(dpcktPGUI);
            bufPGUI = dpcktPGUI.getData();
            s = new String(bufPGUI);
        } catch (Exception e) {
            s = e.toString();
        }
        return s;
    }

    /**
	 * Perform Robot command
	 * 
	 * @param String
	 *            command
	 */
    public void trilby(String cmd) {
        if (cmd.startsWith("Move")) {
            StringTokenizer st = new StringTokenizer(cmd);
            if (st.countTokens() == 5) {
                st.nextToken();
                try {
                    sox = Integer.parseInt(st.nextToken());
                    soy = Integer.parseInt(st.nextToken());
                    width = Integer.parseInt(st.nextToken());
                    height = Integer.parseInt(st.nextToken().trim());
                    upThere.setLocation(sox, soy);
                    upThere.setSize(width, height);
                    upThere.repaint();
                } catch (Exception e) {
                }
            }
        }
    }
}
