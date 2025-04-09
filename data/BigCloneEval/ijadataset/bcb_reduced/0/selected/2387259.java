package org.opencms.workplace.tools.database;

import org.opencms.db.CmsDbIoException;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.importexport.CmsImportExportException;
import org.opencms.loader.CmsResourceManager;
import org.opencms.lock.CmsLock;
import org.opencms.lock.CmsLockType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.report.I_CmsReport;
import org.opencms.staticexport.CmsLinkTable;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.page.CmsXmlPage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;

/**
 * This class implements the HTML->OpenCms Template converter for OpenCms 6.x.<p>
 * 
 * The HTML files can lay in a directory or in a zip file. The entries in the zip file 
 * are saved temporary in the tmp-directory of the system. Every file is stored into the 
 * correct location in the OpenCms VFS.<p>
 * 
 * 
 * @author Michael Emmerich 
 * @author Armen Markarian 
 * @author Peter Bonrad
 * @author Anja R�ttgers
 * 
 * @version $Revision: 1.20 $ 
 * 
 * @since 6.0.0 
 */
public class CmsHtmlImport {

    /** filename of the meta.properties file. */
    public static final String META_PROPERTIES = "meta.properties";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsHtmlImport.class);

    /**
     * This function creates a folder in the temporary-directory.<p>
     * 
     * @param name the name of the folder
     * 
     * @return the folder file
     * 
     * @throws Exception if the folder can not create
     */
    public static File createTempFolder(String name) throws Exception {
        File folder = null;
        folder = File.createTempFile(name, "", null);
        folder.delete();
        folder.mkdirs();
        folder.deleteOnExit();
        return folder;
    }

    /** the CmsObject to use. */
    private CmsObject m_cmsObject;

    /** the destination directory in the OpenCms VFS. */
    private String m_destinationDir;

    /** the download gallery name. */
    private String m_downloadGallery;

    /** the element name of the template. */
    private String m_element;

    /** the end pattern for extracting content. */
    private String m_endPattern;

    /** HashMap of all known extensions in OpenCms. */
    private Map m_extensions;

    /** Storage for external links. */
    private HashSet m_externalLinks;

    /** The file index contains all resource names in the real file system and their renamed ones in the OpenCms VFS. */
    private HashMap m_fileIndex;

    /** the HTML converter to parse and modify the content. */
    private CmsHtmlImportConverter m_htmlConverter;

    /** the path of the import temporary file in the "real" file system.*/
    private String m_httpDir;

    /** the image gallery name. */
    private String m_imageGallery;

    /** Storage for image alt tags, it is filled by the HtmlConverter each time a new image is found. */
    private HashMap m_imageInfo;

    /** the input directory in the "real" file system. */
    private String m_inputDir;

    /** the encoding used for all imported input files. */
    private String m_inputEncoding;

    /** should broken links be kept. */
    private boolean m_keepBrokenLinks;

    /** the external link gallery name. */
    private String m_linkGallery;

    /** the local use for content definition. */
    private String m_locale;

    /** the overwrite value new resources. */
    private boolean m_overwrite;

    /** Map with all parents in file system to OpenCms. */
    private HashMap m_parents;

    /** the report for the output. */
    private I_CmsReport m_report;

    /** the start pattern for extracting content. */
    private String m_startPattern;

    /** the template use for all pages. */
    private String m_template;

    /**
     * Default Constructor.<p>
     */
    public CmsHtmlImport() {
        m_overwrite = true;
        m_extensions = OpenCms.getResourceManager().getExtensionMapping();
        m_fileIndex = new HashMap();
        m_parents = new HashMap();
        m_imageInfo = new HashMap();
        m_externalLinks = new HashSet();
        m_htmlConverter = new CmsHtmlImportConverter(this, false);
    }

    /**
     * Creates a new import object for the given cms object.<p>
     * 
     * @param cms the current cms context
     */
    public CmsHtmlImport(CmsObject cms) {
        this();
        m_cmsObject = cms;
    }

    /**
     * Calculates an absolute uri from a relative "uri" and the given absolute "baseUri".<p> 
     * 
     * If "uri" is already absolute, it is returned unchanged.
     * This method also returns "uri" unchanged if it is not well-formed.<p>
     *    
     * @param relativeUri the relative uri to calculate an absolute uri for
     * @param baseUri the base uri, this must be an absolute uri
     * @return an absolute uri calculated from "uri" and "baseUri"
     */
    public String getAbsoluteUri(String relativeUri, String baseUri) {
        if ((relativeUri == null) || (relativeUri.charAt(0) == '/') || (relativeUri.startsWith("#"))) {
            return relativeUri;
        }
        String windowsAddition = "";
        if (File.separator.equals("\\")) {
            windowsAddition = ":";
        }
        try {
            URL baseUrl = new URL("file://");
            URL url = new URL(new URL(baseUrl, "file://" + baseUri), relativeUri);
            if (url.getQuery() == null) {
                if (url.getRef() == null) {
                    return url.getHost() + windowsAddition + url.getPath();
                } else {
                    return url.getHost() + windowsAddition + url.getPath() + "#" + url.getRef();
                }
            } else {
                return url.getHost() + windowsAddition + url.getPath() + "?" + url.getQuery();
            }
        } catch (MalformedURLException e) {
            return relativeUri;
        }
    }

    /**
     * Returns the destinationDir.<p>
     *
     * @return the destinationDir
     */
    public String getDestinationDir() {
        return m_destinationDir;
    }

    /**
     * Returns the downloadGallery.<p>
     *
     * @return the downloadGallery
     */
    public String getDownloadGallery() {
        return m_downloadGallery;
    }

    /**
     * Returns the element.<p>
     *
     * @return the element
     */
    public String getElement() {
        return m_element;
    }

    /**
     * Returns the endPattern.<p>
     *
     * @return the endPattern
     */
    public String getEndPattern() {
        return m_endPattern;
    }

    /**
     * Returns the httpDir.<p>
     *
     * @return the httpDir
     */
    public String getHttpDir() {
        return m_httpDir;
    }

    /**
     * Returns the imageGallery.<p>
     *
     * @return the imageGallery
     */
    public String getImageGallery() {
        return m_imageGallery;
    }

    /**
     * Returns the inputDir.<p>
     *
     * @return the inputDir
     */
    public String getInputDir() {
        return m_inputDir;
    }

    /**
     * Returns the inputEncoding.<p>
     *
     * @return the inputEncoding
     */
    public String getInputEncoding() {
        return m_inputEncoding;
    }

    /**
     * Returns the linkGallery.<p>
     *
     * @return the linkGallery
     */
    public String getLinkGallery() {
        return m_linkGallery;
    }

    /**
     * Returns the local.<p>
     *
     * @return the local
     */
    public String getLocale() {
        return m_locale;
    }

    /**
     * Returns the startPattern.<p>
     *
     * @return the startPattern
     */
    public String getStartPattern() {
        return m_startPattern;
    }

    /**
     * Returns the template.<p>
     *
     * @return the template
     */
    public String getTemplate() {
        return m_template;
    }

    /**
     * Returns the keepBrokenLinks.<p>
     *
     * @return the keepBrokenLinks
     */
    public boolean isKeepBrokenLinks() {
        return m_keepBrokenLinks;
    }

    /**
     * Returns the overwrite.<p>
     *
     * @return the overwrite
     */
    public boolean isOverwrite() {
        return m_overwrite;
    }

    /**
     * Sets the cmsObject.<p>
     *
     * @param cmsObject the cmsObject to set
     */
    public void setCmsObject(CmsObject cmsObject) {
        m_cmsObject = cmsObject;
    }

    /**
     * Sets the destinationDir.<p>
     *
     * @param destinationDir the destinationDir to set
     */
    public void setDestinationDir(String destinationDir) {
        m_destinationDir = destinationDir;
    }

    /**
     * Sets the downloadGallery.<p>
     *
     * @param downloadGallery the downloadGallery to set
     */
    public void setDownloadGallery(String downloadGallery) {
        m_downloadGallery = downloadGallery;
    }

    /**
     * Sets the element.<p>
     *
     * @param element the element to set
     */
    public void setElement(String element) {
        m_element = element;
    }

    /**
     * Sets the endPattern.<p>
     *
     * @param endPattern the endPattern to set
     */
    public void setEndPattern(String endPattern) {
        m_endPattern = endPattern;
    }

    /**
     * Sets the httpDir.<p>
     *
     * @param httpDir the httpDir to set
     */
    public void setHttpDir(String httpDir) {
        m_httpDir = httpDir;
    }

    /**
     * Sets the imageGallery.<p>
     *
     * @param imageGallery the imageGallery to set
     */
    public void setImageGallery(String imageGallery) {
        m_imageGallery = imageGallery;
    }

    /**
     * Sets the inputDir.<p>
     *
     * @param inputDir the inputDir to set
     */
    public void setInputDir(String inputDir) {
        m_inputDir = inputDir;
    }

    /**
     * Sets the inputEncoding.<p>
     *
     * @param inputEncoding the inputEncoding to set
     */
    public void setInputEncoding(String inputEncoding) {
        m_inputEncoding = inputEncoding;
    }

    /**
     * Sets the keepBrokenLinks.<p>
     *
     * @param keepBrokenLinks the keepBrokenLinks to set
     */
    public void setKeepBrokenLinks(boolean keepBrokenLinks) {
        m_keepBrokenLinks = keepBrokenLinks;
    }

    /**
     * Sets the linkGallery.<p>
     *
     * @param linkGallery the linkGallery to set
     */
    public void setLinkGallery(String linkGallery) {
        m_linkGallery = linkGallery;
    }

    /**
     * Sets the local.<p>
     *
     * @param locale the local to set
     */
    public void setLocale(String locale) {
        m_locale = locale;
    }

    /**
     * Sets the overwrite.<p>
     *
     * @param overwrite the overwrite to set
     */
    public void setOverwrite(boolean overwrite) {
        m_overwrite = overwrite;
    }

    /**
     * Sets the startPattern.<p>
     *
     * @param startPattern the startPattern to set
     */
    public void setStartPattern(String startPattern) {
        m_startPattern = startPattern;
    }

    /**
     * Sets the template.<p>
     *
     * @param template the template to set
     */
    public void setTemplate(String template) {
        m_template = template;
    }

    /**
     * Imports all resources from the real file system, stores them into the correct locations
     * in the OpenCms VFS and modifies all links. This method is called form the JSP to start the
     * import process.<p>
     * 
     * @param report StringBuffer for reporting
     * 
     * @throws Exception if something goes wrong
     */
    public void startImport(I_CmsReport report) throws Exception {
        try {
            m_report = report;
            m_report.println(Messages.get().container(Messages.RPT_HTML_IMPORT_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);
            boolean isStream = !CmsStringUtil.isEmptyOrWhitespaceOnly(m_httpDir);
            File streamFolder = null;
            if (isStream) {
                streamFolder = unzipStream();
                m_inputDir = streamFolder.getAbsolutePath();
            }
            buildIndex(m_inputDir);
            buildParentPath();
            copyHtmlFiles(m_inputDir);
            copyOtherFiles(m_inputDir);
            createExternalLinks();
            if (isStream && streamFolder != null) {
                m_report.println(Messages.get().container(Messages.RPT_HTML_DELETE_0), I_CmsReport.FORMAT_NOTE);
                CmsFileUtil.purgeDirectory(streamFolder);
                File file = new File(m_httpDir);
                if (file.exists() && file.canWrite()) {
                    file.delete();
                }
            }
            m_report.println(Messages.get().container(Messages.RPT_HTML_IMPORT_END_0), I_CmsReport.FORMAT_HEADLINE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a new external link to the storage of external links.<p>
     * 
     * All links in this storage are later used to create entries in the external link gallery.<p>
     * 
     * @param externalLink link to an external resource
     * 
     * @return the complete path to the external link file, if one is created.
     */
    public String storeExternalLink(String externalLink) {
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_linkGallery)) {
            m_externalLinks.add(externalLink);
            return getExternalLinkFile(externalLink);
        }
        return null;
    }

    /**
     * Add a new image info to the storage of image info's.<p>
     * 
     * The image info's are later used to set the description properties of the images.<p>
     * 
     * @param image the name of the image
     * @param altText the alt-text of the image
     */
    public void storeImageInfo(String image, String altText) {
        m_imageInfo.put(image, altText);
    }

    /**
     * Translated a link into the real file system to its new location in the OpenCms VFS.<p>
     * 
     * This is needed by the HtmlConverter to get the correct links for link translation.<p>
     * 
     * @param link link to the real file system
     * 
     * @return string containing absolute link into the OpenCms VFS
     */
    public String translateLink(String link) {
        String translatedLink = null;
        translatedLink = (String) m_fileIndex.get(link.replace('\\', '/'));
        if (translatedLink == null) {
            if (link.startsWith("#")) {
                translatedLink = link;
            } else if (link.startsWith("/")) {
                if (link.startsWith(OpenCms.getSystemInfo().getOpenCmsContext())) {
                    link = link.substring(OpenCms.getSystemInfo().getOpenCmsContext().length());
                }
                if ((m_keepBrokenLinks) || (m_cmsObject.existsResource(link))) {
                    translatedLink = link;
                }
            } else {
                String fileBase = getBasePath(m_inputDir, link);
                String cmsBase = (String) m_parents.get(fileBase);
                if (cmsBase != null) {
                    String outLink = cmsBase + link.substring(fileBase.length()).replace('\\', '/');
                    if ((m_keepBrokenLinks) || (m_cmsObject.existsResource(outLink))) {
                        translatedLink = outLink;
                    }
                }
            }
        }
        if ((translatedLink != null) && translatedLink.endsWith("/")) {
            translatedLink += "index.html";
        }
        if (translatedLink == null) {
            translatedLink = "#";
        }
        return translatedLink;
    }

    /**
     * Tests if all given input parameters for the HTML Import are valid, that is that all the 
     * given folders do exist. <p>
     * 
     * @param fi a file item if a file is uploaded per HTTP otherwise <code>null</code>
     * @param isdefault if this sets, then the destination and input directory can be empty
     * 
     * @throws CmsIllegalArgumentException if some parameters are not valid
     */
    public void validate(FileItem fi, boolean isdefault) throws CmsIllegalArgumentException {
        if (fi == null) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_inputDir) && !isdefault) {
                throw new CmsIllegalArgumentException(Messages.get().container(Messages.GUI_HTMLIMPORT_INPUTDIR_1, m_inputDir));
            } else if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_inputDir)) {
                File inputDir = new File(m_inputDir);
                if (!inputDir.exists() || inputDir.isFile()) {
                    throw new CmsIllegalArgumentException(Messages.get().container(Messages.GUI_HTMLIMPORT_INPUTDIR_1, m_inputDir));
                }
            }
        }
        try {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_destinationDir) && !isdefault) {
                throw new CmsIllegalArgumentException(Messages.get().container(Messages.GUI_HTMLIMPORT_DESTDIR_1, m_destinationDir));
            } else if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_destinationDir)) {
                m_cmsObject.readFolder(m_destinationDir);
            }
        } catch (CmsException e) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.GUI_HTMLIMPORT_DESTDIR_1, m_destinationDir), e);
        }
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_imageGallery)) {
            try {
                CmsFolder folder = m_cmsObject.readFolder(m_imageGallery);
                String name = OpenCms.getResourceManager().getResourceType(folder.getTypeId()).getTypeName();
                if (!name.equals("imagegallery")) {
                    throw new CmsIllegalArgumentException(Messages.get().container(Messages.GUI_HTMLIMPORT_IMGGALLERY_INVALID_1, m_imageGallery));
                }
            } catch (CmsException e) {
                throw new CmsIllegalArgumentException(Messages.get().container(Messages.GUI_HTMLIMPORT_IMGGALLERY_1, m_imageGallery), e);
            }
        }
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_linkGallery)) {
            try {
                CmsFolder folder = m_cmsObject.readFolder(m_linkGallery);
                String name = OpenCms.getResourceManager().getResourceType(folder.getTypeId()).getTypeName();
                if (!name.equals("linkgallery")) {
                    throw new CmsIllegalArgumentException(Messages.get().container(Messages.GUI_HTMLIMPORT_LINKGALLERY_INVALID_1, m_linkGallery));
                }
            } catch (CmsException e) {
                throw new CmsIllegalArgumentException(Messages.get().container(Messages.GUI_HTMLIMPORT_LINKGALLERY_1, m_linkGallery), e);
            }
        }
        if ((!isExternal(m_downloadGallery)) && (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_downloadGallery))) {
            try {
                CmsFolder folder = m_cmsObject.readFolder(m_downloadGallery);
                String name = OpenCms.getResourceManager().getResourceType(folder.getTypeId()).getTypeName();
                if (!name.equals("downloadgallery")) {
                    throw new CmsIllegalArgumentException(Messages.get().container(Messages.GUI_HTMLIMPORT_DOWNGALLERY_INVALID_1, m_downloadGallery));
                }
            } catch (CmsException e) {
                throw new CmsIllegalArgumentException(Messages.get().container(Messages.GUI_HTMLIMPORT_DOWNGALLERY_1, m_downloadGallery), e);
            }
        }
        try {
            m_cmsObject.readResource(m_template, CmsResourceFilter.ALL);
        } catch (CmsException e) {
            if (!isValidElement()) {
                throw new CmsIllegalArgumentException(Messages.get().container(Messages.GUI_HTMLIMPORT_TEMPLATE_1, m_template), e);
            }
        }
        if (!isValidElement()) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.GUI_HTMLIMPORT_INVALID_ELEM_2, m_element, m_template));
        }
        if (m_cmsObject.getRequestContext().currentProject().isOnlineProject()) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.GUI_HTMLIMPORT_CONSTRAINT_OFFLINE_0));
        }
    }

    /**
     * Builds an index of all files to be imported and determines their new names in the OpenCms.<p>
     * 
     * @param startfolder the folder to start with
     * 
     * @throws Exception if something goes wrong
     */
    private void buildIndex(String startfolder) throws Exception {
        File folder = new File(startfolder);
        File[] subresources = folder.listFiles();
        for (int i = 0; i < subresources.length; i++) {
            try {
                String relativeFSName = subresources[i].getAbsolutePath().substring(m_inputDir.length() + 1);
                String absoluteVFSName = getVfsName(relativeFSName, subresources[i].getName(), subresources[i].isFile());
                m_report.print(Messages.get().container(Messages.RPT_CREATE_INDEX_0), I_CmsReport.FORMAT_NOTE);
                m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, relativeFSName.replace('\\', '/')));
                m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                m_report.print(Messages.get().container(Messages.RPT_ARROW_RIGHT_0), I_CmsReport.FORMAT_NOTE);
                m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, absoluteVFSName));
                m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                m_fileIndex.put(subresources[i].getAbsolutePath().replace('\\', '/'), absoluteVFSName);
                if (subresources[i].isDirectory()) {
                    buildIndex(subresources[i].getAbsolutePath());
                }
                m_report.println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0), I_CmsReport.FORMAT_OK);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                m_report.println(e);
            }
        }
    }

    /**
     * Builds a map with all parents of the destination directory to the real file system.<p>
     * So links to resources of outside the import folder can be found.<p>
     */
    private void buildParentPath() {
        String destFolder = m_destinationDir;
        String inputDir = m_inputDir.replace('\\', '/');
        if (!inputDir.endsWith("/")) {
            inputDir += "/";
        }
        int pos = inputDir.lastIndexOf("/");
        while ((pos > 0) && (destFolder != null)) {
            inputDir = inputDir.substring(0, pos);
            m_parents.put(inputDir + "/", destFolder);
            pos = inputDir.lastIndexOf("/", pos - 1);
            destFolder = CmsResource.getParentFolder(destFolder);
        }
    }

    /**
     * This function close a InputStream.<p>
     * 
     * @param stream the <code> {@link InputStream} </code> Object
     */
    private void closeStream(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception ex) {
                LOG.error(ex.getLocalizedMessage(), ex);
            }
        }
    }

    /**
     * Copies all  HTML files to the VFS.<p>
     * 
     * @param startfolder the folder to start with
     * 
     * @throws Exception if something goes wrong
     */
    private void copyHtmlFiles(String startfolder) throws Exception {
        try {
            File folder = new File(startfolder);
            File[] subresources = folder.listFiles();
            for (int i = 0; i < subresources.length; i++) {
                if (subresources[i].isDirectory()) {
                    Hashtable properties = new Hashtable();
                    createFolder(subresources[i].getAbsolutePath(), i, properties);
                    copyHtmlFiles(subresources[i].getAbsolutePath());
                } else {
                    String vfsFileName = (String) m_fileIndex.get(subresources[i].getAbsolutePath().replace('\\', '/'));
                    int type = getFileType(vfsFileName);
                    if (CmsResourceTypePlain.getStaticTypeId() == type) {
                        Hashtable properties = new Hashtable();
                        String content = "";
                        try {
                            content = parseHtmlFile(subresources[i], properties);
                        } catch (CmsException e) {
                            m_report.println(e);
                        }
                        properties.put("template", m_template);
                        createFile(subresources[i].getAbsolutePath(), i, content, properties);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Copies all files except HTML files to the VFS.<p>
     * 
     * @param startfolder the folder to start with
     */
    private void copyOtherFiles(String startfolder) {
        try {
            File folder = new File(startfolder);
            File[] subresources = folder.listFiles();
            for (int i = 0; i < subresources.length; i++) {
                if (subresources[i].isDirectory()) {
                    copyOtherFiles(subresources[i].getAbsolutePath());
                } else {
                    if (!subresources[i].getName().equals(META_PROPERTIES)) {
                        String vfsFileName = (String) m_fileIndex.get(subresources[i].getAbsolutePath().replace('\\', '/'));
                        int type = getFileType(vfsFileName);
                        if (CmsResourceTypePlain.getStaticTypeId() != type) {
                            if (isExternal(vfsFileName)) {
                                m_report.print(Messages.get().container(Messages.RPT_SKIP_EXTERNAL_0), I_CmsReport.FORMAT_NOTE);
                                m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, subresources[i]));
                                m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                                m_report.print(Messages.get().container(Messages.RPT_ARROW_RIGHT_0), I_CmsReport.FORMAT_NOTE);
                                m_report.println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, vfsFileName));
                            } else {
                                m_report.print(Messages.get().container(Messages.RPT_IMPORT_0), I_CmsReport.FORMAT_NOTE);
                                m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, vfsFileName));
                                m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                                byte[] content = getFileBytes(subresources[i]);
                                List properties = new ArrayList();
                                String altText = (String) m_imageInfo.get(subresources[i].getAbsolutePath().replace('\\', '/'));
                                CmsProperty property1 = new CmsProperty(CmsPropertyDefinition.PROPERTY_DESCRIPTION, altText, altText);
                                CmsProperty property2 = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, altText, altText);
                                if (altText != null) {
                                    properties.add(property1);
                                    properties.add(property2);
                                }
                                if (!m_overwrite) {
                                    m_cmsObject.createResource(vfsFileName, type, content, properties);
                                } else {
                                    try {
                                        CmsLock lock = m_cmsObject.getLock(vfsFileName);
                                        if (lock.getType() != CmsLockType.EXCLUSIVE) {
                                            m_cmsObject.lockResource(vfsFileName);
                                        }
                                        m_cmsObject.deleteResource(vfsFileName, CmsResource.DELETE_PRESERVE_SIBLINGS);
                                    } catch (CmsException e) {
                                    } finally {
                                        m_cmsObject.createResource(vfsFileName, type, content, properties);
                                    }
                                    m_report.print(Messages.get().container(Messages.RPT_OVERWRITE_0), I_CmsReport.FORMAT_NOTE);
                                    m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                                }
                                m_report.println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0), I_CmsReport.FORMAT_OK);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            m_report.println(e);
        }
    }

    /**
     * Creates all external links, which were found during the HTML-page processing.<p>
     * 
     */
    private void createExternalLinks() {
        Iterator i = m_externalLinks.iterator();
        while (i.hasNext()) {
            String linkUrl = (String) i.next();
            String filename = getExternalLinkFile(linkUrl);
            m_report.print(Messages.get().container(Messages.RPT_CREATE_EXTERNAL_LINK_0), I_CmsReport.FORMAT_NOTE);
            m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, filename));
            m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
            List properties = new ArrayList();
            CmsProperty property1 = new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, "Link to " + linkUrl, "Link to " + linkUrl);
            properties.add(property1);
            try {
                m_cmsObject.createResource(m_linkGallery + filename, CmsResourceTypePointer.getStaticTypeId(), linkUrl.getBytes(), properties);
            } catch (CmsException e) {
            }
            m_report.println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0), I_CmsReport.FORMAT_OK);
        }
    }

    /**
     * Creates a file in the VFS.<p>
     * 
     * @param filename the complete filename in the real file system
     * @param position the default navigation position of this folder
     * @param content the HTML content of the file
     * @param properties the file properties
     */
    private void createFile(String filename, int position, String content, Hashtable properties) {
        String vfsFileName = (String) m_fileIndex.get(filename.replace('\\', '/'));
        if (vfsFileName != null) {
            try {
                m_report.print(Messages.get().container(Messages.RPT_CREATE_FILE_0), I_CmsReport.FORMAT_NOTE);
                m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, vfsFileName));
                m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                if ((properties.get(CmsPropertyDefinition.PROPERTY_NAVPOS) == null) && (properties.get(CmsPropertyDefinition.PROPERTY_NAVTEXT) != null)) {
                    properties.put(CmsPropertyDefinition.PROPERTY_NAVPOS, (position + 1) + "");
                }
                Locale locale = new Locale(m_locale);
                CmsXmlPage page = new CmsXmlPage(locale, OpenCms.getSystemInfo().getDefaultEncoding());
                page.addValue(m_element, locale);
                page.setStringValue(m_cmsObject, m_element, locale, content);
                CmsLinkTable linkTable = page.getLinkTable(m_element, locale);
                Iterator i = linkTable.iterator();
                while (i.hasNext()) {
                    CmsLink link = (CmsLink) i.next();
                    String target = link.getTarget();
                    if (link.isInternal()) {
                        target = m_cmsObject.getRequestContext().getFileTranslator().translateResource(target);
                        link.updateLink(target, link.getAnchor(), link.getQuery());
                        link.checkConsistency(m_cmsObject);
                    }
                }
                byte[] contentByteArray = page.marshal();
                List oldProperties = new ArrayList();
                if (!m_overwrite) {
                    m_cmsObject.createResource(vfsFileName, CmsResourceTypeXmlPage.getStaticTypeId(), contentByteArray, new ArrayList());
                } else {
                    try {
                        oldProperties = m_cmsObject.readPropertyObjects(vfsFileName, false);
                        CmsLock lock = m_cmsObject.getLock(vfsFileName);
                        if (lock.getType() != CmsLockType.EXCLUSIVE) {
                            m_cmsObject.lockResource(vfsFileName);
                        }
                        m_cmsObject.deleteResource(vfsFileName, CmsResource.DELETE_PRESERVE_SIBLINGS);
                    } catch (CmsException e) {
                    } finally {
                        m_report.print(Messages.get().container(Messages.RPT_OVERWRITE_0), I_CmsReport.FORMAT_NOTE);
                        m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                        m_cmsObject.createResource(vfsFileName, CmsResourceTypeXmlPage.getStaticTypeId(), contentByteArray, new ArrayList());
                    }
                }
                Iterator it = properties.entrySet().iterator();
                List propertyList = new ArrayList();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    String propertyKey = (String) entry.getKey();
                    String propertyVal = (String) entry.getValue();
                    CmsProperty property = new CmsProperty(propertyKey, propertyVal, propertyVal);
                    property.setAutoCreatePropertyDefinition(true);
                    propertyList.add(property);
                }
                try {
                    m_cmsObject.writePropertyObjects(vfsFileName, propertyList);
                    m_cmsObject.writePropertyObjects(vfsFileName, oldProperties);
                } catch (CmsException e1) {
                    e1.printStackTrace();
                }
                m_report.println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0), I_CmsReport.FORMAT_OK);
            } catch (CmsException e) {
                m_report.println(e);
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Creates a folder in the VFS.<p>
     * 
     * @param foldername the complete folder name in the real file system
     * @param position the default navigation position of this folder
     * @param properties the file properties
     */
    private void createFolder(String foldername, int position, Hashtable properties) {
        String vfsFolderName = (String) m_fileIndex.get(foldername.replace('\\', '/'));
        m_report.print(Messages.get().container(Messages.RPT_CREATE_FOLDER_0), I_CmsReport.FORMAT_NOTE);
        m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, vfsFolderName));
        m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
        if (vfsFolderName != null) {
            String path = vfsFolderName.substring(0, vfsFolderName.substring(0, vfsFolderName.length() - 1).lastIndexOf("/"));
            String folder = vfsFolderName.substring(path.length(), vfsFolderName.length());
            try {
                String propertyFileName = foldername + File.separator + META_PROPERTIES;
                boolean metaPropertiesFound = false;
                ExtendedProperties propertyFile = new ExtendedProperties();
                try {
                    propertyFile.load(new FileInputStream(new File(propertyFileName)));
                    metaPropertiesFound = true;
                } catch (Exception e1) {
                }
                if (metaPropertiesFound) {
                    Enumeration enu = propertyFile.keys();
                    String property = "";
                    while (enu.hasMoreElements()) {
                        try {
                            property = (String) enu.nextElement();
                            String propertyvalue = (String) propertyFile.get(property);
                            properties.put(property, propertyvalue);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                    if (properties.get(CmsPropertyDefinition.PROPERTY_NAVPOS) == null) {
                        properties.put(CmsPropertyDefinition.PROPERTY_NAVPOS, (position + 1) + "");
                    }
                    if (properties.get(CmsPropertyDefinition.PROPERTY_NAVTEXT) == null) {
                        String navtext = folder.substring(1, 2).toUpperCase() + folder.substring(2, folder.length() - 1);
                        properties.put(CmsPropertyDefinition.PROPERTY_NAVTEXT, navtext);
                    }
                } else {
                    properties = new Hashtable();
                }
                try {
                    m_cmsObject.readFolder(path + folder);
                    m_cmsObject.lockResource(path + folder);
                } catch (CmsException e1) {
                    m_cmsObject.createResource(path + folder, CmsResourceTypeFolder.getStaticTypeId());
                }
                Enumeration enu = properties.keys();
                List propertyList = new ArrayList();
                while (enu.hasMoreElements()) {
                    String propertyKey = (String) enu.nextElement();
                    String propertyVal = (String) properties.get(propertyKey);
                    CmsProperty property = new CmsProperty(propertyKey, propertyVal, propertyVal);
                    property.setAutoCreatePropertyDefinition(true);
                    propertyList.add(property);
                }
                try {
                    m_cmsObject.writePropertyObjects(path + folder, propertyList);
                } catch (CmsException e1) {
                    e1.printStackTrace();
                }
                m_report.println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0), I_CmsReport.FORMAT_OK);
            } catch (CmsException e) {
                m_report.println(e);
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Compares two path's for the base part which have both equal.<p>
     * 
     * @param path1 the first path to compare
     * @param path2 the second path to compare
     * 
     * @return the base path of both which are equal
     */
    private String getBasePath(String path1, String path2) {
        StringBuffer base = new StringBuffer();
        path1 = path1.replace('\\', '/');
        path2 = path2.replace('\\', '/');
        String[] parts1 = path1.split("/");
        String[] parts2 = path2.split("/");
        for (int i = 0; i < parts1.length; i++) {
            if (i >= parts2.length) {
                break;
            }
            if (parts1[i].equals(parts2[i])) {
                base.append(parts1[i] + "/");
            }
        }
        return base.toString();
    }

    /**
     * Creates the filename of the file of the external link.<p>
     * 
     * @param link the link to get the file path for.
     * 
     * @return the filename of the file for the external link.
     */
    private String getExternalLinkFile(String link) {
        String filename = link.substring(link.indexOf("://") + 3, link.length());
        filename = m_cmsObject.getRequestContext().getFileTranslator().translateResource(filename.replace('/', '-'));
        return filename;
    }

    /**
     * Returns a byte array containing the content of server FS file.<p>
     *
     * @param file the name of the file to read
     * 
     * @return bytes[] the content of the file
     * 
     * @throws CmsException if something goes wrong
     */
    private byte[] getFileBytes(File file) throws CmsException {
        byte[] buffer = null;
        FileInputStream fileStream = null;
        int charsRead;
        int size;
        try {
            fileStream = new FileInputStream(file);
            charsRead = 0;
            size = new Long(file.length()).intValue();
            buffer = new byte[size];
            while (charsRead < size) {
                charsRead += fileStream.read(buffer, charsRead, size - charsRead);
            }
            return buffer;
        } catch (IOException e) {
            throw new CmsDbIoException(Messages.get().container(Messages.ERR_GET_FILE_BYTES_1, file.getAbsolutePath()), e);
        } finally {
            closeStream(fileStream);
        }
    }

    /**
     * Returns the OpenCms file type of a real file system file. <p>
     * This is made by checking the extension.<p>
     * 
     * @param filename the name of the file in the real file system  
     * 
     * @return the id of the OpenCms file type
     * 
     * @throws Exception if something goes wrong
     */
    private int getFileType(String filename) throws Exception {
        String extension = "";
        if (filename.indexOf(".") > -1) {
            extension = filename.substring((filename.lastIndexOf(".")));
        }
        String typename = (String) m_extensions.get(extension.toLowerCase());
        if (typename == null) {
            typename = "binary";
        }
        CmsResourceManager resourceManager = OpenCms.getResourceManager();
        return resourceManager.getResourceType(typename).getTypeId();
    }

    /**
     * Gets a valid VfsName form a given name in the real file system.<p>
     * 
     * This name will later be used for all link translations during the HTML-parsing process.<p>
     * 
     * @param relativeName the name in the real file system, relative to the start folder
     * @param name the name of the file
     * @param isFile flag to indicate that the resource is a file
     * 
     * @return a valid name in the VFS
     * 
     * @throws Exception if something goes wrong
     */
    private String getVfsName(String relativeName, String name, boolean isFile) throws Exception {
        String vfsName = relativeName.replace('\\', '/');
        if (isFile) {
            int filetype = getFileType(name);
            if (name.indexOf(".") == 0) {
                name = "unknown" + name;
                int dot = relativeName.lastIndexOf(".");
                relativeName = relativeName.substring(0, dot) + name;
            }
            boolean leaveImages = CmsStringUtil.isEmptyOrWhitespaceOnly(m_imageGallery);
            boolean leaveDownload = CmsStringUtil.isEmptyOrWhitespaceOnly(m_downloadGallery);
            if ((CmsResourceTypeImage.getStaticTypeId() == filetype) && (!leaveImages)) {
                vfsName = m_imageGallery + name;
            } else if ((CmsResourceTypePlain.getStaticTypeId() == filetype) || (leaveImages) || (leaveDownload)) {
                String folderName = relativeName;
                if (folderName.indexOf(".") > 0) {
                    folderName = folderName.substring(0, folderName.indexOf("."));
                }
                folderName = m_inputDir + "\\" + folderName;
                File folder = new File(folderName);
                if (folder.isDirectory()) {
                    vfsName = m_destinationDir + relativeName.substring(0, relativeName.indexOf(".")) + "/index.html";
                } else {
                    vfsName = m_destinationDir + relativeName;
                }
            } else {
                vfsName = m_downloadGallery + name;
            }
            return validateFilename(vfsName);
        } else {
            vfsName = m_destinationDir + vfsName + "/";
            return vfsName;
        }
    }

    /**
     * Tests if a filename is an external name, that is this name does not point into the OpenCms VFS.<p>
     * A filename is an external name if it contains the string "://", e.g. "http://" or "ftp://".<p>
     * 
     * @param filename the filename to test 
     * 
     * @return true or false
     */
    private boolean isExternal(String filename) {
        boolean external = false;
        if (filename.indexOf("://") > 0) {
            external = true;
        }
        return external;
    }

    /** 
     * Checks if m_element is valid element.<p>
     * 
     * @return true if element is valid, otherwise false
     */
    private boolean isValidElement() {
        boolean validElement = false;
        List elementList = new ArrayList();
        try {
            String elements = m_cmsObject.readPropertyObject(m_template, CmsPropertyDefinition.PROPERTY_TEMPLATE_ELEMENTS, false).getValue();
            if (elements != null) {
                StringTokenizer T = new StringTokenizer(elements, ",");
                while (T.hasMoreTokens()) {
                    String currentElement = T.nextToken();
                    int sepIndex = currentElement.indexOf("|");
                    if (sepIndex != -1) {
                        currentElement = currentElement.substring(0, sepIndex);
                    }
                    if (currentElement.endsWith("*")) {
                        currentElement = currentElement.substring(0, currentElement.length() - 1);
                    }
                    elementList.add(currentElement);
                }
            }
            if (elementList.contains(m_element)) {
                validElement = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return validElement;
    }

    /**
     * Reads the content of an HTML file from the real file system and parses it for link
     * transformation.<p>
     * 
     * @param  file the file in the real file system
     * @param properties the file properties
     * 
     * @return the modified HTML code of the file
     * 
     * @throws CmsException if something goes wrong
     */
    private String parseHtmlFile(File file, Hashtable properties) throws CmsException {
        String parsedHtml = "";
        try {
            byte[] content = getFileBytes(file);
            String contentString = new String(content, m_inputEncoding);
            contentString = CmsEncoder.escapeNonAscii(contentString);
            contentString = CmsStringUtil.substitute(contentString, "&#", "{subst}");
            parsedHtml = m_htmlConverter.convertHTML(file.getAbsolutePath(), contentString, m_startPattern, m_endPattern, properties);
            parsedHtml = CmsStringUtil.substitute(parsedHtml, "{subst}", "&#");
        } catch (Exception e) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_HTMLIMPORT_PARSE_1, file.getAbsolutePath());
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsImportExportException(message, e);
        }
        return parsedHtml;
    }

    /**
     * This function reads the zip-file and saved the files and directories in a new 
     * temporary-folder.<p>
     * 
     * @return the temporary-folder where the files from the zip-file are saved
     */
    private File unzipStream() {
        ZipInputStream importZip = null;
        File folder = null;
        try {
            importZip = new ZipInputStream(new FileInputStream(m_httpDir));
            folder = createTempFolder("import_html");
            ZipEntry entry = null;
            byte[] buffer = null;
            while (true) {
                try {
                    entry = importZip.getNextEntry();
                    if (entry == null) {
                        break;
                    }
                    String name = entry.getName();
                    m_report.print(Messages.get().container(Messages.RPT_HTML_UNZIP_0), I_CmsReport.FORMAT_NOTE);
                    m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, name));
                    m_report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                    name = name.replace('/', File.separatorChar);
                    String path = folder + File.separator + name;
                    if (entry.isDirectory()) {
                        File importFile = new File(path);
                        importFile.mkdirs();
                    } else {
                        int size = new Long(entry.getSize()).intValue();
                        if (size == -1) {
                            buffer = CmsFileUtil.readFully(importZip, false);
                        } else {
                            buffer = CmsFileUtil.readFully(importZip, size, false);
                        }
                        File importFile = new File(path);
                        File parent = importFile.getParentFile();
                        if (parent != null) {
                            parent.mkdirs();
                        }
                        importFile.createNewFile();
                        FileOutputStream fileOutput = new FileOutputStream(importFile.getAbsoluteFile());
                        fileOutput.write(buffer);
                        fileOutput.close();
                    }
                    importZip.closeEntry();
                    m_report.println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0), I_CmsReport.FORMAT_OK);
                } catch (Exception ex) {
                    String name = (entry != null ? entry.getName() : "");
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().getBundle().key(Messages.ERR_ZIPFILE_UNZIP_1, name), ex);
                    }
                    m_report.println(Messages.get().container(Messages.ERR_ZIPFILE_UNZIP_1, name), I_CmsReport.FORMAT_ERROR);
                }
                entry = null;
            }
        } catch (Exception ex) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_ZIPFILE_READ_1, m_httpDir), ex);
            }
            m_report.println(Messages.get().container(Messages.ERR_ZIPFILE_READ_1, m_httpDir), I_CmsReport.FORMAT_ERROR);
        } finally {
            closeStream(importZip);
        }
        return folder;
    }

    /**
     * Validates a filename for OpenCms.<p>
     * 
     * This method checks if there are any illegal characters in the filename and modifies them
     * if necessary. In addition it ensures that no duplicate filenames are created.<p>
     * 
     * @param filename the filename to validate
     * 
     * @return a validated and unique filename in OpenCms
     */
    private String validateFilename(String filename) {
        if (isExternal(filename)) {
            return filename;
        }
        int postfix = 1;
        boolean found = true;
        String validFilename = filename;
        if (!m_overwrite) {
            while (found) {
                try {
                    validFilename = m_cmsObject.getRequestContext().getFileTranslator().translateResource(validFilename);
                    found = true;
                    if (!m_fileIndex.containsValue(validFilename.replace('\\', '/'))) {
                        found = false;
                    }
                    if (!found) {
                        found = true;
                        m_cmsObject.readResource(validFilename, CmsResourceFilter.ALL);
                    }
                    String path = filename.substring(0, filename.lastIndexOf("/") + 1);
                    String name = filename.substring(filename.lastIndexOf("/") + 1, filename.length());
                    validFilename = path;
                    if (name.lastIndexOf(".") > 0) {
                        validFilename += name.substring(0, name.lastIndexOf("."));
                    } else {
                        validFilename += name;
                    }
                    validFilename += "_" + postfix;
                    if (name.lastIndexOf(".") > 0) {
                        validFilename += name.substring(name.lastIndexOf("."), name.length());
                    }
                    postfix++;
                } catch (CmsException e) {
                    found = false;
                }
            }
        } else {
            validFilename = validFilename.replace('\\', '/');
        }
        return OpenCms.getResourceManager().getFileTranslator().translateResource(validFilename);
    }
}
