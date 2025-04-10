package hci.gnomex.controller;

import hci.gnomex.constants.Constants;
import hci.gnomex.model.PropertyDictionary;
import hci.gnomex.model.Request;
import hci.gnomex.model.TransferLog;
import hci.gnomex.security.SecurityAdvisor;
import hci.gnomex.utility.ArchiveHelper;
import hci.gnomex.utility.DictionaryHelper;
import hci.gnomex.utility.FileDescriptor;
import hci.gnomex.utility.HibernateSession;
import hci.gnomex.utility.PropertyDictionaryHelper;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.hibernate.Session;

public class DownloadResultsServlet extends HttpServlet {

    private String keysString = null;

    private String includeTIF = "N";

    private String includeJPG = "N";

    private String baseDir;

    private String baseDirFlowCell;

    private ArchiveHelper archiveHelper = new ArchiveHelper();

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DownloadResultsServlet.class);

    public void init() {
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        if (Constants.REQUIRE_SECURE_REMOTE && !req.isSecure()) {
            if (req.getRemoteAddr().equals(InetAddress.getLocalHost().getHostAddress()) || req.getRemoteAddr().equals("127.0.0.1") || InetAddress.getByName(req.getRemoteAddr()).isLoopbackAddress()) {
                log.debug("Requested from local host");
            } else {
                log.error("Accessing secure command over non-secure line from remote host is not allowed");
                response.setContentType("text/html");
                response.getOutputStream().println("<html><head><title>Error</title></head>");
                response.getOutputStream().println("<body><b>");
                response.getOutputStream().println("Secure connection is required. Prefix your request with 'https: " + "<br>");
                response.getOutputStream().println("</body>");
                response.getOutputStream().println("</html>");
                return;
            }
        }
        keysString = req.getParameter("resultKeys");
        if (req.getParameter("includeTIF") != null && !req.getParameter("includeTIF").equals("")) {
            includeTIF = req.getParameter("includeTIF");
        }
        if (req.getParameter("includeJPG") != null && !req.getParameter("includeJPG").equals("")) {
            includeJPG = req.getParameter("includeJPG");
        }
        if (req.getParameter("mode") != null && !req.getParameter("mode").equals("")) {
            archiveHelper.setMode(req.getParameter("mode"));
        }
        SecurityAdvisor secAdvisor = null;
        try {
            secAdvisor = (SecurityAdvisor) req.getSession().getAttribute(SecurityAdvisor.SECURITY_ADVISOR_SESSION_KEY);
            if (secAdvisor != null) {
                response.setContentType("application/x-download");
                response.setHeader("Content-Disposition", "attachment;filename=gnomexExperimentData.zip");
                response.setHeader("Cache-Control", "max-age=0, must-revalidate");
                Session sess = secAdvisor.getHibernateSession(req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : "guest");
                DictionaryHelper dh = DictionaryHelper.getInstance(sess);
                baseDirFlowCell = dh.getFlowCellDirectory(req.getServerName());
                baseDir = dh.getMicroarrayDirectoryForReading(req.getServerName());
                archiveHelper.setTempDir(dh.getPropertyDictionary(PropertyDictionary.TEMP_DIRECTORY));
                Map fileDescriptorMap = new HashMap();
                long compressedFileSizeTotal = getFileNamesToDownload(baseDir, baseDirFlowCell, keysString, fileDescriptorMap, includeTIF.equals("Y"), includeJPG.equals("Y"), dh.getPropertyDictionary(PropertyDictionary.FLOWCELL_DIRECTORY_FLAG));
                ZipOutputStream zipOut = null;
                TarArchiveOutputStream tarOut = null;
                if (archiveHelper.isZipMode()) {
                    zipOut = new ZipOutputStream(response.getOutputStream());
                } else {
                    tarOut = new TarArchiveOutputStream(response.getOutputStream());
                }
                int totalArchiveSize = 0;
                for (Iterator i = fileDescriptorMap.keySet().iterator(); i.hasNext(); ) {
                    String requestNumber = (String) i.next();
                    Request request = findRequest(sess, requestNumber);
                    if (request == null) {
                        log.error("Unable to find request " + requestNumber + ".  Bypassing download for user " + req.getUserPrincipal().getName() + ".");
                        continue;
                    }
                    if (!secAdvisor.canRead(request)) {
                        log.error("Insufficient permissions to read request " + requestNumber + ".  Bypassing download for user " + req.getUserPrincipal().getName() + ".");
                        continue;
                    }
                    List fileDescriptors = (List) fileDescriptorMap.get(requestNumber);
                    for (Iterator i1 = fileDescriptors.iterator(); i1.hasNext(); ) {
                        FileDescriptor fd = (FileDescriptor) i1.next();
                        TransferLog xferLog = new TransferLog();
                        xferLog.setFileName(fd.getZipEntryName());
                        xferLog.setStartDateTime(new java.util.Date(System.currentTimeMillis()));
                        xferLog.setTransferType(TransferLog.TYPE_DOWNLOAD);
                        xferLog.setTransferMethod(TransferLog.METHOD_HTTP);
                        xferLog.setPerformCompression("Y");
                        xferLog.setIdRequest(request.getIdRequest());
                        xferLog.setIdLab(request.getIdLab());
                        InputStream in = archiveHelper.getInputStreamToArchive(fd.getFileName(), fd.getZipEntryName());
                        if (archiveHelper.isZipMode()) {
                            zipOut.putNextEntry(new ZipEntry(archiveHelper.getArchiveEntryName()));
                        } else {
                            TarArchiveEntry entry = new TarArchiveEntry(archiveHelper.getArchiveEntryName());
                            entry.setSize(archiveHelper.getArchiveFileSize());
                            tarOut.putArchiveEntry(entry);
                        }
                        OutputStream out = null;
                        if (archiveHelper.isZipMode()) {
                            out = zipOut;
                        } else {
                            out = tarOut;
                        }
                        int size = archiveHelper.transferBytes(in, out);
                        totalArchiveSize += size;
                        xferLog.setFileSize(new BigDecimal(size));
                        xferLog.setEndDateTime(new java.util.Date(System.currentTimeMillis()));
                        sess.save(xferLog);
                        if (archiveHelper.isZipMode()) {
                            zipOut.closeEntry();
                        } else {
                            tarOut.closeArchiveEntry();
                        }
                        archiveHelper.removeTemporaryFile();
                    }
                }
                response.setContentLength(totalArchiveSize);
                if (archiveHelper.isZipMode()) {
                    zipOut.finish();
                    zipOut.flush();
                } else {
                    tarOut.finish();
                }
                sess.flush();
            } else {
                response.setStatus(999);
                System.out.println("DownloadResultsServlet: You must have a SecurityAdvisor in order to run this command.");
            }
        } catch (Exception e) {
            response.setStatus(999);
            System.out.println("DownloadResultsServlet: An exception occurred " + e.toString());
            e.printStackTrace();
        } finally {
            if (secAdvisor != null) {
                try {
                    secAdvisor.closeHibernateSession();
                } catch (Exception e) {
                }
            }
            archiveHelper.removeTemporaryFile();
        }
    }

    public static Request findRequest(Session sess, String requestNumber) {
        Request request = null;
        List requests = sess.createQuery("SELECT req from Request req where req.number = '" + requestNumber + "'").list();
        if (requests.size() == 1) {
            request = (Request) requests.get(0);
        }
        return request;
    }

    public static long getFileNamesToDownload(String baseDir, String baseDirFlowCell, String keysString, Map fileDescriptorMap, boolean includeAllTIFFiles, boolean includeAllJPGFiles, String flowCellDirectoryFlag) {
        long fileSizeTotal = 0;
        String[] keys = keysString.split(":");
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            String tokens[] = key.split("-");
            String createYear = tokens[0];
            String createDate = tokens[1];
            String requestNumber = tokens[2];
            String requestNumberBase = Request.getBaseRequestNumber(requestNumber);
            String resultDirectory = tokens[3];
            String flowCellIndicator = "";
            if (tokens.length > 4) {
                flowCellIndicator = tokens[4];
            }
            String directoryName = "";
            String theBaseDir;
            if (flowCellIndicator.equals(flowCellDirectoryFlag)) {
                directoryName = baseDirFlowCell + createYear + "/" + resultDirectory;
                theBaseDir = baseDirFlowCell;
            } else {
                directoryName = baseDir + createYear + "/" + requestNumberBase + "/" + resultDirectory;
                theBaseDir = baseDir;
            }
            fileSizeTotal += getFileNames(requestNumber, directoryName, fileDescriptorMap, includeAllTIFFiles, includeAllJPGFiles, flowCellIndicator, theBaseDir, flowCellDirectoryFlag);
        }
        return fileSizeTotal;
    }

    public static long getFileNames(String requestNumber, String directoryName, Map fileDescriptorMap, boolean includeAllTIFFiles, boolean includeAllJPGFiles, String flowCellIndicator, String theBaseDir, String flowCellDirectoryFlag) {
        File fd = new File(directoryName);
        long fileSizeTotal = 0;
        if (fd.isDirectory()) {
            String[] fileList = fd.list();
            for (int x = 0; x < fileList.length; x++) {
                String fileName = directoryName + "/" + fileList[x];
                File f1 = new File(fileName);
                if (f1.isDirectory()) {
                    fileSizeTotal += getFileNames(requestNumber, fileName, fileDescriptorMap, includeAllTIFFiles, includeAllJPGFiles, flowCellIndicator, theBaseDir, flowCellDirectoryFlag);
                } else {
                    boolean include = true;
                    if (!includeAllJPGFiles && fileName.toLowerCase().endsWith(".jpg")) {
                        include = false;
                    } else if (!includeAllTIFFiles && (fileName.toLowerCase().endsWith(".tif") || fileName.toLowerCase().endsWith(".tif.gz") || fileName.toLowerCase().endsWith(".tif.gzip") || fileName.toLowerCase().endsWith(".tif.zip"))) {
                        include = false;
                    } else if (fileName.toUpperCase().endsWith(".DS_Store")) {
                        include = false;
                    } else if (fileName.toUpperCase().endsWith("THUMBS.DB")) {
                        include = false;
                    }
                    if (include) {
                        long fileSize = f1.length();
                        fileSizeTotal += getEstimatedCompressedFileSize(fileName, fileSize);
                        List fileDescriptors = (List) fileDescriptorMap.get(requestNumber);
                        if (fileDescriptors == null) {
                            fileDescriptors = new ArrayList<FileDescriptor>();
                            fileDescriptorMap.put(requestNumber, fileDescriptors);
                        }
                        String zipEntryName;
                        if (flowCellIndicator.equals(flowCellDirectoryFlag)) {
                            zipEntryName = Request.getBaseRequestNumber(requestNumber) + "/" + fileName.substring(theBaseDir.length() + 5).replaceAll("\\\\", "/");
                        } else {
                            try {
                                zipEntryName = PropertyDictionaryHelper.parseZipEntryName(theBaseDir, f1.getCanonicalPath());
                            } catch (IOException e) {
                                throw new RuntimeException("Cannot get canonical file name for " + f1.getName());
                            }
                            if (zipEntryName.startsWith("/")) {
                                zipEntryName = zipEntryName.substring(1);
                            }
                        }
                        fileDescriptors.add(new FileDescriptor(requestNumber, "", f1, zipEntryName));
                    }
                }
            }
        }
        return fileSizeTotal;
    }

    public static long getEstimatedCompressedFileSize(String fileName, long fileSize) {
        double compressionRatio = 1;
        if (fileName.toUpperCase().endsWith("FEP")) {
            compressionRatio = 1.6;
        } else if (fileName.toUpperCase().endsWith("PDF")) {
            compressionRatio = 1;
        } else if (fileName.toUpperCase().endsWith("TIF")) {
            compressionRatio = 1.9;
        } else if (fileName.toUpperCase().endsWith("TIFF")) {
            compressionRatio = 1.9;
        } else if (fileName.toUpperCase().endsWith("JPG")) {
            compressionRatio = 1;
        } else if (fileName.toUpperCase().endsWith("JPEG")) {
            compressionRatio = 1;
        } else if (fileName.toUpperCase().endsWith("TXT")) {
            compressionRatio = 2.7;
        } else if (fileName.toUpperCase().endsWith("RTF")) {
            compressionRatio = 2.7;
        } else if (fileName.toUpperCase().endsWith("DAT")) {
            compressionRatio = 1.6;
        } else if (fileName.toUpperCase().endsWith("CEL")) {
            compressionRatio = 2.8;
        } else if (fileName.toUpperCase().endsWith("ZIP")) {
            compressionRatio = 1;
        } else if (fileName.toUpperCase().endsWith("GZ")) {
            compressionRatio = 1;
        }
        return new BigDecimal(fileSize / compressionRatio).longValue();
    }
}
