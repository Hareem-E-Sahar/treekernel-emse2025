package home.jes.db.rhumba;

import home.jes.util.Logger;
import home.jes.util.FileUtil;
import java.io.*;
import java.util.List;
import java.util.Iterator;
import java.util.Properties;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: john
 * Date: Apr 25, 2003
 * Time: 2:25:36 PM
 * To change this template use Options | File Templates.
 */
public abstract class RHDatabase {

    public RHDatabase(File dir) {
        vDir = null;
        this.dir = dir;
        initTables(dir);
    }

    protected void initTables(File dir) {
        propfile = new File(dir, "rhumba.def");
        props = new Properties();
        tables = new ArrayList();
    }

    public void open() throws Exception {
        open(false);
    }

    public void open(boolean readonly) throws Exception {
        this.readonly = readonly;
        loadDefinition();
        Iterator itr = tables.iterator();
        while (itr.hasNext()) {
            RHTable table = (RHTable) itr.next();
            table.open(readonly);
        }
        openFlag = true;
    }

    public void close() {
        saveDefinition();
        Iterator itr = tables.iterator();
        while (itr.hasNext()) {
            RHTable table = (RHTable) itr.next();
            table.close();
        }
        openFlag = false;
    }

    public void create(String name, String desc, String version) throws Exception {
        props.setProperty("rhumba.name", name);
        props.setProperty("rhumba.description", desc);
        props.setProperty("rhumba.version", version);
        props.setProperty("rhumba.versioning", "0");
        props.setProperty("rhumba.maxversions", "5");
        props.setProperty("rhumba.versiondir", new File(dir, "versions").toString());
        saveDefinition();
        Iterator itr = tables.iterator();
        while (itr.hasNext()) {
            RHTable table = (RHTable) itr.next();
            table.create();
        }
    }

    public boolean exists() {
        if (propfile.exists() == false) {
            return false;
        }
        Iterator itr = tables.iterator();
        while (itr.hasNext()) {
            RHTable table = (RHTable) itr.next();
            if (!table.exists()) {
                return false;
            }
        }
        return true;
    }

    public void compress() throws Exception {
        int tmpVersionMask = versionMask;
        if ((versionMask & COMPRESS) > 0) {
            makeVersion("compressing database '" + getDBName() + "'");
            versionMask = 0;
        }
        try {
            Iterator itr = tables.iterator();
            while (itr.hasNext()) {
                RHTable table = (RHTable) itr.next();
                table.compress();
            }
        } finally {
            versionMask = tmpVersionMask;
        }
    }

    public String getDBName() {
        return props.getProperty("rhumba.name");
    }

    public File getDBDir() {
        if (vDir != null) {
            return vDir;
        } else {
            return dir;
        }
    }

    public boolean isOpen() {
        return openFlag;
    }

    public int getVersioningMask() {
        return versionMask;
    }

    public void setVersioningMask(int mask) {
        versionMask = mask;
        props.setProperty("rhumba.versioning", Integer.toString(mask));
        saveDefinition();
    }

    public int getMaxVersions() {
        return maxVersions;
    }

    public void setMaxVesions(int max) {
        maxVersions = max;
        props.setProperty("rhumba.maxversions", Integer.toString(max));
        saveDefinition();
    }

    public File getVersionDir() {
        return versionDir;
    }

    public void setVersionDir(File vdir) {
        versionDir = vdir;
        props.setProperty("rhumba.versiondir", vdir.getAbsolutePath());
        saveDefinition();
    }

    public List getVersions() {
        ArrayList versions = new ArrayList();
        if (versionDir.exists()) {
            File s[] = versionDir.listFiles(new FileFilter() {

                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            for (int i = 0; i < s.length; i++) {
                RHDatabaseVersion v = getVersion(s[i]);
                if (v != null) {
                    versions.add(v);
                }
            }
        }
        return versions;
    }

    private RHDatabaseVersion getVersion(File vdir) {
        File def = new File(vdir, "rhumba.def");
        if (!def.exists()) {
            return null;
        }
        try {
            FileInputStream f = new FileInputStream(def);
            Properties p = new Properties();
            p.load(f);
            return new RHDatabaseVersion(Integer.parseInt(vdir.getName()), p.getProperty("rhumba.versioncomment", "no version info available"), def.lastModified());
        } catch (IOException e) {
            Logger.logError("RHDatabase.getVersion", "Exception getting version information in directory " + vdir + " : " + e.getMessage());
            return null;
        }
    }

    public void makeVersion(String comment) {
        if (vDir != null) {
            Logger.logWarning("RHDatabase.makeVersion", "Cannot make a version of a version");
            return;
        }
        if (!versionDir.exists()) {
            versionDir.mkdir();
        }
        int max = 0;
        int min = Integer.MAX_VALUE;
        int count = 0;
        File s[] = versionDir.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        int j;
        for (int i = 0; i < s.length; i++) {
            try {
                j = Integer.parseInt(s[i].getName());
            } catch (Throwable t) {
                continue;
            }
            if (j > max) {
                max = j;
            }
            if (j < min) {
                min = j;
            }
            count++;
        }
        max++;
        File newVersionDir = new File(versionDir, "" + max);
        newVersionDir.mkdir();
        String origName = getDBName();
        props.setProperty("rhumba.versioncomment", comment);
        props.setProperty("rhumba.versioning", "0");
        props.setProperty("rhumba.name", "version " + max + " of " + origName);
        saveDefinition();
        try {
            copyDatabase(newVersionDir, false);
        } catch (IOException e) {
        } finally {
            props.setProperty("rhumba.name", origName);
            props.setProperty("rhumba.versioning", Integer.toString(versionMask));
            props.remove("rhumba.versioncomment");
            saveDefinition();
        }
        if (count >= maxVersions) {
            File killme = new File(versionDir, new Integer(min).toString());
            if (!FileUtil.deleteDir(killme)) {
                Logger.logWarning("RHDatabase.makeVersion", "unable to delete old version: " + killme.getAbsolutePath());
            }
        }
    }

    public void openVersion(int version) throws Exception {
        if (!versionDir.exists()) {
            throw new Exception("version " + version + " does not exist");
        }
        File dir = new File(versionDir, "" + version);
        if (!dir.exists()) {
            throw new Exception("version " + version + " does not exist");
        }
        if (isOpen()) {
            close();
        }
        vDir = dir;
        initTables(vDir);
        open(true);
    }

    public void removeVersion(int version) throws Exception {
        Logger.logInfo("RHDatabase.removeVersion", "removing version " + version);
        if (!versionDir.exists()) {
            throw new Exception("version " + version + " does not exist");
        }
        File dir = new File(versionDir, "" + version);
        if (!dir.exists()) {
            throw new Exception("version " + version + " does not exist");
        }
        if (vDir != null) {
            int v = Integer.parseInt(vDir.getName());
            if (v == version) {
                throw new Exception("Cannot remove active version " + version);
            }
        }
        FileUtil.deleteDir(dir);
    }

    public void resetMainDB() {
        if (vDir == null) {
            return;
        }
        boolean wasOpen = false;
        if (isOpen()) {
            wasOpen = true;
            close();
        }
        vDir = null;
        initTables(dir);
        if (wasOpen) {
            try {
                open();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isVersion() {
        return vDir != null;
    }

    public void copyVersion(int version, File newLocation) {
    }

    public void blessVersion(int version) throws Exception {
        if (!versionDir.exists()) {
            throw new Exception("version " + version + " does not exist");
        }
        File vdir = new File(versionDir, "" + version);
        if (!vdir.exists()) {
            throw new Exception("version " + version + " does not exist");
        }
        boolean wasOpen = isOpen();
        if (wasOpen) {
            close();
        }
        if (vDir != null) {
            vDir = null;
            initTables(dir);
        }
        File corrupted = new File(dir, "corrupted");
        FileUtil.deleteDir(corrupted);
        corrupted.mkdir();
        copyDatabase(corrupted, false);
        initTables(vdir);
        copyDatabase(dir, false);
        if (wasOpen) {
            ignoreGenFiles = true;
            open(readonly);
            ignoreGenFiles = false;
        }
    }

    public void copyDatabase(File newLocation, boolean includeVersions) throws IOException {
        Iterator tableItr = tables.iterator();
        while (tableItr.hasNext()) {
            RHTable table = (RHTable) tableItr.next();
            Iterator files = table.getFiles().iterator();
            while (files.hasNext()) {
                File f = (File) files.next();
                FileUtil.copy(f, newLocation);
            }
        }
        FileUtil.copy(propfile, newLocation);
        if (includeVersions) {
        }
    }

    private void saveDefinition() {
        try {
            FileOutputStream ostream = new FileOutputStream(propfile);
            props.store(ostream, "Rhumba database definition version " + VERSION);
            ostream.close();
        } catch (Exception e) {
            Logger.logError("RHDatabase.saveDefinition", "Exception saving database definition: " + e.getMessage());
        }
    }

    private void loadDefinition() throws Exception {
        FileInputStream istream = new FileInputStream(propfile);
        props.load(istream);
        istream.close();
        versionMask = Integer.parseInt(props.getProperty("rhumba.versioning", "0"));
        maxVersions = Integer.parseInt(props.getProperty("rhumba.maxversions", "5"));
        versionDir = new File(props.getProperty("rhumba.versiondir", dir.getAbsolutePath() + File.separatorChar + "versions"));
    }

    private boolean openFlag = false;

    protected File dir;

    protected File vDir;

    protected File propfile;

    protected List tables;

    protected Properties props;

    private int maxVersions = 5;

    private File versionDir;

    private int versionMask = 0;

    public static final int COMPRESS = 0x01;

    public static final int DELETE = 0x02;

    public static final int UNDELETE = 0x04;

    public static final int UPDATE = 0x08;

    public static final int SAVE = 0x10;

    public static final int BLESS = 0x20;

    private static final String VERSION = "1.0";

    private boolean readonly;

    boolean ignoreGenFiles = false;
}
