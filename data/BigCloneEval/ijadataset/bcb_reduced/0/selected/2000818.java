package net.jwpa.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;
import net.jwpa.cache.model.ActivatableHashSet;
import net.jwpa.cache.model.ActivatableLinkedList;
import net.jwpa.cache.model.CachedFolder;
import net.jwpa.cache.model.CachedMedia;
import net.jwpa.cache.model.CachedPicAlbum;
import net.jwpa.config.Config;
import net.jwpa.config.LogUtil;
import net.jwpa.controller.MediaFollowTools;
import net.jwpa.dao.MediaDAO;
import net.jwpa.dao.StringUtils;
import net.jwpa.model.FolderFacade;
import net.jwpa.model.LocalizedMessageImpl;
import net.jwpa.model.MediaFacade;
import net.jwpa.tools.Tools;

public class FolderReader extends PicAlbumReader {

    private static final Logger logger = LogUtil.getLogger();

    private final String filename;

    private final File file;

    public FolderReader(String fn) {
        super(FOLDER_PREFIX + fn);
        filename = fn;
        file = new File(filename);
    }

    public static FolderReader getInstance(String f) {
        return new FolderReader(f);
    }

    public static FolderFacade getFacade(CachedFolder cpa) {
        return (FolderFacade) PicAlbumReader.getFacade(cpa);
    }

    public static List<FolderFacade> getFacadeList(Collection<CachedFolder> cpa) {
        List<FolderFacade> res = new LinkedList<FolderFacade>();
        for (CachedFolder cf : cpa) res.add(getFacade(cf));
        return res;
    }

    @Override
    public void sync(AbstractCachedObject o) throws IOException {
        LogUtil.logDebug(logger, "Saving folder " + filename);
        Properties properties = new Properties();
        fillInPropertiesWithCache((CachedFolder) o, properties);
        File f = getPropertiesFile();
        FileOutputStream fos = new FileOutputStream(f);
        try {
            properties.store(fos, "Properties for the file " + ((CachedFolder) o).getAbsoluteFileName());
        } finally {
            fos.close();
        }
    }

    private void fillInPropertiesWithCache(CachedFolder cf, Properties properties) {
        properties.setProperty("flags", Tools.getStringFromArray(cf.getFlags()));
        LocalizedMessageImpl.storeToProperties(properties, "longcomment", cf.getLongComment());
        LocalizedMessageImpl.storeToProperties(properties, "comment", cf.getComment());
        LocalizedMessageImpl.storeToProperties(properties, "title", cf.getTitle());
        if (cf.getCustomIcon() == null) properties.setProperty("thumb", ""); else properties.setProperty("thumb", cf.getCustomIcon().getAbsoluteFileName());
    }

    @Override
    void refreshEntryFromDisk(AbstractTopLevelCachedObject o) throws IOException {
        buildEntryFromDisk((CachedFolder) o);
    }

    @Override
    public CachedFolder buildEntryFromDisk() throws IOException {
        CachedFolder cf = new CachedFolder();
        buildEntryFromDisk(cf);
        return cf;
    }

    public void buildEntryFromDisk(CachedFolder cf) throws IOException {
        LogUtil.logDebug(logger, "Building folder " + filename);
        cf.setAbsoluteFileName(filename);
        cf.setRootAlbum(isRootFolder());
        cf.setFlags(getFlags(cf));
        cf.setGenealogy(getGenealogy(cf));
        super.fillInEntryFromDisk(cf);
        cf.setInitialized();
    }

    public static ActivatableLinkedList<CachedFolder> getGenealogy(CachedFolder forFolder) throws IOException {
        File iter = new File(forFolder.getAbsoluteFileName());
        ActivatableLinkedList<CachedFolder> list1 = new ActivatableLinkedList<CachedFolder>(forFolder);
        while (!iter.getAbsolutePath().equals(Config.getCurrentConfig().getAbsoluteRootFolderName())) {
            iter = iter.getParentFile();
            if (iter == null) LogUtil.logError(logger, "Could not find root folder from " + forFolder.getAbsoluteFileName() + ". Chances are that the root folder in the config file is in the wrong case and you are on a case insensitive filesystem.");
            list1.add(0, CacheBuilder.loadCachedObject(CachedFolder.class, iter.getAbsolutePath()));
        }
        list1.add(forFolder);
        return list1;
    }

    private boolean isRootFolder() {
        return filename.equals(Config.getCurrentConfig().getAbsoluteRootFolderName());
    }

    private ActivatableHashSet<String> getFlags(AbstractTopLevelCachedObject tlo) throws IOException {
        String fls = getProperties().getProperty("flags");
        ActivatableHashSet<String> res = new ActivatableHashSet<String>(tlo);
        if (fls == null) return res;
        String[] t = fls.split(",");
        for (String tt : t) {
            String ttt = tt.trim();
            if (!StringUtils.isStringEmpty(ttt)) {
                res.add(ttt);
            }
        }
        return res;
    }

    private File getPropertiesFile() {
        return new File(filename, Config.getCurrentConfig().getFolderPropertiesName());
    }

    private Properties properties;

    public Properties getProperties() throws IOException {
        if (properties == null) {
            File f = getPropertiesFile();
            Properties p = new Properties();
            if (f.exists()) {
                FileInputStream fis = new FileInputStream(f);
                try {
                    p.load(fis);
                } finally {
                    fis.close();
                }
            }
            properties = p;
            if (StringUtils.isStringEmpty(p.getProperty("title"))) p.setProperty("title", new File(filename).getName());
        }
        return properties;
    }

    @Override
    public CachedPicAlbum getParentAlbum() throws IOException {
        if (isRootFolder()) return null; else return FolderReader.getInstance(new File(filename).getParent()).loadOrBuildCachedVersion();
    }

    @Override
    long getLastModifiedTime() {
        return file.lastModified();
    }

    @Override
    public void fillInMedia(CachedPicAlbum fp) throws IOException {
        CachedFolder f = (CachedFolder) fp;
        final Map<String, Date> mediaCache = new HashMap<String, Date>();
        File[] resl = new File(f.getAbsoluteFileName()).listFiles(new FFImage());
        if (resl != null) {
            Arrays.sort(resl, new Comparator<File>() {

                public int compare(File o1, File o2) {
                    try {
                        String n1 = o1.getAbsolutePath();
                        String n2 = o2.getAbsolutePath();
                        Date d1 = mediaCache.get(n1);
                        Date d2 = mediaCache.get(n2);
                        if (d1 == null) {
                            CachedMedia m1 = MediaReader.getInstance(o1.getAbsolutePath()).loadOrBuildCachedVersion();
                            if (m1 != null) {
                                d1 = m1.getDate();
                                mediaCache.put(n1, d1);
                            }
                        }
                        if (d2 == null) {
                            CachedMedia m2 = MediaReader.getInstance(o2.getAbsolutePath()).loadOrBuildCachedVersion();
                            if (m2 != null) {
                                d2 = m2.getDate();
                                mediaCache.put(n2, d2);
                            }
                        }
                        if (d1 != null && d2 != null) {
                            int i = d1.compareTo(d2);
                            if (i != 0) return i;
                        }
                        return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } else {
            resl = new File[0];
        }
        CachedMedia[] res = new CachedMedia[resl.length];
        for (int i = 0; i < resl.length; i++) {
            res[i] = MediaReader.getInstance(resl[i].getAbsolutePath()).loadOrBuildCachedVersion();
            MediaFollowTools.assessExistingResource(Tools.getRelativeFilename(resl[i].getAbsolutePath()));
        }
        fp.defineMedia(res);
    }

    @Override
    public void fillInSampleMedia(int max, CachedPicAlbum infp) throws IOException {
        CachedFolder inf = (CachedFolder) infp;
        List<CachedMedia> alreadyIn = new ArrayList<CachedMedia>();
        alreadyIn.add(inf.getIcon());
        List<CachedMedia> res = new ArrayList<CachedMedia>();
        List<CachedMedia> overall = new ArrayList<CachedMedia>();
        List<CachedPicAlbum> lf = new LinkedList<CachedPicAlbum>();
        lf.add(inf);
        while (!lf.isEmpty()) {
            CachedPicAlbum f = lf.get(lf.size() - 1);
            if (f.isHidden()) continue;
            lf.remove(lf.size() - 1);
            if (f.getIcon() != null) {
                if (!alreadyIn.contains(f.getIcon())) {
                    res.add(f.getIcon());
                    alreadyIn.add(f.getIcon());
                    if (res.size() >= max) break;
                }
            }
            overall.addAll(Arrays.asList(f.getMedia()));
            lf.addAll(Arrays.asList(f.getAlbums()));
        }
        if (res.size() < max) Collections.shuffle(overall, new Random(getSeed(new File(inf.getAbsoluteFileName()))));
        while (res.size() < max) {
            if (overall.size() == 0) break;
            CachedMedia tba = overall.get(overall.size() - 1);
            if (!alreadyIn.contains(tba)) res.add(tba);
            overall.remove(overall.size() - 1);
        }
        while (res.size() > max) {
            res.remove(res.size() - 1);
        }
        infp.defineSampleMedia(res.toArray(new CachedMedia[res.size()]));
    }

    @Override
    public void fillInAlbums(CachedPicAlbum cfp) throws IOException {
        CachedFolder cf = (CachedFolder) cfp;
        File[] resa = new File(cf.getAbsoluteFileName()).listFiles(new FFAlbum());
        if (resa != null) Arrays.sort(resa, new Comparator<File>() {

            public int compare(File o1, File o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });
        List<CachedPicAlbum> res = new LinkedList<CachedPicAlbum>();
        if (resa != null) {
            for (File f : resa) {
                res.add(CacheBuilder.loadCachedObject(CachedFolder.class, f.getAbsolutePath()));
            }
        }
        cfp.defineAlbums(res.toArray(new CachedFolder[res.size()]));
    }

    private static long getSeed(File dir) {
        String s = dir.getAbsolutePath();
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        try {
            crc.update(s.getBytes("UTF-8"));
        } catch (Exception e) {
            LogUtil.logError(logger, "UTF-8 doesn't exists", e);
            crc.update(s.getBytes());
        }
        return crc.getValue();
    }

    @Override
    public CachedFolder loadOrBuildCachedVersion() throws IOException {
        return CacheBuilder.loadCachedObject(CachedFolder.class, filename);
    }
}

class FFImage implements FilenameFilter {

    public boolean accept(File dir, String name) {
        return MediaDAO.getMediaType(name) != MediaFacade.Type.UNKNOWN;
    }
}

class FFAlbum implements FilenameFilter {

    public boolean accept(File dir, String name) {
        if (new File(dir, name).isDirectory()) {
            return true;
        }
        return false;
    }
}
