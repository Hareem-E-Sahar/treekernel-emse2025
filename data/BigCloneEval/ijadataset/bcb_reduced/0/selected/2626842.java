package com.dcivision.dms.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.xml.sax.InputSource;
import com.dcivision.alert.bean.UpdateAlert;
import com.dcivision.alert.core.AdapterMaster;
import com.dcivision.audit.AuditTrailConstant;
import com.dcivision.audit.core.AuditTrailManager;
import com.dcivision.dms.DmsErrorConstant;
import com.dcivision.dms.DmsOperationConstant;
import com.dcivision.dms.bean.DmsArchive;
import com.dcivision.dms.bean.DmsArchiveDetail;
import com.dcivision.dms.bean.DmsContent;
import com.dcivision.dms.bean.DmsDefaultProfileSetting;
import com.dcivision.dms.bean.DmsDocument;
import com.dcivision.dms.bean.DmsDocumentDetail;
import com.dcivision.dms.bean.DmsLocMaster;
import com.dcivision.dms.bean.DmsRoot;
import com.dcivision.dms.bean.DmsValidation;
import com.dcivision.dms.bean.DmsVersion;
import com.dcivision.dms.bean.MtmDocumentRelationship;
import com.dcivision.dms.dao.DmsArchiveDAObject;
import com.dcivision.dms.dao.DmsArchiveDetailDAObject;
import com.dcivision.dms.dao.DmsContentDAObject;
import com.dcivision.dms.dao.DmsDefaultProfileSettingDAObject;
import com.dcivision.dms.dao.DmsDocumentDAObject;
import com.dcivision.dms.dao.DmsDocumentDetailDAObject;
import com.dcivision.dms.dao.DmsLocMasterDAObject;
import com.dcivision.dms.dao.DmsRootDAObject;
import com.dcivision.dms.dao.DmsVersionDAObject;
import com.dcivision.dms.web.MaintDmsArchiveForm;
import com.dcivision.framework.ApplicationException;
import com.dcivision.framework.EventLogger;
import com.dcivision.framework.EventLoggerConstant;
import com.dcivision.framework.GlobalConstant;
import com.dcivision.framework.MessageResourcesFactory;
import com.dcivision.framework.SessionContainer;
import com.dcivision.framework.SystemFunctionConstant;
import com.dcivision.framework.SystemParameterConstant;
import com.dcivision.framework.SystemParameterFactory;
import com.dcivision.framework.TextUtility;
import com.dcivision.framework.UserInfoFactory;
import com.dcivision.framework.Utility;
import com.dcivision.framework.bean.SysUserDefinedIndex;
import com.dcivision.framework.bean.SysUserDefinedIndexDetail;
import com.dcivision.framework.dao.SysUserDefinedIndexDAObject;
import com.dcivision.framework.web.AbstractActionForm;
import com.dcivision.workflow.bean.MtmWorkflowProgressSystemObject;
import com.dcivision.workflow.dao.MtmWorkflowProgressSystemObjectDAObject;
import com.dcivision.workflow.dao.WorkflowProgressDAObject;

/**
 * <p>Class Name:       DmsArchiveManager.java    </p>
 * <p>Description:      The class mainly handle the archive operation functionalies, such as insert, edit and delete.</p>
 *
 * @author              Jenny Li
 * @company             DCIVision Limited
 * @creation date       13/10/2005
 * @version             $Revision: 1.21.2.6 $
 */
public class DmsArchiveManager {

    public static final String REVISION = "$Revision: 1.21.2.6 $";

    protected Log log = LogFactory.getLog(this.getClass().getName());

    private SessionContainer sessionContainer = null;

    private Connection conn = null;

    String errorCode = null;

    /**
   *  Constructor - Creates a new instance of DmsArchiveManager
   */
    public DmsArchiveManager(SessionContainer sessionContainer, Connection conn) {
        this.sessionContainer = sessionContainer;
        this.conn = conn;
        errorCode = MessageResourcesFactory.getMessage(sessionContainer.getSessionLocale(), EventLoggerConstant.LOG_DMS_ARCHVIE_ERROR);
    }

    /**
   * start archive function.
   * @param dmsArchiveList
   * @param dmsArchive
   * @param clipboardList
   * @param defaultProfileSettingList
   * @param offlineViewPath
   * @param destinationID
   * @param userDefinedFieldID
   * @param archiveNow
   * @throws ApplicationException
   */
    public String[] startArchive(List dmsArchiveList, DmsArchive dmsArchive, ArrayList clipboardList, List defaultProfileSettingList, String offlineViewPath, String destinationID, String[] userDefinedFieldID, String archiveNow) throws ApplicationException {
        DmsArchiveDAObject archiveDAO = new DmsArchiveDAObject(sessionContainer, conn);
        DocumentRetrievalManager docRetrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
        DocumentOperationManager docOperationManager = new DocumentOperationManager(sessionContainer, conn);
        boolean genExcel = SystemParameterFactory.getSystemParameterBoolean(SystemParameterConstant.DMS_ENABLE_ARCHIVE_EXCEL);
        if (!checkHasArchiveFile(dmsArchiveList)) {
            if ("Y".equals(archiveNow)) {
                throw new ApplicationException(DmsErrorConstant.NO_DOCUMENT_TO_ARCHIVE);
            } else {
                EventLogger.log(dmsArchive.getCreatorID(), null, DmsErrorConstant.NO_DOCUMENT_TO_ARCHIVE, errorCode, SystemFunctionConstant.DMS_ARCHIVE, null);
            }
        }
        DmsDocument destionDocument = docRetrievalManager.getDocument(TextUtility.parseIntegerObj(destinationID));
        if (DmsDocument.COMPOUND_DOC_TYPE.equals(destionDocument.getDocumentType())) {
            dmsArchiveList.add(destionDocument);
        } else if (DmsDocument.PAPER_DOC_TYPE.equals(destionDocument.getDocumentType())) {
            dmsArchiveList.add(destionDocument);
        }
        List archiveList = this.getCanArchiveList(dmsArchiveList, destionDocument);
        List divList = getDivList(archiveList);
        if (divList.size() > 0 && TextUtility.parseInteger(divList.get(divList.size() - 1).toString()) < archiveList.size() - 1) {
            divList.add(divList.size(), new Integer(archiveList.size() - 1));
        }
        List pathList = this.getFolderPath(archiveList, divList, dmsArchive, archiveNow);
        String folderPath = "";
        if (pathList.size() > 0) {
            folderPath = pathList.get(0).toString();
        }
        if (archiveList.size() > 0) {
            dmsArchive = (DmsArchive) archiveDAO.insertObject(dmsArchive);
        }
        int k = 0;
        int u = 0;
        String[] documentIDs = null;
        String[][] docVersionIDList = null;
        if (divList.size() > 0) {
            documentIDs = new String[TextUtility.parseInteger(divList.get(0).toString()) + 1];
            docVersionIDList = new String[TextUtility.parseInteger(divList.get(0).toString()) + 1][];
        } else {
            documentIDs = new String[archiveList.size()];
            docVersionIDList = new String[archiveList.size()][];
        }
        this.createArchiveDetail(archiveList, dmsArchive, divList);
        List physicalPathList = new ArrayList();
        for (int i = 0; i < archiveList.size(); i++) {
            u++;
            DmsDocument document = (DmsDocument) archiveList.get(i);
            documentIDs[u - 1] = document.getID().toString();
            String name = TextUtility.getURLEncodeInUTF8(document.getDocumentName());
            List documentVersionToArchiveList = new ArrayList();
            if (DmsArchive.ARCHIVE_TYPE.equals(dmsArchive.getArchiveType())) {
                documentVersionToArchiveList = docRetrievalManager.getVersionListByDocumentID(document.getID());
            } else {
                DmsVersion topVersion = docRetrievalManager.getTopVersionByDocumentID(document.getID());
                documentVersionToArchiveList.add(topVersion);
            }
            docVersionIDList[u - 1] = new String[documentVersionToArchiveList.size()];
            for (int j = 0; j < documentVersionToArchiveList.size(); j++) {
                DmsVersion tmpVersion = (DmsVersion) documentVersionToArchiveList.get(j);
                docVersionIDList[u - 1][j] = tmpVersion.getID().toString();
                DmsContent tmpContent = docRetrievalManager.getContentByContentID(tmpVersion.getContentID());
                String converted = tmpContent.getConvertedName();
                String foldersDir = docRetrievalManager.getFoldersDirectories(document).toString();
                if (!DmsDocument.PAPER_DOC_TYPE.equals(document.getDocumentType())) {
                    this.copyPhysicalFiles(foldersDir + converted, folderPath + "/" + converted, dmsArchive.getCreatorID());
                }
                if (DmsArchive.ARCHIVE_TYPE.equals(dmsArchive.getArchiveType())) {
                    physicalPathList.add(foldersDir + converted);
                    if (!Utility.isEmpty(clipboardList)) {
                        if (clipboardList.size() > 0) {
                            if (clipboardList.contains(document.getID())) {
                                clipboardList.remove(document.getID());
                            }
                        }
                    }
                }
                docOperationManager.archiveDocumentVersion(document, tmpVersion.getID().toString(), dmsArchive.getID(), dmsArchive.getArchiveType());
            }
            if (divList.size() > 0 && divList.contains(new Integer(i))) {
                k++;
                String dataFilePath = folderPath + "/data_file.xml";
                String docTypeFilePath = folderPath + "/doc_type.txt";
                GenArchiveIndexXML(documentIDs, docVersionIDList, dataFilePath, dmsArchive);
                this.copyOfflineView(offlineViewPath + "/Dll", folderPath.substring(0, folderPath.indexOf("/image")) + "/Dll", dmsArchive.getCreatorID());
                this.copyOfflineView(offlineViewPath + "/ArchiveViewer.exe", folderPath.substring(0, folderPath.indexOf("/image")) + "/ArchiveViewer.exe", dmsArchive.getCreatorID());
                if (genExcel) {
                    this.GenArchiveExcel(documentIDs, docVersionIDList, folderPath, dmsArchive, defaultProfileSettingList);
                }
                if (divList.size() > k + 1 || divList.size() == k + 1) {
                    documentIDs = new String[TextUtility.parseInteger(divList.get(k).toString()) - i];
                    docVersionIDList = new String[TextUtility.parseInteger(divList.get(k).toString()) - i][];
                }
                u = 0;
                if (pathList.size() > k + 1 || pathList.size() == k + 1) {
                    folderPath = pathList.get(k).toString();
                }
            }
        }
        if (archiveList.size() > 0 && (divList.size() < 0 || divList.size() == 0)) {
            String dataFilePath = folderPath + "/data_file.xml";
            String docTypeFilePath = folderPath + "/doc_type.txt";
            GenArchiveIndexXML(documentIDs, docVersionIDList, dataFilePath, dmsArchive);
            this.copyOfflineView(offlineViewPath + "/Dll", folderPath.substring(0, folderPath.indexOf("/image")) + "/Dll", dmsArchive.getCreatorID());
            this.copyOfflineView(offlineViewPath + "/ArchiveViewer.exe", folderPath.substring(0, folderPath.indexOf("/image")) + "/ArchiveViewer.exe", dmsArchive.getCreatorID());
            if (genExcel) {
                this.GenArchiveExcel(documentIDs, docVersionIDList, folderPath, dmsArchive, defaultProfileSettingList);
            }
        }
        if (!"Y".equals(archiveNow)) {
            try {
                conn.commit();
            } catch (Exception e) {
                this.handleException(dmsArchive.getCreatorID(), e);
            }
        }
        this.addMessageToEvelog(archiveList, dmsArchive.getCreatorID());
        for (int i = 0; i < physicalPathList.size(); i++) {
            String path = (String) physicalPathList.get(i);
            if (!Utility.isEmpty(path)) {
                File physicalfile = new File(path);
                physicalfile.delete();
            }
        }
        String[] messageStr = new String[dmsArchiveList.size()];
        messageStr[0] = new Integer(archiveList.size()).toString();
        for (int i = 0; i < dmsArchiveList.size(); i++) {
            if (!archiveList.contains(dmsArchiveList.get(i))) {
                DmsDocument doc = (DmsDocument) dmsArchiveList.get(i);
                String path = docRetrievalManager.getLocationPath(doc.getParentID()) + doc.getDocumentName();
                messageStr[i + 1] = path;
            }
        }
        return messageStr;
    }

    /**
   * create detail of archvie.
   * @param archiveList
   * @param dmsArchive
   * @param divList
   * @param form
   * @param request
   * @throws ApplicationException
   */
    protected void createArchiveDetail(List archiveList, DmsArchive dmsArchive, List divList) throws ApplicationException {
        DocumentRetrievalManager docRetrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
        DocumentOperationManager docOperationManager = new DocumentOperationManager(sessionContainer, conn);
        int q = 0;
        for (int i = 0; i < archiveList.size(); i++) {
            DmsDocument document = (DmsDocument) archiveList.get(i);
            List documentVersionToArchiveList = new ArrayList();
            if (DmsArchive.ARCHIVE_TYPE.equals(dmsArchive.getArchiveType())) {
                documentVersionToArchiveList = docRetrievalManager.getVersionListByDocumentID(document.getID());
            } else {
                DmsVersion topVersion = docRetrievalManager.getTopVersionByDocumentID(document.getID());
                documentVersionToArchiveList.add(topVersion);
            }
            for (int j = 0; j < documentVersionToArchiveList.size(); j++) {
                DmsVersion tmpVersion = (DmsVersion) documentVersionToArchiveList.get(j);
                DmsArchiveDetail archiveDetail = new DmsArchiveDetail();
                archiveDetail.setArchiveID(dmsArchive.getID());
                archiveDetail.setDocumentID(document.getID());
                archiveDetail.setVersionID(tmpVersion.getID());
                archiveDetail.setArchiveSegmentID(new Integer(q + 1));
                archiveDetail.setCreatorID(dmsArchive.getCreatorID());
                archiveDetail.setUpdaterID(dmsArchive.getUpdaterID());
                archiveDetail = docOperationManager.createArchiveDetail(archiveDetail);
                if (divList.size() > 0 && divList.contains(new Integer(i))) {
                    q++;
                }
            }
        }
    }

    private void copyPhysicalFiles(String sourcePath, String targetPath, Integer userRecordID) {
        try {
            File inputFile = new File(sourcePath);
            java.io.FileInputStream fis = new java.io.FileInputStream(inputFile);
            byte[] content = new byte[1024 * 1024];
            java.io.FileOutputStream fos = new java.io.FileOutputStream(targetPath);
            int bufferSize = 8192;
            byte[] buffer = new byte[bufferSize];
            int length = -1;
            while ((length = fis.read(buffer, 0, bufferSize)) != -1) {
                fos.write(buffer, 0, length);
            }
            fos.flush();
            fos.close();
            fis.close();
        } catch (Exception e) {
            this.handleException(userRecordID, e);
        }
    }

    /**
   * this function is to general XML.
   * @param ctx
   * @param conn
   * @param docList
   * @param versionsList
   * @param dataFilePath
   * @param dmsArchive
   */
    private void GenArchiveIndexXML(String[] docList, String[][] versionsList, String dataFilePath, DmsArchive dmsArchive) {
        try {
            ArchiveDataFileParser archiveParser = new ArchiveDataFileParser();
            archiveParser.setSessionContainer(sessionContainer);
            archiveParser.setConnection(conn);
            archiveParser.setDocList(docList);
            archiveParser.setVersionsList(versionsList);
            archiveParser.setDmsArchive(dmsArchive);
            archiveParser.setDataFilePath(dataFilePath);
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            InputSource inputSource = new InputSource();
            SAXSource source = new SAXSource(archiveParser, inputSource);
            StreamResult result = new StreamResult(dataFilePath);
            transformer.transform(source, result);
        } catch (Exception e) {
            this.handleException(dmsArchive.getCreatorID(), e);
        }
    }

    /**
   * copy offline viewer files to archvie file.
   * @param sourcePath
   * @param targetPath
   */
    protected void copyOfflineView(String sourcePath, String targetPath, Integer userRecordID) {
        int BUFSIZE = 65536;
        try {
            File inputFile = new File(sourcePath);
            if (inputFile.isDirectory()) {
                File targetFile = new File(targetPath);
                if (!targetFile.exists()) {
                    targetFile.mkdir();
                }
                File listFileSource[] = inputFile.listFiles();
                if (listFileSource != null) {
                    for (int i = 0; i < listFileSource.length; i++) {
                        if (listFileSource[i].isFile()) {
                            this.copyPhysicalFiles(listFileSource[i].getPath(), targetPath + "/" + listFileSource[i].getName(), userRecordID);
                        }
                    }
                }
            } else {
                this.copyPhysicalFiles(inputFile.getPath(), targetPath, userRecordID);
            }
        } catch (Exception e) {
            this.handleException(userRecordID, e);
        }
    }

    private void GenArchiveExcel(String[] docList, String[][] versionsList, String folderPath, DmsArchive dmsArchive, List defaultProfileSettingList) throws ApplicationException {
        DmsContentDAObject dmsContentDAO = new DmsContentDAObject(sessionContainer, conn);
        DocumentRetrievalManager retrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
        SysUserDefinedIndexDAObject sysUserDefinedIndexDAO = new SysUserDefinedIndexDAObject(sessionContainer, conn);
        DmsDocumentDetailDAObject dmsDocumentDetailDAO = new DmsDocumentDetailDAObject(sessionContainer, conn);
        DocumentRetrievalManager docRetrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Document Information");
        sheet.setDefaultColumnWidth((short) 18);
        short rowNum = 0;
        HSSFRow row = sheet.createRow(rowNum);
        int totalFieldCount = Integer.parseInt(com.dcivision.framework.SystemParameterFactory.getSystemParameter(com.dcivision.framework.SystemParameterConstant.DMS_DEFAULT_PROFILE_FIELD_COUNT));
        List udfDetailList = new ArrayList();
        SysUserDefinedIndexDetail udfDetail = null;
        List dmsDocumentDetailList = new ArrayList();
        DmsDocumentDetail dmsDocumentDetail = null;
        Method getFileMth = null;
        String fieldValue = "";
        row = sheet.createRow(rowNum++);
        createCell(wb, row, (short) 0, "column", MessageResourcesFactory.getMessage(sessionContainer.getSessionLocale(), "dms.label.name_label_D"));
        createCell(wb, row, (short) 1, "column", MessageResourcesFactory.getMessage(sessionContainer.getSessionLocale(), "dms.label.converted_name"));
        createCell(wb, row, (short) 2, "column", MessageResourcesFactory.getMessage(sessionContainer.getSessionLocale(), "dms.columnheader.location"));
        createCell(wb, row, (short) 3, "column", MessageResourcesFactory.getMessage(sessionContainer.getSessionLocale(), "dms.label.document_type"));
        createCell(wb, row, (short) 4, "column", MessageResourcesFactory.getMessage(sessionContainer.getSessionLocale(), "dms.columnheader.item_size"));
        createCell(wb, row, (short) 5, "column", MessageResourcesFactory.getMessage(sessionContainer.getSessionLocale(), "common.label.creator_name"));
        createCell(wb, row, (short) 6, "column", MessageResourcesFactory.getMessage(sessionContainer.getSessionLocale(), "dms.columnheader.profile_name"));
        createCell(wb, row, (short) 7, "column", MessageResourcesFactory.getMessage(sessionContainer.getSessionLocale(), "dms.label.reference_no"));
        createCell(wb, row, (short) 8, "column", MessageResourcesFactory.getMessage(sessionContainer.getSessionLocale(), "dms.label.description"));
        createCell(wb, row, (short) 9, "column", MessageResourcesFactory.getMessage(sessionContainer.getSessionLocale(), "dms.columnheader.item_status"));
        createCell(wb, row, (short) 10, "column", MessageResourcesFactory.getMessage(sessionContainer.getSessionLocale(), "dms.label.effective_date"));
        for (int i = 0; i < docList.length; i++) {
            DmsDocument dmsDocument = null;
            SysUserDefinedIndex udfIndex = null;
            String[] versions = versionsList[i];
            dmsDocument = retrievalManager.getDocument(new Integer(docList[i]));
            dmsDocumentDetailList = dmsDocumentDetailDAO.getListByDocumentID(dmsDocument.getID());
            for (int j = 0; j < versions.length; j++) {
                Integer versionID = new Integer(versions[j]);
                DmsVersion dmsVersion = retrievalManager.getVersionByVersionID(versionID);
                DmsContent tmpContent = retrievalManager.getContentByContentID(dmsVersion.getContentID());
                String location = "";
                String documentType = dmsDocument.getDocumentType();
                String itemSize = "";
                String effectiveEndDate = "";
                location = retrievalManager.getLocationPath(dmsDocument.getParentID());
                if (DmsDocument.DOCUMENT_TYPE.equals(dmsDocument.getDocumentType())) {
                    documentType = MessageResourcesFactory.getMessage(sessionContainer.getSessionLocale(), "dms.label.doc_type_D");
                } else if (DmsDocument.COMPOUND_DOC_TYPE.equals(dmsDocument.getDocumentType())) {
                    documentType = MessageResourcesFactory.getMessage(sessionContainer.getSessionLocale(), "dms.label.doc_type_C");
                } else if (DmsDocument.PAPER_DOC_TYPE.equals(dmsDocument.getDocumentType())) {
                    documentType = MessageResourcesFactory.getMessage(sessionContainer.getSessionLocale(), "dms.label.doc_type_H");
                }
                if (!DmsDocument.DOCUMENT_LINK.equals(dmsDocument.getDocumentType()) && !DmsDocument.PAPER_DOC_TYPE.equals(dmsDocument.getDocumentType())) {
                    if (!Utility.isEmpty(dmsDocument.getItemSize())) {
                        itemSize = TextUtility.formatFileSize(dmsDocument.getItemSize().intValue());
                    }
                }
                String dateRange = TextUtility.formatTimestampToDate(dmsDocument.getEffectiveStartDate()) + "~ " + TextUtility.formatTimestampToDate(dmsDocument.getEffectiveEndDate());
                row = sheet.createRow(rowNum++);
                createCell(wb, row, (short) 0, "value", dmsDocument.getDocumentName());
                createCell(wb, row, (short) 1, "value", tmpContent.getConvertedName());
                createCell(wb, row, (short) 2, "value", location);
                createCell(wb, row, (short) 3, "value", documentType);
                createCell(wb, row, (short) 4, "value", itemSize);
                String userName = UserInfoFactory.getUserFullName(dmsDocument.getCreatorID());
                createCell(wb, row, (short) 5, "value", userName);
                String udfName = docRetrievalManager.getUserDefinedTypeByDocumentID(dmsDocument.getID());
                createCell(wb, row, (short) 6, "value", udfName);
                createCell(wb, row, (short) 7, "value", dmsDocument.getReferenceNo());
                createCell(wb, row, (short) 8, "value", dmsDocument.getDescription());
                createCell(wb, row, (short) 9, "value", MessageResourcesFactory.getMessage(sessionContainer.getSessionLocale(), "dms.label.doc_lock_" + dmsDocument.getItemStatus()));
                createCell(wb, row, (short) 10, "value", dateRange);
                int udfRow = 11;
                if (!Utility.isEmpty(defaultProfileSettingList)) {
                    for (int m = 0; m < totalFieldCount; m++) {
                        DmsDefaultProfileSetting setting = ((DmsDefaultProfileSetting) defaultProfileSettingList.get(m));
                        if (setting != null) {
                            try {
                                getFileMth = dmsDocument.getClass().getMethod("getUserDef" + (m + 1), null);
                                String udfValue = (String) getFileMth.invoke(dmsDocument, null);
                                createCell(wb, row, (short) udfRow++, "value", udfValue);
                            } catch (Exception e) {
                                this.handleException(dmsArchive.getCreatorID(), e);
                            }
                        }
                    }
                }
                if (dmsDocumentDetailList.size() > 0) {
                    dmsDocumentDetail = (DmsDocumentDetail) dmsDocumentDetailList.get(0);
                    SysUserDefinedIndex sysUserDefinedIndex = (SysUserDefinedIndex) sysUserDefinedIndexDAO.getObjectByID(dmsDocumentDetail.getUserDefinedFieldID());
                    createCell(wb, row, (short) udfRow++, "value", sysUserDefinedIndex.getUserDefinedType());
                    udfDetailList = retrievalManager.getUDFDetailList(dmsDocumentDetail.getUserDefinedFieldID());
                    for (int l = 0; l < udfDetailList.size(); l++) {
                        udfDetail = (SysUserDefinedIndexDetail) udfDetailList.get(l);
                        DmsDocumentDetail dmsDetail = retrievalManager.getDetailObjectByDocIDUDFDetailID(dmsDocument.getID(), udfDetail.getID());
                        if (dmsDetail != null) {
                            if (SysUserDefinedIndexDetail.DATE_FIELD.equals(udfDetail.getFieldType())) {
                                fieldValue = dmsDetail.getDateValue() == null ? "" : dmsDetail.getDateValue().toString();
                            } else if (SysUserDefinedIndexDetail.STRING_FIELD.equals(udfDetail.getFieldType()) || SysUserDefinedIndexDetail.FIELD_TYPE_SELECT_DATABASE.equals(udfDetail.getFieldType())) {
                                if (!Utility.isEmpty(dmsDetail.getFieldValue())) {
                                    fieldValue = dmsDetail.getFieldValue();
                                } else {
                                    fieldValue = "";
                                }
                            } else if (SysUserDefinedIndexDetail.NUMBER_FIELD.equals(udfDetail.getFieldType())) {
                                fieldValue = dmsDetail.getNumericValue() == null ? "" : dmsDetail.getNumericValue().toString();
                            }
                        }
                        createCell(wb, row, (short) (udfRow + l), "value", fieldValue);
                    }
                }
            }
        }
        try {
            String filename = folderPath + "/archive.xls";
            FileOutputStream fileOut = new FileOutputStream(filename);
            wb.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            this.handleException(dmsArchive.getCreatorID(), e);
        }
    }

    /**
   * Creates a cell and aligns it a certain way.
   *
   * @param wb        the workbook
   * @param row       the row to create the cell in
   * @param column    the column number to create the cell in
   * @param align     the alignment for the cell.
   */
    private static void createCell(HSSFWorkbook wb, HSSFRow row, short column, String type, String value) {
        HSSFCell cell = row.createCell(column);
        cell.setEncoding(cell.ENCODING_UTF_16);
        cell.setCellValue(value);
        if ("column".equals(type)) {
            HSSFCellStyle cellStyle = wb.createCellStyle();
            cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
            cellStyle.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
            cellStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
            cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
            cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
            HSSFFont colFontStyle = wb.createFont();
            colFontStyle.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
            colFontStyle.setFontHeightInPoints((short) 9);
            cellStyle.setFont(colFontStyle);
            cell.setCellStyle(cellStyle);
        }
    }

    /**
   *  Gets the dmsDocument list under the directory.
   *
   *  @param parentID                       The parentID of parent directory.
   *  @param rootID                         The rootID of root directory.
   *  @return                               The List of folders under that directory.
   *  @throws ApplicationException          Throws when list operation fault.
   */
    public List getAllArchiveDocList(Integer parentID, Integer rootID, String startDate, String endDate, String[] userDefinedIndex, Integer ownerID) throws ApplicationException {
        DmsDocumentDAObject dmsDocumentDAO = new DmsDocumentDAObject(sessionContainer, conn);
        List finalList = new ArrayList();
        List tmpList = new ArrayList();
        List documentList = dmsDocumentDAO.getArchiveList(parentID, rootID, startDate, endDate, userDefinedIndex, GlobalConstant.STATUS_ACTIVE, DmsDocument.FOLDER_TYPE, false, ownerID);
        finalList.addAll(documentList);
        List folderList = dmsDocumentDAO.getArchiveList(parentID, rootID, null, null, null, GlobalConstant.STATUS_ACTIVE, DmsDocument.FOLDER_TYPE, true, ownerID);
        for (int i = 0; i < folderList.size(); i++) {
            DmsDocument doc = (DmsDocument) folderList.get(i);
            tmpList = this.getAllArchiveDocList(doc.getID(), rootID, startDate, endDate, userDefinedIndex, ownerID);
            finalList.addAll(tmpList);
        }
        dmsDocumentDAO = null;
        return finalList;
    }

    /**
   * new method to get archive document which belong to gived root parent ids, add in 2006/02/23
   * return document types contains general doc, compound doc and paper doc
   * @param dmsDocumentDAO
   * @param parentIDs
   * @param rootID
   * @param startDate
   * @param endDate
   * @param userDefinedIndex
   * @param ownerID
   * @return archive ArrayList
   * @throws ApplicationException
   */
    public List getAllArchiveDocList(DmsDocumentDAObject dmsDocumentDAO, Integer[] parentIDs, Integer rootID, String startDate, String endDate, String[] userDefinedIndex, Integer ownerID) throws ApplicationException {
        String[] forbidTypes = new String[] { DmsDocument.FOLDER_TYPE, DmsDocument.DOCUMENT_LINK };
        List documentList = dmsDocumentDAO.getArchiveList(parentIDs, rootID, startDate, endDate, userDefinedIndex, GlobalConstant.STATUS_ACTIVE, forbidTypes, true, ownerID);
        String[] folderTypes = new String[] { DmsDocument.FOLDER_TYPE, DmsDocument.COMPOUND_DOC_TYPE, DmsDocument.PAPER_DOC_TYPE };
        List doFolderList = dmsDocumentDAO.getArchiveList(parentIDs, rootID, null, null, null, GlobalConstant.STATUS_ACTIVE, folderTypes, false, ownerID);
        if (doFolderList.size() > 0) {
            Integer[] folderIDs = new Integer[doFolderList.size()];
            for (int i = 0; i < folderIDs.length; i++) {
                folderIDs[i] = ((DmsDocument) doFolderList.get(i)).getID();
            }
            List subDocumentList = this.getAllArchiveDocList(dmsDocumentDAO, folderIDs, rootID, startDate, endDate, userDefinedIndex, ownerID);
            documentList.addAll(subDocumentList);
        }
        return documentList;
    }

    /**
   * the the archvie folder has can archvie files.
   * @param folderList
   * @param request
   * @return boolean
   * @throws ApplicationException
   */
    protected boolean checkHasArchiveFile(List folderList) throws ApplicationException {
        if (folderList.size() > 0) {
            for (int i = 0; i < folderList.size(); i++) {
                DmsDocument dmsDocument = (DmsDocument) folderList.get(i);
                if (DmsDocument.PAPER_DOC_TYPE.equals(dmsDocument.getDocumentType())) {
                    if (hasSubDocument(dmsDocument)) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    /**
   * check compound document or paper document has supporting document.
   * @param dmsDocument
   * @param request
   * @return boolean
   * @throws ApplicationException
   */
    protected boolean hasSubDocument(DmsDocument dmsDocument) throws ApplicationException {
        DocumentOperationManager docOperationManager = new DocumentOperationManager(sessionContainer, conn);
        List list = docOperationManager.getSubDocumentByParentID(dmsDocument.getID());
        if (list.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
   * get the last archvie list for archving.
   * @param dmsArchiveList
   * @param destionDocument
   * @param request
   * @return
   * @throws ApplicationException
   */
    protected List getCanArchiveList(List dmsArchiveList, DmsDocument destionDocument) throws ApplicationException {
        List messageList = new ArrayList();
        List archiveList = new ArrayList();
        DocumentRetrievalManager docRetrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
        DocumentOperationManager docOperationManager = new DocumentOperationManager(sessionContainer, conn);
        for (int k = 0; k < dmsArchiveList.size(); k++) {
            DmsDocument dmsDocument = (DmsDocument) dmsArchiveList.get(k);
            if ((DmsDocument.COMPOUND_DOC_TYPE.equals(dmsDocument.getDocumentType()) && !DmsDocument.COMPOUND_DOC_TYPE.equals(destionDocument.getDocumentType())) || (DmsDocument.PAPER_DOC_TYPE.equals(dmsDocument.getDocumentType()) && !DmsDocument.PAPER_DOC_TYPE.equals(destionDocument.getDocumentType()))) {
                List subList = docOperationManager.getSubDocumentByParentID(dmsDocument.getID());
                List list = new ArrayList();
                if (DmsDocument.COMPOUND_DOC_TYPE.equals(dmsDocument.getDocumentType())) {
                    list.add(dmsDocument);
                }
                list.addAll(subList);
                for (int i = 0; i < list.size(); i++) {
                    boolean mm = true;
                    DmsDocument doc = (DmsDocument) list.get(i);
                    if (this.getStandCount() > 0) {
                        if (!DmsDocument.DOCUMENT_LINK.equals(doc.getDocumentType()) && !DmsDocument.PAPER_DOC_TYPE.equals(doc.getDocumentType())) {
                            int iCount = doc.getItemSize().intValue();
                            if (iCount > this.getStandCount()) {
                                mm = false;
                                String location = null;
                                String path = docRetrievalManager.getLocationPath(doc.getParentID());
                                location = path + "/" + doc.getDocumentName();
                                messageList.add(location);
                            }
                        }
                    }
                }
                if (subList != null) {
                    if (this.checkCanArchive(dmsDocument, subList, dmsDocument.getDocumentType())) {
                        dmsArchiveList.addAll(subList);
                    } else {
                        dmsArchiveList.remove(dmsDocument);
                    }
                }
            }
        }
        for (int i = 0; i < dmsArchiveList.size(); i++) {
            if (this.getStandCount() > 0) {
                DmsDocument doc = (DmsDocument) dmsArchiveList.get(i);
                if (!DmsDocument.DOCUMENT_LINK.equals(doc.getDocumentType()) && !DmsDocument.PAPER_DOC_TYPE.equals(doc.getDocumentType())) {
                    int iCount = doc.getItemSize().intValue();
                    if (iCount > this.getStandCount()) {
                        String location = null;
                        String path = docRetrievalManager.getLocationPath(doc.getParentID());
                        location = path + "/" + doc.getDocumentName();
                        messageList.add(location);
                    } else {
                        archiveList.add(doc);
                    }
                } else if (DmsDocument.PAPER_DOC_TYPE.equals(doc.getDocumentType()) && hasSubDocument(doc)) {
                    archiveList.add(doc);
                }
            }
        }
        return archiveList;
    }

    /**
   * get all can archvie files' id.
   * @param archiveList
   * @return
   */
    public List getDivList(List archiveList) {
        List divList = new ArrayList();
        long maxsize = this.getStandCount();
        long count = 0;
        for (int i = 0; i < archiveList.size(); i++) {
            DmsDocument doc = (DmsDocument) archiveList.get(i);
            if (!DmsDocument.DOCUMENT_LINK.equals(doc.getDocumentType()) && !DmsDocument.PAPER_DOC_TYPE.equals(doc.getDocumentType())) {
                count = count + doc.getItemSize().intValue();
                if (maxsize > 0 && count > maxsize) {
                    divList.add(new Integer(i - 1));
                    count = doc.getItemSize().intValue();
                }
            }
        }
        return divList;
    }

    /**
   * this function is to get the archvie folder's path.
   * @param request
   * @param form
   * @param archiveList
   * @param dmsRootDocument
   * @param divList
   * @return
   * @throws ApplicationException
   */
    public List getFolderPath(List archiveList, List divList, DmsArchive dmsArchive, String archiveNow) throws ApplicationException {
        List pathList = new ArrayList();
        String folderPath = "";
        String archiveRootPath = SystemParameterFactory.getSystemParameter(SystemParameterConstant.DMS_ARCHIVE_FILE_PATH);
        String segment = "";
        int segmentCount = 0;
        int standCount = this.getStandCount();
        if (standCount > 0) {
            if (divList.size() <= 0) {
                folderPath = archiveRootPath + "/" + dmsArchive.getArchiveName() + "/" + dmsArchive.getArchiveName() + "_001" + "/image";
                File folder = new File(folderPath);
                if (!folder.exists()) {
                    folder.mkdirs();
                } else {
                    if ("Y".equals(archiveNow)) {
                        throw new ApplicationException(DmsErrorConstant.DUPLICATE_ARCHIVE_NAME);
                    } else {
                        EventLogger.log(dmsArchive.getCreatorID(), null, DmsErrorConstant.DUPLICATE_ARCHIVE_NAME, errorCode, SystemFunctionConstant.DMS_ARCHIVE, null);
                    }
                }
                pathList.add(0, folderPath);
            } else {
                if (divList.size() > 0 && archiveList.size() > 0) {
                    segmentCount = divList.size();
                }
                for (int i = 0; i < segmentCount; i++) {
                    int m = i + 1;
                    if (m < 10) {
                        segment = "00" + m;
                    } else if (m >= 10 && m < 100) {
                        segment = "0" + m;
                    } else {
                        segment = (new Integer(m)).toString();
                    }
                    folderPath = archiveRootPath + "/" + dmsArchive.getArchiveName() + "/" + dmsArchive.getArchiveName() + "_" + segment + "/image";
                    File folder = new File(folderPath);
                    if (!folder.exists()) {
                        folder.mkdirs();
                    } else {
                        if ("Y".equals(archiveNow)) {
                            throw new ApplicationException(DmsErrorConstant.DUPLICATE_ARCHIVE_NAME);
                        } else {
                            EventLogger.log(dmsArchive.getCreatorID(), null, DmsErrorConstant.DUPLICATE_ARCHIVE_NAME, errorCode, SystemFunctionConstant.DMS_ARCHIVE, null);
                        }
                    }
                    pathList.add(i, folderPath);
                }
            }
        } else {
            if ("Y".equals(archiveNow)) {
                throw new ApplicationException(DmsErrorConstant.NO_CAPACITY_TO_STORE);
            } else {
                EventLogger.log(dmsArchive.getCreatorID(), null, DmsErrorConstant.NO_CAPACITY_TO_STORE, errorCode, SystemFunctionConstant.DMS_ARCHIVE, null);
            }
        }
        return pathList;
    }

    /**
   * get stand Count for offline viewer.
   * @return standCount
   */
    public int getStandCount() {
        int standCount = 0;
        String segment_size = SystemParameterFactory.getSystemParameter(SystemParameterConstant.DMS_ARCHIVE_SEGMENT_SIZE);
        int seg_size = 0;
        if (segment_size.endsWith("K")) {
            seg_size = TextUtility.parseInteger(segment_size.substring(0, segment_size.length() - 1)) * 1024;
        } else if (segment_size.endsWith("M")) {
            seg_size = TextUtility.parseInteger(segment_size.substring(0, segment_size.length() - 1)) * 1024 * 1024;
        } else if (segment_size.endsWith("G")) {
            seg_size = TextUtility.parseInteger(segment_size.substring(0, segment_size.length() - 1)) * 1024 * 1024 * 1024;
        } else {
            seg_size = TextUtility.parseInteger(segment_size);
        }
        String offline_viewer_size = SystemParameterFactory.getSystemParameter(SystemParameterConstant.DMS_ARCHIVE_OFFLINE_VIEWER_SIZE);
        int off_viewer_size = 0;
        if (offline_viewer_size.endsWith("K")) {
            off_viewer_size = TextUtility.parseInteger(offline_viewer_size.substring(0, offline_viewer_size.length() - 1)) * 1024;
        } else if (offline_viewer_size.endsWith("M")) {
            off_viewer_size = TextUtility.parseInteger(offline_viewer_size.substring(0, offline_viewer_size.length() - 1)) * 1024 * 1024;
        } else if (offline_viewer_size.endsWith("G")) {
            off_viewer_size = TextUtility.parseInteger(offline_viewer_size.substring(0, offline_viewer_size.length() - 1)) * 1024 * 1024 * 1024;
        } else {
            off_viewer_size = TextUtility.parseInteger(offline_viewer_size);
        }
        boolean genExcel = SystemParameterFactory.getSystemParameterBoolean(SystemParameterConstant.DMS_ENABLE_ARCHIVE_EXCEL);
        int cel_size = 0;
        if (genExcel) {
            String excel_size = SystemParameterFactory.getSystemParameter(SystemParameterConstant.DMS_ARCHIVE_EXCEL_SIZE);
            if (excel_size.endsWith("K")) {
                cel_size = TextUtility.parseInteger(excel_size.substring(0, excel_size.length() - 1)) * 1024;
            } else if (excel_size.endsWith("M")) {
                cel_size = TextUtility.parseInteger(excel_size.substring(0, excel_size.length() - 1)) * 1024 * 1024;
            } else if (excel_size.endsWith("G")) {
                cel_size = TextUtility.parseInteger(excel_size.substring(0, excel_size.length() - 1)) * 1024 * 1024 * 1024;
            } else {
                cel_size = TextUtility.parseInteger(excel_size);
            }
        }
        int cout = off_viewer_size + cel_size;
        if (seg_size > cout) {
            standCount = seg_size - off_viewer_size - cel_size;
        }
        return standCount;
    }

    /**
   * this function is to check can archive the document or not.
   * @param dmsDocument
   * @param subList
   * @param documentType
   * @return
   */
    protected boolean checkCanArchive(DmsDocument dmsDocument, List subList, String documentType) {
        List list = new ArrayList();
        if (DmsDocument.COMPOUND_DOC_TYPE.equals(documentType)) {
            list.add(dmsDocument);
        }
        list.addAll(subList);
        for (int i = 0; i < list.size(); i++) {
            boolean mm = true;
            DmsDocument doc = (DmsDocument) list.get(i);
            if (!DmsDocument.DOCUMENT_LINK.equals(doc.getDocumentType()) && !DmsDocument.PAPER_DOC_TYPE.equals(doc.getDocumentType())) {
                if (this.getStandCount() > 0) {
                    int iCount = doc.getItemSize().intValue();
                    if (iCount > this.getStandCount()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    protected void addMessageToEvelog(List archiveList, Integer userRecordID) throws ApplicationException {
        String message = MessageResourcesFactory.getMessage(sessionContainer.getSessionLocale(), DmsErrorConstant.DMS_MESSAGE_RECORD_INSTERTED, new Integer(archiveList.size()).toString());
        String msgCode = MessageResourcesFactory.getMessage(sessionContainer.getSessionLocale(), EventLoggerConstant.LOG_DMS_ARCHVIE_MSG);
        EventLogger.log(userRecordID, null, message, msgCode, SystemFunctionConstant.DMS_ARCHIVE, null);
    }

    protected void handleException(Integer userRecordID, Exception e) {
        StringWriter strWriter = new StringWriter();
        PrintWriter prtWriter = new PrintWriter(strWriter);
        e.printStackTrace(prtWriter);
        String errorDetail = strWriter.toString();
        EventLogger.log(userRecordID, null, "", errorCode, SystemFunctionConstant.DMS_ARCHIVE, errorDetail);
    }

    /**
   * start archive function.
   * @param dmsArchiveList
   * @param dmsArchive
   * @param clipboardList
   * @param defaultProfileSettingList
   * @param offlineViewPath
   * @param destinationID
   * @param userDefinedFieldID
   * @param archiveNow
   * @throws ApplicationException
   */
    public String[] startArchive(List dmsArchiveList, DmsArchive dmsArchive, String offlineViewPath) throws ApplicationException {
        DmsArchiveDAObject archiveDAO = new DmsArchiveDAObject(sessionContainer, conn);
        DocumentRetrievalManager docRetrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
        DocumentOperationManager docOperationManager = new DocumentOperationManager(sessionContainer, conn);
        boolean genExcel = SystemParameterFactory.getSystemParameterBoolean(SystemParameterConstant.DMS_ENABLE_ARCHIVE_EXCEL);
        if (!checkHasArchiveFile(dmsArchiveList)) {
            throw new ApplicationException(DmsErrorConstant.NO_DOCUMENT_TO_ARCHIVE);
        }
        List archiveList = dmsArchiveList;
        List divList = getDivList(archiveList);
        if (divList.size() > 0 && TextUtility.parseInteger(divList.get(divList.size() - 1).toString()) < archiveList.size() - 1) {
            divList.add(divList.size(), new Integer(archiveList.size() - 1));
        }
        List pathList = this.getFolderPath(archiveList, divList, dmsArchive, "Y");
        String folderPath = "";
        if (pathList.size() > 0) {
            folderPath = pathList.get(0).toString();
        }
        if (archiveList.size() > 0) {
            dmsArchive = (DmsArchive) archiveDAO.insertObject(dmsArchive);
        }
        int k = 0;
        int u = 0;
        String[] documentIDs = null;
        String[][] docVersionIDList = null;
        if (divList.size() > 0) {
            documentIDs = new String[TextUtility.parseInteger(divList.get(0).toString()) + 1];
            docVersionIDList = new String[TextUtility.parseInteger(divList.get(0).toString()) + 1][];
        } else {
            documentIDs = new String[archiveList.size()];
            docVersionIDList = new String[archiveList.size()][];
        }
        this.createArchiveDetail(archiveList, dmsArchive, divList);
        List physicalPathList = new ArrayList();
        for (int i = 0; i < archiveList.size(); i++) {
            u++;
            DmsDocument document = (DmsDocument) archiveList.get(i);
            documentIDs[u - 1] = document.getID().toString();
            String name = TextUtility.getURLEncodeInUTF8(document.getDocumentName());
            List documentVersionToArchiveList = new ArrayList();
            if (DmsArchive.ARCHIVE_TYPE.equals(dmsArchive.getArchiveType())) {
                documentVersionToArchiveList = docRetrievalManager.getVersionListByDocumentID(document.getID());
            } else {
                DmsVersion topVersion = docRetrievalManager.getTopVersionByDocumentID(document.getID());
                documentVersionToArchiveList.add(topVersion);
            }
            docVersionIDList[u - 1] = new String[documentVersionToArchiveList.size()];
            for (int j = 0; j < documentVersionToArchiveList.size(); j++) {
                DmsVersion tmpVersion = (DmsVersion) documentVersionToArchiveList.get(j);
                docVersionIDList[u - 1][j] = tmpVersion.getID().toString();
                DmsContent tmpContent = docRetrievalManager.getContentByContentID(tmpVersion.getContentID());
                String converted = tmpContent.getConvertedName();
                String foldersDir = docRetrievalManager.getFoldersDirectories(document).toString();
                if (!DmsDocument.PAPER_DOC_TYPE.equals(document.getDocumentType())) {
                    this.copyPhysicalFiles(foldersDir + converted, folderPath + "/" + converted, dmsArchive.getCreatorID());
                }
                if (DmsArchive.ARCHIVE_TYPE.equals(dmsArchive.getArchiveType())) {
                    physicalPathList.add(foldersDir + converted);
                }
                docOperationManager.archiveDocumentVersion(document, tmpVersion.getID().toString(), dmsArchive.getID(), dmsArchive.getArchiveType());
            }
            if (divList.size() > 0 && divList.contains(new Integer(i))) {
                k++;
                String dataFilePath = folderPath + "/data_file.xml";
                String docTypeFilePath = folderPath + "/doc_type.txt";
                GenArchiveIndexXML(documentIDs, docVersionIDList, dataFilePath, dmsArchive);
                this.copyOfflineView(offlineViewPath + "/Dll", folderPath.substring(0, folderPath.indexOf("/image")) + "/Dll", dmsArchive.getCreatorID());
                this.copyOfflineView(offlineViewPath + "/ArchiveViewer.exe", folderPath.substring(0, folderPath.indexOf("/image")) + "/ArchiveViewer.exe", dmsArchive.getCreatorID());
                if (genExcel) {
                    this.GenArchiveExcel(documentIDs, docVersionIDList, folderPath, dmsArchive, this.getDefaultProfileSettingList());
                }
                if (divList.size() > k + 1 || divList.size() == k + 1) {
                    documentIDs = new String[TextUtility.parseInteger(divList.get(k).toString()) - i];
                    docVersionIDList = new String[TextUtility.parseInteger(divList.get(k).toString()) - i][];
                }
                u = 0;
                if (pathList.size() > k + 1 || pathList.size() == k + 1) {
                    folderPath = pathList.get(k).toString();
                }
            }
        }
        if (archiveList.size() > 0 && (divList.size() < 0 || divList.size() == 0)) {
            String dataFilePath = folderPath + "/data_file.xml";
            String docTypeFilePath = folderPath + "/doc_type.txt";
            GenArchiveIndexXML(documentIDs, docVersionIDList, dataFilePath, dmsArchive);
            this.copyOfflineView(offlineViewPath + "/Dll", folderPath.substring(0, folderPath.indexOf("/image")) + "/Dll", dmsArchive.getCreatorID());
            this.copyOfflineView(offlineViewPath + "/ArchiveViewer.exe", folderPath.substring(0, folderPath.indexOf("/image")) + "/ArchiveViewer.exe", dmsArchive.getCreatorID());
            if (genExcel) {
                this.GenArchiveExcel(documentIDs, docVersionIDList, folderPath, dmsArchive, this.getDefaultProfileSettingList());
            }
        }
        try {
            conn.commit();
        } catch (Exception e) {
            this.handleException(dmsArchive.getCreatorID(), e);
        }
        this.addMessageToEvelog(archiveList, dmsArchive.getCreatorID());
        for (int i = 0; i < physicalPathList.size(); i++) {
            String path = (String) physicalPathList.get(i);
            if (!Utility.isEmpty(path)) {
                File physicalfile = new File(path);
                physicalfile.delete();
            }
        }
        String[] messageStr = new String[dmsArchiveList.size()];
        messageStr[0] = new Integer(archiveList.size()).toString();
        for (int i = 0; i < dmsArchiveList.size(); i++) {
            if (!archiveList.contains(dmsArchiveList.get(i))) {
                DmsDocument doc = (DmsDocument) dmsArchiveList.get(i);
                String path = docRetrievalManager.getLocationPath(doc.getParentID()) + doc.getDocumentName();
                messageStr[i + 1] = path;
            }
        }
        return messageStr;
    }

    private List getDefaultProfileSettingList() throws ApplicationException {
        List result = new ArrayList();
        DmsDefaultProfileSettingDAObject profileDAO = new DmsDefaultProfileSettingDAObject(sessionContainer, conn);
        result = profileDAO.getFullList();
        return result;
    }

    public String[] startArchive(List dmsArchiveList, DmsArchive dmsArchive, ArrayList clipboardList, List defaultProfileSettingList, String offlineViewPath, String[] userDefinedFieldID, String archiveNow) throws ApplicationException {
        DmsArchiveDAObject archiveDAO = new DmsArchiveDAObject(sessionContainer, conn);
        DocumentRetrievalManager docRetrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
        DocumentOperationManager docOperationManager = new DocumentOperationManager(sessionContainer, conn);
        boolean genExcel = SystemParameterFactory.getSystemParameterBoolean(SystemParameterConstant.DMS_ENABLE_ARCHIVE_EXCEL);
        dmsArchiveList = this.getFilterDmsArchiveList(docRetrievalManager, dmsArchiveList);
        if (dmsArchiveList.size() <= 0) {
            if ("Y".equals(archiveNow)) {
                throw new ApplicationException(DmsErrorConstant.NO_DOCUMENT_TO_ARCHIVE);
            } else {
                EventLogger.log(dmsArchive.getCreatorID(), null, DmsErrorConstant.NO_DOCUMENT_TO_ARCHIVE, errorCode, SystemFunctionConstant.DMS_ARCHIVE, null);
            }
        }
        List archiveList = dmsArchiveList;
        List divList = getDivList(archiveList);
        if (divList.size() > 0 && TextUtility.parseInteger(divList.get(divList.size() - 1).toString()) < archiveList.size() - 1) {
            divList.add(divList.size(), new Integer(archiveList.size() - 1));
        }
        List pathList = this.getFolderPath(archiveList, divList, dmsArchive, archiveNow);
        String folderPath = "";
        if (pathList.size() > 0) {
            folderPath = pathList.get(0).toString();
        }
        if (archiveList.size() > 0) {
            dmsArchive = (DmsArchive) archiveDAO.insertObject(dmsArchive);
        }
        int k = 0;
        int u = 0;
        String[] documentIDs = null;
        String[][] docVersionIDList = null;
        if (divList.size() > 0) {
            documentIDs = new String[TextUtility.parseInteger(divList.get(0).toString()) + 1];
            docVersionIDList = new String[TextUtility.parseInteger(divList.get(0).toString()) + 1][];
        } else {
            documentIDs = new String[archiveList.size()];
            docVersionIDList = new String[archiveList.size()][];
        }
        this.createArchiveDetail(archiveList, dmsArchive, divList);
        List physicalPathList = new ArrayList();
        for (int i = 0; i < archiveList.size(); i++) {
            u++;
            DmsDocument document = (DmsDocument) archiveList.get(i);
            documentIDs[u - 1] = document.getID().toString();
            String name = TextUtility.getURLEncodeInUTF8(document.getDocumentName());
            List documentVersionToArchiveList = new ArrayList();
            if (DmsArchive.ARCHIVE_TYPE.equals(dmsArchive.getArchiveType())) {
                documentVersionToArchiveList = docRetrievalManager.getVersionListByDocumentID(document.getID());
            } else {
                DmsVersion topVersion = docRetrievalManager.getTopVersionByDocumentID(document.getID());
                documentVersionToArchiveList.add(topVersion);
            }
            docVersionIDList[u - 1] = new String[documentVersionToArchiveList.size()];
            for (int j = 0; j < documentVersionToArchiveList.size(); j++) {
                DmsVersion tmpVersion = (DmsVersion) documentVersionToArchiveList.get(j);
                docVersionIDList[u - 1][j] = tmpVersion.getID().toString();
                DmsContent tmpContent = docRetrievalManager.getContentByContentID(tmpVersion.getContentID());
                String converted = tmpContent.getConvertedName();
                String foldersDir = docRetrievalManager.getFoldersDirectories(document).toString();
                if (tmpContent.getSegmentNO() != null) {
                    foldersDir += DmsOperationConstant.documentPath + "/" + DmsOperationConstant.segment + tmpContent.getSegmentNO() + "/";
                }
                if (!DmsDocument.PAPER_DOC_TYPE.equals(document.getDocumentType())) {
                    this.copyPhysicalFiles(foldersDir + converted, folderPath + "/" + converted, dmsArchive.getCreatorID());
                }
                if (DmsArchive.ARCHIVE_TYPE.equals(dmsArchive.getArchiveType())) {
                    physicalPathList.add(foldersDir + converted);
                    if (!Utility.isEmpty(clipboardList)) {
                        if (clipboardList.size() > 0) {
                            if (clipboardList.contains(document.getID())) {
                                clipboardList.remove(document.getID());
                            }
                        }
                    }
                }
                docOperationManager.archiveDocumentVersion(document, tmpVersion.getID().toString(), dmsArchive.getID(), dmsArchive.getArchiveType());
            }
            if (divList.size() > 0 && divList.contains(new Integer(i))) {
                k++;
                String dataFilePath = folderPath + "/data_file.xml";
                String docTypeFilePath = folderPath + "/doc_type.txt";
                GenArchiveIndexXML(documentIDs, docVersionIDList, dataFilePath, dmsArchive);
                this.copyOfflineView(offlineViewPath + "/Dll", folderPath.substring(0, folderPath.indexOf("/image")) + "/Dll", dmsArchive.getCreatorID());
                this.copyOfflineView(offlineViewPath + "/ArchiveViewer.exe", folderPath.substring(0, folderPath.indexOf("/image")) + "/ArchiveViewer.exe", dmsArchive.getCreatorID());
                if (genExcel) {
                    this.GenArchiveExcel(documentIDs, docVersionIDList, folderPath, dmsArchive, defaultProfileSettingList);
                }
                if (divList.size() > k + 1 || divList.size() == k + 1) {
                    documentIDs = new String[TextUtility.parseInteger(divList.get(k).toString()) - i];
                    docVersionIDList = new String[TextUtility.parseInteger(divList.get(k).toString()) - i][];
                }
                u = 0;
                if (pathList.size() > k + 1 || pathList.size() == k + 1) {
                    folderPath = pathList.get(k).toString();
                }
            }
        }
        if (archiveList.size() > 0 && (divList.size() < 0 || divList.size() == 0)) {
            String dataFilePath = folderPath + "/data_file.xml";
            String docTypeFilePath = folderPath + "/doc_type.txt";
            GenArchiveIndexXML(documentIDs, docVersionIDList, dataFilePath, dmsArchive);
            this.copyOfflineView(offlineViewPath + "/Dll", folderPath.substring(0, folderPath.indexOf("/image")) + "/Dll", dmsArchive.getCreatorID());
            this.copyOfflineView(offlineViewPath + "/ArchiveViewer.exe", folderPath.substring(0, folderPath.indexOf("/image")) + "/ArchiveViewer.exe", dmsArchive.getCreatorID());
            if (genExcel) {
                this.GenArchiveExcel(documentIDs, docVersionIDList, folderPath, dmsArchive, defaultProfileSettingList);
            }
        }
        if (!"Y".equals(archiveNow)) {
            try {
                conn.commit();
            } catch (Exception e) {
                this.handleException(dmsArchive.getCreatorID(), e);
            }
        }
        this.addMessageToEvelog(archiveList, dmsArchive.getCreatorID());
        for (int i = 0; i < physicalPathList.size(); i++) {
            String path = (String) physicalPathList.get(i);
            if (!Utility.isEmpty(path)) {
                File physicalfile = new File(path);
                physicalfile.delete();
            }
        }
        String[] messageStr = new String[1];
        messageStr[0] = new Integer(archiveList.size()).toString();
        return messageStr;
    }

    /**
   * get the last archvie list for archving. 
   * notes: when and only when the dmsArchiveList is sorted, can use this method to filter the list;
   * @param docRetrievalManager
   * @param dmsArchiveList
   * @param request
   * @return
   * @throws ApplicationException
   */
    protected List getFilterDmsArchiveList(DocumentRetrievalManager docRetrievalManager, List sortedDmsArchiveList) throws ApplicationException {
        List messageList = new ArrayList();
        List paperDocIDList = new ArrayList();
        List compoDocIDList = new ArrayList();
        List sizeOverIDList = new ArrayList();
        List paperDocIDListNoChidren = new ArrayList();
        int maxsize = this.getStandCount();
        for (int i = 0; i < sortedDmsArchiveList.size(); i++) {
            DmsDocument doc = (DmsDocument) sortedDmsArchiveList.get(i);
            Integer id = doc.getID();
            Integer pid = doc.getParentID();
            String name = doc.getDocumentName();
            String type = doc.getDocumentType();
            int size = doc.getItemSize().intValue();
            if (type.equals(DmsDocument.PAPER_DOC_TYPE)) {
                paperDocIDList.add(id);
                paperDocIDListNoChidren.add(id);
            }
            if (paperDocIDListNoChidren.contains(pid)) {
                paperDocIDListNoChidren.remove(pid);
            }
            if (type.equals(DmsDocument.COMPOUND_DOC_TYPE)) {
                compoDocIDList.add(id);
            }
            if (maxsize > 0 && size > maxsize) {
                String location = docRetrievalManager.getLocationPath(pid) + "/" + name;
                messageList.add(location);
                sizeOverIDList.add(id);
                if (paperDocIDList.contains(pid) || compoDocIDList.contains(pid)) {
                    sizeOverIDList.add(pid);
                }
            }
        }
        for (int i = (sortedDmsArchiveList.size() - 1); i >= 0; i--) {
            DmsDocument doc = (DmsDocument) sortedDmsArchiveList.get(i);
            Integer id = doc.getID();
            Integer pid = doc.getParentID();
            if (sizeOverIDList.contains(id) || paperDocIDListNoChidren.contains(id) || sizeOverIDList.contains(pid)) {
                sortedDmsArchiveList.remove(i);
            }
        }
        return sortedDmsArchiveList;
    }

    /**
   * Validate archive action before really do it.
   * @param form
   * @param request
   * @return
   * @throws ApplicationException
   */
    public DmsValidation validateArchiveAction(AbstractActionForm form, HttpServletRequest request) throws ApplicationException {
        MaintDmsArchiveForm archiveForm = (MaintDmsArchiveForm) form;
        DmsArchive dmsArchive = (DmsArchive) archiveForm.getFormData();
        String[] basicSelectID = (String[]) request.getSession().getAttribute("archiveFolder");
        String dateFrom = archiveForm.getArchiveDateFrom();
        String dateTo = archiveForm.getArchiveDateTo();
        String[] indexIds = archiveForm.getUserDefinedFieldID();
        return this.validateArchiveAction(dmsArchive, basicSelectID, dateFrom, dateTo, indexIds, false);
    }

    /**
   * Validate archive action before really do it.
   * lee.lv add
   * @param form
   * @return
   * @throws ApplicationException
   */
    public DmsValidation validateArchiveAction(DmsArchive dmsArchive, String[] basicSelectID, String dateFrom, String dateTo, String[] indexIds, boolean isForceToPassName) throws ApplicationException {
        DmsDocumentDAObject dmsDocumentDAO = new DmsDocumentDAObject(this.sessionContainer, conn);
        DmsVersionDAObject dmsVersionDAO = new DmsVersionDAObject(sessionContainer, conn);
        DmsArchiveDAObject archiveDAO = new DmsArchiveDAObject(sessionContainer, conn);
        DocumentValidateManager validateManager = new DocumentValidateManager(sessionContainer, conn);
        DmsValidation validation = new DmsValidation();
        try {
            TextUtility.stringValidation(dmsArchive.getArchiveName());
        } catch (Exception ex) {
            validation.setInvalidName(true);
            validation.setSuccess(false);
            return validation;
        }
        int counter = 1;
        if (isForceToPassName) {
            String old_archiveName = dmsArchive.getArchiveName();
            while (true) {
                List archiveNameList = archiveDAO.getListByArchiveName(dmsArchive.getArchiveName());
                if (archiveNameList != null && !archiveNameList.isEmpty()) {
                    dmsArchive.setArchiveName(old_archiveName + "(" + (counter++) + ")");
                } else {
                    break;
                }
            }
        } else {
            List archiveNameList = archiveDAO.getListByArchiveName(dmsArchive.getArchiveName());
            if (archiveNameList != null && !archiveNameList.isEmpty()) {
                validation.setSuccess(false);
                validation.setDuplicateName(true);
                return validation;
            }
        }
        String archiveRootPath = SystemParameterFactory.getSystemParameter(SystemParameterConstant.DMS_ARCHIVE_FILE_PATH);
        String tmpArchiveName = dmsArchive.getArchiveName();
        while (true) {
            File folder = new File(archiveRootPath + "/" + dmsArchive.getArchiveName() + "/");
            if (folder.exists()) {
                dmsArchive.setArchiveName(tmpArchiveName + "(" + (counter++) + ")");
            } else {
                break;
            }
        }
        List lstIdAccept = new ArrayList();
        List lstTmpIdAccept = new ArrayList();
        List lstSelectID = new ArrayList();
        for (int i = 0; i < basicSelectID.length; i++) {
            lstSelectID.add(TextUtility.parseIntegerObj(basicSelectID[i]));
        }
        String[] recordStatus = new String[] { GlobalConstant.RECORD_STATUS_ACTIVE };
        List lstTmpArchiveID = validateManager.getFullTreeNodeID(dmsDocumentDAO, lstSelectID, recordStatus);
        List lstAllArchiveID = new ArrayList();
        if (lstTmpArchiveID != null && !lstTmpArchiveID.isEmpty()) {
            String[] fieldNames = new String[] { "A.ID", "A.DOCUMENT_TYPE", "A.ITEM_STATUS" };
            String[] fieldTypes = new String[] { "Integer", "String", "String" };
            int batchSize = 100;
            int batchIndex = 0;
            int size = lstTmpArchiveID.size();
            while (true) {
                int index_start = (batchIndex++) * batchSize;
                if (index_start >= size) {
                    break;
                }
                int index_end = (index_start + batchSize) > size ? size : (index_start + batchSize);
                List batchDocumentIds = (index_end < batchSize) ? lstTmpArchiveID : lstTmpArchiveID.subList(index_start, index_end);
                Map tmpFilter = dmsDocumentDAO.getMapIdToFieldValueArray(batchDocumentIds, fieldNames, fieldTypes, recordStatus, dateFrom, dateTo, indexIds);
                for (int i = 0; i < batchDocumentIds.size(); i++) {
                    if (tmpFilter.containsKey(batchDocumentIds.get(i))) {
                        Object[] values = (Object[]) tmpFilter.get(batchDocumentIds.get(i));
                        Integer id = (Integer) values[0];
                        String documentType = (String) values[1];
                        String itemStatus = (String) values[2];
                        if ((!DmsVersion.ARCHIVED_STATUS.equals(itemStatus)) && (isArchiveDocumentType(documentType))) {
                            lstAllArchiveID.add(id);
                        }
                    }
                }
            }
        }
        long totalItemSize = 0;
        long countItemSize = 0;
        long dbSetItemSize = this.getStandCount();
        if (!lstAllArchiveID.isEmpty()) {
            int batchSize = 100;
            int batchIndex = 0;
            int size = lstAllArchiveID.size();
            while (true) {
                int index_start = (batchIndex++) * batchSize;
                if (index_start >= size) {
                    break;
                }
                int index_end = (index_start + batchSize) > size ? size : (index_start + batchSize);
                List batchDocumentIds = lstAllArchiveID.subList(index_start, index_end);
                long itemSize = dmsVersionDAO.getItemSizeTotal(batchDocumentIds).longValue();
                totalItemSize += itemSize;
                if ((countItemSize + itemSize) > dbSetItemSize) {
                    Map itemSizes = dmsVersionDAO.getItemSizeTotalMap(batchDocumentIds);
                    for (int i = 0; i < batchDocumentIds.size(); i++) {
                        Integer docId = (Integer) batchDocumentIds.get(i);
                        itemSize = Long.parseLong((String) (itemSizes.get(docId) != null ? itemSizes.get(docId) : "0"));
                        if ((countItemSize + itemSize) > dbSetItemSize) {
                            lstIdAccept.add(lstTmpIdAccept);
                            lstTmpIdAccept = new ArrayList();
                            lstTmpIdAccept.add(docId);
                            countItemSize = itemSize;
                        } else {
                            lstTmpIdAccept.add(docId);
                            countItemSize += itemSize;
                        }
                    }
                } else {
                    lstTmpIdAccept.addAll(batchDocumentIds);
                    countItemSize += itemSize;
                }
            }
        }
        long freeDiskSpace = validateManager.getFreeDiskSpace(SystemParameterFactory.getSystemParameter(SystemParameterConstant.DMS_ARCHIVE_FILE_PATH));
        if (freeDiskSpace != -1 && freeDiskSpace < totalItemSize) {
            validation.setSuccess(false);
            validation.setNotEnoughDiskSpace(true);
            validation.setDiskSpaceFree(freeDiskSpace);
            validation.setDiskSpaceRequire(totalItemSize);
            return validation;
        }
        MtmWorkflowProgressSystemObjectDAObject mtmWorkflowProgressSystemObjectDAO = new MtmWorkflowProgressSystemObjectDAObject(sessionContainer, conn);
        WorkflowProgressDAObject workflowProgressDAO = new WorkflowProgressDAObject(sessionContainer, conn);
        boolean isProgressComplete = true;
        for (int index = 0; index < basicSelectID.length; index++) {
            List documentList = dmsDocumentDAO.getListByParentID(new Integer(basicSelectID[index]), null);
            ListIterator iterator = documentList.listIterator();
            while (iterator.hasNext()) {
                DmsDocument dmsDocument = (DmsDocument) iterator.next();
                List systemObjectByWorkflowProgressList = mtmWorkflowProgressSystemObjectDAO.getListByBeanClassNameObjectID(DmsDocument.class.getName(), dmsDocument.getID());
                if (!Utility.isEmpty(systemObjectByWorkflowProgressList)) {
                    MtmWorkflowProgressSystemObject systemObjectByWorkflowProgress = (MtmWorkflowProgressSystemObject) systemObjectByWorkflowProgressList.get(0);
                    isProgressComplete = workflowProgressDAO.checkProgressComplete(systemObjectByWorkflowProgress.getTrackID());
                    if (!isProgressComplete) {
                        lstTmpIdAccept.remove(dmsDocument.getID());
                    }
                }
            }
        }
        if (!lstTmpIdAccept.isEmpty()) {
            lstIdAccept.add(lstTmpIdAccept);
        }
        validation.setLstIdAccept(lstIdAccept);
        validation.setSuccess(lstIdAccept.isEmpty() ? false : true);
        return validation;
    }

    /**
   * Do archive action after validation.
   * @param form
   * @param request
   * @param validation 
   * @throws ApplicationException
   */
    public void archiveDocument(AbstractActionForm form, HttpServletRequest request, DmsValidation validation) throws ApplicationException {
        MaintDmsArchiveForm archiveForm = (MaintDmsArchiveForm) form;
        DmsArchive dmsArchive = (DmsArchive) archiveForm.getFormData();
        String[] basicSelectID = (String[]) request.getSession().getAttribute("archiveFolder");
        String[] indexIds = archiveForm.getUserDefinedFieldID();
        String offsetViewPath = request.getSession().getServletContext().getRealPath("viewer");
        List defaultProfileSettingList = (List) request.getSession().getServletContext().getAttribute("DMS_DEF_PROFILE");
        List clipboardList = (List) request.getSession().getAttribute("DMS_CLIPBOARD");
        this.archiveDocument(dmsArchive, validation, basicSelectID, indexIds, offsetViewPath, defaultProfileSettingList, clipboardList);
    }

    /**
   * Do archive action after validation.
   * @param form
   * @param request
   * @param validation 
   * @throws ApplicationException
   */
    public void archiveDocument(DmsArchive dmsArchive, DmsValidation validation, String[] basicSelectID, String[] indexIds, String offsetViewPath, List defaultProfileSettingList, List clipboardList) throws ApplicationException {
        DmsArchiveDAObject archiveDAO = new DmsArchiveDAObject(sessionContainer, conn);
        DmsArchiveDetailDAObject archiveDetailDAO = new DmsArchiveDetailDAObject(sessionContainer, conn);
        DmsDocumentDAObject dmsDocumentDAO = new DmsDocumentDAObject(sessionContainer, conn);
        DmsContentDAObject dmsContentDAO = new DmsContentDAObject(sessionContainer, conn);
        DmsRootDAObject dmsRootDAO = new DmsRootDAObject(sessionContainer, conn);
        DmsLocMasterDAObject locMasterDAO = new DmsLocMasterDAObject(sessionContainer, conn);
        AuditTrailManager auditManager = new AuditTrailManager(sessionContainer, conn);
        DocumentRetrievalManager docRetrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
        DocumentOperationManager docOperationManager = new DocumentOperationManager(sessionContainer, conn);
        boolean genExcel = SystemParameterFactory.getSystemParameterBoolean(SystemParameterConstant.DMS_ENABLE_ARCHIVE_EXCEL);
        boolean isArchiveAction = DmsArchive.ARCHIVE_TYPE.equals(dmsArchive.getArchiveType());
        List lstIdAccept = validation.getLstIdAccept();
        String[] fieldNames = new String[] { "D.ID", "D.ROOT_ID", "D.DOCUMENT_TYPE", "V.ID", "C.ID", "C.CONVERTED_NAME", "C.SEGMENT_NO" };
        String[] fieldTypes = new String[] { "Integer", "Integer", "String", "Integer", "Integer", "String", "Integer" };
        String orderBy = "D.ID ASC, V.ID DESC";
        List lstFilePath = new ArrayList();
        try {
            DmsArchive newDmsArchive = (DmsArchive) archiveDAO.insertObject(dmsArchive);
            Map mapLocMaster = new HashMap();
            for (int i = 0; i < lstIdAccept.size(); i++) {
                List diskDocumentIds = (List) lstIdAccept.get(i);
                String[] documentIDs = new String[diskDocumentIds.size()];
                String[][] versionIDs = new String[diskDocumentIds.size()][];
                String destinPath = this.getFolderPath(newDmsArchive.getArchiveName(), (i + 1));
                int batchSize = 100;
                int batchIndex = 0;
                int maxsize = diskDocumentIds.size();
                while (true) {
                    int index_start = (batchIndex++) * batchSize;
                    if (index_start >= maxsize) {
                        break;
                    }
                    int index_end = (index_start + batchSize) > maxsize ? maxsize : (index_start + batchSize);
                    List batchDocumentIds = diskDocumentIds.subList(index_start, index_end);
                    if (isArchiveAction) {
                        dmsDocumentDAO.updateItemStatusByIDList(batchDocumentIds, DmsVersion.ARCHIVED_STATUS);
                    }
                    if (isArchiveAction) {
                        AdapterMaster am = new AdapterMaster(sessionContainer, conn);
                        DmsDocument tempDoc = null;
                        for (int s = 0; s < batchDocumentIds.size(); s++) {
                            tempDoc = (DmsDocument) dmsDocumentDAO.getObjectByID((Integer) batchDocumentIds.get(s));
                            if (tempDoc != null) {
                                am.call(UpdateAlert.DOCUMENT_TYPE, tempDoc.getID(), UpdateAlert.UPDATE_ACTION, tempDoc.getDocumentName(), null, null, null, null);
                            }
                        }
                        am.release();
                    }
                    if (!Utility.isEmpty(batchDocumentIds)) {
                        for (int x = 0; x < batchDocumentIds.size(); x++) {
                            List relationshipList = docRetrievalManager.getRelationshipList((Integer) (batchDocumentIds.get(x)));
                            if (!relationshipList.isEmpty()) {
                                for (int y = 0; y < relationshipList.size(); y++) {
                                    MtmDocumentRelationship tmpRelationship = (MtmDocumentRelationship) relationshipList.get(y);
                                    if (DmsDocument.DOCUMENT_LINK.equals(tmpRelationship.getRelationshipType())) {
                                        DmsDocument delDocument = (DmsDocument) dmsDocumentDAO.getDocumentByID(tmpRelationship.getRelatedDocumentID(), null);
                                        docOperationManager.hardDeleteDocument(delDocument);
                                    }
                                }
                            }
                        }
                    }
                    Map dvcObjects = dmsDocumentDAO.getMapIdToDVCFieldValueArrayList(batchDocumentIds, fieldNames, fieldTypes, orderBy);
                    for (int m = 0; m < batchDocumentIds.size(); m++) {
                        Integer id = (Integer) batchDocumentIds.get(m);
                        List lstDvcValue = (List) dvcObjects.get(id);
                        if (lstDvcValue != null) {
                            documentIDs[index_start + m] = String.valueOf(id);
                            versionIDs[index_start + m] = isArchiveAction ? new String[lstDvcValue.size()] : new String[1];
                            for (int n = 0; n < lstDvcValue.size(); n++) {
                                if (isArchiveAction || n == 0) {
                                    Object[] arrDvcValue = (Object[]) lstDvcValue.get(n);
                                    Integer rootID = (Integer) arrDvcValue[1];
                                    String documentType = (String) arrDvcValue[2];
                                    Integer versionID = (Integer) arrDvcValue[3];
                                    String convertName = (String) arrDvcValue[5];
                                    Integer segmentNo = (Integer) arrDvcValue[6];
                                    versionIDs[index_start + m][n] = String.valueOf(versionID);
                                    DmsArchiveDetail archiveDetail = new DmsArchiveDetail();
                                    archiveDetail.setArchiveID(newDmsArchive.getID());
                                    archiveDetail.setDocumentID(id);
                                    archiveDetail.setVersionID(versionID);
                                    archiveDetail.setArchiveSegmentID(new Integer(i + 1));
                                    archiveDetail.setCreatorID(newDmsArchive.getCreatorID());
                                    archiveDetail.setUpdaterID(newDmsArchive.getCreatorID());
                                    archiveDetailDAO.insertObject(archiveDetail);
                                    if (!Utility.isEmpty(convertName) && !DmsDocument.PAPER_DOC_TYPE.equals(documentType)) {
                                        DmsLocMaster locMaster = (DmsLocMaster) mapLocMaster.get(rootID);
                                        if (locMaster == null) {
                                            locMaster = (DmsLocMaster) locMasterDAO.getObjectByID(((DmsRoot) dmsRootDAO.getObjectByID(rootID)).getLocID());
                                            mapLocMaster.put(rootID, locMaster);
                                        }
                                        String sourceDir = locMaster.getLocPath() + (Utility.isEmpty(segmentNo) ? "" : "/" + "Document" + "/" + "segment" + segmentNo);
                                        this.copyPhysicalFiles(sourceDir + "/" + convertName, destinPath + "/" + convertName);
                                        if (isArchiveAction && !dmsContentDAO.checkHasListByConvertedNameUnderSameLoc(convertName, locMaster.getID())) {
                                            lstFilePath.add(sourceDir + "/" + convertName);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    String accessType = isArchiveAction ? AuditTrailConstant.ACCESS_TYPE_ARCHIVE : AuditTrailConstant.ACCESS_TYPE_BACKUP;
                    for (int k = 0; k < batchDocumentIds.size(); k++) {
                        DmsDocument dmsDocument = (DmsDocument) dmsDocumentDAO.getObjectByID((Integer) batchDocumentIds.get(k));
                        auditManager.auditTrail(GlobalConstant.OBJECT_TYPE_DOCUMENT, dmsDocument, accessType);
                    }
                }
                String dataFilePath = destinPath + "/data_file.xml";
                GenArchiveIndexXML(documentIDs, versionIDs, dataFilePath, newDmsArchive);
                copyOfflineView(offsetViewPath + "/Dll", destinPath.substring(0, destinPath.indexOf("/image")) + "/Dll", dmsArchive.getCreatorID());
                copyOfflineView(offsetViewPath + "/ArchiveViewer.exe", destinPath.substring(0, destinPath.indexOf("/image")) + "/ArchiveViewer.exe", dmsArchive.getCreatorID());
                if (genExcel) {
                    GenArchiveExcel(documentIDs, versionIDs, destinPath, newDmsArchive, defaultProfileSettingList);
                }
            }
            conn.commit();
            if (isArchiveAction) {
                File file = null;
                for (int i = 0; i < lstFilePath.size(); i++) {
                    try {
                        file = new File((String) lstFilePath.get(i));
                        if (file.exists()) {
                            file.delete();
                        }
                    } catch (Exception e) {
                        log.error("error info", e);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("error info", ex);
            try {
                conn.rollback();
            } catch (Exception e) {
            }
        }
        if (isArchiveAction && !Utility.isEmpty(clipboardList) && !clipboardList.isEmpty()) {
            for (int m = 0; m < lstIdAccept.size(); m++) {
                List documentIds = (List) lstIdAccept.get(m);
                for (int n = (clipboardList.size() - 1); n >= 0; n--) {
                    if (documentIds.contains(clipboardList.get(n))) {
                        clipboardList.remove(n);
                    }
                }
            }
        }
    }

    /**
   * Get destination path of file copy.
   * @param archiveName
   * @param diskIndex
   * @return
   * @throws ApplicationException
   */
    public String getFolderPath(String archiveName, int diskIndex) throws ApplicationException {
        String archiveRootPath = SystemParameterFactory.getSystemParameter(SystemParameterConstant.DMS_ARCHIVE_FILE_PATH);
        String segment = "";
        if (diskIndex < 10) {
            segment = "00" + diskIndex;
        } else if (diskIndex >= 10 && diskIndex < 100) {
            segment = "0" + diskIndex;
        } else {
            segment = (new Integer(diskIndex)).toString();
        }
        String folderPath = archiveRootPath + "/" + archiveName + "/" + archiveName + "_" + segment + "/image";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        } else {
            throw new ApplicationException(DmsErrorConstant.DUPLICATE_ARCHIVE_NAME);
        }
        return folderPath;
    }

    /**
   * Copy file to the target path.
   * @param sourcePath
   * @param targetPath
   * @throws ApplicationException
   */
    private void copyPhysicalFiles(String sourcePath, String targetPath) throws ApplicationException {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            File inputFile = new File(sourcePath);
            if (inputFile.exists()) {
                fis = new FileInputStream(inputFile);
                fos = new FileOutputStream(targetPath);
                int bufferSize = 8192;
                byte[] buffer = new byte[bufferSize];
                int length = -1;
                while ((length = fis.read(buffer, 0, bufferSize)) != -1) {
                    fos.write(buffer, 0, length);
                }
                fos.flush();
            }
        } catch (Exception e) {
            log.error(e, e);
        } finally {
            try {
                fos.close();
                fis.close();
            } catch (Exception ignore) {
            }
        }
    }

    /**
   * Checking whether the document can be archive or not.
   * @param documentType
   * @return
   */
    private boolean isArchiveDocumentType(String documentType) {
        return DmsDocument.DOCUMENT_TYPE.equals(documentType) || DmsDocument.PAPER_DOC_TYPE.equals(documentType) || DmsDocument.COMPOUND_DOC_TYPE.equals(documentType) || DmsDocument.EMAIL_DOC_TYPE.equals(documentType) || DmsDocument.ORIGINSLITY_EMAIL_TYPE.equals(documentType);
    }
}
