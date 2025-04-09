package org.cmsuite2.core.util.streamer.reader.employee;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cmsuite2.core.enumeration.MediaRequestType;
import org.cmsuite2.core.model.employee.Employee;
import org.cmsuite2.core.model.employee.EmployeeDAO;
import org.cmsuite2.core.util.streamer.HttpCode;
import org.cmsuite2.core.util.streamer.MediaBean;
import org.cmsuite2.core.util.streamer.reader.IReader;

public class EmployeeReader implements IReader {

    private static Logger logger = Logger.getLogger(EmployeeReader.class);

    private EmployeeDAO employeeDao;

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
        Employee employee = employeeDao.findById(id);
        if (logger.isDebugEnabled()) logger.debug("Employee: " + employee);
        MediaBean mb = new MediaBean();
        if (employee == null) {
            mb.getCodes().add(new HttpCode(404));
            logger.error(new HttpCode(404));
            return mb;
        }
        String contentType = null;
        byte[] content = null;
        Date lastMod = null;
        contentType = employee.getPreviewContentType();
        content = employee.getPreviewContent();
        lastMod = employee.getPreviewMod();
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

    public EmployeeDAO getEmployeeDao() {
        return employeeDao;
    }

    public void setEmployeeDao(EmployeeDAO employeeDao) {
        this.employeeDao = employeeDao;
    }
}
