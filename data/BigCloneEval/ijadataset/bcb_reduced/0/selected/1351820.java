package net.sourceforge.swinguiloc.util;

import java.awt.*;
import java.io.*;
import net.sourceforge.swinguiloc.beans.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.zip.*;
import java.util.*;
import net.sourceforge.swinguiloc.trans.*;
import java.text.*;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import net.sourceforge.swinguiloc.beans.LMenuItem;

public class ResourceBrowser extends LDialog {

    private static final long serialVersionUID = 0L;

    private JPanel jContentPane = null;

    private ZipFile resFile = null;

    private File resFileOnDisk = null;

    private JScrollPane resScroller = null;

    private LButton cmdAdd = null;

    private LButton cmdDel = null;

    private LButton cmdEdit = null;

    private LButton cmdClose = null;

    private JPanel buttonPanel = null;

    private JTable resTable = null;

    private JPopupMenu resPopup = null;

    private LMenuItem resAdd = null;

    private LMenuItem resDel = null;

    private LMenuItem resEdit = null;

    private LMenuItem resExtract = null;

    private String lastOpenLocation = null;

    private LString errorCaption = new LString("Error");

    private LString zipErrorMsg = new LString("Error with zip file ...");

    private LString zipMustHaveOneEntry = new LString("Zip file must ...");

    private LString duplicateZipEntry = new LString("Duplicate zip entry ...");

    private LString msgboxCaption = new LString("Message");

    private LString extractedBytes = new LString("Successfuly extracted file ...");

    private LString inputCaption = new LString("* Input *");

    private LString msgNewComment = new LString("* Enter comment: *");

    private LString labelName = new LString("* Name *", "labelName");

    private LString labelSize = new LString("* Size *", "labelSize");

    private LString labelCompressed = new LString("* Compressed *", "labelCompressed");

    private LString labelComment = new LString("* Comment *", "labelComment");

    private LString labelTime = new LString("* Time *", "labelTime");

    public ResourceBrowser() throws HeadlessException {
        super();
        initialize();
    }

    public ResourceBrowser(Frame source, File resFile) throws HeadlessException {
        super(source);
        resFileOnDisk = resFile;
        if (source instanceof LanguageSwitch) {
            setTranslator(((LanguageSwitch) source).getTranslator());
        }
        initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setCaptionTag("ResBrowser");
        errorCaption.setCaptionTag("msgbox.ErrorCaption");
        zipErrorMsg.setCaptionTag("msgbox.zipError");
        zipMustHaveOneEntry.setCaptionTag("msgbox.zipMustHaveOneEntry");
        duplicateZipEntry.setCaptionTag("msgbox.duplicateZipEntry");
        msgboxCaption.setCaptionTag("msgbox.genericCaption");
        extractedBytes.setCaptionTag("msgbox.extractedBytes");
        inputCaption.setCaptionTag("msgbox.inputCaption");
        msgNewComment.setCaptionTag("msgbox.newComment");
        addTranslatable(errorCaption);
        addTranslatable(zipErrorMsg);
        addTranslatable(zipMustHaveOneEntry);
        addTranslatable(duplicateZipEntry);
        addTranslatable(msgboxCaption);
        addTranslatable(extractedBytes);
        addTranslatable(inputCaption);
        addTranslatable(msgNewComment);
        addTranslatable(labelName);
        addTranslatable(labelSize);
        addTranslatable(labelTime);
        addTranslatable(labelCompressed);
        addTranslatable(labelComment);
        this.setSize(new java.awt.Dimension(796, 350));
        this.setTitle("ResBrowser");
        this.setContentPane(getJContentPane());
        this.resPopup = getResPopup();
        this.addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent e) {
                onClose();
            }
        });
        setLocation(Math.abs((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - getBounds().width / 2), Math.abs((int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - getBounds().height / 2));
    }

    /**
     * This method initializes jContentPane	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(null);
            jContentPane.add(getResScroller(), null);
            jContentPane.add(getButtonPanel(), null);
            jContentPane.addComponentListener(new java.awt.event.ComponentAdapter() {

                public void componentResized(java.awt.event.ComponentEvent e) {
                    int tmpHeight = e.getComponent().getHeight();
                    int tmpWidth = e.getComponent().getWidth();
                    resScroller.setSize(tmpWidth - 10, tmpHeight - 50);
                    refreshTableContents();
                    buttonPanel.setLocation((tmpWidth - buttonPanel.getWidth()) / 2, tmpHeight - buttonPanel.getHeight());
                }
            });
        }
        return jContentPane;
    }

    /**
     * This method initializes resScroller	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getResScroller() {
        if (resScroller == null) {
            resScroller = new JScrollPane(this.getResTable(resFileOnDisk));
            resScroller.setLocation(new java.awt.Point(5, 5));
            resScroller.setSize(new java.awt.Dimension(590, 300));
        }
        return resScroller;
    }

    /**
     * This method initializes cmdAdd	
     * 	
     * @return net.sourceforge.swinguiloc.beans.LButton	
     */
    private LButton getCmdAdd() {
        if (cmdAdd == null) {
            cmdAdd = new LButton();
            cmdAdd.setText("Add");
            cmdAdd.setBounds(new java.awt.Rectangle(8, 1, 100, 25));
            cmdAdd.setCaptionTag("cmdAdd");
            cmdAdd.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onAdd();
                }
            });
        }
        return cmdAdd;
    }

    /**
     * This method initializes cmdDel	
     * 	
     * @return net.sourceforge.swinguiloc.beans.LButton	
     */
    private LButton getCmdDel() {
        if (cmdDel == null) {
            cmdDel = new LButton();
            cmdDel.setText("Remove");
            cmdDel.setActionCommand("Remove");
            cmdDel.setBounds(new java.awt.Rectangle(116, 1, 100, 25));
            cmdDel.setCaptionTag("cmdDel");
            cmdDel.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onDelete();
                }
            });
        }
        return cmdDel;
    }

    /**
     * This method initializes cmdEdit	
     * 	
     * @return net.sourceforge.swinguiloc.beans.LButton	
     */
    private LButton getCmdEdit() {
        if (cmdEdit == null) {
            cmdEdit = new LButton();
            cmdEdit.setCaptionTag("cmdPreview");
            cmdEdit.setBounds(new java.awt.Rectangle(224, 1, 100, 25));
            cmdEdit.setText("Edit");
            cmdEdit.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onPreview();
                }
            });
        }
        return cmdEdit;
    }

    /**
     * This method initializes cmdClose	
     * 	
     * @return net.sourceforge.swinguiloc.beans.LButton	
     */
    private LButton getCmdClose() {
        if (cmdClose == null) {
            cmdClose = new LButton();
            cmdClose.setText("Close");
            cmdClose.setBounds(new java.awt.Rectangle(332, 1, 100, 25));
            cmdClose.setCaptionTag("cmdClose");
            cmdClose.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onClose();
                }
            });
        }
        return cmdClose;
    }

    /**
     * This method initializes ResTable
     * @param f Zip file to display
     * @return Resource table
     */
    private JTable getResTable(File f) {
        if (resTable != null) return resTable;
        if (f != null) {
            try {
                resFile = new ZipFile(f);
            } catch (ZipException ze) {
                resFile = null;
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            JTable t = new JTable(getDataModel());
            t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            TableColumn column;
            for (int i = 0; i < 5; i++) {
                column = t.getColumnModel().getColumn(i);
                if (i == 0) {
                    column.setPreferredWidth(100);
                    column.setCellRenderer(new ZipFileNameRenderer(this));
                } else if ((i == 1) || (i == 2)) {
                    column.setPreferredWidth(25);
                } else column.setPreferredWidth(50);
            }
            resTable = t;
            resTable.setShowHorizontalLines(false);
            resTable.addMouseListener(new java.awt.event.MouseAdapter() {

                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getButton() != java.awt.event.MouseEvent.BUTTON1) {
                        Point p = new Point(e.getX(), e.getY());
                        int row = resTable.rowAtPoint(p);
                        int col = resTable.columnAtPoint(p);
                        if ((row != -1) && (col != -1)) {
                            resTable.changeSelection(row, col, false, false);
                            resPopup.show(resTable, e.getX(), e.getY());
                        }
                    }
                }
            });
            return t;
        }
        return null;
    }

    /**
     * Creates the resource table's data model.
     * @return Resource table's data model
     */
    private TableModel getDataModel() {
        TableModel dm = new AbstractTableModel() {

            private static final long serialVersionUID = 0L;

            public int getColumnCount() {
                return 5;
            }

            public String getColumnName(int col) {
                Object[] colNames = { labelName, labelSize, labelCompressed, labelComment, labelTime };
                if (col < colNames.length) return colNames[col].toString(); else return "* Error *";
            }

            public int getRowCount() {
                if (resFile != null) {
                    return resFile.size();
                } else return 0;
            }

            public Object getValueAt(int row, int col) {
                Vector<ZipEntry> entries = new Vector<ZipEntry>();
                Enumeration e = resFile.entries();
                while (e.hasMoreElements()) {
                    entries.add((ZipEntry) e.nextElement());
                }
                if (col == 0) {
                    return entries.get(row);
                } else if (col == 3) {
                    return entries.get(row).getComment();
                } else if (col == 1) {
                    NumberFormat nf = NumberFormat.getNumberInstance(getLocale());
                    return nf.format(entries.get(row).getSize()) + " B";
                } else if (col == 2) {
                    NumberFormat nf = NumberFormat.getNumberInstance(getLocale());
                    return nf.format(entries.get(row).getCompressedSize()) + " B";
                } else if (col == 4) {
                    DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, getLocale());
                    return df.format(entries.get(row).getTime());
                } else return null;
            }

            @Override
            public Class<?> getColumnClass(int col) {
                if ((col == 1) || (col == 2)) return Long.class; else return String.class;
            }
        };
        return dm;
    }

    private void onClose() {
        this.dispose();
    }

    private void onDelete() {
        int selRow = resTable.getSelectedRow();
        if ((selRow == -1) || (selRow >= resTable.getRowCount())) return;
        if (resFile.size() < 2) {
            JOptionPane.showMessageDialog(this, zipMustHaveOneEntry.toString());
            return;
        }
        if (delFile(resTable.getValueAt(selRow, 0).toString())) {
            refreshTableContents();
        }
    }

    private synchronized boolean delFile(String entryName) {
        if (entryName.trim().length() == 0) return false;
        int buffer = 2048;
        byte[] data = new byte[buffer];
        FileInputStream fis;
        FileOutputStream fos;
        BufferedInputStream source;
        int count;
        try {
            File tempResFile = new File(resFileOnDisk.getCanonicalPath() + ".bak");
            if (!tempResFile.exists()) tempResFile.createNewFile();
            fis = new FileInputStream(resFileOnDisk);
            fos = new FileOutputStream(tempResFile);
            source = new BufferedInputStream(fis, buffer);
            while ((count = source.read(data, 0, buffer)) != -1) {
                fos.write(data, 0, count);
            }
            fos.close();
            source.close();
            fis.close();
            ZipFile oldZip = new ZipFile(tempResFile);
            resFile = null;
            fos = new FileOutputStream(resFileOnDisk);
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos));
            Enumeration e = oldZip.entries();
            while (e.hasMoreElements()) {
                ZipEntry tmpEntry = (ZipEntry) e.nextElement();
                if (!tmpEntry.getName().equals(entryName)) {
                    ZipEntry newEntry = new ZipEntry(tmpEntry.getName());
                    newEntry.setComment(tmpEntry.getComment());
                    zos.putNextEntry(newEntry);
                    source = new BufferedInputStream(oldZip.getInputStream(tmpEntry), buffer);
                    while ((count = source.read(data, 0, buffer)) != -1) {
                        zos.write(data, 0, count);
                    }
                    source.close();
                }
            }
            zos.close();
            fos.close();
            resFile = new ZipFile(resFileOnDisk);
            oldZip = null;
            tempResFile.deleteOnExit();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return true;
    }

    /**
     *  This method is called when the Add button is clicked
     */
    private void onAdd() {
        String target;
        if (lastOpenLocation == null) target = "."; else target = lastOpenLocation;
        JFileChooser jfc = new JFileChooser(target);
        int status = jfc.showOpenDialog(this);
        if (status == JFileChooser.APPROVE_OPTION) {
            File f = jfc.getSelectedFile();
            lastOpenLocation = f.getAbsolutePath();
            Enumeration e = resFile.entries();
            while (e.hasMoreElements()) {
                if (((ZipEntry) e.nextElement()).getName().equals(f.getName())) {
                    JOptionPane.showMessageDialog(this, duplicateZipEntry.toString());
                    return;
                }
            }
            String comment = (String) JOptionPane.showInputDialog(this, msgNewComment, inputCaption.toString(), JOptionPane.QUESTION_MESSAGE, null, null, "");
            if (comment == null) return;
            addFile(f, comment);
            refreshTableContents();
        }
    }

    /**
     *  Opens a file for preview.
     */
    private void onPreview() {
        int selRow = resTable.getSelectedRow();
        if ((selRow == -1) || (selRow >= resTable.getRowCount())) return;
        String selEntryName = resTable.getValueAt(selRow, 0).toString();
        ResourcePreview rp = new ResourcePreview((Frame) this.getParent(), resFile, selEntryName);
        rp.fireLanguageSwitched();
        rp.setVisible(true);
    }

    /**
     * Adds a file to the currently opened resource file.
     * @param f File to add
     */
    private synchronized void addFile(File f, String comment) {
        int buffer = 2048;
        byte[] data = new byte[buffer];
        FileInputStream fis;
        FileOutputStream fos;
        BufferedInputStream source;
        int count;
        try {
            File tempResFile = new File(resFileOnDisk.getCanonicalPath() + ".bak");
            if (!tempResFile.exists()) tempResFile.createNewFile();
            fis = new FileInputStream(resFileOnDisk);
            fos = new FileOutputStream(tempResFile);
            source = new BufferedInputStream(fis, buffer);
            while ((count = source.read(data, 0, buffer)) != -1) {
                fos.write(data, 0, count);
            }
            fos.close();
            source.close();
            fis.close();
            ZipFile oldZip = new ZipFile(tempResFile);
            resFile = null;
            fos = new FileOutputStream(resFileOnDisk);
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos));
            Enumeration e = oldZip.entries();
            while (e.hasMoreElements()) {
                ZipEntry tmpEntry = (ZipEntry) e.nextElement();
                ZipEntry newEntry = new ZipEntry(tmpEntry.getName());
                newEntry.setComment(tmpEntry.getComment());
                zos.putNextEntry(newEntry);
                source = new BufferedInputStream(oldZip.getInputStream(tmpEntry), buffer);
                while ((count = source.read(data, 0, buffer)) != -1) {
                    zos.write(data, 0, count);
                }
                source.close();
            }
            fis = new FileInputStream(f);
            source = new BufferedInputStream(fis, buffer);
            ZipEntry newEntry = new ZipEntry(f.getName());
            newEntry.setComment(comment);
            zos.putNextEntry(newEntry);
            while ((count = source.read(data, 0, buffer)) != -1) {
                zos.write(data, 0, count);
            }
            source.close();
            fis.close();
            zos.close();
            fos.close();
            resFile = new ZipFile(resFileOnDisk);
            oldZip = null;
            tempResFile.deleteOnExit();
        } catch (IOException ze) {
            Object[] options = { "OK" };
            JOptionPane.showOptionDialog(this, zipErrorMsg + "\n" + ze.getLocalizedMessage(), errorCaption.toString(), JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
        }
    }

    /**
     * This method initializes buttonPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel();
            buttonPanel.setLayout(null);
            buttonPanel.setBounds(new java.awt.Rectangle(158, 291, 441, 28));
            buttonPanel.add(getCmdAdd(), null);
            buttonPanel.add(getCmdDel(), null);
            buttonPanel.add(getCmdEdit(), null);
            buttonPanel.add(getCmdClose(), null);
        }
        return buttonPanel;
    }

    /**
     * This method initializes resPopup	
     * 	
     * @return javax.swing.JPopupMenu	
     */
    private JPopupMenu getResPopup() {
        if (resPopup == null) {
            resPopup = new JPopupMenu();
            resPopup.add(getResAdd());
            resPopup.add(getResDel());
            resPopup.add(getResEdit());
            resPopup.add(getResExtract());
        }
        return resPopup;
    }

    /**
     * This method initializes resAdd	
     * 	
     * @return net.sourceforge.swinguiloc.beans.LMenuItem	
     */
    private LMenuItem getResAdd() {
        if (resAdd == null) {
            resAdd = new LMenuItem();
            resAdd.setText("Add");
            resAdd.setCaptionTag("cmdAdd");
            resAdd.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onAdd();
                }
            });
            addTranslatable(resAdd);
        }
        return resAdd;
    }

    /**
     * This method initializes popDel	
     * 	
     * @return net.sourceforge.swinguiloc.beans.LMenuItem	
     */
    private LMenuItem getResDel() {
        if (resDel == null) {
            resDel = new LMenuItem();
            resDel.setText("Del");
            resDel.setCaptionTag("cmdDel");
            resDel.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onDelete();
                }
            });
            addTranslatable(resDel);
        }
        return resDel;
    }

    /**
     * This method initializes popEdit	
     * 	
     * @return net.sourceforge.swinguiloc.beans.LMenuItem	
     */
    private LMenuItem getResEdit() {
        if (resEdit == null) {
            resEdit = new LMenuItem();
            resEdit.setText("Modify");
            resEdit.setCaptionTag("cmdPreview");
            resEdit.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onPreview();
                }
            });
            addTranslatable(resEdit);
        }
        return resEdit;
    }

    /**
     * This method initializes resExtract	
     * 	
     * @return net.sourceforge.swinguiloc.beans.LMenuItem	
     */
    private LMenuItem getResExtract() {
        if (resExtract == null) {
            resExtract = new LMenuItem();
            resExtract.setText("Extract");
            resExtract.setEnabled(true);
            resExtract.setCaptionTag("cmdExtract");
            resExtract.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onExtract();
                }
            });
            addTranslatable(resExtract);
        }
        return resExtract;
    }

    /**
     *  Handles the user's wish to extract a file.
     */
    private void onExtract() {
        int selRow = resTable.getSelectedRow();
        if ((selRow == -1) || (selRow >= resTable.getRowCount())) return;
        JFileChooser jfc = new JFileChooser(".");
        jfc.setSelectedFile(new File(resTable.getValueAt(selRow, 0).toString()));
        int status = jfc.showSaveDialog(this);
        if (status == JFileChooser.APPROVE_OPTION) {
            File f = jfc.getSelectedFile();
            int bytes = extractFile(resTable.getValueAt(selRow, 0).toString(), f);
            JOptionPane.showMessageDialog(this, extractedBytes.toString() + " " + bytes, msgboxCaption.toString(), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Extracts a resource from the resource file to the given
     * destination.
     * @param entryname Name of the entry in the resource file
     * @param dest Destination file
     * @return Number of bytes written
     */
    private synchronized int extractFile(String entryname, File dest) {
        int buffer = 2048;
        byte[] data = new byte[buffer];
        int count;
        int totalBytesRead = 0;
        try {
            FileOutputStream fos = new FileOutputStream(dest);
            BufferedOutputStream destination = new BufferedOutputStream(fos);
            BufferedInputStream source = new BufferedInputStream(resFile.getInputStream(new ZipEntry(entryname)));
            while ((count = source.read(data, 0, buffer)) != -1) {
                destination.write(data, 0, count);
                totalBytesRead += count;
            }
            destination.close();
            source.close();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return totalBytesRead;
    }

    /**
     * Provides access to the loaded ZIP file.
     * @return
     */
    protected ZipFile getResFile() {
        return resFile;
    }

    /**
     * Refreshes the contents of the resource table.
     */
    private void refreshTableContents() {
        resTable.createDefaultColumnsFromModel();
        resTable.getColumnModel().getColumn(0).setCellRenderer(new ZipFileNameRenderer(this));
        resTable.revalidate();
        resScroller.revalidate();
        resTable.repaint();
        resScroller.repaint();
    }
}
