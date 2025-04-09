import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;

public class jgaapGUI extends JPanel implements ActionListener {

    public jgaapGUI() {
        initComponents();
    }

    private void initComponents() {
        currentAuthor = new JTextField();
        chooseFile = new JFileChooser();
        menuBar = new JMenuBar();
        menuFile = new JMenu();
        menuFileExit = new JMenuItem();
        menuFileDemo = new JMenuItem();
        menuHelp = new JMenu();
        menuHelpContents = new JMenuItem();
        menuHelpAbout = new JMenuItem();
        paneOpts = new JTabbedPane();
        panelAcquire = new JPanel();
        panelDocs = new JPanel();
        labelKnown = new JLabel();
        scrollPane1 = new JScrollPane();
        listKnown = new JTextArea(10, 30);
        labelUnknown = new JLabel();
        scrollPane2 = new JScrollPane();
        listUnknown = new JTextArea(10, 30);
        panelOpts = new JPanel();
        radioKnown = new JRadioButton();
        radioUnknown = new JRadioButton();
        buttonLocal = new JButton();
        panelCanonicize = new JPanel();
        panelCOpts = new JPanel();
        checkBox1 = new JCheckBox();
        checkBox2 = new JCheckBox();
        checkBox3 = new JCheckBox();
        panelProgress = new JPanel();
        buttonCanonicize = new JButton();
        label1 = new JLabel();
        progressCanonicize = new JProgressBar();
        labelCanon = new JLabel();
        panelAnalyze = new JPanel();
        labelStatInf = new JLabel();
        panelMethods = new JPanel();
        radioCrossEnt = new JRadioButton();
        radioLZW = new JRadioButton();
        panelAnalProg = new JPanel();
        buttonAnalyze = new JButton();
        labelAnalProg = new JLabel();
        progressAnalysis = new JProgressBar();
        scrollPane3 = new JScrollPane();
        listResults = new JTextArea(20, 30);
        buttonEventSet = new JButton();
        paneEvProgress = new JPanel();
        labelevSet = new JLabel();
        progressEventSet = new JProgressBar();
        panelEventSet = new JPanel();
        panelEOpts = new JPanel();
        panelEvProgress = new JPanel();
        setMinimumSize(new Dimension(400, 600));
        setPreferredSize(new Dimension(600, 400));
        setLayout(new BorderLayout());
        {
            {
                menuFile.setText("File");
                menuFileExit.setText("Exit");
                menuFile.add(menuFileExit);
                menuFileExit.addActionListener(this);
                menuFileDemo.setText("Load Demo");
                menuFileDemo.setActionCommand("demo");
                menuFileDemo.addActionListener(this);
                menuFile.add(menuFileDemo);
            }
            menuBar.add(menuFile);
            {
                menuHelp.setText("Help");
                menuHelpContents.setText("Contents (F1)");
                menuHelp.add(menuHelpContents);
                menuHelpAbout.setText("About");
                menuHelp.add(menuHelpAbout);
            }
            menuBar.add(menuHelp);
        }
        add(menuBar, BorderLayout.NORTH);
        {
            paneOpts.setFont(new Font("Garamond", Font.PLAIN, 14));
            {
                panelAcquire.setLayout(new FlowLayout());
                {
                    panelDocs.setLayout(new BoxLayout(panelDocs, BoxLayout.Y_AXIS));
                    labelKnown.setText("Registered Known Documents");
                    labelKnown.setHorizontalAlignment(SwingConstants.CENTER);
                    labelKnown.setHorizontalTextPosition(SwingConstants.CENTER);
                    panelDocs.add(labelKnown);
                    listKnown.setEnabled(false);
                    {
                        scrollPane1.setViewportView(listKnown);
                    }
                    panelDocs.add(scrollPane1);
                    labelUnknown.setText("Registered Unknown Documents");
                    labelUnknown.setHorizontalAlignment(SwingConstants.CENTER);
                    labelUnknown.setHorizontalTextPosition(SwingConstants.CENTER);
                    panelDocs.add(labelUnknown);
                    listUnknown.setEnabled(false);
                    {
                        scrollPane2.setViewportView(listUnknown);
                    }
                    panelDocs.add(scrollPane2);
                }
                panelAcquire.add(panelDocs);
                {
                    panelOpts.setLayout(new BoxLayout(panelOpts, BoxLayout.Y_AXIS));
                    radioUnknown.setText("Unknown Author");
                    radioUnknown.setActionCommand("unknownauthor");
                    radioUnknown.addActionListener(this);
                    panelOpts.add(radioUnknown);
                    radioKnown.setText("Known Author");
                    radioKnown.setActionCommand("knownauthor");
                    radioKnown.addActionListener(this);
                    panelOpts.add(radioKnown);
                    panelOpts.add(new JLabel("Author:"));
                    panelOpts.add(currentAuthor);
                    buttonLocal.setText("Local Document");
                    panelOpts.add(buttonLocal);
                    buttonLocal.setActionCommand("Import");
                    buttonLocal.addActionListener(this);
                }
                panelAcquire.add(panelOpts);
            }
            paneOpts.addTab("Acquire", panelAcquire);
            {
                panelCanonicize.setLayout(new BorderLayout());
                {
                    panelCOpts.setLayout(new GridLayout(12, 5));
                    checkBox1.setText("Smash Case");
                    checkBox1.setToolTipText("Converts all text to lower case");
                    panelCOpts.add(checkBox1);
                    checkBox2.setText("Normalize Whitespace");
                    checkBox2.setToolTipText("Converts all whitespace characters (newline, space and tab) to a single space");
                    panelCOpts.add(checkBox2);
                    checkBox3.setText("Strip HTML");
                    checkBox3.setToolTipText("Removes all HTML tags and converts hexidecimal representations to actual character representations");
                    panelCOpts.add(checkBox3);
                }
                panelCanonicize.add(panelCOpts, BorderLayout.CENTER);
                {
                    panelProgress.setLayout(new BoxLayout(panelProgress, BoxLayout.X_AXIS));
                    buttonCanonicize.setText("Canonicize");
                    buttonCanonicize.addActionListener(this);
                    panelProgress.add(buttonCanonicize);
                    label1.setText("  Progress:  ");
                    label1.setHorizontalAlignment(SwingConstants.CENTER);
                    label1.setHorizontalTextPosition(SwingConstants.CENTER);
                    panelProgress.add(label1);
                    panelProgress.add(progressCanonicize);
                }
                panelCanonicize.add(panelProgress, BorderLayout.SOUTH);
                labelCanon.setText("Document Canonicization");
                labelCanon.setHorizontalAlignment(SwingConstants.CENTER);
                panelCanonicize.add(labelCanon, BorderLayout.NORTH);
            }
            paneOpts.addTab("Canonicize", panelCanonicize);
            {
                panelEventSet.setLayout(new BorderLayout());
                {
                    panelEOpts.setLayout(new GridLayout(12, 5));
                }
                panelEventSet.add(panelEOpts, BorderLayout.CENTER);
                {
                    panelEvProgress.setLayout(new BoxLayout(panelEvProgress, BoxLayout.X_AXIS));
                    buttonEventSet.setText("Create Event Set");
                    buttonEventSet.setActionCommand("CreateEventSet");
                    buttonEventSet.addActionListener(this);
                    panelEvProgress.add(buttonEventSet);
                    labelevSet.setText("  Progress:  ");
                    labelevSet.setHorizontalAlignment(SwingConstants.CENTER);
                    labelevSet.setHorizontalTextPosition(SwingConstants.CENTER);
                    panelEvProgress.add(labelevSet);
                    panelEvProgress.add(progressEventSet);
                }
                panelEventSet.add(panelEvProgress, BorderLayout.SOUTH);
            }
            paneOpts.addTab("Event Set", panelEventSet);
            {
                panelAnalyze.setLayout(new BorderLayout());
                labelStatInf.setText("Statistical Inference");
                labelStatInf.setHorizontalAlignment(SwingConstants.CENTER);
                panelAnalyze.add(labelStatInf, BorderLayout.NORTH);
                {
                    panelMethods.setLayout(new BoxLayout(panelMethods, BoxLayout.Y_AXIS));
                    radioCrossEnt.setText("Cross Entropy");
                    panelMethods.add(radioCrossEnt);
                    radioLZW.setText("LZW");
                    panelMethods.add(radioLZW);
                }
                panelAnalyze.add(panelMethods, BorderLayout.WEST);
                {
                    panelAnalProg.setLayout(new BoxLayout(panelAnalProg, BoxLayout.X_AXIS));
                    buttonAnalyze.setText("Analyze");
                    buttonAnalyze.addActionListener(this);
                    panelAnalProg.add(buttonAnalyze);
                    labelAnalProg.setText("  Progress:  ");
                    labelAnalProg.setHorizontalAlignment(SwingConstants.CENTER);
                    labelAnalProg.setHorizontalTextPosition(SwingConstants.CENTER);
                    panelAnalProg.add(labelAnalProg);
                    panelAnalProg.add(progressAnalysis);
                }
                panelAnalyze.add(panelAnalProg, BorderLayout.SOUTH);
                listResults.setEnabled(false);
                {
                    scrollPane3.setViewportView(listResults);
                }
                panelAnalyze.add(scrollPane3, BorderLayout.CENTER);
            }
            paneOpts.addTab("Analyze", panelAnalyze);
        }
        add(paneOpts, BorderLayout.CENTER);
        buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(radioKnown);
        buttonGroup1.add(radioUnknown);
        buttonGroup2 = new ButtonGroup();
        buttonGroup2.add(radioCrossEnt);
        buttonGroup2.add(radioLZW);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("Exit")) System.exit(-1); else if (command.equals("knownauthor")) currentAuthor.setEnabled(true); else if (command.equals("unknownauthor")) currentAuthor.setEnabled(false); else if (command.equals("Import")) {
            System.out.println(buttonGroup1.getSelection().getActionCommand());
            int returnVal = chooseFile.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooseFile.getSelectedFile();
                System.out.println(file);
            }
        } else if (command.equals("demo")) {
            loadDemo();
        } else System.out.println(command);
    }

    public void loadDemo() {
        Vector<Document> docgroup = new Vector<Document>();
        Vector<DocumentSet> docsetgroup = new Vector<DocumentSet>();
    }

    private JTextField currentAuthor;

    private ButtonGroup buttonGroup1;

    private ButtonGroup buttonGroup2;

    private JFileChooser chooseFile;

    private JButton buttonEvSet;

    private JPanel paneEvProgress;

    private JLabel labelev;

    private JProgressBar progressEventSet;

    private JMenuBar menuBar;

    private JMenu menuFile;

    private JMenuItem menuFileExit;

    private JMenuItem menuFileDemo;

    private JMenu menuHelp;

    private JMenuItem menuHelpContents;

    private JMenuItem menuHelpAbout;

    private JTabbedPane paneOpts;

    private JPanel panelAcquire;

    private JPanel panelDocs;

    private JLabel labelKnown;

    private JScrollPane scrollPane1;

    private JTextArea listKnown;

    private JLabel labelUnknown;

    private JScrollPane scrollPane2;

    private JTextArea listUnknown;

    private JPanel panelOpts;

    private JRadioButton radioKnown;

    private JRadioButton radioUnknown;

    private JButton buttonLocal;

    private JPanel panelCanonicize;

    private JPanel panelCOpts;

    private JCheckBox checkBox1;

    private JCheckBox checkBox2;

    private JCheckBox checkBox3;

    private JPanel panelProgress;

    private JButton buttonCanonicize;

    private JLabel label1;

    private JProgressBar progressCanonicize;

    private JLabel labelCanon;

    private JPanel panelAnalyze;

    private JLabel labelStatInf;

    private JPanel panelMethods;

    private JRadioButton radioCrossEnt;

    private JRadioButton radioLZW;

    private JPanel panelAnalProg;

    private JButton buttonAnalyze;

    private JLabel labelAnalProg;

    private JProgressBar progressAnalysis;

    private JScrollPane scrollPane3;

    private JTextArea listResults;

    private JPanel panelEventSet;

    private JPanel panelEOpts;

    private JPanel panelEvProgress;

    private JButton buttonEventSet;

    private JLabel labelevSet;
}
