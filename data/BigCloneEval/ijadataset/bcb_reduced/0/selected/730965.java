package org.vramework.msoffice;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.vramework.commons.config.VConf;
import org.vramework.commons.exceptions.VRuntimeException;
import org.vramework.commons.io.BinaryResource;
import org.vramework.commons.logging.ICallLevelAwareLog;
import org.vramework.commons.utils.VSert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Enables read/write from MS Office 2007 and 2010 documents. <br />
 * Note: At the moment, only setting text and setting/adding custom doc properties is supported. Since the MS Office
 * file format is a ZIP format, it is unfortunatley not possible to change only one part of the file "in place", e.g.
 * the doc properties. This was possible with Office 2000 and 2003 which used the OLE file format (and file streams for
 * each part). => Even if one changes only one part, one has to rewrite the whole ZIP file. <br />
 * An optimization could be made by declaring a {@link OfficeDoc} to change one part only. Then, one could at least save
 * the DOM-parsing of the other parts. Still, one would have to copy the bytes from the original to the new file.
 * 
 * @author thomas.mahringer
 */
public class OfficeDoc {

    /**
   * An MS Office Document consists of several parts: Contents, doc properties etc.
   */
    public static class Part {

        public static final Part PartDocument = new Part("word/document.xml");

        public static final Part PartPropertiesCore = new Part("docProps/core.xml");

        public static final Part PartPropertiesApp = new Part("docProps/app.xml");

        public static final Part PartPropertiesCustom = new Part("docProps/custom.xml");

        private String _name;

        public Part(String name) {
            setName(name);
        }

        /**
     * @return the name
     */
        public final String getName() {
            return _name;
        }

        /**
     * @param name
     *          the name to set
     */
        public final void setName(String name) {
            VSert.argNotEmpty(name);
            _name = name;
        }
    }

    /**
   * @see #getOfficeFile()
   */
    private ZipFile _officeFile;

    /**
   * @see #getContentsDom()
   */
    private Document _contentsDom;

    /**
   * @see #getCustomDocPropertiesDom()
   */
    private Document _customDocPropertiesDom;

    /**
   * @see #getCustomDocProperties()
   */
    private Map<String, Element> _customDocProperties;

    private ICallLevelAwareLog _log = VConf.getCallLevelAwareLogger(this);

    public OfficeDoc(String docPath) {
        try {
            ZipFile officeFile = new ZipFile(new File(docPath));
            setOfficeFile(officeFile);
            setContentsDom(parseToDom(Part.PartDocument));
            setCustomDocPropertiesDom(parseToDom(Part.PartPropertiesCustom));
            Map<String, Element> customDocProperties = parseCustomProperties();
            setCustomDocProperties(customDocProperties);
        } catch (Exception e) {
            throw new VRuntimeException(e);
        }
    }

    /**
   * @param part
   * @return The element of the word doc as DOM document.
   */
    public Document parseToDom(Part part) {
        _log.debug("parseToDom", new Object[] { part.getName() });
        ZipFile officeFile = getOfficeFile();
        ZipEntry documentXML = officeFile.getEntry(part.getName());
        if (documentXML == null) {
            return null;
        }
        try {
            InputStream documentXMLIS = officeFile.getInputStream(documentXML);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            Document doc = dbf.newDocumentBuilder().parse(documentXMLIS);
            return doc;
        } catch (Exception e) {
            throw new VRuntimeException(e);
        }
    }

    /**
   * Parses and extracts the custom doc propeties.
   * 
   * @return The extracted custom doc properties.
   */
    public Map<String, Element> parseCustomProperties() {
        Map<String, Element> customDocProps = new HashMap<String, Element>(50);
        if (getCustomDocPropertiesDom() == null) {
            _log.debug("Document does not have any custom doc properties");
        }
        Element docElement = getCustomDocPropertiesDom().getDocumentElement();
        NodeList docProps = docElement.getChildNodes();
        int len = docProps.getLength();
        for (int i = 0; i < len; i++) {
            Node node = docProps.item(i);
            Node nameAttributeNode = node.getAttributes().getNamedItem("name");
            String nameAttribute = nameAttributeNode == null ? "No name attribute" : nameAttributeNode.getNodeValue();
            _log.debug("logDirectChildrenOf", new Object[] { "DocProp node: Name: ", node.getNodeName(), ", name attribute: ", nameAttribute, ", value: ", node.getNodeValue(), ", text content: ", node.getTextContent() });
            customDocProps.put(nameAttribute, (Element) node);
        }
        return customDocProps;
    }

    /**
   * Sets a custom property. Creates it if does not yet exist. <br />
   * The property will be created in the DOM tree for custom properties and also cached in
   * {@link #getCustomDocProperties()}.
   * 
   * @param name
   * @param value
   */
    public void setCustomProperty(String name, String value) {
        VSert.argNotEmpty("name", name);
        Element customProperty = getCustomDocProperties().get(name);
        if (customProperty == null) {
            Document docPropsDom = getCustomDocPropertiesDom();
            Element docPropsDomDocument = docPropsDom.getDocumentElement();
            Element docProp = docPropsDom.createElement("property");
            docProp.setAttribute("fmtid", "{D5CDD505-2E9C-101B-9397-08002B2CF9AE}");
            docProp.setAttribute("name", name);
            docProp.setAttribute("pid", Integer.toString(getCustomDocProperties().size() + 2));
            docPropsDomDocument.appendChild(docProp);
            Element dataType = docPropsDom.createElement("vt:lpwstr");
            dataType.setTextContent(value);
            docProp.appendChild(dataType);
            getCustomDocProperties().put(name, docProp);
        } else {
            Node dataType = customProperty.getChildNodes().item(0);
            dataType.setTextContent(value);
        }
    }

    /**
   * Writes the officeFile file to the disk. The document contents as well as the custom doc properties are written.
   * 
   * @param filePath
   * @param deleteBefore
   *          true: Delete file before writing.
   */
    public void writeToFile(String filePath, boolean deleteBefore) {
        Transformer t;
        ByteArrayOutputStream documentContentsOs;
        ByteArrayOutputStream customPropsOs;
        try {
            t = TransformerFactory.newInstance().newTransformer();
            documentContentsOs = new ByteArrayOutputStream();
            customPropsOs = new ByteArrayOutputStream();
            t.transform(new DOMSource(getContentsDom()), new StreamResult(documentContentsOs));
            t.transform(new DOMSource(getCustomDocPropertiesDom()), new StreamResult(customPropsOs));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        File output = new File(filePath);
        if (output.exists()) {
            output.delete();
        }
        ZipOutputStream officeFileOutFile;
        try {
            officeFileOutFile = new ZipOutputStream(new FileOutputStream(filePath));
        } catch (FileNotFoundException e) {
            throw new VRuntimeException(e);
        }
        try {
            Enumeration<? extends ZipEntry> entriesIter = getOfficeFile().entries();
            while (entriesIter.hasMoreElements()) {
                ZipEntry entry = entriesIter.nextElement();
                _log.info("writeToFile", new Object[] { "Writing entry to output officeFile: ", entry.getName() });
                if (entry.getName().equals(OfficeDoc.Part.PartDocument.getName())) {
                    byte[] data = documentContentsOs.toByteArray();
                    officeFileOutFile.putNextEntry(new ZipEntry(entry.getName()));
                    officeFileOutFile.write(data, 0, data.length);
                    officeFileOutFile.closeEntry();
                    documentContentsOs.close();
                } else if (entry.getName().equals(OfficeDoc.Part.PartPropertiesCustom.getName())) {
                    byte[] data = customPropsOs.toByteArray();
                    officeFileOutFile.putNextEntry(new ZipEntry(entry.getName()));
                    officeFileOutFile.write(data, 0, data.length);
                    officeFileOutFile.closeEntry();
                    customPropsOs.close();
                } else {
                    InputStream incoming = getOfficeFile().getInputStream(entry);
                    byte[] data = BinaryResource.loadResource(incoming);
                    officeFileOutFile.putNextEntry(new ZipEntry(entry.getName()));
                    officeFileOutFile.write(data, 0, data.length);
                    officeFileOutFile.closeEntry();
                }
            }
        } catch (Exception e) {
            throw new VRuntimeException(e);
        } finally {
            try {
                officeFileOutFile.close();
            } catch (IOException e) {
                throw new VRuntimeException(e);
            }
        }
    }

    /**
   * Closes the zip files resources.
   */
    public void close() {
        try {
            getOfficeFile().close();
        } catch (IOException e) {
            throw new VRuntimeException(e);
        }
    }

    /**
   * Logs all direct children of the passed element ({@link ICallLevelAwareLog#info(String, Object...)}).
   * 
   * @param element
   */
    public void logDirectChildrenOf(Part element) {
        Document domDoc = parseToDom(element);
        if (domDoc == null) {
            _log.info("Element does not exist: " + element.getName());
            return;
        }
        Element docElement = domDoc.getDocumentElement();
        NodeList docProps = docElement.getChildNodes();
        int len = docProps.getLength();
        for (int i = 0; i < len; i++) {
            Node node = docProps.item(i);
            Node nameAttributeNode = node.getAttributes().getNamedItem("name");
            String nameAttribute = nameAttributeNode == null ? "No name attribute" : nameAttributeNode.getNodeName();
            _log.info("logDirectChildrenOf", new Object[] { "DocProp node: Name: ", node.getNodeName(), ", name attribute: ", nameAttribute, ", value: ", node.getNodeValue(), ", text content: ", node.getTextContent() });
        }
    }

    /**
   * Logs all entries of the word document zip file using {@link ICallLevelAwareLog#info(String, Object...)}.
   */
    public void logAllEntries() {
        Enumeration<? extends ZipEntry> entriesIter = getOfficeFile().entries();
        while (entriesIter.hasMoreElements()) {
            ZipEntry entry = entriesIter.nextElement();
            _log.info("logAllEntries", new Object[] { entry.getName() });
        }
    }

    /**
   * @return the encapsulated MS Office file
   */
    public final ZipFile getOfficeFile() {
        return _officeFile;
    }

    /**
   * @param officeFile
   *          the officeFile to set
   */
    public final void setOfficeFile(ZipFile officeFile) {
        VSert.argNotNull(officeFile);
        _officeFile = officeFile;
    }

    /**
   * @return the DOM tree of the document's contents
   */
    public final Document getContentsDom() {
        return _contentsDom;
    }

    /**
   * @param contents
   *          the contents to set
   */
    public final void setContentsDom(Document contents) {
        _contentsDom = contents;
    }

    /**
   * @return the custom doc properties as a Hasmap for easier lookup. It is backed by the DOM tree
   */
    public final Map<String, Element> getCustomDocProperties() {
        return _customDocProperties;
    }

    /**
   * @param customDocProperties
   *          the customDocProperties to set
   */
    public final void setCustomDocProperties(Map<String, Element> customDocProperties) {
        _customDocProperties = customDocProperties;
    }

    /**
   * @return the custom doc properties as DOM tree
   */
    public final Document getCustomDocPropertiesDom() {
        return _customDocPropertiesDom;
    }

    /**
   * @param customDocPropertiesDom
   *          the customDocPropertiesDom to set
   */
    public final void setCustomDocPropertiesDom(Document customDocPropertiesDom) {
        _customDocPropertiesDom = customDocPropertiesDom;
    }
}
