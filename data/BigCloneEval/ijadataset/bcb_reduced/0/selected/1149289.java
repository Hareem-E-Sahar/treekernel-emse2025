package com.dotmarketing.servlets;

import static com.dotmarketing.business.PermissionAPI.PERMISSION_READ;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.ParseException;
import org.apache.struts.Globals;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.view.context.ChainedContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;
import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Identifier;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.PermissionAPI;
import com.dotmarketing.business.web.UserWebAPI;
import com.dotmarketing.business.web.WebAPILocator;
import com.dotmarketing.cache.FileCache;
import com.dotmarketing.cache.IdentifierCache;
import com.dotmarketing.cache.LanguageCache;
import com.dotmarketing.cache.LiveCache;
import com.dotmarketing.cache.VirtualLinksCache;
import com.dotmarketing.cms.wiki.utils.WikiUtils;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotHibernateException;
import com.dotmarketing.exception.DotRuntimeException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.factories.HostFactory;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;
import com.dotmarketing.portlets.files.factories.FileFactory;
import com.dotmarketing.portlets.languagesmanager.factories.LanguageFactory;
import com.dotmarketing.portlets.languagesmanager.model.Language;
import com.dotmarketing.portlets.structure.factories.StructureFactory;
import com.dotmarketing.portlets.structure.model.Structure;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.dotmarketing.util.VelocityUtil;
import com.dotmarketing.util.WebKeys;
import com.dotmarketing.velocity.VelocityServlet;
import com.liferay.portal.PortalException;
import com.liferay.portal.SystemException;
import com.liferay.portal.model.User;
import com.lowagie.text.DocumentException;

/**
 * @author Jason Tesser
 * @author Andres Olarte
 * @since 1.6.5
 * 
 */
public class HTMLPDFServlet extends VelocityServlet {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private PermissionAPI perAPI;

    private User user;

    Map<String, String> map = new HashMap<String, String>();

    private ServletContext context;

    private List<String> hostList;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fName = req.getParameter("fname");
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=" + fName);
        HttpSession session = req.getSession();
        String reqURI = req.getRequestURI();
        Logger.debug(this, "Starting PDFServlet at URI " + reqURI);
        String language = String.valueOf(LanguageFactory.getDefaultLanguage().getId());
        if (session.isNew() || !UtilMethods.isSet((String) session.getAttribute(com.dotmarketing.util.WebKeys.HTMLPAGE_LANGUAGE))) {
            Logger.debug(VelocityServlet.class, "session new: " + session.isNew());
            Language l = LanguageCache.getLanguageById(language);
            Locale locale = new Locale(l.getLanguageCode() + "_" + l.getCountryCode().toUpperCase());
            session.setAttribute(Globals.LOCALE_KEY, locale);
            session.setAttribute(com.dotmarketing.util.WebKeys.HTMLPAGE_LANGUAGE, language);
        }
        if (UtilMethods.isSet(req.getParameter(com.dotmarketing.util.WebKeys.HTMLPAGE_LANGUAGE)) || UtilMethods.isSet(req.getParameter("language_id"))) {
            if (UtilMethods.isSet(req.getParameter(com.dotmarketing.util.WebKeys.HTMLPAGE_LANGUAGE))) {
                language = req.getParameter(com.dotmarketing.util.WebKeys.HTMLPAGE_LANGUAGE);
            } else {
                language = req.getParameter("language_id");
            }
            Language l = LanguageFactory.getLanguage(language);
            Locale locale = new Locale(l.getLanguageCode() + "_" + l.getCountryCode());
            session.setAttribute(Globals.LOCALE_KEY, locale);
            session.setAttribute(com.dotmarketing.util.WebKeys.HTMLPAGE_LANGUAGE, language);
        }
        language = (String) session.getAttribute(com.dotmarketing.util.WebKeys.HTMLPAGE_LANGUAGE);
        UserWebAPI uAPI = WebAPILocator.getUserWebAPI();
        user = null;
        try {
            user = uAPI.getLoggedInUser(req);
        } catch (DotRuntimeException e2) {
            Logger.debug(this, "DotRuntimeException: " + e2.getMessage(), e2);
        } catch (PortalException e2) {
            Logger.debug(this, "PortalException: " + e2.getMessage(), e2);
        } catch (SystemException e2) {
            Logger.debug(this, "SystemException: " + e2.getMessage(), e2);
        }
        if (user != null) {
            Logger.debug(this, "The user is " + user.getUserId());
        } else {
            Logger.debug(this, "The user is null");
        }
        boolean working = false;
        boolean live = false;
        String VELOCITY_HTMLPAGE_EXTENSION = Config.getStringProperty("VELOCITY_HTMLPAGE_EXTENSION");
        String location = "live";
        if (working) {
            location = "working";
        }
        Logger.debug(this, "The location is " + location);
        boolean PREVIEW_MODE = ((session.getAttribute(com.dotmarketing.util.WebKeys.PREVIEW_MODE_SESSION) != null));
        boolean EDIT_MODE = ((session.getAttribute(com.dotmarketing.util.WebKeys.EDIT_MODE_SESSION) != null));
        if (PREVIEW_MODE || EDIT_MODE) {
            working = true;
            Logger.debug(this, "Working is true");
        } else {
            live = true;
            Logger.debug(this, "Live is true");
        }
        if (EDIT_MODE) {
            try {
                user = uAPI.getLoggedInUser(req);
            } catch (DotRuntimeException e) {
                Logger.debug(this, "DotRuntimeException: " + e.getMessage(), e);
            } catch (PortalException e) {
                Logger.debug(this, "PortalException: " + e.getMessage(), e);
            } catch (SystemException e) {
                Logger.debug(this, "PortalException: " + e.getMessage(), e);
            }
        }
        perAPI = APILocator.getPermissionAPI();
        String pageID = req.getParameter("_dot_pdfpage");
        if (pageID == null || pageID.length() < 1) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Logger.debug(this, "Page to pdf is " + pageID);
        Identifier ident = null;
        Host host = null;
        String pageHostId = req.getParameter("host_id");
        if (pageHostId != null && (EDIT_MODE || PREVIEW_MODE) && session != null) {
            host = HostFactory.getHost(pageHostId);
            HostFactory.setHostInRequest(req, host);
            session.setAttribute(WebKeys.CURRENT_HOST, host);
        } else {
            host = HostFactory.getCurrentHost(req, EDIT_MODE);
        }
        try {
            Long id = Long.valueOf(pageID);
            ident = IdentifierCache.getIdentifierFromIdentifierCache(id);
        } catch (NumberFormatException e2) {
            boolean external = false;
            String uri = pageID;
            String pointer = null;
            if (uri.endsWith("/")) uri = uri.substring(0, uri.length() - 1);
            pointer = VirtualLinksCache.getPathFromCache(host.getHostname() + ":" + uri);
            if (!UtilMethods.isSet(pointer)) {
                pointer = VirtualLinksCache.getPathFromCache(uri);
            }
            if (UtilMethods.isSet(pointer)) {
                Logger.debug(this, "CMS found virtual link pointer = " + uri + ":" + pointer);
                String auxPointer = pointer;
                if (auxPointer.indexOf("http://") != -1 || auxPointer.indexOf("https://") != -1) {
                    auxPointer = pointer.replace("https://", "");
                    auxPointer = pointer.replace("http://", "");
                    int startIndex = 0;
                    int endIndex = auxPointer.indexOf("/");
                    if (startIndex < endIndex) {
                        String localHostName = auxPointer.substring(startIndex, endIndex);
                        Host localHost = HostFactory.getHostByHostName(localHostName);
                        external = (localHost.getInode() == 0 ? true : false);
                    } else {
                        external = true;
                        pageID = pointer;
                        uri = pointer;
                    }
                }
                if (!external) {
                    String ext = Config.getStringProperty("VELOCITY_PAGE_EXTENSION");
                    if (!pointer.endsWith(ext)) {
                        if (!pointer.endsWith("/")) pointer += "/";
                        pointer += "index." + ext;
                    }
                    pageID = pointer;
                    uri = pointer;
                }
            }
            if (pageID.startsWith("http://") || pageID.startsWith("https://")) {
                if (!external) {
                    URL url = new URL(pageID);
                    if (!hostList.contains(url.getHost())) {
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                }
                ITextRenderer renderer = new ITextRenderer();
                renderer.setDocument(pageID);
                Logger.debug(this, "Calling iText render");
                renderer.layout();
                try {
                    Logger.debug(this, "Using iText to Create PDF");
                    renderer.createPDF(resp.getOutputStream());
                } catch (DocumentException e) {
                    Logger.error(this, e.getMessage(), e);
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                return;
            }
            String[] path = pageID.split("/");
            if (path.length > 2) {
                String wiki = map.get("/" + path[1]);
                String wikiName = pageID.substring(1);
                wikiName = wikiName.substring(wikiName.indexOf("/") + 1);
                String title = WikiUtils.normalizeTitle(wikiName);
                if (wiki != null) {
                    String struct = wiki.split("\\|")[0];
                    String field = wiki.split("\\|")[1];
                    ContentletAPI capi = APILocator.getContentletAPI();
                    String query = "+structureInode:" + struct + " +" + field + ":\"" + title + "\" +languageId:1* +deleted:false +live:true";
                    List<com.dotmarketing.portlets.contentlet.model.Contentlet> cons = null;
                    try {
                        cons = capi.search(query, 1, 0, "text1", user, true);
                    } catch (DotDataException e) {
                        Logger.debug(this, "DotDataException: " + e.getMessage(), e);
                    } catch (DotSecurityException e) {
                        Logger.debug(this, "DotSecurityException: " + e.getMessage(), e);
                    } catch (ParseException e) {
                        Logger.debug(this, "ParseException: " + e.getMessage(), e);
                    }
                    if (cons != null && cons.size() > 0) {
                        com.dotmarketing.portlets.contentlet.model.Contentlet c = cons.get(0);
                        req.setAttribute(WebKeys.WIKI_CONTENTLET, c.getIdentifier());
                    } else {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                    }
                    pageID = "/" + path[1] + "/index." + Config.getStringProperty("VELOCITY_PAGE_EXTENSION");
                    uri = pageID;
                }
            }
            if (pageID.endsWith("/")) {
                uri = pageID + "index." + Config.getStringProperty("VELOCITY_PAGE_EXTENSION");
            } else {
                if (!pageID.endsWith("." + Config.getStringProperty("VELOCITY_PAGE_EXTENSION"))) {
                    uri = pageID + "/index." + Config.getStringProperty("VELOCITY_PAGE_EXTENSION");
                }
            }
            ident = IdentifierCache.getPathFromIdCache(uri, host);
        } catch (DotHibernateException e1) {
            Logger.debug(this, "DotHibernateException: " + e1.getMessage(), e1);
        }
        if (ident == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (ident.getInode() == 0) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String url = ident.getURI();
        url = url.substring(0, url.lastIndexOf("/")) + "/";
        if (!perAPI.doesUserHavePermission(ident, PermissionAPI.PERMISSION_READ, user, true)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
        StringWriter sw = new StringWriter();
        Context context = VelocityServlet.createContext(req, resp);
        context.put("pdfExport", true);
        String pageIdent = ident.getInode() + "";
        try {
            VelocityEngine ve = VelocityUtil.getEngine();
            ve.getTemplate("/" + location + "/" + pageIdent + "." + VELOCITY_HTMLPAGE_EXTENSION).merge(context, sw);
            ITextRenderer renderer = new ITextRenderer();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            String s = sw.toString();
            s = processCSSPath(s, host, "css", "\\(", "\\)", ")", url);
            s = processCSSPath(s, host, "css", "\\\"", "\\\"", "\"", url);
            Tidy tidy = new Tidy();
            tidy.setXHTML(true);
            ByteArrayInputStream is = new ByteArrayInputStream(s.getBytes());
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            tidy.parse(is, os);
            s = os.toString();
            is = new ByteArrayInputStream(s.getBytes());
            Document doc = builder.parse(is);
            NodeList nl = doc.getElementsByTagName("img");
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                Node srcNode = n.getAttributes().getNamedItem("src");
                String srcText = srcNode.getNodeValue();
                String newText = getRealPath(srcText, host, url);
                String cleanText = cleanPath(newText);
                srcNode.setNodeValue(cleanText);
            }
            renderer.setDocument(doc, null);
            renderer.layout();
            renderer.createPDF(resp.getOutputStream());
        } catch (ParseErrorException e) {
            Logger.error(this, "ParseErrorException: " + e.getMessage(), e);
        } catch (ResourceNotFoundException e) {
            Logger.error(this, "ResourceNotFoundException: " + e.getMessage(), e);
        } catch (MethodInvocationException e) {
            Logger.error(this, "MethodInvocationException: " + e.getMessage(), e);
        } catch (Exception e) {
            Logger.error(this, "Exception: " + e.getMessage(), e);
        }
    }

    private String getRealPath(String path, Host host, String url) throws NumberFormatException, DotHibernateException {
        String relativePath = null;
        if (path.toLowerCase().startsWith("http://") || path.toLowerCase().startsWith("https://")) {
            return path;
        }
        if (!path.startsWith("/")) {
            path = cleanPath(url + path);
        }
        if (path.startsWith("/dotAsset/")) {
            if (path.startsWith("/dotAsset?path=")) {
                int index = path.indexOf("/dotAsset?path=");
                relativePath = path.substring(index);
            } else {
                String identifier = path.substring(path.lastIndexOf("/") + 1, path.indexOf("."));
                Identifier ident = IdentifierCache.getIdentifierFromIdentifierCache(Long.parseLong(identifier));
                relativePath = LiveCache.getPathFromCache(ident.getURI(), ident.getHostInode());
            }
        } else if (path.startsWith("/resize_image?")) {
            Logger.debug(this, "Fixing resize image servlet");
            return "http://" + host.getHostname() + path;
        } else {
            relativePath = LiveCache.getPathFromCache(path, host);
        }
        File f = null;
        if (relativePath == null) {
            f = new File(context.getRealPath(path));
        } else {
            f = new File(FileFactory.getRealAssetsRootPath() + relativePath);
        }
        if (!f.exists()) {
            Logger.warn(this, "Invalid path passed: path = " + relativePath + ", file doesn't exists.");
        }
        return ("file:///" + f.getAbsolutePath());
    }

    private String processCSSPath(String text, Host host, String extension, String delimiter1, String delimiter2, String endDelimiter, String url) throws DotHibernateException {
        Pattern p = Pattern.compile(delimiter1 + "[^" + delimiter1 + "]*\\." + extension + delimiter2);
        Matcher m = p.matcher(text);
        StringBuilder sb = new StringBuilder();
        int prevIndex = 0;
        while (m.find()) {
            String match = m.group();
            match = match.substring(1, match.length() - 1);
            sb.append(text.substring(prevIndex, m.start() + 1));
            prevIndex = m.end();
            if (match.toLowerCase().startsWith("http://") || match.toLowerCase().startsWith("https://")) {
                sb.append(match + endDelimiter);
                sb.append(text.substring(prevIndex));
                return sb.toString();
            }
            if (!match.startsWith("/")) {
                match = url + match;
                match = cleanPath(match);
            }
            String relativePath = LiveCache.getPathFromCache(match, host);
            File f = new File(FileFactory.getRealAssetsRootPath() + relativePath);
            if (!f.exists()) {
                Logger.warn(this, "Invalid path passed: path = " + relativePath + ", file doesn't exists.");
                f = new File(context.getRealPath(match));
                if (f.exists()) {
                    sb.append("file:///" + f.getAbsolutePath() + endDelimiter);
                }
            } else {
                String inode = UtilMethods.getFileName(f.getName());
                com.dotmarketing.portlets.files.model.File file = FileCache.getFileByInode(inode);
                Identifier identifier = IdentifierCache.getIdentifierFromIdentifierCache(file);
                if (!perAPI.doesUserHavePermission(identifier, PERMISSION_READ, user)) {
                    Logger.warn(this, "Not authorized: path = " + relativePath);
                } else {
                    String path = f.getAbsolutePath();
                    sb.append("file:///" + path + endDelimiter);
                }
            }
        }
        sb.append(text.substring(prevIndex));
        return sb.toString();
    }

    private String cleanPath(String path) {
        if (!path.contains("..")) {
            return path;
        }
        int index = path.indexOf("..");
        String prev = path.substring(0, index - 1);
        String post = path.substring(index + 2);
        int lastIndex = prev.lastIndexOf("/");
        int lastIndex2 = prev.lastIndexOf("\\");
        if (lastIndex > 0 || lastIndex2 > 0) {
            if (lastIndex2 > lastIndex) {
                lastIndex = lastIndex2;
            }
            prev = prev.substring(0, lastIndex);
        } else {
            prev = "";
        }
        return cleanPath(prev + post);
    }

    @Override
    protected void _setClientVariablesOnContext(HttpServletRequest request, ChainedContext context) {
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        context = config.getServletContext();
        hostList = new ArrayList<String>();
        String hosts = config.getInitParameter("external-hosts");
        if (hosts != null) {
            hosts = hosts.replace(" ", "");
            for (String s : hosts.split(",")) {
                hostList.add(s);
            }
        }
        List<Host> systemHosts = HostFactory.getAllHosts();
        for (Host h : systemHosts) {
            String s = h.getHostname();
            hostList.add(s);
        }
        for (int i = 0; i < 100; i++) {
            String url = Config.getStringProperty("dotcms.wiki" + i + ".uri");
            String structure = Config.getStringProperty("dotcms.wiki" + i + ".structure");
            String field = Config.getStringProperty("dotcms.wiki" + i + ".field");
            if (!UtilMethods.isSet(url) || !UtilMethods.isSet(structure) || !UtilMethods.isSet(field)) {
                break;
            }
            List<Structure> l = StructureFactory.getStructures();
            for (Structure struct : l) {
                if (structure.equals(struct.getName())) {
                    map.put(url, struct.getInode() + "|" + field);
                }
            }
        }
    }
}
