package org.apache.axis2.deployment.resolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.woden.WSDLException;
import org.apache.woden.resolver.URIResolver;
import org.apache.ws.commons.schema.resolver.DefaultURIResolver;
import org.xml.sax.InputSource;
import javax.wsdl.xml.WSDLLocator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Custom WSDL locator to load schemas from zip archives
 * Need to provide the aarFile and the baseInputStream for
 * the base WSDL file
 * <p/>
 * The logic here is that we only care about the import location
 * all imports must be relative to the META-INF folder
 */
public class AARBasedWSDLLocator extends DefaultURIResolver implements WSDLLocator, URIResolver {

    protected static final Log log = LogFactory.getLog(AARBasedWSDLLocator.class);

    private File aarFile;

    private InputStream baseInputStream;

    private URI lastImportLocation;

    private String baseURI;

    public AARBasedWSDLLocator(String baseURI, File zipFile, InputStream baseInputStream) {
        this.baseURI = baseURI;
        this.baseInputStream = baseInputStream;
        this.aarFile = zipFile;
    }

    public InputSource getBaseInputSource() {
        return new InputSource(baseInputStream);
    }

    /**
     * @param parentLocation
     * @param importLocation
     */
    public InputSource getImportInputSource(String parentLocation, String importLocation) {
        lastImportLocation = URI.create(parentLocation).resolve(importLocation);
        if (isAbsolute(importLocation)) {
            return super.resolveEntity(null, importLocation, parentLocation);
        } else {
            ZipInputStream zin = null;
            try {
                zin = new ZipInputStream(new FileInputStream(aarFile));
                ZipEntry entry;
                byte[] buf = new byte[1024];
                int read;
                ByteArrayOutputStream out;
                String searchingStr = lastImportLocation.toString();
                while ((entry = zin.getNextEntry()) != null) {
                    String entryName = entry.getName().toLowerCase();
                    if (entryName.equalsIgnoreCase(searchingStr)) {
                        out = new ByteArrayOutputStream();
                        while ((read = zin.read(buf)) > 0) {
                            out.write(buf, 0, read);
                        }
                        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                        return new InputSource(in);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (zin != null) {
                        zin.close();
                    }
                } catch (IOException e) {
                    log.debug(e);
                }
            }
        }
        log.info("AARBasedWSDLLocator: Unable to resolve " + lastImportLocation);
        return null;
    }

    /**
     * As for the zip there is no point in returning
     * a base URI
     */
    public String getBaseURI() {
        return baseURI;
    }

    /**
     * returns the latest import
     */
    public String getLatestImportURI() {
        return lastImportLocation.toString();
    }

    public void close() {
    }

    public URI resolveURI(URI uri) throws WSDLException, IOException {
        lastImportLocation = URI.create(baseURI).resolve(uri);
        if (isAbsolute(uri.toString())) {
            return uri;
        } else {
            String absolutePath = aarFile.getAbsolutePath();
            try {
                return new URI("jar:file://" + absolutePath + "!/" + lastImportLocation);
            } catch (URISyntaxException e) {
                log.debug(e);
            }
        }
        log.info("AARBasedWSDLLocator: Unable to resolve " + lastImportLocation);
        return null;
    }
}
