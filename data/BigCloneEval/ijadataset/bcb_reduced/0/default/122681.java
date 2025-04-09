import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.*;
import java.io.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.*;

class DirTree extends javax.swing.JTree implements DropTargetListener, DragGestureListener {

    private JPopupMenu pum = null;

    private MainWin myParent = null;

    private String rootValue = null;

    private int rowHighlighted = -1;

    /**
     * enables this component to be a dropTarget
     */
    DropTarget dropTarget = null;

    public DirTree() {
        super();
        initialize();
        addTreeListeners();
        dropTarget = new DropTarget(this, this);
    }

    public DirTree(MainWin parent) {
        super();
        myParent = parent;
        initialize();
        addTreeListeners();
        dropTarget = new DropTarget(this, this);
    }

    public void initialize() {
        this.setExpandsSelectedPaths(true);
        File roots[] = File.listRoots();
        rootValue = new String("c:\\");
        File rootDir = new File("c:\\");
        if (!rootDir.exists()) {
            rootValue = new String("/");
            rootDir = new File("/");
            if (!rootDir.exists()) {
                System.out.println("Can't understand file system");
                System.out.println("Root is neither c:\\ nor / ");
                System.exit(-1);
            }
        }
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(rootValue);
        DefaultMutableTreeNode subDir = null;
        DefaultMutableTreeNode dirEntry = null;
        File[] contents = rootDir.listFiles();
        java.util.Arrays.sort(contents);
        for (int x = 0; x < contents.length; x++) {
            if (contents[x].isDirectory()) {
                if (myParent.showHiddenFiles == false && contents[x].isHidden()) continue;
                subDir = new DefaultMutableTreeNode(contents[x].getName());
                subDir.setAllowsChildren(true);
                top.add(subDir);
            }
        }
        DefaultTreeModel myModel = new DefaultTreeModel(top, true);
        setModel(myModel);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        pum = new JPopupMenu();
        JMenuItem item1 = new JMenuItem("Add directory");
        item1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String newDir = javax.swing.JOptionPane.showInputDialog(myParent, new String("Directory to add to \n " + myParent.currentDir + java.io.File.separator), "Add directory", javax.swing.JOptionPane.QUESTION_MESSAGE);
                if (newDir == null) return;
                newDir = myParent.currentDir + java.io.File.separator + newDir;
                File dir = new File(newDir);
                boolean success = dir.mkdir();
                if (!success) {
                    JOptionPane.showMessageDialog(myParent, new String("Failed to create " + newDir), "Warning", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                initialize();
                myParent.scrollToSelectedDir();
            }
        });
        pum.add(item1);
        JMenuItem item2 = new JMenuItem("Delete directory");
        item2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int answer = JOptionPane.showConfirmDialog(myParent, new String("Do you really want to delete:\n" + myParent.currentDir), "Confirm delete", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (answer != JOptionPane.YES_OPTION) return;
                File dir = new File(myParent.currentDir);
                File contents[] = dir.listFiles();
                if (contents.length > 0) {
                    JOptionPane.showMessageDialog(myParent, new String("Can not delete " + dir.getName() + "\n" + "It is not empty."), "Warning", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                dir.delete();
                initialize();
                String tempString = myParent.currentDir;
                int location = tempString.lastIndexOf(File.separator);
                myParent.directoryChanged(tempString.substring(0, location - 1));
                myParent.scrollToSelectedDir();
            }
        });
        pum.add(item2);
        this.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                if (e.BUTTON3_MASK == e.getModifiers()) {
                    pum.show(e.getComponent(), e.getX(), e.getY());
                    return;
                }
            }
        });
    }

    private void addTreeListeners() {
        addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent tse) {
                TreePath tp = tse.getNewLeadSelectionPath();
                if (tp == null) return;
                String directorySelected = new String(rootValue);
                Object[] allPaths = tp.getPath();
                if (allPaths.length > 0) {
                    for (int x = 1; x < allPaths.length; x++) {
                        directorySelected = new String(directorySelected + File.separator + allPaths[x].toString());
                    }
                }
                if (myParent != null) myParent.directoryChanged(directorySelected);
            }
        });
        addTreeWillExpandListener(new TreeWillExpandListener() {

            public void treeWillExpand(TreeExpansionEvent tee) throws ExpandVetoException {
                expandNode(tee.getPath());
            }

            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
            }
        });
    }

    public void expandNode(TreePath path1) {
        if (!path1.equals(getSelectionPath())) {
            setSelectionPath(path1);
        }
        while (myParent.filteringFiles) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
        }
        DefaultMutableTreeNode selected = (DefaultMutableTreeNode) path1.getLastPathComponent();
        if (myParent.directoryFiles == null) return;
        if (myParent.directoryFiles.size() == 0) return;
        TreeNode[] tp = selected.getPath();
        String path = new String(rootValue);
        for (int z = 1; z < tp.length; z++) {
            path = new String(path + File.separator + tp[z].toString());
        }
        DefaultMutableTreeNode newNode = null;
        File tempFile = null;
        for (int z = 0; z < myParent.directoryFiles.size(); z++) {
            {
                tempFile = (File) myParent.directoryFiles.get(z);
                if (tempFile.isHidden() && myParent.showHiddenFiles == false) continue;
                newNode = new DefaultMutableTreeNode(tempFile.getName());
                newNode.setAllowsChildren(true);
                selected.add(newNode);
            }
        }
    }

    public void dragOver(java.awt.dnd.DropTargetDragEvent event) {
        java.awt.Graphics g = this.getGraphics();
        undrawHighlight();
        Point loc = event.getLocation();
        int row = this.getRowForLocation(loc.x, loc.y);
        java.awt.Rectangle rect = this.getRowBounds(row);
        if (rect == null) return;
        g.setColor(java.awt.Color.black);
        g.drawRect(rect.x, rect.y, rect.width, rect.height);
        rowHighlighted = row;
    }

    public void dropActionChanged(java.awt.dnd.DropTargetDragEvent p1) {
    }

    public void dragEnter(java.awt.dnd.DropTargetDragEvent p1) {
    }

    public void drop(java.awt.dnd.DropTargetDropEvent event) {
        undrawHighlight();
        try {
            Transferable transferable = event.getTransferable();
            if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                event.acceptDrop(DnDConstants.ACTION_MOVE);
                String s = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                event.getDropTargetContext().dropComplete(true);
                Point dropPoint = event.getLocation();
                TreePath dropPath = this.getClosestPathForLocation(dropPoint.x, dropPoint.y);
                String toPath = null;
                for (int x = 0; x < dropPath.getPathCount(); x++) {
                    if (toPath == null) toPath = new String(dropPath.getPathComponent(x).toString()); else toPath = new String(toPath + File.separator + dropPath.getPathComponent(x).toString());
                }
                boolean success = myParent.moveFile(toPath);
            } else {
                event.rejectDrop();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            System.err.println("Exception" + exception.getMessage());
            event.rejectDrop();
        } catch (UnsupportedFlavorException ufException) {
            ufException.printStackTrace();
            System.err.println("Exception" + ufException.getMessage());
            event.rejectDrop();
        }
    }

    public void dragExit(java.awt.dnd.DropTargetEvent p1) {
        undrawHighlight();
    }

    public void dragGestureRecognized(java.awt.dnd.DragGestureEvent p1) {
    }

    private void undrawHighlight() {
        java.awt.Graphics g = this.getGraphics();
        java.awt.Rectangle rect = this.getRowBounds(rowHighlighted);
        if (rect != null) {
            g.setColor(java.awt.Color.white);
            g.drawRect(rect.x, rect.y, rect.width, rect.height);
            rowHighlighted = -1;
        }
        return;
    }
}
