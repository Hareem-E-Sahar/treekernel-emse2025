package com.dcivision.dms.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import com.dcivision.alert.bean.MtmUpdateAlertRecipient;
import com.dcivision.alert.bean.UpdateAlert;
import com.dcivision.alert.bean.UpdateAlertSystemLog;
import com.dcivision.alert.bean.UpdateAlertType;
import com.dcivision.alert.core.AdapterMaster;
import com.dcivision.alert.dao.MtmUpdateAlertRecipientDAObject;
import com.dcivision.alert.dao.UpdateAlertDAObject;
import com.dcivision.alert.dao.UpdateAlertSystemLogDAObject;
import com.dcivision.alert.dao.UpdateAlertTypeDAObject;
import com.dcivision.audit.AuditTrailConstant;
import com.dcivision.audit.bean.AuditTrail;
import com.dcivision.audit.dao.AuditTrailDAObject;
import com.dcivision.dms.bean.DmsDocument;
import com.dcivision.dms.bean.DmsLocMaster;
import com.dcivision.dms.bean.DmsRoot;
import com.dcivision.dms.bean.DmsVersion;
import com.dcivision.dms.core.DocumentRetrievalManager;
import com.dcivision.dms.dao.DmsDocumentDAObject;
import com.dcivision.dms.dao.DmsLocMasterDAObject;
import com.dcivision.dms.dao.DmsRootDAObject;
import com.dcivision.dms.dao.MtmDocumentRelationshipDAObject;
import com.dcivision.framework.AdapterMasterFactory;
import com.dcivision.framework.ApplicationException;
import com.dcivision.framework.DataSourceFactory;
import com.dcivision.framework.GlobalConstant;
import com.dcivision.framework.PermissionManager;
import com.dcivision.framework.SessionContainer;
import com.dcivision.framework.SystemParameterConstant;
import com.dcivision.framework.SystemParameterFactory;
import com.dcivision.framework.TextUtility;
import com.dcivision.framework.Utility;

/**
 * ZIP download dms folder.
 * @author Administrator
 *
 */
public class FileZipDownloadServlet extends HttpServlet {

    private SessionContainer sessionContainer = null;

    private Connection conn = null;

    private double itemSizeTotal = 0;

    private List lstDocumentId = null;

    private List lstDocumentPid = null;

    private List lstDocumentName = null;

    private List lstVersionNumber = null;

    private List lstZipFileName = null;

    private List lstZipFilePath = null;

    private static final Log log = LogFactory.getLog(FileZipDownloadServlet.class);

    private final String[] fieldNames = new String[] { "D.ID", "D.PARENT_ID", "D.DOCUMENT_NAME", "D.DOCUMENT_TYPE", "C.CONVERTED_NAME", "C.SEGMENT_NO", "C.EXT", "V.ITEM_SIZE", "V.VERSION_NUMBER" };

    private final String[] fieldTypes = new String[] { "Integer", "Integer", "String", "String", "String", "Integer", "String", "String", "String" };

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            this.sessionContainer = (SessionContainer) request.getSession().getAttribute(GlobalConstant.SESSION_CONTAINER_KEY);
            this.conn = DataSourceFactory.getConnection();
            this.itemSizeTotal = 0;
            this.lstDocumentId = new ArrayList();
            this.lstDocumentPid = new ArrayList();
            this.lstDocumentName = new ArrayList();
            this.lstVersionNumber = new ArrayList();
            this.lstZipFileName = new ArrayList();
            this.lstZipFilePath = new ArrayList();
            String returnURL = request.getParameter("returnURL");
            String zipFileName = request.getParameter("zipFileName");
            zipFileName = new String(zipFileName.getBytes("iso-8859-1"), "utf-8");
            String cleanClipboard = request.getParameter("cleanClipboard");
            String[] arSelectID = request.getParameterValues("selectID");
            Set selectID = new HashSet();
            List distinctID = new ArrayList();
            for (int i = 0; i < arSelectID.length; i++) {
                selectID.add(TextUtility.parseIntegerObj(arSelectID[i]));
            }
            if (!Utility.isEmpty(selectID)) {
                DocumentRetrievalManager docRetrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
                Iterator iterator = selectID.iterator();
                while (iterator.hasNext()) {
                    Integer docID = (Integer) iterator.next();
                    DmsDocument doc = docRetrievalManager.getDocumentByID(docID);
                    if (DmsDocument.DOCUMENT_LINK.equals(doc.getDocumentType())) {
                        MtmDocumentRelationshipDAObject docRelationshipDAO = new MtmDocumentRelationshipDAObject(sessionContainer, conn);
                        docID = docRelationshipDAO.getTargetDocIDByRelatedDocID(docID, doc.getDocumentType());
                        DmsDocument targetDoc = docRetrievalManager.getDocument(docID);
                        doc = targetDoc;
                    }
                    if (!zipFileName.equals("clipboardZip") && arSelectID.length == 1) {
                        zipFileName = doc.getDocumentName();
                    }
                    List parentIdList = docRetrievalManager.getParentDocumentList(docID);
                    parentIdList.add(docRetrievalManager.getRootFolderByRootID(doc.getRootID()).getID());
                    parentIdList.remove(docID);
                    boolean found = false;
                    if (!Utility.isEmpty(parentIdList)) {
                        for (int i = 0; i < parentIdList.size(); i++) {
                            Integer pid = (Integer) parentIdList.get(i);
                            if (selectID.contains(pid)) {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        distinctID.add(docID);
                    }
                }
            }
            for (int i = 0; i < distinctID.size(); i++) {
                this.appendZipDownload((Integer) distinctID.get(i), request);
            }
            Integer limitSize;
            try {
                limitSize = SystemParameterFactory.getSystemParameterInteger(SystemParameterConstant.DMS_ZIP_SIZE_LIMIT);
            } catch (NullPointerException npe) {
                limitSize = new Integer(1000000);
            }
            if (limitSize != null && this.itemSizeTotal > (1024 * 1024 * limitSize.intValue())) {
                response.addHeader("Content-Transfer-Encoding", "base64");
                response.setContentType("text/html;charset=utf-8");
                PrintWriter out = response.getWriter();
                out.println("<script language='javascript'>");
                out.println("alert('Zip limit restrict, zip size over than " + limitSize + " MB.');");
                if (returnURL != null) {
                    out.println("window.location.href='" + returnURL + "';");
                } else {
                    out.println("history.back(-1);");
                }
                out.println("</script>");
                out.close();
            } else {
                zipFileName = (zipFileName == null ? "download" : zipFileName);
                zipFileName = this.getFittedFileName(zipFileName);
                zipFileName = TextUtility.replaceString(TextUtility.getURLEncodeInUTF8(zipFileName), "+", "%20");
                response.addHeader("Content-Transfer-Encoding", "base64");
                response.addHeader("Content-Disposition", "attachment; filename=" + zipFileName + ".zip");
                response.setContentType("application/x-zip-compressed");
                this.downloadZipDocument(response);
                if ("true".equals(cleanClipboard)) {
                    List clipboardList = (List) request.getSession().getAttribute("DMS_CLIPBOARD");
                    for (int i = 0; i < arSelectID.length; i++) {
                        if (clipboardList.contains(TextUtility.parseIntegerObj(arSelectID[i]))) {
                            clipboardList.remove(TextUtility.parseIntegerObj(arSelectID[i]));
                        }
                    }
                    request.getSession().setAttribute("DMS_CLIPBOARD", clipboardList);
                }
                AdapterMaster am = AdapterMasterFactory.getAdapterMaster(sessionContainer, conn);
                AuditTrailDAObject auditDAO = new AuditTrailDAObject(sessionContainer, conn);
                for (int i = 0; i < this.lstDocumentId.size(); i++) {
                    if (!DmsRoot.PERSONAL_ROOT.equals(request.getAttribute("rootType"))) {
                        am.call(UpdateAlert.DOCUMENT_TYPE, (Integer) lstDocumentId.get(i), UpdateAlert.VIEW_ACTION, (String) lstDocumentName.get(i), null, null, null);
                        am.call(UpdateAlert.DOCUMENT_TYPE, (Integer) lstDocumentPid.get(i), UpdateAlert.VIEW_ACTION, (String) lstDocumentName.get(i), null, null, null, (Integer) lstDocumentId.get(i));
                    }
                    this.auditTrailDocument(auditDAO, (Integer) lstDocumentId.get(i), (String) lstVersionNumber.get(i), AuditTrailConstant.ACCESS_TYPE_VIEW);
                }
                conn.commit();
            }
        } catch (Exception ex) {
            try {
                conn.rollback();
            } catch (Exception exx) {
                log.error(exx);
            }
        } finally {
            try {
                this.conn.close();
            } catch (Exception ignore) {
            } finally {
                this.conn = null;
            }
        }
    }

    /**
   * Download filename length can not be more than 159 en char or 17 cn char, so cut the filename when it is long.
   * Add on 07/01/24 for bug EIP-1481. LEE.LV
   * @param zipFileName
   * @return
   * @throws ApplicationException
   */
    private String getFittedFileName(String fileName) {
        String tmpFileName = null;
        String tmpFileExt = null;
        int position = fileName.lastIndexOf(".");
        if (position > 0) {
            tmpFileName = fileName.substring(0, position);
            tmpFileExt = fileName.substring(position);
        } else {
            tmpFileName = fileName;
            tmpFileExt = "";
        }
        int counter = 0;
        int maxlength = 150;
        for (int i = 0; i < tmpFileName.length(); i++) {
            int chAsii = tmpFileName.charAt(i);
            if (chAsii <= 0 || chAsii >= 126) {
                counter += 9;
            } else {
                counter++;
            }
            if (counter >= maxlength) {
                tmpFileName = tmpFileName.substring(0, i);
                break;
            }
        }
        return tmpFileName + tmpFileExt;
    }

    /**
   * Load zip information for downloadZipDocument() method
   * @param docId                     Integer
   * @param request                   HttpServletRequest
   * @throws ApplicationException
   */
    private void appendZipDownload(Integer docId, HttpServletRequest request) throws ApplicationException {
        DmsDocumentDAObject dmsDocumentDAO = new DmsDocumentDAObject(sessionContainer, conn);
        DmsLocMasterDAObject locMasterDAO = new DmsLocMasterDAObject(sessionContainer, conn);
        DmsRootDAObject rootDAO = new DmsRootDAObject(sessionContainer, conn);
        PermissionManager permissionManager = sessionContainer.getPermissionManager();
        MtmDocumentRelationshipDAObject docRelationshipDAO = new MtmDocumentRelationshipDAObject(sessionContainer, conn);
        UpdateAlertDAObject updateAlertDAO = new UpdateAlertDAObject(sessionContainer, conn);
        UpdateAlertTypeDAObject updateAlertTypeDAO = new UpdateAlertTypeDAObject(sessionContainer, conn);
        UpdateAlertSystemLogDAObject updateAlertSystemLogDAObject = new UpdateAlertSystemLogDAObject(sessionContainer, conn);
        MtmUpdateAlertRecipientDAObject mtmUpdateAlertRecipientDAObject = new MtmUpdateAlertRecipientDAObject(sessionContainer, conn);
        Integer updateAlertID = null;
        Integer updateAlertSystemLogID = null;
        UpdateAlert updateAlert = ((UpdateAlert) updateAlertDAO.getByObjectTypeObjectID(DmsDocument.DOCUMENT_TYPE, docId));
        if (!Utility.isEmpty(updateAlert)) {
            updateAlertID = updateAlert.getID();
            try {
                UpdateAlertSystemLog updateAlertSystemLog = (UpdateAlertSystemLog) updateAlertSystemLogDAObject.getSystemLogByUpdateAlertIDActionType(updateAlertID, "I");
                if (!Utility.isEmpty(updateAlertSystemLog)) {
                    updateAlertSystemLogID = updateAlertSystemLog.getID();
                    if (updateAlertSystemLogID == null) {
                        updateAlertSystemLogID = TextUtility.parseIntegerObj(request.getParameter("systemLogID"));
                    }
                }
            } catch (Exception e) {
                log.error(e);
            }
        }
        DmsDocument dmsDocument = (DmsDocument) dmsDocumentDAO.getObjectByID(docId);
        if ("true".equals(request.getParameter("singleDocDownload")) && DmsDocument.DOCUMENT_LINK.equals(dmsDocument.getDocumentType())) {
            docId = docRelationshipDAO.getTargetDocIDByRelatedDocID(dmsDocument.getID(), DmsDocument.DOCUMENT_LINK);
            dmsDocument = (DmsDocument) dmsDocumentDAO.getObjectByID(docId);
        }
        DmsRoot dmsRoot = (DmsRoot) rootDAO.getObjectByID(dmsDocument.getRootID());
        String basePath = ((DmsLocMaster) locMasterDAO.getObjectByID(dmsRoot.getLocID())).getLocPath();
        String baseName = dmsRoot.getRootName();
        String rootType = dmsRoot.getRootType();
        request.setAttribute("rootType", rootType);
        Integer rootOwnerId = dmsRoot.getOwnerID();
        List listFieldObject = dmsDocumentDAO.getDVCFieldListByDocumentID(fieldNames, fieldTypes, docId, new String[] { DmsVersion.ARCHIVED_STATUS }, false, new String[] { GlobalConstant.RECORD_STATUS_ACTIVE }, true, "D.DOCUMENT_TYPE ASC, D.ID ASC, V.VERSION_NUMBER DESC");
        if (!listFieldObject.isEmpty()) {
            Object[] fields = (Object[]) listFieldObject.get(0);
            Integer documentId = (Integer) fields[0];
            Integer documentPid = (Integer) fields[1];
            String versionNumber = (String) fields[8];
            String documentName = (String) fields[2];
            if (documentName.lastIndexOf(".") == documentName.length() - 1) {
                while (documentName.length() > 0 && documentName.lastIndexOf(".") == documentName.length() - 1) {
                    documentName = documentName.substring(0, documentName.length() - 1);
                }
            }
            String documentType = (String) fields[3];
            String extendName = (String) fields[6];
            String convertedName = (String) fields[4];
            Integer segmentNo = (Integer) fields[5];
            double itemSize = (fields[7] != null ? Double.parseDouble((String) fields[7]) : 0);
            boolean hasInvitation = false;
            boolean hasPermission = permissionManager.hasAccessRight(conn, GlobalConstant.OBJECT_TYPE_DOCUMENT, documentId, rootType, rootOwnerId, "R");
            Integer tmpUpdateAlertID = updateAlertDAO.getInvitationIDByAlertIDorSystemLogID(updateAlertID, updateAlertSystemLogID);
            if (tmpUpdateAlertID != null) {
                List invitations = mtmUpdateAlertRecipientDAObject.getInvitationList(docId, tmpUpdateAlertID, sessionContainer.getUserRecordID(), permissionManager.getUserRoles(), permissionManager.getUserGroups());
                for (int i = 0; i < invitations.size(); i++) {
                    MtmUpdateAlertRecipient recipient = (MtmUpdateAlertRecipient) invitations.get(i);
                    UpdateAlertType updateAlertType = null;
                    try {
                        updateAlertType = (UpdateAlertType) updateAlertTypeDAO.getObjectByID(recipient.getUpdateAlertTypeID());
                    } catch (Exception ex) {
                    }
                    if (updateAlertType != null) {
                        Timestamp dueDate = updateAlertType.getDueDate();
                        if (dueDate != null && System.currentTimeMillis() - dueDate.getTime() > 86400000) {
                        } else {
                            hasInvitation = true;
                            break;
                        }
                    }
                }
            }
            if ("true".equals(request.getParameter("singleDocDownload")) && !DmsDocument.DOCUMENT_LINK.equals(documentType) && (hasPermission || hasInvitation)) {
                this.lstDocumentId.add(documentId);
                this.lstDocumentPid.add(documentPid);
                this.lstDocumentName.add(documentName);
                this.lstVersionNumber.add(versionNumber);
                documentName = documentName.substring(0, documentName.lastIndexOf(".")) + "." + extendName.toLowerCase();
                this.lstZipFileName.add(documentName);
                this.lstZipFilePath.add(basePath + "/" + (segmentNo != null ? "Document/segment" + segmentNo.toString() + "/" : "") + convertedName);
                this.itemSizeTotal += itemSize;
                return;
            }
            if (!DmsDocument.DOCUMENT_LINK.equals(documentType) && (hasPermission || hasInvitation)) {
                if (!DmsRoot.PERSONAL_ROOT.equals(rootType)) {
                    this.lstDocumentId.add(documentId);
                    this.lstDocumentPid.add(documentPid);
                    this.lstDocumentName.add(documentName);
                    this.lstVersionNumber.add(versionNumber);
                }
                if (DmsDocument.DOCUMENT_TYPE.equals(documentType)) {
                    documentName = documentName.substring(0, documentName.lastIndexOf(".")) + "." + extendName.toLowerCase();
                    this.lstZipFileName.add(baseName + "/" + documentName);
                    this.lstZipFilePath.add(basePath + "/" + (segmentNo != null ? "Document/segment" + segmentNo.toString() + "/" : "") + convertedName);
                    this.itemSizeTotal += itemSize;
                } else if (DmsDocument.COMPOUND_DOC_TYPE.equals(documentType) || DmsDocument.PAPER_DOC_TYPE.equals(documentType) || DmsDocument.EMAIL_DOC_TYPE.equals(documentType)) {
                    this.lstZipFileName.add(baseName + "/" + documentName + "[folder]/");
                    this.lstZipFilePath.add("NO_FILE");
                    this.appendZipDownloadRecursive(dmsDocumentDAO, baseName + "/" + documentName + "[folder]", basePath, documentId, rootType, rootOwnerId, request, hasInvitation);
                    if (!DmsDocument.PAPER_DOC_TYPE.equals(documentType)) {
                        documentName = documentName.substring(0, documentName.lastIndexOf(".")) + "." + extendName.toLowerCase();
                        this.lstZipFileName.add(baseName + "/" + documentName);
                        this.lstZipFilePath.add(basePath + "/" + (segmentNo != null ? "Document/segment" + segmentNo.toString() + "/" : "") + convertedName);
                        this.itemSizeTotal += itemSize;
                    }
                } else {
                    this.lstZipFileName.add(baseName + "/" + documentName + "/");
                    this.lstZipFilePath.add("NO_FILE");
                    this.appendZipDownloadRecursive(dmsDocumentDAO, baseName + "/" + documentName, basePath, documentId, rootType, rootOwnerId, request, hasInvitation);
                }
            }
        }
    }

    /**
   * recurse to add zip file information.
   * @param dmsDocumentDAO
   * @param baseName
   * @param basePath
   * @param parentId
   * @param rootType
   * @param rootOwnerId
   * @param request
   * @param hasInvitation
   * @throws ApplicationException
   */
    private void appendZipDownloadRecursive(DmsDocumentDAObject dmsDocumentDAO, String baseName, String basePath, Integer parentId, String rootType, Integer rootOwnerId, HttpServletRequest request, boolean hasInvitation) throws ApplicationException {
        PermissionManager permissionManager = sessionContainer.getPermissionManager();
        List listFieldObject = dmsDocumentDAO.getDVCFieldListByDocumentParentID(fieldNames, fieldTypes, parentId, new String[] { DmsVersion.ARCHIVED_STATUS }, false, new String[] { GlobalConstant.RECORD_STATUS_ACTIVE }, true, "D.DOCUMENT_TYPE ASC, D.ID ASC, V.VERSION_NUMBER DESC");
        Integer old_document_id = null;
        for (int i = 0; i < listFieldObject.size(); i++) {
            Object[] fields = (Object[]) listFieldObject.get(i);
            Integer documentId = (Integer) fields[0];
            if (!documentId.equals(old_document_id)) {
                Integer documentPid = (Integer) fields[1];
                String versionNumber = (String) fields[8];
                String documentName = (String) fields[2];
                if (documentName.lastIndexOf(".") == documentName.length() - 1) {
                    while (documentName.length() > 0 && documentName.lastIndexOf(".") == documentName.length() - 1) {
                        documentName = documentName.substring(0, documentName.length() - 1);
                    }
                }
                String documentType = (String) fields[3];
                String extendName = (String) fields[6];
                String convertedName = (String) fields[4];
                Integer segmentNo = (Integer) fields[5];
                double itemSize = (fields[7] != null ? Double.parseDouble((String) fields[7]) : 0);
                boolean hasPermission = permissionManager.hasAccessRight(conn, GlobalConstant.OBJECT_TYPE_DOCUMENT, documentId, rootType, rootOwnerId, "R");
                if (!DmsDocument.DOCUMENT_LINK.equals(documentType) && (hasPermission || hasInvitation)) {
                    if (!DmsRoot.PERSONAL_ROOT.equals(rootType)) {
                        this.lstDocumentId.add(documentId);
                        this.lstDocumentPid.add(documentPid);
                        this.lstDocumentName.add(documentName);
                        this.lstVersionNumber.add(versionNumber);
                    }
                    if (DmsDocument.DOCUMENT_TYPE.equals(documentType)) {
                        documentName = documentName.substring(0, documentName.lastIndexOf(".")) + "." + extendName.toLowerCase();
                        this.lstZipFileName.add(baseName + "/" + documentName);
                        this.lstZipFilePath.add(basePath + "/" + (segmentNo != null ? "Document/segment" + segmentNo.toString() + "/" : "") + convertedName);
                        this.itemSizeTotal += itemSize;
                    } else if (DmsDocument.COMPOUND_DOC_TYPE.equals(documentType) || DmsDocument.PAPER_DOC_TYPE.equals(documentType) || DmsDocument.EMAIL_DOC_TYPE.equals(documentType)) {
                        this.lstZipFileName.add(baseName + "/" + documentName + "[folder]/");
                        this.lstZipFilePath.add("NO_FILE");
                        this.appendZipDownloadRecursive(dmsDocumentDAO, baseName + "/" + documentName + "[folder]", basePath, documentId, rootType, rootOwnerId, request, hasInvitation);
                        if (!DmsDocument.PAPER_DOC_TYPE.equals(documentType)) {
                            documentName = documentName.substring(0, documentName.lastIndexOf(".")) + "." + extendName.toLowerCase();
                            this.lstZipFileName.add(baseName + "/" + documentName);
                            this.lstZipFilePath.add(basePath + "/" + (segmentNo != null ? "Document/segment" + segmentNo.toString() + "/" : "") + convertedName);
                            this.itemSizeTotal += itemSize;
                        }
                    } else {
                        this.lstZipFileName.add(baseName + "/" + documentName + "/");
                        this.lstZipFilePath.add("NO_FILE");
                        this.appendZipDownloadRecursive(dmsDocumentDAO, baseName + "/" + documentName, basePath, documentId, rootType, rootOwnerId, request, hasInvitation);
                    }
                }
                old_document_id = documentId;
            }
        }
    }

    /**
   * download zip by file name and file path.
   * @param response
   * @throws Exception
   */
    private void downloadZipDocument(HttpServletResponse response) throws Exception {
        ZipOutputStream out = new ZipOutputStream(response.getOutputStream());
        out.setMethod(ZipOutputStream.DEFLATED);
        for (int i = 0; i < this.lstZipFileName.size(); i++) {
            String documentName = (String) this.lstZipFileName.get(i);
            String documentPath = (String) this.lstZipFilePath.get(i);
            if ("NO_FILE".equals(documentPath)) {
                out.putNextEntry(new ZipEntry(documentName));
            } else {
                out.putNextEntry(new ZipEntry(documentName));
                File file = new File(documentPath);
                if (file.exists()) {
                    FileInputStream in = new FileInputStream(file);
                    byte[] buffer = new byte[8192];
                    int length = -1;
                    while ((length = in.read(buffer, 0, 8192)) != -1) {
                        out.write(buffer, 0, length);
                    }
                    in.close();
                }
            }
        }
        out.flush();
        out.close();
    }

    private void auditTrailDocument(AuditTrailDAObject auditDAO, Integer objectID, String versionNumber, String accessType) throws ApplicationException {
        AuditTrail audit = new AuditTrail();
        audit.setObjectType(GlobalConstant.OBJECT_TYPE_DOCUMENT);
        audit.setObjectID(objectID);
        audit.setVersionNumber(versionNumber);
        audit.setAccessType(accessType);
        audit.setSessionID(sessionContainer.getSessionID());
        audit.setAccessorID(sessionContainer.getUserRecordID());
        audit.setIpAddress(sessionContainer.getUserIPAddress());
        auditDAO.insertObject(audit);
    }
}
