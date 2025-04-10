package org.gbif.nameparser.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class StaticUtf8Servlet extends HttpServlet {

    public static class Error implements LookupResult {

        protected final int statusCode;

        protected final String message;

        public Error(int statusCode, String message) {
            this.statusCode = statusCode;
            this.message = message;
        }

        public long getLastModified() {
            return -1;
        }

        public void respondGet(HttpServletResponse resp) throws IOException {
            resp.sendError(statusCode, message);
        }

        public void respondHead(HttpServletResponse resp) {
            throw new UnsupportedOperationException();
        }
    }

    public static interface LookupResult {

        public long getLastModified();

        public void respondGet(HttpServletResponse resp) throws IOException;

        public void respondHead(HttpServletResponse resp);
    }

    public static class StaticFile implements LookupResult {

        protected final long lastModified;

        protected final String mimeType;

        protected final int contentLength;

        protected final URL url;

        public StaticFile(long lastModified, String mimeType, int contentLength, URL url) {
            this.lastModified = lastModified;
            this.mimeType = mimeType;
            this.contentLength = contentLength;
            this.url = url;
        }

        public long getLastModified() {
            return lastModified;
        }

        public void respondGet(HttpServletResponse resp) throws IOException {
            setHeaders(resp);
            final OutputStream os;
            resp.setCharacterEncoding("utf-8");
            os = resp.getOutputStream();
            transferStreams(url.openStream(), os);
        }

        public void respondHead(HttpServletResponse resp) {
            setHeaders(resp);
        }

        protected void setHeaders(HttpServletResponse resp) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(mimeType);
            if (contentLength >= 0) {
                resp.setContentLength(contentLength);
            }
        }
    }

    protected static final int deflateThreshold = 4 * 1024;

    protected static final int bufferSize = 4 * 1024;

    public static <T> T coalesce(T... ts) {
        for (T t : ts) {
            if (t != null) {
                return t;
            }
        }
        return null;
    }

    protected static boolean deflatable(String mimetype) {
        return mimetype.startsWith("text/") || mimetype.equals("application/postscript") || mimetype.startsWith("application/ms") || mimetype.startsWith("application/vnd") || mimetype.endsWith("xml");
    }

    protected static void transferStreams(InputStream is, OutputStream os) throws IOException {
        try {
            byte[] buf = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = is.read(buf)) != -1) {
                os.write(buf, 0, bytesRead);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        lookup(req).respondGet(resp);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            lookup(req).respondHead(resp);
        } catch (UnsupportedOperationException e) {
            super.doHead(req, resp);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doGet(req, resp);
    }

    @Override
    protected long getLastModified(HttpServletRequest req) {
        return lookup(req).getLastModified();
    }

    protected String getMimeType(String path) {
        return coalesce(getServletContext().getMimeType(path), "application/octet-stream");
    }

    protected String getPath(HttpServletRequest req) {
        String servletPath = req.getServletPath();
        String pathInfo = coalesce(req.getPathInfo(), "");
        return servletPath + pathInfo;
    }

    protected boolean isForbidden(String path) {
        String lpath = path.toLowerCase();
        return lpath.startsWith("/web-inf/") || lpath.startsWith("/meta-inf/");
    }

    protected LookupResult lookup(HttpServletRequest req) {
        LookupResult r = (LookupResult) req.getAttribute("lookupResult");
        if (r == null) {
            r = lookupNoCache(req);
            req.setAttribute("lookupResult", r);
        }
        return r;
    }

    protected LookupResult lookupNoCache(HttpServletRequest req) {
        final String path = getPath(req);
        if (isForbidden(path)) {
            return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        }
        final URL url;
        try {
            url = getServletContext().getResource(path);
        } catch (MalformedURLException e) {
            return new Error(HttpServletResponse.SC_BAD_REQUEST, "Malformed path");
        }
        if (url == null) {
            return new Error(HttpServletResponse.SC_NOT_FOUND, "Not found");
        }
        final String mimeType = getMimeType(path);
        System.out.println(mimeType);
        final String realpath = getServletContext().getRealPath(path);
        if (realpath != null) {
            File f = new File(realpath);
            if (!f.isFile()) {
                return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
            } else {
                return new StaticFile(f.lastModified(), mimeType, (int) f.length(), url);
            }
        } else {
            try {
                final ZipEntry ze = ((JarURLConnection) url.openConnection()).getJarEntry();
                if (ze != null) {
                    if (ze.isDirectory()) {
                        return new Error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                    } else {
                        return new StaticFile(ze.getTime(), mimeType, (int) ze.getSize(), url);
                    }
                } else {
                    return new StaticFile(-1, mimeType, -1, url);
                }
            } catch (ClassCastException e) {
                return new StaticFile(-1, mimeType, -1, url);
            } catch (IOException e) {
                return new Error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
            }
        }
    }
}
