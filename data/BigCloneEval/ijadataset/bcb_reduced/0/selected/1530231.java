package cz.zcu.fav.hofhans.packer.view;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import cz.zcu.fav.hofhans.packer.PackerConfiguration;
import cz.zcu.fav.hofhans.packer.bdo.Context;
import cz.zcu.fav.hofhans.packer.bdo.PackerFile;
import cz.zcu.fav.hofhans.packer.bdo.PackerFolder;
import cz.zcu.fav.hofhans.packer.bdo.PackerItem;
import cz.zcu.fav.hofhans.packer.bdo.Context.ContextChanges;
import cz.zcu.fav.hofhans.packer.component.IconListCellRenderer;
import cz.zcu.fav.hofhans.packer.component.IconTreeCellRenderer;
import cz.zcu.fav.hofhans.packer.component.PackerFolderTreeModel;
import cz.zcu.fav.hofhans.packer.component.PackerFolderTreeNode;
import cz.zcu.fav.hofhans.packer.component.PackerItemListModel;
import cz.zcu.fav.hofhans.packer.exception.PackerRuntimeException;
import cz.zcu.fav.hofhans.packer.exception.ValidationException;

/**
 * Perspective for browsing structure.
 * @author Tomáš Hofhans
 * @since 6.1.2010
 */
public class BrowsePerspective extends PackerAbstractView implements Observer {

    private static final Logger LOG = Logger.getLogger(BrowsePerspective.class.getName());

    private JPanel panel;

    private static BrowsePerspective instance;

    private GridBagConstraints gbc;

    private JTree tree;

    private JList content;

    private JPopupMenu folderPopUp;

    private JPopupMenu filePopUp;

    private PackerFolderTreeNode selectedFolderNode;

    private PackerItem selectedItem;

    private JMenuItem createFolder;

    private JMenuItem deleteFolder;

    private JMenuItem renameFolder;

    private JMenuItem createFile;

    private JMenuItem deleteFile;

    private JMenuItem renameFile;

    private JMenuItem createFolder2;

    private JMenuItem deleteFolder2;

    private JMenuItem renameFolder2;

    private JMenuItem createFile2;

    private CreateFolderAction createFolderAction;

    private DeleteFolderAction deleteFolderAction;

    private RenameFolderAction renameFolderAction;

    private CreateItemsAction createFileAction;

    private DeleteFileAction deleteFileAction;

    private RenameFileAction renameFileAction;

    /** Hidden constructor. */
    private BrowsePerspective() {
        super();
        init();
    }

    /** Initialize perspective. */
    private void init() {
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        PackerFolderTreeNode rootNode = PackerFolderTreeNode.getRootNode();
        PackerFolderTreeModel model = new PackerFolderTreeModel(rootNode);
        tree = new JTree(model);
        JScrollPane treeScrollPane = new JScrollPane(tree);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        TreeCellRenderer treeRenderer = IconTreeCellRenderer.getInstance();
        tree.setCellRenderer(treeRenderer);
        content = new JList() {

            private static final long serialVersionUID = 1L;

            /** {@inheritDoc} */
            public String getToolTipText(MouseEvent evt) {
                int index = locationToIndex(evt.getPoint());
                if (index != -1) {
                    PackerItem item = (PackerItem) content.getModel().getElementAt(index);
                    if (item instanceof PackerFile) {
                        PackerFile packerFile = (PackerFile) item;
                        return packerFile.getCompression().toString();
                    }
                }
                return super.getToolTipText();
            }
        };
        JScrollPane contentScrollPane = new JScrollPane(content);
        content.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListCellRenderer renderer = IconListCellRenderer.getInstance();
        content.setCellRenderer(renderer);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, contentScrollPane);
        add(split, 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        split.setDividerLocation(150);
        tree.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.config("Tree selected item changed.");
                }
                if (tree.getSelectionPath() != null) {
                    PackerFolderTreeNode pftn = (PackerFolderTreeNode) tree.getSelectionPath().getLastPathComponent();
                    selectedFolderNode = pftn;
                    content.setModel(pftn.getItemsModel());
                }
            }
        });
        folderPopUp = new JPopupMenu();
        filePopUp = new JPopupMenu();
        MouseListener ml = new BrowserMouseAdapter();
        tree.addMouseListener(ml);
        content.addMouseListener(ml);
        createFolderAction = new CreateFolderAction();
        deleteFolderAction = new DeleteFolderAction();
        renameFolderAction = new RenameFolderAction();
        createFileAction = new CreateItemsAction();
        deleteFileAction = new DeleteFileAction();
        renameFileAction = new RenameFileAction();
        createFolder = new JMenuItem(createFolderAction);
        renameFolder = new JMenuItem(renameFolderAction);
        deleteFolder = new JMenuItem(deleteFolderAction);
        createFile = new JMenuItem(createFileAction);
        deleteFile = new JMenuItem(deleteFileAction);
        renameFile = new JMenuItem(renameFileAction);
        createFolder2 = new JMenuItem(createFolderAction);
        renameFolder2 = new JMenuItem(renameFolderAction);
        deleteFolder2 = new JMenuItem(deleteFolderAction);
        createFile2 = new JMenuItem(createFileAction);
        filePopUp.add(createFile);
        filePopUp.add(renameFile);
        filePopUp.add(deleteFile);
        filePopUp.addSeparator();
        filePopUp.add(createFolder);
        filePopUp.add(renameFolder);
        filePopUp.add(deleteFolder);
        folderPopUp.add(createFile2);
        folderPopUp.addSeparator();
        folderPopUp.add(createFolder2);
        folderPopUp.add(renameFolder2);
        folderPopUp.add(deleteFolder2);
        TransferHandler transfer = new PackerTransferHandler();
        tree.setSelectionPath(rootNode.getPath());
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON);
        tree.setTransferHandler(transfer);
        content.setDragEnabled(true);
        content.setDropMode(DropMode.ON);
        content.setTransferHandler(transfer);
        Context c = Context.getInstance();
        panel.setLocale(c.getLocale());
        localize();
    }

    /**
   * Set actual directory. Folder couldn't be null.
   * @param folder
   */
    private void setActualDir(PackerFolder folder) {
        List<PackerFolder> path = new ArrayList<PackerFolder>();
        for (PackerFolder pf = folder; pf != null; pf = pf.getParent()) {
            path.add(pf);
        }
        PackerFolderTreeNode node = (PackerFolderTreeNode) tree.getModel().getRoot();
        assert path.size() != 0 : "No root folder";
        PackerFolderTreeNode[] treePathArray = new PackerFolderTreeNode[path.size()];
        treePathArray[0] = node;
        for (int i = path.size() - 1; i != 0; i--) {
            node = node.getChildNode(path.get(i - 1));
            treePathArray[path.size() - i] = node;
        }
        TreePath treePath = new TreePath(treePathArray);
        tree.expandPath(treePath);
        tree.setSelectionPath(treePath);
    }

    /** Add component to layout. */
    private void add(Component c, int x, int y, int s, int v, double rs, double rv, int fill, int k) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = s;
        gbc.gridheight = v;
        gbc.weightx = rs;
        gbc.weighty = rv;
        gbc.fill = fill;
        gbc.anchor = k;
        panel.add(c, gbc);
    }

    /** {@inheritDoc} */
    public void localize() {
        ResourceBundle resource = ResourceBundle.getBundle("cz.zcu.fav.hofhans.resources.view.ContextMenu", panel.getLocale());
        createFolder.setText(resource.getString("createFolder"));
        renameFolder.setText(resource.getString("renameFolder"));
        deleteFolder.setText(resource.getString("deleteFolder"));
        createFile.setText(resource.getString("addData"));
        deleteFile.setText(resource.getString("deleteFile"));
        renameFile.setText(resource.getString("renameFile"));
        createFolder2.setText(resource.getString("createFolder"));
        renameFolder2.setText(resource.getString("renameFolder"));
        deleteFolder2.setText(resource.getString("deleteFolder"));
        createFile2.setText(resource.getString("addData"));
    }

    /** {@inheritDoc} */
    public void addObserver(Observer o) {
        observable.addObserver(o);
    }

    /** {@inheritDoc} */
    public Component getComponent() {
        return panel;
    }

    @Override
    public void activate() {
        Context context = Context.getInstance();
        context.addObserver(this);
    }

    @Override
    public void deactivate() {
        Context context = Context.getInstance();
        context.deleteObserver(this);
    }

    /**
   * Factory method for getting singleton.
   * @return instance
   */
    public static PackerView getInstance() {
        if (instance == null) {
            synchronized (BrowsePerspective.class) {
                if (instance == null) {
                    instance = new BrowsePerspective();
                }
            }
        }
        return instance;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof ContextChanges) {
            assert o instanceof Context : "Invalid observable";
            Context context = (Context) o;
            ContextChanges change = (ContextChanges) arg;
            switch(change) {
                case ACTUAL_DIR:
                    setActualDir(context.getActualFolder());
                    break;
                case DATA:
                    break;
            }
        }
    }

    /**
   * Create folder action.
   * @author Tomáš Hofhans
   * @since 12.1.2010
   */
    private class CreateFolderAction extends AbstractAction {

        /** SVUID. */
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            String name = loadString("createFolder", "setFolderName", "");
            if (name != null) {
                ResourceBundle resource = ResourceBundle.getBundle("cz.zcu.fav.hofhans.resources.view.Dialogs", panel.getLocale());
                if (name.equals("")) {
                    JOptionPane.showMessageDialog(panel, resource.getString("emptyValue"), resource.getString("inputError"), JOptionPane.ERROR_MESSAGE);
                } else {
                    PackerFolderTreeNode parent = null;
                    if (selectedItem == null || !(selectedItem instanceof PackerFolder)) {
                        parent = selectedFolderNode;
                    } else {
                        parent = selectedFolderNode.getChildNode((PackerFolder) selectedItem);
                    }
                    PackerFolder newFolder = new PackerFolder(name, parent.getFolder());
                    try {
                        PackerFolderTreeNode newNode = ((PackerFolderTreeModel) tree.getModel()).insert(parent, newFolder);
                        content.setSelectedValue(newNode.getFolder(), true);
                    } catch (ValidationException e1) {
                        JOptionPane.showMessageDialog(panel, e1.getLocalizedMessage(), resource.getString("validationError"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    /**
   * Delete folder action.
   * @author Tomáš Hofhans
   * @since 12.1.2010
   */
    private class DeleteFolderAction extends AbstractAction {

        /** SVUID. */
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            PackerFolderTreeNode node = null;
            if (selectedItem == null || !(selectedItem instanceof PackerFolder)) {
                node = selectedFolderNode;
            } else {
                node = selectedFolderNode.getChildNode((PackerFolder) selectedItem);
            }
            if (confirm("deleteFolder", "deleteFolderConfirm", new Object[] { node })) {
                try {
                    ((PackerFolderTreeModel) tree.getModel()).remove(node);
                    tree.setSelectionPath(((PackerFolderTreeNode) node.getParent()).getPath());
                } catch (ValidationException e1) {
                    ResourceBundle resource = ResourceBundle.getBundle("cz.zcu.fav.hofhans.resources.view.Dialogs", panel.getLocale());
                    JOptionPane.showMessageDialog(panel, e1.getLocalizedMessage(), resource.getString("validationError"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
   * Rename folder action.
   * @author Tomáš Hofhans
   * @since 12.1.2010
   */
    private class RenameFolderAction extends AbstractAction {

        /** SVUID. */
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            PackerFolderTreeNode node = null;
            if (selectedItem == null || !(selectedItem instanceof PackerFolder)) {
                node = selectedFolderNode;
            } else {
                node = selectedFolderNode.getChildNode((PackerFolder) selectedItem);
            }
            String name = loadString("updateFolderName", "setFolderName", node.toString());
            if (name != null) {
                ResourceBundle resource = ResourceBundle.getBundle("cz.zcu.fav.hofhans.resources.view.Dialogs", panel.getLocale());
                if (name.equals("")) {
                    JOptionPane.showMessageDialog(panel, resource.getString("emptyValue"), resource.getString("inputError"), JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        ((PackerFolderTreeModel) tree.getModel()).rename(node, name);
                        if (node.getParent() != null) {
                            ((PackerFolderTreeNode) node.getParent()).getItemsModel().rename(node.getFolder(), name);
                        }
                        if (instance.getMenuInvoker(e.getSource()) == tree) {
                            tree.setSelectionPath(node.getPath());
                        } else {
                            content.setSelectedValue(node.getFolder(), true);
                        }
                    } catch (ValidationException e1) {
                        JOptionPane.showMessageDialog(panel, e1.getLocalizedMessage(), resource.getString("validationError"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    /**
   * Create items action. Create items from file system - files and directories structure.
   * @author Tomáš Hofhans
   * @since 12.1.2010
   */
    private class CreateItemsAction extends AbstractAction {

        /** SVUID. */
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            File file = selectFile("chooseData");
            if (file != null) {
                PackerFolderTreeNode parent = null;
                if (selectedItem == null || !(selectedItem instanceof PackerFolder)) {
                    parent = selectedFolderNode;
                } else {
                    parent = selectedFolderNode.getChildNode((PackerFolder) selectedItem);
                }
                try {
                    PackerFolderTreeNode newNode = ((PackerFolderTreeModel) tree.getModel()).insert(parent, file);
                    content.setSelectedValue(newNode.getFolder(), true);
                } catch (ValidationException e1) {
                    ResourceBundle resource = ResourceBundle.getBundle("cz.zcu.fav.hofhans.resources.view.Dialogs", panel.getLocale());
                    JOptionPane.showMessageDialog(panel, e1.getLocalizedMessage(), resource.getString("validationError"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
   * Delete file action.
   * @author Tomáš Hofhans
   * @since 15.1.2010
   */
    private class DeleteFileAction extends AbstractAction {

        /** SVUID. */
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedItem != null) {
                if (confirm("deleteFile", "deleteFileConfirm", new Object[] { selectedItem })) {
                    PackerItemListModel listModel = (PackerItemListModel) content.getModel();
                    try {
                        listModel.remove(selectedItem);
                    } catch (ValidationException e1) {
                        ResourceBundle resource = ResourceBundle.getBundle("cz.zcu.fav.hofhans.resources.view.Dialogs", panel.getLocale());
                        JOptionPane.showMessageDialog(panel, e1.getLocalizedMessage(), resource.getString("validationError"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    /**
   * Rename file action.
   * @author Tomáš Hofhans
   * @since 15.1.2010
   */
    private class RenameFileAction extends AbstractAction {

        /** SVUID. */
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            String name = loadString("updateFileName", "setFileName", selectedItem.getName());
            if (name != null) {
                ResourceBundle resource = ResourceBundle.getBundle("cz.zcu.fav.hofhans.resources.view.Dialogs", panel.getLocale());
                if (name.equals("")) {
                    JOptionPane.showMessageDialog(panel, resource.getString("emptyValue"), resource.getString("inputError"), JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        ((PackerItemListModel) content.getModel()).rename(selectedItem, name);
                        content.setSelectedValue(selectedItem, true);
                    } catch (ValidationException e1) {
                        JOptionPane.showMessageDialog(panel, e1.getLocalizedMessage(), resource.getString("validationError"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    /**
   * Browser mouse adapter.
   * @author Tomáš Hofhans
   * @since 18.1.2010
   */
    public class BrowserMouseAdapter extends MouseAdapter {

        /**
     * {@inheritDoc}
     */
        public void mousePressed(MouseEvent e) {
            if (e.getComponent() == tree) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    TreePath tp = tree.getPathForLocation(e.getX(), e.getY());
                    if (tp != null) {
                        tree.setSelectionPath(tp);
                        selectedFolderNode = (PackerFolderTreeNode) tp.getLastPathComponent();
                        selectedItem = null;
                        folderPopUp.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            } else if (e.getComponent() == content) {
                int index = content.locationToIndex(e.getPoint());
                if (index != -1 && content.getCellBounds(index, index).contains(e.getPoint())) {
                    content.setSelectedIndex(index);
                    selectedItem = (PackerItem) content.getModel().getElementAt(index);
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        if (selectedItem instanceof PackerFolder) {
                            folderPopUp.show(e.getComponent(), e.getX(), e.getY());
                        } else if (selectedItem instanceof PackerFile) {
                            filePopUp.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                } else {
                    if (selectedFolderNode != null) {
                        selectedItem = null;
                        if (e.getButton() == MouseEvent.BUTTON3) {
                            folderPopUp.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    index = content.locationToIndex(e.getPoint());
                    if (index != -1 && content.getCellBounds(index, index).contains(e.getPoint())) {
                        selectedItem = (PackerItem) content.getModel().getElementAt(index);
                        if (selectedItem instanceof PackerFolder) {
                            selectedFolderNode = selectedFolderNode.getChildNode((PackerFolder) selectedItem);
                            tree.setSelectionPath(selectedFolderNode.getPath());
                        } else if (selectedItem instanceof PackerFile && Desktop.isDesktopSupported()) {
                            PackerFile file = (PackerFile) selectedItem;
                            try {
                                File extracted = file.extract(getTempFolder());
                                extracted = extracted.getCanonicalFile();
                                extracted.deleteOnExit();
                                Desktop desktop = Desktop.getDesktop();
                                desktop.open(extracted);
                            } catch (IOException e1) {
                                LOG.log(Level.INFO, "Problem with opening file. Maybe no association created for this file.", e1);
                            } catch (ValidationException e1) {
                                ResourceBundle resource = ResourceBundle.getBundle("cz.zcu.fav.hofhans.resources.view.Dialogs", panel.getLocale());
                                JOptionPane.showMessageDialog(panel, e1.getLocalizedMessage(), resource.getString("validationError"), JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
   * Get menu invoker - it will be JList or JTree.
   * @param source event source - probably JMenuItem
   * @return menu invoker - JList or JTree instance
   */
    private Component getMenuInvoker(Object source) {
        return ((JPopupMenu) ((Component) source).getParent()).getInvoker();
    }

    /**
   * Get temporary folder with unique name.
   * @return created temporary folder
   */
    private File getTempFolder() {
        PackerConfiguration config = PackerConfiguration.getInstance();
        File temp = config.getTemp();
        temp = new File(temp, UUID.randomUUID().toString());
        temp.mkdirs();
        temp.deleteOnExit();
        return temp;
    }

    /**
   * Transfer handler for this perspective.
   * @author Tomáš Hofhans
   * @since 15.2.2010
   */
    public class PackerTransferHandler extends TransferHandler {

        /** SVUID. */
        private static final long serialVersionUID = 1L;

        /** {@inheritDoc} */
        public int getSourceActions(JComponent c) {
            return COPY;
        }

        /** {@inheritDoc} */
        public Transferable createTransferable(JComponent c) {
            Transferable t = null;
            try {
                if ((c instanceof JTree) && selectedFolderNode != null) {
                    JTree tree = (JTree) c;
                    tree.getSelectionPath().getLastPathComponent();
                    t = new PackerTransfer(extractItems(selectedFolderNode.getFolder()));
                } else if (c instanceof JList && selectedItem != null) {
                    t = new PackerTransfer(extractItems(selectedItem));
                }
            } catch (ValidationException e) {
                ResourceBundle resource = ResourceBundle.getBundle("cz.zcu.fav.hofhans.resources.view.Dialogs", panel.getLocale());
                JOptionPane.showMessageDialog(panel, e.getLocalizedMessage(), resource.getString("validationError"), JOptionPane.ERROR_MESSAGE);
            }
            return t;
        }

        /**
     * Extract item to temporary folder
     * @param node node to export
     * @throws ValidationException problem with extracting items
     */
        private File extractItems(final PackerItem item) throws ValidationException {
            File temp = getTempFolder();
            File returnFile = null;
            File parent;
            Map<PackerFolder, File> parents = new HashMap<PackerFolder, File>();
            List<PackerFolder> folders = new LinkedList<PackerFolder>();
            File extractFolder;
            if (item instanceof PackerFolder) {
                returnFile = new File(temp, item.getName());
                returnFile.mkdir();
                parents.put((PackerFolder) item, returnFile);
                folders.add((PackerFolder) item);
                extractFolder = returnFile;
                extractFolder.deleteOnExit();
            } else if (item instanceof PackerFile) {
                returnFile = ((PackerFile) item).extract(temp);
                returnFile.deleteOnExit();
            }
            List<PackerItem> innerItems;
            while (!folders.isEmpty()) {
                PackerFolder storeFolder = folders.get(0);
                folders.remove(0);
                parent = parents.get(storeFolder);
                innerItems = storeFolder.getItems();
                for (PackerItem storeItem : innerItems) {
                    if (storeItem instanceof PackerFolder) {
                        extractFolder = new File(parent, storeItem.getName());
                        extractFolder.mkdir();
                        extractFolder.deleteOnExit();
                        parents.put((PackerFolder) storeItem, extractFolder);
                        folders.add((PackerFolder) storeItem);
                    } else if (storeItem instanceof PackerFile) {
                        File file = ((PackerFile) storeItem).extract(parent);
                        file.deleteOnExit();
                    }
                }
            }
            return returnFile;
        }

        /** {@inheritDoc} */
        public void exportDone(JComponent c, Transferable t, int action) {
        }

        /** {@inheritDoc} */
        public boolean canImport(TransferSupport support) {
            DataFlavor[] data = support.getDataFlavors();
            for (DataFlavor d : data) {
                if (!d.equals(DataFlavor.javaFileListFlavor)) {
                    return false;
                }
            }
            if ((support.getUserDropAction() & COPY_OR_MOVE) == 0) {
                return false;
            }
            if (selectedFolderNode == null) {
                return false;
            }
            Component c = support.getComponent();
            if (c instanceof JTree) {
                javax.swing.JTree.DropLocation treeLocation = (javax.swing.JTree.DropLocation) support.getDropLocation();
                if (treeLocation.getPath() == null) {
                    return false;
                }
            }
            return true;
        }

        /** {@inheritDoc} */
        public boolean importData(TransferSupport support) {
            Component c = support.getComponent();
            DataFlavor[] data = support.getDataFlavors();
            if (selectedFolderNode == null) {
                return false;
            }
            PackerFolderTreeNode parent = null;
            if (c instanceof JTree) {
                javax.swing.JTree.DropLocation treeLocation = (javax.swing.JTree.DropLocation) support.getDropLocation();
                if (treeLocation.getPath() == null) {
                    return false;
                }
                parent = (PackerFolderTreeNode) treeLocation.getPath().getLastPathComponent();
            } else if (c instanceof JList) {
                javax.swing.JList.DropLocation treeLocation = (javax.swing.JList.DropLocation) support.getDropLocation();
                int treeIndex = treeLocation.getIndex();
                if (treeIndex == -1) {
                    parent = selectedFolderNode;
                } else {
                    PackerItem selected = (PackerItem) content.getModel().getElementAt(treeIndex);
                    if (selected == null || !(selected instanceof PackerFolder)) {
                        parent = selectedFolderNode;
                    } else {
                        parent = selectedFolderNode.getChildNode((PackerFolder) selected);
                    }
                }
            }
            for (DataFlavor d : data) {
                List<File> fileData;
                try {
                    Object objData = support.getTransferable().getTransferData(d);
                    if (objData instanceof List<?>) {
                        fileData = (List<File>) objData;
                    } else {
                        LOG.warning("Unknown data format: " + objData);
                        throw new PackerRuntimeException("Unknown data format.");
                    }
                } catch (UnsupportedFlavorException e) {
                    throw new PackerRuntimeException("Unsupported flavor.", e);
                } catch (IOException e) {
                    throw new PackerRuntimeException("Problem with loading data.", e);
                }
                try {
                    PackerFolderTreeNode parentNode = null;
                    boolean noSelection = true;
                    for (File file : fileData) {
                        PackerFolderTreeNode newNode = ((PackerFolderTreeModel) tree.getModel()).insert(parent, file);
                        if (noSelection) {
                            if (file.isDirectory()) {
                                parentNode = (PackerFolderTreeNode) newNode.getParent();
                            } else {
                                parentNode = newNode;
                            }
                            noSelection = false;
                        }
                    }
                    if (!noSelection) {
                        content.setSelectedValue(parentNode.getFolder(), true);
                        tree.setSelectionPath(parentNode.getPath());
                    }
                } catch (ValidationException e1) {
                    ResourceBundle resource = ResourceBundle.getBundle("cz.zcu.fav.hofhans.resources.view.Dialogs", panel.getLocale());
                    JOptionPane.showMessageDialog(panel, e1.getLocalizedMessage(), resource.getString("validationError"), JOptionPane.ERROR_MESSAGE);
                }
            }
            return true;
        }
    }

    /**
   * Class for packer data transfer.
   * @author Tomáš Hofhans
   * @since 31.1.2010
   */
    private class PackerTransfer implements Transferable {

        private File transfer;

        /**
     * Constructor.
     * @param transfer file to transfer
     */
        public PackerTransfer(File transfer) {
            this.transfer = transfer;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!flavor.equals(DataFlavor.javaFileListFlavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            List<File> list = new LinkedList<File>();
            list.add(transfer);
            return list;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { DataFlavor.javaFileListFlavor };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if (flavor.equals(DataFlavor.javaFileListFlavor)) {
                return true;
            } else {
                return false;
            }
        }
    }
}
