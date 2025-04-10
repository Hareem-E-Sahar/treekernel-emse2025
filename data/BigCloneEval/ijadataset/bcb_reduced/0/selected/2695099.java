package org.opencms.importexport;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsFolder;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsException;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.report.CmsShellReport;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsDataTypeUtil;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.CmsXmlSaxWriter;
import org.opencms.workplace.CmsWorkplace;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXWriter;
import org.xml.sax.SAXException;

/**
 * Provides the functionality to export files from the OpenCms VFS to a ZIP file.<p>
 * 
 * The ZIP file written will contain a copy of all exported files with their contents.
 * It will also contain a <code>manifest.xml</code> file in which all meta-information 
 * about this files are stored, like permissions etc.<p>
 *
 * @author Alexander Kandzior 
 * @author Michael Emmerich 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.95 $ 
 * 
 * @since 6.0.0 
 */
public class CmsExport {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExport.class);

    private static final int SUB_LENGTH = 4096;

    /** Counter for the export. */
    private int m_exportCount;

    /** Set of all exported files, required for preventing redundant sibling export. */
    private Set m_exportedResources;

    /** The export ZIP stream to write resources to. */
    private ZipOutputStream m_exportZipStream;

    /** The export parameters. */
    private CmsExportParameters m_parameters;

    /** The top level file node where all resources are appended to. */
    private Element m_resourceNode;

    /** The SAX writer to write the output to. */
    private SAXWriter m_saxWriter;

    /** Cache for previously added super folders. */
    private List m_superFolders;

    /**
     * Constructs a new uninitialized export, required for special subclass data export.<p>
     */
    public CmsExport() {
    }

    /** The cms context. */
    private CmsObject m_cms;

    /** The report. */
    private I_CmsReport m_report;

    /**
     * Constructs a new export.<p>
     * 
     * @param cms the cms context 
     * @param report the report
     *
     * @throws CmsRoleViolationException if the current user has not the required role
     */
    public CmsExport(CmsObject cms, I_CmsReport report) throws CmsRoleViolationException {
        m_cms = cms;
        m_report = report;
        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.DATABASE_MANAGER);
    }

    /**
     * Export the data.<p>
     * 
     * @param parameters the export parameters
     * 
     * @throws CmsImportExportException if something goes wrong 
     */
    public void exportData(CmsExportParameters parameters) throws CmsImportExportException {
        m_parameters = parameters;
        m_exportCount = 0;
        getReport().println(Messages.get().container(Messages.RPT_CLEARCACHE_0), I_CmsReport.FORMAT_NOTE);
        OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.EMPTY_MAP));
        try {
            Element exportNode = openExportFile();
            if (m_parameters.getModuleInfo() != null) {
                exportNode.add(m_parameters.getModuleInfo());
                digestElement(exportNode, m_parameters.getModuleInfo());
            }
            if (m_parameters.isExportAccountData()) {
                Element accountsElement = exportNode.addElement(CmsImportVersion7.N_ACCOUNTS);
                getSaxWriter().writeOpen(accountsElement);
                exportOrgUnits(accountsElement);
                getSaxWriter().writeClose(accountsElement);
                exportNode.remove(accountsElement);
            }
            if (m_parameters.isExportResourceData()) {
                exportAllResources(exportNode, m_parameters.getResources());
            }
            if (m_parameters.isExportProjectData()) {
                Element projectsElement = exportNode.addElement(CmsImportVersion7.N_PROJECTS);
                getSaxWriter().writeOpen(projectsElement);
                exportProjects(projectsElement);
                getSaxWriter().writeClose(projectsElement);
                exportNode.remove(projectsElement);
            }
            closeExportFile(exportNode);
        } catch (SAXException se) {
            getReport().println(se);
            CmsMessageContainer message = Messages.get().container(Messages.ERR_IMPORTEXPORT_ERROR_EXPORTING_TO_FILE_1, getExportFileName());
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), se);
            }
            throw new CmsImportExportException(message, se);
        } catch (IOException ioe) {
            getReport().println(ioe);
            CmsMessageContainer message = Messages.get().container(Messages.ERR_IMPORTEXPORT_ERROR_EXPORTING_TO_FILE_1, getExportFileName());
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), ioe);
            }
            throw new CmsImportExportException(message, ioe);
        }
    }

    /**
     * Constructs a new export.<p>
     *
     * @param cms the cmsObject to work with
     * @param exportFile the file or folder to export to
     * @param resourcesToExport the paths of folders and files to export
     * @param includeSystem if <code>true</code>, the system folder is included
     * @param includeUnchanged <code>true</code>, if unchanged files should be included
     * 
     * @throws CmsImportExportException if something goes wrong
     * @throws CmsRoleViolationException if the current user has not the required role
     * 
     * @deprecated use the {@link CmsExportParameters} constructor instead
     */
    public CmsExport(CmsObject cms, String exportFile, List resourcesToExport, boolean includeSystem, boolean includeUnchanged) throws CmsImportExportException, CmsRoleViolationException {
        this(cms, exportFile, resourcesToExport, includeSystem, includeUnchanged, null, false, 0, new CmsShellReport(cms.getRequestContext().getLocale()));
    }

    /**
     * Constructs a new export.<p>
     *
     * @param cms the cmsObject to work with
     * @param exportFile the file or folder to export to
     * @param resourcesToExport the paths of folders and files to export
     * @param includeSystem if <code>true</code>, the system folder is included
     * @param includeUnchanged <code>true</code>, if unchanged files should be included
     * @param moduleElement module informations in a Node for module export
     * @param exportUserdata if <code>true</code>, the user and group data will also be exported
     * @param contentAge export contents changed after this date/time
     * @param report to handle the log messages
     * 
     * @throws CmsImportExportException if something goes wrong
     * @throws CmsRoleViolationException if the current user has not the required role
     * 
     * @deprecated use the {@link CmsExportParameters} constructor instead
     */
    public CmsExport(CmsObject cms, String exportFile, List resourcesToExport, boolean includeSystem, boolean includeUnchanged, Element moduleElement, boolean exportUserdata, long contentAge, I_CmsReport report) throws CmsImportExportException, CmsRoleViolationException {
        this(cms, exportFile, resourcesToExport, includeSystem, includeUnchanged, moduleElement, exportUserdata, contentAge, report, true);
    }

    /**
     * Constructs a new export.<p>
     *
     * @param cms the cmsObject to work with
     * @param exportFile the file or folder to export to
     * @param resourcesToExport the paths of folders and files to export
     * @param includeSystem if <code>true</code>, the system folder is included
     * @param includeUnchanged <code>true</code>, if unchanged files should be included
     * @param moduleElement module informations in a Node for module export
     * @param exportUserdata if <code>true</code>, the user and group data will also be exported
     * @param contentAge export contents changed after this date/time
     * @param report to handle the log messages
     * @param recursive recursive flag
     * 
     * @throws CmsImportExportException if something goes wrong
     * @throws CmsRoleViolationException if the current user has not the required role
     * 
     * @deprecated use the {@link CmsExportParameters} constructor instead
     */
    public CmsExport(CmsObject cms, String exportFile, List resourcesToExport, boolean includeSystem, boolean includeUnchanged, Element moduleElement, boolean exportUserdata, long contentAge, I_CmsReport report, boolean recursive) throws CmsImportExportException, CmsRoleViolationException {
        this(cms, report);
        exportData(new CmsExportParameters(exportFile, moduleElement, true, exportUserdata, false, resourcesToExport, includeSystem, includeUnchanged, contentAge, recursive, false));
    }

    /**
     * Exports the given folder and all child resources.<p>
     *
     * @param folderName to complete path to the resource to export
     * 
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     * @throws IOException if not all resources could be appended to the ZIP archive
     */
    protected void addChildResources(String folderName) throws CmsImportExportException, IOException, SAXException {
        try {
            List subFolders = getCms().getSubFolders(folderName, CmsResourceFilter.IGNORE_EXPIRATION);
            List subFiles = getCms().getFilesInFolder(folderName, CmsResourceFilter.IGNORE_EXPIRATION);
            for (int i = 0; i < subFiles.size(); i++) {
                CmsResource file = (CmsResource) subFiles.get(i);
                CmsResourceState state = file.getState();
                long age = file.getDateLastModified() < file.getDateCreated() ? file.getDateCreated() : file.getDateLastModified();
                if (getCms().getRequestContext().currentProject().isOnlineProject() || (m_parameters.isIncludeUnchangedResources()) || state.isNew() || state.isChanged()) {
                    if (!state.isDeleted() && !CmsWorkplace.isTemporaryFile(file) && (age >= m_parameters.getContentAge())) {
                        String export = getCms().getSitePath(file);
                        if (checkExportResource(export)) {
                            if (isInExportableProject(file)) {
                                exportFile(getCms().readFile(export, CmsResourceFilter.IGNORE_EXPIRATION));
                            }
                        }
                    }
                }
                subFiles.set(i, null);
            }
            subFiles = null;
            for (int i = 0; i < subFolders.size(); i++) {
                CmsResource folder = (CmsResource) subFolders.get(i);
                if (folder.getState() != CmsResource.STATE_DELETED) {
                    String export = getCms().getSitePath(folder);
                    if (checkExportResource(export)) {
                        long age = folder.getDateLastModified() < folder.getDateCreated() ? folder.getDateCreated() : folder.getDateLastModified();
                        if (age >= m_parameters.getContentAge()) {
                            appendResourceToManifest(folder, false);
                        }
                        addChildResources(getCms().getSitePath(folder));
                    }
                }
                subFolders.set(i, null);
            }
        } catch (CmsImportExportException e) {
            throw e;
        } catch (CmsException e) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_IMPORTEXPORT_ERROR_ADDING_CHILD_RESOURCES_1, folderName);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }
            throw new CmsImportExportException(message, e);
        }
    }

    /**
     * Adds all files in fileNames to the manifest.xml file.<p>
     * 
     * @param fileNames list of path Strings, e.g. <code>/folder/index.html</code>
     * 
     * @throws CmsImportExportException if something goes wrong
     * @throws IOException if a file could not be exported
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void addFiles(List fileNames) throws CmsImportExportException, IOException, SAXException {
        if (fileNames != null) {
            for (int i = 0; i < fileNames.size(); i++) {
                String fileName = (String) fileNames.get(i);
                try {
                    CmsFile file = getCms().readFile(fileName, CmsResourceFilter.IGNORE_EXPIRATION);
                    if (!file.getState().isDeleted() && !CmsWorkplace.isTemporaryFile(file)) {
                        if (checkExportResource(fileName)) {
                            if (m_parameters.isRecursive()) {
                                addParentFolders(fileName);
                            }
                            if (isInExportableProject(file)) {
                                exportFile(file);
                            }
                        }
                    }
                } catch (CmsImportExportException e) {
                    throw e;
                } catch (CmsException e) {
                    if (e instanceof CmsVfsException) {
                        CmsMessageContainer message = Messages.get().container(Messages.ERR_IMPORTEXPORT_ERROR_ADDING_FILE_1, fileName);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(message.key(), e);
                        }
                        throw new CmsImportExportException(message, e);
                    }
                }
            }
        }
    }

    /**
     * Adds the parent folders of the given resource to the config file, 
     * starting at the top, excluding the root folder.<p>
     * 
     * @param resourceName the name of a resource in the VFS
     * 
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void addParentFolders(String resourceName) throws CmsImportExportException, SAXException {
        try {
            if (!checkExportResource(resourceName)) {
                return;
            }
            if (m_superFolders == null) {
                m_superFolders = new ArrayList();
            }
            List superFolders = new ArrayList();
            if (resourceName.lastIndexOf("/") != (resourceName.length() - 1)) {
                resourceName = resourceName.substring(0, resourceName.lastIndexOf("/") + 1);
            }
            while (resourceName.length() > "/".length()) {
                superFolders.add(resourceName);
                resourceName = resourceName.substring(0, resourceName.length() - 1);
                resourceName = resourceName.substring(0, resourceName.lastIndexOf("/") + 1);
            }
            for (int i = superFolders.size() - 1; i >= 0; i--) {
                String addFolder = (String) superFolders.get(i);
                if (!m_superFolders.contains(addFolder)) {
                    CmsFolder folder = getCms().readFolder(addFolder, CmsResourceFilter.IGNORE_EXPIRATION);
                    appendResourceToManifest(folder, false);
                    m_superFolders.add(addFolder);
                }
            }
        } catch (CmsImportExportException e) {
            throw e;
        } catch (CmsException e) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_IMPORTEXPORT_ERROR_ADDING_PARENT_FOLDERS_1, resourceName);
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }
            throw new CmsImportExportException(message, e);
        }
    }

    /**
     * Adds a property node to the manifest.xml.<p>
     * 
     * @param propertiesElement the parent element to append the node to
     * @param propertyName the name of the property
     * @param propertyValue the value of the property
     * @param shared if <code>true</code>, add a shared property attribute to the generated property node
     */
    protected void addPropertyNode(Element propertiesElement, String propertyName, String propertyValue, boolean shared) {
        if (propertyValue != null) {
            Element propertyElement = propertiesElement.addElement(CmsImportVersion7.N_PROPERTY);
            if (shared) {
                propertyElement.addAttribute(CmsImportVersion7.A_TYPE, CmsImportVersion7.PROPERTY_ATTRIB_TYPE_SHARED);
            }
            propertyElement.addElement(CmsImportVersion7.N_NAME).addText(propertyName);
            propertyElement.addElement(CmsImportVersion7.N_VALUE).addCDATA(propertyValue);
        }
    }

    /**
     * Adds a relation node to the <code>manifest.xml</code>.<p>
     * 
     * @param relationsElement the parent element to append the node to
     * @param structureId the structure id of the target relation
     * @param sitePath the site path of the target relation
     * @param relationType the type of the relation
     */
    protected void addRelationNode(Element relationsElement, String structureId, String sitePath, String relationType) {
        if ((structureId != null) && (sitePath != null) && (relationType != null)) {
            Element relationElement = relationsElement.addElement(CmsImportVersion7.N_RELATION);
            relationElement.addElement(CmsImportVersion7.N_ID).addText(structureId);
            relationElement.addElement(CmsImportVersion7.N_PATH).addText(sitePath);
            relationElement.addElement(CmsImportVersion7.N_TYPE).addText(relationType);
        }
    }

    /**
     * Writes the data for a resource (like access-rights) to the <code>manifest.xml</code> file.<p>
     * 
     * @param resource the resource to get the data from
     * @param source flag to show if the source information in the xml file must be written
     * 
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void appendResourceToManifest(CmsResource resource, boolean source) throws CmsImportExportException, SAXException {
        try {
            String fileName = trimResourceName(getCms().getSitePath(resource));
            if (fileName.startsWith("system/orgunits")) {
                return;
            }
            Element fileElement = m_resourceNode.addElement(CmsImportVersion7.N_FILE);
            if (resource.isFile()) {
                if (source) {
                    fileElement.addElement(CmsImportVersion7.N_SOURCE).addText(fileName);
                }
            } else {
                m_exportCount++;
                I_CmsReport report = getReport();
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_SUCCESSION_1, String.valueOf(m_exportCount)), I_CmsReport.FORMAT_NOTE);
                report.print(Messages.get().container(Messages.RPT_EXPORT_0), I_CmsReport.FORMAT_NOTE);
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, getCms().getSitePath(resource)));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                report.println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0), I_CmsReport.FORMAT_OK);
                if (LOG.isInfoEnabled()) {
                    LOG.info(Messages.get().getBundle().key(Messages.LOG_EXPORTING_OK_2, String.valueOf(m_exportCount), getCms().getSitePath(resource)));
                }
            }
            fileElement.addElement(CmsImportVersion7.N_DESTINATION).addText(fileName);
            fileElement.addElement(CmsImportVersion7.N_TYPE).addText(OpenCms.getResourceManager().getResourceType(resource.getTypeId()).getTypeName());
            fileElement.addElement(CmsImportVersion7.N_UUIDSTRUCTURE).addText(resource.getStructureId().toString());
            if (resource.isFile()) {
                fileElement.addElement(CmsImportVersion7.N_UUIDRESOURCE).addText(resource.getResourceId().toString());
            }
            fileElement.addElement(CmsImportVersion7.N_DATELASTMODIFIED).addText(CmsDateUtil.getHeaderDate(resource.getDateLastModified()));
            String userNameLastModified = null;
            try {
                userNameLastModified = getCms().readUser(resource.getUserLastModified()).getName();
            } catch (CmsException e) {
                userNameLastModified = OpenCms.getDefaultUsers().getUserAdmin();
            }
            fileElement.addElement(CmsImportVersion7.N_USERLASTMODIFIED).addText(userNameLastModified);
            fileElement.addElement(CmsImportVersion7.N_DATECREATED).addText(CmsDateUtil.getHeaderDate(resource.getDateCreated()));
            String userNameCreated = null;
            try {
                userNameCreated = getCms().readUser(resource.getUserCreated()).getName();
            } catch (CmsException e) {
                userNameCreated = OpenCms.getDefaultUsers().getUserAdmin();
            }
            fileElement.addElement(CmsImportVersion7.N_USERCREATED).addText(userNameCreated);
            if (resource.getDateReleased() != CmsResource.DATE_RELEASED_DEFAULT) {
                fileElement.addElement(CmsImportVersion7.N_DATERELEASED).addText(CmsDateUtil.getHeaderDate(resource.getDateReleased()));
            }
            if (resource.getDateExpired() != CmsResource.DATE_EXPIRED_DEFAULT) {
                fileElement.addElement(CmsImportVersion7.N_DATEEXPIRED).addText(CmsDateUtil.getHeaderDate(resource.getDateExpired()));
            }
            int resFlags = resource.getFlags();
            resFlags &= ~CmsResource.FLAG_LABELED;
            fileElement.addElement(CmsImportVersion7.N_FLAGS).addText(Integer.toString(resFlags));
            Element propertiesElement = fileElement.addElement(CmsImportVersion7.N_PROPERTIES);
            List properties = getCms().readPropertyObjects(getCms().getSitePath(resource), false);
            Collections.sort(properties);
            for (int i = 0, n = properties.size(); i < n; i++) {
                CmsProperty property = (CmsProperty) properties.get(i);
                if (isIgnoredProperty(property)) {
                    continue;
                }
                addPropertyNode(propertiesElement, property.getName(), property.getStructureValue(), false);
                addPropertyNode(propertiesElement, property.getName(), property.getResourceValue(), true);
            }
            List relations = getCms().getRelationsForResource(getCms().getSitePath(resource), CmsRelationFilter.TARGETS.filterNotDefinedInContent());
            CmsRelation relation = null;
            Element relationsElement = fileElement.addElement(CmsImportVersion7.N_RELATIONS);
            for (Iterator iter = relations.iterator(); iter.hasNext(); ) {
                relation = (CmsRelation) iter.next();
                CmsResource target = relation.getTarget(getCms(), CmsResourceFilter.ALL);
                String structureId = target.getStructureId().toString();
                String sitePath = getCms().getSitePath(target);
                String relationType = relation.getType().getName();
                addRelationNode(relationsElement, structureId, sitePath, relationType);
            }
            Element acl = fileElement.addElement(CmsImportVersion7.N_ACCESSCONTROL_ENTRIES);
            List fileAcEntries = getCms().getAccessControlEntries(getCms().getSitePath(resource), false);
            Iterator i = fileAcEntries.iterator();
            while (i.hasNext()) {
                CmsAccessControlEntry ace = (CmsAccessControlEntry) i.next();
                Element a = acl.addElement(CmsImportVersion7.N_ACCESSCONTROL_ENTRY);
                int flags = ace.getFlags();
                String acePrincipalName = "";
                CmsUUID acePrincipal = ace.getPrincipal();
                if ((flags & CmsAccessControlEntry.ACCESS_FLAGS_ALLOTHERS) > 0) {
                    acePrincipalName = CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME;
                } else if ((flags & CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE_ALL) > 0) {
                    acePrincipalName = CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME;
                } else if ((flags & CmsAccessControlEntry.ACCESS_FLAGS_GROUP) > 0) {
                    acePrincipalName = getCms().readGroup(acePrincipal).getPrefixedName();
                } else if ((flags & CmsAccessControlEntry.ACCESS_FLAGS_USER) > 0) {
                    acePrincipalName = getCms().readUser(acePrincipal).getPrefixedName();
                } else {
                    acePrincipalName = CmsRole.PRINCIPAL_ROLE + "." + CmsRole.valueOfId(acePrincipal).getRoleName();
                }
                a.addElement(CmsImportVersion7.N_ACCESSCONTROL_PRINCIPAL).addText(acePrincipalName);
                a.addElement(CmsImportVersion7.N_FLAGS).addText(Integer.toString(flags));
                Element b = a.addElement(CmsImportVersion7.N_ACCESSCONTROL_PERMISSIONSET);
                b.addElement(CmsImportVersion7.N_ACCESSCONTROL_ALLOWEDPERMISSIONS).addText(Integer.toString(ace.getAllowedPermissions()));
                b.addElement(CmsImportVersion7.N_ACCESSCONTROL_DENIEDPERMISSIONS).addText(Integer.toString(ace.getDeniedPermissions()));
            }
            digestElement(m_resourceNode, fileElement);
        } catch (CmsImportExportException e) {
            throw e;
        } catch (CmsException e) {
            CmsMessageContainer message = Messages.get().container(Messages.ERR_IMPORTEXPORT_ERROR_APPENDING_RESOURCE_TO_MANIFEST_1, resource.getRootPath());
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }
            throw new CmsImportExportException(message, e);
        }
    }

    /**
     * Returns true if the checked resource name can be exported depending on the include settings.<p>
     * 
     * @param resourcename the absolute path of the resource
     * @return true if the checked resource name can be exported depending on the include settings
     */
    protected boolean checkExportResource(String resourcename) {
        return (!resourcename.startsWith(CmsWorkplace.VFS_PATH_SYSTEM) || resourcename.equalsIgnoreCase(CmsWorkplace.VFS_PATH_SYSTEM) || resourcename.startsWith(CmsWorkplace.VFS_PATH_GALLERIES) || (m_parameters.isIncludeSystemFolder() && resourcename.startsWith(CmsWorkplace.VFS_PATH_SYSTEM)));
    }

    /**
     * Closes the export ZIP file and saves the XML document for the manifest.<p>
     * 
     * @param exportNode the export root node
     * 
     * @throws SAXException if something goes wrong processing the manifest.xml
     * @throws IOException if something goes wrong while closing the export file
     */
    protected void closeExportFile(Element exportNode) throws IOException, SAXException {
        getSaxWriter().writeClose(exportNode);
        CmsXmlSaxWriter xmlSaxWriter = (CmsXmlSaxWriter) getSaxWriter().getContentHandler();
        xmlSaxWriter.endDocument();
        ZipEntry entry = new ZipEntry(CmsImportExportManager.EXPORT_MANIFEST);
        getExportZipStream().putNextEntry(entry);
        StringBuffer result = ((StringWriter) xmlSaxWriter.getWriter()).getBuffer();
        int steps = result.length() / SUB_LENGTH;
        int rest = result.length() % SUB_LENGTH;
        int pos = 0;
        for (int i = 0; i < steps; i++) {
            String sub = result.substring(pos, pos + SUB_LENGTH);
            getExportZipStream().write(sub.getBytes(OpenCms.getSystemInfo().getDefaultEncoding()));
            pos += SUB_LENGTH;
        }
        if (rest > 0) {
            String sub = result.substring(pos, pos + rest);
            getExportZipStream().write(sub.getBytes(OpenCms.getSystemInfo().getDefaultEncoding()));
        }
        getExportZipStream().closeEntry();
        getExportZipStream().close();
    }

    /**
     * Writes the output element to the XML output writer and detaches it 
     * from it's parent element.<p> 
     * 
     * @param parent the parent element
     * @param output the output element 
     * 
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void digestElement(Element parent, Element output) throws SAXException {
        m_saxWriter.write(output);
        parent.remove(output);
    }

    /**
     * Exports all resources and possible sub-folders form the provided list of resources.
     * 
     * @param parent the parent node to add the resources to
     * @param resourcesToExport the list of resources to export
     * 
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     * @throws IOException if not all resources could be appended to the ZIP archive
     */
    protected void exportAllResources(Element parent, List resourcesToExport) throws CmsImportExportException, IOException, SAXException {
        String resourceNodeName = getResourceNodeName();
        m_resourceNode = parent.addElement(resourceNodeName);
        getSaxWriter().writeOpen(m_resourceNode);
        if (m_parameters.isRecursive()) {
            resourcesToExport = CmsFileUtil.removeRedundancies(resourcesToExport);
        }
        List folderNames = new ArrayList();
        List fileNames = new ArrayList();
        Iterator it = resourcesToExport.iterator();
        while (it.hasNext()) {
            String resource = (String) it.next();
            if (CmsResource.isFolder(resource)) {
                folderNames.add(resource);
            } else {
                fileNames.add(resource);
            }
        }
        m_exportedResources = new HashSet();
        for (int i = 0; i < folderNames.size(); i++) {
            String path = (String) folderNames.get(i);
            if (m_parameters.isRecursive()) {
                addParentFolders(path);
                addChildResources(path);
            } else {
                CmsFolder folder;
                try {
                    folder = getCms().readFolder(path, CmsResourceFilter.IGNORE_EXPIRATION);
                } catch (CmsException e) {
                    CmsMessageContainer message = Messages.get().container(Messages.ERR_IMPORTEXPORT_ERROR_ADDING_PARENT_FOLDERS_1, path);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(message.key(), e);
                    }
                    throw new CmsImportExportException(message, e);
                }
                CmsResourceState state = folder.getState();
                long age = folder.getDateLastModified() < folder.getDateCreated() ? folder.getDateCreated() : folder.getDateLastModified();
                if (getCms().getRequestContext().currentProject().isOnlineProject() || (m_parameters.isIncludeUnchangedResources()) || state.isNew() || state.isChanged()) {
                    if (!state.isDeleted() && (age >= m_parameters.getContentAge())) {
                        String export = getCms().getSitePath(folder);
                        if (checkExportResource(export)) {
                            appendResourceToManifest(folder, false);
                        }
                    }
                }
            }
        }
        addFiles(fileNames);
        getSaxWriter().writeClose(m_resourceNode);
        parent.remove(m_resourceNode);
        m_resourceNode = null;
    }

    /**
     * Exports one single file with all its data and content.<p>
     *
     * @param file the file to be exported
     * 
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     * @throws IOException if the ZIP entry for the file could be appended to the ZIP archive
     */
    protected void exportFile(CmsFile file) throws CmsImportExportException, SAXException, IOException {
        String source = trimResourceName(getCms().getSitePath(file));
        I_CmsReport report = getReport();
        m_exportCount++;
        report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_SUCCESSION_1, String.valueOf(m_exportCount)), I_CmsReport.FORMAT_NOTE);
        report.print(Messages.get().container(Messages.RPT_EXPORT_0), I_CmsReport.FORMAT_NOTE);
        report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, getCms().getSitePath(file)));
        report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
        if (!m_exportedResources.contains(file.getResourceId())) {
            ZipEntry entry = new ZipEntry(source);
            entry.setTime(file.getDateLastModified());
            getExportZipStream().putNextEntry(entry);
            getExportZipStream().write(file.getContents());
            getExportZipStream().closeEntry();
            m_exportedResources.add(file.getResourceId());
            appendResourceToManifest(file, true);
        } else {
            appendResourceToManifest(file, false);
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key(Messages.LOG_EXPORTING_OK_2, String.valueOf(m_exportCount), source));
        }
        report.println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0), I_CmsReport.FORMAT_OK);
    }

    /**
     * Exports one single group with all it's data.<p>
     *
     * @param parent the parent node to add the groups to
     * @param group the group to be exported
     * 
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void exportGroup(Element parent, CmsGroup group) throws CmsImportExportException, SAXException {
        try {
            String parentgroup;
            if ((group.getParentId() == null) || group.getParentId().isNullUUID()) {
                parentgroup = "";
            } else {
                parentgroup = getCms().getParent(group.getName()).getName();
            }
            Element e = parent.addElement(CmsImportVersion7.N_GROUP);
            e.addElement(CmsImportVersion7.N_NAME).addText(group.getSimpleName());
            e.addElement(CmsImportVersion7.N_DESCRIPTION).addCDATA(group.getDescription());
            e.addElement(CmsImportVersion7.N_FLAGS).addText(Integer.toString(group.getFlags()));
            e.addElement(CmsImportVersion7.N_PARENTGROUP).addText(parentgroup);
            digestElement(parent, e);
        } catch (CmsException e) {
            CmsMessageContainer message = org.opencms.db.Messages.get().container(org.opencms.db.Messages.ERR_GET_PARENT_GROUP_1, group.getName());
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }
            throw new CmsImportExportException(message, e);
        }
    }

    /**
     * Exports all groups of the given organizational unit.<p>
     *
     * @param parent the parent node to add the groups to
     * @param orgunit the organizational unit to write the groups for
     * 
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void exportGroups(Element parent, CmsOrganizationalUnit orgunit) throws CmsImportExportException, SAXException {
        try {
            I_CmsReport report = getReport();
            List allGroups = OpenCms.getOrgUnitManager().getGroups(getCms(), orgunit.getName(), false);
            for (int i = 0, l = allGroups.size(); i < l; i++) {
                CmsGroup group = (CmsGroup) allGroups.get(i);
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_SUCCESSION_2, String.valueOf(i + 1), String.valueOf(l)), I_CmsReport.FORMAT_NOTE);
                report.print(Messages.get().container(Messages.RPT_EXPORT_GROUP_0), I_CmsReport.FORMAT_NOTE);
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, group.getName()));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                exportGroup(parent, group);
                report.println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0), I_CmsReport.FORMAT_OK);
            }
        } catch (CmsImportExportException e) {
            throw e;
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
            throw new CmsImportExportException(e.getMessageContainer(), e);
        }
    }

    /**
     * Exports one single organizational unit with all it's data.<p>
     *
     * @param parent the parent node to add the groups to
     * @param orgunit the group to be exported
     * 
     * @throws SAXException if something goes wrong processing the manifest.xml
     * @throws CmsException if something goes wrong reading the data to export
     */
    protected void exportOrgUnit(Element parent, CmsOrganizationalUnit orgunit) throws SAXException, CmsException {
        Element orgunitElement = parent.addElement(CmsImportVersion7.N_ORGUNIT);
        getSaxWriter().writeOpen(orgunitElement);
        Element name = orgunitElement.addElement(CmsImportVersion7.N_NAME).addText(orgunit.getName());
        digestElement(orgunitElement, name);
        Element description = orgunitElement.addElement(CmsImportVersion7.N_DESCRIPTION).addCDATA(orgunit.getDescription());
        digestElement(orgunitElement, description);
        Element flags = orgunitElement.addElement(CmsImportVersion7.N_FLAGS).addText(Integer.toString(orgunit.getFlags()));
        digestElement(orgunitElement, flags);
        Element resources = orgunitElement.addElement(CmsImportVersion7.N_RESOURCES);
        Iterator it = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(getCms(), orgunit.getName()).iterator();
        while (it.hasNext()) {
            CmsResource resource = (CmsResource) it.next();
            resources.addElement(CmsImportVersion7.N_RESOURCE).addText(resource.getRootPath());
        }
        digestElement(orgunitElement, resources);
        getReport().println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0), I_CmsReport.FORMAT_OK);
        Element groupsElement = parent.addElement(CmsImportVersion7.N_GROUPS);
        getSaxWriter().writeOpen(groupsElement);
        exportGroups(groupsElement, orgunit);
        getSaxWriter().writeClose(groupsElement);
        Element usersElement = parent.addElement(CmsImportVersion7.N_USERS);
        getSaxWriter().writeOpen(usersElement);
        exportUsers(usersElement, orgunit);
        getSaxWriter().writeClose(usersElement);
        getSaxWriter().writeClose(orgunitElement);
    }

    /**
     * Exports all organizational units with all data.<p>
     *
     * @param parent the parent node to add the organizational units to
     * 
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void exportOrgUnits(Element parent) throws CmsImportExportException, SAXException {
        try {
            Element orgunitsElement = parent.addElement(CmsImportVersion7.N_ORGUNITS);
            getSaxWriter().writeOpen(orgunitsElement);
            I_CmsReport report = getReport();
            List allOUs = new ArrayList();
            allOUs.add(OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), ""));
            allOUs.addAll(OpenCms.getOrgUnitManager().getOrganizationalUnits(getCms(), "", true));
            for (int i = 0; i < allOUs.size(); i++) {
                CmsOrganizationalUnit ou = (CmsOrganizationalUnit) allOUs.get(i);
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_SUCCESSION_2, String.valueOf(i + 1), String.valueOf(allOUs.size())), I_CmsReport.FORMAT_NOTE);
                report.print(Messages.get().container(Messages.RPT_EXPORT_ORGUNIT_0), I_CmsReport.FORMAT_NOTE);
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, ou.getName()));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                exportOrgUnit(orgunitsElement, ou);
            }
            getSaxWriter().writeClose(orgunitsElement);
        } catch (CmsImportExportException e) {
            throw e;
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
            throw new CmsImportExportException(e.getMessageContainer(), e);
        }
    }

    /**
     * Exports one single project with all it's data.<p>
     *
     * @param parent the parent node to add the project to
     * @param project the project to be exported
     * 
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void exportProject(Element parent, CmsProject project) throws CmsImportExportException, SAXException {
        String users;
        try {
            users = getCms().readGroup(project.getGroupId()).getName();
        } catch (CmsException e) {
            CmsMessageContainer message = org.opencms.db.Messages.get().container(org.opencms.db.Messages.ERR_READ_GROUP_FOR_ID_1, project.getGroupId());
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }
            throw new CmsImportExportException(message, e);
        }
        String managers;
        try {
            managers = getCms().readGroup(project.getManagerGroupId()).getName();
        } catch (CmsException e) {
            CmsMessageContainer message = org.opencms.db.Messages.get().container(org.opencms.db.Messages.ERR_READ_GROUP_FOR_ID_1, project.getManagerGroupId());
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), e);
            }
            throw new CmsImportExportException(message, e);
        }
        Element e = parent.addElement(CmsImportVersion7.N_PROJECT);
        e.addElement(CmsImportVersion7.N_NAME).addText(project.getSimpleName());
        e.addElement(CmsImportVersion7.N_DESCRIPTION).addCDATA(project.getDescription());
        e.addElement(CmsImportVersion7.N_USERSGROUP).addText(users);
        e.addElement(CmsImportVersion7.N_MANAGERSGROUP).addText(managers);
        Element resources = e.addElement(CmsImportVersion7.N_RESOURCES);
        try {
            Iterator it = getCms().readProjectResources(project).iterator();
            while (it.hasNext()) {
                String resName = (String) it.next();
                resources.addElement(CmsImportVersion7.N_RESOURCE).addText(resName);
            }
        } catch (CmsException exc) {
            CmsMessageContainer message = org.opencms.db.Messages.get().container(org.opencms.db.Messages.ERR_READ_PROJECT_RESOURCES_2, project.getName(), project.getUuid());
            if (LOG.isDebugEnabled()) {
                LOG.debug(message.key(), exc);
            }
            throw new CmsImportExportException(message, exc);
        }
        digestElement(parent, e);
    }

    /**
     * Exports all projects with all data.<p>
     *
     * @param parent the parent node to add the projects to
     * 
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void exportProjects(Element parent) throws CmsImportExportException, SAXException {
        try {
            I_CmsReport report = getReport();
            List allProjects = OpenCms.getOrgUnitManager().getAllManageableProjects(getCms(), "", true);
            for (int i = 0; i < allProjects.size(); i++) {
                CmsProject project = (CmsProject) allProjects.get(i);
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_SUCCESSION_2, String.valueOf(i + 1), String.valueOf(allProjects.size())), I_CmsReport.FORMAT_NOTE);
                report.print(Messages.get().container(Messages.RPT_EXPORT_PROJECT_0), I_CmsReport.FORMAT_NOTE);
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, project.getName()));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                exportProject(parent, project);
                report.println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0), I_CmsReport.FORMAT_OK);
            }
        } catch (CmsImportExportException e) {
            throw e;
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
            throw new CmsImportExportException(e.getMessageContainer(), e);
        }
    }

    /**
     * Exports one single user with all its data.<p>
     * 
     * @param parent the parent node to add the users to
     * @param user the user to be exported
     * 
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void exportUser(Element parent, CmsUser user) throws CmsImportExportException, SAXException {
        try {
            Element e = parent.addElement(CmsImportVersion7.N_USER);
            e.addElement(CmsImportVersion7.N_NAME).addText(user.getSimpleName());
            String passwd = new String(Base64.encodeBase64(user.getPassword().getBytes()));
            e.addElement(CmsImportVersion7.N_PASSWORD).addCDATA(passwd);
            e.addElement(CmsImportVersion7.N_FIRSTNAME).addText(user.getFirstname());
            e.addElement(CmsImportVersion7.N_LASTNAME).addText(user.getLastname());
            e.addElement(CmsImportVersion7.N_EMAIL).addText(user.getEmail());
            e.addElement(CmsImportVersion7.N_FLAGS).addText(Integer.toString(user.getFlags()));
            e.addElement(CmsImportVersion7.N_DATECREATED).addText(Long.toString(user.getDateCreated()));
            Element userInfoNode = e.addElement(CmsImportVersion7.N_USERINFO);
            List keys = new ArrayList(user.getAdditionalInfo().keySet());
            Collections.sort(keys);
            Iterator itInfoKeys = keys.iterator();
            while (itInfoKeys.hasNext()) {
                String key = (String) itInfoKeys.next();
                if (key == null) {
                    continue;
                }
                Object value = user.getAdditionalInfo(key);
                if (value == null) {
                    continue;
                }
                Element entryNode = userInfoNode.addElement(CmsImportVersion7.N_USERINFO_ENTRY);
                entryNode.addAttribute(CmsImportVersion7.A_NAME, key);
                entryNode.addAttribute(CmsImportVersion7.A_TYPE, value.getClass().getName());
                try {
                    entryNode.addCDATA(CmsDataTypeUtil.dataExport(value));
                } catch (IOException ioe) {
                    getReport().println(ioe);
                    if (LOG.isErrorEnabled()) {
                        LOG.error(Messages.get().getBundle().key(Messages.ERR_IMPORTEXPORT_ERROR_EXPORTING_USER_1, user.getName()), ioe);
                    }
                }
            }
            Element userRoles = e.addElement(CmsImportVersion7.N_USERROLES);
            List roles = OpenCms.getRoleManager().getRolesOfUser(getCms(), user.getName(), "", true, true, true);
            for (int i = 0; i < roles.size(); i++) {
                String roleName = ((CmsRole) roles.get(i)).getFqn();
                userRoles.addElement(CmsImportVersion7.N_USERROLE).addText(roleName);
            }
            Element userGroups = e.addElement(CmsImportVersion7.N_USERGROUPS);
            List groups = getCms().getGroupsOfUser(user.getName(), true, true);
            for (int i = 0; i < groups.size(); i++) {
                String groupName = ((CmsGroup) groups.get(i)).getName();
                userGroups.addElement(CmsImportVersion7.N_USERGROUP).addText(groupName);
            }
            digestElement(parent, e);
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
            throw new CmsImportExportException(e.getMessageContainer(), e);
        }
    }

    /**
     * Exports all users of the given organizational unit.<p>
     *
     * @param parent the parent node to add the users to
     * @param orgunit the organizational unit to write the groups for
     * 
     * @throws CmsImportExportException if something goes wrong
     * @throws SAXException if something goes wrong processing the manifest.xml
     */
    protected void exportUsers(Element parent, CmsOrganizationalUnit orgunit) throws CmsImportExportException, SAXException {
        try {
            I_CmsReport report = getReport();
            List allUsers = OpenCms.getOrgUnitManager().getUsers(getCms(), orgunit.getName(), false);
            for (int i = 0, l = allUsers.size(); i < l; i++) {
                CmsUser user = (CmsUser) allUsers.get(i);
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_SUCCESSION_2, String.valueOf(i + 1), String.valueOf(l)), I_CmsReport.FORMAT_NOTE);
                report.print(Messages.get().container(Messages.RPT_EXPORT_USER_0), I_CmsReport.FORMAT_NOTE);
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_ARGUMENT_1, user.getName()));
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0));
                exportUser(parent, user);
                report.println(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_OK_0), I_CmsReport.FORMAT_OK);
            }
        } catch (CmsImportExportException e) {
            throw e;
        } catch (CmsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getLocalizedMessage(), e);
            }
            throw new CmsImportExportException(e.getMessageContainer(), e);
        }
    }

    /**
     * Returns the OpenCms context object this export was initialized with.<p>
     * 
     * @return the OpenCms context object this export was initialized with
     */
    protected CmsObject getCms() {
        return m_cms;
    }

    /**
     * Returns the name of the export file.<p>
     * 
     * @return the name of the export file
     */
    protected String getExportFileName() {
        return m_parameters.getPath();
    }

    /**
     * Returns the name of the main export node.<p>
     * 
     * @return the name of the main export node
     */
    protected String getExportNodeName() {
        return CmsImportExportManager.N_EXPORT;
    }

    /**
     * Returns the zip output stream to write to.<p>
     * 
     * @return the zip output stream to write to
     */
    protected ZipOutputStream getExportZipStream() {
        return m_exportZipStream;
    }

    /**
     * Returns the report to write progress messages to.<p>
     * 
     * @return the report to write progress messages to
     */
    protected I_CmsReport getReport() {
        return m_report;
    }

    /**
     * Returns the name for the main resource node.<p>
     * 
     * @return the name for the main resource node
     */
    protected String getResourceNodeName() {
        return "files";
    }

    /**
     * Returns the SAX based xml writer to write the XML output to.<p>
     * 
     * @return the SAX based xml writer to write the XML output to
     */
    protected SAXWriter getSaxWriter() {
        return m_saxWriter;
    }

    /**
     * Checks if a property should be written to the export or not.<p>
     * 
     * @param property the property to check
     * 
     * @return if true, the property is to be ignored, otherwise it should be exported
     */
    protected boolean isIgnoredProperty(CmsProperty property) {
        if (property == null) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a resource is belongs to the correct project for exporting.<p>
     * 
     * @param res the resource to check
     * 
     * @return <code>true</code>, if the resource can be exported, false otherwise
     */
    protected boolean isInExportableProject(CmsResource res) {
        boolean retValue = true;
        if (m_parameters.isInProject()) {
            if ((res.getState() == CmsResource.STATE_CHANGED) || (res.getState() == CmsResource.STATE_NEW)) {
                if (!res.getProjectLastModified().equals(getCms().getRequestContext().currentProject().getUuid())) {
                    retValue = false;
                }
            } else {
                retValue = false;
            }
        }
        return retValue;
    }

    /**
     * Opens the export ZIP file and initializes the internal XML document for the manifest.<p>
     * 
     * @return the node in the XML document where all files are appended to
     * 
     * @throws SAXException if something goes wrong processing the manifest.xml
     * @throws IOException if something goes wrong while closing the export file
     */
    protected Element openExportFile() throws IOException, SAXException {
        setExportZipStream(new ZipOutputStream(new FileOutputStream(getExportFileName())));
        CmsXmlSaxWriter saxHandler = new CmsXmlSaxWriter(new StringWriter(4096), OpenCms.getSystemInfo().getDefaultEncoding());
        saxHandler.setEscapeXml(true);
        saxHandler.setEscapeUnknownChars(true);
        setSaxWriter(new SAXWriter(saxHandler, saxHandler));
        Document doc = DocumentHelper.createDocument();
        saxHandler.startDocument();
        if (m_parameters.isXmlValidation()) {
            saxHandler.startDTD(getExportNodeName(), null, CmsConfigurationManager.DEFAULT_DTD_PREFIX + CmsImportVersion7.DTD_FILENAME);
            saxHandler.endDTD();
        }
        String exportNodeName = getExportNodeName();
        Element exportNode = doc.addElement(exportNodeName);
        getSaxWriter().writeOpen(exportNode);
        Element info = exportNode.addElement(CmsImportExportManager.N_INFO);
        info.addElement(CmsImportExportManager.N_CREATOR).addText(getCms().getRequestContext().currentUser().getName());
        info.addElement(CmsImportExportManager.N_OC_VERSION).addText(OpenCms.getSystemInfo().getVersionNumber());
        info.addElement(CmsImportExportManager.N_DATE).addText(CmsDateUtil.getHeaderDate(System.currentTimeMillis()));
        info.addElement(CmsImportExportManager.N_INFO_PROJECT).addText(getCms().getRequestContext().currentProject().getName());
        info.addElement(CmsImportExportManager.N_VERSION).addText(CmsImportExportManager.EXPORT_VERSION);
        digestElement(exportNode, info);
        return exportNode;
    }

    /**
     * Sets the zip output stream to write to.<p>
     * 
     * @param exportZipStream the zip output stream to write to
     */
    protected void setExportZipStream(ZipOutputStream exportZipStream) {
        m_exportZipStream = exportZipStream;
    }

    /**
     * Sets the SAX based xml writer to write the XML output to.<p>
     * 
     * @param saxWriter the SAX based xml writer to write the XML output to
     */
    protected void setSaxWriter(SAXWriter saxWriter) {
        m_saxWriter = saxWriter;
    }

    /**
     * Cuts leading and trailing '/' from the given resource name.<p>
     * 
     * @param resourceName the absolute path of a resource
     * 
     * @return the trimmed resource name
     */
    protected String trimResourceName(String resourceName) {
        if (resourceName.startsWith("/")) {
            resourceName = resourceName.substring(1);
        }
        if (resourceName.endsWith("/")) {
            resourceName = resourceName.substring(0, resourceName.length() - 1);
        }
        return resourceName;
    }
}
