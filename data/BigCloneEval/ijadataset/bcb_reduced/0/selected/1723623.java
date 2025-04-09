package org.gbif.portal.web.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gbif.portal.dto.resources.DataResourceDTO;
import org.gbif.portal.io.DataResourceAuditor;
import org.gbif.portal.io.OutputProperty;
import org.gbif.portal.service.DataResourceManager;
import org.gbif.portal.util.log.GbifLogMessage;
import org.gbif.portal.util.log.GbifLogUtils;
import org.gbif.portal.util.log.LogEvent;
import org.gbif.portal.util.log.LogGroup;
import org.gbif.portal.web.content.occurrence.CitationCreator;
import org.springframework.context.MessageSource;

/**
 * Utility class for download functionality
 * 
 * @author dmartin
 */
public class DownloadUtils {

    protected static final Log logger = LogFactory.getLog(DownloadUtils.class);

    /** Extension indicating file generation in progress */
    public static final String temporaryExtensionIndicator = ".tmp";

    /** Extension indicating file generation in failed */
    public static final String failedExtensionIndicator = ".failed";

    /** the file extension for descriptors */
    public static final String downloadDescriptorExtension = ".properties";

    /** The extension used for zipped downloads */
    public static String zippedFileExtension = ".zip";

    /** the base url for dataset drilldown pages */
    protected String datasetBaseUrl = "/datasets/resource/";

    /** The GBIF log utils for logging download events */
    protected GbifLogUtils gbifLogUtils;

    /** data resource manager */
    protected DataResourceManager dataResourceManager;

    /** i18n messaging */
    protected MessageSource messageSource;

    /** Property file values */
    public static String originalUrlKey = "originalUrl";

    public static String fileTypeKey = "fileType";

    public static String fileDescriptionKey = "fileDescription";

    public static String displayTimeTakenKey = "displayTimeTaken";

    public static String createDateKey = "createdDate";

    public static String completionRedirectUrlKey = "completionRedirectUrl";

    /**
	 * Starts a file writer thread returning the filename of the completed file.
	 * While files are being written they are suffixed with ".tmp" to indicate work in progress.
	 * On completion this suffix is removed to indicate the file has been written successfully.
	 *  
	 * @param request
	 * @param response
	 * @param downloadFieldMappings
	 * @param outputProcess
	 * @param queryDescription
	 * @param requestedFields
	 * @param fwFactory
	 * @param downloadFileNamePrefix
	 * @param searchId
	 * @param zipped
	 * @return the file name of the completed file
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
    public static String startFileWriter(HttpServletRequest request, HttpServletResponse response, List<Field> requestedFields, Map<String, OutputProperty> downloadFieldMappings, OutputProcess outputProcess, List<SecondaryOutput> secondaryOutputs, String queryDescription, FileWriterFactory fwFactory, String downloadFileNamePrefix, String downloadId, boolean zipped) throws IOException, FileNotFoundException {
        File outputFile = createTempFileOutput(downloadFileNamePrefix, downloadId, fwFactory.getFileExtension(), zipped);
        OutputStream outputStream = createOutputStream(outputFile, downloadFileNamePrefix, downloadId, fwFactory.getFileExtension(), zipped);
        Runnable cfw = fwFactory.createFileWriter(request, response, requestedFields, downloadFieldMappings, outputProcess, secondaryOutputs, outputFile, outputStream, queryDescription);
        Thread t = new Thread(cfw);
        t.start();
        String fileName = outputFile.getName();
        String downloadFileName = getFileNameWithoutTempMarker(fileName);
        return downloadFileName;
    }

    /**
	 * Get file name without temp marker.
	 * 
	 * @param fileName
	 * @return
	 */
    public static String getFileNameWithoutTempMarker(String fileName) {
        String downloadFileName = fileName.substring(0, fileName.length() - temporaryExtensionIndicator.length());
        return downloadFileName;
    }

    /**
	 * Create a temporary file for output.
	 * 
	 * @param downloadFileNamePrefix
	 * @param searchId
	 * @param fileExtension
	 * @param zipped
	 * @return
	 */
    public static File createTempFileOutput(String downloadFileNamePrefix, String downloadId, String fileExtension, boolean zipped) throws IOException {
        String fileName = downloadFileNamePrefix + downloadId;
        File tempFile = null;
        if (zipped) {
            tempFile = File.createTempFile(fileName, zippedFileExtension + temporaryExtensionIndicator);
        } else {
            tempFile = File.createTempFile(fileName, fileExtension + temporaryExtensionIndicator);
        }
        fileName = tempFile.getName();
        return tempFile;
    }

    /**
	 * Create an output stream using the supplied file as an output stream, creating a zip entry if zipped = true.
	 * 
	 * @param tempFile
	 * @param downloadFileNamePrefix
	 * @param searchId
	 * @param fileExtension
	 * @param zipped
	 * @return
	 * @throws IOException
	 */
    public static OutputStream createOutputStream(File tempFile, String downloadFileNamePrefix, String downloadId, String fileExtension, boolean zipped) throws IOException {
        OutputStream outputStream = null;
        if (zipped) {
            outputStream = new ZipOutputStream(new FileOutputStream(tempFile));
            ((ZipOutputStream) outputStream).putNextEntry(new ZipEntry(downloadFileNamePrefix + downloadId + fileExtension));
        } else {
            outputStream = new FileOutputStream(tempFile);
        }
        return outputStream;
    }

    /**
	 * Creates a download descriptor for this file. 
	 * 
	 * @param request
	 * @param fileName
	 * @param originalUrl
	 * @param fileType
	 * @param queryDescription
	 * @param displayTimeTaken
	 * @param completionRedirectUrl nullable
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
    public static void writeDownloadToDescriptor(HttpServletRequest request, String fileNameWithoutExtension, String originalUrl, String fileType, String queryDescription, boolean displayTimeTaken, String completionRedirectUrl) throws IOException, FileNotFoundException {
        String downloadDirectory = System.getProperty("java.io.tmpdir");
        File fileDescriptor = new File(downloadDirectory + File.separator + fileNameWithoutExtension + downloadDescriptorExtension);
        FileOutputStream fileOutput = new FileOutputStream(fileDescriptor);
        Properties properties = new Properties();
        properties.put(originalUrlKey, originalUrl);
        properties.put(fileTypeKey, fileType);
        properties.put(fileDescriptionKey, queryDescription);
        properties.put(displayTimeTakenKey, Boolean.toString(displayTimeTaken));
        if (completionRedirectUrl != null) {
            properties.put(completionRedirectUrlKey, completionRedirectUrl);
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdfDate = new SimpleDateFormat("ddMMyyyy HHmm.ss");
        properties.put(createDateKey, sdfDate.format(now));
        properties.store(fileOutput, null);
    }

    /**
	 * Outputs a citation to the supplied output stream. If the stream is zipped, it will create an entry of the specified <code>citationFileName</code>.
	 * 
	 * @param outputStream
	 * @param resultsOutputter
	 * @param citationFileName
	 * @param locale
	 * @param hostUrl
	 * @throws IOException
	 */
    public void outputCitation(OutputStream outputStream, DataResourceAuditor resultsOutputter, String citationFileName, Locale locale, String hostUrl) throws IOException {
        if (outputStream instanceof ZipOutputStream) {
            ((ZipOutputStream) outputStream).putNextEntry(new ZipEntry(citationFileName));
        }
        Map<String, String> dataResources = resultsOutputter.getDataResources();
        CitationCreator cc = new CitationCreator();
        StringBuffer csb = cc.createCitation(dataResources, messageSource, locale, hostUrl + datasetBaseUrl);
        outputStream.write(csb.toString().getBytes());
        if (outputStream instanceof ZipOutputStream) {
            ((ZipOutputStream) outputStream).closeEntry();
        }
    }

    /**
	 * Outputs a rights to the supplied output stream. If the stream is zipped, it will create an entry of the specified <code>rightsFileName</code>. 
	 * 
	 * @param outputStream
	 * @param resultsOutputter
	 * @param rightsFileName
	 * @param locale
	 * @param hostUrl
	 * @throws IOException
	 */
    public void outputRights(OutputStream outputStream, DataResourceAuditor resultsOutputter, String rightsFileName, Locale locale, String hostUrl) throws IOException {
        if (outputStream instanceof ZipOutputStream) {
            ((ZipOutputStream) outputStream).putNextEntry(new ZipEntry(rightsFileName));
        }
        Map<String, String> dataResources = resultsOutputter.getDataResources();
        StringBuffer sb = new StringBuffer();
        sb.append(messageSource.getMessage("rights.introduction", null, locale));
        try {
            for (String dataResourceId : dataResources.keySet()) {
                DataResourceDTO dataResourceDTO = dataResourceManager.getDataResourceFor(dataResourceId);
                Object[] paramsEntry = new Object[2];
                paramsEntry[0] = dataResourceDTO.getName();
                paramsEntry[1] = hostUrl + datasetBaseUrl + dataResourceDTO.getKey();
                sb.append(messageSource.getMessage("rights.entry", paramsEntry, locale));
                sb.append('\n');
                Object[] paramsRights = new Object[1];
                if (dataResourceDTO.getRights() != null && !dataResourceDTO.getRights().equals("")) {
                    paramsRights[0] = dataResourceDTO.getRights();
                } else {
                    paramsRights[0] = "Not supplied";
                }
                sb.append(messageSource.getMessage("rights.supplied", paramsRights, locale));
                sb.append("\n\n");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        outputStream.write(sb.toString().getBytes());
        if (outputStream instanceof ZipOutputStream) {
            ((ZipOutputStream) outputStream).closeEntry();
        }
    }

    /**
	 * Logs the user event of viewing an occurrence record.
	 * 
	 * @param dataProvider
	 */
    public void logDownloadUsage(DataResourceAuditor resultsOutputter, Integer logEventId) {
        LogGroup logGroup = gbifLogUtils.startLogGroup();
        LogEvent le = LogEvent.get(logEventId);
        Map<String, String> dataResourceMap = resultsOutputter.getDataResources();
        Map<String, Integer> counts = resultsOutputter.getDataResourcesCounts();
        try {
            for (String dataResourceId : dataResourceMap.keySet()) {
                DataResourceDTO dataResourceDTO = dataResourceManager.getDataResourceFor(dataResourceId);
                GbifLogMessage gbifMessage = gbifLogUtils.createGbifLogMessage(logGroup, le);
                gbifMessage.setDataProviderId(parseKey(dataResourceDTO.getDataProviderKey()));
                gbifMessage.setDataResourceId(parseKey(dataResourceDTO.getKey()));
                gbifMessage.setPortalInstanceId(1L);
                gbifMessage.setTimestamp(new Date());
                gbifMessage.setRestricted(false);
                gbifMessage.setCount(counts.get(dataResourceDTO.getKey()));
                gbifMessage.setMessage("Dataset downloaded");
                if (logger.isInfoEnabled()) logger.info(gbifMessage);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            gbifLogUtils.endLogGroup(logGroup);
        }
    }

    /**
	 * Adds secondary outputs to a zip file.
	 * 
	 * @param outputStream
	 * @param secondaryDownloadOutputs
	 * @throws Exception
	 */
    public void addSecondaryOutputs(ZipOutputStream outputStream, List<SecondaryOutput> secondaryDownloadOutputs) throws Exception {
        for (SecondaryOutput sdo : secondaryDownloadOutputs) {
            outputStream.putNextEntry(new ZipEntry(sdo.getFileName()));
            sdo.process(outputStream);
            outputStream.closeEntry();
        }
    }

    /**
	 * Get the host url.
	 * @param request
	 * @return
	 */
    public static String getHostUrl(HttpServletRequest request) {
        StringBuffer hostUrl = new StringBuffer("http://");
        String host = request.getHeader("host");
        hostUrl.append(host);
        hostUrl.append(request.getContextPath());
        return hostUrl.toString();
    }

    /**
	 * Parses the supplied key. Returns null if supplied string invalid
	 * @param key
	 * @return a concept key. Returns null if supplied string invalid key
	 */
    protected static Long parseKey(String key) {
        Long parsedKey = null;
        try {
            parsedKey = Long.parseLong(key);
        } catch (NumberFormatException e) {
        }
        return parsedKey;
    }

    /**
	 * @return the zippedFileExtension
	 */
    public String getZippedFileExtension() {
        return zippedFileExtension;
    }

    /**
	 * @param dataResourceManager the dataResourceManager to set
	 */
    public void setDataResourceManager(DataResourceManager dataResourceManager) {
        this.dataResourceManager = dataResourceManager;
    }

    /**
	 * @param datasetBaseUrl the datasetBaseUrl to set
	 */
    public void setDatasetBaseUrl(String datasetBaseUrl) {
        this.datasetBaseUrl = datasetBaseUrl;
    }

    /**
	 * @param gbifLogUtils the gbifLogUtils to set
	 */
    public void setGbifLogUtils(GbifLogUtils gbifLogUtils) {
        this.gbifLogUtils = gbifLogUtils;
    }

    /**
	 * @param messageSource the messageSource to set
	 */
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
}
