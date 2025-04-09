package org.placelab.test;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Hashtable;
import org.placelab.mapper.JDBMMapper;
import org.placelab.mapper.JDBMQuickMapLoader;
import org.placelab.mapper.MapLoader;
import org.placelab.proxy.HTTPRequest;
import org.placelab.proxy.HTTPResponse;
import org.placelab.proxy.ProxyServletEngine;
import org.placelab.proxy.Servlet;
import org.placelab.util.FileSynchronizer;
import org.placelab.util.ZipUtil;

public class JDBMQuickMapLoaderTest implements Testable {

    private static class FileVend implements Servlet {

        private Hashtable files;

        public static String SERVLET_PREFIX = "http://placelab.mapload.test/file";

        public FileVend(Hashtable files) {
            this.files = files;
        }

        public String getName() {
            return "FileVendServlet";
        }

        public void register() {
            ProxyServletEngine.addServlet(SERVLET_PREFIX, this);
        }

        public HTTPResponse serviceRequest(HTTPRequest req) {
            StringBuffer sb = new StringBuffer();
            String s = req.url.toString().substring(SERVLET_PREFIX.length());
            if (!s.startsWith("/")) {
                return whoops();
            }
            String f = s.substring(1);
            String path = (String) files.get(f);
            if (path == null) return whoops();
            try {
                return fileResponse(path);
            } catch (IOException e) {
                return whoops();
            }
        }

        private HTTPResponse fileResponse(String path) throws IOException {
            File file = new File(path);
            ByteArrayOutputStream to = new ByteArrayOutputStream();
            FileInputStream from = new FileInputStream(file);
            ZipUtil.pipeStreams(to, from);
            from.close();
            return new HTTPResponse(HTTPResponse.RESPONSE_OK, "application/unknown", to.size(), to.toByteArray());
        }

        private HTTPResponse whoops() {
            return new HTTPResponse(HTTPResponse.RESPONSE_NOT_FOUND, "text/plain", "whoops".length(), "whoops".getBytes());
        }

        public Hashtable injectHeaders(HTTPRequest req) {
            return null;
        }
    }

    public String getName() {
        return "JDBMQuickMapLoaderTest";
    }

    public void runTests(TestResult result) throws Throwable {
        setup();
        String oldPort = System.getProperty("http.proxyPort");
        String oldHost = System.getProperty("http.proxyHost");
        System.setProperty("http.proxyPort", "2080");
        System.setProperty("http.proxyHost", "localhost");
        JDBMQuickMapLoader qml = new JDBMQuickMapLoader(FileVend.SERVLET_PREFIX + "/mapper-zip.php", FileVend.SERVLET_PREFIX + "/mapper-md5.php", dPath);
        checkCreate(result, qml);
        ((JDBMMapper) qml.getMapper()).deleteAll();
        StringBuffer sb = new StringBuffer();
        sb.append("47.6636333\t-122.3083683\tlambda\t004005b45c85\n");
        sb.append("47.6636333\t-122.3083683\tlinksys\t000c41424432\n");
        InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
        qml.loadMap(is);
        checkDifferent(result, qml);
        checkSame(result, qml);
        if (oldPort != null) System.setProperty("http.proxyPort", oldPort);
        if (oldHost != null) System.setProperty("http.proxyHost", oldHost);
    }

    private void checkSame(TestResult result, JDBMQuickMapLoader qml) throws Exception {
        result.assertTrue(this, true, qml.doIt(), "qml checkSame: up to date check check");
        result.assertTrue(this, 3, ((JDBMMapper) qml.getMapper()).size(), "qml checkSame: Number of items in fresh download check");
    }

    private void checkCreate(TestResult result, JDBMQuickMapLoader qml) throws Exception {
        result.assertTrue(this, false, qml.doIt(), "qml checkCreate: up to date check check");
        result.assertTrue(this, 3, ((JDBMMapper) qml.getMapper()).size(), "qml checkCreate: Number of items in fresh download check");
    }

    private void checkDifferent(TestResult result, JDBMQuickMapLoader qml) throws Exception {
        result.assertTrue(this, false, qml.doIt(), "qml checkDifferent: up to date check check");
        result.assertTrue(this, 3, ((JDBMMapper) qml.getMapper()).size(), "qml checkDifferent: Number of items in fresh download check");
    }

    String tmapperdbPath, tmapperlgPath, tmapperMD5Path;

    String dPath, dmapperdbPath, dmapperlgPath, dmapperMD5Path;

    private void setup() throws Exception {
        File tempDir = File.createTempFile("test", "zip");
        tempDir.delete();
        tempDir.mkdir();
        tempDir.deleteOnExit();
        MapLoader ml = new MapLoader(new JDBMMapper(tempDir.getAbsolutePath() + File.separator + "mapper"));
        StringBuffer sb = new StringBuffer();
        sb.append("47.6636333\t-122.3083683\tlambda\t004005b45c85\n");
        sb.append("47.6636333\t-122.3083683\tlinksys\t000c41424432\n");
        sb.append("47.6637783\t-122.30837\t4714\t00095b5322ec\n");
        InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
        ml.loadMap(is);
        is.close();
        tmapperdbPath = ((JDBMMapper) ml.getMapper()).getDbName() + ".db";
        tmapperlgPath = ((JDBMMapper) ml.getMapper()).getDbName() + ".lg";
        tmapperMD5Path = ((JDBMMapper) ml.getMapper()).getDbName() + ".md5";
        String md5sum = FileSynchronizer.loadFileHash(tmapperdbPath);
        PrintWriter p = new PrintWriter(new BufferedOutputStream(new FileOutputStream(tmapperMD5Path)));
        p.println(md5sum);
        p.close();
        File tempZip = File.createTempFile("mappertmp", "zip");
        ZipUtil.dirToZip(tempDir, tempZip);
        Hashtable files = new Hashtable();
        files.put("mapper-zip.php", tempZip.getAbsolutePath());
        files.put("mapper-md5.php", tmapperMD5Path);
        FileVend vend = new FileVend(files);
        vend.register();
        ProxyServletEngine.startProxy(true);
        File dFile = File.createTempFile("test_download", "");
        dFile.delete();
        dFile.mkdir();
        dFile.deleteOnExit();
        dPath = dFile.getAbsolutePath();
    }
}
