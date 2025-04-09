package com.endfocus.projectbuilder;

import java.util.Vector;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.filechooser.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.help.*;
import gnu.regexp.*;
import org.w3c.dom.*;
import org.apache.xerces.parsers.*;
import org.apache.xerces.dom.*;
import org.apache.xml.serialize.*;
import org.xml.sax.InputSource;
import com.endfocus.layout.*;
import com.endfocus.ui.*;
import com.endfocus.utilities.*;
import com.endfocus.projectbuilder.members.*;

public class ProjectFrame implements ActionListener, WindowListener, TreeSelectionListener {

    private static long startTime = System.currentTimeMillis() + 60 * 60 * 1000;

    private static Dimension screenResolution;

    private static Vector projects = null;

    private static int x, y;

    private static JFileChooser fileChooser;

    private static Vector projectNames;

    private static final int maxHistory = 8;

    private static int session = 1;

    static {
        fileChooser = new JFileChooser();
        projectNames = new Vector();
        projects = new Vector();
        screenResolution = Toolkit.getDefaultToolkit().getScreenSize();
        x = 50;
        y = 50;
        try {
            String filename = System.getProperty("install.root") + System.getProperty("file.separator") + "scripts" + System.getProperty("file.separator") + "sp.history";
            Reader aReader = new FileReader(filename);
            if (aReader != null) {
                DOMParser parser = new DOMParser();
                parser.parse(new InputSource(aReader));
                Document doc = parser.getDocument();
                Element rt = (Element) doc.getDocumentElement();
                loadHistory(rt);
                aReader.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected static void loadHistory(Element root) {
        try {
            int m = 0;
            Element hElement = (Element) root.getElementsByTagName("History").item(0);
            if (hElement != null) {
                NodeList nList = hElement.getElementsByTagName("Projects");
                for (int n = 0; n < nList.getLength(); n++) {
                    Element aElement = (Element) nList.item(n);
                    NodeList aList = aElement.getElementsByTagName("*");
                    for (int x = 0; x < aList.getLength(); x++) {
                        projectNames.addElement(aList.item(x).getFirstChild().getNodeValue());
                        m++;
                        if (m == maxHistory) return;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected static void addProject() {
        projects.addElement(new ProjectFrame());
    }

    protected static void addProject(final String filename) {
        projects.addElement(new ProjectFrame(filename));
    }

    protected static void addProject(ProjectFrame aProject) {
        projects.addElement(aProject);
    }

    protected static void removeProject(ProjectFrame aProject) {
        projects.removeElement(aProject);
        if (projects.size() == 0) {
            if (Main.mainStarter) System.exit(0);
        }
    }

    protected static void removeAllProjects() {
        Vector p = (Vector) projects.clone();
        for (Enumeration e = p.elements(); e.hasMoreElements(); ) {
            ((ProjectFrame) e.nextElement()).close();
        }
        if (Main.mainStarter) System.exit(0);
    }

    private String projectName;

    private JFrame mainWindow;

    private JTree projectTree;

    private JMenuItem newMenuItem, openMenuItem, saveMenuItem, saveAsMenuItem, closeMenuItem, genMenuItem, zipMenuItem, exitMenuItem, buildP;

    private JButton newButton, saveButton;

    private JButton cutButton;

    private JButton copyButton;

    private JButton pasteButton;

    private JLabel status;

    private JPopupMenu popupMenu;

    private JMenuItem pm1, pm2, pm3, pm4, pm5, pm6, pm7;

    private JMenu projectMenu;

    private SwingMenu taskMenu;

    private TreeModel treeModel;

    private JPanel mainPanel;

    private boolean taskAdded;

    private JSplitPane mainSplitPane;

    private SessionPanel sessionPanel;

    private ImportPanel importDetail;

    private AddFolderPanel addFolderPanel;

    private RenameFolderPanel renameFolderPanel;

    private ProjectDetailPanel projectDetail;

    private EnvPanel env;

    private BuildPanel build;

    private MemberPanel member;

    private PluginPanel pluginPanel;

    private String projectFileName;

    private ProjectHolder root = null;

    private boolean working = false;

    private int currentSession = session++;

    ActionListener alCut = new ActionListener() {

        public void actionPerformed(ActionEvent evt) {
        }
    };

    ActionListener alCopy = new ActionListener() {

        public void actionPerformed(ActionEvent evt) {
        }
    };

    ActionListener alPaste = new ActionListener() {

        public void actionPerformed(ActionEvent evt) {
        }
    };

    public ProjectFrame(final String filename) {
        this();
        loadProject(filename);
    }

    public ProjectFrame() {
        treeModel = new DefaultTreeModel(new DefaultMutableTreeNode("Empty Project"), false);
        setupGUI();
        mainWindow.show();
        Main.mainHB.enableHelpKey(mainWindow.getRootPane(), "top", null);
        saveButton.setEnabled(false);
    }

    public void setActionMenu(boolean mem) {
        if (mem) {
            taskMenu.setEnabled("Members", "Add", true);
            taskMenu.setEnabled("Members", "Remove", false);
        } else {
            taskMenu.setEnabled("Members", "Add", false);
            taskMenu.setEnabled("Members", "Remove", true);
        }
    }

    public ProjectFrame(ProjectHolder aProjectHolder) {
        treeModel = new ProjectTreeModel(aProjectHolder);
        setupGUI();
        mainWindow.pack();
        mainWindow.setVisible(true);
    }

    private void setupGUI() {
        JComponent contentPane;
        JPanel content;
        taskAdded = false;
        projectDetail = new ProjectDetailPanel(this);
        importDetail = new ImportPanel(this);
        addFolderPanel = new AddFolderPanel(this);
        renameFolderPanel = new RenameFolderPanel(this);
        env = new EnvPanel(this);
        build = new BuildPanel(this, env);
        member = new MemberPanel(this);
        pluginPanel = new PluginPanel(this);
        sessionPanel = new SessionPanel(this);
        mainWindow = new JFrame();
        mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainWindow.setBounds(x, y, 600, 400);
        mainWindow.setTitle("ProjectBuilder");
        mainWindow.setIconImage(Utilities.getImage("com/endfocus/projectbuilder/images/icon.gif"));
        mainWindow.setJMenuBar(setupMenus());
        mainWindow.addWindowListener(this);
        taskMenu = new SwingMenu();
        mainWindow.getContentPane().add(setupToolBar(), BorderLayout.NORTH);
        mainWindow.getContentPane().add(taskMenu, BorderLayout.WEST);
        UIManager.put("Tree.hash", Color.black);
        UIManager.put("Tree.expandedIcon", Utilities.getIcon("com/endfocus/projectbuilder/images/treeminus.gif"));
        UIManager.put("Tree.collapsedIcon", Utilities.getIcon("com/endfocus/projectbuilder/images/treeplus.gif"));
        projectTree = new JTree(treeModel);
        projectTree.setUI(new ProjectTreeUI());
        ToolTipManager.sharedInstance().registerComponent(projectTree);
        projectTree.setShowsRootHandles(true);
        projectTree.setOpaque(true);
        projectTree.setScrollsOnExpand(false);
        projectTree.addTreeSelectionListener(this);
        projectTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        projectTree.setRowHeight(18);
        JScrollPane aScrollPane = new JScrollPane(projectTree);
        aScrollPane.setMinimumSize(new Dimension(50, 50));
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        mainSplitPane.setLeftComponent(aScrollPane);
        mainSplitPane.setRightComponent(projectDetail);
        MouseListener popupListener = new PopupListener();
        projectTree.addMouseListener(popupListener);
        aScrollPane.addMouseListener(popupListener);
        mainWindow.getContentPane().add(mainSplitPane, BorderLayout.CENTER);
        status = new JLabel(" ");
        status.setBorder(new BevelBorder(BevelBorder.LOWERED));
        status.setForeground(Color.black);
        mainWindow.getContentPane().add(status, BorderLayout.SOUTH);
        x += 20;
        y += 20;
        if (x > screenResolution.width - 650 || y > screenResolution.height - 450) {
            x = 50;
            y = 50;
        }
    }

    public void showStatus(String statusText) {
        status.setText(statusText);
    }

    public JFrame getProjectFrame() {
        return mainWindow;
    }

    private JMenuBar setupMenus() {
        JMenuBar aMenuBar;
        JMenu aMenu;
        JMenuItem aMenuItem;
        aMenuBar = new JMenuBar();
        projectMenu = new JMenu("Project");
        projectMenu.setMnemonic('P');
        aMenuBar.add(projectMenu);
        newMenuItem = createMenuItem("New", "New", 'N', true, this);
        openMenuItem = createMenuItem("Open", "Open", 'O', true, this);
        saveMenuItem = createMenuItem("Save", "Save", 'S', false, this);
        saveAsMenuItem = createMenuItem("Save As...", "SaveAs", 'A', false, this);
        closeMenuItem = createMenuItem("Close", "Close", 'C', false, this);
        zipMenuItem = createMenuItem("Zip", "Zip", 'Z', true, this);
        exitMenuItem = createMenuItem("Exit", "Exit", 'x', true, this);
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.Event.CTRL_MASK));
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.Event.CTRL_MASK));
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.Event.CTRL_MASK));
        buildProjectMenu();
        aMenu = new JMenu("Help");
        aMenu.setMnemonic('H');
        aMenu.add(createMenuItem("Contents", "Content", 'C', true, new CSH.DisplayHelpFromSource(com.endfocus.projectbuilder.Main.mainHB)));
        aMenu.add(createMenuItem("About", "About", 'A', true, this));
        aMenuBar.add(aMenu);
        popupMenu = new JPopupMenu();
        popupMenu.add(buildP = createMenuItem("Build Project", "BuildProject", 'B', true, this));
        popupMenu.add(pm7 = createMenuItem("Build & Run", "BuildAndRun", 'B', true, this));
        popupMenu.add(pm1 = createMenuItem("Build All", "BuildAll", 'B', true, this));
        popupMenu.add(pm2 = createMenuItem("Build Dirty", "BuildDirty", 'B', true, this));
        popupMenu.addSeparator();
        popupMenu.add(pm3 = createMenuItem("Rename Project", "RenameFolder", 'n', true, this));
        popupMenu.add(pm4 = createMenuItem("New Sub Project", "AddFolder", 'A', true, this));
        popupMenu.add(pm5 = createMenuItem("Delete Sub Project", "RemoveFolder", 'R', true, this));
        popupMenu.addSeparator();
        popupMenu.add(pm6 = createMenuItem("Export Sub Project", "ExportProject", 'x', true, this));
        popupMenu.addSeparator();
        popupMenu.add(createMenuItem("Help", "Content", 'H', true, new CSH.DisplayHelpFromSource(com.endfocus.projectbuilder.Main.mainHB)));
        return aMenuBar;
    }

    protected void buildProjectMenu() {
        if (projectMenu.getItemCount() > 0) projectMenu.removeAll();
        projectMenu.add(newMenuItem);
        projectMenu.add(openMenuItem);
        projectMenu.add(saveMenuItem);
        projectMenu.add(saveAsMenuItem);
        projectMenu.add(closeMenuItem);
        projectMenu.add(zipMenuItem);
        projectMenu.addSeparator();
        for (int n = 0; n < projectNames.size(); n++) {
            String path = (String) projectNames.elementAt(n);
            String filename = path.substring(path.lastIndexOf(System.getProperty("file.separator")) + 1);
            JMenuItem mi = createMenuItem(filename, "History", ' ', true, this);
            mi.putClientProperty("FilePath", path);
            mi.setToolTipText(path);
            projectMenu.add(mi);
        }
        projectMenu.addSeparator();
        projectMenu.add(exitMenuItem);
    }

    private JToolBar setupToolBar() {
        JButton aButton;
        JToolBar aToolBar = new JToolBar();
        JLabel aLabel;
        aToolBar.setBounds(0, 0, 440, 48);
        aToolBar.setLayout(new FlowLayout(FlowLayout.LEFT));
        aToolBar.setFloatable(false);
        newButton = new JButton(Utilities.getIcon("com/endfocus/projectbuilder/images/new.gif"));
        newButton.setToolTipText("New Project");
        newButton.setBorder(null);
        newButton.setFocusPainted(false);
        newButton.setActionCommand("New");
        newButton.addActionListener(this);
        newButton.setBorder(new EmptyBorder(1, 1, 1, 1));
        aToolBar.add(newButton);
        aButton = new JButton(Utilities.getIcon("com/endfocus/projectbuilder/images/open.gif"));
        aButton.setToolTipText("Open Project");
        aButton.setBorder(null);
        aButton.setFocusPainted(false);
        aButton.setActionCommand("Open");
        aButton.addActionListener(this);
        aButton.setBorder(new EmptyBorder(1, 1, 1, 1));
        aToolBar.add(aButton);
        saveButton = new JButton(Utilities.getIcon("com/endfocus/projectbuilder/images/save.gif"));
        saveButton.setToolTipText("Save Project");
        saveButton.setBorder(null);
        saveButton.setFocusPainted(false);
        saveButton.setActionCommand("SaveAs");
        saveButton.addActionListener(this);
        saveButton.setEnabled(true);
        saveButton.setBorder(new EmptyBorder(1, 1, 1, 1));
        aToolBar.add(saveButton);
        return aToolBar;
    }

    public JMenuItem createMenuItem(String aLabel, String aCommand, char aMnemonic, boolean aState, ActionListener aListener) {
        JMenuItem aMenuItem = new JMenuItem(aLabel);
        aMenuItem.addActionListener(aListener);
        aMenuItem.setActionCommand(aCommand);
        aMenuItem.setEnabled(aState);
        aMenuItem.setMnemonic(aMnemonic);
        return aMenuItem;
    }

    private void addTask() {
        if (taskAdded) return;
        taskMenu.addTab("Members", Color.gray);
        taskMenu.addButton("Members", "Add", Utilities.getIcon("com/endfocus/projectbuilder/images/addmembers.gif"), "AddMembers", "Add Members");
        taskMenu.addButton("Members", "Remove", Utilities.getIcon("com/endfocus/projectbuilder/images/removemembers.gif"), "RemoveMembers", "Remove Members");
        taskMenu.addButton("Members", "Add", Utilities.getIcon("com/endfocus/projectbuilder/images/folderin.gif"), "AddFolder", "Add Sub Project");
        taskMenu.addButton("Members", "Remove", Utilities.getIcon("com/endfocus/projectbuilder/images/folderout.gif"), "RemoveFolder", "Remove Sub Project");
        taskMenu.addButton("Members", "Rename", Utilities.getIcon("com/endfocus/projectbuilder/images/folderrename.gif"), "RenameFolder", "Rename Sub Project");
        taskMenu.addButton("Members", "Import", Utilities.getIcon("com/endfocus/projectbuilder/images/documentin.gif"), "Import", "Import Files");
        taskMenu.addTab("Build", Color.gray);
        taskMenu.addButton("Build", "Build", Utilities.getIcon("com/endfocus/projectbuilder/images/hammer.gif"), "Build", "Build Project");
        taskMenu.addButton("Build", "Config", Utilities.getIcon("com/endfocus/projectbuilder/images/bulb.gif"), "BuildOptions", "Build Options");
        taskMenu.addTab("Project", Color.gray);
        taskMenu.addButton("Project", "Detail", Utilities.getIcon("com/endfocus/projectbuilder/images/inform.gif"), "ProjectDetail", "Project Detail");
        taskMenu.addButton("Project", "Import", Utilities.getIcon("com/endfocus/projectbuilder/images/datastore.gif"), "ImportProject", "Import Project");
        taskMenu.addButton("Project", "Export", Utilities.getIcon("com/endfocus/projectbuilder/images/dataextract.gif"), "ExportProject", "Export Project");
        taskMenu.addTab("Options", Color.gray);
        taskMenu.addButton("Options", "Plugins", Utilities.getIcon("com/endfocus/projectbuilder/images/plug.gif"), "ToolsPlugins", "Tools Plugins");
        taskMenu.addButton("Options", "Session", Utilities.getIcon("com/endfocus/projectbuilder/images/key.gif"), "SessionId", "Session Id");
        taskMenu.addActionListener(this);
        setActionMenu(false);
        taskMenu.invalidate();
        taskMenu.validate();
        taskMenu.repaint();
    }

    protected static void AddAndSaveHistory(String name) {
        try {
            for (int n = 0; n < projectNames.size(); n++) {
                String aName = (String) projectNames.elementAt(n);
                if (aName.compareTo(name) == 0) return;
            }
            projectNames.insertElementAt(name, 0);
            if (projectNames.size() > maxHistory) {
                projectNames.removeElement(projectNames.lastElement());
            }
            String filename = null;
            String installRoot = System.getProperty("install.root");
            if (installRoot != null) {
                filename = installRoot + System.getProperty("file.separator") + "scripts" + System.getProperty("file.separator") + "sp.history";
            }
            if (filename != null) {
                Document doc = new DocumentImpl();
                Element root = (Element) doc.createElement("ROOT");
                doc.appendChild(root);
                Element rt = (Element) doc.createElement("History");
                root.appendChild(rt);
                for (int n = 0; n < projectNames.size(); n++) {
                    String aName = (String) projectNames.elementAt(n);
                    Element el2 = (Element) doc.createElement("Projects");
                    el2.appendChild(doc.createTextNode(aName));
                    rt.appendChild(el2);
                }
                FileWriter fw = new FileWriter(filename);
                XMLSerializer xmlWriter = new XMLSerializer(new OutputFormat());
                xmlWriter.setOutputCharStream(fw);
                xmlWriter.serialize(doc);
                fw.close();
                for (Enumeration e = projects.elements(); e.hasMoreElements(); ) {
                    ((ProjectFrame) e.nextElement()).buildProjectMenu();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean setSessionId(int newId) {
        Vector p = (Vector) projects.clone();
        for (Enumeration e = p.elements(); e.hasMoreElements(); ) {
            int id = ((ProjectFrame) e.nextElement()).getSessionId();
            if (id == newId) {
                showStatus("Session Id is already in use by another project");
                return false;
            }
        }
        currentSession = newId;
        showStatus("Session ID set to : " + System.getProperty("user.name") + ":" + currentSession);
        return true;
    }

    public int getSessionId() {
        return currentSession;
    }

    public JFileChooser getFileChooser(String title) {
        fileChooser.setDialogTitle(title);
        return fileChooser;
    }

    public ProjectHolder getRoot() {
        return root;
    }

    public void showMembers() {
        if (mainSplitPane.getRightComponent() != member) {
            mainSplitPane.setRightComponent(member);
            mainSplitPane.invalidate();
            mainSplitPane.validate();
            mainSplitPane.repaint();
        }
    }

    public void showProject(ProjectHolder aHolder) {
        mainSplitPane.setRightComponent(member);
        member.setProject(aHolder);
        mainSplitPane.invalidate();
        mainSplitPane.validate();
        mainSplitPane.repaint();
        ((ProjectTreeModel) treeModel).treeChanged();
        try {
            projectTree.scrollPathToVisible(projectTree.getSelectionPath().pathByAddingChild(aHolder));
        } catch (Exception ex) {
        }
    }

    public void setPath(String name, String aPath) {
        File aFile = new File(aPath);
        if (aFile != null && aFile.isDirectory()) {
            try {
                root = new ProjectHolder();
                root.makeRootProject(aPath);
                if (name.length() == 0) root.setName(aFile.getCanonicalPath()); else root.setName(name);
                treeModel = new ProjectTreeModel(root);
                projectTree.setModel(treeModel);
                projectTree.setRootVisible(true);
                projectTree.setSelectionRow(0);
                ProjectTreeCellRenderer aRenderer = new ProjectTreeCellRenderer();
                projectTree.setCellRenderer(aRenderer);
                projectTree.invalidate();
                projectTree.validate();
                projectTree.repaint();
                addTask();
                saveAsMenuItem.setEnabled(true);
                closeMenuItem.setEnabled(true);
                saveButton.setEnabled(true);
            } catch (Exception ex) {
            }
        }
    }

    public void openEditor(String filePath) {
        pluginPanel.openEditorPath(filePath);
    }

    public void openEditorForError(String error) {
        pluginPanel.openEditorForError(error);
    }

    public void refreshTree() {
        ((ProjectTreeModel) treeModel).treeChanged();
        projectTree.setSelectionRow(0);
        mainSplitPane.setDividerLocation(0.25);
    }

    public void importTree(final ProjectHolder aHolder, final String include, final String exclude, final boolean subdir, final boolean empty) {
        final RE includeFilter, excludeFilter;
        RE aFilter;
        working = true;
        projectTree.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        aFilter = null;
        if (include.length() > 0) {
            try {
                aFilter = new RE(include);
            } catch (Exception ex) {
                aFilter = null;
            }
        }
        includeFilter = aFilter;
        aFilter = null;
        if (exclude.length() > 0) {
            try {
                aFilter = new RE(exclude);
            } catch (Exception ex) {
                aFilter = null;
            }
        }
        excludeFilter = aFilter;
        Thread importThread = new Thread(new Runnable() {

            public void run() {
                buildTree(aHolder, includeFilter, excludeFilter, subdir, empty);
                showStatus(" ");
                projectTree.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                working = false;
                refreshTree();
            }
        });
        importThread.start();
    }

    public void buildTree(ProjectHolder aHolder, RE includeFilter, RE excludeFilter, boolean subdir, boolean empty) {
        File aFile, newFile;
        try {
            aFile = new File(aHolder.getPath());
            showStatus("Scanning: " + aFile.getCanonicalPath());
            String filenames[] = aFile.list();
            for (int n = 0; n < filenames.length; n++) {
                newFile = new File(aFile.getCanonicalPath() + File.separator + filenames[n]);
                if (newFile != null) {
                    if (newFile.isDirectory()) {
                        System.out.println("Processing dir: " + newFile);
                        if (!subdir) continue;
                        System.out.println("Looking for Member: " + File.separator + filenames[n]);
                        ProjectHolder newHolder = (ProjectHolder) aHolder.getMember(filenames[n]);
                        if (newHolder != null) {
                            buildTree(newHolder, includeFilter, excludeFilter, subdir, empty);
                        } else {
                            String newPath = aFile.getCanonicalPath() + File.separator + filenames[n];
                            System.out.println("Creating project: " + newPath);
                            newHolder = new ProjectHolder();
                            newHolder.setPath(newPath + File.separator);
                            newHolder.setName(filenames[n]);
                            buildTree(newHolder, includeFilter, excludeFilter, subdir, empty);
                            if (empty || newHolder.getMembers().size() > 0) aHolder.addMember(newHolder);
                        }
                        continue;
                    }
                    if (aHolder.getMember(filenames[n]) != null) continue;
                    if (includeFilter == null || includeFilter.isMatch(filenames[n])) if (excludeFilter == null || excludeFilter.isMatch(filenames[n]) == false) {
                        FileMember newMember = new FileMember(filenames[n]);
                        newMember.setPath(aHolder.getPath() + filenames[n]);
                        aHolder.addMember(newMember);
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    private void outputMembers(ProjectHolder parent, ProjectHolder aHolder, Document doc, Element rt) {
        Element el = (Element) doc.createElement("Project");
        el.setAttribute("Name", aHolder.getName());
        if (parent != null) {
            if (aHolder.getPath().startsWith(parent.getPath()) && aHolder.getPath().indexOf(System.getProperty("file.separator"), parent.getPath().length()) == aHolder.getPath().length() - 1) {
                String aStr = aHolder.getPath().substring(parent.getPath().length(), aHolder.getPath().length() - 1);
                el.setAttribute("Path", aStr);
            } else el.setAttribute("Path", aHolder.getPath());
        } else el.setAttribute("Path", aHolder.getPath());
        rt.appendChild(el);
        for (Enumeration e = aHolder.getMembers().elements(); e.hasMoreElements(); ) {
            Member aMember = (Member) e.nextElement();
            if (!(aMember instanceof ProjectHolder)) {
                Element el2 = (Element) doc.createElement("Member");
                el2.appendChild(doc.createTextNode(aMember.getName()));
                el.appendChild(el2);
            }
        }
        for (Enumeration e = aHolder.getSubprojects().elements(); e.hasMoreElements(); ) {
            ProjectHolder aMember = (ProjectHolder) e.nextElement();
            outputMembers(aHolder, aMember, doc, el);
        }
    }

    private void createMembers(ProjectHolder aHolder, Element rt) {
        NodeList aList;
        aList = rt.getChildNodes();
        for (int n = 0; n < aList.getLength(); n++) {
            if (aList.item(n).getNodeType() == Node.ELEMENT_NODE && aList.item(n).getNodeName().equals("Project")) {
                NamedNodeMap nodeMap = aList.item(n).getAttributes();
                Node nameNode = nodeMap.getNamedItem("Name");
                Node pathNode = nodeMap.getNamedItem("Path");
                String newPath = pathNode.getNodeValue();
                if (newPath.indexOf(System.getProperty("file.separator")) >= 0) {
                    File aFile = new File(newPath);
                    if (aFile.exists() == false) {
                        int rc = JOptionPane.showConfirmDialog(mainWindow, "Sub Project '" + nameNode.getNodeValue() + "'\nNot found at location\n" + newPath + "\nPlease select a new location", "Alert", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                        if (rc == JOptionPane.YES_OPTION) {
                            JFileChooser chooser = getFileChooser("Select Sub Project Root");
                            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                            rc = chooser.showDialog(mainWindow, "Select");
                            if (rc == JFileChooser.APPROVE_OPTION) {
                                try {
                                    newPath = chooser.getSelectedFile().getCanonicalPath() + System.getProperty("file.separator");
                                } catch (Exception ex) {
                                }
                            }
                        }
                    }
                } else newPath = aHolder.getPath() + newPath + System.getProperty("file.separator");
                ProjectHolder newHolder = new ProjectHolder();
                newHolder.setName(nameNode.getNodeValue());
                newHolder.setPath(newPath);
                aHolder.addMember(newHolder);
                createMembers(newHolder, (Element) aList.item(n));
            }
        }
        aList = rt.getChildNodes();
        for (int n = 0; n < aList.getLength(); n++) {
            if (aList.item(n).getNodeType() == Node.ELEMENT_NODE && aList.item(n).getNodeName().equals("Member")) {
                FileMember newMember = new FileMember(aList.item(n).getFirstChild().getNodeValue());
                newMember.setPath(aHolder.getPath() + System.getProperty("file.separator") + newMember.getName());
                aHolder.addMember(newMember);
            }
        }
    }

    public static void traverseDOMBranch(Node node) {
        System.out.println(node.getNodeName() + ":" + node.getNodeValue());
        if (node.hasChildNodes()) {
            NodeList nl = node.getChildNodes();
            int size = nl.getLength();
            for (int i = 0; i < size; i++) {
                traverseDOMBranch(nl.item(i));
            }
        }
    }

    public void loadProject(final String filename) {
        projectFileName = filename;
        loadSubProject(filename, root);
        saveAsMenuItem.setEnabled(true);
        saveMenuItem.setEnabled(true);
        closeMenuItem.setEnabled(true);
        saveButton.setActionCommand("Save");
    }

    public void loadSubProject(final String filename, final ProjectHolder aHolder) {
        working = true;
        projectTree.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Thread aThread = new Thread(new Runnable() {

            public void run() {
                showStatus("Loading : " + filename);
                if (filename != null) {
                    InputStream is;
                    try {
                        is = new FileInputStream(filename);
                        DOMParser parser = new DOMParser();
                        parser.parse(new InputSource(is));
                        Document doc = parser.getDocument();
                        Element rt = (Element) doc.getDocumentElement();
                        NodeList aList;
                        String projVersion = null, projDesc = null;
                        try {
                            aList = rt.getElementsByTagName("ProjectVersion");
                            if (aList.getLength() == 1 && aList.item(0).getNodeType() == Node.ELEMENT_NODE) projVersion = aList.item(0).getFirstChild().getNodeValue();
                        } catch (Exception ex) {
                        }
                        try {
                            aList = rt.getElementsByTagName("ProjectDescription");
                            if (aList.getLength() == 1 && aList.item(0).getNodeType() == Node.ELEMENT_NODE) projDesc = aList.item(0).getFirstChild().getNodeValue();
                        } catch (Exception ex) {
                        }
                        env.loadEnvironment(rt);
                        aList = rt.getChildNodes();
                        for (int n = 0; n < aList.getLength(); n++) {
                            if (aList.item(n).getNodeType() == Node.ELEMENT_NODE && aList.item(n).getNodeName().equals("Project")) {
                                NamedNodeMap nodeMap = aList.item(n).getAttributes();
                                Node nameNode = nodeMap.getNamedItem("Name");
                                Node pathNode = nodeMap.getNamedItem("Path");
                                String aStr = pathNode.getNodeValue();
                                System.out.println(aStr);
                                File aFile = new File(aStr);
                                if (aFile.exists() == false) {
                                    int rc = JOptionPane.showConfirmDialog(mainWindow, "Project Root Not found at location\n" + aStr + "\nPlease select a new location", "Alert", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                                    if (rc == JOptionPane.YES_OPTION) {
                                        JFileChooser chooser = getFileChooser("Select New Project Root");
                                        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                                        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                                        rc = chooser.showDialog(mainWindow, "Select");
                                        if (rc == JFileChooser.APPROVE_OPTION) {
                                            aStr = chooser.getSelectedFile().getCanonicalPath() + System.getProperty("file.separator");
                                        }
                                    }
                                }
                                if (root == null) {
                                    setPath(nameNode.getNodeValue(), aStr);
                                    projectDetail.setProjectName(root.getName());
                                    projectDetail.setProjectPath(root.getPath());
                                    projectDetail.setProjectVersion(projVersion);
                                    projectDetail.setProjectDescription(projDesc);
                                    createMembers(root, (Element) aList.item(n));
                                } else {
                                    ProjectHolder newHolder = new ProjectHolder();
                                    newHolder.setPath(aStr);
                                    newHolder.setName(nameNode.getNodeValue());
                                    aHolder.addMember(newHolder);
                                    createMembers(newHolder, (Element) aList.item(n));
                                }
                            }
                        }
                        aList = rt.getChildNodes();
                        for (int n = 0; n < aList.getLength(); n++) {
                            if (aList.item(n).getNodeType() == Node.ELEMENT_NODE && aList.item(n).getNodeName().equals("Member")) {
                                FileMember newMember = new FileMember(aList.item(n).getFirstChild().getNodeValue());
                                newMember.setPath(root.getPath() + System.getProperty("file.separator") + newMember.getName());
                                root.addMember(newMember);
                            }
                        }
                        showStatus(" ");
                        refreshTree();
                        mainWindow.setTitle("ProjectBuilder - " + filename);
                    } catch (FileNotFoundException notFound) {
                        showStatus("File Not Found");
                        System.err.println(notFound);
                    } catch (Exception ex) {
                        showStatus("Unable to load project");
                    }
                    working = false;
                    projectTree.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    member.setProject(root);
                }
            }
        });
        aThread.start();
    }

    public void saveProject(final String filename) {
        saveSubProject(filename, root);
        projectFileName = filename;
        mainWindow.setTitle("ProjectBuilder - " + filename);
    }

    public void saveSubProject(final String filename, final ProjectHolder aHolder) {
        working = true;
        projectTree.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        Thread aThread = new Thread(new Runnable() {

            public void run() {
                if (filename != null) {
                    String newFilename;
                    projectTree.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    showStatus("Saving...");
                    if (filename.lastIndexOf(".pb") == -1) newFilename = filename + ".pb"; else newFilename = filename;
                    Document doc = new DocumentImpl();
                    Element rt = (Element) doc.createElement("ROOT");
                    doc.appendChild(rt);
                    if (aHolder == root) {
                        Element el = (Element) doc.createElement("ProjectVersion");
                        el.appendChild(doc.createTextNode(projectDetail.getProjectVersion()));
                        rt.appendChild(el);
                        el = (Element) doc.createElement("ProjectDescription");
                        el.appendChild(doc.createTextNode(projectDetail.getProjectDescription()));
                        rt.appendChild(el);
                        env.saveEnvironment(doc, rt);
                    }
                    outputMembers(null, aHolder, doc, rt);
                    try {
                        FileWriter fw = new FileWriter(filename);
                        XMLSerializer xmlWriter = new XMLSerializer(new OutputFormat());
                        xmlWriter.setOutputCharStream(fw);
                        xmlWriter.serialize(doc);
                        fw.close();
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                    working = false;
                    projectTree.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    showStatus(" ");
                }
            }
        });
        aThread.start();
    }

    public void zipSubProject(ZipOutputStream zos, ProjectHolder aHolder) {
        for (Enumeration e = aHolder.getMembers().elements(); e.hasMoreElements(); ) {
            Member aMember = (Member) e.nextElement();
            if (!(aMember instanceof ProjectHolder)) {
                try {
                    File aFile = new File(aMember.getPath());
                    FileInputStream fr = new FileInputStream(aFile);
                    byte fileBuffer[] = new byte[(int) aFile.length()];
                    int count = fr.read(fileBuffer);
                    String pathName = aFile.getCanonicalPath();
                    zos.putNextEntry(new ZipEntry((pathName.substring(pathName.indexOf(System.getProperty("file.separator")) + 1)).replace(System.getProperty("file.separator").charAt(0), '/')));
                    zos.write(fileBuffer, 0, fileBuffer.length);
                    fr.close();
                } catch (Exception ex) {
                    System.err.println("Error creating zip project : " + ex);
                }
            }
        }
        for (Enumeration e = aHolder.getSubprojects().elements(); e.hasMoreElements(); ) {
            ProjectHolder aMember = (ProjectHolder) e.nextElement();
            zipSubProject(zos, aMember);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (working) return;
        if (e.getActionCommand().compareTo("New") == 0) {
            addProject();
            return;
        }
        if (e.getActionCommand().compareTo("Exit") == 0) {
            ProjectFrame.removeAllProjects();
        }
        if (e.getActionCommand().compareTo("Open") == 0) {
            JFileChooser chooser = getFileChooser("Load Project");
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int rc = chooser.showDialog(mainWindow, "Load");
            if (rc == JFileChooser.APPROVE_OPTION) {
                try {
                    String filename = chooser.getSelectedFile().getCanonicalPath();
                    AddAndSaveHistory(filename);
                    if (root != null) addProject(filename); else loadProject(filename);
                } catch (Exception ex) {
                }
            }
            return;
        }
        if (e.getActionCommand().compareTo("History") == 0) {
            String filename = (String) ((JMenuItem) e.getSource()).getClientProperty("FilePath");
            if (root != null) addProject(filename); else loadProject(filename);
            return;
        }
        if (e.getActionCommand().compareTo("Save") == 0) {
            save();
        }
        if (e.getActionCommand().compareTo("ImportProject") == 0) {
            try {
                Object aNode = projectTree.getSelectionPath().getLastPathComponent();
                if (!(aNode instanceof ProjectHolder)) return;
                ProjectHolder aHolder = (ProjectHolder) aNode;
                JFileChooser chooser = getFileChooser("Import Project");
                chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int rc = chooser.showDialog(mainWindow, "Import");
                if (rc == JFileChooser.APPROVE_OPTION) {
                    String filename = chooser.getSelectedFile().getCanonicalPath();
                    loadSubProject(filename, aHolder);
                    ((ProjectTreeModel) treeModel).treeChanged();
                }
            } catch (Exception ex) {
            }
            return;
        }
        if (e.getActionCommand().compareTo("ExportProject") == 0) {
            try {
                Object aNode = projectTree.getSelectionPath().getLastPathComponent();
                if (!(aNode instanceof ProjectHolder)) return;
                ProjectHolder aHolder = (ProjectHolder) aNode;
                JFileChooser chooser = getFileChooser("Export Project");
                chooser.setDialogType(JFileChooser.SAVE_DIALOG);
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int rc = chooser.showDialog(mainWindow, "Export");
                if (rc == JFileChooser.APPROVE_OPTION) {
                    String filename = chooser.getSelectedFile().getCanonicalPath();
                    saveSubProject(filename, aHolder);
                }
            } catch (Exception ex) {
            }
            return;
        }
        if (e.getActionCommand().compareTo("SaveAs") == 0) {
            saveAs();
            return;
        }
        if (e.getActionCommand().compareTo("Close") == 0) {
            close();
        }
        if (e.getActionCommand().compareTo("About") == 0) {
            Dimension screenResolution = Toolkit.getDefaultToolkit().getScreenSize();
            final JDialog about = new JDialog(mainWindow, "About", true);
            about.setResizable(false);
            about.setBounds(screenResolution.width / 2 - 250, screenResolution.height / 2 - 150, 492, 331);
            JLabel aLabel = new JLabel(Utilities.getIcon("com/endfocus/projectbuilder/images/logo.jpg"));
            aLabel.setOpaque(false);
            aLabel.setBounds(0, 0, 492, 311);
            about.getLayeredPane().add(aLabel, JLayeredPane.DEFAULT_LAYER);
            JPanel aPanel = new JPanel();
            aPanel.setOpaque(false);
            aPanel.setLayout(new SpringLayout());
            aPanel.setBounds(0, 0, 492, 311);
            about.getLayeredPane().add(aPanel, JLayeredPane.PALETTE_LAYER);
            JButton aButton = new JButton("");
            aButton.setBounds(0, 0, 492, 311);
            aButton.setOpaque(false);
            aButton.setBorder(null);
            aButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent eve) {
                    about.dispose();
                }
            });
            about.getLayeredPane().add(aButton, JLayeredPane.MODAL_LAYER);
            about.invalidate();
            about.validate();
            about.show();
            return;
        }
        if (e.getActionCommand().compareTo("AddMembers") == 0) {
            if (member.getDisplayMode().compareTo("Non Members") == 0) {
                TreePath aPath = projectTree.getSelectionPath();
                member.addSelectedNonMembers();
                ((ProjectTreeModel) treeModel).treeChanged();
                projectTree.setSelectionPath(aPath);
                projectTree.expandPath(aPath);
            }
            return;
        }
        if (e.getActionCommand().compareTo("RemoveMembers") == 0) {
            if (member.getDisplayMode().compareTo("All Members") == 0) {
                TreePath aPath = projectTree.getSelectionPath();
                member.removeSelectedMembers();
                ((ProjectTreeModel) treeModel).treeChanged();
                projectTree.setSelectionPath(aPath);
            }
            return;
        }
        if (e.getActionCommand().compareTo("AddFolder") == 0) {
            try {
                Object aNode = projectTree.getSelectionPath().getLastPathComponent();
                if (!(aNode instanceof ProjectHolder)) return;
                ProjectHolder aHolder = (ProjectHolder) aNode;
                addFolderPanel.setCurrentRoot(aHolder);
                mainSplitPane.setRightComponent(addFolderPanel);
                mainSplitPane.invalidate();
                mainSplitPane.validate();
                mainSplitPane.repaint();
            } catch (Exception ex) {
            }
            return;
        }
        if (e.getActionCommand().compareTo("RenameFolder") == 0) {
            try {
                Object aNode = projectTree.getSelectionPath().getLastPathComponent();
                if (!(aNode instanceof ProjectHolder)) return;
                ProjectHolder aHolder = (ProjectHolder) aNode;
                renameFolderPanel.setCurrentRoot(aHolder);
                mainSplitPane.setRightComponent(renameFolderPanel);
                mainSplitPane.invalidate();
                mainSplitPane.validate();
                mainSplitPane.repaint();
            } catch (Exception ex) {
            }
            return;
        }
        if (e.getActionCommand().compareTo("RemoveFolder") == 0) {
            try {
                Object aNode = projectTree.getSelectionPath().getLastPathComponent();
                if (!(aNode instanceof ProjectHolder)) return;
                ProjectHolder aHolder = (ProjectHolder) aNode;
                if (aHolder == root) return;
                if (JOptionPane.showConfirmDialog(mainWindow, "Are you sure?\nRemove Sub Project\n'" + aHolder.getName() + "'", "Delete Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    TreePath parentPath = projectTree.getSelectionPath();
                    ProjectHolder parent = (ProjectHolder) parentPath.getPathComponent(parentPath.getPathCount() - 2);
                    parent.removeMember(aHolder);
                    showProject(parent);
                    projectTree.setSelectionPath(parentPath.getParentPath());
                }
            } catch (Exception ex) {
                System.err.println(ex);
            }
            return;
        }
        if (e.getActionCommand().compareTo("Zip") == 0) {
            try {
                Object aNode = projectTree.getSelectionPath().getLastPathComponent();
                if (!(aNode instanceof ProjectHolder)) return;
                ProjectHolder aHolder = (ProjectHolder) aNode;
                JFileChooser chooser = getFileChooser("Zip Project");
                chooser.setDialogType(JFileChooser.SAVE_DIALOG);
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int rc = chooser.showDialog(mainWindow, "Zip File");
                if (rc == JFileChooser.APPROVE_OPTION) {
                    String filename = chooser.getSelectedFile().getCanonicalPath();
                    if (filename.endsWith(".zip") == false && filename.indexOf('.') < 0) filename = filename + ".zip";
                    FileOutputStream fos = new FileOutputStream(filename);
                    ZipOutputStream zos = new ZipOutputStream(fos);
                    zipSubProject(zos, aHolder);
                    zos.close();
                    fos.close();
                }
            } catch (Exception ex) {
                System.err.println(ex);
            }
            return;
        }
        if (e.getActionCommand().compareTo("Compile") == 0) {
            mainSplitPane.setRightComponent(build);
            mainSplitPane.invalidate();
            mainSplitPane.validate();
            mainSplitPane.repaint();
            build.compileMembers(member.getSelectedMembers());
            return;
        }
        if (e.getActionCommand().compareTo("Build") == 0) {
            mainSplitPane.setRightComponent(build);
            mainSplitPane.invalidate();
            mainSplitPane.validate();
            mainSplitPane.repaint();
            return;
        }
        if (e.getActionCommand().compareTo("BuildAll") == 0) {
            mainSplitPane.setRightComponent(build);
            mainSplitPane.invalidate();
            mainSplitPane.validate();
            mainSplitPane.repaint();
            build.buildAll();
            return;
        }
        if (e.getActionCommand().compareTo("BuildAndRun") == 0) {
            mainSplitPane.setRightComponent(build);
            mainSplitPane.invalidate();
            mainSplitPane.validate();
            mainSplitPane.repaint();
            build.buildAndRun();
            return;
        }
        if (e.getActionCommand().compareTo("BuildDirty") == 0) {
            mainSplitPane.setRightComponent(build);
            mainSplitPane.invalidate();
            mainSplitPane.validate();
            mainSplitPane.repaint();
            build.buildDirty();
            return;
        }
        if (e.getActionCommand().compareTo("BuildProject") == 0) {
            mainSplitPane.setRightComponent(build);
            mainSplitPane.invalidate();
            mainSplitPane.validate();
            mainSplitPane.repaint();
            Object aNode = projectTree.getSelectionPath().getLastPathComponent();
            if (!(aNode instanceof ProjectHolder)) return;
            ProjectHolder aHolder = (ProjectHolder) aNode;
            build.buildProject(aHolder);
            return;
        }
        if (e.getActionCommand().compareTo("BuildOptions") == 0) {
            mainSplitPane.setRightComponent(env);
            mainSplitPane.invalidate();
            mainSplitPane.validate();
            mainSplitPane.repaint();
            return;
        }
        if (e.getActionCommand().compareTo("ProjectDetail") == 0) {
            mainSplitPane.setRightComponent(projectDetail);
            return;
        }
        if (e.getActionCommand().compareTo("Import") == 0) {
            try {
                Object aNode = projectTree.getSelectionPath().getLastPathComponent();
                if (!(aNode instanceof ProjectHolder)) return;
                ProjectHolder aHolder = (ProjectHolder) aNode;
                importDetail.setCurrentRoot(aHolder);
                mainSplitPane.setRightComponent(importDetail);
            } catch (Exception ex) {
            }
            return;
        }
        if (e.getActionCommand().compareTo("ToolsPlugins") == 0) {
            mainSplitPane.setRightComponent(pluginPanel);
            mainSplitPane.invalidate();
            mainSplitPane.validate();
            mainSplitPane.repaint();
            return;
        }
        if (e.getActionCommand().compareTo("SessionId") == 0) {
            sessionPanel.setVisible(true);
            mainSplitPane.setRightComponent(sessionPanel);
            mainSplitPane.invalidate();
            mainSplitPane.validate();
            mainSplitPane.repaint();
        }
    }

    private boolean saveAs() {
        JFileChooser chooser = getFileChooser("Save Project");
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int rc = chooser.showDialog(mainWindow, "Save As");
        if (rc == JFileChooser.APPROVE_OPTION) {
            try {
                String filename = chooser.getSelectedFile().getCanonicalPath();
                if (filename.endsWith(".pb") == false && filename.indexOf('.') < 0) filename = filename + ".pb";
                AddAndSaveHistory(filename);
                saveProject(filename);
                saveAsMenuItem.setEnabled(true);
                saveMenuItem.setEnabled(true);
                closeMenuItem.setEnabled(true);
                saveButton.setActionCommand("Save");
                return true;
            } catch (Exception ex) {
            }
        }
        return false;
    }

    private void save() {
        saveProject(projectFileName);
    }

    public void close() {
        int rc = JOptionPane.showConfirmDialog(mainWindow, "Do you want to save this project before closing?", "Save Project Confirmation", JOptionPane.YES_NO_CANCEL_OPTION);
        if (rc == JOptionPane.CANCEL_OPTION) return;
        if (rc == JOptionPane.YES_OPTION) if (saveButton.getActionCommand().compareTo("Save") == 0) save(); else if (!saveAs()) return;
        mainWindow.dispose();
        removeProject(this);
    }

    public void valueChanged(TreeSelectionEvent e) {
        if (working) return;
        try {
            Object aNode = e.getNewLeadSelectionPath().getLastPathComponent();
            if (!(aNode instanceof ProjectHolder)) return;
            ProjectHolder aHolder = (ProjectHolder) aNode;
            mainSplitPane.setRightComponent(member);
            member.setProject(aHolder);
        } catch (Exception ex) {
            mainSplitPane.setRightComponent(projectDetail);
        }
        mainSplitPane.invalidate();
        mainSplitPane.validate();
        mainSplitPane.repaint();
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        close();
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    class PopupListener extends MouseAdapter {

        public void mouseClicked(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            showPopup(e);
            if (root != null && !popupMenu.isVisible() && !e.isPopupTrigger()) {
                showMembers();
            }
        }

        private void showPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                if (root == null) return;
                if (projectTree.getSelectionCount() > 0) {
                    Object aNode = projectTree.getSelectionPath().getLastPathComponent();
                    if (!(aNode instanceof ProjectHolder)) return;
                    ProjectHolder aHolder = (ProjectHolder) aNode;
                    buildP.setText("Build '" + aHolder.getName() + "'");
                    buildP.setEnabled(true);
                    pm1.setEnabled(true);
                    pm2.setEnabled(true);
                    pm3.setEnabled(true);
                    pm4.setEnabled(true);
                    pm5.setEnabled(true);
                    pm6.setEnabled(true);
                    pm7.setEnabled(true);
                } else {
                    buildP.setText("Build ''");
                    buildP.setEnabled(false);
                    pm1.setEnabled(false);
                    pm2.setEnabled(false);
                    pm3.setEnabled(false);
                    pm4.setEnabled(false);
                    pm5.setEnabled(false);
                    pm6.setEnabled(false);
                    pm7.setEnabled(false);
                }
                popupMenu.show(projectTree, e.getX(), e.getY());
            }
        }
    }
}
