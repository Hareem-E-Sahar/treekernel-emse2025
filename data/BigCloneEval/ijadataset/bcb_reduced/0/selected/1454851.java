package org.openxml4j.document.word.headerAndFooter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.openxml4j.document.word.WordprocessingML;
import org.openxml4j.document.word.variable.Variable;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.ContentTypeConstant;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackageRelationship;
import org.openxml4j.opc.PackageRelationshipCollection;
import org.openxml4j.opc.PackageURIHelper;
import org.openxml4j.opc.PartMarshaller;
import org.openxml4j.opc.ZipPartMarshaller;

abstract class FooterAndHeaderBase implements PartMarshaller {

    private static Logger logger = Logger.getLogger("org.openxml4j");

    private HeaderFooterType type;

    /**
	 * the id in the word document (document.xml)
	 */
    private String id;

    /**
	 * where the open xml header and footer code should be (loaded from a file. allow to use word to generate footer/header)
	 *
	 */
    private Document openXmlSource = null;

    /**
	 * we shall provide the .rels (ex word\_rels\footer1.rels) for the images in footer/header
	 */
    private PredefinedRelationshipStorage relationships = null;

    /**
	 * when we analyse a existing package, it may have some relationships. Store them and save them (no handling is done for the moment)
	 * TODO merge usage with PredefinedRelationshipStorage relationships in order to allow their edition
	 */
    private PackageRelationshipCollection legacyRelations = null;

    /**
	 * @param type tell if header/footer shall be displayed by on first page, odd, even
	 */
    public FooterAndHeaderBase(HeaderFooterType type) {
        this.type = type;
    }

    public FooterAndHeaderBase() {
        super();
        type = HeaderFooterType.DEFAULT;
    }

    public FooterAndHeaderBase(HeaderFooterType type, PackageRelationship rel, Package container) throws OpenXML4JException {
        this.type = type;
        setId(rel.getId());
        URI relationUri = rel.getTargetUri();
        String newUriWithPath = "word/" + relationUri.toString();
        try {
            URI uri = new URI(newUriWithPath);
            PackagePart part = container.getPart(uri);
            InputStream inStream = part.getInputStream();
            SAXReader xmlReader = new SAXReader();
            openXmlSource = xmlReader.read(inStream);
            container.addMarshaller(part, this);
            uri = PackageRelationshipCollection.getRelationshipPartUri(part);
            if (container.partExists(uri)) {
                legacyRelations = part.getRelationships();
            }
        } catch (IOException e) {
            final String msg = "error in opening footer ! " + e.getMessage();
            openXmlSource = null;
            logger.error(msg);
            throw new OpenXML4JException(msg);
        } catch (DocumentException e) {
            final String msg = "error in parsing footer ! " + e.getMessage();
            logger.error(msg);
            openXmlSource = null;
            throw new OpenXML4JException(msg);
        } catch (URISyntaxException e) {
            final String msg = "invalid uri ! " + e.getMessage();
            logger.error(msg);
            openXmlSource = null;
            throw new OpenXML4JException(msg);
        }
    }

    /**
	 * load a file that will be use to describe a header/footer
	 * @param filename
	 * @throws OpenXML4JException
	 */
    public void loadOpenXmlSource(String filename) throws OpenXML4JException {
        InputStream inStream;
        try {
            inStream = new FileInputStream(filename);
            SAXReader xmlReader = new SAXReader();
            openXmlSource = xmlReader.read(inStream);
        } catch (IOException e) {
            openXmlSource = null;
            logger.error("cannot read " + filename, e);
            throw new OpenXML4JException("cannot read " + filename + " " + e.getMessage());
        } catch (DocumentException e) {
            openXmlSource = null;
            logger.error("error in  reading " + filename, e);
            throw new OpenXML4JException("error in  reading " + filename);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HeaderFooterType getType() {
        return type;
    }

    public void setType(HeaderFooterType type) {
        this.type = type;
    }

    protected void addAttribute(Element footerOrHeader) {
        footerOrHeader.addAttribute(new QName(WordprocessingML.ATTRIBUTE_TYPE, WordprocessingML.namespaceRelationship), getType().toString());
        footerOrHeader.addAttribute(new QName(WordprocessingML.ATTRIBUTE_ID_TAG_NAME, WordprocessingML.namespaceRelationship), getId());
    }

    public PredefinedRelationshipStorage getRelationships() {
        return relationships;
    }

    public void setRelationships(PredefinedRelationshipStorage relationships) {
        this.relationships = relationships;
    }

    /**
	 * add footer/herader in a word doc
	 * @param container
	 * @return
	 * @throws OpenXML4JException
	 */
    public void integrateInDocument(Package container) throws OpenXML4JException {
        container.addOverrideContentType(getUri(true, true), getContentType());
        container.addPart(getUri(true, false), getContentType());
        container.addMarshaller(getContentType(), this);
        try {
            container.addContentType(new URI(ContentTypeConstant.EXTENSION_WMF));
        } catch (URISyntaxException e) {
            logger.error("invalid uri ", e);
            throw new OpenXML4JException(e.getMessage());
        }
        if (relationships != null) {
            relationships.saveImagesInContainer(container);
        }
    }

    /**
	 * @return uri of the footer/header
	 * @throws OpenXML4JException
	 */
    protected abstract URI getUri(boolean withPath, boolean startWithSlash) throws OpenXML4JException;

    protected abstract String getContentType();

    /**
	 * Save the XML in the Zip file.
	 * @throws OpenXML4JException
	 */
    public boolean marshall(PackagePart part, OutputStream os) throws OpenXML4JException {
        if (!(os instanceof ZipOutputStream)) {
            logger.error("unexpected class " + os.getClass().getName());
            throw new OpenXML4JException(" ZipOutputStream expected!");
        }
        final String fileName = part.getUri().getPath();
        if (logger.isDebugEnabled()) {
            logger.debug("saving footer/header xml =" + fileName);
        }
        ZipOutputStream out = (ZipOutputStream) os;
        ZipEntry ctEntry = new ZipEntry(fileName);
        try {
            out.putNextEntry(ctEntry);
            if (!Package.saveAsXmlInZip(openXmlSource, fileName, out)) {
                return false;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("recording  relationship of " + part.getUri());
            }
            if (relationships != null) {
                ZipPartMarshaller partMarshaller = new ZipPartMarshaller();
                PackageRelationshipCollection packageRelationships = relationships.buildPackageRelationships(part);
                partMarshaller.marshallRelationshipPart(packageRelationships, PackageURIHelper.getRelationshipPartUri(part.getUri()), out);
            }
            if (legacyRelations != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("saving header/footer legacy relationship");
                }
                ZipPartMarshaller partMarshaller = new ZipPartMarshaller();
                partMarshaller.marshallRelationshipPart(legacyRelations, PackageURIHelper.getRelationshipPartUri(part.getUri()), out);
            }
            out.closeEntry();
        } catch (IOException e1) {
            logger.error("IO problem with " + part.getUri(), e1);
            return false;
        }
        return true;
    }

    public void replaceVariableInDoc(Variable variables) {
        variables.replaceVariableInDoc(openXmlSource);
    }
}
