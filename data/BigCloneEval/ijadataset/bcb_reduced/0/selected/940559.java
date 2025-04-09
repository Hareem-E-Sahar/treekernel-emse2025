package net.sourceforge.circuitsmith.eda;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Desktop;
import java.awt.KeyEventDispatcher;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import net.sourceforge.circuitsmith.actions.EdaAction;
import net.sourceforge.circuitsmith.actions.EdaActionKey;
import net.sourceforge.circuitsmith.actions.EdaGlobalAction;
import net.sourceforge.circuitsmith.actions.EdaRecentFileAction;
import net.sourceforge.circuitsmith.dialogs.EdaPropertiesDialog;
import net.sourceforge.circuitsmith.dialogs.EdaSettingsDialog;
import net.sourceforge.circuitsmith.gedagaf.EdaGedaFileParser;
import net.sourceforge.circuitsmith.gui.EdaIcon;
import net.sourceforge.circuitsmith.gui.EdaMenuBar;
import net.sourceforge.circuitsmith.gui.EdaSplashScreen;
import net.sourceforge.circuitsmith.gui.EdaStatusBar;
import net.sourceforge.circuitsmith.gui.EdaTabbedPane;
import net.sourceforge.circuitsmith.gui.Measurement;
import net.sourceforge.circuitsmith.gui.ToolButton;
import net.sourceforge.circuitsmith.dialogs.EdaNewDocumentDialog;
import net.sourceforge.circuitsmith.objectfactory.EdaDefaultSaveableObjectFactory;
import net.sourceforge.circuitsmith.objects.EdaDrawing;
import net.sourceforge.circuitsmith.panes.EdaDocumentPane;
import net.sourceforge.circuitsmith.panes.EdaDrawingPane;
import net.sourceforge.circuitsmith.panes.EdaPcbPane;
import net.sourceforge.circuitsmith.panes.EdaSchematicPane;
import net.sourceforge.circuitsmith.parts.EdaAttribute;
import net.sourceforge.circuitsmith.parts.EdaAttributeList;
import net.sourceforge.circuitsmith.parts.EdaDocAttribute;
import net.sourceforge.circuitsmith.projects.EdaDocument;
import net.sourceforge.circuitsmith.projects.EdaFolder;
import net.sourceforge.circuitsmith.projects.EdaLibrary;
import net.sourceforge.circuitsmith.projects.EdaPcb;
import net.sourceforge.circuitsmith.projects.EdaProject;
import net.sourceforge.circuitsmith.projects.EdaSchematic;
import net.sourceforge.circuitsmith.projects.EdaTreeNode;
import net.sourceforge.circuitsmith.projects.ProjectTree;
import net.sourceforge.circuitsmith.xmlparser.EdaSettingsPrinter;
import net.sourceforge.circuitsmith.xmlparser.EdaXmlException;
import net.sourceforge.circuitsmith.xmlparser.EdaXmlParser;
import net.sourceforge.circuitsmith.xmlparser.project.EdaVersionedProjectHandler;
import net.sourceforge.circuitsmith.xmlparser.project.EdaVersionedProjectHandler.Version;

public class CircuitSmith extends JFrame {

    public static final String VERSION_STRING = "0.1.2";

    public static final File EDA_DIRECTORY = new File(System.getProperty("user.home"), ".CircuitSmith");

    private static final File SETTINGS_FILE = new File(EDA_DIRECTORY, "settings");

    private final Map<EdaActionKey, EdaAction> m_actionMap = new HashMap<EdaActionKey, EdaAction>();

    private JComponent status;

    private final EdaMenuBar m_menuBar = new EdaMenuBar();

    private final EdaTabbedPane tabbedPane = new EdaTabbedPane(this);

    private JSplitPane splitPane;

    private JSplitPane leftSplitPane;

    /** a panel to stick property stuff in*/
    private JPanel m_propertiesPane;

    private final JToolBar m_toolbar = new JToolBar();

    private final EdaXmlParser xmlParser;

    private CircuitSmithSettings settings;

    private ProjectTree projects;

    private Clipboard clipboard;

    private EdaPropertiesDialog m_propertiesDialog;

    private Measurement.Unit linearUnits = Measurement.Unit.MM;

    private Measurement.Unit angularUnits = Measurement.Unit.DEGREES;

    private boolean shiftKeyPressed;

    private final JFileChooser fileDialog;

    public static void main(String s[]) {
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        CircuitSmith e = new CircuitSmith(true);
        e.validate();
        e.setVisible(true);
        if (s.length > 0) {
            File f = new File(s[0]);
            if (f.exists()) {
                e.openProject(f);
            }
        }
    }

    public CircuitSmith(final boolean doSplash) {
        super("CircuitSmith");
        m_propertiesDialog = new EdaPropertiesDialog(this);
        setIconImage(new EdaIcon("Circuitsmith_desktop32.png").getImage());
        if (doSplash) {
            System.out.println("CircuitSmith " + VERSION_STRING + " Java Version: " + System.getProperty("java.version"));
            System.out.println("Directory: " + (new File("")).getAbsolutePath());
            final EdaSplashScreen splash = new EdaSplashScreen(this);
            addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    close();
                }

                @Override
                public void windowOpened(WindowEvent e) {
                    splash.dispose();
                }
            });
        } else {
            addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    close();
                }
            });
        }
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        try {
            xmlParser = new EdaXmlParser(new EdaDefaultSaveableObjectFactory(), this);
        } catch (EdaXmlException ex) {
            throw new RuntimeException("Unable to instantiate xml parser", ex);
        }
        if (SETTINGS_FILE.canRead()) {
            try {
                settings = xmlParser.parseEdaSettings(SETTINGS_FILE);
            } catch (EdaXmlException ex) {
                ex.printStackTrace();
                System.out.println("error parsing settings file " + settings + ", will continue with default settings.");
                settings = new CircuitSmithSettings(this);
            }
        } else {
            settings = new CircuitSmithSettings(this);
        }
        fileDialog = new JFileChooser(settings.getLatestProjectDirectory());
        fileDialog.addChoosableFileFilter(new FileFilter() {

            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".cpf") || f.isDirectory();
            }

            public String getDescription() {
                return "CircuitSmith Project Files";
            }
        });
        DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

            public boolean dispatchKeyEvent(KeyEvent e) {
                processKeyEvent(e);
                DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent((Component) e.getSource(), e);
                return true;
            }
        });
        addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftKeyPressed = true;
                }
            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftKeyPressed = false;
                }
            }

            public void keyTyped(KeyEvent e) {
            }
        });
        updateKeyBindings();
        setBounds(settings.getBounds());
        clipboard = new Clipboard("CircuitSmith");
        final Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        tabbedPane.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                updateProperties();
            }
        });
        status = new EdaStatusBar(this);
        contentPane.add(status, BorderLayout.SOUTH);
        setJMenuBar(m_menuBar);
        buildMenuBar();
        buildToolBar(m_toolbar);
        contentPane.add(m_toolbar, BorderLayout.NORTH);
        projects = new ProjectTree(this);
        final JScrollPane treePane = new JScrollPane(getProjectTree());
        leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treePane, new JPanel());
        leftSplitPane.setBottomComponent(m_propertiesPane = new JPanel());
        m_propertiesPane.setLayout(new BoxLayout(m_propertiesPane, BoxLayout.PAGE_AXIS));
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplitPane, tabbedPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(settings.getSplitPaneWidth());
        leftSplitPane.setDividerLocation(settings.getLeftSplitPaneHeight());
        contentPane.add(splitPane);
        calculateRecentFiles();
    }

    public CircuitSmith() {
        this(false);
    }

    private void saveProject() {
        final EdaProject project = getProjectTree().getSelectedProject();
        if (null == project) {
            return;
        }
        fileDialog.setDialogTitle("Save Project");
        fileDialog.setSelectedFile(project.getFile());
        if (fileDialog.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        final File saveFile = fileDialog.getSelectedFile();
        project.setFile(saveFile);
        settings.setLatestProjectDirectory(fileDialog.getCurrentDirectory());
        Version versionToSave = null;
        switch(settings.getStrategy()) {
            case KEEP_CURRENT:
                versionToSave = project.getVersion();
                break;
            case WRITE_NEWEST:
                versionToSave = CircuitSmithSettings.NEWEST_SUPPORTED_VERSION;
                break;
        }
        if (null == versionToSave) {
            versionToSave = CircuitSmithSettings.OLDEST_SUPPORTED_VERSION;
        }
        try {
            final Writer writer = new FileWriter(saveFile);
            try {
                EdaVersionedProjectHandler.printProject(versionToSave, writer, project);
                project.setVersion(versionToSave);
            } finally {
                writer.close();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        project.setUnchanged();
    }

    private void renameNode() {
        EdaTreeNode node = getProjectTree().getSelectedNode();
        String name = JOptionPane.showInputDialog(null, "Enter Name of Node", node.getName());
        if (name != null) {
            if (!(name.equals(""))) {
                node.setName(name);
            }
        }
    }

    private void closeDocument() {
        final int i = tabbedPane.getSelectedIndex();
        closeDocument(i);
    }

    /**
     * removes the document from the tabbed pane if it is there.
     */
    void closeDocument(EdaDocument doc) {
        final int i = tabbedPane.indexOfDocument(doc);
        if (i != -1) {
            closeDocument(i);
        }
    }

    public void closeDocument(int i) {
        tabbedPane.remove(i);
        if (tabbedPane.getTabCount() == 0) {
            m_actionMap.get(EdaActionKey.CLOSE_DOCUMENT).setEnabled(false);
        }
    }

    public void setSelectedDocument(EdaDocument d) {
        if (d != null) {
            tabbedPane.setSelectedDocument(d);
            setPropertiesPane(d.getDocumentPane().getPropertiesPane());
            if (tabbedPane.getTabCount() > 0) {
                m_actionMap.get(EdaActionKey.CLOSE_DOCUMENT).setEnabled(true);
            }
        } else {
            setPropertiesPane(null);
        }
    }

    /**
     * removes the currenly selected project. If it has changes since the last save, it queries the user and initiates a save if
     * requested
     * @return true if it was successful, false if the user cancels.
     */
    private boolean removeProject() {
        boolean result = true;
        EdaProject p = getProjectTree().getSelectedProject();
        int i = JOptionPane.NO_OPTION;
        if (p.isChanged()) {
            i = JOptionPane.showConfirmDialog(this, "This Project has changed. Do you want to save it?", "Save Project?", JOptionPane.YES_NO_CANCEL_OPTION);
        }
        if (i == JOptionPane.OK_OPTION) {
            saveProject();
        }
        if (i != JOptionPane.CANCEL_OPTION) {
            Enumeration e = p.depthFirstEnumeration();
            while (e.hasMoreElements()) {
                EdaTreeNode node = (EdaTreeNode) e.nextElement();
                if (node instanceof EdaDocument) {
                    EdaDocument dn = (EdaDocument) node;
                    closeDocument(dn);
                }
            }
            getProjectTree().remove(p);
        } else {
            result = false;
        }
        return result;
    }

    private void openProject(final File file) {
        try {
            final EdaProject ep = xmlParser.parseEdaProject(file);
            ep.setUnchanged();
            settings.addRecentFile(ep.getFile());
            calculateRecentFiles();
            ep.linkParts(ep);
            getProjectTree().addProject(ep);
        } catch (EdaXmlException e) {
            System.out.println(e);
        }
    }

    private File chooseSaveableFile(final String title) {
        fileDialog.setCurrentDirectory(settings.getLatestProjectDirectory());
        fileDialog.setDialogTitle(title);
        if (fileDialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            settings.setLatestProjectDirectory(fileDialog.getCurrentDirectory());
            return fileDialog.getSelectedFile();
        }
        return null;
    }

    private void openProject() {
        final File projectFile = chooseSaveableFile("Open Project");
        if (projectFile != null) {
            openProject(projectFile);
        }
    }

    public void addDocument(EdaDocument ed) {
        getProjectTree().add(ed);
        m_actionMap.get(EdaActionKey.CLOSE_DOCUMENT).setEnabled(true);
    }

    private void updateProperties() {
        if (tabbedPane.getTabCount() > 0) {
            final EdaDocumentPane pane = getSelectedDocumentPane();
            if (pane != null) {
                setSelectedDocument(pane.getDocument());
                setStatusBar(pane.getStatusBar());
                pane.setMenuItems(m_menuBar);
                m_toolbar.removeAll();
                pane.addToolbarButtons(m_toolbar);
                pane.updateUndoRedo();
            } else {
                setStatusBar(new EdaStatusBar(this));
                buildMenuBar();
                m_toolbar.removeAll();
                buildToolBar(m_toolbar);
                setSelectedDocument(null);
            }
            calculateRecentFiles();
        }
    }

    /**
     * @return the document which is currently displayed in the tabbed pane
     */
    public EdaDocumentPane getSelectedDocumentPane() {
        if (tabbedPane.isDocumentPaneAvailable()) {
            int i = tabbedPane.getSelectedIndex();
            if (i == -1) {
                i = 0;
                tabbedPane.setSelectedIndex(i);
            }
            return tabbedPane.getDocumentPaneAt(i);
        }
        return null;
    }

    private void setStatusBar(JComponent statusBar) {
        if (statusBar == null) {
            throw new IllegalArgumentException("statusBar must not be null.");
        }
        getContentPane().remove(status);
        status = statusBar;
        getContentPane().add(status, BorderLayout.SOUTH);
    }

    private void nextWindow() {
        int i = tabbedPane.getSelectedIndex() + 1;
        if (i >= tabbedPane.getTabCount()) {
            i = 0;
        }
        tabbedPane.setSelectedIndex(i);
    }

    private void showSettings() {
        final EdaSettingsDialog settingsDialog = new EdaSettingsDialog(this);
        settingsDialog.showDialog();
    }

    public Clipboard getClipboard() {
        return clipboard;
    }

    /**
     * Adds a new folder called name to the currently selected node in the project tree
     */
    private void newFolder() {
        String name = JOptionPane.showInputDialog(null, "Enter Name of Folder", "Sheet Name", JOptionPane.QUESTION_MESSAGE);
        EdaTreeNode newNode = new EdaFolder(name);
        getProjectTree().add(newNode);
    }

    /**
     * Removes the selected node from the project tree
     */
    private void removeNode() {
        EdaTreeNode node = getProjectTree().getSelectedNode();
        if (node.getChildCount() > 0) {
            JOptionPane.showMessageDialog(this, "Cannot remove non-empty node.", "Cannot Remove", JOptionPane.ERROR_MESSAGE);
        } else if (JOptionPane.showConfirmDialog(this, "Do you really want to remove " + node, "Confirm Remove", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
            getProjectTree().remove(node);
            if (node instanceof EdaDocument) {
                EdaDocument ed = (EdaDocument) node;
                int i = tabbedPane.indexOfDocument(ed);
                if (i != -1) {
                    tabbedPane.remove(i);
                }
            }
        }
    }

    /**
     * Adds a library node to the project tree
     */
    private void addLibrary() {
        final File libraryFile = chooseSaveableFile("Select Library");
        if (libraryFile != null) {
            try {
                EdaProject ep = xmlParser.parseEdaProject(libraryFile);
                ep.linkParts(ep);
                EdaLibrary lib = new EdaLibrary(this);
                System.out.println("Eda.addLibrary adding library, size: " + ep.getChildCount());
                lib.setSourceProject(ep);
                getProjectTree().add(lib);
            } catch (EdaXmlException e) {
                System.out.println(e);
            }
        }
    }

    /**
     * @return the settings object for CircuitSmith
     */
    public CircuitSmithSettings getSettings() {
        return settings;
    }

    /**
     * Makes the selected document in the tree to be made shown on the tabbed pane
     */
    public void showDocument() {
        EdaDocument doc = getProjectTree().getSelectedDocument();
        if (doc != null) {
            setSelectedDocument(doc);
        } else {
        }
    }

    /**
     * end this CircuitSmith session
     */
    private void close() {
        boolean goAhead = true;
        while (projects.getSelectedProject() != null && goAhead) {
            goAhead = removeProject();
        }
        if (goAhead) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    System.exit(0);
                }
            });
            if (!EDA_DIRECTORY.isDirectory()) {
                System.out.println("Creating settings directory: " + EDA_DIRECTORY);
                EDA_DIRECTORY.mkdir();
            }
            if (!EDA_DIRECTORY.isDirectory()) {
                System.err.println("No settings directory, cant save settings.");
            } else {
                try {
                    saveSettings();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            dispose();
        }
    }

    private void saveSettings() throws IOException {
        final Rectangle bounds = getBounds();
        settings.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
        settings.setSplitPaneWidth(splitPane.getDividerLocation());
        settings.setLeftSplitPaneHeight(leftSplitPane.getDividerLocation());
        final Writer stringWriter = new StringWriter();
        new EdaSettingsPrinter().printSettings(stringWriter, settings);
        final String stringSettings = stringWriter.toString();
        if (stringSettings.length() < 1) {
            throw new RuntimeException("failed to create settings, abort.");
        }
        System.out.println("Saving settings file: " + SETTINGS_FILE);
        final Writer settingsWriter = new FileWriter(SETTINGS_FILE);
        try {
            settingsWriter.write(stringSettings);
        } finally {
            settingsWriter.close();
        }
    }

    private void calculateRecentFiles() {
        final JMenu recentFileMenu = (JMenu) (m_menuBar.getMenuItem("File", "Recent Files..."));
        recentFileMenu.removeAll();
        char mnemnonicKey = '1';
        for (final File file : settings.getRecentFiles()) {
            recentFileMenu.add(new EdaRecentFileAction(this, file, mnemnonicKey++) {

                public void actionPerformed(ActionEvent e) {
                    openProject(file);
                }
            });
        }
    }

    private EdaMenuBar buildMenuBar() {
        m_menuBar.removeAll();
        final JMenu fileMenu = m_menuBar.getMenu("File", -1);
        fileMenu.add(new JMenuItem(m_actionMap.get(EdaActionKey.NEW_PROJECT)));
        fileMenu.add(new JMenuItem(m_actionMap.get(EdaActionKey.OPEN_PROJECT)));
        fileMenu.add(new JMenuItem(m_actionMap.get(EdaActionKey.SAVE_PROJECT)));
        fileMenu.add(new JMenuItem(m_actionMap.get(EdaActionKey.CLOSE_PROJECT)));
        fileMenu.add(new JMenuItem(m_actionMap.get(EdaActionKey.PRINT)));
        fileMenu.add(new JMenu("Recent Files..."));
        fileMenu.add(new JMenuItem(m_actionMap.get(EdaActionKey.EXIT)));
        final JMenu projectMenu = m_menuBar.getMenu("Project", -1);
        projectMenu.add(new JMenuItem(m_actionMap.get(EdaActionKey.NEW_DOCUMENT)));
        projectMenu.add(new JMenuItem(m_actionMap.get(EdaActionKey.RENAME_NODE)));
        projectMenu.add(new JMenuItem(m_actionMap.get(EdaActionKey.CLOSE_DOCUMENT)));
        projectMenu.add(new JMenuItem(m_actionMap.get(EdaActionKey.NEW_FOLDER)));
        projectMenu.add(new JMenuItem(m_actionMap.get(EdaActionKey.REMOVE_NODE)));
        projectMenu.add(new JMenuItem(m_actionMap.get(EdaActionKey.ADD_LIBRARY)));
        projectMenu.add(new JMenuItem(m_actionMap.get(EdaActionKey.SHOW_DOCUMENT)));
        projectMenu.add(new JMenuItem(m_actionMap.get(EdaActionKey.CALC_NETS)));
        final JMenu settingsMenu = m_menuBar.getMenu("Settings", -1);
        settingsMenu.add(new JMenuItem(m_actionMap.get(EdaActionKey.SETTINGS)));
        final JMenu windowMenu = m_menuBar.getMenu("Window", -1);
        windowMenu.add(new JMenuItem(m_actionMap.get(EdaActionKey.NEXT_WINDOW)));
        final JMenu helpMenu = m_menuBar.getMenu("Help", -1);
        helpMenu.add(new JMenuItem(m_actionMap.get(EdaActionKey.ABOUT)));
        helpMenu.add(new JMenuItem(m_actionMap.get(EdaActionKey.WEB_SITE)));
        helpMenu.add(new JMenuItem(m_actionMap.get(EdaActionKey.DEBUG)));
        return m_menuBar;
    }

    /**
     * @return a toolbar for this object to do useful stuff
     */
    private void buildToolBar(final JToolBar toolBar) {
        toolBar.add(new ToolButton(m_actionMap.get(EdaActionKey.EXIT)));
        toolBar.add(new ToolButton(m_actionMap.get(EdaActionKey.NEW_DOCUMENT)));
        toolBar.add(new ToolButton(m_actionMap.get(EdaActionKey.OPEN_PROJECT)));
        toolBar.add(new ToolButton(m_actionMap.get(EdaActionKey.SAVE_PROJECT)));
        toolBar.add(new ToolButton(m_actionMap.get(EdaActionKey.PRINT)));
    }

    public ProjectTree getProjectTree() {
        return projects;
    }

    /**
     * causes the selected library node to reload from the source project
     */
    private void reloadLibrary() {
        EdaTreeNode node = getProjectTree().getSelectedNode();
        if (node instanceof EdaLibrary) {
            final EdaLibrary library = (EdaLibrary) node;
            try {
                EdaProject newLibrary = xmlParser.parseEdaProject(library.getFile());
                library.removeAllChildren();
                library.setSourceProject(newLibrary);
                library.getPartList().updateSymbols();
                EdaProject containingProject = ((EdaTreeNode) library.getParent()).getProject();
                while (containingProject != null) {
                    containingProject.getPartList().updateSymbols();
                    containingProject = ((EdaTreeNode) containingProject.getParent()).getProject();
                }
                getProjectTree().refresh();
            } catch (EdaXmlException ex) {
                System.out.println(ex);
            }
        }
    }

    /**
     * Invokes a print dialog and then prints the selected document
     */
    private void print() {
        PrinterJob pj = PrinterJob.getPrinterJob();
        final EdaDocumentPane dp = getSelectedDocumentPane();
        Pageable p = new Pageable() {

            public int getNumberOfPages() {
                return Pageable.UNKNOWN_NUMBER_OF_PAGES;
            }

            public PageFormat getPageFormat(int page) throws IndexOutOfBoundsException {
                return dp.getDesiredPageFormat();
            }

            public Printable getPrintable(int arg0) throws IndexOutOfBoundsException {
                return dp;
            }
        };
        pj.setPageable(p);
        if (pj.printDialog()) {
            try {
                pj.print();
            } catch (PrinterException e) {
                System.out.println(e);
            }
        }
    }

    public final Map<EdaActionKey, EdaAction> getActionMap() {
        return m_actionMap;
    }

    private final void createActionMap() {
        m_actionMap.clear();
        m_actionMap.put(EdaActionKey.NEW_PCB_DOC, new EdaGlobalAction(this, EdaActionKey.NEW_PCB_DOC) {

            public void actionPerformed(ActionEvent e) {
                getEda().addDocument(new EdaPcb(new EdaPcbPane(getEda())));
            }
        });
        m_actionMap.put(EdaActionKey.NEW_SCHEMATIC_DOC, new EdaGlobalAction(this, EdaActionKey.NEW_SCHEMATIC_DOC) {

            public void actionPerformed(ActionEvent e) {
                getEda().addDocument(new EdaSchematic(new EdaSchematicPane(getEda())));
            }
        });
        m_actionMap.put(EdaActionKey.NEW_DOCUMENT, new EdaGlobalAction(this, EdaActionKey.NEW_DOCUMENT) {

            public void actionPerformed(ActionEvent e) {
                EdaNewDocumentDialog.showDialog(getEda());
            }
        });
        m_actionMap.put(EdaActionKey.IMPORT_GEDA_FILE, new EdaGlobalAction(this, EdaActionKey.IMPORT_GEDA_FILE) {

            private File chooseGedaFile() {
                fileDialog.setDialogTitle("Choose a symbol file");
                if (fileDialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    return fileDialog.getSelectedFile();
                }
                return null;
            }

            public void actionPerformed(ActionEvent e) {
                final File gedaFile = chooseGedaFile();
                if (gedaFile == null) {
                    return;
                }
                final EdaGedaFileParser parser = new EdaGedaFileParser(new EdaDefaultSaveableObjectFactory(), false);
                try {
                    final EdaDrawing drawing = parser.parseGedaSymbolFile(gedaFile);
                    final EdaDrawingPane pane = new EdaSchematicPane(getEda(), drawing);
                    final EdaSchematic document = new EdaSchematic(pane);
                    document.setName(gedaFile.getName());
                    getEda().addDocument(document);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        m_actionMap.put(EdaActionKey.NEW_PROJECT, new EdaGlobalAction(this, EdaActionKey.NEW_PROJECT) {

            public void actionPerformed(ActionEvent e) {
                EdaProject ep = new EdaProject(getEda());
                ep.setName("Untitled");
                getEda().getProjectTree().addProject(ep);
            }
        });
        m_actionMap.put(EdaActionKey.CLOSE_PROJECT, new EdaGlobalAction(this, EdaActionKey.CLOSE_PROJECT) {

            public void actionPerformed(ActionEvent e) {
                getEda().removeProject();
            }
        });
        m_actionMap.put(EdaActionKey.OPEN_PROJECT, new EdaGlobalAction(this, EdaActionKey.OPEN_PROJECT) {

            public void actionPerformed(ActionEvent e) {
                getEda().openProject();
            }
        });
        m_actionMap.put(EdaActionKey.SAVE_PROJECT, new EdaGlobalAction(this, EdaActionKey.SAVE_PROJECT) {

            public void actionPerformed(ActionEvent e) {
                getEda().saveProject();
            }
        });
        m_actionMap.put(EdaActionKey.NEXT_WINDOW, new EdaGlobalAction(this, EdaActionKey.NEXT_WINDOW) {

            public void actionPerformed(ActionEvent e) {
                getEda().nextWindow();
            }
        });
        m_actionMap.put(EdaActionKey.RENAME_NODE, new EdaGlobalAction(this, EdaActionKey.RENAME_NODE) {

            public void actionPerformed(ActionEvent e) {
                getEda().renameNode();
            }
        });
        m_actionMap.put(EdaActionKey.CLOSE_DOCUMENT, new EdaGlobalAction(this, EdaActionKey.CLOSE_DOCUMENT) {

            public void actionPerformed(ActionEvent e) {
                getEda().closeDocument();
            }
        });
        m_actionMap.put(EdaActionKey.CUT_TREE_NODE, new EdaGlobalAction(this, EdaActionKey.CUT_TREE_NODE) {

            public void actionPerformed(ActionEvent e) {
                getEda().getProjectTree().cut();
            }
        });
        m_actionMap.put(EdaActionKey.COPY_TREE_NODE, new EdaGlobalAction(this, EdaActionKey.COPY_TREE_NODE) {

            public void actionPerformed(ActionEvent e) {
                getEda().getProjectTree().copy();
            }
        });
        m_actionMap.put(EdaActionKey.MOVE_NODE_UP, new EdaGlobalAction(this, EdaActionKey.MOVE_NODE_UP) {

            public void actionPerformed(ActionEvent e) {
                getEda().getProjectTree().moveUp();
            }
        });
        m_actionMap.put(EdaActionKey.MOVE_NODE_DOWN, new EdaGlobalAction(this, EdaActionKey.MOVE_NODE_DOWN) {

            public void actionPerformed(ActionEvent e) {
                getEda().getProjectTree().moveDown();
            }
        });
        m_actionMap.put(EdaActionKey.MOVE_NODE_RIGHT, new EdaGlobalAction(this, EdaActionKey.MOVE_NODE_RIGHT) {

            public void actionPerformed(ActionEvent e) {
                getEda().getProjectTree().moveRight();
            }
        });
        m_actionMap.put(EdaActionKey.MOVE_NODE_LEFT, new EdaGlobalAction(this, EdaActionKey.MOVE_NODE_LEFT) {

            public void actionPerformed(ActionEvent e) {
                getEda().getProjectTree().moveLeft();
            }
        });
        m_actionMap.put(EdaActionKey.PASTE_TREE_NODE, new EdaGlobalAction(this, EdaActionKey.PASTE_TREE_NODE) {

            public void actionPerformed(ActionEvent e) {
                getEda().getProjectTree().paste();
            }
        });
        m_actionMap.put(EdaActionKey.SHOW_DOCUMENT, new EdaGlobalAction(this, EdaActionKey.SHOW_DOCUMENT) {

            public void actionPerformed(ActionEvent e) {
                getEda().showDocument();
            }
        });
        m_actionMap.put(EdaActionKey.NEW_FOLDER, new EdaGlobalAction(this, EdaActionKey.NEW_FOLDER) {

            public void actionPerformed(ActionEvent e) {
                getEda().newFolder();
            }
        });
        m_actionMap.put(EdaActionKey.ADD_LIBRARY, new EdaGlobalAction(this, EdaActionKey.ADD_LIBRARY) {

            public void actionPerformed(ActionEvent e) {
                getEda().addLibrary();
            }
        });
        m_actionMap.put(EdaActionKey.REMOVE_NODE, new EdaGlobalAction(this, EdaActionKey.REMOVE_NODE) {

            public void actionPerformed(ActionEvent e) {
                getEda().removeNode();
            }
        });
        m_actionMap.put(EdaActionKey.SETTINGS, new EdaGlobalAction(this, EdaActionKey.SETTINGS) {

            public void actionPerformed(ActionEvent e) {
                getEda().showSettings();
            }
        });
        m_actionMap.put(EdaActionKey.PRINT, new EdaGlobalAction(this, EdaActionKey.PRINT) {

            public void actionPerformed(ActionEvent e) {
                getEda().print();
            }
        });
        m_actionMap.put(EdaActionKey.EXIT, new EdaGlobalAction(this, EdaActionKey.EXIT) {

            public void actionPerformed(ActionEvent e) {
                getEda().close();
            }
        });
        m_actionMap.put(EdaActionKey.ABOUT, new EdaGlobalAction(this, EdaActionKey.ABOUT) {

            public void actionPerformed(ActionEvent e) {
                new EdaSplashScreen(getEda());
            }
        });
        m_actionMap.put(EdaActionKey.WEB_SITE, new EdaGlobalAction(this, EdaActionKey.WEB_SITE) {

            public void actionPerformed(ActionEvent e) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI("http://www.circuitsmith.com"));
                    } catch (Exception ioe) {
                    }
                }
            }
        });
        m_actionMap.put(EdaActionKey.DEBUG, new EdaGlobalAction(this, EdaActionKey.DEBUG) {

            public void actionPerformed(ActionEvent e) {
                EdaProject p = getEda().getProjectTree().getSelectedProject();
                EdaTreeNode n = getEda().getProjectTree().getSelectedNode();
                System.out.println("Eda.createActionMap working on node " + n.getName());
                for (Iterator<EdaTreeNode> i = n.iterator(); i.hasNext(); ) {
                    EdaTreeNode tn = i.next();
                    if (tn instanceof EdaSchematic) {
                        EdaSchematic s = (EdaSchematic) tn;
                        EdaAttributeList al = s.getDrawing().getAttributeList();
                        EdaAttribute f = al.get("footprint");
                        if (f != null) {
                            String fs[] = f.getValue().split("//");
                            if (fs.length > 1) {
                                EdaDocAttribute fl = new EdaDocAttribute("footprint-lib", fs[0]);
                                f.setValue(fs[1]);
                                fl.linkDocument(p);
                                al.add(fl);
                                System.out.println("Eda.createActionMap debug doing footprints for " + tn.getName());
                            }
                        }
                    }
                }
            }
        });
        m_actionMap.put(EdaActionKey.RELOAD_LIBRARY, new EdaGlobalAction(this, EdaActionKey.RELOAD_LIBRARY) {

            public void actionPerformed(ActionEvent e) {
                getEda().reloadLibrary();
            }
        });
        m_actionMap.put(EdaActionKey.CALC_NETS, new EdaGlobalAction(this, EdaActionKey.CALC_NETS) {

            public void actionPerformed(ActionEvent e) {
                getEda().calcNets();
            }
        });
    }

    public void updateKeyBindings() {
        createActionMap();
        final JRootPane edaRootPane = getRootPane();
        final InputMap keyMap = edaRootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        keyMap.clear();
        final ActionMap actionMap = edaRootPane.getActionMap();
        for (final Map.Entry<EdaActionKey, EdaAction> entry : m_actionMap.entrySet()) {
            final EdaActionKey key = entry.getKey();
            final EdaAction action = entry.getValue();
            keyMap.put((KeyStroke) action.getValue(Action.ACCELERATOR_KEY), key);
            actionMap.put(key, action);
        }
        if (tabbedPane != null) {
            tabbedPane.updateKeyBindings();
            final EdaDocumentPane pane = getSelectedDocumentPane();
            if (pane != null) {
                pane.setMenuItems(m_menuBar);
                m_toolbar.removeAll();
                pane.addToolbarButtons(m_toolbar);
            }
        }
    }

    public Measurement.Unit getLinearUnits() {
        return linearUnits;
    }

    public Measurement.Unit getAngularUnits() {
        return angularUnits;
    }

    public void setUnits(Measurement.Unit u) {
        if (u.getType() == Measurement.UnitType.ANGULAR) {
            angularUnits = u;
        } else if (u.getType() == Measurement.UnitType.LINEAR) {
            linearUnits = u;
        }
    }

    public Measurement.Unit getUnit(Measurement m) {
        if (m.getUnitType() == Measurement.UnitType.ANGULAR) {
            return angularUnits;
        } else if (m.getUnitType() == Measurement.UnitType.LINEAR) {
            return linearUnits;
        }
        return null;
    }

    public EdaPropertiesDialog getPropertiesDialog() {
        return m_propertiesDialog;
    }

    private void setPropertiesPane(Component c) {
        m_propertiesPane.removeAll();
        if (c != null) {
            m_propertiesPane.add(c);
        }
    }

    public boolean isShiftKeyPressed() {
        return shiftKeyPressed;
    }

    /**
     * calculates the nets for this project, based on the top level schematic
     *
     */
    public void calcNets() {
        EdaProject p = getProjectTree().getSelectedProject();
        List<EdaSchematic> schematics = p.getChildrenOfClass(EdaSchematic.class);
        EdaSchematic topSchematic = null;
        if (schematics.size() == 1) {
            topSchematic = schematics.get(0);
        } else {
            topSchematic = (EdaSchematic) JOptionPane.showInputDialog(this, "Select which schematic is the top level", "Select Schematic", JOptionPane.QUESTION_MESSAGE, new EdaIcon("Question48.png"), schematics.toArray(new EdaSchematic[0]), schematics.get(0));
        }
        if (topSchematic != null) {
            EdaSchematicPane sp = (EdaSchematicPane) (topSchematic.getDocumentPane());
            p.updateNetList(sp.getDrawing());
        }
    }

    public JFileChooser getFileChooser() {
        return fileDialog;
    }
}
