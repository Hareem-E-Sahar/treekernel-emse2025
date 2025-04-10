package org.opennms.protocols.xml.collector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.CollectionException;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.dao.support.ResourceTypeUtils;
import org.opennms.protocols.sftp.Sftp3gppUrlConnection;
import org.opennms.protocols.sftp.Sftp3gppUrlHandler;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlObject;
import org.opennms.protocols.xml.config.XmlSource;
import org.w3c.dom.Document;

/**
 * The custom implementation of the interface XmlCollectionHandler for 3GPP XML Data.
 * <p>This supports the processing of several files ordered by filename, and the
 * timestamp between files won't be taken in consideration.</p>
 * <p>The state will be persisted on disk by saving the name of the last successfully
 * processed file.</p>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class Sftp3gppXmlCollectionHandler extends AbstractXmlCollectionHandler {

    /** The Constant XML_LAST_FILENAME. */
    public static final String XML_LAST_FILENAME = "_xmlCollectorLastFilename";

    /** The 3GPP Performance Metric Instance Formats. */
    private Properties m_pmGroups;

    @Override
    public XmlCollectionSet collect(CollectionAgent agent, XmlDataCollection collection, Map<String, Object> parameters) throws CollectionException {
        XmlCollectionSet collectionSet = new XmlCollectionSet(agent);
        collectionSet.setCollectionTimestamp(new Date());
        collectionSet.setStatus(ServiceCollector.COLLECTION_UNKNOWN);
        try {
            File resourceDir = new File(getRrdRepository().getRrdBaseDir(), Integer.toString(agent.getNodeId()));
            for (XmlSource source : collection.getXmlSources()) {
                if (!source.getUrl().startsWith(Sftp3gppUrlHandler.PROTOCOL)) {
                    throw new CollectionException("The 3GPP SFTP Collection Handler can only use the protocol " + Sftp3gppUrlHandler.PROTOCOL);
                }
                String urlStr = parseUrl(source.getUrl(), agent, collection.getXmlRrd().getStep());
                URL url = UrlFactory.getUrl(urlStr);
                String lastFile = getLastFilename(resourceDir, url.getPath());
                Sftp3gppUrlConnection connection = (Sftp3gppUrlConnection) url.openConnection();
                if (lastFile == null) {
                    lastFile = connection.get3gppFileName();
                    log().debug("collect(single): retrieving file from " + url.getPath() + File.separatorChar + lastFile + " from " + agent.getHostAddress());
                    Document doc = getXmlDocument(urlStr);
                    fillCollectionSet(agent, collectionSet, source, doc);
                    setLastFilename(resourceDir, url.getPath(), lastFile);
                    deleteFile(connection, lastFile);
                } else {
                    connection.connect();
                    List<String> files = connection.getFileList();
                    long lastTs = connection.getTimeStampFromFile(lastFile);
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    factory.setIgnoringComments(true);
                    boolean collected = false;
                    for (String fileName : files) {
                        if (connection.getTimeStampFromFile(fileName) > lastTs) {
                            log().debug("collect(multiple): retrieving file " + fileName + " from " + agent.getHostAddress());
                            InputStream is = connection.getFile(fileName);
                            Document doc = builder.parse(is);
                            fillCollectionSet(agent, collectionSet, source, doc);
                            setLastFilename(resourceDir, url.getPath(), fileName);
                            deleteFile(connection, fileName);
                            collected = true;
                        }
                    }
                    if (!collected) {
                        log().warn("collect: could not find any file after " + lastFile + " on " + agent);
                    }
                    connection.disconnect();
                }
            }
            collectionSet.setStatus(ServiceCollector.COLLECTION_SUCCEEDED);
            return collectionSet;
        } catch (Exception e) {
            collectionSet.setStatus(ServiceCollector.COLLECTION_FAILED);
            throw new CollectionException(e.getMessage(), e);
        }
    }

    /**
     * Gets the last filename.
     *
     * @param resourceDir the resource directory
     * @param targetPath the target path
     * @return the last filename
     * @throws Exception the exception
     */
    private String getLastFilename(File resourceDir, String targetPath) throws Exception {
        String filename = null;
        try {
            filename = ResourceTypeUtils.getStringProperty(resourceDir, getCacheId(targetPath));
        } catch (Exception e) {
            log().info("getLastFilename: creating a new filename tracker on " + resourceDir);
        }
        return filename;
    }

    /**
     * Sets the last filename.
     *
     * @param resourceDir the resource directory
     * @param targetPath the target path
     * @param filename the filename
     * @throws Exception the exception
     */
    private void setLastFilename(File resourceDir, String targetPath, String filename) throws Exception {
        ResourceTypeUtils.updateStringProperty(resourceDir, filename, getCacheId(targetPath));
    }

    /**
     * Gets the cache id.
     *
     * @param targetPath the target path
     * @return the cache id
     */
    private String getCacheId(String targetPath) {
        return XML_LAST_FILENAME + '.' + getServiceName() + targetPath.replaceAll("/", "_");
    }

    /**
     * Safely delete file on remote node.
     *
     * @param connection the SFTP URL Connection
     * @param fileName the file name
     */
    private void deleteFile(Sftp3gppUrlConnection connection, String fileName) {
        try {
            connection.deleteFile(fileName);
        } catch (Exception e) {
            log().warn("Can't delete file " + fileName + " from " + connection.getURL().getHost() + " because " + e.getMessage());
        }
    }

    @Override
    protected void processXmlResource(XmlCollectionResource resource, AttributeGroupType attribGroupType) {
        Map<String, String> properties = get3gppProperties(get3gppFormat(resource.getResourceTypeName()), resource.getInstance());
        for (Entry<String, String> entry : properties.entrySet()) {
            XmlCollectionAttributeType attribType = new XmlCollectionAttributeType(new XmlObject(entry.getKey(), "string"), attribGroupType);
            resource.setAttributeValue(attribType, entry.getValue());
        }
    }

    /**
     * Parses the URL.
     *
     * @param unformattedUrl the unformatted URL
     * @param agent the agent
     * @param collectionStep the collection step (in seconds)
     * @param currentTimestamp the current timestamp
     * @return the string
     */
    protected String parseUrl(String unformattedUrl, CollectionAgent agent, Integer collectionStep, long currentTimestamp) throws IllegalArgumentException {
        if (!unformattedUrl.startsWith(Sftp3gppUrlHandler.PROTOCOL)) {
            throw new IllegalArgumentException("The 3GPP SFTP Collection Handler can only use the protocol " + Sftp3gppUrlHandler.PROTOCOL);
        }
        String baseUrl = parseUrl(unformattedUrl, agent, collectionStep);
        return baseUrl + "&referenceTimestamp=" + currentTimestamp;
    }

    /**
     * Gets the 3GPP resource format.
     *
     * @param resourceType the resource type
     * @return the 3gpp format
     */
    public String get3gppFormat(String resourceType) {
        if (m_pmGroups == null) {
            m_pmGroups = new Properties();
            try {
                m_pmGroups.load(getClass().getResourceAsStream("/3gpp-pmgroups.properties"));
            } catch (IOException e) {
                log().warn("Can't load 3GPP PM Groups formats because " + e.getMessage());
            }
        }
        return m_pmGroups.getProperty(resourceType);
    }

    /**
     * Gets the 3GPP properties based on measInfoId.
     *
     * @param format the format
     * @param measInfoId the measInfoId (the resource instance)
     * @return the properties
     */
    public Map<String, String> get3gppProperties(String format, String measInfoId) {
        Map<String, String> properties = new LinkedHashMap<String, String>();
        if (format != null) {
            String[] groups = format.split("\\|");
            for (String group : groups) {
                String[] subgroups = group.split("/");
                for (String subgroup : subgroups) {
                    String pair[] = subgroup.split("=");
                    if (pair.length > 1) {
                        if (pair[1].matches("^[<].+[>]$")) {
                            String valueRegex = pair[1].equals("<directory path>") ? "=([^|]+)" : "=([^|/]+)";
                            Matcher m = Pattern.compile(pair[0] + valueRegex).matcher(measInfoId);
                            if (m.find()) {
                                String v = pair[1].equals("<directory path>") ? m.group(1).replaceAll("\\\\/", "/") : m.group(1);
                                properties.put(pair[0], v);
                            }
                        }
                    }
                }
            }
        }
        properties.put("label", properties.toString().replaceAll("[{}]", ""));
        properties.put("instance", measInfoId);
        return properties;
    }
}
