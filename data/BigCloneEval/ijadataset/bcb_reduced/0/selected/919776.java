package org.openxml4j.document.word;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.openxml4j.document.OpenXMLDocument;
import org.openxml4j.document.word.headerAndFooter.Footer;
import org.openxml4j.document.word.headerAndFooter.Header;
import org.openxml4j.document.word.headerAndFooter.HeaderFooterType;
import org.openxml4j.document.word.numbering.ParagraphNumbering;
import org.openxml4j.document.word.numbering.Numbering;
import org.openxml4j.document.word.style.ParagraphStyleForTOC;
import org.openxml4j.document.word.style.Style;
import org.openxml4j.document.word.table.TableDescription;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.ContentTypeConstant;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackageAccess;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackageRelationship;
import org.openxml4j.opc.PackageRelationshipCollection;
import org.openxml4j.opc.PackageRelationshipConstants;
import org.openxml4j.opc.PackageURIHelper;
import org.openxml4j.opc.PartMarshaller;
import org.openxml4j.opc.ZipPartMarshaller;

/**
 *  Word 2007 document management
 * @author    Julien Chable
 */
public class WordDocument extends OpenXMLDocument {

    private static final String PATH_SETTING_XML = "word/settings.xml";

    /**
	 * XML contents of the word document
	 */
    private Document content;

    /**
	 * the style associated to the document (ex paragraph style)
	 */
    private Style style = new Style();

    private PageSize pageSize = null;

    private PredefinedSectionProperties predefinedSectionProperties = null;

    /**
	 * the document setting (used to set document as read only for ex)
	 */
    private Settings wordSettings = null;

    /**
	 * added doc footer (null if none)
	 */
    private Footer footer;

    /**
	 * added header (null if none)
	 */
    private Header header;

    private List existingFooter;

    private List existingHeader;

    private static Logger logger = Logger.getLogger("org.openxml4j");

    /**
	 * help to manage contents of the document.
	 */
    private MainDocumentHelper mainDocumentHelper;

    /**
	 * dealing witth chapter numbering
	 */
    private Numbering numbering = new Numbering();

    public WordDocument(Package pack) throws OpenXML4JException {
        super(pack);
        mainDocumentHelper = new MainDocumentHelper();
        if (!mainDocumentHelper.parseDocumentContent()) {
            throw new OpenXML4JException("error in parsing doc");
        }
        container.addMarshaller(ContentTypeConstant.WORD_MAIN_DOCUMENT, mainDocumentHelper);
        numbering.load(pack, getCorePart());
        extractFooterAndHeader();
        style.load(pack);
    }

    /**
	 * add a page format (A4) and orientation (portrait/landscape)
	 * @param pageFormat (A4 for ex)
	 * @param isLandscape
	 * @throws OpenXML4JException
	 */
    public void setPageSize(PageFormat pageFormat, boolean isLandscape) throws OpenXML4JException {
        pageSize = new PageSize(pageFormat, isLandscape);
    }

    /**
	 * Obtenir le contenu du document.
	 * @throws OpenXML4JException
	 */
    public PackagePart getCorePart() throws OpenXML4JException {
        return container.getPartByRelationshipType(PackageRelationshipConstants.NS_CORE_DOCUMENT).get(0);
    }

    /**
	 * Permet d'obtenir l'arbre DOM du contenu du document.
	 */
    public Document getCoreDocument() {
        return content;
    }

    /**
	 * saving.
	 * @throws OpenXML4JException
	 */
    public boolean save(File destFile) throws OpenXML4JException {
        addDocumentProperties();
        return super.save(destFile);
    }

    /**
	 * saving.
	 * @throws OpenXML4JException
	 */
    public boolean save(OutputStream outputSource) throws OpenXML4JException {
        addDocumentProperties();
        return super.save(outputSource);
    }

    /**
	 * @return the first free index for images (ie we can insert a file name image4.jpg if we return 4)
	 * @throws OpenXML4JException
	 */
    private int getFirstFreeIdInRelationship() throws OpenXML4JException {
        try {
            PackagePart docPart = container.getPart(new URI(WordprocessingML.PATH_WORD_DOCUMENT_XML));
            if (docPart.hasRelationships()) {
                PackageRelationshipCollection relList = docPart.getRelationships();
                return relList.size() + 1;
            }
        } catch (URISyntaxException e) {
            logger.error("cannot generate URI", e);
        }
        return 1;
    }

    private TreeMap<String, String> mergeImageRelationships(WordDocument secondDoc) throws OpenXML4JException {
        TreeMap<String, String> oldIdToNewId = new TreeMap<String, String>();
        int indexFreeImage = getFirstFreeIdInRelationship();
        PackageRelationshipCollection listOfRelationToForward = secondDoc.getCollectionOfImageAndOleRelationships();
        try {
            PackagePart docPart = container.getPart(new URI(WordprocessingML.PATH_WORD_DOCUMENT_XML));
            for (int i = 0; i < listOfRelationToForward.size(); i++) {
                URI targetUri = new URI(PackageURIHelper.combine("word", listOfRelationToForward.getRelationship(i).getTargetUri().getPath()));
                PackagePart imagePart = secondDoc.getPart(targetUri);
                if (imagePart == null) {
                    oldIdToNewId = null;
                    logger.error("cannot find image in file" + targetUri.getPath());
                } else {
                    InputStream ins = imagePart.getInputStream();
                    ByteArrayOutputStream imageInMemory = new ByteArrayOutputStream();
                    loadInputStreamInMemory(imageInMemory, ins);
                    URI newUri = new URI(PackageURIHelper.buildNewImageName(targetUri.getPath(), indexFreeImage));
                    indexFreeImage++;
                    if (logger.isDebugEnabled()) {
                        logger.debug("moving " + targetUri.getPath() + " to " + newUri.getPath());
                    }
                    PackagePart newPart = container.addPart(newUri, listOfRelationToForward.getRelationship(i).getRelationshipType(), imageInMemory);
                    if (newPart == null) {
                        logger.error("cannot extract image for URI" + newUri.toString());
                        return null;
                    }
                    URI uriWithoutWordAsDirectory = PackageURIHelper.removeFirstDirLevel(newUri);
                    if (uriWithoutWordAsDirectory == null) {
                        logger.error("cannot converting URI" + newUri.toString());
                        return null;
                    }
                    PackageRelationship newRel = docPart.addRelationship(uriWithoutWordAsDirectory, null, listOfRelationToForward.getRelationship(i).getRelationshipType());
                    if (logger.isDebugEnabled()) {
                        logger.debug("replacing id=" + listOfRelationToForward.getRelationship(i).getId() + " by " + newRel.getId());
                    }
                    oldIdToNewId.put(listOfRelationToForward.getRelationship(i).getId(), newRel.getId());
                }
            }
        } catch (URISyntaxException e) {
            logger.error("cannot generate URI", e);
            oldIdToNewId = null;
        } catch (IOException ioe) {
            logger.error("cannot generate image file", ioe);
            oldIdToNewId = null;
        }
        return oldIdToNewId;
    }

    private TreeMap<String, String> mergeNumbering(Numbering secondDocNumbering) throws OpenXML4JException {
        TreeMap<String, Element> abstractNumberingToAdd = new TreeMap<String, Element>();
        TreeMap<String, ArrayList<Element>> numberingToAdd = new TreeMap<String, ArrayList<Element>>();
        if (secondDocNumbering.getNumberingElements(abstractNumberingToAdd, numberingToAdd)) {
            TreeMap<String, String> convertingIdMap = numbering.merge(abstractNumberingToAdd, numberingToAdd);
            return convertingIdMap;
        } else {
            return null;
        }
    }

    /**
	 * merge 2 documents
	 * @param secondDoc
	 * @param mergeStyle the second doc should be set as read only or kept as is
	 *
	 * @return false if failed
	 *
	 * NOTE: in order to make the doc as read onyl, the mergeStyle should be set to read only AND
	 * 		the document should be set as read only (if not the whole doc will be write enabled)
	 * @see setDocumentAsReadOnly()
	 *
	 * @throws OpenXML4JException
	 */
    public boolean merge(WordDocument secondDoc, MergeStyle mergeStyle) throws OpenXML4JException {
        container.mergeDefaultContentType(secondDoc.container);
        TreeMap<String, String> convertingIdMap = mergeNumbering(secondDoc.getNumbering());
        if (convertingIdMap == null) {
            logger.error("failed in merging numbering.xml");
            return false;
        }
        TreeMap<String, String> mapOldIdToNewId = mergeImageRelationships(secondDoc);
        if (mapOldIdToNewId != null) {
            TreeMap<String, Element> treeMapStyleToForward = secondDoc.getCollectionOfStyleToForwardToNextDocument(convertingIdMap);
            if (treeMapStyleToForward == null) {
                return false;
            }
            if (!style.mergeStyle(treeMapStyleToForward, container)) {
                return false;
            }
            if (mergeStyle == MergeStyle.MERGE_AS_READ_ONLY) {
                secondDoc.removeWriteEnabledTags();
            }
            Element firstNode = secondDoc.getDocumentBody();
            return appendAllNodes(firstNode, mapOldIdToNewId);
        } else {
            return false;
        }
    }

    /**
	 * check each paragraph has a style.
	 * If some have none, set by default the value ParagraphBuilder.DEFAULT_PARAGRAPH_STYLE (ie Normal)
	 *
	 * All paragraph should have a tag in order to generate a TOC without any problem
	 */
    public void setStyleForParagraphs() {
        List paragraphNodeList = content.getRootElement().elements(new QName(WordprocessingML.PARAGRAPH_BODY_TAG_NAME, WordprocessingML.namespaceWord));
        for (Iterator iter = paragraphNodeList.iterator(); iter.hasNext(); ) {
            Element element = (Element) iter.next();
            if (Paragraph.hasStyleName(element) == null) {
                Paragraph.addDefaultStyleXmlCode(element);
                if (logger.isDebugEnabled()) {
                    logger.debug("adding default parameter style");
                }
            }
        }
    }

    public static boolean mergeAllFiles(File destFile, List<String> pathWordDocumentToMerge, MergeStyle mergeStyle) throws OpenXML4JException {
        if (pathWordDocumentToMerge.size() < 2) {
            logger.error("at least 2 file needed if you want to merge them " + pathWordDocumentToMerge.size());
            return false;
        }
        ZipFile zipFileSource1;
        try {
            zipFileSource1 = new ZipFile(pathWordDocumentToMerge.get(0));
            Package packSource1 = Package.open(zipFileSource1, PackageAccess.ReadWrite);
            WordDocument docxSource1 = new WordDocument(packSource1);
            for (int i = 1; i < pathWordDocumentToMerge.size(); i++) {
                if (logger.isDebugEnabled()) {
                    logger.debug("merging " + pathWordDocumentToMerge.get(0) + " , " + pathWordDocumentToMerge.get(i));
                }
                ZipFile zipFileSource2 = new ZipFile(pathWordDocumentToMerge.get(i));
                Package packSource2 = Package.open(zipFileSource2, PackageAccess.Read);
                WordDocument docxSource2 = new WordDocument(packSource2);
                if (!docxSource1.merge(docxSource2, mergeStyle)) {
                    return false;
                }
            }
            return docxSource1.save(destFile);
        } catch (IOException e) {
            logger.error("IO error", e);
            return false;
        }
    }

    /**
	 * @param readOnly true= set as read only, false all the document is write enable
	 * @return false if KO
	 * @throws OpenXML4JException
	 */
    public boolean setDocumentAsReadOnly(boolean readOnly) throws OpenXML4JException {
        try {
            if (wordSettings == null) {
                wordSettings = new Settings();
            }
            if (!wordSettings.setDocumentAsReadOnly(container, new URI(PATH_SETTING_XML), readOnly)) {
                return false;
            }
        } catch (URISyntaxException e) {
            logger.error("cannot generate URI", e);
            return false;
        }
        return true;
    }

    private PackagePart getPart(URI targetUri) throws OpenXML4JException {
        return container.getPart(targetUri);
    }

    protected Element getDocumentBody() {
        return content.getRootElement().element(new QName(WordprocessingML.WORD_DOC_BODY_TAG_NAME, WordprocessingML.namespaceWord));
    }

    /**
	 * get the relationships we will extract from the document for merging (or deleting when in read only part)
	 * @return
	 * @throws OpenXML4JException
	 */
    protected PackageRelationshipCollection getCollectionOfImageAndOleRelationships() throws OpenXML4JException {
        PackageRelationshipCollection listOfImages = null;
        try {
            PackagePart docPart = container.getPart(new URI(WordprocessingML.PATH_WORD_DOCUMENT_XML));
            if (docPart.hasRelationships()) {
                PackageRelationshipCollection relList = docPart.getRelationships();
                listOfImages = relList.getRelationships(PackageRelationshipConstants.NS_IMAGE_PART);
                PackageRelationshipCollection listOfOle = relList.getRelationships(PackageRelationshipConstants.NS_OLE_PART);
                listOfImages.addRelationships(listOfOle);
            }
        } catch (URISyntaxException e) {
            logger.error("cannot generate URI", e);
        }
        return listOfImages;
    }

    protected TreeMap<String, Element> getCollectionOfStyleToForwardToNextDocument(TreeMap<String, String> numberingOldToNewId) throws OpenXML4JException {
        Style styleOfDoc = new Style();
        TreeMap<String, Element> mapOfStyles = styleOfDoc.getCollectionOfStyleToForwardToNextDocument(container, numberingOldToNewId);
        return mapOfStyles;
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

    private boolean isNodePermStartPermEnd(Node curNode) {
        if (curNode.getName().equals(WordprocessingML.PARAGRAPH_PERM_START_TAG_NAME)) {
            return true;
        } else {
            if (curNode.getName().equals(WordprocessingML.PARAGRAPH_PERM_END_TAG_NAME)) {
                return true;
            }
        }
        return false;
    }

    /**
	 * TODO check why we have this ...
	 * see doc_with_tab_cr_as_text.docx for example of problematic file
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
            if (isNodeToInsertInOtherDoc(curNodeToInsert)) {
                Element copyNode = (Element) (curNodeToInsert.clone());
                List<Element> listOfImages = Picture.getListReferenceForImagesAndOle(copyNode);
                if (!listOfImages.isEmpty()) {
                    if (!Picture.addNewReferences(listOfImages, mapOldIdToNewId)) {
                        return null;
                    }
                }
                listOfNodes.add(copyNode);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("node ignored for paragraph insertion:" + curNodeToInsert.getName() + " " + curNodeToInsert.getStringValue());
                }
            }
        }
        return listOfNodes;
    }

    /**
	 * @param curNodeToInsert: node to check
	 * @return true if we should add this node to the merge doc. We import paragraph and table from document.xml
	 */
    private boolean isNodeToInsertInOtherDoc(Element curNodeToInsert) {
        if (isNodeParagraph(curNodeToInsert)) {
            return true;
        } else if (isNodeTable(curNodeToInsert)) {
            return true;
        } else if (isNodePermStartPermEnd(curNodeToInsert)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean appendAllNodes(Element paragraphToAppend, TreeMap<String, String> mapOldIdToNewId) {
        List listParagraphToInsert = getListOfNodeToInsert(paragraphToAppend, mapOldIdToNewId);
        if (listParagraphToInsert == null) {
            return false;
        }
        Element body = getDocumentBody();
        for (Iterator i = body.elementIterator(); i.hasNext(); ) {
            Element curNode = (Element) i.next();
            if (!isNodeToInsertInOtherDoc(curNode) && !isNodeIndentText(curNode)) {
                for (Iterator iter = listParagraphToInsert.iterator(); iter.hasNext(); ) {
                    Node elementtoInsert = (Node) iter.next();
                    body.elements().add(body.elements().indexOf(curNode), elementtoInsert);
                }
                return true;
            }
        }
        Iterator iter = listParagraphToInsert.iterator();
        while (iter.hasNext()) {
            if (logger.isDebugEnabled()) {
                logger.debug("appending to body");
            }
            Node curNodeToInsert = (Node) iter.next();
            body.elements().add(curNodeToInsert);
        }
        return true;
    }

    /**
	 * append a paragraph at the end of the document
	 */
    public void appendParagraph(Paragraph paragraph) {
        Element body = getDocumentBody();
        for (Iterator i = body.elementIterator(); i.hasNext(); ) {
            Element curNode = (Element) i.next();
            if (!isNodeToInsertInOtherDoc(curNode) && !isNodeIndentText(curNode)) {
                body.elements().add(body.elements().indexOf(curNode), paragraph.build());
                return;
            }
        }
        body.add(paragraph.build());
    }

    /**
	 * append a table at the end of the document
	 * @throws OpenXML4JException
	 */
    public void appendTable(TableDescription table) throws OpenXML4JException {
        Element body = getDocumentBody();
        for (Iterator i = body.elementIterator(); i.hasNext(); ) {
            Element curNode = (Element) i.next();
            if (!isNodeToInsertInOtherDoc(curNode) && !isNodeIndentText(curNode)) {
                body.elements().add(body.elements().indexOf(curNode), table.build());
                return;
            }
        }
        body.add(table.build());
    }

    private void appendTextObject(TextObject textObj) throws OpenXML4JException {
        if (textObj instanceof TableDescription) {
            TableDescription new_tab = (TableDescription) textObj;
            appendTable(new_tab);
        } else if (textObj instanceof Paragraph) {
            Paragraph newPara = (Paragraph) textObj;
            appendParagraph(newPara);
        } else {
            logger.error("unexpected class " + textObj.getClass().getName());
            throw new OpenXML4JException("unexpected class " + textObj.getClass().getName());
        }
    }

    /**
	 * append paragraph or table to the document
	 * @param list of TextObject
	 * @throws OpenXML4JException
	 */
    public void appendTextObjects(List textObjects) throws OpenXML4JException {
        for (Iterator iter = textObjects.iterator(); iter.hasNext(); ) {
            TextObject element = (TextObject) iter.next();
            appendTextObject(element);
        }
    }

    /**
	 * Tool class to manage the document.xml.
	 *
	 * @author Julien Chable
	 */
    class MainDocumentHelper implements PartMarshaller {

        /**
		 * parsing word document: document.xml
		 * @return false if error
		 * @throws OpenXML4JException
		 */
        private boolean parseDocumentContent() throws OpenXML4JException {
            PackagePart contentPart = getCorePart();
            if (contentPart == null) {
                logger.error("The document has no contents!");
                return false;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("reading doc content:" + contentPart.getUri());
            }
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
		 * @throws OpenXML4JException
		 */
        public boolean marshall(PackagePart part, OutputStream os) throws OpenXML4JException {
            if (!(os instanceof ZipOutputStream)) {
                logger.error("ZipOutputSTream expected!" + os.getClass().getName());
                throw new OpenXML4JException("ZipOutputSTream expected!");
            }
            ZipOutputStream out = (ZipOutputStream) os;
            ZipEntry ctEntry = new ZipEntry(part.getUri().getPath());
            try {
                out.putNextEntry(ctEntry);
                if (!Package.saveAsXmlInZip(content, part.getUri().getPath(), out)) {
                    return false;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("recording word doc relationship");
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

    private boolean checkElementIsPermStart(Element element) {
        if (element.getName().equals(WordprocessingML.PARAGRAPH_PERM_START_TAG_NAME)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkElementIsPermEnd(Element element) {
        if (element.getName().equals(WordprocessingML.PARAGRAPH_PERM_END_TAG_NAME)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isPermStartFound(Element nodeToCheck) throws OpenXML4JException {
        if (nodeToCheck.elements().size() == 0) {
            return checkElementIsPermStart(nodeToCheck);
        }
        for (Iterator i = nodeToCheck.elementIterator(); i.hasNext(); ) {
            Element element = (Element) i.next();
            List listOfChild = element.elements();
            if (listOfChild.size() == 0) {
                if (checkElementIsPermStart(element)) {
                    return true;
                }
            } else {
                for (Iterator j = listOfChild.iterator(); j.hasNext(); ) {
                    Element child = (Element) j.next();
                    if (isPermStartFound(child)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isPermEndFound(Element nodeToCheck) throws OpenXML4JException {
        if (nodeToCheck.elements().size() == 0) {
            return checkElementIsPermEnd(nodeToCheck);
        }
        for (Iterator i = nodeToCheck.elementIterator(); i.hasNext(); ) {
            Element element = (Element) i.next();
            List listOfChild = element.elements();
            if (listOfChild.size() == 0) {
                if (checkElementIsPermEnd(element)) {
                    return true;
                }
            } else {
                for (Iterator j = listOfChild.iterator(); j.hasNext(); ) {
                    Element child = (Element) j.next();
                    if (isPermEndFound(child)) {
                        return true;
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
	 * recursive call to destroy all PARAGRAPH_PERM_START_TAG_NAME/PARAGRAPH_PERM_END_TAG_NAME
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
	 * @throws OpenXML4JException
	 *
	 */
    public void removeWriteEnabledTags() throws OpenXML4JException {
        for (Iterator i = getDocumentBody().elementIterator(); i.hasNext(); ) {
            Element element = (Element) i.next();
            removeAllWriteEnabledTags(element);
        }
    }

    /**
	 * remove all the read only paragraph of a doc
	 * Note:
	 * 1) we assume that the document has been set to read only by setting
	 *   <w:documentProtection w:edit="readOnly" w:enforcement="1" /> in setting.xml. We do not check it
	 * 2) we do not for the moment remove the links that are no longer used (ex if there is a image inserted in read only, the image will remain in doc but will no longer be referenced anywhere)
	 * 3) we do not check the permStart id. Threfore when we find permEnd, we assume it match the permStart

	 * @throws OpenXML4JException
	 *
	 */
    public void stripReadOnlyPartOfDocument() throws OpenXML4JException {
        List listNodes = getDocumentBody().elements();
        boolean inWriteEnableMode = false;
        boolean permEndFound = false;
        for (Iterator iter = listNodes.iterator(); iter.hasNext(); ) {
            Element element = (Element) iter.next();
            if (isPermStartFound(element)) {
                inWriteEnableMode = true;
            }
            if (inWriteEnableMode) {
                permEndFound = isPermEndFound(element);
            } else {
                stripImageInDocAndDeleteElement(element);
            }
            if (permEndFound) {
                inWriteEnableMode = false;
                permEndFound = false;
            }
        }
    }

    /**
	 * normally in order to get easily rid of doc parts we should use w:sdt feature like
	 * <w:sdt>
	 * 		<w:sdtPr>
	 * 			<w:alias w:val="Birthday"/>
	 * 			<w:id w:val="8775518"/>
	 * 		</w:sdtPr>
	 * 		<w:sdtContent>
	 * 			<w:p> <w:r> the open xml code
 			</w:sdtContent>
 		</w:sdt>

 		 unfortunately word 2003 does not support this feature (it can read it but when saving, it is lost ...)
 		 Therfore we use write enabled part to get a marker to know what we shall keep or get rid of

	 * @param writeEnablePartToKeep the index of the write enable part to keep
	 * 			1 ->keep in the document only the first write enable part
	 * 			2 ->                              second
	 * @throws OpenXML4JException
	 */
    public void stripReadOnlyPartOfDocument(int writeEnablePartToKeep) throws OpenXML4JException {
        List listNodes = getDocumentBody().elements();
        boolean inWriteEnableMode = false;
        boolean permEndFound = false;
        int writeEnablePartFound = 0;
        for (Iterator iter = listNodes.iterator(); iter.hasNext(); ) {
            Element element = (Element) iter.next();
            if (writeEnablePartFound <= writeEnablePartToKeep) {
                if (isPermStartFound(element)) {
                    inWriteEnableMode = true;
                    writeEnablePartFound++;
                }
            }
            if (inWriteEnableMode) {
                permEndFound = isPermEndFound(element);
                if (writeEnablePartFound != writeEnablePartToKeep) {
                    stripImageInDocAndDeleteElement(element);
                }
            } else {
                stripImageInDocAndDeleteElement(element);
            }
            if (permEndFound) {
                inWriteEnableMode = false;
                permEndFound = false;
            }
        }
    }

    private void stripImageInDocAndDeleteElement(Element element) throws OpenXML4JException {
        stripImages(element);
        Element father = element.getParent();
        father.remove(element);
    }

    private void stripImages(Element elementToRemove) throws OpenXML4JException {
        List listOfImages = elementToRemove.selectNodes(".//v:" + WordprocessingML.IMAGEDATA_TAG_NAME);
        PackageRelationshipCollection ListOfImagesPart = getCollectionOfImageAndOleRelationships();
        for (Iterator iter = listOfImages.iterator(); iter.hasNext(); ) {
            Element imagedata = (Element) iter.next();
            String imageId = imagedata.attributeValue(new QName(WordprocessingML.ATTRIBUTE_ID_TAG_NAME, WordprocessingML.namespaceRelationship));
            if (logger.isDebugEnabled()) {
                logger.debug("removing images " + imageId);
            }
            try {
                PackagePart docPart = container.getPart(new URI(WordprocessingML.PATH_WORD_DOCUMENT_XML));
                PackageRelationship relationship = ListOfImagesPart.getRelationship(imageId);
                if (relationship == null) {
                    logger.error("image found without relationship ! " + imageId);
                    throw new OpenXML4JException("image found without relationship ! " + imageId);
                }
                URI partUri = relationship.getTargetUri();
                URI uriToDel = new URI("word/" + partUri.toASCIIString());
                docPart.removePart(uriToDel);
                docPart.removeRelationship(imageId);
            } catch (URISyntaxException e) {
                logger.error(e);
                throw new OpenXML4JException(e.getMessage());
            }
        }
    }

    /**
	 * add section properties (ie we the page would be described, footer, header, orientation etc ...)
	 * as the same tag is used to describe the document properties or just a parameter section,
	 * we provide 2 parameters to reuse the function in both case
	 *
	 * @param insertingPoint
	 * @throws OpenXML4JException
	 */
    private void addSectionProperties(Element insertingPoint) throws OpenXML4JException {
        Element sectionProperties = insertingPoint.addElement(new QName(WordprocessingML.SECTION_PROPERTIES, WordprocessingML.namespaceWord));
        if (pageSize != null) {
            sectionProperties.add(pageSize.build());
        }
        if (predefinedSectionProperties != null) {
            predefinedSectionProperties.build(sectionProperties);
        }
        if (footer != null) {
            sectionProperties.add(footer.build());
        }
        if (header != null) {
            sectionProperties.add(header.build());
        }
    }

    /**
	 * add the doc orientation and header/footer
	 * @throws OpenXML4JException
	 */
    protected void addDocumentProperties() throws OpenXML4JException {
        if ((pageSize != null) || (footer != null)) {
            deleteSectionProperties();
            addSectionProperties(getDocumentBody());
        }
    }

    /**
	 * delete  section properties
	 * we remain at the first level of the tree, so if we have more than one section properties it should be OK (TODO TBC)
	 */
    private void deleteSectionProperties() {
        for (Iterator i = getDocumentBody().elementIterator(); i.hasNext(); ) {
            Element element = (Element) i.next();
            if (element.getName().equals(WordprocessingML.SECTION_PROPERTIES)) {
                element.getParent().remove(element);
                if (logger.isDebugEnabled()) {
                    logger.debug("removing section properties");
                }
                break;
            }
        }
    }

    /**
	 * add footer in the relationship (ie document.xml.rels)
	 * @param p_footer
	 * @throws OpenXML4JException
	 */
    public void addFooter(Footer p_footer) throws OpenXML4JException {
        footer = p_footer;
        PackagePart docPart;
        try {
            docPart = container.getPart(new URI(WordprocessingML.PATH_WORD_DOCUMENT_XML));
        } catch (URISyntaxException e) {
            logger.error(e);
            throw new OpenXML4JException(e.getMessage());
        }
        PackageRelationship rel = docPart.addRelationship(footer.getUri(false, false), null, PackageRelationshipConstants.NS_FOOTER_PART, null);
        footer.setId(rel.getId());
        footer.integrateInDocument(container);
    }

    /**
	 * search for something like <w:footerReference w:type="even" r:id="rId8"/> and return even
	 * @return even, default ...
	 * @throws OpenXML4JException
	 */
    private HeaderFooterType getElementForFooterOrHeader(String id, String footerOrHeaderKeyword) throws OpenXML4JException {
        XPath xpathSelector = DocumentHelper.createXPath("//w:" + WordprocessingML.WORD_DOC_BODY_TAG_NAME + "//w:" + WordprocessingML.SECTION_PROPERTIES + "//w:" + footerOrHeaderKeyword + "[@r:id=\"" + id + "\"]");
        Map<String, String> uris = new HashMap<String, String>();
        uris.put("w", WordprocessingML.NS_WORD12);
        uris.put("r", WordprocessingML.NS_RELATIONSHIPS);
        xpathSelector.setNamespaceURIs(uris);
        Element node = (Element) xpathSelector.selectSingleNode(content);
        Attribute type = node.attribute(new QName(WordprocessingML.ATTRIBUTE_TYPE, WordprocessingML.namespaceWord));
        if (type == null) {
            logger.debug("no type found, assume default");
            return HeaderFooterType.DEFAULT;
        }
        return HeaderFooterType.getHeaderFooterTypeFromText(type.getStringValue());
    }

    /**
	 * extract the footer/header from the document
	 * Word 2003 usually produce 3 footers (first, even and default (the class HeaderFooterType values)
	 * @throws OpenXML4JException
	 */
    private void extractFooterAndHeader() throws OpenXML4JException {
        PackagePart docPart;
        try {
            docPart = container.getPart(new URI(WordprocessingML.PATH_WORD_DOCUMENT_XML));
        } catch (URISyntaxException e) {
            logger.error(e);
            throw new OpenXML4JException(e.getMessage());
        }
        {
            PackageRelationshipCollection listMatches = docPart.getRelationshipsByType(PackageRelationshipConstants.NS_FOOTER_PART);
            existingFooter = new ArrayList(listMatches.size());
            for (Iterator iter = listMatches.iterator(); iter.hasNext(); ) {
                PackageRelationship rel = (PackageRelationship) iter.next();
                existingFooter.add(new Footer(getElementForFooterOrHeader(rel.getId(), WordprocessingML.FOOTER_REFERENCE), rel, container));
            }
        }
        {
            PackageRelationshipCollection listMatches = docPart.getRelationshipsByType(PackageRelationshipConstants.NS_HEADER_PART);
            existingHeader = new ArrayList(listMatches.size());
            for (Iterator iter = listMatches.iterator(); iter.hasNext(); ) {
                PackageRelationship rel = (PackageRelationship) iter.next();
                existingHeader.add(new Header(getElementForFooterOrHeader(rel.getId(), WordprocessingML.HEADER_REFERENCE), rel, container));
            }
        }
    }

    public PredefinedSectionProperties getPredefinedSectionProperties() {
        return predefinedSectionProperties;
    }

    public void setPredefinedSectionProperties(PredefinedSectionProperties predefinedSectionProperties) {
        this.predefinedSectionProperties = predefinedSectionProperties;
    }

    public void addAbstractParagraphNumbering(ParagraphNumbering chapterNumber) {
        numbering.addabstractParagaphToAdd(chapterNumber);
        style.addParagraph(chapterNumber);
    }

    public Numbering getNumbering() {
        return numbering;
    }

    /**
	 * insert a table of contents
	 * @param toc
	 * @param paraStyleForToc (null if we wish to use default word toc paragraph)
	 * @param writeEnableTag
	 */
    public void insertAtStartDocumentTableOfContents(TableOfContents toc, ParagraphStyleForTOC paraStyleForToc, boolean writeEnableTag) {
        List list = getDocumentBody().content();
        int index = 0;
        for (Iterator iter = toc.build(writeEnableTag).iterator(); iter.hasNext(); ) {
            Element element = (Element) iter.next();
            list.add(index, element);
            index++;
        }
        if (paraStyleForToc != null) {
            style.addParagraph(paraStyleForToc);
        }
    }

    public void addHeader(Header p_header) throws OpenXML4JException {
        header = p_header;
        PackagePart docPart;
        try {
            docPart = container.getPart(new URI(WordprocessingML.PATH_WORD_DOCUMENT_XML));
        } catch (URISyntaxException e) {
            logger.error(e);
            throw new OpenXML4JException(e.getMessage());
        }
        PackageRelationship rel = docPart.addRelationship(header.getUri(false, false), null, PackageRelationshipConstants.NS_HEADER_PART, null);
        header.setId(rel.getId());
        header.integrateInDocument(container);
    }

    /**
	 * add open xml code "as is"
	 * <BR>This feature is useful to insert complicated formated table/forms in a document. User can build them with MS-Word and then insert it
	 * @param openXmlCode the open xml code
	 */
    public void addOpenXmlCode(Node openXmlCode) {
        Element body = getDocumentBody();
        body.add(openXmlCode);
    }

    public List getExistingFooter() {
        return existingFooter;
    }

    public void setExistingFooter(List existingFooter) {
        this.existingFooter = existingFooter;
    }

    public List getExistingHeader() {
        return existingHeader;
    }

    public void setExistingHeader(List existingHeader) {
        this.existingHeader = existingHeader;
    }
}
