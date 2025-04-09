package org.plantstreamer;

import java.awt.AWTException;
import java.util.logging.LogRecord;
import org.communications.ConnectionStatusChangeEvent;
import java.net.MalformedURLException;
import java.util.logging.Level;
import org.opcda2out.OPCBackupManager.MANAGERSTATUS;
import org.communications.CommunicationManager.STATUS;
import org.communications.ConnectionListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.opcda2out.OPCBkManagerStatusChangeEvent;
import org.opcda2out.OPCBkManagerStatusListener;
import org.plantstreamer.export.opclist.OPCListExportDialog;
import org.plantstreamer.opc.OPCConnectionInfoDialog;
import org.plantstreamer.opc.ServerStateDialog;
import org.plantstreamer.output.OutputTypeHandler;
import swingextras.action.ActionX;
import swingextras.action.ActionXData;
import swingextras.AutoCompletionManager;
import swingextras.GuiUtils;
import swingextras.action.ActionMenuItem;
import swingextras.gui.DialogAbout;
import swingextras.gui.SimpleStatusBar;
import swingextras.gui.SingleDialog;
import swingextras.gui.logtable.LogDialog;
import swingextras.gui.logtable.LoggHandler;
import swingextras.gui.logtable.SystemTrayLogHandler;
import swingextras.license.Licenses.LICENSES;
import swingextras.icons.IconManager;
import swingextras.icons.ResizableIcon;
import swingextras.Library;

/**
 * Plantstreamer's Main Window
 * @author Joao Leal
 */
@SuppressWarnings("serial")
public class MainWindow extends JFrame implements ConnectionListener, OPCBkManagerStatusListener {

    private static final ResourceBundle bundle = ResourceBundle.getBundle("org/plantstreamer/i18n/common");

    /**
     * The package information
     */
    public static final Package PACKAGE = MainWindow.class.getPackage();

    /**
     * Dialog about
     */
    private final SingleDialog aboutDialog = new SingleDialog() {

        @Override
        protected JDialog createDialog(Window parent) {
            DialogAbout da = new DialogAbout(parent);
            da.setLibraries(new Library[] { new Library("PostgreSQL", "http://jdbc.postgresql.org/", "The PostgreSQL JDBC driver", LICENSES.BSD, "PostgreSQL Global Development Group", "1997-2005"), new Library("SwingX", "http://swinglabs.org/", "Contains extensions to the Swing GUI toolkit", LICENSES.LGPL2_1, "Sun Microsystems, Inc., 4150 Network Circle, Santa Clara, California 95054, U.S.A.", "2005-2009"), new Library("Substance", "https://substance.dev.java.net/", "Configurable and customizable production-quality Java look and feel library for Swing applications", LICENSES.BSD, "Kirill Grouchnikov and contributors", "2005-2009"), new Library("OpenSCADA", "http://openscada.org/UtgardProject", "Independent OPC library for Java", LICENSES.GPL2_0, "inavare GmbH (http://inavare.com)", "2006-2007"), new Library("Jasypt", "http://www.jasypt.org/", "Java encryption library", LICENSES.APACHE2_0, "The JASYPT team", "2007-2008") });
            da.setImageAbout(IconManager.getIcon("logo.png"));
            String title = PACKAGE.getImplementationTitle();
            da.setPackageDescripton(MessageFormat.format(bundle.getString("{0}_is_a_program_that_collects_data_from_an_OPC_sever_using_OPC-DA_2.0_and_stores_it_into_a_persistent_backend"), title));
            da.setPackageLicenseSmallDescripton(bundle.getString("Developed_under_the_GNU_General_Public_License_v3") + "<br/>" + MessageFormat.format(bundle.getString("Copyright_{0}_Ciengis_All_rights_reserved"), "2007-2010"));
            Package p = getClass().getPackage();
            da.setProjectName(p.getImplementationTitle());
            da.setProjectVersion(p.getImplementationVersion());
            da.setPackageLicense(LICENSES.GPL3_0.getNotice(MessageFormat.format(bundle.getString("{0}_is_a_program_that_collects_data_from_an_OPC_sever_using_OPC-DA_2.0_and_stores_it_into_a_persistent_backend"), title), "Ciengis", "2007-2010"));
            da.addLicense(LICENSES.GPL3_0.getLicense());
            try {
                da.setProjectHomepage(new URL("http://www.ciengis.com/"));
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
            return da;
        }
    };

    /**
     * Log dialog
     */
    private final SingleDialog logDialog = new SingleDialog() {

        @Override
        protected JDialog createDialog(Window parent) {
            LogDialog d = new LogDialog(parent);
            d.setApplicationName(PACKAGE.getImplementationTitle());
            return d;
        }
    };

    /**
     * Connection manager Dialog
     */
    private final SingleDialog connectManDialog = new SingleDialog() {

        @Override
        protected JDialog createDialog(Window parent) {
            return new ConnectionManagerDialog(parent);
        }
    };

    /**
     * OPC server state dialog
     */
    private final SingleDialog opcServerStateDialog = new SingleDialog() {

        @Override
        protected JDialog createDialog(Window parent) {
            return new ServerStateDialog(parent, Main.opc);
        }
    };

    /**
     * OPC server connection information dialog
     */
    private final SingleDialog opcConnectInfoDialog = new SingleDialog() {

        @Override
        protected JDialog createDialog(Window parent) {
            return new OPCConnectionInfoDialog(parent, Main.opc);
        }
    };

    private final ActionX openOPCInfo = new ActionX(new ActionXData("openOPCInfoDialog")) {

        @Override
        public void actionPerformed(ActionEvent e) {
            opcConnectInfoDialog.show(MainWindow.this);
        }
    };

    /**
     * Closes the application
     */
    private final ActionX exit = new ActionX(new ActionXData("exit")) {

        @Override
        public void actionPerformed(ActionEvent e) {
            closePlantstreamer();
        }
    };

    /**
     * Closes the application
     */
    private final ActionX restore = new ActionX(new ActionXData("openWindow")) {

        @Override
        public void actionPerformed(ActionEvent e) {
            setVisible(true);
            if (MainWindow.this.getState() == Frame.ICONIFIED) {
                MainWindow.this.setState(Frame.NORMAL);
            }
            toFront();
            requestFocus();
        }
    };

    /** Creates new form MainWindow */
    public MainWindow() {
        super();
        final Logger[] loggers = new Logger[] { Logger.getLogger("org.communications"), Logger.getLogger("org.database"), Logger.getLogger("org.opccom"), Logger.getLogger("org.opcda2out"), Logger.getLogger("swingextras"), Logger.getLogger("org.plantstreamer") };
        new LoggHandler(this, Main.mainFolder, "org.plantstreamer", loggers);
        setTitle(PACKAGE.getImplementationTitle());
        List<Image> icons = new ArrayList<Image>(5);
        icons.add(IconManager.getIcon("16x16/logo.png").getImage());
        icons.add(IconManager.getIcon("22x22/logo.png").getImage());
        icons.add(IconManager.getIcon("32x32/logo.png").getImage());
        icons.add(IconManager.getIcon("48x48/logo.png").getImage());
        icons.add(IconManager.getIcon("64x64/logo.png").getImage());
        setIconImages(icons);
        initComponents();
        jMenuItemSave.setAction(Main.openOptFile.saveAction);
        jMenuItemSaveAs.setAction(Main.openOptFile.saveAsAction);
        jMenuIOpen.setAction(Main.openOptFile.openAction);
        jMenuItemExit.setAction(exit);
        jMenuItemOPCConInfo.setAction(openOPCInfo);
        jMenuItemOPC2Out.setAction(tagSelection.startStopOPC2OutAction);
        jMenuItemOPC.setAction(tagSelection.toggleOPCConnection);
        jMenuItemCreateComposite.setAction(tagSelection.createCompositeTag);
        String s = MessageFormat.format(bundle.getString("About_{0}"), PACKAGE.getImplementationTitle());
        jMenuItemAbout.setText(s);
        jMenuItemAbout.setToolTipText(s);
        jMenuItemAbout.setIcon(IconManager.getIcon("16x16/logo.png"));
        s = MessageFormat.format(bundle.getString("{0}_Webpage..."), PACKAGE.getImplementationTitle());
        jMenuItemWebPage.setText(s);
        jMenuItemClearSelection.setAction(tagSelection.clearAction);
        jMenuItemFind.setAction(tagSelection.findAction);
        jMenuItemAddItem.setAction(tagSelection.addNewAction);
        jMenuItemRemoveItem.setAction(tagSelection.removeAction);
        Main.options.autoReconnect.setSelected(true);
        jCheckBoxMenuItemAutoRecon.setAction(Main.options.autoReconnect);
        Main.options.autoFetchOPCTree.setSelected(true);
        jCheckBoxMenuAutoFetch.setAction(Main.options.autoFetchOPCTree);
        jCheckBoxAsync.setAction(Main.options.asynchronous);
        jCheckBoxProperties.setAction(Main.options.saveProperties);
        jRadioButtonMenuItemPassAuto.setAction(Main.options.savePasswordAuto);
        jRadioButtonMenuItemPassAlwayEnc.setAction(Main.options.savePasswordAlwaysEnc);
        jRadioButtonMenuItemPassAlwayPlain.setAction(Main.options.savePasswordAlwaysPlain);
        jRadioButtonMenuItemPassNever.setAction(Main.options.savePasswordNever);
        jCheckBoxMenuItemSysTray.setAction(Main.options.minimize2SysTray);
        for (OutputTypeHandler h : tagSelection.getOutputHandlers()) {
            Action[] actions = h.getActions();
            if (actions.length > 0) {
                JMenu menu = new JMenu(h.getOutputTypeName());
                for (Action a : actions) {
                    menu.add(a);
                }
                jMenuActions.add(menu);
            }
        }
        jLabelOPC2OutStatus = new JLabel();
        jLabelOPC2OutStatus.setHorizontalTextPosition(SwingConstants.LEFT);
        jLabelOPC2OutStatus.setFont(jLabelOPC2OutStatus.getFont().deriveFont(Font.BOLD));
        simpleStatusBar.addComponent(jLabelOPCStatus, 0, 0.0, GridBagConstraints.EAST);
        simpleStatusBar.addComponent(jLabelOPC2OutStatus, 4, 0.0, GridBagConstraints.EAST);
        simpleStatusBar.addLogger(Logger.getLogger("org.plantstreamer"));
        simpleStatusBar.addLogger(Logger.getLogger("org.communications"));
        simpleStatusBar.addLogger(Logger.getLogger("org.opccom"));
        simpleStatusBar.addLogger(Logger.getLogger("org.database"));
        simpleStatusBar.addLogger(Logger.getLogger("org.opcda2out"));
        simpleStatusBar.addLogger(Logger.getLogger("swingextras"));
        updateOPCConStatus(Main.opc.getConnectionStatus());
        managerStatusChanged(new OPCBkManagerStatusChangeEvent(null, MANAGERSTATUS.STOPPED, MANAGERSTATUS.STOPPED));
        Main.opc.support.addConnectionListener(this);
        Main.opc2outManager.addStatusListener(this);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                if (Main.options.minimize2SysTray.isSelected()) {
                    setVisible(false);
                } else {
                    closePlantstreamer();
                }
            }

            @Override
            public void windowIconified(WindowEvent e) {
                if (Main.options.minimize2SysTray.isSelected()) {
                    setVisible(false);
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        System.out.println("Exit");
                        System.exit(0);
                    }
                });
            }
        });
        pack();
        GuiUtils.centerWindowOnScreen(this);
        if (SystemTray.isSupported()) {
            SystemTray st = SystemTray.getSystemTray();
            Dimension d = st.getTrayIconSize();
            int size = Math.min(d.height, d.width);
            ResizableIcon ri = IconManager.getResizableIcon("", "logo.png");
            Image image = ri.getIcon(size).getImage();
            PopupMenu menu = new PopupMenu();
            menu.add(new ActionMenuItem(tagSelection.startStopOPC2OutAction));
            menu.addSeparator();
            menu.add(new ActionMenuItem(restore));
            menu.add(new ActionMenuItem(exit));
            TrayIcon trayIcon = new TrayIcon(image, PACKAGE.getImplementationTitle(), menu);
            trayIcon.addActionListener(restore);
            try {
                st.add(trayIcon);
            } catch (AWTException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.FINE, ex.getMessage(), ex);
            }
            final Handler systemTrayLogHandler = new SystemTrayLogHandler(trayIcon);
            systemTrayLogHandler.setFilter(new Filter() {

                @Override
                public boolean isLoggable(LogRecord record) {
                    return record.getLevel().intValue() >= Level.WARNING.intValue();
                }
            });
            addWindowListener(new WindowAdapter() {

                @Override
                public void windowIconified(WindowEvent e) {
                    for (int i = 0; i < loggers.length; i++) {
                        loggers[i].addHandler(systemTrayLogHandler);
                    }
                }

                @Override
                public void windowDeiconified(WindowEvent e) {
                    for (int i = 0; i < loggers.length; i++) {
                        loggers[i].removeHandler(systemTrayLogHandler);
                    }
                }
            });
        }
    }

    /**
     * Fills the recent load/saved files
     */
    public void fillRecentFilesMenu() {
        jMenuOpenRecent.removeAll();
        String[] paths = AutoCompletionManager.getItems("openRecentFile");
        if (paths != null) {
            for (int i = paths.length - 1; i >= 0; i--) {
                final File file = new File(paths[i]);
                if (file.exists()) {
                    JMenuItem jMenuItem = new JMenuItem(paths[i]);
                    jMenuItem.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Main.openOptFile.openOptionsFile(file);
                        }
                    });
                    jMenuOpenRecent.add(jMenuItem);
                }
            }
        }
        jMenuOpenRecent.setEnabled(jMenuOpenRecent.getMenuComponentCount() != 0);
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        jLabelOPCStatus = new javax.swing.JLabel();
        popupMenu1 = new java.awt.PopupMenu();
        buttonGroupPassword = new javax.swing.ButtonGroup();
        tagSelection = new org.plantstreamer.TagSelection();
        jMenuBar = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jSeparator3 = new javax.swing.JSeparator();
        jMenuItemExportItemList = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator = new javax.swing.JSeparator();
        jMenuOptions = new javax.swing.JMenu();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        jMenuPassword = new javax.swing.JMenu();
        jRadioButtonMenuItemPassAuto = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItemPassAlwayEnc = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItemPassAlwayPlain = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItemPassNever = new javax.swing.JRadioButtonMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        jCheckBoxMenuItemSysTray = new javax.swing.JCheckBoxMenuItem();
        jMenuActions = new javax.swing.JMenu();
        jMenuItemOPC = new javax.swing.JMenuItem();
        jMenuItemOPC2Out = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItemCreateComposite = new javax.swing.JMenuItem();
        jMenuItemFind = new javax.swing.JMenuItem();
        jMenuItemAddItem = new javax.swing.JMenuItem();
        jMenuItemRemoveItem = new javax.swing.JMenuItem();
        jMenuItemClearSelection = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        jMenuWindow = new javax.swing.JMenu();
        jMenuItemLogg = new javax.swing.JMenuItem();
        jMenuItemConMan = new javax.swing.JMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItemAbout = new javax.swing.JMenuItem();
        jMenuItemWebPage = new javax.swing.JMenuItem();
        popupMenu1.setLabel("popupMenu1");
        popupMenu1.addSeparator();
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        getContentPane().add(simpleStatusBar, gridBagConstraints);
        tagSelection.setPreferredSize(new java.awt.Dimension(800, 600));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(tagSelection, gridBagConstraints);
        jMenuFile.setText(bundle.getString("File"));
        jMenuFile.add(jMenuIOpen);
        jMenuOpenRecent.setText(bundle.getString("Open_Recent"));
        jMenuFile.add(jMenuOpenRecent);
        jMenuFile.add(jMenuItemSaveAs);
        jMenuFile.add(jMenuItemSave);
        jMenuFile.add(jSeparator3);
        jMenuExport.setIcon(IconManager.getIcon("16x16/actions/fileexport.png"));
        jMenuExport.setText(bundle.getString("Export"));
        jMenuItemExportItemList.setText(bundle.getString("opc_item_list"));
        jMenuItemExportItemList.setEnabled(false);
        jMenuItemExportItemList.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExportItemListActionPerformed(evt);
            }
        });
        jMenuExport.add(jMenuItemExportItemList);
        jMenuFile.add(jMenuExport);
        jMenuFile.add(jSeparator2);
        jMenuFile.add(jMenuItemOPCConInfo);
        jMenuItemOPCServerState.setText("OPC Server State...");
        jMenuItemOPCServerState.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOPCServerStateActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemOPCServerState);
        jMenuFile.add(jSeparator);
        jMenuFile.add(jMenuItemExit);
        jMenuBar.add(jMenuFile);
        jMenuOptions.setText(bundle.getString("Options"));
        jCheckBoxMenuItemAutoRecon.setText(bundle.getString("auto_reconnect"));
        jMenuOptions.add(jCheckBoxMenuItemAutoRecon);
        jCheckBoxMenuAutoFetch.setText(bundle.getString("auto_fetch_tags"));
        jMenuOptions.add(jCheckBoxMenuAutoFetch);
        jCheckBoxAsync.setText("Asynchronous");
        jMenuOptions.add(jCheckBoxAsync);
        jCheckBoxProperties.setText("Save Description and Units");
        jMenuOptions.add(jCheckBoxProperties);
        jMenuOptions.add(jSeparator6);
        jMenuPassword.setText(bundle.getString("Save_Passwords"));
        buttonGroupPassword.add(jRadioButtonMenuItemPassAuto);
        jRadioButtonMenuItemPassAuto.setText("Auto");
        jMenuPassword.add(jRadioButtonMenuItemPassAuto);
        buttonGroupPassword.add(jRadioButtonMenuItemPassAlwayEnc);
        jRadioButtonMenuItemPassAlwayEnc.setText("Always with encryption");
        jMenuPassword.add(jRadioButtonMenuItemPassAlwayEnc);
        buttonGroupPassword.add(jRadioButtonMenuItemPassAlwayPlain);
        jRadioButtonMenuItemPassAlwayPlain.setText("Always (as plain text)");
        jMenuPassword.add(jRadioButtonMenuItemPassAlwayPlain);
        buttonGroupPassword.add(jRadioButtonMenuItemPassNever);
        jRadioButtonMenuItemPassNever.setText("Never");
        jMenuPassword.add(jRadioButtonMenuItemPassNever);
        jMenuOptions.add(jMenuPassword);
        jMenuOptions.add(jSeparator5);
        jMenuOptions.add(jCheckBoxMenuItemSysTray);
        jMenuBar.add(jMenuOptions);
        jMenuActions.setText(bundle.getString("Actions"));
        jMenuActions.add(jMenuItemOPC);
        jMenuActions.add(jMenuItemOPC2Out);
        jMenuActions.add(jSeparator1);
        jMenuActions.add(jMenuItemCreateComposite);
        jMenuActions.add(jMenuItemFind);
        jMenuActions.add(jMenuItemAddItem);
        jMenuActions.add(jMenuItemRemoveItem);
        jMenuActions.add(jMenuItemClearSelection);
        jMenuActions.add(jSeparator4);
        jMenuBar.add(jMenuActions);
        jMenuWindow.setText(bundle.getString("Window"));
        jMenuItemLogg.setText(bundle.getString("Log_dialog"));
        jMenuItemLogg.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLoggActionPerformed(evt);
            }
        });
        jMenuWindow.add(jMenuItemLogg);
        jMenuItemConMan.setText(bundle.getString("Connection_Manager..."));
        jMenuItemConMan.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemConManActionPerformed(evt);
            }
        });
        jMenuWindow.add(jMenuItemConMan);
        jMenuBar.add(jMenuWindow);
        jMenuHelp.setText(bundle.getString("Help"));
        jMenuHelp.setToolTipText("Help");
        jMenuItemAbout.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAboutActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemAbout);
        jMenuItemWebPage.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemWebPageActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuItemWebPage);
        jMenuBar.add(jMenuHelp);
        setJMenuBar(jMenuBar);
    }

    private void jMenuItemAboutActionPerformed(java.awt.event.ActionEvent evt) {
        aboutDialog.show(this);
    }

    private void jMenuItemLoggActionPerformed(java.awt.event.ActionEvent evt) {
        logDialog.show(this);
    }

    private void jMenuItemConManActionPerformed(java.awt.event.ActionEvent evt) {
        connectManDialog.show(this);
    }

    private void jMenuItemWebPageActionPerformed(java.awt.event.ActionEvent evt) {
        if (java.awt.Desktop.isDesktopSupported()) {
            URI webPage = null;
            try {
                webPage = new URI("http://www.plantstreamer.com/");
            } catch (URISyntaxException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                return;
            }
            try {
                java.awt.Desktop.getDesktop().browse(webPage);
            } catch (IOException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    private void jMenuItemExportItemListActionPerformed(java.awt.event.ActionEvent evt) {
        OPCListExportDialog dialog = new OPCListExportDialog(this, tagSelection.getTreeTableModel());
        dialog.setVisible(true);
    }

    private void jMenuItemOPCServerStateActionPerformed(java.awt.event.ActionEvent evt) {
        opcServerStateDialog.show(this);
    }

    @Override
    public void connectionStateChanged(final ConnectionStatusChangeEvent e) {
        updateOPCConStatus(e.newStatus);
    }

    /**
     * Handles a connection status changes of the OPC manager
     * @param status the connection status
     */
    private void updateOPCConStatus(STATUS status) {
        String iconStatus = null;
        boolean addItem = false;
        switch(status) {
            case CONNECTING:
                iconStatus = "connect_creating.png";
                break;
            case CONNECTED:
                iconStatus = "connect_established.png";
                if (!Main.options.autoFetchOPCTree.isSelected()) {
                    addItem = true;
                }
                break;
            case RECONNECTING:
                iconStatus = "connect_reestablishing.png";
                break;
            case DISCONNECTING:
                iconStatus = "connect_no.png";
                break;
            case DISCONNECTED:
                iconStatus = "connect_no.png";
        }
        jLabelOPCStatus.setIcon(IconManager.getIcon("16x16/actions/" + iconStatus));
        jLabelOPCStatus.setToolTipText(status.getI18nDescription());
        jMenuItemExportItemList.setEnabled(status == STATUS.CONNECTED);
        if (status == STATUS.DISCONNECTED) {
            jCheckBoxMenuAutoFetch.setEnabled(true);
        } else {
            jCheckBoxMenuAutoFetch.setEnabled(false);
        }
        jMenuItemAddItem.setEnabled(addItem);
        jMenuItemRemoveItem.setEnabled(addItem);
    }

    @Override
    public void managerStatusChanged(final OPCBkManagerStatusChangeEvent e) {
        MANAGERSTATUS status = e.getNewStatus();
        if (status == MANAGERSTATUS.STOPPED) {
            jLabelOPC2OutStatus.setText("Stopped");
            jLabelOPC2OutStatus.setForeground(Color.DARK_GRAY);
            jLabelOPC2OutStatus.setIcon(null);
        } else if (status == MANAGERSTATUS.INITIALIZING) {
            jLabelOPC2OutStatus.setText("Initializing");
            jLabelOPC2OutStatus.setForeground(new Color(255, 200, 0));
            jLabelOPC2OutStatus.setIcon(IconManager.getIcon("16x16/logo.png"));
        } else if (status == MANAGERSTATUS.STOPPING) {
            jLabelOPC2OutStatus.setText("Stopping");
            jLabelOPC2OutStatus.setForeground(new Color(255, 200, 0));
            jLabelOPC2OutStatus.setIcon(IconManager.getIcon("16x16/logo.png"));
        } else {
            jLabelOPC2OutStatus.setText("Running");
            jLabelOPC2OutStatus.setForeground(new Color(0, 120, 0));
            jLabelOPC2OutStatus.setIcon(IconManager.getIcon("16x16/logo.png"));
        }
        boolean isStopped = status == MANAGERSTATUS.STOPPED;
        Main.options.asynchronous.setEnabled(isStopped);
        Main.options.saveProperties.setEnabled(isStopped);
    }

    /**
     * Used to close plantstreamer
     */
    public void closePlantstreamer() {
        if (Main.opc2outManager.isRunning()) {
            int answer = JOptionPane.showConfirmDialog(this, MessageFormat.format(bundle.getString("{0}_is_currently_logging_data_from_the_OPC_server.\nDo_you_really_want_to_stop_logging_and_exit_{0}?"), PACKAGE.getImplementationTitle()), bundle.getString("Exit_{0}?"), JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                Main.opc2outManager.requestStop();
            } else {
                return;
            }
        }
        Main.opc.disconnect();
        Main.db.disconnect();
        dispose();
    }

    /**
     * Returns the status bar
     * @return the status bar
     */
    public static SimpleStatusBar getStatusBar() {
        return simpleStatusBar;
    }

    private javax.swing.ButtonGroup buttonGroupPassword;

    private final javax.swing.JCheckBoxMenuItem jCheckBoxAsync = new javax.swing.JCheckBoxMenuItem();

    private final javax.swing.JCheckBoxMenuItem jCheckBoxMenuAutoFetch = new javax.swing.JCheckBoxMenuItem();

    private final javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemAutoRecon = new javax.swing.JCheckBoxMenuItem();

    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemSysTray;

    private final javax.swing.JCheckBoxMenuItem jCheckBoxProperties = new javax.swing.JCheckBoxMenuItem();

    private javax.swing.JLabel jLabelOPCStatus;

    private javax.swing.JMenu jMenuActions;

    private javax.swing.JMenuBar jMenuBar;

    private final javax.swing.JMenu jMenuExport = new javax.swing.JMenu();

    private javax.swing.JMenu jMenuFile;

    private javax.swing.JMenu jMenuHelp;

    private final javax.swing.JMenuItem jMenuIOpen = new javax.swing.JMenuItem();

    private javax.swing.JMenuItem jMenuItemAbout;

    private javax.swing.JMenuItem jMenuItemAddItem;

    private javax.swing.JMenuItem jMenuItemClearSelection;

    private javax.swing.JMenuItem jMenuItemConMan;

    private javax.swing.JMenuItem jMenuItemCreateComposite;

    private final javax.swing.JMenuItem jMenuItemExit = new javax.swing.JMenuItem();

    private javax.swing.JMenuItem jMenuItemExportItemList;

    private javax.swing.JMenuItem jMenuItemFind;

    private javax.swing.JMenuItem jMenuItemLogg;

    private javax.swing.JMenuItem jMenuItemOPC;

    protected javax.swing.JMenuItem jMenuItemOPC2Out;

    private final javax.swing.JMenuItem jMenuItemOPCConInfo = new javax.swing.JMenuItem();

    private final javax.swing.JMenuItem jMenuItemOPCServerState = new javax.swing.JMenuItem();

    private javax.swing.JMenuItem jMenuItemRemoveItem;

    private final javax.swing.JMenuItem jMenuItemSave = new javax.swing.JMenuItem();

    private final javax.swing.JMenuItem jMenuItemSaveAs = new javax.swing.JMenuItem();

    private javax.swing.JMenuItem jMenuItemWebPage;

    private final javax.swing.JMenu jMenuOpenRecent = new javax.swing.JMenu();

    private javax.swing.JMenu jMenuOptions;

    private javax.swing.JMenu jMenuPassword;

    private javax.swing.JMenu jMenuWindow;

    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemPassAlwayEnc;

    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemPassAlwayPlain;

    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemPassAuto;

    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemPassNever;

    private javax.swing.JSeparator jSeparator;

    private javax.swing.JSeparator jSeparator1;

    private javax.swing.JSeparator jSeparator2;

    private javax.swing.JSeparator jSeparator3;

    private javax.swing.JSeparator jSeparator4;

    private javax.swing.JSeparator jSeparator5;

    private javax.swing.JPopupMenu.Separator jSeparator6;

    private java.awt.PopupMenu popupMenu1;

    private static final swingextras.gui.SimpleStatusBar simpleStatusBar = new swingextras.gui.SimpleStatusBar();

    public org.plantstreamer.TagSelection tagSelection;

    private final JLabel jLabelOPC2OutStatus;
}
