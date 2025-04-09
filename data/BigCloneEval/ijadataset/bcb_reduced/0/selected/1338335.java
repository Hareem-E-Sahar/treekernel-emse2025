package net.sourceforge.jsetimon;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.applet.*;

/** Application for monitoring the process of current SETI work units and
  * for checking the status of a user account.
  *
  * @author Adam C Jones
  * @author Kent C. Johnson
  */
public class JSETIMon extends JFrame implements Runnable {

    private static final String REVISION = "$Revision: 1.33 $";

    /** Name of the user to get statistics for. */
    private String c_tUsername = "";

    /** E-mail address used as a login to SETI@Home. */
    private String c_tUserID = "";

    /** Current rank of the user. */
    private String c_tRank = "";

    /** Current number of work units completed by the user. */
    private String c_tWorkUnits = "";

    /** Current ranking percentile among all users. */
    private String c_tPercentile = "";

    /** Number of users currently tied for this rank. */
    private String c_tTied = "";

    /** Total number of users of the SETI@Home project. */
    private String c_tTotalUsers = "";

    /** Total CPU time donated to SETI. */
    private String c_tCPUTotal = "";

    /** Average CPU time per work unit. */
    private String c_tCPUAverage = "";

    /** Number of milliseconds between updates. */
    private int c_iUserUpdateRate = 60000;

    private int c_iWorkUnitUpdateRate = 60000;

    private static final int NUM_SOUNDS = 7;

    private static final int WORK_COMPLETE = 0;

    private static final int RANK_CHANGE = 1;

    private static final int PERCENT_CHANGE = 2;

    private static final int USERS_CHANGE = 3;

    private static final int TIED_CHANGE = 4;

    private static final int CPU_TOTAL_CHANGE = 5;

    private static final int CPU_AVERAGE_CHANGE = 6;

    private boolean c_fSoundsEnabled = true;

    private String[] c_atSoundDescriptions = { "Work Unit Completed", "Rank Changed", "Percentile Changed", "Total Users Changed", "Tied at Rank Changed", "Total CPU Time Changed", "Average CPU Time/Work Unit Changed" };

    private boolean[] c_afSoundEnablers = { true, false, false, false, false, false, false };

    private AudioClip[] c_aoSoundClips = null;

    private URL[] c_aoSoundFiles = null;

    /** Vector containing information on the work units that we wish to 
     * monitor. */
    private Vector c_vWorkUnits = new Vector();

    /** Table view containing the current work unit progress information. */
    private JTable c_oTable;

    /** Table model containing the actual data on the work units. */
    private DefaultTableModel c_oTabledata;

    private static final ImageIcon SETUSER = new ImageIcon(ClassLoader.getSystemResource("images/icons/User.gif"));

    private static final ImageIcon ADDUNIT = new ImageIcon(ClassLoader.getSystemResource("images/icons/Plus.gif"));

    private static final ImageIcon REMOVEUNIT = new ImageIcon(ClassLoader.getSystemResource("images/icons/Delete.gif"));

    private static final ImageIcon ABOUT = new ImageIcon(ClassLoader.getSystemResource("images/icons/About.gif"));

    private static final ImageIcon UPDATERATE = new ImageIcon(ClassLoader.getSystemResource("images/icons/Clock.gif"));

    private static final ImageIcon SOUND = new ImageIcon(ClassLoader.getSystemResource("images/icons/Sound.gif"));

    private JButton c_oSetUser = new JButton(SETUSER);

    private JButton c_oAddUnit = new JButton(ADDUNIT);

    private JButton c_oRemoveUnit = new JButton(REMOVEUNIT);

    private JButton c_oUpdateRate = new JButton(UPDATERATE);

    private JButton c_oSound = new JButton(SOUND);

    private JButton c_oAbout = new JButton(ABOUT);

    private static final String PRE_USER = "Name (and URL)";

    private static final String POST_USER = "&nbsp;";

    private static final String PRE_RESULTS = "Results Received";

    private static final String POST_RESULTS = "Total CPU Time";

    private static final String PRE_USERS = "Your rank out of";

    private static final String POST_USERS = "total users is:";

    private static final String PRE_RANK = "total users is:";

    private static final String POST_RANK = "place.";

    private static final String PRE_TIED = "who have this rank:";

    private static final String POST_TIED = "You have completed";

    private static final String PRE_PERCENT = "work units than";

    private static final String POST_PERCENT = "of our users.";

    private static final String PRE_CPUTOTAL = "Total CPU Time";

    private static final String POST_CPUTOTAL = "Average CPU Time per work unit";

    private static final String PRE_CPUAVERAGE = "CPU Time per work unit";

    private static final String POST_CPUAVERAGE = "Average results";

    private static final String BADUSERNAME = "No user with that name was found";

    private final JLabel USERNAME = new JLabel("User:");

    private final JLabel RANK = new JLabel("Rank:");

    private final JLabel WORKUNITS = new JLabel("Work Units Completed:");

    private final JLabel TIED = new JLabel("Tied with:");

    private final JLabel PERCENTILE = new JLabel("Percentile:");

    private final JLabel TOTALUSERS = new JLabel("Total Users:");

    private final JLabel CPUTOTAL = new JLabel("Total CPU Time:");

    private final JLabel CPUAVERAGE = new JLabel("Average CPU Time:");

    private JTextField c_oUsername = new JTextField();

    private JTextField c_oRank = new JTextField();

    private JTextField c_oWorkUnits = new JTextField();

    private JTextField c_oTied = new JTextField();

    private JTextField c_oPercentile = new JTextField();

    private JTextField c_oTotalUsers = new JTextField();

    private JTextField c_oCPUTotal = new JTextField();

    private JTextField c_oCPUAverage = new JTextField();

    private static final String JSETIMON_OPTIONS_FILE = "jsetimon.ini";

    private static final String JSETIMON_AUDIO_PREFS_FILE = "sound.ini";

    private Thread c_oMonitorThread;

    /** Creates a new instance of the SETI monitor
     *
     * @author Adam C Jones
     */
    public JSETIMon() {
        super("Java SETI Monitor");
        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        getContentPane().add(splitter, BorderLayout.CENTER);
        JPanel topPane = new JPanel();
        topPane.setBackground(Color.black);
        topPane.setForeground(Color.white);
        splitter.setTopComponent(topPane);
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        getContentPane().add(toolbar, BorderLayout.NORTH);
        c_oSetUser.setToolTipText("Set UserID");
        c_oSetUser.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                UserIDDialog dlg = new UserIDDialog(getBounds());
                dlg.setUserID(c_tUserID);
                dlg.show();
                if (!(dlg.getUserID().equals(""))) {
                    try {
                        updateUserStatistics(dlg.getUserID());
                        c_tUserID = dlg.getUserID();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, "That was not a valid username. " + " Keeping old username.", "Bad Username", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        toolbar.add(c_oSetUser);
        toolbar.addSeparator();
        c_oAddUnit.setToolTipText("Add Work Unit");
        c_oAddUnit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {

                    public boolean accept(File f) {
                        if (f.isDirectory()) return true;
                        return f.getName().equals("state.sah");
                    }

                    public String getDescription() {
                        return "State Files (state.sah)";
                    }
                });
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    c_vWorkUnits.add(chooser.getSelectedFile());
                    JProgressBar bar = new JProgressBar(0, 100000000);
                    bar.setStringPainted(true);
                    bar.setString("0%");
                    Vector row = new Vector();
                    try {
                        row.add(chooser.getSelectedFile().getCanonicalPath());
                    } catch (Exception err) {
                        row.add("state.sah");
                    }
                    row.add(bar);
                    row.add("???");
                    row.add("???");
                    c_oTabledata.addRow(row);
                    c_oTable.setRowHeight(c_vWorkUnits.size() - 1, bar.getPreferredSize().height);
                    repack();
                    refresh();
                }
            }
        });
        toolbar.add(c_oAddUnit);
        c_oRemoveUnit.setToolTipText("Remove Work Unit");
        c_oRemoveUnit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                int[] rows = c_oTable.getSelectedRows();
                for (int i = rows.length; i-- != 0; ) {
                    c_vWorkUnits.removeElementAt(rows[i]);
                    c_oTabledata.removeRow(rows[i]);
                }
                repack();
                refresh();
            }
        });
        toolbar.add(c_oRemoveUnit);
        toolbar.addSeparator();
        c_oUpdateRate.setToolTipText("Set Update Rate");
        c_oUpdateRate.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                UpdateRateDialog dlg = new UpdateRateDialog();
                dlg.show();
            }
        });
        toolbar.add(c_oUpdateRate);
        c_oSound.setToolTipText("Set Sound Preferences");
        c_oSound.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SoundPreferencesDialog dlg = new SoundPreferencesDialog("Configure the sounds to be played when a " + "event occurs.", c_fSoundsEnabled, c_atSoundDescriptions, c_afSoundEnablers, c_aoSoundFiles);
                dlg.show();
                if (dlg.apply()) {
                    c_fSoundsEnabled = dlg.areSoundsEnabled();
                    c_aoSoundFiles = dlg.getConfiguredSounds();
                    for (int i = c_aoSoundFiles.length; i-- != 0; ) {
                        c_afSoundEnablers[i] = dlg.isSoundEnabled(i);
                        if (c_aoSoundFiles[i] != null) {
                            c_aoSoundClips[i] = Applet.newAudioClip(c_aoSoundFiles[i]);
                        } else {
                            c_afSoundEnablers[i] = false;
                        }
                    }
                }
            }
        });
        toolbar.add(c_oSound);
        toolbar.addSeparator();
        c_oAbout.setToolTipText("About JSETIMon");
        c_oAbout.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                String revision = "???";
                if (REVISION.indexOf(" ") != -1 && REVISION.lastIndexOf(" ") != -1) {
                    revision = REVISION.substring(REVISION.indexOf(" "), REVISION.lastIndexOf(" "));
                }
                JOptionPane.showMessageDialog(null, "JSETIMon version " + revision + "\n" + "Java SETI@Home Monitor\n" + "\n" + "Copyright (C) 2001 - 2002 Adam C Jones\n" + "http://jsetimon.sourceforge.net/\n" + "\n" + "Some contributions Copyright (C) 2002 Kent C. Johnson\n" + "\n" + "Icons Copyright (C) 1998 Dean S Jones\n" + "dean@gallant.com www.gallant.com/icons.htm\n" + "\n" + "JSETIMon is free software; you can redistribute it and/or modify\n" + "it under the terms of the GNU General Public License as published by\n" + "the Free Software Foundation; either version 2 of the License, or\n" + "(at your option) any later version.\n", "About JSETIMon", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        toolbar.add(c_oAbout);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent event) {
                try {
                    FileOutputStream fo = new FileOutputStream(JSETIMON_OPTIONS_FILE);
                    fo.write(Integer.toString(c_iWorkUnitUpdateRate).getBytes());
                    fo.write('\n');
                    fo.write(Integer.toString(c_iUserUpdateRate).getBytes());
                    fo.write('\n');
                    fo.write(c_tUserID.getBytes());
                    fo.write('\n');
                    for (int i = 0; i < c_vWorkUnits.size(); i++) {
                        fo.write(((File) c_vWorkUnits.elementAt(i)).getCanonicalPath().getBytes());
                        fo.write('\n');
                    }
                    saveAudioPreferences();
                } catch (Exception e) {
                }
                System.exit(0);
            }
        });
        c_oUsername.setHorizontalAlignment(JTextField.RIGHT);
        c_oRank.setHorizontalAlignment(JTextField.RIGHT);
        c_oWorkUnits.setHorizontalAlignment(JTextField.RIGHT);
        c_oTied.setHorizontalAlignment(JTextField.RIGHT);
        c_oPercentile.setHorizontalAlignment(JTextField.RIGHT);
        c_oTotalUsers.setHorizontalAlignment(JTextField.RIGHT);
        c_oCPUTotal.setHorizontalAlignment(JTextField.RIGHT);
        c_oCPUAverage.setHorizontalAlignment(JTextField.RIGHT);
        c_oUsername.setEditable(false);
        c_oRank.setEditable(false);
        c_oWorkUnits.setEditable(false);
        c_oTied.setEditable(false);
        c_oPercentile.setEditable(false);
        c_oTotalUsers.setEditable(false);
        c_oCPUTotal.setEditable(false);
        c_oCPUAverage.setEditable(false);
        c_oUsername.setBackground(Color.black);
        c_oRank.setBackground(Color.black);
        c_oWorkUnits.setBackground(Color.black);
        c_oTied.setBackground(Color.black);
        c_oPercentile.setBackground(Color.black);
        c_oTotalUsers.setBackground(Color.black);
        c_oCPUTotal.setBackground(Color.black);
        c_oCPUAverage.setBackground(Color.black);
        c_oUsername.setForeground(Color.white);
        c_oRank.setForeground(Color.white);
        c_oWorkUnits.setForeground(Color.white);
        c_oTied.setForeground(Color.white);
        c_oPercentile.setForeground(Color.white);
        c_oTotalUsers.setForeground(Color.white);
        c_oCPUTotal.setForeground(Color.white);
        c_oCPUAverage.setForeground(Color.white);
        c_oUsername.setBorder(null);
        c_oRank.setBorder(null);
        c_oWorkUnits.setBorder(null);
        c_oTied.setBorder(null);
        c_oPercentile.setBorder(null);
        c_oTotalUsers.setBorder(null);
        c_oCPUTotal.setBorder(null);
        c_oCPUAverage.setBorder(null);
        Color purple = new Color(0x0CC, 0x099, 0x0FF);
        USERNAME.setForeground(purple);
        RANK.setForeground(purple);
        WORKUNITS.setForeground(purple);
        TIED.setForeground(purple);
        PERCENTILE.setForeground(purple);
        TOTALUSERS.setForeground(purple);
        CPUTOTAL.setForeground(purple);
        CPUAVERAGE.setForeground(purple);
        topPane.setLayout(new GridLayout(4, 4));
        topPane.add(USERNAME);
        topPane.add(c_oUsername);
        topPane.add(PERCENTILE);
        topPane.add(c_oPercentile);
        topPane.add(RANK);
        topPane.add(c_oRank);
        topPane.add(TOTALUSERS);
        topPane.add(c_oTotalUsers);
        topPane.add(WORKUNITS);
        topPane.add(c_oWorkUnits);
        topPane.add(CPUTOTAL);
        topPane.add(c_oCPUTotal);
        topPane.add(TIED);
        topPane.add(c_oTied);
        topPane.add(CPUAVERAGE);
        topPane.add(c_oCPUAverage);
        c_oTabledata = new DefaultTableModel() {

            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        c_oTabledata.addColumn("Description");
        c_oTabledata.addColumn("% Complete");
        c_oTabledata.addColumn("Elapsed Time");
        c_oTabledata.addColumn("EToC");
        c_oTable = new JTable(c_oTabledata);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        DefaultTableCellRenderer headerrenderer = new DefaultTableCellRenderer();
        renderer.setBackground(Color.black);
        renderer.setForeground(Color.white);
        headerrenderer.setBackground(Color.black);
        headerrenderer.setForeground(purple);
        c_oTable.setGridColor(purple);
        JScrollPane scroller = new JScrollPane(c_oTable);
        splitter.setBottomComponent(scroller);
        c_oTable.getColumn("Description").setCellRenderer(renderer);
        c_oTable.getColumn("Elapsed Time").setCellRenderer(renderer);
        c_oTable.getColumn("EToC").setCellRenderer(renderer);
        c_oTable.getColumn("Description").setHeaderRenderer(headerrenderer);
        c_oTable.getColumn("Elapsed Time").setHeaderRenderer(headerrenderer);
        c_oTable.getColumn("EToC").setHeaderRenderer(headerrenderer);
        c_oTable.getColumn("% Complete").setHeaderRenderer(headerrenderer);
        c_oTable.getColumn("% Complete").setCellRenderer(new TableCellComponentRenderer());
        try {
            String nextline = null;
            JProgressBar bar = null;
            Vector row = null;
            File workunit = null;
            BufferedReader reader = new BufferedReader(new FileReader(JSETIMON_OPTIONS_FILE));
            c_iWorkUnitUpdateRate = Integer.parseInt(reader.readLine());
            c_iUserUpdateRate = Integer.parseInt(reader.readLine());
            c_tUserID = reader.readLine();
            while ((nextline = reader.readLine()) != null) {
                workunit = new File(nextline);
                c_vWorkUnits.add(workunit);
                bar = new JProgressBar(0, 100000000);
                bar.setStringPainted(true);
                bar.setString("0%");
                row = new Vector();
                row.add(nextline);
                row.add(bar);
                row.add("???");
                row.add("???");
                c_oTabledata.addRow(row);
                c_oTable.setRowHeight(c_vWorkUnits.size() - 1, bar.getPreferredSize().height);
            }
            if (c_tUserID == "") {
                UserIDDialog dlg = new UserIDDialog();
                dlg.show();
                try {
                    updateUserStatistics(dlg.getUserID());
                    c_tUserID = dlg.getUserID();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "That was not a valid username.  Keeping old username.", "Bad Username", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
        }
        loadAudioPreferences();
        c_oMonitorThread = new Thread(this);
        c_oMonitorThread.start();
        repack();
        Dimension desktopDimensions = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameDimensions = getSize();
        setLocation((desktopDimensions.width - frameDimensions.width) / 2, (desktopDimensions.height - frameDimensions.height) / 2);
        setVisible(true);
    }

    /** Starting point for a new thread.  The new thread will update the user
     * and work unit information at the update rate specified by the user.
     * TODO: This is probably not the best way to implement this feature.  
     * Running this class as a thread on its own is not the intended
     * functionality of this class, but instead a part of the implementation.
     * This functionality should be hidden in the future, preferably as an
     * internal private class.
     *
     * @author Adam C Jones
     */
    public void run() {
        while (true) {
            refresh();
            if (c_tUserID != "") {
                try {
                    updateUserStatistics(c_tUserID);
                } catch (Exception e) {
                    c_tUserID = "";
                }
            }
            try {
                Thread.sleep(c_iWorkUnitUpdateRate);
            } catch (Exception e) {
            }
        }
    }

    /** Loads the audio clips used by this application.  The user's last set
     * audio preferences are saved to a file.  That file is opened here and
     * the stored settings are loaded.  If the file does not exist, the
     * default sound preferences are loaded.  This function should be called
     * when the application starts in order to load the user's audio 
     * settings.
     *
     * @author Adam C Jones
     */
    private void loadAudioPreferences() {
        c_aoSoundClips = new AudioClip[NUM_SOUNDS];
        c_aoSoundFiles = new URL[NUM_SOUNDS];
        URL defaultsoundfile = null;
        AudioClip defaultsoundclip = null;
        try {
            defaultsoundfile = ClassLoader.getSystemResource("sounds/crowd.wav");
            defaultsoundclip = Applet.newAudioClip(defaultsoundfile);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading default sounds: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(System.err);
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(JSETIMON_AUDIO_PREFS_FILE));
            String line = reader.readLine();
            c_fSoundsEnabled = (line.charAt(0) == '1');
            int soundindex = 0;
            while ((line = reader.readLine()) != null) {
                c_afSoundEnablers[soundindex] = (line.charAt(0) == '1');
                String soundname = line.substring(line.indexOf('\t') + 1);
                if (soundname.startsWith("jar")) {
                    c_aoSoundFiles[soundindex] = defaultsoundfile;
                } else {
                    c_aoSoundFiles[soundindex] = new URL(soundname);
                }
                c_aoSoundClips[soundindex] = Applet.newAudioClip(c_aoSoundFiles[soundindex]);
                soundindex++;
            }
            if (soundindex < NUM_SOUNDS) {
                while (soundindex < NUM_SOUNDS) {
                    c_aoSoundFiles[soundindex] = defaultsoundfile;
                    c_aoSoundClips[soundindex] = defaultsoundclip;
                }
            }
        } catch (FileNotFoundException e) {
            try {
                for (int i = NUM_SOUNDS; i-- != 0; ) {
                    c_aoSoundFiles[i] = defaultsoundfile;
                    c_aoSoundClips[i] = defaultsoundclip;
                }
            } catch (Exception err) {
                JOptionPane.showMessageDialog(null, "Error loading default audio preferences: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                err.printStackTrace(System.err);
                c_fSoundsEnabled = false;
                for (int i = NUM_SOUNDS; i-- != 0; ) {
                    c_afSoundEnablers[i] = false;
                    c_aoSoundFiles[i] = null;
                    c_aoSoundClips[i] = null;
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading audio preferences: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(System.err);
            c_fSoundsEnabled = false;
            for (int i = NUM_SOUNDS; i-- != 0; ) {
                c_afSoundEnablers[i] = false;
                c_aoSoundFiles[i] = null;
                c_aoSoundClips[i] = null;
            }
        }
    }

    /** Saves the current audio preferences to a file.  The audio preferences
     * file specifies which audio tracks to play when events occur, and 
     * which events should play a sound at all.  This function should be
     * called prior to the application shutting down in order to preserve the
     * user's latest settings.  The file will be read again when the application
     * starts up.
     *
     * @author Adam C Jones
     */
    private void saveAudioPreferences() {
        try {
            FileOutputStream fo = new FileOutputStream(JSETIMON_AUDIO_PREFS_FILE);
            if (c_fSoundsEnabled) fo.write('1'); else fo.write('0');
            fo.write('\n');
            for (int i = 0; i < c_afSoundEnablers.length; i++) {
                if (c_afSoundEnablers[i]) fo.write('1'); else fo.write('0');
                fo.write('\t');
                fo.write(c_aoSoundFiles[i].toString().getBytes());
                fo.write('\n');
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error writing sound preferences: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Refreshes the status of the work units being monitored.  Each state.sah
     * file that is being monitored is queried for the current progress and
     * an estimated time to completion is calculated.  The results of this
     * query are then updated in the display of the work units.
     *
     * @author Adam C Jones
     */
    private synchronized void refresh() {
        for (int i = 0; i < c_vWorkUnits.size(); i++) {
            try {
                FileInputStream state = new FileInputStream((File) c_vWorkUnits.elementAt(i));
                int nextchar;
                StringBuffer filecontents = new StringBuffer();
                while ((nextchar = state.read()) != -1) filecontents.append((char) nextchar);
                String statetext = filecontents.toString();
                state.close();
                try {
                    int valstart;
                    int valend;
                    double progress = 0.0;
                    double cpu = 0.0;
                    JProgressBar bar = (JProgressBar) c_oTabledata.getValueAt(i, 1);
                    valstart = statetext.indexOf("prog=");
                    if (valstart != -1) {
                        valend = statetext.indexOf('\n', valstart);
                        valstart += 5;
                        if (valend == -1) valend = valstart;
                        String progstring = statetext.substring(valstart, valend);
                        try {
                            progress = Double.parseDouble(progstring);
                        } catch (Exception err) {
                            progress = 0.0;
                        }
                        bar.setValue((int) (progress * 100000000));
                        bar.setString(Integer.toString((int) (progress * 100)) + "%");
                        c_oTabledata.setValueAt(bar, i, 1);
                    }
                    valstart = statetext.indexOf("cpu=");
                    if (valstart != -1) {
                        valend = statetext.indexOf('\n', valstart);
                        valstart += 4;
                        String cpustring = statetext.substring(valstart, valend);
                        try {
                            cpu = Double.parseDouble(cpustring);
                        } catch (Exception err) {
                            cpu = 0.0;
                        }
                    }
                    StringBuffer elapsed = new StringBuffer();
                    int seconds = (int) (cpu % 60);
                    int minutes = (int) ((cpu / 60) % 60);
                    int hours = (int) (cpu / 3600);
                    if (hours < 10) elapsed.append('0');
                    elapsed.append(hours);
                    elapsed.append(":");
                    if (minutes < 10) elapsed.append('0');
                    elapsed.append(minutes);
                    elapsed.append(":");
                    if (seconds < 10) elapsed.append('0');
                    elapsed.append(seconds);
                    c_oTabledata.setValueAt(elapsed.toString(), i, 2);
                    if (progress != 0.0) {
                        double cpuleft = (cpu / progress) - cpu;
                        elapsed.delete(0, elapsed.length());
                        seconds = (int) (cpuleft % 60);
                        minutes = (int) ((cpuleft / 60) % 60);
                        hours = (int) (cpuleft / 3600);
                        if (hours < 10) elapsed.append('0');
                        elapsed.append(hours);
                        elapsed.append(":");
                        if (minutes < 10) elapsed.append('0');
                        elapsed.append(minutes);
                        elapsed.append(":");
                        if (seconds < 10) elapsed.append('0');
                        elapsed.append(seconds);
                        c_oTabledata.setValueAt(elapsed.toString(), i, 3);
                    } else {
                        c_oTabledata.setValueAt("N/A", i, 3);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace(System.out);
                }
            } catch (Exception e) {
            }
        }
    }

    /** Updates the values for the user statistics. Calling this function
     * triggers the application to request the user statistics page from
     * the SETI@Home website.  This page is then parsed to provide the data
     * for the top frame of the display.
     *
     * @param String p_tUsername - username to get statistics for.
     * 
     * @author Adam C Jones
     */
    public synchronized void updateUserStatistics(String p_tUsername) {
        if (p_tUsername == "") return;
        URL webpage = null;
        try {
            webpage = new URL("http://setiathome.ssl.berkeley.edu/fcgi-bin/fcgi?email=" + p_tUsername + "&cmd=user_stats_new");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "The username is invalid, or" + " the website is currently unavailable.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int starttag;
        int endtag;
        int lastChar;
        String user = " ";
        String workunits = " ";
        String rank = " ";
        String percent = " ";
        String users = " ";
        String tied = " ";
        String cputotal = " ";
        String cpuaverage = " ";
        String blocktext;
        boolean soundAlarm = false;
        InputStream is = null;
        try {
            is = webpage.openStream();
        } catch (Exception err) {
            return;
        }
        try {
            StringBuffer userpage = new StringBuffer();
            while ((lastChar = is.read()) != -1) {
                userpage.append((char) lastChar);
            }
            if (userpage.toString().indexOf(BADUSERNAME) != -1) {
                throw new IllegalArgumentException(BADUSERNAME);
            }
            while ((starttag = userpage.toString().indexOf('<')) != -1) {
                endtag = userpage.toString().indexOf('>');
                userpage.delete(starttag, endtag + 1);
            }
            blocktext = userpage.toString();
            starttag = blocktext.indexOf(PRE_USER);
            if (starttag != -1) {
                starttag += PRE_USER.length();
                endtag = blocktext.indexOf(POST_USER);
                user = blocktext.substring(starttag, endtag).trim();
            } else {
                user = c_tUsername;
            }
            starttag = blocktext.indexOf(PRE_RESULTS);
            if (starttag != -1) {
                starttag += PRE_RESULTS.length();
                endtag = blocktext.indexOf(POST_RESULTS);
                workunits = blocktext.substring(starttag, endtag).trim();
            } else {
                workunits = c_tWorkUnits;
            }
            starttag = blocktext.indexOf(PRE_USERS);
            if (starttag != -1) {
                starttag += PRE_USERS.length();
                endtag = blocktext.indexOf(POST_USERS);
                users = blocktext.substring(starttag, endtag).trim();
            } else {
                users = c_tTotalUsers;
            }
            starttag = blocktext.indexOf(PRE_RANK);
            if (starttag != -1) {
                starttag += PRE_RANK.length();
                endtag = blocktext.indexOf(POST_RANK);
                rank = blocktext.substring(starttag, endtag).trim();
            } else {
                rank = c_tRank;
            }
            starttag = blocktext.indexOf(PRE_TIED);
            if (starttag != -1) {
                starttag += PRE_TIED.length();
                endtag = blocktext.indexOf(POST_TIED);
                tied = blocktext.substring(starttag, endtag).trim();
            } else {
                tied = c_tTied;
            }
            starttag = blocktext.indexOf(PRE_PERCENT);
            if (starttag != -1) {
                starttag += PRE_PERCENT.length();
                endtag = blocktext.indexOf(POST_PERCENT);
                percent = blocktext.substring(starttag, endtag).trim();
            } else {
                percent = c_tPercentile;
            }
            starttag = blocktext.indexOf(PRE_CPUTOTAL);
            if (starttag != -1) {
                starttag += PRE_CPUTOTAL.length();
                endtag = blocktext.indexOf(POST_CPUTOTAL);
                cputotal = blocktext.substring(starttag, endtag).trim();
            } else {
                cputotal = c_tCPUTotal;
            }
            starttag = blocktext.indexOf(PRE_CPUAVERAGE);
            if (starttag != -1) {
                starttag += PRE_CPUAVERAGE.length();
                endtag = blocktext.indexOf(POST_CPUAVERAGE);
                cpuaverage = blocktext.substring(starttag, endtag).trim();
            } else {
                cpuaverage = c_tCPUAverage;
            }
            if (user.equals(c_tUsername)) {
                c_oUsername.setForeground(Color.white);
            } else {
                c_tUsername = user;
                c_oUsername.setForeground(Color.yellow);
                c_oUsername.setText(c_tUsername);
            }
            if (workunits.equals(c_tWorkUnits)) {
                c_oWorkUnits.setForeground(Color.white);
            } else {
                if (c_fSoundsEnabled && c_afSoundEnablers[WORK_COMPLETE]) c_aoSoundClips[WORK_COMPLETE].play();
                c_tWorkUnits = workunits;
                c_oWorkUnits.setForeground(Color.yellow);
                c_oWorkUnits.setText(c_tWorkUnits);
            }
            if (rank.equals(c_tRank)) {
                c_oRank.setForeground(Color.white);
            } else {
                if (c_fSoundsEnabled && c_afSoundEnablers[RANK_CHANGE]) c_aoSoundClips[RANK_CHANGE].play();
                c_tRank = rank;
                c_oRank.setForeground(Color.yellow);
                c_oRank.setText(c_tRank);
            }
            if (percent.equals(c_tPercentile)) {
                c_oPercentile.setForeground(Color.white);
            } else {
                if (c_fSoundsEnabled && c_afSoundEnablers[PERCENT_CHANGE]) c_aoSoundClips[PERCENT_CHANGE].play();
                c_tPercentile = percent;
                c_oPercentile.setForeground(Color.yellow);
                c_oPercentile.setText(c_tPercentile);
            }
            if (users.equals(c_tTotalUsers)) {
                c_oTotalUsers.setForeground(Color.white);
            } else {
                if (c_fSoundsEnabled && c_afSoundEnablers[USERS_CHANGE]) c_aoSoundClips[USERS_CHANGE].play();
                c_tTotalUsers = users;
                c_oTotalUsers.setForeground(Color.yellow);
                c_oTotalUsers.setText(c_tTotalUsers);
            }
            if (tied.equals(c_tTied)) {
                c_oTied.setForeground(Color.white);
            } else {
                if (c_fSoundsEnabled && c_afSoundEnablers[TIED_CHANGE]) c_aoSoundClips[TIED_CHANGE].play();
                c_tTied = tied;
                c_oTied.setForeground(Color.yellow);
                c_oTied.setText(c_tTied);
            }
            if (cputotal.equals(c_tCPUTotal)) {
                c_oCPUTotal.setForeground(Color.white);
            } else {
                if (c_fSoundsEnabled && c_afSoundEnablers[CPU_TOTAL_CHANGE]) c_aoSoundClips[CPU_TOTAL_CHANGE].play();
                c_tCPUTotal = cputotal;
                c_oCPUTotal.setForeground(Color.yellow);
                c_oCPUTotal.setText(c_tCPUTotal);
            }
            if (cpuaverage.equals(c_tCPUAverage)) {
                c_oCPUAverage.setForeground(Color.white);
            } else {
                if (c_fSoundsEnabled && c_afSoundEnablers[CPU_AVERAGE_CHANGE]) c_aoSoundClips[CPU_AVERAGE_CHANGE].play();
                c_tCPUAverage = cpuaverage;
                c_oCPUAverage.setForeground(Color.yellow);
                c_oCPUAverage.setText(c_tCPUAverage);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    private void repack() {
        c_oTable.setPreferredScrollableViewportSize(c_oTable.getPreferredSize());
        pack();
    }

    /** Launches the application.
     *
     * @param String[] args - a list of the command line arguments sent to
     *                        this application in the form of an array of 
     *                        strings.
     *
     * @author Adam C Jones
     */
    public static void main(String[] args) {
        new JSETIMon();
    }

    /** Dialog box for entering the UserID. This is a specialized internal 
     * class for requesting the SETI@Hom username to display statistics for.
     *
     * @author Adam C Jones
     */
    private class UserIDDialog extends JDialog {

        private final JLabel MESSAGE = new JLabel("Enter your SETI@Home login");

        private JTextField c_oUserID = new JTextField();

        private JButton c_oOK = new JButton("OK");

        private JButton c_oCancel = new JButton("Cancel");

        /** Creates a new instance of the UserIDDialog class and prepares
        * the dialog box for display.
        *
        * @author Adam C Jones
        */
        public UserIDDialog() {
            setModal(true);
            getContentPane().setLayout(new BorderLayout());
            JPanel buttons = new JPanel();
            buttons.add(c_oOK);
            c_oOK.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    hide();
                }
            });
            buttons.add(c_oCancel);
            c_oCancel.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    c_oUserID.setText("");
                    hide();
                }
            });
            c_oUserID.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    c_oOK.doClick();
                }
            });
            getContentPane().add(MESSAGE, BorderLayout.NORTH);
            getContentPane().add(c_oUserID, BorderLayout.CENTER);
            getContentPane().add(buttons, BorderLayout.SOUTH);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            setTitle("SETI@Home User");
            pack();
        }

        /** Creates a new instance of the UserIDDialog class with dimensions
        * and location specified by supplied Rectangle and prepares the 
        * dialog box for display.
        *
        * @param Rectangle p_oBounds - Specifies the size and position of the 
        *                              dialog box.
        *
        * @author Adam C Jones
        */
        public UserIDDialog(Rectangle p_oBounds) {
            setModal(true);
            getContentPane().setLayout(new BorderLayout());
            JPanel buttons = new JPanel();
            buttons.add(c_oOK);
            c_oOK.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    hide();
                }
            });
            buttons.add(c_oCancel);
            c_oCancel.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    c_oUserID.setText("");
                    hide();
                }
            });
            c_oUserID.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    c_oOK.doClick();
                }
            });
            getContentPane().add(MESSAGE, BorderLayout.NORTH);
            getContentPane().add(c_oUserID, BorderLayout.CENTER);
            getContentPane().add(buttons, BorderLayout.SOUTH);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            setTitle("SETI@Home User");
            pack();
            setLocation(p_oBounds.x + ((p_oBounds.width - getWidth()) >> 1), p_oBounds.y + ((p_oBounds.height - getHeight()) >> 1));
        }

        /** Returns the UserID that the user entered in the dialog.
        *
        * @return String - new User ID value.
        *
        * @author Adam C Jones
        */
        public String getUserID() {
            return c_oUserID.getText();
        }

        /** Sets the string value to appear in the User ID text box of this
        * dialog box.
        *
        * @param String p_tUserID - value to be shown in text box.
        *
        * @author Adam C Jones
        */
        public void setUserID(String p_tUserID) {
            c_oUserID.setText(p_tUserID);
        }
    }

    /** Class which defines a dialog box for setting the update rate.  The
     * update rates set in this dialog determine how often the monitored files
     * and user information will be updated.
     *
     * @author Adam C Jones
     */
    private class UpdateRateDialog extends JDialog {

        private boolean c_fApply;

        private JTextField c_oWorkUnitUpdateRate;

        private JButton ok, cancel;

        /** Creates a new instance of the UpdateRateDialog object and prepares
        * it for display.
        *
        * @author Adam C Jones
        * @author Kent C. Johnson
        */
        public UpdateRateDialog() {
            c_fApply = false;
            setTitle("Update Rate");
            setModal(true);
            JLabel message = new JLabel("Enter the amount of time, in seconds, between updates of " + "the user and work unit information.");
            getContentPane().setLayout(new BorderLayout());
            ok = new JButton("OK");
            cancel = new JButton("Cancel");
            JPanel buttons = new JPanel();
            buttons.add(ok);
            buttons.add(cancel);
            c_oWorkUnitUpdateRate = new JTextField(Integer.toString(c_iWorkUnitUpdateRate / 1000), 10);
            getContentPane().add(buttons, BorderLayout.SOUTH);
            getContentPane().add(message, BorderLayout.NORTH);
            getContentPane().add(c_oWorkUnitUpdateRate, BorderLayout.CENTER);
            cancel.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    hide();
                }
            });
            ok.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    try {
                        int updaterate = Integer.parseInt(c_oWorkUnitUpdateRate.getText());
                        if (updaterate < 1) throw new NumberFormatException("Update rate must" + " be a positive integer.");
                        c_iWorkUnitUpdateRate = updaterate * 1000;
                        c_oMonitorThread.interrupt();
                        c_fApply = true;
                        hide();
                    } catch (Exception err) {
                        JOptionPane.showMessageDialog(null, "You must enter a valid positive integer for " + "the update rate.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            c_oUpdateRate.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    ok.doClick();
                }
            });
            setSize(200, 100);
            pack();
        }

        /** Returns the current value of the apply property which indicates if
        * the user accepted the changes specified in this dialog box or 
        * cancelled them.
        *
        * @return boolean - true indicates the user accepted the changes,
        *                   fales indicates the user rejected the changes.
        *
        * @author Adam C Jones
        */
        public boolean apply() {
            return c_fApply;
        }
    }
}
