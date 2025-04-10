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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.net.FileNameMap;
import java.util.Date;
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
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import com.sshtools.j2ssh.SshClient;
import net.sf.jftp.JFtp;
import net.sf.jftp.config.SaveSet;
import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.base.dir.DirCanvas;
import net.sf.jftp.gui.base.dir.DirCellRenderer;
import net.sf.jftp.gui.base.dir.DirEntry;
import net.sf.jftp.gui.base.dir.DirLister;
import net.sf.jftp.gui.base.dir.DirPanel;
import net.sf.jftp.gui.framework.HFrame;
import net.sf.jftp.gui.framework.HImageButton;
import net.sf.jftp.gui.tasks.Creator;
import net.sf.jftp.gui.tasks.Displayer;
import net.sf.jftp.gui.tasks.PathChanger;
import net.sf.jftp.gui.tasks.RemoteCommand;
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.FilesystemConnection;
import net.sf.jftp.net.FtpConnection;
import net.sf.jftp.net.SftpConnection;
import net.sf.jftp.net.SmbConnection;
import net.sf.jftp.system.LocalIO;
import net.sf.jftp.system.StringUtils;
import net.sf.jftp.system.UpdateDaemon;
import net.sf.jftp.system.logging.Log;

public class RemoteDir extends DirPanel implements ListSelectionListener, ActionListener, ConnectionListener, KeyListener {

    static final String deleteString = "rm";

    static final String mkdirString = "mkdir";

    static final String refreshString = "fresh";

    static final String cdString = "cd";

    static final String cmdString = "cmd";

    static final String downloadString = "<-";

    static final String uploadString = "->";

    static final String queueString = "que";

    static final String cdUpString = "cdUp";

    static final String rnString = "rn";

    HImageButton deleteButton;

    HImageButton mkdirButton;

    HImageButton cmdButton;

    HImageButton refreshButton;

    HImageButton cdButton;

    HImageButton uploadButton;

    HImageButton downloadButton;

    HImageButton queueButton;

    HImageButton cdUpButton;

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

    private HImageButton list = new HImageButton(Settings.listImage, "list", "Show remote listing...", this);

    private HImageButton transferType = new HImageButton(Settings.typeImage, "type", "Toggle transfer type...", this);

    private JPopupMenu popupMenu = new JPopupMenu();

    private JMenuItem props = new JMenuItem("Properties");

    private DirEntry currentPopup = null;

    private String sortMode = null;

    String[] sortTypes = new String[] { "Normal", "Reverse", "Size", "Size/Re" };

    private JComboBox sorter = new JComboBox(sortTypes);

    private boolean dateEnabled = false;

    /**
    * RemoteDir constructor.
    */
    public RemoteDir(SshClient ssh) {
        type = "remote";
        String username = ssh.getConnectionProperties().getUsername();
        System.out.println(username);
        con = new SftpConnection(ssh);
        con.addConnectionListener(this);
        if (!con.chdir("/home/" + username + "/")) {
            con.chdir("C:\\");
        }
        setDate();
    }

    /**
    * RemoteDir constructor.
    */
    public RemoteDir(String path) {
        type = "remote";
        this.path = path;
        con = new FilesystemConnection();
        con.addConnectionListener(this);
        setDate();
    }

    /**
    * Creates the gui and adds the MouseListener etc.
    */
    public void gui_init() {
        setLayout(new BorderLayout());
        currDirPanel.setFloatable(false);
        buttonPanel.setFloatable(false);
        FlowLayout f = new FlowLayout(FlowLayout.LEFT);
        f.setHgap(1);
        f.setVgap(2);
        buttonPanel.setLayout(f);
        buttonPanel.setMargin(new Insets(0, 0, 0, 0));
        props.addActionListener(this);
        popupMenu.add(props);
        rnButton = new HImageButton(Settings.textFileImage, rnString, "Rename selected file or directory", this);
        rnButton.setToolTipText("Rename selected");
        list.setToolTipText("Show remote listing...");
        transferType.setToolTipText("Toggle transfer type...");
        deleteButton = new HImageButton(Settings.deleteImage, deleteString, "Delete  selected", this);
        deleteButton.setToolTipText("Delete selected");
        mkdirButton = new HImageButton(Settings.mkdirImage, mkdirString, "Create a new directory", this);
        mkdirButton.setToolTipText("Create directory");
        refreshButton = new HImageButton(Settings.refreshImage, refreshString, "Refresh current directory", this);
        refreshButton.setToolTipText("Refresh directory");
        cdButton = new HImageButton(Settings.cdImage, cdString, "Change directory", this);
        cdButton.setToolTipText("Change directory");
        cmdButton = new HImageButton(Settings.cmdImage, cmdString, "Execute remote command", this);
        cmdButton.setToolTipText("Execute remote command");
        downloadButton = new HImageButton(Settings.downloadImage, downloadString, "Download selected", this);
        downloadButton.setToolTipText("Download selected");
        queueButton = new HImageButton(Settings.downloadImage, queueString, "Queue selected", this);
        queueButton.setToolTipText("Queue selected");
        cdUpButton = new HImageButton(Settings.cdUpImage, cdUpString, "Go to Parent Directory", this);
        cdUpButton.setToolTipText("Go to Parent Directory");
        setLabel();
        label.setSize(getSize().width - 10, 24);
        currDirPanel.add(label);
        currDirPanel.setSize(getSize().width - 10, 32);
        label.setSize(getSize().width - 20, 24);
        p.setLayout(new BorderLayout());
        p.add("North", currDirPanel);
        buttonPanel.add(downloadButton);
        buttonPanel.add(queueButton);
        buttonPanel.add(new JLabel("  "));
        buttonPanel.add(refreshButton);
        buttonPanel.add(new JLabel("  "));
        buttonPanel.add(rnButton);
        buttonPanel.add(mkdirButton);
        buttonPanel.add(cdButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(cdUpButton);
        buttonPanel.add(new JLabel("  "));
        buttonPanel.add(cmdButton);
        buttonPanel.add(list);
        buttonPanel.add(transferType);
        buttonPanel.add(sorter);
        buttonPanel.setVisible(true);
        buttonPanel.setSize(getSize().width - 10, 32);
        p.add("West", buttonPanel);
        add("North", p);
        sorter.addActionListener(this);
        jlm = new DefaultListModel();
        jl = new JList(jlm);
        jl.setCellRenderer(new DirCellRenderer());
        jl.setVisibleRowCount(Settings.visibleFileRows);
        jl.addListSelectionListener(this);
        jl.addKeyListener(this);
        jl.setDropTarget(JFtp.statusP.jftp.dropTarget);
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
                        if (JFtp.mainFrame != null) {
                            JFtp.mainFrame.setCursor(Cursor.WAIT_CURSOR);
                        }
                        con.chdir(path + tgt);
                        if (JFtp.mainFrame != null) {
                            JFtp.mainFrame.setCursor(Cursor.DEFAULT_CURSOR);
                        }
                    } else if ((dirEntry == null) || (dirEntry.length < index) || (dirEntry[index] == null)) {
                        return;
                    } else if (dirEntry[index].isDirectory()) {
                        con.chdir(path + tgt);
                    } else if (dirEntry[index].isLink()) {
                        if (!con.chdir(path + tgt)) {
                            showContentWindow(path + dirEntry[index].toString(), dirEntry[index]);
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

    private void setLabel() {
        if (con instanceof FilesystemConnection) {
            label.setText("Filesystem: " + StringUtils.cutPath(path));
        } else if (con instanceof FtpConnection) {
            label.setText("Ftp: " + StringUtils.cutPath(path));
        } else if (con instanceof SftpConnection) {
            label.setText("Sftp: " + StringUtils.cutPath(path));
        } else {
            label.setText(StringUtils.cutPath(path));
        }
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
        setLabel();
        if (con instanceof FtpConnection) {
            list.show();
            cmdButton.show();
            transferType.show();
        } else {
            list.hide();
            cmdButton.hide();
            transferType.hide();
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
                        if (dirEntry[i].file.endsWith("###")) {
                            dirEntry[i].setLink();
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
        jl.grabFocus();
        jl.setSelectedIndex(0);
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
            if (!(con instanceof FtpConnection)) {
                Log.debug("This feature is for ftp only.");
                return;
            }
            RemoteCommand rc = new RemoteCommand();
        } else if (e.getActionCommand().equals("cd")) {
            PathChanger pthc = new PathChanger("remote");
        } else if (e.getActionCommand().equals("fresh")) {
            fresh();
        } else if (e.getActionCommand().equals("->")) {
            blockedTransfer(-2);
        } else if (e.getActionCommand().equals("<-")) {
            blockedTransfer(-2);
        } else if (e.getActionCommand().equals("list")) {
            try {
                if (!(con instanceof FtpConnection)) {
                    Log.debug("Can only list FtpConnection output!");
                }
                PrintStream out = new PrintStream(Settings.ls_out);
                for (int i = 0; i < ((FtpConnection) con).currentListing.size(); i++) {
                    out.println(((FtpConnection) con).currentListing.get(i));
                }
                out.flush();
                out.close();
                java.net.URL url = new java.io.File(Settings.ls_out).toURL();
                Displayer d = new Displayer(url);
                JFtp.desktop.add(d, new Integer(Integer.MAX_VALUE - 13));
            } catch (java.net.MalformedURLException ex) {
                ex.printStackTrace();
                Log.debug("ERROR: Malformed URL!");
            } catch (FileNotFoundException ex2) {
                ex2.printStackTrace();
                Log.debug("ERROR: File not found!");
            }
        } else if (e.getActionCommand().equals("type") && (!JFtp.uiBlocked)) {
            if (!(con instanceof FtpConnection)) {
                Log.debug("You can only set the transfer type for ftp connections.");
                return;
            }
            FtpConnection c = (FtpConnection) con;
            String t = c.getTypeNow();
            boolean ret = false;
            if (t.equals(FtpConnection.ASCII)) {
                ret = c.type(FtpConnection.BINARY);
            } else if (t.equals(FtpConnection.BINARY)) {
                ret = c.type(FtpConnection.EBCDIC);
            }
            if (t.equals(FtpConnection.EBCDIC) || (!ret && !t.equals(FtpConnection.L8))) {
                ret = c.type(FtpConnection.L8);
            }
            if (!ret) {
                c.type(FtpConnection.ASCII);
                Log.debug("Warning: type should be \"I\" if you want to transfer binary files!");
            }
            Log.debug("Type is now " + c.getTypeNow());
        } else if (e.getActionCommand().equals("que")) {
            if (!(con instanceof FtpConnection)) {
                Log.debug("Queue supported only for FTP");
                return;
            }
            Object[] o = jl.getSelectedValues();
            DirEntry[] tmp = new DirEntry[Array.getLength(o)];
            for (int i = 0; i < Array.getLength(o); i++) {
                tmp[i] = (DirEntry) o[i];
                JFtp.dQueue.addFtp(tmp[i].toString());
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
        } else if (e.getSource() == sorter) {
            sortMode = (String) sorter.getSelectedItem();
            if (sortMode.equals("Date")) {
                Settings.showDateNoSize = true;
            } else {
                Settings.showDateNoSize = false;
            }
            fresh();
        } else if (e.getActionCommand().equals("cdUp")) {
            JFtp.remoteDir.getCon().chdir("..");
        } else if (e.getActionCommand().equals("rn")) {
            Object[] target = jl.getSelectedValues();
            if ((target == null) || (target.length == 0)) {
                Log.debug("No file selected");
                return;
            } else if (target.length > 1) {
                Log.debug("Too many files selected");
                return;
            }
            String val = JOptionPane.showInternalInputDialog(this, "Choose a name...");
            if (val != null) {
                if (!con.rename(target[0].toString(), val)) {
                    Log.debug("Rename failed.");
                } else {
                    Log.debug("Successfully renamed.");
                    fresh();
                }
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
                if (block) {
                    lock(false);
                }
                transfer(tmpindex);
                if (block) {
                    JFtp.localDir.fresh();
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
            JFtp.localDir.lock(true);
        }
        Log.out("ui locked.");
    }

    /**
    * Unlock the gui.
    */
    public void unlock(boolean first) {
        JFtp.uiBlocked = false;
        jl.setEnabled(true);
        if (!first) {
            JFtp.localDir.unlock(true);
        }
        Log.out("ui unlocked.");
    }

    /**
    * Do a hard UI refresh - do no longe call this directly, use
    * safeUpdate() instead if possible.
    */
    public void fresh() {
        Log.out("fresh() called.");
        Cursor x = null;
        if (JFtp.mainFrame != null) {
            x = JFtp.mainFrame.getCursor();
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
        con.chdir(path);
        if ((idx >= 0) && (idx < jl.getModel().getSize())) {
            if (jl.getModel().getElementAt(idx).toString().equals(i)) {
                jl.setSelectedIndex(idx);
            } else {
                jl.setSelectedIndex(0);
            }
        }
        if ((JFtp.mainFrame != null) && (x.getType() != Cursor.WAIT_CURSOR)) {
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
            for (int i = 0; i < dirEntry.length; i++) {
                if (dirEntry[i] == null) {
                    continue;
                }
                if (dirEntry[i].toString().equals(file)) {
                    s = dirEntry[i].getRawSize();
                    JFtp.dList.sizeCache.put(file, new Long(s));
                    break;
                }
            }
            if (s <= 0) {
                File f = new File(JFtp.localDir.getPath() + file);
                if (f.exists()) {
                    s = f.length();
                }
            }
        }
        dList.updateList(file, type, bytes, s);
    }

    /**
    * Called by FtpConnection
    */
    public void connectionInitialized(BasicConnection con) {
        if (con == null) {
            return;
        }
        setDate();
        Log.out("remote connection initialized");
    }

    /**
    * Called by FtpConnection
    */
    public void connectionFailed(BasicConnection con, String reason) {
        Log.out("remote connection failed");
        if ((Integer.parseInt(reason) == FtpConnection.OFFLINE) && Settings.reconnect) {
            return;
        }
        HFrame h = new HFrame();
        h.getContentPane().setLayout(new BorderLayout(10, 10));
        h.setTitle("Connection failed!");
        h.setLocation(150, 200);
        JTextArea text = new JTextArea();
        h.getContentPane().add("Center", text);
        text.setText(" ---------------- Output -----------------\n" + JFtp.log.getText());
        JFtp.log.setText("");
        text.setEditable(false);
        h.pack();
        h.show();
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
                try {
                    sorter.removeItem("Date");
                    dateEnabled = false;
                    Settings.showDateNoSize = false;
                    UpdateDaemon.updateRemoteDirGUI();
                } catch (Exception ex) {
                }
            }
        }
    }

    /**
    * Called by FtpConnection
    */
    public void updateRemoteDirectory(BasicConnection c) {
        if (con == null) {
            return;
        }
        if ((c != con) && !c.hasUploaded && con instanceof FtpConnection) {
            return;
        }
        setDate();
        if (con instanceof FtpConnection) {
            path = ((FtpConnection) con).getCachedPWD();
        } else if (con instanceof SmbConnection && !path.startsWith("smb://")) {
            path = c.getPWD();
        } else {
            path = con.getPWD();
        }
        if ((c != null) && (c instanceof FtpConnection)) {
            FtpConnection con = (FtpConnection) c;
            String tmp = con.getCachedPWD();
            SaveSet s = new SaveSet(Settings.login_def, con.getHost(), con.getUsername(), con.getPassword(), Integer.toString(con.getPort()), tmp, con.getLocalPath());
        } else if ((c != null) && (c instanceof FilesystemConnection)) {
            JFtp.localDir.getCon().setLocalPath(path);
        }
        pathChanged = true;
        gui(false);
        UpdateDaemon.updateLog();
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
    * WARNING: If you do anything here, please check LocalDir.startTransfer(), too!
    */
    public void startTransfer(DirEntry entry) {
        if (con instanceof FtpConnection && JFtp.localDir.getCon() instanceof FtpConnection) {
            if (entry.isDirectory()) {
                Log.debug("Directory transfer between remote connections is not supported yet!");
                return;
            }
            Log.out("direct ftp transfer started (download)");
            ((FtpConnection) JFtp.localDir.getCon()).upload(entry.file, ((FtpConnection) JFtp.remoteDir.getCon()).getDownloadInputStream(path + entry.file));
        } else if (con instanceof FtpConnection && JFtp.localDir.getCon() instanceof FilesystemConnection) {
            int status = checkForExistingFile(entry);
            if (status >= 0) {
                long s = entry.getRawSize();
                JFtp.dList.sizeCache.put(entry.file, new Long(s));
                if ((entry.getRawSize() < Settings.smallSize) && !entry.isDirectory()) {
                    con.download(entry.file);
                } else {
                    con.handleDownload(path + entry.file);
                }
            }
        } else if (con instanceof FilesystemConnection && JFtp.localDir.getCon() instanceof FtpConnection) {
            try {
                File f = new File(path + entry.file);
                FileInputStream in = new FileInputStream(f);
                JFtp.localDir.getCon().setLocalPath(path);
                Log.debug(JFtp.localDir.getCon().getPWD());
                ((FtpConnection) JFtp.localDir.getCon()).upload(entry.file, in);
            } catch (FileNotFoundException ex) {
                Log.debug("Error: File not found: " + path + entry.file);
            }
        } else if (con instanceof FilesystemConnection && JFtp.localDir.getCon() instanceof FilesystemConnection) {
            con.download(path + entry.file);
            JFtp.localDir.actionPerformed(con, "");
        } else if (JFtp.localDir.getCon() instanceof FilesystemConnection) {
            con.handleDownload(entry.file);
            JFtp.localDir.actionPerformed(con, "");
        } else {
            if (entry.isDirectory()) {
                Log.debug("Directory transfer between remote connections is not supported yet!");
                return;
            }
            Log.out("direct transfer started (download)");
            JFtp.localDir.getCon().upload(entry.file, JFtp.remoteDir.getCon().getDownloadInputStream(path + entry.file));
            JFtp.localDir.actionPerformed(con, "FRESH");
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
    * Ask for resuming or overwrite if a local file does already exist for a download
    */
    private int checkForExistingFile(DirEntry dirEntry) {
        File f = new File(JFtp.localDir.getPath() + dirEntry.file);
        if (f.exists() && Settings.enableResuming && Settings.askToResume) {
            ResumeDialog r = new ResumeDialog(dirEntry);
            return -1;
        }
        return 1;
    }

    /**
    * Called by FtpConnection
    */
    public void actionFinished(BasicConnection c) {
        JFtp.localDir.actionPerformed(c, "LOWFRESH");
        if (c instanceof FtpConnection) {
            if (((FtpConnection) c).hasUploaded) {
                Log.out("actionFinished called by upload: " + c);
                UpdateDaemon.updateRemoteDir();
            }
            Log.out("actionFinished called by download: " + c);
        } else {
            Log.out("actionFinished called by: " + c);
            UpdateDaemon.updateRemoteDir();
        }
        UpdateDaemon.updateLog();
    }

    /**
    * Called by FtpConnection
    */
    public void actionPerformed(Object target, String msg) {
        if (msg.equals(type)) {
            UpdateDaemon.updateRemoteDirGUI();
        } else if (msg.equals("FRESH")) {
            UpdateDaemon.updateRemoteDir();
        }
        UpdateDaemon.updateLog();
    }

    /**
    * Mime type handler for doubleclicks on files
    */
    public void showContentWindow(String url, DirEntry d) {
        try {
            if (d.getRawSize() > 200000) {
                Log.debug("File is too big - 200kb is the maximum, sorry.");
                return;
            }
            String path = JFtp.localDir.getPath();
            if (!path.endsWith("/")) {
                path = path + "/";
            }
            if (!new File(path + StringUtils.getFile(url)).exists()) {
                con.download(url);
            } else {
                Log.debug("\nRemote file must be downloaded to be viewed and\n" + " you already have a local copy present, pleasen rename it\n" + " and try again.");
                return;
            }
            File file = new File(JFtp.localDir.getPath() + StringUtils.getFile(url));
            if (!file.exists()) {
                Log.debug("File not found: " + JFtp.localDir.getPath() + StringUtils.getFile(url));
            }
            HFrame f = new HFrame();
            f.setTitle(url);
            JEditorPane pane = new JEditorPane("file://" + file.getAbsolutePath());
            if (!pane.getEditorKit().getContentType().equals("text/html") && !pane.getEditorKit().getContentType().equals("text/rtf")) {
                if (!pane.getEditorKit().getContentType().equals("text/plain")) {
                    Log.debug("Nothing to do with this filetype - use the buttons if you want to transfer files.");
                    return;
                }
                pane.setEditable(false);
            }
            JScrollPane jsp = new JScrollPane(pane);
            f.getContentPane().setLayout(new BorderLayout());
            f.getContentPane().add("Center", jsp);
            f.setModal(false);
            f.setLocation(100, 100);
            f.setSize(600, 400);
            f.show();
            dList.fresh();
            JFtp.localDir.getCon().removeFileOrDir(StringUtils.getFile(url));
            JFtp.localDir.fresh();
        } catch (Exception ex) {
            Log.debug("File error: " + ex);
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            Object o = jl.getSelectedValue();
            if (o == null) {
                return;
            }
            String tmp = ((DirEntry) o).toString();
            if (tmp.endsWith("/")) {
                con.chdir(tmp);
            } else {
                showContentWindow(path + tmp, (DirEntry) o);
            }
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            int x = ((DirPanel) JFtp.localDir).jl.getSelectedIndex();
            if (x == -1) {
                x = 0;
            }
            ((DirPanel) JFtp.localDir).jl.grabFocus();
            ((DirPanel) JFtp.localDir).jl.setSelectedIndex(x);
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
}
