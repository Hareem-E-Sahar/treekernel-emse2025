package jmri.jmrit.roster;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

/**
 * Offer an easy mechanism to save the entire roster contents from one
 * instance of DecoderPro.  The result is a zip format file, containing 
 * all of the roster entries plus the overall roster.xml index file.
 * 
 * @author david d zuhn
 *
 */
public class FullBackupExportAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private Component _parent;

    /**
     * @param s
     *            Name of this action, e.g. in menus
     * @param parent
     *            Component that action is associated with, used to ensure
     *            proper position in of dialog boxes
     */
    public FullBackupExportAction(String s, Component parent) {
        super(s);
        _parent = parent;
    }

    public void actionPerformed(ActionEvent e) {
        Roster roster = Roster.instance();
        ZipOutputStream zipper = null;
        try {
            String roster_filename_extension = ".roster";
            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser.showSaveDialog(_parent);
            if (returnVal != JFileChooser.APPROVE_OPTION) {
                return;
            }
            String filename = chooser.getSelectedFile().getAbsolutePath();
            if (!filename.endsWith(roster_filename_extension)) {
                filename = filename.concat(roster_filename_extension);
            }
            zipper = new ZipOutputStream(new FileOutputStream(filename));
            for (int index = 0; index < roster.numEntries(); index++) {
                RosterEntry entry = roster.getEntry(index);
                copyFileToStream(entry.getPathName(), "roster", zipper, entry.getId());
            }
            copyFileToStream(Roster.defaultRosterFilename(), null, zipper, null);
            zipper.setComment("Roster file saved from DecoderPro " + jmri.Version.name());
            zipper.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (zipper != null) {
                try {
                    zipper.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Copy a file to an entry in a zip file.
     *
     * The basename of the source file will be used in the zip file, placed in
     * the directory of the zip file specified by dirname.  If dirname is null,
     * the file will be placed in the root level of the zip file.
     *
     * @param filename the file to copy
     * @param dirname the zip file "directory" to place this file in
     * @param zipper the ZipOutputStream
     * @throws IOException
     */
    private void copyFileToStream(String filename, String dirname, ZipOutputStream zipper, String comment) throws IOException {
        File file = new File(filename);
        String entryName;
        if (dirname != null) {
            entryName = dirname + "/" + file.getName();
        } else {
            entryName = file.getName();
        }
        ZipEntry zipEntry = new ZipEntry(entryName);
        zipEntry.setTime(file.lastModified());
        zipEntry.setSize(file.length());
        if (comment != null) {
            zipEntry.setComment(comment);
        }
        zipper.putNextEntry(zipEntry);
        FileInputStream fis = new FileInputStream(file);
        try {
            int c;
            while ((c = fis.read()) != -1) {
                zipper.write(c);
            }
        } finally {
            fis.close();
        }
        zipper.closeEntry();
    }
}
