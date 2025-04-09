package com.jiexplorer.gui.dnd;

import java.io.File;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import com.jiexplorer.JIExplorer;
import com.jiexplorer.filetask.CopyFileTask;
import com.jiexplorer.filetask.MoveFileTask;
import com.jiexplorer.gui.tree.JIDirTree;
import com.jiexplorer.gui.tree.JITreeNode;

public class NodeTransferHandler extends AbstractTransferHandler {

    /**
	 *
	 */
    private static final long serialVersionUID = 8174972825717963250L;

    @Override
    public TransferableFileList exportFileList(final JComponent c) {
        log.debug("exportFileList " + c);
        final JIDirTree tree = (JIDirTree) c;
        final Object obj = tree.getSelectedNode().getUserObject();
        log.debug("exportFileList " + obj.getClass().getName() + " " + obj);
        final TransferableFileList transferable = new TransferableFileList();
        if (obj == null) {
            c.getToolkit().beep();
        } else {
            transferable.add(obj);
        }
        return transferable;
    }

    @Override
    public void importFileList(final JComponent c, final java.util.List<File> fileList) {
        log.debug("importFileList " + fileList);
        final JIDirTree tree = (JIDirTree) c;
        final JITreeNode node = tree.getSelectedNode();
        final File selectedDir = (File) node.getUserObject();
        if (selectedDir == null) {
            c.getToolkit().beep();
            System.out.println("Selected Tree Node == Null");
            return;
        }
        this.importSuccess = true;
        if (fileList.get(0).isDirectory() && selectedDir.getPath().equals(fileList.get(0).getParent())) {
            this.importSuccess = false;
            log.error("Recusive Copy");
        } else {
            if (ClipBoardAction.getInstance().isCutOperation()) {
                final Object obj = ClipBoardAction.getInstance().getCutData();
                if ((obj != null) && (obj instanceof java.util.List)) {
                    final java.util.List<File> files = (java.util.List<File>) obj;
                    if (files.containsAll(fileList)) {
                        final MoveFileTask moveTask = new MoveFileTask(fileList, selectedDir);
                        JIExplorer.instance().runAction(moveTask);
                    }
                }
            } else if (TransferHandler.MOVE == getSourceActions(c)) {
                final MoveFileTask moveTask = new MoveFileTask(fileList, selectedDir);
                JIExplorer.instance().runAction(moveTask);
            } else {
                final CopyFileTask copyTask = new CopyFileTask(fileList, selectedDir);
                JIExplorer.instance().runAction(copyTask);
            }
            final Iterator<File> iterator = fileList.iterator();
            while (iterator.hasNext()) {
                final File file = iterator.next();
                final File fileB = new File(selectedDir, file.getName());
                if (fileB.isDirectory()) {
                    node.add(new JITreeNode(fileB));
                    tree.getTreeModel().nodeStructureChanged(node);
                }
                tree.addFile(fileB);
            }
        }
    }

    @Override
    public void cleanup(final JComponent c, final boolean remove) {
    }
}
