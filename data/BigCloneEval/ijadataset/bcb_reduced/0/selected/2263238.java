package org.fspmboard.server.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.fspmboard.server.util.FileSize;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Controller which handles the upload of fspmmodel file contents</br>
 * mutli file upload possible zips all files to one and saves it to the dataPath on the local disk.
 * @author Holz, Roberto 06.12.2008 | 15:47:02
 */
public class UploadController extends AbstractController {

    static final int BUFFER = 2048;

    protected static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    private long maxSize;

    private String dataPath;

    private String modelName;

    private List<File> uploadedfiles;

    private JSONResponse responseContent;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.debug("UploadController handles Request");
        request.setCharacterEncoding("utf-8");
        initialize();
        String formId = (String) request.getParameter("formId");
        logger.debug("formId: {}", formId);
        if (formId != null && StringUtils.isNotBlank(formId)) {
            uploadedfiles = loadFiles(request, response);
            if (uploadedfiles.size() == 1) {
                zipFiles(uploadedfiles, modelName + ".zip");
            } else {
                if (!uploadedfiles.isEmpty()) zipFiles(uploadedfiles, modelName + ".zip");
            }
        } else {
            responseContent.setErrorMsg("Error not Id of the form submitted!");
        }
        writeResponse(response, responseContent);
        if (uploadedfiles != null && !uploadedfiles.isEmpty()) cleanTempFiles(uploadedfiles);
        return null;
    }

    private void initialize() {
        File dataDir = new File(dataPath);
        try {
            if (!dataDir.isDirectory()) {
                FileUtils.forceMkdir(dataDir);
            }
        } catch (IOException exc) {
            logger.error("Couldn't create Data Dir!");
        }
        logger.debug("dataPath: {}", dataDir.getAbsolutePath());
        logger.debug("fileSize: {}", maxSize);
        responseContent = new JSONResponse();
    }

    private List<File> loadFiles(HttpServletRequest request, HttpServletResponse response) {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "FSPM_BOARD");
        try {
            if (!tempDir.isDirectory()) {
                FileUtils.forceMkdir(tempDir);
            }
        } catch (IOException exc) {
            logger.error("Couldn't create Data Dir!");
        }
        logger.debug("tempPath: {}", tempDir.getAbsolutePath());
        ServletFileUpload upload = new ServletFileUpload();
        upload.setSizeMax(maxSize);
        List<File> uploadedFiles = new ArrayList<File>();
        try {
            FileItemIterator iter = upload.getItemIterator(request);
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                String name = item.getFieldName();
                InputStream stream = item.openStream();
                if (item.isFormField()) {
                    if (name.equals("name")) {
                        modelName = Streams.asString(stream);
                    }
                } else {
                    logger.debug("File field {} with file name {} detected.", name, item.getName());
                    try {
                        File file = new File(tempDir.getPath(), item.getName());
                        logger.debug("TempFilePath: {}", file.getAbsolutePath());
                        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                        int count;
                        byte data[] = new byte[BUFFER];
                        while ((count = stream.read(data, 0, BUFFER)) != -1) {
                            out.write(data, 0, count);
                        }
                        out.close();
                        uploadedFiles.add(file);
                    } catch (IOException io) {
                        logger.error("Couln't read File! Msg: {}", io.getMessage());
                        responseContent.setErrorMsg(io.getMessage());
                    } finally {
                        IOUtils.closeQuietly(stream);
                    }
                }
            }
        } catch (FileUploadException fu) {
            String err = fu.getMessage();
            logger.error("FileUploadException! Msg: {}", err);
            if (fu instanceof SizeLimitExceededException) {
                StringBuilder b = new StringBuilder();
                b.append("Uploadsize (").append(FileSize.getFileSizeAsString((long) request.getContentLength())).append(") exceeds the configured maximum (").append(FileSize.getFileSizeAsString(maxSize)).append(")");
                err = b.toString();
            }
            responseContent.setErrorMsg(err);
        } catch (IOException io) {
            logger.error("general IOError while getting Form Items or Filestream! Msg: {}", io.getMessage());
            responseContent.setErrorMsg(io.getMessage());
        }
        return uploadedFiles;
    }

    private void zipFiles(List<File> files, String modelFileName) {
        logger.debug("zipping files");
        try {
            BufferedInputStream origin = null;
            File zip = new File(dataPath, modelFileName);
            FileOutputStream dest = new FileOutputStream(zip);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            HashSet<String> fileNames = new HashSet<String>(files.size());
            byte data[] = new byte[BUFFER];
            for (File file : files) {
                logger.debug("Adding: {}", file.getName());
                FileInputStream fi = new FileInputStream(file);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry;
                if (fileNames.add(file.getName())) entry = new ZipEntry(file.getName()); else {
                    entry = new ZipEntry(file.getName() + "_1");
                    fileNames.add(entry.getName());
                }
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
            responseContent = new JSONResponse(true, zip.getAbsolutePath(), FileSize.getFileSizeAsString(zip.length()));
        } catch (Exception e) {
            logger.error("Error while packing files to zip! msg: {}", e.getMessage());
            responseContent.setErrorMsg(e.getMessage());
        }
    }

    protected void writeResponse(HttpServletResponse response, Object content) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");
        response.getWriter().write(new JSONObject(content).toString());
        response.getWriter().flush();
        response.getWriter().close();
    }

    /**
	 * TASK replace later with automatic deleting by Scheduler job 
	 * 
	 * @param files
	 */
    private void cleanTempFiles(List<File> files) {
        for (File file : files) {
            file.delete();
        }
    }

    public void setMaxSize(long maxFileSize) {
        this.maxSize = maxFileSize;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }
}
