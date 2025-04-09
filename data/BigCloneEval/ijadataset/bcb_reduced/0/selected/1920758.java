package org.openxml4j.opc.internal.marshallers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.PackageNamespaces;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackagePartName;
import org.openxml4j.opc.PackageRelationship;
import org.openxml4j.opc.PackageRelationshipCollection;
import org.openxml4j.opc.PackagingURIHelper;
import org.openxml4j.opc.StreamHelper;
import org.openxml4j.opc.TargetMode;
import org.openxml4j.opc.internal.PartMarshaller;
import org.openxml4j.opc.internal.ZipHelper;

/**
 * Zip part marshaller. This marshaller is use to save any part in a zip stream.
 * 
 * @author Julien Chable
 * @version 0.1
 */
public class ZipPartMarshaller implements PartMarshaller {

    private static Logger logger = Logger.getLogger("org.openxml4j");

    /**
	 * Save the specified part.
	 * 
	 * @throws OpenXML4JException
	 *             Throws if an internal exception is thrown.
	 */
    public boolean marshall(PackagePart part, OutputStream os) throws OpenXML4JException {
        if (!(os instanceof ZipOutputStream)) {
            logger.error("Unexpected class " + os.getClass().getName());
            throw new OpenXML4JException("ZipOutputStream expected !");
        }
        ZipOutputStream zos = (ZipOutputStream) os;
        ZipEntry partEntry = new ZipEntry(ZipHelper.getZipItemNameFromOPCName(part.getPartName().getURI().getPath()));
        try {
            zos.putNextEntry(partEntry);
            InputStream ins = part.getInputStream();
            byte[] buff = new byte[ZipHelper.READ_WRITE_FILE_BUFFER_SIZE];
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
            logger.error("Cannot write: " + part.getPartName() + ": in ZIP", ioe);
            return false;
        }
        if (part.hasRelationships()) {
            PackagePartName relationshipPartName = PackagingURIHelper.getRelationshipPartName(part.getPartName());
            marshallRelationshipPart(part.getRelationships(), relationshipPartName, zos);
        }
        return true;
    }

    /**
	 * Save relationships into the part.
	 * 
	 * @param rels
	 *            The relationships collection to marshall.
	 * @param relPartURI
	 *            Part name of the relationship part to marshall.
	 * @param zos
	 *            Zip output stream in which to save the XML content of the
	 *            relationships serialization.
	 */
    public static boolean marshallRelationshipPart(PackageRelationshipCollection rels, PackagePartName relPartName, ZipOutputStream zos) {
        Document xmlOutDoc = DocumentHelper.createDocument();
        Namespace dfNs = Namespace.get("", PackageNamespaces.RELATIONSHIPS);
        Element root = xmlOutDoc.addElement(new QName(PackageRelationship.RELATIONSHIPS_TAG_NAME, dfNs));
        URI sourcePartURI = PackagingURIHelper.getSourcePartUriFromRelationshipPartUri(relPartName.getURI());
        for (PackageRelationship rel : rels) {
            Element relElem = root.addElement(PackageRelationship.RELATIONSHIP_TAG_NAME);
            relElem.addAttribute(PackageRelationship.ID_ATTRIBUTE_NAME, rel.getId());
            relElem.addAttribute(PackageRelationship.TYPE_ATTRIBUTE_NAME, rel.getRelationshipType());
            String targetValue;
            URI uri = rel.getTargetURI();
            if (rel.getTargetMode() == TargetMode.EXTERNAL) {
                try {
                    targetValue = URLEncoder.encode(uri.toString(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    targetValue = uri.toString();
                }
                relElem.addAttribute(PackageRelationship.TARGET_MODE_ATTRIBUTE_NAME, "External");
            } else {
                targetValue = PackagingURIHelper.relativizeURI(sourcePartURI, rel.getTargetURI()).getPath();
            }
            relElem.addAttribute(PackageRelationship.TARGET_ATTRIBUTE_NAME, targetValue);
        }
        xmlOutDoc.normalize();
        ZipEntry ctEntry = new ZipEntry(ZipHelper.getZipURIFromOPCName(relPartName.getURI().toASCIIString()).getPath());
        try {
            zos.putNextEntry(ctEntry);
            if (!StreamHelper.saveXmlInStream(xmlOutDoc, zos)) {
                return false;
            }
            zos.closeEntry();
        } catch (IOException e) {
            logger.error("Cannot create zip entry " + relPartName, e);
            return false;
        }
        return true;
    }
}
