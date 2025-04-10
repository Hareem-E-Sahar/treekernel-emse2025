package org.openthinclient.util.dpkg;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import org.apache.commons.io.FileSystemUtils;
import org.apache.log4j.Logger;
import org.openthinclient.pkgmgr.PackageManager;
import org.openthinclient.pkgmgr.PackageManagerException;
import org.openthinclient.pkgmgr.UpdateDatabase;
import org.openthinclient.pkgmgr.connect.DownloadFiles;
import com.levigo.util.preferences.PreferenceStoreHolder;

/**
 * This is the Heart of the whole PackageManger all the different Databases
 * Files and Packages are handeld in here. nearly every interaction is started
 * here.
 * 
 * @author levigo
 */
public class DPKGPackageManager implements PackageManager {

    private static final int OLDINSTALLDIR_FOR_DELETE = 0;

    private static final int INSTALLDIR_FOR_DELETE = 1;

    private static final Logger logger = Logger.getLogger(DPKGPackageManager.class);

    private final LinkedList<String> warnings = new LinkedList<String>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private String installDir;

    private static String archivesDir;

    private String testinstallDir;

    private String oldInstallDir;

    private String listsDir;

    private List<PackagingConflict> conflicts = new ArrayList<PackagingConflict>();

    private int actprogress;

    private final int maxProgress = 100;

    private long maxVolumeinByte;

    private final Thread shutdownHook;

    public static class PackagingConflict {

        public enum Type {

            ALREADY_INSTALLED, CONFLICT_EXISTING, CONFLICT_NEW, UNSATISFIED, FILE_CONFLICT, CONFLICT_WITHIN
        }

        ;

        private final Type type;

        private final Package pkg;

        private Package conflicting;

        private List<Package> pkgs;

        private PackageReference ref;

        private File file;

        public PackagingConflict(Type type, Package pkg) {
            this.type = type;
            this.pkg = pkg;
        }

        public PackagingConflict(Type type, Package pkg, Package conflicting) {
            this(type, pkg);
            this.conflicting = conflicting;
        }

        public PackagingConflict(Type type, PackageReference ref, List<Package> pkgs) {
            this(type, null);
            this.ref = ref;
            this.pkgs = pkgs;
        }

        public PackagingConflict(Type type, Package pkg, Package conflicting, File f) {
            this(type, pkg, conflicting);
            this.file = f;
        }

        public Package getConflictingPackage() {
            return this.pkg;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer();
            switch(type) {
                case ALREADY_INSTALLED:
                    sb.append(pkg).append(" ");
                    break;
                case CONFLICT_EXISTING:
                    sb.append(pkg.forConflictsToString()).append(" ").append(" ").append(conflicting.forConflictsToString());
                    break;
                case CONFLICT_NEW:
                    sb.append(pkg.forConflictsToString()).append(" ").append(" ").append(conflicting.forConflictsToString());
                    break;
                case UNSATISFIED:
                    sb.append(" ").append(ref).append(" ").append(" ");
                    for (final Package pkg : pkgs) sb.append(pkg.getName()).append(" ");
                    break;
                case FILE_CONFLICT:
                    sb.append(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.toString.ALREADY_INSTALLED", "No entry found for packageManager.toString.ALREADY_INSTALLED")).append(" ").append(file).append(" ").append(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.toString.ALREADY_INSTALLED", "No entry found for packageManager.toString.ALREADY_INSTALLED")).append(" ").append(pkg).append(" ").append(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.toString.ALREADY_INSTALLED", "No entry found for packageManager.toString.ALREADY_INSTALLED")).append(" ").append(conflicting);
                    break;
            }
            return sb.toString();
        }
    }

    public static PackageDatabase installedPackages;

    public static PackageDatabase removedDB;

    public static PackageDatabase availablePackages;

    public static PackageDatabase archivesDB;

    public DPKGPackageManager(PackageDatabase availableDB, PackageDatabase removedDB, PackageDatabase installedDB, PackageDatabase archivesDB, Collection<String> locations, boolean removeItReally) throws IOException {
        DPKGPackageManager.installedPackages = installedDB;
        DPKGPackageManager.removedDB = removedDB;
        DPKGPackageManager.availablePackages = availableDB;
        DPKGPackageManager.archivesDB = archivesDB;
        int value = 0;
        for (final String s : locations) {
            switch(value) {
                case 0:
                    installDir = s;
                    break;
                case 1:
                    archivesDir = s;
                    break;
                case 2:
                    testinstallDir = s;
                    break;
                case 3:
                    oldInstallDir = s;
                    break;
                case 4:
                    listsDir = s;
            }
            value++;
        }
        shutdownHook = new Thread("PackageManager shutdown") {

            @Override
            public void run() {
                try {
                    DPKGPackageManager.this.close();
                } catch (final PackageManagerException e) {
                    e.printStackTrace();
                    logger.error(e);
                    addWarning(e.toString());
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    public void close() throws PackageManagerException {
        if (null != shutdownHook) Runtime.getRuntime().removeShutdownHook(shutdownHook);
        lock.writeLock().lock();
        try {
            installedPackages.save();
            availablePackages.save();
            removedDB.save();
            archivesDB.save();
            installedPackages.close();
            availablePackages.close();
            archivesDB.close();
            removedDB.close();
            installedPackages.finalize();
            availablePackages.finalize();
            archivesDB.finalize();
            removedDB.finalize();
        } catch (final Exception e) {
            e.printStackTrace();
            addWarning(e.toString());
            logger.error(e);
        } catch (final Throwable e) {
            e.printStackTrace();
            addWarning(e.toString());
            logger.error(e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void finalize() throws PackageManagerException {
        close();
    }

    /**
	 * install the packages first to a testinstall directory and move them to the
	 * real install directory Returns: true only if all packages are installed on
	 * the drive and also in the packagesDB otherwise it will return false
	 * 
	 * @param installList
	 * @return an boolean value which gives feedback if the packages could be
	 *         installed
	 * @throws PackageManagerException
	 */
    private boolean installPackages(Collection<Package> firstinstallList) throws PackageManagerException {
        boolean ret = false;
        final ArrayList<Package> installList = new ArrayList<Package>();
        for (final Package pkg : firstinstallList) if (pkg.isPackageManager()) installList.add((Package) DeepObjectCopy.clone(pkg, this)); else installList.add(0, (Package) DeepObjectCopy.clone(pkg, this));
        firstinstallList.removeAll(firstinstallList);
        final List<File> filesToDelete = new ArrayList<File>();
        final List<File> directoriesToDelete = new ArrayList<File>();
        List<Package> PackagesForDatabase = new ArrayList<Package>();
        final List<InstallationLogEntry> log = new ArrayList<InstallationLogEntry>();
        int sizeOfS = 0;
        lock.writeLock().lock();
        try {
            final TreeSet<File> s = new TreeSet<File>();
            final int listlength = installList.size();
            int listactuallystands = 0;
            int actuallyProgress = getActprogress();
            for (final Package pkg : installList) {
                pkg.install(new File(testinstallDir), log, archivesDir, this);
                final List<File> dirsForPackage = new ArrayList<File>();
                final List<File> filesForPackage = new ArrayList<File>();
                for (final File fi : pkg.getDirectoryList()) if (!fi.getPath().replace(testinstallDir, " ").equalsIgnoreCase(" ")) {
                    s.add(new File(fi.getPath().replace(testinstallDir, " ").trim()));
                    if (fi.getPath().replace(new File(testinstallDir).getPath(), "").length() != 0) dirsForPackage.add(new File(fi.getPath().replace(testinstallDir, "")));
                }
                for (final File fi : pkg.getFileList()) if (!fi.getPath().replace(testinstallDir, " ").equalsIgnoreCase(" ")) {
                    s.add(new File(fi.getPath().replace(testinstallDir, " ").trim()));
                    filesForPackage.add(new File(fi.getPath().replace(testinstallDir, "")));
                }
                pkg.setDirectoryList(dirsForPackage);
                pkg.setFileList(filesForPackage);
                PackagesForDatabase.add(pkg);
                listactuallystands++;
                setActprogress(actuallyProgress + new Double(listactuallystands / listlength * 20).intValue());
            }
            actuallyProgress = getActprogress();
            sizeOfS = s.size();
            final Iterator<File> it = s.iterator();
            File iteratorFile = null;
            boolean secondTime = false;
            int iteratorFiles = 0;
            while (it.hasNext() || secondTime) {
                if (secondTime) secondTime = false; else iteratorFile = it.next();
                if (testinstallDir.equalsIgnoreCase(iteratorFile.getPath())) iteratorFile = it.next();
                if (testinstallDir.equalsIgnoreCase(iteratorFile.getPath() + File.separator)) iteratorFile = it.next();
                File newFile = new File(installDir + iteratorFile.getPath());
                File oldFile = new File(testinstallDir + iteratorFile.getPath());
                if (!newFile.isDirectory() && oldFile.isDirectory()) {
                    if (!oldFile.renameTo(newFile)) {
                        addWarning(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.installPackages.problem1", "No entry found for packageManager.installPackages.problem1") + " " + newFile.getName() + " " + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.installPackages.problem2", "No entry found for packageManager.installPackages.problem2"));
                        logger.error(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.installPackages.problem1", "No entry found for packageManager.installPackages.problem1") + " " + newFile.getName() + " " + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.installPackages.problem2", "No entry found for packageManager.installPackages.problem2"));
                    }
                    boolean next = true;
                    while (it.hasNext() && next) {
                        final File iteratorFileNext = it.next();
                        if (iteratorFileNext.getPath().length() >= iteratorFile.getPath().length()) {
                            if (iteratorFile.getPath().equalsIgnoreCase(iteratorFileNext.getPath().substring(0, iteratorFile.getPath().length()))) {
                                if (new File(installDir + iteratorFileNext).isDirectory()) directoriesToDelete.add(new File(testinstallDir + iteratorFileNext)); else if (new File(installDir + iteratorFileNext).isFile()) filesToDelete.add(new File(testinstallDir + iteratorFileNext)); else {
                                    next = false;
                                    iteratorFile = iteratorFileNext;
                                }
                            } else {
                                next = false;
                                iteratorFile = iteratorFileNext;
                            }
                        } else {
                            next = false;
                            iteratorFile = iteratorFileNext;
                        }
                    }
                    if (testinstallDir.equalsIgnoreCase(iteratorFile.getPath()) && it.hasNext()) iteratorFile = it.next();
                    if (testinstallDir.equalsIgnoreCase(iteratorFile.getPath() + File.separator) && it.hasNext()) iteratorFile = it.next();
                    newFile = new File(installDir + iteratorFile.getPath());
                    oldFile = new File(testinstallDir + iteratorFile.getPath());
                }
                if (oldFile.isDirectory()) {
                    if (newFile.isDirectory()) directoriesToDelete.add(oldFile); else if (!newFile.isDirectory()) secondTime = true; else {
                        logger.error(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.installPackages.problem1", "No entry found for packageManager.installPackages.problem1") + " " + newFile.getName() + " " + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.installPackages.problem2", "No entry found for packageManager.installPackages.problem2"));
                        addWarning(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.installPackages.problem1", "No entry found for packageManager.installPackages.problem1") + " " + newFile.getName() + " " + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.installPackages.problem2", "No entry found for packageManager.installPackages.problem2"));
                    }
                } else if (oldFile.isFile()) {
                    if (newFile.isFile()) filesToDelete.add(oldFile); else if (!newFile.isFile()) {
                        if (!oldFile.renameTo(newFile)) {
                            addWarning(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.installPackages.problem1", "No entry found for packageManager.installPackages.problem1") + " " + newFile.getName() + " " + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.installPackages.problem2", "No entry found for packageManager.installPackages.problem2"));
                            logger.error(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.installPackages.problem1", "No entry found for packageManager.installPackages.problem1") + " " + newFile.getName() + " " + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.installPackages.problem2", "No entry found for packageManager.installPackages.problem2"));
                        }
                    } else {
                        addWarning(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.installPackages.problem1", "No entry found for packageManager.installPackages.problem1") + " " + newFile.getName() + " " + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.installPackages.problem2", "No entry found for packageManager.installPackages.problem2"));
                        logger.error(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.installPackages.problem1", "No entry found for packageManager.installPackages.problem1") + " " + newFile.getName() + " " + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.installPackages.problem2", "No entry found for packageManager.installPackages.problem2"));
                    }
                } else if (newFile.isFile()) {
                } else if (newFile.isDirectory()) {
                } else if (testinstallDir.equalsIgnoreCase(iteratorFile.getPath())) {
                } else if (testinstallDir.equalsIgnoreCase(iteratorFile.getPath() + File.separator)) {
                } else {
                    logger.error(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.installPackages.problem1", "No entry found for packageManager.installPackages.problem1") + " " + newFile.getName() + " " + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.installPackages.problem2", "No entry found for packageManager.installPackages.problem2"));
                    addWarning(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.installPackages.problem1", "No entry found for packageManager.installPackages.problem1") + " " + newFile.getName() + " " + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("packageManager.installPackages.problem2", "No entry found for packageManager.installPackages.problem2"));
                }
                iteratorFiles++;
                setActprogress(actuallyProgress + new Double(iteratorFiles / sizeOfS * 18).intValue());
            }
            for (final Package pack : PackagesForDatabase) {
                installedPackages.addPackage(pack);
                installedPackages.save();
            }
            ret = true;
        } catch (final PackageManagerException t) {
            t.printStackTrace();
            for (final Package pkg : PackagesForDatabase) for (final File file : pkg.getFileList()) filesToDelete.add(new File(testinstallDir + file.getPath()));
            for (final Package pkg : PackagesForDatabase) for (final File file : pkg.getDirectoryList()) filesToDelete.add(new File(testinstallDir + file.getPath()));
            rollbackInstallation(log);
            PackagesForDatabase = new ArrayList<Package>();
            logger.error(t);
            addWarning(t.toString());
        } catch (final IOException e) {
            e.printStackTrace();
            logger.error(e);
            addWarning(e.toString());
        } finally {
            lock.writeLock().unlock();
        }
        if (filesToDelete != null) if (filesToDelete.size() > 0) for (int n = filesToDelete.size() - 1; n > 0; n--) if (filesToDelete.get(n).isFile()) if (!filesToDelete.get(n).delete()) {
            addWarning(filesToDelete.get(n).getPath() + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("interface.notRemove", "No entry found for interface.notRemove"));
            logger.error(filesToDelete.get(n).getPath() + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("interface.notRemove", "No entry found for interface.notRemove"));
        }
        if (directoriesToDelete != null) if (directoriesToDelete.size() > 0) for (int n = directoriesToDelete.size() - 1; n > 0; n--) if (directoriesToDelete.get(n).isDirectory()) if (directoriesToDelete.get(n).listFiles().length == 0) if (!directoriesToDelete.get(n).delete()) {
            addWarning(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("interface.DirectoryUndeleteable", "No entry found for interface.DirectoryUndeleteable") + directoriesToDelete.get(n).getName());
            logger.error(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("interface.DirectoryUndeleteable", "No entry found for interface.DirectoryUndeleteable") + directoriesToDelete.get(n).getName());
        }
        return ret;
    }

    /**
	 * if the installation goes wrong this method will undo it!
	 * 
	 * @param log
	 */
    private void rollbackInstallation(List<InstallationLogEntry> log) {
        Collections.reverse(log);
        for (final InstallationLogEntry entry : log) switch(entry.getType()) {
            case FILE_INSTALLATION:
                logger.warn("Rollback: removing " + entry.getTargetFile());
                if (entry.getTargetFile().exists() && !entry.getTargetFile().delete()) {
                    addWarning(entry.getTargetFile() + " " + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("setProperties.notRemove", "No entry found for setProperties.notRemove"));
                    logger.error(entry.getTargetFile() + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("interface.notRemove", "No entry found for interface.notRemove"));
                }
                break;
            case SYMLINK_INSTALLATION:
                logger.warn("Rollback: removing symlink " + entry.getTargetFile());
                if (entry.getTargetFile().exists() && !entry.getTargetFile().delete()) {
                    addWarning(entry.getTargetFile() + " " + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("setProperties.notRemove", "No entry found for setProperties.notRemove"));
                    logger.error(entry.getTargetFile() + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("interface.notRemove", "No entry found for interface.notRemove"));
                }
                break;
            case FILE_MODIFICATION:
                logger.warn("Rollback: reverting modification on " + entry.getTargetFile());
                if (!entry.getBackupFile().renameTo(entry.getTargetFile())) {
                    addWarning(entry.getTargetFile() + " " + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("setProperties.cantMove", "No entry found for setProperties.cantMove"));
                    logger.error(entry.getBackupFile() + " " + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("setProperties.cantMove", "No entry found for setProperties.cantMove") + " " + entry.getTargetFile());
                }
                break;
            case FILE_REMOVAL:
                logger.warn("Rollback: reverting deletion of " + entry.getTargetFile());
                if (!entry.getBackupFile().renameTo(entry.getTargetFile())) {
                    addWarning(entry.getTargetFile() + " " + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("setProperties.cantMove", "No entry found for setProperties.cantMove"));
                    logger.error(entry.getBackupFile() + " " + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("setProperties.cantMove", "No entry found for setProperties.cantMove") + " " + entry.getTargetFile());
                }
                break;
            case DIRECTORY_CREATION:
                logger.warn("Rollback: reverting creation of directory " + entry.getTargetFile());
                if (!entry.getTargetFile().delete()) {
                    addWarning(entry.getTargetFile() + " " + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("setProperties.cantMove", "No entry found for setProperties.cantMove"));
                    logger.error(entry.getTargetFile() + " " + PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("setProperties.cantMove", "No entry found for setProperties.cantMove"));
                }
                break;
        }
    }

    public String checkForAlreadyInstalled(List<Package> installList) {
        lock.readLock().lock();
        try {
            for (final Package toBeInstalled : installList) if (installedPackages.isPackageInstalled(toBeInstalled.getName())) {
                if (conflicts == null) conflicts = new ArrayList<PackagingConflict>();
                conflicts.add(new PackagingConflict(PackagingConflict.Type.ALREADY_INSTALLED, toBeInstalled));
            }
            if (conflicts.size() > 0) return conflicts.toString(); else return "";
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean removeConflicts() {
        if (null != conflicts && !conflicts.removeAll(conflicts)) return false; else return true;
    }

    public void invokeDeploymentScan() {
        try {
            final ObjectName objectName = new ObjectName("jboss.deployment:flavor=URL,type=DeploymentScanner");
            final MBeanServer server = MBeanServerFactory.findMBeanServer(null).get(0);
            server.invoke(objectName, "scan", new Object[] {}, new String[] {});
        } catch (final Exception e) {
            e.printStackTrace();
            addWarning(e.getLocalizedMessage());
        }
    }

    private final List<Package> pack = new LinkedList<Package>();

    /**
	 * 
	 * @param installList
	 * @return a ArrayList which contains the packages which are given and also
	 *         the packages on which they depends
	 */
    public List<Package> solveDependencies(Collection<Package> installList) {
        final Map<PackageReference, List<Package>> unsatisfiedDependencies = new HashMap<PackageReference, List<Package>>();
        for (final Package toBeInstalled : installList) {
            if (toBeInstalled.getDepends() instanceof ANDReference) {
                final PackageReference[] dependsPackRef = new PackageReference[((ANDReference) toBeInstalled.getDepends()).getRefs().length];
                for (int j = 0; j < dependsPackRef.length; j++) dependsPackRef[j] = ((ANDReference) toBeInstalled.getDepends()).getRefs()[j];
                for (int i = 0; i < dependsPackRef.length; i++) if (dependsPackRef[i] instanceof ORReference) {
                    final PackageReference[] deopendsOrPackRef = new PackageReference[((ORReference) dependsPackRef[i]).getRefs().length];
                    for (int x = 0; x < deopendsOrPackRef.length; x++) deopendsOrPackRef[x] = ((ORReference) dependsPackRef[i]).getRefs()[x];
                    for (int x = 0; x < deopendsOrPackRef.length; x++) addDependency(unsatisfiedDependencies, deopendsOrPackRef[0], toBeInstalled);
                } else addDependency(unsatisfiedDependencies, dependsPackRef[i], toBeInstalled);
            } else addDependency(unsatisfiedDependencies, toBeInstalled.getDepends(), toBeInstalled);
            if (toBeInstalled.getPreDepends() instanceof ANDReference) {
                final PackageReference[] packRef = new PackageReference[((ANDReference) toBeInstalled.getPreDepends()).getRefs().length];
                for (int j = 0; j < packRef.length; j++) packRef[j] = ((ANDReference) toBeInstalled.getPreDepends()).getRefs()[j];
                for (int i = 0; i < packRef.length; i++) if (packRef[i] instanceof ORReference) {
                    final PackageReference[] orPackRef = new PackageReference[((ORReference) packRef[i]).getRefs().length];
                    for (int x = 0; x < orPackRef.length; x++) orPackRef[x] = ((ORReference) packRef[i]).getRefs()[x];
                    for (int x = 0; x < orPackRef.length; x++) addDependency(unsatisfiedDependencies, orPackRef[0], toBeInstalled);
                } else addDependency(unsatisfiedDependencies, packRef[i], toBeInstalled);
            } else addDependency(unsatisfiedDependencies, toBeInstalled.getPreDepends(), toBeInstalled);
        }
        final Map<String, Package> virtualPackagesToBeInstalled = new HashMap<String, Package>();
        for (final Package pkg : installList) {
            virtualPackagesToBeInstalled.put(pkg.getName(), pkg);
            if (pkg.getProvides() instanceof ANDReference) for (final PackageReference r : ((ANDReference) pkg.getProvides()).getRefs()) virtualPackagesToBeInstalled.put(r.getName(), pkg); else virtualPackagesToBeInstalled.put(pkg.getProvides().getName(), pkg);
        }
        lock.readLock().lock();
        try {
            for (final Iterator<PackageReference> i = unsatisfiedDependencies.keySet().iterator(); i.hasNext(); ) {
                final PackageReference ref = i.next();
                if (ref.isSatisfiedBy(installedPackages.getProvidedPackages()) || ref.isSatisfiedBy(virtualPackagesToBeInstalled)) i.remove();
            }
        } finally {
            lock.readLock().unlock();
        }
        final ArrayList<Entry<PackageReference, List<Package>>> deps = new ArrayList<Entry<PackageReference, List<Package>>>(unsatisfiedDependencies.entrySet());
        Collections.sort(deps, new Comparator<Entry<PackageReference, List<Package>>>() {

            public int compare(Entry<PackageReference, List<Package>> o1, Entry<PackageReference, List<Package>> o2) {
                return o1.getKey().getName().compareTo(o2.getKey().getName());
            }
        });
        final List<Package> ret = new LinkedList<Package>();
        boolean anotherDependency = false;
        boolean check = false;
        for (final Map.Entry<PackageReference, List<Package>> entry : deps) {
            lock.readLock().lock();
            try {
                for (int i = 0; i < entry.getValue().size(); i++) if (availablePackages.getPackage(entry.getKey().getName()) == null) {
                    final List<Package> provided = new ArrayList<Package>();
                    for (int t = 0; t < availablePackages.getProvidesPackages(entry.getKey().getName()).size(); t++) provided.add(availablePackages.getProvidesPackages(entry.getKey().getName()).get(t));
                    if (provided.size() > 0) {
                        String ausw = "";
                        final BufferedReader in1 = new BufferedReader(new InputStreamReader(new DataInputStream(System.in)));
                        try {
                            ausw = in1.readLine();
                        } catch (final IOException e) {
                            e.printStackTrace();
                        }
                        final int prov = Integer.valueOf(ausw).intValue();
                        ret.add(provided.get(prov - 1));
                        anotherDependency = true;
                    }
                } else {
                    for (int n = 0; n < ret.size(); n++) if (ret.get(n).getName().equalsIgnoreCase(entry.getKey().getName())) check = true;
                    for (int x = 0; x < pack.size(); x++) if (pack.get(x).getName().equalsIgnoreCase(entry.getKey().getName())) check = true;
                    if (check == false) {
                        ret.add(availablePackages.getPackage(entry.getKey().getName()));
                        anotherDependency = true;
                    }
                    check = false;
                }
            } finally {
                lock.readLock().unlock();
            }
        }
        final ArrayList<Package> samePackages = new ArrayList<Package>();
        for (final Package pkg1 : pack) for (final Package pkg2 : installList) if (pkg1.equals(pkg2)) samePackages.add(pkg2);
        if (samePackages.size() != 0) for (final Package pkg : samePackages) installList.remove(pkg);
        pack.addAll(installList);
        if (anotherDependency) {
            solveDependencies(ret);
            return pack;
        } else return pack;
    }

    public void refreshSolveDependencies() {
        pack.removeAll(pack);
    }

    /**
	 * adds a dependency to the existent ones
	 * 
	 * @param unsatisfiedDependencies
	 * @param r
	 * @param toBeInstalled
	 */
    private void addDependency(Map<PackageReference, List<Package>> unsatisfiedDependencies, PackageReference r, Package toBeInstalled) {
        if (!unsatisfiedDependencies.containsKey(r)) unsatisfiedDependencies.put(r, new LinkedList<Package>());
        unsatisfiedDependencies.get(r).add(toBeInstalled);
    }

    private final List<Package> packForDelete = new LinkedList<Package>();

    /**
	 * will give a List of all Packages which has dependencies on the packages
	 * which are given and the given packages.
	 * 
	 * @param packList
	 * @return PackageList with all dependencies and given packages
	 */
    public List<Package> isDependencyOf(Collection<Package> packList) {
        packForDelete.removeAll(packForDelete);
        return getDependencyOf(packList);
    }

    /**
	 * 
	 * @param packList
	 * @return List of conflicts
	 */
    @SuppressWarnings("unchecked")
    public String findConflicts(List<Package> packList) {
        lock.readLock().lock();
        Collection<Package> existingPackages = Collections.EMPTY_LIST;
        try {
            existingPackages = installedPackages.getPackages();
        } finally {
            lock.readLock().unlock();
        }
        conflicts.addAll(findConflicts(existingPackages, packList, PackagingConflict.Type.CONFLICT_EXISTING));
        conflicts.addAll(findConflicts(packList, existingPackages, PackagingConflict.Type.CONFLICT_NEW));
        conflicts.addAll(findConflicts(packList, packList, PackagingConflict.Type.CONFLICT_WITHIN));
        if (conflicts.size() > 0) return conflicts.toString(); else return "";
    }

    /**
	 * find conflicts out of two given collections
	 * 
	 * @param l1
	 * @param l2
	 * @param type
	 * @return Collection of conflicts
	 */
    private Collection<PackagingConflict> findConflicts(Collection<Package> l1, Collection<Package> l2, PackagingConflict.Type type) {
        final Collection<PackagingConflict> conflicts = new ArrayList<PackagingConflict>();
        for (final Package p1 : l1) for (final Package p2 : l2) if (p1 != p2 && p1.getConflicts().matches(p2)) conflicts.add(new PackagingConflict(type, p2, p1));
        return conflicts;
    }

    public Collection<Package> getInstallablePackages() throws PackageManagerException {
        final Collection<Package> installable = new ArrayList<Package>();
        final Collection<Package> installed = new ArrayList<Package>();
        lock.readLock().lock();
        try {
            for (final Package pkg : availablePackages.getPackages()) installable.add((Package) DeepObjectCopy.clone(pkg, this));
            for (final Package pkg : installable) if (installedPackages.isPackageInstalled(pkg.getName())) installed.add(pkg);
        } finally {
            lock.readLock().unlock();
        }
        installable.removeAll(installed);
        installed.removeAll(installed);
        return installable;
    }

    @SuppressWarnings("unchecked")
    public Collection<Package> getInstalledPackages() {
        Collection<Package> ret;
        lock.readLock().lock();
        try {
            if (null == installedPackages.getPackages()) ret = new ArrayList<Package>(Collections.EMPTY_LIST); else ret = new ArrayList<Package>(installedPackages.getPackages());
            return ret;
        } finally {
            lock.readLock().unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public Collection<Package> getUpdateablePackages() {
        ArrayList<Package> update = new ArrayList<Package>();
        lock.readLock().lock();
        try {
            for (final Package pkg : installedPackages.getPackages()) {
                final String s = pkg.getName();
                if (availablePackages.isPackageInstalled(s)) if (pkg.getVersion().compareTo(availablePackages.getPackage(s).getVersion()) == -1) update.add(availablePackages.getPackage(s));
            }
        } finally {
            lock.readLock().unlock();
        }
        if (update.size() < 1) update = new ArrayList<Package>(Collections.EMPTY_LIST);
        return update;
    }

    @SuppressWarnings("unchecked")
    public Collection<Package> getDebianFilePackages() {
        lock.readLock().lock();
        try {
            if (archivesDB == null) return Collections.EMPTY_LIST; else return archivesDB.getPackages();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
	 * return the actually formatted date type in YYYY_MM_DD_HH_MM_ss
	 * 
	 * @return String the actually formatted date
	 */
    private String getFormattedDate() {
        final SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy'_'MM'_'dd'_'HH'_'mm'_'ss");
        return sdf.format(new GregorianCalendar().getTime());
    }

    /**
	 * download the given packages from a server which is given in the
	 * sources.list
	 * 
	 * @param downloadable
	 * @return TRUE only if all packages could downloaded properly otherwise FALSE
	 * @throws IOException
	 * @throws PackageManagerException
	 */
    private boolean downloadPackages(ArrayList<Package> downloadable) throws IOException, PackageManagerException {
        boolean ret = false;
        if (downloadable.size() > 0) {
            downloadable = new ArrayList<Package>(checkIfFilesAreInDatabase(downloadable));
            final String conflictString = findConflicts(downloadable);
            if (conflictString != "") {
                addWarning(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("DPKGPackageManager.downloadPackages.conflicts", "No enry found for DPKGPackageManager.downloadPackages.conflicts"));
                logger.error(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("DPKGPackageManager.downloadPackages.conflicts", "No enry found for DPKGPackageManager.downloadPackages.conflicts"));
            } else if (downloadable.size() > 0) {
                long installSizeInKB = 0;
                for (final Package pack : downloadable) {
                    installSizeInKB = installSizeInKB + pack.getSize() / 1024;
                    installSizeInKB = installSizeInKB + pack.getInstalledSize();
                    maxVolumeinByte = maxVolumeinByte + pack.getSize();
                }
                if (installSizeInKB < FileSystemUtils.freeSpaceKb(installDir)) {
                    if (new DownloadFiles(this).downloadAndMD5sumCheck(downloadable)) {
                        if (getActprogress() < new Double(maxProgress * 0.6).intValue()) setActprogress(new Double(maxProgress * 0.6).intValue());
                        lock.writeLock().lock();
                        try {
                            for (final Package pkg : downloadable) {
                                Package pack = null;
                                if (archivesDB.isPackageInstalledDontVerifyVersion(pkg.getName())) {
                                    if (!archivesDB.getPackage(pkg.getName()).getVersion().equals(pkg.getVersion())) {
                                        pack = (Package) DeepObjectCopy.clone(pkg, this);
                                        if (pack != null) {
                                            pack.setName(pack.getFilename());
                                            archivesDB.addPackageDontVerifyVersion(pack);
                                        }
                                    }
                                } else {
                                    pack = (Package) DeepObjectCopy.clone(pkg, this);
                                    if (null != pack) {
                                        pack.setName(pack.getFilename());
                                        archivesDB.addPackageDontVerifyVersion(pack);
                                    }
                                }
                            }
                            setActprogress(new Double(maxProgress * 0.7).intValue());
                            archivesDB.save();
                            if (installPackages(downloadable)) ret = true;
                        } finally {
                            lock.writeLock().unlock();
                        }
                    } else {
                        addWarning(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("DPKGPackageManager.downloadPackages.MD5Failed", "No enry found for DPKGPackageManager.downloadPackages.MD5Failed"));
                        logger.error(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("DPKGPackageManager.downloadPackages.MD5Failed", "No enry found for DPKGPackageManager.downloadPackages.MD5Failed"));
                    }
                } else {
                    downloadable.removeAll(downloadable);
                    throw new PackageManagerException(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("interface.notEnoughtSpaceOnDisk", "No enry found for interface.notEnoughtSpaceOnDisk"));
                }
            } else {
                addWarning(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("DPKGPackageManager.downloadPackages.fileSizeNull", "No enry found for DPKGPackageManager.downloadPackages.fileSizeNull"));
                logger.error(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("DPKGPackageManager.downloadPackages.fileSizeNull", "No enry found for DPKGPackageManager.downloadPackages.fileSizeNull"));
            }
        }
        downloadable.removeAll(downloadable);
        maxVolumeinByte = 0;
        return ret;
    }

    public boolean update(Collection<Package> oldPacks) throws PackageManagerException {
        boolean ret = false;
        final ArrayList<Package> newPacks = new ArrayList<Package>();
        if (oldPacks.size() > 0) {
            lock.readLock().lock();
            try {
                for (final Package pkg : oldPacks) newPacks.add(availablePackages.getPackage(pkg.getName()));
                lock.readLock().unlock();
                if (downloadPackages(newPacks)) {
                    ret = true;
                    setActprogress(maxProgress);
                    return ret;
                }
            } catch (final IOException e) {
                e.printStackTrace();
                addWarning(e.toString());
                logger.error(e);
            } finally {
            }
        }
        setActprogress(maxProgress);
        setIsDoneTrue();
        return ret;
    }

    /**
	 * 
	 * @param downloadable
	 * @return List of all given packages which are not in the installed database
	 */
    private ArrayList<Package> checkIfFilesAreInDatabase(ArrayList<Package> downloadable) {
        lock.readLock().lock();
        try {
            for (int i = 0; i < downloadable.size(); i++) if (installedPackages.isPackageInstalled(downloadable.get(i).getName())) downloadable.remove(i);
        } finally {
            lock.readLock().unlock();
        }
        return downloadable;
    }

    public boolean install(Collection<Package> installList) throws PackageManagerException {
        boolean ret = false;
        try {
            if (downloadPackages(new ArrayList<Package>(installList))) ret = true;
            installList.removeAll(installList);
            pack.removeAll(pack);
        } catch (final Exception e) {
            e.printStackTrace();
            addWarning(e.toString());
            logger.error(e);
        }
        setActprogress(maxProgress);
        setIsDoneTrue();
        return ret;
    }

    public boolean doDelete(Collection<Package> deleteList, int what) throws PackageManagerException {
        String path = "";
        switch(what) {
            case 0:
                path = oldInstallDir;
                break;
            case 1:
                path = installDir;
                break;
        }
        final int packageProg = getMaxProgress() / deleteList.size();
        for (final Package p : deleteList) {
            final int fileProg = packageProg / p.getFileList().size();
            for (final File f : p.getFileList()) {
                if (!new File(path, f.getPath()).delete()) {
                    addWarning(PreferenceStoreHolder.getPreferenceStoreByName("screen").getPreferenceAsString("PackageManagerBean.doNFSmove.couldNotRemove", "No entry found for PackageManagerBean.doNFSmove.couldNotRemove") + " " + new File(path, f.getPath()).getAbsolutePath());
                    logger.error(PreferenceStoreHolder.getPreferenceStoreByName("screen").getPreferenceAsString("PackageManagerBean.doNFSmove.couldNotRemove", "No entry found for PackageManagerBean.doNFSmove.couldNotRemove") + " " + new File(path, f.getPath()).getAbsolutePath());
                }
                setActprogress(getActprogress() + fileProg);
            }
            final List<File> directories = new ArrayList<File>(p.getDirectoryList());
            Collections.sort(directories);
            Collections.reverse(directories);
            for (final File dir : directories) if (new File(path, dir.getPath()).exists() && new File(path, dir.getPath()).isDirectory() && new File(path, dir.getPath()).listFiles().length == 0) if (!new File(path, dir.getPath()).delete()) {
                addWarning(PreferenceStoreHolder.getPreferenceStoreByName("screen").getPreferenceAsString("PackageManagerBean.doNFSmove.couldNotRemove", "No entry found for PackageManagerBean.doNFSmove.couldNotRemove") + " " + new File(path, dir.getPath()).getAbsolutePath());
                logger.error(PreferenceStoreHolder.getPreferenceStoreByName("screen").getPreferenceAsString("PackageManagerBean.doNFSmove.couldNotRemove", "No entry found for PackageManagerBean.doNFSmove.couldNotRemove") + " " + new File(path, dir.getPath()).getAbsolutePath());
            }
        }
        setActprogress(getMaxProgress());
        return true;
    }

    @SuppressWarnings("unchecked")
    public Collection<Package> getAlreadyDeletedPackages() {
        if (!new File(oldInstallDir).isDirectory()) return Collections.EMPTY_LIST;
        lock.readLock().lock();
        try {
            if (removedDB.getPackages() == null || removedDB.getPackages().size() == 0) return Collections.EMPTY_LIST; else {
                final ArrayList<Package> deletePackages = new ArrayList<Package>(removedDB.getPackages());
                return deletePackages;
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean deleteOldPackages(Collection<Package> deleteList) throws PackageManagerException {
        return doDelete(deleteList, OLDINSTALLDIR_FOR_DELETE);
    }

    public boolean realyDelete(Collection<File> directory) {
        boolean ret = true;
        final ArrayList<File> otherDirectories = new ArrayList<File>();
        for (final File file : directory) {
            if (file.isFile()) if (!file.delete()) ret = false;
            if (file.isDirectory()) if (file.listFiles().length == 0) file.delete(); else {
                for (final File fi : file.listFiles()) otherDirectories.add(fi);
                otherDirectories.add(file);
            }
        }
        if (!otherDirectories.isEmpty()) realyDelete(otherDirectories);
        otherDirectories.removeAll(otherDirectories);
        return ret;
    }

    public long getFreeDiskSpace() throws PackageManagerException {
        try {
            return FileSystemUtils.freeSpaceKb(installDir);
        } catch (final IOException io) {
            io.printStackTrace();
            addWarning(io.toString());
            logger.error(io);
            return 0;
        }
    }

    public boolean deleteDebianPackages(Collection<Package> deleteList) {
        boolean ret = true;
        final int multiplier = maxProgress / deleteList.size();
        int i = 1;
        lock.writeLock().lock();
        try {
            for (final Package pkg : deleteList) {
                if (!new File(archivesDir, pkg.getFilename()).delete() || !archivesDB.removePackage(pkg)) ret = false;
                setActprogress(i * multiplier);
                i++;
            }
        } finally {
            lock.writeLock().unlock();
        }
        setActprogress(maxProgress);
        setIsDoneTrue();
        return ret;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<String> getChangelogFile(Package p) throws IOException {
        final String name = (new File((new StringBuilder()).append(listsDir).append(File.separator).append(p.getChangelogDir()).toString(), (new StringBuilder()).append(p.getName()).append(".changelog").toString())).getCanonicalPath();
        if (!new File(name).isFile()) return new ArrayList<String>(Collections.EMPTY_LIST);
        InputStream stream = null;
        BufferedReader br = null;
        if ((new File(name)).length() != 0L) stream = new FileInputStream(name);
        if (stream == null) {
            final ClassLoader aClassLoader = PreferenceStoreHolder.class.getClassLoader();
            if (aClassLoader == null) stream = ClassLoader.getSystemResourceAsStream(name); else stream = aClassLoader.getResourceAsStream(name);
        }
        if (stream == null) return null;
        br = new BufferedReader(new InputStreamReader(stream));
        final ArrayList<String> lines = new ArrayList<String>();
        String line;
        while ((line = br.readLine()) != null) lines.add(line);
        br.close();
        stream.close();
        return lines;
    }

    public Collection<Package> solveConflicts(Collection<Package> selectedList) {
        for (final PackagingConflict pkconf : conflicts) selectedList.removeAll(pkconf.pkgs);
        return selectedList;
    }

    private HashMap<File, File> fromToFileMap;

    private List<File> removeDirectoryList;

    private boolean isDone = false;

    public Collection<Package> filesToRename(Collection<Package> packages) throws PackageManagerException {
        fromToFileMap = new HashMap<File, File>();
        removeDirectoryList = new ArrayList<File>();
        final ArrayList<Package> remove = new ArrayList<Package>();
        for (final Package pkg : packages) remove.add((Package) DeepObjectCopy.clone(pkg, this));
        String newDirName = null;
        if (!new File(oldInstallDir).isDirectory()) new File(oldInstallDir).mkdirs();
        lock.readLock().lock();
        try {
            for (int x = 0; x < remove.size(); x++) {
                final Package pack = installedPackages.getPackage(remove.get(x).getName());
                remove.remove(x);
                remove.add(x, pack);
            }
        } finally {
            lock.readLock().unlock();
        }
        for (int i = 0; i < remove.size(); i++) {
            final String dateForFolder = getFormattedDate();
            newDirName = new File(dateForFolder + "#" + remove.get(i).getName() + "#" + remove.get(i).getVersion().toString().replaceAll(":", "_").replaceAll("\\.", "_")).getPath();
            final String newDirNamePath = oldInstallDir + File.separator + newDirName;
            if (!new File(newDirNamePath).mkdir()) {
                addWarning(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("DPKGPackageManager.getDebianFilePackages.unableToCreateDir", "NO entry for DPKGPackageManager.getDebianFilePackages.unableToCreateDir"));
                logger.error(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("DPKGPackageManager.getDebianFilePackages.unableToCreateDir", "NO entry for DPKGPackageManager.getDebianFilePackages.unableToCreateDir"));
            }
            remove.get(i).setoldFolder(newDirName);
            for (int z = 0; z < remove.get(i).getFileList().size(); z++) {
                final String fi = remove.get(i).getFileList().get(z).getPath();
                if (!new File(installDir, fi).exists()) {
                    final List<File> removeDirs = new ArrayList<File>();
                    for (int n = 0; n < i; n++) {
                        for (final File file : remove.get(n).getDirectoryList()) removeDirs.add(new File(oldInstallDir + File.separator + remove.get(n).getoldFolder(), file.getPath()));
                        removeDirs.add(new File(oldInstallDir + File.separator + remove.get(n).getoldFolder()));
                    }
                    removeDirs.add(new File(newDirNamePath));
                    Collections.sort(removeDirs);
                    Collections.reverse(removeDirs);
                    for (final File file : removeDirs) if (!file.delete()) {
                        addWarning(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("DPKGPackageManager.filesToRename.notExisting", "No entry found for DPKGPackageManager.filesToRename.notExisting") + " \n" + new File(installDir, fi).getPath());
                        logger.error(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("DPKGPackageManager.filesToRename.notExisting", "No entry found for DPKGPackageManager.filesToRename.notExisting") + " \n" + new File(installDir, fi).getPath());
                    }
                }
                if (new File(installDir, fi).isFile()) fromToFileMap.put(new File(installDir, fi), new File(newDirNamePath, fi));
            }
            for (final File f : remove.get(i).getDirectoryList()) {
                new File(newDirNamePath, f.getPath()).mkdirs();
                if (!removeDirectoryList.contains(f)) removeDirectoryList.add(f);
            }
        }
        return remove;
    }

    public HashMap<File, File> getFromToFileMap() {
        return fromToFileMap;
    }

    public boolean saveChangesInDB(Collection<Package> remove) throws IOException, PackageManagerException {
        final boolean ret = true;
        lock.writeLock().lock();
        try {
            for (final Package pkg : remove) if (installedPackages.removePackage(pkg)) {
                final List<File> directoryList = new ArrayList<File>();
                final List<File> fileList = new ArrayList<File>();
                for (final File file : pkg.getDirectoryList()) directoryList.add(new File(new File(pkg.getoldFolder()), file.getPath()));
                for (final File file : pkg.getFileList()) fileList.add(new File(new File(pkg.getoldFolder()), file.getPath()));
                directoryList.add(new File(pkg.getoldFolder()));
                pkg.setDirectoryList(directoryList);
                pkg.setFileList(fileList);
                pkg.setName(new File(pkg.getoldFolder()).getName());
                removedDB.addPackage(pkg);
                removedDB.save();
                installedPackages.save();
            } else {
                addWarning(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("DPKGPackageManager.saveChangesInDB.unableToRemove", "No entry found for DPKGPackageManager.saveChangesInDB.unableToRemove"));
                logger.error(PreferenceStoreHolder.getPreferenceStoreByName("Screen").getPreferenceAsString("DPKGPackageManager.saveChangesInDB.unableToRemove", "No entry found for DPKGPackageManager.saveChangesInDB.unableToRemove"));
            }
        } finally {
            lock.writeLock().unlock();
        }
        return ret;
    }

    public Collection<Package> checkIfPackageMangerIsIn(Collection<Package> deleteList) {
        Package packageManager = null;
        for (final Package pkg : deleteList) if (pkg.isPackageManager()) packageManager = pkg;
        if (packageManager != null) deleteList.remove(packageManager);
        return deleteList;
    }

    public boolean deleteDirectories(List<File> directorysToDelete) {
        while (directorysToDelete.size() > 0) if (directorysToDelete.get(0).isDirectory()) {
            if (directorysToDelete.get(0).listFiles().length == 0 && directorysToDelete.get(0).delete() || directorysToDelete.get(0).listFiles().length > 0) directorysToDelete.remove(0); else return false;
        } else return false;
        return true;
    }

    /**
	 * finds out the packages in the installed databasewhich are depending on the
	 * given Collection of packages
	 * 
	 * @param packList
	 * @return a package list of dependencies
	 */
    private List<Package> getDependencyOf(Collection<Package> packList) {
        boolean anotherDepends = false;
        final ArrayList<Package> remove = new ArrayList<Package>();
        lock.readLock().lock();
        try {
            for (final Package p1 : packList) for (final Package p2 : installedPackages.getDependency(p1)) remove.add(p2);
        } finally {
            lock.readLock().unlock();
        }
        if (packList.size() > 0) for (final Package p1 : packList) {
            boolean check = false;
            for (final Package p2 : packForDelete) if (p1.getName().equals(p2.getName())) check = true;
            if (!check) {
                packForDelete.add(p1);
                anotherDepends = true;
            }
        }
        if (anotherDepends) {
            getDependencyOf(remove);
            return packForDelete;
        } else {
            packList.removeAll(packList);
            return packForDelete;
        }
    }

    public Collection<File> getRemoveDBFileList(String packName) {
        lock.readLock().lock();
        try {
            final List<File> fileListAbsolute = new ArrayList<File>();
            for (final File f : removedDB.getPackage(packName).getFileList()) fileListAbsolute.add(new File(oldInstallDir, f.getPath()));
            return fileListAbsolute;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Collection<File> getRemoveDBDirList(String packName) {
        lock.readLock().lock();
        try {
            final List<File> dirListAbsolute = new ArrayList<File>();
            for (final File d : removedDB.getPackage(packName).getDirectoryList()) dirListAbsolute.add(new File(oldInstallDir, d.getPath()));
            return dirListAbsolute;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean removePackagesFromRemovedDB(List<Package> removeList) throws PackageManagerException {
        lock.writeLock().lock();
        try {
            for (final Package pkg : removeList) if (!removedDB.removePackage(pkg)) removedDB.save();
        } catch (final IOException e) {
            e.printStackTrace();
            addWarning(e.toString());
            logger.error(e);
        } finally {
            lock.writeLock().unlock();
        }
        return true;
    }

    public boolean removePackagesFromInstalledDB(List<Package> removeList) throws PackageManagerException {
        lock.writeLock().lock();
        try {
            for (final Package pkg : removeList) if (!installedPackages.removePackage(pkg)) return false;
            installedPackages.save();
        } catch (final IOException e) {
            this.addWarning(e.toString());
            logger.error(e);
        } finally {
            lock.writeLock().unlock();
        }
        return true;
    }

    public List<File> getRemoveDirectoryList() {
        return removeDirectoryList;
    }

    public int getActprogress() {
        return actprogress;
    }

    public void setActprogress(int actprogress) {
        this.actprogress = actprogress;
    }

    public boolean isDone() {
        return isDone;
    }

    public void refreshIsDone() {
        isDone = false;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public void setIsDoneTrue() {
        isDone = true;
    }

    public long getMaxVolumeinByte() {
        return maxVolumeinByte;
    }

    private int actually;

    private int maxFile;

    public String actPackName;

    public void setActprogressPlusX(int actprogress, int actually, int maxFile, String Name) {
        this.maxFile = maxFile;
        this.actually = actually;
        this.actPackName = Name;
        this.actprogress = actprogress;
    }

    public int[] getActMaxFileSize() {
        return new int[] { actually, maxFile };
    }

    public String getActPackName() {
        return this.actPackName;
    }

    public void resetValuesForDisplaying() {
        this.actPackName = null;
        this.actually = 0;
        this.maxFile = 0;
        refreshIsDone();
        setActprogress(0);
    }

    public boolean updateCacheDB() throws PackageManagerException {
        lock.writeLock().lock();
        try {
            setActprogress(new Double(getMaxProgress() * 0.1).intValue());
            setActprogress(new Double(getActprogress() + getMaxProgress() * 0.1).intValue());
            availablePackages = new UpdateDatabase().doUpdate(true);
            setActprogress(new Double(getActprogress() + getMaxProgress() * 0.5).intValue());
            availablePackages.save();
            setActprogress(maxProgress);
            setIsDoneTrue();
            return true;
        } catch (final IOException e) {
            e.printStackTrace();
            addWarning(e.toString());
            logger.error(e);
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean delete(Collection<Package> collection) throws IOException, PackageManagerException {
        return doDelete(collection, INSTALLDIR_FOR_DELETE);
    }

    public boolean addWarning(String warning) {
        return warnings.add(warning);
    }

    public LinkedList<String> getWarnings() {
        LinkedList<String> s;
        s = (LinkedList<String>) warnings.clone();
        warnings.clear();
        return s;
    }
}
