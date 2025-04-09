package org.openxml4j.opc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.openxml4j.document.OpenXMLDocument;
import org.openxml4j.exceptions.OpenXML4JException;

/**
 * Classe permettant d'enregistrement des parties dans un flux Zip
 *
 * @author Julien Chable
 */
public class ZipPartMarshaller implements PartMarshaller {

    private static Logger logger = Logger.getLogger("org.openxml4j");

    /**
	 * save the specified part.
	 *
	 * @throws OpenXML4JException
	 */
    public boolean marshall(PackagePart part, OutputStream os) throws OpenXML4JException {
        if (!(os instanceof ZipOutputStream)) {
            logger.error("unexpected class " + os.getClass().getName());
            throw new OpenXML4JException("ZipOutputStream expected !");
        }
        ZipOutputStream zos = (ZipOutputStream) os;
        ZipEntry partEntry = new ZipEntry(part.uri.getPath());
        try {
            zos.putNextEntry(partEntry);
            InputStream ins = part.getInputStream();
            byte[] buff = new byte[OpenXMLDocument.READ_WRITE_FILE_BUFFER_SIZE];
            while (ins.available() > 0) {
                int resultRead = ins.read(buff);
                if (resultRead == -1) {
                    break;
                } else {
                    zos.write(buff, 0, resultRead);
                }
            }
            zos.closeEntry();
        } catch (IOException ioe) {
            logger.error("cannot write:" + part.uri + ": in ZIP", ioe);
            return false;
        }
        if (part.hasRelationships()) {
            marshallRelationshipPart(part.getRelationships(), PackageURIHelper.getRelationshipPartUri(part.getUri()), zos);
        }
        return true;
    }

    /**
	 * Enregistrement des relations de la partie.
	 */
    public boolean marshallRelationshipPart(PackageRelationshipCollection rels, URI relPartURI, ZipOutputStream zos) {
        if (logger.isDebugEnabled()) {
            logger.debug("writing relation:" + relPartURI);
        }
        Document xmlOutDoc = DocumentFactory.getInstance().createDocument();
        Namespace dfNs = Namespace.get("", PackageRelationship.RELATIONSHIPS_NAMESPACE);
        Element root = xmlOutDoc.addElement(new QName(PackageRelationship.RELATIONSHIPS_TAG_NAME, dfNs));
        for (PackageRelationship rel : rels) {
            Element relElem = root.addElement(PackageRelationship.RELATIONSHIP_TAG_NAME);
            relElem.addAttribute(PackageRelationship.ID_ATTRIBUTE_NAME, rel.getId());
            relElem.addAttribute(PackageRelationship.TYPE_ATTRIBUTE_NAME, rel.getRelationshipType());
            String targetValue;
            URI uri = rel.getTargetUri();
            if (rel.getTargetMode() == TargetMode.EXTERNAL) {
                targetValue = uri.getScheme() + "://" + uri.getPath();
                relElem.addAttribute(PackageRelationship.TARGET_MODE_ATTRIBUTE_NAME, "External");
            } else {
                targetValue = uri.getPath();
            }
            relElem.addAttribute(PackageRelationship.TARGET_ATTRIBUTE_NAME, targetValue);
        }
        xmlOutDoc.normalize();
        ZipEntry ctEntry = new ZipEntry(relPartURI.getPath());
        try {
            zos.putNextEntry(ctEntry);
            if (!Package.saveAsXmlInZip(xmlOutDoc, relPartURI.getPath(), zos)) {
                return false;
            }
            zos.closeEntry();
        } catch (IOException e1) {
            logger.error("cannot create file " + relPartURI, e1);
            return false;
        }
        return true;
    }
}
