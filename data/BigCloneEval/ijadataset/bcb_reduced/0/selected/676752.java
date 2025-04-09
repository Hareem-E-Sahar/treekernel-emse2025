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
import net.jwpa.cache.PicAlbumReader;
import net.jwpa.cache.model.CachedVirtualAlbum;
import net.jwpa.config.LogUtil;
import net.jwpa.config.Permission;
import net.jwpa.controller.AccessControl;
import net.jwpa.controller.Utils;
import net.jwpa.dao.MediaDAO;
import net.jwpa.dao.MediaIndex;
import net.jwpa.dao.VirtualAlbumUtils;
import freemarker.cache.StringTemplateLoader;
import freemarker.core.ParseException;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateException;

public class VirtualAlbumFacade extends PicAlbumFacade implements Comparable<VirtualAlbumFacade> {

    private static final Logger logger = LogUtil.getLogger();

    private CachedVirtualAlbum cached;

    public VirtualAlbumFacade(CachedVirtualAlbum cv) {
        super(cv);
        cached = cv;
    }

    @Override
    public String getId() {
        return PicAlbumReader.VIRTUAL_PREFIX + cached.getId();
    }

    public long getLastModified() {
        return 0;
    }

    @AccessControl(required = { Permission.PICS_ORGANIZE })
    public String getValue() {
        return cached.getValue();
    }

    @AccessControl(required = { Permission.PICS_ORGANIZE })
    public void setValue(String value) {
        cached.setValue(value);
    }

    @AccessControl(required = { Permission.JWPA_ADMIN })
    public String getVirtualAlbumKey() {
        return cached.getId();
    }

    public String getKey() {
        return this.getClass().getName() + "#" + getVirtualAlbumKey();
    }

    @AccessControl(required = { Permission.JWPA_ADMIN })
    public boolean isReal() {
        return getValue() != null && getVirtualAlbumKey() != null && getTitle() != null && getValue().length() > 0 && getVirtualAlbumKey().length() > 0 && getDefaultTitle().length() > 0;
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public List<MediaFacade> getMediaList() throws IOException {
        return VirtualAlbumUtils.getVaData(cached.getId());
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
        stringLoader.putTemplate("test", "${(" + cached.getValue() + ")?string}");
        cfg.setTemplateLoader(stringLoader);
        try {
            freemarker.template.Template temp = cfg.getTemplate("test");
            Map<String, Map<String, Object>> data = vaDAO.getVirtualAlbumData();
            List<String> res = new ArrayList<String>();
            Map<String, Object> root = new HashMap<String, Object>();
            for (Map.Entry<String, Map<String, Object>> entry : data.entrySet()) {
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
                    long m1 = MediaDAO.getInstance(index.getMediaName(o1)).getDate().getTime();
                    long m2 = MediaDAO.getInstance(index.getMediaName(o2)).getDate().getTime();
                    return new Long(m1).compareTo(m2);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public int compareTo(VirtualAlbumFacade o) {
        return new Integer(cached.getId()).compareTo(new Integer(o.cached.getId()));
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public List<PicAlbumFacade> getAlbums() throws IOException {
        return new LinkedList<PicAlbumFacade>();
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getName() {
        return getDefaultTitle();
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public PicAlbumFacade getParent() throws IOException {
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
        return Integer.parseInt(cached.getId()) == 0;
    }

    @Override
    public void setIcon(MediaFacade filename) throws IOException {
        throw new RuntimeException(Utils.WIP_MESSAGE);
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getShortComment(java.util.Locale loc) throws IOException {
        return getShortComment().getValue(loc);
    }

    @AccessControl(required = { Permission.PICS_ORGANIZE })
    public LocalizedMessage getShortComment() {
        return new LocalizedMessageImpl(cached.getComment(), "");
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getLongComment(java.util.Locale loc) throws IOException {
        return getLongComment().getValue(loc);
    }

    @AccessControl(required = { Permission.PICS_ORGANIZE })
    public LocalizedMessage getLongComment() {
        return new LocalizedMessageImpl(cached.getLongComment(), "");
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getAlbumType() {
        return "virtual";
    }

    @AccessControl(required = { Permission.JWPA_ADMIN })
    public String getThumb() {
        return cached.getCustomIcon().getAbsoluteFileName();
    }

    public void setThumb(String thumb) throws IOException {
        throw new RuntimeException(Utils.WIP_MESSAGE);
    }

    public void sync() throws IOException {
        throw new RuntimeException(Utils.WIP_MESSAGE);
    }

    private long getSeed() {
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        try {
            crc.update(cached.getValue().getBytes("UTF-8"));
            crc.update(getDefaultTitle().getBytes("UTF-8"));
        } catch (Exception e) {
            LogUtil.logError(logger, "UTF-8 doesn't exists", e);
            crc.update(cached.getValue().getBytes());
            crc.update(getDefaultTitle().getBytes());
        }
        return crc.getValue();
    }

    @Override
    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public List<MediaFacade> getSampleMedia(int max) throws IOException {
        List<MediaFacade> alreadyIn = new ArrayList<MediaFacade>();
        alreadyIn.add(getIcon());
        List<MediaFacade> res = new ArrayList<MediaFacade>();
        List<MediaFacade> overall = getMediaList();
        List<MediaFacade> remains = new ArrayList<MediaFacade>();
        String f = "";
        for (MediaFacade me : overall) {
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
            MediaFacade tba = overall.get(overall.size() - 1);
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

        VirtualAlbumFacade va;

        public ComputationReport(VirtualAlbumFacade va) {
            this.va = va;
        }

        public VirtualAlbumFacade getVA() {
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
        return cached.getId().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof VirtualAlbumFacade) {
            return ((VirtualAlbumFacade) o).cached.getId().equals(this.cached.getId());
        }
        return false;
    }

    public LocalizedMessage getTitle() {
        return new LocalizedMessageImpl(cached.getTitle(), "");
    }

    @AccessControl(required = { Permission.PICS_VIEW_REGULAR })
    public String getDefaultTitle() {
        try {
            return getTitle().getDefaultValue();
        } catch (IOException e) {
            LogUtil.logError(logger, e);
        }
        return "";
    }

    @Override
    public boolean exists() throws IOException {
        return true;
    }
}
