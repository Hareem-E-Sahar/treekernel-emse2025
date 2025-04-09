package com.dcivision.dms.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.slide.common.ServiceAccessException;
import org.apache.slide.common.SlideException;
import org.apache.slide.security.AccessDeniedException;
import org.apache.slide.structure.ObjectAlreadyExistsException;
import org.apache.slide.structure.ObjectNotFoundException;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import com.dcivision.alert.bean.UpdateAlert;
import com.dcivision.alert.core.AdapterMaster;
import com.dcivision.alert.core.AlertManager;
import com.dcivision.audit.AuditTrailConstant;
import com.dcivision.audit.core.AuditTrailManager;
import com.dcivision.dms.DmsErrorConstant;
import com.dcivision.dms.DmsOperationConstant;
import com.dcivision.dms.bean.DmsContent;
import com.dcivision.dms.bean.DmsDocument;
import com.dcivision.dms.bean.DmsDocumentDetail;
import com.dcivision.dms.bean.DmsRoot;
import com.dcivision.dms.bean.DmsValidation;
import com.dcivision.dms.bean.DmsVersion;
import com.dcivision.dms.bean.MtmDocumentRelationship;
import com.dcivision.dms.dao.DmsDocumentDAObject;
import com.dcivision.dms.dao.DmsDocumentDetailDAObject;
import com.dcivision.dms.dao.DmsRootDAObject;
import com.dcivision.dms.dao.MtmDocumentRelationshipDAObject;
import com.dcivision.framework.AdapterMasterFactory;
import com.dcivision.framework.ApplicationException;
import com.dcivision.framework.DataSourceFactory;
import com.dcivision.framework.GlobalConstant;
import com.dcivision.framework.PermissionManager;
import com.dcivision.framework.SessionContainer;
import com.dcivision.framework.SystemFunctionConstant;
import com.dcivision.framework.SystemParameterConstant;
import com.dcivision.framework.SystemParameterFactory;
import com.dcivision.framework.TextUtility;
import com.dcivision.framework.Utility;

/**
 * <p>Class Name:       WebdavOperationManager.java    </p>
 * <p>Description:      Webdav operation manager .</p>
 * @author              Beyond Qu
 * @company             DCIVision Limited
 * @creation date       19/11/2004
 * @version             $Revision: 1.28.2.18 $
 */
public class WebdavOperationManager {

    public static final String REVISION = "$Revision: 1.28.2.18 $";

    public static final String DMS_WEBDAV_ROOT_FOLDER_URL = "/files";

    public static final String DMS_WEBDAV_PERSONAL_ROOT_FOLDER_URL = "/files/Personal";

    public static final String DMS_WEBDAV_PUBLIC_ROOT_FOLDER_URL = "/files/Public";

    public static final String DMS_WEBDAV_PERSONAL_ROOT_FOLDER_NAME = "Personal";

    public static final String DMS_WEBDAV_PUBLIC_ROOT_FOLDER_NAME = "Public";

    public static final String DMS_PERMISSION_READ_CODE = "R";

    public static final String DMS_PERMISSION_CREATE_FOLDER_CODE = "F";

    public static final String DMS_PERMISSION_CREATE_DOCUMENT_CODE = "I";

    public static final String DMS_PERMISSION_COPY_CODE = "C";

    public static final String DMS_PERMISSION_MOVE_CODE = "M";

    public static final String DMS_PERMISSION_DELETE_CODE = "D";

    public static final String DMS_PERMISSION_RENAME_CODE = "N";

    public static final String DMS_PERMISSION_UPDATE_CODE = "T";

    private String MOVE_METHED_NAME = "MOVE";

    private String COPY_METHED_NAME = "COPY";

    private String GET_METHED_NAME = "GET";

    private String SHORTCUT_EXTENSION = ".lnk";

    private String COMPOUNT_DOC_ZIP_EXTENSION = "_comp.zip";

    private SessionContainer sessionContainer;

    protected Log log = LogFactory.getLog(this.getClass().getName());

    /**
   * Constructor - Creates a new instance of WebdavOperationManager
   * @param sessionContainer
   * @param conn
   */
    public WebdavOperationManager(SessionContainer sessionContainer, Connection conn) {
        this.sessionContainer = sessionContainer;
    }

    /** Use url get mapping document object
   * 
   * @param url webdav        url  
   * @return DmsDocument      mapping document
   * @throws Exception
   */
    public DmsDocument getMappingDocumentByUrl(String url) throws ObjectNotFoundException {
        boolean isLink = false;
        if (url.lastIndexOf(SHORTCUT_EXTENSION) == url.length() - 4) {
            isLink = true;
            url = url.substring(0, (url.length() - 4));
        }
        if (url.lastIndexOf(COMPOUNT_DOC_ZIP_EXTENSION) == url.length() - 9) {
            url = url.substring(0, (url.length() - 9));
        }
        DmsDocument dmsDocument = null;
        String[] urlArr = TextUtility.splitString(url, "/");
        Connection conn = null;
        try {
            conn = DataSourceFactory.getConnection();
            DmsDocumentDAObject dmsDocumentDAObject = new DmsDocumentDAObject(sessionContainer, conn);
            DocumentRetrievalManager documentRetrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
            Integer rootID = new Integer(-1);
            boolean isPersonal = false;
            RootOperationManager rootOperationManager = new RootOperationManager(this.sessionContainer, conn);
            Integer userID = sessionContainer.getUserRecordID();
            DmsRootDAObject rootDAO = new DmsRootDAObject(this.sessionContainer, conn);
            List rootList = rootDAO.getRootByOwnerIDAndType(userID, DmsRoot.PERSONAL_ROOT, GlobalConstant.RECORD_STATUS_ACTIVE);
            DmsRoot rootPoint = null;
            if (rootList.size() == 0) {
                rootPoint = rootOperationManager.createDefaultPersonalRootPointForUser();
            } else {
                rootPoint = (DmsRoot) rootList.get(0);
            }
            if (url.indexOf(DMS_WEBDAV_PERSONAL_ROOT_FOLDER_URL) == 0) {
                if (rootPoint != null) {
                    rootID = rootPoint.getID();
                    isPersonal = true;
                }
            }
            if (url.equals(DMS_WEBDAV_PERSONAL_ROOT_FOLDER_URL) || url.equals(DMS_WEBDAV_PUBLIC_ROOT_FOLDER_URL)) {
                return (DmsDocument) dmsDocumentDAObject.getObjectByID(rootPoint.getRootFolderID());
            }
            if (urlArr.length > 0) {
                DmsDocument tmpDmsDocument = null;
                for (int i = 0; i < urlArr.length; i++) {
                    if (i == 2) {
                        if (isPersonal) {
                            if (rootPoint != null) {
                                tmpDmsDocument = (DmsDocument) dmsDocumentDAObject.getObjectByID(rootPoint.getRootFolderID());
                            }
                        }
                    }
                    if (i == 3) {
                        if (isPersonal) {
                            if (tmpDmsDocument != null) {
                                if (urlArr.length - 1 != i) {
                                    tmpDmsDocument = documentRetrievalManager.getDocumentByNameParentID(urlArr[i], tmpDmsDocument.getID());
                                } else {
                                    if (isLink) {
                                        tmpDmsDocument = documentRetrievalManager.getDocumentByNameParentID(urlArr[i], tmpDmsDocument.getID(), "L");
                                    } else {
                                        tmpDmsDocument = documentRetrievalManager.getDocumentByNameParentID(urlArr[i], tmpDmsDocument.getID(), "*");
                                    }
                                }
                            }
                        } else {
                            rootPoint = (DmsRoot) rootDAO.getObjectByName(urlArr[i]);
                            if (rootPoint != null) {
                                tmpDmsDocument = (DmsDocument) dmsDocumentDAObject.getObjectByID(rootPoint.getRootFolderID());
                            }
                        }
                    }
                    if (i > 3) {
                        if (tmpDmsDocument != null) {
                            if (isLink && urlArr.length - 1 == i) {
                                tmpDmsDocument = documentRetrievalManager.getDocumentByNameParentID(urlArr[i], tmpDmsDocument.getID(), "L");
                            } else {
                                tmpDmsDocument = documentRetrievalManager.getDocumentByNameParentID(urlArr[i], tmpDmsDocument.getID(), "*");
                            }
                        }
                    }
                }
                dmsDocument = tmpDmsDocument;
            }
            return dmsDocument;
        } catch (Exception e) {
            log.error(e, e);
            throw new ObjectNotFoundException(url);
        } finally {
            closeConnection(conn);
        }
    }

    /** Get a foler's childrents(folder and document)
   * 
   * @param url               webdav url 
   * @return String[]         childrent's document id
   * @throws Exception
   */
    public String[] getChildrenNames(String url) throws Exception {
        String[] childrenNames = new String[0];
        List childList = new ArrayList();
        Connection conn = null;
        try {
            conn = DataSourceFactory.getConnection();
            DmsDocumentDAObject dmsDocumentDAO = new DmsDocumentDAObject(sessionContainer, conn);
            if (DMS_WEBDAV_ROOT_FOLDER_URL.equals(url)) {
                PermissionManager permManager = sessionContainer.getPermissionManager();
                if (permManager.hasAccessRight(SystemFunctionConstant.DMS_PERSONAL_FOLDER, DMS_PERMISSION_READ_CODE)) {
                    childList.add(DMS_WEBDAV_PERSONAL_ROOT_FOLDER_NAME);
                }
                if (permManager.hasAccessRight(SystemFunctionConstant.DMS_PUBLIC_FOLDER, DMS_PERMISSION_READ_CODE)) {
                    childList.add(DMS_WEBDAV_PUBLIC_ROOT_FOLDER_NAME);
                }
            } else if (DMS_WEBDAV_PERSONAL_ROOT_FOLDER_URL.equals(url)) {
                DmsDocument documentRoot = getMappingDocumentByUrl(url);
                List folderTreeList = dmsDocumentDAO.getListByParentIDRecordStatus(documentRoot.getID(), documentRoot.getRootID(), "*", GlobalConstant.RECORD_STATUS_ACTIVE);
                for (int i = 0; i < folderTreeList.size(); i++) {
                    DmsDocument tempDoc = (DmsDocument) folderTreeList.get(i);
                    if (!DmsVersion.ARCHIVED_STATUS.equals(tempDoc.getItemStatus()) && !isLinkOrCompoundDocType(tempDoc.getDocumentType())) {
                        childList.add(tempDoc.getDocumentName());
                    }
                }
            } else if (DMS_WEBDAV_PUBLIC_ROOT_FOLDER_URL.equals(url)) {
                DmsRootDAObject rootDAO = new DmsRootDAObject(this.sessionContainer, conn);
                List folderTreeList = rootDAO.getPublicRootList();
                for (int i = 0; i < folderTreeList.size(); i++) {
                    String sName = ((DmsRoot) folderTreeList.get(i)).getRootName();
                    DmsDocument oTempDoc = dmsDocumentDAO.getDocumentByNameParentID(sName, new Integer(0), "*");
                    if (oTempDoc == null) {
                        continue;
                    }
                    if (checkPermission(oTempDoc.getID(), DMS_PERMISSION_READ_CODE)) {
                        childList.add(sName);
                    }
                }
            } else {
                DmsDocument dmsDocument = getMappingDocumentByUrl(url);
                if (checkPermission(dmsDocument.getID(), DMS_PERMISSION_READ_CODE) == false) {
                    return childrenNames;
                }
                List folderTreeList = dmsDocumentDAO.getListByParentIDRecordStatus(dmsDocument.getID(), dmsDocument.getRootID(), "*", GlobalConstant.RECORD_STATUS_ACTIVE);
                for (int i = 0; i < folderTreeList.size(); i++) {
                    DmsDocument sTempDoc = (DmsDocument) folderTreeList.get(i);
                    if (isExpired(sTempDoc) || isNotYetEffective(sTempDoc) || DmsVersion.ARCHIVED_STATUS.equals(sTempDoc.getItemStatus()) || isLinkOrCompoundDocType(sTempDoc.getDocumentType())) {
                        continue;
                    }
                    if (checkPermission(sTempDoc.getID(), DMS_PERMISSION_READ_CODE)) {
                        childList.add(sTempDoc.getDocumentName());
                    }
                }
            }
        } catch (Exception e) {
            log.error(e);
            throw new Exception(e);
        } finally {
            closeConnection(conn);
        }
        childrenNames = new String[childList.size()];
        childrenNames = (String[]) childList.toArray(childrenNames);
        return childrenNames;
    }

    public boolean isLinkOrCompoundDocType(String docType) {
        if (DmsDocument.COMPOUND_DOC_TYPE.equals(docType) || DmsDocument.DOCUMENT_LINK.equals(docType)) {
            return true;
        }
        return false;
    }

    /** Check a url is root folder object
   * 
   * @param url               webdav url 
   * @return boolean          check result    
   * @throws Exception
   */
    public boolean isRootFolder(String url) throws Exception {
        boolean isFolderFlag = true;
        try {
            if (isRootOrPesonalOrPublicFolder(url)) {
                isFolderFlag = true;
            } else {
                DmsDocument dmsDocument = getMappingDocumentByUrl(url);
                if (dmsDocument != null && dmsDocument.getParentID().intValue() == 0) {
                    isFolderFlag = true;
                } else {
                    isFolderFlag = false;
                }
            }
        } catch (Exception e) {
            log.error(e);
            throw new Exception(e);
        }
        return isFolderFlag;
    }

    /**
   * Check a url is folder object
   * @param url               webdav url 
   * @return boolean          check result    
   * @throws Exception
   */
    public boolean isFolder(String url) throws Exception {
        boolean isFolderFlag = true;
        try {
            if (isRootOrPesonalOrPublicFolder(url)) {
                isFolderFlag = true;
            } else {
                DmsDocument dmsDocument = getMappingDocumentByUrl(url);
                if (dmsDocument != null && DmsDocument.FOLDER_TYPE.equals(dmsDocument.getDocumentType())) {
                    isFolderFlag = true;
                } else {
                    isFolderFlag = false;
                }
            }
        } catch (Exception e) {
            log.error(e);
            throw new Exception(e);
        }
        return isFolderFlag;
    }

    /** Check a url is exitsts at paradoc
   * 
   * @param url                               webdav url 
   * @return boolean                          check result    
   * @throws ServiceAccessException
   * @throws AccessDeniedException
   */
    public boolean objectExists(String url) throws ServiceAccessException, AccessDeniedException {
        boolean objectExistsFlag = false;
        try {
            if (isRootOrPesonalOrPublicFolder(url)) {
                objectExistsFlag = true;
            } else if (getMappingDocumentByUrl(url) != null) {
                objectExistsFlag = true;
            }
        } catch (Exception e) {
            log.error(e);
            throw new AccessDeniedException(url, e.getMessage(), "read");
        }
        return objectExistsFlag;
    }

    /** Create folder operat  
   * 
   * @param url                               webdav url 
   * @return boolean                          operat result
   * @throws ServiceAccessException
   * @throws AccessDeniedException
   * @throws ObjectAlreadyExistsException
   */
    public boolean createFolder(String url) throws ServiceAccessException, AccessDeniedException, ObjectAlreadyExistsException {
        Connection conn = null;
        if (isRootOrPesonalOrPublicFolder(url)) {
            return false;
        }
        try {
            conn = DataSourceFactory.getConnection();
            DocumentOperationManager docOperationManager = new DocumentOperationManager(sessionContainer, conn);
            RootOperationManager rootOperationManager = new RootOperationManager(sessionContainer, conn);
            Integer userID = sessionContainer.getUserRecordID();
            DmsRootDAObject rootDAO = new DmsRootDAObject(this.sessionContainer, conn);
            List rootList = rootDAO.getRootByOwnerIDAndType(userID, DmsRoot.PERSONAL_ROOT, GlobalConstant.RECORD_STATUS_ACTIVE);
            DmsRoot rootPoint = null;
            if (url.indexOf(DMS_WEBDAV_PERSONAL_ROOT_FOLDER_URL) == 0) {
                if (rootList.size() == 0) {
                    rootPoint = rootOperationManager.createDefaultPersonalRootPointForUser();
                } else {
                    rootPoint = (DmsRoot) rootList.get(0);
                }
            } else if (url.indexOf(DMS_WEBDAV_PUBLIC_ROOT_FOLDER_URL) == 0) {
                String[] urlArr = TextUtility.splitString(url, "/");
                String lastDocumentName = "";
                if (urlArr.length > 3) {
                    lastDocumentName = urlArr[3];
                }
                rootPoint = (DmsRoot) rootDAO.getObjectByName(lastDocumentName);
            }
            rootDAO = null;
            String[] urlArr = TextUtility.splitString(url, "/");
            Integer parentID = new Integer(0);
            String parentDocumentName = url;
            if (urlArr.length >= 4) {
                parentDocumentName = urlArr[(urlArr.length - 2)];
                DmsDocument parentDocument = null;
                String parentUrl = "";
                for (int i = 0; i < urlArr.length - 1; i++) {
                    if (i != 0) {
                        parentUrl += "/";
                    }
                    parentUrl += urlArr[i];
                }
                if (!DMS_WEBDAV_PERSONAL_ROOT_FOLDER_URL.equals(parentUrl)) {
                    parentDocument = getMappingDocumentByUrl(parentUrl);
                    parentID = parentDocument.getID();
                } else {
                    parentID = rootPoint.getRootFolderID();
                }
            }
            String lastDocumentName = url;
            if (urlArr.length > 3) {
                lastDocumentName = urlArr[(urlArr.length - 1)];
            }
            DmsDocument dmsDocument = new DmsDocument();
            dmsDocument.setDocumentName(lastDocumentName);
            dmsDocument.setRecordStatus(GlobalConstant.STATUS_ACTIVE);
            dmsDocument.setItemStatus(DmsVersion.AVAILABLE_STATUS);
            dmsDocument.setParentID(parentID);
            dmsDocument.setRootID(rootPoint.getID());
            dmsDocument.setDocumentType(DmsDocument.FOLDER_TYPE);
            dmsDocument.setCreateType("S");
            dmsDocument = docOperationManager.createFolder(dmsDocument);
            conn.commit();
        } catch (Exception e) {
            log.error(e);
            throw new AccessDeniedException(url, e.getMessage(), "create");
        } finally {
            closeConnection(conn);
        }
        return true;
    }

    public boolean isRootOrPesonalOrPublicFolder(String resourceUri) {
        if (DMS_WEBDAV_ROOT_FOLDER_URL.equals(resourceUri) || DMS_WEBDAV_PERSONAL_ROOT_FOLDER_URL.equals(resourceUri) || DMS_WEBDAV_PUBLIC_ROOT_FOLDER_URL.equals(resourceUri)) {
            return true;
        }
        return false;
    }

    /** Create document  operat
   * 
   * @param resourceUri                   webdav url 
   * @return boolean                          operat result
   * @throws ServiceAccessException
   * @throws AccessDeniedException
   * @throws ObjectAlreadyExistsException
   */
    public boolean createResource(String resourceUri) throws ServiceAccessException, AccessDeniedException, ObjectAlreadyExistsException {
        Connection conn = null;
        if (isRootOrPesonalOrPublicFolder(resourceUri)) {
            return false;
        }
        try {
            conn = DataSourceFactory.getConnection();
            DocumentOperationManager docOperationManager = new DocumentOperationManager(sessionContainer, conn);
            RootOperationManager rootOperationManager = new RootOperationManager(sessionContainer, conn);
            DmsDocument newDocument = null;
            Integer userID = sessionContainer.getUserRecordID();
            DmsRootDAObject rootDAO = new DmsRootDAObject(this.sessionContainer, conn);
            List rootList = rootDAO.getRootByOwnerIDAndType(userID, DmsRoot.PERSONAL_ROOT, GlobalConstant.RECORD_STATUS_ACTIVE);
            DmsRoot rootPoint = null;
            if (resourceUri.indexOf(DMS_WEBDAV_PERSONAL_ROOT_FOLDER_URL) == 0) {
                if (rootList.size() == 0) {
                    rootPoint = rootOperationManager.createDefaultPersonalRootPointForUser();
                } else {
                    rootPoint = (DmsRoot) rootList.get(0);
                }
            } else if (resourceUri.indexOf(DMS_WEBDAV_PUBLIC_ROOT_FOLDER_URL) == 0) {
                String[] urlArr = TextUtility.splitString(resourceUri, "/");
                String lastDocumentName = "";
                if (urlArr.length > 3) {
                    lastDocumentName = urlArr[3];
                }
                rootPoint = (DmsRoot) rootDAO.getObjectByName(lastDocumentName);
            }
            rootDAO = null;
            String[] urlArr = TextUtility.splitString(resourceUri, "/");
            Integer parentID = new Integer(0);
            String parentDocumentName = resourceUri;
            if (urlArr.length >= 4) {
                parentDocumentName = urlArr[(urlArr.length - 2)];
                DmsDocument parentDocument = null;
                String parentUrl = "";
                for (int i = 0; i < urlArr.length - 1; i++) {
                    if (i != 0) {
                        parentUrl += "/";
                    }
                    parentUrl += urlArr[i];
                }
                if (!DMS_WEBDAV_PERSONAL_ROOT_FOLDER_URL.equals(parentUrl)) {
                    parentDocument = getMappingDocumentByUrl(parentUrl);
                    parentID = parentDocument.getID();
                } else {
                    parentID = rootPoint.getRootFolderID();
                }
            }
            String lastDocumentName = resourceUri;
            if (urlArr.length > 3) {
                lastDocumentName = urlArr[(urlArr.length - 1)];
            }
            DmsDocument dmsDocument = new DmsDocument();
            dmsDocument.setDocumentName(lastDocumentName);
            dmsDocument.setRecordStatus(GlobalConstant.STATUS_ACTIVE);
            dmsDocument.setItemStatus(DmsVersion.AVAILABLE_STATUS);
            dmsDocument.setParentID(parentID);
            dmsDocument.setRootID(rootPoint.getID());
            dmsDocument.setDocumentType(DmsDocument.FOLDER_TYPE);
            dmsDocument.setCreateType(DmsOperationConstant.DMS_CREATE_BY_SYSTEM);
            newDocument = docOperationManager.createFolder(dmsDocument);
            conn.commit();
        } catch (Exception e) {
            log.error(e);
            throw new AccessDeniedException(resourceUri, e.getMessage(), "create");
        } finally {
            closeConnection(conn);
        }
        return true;
    }

    /** Set document content operat
   * 
   * @param resourceUri               webdav url 
   * @param content                   document content
   * @param contentType               document type
   * @param characterEncoding         characterEncoding
   * @param contentLength             contentLength
   * @param methodName                methodName
   * @return  boolean                          operat result
   * @throws ServiceAccessException
   * @throws AccessDeniedException
   * @throws ObjectNotFoundException
   */
    public boolean setResourceContent(String resourceUri, InputStream content, String contentType, String characterEncoding, Integer contentLength, String methodName) throws ServiceAccessException, AccessDeniedException, ObjectNotFoundException {
        if (isRootOrPesonalOrPublicFolder(resourceUri)) {
            return false;
        }
        Connection conn = null;
        String currentFunction = "";
        if (resourceUri.indexOf(DMS_WEBDAV_PERSONAL_ROOT_FOLDER_URL) == 0) {
            currentFunction = DMS_WEBDAV_PERSONAL_ROOT_FOLDER_NAME;
        } else if (resourceUri.indexOf(DMS_WEBDAV_PUBLIC_ROOT_FOLDER_URL) == 0) {
            currentFunction = DMS_WEBDAV_PUBLIC_ROOT_FOLDER_NAME;
        }
        try {
            conn = DataSourceFactory.getConnection();
            DocumentOperationManager docOperationManager = new DocumentOperationManager(sessionContainer, conn);
            RootOperationManager rootOperationManager = new RootOperationManager(sessionContainer, conn);
            DmsDocumentDAObject dmsDocumentDAObject = new DmsDocumentDAObject(sessionContainer, conn);
            DocumentRetrievalManager docRetrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
            DmsDocument newDocument = null;
            Integer userID = sessionContainer.getUserRecordID();
            DmsRootDAObject rootDAO = new DmsRootDAObject(this.sessionContainer, conn);
            List rootList = rootDAO.getRootByOwnerIDAndType(userID, DmsRoot.PERSONAL_ROOT, GlobalConstant.RECORD_STATUS_ACTIVE);
            DmsRoot rootPoint = null;
            if (resourceUri.indexOf(DMS_WEBDAV_PERSONAL_ROOT_FOLDER_URL) == 0) {
                if (rootList.size() == 0) {
                    rootPoint = rootOperationManager.createDefaultPersonalRootPointForUser();
                } else {
                    rootPoint = (DmsRoot) rootList.get(0);
                }
            } else if (resourceUri.indexOf(DMS_WEBDAV_PUBLIC_ROOT_FOLDER_URL) == 0) {
                String[] urlArr = TextUtility.splitString(resourceUri, "/");
                String lastDocumentName = "";
                if (urlArr.length > 3) {
                    lastDocumentName = urlArr[3];
                }
                rootPoint = (DmsRoot) rootDAO.getObjectByName(lastDocumentName);
            }
            rootDAO = null;
            String[] urlArr = TextUtility.splitString(resourceUri, "/");
            Integer parentID = new Integer(0);
            String parentDocumentName = resourceUri;
            if (urlArr.length >= 4) {
                parentDocumentName = urlArr[(urlArr.length - 2)];
                DmsDocument parentDocument = null;
                String parentUrl = "";
                for (int i = 0; i < urlArr.length - 1; i++) {
                    if (i != 0) {
                        parentUrl += "/";
                    }
                    parentUrl += urlArr[i];
                }
                if (!DMS_WEBDAV_PERSONAL_ROOT_FOLDER_URL.equals(parentUrl)) {
                    parentDocument = getMappingDocumentByUrl(parentUrl);
                    parentID = parentDocument.getID();
                } else {
                    parentID = rootPoint.getRootFolderID();
                }
            }
            String lastDocumentName = resourceUri;
            if (urlArr.length > 3) {
                lastDocumentName = urlArr[(urlArr.length - 1)];
            }
            DmsDocument document = getMappingDocumentByUrl(resourceUri);
            if (Utility.isEmpty(document)) {
                DmsDocument dmsDocument = new DmsDocument();
                dmsDocument.setDocumentName(lastDocumentName);
                dmsDocument.setRecordStatus(GlobalConstant.STATUS_ACTIVE);
                dmsDocument.setItemStatus(DmsVersion.AVAILABLE_STATUS);
                dmsDocument.setParentID(parentID);
                dmsDocument.setRootID(rootPoint.getID());
                dmsDocument.setDocumentType(DmsDocument.DOCUMENT_TYPE);
                dmsDocument.setCreateType(DmsOperationConstant.DMS_CREATE_BY_SYSTEM);
                dmsDocument.setItemSize(contentLength);
                DmsDocument parentProfileFolder = docRetrievalManager.getClosestHaveDefaultProfileFolder(parentID);
                if (!Utility.isEmpty(parentProfileFolder)) {
                    dmsDocument.setUserDef1(parentProfileFolder.getUserDef1());
                    dmsDocument.setUserDef2(parentProfileFolder.getUserDef2());
                    dmsDocument.setUserDef3(parentProfileFolder.getUserDef3());
                    dmsDocument.setUserDef4(parentProfileFolder.getUserDef4());
                    dmsDocument.setUserDef5(parentProfileFolder.getUserDef5());
                    dmsDocument.setUserDef6(parentProfileFolder.getUserDef6());
                    dmsDocument.setUserDef7(parentProfileFolder.getUserDef7());
                    dmsDocument.setUserDef8(parentProfileFolder.getUserDef8());
                    dmsDocument.setUserDef9(parentProfileFolder.getUserDef9());
                    dmsDocument.setUserDef10(parentProfileFolder.getUserDef10());
                    List docDetailList = docRetrievalManager.getDocumentDetailList(parentProfileFolder.getID());
                    if (docDetailList != null && docDetailList.size() > 0) {
                        dmsDocument.setUserDefinedFieldID((((DmsDocumentDetail) docDetailList.get(0)).getUserDefinedFieldID()));
                        dmsDocument.setDocumentDetails(docDetailList);
                    }
                    if (dmsDocument.getUserDefinedFieldID() != null) {
                        List udfDetailList = docRetrievalManager.getUDFDetailList(dmsDocument.getUserDefinedFieldID());
                        dmsDocument.setUdfDetailList(udfDetailList);
                    }
                }
                newDocument = docOperationManager.createDocument(dmsDocument, content, true, null);
                conn.commit();
                File file = getFile(resourceUri);
                if (file != null) {
                    newDocument.setItemSize(new Integer((int) file.length()));
                    dmsDocumentDAObject.updateObject(newDocument);
                    conn.commit();
                }
            } else {
                Integer documentID = document.getID();
                checkDocumentRight(resourceUri, document, null, "checkin");
                VersionControlManager versionControlManager = new VersionControlManager(sessionContainer, conn);
                if (!DmsVersion.EXCLUSIVE_LOCK.equals(document.getItemStatus())) {
                    versionControlManager.checkoutDocument(DmsVersion.EXCLUSIVE_LOCK, document, true, false);
                    conn.commit();
                }
                boolean result = versionControlManager.isCurrentCheckoutPerson(documentID, sessionContainer.getUserRecordID());
                if (DMS_WEBDAV_PUBLIC_ROOT_FOLDER_NAME.equals(currentFunction) && !result) {
                    throw new AccessDeniedException(resourceUri, "this document is checkout by other user!", "create");
                }
                String checkinType = "";
                String version = "TOP";
                Integer versionID = new Integer(1);
                DmsVersion dmsVersion = new DmsVersion();
                if (!Utility.isEmpty(version) && !DmsOperationConstant.DMS_TOP_VERSION.equals(version)) {
                    versionID = TextUtility.parseIntegerObj(version);
                    dmsVersion = docRetrievalManager.getVersionByVersionID(versionID);
                } else if (DmsOperationConstant.DMS_TOP_VERSION.equals(version)) {
                    dmsVersion = docRetrievalManager.getTopVersionByDocumentID(documentID);
                }
                dmsVersion.setParentID(new Integer(0));
                dmsVersion.setDocumentID(documentID);
                dmsVersion.setItemSize(contentLength);
                dmsVersion.setCheckinFileName(document.getDocumentName());
                DmsVersion newVersion = versionControlManager.webdavCheckinDocument(true, checkinType, documentID, dmsVersion, content);
                conn.commit();
            }
        } catch (Exception e) {
            log.error(e, e);
            throw new AccessDeniedException(resourceUri, e.getMessage(), "create");
        } finally {
            closeConnection(conn);
        }
        return true;
    }

    /** Get document item size
   * 
   * @param resourceUri               webdav url 
   * @return long                     document item size
   * @throws ServiceAccessException
   * @throws AccessDeniedException
   * @throws ObjectNotFoundException
   */
    public long getResourceLength(String resourceUri, String methodName) throws ServiceAccessException, AccessDeniedException, ObjectNotFoundException {
        long resourceLength = 0;
        if (isRootOrPesonalOrPublicFolder(resourceUri)) {
            return resourceLength;
        }
        try {
            DmsDocument dmsDocument = null;
            dmsDocument = getMappingDocumentByUrl(resourceUri);
            if (dmsDocument != null) {
                if (GET_METHED_NAME.equals(methodName) && DmsDocument.COMPOUND_DOC_TYPE.equals(dmsDocument.getDocumentType())) {
                    resourceLength = 1000000;
                } else if (dmsDocument.getItemSize() != null) {
                    resourceLength = dmsDocument.getItemSize().longValue();
                }
            }
        } catch (SecurityException e) {
            log.error(e);
            throw new AccessDeniedException(resourceUri, e.getMessage(), "read");
        } catch (Exception e) {
            log.error(e);
            throw new AccessDeniedException(resourceUri, e.getMessage(), "read");
        }
        return resourceLength;
    }

    /** Remove document or folder  
   * 
   * @param uri               webdav url 
   * @throws ServiceAccessException
   * @throws AccessDeniedException
   * @throws ObjectNotFoundException
   */
    public void removeObject(String uri) throws AccessDeniedException {
        Connection conn = null;
        if (isRootOrPesonalOrPublicFolder(uri)) {
            return;
        }
        try {
            conn = DataSourceFactory.getConnection();
            DocumentOperationManager docOperationManager = new DocumentOperationManager(sessionContainer, conn);
            DocumentValidateManager docValidateManager = new DocumentValidateManager(sessionContainer, conn);
            DmsDocument dmsDocument = getMappingDocumentByUrl(uri);
            checkDocumentRight(uri, dmsDocument, null, "delete");
            DmsValidation validation = docValidateManager.validateDeleteAction(dmsDocument, false);
            if (!validation.isSuccess()) {
                throw new ApplicationException(DmsErrorConstant.DMS_MESSAGE_CANNOT_MOVE_SUM_DOCUMENT);
            }
            docOperationManager.deleteDocument2(dmsDocument, null, validation);
            conn.commit();
        } catch (SecurityException e) {
            log.error(e);
            throw new AccessDeniedException(uri, e.getMessage(), "delete");
        } catch (Exception e) {
            log.error(e);
            throw new AccessDeniedException(uri, e.getMessage(), "delete");
        } finally {
            closeConnection(conn);
        }
    }

    /** Move document or folder  
   * 
   * @param sourceUri                     source webdav url 
   * @param destinationUri                destination webdav url 
   * @throws ServiceAccessException
   * @throws AccessDeniedException
   * @throws ObjectNotFoundException
   */
    public void moveObject(String sourceUri, String destinationUri) throws ServiceAccessException, AccessDeniedException, ObjectNotFoundException {
        if (isLinkOrCompoundDocType(sourceUri)) {
            throw new AccessDeniedException(sourceUri, "not allow move this folder!", "move document");
        }
        String destinatFolderUri = "";
        String sourceFolderUri = "";
        if (destinationUri.length() > 0) {
            destinatFolderUri = destinationUri.substring(0, destinationUri.lastIndexOf("/"));
        }
        if (sourceUri.length() > 0) {
            sourceFolderUri = sourceUri.substring(0, sourceUri.lastIndexOf("/"));
        }
        if (destinatFolderUri.length() <= 0 || sourceFolderUri.length() <= 0) {
            return;
        }
        Connection conn = null;
        try {
            conn = DataSourceFactory.getConnection();
            DocumentOperationManager docOperationManager = new DocumentOperationManager(sessionContainer, conn);
            DocumentValidateManager docValidateManager = new DocumentValidateManager(sessionContainer, conn);
            if (destinatFolderUri.equals(sourceFolderUri)) {
                DmsDocument dmsDocument = getMappingDocumentByUrl(sourceUri);
                checkDocumentRight(sourceUri, dmsDocument, null, "rename");
                dmsDocument.setDocumentName(destinationUri.substring(destinationUri.lastIndexOf("/") + 1, destinationUri.length()));
                DmsDocumentDetailDAObject dmsDocumentDetailDAObj = new DmsDocumentDetailDAObject(sessionContainer, conn);
                List docDetailList = dmsDocumentDetailDAObj.getListByDocumentID(dmsDocument.getID());
                if (!Utility.isEmpty(docDetailList)) {
                    DmsDocumentDetail tmpDmsDocumentDetail = (DmsDocumentDetail) docDetailList.get(0);
                    Integer userDefinedFieldID = tmpDmsDocumentDetail.getUserDefinedFieldID();
                    dmsDocument.setDocumentDetails(docDetailList);
                    dmsDocument.setUserDefinedFieldID(userDefinedFieldID);
                }
                docOperationManager.renameRecord(dmsDocument);
            } else {
                DmsDocument dmsDocument = getMappingDocumentByUrl(sourceUri);
                DmsDocument dmsFolder = getMappingDocumentByUrl(destinatFolderUri);
                checkDocumentRight(sourceUri, dmsDocument, null, "move");
                DmsDocument newDoc = new DmsDocument();
                newDoc.setDocumentName(dmsDocument.getDocumentName());
                DmsValidation validation = docValidateManager.validateMoveAction(dmsDocument, dmsFolder, newDoc);
                if (!validation.isSuccess()) {
                    throw new ApplicationException(DmsErrorConstant.DMS_MESSAGE_CANNOT_MOVE_SUM_DOCUMENT);
                }
                if (dmsDocument != null && dmsFolder != null) {
                    if (DmsDocument.FOLDER_TYPE.equals(dmsDocument.getDocumentType()) || DmsDocument.COMPOUND_DOC_TYPE.equals(dmsDocument.getDocumentType()) || DmsDocument.PAPER_DOC_TYPE.equals(dmsDocument.getDocumentType())) {
                        docOperationManager.moveFolder(dmsDocument, dmsFolder, dmsFolder.getRootID());
                    } else {
                        docOperationManager.moveDocument(dmsDocument, dmsFolder, dmsFolder.getRootID());
                    }
                }
            }
            conn.commit();
        } catch (SecurityException e) {
            log.error(e);
            throw new AccessDeniedException(sourceUri, e.getMessage(), "move");
        } catch (Exception e) {
            log.error(e);
            throw new AccessDeniedException(sourceUri, e.getMessage(), "move");
        } finally {
            closeConnection(conn);
        }
    }

    /**Copy document or folder  
   * 
   * @param sourceUri                     source webdav url 
   * @param destinationUri                destination webdav url 
   * @throws ServiceAccessException
   * @throws AccessDeniedException
   * @throws ObjectNotFoundException
   */
    public void copyObject(String sourceUri, String destinationUri) throws ServiceAccessException, AccessDeniedException, ObjectNotFoundException, SlideException {
        Connection conn = null;
        String destinatFolderUri = "";
        String sourceFolderUri = "";
        try {
            conn = DataSourceFactory.getConnection();
            DocumentOperationManager docOperationManager = new DocumentOperationManager(sessionContainer, conn);
            DocumentValidateManager docValidateManager = new DocumentValidateManager(sessionContainer, conn);
            if (isRootOrPesonalOrPublicFolder(sourceUri)) {
                throw new AccessDeniedException(sourceUri, "not allow copy root publice folder!", "copy document");
            }
            if (destinationUri.length() > 0) {
                destinatFolderUri = destinationUri.substring(0, destinationUri.lastIndexOf("/"));
            }
            if (sourceUri.length() > 0) {
                sourceFolderUri = sourceUri.substring(0, sourceUri.lastIndexOf("/"));
            }
            if (destinatFolderUri.length() <= 0 || sourceFolderUri.length() <= 0) {
                return;
            }
            DmsDocument dmsDocument = getMappingDocumentByUrl(sourceUri);
            DmsDocument dmsFolder = getMappingDocumentByUrl(destinatFolderUri);
            checkDocumentRight(sourceUri, dmsDocument, null, "copy");
            boolean[] options = new boolean[4];
            options[0] = false;
            options[1] = false;
            options[2] = false;
            options[3] = false;
            docValidateManager = new DocumentValidateManager(sessionContainer, conn);
            DmsDocument newDoc = new DmsDocument();
            boolean inSameFolder = false;
            if (destinatFolderUri.equals(sourceFolderUri)) {
                newDoc.setDocumentName(destinationUri.substring(destinationUri.lastIndexOf('/') + 1));
                inSameFolder = true;
            } else {
                newDoc.setDocumentName(dmsDocument.getDocumentName());
            }
            DmsValidation validation = docValidateManager.validateCopyAction(dmsDocument, dmsFolder, newDoc, options);
            copyDocument(docOperationManager, dmsFolder, dmsDocument, options, newDoc, inSameFolder, validation);
            conn.commit();
        } catch (SecurityException e) {
            log.error(e);
            throw new AccessDeniedException(sourceUri, e.getMessage(), "copy");
        } catch (Exception e) {
            log.error(e);
            throw new SlideException(e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    /**
   * copy document whether destination is same or difference the source
   * @param docOperationManager
   * @param dmsFolder
   * @param dmsDocument
   * @param options
   * @param newDoc
   * @param inSameFolder
   * @param validation
   * @throws ApplicationException
   */
    private void copyDocument(DocumentOperationManager docOperationManager, DmsDocument dmsFolder, DmsDocument dmsDocument, boolean[] options, DmsDocument newDoc, boolean inSameFolder, DmsValidation validation) throws ApplicationException {
        if (inSameFolder) {
            DmsDocument tempDoc = (DmsDocument) dmsDocument.clone();
            tempDoc.setDocumentName(newDoc.getDocumentName());
            docOperationManager.copyDocument(dmsDocument, dmsFolder, tempDoc, validation, options);
        } else {
            docOperationManager.copyDocument(dmsDocument, dmsFolder, dmsDocument, validation, options);
        }
    }

    /**Get document content 
   * @param resourceUri                   webdav url 
   * @param methodName                    methodName
   * @return InputStream                  document content 
   * @throws ServiceAccessException
   * @throws AccessDeniedException
   * @throws ObjectNotFoundException
   */
    public InputStream getResourceContent(String resourceUri, String methodName) throws ServiceAccessException, AccessDeniedException, ObjectNotFoundException {
        InputStream dataStream = null;
        InputStream inputStream = null;
        ByteArrayOutputStream bot = new ByteArrayOutputStream();
        ZipOutputStream sos = new ZipOutputStream(bot);
        sos.setMethod(ZipOutputStream.DEFLATED);
        Connection conn = null;
        try {
            Integer contentID = new Integer(0);
            DmsDocument dmsDocument = getMappingDocumentByUrl(resourceUri);
            checkDocumentRight(resourceUri, dmsDocument, null, methodName);
            Integer targetID = dmsDocument.getID();
            conn = DataSourceFactory.getConnection();
            DocumentRetrievalManager docRetrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
            DocumentOperationManager docOperationManager = new DocumentOperationManager(sessionContainer, conn);
            AlertManager alertManager = new AlertManager(sessionContainer, conn);
            DmsContentManager dmsContentManager = new DmsContentManager(sessionContainer, conn);
            if (DmsDocument.COMPOUND_DOC_TYPE.equals(dmsDocument.getDocumentType())) {
                List list = docOperationManager.getSubDocumentByParentID(dmsDocument.getID());
                if (DmsDocument.COMPOUND_DOC_TYPE.equals(dmsDocument.getDocumentType())) {
                    list.add(dmsDocument);
                }
                if (!Utility.isEmpty(list)) {
                    for (int i = 0; i < list.size(); i++) {
                        DmsDocument document = (DmsDocument) list.get(i);
                        if ("L".equals(document.getDocumentType())) {
                            continue;
                        }
                        String fName = DocumentRetrievalManager.getEncodeStringByEncodeCode(document.getDocumentName(), SystemParameterFactory.getSystemParameter(SystemParameterConstant.DMS_ZIP_FILE_DEFAULT_ENCODING));
                        DmsVersion sVersion = docRetrievalManager.getTopVersionByDocumentID(document.getID());
                        contentID = sVersion.getContentID();
                        if (!GlobalConstant.RECORD_STATUS_INACTIVE.equals(document.getRecordStatus())) {
                            DmsContent docContent = docRetrievalManager.getContentByContentID(contentID);
                            ZipEntry theEntry = new ZipEntry(fName);
                            sos.putNextEntry(theEntry);
                            byte[] buffer = new byte[8192];
                            int length = -1;
                            inputStream = dmsContentManager.readDmsDocumentStoreContent(document, docContent);
                            while ((length = inputStream.read(buffer, 0, 8192)) != -1) {
                                sos.write(buffer, 0, length);
                            }
                        }
                    }
                }
                sos.flush();
                sos.close();
                dataStream = new DataInputStream(new ByteArrayInputStream(bot.toByteArray()));
            } else {
                String version = "TOP";
                MtmDocumentRelationshipDAObject docRelationshipDAO = new MtmDocumentRelationshipDAObject(sessionContainer, conn);
                DmsVersion dmsVersion = new DmsVersion();
                if (!Utility.isEmpty(version) && !DmsOperationConstant.DMS_TOP_VERSION.equals(version)) {
                    Integer versionID = TextUtility.parseIntegerObj(version);
                    dmsVersion = docRetrievalManager.getVersionByVersionID(versionID);
                    contentID = dmsVersion.getContentID();
                } else if (DmsOperationConstant.DMS_TOP_VERSION.equals(version)) {
                    dmsVersion = docRetrievalManager.getTopVersionByDocumentID(targetID);
                    contentID = dmsVersion.getContentID();
                }
                if (sessionContainer.getUserRecordID() != null && !methodName.equalsIgnoreCase("copy") && !methodName.equalsIgnoreCase("move")) {
                    AdapterMaster am = AdapterMasterFactory.getAdapterMaster(sessionContainer, conn);
                    DmsDocument parentDoc = docRetrievalManager.getDocument(dmsDocument.getParentID());
                    try {
                        am.call(UpdateAlert.DOCUMENT_TYPE, dmsDocument.getID(), UpdateAlert.VIEW_ACTION, dmsDocument.getDocumentName(), null, null, null);
                        am.call(UpdateAlert.DOCUMENT_TYPE, parentDoc.getID(), UpdateAlert.VIEW_ACTION, dmsDocument.getDocumentName(), null, null, null, dmsDocument.getID());
                        if (docRetrievalManager.hasRelationship(dmsDocument.getID(), null)) {
                            List relationList = docRelationshipDAO.getListByIDRelationType(dmsDocument.getID(), null);
                            List inRelationList = docRelationshipDAO.getListByRelationIDRelationType(dmsDocument.getID(), null);
                            List alertList = new ArrayList();
                            if (!Utility.isEmpty(relationList)) {
                                for (int i = 0; i < relationList.size(); i++) {
                                    alertList = alertManager.listUpdateAlertByObjectTypeObjectIDAndAction(UpdateAlert.DOCUMENT_TYPE, ((MtmDocumentRelationship) relationList.get(i)).getDocumentID(), UpdateAlert.MODIFY_RELATED_DOC);
                                    if (!Utility.isEmpty(alertList)) {
                                        am.call(UpdateAlert.DOCUMENT_TYPE, ((MtmDocumentRelationship) relationList.get(i)).getDocumentID(), UpdateAlert.MODIFY_RELATED_DOC, dmsDocument.getDocumentName(), null, null, null, dmsDocument.getID());
                                    }
                                }
                            }
                            if (!Utility.isEmpty(inRelationList)) {
                                for (int i = 0; i < inRelationList.size(); i++) {
                                    alertList = alertManager.listUpdateAlertByObjectTypeObjectIDAndAction(UpdateAlert.DOCUMENT_TYPE, ((MtmDocumentRelationship) inRelationList.get(i)).getRelatedDocumentID(), UpdateAlert.MODIFY_RELATED_DOC);
                                    if (!Utility.isEmpty(alertList)) {
                                        am.call(UpdateAlert.DOCUMENT_TYPE, ((MtmDocumentRelationship) inRelationList.get(i)).getRelatedDocumentID(), UpdateAlert.MODIFY_RELATED_DOC, dmsDocument.getDocumentName(), null, null, null, dmsDocument.getID());
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error(e, e);
                    }
                    AuditTrailManager auditManager = new AuditTrailManager(sessionContainer, conn);
                    auditManager.auditTrail(GlobalConstant.OBJECT_TYPE_DOCUMENT, dmsDocument, AuditTrailConstant.ACCESS_TYPE_VIEW, dmsVersion.getVersionNumber());
                }
                conn.commit();
                DmsDocument targetDoc = null;
                if (DmsDocument.DOCUMENT_LINK.equals(dmsDocument.getDocumentType())) {
                    Integer targetDocID = docRelationshipDAO.getTargetDocIDByRelatedDocID(dmsDocument.getID(), dmsDocument.getDocumentType());
                    targetDoc = docRetrievalManager.getDocument(targetDocID);
                    if (targetDoc != null) {
                        targetDoc.setDocumentName(dmsDocument.getDocumentName());
                        targetDoc.setDocumentType(dmsDocument.getDocumentType());
                        targetID = targetDocID;
                        dmsDocument = targetDoc;
                        dmsVersion = docRetrievalManager.getTopVersionByDocumentID(targetID);
                        contentID = dmsVersion.getContentID();
                    } else {
                        dmsDocument.setRecordStatus(GlobalConstant.RECORD_STATUS_INACTIVE);
                    }
                }
                DmsContent docContent = docRetrievalManager.getContentByContentID(contentID);
                dataStream = new BufferedInputStream(dmsContentManager.readDmsDocumentStoreContent(dmsDocument, docContent));
            }
            if (dataStream == null) {
                log.info("dataStream==null is true");
            }
            return dataStream;
        } catch (Exception e) {
            log.error(e);
            throw new ObjectNotFoundException(resourceUri);
        } finally {
            closeConnection(conn);
        }
    }

    /**Get document 
   * 
   * @param uri                    webdav url 
   * @return File  document's source file
   */
    public File getFile(String uri) {
        File file = null;
        Connection conn = null;
        try {
            if (!objectExists(uri)) {
                throw new ObjectNotFoundException(uri);
            }
            DmsDocument dmsDocument = null;
            dmsDocument = getMappingDocumentByUrl(uri);
            Integer targetID = dmsDocument.getID();
            String version = "TOP";
            conn = DataSourceFactory.getConnection();
            DocumentRetrievalManager docRetrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
            MtmDocumentRelationshipDAObject docRelationshipDAO = new MtmDocumentRelationshipDAObject(sessionContainer, conn);
            DmsContentManager dmsContentManager = new DmsContentManager(sessionContainer, conn);
            DmsVersion dmsVersion = new DmsVersion();
            Integer contentID = new Integer(0);
            if (!Utility.isEmpty(version) && !DmsOperationConstant.DMS_TOP_VERSION.equals(version)) {
                Integer versionID = TextUtility.parseIntegerObj(version);
                dmsVersion = docRetrievalManager.getVersionByVersionID(versionID);
                contentID = dmsVersion.getContentID();
            } else if (DmsOperationConstant.DMS_TOP_VERSION.equals(version)) {
                dmsVersion = docRetrievalManager.getTopVersionByDocumentID(targetID);
                contentID = dmsVersion.getContentID();
            }
            DmsDocument targetDoc = null;
            if (DmsDocument.DOCUMENT_LINK.equals(dmsDocument.getDocumentType())) {
                Integer targetDocID = docRelationshipDAO.getTargetDocIDByRelatedDocID(dmsDocument.getID(), dmsDocument.getDocumentType());
                targetDoc = docRetrievalManager.getDocument(targetDocID);
                if (targetDoc != null) {
                    targetDoc.setDocumentName(dmsDocument.getDocumentName());
                    targetDoc.setDocumentType(dmsDocument.getDocumentType());
                    targetID = targetDocID;
                    dmsDocument = targetDoc;
                    dmsVersion = docRetrievalManager.getTopVersionByDocumentID(targetID);
                    contentID = dmsVersion.getContentID();
                } else {
                    dmsDocument.setRecordStatus(GlobalConstant.RECORD_STATUS_INACTIVE);
                }
            }
            DmsContent docContent = docRetrievalManager.getContentByContentID(contentID);
            String physicalPath = dmsContentManager.getDmsDocumentStoreFilePysicalPath(dmsDocument, docContent);
            file = new File(physicalPath);
        } catch (Exception e) {
            log.error(e);
        } finally {
            closeConnection(conn);
        }
        return file;
    }

    /** Get a document last modified date
   * @param url                    webdav url 
   * @return Date                  a document last modified date
   * @throws ServiceAccessException
   * @throws AccessDeniedException
   * @throws ObjectNotFoundException
   */
    public Date getLastModified(String url) throws AccessDeniedException {
        Date reDate = new Date();
        try {
            DmsDocument dmsDocument = null;
            dmsDocument = getMappingDocumentByUrl(url);
            if (dmsDocument != null) {
                reDate = dmsDocument.getUpdateDate();
            }
        } catch (Exception e) {
            log.error(e);
            throw new AccessDeniedException(url, e.getMessage(), "read");
        }
        return reDate;
    }

    /**Check Permission
   * 
   * @param sessionContainer        SessionContainer
   * @param uri                     webdav url 
   * @param rightType               right Type 
   * @return boolean                if have permission  
   */
    public boolean checkPermission(String uri, String rightCode) {
        if (Utility.isEmpty(uri)) {
            return false;
        }
        boolean hasRight = false;
        DmsDocument dmsDocument = null;
        PermissionManager permManager = sessionContainer.getPermissionManager();
        permManager.clearPermissionCache();
        try {
            if (uri.indexOf(DMS_WEBDAV_PERSONAL_ROOT_FOLDER_URL) == 0) {
                return true;
            }
            if (DMS_PERMISSION_CREATE_FOLDER_CODE.equals(rightCode) || DMS_PERMISSION_CREATE_DOCUMENT_CODE.equals(rightCode)) {
                String parentFolderUrl = uri.substring(0, uri.lastIndexOf("/"));
                dmsDocument = Utility.isEmpty(parentFolderUrl) ? null : getMappingDocumentByUrl(parentFolderUrl);
            } else {
                dmsDocument = getMappingDocumentByUrl(uri);
            }
            if (!Utility.isEmpty(dmsDocument)) {
                hasRight = permManager.hasAccessRight(GlobalConstant.OBJECT_TYPE_DOCUMENT, dmsDocument.getID(), rightCode);
            }
        } catch (Exception e) {
            log.error(e);
            hasRight = false;
        }
        return hasRight;
    }

    /**
   * check object right
   * @param nObjectID
   * @param sRightCode
   * @return
   */
    public boolean checkPermission(Integer nObjectID, String sRightCode) {
        boolean bHasRihgt = false;
        PermissionManager permManager = sessionContainer.getPermissionManager();
        permManager.clearPermissionCache();
        try {
            bHasRihgt = permManager.hasAccessRight(GlobalConstant.OBJECT_TYPE_DOCUMENT, nObjectID, sRightCode);
        } catch (ApplicationException exp) {
            log.error(exp);
            bHasRihgt = false;
        }
        return bHasRihgt;
    }

    private boolean isExpired(DmsDocument targetDmsDocument) {
        if (targetDmsDocument.getEffectiveEndDate() == null) {
            return false;
        }
        boolean isExpired = true;
        Timestamp nowTime = Utility.getCurrentTimestamp();
        Timestamp oneDayBeforeCurrTime = Utility.addDay(nowTime, -1);
        if (isExpired && (targetDmsDocument.getEffectiveStartDate() == null || targetDmsDocument.getEffectiveStartDate().equals(nowTime) || targetDmsDocument.getEffectiveStartDate().before(nowTime)) && (targetDmsDocument.getEffectiveEndDate() == null || targetDmsDocument.getEffectiveEndDate().equals(nowTime) || targetDmsDocument.getEffectiveEndDate().after(oneDayBeforeCurrTime))) {
            isExpired = false;
        }
        return isExpired;
    }

    private boolean isNotYetEffective(DmsDocument targetDmsDocument) {
        if (targetDmsDocument.getEffectiveStartDate() == null) {
            return false;
        }
        boolean isNotYetEffective = false;
        if (targetDmsDocument.getEffectiveStartDate().getTime() >= Utility.getCurrentTimestamp().getTime()) {
            isNotYetEffective = true;
        }
        return isNotYetEffective;
    }

    public void closeConnection(Connection conn) {
        try {
            DbUtils.close(conn);
        } catch (Exception e) {
            log.error("connection close error!");
        } finally {
            conn = null;
        }
    }

    public void checkCreateRight(String resourceUri, boolean isFolder) throws AccessDeniedException, ObjectNotFoundException {
        if (resourceUri.indexOf(DMS_WEBDAV_PERSONAL_ROOT_FOLDER_URL) == 0) {
            return;
        }
        String parentFolderUri = resourceUri.substring(0, resourceUri.lastIndexOf("/"));
        if (DMS_WEBDAV_PUBLIC_ROOT_FOLDER_URL.equals(parentFolderUri) || DMS_WEBDAV_ROOT_FOLDER_URL.equals(parentFolderUri)) {
            throw new AccessDeniedException(resourceUri, "not allow create folder or document in root folder document", "access");
        }
        DmsDocument pFolder = getMappingDocumentByUrl(parentFolderUri);
        boolean createRight = isFolder ? checkPermission(pFolder.getID(), DMS_PERMISSION_CREATE_FOLDER_CODE) : checkPermission(pFolder.getID(), DMS_PERMISSION_CREATE_DOCUMENT_CODE);
        if (!createRight) {
            throw new AccessDeniedException(resourceUri, "haven't create right", "create");
        }
        DmsDocument document = getMappingDocumentByUrl(resourceUri);
        if (!Utility.isEmpty(document)) {
            if (DmsVersion.ARCHIVED_STATUS.equals(document.getItemStatus()) || isExpired(document) || isNotYetEffective(document)) {
                throw new AccessDeniedException(resourceUri, "haven't check in right", "check in");
            }
        }
    }

    public void checkDocumentRight(String sourceUri, DmsDocument source, DmsDocument sourceFolder, String reqMethod) throws AccessDeniedException, ObjectNotFoundException {
        if (sourceUri.indexOf(DMS_WEBDAV_PERSONAL_ROOT_FOLDER_URL) == 0) {
            return;
        }
        if (Utility.isEmpty(source)) {
            throw new ObjectNotFoundException(sourceUri);
        }
        Integer doumnetId = source.getID();
        if (source.getParentID().intValue() == 0) {
            throw new AccessDeniedException(sourceUri, "not allow move,copy,delete,rename of root folder document", reqMethod);
        }
        if (!checkPermission(doumnetId, DMS_PERMISSION_READ_CODE)) {
            throw new AccessDeniedException(sourceUri, "not allow access document!", reqMethod);
        }
        if (DmsVersion.EXCLUSIVE_LOCK.equals(source.getItemStatus()) && !"checkin".equalsIgnoreCase(reqMethod)) {
            throw new AccessDeniedException(sourceUri, "not allow read document!", reqMethod);
        }
        if (DmsVersion.ARCHIVED_STATUS.equals(source.getItemStatus()) || isExpired(source) || isNotYetEffective(source)) {
            throw new AccessDeniedException(sourceUri, "not allow read document!", reqMethod);
        }
        if ("copy".equalsIgnoreCase(reqMethod)) {
            if (!checkPermission(doumnetId, DMS_PERMISSION_COPY_CODE)) {
                throw new AccessDeniedException(sourceUri, "not allow copy document!", reqMethod);
            }
        } else if ("move".equalsIgnoreCase(reqMethod)) {
            if (!checkPermission(doumnetId, DMS_PERMISSION_MOVE_CODE)) {
                throw new AccessDeniedException(sourceUri, "not allow move document!", reqMethod);
            }
        } else if ("delete".equalsIgnoreCase(reqMethod)) {
            if (!checkPermission(source.getID(), DMS_PERMISSION_DELETE_CODE)) {
                throw new AccessDeniedException(sourceUri, "not allow delete document!", reqMethod);
            }
        } else if ("rename".equalsIgnoreCase(reqMethod)) {
            if (!checkPermission(source.getID(), DMS_PERMISSION_RENAME_CODE)) {
                throw new AccessDeniedException(sourceUri, "not allow rename document!", reqMethod);
            }
        } else if ("checkin".equalsIgnoreCase(reqMethod)) {
            if (!checkPermission(source.getID(), DMS_PERMISSION_UPDATE_CODE)) {
                throw new AccessDeniedException(sourceUri, "not allow checkin file!", reqMethod);
            }
        }
        if (("copy".equalsIgnoreCase(reqMethod) || "move".equalsIgnoreCase(reqMethod)) && sourceFolder != null) {
            if (DmsDocument.COMPOUND_DOC_TYPE.equals(source.getDocumentType()) && DmsDocument.COMPOUND_DOC_TYPE.equals(sourceFolder.getDocumentType())) {
                throw new AccessDeniedException(sourceUri, "not allow " + reqMethod + " compound to compound document", reqMethod);
            }
            if (DmsDocument.FOLDER_TYPE.equals(source.getDocumentType()) && DmsDocument.COMPOUND_DOC_TYPE.equals(sourceFolder.getDocumentType())) {
                throw new AccessDeniedException(sourceUri, "not allow " + reqMethod + " folder to compound document!", reqMethod);
            }
        }
    }
}
