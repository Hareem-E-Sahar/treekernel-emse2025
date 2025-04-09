package hu.scytha.action;

import hu.scytha.common.*;
import hu.scytha.gui.dialog.MultiButtonDialog;
import hu.scytha.main.Scytha;
import hu.scytha.main.Settings;
import hu.scytha.plugin.IFile;
import java.io.File;
import java.io.FileFilter;
import java.util.*;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;

/**
 * @author LaczBert
 *
 */
public class RenameAction extends Action {

    private int fAnswerExistsFile = 0;

    private int fAnswerRenamingProblem = 0;

    private String fDlgTitel = Messages.getString("RenameAction.rename");

    private List<IFile> fInputFiles;

    private String fDestDirName;

    private boolean fMoreFiles = false;

    /**
    * Constructor.
    * @param inputFile
    */
    public RenameAction(List<IFile> pInputFiles, String pDestDirName) {
        fDestDirName = pDestDirName;
        fInputFiles = pInputFiles;
        if (pInputFiles.size() > 1) {
            fMoreFiles = true;
        }
    }

    /**
    * Constructor.
    * @param inputFile
    */
    public RenameAction(String pDlgTitel, List<IFile> pInputFiles, String pDestDirName) {
        this(pInputFiles, pDestDirName);
        fDlgTitel = pDlgTitel;
    }

    public void run() {
        InputDialog dlg;
        if (!this.fMoreFiles) {
            dlg = new InputDialog(Scytha.getWindow().getShell(), this.fDlgTitel, fInputFiles.get(0).getAbsolutePath() + "\n" + Messages.getString("RenameAction.to") + " " + this.fDestDirName + File.separator, fInputFiles.get(0).getName(), null);
        } else {
            dlg = new InputDialog(Scytha.getWindow().getShell(), Messages.getString("RenameAction.rename"), Messages.getString("RenameAction.to") + " " + this.fDestDirName + File.separator, "*.*", null);
        }
        dlg.open();
        if (dlg.getReturnCode() == 0) {
            try {
                if (this.fMoreFiles) {
                    ArrayList<IFile> dirs = new ArrayList<IFile>();
                    ArrayList<IFile> files = new ArrayList<IFile>();
                    Util.readSubDirsAndFiles(fInputFiles, dirs, files, true);
                    String sourceDir = Scytha.getWindow().getSelectedFilePanel().getActiveDirectoryName();
                    for (Iterator iter = files.iterator(); iter.hasNext(); ) {
                        IFile fileToRename = (IFile) iter.next();
                        LocalFile newFile = new LocalFile(this.fDestDirName + File.separator + fileToRename.getAbsolutePath().substring(sourceDir.length()));
                        renameFile(newFile, fileToRename);
                        if (fAnswerRenamingProblem == 3 || fAnswerExistsFile == 4) {
                            return;
                        }
                    }
                    for (IFile file : fInputFiles) {
                        if (file.isDirectory()) {
                            deleteEmptyDirectoryEntries(file.getAbsolutePath());
                            file.delete();
                        }
                    }
                } else {
                    LocalFile newFile = new LocalFile(this.fDestDirName + File.separator + dlg.getValue());
                    if (renameFile(newFile, fInputFiles.get(0))) {
                        Scytha.getWindow().refreshPanels();
                        Scytha.getWindow().getSelectedFilePanel().setSelectedItem(newFile);
                    } else {
                        MessageSystem.showErrorMessage(Messages.getString("RenameAction.could.not.be.renamed", new Object[] { fInputFiles.get(0) }));
                    }
                }
            } catch (ScythaException e) {
                MessageSystem.logException("", getClass().getName(), "run", null, e);
                MessageSystem.showErrorMessage(e.getLocalizedMessage());
            }
        }
    }

    /**
    * 
    * @param newFileName
    */
    private boolean renameFile(IFile newFile, IFile oldFile) {
        if (newFile.exists() && newFile.isDirectory()) {
        } else if (newFile.exists()) {
            if (fAnswerExistsFile != 1 && fAnswerExistsFile != 3) {
                String buttonTitles[] = new String[] { Messages.getString("CopyAction.yes"), Messages.getString("CopyAction.yes.to.all"), Messages.getString("CopyAction.no"), Messages.getString("CopyAction.no.to.all"), Messages.getString("CopyAction.cancel") };
                MultiButtonDialog dlg = new MultiButtonDialog(Scytha.getWindow().getShell(), Messages.getString("RenameAction.rename"), buttonTitles, Messages.getString("RenameAction.file.exists.overwrite", new Object[] { newFile.getName() }), null, 5);
                fAnswerExistsFile = dlg.open();
            } else if (fAnswerExistsFile == 3) {
                return true;
            }
        }
        if ((fAnswerExistsFile == 0 || fAnswerExistsFile == 1) && !oldFile.renameTo(newFile)) {
            newFile.getParentFile().mkdirs();
            if (!oldFile.renameTo(newFile)) {
                while (fAnswerRenamingProblem == 0) {
                    if (fAnswerRenamingProblem != 2) {
                        fAnswerRenamingProblem = handleRenamingProblem(newFile);
                        switch(fAnswerRenamingProblem) {
                            case 0:
                                break;
                            case 1:
                                fAnswerRenamingProblem = 0;
                            case 2:
                                return true;
                            case 3:
                                return false;
                        }
                    } else {
                        return true;
                    }
                }
                return false;
            }
        }
        return true;
    }

    private int handleRenamingProblem(IFile file) {
        String[] buttonTitles = new String[] { Messages.getString("DeleteAction.retry"), Messages.getString("DeleteAction.skip"), Messages.getString("DeleteAction.skip.all"), Messages.getString("DeleteAction.abort") };
        MultiButtonDialog dlg = new MultiButtonDialog(Scytha.getWindow().getShell(), Messages.getString("Scytha.error.title"), buttonTitles, Messages.getString("RenameAction.file.was.not.created", new Object[] { file.getName() }), null, 4);
        return dlg.open();
    }

    /**
    * 
    * @param directoryName
    */
    private static void deleteEmptyDirectoryEntries(String directoryName) {
        if (Settings.traceOn) {
            MessageSystem.trace(Util.class.getName(), "deleteEmptyDirectoryEntriesOfDirectory(directoryName");
        }
        File dir = new File(directoryName);
        if (dir.isDirectory()) {
            recoursiveDelete(dir);
        }
    }

    /**
    * 
    * @param directory
    */
    private static void recoursiveDelete(File directory) {
        File[] dirs = directory.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        for (int i = 0; i < dirs.length; i++) {
            recoursiveDelete(dirs[i]);
            dirs[i].delete();
        }
    }
}
