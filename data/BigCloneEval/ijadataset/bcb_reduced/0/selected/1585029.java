package org.dbe.dfs.explorer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.log4j.Logger;
import org.dbe.dfs.DFSException;
import org.dbe.dfs.DFSFile;
import org.dbe.dfs.DFSFileInfo;

/**
 * DSSExplorer provides a user interface for exploring and manipulating
 * a Distributed Storage System (DSS). The keyboard and/or mouse can be used to
 * navigate, upload, download, rename and delete files and directories.
 * New directories can be created and properties can be viewed.
 *
 * The browser supports concurrent uploads and downloads. A location can be
 * jumped to directly by entering the path into the toolbar and clicking the Go
 * button.
 *
 * @author Intel Ireland Ltd.
 * @version 1.0.0a1
 */
public class DFSExplorer extends JFrame {

    /**
     * Processes Paste command
     */
    private final class PasteActionListener implements ActionListener {

        private final int maxRwLength;

        private final DFSExplorer explorer;

        /**
         * Creates a new instance.
         *
         * @param blockSize size of the bloks of data
         * @param dssExplorer parent explorer object
         */
        private PasteActionListener(final int blockSize, final DFSExplorer dssExplorer) {
            super();
            maxRwLength = blockSize;
            explorer = dssExplorer;
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent e) {
            FilePasteTask task = new FilePasteTask(explorer, current, clipboard, maxRwLength);
            task.start();
        }
    }

    /**
     * Processes Home command
     */
    private final class HomeActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent e) {
            goHome();
        }
    }

    /**
     * Processes 'Up one Level' command
     */
    private final class UpActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent e) {
            upLevel();
        }
    }

    /**
     * Processes 'Add Connection' command
     */
    private final class AddConnectionActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent e) {
            new AddFadaConnectionDialog(explorer);
        }
    }

    /**
     * Processes About command
     */
    private final class AboutActionListener implements ActionListener {

        private final String version;

        /**
         * Creates a new instance.
         *
         * @param ver version String of the DFS Explorer
         */
        private AboutActionListener(final String ver) {
            super();
            this.version = ver;
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent e) {
            JOptionPane.showMessageDialog(null, "DFS Explorer Version " + version + ", Intel Ireland Ltd. 2007", "About", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Processes Refresh command
     */
    private final class RefreshActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent e) {
            refresh();
        }
    }

    /**
     * Processes Close command
     */
    private final class CloseActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent e) {
            System.exit(0);
        }
    }

    /**
     * Processes Properties command
     */
    private final class PropertiesActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent e) {
            displayProperties(selected);
        }
    }

    /**
     * Processes Rename command
     */
    private final class RenameActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent e) {
            try {
                renameFile(new DFSFile("JFS", selected.getPath()));
            } catch (DFSException ex) {
                logger.debug(ex);
            }
        }
    }

    /**
     * Processes Delete command
     */
    private final class DeleteActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent e) {
            SwingWorker worker = new SwingWorker() {

                public Object construct() {
                    try {
                        if (selected != null) {
                            delete(new DFSFile("JFS", selected.getPath()));
                        }
                    } catch (DFSException ex) {
                        logger.debug(ex);
                    }
                    return null;
                }
            };
            worker.start();
        }
    }

    /**
     * Processes Open command
     */
    private final class OpenActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent event) {
            if (selected != null) {
                if (selected.isDirectory()) {
                    try {
                        open(new DFSFile("JFS", selected.getPath()));
                    } catch (DFSException e) {
                        logger.error(e);
                    }
                } else {
                    FileDownloadTask task = new FileDownloadTask(explorer, current, selected, MAX_RW_LENGTH);
                    task.start();
                }
            }
        }
    }

    /**
     * Processes Download command
     */
    private final class DownloadActionListener implements ActionListener {

        private final int maxRwLength;

        private final DFSExplorer explorer;

        /**
         * Creates a new instance.
         *
         * @param blockSize size of blocks of data to be transferred
         * @param dssExplorer parent explorer object
         */
        private DownloadActionListener(final int blockSize, final DFSExplorer dssExplorer) {
            super();
            maxRwLength = blockSize;
            explorer = dssExplorer;
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent e) {
            FileDownloadTask task = new FileDownloadTask(explorer, current, selected, maxRwLength);
            task.start();
        }
    }

    /**
     * Processes Upload command
     */
    private final class UploadActionListener implements ActionListener {

        private final DFSExplorer explorer;

        private final int maxRwLength;

        /**
         * Creates a new instance.
         *
         * @param dssExplorer parent explorer object
         * @param blockSize size of the blocks of data to be transferred
         */
        private UploadActionListener(final DFSExplorer dssExplorer, final int blockSize) {
            super();
            explorer = dssExplorer;
            maxRwLength = blockSize;
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent e) {
            FileUploadTask task = new FileUploadTask(explorer, current, selected, maxRwLength);
            task.start();
        }
    }

    /**
     * Processes New Directory command
     */
    private final class NewDirectoryActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent e) {
            try {
                createDirectory();
            } catch (DFSException ex) {
                logger.debug(ex);
            }
        }
    }

    /**
     * Processes Copy command
     */
    private final class CopyActionListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(final ActionEvent e) {
            if (selected != null) {
                copyFile(new DFSFile("JFS", selected.getPath()));
            }
        }
    }

    /**
     * Listens for changes in selection of files
     *
     */
    private final class ExplorerListListener implements ListSelectionListener {

        /**
         * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
         */
        public void valueChanged(final ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                setControls(true);
                selected = (DFSFileInfo) fileList.getSelectedValue();
            }
        }
    }

    private static final String VERSION = "0.6.0";

    final DFSExplorer explorer = this;

    private Container c;

    private JList fileList;

    private JTextField displayPath;

    JMenuItem downloadItem;

    JMenuItem openItem;

    JMenuItem deleteItem;

    JMenuItem renameItem;

    JMenuItem addConnectionItem;

    JMenuItem propertiesItem;

    JMenuItem downloadItemPU;

    JMenuItem renameItemPU;

    JMenuItem propertiesItemPU;

    JMenuItem deleteItemPU;

    JButton propertiesButton;

    JButton downloadButton;

    JButton deleteButton;

    JButton copyButton;

    JButton pasteButton;

    private String sep;

    private DFSFile root;

    private DFSFile current;

    private DFSFileInfo selected;

    private static final int MAX_RW_LENGTH = 65536;

    private DFSFile clipboard;

    private static Logger logger = Logger.getLogger("org.dbe.dfs.explorer.DFSExplorer");

    private ArrayList names;

    static final String ROOT = DFSFile.separator;

    /**
     * Constructor initialises list with the current contents of the
     * root directory and displays the elements of the user interface.
     * It also adds action handlers for the components.
     */
    public DFSExplorer() {
        super("DFS Explorer");
        BorderLayout layout = new BorderLayout();
        sep = DFSFile.separator;
        root = new DFSFile("JFS", ROOT);
        current = new DFSFile("JFS", ROOT);
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        JMenuItem newItem = new JMenuItem("New Directory");
        newItem.setMnemonic('N');
        newItem.addActionListener(new NewDirectoryActionListener());
        fileMenu.add(newItem);
        fileMenu.addSeparator();
        JMenuItem uploadItem = new JMenuItem("Upload");
        uploadItem.setMnemonic('U');
        uploadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.ALT_MASK));
        uploadItem.addActionListener(new UploadActionListener(explorer, MAX_RW_LENGTH));
        fileMenu.add(uploadItem);
        downloadItem = new JMenuItem("Download");
        downloadItem.setMnemonic('w');
        downloadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK));
        downloadItem.addActionListener(new DownloadActionListener(MAX_RW_LENGTH, explorer));
        fileMenu.add(downloadItem);
        fileMenu.addSeparator();
        openItem = new JMenuItem("Open");
        openItem.setMnemonic('O');
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        openItem.addActionListener(new OpenActionListener());
        fileMenu.add(openItem);
        deleteItem = new JMenuItem("Delete");
        deleteItem.setMnemonic('D');
        deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteItem.addActionListener(new DeleteActionListener());
        fileMenu.add(deleteItem);
        renameItem = new JMenuItem("Rename");
        renameItem.setMnemonic('m');
        renameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        renameItem.addActionListener(new RenameActionListener());
        fileMenu.add(renameItem);
        propertiesItem = new JMenuItem("Properties");
        propertiesItem.setMnemonic('r');
        propertiesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.ALT_MASK));
        propertiesItem.addActionListener(new PropertiesActionListener());
        fileMenu.add(propertiesItem);
        fileMenu.addSeparator();
        addConnectionItem = new JMenuItem("Add connection");
        addConnectionItem.setMnemonic('A');
        addConnectionItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
        addConnectionItem.addActionListener(new AddConnectionActionListener());
        fileMenu.add(addConnectionItem);
        fileMenu.addSeparator();
        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.setMnemonic('C');
        closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
        closeItem.addActionListener(new CloseActionListener());
        fileMenu.add(closeItem);
        menuBar.add(fileMenu);
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');
        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.setMnemonic('C');
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        copyItem.addActionListener(new CopyActionListener());
        editMenu.add(copyItem);
        JMenuItem pasteItem = new JMenuItem("Paste");
        pasteItem.setMnemonic('P');
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        pasteItem.addActionListener(new PasteActionListener(MAX_RW_LENGTH, this));
        editMenu.add(pasteItem);
        menuBar.add(editMenu);
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');
        JMenu gotoMenu = new JMenu("Go To");
        gotoMenu.setMnemonic('o');
        viewMenu.add(gotoMenu);
        JMenuItem homeItem = new JMenuItem("Home");
        homeItem.setMnemonic('H');
        homeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, ActionEvent.ALT_MASK));
        homeItem.addActionListener(new HomeActionListener());
        gotoMenu.add(homeItem);
        JMenuItem upItem = new JMenuItem("Up One Level");
        upItem.setMnemonic('U');
        upItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.ALT_MASK));
        upItem.addActionListener(new UpActionListener());
        gotoMenu.add(upItem);
        JMenuItem refreshItem = new JMenuItem("Refresh");
        refreshItem.setMnemonic('R');
        refreshItem.addActionListener(new RefreshActionListener());
        viewMenu.add(refreshItem);
        menuBar.add(viewMenu);
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        JMenuItem aboutItem = new JMenuItem("About DFS Explorer");
        aboutItem.setMnemonic('A');
        aboutItem.addActionListener(new AboutActionListener(VERSION));
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);
        fileList = new JList();
        fileList.setVisibleRowCount(-1);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.setLayoutOrientation(JList.VERTICAL_WRAP);
        fileList.setCellRenderer(new FileListCellRenderer());
        fileList.addListSelectionListener(new ExplorerListListener());
        MouseListener fileClickListener = new MouseAdapter() {

            public void mouseClicked(final MouseEvent e) {
                int index = fileList.locationToIndex(e.getPoint());
                fileList.setSelectedIndex(index);
                setControls(true);
                try {
                    selected = (DFSFileInfo) fileList.getSelectedValue();
                    if (e.getClickCount() == 2) {
                        if (selected.isDirectory()) {
                            open(new DFSFile("JFS", selected.getPath()));
                        } else {
                            FileDownloadTask task = new FileDownloadTask(explorer, current, selected, MAX_RW_LENGTH);
                            task.start();
                        }
                    }
                } catch (DFSException ex) {
                    logger.debug(ex);
                }
            }
        };
        fileList.addMouseListener(fileClickListener);
        JPopupMenu popupMenu = new JPopupMenu();
        downloadItemPU = new JMenuItem("Download");
        downloadItemPU.addActionListener(new DownloadActionListener(MAX_RW_LENGTH, explorer));
        popupMenu.add(downloadItemPU);
        popupMenu.addSeparator();
        renameItemPU = new JMenuItem("Rename");
        renameItemPU.addActionListener(new RenameActionListener());
        popupMenu.add(renameItemPU);
        deleteItemPU = new JMenuItem("Delete");
        deleteItemPU.addActionListener(new DeleteActionListener());
        popupMenu.add(deleteItemPU);
        propertiesItemPU = new JMenuItem("Properties");
        propertiesItemPU.addActionListener(new PropertiesActionListener());
        popupMenu.add(propertiesItemPU);
        MouseListener popupListener = new PopupListener(popupMenu, fileList);
        fileList.addMouseListener(popupListener);
        ImageIcon upIcon = createImageIcon("up.gif", "Up");
        JButton upButton = new JButton(upIcon);
        upButton.setToolTipText("Up");
        upButton.addActionListener(new UpActionListener());
        ImageIcon refreshIcon = createImageIcon("refresh.gif", "Refresh");
        JButton refreshButton = new JButton(refreshIcon);
        refreshButton.setToolTipText("Refresh");
        refreshButton.addActionListener(new RefreshActionListener());
        ImageIcon homeIcon = createImageIcon("home.gif", "Home");
        JButton homeButton = new JButton(homeIcon);
        homeButton.setToolTipText("Home");
        homeButton.addActionListener(new HomeActionListener());
        ImageIcon propertiesIcon = createImageIcon("properties.gif", "Properties");
        propertiesButton = new JButton(propertiesIcon);
        propertiesButton.setToolTipText("Properties");
        propertiesButton.addActionListener(new PropertiesActionListener());
        ImageIcon copyIcon = createImageIcon("copy.gif", "Copy");
        copyButton = new JButton(copyIcon);
        copyButton.setToolTipText("Copy");
        copyButton.addActionListener(new CopyActionListener());
        ImageIcon pasteIcon = createImageIcon("paste.gif", "Paste");
        pasteButton = new JButton(pasteIcon);
        pasteButton.setToolTipText("Paste");
        pasteButton.addActionListener(new PasteActionListener(MAX_RW_LENGTH, this));
        ImageIcon newIcon = createImageIcon("new.gif", "New Directory");
        JButton newButton = new JButton(newIcon);
        newButton.setToolTipText("New DFS Directory");
        newButton.addActionListener(new NewDirectoryActionListener());
        ImageIcon uploadIcon = createImageIcon("upload.gif", "Upload");
        JButton uploadButton = new JButton(uploadIcon);
        uploadButton.setToolTipText("Upload to DFS");
        uploadButton.addActionListener(new UploadActionListener(explorer, MAX_RW_LENGTH));
        ImageIcon downloadIcon = createImageIcon("download.gif", "Download");
        downloadButton = new JButton(downloadIcon);
        downloadButton.setToolTipText("Download from DFS");
        downloadButton.addActionListener(new DownloadActionListener(MAX_RW_LENGTH, explorer));
        ImageIcon deleteIcon = createImageIcon("delete.gif", "Delete");
        deleteButton = new JButton(deleteIcon);
        deleteButton.setToolTipText("Delete");
        deleteButton.addActionListener(new DeleteActionListener());
        displayPath = new JTextField(25);
        displayPath.addKeyListener(new KeyListener() {

            public void keyPressed(final KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_ENTER) {
                    try {
                        go(displayPath.getText());
                    } catch (DFSException ex) {
                        logger.debug(ex);
                    }
                }
            }

            public void keyTyped(final KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_ENTER) {
                    try {
                        go(displayPath.getText());
                    } catch (DFSException ex) {
                        logger.debug(ex);
                    }
                }
            }

            public void keyReleased(final KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_ENTER) {
                    try {
                        go(displayPath.getText());
                    } catch (DFSException ex) {
                        logger.debug(ex);
                    }
                }
            }
        });
        ImageIcon goIcon = createImageIcon("go.gif", "Go");
        JButton goButton = new JButton(goIcon);
        goButton.setToolTipText("Go");
        goButton.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                try {
                    go(displayPath.getText());
                } catch (DFSException ex) {
                    logger.debug(ex);
                }
            }
        });
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        JScrollPane filePane = new JScrollPane(fileList);
        c = getContentPane();
        c.setBackground(Color.gray);
        c.setLayout(layout);
        c.add(filePane, BorderLayout.CENTER);
        toolbar.add(upButton);
        toolbar.add(refreshButton);
        toolbar.add(homeButton);
        toolbar.addSeparator();
        toolbar.add(propertiesButton);
        toolbar.addSeparator();
        toolbar.add(copyButton);
        toolbar.add(pasteButton);
        toolbar.addSeparator();
        toolbar.add(newButton);
        toolbar.add(uploadButton);
        toolbar.add(downloadButton);
        toolbar.add(deleteButton);
        toolbar.add(displayPath);
        displayPath.setText(current.getPath());
        toolbar.add(goButton);
        c.add(toolbar, BorderLayout.NORTH);
        setControls(false);
        pasteButton.setEnabled(false);
        setSize(650, 400);
        show();
        checkDssAvailable();
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     *
     * @param path the path of the file relative to the class
     * @param description a description of the image
     * @return the image icon located at the path specified
     */
    private static ImageIcon createImageIcon(final String path, final String description) {
        java.net.URL imgURL = DFSExplorer.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            logger.warn("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Switches on and off controls that should be enabled/disabled
     * depending on certain criteria.
     *
     * @param status whether the controls should be on or off
     */
    private void setControls(final boolean status) {
        downloadItem.setEnabled(status);
        deleteItem.setEnabled(status);
        renameItem.setEnabled(status);
        downloadItemPU.setEnabled(status);
        renameItemPU.setEnabled(status);
        deleteItemPU.setEnabled(status);
        propertiesButton.setEnabled(status);
        downloadButton.setEnabled(status);
        deleteButton.setEnabled(status);
        copyButton.setEnabled(status);
    }

    /**
     * Returns the total size (in bytes) of the files that the relevant
     * operation is to be performed on.
     *
     * @param opFile the file that the operation is being performed on
     * @return the size in bytes of the file
     * @throws DFSException if a DFS exception occurs
     */
    protected final long getOperationSize(final File opFile) throws DFSException {
        if (opFile.isFile()) {
            return opFile.length();
        } else {
            return totalSize(opFile);
        }
    }

    /**
     * Recursive function calculates the total size (in bytes) of all
     * files in the specified directory.
     *
     * @param opFile the directory that the operation is being performed on
     * @return the total size (in bytes) of all files in the directory
     * @throws DFSException if a DFS exception occurs
     */
    private long totalSize(final File opFile) throws DFSException {
        long totalBytes = 0;
        File[] fileArray = opFile.listFiles();
        if (fileArray == null || fileArray.length == 0) {
            totalBytes = 0;
        } else {
            for (int i = 0; i < fileArray.length; i++) {
                if (fileArray[i].isFile()) {
                    totalBytes += fileArray[i].length();
                } else {
                    totalBytes += totalSize(fileArray[i]);
                }
            }
        }
        return totalBytes;
    }

    /**
     * Makes a copy of a file in the distributed storage system
     * to move elsewhere.
     *
     * @param cpyFile the file that is being copied
     */
    private void copyFile(final DFSFile cpyFile) {
        clipboard = new DFSFile("JFS", cpyFile.getPath());
        pasteButton.setEnabled(true);
    }

    /**
     * Helper method that fills the list with the contents of
     * the current directory.
     *
     * @param currentDir the directory currently being browsed
     */
    private void populateList(final DFSFile currentDir) {
        getContents(currentDir);
        displayPath.setText(currentDir.getPath());
        fileList.setListData(names.toArray());
        setControls(false);
    }

    /**
     * Fills the directory list (left-hand side) with any directories
     * in the current directory.
     *
     * @param curr the directory currently being browsed
     */
    private void getContents(final DFSFile curr) {
        try {
            DFSFileInfo[] fileInfoArray = curr.listFilesInfo();
            if (fileInfoArray != null) {
                List currentContents = Arrays.asList(fileInfoArray);
                ArrayList directories = new ArrayList(currentContents.size());
                ArrayList files = new ArrayList(currentContents.size());
                for (int i = 0; i < currentContents.size(); i++) {
                    DFSFileInfo transfer = (DFSFileInfo) currentContents.get(i);
                    if (transfer.isDirectory()) {
                        directories.add(transfer);
                    } else {
                        files.add(transfer);
                    }
                }
                names = new ArrayList(directories);
                for (int i = 0; i < files.size(); i++) {
                    names.add(files.get(i));
                }
            } else {
                logger.error("Unexpected error while listing files");
                checkDssAvailable();
            }
        } catch (DFSException ex) {
            logger.debug(ex);
            checkDssAvailable();
        }
    }

    /**
     * Goes directly to the directory specified by the text in the path field.
     *
     * @param goPath the abstract path specified by the text in the path field
     * @throws DFSException if a DFS exception occured
     */
    private void go(final String goPath) throws DFSException {
        DFSFile check = new DFSFile("JFS", goPath);
        if (check.exists() && check.isDirectory()) {
            current = check;
            refresh();
        } else {
            JOptionPane.showMessageDialog(null, "Connection error or directory does not exist", "Exit", JOptionPane.ERROR_MESSAGE);
            refresh();
        }
    }

    /**
     * Opens the relevant directory and repopulates the list with its contents.
     *
     * @param openDir the directory being navigated to
     * @throws DFSException if a DFS exception occured
     */
    private void open(final DFSFile openDir) throws DFSException {
        if (openDir.isDirectory()) {
            current = openDir;
            refresh();
        }
    }

    /**
     * Deletes the relevant file or directory from
     * the distributed storage system.
     * @param delFile the file or directory being deleted
     * @throws DFSException if a DFSException occured
     */
    private void delete(final DFSFile delFile) throws DFSException {
        boolean isDeleted = false;
        if (delFile.isFile()) {
            int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this file?", "Delete File", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.CANCEL_OPTION) {
                return;
            } else {
                isDeleted = deleteRecursive(delFile);
                if (!isDeleted) {
                    JOptionPane.showMessageDialog(this, "File could not be deleted", "Deletion Failed", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } else {
            int opt = JOptionPane.showConfirmDialog(this, "Do you want to delete this directory and its contents?", "Delete Directory", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.CANCEL_OPTION) {
                return;
            } else {
                c.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                try {
                    isDeleted = deleteRecursive(delFile);
                } catch (DFSException e) {
                    if (!isDeleted) {
                        JOptionPane.showMessageDialog(this, "This Directory could not be fully deleted", "Deletion Failed", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        }
        c.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        refresh();
    }

    /**
     * Deletes the relevant directory (and all of its contents)
     * from the distributed storage system.
     *
     * @param del the directory being deleted
     * @return <code>false</code> if the directory could not be removed
     * @throws DFSException if a DFS exception occured
     */
    private boolean deleteRecursive(final DFSFile del) throws DFSException {
        boolean isDeleted = false;
        if (del.isFile()) {
            return del.delete();
        } else {
            DFSFile[] fileArray = del.listFiles();
            if (fileArray == null || fileArray.length <= 0) {
                isDeleted = del.delete();
                if (!isDeleted) {
                    throw new DFSException();
                }
                return isDeleted;
            } else {
                for (int i = 0; i < fileArray.length; i++) {
                    deleteRecursive(fileArray[i]);
                }
            }
        }
        isDeleted = del.delete();
        if (!isDeleted) {
            throw new DFSException();
        }
        return isDeleted;
    }

    /**
     * Navigates up one level in the directory structure.
     * If already in the root directory than nothing happens.
     *
     */
    private void upLevel() {
        String currentPath = current.getPath();
        String currentName = current.getName();
        String upPath = currentPath.substring(0, currentPath.length() - (currentName.length() + 1));
        if (!currentPath.equals(root.getPath())) {
            current = new DFSFile("JFS", upPath);
            refresh();
        }
    }

    /**
     * Goes to the home directory (the root directory in this implementation).
     *
     */
    private void goHome() {
        current = new DFSFile("JFS", root.getPath());
        refresh();
    }

    /**
     * Refreshes the list of DFS Files and Directories.
     *
     */
    protected final void refresh() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                populateList(current);
            }
        });
    }

    /**
     * Creates a new directory inside the current directory.
     *
     * @throws DFSException if a DFSException occured
     */
    private void createDirectory() throws DFSException {
        boolean created = false;
        String name = JOptionPane.showInputDialog("Enter name of directory");
        if (name != null && !name.equals("")) {
            DFSFile newDir = new DFSFile("JFS", current.getPath() + sep + name);
            created = newDir.mkdir();
            if (!created) {
                JOptionPane.showMessageDialog(this, "Connection error or invalid directory name", "Directory Not Created", JOptionPane.ERROR_MESSAGE);
            }
            refresh();
        }
    }

    /**
     * Changes the name of the specified file to that inputted by the user.
     *
     * @param renamefile the file being renamed
     * @throws DFSException if a DFS exception occured
     */
    private void renameFile(final DFSFile renamefile) throws DFSException {
        String newName = JOptionPane.showInputDialog("Enter New Name for File", renamefile.getName());
        DFSFile newNameFile = new DFSFile("JFS", current.getPath() + sep + newName);
        if (!renamefile.renameTo(newNameFile)) {
            JOptionPane.showMessageDialog(this, "Unable to rename file", "Error Renaming File", JOptionPane.ERROR_MESSAGE);
        }
        refresh();
    }

    /**
     * Displays properties (path, type, name and length)
     * for a selected file or directory.
     *
     * @param propFile the file that properties are being displayed for
     */
    private void displayProperties(final DFSFileInfo propFile) {
        String fPath = propFile.getPath();
        String fType = "";
        if (propFile.isDirectory()) {
            fType = "Directory";
        } else {
            fType = "File";
        }
        String fName = propFile.getName();
        long size = 0;
        String fSize = "";
        if (propFile.isFile()) {
            size = propFile.length();
            if (size < 1024) {
                fSize = size + " Bytes";
            } else if (size < 1000000) {
                fSize = (size / 1000) + " Kb";
            } else {
                fSize = (size / 1000000) + " Mb";
            }
        } else {
            try {
                size = new DFSFile("JFS", propFile.getPath()).directoryInfo().getNumBytes();
                if (size < 1024) {
                    fSize = size + " Bytes";
                } else if (size < 1048576) {
                    fSize = (size / 1024) + " Kb";
                } else {
                    fSize = (size / 1048576) + " Mb";
                }
            } catch (IOException e) {
                logger.debug(e);
            }
        }
        JOptionPane.showMessageDialog(null, "Path : " + fPath + "\n" + "Type : " + fType + "\n" + "Name : " + fName + "\n" + "Size : " + fSize + "\n", "File Properties", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Closes the input and output streams for a particular operation.
     *
     * @param is the input stream
     * @param os the output stream
     */
    protected final void closeStreams(final InputStream is, final OutputStream os) {
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException e) {
            logger.debug(e);
        }
        try {
            if (os != null) {
                os.close();
            }
        } catch (IOException e) {
            logger.debug(e);
        }
    }

    /**
     * Checks if a DFS service instance is available.
     */
    protected final void checkDssAvailable() {
        new DFSLookupDialog(this, current);
    }

    /**
     * Main method executes DFSExplorer and also handles
     * the window closing event.
     *
     * @param args any arguments passed into the method
     */
    public static void main(final String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    DFSExplorer app = new DFSExplorer();
                    app.addWindowListener(new WindowAdapter() {

                        public void windowClosing(final WindowEvent e) {
                            System.exit(0);
                        }
                    });
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        });
    }
}
