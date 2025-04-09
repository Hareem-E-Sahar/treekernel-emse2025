import gui.*;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.LinkedList;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.text.DefaultEditorKit;
import java.text.NumberFormat;
import core.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import java.io.IOException;

/**
 *
 */
class UMLOptions {
}

public class Titrator extends JFrame {

    protected ResourceBundle resbundle;

    protected AboutBox aboutBox;

    protected PrefPane prefs;

    protected TrialFrame trialFrame;

    protected Action newAction, openAction, closeAction, saveAction, saveAsAction, cutAction, copyAction, pasteAction, clearAction, preferencesAction, quitAction, aboutAction, modelAction;

    static final JMenuBar mainMenuBar = new JMenuBar();

    protected JMenu fileMenu, editMenu;

    protected Color background;

    private ComponentTableModel componentTableModel;

    private EquationTableModel equationTableModel;

    private GridBagConstraints gbc;

    private Insets inset;

    private JButton btnTitrate;

    private JLabel lblIonic;

    private JLabel lblTemp;

    private JPanel mainPanel;

    private JPanel systemPanel;

    private JScrollPane componentSP;

    private JScrollPane equationSP;

    private JScrollPane speciesSP;

    private JTable componentTable;

    private JTable equationTable;

    private JTable speciesTable;

    private JFormattedTextField ionicStrength;

    private JFormattedTextField temperature;

    private SpeciesInputTableModel speciesTableModel;

    private TitledBorder componentBorder;

    private TitledBorder equationBorder;

    private TitledBorder speciesBorder;

    private TitledBorder systemBorder;

    private JFileChooser fc;

    private TitratorXMLParser fileParser;

    private String systemName;

    private SystemController controller;

    public static boolean MAC_OS_X = System.getProperty("os.name").startsWith("Mac OS X");

    public Titrator() {
        super();
        resbundle = ResourceBundle.getBundle("strings", Locale.getDefault());
        setTitle(resbundle.getString("defaultProjectTitle"));
        systemName = resbundle.getString("defaultProjectTitle");
        background = Color.LIGHT_GRAY;
        controller = new SystemController(systemName);
        createActions();
        addMenus();
        setPreferredSize(new Dimension(800, 400));
        setLocation(new Point(0, 50));
        TableColumn column;
        inset = new Insets(0, 0, 0, 0);
        gbc = new GridBagConstraints();
        gbc.insets = inset;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(background);
        systemBorder = BorderFactory.createTitledBorder(resbundle.getString("systemBorder"));
        systemPanel = new JPanel(new GridBagLayout());
        systemPanel.setBorder(systemBorder);
        systemPanel.setBackground(background);
        lblIonic = new JLabel(resbundle.getString("ionicStrength"));
        ionicStrength = new JFormattedTextField(NumberFormat.getNumberInstance());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        systemPanel.add(lblIonic, gbc);
        gbc.gridx = 1;
        systemPanel.add(ionicStrength, gbc);
        lblTemp = new JLabel(resbundle.getString("temperature"));
        temperature = new JFormattedTextField(NumberFormat.getNumberInstance());
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        systemPanel.add(lblTemp, gbc);
        gbc.gridx = 1;
        systemPanel.add(temperature, gbc);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.01;
        gbc.weighty = 0.5;
        mainPanel.add(systemPanel, gbc);
        equationBorder = BorderFactory.createTitledBorder(resbundle.getString("equationBorder"));
        equationTableModel = new EquationTableModel(controller, true);
        equationTable = new JTable(equationTableModel);
        equationTable.setShowGrid(true);
        equationTable.setCellSelectionEnabled(true);
        equationTable.setDefaultRenderer(Double.class, new ScientificRenderer());
        column = equationTable.getColumnModel().getColumn(0);
        column.setPreferredWidth(450);
        equationSP = new JScrollPane(equationTable);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 0.9;
        equationSP.setBorder(equationBorder);
        equationSP.setBackground(background);
        mainPanel.add(equationSP, gbc);
        speciesBorder = BorderFactory.createTitledBorder(resbundle.getString("speciesBorder"));
        speciesTable = new JTable(new SpeciesInputTableModel(controller));
        speciesTable.setShowGrid(true);
        speciesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        speciesTable.setCellSelectionEnabled(true);
        speciesTable.setDefaultRenderer(Double.class, new ScientificRenderer());
        column = speciesTable.getColumnModel().getColumn(0);
        column.setPreferredWidth(300);
        column = speciesTable.getColumnModel().getColumn(1);
        column.setCellEditor(new DefaultCellEditor(new JComboBox(controller.getSpecieStates())));
        speciesSP = new JScrollPane(speciesTable);
        speciesSP.setBackground(background);
        speciesSP.setBorder(speciesBorder);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 0.6;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(speciesSP, gbc);
        componentBorder = BorderFactory.createTitledBorder(resbundle.getString("componentBorder"));
        componentTableModel = new ComponentTableModel(controller);
        componentTable = new JTable(componentTableModel);
        componentTable.setShowGrid(true);
        componentTable.setCellSelectionEnabled(true);
        componentTable.setDefaultRenderer(Double.class, new ScientificRenderer());
        column = componentTable.getColumnModel().getColumn(1);
        column.setCellEditor(new DefaultCellEditor(new JComboBox(controller.getComponentStates())));
        componentSP = new JScrollPane(componentTable);
        componentSP.setBorder(componentBorder);
        componentSP.setBackground(background);
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.4;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(componentSP, gbc);
        btnTitrate = new JButton(modelAction);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(btnTitrate, gbc);
        this.getContentPane().add(mainPanel);
        this.pack();
        registerForMacOSXEvents();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        if (Toolkit.getDefaultToolkit().isFrameStateSupported(JFrame.MAXIMIZED_HORIZ)) {
            setExtendedState(JFrame.MAXIMIZED_HORIZ);
        }
    }

    /** Generic registration with the Mac OS X application menu
     *  Checks the platform, then attempts to register with the Apple EAWT
     *  See OSXAdapter.java to see how this is done without directly referencing any Apple APIs
     */
    public void registerForMacOSXEvents() {
        if (MAC_OS_X) {
            try {
                OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("quit", (Class[]) null));
                OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("about", (Class[]) null));
                OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("preferences", (Class[]) null));
            } catch (Exception e) {
                System.err.println("Error while loading the OSXAdapter:");
                e.printStackTrace();
            }
        }
    }

    private void buildSystem() {
    }

    public void trial() {
        buildSystem();
        trialFrame = new TrialFrame(controller);
        trialFrame.setVisible(true);
    }

    public void about() {
        aboutBox = new AboutBox();
        aboutBox.setResizable(false);
        aboutBox.setVisible(true);
    }

    public void preferences() {
        prefs = new PrefPane();
        prefs.setResizable(false);
        prefs.setVisible(true);
    }

    public void quit() {
        System.exit(0);
    }

    public void createActions() {
        int shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        newAction = new newActionClass(resbundle.getString("newItem"), KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcutKeyMask));
        openAction = new openActionClass(resbundle.getString("openItem"), KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcutKeyMask));
        closeAction = new closeActionClass(resbundle.getString("closeItem"), KeyStroke.getKeyStroke(KeyEvent.VK_W, shortcutKeyMask));
        saveAction = new saveActionClass(resbundle.getString("saveItem"), KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcutKeyMask));
        saveAsAction = new saveAsActionClass(resbundle.getString("saveAsItem"));
        cutAction = new cutActionClass(resbundle.getString("cutItem"), KeyStroke.getKeyStroke(KeyEvent.VK_X, shortcutKeyMask));
        copyAction = new copyActionClass(resbundle.getString("copyItem"), KeyStroke.getKeyStroke(KeyEvent.VK_C, shortcutKeyMask));
        pasteAction = new pasteActionClass(resbundle.getString("pasteItem"), KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcutKeyMask));
        clearAction = new clearActionClass(resbundle.getString("clearItem"));
        preferencesAction = new preferencesActionClass(resbundle.getString("preferencesItem"), KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, shortcutKeyMask));
        aboutAction = new aboutActionClass(resbundle.getString("aboutItem"));
        modelAction = new modelActionClass(resbundle.getString("modelItem"));
        quitAction = new quitActionClass(resbundle.getString("quitItem"), KeyStroke.getKeyStroke(KeyEvent.VK_Q, shortcutKeyMask));
    }

    public void addMenus() {
        fileMenu = new JMenu(resbundle.getString("fileMenu"));
        fileMenu.add(new JMenuItem(newAction));
        fileMenu.add(new JMenuItem(openAction));
        fileMenu.add(new JMenuItem(closeAction));
        fileMenu.add(new JMenuItem(saveAction));
        fileMenu.add(new JMenuItem(saveAsAction));
        if (!MAC_OS_X) {
            fileMenu.add(new JMenuItem(preferencesAction));
            fileMenu.add(new JMenuItem(aboutAction));
            fileMenu.add(new JMenuItem(quitAction));
        }
        mainMenuBar.add(fileMenu);
        editMenu = new JMenu(resbundle.getString("editMenu"));
        editMenu.addSeparator();
        editMenu.add(new JMenuItem(cutAction));
        editMenu.add(new JMenuItem(copyAction));
        editMenu.add(new JMenuItem(pasteAction));
        editMenu.add(new JMenuItem(clearAction));
        editMenu.addSeparator();
        mainMenuBar.add(editMenu);
        mainMenuBar.setBackground(background);
        setJMenuBar(mainMenuBar);
    }

    public class newActionClass extends AbstractAction {

        public newActionClass(String text, KeyStroke shortcut) {
            super(text);
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("New...");
        }
    }

    public class openActionClass extends AbstractAction {

        public openActionClass(String text, KeyStroke shortcut) {
            super(text);
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("Open...");
            fc = new JFileChooser();
            int retval = fc.showOpenDialog(null);
            try {
                if (retval == JFileChooser.APPROVE_OPTION) {
                    fileParser = new TitratorXMLParser(fc.getSelectedFile());
                    fileParser.parseFile();
                }
            } catch (SAXParseException err) {
                System.out.println("** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
                System.out.println(" " + err.getMessage());
            } catch (SAXException saxErr) {
                Exception x = saxErr.getException();
                ((x == null) ? saxErr : x).printStackTrace();
            } catch (IOException i) {
                i.printStackTrace();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public class closeActionClass extends AbstractAction {

        public closeActionClass(String text, KeyStroke shortcut) {
            super(text);
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("Close...");
        }
    }

    public class saveActionClass extends AbstractAction {

        public saveActionClass(String text, KeyStroke shortcut) {
            super(text);
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("Save...");
        }
    }

    public class saveAsActionClass extends AbstractAction {

        public saveAsActionClass(String text) {
            super(text);
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("Save As...");
        }
    }

    public class cutActionClass extends AbstractAction {

        protected Action cutAction;

        public cutActionClass(String text, KeyStroke shortcut) {
            super(text);
            cutAction = new DefaultEditorKit.CutAction();
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            cutAction.actionPerformed(e);
        }
    }

    public class copyActionClass extends AbstractAction {

        protected Action copyAction;

        public copyActionClass(String text, KeyStroke shortcut) {
            super(text);
            copyAction = new DefaultEditorKit.CopyAction();
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            copyAction.actionPerformed(e);
        }
    }

    public class pasteActionClass extends AbstractAction {

        protected Action pasteAction;

        public pasteActionClass(String text, KeyStroke shortcut) {
            super(text);
            pasteAction = new DefaultEditorKit.PasteAction();
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            pasteAction.actionPerformed(e);
        }
    }

    public class clearActionClass extends AbstractAction {

        public clearActionClass(String text) {
            super(text);
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("Clear...");
        }
    }

    public class preferencesActionClass extends AbstractAction {

        public preferencesActionClass(String text, KeyStroke shortcut) {
            super(text);
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("Preferences");
            preferences();
        }
    }

    public class quitActionClass extends AbstractAction {

        public quitActionClass(String text, KeyStroke shortcut) {
            super(text);
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            quit();
        }
    }

    public class aboutActionClass extends AbstractAction {

        public aboutActionClass(String text) {
            super(text);
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("About Titrator");
            about();
        }
    }

    public class modelActionClass extends AbstractAction {

        public modelActionClass(String text) {
            super(text);
        }

        public void actionPerformed(ActionEvent e) {
            trial();
        }
    }

    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                new Titrator();
            }
        });
    }
}
