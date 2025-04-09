package com.nullfish.app.jfd2.command.embed;

import java.text.MessageFormat;
import com.nullfish.app.jfd2.JFD;
import com.nullfish.app.jfd2.JFDModel;
import com.nullfish.app.jfd2.command.Command;
import com.nullfish.app.jfd2.dialog.JFDDialog;
import com.nullfish.app.jfd2.resource.JFDResource;
import com.nullfish.lib.vfs.FileSystem;
import com.nullfish.lib.vfs.FileUtil;
import com.nullfish.lib.vfs.Manipulation;
import com.nullfish.lib.vfs.VFile;
import com.nullfish.lib.vfs.exception.VFSException;
import com.nullfish.lib.vfs.impl.filelist.FileListFileSystem;
import com.nullfish.lib.vfs.manipulation.DeleteFailurePolicy;

/**
 * ソートコマンド
 * 
 * @author shunji
 */
public class DeleteFileCommand extends Command {

    public static final String OK = "ok";

    public static final String CANCEL = "cancel";

    public void doExecute() throws VFSException {
        JFD jfd = getJFD();
        JFDModel model = jfd.getModel();
        JFDDialog dialog = null;
        try {
            model.lockAutoUpdate(this);
            VFile selectedFile = model.getSelectedFile();
            VFile[] markedFiles = model.getMarkedFiles();
            if (selectedFile.isDirectory() && markedFiles.length == 0) {
                return;
            }
            FileSystem fileSystem = model.getCurrentDirectory().getFileSystem();
            if (fileSystem instanceof FileListFileSystem) {
                removeFileList((FileListFileSystem) fileSystem, selectedFile, markedFiles);
                return;
            }
            dialog = jfd.createDialog();
            dialog.setTitle(JFDResource.LABELS.getString("title_delete"));
            if (markedFiles.length == 0) {
                Object[] params = { selectedFile.getName() };
                dialog.addMessage(MessageFormat.format(JFDResource.MESSAGES.getString("message_delete_file"), params));
            } else {
                dialog.addMessage(JFDResource.MESSAGES.getString("message_delete_marked_files"));
            }
            boolean defaultOk = ((Boolean) jfd.getCommonConfiguration().getParam("delete_ok_default", Boolean.TRUE)).booleanValue();
            dialog.addButton(OK, JFDResource.LABELS.getString("ok"), 'y', defaultOk);
            dialog.addButton(CANCEL, JFDResource.LABELS.getString("cancel"), 'n', !defaultOk);
            dialog.pack();
            dialog.setVisible(true);
            String answer = dialog.getButtonAnswer();
            if (answer == null || CANCEL.equals(answer)) {
                return;
            }
            showProgress(1000);
            Manipulation[] children;
            DeleteFailurePolicy policy = new JFDDeleteFailurePolicy(getJFD());
            if (markedFiles.length > 0) {
                children = new Manipulation[markedFiles.length];
                for (int i = 0; i < markedFiles.length; i++) {
                    children[i] = FileUtil.prepareDelete(markedFiles[i], policy, this);
                    children[i].setParentManipulation(this);
                    children[i].prepare();
                }
            } else {
                children = new Manipulation[1];
                children[0] = FileUtil.prepareDelete(selectedFile, policy, this);
                children[0].setParentManipulation(this);
                children[0].prepare();
            }
            setChildManipulations(children);
            for (int i = 0; i < children.length; i++) {
                children[i].execute();
            }
        } finally {
            model.unlockAutoUpdate(this);
            model.refresh();
            if (dialog != null) {
                dialog.dispose();
            }
        }
    }

    /**
	 * ファイルリストからファイルを削除する。
	 * @param fileSystem
	 * @param selectedFile
	 * @param markedFiles
	 * @throws VFSException
	 */
    private void removeFileList(FileListFileSystem fileSystem, VFile selectedFile, VFile[] markedFiles) throws VFSException {
        JFDDialog dialog = null;
        try {
            dialog = getJFD().createDialog();
            dialog.setTitle(JFDResource.LABELS.getString("title_delete"));
            dialog.addMessage(JFDResource.MESSAGES.getString("message_delete_filelist_file"));
            dialog.addButton(OK, JFDResource.LABELS.getString("ok"), 'y', true);
            dialog.addButton(CANCEL, JFDResource.LABELS.getString("cancel"), 'n', false);
            dialog.pack();
            dialog.setVisible(true);
            String answer = dialog.getButtonAnswer();
            if (answer == null || CANCEL.equals(answer)) {
                return;
            }
            if (markedFiles == null || markedFiles.length == 0) {
                markedFiles = new VFile[1];
                markedFiles[0] = selectedFile;
            }
            fileSystem.initFileTree(this);
            for (int i = 0; i < markedFiles.length; i++) {
                fileSystem.removeFile(markedFiles[i]);
            }
            fileSystem.save();
        } finally {
            if (dialog != null) {
                dialog.dispose();
            }
        }
    }

    public boolean closesUnusingFileSystem() {
        return false;
    }
}
