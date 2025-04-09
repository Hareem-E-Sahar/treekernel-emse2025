package org.mozilla.browser.impl;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.zip.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileUtils {

    static Log log = LogFactory.getLog(FileUtils.class);

    public static String replaceExtensionInFileName(String file_name, String in_ext, String out_ext) {
        int idx = file_name.lastIndexOf('.');
        if (idx >= 0 && (in_ext == null || file_name.substring(idx + 1).equalsIgnoreCase(in_ext))) return file_name.substring(0, idx + 1) + out_ext; else return file_name + "." + out_ext;
    }

    public static String joinPaths(String path1, String path2) {
        if (path1.endsWith("" + File.separatorChar)) return path1 + path2; else return path1 + File.separatorChar + path2;
    }

    public static String getFileName(String path) {
        int idx = path.lastIndexOf(File.separatorChar);
        return (idx >= 0 ? path.substring(idx + 1) : path);
    }

    public static void createZip(File f, OutputStream os) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(os);
        zos.setMethod(ZipOutputStream.DEFLATED);
        recZip(zos, f, "");
        zos.close();
    }

    private static void recZip(ZipOutputStream zos, File zipBaseDir, String relPath) throws IOException {
        File f = new File(zipBaseDir, relPath);
        if (f.exists()) {
            if (f.isDirectory()) {
                String[] flist = f.list();
                for (int i = 0; i < flist.length; i++) {
                    String childRelPath = relPath.length() > 0 ? relPath + File.separator + flist[i] : flist[i];
                    recZip(zos, zipBaseDir, childRelPath);
                }
            } else {
                InputStream in = new BufferedInputStream(new FileInputStream(f));
                zos.putNextEntry(new ZipEntry(relPath));
                int len;
                byte[] buf = new byte[1024];
                while ((len = in.read(buf)) > 0) {
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
                in.close();
            }
        }
    }

    public static byte[] readFile(File f) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(f);
        InputStream is = new BufferedInputStream(fis);
        int len;
        byte[] buf = new byte[1024];
        while ((len = is.read(buf)) != -1) {
            bos.write(buf, 0, len);
        }
        is.close();
        fis.close();
        return bos.toByteArray();
    }

    public static byte[] readStream(InputStream is) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1000];
            int n;
            while ((n = is.read(buf, 0, buf.length)) != -1) {
                bos.write(buf, 0, n);
            }
            is.close();
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("error reading stream", e);
            return new byte[0];
        }
    }

    /**
     * Safely converts a 'file:...' URL into a File,
     * (correctly handles spaces in the URL)
     */
    public static File toFile(URL url) {
        try {
            String path = URLDecoder.decode(url.getPath(), "UTF-8");
            String proto = url.getProtocol();
            return toFile(path, proto);
        } catch (UnsupportedEncodingException e) {
            log.error("error converting url to filename", e);
            throw new RuntimeException(e);
        }
    }

    public static File toFile(URI uri) {
        try {
            String path = URLDecoder.decode(uri.getPath(), "UTF-8");
            String proto = uri.getScheme();
            return toFile(path, proto);
        } catch (UnsupportedEncodingException e) {
            log.error("error converting uri to filename", e);
            throw new RuntimeException(e);
        }
    }

    public static File urlToFile(String uri) {
        try {
            if (uri.startsWith("file:")) {
                URL u = new URL(uri);
                return toFile(u);
            } else {
                return null;
            }
        } catch (MalformedURLException e) {
            log.error("malformed url", e);
            throw new RuntimeException(e);
        }
    }

    private static File toFile(String path, String proto) {
        if (File.separatorChar != '/') path = path.replace('/', File.separatorChar);
        if (File.separatorChar == '\\' && proto != null && proto.equals("file") && path.startsWith("\\")) {
            path = path.substring(1);
        }
        return new File(path);
    }
}
