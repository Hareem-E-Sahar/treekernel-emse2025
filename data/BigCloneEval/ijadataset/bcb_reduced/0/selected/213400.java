package cz.fi.muni.xkremser.editor.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.inject.Inject;
import com.google.inject.Injector;
import org.apache.log4j.Logger;
import cz.fi.muni.xkremser.editor.client.util.Constants;
import cz.fi.muni.xkremser.editor.server.config.EditorConfiguration;
import cz.fi.muni.xkremser.editor.server.fedora.utils.IOUtils;
import cz.fi.muni.xkremser.editor.server.fedora.utils.RESTHelper;

/**
 * @author Matous Jobanek
 * @version $Id$
 */
public class DownloadFoxmlServlet extends HttpServlet {

    private static final long serialVersionUID = -1863406403841249392L;

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(DownloadFoxmlServlet.class.getPackage().toString());

    /** The configuration. */
    @Inject
    private EditorConfiguration config;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uuid = req.getParameterValues(Constants.PARAM_UUID)[0];
        String datastream = null;
        if (req.getRequestURI().contains(Constants.SERVLET_DOWNLOAD_FOXML_PREFIX)) {
            resp.addHeader("Content-Disposition", "attachment; ContentType = \"text/xml\"; filename=\"" + uuid + "_local_version.foxml\"");
        } else {
            datastream = req.getParameterValues(Constants.PARAM_DATASTREAM)[0];
            resp.addHeader("Content-Disposition", "attachment; ContentType = \"text/xml\"; filename=\"" + uuid + "_local_version_" + datastream + ".xml\"");
        }
        String xmlContent = URLDecoder.decode(req.getParameterValues(Constants.PARAM_CONTENT)[0], "UTF-8");
        InputStream is = new ByteArrayInputStream(xmlContent.getBytes("UTF-8"));
        ServletOutputStream os = resp.getOutputStream();
        IOUtils.copyStreams(is, os);
        os.flush();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uuid = req.getParameterValues(Constants.PARAM_UUID)[0];
        String datastream = null;
        if (req.getRequestURI().contains(Constants.SERVLET_DOWNLOAD_FOXML_PREFIX)) {
            resp.addHeader("Content-Disposition", "attachment; ContentType = \"text/xml\"; filename=\"" + uuid + "_server_version.foxml\"");
        } else {
            datastream = req.getParameterValues(Constants.PARAM_DATASTREAM)[0];
            resp.addHeader("Content-Disposition", "attachment; ContentType = \"text/xml\"; filename=\"" + uuid + "_server_version_" + datastream + ".xml\"");
        }
        ServletOutputStream os = resp.getOutputStream();
        if (uuid != null && !"".equals(uuid)) {
            try {
                StringBuffer sb = new StringBuffer();
                if (req.getRequestURI().contains(Constants.SERVLET_DOWNLOAD_FOXML_PREFIX)) {
                    sb.append(config.getFedoraHost()).append("/objects/").append(uuid).append("/objectXML");
                } else if (req.getRequestURI().contains(Constants.SERVLET_DOWNLOAD_DATASTREAMS_PREFIX)) {
                    sb.append(config.getFedoraHost()).append("/objects/").append(uuid).append("/datastreams/").append(datastream).append("/content");
                }
                InputStream is = RESTHelper.get(sb.toString(), config.getFedoraLogin(), config.getFedoraPassword(), false);
                if (is == null) {
                    return;
                }
                try {
                    if (req.getRequestURI().contains(Constants.SERVLET_DOWNLOAD_DATASTREAMS_PREFIX)) {
                        os.write(Constants.XML_HEADER_WITH_BACKSLASHES.getBytes());
                    }
                    IOUtils.copyStreams(is, os);
                } catch (IOException e) {
                    resp.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
                    LOGGER.error("Problem with downloading foxml.", e);
                } finally {
                    os.flush();
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            resp.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
                            LOGGER.error("Problem with downloading foxml.", e);
                        } finally {
                            is = null;
                        }
                    }
                }
            } catch (IOException e) {
                resp.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
                LOGGER.error("Problem with downloading foxml.", e);
            } finally {
                os.flush();
            }
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        Injector injector = getInjector();
        injector.injectMembers(this);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        Injector injector = getInjector();
        injector.injectMembers(this);
    }

    /**
     * Gets the injector.
     * 
     * @return the injector
     */
    protected Injector getInjector() {
        return (Injector) getServletContext().getAttribute(Injector.class.getName());
    }
}
