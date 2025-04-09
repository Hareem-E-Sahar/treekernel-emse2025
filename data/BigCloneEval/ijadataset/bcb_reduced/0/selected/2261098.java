package org.openorb.compiler.taskdefs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.zip.ZipInputStream;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

/**
 * Ant FileNameMapper that uses a cache for tracking
 * file dependencies
 * @author Erik Putrycz erik.putrycz_at_ieee.org
 */
public class CacheFileNameMapper implements FileNameMapper {

    protected Task m_task;

    protected HashMap m_cachecontent;

    protected File m_cachefile;

    protected File m_current_srcdir = null;

    protected boolean m_is_loaded = false;

    private boolean m_verbose = false;

    /**
     * Creates a new CacheFileMapper
     * Provides a solution for ant tasks having 1 to n files dependencies
     * @param task
     * @param cachefile
     */
    public CacheFileNameMapper(Task task, File cachefile) {
        m_task = task;
        m_cachefile = cachefile;
    }

    /**
     * @see org.apache.tools.ant.util.FileNameMapper#setFrom(java.lang.String)
     */
    public void setFrom(String arg0) {
    }

    public void setVerbose(boolean verb) {
        m_verbose = verb;
    }

    /**
     * @see org.apache.tools.ant.util.FileNameMapper#setTo(java.lang.String)
     */
    public void setTo(String arg0) {
    }

    public void setCurrentSrcDir(File srcdir) {
        m_current_srcdir = srcdir;
    }

    /**
     * @see org.apache.tools.ant.util.FileNameMapper#mapFileName(java.lang.String)
     */
    public String[] mapFileName(String arg0) {
        File lookup = new File(m_current_srcdir, arg0);
        try {
            lookup = lookup.getCanonicalFile();
        } catch (IOException e) {
        }
        if (m_verbose) {
            System.out.println("searching for " + lookup.toString());
        }
        String[] result = (String[]) m_cachecontent.get(lookup);
        if ((result == null) || (result.length == 0)) {
            result = new String[1];
            result[0] = arg0;
        }
        if (m_verbose) {
            for (int il = 0; il < result.length; il++) {
                System.out.println("result[" + il + "]=" + result[il]);
            }
        }
        return result;
    }

    /**
     * Add a new entry to the cache
     * @param entry entry
     * @param destfiles an array containing all the destination files
     */
    public void addEntry(File src_file, String[] destfiles) {
        File can_file = src_file;
        try {
            can_file = src_file.getCanonicalFile();
        } catch (IOException e) {
        }
        m_cachecontent.put(can_file, destfiles);
    }

    /**
     * Writes the cache to the disk
     */
    public void writeCache() {
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(m_cachefile));
            zos.putNextEntry(new ZipEntry("FileNameMapper.cache"));
            ObjectOutputStream os = new ObjectOutputStream(zos);
            os.writeObject(m_cachecontent);
            os.flush();
            os.close();
        } catch (Exception ex) {
            m_task.log("Impossible to write cache file " + m_cachefile);
        }
    }

    /**
     * Loads the cache from the disk and if fails then create a new empty one
     */
    public void init(boolean force_reload) {
        if (force_reload || !m_is_loaded) {
            try {
                ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(m_cachefile)));
                zis.getNextEntry();
                ObjectInputStream ios = new ObjectInputStream(zis);
                m_cachecontent = (HashMap) ios.readObject();
                ios.close();
                m_is_loaded = true;
            } catch (Exception ex) {
                m_task.log("Impossible to read cache file " + m_cachefile);
                m_cachecontent = new HashMap();
            }
        }
    }
}
