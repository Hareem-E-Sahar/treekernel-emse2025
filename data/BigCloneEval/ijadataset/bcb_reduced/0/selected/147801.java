package com.rbnb.applications;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class FileUpdater extends javax.swing.JDialog {

    public FileUpdater() {
        setTitle("File Update Manager");
        final int SPACING = 20;
        setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);
        Container cp = getContentPane();
        JButton jb;
        Box b, b2;
        cp.add(b = new Box(BoxLayout.Y_AXIS), java.awt.BorderLayout.NORTH);
        b.add(new JLabel("Please select the source directory:"));
        b.add(b2 = new Box(BoxLayout.X_AXIS));
        b2.add(sourceTxtBox);
        b2.add(jb = new JButton("Browse"));
        jb.setMnemonic('B');
        jb.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                doBrowse(sourceTxtBox);
            }
        });
        b.add(Box.createVerticalStrut(SPACING));
        b.add(new JLabel("Then select the destination directory:"));
        b.add(b2 = new Box(BoxLayout.X_AXIS));
        b2.add(destTxtBox);
        b2.add(jb = new JButton("Browse"));
        jb.setMnemonic('r');
        jb.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                doBrowse(destTxtBox);
            }
        });
        b.add(Box.createVerticalStrut(SPACING));
        b.add(new JLabel("Select the exception file:"));
        b.add(b2 = new Box(BoxLayout.X_AXIS));
        b2.add(exceptionFileTxtBox);
        b2.add(jb = new JButton("Browse"));
        jb.setToolTipText("The exception file consists of a list of file filters," + " one per line, which should NOT be copied to the destination." + "  The wildcard character may be used at the end of each" + " line to represent all files starting with the characters" + " up to the wildcard.");
        jb.setMnemonic('o');
        jb.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (doBrowse(exceptionFileTxtBox, false)) {
                    parseExceptionFile();
                }
            }
        });
        b.add(Box.createVerticalStrut(SPACING));
        b.add(new JLabel("Select the backup file location:"));
        b.add(b2 = new Box(BoxLayout.X_AXIS));
        b2.add(backupTxtBox);
        b2.add(jb = new JButton("Browse"));
        jb.setMnemonic('w');
        jb.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                doBrowse(backupTxtBox);
            }
        });
        b.add(Box.createVerticalStrut(SPACING));
        b.add(jb = startButton);
        jb.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                (new Thread(new Runnable() {

                    public void run() {
                        startStop();
                    }
                })).start();
            }
        });
        jb.setEnabled(false);
        getRootPane().setDefaultButton(jb);
        b.add(Box.createVerticalStrut(SPACING));
        b.add(new javax.swing.JScrollPane(outputConsole));
        outputConsole.setVisibleRowCount(10);
        outputConsole.setEnabled(false);
        outputConsole.setCellRenderer(new ConsoleCellRenderer());
        sourceTxtBox.setEditable(false);
        destTxtBox.setEditable(false);
        exceptionFileTxtBox.setEditable(false);
        backupTxtBox.setEditable(false);
        try {
            String temp = System.getenv("TEMP");
            if (temp != null && (new File(temp)).exists()) backupTxtBox.setText(temp);
        } catch (Exception e) {
        }
        pack();
        setLocationRelativeTo(null);
    }

    private void copyFiles() throws IOException {
        log("Copying files...");
        File srcRoot = new File(sourceTxtBox.getText());
        File destRoot = new File(destTxtBox.getText());
        copyDir(srcRoot, srcRoot, destRoot);
        log("Copying complete.");
    }

    private String makeRel(Object srcRoot, Object child) {
        return child.toString().substring(srcRoot.toString().length() + 1);
    }

    private void copyDir(File srcRoot, File dir, File destRoot) throws IOException {
        File[] children = dir.listFiles();
        if (children == null) return;
        for (int ii = 0; ii < children.length; ++ii) {
            File child = children[ii];
            String relPath = makeRel(srcRoot, child);
            if (checkExceptionList(relPath)) continue;
            if (child.isDirectory()) {
                File destDir = new File(destRoot, relPath);
                destDir.mkdirs();
                copyDir(srcRoot, child, destRoot);
            } else {
                log(child.toString());
                File destFile = new File(destRoot, relPath);
                copyFile(child, destFile);
            }
        }
    }

    private void copyFile(File src, File dest) throws IOException {
        FileInputStream fis = new FileInputStream(src);
        FileOutputStream fos = new FileOutputStream(dest);
        int len;
        while ((len = fis.read(fileBuffer)) != -1) {
            fos.write(fileBuffer, 0, len);
        }
        fos.close();
        fis.close();
        dest.setLastModified(src.lastModified());
    }

    private boolean checkExceptionList(String name) {
        for (Iterator iter = exceptionList.iterator(); iter.hasNext(); ) {
            String filter = iter.next().toString();
            int ii = filter.indexOf('*');
            if (ii == -1) {
                if (name.equals(filter)) return true;
            } else if (name.regionMatches(0, filter, 0, ii)) {
                return true;
            }
        }
        return false;
    }

    private void createBackup() throws IOException {
        String fname = "FileUpdaterBackup_" + (new java.text.SimpleDateFormat("yyyyMMdd_HHmm")).format(new java.util.Date()) + ".zip";
        File backupFile = new File(backupTxtBox.getText(), fname);
        log("Creating backup at:");
        log("        " + backupFile);
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(backupFile));
        backupDir(zos, destTxtBox.getText(), new File(destTxtBox.getText()));
        zos.finish();
        zos.close();
        log("Backup complete.");
    }

    private void backupDir(ZipOutputStream zos, String root, File dir) throws IOException {
        if (!root.equals(dir.toString())) {
            String relDir = dir.toString().substring(root.length() + 1) + '/';
            zos.putNextEntry(new ZipEntry(relDir));
        }
        File[] children = dir.listFiles();
        if (children == null) return;
        for (int ii = 0; ii < children.length; ++ii) {
            File child = children[ii];
            log(child.toString());
            if (child.isDirectory()) backupDir(zos, root, child); else {
                String relFile = child.toString().substring(root.length() + 1);
                ZipEntry ze = new ZipEntry(relFile);
                ze.setSize(child.length());
                ze.setTime(child.lastModified());
                zos.putNextEntry(ze);
                FileInputStream fis = new FileInputStream(child);
                int len;
                while ((len = fis.read(fileBuffer)) != -1) {
                    crc32.update(fileBuffer, 0, len);
                    zos.write(fileBuffer, 0, len);
                }
                fis.close();
                ze.setCrc(crc32.getValue());
            }
        }
    }

    private boolean doBrowse(JTextField outTxtBox) {
        return doBrowse(outTxtBox, true);
    }

    private boolean doBrowse(JTextField outTxtBox, boolean dirOnly) {
        if (dirOnly) dirBrowser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); else dirBrowser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        dirBrowser.setSelectedFile(new File(outTxtBox.getText()));
        if (dirBrowser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = dirBrowser.getSelectedFile();
            if (!f.exists()) {
                showError("Error selecting directory:", f.toString() + " is not a directory!");
                return false;
            } else outTxtBox.setText(f.toString());
            if (sourceTxtBox.getText().length() > 0 && destTxtBox.getText().length() > 0 && backupTxtBox.getText().length() > 0) {
                startButton.setEnabled(true);
            }
            return true;
        }
        return false;
    }

    class LogMessage implements Runnable {

        public LogMessage(String s) {
            this.s = s;
        }

        public void run() {
            ((javax.swing.DefaultListModel) outputConsole.getModel()).addElement(s);
            outputConsole.ensureIndexIsVisible(outputConsole.getModel().getSize() - 1);
        }

        private final String s;
    }

    private void log(String s) {
        SwingUtilities.invokeLater(new LogMessage(s));
    }

    private void parseExceptionFile() {
        try {
            FileReader fr = new FileReader(exceptionFileTxtBox.getText());
            BufferedReader br = new BufferedReader(fr);
            exceptionList.clear();
            while (true) {
                String s = br.readLine();
                if (s == null) break;
                if (File.separatorChar == '/') s = s.replace('\\', '/'); else s = s.replace('/', '\\');
                exceptionList.add(s);
            }
            fr.close();
        } catch (IOException ioe) {
            showError("Error reading exception file:", ioe.getMessage());
        }
    }

    private void showError(String title, String msg) {
        javax.swing.JOptionPane.showMessageDialog(this, msg, title, javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    private void startStop() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setEnabled(false);
        try {
            try {
                createBackup();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                showError("I/O Error creating backup:", ioe.getMessage());
                return;
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error creating backup:", e.getMessage());
                return;
            }
            try {
                copyFiles();
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error copying files:", e.getMessage());
            }
        } finally {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    public void run() {
                        setEnabled(true);
                        setCursor(Cursor.getDefaultCursor());
                    }
                });
            } catch (Exception e) {
            }
        }
    }

    private final JTextField sourceTxtBox = new JTextField(), destTxtBox = new JTextField(), backupTxtBox = new JTextField(), exceptionFileTxtBox = new JTextField();

    private final JButton startButton = new JButton("Start");

    private final JList outputConsole = new JList(new javax.swing.DefaultListModel());

    private final JFileChooser dirBrowser = new JFileChooser();

    /**
	  * The list of files not to be copied.
	  */
    private final java.util.ArrayList exceptionList = new java.util.ArrayList();

    /** 
	  * Used in backup zip-file generation.
	  */
    private final CRC32 crc32 = new CRC32();

    private final byte[] fileBuffer = new byte[4096];

    static class ConsoleCellRenderer extends JLabel implements javax.swing.ListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String s = value.toString();
            setText(s);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setEnabled(true);
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }

    public static void main(String[] args) {
        (new FileUpdater()).setVisible(true);
    }
}
