package net.jwpa.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import net.jwpa.config.Config;
import net.jwpa.config.LogUtil;
import net.jwpa.config.Permission;
import net.jwpa.controller.AccessControl;
import net.jwpa.controller.MediaFollowTools;
import net.jwpa.controller.TplSerCtx;
import net.jwpa.controller.URLTools;
import net.jwpa.controller.Utils;
import net.jwpa.dao.CacheDataProvider;
import net.jwpa.dao.FolderDAO;
import net.jwpa.dao.cache.CacheUtils;
import net.jwpa.tools.Tools;

public class Folder extends Album {

    private static final Logger logger = LogUtil.getLogger();

    private File dir;

    private final boolean exists;

    Properties properties;

    boolean rootAlbum;

    boolean markedForDeletion;

    public void markForDeletion() {
        markedForDeletion = true;
    }

    @Override
    public String getId() {
        return "f:" + dir.getAbsolutePath();
    }

    public Folder(File _dir) {
        dir = _dir;
        rootAlbum = dir.getAbsolutePath().equals(Config.getCurrentConfig().getAbsoluteRootFolderName());
        exists = dir.exists();
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getName() {
        return dir.getName();
    }

    public String getAbsolutePath() throws IOException {
        return dir.getAbsolutePath();
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public Folder getParent() throws IOException {
        if (rootAlbum) return null; else return FolderDAO.getFolder(dir.getParentFile());
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public boolean isRootAlbum() {
        return rootAlbum;
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public boolean isHidden() throws IOException {
        if (dir.getName().startsWith(".")) return true;
        if (getParent() == null) return false;
        return getParent().isHidden();
    }

    public String[] getFlags() throws IOException {
        String flagsS = getProperties().getProperty("flags");
        if (flagsS == null) return new String[0];
        return flagsS.split(",");
    }

    public boolean isFlagged(String flag) throws IOException {
        String[] flags = getFlags();
        for (String f : flags) {
            if (f.equals(flag)) return true;
        }
        return false;
    }

    public void sync() throws IOException {
        LogUtil.logDebug(logger, "FOLDER SYNCED " + getName());
        synchronized (LOCK) {
            if (!dir.exists()) {
                if (!markedForDeletion && exists) throw new RuntimeException("Folder could not be synced because it doesn't exist anymore");
                return;
            }
            File f = new File(dir, Config.getCurrentConfig().getFolderPropertiesName());
            FileOutputStream fos = new FileOutputStream(f);
            try {
                properties.store(fos, "Properties for the folder " + dir.getAbsolutePath());
            } finally {
                fos.close();
            }
        }
    }

    public Properties getProperties() throws IOException {
        if (properties == null) {
            File f = new File(dir, Config.getCurrentConfig().getFolderPropertiesName());
            Properties p = new Properties();
            if (f.exists()) {
                FileInputStream fis = new FileInputStream(f);
                try {
                    p.load(fis);
                } finally {
                    fis.close();
                }
            }
            if (Utils.isStringEmpty(p.getProperty("title"))) p.setProperty("title", getName());
            properties = p;
        }
        return properties;
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public boolean isEmpty() throws IOException {
        return getCachedIntProperty("isEmpty", DAY * 5, "isEmptyImpl") == 0;
    }

    public int isEmptyImpl() throws IOException {
        File[] resl = dir.listFiles();
        if (resl == null) return 0;
        return resl.length;
    }

    public String[] getMediaListNames() throws IOException {
        return getCachedSAProperty("mediaList", DAY * 5, "getMediaListNamesImpl");
    }

    public long getLastModified() {
        return getDir().lastModified();
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public List<Media> getMediaList() throws IOException {
        return (List<Media>) getMemoryCachedObjectProperty("mediaListAsObject", SECOND * 3, "getMediaListImpl");
    }

    public List<Media> getMediaListImpl() throws IOException {
        String[] data = getMediaListNames();
        List<Media> res = new ArrayList<Media>(data.length);
        for (String s : data) res.add(Media.getInstance(s));
        return res;
    }

    public String[] getMediaListNamesImpl() throws IOException {
        final Map<String, Date> mediaCache = new HashMap<String, Date>();
        File[] resl = dir.listFiles(new FFImage());
        if (resl != null) {
            Arrays.sort(resl, new Comparator<File>() {

                public int compare(File o1, File o2) {
                    try {
                        String n1 = o1.getAbsolutePath();
                        String n2 = o2.getAbsolutePath();
                        Date d1 = mediaCache.get(n1);
                        Date d2 = mediaCache.get(n2);
                        if (d1 == null) {
                            Media m1 = Media.getInstance(o1.getAbsolutePath());
                            if (m1 != null) {
                                d1 = m1.getDate();
                                mediaCache.put(n1, d1);
                            }
                        }
                        if (d2 == null) {
                            Media m2 = Media.getInstance(o2.getAbsolutePath());
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
        String[] res = new String[resl.length];
        for (int i = 0; i < resl.length; i++) {
            res[i] = resl[i].getAbsolutePath();
            MediaFollowTools.assessExistingResource(Tools.getRelativeFilename(resl[i]));
        }
        return res;
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public List<Album> getAlbums() throws IOException {
        return getAlbums(TplSerCtx.getInstance().getUser().isAllowed(Permission.PICS_VIEW_PROTECTED));
    }

    public List<Folder> getFolders(boolean authenticated) throws IOException {
        return (List<Folder>) getMemoryCachedObjectProperty("foldersList." + authenticated, DAY * 5, "getFoldersImpl" + authenticated);
    }

    public List<Folder> getFoldersImpltrue() throws IOException {
        return getFoldersImpl(true);
    }

    public List<Folder> getFoldersImplfalse() throws IOException {
        return getFoldersImpl(false);
    }

    public List<Folder> getFoldersImpl(boolean authenticated) throws IOException {
        List<Album> l = getAlbums(authenticated);
        List<Folder> res = new ArrayList<Folder>(l.size());
        for (Album a : l) res.add((Folder) a);
        return res;
    }

    public List<Album> getAlbums(boolean authenticated) throws IOException {
        List res = (List) getMemoryCachedObjectProperty("albumsList." + authenticated, MINUTE, "getAlbumsImpl" + authenticated);
        return (List<Album>) res;
    }

    public List<Album> getAlbumsImpltrue() throws IOException {
        return getAlbumsImpl(true);
    }

    public List<Album> getAlbumsImplfalse() throws IOException {
        return getAlbumsImpl(false);
    }

    public List<Album> getAlbumsImpl(boolean authenticated) throws IOException {
        String[] data = getAlbumsListNames(authenticated);
        List<Album> res = new ArrayList<Album>(data.length);
        for (String s : data) {
            res.add(FolderDAO.getFolder(s));
        }
        return res;
    }

    public String[] getAlbumsListNames(boolean auth) throws IOException {
        return getCachedSAProperty("albumsListNames." + auth, DAY * 5, "getAlbumsListNamesImpl" + auth);
    }

    public String[] getAlbumsListNamesImpltrue() throws IOException {
        return getAlbumsListNamesImpl(true);
    }

    public String[] getAlbumsListNamesImplfalse() throws IOException {
        return getAlbumsListNamesImpl(false);
    }

    public String[] getAlbumsListNamesImpl(boolean authenticated) throws IOException {
        File[] resa = dir.listFiles(new FFAlbum());
        if (resa != null) Arrays.sort(resa, new Comparator<File>() {

            public int compare(File o1, File o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });
        List<String> res = new ArrayList<String>();
        if (resa != null) for (File f : resa) {
            if (authenticated || !f.getName().startsWith(".")) res.add(f.getAbsolutePath());
        }
        return res.toArray(new String[res.size()]);
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getRelativeFilename() {
        return Tools.getRelativeFilename(dir);
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getURL() throws IOException {
        return URLTools.encodePathToUrl(getRelativeFilename());
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getParentURL() throws IOException {
        return URLTools.encodePathToUrl(Tools.getRelativeFilename(dir.getParentFile()));
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getShortComment(java.util.Locale loc) throws IOException {
        return getLocalizedProperty("comment").getValue(loc);
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getLongComment(java.util.Locale loc) throws IOException {
        return getLocalizedProperty("longcomment").getValue(loc);
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public LocalizedProperty getLocalizedProperty(String propertyName) {
        return new LocalizedPropertyImpl(this, propertyName);
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public List<Folder> getGenealogy() throws Exception {
        File iter = dir;
        List<Folder> list1 = new ArrayList<Folder>();
        while (!iter.getAbsolutePath().equals(Config.getCurrentConfig().getAbsoluteRootFolderName())) {
            iter = iter.getParentFile();
            if (iter == null) LogUtil.logError(logger, "Could not find root folder from " + dir + ". Chances are that the root folder in the config file is in the wrong case and you are on a case insensitive filesystem.");
            list1.add(0, FolderDAO.getFolder(iter));
        }
        list1.add(this);
        return list1;
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public boolean equals(Object o) {
        if (o instanceof Folder) {
            return dir.getAbsolutePath().equals(((Folder) o).dir.getAbsolutePath());
        }
        return false;
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getAlbumType() {
        return "folder";
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getCustomIconFileName() {
        return getThumbFileName();
    }

    private long getSeed() {
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
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public List<Media> getSampleMedia(int max) throws IOException {
        List<Media> alreadyIn = new ArrayList<Media>();
        alreadyIn.add(getIcon());
        List<Media> res = new ArrayList<Media>();
        List<Media> overall = new ArrayList<Media>();
        List<Folder> lf = new LinkedList<Folder>();
        lf.add(this);
        while (!lf.isEmpty()) {
            Folder f = lf.get(lf.size() - 1);
            lf.remove(lf.size() - 1);
            if (f.getIcon() != null) {
                if (!alreadyIn.contains(f.getIcon())) {
                    res.add(f.getIcon());
                    alreadyIn.add(f.getIcon());
                }
            }
            overall.addAll(f.getMediaList());
            lf.addAll(f.getFolders(false));
        }
        if (res.size() < max) Collections.shuffle(overall, new Random(getSeed()));
        while (res.size() < max) {
            if (overall.size() == 0) break;
            Media tba = overall.get(overall.size() - 1);
            if (!alreadyIn.contains(tba)) res.add(tba);
            overall.remove(overall.size() - 1);
        }
        while (res.size() > max) {
            res.remove(res.size() - 1);
        }
        return res;
    }

    public File getDir() {
        return dir;
    }

    public String getKey() {
        return this.getClass().getName() + "#" + getDir().getAbsolutePath();
    }

    public void setThumbFileName(Media filename) throws IOException {
        getProperties().setProperty("thumb", filename.file.getAbsolutePath());
        CacheUtils.setModified(this);
    }

    public String getThumbFileName() {
        try {
            return getProperties().getProperty("thumb");
        } catch (IOException e) {
            LogUtil.logError(logger, e);
        }
        return null;
    }

    public void addFlag(String flag) throws IOException {
        String[] flags = getFlags();
        Set<String> set = new HashSet<String>();
        for (String fn : flags) if (fn.trim().length() > 0) set.add(fn.trim());
        set.add(flag);
        setFlags(set);
        CacheUtils.setModified(this);
    }

    public void setFlags(Set<String> set) throws IOException {
        StringBuffer sbf = new StringBuffer();
        for (String fn : set) sbf.append(',').append(fn);
        LogUtil.logDebug(logger, "fl:" + sbf.substring(1));
        getProperties().setProperty("flags", sbf.substring(1));
        CacheUtils.setModified(this);
    }

    public void remFlag(String flag) throws IOException {
        String[] flags = getFlags();
        Set<String> set = new HashSet<String>();
        for (String fn : flags) if (fn.trim().length() > 0 && !fn.trim().equals(flag)) set.add(fn.trim());
        setFlags(set);
        CacheUtils.setModified(this);
    }

    public void contentChanged() throws IOException {
        CacheUtils.resetCacheKey(this.getKey());
    }

    /***************************************************************************
	 * Cached Properties
	 */
    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public int getNbAlbums() throws IOException {
        return getCachedIntProperty("nbAlbums." + (TplSerCtx.getInstance().getUser().isAllowed(Permission.PICS_VIEW_PROTECTED)), DAY * 5, "getNbAlbumsImpl");
    }

    public int getNbAlbumsImpl() throws IOException {
        return getAlbums(TplSerCtx.getInstance().getUser().isAllowed(Permission.PICS_VIEW_PROTECTED)).size();
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public int getNbMedia() throws IOException {
        return getCachedIntProperty("nbMedia", DAY * 5, "getNbMediaImpl");
    }

    public int getNbMediaImpl() throws IOException {
        return getMediaList().size();
    }

    @Override
    public boolean exists() {
        return dir.exists() && dir.isDirectory();
    }
}

class FFImage implements FilenameFilter {

    public boolean accept(File dir, String name) {
        return Media.getMediaType(name) != Media.Type.UNKNOWN;
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
