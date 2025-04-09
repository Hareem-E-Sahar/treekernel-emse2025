package com.testonica.kickelhahn.core.ui.project;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.print.Printable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import com.testonica.common.utils.FileUtils;
import com.testonica.kickelhahn.core.KickelhahnInfo;
import com.testonica.kickelhahn.core.formats.common.FormatInformation;
import com.testonica.kickelhahn.core.manager.MenuHandler;
import com.testonica.kickelhahn.core.manager.project.FileAssociationsManager;
import com.testonica.kickelhahn.core.manager.project.FileComparator;
import com.testonica.kickelhahn.core.printer.TextPrinter;
import com.testonica.kickelhahn.core.ui.common.ToolbarToggleButton;
import com.testonica.kickelhahn.core.ui.netlist.NetlistViewerEditor;
import com.testonica.kickelhahn.core.ui.project.tree.ProjectDnDTree;
import com.testonica.kickelhahn.core.ui.project.tree.ProjectTreeCellRendered;
import com.testonica.kickelhahn.core.ui.project.tree.ProjectTreeNode;
import com.testonica.kickelhahn.core.ui.viewereditor.ViewerEditor;

public class ProjectViewerEditor extends ViewerEditor implements TreeSelectionListener, MouseListener, MenuHandler {

    private static final String UNSPECIFIED_GROUP = "Unspecified";

    private static final String LIBRARY = "library";

    private static final int LAYOUT_FLAT = 0;

    private static final int LAYOUT_HIERARCHICAL = 1;

    private int treeLayout = LAYOUT_HIERARCHICAL;

    private JMenuItem popupMenuExecute;

    private JPopupMenu popupMenu;

    private File projectPath;

    private ProjectTreeNode root;

    protected String title = "Project Explorer";

    private ProjectDnDTree tree = new ProjectDnDTree(this);

    private DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();

    private Font plainFont;

    private Action executeAction = new AbstractAction() {

        public void actionPerformed(ActionEvent arg0) {
            openAction();
        }
    };

    private Action openAction = new AbstractAction() {

        public void actionPerformed(ActionEvent arg0) {
            getInfo().getProjectManager().showOpenDialog();
        }
    };

    private Action refreshAction = new AbstractAction() {

        public void actionPerformed(ActionEvent arg0) {
            refresh(false);
        }
    };

    private Action removeAction = new AbstractAction() {

        public void actionPerformed(ActionEvent arg0) {
            if (!popupMenuDelete.isEnabled()) return;
            getInfo().getProjectManager().removeFromProject(getSelectedFiles(), true);
        }
    };

    private Action renameAction = new AbstractAction() {

        public void actionPerformed(ActionEvent arg0) {
            if (!popupMenuRename.isEnabled()) return;
            if (tree.getSelectionPath() != null) {
                if (isProjectSelected()) {
                    getInfo().getProjectManager().showProjectRenameDialog(getSelectedFile());
                } else getInfo().getProjectManager().showRenameFileDialog(getSelectedFile());
            }
        }
    };

    private Action propertiesAction = new AbstractAction() {

        public void actionPerformed(ActionEvent arg0) {
            getInfo().getProjectManager().showFileProperties(getSelectedFile());
        }
    };

    private Action newProjectAction = new AbstractAction() {

        public void actionPerformed(ActionEvent arg0) {
            getInfo().getProjectManager().showNewProjectDialog();
        }
    };

    private Action newBSDLAction = new AbstractAction() {

        public void actionPerformed(ActionEvent arg0) {
            getInfo().getProjectManager().showNewFileDialog(getCurrentFolder(), "BSDL", "bsd");
        }
    };

    private Action newNetlistAction = new AbstractAction() {

        public void actionPerformed(ActionEvent arg0) {
            getInfo().getProjectManager().showNewFileDialog(getCurrentFolder(), "Netlist", "nl");
        }
    };

    private Action newSVFAction = new AbstractAction() {

        public void actionPerformed(ActionEvent arg0) {
            getInfo().getProjectManager().showNewFileDialog(getCurrentFolder(), "SVF", "svf");
        }
    };

    private Action newLogicAction = new AbstractAction() {

        public void actionPerformed(ActionEvent arg0) {
            getInfo().getProjectManager().showNewFileDialog(getCurrentFolder(), "Decision Diagram", "agm");
        }
    };

    private Action newFolderAction = new AbstractAction() {

        public void actionPerformed(ActionEvent arg0) {
            getInfo().getProjectManager().showNewFolderDialog(getCurrentFolder());
        }
    };

    public File getCurrentFolder() {
        File folder = super.getCurrentFolder();
        if (folder == null) return getInfo().getProjectManager().getProjectFile();
        return folder;
    }

    private Action newPlainAction = new AbstractAction() {

        public void actionPerformed(ActionEvent arg0) {
            getInfo().getProjectManager().showNewFileDialog(getCurrentFolder(), "", "");
        }
    };

    private Action cutAction = new AbstractAction() {

        public void actionPerformed(ActionEvent arg0) {
            if (!popupMenuCut.isEnabled()) return;
            cut();
        }
    };

    private Action copyAction = new AbstractAction() {

        public void actionPerformed(ActionEvent arg0) {
            if (!popupMenuCopy.isEnabled()) return;
            copy();
        }
    };

    private Action pasteAction = new AbstractAction() {

        public void actionPerformed(ActionEvent arg0) {
            paste();
        }
    };

    private KeyStroke properiesKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.ALT_MASK);

    private KeyStroke refreshKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);

    private KeyStroke executeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

    private KeyStroke cutKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK);

    private KeyStroke copyKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK);

    private KeyStroke pasteKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK);

    private KeyStroke deleteKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);

    private KeyStroke renameKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);

    private JMenuItem popupMenuCut;

    private JMenuItem popupMenuCopy;

    private JMenuItem popupMenuPaste;

    private JMenuItem popupMenuDelete;

    private JMenuItem popupMenuRename;

    private JMenu popupMenuImport;

    private JMenuItem popupMenuRefresh;

    private JMenuItem popupMenuProperties;

    private JMenuItem popupMenuExport;

    private JMenuItem popupMenuExportAsZip;

    private JMenu popupMenuNew;

    private JMenuItem popupMenuNewProject;

    private JMenuItem popupMenuNewBSDL;

    private JMenuItem popupMenuNewNetlist;

    private JMenuItem popupMenuNewSVF;

    private JMenuItem popupMenuNewLogic;

    private JMenuItem popupMenuNewFolder;

    private JMenuItem popupMenuNewPlain;

    private JMenuItem popupMenuOpen;

    private boolean wasCut;

    private ToolbarToggleButton buttonHierarchicalLayout;

    private HashMap<String, ArrayList<String>> projectGroups = new HashMap<String, ArrayList<String>>();

    public ProjectViewerEditor(KickelhahnInfo info) {
        super(info);
        add(new JScrollPane(tree), BorderLayout.CENTER);
        ArrayList<String> netlistFiles = new ArrayList<String>();
        netlistFiles.add(".nl");
        projectGroups.put("Netlists", netlistFiles);
        ArrayList<String> componentsFiles = new ArrayList<String>();
        componentsFiles.add(".agm");
        componentsFiles.add(".bsd");
        componentsFiles.add(".bsdl");
        componentsFiles.add(".bsm");
        projectGroups.put("Components", componentsFiles);
        ArrayList<String> executablesFiles = new ArrayList<String>();
        executablesFiles.add(".svf");
        projectGroups.put("Executables", executablesFiles);
        ArrayList<String> docsFiles = new ArrayList<String>();
        docsFiles.add(".doc");
        docsFiles.add(".txt");
        docsFiles.add(".rtf");
        docsFiles.add(".pdf");
        docsFiles.add(".htm");
        docsFiles.add(".html");
        projectGroups.put("Docs", docsFiles);
        initTree();
        initToolbar();
        buttonHierarchicalLayout = new ToolbarToggleButton(info.getImage("flatLayout16x16.png"), new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                setTreeLayout(e.getStateChange() == ItemEvent.SELECTED ? LAYOUT_FLAT : LAYOUT_HIERARCHICAL);
                buttonHierarchicalLayout.setToolTipText(e.getStateChange() == ItemEvent.SELECTED ? "Hierarchical layout" : "Flat layout");
                getInfo().getConfigurationManager().setProjectExplorerFlat(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        buttonHierarchicalLayout.setSelectedIcon(info.getImage("hierarchicalLayout16x16.png"));
        buttonHierarchicalLayout.setToolTipText("Hierarchical layout");
        buttonHierarchicalLayout.setSelected(getInfo().getConfigurationManager().isProjectExplorerFlat());
        buttonHierarchicalLayout.setEnabled(false);
        addToolbarComponent(buttonHierarchicalLayout);
        initPopupMenu();
        refresh(false);
    }

    private boolean isProjectSelected() {
        if (getSelectedFile() == null || getInfo().getProjectManager().getProjectFile() == null) return false;
        return getSelectedFile().getAbsolutePath().equals(getInfo().getProjectManager().getProjectFile().getAbsolutePath());
    }

    public String getSelectedPackagePath() {
        return tree.getSelectionPath().getPathComponent(1).toString().equals(LIBRARY) ? getInfo().getConfigurationManager().getProjectExplorerLibraryPath() : getInfo().getProjectManager().getProjectFile().getAbsolutePath();
    }

    public File[] getSelectedFiles() {
        if ((getInfo().getProjectManager() == null) || (getInfo().getProjectManager().getProjectFile() == null) || (tree == null) || (tree.getSelectionPaths() == null)) return null;
        File[] files = new File[tree.getSelectionPaths().length];
        for (int i = 0; i < tree.getSelectionPaths().length; i++) {
            if (((ProjectTreeNode) tree.getSelectionPaths()[i].getLastPathComponent()).getUserObject() instanceof File) files[i] = (File) ((ProjectTreeNode) tree.getSelectionPaths()[i].getLastPathComponent()).getUserObject(); else files[i] = new File("");
        }
        return files;
    }

    private void initTree() {
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        tree.setToggleClickCount(3);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.setCellRenderer(new ProjectTreeCellRendered(getInfo()));
        tree.addTreeSelectionListener(this);
        tree.addMouseListener(this);
        tree.getInputMap().clear();
        tree.getActionMap().clear();
        tree.getInputMap().put(executeKeyStroke, "openAction");
        tree.getActionMap().put("openAction", executeAction);
        tree.getInputMap().put(refreshKeyStroke, "refreshAction");
        tree.getActionMap().put("refreshAction", refreshAction);
        tree.getInputMap().put(deleteKeyStroke, "removeAction");
        tree.getActionMap().put("removeAction", removeAction);
        tree.getInputMap().put(renameKeyStroke, "renameAction");
        tree.getActionMap().put("renameAction", renameAction);
        tree.getInputMap().put(properiesKeyStroke, "propertiesAction");
        tree.getActionMap().put("propertiesAction", propertiesAction);
        tree.getInputMap().put(cutKeyStroke, "cutAction");
        tree.getActionMap().put("cutAction", cutAction);
        tree.getInputMap().put(copyKeyStroke, "copyAction");
        tree.getActionMap().put("copyAction", copyAction);
        tree.getInputMap().put(pasteKeyStroke, "pasteAction");
        tree.getActionMap().put("pasteAction", pasteAction);
        treeModel.setRoot(null);
    }

    private void initPopupMenu() {
        popupMenu = new JPopupMenu("Project Explorer Popup Menu");
        popupMenuExecute = new JMenuItem("Open", getInfo().getImage("none16x16.png"));
        popupMenuExecute.setMnemonic('O');
        popupMenuExecute.setAccelerator(executeKeyStroke);
        popupMenuExecute.addActionListener(executeAction);
        plainFont = popupMenuExecute.getFont().deriveFont(Font.PLAIN);
        popupMenuNew = new JMenu("New");
        popupMenuNew.setFont(plainFont);
        popupMenuNew.setIcon(getInfo().getImage("none16x16.png"));
        popupMenuNew.setMnemonic('N');
        popupMenuNewProject = new JMenuItem("Project...", getInfo().getImage("files" + File.separator + "folderProject16x16.png"));
        popupMenuNewProject.setFont(plainFont);
        popupMenuNewProject.setMnemonic('P');
        popupMenuNewProject.addActionListener(newProjectAction);
        popupMenuNewBSDL = new JMenuItem("BSDL File...", getInfo().getImage("files" + File.separator + "fileBSDL16x16.png"));
        popupMenuNewBSDL.setFont(plainFont);
        popupMenuNewBSDL.setMnemonic('B');
        popupMenuNewBSDL.addActionListener(newBSDLAction);
        popupMenuNewNetlist = new JMenuItem("Netlist File...", getInfo().getImage("files" + File.separator + "fileNL16x16.png"));
        popupMenuNewNetlist.setFont(plainFont);
        popupMenuNewNetlist.setMnemonic('N');
        popupMenuNewNetlist.addActionListener(newNetlistAction);
        popupMenuNewSVF = new JMenuItem("SVF File...", getInfo().getImage("files" + File.separator + "fileSVF16x16.png"));
        popupMenuNewSVF.setFont(plainFont);
        popupMenuNewSVF.setMnemonic('S');
        popupMenuNewSVF.addActionListener(newSVFAction);
        popupMenuNewLogic = new JMenuItem("Logic Model...", getInfo().getImage("files" + File.separator + "fileAGM16x16.png"));
        popupMenuNewLogic.setFont(plainFont);
        popupMenuNewLogic.setMnemonic('L');
        popupMenuNewLogic.addActionListener(newLogicAction);
        popupMenuNewFolder = new JMenuItem("Folder...", getInfo().getImage("files" + File.separator + "folderClosed16x16.png"));
        popupMenuNewFolder.setFont(plainFont);
        popupMenuNewFolder.setMnemonic('F');
        popupMenuNewFolder.addActionListener(newFolderAction);
        popupMenuNewPlain = new JMenuItem("Plain File...", getInfo().getImage("files" + File.separator + "filePlain16x16.png"));
        popupMenuNewPlain.setFont(plainFont);
        popupMenuNewPlain.setMnemonic('A');
        popupMenuNewPlain.addActionListener(newPlainAction);
        popupMenuNew.setMnemonic('N');
        popupMenuNew.add(popupMenuNewProject);
        popupMenuNew.addSeparator();
        popupMenuNew.add(popupMenuNewBSDL);
        popupMenuNew.add(popupMenuNewNetlist);
        popupMenuNew.add(popupMenuNewSVF);
        popupMenuNew.add(popupMenuNewLogic);
        popupMenuNew.addSeparator();
        popupMenuNew.add(popupMenuNewFolder);
        popupMenuNew.add(popupMenuNewPlain);
        popupMenuOpen = new JMenuItem("Open...", getInfo().getImage("none16x16.png"));
        popupMenuOpen.setFont(plainFont);
        popupMenuOpen.setMnemonic('O');
        popupMenuOpen.addActionListener(openAction);
        popupMenuCut = new JMenuItem("Cut", getInfo().getImage("cut16x16.png"));
        popupMenuCut.setFont(plainFont);
        popupMenuCut.setMnemonic('T');
        popupMenuCut.setAccelerator(cutKeyStroke);
        popupMenuCut.addActionListener(cutAction);
        popupMenuCopy = new JMenuItem("Copy", getInfo().getImage("copy16x16.png"));
        popupMenuCopy.setFont(plainFont);
        popupMenuCopy.setMnemonic('C');
        popupMenuCopy.setAccelerator(copyKeyStroke);
        popupMenuCopy.addActionListener(copyAction);
        popupMenuPaste = new JMenuItem("Paste", getInfo().getImage("paste16x16.png"));
        popupMenuPaste.setFont(plainFont);
        popupMenuPaste.setMnemonic('P');
        popupMenuPaste.setAccelerator(pasteKeyStroke);
        popupMenuPaste.addActionListener(pasteAction);
        popupMenuDelete = new JMenuItem("Delete", getInfo().getImage("delete16x16.png"));
        popupMenuDelete.setFont(plainFont);
        popupMenuDelete.setMnemonic('D');
        popupMenuDelete.setAccelerator(deleteKeyStroke);
        popupMenuDelete.addActionListener(removeAction);
        popupMenuRename = new JMenuItem("Rename", getInfo().getImage("none16x16.png"));
        popupMenuRename.setFont(plainFont);
        popupMenuRename.setMnemonic('D');
        popupMenuRename.setAccelerator(renameKeyStroke);
        popupMenuRename.addActionListener(renameAction);
        JMenuItem popupMenuImportBSDL = new JMenuItem("Boundary Scan Description...");
        popupMenuImportBSDL.setFont(plainFont);
        popupMenuImportBSDL.setMnemonic('B');
        popupMenuImportBSDL.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                FormatInformation fileInformator = getInfo().getProjectManager().getFileInformator().getBSDLFormatInformation();
                getInfo().getProjectManager().showImportFilesDialog(getCurrentFolder(), fileInformator.getExtensions(), fileInformator.getDescription());
            }
        });
        JMenuItem popupMenuImportNetlist = new JMenuItem("Netlist...");
        popupMenuImportNetlist.setFont(plainFont);
        popupMenuImportNetlist.setMnemonic('N');
        popupMenuImportNetlist.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                FormatInformation fileInformator = getInfo().getProjectManager().getFileInformator().getNLFormatInformation();
                getInfo().getProjectManager().showImportFilesDialog(getCurrentFolder(), fileInformator.getExtensions(), fileInformator.getDescription());
            }
        });
        JMenuItem popupMenuImportVectors = new JMenuItem("Vectors...");
        popupMenuImportVectors.setFont(plainFont);
        popupMenuImportVectors.setMnemonic('V');
        popupMenuImportVectors.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                FormatInformation fileInformator = getInfo().getProjectManager().getFileInformator().getSVFFormatInformation();
                getInfo().getProjectManager().showImportFilesDialog(getCurrentFolder(), fileInformator.getExtensions(), fileInformator.getDescription());
            }
        });
        JMenuItem popupMenuImportLogic = new JMenuItem("Logic Model...");
        popupMenuImportLogic.setFont(plainFont);
        popupMenuImportLogic.setMnemonic('L');
        popupMenuImportLogic.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                FormatInformation fileInformator = getInfo().getProjectManager().getFileInformator().getBDDFormatInformation();
                getInfo().getProjectManager().showImportFilesDialog(getCurrentFolder(), fileInformator.getExtensions(), fileInformator.getDescription());
            }
        });
        JMenuItem popupMenuImportOther = new JMenuItem("Other...");
        popupMenuImportOther.setFont(plainFont);
        popupMenuImportOther.setMnemonic('O');
        popupMenuImportOther.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                getInfo().getProjectManager().showImportFilesDialog(getCurrentFolder(), new String[] {}, "");
            }
        });
        popupMenuImport = new JMenu("Import...");
        popupMenuImport.setIcon(getInfo().getImage("import16x16.png"));
        popupMenuImport.setFont(plainFont);
        popupMenuImport.setMnemonic('I');
        popupMenuImport.add(popupMenuImportBSDL);
        popupMenuImport.add(popupMenuImportNetlist);
        popupMenuImport.add(popupMenuImportVectors);
        popupMenuImport.add(popupMenuImportLogic);
        popupMenuImport.addSeparator();
        popupMenuImport.add(popupMenuImportOther);
        popupMenuExport = new JMenuItem("Export...", getInfo().getImage("export16x16.png"));
        popupMenuExport.setFont(plainFont);
        popupMenuExport.setMnemonic('E');
        popupMenuExport.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                File file = getInfo().getProjectManager().showExportFileDialog(getSelectedFile(), null, null);
                if (file != null) getInfo().getProjectManager().exportFiles(getSelectedFiles(), file);
            }
        });
        popupMenuExportAsZip = new JMenuItem("Export Project as Zip Archive...", getInfo().getImage("exportZip16x16.png"));
        popupMenuExportAsZip.setFont(plainFont);
        popupMenuExportAsZip.setMnemonic('E');
        popupMenuExportAsZip.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                File file = getInfo().getProjectManager().showExportFileDialog(new File(getInfo().getProjectManager().getProjectName() + ".zip"), new String[] { "zip" }, "Zip Archive");
                if (file != null) getInfo().getProjectManager().zipProject(file);
            }
        });
        popupMenuRefresh = new JMenuItem("Refresh", getInfo().getImage("refresh16x16.png"));
        popupMenuRefresh.setFont(plainFont);
        popupMenuRefresh.setMnemonic('R');
        popupMenuRefresh.setAccelerator(refreshKeyStroke);
        popupMenuRefresh.addActionListener(refreshAction);
        popupMenuProperties = new JMenuItem("Properties", getInfo().getImage("properties16x16.png"));
        popupMenuProperties.setFont(plainFont);
        popupMenuProperties.setMnemonic('P');
        popupMenuProperties.setAccelerator(properiesKeyStroke);
        popupMenuProperties.addActionListener(propertiesAction);
    }

    private TreePath produceTreePath(TreePath selection) {
        if (((selection == null) || (selection.getPathComponent(0) == null)) | (root == null)) return null;
        if (!root.toString().equals(selection.getPathComponent(0).toString())) return null;
        TreePath newPath = new TreePath(root);
        Object[] path = selection.getPath();
        ProjectTreeNode child = root;
        for (int i = 1; i < path.length; i++) {
            boolean added = false;
            for (int j = 0; j < child.getChildCount(); j++) {
                if (path[i].toString().equals(child.getChildAt(j).toString())) {
                    added = true;
                    newPath = newPath.pathByAddingChild(child.getChildAt(j));
                    child = (ProjectTreeNode) child.getChildAt(j);
                    break;
                }
            }
            if (!added) break;
        }
        return newPath;
    }

    public boolean isPrintingSupported() {
        return (getCurrentFile() != null) && (!getCurrentFile().isDirectory());
    }

    public ProjectTreeNode getFlatProjectContents(ProjectTreeNode node, String projectName, File projectFile) {
        ProjectTreeNode projectRoot = null;
        if (!projectFile.isDirectory()) {
            ProjectTreeNode child = new ProjectTreeNode(projectFile.getName());
            child.setUserObject(projectFile);
            child = getComponentNodes(child, projectFile);
            if (node != null) node.add(child); else {
                projectRoot = new ProjectTreeNode(projectName);
                projectRoot.setUserObject(projectFile);
                projectRoot.setText(projectName);
                child = projectRoot;
            }
        } else {
            ProjectTreeNode child = new ProjectTreeNode(projectFile.getName());
            child.setUserObject(projectFile);
            if (node != null) {
                node.add(child);
            } else {
                projectRoot = new ProjectTreeNode(projectName);
                projectRoot.setUserObject(projectFile);
                projectRoot.setText(projectName);
                child = projectRoot;
            }
            File fileList[] = projectFile.listFiles();
            Arrays.<Object>sort(fileList, new FileComparator());
            for (int i = 0; i < fileList.length; i++) getFlatProjectContents(child, projectName, fileList[i]);
        }
        return projectRoot;
    }

    private ProjectTreeNode getComponentNodes(ProjectTreeNode child, File file) {
        return child;
    }

    private boolean isFiltered(File file, ArrayList<String> filter) {
        if (file.isDirectory()) return false;
        for (int i = 0; i < filter.size(); i++) {
            if (file.getName().contains((String) filter.get(i))) return true;
        }
        return false;
    }

    public ProjectTreeNode getHierarchicalProjectContents(String projectName) {
        ProjectTreeNode projectRoot = new ProjectTreeNode(projectName);
        projectRoot.setUserObject(projectPath);
        projectRoot.setText(projectName);
        List<File> fileList = getFileList(projectPath, new ArrayList<File>());
        Object[] groupNames = projectGroups.keySet().toArray();
        for (int i = 0; i < groupNames.length; i++) projectRoot.add(getGroupNode((String) groupNames[i], fileList));
        if (fileList.size() > 0) {
            ProjectTreeNode node = new ProjectTreeNode(UNSPECIFIED_GROUP);
            node.setPseudoFolder(true);
            node.setUserObject(new File(UNSPECIFIED_GROUP));
            for (int i = 0; i < fileList.size(); i++) {
                ProjectTreeNode child = new ProjectTreeNode(fileList.get(i).getName());
                child.setUserObject(fileList.get(i));
                if (fileList.get(i).isDirectory()) continue;
                if (child != null) node.add(child);
            }
            projectRoot.add(node);
        }
        return projectRoot;
    }

    private ProjectTreeNode getGroupNode(String groupName, List<File> fileList) {
        ProjectTreeNode node = new ProjectTreeNode(groupName);
        node.setPseudoFolder(true);
        node.setUserObject(new File(groupName));
        for (int i = 0; i < fileList.size(); i++) {
            if (!fileList.get(i).isDirectory() & !isFiltered(fileList.get(i), projectGroups.get(groupName))) {
                continue;
            }
            ProjectTreeNode child = new ProjectTreeNode(fileList.get(i).getName());
            child.setUserObject(fileList.get(i));
            if (fileList.get(i).isDirectory()) {
                i = getFolderNode(fileList.get(i), fileList, groupName, child, i);
                if (child.getChildCount() == 0) continue;
            } else {
                fileList.remove(i);
                i--;
            }
            if (child != null) node.add(child);
        }
        return node;
    }

    private int getFolderNode(File file, List<File> fileList, String groupName, ProjectTreeNode node, int index) {
        File[] folderFiles = file.listFiles();
        boolean folderIsEmpty = true;
        for (int i = 0; i < folderFiles.length; i++, index++) {
            if (!fileList.contains(folderFiles[i]) || (!folderFiles[i].isDirectory() && (!groupName.equals(UNSPECIFIED_GROUP)) && !isFiltered(folderFiles[i], projectGroups.get(groupName)))) {
                folderIsEmpty = false;
                continue;
            }
            ProjectTreeNode child = new ProjectTreeNode(folderFiles[i].getName());
            child.setUserObject(folderFiles[i]);
            fileList.remove(folderFiles[i]);
            index--;
            if (child != null) node.add(child);
        }
        if (folderIsEmpty) {
            fileList.remove(file);
            index--;
        }
        return index;
    }

    private List<File> getFileList(File folder, List<File> allFiles) {
        if (!folder.isDirectory()) return allFiles;
        List<File> files = new ArrayList<File>();
        File[] fileList = folder.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isDirectory()) getFileList(fileList[i], allFiles); else files.add(fileList[i]);
        }
        allFiles.addAll(files);
        return allFiles;
    }

    public File getSelectedFile() {
        if ((getInfo().getProjectManager() == null) || (tree == null) || tree.getSelectionPath() == null) return null;
        ProjectTreeNode fileTreeNode = ((ProjectTreeNode) tree.getSelectionPath().getLastPathComponent());
        if (fileTreeNode.getUserObject() instanceof File) return (File) fileTreeNode.getUserObject();
        return null;
    }

    private boolean isPseudoFolderSelected() {
        if ((getInfo().getProjectManager() == null) || (tree == null) || tree.getSelectionPath() == null) return false;
        ProjectTreeNode fileTreeNode = ((ProjectTreeNode) tree.getSelectionPath().getLastPathComponent());
        return fileTreeNode.isPseudoFolder();
    }

    protected String getSpecificInfo() {
        if ((getSelectedFiles() == null) || (getSelectedFiles().length <= 0)) return "";
        if (getSelectedFiles().length == 1) {
            String projectRelatedPath = getProjectRelatedPath(getSelectedFile());
            if (projectRelatedPath == null) return "";
            if (projectRelatedPath.equals(getSelectedFile().getAbsolutePath())) return getSelectedFile().getName();
            return projectRelatedPath;
        }
        return getSelectedFiles().length + " files selected";
    }

    private String getProjectRelatedPath(File selectedFile) {
        if (selectedFile == null) return "";
        String path = selectedFile.getAbsolutePath();
        int projectPathIndex = path.indexOf(projectPath.getAbsolutePath());
        if (projectPathIndex > -1) path = path.substring(projectPathIndex + projectPath.getAbsolutePath().length());
        return path;
    }

    public String getTitle() {
        return title;
    }

    public void importToProject(File[] selectedFiles) {
        try {
            getInfo().getProjectManager().copyWithOverwrite(selectedFiles, getSeletedFolder());
        } catch (IOException e) {
            getInfo().getErrorHandler().error(e);
        }
        refresh(false);
    }

    public File getSeletedFolder() {
        if (getSelectedFile() == null || !getSelectedFile().exists()) return projectPath;
        if (getSelectedFile().isDirectory()) return getSelectedFile();
        return getSelectedFile().getParentFile();
    }

    public void mouseClicked(MouseEvent arg0) {
        if (arg0.getClickCount() > 1) openAction();
    }

    private void openAction() {
        openInEditorAction(0);
    }

    private void openInEditorAction(int editor) {
        if (tree.getSelectionPath() == null) return;
        if (((MutableTreeNode) tree.getSelectionPath().getLastPathComponent()).isLeaf()) {
            if (tree.getSelectionPath().getLastPathComponent() != root) if ((!getSelectedFile().isDirectory()) && (getSelectedFile().exists())) getInfo().getProjectManager().openFile(getSelectedFile(), editor);
        } else {
            if (tree.isExpanded(tree.getSelectionPath())) tree.collapsePath(tree.getSelectionPath()); else tree.expandPath(tree.getSelectionPath());
        }
    }

    public void mouseEntered(MouseEvent arg0) {
    }

    public void mouseExited(MouseEvent arg0) {
    }

    public void mousePressed(MouseEvent arg0) {
    }

    public void mouseReleased(MouseEvent arg0) {
        Point pointInTree = arg0.getPoint();
        if ((arg0.isPopupTrigger()) & (popupMenu != null)) {
            Point pointInPanel = SwingUtilities.convertPoint(tree, pointInTree, tree);
            updatePopupMenu(tree.getSelectionPaths());
            tree.setFocusable(false);
            popupMenu.show(tree, pointInPanel.x, pointInPanel.y);
            tree.setFocusable(true);
        }
    }

    private void updatePopupMenu(TreePath[] paths) {
        if (paths != null) {
            if (paths.length <= 1) {
                if (((MutableTreeNode) paths[0].getLastPathComponent()).isLeaf()) popupMenuExecute.setText("Open"); else {
                    if (tree.isExpanded(paths[0])) popupMenuExecute.setText("Collapse"); else popupMenuExecute.setText("Expand");
                }
                popupMenu.removeAll();
                if ((!((MutableTreeNode) paths[0].getLastPathComponent()).isLeaf()) | (getSelectedFile() != null && getSelectedFile().exists() && !getSelectedFile().isDirectory())) popupMenu.add(popupMenuExecute);
                if ((getCurrentFile() != null && !getCurrentFile().isDirectory()) && (getInfo().getProjectManager().getFileAssociationsManager().getAssociatedEditors(FileUtils.getExtension(getCurrentFile())) != null) && (getInfo().getProjectManager().getFileAssociationsManager().getAssociatedEditors(FileUtils.getExtension(getCurrentFile())).size() > 1)) {
                    for (int i = 1; i < getInfo().getProjectManager().getFileAssociationsManager().getAssociatedEditors(FileUtils.getExtension(getCurrentFile())).size(); i++) {
                        final int editorID = getInfo().getProjectManager().getFileAssociationsManager().getAssociatedEditors(FileUtils.getExtension(getCurrentFile())).get(i);
                        ViewerEditor editor = getInfo().getEditor(editorID);
                        JMenuItem popupMenuOpenInEditor = new JMenuItem(editor.getPopupMenuText(), getInfo().getImage(editor.getIcon()));
                        popupMenuOpenInEditor.setMnemonic(editor.getPopupMenuMnemonic());
                        popupMenuOpenInEditor.setFont(plainFont);
                        popupMenuOpenInEditor.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent arg0) {
                                openInEditorAction(editorID);
                            }
                        });
                        if ((editor instanceof NetlistViewerEditor) && (getInfo().getEditor(FileAssociationsManager.EDITOR_GRAPHIC_NETLIST)).getCurrentFile() == null) popupMenuOpenInEditor.setEnabled(false);
                        popupMenu.add(popupMenuOpenInEditor);
                    }
                }
                popupMenu.add(popupMenuNew);
                popupMenu.addSeparator();
                popupMenu.add(popupMenuCut);
                popupMenu.add(popupMenuCopy);
                popupMenu.add(popupMenuPaste);
                popupMenu.add(popupMenuDelete);
                popupMenu.add(popupMenuRename);
                popupMenu.addSeparator();
                popupMenu.add(popupMenuImport);
                popupMenu.add(popupMenuExport);
                if (isProjectSelected()) popupMenu.add(popupMenuExportAsZip);
                popupMenu.addSeparator();
                popupMenu.add(popupMenuRefresh);
                popupMenu.addSeparator();
                popupMenu.add(popupMenuProperties);
            } else {
                popupMenu.removeAll();
                popupMenu.add(popupMenuCut);
                popupMenu.add(popupMenuCopy);
                popupMenu.add(popupMenuPaste);
                popupMenu.add(popupMenuDelete);
                popupMenu.addSeparator();
                popupMenu.add(popupMenuExport);
                popupMenu.addSeparator();
                popupMenu.add(popupMenuRefresh);
            }
        } else {
            popupMenu.removeAll();
            popupMenu.add(popupMenuNew);
            popupMenu.add(popupMenuOpen);
            popupMenu.addSeparator();
            popupMenu.add(popupMenuRefresh);
        }
    }

    protected boolean openFileInternal(File projectPath) throws Exception {
        this.projectPath = projectPath;
        refresh(false);
        return true;
    }

    public void refreshInternal(boolean workspace) {
        TreePath selectedPath = tree.getSelectionPath();
        ArrayList<TreePath> expandedPaths = new ArrayList<TreePath>();
        root = new ProjectTreeNode("root");
        if (((tree != null) & (treeModel != null)) && (treeModel.getRoot() != null)) {
            Enumeration<TreePath> e = tree.getExpandedDescendants(new TreePath(treeModel.getRoot()));
            if (e != null) while (e.hasMoreElements()) expandedPaths.add(e.nextElement());
        }
        if ((getInfo().getProjectManager().getProjectFile() == null) || (projectPath == null)) {
            projectPath = null;
            treeModel.setRoot(null);
            if (getInfo().getConfigurationManager().isProjectExplorerLibraryEnabled() && (!"".equals(getInfo().getConfigurationManager().getProjectExplorerLibraryPath()))) {
                addLibrary();
                treeModel.setRoot(root);
            }
            buttonHierarchicalLayout.setEnabled(false);
            return;
        }
        buttonHierarchicalLayout.setEnabled(true);
        if (projectPath.exists()) if (getTreeLayout() == LAYOUT_FLAT) root.add(getFlatProjectContents(null, getInfo().getProjectManager().getProjectName(), projectPath)); else if (getTreeLayout() == LAYOUT_HIERARCHICAL) root.add(getHierarchicalProjectContents(getInfo().getProjectManager().getProjectName()));
        if (getInfo().getConfigurationManager().isProjectExplorerLibraryEnabled() && (!"".equals(getInfo().getConfigurationManager().getProjectExplorerLibraryPath()))) addLibrary();
        treeModel.setRoot(null);
        treeModel.setRoot(root);
        for (int i = 0; i < expandedPaths.size(); i++) tree.expandPath(produceTreePath(expandedPaths.get(i)));
        tree.setSelectionPath(produceTreePath(selectedPath));
        if (getCurrentFile() != null) getInfo().getProjectManager().updateHash(getCurrentFile());
        getInfo().getProjectManager().refreshContents();
        if (!workspace) getInfo().refreshWorkspace(this);
    }

    private void addLibrary() {
        root.add(getFlatProjectContents(null, LIBRARY, new File(getInfo().getConfigurationManager().getProjectExplorerLibraryPath())));
    }

    protected void storeFileInternal(File file) throws IOException {
        getInfo().getProjectManager().exportFile(getSelectedFile(), file);
    }

    public void valueChanged(TreeSelectionEvent e) {
        if (tree.getSelectionPath() != null) {
            if (getSelectedFile() == null) return;
            setCurrentFile(getSelectedFile());
            getInfo().getStatusOutput().outputSpecificInfo(getSpecificInfo());
            if (isPseudoFolderSelected()) getInfo().getInfoViewerEditor().setInformation("<html><b>Group:</b>&nbsp;" + getCurrentFile().getName() + "</html>"); else getInfo().getInfoViewerEditor().setFileInformation(getCurrentFile());
            if (popupMenuNew != null) popupMenuNew.setEnabled(!getSelectedPackagePath().equals(getInfo().getConfigurationManager().getProjectExplorerLibraryPath()));
            if (popupMenuCut != null) popupMenuCut.setEnabled(getSelectedFile().exists() && !getSelectedPackagePath().equals(getInfo().getConfigurationManager().getProjectExplorerLibraryPath()));
            if (popupMenuCopy != null) popupMenuCopy.setEnabled(getSelectedFile().exists());
            if (popupMenuPaste != null) popupMenuPaste.setEnabled(getSelectedFile().exists() && !getSelectedPackagePath().equals(getInfo().getConfigurationManager().getProjectExplorerLibraryPath()));
            if (popupMenuDelete != null) popupMenuDelete.setEnabled(getSelectedFile().exists() && !getSelectedPackagePath().equals(getInfo().getConfigurationManager().getProjectExplorerLibraryPath()));
            if (popupMenuRename != null) popupMenuRename.setEnabled(getSelectedFile().exists() && !getSelectedPackagePath().equals(getInfo().getConfigurationManager().getProjectExplorerLibraryPath()));
            if (popupMenuImport != null) popupMenuImport.setEnabled(!getSelectedPackagePath().equals(getInfo().getConfigurationManager().getProjectExplorerLibraryPath()));
            if (popupMenuExport != null) popupMenuExport.setEnabled(getSelectedFile().exists());
            if (popupMenuProperties != null) popupMenuProperties.setEnabled(getSelectedFile().exists());
            fireContentChangedEvent();
        }
    }

    public void newFile() {
    }

    public void close() {
        getInfo().getProjectManager().closeProject();
    }

    public boolean isUndoRedoSupported() {
        return false;
    }

    public boolean isDeleteSupported() {
        return false;
    }

    public boolean isSelectAllSupported() {
        return false;
    }

    public Printable getPrintable() {
        String text = "";
        try {
            if (!isPrintingSupported()) return null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(getCurrentFile().toURI().toURL().openStream()));
            String line = "";
            while ((line = reader.readLine()) != null) text += line + "\n";
            text = text.trim();
        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
        if (text.length() == 0) return null;
        return new TextPrinter(text, new Font("Monospaced", Font.PLAIN, 13));
    }

    public void undo() {
    }

    public void redo() {
    }

    public void delete() {
    }

    public void selectAll() {
    }

    public void cut() {
        copy();
        wasCut = true;
    }

    public void copy() {
        getToolkit().getSystemClipboard().setContents(new StringSelection(FileUtils.getFileList(getSelectedFiles())), getInfo().getClipboardOwner());
    }

    public void paste() {
        try {
            File[] filesToPaste = FileUtils.getFilesFromList((String) getToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor));
            getInfo().getProjectManager().copyWithOverwrite(filesToPaste, getCurrentFolder(), wasCut);
            wasCut = false;
            refresh(false);
        } catch (Exception e) {
            return;
        }
    }

    public boolean isCutCopySupported() {
        return true;
    }

    public boolean isPasteSupported() {
        return true;
    }

    public MenuHandler getMenuHandler() {
        return this;
    }

    public String getName() {
        return "Project Explorer";
    }

    private int getTreeLayout() {
        return treeLayout;
    }

    private void setTreeLayout(int treeLayout) {
        this.treeLayout = treeLayout;
        refresh(false);
    }

    public void removeProjectGroup(String name) {
        projectGroups.remove(name);
    }

    protected boolean hasInformation() {
        return true;
    }

    public boolean isSavingSupported() {
        return false;
    }

    public boolean isNewFileSupported() {
        return false;
    }

    public String getIcon() {
        return "editors" + File.separator + "projectVE16x16.png";
    }

    protected void setToolbarEnabled(boolean enabled) {
    }
}
