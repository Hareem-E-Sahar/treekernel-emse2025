package com.dcivision.dms.client.analyzer;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.dcivision.dms.DmsErrorConstant;
import com.dcivision.dms.DmsOperationConstant;
import com.dcivision.dms.bean.DmsDefaultProfileSetting;
import com.dcivision.dms.bean.DmsDocument;
import com.dcivision.dms.bean.DmsVersion;
import com.dcivision.dms.core.DocumentOperationManager;
import com.dcivision.dms.core.DocumentRetrievalManager;
import com.dcivision.dms.dao.DmsDefaultProfileSettingDAObject;
import com.dcivision.dms.web.MaintDmsDocumentForm;
import com.dcivision.framework.ApplicationException;
import com.dcivision.framework.GlobalConstant;
import com.dcivision.framework.PermissionManager;
import com.dcivision.framework.SessionContainer;
import com.dcivision.framework.SystemParameterConstant;
import com.dcivision.framework.SystemParameterFactory;
import com.dcivision.framework.TextUtility;
import com.dcivision.framework.Utility;

/**
 * <p>Class Name:       DmsStandardDocumentAnalyzer.java    </p>
 * <p>Description:      This class is providing an interface for all kind of method on document Name defining and folder creation for uploading files.</p>
 * @author              Zoe Shum
 * @company             DCIVision Limited
 * @creation date       05/08/2003
 * @version             $Revision: 1.52.2.2 $
 */
public class DmsStandardDocumentAnalyzer implements DmsDocumentAnalyzer {

    public static final String REVISION = "$Revision: 1.52.2.2 $";

    protected Log log = LogFactory.getLog(this.getClass().getName());

    protected Integer ID = null;

    protected String documentName = null;

    protected Integer parentID = null;

    protected Integer rootID = null;

    protected String effectiveStartDate = null;

    protected String referenceNo = null;

    protected String description = null;

    protected String userDef1 = null;

    protected String userDef2 = null;

    protected String userDef3 = null;

    protected String userDef4 = null;

    protected String userDef5 = null;

    protected String userDef6 = null;

    protected String userDef7 = null;

    protected String userDef8 = null;

    protected String userDef9 = null;

    protected String userDef10 = null;

    protected String fullText = null;

    protected String opMode = null;

    protected String submitSystemWorkflow = "";

    protected String workflowRecordID = null;

    protected String fileExten = null;

    public Integer getID() {
        return (this.ID);
    }

    public String getDocumentName() {
        return (this.documentName);
    }

    public Integer getParentID() {
        return (this.parentID);
    }

    public Integer getRootID() {
        return (this.rootID);
    }

    public String getEffectiveStartDate() {
        return (this.effectiveStartDate);
    }

    public String getReferenceNo() {
        return (this.referenceNo);
    }

    public String getDescription() {
        return (this.description);
    }

    public String getUserDef1() {
        return (this.userDef1);
    }

    public String getUserDef2() {
        return (this.userDef2);
    }

    public String getUserDef3() {
        return (this.userDef3);
    }

    public String getUserDef4() {
        return (this.userDef4);
    }

    public String getUserDef5() {
        return (this.userDef5);
    }

    public String getUserDef6() {
        return (this.userDef6);
    }

    public String getUserDef7() {
        return (this.userDef7);
    }

    public String getUserDef8() {
        return (this.userDef8);
    }

    public String getUserDef9() {
        return (this.userDef9);
    }

    public String getUserDef10() {
        return (this.userDef10);
    }

    public String getFullText() {
        return (this.fullText);
    }

    public String getOpMode() {
        return (this.opMode);
    }

    public String getSubmitSystemWorkflow() {
        return (this.submitSystemWorkflow);
    }

    public String getWorkflowRecordID() {
        return (this.workflowRecordID);
    }

    public Boolean analyze(SessionContainer sessionContainer, Connection conn, MaintDmsDocumentForm docForm, java.io.InputStream infile) throws ApplicationException {
        Boolean analyzedResult = new Boolean(true);
        String createFolderDateFormat = SystemParameterFactory.getSystemParameter(SystemParameterConstant.DMS_SCAN_FOLDER_DATE_FORMAT);
        SimpleDateFormat dateFolderFormat = new SimpleDateFormat(createFolderDateFormat, Utility.getLocaleByString(SystemParameterFactory.getSystemParameter(SystemParameterConstant.LOCALE)));
        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        org.w3c.dom.Document indexFile = null;
        try {
            docBuilder = dfactory.newDocumentBuilder();
            indexFile = docBuilder.parse(infile);
        } catch (Exception e) {
            log.error(e, e);
        }
        fileExten = docForm.getTrueFileName();
        this.documentName = this.resolveDocumentName(indexFile);
        this.description = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.DESCRIPTION_TAG);
        this.referenceNo = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.REFERENCE_NUM_TAG);
        this.userDef1 = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.USER_DEF_1);
        this.userDef2 = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.USER_DEF_2);
        this.userDef3 = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.USER_DEF_3);
        this.userDef4 = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.USER_DEF_4);
        this.userDef5 = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.USER_DEF_5);
        this.userDef6 = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.USER_DEF_6);
        this.userDef7 = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.USER_DEF_7);
        this.userDef8 = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.USER_DEF_8);
        this.userDef9 = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.USER_DEF_9);
        this.userDef10 = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.USER_DEF_10);
        this.fullText = this.resolverFullText(indexFile);
        this.workflowRecordID = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.WORKFLOW_RECORD_ID);
        String scanDefaultWorkflowRecordID = SystemParameterFactory.getSystemParameter(SystemParameterConstant.DMS_SCAN_DEFAULT_WORKFLOW_RECORD_ID);
        if (!Utility.isEmpty(scanDefaultWorkflowRecordID)) {
            this.workflowRecordID = scanDefaultWorkflowRecordID;
        }
        if (!Utility.isEmpty(this.workflowRecordID)) {
            this.submitSystemWorkflow = GlobalConstant.TRUE;
        }
        if (!"0".equals(this.workflowRecordID)) {
            DocumentOperationManager docOperationManager = new DocumentOperationManager(sessionContainer, conn);
            DocumentRetrievalManager docReterialManager = new DocumentRetrievalManager(sessionContainer, conn);
            PermissionManager permissionManager = sessionContainer.getPermissionManager();
            DmsDocument dmsDocument = (DmsDocument) docForm.getFormData();
            if (!"Y".equals(docForm.getScanSupportFolderStructure())) {
                if (!Utility.isEmpty(resolverSingleDocumentInfoByTagAttribute(indexFile, DmsDocumentAnalyzer.DESCRIPTOR, DmsDocumentAnalyzer.PARENT_ID))) {
                    dmsDocument.setParentID(TextUtility.parseIntegerObj(resolverSingleDocumentInfoByTagAttribute(indexFile, DmsDocumentAnalyzer.DESCRIPTOR, DmsDocumentAnalyzer.PARENT_ID)));
                }
                if (!Utility.isEmpty(resolverSingleDocumentInfoByTagAttribute(indexFile, DmsDocumentAnalyzer.DESCRIPTOR, DmsDocumentAnalyzer.ROOT_ID))) {
                    dmsDocument.setRootID(TextUtility.parseIntegerObj(resolverSingleDocumentInfoByTagAttribute(indexFile, DmsDocumentAnalyzer.DESCRIPTOR, DmsDocumentAnalyzer.ROOT_ID)));
                }
            }
            String documentProfileName = this.getDocumentProfileName(indexFile);
            com.dcivision.framework.dao.SysUserDefinedIndexDAObject sysUDFDAO = new com.dcivision.framework.dao.SysUserDefinedIndexDAObject(sessionContainer, conn);
            com.dcivision.framework.bean.SysUserDefinedIndex udfObj = (com.dcivision.framework.bean.SysUserDefinedIndex) sysUDFDAO.getObjectByUserDefinedType(documentProfileName);
            String folderName = "";
            String createFolderMethod = SystemParameterFactory.getSystemParameter(SystemParameterConstant.DMS_SCAN_FOLDER_CREATION_FORMAT);
            if (udfObj != null && !Utility.isEmpty(udfObj.getDmsScanFolderCreationType())) {
                createFolderMethod = udfObj.getDmsScanFolderCreationType();
            }
            if ("true".equals(docForm.getErrorScanFormat())) {
                createFolderMethod = DmsOperationConstant.SCAN_FOLDER_CREATE_BY_LOGINNAME;
            }
            if (DmsOperationConstant.SCAN_FOLDER_CREATION_BY_UDF.equals(createFolderMethod) || DmsOperationConstant.SCAN_FOLDER_CREATION_BY_UDF_BY_DATE.equals(createFolderMethod)) {
                folderName = this.resolverFolderName(indexFile);
            } else if (DmsOperationConstant.SCAN_FOLDER_CREATION_BY_UDF_VALUE.equals(createFolderMethod) || DmsOperationConstant.SCAN_FOLDER_CREATION_BY_UDF_VALUE_BY_DATE.equals(createFolderMethod)) {
                folderName = this.resolverFolderNameUsingUDFValue(indexFile);
                if ("".equals(folderName)) {
                    folderName = dateFolderFormat.format(new Date());
                }
            } else if (DmsOperationConstant.SCAN_FOLDER_CREATION_BY_UDF_VALUE_BY_UDF_BY_DATE.equals(createFolderMethod)) {
                folderName = this.resolverFolderNameUsingUDFValue(indexFile);
                if ("".equals(folderName)) {
                    folderName = dateFolderFormat.format(new Date());
                }
            } else if (DmsOperationConstant.SCAN_FOLDER_CREATION_BY_DATE.equals(createFolderMethod)) {
                folderName = dateFolderFormat.format(new Date());
            } else if (DmsOperationConstant.SCAN_FOLDER_CREATION_BY_DPF_NAME.equals(createFolderMethod)) {
                Integer defaultProfileID = SystemParameterFactory.getSystemParameterInteger(SystemParameterConstant.DMS_SCAN_FOLDER_CREATION_DPF_ID);
                String tagName = "user_def_" + defaultProfileID;
                if (!Utility.isEmpty(resolverDocumentInfo(indexFile, tagName))) {
                    folderName = resolverDocumentInfo(indexFile, tagName);
                }
                if ("".equals(folderName)) {
                    folderName = dateFolderFormat.format(new Date());
                }
            } else if ("SCAN.FOLDER_DEMO_DP1_DATE".equals(createFolderMethod)) {
                folderName = this.userDef1;
            } else if ("SCAN.FOLDER_DEMO_DP1_DP2_DATE".equals(createFolderMethod)) {
                folderName = this.userDef1;
            } else if (DmsOperationConstant.SCAN_FOLDER_CREATE_BY_LOGINNAME.equals(createFolderMethod)) {
                folderName = sessionContainer.getUserRecord().getLoginName();
            }
            if (udfObj != null && !Utility.isEmpty(udfObj.getDmsParentID()) && !DmsOperationConstant.SCAN_FOLDER_CREATE_BY_LOGINNAME.equals(createFolderMethod)) {
                dmsDocument.setParentID(udfObj.getDmsParentID());
                dmsDocument.setRootID(udfObj.getDmsRootID());
            }
            dmsDocument.setDocumentName(folderName);
            dmsDocument.setDocumentType(DmsDocument.FOLDER_TYPE);
            dmsDocument.setRecordStatus(GlobalConstant.STATUS_ACTIVE);
            dmsDocument.setItemStatus(DmsVersion.AVAILABLE_STATUS);
            dmsDocument.setCreateType(DmsOperationConstant.DMS_CREATE_BY_UPLOAD);
            DmsDocument newParentDocument = docReterialManager.getDocument(dmsDocument.getParentID());
            DmsDocument parentDocument = docReterialManager.getDocumentByNameParentID(folderName, dmsDocument.getParentID());
            String permission = permissionManager.getPermission(GlobalConstant.OBJECT_TYPE_DOCUMENT, dmsDocument.getParentID());
            boolean createFolderBypassSecurity = SystemParameterFactory.getSystemParameterBoolean(SystemParameterConstant.DMS_SCAN_FOLDER_CREATION_BYPASS_SECURITY);
            boolean canCreateFolderFlag = false;
            if (!createFolderBypassSecurity && permission.indexOf("F") < 0) {
                canCreateFolderFlag = false;
            } else {
                canCreateFolderFlag = true;
            }
            if (DmsOperationConstant.SCAN_FOLDER_CREATION_BY_UDF.equals(createFolderMethod) || DmsOperationConstant.SCAN_FOLDER_CREATION_BY_UDF_VALUE.equals(createFolderMethod) || DmsOperationConstant.SCAN_FOLDER_CREATION_BY_DATE.equals(createFolderMethod) || DmsOperationConstant.SCAN_FOLDER_CREATION_BY_DPF_NAME.equals(createFolderMethod) || DmsOperationConstant.SCAN_FOLDER_CREATE_BY_LOGINNAME.equals(createFolderMethod)) {
                if (parentDocument == null) {
                    if (!Utility.isEmpty(folderName) && DmsDocument.FOLDER_TYPE.equals(newParentDocument.getDocumentType())) {
                        if (canCreateFolderFlag) {
                            parentDocument = docOperationManager.createFolder(dmsDocument);
                        } else {
                            throw new ApplicationException(DmsErrorConstant.NO_RIGHT_CREATE_FOLDER);
                        }
                    } else {
                        parentDocument = docReterialManager.getDocument(dmsDocument.getParentID());
                    }
                }
                this.parentID = parentDocument.getID();
                this.rootID = parentDocument.getRootID();
            } else if (DmsOperationConstant.SCAN_FOLDER_CREATION_BY_UDF_BY_DATE.equals(createFolderMethod)) {
                String DateFolder = dateFolderFormat.format(new Date());
                if (parentDocument == null) {
                    if (!Utility.isEmpty(folderName) && DmsDocument.FOLDER_TYPE.equals(newParentDocument.getDocumentType())) {
                        if (canCreateFolderFlag) {
                            parentDocument = docOperationManager.createFolder(dmsDocument);
                        } else {
                            throw new ApplicationException(DmsErrorConstant.NO_RIGHT_CREATE_FOLDER);
                        }
                    } else {
                        parentDocument = docReterialManager.getDocument(dmsDocument.getParentID());
                    }
                }
                DmsDocument dateFolder = docReterialManager.getDocumentByNameParentID(DateFolder, parentDocument.getID());
                if (dateFolder == null && DmsDocument.FOLDER_TYPE.equals(parentDocument.getDocumentType())) {
                    dateFolder = (DmsDocument) docForm.getFormData();
                    dateFolder.setDocumentName(DateFolder);
                    dateFolder.setParentID(parentDocument.getID());
                    dateFolder.setRootID(parentDocument.getRootID());
                    dateFolder.setDocumentType(DmsDocument.FOLDER_TYPE);
                    dateFolder.setRecordStatus(GlobalConstant.STATUS_ACTIVE);
                    dateFolder.setItemStatus(DmsVersion.AVAILABLE_STATUS);
                    dateFolder.setCreateType(DmsOperationConstant.DMS_CREATE_BY_UPLOAD);
                    dateFolder = docOperationManager.createFolder(dateFolder);
                }
                if (dateFolder != null) {
                    this.parentID = dateFolder.getID();
                    this.rootID = dateFolder.getRootID();
                    log.debug(" New Parent ID = " + parentID + " rootID = " + rootID);
                } else {
                    this.parentID = parentDocument.getID();
                    this.rootID = parentDocument.getRootID();
                }
            } else if (DmsOperationConstant.SCAN_FOLDER_CREATION_BY_UDF_VALUE_BY_UDF_BY_DATE.equals(createFolderMethod)) {
                String DateFolder = dateFolderFormat.format(new Date());
                if (parentDocument == null || parentDocument.getID() == null) {
                    if (!Utility.isEmpty(folderName) && DmsDocument.FOLDER_TYPE.equals(newParentDocument.getDocumentType())) {
                        if (canCreateFolderFlag) {
                            parentDocument = docOperationManager.createFolder(dmsDocument);
                        } else {
                            throw new ApplicationException(DmsErrorConstant.NO_RIGHT_CREATE_FOLDER);
                        }
                    } else {
                        parentDocument = docReterialManager.getDocument(dmsDocument.getParentID());
                    }
                }
                DmsDocument udfFolder = docReterialManager.getDocumentByNameParentID(this.resolverFolderName(indexFile), parentDocument.getID());
                if (udfFolder == null || udfFolder.getID() == null && DmsDocument.FOLDER_TYPE.equals(parentDocument.getDocumentType())) {
                    udfFolder = (DmsDocument) docForm.getFormData();
                    udfFolder.setDocumentName(this.resolverFolderName(indexFile));
                    udfFolder.setParentID(parentDocument.getID());
                    udfFolder.setRootID(parentDocument.getRootID());
                    udfFolder.setDocumentType(DmsDocument.FOLDER_TYPE);
                    udfFolder.setRecordStatus(GlobalConstant.STATUS_ACTIVE);
                    udfFolder.setItemStatus(DmsVersion.AVAILABLE_STATUS);
                    udfFolder.setCreateType(DmsOperationConstant.DMS_CREATE_BY_UPLOAD);
                    if (canCreateFolderFlag) {
                        udfFolder = docOperationManager.createFolder(udfFolder);
                    } else {
                        throw new ApplicationException(DmsErrorConstant.NO_RIGHT_CREATE_FOLDER);
                    }
                }
                DmsDocument dateFolder = docReterialManager.getDocumentByNameParentID(DateFolder, udfFolder.getID());
                if (dateFolder == null || dateFolder.getID() == null && DmsDocument.FOLDER_TYPE.equals(udfFolder.getDocumentType())) {
                    dateFolder = (DmsDocument) docForm.getFormData();
                    dateFolder.setDocumentName(DateFolder);
                    dateFolder.setParentID(udfFolder.getID());
                    dateFolder.setRootID(udfFolder.getRootID());
                    dateFolder.setDocumentType(DmsDocument.FOLDER_TYPE);
                    dateFolder.setRecordStatus(GlobalConstant.STATUS_ACTIVE);
                    dateFolder.setItemStatus(DmsVersion.AVAILABLE_STATUS);
                    dateFolder.setCreateType(DmsOperationConstant.DMS_CREATE_BY_UPLOAD);
                    if (canCreateFolderFlag) {
                        dateFolder = docOperationManager.createFolder(dateFolder);
                    } else {
                        throw new ApplicationException(DmsErrorConstant.NO_RIGHT_CREATE_FOLDER);
                    }
                }
                if (dateFolder != null) {
                    this.parentID = dateFolder.getID();
                    this.rootID = dateFolder.getRootID();
                } else {
                    this.parentID = parentDocument.getID();
                    this.rootID = parentDocument.getRootID();
                }
            } else if (DmsOperationConstant.SCAN_FOLDER_CREATION_BY_UDF_VALUE_BY_DATE.equals(createFolderMethod)) {
                String DateFolder = dateFolderFormat.format(new Date());
                if (parentDocument == null || parentDocument.getID() == null) {
                    if (!Utility.isEmpty(folderName) && DmsDocument.FOLDER_TYPE.equals(newParentDocument.getDocumentType())) {
                        if (canCreateFolderFlag) {
                            parentDocument = docOperationManager.createFolder(dmsDocument);
                        } else {
                            throw new ApplicationException(DmsErrorConstant.NO_RIGHT_CREATE_FOLDER);
                        }
                    } else {
                        parentDocument = docReterialManager.getDocument(dmsDocument.getParentID());
                    }
                }
                DmsDocument udfFolder = parentDocument;
                DmsDocument dateFolder = docReterialManager.getDocumentByNameParentID(DateFolder, udfFolder.getID());
                if (dateFolder == null || dateFolder.getID() == null && DmsDocument.FOLDER_TYPE.equals(udfFolder.getDocumentType())) {
                    dateFolder = (DmsDocument) docForm.getFormData();
                    dateFolder.setDocumentName(DateFolder);
                    dateFolder.setParentID(udfFolder.getID());
                    dateFolder.setRootID(udfFolder.getRootID());
                    dateFolder.setDocumentType(DmsDocument.FOLDER_TYPE);
                    dateFolder.setRecordStatus(GlobalConstant.STATUS_ACTIVE);
                    dateFolder.setItemStatus(DmsVersion.AVAILABLE_STATUS);
                    dateFolder.setCreateType(DmsOperationConstant.DMS_CREATE_BY_UPLOAD);
                    if (canCreateFolderFlag) {
                        dateFolder = docOperationManager.createFolder(dateFolder);
                    } else {
                        throw new ApplicationException(DmsErrorConstant.NO_RIGHT_CREATE_FOLDER);
                    }
                }
                if (dateFolder != null) {
                    this.parentID = dateFolder.getID();
                    this.rootID = dateFolder.getRootID();
                } else {
                    this.parentID = parentDocument.getID();
                    this.rootID = parentDocument.getRootID();
                }
            } else if (DmsOperationConstant.SCAN_FOLDER_DEMO_DP1_DATE.equals(createFolderMethod)) {
                String DateFolder = dateFolderFormat.format(new Date());
                if (parentDocument == null) {
                    if (!Utility.isEmpty(folderName) && DmsDocument.FOLDER_TYPE.equals(newParentDocument.getDocumentType())) {
                        if (canCreateFolderFlag) {
                            parentDocument = docOperationManager.createFolder(dmsDocument);
                        } else {
                            throw new ApplicationException(DmsErrorConstant.NO_RIGHT_CREATE_FOLDER);
                        }
                    } else {
                        parentDocument = docReterialManager.getDocument(dmsDocument.getParentID());
                    }
                }
                DmsDocument dateFolder = docReterialManager.getDocumentByNameParentID(DateFolder, parentDocument.getID());
                if (dateFolder == null && DmsDocument.FOLDER_TYPE.equals(parentDocument.getDocumentType())) {
                    dateFolder = (DmsDocument) docForm.getFormData();
                    dateFolder.setDocumentName(DateFolder);
                    dateFolder.setParentID(parentDocument.getID());
                    dateFolder.setRootID(parentDocument.getRootID());
                    dateFolder.setDocumentType(DmsDocument.FOLDER_TYPE);
                    dateFolder.setRecordStatus(GlobalConstant.STATUS_ACTIVE);
                    dateFolder.setItemStatus(DmsVersion.AVAILABLE_STATUS);
                    dateFolder.setCreateType(DmsOperationConstant.DMS_CREATE_BY_UPLOAD);
                    if (canCreateFolderFlag) {
                        dateFolder = docOperationManager.createFolder(dateFolder);
                    } else {
                        throw new ApplicationException(DmsErrorConstant.NO_RIGHT_CREATE_FOLDER);
                    }
                }
                if (dateFolder != null) {
                    this.parentID = dateFolder.getID();
                    this.rootID = dateFolder.getRootID();
                    log.debug(" New Parent ID = " + parentID + " rootID = " + rootID);
                } else {
                    this.parentID = parentDocument.getID();
                    this.rootID = parentDocument.getRootID();
                }
            } else if (DmsOperationConstant.SCAN_FOLDER_DEMO_DP1_DP2_DATE.equals(createFolderMethod)) {
                String DateFolder = dateFolderFormat.format(new Date());
                if (parentDocument == null) {
                    if (!Utility.isEmpty(folderName) && DmsDocument.FOLDER_TYPE.equals(newParentDocument.getDocumentType())) {
                        if (canCreateFolderFlag) {
                            parentDocument = docOperationManager.createFolder(dmsDocument);
                        } else {
                            throw new ApplicationException(DmsErrorConstant.NO_RIGHT_CREATE_FOLDER);
                        }
                    } else {
                        parentDocument = docReterialManager.getDocument(dmsDocument.getParentID());
                    }
                }
                DmsDefaultProfileSettingDAObject dmsDefaultProfileDAO = new DmsDefaultProfileSettingDAObject(sessionContainer, conn);
                DmsDefaultProfileSetting defaultProfile = (DmsDefaultProfileSetting) dmsDefaultProfileDAO.getObjectByID(new Integer(2));
                DmsDocument dpfFolder = docReterialManager.getDocumentByNameParentID(this.userDef2, parentDocument.getID());
                if (dpfFolder == null || dpfFolder.getID() == null && DmsDocument.FOLDER_TYPE.equals(parentDocument.getDocumentType())) {
                    dpfFolder = (DmsDocument) docForm.getFormData();
                    dpfFolder.setDocumentName(this.userDef2);
                    dpfFolder.setParentID(parentDocument.getID());
                    dpfFolder.setRootID(parentDocument.getRootID());
                    dpfFolder.setDocumentType(DmsDocument.FOLDER_TYPE);
                    dpfFolder.setRecordStatus(GlobalConstant.STATUS_ACTIVE);
                    dpfFolder.setItemStatus(DmsVersion.AVAILABLE_STATUS);
                    dpfFolder.setCreateType(DmsOperationConstant.DMS_CREATE_BY_UPLOAD);
                    if (canCreateFolderFlag) {
                        dpfFolder = docOperationManager.createFolder(dpfFolder);
                    } else {
                        throw new ApplicationException(DmsErrorConstant.NO_RIGHT_CREATE_FOLDER);
                    }
                }
                DmsDocument dateFolder = docReterialManager.getDocumentByNameParentID(DateFolder, dpfFolder.getID());
                if (dateFolder == null || dateFolder.getID() == null && DmsDocument.FOLDER_TYPE.equals(dpfFolder.getDocumentType())) {
                    dateFolder = (DmsDocument) docForm.getFormData();
                    dateFolder.setDocumentName(DateFolder);
                    dateFolder.setParentID(dpfFolder.getID());
                    dateFolder.setRootID(dpfFolder.getRootID());
                    dateFolder.setDocumentType(DmsDocument.FOLDER_TYPE);
                    dateFolder.setRecordStatus(GlobalConstant.STATUS_ACTIVE);
                    dateFolder.setItemStatus(DmsVersion.AVAILABLE_STATUS);
                    dateFolder.setCreateType(DmsOperationConstant.DMS_CREATE_BY_UPLOAD);
                    if (canCreateFolderFlag) {
                        dateFolder = docOperationManager.createFolder(dateFolder);
                    } else {
                        throw new ApplicationException(DmsErrorConstant.NO_RIGHT_CREATE_FOLDER);
                    }
                }
                if (dateFolder != null) {
                    this.parentID = dateFolder.getID();
                    this.rootID = dateFolder.getRootID();
                } else {
                    this.parentID = parentDocument.getID();
                    this.rootID = parentDocument.getRootID();
                }
            } else {
                this.parentID = dmsDocument.getParentID();
                this.rootID = dmsDocument.getRootID();
            }
            docOperationManager.release();
            docReterialManager.release();
        }
        log.debug(" New Parent ID = " + parentID + " rootID = " + rootID);
        analyzedResult = this.analyzeForCustomizationAction(sessionContainer, conn, docForm, infile);
        return analyzedResult;
    }

    public String resolveDocumentName(org.w3c.dom.Document indexFile) throws ApplicationException {
        String documentName = "";
        SimpleDateFormat sdf = new SimpleDateFormat(SystemParameterFactory.getSystemParameter(SystemParameterConstant.DMS_SCAN_DOCUMENT_NAME_AUTO_GEN_DATETIME_FORMAT));
        String resolveNameMethod = SystemParameterFactory.getSystemParameter(SystemParameterConstant.DMS_SCAN_DOCUMENT_NAME_FORMAT);
        if (DmsOperationConstant.SCAN_DOCUMENT_NAME_AUTO_GEN.equals(resolveNameMethod)) {
            documentName = sdf.format(new Date()) + fileExten;
        } else if (DmsOperationConstant.SCAN_DOCUMENT_NAME_IN_UDF.equals(resolveNameMethod)) {
            Integer udfDetailPosition = TextUtility.parseIntegerObj(SystemParameterFactory.getSystemParameter(SystemParameterConstant.DMS_SCAN_DOCUMENT_NAME_IN_UDF_FIELD_POSITION));
            NodeList fieldList = indexFile.getElementsByTagName("field");
            Node subnode = fieldList.item(udfDetailPosition.intValue()).getFirstChild();
            String fieldValue = "";
            if (subnode != null) {
                documentName = subnode.getNodeValue() + fileExten;
            }
        } else {
            Integer udfDetailPosition = TextUtility.parseIntegerObj(SystemParameterFactory.getSystemParameter(SystemParameterConstant.DMS_SCAN_DOCUMENT_NAME_IN_UDF_FIELD_POSITION));
            NodeList fieldList = indexFile.getElementsByTagName("page");
            Node subnode = fieldList.item(udfDetailPosition.intValue()).getFirstChild();
            String fieldValue = "";
            if (subnode != null) {
                documentName = subnode.getNodeValue();
            }
        }
        return documentName;
    }

    public String getDocumentProfileName(org.w3c.dom.Document indexFile) throws ApplicationException {
        String folderName = "";
        NodeList nl = indexFile.getElementsByTagName("document_profile");
        for (int i = 0; i < nl.getLength(); i++) {
            Element targetNode = (Element) nl.item(i);
            folderName = targetNode.getAttribute("name");
        }
        return folderName;
    }

    public String resolverFolderName(org.w3c.dom.Document indexFile) throws ApplicationException {
        String folderName = "";
        NodeList nl = indexFile.getElementsByTagName("document_profile");
        for (int i = 0; i < nl.getLength(); i++) {
            Element targetNode = (Element) nl.item(i);
            folderName = targetNode.getAttribute("name");
            log.debug("udf Class = " + folderName);
        }
        return folderName;
    }

    public String resolverFolderNameUsingUDFValue(org.w3c.dom.Document indexFile) throws ApplicationException {
        String folderName = "";
        String targetUDFValuePosition = SystemParameterFactory.getSystemParameter(SystemParameterConstant.DMS_SCAN_DOCUMENT_NAME_IN_UDF_FIELD_VALUE_POSITION);
        NodeList nl = indexFile.getElementsByTagName("field");
        for (int i = 0; i < nl.getLength(); i++) {
            if (i == TextUtility.parseInteger(targetUDFValuePosition)) {
                Node subnode = nl.item(i).getFirstChild();
                if (subnode != null) {
                    folderName = subnode.getNodeValue();
                }
            }
        }
        return folderName;
    }

    public boolean resolverSubmitSystemWorkflow(org.w3c.dom.Document indexFile) throws ApplicationException {
        String submitSystemWorkflow = "false";
        NodeList nl = indexFile.getElementsByTagName("descriptor");
        for (int i = 0; i < nl.getLength(); i++) {
            Element attr = (Element) nl.item(i);
            if (attr.getAttributeNode(DmsDocumentAnalyzer.SUBMIT_SYSTEM_WORKFLOW) != null) {
                submitSystemWorkflow = attr.getAttributeNode(DmsDocumentAnalyzer.SUBMIT_SYSTEM_WORKFLOW).getValue();
            }
        }
        return TextUtility.parseBoolean(submitSystemWorkflow);
    }

    public String resolverDocumentInfo(org.w3c.dom.Document indexFile, String tagName) throws ApplicationException {
        String value = "";
        NodeList nl = indexFile.getElementsByTagName(tagName);
        for (int i = 0; i < nl.getLength(); i++) {
            Node subnode = nl.item(i).getFirstChild();
            if (subnode != null) {
                value = subnode.getNodeValue();
            }
        }
        return value;
    }

    public String resolverSingleDocumentInfoByTagAttribute(org.w3c.dom.Document indexFile, String tagName, String attribute) throws ApplicationException {
        String value = "";
        String folderName = "";
        NodeList nl = indexFile.getElementsByTagName(tagName);
        for (int i = 0; i < nl.getLength(); i++) {
            Element targetNode = (Element) nl.item(i);
            value = targetNode.getAttribute(attribute);
        }
        return value;
    }

    public String resolverFullText(org.w3c.dom.Document indexFile) throws ApplicationException {
        String fullText = "";
        NodeList nl = indexFile.getElementsByTagName("full_text");
        for (int i = 0; i < nl.getLength(); i++) {
            Node subnode = nl.item(i).getFirstChild();
            if (subnode != null) {
                fullText = subnode.getNodeValue();
            }
        }
        return fullText;
    }

    public Boolean analyzeForCustomizationAction(SessionContainer sessionContainer, Connection conn, MaintDmsDocumentForm docForm, java.io.InputStream infile) throws ApplicationException {
        Boolean analyzedResult = new Boolean(false);
        if (docForm.getFormFieldXmlFile() != null) {
            String createFolderDateFormat = SystemParameterFactory.getSystemParameter(SystemParameterConstant.DMS_SCAN_FOLDER_DATE_FORMAT);
            SimpleDateFormat dateFolderFormat = new SimpleDateFormat(createFolderDateFormat, Utility.getLocaleByString(SystemParameterFactory.getSystemParameter(SystemParameterConstant.LOCALE)));
            DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            org.w3c.dom.Document indexFile = null;
            try {
                docBuilder = dfactory.newDocumentBuilder();
                indexFile = docBuilder.parse(infile);
            } catch (Exception e) {
                log.error(e, e);
            }
            fileExten = docForm.getTrueFileName();
            this.documentName = this.resolveDocumentName(indexFile);
            this.description = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.DESCRIPTION_TAG);
            this.referenceNo = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.REFERENCE_NUM_TAG);
            this.userDef1 = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.USER_DEF_1);
            this.userDef2 = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.USER_DEF_2);
            this.userDef3 = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.USER_DEF_3);
            this.userDef4 = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.USER_DEF_4);
            this.userDef5 = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.USER_DEF_5);
            this.userDef6 = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.USER_DEF_6);
            this.userDef7 = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.USER_DEF_7);
            this.userDef8 = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.USER_DEF_8);
            this.userDef9 = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.USER_DEF_9);
            this.userDef10 = this.resolverDocumentInfo(indexFile, DmsDocumentAnalyzer.USER_DEF_10);
            this.fullText = this.resolverFullText(indexFile);
        }
        return analyzedResult;
    }
}
