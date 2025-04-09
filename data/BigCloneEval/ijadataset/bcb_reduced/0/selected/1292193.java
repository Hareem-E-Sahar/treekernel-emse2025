package de.miethxml.hawron.gui.context.action;

import java.io.File;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import de.miethxml.toolkit.io.FileModel;

/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth </a>
 *
 *
 *
 *
 *
 *
 *
 */
public class DeleteAction implements Action, FileModelAction {

    /**
     *
     *
     *
     */
    public DeleteAction() {
        super();
    }

    public void doAction(String uri) {
        File f = new File(uri);
        String msg = "";
        if (f.isDirectory()) {
            msg = "Are you sure to delete directory and all subdirectories " + uri;
        } else if (f.isFile()) {
            msg = "Are you sure to delete file " + uri;
        }
        int option = JOptionPane.showConfirmDialog(null, msg, "Delete?", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            delete(f);
        }
    }

    public Icon getIcon() {
        ImageIcon icon = new ImageIcon("icons/delete.gif");
        return icon;
    }

    public boolean isSupported(String extention) {
        return true;
    }

    public boolean isHandleDirectory() {
        return true;
    }

    public boolean isHandleFile() {
        return true;
    }

    public String getToolTip(String lang) {
        return "Delete";
    }

    private void delete(File f) {
        if (f.exists() && f.isDirectory()) {
            File[] entries = f.listFiles();
            for (int i = 0; i < entries.length; i++) {
                delete(entries[i]);
            }
            f.delete();
        } else {
            f.delete();
        }
    }

    public void doAction(FileModel model) {
        String msg = "";
        if (!model.isFile()) {
            msg = "Are you sure to delete directory and all subdirectories " + model.getPath();
        } else {
            msg = "Are you sure to delete file " + model.getPath();
        }
        int option = JOptionPane.showConfirmDialog(null, msg, "Delete?", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            delete(model);
        }
    }

    private void delete(FileModel f) {
        if (f.exists()) {
            if (f.isFile()) {
                f.delete();
            } else {
                FileModel[] children = f.getChildren();
                for (int i = 0; i < children.length; i++) {
                    delete(children[i]);
                }
                f.delete();
            }
        }
    }
}
