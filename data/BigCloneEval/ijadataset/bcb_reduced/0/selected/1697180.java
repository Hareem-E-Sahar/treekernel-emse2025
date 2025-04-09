package com.rbnb.chat;

import com.rbnb.utility.InfoDialog;
import com.rbnb.utility.RBNBProcessInterface;
import com.rbnb.utility.Utility;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CheckboxMenuItem;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.TextField;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Chat implements ActionListener, ItemListener, WindowListener, com.rbnb.api.BuildInterface, com.rbnb.chat.ClientReceiverInterface {

    private boolean isSecureHost = false;

    private boolean fancyReceipts = false;

    private java.util.Date buildDate = null;

    private String buildVersion = "V2";

    private volatile String currentName = "user", currentChatHost = "host-chat", currentGroup = "chat", currentServer = null, currentPassword = null;

    private volatile Host currentChatHostObject = null;

    private volatile boolean stopRequested = false, showDate = false, showTime = true, beepOnNew = false, useMilitary = false, useGMT = false;

    private Toolkit defTool = null;

    private volatile com.rbnb.chat.Client mcon = null;

    protected Frame displayFrame = null;

    protected SidePanel sidePanel = null;

    private TextArea displayArea = null;

    private Panel displayPanel = null;

    protected TextField inputArea = null;

    private Button alertB = null;

    private Thread listenThread = null;

    private double startTime = 0., durTime = 0.;

    private CheckboxMenuItem chosenFont = null, logging = null, showTimeCMI = null, showDateCMI = null, useMilitaryCMI = null, useGMTCMI = null, useLocalCMI = null;

    private FileOutputStream logFile = null;

    private PrintStream log = null;

    private RBNBProcessInterface processID = null;

    private static final String[] DATA_TYPES = { "Unknown", "Unknown", "Boolean", "8-bit Integer", "16-bit Integer", "32-bit Integer", "64-bit Integer", "32-bit Float", "64-bit Float", "String", "Byte Array" };

    public Chat(String server, String chatHost, String group, String name) {
        com.rbnb.api.BuildFile.loadBuildFile(this);
        currentServer = ((server == null) || (server.equals(""))) ? "localhost:3333" : new String(server);
        currentChatHost = (chatHost == null) ? "host-chat" : chatHost;
        currentGroup = group;
        currentName = name;
        displayFrame = new Frame();
        displayFrame.addWindowListener((WindowListener) this);
        displayFrame.setLayout(new BorderLayout());
        displayFrame.setMenuBar(createMenus());
        sidePanel = new SidePanel(this);
        displayFrame.add(sidePanel, "East");
        createDisplay();
        Panel panel = new Panel(new BorderLayout(10, 10));
        panel.setBackground(Color.lightGray);
        displayFrame.add(panel, "South");
        panel.add(new Label("  Message", Label.RIGHT), BorderLayout.WEST);
        inputArea = new TextField(40);
        inputArea.addActionListener(this);
        panel.add(inputArea, BorderLayout.CENTER);
        alertB = new Button("Send Alert");
        alertB.addActionListener(this);
        alertB.setEnabled(false);
        panel.add(alertB, BorderLayout.EAST);
        inputArea.setEnabled(false);
        displayFrame.setSize(700, 500);
        title();
        displayFrame.show();
        inputArea.requestFocus();
        defTool = Toolkit.getDefaultToolkit();
    }

    public Chat(String server, String chatHost, String group, String name, boolean autoconnect, boolean refresh, boolean promptForPass, RBNBProcessInterface processIDI) {
        this(server, group, chatHost, name, autoconnect, refresh, promptForPass);
        processID = processIDI;
    }

    public Chat(String server, String chatHost, String group, String name, boolean autoconnect, boolean refresh, RBNBProcessInterface processIDI) {
        this(server, chatHost, group, name, autoconnect, refresh, false);
        processID = processIDI;
    }

    public Chat(String server, String chatHost, String group, String name, boolean autoconnect, boolean refresh, boolean promptForPass) {
        this(server, chatHost, group, name);
        if (autoconnect) {
            if (promptForPass || (name == null || name.equals(""))) {
                openAction();
            } else {
                connect();
            }
            if (refresh) {
                refreshAction((long) 3600);
            }
        }
    }

    private final MenuBar createMenus() {
        Font mfont = new Font("Dialog", Font.PLAIN, 12);
        MenuItem mi;
        MenuBar mbar = new MenuBar();
        Menu fileM = new Menu("File");
        fileM.setFont(mfont);
        mi = new MenuItem("New");
        mi.setFont(mfont);
        mi.addActionListener(this);
        fileM.add(mi);
        mi = new MenuItem("Open");
        mi.setFont(mfont);
        mi.addActionListener(this);
        fileM.add(mi);
        mi = new MenuItem("Close");
        mi.setFont(mfont);
        mi.addActionListener(this);
        fileM.add(mi);
        mi = new MenuItem("Exit");
        mi.setFont(mfont);
        mi.addActionListener(this);
        fileM.add(mi);
        mbar.add(fileM);
        Menu viewM = new Menu("View");
        viewM.setFont(mfont);
        mi = new MenuItem("Clear");
        mi.setFont(mfont);
        mi.addActionListener(this);
        viewM.add(mi);
        mi = new MenuItem("-");
        viewM.add(mi);
        showDateCMI = new CheckboxMenuItem("Date");
        showDateCMI.setFont(mfont);
        showDateCMI.setState(false);
        showDateCMI.addItemListener(this);
        viewM.add(showDateCMI);
        showTimeCMI = new CheckboxMenuItem("Time");
        showTimeCMI.setFont(mfont);
        showTimeCMI.setState(true);
        showTimeCMI.addItemListener(this);
        viewM.add(showTimeCMI);
        useMilitaryCMI = new CheckboxMenuItem("Military");
        useMilitaryCMI.setFont(mfont);
        useMilitaryCMI.setState(false);
        useMilitaryCMI.addItemListener(this);
        viewM.add(useMilitaryCMI);
        mi = new MenuItem("-");
        viewM.add(mi);
        useGMTCMI = new CheckboxMenuItem("Greenwich Mean Time");
        useGMTCMI.setFont(mfont);
        useGMTCMI.setState(useGMT);
        useGMTCMI.addItemListener(this);
        viewM.add(useGMTCMI);
        useLocalCMI = new CheckboxMenuItem("Local Time");
        useLocalCMI.setFont(mfont);
        useLocalCMI.setState(!useGMT);
        useLocalCMI.addItemListener(this);
        viewM.add(useLocalCMI);
        mi = new MenuItem("-");
        viewM.add(mi);
        Menu fontMenu = new Menu("Font Size");
        fontMenu.setFont(mfont);
        viewM.add(fontMenu);
        CheckboxMenuItem cmi;
        cmi = new CheckboxMenuItem(" 8");
        cmi.setFont(mfont);
        cmi.addItemListener(this);
        fontMenu.add(cmi);
        cmi = new CheckboxMenuItem(" 9");
        cmi.setFont(mfont);
        cmi.addItemListener(this);
        fontMenu.add(cmi);
        cmi = new CheckboxMenuItem("10");
        cmi.setFont(mfont);
        cmi.addItemListener(this);
        fontMenu.add(cmi);
        cmi = new CheckboxMenuItem("11");
        cmi.setFont(mfont);
        cmi.addItemListener(this);
        fontMenu.add(cmi);
        cmi = new CheckboxMenuItem("12", true);
        cmi.setFont(mfont);
        cmi.addItemListener(this);
        fontMenu.add(cmi);
        chosenFont = cmi;
        cmi = new CheckboxMenuItem("14");
        cmi.setFont(mfont);
        cmi.addItemListener(this);
        fontMenu.add(cmi);
        cmi = new CheckboxMenuItem("16");
        cmi.setFont(mfont);
        cmi.addItemListener(this);
        fontMenu.add(cmi);
        cmi = new CheckboxMenuItem("18");
        cmi.setFont(mfont);
        cmi.addItemListener(this);
        fontMenu.add(cmi);
        mbar.add(viewM);
        Menu refM = new Menu("Refresh");
        refM.setFont(mfont);
        mi = new MenuItem("10 seconds");
        mi.setFont(mfont);
        mi.addActionListener(this);
        refM.add(mi);
        mi = new MenuItem("1 minute");
        mi.setFont(mfont);
        mi.addActionListener(this);
        refM.add(mi);
        mi = new MenuItem("10 minutes");
        mi.setFont(mfont);
        mi.addActionListener(this);
        refM.add(mi);
        mi = new MenuItem("1 hour");
        mi.setFont(mfont);
        mi.addActionListener(this);
        refM.add(mi);
        mi = new MenuItem("10 hours");
        mi.setFont(mfont);
        mi.addActionListener(this);
        refM.add(mi);
        mi = new MenuItem("1 day");
        mi.setFont(mfont);
        mi.addActionListener(this);
        refM.add(mi);
        mi = new MenuItem("10 days");
        mi.setFont(mfont);
        mi.addActionListener(this);
        refM.add(mi);
        mi = new MenuItem("-");
        refM.add(mi);
        mi = new MenuItem("All");
        mi.setFont(mfont);
        mi.addActionListener(this);
        refM.add(mi);
        mbar.add(refM);
        Menu setM = new Menu("Options");
        setM.setFont(mfont);
        cmi = new CheckboxMenuItem("Beep on New");
        cmi.setFont(mfont);
        cmi.addItemListener(this);
        setM.add(cmi);
        cmi = new CheckboxMenuItem("Log");
        cmi.setFont(mfont);
        cmi.addItemListener(this);
        logging = cmi;
        setM.add(cmi);
        mbar.add(setM);
        Menu helpM = new Menu("Help");
        helpM.setFont(mfont);
        mi = new MenuItem("About");
        mi.setFont(mfont);
        mi.addActionListener(this);
        helpM.add(mi);
        mbar.add(helpM);
        return mbar;
    }

    public static final void main(String args[]) {
        boolean autoconnect = false;
        boolean refresh = false;
        boolean promptForPass = false;
        boolean fancy = false;
        int idx, idx1, idx2;
        String inputServer = null;
        String inputChatHost = "host-chat";
        String inputGroup = "chat";
        String inputName = "user";
        if (args.length != 0) {
            autoconnect = true;
        }
        for (idx = 0; idx < args.length; ++idx) {
            if (args[idx].charAt(0) != '-') {
                inputName = args[idx];
            }
            if (args[idx].length() == 2) {
                idx1 = idx + 1;
                idx2 = 0;
            } else {
                idx1 = idx;
                idx2 = 2;
            }
            switch(args[idx].charAt(1)) {
                case 'a':
                    inputServer = args[idx1].substring(idx2);
                    break;
                case 'f':
                    refresh = true;
                    idx1 = idx;
                    break;
                case 'g':
                    inputGroup = args[idx1].substring(idx2);
                    break;
                case 'h':
                    inputChatHost = args[idx1].substring(idx2);
                    break;
                case 'u':
                    inputName = args[idx1].substring(idx2);
                    break;
                case 'p':
                    promptForPass = true;
                    idx1 = idx;
                    break;
                case 'R':
                    fancy = true;
                    if (args.length == 1) {
                        autoconnect = false;
                    }
                    break;
                default:
                    System.err.println("Unrecognized switch: " + args[idx]);
                    System.err.println("\nLegal Usage:");
                    System.err.println("-a [<RBNB host>][:<RBNB port>] :" + "set DataTurbine for connection");
                    System.err.println("-f                             : " + "refresh at start-up");
                    System.err.println("-g <groupname>                 : set groupname");
                    System.err.println("-h <chathostname>              : set chathostname");
                    System.err.println("-u <username>                  : " + "set username");
                    System.err.println("-p                             : " + "prompt for password at start-up");
                    System.err.println("-R                             : " + "provides fancy receipts.");
                    com.rbnb.utility.RBNBProcess.exit(-1);
                    break;
            }
            idx = idx1;
        }
        Chat chat = new Chat(inputServer, inputChatHost, inputGroup, inputName, autoconnect, refresh, promptForPass);
        chat.fancyReceipts = fancy;
    }

    public final void abort() {
        closeAction();
    }

    protected void createDisplay() {
        FocusListener fl = new FocusListener() {

            public void focusGained(FocusEvent feg) {
                displayArea.transferFocus();
            }

            public void focusLost(FocusEvent fel) {
            }
        };
        displayArea = new TextArea();
        displayArea.addFocusListener(fl);
        displayArea.setEditable(false);
        GridBagLayout gbl = new GridBagLayout();
        displayPanel = new Panel(gbl);
        displayPanel.setBackground(Color.lightGray);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = gbc.weighty = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbl.setConstraints(displayArea, gbc);
        displayPanel.add(displayArea);
        displayFrame.add(displayPanel, "Center");
        setDisplayFont(12);
    }

    private final void title() {
        String prefix = "rbnbChat " + getBuildVersion();
        if (mcon == null) {
            displayFrame.setTitle(prefix + ", not connected to any server");
            displayPanel.setBackground(Color.lightGray);
        } else {
            displayFrame.setTitle(prefix + ((currentChatHostObject == null) ? "" : "   *HOST*"));
            if (isSecure()) {
                displayPanel.setBackground(Color.red);
            } else {
                displayPanel.setBackground(Color.lightGray);
            }
        }
    }

    protected void setCursor(Cursor cur) {
        displayArea.setCursor(cur);
    }

    protected void setDisplayFont(int pointSize) {
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, pointSize));
    }

    private final void enableRefresh(boolean enableIt) {
    }

    protected void report(String rep) {
        displayArea.append(rep);
    }

    public final void receive(Client clientI, com.rbnb.sapi.ChannelMap messageI) {
        showMessage(clientI, messageI, false);
    }

    public final void refresh(Client clientI, com.rbnb.sapi.ChannelMap messageI) {
        showMessage(clientI, messageI, true);
    }

    private final void showMessage(Client clientI, com.rbnb.sapi.ChannelMap messageI, boolean refreshI) {
        String sender;
        String message;
        double[][] times = new double[messageI.NumberOfChannels()][];
        int[] index = new int[messageI.NumberOfChannels()];
        for (int idx = 0; idx < messageI.NumberOfChannels(); ++idx) {
            times[idx] = messageI.GetTimes(idx);
            index[idx] = 0;
        }
        boolean done = false;
        double wtime, ctime;
        int cIdx;
        boolean updateUsers = false;
        while (!done) {
            done = true;
            cIdx = -1;
            wtime = Double.MAX_VALUE;
            for (int idx = 0; idx < messageI.NumberOfChannels(); ++idx) {
                if (index[idx] < times[idx].length) {
                    done = false;
                    ctime = times[idx][index[idx]];
                    if ((cIdx == -1) || (ctime < wtime)) {
                        cIdx = idx;
                        wtime = ctime;
                    }
                }
            }
            if (!done && (cIdx >= 0)) {
                sender = messageI.GetName(cIdx);
                sender = sender.substring(clientI.getHostName().length() + 1 + clientI.getChatRoom().length());
                if (!updateUsers) {
                    if (sidePanel.chatUserList == null) {
                        updateUsers = true;
                    } else {
                        int lo = 0, hi = sidePanel.chatUserList.length - 1, idx1;
                        updateUsers = true;
                        for (int idx = (lo + hi) / 2; updateUsers && (lo <= hi); idx = (lo + hi) / 2) {
                            idx1 = sender.compareTo(sidePanel.chatUserList[idx]);
                            if (idx1 == 0) {
                                updateUsers = false;
                            } else if (idx1 < 0) {
                                hi = idx1 - 1;
                            } else {
                                lo = idx1 + 1;
                            }
                        }
                    }
                }
                String say;
                int count = 1;
                if (messageI.GetType(cIdx) == com.rbnb.sapi.ChannelMap.TYPE_STRING) {
                    say = messageI.GetDataAsString(cIdx)[index[cIdx]];
                } else {
                    count = messageI.GetTimes(cIdx).length;
                    say = ("Binary data message - " + count + " " + DATA_TYPES[messageI.GetType(cIdx)] + ((count > 1) ? "s." : "."));
                }
                String time = formatDisplayTime(com.rbnb.api.Time.since1970(wtime));
                String serv = null;
                String s = sender;
                int idx1 = s.lastIndexOf("/");
                s = s.substring(idx1 + 1);
                serv = "<" + s + ">: ";
                if (say.startsWith("\n")) {
                    say = say.substring(1);
                }
                if (say.endsWith("\n")) {
                    say = say.substring(0, say.length() - 1);
                }
                String disp = time + serv + say + "\n";
                report(disp);
                if (log != null) {
                    log.print(disp);
                    log.flush();
                }
                if (!refreshI) {
                    if (beepOnNew) {
                        defTool.beep();
                    }
                    if (say.startsWith("ALERT")) {
                        showAlert(time, serv, say);
                    }
                }
                index[cIdx] += count;
            }
            if (updateUsers) {
                sidePanel.updateUsers();
            }
        }
    }

    private String formatDisplayTime(String rawTime) {
        String time = rawTime;
        if (useGMT) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy zzz HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date d = sdf.parse(rawTime);
                String[] str = Utility.unpack(time, " ");
                time = sdf.format(d);
                String[] str2 = Utility.unpack(time, " ");
                time = str2[0] + " " + str2[1] + " " + str2[2] + str[2].substring(str[2].indexOf("."));
            } catch (Exception e) {
                return "<unknown> ";
            }
        }
        if (useMilitary) {
            String str[] = Utility.unpack(time, " ");
            String d[] = Utility.unpack(str[0], "-");
            String t[] = Utility.unpack(str[2], ":");
            return "<" + d[0] + t[0] + t[1] + t[2] + (useGMT ? "Z " : "L ") + d[1] + " " + d[2].substring(2) + "> ";
        } else {
            if (showTime && showDate) {
                return "<" + time + "> ";
            } else if (!showTime && showDate) {
                String str[] = Utility.unpack(time, " ");
                return "<" + str[0] + " " + str[1] + "> ";
            } else if (showTime && !showDate) {
                String str[] = Utility.unpack(time, " ");
                return "<" + str[2] + (useGMT ? " GMT" : "") + "> ";
            } else {
                return "";
            }
        }
    }

    private void showAlert(String time, String serv, String text) {
        String[] str = new String[] { time + serv, text };
        InfoDialog infoDialog = new InfoDialog(displayFrame, true, "Alert!", str);
        infoDialog.setLocation(Utility.centerRect(infoDialog.getBounds(), displayFrame.getBounds()));
        if (!beepOnNew) {
            defTool.beep();
        }
        infoDialog.show();
        infoDialog.dispose();
    }

    private final void listen() {
        mcon.start();
        while (!stopRequested) {
            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
                report("An error occurred while reading messages.\n");
                break;
            }
        }
        mcon.stop();
    }

    private final synchronized void startListening() {
        if (mcon != null) {
            Runnable r = new Runnable() {

                public void run() {
                    mcon.stopped = false;
                    mcon.run();
                }
            };
            listenThread = new Thread(r);
            listenThread.start();
            mcon.waitForStart();
        }
    }

    private final synchronized void stopListening() {
        stopRequested = true;
        mcon.stop();
        if ((listenThread != null) && (Thread.currentThread() != listenThread)) {
            try {
                listenThread.join();
            } catch (InterruptedException e) {
            }
        }
        stopRequested = false;
        mcon.close();
        mcon = null;
    }

    public final synchronized void itemStateChanged(ItemEvent event) {
        CheckboxMenuItem item = (CheckboxMenuItem) event.getSource();
        String label = item.getLabel();
        if (label.equals("Date")) {
            if (showDate = showDateCMI.getState()) {
                useMilitaryCMI.setState(false);
                useMilitary = false;
            }
        } else if (label.equals("Time")) {
            if (showTime = showTimeCMI.getState()) {
                useMilitaryCMI.setState(false);
                useMilitary = false;
            }
        } else if (label.equals("Military")) {
            if (useMilitary = useMilitaryCMI.getState()) {
                showTimeCMI.setState(false);
                showDateCMI.setState(false);
                showTime = false;
                showDate = false;
            }
        } else if (label.equals("Greenwich Mean Time")) {
            if (useGMT = useGMTCMI.getState()) {
                useLocalCMI.setState(false);
            } else {
                useLocalCMI.setState(true);
            }
        } else if (label.equals("Local Time")) {
            useGMT = !useLocalCMI.getState();
            if (!useGMT) {
                useGMTCMI.setState(false);
            } else {
                useGMTCMI.setState(true);
            }
        } else if (label.equals("Log")) {
            try {
                if (item.getState()) {
                    if (logFile == null) {
                        if (log != null) {
                            log.close();
                            log = null;
                        }
                        logFile = new FileOutputStream("Chat.log", true);
                    }
                    if (log == null) {
                        log = new PrintStream(logFile);
                    }
                } else {
                    if (log != null) {
                        log.close();
                        log = null;
                    }
                    if (logFile != null) {
                        logFile.close();
                        logFile = null;
                    }
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        } else if (label.equals("Beep on New")) {
            beepOnNew = item.getState();
        } else {
            chosenFont.setState(false);
            chosenFont = item;
            int point = 12;
            try {
                point = Integer.parseInt(label.trim());
            } catch (NumberFormatException e) {
            }
            setDisplayFont(point);
        }
    }

    public final synchronized void actionPerformed(ActionEvent event) {
        if (event.getSource() instanceof TextField) {
            String sendStr = inputArea.getText().trim() + "\n";
            if ((sendStr != null) && !sendStr.equals("") && !sendStr.equals("\n")) {
                try {
                    isSecureHost = mcon.send(sendStr);
                } catch (Exception e) {
                    e.printStackTrace();
                    report("An error occurred while sending a message.\n");
                }
                inputArea.setText("");
            }
        } else if (event.getSource() instanceof Button) {
            String sendStr = "ALERT: " + inputArea.getText().trim() + "\n";
            if ((sendStr != null) && !sendStr.equals("") && !sendStr.equals("ALERT: \n")) {
                try {
                    isSecureHost = mcon.send(sendStr);
                } catch (Exception e) {
                    e.printStackTrace();
                    report("An error occurred while sending a message.\n");
                }
                inputArea.setText("");
            }
        } else {
            String label = event.getActionCommand();
            if (label.equals("Open")) {
                openAction();
            } else if (label.equals("New")) {
                newAction();
            } else if (label.equals("Close")) {
                boolean doClose = true;
                if (currentChatHostObject != null) {
                    ConfirmDialog cfd = new ConfirmDialog(displayFrame, true, "Shutdown " + currentChatHostObject.getHostName(), ("Do you really want to shut down the " + currentChatHostObject.getHostName() + " chat host server?"), ConfirmDialog.CENTER_ALIGNMENT);
                    cfd.show();
                    try {
                        doClose = cfd.confirmed;
                    } finally {
                        cfd.dispose();
                    }
                }
                if (doClose) {
                    enableRefresh(false);
                    closeAction();
                }
            } else if (label.equals("Exit")) {
                exitAction();
            } else if (label.equals("Clear")) {
                clearAction();
            } else if (label.equals("All")) {
                refreshAllAction();
            } else if (label.equals("10 seconds")) {
                refreshAction((long) 10);
            } else if (label.equals("1 minute")) {
                refreshAction((long) 60);
            } else if (label.equals("10 minutes")) {
                refreshAction((long) 600);
            } else if (label.equals("1 hour")) {
                refreshAction((long) 3600);
            } else if (label.equals("10 hours")) {
                refreshAction((long) 36000);
            } else if (label.equals("1 day")) {
                refreshAction((long) 86400);
            } else if (label.equals("10 days")) {
                refreshAction((long) 864000);
            } else if (label.equals("About")) {
                Frame frame = new Frame("About rbnbChat");
                Panel panel = new Panel();
                frame.add(panel);
                frame.addWindowListener((WindowListener) this);
                panel.setLayout(new BorderLayout());
                panel.add(new Label("rbnbChat " + getBuildVersion() + " - Communicate via RBNB"), "North");
                panel.add(new Label("Copyright 2001, 2003 Creare Inc."), "Center");
                panel.add(new Label("All Rights Reserved"), "South");
                frame.setSize(300, 100);
                Rectangle bounds = displayFrame.getBounds();
                Rectangle abounds = frame.getBounds();
                frame.setLocation(bounds.x + (bounds.width - abounds.width) / 2, bounds.y + (bounds.height - abounds.height) / 2);
                frame.addNotify();
                frame.validate();
                frame.show();
            }
        }
    }

    protected void connect() {
        boolean wasListening = (mcon != null);
        displayFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            if (mcon == null) {
                mcon = new Client();
                mcon.setHostName(currentChatHost);
                mcon.setChatRoom(currentGroup);
                mcon.setCRI(this);
                mcon.open(currentServer, currentName, currentPassword);
                currentName = mcon.getName();
            } else {
                isSecureHost = mcon.send(mcon.getName() + " has left this group.");
                synchronized (mcon) {
                    mcon.setHostName(currentChatHost);
                    mcon.setChatRoom(currentGroup);
                }
            }
            isSecureHost = mcon.send(currentName + " has entered this group.\n");
            Thread.currentThread().sleep(1000);
            clearAction();
            refreshAction(10);
        } catch (Exception e) {
            stopListening();
            e.printStackTrace();
            report("An error occurred while trying to connect.\n");
            displayFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            String txt = e.getMessage();
            if ((txt != null) && (txt.indexOf("maximum number of clients") != -1)) {
                report("The maximum number of clients supported by this RBNB license has been reached!\n");
            }
            return;
        }
        title();
        sidePanel.updateHost();
        sidePanel.updateGroups();
        sidePanel.updateUsers();
        alertB.setEnabled(true);
        inputArea.setEnabled(true);
        if (!wasListening) {
            startListening();
        }
        displayFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    private final void openAction() {
        String oldChatHost = currentChatHost;
        ChatOpenDlg cod = new ChatOpenDlg(displayFrame, currentServer, currentChatHost, currentName, currentGroup);
        cod.show();
        try {
            if (cod.state != ChatOpenDlg.CANCEL) {
                currentServer = cod.serverAddress;
                currentChatHost = cod.chatHost;
                currentName = cod.username;
                currentGroup = cod.groupname;
                currentPassword = cod.password;
                if ((currentChatHostObject != null) && !currentChatHost.equals(oldChatHost)) {
                    ConfirmDialog cfd = new ConfirmDialog(displayFrame, true, "Shutdown " + currentChatHostObject.getHostName(), ("Do you really want to shut down the " + currentChatHostObject.getHostName() + " chat host server?"), ConfirmDialog.CENTER_ALIGNMENT);
                    cfd.show();
                    try {
                        if (!cfd.confirmed) {
                            return;
                        }
                    } finally {
                        cfd.dispose();
                    }
                }
                Host oldHostObject = currentChatHostObject;
                currentChatHostObject = null;
                closeAction();
                currentChatHostObject = oldHostObject;
                connect();
            }
        } finally {
            cod.dispose();
        }
    }

    private final void newAction() {
        int lio;
        String host = currentChatHost;
        if ((lio = host.lastIndexOf("/")) != -1) {
            host = host.substring(lio + 1);
        }
        ChatNewDlg cnd = new ChatNewDlg(displayFrame, currentServer, host, currentName, "chat", 1000, 0);
        cnd.show();
        if (cnd.state == ChatNewDlg.CANCEL) {
            cnd.dispose();
            return;
        } else {
            currentServer = cnd.serverAddress;
            currentChatHost = cnd.chatHost;
            currentName = cnd.username;
            currentGroup = cnd.groupname;
            currentPassword = cnd.password;
            long cache = cnd.cache;
            long archive = cnd.archive;
            cnd.dispose();
            if (currentChatHostObject != null) {
                ConfirmDialog cfd = new ConfirmDialog(displayFrame, true, "Shutdown " + currentChatHostObject.getHostName(), ("Do you really want to shut down the " + currentChatHostObject.getHostName() + " chat host server?"), ConfirmDialog.CENTER_ALIGNMENT);
                cfd.show();
                try {
                    if (!cfd.confirmed) {
                        return;
                    }
                } finally {
                    cfd.dispose();
                }
            }
            closeAction();
            try {
                currentChatHostObject = new Host(currentServer, currentChatHost, currentPassword, cache, ((archive == 0) ? "None" : "Append"), archive);
                currentChatHostObject.setFancyReceipts(fancyReceipts);
                currentChatHostObject.start();
                if (currentChatHostObject.getHostName() != null) {
                    connect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                report("An error occurred while trying to connect.\n");
                displayFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                String txt = e.getMessage();
                if ((txt != null) && (txt.indexOf("maximum number of clients") != -1)) {
                    report("The maximum number of clients supported by this RBNB license has been reached!\n");
                }
                return;
            }
        }
    }

    private final void closeAction() {
        if (mcon != null) {
            try {
                isSecureHost = mcon.send(mcon.getName() + " has left this group.");
            } catch (java.lang.Exception e) {
            }
            stopListening();
            mcon = null;
            inputArea.setEnabled(false);
            alertB.setEnabled(false);
            clearAction();
            title();
            sidePanel.updateHost();
            sidePanel.updateGroups();
            sidePanel.updateUsers();
        }
        if (currentChatHostObject != null) {
            currentChatHostObject.stop();
            currentChatHostObject = null;
        }
    }

    public void exitAction() {
        if (currentChatHostObject != null) {
            ConfirmDialog cfd = new ConfirmDialog(displayFrame, true, "Shutdown " + currentChatHostObject.getHostName(), ("Do you really want to shut down the " + currentChatHostObject.getHostName() + " chat host server?"), ConfirmDialog.CENTER_ALIGNMENT);
            cfd.show();
            try {
                if (!cfd.confirmed) {
                    return;
                }
            } finally {
                cfd.dispose();
            }
        }
        if (displayFrame != null) {
            displayFrame.setVisible(false);
        }
        closeAction();
        if (displayFrame != null) {
            displayFrame.dispose();
            displayFrame = null;
        }
        com.rbnb.utility.RBNBProcess.exit(0, processID);
    }

    protected void clearAction() {
        displayArea.setText("");
        inputArea.setText("");
    }

    protected void refreshAllAction() {
        refreshAction(0);
    }

    protected void refreshAction(long dur) {
        if (mcon == null) {
            return;
        }
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
        }
        clearAction();
        try {
            if (dur == 0) {
                mcon.refresh(Double.MAX_VALUE);
            } else {
                mcon.refresh(dur);
            }
            sidePanel.updateUsers();
        } catch (com.rbnb.sapi.SAPIException e) {
            e.printStackTrace();
        }
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    public boolean isSecure() {
        return ((currentPassword != null) && !currentPassword.equals("") && isSecureHost);
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        if ((Frame) e.getWindow() == displayFrame) {
            exitAction();
        } else if ((Frame) e.getWindow() != displayFrame) {
            ((Frame) e.getWindow()).dispose();
        }
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public final java.util.Date getBuildDate() {
        return (buildDate);
    }

    public final String getBuildVersion() {
        return (buildVersion);
    }

    public final void setBuildDate(java.util.Date buildDateI) {
        buildDate = buildDateI;
    }

    public final void setBuildVersion(String buildVersionI) {
        buildVersion = buildVersionI;
    }

    private final class SidePanel extends Panel implements ItemListener, MouseListener {

        /**
	 * the groups choice pulldown.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/16/2003
	 */
        private Choice chatGroups;

        /**
	 * the chat host text field.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/16/2003
	 */
        private TextField chatHost;

        /**
	 * the current user.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/17/2003
	 */
        private Label chatUser;

        /**
	 * the current list of users.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/21/2003
	 */
        public String[] chatUserList = null;

        /**
	 * the users list.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/16/2003
	 */
        private TextArea chatUsers;

        /**
	 * our parent chat.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/17/2003
	 */
        private Chat parent = null;

        SidePanel(Chat parentI) {
            super();
            parent = parentI;
            Font font = new Font("Dialog", Font.PLAIN, 12);
            setBackground(Color.lightGray);
            setFont(font);
            GridBagLayout gbl = new GridBagLayout();
            setLayout(gbl);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.SOUTH;
            int row = 0, col = 0;
            gbc.weightx = 100;
            gbc.weighty = 100;
            Label label = new Label("Chat Host");
            Utility.add(this, label, gbl, gbc, col, row++, 1, 1);
            chatHost = new TextField(20);
            chatHost.setFont(font);
            chatHost.setEditable(false);
            updateHost();
            Utility.add(this, chatHost, gbl, gbc, col, row++, 1, 1);
            label = new Label("");
            Utility.add(this, label, gbl, gbc, col, row++, 1, 1);
            label = new Label("Group");
            Utility.add(this, label, gbl, gbc, col, row++, 1, 1);
            chatGroups = new Choice();
            chatGroups.addMouseListener(this);
            chatGroups.addItemListener(this);
            updateGroups();
            Utility.add(this, chatGroups, gbl, gbc, col, row++, 1, 1);
            label = new Label("");
            Utility.add(this, label, gbl, gbc, col, row++, 1, 1);
            label = new Label("Users");
            Utility.add(this, label, gbl, gbc, col, row++, 1, 1);
            chatUser = new Label("");
            Utility.add(this, chatUser, gbl, gbc, col, row++, 1, 1);
            chatUsers = new TextArea(10, 20);
            chatUsers.addMouseListener(this);
            chatUsers.setFont(font);
            chatUsers.setEditable(false);
            Utility.add(this, chatUsers, gbl, gbc, col, row, 1, 11);
            row += 11;
            updateUsers();
        }

        public final void itemStateChanged(ItemEvent e) {
            String group = (String) e.getItem();
            if (group.equals("<NEW>")) {
                NewGroupDialog ngd = new NewGroupDialog(parent.displayFrame);
                ngd.show();
                if (ngd.state == ngd.CANCEL) {
                    group = currentGroup;
                    ngd.dispose();
                    return;
                } else {
                    group = ngd.groupName;
                    ngd.dispose();
                }
            }
            if ((group != null) && !group.equals("") && ((currentGroup == null) || ((group != currentGroup) && !group.equals(currentGroup)))) {
                currentGroup = group;
                connect();
            }
        }

        public final void mouseClicked(MouseEvent eventI) {
        }

        public final void mouseEntered(MouseEvent eventI) {
        }

        public final void mouseExited(MouseEvent eventI) {
        }

        public final void mousePressed(MouseEvent eventI) {
            if (eventI.getSource() == chatGroups) {
                updateGroups();
            } else if (eventI.getSource() == chatUsers) {
                updateUsers();
            }
        }

        public final void mouseReleased(MouseEvent eventI) {
        }

        final void updateGroups() {
            String[] list = new String[chatGroups.getItemCount()];
            for (int idx = 0; idx < list.length; ++idx) {
                list[idx] = chatGroups.getItem(idx);
            }
            String[] list2;
            if (mcon == null) {
                list2 = new String[1];
                list2[0] = "";
            } else {
                try {
                    String[] rooms = mcon.receiveRooms();
                    list2 = new String[rooms.length + 1];
                    System.arraycopy(rooms, 0, list2, 0, rooms.length);
                    list2[rooms.length] = "<NEW>";
                } catch (Exception e) {
                    list2 = new String[1];
                    list2[0] = "";
                }
            }
            int adjust = 1;
            for (int idx = 0, idx2 = 0; ((idx < list.length) || (idx2 < list2.length)); ) {
                if (idx >= list.length) {
                    chatGroups.add(list2[idx2++]);
                } else if (idx2 >= list2.length) {
                    chatGroups.remove(list[idx++]);
                } else {
                    int cmprd = list[idx].compareTo(list2[idx2]);
                    if (cmprd == 0) {
                        ++idx;
                        ++idx2;
                    } else if (cmprd < 0) {
                        chatGroups.remove(list[idx++]);
                        --adjust;
                    } else {
                        chatGroups.insert(list2[idx2++], idx + adjust);
                        ++adjust;
                    }
                }
            }
            if (currentGroup != null) {
                chatGroups.select(currentGroup);
            }
        }

        final void updateHost() {
            if (mcon == null) {
                chatHost.setText("");
            } else {
                chatHost.setText(currentChatHost);
            }
        }

        final void updateUsers() {
            String cUser = "", value = "";
            if (mcon != null) {
                cUser = currentName;
                try {
                    chatUserList = mcon.receiveUsers();
                    for (int idx = 0; idx < chatUserList.length; ++idx) {
                        if (!chatUserList[idx].equals(currentName)) {
                            value += chatUserList[idx] + "\n";
                        }
                    }
                } catch (Exception e) {
                }
            }
            chatUser.setText(cUser);
            chatUsers.setText(value);
        }
    }

    private final class NewGroupDialog extends Dialog implements ActionListener, WindowListener {

        /**
	 * dialog cancelled.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/17/2003
	 */
        public static final int CANCEL = 0;

        /**
	 * the group name.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/17/2003
	 */
        public String groupName = null;

        /**
	 * the new group field.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/17/2003
	 */
        private TextField newGroup = null;

        /**
	 * dialog accepted.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/17/2003
	 */
        public static final int OK = 1;

        /**
	 * the state.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 01/17/2003
	 */
        public int state = CANCEL;

        NewGroupDialog(Frame parentI) {
            super(parentI, true);
            setTitle("Create New Group");
            Font font = new Font("Dialog", Font.PLAIN, 12);
            setBackground(Color.lightGray);
            setFont(font);
            GridBagLayout gbl = new GridBagLayout();
            setLayout(gbl);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = gbc.weighty = 100;
            Label label;
            int row = 0, col = 0;
            label = new Label("New Group:");
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(15, 15, 0, 5);
            Utility.add(this, label, gbl, gbc, col++, row, 1, 1);
            newGroup = new TextField(32);
            newGroup.setText(currentGroup);
            newGroup.setFont(font);
            newGroup.setEnabled(true);
            newGroup.addActionListener(this);
            gbc.insets = new Insets(15, 0, 0, 15);
            Utility.add(this, newGroup, gbl, gbc, col, row++, 1, 1);
            col = 0;
            Panel buttonPanel = new Panel(new GridLayout(1, 2, 15, 5));
            Button okButton = new Button("OK"), cancelButton = new Button("Cancel");
            buttonPanel.add(okButton);
            okButton.addActionListener(this);
            buttonPanel.add(cancelButton);
            cancelButton.addActionListener(this);
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.insets = new Insets(15, 15, 15, 15);
            Utility.add(this, buttonPanel, gbl, gbc, col, row++, 2, 1);
            pack();
            setResizable(false);
            addWindowListener(this);
            setLocation(Utility.centerRect(getBounds(), parentI.getBounds()));
        }

        public final void actionPerformed(ActionEvent eventI) {
            if (eventI.getActionCommand().equals("Cancel")) {
                state = CANCEL;
            } else if (eventI.getActionCommand().equals("OK") || (eventI.getSource() == newGroup)) {
                groupName = newGroup.getText().trim();
                if ((groupName == currentGroup) || (groupName.indexOf("/") != -1) || groupName.equals("")) {
                    groupName = currentGroup;
                    state = CANCEL;
                } else {
                    state = OK;
                }
            } else {
                state = CANCEL;
            }
            setVisible(false);
        }

        public final void windowActivated(WindowEvent eventI) {
        }

        public final void windowClosed(WindowEvent eventI) {
        }

        public final void windowClosing(WindowEvent eventI) {
            state = CANCEL;
            setVisible(false);
        }

        public final void windowDeactivated(WindowEvent eventI) {
        }

        public final void windowDeiconified(WindowEvent eventI) {
        }

        public final void windowIconified(WindowEvent eventI) {
        }

        public final void windowOpened(WindowEvent eventI) {
        }
    }
}
