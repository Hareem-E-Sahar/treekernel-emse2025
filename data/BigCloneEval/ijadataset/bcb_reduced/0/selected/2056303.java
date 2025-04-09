package alx.library;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import alx.library.actions.*;
import alx.library.gui.*;
import alx.library.tasks.*;

public class MainWindow extends JFrame implements ActionListener, DocumentListener {

    private static final Insets INSETS_4 = new Insets(4, 4, 4, 4);

    private static final String WINDOW_TITLE = "alx library v. 1.4";

    private static final String RSRC_HELP = "queryparsersyntax.html";

    private static final String RSRC_ABOUT = "about.html";

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(MainWindow.class.getName());

    public static final int TABLE_COLUMN_FILE = 0;

    public static final int TABLE_COLUMN_TYPE = 1;

    public static final int TABLE_COLUMN_SCORE = 2;

    public static final int TABLE_COLUMN_SIZE = 3;

    public static final int TABLE_COLUMN_LAST_MODIFIED = 4;

    private JPanel jContentPane = null;

    private JPanel topPanel = null;

    private JPanel bottomPanel = null;

    private JTextField searchTextField = null;

    private JButton searchButton = null;

    private JButton clearButton = null;

    private JScrollPane jScrollPane = null;

    private JTable mainTable = null;

    private JTextField statusTextField = null;

    private SearchResultsTableModel model = new SearchResultsTableModel();

    private JMenuBar menu;

    private JPopupMenu popup;

    private JMenu fileMenu;

    private JMenu actionsMenu;

    private JMenu toolsMenu;

    private JMenu helpMenu;

    private ExitAction actionExit;

    private SettingsAction actionSettings;

    private JMenu toolsIndexMenu;

    private IndexAllAction actionIndexAll;

    private IndexChangedAction actionIndexChanged;

    private IndexDeleteAction actionIndexDelete;

    private IndexInfoAction actionIndexInfo;

    private HelpAction actionHelp;

    private WebsiteAction actionWebsite;

    private AboutAction actionAbout;

    private OpenDocumentAction actionOpenDocument;

    private OpenEnclosingFolderAction actionOpenEnclosingFolder;

    private IndexCleanupAction actionIndexCleanup;

    CancelIndexingAction actionCancelIndexing;

    private JButton queryHelpButton;

    private HtmlDialog helpDialog;

    private HtmlDialog aboutDialog;

    private final ThreadPoolExecutor indexOpsExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(10));

    private final ThreadPoolExecutor guiOpsExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(10));

    private IndexingPanel indexingPanel;

    private SortedSet<SearchResultItem> results = new TreeSet<SearchResultItem>();

    private Cursor prevCursor;

    /**
	 * This is the default constructor
	 */
    public MainWindow() {
        super();
        initialize();
        log.fine("Main window inited ok");
        if (!Settings.getInstance().isReadOnly()) startupMaintenanceTasks();
    }

    public Future<?> executeAsyncIndexTask(Runnable task) {
        return indexOpsExecutor.submit(task);
    }

    private void startupMaintenanceTasks() {
        indexOpsExecutor.execute(new CleanupIndexTask());
        indexOpsExecutor.execute(new CommitTask());
    }

    /**
	 * This method initializes this
	 * 
	 * @return void
	 */
    private void initialize() {
        this.setSize(800, 400);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setTitle(WINDOW_TITLE);
        setupPopupMenu();
        this.setJMenuBar(getMenu());
        this.setContentPane(getJContentPane());
        setStatusText("Documents in library: " + IndexResources.getDocsNumber());
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent evt) {
                shutdown();
            }
        });
        GUIUtils.restorePosition(this);
    }

    private void setupPopupMenu() {
        if (popup == null) {
            popup = new JPopupMenu();
            actionOpenDocument = new OpenDocumentAction(this);
            popup.add(actionOpenDocument);
            actionOpenEnclosingFolder = new OpenEnclosingFolderAction(this);
            popup.add(actionOpenEnclosingFolder);
        }
    }

    private JMenuBar getMenu() {
        if (menu == null) {
            menu = new JMenuBar();
            fileMenu = new JMenu("File");
            actionSettings = new SettingsAction(this);
            fileMenu.add(actionSettings);
            fileMenu.addSeparator();
            actionExit = new ExitAction(this);
            fileMenu.add(actionExit).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
            menu.add(fileMenu);
            actionsMenu = new JMenu("Actions");
            actionsMenu.add(actionOpenDocument);
            actionsMenu.add(actionOpenEnclosingFolder);
            menu.add(actionsMenu);
            toolsMenu = new JMenu("Tools");
            toolsIndexMenu = new JMenu("Index");
            actionIndexAll = new IndexAllAction(this);
            toolsIndexMenu.add(actionIndexAll);
            actionIndexChanged = new IndexChangedAction(this);
            toolsIndexMenu.add(actionIndexChanged);
            actionCancelIndexing = new CancelIndexingAction(this);
            toolsIndexMenu.add(actionCancelIndexing);
            actionIndexDelete = new IndexDeleteAction(this);
            toolsIndexMenu.add(actionIndexDelete);
            actionIndexInfo = new IndexInfoAction(this);
            toolsIndexMenu.add(actionIndexInfo);
            actionIndexCleanup = new IndexCleanupAction(this);
            toolsIndexMenu.add(actionIndexCleanup);
            toolsMenu.add(toolsIndexMenu);
            menu.add(toolsMenu);
            helpMenu = new JMenu("Help");
            actionHelp = new HelpAction(this);
            helpMenu.add(actionHelp).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
            actionWebsite = new WebsiteAction();
            helpMenu.add(actionWebsite);
            actionAbout = new AboutAction(this);
            helpMenu.add(actionAbout);
            menu.add(helpMenu);
        }
        return menu;
    }

    /**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getTopPanel(), BorderLayout.NORTH);
            jContentPane.add(getBottomPanel(), BorderLayout.SOUTH);
            jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
	 * This method initializes topPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
    private JPanel getTopPanel() {
        if (topPanel == null) {
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 3;
            gridBagConstraints3.insets = INSETS_4;
            gridBagConstraints3.gridy = 0;
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 2;
            gridBagConstraints2.insets = INSETS_4;
            gridBagConstraints2.gridy = 0;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 1;
            gridBagConstraints1.insets = INSETS_4;
            gridBagConstraints1.gridy = 0;
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.insets = INSETS_4;
            gridBagConstraints.weightx = 1.0;
            topPanel = new JPanel();
            topPanel.setLayout(new GridBagLayout());
            topPanel.add(getSearchTextField(), gridBagConstraints);
            topPanel.add(getSearchButton(), gridBagConstraints1);
            topPanel.add(getClearButton(), gridBagConstraints2);
            topPanel.add(getQueryHelpButton(), gridBagConstraints3);
        }
        return topPanel;
    }

    /**
	 * This method initializes bottomPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
    private JPanel getBottomPanel() {
        if (bottomPanel == null) {
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.fill = GridBagConstraints.BOTH;
            gridBagConstraints3.insets = INSETS_4;
            gridBagConstraints3.weightx = 1.0;
            gridBagConstraints3.gridx = 0;
            gridBagConstraints3.gridy = 0;
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.fill = GridBagConstraints.BOTH;
            gridBagConstraints4.insets = INSETS_4;
            gridBagConstraints4.gridx = 1;
            gridBagConstraints4.gridy = 0;
            bottomPanel = new JPanel();
            bottomPanel.setLayout(new GridBagLayout());
            bottomPanel.add(getStatusTextField(), gridBagConstraints3);
            bottomPanel.add(getIndexingPanel(), gridBagConstraints4);
        }
        return bottomPanel;
    }

    public IndexingPanel getIndexingPanel() {
        if (indexingPanel == null) {
            indexingPanel = new IndexingPanel(this);
        }
        return indexingPanel;
    }

    /**
	 * This method initializes searchTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
    private JTextField getSearchTextField() {
        if (searchTextField == null) {
            searchTextField = new JTextField();
            searchTextField.addActionListener(this);
            searchTextField.getDocument().addDocumentListener(this);
        }
        return searchTextField;
    }

    /**
	 * This method initializes searchButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
    private JButton getSearchButton() {
        if (searchButton == null) {
            searchButton = new JButton();
            searchButton.setText("Search");
            searchButton.addActionListener(this);
            searchButton.setEnabled(false);
        }
        return searchButton;
    }

    /**
	 * This method initializes indexButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
    private JButton getClearButton() {
        if (clearButton == null) {
            clearButton = new JButton();
            clearButton.setText("Clear");
            clearButton.addActionListener(this);
        }
        return clearButton;
    }

    private JButton getQueryHelpButton() {
        if (queryHelpButton == null) {
            queryHelpButton = new JButton();
            queryHelpButton.setText("Help");
            queryHelpButton.addActionListener(this);
        }
        return queryHelpButton;
    }

    /**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTable());
        }
        return jScrollPane;
    }

    /**
	 * This method initializes jTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
    private JTable getJTable() {
        if (mainTable == null) {
            mainTable = new JTable(model);
            TableRowSorter<SearchResultsTableModel> sorter = new TableRowSorter<SearchResultsTableModel>(model);
            sorter.setComparator(TABLE_COLUMN_SIZE, new Comparator<Object>() {

                @Override
                public int compare(Object o1, Object o2) {
                    if (o1 != null && o2 != null) {
                        if (o1 instanceof Long && o2 instanceof Long) {
                            return ((Long) o1).compareTo((Long) o2);
                        } else {
                            return o1.toString().compareTo(o2.toString());
                        }
                    } else if (o1 != null && o2 == null) {
                        return 1;
                    } else if (o1 == null && o2 != null) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
            mainTable.setRowSorter(sorter);
            mainTable.getColumnModel().getColumn(TABLE_COLUMN_TYPE).setPreferredWidth(64);
            mainTable.getColumnModel().getColumn(TABLE_COLUMN_TYPE).setMaxWidth(128);
            mainTable.getColumnModel().getColumn(TABLE_COLUMN_SCORE).setPreferredWidth(100);
            mainTable.getColumnModel().getColumn(TABLE_COLUMN_SCORE).setMaxWidth(128);
            mainTable.getColumnModel().getColumn(TABLE_COLUMN_SIZE).setPreferredWidth(100);
            mainTable.getColumnModel().getColumn(TABLE_COLUMN_SIZE).setMaxWidth(128);
            mainTable.getColumnModel().getColumn(TABLE_COLUMN_LAST_MODIFIED).setPreferredWidth(128);
            mainTable.getColumnModel().getColumn(TABLE_COLUMN_LAST_MODIFIED).setMaxWidth(128);
            mainTable.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() > 1) {
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                doOpen();
                            }
                        });
                    }
                }

                private void maybeShowPopup(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        int row = mainTable.getRowSorter().convertRowIndexToView(mainTable.getRowSorter().convertRowIndexToModel(mainTable.rowAtPoint(e.getPoint())));
                        mainTable.getSelectionModel().setSelectionInterval(row, row);
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    maybeShowPopup(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    maybeShowPopup(e);
                }
            });
            mainTable.getColumnModel().getColumn(TABLE_COLUMN_FILE).setCellRenderer(new FilenameCellRenderer());
            mainTable.getColumnModel().getColumn(TABLE_COLUMN_SCORE).setCellRenderer(new ScoreCellRenderer());
            mainTable.getColumnModel().getColumn(TABLE_COLUMN_SIZE).setCellRenderer(new FilesizeCellRenderer());
            mainTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            mainTable.getSelectionModel().addListSelectionListener(actionOpenDocument);
            mainTable.getSelectionModel().addListSelectionListener(actionOpenEnclosingFolder);
        }
        return mainTable;
    }

    public synchronized void doOpen() {
        setFileActionsEnabled(false);
        int row = mainTable.getRowSorter().convertRowIndexToModel(mainTable.getSelectedRow());
        if (row == -1) return;
        final String path = model.getPath(row);
        final String msg = "Opening " + path + "...";
        log.info(msg);
        statusTextField.setText(msg);
        guiOpsExecutor.execute(new OpenTask(this, path));
    }

    /**
	 * This method initializes statusTextField
	 * 
	 * @return javax.swing.JTextField
	 */
    private JTextField getStatusTextField() {
        if (statusTextField == null) {
            statusTextField = new JTextField();
            statusTextField.setEditable(false);
        }
        return statusTextField;
    }

    public void setStatusText(String text) {
        if (text == null) text = "";
        statusTextField.setText(text);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == searchButton || e.getSource() == searchTextField) {
            if (searchTextField.getText().length() > 0) {
                setSearchRunning(true);
                guiOpsExecutor.execute(new DoSearchTask(this));
            }
        } else if (e.getSource() == clearButton) {
            doClear();
        } else if (e.getSource() == queryHelpButton) {
            doShowQueryHelp();
        }
    }

    public void doShowQueryHelp() {
        if (helpDialog == null) {
            helpDialog = new HtmlDialog(this, "Help", RSRC_HELP, true);
        }
        helpDialog.setVisible(true);
    }

    private void doClear() {
        searchTextField.setText("");
        model.clear();
        setStatusText("");
    }

    public void doRebuildAll() {
        doDeleteIndex();
        doIndex(false);
    }

    public void doDeleteIndex() {
        log.fine("Deleting index");
        Engine.deleteIndex();
        setStatusText("Index deleted");
    }

    protected void doIndex(boolean incremental) {
        log.fine("Building index");
        indexingPanel.setIncremental(incremental);
        indexingPanel.execute();
    }

    /**
	 * Called from async executor.
	 */
    public void doSearch() {
        try {
            model.clear();
            results = Engine.search(searchTextField.getText());
            model.populate(results);
        } catch (IndexNotExistsException e) {
            int response = JOptionPane.showConfirmDialog(this, e.getMessage() + "\nWould you like to create it now?", "Error", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) doIndex(false);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error", e);
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    jScrollPane.getViewport().setViewPosition(new Point(0, 0));
                    searchTextField.selectAll();
                    setStatusText("Documents found: " + (results != null ? results.size() : 0));
                    setSearchRunning(false);
                }
            });
        }
    }

    private void setSearchRunning(boolean b) {
        searchButton.setEnabled(!b);
        searchTextField.setEnabled(!b);
        clearButton.setEnabled(!b);
        if (b) {
            prevCursor = getCursor();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            setCursor(prevCursor);
        }
    }

    public void changedUpdate(DocumentEvent e) {
        searchButton.setEnabled(e.getDocument().getLength() > 0);
    }

    public void insertUpdate(DocumentEvent e) {
        searchButton.setEnabled(e.getDocument().getLength() > 0);
    }

    public void removeUpdate(DocumentEvent e) {
        searchButton.setEnabled(e.getDocument().getLength() > 0);
    }

    public void doRebuildChanged() {
        doIndex(true);
    }

    public synchronized void doOpenEnclosingFolder() {
        setFileActionsEnabled(false);
        int row = mainTable.getRowSorter().convertRowIndexToModel(mainTable.getSelectedRow());
        if (row == -1) return;
        final String path = model.getPath(row);
        final String msg = "Opening enclosing folder for " + path + "...";
        statusTextField.setText(msg);
        log.info(msg);
        guiOpsExecutor.execute(new OpenEnclosingFolderTask(this, path));
    }

    public void setFileActionsEnabled(boolean b) {
        actionOpenDocument.setEnabled(b);
        actionOpenEnclosingFolder.setEnabled(b);
    }

    @Override
    public void dispose() {
        GUIUtils.savePosition(this);
        super.dispose();
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (Settings.getInstance().getRootDir() == null) {
            actionSettings.actionPerformed(null);
        }
        if (Settings.getInstance().getRootDir() == null) {
            JOptionPane.showMessageDialog(this, "Settings should be specified", "Error", JOptionPane.ERROR_MESSAGE);
            this.dispose();
        }
    }

    public void doShowAbout() {
        if (aboutDialog == null) {
            aboutDialog = new HtmlDialog(this, "About", RSRC_ABOUT, 560, 420);
            GUIUtils.centerDialog(aboutDialog, this);
        }
        aboutDialog.setVisible(true);
    }

    public static void openDesktop(final File fileToOpen) {
        if (fileToOpen != null && fileToOpen.exists()) {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.open(fileToOpen);
                }
            } catch (IOException e) {
                log.log(Level.WARNING, "Error", e);
            }
        }
    }

    public void shutdown() {
        log.info("Initiating shutdown sequence");
        indexOpsExecutor.shutdownNow();
        indexingPanel.shutdown();
        try {
            GUIUtils.savePosition(MainWindow.this);
        } catch (Throwable e) {
            log.log(Level.WARNING, "Error", e);
        }
        this.dispose();
        log.info("Shutdown sequence completed");
        System.exit(0);
    }
}
