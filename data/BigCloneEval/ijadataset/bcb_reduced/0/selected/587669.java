package net.sf.excompcel.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import net.iharder.dnd.FileDrop;
import net.sf.excompcel.AppPreferences;
import net.sf.excompcel.Main;
import net.sf.excompcel.eGridAction;
import net.sf.excompcel.eMASERT_SLAVE;
import net.sf.excompcel.gui.controls.FileChooserPreselect;
import net.sf.excompcel.gui.controls.HyperlinkLabel;
import net.sf.excompcel.gui.controls.ProgressBar;
import net.sf.excompcel.gui.controls.StatusBar;
import net.sf.excompcel.gui.controls.TextFieldDropTarget;
import net.sf.excompcel.gui.dialog.AboutDialog;
import net.sf.excompcel.gui.dialog.SettingDialog;
import net.sf.excompcel.gui.model.MainModel;
import net.sf.excompcel.gui.util.Util;
import net.sf.excompcel.util.UtilFileHandling;
import net.sf.excompcel.util.fileopener.FolderOpener;
import net.sf.excompcel.util.fileopener.FolderOpenerFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.adapter.ComboBoxAdapter;
import com.jgoodies.binding.beans.BeanAdapter;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ConverterFactory;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The Main Window of the Application.
 * 
 * It implements the PropertyChangeListener Interface to get Events from class TaskCompareRunner.
 * 
 * @author Detlev Struebig
 * @since v0.1
 * 
 */
public class MainWindow extends JFrame implements PropertyChangeListener {

    /** serialize. */
    private static final long serialVersionUID = 2544089773508252477L;

    /** Logger. */
    private static Logger log = Logger.getLogger(MainWindow.class);

    private static AppPreferences prefs = AppPreferences.getInstance();

    /** WIDTH_MAIN_WINDOW. */
    public static final int WIDTH_MAIN_WINDOW = 700;

    /** HIGHT_MAIN_WINDOW. */
    public static final int HEIGHT_MAIN_WINDOW = 600;

    /** The common height of Controls (Text, Combo, etc) */
    public static final int CONTROL_HEIGHT = 25;

    /** Window Resource. */
    private ResourceBundle resources;

    /** Error Messages Resource. */
    private ResourceBundle resErrorMessages;

    /** Menu Bar. */
    private JMenuBar menuBar = new JMenuBar();

    /**   */
    private TextFieldDropTarget txtFileControlMaster;

    /**   */
    private TextFieldDropTarget txtFileControlSlave;

    /**   */
    private JButton btnOpenChooserMaster;

    /**   */
    private JButton btnOpenChooserSlave;

    /**   */
    private JComboBox cboSheetsMaster;

    /**   */
    private JComboBox cboSheetsSlave;

    /** Checkbox to decide, that Master and Slave File are the same. */
    private JCheckBox chkSlaveSameAsMaster;

    /** Add Report Sheets additionally to Master Sheet. */
    private JCheckBox chkReportSheetsInMaster;

    /** Start Compare Button. */
    private JButton btnCompare;

    /** Button to start Setting Dialog. */
    private JButton btnSetting;

    /***/
    private JRadioButton radioNone;

    /***/
    private JRadioButton radioRange;

    /***/
    private JRadioButton radioDoublet;

    /***/
    private JRadioButton radioContains;

    /**
	 * The Panel contain Controls to configure a Grid Range.
	 */
    private JPanel panelCompareRange;

    /**
	 * The Panel contains Controls to configure two Columns or two Rows and the Sheet for Each.
	 */
    private JPanel panelCompareColRow;

    /** View Process Text. */
    private HyperlinkLabel lblProcess;

    /** progressBar. */
    private ProgressBar progressBar;

    /** Application Model */
    private MainModel model = new MainModel();

    /** Last used File Filter in FileChooser. */
    private FileFilter fileFilterLastUsed;

    /** System Tray */
    @SuppressWarnings("unused")
    private AppSystemTray tray;

    /** ASCII input Field for Column. */
    private JTextField txtRangeColStart;

    /** ASCII input Field for Column. */
    private JTextField txtRangeColEnd;

    /** Integer input for Row. */
    private JTextField txtRangeRowStart;

    /** Integer input for Row. */
    private JTextField txtRangeRowEnd;

    /** */
    private JComboBox cboColRowRef;

    /** String and Integer input */
    private JTextField txtCompareReferenceCol;

    /** String and Integer input */
    private JTextField txtCompareCompareCol;

    /** String and Integer input */
    private JTextField txtCompareRefrenceRow;

    /** String and Integer input */
    private JTextField txtCompareCompareRow;

    /** Compare Formula or the Value of Formula */
    private JCheckBox chkCompareFormulaValue;

    /**
	 * Constructor.
	 * @param configureLAF if true, than configure Look and feel.
	 */
    public MainWindow(boolean configureLAF) {
        super("EXcompCEL");
        try {
            resources = ResourceBundle.getBundle("gui/resource/MainWindow");
            resErrorMessages = ResourceBundle.getBundle("gui/resource/ErrorMessages");
        } catch (MissingResourceException e) {
            log.error(e);
            int res = MessageBox.ok(this, "Can't load Application Resources. The Application ends here.");
            if (res == JOptionPane.OK_OPTION) {
                return;
            }
        }
        init();
        tray = new AppSystemTray(this);
        if (configureLAF) {
            initLAF();
        }
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(WindowEvent e) {
                log.debug("windowOpened");
                getModel().initilize();
                super.windowOpened(e);
            }
        });
    }

    /**
	 * Constructor.
	 */
    public MainWindow() {
        this(true);
    }

    /**
	 * Get Model of MainWindow Data.
	 * @return {@link MainModel}
	 */
    public MainModel getModel() {
        return model;
    }

    /**
	 * Window Close Listener to get the closing Action from 
	 * Window Title cross Button. 
	 *  
	 * @author Detlev Struebig
	 * @since v0.5
	 *
	 */
    class WindowCloseListsner extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent e) {
            actionExit(e);
        }
    }

    /**
	 * Common Initialisation of this Window.
	 */
    private void init() {
        setLayout(new BorderLayout());
        buildMainWindowBase();
        setJMenuBar(initMenu());
        getContentPane().add(initContentPanel(), BorderLayout.CENTER);
        initPopUpMenuFileInput();
    }

    /**
	 * Create a Status Bar Panel
	 * @return {@link JPanel} Status Bar Panel.
	 */
    private JPanel buildStatusBar() {
        StatusBar statusBar = new StatusBar();
        statusBar.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                log.debug(evt);
            }
        });
        return statusBar;
    }

    /**
	 * 
	 */
    private void buildMainWindowBase() {
        setMinimumSize(new Dimension(WIDTH_MAIN_WINDOW, HEIGHT_MAIN_WINDOW));
        getModel().setPathLatestUse(prefs.getPathLatestUse());
        int x = prefs.getLocationOnScreenX();
        int y = prefs.getLocationOnScreenY();
        setLocation(x, y);
        int width = prefs.getWindowWidth();
        int height = prefs.getWindowHeight();
        setSize(width, height);
        setVisible(true);
        String versionNumber = Main.class.getPackage().getImplementationVersion();
        if (StringUtils.isEmpty(versionNumber)) {
            versionNumber = "Version: Source";
        }
        StringBuffer buf = new StringBuffer(resources.getString("Window.Title"));
        buf.append(" - ");
        buf.append(versionNumber);
        setTitle(buf.toString());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowCloseListsner());
        ImageIcon imgLogoIcon = Util.createImageIcon(this.getClass(), resources.getString("Common.Icon"), "Icon");
        setIconImage(imgLogoIcon.getImage());
    }

    /**
	 * Initialize Look & Feel of Application.
	 */
    private void initLAF() {
        String classLF = prefs.getSystemLookAndFeelClassName();
        try {
            log.debug("Look & Feel Class: " + classLF);
            UIManager.setLookAndFeel(classLF);
            SwingUtilities.updateComponentTreeUI(this);
        } catch (ClassNotFoundException e) {
            log.error(e);
        } catch (InstantiationException e) {
            log.error(e);
        } catch (IllegalAccessException e) {
            log.error(e);
        } catch (UnsupportedLookAndFeelException e) {
            log.error(e);
        }
    }

    /**
	 * Initialize Menu.
	 * 
	 * @return {@link JMenuBar}
	 */
    private JMenuBar initMenu() {
        JMenu fileMenu = new JMenu(resources.getString("Menu.File.Title"));
        fileMenu.setMnemonic(Util.convertCharacterToKeyEventCodeTest(resources.getString("Menu.File.Mnemonic")));
        JMenuItem menuItem = new JMenuItem(resources.getString("Menu.File.OpenHomeFolder.Title"));
        menuItem.setMnemonic(Util.convertCharacterToKeyEventCodeTest(resources.getString("Menu.File.OpenHomeFolder.Mnemonic")));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent evt) {
                log.debug("actionPerformed. " + evt.getActionCommand());
                actionOpenHomeFolder(evt);
            }
        });
        fileMenu.add(menuItem);
        menuItem = new JMenuItem(resources.getString("Menu.File.OpenFile.Title"));
        menuItem.setMnemonic(Util.convertCharacterToKeyEventCodeTest(resources.getString("Menu.File.OpenFile.Mnemonic")));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent evt) {
                log.debug("actionPerformed. " + evt.getActionCommand());
                actionOpenFolder(evt);
            }
        });
        fileMenu.add(menuItem);
        fileMenu.addSeparator();
        JMenuItem exitMenuItem = new JMenuItem(resources.getString("Menu.File.Quit.Title"));
        exitMenuItem.setMnemonic(Util.convertCharacterToKeyEventCodeTest(resources.getString("Menu.File.Quit.Mnemonic")));
        int iKeyStroke = Util.convertCharacterToKeyEventCodeTest(resources.getString("Menu.File.Quit.Accelerator"));
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(iKeyStroke, ActionEvent.CTRL_MASK));
        exitMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent evt) {
                log.debug("actionPerformed. " + evt.getActionCommand());
                actionExit(evt);
            }
        });
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        JMenu fileOption = new JMenu(resources.getString("Menu.Option.Title"));
        fileOption.setMnemonic(Util.convertCharacterToKeyEventCodeTest(resources.getString("Menu.Option.Mnemonic")));
        JMenuItem menuItemSetting = new JMenuItem(resources.getString("Menu.Option.Setting.Title"));
        menuItemSetting.setMnemonic(Util.convertCharacterToKeyEventCodeTest(resources.getString("Menu.Option.Setting.Mnemonic")));
        menuItemSetting.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent evt) {
                log.debug("actionPerformed. " + evt.getActionCommand());
                actionOptionSetting(evt);
            }
        });
        fileOption.add(menuItemSetting);
        JMenuItem menuItemResetWindow = new JMenuItem(resources.getString("Menu.Option.ResetWindow.Title"));
        menuItemResetWindow.setMnemonic(Util.convertCharacterToKeyEventCodeTest(resources.getString("Menu.Option.ResetWindow.Mnemonic")));
        menuItemResetWindow.setToolTipText(resources.getString("Menu.Option.ResetWindow.ToolTip"));
        menuItemResetWindow.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent evt) {
                log.debug("actionPerformed. " + evt.getActionCommand());
                actionOptionResetWindow(evt);
            }
        });
        fileOption.add(menuItemResetWindow);
        menuBar.add(fileOption);
        JMenuItem menuItemResetAll = new JMenuItem(resources.getString("Menu.Option.ResetAll.Title"));
        menuItemResetAll.setMnemonic(Util.convertCharacterToKeyEventCodeTest(resources.getString("Menu.Option.ResetAll.Mnemonic")));
        menuItemResetAll.setToolTipText(resources.getString("Menu.Option.ResetAll.ToolTip"));
        menuItemResetAll.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent evt) {
                log.debug("actionPerformed. " + evt.getActionCommand());
                actionOptionResetAll(evt);
            }
        });
        fileOption.add(menuItemResetAll);
        menuBar.add(fileOption);
        JMenu fileHelp = new JMenu(resources.getString("Menu.Help.Title"));
        fileHelp.setMnemonic(Util.convertCharacterToKeyEventCodeTest(resources.getString("Menu.Help.Mnemonic")));
        JMenuItem menuItemAbout = new JMenuItem(resources.getString("Menu.Help.About.Title"));
        menuItemAbout.setMnemonic(Util.convertCharacterToKeyEventCodeTest(resources.getString("Menu.Help.About.Mnemonic")));
        menuItemAbout.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent evt) {
                log.debug("actionPerformed. " + evt.getActionCommand());
                actionAbout(evt);
            }
        });
        fileHelp.add(menuItemAbout);
        menuBar.add(fileHelp);
        return menuBar;
    }

    /**
	 * Reset all Settings.
	 * @param evt {@link ActionEvent}
	 */
    protected void actionOptionResetAll(ActionEvent evt) {
        log.debug("actionOptionResetAll");
        try {
            prefs.resetAppPreferences();
            prefs.save();
        } catch (BackingStoreException e) {
            log.error(e);
        }
    }

    /**
	 * Reset Window Size.
	 * @param evt {@link ActionEvent}
	 */
    protected void actionOptionResetWindow(ActionEvent evt) {
        log.debug("actionOptionResetWindow");
        setSize(WIDTH_MAIN_WINDOW, HEIGHT_MAIN_WINDOW);
        prefs.setLocationOnScreenX(WIDTH_MAIN_WINDOW);
        prefs.setLocationOnScreenY(HEIGHT_MAIN_WINDOW);
    }

    /**
	 * Open File Dialog comes up.
	 * @param evt {@link ActionEvent}
	 */
    protected void actionOpenFolder(ActionEvent evt) {
        log.debug("actionOpenFolder");
        if (!Desktop.isDesktopSupported()) {
            log.info("Open Folder is not supported by the OS.");
            MessageBox.ok(this, resErrorMessages.getString("MainWindow.File.Folder.CantOpenFolder"));
        }
        JFileChooser fc;
        fc = createJFileChooser();
        fc.setDialogTitle(resources.getString("Chooser.FileOpen.Title"));
        int ret = fc.showOpenDialog(this);
        switch(ret) {
            case JFileChooser.CANCEL_OPTION:
                break;
            case JFileChooser.APPROVE_OPTION:
                File file = fc.getSelectedFile();
                log.debug("actionOpenFolder:" + file.getAbsolutePath());
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    log.error(e);
                    MessageBox.okError(this, "Can't open Folder '" + file.getAbsolutePath() + "'\nError:" + e.getMessage());
                }
                break;
            case JFileChooser.ERROR_OPTION:
                break;
            default:
                log.warn("File Chooser ends with wrong Return Code. Code=" + ret);
        }
    }

    /**
	 * Open Home Folder in Explorer.
	 * @param evt {@link ActionEvent}
	 */
    protected void actionOpenHomeFolder(ActionEvent evt) {
        log.debug("actionOpenHomeFolder");
        if (Desktop.isDesktopSupported()) {
            File fHome = Util.getDefaultPath();
            try {
                FolderOpener opener = FolderOpenerFactory.createFolderOpener();
                if (opener != null) {
                    opener.openFolder(fHome);
                } else {
                    MessageBox.ok(this, resErrorMessages.getString("MainWindow.File.Folder.CantOpenFolderManager"));
                }
            } catch (IOException e) {
                log.error(e);
                MessageBox.okError(this, resErrorMessages.getString("MainWindow.File.Folder.CantOpenHomeFolder") + "\nErro:" + e.getMessage());
            } catch (Exception e) {
                log.error(e, e);
                MessageBox.okError(this, e.getMessage());
            }
        } else {
            log.warn("Can't open File Manager to open default Home Folder. Desktop is not supported.");
        }
    }

    /**
	 * Open Setting Dialog.
	 * @param evt {@link AWTEvent}
	 */
    protected final void actionOptionSetting(AWTEvent evt) {
        log.debug("actionOptionSetting");
        SettingDialog dlg = new SettingDialog(this);
        dlg.setVisible(true);
    }

    protected void actionAbout(AWTEvent evt) {
        log.debug("actionAbout");
        AboutDialog dlgAbout = new AboutDialog(this);
        dlgAbout.setVisible(true);
    }

    /**
	 * Create PopupMenu for Files Input Controls to
	 * to open the File of Input Control. 
	 */
    private void initPopUpMenuFileInput() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem(resources.getString("Popup.Menu.Open"));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent evt) {
                log.debug("actionPerformed. " + evt.getActionCommand());
                actionOpenFileMaster(evt);
            }
        });
        popupMenu.add(menuItem);
        txtFileControlMaster.addMouseListener(new PopupListener(popupMenu));
        popupMenu = new JPopupMenu();
        menuItem = new JMenuItem(resources.getString("Popup.Menu.Open"));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent evt) {
                log.debug("actionPerformed. " + evt.getActionCommand());
                actionOpenFileSlave(evt);
            }
        });
        popupMenu.add(menuItem);
        txtFileControlSlave.addMouseListener(new PopupListener(popupMenu));
    }

    /**
	 * actionOpenFileMaster.
	 * @param evt {@link ActionEvent}
	 */
    protected void actionOpenFileMaster(ActionEvent evt) {
        log.debug("actionOpenFileMaster." + evt.getActionCommand());
        openFileViaDesktop(getModel().getFilenameMaster());
    }

    /**
	 * 
	 * @param filename 
	 */
    private void openFileViaDesktop(String filename) {
        try {
            log.info("Context Menu - Input File Control - Open File : " + filename);
            File fFile = new File(filename);
            if (fFile.exists()) {
                Desktop.getDesktop().open(fFile);
            } else {
                MessageBox.okError(this, resErrorMessages.getString("MainWindow.File.Common.FileDoesNotExist"));
            }
        } catch (IOException e) {
            log.error(e);
        }
    }

    /**
	 * actionOpenFileSlave.
	 * @param evt {@link ActionEvent}
	 */
    protected void actionOpenFileSlave(ActionEvent evt) {
        log.debug("actionOpenFileMaster." + evt.getActionCommand());
        openFileViaDesktop(getModel().getFilenameSlave());
    }

    /**
	 * PopupListener to Start Context Menu.
	 * @author Detlev Struebig
	 * @since v0.2
	 */
    class PopupListener extends MouseAdapter {

        /** Logger. */
        private Logger log = Logger.getLogger(PopupListener.class);

        private JPopupMenu popup;

        public PopupListener(JPopupMenu popupMenu) {
            popup = popupMenu;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            log.debug("mousePressed");
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            log.debug("mouseReleased");
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                log.debug("maybeShowPopup");
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    /**
	 * Initialize GUI Controls-
	 */
    private void initControls() {
        BeanAdapter<MainModel> adapter = new BeanAdapter<MainModel>(getModel(), true);
        final ValueModel vmGridFileType = adapter.getValueModel(MainModel.PROPERTY_IsGridFileType);
        final ValueModel vmSameAsMaster = adapter.getValueModel(MainModel.PROPERTY_IsSameAsMaster);
        final ValueModel vmSameAsMasterConvert = ConverterFactory.createBooleanNegator(vmSameAsMaster);
        final ValueModel vmRadioSelectionModel = adapter.getValueModel(MainModel.PROPERTY_RadioGridAction);
        final ValueModel vmIsColRowPanelVisibleContain = adapter.getValueModel(MainModel.PROPERTY_IsRadioGridActionEnableContain);
        final ValueModel vmIsColRowPanelVisibleRange = adapter.getValueModel(MainModel.PROPERTY_IsRadioGridActionEnableRange);
        FileDrop.Listener listenerDropMaster = new FileDrop.Listener() {

            public void filesDropped(final File[] files) {
                for (int i = 0; i < files.length; i++) {
                    try {
                        getModel().setFilenameMaster(files[i].getCanonicalPath());
                        updateCbo(cboSheetsMaster, txtFileControlMaster.getText(), getModel().getListSheetNameMaster());
                    } catch (IOException e) {
                        log.error(e);
                    }
                }
            }
        };
        txtFileControlMaster = new TextFieldDropTarget(listenerDropMaster);
        Util.setPreferredSize(txtFileControlMaster, 30);
        txtFileControlMaster.setColumns(30);
        final ValueModel vmFilenameMaster = adapter.getValueModel(MainModel.PROPERTY_FilenameMaster);
        final ValueModel vmSheetnameMaster = adapter.getValueModel(MainModel.PROPERTY_SheetnameMaster);
        final SelectionInList<String> vmListCboMaster = new SelectionInList<String>(getModel().getListSheetNameMaster());
        final ValueModel vmEnableCboMaster = adapter.getValueModel(MainModel.PROPERTY_IsEnableCboMaster);
        Bindings.bind(txtFileControlMaster, vmFilenameMaster);
        Bindings.bind(txtFileControlMaster, "toolTipText", vmFilenameMaster);
        btnOpenChooserMaster = new JButton(resources.getString("Button.OpenFile.Title"));
        btnOpenChooserMaster.setToolTipText(resources.getString("Button.OpenFile.ToolTipText"));
        btnOpenChooserMaster.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent evt) {
                if (btnOpenChooserMaster.isEnabled()) {
                    log.debug("actionPerformed. " + evt.getActionCommand());
                    actionOpenExcel(evt, eMASERT_SLAVE.MASTER);
                }
            }
        });
        cboSheetsMaster = BasicComponentFactory.createComboBox(vmListCboMaster);
        Util.setPreferredSize(cboSheetsMaster, 200);
        cboSheetsMaster.setToolTipText(resources.getString("Combobox.Sheets.ToolTipText"));
        Bindings.bind(cboSheetsMaster, "enabled", vmEnableCboMaster);
        JTextField tfDummyMaster = BasicComponentFactory.createTextField(vmListCboMaster.getSelectionHolder());
        Bindings.bind(tfDummyMaster, vmSheetnameMaster);
        final ValueModel vmFilenameSlave = adapter.getValueModel(MainModel.PROPERTY_FilenameSlave);
        final ValueModel vmSheetnameSlave = adapter.getValueModel(MainModel.PROPERTY_SheetnameSlave);
        final SelectionInList<String> vmListCboSlave = new SelectionInList<String>(getModel().getListSheetNameSlave());
        final ValueModel vmEnableCboSlave = adapter.getValueModel(MainModel.PROPERTY_IsEnableCboSlave);
        FileDrop.Listener listenerDropSlave = new FileDrop.Listener() {

            public void filesDropped(final File[] files) {
                for (int i = 0; i < files.length; i++) {
                    try {
                        getModel().setFilenameSlave(files[i].getCanonicalPath());
                        updateCbo(cboSheetsSlave, txtFileControlSlave.getText(), getModel().getListSheetNameSlave());
                    } catch (IOException e) {
                        log.error(e);
                    }
                }
            }
        };
        txtFileControlSlave = new TextFieldDropTarget(listenerDropSlave);
        Util.setPreferredSize(txtFileControlSlave, 30);
        txtFileControlSlave.setColumns(30);
        Bindings.bind(txtFileControlSlave, vmFilenameSlave);
        Bindings.bind(txtFileControlSlave, "toolTipText", vmFilenameSlave);
        Bindings.bind(txtFileControlSlave, "enabled", vmSameAsMasterConvert);
        btnOpenChooserSlave = new JButton(resources.getString("Button.OpenFile.Title"));
        btnOpenChooserSlave.setToolTipText(resources.getString("Button.OpenFile.ToolTipText"));
        btnOpenChooserSlave.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent evt) {
                if (btnOpenChooserSlave.isEnabled()) {
                    log.debug("actionPerformed. " + evt.getActionCommand());
                    actionOpenExcel(evt, eMASERT_SLAVE.SLAVE);
                    updateCbo(cboSheetsSlave, getModel().getFilenameSlave(), getModel().getListSheetNameSlave());
                }
            }
        });
        Bindings.bind(btnOpenChooserSlave, "enabled", vmSameAsMasterConvert);
        cboSheetsSlave = BasicComponentFactory.createComboBox(vmListCboSlave);
        Bindings.bind(cboSheetsSlave, "enabled", vmEnableCboSlave);
        Util.setPreferredSize(cboSheetsSlave, 200);
        cboSheetsSlave.setToolTipText(resources.getString("Combobox.Sheets.ToolTipText"));
        JTextField tfDummySlave = BasicComponentFactory.createTextField(vmListCboSlave.getSelectionHolder());
        Bindings.bind(tfDummySlave, vmSheetnameSlave);
        FlowLayout flm = new FlowLayout();
        flm.setAlignment(FlowLayout.LEFT);
        JPanel pnlSameAsMaster = new JPanel(flm);
        chkSlaveSameAsMaster = BasicComponentFactory.createCheckBox(vmSameAsMaster, "");
        Bindings.bind(chkSlaveSameAsMaster, "enabled", vmGridFileType);
        chkSlaveSameAsMaster.setToolTipText(resources.getString("Checkbox.SameAs.Master.ToolTip"));
        chkSlaveSameAsMaster.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                log.debug("chkSlaveSameAsMaster:" + e.getStateChange());
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    log.debug("chkSlaveSameAsMaster State=" + e.getStateChange());
                    model.setFilenameSlave(model.getFilenameMaster());
                    updateCbo(cboSheetsSlave, model.getFilenameSlave(), getModel().getListSheetNameSlave());
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    log.debug("chkSlaveSameAsMaster State=" + e.getStateChange());
                    model.setFilenameSlave("");
                    updateCbo(cboSheetsSlave, model.getFilenameSlave(), getModel().getListSheetNameSlave());
                }
            }
        });
        pnlSameAsMaster.add(chkSlaveSameAsMaster);
        JPanel pnlCompareFormulaValue = new JPanel(flm);
        final ValueModel vmCompareFormulaValue = adapter.getValueModel(MainModel.PROPERTY_CompareFormula);
        chkCompareFormulaValue = BasicComponentFactory.createCheckBox(vmCompareFormulaValue, "");
        Bindings.bind(chkCompareFormulaValue, "enabled", vmGridFileType);
        chkCompareFormulaValue.setToolTipText(resources.getString("Checkbox.CompareFormulaValue.Master.ToolTip"));
        pnlCompareFormulaValue.add(chkCompareFormulaValue);
        flm = new FlowLayout();
        flm.setAlignment(FlowLayout.LEFT);
        JPanel pnlReportInMaster = new JPanel(flm);
        final ValueModel vmReportSheetsInMaster = adapter.getValueModel(MainModel.PROPERTY_IsReportSheetsInMaster);
        chkReportSheetsInMaster = BasicComponentFactory.createCheckBox(vmReportSheetsInMaster, "");
        Bindings.bind(chkReportSheetsInMaster, vmReportSheetsInMaster);
        Bindings.bind(chkReportSheetsInMaster, "enabled", vmGridFileType);
        chkReportSheetsInMaster.setToolTipText(resources.getString("Checkbox.ReportInMaster.Master.ToolTip"));
        pnlReportInMaster.add(chkReportSheetsInMaster);
        btnSetting = new JButton();
        Bindings.bind(btnSetting, "enabled", vmReportSheetsInMaster);
        btnSetting.setText(resources.getString("Button.ReportInMaster.Master.SettingButton.Label"));
        btnSetting.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent evt) {
                if (btnSetting.isEnabled()) {
                    log.debug("actionPerformed. " + evt.getActionCommand());
                    actionOptionSetting(evt);
                }
            }
        });
        pnlReportInMaster.add(btnSetting);
        radioNone = BasicComponentFactory.createRadioButton(vmRadioSelectionModel, eGridAction.NONE, resources.getString("RadioAction.None"));
        Bindings.bind(radioNone, "enabled", vmGridFileType);
        radioRange = BasicComponentFactory.createRadioButton(vmRadioSelectionModel, eGridAction.RANGE, resources.getString("RadioAction.Compare"));
        Bindings.bind(radioRange, "enabled", vmGridFileType);
        radioDoublet = BasicComponentFactory.createRadioButton(vmRadioSelectionModel, eGridAction.DOUBLET, resources.getString("RadioAction.Doublet"));
        Bindings.bind(radioDoublet, "enabled", vmGridFileType);
        radioContains = BasicComponentFactory.createRadioButton(vmRadioSelectionModel, eGridAction.CONTAINS, resources.getString("RadioAction.Contains"));
        Bindings.bind(radioContains, "enabled", vmGridFileType);
        final ValueModel vmRangeColStart = adapter.getValueModel(MainModel.PROPERTY_RangeColStart);
        final ValueModel vmRangeColEnd = adapter.getValueModel(MainModel.PROPERTY_RangeColEnd);
        final ValueModel vmRangeRowStart = adapter.getValueModel(MainModel.PROPERTY_RangeRowStart);
        final ValueModel vmRangeRowEnd = adapter.getValueModel(MainModel.PROPERTY_RangeRowEnd);
        txtRangeColStart = BasicComponentFactory.createTextField(vmRangeColStart);
        Util.setPreferredSize(txtRangeColStart, 50, CONTROL_HEIGHT);
        Bindings.bind(txtRangeColStart, "enabled", vmGridFileType);
        txtRangeColEnd = BasicComponentFactory.createTextField(vmRangeColEnd);
        Util.setPreferredSize(txtRangeColEnd, 50, CONTROL_HEIGHT);
        Bindings.bind(txtRangeColEnd, "enabled", vmGridFileType);
        txtRangeRowStart = BasicComponentFactory.createIntegerField(vmRangeRowStart, 0);
        Util.setPreferredSize(txtRangeRowStart, 50, CONTROL_HEIGHT);
        Bindings.bind(txtRangeRowStart, "enabled", vmGridFileType);
        txtRangeRowEnd = BasicComponentFactory.createIntegerField(vmRangeRowEnd, 0);
        Util.setPreferredSize(txtRangeRowEnd, 50, CONTROL_HEIGHT);
        Bindings.bind(txtRangeRowEnd, "enabled", vmGridFileType);
        panelCompareRange = new JPanel();
        panelCompareRange.setLayout(new GridLayout(2, 4));
        panelCompareRange.add(new JLabel(resources.getString("RadioAction.Range.ColStart")));
        panelCompareRange.add(txtRangeColStart);
        panelCompareRange.add(new JLabel(resources.getString("RadioAction.Range.ColEnd")));
        panelCompareRange.add(txtRangeColEnd);
        panelCompareRange.add(new JLabel(resources.getString("RadioAction.Range.RowStart")));
        panelCompareRange.add(txtRangeRowStart);
        panelCompareRange.add(new JLabel(resources.getString("RadioAction.Range.RowEnd")));
        panelCompareRange.add(txtRangeRowEnd);
        Bindings.bind(panelCompareRange, "enabled", vmGridFileType);
        Bindings.bind(panelCompareRange, "visible", vmIsColRowPanelVisibleRange);
        final ValueModel vmRadioColRowViewControlsROW = adapter.getValueModel(MainModel.PROPERTY_RadioColRowViewControls);
        final ValueModel vmRadioColRowViewControlsCOL = ConverterFactory.createBooleanNegator(vmRadioColRowViewControlsROW);
        final ValueModel vmCompareCompareCol = adapter.getValueModel(MainModel.PROPERTY_CompareCompareCol);
        final ValueModel vmCompareReferenceCol = adapter.getValueModel(MainModel.PROPERTY_CompareReferenceCol);
        final ValueModel vmCompareRefrenceRow = adapter.getValueModel(MainModel.PROPERTY_CompareReferenceRow);
        final ValueModel vmCompareCompareRow = adapter.getValueModel(MainModel.PROPERTY_CompareCompareRow);
        cboColRowRef = new JComboBox();
        final ValueModel vmCompareColRow = adapter.getValueModel(MainModel.PROPERTY_CompareColRow);
        cboColRowRef.setModel(new ComboBoxAdapter<Object>(MainModel.eGridPositionRangeType_CHOICES, vmCompareColRow));
        Util.setPreferredSize(cboColRowRef, 70, CONTROL_HEIGHT);
        Bindings.bind(cboColRowRef, "enabled", vmGridFileType);
        txtCompareReferenceCol = BasicComponentFactory.createTextField(vmCompareReferenceCol);
        Util.setPreferredSize(txtCompareReferenceCol, 40, CONTROL_HEIGHT);
        Bindings.bind(txtCompareReferenceCol, "enabled", vmGridFileType);
        Bindings.bind(txtCompareReferenceCol, "visible", vmRadioColRowViewControlsCOL);
        txtCompareCompareCol = BasicComponentFactory.createTextField(vmCompareCompareCol);
        Util.setPreferredSize(txtCompareCompareCol, 40, CONTROL_HEIGHT);
        Bindings.bind(txtCompareCompareCol, "enabled", vmGridFileType);
        Bindings.bind(txtCompareCompareCol, "visible", vmRadioColRowViewControlsCOL);
        txtCompareRefrenceRow = BasicComponentFactory.createIntegerField(vmCompareRefrenceRow, 0);
        Util.setPreferredSize(txtCompareRefrenceRow, 40, CONTROL_HEIGHT);
        Bindings.bind(txtCompareRefrenceRow, "enabled", vmGridFileType);
        Bindings.bind(txtCompareRefrenceRow, "visible", vmRadioColRowViewControlsROW);
        txtCompareCompareRow = BasicComponentFactory.createIntegerField(vmCompareCompareRow, 0);
        Util.setPreferredSize(txtCompareCompareRow, 40, CONTROL_HEIGHT);
        Bindings.bind(txtCompareCompareRow, "enabled", vmGridFileType);
        Bindings.bind(txtCompareCompareRow, "visible", vmRadioColRowViewControlsROW);
        FormLayout layout = new FormLayout("left:PREF,left:PREF", "pref, 3dlu, pref, 3dlu, pref");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();
        int row = 1;
        builder.addLabel(resources.getString("RadioAction.Compare.ColRow"), cc.xy(1, row));
        builder.add(cboColRowRef, cc.xy(2, row));
        row += 2;
        builder.addLabel(resources.getString("RadioAction.Compare.RefColRow"), cc.xy(1, row));
        builder.add(txtCompareReferenceCol, cc.xy(2, row));
        builder.add(txtCompareRefrenceRow, cc.xy(2, row));
        row += 2;
        builder.addLabel(resources.getString("RadioAction.Compare.CompColRow"), cc.xy(1, row));
        builder.add(txtCompareCompareCol, cc.xy(2, row));
        builder.add(txtCompareCompareRow, cc.xy(2, row));
        panelCompareColRow = builder.getPanel();
        Bindings.bind(panelCompareColRow, "visible", vmIsColRowPanelVisibleContain);
        Bindings.bind(panelCompareColRow, "enabled", vmGridFileType);
        cboColRowRef.setSelectedIndex(0);
        btnCompare = new JButton(resources.getString("Button.Compare.Title"));
        btnCompare.setToolTipText(resources.getString("Button.Compare.ToolTip"));
        btnCompare.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent evt) {
                if (btnCompare.isEnabled()) {
                    log.debug("actionPerformed. " + evt.getActionCommand());
                    actionCompareExcelFiles(evt);
                }
            }
        });
        progressBar = new ProgressBar();
        Util.setPreferredSize(progressBar, 300);
        progressBar.setVisible(true);
        lblProcess = new HyperlinkLabel(resources.getString("Label.Result.Title"));
        Util.setPreferredSize(lblProcess, 300);
        lblProcess.setHorizontalTextPosition(SwingConstants.CENTER);
    }

    /**
	 * Init the Content Panel.
	 */
    private JPanel initContentPanel() {
        initControls();
        String colDef = "right:pref, 3dlu, left:PREF, 3dlu, left:pref";
        StringBuffer rowDefBuf = new StringBuffer();
        rowDefBuf.append("p, 3dlu");
        rowDefBuf.append(", p, 3dlu");
        rowDefBuf.append(", p, 3dlu");
        rowDefBuf.append(", p, 3dlu");
        rowDefBuf.append(", p, 3dlu");
        rowDefBuf.append(", p, 3dlu");
        rowDefBuf.append(", p, 3dlu");
        rowDefBuf.append(", p, 3dlu");
        rowDefBuf.append(", p, 3dlu");
        rowDefBuf.append(", p, 3dlu");
        rowDefBuf.append(", p, 3dlu");
        rowDefBuf.append(", p, 3dlu");
        rowDefBuf.append(", p, 3dlu");
        rowDefBuf.append(", p, 3dlu");
        rowDefBuf.append(", p, 3dlu");
        FormLayout layout = new FormLayout(colDef, rowDefBuf.toString());
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        layout.setColumnGroups(new int[][] { { 1, 5 }, { 3 } });
        CellConstraints cc = new CellConstraints();
        int row = 1;
        builder.addSeparator(resources.getString("Separator.Reference"), cc.xyw(1, row, 5));
        row += 2;
        builder.addLabel(resources.getString("Label.MasterFile.Title"), cc.xy(1, row));
        builder.add(txtFileControlMaster, cc.xy(3, row));
        builder.add(btnOpenChooserMaster, cc.xy(5, row));
        row += 2;
        builder.add(cboSheetsMaster, cc.xy(3, row));
        row += 2;
        builder.addSeparator(resources.getString("Separator.Compare"), cc.xyw(1, row, 5));
        row += 2;
        builder.addLabel(resources.getString("Label.SlaveFile.Title"), cc.xy(1, row));
        builder.add(txtFileControlSlave, cc.xy(3, row));
        builder.add(btnOpenChooserSlave, cc.xy(5, row));
        row += 2;
        builder.add(cboSheetsSlave, cc.xy(3, row));
        row += 2;
        builder.addSeparator(resources.getString("Separator.Grid"), cc.xyw(1, row, 5));
        row += 2;
        JPanel panelSlaveSameAsMaster = new JPanel();
        panelSlaveSameAsMaster.add(chkSlaveSameAsMaster);
        JLabel lblSameAsMaster = new JLabel(resources.getString("Checkbox.SameAs.Master.Label"));
        lblSameAsMaster.setToolTipText(resources.getString("Checkbox.SameAs.Master.ToolTip"));
        builder.add(lblSameAsMaster, cc.xy(1, row));
        builder.add(panelSlaveSameAsMaster, cc.xy(3, row));
        row += 2;
        JPanel panelCompareFormulaValue = new JPanel();
        panelCompareFormulaValue.add(chkCompareFormulaValue);
        JLabel lblCompareFormulaValue = new JLabel(resources.getString("Checkbox.CompareFormulaValue.Master.Label"));
        lblCompareFormulaValue.setToolTipText(resources.getString("Checkbox.CompareFormulaValue.Master.ToolTip"));
        builder.add(lblCompareFormulaValue, cc.xy(1, row));
        builder.add(panelCompareFormulaValue, cc.xy(3, row));
        row += 2;
        JPanel panelReportSheetsInMaster = new JPanel();
        panelReportSheetsInMaster.add(chkReportSheetsInMaster);
        panelReportSheetsInMaster.add(btnSetting);
        JLabel lblReportSheetsInMaster = new JLabel(resources.getString("Checkbox.ReportInMaster.Master.Label"));
        lblReportSheetsInMaster.setToolTipText(resources.getString("Checkbox.ReportInMaster.Master.ToolTip"));
        builder.add(lblReportSheetsInMaster, cc.xy(1, row));
        builder.add(panelReportSheetsInMaster, cc.xy(3, row));
        row += 2;
        builder.addLabel(resources.getString("RadioAction.Label"), cc.xy(1, row));
        JPanel panelRadio = new JPanel();
        panelRadio.add(radioNone);
        panelRadio.add(radioRange);
        panelRadio.add(radioDoublet);
        panelRadio.add(radioContains);
        builder.add(panelRadio, cc.xy(3, row));
        row += 2;
        builder.add(panelCompareRange, cc.xy(3, row));
        builder.add(panelCompareColRow, cc.xy(3, row));
        row += 4;
        builder.add(btnCompare, cc.xy(1, row));
        builder.add(progressBar, cc.xy(3, row));
        row += 2;
        builder.add(lblProcess, cc.xy(3, row));
        return builder.getPanel();
    }

    /**
	 * Refill Excel Sheet names Combobox.
	 * 
	 * @param cbo {@link JComboBox} The Combobox to fill with Sheet names.
	 * @param filename Filename of the Excel
	 * @param listCboData {@link List} MainModel Data of Sheets of Grid.
	 */
    protected void updateCbo(JComboBox cbo, String filename, List<String> listCboData) {
        log.debug("Update Combobox with Sheets from Excel File " + filename);
        if (Util.isExcelFile(filename) && new File(filename).exists()) {
            listCboData.clear();
            listCboData.addAll(UtilFileHandling.getSheets(filename));
            if (listCboData.size() > 0) {
                cbo.setSelectedIndex(0);
                cbo.updateUI();
            }
        } else {
            listCboData.clear();
            cbo.updateUI();
        }
    }

    /**
	 * Show File Open Dialog for Slave Excel File.
	 * 
	 * @param evt {@link ActionEvent}
	 */
    protected final void actionOpenExcel(final ActionEvent evt, eMASERT_SLAVE eMS) {
        log.info("actionOpenSlaveExcel Action");
        if (eMS.equals(eMASERT_SLAVE.MASTER)) {
            openFileChooser(cboSheetsMaster, txtFileControlMaster, getModel().getListSheetNameMaster());
        } else {
            openFileChooser(cboSheetsSlave, txtFileControlSlave, getModel().getListSheetNameSlave());
        }
    }

    /**
	 * Open File Chooser for Excel Files.
	 * 
	 * @param cbo {@link ComboBox} with Excel Sheet Names.
	 * @param txtControl The Excel File Input Control.
	 */
    private void openFileChooser(JComboBox cbo, TextFieldDropTarget txtControl, List<String> listCboData) {
        JFileChooser fc;
        fc = createJFileChooser();
        fc.setDialogTitle(resources.getString("Chooser.FileOpen.Excel.Title"));
        String txt = txtControl.getText();
        File txtFile = new File(txt);
        if (txtFile != null && txtFile.exists()) {
            log.debug("Open Folder previously set in Input Control.");
            fc.setCurrentDirectory(txtFile);
        } else {
            if (!StringUtils.isEmpty(getModel().getPathLatestUse())) {
                log.debug("Open Folder previously used.");
                fc.setCurrentDirectory(new File(getModel().getPathLatestUse()));
            }
        }
        int ret = fc.showOpenDialog(this);
        switch(ret) {
            case JFileChooser.CANCEL_OPTION:
                break;
            case JFileChooser.APPROVE_OPTION:
                File file = fc.getSelectedFile();
                String fullpath = file.getAbsolutePath();
                getModel().setFilenameMaster(fullpath);
                getModel().setPathLatestUse(file.getParent());
                log.debug("actionOpenMasterExcel:" + fullpath);
                fileFilterLastUsed = fc.getFileFilter();
                log.debug("Hold FileFilter: " + fileFilterLastUsed.getDescription());
                updateCbo(cbo, txtControl.getText(), listCboData);
                break;
            case JFileChooser.ERROR_OPTION:
                break;
            default:
                log.warn("File Cooser ends with wrong Return Code. Code=" + ret);
        }
    }

    /**
	 * Create a JFileChooser.
	 * 
	 * @return {@link JFileChooser}
	 */
    private JFileChooser createJFileChooser() {
        FileChooserPreselect fc = new FileChooserPreselect(fileFilterLastUsed);
        fc.setCurrentDirectory(Util.getDefaultPath());
        return fc;
    }

    /**
	 * Event destination to start the comparison of the Reference and Comparer Excel File.
	 * 
	 * @param evt {@link ActionEvent}
	 */
    protected final void actionCompareExcelFiles(final ActionEvent evt) {
        log.debug("actionCompareExcelFiles");
        StringBuffer buf = new StringBuffer();
        buf.append("Start comparison of Master Excel '");
        buf.append(getModel().getFilenameMaster());
        buf.append("' and Slave Excel '");
        buf.append(getModel().getFilenameSlave());
        buf.append("'.");
        log.info(buf);
        startComparison();
    }

    /**
	 * Cause Exist of this Application.
	 * @param evt {@link ActionEvent}
	 */
    public final void exit(ActionEvent evt) {
        actionExit(evt);
    }

    /**
	 * 'Exit Application' Event destination 
	 * 
	 * @param evt {@link AWTEvent}
	 */
    protected final void actionExit(final AWTEvent evt) {
        log.info("actionExit");
        log.debug(this.getLocationOnScreen().toString());
        prefs.setLocationOnScreenX(getLocationOnScreen().x);
        prefs.setLocationOnScreenY(getLocationOnScreen().y);
        prefs.setWindowWidth(getWidth());
        prefs.setWindowHeight(getHeight());
        prefs.setPathLatestUse(getModel().getPathLatestUse());
        prefs.save();
        System.exit(0);
    }

    /**
	 * Invoked when task's progress property changes.
	 * 
	 * @param evt The {@link PropertyChangeEvent}.
	 */
    public final void propertyChange(final PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        }
    }

    /**
	 * Start the comparison of the Master and Slave File.
	 */
    private final void startComparison() {
        if (!getModel().isValid()) {
            String message = StringUtils.join(getModel().getErrorList(), "\n");
            MessageBox.okError(this, "Your Setting is not valid.\n" + message);
            return;
        }
        if (StringUtils.isEmpty(getModel().getFilenameMaster())) {
            MessageBox.okError(this, resErrorMessages.getString("MainWindow.File.Common.MasterIsNullOrEmpty"));
            return;
        }
        if (StringUtils.isEmpty(getModel().getFilenameSlave())) {
            MessageBox.okError(this, resErrorMessages.getString("MainWindow.File.Common.SlaveIsNullOrEmpty"));
            return;
        }
        File master = new File(getModel().getFilenameMaster());
        if (!master.exists() || !master.isFile() || !master.canRead()) {
            MessageBox.okError(this, resErrorMessages.getString("MainWindow.File.Common.CantOpenFile") + " '" + getModel().getFilenameMaster() + "'");
            return;
        }
        File slave = new File(getModel().getFilenameSlave());
        if (!slave.exists() || !slave.isFile() || !slave.canRead()) {
            MessageBox.okError(this, resErrorMessages.getString("MainWindow.File.Common.CantOpenFile") + " '" + getModel().getFilenameSlave() + "'");
            return;
        }
        if (!Util.getFileExtension(getModel().getFilenameMaster()).equals(Util.getFileExtension(getModel().getFilenameSlave()))) {
            MessageBox.okError(this, resErrorMessages.getString("MainWindow.File.Common.DifferentFileExtension"));
            return;
        }
        try {
            progressBar.setVisible(true);
            TaskCompareRunner task = new TaskCompareRunner(getModel(), progressBar, lblProcess);
            task.addPropertyChangeListener(this);
            task.execute();
        } catch (Exception e) {
            log.error(e);
        }
    }
}
