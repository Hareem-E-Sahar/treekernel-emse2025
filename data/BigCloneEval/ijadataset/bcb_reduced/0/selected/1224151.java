package com.germinus.xpression.cms.educative;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletContext;
import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.germinus.xpression.cms.CMSRuntimeException;
import com.germinus.xpression.cms.CmsConfig;
import com.germinus.xpression.cms.contents.Content;
import com.germinus.xpression.cms.directory.DirectoryFile;
import com.germinus.xpression.cms.directory.DirectoryFolder;
import com.germinus.xpression.cms.directory.DirectoryItem;
import com.germinus.xpression.cms.directory.DirectoryItemNotFoundException;
import com.germinus.xpression.cms.directory.DirectoryPersister;
import com.germinus.xpression.cms.directory.MalformedDirectoryItemException;
import com.germinus.xpression.cms.jcr.JCRUtil;
import com.germinus.xpression.cms.model.ContentTypes;
import com.germinus.xpression.cms.util.ManagerRegistry;
import com.germinus.xpression.menu.Organizations;
import com.germinus.xpression.menu.ToolMenu;
import com.germinus.xpression.menu.ToolMenuItem;

public abstract class ImportExportFileUtil {

    static final String EMBED = "embed";

    static final String HREF = "href";

    static final String SRC = "src";

    static final String DATA = "data";

    static final String VALUE = "value";

    static final String ASSOCIATED_FILES_PREFIX = "/cms_tools/files/";

    static final String MEDIA_PLAYER_FILE_ABS_PATH = "/cms_tools/html/controls/media_player/player.swf";

    static final String MEDIA_PLAYER_FILE__REL_PATH = "html/controls/media_player/player.swf";

    static final String MEDIA_PLAYER_FILE_NAME = "player.swf";

    static final String IDENTIFIER_PREFIX = "scribe-";

    static final String ASSOCIATED_IMAGES_PREFIX = "/images/";

    private static Log log = LogFactory.getLog(ImportExportFileUtil.class);

    public abstract String exportContentToZip(ExportContentData exportContentData, ZipOutputStream zipOutStream);

    abstract InputStream getInputStreamFromPath(DirectoryFile file) throws MalformedDirectoryItemException;

    /**
	 * Add resources dom nodes from organizations DOM nodes.
	 */
    Set<String> addResourcesFromOrganizations(Content content, Document document, Organizations organizations, Element resourcesElement) {
        HashSet<String> includedChapters = new HashSet<String>();
        Iterator<ToolMenu> organizationsIter = organizations.iterator();
        while (organizationsIter.hasNext()) {
            ToolMenu organization = organizationsIter.next();
            List<ToolMenuItem> toolMenuItems = organization.getChildren();
            try {
                includedChapters.addAll(addItemsToResources(toolMenuItems, content, document, resourcesElement));
            } catch (DirectoryItemNotFoundException e) {
                log.error("Tool menu Item " + e.getFilePath() + " not found");
            }
        }
        if (document != null) {
            Node manifestDomNode = document.getFirstChild();
            manifestDomNode.appendChild(resourcesElement);
        }
        return includedChapters;
    }

    private Set<String> addItemsToResources(List<ToolMenuItem> toolMenuItems, Content content, Document document, Element resourcesElement) throws DirectoryItemNotFoundException {
        Set<String> includedChapters = new HashSet<String>();
        String chapterUrlPath = null;
        Iterator<ToolMenuItem> itemsIterator = toolMenuItems.iterator();
        while (itemsIterator.hasNext()) {
            ToolMenuItem item = itemsIterator.next();
            chapterUrlPath = addItemToResources(item, content, document, resourcesElement);
            if (chapterUrlPath.endsWith(".html")) includedChapters.add(chapterUrlPath);
            if (document != null) includedChapters.addAll(addItemsToResources(item.getChildren(), content, document, resourcesElement));
        }
        return includedChapters;
    }

    private String addItemToResources(ToolMenuItem item, Content content, Document document, Element resourcesElement) throws DirectoryItemNotFoundException {
        String resourceId = item.getLink();
        String chapterURLPath = null;
        DirectoryItem resourceItem = null;
        try {
            resourceItem = ManagerRegistry.getDirectoryPersister().getItemByUUIDWorkspace(resourceId, null);
            if (document != null) {
                Element resourceElement = document.createElement("resource");
                resourceElement.setAttribute(HREF, getDirectoryItemRelativePath(content, resourceItem));
                resourceElement.setAttribute("identifier", resourceId);
                resourceElement.setAttribute("type", "webcontent");
                resourcesElement.appendChild(resourceElement);
            }
        } catch (MalformedDirectoryItemException e) {
            log.error("Cannot add item to resources. Item " + resourceId + " is malformed");
        }
        if (ContentTypes.CHAPTERS.equals(content.getContentTypeId()) || ContentTypes.TALE.equals(content.getContentTypeId())) {
            chapterURLPath = resourceItem.getURLPath();
        }
        return chapterURLPath;
    }

    String getDirectoryItemRelativePath(Content content, DirectoryItem directoryItem) {
        String resourceItemPath = directoryItem.getURLPath();
        return resourceItemPath.substring(content.getResourcesFolder().getURLPath().length() + 1);
    }

    void addDirectoryItemsToZip(DirectoryFolder folder, String path, ZipOutputStream zipOutStream, Set<String> includedChapters, ExportContentData exportContentData, List<String> itemsToExclude) {
        DirectoryPersister directoryPersister = ManagerRegistry.getDirectoryPersister();
        List<DirectoryItem> childDirectoryItems = directoryPersister.listItems(folder);
        Iterator<DirectoryItem> itemsIterator = childDirectoryItems.iterator();
        while (itemsIterator.hasNext()) {
            DirectoryItem item = (DirectoryItem) itemsIterator.next();
            String filePath = path + item.getName();
            if (!itemsToExclude.contains(filePath)) {
                log.debug(folder.getName() + " newPath: " + path);
                try {
                    addDirectoryItemToZip(zipOutStream, item, path, includedChapters, exportContentData);
                } catch (DirectoryItemNotFoundException e) {
                    throw new CMSRuntimeException("Unexpected file not found " + e.getFilePath());
                }
            }
        }
    }

    private void addDirectoryItemToZip(ZipOutputStream zipOutStream, DirectoryItem directoryItem, String previousPath, Set<String> includedChapters, ExportContentData exportContentData) throws DirectoryItemNotFoundException {
        if (DirectoryItem.DIRECTORY_ITEM_FOLDER_TYPE.equals(directoryItem.getType())) {
            DirectoryFolder folder = (DirectoryFolder) directoryItem;
            if (log.isDebugEnabled()) {
                log.debug("Item Directory " + folder.getName() + " previousPath: " + previousPath);
            }
            addDirectoryFolderToZip(zipOutStream, folder, previousPath, includedChapters, exportContentData);
        } else if (DirectoryItem.DIRECTORY_ITEM_FILE_TYPE.equals(directoryItem.getType())) {
            DirectoryFile file = (DirectoryFile) directoryItem;
            String htmlPath = (includedChapters.contains(file.getURLPath())) ? file.getURLPath() : null;
            addDirectoryFileToZip(file, zipOutStream, previousPath, htmlPath, exportContentData);
        }
    }

    private void addDirectoryFolderToZip(ZipOutputStream zipOutStream, DirectoryFolder folder, String previousPath, Set<String> includedChapters, ExportContentData exportContentData) {
        log.debug("Directory " + folder.getName() + " previousPath: " + previousPath);
        ZipEntry zipEntry = new ZipEntry(previousPath + folder.getName() + '/');
        try {
            zipOutStream.putNextEntry(zipEntry);
            zipOutStream.closeEntry();
            log.debug("Processing item: " + previousPath + folder.getName() + '/');
            addDirectoryItemsToZip(folder, previousPath + folder.getName() + "/", zipOutStream, includedChapters, exportContentData, new ArrayList<String>());
        } catch (IOException e) {
            String errorMessage = "Error adding folder " + previousPath + "/" + folder.getName() + " to zip.";
            log.error(errorMessage, e);
            throw new CMSRuntimeException(errorMessage, e);
        }
    }

    void addFileToZip(File file, ZipOutputStream zipOutStream) throws FileNotFoundException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        ZipEntry zipEntry = new ZipEntry(file.getName());
        try {
            try {
                zipOutStream.putNextEntry(zipEntry);
            } catch (ZipException e) {
                log.warn("File " + file.getName() + " is already at the zip. Ignored");
                zipOutStream.closeEntry();
                return;
            }
            writeInputStreamIntoOutputStream(in, zipOutStream);
        } catch (IOException e) {
            String errorMessage = "Error adding file " + file.getName();
            log.error(errorMessage, e);
            throw new CMSRuntimeException(errorMessage, e);
        }
    }

    static void writeInputStreamIntoOutputStream(InputStream inStream, ZipOutputStream outStream) throws IOException {
        int len;
        byte[] buf = new byte[1024];
        while ((len = inStream.read(buf)) > 0) {
            outStream.write(buf, 0, len);
        }
    }

    String writeInputStreamIntoString(InputStream inStream) throws IOException {
        StringBuffer bufferRead = new StringBuffer();
        int len;
        byte[] buf = new byte[1024];
        while ((len = inStream.read(buf)) > 0) {
            bufferRead.append(new String(buf, 0, len));
        }
        return bufferRead.toString();
    }

    void addDirectoryFileToZip(DirectoryFile file, ZipOutputStream zipOutStream, String previousPath, String filePathtoConverTags, ExportContentData exportContentData) {
        log.debug("File " + file.getName() + " previousPath: " + previousPath);
        try {
            BufferedInputStream in = new BufferedInputStream(getInputStreamFromPath(file));
            ZipEntry zipEntry = new ZipEntry(previousPath + file.getName());
            try {
                try {
                    zipOutStream.putNextEntry(zipEntry);
                } catch (ZipException e) {
                    log.warn("File " + file.getName() + " is already at the zip. Ignored");
                    zipOutStream.closeEntry();
                    return;
                }
                if (filePathtoConverTags != null) {
                    String chapterHTMLContent = writeInputStreamIntoString(in);
                    String contentResourcesFolderId = filePathtoConverTags.substring(0, 36);
                    String relativeChapterPath = filePathtoConverTags.substring(37, filePathtoConverTags.length());
                    String convertedChapterContent = convertToLocalUrls(chapterHTMLContent, zipOutStream, contentResourcesFolderId, relativeChapterPath, file.getWorkspace(), exportContentData);
                    in = new BufferedInputStream(new ByteArrayInputStream(convertedChapterContent.getBytes()));
                }
                writeInputStreamIntoOutputStream(in, zipOutStream);
                zipOutStream.closeEntry();
            } catch (IOException e) {
                String errorMessage = "Error adding file " + previousPath + file.getName();
                log.error(errorMessage, e);
                throw new CMSRuntimeException(errorMessage, e);
            }
        } catch (MalformedDirectoryItemException e) {
            log.error("Cannot add file to zip. Item " + file.getId() + " is malformed");
        }
    }

    @SuppressWarnings("unchecked")
    public String convertToLocalUrls(String htmlAsString, ZipOutputStream zipOutStream, String contentResourcesFolderId, String filePathtoConverTags, String workspace, ExportContentData exportContentData) {
        Source sourceDocument = new Source(htmlAsString);
        OutputDocument outputDocument = new OutputDocument(sourceDocument);
        convertToLocalUrlTags((List<StartTag>) sourceDocument.getAllStartTags(HTMLElementName.IMG), SRC, outputDocument, zipOutStream, contentResourcesFolderId, filePathtoConverTags, workspace, exportContentData);
        convertToLocalUrlTags((List<StartTag>) sourceDocument.getAllStartTags(HTMLElementName.A), HREF, outputDocument, zipOutStream, contentResourcesFolderId, filePathtoConverTags, workspace, exportContentData);
        convertToLocalUrlTags((List<StartTag>) sourceDocument.getAllStartTags(HTMLElementName.IFRAME), SRC, outputDocument, zipOutStream, contentResourcesFolderId, filePathtoConverTags, workspace, exportContentData);
        ServletContext servletContext = exportContentData.getServletContext();
        if (servletContext != null) {
            convertToLocalPlayerTags((List<StartTag>) sourceDocument.getAllStartTags(HTMLElementName.OBJECT), outputDocument, zipOutStream, contentResourcesFolderId, workspace, servletContext);
            convertToLocalPlayerTags((List<StartTag>) sourceDocument.getAllStartTags(HTMLElementName.PARAM), outputDocument, zipOutStream, contentResourcesFolderId, workspace, servletContext);
            convertToLocalPlayerTags((List<StartTag>) sourceDocument.getAllStartTags(EMBED), outputDocument, zipOutStream, contentResourcesFolderId, workspace, servletContext);
        }
        return outputDocument.toString();
    }

    private void convertToLocalPlayerTags(List<StartTag> startTags, OutputDocument outputDocument, ZipOutputStream zipOutStream, String contentResourcesFolderId, String workspace, ServletContext servletContext) {
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
                                javax.jcr.Node node = session.getNodeByUUID(contentResourcesFolderId);
                                dFolder = (DirectoryFolder) directoryPersister.getItemFromNode(node);
                                List<DirectoryFile> folderList = directoryPersister.listFiles(dFolder);
                                Iterator<DirectoryFile> folderIt = folderList.iterator();
                                while (folderIt.hasNext()) {
                                    dFile = folderIt.next();
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

    void convertToLocalUrlTags(List<StartTag> startTags, String urlAttributeName, OutputDocument outputDocument, ZipOutputStream zipOutStream, String contentResourcesFolderId, String filePathFromConvert, String workspace, ExportContentData exportContentData) {
        String attributeName = null;
        String attributeValue = null;
        String filePathToConvert = null;
        for (StartTag startTag : startTags) {
            attributeName = urlAttributeName;
            attributeValue = startTag.getAttributeValue(attributeName);
            int index = attributeValue.indexOf(ASSOCIATED_FILES_PREFIX);
            int contentPathLength = ASSOCIATED_FILES_PREFIX.length() + 37;
            if ((attributeValue != null) && (attributeValue.length() > contentPathLength)) {
                if (attributeValue.contains(ASSOCIATED_FILES_PREFIX)) {
                    filePathToConvert = convertReferencePath(attributeValue, contentPathLength, filePathFromConvert);
                    int lengthPrefix = ASSOCIATED_FILES_PREFIX.length() + index;
                    String newStartTag = substituteAttributeValue(startTag, attributeName, filePathToConvert);
                    String folderId = attributeValue.substring(lengthPrefix, lengthPrefix + 36);
                    outputDocument.replace(startTag, newStartTag);
                    if (!folderId.equals(contentResourcesFolderId) && (zipOutStream != null)) {
                        addReferencedFilesToZip(filePathToConvert, folderId, zipOutStream, workspace, exportContentData);
                    }
                }
            } else if (attributeValue.startsWith(ASSOCIATED_IMAGES_PREFIX)) {
                String newStartTag = substituteAttributeValue(startTag, attributeName, attributeValue.substring(ASSOCIATED_IMAGES_PREFIX.length()));
                outputDocument.replace(startTag, newStartTag);
            }
        }
    }

    private String convertReferencePath(String attributeValue, int contentPathLength, String filePathFromConvert) {
        int index = attributeValue.indexOf(ASSOCIATED_FILES_PREFIX);
        if (index == -1) return attributeValue;
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

    protected String substituteAttributeValue(StartTag startTag, String attributeName, String newAttributeValue) {
        StringBuffer newStartTagBuffer = new StringBuffer();
        newStartTagBuffer.append("<").append(startTag.getName()).append(" ");
        newStartTagBuffer.append(attributeName + "=\"").append(newAttributeValue).append("\"");
        Iterator<Attribute> attributesIterator = attributesIterator(startTag);
        while (attributesIterator.hasNext()) {
            Attribute attribute = attributesIterator.next();
            if (!attributeName.equals(attribute.getName())) {
                newStartTagBuffer.append(" ").append(attribute.getName());
                newStartTagBuffer.append("=\"").append(attribute.getValue());
                newStartTagBuffer.append("\"");
            }
        }
        newStartTagBuffer.append(" />");
        return newStartTagBuffer.toString();
    }

    @SuppressWarnings("unchecked")
    protected Iterator<Attribute> attributesIterator(StartTag startTag) {
        return startTag.getAttributes().iterator();
    }

    private void addReferencedFilesToZip(String fullPath, String folderId, ZipOutputStream zipOutStream, String workspace, ExportContentData exportContentData) {
        Session session = JCRUtil.currentSession(workspace);
        DirectoryPersister directoryPersister = ManagerRegistry.getDirectoryPersister();
        DirectoryFolder folder;
        try {
            StringTokenizer st = new StringTokenizer(fullPath, "/");
            int numberofPathElements = st.countTokens();
            while (numberofPathElements > 1) {
                String currenPathElement = st.nextToken();
                javax.jcr.Node node = session.getNodeByUUID(folderId);
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
            javax.jcr.Node targetNode = session.getNodeByUUID(folderId);
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

    private void addReferencedFilesToZip(DirectoryFile file, ZipOutputStream zipOutStream, String path, ExportContentData exportContentData) {
        addDirectoryFileToZip(file, zipOutStream, path, null, exportContentData);
    }

    void exportAdditionalFiles(Content content, ZipOutputStream zipOutStream) {
        List<String> filesEntries = CmsConfig.getExportedImagesChaptersZipEntries();
        String basePath = CmsConfig.getExportedImagesFilesDirectoryPath();
        writeFilesToZip(filesEntries, basePath, content.getResourcesFolder(), zipOutStream);
    }

    void writeFilesToZip(List<String> filesEntries, String basePath, DirectoryFolder resourcesFolder, ZipOutputStream zipOutStream) {
        DirectoryPersister directoryPersister = ManagerRegistry.getDirectoryPersister();
        for (String filezipEntryName : filesEntries) {
            ZipEntry zipEntry = new ZipEntry(filezipEntryName);
            String resourceName = null;
            try {
                if (!directoryPersister.existFileInRootFolder(filezipEntryName, resourcesFolder.getId(), resourcesFolder.getWorkspace())) {
                    zipOutStream.putNextEntry(zipEntry);
                    if (!filezipEntryName.endsWith("/")) {
                        resourceName = basePath + filezipEntryName;
                        BufferedInputStream inputStream = new BufferedInputStream(ScormUtil.class.getResourceAsStream(resourceName));
                        writeInputStreamIntoOutputStream(inputStream, zipOutStream);
                    }
                }
                zipOutStream.closeEntry();
            } catch (ZipException e) {
                log.warn("ZipException adding control files to exported zip", e);
            } catch (IOException e) {
                String errorAddingFileMessage = "Error adding file to zip" + resourceName;
                log.error(errorAddingFileMessage, e);
                throw new CMSRuntimeException(errorAddingFileMessage, e);
            }
        }
    }

    public static String dumpXml(Document document) {
        StringWriter writer = new StringWriter();
        try {
            OutputFormat outputFormat = new OutputFormat();
            outputFormat.setIndenting(true);
            outputFormat.setIndent(4);
            new XMLSerializer(writer, outputFormat).serialize(document);
            return writer.getBuffer().toString();
        } catch (IOException e) {
            return e.toString();
        }
    }
}
