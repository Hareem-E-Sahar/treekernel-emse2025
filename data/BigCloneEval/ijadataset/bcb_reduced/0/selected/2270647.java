package ossobook.client.controllers;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.RollingFileAppender;
import ossobook.Messages;
import ossobook.client.base.metainfo.Project;
import ossobook.client.events.EventType;
import ossobook.client.events.ObservableSupport;
import ossobook.client.events.OssoBookEvent;
import ossobook.client.gui.common.OssobookFrame;
import ossobook.client.gui.common.ProjectManipulation;
import ossobook.client.gui.dialogs.DatabaseSelectionDialog;
import ossobook.client.gui.dialogs.LoginDialog;
import ossobook.client.gui.dialogs.ProjectDialog;
import ossobook.client.gui.dialogs.ProjectDialogType;
import ossobook.client.gui.synchronization.SynchronizationFrame;
import ossobook.client.gui.update.components.other.TabellenAnsicht;
import ossobook.client.gui.update.components.window.NeuerTierCodeFenster;
import ossobook.client.gui.update.components.window.ProjektFenster;
import ossobook.client.io.CSVExport;
import ossobook.client.io.database.ConnectionType;
import ossobook.client.util.Configuration;
import ossobook.exceptions.NoWriteRightException;
import ossobook.exceptions.StatementNotExecutedException;
import ossobook.queries.QueryManager;
import ossobook.queries.QueryManagerFactory;

/**
 * The controller class which performs actions triggered by the GUI.
 * 
 * <p>
 * GUI classes should add this class as an {@link Observer} to their observer
 * list (either by extending the {@link Observable} class directly, where
 * possible, or alternatively by keeping a private {@link ObservableSupport}
 * instance) to notify the controller of GUI events.
 * </p>
 * 
 * <p>
 * The controller should support all actions defined in the {@link EventType}
 * enumeration. If an unknown type is received, a warning will be logged, but
 * no further action will be taken.
 * </p>
 * 
 * @see OssoBookEvent
 * @see EventType
 * @see ObservableSupport
 * 
 * @author j.lamprecht
 * @author fnuecke
 */
public class GuiController implements Observer {

    /**
	 * Logging...
	 */
    private static final Log log = LogFactory.getLog(GuiController.class);

    /**
	 * Maps actions to queries for lookup tables (to avoid having a gazillion
	 * functions doing the same thing, basically).
	 */
    private static final Map<EventType, String> queryMapping = new HashMap<EventType, String>();

    /**
	 * Maps actions to their type for lookup tables (same as with queries).
	 */
    private static final Map<EventType, Integer> constantMapping = new HashMap<EventType, Integer>();

    static {
        queryMapping.put(EventType.TIERARTSUCHE, "SELECT TierCode,TierName,DTier FROM tierart  WHERE geloescht='N' ORDER by TierName;");
        queryMapping.put(EventType.SKELTEILSUCHE, "SELECT SkelCode,SkelName FROM skelteil  WHERE geloescht='N' ORDER by SkelName;");
        queryMapping.put(EventType.BRUCHKANTESUCHE, "SELECT BruchkanteCode,BruchkanteName FROM bruchkante WHERE geloescht='N' ORDER by BruchkanteName;");
        queryMapping.put(EventType.ERHALTUNGSUCHE, "SELECT ErhaltungCode,ErhaltungName FROM erhaltung WHERE geloescht='N' ORDER by ErhaltungName;");
        queryMapping.put(EventType.ALTER1SUCHE, "SELECT Alter1Code,Alter1Name FROM alter1 WHERE geloescht='N' ORDER by Alter1Name;");
        queryMapping.put(EventType.ALTER2SUCHE, "SELECT Alter2Code,Alter2Name FROM alter2 WHERE geloescht='N' ORDER by Alter2Name;");
        queryMapping.put(EventType.ALTER3SUCHE, "SELECT Alter3Code,Alter3Name FROM alter3 WHERE geloescht='N' ORDER by Alter3Name;");
        queryMapping.put(EventType.ALTER4SUCHE, "SELECT Alter4Code,Alter4Name FROM alter4 WHERE geloescht='N' ORDER by Alter4Name;");
        queryMapping.put(EventType.ALTER5SUCHE, "SELECT Alter5Code,Alter5Name FROM alter5 WHERE geloescht='N' ORDER by Alter5Name;");
        queryMapping.put(EventType.WURZELFRASS, "SELECT WurzelfrassCode,WurzelfrassName FROM wurzelfrass WHERE geloescht='N' ORDER by WurzelfrassName;");
        queryMapping.put(EventType.VERSINTERUNG, "SELECT VersinterungCode,VersinterungName FROM versinterung WHERE geloescht='N' ORDER by VersinterungName;");
        queryMapping.put(EventType.FETTIGSUCHE, "SELECT FettCode,FettName FROM fett WHERE geloescht='N' ORDER by FettName;");
        queryMapping.put(EventType.PATINASUCHE, "SELECT PatinaCode,PatinaName FROM patina WHERE geloescht='N' ORDER by PatinaName;");
        queryMapping.put(EventType.BRANDSPURSUCHE, "SELECT BrandspurCode,BrandspurName FROM brandspur WHERE geloescht='N' ORDER by BrandspurName;");
        queryMapping.put(EventType.VERBISSSUCHE, "SELECT VerbissCode,VerbissName FROM verbiss WHERE geloescht='N' ORDER by VerbissName;");
        queryMapping.put(EventType.SCHLACHTSPUR1, "SELECT Schlachtspur1Code,Schlachtspur1Name FROM schlachtspur1 WHERE geloescht='N' ORDER by Schlachtspur1Name;");
        queryMapping.put(EventType.SCHLACHTSPUR2, "SELECT Schlachtspur2Code,Schlachtspur2Name FROM schlachtspur2 WHERE geloescht='N' ORDER by Schlachtspur2Name;");
        queryMapping.put(EventType.GESCHLECHT, "SELECT GeschlechtCode,GeschlechtName FROM geschlecht WHERE geloescht='N' ORDER by GeschlechtName;");
        queryMapping.put(EventType.BRUCHKANTE2, "SELECT Bruchkante2Code,Bruchkante2Name FROM bruchkante2 WHERE geloescht='N' ORDER by Bruchkante2Name;");
        constantMapping.put(EventType.TIERARTSUCHE, TabellenAnsicht.TIERARTSUCHE);
        constantMapping.put(EventType.SKELTEILSUCHE, TabellenAnsicht.SKELTEILSUCHE);
        constantMapping.put(EventType.BRUCHKANTESUCHE, TabellenAnsicht.BRUCHKANTESUCHE);
        constantMapping.put(EventType.ERHALTUNGSUCHE, TabellenAnsicht.ERHALTUNGSUCHE);
        constantMapping.put(EventType.ALTER1SUCHE, TabellenAnsicht.ALTER1SUCHE);
        constantMapping.put(EventType.ALTER2SUCHE, TabellenAnsicht.ALTER2SUCHE);
        constantMapping.put(EventType.ALTER3SUCHE, TabellenAnsicht.ALTER3SUCHE);
        constantMapping.put(EventType.ALTER4SUCHE, TabellenAnsicht.ALTER4SUCHE);
        constantMapping.put(EventType.ALTER5SUCHE, TabellenAnsicht.ALTER5SUCHE);
        constantMapping.put(EventType.WURZELFRASS, TabellenAnsicht.WURZELFRASS);
        constantMapping.put(EventType.VERSINTERUNG, TabellenAnsicht.VERSINTERUNG);
        constantMapping.put(EventType.FETTIGSUCHE, TabellenAnsicht.FETTIGSUCHE);
        constantMapping.put(EventType.PATINASUCHE, TabellenAnsicht.PATINASUCHE);
        constantMapping.put(EventType.BRANDSPURSUCHE, TabellenAnsicht.BRANDSPURSUCHE);
        constantMapping.put(EventType.VERBISSSUCHE, TabellenAnsicht.VERBISSSUCHE);
        constantMapping.put(EventType.SCHLACHTSPUR1, TabellenAnsicht.SCHLACHTSPUR1);
        constantMapping.put(EventType.SCHLACHTSPUR2, TabellenAnsicht.SCHLACHTSPUR2);
        constantMapping.put(EventType.GESCHLECHT, TabellenAnsicht.GESCHLECHT);
        constantMapping.put(EventType.BRUCHKANTE2, TabellenAnsicht.BRUCHKANTE2);
    }

    /**
	 * The main GUI frame.
	 */
    private final OssobookFrame ossobookFrame;

    /**
	 * starts Ossobook, particularly the Ossobook main window
	 */
    public GuiController() {
        ossobookFrame = new OssobookFrame(this);
    }

    /**
	 * Update function of the {@link Observer} interface, which is called
	 * whenever a {@link Observable} wants to notify the controller of a change.
	 * 
	 * <p>
	 * The controller only processes events of the type {@link OssoBookEvent},
	 * i.e. the argument (<code>arg</code>) parameter must be an instance of
	 * that class.
	 * </p>
	 * 
	 * <p>
	 * Based on the type of the event (see {@link EventType}) further action is
	 * taken.
	 * </p>
	 * 
	 * @param o the object which fired the event. Not used.
	 * @param arg the event argument. Must be an {@link OssoBookEvent}.
	 * @see OssoBookEvent
	 */
    public void update(Observable o, Object arg) {
        if (arg instanceof OssoBookEvent) {
            EventType type = ((OssoBookEvent) arg).getType();
            switch(type) {
                case LOGIN:
                    login();
                    break;
                case OPEN_PROJECT:
                    openProject();
                    break;
                case SYNCHRONIZE:
                    try {
                        SynchronizationFrame synchronizationFrame = new SynchronizationFrame();
                        ossobookFrame.getDesktop().add(synchronizationFrame);
                        synchronizationFrame.moveToFront();
                        synchronizationFrame.setVisible(true);
                        try {
                            synchronizationFrame.setSelected(true);
                        } catch (PropertyVetoException ex) {
                            ex.printStackTrace();
                        }
                    } catch (IllegalStateException ignore) {
                    }
                    break;
                case CHECK_FOR_UPDATES:
                    checkForUpdates();
                    break;
                case EXIT:
                    System.exit(0);
                    break;
                case SHOW_LOG:
                    showLog();
                    break;
                case SHOW_ABOUT:
                    showAbout();
                    break;
                case ADD_ANIMAL_CODE:
                    addAnimalCode();
                    break;
                case ANIMAL_ANALYSIS:
                    performAnimalAnalysis();
                    break;
                case SKELETON_ANALYSIS:
                    performSkeletonAnalysis();
                    break;
                case ALTER1SUCHE:
                case ALTER2SUCHE:
                case ALTER3SUCHE:
                case ALTER4SUCHE:
                case ALTER5SUCHE:
                case BRANDSPURSUCHE:
                case BRUCHKANTE2:
                case BRUCHKANTESUCHE:
                case ERHALTUNGSUCHE:
                case FETTIGSUCHE:
                case GESCHLECHT:
                case PATINASUCHE:
                case SCHLACHTSPUR1:
                case SCHLACHTSPUR2:
                case SKELTEILSUCHE:
                case TIERARTSUCHE:
                case VERBISSSUCHE:
                case VERSINTERUNG:
                case WURZELFRASS:
                    lookupCode(type);
                    break;
                case NEW_PROJECT:
                    newProject();
                    break;
                case DELETE_PROJECT:
                    deleteProject();
                    break;
                default:
                    log.warn(Messages.getString("GuiController.4"));
                    break;
            }
        }
    }

    /**
	 * <p>
	 * This is a convenience function for {@link #checkForUpdates(boolean)},
	 * always passing <code>false</code>.
	 * </p>
	 */
    private void checkForUpdates() {
        checkForUpdates(false);
    }

    /**
	 * Checks for updates of the client.
	 * 
	 * <p>
	 * First checks if a direct global connection is available. If so it tries
	 * to query the current client version from the global connection / query
	 * manager.
	 * </p>
	 * 
	 * <p>
	 * If that fails for some reason, tries to create a global connection used
	 * during synchronization to fetch the version that way.
	 * </p>
	 * 
	 * <p>
	 * If that doesn't work either, an error message is shown.
	 * </p>
	 * 
	 * <p>
	 * If it does work, if the client is still up to date, the user is told so,
	 * if it is not, the user is offered a link which she can follow to download
	 * an updated version of the client.
	 * </p>
	 * 
	 * @param silent
	 *            when set to <code>true</code>, no "negative" feedback will be
	 *            forwarded to the user (i.e. no notifications regarding failed
	 *            checks, or the update not being necessary). This can be useful
	 *            when automatically checking for a new version.
	 */
    private void checkForUpdates(boolean silent) {
        QueryManager manager = null;
        if (QueryManagerFactory.isGlobalAvailable()) {
            manager = QueryManagerFactory.getGlobalManager();
        }
        if (manager == null) {
            if (QueryManagerFactory.isSyncAvailable()) {
                manager = QueryManagerFactory.getGlobalSyncManager();
            } else {
                log.warn(Messages.getString("GuiController.5"));
                if (!silent) {
                    showErrorDialog(Messages.getString("GuiController.6"));
                }
                return;
            }
        }
        if (manager == null) {
            log.error(Messages.getString("GuiController.7"));
            if (!silent) {
                showErrorDialog(Messages.getString("GuiController.7"));
            }
            return;
        }
        try {
            String globalVersion = manager.getOssobookVersion();
            try {
                double globalVersionNumeric = Double.parseDouble(globalVersion);
                double localVersionNumeric = Double.parseDouble(Configuration.config.getProperty("ossobook.version"));
                if (globalVersionNumeric > localVersionNumeric) {
                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(ossobookFrame, String.format(Messages.getString("GuiController.11"), String.valueOf(globalVersion)), Messages.getString("GuiController.12"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                        String projectHomepage = Configuration.config.getProperty("ossobook.homepage");
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().browse(new URI(projectHomepage));
                            } catch (IOException e) {
                                log.error(Messages.getString("GuiController.14"), e);
                                showWarnDialog(String.format(Messages.getString("GuiController.15"), projectHomepage));
                            } catch (URISyntaxException e) {
                                log.error(Messages.getString("GuiController.16"), e);
                                showErrorDialog(Messages.getString("GuiController.17"));
                            }
                        } else {
                            showWarnDialog(String.format(Messages.getString("GuiController.18"), projectHomepage));
                        }
                    }
                } else {
                    log.info(Messages.getString("GuiController.19"));
                    if (!silent) {
                        showNoticeDialog(Messages.getString("GuiController.20"));
                    }
                }
            } catch (NumberFormatException e) {
                log.error(Messages.getString("GuiController.21"));
                if (!silent) {
                    showErrorDialog(Messages.getString("GuiController.22"));
                }
            }
        } catch (StatementNotExecutedException e) {
            log.error(Messages.getString("GuiController.23"), e);
            if (!silent) {
                showErrorDialog(Messages.getString("GuiController.23"));
            }
        }
    }

    /**
	 * Initializes a user login.
	 * 
	 * <p>
	 * This opens the login window and waits for the user to make his input.
	 * After the data is submitted this will initialize the login progress,
	 * trying to connect to the database (using the {@link QueryManagerFactory}).
	 * </p>
	 */
    public void login() {
        LoginDialog login = new LoginDialog(ossobookFrame);
        do {
            if (login.open()) {
                ossobookFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                QueryManagerFactory.initialize(login.getUser(), login.getPassword());
                ossobookFrame.setCursor(Cursor.getDefaultCursor());
                if (!QueryManagerFactory.isGlobalAvailable() && !QueryManagerFactory.isLocalAvailable()) {
                    showErrorDialog(Messages.getString("GuiController.9"));
                } else {
                    ossobookFrame.getMainMenu().loginCompleted(QueryManagerFactory.isUserAdmin());
                    if ("true".equals(Configuration.config.getProperty("ossobook.checkforupdates"))) {
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                checkForUpdates(true);
                            }
                        }).start();
                    }
                    break;
                }
            } else {
                break;
            }
        } while (true);
        login.dispose();
    }

    /**
	 * Allows the creation of a new project.
	 * 
	 * <p>
	 * This first opens a dialog allowing the user to choose whether to create
	 * the project in the local or global database. If only one database is
	 * available, it is used automatically.
	 * </p>
	 * 
	 * <p>
	 * If a database was chosen, an input box is shown, asking the user for a
	 * project name.
	 * </p>
	 * 
	 * <p>
	 * After successful entry of the name, the project is created in the
	 * selected database. The user will then be notified of either success or
	 * failure due to errors.
	 * </p>
	 */
    private void newProject() {
        QueryManager manager = getManager();
        if (manager == null) {
            return;
        }
        String projectName = JOptionPane.showInputDialog(Messages.getString("ProjectSelectionWindow.20"));
        if (projectName != null) {
            if (projectName.length() > 0) {
                try {
                    manager.newProject(projectName);
                    showNoticeDialog(Messages.getString("GuiController.25"));
                } catch (StatementNotExecutedException e) {
                    log.error(Messages.getString("GuiController.26"), e);
                    showErrorDialog(Messages.getString("GuiController.27"));
                }
            } else {
                showErrorDialog(Messages.getString("GuiController.28"));
            }
        }
    }

    /**
	 * Allows the user to pick a project to open.
	 * 
	 * <p>
	 * This first opens a dialog allowing the user to choose whether to open
	 * from the local or global database. If only one database is available, it
	 * is used automatically.
	 * </p>
	 * 
	 * <p>
	 * Based on the chosen / avaiable database, project selection dialog is
	 * shown, in which the user may pick the project to open.
	 * </p>
	 * 
	 * <p>
	 * If a project was selected, a new project manipulation window is opened.
	 * </p>
	 */
    private void openProject() {
        QueryManager manager = getManager();
        if (manager == null) {
            return;
        }
        ProjectDialog dialog = new ProjectDialog(ossobookFrame, manager, ProjectDialogType.OPEN);
        if (dialog.open()) {
            Project project = dialog.getSelectedProject();
            if (project != null) {
                ProjectManipulation projectFrame = new ProjectManipulation(project.getName(), ossobookFrame, manager, project);
                try {
                    projectFrame.setSelected(true);
                } catch (PropertyVetoException ex) {
                    ex.printStackTrace();
                }
                projectFrame.moveToFront();
            }
        }
        dialog.dispose();
    }

    /**
	 * Allows deleting a project.
	 * 
	 * <p>
	 * This first opens a dialog allowing the user to choose whether to delete
	 * from the local or global database. If only one database is available, it
	 * is used automatically.
	 * </p>
	 * 
	 * <p>
	 * Based on the chosen / avaiable database, project selection dialog is
	 * shown, in which the user may pick the project to delete.
	 * </p>
	 * 
	 * <p>
	 * Finally, a last confirmation is asked, then the project is deleted. The
	 * user will then be notified of either success or failure due to errors.
	 * </p>
	 */
    private void deleteProject() {
        QueryManager manager = getManager();
        if (manager == null) {
            return;
        }
        ProjectDialog dialog = new ProjectDialog(ossobookFrame, manager, ProjectDialogType.DELETE);
        if (dialog.open()) {
            Project project = dialog.getSelectedProject();
            if (project != null) {
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(ossobookFrame, (manager.getConnectionType() == ConnectionType.CONNECTION_LOCAL) ? String.format(Messages.getString("GuiController.29"), project.getName()) : String.format(Messages.getString("GuiController.30"), project.getName()), Messages.getString("GuiController.31"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                    try {
                        manager.deleteProject(project);
                        showNoticeDialog(Messages.getString("GuiController.32"));
                    } catch (StatementNotExecutedException e) {
                        log.error(Messages.getString("GuiController.33"), e);
                        showErrorDialog(Messages.getString("GuiController.34"));
                    } catch (NoWriteRightException e) {
                        log.error(Messages.getString("GuiController.33"), e);
                        showErrorDialog(Messages.getString("GuiController.34"));
                    }
                }
            }
        }
        dialog.dispose();
    }

    /**
	 * Show the application log.
	 * 
	 * <p>
	 * First, tries to open the logfile in the preferred text editor, using the
	 * {@link Desktop#open(File)} functionality. Should that fail for any reason
	 * an internal frame is opened which will be used to display the contents
	 * of the fail.
	 * </p>
	 */
    private void showLog() {
        String logFileDestination;
        try {
            logFileDestination = ((RollingFileAppender) org.apache.log4j.LogManager.getRootLogger().getAppender("FILE")).getFile();
        } catch (Exception e) {
            showErrorDialog(Messages.getString("GuiController.8"));
            return;
        }
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(new File(logFileDestination));
                return;
            } catch (IOException e) {
                log.error(e, e);
            }
        }
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(logFileDestination)));
            try {
                while (bufferedReader.ready()) {
                    text.append(bufferedReader.readLine()).append("\n");
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    log.error(e, e);
                }
            }
        } catch (FileNotFoundException e) {
            log.error(e, e);
        }
        JInternalFrame logwindow = new JInternalFrame(Messages.getString("MainMenuBar.0"));
        logwindow.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
        logwindow.setPreferredSize(new Dimension(900, 600));
        logwindow.setClosable(true);
        logwindow.setResizable(true);
        logwindow.setMaximizable(true);
        logwindow.setIconifiable(true);
        JScrollPane scrollPane = new JScrollPane();
        JTextArea textArea = new JTextArea(text.toString());
        scrollPane.setViewportView(textArea);
        logwindow.setContentPane(scrollPane);
        logwindow.pack();
        textArea.setCaretPosition(textArea.getDocument().getLength() - 1);
        ossobookFrame.getDesktop().add(logwindow);
        logwindow.setVisible(true);
        logwindow.moveToFront();
    }

    /**
	 * Show 'about' dialog box.
	 */
    private void showAbout() {
        JOptionPane.showMessageDialog(ossobookFrame, Messages.getString("GuiController.0"), Messages.getString("GuiController.1"), JOptionPane.INFORMATION_MESSAGE);
    }

    /**
	 * Open "add new animal code" frame.
	 */
    private void addAnimalCode() {
        ProjectManipulation projectFrame = getSelectedProjectFrame();
        if (projectFrame != null) {
            ossobookFrame.getDesktop().add(new NeuerTierCodeFenster(projectFrame.getProjectWindow().getManager()));
        } else {
            showNoProjectSelectedMessage();
        }
    }

    /**
	 * Save skeleton analysis to file.
	 */
    private void performSkeletonAnalysis() {
        ProjectManipulation projectFrame = getSelectedProjectFrame();
        if (projectFrame == null) {
            showNoProjectSelectedMessage();
            return;
        }
        Project project = projectFrame.getProjectWindow().getProject();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("GuiController.37"), "txt"));
        if (JFileChooser.APPROVE_OPTION != fileChooser.showSaveDialog(ossobookFrame)) {
            return;
        }
        String path = fileChooser.getSelectedFile().getAbsolutePath();
        if (!path.endsWith(".txt")) {
            path += ".txt";
        }
        File f = new File(path);
        String result = JOptionPane.showInputDialog(ossobookFrame.getDesktop(), Messages.getString("MainMenuBar.88"), "26");
        if (result == null) {
            return;
        } else if (result.length() == 0) {
            showErrorDialog(Messages.getString("GuiController.40"));
            return;
        }
        int tiercode;
        try {
            tiercode = Integer.parseInt(result);
        } catch (NumberFormatException e) {
            showErrorDialog(Messages.getString("GuiController.41"));
            return;
        }
        result = JOptionPane.showInputDialog(ossobookFrame.getDesktop(), Messages.getString("MainMenuBar.90"), "fK,phase1,phase2,phase3,phase4");
        if (result == null) {
            return;
        }
        try {
            CSVExport export = new CSVExport(projectFrame.getProjectWindow().getManager().getConnection());
            export.exportSkelettListe(f, project, tiercode, result);
        } catch (Exception e) {
            showErrorDialog(Messages.getString("GuiController.42"));
        }
    }

    /**
	 * Save animal analysis to file.
	 */
    private void performAnimalAnalysis() {
        ProjectManipulation projectFrame = getSelectedProjectFrame();
        if (projectFrame == null) {
            showNoProjectSelectedMessage();
            return;
        }
        Project project = projectFrame.getProjectWindow().getProject();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("GuiController.37"), "txt"));
        if (JFileChooser.APPROVE_OPTION != fileChooser.showSaveDialog(ossobookFrame)) {
            return;
        }
        String path = fileChooser.getSelectedFile().getAbsolutePath();
        if (!path.endsWith(".txt")) {
            path += ".txt";
        }
        File f = new File(path);
        try {
            CSVExport export = new CSVExport(projectFrame.getProjectWindow().getManager().getConnection());
            export.exportTierartenListe(f, project);
        } catch (Exception e) {
            showErrorDialog(Messages.getString("GuiController.42"));
        }
    }

    /**
	 * Opens the lookup table for the given type of content.
	 * 
	 * @param type
	 *            the event type for which to open the table.
	 */
    private void lookupCode(EventType type) {
        ProjectManipulation projectFrame = getSelectedProjectFrame();
        if (projectFrame == null) {
            showNoProjectSelectedMessage();
            return;
        }
        ProjektFenster projectWindow = projectFrame.getProjectWindow();
        String query = queryMapping.get(type);
        int suchmodus = constantMapping.get(type);
        TabellenAnsicht result;
        try {
            CSVExport export = new CSVExport(projectWindow.getManager().getConnection());
            result = export.showResultTable(query, projectWindow, suchmodus);
        } catch (Exception f) {
            showErrorDialog(Messages.getString("GuiController.47"));
            return;
        }
        if (result != null) {
            JInternalFrame intFrame = new JInternalFrame();
            result.setParentContainer(intFrame);
            intFrame.setSize(400, 500);
            intFrame.setClosable(true);
            intFrame.setResizable(true);
            JScrollPane sp = new JScrollPane(result);
            intFrame.getContentPane().add(sp);
            intFrame.setVisible(true);
            ossobookFrame.getDesktop().add(intFrame);
            try {
                intFrame.setSelected(true);
            } catch (PropertyVetoException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
	 * Get a database connection to use.
	 * 
	 * <p>
	 * If more than one database is available, i.e. the local and the global one
	 * are active simultaneously, a dialog is shown, allowing the user to pick
	 * which database to use.
	 * </p>
	 * 
	 * <p>
	 * If only one database is available it is automatically used.
	 * </p>
	 * 
	 * <p>
	 * If no databases are available, or the factory fails to produce a valid
	 * connection, <code>null</code> is returned. If no databases are available,
	 * additionally, an error message is displayed, notifying the user about
	 * that.
	 * </p>
	 */
    private QueryManager getManager() {
        if (QueryManagerFactory.isGlobalAvailable() && QueryManagerFactory.isLocalAvailable()) {
            DatabaseSelectionDialog selection = new DatabaseSelectionDialog(ossobookFrame);
            if (selection.open()) {
                ConnectionType type = selection.getDatabaseType();
                selection.dispose();
                switch(type) {
                    case CONNECTION_GLOBAL:
                        return QueryManagerFactory.getGlobalManager();
                    case CONNECTION_LOCAL:
                        return QueryManagerFactory.getLocalManager();
                }
            }
        } else if (QueryManagerFactory.isGlobalAvailable()) {
            return QueryManagerFactory.getGlobalManager();
        } else if (QueryManagerFactory.isLocalAvailable()) {
            return QueryManagerFactory.getLocalManager();
        } else {
            showErrorDialog(Messages.getString("GuiController.3"));
        }
        return null;
    }

    /**
	 * Get the currently active project window, or, if none is active, the
	 * first one found on the desktop.
	 * 
	 * <p>
	 * If no projects are open, the function returns <code>null</code>.
	 * </p>
	 * 
	 * @return project window or <code>null</code>.
	 */
    private ProjectManipulation getSelectedProjectFrame() {
        if (ossobookFrame.getDesktop().getSelectedFrame() instanceof ProjectManipulation) {
            return (ProjectManipulation) ossobookFrame.getDesktop().getSelectedFrame();
        } else {
            for (JInternalFrame frame : ossobookFrame.getDesktop().getAllFrames()) {
                if (frame instanceof ProjectManipulation) {
                    frame.moveToFront();
                    try {
                        frame.setSelected(true);
                    } catch (PropertyVetoException ex) {
                        ex.printStackTrace();
                    }
                    return (ProjectManipulation) frame;
                }
            }
            return null;
        }
    }

    /**
	 * Show message that a project must be opened first.
	 */
    private void showNoProjectSelectedMessage() {
        showNoticeDialog(Messages.getString("GuiController.48"));
    }

    /**
	 * For simplified creation of notification typed message dialogs.
	 * 
	 * @param message
	 *            the message to display.
	 */
    private void showNoticeDialog(String message) {
        JOptionPane.showMessageDialog(ossobookFrame, message, Messages.getString("GuiController.49"), JOptionPane.INFORMATION_MESSAGE);
    }

    /**
	 * For simplified creation of warning typed message dialogs.
	 * 
	 * @param message
	 *            the message to display.
	 */
    private void showWarnDialog(String message) {
        JOptionPane.showMessageDialog(ossobookFrame, message, Messages.getString("GuiController.50"), JOptionPane.WARNING_MESSAGE);
    }

    /**
	 * For simplified creation of error typed message dialogs.
	 * 
	 * @param message
	 *            the message to display.
	 */
    private void showErrorDialog(final String message) {
        Runnable showError = new Runnable() {

            @Override
            public void run() {
                JOptionPane.showMessageDialog(ossobookFrame, message, Messages.getString("GuiController.2"), JOptionPane.ERROR_MESSAGE);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            showError.run();
        } else {
            SwingUtilities.invokeLater(showError);
        }
    }
}
