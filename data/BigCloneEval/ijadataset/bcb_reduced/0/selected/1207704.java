package net.sf.vfsjfilechooser.plaf.windows;

import net.sf.vfsjfilechooser.VFSJFileChooser;
import net.sf.vfsjfilechooser.filechooser.VFSFileFilter;
import net.sf.vfsjfilechooser.filechooser.VFSFileSystemView;
import net.sf.vfsjfilechooser.plaf.basic.BasicVFSFileChooserUI;
import net.sf.vfsjfilechooser.utils.VFSUtils;
import org.apache.commons.vfs.FileObject;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.Position;

/**
 * DO NOT USE, NOT COMPLETE....
 * @author Yves Zoundi <yveszoundi at users dot sf dot net>
 */
public class WindowsVFSFileChooserUI extends BasicVFSFileChooserUI {

    private static final String[] OS_NAMES = new String[] { "Windows 3.1", "Windows 95", "Windows NT", "Windows 98", "Windows 2000", "Windows Me", "Windows XP" };

    private static int WIN_31 = 0;

    private static int WIN_95 = 1;

    private static int WIN_NT = 2;

    private static int WIN_98 = 3;

    private static int WIN_2k = 4;

    private static int WIN_Me = 5;

    private static int WIN_XP = 6;

    private static String osName = System.getProperty("os.name");

    private static String osVersion = System.getProperty("os.version");

    private static final String OS_NAME = ((osName.equals(OS_NAMES[WIN_98]) && osVersion.startsWith("4.9")) ? "Windows Me" : osName);

    private static final int OS_LEVEL = Arrays.asList(OS_NAMES).indexOf(OS_NAME);

    private static final Dimension hstrut10 = new Dimension(10, 1);

    private static final Dimension hstrut25 = new Dimension(25, 1);

    private static final Dimension vstrut1 = new Dimension(1, 1);

    private static final Dimension vstrut4 = new Dimension(1, 4);

    private static final Dimension vstrut5 = new Dimension(1, 5);

    private static final Dimension vstrut6 = new Dimension(1, 6);

    private static final Dimension vstrut8 = new Dimension(1, 8);

    private static final Insets shrinkwrap = new Insets(0, 0, 0, 0);

    private static int PREF_WIDTH = 425;

    private static int PREF_HEIGHT = 245;

    private static Dimension PREF_SIZE = new Dimension(PREF_WIDTH, PREF_HEIGHT);

    private static int MIN_WIDTH = 425;

    private static int MIN_HEIGHT = 245;

    private static Dimension MIN_SIZE = new Dimension(MIN_WIDTH, MIN_HEIGHT);

    private static int LIST_PREF_WIDTH = 444;

    private static int LIST_PREF_HEIGHT = 138;

    private static Dimension LIST_PREF_SIZE = new Dimension(LIST_PREF_WIDTH, LIST_PREF_HEIGHT);

    private static final int COLUMN_FILENAME = 0;

    private static final int COLUMN_FILESIZE = 1;

    private static final int COLUMN_FILETYPE = 2;

    private static final int COLUMN_FILEDATE = 3;

    private static final int COLUMN_FILEATTR = 4;

    private static final int COLUMN_COLCOUNT = 5;

    static final int space = 10;

    private JPanel centerPanel;

    private JLabel lookInLabel;

    private JComboBox directoryComboBox;

    private DirectoryComboBoxModel directoryComboBoxModel;

    private ActionListener directoryComboBoxAction = new DirectoryComboBoxAction();

    private FilterComboBoxModel filterComboBoxModel;

    private JTextField filenameTextField;

    private JToggleButton listViewButton;

    private JPanel listViewPanel;

    private JPanel currentViewPanel;

    private FocusListener editorFocusListener = new FocusAdapter() {

        public void focusLost(FocusEvent e) {
            if (!e.isTemporary()) {
                applyEdit();
            }
        }
    };

    private boolean smallIconsView = false;

    private Border listViewBorder;

    private boolean useShellFolder;

    private ListSelectionModel listSelectionModel;

    private JList list;

    private JButton approveButton;

    private JButton cancelButton;

    private JPanel buttonPanel;

    private JPanel bottomPanel;

    private JComboBox filterComboBox;

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

    private String fileNameHeaderText = null;

    private String fileSizeHeaderText = null;

    private String fileTypeHeaderText = null;

    private String fileDateHeaderText = null;

    private String fileAttrHeaderText = null;

    private Action newFolderAction = new WindowsNewFolderAction();

    private FileObject newFolderFile;

    private BasicVFSFileView fileView = new WindowsVFSFileView();

    int lastIndex = -1;

    FileObject editFile = null;

    int editX = 20;

    JTextField editCell = null;

    public WindowsVFSFileChooserUI(VFSJFileChooser filechooser) {
        super(filechooser);
    }

    public DirectoryComboBoxModel getCombo() {
        return directoryComboBoxModel;
    }

    public static ComponentUI createUI(JComponent c) {
        return new WindowsVFSFileChooserUI((VFSJFileChooser) c);
    }

    public void installUI(JComponent c) {
        super.installUI(c);
    }

    public void uninstallComponents(VFSJFileChooser fc) {
        fc.removeAll();
    }

    public void installComponents(VFSJFileChooser fc) {
        VFSFileSystemView fsv = fc.getFileSystemView();
        listViewBorder = new BevelBorder(BevelBorder.LOWERED, UIManager.getColor("ToolBar.highlight"), UIManager.getColor("ToolBar.background"), UIManager.getColor("ToolBar.darkShadow"), UIManager.getColor("ToolBar.shadow"));
        fc.setBorder(new EmptyBorder(4, 10, 10, 10));
        fc.setLayout(new BorderLayout(8, 8));
        JToolBar topPanel = new JToolBar();
        topPanel.setFloatable(false);
        if (OS_LEVEL >= WIN_2k) {
            topPanel.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        }
        fc.add(topPanel, BorderLayout.NORTH);
        lookInLabel = new JLabel(lookInLabelText);
        lookInLabel.setDisplayedMnemonic(lookInLabelMnemonic);
        lookInLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        lookInLabel.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        topPanel.add(Box.createRigidArea(new Dimension(14, 0)));
        topPanel.add(lookInLabel);
        topPanel.add(Box.createRigidArea(new Dimension(29, 0)));
        directoryComboBox = new JComboBox();
        directoryComboBox.putClientProperty("JComboBox.lightweightKeyboardNavigation", "Lightweight");
        lookInLabel.setLabelFor(directoryComboBox);
        directoryComboBoxModel = createDirectoryComboBoxModel(fc);
        directoryComboBox.setModel(directoryComboBoxModel);
        directoryComboBox.addActionListener(directoryComboBoxAction);
        directoryComboBox.setRenderer(createDirectoryComboBoxRenderer(fc));
        directoryComboBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        directoryComboBox.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        directoryComboBox.setMaximumRowCount(8);
        topPanel.add(directoryComboBox);
        topPanel.add(Box.createRigidArea(hstrut10));
        JButton upFolderButton = new JButton(getChangeToParentDirectoryAction());
        upFolderButton.setText(null);
        upFolderButton.setIcon(upFolderIcon);
        upFolderButton.setToolTipText(upFolderToolTipText);
        upFolderButton.getAccessibleContext().setAccessibleName(upFolderAccessibleName);
        upFolderButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        upFolderButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        upFolderButton.setMargin(shrinkwrap);
        upFolderButton.setFocusPainted(false);
        topPanel.add(upFolderButton);
        if (OS_LEVEL < WIN_2k) {
            topPanel.add(Box.createRigidArea(hstrut10));
        }
        JButton b;
        if (OS_LEVEL == WIN_98) {
            FileObject homeDir = fsv.getHomeDirectory();
            String toolTipText = homeFolderToolTipText;
            if (fsv.isRoot(homeDir)) {
                toolTipText = getFileView(fc).getName(homeDir);
            }
            b = new JButton(getFileView(fc).getIcon(homeDir));
            b.setToolTipText(toolTipText);
            b.getAccessibleContext().setAccessibleName(toolTipText);
            b.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            b.setAlignmentY(JComponent.CENTER_ALIGNMENT);
            b.setMargin(shrinkwrap);
            b.setFocusPainted(false);
            b.addActionListener(getGoHomeAction());
            topPanel.add(b);
            topPanel.add(Box.createRigidArea(hstrut10));
        }
        b = new JButton(getNewFolderAction());
        b.setText(null);
        b.setIcon(newFolderIcon);
        b.setToolTipText(newFolderToolTipText);
        b.getAccessibleContext().setAccessibleName(newFolderAccessibleName);
        b.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        b.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        b.setMargin(shrinkwrap);
        b.setFocusPainted(false);
        topPanel.add(b);
        if (OS_LEVEL < WIN_2k) {
            topPanel.add(Box.createRigidArea(hstrut10));
        }
        ButtonGroup viewButtonGroup = new ButtonGroup();
        class ViewButtonListener implements ActionListener {

            VFSJFileChooser fc;

            ViewButtonListener(VFSJFileChooser fc) {
                this.fc = fc;
            }

            public void actionPerformed(ActionEvent e) {
                JToggleButton b = (JToggleButton) e.getSource();
                JPanel oldViewPanel = currentViewPanel;
                currentViewPanel = listViewPanel;
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
        listViewButton.setFocusPainted(false);
        listViewButton.setSelected(true);
        listViewButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        listViewButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        listViewButton.setMargin(shrinkwrap);
        listViewButton.addActionListener(viewButtonListener);
        topPanel.add(listViewButton);
        viewButtonGroup.add(listViewButton);
        {
            useShellFolder = false;
            FileObject[] roots = fsv.getRoots(VFSUtils.resolveFileObject(System.getProperty("user.home")));
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
        getBottomPanel().setLayout(new BoxLayout(getBottomPanel(), BoxLayout.LINE_AXIS));
        centerPanel.add(getBottomPanel(), BorderLayout.SOUTH);
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.PAGE_AXIS));
        labelPanel.add(Box.createRigidArea(vstrut4));
        JLabel fnl = new JLabel(fileNameLabelText);
        fnl.setDisplayedMnemonic(fileNameLabelMnemonic);
        fnl.setAlignmentY(0);
        labelPanel.add(fnl);
        labelPanel.add(Box.createRigidArea(new Dimension(1, 12)));
        JLabel ftl = new JLabel(filesOfTypeLabelText);
        ftl.setDisplayedMnemonic(filesOfTypeLabelMnemonic);
        labelPanel.add(ftl);
        getBottomPanel().add(labelPanel);
        getBottomPanel().add(Box.createRigidArea(new Dimension(15, 0)));
        JPanel fileAndFilterPanel = new JPanel();
        fileAndFilterPanel.add(Box.createRigidArea(vstrut8));
        fileAndFilterPanel.setLayout(new BoxLayout(fileAndFilterPanel, BoxLayout.Y_AXIS));
        filenameTextField = new JTextField(35) {

            public Dimension getMaximumSize() {
                return new Dimension(Short.MAX_VALUE, super.getPreferredSize().height);
            }
        };
        fnl.setLabelFor(filenameTextField);
        filenameTextField.addFocusListener(new FocusAdapter() {

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
        fileAndFilterPanel.add(filenameTextField);
        fileAndFilterPanel.add(Box.createRigidArea(vstrut8));
        filterComboBoxModel = createFilterComboBoxModel();
        fc.addPropertyChangeListener(filterComboBoxModel);
        filterComboBox = new JComboBox(filterComboBoxModel);
        ftl.setLabelFor(filterComboBox);
        filterComboBox.setRenderer(createFilterComboBoxRenderer());
        fileAndFilterPanel.add(filterComboBox);
        getBottomPanel().add(fileAndFilterPanel);
        getBottomPanel().add(Box.createRigidArea(hstrut10));
        getButtonPanel().setLayout(new BoxLayout(getButtonPanel(), BoxLayout.Y_AXIS));
        approveButton = new JButton(getApproveButtonText(fc)) {

            public Dimension getMaximumSize() {
                return (approveButton.getPreferredSize().width > cancelButton.getPreferredSize().width) ? approveButton.getPreferredSize() : cancelButton.getPreferredSize();
            }
        };
        approveButton.setMnemonic(getApproveButtonMnemonic(fc));
        approveButton.addActionListener(getApproveSelectionAction());
        approveButton.setToolTipText(getApproveButtonToolTipText(fc));
        getButtonPanel().add(Box.createRigidArea(vstrut4));
        getButtonPanel().add(approveButton);
        getButtonPanel().add(Box.createRigidArea(vstrut6));
        cancelButton = new JButton(cancelButtonText) {

            public Dimension getMaximumSize() {
                return (approveButton.getPreferredSize().width > cancelButton.getPreferredSize().width) ? approveButton.getPreferredSize() : cancelButton.getPreferredSize();
            }
        };
        cancelButton.setMnemonic(cancelButtonMnemonic);
        cancelButton.setToolTipText(cancelButtonToolTipText);
        cancelButton.addActionListener(getCancelSelectionAction());
        getButtonPanel().add(cancelButton);
        if (fc.getControlButtonsAreShown()) {
            addControlButtons();
        }
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

    protected void installStrings(VFSJFileChooser fc) {
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
        fileNameHeaderText = UIManager.getString("FileChooser.fileNameHeaderText", l);
        fileSizeHeaderText = UIManager.getString("FileChooser.fileSizeHeaderText", l);
        fileTypeHeaderText = UIManager.getString("FileChooser.fileTypeHeaderText", l);
        fileDateHeaderText = UIManager.getString("FileChooser.fileDateHeaderText", l);
        fileAttrHeaderText = UIManager.getString("FileChooser.fileAttrHeaderText", l);
    }

    protected void installListeners(VFSJFileChooser fc) {
        super.installListeners(fc);
        ActionMap actionMap = getActionMap();
        SwingUtilities.replaceUIActionMap(fc, actionMap);
    }

    protected ActionMap getActionMap() {
        return createActionMap();
    }

    protected ActionMap createActionMap() {
        AbstractAction escAction = new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (editFile != null) {
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

    private void updateListRowCount() {
        if (smallIconsView) {
            list.setVisibleRowCount(getModel().getSize() / 3);
        } else {
            list.setVisibleRowCount(-1);
        }
    }

    protected JPanel createList(VFSJFileChooser fc) {
        JPanel p = new JPanel(new BorderLayout());
        final VFSJFileChooser fileChooser = fc;
        list = new JList() {

            public int getNextMatch(String prefix, int startIndex, Position.Bias bias) {
                ListModel model = getModel();
                int max = model.getSize();
                if ((prefix == null) || (startIndex < 0) || (startIndex >= max)) {
                    throw new IllegalArgumentException();
                }
                boolean backwards = (bias == Position.Bias.Backward);
                for (int i = startIndex; backwards ? (i >= 0) : (i < max); i += (backwards ? (-1) : 1)) {
                    String filename = fileChooser.getName((FileObject) model.getElementAt(i));
                    if (filename.regionMatches(true, 0, prefix, 0, prefix.length())) {
                        return i;
                    }
                }
                return -1;
            }
        };
        list.setCellRenderer(new FileRenderer());
        list.setLayoutOrientation(JList.VERTICAL_WRAP);
        updateListRowCount();
        getModel().addListDataListener(new ListDataListener() {

            public void intervalAdded(ListDataEvent e) {
                updateListRowCount();
            }

            public void intervalRemoved(ListDataEvent e) {
                updateListRowCount();
            }

            public void contentsChanged(ListDataEvent e) {
                updateListRowCount();
            }
        });
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
                int i0 = e.getIndex0();
                int i1 = e.getIndex1();
                if (i0 == i1) {
                    File file = (File) getModel().getElementAt(i0);
                    if (file.equals(newFolderFile)) {
                        new DelayedSelectionUpdater(file);
                        newFolderFile = null;
                    }
                }
            }

            public void intervalRemoved(ListDataEvent e) {
            }
        });
        JScrollPane scrollpane = new JScrollPane(list);
        if (listViewBorder != null) {
            scrollpane.setBorder(listViewBorder);
        }
        p.add(scrollpane, BorderLayout.CENTER);
        return p;
    }

    /**
     * Creates a selection listener for the list of files and directories.
     *
     * @param fc a <code>VFSJFileChooser</code>
     * @return a <code>ListSelectionListener</code>
     */
    public ListSelectionListener createListSelectionListener(VFSJFileChooser fc) {
        return new SelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    VFSJFileChooser chooser = getFileChooser();
                    VFSFileSystemView fsv = chooser.getFileSystemView();
                    JList list = (JList) e.getSource();
                    if (chooser.isMultiSelectionEnabled()) {
                        FileObject[] files = null;
                        Object[] objects = list.getSelectedValues();
                        if (objects != null) {
                            if ((objects.length == 1) && (VFSUtils.isDirectory((FileObject) objects[0])) && chooser.isTraversable(((FileObject) objects[0])) && ((chooser.getFileSelectionMode() == chooser.FILES_ONLY) || !fsv.isFileSystem(((FileObject) objects[0])))) {
                                setDirectorySelected(true);
                                setDirectory(((FileObject) objects[0]));
                            } else {
                                files = new FileObject[objects.length];
                                int j = 0;
                                for (int i = 0; i < objects.length; i++) {
                                    FileObject f = (FileObject) objects[i];
                                    boolean isDir = VFSUtils.isDirectory(f);
                                    boolean isFile = !isDir;
                                    if ((chooser.isFileSelectionEnabled() && isFile) || (chooser.isDirectorySelectionEnabled() && fsv.isFileSystem(f) && isDir)) {
                                        files[j++] = f;
                                    }
                                }
                                if (j == 0) {
                                    files = null;
                                } else if (j < objects.length) {
                                    FileObject[] tmpFiles = new FileObject[j];
                                    System.arraycopy(files, 0, tmpFiles, 0, j);
                                    files = tmpFiles;
                                }
                                setDirectorySelected(false);
                            }
                        }
                        chooser.setSelectedFiles(files);
                    } else {
                        FileObject file = (FileObject) list.getSelectedValue();
                        if ((file != null) && VFSUtils.isDirectory(file) && chooser.isTraversable(file) && ((chooser.getFileSelectionMode() == chooser.FILES_ONLY) || !fsv.isFileSystem(file))) {
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

    private MouseListener createSingleClickListener(VFSJFileChooser fc, JList list) {
        return new SingleClickListener(list);
    }

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
        if (editFile != null) {
            editFile = null;
            list.remove(editCell);
            centerPanel.repaint();
        }
    }

    private void editFileName(int index) {
        ensureIndexIsVisible(index);
        if (listViewPanel.isVisible()) {
            editFile = (FileObject) getModel().getElementAt(index);
            Rectangle r = list.getCellBounds(index, index);
            if (editCell == null) {
                editCell = new JTextField();
                editCell.addActionListener(new EditActionListener());
                editCell.addFocusListener(editorFocusListener);
                editCell.setNextFocusableComponent(list);
            }
            list.add(editCell);
            editCell.setText(getFileChooser().getName(editFile));
            if (list.getComponentOrientation().isLeftToRight()) {
                editCell.setBounds(editX + r.x, r.y, r.width - editX, r.height);
            } else {
                editCell.setBounds(r.x, r.y, r.width - editX, r.height);
            }
            editCell.requestFocus();
            editCell.selectAll();
        }
    }

    public Action getNewFolderAction() {
        return newFolderAction;
    }

    private void applyEdit() {
        if ((editFile != null) && VFSUtils.exists(editFile)) {
            VFSJFileChooser chooser = getFileChooser();
            String oldDisplayName = chooser.getName(editFile);
            String oldFileName = editFile.getName().getBaseName();
            String newDisplayName = editCell.getText().trim();
            String newFileName;
            if (!newDisplayName.equals(oldDisplayName)) {
                newFileName = newDisplayName;
                int i1 = oldFileName.length();
                int i2 = oldDisplayName.length();
                if ((i1 > i2) && (oldFileName.charAt(i2) == '.')) {
                    newFileName = newDisplayName + oldFileName.substring(i2);
                }
                VFSFileSystemView fsv = chooser.getFileSystemView();
                FileObject f2 = fsv.createFileObject(VFSUtils.getParentDirectory(editFile), newFileName);
                if (!VFSUtils.exists(f2) && getModel().renameFile(editFile, f2)) {
                    if (fsv.isParent(chooser.getCurrentDirectory(), f2)) {
                        if (chooser.isMultiSelectionEnabled()) {
                            chooser.setSelectedFiles(new FileObject[] { f2 });
                        } else {
                            chooser.setSelectedFile(f2);
                        }
                    } else {
                    }
                } else {
                }
            }
        }
        cancelEdit();
    }

    public void uninstallUI(JComponent c) {
        c.removePropertyChangeListener(filterComboBoxModel);
        cancelButton.removeActionListener(getCancelSelectionAction());
        approveButton.removeActionListener(getApproveSelectionAction());
        filenameTextField.removeActionListener(getApproveSelectionAction());
        super.uninstallUI(c);
    }

    /**
     * Returns the preferred size of the specified
     * <code>VFSJFileChooser</code>.
     * The preferred size is at least as large,
     * in both height and width,
     * as the preferred size recommended
     * by the file chooser's layout manager.
     *
     * @param c  a <code>VFSJFileChooser</code>
     * @return   a <code>Dimension</code> specifying the preferred
     *           width and height of the file chooser
     */
    public Dimension getPreferredSize(JComponent c) {
        int prefWidth = PREF_SIZE.width;
        Dimension d = c.getLayout().preferredLayoutSize(c);
        if (d != null) {
            return new Dimension((d.width < prefWidth) ? prefWidth : d.width, (d.height < PREF_SIZE.height) ? PREF_SIZE.height : d.height);
        } else {
            return new Dimension(prefWidth, PREF_SIZE.height);
        }
    }

    /**
     * Returns the minimum size of the <code>VFSJFileChooser</code>.
     *
     * @param c  a <code>VFSJFileChooser</code>
     * @return   a <code>Dimension</code> specifying the minimum
     *           width and height of the file chooser
     */
    public Dimension getMinimumSize(JComponent c) {
        return MIN_SIZE;
    }

    /**
     * Returns the maximum size of the <code>VFSJFileChooser</code>.
     *
     * @param c  a <code>VFSJFileChooser</code>
     * @return   a <code>Dimension</code> specifying the maximum
     *           width and height of the file chooser
     */
    public Dimension getMaximumSize(JComponent c) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    void setFileSelected() {
        if (getFileChooser().isMultiSelectionEnabled() && !isDirectorySelected()) {
            FileObject[] files = getFileChooser().getSelectedFiles();
            Object[] selectedObjects = list.getSelectedValues();
            listSelectionModel.setValueIsAdjusting(true);
            try {
                Arrays.sort(files);
                Arrays.sort(selectedObjects);
                int shouldIndex = 0;
                int actuallyIndex = 0;
                while ((shouldIndex < files.length) && (actuallyIndex < selectedObjects.length)) {
                    shouldIndex++;
                    actuallyIndex++;
                }
                while (shouldIndex < files.length) {
                    int index = getModel().indexOf(files[shouldIndex]);
                    listSelectionModel.addSelectionInterval(index, index);
                    shouldIndex++;
                }
                while (actuallyIndex < selectedObjects.length) {
                    int index = getModel().indexOf(selectedObjects[actuallyIndex]);
                    listSelectionModel.removeSelectionInterval(index, index);
                    actuallyIndex++;
                }
            } finally {
                listSelectionModel.setValueIsAdjusting(false);
            }
        } else {
            VFSJFileChooser chooser = getFileChooser();
            FileObject f = null;
            if (isDirectorySelected()) {
                f = getDirectory();
            } else {
                f = chooser.getSelectedFile();
            }
            int i;
            if ((f != null) && ((i = getModel().indexOf(f)) >= 0)) {
                listSelectionModel.setSelectionInterval(i, i);
                ensureIndexIsVisible(i);
            } else {
                listSelectionModel.clearSelection();
            }
        }
    }

    private String fileNameString(FileObject file) {
        if (file == null) {
            return null;
        } else {
            VFSJFileChooser fc = getFileChooser();
            if ((fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled()) || (fc.isDirectorySelectionEnabled() && fc.isFileSelectionEnabled() && fc.getFileSystemView().isFileSystemRoot(file))) {
                return file.getName().getBaseName();
            } else {
                return file.getName().getBaseName();
            }
        }
    }

    private String fileNameString(FileObject[] files) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; (files != null) && (i < files.length); i++) {
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
        FileObject f = (FileObject) e.getNewValue();
        VFSJFileChooser fc = getFileChooser();
        if ((f != null) && ((fc.isFileSelectionEnabled() && !VFSUtils.isDirectory(f)) || (VFSUtils.isDirectory(f) && fc.isDirectorySelectionEnabled()))) {
            setFileName(fileNameString(f));
        }
    }

    private void doSelectedFilesChanged(PropertyChangeEvent e) {
        FileObject[] files = (FileObject[]) e.getNewValue();
        VFSJFileChooser fc = getFileChooser();
        if ((files != null) && (files.length > 0) && ((files.length > 1) || fc.isDirectorySelectionEnabled() || !VFSUtils.isDirectory(files[0]))) {
            setFileName(fileNameString(files));
        }
    }

    private void doDirectoryChanged(PropertyChangeEvent e) {
        VFSJFileChooser fc = getFileChooser();
        VFSFileSystemView fsv = fc.getFileSystemView();
        clearIconCache();
        FileObject currentDirectory = fc.getCurrentDirectory();
        if (currentDirectory != null) {
            directoryComboBoxModel.addItem(currentDirectory);
            directoryComboBox.setSelectedItem(currentDirectory);
            fc.setCurrentDirectory(currentDirectory);
            if (fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled()) {
                if (fsv.isFileSystem(currentDirectory)) {
                    setFileName(currentDirectory.getName().getBaseName() + "");
                } else {
                    setFileName(currentDirectory.getName().getBaseName());
                }
            }
        }
    }

    private void doFilterChanged(PropertyChangeEvent e) {
        clearIconCache();
    }

    private void doFileSelectionModeChanged(PropertyChangeEvent e) {
        applyEdit();
        resetEditIndex();
        clearIconCache();
        listSelectionModel.clearSelection();
        VFSJFileChooser fc = getFileChooser();
        FileObject currentDirectory = fc.getCurrentDirectory();
        if ((currentDirectory != null) && fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled() && fc.getFileSystemView().isFileSystem(currentDirectory)) {
            setFileName(currentDirectory.getName().getBaseName());
        } else {
            setFileName(currentDirectory.getName().getBaseName());
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
        VFSJFileChooser chooser = getFileChooser();
        approveButton.setText(getApproveButtonText(chooser));
        approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
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

    private void doDialogTypeChanged(PropertyChangeEvent e) {
        VFSJFileChooser chooser = getFileChooser();
        approveButton.setText(getApproveButtonText(chooser));
        approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
        approveButton.setMnemonic(getApproveButtonMnemonic(chooser));
        if (chooser.getDialogType() == VFSJFileChooser.SAVE_DIALOG) {
            lookInLabel.setText(saveInLabelText);
        } else {
            lookInLabel.setText(lookInLabelText);
        }
    }

    private void doApproveButtonMnemonicChanged(PropertyChangeEvent e) {
        approveButton.setMnemonic(getApproveButtonMnemonic(getFileChooser()));
    }

    private void doControlButtonsChanged(PropertyChangeEvent e) {
        if (getFileChooser().getControlButtonsAreShown()) {
            addControlButtons();
        } else {
            removeControlButtons();
        }
    }

    public PropertyChangeListener createPropertyChangeListener(VFSJFileChooser fc) {
        return new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent e) {
                String s = e.getPropertyName();
                if (s.equals(VFSJFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                    doSelectedFileChanged(e);
                } else if (s.equals(VFSJFileChooser.SELECTED_FILES_CHANGED_PROPERTY)) {
                    doSelectedFilesChanged(e);
                } else if (s.equals(VFSJFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
                    doDirectoryChanged(e);
                } else if (s.equals(VFSJFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
                    doFilterChanged(e);
                } else if (s.equals(VFSJFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY)) {
                    doFileSelectionModeChanged(e);
                } else if (s.equals(VFSJFileChooser.MULTI_SELECTION_ENABLED_CHANGED_PROPERTY)) {
                    doMultiSelectionChanged(e);
                } else if (s.equals(VFSJFileChooser.ACCESSORY_CHANGED_PROPERTY)) {
                    doAccessoryChanged(e);
                } else if (s.equals(VFSJFileChooser.APPROVE_BUTTON_TEXT_CHANGED_PROPERTY) || s.equals(VFSJFileChooser.APPROVE_BUTTON_TOOL_TIP_TEXT_CHANGED_PROPERTY)) {
                    doApproveButtonTextChanged(e);
                } else if (s.equals(VFSJFileChooser.DIALOG_TYPE_CHANGED_PROPERTY)) {
                    doDialogTypeChanged(e);
                } else if (s.equals(VFSJFileChooser.APPROVE_BUTTON_MNEMONIC_CHANGED_PROPERTY)) {
                    doApproveButtonMnemonicChanged(e);
                } else if (s.equals(VFSJFileChooser.CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY)) {
                    doControlButtonsChanged(e);
                } else if (s.equals("componentOrientation")) {
                    ComponentOrientation o = (ComponentOrientation) e.getNewValue();
                    VFSJFileChooser cc = (VFSJFileChooser) e.getSource();
                    if (o != (ComponentOrientation) e.getOldValue()) {
                        cc.applyComponentOrientation(o);
                    }
                } else if (s.equals("ancestor")) {
                    if ((e.getOldValue() == null) && (e.getNewValue() != null)) {
                        filenameTextField.selectAll();
                        filenameTextField.requestFocus();
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
        }
    }

    public void ensureFileIsVisible(VFSJFileChooser fc, FileObject f) {
        ensureIndexIsVisible(getModel().indexOf(f));
    }

    public void rescanCurrentDirectory(VFSJFileChooser fc) {
        getModel().validateFileCache();
    }

    public String getFileName() {
        if (filenameTextField != null) {
            return filenameTextField.getText();
        } else {
            return null;
        }
    }

    public void setFileName(String filename) {
        if (filenameTextField != null) {
            filenameTextField.setText(filename);
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
        VFSJFileChooser chooser = getFileChooser();
        if (directorySelected) {
            approveButton.setText(directoryOpenButtonText);
            approveButton.setToolTipText(directoryOpenButtonToolTipText);
            approveButton.setMnemonic(directoryOpenButtonMnemonic);
        } else {
            approveButton.setText(getApproveButtonText(chooser));
            approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
            approveButton.setMnemonic(getApproveButtonMnemonic(chooser));
        }
    }

    public String getDirectoryName() {
        return null;
    }

    public void setDirectoryName(String dirname) {
    }

    protected DirectoryComboBoxRenderer createDirectoryComboBoxRenderer(VFSJFileChooser fc) {
        return new DirectoryComboBoxRenderer();
    }

    protected DirectoryComboBoxModel createDirectoryComboBoxModel(VFSJFileChooser fc) {
        return new DirectoryComboBoxModel();
    }

    protected FilterComboBoxRenderer createFilterComboBoxRenderer() {
        return new FilterComboBoxRenderer();
    }

    protected FilterComboBoxModel createFilterComboBoxModel() {
        return new FilterComboBoxModel();
    }

    public void valueChanged(ListSelectionEvent e) {
        VFSJFileChooser fc = getFileChooser();
        FileObject f = fc.getSelectedFile();
        if (!e.getValueIsAdjusting() && (f != null) && !getFileChooser().isTraversable(f)) {
            setFileName(fileNameString(f));
        }
    }

    protected JButton getApproveButton(VFSJFileChooser fc) {
        return approveButton;
    }

    public BasicVFSFileView getFileView(VFSJFileChooser fc) {
        return fileView;
    }

    private class DelayedSelectionUpdater implements Runnable {

        File editFile;

        DelayedSelectionUpdater() {
            this(null);
        }

        DelayedSelectionUpdater(File editFile) {
            this.editFile = editFile;
            SwingUtilities.invokeLater(this);
        }

        public void run() {
            setFileSelected();
            if (editFile != null) {
                editFileName(getModel().indexOf(editFile));
                editFile = null;
            }
        }
    }

    protected class SingleClickListener extends MouseAdapter {

        JList list;

        public SingleClickListener(JList list) {
            this.list = list;
        }

        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (e.getClickCount() == 1) {
                    VFSJFileChooser fc = getFileChooser();
                    int index = list.locationToIndex(e.getPoint());
                    if ((!fc.isMultiSelectionEnabled() || (fc.getSelectedFiles().length <= 1)) && (index >= 0) && list.isSelectedIndex(index) && (getEditIndex() == index) && (editFile == null)) {
                        editFileName(index);
                    } else {
                        if (index >= 0) {
                            setEditIndex(index);
                        } else {
                            resetEditIndex();
                        }
                    }
                } else {
                    resetEditIndex();
                }
            }
        }
    }

    /**
     * Creates a new folder.
     */
    protected class WindowsNewFolderAction extends NewFolderAction {

        public void actionPerformed(ActionEvent e) {
            VFSJFileChooser fc = getFileChooser();
            FileObject oldFile = fc.getSelectedFile();
            super.actionPerformed(e);
            FileObject newFile = fc.getSelectedFile();
            if ((newFile != null) && !newFile.getName().getURI().equals(oldFile.getName().getURI()) && VFSUtils.isDirectory(newFile)) {
                newFolderFile = newFile;
            }
        }
    }

    class EditActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            applyEdit();
        }
    }

    protected class FileRenderer extends DefaultListCellRenderer {

        public void setBounds(int x, int y, int width, int height) {
            super.setBounds(x, y, Math.min(width, this.getPreferredSize().width + 4), height);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            FileObject file = (FileObject) value;
            String fileName = getFileChooser().getName(file);
            setText(fileName);
            Icon icon = getFileChooser().getIcon(file);
            setIcon(icon);
            if (isSelected) {
                editX = icon.getIconWidth() + 4;
            }
            return this;
        }
    }

    class DirectoryComboBoxRenderer extends DefaultListCellRenderer {

        IndentIcon ii = new IndentIcon();

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null) {
                setText("");
                return this;
            }
            FileObject directory = (FileObject) value;
            setText(VFSUtils.getFriendlyName(getFileChooser().getName(directory)));
            Icon icon = getFileChooser().getIcon(directory);
            ii.icon = icon;
            ii.depth = directoryComboBoxModel.getDepth(index);
            setIcon(ii);
            return this;
        }
    }

    class IndentIcon implements Icon {

        Icon icon = null;

        int depth = 0;

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (c.getComponentOrientation().isLeftToRight()) {
                icon.paintIcon(c, g, x + (depth * space), y);
            } else {
                icon.paintIcon(c, g, x, y);
            }
        }

        public int getIconWidth() {
            return icon.getIconWidth() + (depth * space);
        }

        public int getIconHeight() {
            return icon.getIconHeight();
        }
    }

    /**
     * Data model for a type-face selection combo-box.
     */
    public class DirectoryComboBoxModel extends AbstractListModel implements ComboBoxModel {

        Vector directories = new Vector();

        int[] depths = null;

        FileObject selectedDirectory = null;

        VFSJFileChooser chooser = getFileChooser();

        VFSFileSystemView fsv = chooser.getFileSystemView();

        public DirectoryComboBoxModel() {
            FileObject dir = getFileChooser().getCurrentDirectory();
            if (dir != null) {
                addItem(dir);
            }
        }

        /**
         * Adds the directory to the model and sets it to be selected,
         * additionally clears out the previous selected directory and
         * the paths leading up to it, if any.
         */
        private void addItem(FileObject directory) {
            if (directory == null) {
                return;
            }
            directories.clear();
            FileObject[] baseFolders;
            baseFolders = fsv.getRoots(directory);
            directories.addAll(Arrays.asList(baseFolders));
            try {
                FileObject f = directory;
                Vector path = new Vector(10);
                do {
                    path.addElement(f);
                } while ((f = VFSUtils.getParentDirectory(f)) != null);
                int pathCount = path.size();
                for (int i = 0; i < pathCount; i++) {
                    f = (FileObject) path.get(i);
                    if (directories.contains(f)) {
                        int topIndex = directories.indexOf(f);
                        for (int j = i - 1; j >= 0; j--) {
                            directories.insertElementAt(path.get(j), (topIndex + i) - j);
                        }
                        break;
                    }
                }
                calculateDepths();
            } catch (Exception ex) {
                calculateDepths();
            }
        }

        private void calculateDepths() {
            depths = new int[directories.size()];
            for (int i = 0; i < depths.length; i++) {
                FileObject dir = (FileObject) directories.get(i);
                FileObject parent = VFSUtils.getParentDirectory(dir);
                depths[i] = 0;
                if (parent != null) {
                    for (int j = i - 1; j >= 0; j--) {
                        if (parent.equals((FileObject) directories.get(j))) {
                            depths[i] = depths[j] + 1;
                            break;
                        }
                    }
                }
            }
        }

        public int getDepth(int i) {
            return ((depths != null) && (i >= 0) && (i < depths.length)) ? depths[i] : 0;
        }

        public void setSelectedItem(Object selectedDirectory) {
            this.selectedDirectory = (FileObject) selectedDirectory;
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

    /**
     * Render different type sizes and styles.
     */
    public class FilterComboBoxRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if ((value != null) && value instanceof FileFilter) {
                setText(((VFSFileFilter) value).getDescription());
            }
            return this;
        }
    }

    /**
     * Data model for a type-face selection combo-box.
     */
    protected class FilterComboBoxModel extends AbstractListModel implements ComboBoxModel, PropertyChangeListener {

        protected VFSFileFilter[] filters;

        protected FilterComboBoxModel() {
            super();
            filters = getFileChooser().getChoosableFileFilters();
        }

        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (prop == VFSJFileChooser.CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY) {
                filters = (VFSFileFilter[]) e.getNewValue();
                fireContentsChanged(this, -1, -1);
            } else if (prop == VFSJFileChooser.FILE_FILTER_CHANGED_PROPERTY) {
                fireContentsChanged(this, -1, -1);
            }
        }

        public void setSelectedItem(Object filter) {
            if (filter != null) {
                getFileChooser().setFileFilter((VFSFileFilter) filter);
                setFileName(null);
                fireContentsChanged(this, -1, -1);
            }
        }

        public Object getSelectedItem() {
            VFSFileFilter currentFilter = getFileChooser().getFileFilter();
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
            if (index > (getSize() - 1)) {
                return getFileChooser().getFileFilter();
            }
            if (filters != null) {
                return filters[index];
            } else {
                return null;
            }
        }
    }

    /**
     * Acts when DirectoryComboBox has changed the selected item.
     */
    protected class DirectoryComboBoxAction implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            FileObject f = (FileObject) directoryComboBox.getSelectedItem();
            getFileChooser().setCurrentDirectory(f);
        }
    }

    protected class WindowsVFSFileView extends BasicVFSFileView {

        public Icon getIcon(FileObject f) {
            Icon icon = getCachedIcon(f);
            if (icon != null) {
                return icon;
            }
            if (f != null) {
                icon = getFileChooser().getFileSystemView().getSystemIcon(f);
            }
            if (icon == null) {
                icon = super.getIcon(f);
            }
            cacheIcon(f, icon);
            return icon;
        }
    }
}
