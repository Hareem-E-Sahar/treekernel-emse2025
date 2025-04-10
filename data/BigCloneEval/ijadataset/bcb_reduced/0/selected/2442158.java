package com.webmotix.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.webmotix.core.SystemProperty;

/**
 * Util used to process config files
 * @author Philipp Bracher
 * @version $Id: ConfigUtil.java 6341 2006-09-12 09:18:27Z philipp $
 *
 */
public abstract class ConfigUtil {

    /**
     * EntityResolver using a Map to resources
     * @author Philipp Bracher
     * @version $Id: ConfigUtil.java 6341 2006-09-12 09:18:27Z philipp $
     *
     */
    public static final class MapDTDEntityResolver implements EntityResolver {

        private final Map dtds;

        public MapDTDEntityResolver(final Map dtds) {
            this.dtds = dtds;
        }

        public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
            final String key = StringUtils.substringAfterLast(systemId, "/");
            if (dtds.containsKey(key)) {
                final Class clazz = getClass();
                final InputStream in = clazz.getResourceAsStream((String) dtds.get(key));
                if (in == null) {
                    log.error("Could not find [" + systemId + "]. Used [" + clazz.getClassLoader() + "] class loader in the search.");
                    return null;
                } else {
                    return new InputSource(in);
                }
            } else {
                return null;
            }
        }
    }

    /**
     * Log
     */
    public static Logger log = LoggerFactory.getLogger(ConfigUtil.class);

    /**
     * Try to get the file. Get it first in the file system, then from the resources
     * @param fileName
     * @return the input stream
     */
    public static InputStream getConfigFile(final String fileName) {
        final File log4jFile = new File(SystemProperty.getProperty(SystemProperty.WEBMOTIX_APP_ROOTDIR), fileName);
        InputStream stream;
        if (log4jFile.exists()) {
            URL url;
            try {
                url = new URL("file:" + log4jFile.getAbsolutePath());
            } catch (final MalformedURLException e) {
                log.error("Unable to read config file from [" + fileName + "], got a MalformedURLException: ", e);
                return null;
            }
            try {
                stream = url.openStream();
            } catch (final IOException e) {
                log.error("Unable to read config file from [" + fileName + "], got a IOException ", e);
                return null;
            }
        } else {
            try {
                stream = new FileInputStream(fileName);
            } catch (final FileNotFoundException e) {
                log.error("Unable to read config file from [" + fileName + "], got a FileNotFoundException ", e);
                return null;
            }
        }
        return stream;
    }

    /**
     * Read the stream and replace tokens
     * @param stream
     * @return the string with replaced tokens
     * @throws IOException
     */
    public static String replaceTokens(final InputStream stream) throws IOException {
        String config;
        config = IOUtils.toString(stream);
        IOUtils.closeQuietly(stream);
        return replaceTokens(config);
    }

    /**
     * Replace tokens in a string
     * @param config
     * @return
     * @throws IOException
     */
    public static String replaceTokens(String config) throws IOException {
        for (final Iterator iter = SystemProperty.getPropertyList().keySet().iterator(); iter.hasNext(); ) {
            final String key = (String) iter.next();
            config = StringUtils.replace(config, "${" + key + "}", SystemProperty.getProperty(key, ""));
        }
        return config;
    }

    /**
     * Convert the string to an DOM Document
     * @param xml
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Document string2DOM(final String xml) throws ParserConfigurationException, SAXException, IOException {
        return string2DOM(xml, Collections.EMPTY_MAP);
    }

    /**
     * Convert the string to a JDOM Document
     * @param xml
     * @return
     * @throws JDOMException
     * @throws IOException
     */
    public static org.jdom.Document string2JDOM(final String xml) throws JDOMException, IOException {
        return string2JDOM(xml, Collections.EMPTY_MAP);
    }

    /**
     * Uses a map to find the dtds in the resources
     * @param xml
     * @param dtds
     * @return
     * @throws JDOMException
     * @throws IOException
     */
    public static org.jdom.Document string2JDOM(final String xml, final Map dtds) throws JDOMException, IOException {
        final SAXBuilder builder = new SAXBuilder();
        builder.setEntityResolver(new MapDTDEntityResolver(dtds));
        return builder.build(IOUtils.toInputStream(xml));
    }

    /**
     * Uses a map to find dtds in the resources
     * @param xml
     * @param dtds
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static Document string2DOM(final String xml, final Map dtds) throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(true);
        DocumentBuilder builder;
        builder = dbf.newDocumentBuilder();
        builder.setEntityResolver(new MapDTDEntityResolver(dtds));
        return builder.parse(IOUtils.toInputStream(xml));
    }
}
