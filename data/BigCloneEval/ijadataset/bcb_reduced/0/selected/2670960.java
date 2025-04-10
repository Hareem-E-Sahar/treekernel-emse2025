package jfpsm;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Panel that allows to manipulate the projects in a tree containing their
 * sub-components.
 * @author Julien Gouesse
 *
 */
public final class ProjectManager extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JTree projectsTree;

    private final MainWindow mainWindow;

    private boolean quitEnabled;

    private final ProgressDialog progressDialog;

    /**
	 * build a project manager
	 * @param mainWindow window that contains this manager
	 */
    public ProjectManager(final MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        quitEnabled = true;
        progressDialog = new ProgressDialog(mainWindow.getApplicativeFrame(), "Work in progress...");
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        final DefaultMutableTreeNode projectsRoot = new DefaultMutableTreeNode(new ProjectSet("Project Set"));
        projectsTree = new JTree(new DefaultTreeModel(projectsRoot));
        JScrollPane treePane = new JScrollPane(projectsTree);
        projectsTree.setShowsRootHandles(true);
        projectsTree.addTreeWillExpandListener(new TreeWillExpandListener() {

            @Override
            public final void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                if (((DefaultMutableTreeNode) event.getPath().getLastPathComponent()).getUserObject() instanceof ProjectSet) throw new ExpandVetoException(event);
            }

            @Override
            public final void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
            }
        });
        projectsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        loadExistingProjects();
        final JPopupMenu treePopupMenu = new JPopupMenu();
        final JMenuItem newMenuItem = new JMenuItem("New");
        final JMenuItem renameMenuItem = new JMenuItem("Rename");
        final JMenuItem importMenuItem = new JMenuItem("Import");
        final JMenuItem exportMenuItem = new JMenuItem("Export");
        final JMenuItem generateGameFilesMenuItem = new JMenuItem("Generate game files");
        final JMenuItem refreshMenuItem = new JMenuItem("Refresh");
        final JMenuItem openMenuItem = new JMenuItem("Open");
        final JMenuItem closeMenuItem = new JMenuItem("Close");
        final JMenuItem deleteMenuItem = new JMenuItem("Delete");
        final JMenuItem saveMenuItem = new JMenuItem("Save");
        treePopupMenu.add(newMenuItem);
        treePopupMenu.add(renameMenuItem);
        treePopupMenu.add(importMenuItem);
        treePopupMenu.add(exportMenuItem);
        treePopupMenu.add(generateGameFilesMenuItem);
        treePopupMenu.add(refreshMenuItem);
        treePopupMenu.add(openMenuItem);
        treePopupMenu.add(closeMenuItem);
        treePopupMenu.add(deleteMenuItem);
        treePopupMenu.add(saveMenuItem);
        newMenuItem.addActionListener(new CreateNewEntityFromSelectedEntityAction(this));
        renameMenuItem.addActionListener(new RenameSelectedEntityAction(this));
        importMenuItem.addActionListener(new ImportSelectedEntityAction(this));
        exportMenuItem.addActionListener(new ExportSelectedEntityAction(this));
        generateGameFilesMenuItem.addActionListener(new GenerateGameFilesAction(this));
        refreshMenuItem.addActionListener(new RefreshSelectedEntitiesAction(this));
        openMenuItem.addActionListener(new OpenSelectedEntitiesAction(this));
        closeMenuItem.addActionListener(new CloseSelectedEntitiesAction(this));
        deleteMenuItem.addActionListener(new DeleteSelectedEntitiesAction(this));
        saveMenuItem.addActionListener(new SaveSelectedEntitiesAction(this));
        projectsTree.setCellRenderer(new DefaultTreeCellRenderer() {

            private static final long serialVersionUID = 1L;

            private ImageIcon coloredTileLeafIcon = null;

            private Icon defaultLeafIcon = null;

            @Override
            public final Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                if (leaf) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                    Object userObject = node.getUserObject();
                    if (defaultLeafIcon == null) defaultLeafIcon = super.getLeafIcon();
                    if (userObject instanceof Tile) {
                        int w = super.getLeafIcon().getIconWidth(), h = super.getLeafIcon().getIconHeight();
                        if (coloredTileLeafIcon == null) {
                            BufferedImage coloredTileImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                            coloredTileLeafIcon = new ImageIcon(coloredTileImage);
                        }
                        Tile tile = (Tile) userObject;
                        for (int x = 0; x < w; x++) for (int y = 0; y < h; y++) ((BufferedImage) coloredTileLeafIcon.getImage()).setRGB(x, y, tile.getColor().getRGB());
                        setLeafIcon(coloredTileLeafIcon);
                    } else setLeafIcon(defaultLeafIcon);
                }
                return (super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus));
            }
        });
        projectsTree.addMouseListener(new MouseAdapter() {

            @Override
            public final void mousePressed(MouseEvent e) {
                handleMouseEvent(e);
            }

            @Override
            public final void mouseReleased(MouseEvent e) {
                handleMouseEvent(e);
            }

            private final void handleMouseEvent(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    TreePath[] paths = projectsTree.getSelectionPaths();
                    TreePath mouseOverPath = projectsTree.getClosestPathForLocation(e.getX(), e.getY());
                    if (mouseOverPath != null) {
                        boolean found = false;
                        if (paths != null) for (TreePath path : paths) if (path.equals(mouseOverPath)) {
                            found = true;
                            break;
                        }
                        if (!found) {
                            projectsTree.setSelectionPath(mouseOverPath);
                            paths = new TreePath[] { mouseOverPath };
                        }
                    }
                    if (paths != null) {
                        final boolean singleSelection = paths.length == 1;
                        final TreePath path = projectsTree.getSelectionPath();
                        final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                        final JFPSMUserObject userObject = (JFPSMUserObject) selectedNode.getUserObject();
                        final boolean showNew = singleSelection && userObject.canInstantiateChildren();
                        final boolean showImport = singleSelection && (userObject instanceof ProjectSet || userObject instanceof Map);
                        final boolean showExport = singleSelection && userObject instanceof Project || userObject instanceof Map;
                        final boolean showGenerateGameFiles = singleSelection && userObject instanceof Project;
                        final boolean showRefresh = singleSelection && userObject instanceof ProjectSet;
                        final boolean showRename = singleSelection && (userObject instanceof FloorSet || userObject instanceof Floor || userObject instanceof Tile);
                        final boolean showSave = singleSelection && userObject instanceof Project;
                        boolean showOpenAndClose;
                        showOpenAndClose = false;
                        JFPSMUserObject currentUserObject;
                        for (TreePath currentPath : paths) {
                            currentUserObject = (JFPSMUserObject) ((DefaultMutableTreeNode) currentPath.getLastPathComponent()).getUserObject();
                            if (currentUserObject.isOpenable()) {
                                showOpenAndClose = true;
                                break;
                            }
                        }
                        boolean showDelete;
                        showDelete = false;
                        for (TreePath currentPath : paths) {
                            currentUserObject = (JFPSMUserObject) ((DefaultMutableTreeNode) currentPath.getLastPathComponent()).getUserObject();
                            if (currentUserObject.isRemovable()) {
                                showDelete = true;
                                break;
                            }
                        }
                        newMenuItem.setVisible(showNew);
                        renameMenuItem.setVisible(showRename);
                        importMenuItem.setVisible(showImport);
                        exportMenuItem.setVisible(showExport);
                        generateGameFilesMenuItem.setVisible(showGenerateGameFiles);
                        refreshMenuItem.setVisible(showRefresh);
                        openMenuItem.setVisible(showOpenAndClose);
                        closeMenuItem.setVisible(showOpenAndClose);
                        deleteMenuItem.setVisible(showDelete);
                        saveMenuItem.setVisible(showSave);
                        treePopupMenu.show(mainWindow.getApplicativeFrame(), e.getXOnScreen(), e.getYOnScreen());
                    }
                } else if (e.getClickCount() == 2) {
                    final TreePath path = projectsTree.getSelectionPath();
                    final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                    final JFPSMUserObject userObject = (JFPSMUserObject) selectedNode.getUserObject();
                    if (userObject != null) {
                        final Project project = getProjectFromSelectedNode(selectedNode);
                        if (project != null) ProjectManager.this.mainWindow.getEntityViewer().openEntityView(userObject, project);
                    }
                }
            }
        });
        add(treePane);
    }

    private static final Project getProjectFromSelectedNode(final DefaultMutableTreeNode selectedNode) {
        DefaultMutableTreeNode node = selectedNode;
        Project project = null;
        Object userObject;
        while (node != null) {
            userObject = node.getUserObject();
            if (userObject != null && userObject instanceof Project) {
                project = (Project) userObject;
                break;
            } else node = (DefaultMutableTreeNode) node.getParent();
        }
        return (project);
    }

    /**
	 * get project names
	 * @return all project names in the tree
	 */
    private final ArrayList<String> getAllProjectNames() {
        return (getAllChildrenNames((DefaultMutableTreeNode) projectsTree.getModel().getRoot()));
    }

    private final ArrayList<String> getAllChildrenNames(DefaultMutableTreeNode parentNode) {
        ArrayList<String> namesList = new ArrayList<String>();
        final int size = parentNode.getChildCount();
        for (int index = 0; index < size; index++) namesList.add(((DefaultMutableTreeNode) parentNode.getChildAt(index)).getUserObject().toString());
        return (namesList);
    }

    /**
     * save all projects of the current projects set
     */
    final void saveCurrentWorkspace() {
        if (projectsTree != null) {
            DefaultTreeModel treeModel = (DefaultTreeModel) projectsTree.getModel();
            if (treeModel != null) {
                DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
                if (rootNode != null) {
                    ProjectSet projectSet = (ProjectSet) rootNode.getUserObject();
                    if (projectSet != null) {
                        if (projectSet.isDirty()) {
                            for (Project project : projectSet.getProjectsList()) if (project.isDirty()) {
                                try {
                                    projectSet.saveProject(project);
                                } catch (Throwable throwable) {
                                    displayErrorMessage(throwable, false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    final void displayErrorMessage(Throwable throwable, boolean fatal) {
        mainWindow.displayErrorMessage(throwable, fatal);
    }

    /**
     * load existing projects, skips already loaded projects
     */
    final void loadExistingProjects() {
        ProjectSet workspace = (ProjectSet) ((DefaultMutableTreeNode) projectsTree.getModel().getRoot()).getUserObject();
        for (String projectName : workspace.getProjectNames()) addProject(projectName);
    }

    private final Project addProject(String name) {
        Project project = null;
        if (!getAllProjectNames().contains(name)) {
            ProjectSet workspace = (ProjectSet) ((DefaultMutableTreeNode) projectsTree.getModel().getRoot()).getUserObject();
            String[] projectNames = workspace.getProjectNames();
            File[] projectFiles = workspace.getProjectFiles();
            File projectFile = null;
            for (int i = 0; i < projectNames.length; i++) if (projectNames[i].equals(name)) {
                projectFile = projectFiles[i];
                break;
            }
            if (projectFile != null) project = workspace.loadProject(projectFile); else {
                project = new Project(name);
                workspace.addProject(project);
            }
            DefaultTreeModel treeModel = (DefaultTreeModel) projectsTree.getModel();
            DefaultMutableTreeNode projectsRoot = (DefaultMutableTreeNode) treeModel.getRoot();
            DefaultMutableTreeNode projectRootNode = new DefaultMutableTreeNode(project);
            treeModel.insertNodeInto(projectRootNode, projectsRoot, projectsRoot.getChildCount());
            LevelSet levelSet = project.getLevelSet();
            DefaultMutableTreeNode levelSetNode = new DefaultMutableTreeNode(levelSet);
            treeModel.insertNodeInto(levelSetNode, projectRootNode, projectRootNode.getChildCount());
            DefaultMutableTreeNode floorSetRootNode;
            for (FloorSet floorSet : levelSet.getFloorSetsList()) {
                floorSetRootNode = new DefaultMutableTreeNode(floorSet);
                treeModel.insertNodeInto(floorSetRootNode, levelSetNode, levelSetNode.getChildCount());
                for (Floor floor : floorSet.getFloorsList()) {
                    DefaultMutableTreeNode floorNode = new DefaultMutableTreeNode(floor);
                    treeModel.insertNodeInto(floorNode, floorSetRootNode, floorSetRootNode.getChildCount());
                    DefaultMutableTreeNode mapNode;
                    for (MapType type : MapType.values()) {
                        mapNode = new DefaultMutableTreeNode(floor.getMap(type));
                        ((DefaultTreeModel) projectsTree.getModel()).insertNodeInto(mapNode, floorNode, floorNode.getChildCount());
                    }
                    TreePath floorPath = new TreePath(((DefaultTreeModel) projectsTree.getModel()).getPathToRoot(floorNode));
                    if (!projectsTree.isExpanded(floorPath)) projectsTree.expandPath(floorPath);
                }
                TreePath floorSetPath = new TreePath(treeModel.getPathToRoot(floorSetRootNode));
                if (!projectsTree.isExpanded(floorSetPath)) projectsTree.expandPath(floorSetPath);
            }
            TreePath levelSetPath = new TreePath(treeModel.getPathToRoot(levelSetNode));
            if (!projectsTree.isExpanded(levelSetPath)) projectsTree.expandPath(levelSetPath);
            TileSet tileSet = project.getTileSet();
            DefaultMutableTreeNode tilesRootNode = new DefaultMutableTreeNode(tileSet);
            treeModel.insertNodeInto(tilesRootNode, projectRootNode, projectRootNode.getChildCount());
            for (Tile tile : tileSet.getTilesList()) {
                DefaultMutableTreeNode tileNode = new DefaultMutableTreeNode(tile);
                treeModel.insertNodeInto(tileNode, tilesRootNode, tilesRootNode.getChildCount());
            }
            TreePath tileSetPath = new TreePath(treeModel.getPathToRoot(tilesRootNode));
            if (!projectsTree.isExpanded(tileSetPath)) projectsTree.expandPath(tileSetPath);
            TreePath rootPath = new TreePath(projectsRoot);
            if (!projectsTree.isExpanded(rootPath)) projectsTree.expandPath(rootPath);
            TreePath projectPath = new TreePath(new Object[] { projectsRoot, projectRootNode });
            projectsTree.expandPath(projectPath);
        }
        return (project);
    }

    final Namable createNewEntityFromSelectedEntity() {
        TreePath path = projectsTree.getSelectionPath();
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObject = selectedNode.getUserObject();
        NamingDialog enterNameDialog = null;
        Namable newlyCreatedEntity = null;
        if (userObject instanceof ProjectSet) enterNameDialog = new NamingDialog(mainWindow.getApplicativeFrame(), getAllProjectNames(), "project"); else if (userObject instanceof FloorSet) enterNameDialog = new NamingDialog(mainWindow.getApplicativeFrame(), getAllChildrenNames(selectedNode), "floor"); else if (userObject instanceof TileSet) {
            final ArrayList<Color> colors = new ArrayList<Color>();
            colors.add(Color.WHITE);
            for (Tile tile : ((TileSet) userObject).getTilesList()) colors.add(tile.getColor());
            enterNameDialog = new TileCreationDialog(mainWindow.getApplicativeFrame(), getAllChildrenNames(selectedNode), colors);
        } else if (userObject instanceof LevelSet) enterNameDialog = new NamingDialog(mainWindow.getApplicativeFrame(), getAllChildrenNames(selectedNode), "level");
        if (enterNameDialog != null) {
            enterNameDialog.setVisible(true);
            String name = enterNameDialog.getValidatedText();
            enterNameDialog.dispose();
            if (name != null) {
                if (userObject instanceof ProjectSet) newlyCreatedEntity = addProject(name); else if (userObject instanceof FloorSet) {
                    Floor floor = new Floor(name);
                    ((FloorSet) userObject).addFloor(floor);
                    DefaultMutableTreeNode floorNode = new DefaultMutableTreeNode(floor);
                    ((DefaultTreeModel) projectsTree.getModel()).insertNodeInto(floorNode, selectedNode, selectedNode.getChildCount());
                    DefaultMutableTreeNode mapNode;
                    for (MapType type : MapType.values()) {
                        mapNode = new DefaultMutableTreeNode(floor.getMap(type));
                        ((DefaultTreeModel) projectsTree.getModel()).insertNodeInto(mapNode, floorNode, floorNode.getChildCount());
                    }
                    TreePath floorSetPath = new TreePath(((DefaultTreeModel) projectsTree.getModel()).getPathToRoot(selectedNode));
                    if (!projectsTree.isExpanded(floorSetPath)) projectsTree.expandPath(floorSetPath);
                    TreePath floorPath = new TreePath(((DefaultTreeModel) projectsTree.getModel()).getPathToRoot(floorNode));
                    if (!projectsTree.isExpanded(floorPath)) projectsTree.expandPath(floorPath);
                    newlyCreatedEntity = floor;
                } else if (userObject instanceof TileSet) {
                    Tile tile = new Tile(name);
                    tile.setColor(((TileCreationDialog) enterNameDialog).getValidatedColor());
                    ((TileSet) userObject).addTile(tile);
                    DefaultMutableTreeNode tileNode = new DefaultMutableTreeNode(tile);
                    ((DefaultTreeModel) projectsTree.getModel()).insertNodeInto(tileNode, selectedNode, selectedNode.getChildCount());
                    TreePath tileSetPath = new TreePath(((DefaultTreeModel) projectsTree.getModel()).getPathToRoot(selectedNode));
                    if (!projectsTree.isExpanded(tileSetPath)) projectsTree.expandPath(tileSetPath);
                    newlyCreatedEntity = tile;
                } else if (userObject instanceof LevelSet) {
                    FloorSet floorSet = new FloorSet(name);
                    ((LevelSet) userObject).addFloorSet(floorSet);
                    DefaultMutableTreeNode floorSetNode = new DefaultMutableTreeNode(floorSet);
                    ((DefaultTreeModel) projectsTree.getModel()).insertNodeInto(floorSetNode, selectedNode, selectedNode.getChildCount());
                    TreePath floorSetPath = new TreePath(((DefaultTreeModel) projectsTree.getModel()).getPathToRoot(selectedNode));
                    if (!projectsTree.isExpanded(floorSetPath)) projectsTree.expandPath(floorSetPath);
                    newlyCreatedEntity = floorSet;
                }
            }
        }
        return (newlyCreatedEntity);
    }

    final void saveSelectedEntities() {
        TreePath[] paths = projectsTree.getSelectionPaths();
        DefaultMutableTreeNode selectedNode;
        Object userObject;
        for (TreePath path : paths) {
            selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
            userObject = selectedNode.getUserObject();
            Project project = null;
            ProjectSet projectSet = null;
            if (userObject instanceof Project) {
                project = (Project) userObject;
                projectSet = (ProjectSet) ((DefaultMutableTreeNode) selectedNode.getParent()).getUserObject();
                try {
                    projectSet.saveProject(project);
                } catch (Throwable throwable) {
                    displayErrorMessage(throwable, false);
                }
            }
        }
    }

    final void exportSelectedEntity() {
        TreePath path = projectsTree.getSelectionPath();
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        JFPSMUserObject userObject = (JFPSMUserObject) selectedNode.getUserObject();
        if (userObject instanceof Project) {
            Project project = (Project) userObject;
            ProjectSet projectSet = (ProjectSet) ((DefaultMutableTreeNode) selectedNode.getParent()).getUserObject();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setFileFilter(new FileNameExtensionFilter("JFPSM Projects", "jfpsm.zip"));
            int result = fileChooser.showSaveDialog(mainWindow.getApplicativeFrame());
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    projectSet.saveProject(project, fileChooser.getSelectedFile());
                } catch (Throwable throwable) {
                    displayErrorMessage(throwable, false);
                }
            }
        } else if (userObject instanceof Map) {
            Map map = (Map) userObject;
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "bmp", "gif", "jpg", "jpeg", "png"));
            int result = fileChooser.showSaveDialog(mainWindow.getApplicativeFrame());
            if (result == JFileChooser.APPROVE_OPTION) {
                File imageFile = fileChooser.getSelectedFile();
                int lastIndexOfDot = imageFile.getName().lastIndexOf(".");
                String formatName = lastIndexOfDot >= 0 ? imageFile.getName().substring(lastIndexOfDot + 1) : "";
                try {
                    if (formatName.equals("")) throw new UnsupportedOperationException("Cannot export an image into a file without extension"); else ImageIO.write(map.getImage(), formatName, imageFile);
                } catch (Throwable throwable) {
                    displayErrorMessage(throwable, false);
                }
            }
        }
    }

    final void importSelectedEntity() {
        TreePath path = projectsTree.getSelectionPath();
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object userObject = selectedNode.getUserObject();
        if (userObject instanceof ProjectSet) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setFileFilter(new FileNameExtensionFilter("JFPSM Projects", "jfpsm.zip"));
            int result = fileChooser.showOpenDialog(mainWindow.getApplicativeFrame());
            if (result == JFileChooser.APPROVE_OPTION) {
                String fullname = fileChooser.getSelectedFile().getName();
                String projectName = fullname.substring(0, fullname.length() - Project.getFileExtension().length());
                ProjectSet workspace = (ProjectSet) userObject;
                boolean confirmLoad = true;
                if (Arrays.asList(workspace.getProjectNames()).contains(projectName)) {
                    confirmLoad = JOptionPane.showConfirmDialog(mainWindow.getApplicativeFrame(), "Overwrite project \"" + projectName + "\"" + "?", "Overwrite project", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
                    if (confirmLoad) {
                        final int count = selectedNode.getChildCount();
                        DefaultMutableTreeNode projectNode = null;
                        for (int i = 0; i < count; i++) if (((Project) ((DefaultMutableTreeNode) selectedNode.getChildAt(i)).getUserObject()).getName().equals(projectName)) {
                            projectNode = (DefaultMutableTreeNode) selectedNode.getChildAt(i);
                            break;
                        }
                        Project project = (Project) projectNode.getUserObject();
                        for (FloorSet floorSet : project.getLevelSet().getFloorSetsList()) for (Floor floor : floorSet.getFloorsList()) mainWindow.getEntityViewer().closeEntityView(floor);
                        for (Tile tile : project.getTileSet().getTilesList()) mainWindow.getEntityViewer().closeEntityView(tile);
                        workspace.removeProject(project);
                        ((DefaultTreeModel) projectsTree.getModel()).removeNodeFromParent(projectNode);
                    }
                }
                if (confirmLoad) {
                    File projectFile = new File(workspace.createProjectPath(projectName));
                    boolean success = true;
                    try {
                        success = projectFile.createNewFile();
                        if (success) {
                            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileChooser.getSelectedFile()));
                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(projectFile));
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = bis.read(buf)) > 0) bos.write(buf, 0, len);
                            bis.close();
                            bos.close();
                        }
                    } catch (Throwable throwable) {
                        displayErrorMessage(throwable, false);
                        success = false;
                    }
                    if (success) addProject(projectName);
                }
            }
        } else if (userObject instanceof Map) {
            Map map = (Map) userObject;
            Floor floor = (Floor) ((DefaultMutableTreeNode) selectedNode.getParent()).getUserObject();
            importImageForSelectedMap(floor, map);
        }
    }

    final BufferedImage openFileAndLoadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "bmp", "gif", "jpg", "jpeg", "png"));
        int result = fileChooser.showOpenDialog(mainWindow.getApplicativeFrame());
        BufferedImage image = null;
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                image = ImageIO.read(fileChooser.getSelectedFile());
            } catch (Throwable throwable) {
                displayErrorMessage(throwable, false);
            }
        }
        return (image);
    }

    private final void importImageForSelectedMap(Floor floor, Map map) {
        BufferedImage imageMap = openFileAndLoadImage();
        if (imageMap != null) {
            map.setImage(imageMap);
            int maxWidth = 0, maxHeight = 0, rgb;
            Map currentMap;
            BufferedImage nextImageMap;
            for (MapType currentType : MapType.values()) {
                currentMap = floor.getMap(currentType);
                maxWidth = Math.max(currentMap.getWidth(), maxWidth);
                maxHeight = Math.max(currentMap.getHeight(), maxHeight);
            }
            for (MapType currentType : MapType.values()) {
                currentMap = floor.getMap(currentType);
                if (currentMap.getWidth() != maxWidth || maxHeight != currentMap.getHeight()) {
                    nextImageMap = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
                    for (int x = 0; x < nextImageMap.getWidth(); x++) for (int y = 0; y < nextImageMap.getHeight(); y++) {
                        if (x < currentMap.getWidth() && y < currentMap.getHeight()) rgb = currentMap.getImage().getRGB(x, y); else rgb = Color.WHITE.getRGB();
                        nextImageMap.setRGB(x, y, rgb);
                    }
                    floor.getMap(currentType).setImage(nextImageMap);
                }
            }
            mainWindow.getEntityViewer().repaint();
        }
    }

    private final void expandPathDeeplyFromPath(TreePath path) {
        projectsTree.expandPath(path);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        DefaultTreeModel treeModel = (DefaultTreeModel) projectsTree.getModel();
        for (int i = 0; i < node.getChildCount(); i++) expandPathDeeplyFromPath(new TreePath(treeModel.getPathToRoot(node.getChildAt(i))));
    }

    final void openSelectedEntities() {
        TreePath[] paths = projectsTree.getSelectionPaths();
        DefaultMutableTreeNode selectedNode;
        JFPSMUserObject userObject;
        for (TreePath path : paths) {
            selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
            userObject = (JFPSMUserObject) selectedNode.getUserObject();
            if (userObject instanceof Project || userObject instanceof LevelSet || userObject instanceof FloorSet || userObject instanceof TileSet || userObject instanceof Floor) expandPathDeeplyFromPath(path);
            if (userObject instanceof Tile) {
                Project project = (Project) ((DefaultMutableTreeNode) selectedNode.getParent().getParent()).getUserObject();
                ProjectManager.this.mainWindow.getEntityViewer().openEntityView(userObject, project);
            } else if (userObject instanceof Floor) {
                Project project = (Project) ((DefaultMutableTreeNode) selectedNode.getParent().getParent().getParent()).getUserObject();
                ProjectManager.this.mainWindow.getEntityViewer().openEntityView(userObject, project);
            }
        }
    }

    final void closeSelectedEntities() {
        TreePath[] paths = projectsTree.getSelectionPaths();
        DefaultMutableTreeNode selectedNode;
        Object userObject;
        for (TreePath path : paths) {
            selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
            userObject = selectedNode.getUserObject();
            if (userObject instanceof Project || userObject instanceof LevelSet || userObject instanceof FloorSet || userObject instanceof TileSet || userObject instanceof Floor) {
                projectsTree.collapsePath(path);
                if (userObject instanceof LevelSet) {
                    for (FloorSet floorSet : ((LevelSet) userObject).getFloorSetsList()) for (Floor floor : floorSet.getFloorsList()) mainWindow.getEntityViewer().closeEntityView(floor);
                } else if (userObject instanceof FloorSet) {
                    for (Floor floor : ((FloorSet) userObject).getFloorsList()) mainWindow.getEntityViewer().closeEntityView(floor);
                } else if (userObject instanceof TileSet) {
                    for (Tile tile : ((TileSet) userObject).getTilesList()) mainWindow.getEntityViewer().closeEntityView(tile);
                } else if (userObject instanceof Project) {
                    Project project = (Project) userObject;
                    for (FloorSet floorSet : project.getLevelSet().getFloorSetsList()) for (Floor floor : floorSet.getFloorsList()) mainWindow.getEntityViewer().closeEntityView(floor);
                    for (Tile tile : project.getTileSet().getTilesList()) mainWindow.getEntityViewer().closeEntityView(tile);
                }
            }
            if (userObject instanceof Floor || userObject instanceof Tile) mainWindow.getEntityViewer().closeEntityView((Namable) userObject);
        }
    }

    final void renameSelectedEntity() {
        TreePath path = projectsTree.getSelectionPath();
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        JFPSMUserObject userObject = (JFPSMUserObject) selectedNode.getUserObject();
        NamingDialog enterNameDialog = null;
        if (userObject instanceof Floor) {
            enterNameDialog = new NamingDialog(mainWindow.getApplicativeFrame(), getAllChildrenNames(selectedNode), "floor");
            enterNameDialog.setTitle("Rename floor");
        } else if (userObject instanceof Tile) {
            enterNameDialog = new NamingDialog(mainWindow.getApplicativeFrame(), getAllChildrenNames(selectedNode), "tile");
            enterNameDialog.setTitle("Rename tile");
        } else if (userObject instanceof FloorSet) {
            enterNameDialog = new NamingDialog(mainWindow.getApplicativeFrame(), getAllChildrenNames(selectedNode), "level");
            enterNameDialog.setTitle("Rename level");
        }
        if (enterNameDialog != null) {
            enterNameDialog.setVisible(true);
            String name = enterNameDialog.getValidatedText();
            enterNameDialog.dispose();
            if (name != null) {
                userObject.setName(name);
                mainWindow.getEntityViewer().renameEntityView(userObject);
            }
        }
    }

    final Color getSelectedTileColor(final Project project) {
        Color color = null;
        TreePath[] paths = projectsTree.getSelectionPaths();
        DefaultMutableTreeNode selectedNode;
        Object userObject;
        for (TreePath path : paths) {
            selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
            userObject = selectedNode.getUserObject();
            if (userObject instanceof Tile && ((DefaultMutableTreeNode) selectedNode.getParent().getParent()).getUserObject() == project) {
                color = ((Tile) userObject).getColor();
                break;
            }
        }
        return (color);
    }

    final void deleteSelectedEntities() {
        TreePath[] paths = projectsTree.getSelectionPaths();
        DefaultMutableTreeNode selectedNode;
        JFPSMUserObject userObject;
        ArrayList<DefaultMutableTreeNode> floorsTrashList = new ArrayList<DefaultMutableTreeNode>();
        ArrayList<DefaultMutableTreeNode> floorSetsTrashList = new ArrayList<DefaultMutableTreeNode>();
        ArrayList<DefaultMutableTreeNode> tilesTrashList = new ArrayList<DefaultMutableTreeNode>();
        ArrayList<DefaultMutableTreeNode> projectsTrashList = new ArrayList<DefaultMutableTreeNode>();
        for (TreePath path : paths) {
            selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
            userObject = (JFPSMUserObject) selectedNode.getUserObject();
            if (userObject instanceof Tile) tilesTrashList.add(selectedNode); else if (userObject instanceof Floor) floorsTrashList.add(selectedNode); else if (userObject instanceof FloorSet) floorSetsTrashList.add(selectedNode); else if (userObject instanceof Project) projectsTrashList.add(selectedNode);
        }
        final int elementsCount = floorsTrashList.size() + floorSetsTrashList.size() + tilesTrashList.size() + projectsTrashList.size();
        if (elementsCount >= 1) {
            StringBuffer entitiesBuffer = new StringBuffer();
            for (int index = 0; index < floorsTrashList.size(); index++) entitiesBuffer.append(", \"" + floorsTrashList.get(index).getUserObject().toString() + "\"");
            for (int index = 0; index < floorSetsTrashList.size(); index++) entitiesBuffer.append(", \"" + floorSetsTrashList.get(index).getUserObject().toString() + "\"");
            for (int index = 0; index < tilesTrashList.size(); index++) entitiesBuffer.append(", \"" + tilesTrashList.get(index).getUserObject().toString() + "\"");
            for (int index = 0; index < projectsTrashList.size(); index++) entitiesBuffer.append(", \"" + projectsTrashList.get(index).getUserObject().toString() + "\"");
            entitiesBuffer.delete(0, 2);
            String questionStart;
            final boolean noFloor = floorsTrashList.isEmpty();
            final boolean noLevel = floorSetsTrashList.isEmpty();
            final boolean noProject = projectsTrashList.isEmpty();
            final boolean noTile = tilesTrashList.isEmpty();
            if (noFloor && noProject && noLevel) questionStart = "Delete tile"; else if (noTile && noProject && noLevel) questionStart = "Delete floor"; else if (noFloor && noTile && noLevel) questionStart = "Delete project"; else if (noFloor && noTile && noProject) questionStart = "Delete level"; else questionStart = "Delete element";
            if (elementsCount > 1) questionStart += "s";
            String windowTitle = "Confirm " + questionStart.toLowerCase();
            if (JOptionPane.showConfirmDialog(mainWindow.getApplicativeFrame(), questionStart + " " + entitiesBuffer.toString() + "?", windowTitle, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                final DefaultTreeModel treeModel = (DefaultTreeModel) projectsTree.getModel();
                for (DefaultMutableTreeNode node : tilesTrashList) {
                    TileSet tileSet = (TileSet) ((DefaultMutableTreeNode) node.getParent()).getUserObject();
                    Tile tile = (Tile) node.getUserObject();
                    mainWindow.getEntityViewer().closeEntityView(tile);
                    tileSet.removeTile(tile);
                    treeModel.removeNodeFromParent(node);
                }
                for (DefaultMutableTreeNode node : floorsTrashList) {
                    FloorSet floorSet = (FloorSet) ((DefaultMutableTreeNode) node.getParent()).getUserObject();
                    Floor floor = (Floor) node.getUserObject();
                    mainWindow.getEntityViewer().closeEntityView(floor);
                    floorSet.removeFloor(floor);
                    treeModel.removeNodeFromParent(node);
                }
                for (DefaultMutableTreeNode node : floorSetsTrashList) {
                    LevelSet levelSet = (LevelSet) ((DefaultMutableTreeNode) node.getParent()).getUserObject();
                    FloorSet floorSet = (FloorSet) node.getUserObject();
                    for (Floor floor : floorSet.getFloorsList()) mainWindow.getEntityViewer().closeEntityView(floor);
                    floorSet.removeAllFloors();
                    levelSet.removeFloorSet(floorSet);
                    treeModel.removeNodeFromParent(node);
                }
                for (DefaultMutableTreeNode node : projectsTrashList) {
                    Project project = (Project) node.getUserObject();
                    ProjectSet projectSet = (ProjectSet) ((DefaultMutableTreeNode) node.getParent()).getUserObject();
                    for (FloorSet floorSet : project.getLevelSet().getFloorSetsList()) for (Floor floor : floorSet.getFloorsList()) mainWindow.getEntityViewer().closeEntityView(floor);
                    for (Tile tile : project.getTileSet().getTilesList()) mainWindow.getEntityViewer().closeEntityView(tile);
                    projectSet.removeProject(project);
                    treeModel.removeNodeFromParent(node);
                }
            }
        }
    }

    final String createRawDataPath(String name) {
        ProjectSet workspace = (ProjectSet) ((DefaultMutableTreeNode) projectsTree.getModel().getRoot()).getUserObject();
        return (workspace.createRawDataPath(name));
    }

    final synchronized boolean isQuitEnabled() {
        return (quitEnabled);
    }

    final synchronized void setQuitEnabled(final boolean quitEnabled) {
        this.quitEnabled = quitEnabled;
    }

    /**
     * generate level files one by one
     */
    final void generateGameFiles() {
        TreePath path = projectsTree.getSelectionPath();
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        JFPSMUserObject userObject = (JFPSMUserObject) selectedNode.getUserObject();
        if (userObject instanceof Project) {
            final Project project = (Project) userObject;
            new GameFileExportSwingWorker(this, project, progressDialog).execute();
        }
    }

    private static final class GameFileExportSwingWorker extends SwingWorker<ArrayList<String>, String> {

        private final ArrayList<FloorSet> levelsList;

        private final ProjectManager projectManager;

        private final Project project;

        private final ProgressDialog dialog;

        private GameFileExportSwingWorker(final ProjectManager projectManager, final Project project, final ProgressDialog dialog) {
            this.levelsList = project.getLevelSet().getFloorSetsList();
            this.projectManager = projectManager;
            this.project = project;
            this.dialog = dialog;
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public final void run() {
                    dialog.reset();
                    dialog.setVisible(true);
                }
            });
        }

        @Override
        protected final ArrayList<String> doInBackground() {
            projectManager.setQuitEnabled(false);
            ArrayList<String> filenamesList = new ArrayList<String>();
            File levelFile, levelCollisionFile;
            int levelIndex = 0;
            for (FloorSet level : levelsList) {
                levelFile = new File(projectManager.createRawDataPath(level.getName() + ".abin"));
                levelCollisionFile = new File(projectManager.createRawDataPath(level.getName() + ".collision.abin"));
                try {
                    GameFilesGenerator.getInstance().writeLevel(level, levelIndex, project, levelFile, levelCollisionFile);
                } catch (Throwable throwable) {
                    projectManager.displayErrorMessage(throwable, false);
                }
                filenamesList.add(level.getName());
                publish(level.getName());
                setProgress(100 * filenamesList.size() / levelsList.size());
                levelIndex++;
            }
            return (filenamesList);
        }

        @Override
        protected final void process(List<String> chunks) {
            StringBuilder builder = new StringBuilder();
            for (String chunk : chunks) {
                builder.append(chunk);
                builder.append(" ");
            }
            dialog.setText(builder.toString());
            dialog.setValue(100 * chunks.size() / levelsList.size());
        }

        @Override
        protected final void done() {
            projectManager.setQuitEnabled(true);
            dialog.setVisible(false);
            dialog.reset();
        }
    }
}
