package com.digitprop.tonic;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.*;
import javax.swing.text.Position;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;
import sun.awt.shell.ShellFolder;

public class FileChooserUI extends BasicFileChooserUI {

    /**	Caches icons used for the file chooser */
    private static final Hashtable iconCache = new Hashtable();

    private JPanel centerPanel;

    private JLabel lookInLabel;

    private JComboBox directoryComboBox;

    private DirectoryComboBoxModel directoryComboBoxModel;

    private Action directoryComboBoxAction = new DirectoryComboBoxAction();

    private FilterComboBoxModel filterComboBoxModel;

    private JTextField fileNameTextField;

    private JToggleButton listViewButton;

    private JToggleButton detailsViewButton;

    private JPanel listViewPanel;

    private JPanel detailsViewPanel;

    private JPanel currentViewPanel;

    private FocusListener editorFocusListener = new FocusAdapter() {

        public void focusLost(FocusEvent e) {
            if (!e.isTemporary()) {
                applyEdit();
            }
        }
    };

    private boolean useShellFolder;

    private ListSelectionModel listSelectionModel;

    private JList list;

    private JTable detailsTable;

    private JButton approveButton;

    private JButton cancelButton;

    private JPanel buttonPanel;

    private JPanel bottomPanel;

    private JComboBox filterComboBox;

    private static final Dimension hstrut5 = new Dimension(5, 1);

    private static final Dimension hstrut11 = new Dimension(11, 1);

    private static final Dimension vstrut5 = new Dimension(1, 5);

    private static final Insets shrinkwrap = new Insets(0, 0, 0, 0);

    private static int PREF_WIDTH = 500;

    private static int PREF_HEIGHT = 326;

    private static Dimension PREF_SIZE = new Dimension(PREF_WIDTH, PREF_HEIGHT);

    private static int MIN_WIDTH = 500;

    private static int MIN_HEIGHT = 326;

    private static Dimension MIN_SIZE = new Dimension(MIN_WIDTH, MIN_HEIGHT);

    private static int LIST_PREF_WIDTH = 405;

    private static int LIST_PREF_HEIGHT = 135;

    private static Dimension LIST_PREF_SIZE = new Dimension(LIST_PREF_WIDTH, LIST_PREF_HEIGHT);

    private static final int COLUMN_FILENAME = 0;

    private static final int COLUMN_FILESIZE = 1;

    private static final int COLUMN_FILETYPE = 2;

    private static final int COLUMN_FILEDATE = 3;

    private static final int COLUMN_FILEATTR = 4;

    private static final int COLUMN_COLCOUNT = 5;

    private int[] COLUMN_WIDTHS = { 150, 75, 130, 130, 40 };

    private int lookInLabelMnemonic = 0;

    private String lookInLabelText = null;

    private String saveInLabelText = null;

    private int fileNameLabelMnemonic = 0;

    private String fileNameLabelText = null;

    private int filesOfTypeLabelMnemonic = 0;

    private String filesOfTypeLabelText = null;

    private String upFolderToolTipText = null;

    private String upFolderAccessibleName = null;

    private String homeFolderToolTipText = null;

    private String homeFolderAccessibleName = null;

    private String newFolderToolTipText = null;

    private String newFolderAccessibleName = null;

    private String listViewButtonToolTipText = null;

    private String listViewButtonAccessibleName = null;

    private String detailsViewButtonToolTipText = null;

    private String detailsViewButtonAccessibleName = null;

    private String fileNameHeaderText = null;

    private String fileSizeHeaderText = null;

    private String fileTypeHeaderText = null;

    private String fileDateHeaderText = null;

    private String fileAttrHeaderText = null;

    /**	Creates and returns a UI delegate for the 
	 * 	specified component.
	 */
    public static ComponentUI createUI(JComponent c) {
        return new FileChooserUI((JFileChooser) c);
    }

    /**	Creates an instance for the specified JFileChooser */
    public FileChooserUI(JFileChooser filechooser) {
        super(filechooser);
    }

    /**	Installs the UI delegate for the specified component */
    public void installUI(JComponent c) {
        super.installUI(c);
    }

    /**	Uninstalls the UI delegate for the specified JFileChooser */
    public void uninstallComponents(JFileChooser fc) {
        fc.removeAll();
        bottomPanel = null;
        buttonPanel = null;
    }

    /**	Installs the components for the specified JFileChooser */
    public void installComponents(JFileChooser fc) {
        FileSystemView fsv = fc.getFileSystemView();
        fc.setBorder(new EmptyBorder(12, 12, 11, 11));
        fc.setLayout(new BorderLayout(0, 11));
        JPanel topPanel = new JPanel(new BorderLayout(11, 0));
        JPanel topButtonPanel = new JPanel();
        topButtonPanel.setLayout(new BoxLayout(topButtonPanel, BoxLayout.LINE_AXIS));
        topPanel.add(topButtonPanel, BorderLayout.AFTER_LINE_ENDS);
        fc.add(topPanel, BorderLayout.NORTH);
        lookInLabel = new JLabel(lookInLabelText);
        lookInLabel.setDisplayedMnemonic(lookInLabelMnemonic);
        topPanel.add(lookInLabel, BorderLayout.BEFORE_LINE_BEGINS);
        directoryComboBox = new JComboBox();
        directoryComboBox.putClientProperty("JComboBox.lightweightKeyboardNavigation", "Lightweight");
        lookInLabel.setLabelFor(directoryComboBox);
        directoryComboBoxModel = createDirectoryComboBoxModel(fc);
        directoryComboBox.setModel(directoryComboBoxModel);
        directoryComboBox.addActionListener(directoryComboBoxAction);
        directoryComboBox.setRenderer(createDirectoryComboBoxRenderer(fc));
        directoryComboBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        directoryComboBox.setAlignmentY(JComponent.TOP_ALIGNMENT);
        directoryComboBox.setMaximumRowCount(8);
        topPanel.add(directoryComboBox, BorderLayout.CENTER);
        JButton upFolderButton = new JButton(getChangeToParentDirectoryAction());
        upFolderButton.setText(null);
        upFolderButton.setIcon(upFolderIcon);
        upFolderButton.setDisabledIcon(new GrayedIcon(upFolderIcon));
        upFolderButton.setToolTipText(upFolderToolTipText);
        upFolderButton.getAccessibleContext().setAccessibleName(upFolderAccessibleName);
        upFolderButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        upFolderButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        upFolderButton.setMargin(shrinkwrap);
        topButtonPanel.add(upFolderButton);
        topButtonPanel.add(Box.createRigidArea(hstrut5));
        File homeDir = fsv.getHomeDirectory();
        String toolTipText = homeFolderToolTipText;
        if (fsv.isRoot(homeDir)) {
            toolTipText = getFileView(fc).getName(homeDir);
        }
        JButton b = new JButton(homeFolderIcon);
        b.setDisabledIcon(new GrayedIcon(homeFolderIcon));
        b.setToolTipText(toolTipText);
        b.getAccessibleContext().setAccessibleName(homeFolderAccessibleName);
        b.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        b.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        b.setMargin(shrinkwrap);
        b.addActionListener(getGoHomeAction());
        topButtonPanel.add(b);
        topButtonPanel.add(Box.createRigidArea(hstrut5));
        b = new JButton(getNewFolderAction());
        b.setText(null);
        b.setIcon(newFolderIcon);
        b.setDisabledIcon(new GrayedIcon(newFolderIcon));
        b.setToolTipText(newFolderToolTipText);
        b.getAccessibleContext().setAccessibleName(newFolderAccessibleName);
        b.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        b.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        b.setMargin(shrinkwrap);
        topButtonPanel.add(b);
        topButtonPanel.add(Box.createRigidArea(hstrut5));
        ButtonGroup viewButtonGroup = new ButtonGroup();
        class ViewButtonListener implements ActionListener {

            JFileChooser fc;

            ViewButtonListener(JFileChooser fc) {
                this.fc = fc;
            }

            public void actionPerformed(ActionEvent e) {
                JToggleButton b = (JToggleButton) e.getSource();
                JPanel oldViewPanel = currentViewPanel;
                if (b == detailsViewButton) {
                    if (detailsViewPanel == null) {
                        detailsViewPanel = createDetailsView(fc);
                        detailsViewPanel.setPreferredSize(LIST_PREF_SIZE);
                    }
                    currentViewPanel = detailsViewPanel;
                } else {
                    currentViewPanel = listViewPanel;
                }
                if (currentViewPanel != oldViewPanel) {
                    centerPanel.remove(oldViewPanel);
                    centerPanel.add(currentViewPanel, BorderLayout.CENTER);
                    centerPanel.revalidate();
                    centerPanel.repaint();
                }
            }
        }
        ViewButtonListener viewButtonListener = new ViewButtonListener(fc);
        listViewButton = new JToggleButton(listViewIcon);
        listViewButton.setToolTipText(listViewButtonToolTipText);
        listViewButton.getAccessibleContext().setAccessibleName(listViewButtonAccessibleName);
        listViewButton.setSelected(true);
        listViewButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        listViewButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        listViewButton.setMargin(shrinkwrap);
        listViewButton.addActionListener(viewButtonListener);
        topButtonPanel.add(listViewButton);
        viewButtonGroup.add(listViewButton);
        topButtonPanel.add(Box.createHorizontalStrut(2));
        detailsViewButton = new JToggleButton(detailsViewIcon);
        detailsViewButton.setToolTipText(detailsViewButtonToolTipText);
        detailsViewButton.getAccessibleContext().setAccessibleName(detailsViewButtonAccessibleName);
        detailsViewButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        detailsViewButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        detailsViewButton.setMargin(shrinkwrap);
        detailsViewButton.addActionListener(viewButtonListener);
        topButtonPanel.add(detailsViewButton);
        viewButtonGroup.add(detailsViewButton);
        {
            useShellFolder = false;
            File[] roots = fsv.getRoots();
            if (roots != null && roots.length == 1) {
                File[] cbFolders = (File[]) ShellFolder.get("fileChooserComboBoxFolders");
                if (cbFolders != null && cbFolders.length > 0 && roots[0] == cbFolders[0]) {
                    useShellFolder = true;
                }
            }
        }
        centerPanel = new JPanel(new BorderLayout());
        listViewPanel = createList(fc);
        listSelectionModel = list.getSelectionModel();
        listViewPanel.setPreferredSize(LIST_PREF_SIZE);
        centerPanel.add(listViewPanel, BorderLayout.CENTER);
        currentViewPanel = listViewPanel;
        centerPanel.add(getAccessoryPanel(), BorderLayout.AFTER_LINE_ENDS);
        JComponent accessory = fc.getAccessory();
        if (accessory != null) {
            getAccessoryPanel().add(accessory);
        }
        fc.add(centerPanel, BorderLayout.CENTER);
        JPanel bottomPanel = getBottomPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        fc.add(bottomPanel, BorderLayout.SOUTH);
        JPanel fileNamePanel = new JPanel();
        fileNamePanel.setLayout(new BoxLayout(fileNamePanel, BoxLayout.LINE_AXIS));
        bottomPanel.add(fileNamePanel);
        bottomPanel.add(Box.createRigidArea(vstrut5));
        AlignedLabel fileNameLabel = new AlignedLabel(fileNameLabelText);
        fileNameLabel.setDisplayedMnemonic(fileNameLabelMnemonic);
        fileNamePanel.add(fileNameLabel);
        fileNameTextField = new JTextField() {

            public Dimension getMaximumSize() {
                return new Dimension(Short.MAX_VALUE, super.getPreferredSize().height);
            }
        };
        fileNamePanel.add(fileNameTextField);
        fileNameLabel.setLabelFor(fileNameTextField);
        fileNameTextField.addFocusListener(new FocusAdapter() {

            public void focusGained(FocusEvent e) {
                if (!getFileChooser().isMultiSelectionEnabled()) {
                    listSelectionModel.clearSelection();
                }
            }
        });
        if (fc.isMultiSelectionEnabled()) {
            setFileName(fileNameString(fc.getSelectedFiles()));
        } else {
            setFileName(fileNameString(fc.getSelectedFile()));
        }
        JPanel filesOfTypePanel = new JPanel();
        filesOfTypePanel.setLayout(new BoxLayout(filesOfTypePanel, BoxLayout.LINE_AXIS));
        bottomPanel.add(filesOfTypePanel);
        AlignedLabel filesOfTypeLabel = new AlignedLabel(filesOfTypeLabelText);
        filesOfTypeLabel.setDisplayedMnemonic(filesOfTypeLabelMnemonic);
        filesOfTypePanel.add(filesOfTypeLabel);
        filterComboBoxModel = createFilterComboBoxModel();
        fc.addPropertyChangeListener(filterComboBoxModel);
        filterComboBox = new JComboBox(filterComboBoxModel);
        filesOfTypeLabel.setLabelFor(filterComboBox);
        filterComboBox.setRenderer(createFilterComboBoxRenderer());
        filesOfTypePanel.add(filterComboBox);
        getButtonPanel().setLayout(new ButtonAreaLayout());
        approveButton = new JButton(getApproveButtonText(fc));
        approveButton.addActionListener(getApproveSelectionAction());
        approveButton.setToolTipText(getApproveButtonToolTipText(fc));
        getButtonPanel().add(approveButton);
        cancelButton = new JButton(cancelButtonText);
        cancelButton.setToolTipText(cancelButtonToolTipText);
        cancelButton.addActionListener(getCancelSelectionAction());
        getButtonPanel().add(cancelButton);
        if (fc.getControlButtonsAreShown()) {
            addControlButtons();
        }
        groupLabels(new AlignedLabel[] { fileNameLabel, filesOfTypeLabel });
    }

    protected ImageIcon getDisabledIcon(Icon icon, Component c) {
        Image img = c.createImage(icon.getIconWidth(), icon.getIconHeight());
        Graphics g = img.getGraphics();
        icon.paintIcon(c, g, 0, 0);
        g.dispose();
        ImageIcon disabledIcon = new ImageIcon(GrayFilter.createDisabledImage(img));
        return disabledIcon;
    }

    protected JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel();
        }
        return buttonPanel;
    }

    protected JPanel getBottomPanel() {
        if (bottomPanel == null) {
            bottomPanel = new JPanel();
        }
        return bottomPanel;
    }

    protected void installStrings(JFileChooser fc) {
        super.installStrings(fc);
        Locale l = fc.getLocale();
        lookInLabelMnemonic = UIManager.getInt("FileChooser.lookInLabelMnemonic");
        lookInLabelText = UIManager.getString("FileChooser.lookInLabelText", l);
        saveInLabelText = UIManager.getString("FileChooser.saveInLabelText", l);
        fileNameLabelMnemonic = UIManager.getInt("FileChooser.fileNameLabelMnemonic");
        fileNameLabelText = UIManager.getString("FileChooser.fileNameLabelText", l);
        filesOfTypeLabelMnemonic = UIManager.getInt("FileChooser.filesOfTypeLabelMnemonic");
        filesOfTypeLabelText = UIManager.getString("FileChooser.filesOfTypeLabelText", l);
        upFolderToolTipText = UIManager.getString("FileChooser.upFolderToolTipText", l);
        upFolderAccessibleName = UIManager.getString("FileChooser.upFolderAccessibleName", l);
        homeFolderToolTipText = UIManager.getString("FileChooser.homeFolderToolTipText", l);
        homeFolderAccessibleName = UIManager.getString("FileChooser.homeFolderAccessibleName", l);
        newFolderToolTipText = UIManager.getString("FileChooser.newFolderToolTipText", l);
        newFolderAccessibleName = UIManager.getString("FileChooser.newFolderAccessibleName", l);
        listViewButtonToolTipText = UIManager.getString("FileChooser.listViewButtonToolTipText", l);
        listViewButtonAccessibleName = UIManager.getString("FileChooser.listViewButtonAccessibleName", l);
        detailsViewButtonToolTipText = UIManager.getString("FileChooser.detailsViewButtonToolTipText", l);
        detailsViewButtonAccessibleName = UIManager.getString("FileChooser.detailsViewButtonAccessibleName", l);
        fileNameHeaderText = UIManager.getString("FileChooser.fileNameHeaderText", l);
        fileSizeHeaderText = UIManager.getString("FileChooser.fileSizeHeaderText", l);
        fileTypeHeaderText = UIManager.getString("FileChooser.fileTypeHeaderText", l);
        fileDateHeaderText = UIManager.getString("FileChooser.fileDateHeaderText", l);
        fileAttrHeaderText = UIManager.getString("FileChooser.fileAttrHeaderText", l);
    }

    protected void installListeners(JFileChooser fc) {
        super.installListeners(fc);
        ActionMap actionMap = getMyActionMap();
        SwingUtilities.replaceUIActionMap(fc, actionMap);
    }

    protected ActionMap getMyActionMap() {
        return createMyActionMap();
    }

    protected ActionMap createMyActionMap() {
        AbstractAction escAction = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (editing) {
                    cancelEdit();
                } else {
                    getFileChooser().cancelSelection();
                }
            }

            public boolean isEnabled() {
                return getFileChooser().isEnabled();
            }
        };
        ActionMap map = new ActionMapUIResource();
        map.put("approveSelection", getApproveSelectionAction());
        map.put("cancelSelection", escAction);
        map.put("Go Up", getChangeToParentDirectoryAction());
        return map;
    }

    protected JPanel createList(JFileChooser fc) {
        JPanel p = new JPanel(new BorderLayout());
        final JFileChooser fileChooser = fc;
        list = new JList() {

            public int getNextMatch(String prefix, int startIndex, Position.Bias bias) {
                ListModel model = getModel();
                int max = model.getSize();
                if (prefix == null || startIndex < 0 || startIndex >= max) {
                    throw new IllegalArgumentException();
                }
                boolean backwards = (bias == Position.Bias.Backward);
                for (int i = startIndex; backwards ? i >= 0 : i < max; i += (backwards ? -1 : 1)) {
                    String filename = fileChooser.getName((File) model.getElementAt(i));
                    if (filename.regionMatches(true, 0, prefix, 0, prefix.length())) {
                        return i;
                    }
                }
                return -1;
            }
        };
        list.setCellRenderer(new FileRenderer());
        list.setLayoutOrientation(JList.VERTICAL_WRAP);
        list.setVisibleRowCount(-1);
        if (fc.isMultiSelectionEnabled()) {
            list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        } else {
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        list.setModel(getModel());
        list.addListSelectionListener(createListSelectionListener(fc));
        list.addMouseListener(createDoubleClickListener(fc, list));
        list.addMouseListener(createSingleClickListener(fc, list));
        getModel().addListDataListener(new ListDataListener() {

            public void contentsChanged(ListDataEvent e) {
                new DelayedSelectionUpdater();
            }

            public void intervalAdded(ListDataEvent e) {
                new DelayedSelectionUpdater();
            }

            public void intervalRemoved(ListDataEvent e) {
            }
        });
        JScrollPane scrollpane = new JScrollPane(list);
        p.add(scrollpane, BorderLayout.CENTER);
        return p;
    }

    class DetailsTableModel extends AbstractTableModel implements ListDataListener {

        String[] columnNames = { fileNameHeaderText, fileSizeHeaderText, fileTypeHeaderText, fileDateHeaderText, fileAttrHeaderText };

        JFileChooser chooser;

        ListModel listModel;

        DetailsTableModel(JFileChooser fc) {
            this.chooser = fc;
            listModel = getModel();
            listModel.addListDataListener(this);
        }

        public int getRowCount() {
            return listModel.getSize();
        }

        public int getColumnCount() {
            return COLUMN_COLCOUNT;
        }

        public String getColumnName(int column) {
            return columnNames[column];
        }

        public Class getColumnClass(int column) {
            switch(column) {
                case COLUMN_FILENAME:
                    return File.class;
                case COLUMN_FILEDATE:
                    return Date.class;
                default:
                    return super.getColumnClass(column);
            }
        }

        public Object getValueAt(int row, int col) {
            File f = (File) listModel.getElementAt(row);
            switch(col) {
                case COLUMN_FILENAME:
                    return f;
                case COLUMN_FILESIZE:
                    if (!f.exists() || f.isDirectory()) {
                        return null;
                    }
                    long len = f.length() / 1024L;
                    if (len < 1024L) {
                        return ((len == 0L) ? 1L : len) + " KB";
                    } else {
                        len /= 1024L;
                        if (len < 1024L) {
                            return len + " MB";
                        } else {
                            len /= 1024L;
                            return len + " GB";
                        }
                    }
                case COLUMN_FILETYPE:
                    if (!f.exists()) {
                        return null;
                    }
                    return chooser.getFileSystemView().getSystemTypeDescription(f);
                case COLUMN_FILEDATE:
                    if (!f.exists() || chooser.getFileSystemView().isFileSystemRoot(f)) {
                        return null;
                    }
                    long time = f.lastModified();
                    return (time == 0L) ? null : new Date(time);
                case COLUMN_FILEATTR:
                    if (!f.exists() || chooser.getFileSystemView().isFileSystemRoot(f)) {
                        return null;
                    }
                    String attributes = "";
                    if (!f.canWrite()) {
                        attributes += "R";
                    }
                    if (f.isHidden()) {
                        attributes += "H";
                    }
                    return attributes;
            }
            return null;
        }

        public void setValueAt(Object value, int row, int col) {
            if (col == COLUMN_FILENAME) {
                JFileChooser chooser = getFileChooser();
                File f = (File) getValueAt(row, col);
                if (f != null) {
                    String oldDisplayName = chooser.getName(f);
                    String oldFileName = f.getName();
                    String newDisplayName = ((String) value).trim();
                    String newFileName;
                    if (!newDisplayName.equals(oldDisplayName)) {
                        newFileName = newDisplayName;
                        int i1 = oldFileName.length();
                        int i2 = oldDisplayName.length();
                        if (i1 > i2 && oldFileName.charAt(i2) == '.') {
                            newFileName = newDisplayName + oldFileName.substring(i2);
                        }
                        FileSystemView fsv = chooser.getFileSystemView();
                        File f2 = fsv.createFileObject(f.getParentFile(), newFileName);
                        if (FileChooserUI.this.getModel().renameFile(f, f2)) {
                            if (fsv.isParent(chooser.getCurrentDirectory(), f2)) {
                                if (chooser.isMultiSelectionEnabled()) {
                                    chooser.setSelectedFiles(new File[] { f2 });
                                } else {
                                    chooser.setSelectedFile(f2);
                                }
                            } else {
                            }
                        } else {
                        }
                    }
                }
            }
        }

        public boolean isCellEditable(int row, int column) {
            return (column == COLUMN_FILENAME);
        }

        public void contentsChanged(ListDataEvent e) {
            fireTableDataChanged();
        }

        public void intervalAdded(ListDataEvent e) {
            fireTableDataChanged();
        }

        public void intervalRemoved(ListDataEvent e) {
            fireTableDataChanged();
        }
    }

    class DetailsTableCellRenderer extends DefaultTableCellRenderer {

        JFileChooser chooser;

        DateFormat df;

        DetailsTableCellRenderer(JFileChooser chooser) {
            this.chooser = chooser;
            df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, chooser.getLocale());
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (column == COLUMN_FILESIZE || column == COLUMN_FILEATTR) {
                setHorizontalAlignment(SwingConstants.TRAILING);
            } else {
                setHorizontalAlignment(SwingConstants.LEADING);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

        public void setValue(Object value) {
            setIcon(null);
            if (value instanceof File) {
                File file = (File) value;
                String fileName = chooser.getName(file);
                setText(fileName);
                Icon icon = FileChooserUI.getIcon(file, fileIcon, getFileChooser().getFileSystemView());
                setIcon(icon);
            } else if (value instanceof Date) {
                setText((value == null) ? "" : df.format((Date) value));
            } else {
                super.setValue(value);
            }
        }
    }

    protected JPanel createDetailsView(JFileChooser fc) {
        final JFileChooser chooser = fc;
        JPanel p = new JPanel(new BorderLayout());
        DetailsTableModel detailsTableModel = new DetailsTableModel(chooser);
        detailsTable = new JTable(detailsTableModel) {

            protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE && getCellEditor() == null) {
                    chooser.dispatchEvent(e);
                    return true;
                }
                return super.processKeyBinding(ks, e, condition, pressed);
            }
        };
        detailsTable.setComponentOrientation(chooser.getComponentOrientation());
        detailsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        detailsTable.setShowGrid(false);
        detailsTable.setSelectionModel(listSelectionModel);
        detailsTable.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
        TableColumnModel columnModel = detailsTable.getColumnModel();
        TableColumn[] columns = new TableColumn[COLUMN_COLCOUNT];
        for (int i = 0; i < COLUMN_COLCOUNT; i++) {
            columns[i] = columnModel.getColumn(i);
            columns[i].setPreferredWidth(COLUMN_WIDTHS[i]);
        }
        if (!System.getProperty("os.name").startsWith("Windows")) {
            columnModel.removeColumn(columns[COLUMN_FILETYPE]);
            columnModel.removeColumn(columns[COLUMN_FILEATTR]);
        }
        TableCellRenderer cellRenderer = new DetailsTableCellRenderer(chooser);
        detailsTable.setDefaultRenderer(File.class, cellRenderer);
        detailsTable.setDefaultRenderer(Date.class, cellRenderer);
        detailsTable.setDefaultRenderer(Object.class, cellRenderer);
        final JTextField tf = new JTextField();
        tf.addFocusListener(editorFocusListener);
        columns[COLUMN_FILENAME].setCellEditor(new DefaultCellEditor(tf) {

            private long lastClick = 0L;

            public boolean isCellEditable(EventObject e) {
                if (e instanceof MouseEvent) {
                    MouseEvent me = (MouseEvent) e;
                    int index = detailsTable.rowAtPoint(me.getPoint());
                    long clickTime = System.currentTimeMillis();
                    if (clickTime - lastClick > 750 && detailsTable.isRowSelected(index)) {
                        lastClick = clickTime;
                        return true;
                    } else {
                        lastClick = clickTime;
                        return false;
                    }
                }
                return super.isCellEditable(e);
            }

            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                Component comp = super.getTableCellEditorComponent(table, value, isSelected, row, column);
                if (value instanceof File) {
                    tf.setText(chooser.getName((File) value));
                    tf.requestFocus();
                    tf.selectAll();
                }
                return comp;
            }
        });
        JList fakeList = new JList(detailsTableModel.listModel) {

            JTable table = detailsTable;

            public int locationToIndex(Point location) {
                return table.rowAtPoint(location);
            }

            public Rectangle getCellBounds(int index0, int index1) {
                Rectangle r0 = table.getCellRect(index0, COLUMN_FILENAME, false);
                Rectangle r1 = table.getCellRect(index1, COLUMN_FILENAME, false);
                return r0.union(r1);
            }

            public Object getSelectedValue() {
                return table.getValueAt(table.getSelectedRow(), COLUMN_FILENAME);
            }

            public Component add(Component comp) {
                if (comp instanceof JTextField) {
                    return table.add(comp);
                } else {
                    return super.add(comp);
                }
            }

            public void repaint() {
                if (table != null) table.repaint();
            }

            public TransferHandler getTransferHandler() {
                if (table != null) {
                    return table.getTransferHandler();
                } else {
                    return super.getTransferHandler();
                }
            }

            public void setTransferHandler(TransferHandler newHandler) {
                if (table != null) {
                    table.setTransferHandler(newHandler);
                } else {
                    super.setTransferHandler(newHandler);
                }
            }

            public boolean getDragEnabled() {
                if (table != null) {
                    return table.getDragEnabled();
                } else {
                    return super.getDragEnabled();
                }
            }

            public void setDragEnabled(boolean b) {
                if (table != null) {
                    table.setDragEnabled(b);
                } else {
                    super.setDragEnabled(b);
                }
            }
        };
        fakeList.setSelectionModel(listSelectionModel);
        detailsTable.addMouseListener(createDoubleClickListener(chooser, fakeList));
        JScrollPane scrollpane = new JScrollPane(detailsTable);
        scrollpane.setComponentOrientation(chooser.getComponentOrientation());
        LookAndFeel.installColors(scrollpane.getViewport(), "Table.background", "Table.foreground");
        scrollpane.addComponentListener(new ComponentAdapter() {

            public void componentResized(ComponentEvent e) {
                JScrollPane sp = (JScrollPane) e.getComponent();
                fixNameColumnWidth(sp.getViewport().getSize().width);
                sp.removeComponentListener(this);
            }
        });
        p.add(scrollpane, BorderLayout.CENTER);
        return p;
    }

    private void fixNameColumnWidth(int viewWidth) {
        TableColumn nameCol = detailsTable.getColumnModel().getColumn(COLUMN_FILENAME);
        int tableWidth = detailsTable.getPreferredSize().width;
        if (tableWidth < viewWidth) {
            nameCol.setPreferredWidth(nameCol.getPreferredWidth() + viewWidth - tableWidth);
        }
    }

    private class DelayedSelectionUpdater implements Runnable {

        DelayedSelectionUpdater() {
            SwingUtilities.invokeLater(this);
        }

        public void run() {
            setFileSelected();
        }
    }

    /**
	 * Creates a selection listener for the list of files and directories.
	 *
	 * @param fc a <code>JFileChooser</code>
	 * @return a <code>ListSelectionListener</code>
	 */
    public ListSelectionListener createListSelectionListener(JFileChooser fc) {
        return new SelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    JFileChooser chooser = getFileChooser();
                    FileSystemView fsv = chooser.getFileSystemView();
                    JList list = (JList) e.getSource();
                    if (chooser.isMultiSelectionEnabled()) {
                        File[] files = null;
                        Object[] objects = list.getSelectedValues();
                        if (objects != null) {
                            if (objects.length == 1 && ((File) objects[0]).isDirectory() && chooser.isTraversable(((File) objects[0])) && (chooser.getFileSelectionMode() == JFileChooser.FILES_ONLY || !fsv.isFileSystem(((File) objects[0])))) {
                                setDirectorySelected(true);
                                setDirectory(((File) objects[0]));
                            } else {
                                files = new File[objects.length];
                                int j = 0;
                                for (int i = 0; i < objects.length; i++) {
                                    File f = (File) objects[i];
                                    if ((chooser.isFileSelectionEnabled() && f.isFile()) || (chooser.isDirectorySelectionEnabled() && fsv.isFileSystem(f) && f.isDirectory())) {
                                        files[j++] = f;
                                    }
                                }
                                if (j == 0) {
                                    files = null;
                                } else if (j < objects.length) {
                                    File[] tmpFiles = new File[j];
                                    System.arraycopy(files, 0, tmpFiles, 0, j);
                                    files = tmpFiles;
                                }
                                setDirectorySelected(false);
                            }
                        }
                        chooser.setSelectedFiles(files);
                    } else {
                        File file = (File) list.getSelectedValue();
                        if (file != null && file.isDirectory() && chooser.isTraversable(file) && (chooser.getFileSelectionMode() == JFileChooser.FILES_ONLY || !fsv.isFileSystem(file))) {
                            setDirectorySelected(true);
                            setDirectory(file);
                            chooser.setSelectedFile(null);
                        } else {
                            setDirectorySelected(false);
                            if (file != null) {
                                chooser.setSelectedFile(file);
                            }
                        }
                    }
                }
            }
        };
    }

    private MouseListener createSingleClickListener(JFileChooser fc, JList list) {
        return new SingleClickListener(list);
    }

    int lastIndex = -1;

    boolean editing = false;

    int editX = 20;

    private int getEditIndex() {
        return lastIndex;
    }

    private void setEditIndex(int i) {
        lastIndex = i;
    }

    private void resetEditIndex() {
        lastIndex = -1;
    }

    private void cancelEdit() {
        if (editing) {
            editing = false;
            list.remove(editCell);
            centerPanel.repaint();
        } else if (detailsTable != null && detailsTable.isEditing()) {
            detailsTable.getCellEditor().cancelCellEditing();
        }
    }

    JTextField editCell = null;

    private void editFileName(int index) {
        ensureIndexIsVisible(index);
        if (listViewPanel.isVisible()) {
            editing = true;
            Rectangle r = list.getCellBounds(index, index);
            if (editCell == null) {
                editCell = new JTextField();
                editCell.addActionListener(new EditActionListener());
                editCell.addFocusListener(editorFocusListener);
                editCell.setNextFocusableComponent(list);
            }
            list.add(editCell);
            File f = (File) getModel().getElementAt(index);
            editCell.setText(getFileChooser().getName(f));
            if (list.getComponentOrientation().isLeftToRight()) {
                editCell.setBounds(editX + r.x, r.y, r.width - editX, r.height);
            } else {
                editCell.setBounds(r.x, r.y, r.width - editX, r.height);
            }
            editCell.requestFocus();
            editCell.selectAll();
        } else if (detailsViewPanel.isVisible()) {
            detailsTable.editCellAt(index, COLUMN_FILENAME);
        }
    }

    protected class SingleClickListener extends MouseAdapter {

        JList list;

        private long lastClick = 0L;

        public SingleClickListener(JList list) {
            this.list = list;
        }

        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClick > 750 && e.getClickCount() == 1) {
                    lastClick = clickTime;
                    JFileChooser fc = getFileChooser();
                    int index = list.locationToIndex(e.getPoint());
                    if ((!fc.isMultiSelectionEnabled() || fc.getSelectedFiles().length <= 1) && index >= 0 && list.isSelectedIndex(index) && getEditIndex() == index && !editing) {
                        editFileName(index);
                    } else {
                        if (index >= 0) {
                            setEditIndex(index);
                        } else {
                            resetEditIndex();
                        }
                    }
                } else {
                    lastClick = clickTime;
                    resetEditIndex();
                }
            }
        }
    }

    class EditActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            applyEdit();
        }
    }

    private void applyEdit() {
        if (editing) {
            JFileChooser chooser = getFileChooser();
            File f = null;
            if (isDirectorySelected()) {
                f = getDirectory();
            } else {
                f = chooser.getSelectedFile();
            }
            if (f != null) {
                String oldDisplayName = chooser.getName(f);
                String oldFileName = f.getName();
                String newDisplayName = editCell.getText().trim();
                String newFileName;
                if (!newDisplayName.equals(oldDisplayName)) {
                    newFileName = newDisplayName;
                    int i1 = oldFileName.length();
                    int i2 = oldDisplayName.length();
                    if (i1 > i2 && oldFileName.charAt(i2) == '.') {
                        newFileName = newDisplayName + oldFileName.substring(i2);
                    }
                    FileSystemView fsv = chooser.getFileSystemView();
                    File f2 = fsv.createFileObject(f.getParentFile(), newFileName);
                    if (getModel().renameFile(f, f2)) {
                        if (fsv.isParent(chooser.getCurrentDirectory(), f2)) {
                            if (chooser.isMultiSelectionEnabled()) {
                                chooser.setSelectedFiles(new File[] { f2 });
                            } else {
                                chooser.setSelectedFile(f2);
                            }
                        } else {
                        }
                    } else {
                    }
                }
            }
        }
        if (detailsTable != null && detailsTable.isEditing()) {
            detailsTable.getCellEditor().stopCellEditing();
        }
        cancelEdit();
    }

    protected class FileRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            File file = (File) value;
            String fileName = getFileChooser().getName(file);
            setText(fileName);
            Icon icon = FileChooserUI.getIcon(file, fileIcon, getFileChooser().getFileSystemView());
            setIcon(icon);
            if (isSelected) {
                editX = icon.getIconWidth() + 4;
            }
            return this;
        }
    }

    /**	Uninstalls the UI delegeate from the specified component */
    public void uninstallUI(JComponent c) {
        c.removePropertyChangeListener(filterComboBoxModel);
        cancelButton.removeActionListener(getCancelSelectionAction());
        approveButton.removeActionListener(getApproveSelectionAction());
        fileNameTextField.removeActionListener(getApproveSelectionAction());
        super.uninstallUI(c);
    }

    /**
	 * Returns the preferred size of the specified
	 * <code>JFileChooser</code>.
	 * The preferred size is at least as large,
	 * in both height and width,
	 * as the preferred size recommended
	 * by the file chooser's layout manager.
	 *
	 * @param c  a <code>JFileChooser</code>
	 * @return   a <code>Dimension</code> specifying the preferred
	 *           width and height of the file chooser
	 */
    public Dimension getPreferredSize(JComponent c) {
        int prefWidth = PREF_SIZE.width;
        Dimension d = c.getLayout().preferredLayoutSize(c);
        if (d != null) {
            return new Dimension(d.width < prefWidth ? prefWidth : d.width, d.height < PREF_SIZE.height ? PREF_SIZE.height : d.height);
        } else {
            return new Dimension(prefWidth, PREF_SIZE.height);
        }
    }

    /**
	 * Returns the minimum size of the <code>JFileChooser</code>.
	 *
	 * @param c  a <code>JFileChooser</code>
	 * @return   a <code>Dimension</code> specifying the minimum
	 *           width and height of the file chooser
	 */
    public Dimension getMinimumSize(JComponent c) {
        return MIN_SIZE;
    }

    /**
	 * Returns the maximum size of the <code>JFileChooser</code>.
	 *
	 * @param c  a <code>JFileChooser</code>
	 * @return   a <code>Dimension</code> specifying the maximum 
	 *           width and height of the file chooser
	 */
    public Dimension getMaximumSize(JComponent c) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    void setFileSelected() {
        if (getFileChooser().isMultiSelectionEnabled() && !isDirectorySelected()) {
            File[] files = getFileChooser().getSelectedFiles();
            Object[] selectedObjects = list.getSelectedValues();
            for (int j = 0; j < selectedObjects.length; j++) {
                boolean found = false;
                for (int i = 0; i < files.length; i++) {
                    if (files[i].equals(selectedObjects[j])) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    int index = getModel().indexOf(selectedObjects[j]);
                    if (index >= 0) {
                        listSelectionModel.removeSelectionInterval(index, index);
                    }
                }
            }
            for (int i = 0; i < files.length; i++) {
                boolean found = false;
                for (int j = 0; j < selectedObjects.length; j++) {
                    if (files[i].equals(selectedObjects[j])) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    int index = getModel().indexOf(files[i]);
                    if (index >= 0) {
                        listSelectionModel.addSelectionInterval(index, index);
                    }
                }
            }
        } else {
            JFileChooser chooser = getFileChooser();
            File f = null;
            if (isDirectorySelected()) {
                f = getDirectory();
            } else {
                f = chooser.getSelectedFile();
            }
            int i;
            if (f != null && (i = getModel().indexOf(f)) >= 0) {
                listSelectionModel.setSelectionInterval(i, i);
                ensureIndexIsVisible(i);
            } else {
                listSelectionModel.clearSelection();
            }
        }
    }

    /**	Returns the icon for the specified file.
	 * 
	 * 	@param	f				The file for which to return the icon
	 * 	@param	fileIcon		The default file icon
	 * 	@param	fsv			The file system view from which we get
	 * 								the system-specific file icons
	 * 
	 * 	@return					The icon for the specified file, or the
	 * 								default icon, if the file system view did not
	 * 								provide a suitable icon.
	 */
    public static Icon getIcon(File f, Icon fileIcon, FileSystemView fsv) {
        Icon icon = (Icon) iconCache.get(f);
        if (icon != null) {
            return icon;
        }
        icon = fileIcon;
        if (f != null) {
            if (f.isDirectory() && !fsv.isDrive(f) && !fsv.isFileSystemRoot(f) && !fsv.isComputerNode(f) && !fsv.isFloppyDrive(f) && !fsv.isRoot(f)) icon = UIManager.getIcon("FileChooser.folderIcon"); else icon = fsv.getSystemIcon(f);
        }
        iconCache.put(f, icon);
        return icon;
    }

    private String fileNameString(File file) {
        if (file == null) {
            return null;
        } else {
            JFileChooser fc = getFileChooser();
            if (fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled()) {
                return file.getPath();
            } else {
                return file.getName();
            }
        }
    }

    private String fileNameString(File[] files) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; files != null && i < files.length; i++) {
            if (i > 0) {
                buf.append(" ");
            }
            if (files.length > 1) {
                buf.append("\"");
            }
            buf.append(fileNameString(files[i]));
            if (files.length > 1) {
                buf.append("\"");
            }
        }
        return buf.toString();
    }

    private void doSelectedFileChanged(PropertyChangeEvent e) {
        applyEdit();
        File f = (File) e.getNewValue();
        JFileChooser fc = getFileChooser();
        if (f != null && ((fc.isFileSelectionEnabled() && !f.isDirectory()) || (f.isDirectory() && fc.isDirectorySelectionEnabled()))) {
            setFileName(fileNameString(f));
        } else {
            setFileName(null);
        }
        setFileSelected();
    }

    private void doSelectedFilesChanged(PropertyChangeEvent e) {
        applyEdit();
        File[] files = (File[]) e.getNewValue();
        JFileChooser fc = getFileChooser();
        if (files != null && files.length > 0 && (files.length > 1 || fc.isDirectorySelectionEnabled() || !files[0].isDirectory())) {
            setFileName(fileNameString(files));
        } else {
            setFileName(null);
        }
        setFileSelected();
    }

    private void doDirectoryChanged(PropertyChangeEvent e) {
        JFileChooser fc = getFileChooser();
        FileSystemView fsv = fc.getFileSystemView();
        applyEdit();
        resetEditIndex();
        clearIconCache();
        listSelectionModel.clearSelection();
        ensureIndexIsVisible(0);
        File currentDirectory = fc.getCurrentDirectory();
        if (currentDirectory != null) {
            directoryComboBoxModel.addItem(currentDirectory);
            getNewFolderAction().setEnabled(fsv.isFileSystem(currentDirectory) && currentDirectory.canWrite());
            getChangeToParentDirectoryAction().setEnabled(!fsv.isRoot(currentDirectory));
            if (fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled() && fsv.isFileSystem(currentDirectory)) {
                setFileName(currentDirectory.getPath());
            } else {
                setFileName(null);
            }
        }
    }

    private void doFilterChanged(PropertyChangeEvent e) {
        applyEdit();
        resetEditIndex();
        clearIconCache();
        listSelectionModel.clearSelection();
    }

    private void doFileSelectionModeChanged(PropertyChangeEvent e) {
        applyEdit();
        resetEditIndex();
        clearIconCache();
        listSelectionModel.clearSelection();
        JFileChooser fc = getFileChooser();
        File currentDirectory = fc.getCurrentDirectory();
        if (currentDirectory != null && fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled() && fc.getFileSystemView().isFileSystem(currentDirectory)) {
            setFileName(currentDirectory.getPath());
        } else {
            setFileName(null);
        }
    }

    private void doMultiSelectionChanged(PropertyChangeEvent e) {
        if (getFileChooser().isMultiSelectionEnabled()) {
            listSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        } else {
            listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            listSelectionModel.clearSelection();
            getFileChooser().setSelectedFiles(null);
        }
    }

    private void doAccessoryChanged(PropertyChangeEvent e) {
        if (getAccessoryPanel() != null) {
            if (e.getOldValue() != null) {
                getAccessoryPanel().remove((JComponent) e.getOldValue());
            }
            JComponent accessory = (JComponent) e.getNewValue();
            if (accessory != null) {
                getAccessoryPanel().add(accessory, BorderLayout.CENTER);
            }
        }
    }

    private void doApproveButtonTextChanged(PropertyChangeEvent e) {
        JFileChooser chooser = getFileChooser();
        approveButton.setText(getApproveButtonText(chooser));
        approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
    }

    private void doDialogTypeChanged(PropertyChangeEvent e) {
        JFileChooser chooser = getFileChooser();
        approveButton.setText(getApproveButtonText(chooser));
        approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
        if (chooser.getDialogType() == JFileChooser.SAVE_DIALOG) {
            lookInLabel.setText(saveInLabelText);
        } else {
            lookInLabel.setText(lookInLabelText);
        }
    }

    private void doApproveButtonMnemonicChanged(PropertyChangeEvent e) {
    }

    private void doControlButtonsChanged(PropertyChangeEvent e) {
        if (getFileChooser().getControlButtonsAreShown()) {
            addControlButtons();
        } else {
            removeControlButtons();
        }
    }

    public PropertyChangeListener createPropertyChangeListener(JFileChooser fc) {
        return new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent e) {
                String s = e.getPropertyName();
                if (s.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                    doSelectedFileChanged(e);
                } else if (s.equals(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY)) {
                    doSelectedFilesChanged(e);
                } else if (s.equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
                    doDirectoryChanged(e);
                } else if (s.equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
                    doFilterChanged(e);
                } else if (s.equals(JFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY)) {
                    doFileSelectionModeChanged(e);
                } else if (s.equals(JFileChooser.MULTI_SELECTION_ENABLED_CHANGED_PROPERTY)) {
                    doMultiSelectionChanged(e);
                } else if (s.equals(JFileChooser.ACCESSORY_CHANGED_PROPERTY)) {
                    doAccessoryChanged(e);
                } else if (s.equals(JFileChooser.APPROVE_BUTTON_TEXT_CHANGED_PROPERTY) || s.equals(JFileChooser.APPROVE_BUTTON_TOOL_TIP_TEXT_CHANGED_PROPERTY)) {
                    doApproveButtonTextChanged(e);
                } else if (s.equals(JFileChooser.DIALOG_TYPE_CHANGED_PROPERTY)) {
                    doDialogTypeChanged(e);
                } else if (s.equals(JFileChooser.APPROVE_BUTTON_MNEMONIC_CHANGED_PROPERTY)) {
                    doApproveButtonMnemonicChanged(e);
                } else if (s.equals(JFileChooser.CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY)) {
                    doControlButtonsChanged(e);
                } else if (s.equals(JFileChooser.CANCEL_SELECTION)) {
                    applyEdit();
                } else if (s.equals("componentOrientation")) {
                    ComponentOrientation o = (ComponentOrientation) e.getNewValue();
                    JFileChooser cc = (JFileChooser) e.getSource();
                    if (o != (ComponentOrientation) e.getOldValue()) {
                        cc.applyComponentOrientation(o);
                    }
                    if (detailsTable != null) {
                        detailsTable.setComponentOrientation(o);
                        detailsTable.getParent().getParent().setComponentOrientation(o);
                    }
                } else if (s.equals("ancestor")) {
                    if (e.getOldValue() == null && e.getNewValue() != null) {
                        fileNameTextField.selectAll();
                        fileNameTextField.requestFocus();
                    }
                }
            }
        };
    }

    protected void removeControlButtons() {
        getBottomPanel().remove(getButtonPanel());
    }

    protected void addControlButtons() {
        getBottomPanel().add(getButtonPanel());
    }

    private void ensureIndexIsVisible(int i) {
        if (i >= 0) {
            list.ensureIndexIsVisible(i);
            if (detailsTable != null) {
                detailsTable.scrollRectToVisible(detailsTable.getCellRect(i, COLUMN_FILENAME, true));
            }
        }
    }

    /**	Ensures that the specified file in the specified JFileChooser
	 * 	is visible in the file list.
	 */
    public void ensureFileIsVisible(JFileChooser fc, File f) {
        ensureIndexIsVisible(getModel().indexOf(f));
    }

    /**	Updates the display for the current directory, by rescanning it
	 * 	and updating the files contained in the directory.
	 */
    public void rescanCurrentDirectory(JFileChooser fc) {
        getModel().validateFileCache();
    }

    /**	Returns the file name of the selected file, or null if 
	 * 	the user has not entered or selected a file.
	 */
    public String getFileName() {
        if (fileNameTextField != null) {
            return fileNameTextField.getText();
        } else {
            return null;
        }
    }

    /**	Sets the file name in the file name text field */
    public void setFileName(String filename) {
        if (fileNameTextField != null) {
            fileNameTextField.setText(filename);
        }
    }

    /**
	 * Property to remember whether a directory is currently selected in the UI.
	 * This is normally called by the UI on a selection event.
	 *
	 * @param directorySelected if a directory is currently selected.
	 * @since 1.4
	 */
    protected void setDirectorySelected(boolean directorySelected) {
        super.setDirectorySelected(directorySelected);
        JFileChooser chooser = getFileChooser();
        if (directorySelected) {
            approveButton.setText(directoryOpenButtonText);
            approveButton.setToolTipText(directoryOpenButtonToolTipText);
        } else {
            approveButton.setText(getApproveButtonText(chooser));
            approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
        }
    }

    protected DirectoryComboBoxRenderer createDirectoryComboBoxRenderer(JFileChooser fc) {
        return new DirectoryComboBoxRenderer();
    }

    class DirectoryComboBoxRenderer extends DefaultListCellRenderer {

        IndentIcon ii = new IndentIcon();

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null) {
                setText("");
                return this;
            }
            File directory = (File) value;
            setText(getFileChooser().getName(directory));
            Icon icon = FileChooserUI.getIcon(directory, fileIcon, getFileChooser().getFileSystemView());
            ii.icon = icon;
            ii.depth = directoryComboBoxModel.getDepth(index);
            setIcon(ii);
            return this;
        }
    }

    static final int space = 10;

    class IndentIcon implements Icon {

        Icon icon = null;

        int depth = 0;

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (c.getComponentOrientation().isLeftToRight()) {
                icon.paintIcon(c, g, x + depth * space, y);
            } else {
                icon.paintIcon(c, g, x, y);
            }
        }

        public int getIconWidth() {
            return icon.getIconWidth() + depth * space;
        }

        public int getIconHeight() {
            return icon.getIconHeight();
        }
    }

    protected DirectoryComboBoxModel createDirectoryComboBoxModel(JFileChooser fc) {
        return new DirectoryComboBoxModel();
    }

    /**
	 * Data model for a type-face selection combo-box.
	 */
    protected class DirectoryComboBoxModel extends AbstractListModel implements ComboBoxModel {

        Vector directories = new Vector();

        int[] depths = null;

        File selectedDirectory = null;

        JFileChooser chooser = getFileChooser();

        FileSystemView fsv = chooser.getFileSystemView();

        public DirectoryComboBoxModel() {
            File dir = getFileChooser().getCurrentDirectory();
            if (dir != null) {
                addItem(dir);
            }
        }

        /**
		 * Adds the directory to the model and sets it to be selected,
		 * additionally clears out the previous selected directory and
		 * the paths leading up to it, if any.
		 */
        private void addItem(File directory) {
            if (directory == null) {
                return;
            }
            directories.clear();
            File[] baseFolders;
            if (useShellFolder) {
                baseFolders = (File[]) ShellFolder.get("fileChooserComboBoxFolders");
            } else {
                baseFolders = fsv.getRoots();
            }
            directories.addAll(Arrays.asList(baseFolders));
            File canonical = null;
            try {
                canonical = directory.getCanonicalFile();
            } catch (IOException e) {
                canonical = directory;
            }
            try {
                File sf = ShellFolder.getShellFolder(canonical);
                File f = sf;
                Vector path = new Vector(10);
                do {
                    path.addElement(f);
                } while ((f = f.getParentFile()) != null);
                int pathCount = path.size();
                for (int i = 0; i < pathCount; i++) {
                    f = (File) path.get(i);
                    if (directories.contains(f)) {
                        int topIndex = directories.indexOf(f);
                        for (int j = i - 1; j >= 0; j--) {
                            directories.insertElementAt(path.get(j), topIndex + i - j);
                        }
                        break;
                    }
                }
                calculateDepths();
                setSelectedItem(sf);
            } catch (FileNotFoundException ex) {
                calculateDepths();
            }
        }

        private void calculateDepths() {
            depths = new int[directories.size()];
            for (int i = 0; i < depths.length; i++) {
                File dir = (File) directories.get(i);
                File parent = dir.getParentFile();
                depths[i] = 0;
                if (parent != null) {
                    for (int j = i - 1; j >= 0; j--) {
                        if (parent.equals((File) directories.get(j))) {
                            depths[i] = depths[j] + 1;
                            break;
                        }
                    }
                }
            }
        }

        public int getDepth(int i) {
            return (depths != null && i >= 0 && i < depths.length) ? depths[i] : 0;
        }

        public void setSelectedItem(Object selectedDirectory) {
            this.selectedDirectory = (File) selectedDirectory;
            fireContentsChanged(this, -1, -1);
        }

        public Object getSelectedItem() {
            return selectedDirectory;
        }

        public int getSize() {
            return directories.size();
        }

        public Object getElementAt(int index) {
            return directories.elementAt(index);
        }
    }

    protected FilterComboBoxRenderer createFilterComboBoxRenderer() {
        return new FilterComboBoxRenderer();
    }

    /**
	 * Render different type sizes and styles.
	 */
    public class FilterComboBoxRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null && value instanceof FileFilter) {
                setText(((FileFilter) value).getDescription());
            }
            return this;
        }
    }

    protected FilterComboBoxModel createFilterComboBoxModel() {
        return new FilterComboBoxModel();
    }

    /**
	 * Data model for a type-face selection combo-box.
	 */
    protected class FilterComboBoxModel extends AbstractListModel implements ComboBoxModel, PropertyChangeListener {

        protected FileFilter[] filters;

        protected FilterComboBoxModel() {
            super();
            filters = getFileChooser().getChoosableFileFilters();
        }

        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (prop == JFileChooser.CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY) {
                filters = (FileFilter[]) e.getNewValue();
                fireContentsChanged(this, -1, -1);
            } else if (prop == JFileChooser.FILE_FILTER_CHANGED_PROPERTY) {
                fireContentsChanged(this, -1, -1);
            }
        }

        public void setSelectedItem(Object filter) {
            if (filter != null) {
                getFileChooser().setFileFilter((FileFilter) filter);
                setFileName(null);
                fireContentsChanged(this, -1, -1);
            }
        }

        public Object getSelectedItem() {
            FileFilter currentFilter = getFileChooser().getFileFilter();
            boolean found = false;
            if (currentFilter != null) {
                for (int i = 0; i < filters.length; i++) {
                    if (filters[i] == currentFilter) {
                        found = true;
                    }
                }
                if (found == false) {
                    getFileChooser().addChoosableFileFilter(currentFilter);
                }
            }
            return getFileChooser().getFileFilter();
        }

        public int getSize() {
            if (filters != null) {
                return filters.length;
            } else {
                return 0;
            }
        }

        public Object getElementAt(int index) {
            if (index > getSize() - 1) {
                return getFileChooser().getFileFilter();
            }
            if (filters != null) {
                return filters[index];
            } else {
                return null;
            }
        }
    }

    /**	Informs this instance that the selection has changed. */
    public void valueChanged(ListSelectionEvent e) {
        JFileChooser fc = getFileChooser();
        File f = fc.getSelectedFile();
        if (!e.getValueIsAdjusting() && f != null && !getFileChooser().isTraversable(f)) {
            setFileName(fileNameString(f));
        }
    }

    /**
	 * Acts when DirectoryComboBox has changed the selected item.
	 */
    protected class DirectoryComboBoxAction extends AbstractAction {

        protected DirectoryComboBoxAction() {
            super("DirectoryComboBoxAction");
        }

        public void actionPerformed(ActionEvent e) {
            File f = (File) directoryComboBox.getSelectedItem();
            getFileChooser().setCurrentDirectory(f);
        }
    }

    protected JButton getApproveButton(JFileChooser fc) {
        return approveButton;
    }

    /**
	 * <code>ButtonAreaLayout</code> behaves in a similar manner to
	 * <code>FlowLayout</code>. It lays out all components from left to
	 * right, flushed right. The widths of all components will be set
	 * to the largest preferred size width.
	 */
    private static class ButtonAreaLayout implements LayoutManager {

        private int hGap = 5;

        private int topMargin = 17;

        public void addLayoutComponent(String string, Component comp) {
        }

        public void layoutContainer(Container container) {
            Component[] children = container.getComponents();
            if (children != null && children.length > 0) {
                int numChildren = children.length;
                Dimension[] sizes = new Dimension[numChildren];
                Insets insets = container.getInsets();
                int yLocation = insets.top + topMargin;
                int maxWidth = 0;
                for (int counter = 0; counter < numChildren; counter++) {
                    sizes[counter] = children[counter].getPreferredSize();
                    maxWidth = Math.max(maxWidth, sizes[counter].width);
                }
                int xLocation, xOffset;
                if (container.getComponentOrientation().isLeftToRight()) {
                    xLocation = container.getSize().width - insets.left - maxWidth;
                    xOffset = hGap + maxWidth;
                } else {
                    xLocation = insets.left;
                    xOffset = -(hGap + maxWidth);
                }
                for (int counter = numChildren - 1; counter >= 0; counter--) {
                    children[counter].setBounds(xLocation, yLocation, maxWidth, sizes[counter].height);
                    xLocation -= xOffset;
                }
            }
        }

        public Dimension minimumLayoutSize(Container c) {
            if (c != null) {
                Component[] children = c.getComponents();
                if (children != null && children.length > 0) {
                    int numChildren = children.length;
                    int height = 0;
                    Insets cInsets = c.getInsets();
                    int extraHeight = topMargin + cInsets.top + cInsets.bottom;
                    int extraWidth = cInsets.left + cInsets.right;
                    int maxWidth = 0;
                    for (int counter = 0; counter < numChildren; counter++) {
                        Dimension aSize = children[counter].getPreferredSize();
                        height = Math.max(height, aSize.height);
                        maxWidth = Math.max(maxWidth, aSize.width);
                    }
                    return new Dimension(extraWidth + numChildren * maxWidth + (numChildren - 1) * hGap, extraHeight + height);
                }
            }
            return new Dimension(0, 0);
        }

        public Dimension preferredLayoutSize(Container c) {
            return minimumLayoutSize(c);
        }

        public void removeLayoutComponent(Component c) {
        }
    }

    private static void groupLabels(AlignedLabel[] group) {
        for (int i = 0; i < group.length; i++) {
            group[i].group = group;
        }
    }

    private class AlignedLabel extends JLabel {

        private AlignedLabel[] group;

        private int maxWidth = 0;

        AlignedLabel(String text) {
            super(text);
            setAlignmentX(JComponent.LEFT_ALIGNMENT);
        }

        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(getMaxWidth() + 11, d.height);
        }

        private int getMaxWidth() {
            if (maxWidth == 0 && group != null) {
                int max = 0;
                for (int i = 0; i < group.length; i++) {
                    max = Math.max(group[i].getSuperPreferredWidth(), max);
                }
                for (int i = 0; i < group.length; i++) {
                    group[i].maxWidth = max;
                }
            }
            return maxWidth;
        }

        private int getSuperPreferredWidth() {
            return super.getPreferredSize().width;
        }
    }
}
