package org.gnu.amSpacks.update;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.gnu.amSpacks.DefaultLogger;
import org.gnu.amSpacks.Exclude;
import org.gnu.amSpacks.ILogger;
import org.gnu.amSpacks.exception.AMSpacksException;
import org.gnu.amSpacks.exception.PermissionException;
import org.gnu.amSpacks.exception.UpdateException;
import org.gnu.amSpacks.exception.UpdateSecurityException;
import org.gnu.amSpacks.exception.VersionException;
import org.gnu.amSpacks.exception.runtime.UpdateIOException;
import org.gnu.amSpacks.io.CloseNowZipOutputStream;
import org.gnu.amSpacks.io.NullEofZipInputStream;
import org.gnu.amSpacks.model.UpdateInfo;

/**
 *
 */
public class Updater {

    protected static ILogger log = new DefaultLogger();

    private final File applicationDirectory;

    private File updateJar;

    private final String applicationDirectoryName;

    private ZipInputStream jarInputStream;

    private File uninstallJar;

    private CloseNowZipOutputStream uninstallStream;

    private boolean createUninstaller;

    private final byte[] buf = new byte[8000];

    private final ByteArrayOutputStream bentry = new ByteArrayOutputStream(20000);

    /**
	 * needed for uninstaller
	 */
    private final TreeSet<String> newEntries = new TreeSet<String>();

    private final TreeSet<String> deleteThem = new TreeSet<String>();

    public Updater(File _applicationDirectory, File _updateJar) throws UpdateException {
        applicationDirectory = _applicationDirectory;
        applicationDirectoryName = applicationDirectory.getAbsolutePath();
        updateJar = _updateJar;
        try {
            jarInputStream = new NullEofZipInputStream(new BufferedInputStream(new FileInputStream(updateJar)));
        } catch (FileNotFoundException e) {
            throw new UpdateException("Update archive not found: " + updateJar.getAbsolutePath(), e);
        }
        createUninstaller = true;
    }

    public Updater(File applicationDir) {
        applicationDirectory = applicationDir;
        applicationDirectoryName = applicationDirectory.getAbsolutePath();
    }

    public void run() throws VersionException, PermissionException {
        log.log("Checking version information");
        UpdateInfo info = Utils.verifyVersion(updateJar, applicationDirectory);
        if (info.from.compareTo(info.to) < 0) {
            log.log("Upgrading from " + info.from + " to " + info.to);
        } else {
            log.log("Downgrading from " + info.from + " back to " + info.to);
        }
        log.log("Checking write permissions");
        checkPermissions();
        if (createUninstaller) {
            log.log("Creating uninstall folder");
            uninstallJar = new File(applicationDirectory, "autooutdate");
            uninstallJar.mkdirs();
            String verName = info.to.getCleanName() + "_to_" + info.from.getCleanName();
            uninstallJar = new File(uninstallJar, verName + ".jar");
            log.log("Creating uninstaller:  " + uninstallJar.getAbsolutePath());
            try {
                uninstallStream = new CloseNowZipOutputStream(new FileOutputStream(uninstallJar));
            } catch (FileNotFoundException e) {
                throw new UpdateIOException("Unable to create uninstaller.", e);
            }
        } else {
            log.log("Uninstaller was not requested.");
        }
        deleteThem.clear();
        deleteThem.addAll(info.deleteFiles);
        deleteSingleFiles();
        log.log("Updating " + applicationDirectoryName + " from " + updateJar.getAbsolutePath());
        while (true) {
            ZipEntry en = null;
            try {
                en = jarInputStream.getNextEntry();
            } catch (IOException e) {
                throw new UpdateIOException("Error reading from udpate archive.", e);
            }
            if (en == null) {
                break;
            }
            updateEntry(en);
        }
        deleteWhereNoneAdded();
        if (createUninstaller) {
            try {
                UpdateInfo uninfo = new UpdateInfo();
                uninfo.from = info.to;
                uninfo.to = info.from;
                uninfo.setDelete(newEntries);
                uninfo.writeTo(uninstallStream);
                uninstallStream.closeNOW();
            } catch (IOException e) {
                throw new UpdateIOException("Error writing to uninstaller.", e);
            }
        }
        finished();
        log.log(updateJar.getAbsolutePath() + " will be deleted on exit.");
        updateJar.deleteOnExit();
    }

    /** Store an uninstall backup of the file f */
    private void backup(File file) {
        if (!createUninstaller) return;
        String relative = file.getAbsolutePath().substring(applicationDirectoryName.length());
        log.log("backup for " + file.getAbsolutePath());
        if (!file.exists()) {
            newEntries.add(relative);
        } else {
            try {
                FileInputStream in = new FileInputStream(file);
                ZipEntry ze = new ZipEntry(relative);
                ze.setSize(file.length());
                ze.setCompressedSize(-1);
                uninstallStream.putNextEntry(ze);
                int n;
                while (true) {
                    n = in.read(buf);
                    if (n <= 0) {
                        break;
                    }
                    uninstallStream.write(buf, 0, n);
                }
                in.close();
                uninstallStream.closeEntry();
                uninstallStream.flush();
            } catch (IOException e) {
                throw new UpdateIOException("Error creating backup entry for " + file.getAbsolutePath(), e);
            }
        }
    }

    protected void finished() {
        try {
            jarInputStream.close();
        } catch (IOException ex) {
        }
        System.gc();
    }

    /** Delete single files. */
    private void deleteSingleFiles() {
        log.log(" Deleting single files.");
        Iterator<String> iter = deleteThem.iterator();
        String file;
        while (iter.hasNext()) {
            {
                file = iter.next();
                if (file.indexOf('!') < 0) {
                    log.log("Deleting " + file);
                    File deleteFile = new File(applicationDirectory, file);
                    if (deleteFile.exists()) {
                        backup(deleteFile);
                    }
                    try {
                        checkSecurity(deleteFile);
                        if (deleteFile.exists() && !Exclude.matches(deleteFile)) {
                            boolean deleted = deleteFile.delete();
                            if (!deleted || deleteFile.exists()) {
                                log.log("Unable to delete " + deleteFile.getAbsolutePath());
                            }
                        } else {
                            iter.remove();
                        }
                    } catch (UpdateSecurityException e) {
                        log.log(e.getMessage());
                    }
                }
            }
        }
    }

    private final void checkSecurity(File file) throws UpdateSecurityException {
        if (!file.getAbsolutePath().startsWith(applicationDirectoryName)) {
            throw new UpdateSecurityException("Attempt to modify file outside the installation scope.", file);
        }
    }

    /** Delete items from archives where no new files were added. */
    private void deleteWhereNoneAdded() {
        if (deleteThem.size() == 0) return;
        TreeSet<String> additionallyProcess = new TreeSet<String>();
        Iterator<String> iter = deleteThem.iterator();
        String item;
        String archive;
        int p;
        while (iter.hasNext()) {
            item = iter.next();
            p = item.indexOf('!');
            if (p > 0) {
                archive = item.substring(0, p);
                additionallyProcess.add(archive);
            }
        }
        iter = additionallyProcess.iterator();
        while (iter.hasNext()) {
            archive = iter.next();
            updateArchive(new File(applicationDirectory, archive));
        }
    }

    private void updateEntry(ZipEntry e) {
        File existing = new File(applicationDirectory, e.getName());
        String name = existing.getName().toLowerCase();
        if (name.endsWith(".jar") || name.endsWith(".zip")) {
            updateArchive(existing);
        } else {
            updateSimple(existing);
        }
    }

    private void updateSimple(File f) {
        if (f.getAbsolutePath().endsWith(".aupService.xml") || Exclude.matches(f)) {
            log.log("Skipping " + f.getAbsolutePath());
            return;
        }
        try {
            checkSecurity(f);
        } catch (UpdateSecurityException e) {
            log.log(e.getMessage());
            return;
        }
        if (f.exists()) {
            backup(f);
            log.log("Replacing " + f.getAbsolutePath());
            f.delete();
            if (f.exists()) {
                log.log("CANNOT DELETE " + f.getAbsolutePath());
            }
        }
        log.log("Adding " + f.getAbsolutePath());
        newEntries.add(f.getAbsolutePath().substring(applicationDirectoryName.length()));
        if (!f.getParentFile().exists()) {
            log.log("Creating folder " + f.getParent());
            f.getParentFile().mkdirs();
        }
        InputStream in = jarInputStream;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(f);
            int n;
            while (true) {
                n = in.read(buf);
                if (n <= 0) {
                    break;
                }
                out.write(buf, 0, n);
            }
            out.flush();
        } catch (IOException e) {
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new UpdateIOException("Error closing stream.", e);
                }
            }
        }
    }

    private void updateArchive(File f) {
        if (Exclude.matches(f)) return;
        try {
            checkSecurity(f);
        } catch (UpdateSecurityException e) {
            log.log(e.getMessage());
            return;
        }
        if (!f.exists()) {
            updateSimple(f);
            return;
        }
        log.log("Updating archive " + f.getAbsolutePath());
        File ftmp = new File(f.getAbsolutePath() + ".tmp");
        if (ftmp.exists()) {
            ftmp.delete();
        }
        boolean renamed = f.renameTo(ftmp);
        if (!renamed) throw new UpdateIOException("Aborting update. Unable to modify archive " + f.getAbsolutePath());
        TreeSet<String> changed = new TreeSet<String>();
        ZipInputStream in = new NullEofZipInputStream(jarInputStream);
        ZipOutputStream out;
        try {
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
            while (true) {
                ZipEntry e = in.getNextEntry();
                if (e == null) {
                    break;
                }
                changed.add(e.getName());
                log.log("    replacing " + e.getName());
                bentry.reset();
                int bb;
                while (true) {
                    bb = in.read();
                    if (bb < 0) {
                        break;
                    } else {
                        bentry.write(bb);
                    }
                }
                bentry.close();
                e.setSize(bentry.size());
                out.putNextEntry(e);
                bentry.writeTo(out);
                out.closeEntry();
            }
            log.log("Putting the rest of contents");
            TreeSet<String> enew = new TreeSet<String>();
            enew.addAll(changed);
            Iterator<String> iter = deleteThem.iterator();
            String item = f.getAbsolutePath();
            String this_archive = item.substring(applicationDirectoryName.length());
            TreeSet<String> deleted = new TreeSet<String>();
            while (iter.hasNext()) {
                item = iter.next();
                int p = item.indexOf('!');
                if (p > 0) {
                    String archive = item.substring(0, p);
                    if (archive.equals(this_archive)) {
                        String path = item.substring(p + 1);
                        deleted.add(path);
                        iter.remove();
                    }
                }
            }
            in = new NullEofZipInputStream(new BufferedInputStream(new FileInputStream(ftmp)));
            ZipEntry be = new ZipEntry(f.getAbsolutePath().substring(applicationDirectoryName.length()));
            ZipOutputStream bos = null;
            while (true) {
                ZipEntry e = in.getNextEntry();
                if (e == null) {
                    break;
                }
                String ename = e.getName();
                enew.remove(ename);
                if (!changed.contains(ename) && !deleted.contains(ename)) {
                    store(in, out, e);
                } else {
                    if (!deleted.contains(ename)) {
                        log.log("    discarding replaced " + ename);
                    } else {
                        log.log("    deleting " + ename);
                    }
                    if (createUninstaller) {
                        if (bos == null) {
                            bos = new ZipOutputStream(uninstallStream);
                            uninstallStream.putNextEntry(be);
                        }
                        store(in, bos, e);
                    }
                }
            }
            in.close();
            out.flush();
            out.close();
            if (bos != null) {
                assert createUninstaller;
                bos.flush();
                bos.close();
                uninstallStream.closeEntry();
            }
            log.log("Deleting " + ftmp.getAbsolutePath());
            ftmp.deleteOnExit();
            iter = enew.iterator();
            while (iter.hasNext()) {
                item = this_archive + "!" + iter.next();
                newEntries.add(item);
            }
        } catch (IOException e1) {
            throw new UpdateIOException("Error updating archive " + f.getAbsolutePath(), e1);
        }
    }

    private void store(ZipInputStream in, ZipOutputStream bos, ZipEntry e) {
        try {
            bentry.reset();
            int bb;
            while (true) {
                bb = in.read();
                if (bb < 0) {
                    break;
                } else {
                    bentry.write(bb);
                }
            }
            bentry.close();
            e = new ZipEntry(e);
            e.setSize(bentry.size());
            e.setCompressedSize(-1);
            bos.putNextEntry(e);
            bentry.writeTo(bos);
            bos.closeEntry();
        } catch (IOException ex) {
            throw new UpdateIOException("Cannot store " + e.getName(), ex);
        }
    }

    private void checkPermissions() throws PermissionException {
        boolean ok = true;
        while (true) {
            ZipEntry en = null;
            try {
                en = jarInputStream.getNextEntry();
            } catch (IOException e) {
                throw new UpdateIOException("Error checking permissions.", e);
            }
            if (en == null) {
                break;
            }
            File existing = new File(applicationDirectory, en.getName());
            if (existing.exists()) if (!existing.isDirectory()) {
                if (Exclude.matches(existing)) {
                    ok = true;
                } else {
                    if (!existing.canWrite()) {
                        ok = false;
                    } else {
                        ok = true;
                        File nn = new File(existing.getAbsolutePath() + ".galiu_rasyti");
                        if (!existing.renameTo(nn)) {
                            ok = false;
                        } else {
                            nn.renameTo(existing);
                            ok = true;
                        }
                    }
                }
            }
            if (!ok) {
                log.log("cannot write " + existing.getAbsolutePath());
                if (existing.getParentFile().getName().equals("autooutdate")) {
                    log.log("This is in a backup information folder, process continues.");
                } else {
                    log.log("Update aborted.");
                    throw new PermissionException("Updater could not write this file", existing);
                }
            }
        }
        resetInputJar();
    }

    private void resetInputJar() {
        try {
            jarInputStream.close();
            jarInputStream = new NullEofZipInputStream(new BufferedInputStream(new FileInputStream(updateJar)));
        } catch (IOException ex) {
            throw new UpdateIOException("Unable to reset update archive.", ex);
        }
    }

    public boolean isCreateUninstaller() {
        return createUninstaller;
    }

    public void setCreateUninstaller(boolean createUninstaller) {
        this.createUninstaller = createUninstaller;
    }

    /**
	 * Uninstall the latest update.
	 *
	 * @throws Exception
	 */
    public void revert() throws AMSpacksException {
        this.updateJar = Utils.getUninstallJar(applicationDirectory);
        try {
            jarInputStream = new NullEofZipInputStream(new BufferedInputStream(new FileInputStream(updateJar)));
        } catch (FileNotFoundException e) {
            throw new UpdateException("Update archive not found: " + updateJar.getAbsolutePath(), e);
        }
        createUninstaller = false;
        run();
    }

    /**
	 * @param log
	 *            the log to set
	 */
    public static void setLog(ILogger log) {
        Updater.log = log;
    }

    public static void main(String[] args) {
        Updater g = null;
        try {
            if (args.length == 2) {
                File f_old = new File(args[0]);
                File f_jar = new File(args[1]);
                if (f_old.exists() && f_jar.exists()) {
                    if (f_old.isDirectory()) {
                        g = new Updater(f_old, f_jar);
                        g.run();
                        g.finished();
                        return;
                    }
                }
            } else {
                System.out.println("required parameters: old_version_folder updateFile.jar");
            }
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }
    }
}
