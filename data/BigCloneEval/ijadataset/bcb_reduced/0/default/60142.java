import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import utils.MiscUtils;
import utils.AppLogger;
import utils.SnapConfigParser;
import utils.SnapConfigChangeListener;
import widget.HelpBrowser;
import widget.WidgetUtils;
import widget.AboutDialog;
import widget.OptionsDialog;

class SnapGUI extends JFrame implements ActionListener, SnapConfigChangeListener {

    boolean doContinue = true;

    static SnapGUI snapGui;

    MiscUtils util;

    SnapConfigParser oParser;

    HelpBrowser helpPane;

    JPanel toolsPane;

    JPanel viewPane;

    JPanel statusBar;

    JLabel time;

    JLabel capSaveDir;

    Container base;

    JTabbedPane tabs;

    JRadioButton singlePic;

    JRadioButton multiplePic;

    JLabel captureAfter_1, captureAfter_2;

    JLabel secsAs_1, secsAs_2;

    JComboBox fileType_1, fileType_2;

    JButton click_1, click_2;

    JButton refresh_1;

    JTextField secs_1, secs_2;

    JLabel capture;

    JPanel canvas;

    JTextArea status;

    JRadioButton crazy, sexy, cool;

    CompoundBorder customBorder;

    String filePath;

    String fileType;

    boolean SCREENSHOT_TAKEN;

    AboutDialog oAboutDialog;

    OptionsDialog oOptionsDialog;

    WidgetUtils oWidgets;

    AppLogger oLogger;

    public SnapGUI() {
        super("GNUSnap");
        oLogger = AppLogger.getInstance();
        oParser = SnapConfigParser.getInstance();
        oWidgets = new WidgetUtils();
        init();
        buildMenuPane();
        buildToolsPane();
        buildViewPane();
        base.add(viewPane, BorderLayout.CENTER);
        base.add(toolsPane, BorderLayout.EAST);
        statusBar.add(time, BorderLayout.WEST);
        statusBar.add(capSaveDir, BorderLayout.EAST);
        base.add(statusBar, BorderLayout.SOUTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(750, 580);
        Dimension oDimScreenLoc = WidgetUtils.getCenterScreenDimension(this.getSize());
        setLocation(oDimScreenLoc.width, oDimScreenLoc.height);
        disableMultiSnapshotComponents(true);
        disableMultiSnapshotStartButton(true);
        refresh_1.setEnabled(false);
        setVisible(true);
        oAboutDialog = new AboutDialog();
        oOptionsDialog = new OptionsDialog();
        oOptionsDialog.setSnapConfigChangeListener(this);
        oLogger.writeVersion();
    }

    private void setFilename(int isSingleSnap) {
        filePath = util.getCaptureFileName();
        if (isSingleSnap == 1) fileType = (String) fileType_1.getSelectedItem(); else fileType = (String) fileType_2.getSelectedItem();
        filePath = filePath + "." + fileType;
    }

    public void capture(int isSingleSnap) {
        setFilename(isSingleSnap);
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();
            Rectangle screenRect = new Rectangle(screenSize);
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(screenRect);
            ImageIO.write(image, fileType, new File(filePath));
            oLogger.log("Screen Shot Captured and written to :" + filePath);
            SCREENSHOT_TAKEN = true;
        } catch (Exception ex) {
            System.out.println("capture : " + ex);
            oLogger.log("Failed to write Screen Shot to :" + filePath + " because : \n" + ex);
            SCREENSHOT_TAKEN = false;
        }
    }

    public void refreshCanvas() {
        canvas.removeAll();
        capture.setIcon(null);
        capture.setText("        Take A Snapshot & See its preview here.");
        canvas.add(new JScrollPane(capture), BorderLayout.CENTER);
        canvas.repaint();
        canvas.validate();
    }

    public void paintCapture() {
        canvas.removeAll();
        capture.setText("");
        capture.setIcon(new ImageIcon(filePath));
        status.setText("Screen Captured");
        capture.repaint();
        canvas.add(new JScrollPane(capture), BorderLayout.CENTER);
        canvas.repaint();
        canvas.validate();
        if (tabs.getSelectedIndex() != 0) ;
        tabs.setSelectedIndex(0);
    }

    public void disableSingleSnapshotComponents(boolean status) {
        if (status) status = false; else status = true;
        captureAfter_1.setEnabled(status);
        secsAs_1.setEnabled(status);
        fileType_1.setEnabled(status);
        click_1.setEnabled(status);
        refresh_1.setEnabled(status);
        secs_1.setEnabled(status);
    }

    public void disableMultiSnapshotComponents(boolean status) {
        if (status) status = false; else status = true;
        captureAfter_2.setEnabled(status);
        secsAs_2.setEnabled(status);
        fileType_2.setEnabled(status);
        secs_2.setEnabled(status);
    }

    public void disableMultiSnapshotStartButton(boolean status) {
        if (status) status = false; else status = true;
        click_2.setEnabled(status);
    }

    private void init() {
        customBorder = new CompoundBorder(new EmptyBorder(5, 5, 5, 5), new BevelBorder(BevelBorder.LOWERED));
        util = new MiscUtils();
        helpPane = new HelpBrowser("HelpFiles" + util.getFileSeparator() + "Helpindex.html");
        toolsPane = new JPanel();
        toolsPane.setLayout(new GridLayout(3, 1));
        viewPane = new JPanel();
        viewPane.setLayout(new BorderLayout());
        statusBar = new JPanel();
        statusBar.setLayout(new BorderLayout());
        statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
        base = this.getContentPane();
        base.setLayout(new BorderLayout());
        tabs = new JTabbedPane(JTabbedPane.BOTTOM);
        time = new JLabel("Today is :  " + util.getDayOfMonth() + " / " + util.getMonth() + " / " + util.getYear());
        capSaveDir = new JLabel("Capture Save Path : " + oParser.getSavePath());
    }

    public JButton createButton(String caption, String cmd) {
        JButton but = new JButton(caption);
        but.addActionListener(this);
        if (cmd.equals("")) but.setActionCommand(caption); else but.setActionCommand(cmd);
        return but;
    }

    private void buildMenuPane() {
        JMenuBar mnuBar = new JMenuBar();
        JMenu mnuAction = new JMenu("Action");
        KeyStroke oKeyS = KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK);
        JMenuItem mnuOptions = oWidgets.getMenuItem("Options", this, oKeyS);
        oKeyS = KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK);
        JMenuItem mnuCaptureNow = oWidgets.getMenuItem("Capture Now", this, oKeyS);
        JMenuItem mnuExit = new JMenuItem("Exit");
        mnuExit.addActionListener(this);
        mnuAction.add(mnuCaptureNow);
        mnuAction.addSeparator();
        mnuAction.add(mnuOptions);
        mnuAction.addSeparator();
        mnuAction.add(mnuExit);
        JMenu mnuHelp = new JMenu("Help");
        JMenuItem mnuHelpContents = new JMenuItem("Contents");
        JMenuItem mnuAbout = new JMenuItem("About");
        mnuAbout.addActionListener(this);
        mnuHelpContents.setActionCommand("Help");
        mnuHelpContents.addActionListener(this);
        mnuHelp.add(mnuHelpContents);
        mnuHelp.addSeparator();
        mnuHelp.add(mnuAbout);
        mnuBar.add(mnuAction);
        mnuBar.add(mnuHelp);
        this.setJMenuBar(mnuBar);
    }

    private void buildToolsPane() {
        Vector supportedFileTypes;
        secs_1 = new JTextField("05");
        secs_2 = new JTextField("05");
        click_1 = createButton("Click", "Click_1");
        click_1.setMnemonic('C');
        refresh_1 = createButton("Refresh", "Refresh_1");
        refresh_1.setMnemonic('e');
        click_2 = createButton("Start", "Click_2");
        click_2.setMnemonic('S');
        supportedFileTypes = new Vector();
        supportedFileTypes.addElement("jpg");
        supportedFileTypes.addElement("png");
        captureAfter_1 = new JLabel("Capture After ");
        captureAfter_2 = new JLabel("Capture After ");
        secsAs_1 = new JLabel(" Secs As ");
        secsAs_2 = new JLabel(" Secs As ");
        fileType_1 = new JComboBox(supportedFileTypes);
        fileType_2 = new JComboBox(supportedFileTypes);
        JPanel singlePicOptions = new JPanel(new BorderLayout());
        singlePic = new JRadioButton("One Screenshoot", true);
        singlePic.setMnemonic('n');
        multiplePic = new JRadioButton("Multiple Screenshoots");
        multiplePic.setMnemonic('u');
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(singlePic);
        buttonGroup.add(multiplePic);
        singlePic.addActionListener(this);
        multiplePic.addActionListener(this);
        JPanel tools = new JPanel(new BorderLayout());
        singlePicOptions.add(singlePic, BorderLayout.NORTH);
        JPanel singlePic1 = new JPanel();
        singlePic1.add(captureAfter_1);
        singlePic1.add(secs_1);
        singlePic1.add(secsAs_1);
        singlePic1.add(fileType_1);
        JPanel singlePic2 = new JPanel();
        singlePic2.setLayout(new GridLayout(1, 3));
        singlePic2.add(refresh_1);
        singlePic2.add(click_1);
        singlePicOptions.add(singlePic1, BorderLayout.CENTER);
        singlePicOptions.add(singlePic2, BorderLayout.SOUTH);
        CompoundBorder inborder = new CompoundBorder(new EmptyBorder(10, 10, 10, 10), new LineBorder(Color.black, 1, true));
        CompoundBorder border = new CompoundBorder(inborder, new EmptyBorder(10, 10, 10, 10));
        singlePicOptions.setBorder(border);
        JPanel multiPicOptions = new JPanel(new BorderLayout());
        multiPicOptions.add(multiplePic, BorderLayout.NORTH);
        JPanel multiPic1 = new JPanel();
        multiPic1.add(captureAfter_2);
        multiPic1.add(secs_2);
        multiPic1.add(secsAs_2);
        multiPic1.add(fileType_2);
        JPanel multiPic2 = new JPanel();
        multiPic2.setLayout(new GridLayout(1, 3));
        multiPic2.add(new JLabel(""));
        multiPic2.add(click_2);
        multiPicOptions.add(multiPic1, BorderLayout.CENTER);
        multiPicOptions.add(multiPic2, BorderLayout.SOUTH);
        multiPicOptions.setBorder(border);
        crazy = new JRadioButton("Crazy");
        sexy = new JRadioButton("Sexy");
        cool = new JRadioButton("Cool", true);
        crazy.addActionListener(this);
        sexy.addActionListener(this);
        cool.addActionListener(this);
        crazy.setMnemonic('r');
        sexy.setMnemonic('e');
        cool.setMnemonic('o');
        ButtonGroup buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(crazy);
        buttonGroup1.add(sexy);
        buttonGroup1.add(cool);
        JPanel miscTools = new JPanel(new GridLayout(1, 3));
        JButton about = createButton("About", "");
        about.setMnemonic('A');
        JButton help = createButton("Help", "");
        help.setMnemonic('H');
        JButton exit = createButton("Exit", "");
        exit.setMnemonic('x');
        miscTools.add(about);
        miscTools.add(help);
        miscTools.add(exit);
        JPanel look = new JPanel();
        look.add(crazy);
        look.add(sexy);
        look.add(cool);
        JPanel opts1 = new JPanel(new BorderLayout());
        opts1.add(miscTools, BorderLayout.CENTER);
        opts1.add(look, BorderLayout.SOUTH);
        opts1.setBorder(border);
        JPanel statusPane = new JPanel();
        status = new JTextArea("Status");
        status.setEditable(false);
        status.setForeground(Color.blue);
        statusPane.add(status);
        statusPane.setBorder(border);
        JPanel opts = new JPanel(new BorderLayout());
        opts.add(opts1, BorderLayout.NORTH);
        opts.add(statusPane, BorderLayout.SOUTH);
        toolsPane.add(singlePicOptions);
        toolsPane.add(multiPicOptions);
        toolsPane.add(opts);
    }

    private void buildViewPane() {
        capture = new JLabel("        Take A Snapshot & See its preview here.");
        canvas = new JPanel();
        canvas.setLayout(new BorderLayout());
        canvas.add(new JScrollPane(capture), BorderLayout.CENTER);
        canvas.setBorder(customBorder);
        tabs.add(canvas, "Latest Screen Capture");
        tabs.add(helpPane, "Help");
        viewPane.add(tabs);
    }

    private JPanel getAboutPanel() {
        JPanel base = new JPanel();
        JLabel msg = new JLabel("Snap.");
        base.add(msg);
        return base;
    }

    private void setLookAndFeel(String lnf) {
        try {
            UIManager.setLookAndFeel(lnf);
            SwingUtilities.updateComponentTreeUI(this);
            SwingUtilities.updateComponentTreeUI(oAboutDialog);
            SwingUtilities.updateComponentTreeUI(oOptionsDialog);
        } catch (Exception ev) {
            System.err.println(" Failed to set : " + lnf + " - " + ev);
            ev.printStackTrace();
        }
    }

    public void snapConfigChanged(String _oStrCapSaveDir) {
        capSaveDir.setText("Capture Save Path : " + _oStrCapSaveDir);
    }

    public void actionPerformed(ActionEvent ae) {
        String cmd = ae.getActionCommand();
        if (cmd.equals("Click_1")) {
            try {
                int sec = Integer.parseInt(secs_1.getText());
                CaptureMaker x = new CaptureMaker(sec, 1);
            } catch (NumberFormatException ex) {
                Toolkit.getDefaultToolkit().beep();
            }
        } else if (cmd.equals("Refresh_1")) {
            refreshCanvas();
        } else if (cmd.equals("Click_2")) {
            try {
                int sec = Integer.parseInt(secs_2.getText());
                doContinue = true;
                CaptureMaker x = new CaptureMaker(sec, 2);
                click_2.setText("Stop");
                click_2.setActionCommand("Stop");
            } catch (NumberFormatException ex) {
                Toolkit.getDefaultToolkit().beep();
            }
        } else if (cmd.equals("Stop")) {
            doContinue = false;
            click_2.setText("Start");
            click_2.setActionCommand("Click_2");
            disableMultiSnapshotComponents(false);
        } else if (cmd.equals("One Screenshoot")) {
            disableSingleSnapshotComponents(false);
            disableMultiSnapshotComponents(true);
            disableMultiSnapshotStartButton(true);
        } else if (cmd.equals("Multiple Screenshoots")) {
            disableSingleSnapshotComponents(true);
            disableMultiSnapshotComponents(false);
            disableMultiSnapshotStartButton(false);
        } else if (cmd.equals("About")) {
            oAboutDialog.showAboutDialog();
        } else if (cmd.equals("Help")) tabs.setSelectedIndex(1); else if (cmd.equals("Exit")) {
            int iValRes = JOptionPane.showConfirmDialog(this, "Are you sure you want to close Snap ?", "Exit Snap ?", JOptionPane.YES_NO_OPTION);
            if (iValRes == JOptionPane.YES_OPTION) {
                oLogger.log("Snap Exiting");
                System.exit(0);
            }
        } else if (cmd.equals("Crazy")) {
            if ((new MiscUtils()).getOSName().equals("Linux")) {
                setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            } else {
                setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            }
        } else if (cmd.equals("Sexy")) setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel"); else if (cmd.equals("Cool")) setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel"); else if (cmd.equals("Capture Now")) {
            CaptureMaker x = new CaptureMaker(0, 1);
        } else if (cmd.equals("Options")) {
            oOptionsDialog.showOptionsDialog();
        }
    }

    public static void main(String[] args) {
        snapGui = new SnapGUI();
    }

    public class CaptureMaker extends Thread {

        int secsToWait;

        int isSingleSnap;

        public CaptureMaker(int secsToWait, int isSingleSnap) {
            this.secsToWait = secsToWait;
            this.start();
            this.isSingleSnap = isSingleSnap;
        }

        public void pause_N_Click() {
            int secCount = secsToWait;
            while (secCount-- != 0) {
                try {
                    status.setText("Status : Secs before capture " + secCount);
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
            capture(isSingleSnap);
        }

        public void run() {
            multiplePic.setEnabled(false);
            singlePic.setEnabled(false);
            if (isSingleSnap == 1) {
                disableSingleSnapshotComponents(true);
                disableMultiSnapshotStartButton(true);
            } else {
                disableMultiSnapshotComponents(true);
            }
            if (isSingleSnap == 1) pause_N_Click(); else {
                while (doContinue) pause_N_Click();
            }
            paintCapture();
            if (isSingleSnap == 1) {
                disableSingleSnapshotComponents(false);
                disableMultiSnapshotStartButton(true);
            } else {
                disableMultiSnapshotComponents(false);
                disableMultiSnapshotStartButton(false);
            }
            multiplePic.setEnabled(true);
            singlePic.setEnabled(true);
        }
    }
}
