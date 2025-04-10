package org.apache.catalina.servlets;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.catalina.Globals;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.ServerInfo;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.util.URLEncoder;
import org.apache.naming.resources.CacheEntry;
import org.apache.naming.resources.ProxyDirContext;
import org.apache.naming.resources.Resource;
import org.apache.naming.resources.ResourceAttributes;

/**
 * The default resource-serving servlet for most web applications,
 * used to serve static resources such as HTML pages and images.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 620842 $ $Date: 2008-02-12 17:13:54 +0100 (Tue, 12 Feb 2008) $
 */
public class DefaultServlet extends HttpServlet {

    /**
     * The debugging detail level for this servlet.
     */
    protected int debug = 0;

    /**
     * The input buffer size to use when serving resources.
     */
    protected int input = 2048;

    /**
     * Should we generate directory listings?
     */
    protected boolean listings = false;

    /**
     * Read only flag. By default, it's set to true.
     */
    protected boolean readOnly = true;

    /**
     * The output buffer size to use when serving resources.
     */
    protected int output = 2048;

    /**
     * Array containing the safe characters set.
     */
    protected static URLEncoder urlEncoder;

    /**
     * Allow customized directory listing per directory.
     */
    protected String localXsltFile = null;

    /**
     * Allow customized directory listing per instance.
     */
    protected String globalXsltFile = null;

    /**
     * Allow a readme file to be included.
     */
    protected String readmeFile = null;

    /**
     * Proxy directory context.
     */
    protected ProxyDirContext resources = null;

    /**
     * File encoding to be used when reading static files. If none is specified
     * the platform default is used.
     */
    protected String fileEncoding = null;

    /**
     * Minimum size for sendfile usage in bytes.
     */
    protected int sendfileSize = 48 * 1024;

    /**
     * Full range marker.
     */
    protected static ArrayList FULL = new ArrayList();

    /**
     * GMT timezone - all HTTP dates are on GMT
     */
    static {
        urlEncoder = new URLEncoder();
        urlEncoder.addSafeCharacter('-');
        urlEncoder.addSafeCharacter('_');
        urlEncoder.addSafeCharacter('.');
        urlEncoder.addSafeCharacter('*');
        urlEncoder.addSafeCharacter('/');
    }

    /**
     * MIME multipart separation string
     */
    protected static final String mimeSeparation = "CATALINA_MIME_BOUNDARY";

    /**
     * JNDI resources name.
     */
    protected static final String RESOURCES_JNDI_NAME = "java:/comp/Resources";

    /**
     * The string manager for this package.
     */
    protected static StringManager sm = StringManager.getManager(Constants.Package);

    /**
     * Size of file transfer buffer in bytes.
     */
    protected static final int BUFFER_SIZE = 4096;

    /**
     * Finalize this servlet.
     */
    public void destroy() {
    }

    /**
     * Initialize this servlet.
     */
    public void init() throws ServletException {
        if (getServletConfig().getInitParameter("debug") != null) debug = Integer.parseInt(getServletConfig().getInitParameter("debug"));
        if (getServletConfig().getInitParameter("input") != null) input = Integer.parseInt(getServletConfig().getInitParameter("input"));
        if (getServletConfig().getInitParameter("output") != null) output = Integer.parseInt(getServletConfig().getInitParameter("output"));
        listings = Boolean.parseBoolean(getServletConfig().getInitParameter("listings"));
        if (getServletConfig().getInitParameter("readonly") != null) readOnly = Boolean.parseBoolean(getServletConfig().getInitParameter("readonly"));
        if (getServletConfig().getInitParameter("sendfileSize") != null) sendfileSize = Integer.parseInt(getServletConfig().getInitParameter("sendfileSize")) * 1024;
        fileEncoding = getServletConfig().getInitParameter("fileEncoding");
        globalXsltFile = getServletConfig().getInitParameter("globalXsltFile");
        localXsltFile = getServletConfig().getInitParameter("localXsltFile");
        readmeFile = getServletConfig().getInitParameter("readmeFile");
        if (input < 256) input = 256;
        if (output < 256) output = 256;
        if (debug > 0) {
            log("DefaultServlet.init:  input buffer size=" + input + ", output buffer size=" + output);
        }
        resources = (ProxyDirContext) getServletContext().getAttribute(Globals.RESOURCES_ATTR);
        if (resources == null) {
            try {
                resources = (ProxyDirContext) new InitialContext().lookup(RESOURCES_JNDI_NAME);
            } catch (NamingException e) {
                throw new ServletException("No resources", e);
            }
        }
        if (resources == null) {
            throw new UnavailableException("No resources");
        }
    }

    /**
     * Return the relative path associated with this servlet.
     *
     * @param request The servlet request we are processing
     */
    protected String getRelativePath(HttpServletRequest request) {
        if (request.getAttribute(Globals.INCLUDE_REQUEST_URI_ATTR) != null) {
            String result = (String) request.getAttribute(Globals.INCLUDE_PATH_INFO_ATTR);
            if (result == null) result = (String) request.getAttribute(Globals.INCLUDE_SERVLET_PATH_ATTR);
            if ((result == null) || (result.equals(""))) result = "/";
            return (result);
        }
        String result = request.getPathInfo();
        if (result == null) {
            result = request.getServletPath();
        }
        if ((result == null) || (result.equals(""))) {
            result = "/";
        }
        return (result);
    }

    /**
     * Process a GET request for the specified resource.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        serveResource(request, response, true);
    }

    /**
     * Process a HEAD request for the specified resource.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        serveResource(request, response, false);
    }

    /**
     * Process a POST request for the specified resource.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        doGet(request, response);
    }

    /**
     * Process a POST request for the specified resource.
     *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (readOnly) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String path = getRelativePath(req);
        boolean exists = true;
        try {
            resources.lookup(path);
        } catch (NamingException e) {
            exists = false;
        }
        boolean result = true;
        File contentFile = null;
        Range range = parseContentRange(req, resp);
        InputStream resourceInputStream = null;
        if (range != null) {
            contentFile = executePartialPut(req, range, path);
            resourceInputStream = new FileInputStream(contentFile);
        } else {
            resourceInputStream = req.getInputStream();
        }
        try {
            Resource newResource = new Resource(resourceInputStream);
            if (exists) {
                resources.rebind(path, newResource);
            } else {
                resources.bind(path, newResource);
            }
        } catch (NamingException e) {
            result = false;
        }
        if (result) {
            if (exists) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                resp.setStatus(HttpServletResponse.SC_CREATED);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_CONFLICT);
        }
    }

    /**
     * Handle a partial PUT.  New content specified in request is appended to
     * existing content in oldRevisionContent (if present). This code does
     * not support simultaneous partial updates to the same resource.
     */
    protected File executePartialPut(HttpServletRequest req, Range range, String path) throws IOException {
        File tempDir = (File) getServletContext().getAttribute("javax.servlet.context.tempdir");
        String convertedResourcePath = path.replace('/', '.');
        File contentFile = new File(tempDir, convertedResourcePath);
        if (contentFile.createNewFile()) {
            contentFile.deleteOnExit();
        }
        RandomAccessFile randAccessContentFile = new RandomAccessFile(contentFile, "rw");
        Resource oldResource = null;
        try {
            Object obj = resources.lookup(path);
            if (obj instanceof Resource) oldResource = (Resource) obj;
        } catch (NamingException e) {
            ;
        }
        if (oldResource != null) {
            BufferedInputStream bufOldRevStream = new BufferedInputStream(oldResource.streamContent(), BUFFER_SIZE);
            int numBytesRead;
            byte[] copyBuffer = new byte[BUFFER_SIZE];
            while ((numBytesRead = bufOldRevStream.read(copyBuffer)) != -1) {
                randAccessContentFile.write(copyBuffer, 0, numBytesRead);
            }
            bufOldRevStream.close();
        }
        randAccessContentFile.setLength(range.length);
        randAccessContentFile.seek(range.start);
        int numBytesRead;
        byte[] transferBuffer = new byte[BUFFER_SIZE];
        BufferedInputStream requestBufInStream = new BufferedInputStream(req.getInputStream(), BUFFER_SIZE);
        while ((numBytesRead = requestBufInStream.read(transferBuffer)) != -1) {
            randAccessContentFile.write(transferBuffer, 0, numBytesRead);
        }
        randAccessContentFile.close();
        requestBufInStream.close();
        return contentFile;
    }

    /**
     * Process a POST request for the specified resource.
     *
     * @param req The servlet request we are processing
     * @param resp The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (readOnly) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String path = getRelativePath(req);
        boolean exists = true;
        try {
            resources.lookup(path);
        } catch (NamingException e) {
            exists = false;
        }
        if (exists) {
            boolean result = true;
            try {
                resources.unbind(path);
            } catch (NamingException e) {
                result = false;
            }
            if (result) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Check if the conditions specified in the optional If headers are
     * satisfied.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resourceAttributes The resource information
     * @return boolean true if the resource meets all the specified conditions,
     * and false if any of the conditions is not satisfied, in which case
     * request processing is stopped
     */
    protected boolean checkIfHeaders(HttpServletRequest request, HttpServletResponse response, ResourceAttributes resourceAttributes) throws IOException {
        return checkIfMatch(request, response, resourceAttributes) && checkIfModifiedSince(request, response, resourceAttributes) && checkIfNoneMatch(request, response, resourceAttributes) && checkIfUnmodifiedSince(request, response, resourceAttributes);
    }

    /**
     * Get the ETag associated with a file.
     *
     * @param resourceAttributes The resource information
     */
    protected String getETag(ResourceAttributes resourceAttributes) {
        String result = null;
        if ((result = resourceAttributes.getETag(true)) != null) {
            return result;
        } else if ((result = resourceAttributes.getETag()) != null) {
            return result;
        } else {
            return "W/\"" + resourceAttributes.getContentLength() + "-" + resourceAttributes.getLastModified() + "\"";
        }
    }

    /**
     * URL rewriter.
     *
     * @param path Path which has to be rewiten
     */
    protected String rewriteUrl(String path) {
        return urlEncoder.encode(path);
    }

    /**
     * Display the size of a file.
     */
    protected void displaySize(StringBuffer buf, int filesize) {
        int leftside = filesize / 1024;
        int rightside = (filesize % 1024) / 103;
        if (leftside == 0 && rightside == 0 && filesize != 0) rightside = 1;
        buf.append(leftside).append(".").append(rightside);
        buf.append(" KB");
    }

    /**
     * Serve the specified resource, optionally including the data content.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param content Should the content be included?
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    protected void serveResource(HttpServletRequest request, HttpServletResponse response, boolean content) throws IOException, ServletException {
        String path = getRelativePath(request);
        if (debug > 0) {
            if (content) log("DefaultServlet.serveResource:  Serving resource '" + path + "' headers and data"); else log("DefaultServlet.serveResource:  Serving resource '" + path + "' headers only");
        }
        CacheEntry cacheEntry = resources.lookupCache(path);
        if (!cacheEntry.exists) {
            String requestUri = (String) request.getAttribute(Globals.INCLUDE_REQUEST_URI_ATTR);
            if (requestUri == null) {
                requestUri = request.getRequestURI();
            } else {
                response.getWriter().write(sm.getString("defaultServlet.missingResource", requestUri));
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND, requestUri);
            return;
        }
        if (cacheEntry.context == null) {
            if (path.endsWith("/") || (path.endsWith("\\"))) {
                String requestUri = (String) request.getAttribute(Globals.INCLUDE_REQUEST_URI_ATTR);
                if (requestUri == null) {
                    requestUri = request.getRequestURI();
                }
                response.sendError(HttpServletResponse.SC_NOT_FOUND, requestUri);
                return;
            }
        }
        if (cacheEntry.context == null) {
            boolean included = (request.getAttribute(Globals.INCLUDE_CONTEXT_PATH_ATTR) != null);
            if (!included && !checkIfHeaders(request, response, cacheEntry.attributes)) {
                return;
            }
        }
        String contentType = cacheEntry.attributes.getMimeType();
        if (contentType == null) {
            contentType = getServletContext().getMimeType(cacheEntry.name);
            cacheEntry.attributes.setMimeType(contentType);
        }
        ArrayList ranges = null;
        long contentLength = -1L;
        if (cacheEntry.context != null) {
            if (!listings) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
                return;
            }
            contentType = "text/html;charset=UTF-8";
        } else {
            ranges = parseRange(request, response, cacheEntry.attributes);
            response.setHeader("ETag", getETag(cacheEntry.attributes));
            response.setHeader("Last-Modified", cacheEntry.attributes.getLastModifiedHttp());
            contentLength = cacheEntry.attributes.getContentLength();
            if (contentLength == 0L) {
                content = false;
            }
        }
        ServletOutputStream ostream = null;
        PrintWriter writer = null;
        if (content) {
            try {
                ostream = response.getOutputStream();
            } catch (IllegalStateException e) {
                if ((contentType == null) || (contentType.startsWith("text")) || (contentType.endsWith("xml"))) {
                    writer = response.getWriter();
                } else {
                    throw e;
                }
            }
        }
        if ((cacheEntry.context != null) || (((ranges == null) || (ranges.isEmpty())) && (request.getHeader("Range") == null)) || (ranges == FULL)) {
            if (contentType != null) {
                if (debug > 0) log("DefaultServlet.serveFile:  contentType='" + contentType + "'");
                response.setContentType(contentType);
            }
            if ((cacheEntry.resource != null) && (contentLength >= 0)) {
                if (debug > 0) log("DefaultServlet.serveFile:  contentLength=" + contentLength);
                if (contentLength < Integer.MAX_VALUE) {
                    response.setContentLength((int) contentLength);
                } else {
                    response.setHeader("content-length", "" + contentLength);
                }
            }
            InputStream renderResult = null;
            if (cacheEntry.context != null) {
                if (content) {
                    renderResult = render(request.getContextPath(), cacheEntry);
                }
            }
            if (content) {
                try {
                    response.setBufferSize(output);
                } catch (IllegalStateException e) {
                }
                if (ostream != null) {
                    if (!checkSendfile(request, response, cacheEntry, contentLength, null)) copy(cacheEntry, renderResult, ostream);
                } else {
                    copy(cacheEntry, renderResult, writer);
                }
            }
        } else {
            if ((ranges == null) || (ranges.isEmpty())) return;
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            if (ranges.size() == 1) {
                Range range = (Range) ranges.get(0);
                response.addHeader("Content-Range", "bytes " + range.start + "-" + range.end + "/" + range.length);
                long length = range.end - range.start + 1;
                if (length < Integer.MAX_VALUE) {
                    response.setContentLength((int) length);
                } else {
                    response.setHeader("content-length", "" + length);
                }
                if (contentType != null) {
                    if (debug > 0) log("DefaultServlet.serveFile:  contentType='" + contentType + "'");
                    response.setContentType(contentType);
                }
                if (content) {
                    try {
                        response.setBufferSize(output);
                    } catch (IllegalStateException e) {
                    }
                    if (ostream != null) {
                        if (!checkSendfile(request, response, cacheEntry, range.end - range.start + 1, range)) copy(cacheEntry, ostream, range);
                    } else {
                        copy(cacheEntry, writer, range);
                    }
                }
            } else {
                response.setContentType("multipart/byteranges; boundary=" + mimeSeparation);
                if (content) {
                    try {
                        response.setBufferSize(output);
                    } catch (IllegalStateException e) {
                    }
                    if (ostream != null) {
                        copy(cacheEntry, ostream, ranges.iterator(), contentType);
                    } else {
                        copy(cacheEntry, writer, ranges.iterator(), contentType);
                    }
                }
            }
        }
    }

    /**
     * Parse the content-range header.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @return Range
     */
    protected Range parseContentRange(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String rangeHeader = request.getHeader("Content-Range");
        if (rangeHeader == null) return null;
        if (!rangeHeader.startsWith("bytes")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        rangeHeader = rangeHeader.substring(6).trim();
        int dashPos = rangeHeader.indexOf('-');
        int slashPos = rangeHeader.indexOf('/');
        if (dashPos == -1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        if (slashPos == -1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        Range range = new Range();
        try {
            range.start = Long.parseLong(rangeHeader.substring(0, dashPos));
            range.end = Long.parseLong(rangeHeader.substring(dashPos + 1, slashPos));
            range.length = Long.parseLong(rangeHeader.substring(slashPos + 1, rangeHeader.length()));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        if (!range.validate()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        return range;
    }

    /**
     * Parse the range header.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @return Vector of ranges
     */
    protected ArrayList parseRange(HttpServletRequest request, HttpServletResponse response, ResourceAttributes resourceAttributes) throws IOException {
        String headerValue = request.getHeader("If-Range");
        if (headerValue != null) {
            long headerValueTime = (-1L);
            try {
                headerValueTime = request.getDateHeader("If-Range");
            } catch (IllegalArgumentException e) {
                ;
            }
            String eTag = getETag(resourceAttributes);
            long lastModified = resourceAttributes.getLastModified();
            if (headerValueTime == (-1L)) {
                if (!eTag.equals(headerValue.trim())) return FULL;
            } else {
                if (lastModified > (headerValueTime + 1000)) return FULL;
            }
        }
        long fileLength = resourceAttributes.getContentLength();
        if (fileLength == 0) return null;
        String rangeHeader = request.getHeader("Range");
        if (rangeHeader == null) return null;
        if (!rangeHeader.startsWith("bytes")) {
            response.addHeader("Content-Range", "bytes */" + fileLength);
            response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return null;
        }
        rangeHeader = rangeHeader.substring(6);
        ArrayList<Range> result = new ArrayList<Range>();
        StringTokenizer commaTokenizer = new StringTokenizer(rangeHeader, ",");
        while (commaTokenizer.hasMoreTokens()) {
            String rangeDefinition = commaTokenizer.nextToken().trim();
            Range currentRange = new Range();
            currentRange.length = fileLength;
            int dashPos = rangeDefinition.indexOf('-');
            if (dashPos == -1) {
                response.addHeader("Content-Range", "bytes */" + fileLength);
                response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return null;
            }
            if (dashPos == 0) {
                try {
                    long offset = Long.parseLong(rangeDefinition);
                    currentRange.start = fileLength + offset;
                    currentRange.end = fileLength - 1;
                } catch (NumberFormatException e) {
                    response.addHeader("Content-Range", "bytes */" + fileLength);
                    response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return null;
                }
            } else {
                try {
                    currentRange.start = Long.parseLong(rangeDefinition.substring(0, dashPos));
                    if (dashPos < rangeDefinition.length() - 1) currentRange.end = Long.parseLong(rangeDefinition.substring(dashPos + 1, rangeDefinition.length())); else currentRange.end = fileLength - 1;
                } catch (NumberFormatException e) {
                    response.addHeader("Content-Range", "bytes */" + fileLength);
                    response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return null;
                }
            }
            if (!currentRange.validate()) {
                response.addHeader("Content-Range", "bytes */" + fileLength);
                response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return null;
            }
            result.add(currentRange);
        }
        return result;
    }

    /**
     *  Decide which way to render. HTML or XML.
     */
    protected InputStream render(String contextPath, CacheEntry cacheEntry) throws IOException, ServletException {
        InputStream xsltInputStream = findXsltInputStream(cacheEntry.context);
        if (xsltInputStream == null) {
            return renderHtml(contextPath, cacheEntry);
        } else {
            return renderXml(contextPath, cacheEntry, xsltInputStream);
        }
    }

    /**
     * Return an InputStream to an HTML representation of the contents
     * of this directory.
     *
     * @param contextPath Context path to which our internal paths are
     *  relative
     */
    protected InputStream renderXml(String contextPath, CacheEntry cacheEntry, InputStream xsltInputStream) throws IOException, ServletException {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\"?>");
        sb.append("<listing ");
        sb.append(" contextPath='");
        sb.append(contextPath);
        sb.append("'");
        sb.append(" directory='");
        sb.append(cacheEntry.name);
        sb.append("' ");
        sb.append(" hasParent='").append(!cacheEntry.name.equals("/"));
        sb.append("'>");
        sb.append("<entries>");
        try {
            NamingEnumeration enumeration = resources.list(cacheEntry.name);
            String rewrittenContextPath = rewriteUrl(contextPath);
            while (enumeration.hasMoreElements()) {
                NameClassPair ncPair = (NameClassPair) enumeration.nextElement();
                String resourceName = ncPair.getName();
                String trimmed = resourceName;
                if (trimmed.equalsIgnoreCase("WEB-INF") || trimmed.equalsIgnoreCase("META-INF") || trimmed.equalsIgnoreCase(localXsltFile)) continue;
                CacheEntry childCacheEntry = resources.lookupCache(cacheEntry.name + resourceName);
                if (!childCacheEntry.exists) {
                    continue;
                }
                sb.append("<entry");
                sb.append(" type='").append((childCacheEntry.context != null) ? "dir" : "file").append("'");
                sb.append(" urlPath='").append(rewrittenContextPath).append(rewriteUrl(cacheEntry.name + resourceName)).append((childCacheEntry.context != null) ? "/" : "").append("'");
                if (childCacheEntry.resource != null) {
                    sb.append(" size='").append(renderSize(childCacheEntry.attributes.getContentLength())).append("'");
                }
                sb.append(" date='").append(childCacheEntry.attributes.getLastModifiedHttp()).append("'");
                sb.append(">");
                sb.append(RequestUtil.filter(trimmed));
                if (childCacheEntry.context != null) sb.append("/");
                sb.append("</entry>");
            }
        } catch (NamingException e) {
            throw new ServletException("Error accessing resource", e);
        }
        sb.append("</entries>");
        String readme = getReadme(cacheEntry.context);
        if (readme != null) {
            sb.append("<readme><![CDATA[");
            sb.append(readme);
            sb.append("]]></readme>");
        }
        sb.append("</listing>");
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Source xmlSource = new StreamSource(new StringReader(sb.toString()));
            Source xslSource = new StreamSource(xsltInputStream);
            Transformer transformer = tFactory.newTransformer(xslSource);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            OutputStreamWriter osWriter = new OutputStreamWriter(stream, "UTF8");
            StreamResult out = new StreamResult(osWriter);
            transformer.transform(xmlSource, out);
            osWriter.flush();
            return (new ByteArrayInputStream(stream.toByteArray()));
        } catch (TransformerException e) {
            throw new ServletException("XSL transformer error", e);
        }
    }

    /**
     * Return an InputStream to an HTML representation of the contents
     * of this directory.
     *
     * @param contextPath Context path to which our internal paths are
     *  relative
     */
    protected InputStream renderHtml(String contextPath, CacheEntry cacheEntry) throws IOException, ServletException {
        String name = cacheEntry.name;
        int trim = name.length();
        if (!name.endsWith("/")) trim += 1;
        if (name.equals("/")) trim = 1;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter osWriter = new OutputStreamWriter(stream, "UTF8");
        PrintWriter writer = new PrintWriter(osWriter);
        StringBuffer sb = new StringBuffer();
        String rewrittenContextPath = rewriteUrl(contextPath);
        sb.append("<html>\r\n");
        sb.append("<head>\r\n");
        sb.append("<title>");
        sb.append(sm.getString("directory.title", name));
        sb.append("</title>\r\n");
        sb.append("<STYLE><!--");
        sb.append(org.apache.catalina.util.TomcatCSS.TOMCAT_CSS);
        sb.append("--></STYLE> ");
        sb.append("</head>\r\n");
        sb.append("<body>");
        sb.append("<h1>");
        sb.append(sm.getString("directory.title", name));
        String parentDirectory = name;
        if (parentDirectory.endsWith("/")) {
            parentDirectory = parentDirectory.substring(0, parentDirectory.length() - 1);
        }
        int slash = parentDirectory.lastIndexOf('/');
        if (slash >= 0) {
            String parent = name.substring(0, slash);
            sb.append(" - <a href=\"");
            sb.append(rewrittenContextPath);
            if (parent.equals("")) parent = "/";
            sb.append(rewriteUrl(parent));
            if (!parent.endsWith("/")) sb.append("/");
            sb.append("\">");
            sb.append("<b>");
            sb.append(sm.getString("directory.parent", parent));
            sb.append("</b>");
            sb.append("</a>");
        }
        sb.append("</h1>");
        sb.append("<HR size=\"1\" noshade=\"noshade\">");
        sb.append("<table width=\"100%\" cellspacing=\"0\"" + " cellpadding=\"5\" align=\"center\">\r\n");
        sb.append("<tr>\r\n");
        sb.append("<td align=\"left\"><font size=\"+1\"><strong>");
        sb.append(sm.getString("directory.filename"));
        sb.append("</strong></font></td>\r\n");
        sb.append("<td align=\"center\"><font size=\"+1\"><strong>");
        sb.append(sm.getString("directory.size"));
        sb.append("</strong></font></td>\r\n");
        sb.append("<td align=\"right\"><font size=\"+1\"><strong>");
        sb.append(sm.getString("directory.lastModified"));
        sb.append("</strong></font></td>\r\n");
        sb.append("</tr>");
        try {
            NamingEnumeration enumeration = resources.list(cacheEntry.name);
            boolean shade = false;
            while (enumeration.hasMoreElements()) {
                NameClassPair ncPair = (NameClassPair) enumeration.nextElement();
                String resourceName = ncPair.getName();
                String trimmed = resourceName;
                if (trimmed.equalsIgnoreCase("WEB-INF") || trimmed.equalsIgnoreCase("META-INF")) continue;
                CacheEntry childCacheEntry = resources.lookupCache(cacheEntry.name + resourceName);
                if (!childCacheEntry.exists) {
                    continue;
                }
                sb.append("<tr");
                if (shade) sb.append(" bgcolor=\"#eeeeee\"");
                sb.append(">\r\n");
                shade = !shade;
                sb.append("<td align=\"left\">&nbsp;&nbsp;\r\n");
                sb.append("<a href=\"");
                sb.append(rewrittenContextPath);
                resourceName = rewriteUrl(name + resourceName);
                sb.append(resourceName);
                if (childCacheEntry.context != null) sb.append("/");
                sb.append("\"><tt>");
                sb.append(RequestUtil.filter(trimmed));
                if (childCacheEntry.context != null) sb.append("/");
                sb.append("</tt></a></td>\r\n");
                sb.append("<td align=\"right\"><tt>");
                if (childCacheEntry.context != null) sb.append("&nbsp;"); else sb.append(renderSize(childCacheEntry.attributes.getContentLength()));
                sb.append("</tt></td>\r\n");
                sb.append("<td align=\"right\"><tt>");
                sb.append(childCacheEntry.attributes.getLastModifiedHttp());
                sb.append("</tt></td>\r\n");
                sb.append("</tr>\r\n");
            }
        } catch (NamingException e) {
            throw new ServletException("Error accessing resource", e);
        }
        sb.append("</table>\r\n");
        sb.append("<HR size=\"1\" noshade=\"noshade\">");
        String readme = getReadme(cacheEntry.context);
        if (readme != null) {
            sb.append(readme);
            sb.append("<HR size=\"1\" noshade=\"noshade\">");
        }
        sb.append("<h3>").append(ServerInfo.getServerInfo()).append("</h3>");
        sb.append("</body>\r\n");
        sb.append("</html>\r\n");
        writer.write(sb.toString());
        writer.flush();
        return (new ByteArrayInputStream(stream.toByteArray()));
    }

    /**
     * Render the specified file size (in bytes).
     *
     * @param size File size (in bytes)
     */
    protected String renderSize(long size) {
        long leftSide = size / 1024;
        long rightSide = (size % 1024) / 103;
        if ((leftSide == 0) && (rightSide == 0) && (size > 0)) rightSide = 1;
        return ("" + leftSide + "." + rightSide + " kb");
    }

    /**
     * Get the readme file as a string.
     */
    protected String getReadme(DirContext directory) throws IOException, ServletException {
        if (readmeFile != null) {
            try {
                Object obj = directory.lookup(readmeFile);
                if ((obj != null) && (obj instanceof Resource)) {
                    StringWriter buffer = new StringWriter();
                    InputStream is = ((Resource) obj).streamContent();
                    copyRange(new InputStreamReader(is), new PrintWriter(buffer));
                    return buffer.toString();
                }
            } catch (NamingException e) {
                if (debug > 10) log("readme '" + readmeFile + "' not found", e);
                return null;
            }
        }
        return null;
    }

    /**
     * Return the xsl template inputstream (if possible)
     */
    protected InputStream findXsltInputStream(DirContext directory) throws IOException, ServletException {
        if (localXsltFile != null) {
            try {
                Object obj = directory.lookup(localXsltFile);
                if ((obj != null) && (obj instanceof Resource)) {
                    InputStream is = ((Resource) obj).streamContent();
                    if (is != null) return is;
                }
            } catch (NamingException e) {
                if (debug > 10) log("localXsltFile '" + localXsltFile + "' not found", e);
                return null;
            }
        }
        if (globalXsltFile != null) {
            FileInputStream fis = null;
            try {
                File f = new File(globalXsltFile);
                if (f.exists()) {
                    fis = new FileInputStream(f);
                    byte b[] = new byte[(int) f.length()];
                    fis.read(b);
                    return new ByteArrayInputStream(b);
                }
            } finally {
                if (fis != null) fis.close();
            }
        }
        return null;
    }

    /**
     * Check if sendfile can be used.
     */
    protected boolean checkSendfile(HttpServletRequest request, HttpServletResponse response, CacheEntry entry, long length, Range range) {
        if ((sendfileSize > 0) && (entry.resource != null) && ((length > sendfileSize) || (entry.resource.getContent() == null)) && (entry.attributes.getCanonicalPath() != null) && (Boolean.TRUE == request.getAttribute("org.apache.tomcat.sendfile.support")) && (request.getClass().getName().equals("org.apache.catalina.connector.RequestFacade")) && (response.getClass().getName().equals("org.apache.catalina.connector.ResponseFacade"))) {
            request.setAttribute("org.apache.tomcat.sendfile.filename", entry.attributes.getCanonicalPath());
            if (range == null) {
                request.setAttribute("org.apache.tomcat.sendfile.start", new Long(0L));
                request.setAttribute("org.apache.tomcat.sendfile.end", new Long(length));
            } else {
                request.setAttribute("org.apache.tomcat.sendfile.start", new Long(range.start));
                request.setAttribute("org.apache.tomcat.sendfile.end", new Long(range.end + 1));
            }
            request.setAttribute("org.apache.tomcat.sendfile.token", this);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check if the if-match condition is satisfied.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resourceInfo File object
     * @return boolean true if the resource meets the specified condition,
     * and false if the condition is not satisfied, in which case request
     * processing is stopped
     */
    protected boolean checkIfMatch(HttpServletRequest request, HttpServletResponse response, ResourceAttributes resourceAttributes) throws IOException {
        String eTag = getETag(resourceAttributes);
        String headerValue = request.getHeader("If-Match");
        if (headerValue != null) {
            if (headerValue.indexOf('*') == -1) {
                StringTokenizer commaTokenizer = new StringTokenizer(headerValue, ",");
                boolean conditionSatisfied = false;
                while (!conditionSatisfied && commaTokenizer.hasMoreTokens()) {
                    String currentToken = commaTokenizer.nextToken();
                    if (currentToken.trim().equals(eTag)) conditionSatisfied = true;
                }
                if (!conditionSatisfied) {
                    response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if the if-modified-since condition is satisfied.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resourceInfo File object
     * @return boolean true if the resource meets the specified condition,
     * and false if the condition is not satisfied, in which case request
     * processing is stopped
     */
    protected boolean checkIfModifiedSince(HttpServletRequest request, HttpServletResponse response, ResourceAttributes resourceAttributes) throws IOException {
        try {
            long headerValue = request.getDateHeader("If-Modified-Since");
            long lastModified = resourceAttributes.getLastModified();
            if (headerValue != -1) {
                if ((request.getHeader("If-None-Match") == null) && (lastModified < headerValue + 1000)) {
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    response.setHeader("ETag", getETag(resourceAttributes));
                    return false;
                }
            }
        } catch (IllegalArgumentException illegalArgument) {
            return true;
        }
        return true;
    }

    /**
     * Check if the if-none-match condition is satisfied.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resourceInfo File object
     * @return boolean true if the resource meets the specified condition,
     * and false if the condition is not satisfied, in which case request
     * processing is stopped
     */
    protected boolean checkIfNoneMatch(HttpServletRequest request, HttpServletResponse response, ResourceAttributes resourceAttributes) throws IOException {
        String eTag = getETag(resourceAttributes);
        String headerValue = request.getHeader("If-None-Match");
        if (headerValue != null) {
            boolean conditionSatisfied = false;
            if (!headerValue.equals("*")) {
                StringTokenizer commaTokenizer = new StringTokenizer(headerValue, ",");
                while (!conditionSatisfied && commaTokenizer.hasMoreTokens()) {
                    String currentToken = commaTokenizer.nextToken();
                    if (currentToken.trim().equals(eTag)) conditionSatisfied = true;
                }
            } else {
                conditionSatisfied = true;
            }
            if (conditionSatisfied) {
                if (("GET".equals(request.getMethod())) || ("HEAD".equals(request.getMethod()))) {
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    response.setHeader("ETag", getETag(resourceAttributes));
                    return false;
                } else {
                    response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if the if-unmodified-since condition is satisfied.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param resourceInfo File object
     * @return boolean true if the resource meets the specified condition,
     * and false if the condition is not satisfied, in which case request
     * processing is stopped
     */
    protected boolean checkIfUnmodifiedSince(HttpServletRequest request, HttpServletResponse response, ResourceAttributes resourceAttributes) throws IOException {
        try {
            long lastModified = resourceAttributes.getLastModified();
            long headerValue = request.getDateHeader("If-Unmodified-Since");
            if (headerValue != -1) {
                if (lastModified >= (headerValue + 1000)) {
                    response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
                    return false;
                }
            }
        } catch (IllegalArgumentException illegalArgument) {
            return true;
        }
        return true;
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param resourceInfo The resource information
     * @param ostream The output stream to write to
     *
     * @exception IOException if an input/output error occurs
     */
    protected void copy(CacheEntry cacheEntry, InputStream is, ServletOutputStream ostream) throws IOException {
        IOException exception = null;
        InputStream resourceInputStream = null;
        if (cacheEntry.resource != null) {
            byte buffer[] = cacheEntry.resource.getContent();
            if (buffer != null) {
                ostream.write(buffer, 0, buffer.length);
                return;
            }
            resourceInputStream = cacheEntry.resource.streamContent();
        } else {
            resourceInputStream = is;
        }
        InputStream istream = new BufferedInputStream(resourceInputStream, input);
        exception = copyRange(istream, ostream);
        istream.close();
        if (exception != null) throw exception;
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param resourceInfo The resource info
     * @param writer The writer to write to
     *
     * @exception IOException if an input/output error occurs
     */
    protected void copy(CacheEntry cacheEntry, InputStream is, PrintWriter writer) throws IOException {
        IOException exception = null;
        InputStream resourceInputStream = null;
        if (cacheEntry.resource != null) {
            resourceInputStream = cacheEntry.resource.streamContent();
        } else {
            resourceInputStream = is;
        }
        Reader reader;
        if (fileEncoding == null) {
            reader = new InputStreamReader(resourceInputStream);
        } else {
            reader = new InputStreamReader(resourceInputStream, fileEncoding);
        }
        exception = copyRange(reader, writer);
        reader.close();
        if (exception != null) throw exception;
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param resourceInfo The ResourceInfo object
     * @param ostream The output stream to write to
     * @param range Range the client wanted to retrieve
     * @exception IOException if an input/output error occurs
     */
    protected void copy(CacheEntry cacheEntry, ServletOutputStream ostream, Range range) throws IOException {
        IOException exception = null;
        InputStream resourceInputStream = cacheEntry.resource.streamContent();
        InputStream istream = new BufferedInputStream(resourceInputStream, input);
        exception = copyRange(istream, ostream, range.start, range.end);
        istream.close();
        if (exception != null) throw exception;
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param resourceInfo The ResourceInfo object
     * @param writer The writer to write to
     * @param range Range the client wanted to retrieve
     * @exception IOException if an input/output error occurs
     */
    protected void copy(CacheEntry cacheEntry, PrintWriter writer, Range range) throws IOException {
        IOException exception = null;
        InputStream resourceInputStream = cacheEntry.resource.streamContent();
        Reader reader;
        if (fileEncoding == null) {
            reader = new InputStreamReader(resourceInputStream);
        } else {
            reader = new InputStreamReader(resourceInputStream, fileEncoding);
        }
        exception = copyRange(reader, writer, range.start, range.end);
        reader.close();
        if (exception != null) throw exception;
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param resourceInfo The ResourceInfo object
     * @param ostream The output stream to write to
     * @param ranges Enumeration of the ranges the client wanted to retrieve
     * @param contentType Content type of the resource
     * @exception IOException if an input/output error occurs
     */
    protected void copy(CacheEntry cacheEntry, ServletOutputStream ostream, Iterator ranges, String contentType) throws IOException {
        IOException exception = null;
        while ((exception == null) && (ranges.hasNext())) {
            InputStream resourceInputStream = cacheEntry.resource.streamContent();
            InputStream istream = new BufferedInputStream(resourceInputStream, input);
            Range currentRange = (Range) ranges.next();
            ostream.println();
            ostream.println("--" + mimeSeparation);
            if (contentType != null) ostream.println("Content-Type: " + contentType);
            ostream.println("Content-Range: bytes " + currentRange.start + "-" + currentRange.end + "/" + currentRange.length);
            ostream.println();
            exception = copyRange(istream, ostream, currentRange.start, currentRange.end);
            istream.close();
        }
        ostream.println();
        ostream.print("--" + mimeSeparation + "--");
        if (exception != null) throw exception;
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param resourceInfo The ResourceInfo object
     * @param writer The writer to write to
     * @param ranges Enumeration of the ranges the client wanted to retrieve
     * @param contentType Content type of the resource
     * @exception IOException if an input/output error occurs
     */
    protected void copy(CacheEntry cacheEntry, PrintWriter writer, Iterator ranges, String contentType) throws IOException {
        IOException exception = null;
        while ((exception == null) && (ranges.hasNext())) {
            InputStream resourceInputStream = cacheEntry.resource.streamContent();
            Reader reader;
            if (fileEncoding == null) {
                reader = new InputStreamReader(resourceInputStream);
            } else {
                reader = new InputStreamReader(resourceInputStream, fileEncoding);
            }
            Range currentRange = (Range) ranges.next();
            writer.println();
            writer.println("--" + mimeSeparation);
            if (contentType != null) writer.println("Content-Type: " + contentType);
            writer.println("Content-Range: bytes " + currentRange.start + "-" + currentRange.end + "/" + currentRange.length);
            writer.println();
            exception = copyRange(reader, writer, currentRange.start, currentRange.end);
            reader.close();
        }
        writer.println();
        writer.print("--" + mimeSeparation + "--");
        if (exception != null) throw exception;
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param istream The input stream to read from
     * @param ostream The output stream to write to
     * @return Exception which occurred during processing
     */
    protected IOException copyRange(InputStream istream, ServletOutputStream ostream) {
        IOException exception = null;
        byte buffer[] = new byte[input];
        int len = buffer.length;
        while (true) {
            try {
                len = istream.read(buffer);
                if (len == -1) break;
                ostream.write(buffer, 0, len);
            } catch (IOException e) {
                exception = e;
                len = -1;
                break;
            }
        }
        return exception;
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param reader The reader to read from
     * @param writer The writer to write to
     * @return Exception which occurred during processing
     */
    protected IOException copyRange(Reader reader, PrintWriter writer) {
        IOException exception = null;
        char buffer[] = new char[input];
        int len = buffer.length;
        while (true) {
            try {
                len = reader.read(buffer);
                if (len == -1) break;
                writer.write(buffer, 0, len);
            } catch (IOException e) {
                exception = e;
                len = -1;
                break;
            }
        }
        return exception;
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param istream The input stream to read from
     * @param ostream The output stream to write to
     * @param start Start of the range which will be copied
     * @param end End of the range which will be copied
     * @return Exception which occurred during processing
     */
    protected IOException copyRange(InputStream istream, ServletOutputStream ostream, long start, long end) {
        if (debug > 10) log("Serving bytes:" + start + "-" + end);
        try {
            istream.skip(start);
        } catch (IOException e) {
            return e;
        }
        IOException exception = null;
        long bytesToRead = end - start + 1;
        byte buffer[] = new byte[input];
        int len = buffer.length;
        while ((bytesToRead > 0) && (len >= buffer.length)) {
            try {
                len = istream.read(buffer);
                if (bytesToRead >= len) {
                    ostream.write(buffer, 0, len);
                    bytesToRead -= len;
                } else {
                    ostream.write(buffer, 0, (int) bytesToRead);
                    bytesToRead = 0;
                }
            } catch (IOException e) {
                exception = e;
                len = -1;
            }
            if (len < buffer.length) break;
        }
        return exception;
    }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param reader The reader to read from
     * @param writer The writer to write to
     * @param start Start of the range which will be copied
     * @param end End of the range which will be copied
     * @return Exception which occurred during processing
     */
    protected IOException copyRange(Reader reader, PrintWriter writer, long start, long end) {
        try {
            reader.skip(start);
        } catch (IOException e) {
            return e;
        }
        IOException exception = null;
        long bytesToRead = end - start + 1;
        char buffer[] = new char[input];
        int len = buffer.length;
        while ((bytesToRead > 0) && (len >= buffer.length)) {
            try {
                len = reader.read(buffer);
                if (bytesToRead >= len) {
                    writer.write(buffer, 0, len);
                    bytesToRead -= len;
                } else {
                    writer.write(buffer, 0, (int) bytesToRead);
                    bytesToRead = 0;
                }
            } catch (IOException e) {
                exception = e;
                len = -1;
            }
            if (len < buffer.length) break;
        }
        return exception;
    }

    protected class Range {

        public long start;

        public long end;

        public long length;

        /**
         * Validate range.
         */
        public boolean validate() {
            if (end >= length) end = length - 1;
            return ((start >= 0) && (end >= 0) && (start <= end) && (length > 0));
        }

        public void recycle() {
            start = 0;
            end = 0;
            length = 0;
        }
    }
}
