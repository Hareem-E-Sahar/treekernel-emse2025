package net.jwpa.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;
import net.jwpa.config.LogUtil;
import net.jwpa.config.Permission;
import net.jwpa.controller.AccessControl;
import net.jwpa.dao.CacheDataProvider;
import net.jwpa.dao.MediaIndex;
import net.jwpa.dao.VirtualAlbumUtils;
import net.jwpa.dao.cache.CacheUtils;
import freemarker.cache.StringTemplateLoader;
import freemarker.core.ParseException;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateException;

public class VirtualAlbum extends Album implements Comparable<VirtualAlbum> {

    private static final Logger logger = LogUtil.getLogger();

    private String value;

    private LocalizedProperty title;

    private LocalizedProperty description;

    private String thumb;

    private String key;

    public VirtualAlbum(LocalizedProperty _title, String _value, LocalizedProperty _desc, String _thumb, String _key, CacheDataProvider cdp) {
        description = _desc;
        value = _value;
        title = _title;
        key = _key;
        thumb = _thumb;
    }

    @Override
    public String getId() {
        return "v:" + key;
    }

    public long getLastModified() {
        return 0;
    }

    @AccessControl(required = { Permission.JWPA_ADMIN })
    public String getValue() {
        return value;
    }

    @AccessControl(required = { Permission.JWPA_ADMIN })
    public String getVirtualAlbumKey() {
        return key;
    }

    public String getKey() {
        return this.getClass().getName() + "#" + getVirtualAlbumKey();
    }

    @AccessControl(required = { Permission.JWPA_ADMIN })
    public boolean isReal() {
        return value != null && key != null && title != null && value.length() > 0 && key.length() > 0 && getDefaultTitle().length() > 0;
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public List<Media> getMediaList() throws IOException {
        return VirtualAlbumUtils.getVaData(key);
    }

    /**
     * Note that this method computes the expression for every picture
     * in the collection. Accordingly, it might be very slow on big 
     * collections. For example, it takes ~2s for a simple expression 
     * on a 10k images gallery on an Intel Core 2 Duo E4400.
     * @return the list of media ID that fit this virtual album.
     * @throws IOException if the tags and other data for the picture collection cannot be loaded
     */
    public List<String> computeMedia(VirtualAlbumUtils vaDAO, ComputationReport report) throws IOException {
        freemarker.template.Configuration cfg = new freemarker.template.Configuration();
        cfg.setObjectWrapper(new DefaultObjectWrapper());
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        stringLoader.putTemplate("test", "${(" + value + ")?string}");
        cfg.setTemplateLoader(stringLoader);
        try {
            freemarker.template.Template temp = cfg.getTemplate("test");
            Map<String, Map> data = vaDAO.getVirtualAlbumData();
            List<String> res = new ArrayList<String>();
            Map<String, Object> root = new HashMap<String, Object>();
            for (Map.Entry<String, Map> entry : data.entrySet()) {
                root.put("i", entry.getValue());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                OutputStreamWriter outs = new OutputStreamWriter(out, "UTF-8");
                try {
                    temp.process(root, outs);
                    if (report != null) {
                        report.reportSuccess();
                    }
                } catch (TemplateException e) {
                    if (report != null) {
                        report.reportError(e);
                    }
                }
                outs.close();
                if (out.toString("UTF-8").equals("true")) res.add(entry.getKey());
            }
            sortMediaList(res);
            return res;
        } catch (ParseException e) {
            report.reportError(e);
            return new ArrayList<String>();
        }
    }

    public void sortMediaList(List<String> data) {
        final MediaIndex index = new MediaIndex();
        Collections.sort(data, new Comparator<String>() {

            public int compare(String o1, String o2) {
                try {
                    long m1 = Media.getInstance(index.getMediaName(o1)).getDate().getTime();
                    long m2 = Media.getInstance(index.getMediaName(o2)).getDate().getTime();
                    return new Long(m1).compareTo(m2);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public int compareTo(VirtualAlbum o) {
        return new Integer(key).compareTo(new Integer(o.key));
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public List<Album> getAlbums() throws IOException {
        return new LinkedList<Album>();
    }

    @Override
    public List<Album> getAlbums(boolean authenticated) throws IOException {
        return new LinkedList<Album>();
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getName() {
        return getDefaultTitle();
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public Album getParent() throws IOException {
        return null;
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getParentURL() throws IOException {
        return null;
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getURL() throws IOException {
        return null;
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public boolean isEmpty() throws IOException {
        return getMediaList().size() == 0;
    }

    @Override
    public boolean isHidden() throws IOException {
        return false;
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public boolean isRootAlbum() {
        return false;
    }

    @Override
    public void setThumbFileName(Media filename) throws IOException {
        thumb = filename.file.getAbsolutePath();
        CacheUtils.setModified(this);
    }

    @Override
    public String getThumbFileName() {
        return thumb;
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getShortComment(java.util.Locale loc) throws IOException {
        return getDescription().getValue(loc);
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getLongComment(java.util.Locale loc) throws IOException {
        return getDescription().getValue(loc);
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getAlbumType() {
        return "virtual";
    }

    @AccessControl(required = { Permission.JWPA_ADMIN })
    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) throws IOException {
        this.thumb = thumb;
        CacheUtils.setModified(this);
    }

    @Override
    public String getCustomIconFileName() {
        return thumb;
    }

    @Override
    public void sync() throws IOException {
        VirtualAlbumUtils.sync(this);
    }

    private long getSeed() {
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        try {
            crc.update(value.getBytes("UTF-8"));
            crc.update(getDefaultTitle().getBytes("UTF-8"));
        } catch (Exception e) {
            LogUtil.logError(logger, "UTF-8 doesn't exists", e);
            crc.update(value.getBytes());
            crc.update(getDefaultTitle().getBytes());
        }
        return crc.getValue();
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public List<Media> getSampleMedia(int max) throws IOException {
        List<Media> alreadyIn = new ArrayList<Media>();
        alreadyIn.add(getIcon());
        List<Media> res = new ArrayList<Media>();
        List<Media> overall = getMediaList();
        List<Media> remains = new ArrayList<Media>();
        String f = "";
        for (Media me : overall) {
            String ff = me.getFolderFullName();
            if (!ff.equals(f) && !alreadyIn.contains(me)) {
                res.add(me);
                alreadyIn.add(me);
                f = ff;
            }
        }
        if (res.size() < max) Collections.shuffle(remains, new Random(getSeed()));
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

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public int getNbAlbums() throws IOException {
        return 0;
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public int getNbMedia() throws IOException {
        return getMediaList().size();
    }

    public Properties getProperties() throws IOException {
        return properties;
    }

    private Properties properties = new Properties();

    public class ComputationReport {

        Map<String, Integer> errors = new HashMap<String, Integer>();

        int errorsCount = 0;

        int successCount = 0;

        VirtualAlbum va;

        public ComputationReport(VirtualAlbum va) {
            this.va = va;
        }

        public VirtualAlbum getVA() {
            return va;
        }

        public void reportError(Exception e) {
            errorsCount++;
            String msg = e.getMessage();
            Integer i = errors.get(msg);
            if (i == null) {
                i = new Integer(0);
            }
            errors.put(msg, i + 1);
        }

        public void reportSuccess() {
            successCount++;
        }

        public List<String> getErrors() {
            List<String> res = new LinkedList<String>();
            for (Map.Entry<String, Integer> e : errors.entrySet()) {
                res.add(e.getValue() + " : " + e.getKey());
            }
            return res;
        }

        public int getErrorsCount() {
            return errorsCount;
        }

        public int getSuccessCount() {
            return successCount;
        }
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof VirtualAlbum) {
            return ((VirtualAlbum) o).key.equals(this.key);
        }
        return false;
    }

    public LocalizedProperty getLocalizedProperty(String propertyName) {
        if (propertyName.equals("comment")) return description;
        if (propertyName.equals("longcomment")) return description;
        if (propertyName.equals("title")) return title;
        return null;
    }

    public LocalizedProperty getTitle() {
        return title;
    }

    public LocalizedProperty getDescription() {
        return description;
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getDefaultTitle() {
        try {
            return title.getDefaultValue();
        } catch (IOException e) {
            LogUtil.logError(logger, e);
        }
        return "";
    }

    @Override
    public boolean exists() throws IOException {
        return VirtualAlbumUtils.albumExists(getKey());
    }
}
