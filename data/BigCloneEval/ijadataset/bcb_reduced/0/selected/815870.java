package org.mitre.rt.client.ui.applications;

import java.awt.Desktop;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URI;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.mitre.rt.client.core.MetaManager;
import org.apache.log4j.Logger;
import org.mitre.rt.client.core.DataManager;
import org.mitre.rt.client.core.SynchronizationManager;
import org.mitre.rt.client.exceptions.DataManagerException;
import org.mitre.rt.client.synchronize.SynchronizeClientDialog;
import org.mitre.rt.client.ui.configuration.RTConfigurationDialog;
import org.mitre.rt.client.xml.ApplicationHelper;
import org.mitre.rt.server.properties.RTServerProperties;
import org.mitre.rt.rtclient.ApplicationType;
import org.mitre.rt.rtclient.RTDocument;
import org.mitre.rt.rtclient.RTDocument.RT;
import org.mitre.rt.rtclient.UserType;
import org.mitre.rt.client.ui.help.AboutDialog;
import org.mitre.rt.client.ui.html.ViewHtmlDialog;
import org.mitre.rt.client.ui.users.UserEditJDialog;
import org.mitre.rt.client.ui.users.UserManageJDialog;
import org.mitre.rt.client.util.GlobalUITools;

/**
 * This class creates the menubar for the editor, along with the many listeners
 * corresponding to each menu selection
 *
 */
public class MenuBar extends JMenuBar {

    private static Logger logger = Logger.getLogger(MenuBar.class.getPackage().getName());

    /**
     *	primary menu bar options
     */
    private JMenu fileMenu;

    private JMenu serverMenu;

    private JMenu adminMenu;

    private JMenu helpMenu;

    private JMenu applicationMenu;

    private JMenuItem appNew;

    private JMenuItem appEdit;

    private JMenuItem appView;

    private JMenuItem appRemove;

    private JMenuItem appGenXCCDF;

    private JMenuItem appImportXCCDF;

    private JMenuItem save;

    private JMenuItem userProfile;

    private JMenuItem options;

    private JMenuItem exit;

    private JMenuItem userAdmin;

    private JMenuItem rtEnums;

    private JMenuItem selectedApplications;

    private JMenuItem sync;

    private JMenuItem revertLocalData;

    private JMenuItem configure;

    private JMenuItem aboutRecommendationTracker;

    private ApplicationType application = null;

    /**
     * Constructor
     *
     */
    public MenuBar() {
        applicationMenu = new JMenu("Application");
        appNew = new JMenuItem("New Application...");
        appNew.setMnemonic(KeyEvent.VK_N);
        appNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK));
        appNew.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    DataManager dm = DataManager.instance();
                    RTDocument rtDoc = dm.getRTDocument();
                    RT rt = rtDoc.getRT();
                    NewApplicationDialog newAppWindow = new NewApplicationDialog(MetaManager.getMainWindow(), true, rt);
                    newAppWindow.setVisible(true);
                } catch (Exception ex) {
                    logger.debug(ex, ex);
                }
            }
        });
        applicationMenu.add(appNew);
        applicationMenu.addSeparator();
        appImportXCCDF = new JMenuItem("Import XCCDF...");
        appImportXCCDF.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                logger.debug("Import XCCDF ActionEvent");
                ImportXCCDFDialog dialog = new ImportXCCDFDialog(MetaManager.getMainWindow(), true);
                dialog.setVisible(true);
            }
        });
        applicationMenu.add(appImportXCCDF);
        applicationMenu.addSeparator();
        appEdit = new JMenuItem("Edit Application...");
        appEdit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    DataManager dm = DataManager.instance();
                    RTDocument rtDoc = dm.getRTDocument();
                    RT rt = rtDoc.getRT();
                    EditApplicationDialog editApp = new EditApplicationDialog(MetaManager.getMainWindow(), true, MetaManager.getMainWindow().getApplicationBar().getApplication(), rt);
                    editApp.setVisible(true);
                } catch (Exception ex) {
                    logger.fatal(ex, ex);
                }
            }
        });
        applicationMenu.add(appEdit);
        appView = new JMenuItem("View Application...");
        appView.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ApplicationType app = MetaManager.getMainWindow().getApplicationBar().getApplication();
                    File outputHtml = new ApplicationHelper().applyViewApplicationXsl(app);
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        URI uri = outputHtml.toURI();
                        logger.debug("Displaying in browser: " + uri.toString());
                        desktop.browse(uri);
                    } else {
                        logger.debug("Displaying via dialog");
                        String title = "View Recommendation: " + app.getName();
                        ViewHtmlDialog recDialog = new ViewHtmlDialog(MetaManager.getMainWindow(), true, title, outputHtml);
                        recDialog.setVisible(true);
                    }
                } catch (Exception ex) {
                    logger.fatal("Error while viewing an application.", ex);
                }
            }
        });
        applicationMenu.add(appView);
        appRemove = new JMenuItem("Remove Application...");
        appRemove.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ApplicationType currentApp = MetaManager.getMainWindow().getApplicationBar().getApplication();
                if (currentApp != null) {
                    ApplicationRemoveDialog removeDialog = new ApplicationRemoveDialog(MetaManager.getMainWindow(), true, currentApp);
                }
            }
        });
        appRemove.setEnabled(false);
        applicationMenu.add(appRemove);
        applicationMenu.addSeparator();
        appGenXCCDF = new JMenuItem("Generate XCCDF...");
        appGenXCCDF.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                logger.debug("Generate XCCDF ActionEvent");
                GenerateXCCDFDialog dialog = new GenerateXCCDFDialog(MetaManager.getMainWindow(), true, MetaManager.getMainWindow().getApplicationBar().getApplication());
                dialog.setVisible(true);
            }
        });
        applicationMenu.add(appGenXCCDF);
        adminMenu = new JMenu("Administration");
        userAdmin = new JMenuItem("Manage Users...");
        userAdmin.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                UserManageJDialog dialog = new UserManageJDialog(MetaManager.getMainWindow(), true);
                dialog.setVisible(true);
            }
        });
        adminMenu.add(userAdmin);
        adminMenu.addSeparator();
        rtEnums = new JMenuItem("Configure Defaults...");
        rtEnums.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                logger.debug("TODO - rtEnums: " + e.getActionCommand());
            }
        });
        rtEnums.setEnabled(false);
        adminMenu.add(rtEnums);
        serverMenu = new JMenu("Server");
        selectedApplications = new JMenuItem("Select Applications...");
        selectedApplications.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    logger.debug("Selected Applications: " + e.getActionCommand());
                    RTServerProperties serverProps = RTServerProperties.instance();
                    boolean verified = serverProps.verifyProperties();
                    if (verified) {
                        SynchronizeClientDialog.displaySynchronizeDialog(true);
                        if (SynchronizeClientDialog.SYNC_COMPLETED == true) {
                            SynchronizationManager.instance().getApplications(serverProps.getDbHost());
                        }
                    } else {
                        GlobalUITools.displayWarningMessage(MetaManager.getMainWindow(), "Server Connection Error", "Unable to connect your RT Server. \nPlease verify that you are online and your server configuration settings are correct before trying again");
                    }
                } catch (DataManagerException ex) {
                    logger.warn(ex, ex);
                }
            }
        });
        selectedApplications.setEnabled(true);
        serverMenu.add(selectedApplications);
        serverMenu.addSeparator();
        sync = new JMenuItem("Synchronize...");
        sync.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    SynchronizeClientDialog.displaySynchronizeDialog(false);
                } catch (DataManagerException ex) {
                    logger.warn(ex, ex);
                }
            }
        });
        serverMenu.add(sync);
        JPopupMenu serverPopup = serverMenu.getPopupMenu();
        serverPopup.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                sync.setEnabled(RTServerProperties.instance().isDbStandAlone() == false);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        revertLocalData = new JMenuItem("Revert Local Changes...");
        revertLocalData.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        revertLocalData.setEnabled(false);
        serverMenu.add(revertLocalData);
        serverMenu.addSeparator();
        configure = new JMenuItem("Configure...");
        configure.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                RTServerProperties.instance().popUpAndCheckSettings();
            }
        });
        configure.setEnabled(true);
        serverMenu.add(configure);
        fileMenu = new JMenu("File");
        save = new JMenuItem("Save");
        save.setMnemonic(KeyEvent.VK_S);
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
        save.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    DataManager.instance().saveWithWorkerDialog(true);
                } catch (DataManagerException ex) {
                    GlobalUITools.displayFatalExceptionMessage(null, "Error saving RTDocument", ex, false);
                }
            }
        });
        fileMenu.add(save);
        fileMenu.addSeparator();
        userProfile = new JMenuItem("My Account...");
        userProfile.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                UserType user = MetaManager.getAuthenticatedUser();
                UserEditJDialog dialog = new UserEditJDialog(MetaManager.getMainWindow(), true, user);
                dialog.setVisible(true);
            }
        });
        fileMenu.add(userProfile);
        fileMenu.addSeparator();
        options = new JMenuItem("Options...");
        options.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                RTConfigurationDialog dialog = new RTConfigurationDialog(MetaManager.getMainWindow(), true);
                dialog.setVisible(true);
            }
        });
        fileMenu.add(options);
        fileMenu.addSeparator();
        exit = new JMenuItem("Exit");
        exit.setMnemonic(KeyEvent.VK_Q);
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK));
        exit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                logger.debug("File: " + e.getActionCommand());
                MetaManager.getRtClient().shutdown(true);
            }
        });
        fileMenu.add(exit);
        helpMenu = new JMenu("Help");
        aboutRecommendationTracker = new JMenuItem("About...");
        aboutRecommendationTracker.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                logger.debug("About: " + e.getActionCommand());
                AboutDialog dialog = new AboutDialog(MetaManager.getMainWindow(), true);
                dialog.setVisible(true);
            }
        });
        helpMenu.add(aboutRecommendationTracker);
        this.add(fileMenu);
        this.add(applicationMenu);
        this.add(serverMenu);
        this.add(adminMenu);
        this.add(helpMenu);
        this.restrictAdminOnlyItems();
        this.setApplication(null);
    }

    private void restrictAdminOnlyItems() {
        UserType user = MetaManager.getAuthenticatedUser();
        if (!user.getAdmin()) {
            this.remove(adminMenu);
        }
    }

    public ApplicationType getApplication() {
        return application;
    }

    public void setApplication(ApplicationType application) {
        this.application = application;
        if (this.application == null) {
            this.appEdit.setEnabled(false);
            this.appView.setEnabled(false);
            this.appGenXCCDF.setEnabled(false);
            this.appRemove.setEnabled(false);
        } else {
            this.appEdit.setEnabled(true);
            this.appView.setEnabled(true);
            this.appGenXCCDF.setEnabled(true);
            this.appRemove.setEnabled(true);
        }
    }
}
