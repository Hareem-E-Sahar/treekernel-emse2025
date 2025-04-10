package net.sf.jftp.gui.base;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.sf.jftp.JFtp;
import net.sf.jftp.config.SaveSet;
import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.base.dir.DirCanvas;
import net.sf.jftp.gui.base.dir.DirCellRenderer;
import net.sf.jftp.gui.base.dir.DirEntry;
import net.sf.jftp.gui.base.dir.DirLister;
import net.sf.jftp.gui.base.dir.DirPanel;
import net.sf.jftp.gui.framework.HImageButton;
import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.gui.tasks.Creator;
import net.sf.jftp.gui.tasks.ImageViewer;
import net.sf.jftp.gui.tasks.NameChooser;
import net.sf.jftp.gui.tasks.RemoteCommand;
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.FilesystemConnection;
import net.sf.jftp.net.FtpConnection;
import net.sf.jftp.net.SmbConnection;
import net.sf.jftp.system.LocalIO;
import net.sf.jftp.system.StringUtils;
import net.sf.jftp.system.UpdateDaemon;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.util.ZipFileCreator;

public class LocalDir extends DirPanel implements ListSelectionListener, ActionListener, ConnectionListener, KeyListener {

    static final String deleteString = "rm";

    static final String mkdirString = "mkdir";

    static final String refreshString = "fresh";

    static final String cdString = "cd";

    static final String cmdString = "cmd";

    static final String downloadString = "<-";

    static final String uploadString = "->";

    static final String zipString = "zip";

    static final String cpString = "cp";

    static final String rnString = "rn";

    static final String cdUpString = "cdUp";

    HImageButton deleteButton;

    HImageButton mkdirButton;

    HImageButton cmdButton;

    HImageButton refreshButton;

    HImageButton cdButton;

    HImageButton uploadButton;

    HImageButton zipButton;

    HImageButton cpButton;

    HImageButton rnButton;

    private DirCanvas label = new DirCanvas(this);

    private boolean pathChanged = true;

    private boolean firstGui = true;

    private int pos = 0;

    private JPanel p = new JPanel();

    private JToolBar buttonPanel = new JToolBar() {

        public Insets getInsets() {
            return new Insets(0, 0, 0, 0);
        }
    };

    private JToolBar currDirPanel = new JToolBar() {

        public Insets getInsets() {
            return new Insets(0, 0, 0, 0);
        }
    };

    private DefaultListModel jlm;

    private JScrollPane jsp = new JScrollPane(jl);

    private int tmpindex = -1;

    private Hashtable dummy = new Hashtable();

    private JPopupMenu popupMenu = new JPopupMenu();

    private JMenuItem runFile = new JMenuItem("Launch file");

    private JMenuItem viewFile = new JMenuItem("View file");

    private JMenuItem props = new JMenuItem("Properties");

    private DirEntry currentPopup = null;

    private String sortMode = null;

    String[] sortTypes = new String[] { "Normal", "Reverse", "Size", "Size/Re" };

    private JComboBox sorter = new JComboBox(sortTypes);

    HImageButton cdUpButton;

    private boolean dateEnabled = false;

    /**
    * LocalDir constructor.
    */
    public LocalDir() {
        type = "local";
        con = new FilesystemConnection();
        con.addConnectionListener(this);
    }

    /**
    * LocalDir constructor.
    */
    public LocalDir(String path) {
        type = "local";
        this.path = path;
        con = new FilesystemConnection();
        con.addConnectionListener(this);
        con.chdir(path);
    }

    /**
    * Creates the gui and adds the MouseListener etc.
    */
    public void gui_init() {
        setLayout(new BorderLayout());
        currDirPanel.setFloatable(false);
        buttonPanel.setFloatable(false);
        FlowLayout f = new FlowLayout(FlowLayout.RIGHT);
        f.setHgap(1);
        f.setVgap(2);
        buttonPanel.setLayout(f);
        buttonPanel.setMargin(new Insets(0, 0, 0, 0));
        runFile.addActionListener(this);
        viewFile.addActionListener(this);
        props.addActionListener(this);
        popupMenu.add(runFile);
        popupMenu.add(viewFile);
        popupMenu.add(props);
        deleteButton = new HImageButton(Settings.deleteImage, deleteString, "Delete selected", this);
        deleteButton.setToolTipText("Delete selected");
        mkdirButton = new HImageButton(Settings.mkdirImage, mkdirString, "Create a new directory", this);
        mkdirButton.setToolTipText("Create directory");
        refreshButton = new HImageButton(Settings.refreshImage, refreshString, "Refresh current directory", this);
        refreshButton.setToolTipText("Refresh directory");
        cdButton = new HImageButton(Settings.cdImage, cdString, "Change directory", this);
        cdButton.setToolTipText("Change directory");
        uploadButton = new HImageButton(Settings.uploadImage, uploadString, "Upload selected", this);
        uploadButton.setToolTipText("Upload selected");
        zipButton = new HImageButton(Settings.zipFileImage, zipString, "Add selected to new zip file", this);
        zipButton.setToolTipText("Create zip");
        cpButton = new HImageButton(Settings.copyImage, cpString, "Copy selected files to another local dir", this);
        cpButton.setToolTipText("Local copy selected");
        rnButton = new HImageButton(Settings.textFileImage, rnString, "Rename selected file or directory", this);
        rnButton.setToolTipText("Rename selected");
        cdUpButton = new HImageButton(Settings.cdUpImage, cdUpString, "Go to Parent Directory", this);
        cdUpButton.setToolTipText("Go to Parent Directory");
        label.setText("Filesystem: " + StringUtils.cutPath(path));
        label.setSize(getSize().width - 10, 24);
        currDirPanel.add(label);
        currDirPanel.setSize(getSize().width - 10, 32);
        label.setSize(getSize().width - 20, 24);
        p.setLayout(new BorderLayout());
        p.add("North", currDirPanel);
        buttonPanel.add(sorter);
        buttonPanel.add(new JLabel("  "));
        buttonPanel.add(refreshButton);
        buttonPanel.add(new JLabel("  "));
        buttonPanel.add(cpButton);
        buttonPanel.add(rnButton);
        buttonPanel.add(mkdirButton);
        buttonPanel.add(cdButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(cdUpButton);
        buttonPanel.add(new JLabel("  "));
        buttonPanel.add(zipButton);
        buttonPanel.add(new JLabel("   "));
        buttonPanel.add(uploadButton);
        buttonPanel.setVisible(true);
        buttonPanel.setSize(getSize().width - 10, 32);
        p.add("East", buttonPanel);
        sorter.addActionListener(this);
        add("North", p);
        setDirList(true);
        jlm = new DefaultListModel();
        jl = new JList(jlm);
        jl.setCellRenderer(new DirCellRenderer());
        jl.setVisibleRowCount(Settings.visibleFileRows);
        jl.addListSelectionListener(this);
        jl.addKeyListener(this);
        jl.requestFocus();
        MouseListener mouseListener = new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                if (JFtp.uiBlocked) {
                    return;
                }
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int index = jl.getSelectedIndex() - 1;
                    if (index < -1) {
                        return;
                    }
                    String tgt = (String) jl.getSelectedValue().toString();
                    if (index < 0) {
                    } else if ((dirEntry == null) || (dirEntry.length < index) || (dirEntry[index] == null)) {
                        return;
                    } else {
                        currentPopup = dirEntry[index];
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }

            public void mouseClicked(MouseEvent e) {
                if (JFtp.uiBlocked) {
                    return;
                }
                if (e.getClickCount() == 2) {
                    int index = jl.getSelectedIndex() - 1;
                    if (index < -1) {
                        return;
                    }
                    String tgt = (String) jl.getSelectedValue().toString();
                    if (index < 0) {
                        chdir(path + tgt);
                    } else if ((dirEntry == null) || (dirEntry.length < index) || (dirEntry[index] == null)) {
                        return;
                    } else if (dirEntry[index].isDirectory()) {
                        if (JFtp.mainFrame != null) {
                            JFtp.mainFrame.setCursor(Cursor.WAIT_CURSOR);
                        }
                        chdir(path + tgt);
                        if (JFtp.mainFrame != null) {
                            JFtp.mainFrame.setCursor(Cursor.DEFAULT_CURSOR);
                        }
                    } else {
                        showContentWindow(path + dirEntry[index].toString(), dirEntry[index]);
                    }
                }
            }
        };
        jl.addMouseListener(mouseListener);
        jsp = new JScrollPane(jl);
        jsp.setSize(getSize().width - 20, getSize().height - 72);
        add("Center", jsp);
        jsp.setVisible(true);
        setVisible(true);
    }

    public void setViewPort() {
    }

    /**
    * Part of a gui refresh.
    * There's no need to call this by hand.
    */
    public void gui(boolean fakeInit) {
        if (firstGui) {
            gui_init();
            firstGui = false;
        }
        if (con instanceof FilesystemConnection) {
            label.setText("Filesystem: " + StringUtils.cutPath(path));
        } else {
            label.setText("Remote: " + StringUtils.cutPath(path));
        }
        if (!fakeInit) {
            setDirList(false);
        }
        invalidate();
        validate();
    }

    /**
    * List directory and create/update the whole file list.
    * There's no need to call this by hand.
    */
    public void setDirList(boolean fakeInit) {
        jlm = new DefaultListModel();
        DirEntry dwn = new DirEntry("..", this);
        dwn.setDirectory();
        jlm.addElement(dwn);
        if (!fakeInit) {
            if (pathChanged) {
                pathChanged = false;
                DirLister dir = new DirLister(con, sortMode);
                while (!dir.finished) {
                    LocalIO.pause(10);
                }
                if (dir.isOk()) {
                    length = dir.getLength();
                    dirEntry = new DirEntry[length];
                    files = dir.list();
                    String[] fSize = dir.sList();
                    int[] perms = dir.getPermissions();
                    for (int i = 0; i < length; i++) {
                        if ((files == null) || (files[i] == null)) {
                            System.out.println("skipping setDirList, files or files[i] is null!");
                            return;
                        }
                        dirEntry[i] = new DirEntry(files[i], this);
                        if (dirEntry[i] == null) {
                            System.out.println("\nskipping setDirList, dirEntry[i] is null!");
                            return;
                        }
                        if (dirEntry[i].file == null) {
                            System.out.println("\nskipping setDirList, dirEntry[i].file is null!");
                            return;
                        }
                        if (perms != null) {
                            dirEntry[i].setPermission(perms[i]);
                        }
                        if (fSize[i].startsWith("@")) {
                            fSize[i] = fSize[i].substring(1);
                        }
                        dirEntry[i].setFileSize(Long.parseLong(fSize[i]));
                        if (dirEntry[i].file.endsWith("/")) {
                            dirEntry[i].setDirectory();
                        } else {
                            dirEntry[i].setFile();
                        }
                        Object[] d = dir.getDates();
                        if (d != null) {
                            dirEntry[i].setDate((Date) d[i]);
                        }
                        jlm.addElement(dirEntry[i]);
                    }
                } else {
                    Log.debug("Not a directory: " + path);
                }
            }
        }
        jl.setModel(jlm);
    }

    /**
    * Change the directory of this connection and update the gui
    */
    public boolean chdir(String p) {
        if ((JFtp.remoteDir == null) || (p == null) || p.trim().equals("")) {
            return false;
        }
        BasicConnection c = JFtp.remoteDir.getCon();
        if ((c != null) && (c instanceof FtpConnection)) {
            FtpConnection con = (FtpConnection) c;
            SaveSet s = new SaveSet(Settings.login_def, con.getHost(), con.getUsername(), con.getPassword(), Integer.toString(con.getPort()), con.getCachedPWD(), con.getLocalPath());
        }
        if (con.chdirNoRefresh(p)) {
            path = con.getPWD();
            if (con instanceof FilesystemConnection) {
                JFtp.remoteDir.getCon().setLocalPath(path);
                setDate();
            }
            pathChanged = true;
            gui(false);
            return true;
        }
        return false;
    }

    /**
    * Handles the user events if the ui is unlocked
    */
    public void actionPerformed(ActionEvent e) {
        if (JFtp.uiBlocked) {
            return;
        }
        if (e.getActionCommand().equals("rm")) {
            lock(false);
            if (Settings.getAskToDelete()) {
                if (!UITool.askToDelete(this)) {
                    unlock(false);
                    return;
                }
            }
            for (int i = 0; i < length; i++) {
                if (dirEntry[i].selected) {
                    con.removeFileOrDir(dirEntry[i].file);
                }
            }
            unlock(false);
            fresh();
        } else if (e.getActionCommand().equals("mkdir")) {
            Creator c = new Creator("Create:", con);
        } else if (e.getActionCommand().equals("cmd")) {
            RemoteCommand rc = new RemoteCommand();
        } else if (e.getActionCommand().equals("cd")) {
            String tmp = UITool.getPathFromDialog(path);
            chdir(tmp);
        } else if (e.getActionCommand().equals("fresh")) {
            fresh();
        } else if (e.getActionCommand().equals("cp")) {
            Object[] o = jl.getSelectedValues();
            if (o == null) {
                return;
            }
            String tmp = UITool.getPathFromDialog(path);
            if (tmp == null) {
                return;
            }
            if (!tmp.endsWith("/")) {
                tmp = tmp + "/";
            }
            try {
                copy(o, path, "", tmp);
                Log.debug("Copy finished...");
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.debug("Copy failed!");
            }
        } else if (e.getActionCommand().equals("zip")) {
            try {
                Object[] entry = jl.getSelectedValues();
                if (entry == null) {
                    return;
                }
                String[] tmp = new String[entry.length];
                for (int i = 0; i < tmp.length; i++) {
                    tmp[i] = entry[i].toString();
                }
                NameChooser n = new NameChooser();
                String name = n.text.getText();
                ZipFileCreator z = new ZipFileCreator(tmp, path, name);
                fresh();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (e.getActionCommand().equals("->")) {
            blockedTransfer(-2);
        } else if (e.getActionCommand().equals("<-")) {
            blockedTransfer(-2);
        } else if (e.getActionCommand().equals("rn")) {
            Object[] target = jl.getSelectedValues();
            if ((target == null) || (target.length == 0)) {
                Log.debug("No file selected");
                return;
            } else if (target.length > 1) {
                Log.debug("Too many files selected");
                return;
            }
            String val = JOptionPane.showInternalInputDialog(JFtp.desktop, "Choose a name...");
            if (val != null) {
                if (!con.rename(target[0].toString(), val)) {
                    Log.debug("Rename failed.");
                } else {
                    Log.debug("Successfully renamed.");
                    fresh();
                }
            }
        } else if (e.getSource() == props) {
            JFtp.statusP.jftp.clearLog();
            int x = currentPopup.getPermission();
            String tmp;
            if (x == FtpConnection.R) {
                tmp = "read only";
            } else if (x == FtpConnection.W) {
                tmp = "read/write";
            } else if (x == FtpConnection.DENIED) {
                tmp = "denied";
            } else {
                tmp = "undefined";
            }
            String msg = "File: " + currentPopup.toString() + "\n" + " Size: " + currentPopup.getFileSize() + " raw size: " + currentPopup.getRawSize() + "\n" + " Symlink: " + currentPopup.isLink() + "\n" + " Directory: " + currentPopup.isDirectory() + "\n" + " Permission: " + tmp + "\n";
            Log.debug(msg);
        } else if (e.getSource() == viewFile) {
            if (currentPopup.isDirectory()) {
                Log.debug("This is a directory, not a file.");
                return;
            }
            String url = JFtp.localDir.getPath() + currentPopup.toString();
            showContentWindow(url, currentPopup);
        } else if (e.getSource() == runFile) {
            if (currentPopup.isDirectory()) {
                Log.debug("This is a directory, not a file.");
                return;
            }
            String url = JFtp.localDir.getPath() + currentPopup.toString();
            showContentWindow("popup-run@" + url, currentPopup);
        } else if (e.getSource() == sorter) {
            sortMode = (String) sorter.getSelectedItem();
            if (sortMode.equals("Date")) {
                Settings.showLocalDateNoSize = true;
            } else {
                Settings.showLocalDateNoSize = false;
            }
            fresh();
        } else if (e.getActionCommand().equals("cdUp")) {
            chdir("..");
        }
    }

    private void copy(Object[] fRaw, String path, String offset, String target) throws Exception {
        String[] files = new String[fRaw.length];
        for (int j = 0; j < fRaw.length; j++) {
            files[j] = fRaw[j].toString();
        }
        for (int i = 0; i < files.length; i++) {
            File f = new File(path + offset + files[i]);
            BufferedInputStream in = null;
            BufferedOutputStream out = null;
            byte[] buf = new byte[4096];
            if (f.exists() && !f.isDirectory()) {
                in = new BufferedInputStream(new FileInputStream(path + offset + files[i]));
                out = new BufferedOutputStream(new FileOutputStream(target + offset + files[i]));
                int len = 0;
                while (in != null) {
                    len = in.read(buf);
                    if (len == StreamTokenizer.TT_EOF) {
                        break;
                    }
                    out.write(buf, 0, len);
                }
                out.flush();
                out.close();
            } else if (f.exists()) {
                if (!files[i].endsWith("/")) {
                    files[i] = files[i] + "/";
                }
                File f2 = new File(target + offset + files[i]);
                if (!f.exists()) {
                    f.mkdir();
                }
                copy(f.list(), path, offset + files[i], target);
            }
        }
    }

    /**
    * Initiate a tranfer with ui locking enabled
    */
    public synchronized void blockedTransfer(int index) {
        tmpindex = index;
        Runnable r = new Runnable() {

            public void run() {
                boolean block = !Settings.getEnableMultiThreading();
                if (!(con instanceof FtpConnection)) {
                    block = true;
                }
                if (block || Settings.getNoUploadMultiThreading()) {
                    lock(false);
                }
                transfer(tmpindex);
                if (block || Settings.getNoUploadMultiThreading()) {
                    unlock(false);
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    /**
    * Lock the gui.
    */
    public void lock(boolean first) {
        JFtp.uiBlocked = true;
        jl.setEnabled(false);
        if (!first) {
            JFtp.remoteDir.lock(true);
        }
    }

    /**
    * Unlock the gui.
    */
    public void unlock(boolean first) {
        JFtp.uiBlocked = false;
        jl.setEnabled(true);
        if (!first) {
            JFtp.remoteDir.unlock(true);
        }
    }

    /**
    * Do a hard UI refresh - do no longer call this directly, use
    * safeUpdate() instead.
    */
    public void fresh() {
        if (JFtp.mainFrame != null) {
            JFtp.mainFrame.setCursor(Cursor.WAIT_CURSOR);
        }
        String i = "";
        int idx = jl.getSelectedIndex();
        if (idx >= 0) {
            Object o = jl.getSelectedValue();
            if (o != null) {
                i = o.toString();
            }
        }
        chdir(path);
        if ((idx >= 0) && (idx < jl.getModel().getSize())) {
            if (jl.getModel().getElementAt(idx).toString().equals(i)) {
                jl.setSelectedIndex(idx);
            } else {
                jl.setSelectedIndex(0);
            }
        }
        if (JFtp.mainFrame != null) {
            JFtp.mainFrame.setCursor(Cursor.DEFAULT_CURSOR);
        }
    }

    /**
    * This manages the selections
    */
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) {
            int index = jl.getSelectedIndex() - 1;
            if ((index < 0) || (dirEntry == null) || (dirEntry.length < index) || (dirEntry[index] == null)) {
                return;
            } else {
                String tgt = (String) jl.getSelectedValue().toString();
                for (int i = 0; i < dirEntry.length; i++) {
                    dirEntry[i].setSelected(jl.isSelectedIndex(i + 1));
                }
            }
        }
    }

    /**
    * Transfers all selected files
    */
    public synchronized void transfer() {
        boolean[] bFileSelected = new boolean[dirEntry.length + 1];
        DirEntry[] cacheEntry = new DirEntry[dirEntry.length];
        System.arraycopy(dirEntry, 0, cacheEntry, 0, cacheEntry.length);
        for (int i = 0; i < dirEntry.length; i++) {
            bFileSelected[i] = cacheEntry[i].selected;
            if (!cacheEntry[i].equals(dirEntry[i])) {
                Log.out("mismatch");
            }
        }
        for (int i = 0; i < cacheEntry.length; i++) {
            if (bFileSelected[i]) {
                startTransfer(cacheEntry[i]);
            }
        }
    }

    /**
    * Start a file transfer.
    * Depending on the local and remote connection types some things like
    * local working directory have to be set, resuming may have to be checked etc.
    * As with ftp to ftp transfers the action used to download a file might actually be
    * an upload.
    *
    * WARNING: If you do anything here, please check RemoteDir.startTransfer(), too!
    */
    public void startTransfer(DirEntry entry) {
        if (con instanceof FilesystemConnection && JFtp.remoteDir.getCon() instanceof FtpConnection) {
            if ((entry.getRawSize() < Settings.smallSizeUp) && !entry.isDirectory()) {
                JFtp.remoteDir.getCon().upload(path + entry.file);
            } else {
                JFtp.remoteDir.getCon().handleUpload(path + entry.file);
            }
        } else if (con instanceof FtpConnection && JFtp.remoteDir.getCon() instanceof FtpConnection) {
            if (entry.isDirectory()) {
                Log.debug("Directory transfer between remote connections is not supported yet!");
                return;
            }
            Log.out("direct ftp transfer started (upload)");
            ((FtpConnection) JFtp.remoteDir.getCon()).upload(entry.file, ((FtpConnection) JFtp.localDir.getCon()).getDownloadInputStream(path + entry.file));
        } else if (con instanceof FtpConnection && JFtp.remoteDir.getCon() instanceof FilesystemConnection) {
            con.setLocalPath(JFtp.remoteDir.getPath());
            con.handleDownload(path + entry.file);
        } else if (con instanceof FilesystemConnection && JFtp.remoteDir.getCon() instanceof FilesystemConnection) {
            con.upload(entry.file);
            JFtp.remoteDir.actionPerformed(con, "FRESH");
        } else if (con instanceof FilesystemConnection) {
            JFtp.remoteDir.getCon().handleUpload(entry.file);
            JFtp.remoteDir.actionPerformed(con, "FRESH");
        } else {
            if (entry.isDirectory()) {
                Log.debug("Directory transfer between remote connections is not supported yet!");
                return;
            }
            Log.out("direct transfer started (upload)");
            JFtp.remoteDir.getCon().upload(entry.file, JFtp.localDir.getCon().getDownloadInputStream(path + entry.file));
            JFtp.remoteDir.actionPerformed(con, "FRESH");
        }
    }

    /**
    * Transfers single file, or all selected files if index is -1
    */
    public void transfer(int i) {
        if (i == -2) {
            transfer();
            return;
        } else if (dirEntry[i].selected) {
            startTransfer(dirEntry[i]);
        }
    }

    /**
    * Safely refresh the UI.
    */
    public void safeUpdate() {
        UpdateDaemon.updateLocalDir();
    }

    /**
    * Called by FtpConnection
    */
    public void actionPerformed(Object target, String msg) {
        safeUpdate();
    }

    /**
    * Called by FtpConnection, DownloadList is updated from here
    */
    public void updateProgress(String file, String type, long bytes) {
        if ((dList == null) || (dirEntry == null)) {
            return;
        }
        boolean flag = false;
        if (file.endsWith("/") && (file.length() > 1)) {
            flag = true;
            file = file.substring(0, file.lastIndexOf("/"));
        }
        file = file.substring(file.lastIndexOf("/") + 1);
        if (flag) {
            file = file + "/";
        }
        long s = 0;
        if (JFtp.dList.sizeCache.containsKey(file)) {
            s = ((Long) JFtp.dList.sizeCache.get(file)).longValue();
        } else {
            s = new File(getPath() + file).length();
            JFtp.dList.sizeCache.put(file, new Long(s));
        }
        dList.updateList(file, type, bytes, s);
    }

    /**
    * Called by FtpConnection
    */
    public void connectionInitialized(BasicConnection con) {
    }

    /**
    * Called by FtpConnection
    */
    public void actionFinished(BasicConnection con) {
        safeUpdate();
    }

    /**
    * Called by FtpConnection
    */
    public void connectionFailed(BasicConnection con, String reason) {
    }

    private void setDate() {
        if (!(con instanceof FtpConnection) && !(con instanceof FilesystemConnection)) {
            try {
                sorter.removeItem("Date");
            } catch (Exception ex) {
            }
            dateEnabled = false;
            return;
        }
        if ((con instanceof FtpConnection) && (((FtpConnection) con).dateVector.size() > 0)) {
            if (!dateEnabled) {
                sorter.addItem("Date");
                dateEnabled = true;
                UpdateDaemon.updateRemoteDirGUI();
            }
        } else if ((con instanceof FilesystemConnection) && (((FilesystemConnection) con).dateVector.size() > 0)) {
            if (!dateEnabled) {
                sorter.addItem("Date");
                dateEnabled = true;
                UpdateDaemon.updateRemoteDirGUI();
            }
        } else {
            if (dateEnabled) {
                sorter.removeItem("Date");
                dateEnabled = false;
                Settings.showLocalDateNoSize = false;
                UpdateDaemon.updateRemoteDirGUI();
            }
        }
    }

    /**
    * Called by FtpConnection
    */
    public void updateRemoteDirectory(BasicConnection c) {
        if (con instanceof FtpConnection) {
            path = ((FtpConnection) con).getCachedPWD();
        } else if (con instanceof SmbConnection && !path.startsWith("smb://")) {
            path = con.getPWD();
        } else {
            path = con.getPWD();
        }
        safeUpdate();
    }

    /**
    * Extract a zip file and change to the directory
    */
    private void setZipFilePath(DirEntry entry) {
        if (JFtp.mainFrame != null) {
            JFtp.mainFrame.setCursor(Cursor.WAIT_CURSOR);
        }
        String n = entry.toString();
        String tmp = path + n + "-dir/";
        Log.debug("\nExtracting: " + path + n);
        File tmpDir = new File(tmp);
        if (tmpDir.exists() || tmpDir.mkdir()) {
            try {
                ZipFile z = new ZipFile(path + n);
                Enumeration e = z.entries();
                while (e.hasMoreElements()) {
                    ZipEntry z1 = (ZipEntry) e.nextElement();
                    Log.debug("-> " + z1.getName());
                    BufferedInputStream in = new BufferedInputStream(z.getInputStream(z1));
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tmp + z1.getName()));
                    byte[] buf = new byte[8192];
                    while (in.read(buf, 0, 8192) != StreamTokenizer.TT_EOF) {
                        out.write(buf);
                    }
                    out.flush();
                    out.close();
                    in.close();
                }
                chdir(tmp);
            } catch (IOException ex) {
                ex.printStackTrace();
                Log.debug("ERROR: " + ex);
            }
        } else {
            Log.debug("Cannot create directory, skipping extraction...");
        }
        if (JFtp.mainFrame != null) {
            JFtp.mainFrame.setCursor(Cursor.DEFAULT_CURSOR);
        }
    }

    /**
    * Mime type handler for doubleclicks on files
    */
    public void showContentWindow(String url, DirEntry d) {
        if (Settings.runtimeCommands > 0 && url.startsWith("popup-run@")) {
            String ext = url.substring(10);
            try {
                System.out.println("xx: " + ext);
                if (!Settings.askToRun || (Settings.askToRun && UITool.askToRun(this))) UIUtils.runCommand(ext);
            } catch (Exception ex) {
                Log.out("Could not launch file: " + ext);
            }
            return;
        }
        if (d.toString().endsWith(".zip")) {
            setZipFilePath(d);
        } else {
            try {
                url = "file://" + url;
                String ext = url.toLowerCase();
                if (ext.endsWith(".jpg") || ext.endsWith(".gif") || ext.endsWith(".jpeg") || ext.endsWith(".png")) {
                    ImageViewer d1 = new ImageViewer(url);
                    JFtp.desktop.add(d1, new Integer(Integer.MAX_VALUE - 11));
                    return;
                }
                if (Settings.runtimeCommands > 1) {
                    try {
                        if (!Settings.askToRun || (Settings.askToRun && UITool.askToRun(this))) {
                            UIUtils.runCommand(ext);
                            return;
                        }
                    } catch (Exception ex) {
                        Log.out("Could not launch file: " + ext);
                    }
                }
                if (d.getRawSize() > 200000) {
                    Log.debug("File is too big - 200kb is the maximum, sorry.");
                    return;
                }
                HPanel f = new HPanel();
                JEditorPane pane = new JEditorPane(url);
                pane.setEditable(false);
                if (!pane.getEditorKit().getContentType().equals("text/html") && !pane.getEditorKit().getContentType().equals("text/rtf")) {
                    if (!pane.getEditorKit().getContentType().equals("text/plain")) {
                        Log.debug("Nothing to do with this filetype - use the buttons if you want to transfer files.");
                        return;
                    }
                    pane.setEditable(false);
                }
                JScrollPane jsp = new JScrollPane(pane);
                f.setLayout(new BorderLayout());
                f.add("Center", jsp);
                JFtp.statusP.jftp.addToDesktop(url, f, 600, 400);
            } catch (Exception ex) {
                Log.debug("File error: " + ex);
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            Object o = jl.getSelectedValue();
            if (o == null) {
                return;
            }
            String tmp = ((DirEntry) o).toString();
            if (tmp.endsWith("/") || tmp.equals("..")) {
                chdir(tmp);
            } else {
                showContentWindow(path + tmp, (DirEntry) o);
            }
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            int x = ((DirPanel) JFtp.remoteDir).jl.getSelectedIndex();
            if (x == -1) {
                x = 0;
            }
            ((DirPanel) JFtp.remoteDir).jl.grabFocus();
            ((DirPanel) JFtp.remoteDir).jl.setSelectedIndex(x);
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
}
