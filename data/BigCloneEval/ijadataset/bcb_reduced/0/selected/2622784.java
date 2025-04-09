package net.jwpa.model;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.jwpa.config.Config;
import net.jwpa.config.LogUtil;
import net.jwpa.config.Permission;
import net.jwpa.controller.AccessControl;
import net.jwpa.controller.TplSerCtx;
import net.jwpa.controller.Utils;
import net.jwpa.dao.Cacheable;
import net.jwpa.dao.FolderDAO;
import net.jwpa.dao.VirtualAlbumDAO;
import net.jwpa.dao.VirtualAlbumUtils;
import net.jwpa.dao.cache.CachedPropertyHolder;
import net.jwpa.tools.OutputStreamCounter;

public abstract class Album extends CachedPropertyHolder implements LocalizedPropertyHolder, Cacheable {

    private static final Logger logger = LogUtil.getLogger();

    public Album() {
    }

    public abstract String getId();

    public abstract List<Media> getSampleMedia(int max) throws IOException;

    public abstract String getAlbumType();

    public abstract String getName();

    public abstract String getLongComment(java.util.Locale loc) throws IOException;

    public abstract String getShortComment(java.util.Locale loc) throws IOException;

    public abstract Album getParent() throws IOException;

    public abstract boolean isRootAlbum();

    public abstract boolean exists() throws IOException;

    public abstract boolean isHidden() throws IOException;

    public abstract boolean isEmpty() throws IOException;

    public abstract int getNbMedia() throws IOException;

    public abstract int getNbAlbums() throws IOException;

    public abstract List<Media> getMediaList() throws IOException;

    public abstract List<Album> getAlbums() throws IOException;

    public abstract List<Album> getAlbums(boolean authenticated) throws IOException;

    public abstract String getURL() throws IOException;

    public abstract String getParentURL() throws IOException;

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public boolean equals(Object o) {
        if (o instanceof Album) return (getId().equals(((Album) o).getId())); else return false;
    }

    public abstract void setThumbFileName(Media media) throws IOException;

    public abstract String getThumbFileName();

    public abstract void sync() throws IOException;

    public abstract String getCustomIconFileName();

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public long getEstimatedDownloadSize() throws IOException {
        return getCachedIntProperty("estimatedDownloadSize", DAY * 5, "getEstimatedDownloadSizeImpl");
    }

    public int getEstimatedDownloadSizeImpl() throws IOException {
        List<Media> list = getMediaList();
        long total = 0;
        for (Media m : list) {
            total += m.file.length();
        }
        return (int) total;
    }

    public long getComputedDownloadSize() throws IOException {
        OutputStreamCounter osc = new OutputStreamCounter();
        ZipOutputStream zos = new ZipOutputStream(osc);
        List<Media> list = getMediaList();
        if (list == null || list.size() == 0) return 0;
        for (Media m : list) {
            ZipEntry ze = new ZipEntry(m.file.getName());
            ze.setMethod(ZipEntry.STORED);
            ze.setCrc(Utils.getDummyCRC(m.file.length()));
            ze.setSize(m.file.length());
            zos.putNextEntry(ze);
            Utils.serveDummyData(m.file.length(), zos);
            zos.closeEntry();
        }
        zos.flush();
        zos.finish();
        zos.close();
        return osc.getCount();
    }

    public void dumpAlbum(OutputStream os) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(os);
        List<Media> list = getMediaList();
        for (Media m : list) {
            ZipEntry ze = new ZipEntry(m.file.getName());
            ze.setCrc(Utils.getCRC(m.file));
            ze.setMethod(ZipEntry.STORED);
            ze.setSize(m.file.length());
            zos.putNextEntry(ze);
            Utils.serveFile(m.file.getAbsolutePath(), zos);
            zos.closeEntry();
        }
        zos.flush();
        zos.finish();
        zos.close();
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public int getRank(Media media) throws IOException {
        List<Media> list = getMediaList();
        int rank = 1;
        for (Media i : list) {
            if (i.getFile().getCanonicalPath().equals(media.getFile().getCanonicalPath())) return rank;
            rank++;
        }
        return 0;
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public Media getNextMedia(Media m) throws IOException {
        List<Media> list = getMediaList();
        boolean next = false;
        Media first = null;
        for (Media i : list) {
            if (next) return i;
            if (first == null) first = i;
            if (i.getFile().getAbsolutePath().equals(m.getFile().getAbsolutePath())) next = true;
        }
        return first;
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public Media getPreviousMedia(Media m) throws IOException {
        List<Media> list = getMediaList();
        Media last = null;
        for (Media i : list) {
            if (i.getFile().getAbsolutePath().equals(m.getFile().getAbsolutePath())) {
                if (last != null) return last;
                return list.get(list.size() - 1);
            }
            last = i;
        }
        return last;
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public boolean hasParent() throws IOException {
        return getParent() != null;
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public List<Media> getMediaListRecursive() throws IOException {
        return getMediaListRecursive(TplSerCtx.getInstance().getUser().isAllowed(Permission.PICS_VIEW_PROTECTED));
    }

    public List<Media> getMediaListRecursive(boolean authenticated) throws IOException {
        List<Media> res = new ArrayList<Media>();
        res.addAll(getMediaList());
        for (Album a : getAlbums(authenticated)) {
            res.addAll(a.getMediaListRecursive(authenticated));
        }
        return res;
    }

    @AccessControl(required = { Permission.PICS_ORGANIZE })
    public List<String> getMediaTags(String additionalTags) throws IOException {
        List<Media> list = getMediaList();
        Set<String> set = loadTags(list);
        if (additionalTags != null) {
            String[] splts = additionalTags.split(",");
            for (String s : splts) {
                s = s.trim();
                if (s.length() > 0) set.add(s);
            }
        }
        List<String> tagsList = new ArrayList<String>(set.size());
        tagsList.addAll(set);
        Collections.sort(tagsList);
        return tagsList;
    }

    public Set<String> loadTags(List<Media> list) throws IOException {
        Set<String> res = new HashSet<String>();
        for (Media i : list) {
            for (String s : i.getDefinedTagsList()) {
                if (s != null && s.length() > 0) res.add(s);
            }
        }
        return res;
    }

    public String getIconName() throws IOException {
        String res = getCachedStringProperty("iconName", DAY * 5, "getIconNameImpl");
        return res;
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public Media getIcon() throws IOException {
        return (Media) getMemoryCachedObjectProperty("iconAsJpegImage", HOUR, "getIconImpl");
    }

    public Media getIconImpl() throws IOException {
        String imgt = getIconName();
        if (imgt != null && imgt.equals("noicon")) return null;
        if (imgt == null || imgt.length() == 0 || !new File(imgt).exists()) {
            resetCachedValues();
            imgt = getIconName();
        }
        return Media.getInstance(imgt);
    }

    public String getIconNameImpl() throws IOException {
        String imgt = getThumbFileName();
        if (imgt != null && imgt.length() > 0 && new File(imgt).exists()) return imgt;
        List<Media> listi = getMediaList();
        if (listi != null) for (Media m : listi) if (m instanceof JpegImage) return m.file.getAbsolutePath();
        List<Album> listf = getAlbums(false);
        if (listf != null) for (Album f : listf) {
            Media ii = f.getIcon();
            if (ii != null) return ii.file.getAbsolutePath();
        }
        if (listi != null) for (Media m : listi) return m.file.getAbsolutePath();
        return "noicon";
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public boolean hasIcon() throws IOException {
        return getIcon() != null;
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getIconURL(int box) throws IOException {
        return getIconURL(box, TplSerCtx.getInstance());
    }

    public String getIconURL(int box, TplSerCtx context) throws IOException {
        Media i = getIcon();
        if (i != null) return i.getIconURL(box, box, context); else return "";
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public int getIconHeight(int box) throws IOException {
        Media i = getIcon();
        try {
            if (i != null) return i.getIconHeight(box);
        } catch (Exception e) {
        }
        return box;
    }

    public static Album getAlbum(String id) throws IOException {
        if (id.startsWith("f:")) {
            return FolderDAO.getFolder(id.substring(2));
        } else if (id.startsWith("v:")) {
            return VirtualAlbumDAO.get(id.substring(2));
        } else {
            LogUtil.logWarn(logger, "Cannot get album " + id);
        }
        return null;
    }

    public static Album getAlbum(String showva, String dir, VirtualAlbumUtils vaData) throws IOException {
        if (showva == null) return FolderDAO.getFolder(dir); else return VirtualAlbumDAO.get(showva);
    }
}
