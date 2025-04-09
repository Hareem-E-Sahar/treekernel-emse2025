package org.eaasyst.eaa.servlets.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.eaasyst.eaa.data.UploadPostProcessor;
import org.eaasyst.eaa.resources.PropertiesManager;
import org.eaasyst.eaa.security.UserProfileManager;
import org.eaasyst.eaa.syst.EaasyStreet;
import org.eaasyst.eaa.syst.data.persistent.FileAttachment;
import org.eaasyst.eaa.syst.data.transients.UploadResult;
import org.eaasyst.eaa.utils.DateUtils;
import org.eaasyst.eaa.utils.FileUtils;
import org.eaasyst.eaa.utils.StringUtils;

/**
 * <p>This servlet provides generic upload services.</p>
 *
 * @version 2.9.1
 * @author Jeff Chilton
 */
public class UploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1;

    private static final String CONFIG_CONTEXT_KEY = "org.eaasyst.eaa.upload.LoadedConfigurations";

    private String systemName = "EaasyStreet";

    private String appRoot = null;

    /**
	 * <p>The Servlet "init" method.</p>
	 *
	 * @param config the <code>ServletConfig</code> object
	 * @since Eaasy Street 2.8
	 */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        systemName = config.getInitParameter("systemName");
        Map propertyMap = new HashMap();
        PropertiesManager manager = new PropertiesManager(config.getServletContext());
        Properties rawProps = manager.loadProperties("/WEB-INF/uploadServlet");
        if (rawProps != null && rawProps.size() > 0) {
            propertyMap.put("rawProperties", rawProps);
        }
        ServletContext context = config.getServletContext();
        context.setAttribute(CONFIG_CONTEXT_KEY, propertyMap);
        appRoot = context.getRealPath("");
    }

    /**
	 * <p>The Servlet "doGet" method -- the "GET" method is not supported
	 * in this servlet, since the whole purpose of the servlet is to process
	 * files uploaded with an HTTP "POST" method.</p>
	 *
	 * @param req the <code>HttpServletRequest</code> object
	 * @since Eaasy Street 2.8
	 */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        EaasyStreet.register(systemName, request);
        EaasyStreet.logError("[UploadServlet] This servlet does not process a \"GET\" request.");
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    /**
	 * <p>The Servlet "doPost" method -- this method is used to process
	 * files uploaded with an HTTP "POST" method.</p>
	 *
	 * @param req the <code>HttpServletRequest</code> object
	 * @since Eaasy Street 2.8
	 */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        EaasyStreet.register(systemName, request);
        if (FileUpload.isMultipartContent(request)) {
            DiskFileUpload upload = new DiskFileUpload();
            List items = new ArrayList();
            try {
                items = upload.parseRequest(request);
            } catch (FileUploadException e) {
                EaasyStreet.logError("[UploadServlet] Exception processing file upload: " + e.toString(), e);
            }
            Properties formFields = new Properties();
            Iterator i = items.iterator();
            while (i.hasNext()) {
                FileItem item = (FileItem) i.next();
                if (item.isFormField()) {
                    formFields.setProperty(item.getFieldName(), item.getString());
                }
            }
            if (formFields.containsKey("config")) {
                Properties configurationProperties = getConfigurationProperties(formFields);
                if (configurationProperties != null) {
                    processUpload(request, items, formFields, configurationProperties);
                    String responsePage = configurationProperties.getProperty("responsePage");
                    getServletContext().getRequestDispatcher(responsePage).forward(request, response);
                } else {
                    EaasyStreet.logError("[UploadServlet] No configuration properties loaded for key \"" + formFields.getProperty("config") + "\"");
                    response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
                }
            } else {
                EaasyStreet.logError("[UploadServlet] No configuration key present in request.");
                response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
            }
        } else {
            EaasyStreet.logError("[UploadServlet] This is not a multi-part request.");
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
        }
    }

    /**
	 * <p>This method iterates through the request components and processes all file
	 * elements.</p>
	 *
	 * @param req the <code>HttpServletRequest</code> object
	 * @param items the list of parts for this request
	 * @param props the configuration properties for this upload
	 * @return a String containing the name of the response page (.jsp)
	 * @since Eaasy Street 2.8
	 */
    private void processUpload(HttpServletRequest request, List items, Properties formFields, Properties props) {
        List results = new ArrayList();
        Iterator i = items.iterator();
        while (i.hasNext()) {
            FileItem item = (FileItem) i.next();
            if (!item.isFormField()) {
                UploadResult result = new UploadResult();
                String fileName = item.getName();
                String fileType = item.getContentType();
                long fileSize = item.getSize();
                if (fileName.indexOf('\\') > -1) {
                    fileName = fileName.substring(fileName.lastIndexOf('\\') + 1);
                }
                if (fileName.endsWith(".xls")) {
                    fileType = "application/vnd.ms-excel";
                }
                result.setFileName(fileName);
                result.setFileType(fileType);
                result.setFileSize(fileSize);
                if (!StringUtils.nullOrBlank(props.getProperty(fileType))) {
                    long maxSize = new Long(props.getProperty(fileType)).longValue();
                    if (!(fileSize > maxSize)) {
                        String separator = System.getProperty("file.separator");
                        fileName = appRoot + separator + "temp" + separator + result.getFileName();
                        File uploadedFile = new File(fileName);
                        try {
                            item.write(uploadedFile);
                            result.setFullUrl(props.getProperty("urlRoot") + "/" + result.getFileName());
                            result.setRelativeUrl(props.getProperty("uriRoot") + "/" + result.getFileName());
                            result.setResponseMessage("File successfully uploaded.");
                            result.setSuccessful(true);
                            if ("true".equalsIgnoreCase(props.getProperty("store.on.database"))) {
                                result = saveOnDatabase(result, formFields, uploadedFile, props.getProperty("hibernate.version.override"));
                            }
                        } catch (Exception e) {
                            EaasyStreet.logError("[UploadServlet] Exception saving updloaded file \"" + result.getFileName() + "\": " + e.toString(), e);
                            result.setResponseMessage("The system encountered an error while attempting to save this file on the server.");
                        }
                    } else {
                        EaasyStreet.logWarn("[UploadServlet] File of type (\"" + fileType + "\") not uploaded; size of " + fileSize + " exceeds maximum allowed (" + maxSize + ").");
                        result.setResponseMessage("File size of " + fileSize + "  exceeds the maximum allowed of " + maxSize + " for file type \"" + fileType + "\".");
                    }
                } else {
                    EaasyStreet.logWarn("[UploadServlet] Invalid file type (\"" + fileType + "\") not uploaded.");
                    result.setResponseMessage("Uploading files of type \"" + fileType + "\" is not supported in this system.");
                }
                results.add(result);
            }
        }
        if ((props.getProperty("uploadPostProcessorClass")) != null) {
            UploadPostProcessor uploadPostProcessor = (UploadPostProcessor) EaasyStreet.getInstance(props.getProperty("uploadPostProcessorClass"));
            if (uploadPostProcessor != null) {
                uploadPostProcessor.processUpload(request, formFields, results);
            }
        }
        request.setAttribute("uploadResults", results);
    }

    /**
	 * <p>This method creates a fileAttachment object and stores it in the
	 * application database.</p>
	 *
	 * @param result the <code>UploadResult</code> object
	 * @param file the <code>File</code> object
	 * @return the updated <code>UploadResult</code> object
	 * @since Eaasy Street 2.8
	 */
    public UploadResult saveOnDatabase(UploadResult result, Properties formFields, File file, String version) {
        String userId = UserProfileManager.getUserId().toLowerCase();
        Date rightNow = new Date();
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setCreationDate(rightNow);
        fileAttachment.setCreatedBy(userId);
        fileAttachment.setLastUpdate(rightNow);
        fileAttachment.setLastUpdateBy(userId);
        fileAttachment.setFileName(result.getFileName());
        fileAttachment.setFileType(result.getFileType());
        fileAttachment.setFileSize(new Long(result.getFileSize()).intValue());
        if (formFields.containsKey("description")) {
            fileAttachment.setDescription(formFields.getProperty("description"));
        } else {
            StringBuffer buffer = new StringBuffer();
            buffer.append("File \"");
            buffer.append(result.getFileName());
            buffer.append("\" uploaded by user \"");
            buffer.append(userId);
            buffer.append("\" from remote location \"");
            buffer.append(EaasyStreet.getServletRequest().getRemoteHost());
            buffer.append("\" on ");
            buffer.append(DateUtils.dateTimeToString(rightNow));
            fileAttachment.setDescription(buffer.toString());
        }
        InputStream is = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream os = null;
        BufferedOutputStream bos = null;
        try {
            is = new FileInputStream(file);
            bis = new BufferedInputStream(is);
            os = new ByteArrayOutputStream();
            bos = new BufferedOutputStream(os);
            int byteRead = 0;
            byte[] buf = new byte[bis.available()];
            while ((byteRead = bis.read(buf, 0, buf.length)) != -1) {
                bos.write(buf, 0, byteRead);
            }
        } catch (Exception e) {
            EaasyStreet.logError("Exception processing attachment: " + e.toString(), e);
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (Exception e) {
                    ;
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                    ;
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (Exception e) {
                    ;
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    ;
                }
            }
        }
        fileAttachment.setFileData(os.toByteArray());
        int id = FileUtils.saveFileAttachment(fileAttachment, version);
        if (id > -1) {
            String url = EaasyStreet.getServletRequest().getRequestURL().toString();
            url = url.substring(0, url.lastIndexOf("/"));
            result.setFullUrl(url + "/fetch?id=" + id);
            String uri = EaasyStreet.getServletRequest().getRequestURI();
            uri = uri.substring(0, uri.lastIndexOf("/"));
            result.setRelativeUrl(uri + "/fetch?id=" + id);
            result.setKey(new Integer(id));
        }
        return result;
    }

    /**
	 * <p>This method retrieves the configuration properties for this request.</p>
	 *
	 * @param formFields the field values from the request form
	 * @return the configuration properties for this request, or null if the properties
	 * are missing, invalid, or incomplete
	 * @since Eaasy Street 2.8
	 */
    public Properties getConfigurationProperties(Properties formFields) {
        Properties props = null;
        String configKey = formFields.getProperty("config");
        Properties baseProperties = getBaseProperties(configKey);
        if (baseProperties != null) {
            props = new Properties(baseProperties);
            if (formFields.containsKey("folder")) {
                props.setProperty("fileRoot", baseProperties.getProperty("fileRoot") + System.getProperty("file.separator") + formFields.getProperty("folder"));
                props.setProperty("urlRoot", baseProperties.getProperty("urlRoot") + "/" + formFields.getProperty("folder"));
                props.setProperty("uriRoot", baseProperties.getProperty("uriRoot") + "/" + formFields.getProperty("folder"));
            }
        }
        return props;
    }

    /**
	 * <p>This method retrieves the base configuration properties for this request.</p>
	 *
	 * @param configKey the config string from the request form
	 * @return the base configuration properties for this request, or null if the properties
	 * are missing, invalid, or incomplete
	 * @since Eaasy Street 2.8
	 */
    public Properties getBaseProperties(String configKey) {
        Properties props = null;
        Map propertyMap = (Map) EaasyStreet.getContextAttribute(CONFIG_CONTEXT_KEY);
        if (propertyMap.containsKey(configKey)) {
            props = (Properties) propertyMap.get(configKey);
        } else if (propertyMap.containsKey("rawProperties")) {
            props = loadProperties((Properties) propertyMap.get("rawProperties"), configKey);
            if (props != null) {
                propertyMap.put(configKey, props);
            }
        } else {
            EaasyStreet.logError("[UploadServlet] No configuration properties available for upload servlet.");
        }
        return props;
    }

    /**
	 * <p>This method validates and loads the configuration properties for a specific
	 * configuration key.</p>
	 *
	 * @param rawProperties all property values found in the property file of the web app
	 * @param configKey the config string from the request form
	 * @return the base configuration properties for this configuration key, or null if
	 * the properties are missing, invalid, or incomplete
	 * @since Eaasy Street 2.8
	 */
    public Properties loadProperties(Properties rawProperties, String configKey) {
        Properties props = new Properties();
        props = addRequiredProperty(props, rawProperties, configKey + ".responsePage", "responsePage");
        props = addRequiredProperty(props, rawProperties, configKey + ".fileRoot", "fileRoot");
        props = addRequiredProperty(props, rawProperties, configKey + ".urlRoot", "urlRoot");
        props = addRequiredProperty(props, rawProperties, configKey + ".uriRoot", "uriRoot");
        props = addRequiredProperty(props, rawProperties, configKey + ".fileTypes", "fileTypes");
        if (rawProperties.containsKey(configKey + ".hibernate.version.override")) {
            props.setProperty("hibernate.version.override", rawProperties.getProperty(configKey + ".hibernate.version.override"));
            EaasyStreet.logInfo("[UploadServlet] \"hibernate.version.override\": " + props.getProperty("hibernate.version.override"));
        }
        if (props.containsKey("errorCondition")) {
            props = null;
        } else {
            List fileTypes = StringUtils.split(props.getProperty("fileTypes"), ",");
            if (fileTypes.size() > 0) {
                Iterator i = fileTypes.iterator();
                while (i.hasNext()) {
                    String type = (String) i.next();
                    props = addRequiredProperty(props, rawProperties, configKey + "." + type + ".maxFileSize", type);
                }
                if (props.containsKey("errorCondition")) {
                    props = null;
                } else {
                    String key = configKey + ".store.on.database";
                    if (rawProperties.containsKey(key)) {
                        props.setProperty("store.on.database", rawProperties.getProperty(key));
                    }
                    key = configKey + ".uploadPostProcessorClass";
                    if (rawProperties.containsKey(key)) {
                        props.setProperty("uploadPostProcessorClass", rawProperties.getProperty(key));
                    }
                }
            } else {
                props = null;
                EaasyStreet.logError("[UploadServlet] No authorized file types specified for configuration key \"" + configKey + "\"");
            }
        }
        return props;
    }

    /**
	 * <p>This method validates and loads the configuration properties for a specific
	 * configuration key.</p>
	 *
	 * @param rawProperties all property values found in the property file of the web app
	 * @param configKey the config string from the request form
	 * @return the base configuration properties for this configuration key, or null if
	 * the properties are missing, invalid, or incomplete
	 * @since Eaasy Street 2.8
	 */
    public Properties addRequiredProperty(Properties props, Properties rawProperties, String rawProperty, String property) {
        if (rawProperties.containsKey(rawProperty)) {
            props.setProperty(property, rawProperties.getProperty(rawProperty));
            EaasyStreet.logInfo("[UploadServlet] \"" + property + "\": " + props.getProperty(property));
        } else {
            props.setProperty("errorCondition", "true");
            EaasyStreet.logError("[UploadServlet] Required property missing: \"" + rawProperty + "\"");
        }
        return props;
    }
}
