package org.openxml4j.document.wordprocessing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.openxml4j.document.OpenXMLDocument;
import org.openxml4j.document.wordprocessing.model.table.TableDescription;
import org.openxml4j.exceptions.InvalidOperationException;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackageRelationshipCollection;
import org.openxml4j.opc.PackageRelationshipTypes;
import org.openxml4j.opc.PackagingURIHelper;
import org.openxml4j.opc.StreamHelper;
import org.openxml4j.opc.internal.PartMarshaller;
import org.openxml4j.opc.internal.marshallers.ZipPartMarshaller;

/**
 * WordprocessingML document
 * 
 * @author Julien Chable
 * @version 0.1
 */
@Deprecated
public class WordDocument extends OpenXMLDocument {

    static final String PATH_WORD_DOCUMENT_XML = "word/document.xml";

    public static final Namespace namespaceWord = new Namespace("w", WordprocessingML.NS_WORD12);

    /**
	 * XML contents of the word document
	 */
    private Document content;

    private static Logger logger = Logger.getLogger("org.openxml4j");

    /**
	 * Help to manage contents of the document.
	 */
    private MainDocumentHelper mainDocumentHelper;

    public WordDocument(Package pack) throws OpenXML4JException {
        super(pack);
        mainDocumentHelper = new MainDocumentHelper();
        if (!mainDocumentHelper.parseDocumentContent()) {
            throw new OpenXML4JException("error in parsing doc");
        }
        container.addMarshaller(WMLContentType.WORD_MAIN_DOCUMENT, mainDocumentHelper);
    }

    /**
	 * Retrieve main document part (document.xml).
	 * 
	 * @throws OpenXML4JException
	 */
    public PackagePart getCorePart() throws OpenXML4JException {
        return container.getPartsByRelationshipType(PackageRelationshipTypes.CORE_DOCUMENT).get(0);
    }

    /**
	 * Permet d'obtenir l'arbre DOM du contenu du document.
	 */
    public Document getCoreDocument() {
        return content;
    }

    /**
	 * check each paragraph has a style. If some have none, set by default the
	 * value ParagraphBuilder.DEFAULT_PARAGRAPH_STYLE (ie Normal)
	 * 
	 * All paragraph should have a tag in order to generate a TOC without any
	 * problem
	 */
    public void setStyleForParagraphs() {
        List paragraphNodeList = content.getRootElement().elements(new QName(WordprocessingML.PARAGRAPH_BODY_TAG_NAME, namespaceWord));
        for (Iterator iter = paragraphNodeList.iterator(); iter.hasNext(); ) {
            Element element = (Element) iter.next();
            if (Paragraph.hasStyleName(element) == null) {
                Paragraph.addDefaultStyleXmlCode(element);
                logger.debug("adding default parameter style");
            }
        }
    }

    protected Element getDocumentBody() {
        return content.getRootElement().element(new QName(WordprocessingML.WORD_DOC_BODY_TAG_NAME, namespaceWord));
    }

    /**
	 * get the relationships we will extract from the document
	 * 
	 * @return
	 * @throws OpenXML4JException
	 */
    protected PackageRelationshipCollection getCollectionOfImageRelationshipsToForwardToNextDocument() throws OpenXML4JException {
        PackageRelationshipCollection listOfImages = null;
        try {
            PackagePart docPart = container.getPart(PackagingURIHelper.createPartName(new URI(PATH_WORD_DOCUMENT_XML)));
            if (docPart.hasRelationships()) {
                PackageRelationshipCollection relList = docPart.getRelationships();
                listOfImages = relList.getRelationships(PackageRelationshipTypes.IMAGE_PART);
            }
        } catch (URISyntaxException e) {
            logger.error("cannot generate URI", e);
        }
        return listOfImages;
    }

    /**
	 * @param curNode
	 * @return true if XML node is a paragraph in the word doc
	 */
    private boolean isNodeParagraph(Element curNode) {
        if (curNode.getName().equals(WordprocessingML.PARAGRAPH_BODY_TAG_NAME)) {
            return true;
        } else {
            return false;
        }
    }

    /**
	 * @param curNode
	 * @return true if the node is a table in the word doc
	 */
    private boolean isNodeTable(Node curNode) {
        if (curNode.getName().equals(WordprocessingML.TABLE_BODY_TAG_NAME)) {
            return true;
        } else {
            return false;
        }
    }

    /**
	 * TODO check why we have this ... see doc_with_tab_cr_as_text.docx for
	 * example of problematic file
	 * 
	 * @param curNode
	 * @return
	 */
    private boolean isNodeIndentText(Node curNode) {
        String strToTest;
        strToTest = curNode.getName();
        if (strToTest.equals("#text")) {
            return true;
        } else {
            return false;
        }
    }

    private List<Element> getListOfNodeToInsert(Element textToAppend, TreeMap<String, String> mapOldIdToNewId) {
        ArrayList<Element> listOfNodes = new ArrayList<Element>();
        for (Iterator j = textToAppend.elementIterator(); j.hasNext(); ) {
            Element curNodeToInsert = (Element) j.next();
            if (isNodeParagraphOrTable(curNodeToInsert)) {
                Element copyNode = (Element) (curNodeToInsert.clone());
                ArrayList<Element> listOfImages = new ArrayList<Element>();
                Picture.getListReferenceForImages(copyNode, listOfImages);
                if (!listOfImages.isEmpty()) {
                    if (!Picture.addNewReferences(listOfImages, mapOldIdToNewId)) {
                        return null;
                    }
                }
                listOfNodes.add(copyNode);
            } else {
                logger.debug("node ignored for paragraph insertion:" + curNodeToInsert.getName() + " " + curNodeToInsert.getStringValue());
            }
        }
        return listOfNodes;
    }

    /**
	 * @param curNodeToInsert:
	 *            node to check
	 * @return true if we should add this node to the merge doc. We import
	 *         paragraph and table from document.xml
	 */
    private boolean isNodeParagraphOrTable(Element curNodeToInsert) {
        if (isNodeParagraph(curNodeToInsert)) {
            return true;
        } else {
            return isNodeTable(curNodeToInsert);
        }
    }

    @SuppressWarnings("unchecked")
    public boolean appendAllParagraph(Element paragraphToAppend, TreeMap<String, String> mapOldIdToNewId) {
        List listParagraphToInsert = getListOfNodeToInsert(paragraphToAppend, mapOldIdToNewId);
        if (listParagraphToInsert == null) {
            return false;
        }
        Element body = getDocumentBody();
        for (Iterator i = body.elementIterator(); i.hasNext(); ) {
            Element curNode = (Element) i.next();
            if (!isNodeParagraphOrTable(curNode) && !isNodeIndentText(curNode)) {
                for (Iterator iter = listParagraphToInsert.iterator(); iter.hasNext(); ) {
                    Node elementtoInsert = (Node) iter.next();
                    body.elements().add(body.elements().indexOf(curNode), elementtoInsert);
                }
                return true;
            }
        }
        Iterator iter = listParagraphToInsert.iterator();
        while (iter.hasNext()) {
            logger.debug("appending to body");
            Node curNodeToInsert = (Node) iter.next();
            body.elements().add(curNodeToInsert);
        }
        return true;
    }

    /**
	 * append a paragraph at the end of the document
	 */
    @SuppressWarnings("unchecked")
    public void appendParagraph(Paragraph paragraph) {
        Element body = getDocumentBody();
        for (Iterator i = body.elementIterator(); i.hasNext(); ) {
            Element curNode = (Element) i.next();
            if (!isNodeParagraphOrTable(curNode) && !isNodeIndentText(curNode)) {
                body.elements().add(body.elements().indexOf(curNode), paragraph.build());
                return;
            }
        }
        body.add(paragraph.build());
    }

    /**
	 * append a table at the end of the document
	 * 
	 * @throws OpenXML4JException
	 */
    @SuppressWarnings("unchecked")
    public void appendTable(TableDescription table) throws OpenXML4JException {
        Element body = getDocumentBody();
        for (Iterator i = body.elementIterator(); i.hasNext(); ) {
            Element curNode = (Element) i.next();
            if (!isNodeParagraphOrTable(curNode) && !isNodeIndentText(curNode)) {
                body.elements().add(body.elements().indexOf(curNode), table.build());
                return;
            }
        }
        body.add(table.build());
    }

    /**
	 * Tool class to manage the document.xml.
	 * 
	 * @author Julien Chable
	 */
    class MainDocumentHelper implements PartMarshaller {

        /**
		 * parsing word document: document.xml
		 * 
		 * @return false if error
		 * @throws OpenXML4JException
		 */
        private boolean parseDocumentContent() throws OpenXML4JException {
            PackagePart contentPart = getCorePart();
            if (contentPart == null) {
                logger.error("The document has no contents!");
                return false;
            }
            logger.debug("reading doc content:" + contentPart.getPartName());
            InputStream inStream = null;
            try {
                inStream = contentPart.getInputStream();
            } catch (IOException e) {
                logger.error("error reading the document.xml", e);
                return false;
            }
            try {
                SAXReader reader = new SAXReader();
                content = reader.read(inStream);
            } catch (DocumentException e) {
                logger.error("cannot read input", e);
                return false;
            }
            return true;
        }

        /**
		 * Save the XML in document.xml in the Zip file
		 * 
		 * @throws OpenXML4JException
		 */
        public boolean marshall(PackagePart part, OutputStream os) throws OpenXML4JException {
            if (!(os instanceof ZipOutputStream)) {
                logger.error("ZipOutputSTream expected!" + os.getClass().getName());
                throw new OpenXML4JException("ZipOutputSTream expected!");
            }
            ZipOutputStream out = (ZipOutputStream) os;
            ZipEntry ctEntry = new ZipEntry(part.getPartName().getURI().getPath());
            try {
                out.putNextEntry(ctEntry);
                if (!StreamHelper.saveXmlInStream(content, out)) {
                    return false;
                }
                logger.debug("recording word doc relationship");
                if (part.hasRelationships()) {
                    ZipPartMarshaller.marshallRelationshipPart(part.getRelationships(), PackagingURIHelper.getRelationshipPartName(part.getPartName()), out);
                }
                out.closeEntry();
            } catch (IOException e1) {
                logger.error("IO problem with " + part.getPartName(), e1);
                return false;
            }
            return true;
        }
    }

    private boolean checkElementIsPermStart(Element element) {
        if (element.getName().equals(WordprocessingML.PARAGRAPH_PERM_START_TAG_NAME)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isWriteEnabledItem(Element nodeToCheck) throws OpenXML4JException {
        for (Iterator i = nodeToCheck.elementIterator(); i.hasNext(); ) {
            Element element = (Element) i.next();
            List listOfChild = element.elements();
            if (listOfChild.size() == 0) {
                return checkElementIsPermStart(element);
            } else {
                if (checkElementIsPermStart(element)) {
                    return true;
                }
                for (Iterator j = listOfChild.iterator(); j.hasNext(); ) {
                    Element child = (Element) j.next();
                    if (checkElementIsPermStart(child)) {
                        return true;
                    } else {
                        checkElementIsPermStart(child);
                    }
                }
            }
        }
        return false;
    }

    private boolean checkAndRemoveAllWriteEnableTags(Element element) {
        if (element.getName().equals(WordprocessingML.PARAGRAPH_PERM_START_TAG_NAME)) {
            element.getParent().remove(element);
            return true;
        } else {
            if (element.getName().equals(WordprocessingML.PARAGRAPH_PERM_END_TAG_NAME)) {
                element.getParent().remove(element);
                return true;
            }
        }
        return false;
    }

    /**
	 * recursive call to destroy all
	 * PARAGRAPH_PERM_START_TAG_NAME/PARAGRAPH_PERM_END_TAG_NAME
	 * 
	 * @param element
	 */
    private void removeAllWriteEnabledTags(Element element) {
        List listOfChild = element.elements();
        if (listOfChild.size() == 0) {
            checkAndRemoveAllWriteEnableTags(element);
        } else {
            if (checkAndRemoveAllWriteEnableTags(element)) {
                return;
            }
            for (Iterator j = listOfChild.iterator(); j.hasNext(); ) {
                Element child = (Element) j.next();
                checkAndRemoveAllWriteEnableTags(child);
            }
        }
    }

    /**
	 * remove all the "permStart"/ "permEnd" of the doc
	 * 
	 * @throws OpenXML4JException
	 * 
	 */
    public void removeWriteEnabledTags() throws OpenXML4JException {
        for (Iterator i = getDocumentBody().elementIterator(); i.hasNext(); ) {
            Element element = (Element) i.next();
            removeAllWriteEnabledTags(element);
        }
    }

    public void save(File file) {
        throw new InvalidOperationException("Method not implemented !");
    }

    /**
	 * remove all the read only paragraph of a doc Note: 1) we assume that the
	 * document has been set to read only by setting <w:documentProtection
	 * w:edit="readOnly" w:enforcement="1" /> in setting.xml. We do not check it
	 * 2) we do not for the moment remove the links that are no longer used (ex
	 * if there is a image inserted in read only, the image will remian in odc
	 * but will no longer be referenced anywhere) 3) we rely on the fact that
	 * the non read only paragraph have the xml permStart/permEnd We do not
	 * manage the case of only one permStart/permEnd is used to allow edition in
	 * more than 1 paragraph 4) table case is not managed (TODO) 5) the
	 * condition 1) and 3) are fulfilled when you generate read only doc with
	 * this API
	 * 
	 * @throws OpenXML4JException
	 * 
	 */
    public void stripReadOnlyPartOfDocument() throws OpenXML4JException {
        List paragraphNodeList = getDocumentBody().elements(new QName(WordprocessingML.PARAGRAPH_BODY_TAG_NAME, namespaceWord));
        for (Iterator iter = paragraphNodeList.iterator(); iter.hasNext(); ) {
            Element element = (Element) iter.next();
            if (!isWriteEnabledItem(element)) {
                Element father = element.getParent();
                father.remove(element);
            }
        }
    }
}
