package net.jwpa.model;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.jwpa.cache.model.CachedMedia;
import net.jwpa.cache.model.CachedPicAlbum;
import net.jwpa.config.Permission;
import net.jwpa.controller.AccessControl;
import net.jwpa.controller.Utils;
import net.jwpa.dao.MediaDAO;
import net.jwpa.tools.OutputStreamCounter;

public abstract class PicAlbumFacade implements LocalizedMessageHolder {

    CachedPicAlbum cachedVersion;

    CachedPicAlbum getCachedVersion() {
        return cachedVersion;
    }

    public PicAlbumFacade(CachedPicAlbum al) {
        cachedVersion = al;
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public final MediaFacade getIcon() throws IOException {
        return MediaDAO.getFacade(getCachedVersion().getIcon());
    }

    public void setIcon(MediaFacade icon) throws IOException {
        throw new RuntimeException(Utils.WIP_MESSAGE);
    }

    public abstract String getId();

    public abstract List<MediaFacade> getSampleMedia(int max) throws IOException;

    public abstract String getAlbumType();

    public abstract String getName();

    public abstract String getLongComment(java.util.Locale loc) throws IOException;

    public abstract String getShortComment(java.util.Locale loc) throws IOException;

    public abstract PicAlbumFacade getParent() throws IOException;

    public abstract boolean isRootAlbum();

    public abstract boolean exists() throws IOException;

    public abstract boolean isHidden() throws IOException;

    public abstract boolean isEmpty() throws IOException;

    public abstract int getNbMedia() throws IOException;

    public abstract int getNbAlbums() throws IOException;

    public abstract List<MediaFacade> getMediaList() throws IOException;

    public abstract List<PicAlbumFacade> getAlbums() throws IOException;

    public abstract String getURL() throws IOException;

    public String getParentURL() throws IOException {
        PicAlbumFacade parent = getParent();
        if (parent != null) return parent.getURL(); else return getURL();
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public boolean equals(Object o) {
        if (o instanceof PicAlbumFacade) return (getId().equals(((PicAlbumFacade) o).getId())); else return false;
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public long getEstimatedDownloadSize() throws IOException {
        return cachedVersion.getEstimatedDownloadSize();
    }

    public long getComputedDownloadSize() throws IOException {
        OutputStreamCounter osc = new OutputStreamCounter();
        ZipOutputStream zos = new ZipOutputStream(osc);
        List<MediaFacade> list = getMediaList();
        if (list == null || list.size() == 0) return 0;
        for (MediaFacade m : list) {
            ZipEntry ze = new ZipEntry(m.getName());
            ze.setMethod(ZipEntry.STORED);
            ze.setCrc(Utils.getDummyCRC(m.getSize()));
            ze.setSize(m.getSize());
            zos.putNextEntry(ze);
            Utils.serveDummyData(m.getSize(), zos);
            zos.closeEntry();
        }
        zos.flush();
        zos.finish();
        zos.close();
        return osc.getCount();
    }

    public void dumpAlbum(OutputStream os) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(os);
        List<MediaFacade> list = getMediaList();
        for (MediaFacade m : list) {
            ZipEntry ze = new ZipEntry(m.getName());
            ze.setCrc(Utils.getCRC(m.getFile()));
            ze.setMethod(ZipEntry.STORED);
            ze.setSize(m.getSize());
            zos.putNextEntry(ze);
            Utils.serveFile(m.getAbsolutePath(), zos);
            zos.closeEntry();
        }
        zos.flush();
        zos.finish();
        zos.close();
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public int getRank(MediaFacade media) throws IOException {
        CachedMedia[] list = cachedVersion.getMedia();
        int rank = 1;
        for (CachedMedia i : list) {
            if (i.getAbsoluteFileName().equals(media.getAbsolutePath())) return rank;
            rank++;
        }
        return 0;
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public MediaFacade getNextMedia(MediaFacade m) throws IOException {
        CachedMedia[] list = cachedVersion.getMedia();
        boolean next = false;
        CachedMedia first = null;
        for (CachedMedia i : list) {
            if (next) return MediaDAO.getFacade(i);
            if (first == null) first = i;
            if (i.getAbsoluteFileName().equals(m.getAbsolutePath())) next = true;
        }
        return MediaDAO.getFacade(first);
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public MediaFacade getPreviousMedia(MediaFacade m) throws IOException {
        CachedMedia[] list = cachedVersion.getMedia();
        CachedMedia last = null;
        for (CachedMedia i : list) {
            if (i.getAbsoluteFileName().equals(m.getAbsolutePath())) {
                if (last != null) return MediaDAO.getFacade(last);
                return MediaDAO.getFacade(list[list.length - 1]);
            }
            last = i;
        }
        return MediaDAO.getFacade(last);
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public boolean hasParent() throws IOException {
        return cachedVersion.getParent() != null;
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public List<MediaFacade> getMediaListRecursive() throws IOException {
        List<MediaFacade> res = new LinkedList<MediaFacade>();
        res.addAll(getMediaList());
        for (PicAlbumFacade a : getAlbums()) {
            res.addAll(a.getMediaListRecursive());
        }
        return res;
    }

    @AccessControl(required = { Permission.PICS_ORGANIZE })
    public List<String> getMediaTags(String additionalTags) throws IOException {
        List<MediaFacade> list = getMediaList();
        List<String> set = new LinkedList<String>();
        set.addAll(loadTags(list));
        if (additionalTags != null) {
            String[] splts = additionalTags.split(",");
            for (String s : splts) {
                s = s.trim();
                if (s.length() > 0 && !set.contains(s)) set.add(s);
            }
        }
        Collections.sort(set);
        return set;
    }

    private Set<String> loadTags(List<MediaFacade> list) throws IOException {
        Set<String> res = new HashSet<String>();
        for (MediaFacade i : list) {
            for (String s : i.getDefinedTagsList()) {
                if (s != null && s.length() > 0) res.add(s);
            }
        }
        return res;
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public boolean hasIcon() throws IOException {
        return getIcon() != null;
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getIconURL(int box) throws IOException {
        MediaFacade i = getIcon();
        if (i != null) return i.getIconURL(box); else return "";
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public int getIconHeight(int box) throws IOException {
        MediaFacade i = getIcon();
        if (i != null) return i.getIconHeight(box);
        return box;
    }

    public final Map<String, String> getMessageLabels(Object messageId) {
        if (messageId.equals("comment")) return cachedVersion.getComment();
        if (messageId.equals("longcomment")) return cachedVersion.getLongComment();
        if (messageId.equals("title")) return cachedVersion.getTitle();
        return null;
    }
}
