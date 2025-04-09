package dream.setup;

import javax.swing.*;
import javax.swing.text.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Vector;
import java.awt.event.*;
import dream.util.*;
import java.util.jar.*;

/**
 *
 * Provides a wizard that helps with Dream installation.
 *
 * @author  mike
 * @version 0.2
 */
public class Setup extends javax.swing.JFrame implements WindowListener {

    /** Current release version this setup belongs to,
     * see Base.RELEASE_VERSION. */
    public int DREAM_VERSION = 210000;

    /** Property name for the dream version stored in installer props. */
    public static final String DREAM_VERSION_PROP = "DreamVersion";

    /** Property name for the dream installation directory. */
    public static final String DREAM_INSTALLDIR_PROP = "DreamInstallationDir";

    /** Step is the number of the active installation step. */
    int step = 0;

    /** Runtime object used for controlling external processes. */
    protected Runtime run = Runtime.getRuntime();

    public PropertyHandler propHandler = PropertyHandler.getDefaultHandler();

    /** Default width of this frame. */
    private static int DEFAULT_WIDTH = 700;

    /** Default height of this frame. */
    private static int DEFAULT_HEIGHT = 500;

    /** Step for the welcome screen. */
    private static final int STEP_WELCOME = 0;

    /** Step for file selection panel. */
    private static final int STEP_FILE_SELECT = 1;

    /** Step for copying files and showing progress bars. */
    private static final int STEP_FILE_COPY = 2;

    /** Step for detecting os and java environment. */
    private static final int STEP_JAVA_DETECT = 3;

    /** Step for installing windows shortcuts and registry entries. */
    private static final int STEP_WINDOWS_COMPONENTS = 4;

    /** Step for installing linux windows shortcuts and scripts. */
    private static final int STEP_LINUX_COMPONENTS = 5;

    /** Step for watching the progress of steps 4 + 5. */
    private static final int STEP_COMPONENTS_PROGRESS = 6;

    /** Step for searching old DREAM installation. */
    private static final int STEP_ANALYSIS = 8;

    /** Uninstallation step. */
    private static final int STEP_UNINSTALL = 9;

    /** Step for showing installation info and enable startup of node and Console. */
    private static final int STEP_SHOW_INFO = 99;

    /** Step for exit page with last informations. */
    private static final int STEP_OUTRO = 100;

    /** This is the name of the subdirectory that is established inside the users home
     *  directory to store the Dream preferences. */
    public static final String DREAM_PREFS_DIR_SUFFIX = ".dream";

    /**  default directory for the Dream installation. */
    public static final String DREAM_DEFAULT_DIR = "dr-ea-m";

    /** Color definition for warning messages. */
    public static final Color WARNING_COLOR = new Color(120, 32, 32);

    /** Message to the user in case an unimplemented feature is shown. */
    public static final String FEATURE_UNIMPLEMENTED = "This feature is not implemented yet. " + "Please proceed to the next step or select the finish button.";

    /** Waiting time in milliseconds when about to start a watched (progress bar) action. */
    public static final int BOP_WAIT = 60;

    /** Needed global for progress bar update via timer. */
    private long written = 0;

    /** Needed global for progress bar update via timer. */
    private long length = 0;

    /** counter needed for recursive actions. */
    private int globalCount = 0;

    /** Java installation ok? */
    private boolean javaOk = false;

    /** Preferences dir ok? */
    private boolean prefsOk = false;

    /** Os specific components ok? */
    private boolean compOk = false;

    /** If set true, we are switching to uninstallation. */
    private boolean uninstallMode = false;

    /** If set true during installation, we need to give advise at the end. */
    private boolean globalNodeStartupHelp = false;

    /** If true, we are installing into a new directory. */
    private boolean newInstall = false;

    /** If set true, button reaction is disabled. */
    private boolean buttonsDisabled = false;

    /** Operating system name */
    public String os = System.getProperty("os.name");

    /** User name */
    public String user = System.getProperty("user.name");

    /** Java version number */
    public double javaVersion = 0.0;

    /** Java version and vendor */
    public String javaName = System.getProperty("java.version") + ", " + System.getProperty("java.vendor");

    /** Java installation directory */
    public String javaDir = System.getProperty("java.home");

    /** Java extensions directory */
    public String javaExtDir = System.getProperty("java.ext.dirs");

    /** Java vendor URL */
    public String javaVendorUrl = System.getProperty("java.vendor.url");

    /** User home directory */
    public String userHome = System.getProperty("user.home");

    /** Dream installation directory */
    public String installDir = getInstallPath(true);

    /** Name of directory used for Dream preferences */
    public String dreamPrefsDir = userHome + File.separator + DREAM_PREFS_DIR_SUFFIX;

    /** Path to the new jar file or the directory where the
     * Dream is already installed. */
    public String installClassPath = getInstallPath(false);

    /** Path to the directory where the dream is installed (if so). */
    public String dreamPath = getDreamLocation(dreamPrefsDir);

    /** Standard path for installing Windows programs, from the registry. */
    public String windowsDefaultPath = "";

    /** Holds the permission string to add to various startup scripts. */
    public String permOptionString = "";

    public Properties installerProps = null;

    /** Windows 98 registry key path. */
    public String key98 = "HKEY_LOCAL_MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";

    /** Windows 2000 registry key path. */
    public String key2000user = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";

    /** Old registry key. */
    public String oldDreamKey = "DreamServent";

    /** New registry key. */
    public String dreamKey = "DreamNode";

    /** Universal windows key to locate system specific user folders. */
    public static String EXPLORER_FOLDERS_KEY = "\"HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\"";

    /** Properties found in the windows shell folders key. */
    Properties winUserProps = null;

    /** All steps: progress watch */
    public ProgressWatchPanel progressPanel = null;

    /** Step 3: test java environment */
    public TestJavaPanel javaPanel = null;

    /** Step 4: windows specific installations */
    public WindowsComponentsPanel windowsPanel = null;

    /** Step 8: analysis of existing DREAM installation. */
    public AnalysisPanel analysisPanel = null;

    /** Panel for storing results of the different steps. */
    public ReportPanel reportPanel = null;

    /** Panel for selecting the dream installation directory. */
    public FileSelectPanel fileSelectPanel = null;

    /** We have been called by this object. */
    public Object callee = null;

    /** This archive file has to be unzipped. */
    public String archive = null;

    /** Creates new form Setup */
    public Setup() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        initComponents();
        setup();
    }

    /** Creates new form Setup running in archive mode (with a given
     *  zip file). */
    public Setup(String archive) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (archive != null) this.archive = archive;
        initComponents();
        setup();
    }

    /** Creates new form Setup as a client worker to the calling object.
     * In opposite to the default constructor, no change is done to
     * the look-and-feel. */
    public Setup(Object callee) {
        this.callee = callee;
        initComponents();
        setup();
    }

    /** Does some configuration work. */
    public void setup() {
        Toolkit tool = Toolkit.getDefaultToolkit();
        Dimension screenSize = tool.getScreenSize();
        Image img = tool.createImage(getClass().getResource("/dream/resources/icons/dream-icon.gif"));
        leftPanel.setOpaque(true);
        leftPanel.setForeground(Color.white);
        leftPanel.setBackground(new Color(0, 153, 51));
        leftPanel.setPreferredSize(new Dimension(220, 400));
        leftPanel.setMaximumSize(new Dimension(220, 400));
        leftPanel.setMinimumSize(new Dimension(220, 400));
        validate();
        if (img != null) setIconImage(img);
        addWindowListener(this);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.setLocation((screenSize.width - DEFAULT_WIDTH) / 2, (screenSize.height - DEFAULT_HEIGHT) / 2);
        setTitle("DREAM Setup Wizard (Install/Uninstall)");
        mainPanel.remove(textPanel);
        textPanel.setEditable(false);
        textPanel.setForeground(uiLabel.getForeground());
        textPanel.setBackground(uiLabel.getBackground());
        textPanel.setFont(uiLabel.getFont());
        reportPanel = new ReportPanel(uiLabel);
        enter(STEP_WELCOME);
        if (os.indexOf("Windows") >= 0) {
            winUserProps = RegEdit.getKey(EXPLORER_FOLDERS_KEY);
        }
    }

    /** Reads the given file into a vector of strings which is returned. */
    private static Vector readIntoBuffer(File toRead) {
        Vector answer = null;
        Vector tmp = new Vector();
        if ((toRead == null) || (!toRead.exists())) return null;
        try {
            LineNumberReader lin = new LineNumberReader(new FileReader(toRead));
            while (lin.ready()) {
                tmp.add(lin.readLine());
            }
            answer = tmp;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return answer;
    }

    /** Takes a vector of strings and writes it to the file denoted by
     *  target. If target already exists, it is deleted and newly created.
     *  Returns true on success, false otherwise. */
    private static boolean writeBufferToFile(Vector filebuf, File target) {
        boolean answer = false;
        try {
            if (target.exists()) target.delete();
            target.createNewFile();
            if (target.exists()) {
                FileWriter lout = new FileWriter(target);
                for (int i = 0; i < filebuf.size(); i++) {
                    lout.write((String) filebuf.get(i));
                }
                lout.close();
                answer = true;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return answer;
    }

    /** Called for entering a new step, establishes appropriate display. */
    private void enter(int step) {
        this.step = step;
        switch(step) {
            case STEP_WELCOME:
                installStepLabel.setText("Welcome to the Dream");
                backButton.setEnabled(false);
                nextButton.setEnabled(true);
                getRootPane().setDefaultButton(nextButton);
                finishButton.setEnabled(false);
                String welcomeText = "Dear " + user + ", welcome to the Dream!\n\n" + "This wizard will lead you through the Dream setup on your machine.\n" + "At first, the system is searched for existing DREAM installations. " + "If old components are found, you will be offered the possibility " + "of removing them (uninstall the DREAM) or updating to the current " + "version.\n\n" + "In case of installation or update, a preferences directory " + "will be established/updated and a new property file written" + " therein.\n" + "In case of uninstallation, the preferences directory, " + "the dream installation directory " + "and some registry keys (Windows only) will be deleted.\n\n";
                if (archive != null) {
                    if (dreamPath != null) {
                        welcomeText += "As the wizard has been started from an " + "installation archive, the new files will be copied" + " to the same location as your installed version. " + "If you want to put them elsewhere, please uninstall" + " the Dream software first.";
                    } else {
                        welcomeText += "As the wizard has been started from an " + "installation archive, you will be prompted for an " + "installation location shortly.";
                        newInstall = true;
                    }
                } else {
                    welcomeText += "The directory this wizard has been started " + "from will not be touched.";
                    copyFilesCheckBox.setEnabled(false);
                }
                welcomeText += "\n\nPlease always use the buttons below to continue to the " + "next step after you " + "have finished doing your settings. ";
                textPanel.setText(welcomeText);
                mainPanel.add(textPanel);
                break;
            case STEP_FILE_SELECT:
                installStepLabel.setText(copyFilesCheckBox.getText());
                backButton.setEnabled(true);
                getRootPane().setDefaultButton(nextButton);
                finishButton.setEnabled(false);
                fileSelectPanel = new FileSelectPanel(uiLabel);
                nextButton.setEnabled(false);
                fileSelectPanel.setSelectButton(nextButton);
                mainPanel.add(fileSelectPanel);
                validate();
                JFileChooser fileChoose = fileSelectPanel.getFileChooser();
                if ((os.indexOf("Windows") >= 0) && (windowsDefaultPath != null)) {
                    synchronized (windowsDefaultPath) {
                        if (windowsDefaultPath.length() > 0) {
                            fileChoose.setCurrentDirectory(new File(windowsDefaultPath));
                            fileChoose.setSelectedFile(new File(windowsDefaultPath));
                        }
                    }
                } else {
                    File defaultDir = new File(File.separator + "usr");
                    if (!defaultDir.exists()) defaultDir.mkdirs();
                    fileChoose.setCurrentDirectory(defaultDir);
                    fileChoose.setSelectedFile(defaultDir);
                }
                break;
            case STEP_FILE_COPY:
                report("Beginning archive unpacking...\n");
                installStepLabel.setText(copyFilesCheckBox.getText());
                progressPanel = new ProgressWatchPanel(uiLabel);
                progressPanel.singleLabel.setText("current file progress");
                getRootPane().setDefaultButton(nextButton);
                backButton.setEnabled(true);
                nextButton.setEnabled(false);
                mainPanel.add(progressPanel);
                bopRun(new Runnable() {

                    public void run() {
                        if (doFileCopy(dreamPath, progressPanel)) {
                            nextButton.setEnabled(true);
                            getRootPane().setDefaultButton(nextButton);
                            copyFilesCheckBox.setSelected(true);
                            validate();
                        } else {
                            nextButton.setEnabled(false);
                            getRootPane().setDefaultButton(cancelButton);
                        }
                    }
                });
                break;
            case STEP_JAVA_DETECT:
                report("Beginning JAVA validation/preferences setup phase...\n");
                installStepLabel.setText(validateJavaCheckBox.getText());
                getRootPane().setDefaultButton(nextButton);
                backButton.setEnabled(true);
                nextButton.setEnabled(true);
                javaPanel = new TestJavaPanel(uiLabel);
                File prefsDir = new File(dreamPrefsDir);
                prefsOk = prefsDir.exists();
                if (!prefsOk) prefsOk = prefsDir.mkdirs();
                if (prefsOk) {
                    report("Dream preferences directory found/established: " + dreamPrefsDir + "\n");
                    javaPanel.prefsDirTextPane.setText("The Dream preferences directory has been established.\n");
                    if (installerProps == null) installerProps = propHandler.fetch("installer.properties", null, dreamPrefsDir, null, null, null);
                    try {
                        File permFile = new File(dreamPrefsDir + File.separator + "dream.policy");
                        if (permFile.exists()) {
                            permFile.delete();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    installerProps.remove("ServentCommandLine");
                    installerProps.setProperty("NodeCommandLine", this.getNodeCommandLine());
                    installerProps.setProperty(DREAM_INSTALLDIR_PROP, dreamPath);
                    installerProps.setProperty(DREAM_VERSION_PROP, String.valueOf(((double) DREAM_VERSION / 100000)));
                    if (newInstall) {
                        installerProps.setProperty("ArchiveInstall", String.valueOf(((double) DREAM_VERSION / 100000)));
                    }
                    File drmconfig = new File(prefsDir + File.separator + "drm.config");
                    Vector filebuf = null;
                    if (drmconfig.exists()) {
                        filebuf = readIntoBuffer(drmconfig);
                        if (filebuf != null) {
                            boolean found = false;
                            for (int i = 0; i < filebuf.size(); i++) {
                                if (((String) filebuf.elementAt(i)).trim().startsWith("DRM_HOME")) {
                                    filebuf.removeElementAt(i);
                                    filebuf.insertElementAt("DRM_HOME " + dreamPath, i);
                                    found = true;
                                }
                            }
                            if (!found) {
                                filebuf.add("DRM_HOME " + dreamPath);
                            }
                        }
                    }
                    if (filebuf == null) {
                        filebuf = new Vector();
                        filebuf.add("DRM_HOME " + dreamPath);
                    }
                    boolean success = writeBufferToFile(filebuf, drmconfig);
                    if (success) report("Adjusted DRM_HOME statement in " + "config file " + drmconfig.getAbsolutePath() + " .\n"); else report("Failed to write DRM_HOME statement in " + "config file " + drmconfig.getAbsolutePath() + " .\n");
                } else {
                    javaPanel.prefsDirTextPane.setForeground(WARNING_COLOR);
                    javaPanel.prefsDirTextPane.setText("Warning! The Dream preferences directory could not be established. " + "The Dream software may not run properly. You may consider solving the file system problem and " + "restarting the installation.");
                }
                javaPanel.propsTableModel.addRow(new Object[] { "operating system", os });
                javaPanel.propsTableModel.addRow(new Object[] { "Java version", javaName });
                javaPanel.propsTableModel.addRow(new Object[] { "Java installation directory", javaDir });
                javaPanel.propsTableModel.addRow(new Object[] { "user home directory", userHome });
                javaPanel.propsTableModel.addRow(new Object[] { "Dream installation location", dreamPath });
                javaPanel.propsTableModel.addRow(new Object[] { "Dream preferences directory", dreamPrefsDir });
                try {
                    String javaTemp = System.getProperty("java.version");
                    if (javaTemp.length() > 3) javaTemp = javaTemp.substring(0, 3);
                    javaVersion = Double.parseDouble(javaTemp);
                } catch (NumberFormatException ex) {
                    javaVersion = 1.0;
                }
                javaOk = false;
                if (javaVersion >= 1.4) {
                    javaPanel.resultTextPane.setText("Congratulations, your installed Java " + "system is fully compatible to the Dream software. You may proceed " + "to the next step.");
                    report("Java system validated ok.\n");
                    validateJavaCheckBox.setSelected(true);
                    javaOk = true;
                } else {
                    javaPanel.resultTextPane.setForeground(WARNING_COLOR);
                    javaPanel.resultTextPane.setText("Warning! Your installed Java " + "systems version number is too low and may not be fully compatible " + "to the Dream software. You should consider installing a newer " + "version from " + javaVendorUrl + " . However, you may proceed on " + "your own risk.");
                    report("Warning: installed JAVA system is too old " + "(should be 1.4.1).\n");
                }
                javaPanel.validate();
                mainPanel.add(javaPanel);
                break;
            case STEP_WINDOWS_COMPONENTS:
                installStepLabel.setText(installComponentsCheckBox.getText());
                getRootPane().setDefaultButton(nextButton);
                backButton.setEnabled(true);
                nextButton.setEnabled(true);
                windowsPanel = new WindowsComponentsPanel(uiLabel);
                mainPanel.add(windowsPanel);
                break;
            case STEP_LINUX_COMPONENTS:
                report("Beginning non-Windows specific component " + "installation...\n");
                textPanel.setText("No components available for non-Windows" + " systems.\n\nPlease proceed with the \"next\" button.\n");
                mainPanel.add(textPanel);
                installStepLabel.setText(installComponentsCheckBox.getText());
                getRootPane().setDefaultButton(nextButton);
                backButton.setEnabled(true);
                nextButton.setEnabled(true);
                report("Nothing to do (only Windows specific components known" + " yet.\n\n");
                installComponentsCheckBox.setSelected(true);
                break;
            case STEP_COMPONENTS_PROGRESS:
                installStepLabel.setText(installComponentsCheckBox.getText());
                getRootPane().setDefaultButton(nextButton);
                progressPanel = new ProgressWatchPanel(uiLabel);
                if (os.indexOf("Windows") >= 0) {
                    bopRun(new Runnable() {

                        public void run() {
                            doWindowsInstall(windowsPanel.automaticServentCheckBox.isSelected(), windowsPanel.quickLaunchCheckBox.isSelected(), windowsPanel.desktopCheckBox.isSelected(), windowsPanel.startMenuCheckBox.isSelected());
                        }
                    });
                } else {
                }
                mainPanel.add(progressPanel);
                break;
            case STEP_ANALYSIS:
                backButton.setEnabled(true);
                nextButton.setEnabled(true);
                installStepLabel.setText(analyzeCheckBox.getText());
                analysisPanel = new AnalysisPanel(uiLabel, this);
                switchInstallMode(true);
                doAnalysis();
                if ((os.indexOf("Windows") >= 0) && (windowsDefaultPath != null) && (windowsDefaultPath.length() < 1)) {
                    nextButton.setEnabled(false);
                    bopRun(new Runnable() {

                        public void run() {
                            synchronized (windowsDefaultPath) {
                                RegEdit.progressMonitorParent = null;
                                windowsDefaultPath = RegEdit.getValue("HKEY_LOCAL_MACHINE\\Software\\Microsoft\\Windows\\CurrentVersion", "ProgramFilesDir");
                                nextButton.setEnabled(true);
                            }
                        }
                    });
                }
                mainPanel.add(analysisPanel);
                break;
            case STEP_UNINSTALL:
                installStepLabel.setText(uninstallCheckBox.getText());
                progressPanel = new ProgressWatchPanel(uiLabel);
                progressPanel.doWhatTextPane.setText("Please stand by " + "while uninstall is in progress.");
                mainPanel.add(progressPanel);
                mainPanel.validate();
                bopRun(new Runnable() {

                    public void run() {
                        performUninstall();
                    }
                });
                break;
            case STEP_SHOW_INFO:
                installStepLabel.setText(showInfoCheckBox.getText());
                nextButton.setEnabled(true);
                backButton.setEnabled(true);
                cancelButton.setEnabled(true);
                getRootPane().setDefaultButton(nextButton);
                mainPanel.add(reportPanel);
                showInfoCheckBox.setSelected(true);
                break;
            case STEP_OUTRO:
                installStepLabel.setText("Finish installation");
                backButton.setEnabled(true);
                finishButton.setEnabled(true);
                nextButton.setEnabled(false);
                cancelButton.setEnabled(false);
                getRootPane().setDefaultButton(nextButton);
                if (uninstallMode) {
                    String uninstText = "Uninstallation of the DREAM " + "software is finished now.\n\n";
                    if (new File(dreamPath).exists()) uninstText += "If you want to remove the sofware from your " + "system completely, please delete the directory " + dreamPath + " manually after exiting this wizard.\n";
                    textPanel.setText(uninstText);
                } else {
                    String instEndText = "Installation/update of the DREAM " + "software is finished now.\n\n" + "You may start working with the DREAM software now.\n" + "Please consider having a look at the documents you " + "find in the DREAM installation directory \"" + dreamPath + "\" . Further support can also be found at the project " + "webpage \"http://www.dr-ea-m.org\" .\n\n";
                    if (globalNodeStartupHelp) {
                        instEndText += "Be reminded that you installed a start " + "mechanism for the Node." + " A Dream Node will be started on" + " your computer every time it is booted now, it will " + "run in the background and can only be made " + "visible by process view tools or Dream GUI tools " + "like the Console.\n\n";
                    }
                    instEndText += "Have a fun with the DREAM!\n";
                    textPanel.setText(instEndText);
                }
                installerProps.setProperty("DreamInstalled", "true");
                mainPanel.add(textPanel);
                break;
            default:
        }
        validate();
    }

    /** Called for leaving a step, removes visible components and listeners. */
    private void leave(int step) {
        switch(step) {
            case STEP_WELCOME:
                mainPanel.remove(textPanel);
                break;
            case STEP_FILE_SELECT:
                mainPanel.remove(fileSelectPanel);
                break;
            case STEP_FILE_COPY:
                mainPanel.remove(progressPanel);
                report("File unpacking phase finished.\n\n");
                break;
            case STEP_JAVA_DETECT:
                mainPanel.remove(javaPanel);
                report("JAVA validation/preferences setup phase finished.\n\n");
                break;
            case STEP_WINDOWS_COMPONENTS:
                mainPanel.remove(windowsPanel);
                break;
            case STEP_LINUX_COMPONENTS:
                mainPanel.remove(textPanel);
                break;
            case STEP_COMPONENTS_PROGRESS:
                mainPanel.remove(progressPanel);
                break;
            case STEP_ANALYSIS:
                if (analysisPanel.uninstallRadioButton.isSelected()) {
                    uninstallMode = true;
                    report("Switching to uninstall mode.\n");
                } else {
                    uninstallMode = false;
                    report("Switching to install/update mode.\n");
                }
                report("Analysis phase finished.\n\n");
                mainPanel.remove(analysisPanel);
                break;
            case STEP_UNINSTALL:
                mainPanel.remove(progressPanel);
                break;
            case STEP_SHOW_INFO:
                mainPanel.remove(reportPanel);
                break;
            case STEP_OUTRO:
                mainPanel.remove(textPanel);
                finishButton.setEnabled(false);
                break;
            default:
        }
        validate();
    }

    /** Exports some ui settings from the first to the second component. */
    public static void copyUISettings(JComponent from, JComponent to) {
        to.setBackground(from.getBackground());
        to.setForeground(from.getForeground());
        to.setFont(from.getFont());
    }

    /** Waits for the event handler thread to enable windows operations while starting
     *  progress bar actions, should only be called while executing in a seperate
     *  thread (not inside the event handler thread). */
    public void bopWait() {
        try {
            Thread.currentThread().sleep(BOP_WAIT);
        } catch (InterruptedException ex) {
            ;
        }
    }

    /** Same as bop wait, but waits only a third of the time. */
    public void shortBopWait() {
        try {
            Thread.currentThread().sleep(BOP_WAIT / 3);
        } catch (InterruptedException ex) {
            ;
        }
    }

    /** Builds a thread from the given Runnable and starts it as a new thread. */
    public void bopRun(final Runnable toRun) {
        Thread waitingThread = new Thread(new Runnable() {

            public void run() {
                toRun.run();
                buttonsDisabled = false;
            }
        });
        buttonsDisabled = true;
        if (waitingThread != null) {
            waitingThread.start();
        }
    }

    /** Returns the URL to the jar file this application has been started
     * from or null if it has been started from class files. */
    public URL getMyJarURL() {
        String myClassName = this.getClass().getName().replace('.', '/');
        URL myself = ClassLoader.getSystemResource(myClassName + ".class");
        String myselfString = myself.toExternalForm();
        if (myselfString.startsWith("jar:")) {
            try {
                return new URL(myselfString.substring(4, myselfString.indexOf("!")));
            } catch (MalformedURLException ex) {
                ;
            }
        }
        return null;
    }

    /** Returns the canonical path to the classes this application has been started
     * from or null if it has been started from a jar file.  */
    public String getClassPath() {
        String myClassName = this.getClass().getName().replace('.', '/');
        URL myself = ClassLoader.getSystemResource(myClassName + ".class");
        String myselfPath = null;
        myselfPath = myself.getPath();
        File classDir = new File(myselfPath);
        if (classDir != null) {
            try {
                return classDir.getCanonicalPath();
            } catch (IOException ex) {
                ;
            }
        }
        return null;
    }

    /** Returns the canonical file system path to the jar file or class directory
     *  this application has been started from. */
    public String getInstallPath(boolean stripJarSuffix) {
        URL jarPath = getMyJarURL();
        if (jarPath != null) {
            String jar = new File(jarPath.getPath()).getAbsolutePath();
            if (stripJarSuffix) {
                if (jar.lastIndexOf(File.separator) >= 0) {
                    jar = jar.substring(0, jar.lastIndexOf(File.separator));
                } else {
                    jar = ".";
                }
            }
            return jar;
        } else {
            return getClassPath();
        }
    }

    /** Does the file installation process, copies from this.archive . */
    public boolean doFileCopy(String targetDir, ProgressWatchPanel progressWatch) {
        if (targetDir == null) {
            progressPanel.reportTextPane.setText("No target directory given, " + "unpacking cancelled.\n");
            report("Unpacking failed, no target directory given.\n");
            return false;
        }
        if (targetDir.endsWith(DREAM_DEFAULT_DIR)) {
            targetDir = targetDir.substring(0, targetDir.length() - DREAM_DEFAULT_DIR.length());
        }
        File archiveFile = new File(archive);
        if (!archiveFile.exists()) {
            progressPanel.reportTextPane.setText("Archive file " + archive + " not accessible, unpacking cancelled.\n");
            report("Unpacking failed, archive file not accessible.\n");
            return false;
        }
        int filesToCopy = 0;
        int filesCopied = 0;
        try {
            JarFile jar = new JarFile(archive);
            Enumeration entries = jar.entries();
            filesToCopy = jar.size();
            if (progressWatch != null) {
                progressWatch.doWhatTextPane.setText("Unpacking archive " + archive + " (" + filesToCopy + " files) to " + targetDir + " .");
            }
            report("Unpacking archive " + archive + " (" + filesToCopy + " files) to " + targetDir + " .");
            progressPanel.reportTextPane.setText("Waiting for file copy.\n");
            mainPanel.validate();
            bopWait();
            JarEntry actEntry = null;
            while (entries.hasMoreElements()) {
                actEntry = (JarEntry) entries.nextElement();
                if (!actEntry.isDirectory()) {
                    if (fileCopy(jar.getInputStream(actEntry), targetDir + File.separator + actEntry.getName(), progressWatch)) filesCopied++;
                } else {
                    filesCopied++;
                }
                progressPanel.overallProgressBar.setValue((int) ((filesCopied * 100) / filesToCopy));
                progressPanel.overallSizeLabel.setText(String.valueOf(filesCopied) + " of " + String.valueOf(filesToCopy) + " files");
                progressPanel.reportTextPane.setText("");
            }
            jar.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (filesCopied == filesToCopy) {
            progressPanel.reportTextPane.setText(progressPanel.reportTextPane.getText() + "Unpacking files ok, you may proceed to the next step now.\n");
            nextButton.setEnabled(true);
        } else {
            progressPanel.reportTextPane.setText(progressPanel.reportTextPane.getText() + "Something went wrong with the file copy, please go back or cancel.\n");
            return false;
        }
        return true;
    }

    /** Copies a file from the source to the target. */
    public boolean fileCopy(InputStream source, String target, final ProgressWatchPanel progressPanel) {
        written = 0;
        length = 1000000;
        Timer progressTimer = new Timer(33, new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                if (length > 0) {
                    progressPanel.singleFileProgressBar.setValue((int) ((written * 100) / length));
                } else {
                    progressPanel.singleFileProgressBar.setValue(100);
                }
                progressPanel.singleSizeLabel.setText(written + " of " + length + " bytes");
                progressPanel.validate();
            }
        });
        try {
            BufferedInputStream in = new BufferedInputStream(source);
            length = in.available();
            if (length > 0) {
                progressTimer.setRepeats(true);
                progressTimer.start();
            }
            File targetFile = new File(target);
            if (!targetFile.getParentFile().exists()) targetFile.getParentFile().mkdirs();
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(target));
            int c;
            while ((c = in.read()) != -1) {
                out.write(c);
                written++;
            }
            in.close();
            out.close();
            if (length > 0) {
                progressTimer.stop();
                progressTimer.setRepeats(false);
                progressTimer.start();
            }
        } catch (Exception ex) {
            System.err.println(ex);
            return false;
        }
        return true;
    }

    /** Recursively counts the number of files under the current file
     * (including itself). */
    public int countFiles(File root) {
        int answer = 1;
        File[] fileList = null;
        if (root == null) return 0;
        if (root.isDirectory()) {
            fileList = root.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                answer += countFiles(fileList[i]);
            }
        }
        return answer;
    }

    /** Recursively removes the files under the current file
     * (including itself). */
    public int removeFiles(File root, JProgressBar toUpdate, int min, int max, int numOfFiles) {
        int answer = 1;
        File[] fileList = null;
        if (root == null) return 0;
        if (numOfFiles == 0) return 0;
        if (root.isDirectory()) {
            fileList = root.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                answer += removeFiles(fileList[i], toUpdate, min, max, numOfFiles);
            }
        }
        root.delete();
        globalCount++;
        toUpdate.setValue(min + ((globalCount * (max - min) / numOfFiles)));
        mainPanel.validate();
        return answer;
    }

    /** Recursively looks out for files of a special type (suffix)
     *  under the root file (including itself) and returns the
     *  number of occurances. countFiles has to be called in advance
     *  to determine the available number of files. */
    public int checkFiles(File root, JProgressBar toUpdate, int min, int max, int numOfFiles, String[] suffixes) {
        int answer = 0;
        File[] fileList = null;
        if (root == null) return 0;
        if (numOfFiles == 0) return 0;
        if ((suffixes == null) || (suffixes.length == 0)) return 0;
        if (root.isDirectory()) {
            fileList = root.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                answer += checkFiles(fileList[i], toUpdate, min, max, numOfFiles, suffixes);
            }
            globalCount++;
        } else {
            for (int i = 0; i < suffixes.length; i++) {
                if (root.getName().endsWith(suffixes[i])) answer++;
            }
            globalCount++;
            toUpdate.setValue(min + ((globalCount * (max - min) / numOfFiles)));
            mainPanel.validate();
        }
        return answer;
    }

    /** Updates the overall progress bar and overallSizeLabel of the progressPanel. */
    private void updateTasksProgress(final int tasks, final int allTasks) {
        progressPanel.overallSizeLabel.setText("tasks completed: " + String.valueOf(tasks) + " of " + String.valueOf(allTasks));
        if (allTasks > 0) {
            progressPanel.overallProgressBar.setValue((100 * tasks) / allTasks);
        } else {
            progressPanel.overallProgressBar.setValue(100);
        }
        progressPanel.validate();
    }

    /** Adds the given string to the internal report. */
    public void report(String addToReport) {
        reportPanel.reportTextPane.setText(reportPanel.reportTextPane.getText() + addToReport);
    }

    /** Switches to install/update mode if install is true, to uninstall mode
     *  if install is false. */
    public void switchInstallMode(boolean install) {
        if (install) {
            uninstallMode = false;
            installComponentsCheckBox.setEnabled(true);
            validateJavaCheckBox.setEnabled(true);
            uninstallCheckBox.setEnabled(false);
            copyFilesCheckBox.setEnabled(archive != null);
        } else {
            uninstallMode = true;
            uninstallCheckBox.setEnabled(true);
            installComponentsCheckBox.setEnabled(false);
            validateJavaCheckBox.setEnabled(false);
            copyFilesCheckBox.setEnabled(false);
        }
        validate();
    }

    /** Look up the dream directory and return its name
     * if found, null otherwise. */
    private String getDreamLocation(String dreamPrefsDir) {
        String answer = null;
        File prefsDir = new File(dreamPrefsDir);
        if (prefsDir.exists()) {
            Properties instProps = propHandler.fetch("installer.properties", null, dreamPrefsDir, null, null, null);
            if (instProps != null) answer = instProps.getProperty(DREAM_INSTALLDIR_PROP);
        }
        return answer;
    }

    /** Looks for DREAM-related existing components and adapts the
     *  analysisPanel to the stuff found. */
    private void doAnalysis() {
        String oldDreamVersion = "???";
        report("Beginning search for existing components...\n");
        if (os.indexOf("Windows") < 0) {
            analysisPanel.registryLabel.setEnabled(false);
            analysisPanel.registryResultLabel.setText("not on Windows.");
            analysisPanel.registryResultLabel.setEnabled(false);
        }
        analysisPanel.introTextPane.setText("Looking for existing DREAM components...\n");
        File prefsDir = new File(dreamPrefsDir);
        boolean prefsExist = prefsDir.exists();
        if (prefsExist) {
            analysisPanel.prefsResultLabel.setText("found.");
            installerProps = propHandler.fetch("installer.properties", null, dreamPrefsDir, null, null, null);
            if (installerProps != null) oldDreamVersion = installerProps.getProperty(DREAM_VERSION_PROP, "???");
        } else {
            analysisPanel.prefsResultLabel.setText("not found.");
        }
        boolean registryUsed = false;
        boolean newKeys = false;
        boolean oldKeys = false;
        if (os.indexOf("Windows") >= 0) {
            oldKeys = (RegEdit.getValue(key2000user, oldDreamKey) != null) || (RegEdit.getValue(key98, oldDreamKey) != null);
            if (oldKeys) analysisPanel.registryResultLabel.setText("old keys.");
            newKeys = (RegEdit.getValue(key2000user, dreamKey) != null) || (RegEdit.getValue(key98, dreamKey) != null);
            if (newKeys) analysisPanel.registryResultLabel.setText("new keys.");
            registryUsed = newKeys || oldKeys;
            if (!registryUsed) analysisPanel.registryResultLabel.setText("none found.");
        }
        analysisPanel.releaseTextLabel.setText(oldDreamVersion);
        if (prefsExist || registryUsed) {
            analysisPanel.introTextPane.setText(analysisPanel.introTextPane.getText() + "Some components of an installed release have been found. " + "You may choose to update your DREAM installation or " + "uninstall the existing components.");
            analysisPanel.outroTextPane.setText("Please make your selection " + " and press the 'next' button to begin update/uninstall.");
            report("Existing DREAM installation was found, " + "uninstall enabled.\n");
        } else {
            analysisPanel.introTextPane.setText(analysisPanel.introTextPane.getText() + "No remains of an installed release have been found, " + "uninstall is disabled.");
            analysisPanel.uninstallRadioButton.setEnabled(false);
            analysisPanel.updateRadioButton.setText("begin new installation");
            analysisPanel.outroTextPane.setText("Please press the 'next'" + " button to begin installation.");
            report("No installed DREAM components found.\n");
        }
        analyzeCheckBox.setSelected(true);
    }

    /** Performs uninstallation. */
    private void performUninstall() {
        int tasks = 5;
        int completed = 0;
        boolean removeInstFiles = false;
        report("Beginning uninstall...\n");
        progressPanel.singleLabel.setText("current task progress");
        Properties instProps = null;
        File prefsDir = new File(dreamPrefsDir);
        if (!prefsDir.exists()) tasks--; else {
            instProps = propHandler.fetch("installer.properties", null, dreamPrefsDir, null, null, null);
            if (instProps != null) {
                if (instProps.getProperty("ArchiveInstall") != null) removeInstFiles = true;
            }
        }
        if (!removeInstFiles) tasks--;
        if (os.indexOf("Windows") < 0) {
            tasks--;
            updateTasksProgress(completed, tasks);
        } else {
            report("Windows registry keys deleted.\n");
            updateTasksProgress(completed, tasks);
            progressPanel.subTaskTextPane.setText("deleting Windows " + "registry keys...");
            removeOldKeys(progressPanel.singleFileProgressBar, 100);
            completed++;
            updateTasksProgress(completed, tasks);
            bopWait();
        }
        if (os.indexOf("Windows") < 0) {
            tasks--;
            updateTasksProgress(completed, tasks);
        } else {
            updateTasksProgress(completed, tasks);
            installQuickLaunch(false);
            completed++;
            updateTasksProgress(completed, tasks);
            bopWait();
        }
        if (os.indexOf("Windows") < 0) {
            tasks--;
            updateTasksProgress(completed, tasks);
        } else {
            updateTasksProgress(completed, tasks);
            installDesktop(false);
            completed++;
            updateTasksProgress(completed, tasks);
            bopWait();
        }
        if (removeInstFiles) {
            report("Checking Dream installation directory " + dreamPath + " ...\n");
            progressPanel.subTaskTextPane.setText("Counting files in " + "Dream installation directory " + dreamPath + "...");
            File dreamPathRoot = new File(dreamPath);
            int files = countFiles(dreamPathRoot);
            progressPanel.subTaskTextPane.setText("Checking Dream " + "installation directory... " + dreamPath + " (" + files + " files)");
            int sourceFiles = checkFiles(dreamPathRoot, progressPanel.singleFileProgressBar, 0, 100, files, new String[] { ".java", ".jav", ".form", ".for" });
            if (sourceFiles > 0) {
                report(sourceFiles + " source files (.java / .form) found!\n");
                if (JOptionPane.showConfirmDialog(progressPanel, "The Dream " + "installation directory " + dreamPath + " contains source " + "files. Are you sure you want to delete this directory?", "Please confirm directory deletion", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                    removeInstFiles = false;
                }
            }
            if (removeInstFiles) {
                report("Deleting Dream installation directory " + dreamPath + " ...");
                progressPanel.subTaskTextPane.setText("Deleting Dream " + "installation directory... " + dreamPath + " (" + files + " files)");
                globalCount = 0;
                int removed = removeFiles(new File(dreamPath), progressPanel.singleFileProgressBar, 0, 100, files);
                if (files == removed) {
                    report("ok.\n");
                    completed++;
                } else {
                    report("error: " + (files - removed) + " files left.\n");
                    progressPanel.reportTextPane.setText(progressPanel.reportTextPane.getText() + "Deletion of " + "directory " + dreamPath + " was not entirely successful " + "(" + (files - removed) + " files left). Please check if " + "any of the contained files is open or not writable.\n");
                }
                updateTasksProgress(completed, tasks);
            }
        }
        bopWait();
        if (prefsDir.exists()) {
            propHandler.clear();
            bopWait();
            report("Deleting Dream preferences directory " + dreamPrefsDir + " ...");
            int files = countFiles(prefsDir);
            progressPanel.subTaskTextPane.setText("deleting Dream " + "preferences directory... " + dreamPrefsDir + " (" + files + " files)");
            globalCount = 0;
            int removed = removeFiles(prefsDir, progressPanel.singleFileProgressBar, 0, 100, files);
            if (files == removed) {
                report("ok.\n");
                completed++;
            } else {
                report("error: " + (files - removed) + " files left.\n");
                progressPanel.reportTextPane.setText(progressPanel.reportTextPane.getText() + "Deletion of " + "directory " + dreamPrefsDir + " was not entirely successful " + "(" + (files - removed) + " files left). Please check if " + "any of the contained files is open or not writable.\n");
            }
            updateTasksProgress(completed, tasks);
        }
        if (completed == tasks) progressPanel.reportTextPane.setText("Uninstallation finished successfully. Please proceed to the next " + "step.\n");
        report("Uninstall phase finished.\n\n");
        if (completed > 0) uninstallCheckBox.setSelected(true);
    }

    /** Performs windows specific installation. */
    private void doWindowsInstall(boolean automaticServent, boolean quickLaunch, boolean desktopCheckBox, boolean startMenuCheckBox) {
        int tasks = 0;
        int completed = 0;
        boolean installSuccess = false;
        boolean automaticInstall = true;
        report("Beginning Windows specific installation of components...\n");
        if (automaticServent) tasks++;
        if (quickLaunch) tasks++;
        if (desktopCheckBox) tasks++;
        if (startMenuCheckBox) tasks++;
        progressPanel.singleLabel.setText("current task progress:");
        if (tasks == 0) {
            automaticServent = true;
            automaticInstall = false;
            tasks++;
            progressPanel.doWhatTextPane.setText("Removing old components only. You may however consider " + "to install some of the Windows system specific tasks. If so, please choose the back button.");
        }
        updateTasksProgress(completed, tasks);
        if (automaticServent) {
            if (installAutomaticNode(automaticInstall)) {
                completed++;
                globalNodeStartupHelp = true;
            }
            updateTasksProgress(completed, tasks);
        }
        if (quickLaunch) {
            if (installQuickLaunch(true)) {
                completed++;
            }
            updateTasksProgress(completed, tasks);
        }
        if (desktopCheckBox) {
            if (installDesktop(true)) {
                completed++;
            }
            updateTasksProgress(completed, tasks);
        }
        if (tasks == completed) {
            progressPanel.reportTextPane.setText("Installation finished. You may proceed to the next step now.");
            installSuccess = true;
        } else {
            progressPanel.reportTextPane.setText("There have been problems with" + " some components. You may continue or try to resolve the " + " problems first and then start the installation again.");
        }
        compOk = installSuccess;
        if (compOk) installComponentsCheckBox.setSelected(true);
        report("Windows specific installation phase finished.\n\n");
    }

    /** Returns the command line for the NakedNode. This is the same string
     *  that is written into the Windows registry for automatic startup. */
    private String getNodeCommandLine() {
        String answer = "";
        if (os.indexOf("Windows") < 0) {
            answer = dreamPath + File.separator + "linux" + File.separator + "bin" + File.separator + "drmstarter drm.server.NakedNode";
        } else {
            answer = "\"" + dreamPath + File.separator + "windows" + File.separator + "bin" + File.separator + "drmstart.exe\" --detached --nice drm.server.NakedNode";
        }
        return answer;
    }

    /** Saves the command line for the NakedNode into "dreamDir/startNode" */
    private void saveNodeCommandLine() {
        try {
            FileWriter fw = new FileWriter(new File(dreamPrefsDir, "startNode"));
            fw.write(getNodeCommandLine() + "\n");
            fw.close();
        } catch (Exception e) {
        }
    }

    /** Remove all known registry keys. If toUpdate holds a JProgressBar,
     * it is updated by subsequently adding to the value up to the
     * given maximum value. */
    private void removeOldKeys(JProgressBar toUpdate, int max) {
        int oldValue = 0;
        if (toUpdate != null) oldValue = toUpdate.getValue();
        if (oldValue >= max) oldValue = max - 1;
        RegEdit.deleteValue(key98, dreamKey);
        if (toUpdate != null) toUpdate.setValue(oldValue + (max - oldValue) / 4);
        shortBopWait();
        RegEdit.deleteValue(key2000user, dreamKey);
        if (toUpdate != null) toUpdate.setValue(oldValue + (max - oldValue) / 2);
        shortBopWait();
        RegEdit.deleteValue(key98, oldDreamKey);
        if (toUpdate != null) toUpdate.setValue(oldValue + 3 * (max - oldValue) / 4);
        shortBopWait();
        RegEdit.deleteValue(key2000user, oldDreamKey);
        if (toUpdate != null) toUpdate.setValue(max);
        bopWait();
    }

    /** Installs a link to the console and node startup executables in the
     *  windows quick launch bar. If install is true, we are installing,
     *  otherwise we are uninstalling. */
    private boolean installQuickLaunch(boolean install) {
        boolean answer = true;
        String mklinkPath = "\"" + dreamPath + "\\windows\\bin\\mklnk.exe\"";
        String consolePath = "\"" + dreamPath + "\\windows\\bin\\drmconsole.exe\"";
        String nodePath = "\"" + dreamPath + "\\windows\\bin\\drmstart.exe\"";
        String nodeOptions = "\"--nice drm.server.TestGUINode\"";
        String quickLaunchPath = "\\Microsoft\\Internet Explorer\\Quick Launch";
        String userAppPath = winUserProps.getProperty("AppData", userHome + "\\Application Data");
        if (install) {
            progressPanel.doWhatTextPane.setText("Performing installation of Quick Launch Bar icons:");
        } else {
            progressPanel.subTaskTextPane.setText("Deleting Quick Launch Bar icons.");
        }
        File quickLaunch = new File(userAppPath + quickLaunchPath);
        progressPanel.singleFileProgressBar.setValue(0);
        if (install) {
            if (quickLaunch.exists()) {
                progressPanel.subTaskTextPane.setText("Using Windows Quick Launch path :" + quickLaunch);
            } else {
                progressPanel.subTaskTextPane.setText("Path to Windows Quick Launch bar not found: " + quickLaunch.getAbsolutePath());
                report("Quick Launch icon install failed, path not found.\n");
                return false;
            }
        } else {
            if (!quickLaunch.exists()) return false;
        }
        if (install) {
            int result = 1;
            String commandLine = mklinkPath + " " + consolePath + " " + "\"" + quickLaunch.getAbsolutePath() + File.separator + "drmconsole.lnk\"" + " --detached";
            try {
                Process lnkProcess = run.exec(commandLine);
                lnkProcess.waitFor();
                result = lnkProcess.exitValue();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (result != 0) {
                answer = false;
                report("Dream console icon could not be established on the" + " Quick Launch bar.\n");
            } else {
                report("Dream console icon established/updated on the" + " Quick Launch bar.\n");
            }
            progressPanel.singleFileProgressBar.setValue(50);
            commandLine = mklinkPath + " " + nodePath + " " + "\"" + quickLaunch.getAbsolutePath() + File.separator + "drmnode.lnk\" " + nodeOptions;
            try {
                Process lnkProcess = run.exec(commandLine);
                lnkProcess.waitFor();
                result = lnkProcess.exitValue();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (result != 0) {
                answer = false;
                report("Dream node icon could not be established in Quick" + " Launch bar.\n");
            } else {
                report("Dream node icon established/updated on the Quick" + " Launch bar.\n");
            }
        } else {
            File remConsole = new File(quickLaunch.getAbsolutePath() + File.separator + "drmconsole.lnk");
            if (remConsole.exists()) remConsole.delete();
            progressPanel.singleFileProgressBar.setValue(50);
            mainPanel.validate();
            File remNode = new File(quickLaunch.getAbsolutePath() + File.separator + "drmnode.lnk");
            if (remNode.exists()) remNode.delete();
            report("Quick Launch bar icons removed.\n");
        }
        progressPanel.singleFileProgressBar.setValue(100);
        mainPanel.validate();
        return answer;
    }

    /** Installs a link to the console and node startup executables on the
     *  windows Desktop. If install is true, we are installing,
     *  otherwise we are uninstalling. */
    private boolean installDesktop(boolean install) {
        boolean answer = true;
        String mklinkPath = "\"" + dreamPath + "\\windows\\bin\\mklnk.exe\"";
        String consolePath = "\"" + dreamPath + "\\windows\\bin\\drmconsole.exe\"";
        String nodePath = "\"" + dreamPath + "\\windows\\bin\\drmstart.exe\"";
        String nodeOptions = "\"--nice drm.server.TestGUINode\"";
        String desktopPath = winUserProps.getProperty("Desktop", userHome + "\\Desktop");
        if (install) {
            progressPanel.doWhatTextPane.setText("Performing installation of Desktop icons:");
        } else {
            progressPanel.subTaskTextPane.setText("Deleting Desktop icons.");
        }
        File desktop = new File(desktopPath);
        progressPanel.singleFileProgressBar.setValue(0);
        if (install) {
            if (desktop.exists()) {
                progressPanel.subTaskTextPane.setText("Using Windows Desktop path :" + desktop);
            } else {
                progressPanel.subTaskTextPane.setText("Path to Windows Desktop not found: " + desktop.getAbsolutePath());
                report("Desktop icons install failed, path not found.\n");
                return false;
            }
        } else {
            if (!desktop.exists()) return false;
        }
        if (install) {
            int result = 1;
            String commandLine = mklinkPath + " " + consolePath + " " + "\"" + desktop.getAbsolutePath() + File.separator + "drmconsole.lnk\"" + " --detached";
            try {
                Process lnkProcess = run.exec(commandLine);
                lnkProcess.waitFor();
                result = lnkProcess.exitValue();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (result != 0) {
                answer = false;
                report("Dream console icon could not be established on the" + " Desktop.\n");
            } else {
                report("Dream console icon established/updated on the" + " Desktop.\n");
            }
            progressPanel.singleFileProgressBar.setValue(50);
            commandLine = mklinkPath + " " + nodePath + " " + "\"" + desktop.getAbsolutePath() + File.separator + "drmnode.lnk\" " + nodeOptions;
            try {
                Process lnkProcess = run.exec(commandLine);
                lnkProcess.waitFor();
                result = lnkProcess.exitValue();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (result != 0) {
                answer = false;
                report("Dream node icon could not be established on" + " the Desktop.\n");
            } else {
                report("Dream node icon established/updated on the" + " Desktop.\n");
            }
        } else {
            File remConsole = new File(desktop.getAbsolutePath() + File.separator + "drmconsole.lnk");
            if (remConsole.exists()) remConsole.delete();
            progressPanel.singleFileProgressBar.setValue(50);
            mainPanel.validate();
            File remNode = new File(desktop.getAbsolutePath() + File.separator + "drmnode.lnk");
            if (remNode.exists()) remNode.delete();
            report("Desktop icons removed.\n");
        }
        progressPanel.singleFileProgressBar.setValue(100);
        mainPanel.validate();
        return answer;
    }

    /** Installs the automatic node startup. If install is false, we
     * are removing only. */
    private boolean installAutomaticNode(boolean install) {
        boolean enterRegistry = false;
        boolean answer = false;
        String value = getNodeCommandLine();
        progressPanel.doWhatTextPane.setText("Performing installation of automatic Node startup:");
        progressPanel.subTaskTextPane.setText("deleting old Dream registry entries... ");
        removeOldKeys(progressPanel.singleFileProgressBar, 25);
        if (!install) {
            progressPanel.singleFileProgressBar.setValue(100);
            enterRegistry = true;
        }
        mainPanel.validate();
        report("Old Windows registry keys deleted.\n");
        bopWait();
        if (install) {
            progressPanel.subTaskTextPane.setText("adding entries to the registry... ");
            RegEdit.writeValue(key98, dreamKey, value);
            progressPanel.singleFileProgressBar.setValue(50);
            mainPanel.validate();
            bopWait();
            progressPanel.subTaskTextPane.setText(progressPanel.subTaskTextPane.getText() + "validating... ");
            enterRegistry = (RegEdit.getValue(key98, dreamKey) != null);
            if (enterRegistry) {
                progressPanel.subTaskTextPane.setText(progressPanel.subTaskTextPane.getText() + "successful");
                progressPanel.singleFileProgressBar.setValue(100);
                report("New Windows registry keys written.\n");
                enterRegistry = true;
            } else {
                progressPanel.subTaskTextPane.setText(progressPanel.subTaskTextPane.getText() + "unsuccessful, could not write to the registry");
                progressPanel.singleFileProgressBar.setValue(75);
                mainPanel.validate();
                bopWait();
                progressPanel.subTaskTextPane.setText("trying different key... ");
                RegEdit.writeValue(key2000user, dreamKey, value);
                progressPanel.subTaskTextPane.setText(progressPanel.subTaskTextPane.getText() + "validating... ");
                enterRegistry = (RegEdit.getValue(key2000user, dreamKey) != null);
                if (enterRegistry) {
                    progressPanel.subTaskTextPane.setText(progressPanel.subTaskTextPane.getText() + "successful");
                    report("New Windows registry keys written.\n");
                } else {
                    progressPanel.subTaskTextPane.setText(progressPanel.subTaskTextPane.getText() + "failed, could not enter key to the Windows registry.");
                    report("Failed to write new Windows registry keys.\n");
                }
                progressPanel.singleFileProgressBar.setValue(100);
                mainPanel.validate();
                bopWait();
            }
        }
        answer = enterRegistry;
        return answer;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        uiLabel = new javax.swing.JLabel();
        leftPanel = new javax.swing.JLayeredPane();
        checkBoxPanel = new javax.swing.JPanel();
        indexTitleLabel = new javax.swing.JLabel();
        underlinePanel = new javax.swing.JPanel();
        gluePanel = new javax.swing.JPanel();
        analyzeCheckBox = new javax.swing.JCheckBox();
        copyFilesCheckBox = new javax.swing.JCheckBox();
        validateJavaCheckBox = new javax.swing.JCheckBox();
        installComponentsCheckBox = new javax.swing.JCheckBox();
        uninstallCheckBox = new javax.swing.JCheckBox();
        showInfoCheckBox = new javax.swing.JCheckBox();
        dreamBackgroundLabel = new javax.swing.JLabel();
        mainPanel = new javax.swing.JPanel();
        textPanel = new javax.swing.JTextPane();
        closeButtonPanel = new javax.swing.JPanel();
        pushButtonsRightPanel = new javax.swing.JPanel();
        backButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        finishButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        leftGluePanel = new javax.swing.JPanel();
        rightGluePanel = new javax.swing.JPanel();
        installPanel = new javax.swing.JPanel();
        installStepLabel = new javax.swing.JLabel();
        linePanel = new javax.swing.JPanel();
        separatorPanel = new javax.swing.JPanel();
        buttonPanelSeparator = new javax.swing.JSeparator();
        getContentPane().setLayout(new java.awt.GridBagLayout());
        setFont(new java.awt.Font("Dialog", 0, 12));
        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        uiLabel.setForeground(java.awt.Color.black);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        getContentPane().add(uiLabel, gridBagConstraints);
        checkBoxPanel.setLayout(new javax.swing.BoxLayout(checkBoxPanel, javax.swing.BoxLayout.Y_AXIS));
        checkBoxPanel.setForeground(java.awt.Color.white);
        checkBoxPanel.setMaximumSize(new java.awt.Dimension(200, 200));
        checkBoxPanel.setMinimumSize(new java.awt.Dimension(200, 176));
        checkBoxPanel.setPreferredSize(new java.awt.Dimension(200, 200));
        checkBoxPanel.setOpaque(false);
        copyUISettings(leftPanel, checkBoxPanel);
        indexTitleLabel.setText("Setup tasks:");
        indexTitleLabel.setMaximumSize(new java.awt.Dimension(100, 20));
        indexTitleLabel.setMinimumSize(new java.awt.Dimension(100, 20));
        indexTitleLabel.setPreferredSize(new java.awt.Dimension(100, 20));
        copyUISettings(leftPanel, indexTitleLabel);
        checkBoxPanel.add(indexTitleLabel);
        underlinePanel.setBackground(java.awt.Color.white);
        underlinePanel.setMaximumSize(new java.awt.Dimension(32767, 2));
        underlinePanel.setMinimumSize(new java.awt.Dimension(10, 2));
        underlinePanel.setPreferredSize(new java.awt.Dimension(10, 2));
        checkBoxPanel.add(underlinePanel);
        gluePanel.setBackground(java.awt.Color.white);
        gluePanel.setMaximumSize(new java.awt.Dimension(32767, 4));
        gluePanel.setMinimumSize(new java.awt.Dimension(10, 4));
        gluePanel.setPreferredSize(new java.awt.Dimension(10, 4));
        gluePanel.setOpaque(false);
        checkBoxPanel.add(gluePanel);
        analyzeCheckBox.setText("Analyze system");
        analyzeCheckBox.setToolTipText("initial phase: check for existing components");
        analyzeCheckBox.setRequestFocusEnabled(false);
        analyzeCheckBox.setOpaque(false);
        copyUISettings(leftPanel, analyzeCheckBox);
        analyzeCheckBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                analyzeCheckBoxActionPerformed(evt);
            }
        });
        checkBoxPanel.add(analyzeCheckBox);
        copyFilesCheckBox.setText("Copy / Unpack files");
        copyFilesCheckBox.setToolTipText("initial phase: check for existing components");
        copyFilesCheckBox.setRequestFocusEnabled(false);
        copyFilesCheckBox.setOpaque(false);
        copyUISettings(leftPanel, copyFilesCheckBox);
        copyFilesCheckBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyFilesCheckBoxActionPerformed(evt);
            }
        });
        checkBoxPanel.add(copyFilesCheckBox);
        validateJavaCheckBox.setText("Validate Java environment");
        validateJavaCheckBox.setMaximumSize(new java.awt.Dimension(185, 25));
        validateJavaCheckBox.setMinimumSize(new java.awt.Dimension(185, 25));
        validateJavaCheckBox.setPreferredSize(new java.awt.Dimension(185, 25));
        validateJavaCheckBox.setRequestFocusEnabled(false);
        validateJavaCheckBox.setOpaque(false);
        copyUISettings(leftPanel, validateJavaCheckBox);
        validateJavaCheckBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validateJavaCheckBoxActionPerformed(evt);
            }
        });
        checkBoxPanel.add(validateJavaCheckBox);
        installComponentsCheckBox.setText("Install os specific components");
        installComponentsCheckBox.setRequestFocusEnabled(false);
        installComponentsCheckBox.setOpaque(false);
        copyUISettings(leftPanel, installComponentsCheckBox);
        installComponentsCheckBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installComponentsCheckBoxActionPerformed(evt);
            }
        });
        checkBoxPanel.add(installComponentsCheckBox);
        uninstallCheckBox.setText("Uninstall DREAM software");
        uninstallCheckBox.setToolTipText("deletes preferences and (if available) Windows registry keys");
        uninstallCheckBox.setRequestFocusEnabled(false);
        uninstallCheckBox.setEnabled(false);
        uninstallCheckBox.setOpaque(false);
        copyUISettings(leftPanel, uninstallCheckBox);
        uninstallCheckBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uninstallCheckBoxActionPerformed(evt);
            }
        });
        checkBoxPanel.add(uninstallCheckBox);
        showInfoCheckBox.setText("Show report");
        showInfoCheckBox.setContentAreaFilled(false);
        showInfoCheckBox.setRequestFocusEnabled(false);
        copyUISettings(leftPanel, showInfoCheckBox);
        showInfoCheckBox.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showInfoCheckBoxActionPerformed(evt);
            }
        });
        checkBoxPanel.add(showInfoCheckBox);
        checkBoxPanel.setBounds(10, 245, 200, 220);
        leftPanel.add(checkBoxPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        dreamBackgroundLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/dream/resources/icons/dreamwizardback.gif")));
        dreamBackgroundLabel.setOpaque(true);
        dreamBackgroundLabel.setBounds(90, 0, 110, 350);
        leftPanel.add(dreamBackgroundLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 10.0;
        getContentPane().add(leftPanel, gridBagConstraints);
        mainPanel.setLayout(new java.awt.GridLayout(1, 1));
        textPanel.setEditable(false);
        mainPanel.add(textPanel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 4.0;
        getContentPane().add(mainPanel, gridBagConstraints);
        closeButtonPanel.setLayout(new java.awt.GridBagLayout());
        closeButtonPanel.setMinimumSize(new java.awt.Dimension(270, 30));
        closeButtonPanel.setPreferredSize(new java.awt.Dimension(270, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 20.0;
        closeButtonPanel.add(pushButtonsRightPanel, gridBagConstraints);
        backButton.setText("back");
        backButton.setToolTipText("go to previous installation step");
        backButton.setAlignmentY(0.0F);
        backButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        backButton.setMargin(new java.awt.Insets(0, 14, 0, 14));
        backButton.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        backButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        closeButtonPanel.add(backButton, gridBagConstraints);
        nextButton.setText("next");
        nextButton.setToolTipText("go to next installation step");
        nextButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nextButton.setMargin(new java.awt.Insets(0, 14, 0, 14));
        nextButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        closeButtonPanel.add(nextButton, gridBagConstraints);
        finishButton.setText("finish");
        finishButton.setToolTipText("finish installation with given parameters");
        finishButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        finishButton.setMargin(new java.awt.Insets(0, 14, 0, 14));
        finishButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finishButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        closeButtonPanel.add(finishButton, gridBagConstraints);
        cancelButton.setText("cancel");
        cancelButton.setToolTipText("cancel installation process");
        cancelButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cancelButton.setMargin(new java.awt.Insets(0, 14, 0, 14));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        closeButtonPanel.add(cancelButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(closeButtonPanel, gridBagConstraints);
        leftGluePanel.setMaximumSize(new java.awt.Dimension(10, 32767));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        getContentPane().add(leftGluePanel, gridBagConstraints);
        rightGluePanel.setMaximumSize(new java.awt.Dimension(6, 32767));
        rightGluePanel.setMinimumSize(new java.awt.Dimension(6, 10));
        rightGluePanel.setPreferredSize(new java.awt.Dimension(6, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        getContentPane().add(rightGluePanel, gridBagConstraints);
        installPanel.setLayout(new java.awt.GridBagLayout());
        installPanel.setMinimumSize(new java.awt.Dimension(90, 40));
        installPanel.setPreferredSize(new java.awt.Dimension(90, 40));
        installStepLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        installStepLabel.setText("setup phase");
        installStepLabel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(2, 0, 4, 0)));
        installStepLabel.setMaximumSize(new java.awt.Dimension(90, 30));
        installStepLabel.setMinimumSize(new java.awt.Dimension(90, 30));
        installStepLabel.setPreferredSize(new java.awt.Dimension(90, 30));
        installStepLabel.setForeground(Color.black);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        installPanel.add(installStepLabel, gridBagConstraints);
        linePanel.setBackground(new java.awt.Color(40, 63, 136));
        linePanel.setMinimumSize(new java.awt.Dimension(10, 4));
        linePanel.setPreferredSize(new java.awt.Dimension(10, 4));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 4, 4);
        installPanel.add(linePanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(installPanel, gridBagConstraints);
        separatorPanel.setLayout(new java.awt.GridBagLayout());
        buttonPanelSeparator.setBorder(new javax.swing.border.EtchedBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        separatorPanel.add(buttonPanelSeparator, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(separatorPanel, gridBagConstraints);
        pack();
    }

    private void copyFilesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
        revertCheckMark(evt);
    }

    private void uninstallCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
        revertCheckMark(evt);
    }

    private void showInfoCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
        revertCheckMark(evt);
    }

    private void installComponentsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
        revertCheckMark(evt);
    }

    private void validateJavaCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
        revertCheckMark(evt);
    }

    private void finishButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (!buttonsDisabled) {
            if ((archive != null) && (!uninstallMode)) {
                new File(archive).delete();
            }
            shutdown(0);
        }
    }

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (!buttonsDisabled) {
            switch(step) {
                case STEP_WELCOME:
                    break;
                case STEP_FILE_SELECT:
                    leave(step);
                    enter(STEP_ANALYSIS);
                    break;
                case STEP_FILE_COPY:
                    leave(step);
                    enter(STEP_FILE_SELECT);
                    break;
                case STEP_ANALYSIS:
                    leave(step);
                    enter(STEP_WELCOME);
                    break;
                case STEP_UNINSTALL:
                    leave(step);
                    enter(STEP_ANALYSIS);
                case STEP_JAVA_DETECT:
                    leave(step);
                    enter(STEP_ANALYSIS);
                    break;
                case STEP_WINDOWS_COMPONENTS:
                    leave(step);
                    enter(STEP_JAVA_DETECT);
                    break;
                case STEP_LINUX_COMPONENTS:
                    leave(step);
                    enter(STEP_JAVA_DETECT);
                    break;
                case STEP_COMPONENTS_PROGRESS:
                    leave(step);
                    if (os.indexOf("Windows") >= 0) {
                        enter(STEP_WINDOWS_COMPONENTS);
                    } else {
                        enter(STEP_LINUX_COMPONENTS);
                    }
                    break;
                case STEP_SHOW_INFO:
                    leave(step);
                    if (uninstallMode) {
                        enter(STEP_ANALYSIS);
                    } else {
                        if (os.indexOf("Windows") >= 0) {
                            enter(STEP_WINDOWS_COMPONENTS);
                        } else {
                            enter(STEP_LINUX_COMPONENTS);
                        }
                    }
                    break;
                case STEP_OUTRO:
                    leave(step);
                    enter(STEP_SHOW_INFO);
                    break;
                default:
            }
        }
    }

    private void analyzeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
        revertCheckMark(evt);
    }

    private void revertCheckMark(java.awt.event.ActionEvent evt) {
        if (evt.getSource() instanceof JCheckBox) {
            JCheckBox box = (JCheckBox) evt.getSource();
            box.setSelected(!box.isSelected());
        }
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (!buttonsDisabled) {
            int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel setup?", "Please confirm cancel", JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.OK_OPTION) {
                shutdown(0);
            }
        }
    }

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (!buttonsDisabled) {
            switch(step) {
                case STEP_WELCOME:
                    leave(step);
                    enter(STEP_ANALYSIS);
                    break;
                case STEP_ANALYSIS:
                    leave(step);
                    if (uninstallMode) {
                        enter(STEP_UNINSTALL);
                    } else {
                        if (archive != null) {
                            if (dreamPath != null) {
                                enter(STEP_FILE_COPY);
                            } else {
                                enter(STEP_FILE_SELECT);
                            }
                        } else {
                            enter(STEP_JAVA_DETECT);
                        }
                    }
                    break;
                case STEP_UNINSTALL:
                    leave(step);
                    enter(STEP_SHOW_INFO);
                    break;
                case STEP_FILE_SELECT:
                    File selectedFile = fileSelectPanel.getFileChooser().getSelectedFile();
                    if (selectedFile == null) selectedFile = new File(File.separator + "usr");
                    String testDirName = selectedFile.getAbsolutePath();
                    if (testDirName.endsWith(DREAM_DEFAULT_DIR)) {
                        testDirName = testDirName.substring(0, testDirName.length() - DREAM_DEFAULT_DIR.length());
                        selectedFile = new File(testDirName);
                    }
                    if (!selectedFile.exists()) {
                        if (JOptionPane.showConfirmDialog(fileSelectPanel, "The directory " + testDirName + " does not exist. Do you want to create it?", "Please confirm directory creation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            selectedFile.mkdirs();
                        }
                    }
                    if (selectedFile.exists()) {
                        dreamPath = selectedFile.getAbsolutePath();
                        try {
                            dreamPath = selectedFile.getCanonicalPath();
                        } catch (IOException ex) {
                            System.err.println(ex);
                        }
                        if (!dreamPath.endsWith(File.separator)) dreamPath += File.separator;
                        dreamPath += DREAM_DEFAULT_DIR;
                        leave(step);
                        enter(STEP_FILE_COPY);
                    } else {
                        dreamPath = DREAM_DEFAULT_DIR;
                    }
                    break;
                case STEP_FILE_COPY:
                    leave(step);
                    enter(STEP_JAVA_DETECT);
                    break;
                case STEP_JAVA_DETECT:
                    leave(step);
                    if (os.indexOf("Windows") >= 0) {
                        enter(STEP_WINDOWS_COMPONENTS);
                    } else {
                        enter(STEP_LINUX_COMPONENTS);
                    }
                    break;
                case STEP_WINDOWS_COMPONENTS:
                    leave(step);
                    enter(STEP_COMPONENTS_PROGRESS);
                    break;
                case STEP_LINUX_COMPONENTS:
                    leave(step);
                    enter(STEP_SHOW_INFO);
                    break;
                case STEP_COMPONENTS_PROGRESS:
                    leave(step);
                    enter(STEP_SHOW_INFO);
                    break;
                case STEP_SHOW_INFO:
                    leave(step);
                    enter(STEP_OUTRO);
                    break;
                case STEP_OUTRO:
                    leave(step);
                    shutdown(0);
                    break;
                default:
            }
        }
    }

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {
        shutdown(0);
    }

    private void shutdown(int returnValue) {
        propHandler.saveAll();
        if (callee == null) {
            System.exit(returnValue);
        } else {
            this.dispose();
        }
    }

    public void windowOpened(java.awt.event.WindowEvent windowEvent) {
    }

    public void windowClosed(java.awt.event.WindowEvent windowEvent) {
    }

    public void windowDeiconified(java.awt.event.WindowEvent windowEvent) {
    }

    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
    }

    public void windowDeactivated(java.awt.event.WindowEvent windowEvent) {
    }

    public void windowActivated(java.awt.event.WindowEvent windowEvent) {
    }

    public void windowIconified(java.awt.event.WindowEvent windowEvent) {
    }

    private javax.swing.JLayeredPane leftPanel;

    private javax.swing.JButton backButton;

    private javax.swing.JPanel linePanel;

    private javax.swing.JPanel pushButtonsRightPanel;

    private javax.swing.JLabel indexTitleLabel;

    private javax.swing.JLabel installStepLabel;

    private javax.swing.JPanel installPanel;

    private javax.swing.JButton finishButton;

    private javax.swing.JCheckBox uninstallCheckBox;

    private javax.swing.JLabel dreamBackgroundLabel;

    private javax.swing.JPanel checkBoxPanel;

    private javax.swing.JPanel separatorPanel;

    private javax.swing.JCheckBox validateJavaCheckBox;

    private javax.swing.JPanel closeButtonPanel;

    private javax.swing.JPanel underlinePanel;

    private javax.swing.JTextPane textPanel;

    private javax.swing.JCheckBox installComponentsCheckBox;

    private javax.swing.JSeparator buttonPanelSeparator;

    private javax.swing.JPanel rightGluePanel;

    private javax.swing.JCheckBox analyzeCheckBox;

    private javax.swing.JPanel gluePanel;

    private javax.swing.JCheckBox showInfoCheckBox;

    private javax.swing.JButton cancelButton;

    private javax.swing.JPanel mainPanel;

    private javax.swing.JPanel leftGluePanel;

    private javax.swing.JButton nextButton;

    private javax.swing.JCheckBox copyFilesCheckBox;

    public static javax.swing.JLabel uiLabel;

    /**
     * stand-alone starter
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        String archive = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("archive=")) {
                archive = args[i].substring("archive=".length());
            }
        }
        new Setup(archive).show();
    }
}
