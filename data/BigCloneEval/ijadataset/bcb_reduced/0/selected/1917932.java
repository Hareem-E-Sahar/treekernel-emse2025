package org.gerhardb.lib.dirtree.rdp;

import java.awt.Cursor;
import java.io.File;
import javax.swing.*;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import org.gerhardb.lib.dirtree.DirectoryTreeNode;
import org.gerhardb.lib.dirtree.UndoableMovingFiles;
import org.gerhardb.lib.io.FileUtil;
import org.gerhardb.lib.scroller.IScroll;
import org.gerhardb.lib.util.ModalProgressDialogFast;
import org.gerhardb.lib.util.startup.ActiveActions;
import org.gerhardb.lib.util.startup.AppStarter;

/**
 * Manages Moves
 */
public class MoveManager {

    static final int MOVE_NONE = 0;

    static final int MOVE_ONE = 1;

    static final int MOVE_MANY = 2;

    private UndoManager myUndoManager = new UndoManager();

    private JButton myUndoBtn;

    private JButton myRedoBtn;

    File myResetRecommendationFile = null;

    RDPmanager myRDPmanager;

    public MoveManager(RDPmanager manager) {
        this.myRDPmanager = manager;
    }

    public void addActions(ActiveActions aa) {
        this.myUndoBtn = aa.getToolBarButton("edit", "undo");
        this.myRedoBtn = aa.getToolBarButton("edit", "redo");
    }

    public UndoManager getUndoManager() {
        return this.myUndoManager;
    }

    public JButton getUndoButton() {
        return this.myUndoBtn;
    }

    public JButton getRedoButton() {
        return this.myRedoBtn;
    }

    void moveCurrentFile(DirectoryTreeNode toNode) {
        try {
            UndoableMovingFiles undo = new UndoableMovingFiles(toNode, this.myRDPmanager, false);
            moveCurrentFile(undo);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this.myRDPmanager.myPlugins.getTopFrame(), AppStarter.getString("Single.0") + ex.getMessage(), AppStarter.getString("Single.1"), JOptionPane.ERROR_MESSAGE);
        }
    }

    void moveCurrentFile(File toDir) {
        try {
            UndoableMovingFiles undo = new UndoableMovingFiles(toDir, this.myRDPmanager);
            moveCurrentFile(undo);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this.myRDPmanager.myPlugins.getTopFrame(), AppStarter.getString("Single.0") + ex.getMessage(), AppStarter.getString("Single.1"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
	 * Undo handle updating file counts for us.
	 * @param undo
	 */
    void moveCurrentFile(UndoableMovingFiles undo) {
        File moveThis = this.myRDPmanager.myPlugins.getCurrentFile();
        this.myRDPmanager.setWaitCursor(true);
        try {
            updateResetRecommendation();
            undo.add(moveThis);
            File[] failedFiles = undo.getFailedFilesShowingMessage(this.myRDPmanager.myPlugins.getTopFrame());
            if (failedFiles.length == 0) {
                this.myRDPmanager.myPlugins.removeCurrentFile();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this.myRDPmanager.myPlugins.getTopFrame(), AppStarter.getString("Single.0") + ex.getMessage(), AppStarter.getString("Single.1"), JOptionPane.ERROR_MESSAGE);
        } finally {
            this.myRDPmanager.setWaitCursor(false);
        }
    }

    /**
	 * Pass in EITHER toNode or toDir.
	 * @param moveNotCopy
	 * @param node
	 * @param dir
	 * @param filesPicked
	 */
    void moveOrCopySeveral(boolean moveNotCopy, DirectoryTreeNode toNode, String toDir, Object[] filesPicked, boolean updateRepeatButton) {
        BoundedRangeModel range = new DefaultBoundedRangeModel();
        new MoveOrCopyIt(moveNotCopy, toNode, toDir, filesPicked, range, updateRepeatButton);
    }

    int confirmMove(String label, DirectoryTreeNode node, String dir, Object[] filesPicked) {
        if (filesPicked.length == 0) {
            return MOVE_ONE;
        }
        String location = "unknown";
        if (node != null) {
            location = node.getAbsolutePath();
        } else {
            location = dir;
        }
        int answer = JOptionPane.showConfirmDialog(this.myRDPmanager.myPlugins.getTopFrame(), AppStarter.getString("MoveManager.11") + " " + filesPicked.length + " " + AppStarter.getString("MoveManager.12") + location + "?\n", AppStarter.getString("MoveManager.13") + " " + label, JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.NO_OPTION) {
            return MOVE_NONE;
        }
        return MOVE_MANY;
    }

    private class MoveOrCopyIt implements Runnable {

        boolean iMoveNotCopy;

        DirectoryTreeNode myToNode;

        String myToDir;

        Object[] myFilesPicked;

        BoundedRangeModel myRange;

        boolean iSetRepeatButtonToThisDirectory;

        ModalProgressDialogFast myDialog;

        JFrame myTopFrame = MoveManager.this.myRDPmanager.myPlugins.getTopFrame();

        /**
		 * Pass in EITHER toNode or toDir.
		 * @param toNode
		 * @param toDir
		 * @param filesPicked
		 * @param range
		 */
        MoveOrCopyIt(boolean moveNotCopy, DirectoryTreeNode toNode, String toDir, Object[] filesPicked, BoundedRangeModel range, boolean setRepeatButtonToThisDirectory) {
            this.iMoveNotCopy = moveNotCopy;
            this.myToNode = toNode;
            this.myToDir = toDir;
            this.myFilesPicked = filesPicked;
            this.myRange = range;
            this.iSetRepeatButtonToThisDirectory = setRepeatButtonToThisDirectory;
            if (this.myToNode == null && this.myToDir == null) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(MoveOrCopyIt.this.myTopFrame, AppStarter.getString("MoveManager.3") + FileUtil.NEWLINE + AppStarter.getString("MoveManager.4") + FileUtil.NEWLINE, AppStarter.getString("MoveManager.5"), JOptionPane.ERROR_MESSAGE);
                    }
                });
                return;
            }
            String title = AppStarter.getString("MoveManager.0");
            String message = AppStarter.getString("MoveManager.1");
            String cancelBtnText = AppStarter.getString("MoveManager.2");
            if (!this.iMoveNotCopy) {
                title = AppStarter.getString("MoveManager.6");
                message = AppStarter.getString("MoveManager.7");
                cancelBtnText = AppStarter.getString("MoveManager.8");
            }
            this.myDialog = new ModalProgressDialogFast(this.myTopFrame, title, filesPicked.length + FileUtil.SPACE + message, cancelBtnText, this.myRange, this);
            this.myDialog.start();
        }

        @Override
        public void run() {
            updateResetRecommendation();
            this.myRange.setMaximum(this.myFilesPicked.length - 1);
            UndoableMovingFiles undo = null;
            try {
                if (this.myToNode != null) {
                    undo = new UndoableMovingFiles(this.myToNode, MoveManager.this.myRDPmanager, this.iSetRepeatButtonToThisDirectory);
                    undo.setMoveNotCopy(this.iMoveNotCopy);
                    try {
                        undo.add(this.myFilesPicked, this.myRange);
                    } catch (org.gerhardb.lib.io.TargetFileExistsException ex) {
                        System.out.println("TargetFileExistsException Issue in MoveManager MoveOrCopyIt NODE");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (this.myToDir != null) {
                    undo = new UndoableMovingFiles(new File(this.myToDir), MoveManager.this.myRDPmanager);
                    undo.setMoveNotCopy(this.iMoveNotCopy);
                    try {
                        undo.add(this.myFilesPicked, this.myRange);
                    } catch (org.gerhardb.lib.io.TargetFileExistsException ex) {
                        System.out.println("TargetFileExistsException Issue in MoveManager MoveOrCopyIt DIRECTORY");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    System.out.println("MoveOrCopyIt: Move Pressed with empty directory: THIS SHOULD NEVER HAPPEN");
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            File[] potentialFailedFiles = new File[0];
            if (undo != null) {
                potentialFailedFiles = undo.getFailedFilesShowingMessage(this.myTopFrame);
                if (potentialFailedFiles.length > 0) {
                    System.out.println("MoveOrCopyIt: Number of files that failed: " + potentialFailedFiles.length);
                } else {
                    MoveManager.this.myRDPmanager.myPlugins.clearListSelections();
                }
            }
            final File[] failedFiles = potentialFailedFiles;
            this.myDialog.done();
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (MoveOrCopyIt.this.iMoveNotCopy) {
                        MoveOrCopyIt.this.myTopFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        MoveManager.this.myRDPmanager.myTreeManager.myCoordinator.getFileList().clearSelection();
                        MoveManager.this.myRDPmanager.myPlugins.reloadScroller(MoveManager.this.myResetRecommendationFile, IScroll.KEEP_CACHE);
                        MoveManager.this.myRDPmanager.getIScrollDirTree().selectFiles(failedFiles);
                        MoveOrCopyIt.this.myTopFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                    MoveManager.this.myRDPmanager.myTreeManager.myCoordinator.getScroller().requestFocus();
                }
            });
        }
    }

    public void setUndoOff() {
        this.myUndoManager.discardAllEdits();
        updateUndoRedoButtons();
    }

    public void addUndoable(UndoableEdit edit) {
        this.myUndoManager.addEdit(edit);
        updateUndoRedoButtons();
    }

    public void updateUndoRedoButtons() {
        if (this.myUndoManager.canUndo()) {
            Action action = this.myUndoBtn.getAction();
            action.setEnabled(true);
            action.putValue(Action.SHORT_DESCRIPTION, this.myUndoManager.getUndoPresentationName());
        } else {
            Action action = this.myUndoBtn.getAction();
            action.setEnabled(false);
            String resourseKey = "SortScreen.menu.edit.undo.tip";
            String retrieved = AppStarter.getString(resourseKey);
            action.putValue(Action.SHORT_DESCRIPTION, retrieved);
        }
        if (this.myUndoManager.canRedo()) {
            Action action = this.myRedoBtn.getAction();
            action.setEnabled(true);
            action.putValue(Action.SHORT_DESCRIPTION, this.myUndoManager.getRedoPresentationName());
        } else {
            Action action = this.myRedoBtn.getAction();
            action.setEnabled(false);
            String resourseKey = "SortScreen.menu.edit.redo.tip";
            String retrieved = AppStarter.getString(resourseKey);
            action.putValue(Action.SHORT_DESCRIPTION, retrieved);
        }
    }

    /**
	 * This happens BEFORE the move is done so that we can figure out what the NEXT file will be.
	 * Hard to do after files have been removed.
	 */
    void updateResetRecommendation() {
        this.myResetRecommendationFile = this.myRDPmanager.myTreeManager.myCoordinator.getScroller().getNextFileRecommendation();
    }
}
