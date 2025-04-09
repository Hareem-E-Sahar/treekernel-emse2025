package org.openxml4j.document;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.ContentTypeConstant;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackageAccess;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackageRelationship;
import org.openxml4j.opc.PackageRelationshipConstants;
import org.openxml4j.opc.PackageURIHelper;
import org.openxml4j.opc.PartMarshaller;

/**
 * Repr�sente un document OpenXML en g�n�ral. Cette classe encapsule toutes les
 * m�thodes qui permettent de manipuler un package de mani�re transparente.
 *
 * @author Julien Chable
 * @version 0.2
 */
public class OpenXMLDocument implements ContentTypeConstant {

    private static final String NAMESPACE_DC_URI = "http://purl.org/dc/elements/1.1/";

    private static final String NAMESPACE_CP_URI = "http://schemas.openxmlformats.org/package/2006/metadata/core-properties";

    private static final String NAMESPACE_DCTERMS_URI = "http://purl.org/dc/terms/";

    private static final Namespace namespaceDC = new Namespace("dc", NAMESPACE_DC_URI);

    private static final Namespace namespaceCP = new Namespace("cp", NAMESPACE_CP_URI);

    private static final Namespace namespaceDcTerms = new Namespace("dcterms", NAMESPACE_DCTERMS_URI);

    /**
	 * buffer to read data from file. Use big buffer to improve performaces. the
	 * InputStream class is reading only 8192 bytes per read call (default value
	 * set by sun)
	 */
    public static final int READ_WRITE_FILE_BUFFER_SIZE = 8192;

    protected Package container;

    private static Logger logger = Logger.getLogger("org.openxml4j");

    protected CorePropertiesHelper corePropertiesHelper;

    /**
	 * Constructeur.
	 *
	 * @param docPackage
	 *            R�f�rence vers le package du document.
	 * @throws OpenXML4JException
	 */
    public OpenXMLDocument(Package docPackage) throws OpenXML4JException {
        container = docPackage;
        corePropertiesHelper = new CorePropertiesHelper(container);
        container.addMarshaller(ContentTypeConstant.CORE_PROPERTIES, corePropertiesHelper);
    }

    /**
	 * Extrait toutes les ressources du type sp�cifi� et les place dans le
	 * r�pertoire cible.
	 *
	 * @param contentType
	 *            Le type de contenu.
	 * @param destFolder
	 *            Le r�pertoire cible.
	 * @throws OpenXML4JException
	 */
    public void extractFiles(String contentType, File destFolder) throws OpenXML4JException {
        if (!destFolder.isDirectory()) {
            throw new OpenXML4JException("parameter desFolder should be a directory !");
        }
        ArrayList<PackagePart> parts = new ArrayList<PackagePart>();
        for (PackagePart part : container.getPartByContentType(contentType)) {
            parts.add(part);
        }
        extractParts(parts, destFolder);
    }

    public boolean extractParts(ArrayList<PackagePart> parts, File destFolder) {
        boolean result = true;
        for (PackagePart part : parts) {
            String filename = PackageURIHelper.getFilename(part.getUri());
            try {
                InputStream ins = part.getInputStream();
                FileOutputStream fw = new FileOutputStream(destFolder.getAbsolutePath() + File.separator + filename);
                byte[] buff = new byte[READ_WRITE_FILE_BUFFER_SIZE];
                while (ins.available() > 0) {
                    int resultRead = ins.read(buff);
                    if (resultRead == -1) {
                        break;
                    } else {
                        fw.write(buff, 0, resultRead);
                    }
                }
                fw.close();
            } catch (IOException e) {
                logger.error("cannot generate file " + filename, e);
                result = false;
            }
        }
        return result;
    }

    /**
	 * load in memory a file stored in the zip package
	 *
	 * @param memoryBuffer
	 *            where the file shall be stored
	 *
	 * @param ins
	 *            where the file is in the package
	 *
	 * @return false if problem
	 */
    public static boolean loadInputStreamInMemory(ByteArrayOutputStream memoryBuffer, InputStream ins) {
        boolean result = true;
        try {
            byte[] buff = new byte[READ_WRITE_FILE_BUFFER_SIZE];
            while (ins.available() > 0) {
                int resultRead = ins.read(buff);
                if (resultRead == -1) {
                    break;
                } else {
                    memoryBuffer.write(buff, 0, resultRead);
                }
            }
            memoryBuffer.close();
        } catch (IOException e) {
            logger.error("cannot read file ", e);
            result = false;
        }
        return result;
    }

    public ArrayList<PackagePart> getThumbnails() throws OpenXML4JException {
        return container.getPartByRelationshipType(PackageRelationshipConstants.NS_THUMBNAIL_PART);
    }

    /**
	 * Ouvre un document.
	 *
	 * @param zipFile
	 *            Le fichier Zip du document OpenXML.
	 * @param access
	 *            Le mode d'acc�s au document.
	 * @throws OpenXML4JException
	 */
    public static OpenXMLDocument open(ZipFile zipFile, PackageAccess access) throws OpenXML4JException {
        return new OpenXMLDocument(Package.open(zipFile, access));
    }

    /**
	 * saving a document in an open XML file
	 * @param destFile : the file where we will save
	 * @return false if error
	 * @throws OpenXML4JException
	 */
    public boolean save(File destFile) throws OpenXML4JException {
        return container.save(destFile);
    }

    /**
	 * saving a document in a stream (useful if we wish to get the document in memory not in a file for ex)
	 *
	 * @param outputSource
	 * @return false if error
	 * @throws OpenXML4JException
	 */
    public boolean save(OutputStream outputSource) throws OpenXML4JException {
        return container.save(outputSource);
    }

    public CoreProperties getCoreProperties() {
        return corePropertiesHelper.getCoreProperties();
    }

    /**
	 * @author CDubettier
	 */
    final class CorePropertiesHelper implements PartMarshaller {

        private static final String KEYWORD_CREATED = "created";

        private static final String KEYWORD_LAST_MODIFIED_BY = "lastModifiedBy";

        private static final String KEYWORD_MODIFIED = "modified";

        private static final String KEYWORD_REVISION = "revision";

        private static final String KEYWORD_DESCRIPTION = "description";

        private static final String KEYWORD_KEYWORDS = "keywords";

        private static final String KEYWORD_SUBJECT = "subject";

        private static final String KEYWORD_TITLE = "title";

        private static final String KEYWORD_CREATOR = "creator";

        /**
		 * R�f�rence vers le package.
		 */
        private Package container;

        /**
		 * L'entr�e Zip du fichier de propri�t�s du doccuments.
		 */
        private ZipEntry corePropertiesZipEntry;

        /**
		 * Le bean des propri�t�s du document.
		 */
        private CoreProperties coreProperties;

        /**
		 *  DOM4j document (used for saving)
		 */
        private Document xmlDoc;

        public CorePropertiesHelper(Package container) throws OpenXML4JException {
            this.container = container;
            coreProperties = parseCorePropertiesFile();
        }

        /**
		 * parse properties file of the document.
		 *
		 * @return
		 * @throws OpenXML4JException
		 */
        private CoreProperties parseCorePropertiesFile() throws OpenXML4JException {
            CoreProperties coreProps = new CoreProperties();
            corePropertiesZipEntry = getCorePropertiesZipEntry();
            InputStream inStream = null;
            try {
                inStream = container.getArchive().getInputStream(corePropertiesZipEntry);
            } catch (IOException e) {
                throw new OpenXML4JException("cannot read properties file" + corePropertiesZipEntry.getName());
            }
            if (logger.isDebugEnabled()) {
                logger.debug("reading properties from:" + corePropertiesZipEntry.getName());
            }
            try {
                SAXReader xmlReader = new SAXReader();
                xmlDoc = xmlReader.read(inStream);
                loadCreator(coreProps);
                loadTitle(coreProps);
                loadSubject(coreProps);
                loadKeyword(coreProps);
                loadDescription(coreProps);
                loadLastModificationPerson(coreProps);
                loadRevision(coreProps);
                loadCreated(coreProps);
                loadModified(coreProps);
            } catch (DocumentException e) {
                logger.error(e);
                return null;
            }
            return coreProps;
        }

        private void loadModified(CoreProperties coreProps) {
            Element modified = xmlDoc.getRootElement().element(new QName(KEYWORD_MODIFIED, namespaceDcTerms));
            if (modified != null) {
                coreProps.setModified(modified.getStringValue());
            }
        }

        private void loadCreated(CoreProperties coreProps) {
            Element created = xmlDoc.getRootElement().element(new QName(KEYWORD_CREATED, namespaceDcTerms));
            if (created != null) {
                coreProps.setCreated(created.getStringValue());
            }
        }

        private void loadRevision(CoreProperties coreProps) {
            Element revision = xmlDoc.getRootElement().element(new QName(KEYWORD_REVISION, namespaceCP));
            if (revision != null) {
                coreProps.setRevision(revision.getStringValue());
            }
        }

        private void loadLastModificationPerson(CoreProperties coreProps) {
            Element lastModicationPerson = xmlDoc.getRootElement().element(new QName(KEYWORD_LAST_MODIFIED_BY, namespaceCP));
            if (lastModicationPerson != null) {
                coreProps.setLastModifiedBy(lastModicationPerson.getStringValue());
            }
        }

        private void loadDescription(CoreProperties coreProps) {
            Element description = xmlDoc.getRootElement().element(new QName(KEYWORD_DESCRIPTION, namespaceDC));
            if (description != null) {
                coreProps.setDescription(description.getStringValue());
            }
        }

        private void loadKeyword(CoreProperties coreProps) {
            Element keyword = xmlDoc.getRootElement().element(new QName(KEYWORD_KEYWORDS, namespaceCP));
            if (keyword != null) {
                coreProps.setKeywords(keyword.getStringValue());
            }
        }

        private void loadSubject(CoreProperties coreProps) {
            Element subject = xmlDoc.getRootElement().element(new QName(KEYWORD_SUBJECT, namespaceDC));
            if (subject != null) {
                coreProps.setSubject(subject.getStringValue());
            }
        }

        private void loadTitle(CoreProperties coreProps) {
            Element title = xmlDoc.getRootElement().element(new QName(KEYWORD_TITLE, namespaceDC));
            if (title != null) {
                coreProps.setTitle(title.getStringValue());
            }
        }

        private void loadCreator(CoreProperties coreProps) {
            Element creator = xmlDoc.getRootElement().element(new QName(KEYWORD_CREATOR, namespaceDC));
            if (creator != null) {
                coreProps.setCreator(creator.getStringValue());
            }
        }

        /**
		 * Save document properties, like title, subject ...
		 *
		 * @param out
		 *            where it should be written.
		 * @throws OpenXML4JException
		 */
        public boolean marshall(PackagePart part, OutputStream os) throws OpenXML4JException {
            if (!(os instanceof ZipOutputStream)) {
                logger.error("unexpected class " + os.getClass().getName());
                throw new OpenXML4JException(" ZipOutputStream expected!");
            }
            ZipOutputStream out = (ZipOutputStream) os;
            addCreator();
            addTitle();
            addSubject();
            addModified();
            addCreated();
            addRevision();
            addLastModifiedBy();
            addDescription();
            addKeywords();
            ZipEntry ctEntry = new ZipEntry(corePropertiesZipEntry.getName());
            try {
                out.putNextEntry(ctEntry);
                if (!Package.saveAsXmlInZip(xmlDoc, corePropertiesZipEntry.getName(), out)) {
                    return false;
                }
                out.closeEntry();
            } catch (IOException e1) {
                logger.error(e1);
                return false;
            }
            return true;
        }

        private void addModified() {
            Element elem = xmlDoc.getRootElement().element(new QName(KEYWORD_MODIFIED, namespaceDcTerms));
            if (elem == null) {
                elem = xmlDoc.getRootElement().addElement(new QName(KEYWORD_MODIFIED, namespaceDcTerms));
            } else {
                elem.clearContent();
            }
            elem.addText(coreProperties.getModified());
        }

        private void addCreated() {
            Element elem = xmlDoc.getRootElement().element(new QName(KEYWORD_CREATED, namespaceDcTerms));
            if (elem == null) {
                elem = xmlDoc.getRootElement().addElement(new QName(KEYWORD_CREATED, namespaceDcTerms));
            } else {
                elem.clearContent();
            }
            elem.addText(coreProperties.getCreated());
        }

        private void addRevision() {
            Element elem = xmlDoc.getRootElement().element(new QName(KEYWORD_REVISION, namespaceCP));
            if (elem == null) {
                elem = xmlDoc.getRootElement().addElement(new QName(KEYWORD_REVISION, namespaceCP));
            } else {
                elem.clearContent();
            }
            elem.addText(coreProperties.getRevision());
        }

        private void addLastModifiedBy() {
            Element elem = xmlDoc.getRootElement().element(new QName(KEYWORD_LAST_MODIFIED_BY, namespaceCP));
            if (elem == null) {
                elem = xmlDoc.getRootElement().addElement(new QName(KEYWORD_LAST_MODIFIED_BY, namespaceCP));
            } else {
                elem.clearContent();
            }
            elem.addText(coreProperties.getLastModifiedBy());
        }

        private void addDescription() {
            Element elem = xmlDoc.getRootElement().element(new QName(KEYWORD_DESCRIPTION, namespaceDC));
            if (elem == null) {
                elem = xmlDoc.getRootElement().addElement(new QName(KEYWORD_DESCRIPTION, namespaceDC));
            } else {
                elem.clearContent();
            }
            elem.addText(coreProperties.getDescription());
        }

        private void addKeywords() {
            Element elem = xmlDoc.getRootElement().element(new QName(KEYWORD_KEYWORDS, namespaceCP));
            if (elem == null) {
                elem = xmlDoc.getRootElement().addElement(new QName(KEYWORD_KEYWORDS, namespaceCP));
            } else {
                elem.clearContent();
            }
            elem.addText(coreProperties.getKeywords());
        }

        private void addSubject() {
            Element elem = xmlDoc.getRootElement().element(new QName(KEYWORD_SUBJECT, namespaceDC));
            if (elem == null) {
                elem = xmlDoc.getRootElement().addElement(new QName(KEYWORD_SUBJECT, namespaceDC));
            } else {
                elem.clearContent();
            }
            elem.addText(coreProperties.getSubject());
        }

        private void addTitle() {
            Element elem = xmlDoc.getRootElement().element(new QName(KEYWORD_TITLE, namespaceDC));
            if (elem == null) {
                elem = xmlDoc.getRootElement().addElement(new QName(KEYWORD_TITLE, namespaceDC));
            } else {
                elem.clearContent();
            }
            elem.addText(coreProperties.getTitle());
        }

        private void addCreator() {
            Element elem = xmlDoc.getRootElement().element(new QName(KEYWORD_CREATOR, namespaceDC));
            if (elem == null) {
                elem = xmlDoc.getRootElement().addElement(new QName(KEYWORD_CREATOR, namespaceDC));
            } else {
                elem.clearContent();
            }
            elem.addText(coreProperties.getCreator());
        }

        /**
		 * @return the container
		 * @uml.property name="container"
		 */
        public Package getContainer() {
            return container;
        }

        /**
		 * @return the coreProperties
		 * @uml.property name="coreProperties"
		 */
        public CoreProperties getCoreProperties() {
            return coreProperties;
        }

        /**
		 * R�cup�rer l'entr�e Zip du fichier de propri�t� du document.
		 *
		 * @throws OpenXML4JException
		 * @uml.property name="corePropertiesZipEntry"
		 */
        private ZipEntry getCorePropertiesZipEntry() throws OpenXML4JException {
            PackageRelationship corePropsRel = container.getRelationshipsByType(PackageRelationshipConstants.NS_CORE_PROPERTIES).getRelationship(0);
            if (corePropsRel == null) {
                return null;
            }
            return new ZipEntry(corePropsRel.getTargetUri().getPath());
        }
    }
}
