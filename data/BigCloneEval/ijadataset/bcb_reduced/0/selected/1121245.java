package aurora.hwc.config;

import java.io.*;
import java.net.*;
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
 * Main window of Aurora Configuration Utility.
 * @author Alex Kurzhanskiy
 * @version $Id: MainPane.java 135 2010-06-05 00:57:31Z akurzhan $
 */
public final class MainPane extends JFrame implements ActionListener, ItemListener {

    private static final long serialVersionUID = 3515636962372050014L;

    private File currentDir = new File("c:\\tmp");

    private String configURI;

    private ContainerHWC mySystem = null;

    private TreePane treePane = null;

    private JLabel statusBar = new JLabel(" ");

    private HashMap<String, JMenuItem> cmd2item = new HashMap<String, JMenuItem>();

    private static final String cmdFileNewConfiguration = "FileNewConfiguration";

    private static final String cmdFileOpenFile = "FileOpenFile";

    private static final String cmdFileSaveConfiguration = "FileSaveConfiguration";

    private static final String cmdFileExit = "FileExit";

    private static final String cmdEditSettings = "EditSettings";

    private static final String cmdViewFilter = "ViewFilter";

    private static final String cmdToolsConfigurationSummary = "ToolsConfigurationSummary";

    private static final String cmdToolsValidate = "ToolsValidate";

    private static final String cmdHelpAbout = "HelpAbout";

    private static final String cmdHelpContactTOPL = "HelpContactTOPL";

    private static final String myTitle = "Aurora RNM Configurator";

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
        ImageIcon icon = UtilGUI.createImageIcon("/icons/configlogo2.jpg");
        if (icon != null) setIconImage(icon.getImage());
        setVisible(true);
    }

    /**
	 * Returns current file directory.
	 */
    public File getCurrentDir() {
        return currentDir;
    }

    /**
	 * Sets current file directory.
	 * @param dir file directory.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
    public synchronized boolean setCurrentDir(File dir) {
        currentDir = dir;
        return true;
    }

    /**
	 * Disables all main menu items except 'Open', 'Exit' and 'About',
	 * and clears the main window.
	 */
    private void clearMainPane() {
        JMenuBar mainMenu = getJMenuBar();
        for (int i = 0; i < mainMenu.getMenuCount(); i++) {
            JMenu menu = mainMenu.getMenu(i);
            for (int j = 0; j < menu.getItemCount(); j++) if ((menu.getItem(j) != null) && (!menu.getItem(j).getActionCommand().equals(cmdFileNewConfiguration)) && (!menu.getItem(j).getActionCommand().equals(cmdFileOpenFile)) && (!menu.getItem(j).getActionCommand().equals(cmdFileExit)) && (!menu.getItem(j).getActionCommand().equals(cmdHelpAbout)) && (!menu.getItem(j).getActionCommand().equals(cmdHelpContactTOPL))) menu.getItem(j).setEnabled(false);
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
	 * Enables menu items for configuration editing.
	 */
    void enableMenu1() {
        JMenuBar mainMenu = getJMenuBar();
        for (int i = 0; i < mainMenu.getMenuCount(); i++) {
            JMenu menu = mainMenu.getMenu(i);
            for (int j = 0; j < menu.getItemCount(); j++) if (menu.getItem(j) != null) menu.getItem(j).setEnabled(true);
        }
        return;
    }

    /**
	 * Resets configuration data and views.
	 */
    public void resetAll() {
        try {
            if (mySystem.initialize()) {
                if (treePane != null) treePane.resetView();
                statusBar.setText("");
            } else JOptionPane.showMessageDialog(this, "Cannot reset configuration data.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
        }
        return;
    }

    /**
	 * Opens configuration or GIS file.
	 */
    private void openFile() {
        JFileFilter filter = new JFileFilter();
        filter.addType("xml");
        filter.setDescription("Aurora files (*.xml)");
        JFileChooser fc = new JFileChooser("Open File");
        fc.setFileFilter(filter);
        fc.setCurrentDirectory(currentDir);
        boolean error = false;
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            clearMainPane();
            if (fc.getSelectedFile().getName().endsWith("xml")) {
                configURI = "file:" + fc.getSelectedFile().getAbsolutePath();
                if (mySystem == null) mySystem = new ContainerHWC();
                mySystem.applicationConfiguration();
                try {
                    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configURI);
                    mySystem.initFromDOM(doc.getChildNodes().item(0));
                } catch (Exception e) {
                    error = true;
                    String buf = e.getMessage();
                    if ((buf == null) || (buf.equals(""))) buf = "Unknown error...";
                    JOptionPane.showMessageDialog(this, buf, e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
                }
                if (!error) {
                    enableMenu1();
                    resetAll();
                }
            }
            if (!error) {
                setTitle(myTitle + " - " + fc.getSelectedFile());
                mySystem.getMySettings().setWindowSize(getSize());
                treePane = new TreePane(mySystem, this);
                getContentPane().add(treePane, BorderLayout.CENTER);
                getContentPane().add(statusBar, BorderLayout.SOUTH);
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
                setTitle(myTitle + " - " + fc.getSelectedFile());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
            }
        }
        return;
    }

    /**
	 * Generate new configuration.
	 */
    private void newConfiguration() {
        clearMainPane();
        if (mySystem == null) mySystem = new ContainerHWC();
        mySystem.applicationConfiguration();
        NodeHWCNetwork ntwk = new NodeHWCNetwork(-1, true);
        ntwk.setContainer(mySystem);
        ntwk.setName("My Network");
        ntwk.setDescription("Created by Aurora Configurator");
        ntwk.setMyNetwork(ntwk);
        ntwk.setPosition(new PositionNode());
        mySystem.setMyNetwork(ntwk);
        mySystem.setMyEventManager(new EventManager());
        mySystem.setMySettings(new SimulationSettingsHWC());
        mySystem.setMyStatus(new SimulationStatus());
        mySystem.getMyStatus().setSaved(false);
        mySystem.getMyStatus().setStopped(true);
        enableMenu1();
        resetAll();
        mySystem.getMySettings().setWindowSize(getSize());
        treePane = new TreePane(mySystem, this);
        getContentPane().add(treePane, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH);
        setVisible(true);
        return;
    }

    /**
	 * Checks if the configuration is saved, if not - asks the user.
	 */
    public boolean checkSaved() {
        if ((mySystem == null) || (mySystem.getMyStatus().isSaved())) return true;
        int v = JOptionPane.showConfirmDialog(this, "Save configuration?", "Confirmation", JOptionPane.YES_NO_CANCEL_OPTION);
        switch(v) {
            case JOptionPane.YES_OPTION:
                saveConfiguration();
                return true;
            case JOptionPane.NO_OPTION:
                return true;
            case JOptionPane.CANCEL_OPTION:
            default:
        }
        return false;
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
        if (cmdFileNewConfiguration.equals(cmd)) {
            if (!checkSaved()) return;
            newConfiguration();
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
        if (cmdFileExit.equals(cmd)) {
            closeAndExit();
            return;
        }
        if (cmdEditSettings.equals(cmd)) {
            WindowSettings ws = new WindowSettingsHWC(mySystem, null);
            ws.setVisible(true);
            return;
        }
        if (cmdViewFilter.equals(cmd)) {
            WindowFilter wf = new WindowFilter(treePane, null);
            wf.setVisible(true);
            return;
        }
        if (cmdToolsConfigurationSummary.equals(cmd)) {
            ConfigurationSummaryHWC cs = new ConfigurationSummaryHWC();
            if ((mySystem != null) && (mySystem.getMyNetwork() != null)) {
                mySystem.getMyNetwork().updateConfigurationSummary(cs);
                new WindowConfigurationSummary(cs, this);
            }
            return;
        }
        if (cmdToolsValidate.equals(cmd)) {
            if ((mySystem != null) && (mySystem.getMyNetwork() != null)) {
                try {
                    mySystem.getMyNetwork().validate();
                } catch (Exception exp) {
                    String buf = exp.getMessage();
                    if ((buf == null) || (buf.equals(""))) buf = "Unknown error...";
                    JOptionPane.showMessageDialog(this, buf, exp.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
                }
                treePane.getActionPane().updateErrorTable();
            }
            return;
        }
        if (cmdHelpAbout.equals(cmd)) {
            new WindowAbout("Configurator", this);
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
	 * Creates menu bar.
	 * @return menu bar.
	 */
    private JMenuBar createMenuBar() {
        JMenuItem item;
        JMenu menu;
        JMenuBar mainMenu = new JMenuBar();
        menu = new JMenu("File");
        item = new JMenuItem("New");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
        item.addActionListener(this);
        item.setEnabled(true);
        item.setActionCommand(cmdFileNewConfiguration);
        cmd2item.put(cmdFileNewConfiguration, item);
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem("Open");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        item.addActionListener(this);
        item.setActionCommand(cmdFileOpenFile);
        cmd2item.put(cmdFileOpenFile, item);
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem("Save");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        item.addActionListener(this);
        item.setEnabled(false);
        item.setActionCommand(cmdFileSaveConfiguration);
        cmd2item.put(cmdFileSaveConfiguration, item);
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
        menu = new JMenu("View");
        item = new JMenuItem("Filter");
        item.addActionListener(this);
        item.setEnabled(false);
        item.setActionCommand(cmdViewFilter);
        cmd2item.put(cmdViewFilter, item);
        menu.add(item);
        mainMenu.add(menu);
        menu = new JMenu("Tools");
        item = new JMenuItem("Configuration Summary");
        item.addActionListener(this);
        item.setEnabled(false);
        item.setActionCommand(cmdToolsConfigurationSummary);
        cmd2item.put(cmdToolsConfigurationSummary, item);
        menu.add(item);
        item = new JMenuItem("Validate");
        item.addActionListener(this);
        item.setEnabled(false);
        item.setActionCommand(cmdToolsValidate);
        cmd2item.put(cmdToolsValidate, item);
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

    public void itemStateChanged(ItemEvent e) {
        ;
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
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI();
            }
        });
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
