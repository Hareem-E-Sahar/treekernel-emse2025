package modmanager.controller;

import modmanager.business.modactions.ActionEditFileFindAll;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.StreamException;
import com.mallardsoft.tuple.Pair;
import com.mallardsoft.tuple.Tuple;
import org.apache.log4j.Logger;
import modmanager.business.ManagerOptions;
import modmanager.business.Mod;
import modmanager.business.ModsOutOfDateReminder;
import modmanager.exceptions.*;
import modmanager.gui.l10n.L10n;
import modmanager.utility.OS;
import modmanager.utility.XML;
import modmanager.utility.ZIP;
import modmanager.utility.FileUtils;
import modmanager.utility.Game;
import modmanager.utility.SplashScreenMain;
import modmanager.utility.update.UpdateReturn;
import modmanager.utility.update.UpdateThread;
import java.nio.channels.FileLockInterruptionException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipException;
import java.security.InvalidParameterException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import modmanager.business.modactions.Action;
import modmanager.business.modactions.ActionApplyAfter;
import modmanager.business.modactions.ActionApplyBefore;
import modmanager.business.modactions.ActionCopyFile;
import modmanager.business.modactions.ActionEditFile;
import modmanager.business.modactions.ActionEditFileActions;
import modmanager.business.modactions.ActionEditFileDelete;
import modmanager.business.modactions.ActionEditFileFind;
import modmanager.business.modactions.ActionEditFileFindUp;
import modmanager.business.modactions.ActionEditFileInsert;
import modmanager.business.modactions.ActionEditFileReplace;
import modmanager.business.modactions.ActionIncompatibility;
import modmanager.business.modactions.ActionRequirement;

/**
 * Implementation of the core functionality of HoN modification manager. This class is
 * the 'model' part of the MVC framework used for creating manager GUI. After any updates
 * that should result in UI changes (such as new mod added) it should call updateNotify
 * method which will notify observers to refresh. This class should never directly call
 * view or controller classes.
 *
 * @author Shirkit
 */
public class Manager extends Observable {

    private static Manager instance = null;

    private HashMap<Mod, HashMap<String, String>> deps;

    private HashSet<HashMap<String, String>> cons;

    private HashMap<Mod, HashMap<String, String>> after;

    private HashMap<Mod, HashMap<String, String>> before;

    private ArrayList<String> resources0FolderTree;

    private static Logger logger = Logger.getLogger(Manager.class.getPackage().getName());

    /**
     * It's private since only one isntance of the controller is allowed to exist.
     */
    private Manager() {
        deps = new HashMap<Mod, HashMap<String, String>>();
        cons = new HashSet<HashMap<String, String>>();
        after = new HashMap<Mod, HashMap<String, String>>();
        before = new HashMap<Mod, HashMap<String, String>>();
        resources0FolderTree = new ArrayList<String>();
        resources0FolderTree.add("buildings");
        resources0FolderTree.add("core" + File.separator + "cursors");
        resources0FolderTree.add("core" + File.separator + "fonts");
        resources0FolderTree.add("core" + File.separator + "materials");
        resources0FolderTree.add("core" + File.separator + "null");
        resources0FolderTree.add("core" + File.separator + "post");
        resources0FolderTree.add("heroes");
        resources0FolderTree.add("items");
        resources0FolderTree.add("music");
        resources0FolderTree.add("npcs");
        resources0FolderTree.add("scripts");
        resources0FolderTree.add("shared");
        resources0FolderTree.add("stringtables");
        resources0FolderTree.add("tools");
        resources0FolderTree.add("tools");
        resources0FolderTree.add("triggers");
        resources0FolderTree.add("ui");
        resources0FolderTree.add("world");
    }

    /**
     * This method is used to get the running instance of the Manager class.
     * @return the instance.
     * @see get()
     */
    public static Manager getInstance() {
        if (instance == null) {
            instance = new Manager();
        }
        return instance;
    }

    public ArrayList<String> getResources0FolderTree() {
        return resources0FolderTree;
    }

    /**
     * This should be called after adding all the honmod files to build and initialize the arrays
     */
    public void buildGraphs() {
        ArrayList<Mod> mods = ManagerOptions.getInstance().getMods();
        for (int i = 0; i < mods.size(); i++) {
            if (mods.get(i).getActions() != null) {
                for (int j = 0; j < mods.get(i).getActions().size(); j++) {
                    if (mods.get(i).getActions().get(j).getClass() == ActionApplyAfter.class) {
                        if (!after.containsKey(mods.get(i))) {
                            after.put(mods.get(i), new HashMap<String, String>());
                        }
                        after.get(mods.get(i)).put(((ActionApplyAfter) mods.get(i).getActions().get(j)).getName(), ((ActionApplyAfter) mods.get(i).getActions().get(j)).getVersion());
                    } else if (mods.get(i).getActions().get(j).getClass() == ActionApplyBefore.class) {
                        if (!before.containsKey(mods.get(i))) {
                            before.put(mods.get(i), new HashMap<String, String>());
                        }
                        before.get(mods.get(i)).put(((ActionApplyBefore) mods.get(i).getActions().get(j)).getName(), ((ActionApplyBefore) mods.get(i).getActions().get(j)).getVersion());
                    } else if (mods.get(i).getActions().get(j).getClass() == ActionIncompatibility.class) {
                        HashMap<String, String> mapping = new HashMap<String, String>();
                        mapping.put(mods.get(i).getName(), mods.get(i).getVersion());
                        mapping.put(((ActionIncompatibility) mods.get(i).getActions().get(j)).getName(), ((ActionIncompatibility) mods.get(i).getActions().get(j)).getVersion());
                        cons.add(mapping);
                    } else if (mods.get(i).getActions().get(j).getClass() == ActionRequirement.class) {
                        if (!deps.containsKey(mods.get(i))) {
                            deps.put(mods.get(i), new HashMap<String, String>());
                        }
                        deps.get(mods.get(i)).put(((ActionRequirement) mods.get(i).getActions().get(j)).getName(), ((ActionRequirement) mods.get(i).getActions().get(j)).getVersion());
                    }
                }
            }
        }
    }

    private void doSaveOptions() throws IOException {
        String name = FileUtils.getManagerPerpetualFolder() + File.separator + ManagerOptions.OPTIONS_FILENAME;
        File f = new File(name);
        f.setReadable(true);
        f.setWritable(true);
        ManagerOptions.getInstance().saveOptions(f);
    }

    /**
     * This method saves the ManagerOptions attributes in a file. The file is located in the same folder of the Manager.
     * The filename can be get in the ManagerOptions.
     * @throws IOException if a random I/O exception happened.
     * 
     */
    public void saveOptions() throws IOException {
        doSaveOptions();
        logger.info("Options saved. Path=" + FileUtils.getManagerPerpetualFolder() + File.separator + ManagerOptions.OPTIONS_FILENAME);
    }

    /**
     * This method saves the ManagerOptions attributes in a file but without adding a logging info. The file is located in the same folder of the Manager.
     * This method's existence is just for not spam the saving Column size change thing. If the user begins to change the colum's Widht, it was spamming infinite Saving log stuff.
     * The filename can be get in the ManagerOptions.
     * @throws IOException if a random I/O exception happened.
     *
     */
    public void saveOptionsNoLog() throws IOException {
        doSaveOptions();
    }

    /**
     * Not using it currently
     * check update the path of the Hon or Mod folder according to the string passed in
     * and prompt the user for input if the designate functions have failed.
     * @deprecated no sense on this method.
     */
    public String check(String name) {
        String path = "";
        if (name.equalsIgnoreCase("HoN folder")) {
            path = Game.findHonFolder();
        } else if (name.equalsIgnoreCase("Mod folder")) {
            path = Game.findModFolder(Game.findHonFolder());
        }
        if (path == null || path.isEmpty()) {
            JFileChooser fc = new JFileChooser();
            fc.setAcceptAllFileFilterUsed(false);
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (OS.isMac()) {
                fc.setCurrentDirectory(new File("/Applications"));
            }
            fc.setMultiSelectionEnabled(false);
            fc.showOpenDialog(null);
            if (fc.getSelectedFile() != null) {
                path = fc.getSelectedFile().getAbsolutePath();
            } else {
                path = null;
            }
        }
        return path;
    }

    /**
     * This method runs the ManagerOptions.loadOptions method to load the options located in a file.
     */
    public void loadOptions() {
        try {
            ManagerOptions.getInstance().loadOptions();
            ManagerOptions.getInstance().setNoOptionsFile(false);
            logger.info("Options loaded.");
        } catch (FileNotFoundException e) {
            logger.error("Failed loading options file.", e);
            ManagerOptions.getInstance().setGamePath(Game.findHonFolder());
            logger.info("HoN folder set to=" + ManagerOptions.getInstance().getGamePath());
            ManagerOptions.getInstance().setModPath(Game.findModFolder(Game.findHonFolder()));
            logger.info("Mods folder set to=" + ManagerOptions.getInstance().getModPath());
        } catch (StreamException e) {
            logger.error("Failed loading options file.", e);
            ManagerOptions.getInstance().setGamePath(Game.findHonFolder());
            logger.info("HoN folder set to=" + ManagerOptions.getInstance().getGamePath());
            ManagerOptions.getInstance().setModPath(Game.findModFolder(Game.findHonFolder()));
            logger.info("Mods folder set to=" + ManagerOptions.getInstance().getModPath());
        }
        logger.info("MAN: finished loading options.");
    }

    /**
     * Adds a Mod to the list of mods. This adds the mod to the Model list of mods.
     * @param Mod to be added.
     */
    private void addMod(Mod mod) {
        ManagerOptions.getInstance().addMod(mod, false);
    }

    boolean firstLoad = false;

    /**
     * This method returns possible Honmod files inside a target folder.
     * @param targetFolder
     * @return
     */
    public File[] listHonmodFiles(String targetFolder) {
        FileFilter fileFilter = new FileFilter() {

            public boolean accept(File file) {
                String fileName = file.getName();
                if ((!file.isDirectory()) && (!fileName.startsWith(".")) && ((fileName.endsWith(".honmod")) || (fileName.endsWith(".zip")))) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        return new File(targetFolder).listFiles(fileFilter);
    }

    /**
     * Load all mods from the mods folder (set in Model) and put them into the Model array of mods.
     */
    public ArrayList<ArrayList<Pair<String, String>>> loadMods(boolean developerMode) throws IOException {
        ManagerOptions.getInstance().getMods().clear();
        File[] files = listHonmodFiles(ManagerOptions.getInstance().getModPath());
        if (files == null || files.length == 0) {
            return new ArrayList<ArrayList<Pair<String, String>>>();
        }
        ArrayList<Pair<String, String>> stream = new ArrayList<Pair<String, String>>();
        ArrayList<Pair<String, String>> notfound = new ArrayList<Pair<String, String>>();
        ArrayList<Pair<String, String>> zip = new ArrayList<Pair<String, String>>();
        ArrayList<Pair<String, String>> duplicate = new ArrayList<Pair<String, String>>();
        ArrayList<ArrayList<Pair<String, String>>> problems = new ArrayList<ArrayList<Pair<String, String>>>();
        if (SplashScreenMain.getInstance().isSplashScreenActive()) {
            SplashScreenMain.getInstance().setProgressMax(files.length);
        }
        String devMod = ManagerOptions.getInstance().getDevelopingMod();
        if (developerMode && devMod != null && !devMod.isEmpty()) {
            File[] filesTemp = new File[files.length + 1];
            System.arraycopy(files, 0, filesTemp, 1, files.length);
            filesTemp[0] = new File(devMod);
            files = filesTemp;
        }
        for (int i = 0; i < files.length; i++) {
            try {
                addHonmod(files[i], false);
                if (SplashScreenMain.getInstance().isSplashScreenActive()) {
                    SplashScreenMain.getInstance().setProgress("" + i + "/" + files.length, i);
                }
            } catch (ModStreamException e) {
                logger.error("StreamException from loadMods(): file - " + files[i].getName() + " - is corrupted.", e);
                stream.addAll(e.getMods());
            } catch (ModNotFoundException e) {
                logger.error("FileNotFoundException from loadMods(): file - " + files[i].getName() + " - is corrupted.", e);
                notfound.addAll(e.getMods());
            } catch (ModDuplicateException e) {
                logger.error("ModDuplicateException from loadMods().", e);
                duplicate.addAll(e.getMods());
            } catch (ConversionException e) {
                logger.error("Conversion from loadMods(): file - " + files[i].getName() + " - is corrupted.", e);
            } catch (ModZipException e) {
                logger.error("ZipException from loadsMods(): file - " + files[i].getName() + " - is corrupted.", e);
                zip.addAll(e.getMods());
            }
        }
        try {
            Mod moodr = ModsOutOfDateReminder.getMod();
            moodr.setIcon(new javax.swing.ImageIcon(getClass().getResource("/modmanager/gui/resources/icon.png")));
            moodr.setChangelog(null);
            moodr.setPath(null);
            addMod(moodr);
        } catch (Exception e) {
            logger.error("Failed to load Mods Out of Date Reminder", e);
        }
        problems.add(stream);
        problems.add(notfound);
        problems.add(zip);
        problems.add(duplicate);
        return problems;
    }

    /**
     * This function is used internally from the GUI itself automatically when launch to initiate existing mods.
     * @param honmod is the file (.honmod) to be add.
     * @param copy flag to indicate whether to copy the file to mods folder
     * @throws FileNotFoundException if the file wasn't found.
     * @throws IOException if a random I/O exception has happened.
     */
    public void addHonmod(File honmod, boolean copy) throws ModNotFoundException, ModStreamException, IOException, ModZipException, ModDuplicateException {
        ArrayList<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
        if (!honmod.exists()) {
            list.add(Tuple.from(honmod.getName(), "notfound"));
            throw new ModNotFoundException(list);
        }
        String xml = null;
        try {
            if (honmod.isFile()) {
                xml = new String(ZIP.getFile(honmod, Mod.MOD_FILENAME), "UTF-8");
            } else {
                xml = FileUtils.loadFile(new File(honmod, Mod.MOD_FILENAME), "UTF-8");
            }
        } catch (ZipException ex) {
            list.add(Tuple.from(honmod.getName(), "zip"));
            logger.error(ex);
            throw new ModZipException(list);
        } catch (FileNotFoundException ex) {
            list.add(Tuple.from(honmod.getName(), "zip"));
            logger.error(ex);
            throw new ModZipException(list);
        }
        Mod m = null;
        try {
            m = XML.xmlToMod(xml);
        } catch (StreamException ex) {
            list.add(Tuple.from(honmod.getName(), "stream"));
            throw new ModStreamException(list);
        }
        if (honmod.getName().endsWith(".zip")) {
            honmod.setWritable(true);
            honmod.renameTo(new File(honmod.getParentFile(), honmod.getName().replace(".zip", ".honmod")));
        }
        m.setPath(honmod.getAbsolutePath());
        if (getMod(m.getName(), m.getVersion()) != null) {
            list.add(Tuple.from(new File(getMod(m.getName(), m.getVersion()).getPath()).getName(), "duplicate"));
            list.add(Tuple.from(honmod.getName(), "duplicate"));
            throw new ModDuplicateException(list);
        }
        Icon icon;
        try {
            if (honmod.isFile()) {
                icon = new ImageIcon(ZIP.getFile(honmod, Mod.ICON_FILENAME));
            } else {
                icon = new ImageIcon(honmod.getAbsolutePath() + File.separator + Mod.ICON_FILENAME);
            }
        } catch (FileNotFoundException e) {
            icon = new javax.swing.ImageIcon(getClass().getResource("/modmanager/gui/resources/icon.png"));
        }
        String changelog = null;
        try {
            if (honmod.isFile()) {
                changelog = new String(ZIP.getFile(honmod, Mod.CHANGELOG_FILENAME));
            } else {
                changelog = FileUtils.loadFile(new File(honmod, Mod.CHANGELOG_FILENAME), null);
            }
        } catch (IOException e) {
            changelog = null;
        }
        m.setChangelog(changelog);
        m.setIcon(icon);
        logger.info("Mod file opened. Mod name: " + m.getName());
        if (copy && !(new File(ManagerOptions.getInstance().getModPath() + File.separator + honmod.getName()).exists())) {
            logger.info("Mod file copied to mods folder");
            File f = new File(ManagerOptions.getInstance().getModPath());
            f.mkdirs();
            FileUtils.copyFile(honmod, new File(f, honmod.getName()));
            logger.info("Mod file copied to mods older");
            m.setPath(f.getAbsolutePath() + File.separator + honmod.getName());
        }
        addMod(m);
    }

    /**
     * Open specified website in the default browser. This method is using java
     * Desktop API and therefore requires Java 1.6. Also, this operation might not
     * be supported on all platforms.
     *
     * @param url url of the website to open
     * @return true on success, false in case the operation is not supported on this platform
     */
    public boolean openWebsite(String url) {
        if (!java.awt.Desktop.isDesktopSupported()) {
            logger.info("Opening websites is not supported");
            return false;
        }
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            logger.info("Opening websites is not supported");
            return false;
        }
        try {
            java.net.URI uri = new java.net.URI(url);
            desktop.browse(uri);
        } catch (Exception e) {
            logger.error("Unable to open website: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Open folder containing mods. The folder is opened using OS specific explorer
     * and therefore might not be supported on all platforms. This operation uses java
     * Desktop API and requires Java 1.6
     *
     * @return 0 on succuess, -1 in case the operation is not supported on this platform
     */
    public int openModFolder() {
        if (!java.awt.Desktop.isDesktopSupported()) {
            logger.info("Opening local folders is not supported");
            return -1;
        }
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        try {
            desktop.open(new File(ManagerOptions.getInstance().getModPath()));
        } catch (Exception e) {
            logger.error("Unable to open local folder: " + e.getMessage());
            return -1;
        }
        return 0;
    }

    /**
     * Get mod at specified index
     *
     * @param index index of the mod in the list of mods
     * @return mod at the given index
     * @throws IndexOutOfBoundsException in case index does not exist in the list of mods
     * @deprecated This method doesn't make sense and it should be avoided.
     */
    public Mod getMod(int index) throws IndexOutOfBoundsException {
        return (Mod) ManagerOptions.getInstance().getMods().get(index);
    }

    /**
     * This function returns the mod from the arraylist mods given it's name and version.
     * @param name of the mod.
     * @param version Version or a version expression of the mod. Examples: "1.1", "1.1-1.5", "*-1.6" or "*". A null string or no lenght will be assumed as any version.
     * @return the found Mod or null if isn't found.
     */
    public Mod getMod(String name, String version) {
        return ManagerOptions.getInstance().getMod(name, version);
    }

    /**
     * This method updates the given mods. It handles all exceptions that can exist, and take the needed actions to complete the task,
     * without needing any external influence.
     * @param mods to be updated.
     * @return a instance of a UpdateReturn containing the result of the method. Updated, failed and already up-to-date mods can be easily found there.
     * @throws StreamException This exception is thrown in a serius error.
     * @throws ModVersionUnsatisfiedException 
     * @throws ModNotEnabledException 
     */
    public UpdateReturn updateMod(ArrayList<Mod> mods) {
        ExecutorService pool = Executors.newCachedThreadPool();
        Iterator<Mod> it = mods.iterator();
        HashSet<Future<UpdateThread>> temp = new HashSet<Future<UpdateThread>>();
        while (it.hasNext()) {
            Mod tempMod = it.next();
            temp.add(pool.submit(new UpdateThread(tempMod)));
            logger.info("Started update on: " + tempMod.getName() + " - " + tempMod.getVersion());
        }
        HashSet<Future<UpdateThread>> result = new HashSet<Future<UpdateThread>>();
        while (temp.size() != result.size()) {
            Iterator<Future<UpdateThread>> ite = temp.iterator();
            while (ite.hasNext()) {
                Future<UpdateThread> ff = ite.next();
                if (!result.contains(ff) && ff.isDone()) {
                    result.add(ff);
                    int[] ints = new int[2];
                    ints[0] = result.size();
                    ints[1] = temp.size();
                    setChanged();
                    notifyObservers(ints);
                }
            }
        }
        Iterator<Future<UpdateThread>> ite = result.iterator();
        UpdateReturn returnValue = new UpdateReturn();
        while (ite.hasNext()) {
            Future<UpdateThread> ff = ite.next();
            try {
                UpdateThread mod = (UpdateThread) ff.get();
                File file = mod.getFile();
                if (file != null) {
                    new File(mod.getMod().getPath()).setWritable(true);
                    FileUtils.copyFile(file, mod.getMod().getPath());
                    Mod newMod = null;
                    String olderVersion = mod.getMod().getVersion();
                    try {
                        newMod = XML.xmlToMod(new String(ZIP.getFile(file, Mod.MOD_FILENAME)));
                    } catch (StreamException ex) {
                        logger.info("StreamException: Failed to update: " + mod.getMod().getName(), ex);
                        returnValue.addModFailed(mod.getMod(), ex);
                    } catch (ZipException ex) {
                        logger.info("ZipException: Failed to update: " + mod.getMod().getName(), ex);
                        returnValue.addModFailed(mod.getMod(), ex);
                    }
                    if (newMod != null) {
                        newMod.setPath(mod.getMod().getPath());
                        Mod oldMod = getMod(mod.getMod().getName(), olderVersion);
                        boolean wasEnabled = oldMod.isEnabled();
                        HashSet<Mod> gotDisable = new HashSet<Mod>();
                        gotDisable.add(oldMod);
                        while (!gotDisable.isEmpty()) {
                            Iterator<Mod> iter = gotDisable.iterator();
                            while (iter.hasNext()) {
                                try {
                                    Mod next = iter.next();
                                    disableMod(next);
                                    gotDisable.remove(next);
                                } catch (ModEnabledException ex) {
                                    Iterator<Pair<String, String>> itera = ex.getDeps().iterator();
                                    while (itera.hasNext()) {
                                        Pair<String, String> pair = itera.next();
                                        if (!gotDisable.contains(getMod(Tuple.get1(pair), Tuple.get2(pair)))) {
                                            gotDisable.add(getMod(Tuple.get1(pair), Tuple.get2(pair)));
                                        }
                                    }
                                }
                            }
                        }
                        oldMod.copy(newMod);
                        if (wasEnabled) {
                            try {
                                enableMod(newMod, false);
                            } catch (Exception ex) {
                                logger.error("Could not enable mod " + newMod.getName());
                            }
                        }
                        returnValue.addUpdated(mod.getMod(), olderVersion);
                        logger.info(mod.getMod().getName() + " was updated to " + newMod.getVersion() + " from " + olderVersion);
                    }
                } else {
                    logger.info(mod.getMod().getName() + " is up-to-date");
                    returnValue.addUpToDate(mod.getMod());
                }
            } catch (SecurityException ex) {
                logger.info("Couldn't write on the file.");
            } catch (InterruptedException ex) {
            } catch (ExecutionException ex) {
                try {
                    UpdateModException ex2 = (UpdateModException) ex.getCause();
                    logger.info("Failed to update: " + ex2.getMod().getName() + " - " + ex2.getCause().getClass() + " - " + ex2.getCause().getMessage());
                    returnValue.addModFailed(ex2.getMod(), (Exception) ex2.getCause());
                } catch (ClassCastException ex3) {
                    logger.info(ex.getCause());
                }
            } catch (FileNotFoundException ex) {
            } catch (IOException ex) {
                logger.error("Random I/O Exception happened", ex);
            }
        }
        pool.shutdown();
        return returnValue;
    }

    private void checkdiff(Mod mod) throws ModSameNameDifferentVersionsException {
        HashSet<Pair<String, String>> modDiffEx = new HashSet<Pair<String, String>>();
        Enumeration e = Collections.enumeration(ManagerOptions.getInstance().getMods());
        while (e.hasMoreElements()) {
            Mod m = (Mod) e.nextElement();
            if (m.getName().equalsIgnoreCase(mod.getName()) && m.isEnabled() && !m.getVersion().equalsIgnoreCase(mod.getVersion())) {
                modDiffEx.add(Tuple.from(m.getName(), m.getVersion()));
            }
        }
        if (!modDiffEx.isEmpty()) {
            throw new ModSameNameDifferentVersionsException(modDiffEx);
        }
    }

    /**
     * This function checks to see if all dependencies of a given mod are satisfied. If a dependency isn't satisfied, throws exceptions.
     * @param mod to be checked which is guaranteed to be disabled.
     * @throws ModNotEnabledException if the mod given by parameter requires another mod to be enabled.
     */
    private void checkdeps(Mod mod) throws ModNotEnabledException, ModEnabledException, ModVersionUnsatisfiedException {
        if (mod.isEnabled()) {
            Iterator it = deps.entrySet().iterator();
            HashSet<Pair<String, String>> modEnabledEx = new HashSet<Pair<String, String>>();
            Pair<String, String> match = Tuple.from(mod.getName(), mod.getVersion());
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                Mod m = (Mod) entry.getKey();
                if (m.isEnabled() && ((HashMap<String, String>) entry.getValue()).containsKey(mod.getName()) && compareModsVersions(mod.getVersion(), ((HashMap<String, String>) entry.getValue()).get(mod.getName()))) {
                    modEnabledEx.add(Tuple.from(m.getName(), m.getVersion()));
                }
            }
            if (!modEnabledEx.isEmpty()) {
                throw new ModEnabledException(modEnabledEx);
            }
        } else if (deps.containsKey(mod)) {
            Iterator it = deps.get(mod).entrySet().iterator();
            HashSet<Pair<String, String>> modDisabledEx = new HashSet<Pair<String, String>>();
            HashSet<Pair<String, String>> modUnsatisfiedEx = new HashSet<Pair<String, String>>();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                ArrayList<Mod> allModWithName = ManagerOptions.getInstance().getModsWithName((String) entry.getKey());
                if (allModWithName.isEmpty()) {
                    modDisabledEx.add(Tuple.from((String) entry.getKey(), (String) entry.getValue()));
                } else {
                    Enumeration e = Collections.enumeration(allModWithName);
                    while (e.hasMoreElements()) {
                        Mod m = (Mod) e.nextElement();
                        if (!m.isEnabled() && compareModsVersions(m.getVersion(), (String) entry.getValue())) {
                            modDisabledEx.add(Tuple.from(m.getName(), m.getVersion()));
                        }
                        if (m.isEnabled() && !compareModsVersions(m.getVersion(), (String) entry.getValue())) {
                            modUnsatisfiedEx.add(Tuple.from(m.getName(), m.getVersion()));
                        }
                    }
                }
            }
            if (!modDisabledEx.isEmpty()) {
                throw new ModNotEnabledException(modDisabledEx);
            }
            if (!modUnsatisfiedEx.isEmpty()) {
                throw new ModVersionUnsatisfiedException(modUnsatisfiedEx);
            }
        }
    }

    /**
     * This function checks to see if there is any conflict by the given mod with other enabled mods.
     * @param mod to be checked.
     * @throws ModEnabledException if another mod that is already enabled has a conflict with the mod given by parameter.
     */
    private void checkcons(Mod mod) throws ModConflictException {
        Iterator it = cons.iterator();
        HashSet<Pair<String, String>> list = new HashSet<Pair<String, String>>();
        while (it.hasNext()) {
            HashMap<String, String> mapping = (HashMap<String, String>) it.next();
            if (mapping.containsKey(mod.getName())) {
                if (compareModsVersions(mod.getVersion(), mapping.get(mod.getName()))) {
                    Iterator itt = mapping.entrySet().iterator();
                    while (itt.hasNext()) {
                        Map.Entry entry = (Map.Entry) itt.next();
                        if (!entry.getKey().equals(mod.getName())) {
                            Enumeration modsConflict = Collections.enumeration(ManagerOptions.getInstance().getModsWithName((String) entry.getKey()));
                            while (modsConflict.hasMoreElements()) {
                                Mod compare = (Mod) modsConflict.nextElement();
                                if (compareModsVersions(compare.getVersion(), (String) entry.getValue()) && compare.isEnabled()) {
                                    list.add(Tuple.from((String) entry.getKey(), (String) entry.getValue()));
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!list.isEmpty()) {
            throw new ModConflictException(list);
        }
    }

    /**
     * This function trys to enable the mod with the name given. Throws exceptions if didn't no success while enabling the mod.
     * ignoreGameVersion should be always false, unless the user especifically says so.
     * @param name of the mod
     * @throws ModEnabledException if a mod was enabled and caused an incompatibility with the Mod that is being tryied to apply.
     * @throws ModNotEnabledException if a mod that was required by this mod wasn't enabled.
     * @throws NoSuchElementException if the mod doesn't exist
     * @throws ModVersionMissmatchException if the mod's version is imcompatible with the game version.
     * @throws NullPointerException if there is a problem with the game path (maybe the path was not set in the game class,
     * or hon.exe wasn't found, or happened a random I/O error).
     * @throws FileNotFoundException if the Hon.exe file wasn't found
     * @throws IOException if a random I/O Exception happened.
     * @throws IllegalArgumentException if a mod used a invalid parameter to compare the mods version.
     */
    public void enableMod(Mod m, boolean ignoreGameVersion) throws ModConflictException, ModVersionUnsatisfiedException, ModNotEnabledException, NoSuchElementException, ModVersionMissmatchException, NullPointerException, FileNotFoundException, IllegalArgumentException, IOException, ModSameNameDifferentVersionsException {
        if (!m.isEnabled()) {
            if (!ignoreGameVersion) {
                if (m.getAppVersion() != null) {
                    if (!m.getAppVersion().contains("-") && !m.getAppVersion().contains("*")) {
                        if (!compareModsVersions(Game.getInstance().getVersion(), m.getAppVersion() + ".*")) {
                            throw new ModVersionMissmatchException(m.getName(), m.getVersion(), m.getAppVersion());
                        }
                    } else if (m.getAppVersion().contains("*") && !m.getAppVersion().contains("-")) {
                        if (!compareModsVersions(Game.getInstance().getVersion(), "-" + m.getAppVersion())) {
                            throw new ModVersionMissmatchException(m.getName(), m.getVersion(), m.getAppVersion());
                        }
                    } else {
                        if (!compareModsVersions(Game.getInstance().getVersion(), m.getAppVersion())) {
                            throw new ModVersionMissmatchException(m.getName(), m.getVersion(), m.getAppVersion());
                        }
                    }
                }
            }
            checkdiff(m);
            checkcons(m);
            try {
                checkdeps(m);
            } catch (ModEnabledException e) {
            }
            ManagerOptions.getInstance().getMods().get(ManagerOptions.getInstance().getMods().indexOf(m)).enable();
            m.enable();
        }
    }

    /**
     * Tries to disable a mod given by it's name. Throws exception if an error occurred  .
     * @param m name of the mod.
     * @throws ModEnabledException if another mod is enabled and requires the given by parameter mod to continue enabled.
     */
    public void disableMod(Mod m) throws ModEnabledException {
        if (m.isEnabled()) {
            try {
                checkdeps(m);
            } catch (ModNotEnabledException ex) {
            } catch (ModVersionUnsatisfiedException ex) {
            }
            ManagerOptions.getInstance().getMods().get(ManagerOptions.getInstance().getMods().indexOf(m)).disable();
        }
    }

    public ArrayList<Mod> depSort(ArrayList<Mod> list) {
        ArrayList<Mod> ulayer = new ArrayList<Mod>();
        ArrayList<Mod> dlayer = new ArrayList<Mod>();
        Enumeration e = Collections.enumeration(list);
        while (e.hasMoreElements()) {
            Mod m = (Mod) e.nextElement();
            if (deps.containsKey(m)) {
                Iterator it = deps.get(m).entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    Mod mod = ManagerOptions.getInstance().getMod((String) entry.getKey(), (String) entry.getValue());
                    if (mod != null && !ulayer.contains(mod) && mod.isEnabled()) {
                        ulayer.add(mod);
                    }
                }
            }
        }
        e = Collections.enumeration(list);
        while (e.hasMoreElements()) {
            Mod m = (Mod) e.nextElement();
            if (!ulayer.contains(m)) {
                dlayer.add(m);
            }
        }
        if (ulayer.isEmpty()) {
            return dlayer;
        }
        ulayer = depSort(ulayer);
        ulayer.addAll(dlayer);
        return ulayer;
    }

    public ArrayList<Mod> afterSort(ArrayList<Mod> list) {
        Enumeration e = Collections.enumeration(list);
        while (e.hasMoreElements()) {
            Mod m = (Mod) e.nextElement();
            if (after.containsKey(m)) {
                Iterator it = after.get(m).entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    Mod mod = ManagerOptions.getInstance().getMod((String) entry.getKey(), (String) entry.getValue());
                    if (mod != null && mod.isEnabled()) {
                        if (list.indexOf(mod) >= list.indexOf(m)) {
                            int j = list.indexOf(mod);
                            int x = list.indexOf(m);
                            list.set(j, m);
                            list.set(x, mod);
                        }
                    }
                }
            }
        }
        return list;
    }

    public ArrayList<Mod> beforeSort(ArrayList<Mod> list) {
        Enumeration e = Collections.enumeration(list);
        while (e.hasMoreElements()) {
            Mod m = (Mod) e.nextElement();
            if (before.containsKey(m)) {
                Iterator it = before.get(m).entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    Mod mod = ManagerOptions.getInstance().getMod((String) entry.getKey(), (String) entry.getValue());
                    if (mod != null && mod.isEnabled()) {
                        if (list.indexOf(mod) <= list.indexOf(m)) {
                            int j = list.indexOf(mod);
                            int x = list.indexOf(m);
                            list.set(j, m);
                            list.set(x, mod);
                        }
                    }
                }
            }
        }
        return list;
    }

    public ArrayList<Mod> newSort(ArrayList<Mod> mods) {
        boolean changed = false;
        Mod[] sorted = new Mod[mods.size()];
        Iterator<Mod> it = mods.iterator();
        for (int i = 0; i < sorted.length; i++) {
            sorted[i] = mods.get(i);
        }
        it = mods.iterator();
        while (it.hasNext()) {
            Mod mod = it.next();
            if (deps.containsKey(mod)) {
                Iterator<Entry<String, String>> iterator1 = deps.get(mod).entrySet().iterator();
                while (iterator1.hasNext()) {
                    Entry<String, String> entry = iterator1.next();
                    Mod m = ManagerOptions.getInstance().getMod(entry.getKey(), entry.getValue());
                    if (m != null && m.isEnabled()) {
                        int actual = indexOf(sorted, mod);
                        int target = indexOf(sorted, m);
                        if (actual < target) {
                            changed = true;
                            for (int i = actual; i < target; i++) {
                                Mod x = sorted[i + 1];
                                sorted[i + 1] = sorted[i];
                                sorted[i] = x;
                            }
                        }
                    }
                }
            }
            if (after.containsKey(mod)) {
                Iterator<Entry<String, String>> iterator1 = after.get(mod).entrySet().iterator();
                while (iterator1.hasNext()) {
                    Entry<String, String> entry = iterator1.next();
                    Mod m = ManagerOptions.getInstance().getMod(entry.getKey(), entry.getValue());
                    if (m != null && m.isEnabled()) {
                        int actual = indexOf(sorted, mod);
                        int target = indexOf(sorted, m);
                        if (actual < target) {
                            changed = true;
                            for (int i = actual; i < target; i++) {
                                Mod x = sorted[i + 1];
                                sorted[i + 1] = sorted[i];
                                sorted[i] = x;
                            }
                        }
                    }
                }
            }
            if (before.containsKey(mod)) {
                Iterator<Entry<String, String>> iterator1 = before.get(mod).entrySet().iterator();
                while (iterator1.hasNext()) {
                    Entry<String, String> entry = iterator1.next();
                    Mod m = ManagerOptions.getInstance().getMod(entry.getKey(), entry.getValue());
                    if (m != null && m.isEnabled()) {
                        int actual = indexOf(sorted, mod);
                        int target = indexOf(sorted, m);
                        if (actual > target) {
                            changed = true;
                            for (int i = actual; i > target; i--) {
                                Mod x = sorted[i - 1];
                                sorted[i - 1] = sorted[i];
                                sorted[i] = x;
                            }
                        }
                    }
                }
            }
        }
        ArrayList<Mod> returnn = new ArrayList<Mod>();
        returnn.addAll(Arrays.asList(sorted));
        if (changed) {
            returnn = newSort(returnn);
        }
        return returnn;
    }

    private int indexOf(Mod[] list, Mod m) {
        for (int i = 0; i < list.length; i++) {
            if (list[i] == m) {
                return i;
            }
        }
        return -1;
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public ArrayList<Mod> sortMods() throws IOException {
        ArrayList<Mod> left = new ArrayList<Mod>();
        for (int i = 0; i < ManagerOptions.getInstance().getMods().size(); i++) {
            if (ManagerOptions.getInstance().getMods().get(i).isEnabled()) {
                left.add(ManagerOptions.getInstance().getMods().get(i));
            }
        }
        try {
            left = newSort(left);
        } catch (Exception e) {
            logger.error("Failed to use the new sorting method, attemping to use the old one");
            left = beforeSort(afterSort(depSort(left)));
        }
        return left;
    }

    public int getApplyIterationsCount() {
        int total = 3;
        for (Mod m : ManagerOptions.getInstance().getMods()) {
            if (m.isEnabled()) {
                for (Action a : m.getActions()) {
                    total++;
                    if (a.getClass().equals(ActionEditFile.class)) {
                        ActionEditFile ac = (ActionEditFile) a;
                        if (ac.getActions() != null) {
                            for (ActionEditFileActions b : ac.getActions()) {
                                total++;
                            }
                        }
                    }
                }
            }
        }
        return total;
    }

    /**
     * Tries to apply the currently enabled mods. They can be found in the Model class.
     * @param developerMode If true, the Manager will output the current mods to a folder tree in the HoN/game folder puting the files inside. If not, it will generate the resources999.s2z file. This sould be true for 'Developer Mode'.
     * @throws IOException if a random I/O error happened.
     * @throws UnknowModActionException if a unkown Action was found. Actions that aren't know by the program can't be applied.
     * @throws NothingSelectedModActionException if a action tried to do a action that involves a string, but no string was selected.
     * @throws StringNotFoundModActionException if a search for a string was made, but that string wasn't found. Probally,
     * imcompatibility or a error by the mod's author.
     * @throws InvalidModActionParameterException if a action had a invalid parameter. Only the position of actions 'insert' and 'find' can throw this exception.
     * @throws SecurityException if the Manager couldn't do a action because of security business.
     * @throws FileLockInterruptionException if the Manager couldn't open the resources999.s2z file.
     */
    public void applyMods(boolean developerMode, boolean deleteFolderTree) throws IOException, UnknowModActionException, NothingSelectedModActionException, StringNotFoundModActionException, InvalidModActionParameterException, SecurityException, FileLockInterruptionException, ModFileNotFoundException {
        ArrayList<Mod> applyOrder = sortMods();
        File tempFolder = FileUtils.generateTempFolder(true);
        logger.info("Started mod applying. Folder=" + tempFolder.getAbsolutePath() + ". Game version=" + Game.getInstance().getVersion());
        Enumeration<Mod> list = Collections.enumeration(applyOrder);
        int counted[] = new int[] { 0 };
        while (list.hasMoreElements()) {
            Mod mod = list.nextElement();
            setChanged();
            notifyObservers(mod.getName());
            logger.info("Applying Mod=" + mod.getName() + " | Version=" + mod.getVersion());
            Action lastAction = null;
            Action beforeLastAction = null;
            for (int j = 0; j < mod.getActions().size(); j++) {
                counted[0]++;
                setChanged();
                notifyObservers(counted);
                Action action = mod.getActions().get(j);
                String resources0 = ManagerOptions.getInstance().getGamePath() + File.separator + "game" + File.separator + "resources0.s2z";
                if (!new File(resources0).exists()) {
                    throw new FileNotFoundException(resources0);
                }
                if (action.getClass().equals(ActionCopyFile.class)) {
                    ActionCopyFile copyfile = (ActionCopyFile) action;
                    beforeLastAction = lastAction;
                    lastAction = copyfile;
                    if (!isValidCondition(action)) {
                    } else {
                        String toCopy;
                        if (copyfile.getSource() == null || copyfile.getSource().isEmpty() || copyfile.getSource().equals("")) {
                            toCopy = copyfile.getName();
                        } else {
                            toCopy = copyfile.getSource();
                        }
                        File temp = new File(tempFolder.getAbsolutePath() + File.separator + copyfile.getName());
                        if (temp.exists()) {
                            if (copyfile.overwrite() == -1) {
                                throw new InvalidModActionParameterException(mod.getName(), mod.getVersion(), (Action) copyfile);
                            }
                            if (copyfile.overwrite() == 0) {
                            } else if (copyfile.overwrite() == 1) {
                                if (ZIP.getLastModified(new File(mod.getPath()), toCopy) > temp.lastModified()) {
                                    if (temp.delete() && temp.createNewFile()) {
                                        if (!copyfile.getFromResource()) {
                                            FileUtils.writeFile(ZIP.getFile(new File(mod.getPath()), toCopy), temp);
                                        } else {
                                            FileUtils.writeFile(ZIP.getFile(new File(resources0), toCopy), temp);
                                        }
                                    } else {
                                        throw new SecurityException(temp.getAbsolutePath());
                                    }
                                }
                            } else if (copyfile.overwrite() == 2) {
                                if (temp.delete() && temp.createNewFile()) {
                                    if (!copyfile.getFromResource()) {
                                        FileUtils.writeFile(ZIP.getFile(new File(mod.getPath()), toCopy), temp);
                                    } else {
                                        FileUtils.writeFile(ZIP.getFile(new File(resources0), toCopy), temp);
                                    }
                                } else {
                                    throw new SecurityException(temp.getAbsolutePath());
                                }
                            }
                        } else {
                            if (!temp.getParentFile().exists() && !temp.getParentFile().mkdirs()) {
                                throw new SecurityException(temp.getAbsolutePath());
                            }
                            if (!copyfile.getFromResource()) {
                                FileUtils.writeFile(ZIP.getFile(new File(mod.getPath()), toCopy), temp);
                            } else {
                                FileUtils.writeFile(ZIP.getFile(new File(resources0), toCopy), temp);
                            }
                        }
                        if (!copyfile.getFromResource()) {
                            temp.setLastModified(ZIP.getLastModified(new File(mod.getPath()), toCopy));
                        } else {
                            temp.setLastModified(ZIP.getLastModified(new File(resources0), toCopy));
                        }
                    }
                } else if (action.getClass().equals(ActionEditFile.class)) {
                    ActionEditFile editfile = (ActionEditFile) action;
                    beforeLastAction = lastAction;
                    lastAction = editfile;
                    if (!isValidCondition(action)) {
                        for (int i = 0; editfile.getActions() != null && i < editfile.getActions().size(); i++) {
                            counted[0]++;
                            setChanged();
                            notifyObservers(counted);
                        }
                    } else {
                        int cursor[] = new int[] { 0 };
                        int cursor2[] = new int[] { 0 };
                        File f = new File(tempFolder.getAbsolutePath() + File.separator + editfile.getName());
                        String afterEdit = "";
                        if (f.exists()) {
                            afterEdit = FileUtils.loadFile(f, "UTF-8");
                        } else {
                            try {
                                afterEdit = new String(ZIP.getFile(new File(resources0), editfile.getName()), "UTF-8");
                            } catch (FileNotFoundException e) {
                                throw new ModFileNotFoundException(mod.getName(), mod.getVersion(), e.getLocalizedMessage(), action, mod);
                            }
                        }
                        if (editfile.getActions() != null && editfile.getActions().size() > 0) {
                            for (int k = 0; k < editfile.getActions().size(); k++) {
                                counted[0]++;
                                setChanged();
                                notifyObservers(counted);
                                ActionEditFileActions editFileAction = editfile.getActions().get(k);
                                if (editFileAction.getClass().equals(ActionEditFileDelete.class)) {
                                    beforeLastAction = lastAction;
                                    lastAction = (ActionEditFileDelete) editFileAction;
                                    for (int i = 0; i < cursor.length; i++) {
                                        afterEdit = afterEdit.substring(0, cursor[i]) + afterEdit.substring(cursor2[i]);
                                        int lenght = cursor2[i] - cursor[i];
                                        cursor2[i] = cursor[i];
                                        for (int l = i + 1; l < cursor.length; l++) {
                                            cursor[l] = cursor[l] - (lenght);
                                            cursor2[l] = cursor2[l] - (lenght);
                                        }
                                    }
                                } else if (editFileAction.getClass().equals(ActionEditFileFind.class)) {
                                    ActionEditFileFind find = (ActionEditFileFind) editFileAction;
                                    if (beforeLastAction != null && beforeLastAction.getClass().equals(ActionEditFileFindAll.class)) {
                                        cursor = new int[] { 0 };
                                        cursor2 = new int[] { 0 };
                                    } else {
                                        cursor = new int[] { cursor[0] };
                                        cursor2 = new int[] { cursor2[0] };
                                    }
                                    beforeLastAction = lastAction;
                                    lastAction = find;
                                    if (find.getContent() == null || find.getContent().isEmpty()) {
                                        if (find.isPositionAtEnd()) {
                                            cursor[0] = afterEdit.length();
                                            cursor2[0] = cursor[0];
                                        } else if (find.isPositionAtStart()) {
                                            cursor[0] = 0;
                                            cursor2[0] = 0;
                                        } else {
                                            try {
                                                cursor[0] = cursor[0] + Integer.parseInt(find.getPosition());
                                                cursor2[0] = cursor[0];
                                            } catch (NumberFormatException e) {
                                                throw new InvalidModActionParameterException(mod.getName(), mod.getVersion(), (Action) find);
                                            }
                                        }
                                    } else {
                                        cursor[0] = afterEdit.toLowerCase().indexOf(find.getContent().toLowerCase(), cursor2[0]);
                                        if (cursor[0] == -1) {
                                            throw new StringNotFoundModActionException(mod.getName(), mod.getVersion(), (Action) find, find.getContent(), mod);
                                        }
                                        cursor2[0] = cursor[0] + find.getContent().length();
                                    }
                                } else if (editFileAction.getClass().equals(ActionEditFileFindUp.class)) {
                                    ActionEditFileFindUp findup = (ActionEditFileFindUp) editFileAction;
                                    if (beforeLastAction != null && beforeLastAction.getClass().equals(ActionEditFileFindAll.class)) {
                                        cursor = new int[] { 0 };
                                        cursor2 = new int[] { 0 };
                                    } else {
                                        cursor = new int[] { cursor[0] };
                                        cursor2 = new int[] { cursor2[0] };
                                    }
                                    beforeLastAction = lastAction;
                                    lastAction = findup;
                                    cursor[0] = afterEdit.toLowerCase().lastIndexOf(findup.getContent().toLowerCase(), cursor2[0]);
                                    if (cursor[0] == -1) {
                                        throw new StringNotFoundModActionException(mod.getName(), mod.getVersion(), (Action) findup, findup.getContent(), mod);
                                    }
                                    cursor2[0] = cursor[0] + findup.getContent().length();
                                } else if (editFileAction.getClass().equals(ActionEditFileFindAll.class)) {
                                    ActionEditFileFindAll findall = (ActionEditFileFindAll) editFileAction;
                                    beforeLastAction = lastAction;
                                    lastAction = findall;
                                    ArrayList<Integer> firstPosition = new ArrayList<Integer>();
                                    ArrayList<Integer> lastPosition = new ArrayList<Integer>();
                                    int index = -1;
                                    int lastIndex = 0;
                                    while ((index = afterEdit.toLowerCase().indexOf(findall.getContent().toLowerCase(), lastIndex)) != -1) {
                                        firstPosition.add(index);
                                        lastPosition.add(index + findall.getContent().length());
                                        lastIndex = index + findall.getContent().length();
                                    }
                                    if (!firstPosition.isEmpty()) {
                                        cursor = new int[firstPosition.size()];
                                        cursor2 = new int[firstPosition.size()];
                                        for (int i = 0; i < cursor.length; i++) {
                                            cursor[i] = firstPosition.get(i);
                                            cursor2[i] = lastPosition.get(i);
                                        }
                                    } else {
                                        cursor = new int[0];
                                        cursor2 = new int[0];
                                    }
                                } else if (editFileAction.getClass().equals(ActionEditFileInsert.class)) {
                                    ActionEditFileInsert insert = (ActionEditFileInsert) editFileAction;
                                    beforeLastAction = lastAction;
                                    lastAction = insert;
                                    for (int i = 0; i < cursor.length; i++) {
                                        if (insert.isPositionAfter()) {
                                            afterEdit = afterEdit.substring(0, cursor2[i]) + insert.getContent() + afterEdit.substring(cursor2[i]);
                                            cursor[i] = cursor2[i];
                                            cursor2[i] = cursor2[i] + insert.getContent().length();
                                            for (int l = i + 1; l < cursor.length; l++) {
                                                cursor[l] = cursor[l] + insert.getContent().length();
                                                cursor2[l] = cursor2[l] + insert.getContent().length();
                                            }
                                        } else if (insert.isPositionBefore()) {
                                            afterEdit = afterEdit.substring(0, cursor[i]) + insert.getContent() + afterEdit.substring(cursor[i]);
                                            cursor2[i] = cursor[i] + insert.getContent().length();
                                            for (int l = i + 1; l < cursor.length; l++) {
                                                cursor[l] = cursor[l] + insert.getContent().length();
                                                cursor2[l] = cursor2[l] + insert.getContent().length();
                                            }
                                        } else {
                                            throw new InvalidModActionParameterException(mod.getName(), mod.getVersion(), (Action) insert);
                                        }
                                    }
                                } else if (editFileAction.getClass().equals(ActionEditFileReplace.class)) {
                                    ActionEditFileReplace replace = (ActionEditFileReplace) editFileAction;
                                    beforeLastAction = lastAction;
                                    lastAction = replace;
                                    for (int i = 0; i < cursor.length; i++) {
                                        afterEdit = afterEdit.substring(0, cursor[i]) + replace.getContent() + afterEdit.substring(cursor2[i]);
                                        int difference = replace.getContent().length() - (cursor2[i] - cursor[i]);
                                        cursor2[i] = cursor[i] + replace.getContent().length();
                                        for (int l = i + 1; l < cursor.length; l++) {
                                            cursor[l] = cursor[l] + difference;
                                            cursor2[l] = cursor2[l] + difference;
                                        }
                                    }
                                } else {
                                    throw new UnknowModActionException(editFileAction.getClass().getName(), mod.getName());
                                }
                            }
                        }
                        File temp = new File(tempFolder.getAbsolutePath() + File.separator + editfile.getName().replace("\\", "/"));
                        File folder = new File(temp.getAbsolutePath().replace(temp.getName(), "") + File.separator);
                        if (!folder.getAbsolutePath().equalsIgnoreCase(tempFolder.getAbsolutePath())) {
                            if (!folder.exists()) {
                                if (!folder.mkdirs()) {
                                    throw new SecurityException(folder.getAbsolutePath());
                                }
                            }
                        }
                        if (editfile.getName().endsWith(".lua")) {
                            FileUtils.writeFile(afterEdit.getBytes("UTF-8"), temp);
                        } else {
                            FileUtils.writeFileWithBom(afterEdit.getBytes("UTF-8"), temp);
                        }
                    }
                } else if (action.getClass().equals(ActionApplyAfter.class) || action.getClass().equals(ActionApplyBefore.class) || action.getClass().equals(ActionIncompatibility.class) || action.getClass().equals(ActionRequirement.class)) {
                } else {
                    throw new UnknowModActionException(action.getClass().getName(), mod.getName());
                }
            }
        }
        setChanged();
        notifyObservers(L10n.getString("status.compressingfiles"));
        counted[0]++;
        setChanged();
        notifyObservers(counted);
        String dest = "";
        if (OS.isWindows() || OS.isLinux()) {
            dest = ManagerOptions.getInstance().getGamePath() + File.separator + "game" + File.separator + "resources999.s2z";
        } else if (OS.isMac()) {
            dest = System.getProperty("user.home") + "/Library/Application Support/Heroes of Newerth/game/resources999.s2z";
        }
        File targetZip = new File(dest);
        if (targetZip.exists()) {
            targetZip.setReadable(true);
            targetZip.setWritable(true);
            if (!targetZip.delete()) {
                throw new FileLockInterruptionException();
            }
        }
        counted[0]++;
        setChanged();
        notifyObservers(counted);
        if (deleteFolderTree) {
            deleteFolderTree();
        }
        if (!applyOrder.isEmpty()) {
            if (!developerMode) {
                String comment = "All-In Hon ModManager Output\n\nGame Version:" + Game.getInstance().getVersion() + "\n\nApplied Mods:";
                for (Iterator<Mod> it = applyOrder.iterator(); it.hasNext(); ) {
                    Mod mod = it.next();
                    comment += "\n" + mod.getName() + " (v" + mod.getVersion() + ")";
                }
                ZIP.createZIP(tempFolder.getAbsolutePath(), targetZip.getAbsolutePath(), comment);
            } else {
                if (OS.isMac()) {
                    FileUtils.copyFolderToFolder(tempFolder, new File(System.getProperty("user.home") + "/Library/Application Support/Heroes of Newerth/game"));
                } else if (OS.isWindows() || OS.isLinux()) {
                    FileUtils.copyFolderToFolder(tempFolder, new File(ManagerOptions.getInstance().getGamePath() + File.separator + "game"));
                }
                Iterator<String> it = resources0FolderTree.iterator();
                while (it.hasNext()) {
                    File folder = null;
                    if (OS.isMac()) {
                        folder = new File(System.getProperty("user.home") + "/Library/Application Support/Heroes of Newerth/game" + File.separator + it.next());
                    } else if (OS.isWindows() || OS.isLinux()) {
                        folder = new File(ManagerOptions.getInstance().getGamePath() + File.separator + "game" + File.separator + it.next());
                    }
                    if (folder.exists() && folder.isDirectory()) {
                        File warningFile = new File(folder, "! FILES AND FOLDERS HERE WILL BE DELETED ON NEXT APPLY");
                        warningFile.createNewFile();
                    }
                }
            }
        } else {
            targetZip.createNewFile();
        }
        ManagerOptions.getInstance().setAppliedMods(new HashSet<Mod>(applyOrder));
        counted[0]++;
        setChanged();
        notifyObservers(counted);
        saveOptions();
    }

    /**
     * Unapplies all currently enabled mods. After that, the method calls the saveOptions().
     * @throws SecurityException if a security issue happened, and the action couldn't be completed.
     * @throws IOException if a random I/O exception happened.
     */
    public void unapplyMods(boolean deleteFolderTree) throws SecurityException, IOException {
        ManagerOptions.getInstance().getAppliedMods().clear();
        Iterator<Mod> i = ManagerOptions.getInstance().getMods().iterator();
        while (i.hasNext()) {
            i.next().disable();
        }
        deleteFolderTree();
        try {
            applyMods(false, deleteFolderTree);
        } catch (IOException ex) {
            throw ex;
        } catch (UnknowModActionException ex) {
        } catch (ModFileNotFoundException ex) {
        } catch (NothingSelectedModActionException ex) {
        } catch (StringNotFoundModActionException ex) {
        } catch (InvalidModActionParameterException ex) {
        } catch (SecurityException ex) {
            throw ex;
        }
        try {
            saveOptions();
        } catch (IOException ex) {
            throw ex;
        }
    }

    private void deleteFolderTree() {
        Iterator<String> it = resources0FolderTree.iterator();
        while (it.hasNext()) {
            File folder = new File(ManagerOptions.getInstance().getGamePath() + File.separator + "game" + File.separator + it.next());
            if (folder.exists() && folder.isDirectory()) {
                if (!FileUtils.deleteDir(folder)) {
                }
            }
        }
    }

    public boolean hasUnappliedMods() {
        Iterator<Mod> it = ManagerOptions.getInstance().getMods().iterator();
        while (it.hasNext()) {
            Mod mod = it.next();
            if ((mod.isEnabled() && !ManagerOptions.getInstance().getAppliedMods().contains(mod)) || (!mod.isEnabled() && ManagerOptions.getInstance().getAppliedMods().contains(mod))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compares the singleVersion of a Mod and another expressionVersion. Letters are ignored (they are removed before the test) and commas (,) are replaced by dots (.)
     * @param singleVersion is the base version to be compared of. For example, a Mod's version go in here ('1.3', '3.2.57').
     * @param expressionVersion generally you put the ApplyAfter, ApplyBefore, ConditionVersion here ('1.35-*'). This can be a singleVersion like paramether too.
     * @return true if the mods have the same expressionVersion OR the singleVersion is in the range of the passed String expressionVersion. False otherwise.
     * @throws InvalidParameterException if can't compare the versions for some reason (out of format).
     */
    public boolean compareModsVersions(String singleVersion, String expressionVersion) throws InvalidParameterException {
        boolean result = false;
        if (expressionVersion == null || expressionVersion.isEmpty()) {
            expressionVersion = "*";
        }
        if (singleVersion == null || singleVersion.isEmpty()) {
            singleVersion = "*";
        }
        for (int i = 0; i < expressionVersion.length(); i++) {
            if (Character.isLetter(expressionVersion.charAt(i))) {
                expressionVersion = expressionVersion.replaceFirst(Character.toString(expressionVersion.charAt(i)), "");
            }
        }
        for (int i = 0; i < singleVersion.length(); i++) {
            if (Character.isLetter(singleVersion.charAt(i))) {
                singleVersion = singleVersion.replaceFirst(Character.toString(singleVersion.charAt(i)), "");
            }
        }
        expressionVersion = expressionVersion.replace(",", ".");
        singleVersion = singleVersion.replace(",", ".");
        if (expressionVersion.equals("*-*") || expressionVersion.equals("*") || expressionVersion.equals(singleVersion) || singleVersion.equals("*-*") || singleVersion.equals("*")) {
            result = true;
        } else if (expressionVersion.contains("-")) {
            int check = 0;
            String vEx1 = expressionVersion.substring(0, expressionVersion.indexOf("-"));
            if (vEx1 == null || vEx1.isEmpty()) {
                vEx1 = "*";
            }
            String vEx2 = expressionVersion.substring(expressionVersion.indexOf("-") + 1, expressionVersion.length());
            if (vEx2 == null || vEx2.isEmpty()) {
                vEx2 = "*";
            }
            result = checkVersion(vEx1, singleVersion) && checkVersion(singleVersion, vEx2);
        } else {
            result = singleVersion.equals(expressionVersion);
        }
        return result;
    }

    /**
     * ??
     * @param action
     * @return
     */
    private boolean isValidCondition(Action action) {
        String condition = null;
        if (action.getClass().equals(ActionEditFile.class)) {
            ActionEditFile editfile = (ActionEditFile) action;
            condition = editfile.getCondition();
        } else if (action.getClass().equals(ActionCopyFile.class)) {
            ActionCopyFile copyfile = (ActionCopyFile) action;
            condition = copyfile.getCondition();
        }
        if (condition == null || condition.isEmpty()) {
            return true;
        }
        return isValidCondition(condition);
    }

    /**
     * findNextExpression returns the next expression from the given StringTokenizer
     * @param st
     * @return
     */
    private String findNextExpression(String previous, StringTokenizer st) {
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equalsIgnoreCase("\'")) {
                String mod = token;
                while (st.hasMoreTokens()) {
                    String next = st.nextToken();
                    if (next.equalsIgnoreCase("\'")) {
                        mod += next;
                        break;
                    }
                    mod += next;
                }
                return mod;
            } else if (token.equalsIgnoreCase("(")) {
                String cond = "";
                boolean done = false;
                int level = 0;
                while (!done && st.hasMoreTokens()) {
                    String next = st.nextToken();
                    if (next.equalsIgnoreCase("(")) {
                        level++;
                    } else if (next.equalsIgnoreCase(")")) {
                        if (level == 0 && !done) {
                            break;
                        } else {
                            level--;
                        }
                    }
                    cond += next;
                }
                return cond;
            } else if (token.equalsIgnoreCase(" ")) {
                continue;
            } else {
                String ret = "";
                boolean added = false;
                while (st.hasMoreTokens()) {
                    ret += token;
                    added = true;
                    try {
                        token = st.nextToken();
                        added = false;
                    } catch (Exception e) {
                        break;
                    }
                }
                if (!added) {
                    ret += token;
                }
                return ret;
            }
        }
        return "";
    }

    /**
     * checkVersion checks to see if first argument <= second argument is true
     * @param lower
     * @param higher
     * @return
     */
    public boolean checkVersion(String lower, String higher) {
        if (lower.equalsIgnoreCase("*") || higher.equalsIgnoreCase("*")) {
            return true;
        }
        StringTokenizer lowst = new StringTokenizer(lower, ".", false);
        StringTokenizer highst = new StringTokenizer(higher, ".", false);
        while (lowst.hasMoreTokens() && highst.hasMoreTokens()) {
            String firsttk = lowst.nextToken();
            String secondtk = highst.nextToken();
            if (firsttk.contains("*") || secondtk.contains("*")) {
                return true;
            }
            int first = Integer.parseInt(firsttk);
            int second = Integer.parseInt(secondtk);
            if (first < second) {
                return true;
            } else if (first > second) {
                return false;
            } else if (first == second) {
                continue;
            }
        }
        if (lowst.hasMoreTokens()) {
            return false;
        }
        return true;
    }

    /**
     * validVesion checks to see if the version for m is within the condition set by version on the other parameter
     * in development
     * @param m
     * @param version
     * @return
     */
    public boolean validVersion(Mod m, String version) throws NumberFormatException {
        String target = m.getVersion().trim();
        if (version.contains("-")) {
            String low, high;
            low = version.substring(1, version.indexOf("-")).trim();
            high = version.substring(version.indexOf("-") + 1).trim();
            return checkVersion(low, target) && checkVersion(target, high);
        } else if (version.isEmpty()) {
            return true;
        } else {
            String compare = version.trim();
            return compare.equalsIgnoreCase(target);
        }
    }

    /**
     * isValidCondition evaluates the condition string and return the result of it
     * Looks like it's working now, should work ;)
     * @param condition
     * @return
     */
    public boolean isValidCondition(String condition) {
        boolean valid = true;
        StringTokenizer st = new StringTokenizer(condition, "\' ()", true);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equalsIgnoreCase("\'")) {
                String mod = "";
                while (st.hasMoreTokens()) {
                    String next = st.nextToken();
                    if (next.equalsIgnoreCase("\'")) {
                        break;
                    }
                    mod += next;
                }
                try {
                    String version = "";
                    if (mod.endsWith("]")) {
                        version = mod.substring(mod.indexOf('[') + 1, mod.length() - 1);
                        mod = mod.substring(0, mod.indexOf('['));
                    }
                    Mod m = getMod(mod, version);
                    try {
                        valid = (m.isEnabled() && validVersion(m, version));
                    } catch (Exception e) {
                        valid = false;
                    }
                } catch (NoSuchElementException e) {
                    valid = false;
                }
            } else if (token.equalsIgnoreCase(" ")) {
                continue;
            } else if (token.equalsIgnoreCase("(")) {
                String cond = "";
                boolean done = false;
                int level = 0;
                while (!done && st.hasMoreTokens()) {
                    String next = st.nextToken();
                    if (next.equalsIgnoreCase("(")) {
                        level++;
                    } else if (next.equalsIgnoreCase(")")) {
                        if (level == 0 && !done) {
                            break;
                        } else {
                            level--;
                        }
                    }
                    cond += next;
                }
                return isValidCondition(cond);
            } else if (token.equalsIgnoreCase(")")) {
                return false;
            } else {
                String next = findNextExpression(token, st);
                if (token.equalsIgnoreCase("not")) {
                    valid = !isValidCondition(next);
                } else if (token.equalsIgnoreCase("and")) {
                    boolean compare = isValidCondition(next);
                    valid = (valid && compare);
                } else if (token.equalsIgnoreCase("or")) {
                    boolean compare = isValidCondition(next);
                    valid = (valid || compare);
                } else {
                    String mod = token + " " + next;
                }
            }
        }
        return valid;
    }
}
