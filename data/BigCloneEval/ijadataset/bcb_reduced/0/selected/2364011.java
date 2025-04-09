package org.openxml4j.document.word;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackageURIHelper;
import org.openxml4j.opc.PartMarshaller;
import org.openxml4j.opc.ZipPartMarshaller;

public class DocumentManager implements PartMarshaller {

    protected static Logger logger = Logger.getLogger("org.openxml4j");

    /**
	 * the XML data from DOM
	 */
    protected Document xmlContent = null;

    public DocumentManager() {
        super();
    }

    /**
	 * load the file (like style.xml) used by word doc
	 * @param container : where the XML files are
	 * @param targetUri : URI of style.xml
	 * @throws OpenXML4JException
	 *
	 */
    protected void loadContentsFromPackage(Package container, URI targetUri) throws OpenXML4JException {
        try {
            PackagePart sytlePart = container.getPart(targetUri);
            if (sytlePart == null) {
                logger.error("style not found-> aborting");
                xmlContent = null;
                return;
            }
            InputStream inStream = sytlePart.getInputStream();
            SAXReader reader = new SAXReader();
            xmlContent = reader.read(inStream);
        } catch (IOException e) {
            logger.error("I/O problem for style", e);
            xmlContent = null;
            throw new OpenXML4JException(e.getMessage());
        } catch (DocumentException e) {
            logger.error("problem for style", e);
            xmlContent = null;
            throw new OpenXML4JException(e.getMessage());
        }
    }

    /**
	 * Save the XML in the Zip file.
	 * @throws OpenXML4JException
	 */
    public boolean marshall(PackagePart part, OutputStream os) throws OpenXML4JException {
        if (!(os instanceof ZipOutputStream)) {
            logger.error("unexpected class " + os.getClass().getName());
            throw new OpenXML4JException(" ZipOutputStream expected!");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("saving CHANGED xml file");
        }
        ZipOutputStream out = (ZipOutputStream) os;
        ZipEntry ctEntry = new ZipEntry(part.getUri().getPath());
        try {
            out.putNextEntry(ctEntry);
            if (!Package.saveAsXmlInZip(xmlContent, part.getUri().getPath(), out)) {
                return false;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("recording style relationship (should be none)");
            }
            if (part.hasRelationships()) {
                ZipPartMarshaller partMarshaller = new ZipPartMarshaller();
                partMarshaller.marshallRelationshipPart(part.getRelationships(), PackageURIHelper.getRelationshipPartUri(part.getUri()), out);
            }
            out.closeEntry();
        } catch (IOException e1) {
            logger.error("IO problem with " + part.getUri(), e1);
            return false;
        }
        return true;
    }
}
