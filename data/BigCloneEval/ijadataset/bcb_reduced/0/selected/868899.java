package com.izforge.izpack.util.os;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.ResourceManager;
import com.izforge.izpack.installer.ResourceNotFoundException;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.FileExecutor;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.StringTool;
import com.izforge.izpack.util.os.unix.ShellScript;
import com.izforge.izpack.util.os.unix.UnixHelper;
import com.izforge.izpack.util.os.unix.UnixUser;
import com.izforge.izpack.util.os.unix.UnixUsers;
import java.io.*;
import java.util.*;

/**
 * This is the Implementation of the RFC-Based Desktop-Link. Used in KDE and GNOME.
 * 
 * @author marc.eppelmann&#064;reddot.de
 */
public class Unix_Shortcut extends Shortcut implements Unix_ShortcutConstants {

    /**
     * version = "$Id: Unix_Shortcut.java 2452 2008-12-16 10:08:10Z eppelman $"
     */
    private static String version = "$Id: Unix_Shortcut.java 2452 2008-12-16 10:08:10Z eppelman $";

    /**
     * rev = "$Revision: 2452 $"
     */
    private static String rev = "$Revision: 2452 $";

    /**
     * DESKTOP_EXT = ".desktop"
     */
    private static String DESKTOP_EXT = ".desktop";

    /**
     * template = ""
     */
    private static String template = "";

    /**
     * N = "\n"
     */
    private static final String N = "\n";

    /**
     * H = "#"
     */
    private static final String H = "#";

    /**
     * S = " "
     */
    private static final String S = " ";

    /**
     * C = Comment = H+S = "# "
     */
    private static final String C = H + S;

    /**
     * QM = "\"" : <b>Q</b>uotation<b>M</b>ark
     */
    private static final String QM = "\"";

    private int ShortcutType;

    private static ShellScript rootScript = null;

    private static ShellScript uninstallScript = null;

    private static ArrayList users = UnixUsers.getUsersWithValidShellsExistingHomesAndDesktops();

    /**
     * internal String createdDirectory
     */
    private String createdDirectory;

    /**
     * internal int itsUserType
     */
    private int itsUserType;

    /**
     * internal String itsGroupName
     */
    private String itsGroupName;

    /**
     * internal String itsName
     */
    private String itsName;

    /**
     * internal String itsFileName
     */
    private String itsFileName;

    /**
     * internal String itsApplnkFolder = "applnk"
     */
    private String itsApplnkFolder = "applnk";

    /**
     * internal Properties Set
     */
    private Properties props;

    /**
     * forAll = new Boolean(false): A flag to indicate that this should created for all users.
     */
    private Boolean forAll = Boolean.FALSE;

    /**
     * Internal Help Buffer
     */
    public StringBuffer hlp;

    /** my Install ShellScript **/
    public ShellScript myInstallScript;

    /** Internal Constant: FS = File.separator // **/
    public final String FS = File.separator;

    /** Internal Constant: myHome = System.getProperty("user.home") **/
    public final String myHome = System.getProperty("user.home");

    /** Internal Constant: su = UnixHelper.getSuCommand() **/
    public final String su = UnixHelper.getSuCommand();

    /** Internal Constant: xdgDesktopIconCmd = UnixHelper.getCustomCommand("xdg-desktop-icon") **/
    public final String xdgDesktopIconCmd = UnixHelper.getCustomCommand("xdg-desktop-icon");

    public String myXdgDesktopIconScript;

    public String myXdgDesktopIconCmd;

    /**
     * Creates a new Unix_Shortcut object.
     */
    public Unix_Shortcut() {
        hlp = new StringBuffer();
        String userLanguage = System.getProperty("user.language", "en");
        hlp.append("[Desktop Entry]" + N);
        hlp.append("Categories=" + $Categories + N);
        hlp.append("Comment=" + $Comment + N);
        hlp.append("Comment[").append(userLanguage).append("]=" + $Comment + N);
        hlp.append("Encoding=" + $Encoding + N);
        hlp.append("Exec=" + $E_QUOT + $Exec + $E_QUOT + S + $Arguments + N);
        hlp.append("GenericName=" + $GenericName + N);
        hlp.append("GenericName[").append(userLanguage).append("]=" + $GenericName + N);
        hlp.append("Icon=" + $Icon + N);
        hlp.append("MimeType=" + $MimeType + N);
        hlp.append("Name=" + $Name + N);
        hlp.append("Name[").append(userLanguage).append("]=" + $Name + N);
        hlp.append("Path=" + $P_QUOT + $Path + $P_QUOT + N);
        hlp.append("ServiceTypes=" + $ServiceTypes + N);
        hlp.append("SwallowExec=" + $SwallowExec + N);
        hlp.append("SwallowTitle=" + $SwallowTitle + N);
        hlp.append("Terminal=" + $Terminal + N);
        hlp.append("TerminalOptions=" + $Options_For_Terminal + N);
        hlp.append("Type=" + $Type + N);
        hlp.append("URL=" + $URL + N);
        hlp.append("X-KDE-SubstituteUID=" + $X_KDE_SubstituteUID + N);
        hlp.append("X-KDE-Username=" + $X_KDE_Username + N);
        hlp.append(N);
        hlp.append(C + "created by" + S).append(getClass().getName()).append(S).append(rev).append(N);
        hlp.append(C).append(version);
        template = hlp.toString();
        props = new Properties();
        initProps();
        if (rootScript == null) {
            rootScript = new ShellScript();
        }
        if (uninstallScript == null) {
            uninstallScript = new ShellScript();
        }
        if (myInstallScript == null) {
            myInstallScript = new ShellScript();
        }
    }

    /**
     * This initialisizes all Properties Values with &quot;&quot;.
     */
    private void initProps() {
        String[] propsArray = { $Comment, $$LANG_Comment, $Encoding, $Exec, $Arguments, $GenericName, $$LANG_GenericName, $MimeType, $Name, $$LANG_Name, $Path, $ServiceTypes, $SwallowExec, $SwallowTitle, $Terminal, $Options_For_Terminal, $Type, $X_KDE_SubstituteUID, $X_KDE_Username, $Icon, $URL, $E_QUOT, $P_QUOT, $Categories, $TryExec };
        for (String aPropsArray : propsArray) {
            props.put(aPropsArray, "");
        }
    }

    /**
     * Overridden Method
     * 
     * @see com.izforge.izpack.util.os.Shortcut#initialize(int, java.lang.String)
     */
    public void initialize(int aType, String aName) throws Exception {
        this.itsName = aName;
        props.put($Name, aName);
    }

    /**
     * This indicates that Unix will be supported.
     * 
     * @see com.izforge.izpack.util.os.Shortcut#supported()
     */
    public boolean supported() {
        return true;
    }

    /**
     * Dummy
     * 
     * @see com.izforge.izpack.util.os.Shortcut#getDirectoryCreated()
     */
    public String getDirectoryCreated() {
        return this.createdDirectory;
    }

    /**
     * Dummy
     * 
     * @see com.izforge.izpack.util.os.Shortcut#getFileName()
     */
    public String getFileName() {
        return (this.itsFileName);
    }

    /**
     * Overridden compatibility method. Returns all directories in $USER/.kde/share/applink.
     * 
     * @see com.izforge.izpack.util.os.Shortcut#getProgramGroups(int)
     */
    public Vector<String> getProgramGroups(int userType) {
        Vector<String> groups = new Vector<String>();
        File kdeShareApplnk = getKdeShareApplnkFolder(userType);
        try {
            File[] listing = kdeShareApplnk.listFiles();
            for (File aListing : listing) {
                if (aListing.isDirectory()) {
                    groups.add(aListing.getName());
                }
            }
        } catch (Exception e) {
        }
        return (groups);
    }

    /**
     * Gets the Programsfolder for the given User (non-Javadoc).
     * 
     * @see com.izforge.izpack.util.os.Shortcut#getProgramsFolder(int)
     */
    public String getProgramsFolder(int current_user) {
        String result = "";
        result = getKdeShareApplnkFolder(current_user).toString();
        return result;
    }

    /**
     * Gets the XDG path to place the menu shortcuts
     * 
     * @param userType to get for.
     * @return handle to the directory
     */
    private File getKdeShareApplnkFolder(int userType) {
        if (userType == Shortcut.ALL_USERS) {
            return new File(File.separator + "usr" + File.separator + "share" + File.separator + "applications");
        } else {
            return new File(System.getProperty("user.home") + File.separator + ".local" + File.separator + "share" + File.separator + "applications");
        }
    }

    /**
     * Gets the name of the applink folder for the currently used distribution. Currently
     * "applnk-redhat for RedHat, "applnk-mdk" for Mandrake, and simply "applnk" for all others.
     * 
     * @return result
     */
    private String getKdeApplinkFolderName() {
        String applinkFolderName = "applnk";
        if (OsVersion.IS_REDHAT_LINUX) {
            applinkFolderName = "applnk-redhat";
        }
        if (OsVersion.IS_MANDRAKE_LINUX) {
            applinkFolderName = "applnk-mdk";
        }
        return applinkFolderName;
    }

    /**
     * Gets the KDEBasedir for the given User.
     * 
     * @param userType one of root or regular user
     * @return the basedir
     */
    private File getKdeBase(int userType) {
        File result = null;
        if (userType == Shortcut.ALL_USERS) {
            FileExecutor fe = new FileExecutor();
            String[] execOut = new String[2];
            int execResult = fe.executeCommand(new String[] { "/usr/bin/env", "kde-config", "--prefix" }, execOut);
            result = new File(execOut[0].trim());
        } else {
            result = new File(System.getProperty("user.home") + File.separator + ".kde");
        }
        return result;
    }

    /**
     * overridden method
     * 
     * @return true
     * @see com.izforge.izpack.util.os.Shortcut#multipleUsers()
     */
    public boolean multipleUsers() {
        return (true);
    }

    /**
     * Creates and stores the shortcut-files.
     * 
     * @see com.izforge.izpack.util.os.Shortcut#save()
     */
    public void save() throws Exception {
        String target = null;
        String shortCutDef = this.replace();
        boolean rootUser4All = this.getUserType() == Shortcut.ALL_USERS;
        boolean create4All = this.getCreateForAll();
        if ("".equals(this.itsGroupName) && (this.getLinkType() == Shortcut.DESKTOP)) {
            this.itsFileName = target;
            AutomatedInstallData idata;
            idata = AutomatedInstallData.getInstance();
            File shortCutLocation = null;
            File ApplicationShortcutPath;
            String ApplicationShortcutPathName = idata.getVariable("ApplicationShortcutPath");
            if (null != ApplicationShortcutPathName && !ApplicationShortcutPathName.equals("")) {
                ApplicationShortcutPath = new File(ApplicationShortcutPathName);
                if (ApplicationShortcutPath.isAbsolute()) {
                    if (!ApplicationShortcutPath.exists() && ApplicationShortcutPath.mkdirs() && ApplicationShortcutPath.canWrite()) shortCutLocation = ApplicationShortcutPath;
                    if (ApplicationShortcutPath.exists() && ApplicationShortcutPath.isDirectory() && ApplicationShortcutPath.canWrite()) shortCutLocation = ApplicationShortcutPath;
                } else {
                    File relativePath = new File(idata.getInstallPath() + FS + ApplicationShortcutPath);
                    relativePath.mkdirs();
                    shortCutLocation = new File(relativePath.toString());
                }
            } else shortCutLocation = new File(idata.getInstallPath());
            File writtenDesktopFile = writeAppShortcutWithOutSpace(shortCutLocation.toString(), this.itsName, shortCutDef);
            uninstaller.addFile(writtenDesktopFile.toString(), true);
            if (xdgDesktopIconCmd != null) {
                createExtXdgDesktopIconCmd(shortCutLocation);
                myInstallScript.appendln(new String[] { myXdgDesktopIconCmd, "install", "--novendor", StringTool.escapeSpaces(writtenDesktopFile.toString()) });
                ShellScript myUninstallScript = new ShellScript();
                myUninstallScript.appendln(new String[] { myXdgDesktopIconCmd, "uninstall", "--novendor", StringTool.escapeSpaces(writtenDesktopFile.toString()) });
                uninstaller.addUninstallScript(myUninstallScript.getContentAsString());
            } else {
                File myDesktopFile;
                do {
                    myDesktopFile = new File(myHome + FS + "Desktop" + writtenDesktopFile.getName() + "-" + System.currentTimeMillis() + DESKTOP_EXT);
                } while (myDesktopFile.exists());
                copyTo(writtenDesktopFile, myDesktopFile);
                uninstaller.addFile(myDesktopFile.toString(), true);
            }
            if (rootUser4All && create4All) {
                if (xdgDesktopIconCmd != null) {
                    installDesktopFileToAllUsersDesktop(writtenDesktopFile);
                } else {
                    copyDesktopFileToAllUsersDesktop(writtenDesktopFile);
                }
            }
        } else {
            if (rootUser4All && create4All) {
                {
                    File theIcon = new File(this.getIconLocation());
                    File commonIcon = new File("/usr/share/pixmaps/" + theIcon.getName());
                    try {
                        copyTo(theIcon, commonIcon);
                        uninstaller.addFile(commonIcon.toString(), true);
                    } catch (Exception cnc) {
                        Debug.log("Could Not Copy: " + theIcon + " to " + commonIcon + "( " + cnc.getMessage() + " )");
                    }
                    this.itsFileName = target;
                    File writtenFile = writeAppShortcut("/usr/share/applications/", this.itsName, shortCutDef);
                    setWrittenFileName(writtenFile.getName());
                    uninstaller.addFile(writtenFile.toString(), true);
                }
            } else {
                String localApps = myHome + "/.local/share/applications/";
                String localPixmaps = myHome + "/.local/share/pixmaps/";
                try {
                    java.io.File f = new java.io.File(localApps);
                    f.mkdirs();
                    f = new java.io.File(localPixmaps);
                    f.mkdirs();
                } catch (Exception ignore) {
                    Debug.log("Failed creating " + localApps + " or " + localPixmaps);
                }
                File theIcon = new File(this.getIconLocation());
                File commonIcon = new File(localPixmaps + theIcon.getName());
                try {
                    copyTo(theIcon, commonIcon);
                    uninstaller.addFile(commonIcon.toString(), true);
                } catch (Exception cnc) {
                    Debug.log("Could Not Copy: " + theIcon + " to " + commonIcon + "( " + cnc.getMessage() + " )");
                }
                this.itsFileName = target;
                File writtenFile = writeAppShortcut(localApps, this.itsName, shortCutDef);
                setWrittenFileName(writtenFile.getName());
                uninstaller.addFile(writtenFile.toString(), true);
            }
        }
    }

    /**
     * Ceates Extended Locale Enabled XdgDesktopIcon Command script.
     * Fills the File myXdgDesktopIconScript with the content of  
     * com/izforge/izpack/util/os/unix/xdgscript.sh and uses this to
     * creates User Desktop icons
     * 
     * @param shortCutLocation in which folder should this stored.
     * @throws IOException
     * @throws ResourceNotFoundException
     */
    public void createExtXdgDesktopIconCmd(File shortCutLocation) throws IOException, ResourceNotFoundException {
        ShellScript myXdgDesktopIconScript = new ShellScript(null);
        String lines = "";
        ResourceManager m = ResourceManager.getInstance();
        m.setDefaultOrResourceBasePath("");
        lines = m.getTextResource("/com/izforge/izpack/util/os/unix/xdgdesktopiconscript.sh");
        m.setDefaultOrResourceBasePath(null);
        myXdgDesktopIconScript.append(lines);
        myXdgDesktopIconCmd = new String(shortCutLocation + FS + "IzPackLocaleEnabledXdgDesktopIconScript.sh");
        myXdgDesktopIconScript.write(myXdgDesktopIconCmd);
        FileExecutor.getExecOutput(new String[] { UnixHelper.getCustomCommand("chmod"), "+x", myXdgDesktopIconCmd }, true);
    }

    /**
     * Calls and creates the Install/Unistall Script which installs Desktop Icons using
     * xdgDesktopIconCmd un-/install
     * 
     * @param writtenDesktopFile An applications desktop file, which should be installed.
     */
    private void installDesktopFileToAllUsersDesktop(File writtenDesktopFile) {
        for (Object user1 : users) {
            UnixUser user = ((UnixUser) user1);
            if (user.getHome().equals(myHome)) {
                Debug.log("need not to copy for itself: " + user.getHome() + "==" + myHome);
                continue;
            }
            try {
                rootScript.append(new String[] { su, user.getName(), "-c" });
                rootScript.appendln(new String[] { "\"" + myXdgDesktopIconCmd, "install", "--novendor", StringTool.escapeSpaces(writtenDesktopFile.toString()) + "\"" });
                uninstallScript.append(new String[] { su, user.getName(), "-c" });
                uninstallScript.appendln(new String[] { "\"" + myXdgDesktopIconCmd, "uninstall", "--novendor", StringTool.escapeSpaces(writtenDesktopFile.toString()) + "\"" });
            } catch (Exception e) {
                Debug.log(e.getMessage());
                Debug.log(e.toString());
            }
        }
        Debug.log("==============================");
        Debug.log(rootScript.getContentAsString());
    }

    /**
     * @param writtenDesktopFile
     * @throws IOException
     */
    private void copyDesktopFileToAllUsersDesktop(File writtenDesktopFile) throws IOException {
        String chmod = UnixHelper.getCustomCommand("chmod");
        String chown = UnixHelper.getCustomCommand("chown");
        String rm = UnixHelper.getRmCommand();
        String copy = UnixHelper.getCpCommand();
        File dest = null;
        File tempFile = File.createTempFile(this.getClass().getName(), Long.toString(System.currentTimeMillis()) + ".tmp");
        copyTo(writtenDesktopFile, tempFile);
        FileExecutor.getExecOutput(new String[] { chmod, "uga+rwx", tempFile.toString() });
        for (Object user1 : users) {
            UnixUser user = ((UnixUser) user1);
            if (user.getHome().equals(myHome)) {
                Debug.log("need not to copy for itself: " + user.getHome() + "==" + myHome);
                continue;
            }
            try {
                dest = new File(user.getHome() + FS + "Desktop" + FS + writtenDesktopFile.getName());
                rootScript.append(su);
                rootScript.append(S);
                rootScript.append(user.getName());
                rootScript.append(S);
                rootScript.append("-c");
                rootScript.append(S);
                rootScript.append('"');
                rootScript.append(copy);
                rootScript.append(S);
                rootScript.append(tempFile.toString());
                rootScript.append(S);
                rootScript.append(StringTool.replace(dest.toString(), " ", "\\ "));
                rootScript.appendln('"');
                rootScript.append('\n');
                rootScript.append(chown);
                rootScript.append(S);
                rootScript.append(user.getName());
                rootScript.append(S);
                rootScript.appendln(StringTool.replace(dest.toString(), " ", "\\ "));
                rootScript.append('\n');
                rootScript.append('\n');
                uninstallScript.append(su);
                uninstallScript.append(S);
                uninstallScript.append(user.getName());
                uninstallScript.append(S);
                uninstallScript.append("-c");
                uninstallScript.append(S);
                uninstallScript.append('"');
                uninstallScript.append(rm);
                uninstallScript.append(S);
                uninstallScript.append(StringTool.replace(dest.toString(), " ", "\\ "));
                uninstallScript.appendln('"');
                uninstallScript.appendln();
            } catch (Exception rex) {
                System.out.println("Error while su Copy: " + rex.getLocalizedMessage() + "\n\n");
                rex.printStackTrace();
            }
        }
        rootScript.append(rm);
        rootScript.append(S);
        rootScript.appendln(tempFile.toString());
        rootScript.appendln();
    }

    /**
     * Post Exec Action especially for the Unix Root User. which executes the Root ShortCut
     * Shellscript. to copy all ShellScripts to the users Desktop.
     */
    public void execPostAction() {
        Debug.log("Call of Impl. execPostAction Method in " + this.getClass().getName());
        String pseudoUnique = this.getClass().getName() + Long.toString(System.currentTimeMillis());
        String scriptFilename = null;
        try {
            scriptFilename = File.createTempFile(pseudoUnique, ".sh").toString();
        } catch (IOException e) {
            scriptFilename = System.getProperty("java.io.tmpdir", "/tmp") + "/" + pseudoUnique + ".sh";
            e.printStackTrace();
        }
        rootScript.write(scriptFilename);
        rootScript.exec();
        rootScript.delete();
        Debug.log(rootScript);
        pseudoUnique = this.getClass().getName() + Long.toString(System.currentTimeMillis());
        try {
            scriptFilename = File.createTempFile(pseudoUnique, ".sh").toString();
        } catch (IOException e) {
            scriptFilename = System.getProperty("java.io.tmpdir", "/tmp") + "/" + pseudoUnique + ".sh";
            e.printStackTrace();
        }
        myInstallScript.write(scriptFilename);
        myInstallScript.exec();
        myInstallScript.delete();
        Debug.log(myInstallScript);
        Debug.log(uninstallScript);
        uninstaller.addUninstallScript(uninstallScript.getContentAsString());
    }

    /**
     * Copies the inFile file to outFile using cbuff as buffer.
     * 
     * @param inFile The File to read from.
     * @param outFile The targetFile to write to.
     * @throws IOException If an IO Error occurs
     */
    public static void copyTo(File inFile, File outFile) throws IOException {
        char[] cbuff = new char[32768];
        BufferedReader reader = new BufferedReader(new FileReader(inFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
        int readedBytes = 0;
        long absWrittenBytes = 0;
        while ((readedBytes = reader.read(cbuff, 0, cbuff.length)) != -1) {
            writer.write(cbuff, 0, readedBytes);
            absWrittenBytes += readedBytes;
        }
        reader.close();
        writer.close();
    }

    private String writtenFileName;

    public String getWrittenFileName() {
        return writtenFileName;
    }

    protected void setWrittenFileName(String s) {
        writtenFileName = s;
    }

    /**
     * Write the given ShortDefinition in a File $ShortcutName-$timestamp.desktop in the given
     * TargetPath.
     * 
     * @param targetPath The Path in which the files should be written.
     * @param shortcutName The Name for the File
     * @param shortcutDef The Shortcut FileContent
     * 
     * @return The written File
     */
    private File writeAppShortcut(String targetPath, String shortcutName, String shortcutDef) {
        return writeAppShortcutWithSimpleSpacehandling(targetPath, shortcutName, shortcutDef, false);
    }

    /**
     * Write the given ShortDefinition in a File $ShortcutName-$timestamp.desktop in the given
     * TargetPath. ALSO all WhiteSpaces in the ShortCutName will be repalced with "-"
     * 
     * @param targetPath The Path in which the files should be written.
     * @param shortcutName The Name for the File
     * @param shortcutDef The Shortcut FileContent
     * 
     * @return The written File
     */
    private File writeAppShortcutWithOutSpace(String targetPath, String shortcutName, String shortcutDef) {
        return writeAppShortcutWithSimpleSpacehandling(targetPath, shortcutName, shortcutDef, true);
    }

    /**
     * Write the given ShortDefinition in a File $ShortcutName-$timestamp.desktop in the given
     * TargetPath. If the given replaceSpaces was true ALSO all WhiteSpaces in the ShortCutName will
     * be replaced with "-"
     * 
     * @param targetPath The Path in which the files should be written.
     * @param shortcutName The Name for the File
     * @param shortcutDef The Shortcut FileContent
     * @param replaceSpaces Replaces Spaces in the Filename if true was given
     * @return The written File
     */
    private File writeAppShortcutWithSimpleSpacehandling(String targetPath, String shortcutName, String shortcutDef, boolean replaceSpacesWithMinus) {
        if (!(targetPath.endsWith("/") || targetPath.endsWith("\\"))) {
            targetPath += File.separatorChar;
        }
        File shortcutFile;
        do {
            shortcutFile = new File(targetPath + (replaceSpacesWithMinus == true ? StringTool.replaceSpacesWithMinus(shortcutName) : shortcutName) + "-" + System.currentTimeMillis() + DESKTOP_EXT);
        } while (shortcutFile.exists());
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(shortcutFile);
        } catch (IOException e1) {
            System.out.println(e1.getMessage());
        }
        try {
            fileWriter.write(shortcutDef);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fileWriter.close();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return shortcutFile;
    }

    /**
     * Writes the given Shortcutdefinition to the given Target. Returns the written File.
     * 
     * @param target
     * @param shortCutDef
     * @return the File of the written shortcut.
     */
    private File writeShortCut(String target, String shortCutDef) {
        File targetPath = new File(target.substring(0, target.lastIndexOf(File.separatorChar)));
        if (!targetPath.exists()) {
            targetPath.mkdirs();
            this.createdDirectory = targetPath.toString();
        }
        File targetFileName = new File(target);
        File backupFile = new File(targetPath + File.separator + "." + targetFileName.getName() + System.currentTimeMillis());
        if (targetFileName.exists()) {
            try {
                copyTo(targetFileName, backupFile);
                targetFileName.delete();
            } catch (IOException e3) {
                System.out.println("cannot create backup file " + backupFile + " of " + targetFileName);
            }
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(targetFileName);
        } catch (IOException e1) {
            System.out.println(e1.getMessage());
        }
        try {
            fileWriter.write(shortCutDef);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fileWriter.close();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return targetFileName;
    }

    /**
     * Set the Commandline Arguments
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setArguments(java.lang.String)
     */
    public void setArguments(String args) {
        props.put($Arguments, args);
    }

    /**
     * Sets the Description
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setDescription(java.lang.String)
     */
    public void setDescription(String description) {
        props.put($Comment, description);
    }

    /**
     * Sets The Icon Path
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setIconLocation(java.lang.String, int)
     */
    public void setIconLocation(String path, int index) {
        props.put($Icon, path);
    }

    /**
     * Sets the Name of this Shortcut
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setLinkName(java.lang.String)
     */
    public void setLinkName(String aName) {
        this.itsName = aName;
        props.put($Name, aName);
    }

    /**
     * Sets the type of this Shortcut
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setLinkType(int)
     */
    public void setLinkType(int aType) throws IllegalArgumentException, UnsupportedEncodingException {
        ShortcutType = aType;
    }

    /**
     * Sets the ProgramGroup
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setProgramGroup(java.lang.String)
     */
    public void setProgramGroup(String aGroupName) {
        this.itsGroupName = aGroupName;
    }

    /**
     * Sets the ShowMode
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setShowCommand(int)
     */
    public void setShowCommand(int show) {
    }

    /**
     * Sets The TargetPath
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setTargetPath(java.lang.String)
     */
    public void setTargetPath(String aPath) {
        StringTokenizer whiteSpaceTester = new StringTokenizer(aPath);
        if (whiteSpaceTester.countTokens() > 1) {
            props.put($E_QUOT, QM);
        }
        props.put($Exec, aPath);
    }

    /**
     * Sets the usertype.
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setUserType(int)
     */
    public void setUserType(int aUserType) {
        this.itsUserType = aUserType;
    }

    /**
     * Sets the working-directory
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setWorkingDirectory(java.lang.String)
     */
    public void setWorkingDirectory(String aDirectory) {
        StringTokenizer whiteSpaceTester = new StringTokenizer(aDirectory);
        if (whiteSpaceTester.countTokens() > 1) {
            props.put($P_QUOT, QM);
        }
        props.put($Path, aDirectory);
    }

    /**
     * Dumps the Name to console.
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.itsName + N + template;
    }

    /**
     * Creates the Shortcut String which will be stored as File.
     * 
     * @return contents of the shortcut file
     */
    public String replace() {
        String result = template;
        Enumeration enumeration = props.keys();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            result = StringTool.replace(result, key, props.getProperty(key));
        }
        return result;
    }

    /**
     * Test Method
     * 
     * @param args
     * @throws IOException
     * @throws ResourceNotFoundException
     */
    public static void main(String[] args) throws IOException, ResourceNotFoundException {
        Unix_Shortcut aSample = new Unix_Shortcut();
        System.out.println(">>" + aSample.getClass().getName() + "- Test Main Program\n\n");
        try {
            aSample.initialize(APPLICATIONS, "Start Tomcat");
        } catch (Exception exc) {
            System.err.println("Could not init Unix_Shourtcut");
        }
        aSample.replace();
        System.out.println(aSample);
        aSample.createExtXdgDesktopIconCmd(new File(System.getProperty("user.home")));
        System.out.println("DONE.\n");
    }

    /**
     * Sets The Encoding
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setEncoding(java.lang.String)
     */
    public void setEncoding(String aEncoding) {
        props.put($Encoding, aEncoding);
    }

    /**
     * Sets The KDE Specific subst UID property
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setKdeSubstUID(java.lang.String)
     */
    public void setKdeSubstUID(String trueFalseOrNothing) {
        props.put($X_KDE_SubstituteUID, trueFalseOrNothing);
    }

    /**
     * Sets The KDE Specific subst UID property
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setKdeSubstUID(java.lang.String)
     */
    public void setKdeUserName(String aUserName) {
        props.put($X_KDE_Username, aUserName);
    }

    /**
     * Sets the MimeType
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setMimetype(java.lang.String)
     */
    public void setMimetype(String aMimetype) {
        props.put($MimeType, aMimetype);
    }

    /**
     * Sets the terminal
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setTerminal(java.lang.String)
     */
    public void setTerminal(String trueFalseOrNothing) {
        props.put($Terminal, trueFalseOrNothing);
    }

    /**
     * Sets the terminal options
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setTerminalOptions(java.lang.String)
     */
    public void setTerminalOptions(String someTerminalOptions) {
        props.put($Options_For_Terminal, someTerminalOptions);
    }

    /**
     * Sets the Shortcut type (one of Application, Link or Device)
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setType(java.lang.String)
     */
    public void setType(String aType) {
        props.put($Type, aType);
    }

    /**
     * Sets the Url for type Link. Can be also a apsolute file/path
     * 
     * @see com.izforge.izpack.util.os.Shortcut#setURL(java.lang.String)
     */
    public void setURL(String anUrl) {
        props.put($URL, anUrl);
    }

    /**
     * Gets the Usertype of the Shortcut.
     * 
     * @see com.izforge.izpack.util.os.Shortcut#getUserType()
     */
    public int getUserType() {
        return itsUserType;
    }

    /**
     * Sets the Categories Field
     * 
     * @param theCategories the categories
     */
    public void setCategories(String theCategories) {
        props.put($Categories, theCategories);
    }

    /**
     * Sets the TryExecField.
     * 
     * @param aTryExec the try exec command
     */
    public void setTryExec(String aTryExec) {
        props.put($TryExec, aTryExec);
    }

    public int getLinkType() {
        return ShortcutType;
    }
}
