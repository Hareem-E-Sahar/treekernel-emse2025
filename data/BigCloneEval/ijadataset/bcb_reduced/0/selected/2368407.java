package org.python.core.packagecache;

import org.python.core.Options;
import org.python.util.Generic;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessControlException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Abstract package manager that gathers info about statically known classes
 * from a set of jars. This info can be eventually cached. Off-the-shelf this
 * class offers a local file-system based cache impl.
 */
public abstract class CachedJarsPackageManager extends PackageManager {

    /**
     * Message log method - hook. This default impl does nothing.
     *
     * @param msg message text
     */
    protected void message(String msg) {
    }

    /**
     * Warning log method - hook. This default impl does nothing.
     *
     * @param warn warning text
     */
    protected void warning(String warn) {
    }

    /**
     * Comment log method - hook. This default impl does nothing.
     *
     * @param msg message text
     */
    protected void comment(String msg) {
    }

    /**
     * Debug log method - hook. This default impl does nothing.
     *
     * @param msg message text
     */
    protected void debug(String msg) {
    }

    /**
     * Filter class/pkg by name helper method - hook. The default impl. is used
     * by {@link #addJarToPackages} in order to filter out classes whose name
     * contains '$' (e.g. inner classes,...). Should be used or overriden by
     * derived classes too. Also to be used in {@link #doDir}.
     *
     * @param name class/pkg name
     * @param pkg if true, name refers to a pkg
     * @return true if name must be filtered out
     */
    protected boolean filterByName(String name, boolean pkg) {
        return name.indexOf('$') != -1;
    }

    /**
     * Filter class by access perms helper method - hook. The default impl. is
     * used by {@link #addJarToPackages} in order to filter out non-public
     * classes. Should be used or overriden by derived classes too. Also to be
     * used in {@link #doDir}. Access perms can be read with
     * {@link #checkAccess}.
     *
     * @param name class name
     * @param acc class access permissions as int
     * @return true if name must be filtered out
     */
    protected boolean filterByAccess(String name, int acc) {
        return (acc & Modifier.PUBLIC) != Modifier.PUBLIC;
    }

    private boolean indexModified;

    private Map<String, JarXEntry> jarfiles;

    private static String listToString(List<String> list) {
        int n = list.size();
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < n; i++) {
            ret.append(list.get(i));
            if (i < n - 1) {
                ret.append(",");
            }
        }
        return ret.toString();
    }

    private void addZipEntry(Map<String, List<String>[]> zipPackages, ZipEntry entry, ZipInputStream zip) throws IOException {
        String name = entry.getName();
        if (!name.endsWith(".class")) {
            return;
        }
        char sep = '/';
        int breakPoint = name.lastIndexOf(sep);
        if (breakPoint == -1) {
            breakPoint = name.lastIndexOf('\\');
            sep = '\\';
        }
        String packageName;
        if (breakPoint == -1) {
            packageName = "";
        } else {
            packageName = name.substring(0, breakPoint).replace(sep, '.');
        }
        String className = name.substring(breakPoint + 1, name.length() - 6);
        if (filterByName(className, false)) {
            return;
        }
        List<String>[] vec = zipPackages.get(packageName);
        if (vec == null) {
            vec = new List[] { Generic.list(), Generic.list() };
            zipPackages.put(packageName, vec);
        }
        int access = checkAccess(zip);
        if ((access != -1) && !filterByAccess(name, access)) {
            vec[0].add(className);
        } else {
            vec[1].add(className);
        }
    }

    private Map<String, String> getZipPackages(InputStream jarin) throws IOException {
        Map<String, List<String>[]> zipPackages = Generic.map();
        ZipInputStream zip = new ZipInputStream(jarin);
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
            addZipEntry(zipPackages, entry, zip);
            zip.closeEntry();
        }
        Map<String, String> transformed = Generic.map();
        for (Entry<String, List<String>[]> kv : zipPackages.entrySet()) {
            List<String>[] vec = kv.getValue();
            String classes = listToString(vec[0]);
            if (vec[1].size() > 0) {
                classes += '@' + listToString(vec[1]);
            }
            transformed.put(kv.getKey(), classes);
        }
        return transformed;
    }

    /**
     * Gathers classes info from jar specified by jarurl URL. Eventually just
     * using previously cached info. Eventually updated info is not cached.
     * Persistent cache storage access goes through inOpenCacheFile() and
     * outCreateCacheFile().
     */
    public void addJarToPackages(java.net.URL jarurl) {
        addJarToPackages(jarurl, null, false);
    }

    /**
     * Gathers classes info from jar specified by jarurl URL. Eventually just
     * using previously cached info. Eventually updated info is (re-)cached if
     * param cache is true. Persistent cache storage access goes through
     * inOpenCacheFile() and outCreateCacheFile().
     */
    public void addJarToPackages(URL jarurl, boolean cache) {
        addJarToPackages(jarurl, null, cache);
    }

    /**
     * Gathers classes info from jar specified by File jarfile. Eventually just
     * using previously cached info. Eventually updated info is not cached.
     * Persistent cache storage access goes through inOpenCacheFile() and
     * outCreateCacheFile().
     */
    public void addJarToPackages(File jarfile) {
        addJarToPackages(null, jarfile, false);
    }

    /**
     * Gathers classes info from jar specified by File jarfile. Eventually just
     * using previously cached info. Eventually updated info is (re-)cached if
     * param cache is true. Persistent cache storage access goes through
     * inOpenCacheFile() and outCreateCacheFile().
     */
    public void addJarToPackages(File jarfile, boolean cache) {
        addJarToPackages(null, jarfile, cache);
    }

    private void addJarToPackages(URL jarurl, File jarfile, boolean cache) {
        try {
            boolean caching = this.jarfiles != null;
            URLConnection jarconn = null;
            boolean localfile = true;
            if (jarfile == null) {
                jarconn = jarurl.openConnection();
                if (jarconn.getURL().getProtocol().equals("file")) {
                    String jarfilename = jarurl.getFile();
                    jarfilename = jarfilename.replace('/', File.separatorChar);
                    jarfile = new File(jarfilename);
                } else {
                    localfile = false;
                }
            }
            if (localfile && !jarfile.exists()) {
                return;
            }
            Map<String, String> zipPackages = null;
            long mtime = 0;
            String jarcanon = null;
            JarXEntry entry = null;
            boolean brandNew = false;
            if (caching) {
                if (localfile) {
                    mtime = jarfile.lastModified();
                    jarcanon = jarfile.getCanonicalPath();
                } else {
                    mtime = jarconn.getLastModified();
                    jarcanon = jarurl.toString();
                }
                entry = this.jarfiles.get(jarcanon);
                if ((entry == null || !(new File(entry.cachefile).exists())) && cache) {
                    message("processing new jar, '" + jarcanon + "'");
                    String jarname;
                    if (localfile) {
                        jarname = jarfile.getName();
                    } else {
                        jarname = jarurl.getFile();
                        int slash = jarname.lastIndexOf('/');
                        if (slash != -1) jarname = jarname.substring(slash + 1);
                    }
                    jarname = jarname.substring(0, jarname.length() - 4);
                    entry = new JarXEntry(jarname);
                    this.jarfiles.put(jarcanon, entry);
                    brandNew = true;
                }
                if (mtime != 0 && entry != null && entry.mtime == mtime) {
                    zipPackages = readCacheFile(entry, jarcanon);
                }
            }
            if (zipPackages == null) {
                caching = caching && cache;
                if (caching) {
                    this.indexModified = true;
                    if (entry.mtime != 0) {
                        message("processing modified jar, '" + jarcanon + "'");
                    }
                    entry.mtime = mtime;
                }
                InputStream jarin;
                if (jarconn == null) {
                    jarin = new BufferedInputStream(new FileInputStream(jarfile));
                } else {
                    jarin = jarconn.getInputStream();
                }
                zipPackages = getZipPackages(jarin);
                if (caching) {
                    writeCacheFile(entry, jarcanon, zipPackages, brandNew);
                }
            }
            addPackages(zipPackages, jarcanon);
        } catch (IOException ioe) {
            warning("skipping bad jar, '" + (jarfile != null ? jarfile.toString() : jarurl.toString()) + "'");
        }
    }

    private void addPackages(Map<String, String> zipPackages, String jarfile) {
        for (Entry<String, String> entry : zipPackages.entrySet()) {
            String pkg = entry.getKey();
            String classes = entry.getValue();
            int idx = classes.indexOf('@');
            if (idx >= 0 && Options.respectJavaAccessibility) {
                classes = classes.substring(0, idx);
            }
            makeJavaPackage(pkg, classes, jarfile);
        }
    }

    @SuppressWarnings("empty-statement")
    private Map<String, String> readCacheFile(JarXEntry entry, String jarcanon) {
        String cachefile = entry.cachefile;
        long mtime = entry.mtime;
        debug("reading cache, '" + jarcanon + "'");
        try {
            DataInputStream istream = inOpenCacheFile(cachefile);
            String old_jarcanon = istream.readUTF();
            long old_mtime = istream.readLong();
            if ((!old_jarcanon.equals(jarcanon)) || (old_mtime != mtime)) {
                comment("invalid cache file: " + cachefile + ", " + jarcanon + ":" + old_jarcanon + ", " + mtime + ":" + old_mtime);
                deleteCacheFile(cachefile);
                return null;
            }
            Map<String, String> packs = Generic.map();
            try {
                while (true) {
                    String packageName = istream.readUTF();
                    String classes = istream.readUTF();
                    packs.put(packageName, classes);
                }
            } catch (EOFException eof) {
                ;
            }
            istream.close();
            return packs;
        } catch (IOException ioe) {
            return null;
        }
    }

    private void writeCacheFile(JarXEntry entry, String jarcanon, Map<String, String> zipPackages, boolean brandNew) {
        try {
            DataOutputStream ostream = outCreateCacheFile(entry, brandNew);
            ostream.writeUTF(jarcanon);
            ostream.writeLong(entry.mtime);
            comment("rewriting cachefile for '" + jarcanon + "'");
            for (Entry<String, String> kv : zipPackages.entrySet()) {
                String classes = kv.getValue();
                ostream.writeUTF(kv.getKey());
                ostream.writeUTF(classes);
            }
            ostream.close();
        } catch (IOException ioe) {
            warning("can't write cache file for '" + jarcanon + "'");
        }
    }

    /**
     * Initializes cache. Eventually reads back cache index. Index persistent
     * storage is accessed through inOpenIndex().
     */
    protected void initCache() {
        this.indexModified = false;
        this.jarfiles = Generic.map();
        try {
            DataInputStream istream = inOpenIndex();
            if (istream == null) {
                return;
            }
            try {
                while (true) {
                    String jarcanon = istream.readUTF();
                    String cachefile = istream.readUTF();
                    long mtime = istream.readLong();
                    this.jarfiles.put(jarcanon, new JarXEntry(cachefile, mtime));
                }
            } catch (EOFException eof) {
                ;
            }
            istream.close();
        } catch (IOException ioe) {
            warning("invalid index file");
        }
    }

    /**
     * Write back cache index. Index persistent storage is accessed through
     * outOpenIndex().
     */
    public void saveCache() {
        if (jarfiles == null || !indexModified) {
            return;
        }
        indexModified = false;
        comment("writing modified index file");
        try {
            DataOutputStream ostream = outOpenIndex();
            for (Entry<String, JarXEntry> entry : jarfiles.entrySet()) {
                String jarcanon = entry.getKey();
                JarXEntry xentry = entry.getValue();
                ostream.writeUTF(jarcanon);
                ostream.writeUTF(xentry.cachefile);
                ostream.writeLong(xentry.mtime);
            }
            ostream.close();
        } catch (IOException ioe) {
            warning("can't write index file");
        }
    }

    /**
     * To pass a cachefile id by ref. And for internal use. See
     * outCreateCacheFile
     */
    public static class JarXEntry extends Object {

        /** cachefile id */
        public String cachefile;

        public long mtime;

        public JarXEntry(String cachefile) {
            this.cachefile = cachefile;
        }

        public JarXEntry(String cachefile, long mtime) {
            this.cachefile = cachefile;
            this.mtime = mtime;
        }
    }

    /**
     * Open cache index for reading from persistent storage - hook. Must Return
     * null if this is absent. This default impl is part of the off-the-shelf
     * local file-system cache impl. Can be overriden.
     */
    protected DataInputStream inOpenIndex() throws IOException {
        File indexFile = new File(this.cachedir, "packages.idx");
        if (!indexFile.exists()) {
            return null;
        }
        DataInputStream istream = new DataInputStream(new BufferedInputStream(new FileInputStream(indexFile)));
        return istream;
    }

    /**
     * Open cache index for writing back to persistent storage - hook. This
     * default impl is part of the off-the-shelf local file-system cache impl.
     * Can be overriden.
     */
    protected DataOutputStream outOpenIndex() throws IOException {
        File indexFile = new File(this.cachedir, "packages.idx");
        return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(indexFile)));
    }

    /**
     * Open cache file for reading from persistent storage - hook. This default
     * impl is part of the off-the-shelf local file-system cache impl. Can be
     * overriden.
     */
    protected DataInputStream inOpenCacheFile(String cachefile) throws IOException {
        return new DataInputStream(new BufferedInputStream(new FileInputStream(cachefile)));
    }

    /**
     * Delete (invalidated) cache file from persistent storage - hook. This
     * default impl is part of the off-the-shelf local file-system cache impl.
     * Can be overriden.
     */
    protected void deleteCacheFile(String cachefile) {
        new File(cachefile).delete();
    }

    /**
     * Create/open cache file for rewriting back to persistent storage - hook.
     * If create is false, cache file is supposed to exist and must be opened
     * for rewriting, entry.cachefile is a valid cachefile id. If create is
     * true, cache file must be created. entry.cachefile is a flat jarname to be
     * used to produce a valid cachefile id (to be put back in entry.cachefile
     * on exit). This default impl is part of the off-the-shelf local
     * file-system cache impl. Can be overriden.
     */
    protected DataOutputStream outCreateCacheFile(JarXEntry entry, boolean create) throws IOException {
        File cachefile = null;
        if (create) {
            int index = 1;
            String suffix = "";
            String jarname = entry.cachefile;
            while (true) {
                cachefile = new File(this.cachedir, jarname + suffix + ".pkc");
                if (!cachefile.exists()) {
                    break;
                }
                suffix = "$" + index;
                index += 1;
            }
            entry.cachefile = cachefile.getCanonicalPath();
        } else cachefile = new File(entry.cachefile);
        return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(cachefile)));
    }

    private File cachedir;

    /**
     * Initialize off-the-shelf (default) local file-system cache impl. Must be
     * called before {@link #initCache}. cachedir is the cache repository
     * directory, this is eventually created. Returns true if dir works.
     */
    protected boolean useCacheDir(File aCachedir1) {
        if (aCachedir1 == null) {
            return false;
        }
        try {
            if (!aCachedir1.isDirectory() && aCachedir1.mkdirs() == false) {
                warning("can't create package cache dir, '" + aCachedir1 + "'");
                return false;
            }
        } catch (AccessControlException ace) {
            warning("The java security manager isn't allowing access to the package cache dir, '" + aCachedir1 + "'");
            return false;
        }
        this.cachedir = aCachedir1;
        return true;
    }
}
