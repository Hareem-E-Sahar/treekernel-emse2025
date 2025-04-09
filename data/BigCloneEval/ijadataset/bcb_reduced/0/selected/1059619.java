package uqdsd.infosec;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.undo.UndoManager;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import uqdsd.infosec.actions.AboutAction;
import uqdsd.infosec.actions.AllPathsAction;
import uqdsd.infosec.actions.BackgroundTaskAction;
import uqdsd.infosec.actions.CloseAction;
import uqdsd.infosec.actions.ConfigurationAction;
import uqdsd.infosec.actions.CreateMinimalCutSetAction;
import uqdsd.infosec.actions.ExitAction;
import uqdsd.infosec.actions.FlowAnalysisAction;
import uqdsd.infosec.actions.ImportAction;
import uqdsd.infosec.actions.OpenAction;
import uqdsd.infosec.actions.PointPointConsistencyAction;
import uqdsd.infosec.actions.RenameAction;
import uqdsd.infosec.actions.SaveAction;
import uqdsd.infosec.actions.SortMatrixAction;
import uqdsd.infosec.actions.SystemSaveAction;
import uqdsd.infosec.actions.SystemSaveAsAction;
import uqdsd.infosec.actions.TraceConsistencyAction;
import uqdsd.infosec.model.Hierarchy;
import uqdsd.infosec.model.HierarchyNode;
import uqdsd.infosec.model.InstanceComponent;
import uqdsd.infosec.model.NameChangeEvent;
import uqdsd.infosec.model.NameChangeEventListener;
import uqdsd.infosec.model.PortComponent;
import uqdsd.infosec.model.Schematic;
import uqdsd.infosec.model.StandardComponent;
import uqdsd.infosec.model.faultset.FaultMode;
import uqdsd.infosec.model.faultset.FaultModeSet;
import uqdsd.infosec.model.visitors.XMLVisitor;
import uqdsd.infosec.tasks.BackgroundTaskManager;
import uqdsd.infosec.tasks.BackgroundTaskManagerPanel;

/**
 * @author	InfoSec Project (c) 2008 UQ The main class responsible for the GUI frame and primary
 *			interaction
 */
public class InfoSec extends JFrame implements GraphModelListener, InternalFrameListener, DropEscalationListener, NameChangeEventListener {

    static final long serialVersionUID = 0;

    private static enum STARTACTION {

        NORMAL, EXPORT
    }

    ;

    private static String VERSION = "1.0";

    private static final String BUILDDATE = "$Date: 2010/05/26 11:48:00 $";

    static {
        String bd = BUILDDATE.replaceAll("\\$", "").replaceAll("Date:", "").trim();
        bd = bd.substring(0, bd.indexOf(' '));
        bd = bd.replaceAll("/", "");
        VERSION += "." + bd;
        if (GlobalProperties.getCondition("SIFA.AlphaFeatures")) {
            VERSION += "α";
        }
    }

    private static final String DEFAULTSTATUS = "Welcome to SIFA v. " + VERSION;

    private static final String SYSTEMICON = "img/diode.png";

    private static final String SMSYSTEMICON = "img/diode-small.png";

    private static final String WINDOWNAME = "Secure Information Flow Analyser - v. " + VERSION;

    private static InfoSec singleInstance = null;

    private static final int FILE_ABBREVIATION_SIZE = 30;

    private JMenuItem openItem, aboutItem, exitItem, configItem;

    private JMenuItem saveItem = new JMenuItem("Save");

    private JMenuItem saveAsItem = new JMenuItem("Save As...");

    private JMenuItem closeItem = new JMenuItem("Close");

    private boolean dirty = false;

    private JDesktopPane desktopPane = new JDesktopPane();

    private JMenuBar menuBar;

    private JMenu sifaMenu, fileMenu;

    private Hierarchy hierarchy = null;

    private JTree tree = null;

    private final JLabel statusBar = new JLabel(DEFAULTSTATUS);

    private JFileChooser fc = null;

    private final LinkedList<File> recentFiles = new LinkedList<File>();

    private final LinkedList<JMenuItem> recentFileMenuItems = new LinkedList<JMenuItem>();

    private final BackgroundTaskManager backgroundTaskManager = new BackgroundTaskManager();

    private Map<String, JInternalFrame> hierarchyNodeHashToInternalFrame = new TreeMap<String, JInternalFrame>();

    private Map<String, HierarchyNode> hierarchyNodeHashToHierarchyNode = new TreeMap<String, HierarchyNode>();

    private HierarchyNode autoSelected = null;

    private File currentlyOpenFile = null;

    private UndoManager undoManager = new SIFAUndoManager();

    private Map<String, String> inputFilterCommands = new TreeMap<String, String>();

    private Map<String, String> outputFilterCommands = new TreeMap<String, String>();

    public InfoSec() {
        this(null, STARTACTION.NORMAL);
    }

    public InfoSec(String toOpen) {
        this(toOpen, STARTACTION.NORMAL);
    }

    @SuppressWarnings("serial")
    public InfoSec(String toOpen, STARTACTION command) {
        super(WINDOWNAME);
        singleInstance = this;
        if (command == STARTACTION.EXPORT && toOpen != null) {
            File file = new File(toOpen);
            try {
                exportFile(file, new PrintWriter(System.out));
            } catch (ApplicationException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
            System.exit(0);
        }
        setIconImage(Toolkit.getDefaultToolkit().getImage(SYSTEMICON));
        statusBar.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                try {
                    exit();
                } catch (ApplicationException ex) {
                    ex.handler();
                }
            }
        });
        menuBar = new JMenuBar();
        openItem = new JMenuItem("Open");
        openItem.setIcon(new ImageIcon(getClass().getClassLoader().getResource("img/fileopen.gif")));
        openItem.setMnemonic(KeyEvent.VK_O);
        openItem.addActionListener(new OpenAction(this));
        saveItem.setMnemonic(KeyEvent.VK_S);
        saveItem.setIcon(new ImageIcon(getClass().getClassLoader().getResource("img/save.png")));
        saveItem.addActionListener(new SystemSaveAction(this));
        saveItem.setEnabled(false);
        saveAsItem.setMnemonic(KeyEvent.VK_A);
        saveAsItem.setIcon(new ImageIcon(getClass().getClassLoader().getResource("img/save.png")));
        saveAsItem.addActionListener(new SystemSaveAsAction(this));
        saveAsItem.setEnabled(false);
        closeItem.setMnemonic(KeyEvent.VK_C);
        closeItem.addActionListener(new CloseAction(this));
        aboutItem = new JMenuItem("About/Legal");
        aboutItem.setIcon(new ImageIcon(getClass().getClassLoader().getResource(SMSYSTEMICON)));
        aboutItem.setMnemonic(KeyEvent.VK_A);
        aboutItem.addActionListener(new AboutAction(this));
        configItem = new JMenuItem("Preferences...");
        configItem.setMnemonic(KeyEvent.VK_P);
        configItem.addActionListener(new ConfigurationAction());
        exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic(KeyEvent.VK_X);
        exitItem.addActionListener(new ExitAction(this));
        fileMenu = new JMenu("Project");
        fileMenu.setMnemonic(KeyEvent.VK_P);
        Properties p = new Properties();
        try {
            p.load(new FileInputStream(new File("recent.prop")));
            for (int ii = 1; ii <= 4; ++ii) {
                if (p.containsKey("" + ii)) {
                    String fileName = p.getProperty("" + ii);
                    File f = new File(fileName);
                    if (f.canRead()) {
                        recentFiles.addLast(f);
                        JMenuItem jmi = new JMenuItem(abbreviateFileName(f.getCanonicalPath()));
                        recentFileMenuItems.addLast(jmi);
                        jmi.addActionListener(new OpenAction(this, fileName));
                    }
                }
            }
        } catch (Exception e) {
        }
        constructFileMenu();
        menuBar.add(fileMenu);
        JMenuItem highlightItem = new JMenuItem("Highlight ports...");
        highlightItem.setMnemonic(KeyEvent.VK_H);
        highlightItem.addActionListener(new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog("Please enter a port name to highlight", null);
                PortComponent.highlightName(name);
            }
        });
        JMenuItem clearHighlightItem = new JMenuItem("Clear highlighted ports");
        clearHighlightItem.setMnemonic(KeyEvent.VK_C);
        clearHighlightItem.addActionListener(new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                PortComponent.clearHighlightedNames();
            }
        });
        final String UNDOICON = "img/undo.gif";
        final String REDOICON = "img/redo.gif";
        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.setIcon(new ImageIcon(getClass().getClassLoader().getResource(UNDOICON)));
        undoItem.setMnemonic(KeyEvent.VK_U);
        undoItem.setEnabled(false);
        undoItem.addActionListener(new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (undoManager.canUndo()) undoManager.undo();
            }
        });
        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.setIcon(new ImageIcon(getClass().getClassLoader().getResource(REDOICON)));
        redoItem.setMnemonic(KeyEvent.VK_R);
        redoItem.setEnabled(false);
        redoItem.addActionListener(new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (undoManager.canRedo()) undoManager.redo();
            }
        });
        JMenuItem simulationItem = new JMenuItem("Consistency analysis (trace)");
        simulationItem.setMnemonic(KeyEvent.VK_T);
        simulationItem.addActionListener(new TraceConsistencyAction(this));
        JMenuItem ceSimulationItem = new JMenuItem("Consistency analysis (point-point)");
        ceSimulationItem.setMnemonic(KeyEvent.VK_P);
        ceSimulationItem.addActionListener(new PointPointConsistencyAction(this));
        JMenuItem allpathsItem = new JMenuItem("Find all paths");
        allpathsItem.setMnemonic(KeyEvent.VK_A);
        allpathsItem.addActionListener(new AllPathsAction(this));
        JMenu analysisMenu = new JMenu("Analysis");
        analysisMenu.setMnemonic(KeyEvent.VK_A);
        analysisMenu.add(simulationItem);
        analysisMenu.add(ceSimulationItem);
        analysisMenu.add(allpathsItem);
        analysisMenu.addSeparator();
        analysisMenu.add(highlightItem);
        analysisMenu.add(clearHighlightItem);
        menuBar.add(analysisMenu);
        JMenuItem taskItem = new JMenuItem("Task manager");
        taskItem.setMnemonic(KeyEvent.VK_T);
        taskItem.addActionListener(new BackgroundTaskAction(this));
        sifaMenu = new JMenu("SIFA");
        sifaMenu.setMnemonic(KeyEvent.VK_S);
        sifaMenu.add(taskItem);
        sifaMenu.addSeparator();
        sifaMenu.add(configItem);
        sifaMenu.add(aboutItem);
        menuBar.add(sifaMenu);
        newSchematic();
        if (toOpen != null) {
            try {
                openFile(toOpen);
            } catch (ApplicationException e) {
                e.handler();
            }
        }
        desktopPane.setBackground(GlobalProperties.getColor("Editor.DesktopColour"));
        JSplitPane leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree), desktopPane);
        leftSplitPane.setDividerLocation(200);
        leftSplitPane.setDividerSize(4);
        JSplitPane desktopSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, leftSplitPane, new BackgroundTaskManagerPanel(getBackgroundTaskManager()));
        desktopSplitPane.setDividerSize(2);
        desktopSplitPane.setResizeWeight(1);
        desktopSplitPane.setDividerLocation(500);
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(desktopSplitPane, BorderLayout.CENTER);
        setJMenuBar(menuBar);
        setContentPane(contentPane);
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    private String abbreviateFileName(String filename) {
        if (filename.length() > FILE_ABBREVIATION_SIZE) {
            int idx = filename.indexOf(File.separatorChar, filename.length() - FILE_ABBREVIATION_SIZE);
            return "... " + filename.substring(idx >= 0 ? idx : filename.length() - FILE_ABBREVIATION_SIZE);
        } else {
            return filename;
        }
    }

    private void constructFileMenu() {
        fileMenu.removeAll();
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.add(closeItem);
        fileMenu.addSeparator();
        if (!recentFileMenuItems.isEmpty()) {
            int ii = 1;
            for (JMenuItem jmi : recentFileMenuItems) {
                if (ii > 4) {
                    break;
                }
                if (jmi.getMnemonic() != KeyEvent.VK_0 + ii) {
                    boolean isPrefixed = false;
                    for (int jj = 1; jj <= 4; jj++) {
                        if (jmi.getText().startsWith(jj + ": ")) {
                            isPrefixed = true;
                        }
                    }
                    if (isPrefixed) {
                        jmi.setText(jmi.getText().substring(3));
                    }
                    jmi.setText(ii + ": " + jmi.getText());
                    jmi.setMnemonic(KeyEvent.VK_0 + ii);
                }
                fileMenu.add(jmi);
                ii++;
            }
            fileMenu.addSeparator();
        }
        fileMenu.add(exitItem);
    }

    public BackgroundTaskManager getBackgroundTaskManager() {
        return backgroundTaskManager;
    }

    private String pathDelimConv(String s) {
        return s;
    }

    public JFileChooser getSaveFileChooser() {
        JFileChooser ofc = new JFileChooser();
        ofc.setCurrentDirectory(new File(System.getProperty("user.dir")));
        try {
            Properties execMap = new Properties();
            execMap.load(new FileInputStream(new File("output.prop")));
            for (Object key : execMap.keySet()) {
                String entry = key.toString();
                if (entry.indexOf('.') < 0) {
                    String command = execMap.getProperty(entry);
                    ExtensionFilter ef;
                    if (execMap.containsKey(entry + ".description")) {
                        String desc = execMap.getProperty(entry + ".description");
                        ef = new ExtensionFilter(desc);
                    } else {
                        ef = new ExtensionFilter(entry + " files");
                    }
                    ef.addExtension(entry);
                    outputFilterCommands.put(entry, pathDelimConv(command));
                    if (execMap.containsKey(entry + ".alt")) {
                        String alt = execMap.getProperty(entry + ".alt");
                        StringTokenizer st = new StringTokenizer(alt);
                        while (st.hasMoreTokens()) {
                            String token = st.nextToken();
                            ef.addExtension(token);
                            outputFilterCommands.put(token, pathDelimConv(command));
                        }
                    }
                    ofc.addChoosableFileFilter(ef);
                }
            }
        } catch (Exception e) {
        }
        ExtensionFilter sifafilter = new ExtensionFilter("SIFA files (.sifa)");
        sifafilter.addExtension("sifa");
        ofc.addChoosableFileFilter(sifafilter);
        ofc.setFileFilter(sifafilter);
        return ofc;
    }

    public JFileChooser getOpenFileChooser() {
        if (fc == null) {
            fc = new JFileChooser();
            fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
            try {
                Properties execMap = new Properties();
                execMap.load(new FileInputStream(new File("input.prop")));
                for (Object key : execMap.keySet()) {
                    String entry = key.toString();
                    if (entry.indexOf('.') < 0) {
                        String command = execMap.getProperty(entry);
                        ExtensionFilter ef;
                        if (execMap.containsKey(entry + ".description")) {
                            String desc = execMap.getProperty(entry + ".description");
                            ef = new ExtensionFilter(desc);
                        } else {
                            ef = new ExtensionFilter(entry + " files");
                        }
                        ef.addExtension(entry);
                        inputFilterCommands.put(entry, pathDelimConv(command));
                        if (execMap.containsKey(entry + ".alt")) {
                            String alt = execMap.getProperty(entry + ".alt");
                            StringTokenizer st = new StringTokenizer(alt);
                            while (st.hasMoreTokens()) {
                                String token = st.nextToken();
                                ef.addExtension(token);
                                inputFilterCommands.put(token, pathDelimConv(command));
                            }
                        }
                        fc.addChoosableFileFilter(ef);
                    }
                }
            } catch (Exception e) {
            }
            ExtensionFilter sifafilter = new ExtensionFilter("SIFA files (.sifa)");
            sifafilter.addExtension("sifa");
            fc.addChoosableFileFilter(sifafilter);
            fc.setFileFilter(sifafilter);
        }
        return fc;
    }

    public static InfoSec getInstance() {
        return singleInstance;
    }

    public void setStatus(String text) {
        statusBar.setText(text);
    }

    public void freeze(boolean on) {
        if (on) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        desktopPane.setEnabled(!on);
        for (JInternalFrame c : desktopPane.getAllFrames()) {
            c.getContentPane().setEnabled(!on);
            for (Component mc : c.getJMenuBar().getComponents()) {
                mc.setEnabled(!on);
            }
        }
        tree.setEnabled(!on);
        menuBar.setEnabled(!on);
        for (Component c : menuBar.getComponents()) {
            c.setEnabled(!on);
        }
        sifaMenu.setEnabled(true);
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        try {
            while (true) {
                int data = in.read();
                if (data == -1) {
                    break;
                }
                out.write(data);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    @SuppressWarnings("null")
    private Document openDoc(File file) throws ApplicationException {
        Document doc = null;
        ByteArrayOutputStream buffer = null;
        ByteArrayOutputStream errorMessage = null;
        int lastDot = file.getName().lastIndexOf('.');
        if (lastDot >= 0) {
            String extension = file.getName().substring(lastDot + 1);
            try {
                if (inputFilterCommands.containsKey(extension)) {
                    String command = inputFilterCommands.get(extension);
                    Process filter = Runtime.getRuntime().exec(command);
                    InputStream fileData = new BufferedInputStream(new FileInputStream(file));
                    OutputStream processInput = new BufferedOutputStream(filter.getOutputStream());
                    InputStream processOutput = new BufferedInputStream(filter.getInputStream());
                    InputStream processError = new BufferedInputStream(filter.getErrorStream());
                    buffer = new ByteArrayOutputStream();
                    try {
                        copy(fileData, processInput);
                        copy(processOutput, buffer);
                        copy(processError, errorMessage);
                    } catch (Exception e) {
                        filter.destroy();
                        throw new ApplicationException("Converter subprocess failed");
                    }
                    int exitCode = filter.waitFor();
                    if (exitCode != 0) {
                        String message = errorMessage.toString();
                        buffer.close();
                        throw new ApplicationException("Converter process returned exit code " + exitCode + "\n" + message);
                    }
                } else {
                    buffer = null;
                }
            } catch (Exception e) {
                if (e instanceof ApplicationException) {
                    throw (ApplicationException) e;
                }
                buffer = null;
            }
        }
        if (buffer == null) {
            try {
                buffer = new ByteArrayOutputStream();
                copy(new FileInputStream(file), buffer);
            } catch (Exception e) {
                throw new ApplicationException("Problem accessing " + file.getName());
            }
        }
        if (buffer != null) {
            SAXBuilder builder = new SAXBuilder();
            try {
                ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(buffer.toByteArray()));
                zis.getNextEntry();
                doc = builder.build(new BufferedInputStream(zis));
            } catch (Exception e) {
                try {
                    doc = builder.build(new ByteArrayInputStream(buffer.toByteArray()));
                } catch (Exception e2) {
                    throw new ApplicationException(file.getName() + " cannot be parsed.\n" + e2.getMessage());
                }
            }
            try {
                buffer.close();
            } catch (IOException e) {
                throw new ApplicationException("Could not close internal buffer.");
            }
        }
        return doc;
    }

    public void exportFile(File file, Writer w) throws ApplicationException {
        Document doc = openDoc(file);
        if (doc != null) {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            try {
                outputter.output(doc, w);
            } catch (IOException ioe) {
                System.err.println("IO Error");
            }
        }
    }

    public Rectangle defaultDialogBounds() {
        Rectangle bounds = getBounds();
        bounds.grow(-bounds.width / 5, -bounds.height / 5);
        return bounds.getBounds();
    }

    public HierarchyNode getCurrentlySelectedHierarchyNode() {
        TreePath path = tree.getSelectionPath();
        if (path != null) {
            return (HierarchyNode) path.getLastPathComponent();
        }
        return null;
    }

    public HierarchyNode lookupHierarchyNode(String key) {
        if (hierarchyNodeHashToHierarchyNode.containsKey(key)) {
            return hierarchyNodeHashToHierarchyNode.get(key);
        } else {
            return null;
        }
    }

    public void setDirty(boolean dirty) {
        if (this.dirty != dirty) {
            this.dirty = dirty;
            saveItem.setEnabled(dirty && currentlyOpenFile != null);
            saveAsItem.setEnabled(dirty);
            setTitle(WINDOWNAME + (currentlyOpenFile != null || dirty ? " [" + (currentlyOpenFile != null ? currentlyOpenFile.getName() : "") + (dirty ? "*" : "") + "]" : ""));
        }
    }

    private static void printUsage() {
        System.out.println();
        System.out.println("Usage:\n    InfoSec [--export] filename");
    }

    private static void createAndShowGUI(String[] args) {
        {
            int i = 0;
            while (i < args.length) {
                if (args[i].startsWith("--")) {
                    if (args[i].equals("--export")) {
                        i++;
                        if (i < args.length) {
                            new InfoSec(args[i], STARTACTION.EXPORT);
                            break;
                        } else {
                            System.err.println("Error: no filename after --export.");
                            printUsage();
                            System.exit(1);
                        }
                    }
                    System.err.println("Error: do not understand " + args[i]);
                    printUsage();
                    System.exit(1);
                }
                new InfoSec(args[i]);
                break;
            }
            i++;
        }
        try {
            UIManager.setLookAndFeel(GlobalProperties.getProperty("SIFA.LookAndFeel"));
        } catch (Exception e1) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e3) {
            }
        }
        if (singleInstance == null) {
            new InfoSec();
        }
        singleInstance.setBounds(100, 100, 900, 650);
        singleInstance.setVisible(true);
    }

    public void exit() throws ApplicationException {
        if (isDirty()) {
            if (!closeFile()) return;
        }
        if (!recentFiles.isEmpty()) {
            Properties rp = new Properties();
            int ii = 1;
            for (File file : recentFiles) {
                if (ii > 4) {
                    break;
                }
                try {
                    rp.setProperty("" + ii, file.getCanonicalPath());
                    ii++;
                } catch (IOException ioe) {
                }
            }
            try {
                FileOutputStream fos = new FileOutputStream(new File("recent.prop"));
                rp.store(fos, "");
                fos.close();
            } catch (IOException ioe) {
            }
        }
        System.exit(0);
    }

    public boolean closeFile() throws ApplicationException {
        if (isDirty()) {
            int result = JOptionPane.showConfirmDialog(this, "Would you like to save " + ((HierarchyNode) hierarchy.getRoot()).getComponentModel().getDisplayName() + "?", "Confirm Session Close", JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                saveSystemFile(false);
            }
            if (result == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }
        for (JInternalFrame frame : hierarchyNodeHashToInternalFrame.values()) {
            desktopPane.remove(frame);
        }
        desktopPane.removeAll();
        desktopPane.repaint();
        Hierarchy.staticClean();
        Schematic.staticClean();
        PortComponent.staticClean();
        FaultMode.staticClean();
        hierarchyNodeHashToInternalFrame.clear();
        hierarchyNodeHashToHierarchyNode.clear();
        autoSelected = null;
        hierarchy = null;
        tree.setModel(null);
        System.gc();
        newSchematic();
        currentlyOpenFile = null;
        setDirty(true);
        setDirty(false);
        return true;
    }

    public Hierarchy getHierarchy() {
        return hierarchy;
    }

    public void newSchematic() {
        HierarchyNode rootHierarchyNode = XmlEngine.createCluster(this, "Root");
        hierarchy = new Hierarchy(rootHierarchyNode);
        hierarchy.setRoot(rootHierarchyNode);
        rootHierarchyNode.setModel(hierarchy);
        rootHierarchyNode.getComponentModel().setNameChangeEventListener(this);
        if (tree == null) {
            tree = new DragOutJTree(hierarchy, this);
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        } else {
            tree.setModel(hierarchy);
        }
        setDirty(false);
    }

    public void showAboutBox() {
        String message = "SIFA: Secure Information Flow Analyser. " + "Version " + VERSION + ".\n" + "Modified 2010 Summer Project QUT.\n" + "Copyright (c) 2008 InfoSec Project UQ.\n\n" + "THIS SOFTWARE IS PROTOTYPE SOFTWARE AND NOT SUITABLE FOR USE IN PRODUCTION ENVIRONMENTS.\n\n" + "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,\n" + "INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A \n" + "PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL ANY \n" + "CONTRIBUTOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION \n" + "OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE \n" + "OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.";
        JOptionPane.showMessageDialog(this, message, "About " + WINDOWNAME, JOptionPane.PLAIN_MESSAGE, new ImageIcon(getClass().getClassLoader().getResource(SYSTEMICON)));
    }

    public void saveSystemFile(boolean useCurrentFile) throws ApplicationException {
        HierarchyNode hnode = (HierarchyNode) hierarchy.getRoot();
        File toSaveAs = null;
        if (!useCurrentFile) {
            JFileChooser newfc = getSaveFileChooser();
            if (currentlyOpenFile != null) {
                newfc.setSelectedFile(currentlyOpenFile);
            }
            int returnVal = newfc.showDialog(this, "Save As...");
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                toSaveAs = newfc.getSelectedFile();
            }
        } else {
            toSaveAs = currentlyOpenFile;
        }
        if (toSaveAs != null) {
            writeToFile(hnode, toSaveAs);
            currentlyOpenFile = toSaveAs;
            setDirty(false);
        }
    }

    public void repaintTree() {
        if (tree != null) tree.repaint();
    }

    private void writeToFile(HierarchyNode hnode, File file) throws ApplicationException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ByteArrayOutputStream errorMessage = new ByteArrayOutputStream();
        try {
            setStatus("Saving " + hnode);
            freeze(true);
            OutputStream fileData = new BufferedOutputStream(new FileOutputStream(file));
            int lastDot = file.getName().lastIndexOf('.');
            if (lastDot >= 0) {
                String extension = file.getName().substring(lastDot + 1);
                if (outputFilterCommands.containsKey(extension)) {
                    JOptionPane.showMessageDialog(this, "Warning: Some data may be lost converting to\n" + extension + " format.", "Export filter warning", JOptionPane.WARNING_MESSAGE);
                    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                    outputter.output(new Document(XMLVisitor.toXML(hnode)), buffer);
                    String command = outputFilterCommands.get(extension);
                    Process filter = Runtime.getRuntime().exec(command);
                    OutputStream processInput = new BufferedOutputStream(filter.getOutputStream());
                    InputStream processOutput = new BufferedInputStream(filter.getInputStream());
                    InputStream processError = new BufferedInputStream(filter.getErrorStream());
                    try {
                        copy(new ByteArrayInputStream(buffer.toByteArray()), processInput);
                        copy(processOutput, fileData);
                        copy(processError, errorMessage);
                    } catch (Exception e) {
                        filter.destroy();
                        throw new ApplicationException("Converter subprocess failed");
                    }
                    int exitCode = filter.waitFor();
                    if (exitCode != 0) {
                        String message = errorMessage.toString();
                        buffer.close();
                        throw new ApplicationException("Converter process returned exit code " + exitCode + "\n" + message);
                    }
                } else {
                    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                    ZipOutputStream zos = new ZipOutputStream(fileData);
                    zos.putNextEntry(new ZipEntry(file.getName()));
                    outputter.output(new Document(XMLVisitor.toXML(hnode)), zos);
                    zos.close();
                }
            } else {
                XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                ZipOutputStream zos = new ZipOutputStream(fileData);
                zos.putNextEntry(new ZipEntry(file.getName()));
                outputter.output(new Document(XMLVisitor.toXML(hnode)), zos);
                zos.close();
            }
            freeze(false);
            setStatus("Saved " + hnode + ".");
        } catch (Exception e) {
            freeze(false);
            if (e instanceof ApplicationException) throw (ApplicationException) e; else throw new ApplicationException("Unable to save file -- IO error.");
        }
    }

    public void saveFile(HierarchyNode hnode) throws ApplicationException {
        JFileChooser newfc = getSaveFileChooser();
        int returnVal = newfc.showDialog(this, "Save As...");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            writeToFile(hnode, newfc.getSelectedFile());
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    private void importComponentDirect(HierarchyNode parent, Document doc) throws ApplicationException {
        try {
            Element component = doc.getRootElement();
            if (component == null) throw new ApplicationException("Bad Component tag");
            Attribute component_name = component.getAttribute("name");
            if (component_name == null) throw new ApplicationException("Bad Component tag");
            Element schematicElement = component.getChild("Schematic");
            if (schematicElement == null) throw new ApplicationException("No schematic tag inside component");
            XmlEngine.doChildComponents(this, parent, schematicElement.getChildren("ChildComponent"), schematicElement.getChildren("Connection"), schematicElement.getChildren("Instantiation"), true);
            if (component.getChild("LocalFaultData") == null) throw new ApplicationException("No local fault data for component");
            XmlEngine.doLocalFaultData(parent, component.getChild("LocalFaultData"));
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            throw new ApplicationException(e.getMessage());
        }
    }

    public void importComponent(HierarchyNode parent) throws ApplicationException {
        JFileChooser fc = getOpenFileChooser();
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            freeze(true);
            File file = fc.getSelectedFile();
            Document doc = openDoc(file);
            String reason = "Unknown reason";
            if (doc != null) {
                importComponentDirect(parent, doc);
            }
            freeze(false);
            if (doc == null) {
                throw new ApplicationException("Import failed - " + reason);
            }
            setDirty(true);
        }
    }

    public void openFile() throws ApplicationException {
        openFile(null);
    }

    public void openFile(String filename) throws ApplicationException {
        int returnVal;
        JFileChooser fc = getOpenFileChooser();
        if (filename == null) {
            returnVal = fc.showOpenDialog(this);
        } else {
            fc.setSelectedFile(new File(filename));
            returnVal = JFileChooser.APPROVE_OPTION;
        }
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            if (!closeFile()) return;
            freeze(true);
            final File file = fc.getSelectedFile();
            if (file.canRead()) {
                int ii = 0;
                boolean pushit = true;
                for (File recentFile : recentFiles) {
                    if (ii++ >= 4) {
                        break;
                    }
                    if (file.equals(recentFile)) {
                        pushit = false;
                        break;
                    }
                }
                if (pushit) {
                    try {
                        String desc = abbreviateFileName(file.getCanonicalPath());
                        recentFileMenuItems.addFirst(new JMenuItem(desc));
                        recentFiles.addFirst(file);
                        constructFileMenu();
                    } catch (IOException ioe) {
                    }
                }
            }
            try {
                Document doc = openDoc(file);
                String reason = "Unknown reason";
                if (doc != null) {
                    final Document finaldoc = doc;
                    Thread doModelPopulation = new Thread() {

                        public void run() {
                            try {
                                desktopPane.removeAll();
                                Element component = finaldoc.getRootElement();
                                Attribute component_name = component.getAttribute("name");
                                if (component == null || component_name == null) throw new Exception("Bad Component tag");
                                HierarchyNode rootHierarchyNode = XmlEngine.createCluster(InfoSec.this, component_name.getValue());
                                hierarchy = new Hierarchy(rootHierarchyNode);
                                hierarchy.setRoot(rootHierarchyNode);
                                rootHierarchyNode.setModel(hierarchy);
                                rootHierarchyNode.getComponentModel().setNameChangeEventListener(InfoSec.this);
                                tree.setModel(hierarchy);
                                Element schematicElement = component.getChild("Schematic");
                                if (schematicElement == null) throw new Exception("No schematic tag inside component");
                                XmlEngine.doChildComponents(InfoSec.this, rootHierarchyNode, schematicElement.getChildren("ChildComponent"), schematicElement.getChildren("Connection"), schematicElement.getChildren("Instantiation"), false);
                                if (component.getChild("LocalFaultData") == null) throw new Exception("No local fault data for component");
                                XmlEngine.doLocalFaultData(rootHierarchyNode, component.getChild("LocalFaultData"));
                                setDirty(false);
                                hierarchyNodeHashToInternalFrame.clear();
                                hierarchyNodeHashToHierarchyNode.clear();
                                freeze(false);
                                setStatus("Finished loading " + file.getName());
                            } catch (Exception e2) {
                                freeze(false);
                                new ApplicationException("Open failed - XML not valid\n" + e2.getMessage()).handler();
                            }
                        }
                    };
                    doModelPopulation.start();
                    currentlyOpenFile = file;
                } else {
                    setStatus("Open failed - " + reason);
                    freeze(false);
                    throw new ApplicationException("Open failed - " + reason);
                }
            } catch (ApplicationException e) {
                freeze(false);
                throw e;
            }
        }
    }

    public static void errorHandler(ApplicationException e) {
        InfoSec.getInstance().freeze(false);
        JOptionPane.showMessageDialog(getInstance(), e.getMessage(), "SIFA Problem", JOptionPane.ERROR_MESSAGE, new ImageIcon(getInstance().getClass().getClassLoader().getResource(SYSTEMICON)));
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI(args);
            }
        });
    }

    public void graphChanged(GraphModelEvent e) {
        setDirty(true);
        repaintTree();
        Schematic source = (Schematic) e.getChange().getSource();
        HierarchyNode hnode = Hierarchy.lookupNode(source);
        hnode.getComponentModel().adjustColour();
        Object[] insertedObjects = e.getChange().getInserted();
        if (insertedObjects != null) {
            for (int i = 0; i < insertedObjects.length; i++) {
                if (insertedObjects[i] instanceof StandardComponent) {
                    StandardComponent theNewComponent = (StandardComponent) insertedObjects[i];
                    Schematic componentSchematic;
                    if (theNewComponent.isOrphan()) {
                        HierarchyNode parentnode = Hierarchy.lookupNode(source);
                        componentSchematic = new Schematic();
                        HierarchyNode childnode;
                        if (theNewComponent.getHierarchyNode() == null) {
                            childnode = new HierarchyNode(componentSchematic, theNewComponent);
                        } else {
                            childnode = theNewComponent.getHierarchyNode();
                        }
                        parentnode.add(childnode);
                        theNewComponent.setHierarchyNode(childnode);
                    } else {
                        componentSchematic = (Schematic) theNewComponent.getHierarchyNode().getSchematicModel();
                    }
                    componentSchematic.addGraphModelListener(this);
                    theNewComponent.graphChanged(e);
                    continue;
                }
                if (insertedObjects[i] instanceof InstanceComponent) {
                    ((InstanceComponent) insertedObjects[i]).setHierarchyNode(hnode);
                }
            }
        }
        Object[] removedObjects = e.getChange().getRemoved();
        if (removedObjects != null) {
            for (int i = 0; i < removedObjects.length; i++) {
                if (removedObjects[i] instanceof StandardComponent) {
                    StandardComponent theRemovedComponent = (StandardComponent) removedObjects[i];
                    String key = "" + System.identityHashCode(theRemovedComponent.getHierarchyNode());
                    JInternalFrame iframe = (JInternalFrame) hierarchyNodeHashToInternalFrame.get(key);
                    if (iframe != null) {
                        desktopPane.remove(iframe);
                        hierarchyNodeHashToInternalFrame.remove(key);
                        hierarchyNodeHashToHierarchyNode.remove(key);
                        desktopPane.repaint();
                    }
                    HierarchyNode parentnode = (HierarchyNode) theRemovedComponent.getHierarchyNode().getParent();
                    HierarchyNode childnode = theRemovedComponent.getHierarchyNode();
                    if (parentnode != null && childnode != null) {
                        Hierarchy.deregisterSchematic(childnode.getSchematicModel());
                        parentnode.remove(childnode);
                    }
                }
            }
        }
    }

    public void nameChanged(NameChangeEvent nce) {
        Object eventSource = nce.getSource();
        if (!(eventSource instanceof StandardComponent)) return;
        setDirty(true);
        StandardComponent sc_invalidated = (StandardComponent) eventSource;
        String key = "" + System.identityHashCode(sc_invalidated.getHierarchyNode());
        JInternalFrame iframe = (JInternalFrame) hierarchyNodeHashToInternalFrame.get(key);
        if (iframe != null) {
            iframe.setTitle(nce.getNewName());
        }
        hierarchy.reload(sc_invalidated.getHierarchyNode());
        for (FaultMode fm : new FaultModeSet(sc_invalidated.getMatrix().getIdentity())) {
            FaultMode modeWithAssociation = new FaultMode(nce.getNewName(), fm.getAssociatedInstance(), fm.getAbbreviation(), fm.getDescription(), fm.isNormal(), fm.getProbability());
            sc_invalidated.replaceFaultMode(fm, modeWithAssociation);
        }
    }

    public void frameOpen() {
        frameOpen(getCurrentlySelectedHierarchyNode());
    }

    public void frameOpen(HierarchyNode source) {
        if (source == autoSelected) {
            return;
        }
        autoSelected = null;
        String key = "" + System.identityHashCode(source);
        JInternalFrame iframe = (JInternalFrame) hierarchyNodeHashToInternalFrame.get(key);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (iframe == null) {
            Schematic componentSchematic = source.getSchematicModel();
            SchematicEditor componentGraph = new SchematicEditor(componentSchematic);
            componentGraph.addDropEscalationListener(this);
            source.setChildrenRepaintListener(componentGraph);
            componentSchematic.addGraphModelListener(source.getComponentModel());
            JTable componentTable = new JTable(source.getComponentModel().getTableModel());
            componentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            FaultSetEditor fs_editor = new FaultSetEditor(source.getComponentModel());
            componentTable.setDefaultEditor(FaultModeSet.class, fs_editor);
            componentTable.setDefaultRenderer(FaultModeSet.class, new FaultSetRenderer());
            FaultModesEditor faultModesEditor = new FaultModesEditor(source.getComponentModel());
            JSplitPane splitPaneLower = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(componentTable), faultModesEditor);
            JInternalFrame i = new JInternalFrame(source.toString(), true, true, true, true);
            i.addInternalFrameListener(this);
            JTabbedPane miniTabbedPane = new JTabbedPane();
            miniTabbedPane.addTab("Schematic", componentGraph);
            miniTabbedPane.addTab("Modes", splitPaneLower);
            miniTabbedPane.setOpaque(true);
            i.setContentPane(miniTabbedPane);
            i.setFrameIcon(new ImageIcon(getClass().getClassLoader().getResource(SMSYSTEMICON)));
            desktopPane.add(i);
            JMenuBar menubar = new JMenuBar();
            JMenu cmenu = new JMenu("Component");
            cmenu.setMnemonic(KeyEvent.VK_C);
            JMenuItem importItem = new JMenuItem("Import");
            importItem.setIcon(new ImageIcon(getClass().getClassLoader().getResource("img/fileopen.gif")));
            importItem.setMnemonic(KeyEvent.VK_I);
            importItem.setActionCommand(key);
            importItem.addActionListener(new ImportAction(this));
            cmenu.add(importItem);
            JMenuItem saveItem = new JMenuItem("Save As...");
            saveItem.setMnemonic(KeyEvent.VK_A);
            saveItem.setIcon(new ImageIcon(getClass().getClassLoader().getResource("img/save.png")));
            saveItem.setActionCommand(key);
            saveItem.addActionListener(new SaveAction(this));
            cmenu.add(saveItem);
            JMenuItem renameItem = new JMenuItem("Rename");
            renameItem.setMnemonic(KeyEvent.VK_R);
            renameItem.setActionCommand(key);
            renameItem.addActionListener(new RenameAction(this));
            cmenu.addSeparator();
            cmenu.add(renameItem);
            menubar.add(cmenu);
            JMenu analysisMenu = new JMenu("Analysis");
            analysisMenu.setMnemonic(KeyEvent.VK_A);
            JMenuItem cutsetItem = new JMenuItem("Topological minimal cutsets");
            cutsetItem.setMnemonic(KeyEvent.VK_C);
            cutsetItem.setActionCommand(key);
            cutsetItem.addActionListener(new CreateMinimalCutSetAction(this));
            analysisMenu.add(cutsetItem);
            JMenuItem flowItem = new JMenuItem("Topological flow");
            flowItem.setMnemonic(KeyEvent.VK_F);
            flowItem.setActionCommand(key);
            flowItem.addActionListener(new FlowAnalysisAction(this));
            analysisMenu.add(flowItem);
            menubar.add(analysisMenu);
            JMenu viewMenu = new JMenu("View");
            viewMenu.setMnemonic(KeyEvent.VK_V);
            JMenuItem sortItem = new JMenuItem("Sort matrix");
            sortItem.setMnemonic(KeyEvent.VK_S);
            sortItem.setActionCommand(key);
            sortItem.addActionListener(new SortMatrixAction(this));
            viewMenu.add(sortItem);
            menubar.add(viewMenu);
            i.setJMenuBar(menubar);
            i.pack();
            if (desktopPane.getHeight() < i.getHeight() || desktopPane.getWidth() < i.getWidth()) {
                i.setSize(desktopPane.getSize());
            }
            i.show();
            splitPaneLower.setDividerLocation(0.5);
            hierarchyNodeHashToInternalFrame.put(key, i);
            hierarchyNodeHashToHierarchyNode.put(key, source);
        }
        JInternalFrame i = (JInternalFrame) hierarchyNodeHashToInternalFrame.get(key);
        i.toFront();
        try {
            if (i.isIcon()) i.setIcon(false);
        } catch (Exception exception) {
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void internalFrameActivated(InternalFrameEvent e) {
        JInternalFrame frame = e.getInternalFrame();
        String hash = null;
        for (Map.Entry<String, JInternalFrame> entry : hierarchyNodeHashToInternalFrame.entrySet()) {
            if (entry.getValue() == frame) {
                hash = entry.getKey();
            }
        }
        if (hash != null) {
            for (int jj = 0; jj < tree.getRowCount(); jj++) {
                HierarchyNode source = (HierarchyNode) tree.getPathForRow(jj).getLastPathComponent();
                if (("" + System.identityHashCode(source)).equals(hash)) {
                    tree.setSelectionRow(jj);
                    autoSelected = source;
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void internalFrameClosed(InternalFrameEvent e) {
        Iterator<Map.Entry<String, JInternalFrame>> ii = hierarchyNodeHashToInternalFrame.entrySet().iterator();
        while (ii.hasNext()) {
            Map.Entry<String, JInternalFrame> next = ii.next();
            if (next.getValue() == e.getInternalFrame()) {
                ii.remove();
                HierarchyNode hn_to_go = hierarchyNodeHashToHierarchyNode.remove(next.getKey());
                if (hn_to_go != null) {
                    hn_to_go.removeChildrenRepaintListener();
                    Enumeration ee = hn_to_go.children();
                    while (ee.hasMoreElements()) {
                        HierarchyNode child = (HierarchyNode) ee.nextElement();
                        child.getComponentModel().removeRepaintListener();
                    }
                }
            }
        }
    }

    public void internalFrameClosing(InternalFrameEvent e) {
        tree.clearSelection();
    }

    public void internalFrameDeactivated(InternalFrameEvent e) {
    }

    public void internalFrameDeiconified(InternalFrameEvent e) {
    }

    public void internalFrameIconified(InternalFrameEvent e) {
    }

    public void internalFrameOpened(InternalFrameEvent e) {
    }

    public void importTransferable(Transferable tr, HierarchyNode openNode) throws Exception {
        String xml = (String) tr.getTransferData(DataFlavor.stringFlavor);
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(xml));
        importComponentDirect(openNode, doc);
    }

    public void drop(DropTargetDropEvent e, Schematic src) {
        e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        for (HierarchyNode openNode : hierarchyNodeHashToHierarchyNode.values()) {
            if (openNode.getSchematicModel() == src) {
                try {
                    importTransferable(e.getTransferable(), openNode);
                } catch (Exception e2) {
                    JOptionPane.showMessageDialog(this, "The dropped text must be in SIFA's XML format", "Drop failed", JOptionPane.ERROR_MESSAGE);
                    e.rejectDrop();
                }
                e.dropComplete(true);
                return;
            }
        }
        e.dropComplete(true);
    }
}

class ExtensionFilter extends javax.swing.filechooser.FileFilter {

    private final TreeSet<String> exts = new TreeSet<String>();

    private final String desc;

    public ExtensionFilter(String desc) {
        this.desc = desc;
    }

    public void addExtension(String ext) {
        exts.add(ext);
    }

    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return true;
        }
        int idx = pathname.getName().lastIndexOf('.');
        if (idx >= 0) {
            return exts.contains(pathname.getName().substring(idx + 1).toLowerCase());
        }
        return false;
    }

    @Override
    public String getDescription() {
        return desc;
    }
}
