package org.gerhardb.jibs.util;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.zip.*;
import javax.swing.*;
import org.gerhardb.lib.dirtree.filelist.popup.FileName;
import org.gerhardb.lib.io.*;
import org.gerhardb.lib.swing.JFileChooserExtra;
import org.gerhardb.lib.swing.JPanelRows;
import org.gerhardb.lib.swing.SwingUtils;
import org.gerhardb.lib.util.Icons;
import org.gerhardb.lib.util.StopCheck;
import org.gerhardb.lib.util.startup.AppStarter;

/**
 * The Unzip option will unzip all of the zip files found in the root directory to the root directory.
 * It creates a directory with the file name of the zip file minus the zip.
 * It aborts in the case it would over write a file.  This takes care of a case sensitive operating system with 
 * files named foo.zip and foo.ZIP.  No problem if there contents don't match, you will get a merge.
 * Subdirectories are correctly unzipped.
 * 
 * 
 */
public class FileRenameWithDirName extends JFrame implements StopCheck, Runnable {

    private static final int BUFFER = 2048;

    private static final String ZIP_FILE = "ZipFile";

    private static final String SUB_DIR = "SubFile";

    private static final String MOVE_FILES = "MoveFile";

    private static final String PATTERN = "Pattern";

    private static final String USE_DIR_NAMES = "UseDirNaems";

    private static final String IGNORE = "Ignore";

    private static final String LAST_ROOT = "LastRoot";

    private static final Preferences clsPrefs = Preferences.userRoot().node("/org/gerhardb/jibs/util/FileRenameWithDirName");

    JTextField myRootDir = new JTextField(60);

    JProgressBar myProgressBar = new JProgressBar();

    boolean iStop = false;

    JButton myStopBtn = new JButton("Stop");

    String myRootString;

    JCheckBox myZipFiles = new JCheckBox("Unzip Zip Files - will create subdirectories with file name");

    JCheckBox myRename = new JCheckBox("Rename files in subdirectories - Pattern: ");

    JTextField myPattern = new JTextField(20);

    JCheckBox iUseDirectoryNames = new JCheckBox("Add directories to name ignoring (comma separated list): ");

    JTextField myIgnore = new JTextField(70);

    JCheckBox myMoveFilesToRootDirectory = new JCheckBox("Move files to root directory");

    boolean iExitOnClose;

    EzLogger myLogger;

    /**
	 * Zip functionality ready to be wired in.
	 */
    public FileRenameWithDirName(boolean exitOnClose) {
        super("Rename Files with Directory Name");
        this.iExitOnClose = exitOnClose;
        if (this.iExitOnClose) {
            this.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent evt) {
                    System.exit(0);
                }
            });
        }
        layoutComponents();
        this.myRootDir.setText(clsPrefs.get(LAST_ROOT, null));
        this.setIconImage(Icons.getIcon(Icons.JIBS_16).getImage());
        this.myZipFiles.setSelected(clsPrefs.getBoolean(ZIP_FILE, false));
        this.myRename.setSelected(clsPrefs.getBoolean(SUB_DIR, true));
        this.myMoveFilesToRootDirectory.setSelected(clsPrefs.getBoolean(MOVE_FILES, true));
        this.myPattern.setText(clsPrefs.get(PATTERN, "Jibs-#"));
        this.iUseDirectoryNames.setSelected(clsPrefs.getBoolean(USE_DIR_NAMES, true));
        this.myIgnore.setText(clsPrefs.get(IGNORE, "images,thumbnails"));
        renameUpdated();
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                FileRenameWithDirName.this.pack();
                SwingUtils.centerOnScreen(FileRenameWithDirName.this);
                FileRenameWithDirName.this.setVisible(true);
            }
        });
        this.myRename.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                renameUpdated();
            }
        });
        this.iUseDirectoryNames.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                renameUpdated();
            }
        });
    }

    @Override
    public boolean isStopped() {
        return this.iStop;
    }

    void renameUpdated() {
        if (this.myRename.isSelected()) {
            this.myPattern.setEnabled(true);
            this.iUseDirectoryNames.setEnabled(true);
            if (this.iUseDirectoryNames.isSelected()) {
                this.myIgnore.setEnabled(true);
            } else {
                this.myIgnore.setEnabled(false);
            }
        } else {
            this.myPattern.setEnabled(false);
            this.iUseDirectoryNames.setEnabled(false);
            this.myIgnore.setEnabled(false);
        }
    }

    private void layoutComponents() {
        this.setSize(new Dimension(600, 600));
        this.myProgressBar.setStringPainted(true);
        this.myProgressBar.setIndeterminate(false);
        JButton goBtn = new JButton("Process");
        goBtn.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Thread t = new Thread(FileRenameWithDirName.this);
                t.start();
            }
        });
        this.myStopBtn.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FileRenameWithDirName.this.iStop = true;
            }
        });
        JButton rootBtn = new JButton("...");
        rootBtn.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectRoot();
            }
        });
        JPanelRows topPanel = new JPanelRows();
        JPanel aRow = topPanel.topRow(FlowLayout.CENTER);
        aRow.add(new JLabel("Root Directory: "));
        aRow.add(this.myRootDir);
        aRow.add(rootBtn);
        aRow = topPanel.nextRow();
        aRow.add(this.myZipFiles);
        aRow = topPanel.nextRow();
        aRow.add(this.myRename);
        aRow.add(this.myPattern);
        aRow.add(new JLabel(AppStarter.getString("FileNameChangeCompleteTab.6")));
        aRow = topPanel.nextRow();
        aRow.add(this.iUseDirectoryNames);
        aRow = topPanel.nextRow();
        aRow.add(this.myIgnore);
        aRow = topPanel.nextRow();
        aRow.add(this.myMoveFilesToRootDirectory);
        aRow = topPanel.nextRow();
        aRow.add(goBtn);
        aRow.add(this.myStopBtn);
        JPanel content = new JPanel(new BorderLayout());
        content.add(topPanel, BorderLayout.CENTER);
        content.add(this.myProgressBar, BorderLayout.SOUTH);
        this.setContentPane(content);
    }

    void selectRoot() {
        JFileChooserExtra chooser = new JFileChooserExtra(clsPrefs.get(LAST_ROOT, null));
        chooser.setSaveName("FileRenameWithDirName", "Select Root Directory");
        chooser.setApproveButtonText("Select Root Directory");
        chooser.setDialogTitle("Root");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.myProgressBar.setValue(0);
            this.myProgressBar.setString(" ");
            File picked = chooser.getSelectedFile();
            if (picked != null) {
                this.myRootDir.setText(picked.toString());
                try {
                    clsPrefs.put(LAST_ROOT, picked.toString());
                    clsPrefs.flush();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void run() {
        this.myProgressBar.setString("Reading Directories");
        this.myProgressBar.setValue(0);
        clsPrefs.putBoolean(ZIP_FILE, this.myZipFiles.isSelected());
        clsPrefs.putBoolean(SUB_DIR, this.myRename.isSelected());
        clsPrefs.putBoolean(MOVE_FILES, this.myMoveFilesToRootDirectory.isSelected());
        clsPrefs.put(PATTERN, this.myPattern.getText());
        clsPrefs.putBoolean(USE_DIR_NAMES, this.iUseDirectoryNames.isSelected());
        clsPrefs.put(IGNORE, this.myIgnore.getText());
        this.myRootString = this.myRootDir.getText();
        File rootDir = new File(this.myRootString);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Root directory must be a directory", "Problem", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            this.myLogger = EzLogger.makeEzLogger(rootDir);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not open log file in:\n " + rootDir, "Problem", JOptionPane.ERROR_MESSAGE);
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            if (this.myZipFiles.isSelected()) {
                unzipFiles(rootDir.listFiles(new EndingFileFilter("zip")));
            }
            if (this.myRename.isSelected() || this.myMoveFilesToRootDirectory.isSelected()) {
                ListSubdirectories list = new ListSubdirectories(rootDir);
                if (this.myRename.isSelected()) {
                    renameLoop(rootDir, list);
                }
                if (this.myMoveFilesToRootDirectory.isSelected()) {
                    moveFilesToTop(rootDir, list);
                    deleteEmptyDirectories(rootDir);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Problem Encountered", JOptionPane.ERROR_MESSAGE);
        }
        try {
            this.myLogger.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        this.myProgressBar.setString("Done");
    }

    private void moveFilesToTop(File rootDir, ListSubdirectories list) throws Exception {
        File[] dirs = list.getSubDirectoryList();
        this.myProgressBar.setValue(0);
        this.myProgressBar.setMaximum(dirs.length);
        for (int i = 0; i < dirs.length; i++) {
            File[] files = dirs[i].listFiles(FilesOnlyFileFilter.FILES_ONLY);
            moveFilesToTop(rootDir, files);
            this.myProgressBar.setValue(i);
        }
        this.myProgressBar.setValue(this.myProgressBar.getMaximum());
    }

    private void renameLoop(File rootDir, ListSubdirectories list) {
        File[] dirs = list.getSubDirectoryList();
        this.myProgressBar.setValue(0);
        this.myProgressBar.setMaximum(dirs.length);
        FileName fileName = new FileName(list.getFileCount());
        fileName.setTemplate(this.myPattern.getText());
        int nextFileNumber = 1;
        ArrayList<String> exclude = new ArrayList<String>(20);
        StringTokenizer st = new StringTokenizer(this.myIgnore.getText(), ",");
        while (st.hasMoreTokens()) {
            exclude.add(st.nextToken().trim());
        }
        if (this.myRename.isSelected()) {
            File[] files = rootDir.listFiles(FilesOnlyFileFilter.FILES_ONLY);
            if (this.iUseDirectoryNames.isSelected()) {
                nextFileNumber = renameFilesDir(rootDir, rootDir, files, nextFileNumber, fileName, exclude);
            } else {
                nextFileNumber = renameFilesFlat(files, nextFileNumber, fileName);
            }
        }
        for (int i = 0; i < dirs.length; i++) {
            this.myLogger.logLine(EzLogger.NEW_LINE + EzLogger.NEW_LINE + "Processing Directory: " + dirs[i].getName() + EzLogger.NEW_LINE);
            this.myProgressBar.setString("Processing subdirectory: " + dirs[i].getName());
            this.myProgressBar.setValue(i);
            File[] files = dirs[i].listFiles(FilesOnlyFileFilter.FILES_ONLY);
            Arrays.sort(files);
            if (this.myRename.isSelected()) {
                if (this.iUseDirectoryNames.isSelected()) {
                    nextFileNumber = renameFilesDir(rootDir, dirs[i], files, nextFileNumber, fileName, exclude);
                } else {
                    nextFileNumber = renameFilesFlat(files, nextFileNumber, fileName);
                }
            }
        }
        this.myProgressBar.setValue(this.myProgressBar.getMaximum());
    }

    private void unzipFiles(File[] zipFiles) throws Exception {
        this.myProgressBar.setValue(0);
        this.myProgressBar.setMaximum(zipFiles.length);
        Arrays.sort(zipFiles, new FileNameComparatorInsensative());
        for (int i = 0; i < zipFiles.length; i++) {
            this.myLogger.logLine(EzLogger.NEW_LINE + "Unzipping: " + zipFiles[i] + EzLogger.NEW_LINE);
            this.myProgressBar.setString("Processing Zip File: " + zipFiles[i].getName());
            this.myProgressBar.setValue(i);
            String dirToSaveTo = this.myRootString + "/" + FileUtil.fileNameNoExtension(zipFiles[i].getName()) + "/";
            try {
                ArrayList<String> nameList = new ArrayList<String>(100);
                ZipFile zipFile = new ZipFile(zipFiles[i]);
                Enumeration<?> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) entries.nextElement();
                    if (!entry.isDirectory()) {
                        nameList.add(entry.getName());
                    }
                }
                String[] names = new String[nameList.size()];
                names = nameList.toArray(names);
                Arrays.sort(names, new FileNameStringComparatorInsensative());
                for (int j = 0; j < names.length; j++) {
                    ZipEntry entry = zipFile.getEntry(names[j]);
                    String nameWithRoot = dirToSaveTo + entry.getName();
                    if (new File(nameWithRoot).exists()) {
                        throw new Exception("Aborting unzip because of duplicate file:\n " + nameWithRoot);
                    }
                    BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry));
                    int count;
                    byte data[] = new byte[BUFFER];
                    File entryFile = new File(nameWithRoot);
                    File parent = entryFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    this.myLogger.logLine("    Inflating: " + nameWithRoot);
                    BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(nameWithRoot), BUFFER);
                    while ((count = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }
                zipFile.close();
            } catch (Exception ex) {
                System.out.println(ex);
                ex.printStackTrace();
                this.myLogger.logLine(ex.getMessage());
            }
        }
        this.myProgressBar.setValue(this.myProgressBar.getMaximum());
    }

    private void moveFilesToTop(File rootDir, File[] files) {
        for (int j = 0; j < files.length; j++) {
            if (files[j] != null) {
                FileUtil.moveFileToDir(rootDir, files[j], this);
                this.myLogger.logLine("     Moving: " + files[j].getName() + " to " + rootDir);
            }
        }
    }

    private int renameFilesFlat(File[] inputFileList, int nextFileNumber, FileName fileName) {
        for (int i = 0; i < inputFileList.length; i++) {
            try {
                String newName = fileName.getNewName(nextFileNumber, inputFileList[i].getName());
                String dir = inputFileList[i].getParentFile().getAbsolutePath();
                File newFileName = new File(dir + "/" + newName);
                if (newFileName.exists()) {
                    String entry = "There is already a file named: " + newFileName + EzLogger.NEW_LINE;
                    this.myLogger.logLine(entry);
                } else if (inputFileList[i].getName().toLowerCase().endsWith("zip")) {
                } else {
                    inputFileList[i].renameTo(newFileName);
                    String entry = "Renamed: " + inputFileList[i] + EzLogger.NEW_LINE + "to: " + newFileName + EzLogger.NEW_LINE;
                    this.myLogger.logLine(entry);
                }
            } catch (Exception ex) {
                System.out.println("FileRenameWithDirName.renameFiles-flat: " + ex.getMessage());
            }
            nextFileNumber++;
        }
        return nextFileNumber;
    }

    private int renameFilesDir(File rootDir, File currentDir, File[] inputFileList, int nextFileNumber, FileName fileName, ArrayList<String> exclude) {
        String currentDirPath = "";
        String pattern = this.myPattern.getText();
        if (rootDir != currentDir) {
            currentDirPath = currentDir.getAbsolutePath();
            currentDirPath = currentDirPath.substring(rootDir.getAbsolutePath().length() + 1);
            Iterator<String> itr = exclude.iterator();
            while (itr.hasNext()) {
                currentDirPath = currentDirPath.replace(itr.next(), "");
            }
            currentDirPath = currentDirPath.replace(File.separator + File.separator, File.separator);
            currentDirPath = currentDirPath.replace(File.separator, "_");
            int maxDirPathSize = 253 - rootDir.getAbsolutePath().length() - pattern.length() - fileName.getDigitsLength();
            if (currentDirPath.length() > maxDirPathSize) {
                currentDirPath = currentDirPath.substring(0, maxDirPathSize);
            }
            pattern = currentDirPath + "_" + pattern;
        }
        fileName.setTemplate(pattern);
        for (int i = 0; i < inputFileList.length; i++) {
            try {
                String newName = fileName.getNewName(nextFileNumber, inputFileList[i].getName());
                String dir = inputFileList[i].getParentFile().getAbsolutePath();
                File renameInPlace = new File(dir + "/" + newName);
                File renameAndMaybeMove = renameInPlace;
                if (this.myMoveFilesToRootDirectory.isSelected()) {
                    renameAndMaybeMove = new File(rootDir.getAbsolutePath() + "/" + newName);
                }
                if (renameInPlace.exists()) {
                    String entry = "There is already a file named: " + renameInPlace + EzLogger.NEW_LINE;
                    this.myLogger.logLine(entry);
                } else if (inputFileList[i].getName().toLowerCase().endsWith("zip")) {
                } else {
                    if (inputFileList[i].renameTo(renameAndMaybeMove)) {
                        String entry = "Renamed: " + inputFileList[i] + EzLogger.NEW_LINE + "to:      " + renameAndMaybeMove + EzLogger.NEW_LINE;
                        this.myLogger.logLine(entry);
                    } else {
                        if (inputFileList[i].renameTo(renameInPlace)) {
                            String entry = "Renamed: " + inputFileList[i] + EzLogger.NEW_LINE + "to:      " + renameInPlace + EzLogger.NEW_LINE;
                            this.myLogger.logLine(entry);
                        } else {
                            String entry = "FAILED to rename: " + inputFileList[i] + EzLogger.NEW_LINE + "to:      " + renameInPlace + EzLogger.NEW_LINE;
                            this.myLogger.logLine(entry);
                        }
                    }
                }
            } catch (Exception ex) {
                System.out.println("FileRenameWithDirName.renameFiles-flat: " + ex.getMessage());
            }
            nextFileNumber++;
        }
        return nextFileNumber;
    }

    public static String getNewName(String oldName, int i, String newBase, DecimalFormat formatter) {
        String baseName = newBase + "-" + formatter.format(i);
        if (oldName == null) {
            return baseName;
        }
        int lastPeriod = oldName.lastIndexOf('.');
        if (lastPeriod < 0 || oldName.length() == 1) {
            return baseName;
        }
        String oldEnding = oldName.substring(lastPeriod + 1).toLowerCase();
        return baseName + "." + oldEnding;
    }

    void deleteEmptyDirectories(File rootDir) {
        File[] dirs = rootDir.listFiles(DirectoriesOnlyFileFilter.DIRECTORIES_ONLY);
        for (int i = 0; i < dirs.length; i++) {
            deleteEmptyDirectories(dirs[i]);
            if (dirs[i].list().length == 0) {
                dirs[i].delete();
            }
        }
    }

    public static void main(String[] args) {
        AppStarter.startUpApp(args, "org.gerhardb.jibs.Jibs", true);
        new FileRenameWithDirName(true);
    }
}
