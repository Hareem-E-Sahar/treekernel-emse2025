package org.opencms.search.documents;

import org.opencms.cache.CmsVfsDiskCache;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsFileUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;
import org.apache.commons.logging.Log;

/**
 * Implements a disk cache that stores text extraction results in the RFS.<p>
 * 
 * This cache operates on resource file names, plus a hashcode calculated from 
 * {@link org.opencms.file.CmsResource#getDateLastModified()}
 * and {@link org.opencms.file.CmsResource#getLength()}. Optional a locale can be appended to this name.<p> 
 * 
 * Since text extraction is done only on the content of a resource, all siblings must have the same content.
 * The difference can be only by the locale setting in case of an XML content or XML page. However,
 * the most problematic contents to extract for the search are in fact the MS Office and PDF formats.
 * For these documents, all siblings must produce the exact same text extraction result.<p>
 * 
 * This cache is usable for resources from the online AND the offline project at the same time, 
 * because any change to a resource will result in a changed hashcode. This means a resource changed in the offline
 * project will have a new hashcode compared to the online project. If the resource is identical in the online and 
 * the offline project, the generated hashcodes will be the same.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.5 $
 * 
 * @since 6.2.0
 */
public class CmsExtractionResultCache {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExtractionResultCache.class);

    /** The name of the cache base repository folder in the RFS. */
    private String m_rfsRepository;

    /**
     * Creates a new disk cache.<p>
     * 
     * @param basepath the base path for the cache in the RFS
     * @param foldername the folder name for this cache, to be used a subfolder for the base folder
     */
    public CmsExtractionResultCache(String basepath, String foldername) {
        m_rfsRepository = CmsFileUtil.normalizePath(basepath + foldername + File.separatorChar);
    }

    /**
     * Removes all expired extraction result cache entries from the RFS cache.<p>
     * 
     * @param maxAge the maximum age of the extraction result cache files in hours (or fractions of hours)
     * 
     * @return the total number of deleted resources
     */
    public synchronized int cleanCache(float maxAge) {
        long expireDate = System.currentTimeMillis() - (long) (maxAge * 60.0f * 60.0f * 1000.0f);
        File basedir = new File(m_rfsRepository);
        int count = 0;
        if (basedir.canRead() && basedir.isDirectory()) {
            File[] files = basedir.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File f = files[i];
                    if (f.canWrite()) {
                        if (f.lastModified() < expireDate) {
                            try {
                                f.delete();
                                count++;
                            } catch (Exception e) {
                                if (LOG.isWarnEnabled()) {
                                    LOG.warn(Messages.get().getBundle().key(Messages.LOG_EXCERPT_CACHE_DELETE_ERROR_1, f.getAbsolutePath()), e);
                                }
                            }
                        }
                    }
                }
            }
        }
        return count;
    }

    /**
     * Returns the RFS name used for caching an the text extraction result
     * based on the given VFS resource and locale.<p>  
     * 
     * @param resource the VFS resource to generate the cache name for
     * @param locale the locale to generate the cache name for (may be <code>null</code>)
     * 
     * @return the RFS name to use for caching the given VFS resource with parameters 
     */
    public String getCacheName(CmsResource resource, Locale locale) {
        StringBuffer buf = new StringBuffer(m_rfsRepository.length() + 36);
        buf.append(m_rfsRepository);
        buf.append('/');
        buf.append(resource.getResourceId().toString());
        if (locale != null) {
            buf.append('_');
            buf.append(locale.toString());
        }
        buf.append('_');
        buf.append(resource.getDateContent());
        buf.append(".ext");
        return buf.toString();
    }

    /**
     * Returns the object stored in the requested file in the disk cache, or <code>null</code> if the
     * file is not found in the cache, or is found but outdated.<p>
     * 
     * @param rfsName the file RFS name to look up in the cache 
     * 
     * @return the object stored in the requested file in the RFS disk cache, or <code>null</code> 
     */
    public Object getCacheObject(String rfsName) {
        try {
            File f = new File(rfsName);
            if (f.exists()) {
                long age = f.lastModified();
                if ((System.currentTimeMillis() - age) > 3600000) {
                    f.setLastModified(System.currentTimeMillis());
                }
                byte[] byteContent = CmsFileUtil.readFile(f);
                if (byteContent != null) {
                    ByteArrayInputStream in = new ByteArrayInputStream(byteContent);
                    ObjectInputStream oin = new ObjectInputStream(in);
                    Object result = oin.readObject();
                    oin.close();
                    return result;
                }
            }
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

    /**
     * Returns the absolute path of the cache repository in the RFS.<p>
     * 
     * @return the absolute path of the cache repository in the RFS
     */
    public String getRepositoryPath() {
        return m_rfsRepository;
    }

    /**
     * Serializes the given object and saves it in the disk cache.<p> 
     * 
     * @param rfsName the RFS name of the file to save the object in
     * @param object the object to serialize and save
     * 
     * @throws IOException in case of disk access errors
     */
    public void saveCacheObject(String rfsName, Object object) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(512);
        ObjectOutputStream oout = new ObjectOutputStream(out);
        oout.writeObject(object);
        oout.close();
        byte[] byteContent = out.toByteArray();
        CmsVfsDiskCache.saveFile(rfsName, byteContent);
    }
}
