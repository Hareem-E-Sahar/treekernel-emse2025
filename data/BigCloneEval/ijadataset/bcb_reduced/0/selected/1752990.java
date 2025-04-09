package com.telstra.dynamicrva.servlet;

import java.io.*;
import java.text.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.telstra.ess.*;
import com.telstra.ess.alarming.*;
import com.telstra.ess.logging.*;
import com.telstra.ess.configuration.*;
import com.telstra.dynamicrva.servlet.helper.*;
import org.apache.commons.fileupload.*;
import org.w3c.dom.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.*;
import javax.xml.parsers.*;
import java.util.*;
import javax.sound.sampled.*;

/**
 *
 * @author c957258
 * @version
 */
public class DynamicRVAUploadServlet extends HttpServlet {

    /** Initializes the servlet.
     */
    private static final String DEF_REP_PATH = "/var/tmp";

    private static final String DEF_SIZE_THRESHOLD = "524288";

    private static final String DEF_STAGING_DIR_PATH = "/var/tmp";

    private static final String DEF_MAX_FILE_SIZE_BYTES_STR = "524288";

    private static final long DEF_MAX_FILE_SIZE_BYTES = 524288L;

    private static File stagingDirPath = null;

    private static EssLogger logger = null;

    private static ConfigurationManager confManager = null;

    private DiskFileUpload uploadHandler = null;

    private String timeToWaitStr = null;

    private long maxFileSizeBytes = 0L;

    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    private static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

    private static EssComponent component = new EssComponent("DynamicRVAUploadServlet");

    private Alarm uploadHandlerInitializationAlarm = new Alarm(11, Alarm.FATAL, "Could not initialize upload handler");

    private Alarm sessionEnvCreationAlarm = new Alarm(12, Alarm.CRITICAL, "Could not create environment for session");

    private Alarm sessionEnvRemovalAlarm = new Alarm(13, Alarm.MAJOR, "Could not clean up session environment");

    private Alarm processRequestAlarm = new Alarm(14, Alarm.MAJOR, "Could not process request");

    private Alarm failedUploadAlarm = new Alarm(16, Alarm.MAJOR, "File update failed!");

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        logger = LoggingManager.getEssLoggerInstance(component);
        logger.debug("Initializing multipart upload handler...");
        uploadHandler = new DiskFileUpload();
        uploadHandler.setSizeMax(-1);
        String inMemorySizeThresStr = DEF_SIZE_THRESHOLD;
        logger.debug("Loading repository path for upload handler");
        File repPath = loadUpdateRepositoryPath(component);
        logger.debug("Checking upload handler repository path: " + repPath);
        boolean uploadHandlerProblem = false;
        try {
            if (!repPath.canRead()) {
                logger.error("Upload handler repository path is not readable");
                uploadHandlerProblem = true;
            } else if (!repPath.canWrite()) {
                logger.error("Upload handler repository path is not writeable");
                uploadHandlerProblem = true;
            } else if (!repPath.exists()) {
                logger.error("Upload handler repository path does not exist");
                uploadHandlerProblem = true;
            } else {
                uploadHandlerProblem = false;
            }
        } catch (Exception e) {
            logger.error("Exception thrown while checking upload repository", e);
            uploadHandlerProblem = true;
        }
        if (uploadHandlerProblem) {
            logger.warn("Raising alarm for upload handler initialization");
            AlarmManager.getInstance(component).raise(uploadHandlerInitializationAlarm);
        } else {
            AlarmManager.getInstance(component).clear(uploadHandlerInitializationAlarm);
        }
        logger.debug("Attempting to load in-memory threshold...");
        long sizeThreshold = loadInMemoryThreshold(component);
        uploadHandler.setRepositoryPath(repPath.getAbsolutePath());
        uploadHandler.setSizeThreshold(new Long(sizeThreshold).intValue());
        logger.debug("Upload handler repository path: " + uploadHandler.getRepositoryPath());
        logger.debug("Upload handler in memory size threshold: " + uploadHandler.getSizeThreshold());
        logger.debug("Loading max file size...");
        maxFileSizeBytes = this.loadMaxFileSize(component);
        logger.debug("Max supported file size: " + this.maxFileSizeBytes);
        logger.debug("Loading staging directory...");
        stagingDirPath = this.loadUpdateStagingDirectory(component);
        boolean stagingDirPathProblem = false;
        logger.debug("Checking staging path: " + stagingDirPath.getAbsolutePath());
        try {
            if (!stagingDirPath.isDirectory()) {
                logger.error(stagingDirPath + " is not a directory");
                stagingDirPath = null;
                stagingDirPathProblem = true;
            } else if (!stagingDirPath.exists()) {
                logger.error(stagingDirPath + " does not exist");
                stagingDirPath = null;
                stagingDirPathProblem = true;
            } else if (!stagingDirPath.canRead()) {
                logger.error(stagingDirPath + " is not readable");
                stagingDirPath = null;
                stagingDirPathProblem = true;
            } else if (!stagingDirPath.canWrite()) {
                logger.error(stagingDirPath + " is not writeable");
                stagingDirPath = null;
                stagingDirPathProblem = true;
            } else {
                logger.debug(stagingDirPath + " is okay!");
                stagingDirPathProblem = false;
            }
        } catch (Exception e) {
            logger.error("Exception thrown while checking staging directory", e);
            stagingDirPathProblem = true;
        }
        if (stagingDirPathProblem) {
            logger.warn("Raising alarm for invalid staging environment");
            AlarmManager.getInstance(component).raise(sessionEnvCreationAlarm);
        } else {
            AlarmManager.getInstance(component).clear(sessionEnvCreationAlarm);
        }
        logger.debug("Using directory: " + this.stagingDirPath + " as file staging directory");
        logger.debug("Initialization complete...");
    }

    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Document responseDocument = null;
        File stagingDir = null;
        boolean sessionEnvCreated = false;
        boolean sessionCleanupOkay = true;
        DocumentBuilder builder = null;
        logger.debug("Processing request from: " + request.getRemoteAddr());
        logger.debug("Setting up request session environment");
        HttpSession s = request.getSession();
        try {
            builder = getDocumentBuilder();
        } catch (Exception e) {
            throw new ServletException("Could not create document builder for response", e);
        }
        try {
            sessionEnvCreated = ((stagingDir = this.createStagingEnvironment(component, s.getId())) != null);
            if (sessionEnvCreated) {
                logger.debug("Loading and validating request parameters");
                MultipleFileUpdateRequest mfuReq = loadAndValidateRequest(uploadHandler, request, stagingDir.getAbsolutePath());
                logger.debug("Initializing upload manager for request");
                FileUpdateManager updateManager = new FileUpdateManager(mfuReq.getCustomer());
                logger.debug("File requests: " + mfuReq.getFileUpdateRequests().size());
                logger.debug("Initiating update process(es)...");
                try {
                    MultipleFileUpdateResult mfuRes = updateManager.update(mfuReq);
                    logger.debug("Initializing DOM instance for result creation");
                    responseDocument = this.constructXMLResponse(builder, mfuRes);
                    AlarmManager.getInstance(component).clear(failedUploadAlarm);
                } catch (FileUpdateException e) {
                    logger.error("File update failed", e);
                    responseDocument = this.constructXMLErrorResponse(builder, e.expr(), e.getMessage());
                    AlarmManager.getInstance(component).raise(failedUploadAlarm);
                }
            } else {
                SessionEnvironmentException see = new SessionEnvironmentException("Could not create session environment for " + s.getId());
                responseDocument = this.constructXMLErrorResponse(builder, see.expr(), see.getMessage());
            }
        } catch (FileUpdateException e) {
            logger.error("Could not validate and load request", e);
            responseDocument = this.constructXMLErrorResponse(builder, e.expr(), e.getMessage());
        }
        logger.debug("Updates completed - Cleaning up session environment");
        if (sessionEnvCreated) {
            try {
                this.cleanupStagingEnvironment(stagingDir);
            } catch (FileUpdateException e) {
                logger.error("Could not clean up staging environment for: " + s.getId());
                sessionCleanupOkay = false;
            }
            if (!sessionCleanupOkay) {
                logger.error("Could not clean up session environment");
                AlarmManager.getInstance(component).raise(sessionEnvRemovalAlarm);
            } else {
                AlarmManager.getInstance(component).clear(sessionEnvRemovalAlarm);
            }
        }
        logger.debug("Invalidating session: " + s.getId());
        s.invalidate();
        try {
            if (responseDocument != null) {
                DOMSource source = new DOMSource(responseDocument);
                TransformerFactory tFactory = TransformerFactory.newInstance();
                Transformer transformer = tFactory.newTransformer();
                StreamResult result = new StreamResult(response.getOutputStream());
                transformer.transform(source, result);
                logger.debug("XML result closed");
            } else {
                logger.error("Response document could not be created");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            logger.error("Could transform response document to output stream!", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        logger.debug("Request processing complete!");
    }

    private Document constructXMLErrorResponse(DocumentBuilder builder, String reason, String desc) {
        if (builder == null) {
            logger.error("Could not create error response, no builder");
            return null;
        }
        try {
            Document responseDocument = builder.newDocument();
            Node responseNode = responseDocument.createElement("file-update-response");
            Node errorNode = responseDocument.createElement("file-update-error");
            Node errorTimeNode = responseDocument.createElement("error-time");
            Node errorCodeNode = responseDocument.createElement("error-code");
            Node errorDescNode = responseDocument.createElement("error-description");
            Date currentTime = new Date();
            Node errorTimeTextNode = responseDocument.createTextNode(this.timeFormatter.format(currentTime));
            Node errorCodeTextNode = responseDocument.createTextNode(reason);
            Node errorDescTextNode = responseDocument.createTextNode(desc);
            errorTimeNode.appendChild(errorTimeTextNode);
            errorCodeNode.appendChild(errorCodeTextNode);
            errorDescNode.appendChild(errorDescTextNode);
            errorNode.appendChild(errorTimeNode);
            errorNode.appendChild(errorCodeNode);
            errorNode.appendChild(errorDescNode);
            responseNode.appendChild(errorNode);
            responseDocument.appendChild(responseNode);
            return responseDocument;
        } catch (Exception e) {
            logger.error("Could not construct error response", e);
            return null;
        }
    }

    private Document constructXMLResponse(DocumentBuilder builder, MultipleFileUpdateResult result) {
        if (builder == null) {
            logger.error("Could not create update response document, no builder");
            return null;
        }
        try {
            Document responseDocument = builder.newDocument();
            Node responseNode = responseDocument.createElement("file-update-response");
            responseDocument.appendChild(responseNode);
            if (result != null) {
                String customerName = result.getCustomer();
                if (customerName != null) {
                    Node customerNameNode = responseDocument.createElement("customer-name");
                    Node customerNameTextNode = responseDocument.createTextNode(customerName);
                    customerNameNode.appendChild(customerNameTextNode);
                    responseNode.appendChild(customerNameNode);
                }
                List resultsList = result.getUpdateResults();
                if (resultsList != null) {
                    Iterator i = resultsList.iterator();
                    Node fileUpdateResultsNode = responseDocument.createElement("file-update-results");
                    responseNode.appendChild(fileUpdateResultsNode);
                    while (i.hasNext()) {
                        FileUpdateResult fur = (FileUpdateResult) i.next();
                        String serviceId = fur.getServiceId();
                        String newFile = (fur.getNewFile() != null) ? fur.getNewFile().getName() : "Unknown";
                        String oldFile = (fur.getOldFile() != null) ? fur.getOldFile().getName() : "Unknown";
                        Date updateTime = fur.getUpdateEndTime();
                        String updateTimeStr = null, updateDateStr = null;
                        if (updateTime != null) {
                            updateTimeStr = timeFormatter.format(updateTime);
                            updateDateStr = dateFormatter.format(updateTime);
                        } else {
                            updateTimeStr = "Unknown";
                            updateDateStr = "Unknown";
                        }
                        String updateResult = fur.getResult();
                        Node singleFileUpdateResultNode = responseDocument.createElement("single-file-update-result");
                        String status = fur.getStatus();
                        Node serviceIdTextNode = responseDocument.createTextNode(serviceId);
                        Node newFileTextNode = responseDocument.createTextNode(newFile);
                        Node oldFileTextNode = responseDocument.createTextNode(oldFile);
                        Node updateTimeTextNode = responseDocument.createTextNode(updateTimeStr);
                        Node updateDateTextNode = responseDocument.createTextNode(updateDateStr);
                        Node updateStatusTextNode = responseDocument.createTextNode(status);
                        Node updateResultTextNode = responseDocument.createTextNode(updateResult);
                        Node serviceIdNode = responseDocument.createElement("service-id");
                        serviceIdNode.appendChild(serviceIdTextNode);
                        singleFileUpdateResultNode.appendChild(serviceIdNode);
                        Node oldFileNode = responseDocument.createElement("old-file-name");
                        oldFileNode.appendChild(oldFileTextNode);
                        singleFileUpdateResultNode.appendChild(oldFileNode);
                        Node newFileNode = responseDocument.createElement("new-file-name");
                        newFileNode.appendChild(newFileTextNode);
                        singleFileUpdateResultNode.appendChild(newFileNode);
                        Node updateTimeNode = responseDocument.createElement("update-time");
                        updateTimeNode.appendChild(updateTimeTextNode);
                        singleFileUpdateResultNode.appendChild(updateTimeNode);
                        Node updateDateNode = responseDocument.createElement("update-date");
                        updateDateNode.appendChild(updateDateTextNode);
                        singleFileUpdateResultNode.appendChild(updateDateNode);
                        Node updateStatusNode = responseDocument.createElement("status");
                        updateStatusNode.appendChild(updateStatusTextNode);
                        singleFileUpdateResultNode.appendChild(updateStatusNode);
                        Node updateResultNode = responseDocument.createElement("result");
                        updateResultNode.appendChild(updateResultTextNode);
                        singleFileUpdateResultNode.appendChild(updateResultNode);
                        fileUpdateResultsNode.appendChild(singleFileUpdateResultNode);
                    }
                    responseNode.appendChild(fileUpdateResultsNode);
                }
            }
            return responseDocument;
        } catch (Exception e) {
            logger.error("Could not create update response document", e);
            return null;
        }
    }

    private MultipleFileUpdateRequest loadAndValidateRequest(DiskFileUpload handler, HttpServletRequest request, String stagingDir) throws FileUpdateException {
        boolean customerNameSet = false;
        boolean timeOutSet = false;
        boolean requestValid = true;
        boolean allFilesOkay = true;
        String customerName = null;
        List params = null;
        if (request != null) {
            if (request.getMethod().equalsIgnoreCase("post")) {
                logger.debug("Loading request parameters...");
                try {
                    params = handler.parseRequest(request);
                    logger.debug("Request parameters loaded!");
                } catch (FileUploadException e) {
                    logger.error("Could not handle upload request", e);
                    throw new MalformedRequestException("Could not parse request", e);
                }
            } else {
                throw new MalformedRequestException("Invalid request method: " + request.getMethod());
            }
        } else {
            throw new MalformedRequestException("No request object");
        }
        if (params != null) {
            logger.debug("Validating request parameters...");
            Iterator paramIter = params.iterator();
            while (paramIter.hasNext()) {
                FileItem paramItem = (FileItem) paramIter.next();
                if (paramItem.isFormField()) {
                    if (paramItem.getFieldName().equalsIgnoreCase("customername")) {
                        if (!customerNameSet) {
                            customerName = paramItem.getString();
                            if (customerName == null || customerName.length() == 0) {
                                customerNameSet = false;
                            } else {
                                customerNameSet = true;
                            }
                        } else {
                            logger.error("Too many CustomerName parameters");
                            throw new MultipleCustomerException("Too many CustomerName parameters");
                        }
                    }
                } else {
                    logger.debug("Checking file information for " + paramItem.getFieldName());
                    String fileUpdateParamName = paramItem.getFieldName();
                    if (fileUpdateParamName.toLowerCase().startsWith(new String("UpdateFile-").toLowerCase())) {
                        String rem = fileUpdateParamName.substring(new String("UpdateFile-").length()).trim();
                        if (rem.length() == 0) {
                            throw new InvalidServiceIdException(fileUpdateParamName + " is invalid");
                        } else {
                            logger.debug("Service ID [" + rem + "] is okay ");
                        }
                    } else {
                        logger.error("File parameter incorrectly encoded: " + fileUpdateParamName);
                        throw new InvalidParameterException(fileUpdateParamName + " is not valid for this service");
                    }
                    maxFileSizeBytes = loadMaxFileSize(component);
                    logger.debug("Reloaded max file size: " + maxFileSizeBytes + ", checking file size...");
                    if (paramItem.getSize() > this.maxFileSizeBytes) {
                        logger.error("Parameter: [" + fileUpdateParamName + "], has file: " + paramItem.getName() + " which is too large: " + paramItem.getSize());
                        throw new FileSizeException(paramItem.getName() + " is too large");
                    } else if (paramItem.getSize() == 0) {
                        logger.error("Parameter: [" + fileUpdateParamName + "], has file: " + paramItem.getName() + " which is empty (0 bytes)");
                        throw new FileEmptyException(paramItem.getFieldName() + " had no file or file was empty");
                    }
                }
            }
            if (!customerNameSet) {
                logger.error("CustomerName parameter not found!");
                throw new NoCustomerException("CustomerName parameter not found!");
            }
            MultipleFileUpdateRequest mfuReq = new MultipleFileUpdateRequest(customerName);
            Iterator fIter = params.iterator();
            while (fIter.hasNext()) {
                FileItem fi = (FileItem) fIter.next();
                if (!fi.isFormField()) {
                    String fileUpdateParamName = fi.getFieldName();
                    logger.debug("Current upload file: " + fileUpdateParamName);
                    if (fileUpdateParamName.toLowerCase().startsWith(new String("UpdateFile-").toLowerCase())) {
                        String id = fileUpdateParamName.substring(new String("UpdateFile-").length()).trim();
                        try {
                            String fileNameOnly = this.getFileNameOnly(fi.getName());
                            File fullStagedFilePath = new File(stagingDir + File.separator + fileNameOnly);
                            logger.debug("Saving: " + fullStagedFilePath);
                            if (fullStagedFilePath.exists()) {
                                logger.warn(fullStagedFilePath + " already exists. Deleting...");
                                if (!fullStagedFilePath.delete()) {
                                    throw new RequestCreationException("Could not delete " + fullStagedFilePath);
                                }
                            }
                            fi.write(fullStagedFilePath);
                            if (isSupportedAudioFormat(fullStagedFilePath)) {
                                FileUpdateRequest fur = new FileUpdateRequest(customerName, id, fullStagedFilePath);
                                mfuReq.addFileUpdateRequest(fur);
                            } else {
                                logger.error("File: " + fullStagedFilePath.getAbsolutePath() + ", specified with: " + fileUpdateParamName + " is not of a supported audio format");
                                throw new UnsupportedAudioException("File: " + fullStagedFilePath.getAbsolutePath() + ", specified with: " + fileUpdateParamName + " is not of a supported audio format");
                            }
                        } catch (Exception e) {
                            logger.error("Could not add file request for: " + fileUpdateParamName);
                            throw new RequestCreationException("Could not add file request for: " + fileUpdateParamName, e);
                        }
                    }
                }
            }
            return mfuReq;
        } else {
            logger.error("No parameters in request");
            throw new MalformedRequestException("No parameters");
        }
    }

    private String getFileNameOnly(String fullPath) {
        if (fullPath == null) {
            return null;
        }
        logger.debug("Extracting filename from: " + fullPath);
        String fileName = null;
        StringTokenizer strtok = new StringTokenizer(fullPath, "\\/");
        while (strtok.hasMoreElements()) {
            fileName = strtok.nextToken();
        }
        logger.debug("Filename: " + fileName);
        return fileName;
    }

    private File createStagingEnvironment(EssComponent comp, String sessionID) throws FileUpdateException {
        File stagingDir = null;
        try {
            if (this.stagingDirPath == null) {
                logger.debug("Loading staging directory...");
                stagingDirPath = this.loadUpdateStagingDirectory(comp);
                logger.debug("Checking staging path: " + stagingDirPath.getAbsolutePath());
                if (!stagingDirPath.isDirectory()) {
                    logger.error(stagingDirPath + " is not a directory");
                    stagingDirPath = null;
                } else if (!stagingDirPath.canRead()) {
                    logger.error(stagingDirPath + " is not readable");
                    stagingDirPath = null;
                } else if (!stagingDirPath.canWrite()) {
                    logger.error(stagingDirPath + " is not writeable");
                    stagingDirPath = null;
                } else {
                    logger.debug(stagingDirPath + " is okay!");
                }
                logger.debug("Using directory: " + this.stagingDirPath + " as file staging directory");
                if (stagingDirPath == null) {
                    throw new SessionEnvironmentException(stagingDirPath + " is not valid as a staging directory");
                }
            }
            stagingDir = new File(this.stagingDirPath + File.separator + sessionID);
            logger.debug("Creating file space for session: " + sessionID + ", at " + stagingDir);
            if (stagingDir.mkdir()) {
                if (stagingDir.exists() && stagingDir.isDirectory()) {
                    return stagingDir;
                } else {
                    return null;
                }
            } else {
                logger.error("Could not create staging directory: " + stagingDir.getAbsolutePath());
                throw new SessionEnvironmentException("Existence check failed for: " + stagingDir.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.error("Could not establish staging environment", e);
            throw new SessionEnvironmentException("Could not create staging environment", e);
        }
    }

    private void cleanupStagingEnvironment(File stagingDir) throws FileUpdateException {
        if (stagingDir != null && stagingDir.exists() && stagingDir.isDirectory()) {
            File[] files = stagingDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (!f.delete()) {
                    logger.error("Could not delete: " + f.getAbsolutePath());
                    throw new SessionEnvironmentException("Could not delete: " + f.getAbsolutePath());
                }
            }
            if (stagingDir.exists()) {
                if (stagingDir.delete()) {
                    logger.debug(stagingDir + " was successfully deleted");
                } else {
                    logger.error("Could not clean up staging directory: " + stagingDir.getAbsolutePath());
                    throw new SessionEnvironmentException("Could not remove directory: " + stagingDir.getAbsolutePath());
                }
            }
        } else {
            throw new SessionEnvironmentException("Staging directory undefined, does not exist, or is not a directory");
        }
    }

    private File loadUpdateRepositoryPath(EssComponent comp) {
        File repPath = new File(DEF_REP_PATH);
        if (comp == null) {
            return repPath;
        }
        if (confManager == null) {
            confManager = ConfigurationManager.getInstance(comp);
        }
        try {
            repPath = confManager.getFileConfigurationItem("dynamicrvaupload.upload.reppath", null);
            if (repPath != null) {
                if (repPath.exists() && repPath.isDirectory() && repPath.isAbsolute()) {
                    return repPath;
                } else {
                    logger.warn("Repository path: " + repPath.getAbsolutePath() + " is not valid, loading default");
                    repPath = this.loadDefaultDirectory();
                    if (repPath == null) {
                        logger.error("Default path was invalid. Loading application default");
                        return new File(DEF_REP_PATH);
                    }
                }
            } else {
                repPath = loadDefaultDirectory();
                if (repPath == null) {
                    return new File(DEF_REP_PATH);
                }
            }
        } catch (Exception e) {
            logger.warn("Could not load repository path. Assigning default", e);
            repPath = new File(DEF_REP_PATH);
        }
        return repPath;
    }

    private File loadDefaultDirectory() {
        String path = (System.getProperty("java.io.tmpdir") != null) ? System.getProperty("java.io.tmpdir") : "";
        if (path != null) {
            return new File(path);
        } else {
            return null;
        }
    }

    private long loadInMemoryThreshold(EssComponent comp) {
        long sizeThreshDef = 524888;
        long sizeThresh = 0;
        if (comp == null) {
            return sizeThreshDef;
        }
        if (confManager == null) {
            confManager = ConfigurationManager.getInstance(comp);
        }
        try {
            String sizeThreshStr = confManager.getConfigurationItem("dynamicrvaupload.upload.inmemorythreshold", null);
            if (sizeThreshStr == null) {
                sizeThresh = sizeThreshDef;
            } else {
                sizeThresh = Long.parseLong(sizeThreshStr);
                if (sizeThresh < 0) {
                    sizeThresh = sizeThreshDef;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not load in-memory size threshold. Assigning default", e);
            sizeThresh = sizeThreshDef;
        }
        return sizeThresh;
    }

    private long loadMaxFileSize(EssComponent comp) {
        long maxSizeDef = 524888;
        long maxSize = 0;
        if (comp == null) {
            return maxSizeDef;
        }
        if (confManager == null) {
            confManager = ConfigurationManager.getInstance(comp);
        }
        try {
            String sizeThreshStr = confManager.getConfigurationItem("dynamicrvaupload.upload.maxfilesize", null);
            if (sizeThreshStr == null) {
                maxSize = maxSizeDef;
            } else {
                maxSize = Long.parseLong(sizeThreshStr);
                if (maxSize < 0) {
                    maxSize = maxSizeDef;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not load max file size parameter. Assigning default", e);
            maxSize = maxSizeDef;
        }
        return maxSize;
    }

    private File loadUpdateStagingDirectory(EssComponent comp) {
        File stagingDir = new File(DEF_STAGING_DIR_PATH);
        if (component == null) {
            return stagingDir;
        }
        if (confManager == null) {
            confManager = ConfigurationManager.getInstance(comp);
        }
        try {
            stagingDir = confManager.getFileConfigurationItem("dynamicrvaupload.upload.stagingdirpath", null);
            if (stagingDir != null) {
                if (stagingDir.exists() && stagingDir.isDirectory() && stagingDir.isAbsolute()) {
                    return stagingDir;
                } else {
                    logger.warn("Repository path: " + stagingDir.getAbsolutePath() + " is not valid, loading default");
                    return new File(DEF_STAGING_DIR_PATH);
                }
            } else {
                return new File(DEF_STAGING_DIR_PATH);
            }
        } catch (Exception e) {
            logger.warn("Could not load repository path. Assigning default", e);
            stagingDir = new File(DEF_STAGING_DIR_PATH);
        }
        return stagingDir;
    }

    private DocumentBuilder getDocumentBuilder() throws Exception {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            logger.debug("Created document builder!");
            return builder;
        } catch (Exception e) {
            logger.debug("Could not construct response document builder", e);
            throw e;
        }
    }

    private boolean isSupportedAudioFormat(File f) throws IOException, FileNotFoundException {
        if (f == null) {
            throw new IOException("File was null");
        } else if (!f.exists()) {
            throw new IOException("File: " + f.getAbsolutePath() + " does not exist");
        } else if (!f.canRead()) {
            throw new IOException("File: " + f.getAbsolutePath() + " is not readable");
        }
        logger.debug("Verifying audio format of " + f.getAbsolutePath());
        try {
            AudioFileFormat format = AudioSystem.getAudioFileFormat(f);
            if (format.getType() == AudioFileFormat.Type.WAVE) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }

    /** Destroys the servlet.
     */
    public synchronized void destroy() {
        logger.warn("Servlet destroyed");
    }
}
