package org.ajaxaio.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ajaxaio.AjaxManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * @author qiangli
 * 
 */
public class AjaxController extends AbstractController {

    private final Log logger = LogFactory.getLog(getClass());

    private AjaxManager ajaxManager = null;

    private boolean compress = false;

    public AjaxManager getAjaxManager() {
        return this.ajaxManager;
    }

    public void setAjaxManager(AjaxManager ajaxManager) {
        this.ajaxManager = ajaxManager;
    }

    public boolean isCompress() {
        return this.compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            String userAgent = request.getHeader("User-Agent");
            String referer = request.getHeader("Referer");
            String acceptEncoding = request.getHeader("Accept-Encoding");
            boolean supportGzip = false;
            if (acceptEncoding != null && acceptEncoding.toLowerCase().indexOf("gzip") != -1) {
                supportGzip = true;
            }
            logger.debug("User-Agent: " + userAgent + " Referer: " + referer + " Accept-Encoding: " + acceptEncoding + " supportZip: " + supportGzip);
            String uri = request.getRequestURI();
            String ctxpath = request.getContextPath();
            ServletContext sc = getServletContext();
            String root = sc.getRealPath("/");
            String filename = uri.substring(ctxpath.length());
            logger.debug("uri: " + uri + " contextPath: " + ctxpath + " root: " + root + " filename: " + filename);
            File file = ajaxManager.getFile(root, ctxpath, filename);
            String mimeType = sc.getMimeType(file.getAbsolutePath());
            logger.debug("file: " + file + " mimeType: " + mimeType);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
                logger.debug("Unknown MIME type: " + file);
            }
            if (isCompress() && mimeType.equals("text/javascript")) {
                file = ajaxManager.compress(file);
                logger.debug("filename: " + filename + " compressed file: " + file);
            }
            if (supportGzip && mimeType.startsWith("text/")) {
                try {
                    file = ajaxManager.gzip(file);
                    response.setHeader("Content-Encoding", "gzip");
                    logger.debug("filename: " + filename + " gzip file: " + file);
                } catch (Exception e) {
                    logger.error(e);
                }
            }
            response.setContentType(mimeType);
            response.setContentLength((int) file.length());
            FileInputStream in = new FileInputStream(file);
            OutputStream out = response.getOutputStream();
            byte[] buf = new byte[4096];
            int nread = 0;
            while ((nread = in.read(buf)) >= 0) {
                out.write(buf, 0, nread);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            logger.error(e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
        }
        return null;
    }
}
