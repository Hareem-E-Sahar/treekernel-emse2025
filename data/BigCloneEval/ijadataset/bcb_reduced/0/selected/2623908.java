package com.germinus.xpression.cms.educative;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
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
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import com.germinus.xpression.cms.CMSRuntimeException;
import com.germinus.xpression.cms.CmsConfig;
import com.germinus.xpression.cms.contents.CMSData;
import com.germinus.xpression.cms.contents.Content;
import com.germinus.xpression.cms.contents.ContentManager;
import com.germinus.xpression.cms.contents.DraftContent;
import com.germinus.xpression.cms.contents.JCRContentManager;
import com.germinus.xpression.cms.contents.MalformedContentException;
import com.germinus.xpression.cms.directory.DirectoryFile;
import com.germinus.xpression.cms.directory.DirectoryFolder;
import com.germinus.xpression.cms.directory.DirectoryItem;
import com.germinus.xpression.cms.directory.DirectoryItemNotFoundException;
import com.germinus.xpression.cms.directory.DirectoryPersister;
import com.germinus.xpression.cms.jcr.JCRUtil;
import com.germinus.xpression.cms.model.ContentTypes;
import com.germinus.xpression.cms.util.ManagerRegistry;
import com.germinus.xpression.cms.worlds.World;
import com.germinus.xpression.cms.worlds.WorldManager;
import com.germinus.xpression.menu.Organizations;
import com.germinus.xpression.menu.ToolMenu;
import com.germinus.xpression.menu.ToolMenuItem;

public class SCORMUtil {

    private static final String IDENTIFIER_PREFIX = "scribe-";

    public static final String DEFAULT_ORGANIZATION_RESOURCE_HREF = "index.html";

    static final String IMSMANIFEST_FILE_NAME = "imsmanifest.xml";

    private static long nextOrganizationItemIdSuffix = System.currentTimeMillis();

    private static Log log = LogFactory.getLog(SCORMUtil.class);

    public static Content importScormZip(String userId, String worldId, String contentType, String folderPath, String fileName, InputStream inputStream) throws ScormFormatException {
        DraftContent content = null;
        try {
            content = new DraftContent();
            content.setContentTypeId(ContentTypes.CHAPTERS);
            content.setAuthorId(userId);
            CMSData contentData = ContentTypes.createContentDataFor(ContentTypes.CHAPTERS);
            content.setContentData(contentData);
            DirectoryFolder targetFolder = getTargetFolder(folderPath);
            content = saveAsDraftContent(content, worldId, targetFolder);
            DirectoryFolder resourcesFolder = content.getResourcesFolder();
            addZipFileToResourcesFolder(inputStream, resourcesFolder, contentType, fileName);
            Document document = parseDirectoryFile(resourcesFolder.getURLPath() + "/" + IMSMANIFEST_FILE_NAME);
            removeDirectoryItem(resourcesFolder.getURLPath() + "/" + IMSMANIFEST_FILE_NAME);
            Node contentNode = JCRUtil.getNodeById(content.getId());
            removeCurrentManifestNode(contentNode);
            removePrefixes(document);
            addManifestToContentNode(document, contentNode);
            changeOrganizationItemReferences(content);
            convertStringLanguageValues(contentNode);
            contentNode.save();
            JCRContentManager jcrContentManager = (JCRContentManager) ManagerRegistry.getContentManager();
            jcrContentManager.removeFromCache(contentNode);
            Content contentById = jcrContentManager.getContentById(content.getId());
            addLomIfNotPresent(worldId, contentById);
            jcrContentManager.removeFromCache(JCRUtil.getNodeById(content.getId()));
            return contentById;
        } catch (SAXException e) {
            log.error("Error parsing manifest file.");
            throw new ScormFormatException(e);
        } catch (Exception e) {
            log.error("SCORM content " + fileName + " could not be " + "imported correctly in world " + worldId + " by user " + userId, e);
            throw new CMSRuntimeException(e);
        }
    }

    private static void removePrefixes(Document document) {
        NodeList elements = document.getElementsByTagName("*");
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            if (element.getPrefix() != null) {
                element.setPrefix(null);
                element.setAttribute("xmlns", element.getNamespaceURI());
            }
        }
    }

    private static Content addLomIfNotPresent(String worldId, Content contentToCheckLomPresence) throws MalformedContentException {
        JCRContentManager jcrContentManagerAux = (JCRContentManager) ManagerRegistry.getContentManager();
        if (contentToCheckLomPresence.getManifest().getMetadata().get(IMSConstants.METADATA_LOM) == null) {
            contentToCheckLomPresence.getManifest().getMetadata().set(IMSConstants.METADATA_LOM, new Lom());
            contentToCheckLomPresence = jcrContentManagerAux.updateContent(contentToCheckLomPresence, ManagerRegistry.getWorldManager().getWorldById(worldId));
        }
        return contentToCheckLomPresence;
    }

    private static void convertStringLanguageValues(Node contentNode) {
        try {
            String stringNodesWitLanguagePath = "/" + contentNode.getPath() + "/" + JCRUtil.MANIFEST_PREFIX + "//" + IMSConstants.STRING + "[@" + JCRUtil.XML_LANG_PROPERTY + "]";
            NodeIterator stringNodesWithLanguage = JCRUtil.searchByXPathQuery(stringNodesWitLanguagePath, JCRUtil.currentSession()).getNodes();
            while (stringNodesWithLanguage.hasNext()) {
                Node node = stringNodesWithLanguage.nextNode();
                Property languageProperty = node.getProperty(JCRUtil.XML_LANG_PROPERTY);
                languageProperty.setValue(languageProperty.getString().replaceAll("-", "_"));
            }
        } catch (RepositoryException e) {
            String errorMessage = "Error converting language values in string nodes";
            log.error(errorMessage, e);
            throw new CMSRuntimeException(errorMessage, e);
        }
    }

    private static void addManifestToContentNode(Document document, Node contentNode) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException, AccessDeniedException, InvalidItemStateException, ReferentialIntegrityException, NoSuchNodeTypeException {
        if (log.isDebugEnabled()) log.debug("Adding manifest to content node.");
        NodeList documentChildNodes = document.getChildNodes();
        JCRXMLConverter converter = new JCRXMLConverter();
        for (int i = 0; i < documentChildNodes.getLength(); i++) {
            log.debug("Adding node to JCR: " + documentChildNodes.item(i).getNodeName());
            converter.addDomNodeToJcrNode(documentChildNodes.item(i), contentNode);
        }
        contentNode.save();
    }

    private static void removeCurrentManifestNode(Node contentNode) throws PathNotFoundException, RepositoryException, VersionException, LockException, ConstraintViolationException, AccessDeniedException, ItemExistsException, InvalidItemStateException, ReferentialIntegrityException, NoSuchNodeTypeException {
        if (log.isDebugEnabled()) log.debug("Removing current manifest node.");
        Node manifestNode = contentNode.getNode(JCRUtil.MANIFEST_PREFIX);
        manifestNode.remove();
        contentNode.save();
    }

    private static void removeDirectoryItem(String path) throws DirectoryItemNotFoundException {
        if (log.isDebugEnabled()) log.debug("Removing file from directory: " + path);
        DirectoryPersister directoryPersister = ManagerRegistry.getDirectoryPersister();
        DirectoryFile manifestFile1 = (DirectoryFile) directoryPersister.getItemFromPath(path);
        directoryPersister.deleteItem(manifestFile1);
    }

    private static Document parseDirectoryFile(String path) throws DirectoryItemNotFoundException, SAXException, IOException {
        if (log.isDebugEnabled()) log.debug("Parsing directory file.");
        DirectoryPersister directoryPersister = ManagerRegistry.getDirectoryPersister();
        DirectoryFile manifestFile = (DirectoryFile) directoryPersister.getItemFromPath(path);
        InputStream manifestInputStream = directoryPersister.getInputStreamFromUuid(manifestFile.getId());
        Document document = JCRXMLConverter.parseDom(manifestInputStream);
        manifestInputStream.close();
        return document;
    }

    private static void addZipFileToResourcesFolder(InputStream inputStream, DirectoryFolder resourcesFolder, String contentType, String fileName) {
        if (log.isDebugEnabled()) log.debug("Adding zip file to resources folder");
        DirectoryPersister directoryPersister = ManagerRegistry.getDirectoryPersister();
        DirectoryFile itemFile = new DirectoryFile(fileName);
        itemFile.setMimeType(contentType);
        directoryPersister.addZipFile(resourcesFolder, itemFile, inputStream);
    }

    private static DraftContent saveAsDraftContent(DraftContent content, String worldId, DirectoryFolder targetFolder) throws MalformedContentException {
        ContentManager contentManager1 = ManagerRegistry.getContentManager();
        WorldManager worldManager = ManagerRegistry.getWorldManager();
        World world = worldManager.getWorldById(worldId);
        content = contentManager1.saveAsDraftContent(content, world, targetFolder);
        return content;
    }

    private static DirectoryFolder getTargetFolder(String folderPath) throws DirectoryItemNotFoundException {
        DirectoryPersister directoryPersister1 = ManagerRegistry.getDirectoryPersister();
        return (DirectoryFolder) directoryPersister1.getItemFromPath(folderPath);
    }

    public static void exportScormZip(Content content, ZipOutputStream zipOutStream) {
        try {
            zipOutStream.putNextEntry(new ZipEntry(IMSMANIFEST_FILE_NAME));
            JCRXMLConverter converter = new JCRXMLConverter();
            Node contentNode = JCRUtil.getNodeById(content.getId());
            Node manifestNode = contentNode.getNode(JCRUtil.MANIFEST_PREFIX);
            Document document = converter.convertToDom(manifestNode);
            Element manifestDomElement = (Element) document.getFirstChild();
            appendDefaultManifestAttributes(manifestDomElement);
            Organizations organizations = content.getManifest().getOrganizations();
            if (organizations.size() == 0) {
                createDefaultOrganizationsAndResources(document, content.getName());
            } else {
                addOrganizationIdentifiers(document);
            }
            Element resourcesElement = getElement(document, IMSConstants.RESOURCES);
            if (!resourcesElement.hasChildNodes()) {
                addResourcesFromOrganizations(content, document, organizations, resourcesElement);
            }
            replaceIdentifiers(document);
            addLomNameSpace(document);
            convertStringLanguageValues(document);
            removeUnnecesaryAttributes(document);
            reorganizeManifestChildElements(document);
            JCRXMLConverter.writeXmlDocument(document, zipOutStream);
            zipOutStream.closeEntry();
            DirectoryFolder resourcesFolder = content.getResourcesFolder();
            addDirectoryItemsToZip(resourcesFolder, "", zipOutStream);
            addScormDocumentControlFiles(zipOutStream);
        } catch (ItemNotFoundException e) {
            String errorMessage = "There is no jcr node with the id of the content: " + content.getId();
            log.error(errorMessage, e);
            throw new CMSRuntimeException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = "Error exporting to scorm zip the content " + content.getName() + " with id " + content.getId();
            log.error(errorMessage);
            throw new CMSRuntimeException(errorMessage, e);
        }
    }

    private static void removeUnnecesaryAttributes(Document document) {
        removeAttribute(document, "collectionClass");
    }

    /**
     * The order of manifest child elements is: metadata, organizations and resources.
     * @param document
     */
    private static void reorganizeManifestChildElements(Document document) {
        putOrganizationsBeforeResources(document);
        putMetadataBeforeOrganizations(document);
    }

    private static Element putOrganizationsBeforeResources(Document document) {
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

    private static void putMetadataBeforeOrganizations(Document document) {
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

    private static void convertStringLanguageValues(Document document) {
        NodeList stringNodes = document.getElementsByTagName(IMSConstants.STRING);
        for (int i = 0; i < stringNodes.getLength(); i++) {
            Element stringElement = (Element) stringNodes.item(i);
            String originalLangValue = stringElement.getAttribute(JCRUtil.XML_LANG_PROPERTY);
            if (!StringUtils.isEmpty(originalLangValue)) {
                stringElement.removeAttribute(JCRUtil.XML_LANG_PROPERTY);
                stringElement.setAttribute(JCRUtil.XML_LANG_PROPERTY, originalLangValue.replaceAll("_", "-"));
            }
        }
    }

    private static void removeAttribute(Document document, String attributeName) {
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

    private static void addScormDocumentControlFiles(ZipOutputStream zipOutStream) {
        List<String> scormControlFilesZipEntriesNames = CmsConfig.getScormControlFilesZipEntries();
        for (String scormControlFilezipEntryName : scormControlFilesZipEntriesNames) {
            ZipEntry zipEntry = new ZipEntry(scormControlFilezipEntryName);
            String resourceName = null;
            try {
                zipOutStream.putNextEntry(zipEntry);
                if (!scormControlFilezipEntryName.endsWith("/")) {
                    resourceName = CmsConfig.getScormControlFilesDirectoryPath() + scormControlFilezipEntryName;
                    BufferedInputStream inputStream = new BufferedInputStream(SCORMUtil.class.getResourceAsStream(resourceName));
                    writeInputStreamIntoOutputStream(inputStream, zipOutStream);
                }
                zipOutStream.closeEntry();
            } catch (ZipException e) {
                String warningMessage = "ZipException adding control files to exported SCORM zip";
                log.warn(warningMessage, e);
            } catch (IOException e) {
                String errorMessage = "Error adding scorm control file " + resourceName;
                log.error(errorMessage, e);
                throw new CMSRuntimeException(errorMessage, e);
            }
        }
    }

    private static void writeInputStreamIntoOutputStream(InputStream inStream, ZipOutputStream outStream) throws IOException {
        int len;
        byte[] buf = new byte[1024];
        while ((len = inStream.read(buf)) > 0) {
            outStream.write(buf, 0, len);
        }
    }

    private static void replaceIdentifiers(Document document) {
        prefixAttributeValueInElements(document, IMSConstants.ORGANIZATIONS, IMSConstants.IDENTIFIER, IDENTIFIER_PREFIX);
        prefixAttributeValueInElements(document, IMSConstants.ORGANIZATIONS, IMSConstants.DEFAULT, IDENTIFIER_PREFIX);
        prefixAttributeValueInElements(document, IMSConstants.ORGANIZATION, IMSConstants.IDENTIFIER, IDENTIFIER_PREFIX);
        prefixAttributeValueInElements(document, IMSConstants.RESOURCE, IMSConstants.IDENTIFIER, IDENTIFIER_PREFIX);
        prefixAttributeValueInElements(document, IMSConstants.ITEM, IMSConstants.IDENTIFIER_REF, IDENTIFIER_PREFIX);
    }

    private static void prefixAttributeValueInElements(Document document, String elementName, String attributeName, String prefix) {
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

    private static void addOrganizationIdentifiers(Document document) {
        NodeList items = document.getElementsByTagName(IMSConstants.ITEM);
        for (int i = 0; i < items.getLength(); i++) {
            Element itemElement = (Element) items.item(i);
            if (StringUtils.isEmpty(itemElement.getAttribute(IMSConstants.IDENTIFIER))) {
                itemElement.setAttribute(IMSConstants.IDENTIFIER, getNextOrganizationItemId().toString());
            }
        }
    }

    private static void appendDefaultManifestAttributes(Element manifestDomNode) {
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

    /**
	 * Add resources dom nodes from organizations DOM nodes.
	 */
    private static void addResourcesFromOrganizations(Content content, Document document, Organizations organizations, Element resourcesElement) {
        Iterator organizationsIter = organizations.iterator();
        while (organizationsIter.hasNext()) {
            ToolMenu organization = (ToolMenu) organizationsIter.next();
            List toolMenuItems = organization.getChildren();
            addItemsToResources(toolMenuItems, content, document, resourcesElement);
        }
        org.w3c.dom.Node manifestDomNode = document.getFirstChild();
        manifestDomNode.appendChild(resourcesElement);
    }

    private static void addItemsToResources(List toolMenuItems, Content content, Document document, Element resourcesElement) {
        Iterator itemsIterator = toolMenuItems.iterator();
        while (itemsIterator.hasNext()) {
            ToolMenuItem item = (ToolMenuItem) itemsIterator.next();
            addItemToResources(item, content, document, resourcesElement);
            addItemsToResources(item.getChildren(), content, document, resourcesElement);
        }
    }

    private static void addItemToResources(ToolMenuItem item, Content content, Document document, Element resourcesElement) {
        String resourceId = item.getLink();
        DirectoryItem resourceItem = ManagerRegistry.getDirectoryPersister().getItemByUUID(resourceId);
        Element resourceElement = document.createElement("resource");
        resourceElement.setAttribute("adlcp:scormType", "sco");
        resourceElement.setAttribute("href", getDirectoryItemRelativePath(content, resourceItem));
        resourceElement.setAttribute("identifier", resourceId);
        resourceElement.setAttribute("type", "webcontent");
        resourcesElement.appendChild(resourceElement);
    }

    private static String getDirectoryItemRelativePath(Content content, DirectoryItem directoryItem) {
        String resourceItemPath = directoryItem.getURLPath();
        return resourceItemPath.substring(content.getResourcesFolder().getURLPath().length() + 1);
    }

    private static Element getElement(Document document, String elementName) {
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

    private static void createDefaultOrganizationsAndResources(Document document, String contentName) {
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
        file.setAttribute(IMSConstants.HREF, DEFAULT_ORGANIZATION_RESOURCE_HREF);
        Element resource = document.createElement(IMSConstants.RESOURCE);
        resource.setAttribute("adlcp:scormType", "sco");
        resource.setAttribute("href", DEFAULT_ORGANIZATION_RESOURCE_HREF);
        resource.setAttribute(IMSConstants.IDENTIFIER, "defaultResourceId");
        resource.setAttribute("type", "webcontent");
        resource.appendChild(file);
        getElement(document, "resources").appendChild(resource);
    }

    private static World calculateWorld(DirectoryFolder folder) {
        return ManagerRegistry.getWorldManager().getOwnerWorld(folder);
    }

    private static void addDirectoryItemsToZip(DirectoryFolder folder, String path, ZipOutputStream zipOutStream) {
        DirectoryPersister directoryPersister = ManagerRegistry.getDirectoryPersister();
        List childDirectoryItems = directoryPersister.listItems(folder);
        Iterator itemsIterator = childDirectoryItems.iterator();
        while (itemsIterator.hasNext()) {
            DirectoryItem item = (DirectoryItem) itemsIterator.next();
            log.debug(folder.getName() + " newPath: " + path);
            addDirectoryItemToZip(zipOutStream, item.getId(), path);
        }
    }

    private static void addDirectoryItemToZip(ZipOutputStream zipOutStream, String uuid, String previousPath) {
        DirectoryPersister directoryPersister = ManagerRegistry.getDirectoryPersister();
        DirectoryItem directoryItem = directoryPersister.getItemByUUID(uuid);
        directoryItem.getType();
        if (DirectoryItem.DIRECTORY_ITEM_FOLDER_TYPE.equals(directoryItem.getType())) {
            DirectoryFolder folder = (DirectoryFolder) directoryItem;
            log.debug("Item Directory " + folder.getName() + " previousPath: " + previousPath);
            addDirectoryFolderToZip(zipOutStream, folder, previousPath);
        } else if (DirectoryItem.DIRECTORY_ITEM_FILE_TYPE.equals(directoryItem.getType())) {
            DirectoryFile file = (DirectoryFile) directoryItem;
            addDirectoryFileToZip(file, zipOutStream, previousPath);
        }
    }

    private static void addDirectoryFileToZip(DirectoryFile file, ZipOutputStream zipOutStream, String previousPath) {
        log.debug("File " + file.getName() + " previousPath: " + previousPath);
        DirectoryPersister directoryPersister = ManagerRegistry.getDirectoryPersister();
        BufferedInputStream in = new BufferedInputStream(directoryPersister.getInputStreamFromUuid(file.getId()));
        ZipEntry zipEntry = new ZipEntry(previousPath + file.getName());
        try {
            zipOutStream.putNextEntry(zipEntry);
            writeInputStreamIntoOutputStream(in, zipOutStream);
            zipOutStream.closeEntry();
        } catch (IOException e) {
            String errorMessage = "Error adding file " + previousPath + file.getName();
            log.error(errorMessage, e);
            throw new CMSRuntimeException(errorMessage, e);
        }
    }

    public static void addReferencedFilesToZip(DirectoryFile file, ZipOutputStream zipOutStream, String path) {
        addDirectoryFileToZip(file, zipOutStream, path);
    }

    private static void addDirectoryFolderToZip(ZipOutputStream zipOutStream, DirectoryFolder folder, String previousPath) {
        log.debug("Directory " + folder.getName() + " previousPath: " + previousPath);
        ZipEntry zipEntry = new ZipEntry(previousPath + folder.getName() + '/');
        try {
            zipOutStream.putNextEntry(zipEntry);
            zipOutStream.closeEntry();
            log.debug("Processing item: " + previousPath + folder.getName() + '/');
            addDirectoryItemsToZip(folder, previousPath + folder.getName() + "/", zipOutStream);
        } catch (IOException e) {
            String errorMessage = "Error adding folder " + previousPath + "/" + folder.getName() + " to zip.";
            log.error(errorMessage, e);
            throw new CMSRuntimeException(errorMessage, e);
        }
    }

    private static void changeOrganizationItemReferences(Content content) throws ScormFormatException {
        if (log.isDebugEnabled()) log.debug("Changing organization item references");
        try {
            Node contentNode = JCRUtil.getNodeById(content.getId());
            String manifestPath = "/" + contentNode.getPath() + "/" + JCRUtil.MANIFEST_PREFIX;
            NodeIterator itemNodesIter = getOrganizationItems(manifestPath);
            while (itemNodesIter.hasNext()) {
                Node itemNode = (Node) itemNodesIter.next();
                String originalIdRef;
                originalIdRef = itemNode.getProperty(IMSConstants.IDENTIFIER_REF).getString();
                log.debug("Processing organization item ref: " + originalIdRef);
                NodeIterator resourceNodesIter = getResources(manifestPath, originalIdRef);
                if (resourceNodesIter.hasNext()) {
                    Node resourceNode = resourceNodesIter.nextNode();
                    String relativeFilePath = resourceNode.getProperty(IMSConstants.HREF).getString();
                    DirectoryItem fileDirectoryItem = null;
                    try {
                        fileDirectoryItem = getDirectoryItem(content, URLDecoder.decode(relativeFilePath, "UTF-8"));
                    } catch (DirectoryItemNotFoundException e) {
                        log.error("Error getting directory item referenced by organization. File " + relativeFilePath + "was not found in repository.");
                        throw new ScormFormatException(e);
                    }
                    itemNode.setProperty(IMSConstants.IDENTIFIER_REF, fileDirectoryItem.getId());
                    if (log.isDebugEnabled()) log.debug("Item identifierref changed to: " + itemNode.getProperty(IMSConstants.IDENTIFIER_REF).getString());
                    resourceNode.setProperty(IMSConstants.IDENTIFIER, fileDirectoryItem.getId());
                    if (log.isDebugEnabled()) log.debug("Resource identifier changed to: " + resourceNode.getProperty(IMSConstants.IDENTIFIER).getString());
                }
            }
        } catch (UnsupportedEncodingException e) {
            log.error("Error trying to change organization item references. Content with id " + content.getId() + " was not found.", e);
            throw new CMSRuntimeException(e);
        } catch (RepositoryException e) {
            log.error("Error trying to change organization item references. Content with id " + content.getId() + " was not found.", e);
            throw new CMSRuntimeException(e);
        }
    }

    private static DirectoryItem getDirectoryItem(Content content, String relativeFilePath) throws DirectoryItemNotFoundException {
        DirectoryPersister directoryPersister = ManagerRegistry.getDirectoryPersister();
        DirectoryFolder resourcesFolder = content.getResourcesFolder();
        DirectoryItem fileDirectoryItem = directoryPersister.getItemFromPath(resourcesFolder.getURLPath() + "/" + relativeFilePath);
        return fileDirectoryItem;
    }

    private static NodeIterator getResources(String manifestPath, String itemId) {
        String query = manifestPath + "/" + IMSConstants.RESOURCES + "/" + IMSConstants.RESOURCE + "[@" + IMSConstants.IDENTIFIER + "='" + itemId + "']";
        log.debug("Resources query: " + query);
        NodeIterator resourceNodesIter;
        try {
            resourceNodesIter = JCRUtil.searchByXPathQuery(query, JCRUtil.currentSession()).getNodes();
        } catch (RepositoryException e) {
            String errorMessage = "Respository error trying to get resources with id " + itemId;
            log.error(errorMessage, e);
            throw new CMSRuntimeException(errorMessage, e);
        }
        log.debug("Resources found: " + resourceNodesIter.getSize());
        return resourceNodesIter;
    }

    private static NodeIterator getOrganizationItems(String manifestPath) {
        String itemPath = manifestPath + "/" + IMSConstants.ORGANIZATIONS + "//" + IMSConstants.ITEM;
        log.debug("XPATH to search items: " + itemPath);
        NodeIterator itemNodesIter = null;
        try {
            itemNodesIter = JCRUtil.searchByXPathQuery(itemPath, JCRUtil.currentSession()).getNodes();
        } catch (RepositoryException e) {
            String errorMessage = "Repository error getting organization items with path " + itemPath;
            log.error(errorMessage, e);
            throw new CMSRuntimeException(errorMessage, e);
        }
        log.debug("Items found:" + itemNodesIter.getSize());
        return itemNodesIter;
    }

    private static synchronized String getNextOrganizationItemId() {
        return IDENTIFIER_PREFIX + new Long(nextOrganizationItemIdSuffix++);
    }
}
