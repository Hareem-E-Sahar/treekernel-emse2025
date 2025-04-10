package org.apache.axis2.deployment.resolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.resolver.DefaultURIResolver;
import org.xml.sax.InputSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A custom URI resolver that can
 */
public class AARFileBasedURIResolver extends DefaultURIResolver {

    protected static final Log log = LogFactory.getLog(AARFileBasedURIResolver.class);

    private File aarFile;

    private URI lastImportLocation;

    public AARFileBasedURIResolver(File aarFile) {
        this.aarFile = aarFile;
    }

    public InputSource resolveEntity(String targetNamespace, String schemaLocation, String baseUri) {
        if (isAbsolute(schemaLocation)) {
            return super.resolveEntity(targetNamespace, schemaLocation, baseUri);
        } else {
            if ((baseUri == null || "".equals(baseUri)) && schemaLocation.startsWith("..")) {
                throw new RuntimeException("Unsupported schema location " + schemaLocation);
            }
            lastImportLocation = URI.create(baseUri).resolve(schemaLocation);
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
                        InputSource inputSoruce = new InputSource(in);
                        inputSoruce.setSystemId(lastImportLocation.getPath());
                        inputSoruce.setPublicId(targetNamespace);
                        return inputSoruce;
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
        log.info("AARFileBasedURIResolver: Unable to resolve" + lastImportLocation);
        return null;
    }
}
