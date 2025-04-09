package de.fhtw.ps.mergesort.monitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.ServerSocket;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

public class Monitor implements ActionListener {

    private static final String FRAME_TITLE = "Parallel MergeSort Monitor  (c) 2007 H�rning, Miehlke, Stoll";

    private static final String FILE_DIALOG_TITLE = "please choose logfile:";

    private static final Dimension FRAME_SIZE = new Dimension(800, 600);

    private static final int FRAME_LOCATION_X = 100;

    private static final int FRAME_LOCATION_Y = 100;

    private static final int[] SPEED_VALUES = { 1, 2, 5, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 200, 500, 1000, 2000, 5000, 10000 };

    private ArrayList processors;

    private JFrame frame;

    private JTextArea textArea;

    private EventCue eventCue;

    private JScrollPane scrollPane;

    private JButton buttonOpenFile;

    private JPanel panelTree;

    private JComboBox comboBoxSpeed;

    private JButton buttonPause;

    private JCheckBox checkBoxLogActive;

    private int socketPort = 5678;

    private boolean isInitialized = false;

    private WorkerThread workerThread;

    /**
	 * Standartkonstruktor
	 *
	 */
    public Monitor() {
        eventCue = new EventCue();
        processors = new ArrayList();
        workerThread = new WorkerThread(this);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(comboBoxSpeed)) {
            eventCue.setSpeed(SPEED_VALUES[comboBoxSpeed.getSelectedIndex()]);
        }
        if (e.getSource().equals(buttonPause)) {
            eventCue.setPaused(!eventCue.isPaused());
            if (eventCue.isPaused()) {
                logEvent("monitor is paused");
                buttonPause.setText("start");
            } else {
                logEvent("monitor is started");
                buttonPause.setText("pause");
            }
        }
        if (e.getSource().equals(buttonOpenFile)) {
            JFileChooser chooser = new JFileChooser();
            chooser.addChoosableFileFilter(new FileFilter() {

                public boolean accept(File f) {
                    if (f.isDirectory()) return true;
                    return f.getName().toLowerCase().endsWith(".txt");
                }

                public String getDescription() {
                    return "txt";
                }
            });
            chooser.setMultiSelectionEnabled(false);
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                readEventsFromFile(chooser.getSelectedFile());
                new WorkerThread(this).run();
            }
        }
        if (e.getSource().equals(checkBoxLogActive)) {
        }
    }

    private void openSocket() {
        ServerSocket s = null;
        try {
            s = new ServerSocket(socketPort);
            logEvent("listen on port " + socketPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            Socket socket = null;
            try {
                socket = s.accept();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            new SocketHandler(socket, this).start();
        }
    }

    private void readEventsFromFile(File file) {
        String newEvent = null;
        logEvent("reading \"" + file.getAbsolutePath() + "\"...");
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            do {
                newEvent = in.readLine();
                if (newEvent != null) {
                    String[] ss = newEvent.split("\t");
                    for (int i = 0; i < ss.length; i++) ss[i] = ss[i].trim();
                    int rows = 0;
                    if (ss.length >= 29 && isNumber(ss[28])) rows = 5; else if (ss.length >= 23 && isNumber(ss[22])) rows = 4; else if (ss.length >= 17 && isNumber(ss[16])) rows = 3; else if (ss.length >= 11 && isNumber(ss[10])) rows = 2; else if (ss.length >= 5 && isNumber(ss[4])) rows = 1;
                    for (int i = 0; i < rows; i++) {
                        int j = i * 6;
                        if (ss != null && isNumber(ss[j]) && isNumber(ss[j + 1]) && isNumber(ss[j + 2]) && isNumber(ss[j + 3]) && isNumber(ss[j + 4])) {
                            Date startTime = new Date(Long.parseLong(ss[j]));
                            int type = Integer.parseInt(ss[j + 1]);
                            int intData1 = Integer.parseInt(ss[j + 2]);
                            int intData2 = Integer.parseInt(ss[j + 3]);
                            int intData3 = Integer.parseInt(ss[j + 4]);
                            MergeSortEvent event = new MergeSortEvent(startTime, type, intData1, intData2, intData3);
                            receiveEvent(event);
                        }
                    }
                }
            } while (newEvent != null);
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void receiveEvent(String newEvent) {
        if (newEvent != null) {
            String[] ss = newEvent.split(",");
            if (ss != null && ss.length == 5 && isNumber(ss[0]) && isNumber(ss[1]) && isNumber(ss[2]) && isNumber(ss[3]) && isNumber(ss[4])) {
                Date startTime = new Date(Long.parseLong(ss[0]));
                int type = Integer.parseInt(ss[1]);
                int intData1 = Integer.parseInt(ss[2]);
                int intData2 = Integer.parseInt(ss[3]);
                int intData3 = Integer.parseInt(ss[4]);
                MergeSortEvent event = new MergeSortEvent(startTime, type, intData1, intData2, intData3);
                receiveEvent(event);
            }
        }
    }

    /**
	 * empfaengt neue Events und stellt die in die Warteschlange
	 * @param event neues Event
	 */
    private void receiveEvent(MergeSortEvent event) {
        eventCue.add(event);
    }

    /**
	 * Zeigt einen Protokolleintrag zu einem Event an
	 * @param event Event
	 */
    public void logEvent(MergeSortEvent event) {
        logEvent(event.toString());
    }

    /**
	 * fuegt einen neuen Protokolleintrag hinzu
	 * @param text Text
	 * @param info zusaetzliche Information
	 */
    public void logEvent(String text) {
        if (checkBoxLogActive.isSelected()) {
            String now = DateFormat.getTimeInstance(DateFormat.MEDIUM).format(new Date());
            textArea.setText(now + " " + text + "\n" + textArea.getText());
        }
    }

    /**
	 * Initialisiert die grafische Benutzeroberflaeche
	 *
	 */
    private void initGUI() {
        frame = new JFrame(FRAME_TITLE);
        frame.setSize(FRAME_SIZE);
        frame.setLocation(FRAME_LOCATION_X, FRAME_LOCATION_Y);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panelNorth = new JPanel();
        frame.add(panelNorth, BorderLayout.NORTH);
        buttonOpenFile = new JButton("open logfile");
        buttonOpenFile.setVisible(false);
        buttonOpenFile.addActionListener(this);
        panelNorth.add(buttonOpenFile);
        panelNorth.add(new JLabel("speed"));
        comboBoxSpeed = new JComboBox();
        comboBoxSpeed.setMaximumRowCount(10);
        for (int i = 0; i < SPEED_VALUES.length; i++) {
            comboBoxSpeed.addItem(SPEED_VALUES[i] + "%");
        }
        comboBoxSpeed.addActionListener(this);
        comboBoxSpeed.setSelectedIndex(12);
        panelNorth.add(comboBoxSpeed);
        buttonPause = new JButton("start");
        buttonPause.addActionListener(this);
        panelNorth.add(buttonPause);
        checkBoxLogActive = new JCheckBox("show log", true);
        checkBoxLogActive.addActionListener(this);
        panelNorth.add(checkBoxLogActive);
        textArea = new JTextArea(10, 60);
        textArea.setEditable(false);
        textArea.setAutoscrolls(true);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        frame.add(scrollPane, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void doEventProcessorCount(MergeSortEvent event) {
        if (isInitialized) return;
        isInitialized = true;
        processors.clear();
        int processorCount = event.getIntData1();
        if (processorCount != 3 && processorCount != 7 && processorCount != 15 && processorCount != 31) {
            logEvent("processorCount must be 3, 7, 15 or 31!");
            return;
        }
        panelTree = new JPanel(new GridLayout(0, processorCount));
        frame.add(panelTree, BorderLayout.CENTER);
        int begin = (processorCount + 1) / 2;
        int gap = 0;
        int count = 1;
        int num = 0;
        ProcessorPanel processorPanel;
        while (count <= processorCount) {
            for (int i = 0; i < begin - 1; i++) panelTree.add(new JPanel());
            for (int x = 0; x < count - 1; x++) {
                processorPanel = new ProcessorPanel("" + (num++));
                panelTree.add(processorPanel);
                processors.add(processorPanel);
                for (int i = 0; i < gap - 1; i++) panelTree.add(new JPanel());
            }
            processorPanel = new ProcessorPanel("" + (num++));
            panelTree.add(processorPanel);
            processors.add(processorPanel);
            for (int i = 0; i < begin - 1; i++) panelTree.add(new JPanel());
            gap = begin;
            begin /= 2;
            count *= 2;
        }
        frame.setVisible(true);
    }

    private void doEventPipelineSize(MergeSortEvent event) {
        int processor = event.getIntData1();
        int pipeline = event.getIntData2();
        int size = event.getIntData3();
        if (processors != null && processor >= 0 && processor < processors.size() && pipeline >= 0 && pipeline < 4) {
            ProcessorPanel processorPanel = (ProcessorPanel) processors.get(processor);
            JProgressBar progress = null;
            switch(pipeline) {
                case 0:
                    progress = processorPanel.getProgressDone();
                    break;
                case 1:
                    progress = processorPanel.getProgressOut();
                    break;
                case 2:
                    progress = processorPanel.getProgressInLeft();
                    break;
                case 3:
                    progress = processorPanel.getProgressInRight();
                    break;
            }
            if (progress != null) progress.setMaximum(size);
        }
    }

    private void doEventPipelineFillLevel(MergeSortEvent event) {
        int processor = event.getIntData1();
        int pipeline = event.getIntData2();
        int level = event.getIntData3();
        if (processors != null && pipeline <= processors.size() - 1) {
            ProcessorPanel processorPanel = (ProcessorPanel) processors.get(processor);
            JProgressBar progress = null;
            switch(pipeline) {
                case 0:
                    progress = processorPanel.getProgressDone();
                    break;
                case 1:
                    progress = processorPanel.getProgressOut();
                    break;
                case 2:
                    progress = processorPanel.getProgressInLeft();
                    break;
                case 3:
                    progress = processorPanel.getProgressInRight();
                    break;
            }
            if (progress != null && level <= progress.getMaximum() && level >= progress.getMinimum()) {
                progress.setValue(level);
            }
        }
    }

    /**
	 * fuehrt ein Event aus, d.h. stellt die Veraenderung in der GUI dar
	 * @param event Event
	 */
    private void executeEvent(MergeSortEvent event) {
        switch(event.getType()) {
            case MergeSortEvent.PROCESSOR_COUNT:
                doEventProcessorCount(event);
                break;
            case MergeSortEvent.PIPELINE_SIZE:
                doEventPipelineSize(event);
                break;
            case MergeSortEvent.PIPELINE_FILL_LEVEL:
                doEventPipelineFillLevel(event);
                break;
        }
    }

    /**
	 * arbeitet die Event-Warteliste ab
	 *
	 */
    public void doNextEvent() {
        MergeSortEvent nextEvent = eventCue.nextEvent();
        if (nextEvent != null) {
            logEvent(nextEvent);
            executeEvent(nextEvent);
        }
    }

    private boolean isNumber(String s) {
        String validChars = "0123456789";
        for (int i = 0; i < s.length(); i++) if (validChars.indexOf(s.charAt(i)) == -1) return false;
        return true;
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            Monitor monitor = new Monitor();
            monitor.initGUI();
            monitor.eventCue.setPaused(true);
            monitor.eventCue.setSpeed(100);
            String strPort = args[0];
            if (monitor.isNumber(strPort)) {
                monitor.socketPort = Integer.parseInt(strPort);
            } else {
                monitor.readEventsFromFile(new File(args[0]));
                monitor.logEvent(monitor.eventCue.size() + " events imported");
                while (monitor.eventCue.size() > 0) monitor.doNextEvent();
            }
        } else System.err.println("Start: Monitor [port] | [logfile]");
    }
}
