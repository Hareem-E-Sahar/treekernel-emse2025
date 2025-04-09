package com.germinus.xpression.cms.educative;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import javax.servlet.ServletContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;
import com.germinus.xpression.cms.CMSRuntimeException;
import com.germinus.xpression.cms.CmsConfig;
import com.germinus.xpression.cms.JCRLockedOperationResult;
import com.germinus.xpression.cms.contents.CMSData;
import com.germinus.xpression.cms.contents.Content;
import com.germinus.xpression.cms.contents.ContentIF;
import com.germinus.xpression.cms.contents.ContentManager;
import com.germinus.xpression.cms.contents.ContentWithoutChaptersException;
import com.germinus.xpression.cms.contents.DraftContent;
import com.germinus.xpression.cms.contents.JCRContentManager;
import com.germinus.xpression.cms.contents.MalformedContentException;
import com.germinus.xpression.cms.directory.DirectoryFile;
import com.germinus.xpression.cms.directory.DirectoryFolder;
import com.germinus.xpression.cms.directory.DirectoryItem;
import com.germinus.xpression.cms.directory.DirectoryItemNotFoundException;
import com.germinus.xpression.cms.directory.DirectoryPersister;
import com.germinus.xpression.cms.directory.ErrorUnzippingZipEntryException;
import com.germinus.xpression.cms.directory.MalformedDirectoryItemException;
import com.germinus.xpression.cms.jcr.ContentNode;
import com.germinus.xpression.cms.jcr.JCRLockManager;
import com.germinus.xpression.cms.jcr.JCRManagerRegistry;
import com.germinus.xpression.cms.jcr.JCRUtil;
import com.germinus.xpression.cms.model.ContentTypes;
import com.germinus.xpression.cms.util.ManagerRegistry;
import com.germinus.xpression.cms.worlds.World;
import com.germinus.xpression.cms.worlds.WorldManager;
import com.germinus.xpression.menu.Organizations;
import com.germinus.xpression.menu.ToolMenu;

public class ScormUtil extends ImportExportFileUtil {

    static final String IMSMANIFEST_FILE_NAME = "imsmanifest.xml";

    public static final String FIRST_CHAPTER_HTML = "first_chapter.html";

    private static long nextOrganizationItemIdSuffix = System.currentTimeMillis();

    private static Log log = LogFactory.getLog(ScormUtil.class);

    private static ScormUtil instance;

    public static ScormUtil getInstance() {
        if (instance == null) {
            instance = new ScormUtil();
            return instance;
        }
        return instance;
    }

    public Content importScormZip(String userId, String worldId, String contentType, DirectoryFolder targetFolder, String fileName, InputStream inputStream, File file) throws ScormFormatException, ErrorUnzippingZipEntryException {
        DraftContent content = null;
        try {
            content = new DraftContent();
            content.setContentTypeId(ContentTypes.CHAPTERS);
            content.setAuthorId(userId);
            CMSData contentData = ContentTypes.createContentDataFor(ContentTypes.CHAPTERS);
            content.setContentData(contentData);
            content.setScormImported(true);
            content = saveAsDraftContent(content, worldId, targetFolder);
            DirectoryFolder resourcesFolder = content.getResourcesFolder();
            ManagerRegistry.getDirectoryPersister().addZipFile(resourcesFolder, file, null);
            Document document = parseDirectoryFile(resourcesFolder.getURLPath() + "/" + IMSMANIFEST_FILE_NAME);
            removeDirectoryItem(resourcesFolder.getURLPath() + "/" + IMSMANIFEST_FILE_NAME);
            Node contentNode = JCRUtil.getNodeById(content.getId(), content.getWorkspace());
            removeCurrentManifestNode(contentNode);
            removePrefixes(document);
            addManifestToContentNode(document, contentNode);
            contentNode.save();
            changeOrganizationItemReferences(content);
            convertStringLanguageValues(contentNode);
            contentNode.save();
            JCRContentManager jcrContentManager = JCRManagerRegistry.getJcrContentManager();
            jcrContentManager.removeFromCache(contentNode);
            Content contentById = jcrContentManager.getContentById(content.getId(), content.getWorkspace());
            addLomIfNotPresent(worldId, contentById);
            jcrContentManager.removeFromCache(content.getScribeContentUrl());
            JCRUtil.currentSession(targetFolder.getWorkspace()).save();
            return contentById;
        } catch (SAXException e) {
            log.error("Error parsing manifest file.");
            throw new ScormFormatException(e);
        } catch (ErrorUnzippingZipEntryException ezipe) {
            ManagerRegistry.getContentManager().deleteContent(content);
            log.error("Error reading zip entry");
            throw ezipe;
        } catch (Exception e) {
            log.error("SCORM content " + fileName + " could not be " + "imported correctly in world " + worldId + " by user " + userId, e);
            throw new CMSRuntimeException(e);
        }
    }

    private void removePrefixes(Document document) {
        NodeList elements = document.getElementsByTagName("*");
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            if (element.getPrefix() != null) {
                element.setPrefix(null);
                element.setAttribute("xmlns", element.getNamespaceURI());
            }
        }
    }

    private ContentIF addLomIfNotPresent(final String worldId, final Content contentToCheckLomPresence) throws MalformedContentException {
        final JCRContentManager jcrContentManagerAux = JCRManagerRegistry.getJcrContentManager();
        if (contentToCheckLomPresence.getManifest().getMetadata().get(IMSConstants.METADATA_LOM) == null) {
            contentToCheckLomPresence.getManifest().getMetadata().set(IMSConstants.METADATA_LOM, new Lom());
            Callable<Content> callable = new Callable<Content>() {

                public Content call() throws Exception {
                    return jcrContentManagerAux.updateContentStealthy(contentToCheckLomPresence, ManagerRegistry.getWorldManager().findWorldById(worldId));
                }
            };
            Node contentNode;
            try {
                contentNode = JCRUtil.getNodeById(contentToCheckLomPresence.getId(), contentToCheckLomPresence.getWorkspace());
            } catch (RepositoryException e) {
                throw new CMSRuntimeException("Error obtaining node for scorm imported content " + contentToCheckLomPresence.getId());
            }
            JCRLockedOperationResult<Content> result = new JCRLockManager().synchronize(contentNode, callable, false);
            if (result.isExceptionalResult()) {
                throw (MalformedContentException) result.getExcepcionalResult();
            }
            return result.getNormalResultValue();
        }
        return contentToCheckLomPresence;
    }

    private void convertStringLanguageValues(Node contentNode) {
        try {
            Node manifestNode = ContentNode.createContentNode(contentNode).getManifestNode();
            searchAndModifyStringLanguageNodes(manifestNode);
        } catch (RepositoryException e) {
            String errorMessage = "Error converting language values in string nodes";
            log.error(errorMessage, e);
            throw new CMSRuntimeException(errorMessage, e);
        } catch (MalformedContentException e) {
            String errorMessage = "Error converting language values in string nodes";
            log.error(errorMessage, e);
            throw new CMSRuntimeException(errorMessage, e);
        }
    }

    private void searchAndModifyStringLanguageNodes(Node parentNode) throws RepositoryException, ValueFormatException, VersionException, LockException, ConstraintViolationException {
        NodeIterator nodes = parentNode.getNodes();
        while (nodes.hasNext()) {
            Node node = nodes.nextNode();
            if (node.getName().equals(IMSConstants.STRING)) modifyStringNode(node); else searchAndModifyStringLanguageNodes(node);
        }
    }

    private Property modifyStringNode(Node stringNode) throws RepositoryException, ValueFormatException, VersionException, LockException, ConstraintViolationException {
        Property languageProperty;
        try {
            languageProperty = stringNode.getProperty(JCRUtil.XML_LANG_PROPERTY);
            languageProperty.setValue(languageProperty.getString().replaceAll("-", "_"));
            return languageProperty;
        } catch (PathNotFoundException e) {
            return null;
        }
    }

    private void addManifestToContentNode(Document document, Node contentNode) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException, AccessDeniedException, InvalidItemStateException, ReferentialIntegrityException, NoSuchNodeTypeException {
        if (log.isDebugEnabled()) log.debug("Adding manifest to content node.");
        NodeList documentChildNodes = document.getChildNodes();
        JCRXMLConverter converter = new JCRXMLConverter();
        for (int i = 0; i < documentChildNodes.getLength(); i++) {
            log.debug("Adding node to JCR: " + documentChildNodes.item(i).getNodeName());
            converter.addDomNodeToJcrNode(documentChildNodes.item(i), contentNode);
        }
        contentNode.save();
        ContentNode contentNode2 = ContentNode.createContentNode(contentNode);
        try {
            Node manifestNode = contentNode2.getManifestNode();
            new JCRIMSUtil().simplifyIMSListNodes(manifestNode);
            manifestNode.save();
        } catch (MalformedContentException e) {
            e.printStackTrace();
        }
    }

    private void removeCurrentManifestNode(Node contentNode) throws PathNotFoundException, RepositoryException, VersionException, LockException, ConstraintViolationException, AccessDeniedException, ItemExistsException, InvalidItemStateException, ReferentialIntegrityException, NoSuchNodeTypeException {
        if (log.isDebugEnabled()) log.debug("Removing current manifest node.");
        Node manifestNode = contentNode.getNode(JCRUtil.MANIFEST_PREFIX);
        manifestNode.remove();
        contentNode.save();
    }

    private void removeDirectoryItem(String path) throws DirectoryItemNotFoundException {
        if (log.isDebugEnabled()) log.debug("Removing file from directory: " + path);
        DirectoryPersister directoryPersister = ManagerRegistry.getDirectoryPersister();
        DirectoryFile manifestFile1 = (DirectoryFile) directoryPersister.getItemFromPath(path);
        directoryPersister.deleteItem(manifestFile1);
    }

    private Document parseDirectoryFile(String path) throws DirectoryItemNotFoundException, SAXException, IOException, MalformedDirectoryItemException {
        if (log.isDebugEnabled()) log.debug("Parsing directory file.");
        DirectoryPersister directoryPersister = ManagerRegistry.getDirectoryPersister();
        DirectoryFile manifestFile = (DirectoryFile) directoryPersister.getItemFromPath(path);
        InputStream manifestInputStream = directoryPersister.getInputStreamFromFile(manifestFile);
        Document document = JCRXMLConverter.parseDom(manifestInputStream);
        manifestInputStream.close();
        return document;
    }

    private static void addZipFileToResourcesFolder(DirectoryFolder resourcesFolder, String contentType, String fileName, File file) throws ErrorUnzippingZipEntryException {
        ManagerRegistry.getDirectoryPersister().addZipFile(resourcesFolder, file, null);
    }

    private DraftContent saveAsDraftContent(DraftContent content, String worldId, DirectoryFolder targetFolder) throws MalformedContentException {
        ContentManager contentManager1 = ManagerRegistry.getContentManager();
        WorldManager worldManager = ManagerRegistry.getWorldManager();
        World world = worldManager.findWorldById(worldId);
        content = contentManager1.saveAsDraftContent(content, world, targetFolder);
        return content;
    }

    public boolean containsChapters(Content content) {
        Organizations organizations = content.getManifest().getOrganizations();
        if (organizations != null) {
            Iterator<ToolMenu> organizationsIter = organizations.iterator();
            if (organizationsIter.hasNext()) {
                ToolMenu tmenu = organizationsIter.next();
                if (!tmenu.getChildren().isEmpty()) return true;
            }
        }
        return false;
    }

    public String exportContentToZip(ExportContentData exportContentData, ZipOutputStream zipOutStream) throws CMSRuntimeException, ContentWithoutChaptersException {
        Content content = exportContentData.getContent();
        try {
            Document document = getXMLDocumentFromContent(content);
            appendDefaultManifestAttributes((Element) document.getFirstChild());
            Organizations organizations = content.getManifest().getOrganizations();
            Set<String> includedChapters = new HashSet<String>();
            List<String> filesToExclude = new ArrayList<String>();
            String generateFirstChapter = "";
            String indexFile = FIRST_CHAPTER_HTML;
            if (!containsChapters(content)) {
                Long contentTypeId = content.getContentTypeId();
                if (contentTypeId.equals(ContentTypes.CHAPTERS)) {
                    log.error("Content with id " + content.getId() + " got no chapters");
                    throw new ContentWithoutChaptersException();
                }
                if (contentTypeId.equals(ContentTypes.WEB_FILES)) {
                    indexFile = BeanUtils.getProperty(content, "contentData.indexFile.fileName");
                } else {
                    filesToExclude.add(indexFile);
                    generateFirstChapter = indexFile;
                }
                createDefaultOrganizationsAndResources(document, content.getName(), indexFile);
            } else {
                addOrganizationIdentifiers(document);
                Element resourcesElement = getElement(document, IMSConstants.RESOURCES);
                if (resourcesElement.hasChildNodes()) {
                    NodeList resourcesChildren = resourcesElement.getChildNodes();
                    int lengthChildren = resourcesChildren.getLength();
                    for (int i = 0; i < lengthChildren; i++) resourcesElement.removeChild(resourcesChildren.item(0));
                }
                includedChapters = addResourcesFromOrganizations(content, document, organizations, resourcesElement);
            }
            replaceIdentifiers(document);
            addLomNameSpace(document);
            convertStringLanguageValues(document, content.getWorkspace());
            removeUnnecesaryNameSpacePrefixes(document);
            removeUnnecesaryAttributes(document);
            reorganizeManifestChildElements(document);
            addManifestEntryToZip(zipOutStream, document);
            addDirectoryItemsToZip(content.getResourcesFolder(), "", zipOutStream, includedChapters, exportContentData, filesToExclude);
            exportScormControlAndImageFiles(content, zipOutStream);
            return generateFirstChapter;
        } catch (ItemNotFoundException e) {
            String errorMessage = "There is no jcr node with the id of the content: " + content.getId();
            log.error(errorMessage, e);
            throw new CMSRuntimeException(errorMessage, e);
        } catch (ContentWithoutChaptersException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = "Error exporting to scorm zip the content " + content.getName() + " with id " + content.getId();
            log.error(errorMessage);
            String originalErrorMessage = e.getMessage();
            throw new CMSRuntimeException((originalErrorMessage != null) ? originalErrorMessage : errorMessage, e);
        }
    }

    public InputStream getInputStreamFromPath(DirectoryFile file) throws MalformedDirectoryItemException {
        return ManagerRegistry.getDirectoryPersister().getInputStreamFromFile(file);
    }

    private void addManifestEntryToZip(ZipOutputStream zipOutStream, Document document) throws IOException, TransformerFactoryConfigurationError, TransformerException {
        zipOutStream.putNextEntry(new ZipEntry(IMSMANIFEST_FILE_NAME));
        JCRXMLConverter.writeXmlDocument(document, zipOutStream);
        zipOutStream.closeEntry();
    }

    private Document getXMLDocumentFromContent(Content content) throws ItemNotFoundException, RepositoryException, PathNotFoundException, ParserConfigurationException {
        Document document;
        Node contentNode = JCRUtil.getNodeById(content.getId(), content.getWorkspace());
        Node manifestNode = contentNode.getNode(JCRUtil.MANIFEST_PREFIX);
        document = new JCRXMLConverter().convertToDom(manifestNode);
        return document;
    }

    private void exportScormControlAndImageFiles(Content content, ZipOutputStream zipOutStream) {
        if (content.getContentTypeId().equals(ContentTypes.CHAPTERS)) {
            exportAdditionalFiles(content, zipOutStream);
        }
        List<String> filesEntries = (List<String>) CmsConfig.getExportedControlFilesZipEntries();
        String basePath = CmsConfig.getExportedControlFilesDirectoryPath();
        writeFilesToZip(filesEntries, basePath, content.getResourcesFolder(), zipOutStream);
    }

    private void removeUnnecesaryAttributes(Document document) {
        removeAttribute(document, "collectionClass");
    }

    /**
     * The order of manifest child elements is: metadata, organizations and resources.
     * @param document
     */
    private void reorganizeManifestChildElements(Document document) {
        putOrganizationsBeforeResources(document);
        putMetadataBeforeOrganizations(document);
    }

    private Element putOrganizationsBeforeResources(Document document) {
        Element manifestElement = (Element) document.getFirstChild();
        NodeList resourcesNodes = document.getElementsByTagName(IMSConstants.RESOURCES);
        assert (resourcesNodes.getLength() == 1) : "Manifest has to have exactly one resouces element";
        Element resourcesElement = (Element) resourcesNodes.item(0);
        NodeList organizationsNodes = document.getElementsByTagName(IMSConstants.ORGANIZATIONS);
        assert (organizationsNodes.getLength() == 1) : "Manifest has to have exactly one organizations element";
        Element organizationsElement = (Element) organizationsNodes.item(0);
        manifestElement.removeChild(organizationsElement);
        manifestElement.insertBefore(organizationsElement, resourcesElement);
        return organizationsElement;
    }

    private void putMetadataBeforeOrganizations(Document document) {
        Element mymanifestElement = (Element) document.getFirstChild();
        NodeList organizationsNodes = document.getElementsByTagName(IMSConstants.ORGANIZATIONS);
        assert (organizationsNodes.getLength() == 1) : "Manifest has to have exactly one organizations element";
        Element organizationsElement = (Element) organizationsNodes.item(0);
        NodeList metadataNodes = document.getElementsByTagName(IMSConstants.METADATA);
        assert (metadataNodes.getLength() <= 1) : "Manifest has to have zero or one metadata element";
        if (metadataNodes.getLength() == 1) {
            Element metadataElement = (Element) metadataNodes.item(0);
            mymanifestElement.removeChild(metadataElement);
            mymanifestElement.insertBefore(metadataElement, organizationsElement);
        }
    }

    private void convertStringLanguageValues(Document document, String workspace) {
        NodeList stringNodes = document.getElementsByTagName(IMSConstants.STRING);
        for (int i = 0; i < stringNodes.getLength(); i++) {
            Element stringElement = (Element) stringNodes.item(i);
            String originalLangValue = stringElement.getAttribute(JCRUtil.XML_LANG_PROPERTY);
            if (!StringUtils.isEmpty(originalLangValue)) {
                stringElement.removeAttribute(JCRUtil.XML_LANG_PROPERTY);
                stringElement.setAttribute(JCRUtil.XML_LANG_PROPERTY, originalLangValue.replaceAll("_", "-"));
            }
            org.w3c.dom.Node description = stringElement.getFirstChild();
            Source fieldHTML = new Source(description.getNodeValue());
            if ((fieldHTML.getAllStartTags(HTMLElementName.IMG).size() > 0) || (fieldHTML.getAllStartTags(HTMLElementName.A).size() > 0) || (fieldHTML.getAllStartTags(HTMLElementName.IFRAME).size() > 0)) {
                String fieldHTMLConverted = convertToLocalUrls(fieldHTML.toString(), null, "", IMSMANIFEST_FILE_NAME, workspace, null);
                description.setNodeValue(fieldHTMLConverted);
            }
        }
    }

    private void removeUnnecesaryNameSpacePrefixes(Document document) {
        List<String> tagNamesToSubstitute = new ArrayList<String>();
        tagNamesToSubstitute.add("collection:classification");
        for (String tagNameToSubstitue : tagNamesToSubstitute) {
            removeElementNameSpacePrefix(document, tagNameToSubstitue);
        }
    }

    private void removeElementNameSpacePrefix(Document document, String tagNameToSubstitue) {
        NodeList collectionElements = document.getElementsByTagNameNS(JCRUtil.COLLECTION_URI, "*");
        for (int i = 0; i < collectionElements.getLength(); i++) {
            org.w3c.dom.Node collectionNode = collectionElements.item(i);
            document.renameNode(collectionNode, "", collectionNode.getLocalName());
        }
    }

    private void removeAttribute(Document document, String attributeName) {
        NodeList elements = document.getElementsByTagName("*");
        for (int i = 0; i < elements.getLength(); i++) {
            org.w3c.dom.Node element = elements.item(i);
            NamedNodeMap attributes = element.getAttributes();
            if (attributes.getNamedItem(attributeName) != null) attributes.removeNamedItem(attributeName);
        }
    }

    private static void addLomNameSpace(Document document) {
        NodeList lomNodes = document.getElementsByTagName(IMSConstants.METADATA_LOM);
        for (int i = 0; i < lomNodes.getLength(); i++) {
            Element lomElement = (Element) lomNodes.item(i);
            if (StringUtils.isEmpty(lomElement.getAttribute(JCRUtil.XML_NAMESPACE))) {
                lomElement.setAttribute(JCRUtil.XML_NAMESPACE, JCRUtil.LOM_URI);
            }
        }
    }

    private void replaceIdentifiers(Document document) {
        prefixAttributeValueInElements(document, IMSConstants.ORGANIZATIONS, IMSConstants.IDENTIFIER, IDENTIFIER_PREFIX);
        prefixAttributeValueInElements(document, IMSConstants.ORGANIZATIONS, IMSConstants.DEFAULT, IDENTIFIER_PREFIX);
        prefixAttributeValueInElements(document, IMSConstants.ORGANIZATION, IMSConstants.IDENTIFIER, IDENTIFIER_PREFIX);
        prefixAttributeValueInElements(document, IMSConstants.RESOURCE, IMSConstants.IDENTIFIER, IDENTIFIER_PREFIX);
        prefixAttributeValueInElements(document, IMSConstants.ITEM, IMSConstants.IDENTIFIER_REF, IDENTIFIER_PREFIX);
    }

    private void prefixAttributeValueInElements(Document document, String elementName, String attributeName, String prefix) {
        NodeList elements = document.getElementsByTagName(elementName);
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            String oldAttributeValue = element.getAttribute(attributeName);
            if (StringUtils.isNotEmpty(oldAttributeValue)) {
                element.removeAttribute(attributeName);
                element.setAttribute(attributeName, prefix + oldAttributeValue);
            }
        }
    }

    private void addOrganizationIdentifiers(Document document) {
        NodeList items = document.getElementsByTagName(IMSConstants.ITEM);
        for (int i = 0; i < items.getLength(); i++) {
            Element itemElement = (Element) items.item(i);
            if (StringUtils.isEmpty(itemElement.getAttribute(IMSConstants.IDENTIFIER))) {
                itemElement.setAttribute(IMSConstants.IDENTIFIER, getNextOrganizationItemId().toString());
            }
        }
    }

    private void appendDefaultManifestAttributes(Element manifestDomNode) {
        manifestDomNode.setAttribute("identifier", "MANIFEST-Plugin-Technologies");
        manifestDomNode.setAttribute("version", "1.3");
        manifestDomNode.setAttribute("xmlns:collection", JCRUtil.COLLECTION_URI);
        manifestDomNode.setAttribute("xmlns:binary", JCRUtil.BINARY_URI);
        manifestDomNode.setAttribute("xmlns:cms", JCRUtil.CMS_URI);
        manifestDomNode.setAttribute("xmlns:adlcp", JCRUtil.ADLCP_URI);
        manifestDomNode.setAttribute("xmlns:lom", JCRUtil.LOM_URI);
        manifestDomNode.setAttribute("xmlns:xsi", JCRUtil.XSI_URI);
        manifestDomNode.setAttribute("xsi:schemaLocation", JCRUtil.IMSCP_URI + " imscp_v1p1.xsd" + "   " + JCRUtil.ADLCP_URI + " adlcp_v1p3.xsd" + "   " + JCRUtil.LOM_URI + " lom.xsd");
    }

    private Element getElement(Document document, String elementName) {
        NodeList resourcesDomNodes = document.getElementsByTagName(elementName);
        Element resourcesElement = null;
        for (int i = 0; i < resourcesDomNodes.getLength(); i++) {
            org.w3c.dom.Node resourcesNode = resourcesDomNodes.item(i);
            if (resourcesNode instanceof Element) {
                resourcesElement = (Element) resourcesNode;
                break;
            }
        }
        if (resourcesElement == null) {
            resourcesElement = document.createElement(elementName);
            org.w3c.dom.Node manifestNode = document.getFirstChild();
            manifestNode.appendChild(resourcesElement);
        }
        return resourcesElement;
    }

    private void createDefaultOrganizationsAndResources(Document document, String contentName, String firstResourceName) {
        Text titleText = document.createTextNode(contentName);
        Element title = document.createElement("title");
        title.appendChild(titleText);
        Text itemTitleText = document.createTextNode(contentName);
        Element itemTitle = document.createElement("title");
        itemTitle.appendChild(itemTitleText);
        Element item = document.createElement("item");
        item.setAttribute(IMSConstants.IDENTIFIER, "defaultItem");
        item.setAttribute(IMSConstants.IDENTIFIER_REF, "defaultResourceId");
        item.setAttribute(IMSConstants.IS_VISIBLE, "true");
        item.appendChild(itemTitle);
        Element organization = document.createElement("organization");
        organization.setAttribute(IMSConstants.IDENTIFIER, "defaultOrganization");
        organization.appendChild(title);
        organization.appendChild(item);
        getElement(document, IMSConstants.ORGANIZATIONS).appendChild(organization);
        Element file = document.createElement("file");
        file.setAttribute(IMSConstants.HREF, firstResourceName);
        Element resource = document.createElement(IMSConstants.RESOURCE);
        resource.setAttribute("adlcp:scormType", "sco");
        resource.setAttribute(HREF, firstResourceName);
        resource.setAttribute(IMSConstants.IDENTIFIER, "defaultResourceId");
        resource.setAttribute("type", "webcontent");
        resource.appendChild(file);
        getElement(document, "resources").appendChild(resource);
    }

    protected void convertToLocalPlayerTags(List<StartTag> startTags, OutputDocument outputDocument, ZipOutputStream zipOutStream, String contentResourcesFolderId, String workspace, ServletContext servletContext) {
        DirectoryPersister directoryPersister = ManagerRegistry.getDirectoryPersister();
        Session session = JCRUtil.currentSession(workspace);
        DirectoryFolder dFolder;
        DirectoryFile dFile;
        boolean playerIncluded = false;
        String attributeToChange;
        String videoValue;
        String videoSrcChanged;
        String tagName;
        int indexParams = -1;
        for (StartTag startTag : startTags) {
            tagName = startTag.getName();
            if (tagName.equals(HTMLElementName.OBJECT)) attributeToChange = DATA; else if (tagName.equals(HTMLElementName.PARAM)) attributeToChange = VALUE; else attributeToChange = SRC;
            videoValue = startTag.getAttributeValue(attributeToChange);
            if (videoValue != null) {
                videoSrcChanged = MEDIA_PLAYER_FILE_NAME;
                if (videoValue.contains("file=")) {
                    indexParams = videoValue.indexOf("file=");
                    String pathToMediaFile = videoValue.substring(indexParams + 5);
                    if (indexParams > 0) videoSrcChanged += "?file=" + convertReferencePath(pathToMediaFile, ASSOCIATED_FILES_PREFIX.length() + 37, null); else videoSrcChanged = "file=" + convertReferencePath(pathToMediaFile, ASSOCIATED_FILES_PREFIX.length() + 37, null);
                } else videoSrcChanged = convertReferencePath(videoValue, ASSOCIATED_FILES_PREFIX.length() + 37, null);
                if (videoValue.contains(ASSOCIATED_FILES_PREFIX)) {
                    String newStartTag = substituteAttributeValue(startTag, attributeToChange, videoSrcChanged);
                    outputDocument.replace(startTag, newStartTag);
                    if (videoValue.contains(MEDIA_PLAYER_FILE_ABS_PATH)) {
                        if (!playerIncluded && (zipOutStream != null)) {
                            try {
                                Node node = session.getNodeByUUID(contentResourcesFolderId);
                                dFolder = (DirectoryFolder) directoryPersister.getItemFromNode(node);
                                List<DirectoryFile> folderList = directoryPersister.listFiles(dFolder);
                                Iterator<DirectoryFile> folderIt = folderList.iterator();
                                while (folderIt.hasNext()) {
                                    dFile = (DirectoryFile) folderIt.next();
                                    if (dFile.getName().equals(MEDIA_PLAYER_FILE_NAME)) playerIncluded = true;
                                }
                            } catch (RepositoryException e) {
                                log.error(e.getMessage());
                                throw new CMSRuntimeException("Error accessing repository", e);
                            } catch (MalformedDirectoryItemException e) {
                                log.error(e.getMessage());
                                throw new CMSRuntimeException("Error accesing directory items in repository", e);
                            }
                            if ((!playerIncluded) && (attributeToChange.equals(DATA))) {
                                int lastIndexPlayerPath = videoValue.lastIndexOf(MEDIA_PLAYER_FILE_NAME) + MEDIA_PLAYER_FILE_NAME.length();
                                videoValue = videoValue.substring(0, lastIndexPlayerPath);
                                try {
                                    String playerPath = servletContext.getRealPath(MEDIA_PLAYER_FILE__REL_PATH);
                                    File playerFile = new File(playerPath);
                                    addFileToZip(playerFile, zipOutStream);
                                    playerIncluded = true;
                                } catch (FileNotFoundException fne) {
                                    log.debug("Media player file not found: " + fne);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private String convertReferencePath(String attributeValue, int contentPathLength, String filePathFromConvert) {
        int index = attributeValue.indexOf(ASSOCIATED_FILES_PREFIX);
        int lengthPrefix = ASSOCIATED_FILES_PREFIX.length() + index;
        String filePathToConvert = null;
        String contentPathPrefix = attributeValue.substring(lengthPrefix, index + contentPathLength - 1);
        try {
            UUID.fromString(contentPathPrefix);
            filePathToConvert = attributeValue.substring(index + ASSOCIATED_FILES_PREFIX.length() + 37);
            if (filePathFromConvert != null) {
                filePathToConvert = convertPathRelativeToFile(filePathToConvert, filePathFromConvert);
            }
        } catch (IllegalArgumentException iae) {
            log.debug("Malformed rootFolder UUID: " + iae);
            return null;
        }
        return filePathToConvert;
    }

    private String convertPathRelativeToFile(String filePathToConvert, String filePathFromConvert) {
        String dirFileTo = "";
        String dirFileFrom = "";
        String convertedRelativePath = "";
        int lastSlashTo = filePathToConvert.lastIndexOf("/");
        int lastSlashFrom = filePathFromConvert.lastIndexOf("/");
        String fileTo = (lastSlashTo == -1) ? filePathToConvert : filePathToConvert.substring(lastSlashTo + 1, filePathToConvert.length());
        if (lastSlashTo == -1) lastSlashTo = 0;
        if (lastSlashFrom == -1) lastSlashFrom = 0;
        String pathTo = filePathToConvert.substring(0, lastSlashTo);
        String pathFrom = filePathFromConvert.substring(0, lastSlashFrom);
        StringTokenizer stokTo = new StringTokenizer(pathTo, "/");
        StringTokenizer stokFrom = new StringTokenizer(pathFrom, "/");
        int totalFromTokens = stokFrom.countTokens();
        for (int fromTokenCounter = 0; fromTokenCounter < totalFromTokens; fromTokenCounter++) {
            dirFileFrom = stokFrom.nextToken();
            if ((!convertedRelativePath.contains("../")) && (stokTo.hasMoreTokens())) dirFileTo = stokTo.nextToken(); else dirFileTo = "";
            if (!dirFileFrom.equals(dirFileTo) || (convertedRelativePath.contains("../"))) {
                convertedRelativePath = convertedRelativePath.concat("../");
            } else dirFileTo = "";
        }
        if (!dirFileTo.equals("")) convertedRelativePath = convertedRelativePath.concat(new StringBuffer(dirFileTo).append("/").toString());
        while (stokTo.hasMoreTokens()) {
            dirFileTo = stokTo.nextToken();
            convertedRelativePath = convertedRelativePath.concat(new StringBuffer(dirFileTo).append("/").toString());
        }
        convertedRelativePath = convertedRelativePath.concat(fileTo);
        return convertedRelativePath;
    }

    protected void addReferencedFilesToZip(String fullPath, String folderId, ZipOutputStream zipOutStream, String workspace, ExportContentData exportContentData) {
        Session session = JCRUtil.currentSession(workspace);
        DirectoryPersister directoryPersister = ManagerRegistry.getDirectoryPersister();
        DirectoryFolder folder;
        try {
            StringTokenizer st = new StringTokenizer(fullPath, "/");
            int numberofPathElements = st.countTokens();
            while (numberofPathElements > 1) {
                String currenPathElement = st.nextToken();
                Node node = session.getNodeByUUID(folderId);
                folder = (DirectoryFolder) directoryPersister.getItemFromNode(node);
                List<DirectoryFolder> folderList = directoryPersister.listFolders(folder);
                for (DirectoryFolder currentFolder : folderList) {
                    if (currentFolder.getName().equals(currenPathElement)) {
                        folderId = currentFolder.getId();
                        break;
                    }
                }
                numberofPathElements--;
            }
            Node targetNode = session.getNodeByUUID(folderId);
            folder = (DirectoryFolder) directoryPersister.getItemFromNode(targetNode);
            List<DirectoryFile> fileList = directoryPersister.listFiles(folder);
            String fileName = st.nextToken();
            for (DirectoryFile currentFile : fileList) {
                if (currentFile.getName().equals(fileName)) {
                    addReferencedFilesToZip(currentFile, zipOutStream, fullPath.substring(0, fullPath.lastIndexOf('/') + 1), exportContentData);
                    break;
                }
            }
        } catch (RepositoryException e) {
            log.error(e.getMessage());
            throw new CMSRuntimeException("Error accessing repository", e);
        } catch (MalformedDirectoryItemException e) {
            log.error(e.getMessage());
            throw new CMSRuntimeException("Error accesing directory items in repository", e);
        }
    }

    protected void addReferencedFilesToZip(DirectoryFile file, ZipOutputStream zipOutStream, String path, ExportContentData exportContentData) {
        addDirectoryFileToZip(file, zipOutStream, path, null, exportContentData);
    }

    private void changeOrganizationItemReferences(Content content) throws ScormFormatException {
        if (log.isDebugEnabled()) log.debug("Changing organization item references");
        try {
            Node contentNode = JCRUtil.getNodeById(content.getId(), content.getWorkspace());
            String manifestPath = "/" + contentNode.getPath() + "/" + JCRUtil.MANIFEST_PREFIX;
            Node manifestNode = ContentNode.createContentNode(contentNode).getManifestNode();
            List<Node> organizationItems = getOrganizationItems(manifestPath, manifestNode);
            for (Node itemNode : organizationItems) {
                try {
                    String originalIdRef = originalIdRef(itemNode);
                    Node targetResourceNode = getResourceNodeFromIdentifierRef(manifestPath, itemNode, originalIdRef);
                    if (targetResourceNode != null) {
                        String relativeFilePath = targetResourceNode.getProperty(IMSConstants.HREF).getString();
                        DirectoryItem fileDirectoryItem = null;
                        try {
                            fileDirectoryItem = getDirectoryItem(content, URLDecoder.decode(relativeFilePath, "UTF-8"));
                        } catch (DirectoryItemNotFoundException e) {
                            log.error("Error getting directory item referenced by organization. File " + relativeFilePath + "was not found in repository.");
                            throw new ScormFormatException(e);
                        }
                        itemNode.setProperty(IMSConstants.IDENTIFIER_REF, fileDirectoryItem.getId());
                        if (log.isDebugEnabled()) log.debug("Item identifierref changed to: " + itemNode.getProperty(IMSConstants.IDENTIFIER_REF).getString());
                        targetResourceNode.setProperty(IMSConstants.IDENTIFIER, fileDirectoryItem.getId());
                        if (log.isDebugEnabled()) log.debug("Resource identifier changed to: " + targetResourceNode.getProperty(IMSConstants.IDENTIFIER).getString());
                    }
                } catch (UnsupportedEncodingException e) {
                    log.error("Error trying to change organization item references. Skipping organization item", e);
                } catch (RepositoryException e) {
                    log.error("Error trying to change organization item reference. Skipping organization item", e);
                }
            }
        } catch (RepositoryException e) {
            log.error("Error trying to change organization item references.", e);
            throw new CMSRuntimeException(e);
        } catch (MalformedContentException e) {
            throw new CMSRuntimeException(e);
        }
    }

    private Node getResourceNodeFromIdentifierRef(String manifestPath, Node itemNode, String originalIdRef) throws RepositoryException {
        if (originalIdRef == null) return null;
        Node resourceNode = null;
        NodeIterator resouceNode = getResources(manifestPath, originalIdRef, itemNode.getSession());
        if (resouceNode.hasNext()) {
            resourceNode = resouceNode.nextNode();
        }
        return resourceNode;
    }

    private static String originalIdRef(Node itemNode) throws ValueFormatException, RepositoryException {
        String originalIdRef = null;
        try {
            originalIdRef = itemNode.getProperty(IMSConstants.IDENTIFIER_REF).getString();
            if (log.isDebugEnabled()) log.debug("Processing organization item ref: " + originalIdRef);
        } catch (PathNotFoundException e) {
            log.error("This organization item[" + "] hasn't got identiferref property");
        }
        return originalIdRef;
    }

    private DirectoryItem getDirectoryItem(Content content, String relativeFilePath) throws DirectoryItemNotFoundException {
        DirectoryPersister directoryPersister = ManagerRegistry.getDirectoryPersister();
        DirectoryFolder resourcesFolder = content.getResourcesFolder();
        DirectoryItem fileDirectoryItem = directoryPersister.getItemFromPath(resourcesFolder.getURLPath() + "/" + relativeFilePath);
        return fileDirectoryItem;
    }

    private NodeIterator getResources(String manifestPath, String itemId, Session session) {
        String query = manifestPath + "/" + IMSConstants.RESOURCES + "/" + IMSConstants.RESOURCE + "[@" + IMSConstants.IDENTIFIER + "='" + itemId + "']";
        log.debug("Resources query: " + query);
        NodeIterator resourceNodesIter;
        try {
            resourceNodesIter = JCRUtil.searchByXPathQuery(query, session).getNodes();
        } catch (RepositoryException e) {
            String errorMessage = "Respository error trying to get resources with id " + itemId;
            log.error(errorMessage, e);
            throw new CMSRuntimeException(errorMessage, e);
        }
        log.debug("Resources found: " + resourceNodesIter.getSize());
        return resourceNodesIter;
    }

    private List<Node> getOrganizationItems(String manifestPath, Node manifestNode) throws PathNotFoundException, RepositoryException {
        Node organizationNode = manifestNode.getNode(IMSConstants.ORGANIZATIONS).getNode(IMSConstants.ORGANIZATION);
        return getOrganizationItemSubNodes(organizationNode);
    }

    private List<Node> getOrganizationItemSubNodes(Node organizationNode) throws RepositoryException, PathNotFoundException {
        List<Node> itemNodes = new ArrayList<Node>();
        NodeIterator nodes = organizationNode.getNodes(IMSConstants.ITEM);
        while (nodes.hasNext()) {
            Node itemNode = nodes.nextNode();
            itemNodes.add(itemNode);
            itemNodes.addAll(getOrganizationItemSubNodes(itemNode));
        }
        return itemNodes;
    }

    private synchronized String getNextOrganizationItemId() {
        return IDENTIFIER_PREFIX + new Long(nextOrganizationItemIdSuffix++);
    }
}
