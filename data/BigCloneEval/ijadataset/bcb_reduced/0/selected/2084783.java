package be.fedict.eid.dss.sp.servlet;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.util.encoders.Base64;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class UploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Log LOG = LogFactory.getLog(UploadServlet.class);

    public static final String DOCUMENT_SESSION_ATTRIBUTE = UploadServlet.class.getName() + ".Document";

    private static final String POST_PAGE_INIT_PARAM = "PostPage";

    private String postPage;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.postPage = config.getInitParameter(POST_PAGE_INIT_PARAM);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOG.debug("doPost");
        String fileName = null;
        String contentType;
        byte[] document = null;
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        try {
            List<FileItem> items = upload.parseRequest(request);
            if (!items.isEmpty()) {
                fileName = items.get(0).getName();
                document = items.get(0).get();
            }
        } catch (FileUploadException e) {
            throw new ServletException(e);
        }
        String extension = FilenameUtils.getExtension(fileName).toLowerCase();
        contentType = supportedFileExtensions.get(extension);
        if (null == contentType) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOutputStream.putNextEntry(zipEntry);
            IOUtils.write(document, zipOutputStream);
            zipOutputStream.close();
            fileName = FilenameUtils.getBaseName(fileName) + ".zip";
            document = outputStream.toByteArray();
            contentType = "application/zip";
        }
        LOG.debug("File name: " + fileName);
        LOG.debug("Content Type: " + contentType);
        String signatureRequest = new String(Base64.encode(document));
        request.getSession().setAttribute(DOCUMENT_SESSION_ATTRIBUTE, document);
        request.getSession().setAttribute("SignatureRequest", signatureRequest);
        request.getSession().setAttribute("ContentType", contentType);
        response.sendRedirect(request.getContextPath() + this.postPage);
    }

    private static final Map<String, String> supportedFileExtensions;

    static {
        supportedFileExtensions = new HashMap<String, String>();
        supportedFileExtensions.put("xml", "text/xml");
        supportedFileExtensions.put("odt", "application/vnd.oasis.opendocument.text");
        supportedFileExtensions.put("ods", "application/vnd.oasis.opendocument.spreadsheet");
        supportedFileExtensions.put("odp", "application/vnd.oasis.opendocument.presentation");
        supportedFileExtensions.put("odg", "application/vnd.oasis.opendocument.graphics");
        supportedFileExtensions.put("odc", "application/vnd.oasis.opendocument.chart");
        supportedFileExtensions.put("odf", "application/vnd.oasis.opendocument.formula");
        supportedFileExtensions.put("odi", "application/vnd.oasis.opendocument.image");
        supportedFileExtensions.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        supportedFileExtensions.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        supportedFileExtensions.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        supportedFileExtensions.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
        supportedFileExtensions.put("zip", "application/zip");
    }
}
