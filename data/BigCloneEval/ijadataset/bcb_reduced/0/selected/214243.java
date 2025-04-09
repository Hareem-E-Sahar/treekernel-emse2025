package tufts.vue.action;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.*;
import tufts.Util;
import tufts.vue.DEBUG;
import tufts.vue.VueUtil;
import tufts.vue.Version;
import tufts.vue.VUE;
import tufts.vue.Resource;
import tufts.vue.PropertyEntry;
import tufts.vue.URLResource;
import tufts.vue.Images;
import tufts.vue.IMSCP;
import tufts.vue.LWComponent;
import tufts.vue.LWMap;
import static tufts.vue.Resource.*;

/**
 * Code related to identifying, creating and unpacking VUE archives.
 *
 * @version $Revision: 1.14 $ / $Date: 2010-02-03 19:13:45 $ / $Author: mike $ 
 */
public class Archive {

    private static final org.apache.log4j.Logger Log = org.apache.log4j.Logger.getLogger(Archive.class);

    private static final String ZIP_IMPORT_LABEL = "Imported";

    private static final String MAP_ARCHIVE_KEY = "@(#)TUFTS-VUE-ARCHIVE";

    private static final String SPEC_KEY = "spec=";

    private static final int SPEC_KEY_LEN = SPEC_KEY.length();

    public static boolean isVueIMSCPArchive(File file) {
        if (!file.getName().toLowerCase().endsWith(".zip")) return false;
        try {
            ZipFile zipFile = new ZipFile(file);
            return zipFile.getEntry(IMSCP.MAP_FILE) != null && zipFile.getEntry(IMSCP.MANIFEST_FILE) != null;
        } catch (Throwable t) {
            Log.warn(t);
            return false;
        }
    }

    public static boolean isVuePackage(File file) {
        return file.getName().toLowerCase().endsWith(VueUtil.VueArchiveExtension);
    }

    /**
     * @return true if we can create files in the given directory
     * File.canWrite is insufficient to ensure this.   If the filesystem
     * the directory is on is not writeable, we wont know this until
     * we attempt to create a file there, and it fails.
     */
    public static boolean canCreateFiles(File directory) {
        if (directory == null) return false;
        File tmp = null;
        try {
            tmp = directory.createTempFile(".vueFScheck", "", directory);
        } catch (Throwable t) {
            Log.info("Cannot write to filesystem inside: " + directory + "; " + t);
        }
        if (tmp != null) {
            if (DEBUG.Enabled) Log.debug("Created test file: " + tmp);
            try {
                tmp.delete();
            } catch (Throwable t) {
                Log.error("Couldn't delete tmp file " + tmp, t);
            }
            return true;
        }
        return false;
    }

    /**
     * @param zipFile should be a File pointing to a VUE Package -- a Zip Archive created by VUE
     */
    public static LWMap openVuePackage(final File zipFile) throws java.io.IOException, java.util.zip.ZipException {
        Log.info("Unpacking VUE zip archive: " + zipFile);
        final String unpackingDir;
        final File parentDirectory = zipFile.getParentFile();
        if (false && canCreateFiles(parentDirectory)) unpackingDir = parentDirectory.toString(); else unpackingDir = VUE.getSystemProperty("java.io.tmpdir");
        Log.info("Unpacking location: " + unpackingDir);
        final ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
        final Map<String, String> packagedResources = new HashMap();
        ZipEntry entry;
        ZipEntry mapEntry = null;
        String mapFile = null;
        while ((entry = zin.getNextEntry()) != null) {
            final String location = unzipEntryToFile(zin, entry, unpackingDir);
            final String comment = Archive.getComment(entry);
            if (comment != null) {
                if (comment.startsWith(MAP_ARCHIVE_KEY)) {
                    mapEntry = entry;
                    mapFile = location;
                    Log.info("Identified map entry: " + comment + " (" + entry.getName() + ")");
                } else {
                    String spec = comment.substring(comment.indexOf(SPEC_KEY) + SPEC_KEY_LEN);
                    Log.info("             [" + spec + "]");
                    if (packagedResources.put(spec, location) != null) Log.warn("repeated resource spec in archive! [" + spec + "]");
                }
            } else {
                Log.warn("ENTRY WITH NO COMMENT: " + entry);
            }
        }
        zin.close();
        final LWMap map = ActionUtil.unmarshallMap(new File(mapFile), new ArchiveMapUnmarshalHandler(zipFile + "(" + mapEntry + ")", zipFile, packagedResources));
        map.setFile(zipFile);
        map.markAsSaved();
        return map;
    }

    private static class ArchiveMapUnmarshalHandler extends MapUnmarshalHandler {

        final Map<String, String> packagedResources;

        final File archiveFile;

        ArchiveMapUnmarshalHandler(Object source, File archiveFile, Map<String, String> resourcesFoundInPackage) {
            super(source, Resource.MANAGED_UNMARSHALLING);
            this.packagedResources = resourcesFoundInPackage;
            this.archiveFile = archiveFile;
        }

        /** skip setFile -- we're going to use the package file */
        @Override
        void notifyFile(final LWMap map, final File file) {
            super.map = map;
            super.file = file;
            map.setArchiveMap(true);
        }

        /** this impl is so we can patch up resources first before completing the restore */
        @Override
        void notifyUnmarshallingCompleted() {
            map.runResourceDeserializeInits(map.getAllResources());
            patchResourcesForPackage();
            map.completeXMLRestore(context);
        }

        private void patchResourcesForPackage() {
            for (Resource r : map.getAllResources()) {
                final String packageCacheFile = packagedResources.get(r.getSpec());
                if (packageCacheFile != null) {
                    if (DEBUG.Enabled) Log.debug("patching packaged resource: " + packageCacheFile + "; into " + r);
                    final File localFile = new File(packageCacheFile);
                    final String localPath = localFile.toString();
                    if (DEBUG.RESOURCE && !localPath.equals(packageCacheFile)) Log.info("       localized file path: " + localPath);
                    if (r instanceof URLResource) {
                        ((URLResource) r).setPackageFile(localFile, archiveFile);
                    } else {
                        Log.warn("package file fallback for unknown resource type, impl in question: " + Util.tags(r) + "; " + localFile);
                        r.setProperty(PACKAGE_FILE, localFile);
                        r.setCached(true);
                    }
                } else {
                    if (DEBUG.Enabled) Log.debug("No archive entry matching: " + r.getSpec());
                }
            }
        }
    }

    /**
     * @param location -- if null, entry will be unzipped in local (current) working directory,
     * otherwise, entry will be unzipped at the given path location in the file system.
     * @return filename of unzipped file
     */
    public static String unzipEntryToFile(ZipInputStream zin, ZipEntry entry, String location) throws IOException {
        final String filename;
        if (location == null) {
            filename = entry.getName();
        } else {
            if (location.endsWith(File.separator)) filename = location + entry.getName(); else filename = location + File.separator + entry.getName();
        }
        if (true || DEBUG.IO) {
            String msg = "Unzipping to " + filename + " from entry " + entry;
            Log.info(msg);
        }
        final File newFile = createFile(filename);
        final FileOutputStream out = new FileOutputStream(newFile);
        byte[] b = new byte[1024];
        int len = 0;
        int wrote = 0;
        while ((len = zin.read(b)) != -1) {
            wrote += len;
            out.write(b, 0, len);
        }
        out.close();
        if (DEBUG.IO) {
            Log.debug("    Unzipped " + filename + "; wrote=" + wrote + "; size=" + entry.getSize());
        }
        return filename;
    }

    public static File createFile(String name) throws IOException {
        final File file = new File(name);
        File parent = file;
        while ((parent = parent.getParentFile()) != null) {
            if (parent.getPath().equals("/")) {
                break;
            }
            if (!parent.exists()) {
                Log.debug("Creating: " + parent);
                parent.mkdir();
            }
        }
        file.createNewFile();
        return file;
    }

    private static void unzipIMSCP(ZipInputStream zin, ZipEntry entry) throws IOException {
        unzipEntryToFile(zin, entry, VueUtil.getDefaultUserFolder().getAbsolutePath());
    }

    public static LWMap loadVueIMSCPArchive(File file) throws java.io.FileNotFoundException, java.util.zip.ZipException, java.io.IOException {
        Log.info("Unpacking VUE IMSCP zip archive: " + file);
        ZipFile zipFile = new ZipFile(file);
        Vector<Resource> resourceVector = new Vector();
        File resourceFolder = new File(VueUtil.getDefaultUserFolder().getAbsolutePath() + File.separator + IMSCP.RESOURCE_FILES);
        if (resourceFolder.exists() || resourceFolder.mkdir()) {
            ZipInputStream zin = new ZipInputStream(new FileInputStream(file));
            ZipEntry e;
            while ((e = zin.getNextEntry()) != null) {
                unzipIMSCP(zin, e);
                if (!e.getName().equalsIgnoreCase(IMSCP.MAP_FILE) && !e.getName().equalsIgnoreCase(IMSCP.MANIFEST_FILE)) {
                    Resource resource = Resource.getFactory().get(e.getName());
                    resourceVector.add(resource);
                }
            }
            zin.close();
        }
        File mapFile = new File(VueUtil.getDefaultUserFolder().getAbsolutePath() + File.separator + IMSCP.MAP_FILE);
        LWMap map = ActionUtil.unmarshallMap(mapFile);
        map.setFile(null);
        map.setLabel(ZIP_IMPORT_LABEL);
        for (Resource r : resourceVector) {
            replaceResource(map, r, Resource.getFactory().get(VueUtil.getDefaultUserFolder().getAbsolutePath() + File.separator + r.getSpec()));
        }
        map.markAsSaved();
        return map;
    }

    public static void replaceResource(LWMap map, Resource r1, Resource r2) {
        for (LWComponent component : map.getAllDescendents()) {
            if (component.hasResource()) {
                Resource resource = component.getResource();
                if (resource.getSpec().equals(r1.getSpec())) component.setResource(r2);
            }
        }
    }

    private static final String COMMENT_ENCODING = "UTF-8";

    /**
     *
     * There's a java bug (STILL!) as of JAN 2008: comments are encoded in the zip file,
     * but not extractable via any method call in any JDK.  See:
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6646605
     *
     * This method encapsulates workaround comment setting code.  We add comments anyway
     * for easy debug (e.g., unzip -l), and then encode them again as "extra" zip entry
     * bytes, which we can extract later as the comment.
     *
     * Note also that for special characters to make it through this process across
     * multiple platforms, the same, platform-neutral encoding must be used
     * both when setting and getting.
     *
     */
    private static void setComment(ZipEntry entry, String comment) {
        entry.setComment(comment);
        try {
            entry.setExtra(comment.getBytes(COMMENT_ENCODING));
        } catch (Throwable t) {
            Log.warn("Couldn't " + COMMENT_ENCODING + " encode 'extra' bytes into ZipEntry comment; " + entry + "; [" + comment + "]", t);
            entry.setExtra(comment.getBytes());
        }
    }

    /**
     * Extract comments from the given ZipEntry.
     * @See setComment
     */
    public static String getComment(ZipEntry entry) {
        byte[] extra = entry.getExtra();
        String comment = null;
        if (extra != null && extra.length > 0) {
            if (DEBUG.IO && DEBUG.META) Log.debug("getComment found " + extra.length + " extra bytes");
            try {
                comment = new String(extra, COMMENT_ENCODING);
            } catch (Throwable t) {
                Log.warn("Couldn't " + COMMENT_ENCODING + " decode 'extra' bytes from ZipEntry comment; " + entry, t);
                comment = new String(extra);
            }
        }
        return comment;
    }

    private static int UniqueNameFailsafeCount = 1;

    /**
     * Generate a package file name from the given URLResource.  We could just as easily
     * generate random names, but we base it on the URL for easy debugging and
     * and exploring of the package in Finder/Explorer (e.g., we also try to make
     * sure the documents have appropriate extensions so the OS shell applications
     * can generate appropriate icons, etc).
     *
     * @param existingNames -- if provided, will put the result of generated names
     * in this set, and will be used to ensure that no repeated names are generated
     * on future calls.
     */
    private static String generatePackageFileName(Resource r, Set<String> existingNames) {
        String packageName = null;
        try {
            packageName = generateInformativePackageFileName(r);
        } catch (Throwable t) {
            Log.warn("Failed to create informative package name for " + r, t);
        }
        if (packageName != null && packageName.length() > 250) {
            Log.info("Truncating long name: " + packageName);
            packageName = packageName.substring(0, 250);
        }
        if (packageName == null) {
            packageName = String.format("vuedata%03d", UniqueNameFailsafeCount++);
        } else if (existingNames != null) {
            if (existingNames.contains(packageName)) {
                Log.info("repeated name [" + packageName + "]");
                int cnt = 1;
                String uniqueName = packageName;
                int lastDot = packageName.lastIndexOf('.');
                if (lastDot > 0) {
                    final String preDot = packageName.substring(0, lastDot);
                    final String postDot = packageName.substring(lastDot);
                    do {
                        uniqueName = String.format("%s.%03d%s", preDot, cnt++, postDot);
                    } while (existingNames.contains(uniqueName));
                } else {
                    do {
                        uniqueName = String.format("%s.%03d", packageName, cnt++);
                    } while (existingNames.contains(uniqueName));
                }
                packageName = uniqueName;
                Log.info("uniqified package name: " + packageName);
            }
            existingNames.add(packageName);
        }
        return packageName;
    }

    private static String generateInformativePackageFileName(Resource r) throws java.io.UnsupportedEncodingException {
        if (DEBUG.IO) Log.debug("Generating package file name from " + r + "; " + r.getProperties());
        try {
            if (r.hasProperty(PACKAGE_FILE)) {
                File pf = (File) r.getPropertyValue(PACKAGE_FILE);
                String name = pf.getName();
                if (DEBUG.IO) Log.debug("Using pre-existing package file name: " + name);
                return name;
            }
        } catch (Throwable t) {
            Log.warn(t);
        }
        final Object imageSource = r.getImageSource();
        if (imageSource == null) return r.getSpec();
        String packageName;
        if (imageSource instanceof File) {
            packageName = ((File) imageSource).getName();
        } else if (imageSource instanceof URL) {
            final URL url = (URL) imageSource;
            packageName = url.toString();
            if (packageName.startsWith("http://")) {
                packageName = packageName.substring(7);
            }
            if (r.isImage() && r.hasProperty(IMAGE_FORMAT) && !Resource.looksLikeImageFile(packageName)) packageName += "." + r.getProperty(IMAGE_FORMAT).toLowerCase();
        } else {
            throw new IllegalArgumentException("image source is neither URL or File: " + Util.tags(imageSource));
        }
        if (DEBUG.IO) Log.debug("     decoding " + packageName);
        packageName = java.net.URLDecoder.decode(packageName, "UTF-8");
        if (DEBUG.IO) Log.debug("   decoded to " + packageName);
        packageName = java.net.URLEncoder.encode(packageName, "UTF-8");
        if (DEBUG.IO) Log.debug("re-encoded to " + packageName);
        packageName = packageName.replace('%', '$');
        if (DEBUG.IO) Log.debug(" locked in at " + packageName);
        packageName = packageName.replaceAll("\\+", "\\$20");
        return packageName;
    }

    private static class Item {

        final ZipEntry entry;

        final Resource resource;

        final File dataFile;

        Item(ZipEntry e, Resource r, File f) {
            entry = e;
            resource = r;
            dataFile = f;
        }

        public String toString() {
            return "Item[" + entry.toString() + "; " + resource + "; " + dataFile + "]";
        }
    }

    /**

     * Write the map to the given file as a Zip archive, along with all unique resources
     * for which data can be found locally (local user files, or local image cache).
     * Entries for Resource data in the zip archive are annotated with their original
     * Resource spec, so they can be identified on unpacking, and associated with their
     * original aResources.

     */
    public static void writeArchive(LWMap map, File archive) throws java.io.IOException {
        Log.info("Writing archive package " + archive);
        final String label = archive.getName();
        final String mapName;
        if (label.endsWith(VueUtil.VueArchiveExtension)) mapName = label.substring(0, label.length() - 4); else mapName = label;
        final String dirName = mapName + ".vdr";
        final Collection<Resource> uniqueResources = map.getAllUniqueResources();
        final Collection<PropertyEntry> manifest = new ArrayList();
        final List<Item> items = new ArrayList();
        final Set<String> uniqueEntryNames = new HashSet();
        Archive.UniqueNameFailsafeCount = 1;
        for (Resource r : uniqueResources) {
            try {
                final File sourceFile = r.getActiveDataFile();
                final String description = "" + (DEBUG.Enabled ? r : r.getSpec());
                if (sourceFile == null) {
                    Log.info("skipped: " + description);
                    continue;
                } else if (!sourceFile.exists()) {
                    Log.warn("Missing local file: " + sourceFile + "; for " + r);
                    continue;
                }
                final String packageEntryName = generatePackageFileName(r, uniqueEntryNames);
                final ZipEntry entry = new ZipEntry(dirName + "/" + packageEntryName);
                Archive.setComment(entry, "\t" + SPEC_KEY + r.getSpec());
                final Item item = new Item(entry, r, sourceFile);
                items.add(item);
                manifest.add(new PropertyEntry(r.getSpec(), packageEntryName));
                if (DEBUG.Enabled) Log.info("created: " + item);
            } catch (Throwable t) {
                Log.error("writeArchive: failed to handle " + Util.tags(r), t);
            }
        }
        final ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(archive)));
        final ZipEntry mapEntry = new ZipEntry(dirName + "/" + mapName + "$map.vue");
        final String comment = MAP_ARCHIVE_KEY + "; VERSION: 2;" + " Saved " + new Date() + " by " + VUE.getName() + " built " + Version.AllInfo + "; items=" + items.size() + ";" + ">";
        Archive.setComment(mapEntry, comment);
        zos.putNextEntry(mapEntry);
        final Writer mapOut = new OutputStreamWriter(zos);
        try {
            map.setArchiveManifest(manifest);
            ActionUtil.marshallMapToWriter(map, mapOut);
        } catch (Throwable t) {
            Log.error(t);
            throw new RuntimeException(t);
        } finally {
            map.setArchiveManifest(null);
        }
        for (Item item : items) {
            if (DEBUG.Enabled) Log.debug("writing: " + item); else Log.info("writing: " + item.entry);
            try {
                zos.putNextEntry(item.entry);
                copyBytesToZip(item.dataFile, zos);
            } catch (Throwable t) {
                Log.error("Failed to archive item: " + item, t);
            }
        }
        zos.closeEntry();
        zos.close();
        Log.info("Wrote " + archive);
    }

    private static void copyBytesToZip(File file, ZipOutputStream zos) throws java.io.IOException {
        final BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
        byte[] buf = new byte[2048];
        int len;
        int total = 0;
        while ((len = fis.read(buf)) > 0) {
            if (DEBUG.IO && DEBUG.META) System.err.print(".");
            zos.write(buf, 0, len);
            total += len;
        }
        fis.close();
    }
}
