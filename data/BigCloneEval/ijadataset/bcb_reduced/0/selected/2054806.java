package cz.zcu.fav.hofhans.packer;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import cz.zcu.fav.hofhans.packer.exception.PackerRuntimeException;
import cz.zcu.fav.hofhans.packer.view.PackerAbstractView;

/**
 * Class providing creating and restoring backups.
 * @author Tomáš Hofhans
 * @since 25.4.2010
 */
public class BackupService extends PackerAbstractView {

    private static final String DB_FOLDER = "db/";

    private static final int BUF_SIZE = 1000;

    /**
   * Start up backup maker.
   * @param args arguments
   */
    public static void backup(String[] args) {
        if (args.length < 2) {
            System.out.println("Invalid parameters.");
            showHelp();
        } else {
            File file = null;
            if (args.length > 2) {
                file = new File(args[2]);
            }
            if (args[1].equals("-i")) {
                BackupService bm = new BackupService();
                if (file == null) {
                    bm.importData();
                } else {
                    bm.loadBackup(file);
                }
            } else if (args[1].equals("-e")) {
                BackupService bm = new BackupService();
                if (file == null) {
                    bm.exportData();
                } else {
                    bm.createBackup(file);
                }
            } else {
                System.out.println("Invalid parameters.");
                showHelp();
            }
        }
    }

    /**
   * Show help.
   */
    private static void showHelp() {
        Main.showHelp();
    }

    /**
   * Export data. 
   */
    private void exportData() {
        File exportFile = selectFile("selectExportFile", new File("./export.zip"), ZIP_FILTER, JFileChooser.FILES_ONLY, DialogType.SAVE);
        if (exportFile != null) {
            BackupService bm = new BackupService();
            bm.createBackup(exportFile);
        }
    }

    /**
   * Import data.
   */
    private void importData() {
        File importFile = selectFile("selectImportFile", new File("./import.zip"), ZIP_FILTER, JFileChooser.FILES_ONLY, DialogType.OPEN);
        if (importFile != null) {
            if (confirm("realyImport", "realyImportMsg")) {
                BackupService bm = new BackupService();
                bm.loadBackup(importFile);
            }
        }
    }

    /**
   * Crate backup to given file.
   * @param outFolder output foldler for backup.
   */
    public void createBackup(File outFolder) {
        try {
            File inFolder = new File(DB_FOLDER);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outFolder)));
            BufferedInputStream in = null;
            byte[] data = new byte[BUF_SIZE];
            List<File> folders = new ArrayList<File>();
            folders.add(inFolder);
            while (folders.size() != 0) {
                inFolder = folders.get(0);
                File files[] = inFolder.listFiles();
                folders.remove(0);
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        folders.add(files[i]);
                        continue;
                    }
                    in = new BufferedInputStream(new FileInputStream(inFolder.getPath() + "/" + files[i].getName()), BUF_SIZE);
                    out.putNextEntry(new ZipEntry(files[i].getPath()));
                    int count;
                    while ((count = in.read(data, 0, 1000)) != -1) {
                        out.write(data, 0, count);
                    }
                    out.closeEntry();
                }
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
   * Load backup file to application.
   * @param inFile input backup file
   */
    public void loadBackup(File inFile) {
        Enumeration<? extends ZipEntry> entries;
        ZipFile zipFile;
        File dbFolder = new File(DB_FOLDER);
        Installer.deleteDirectory(dbFolder);
        dbFolder.mkdir();
        try {
            zipFile = new ZipFile(inFile);
            entries = zipFile.entries();
            File output;
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                output = new File(entry.getName());
                output.getParentFile().mkdirs();
                if (!output.exists()) {
                    if (!output.createNewFile()) {
                        throw new PackerRuntimeException("Problem with creating file");
                    }
                }
                copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(output)));
            }
            zipFile.close();
        } catch (IOException ioe) {
            throw new PackerRuntimeException("Problem with data import.", ioe);
        }
    }

    /**
   * Copy input stream to output stream.
   * @param in input
   * @param out output
   * @throws IOException some problems with copy
   */
    private void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUF_SIZE];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();
    }

    @Override
    public void activate() {
    }

    @Override
    public void addObserver(Observer o) {
    }

    @Override
    public void deactivate() {
    }

    @Override
    public Component getComponent() {
        return fileChooser;
    }

    @Override
    public void localize() {
        ResourceBundle resourceCore = ResourceBundle.getBundle("cz.zcu.fav.hofhans.resources.view.Core", getComponent().getLocale());
        Enumeration<String> keys = resourceCore.getKeys();
        String key;
        while (keys.hasMoreElements()) {
            key = keys.nextElement();
            UIManager.put(key, resourceCore.getString(key));
        }
    }
}
