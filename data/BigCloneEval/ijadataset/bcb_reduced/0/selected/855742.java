package imi.repository;

import imi.utils.Cosmic;
import imi.utils.MD5HashUtils;
import imi.utils.ObjectInputStreamEx;
import imi.utils.ObjectOutputStreamEx;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * Default implementation of caching behavior.
 * @author Ronald E Dahlgren
 */
public class DefaultCacheBehavior implements CacheBehavior {

    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(Repository.class.getName());

    /** Used to tune the buffer size for the max **/
    private static final int BUFFER_SIZE = 1024 * 64;

    /** Instrumentation **/
    private static CacheInstrumentation instruments = null;

    /** The folder we will be searching for and storing cache files in. **/
    private File cacheFolder = null;

    /**
     * Construct a new default avatar caching behavior.
     * @param cacheFolder The cache folder to use.
     * @param instrument True to instrument
     * @throws ExceptionInInitializerError If (cacheFolder == null)
     */
    public DefaultCacheBehavior(File cacheFolder) {
        if (cacheFolder == null) throw new ExceptionInInitializerError("Cannot have a null cache folder!");
        if (cacheFolder.exists() == false) if (cacheFolder.mkdir() == false) throw new ExceptionInInitializerError("Could not create cache folder: " + cacheFolder);
        this.cacheFolder = cacheFolder;
        System.out.println("Cache folder: " + cacheFolder);
        if (instruments == null) instruments = new CacheInstrumentation();
    }

    public boolean initialize(Object[] params) {
        return true;
    }

    public boolean shutdown() {
        instruments.dumpStats();
        return true;
    }

    public boolean isCached(RRL location) {
        boolean fileFound;
        File cacheFile = rrlToCacheFile(location);
        fileFound = cacheFile.exists();
        if (!fileFound) instruments.cacheMisses++;
        return fileFound;
    }

    public boolean clearCache(RRL resource) {
        System.out.println("Clearing cache: " + resource);
        boolean fileFound;
        File cacheFile = rrlToCacheFile(resource);
        fileFound = cacheFile.exists();
        boolean deleted = false;
        if (fileFound) {
            deleted = cacheFile.delete();
            if (!deleted) logger.log(Level.SEVERE, "CACHE FILE DELETION FAILED: {0}", cacheFile.getName());
        }
        return deleted;
    }

    public boolean clearCacheFolder() {
        boolean deletion = false;
        for (File file : cacheFolder.listFiles()) {
            deletion = file.delete();
            if (!deletion) logger.log(Level.SEVERE, "CACHE FILE DELETION FAILED: {0}", file.getName());
        }
        if (cacheFolder.listFiles().length == 0) return true; else return false;
    }

    void dumpStats() {
        instruments.dumpStats();
    }

    /**
     * {@inheritDoc CacheBehavior}
     */
    public void createCachePackage(OutputStream output) {
        int bytesRead = 0;
        byte[] transferBuffer = new byte[BUFFER_SIZE];
        try {
            ZipOutputStream zos = new ZipOutputStream(output);
            for (File file : cacheFolder.listFiles()) {
                if (file.getName().contains("DS_Store") || file.getName().endsWith(".svn")) continue;
                ZipEntry fileEntry = new ZipEntry(file.getName());
                zos.putNextEntry(fileEntry);
                InputStream bis = new FileInputStream(file);
                while ((bytesRead = bis.read(transferBuffer)) != -1) {
                    zos.write(transferBuffer, 0, bytesRead);
                }
                bis.close();
                zos.closeEntry();
                logger.log(Level.INFO, "Wrote {0}, size is {1}", new Object[] { file.toString(), fileEntry.getSize() });
            }
            zos.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Caught an IOException while making cache package: {0}", ex.getMessage());
        }
    }

    /**
     * {@inheritDoc CacheBehavior}
     */
    public void loadCachePackage(InputStream input, CachePackageListener listener) {
        try {
            byte[] transferBuffer = new byte[BUFFER_SIZE];
            ZipInputStream zis = new ZipInputStream(input);
            ZipEntry currentEntry = null;
            while ((currentEntry = zis.getNextEntry()) != null) {
                readCacheItemEntry(zis, currentEntry, transferBuffer, listener);
                zis.closeEntry();
            }
            input.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to load cache package due to IOException: {0}", ex.getMessage());
        } catch (ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "Problem with binary file: {0}", ex.getMessage());
        }
    }

    private void readCacheItemEntry(ZipInputStream zis, ZipEntry entry, byte[] buffer, CachePackageListener listener) throws IOException, ClassNotFoundException {
        int bytesRead = 0;
        File outputCacheFile = new File(cacheFolder, entry.getName());
        FileOutputStream fos = new FileOutputStream(outputCacheFile);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((bytesRead = zis.read(buffer, 0, BUFFER_SIZE)) != -1) {
            fos.write(buffer, 0, bytesRead);
            bos.write(buffer, 0, bytesRead);
            bytesRead = 0;
        }
        if (listener != null && entry.getSize() > 0) listener.cachedItemLoaded(loadZippedCacheFile(new ByteArrayInputStream(bos.toByteArray())));
    }

    private File rrlToCacheFile(RRL location) {
        String urlString = location.getRelativePath().toString();
        String hashFileName = MD5HashUtils.getStringFromHash(urlString.getBytes());
        File result = new File(cacheFolder, hashFileName);
        File localFile = new File("assets/" + urlString);
        if (localFile != null && result.exists()) {
            if (localFile.lastModified() > result.lastModified()) {
                boolean deletion = result.delete();
                if (!deletion) logger.log(Level.SEVERE, "Deletion of old cache file ({0}) failed: ", result.getName()); else logger.log(Level.FINE, "Deleted old cache file ({0}): ", result.getName());
            }
        }
        return result;
    }

    public boolean writeToCache(RRL location, Object runTimeRepresentation) {
        boolean result = false;
        try {
            File file = rrlToCacheFile(location);
            file.getParentFile().mkdirs();
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            CachedItem itemEntry = new CachedItem(new Date(), location.getRelativePath(), runTimeRepresentation.getClass());
            writeZippedCacheFile(fos, runTimeRepresentation, itemEntry);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Problem creating cache file.", ex);
            result = false;
        }
        return result;
    }

    public CachedItem loadCachedItem(RRL location) {
        CachedItem itemEntry = null;
        try {
            FileInputStream fis = new FileInputStream(rrlToCacheFile(location));
            itemEntry = loadZippedCacheFile(fis);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Problem reading cache file.", ex);
            itemEntry = null;
            clearCache(location);
        }
        return itemEntry;
    }

    private CachedItem loadZippedCacheFile(InputStream stream) throws IOException, ClassNotFoundException {
        CachedItem result = null;
        GZIPInputStream zis = new GZIPInputStream(stream);
        ObjectInputStreamEx inStream = new ObjectInputStreamEx(zis);
        result = (CachedItem) inStream.readObject();
        result.setLoadedData(inStream.readObject());
        logger.log(Level.FINE, "Loaded from cache: {0}", result);
        return result;
    }

    private void writeZippedCacheFile(OutputStream outStream, Object runtime, CachedItem cacheEntry) throws IOException {
        GZIPOutputStream zos = new GZIPOutputStream(outStream);
        ObjectOutputStreamEx out = new ObjectOutputStreamEx(zos);
        out.writeObject(cacheEntry);
        out.writeObject(runtime);
        zos.flush();
        zos.close();
        out.close();
    }

    private class CacheInstrumentation {

        int cacheHits;

        int cacheMisses;

        void dumpStats() {
            System.out.println("Cache Data, hits: " + cacheHits + ", misses:" + cacheMisses);
        }
    }
}
