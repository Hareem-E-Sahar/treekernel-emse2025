package aurora.hwc.gui;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import aurora.*;
import aurora.common.*;
import aurora.hwc.*;
import aurora.hwc.common.*;
import aurora.util.*;

/**
 * Main window of Aurora HWC.
 * @author Alex Kurzhanskiy
 * @version $Id: MainPane.java 143 2010-07-08 06:05:07Z akurzhan $
 */
public final class MainPane extends JFrame implements ActionListener, ItemListener, CommonMain {

    private static final long serialVersionUID = 8711529895478014515L;

    private File currentDir = new File(AuroraConstants.DEFAULT_HOME);

    private String configURI;

    private ContainerHWC mySystem = null;

    private TreePane treePane = null;

    private JLabel statusBar = new JLabel(" ");

    private HashMap<String, JMenuItem> cmd2item = new HashMap<String, JMenuItem>();

    private static final String cmdFileNewSimulation = "FileNewSimulation";

    private static final String cmdFileOpenFile = "FileOpenFile";

    private static final String cmdFileSaveConfiguration = "FileSaveConfiguration";

    private static final String cmdFileSaveSimulation = "FileSaveSimulation";

    private static final String cmdFileExit = "FileExit";

    private static final String cmdEditSettings = "EditSettings";

    private static final String cmdSimulationRun = "SimulationRun";

    private static final String cmdSimulationStop = "SimulationStop";

    private static final String cmdControlMainline = "ControlMainline";

    private static final String cmdControlQueue = "ControlQueue";

    private static final String cmdHelpAbout = "HelpAbout";

    private static final String cmdHelpContactTOPL = "HelpContactTOPL";

    private static final String myTitle = "Aurora RNM Simulator";

    public MainPane() {
        super();
    }

    public MainPane(String title) {
        super(title);
        addWindowListener(new MainPaneWindowAdapter());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(850, 700);
        UtilGUI.center(this);
        setJMenuBar(createMenuBar());
        ImageIcon icon = UtilGUI.createImageIcon("/icons/simlogo2.jpg");
        if (icon != null) setIconImage(icon.getImage());
        setVisible(true);
    }

    /**
	 * Disables all main menu items except 'Open', 'Exit' and 'About',
	 * and clears the main window.
	 */
    private void clearMainPane() {
        JCheckBoxMenuItem mi;
        mi = (JCheckBoxMenuItem) cmd2item.get(cmdControlMainline);
        mi.setSelected(false);
        mi = (JCheckBoxMenuItem) cmd2item.get(cmdControlQueue);
        mi.setSelected(false);
        JMenuBar mainMenu = getJMenuBar();
        for (int i = 0; i < mainMenu.getMenuCount(); i++) {
            JMenu menu = mainMenu.getMenu(i);
            for (int j = 0; j < menu.getItemCount(); j++) if ((menu.getItem(j) != null) && (!menu.getItem(j).getActionCommand().equals(cmdFileOpenFile)) && (!menu.getItem(j).getActionCommand().equals(cmdFileExit)) && (!menu.getItem(j).getActionCommand().equals(cmdHelpAbout)) && (!menu.getItem(j).getActionCommand().equals(cmdHelpContactTOPL))) menu.getItem(j).setEnabled(false);
        }
        getContentPane().removeAll();
        setTitle(myTitle);
        setVisible(false);
        setVisible(true);
        if (treePane != null) treePane.stop();
        treePane = null;
        System.gc();
        return;
    }

    /**
	 * Prepares main menu for simulation.
	 */
    private void prepareForSimulation() {
        JMenuBar mainMenu = getJMenuBar();
        for (int i = 0; i < mainMenu.getMenuCount(); i++) {
            JMenu menu = mainMenu.getMenu(i);
            for (int j = 0; j < menu.getItemCount(); j++) {
                if (menu.getItem(j) == null) continue;
                menu.getItem(j).setEnabled(true);
            }
        }
        cmd2item.get(cmdSimulationStop).setEnabled(false);
        JCheckBoxMenuItem miml = (JCheckBoxMenuItem) cmd2item.get(cmdControlMainline);
        JCheckBoxMenuItem miq = (JCheckBoxMenuItem) cmd2item.get(cmdControlQueue);
        boolean cv = mySystem.getMyNetwork().isControlled();
        boolean qcv = ((NodeHWCNetwork) mySystem.getMyNetwork()).hasQControl();
        miml.setSelected(cv);
        miq.setSelected(qcv);
        miq.setEnabled(cv);
        return;
    }

    /**
	 * Resets simulation data and views.
	 */
    public void resetAll() {
        try {
            mySystem.getMySettings().createNewTmpDataFile(currentDir);
            if (mySystem.initialize()) {
                prepareForSimulation();
                if (treePane != null) treePane.resetView();
                statusBar.setText("Simulation ready. Press 'F5' to run...");
            } else JOptionPane.showMessageDialog(this, "Cannot reset simulation data.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
        }
        return;
    }

    /**
	 * Opens configuration or simulation file.
	 */
    private void openFile() {
        JFileFilter filter = new JFileFilter();
        filter.addType("xml");
        filter.addType("dat");
        filter.setDescription("Aurora files (*.xml, *.dat)");
        JFileChooser fc = new JFileChooser("Open File");
        fc.setFileFilter(filter);
        fc.setCurrentDirectory(currentDir);
        boolean error = false;
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            clearMainPane();
            if (fc.getSelectedFile().getName().endsWith("xml")) {
                configURI = "file:" + fc.getSelectedFile().getAbsolutePath();
                if (mySystem == null) mySystem = new ContainerHWC();
                try {
                    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configURI);
                    mySystem.initFromDOM(doc.getChildNodes().item(0));
                    mySystem.validate();
                } catch (Exception e) {
                    error = true;
                    String buf = e.getMessage();
                    if ((buf == null) || (buf.equals(""))) buf = "Unknown error...";
                    JOptionPane.showMessageDialog(this, buf, e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
                }
                if (!error) {
                    setTitle(myTitle + " - " + fc.getSelectedFile());
                    currentDir = fc.getCurrentDirectory();
                    resetAll();
                }
            }
            if (fc.getSelectedFile().getName().endsWith("dat")) {
                try {
                    ObjectInputStream is = new ObjectInputStream(new FileInputStream(fc.getSelectedFile().getAbsolutePath()));
                    mySystem = (ContainerHWC) is.readObject();
                    is.close();
                    if (mySystem.getMyNetwork() == null) error = true;
                    if (mySystem.getMyEventManager() == null) mySystem.setMyEventManager(new EventManager());
                    if (mySystem.getMySettings() == null) mySystem.setMySettings(new SimulationSettings());
                    if (mySystem.getMyStatus() == null) mySystem.setMyStatus(new SimulationStatus());
                } catch (Exception e) {
                    error = true;
                    JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
                }
                if (!error) {
                    setTitle(myTitle + " - " + fc.getSelectedFile());
                    prepareForSimulation();
                    String status;
                    int ts = mySystem.getMyNetwork().getTS();
                    if (mySystem.canResume(ts)) {
                        cmd2item.get(cmdSimulationRun).setEnabled(true);
                        status = "Simulation paused at step " + Integer.toString(ts) + ", time ";
                        status += Util.time2string(mySystem.getMyNetwork().getTS() * mySystem.getMyNetwork().getTP());
                        status += ". Press 'F5' to resume...";
                    } else {
                        cmd2item.get(cmdSimulationRun).setEnabled(false);
                        status = "Simulation stopped at step " + Integer.toString(ts) + ", time ";
                        status += Util.time2string(mySystem.getMyNetwork().getTS() * mySystem.getMyNetwork().getTP());
                        status += ". Press 'ALT-N' for new simulation...";
                    }
                    statusBar.setText(status);
                }
            }
            if (!error) {
                mySystem.getMySettings().setWindowSize(getSize());
                treePane = new TreePane(mySystem);
                getContentPane().add(treePane, BorderLayout.CENTER);
                getContentPane().add(statusBar, BorderLayout.SOUTH);
                String s = new String(statusBar.getText());
                statusBar.setText(" ");
                statusBar.setText(s);
            }
        }
        setVisible(true);
        currentDir = fc.getCurrentDirectory();
        return;
    }

    /**
	 * Saves configuration.
	 */
    private void saveConfiguration() {
        String ext = "xml";
        JFileFilter filter = new JFileFilter();
        filter.addType(ext);
        filter.setDescription("Aurora configuration files (*." + ext + ")");
        JFileChooser fc = new JFileChooser("Save Configuration");
        fc.setFileFilter(filter);
        fc.setCurrentDirectory(currentDir);
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentDir = fc.getCurrentDirectory();
            File fp = fc.getSelectedFile();
            if (fp.exists()) {
                int res = JOptionPane.showConfirmDialog(this, "File '" + fp.getName() + "' exists. Overwrite?", "Confirmation", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.NO_OPTION) return;
            }
            try {
                String fpath = fp.getAbsolutePath();
                if (!fp.getName().endsWith(ext)) fpath += "." + ext;
                PrintStream oos = new PrintStream(new FileOutputStream(fpath));
                mySystem.xmlDump(oos);
                oos.close();
                mySystem.getMyStatus().setSaved(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
            }
        }
        return;
    }

    /**
	 * Saves simulation.
	 */
    private void saveSimulation() {
        String ext = "dat";
        String dext = "csv";
        JFileFilter filter = new JFileFilter();
        filter.addType(ext);
        filter.setDescription("Aurora simulation files (*." + ext + ")");
        JFileChooser fc = new JFileChooser("Save Simulation");
        fc.setFileFilter(filter);
        fc.setCurrentDirectory(currentDir);
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentDir = fc.getCurrentDirectory();
            File fp = fc.getSelectedFile();
            if (fp.exists()) {
                int res = JOptionPane.showConfirmDialog(this, "File '" + fp.getName() + "' exists. Overwrite?", "Confirmation", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.NO_OPTION) return;
            }
            try {
                String fpath = fp.getAbsolutePath();
                String dfpath = fp.getAbsolutePath();
                if (!fp.getName().endsWith(ext)) {
                    fpath += "." + ext;
                    dfpath += ".";
                } else {
                    dfpath = dfpath.substring(0, dfpath.length() - 3);
                }
                dfpath += dext;
                if ((mySystem != null) && (mySystem.getMySettings() != null)) mySystem.getMySettings().copyTmpDataFile(dfpath);
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fpath));
                mySystem.getMyStatus().setSaved(true);
                oos.writeObject(mySystem);
                oos.close();
                if ((mySystem != null) && (mySystem.getMySettings() != null)) mySystem.getMySettings().establishDataOutput();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
            }
        }
        return;
    }

    /**
	 * Checks if the simulation is saved, if not - asks the user.
	 */
    public boolean checkSaved() {
        if (mySystem == null) return true;
        if (mySystem.getMyStatus().isSaved()) {
            if (mySystem.getMySettings() != null) try {
                mySystem.getMySettings().deleteTmpDataFile();
            } catch (IOException e) {
            }
            return true;
        }
        int v = JOptionPane.showConfirmDialog(this, "Save simulation?", "Confirmation", JOptionPane.YES_NO_CANCEL_OPTION);
        switch(v) {
            case JOptionPane.YES_OPTION:
                saveSimulation();
                return true;
            case JOptionPane.NO_OPTION:
                if (mySystem.getMySettings() != null) try {
                    mySystem.getMySettings().deleteTmpDataFile();
                } catch (IOException e) {
                }
                return true;
            case JOptionPane.CANCEL_OPTION:
            default:
        }
        return false;
    }

    /**
	 * Runs simulation and updates displayed data accordingly.
	 */
    private void runSimulation() {
        boolean res = true;
        mySystem.getMyStatus().setStopped(false);
        mySystem.getMyStatus().setSaved(false);
        int ts = mySystem.getMyNetwork().getTS();
        int tsV = ts;
        int initTS = Math.max(mySystem.getMySettings().getTSInitial(), (int) (mySystem.getMySettings().getTimeInitial() / mySystem.getMyNetwork().getTP()));
        while ((!mySystem.getMyStatus().isStopped()) && res) {
            String status = "Simulation running: step " + Integer.toString(ts) + ", time ";
            status += Util.time2string(mySystem.getMyNetwork().getTS() * mySystem.getMyNetwork().getTP());
            status += ".";
            statusBar.setText(status);
            try {
                res = mySystem.dataUpdate(++ts);
                if ((ts - initTS == 1) || (((ts - tsV) * mySystem.getMyNetwork().getTP()) >= mySystem.getMySettings().getDisplayTP())) {
                    JCheckBoxMenuItem miml = (JCheckBoxMenuItem) cmd2item.get(cmdControlMainline);
                    JCheckBoxMenuItem miq = (JCheckBoxMenuItem) cmd2item.get(cmdControlQueue);
                    boolean cv = mySystem.getMyNetwork().isControlled();
                    boolean qcv = ((NodeHWCNetwork) mySystem.getMyNetwork()).hasQControl();
                    miml.setSelected(cv);
                    miq.setSelected(qcv);
                    miq.setEnabled(cv);
                    tsV = ts;
                    treePane.updateView();
                    Thread.sleep(mySystem.getMySettings().getTimeout());
                }
            } catch (Exception e) {
                mySystem.getMyStatus().setStopped(true);
                JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
            }
        }
        treePane.updateView();
        cmd2item.get(cmdSimulationStop).setEnabled(false);
        String status;
        if ((mySystem.canResume(ts)) && res) {
            cmd2item.get(cmdSimulationRun).setEnabled(true);
            status = "Simulation paused at step " + Integer.toString(ts) + ", time ";
            status += Util.time2string(mySystem.getMyNetwork().getTS() * mySystem.getMyNetwork().getTP());
            status += ". Press 'F5' to resume...";
        } else {
            cmd2item.get(cmdSimulationRun).setEnabled(false);
            status = "Simulation stopped at step " + Integer.toString(ts) + ", time ";
            status += Util.time2string(mySystem.getMyNetwork().getTS() * mySystem.getMyNetwork().getTP());
            status += ". Press 'ALT-N' for new simulation...";
        }
        statusBar.setText(status);
        return;
    }

    /**
	 * Processes menu item actions.
	 * @param e action event.
	 */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if ((mySystem != null) && (!mySystem.getMyStatus().isStopped())) {
            mySystem.getMyStatus().setStopped(true);
            return;
        }
        if (cmdFileNewSimulation.equals(cmd)) {
            if (!checkSaved()) return;
            resetAll();
            return;
        }
        if (cmdFileOpenFile.equals(cmd)) {
            if (!checkSaved()) return;
            openFile();
            return;
        }
        if (cmdFileSaveConfiguration.equals(cmd)) {
            saveConfiguration();
            return;
        }
        if (cmdFileSaveSimulation.equals(cmd)) {
            saveSimulation();
            return;
        }
        if (cmdFileExit.equals(cmd)) {
            closeAndExit();
            return;
        }
        if (cmdEditSettings.equals(cmd)) {
            WindowSettings ws = new WindowSettingsHWC(mySystem, this);
            ws.setVisible(true);
            return;
        }
        if (cmdSimulationRun.equals(cmd)) {
            cmd2item.get(cmdSimulationRun).setEnabled(false);
            cmd2item.get(cmdSimulationStop).setEnabled(true);
            new Thread() {

                public void run() {
                    runSimulation();
                }
            }.start();
            return;
        }
        if (cmdHelpAbout.equals(cmd)) {
            new WindowAbout("Simulator", this);
            return;
        }
        if (cmdHelpContactTOPL.equals(cmd)) {
            Desktop desktop = null;
            String email = AuroraConstants.CONTACT_EMAIL;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.MAIL)) try {
                    desktop.mail(new URI("mailto", email, null));
                    return;
                } catch (Exception exp) {
                }
            }
            JOptionPane.showMessageDialog(this, "Cannot launch email client...\n Please, email your questions to\n" + email, "", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        return;
    }

    /**
	 * Processes menu item checking.
	 * @param e item event.
	 */
    public void itemStateChanged(ItemEvent e) {
        JCheckBoxMenuItem miml = (JCheckBoxMenuItem) cmd2item.get(cmdControlMainline);
        JCheckBoxMenuItem miq = (JCheckBoxMenuItem) cmd2item.get(cmdControlQueue);
        boolean cv = miml.isSelected();
        boolean qcv = miq.isSelected();
        miq.setEnabled(cv);
        ((NodeHWCNetwork) mySystem.getMyNetwork()).setControlled(cv, qcv);
        if (treePane != null) treePane.updateConfig();
        return;
    }

    /**
	 * Creates menu bar.
	 * @return menu bar.
	 */
    private JMenuBar createMenuBar() {
        JMenuItem item;
        JMenu menu;
        JMenuBar mainMenu = new JMenuBar();
        menu = new JMenu("File");
        item = new JMenuItem("New Simulation");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
        item.addActionListener(this);
        item.setEnabled(false);
        item.setActionCommand(cmdFileNewSimulation);
        cmd2item.put(cmdFileNewSimulation, item);
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem("Open");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        item.addActionListener(this);
        item.setActionCommand(cmdFileOpenFile);
        cmd2item.put(cmdFileOpenFile, item);
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem("Save Configuration");
        item.addActionListener(this);
        item.setEnabled(false);
        item.setActionCommand(cmdFileSaveConfiguration);
        cmd2item.put(cmdFileSaveConfiguration, item);
        menu.add(item);
        item = new JMenuItem("Save Simulation");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        item.addActionListener(this);
        item.setEnabled(false);
        item.setActionCommand(cmdFileSaveSimulation);
        cmd2item.put(cmdFileSaveSimulation, item);
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem("Exit");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
        item.addActionListener(this);
        item.setActionCommand(cmdFileExit);
        cmd2item.put(cmdFileExit, item);
        menu.add(item);
        mainMenu.add(menu);
        menu = new JMenu("Edit");
        item = new JMenuItem("Settings");
        item.addActionListener(this);
        item.setEnabled(false);
        item.setActionCommand(cmdEditSettings);
        cmd2item.put(cmdEditSettings, item);
        menu.add(item);
        mainMenu.add(menu);
        menu = new JMenu("Simulation");
        item = new JMenuItem("Start");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        item.addActionListener(this);
        item.setEnabled(false);
        item.setActionCommand(cmdSimulationRun);
        cmd2item.put(cmdSimulationRun, item);
        menu.add(item);
        item = new JMenuItem("Stop");
        item.addActionListener(this);
        item.setEnabled(false);
        item.setActionCommand(cmdSimulationStop);
        cmd2item.put(cmdSimulationStop, item);
        menu.add(item);
        mainMenu.add(menu);
        menu = new JMenu("Control");
        item = new JCheckBoxMenuItem("Mainline");
        item.setEnabled(false);
        item.addItemListener(this);
        cmd2item.put(cmdControlMainline, item);
        menu.add(item);
        item = new JCheckBoxMenuItem("Queue");
        item.setEnabled(false);
        item.addItemListener(this);
        cmd2item.put(cmdControlQueue, item);
        menu.add(item);
        mainMenu.add(menu);
        menu = new JMenu("Help");
        item = new JMenuItem("About");
        item.addActionListener(this);
        item.setActionCommand(cmdHelpAbout);
        cmd2item.put(cmdHelpAbout, item);
        menu.add(item);
        item = new JMenuItem("Contact TOPL");
        item.addActionListener(this);
        item.setActionCommand(cmdHelpContactTOPL);
        cmd2item.put(cmdHelpContactTOPL, item);
        menu.add(item);
        mainMenu.add(menu);
        return mainMenu;
    }

    private void closeAndExit() {
        if (mySystem != null) mySystem.getMyStatus().setStopped(true);
        if (checkSaved()) {
            statusBar.setText("Exiting...");
            setVisible(false);
            dispose();
            System.exit(0);
        }
    }

    public void LogErrors(Vector<ErrorConfiguration> e) {
        String str = null;
        for (int i = 0; i < e.size(); i++) {
            str = e.get(i).getMessage();
            if (false) System.err.println(str);
        }
        return;
    }

    /**
	 * Run Simulator in batch mode.
	 * @param infile configuration file path.
	 * @param outfile output file path.
	 */
    private static void runBatch(String infile, String outfile) {
        File config = new File(infile);
        ContainerHWC mySystem = new ContainerHWC();
        mySystem.batchMode();
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("file:" + config.getAbsolutePath());
            mySystem.initFromDOM(doc.getChildNodes().item(0));
            mySystem.validate();
        } catch (Exception e) {
            String buf = e.getMessage();
            if ((buf == null) || (buf.equals(""))) buf = "Failed to read configuration file!";
            System.err.println(buf + "\nExiting...");
            return;
        }
        File data = new File(outfile);
        try {
            if ((!mySystem.getMySettings().setTmpDataFile(data)) || (!mySystem.getMySettings().createDataHeader())) {
                System.err.println("Failed to open data output file!\nExiting...");
                return;
            }
        } catch (Exception e) {
            String buf = e.getMessage();
            if ((buf == null) || (buf.equals(""))) buf = "Failed to open data output file!";
            System.err.println(buf + "\nExiting...");
            return;
        }
        try {
            mySystem.initialize();
        } catch (Exception e) {
            String buf = e.getMessage();
            if ((buf == null) || (buf.equals(""))) buf = "Initialization failed!";
            System.err.println(buf + "\nExiting...");
            mySystem.getMySettings().getTmpDataOutput().close();
            return;
        }
        boolean res = true;
        mySystem.getMyStatus().setStopped(false);
        mySystem.getMyStatus().setSaved(false);
        int ts = mySystem.getMyNetwork().getTS();
        while ((!mySystem.getMyStatus().isStopped()) && res) {
            try {
                res = mySystem.dataUpdate(++ts);
            } catch (Exception e) {
                String buf = e.getMessage();
                if ((buf == null) || (buf.equals(""))) buf = "Simulation failed on time step " + ts + "!";
                System.err.println(buf + "\nExiting...");
                mySystem.getMySettings().getTmpDataOutput().close();
                return;
            }
        }
        if (!res) System.err.println("Simulation failed on time step " + ts + "!\nExiting...");
        mySystem.getMySettings().getTmpDataOutput().close();
        System.out.println("Done!");
        return;
    }

    /**
     * Create the GUI and show it.
     * For thread safety, this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        new MainPane(myTitle);
    }

    public static void main(String[] args) {
        if (args.length == 2) {
            runBatch(args[0], args[1]);
        } else {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    createAndShowGUI();
                }
            });
        }
    }

    /**
     * This class is needed for proper exiting.
     */
    private class MainPaneWindowAdapter extends WindowAdapter {

        /**
    	 * Function that is called when user closes the window.
    	 * @param e window event.
    	 */
        public void windowClosing(WindowEvent e) {
            closeAndExit();
            return;
        }
    }
}
