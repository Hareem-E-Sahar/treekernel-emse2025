import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.JOptionPane;
import javax.swing.filechooser.*;

public class Interface extends JFrame implements ActionListener {

    boolean isWindows = true;

    boolean bCross = true;

    boolean bLZW = false;

    boolean bWord = true;

    boolean bLetter = false;

    boolean bYesPre = true;

    boolean bNoPre = false;

    Processor myProcessor = new Processor();

    final JFileChooser chooseFile = new JFileChooser();

    public Interface() {
        super("Authorship Processor");
    }

    JLabel blank = new JLabel("\t");

    JLabel blank2 = new JLabel("\t");

    JLabel blank3 = new JLabel("\t");

    JLabel sampFileLabel = new JLabel("Sample Filename");

    JLabel blank4 = new JLabel("\t");

    JLabel sampAuthorLabel = new JLabel("Sample Author");

    JLabel blank5 = new JLabel("\t");

    JLabel testFileLabel = new JLabel("Testing Filename");

    JLabel blank6 = new JLabel("\t");

    JLabel testAuthorLabel = new JLabel("Testing Author");

    JLabel blank7 = new JLabel("\t");

    JLabel blank8 = new JLabel("\t");

    JTextArea sampFilenames = new JTextArea(10, 10);

    JTextArea sampAuthors = new JTextArea(10, 10);

    JTextArea testFilenames = new JTextArea(10, 10);

    JTextArea testAuthors = new JTextArea(10, 10);

    public JMenuBar createMenuBar() {
        JMenuBar mnuBar = new JMenuBar();
        setJMenuBar(mnuBar);
        JMenu mnuFile = new JMenu("File", true);
        mnuFile.setMnemonic(KeyEvent.VK_F);
        mnuBar.add(mnuFile);
        mnuFile.setToolTipText("Perform operations on documents");
        JMenuItem mnuAddSample = new JMenuItem("Add Sample Document");
        mnuAddSample.setMnemonic(KeyEvent.VK_S);
        mnuFile.add(mnuAddSample);
        mnuAddSample.setActionCommand("AddSample");
        mnuAddSample.addActionListener(this);
        JMenuItem mnuAddTesting = new JMenuItem("Add Testing Document");
        mnuAddTesting.setMnemonic(KeyEvent.VK_T);
        mnuFile.add(mnuAddTesting);
        mnuAddTesting.setActionCommand("AddTesting");
        mnuAddTesting.addActionListener(this);
        JMenuItem mnuProcess = new JMenuItem("Process Documents");
        mnuProcess.setMnemonic(KeyEvent.VK_P);
        mnuFile.add(mnuProcess);
        mnuProcess.setActionCommand("Process");
        mnuProcess.addActionListener(this);
        JMenuItem mnuClear = new JMenuItem("Clear Documents");
        mnuClear.setMnemonic(KeyEvent.VK_C);
        mnuFile.add(mnuClear);
        mnuClear.setActionCommand("Clear");
        mnuClear.addActionListener(this);
        JMenuItem mnuExit = new JMenuItem("Exit");
        mnuExit.setMnemonic(KeyEvent.VK_E);
        mnuFile.add(mnuExit);
        mnuExit.setActionCommand("Exit");
        mnuExit.addActionListener(this);
        JMenu mnuAlgo = new JMenu("Algorithm", true);
        mnuAlgo.setMnemonic(KeyEvent.VK_A);
        mnuBar.add(mnuAlgo);
        mnuAlgo.setToolTipText("Change processing algorithm");
        ButtonGroup grpAlgo = new ButtonGroup();
        JRadioButtonMenuItem algoItem = new JRadioButtonMenuItem("Cross - Entropy");
        algoItem.setSelected(true);
        algoItem.setMnemonic(KeyEvent.VK_C);
        grpAlgo.add(algoItem);
        mnuAlgo.add(algoItem);
        algoItem.setActionCommand("Cross");
        algoItem.addActionListener(this);
        algoItem = new JRadioButtonMenuItem("LZW (*NIX Only)");
        algoItem.setMnemonic(KeyEvent.VK_L);
        grpAlgo.add(algoItem);
        mnuAlgo.add(algoItem);
        algoItem.setActionCommand("LZW");
        algoItem.addActionListener(this);
        JMenu mnuEvent = new JMenu("Event Type", true);
        mnuEvent.setMnemonic(KeyEvent.VK_E);
        mnuBar.add(mnuEvent);
        mnuEvent.setToolTipText("Change definition of events");
        ButtonGroup grpEvent = new ButtonGroup();
        JRadioButtonMenuItem eventItem = new JRadioButtonMenuItem("Word");
        eventItem.setSelected(true);
        eventItem.setMnemonic(KeyEvent.VK_W);
        grpEvent.add(eventItem);
        mnuEvent.add(eventItem);
        eventItem.setActionCommand("Word");
        eventItem.addActionListener(this);
        eventItem = new JRadioButtonMenuItem("Letter");
        eventItem.setSelected(false);
        eventItem.setMnemonic(KeyEvent.VK_L);
        grpEvent.add(eventItem);
        mnuEvent.add(eventItem);
        eventItem.setActionCommand("Letter");
        eventItem.addActionListener(this);
        JMenu mnuPre = new JMenu("Preprocessing", true);
        mnuPre.setMnemonic(KeyEvent.VK_P);
        mnuBar.add(mnuPre);
        mnuPre.setToolTipText("Turn preprocessing on/off");
        ButtonGroup grpPre = new ButtonGroup();
        JRadioButtonMenuItem preItem = new JRadioButtonMenuItem("Yes");
        preItem.setSelected(true);
        preItem.setMnemonic(KeyEvent.VK_Y);
        grpPre.add(preItem);
        mnuPre.add(preItem);
        preItem.setActionCommand("Yes");
        preItem.addActionListener(this);
        preItem = new JRadioButtonMenuItem("No");
        preItem.setSelected(false);
        preItem.setMnemonic(KeyEvent.VK_N);
        grpPre.add(preItem);
        mnuPre.add(preItem);
        preItem.setActionCommand("No");
        preItem.addActionListener(this);
        JMenu mnuHelp = new JMenu("Help", true);
        mnuHelp.setMnemonic(KeyEvent.VK_H);
        mnuBar.add(mnuHelp);
        mnuHelp.setToolTipText("Instructions and program information");
        JMenuItem mnuInst = new JMenuItem("Instructions");
        mnuInst.setMnemonic(KeyEvent.VK_I);
        mnuHelp.add(mnuInst);
        mnuInst.setActionCommand("Instructions");
        mnuInst.addActionListener(this);
        JMenuItem mnuAbout = new JMenuItem("About");
        mnuAbout.setMnemonic(KeyEvent.VK_A);
        mnuHelp.add(mnuAbout);
        mnuAbout.setActionCommand("About");
        mnuAbout.addActionListener(this);
        return mnuBar;
    }

    public Container createContentPane() {
        JButton addSamp = new JButton("Add Sample Document");
        addSamp.setActionCommand("AddSample");
        addSamp.addActionListener(this);
        JButton addTest = new JButton("Add Testing Document");
        addTest.setActionCommand("AddTesting");
        addTest.addActionListener(this);
        JButton clearDocs = new JButton("Clear Documents");
        clearDocs.setActionCommand("Clear");
        clearDocs.addActionListener(this);
        JButton processDocs = new JButton("Process Documents");
        processDocs.setActionCommand("Process");
        processDocs.addActionListener(this);
        sampFileLabel.setToolTipText("Displays filenames of sample documents");
        sampAuthorLabel.setToolTipText("Displays sample document's author");
        testFileLabel.setToolTipText("Displays filenames of testing documents");
        testAuthorLabel.setToolTipText("Displays authorship of testing documents");
        addSamp.setToolTipText("Click to add sample documents");
        addTest.setToolTipText("Click to add testing documents");
        clearDocs.setToolTipText("Click to unload all documents");
        processDocs.setToolTipText("Click to process documents");
        sampFilenames.setToolTipText("Displays filenames of sample documents");
        sampAuthors.setToolTipText("Displays sample document's author");
        testFilenames.setToolTipText("Displays filenames of testing documents");
        testAuthors.setToolTipText("Displays authorship of testing documents");
        sampFilenames.setEditable(false);
        sampAuthors.setEditable(false);
        testFilenames.setEditable(false);
        testAuthors.setEditable(false);
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(1, 1));
        northPanel.add(sampFileLabel);
        northPanel.add(blank4);
        northPanel.add(sampAuthorLabel);
        northPanel.add(blank5);
        northPanel.add(blank7);
        northPanel.add(testFileLabel);
        northPanel.add(blank6);
        northPanel.add(testAuthorLabel);
        northPanel.setVisible(true);
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(1, 1));
        centerPanel.add(sampFilenames);
        centerPanel.add(blank);
        centerPanel.add(sampAuthors);
        centerPanel.add(blank2);
        centerPanel.add(blank8);
        centerPanel.add(testFilenames);
        centerPanel.add(blank3);
        centerPanel.add(testAuthors);
        centerPanel.setVisible(true);
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new GridLayout(1, 1));
        southPanel.add(addSamp);
        southPanel.add(addTest);
        southPanel.add(clearDocs);
        southPanel.add(processDocs);
        southPanel.setVisible(true);
        Container c = getContentPane();
        c.setLayout(new BorderLayout(10, 10));
        c.add(northPanel, BorderLayout.NORTH);
        c.add(centerPanel, BorderLayout.CENTER);
        c.add(southPanel, BorderLayout.SOUTH);
        return c;
    }

    public void actionPerformed(ActionEvent e) {
        String pretype = null;
        String eventtype = null;
        String stNumWorks = new String();
        String arg = e.getActionCommand();
        if (arg.equals("AddSample")) {
            int iReturnValue = chooseFile.showOpenDialog(Interface.this);
            try {
                Process p = Runtime.getRuntime().exec("uname");
                BufferedInputStream buffer = new BufferedInputStream(p.getInputStream());
                DataInputStream commandResult = new DataInputStream(buffer);
                String s = null;
                s = commandResult.readLine();
                isWindows = false;
                commandResult.close();
            } catch (Exception g) {
            }
            if (iReturnValue == JFileChooser.APPROVE_OPTION) {
                String stAuthor = JOptionPane.showInputDialog("Who is the author of this document?");
                File file = chooseFile.getSelectedFile();
                File path = chooseFile.getCurrentDirectory();
                String fullPath = null;
                if (isWindows) {
                    fullPath = path.toString() + "\\" + file.getName();
                } else {
                    fullPath = path.toString() + "/" + file.getName();
                }
                myProcessor.addSampleWork(fullPath, stAuthor);
                sampFilenames.append(file.getName() + "\n");
                sampAuthors.append(stAuthor + "\n");
            }
        } else if (arg.equals("AddTesting")) {
            int iReturnValue = chooseFile.showOpenDialog(Interface.this);
            try {
                Process p = Runtime.getRuntime().exec("uname");
                BufferedInputStream buffer = new BufferedInputStream(p.getInputStream());
                DataInputStream commandResult = new DataInputStream(buffer);
                String s = null;
                s = commandResult.readLine();
                isWindows = false;
                commandResult.close();
            } catch (Exception g) {
            }
            if (iReturnValue == JFileChooser.APPROVE_OPTION) {
                File file = chooseFile.getSelectedFile();
                File path = chooseFile.getCurrentDirectory();
                String fullPath = null;
                if (isWindows) {
                    fullPath = path.toString() + "\\" + file.getName();
                } else {
                    fullPath = path.toString() + "/" + file.getName();
                }
                myProcessor.addTestingWork(fullPath, "");
                testFilenames.append(file.getName() + "\n");
            }
        } else if (arg.equals("Process")) {
            sampFilenames.setEditable(true);
            sampAuthors.setEditable(true);
            testFilenames.setEditable(true);
            testAuthors.setEditable(true);
            testAuthors.selectAll();
            testAuthors.cut();
            sampFilenames.setEditable(false);
            sampAuthors.setEditable(false);
            testFilenames.setEditable(false);
            testAuthors.setEditable(false);
            if (bWord) {
                eventtype = "Word";
            } else {
                eventtype = "Letter";
            }
            if (bYesPre) {
                pretype = "Yes";
            } else {
                pretype = "No";
            }
            if (bCross) {
                myProcessor.createData(eventtype, pretype);
                myProcessor.crossEntDistance(testAuthors);
            } else if (bLZW) {
                myProcessor.createData(eventtype, pretype);
                myProcessor.LZWDistance(testAuthors);
            }
        } else if (arg.equals("Clear")) {
            myProcessor.sampleWorks.removeAllElements();
            myProcessor.testingWorks.removeAllElements();
            sampFilenames.setEditable(true);
            sampAuthors.setEditable(true);
            testFilenames.setEditable(true);
            testAuthors.setEditable(true);
            sampFilenames.selectAll();
            sampFilenames.cut();
            testFilenames.selectAll();
            testFilenames.cut();
            sampAuthors.selectAll();
            sampAuthors.cut();
            testAuthors.selectAll();
            testAuthors.cut();
            sampFilenames.setEditable(false);
            sampAuthors.setEditable(false);
            testFilenames.setEditable(false);
            testAuthors.setEditable(false);
        } else if (arg.equals("Word")) {
            bWord = true;
            bLetter = false;
        } else if (arg.equals("Letter")) {
            bLetter = true;
            bWord = false;
        } else if (arg.equals("Yes")) {
            bYesPre = true;
            bNoPre = false;
        } else if (arg.equals("No")) {
            bNoPre = true;
            bYesPre = false;
        } else if (arg.equals("Exit")) {
            System.exit(0);
        } else if (arg.equals("Cross")) {
            bCross = true;
            bLZW = false;
        } else if (arg.equals("LZW")) {
            bCross = false;
            bLZW = true;
        } else if (arg.equals("Instructions")) {
            JOptionPane.showMessageDialog(null, "Step 1: Add Sample Documents, Entering Their Author in the Dialog Box\nStep 2: Add Testing Documents\nStep 3: Choose Processing Algorithm From Menu (Default: Cross-Entropy)\nStep 4: Choose Event Type From Menu (Default: Word)\nStep 5: Choose Preprocessing / No Preprocessing (Default: Preprocessing)\nStep 6: Click Process Documents\nStep 7: Results Will Be Displayed", "Instructions", JOptionPane.PLAIN_MESSAGE);
        } else if (arg.equals("About")) {
            JOptionPane.showMessageDialog(null, "Authorship Processor\nCoded By: John Sofko\nCOSC-494-61 Computer Stylometry\nDuquesne University\n17 July, 2004", "About", JOptionPane.PLAIN_MESSAGE);
        }
    }

    public static void main(String args[]) {
        Interface I = new Interface();
        I.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        I.setJMenuBar(I.createMenuBar());
        I.setContentPane(I.createContentPane());
        I.setSize(900, 600);
        I.setResizable(false);
        I.setVisible(true);
    }
}
