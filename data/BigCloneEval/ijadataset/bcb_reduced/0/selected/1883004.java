package modrcon;

import javax.swing.*;
import java.io.*;
import java.util.zip.*;

/**
 * A class to import/export 1up ModRcon Settings,
 * so they can be shared amongst clan mates.
 *
 * @author Pyrite
 */
public class ImportExportManager {

    /** A reference to the Main Window */
    private MainWindow parent;

    public ImportExportManager(MainWindow owner) {
        this.parent = owner;
    }

    /** Gets an array of the settings filenames that ModRcon uses. */
    private String[] getSettingsFiles() {
        File dir = new File(PropertyManager.settingsPath);
        return dir.list();
    }

    public void importSettings() {
    }

    public void exportSettings() {
        String[] filenames = this.getSettingsFiles();
        byte[] buf = new byte[1024];
        try {
            String outFilename = "ModRcon_v" + PropertyManager.MODRCON_VERSION + "_SettingsBackup.pu1";
            JFileChooser file = new JFileChooser();
            file.setSelectedFile(new File(outFilename));
            int choice = file.showSaveDialog(parent);
            if (choice == 0) {
                String path = file.getSelectedFile().getAbsolutePath();
                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(path));
                for (int i = 0; i < filenames.length; i++) {
                    FileInputStream in = new FileInputStream(PropertyManager.settingsPath + filenames[i]);
                    out.putNextEntry(new ZipEntry(filenames[i]));
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                    in.close();
                }
                out.close();
                JOptionPane.showMessageDialog(parent, "1up ModRcon Settings Exported Successfully!", "File Saved", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            System.out.println("Error Creating Export File:" + e.getMessage());
        }
    }
}
