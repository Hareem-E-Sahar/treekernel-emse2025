package net.sundog.hoople.servlet;

import org.apache.tools.ant.DirectoryScanner;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.GZIPOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sundog.hoople.Hoople;
import net.sundog.hoople.GoogleSiteMapParser;

public class GoogleSitemapServlet extends HttpServlet {

    private List includes = new ArrayList();

    private List excludes = new ArrayList();

    private String configurationSupportClass;

    public GoogleSitemapServlet() {
    }

    public void init(ServletConfig servletConfig) throws ServletException {
        String includeString = servletConfig.getInitParameter("include");
        StringTokenizer includeTokenizer;
        if (includeString != null) {
            includeTokenizer = new StringTokenizer(includeString.replaceAll(",\\s*$", ""), ",");
            while (includeTokenizer.hasMoreElements()) {
                this.includes.add(((String) includeTokenizer.nextElement()).trim());
            }
        }
        String excludeString = servletConfig.getInitParameter("exclude");
        if (excludeString != null) {
            StringTokenizer excludeTokenizer = new StringTokenizer(excludeString.replaceAll(",\\s*$", ""), ",");
            while (excludeTokenizer.hasMoreElements()) {
                this.excludes.add(((String) excludeTokenizer.nextElement()).trim());
            }
        }
        configurationSupportClass = servletConfig.getInitParameter("configurationSupportClass");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/xml");
        try {
            Hoople hoople = new Hoople();
            Matcher urlMatcher = Pattern.compile("(\\w+://[^/]+).*").matcher(request.getRequestURL().toString());
            if (!urlMatcher.matches()) {
                throw new ServletException("Could not determine base url");
            }
            String urlBase = urlMatcher.group(1);
            GoogleSiteMapParser siteMapSupport = getConfigurationSupport(urlBase);
            hoople.getHoopleParsers().add(siteMapSupport);
            hoople.setDocumentRoot(new File(request.getSession().getServletContext().getRealPath("/")));
            DirectoryScanner ds = new DirectoryScanner();
            ds.setBasedir(hoople.getDocumentRoot());
            ds.setIncludes((String[]) includes.toArray(new String[includes.size()]));
            ds.setExcludes((String[]) excludes.toArray(new String[excludes.size()]));
            ds.scan();
            hoople.run(ds.getIncludedFiles());
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            OutputStream outputStream = response.getOutputStream();
            if (request.getRequestURI().endsWith(".gz")) {
                response.addHeader("Content-Encoding", "gzip");
                outputStream = new GZIPOutputStream(response.getOutputStream());
                transformer.transform(new DOMSource(siteMapSupport.getSiteMap()), new StreamResult(outputStream));
                ((GZIPOutputStream) outputStream).finish();
            } else {
                transformer.transform(new DOMSource(siteMapSupport.getSiteMap()), new StreamResult(outputStream));
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private GoogleSiteMapParser getConfigurationSupport(String urlBase) throws Exception {
        if (configurationSupportClass == null) {
            return new GoogleSiteMapParser(urlBase);
        } else {
            try {
                Class supportClass = Class.forName(configurationSupportClass);
                if (!GoogleSiteMapParser.class.isAssignableFrom(supportClass)) {
                    throw new ServletException(supportClass.getName() + " is not assignable from " + GoogleSiteMapParser.class.getName());
                }
                return (GoogleSiteMapParser) supportClass.getConstructor(new Class[] { String.class }).newInstance(new String[] { urlBase });
            } catch (ServletException e) {
                throw e;
            } catch (Exception e) {
                throw new ServletException("Could not instantiate class " + configurationSupportClass);
            }
        }
    }
}
