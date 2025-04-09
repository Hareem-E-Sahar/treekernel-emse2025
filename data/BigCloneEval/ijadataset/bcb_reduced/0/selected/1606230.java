package org.microskills.ZIPAnywhere;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.*;

public class ZipTablePanel extends JPanel implements ActionListener, Printable {

    public ZipTablePanel() throws IOException {
        defaultViewerType = 0;
        currentArchive = null;
        workingDir = new File(".");
        ziptm = new ZipTableModel();
        table = new JTable(ziptm);
        maxNumPage = 1;
        statusPanel = new JPanel();
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("DialogInput", Font.PLAIN, 13));
        statusLabel.setForeground(Color.black);
        propLabel = new JLabel("");
        popMenu = new JPopupMenu("Actions");
        addMenuItem = new JMenuItem("Add...");
        delMenuItem = new JMenuItem("Delete...");
        extractMenuItem = new JMenuItem("Extract...");
        viewMenuItem = new JMenuItem("View...");
        selAllMenuItem = new JMenuItem("Select All");
        deSelAllMenuItem = new JMenuItem("DeSelect All");
        sortMenu = new JMenu("Sort");
        sortNameMenuItem = new JRadioButtonMenuItem("By File Name");
        sortTimeMenuItem = new JRadioButtonMenuItem("By File Modified Time");
        sortSizeMenuItem = new JRadioButtonMenuItem("By File Size");
        sortPathMenuItem = new JRadioButtonMenuItem("By Directory Path");
        overwriteCheckBox = new JCheckBox("Overwrite Existing Files");
        keepDirCheckBox = new JCheckBox("Keep Directory Names");
        selFilesRadioButton = new JRadioButton("Only Selected Files");
        allFilesRadioButton = new JRadioButton("All Files");
        retrieveSubCheckBox = new JCheckBox("Retrieve All Sub-directores");
        saveDirCheckBox = new JCheckBox("Save Directory Names");
        compressMethodComboBox = new JComboBox(methods);
        root = this;
        setLayout(new BorderLayout());
        setBackground(Color.white);
        table.setSelectionMode(2);
        setupPopupMenu();
        table.setUI(new WinBasicTableUI());
        setupRenderers();
        header = table.getTableHeader();
        table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {

            public void columnAdded(TableColumnModelEvent tablecolumnmodelevent) {
            }

            public void columnMarginChanged(ChangeEvent changeevent) {
            }

            public void columnMoved(TableColumnModelEvent tablecolumnmodelevent) {
                if (ziptm.getSortedColumn() == tablecolumnmodelevent.getFromIndex()) {
                    ziptm.setSortedColumn(tablecolumnmodelevent.getToIndex());
                }
            }

            public void columnRemoved(TableColumnModelEvent tablecolumnmodelevent) {
            }

            public void columnSelectionChanged(ListSelectionEvent listselectionevent) {
            }
        });
        overwriteCheckBox.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent mouseevent) {
                if (!overwriteCheckBox.isSelected()) {
                    return;
                }
                int i = JOptionPane.showConfirmDialog((JComponent) mouseevent.getSource(), "If you select this option, program will automatically overwrite \nthe existing files without asking you. \nDo you really want to select this option?", "Warning", 0, 2, null);
                if (i == 0) {
                    overwriteCheckBox.setSelected(true);
                } else {
                    overwriteCheckBox.setSelected(false);
                }
            }
        });
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent listselectionevent) {
                int i = table.getSelectedRowCount();
                statusLabel.setText(i + " file(s) selected");
                statusLabel.setIcon(null);
            }
        });
        table.setAutoResizeMode(0);
        JTableHeader jtableheader = table.getTableHeader();
        jtableheader.setUpdateTableInRealTime(true);
        jtableheader.setReorderingAllowed(true);
        this.add(new JScrollPane(table), BorderLayout.CENTER);
        add(statusPanel, "South");
        statusPanel.setLayout(new StatusBarLayout(3, 150));
        statusPanel.add(statusLabel);
        statusPanel.add(propLabel);
        statusLabel.setBorder(BorderFactory.createBevelBorder(1));
        propLabel.setBorder(BorderFactory.createBevelBorder(1));
    }

    class DateRenderer extends DefaultTableCellRenderer {

        protected void setValue(Object obj) {
            if (obj != null) {
                setText(obj.toString());
            }
        }

        SimpleDateFormat formatter;

        DateRenderer() {
            formatter = new SimpleDateFormat("MM/dd/yyyy hh:mma");
        }
    }

    class RatioRenderer extends DefaultTableCellRenderer {

        protected void setValue(Object obj) {
            if (obj != null) {
                setText(obj.toString());
                setHorizontalAlignment(4);
            }
        }

        NumberFormat formatter;

        RatioRenderer() {
            formatter = NumberFormat.getPercentInstance();
        }
    }

    class CrcRenderer extends DefaultTableCellRenderer {

        protected void setValue(Object obj) {
            if (obj != null) {
                setText(obj.toString());
            }
        }

        CrcRenderer() {
        }
    }

    class WinBasicTableUI extends BasicTableUI {

        WinBasicTableUI() {
        }
    }

    class Deleter extends Thread {

        public void run() {
            root.setCursor(Cursor.getPredefinedCursor(3));
            try {
                tempArchiveFile = File.createTempFile("JZip", ".zip");
                if (table.getRowCount() != delRows.length) {
                    stream = new FileOutputStream(tempArchiveFile);
                    out = new ZipOutputStream(stream);
                    boolean aflag[] = new boolean[table.getRowCount()];
                    for (int i = 0; i < table.getRowCount(); i++) {
                        aflag[i] = false;
                    }
                    for (int j = 0; j < delRows.length; j++) {
                        aflag[delRows[j]] = true;
                    }
                    ZipFile zipfile = new ZipFile(archiveFile);
                    for (int k = 0; k < table.getRowCount(); k++) {
                        if (!aflag[k]) {
                            Object obj = ziptm.getRealValueAt(k, 6);
                            String s1 = ziptm.getRealValueAt(k, 0).toString();
                            String s2 = s1;
                            String s3 = "";
                            if (obj != null) {
                                String s4 = obj.toString();
                                s2 = s4 + s1;
                            }
                            ZipEntry zipentry = zipfile.getEntry(s2.replace(File.separatorChar, '/'));
                            if (zipentry == null) {
                                System.out.println("The entry with name<" + s2 + "> is null");
                            } else {
                                InputStream inputstream = zipfile.getInputStream(zipentry);
                                out.putNextEntry(zipentry);
                                do {
                                    int l = inputstream.read(buffer, 0, buffer.length);
                                    if (l <= 0) {
                                        break;
                                    }
                                    out.write(buffer, 0, l);
                                } while (true);
                                inputstream.close();
                                out.closeEntry();
                            }
                        }
                    }
                    zipfile.close();
                    out.close();
                    stream.close();
                }
                String s = archiveFile.getAbsolutePath();
                archiveFile.delete();
                tempArchiveFile.renameTo(new File(s));
                tempArchiveFile.getCanonicalPath();
                openArchive(new File(s));
                table.clearSelection();
                setStatus("Totally " + delRows.length + " file(s) deleted from this archive!");
            } catch (Exception exception) {
                exception.printStackTrace();
                setStatus("Error: " + exception.getMessage());
            }
            root.setCursor(Cursor.getPredefinedCursor(0));
            notifyAll();
        }

        File archiveFile;

        File tempArchiveFile;

        FileOutputStream stream;

        ZipOutputStream out;

        int[] delRows;

        byte[] buffer;

        public Deleter(File file) {
            delRows = table.getSelectedRows();
            buffer = new byte[1024];
            archiveFile = file;
        }
    }

    class Adder extends Thread {

        public void addAllDir(File file) {
            try {
                String s = file.getCanonicalPath().substring(baseDirLength + 1).replace(File.separatorChar, '/') + "/";
                ZipEntry zipentry = new ZipEntry(s);
                zipentry.setTime(file.lastModified());
                try {
                    out.putNextEntry(zipentry);
                } catch (Exception exception) {
                    JOptionPane.showConfirmDialog(null, "err in writing" + zipentry + exception);
                }
            } catch (Exception _ex) {
                System.out.println("Error in adding directory " + file);
            }
            File afile[] = file.listFiles();
            for (int i = 0; i < afile.length; i++) {
                if (afile[i].isDirectory()) {
                    addAllDir(afile[i]);
                } else {
                    addOneFile(afile[i]);
                }
            }
        }

        public void addOneFile(File file) {
            try {
                if (file == null || !file.exists() || file.isDirectory()) {
                    return;
                }
                filesAdded++;
                String s = file.getCanonicalPath().substring(baseDirLength + 1).replace(File.separatorChar, '/');
                ZipEntry zipentry = new ZipEntry(s);
                zipentry.setTime(file.lastModified());
                try {
                    out.putNextEntry(zipentry);
                } catch (Exception exception1) {
                    System.out.println(exception1);
                }
                FileInputStream fileinputstream = new FileInputStream(file);
                do {
                    int i = fileinputstream.read(buffer, 0, buffer.length);
                    if (i <= 0) {
                        break;
                    }
                    out.write(buffer, 0, i);
                } while (true);
                fileinputstream.close();
            } catch (Exception exception) {
                System.out.println("Error in adding " + file + ":" + exception);
            }
        }

        public void run() {
            root.setCursor(Cursor.getPredefinedCursor(3));
            try {
                Thread.sleep(30L);
                tempArchiveFile = File.createTempFile("JZip", ".zip");
                stream = new FileOutputStream(tempArchiveFile);
                out = new ZipOutputStream(stream);
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory() && retrieveSubCheckBox.isSelected()) {
                        addAllDir(files[i]);
                    } else {
                        addOneFile(files[i]);
                    }
                }
                if (archiveFile.length() != 0L) {
                    ZipFile zipfile = new ZipFile(archiveFile);
                    Enumeration enumeration = zipfile.entries();
                    while (enumeration.hasMoreElements()) {
                        ZipEntry zipentry = (ZipEntry) enumeration.nextElement();
                        ZipEntry zipentry1 = new ZipEntry(zipentry.getName());
                        zipentry1.setTime(zipentry.getTime());
                        zipentry1.setComment(zipentry.getComment());
                        zipentry1.setExtra(zipentry.getExtra());
                        try {
                            out.putNextEntry(zipentry1);
                        } catch (Exception _ex) {
                            continue;
                        }
                        InputStream inputstream = zipfile.getInputStream(zipentry);
                        do {
                            int j = inputstream.read(buffer, 0, buffer.length);
                            if (j <= 0) {
                                break;
                            }
                            out.write(buffer, 0, j);
                        } while (true);
                        inputstream.close();
                    }
                    zipfile.close();
                }
                out.close();
                stream.close();
                String s = archiveFile.getAbsolutePath();
                archiveFile.delete();
                tempArchiveFile.renameTo(new File(s));
                tempArchiveFile.getCanonicalPath();
                openArchive(currentArchive);
                setStatus("Totally " + filesAdded + " file(s) added into archive " + archiveFile);
            } catch (Exception exception) {
                exception.printStackTrace();
                setStatus("Error: " + exception.getMessage());
            }
            root.setCursor(Cursor.getPredefinedCursor(0));
        }

        int baseDirLength;

        int filesAdded;

        int compressMethod;

        int compressLevel;

        File[] files;

        File archiveFile;

        File tempArchiveFile;

        FileOutputStream stream;

        ZipOutputStream out;

        byte[] buffer;

        public Adder(File file, File[] afile, File file1) {
            buffer = new byte[1024];
            filesAdded = 0;
            archiveFile = file;
            files = afile;
            try {
                baseDirLength = file1.getCanonicalPath().length();
                if (file1.getCanonicalPath().endsWith(File.separator)) {
                    baseDirLength--;
                }
            } catch (Exception _ex) {
                System.out.println("Failed in getting baseDir path length!");
            }
        }
    }

    public void actionPerformed(ActionEvent actionevent) {
        String s = actionevent.getActionCommand();
        repaint(0L);
        if (s.equals("extract")) {
            extractFiles();
        } else if (s.equals("add")) {
            addFiles();
        } else if (s.equals("del")) {
            deleteFiles();
        } else if (s.equals("view")) {
            viewFile();
        } else if (s.equals("selAll")) {
            table.getSelectionModel().setSelectionInterval(0, table.getRowCount() - 1);
        } else if (s.equals("deSelAll")) {
            table.getSelectionModel().clearSelection();
        } else if (s.equals("sortName") || s.equals("sortSize") || s.equals("sortTime") || s.equals("sortPath")) {
            byte byte0 = 0;
            if (s.equals("sortName")) {
                byte0 = 0;
            } else if (s.equals("sortSize")) {
                byte0 = 1;
            } else if (s.equals("sortTime")) {
                byte0 = 3;
            } else if (s.equals("sortPath")) {
                byte0 = 6;
            }
            ziptm.sortByColumn(table, byte0);
        }
    }

    public void addFiles() {
        JFileChooser jfilechooser = new JFileChooser();
        jfilechooser.setCurrentDirectory(new File("."));
        jfilechooser.setDialogType(0);
        jfilechooser.setDialogTitle("Select Files/Directories to Add to " + currentArchive.getName());
        jfilechooser.setMultiSelectionEnabled(true);
        jfilechooser.setFileSelectionMode(2);
        JPanel jpanel = new JPanel(new GridLayout(0, 2));
        JPanel jpanel1 = new JPanel();
        JPanel jpanel2 = new JPanel(new GridLayout(2, 0));
        jpanel.add(jpanel2);
        jpanel1.setBorder(new TitledBorder("Compression Method"));
        jpanel2.setBorder(new TitledBorder("File Selection Options"));
        jpanel1.add(compressMethodComboBox);
        jpanel2.add(retrieveSubCheckBox);
        retrieveSubCheckBox.setSelected(true);
        saveDirCheckBox.setSelected(true);
        if (jfilechooser.showDialog(this, "Add...") != 0) {
            return;
        } else {
            File file = jfilechooser.getCurrentDirectory();
            File afile[] = getSelectedFiles(jfilechooser);
            Adder adder = new Adder(currentArchive, afile, file);
            adder.start();
            return;
        }
    }

    public void clearHeaderSorterSelection() {
    }

    public void closeArchive() {
        currentArchive = null;
        ziptm.closeArchive();
        table.clearSelection();
        propLabel.setText("");
        statusLabel.setText("");
        statusLabel.setIcon(null);
    }

    protected void createZipArchive(File file, File[] afile) {
        try {
            byte abyte0[] = new byte[1024];
            FileOutputStream fileoutputstream = new FileOutputStream(file);
            ZipOutputStream zipoutputstream = new ZipOutputStream(fileoutputstream);
            for (int i = 0; i < afile.length; i++) {
                if (afile[i] != null && afile[i].exists() && !afile[i].isDirectory()) {
                    setStatus("Adding " + afile[i].getName());
                    ZipEntry zipentry = new ZipEntry(afile[i].getName());
                    zipentry.setTime(afile[i].lastModified());
                    zipoutputstream.putNextEntry(zipentry);
                    FileInputStream fileinputstream = new FileInputStream(afile[i]);
                    do {
                        int j = fileinputstream.read(abyte0, 0, abyte0.length);
                        if (j <= 0) {
                            break;
                        }
                        zipoutputstream.write(abyte0, 0, j);
                    } while (true);
                    fileinputstream.close();
                }
            }
            zipoutputstream.close();
            fileoutputstream.close();
            setStatus("Adding completed OK");
        } catch (Exception exception) {
            exception.printStackTrace();
            setStatus("Error: " + exception.getMessage());
        }
    }

    public void deleteFiles() {
        int i = JOptionPane.showConfirmDialog(this, "This delete operation is not reversable!\nDo you really want to continue?", "Warning", 0, 2, null);
        if (i == 1) {
            return;
        } else {
            Deleter deleter = new Deleter(currentArchive);
            deleter.start();
            return;
        }
    }

    public void extractFiles() {
        JFileChooser jfilechooser = new JFileChooser();
        jfilechooser.setCurrentDirectory(workingDir);
        jfilechooser.setDialogType(0);
        jfilechooser.setDialogTitle("Select destination directory for extracting " + currentArchive.getName());
        jfilechooser.setMultiSelectionEnabled(false);
        jfilechooser.setFileSelectionMode(1);
        overwriteCheckBox.setSelected(false);
        JButton jbutton = new JButton("Create a New Directory...");
        JPanel jpanel = new JPanel(new GridLayout(0, 2));
        JPanel jpanel1 = new JPanel(new GridLayout(2, 0));
        JPanel jpanel2 = new JPanel(new GridLayout(2, 0));
        jpanel.add(jpanel1);
        jpanel.add(jpanel2);
        jpanel1.setBorder(new TitledBorder("File Selection"));
        jpanel2.setBorder(new TitledBorder("Extracting options"));
        jpanel1.add(selFilesRadioButton);
        selFilesRadioButton.setSelected(true);
        jpanel1.add(allFilesRadioButton);
        ButtonGroup buttongroup = new ButtonGroup();
        buttongroup.add(selFilesRadioButton);
        buttongroup.add(allFilesRadioButton);
        jpanel2.add(overwriteCheckBox);
        jpanel2.add(keepDirCheckBox);
        keepDirCheckBox.setSelected(true);
        jfilechooser.add(jpanel);
        if (jfilechooser.showDialog(this, "Select") != 0) {
            return;
        }
        workingDir = jfilechooser.getCurrentDirectory();
        File file = jfilechooser.getCurrentDirectory();
        File file1 = jfilechooser.getSelectedFile();
        if (allFilesRadioButton.isSelected()) {
            table.getSelectionModel().setSelectionInterval(0, table.getRowCount() - 1);
        }
        Extractor extractor = new Extractor(file1);
        extractor.start();
    }

    public int getDefaultViewer() {
        return defaultViewerType;
    }

    public int[] getDisplayColumns() {
        int i = table.getColumnCount();
        int j = table.getColumnModel().getColumnCount();
        System.out.println("getDisplayColumns(): " + i + "temp:" + j);
        int ai[] = new int[i];
        for (int k = 0; k < i; k++) {
            ai[k] = table.convertColumnIndexToModel(k);
        }
        return ai;
    }

    public static File[] getSelectedFiles(JFileChooser jfilechooser) {
        Container container = (Container) jfilechooser.getComponent(3);
        JList jlist = null;
        Container container1;
        for (; container != null; container = container1) {
            container1 = (Container) container.getComponent(0);
            if (!(container1 instanceof JList)) {
                continue;
            }
            jlist = (JList) container1;
            break;
        }
        Object aobj[] = jlist.getSelectedValues();
        File afile[] = new File[aobj.length];
        for (int i = 0; i < aobj.length; i++) {
            if (aobj[i] instanceof File) {
                afile[i] = (File) aobj[i];
            }
        }
        return afile;
    }

    public int getSelectedRowCount() {
        return table.getSelectedRowCount();
    }

    public void getSummaryReport() {
        StringBuffer stringbuffer = new StringBuffer();
        stringbuffer.append("<html>");
        stringbuffer.append("<body>");
        stringbuffer.append("<B>Archive File: " + currentArchive.toString() + "</B>");
        stringbuffer.append("<HR><BR>");
        stringbuffer.append("<table BORDER=\"1\" CELLPADDING=\"3\" CELLSPACING=\"0\" WIDTH=\"100%\">");
        stringbuffer.append("<TR BGCOLOR=\"#CCCCFF\">");
        for (int i = 0; i < table.getColumnCount(); i++) {
            stringbuffer.append("<td>");
            stringbuffer.append("<B>" + table.getColumnName(i) + "</B>");
            stringbuffer.append("</td>");
        }
        stringbuffer.append("</TR>");
        for (int j = 0; j < table.getRowCount(); j++) {
            stringbuffer.append("<tr>");
            for (int k = 0; k < table.getColumnCount(); k++) {
                stringbuffer.append("<td>");
                stringbuffer.append(table.getValueAt(j, k));
                stringbuffer.append("</td>");
            }
            stringbuffer.append("</tr>");
        }
        stringbuffer.append("</body> </html>");
        new HtmlViewer(stringbuffer.toString());
    }

    public JTable getTable() {
        return table;
    }

    public ZipTableModel getTableModel() {
        return ziptm;
    }

    public void makeExeArchive(File file, File[] afile, File file1) {
        Adder adder = new Adder(file, afile, file1);
        adder.start();
        try {
            adder.join();
        } catch (InterruptedException _ex) {
        }
    }

    public void newArchive(File file) {
        table.clearSelection();
        openArchive(file);
        addFiles();
    }

    public void openArchive(File file) {
        table.clearSelection();
        currentArchive = file;
        ziptm.openArchive(currentArchive);
        long l = ziptm.getTotalSize();
        propLabel.setText("Total " + table.getRowCount() + " files, " + l / 1024L + "KB");
        if (l != 0L) {
            statusLabel.setText("Please select \"Actions\" menu or right click mouse to continue!");
            statusLabel.setIcon(JarIcon.getImageIcon("/images/play16.gif"));
        } else {
            statusLabel.setText("Error in opening " + file);
            statusLabel.setIcon(JarIcon.getImageIcon("/images/welcome16.gif"));
        }
    }

    public int print(Graphics g, PageFormat pageformat, int i) throws PrinterException {
        if (i >= maxNumPage) {
            return 1;
        }
        g.translate((int) pageformat.getImageableX(), (int) pageformat.getImageableY());
        int j = 0;
        int k = 0;
        if (pageformat.getOrientation() == 1) {
            j = (int) pageformat.getImageableWidth();
            k = (int) pageformat.getImageableHeight();
        } else {
            j = (int) pageformat.getImageableWidth();
            j += j / 2;
            k = (int) pageformat.getImageableHeight();
            g.setClip(0, 0, j, k);
        }
        int l = 0;
        g.setColor(Color.black);
        Font font = g.getFont();
        FontMetrics fontmetrics = g.getFontMetrics();
        l += fontmetrics.getAscent();
        g.drawString("ZipAnywhere", 0, l);
        l += 20;
        Font font1 = table.getFont().deriveFont(1);
        g.setFont(font1);
        fontmetrics = g.getFontMetrics();
        TableColumnModel tablecolumnmodel = table.getColumnModel();
        int i1 = tablecolumnmodel.getColumnCount();
        int ai[] = new int[i1];
        ai[0] = 0;
        int j1 = fontmetrics.getAscent();
        l += j1;
        for (int l1 = 0; l1 < i1; l1++) {
            TableColumn tablecolumn = tablecolumnmodel.getColumn(l1);
            int k2 = tablecolumn.getWidth();
            if (ai[l1] + k2 > j) {
                i1 = l1;
                break;
            }
            if (l1 + 1 < i1) {
                ai[l1 + 1] = ai[l1] + k2;
            }
            String s = (String) tablecolumn.getIdentifier();
            g.drawString(s, ai[l1], l);
        }
        g.setFont(table.getFont());
        fontmetrics = g.getFontMetrics();
        int j2 = l;
        j1 = fontmetrics.getHeight();
        int l2 = Math.max((int) ((double) j1 * 1.5D), 10);
        int i3 = (k - j2) / l2;
        maxNumPage = Math.max((int) Math.ceil((double) table.getRowCount() / (double) i3), 1);
        javax.swing.table.TableModel tablemodel = table.getModel();
        int j3 = i * i3;
        int k3 = Math.min(table.getRowCount(), j3 + i3);
        for (int k1 = j3; k1 < k3; k1++) {
            l += j1;
            for (int i2 = 0; i2 < i1; i2++) {
                int l3 = table.getColumnModel().getColumn(i2).getModelIndex();
                Object obj = ziptm.getValueAt(k1, l3);
                String s1 = obj.toString();
                if (s1.length() != 0) {
                    g.drawString(s1, ai[i2], l);
                }
            }
        }
        System.gc();
        return 0;
    }

    public void printZipTable() {
        try {
            PrinterJob printerjob = PrinterJob.getPrinterJob();
            printerjob.setPrintable(this);
            if (!printerjob.printDialog()) {
                return;
            }
            maxNumPage = 1;
            printerjob.print();
        } catch (PrinterException printerexception) {
            printerexception.printStackTrace();
            System.err.println("Printing error: " + printerexception.toString());
        }
    }

    public void setColumnVisible(int i, boolean flag) {
        ziptm.setColumnVisible(table, i, flag);
    }

    public void setDefaultViewer(int i) {
        defaultViewerType = i;
    }

    public void setDisplayColumns(int[] ai) {
        for (int i = 0; i < ZipTableModel.colInfos.length; i++) {
            ziptm.setColumnVisible(table, i, false);
        }
        for (int j = 0; j < ai.length; j++) {
            ziptm.setColumnVisible(table, ai[j], true);
        }
    }

    public void setStatus(String s) {
        statusLabel.setText(s);
    }

    public void setupPopupMenu() {
        popMenu.add(extractMenuItem);
        popMenu.add(addMenuItem);
        popMenu.add(delMenuItem);
        popMenu.add(viewMenuItem);
        popMenu.addSeparator();
        popMenu.add(selAllMenuItem);
        popMenu.add(deSelAllMenuItem);
        popMenu.addSeparator();
        popMenu.add(sortMenu);
        sortMenu.add(sortNameMenuItem);
        sortMenu.add(sortSizeMenuItem);
        sortMenu.add(sortTimeMenuItem);
        sortMenu.add(sortPathMenuItem);
        extractMenuItem.setActionCommand("extract");
        extractMenuItem.addActionListener(this);
        addMenuItem.setActionCommand("add");
        addMenuItem.addActionListener(this);
        delMenuItem.setActionCommand("del");
        delMenuItem.addActionListener(this);
        viewMenuItem.setActionCommand("view");
        viewMenuItem.addActionListener(this);
        selAllMenuItem.setActionCommand("selAll");
        selAllMenuItem.addActionListener(this);
        deSelAllMenuItem.setActionCommand("deSelAll");
        deSelAllMenuItem.addActionListener(this);
        sortNameMenuItem.setActionCommand("sortName");
        sortNameMenuItem.addActionListener(this);
        sortTimeMenuItem.setActionCommand("sortTime");
        sortTimeMenuItem.addActionListener(this);
        sortSizeMenuItem.setActionCommand("sortSize");
        sortSizeMenuItem.addActionListener(this);
        sortPathMenuItem.setActionCommand("sortPath");
        sortPathMenuItem.addActionListener(this);
        ButtonGroup buttongroup = new ButtonGroup();
        buttongroup.add(sortNameMenuItem);
        buttongroup.add(sortSizeMenuItem);
        buttongroup.add(sortTimeMenuItem);
        buttongroup.add(sortPathMenuItem);
    }

    public void setupRenderers() {
        TableColumnModel tablecolumnmodel = table.getColumnModel();
        int i = table.getColumnCount();
        ArrowButtonRenderer arrowbuttonrenderer = new ArrowButtonRenderer();
        for (int j = 0; j < i; j++) {
            table.getColumnModel().getColumn(j).setHeaderRenderer(arrowbuttonrenderer);
            tablecolumnmodel.getColumn(j).setPreferredWidth(ziptm.getPreferredColumnWidth(j));
        }
        table.getColumnModel().getColumn(3).setCellRenderer(new DateRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new RatioRenderer());
        table.getColumnModel().getColumn(9).setCellRenderer(new CrcRenderer());
    }

    public void updatePopupMenuState() {
        int i = table.getSelectedRowCount();
        if (i == 0) {
            delMenuItem.setEnabled(false);
            extractMenuItem.setEnabled(false);
            viewMenuItem.setEnabled(false);
            selAllMenuItem.setEnabled(true);
            deSelAllMenuItem.setEnabled(false);
        } else if (i == 1) {
            delMenuItem.setEnabled(true);
            extractMenuItem.setEnabled(true);
            viewMenuItem.setEnabled(true);
            selAllMenuItem.setEnabled(true);
            deSelAllMenuItem.setEnabled(true);
        } else {
            delMenuItem.setEnabled(true);
            extractMenuItem.setEnabled(true);
            viewMenuItem.setEnabled(false);
            selAllMenuItem.setEnabled(true);
            deSelAllMenuItem.setEnabled(true);
        }
    }

    public void viewFile() {
        try {
            int i = table.getSelectedRow();
            ZipFile zipfile = new ZipFile(currentArchive);
            Object obj = ziptm.getRealValueAt(i, 6);
            int j = ((Long) ziptm.getRealValueAt(i, 1)).intValue();
            String s = ziptm.getRealValueAt(i, 0).toString();
            String s1 = s;
            String s2 = "";
            String s4 = s1.toLowerCase();
            boolean flag = false;
            int k = defaultViewerType;
            for (int l = 0; l < supportedExt.length; l++) {
                if (!s4.endsWith(supportedExt[l])) {
                    continue;
                }
                flag = true;
                k = viewerType[l];
                break;
            }
            if (!flag) {
                int i1 = JOptionPane.showConfirmDialog(this, "Sorry, no viwer for this type of document!\nPlease extract this file and open it with other application.\nDo you want to use the default viewer?", "Warning", 0, 2, null);
                if (i1 == 1) {
                    return;
                }
            }
            if (obj != null) {
                String s3 = obj.toString();
                s1 = s3 + s;
            }
            ZipEntry zipentry = zipfile.getEntry(s1.replace(File.separatorChar, '/'));
            InputStream inputstream = zipfile.getInputStream(zipentry);
            byte abyte0[] = new byte[1024];
            File file = null;
            try {
                file = File.createTempFile("JZip", ".tmp");
                file.deleteOnExit();
                FileOutputStream fileoutputstream = new FileOutputStream(file);
                do {
                    int j1 = inputstream.read(abyte0, 0, abyte0.length);
                    if (j1 <= 0) {
                        break;
                    }
                    fileoutputstream.write(abyte0, 0, j1);
                } while (true);
                inputstream.close();
                fileoutputstream.close();
                zipfile.close();
            } catch (Exception _ex) {
                System.out.println("Error in creating tempary file. Check your disk space!");
                return;
            }
            switch(k) {
                case 0:
                    new TextViewer(file, true);
                    break;
                case 1:
                    new RTFViewer(file, true);
                    break;
                case 3:
                    new ImageViewer(file, true);
                    break;
                case 2:
                    new HtmlViewer(file, true);
                    break;
                case 4:
                    new HexViewer(file, true);
                    break;
            }
        } catch (Exception exception) {
            System.out.println(exception);
        }
    }

    public static final int BUFFER_SIZE = 1024;

    public static final int TEXT_VIEWER = 0;

    public static final int RTF_VIEWER = 1;

    public static final int HTML_VIEWER = 2;

    public static final int IMAGE_VIEWER = 3;

    public static final int HEX_VIEWER = 4;

    public static final String[] viewerNames = { "Plain Text Viewer (.txt)", "RTF Text Viewer (*.rtf)", "HTML Viewer (*.html, *.htm)", "Image Viewer(*.gif, *.jpg, *.jpeg)", "Heximal Viewer" };

    String[] supportedExt = { ".txt", ".rtf", ".jpeg", ".jpg", ".gif", ".htm", "html" };

    int[] viewerType = { 0, 1, 3, 3, 3, 2, 2 };

    int defaultViewerType;

    File currentArchive;

    File workingDir;

    Component root;

    JTable table;

    ZipTableModel ziptm;

    JTableHeader header;

    protected int maxNumPage;

    JScrollPane scrollPane;

    JPanel statusPanel;

    JLabel statusLabel;

    JLabel propLabel;

    JPopupMenu popMenu;

    JMenuItem addMenuItem;

    JMenuItem delMenuItem;

    JMenuItem extractMenuItem;

    JMenuItem viewMenuItem;

    JMenuItem selAllMenuItem;

    JMenuItem deSelAllMenuItem;

    JMenu sortMenu;

    JRadioButtonMenuItem sortNameMenuItem;

    JRadioButtonMenuItem sortTimeMenuItem;

    JRadioButtonMenuItem sortSizeMenuItem;

    JRadioButtonMenuItem sortPathMenuItem;

    JCheckBox overwriteCheckBox;

    JCheckBox keepDirCheckBox;

    JRadioButton selFilesRadioButton;

    JRadioButton allFilesRadioButton;

    JCheckBox retrieveSubCheckBox;

    JCheckBox saveDirCheckBox;

    static final Object[] methods = { "Normal", "Best Compression", "Fastest Speed", "No Compression" };

    JComboBox compressMethodComboBox;

    class Extractor extends Thread {

        public void run() {
            root.setCursor(Cursor.getPredefinedCursor(3));
            boolean flag = overwriteCheckBox.isSelected();
            try {
                System.out.println("currentArchive is " + currentArchive);
                ZipFile zipfile = new ZipFile(currentArchive);
                for (int i = 0; i < selRows.length; i++) {
                    Object obj = ziptm.getRealValueAt(selRows[i], 6);
                    String s = ziptm.getRealValueAt(selRows[i], 0).toString();
                    String s1 = s;
                    String s2 = "";
                    if (obj != null) {
                        s2 = obj.toString();
                        s1 = s2 + s;
                    }
                    pm.setProgress(i);
                    pm.setNote(s1);
                    if (pm.isCanceled()) {
                        return;
                    }
                    ZipEntry zipentry = zipfile.getEntry(s1.replace(File.separatorChar, '/'));
                    InputStream inputstream = zipfile.getInputStream(zipentry);
                    s2 = s2.replace('/', File.separatorChar);
                    if (keepDirCheckBox.isSelected()) {
                        s2 = outputDir.getCanonicalPath() + File.separator + s2;
                    } else {
                        s2 = outputDir.getCanonicalPath() + File.separator;
                    }
                    File file = new File(s2, s);
                    if (!flag && file.exists()) {
                        Object aobj[] = { "Yes", "Yes To All", "No" };
                        Date date = new Date(file.lastModified());
                        Date date1 = ((SimpleDate) ziptm.getValueAt(selRows[i], 3)).data;
                        Long long1 = (Long) ziptm.getValueAt(selRows[i], 1);
                        String s3 = "ZipAnywhere detects a file with the same name on disk. \nFile name: " + file.getName() + "\nExisting file: " + formatter.format(date) + ",  " + file.length() + "Bytes" + "\nFile in archive: " + formatter.format(date1) + ",  " + long1 + "Bytes" + "\nWould you like to overwrite?";
                        int k = JOptionPane.showOptionDialog(ZipTablePanel.this, s3, "Warning", -1, 2, null, aobj, aobj[0]);
                        if (k == 2) {
                            continue;
                        }
                        if (k == 1) {
                            flag = true;
                        }
                    }
                    File file1 = new File(file.getParent());
                    if (file1 != null && !file1.exists()) {
                        file1.mkdirs();
                    }
                    FileOutputStream fileoutputstream = new FileOutputStream(file);
                    do {
                        int j = inputstream.read(buf, 0, buf.length);
                        if (j <= 0) {
                            break;
                        }
                        fileoutputstream.write(buf, 0, j);
                    } while (true);
                    fileoutputstream.close();
                    file.setLastModified(((SimpleDate) ziptm.getValueAt(selRows[i], 3)).data.getTime());
                    Thread.sleep(5L);
                }
                pm.close();
                zipfile.close();
                getToolkit().beep();
                setStatus("Totally " + selRows.length + " file(s) extracted from this archive to " + outputDir.getPath());
            } catch (Exception exception1) {
                System.out.println(exception1);
            } finally {
                root.setCursor(Cursor.getPredefinedCursor(0));
            }
        }

        File outputDir;

        byte[] buf;

        SimpleDateFormat formatter;

        int[] selRows;

        final ProgressMonitor pm;

        public Extractor(File file) {
            buf = new byte[1024];
            formatter = new SimpleDateFormat("MM/dd/yyyy hh:mma", Locale.getDefault());
            selRows = table.getSelectedRows();
            pm = new ProgressMonitor(getParent(), "Extracting files...", "starting", 0, selRows.length - 1);
            pm.setMillisToDecideToPopup(0);
            pm.setMillisToPopup(0);
            outputDir = file;
        }
    }

    public int[] getSelectedRows() {
        return (table.getSelectedRows());
    }

    public String[] getSelectedFiles() {
        int[] selectedRows = table.getSelectedRows();
        String[] sRetVal = new String[selectedRows.length];
        for (int i = 0; i < selectedRows.length; i++) {
            sRetVal[i] = (String) ziptm.getRealValueAt(i, ZipTableModel.NAME);
        }
        return (sRetVal);
    }
}
