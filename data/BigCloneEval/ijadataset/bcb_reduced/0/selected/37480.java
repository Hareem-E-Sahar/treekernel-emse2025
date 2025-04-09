package org.cmsuite2.core.util.streamer.reader.supplier;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cmsuite2.core.enumeration.MediaRequestType;
import org.cmsuite2.core.model.supplier.Supplier;
import org.cmsuite2.core.model.supplier.SupplierDAO;
import org.cmsuite2.core.util.streamer.HttpCode;
import org.cmsuite2.core.util.streamer.MediaBean;
import org.cmsuite2.core.util.streamer.reader.IReader;

public class SupplierReader implements IReader {

    private static Logger logger = Logger.getLogger(SupplierReader.class);

    private SupplierDAO supplierDao;

    @Override
    public MediaBean getVideo(MediaRequestType type, long id, boolean force, SimpleDateFormat sdf, Locale currLocale, String modStr) {
        if (logger.isDebugEnabled()) logger.debug("getVideo()");
        return null;
    }

    @Override
    public MediaBean getAudio(MediaRequestType type, long id, boolean force, SimpleDateFormat sdf, Locale currLocale, String modStr) {
        if (logger.isDebugEnabled()) logger.debug("getAudio()");
        return null;
    }

    @Override
    public MediaBean getImage(MediaRequestType type, long id, boolean force, SimpleDateFormat sdf, Locale currLocale, String modStr) {
        if (logger.isDebugEnabled()) logger.debug("getImage()");
        Supplier supplier = supplierDao.findById(id);
        if (logger.isDebugEnabled()) logger.debug("Supplier: " + supplier);
        MediaBean mb = new MediaBean();
        if (supplier == null) {
            mb.getCodes().add(new HttpCode(404));
            logger.error(new HttpCode(404));
            return mb;
        }
        String contentType = null;
        byte[] content = null;
        Date lastMod = null;
        contentType = supplier.getPreviewContentType();
        content = supplier.getPreviewContent();
        lastMod = supplier.getPreviewMod();
        mb.setContentType(contentType);
        mb.setContent(content);
        if (!force) if (StringUtils.isNotEmpty(modStr)) {
            Date ims = null;
            try {
                ims = sdf.parse(modStr);
            } catch (ParseException e) {
                logger.error(e, e);
            }
            if (!ims.before(lastMod)) {
                mb.getCodes().add(new HttpCode(304));
                logger.warn(new HttpCode(304));
                return mb;
            }
        }
        mb.setLastMod(sdf.format(lastMod));
        return mb;
    }

    public SupplierDAO getSupplierDao() {
        return supplierDao;
    }

    public void setSupplierDao(SupplierDAO supplierDao) {
        this.supplierDao = supplierDao;
    }
}
