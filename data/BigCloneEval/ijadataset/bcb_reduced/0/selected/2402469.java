package com.sts.webmeet.server.servlets;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import com.sts.webmeet.common.IOUtil;
import com.sts.webmeet.server.PlaybackConstants;
import com.sts.webmeet.web.Constants;
import com.sts.webmeet.server.interfaces.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class StreamServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(StreamServlet.class);

    public void init(ServletConfig config) throws ServletException {
        super.init();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            logger.info(".doGet");
            if (null == request.getSession().getAttribute(Constants.USER_KEY) || null == request.getParameter("confID")) {
                logger.error("user session not found or conf not specified.");
                return;
            }
            String strConfID = request.getParameter("confID");
            if (!customerOwnsMeeting(getCustomerID(request), strConfID)) {
                throw new Exception("Error: customer does not own meeting");
            }
            String strRecordingRoot = System.getProperty(PlaybackConstants.RECORDINGS_DIR_PROPERTY, PlaybackConstants.RECORDINGS_DIR_DEFAULT);
            File fileRecordingDir = new File(strRecordingRoot + "/" + strConfID);
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=" + PlaybackConstants.getArchiveForConf(strConfID));
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=0");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            File fileArchive = new File(fileRecordingDir.getAbsolutePath() + "/" + PlaybackConstants.getArchiveForConf(strConfID));
            logger.info("length: " + fileArchive.length());
            response.setContentLength((int) fileArchive.length());
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileArchive));
            OutputStream os = response.getOutputStream();
            logger.info("response.getOutputStream():" + os);
            IOUtil.copyStream(bis, os);
            os.flush();
            os.close();
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean customerOwnsMeeting(String strCustomerID, String strMeetingID) throws Exception {
        MeetingLocal meeting = MeetingUtil.getLocalHome().findByPrimaryKey(new Integer(strMeetingID));
        return (strCustomerID.equals(meeting.getCustomer().getCustomerId() + ""));
    }

    private String getCustomerID(HttpServletRequest request) throws Exception {
        CustomerData data = (CustomerData) (request.getSession(false).getAttribute(Constants.USER_KEY));
        return "" + data.getCustomerId();
    }

    private void writeToZip(InputStream is, String entry, ZipOutputStream zos) throws java.io.IOException {
        zos.putNextEntry(new ZipEntry(entry));
        for (int iRead = is.read(); iRead != -1; iRead = is.read()) {
            zos.write(iRead);
        }
    }
}
