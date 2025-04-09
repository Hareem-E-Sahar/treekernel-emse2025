package com.dcivision.dms.web;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import com.dcivision.dms.DmsOperationConstant;
import com.dcivision.dms.bean.DmsContent;
import com.dcivision.dms.bean.DmsDocument;
import com.dcivision.dms.bean.DmsDocumentDetail;
import com.dcivision.dms.bean.DmsLocMaster;
import com.dcivision.dms.bean.DmsRoot;
import com.dcivision.dms.bean.DmsValidation;
import com.dcivision.dms.bean.DmsVersion;
import com.dcivision.dms.core.DmsContentManager;
import com.dcivision.dms.core.DocumentOperationManager;
import com.dcivision.dms.core.DocumentRetrievalManager;
import com.dcivision.dms.core.DocumentValidateManager;
import com.dcivision.dms.core.RootRetrievalManager;
import com.dcivision.dms.core.VersionControlManager;
import com.dcivision.dms.dao.DmsDocumentDAObject;
import com.dcivision.dms.dao.DmsDocumentDetailDAObject;
import com.dcivision.dms.dao.DmsRootDAObject;
import com.dcivision.dms.dao.MtmDocumentRelationshipDAObject;
import com.dcivision.framework.ApplicationContainer;
import com.dcivision.framework.ApplicationException;
import com.dcivision.framework.GlobalConstant;
import com.dcivision.framework.SchedulerFactory;
import com.dcivision.framework.SessionContainer;
import com.dcivision.framework.SystemFunctionConstant;
import com.dcivision.framework.SystemParameterConstant;
import com.dcivision.framework.SystemParameterFactory;
import com.dcivision.framework.TextUtility;
import com.dcivision.framework.Utility;
import com.dcivision.framework.web.AbstractListAction;
import com.dcivision.framework.web.AbstractSearchForm;
import com.dcivision.framework.web.WorkflowActionFormInterface;
import com.dcivision.workflow.web.MaintWorkflowRecordForm;

/**
 * <p>
 * Class Name: ListDmsClipboardAction.java
 * </p>
 * <p>
 * Description: The list action class for ListUserRecord.jsp
 * </p>
 * 
 * @author Jenny Li
 * @company DCIVision Limited
 * @creation date 16/04/2004
 * @version $Revision: 1.74.2.4 $
 */
public class ListDmsClipboardAction extends AbstractListAction implements com.dcivision.framework.web.WorkflowActionInterface {

    public static final String REVISION = "$Revision: 1.74.2.4 $";

    protected static String MESSAGE_RECORD_MOVE = "dms.message.records_move";

    protected static String MESSAGE_RECORD_COPY = "dms.message.records_copy";

    protected static String MESSAGE_RECORD_CHECK_OUT = "dms.message.records_checked_out";

    protected static String MESSAGE_RECORD_NOT_COPY = "dms.message.no_right_copy_doc";

    protected static String MESSAGE_RECORD_NOT_MOVE = "dms.message.no_right_move_doc";

    protected static String CLIPBOARD_SESSION_ATTR_NAME = "DMS_CLIPBOARD";

    /**
   * Constructor - Creates a new instance of ListDmsDocumentAction and define
   * the default listName.
   */
    public ListDmsClipboardAction() {
        super();
        this.setListName("dmsClipbordList");
    }

    /**
   * getMajorDAOClassName
   * 
   * @return The class name of the major DAObject will be used in this action.
   */
    public String getMajorDAOClassName() {
        return ("com.dcivision.dms.dao.DmsDocumentDAObject");
    }

    /**
   * getFunctionCode
   * 
   * @return The corresponding system function code of action.
   */
    public String getFunctionCode() {
        return (SystemFunctionConstant.DMS_PERSONAL_FOLDER + "||" + SystemFunctionConstant.DMS_PUBLIC_FOLDER);
    }

    /**
   * execute - Override the parent function.
   * 
   * @param mapping
   * @param form
   * @param request
   * @param response
   * @return
   * @throws ServletException
   */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws ServletException {
        SessionContainer sessionContainer = this.getSessionContainer(request);
        Connection conn = this.getConnection(request);
        ListDmsDocumentForm searchForm = (ListDmsDocumentForm) form;
        DocumentRetrievalManager docRetrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
        DmsDocumentDAObject dmsDocumentDAO = new DmsDocumentDAObject(sessionContainer, conn);
        String opMode = searchForm.getOpMode();
        String[] selectedID = searchForm.getBasicSelectedID();
        List dmsClipbordList = new ArrayList();
        ActionForward forward = this.retrieveFunctionCode(request, response, mapping);
        if (forward != null) {
            return forward;
        }
        if (DmsOperationConstant.COPY_OPERATION.equals(opMode)) {
            log.info("Going to copy multiple!" + searchForm.getCleanClipboard());
            try {
                this.regeditDuplicateNameCache(request);
                this.copyMultiple(mapping, searchForm, request, response);
                try {
                    this.handleWorkflowRoutine(mapping, searchForm, request, response, opMode, searchForm.getNavMode());
                } catch (ApplicationException e) {
                    log.error(e, e);
                    throw e;
                }
                this.commit(request);
            } catch (ApplicationException appEx) {
                this.rollback(request);
                handleApplicationException(request, appEx);
            } finally {
                this.unlockDuplicateNameCache(request);
            }
            if (isError(request)) {
                this.getListData(mapping, searchForm, request, response);
                searchForm.setNavMode(GlobalConstant.NAV_MODE_LIST);
                searchForm.setOpMode("");
                return mapping.findForward(GlobalConstant.NAV_MODE_LIST);
            } else {
                if (!searchForm.getCleanClipboard()) {
                    ArrayList clipboardList = (ArrayList) request.getSession().getAttribute("DMS_CLIPBOARD");
                    for (int i = 0; i < selectedID.length; i++) {
                        if (!Utility.isEmpty(selectedID[i])) {
                            DmsDocument document = docRetrievalManager.getDocument(TextUtility.parseIntegerObj(selectedID[i]));
                            clipboardList.remove(document.getID());
                        }
                    }
                    if (!Utility.isEmpty(clipboardList)) {
                        for (int i = 0; i < clipboardList.size(); i++) {
                            Integer id = (Integer) clipboardList.get(i);
                            DmsDocument dmsDocument = (DmsDocument) dmsDocumentDAO.getByIDRecordStatus(id, GlobalConstant.RECORD_STATUS_ACTIVE);
                            dmsClipbordList.add(dmsDocument);
                        }
                    }
                    request.getSession().setAttribute("DMS_CLIPBOARD", clipboardList);
                    request.setAttribute(this.getListName(), dmsClipbordList);
                } else {
                    this.getListData(mapping, searchForm, request, response);
                }
                return mapping.findForward(DmsOperationConstant.COPY_OPERATION);
            }
        } else if (DmsOperationConstant.MOVE_OPERATION.equals(opMode)) {
            log.info("Going to move multiple!" + searchForm.getCleanClipboard());
            try {
                this.regeditDuplicateNameCache(request);
                this.moveMultiple(mapping, searchForm, request, response);
                try {
                    this.handleWorkflowRoutine(mapping, searchForm, request, response, opMode, searchForm.getNavMode());
                } catch (ApplicationException e) {
                    log.error(e);
                    throw e;
                }
                this.commit(request);
            } catch (ApplicationException appEx) {
                this.rollback(request);
                handleApplicationException(request, appEx);
            } finally {
                this.unlockDuplicateNameCache(request);
            }
            if (isError(request)) {
                this.getListData(mapping, searchForm, request, response);
                searchForm.setNavMode(GlobalConstant.NAV_MODE_LIST);
                return mapping.findForward(GlobalConstant.NAV_MODE_LIST);
            } else {
                ArrayList clipboardList = (ArrayList) request.getSession().getAttribute("DMS_CLIPBOARD");
                for (int i = 0; i < selectedID.length; i++) {
                    if (!Utility.isEmpty(selectedID[i])) {
                        DmsDocument document = docRetrievalManager.getDocument(TextUtility.parseIntegerObj(selectedID[i]));
                        clipboardList.remove(document.getID());
                    }
                }
                if (!Utility.isEmpty(clipboardList)) {
                    for (int i = 0; i < clipboardList.size(); i++) {
                        Integer id = (Integer) clipboardList.get(i);
                        DmsDocument dmsDocument = (DmsDocument) dmsDocumentDAO.getByIDRecordStatus(id, GlobalConstant.RECORD_STATUS_ACTIVE);
                        dmsClipbordList.add(dmsDocument);
                    }
                }
                request.getSession().setAttribute("DMS_CLIPBOARD", clipboardList);
                request.setAttribute(this.getListName(), dmsClipbordList);
                return mapping.findForward(DmsOperationConstant.MOVE_OPERATION);
            }
        } else if ("ZIP.DOWNLOAD".equals(opMode)) {
            List clipboardList = (List) request.getSession().getAttribute("DMS_CLIPBOARD");
            String zipDocumentId = "";
            for (int i = 0; i < selectedID.length; i++) {
                if (!Utility.isEmpty(selectedID[i])) {
                    if (!searchForm.getCleanClipboard()) {
                        clipboardList.remove(TextUtility.parseIntegerObj(selectedID[i]));
                    }
                    zipDocumentId += zipDocumentId.equals("") ? selectedID[i] : "," + selectedID[i];
                }
            }
            request.getSession().setAttribute("DMS_CLIPBOARD", clipboardList);
            request.setAttribute("ZIP_DOUCMENT_ID", zipDocumentId);
            this.getListData(mapping, searchForm, request, response);
            return mapping.findForward(GlobalConstant.NAV_MODE_LIST);
        } else if ("MUL_CHECKOUT".equals(opMode)) {
            try {
                this.checkOutMultiple(mapping, searchForm, request, response);
                this.commit(request);
                addMessage(request, MESSAGE_RECORD_CHECK_OUT);
                request.setAttribute("SUCCESS_CHECKOUT", "true");
            } catch (ApplicationException appEx) {
                this.rollback(request);
                handleApplicationException(request, appEx);
            }
            if (!searchForm.getCleanClipboard()) {
                ArrayList clipboardList = (ArrayList) request.getSession().getAttribute("DMS_CLIPBOARD");
                for (int i = 0; i < selectedID.length; i++) {
                    if (!Utility.isEmpty(selectedID[i])) {
                        DmsDocument document = docRetrievalManager.getDocument(TextUtility.parseIntegerObj(selectedID[i]));
                        clipboardList.remove(document.getID());
                    }
                }
                if (!Utility.isEmpty(clipboardList)) {
                    for (int i = 0; i < clipboardList.size(); i++) {
                        Integer id = (Integer) clipboardList.get(i);
                        DmsDocument dmsDocument = (DmsDocument) dmsDocumentDAO.getByIDRecordStatus(id, GlobalConstant.RECORD_STATUS_ACTIVE);
                        dmsClipbordList.add(dmsDocument);
                    }
                }
                request.getSession().setAttribute("DMS_CLIPBOARD", clipboardList);
                request.setAttribute(this.getListName(), dmsClipbordList);
            } else {
                this.getListData(mapping, searchForm, request, response);
            }
            return mapping.findForward(GlobalConstant.NAV_MODE_LIST);
        } else if ("INVITATION".equals(opMode)) {
            if (!searchForm.getCleanClipboard()) {
                ArrayList clipboardList = (ArrayList) request.getSession().getAttribute("DMS_CLIPBOARD");
                for (int i = 0; i < selectedID.length; i++) {
                    if (!Utility.isEmpty(selectedID[i])) {
                        DmsDocument document = docRetrievalManager.getDocument(TextUtility.parseIntegerObj(selectedID[i]));
                        clipboardList.remove(document.getID());
                    }
                }
                if (!Utility.isEmpty(clipboardList)) {
                    for (int i = 0; i < clipboardList.size(); i++) {
                        Integer id = (Integer) clipboardList.get(i);
                        DmsDocument dmsDocument = (DmsDocument) dmsDocumentDAO.getByIDRecordStatus(id, GlobalConstant.RECORD_STATUS_ACTIVE);
                        dmsClipbordList.add(dmsDocument);
                    }
                }
                request.getSession().setAttribute("DMS_CLIPBOARD", clipboardList);
                request.setAttribute(this.getListName(), dmsClipbordList);
            } else {
                this.getListData(mapping, searchForm, request, response);
            }
            return mapping.findForward(GlobalConstant.NAV_MODE_LIST);
        } else if ("SENDEMAIL".equals(opMode)) {
            if (!searchForm.getCleanClipboard()) {
                ArrayList clipboardList = (ArrayList) request.getSession().getAttribute("DMS_CLIPBOARD");
                for (int i = 0; i < selectedID.length; i++) {
                    if (!Utility.isEmpty(selectedID[i])) {
                        DmsDocument document = docRetrievalManager.getDocument(TextUtility.parseIntegerObj(selectedID[i]));
                        clipboardList.remove(document.getID());
                    }
                }
                if (!Utility.isEmpty(clipboardList)) {
                    for (int i = 0; i < clipboardList.size(); i++) {
                        Integer id = (Integer) clipboardList.get(i);
                        DmsDocument dmsDocument = (DmsDocument) dmsDocumentDAO.getByIDRecordStatus(id, GlobalConstant.RECORD_STATUS_ACTIVE);
                        dmsClipbordList.add(dmsDocument);
                    }
                }
                request.getSession().setAttribute("DMS_CLIPBOARD", clipboardList);
                request.setAttribute(this.getListName(), dmsClipbordList);
            } else {
                this.getListData(mapping, searchForm, request, response);
            }
            return mapping.findForward(GlobalConstant.NAV_MODE_LIST);
        } else {
            return super.execute(mapping, form, request, response);
        }
    }

    /**
   * getListData
   * 
   * Override the parent's function. Purpose in create the default personal
   * folder when non-exists, and load the dmsDocument list.
   * 
   * @param mapping
   * @param form
   * @param request
   * @param response
   * @throws ApplicationException
   */
    public void getListData(ActionMapping mapping, AbstractSearchForm form, HttpServletRequest request, HttpServletResponse response) throws ApplicationException {
        SessionContainer sessionContainer = this.getSessionContainer(request);
        Connection conn = this.getConnection(request);
        int startOffset = TextUtility.parseInteger(form.getCurStartRowNo());
        int pageSize = TextUtility.parseInteger(form.getPageOffset());
        ArrayList clipboardList = (ArrayList) request.getSession().getAttribute("DMS_CLIPBOARD");
        List dmsClipbordList = new ArrayList();
        DocumentRetrievalManager docRetrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
        if (!Utility.isEmpty(clipboardList)) {
            for (int i = 0; i < clipboardList.size(); i++) {
                DmsDocument document = docRetrievalManager.getDocument((Integer) clipboardList.get(i));
                if (!GlobalConstant.RECORD_STATUS_ACTIVE.equals(document.getRecordStatus())) {
                    clipboardList.remove(document.getID());
                }
            }
            request.getSession().setAttribute("DMS_CLIPBOARD", clipboardList);
        }
        if (!Utility.isEmpty(clipboardList)) {
            if (clipboardList.size() - startOffset >= pageSize) {
                for (int i = startOffset - 1; i < startOffset - 1 + pageSize; i++) {
                    Integer id = (Integer) clipboardList.get(i);
                    DmsDocument dmsDocument = docRetrievalManager.getDocument(id);
                    if (GlobalConstant.RECORD_STATUS_ACTIVE.equals(dmsDocument.getRecordStatus())) {
                        List docDetailList = docRetrievalManager.getDocumentDetailList(id);
                        if (docDetailList != null && docDetailList.size() > 0) {
                            dmsDocument.setUserDefinedFieldID(((DmsDocumentDetail) docDetailList.get(0)).getUserDefinedFieldID());
                        }
                        dmsDocument.setRowNum(i + 1);
                        dmsDocument.setRecordCount(clipboardList.size());
                        dmsDocument.setHasRelationship(docRetrievalManager.hasRelationship(dmsDocument.getID(), ""));
                        dmsClipbordList.add(dmsDocument);
                    }
                }
            } else {
                for (int i = startOffset - 1; i < clipboardList.size(); i++) {
                    Integer id = (Integer) clipboardList.get(i);
                    DmsDocument dmsDocument = docRetrievalManager.getDocument(id);
                    if (GlobalConstant.RECORD_STATUS_ACTIVE.equals(dmsDocument.getRecordStatus())) {
                        List docDetailList = docRetrievalManager.getDocumentDetailList(id);
                        if (docDetailList != null && docDetailList.size() > 0) {
                            dmsDocument.setUserDefinedFieldID(((DmsDocumentDetail) docDetailList.get(0)).getUserDefinedFieldID());
                        }
                        dmsDocument.setRowNum(i + 1);
                        dmsDocument.setRecordCount(clipboardList.size());
                        dmsDocument.setHasRelationship(docRetrievalManager.hasRelationship(dmsDocument.getID(), ""));
                        dmsClipbordList.add(dmsDocument);
                    }
                }
            }
            request.setAttribute(this.getListName(), dmsClipbordList);
        } else {
            request.setAttribute(this.getListName(), new ArrayList());
        }
        conn = null;
    }

    public void copyMultiple(ActionMapping mapping, AbstractSearchForm form, HttpServletRequest request, HttpServletResponse response) throws ApplicationException {
        SessionContainer sessionContainer = this.getSessionContainer(request);
        Connection conn = this.getConnection(request);
        ListDmsClipboardForm documentForm = (ListDmsClipboardForm) form;
        DocumentRetrievalManager docRetrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
        DocumentOperationManager docOperationManager = new DocumentOperationManager(sessionContainer, conn);
        DocumentValidateManager docValidateManager = new DocumentValidateManager(sessionContainer, conn);
        DmsDocumentDetailDAObject docDetailDAO = new DmsDocumentDetailDAObject(sessionContainer, conn);
        Integer destinID = documentForm.getDestinationID() != null ? new Integer(documentForm.getDestinationID()) : null;
        DmsDocument destinDocument = docRetrievalManager.getDocument(destinID);
        String[] selectedID = form.getBasicSelectedID();
        List wfRecordList = new ArrayList();
        for (int n = 0; n < selectedID.length; n++) {
            if (Utility.isEmpty(selectedID)) {
                continue;
            }
            String tmpSelectedID = selectedID[n];
            DmsDocument sourceDocument = docRetrievalManager.getDocument(TextUtility.parseIntegerObj(tmpSelectedID));
            DmsDocument newDmsDocument = (DmsDocument) sourceDocument.clone();
            List lstDocumentDetail = docDetailDAO.getListByDocumentID(sourceDocument.getID());
            if (!Utility.isEmpty(lstDocumentDetail)) {
                newDmsDocument.setDocumentDetails(lstDocumentDetail);
                DmsDocumentDetail documentDetail = (DmsDocumentDetail) lstDocumentDetail.get(0);
                if (!Utility.isEmpty(documentDetail)) {
                    newDmsDocument.setUserDefinedFieldID(documentDetail.getUserDefinedFieldID());
                }
            }
            String wfRecordID = request.getParameter("wfRecordID_" + tmpSelectedID);
            String wfComment = request.getParameter("wfRecordComment_" + tmpSelectedID);
            boolean bDocumentActive = false;
            if (!Utility.isEmpty(wfRecordID) && !"null".equals(wfRecordID)) {
                bDocumentActive = true;
            }
            boolean[] options = new boolean[4];
            options[0] = bDocumentActive;
            options[1] = DmsOperationConstant.COPY_OPERATION_AS_NEW.equals(documentForm.getCopyFileOpt());
            options[2] = DmsOperationConstant.COPY_ALL_VERSIONS.equals(request.getParameter("doc" + tmpSelectedID));
            options[3] = DmsOperationConstant.COPY_STRUCTURE_ONLY.equals(request.getParameter("copyOnly" + tmpSelectedID));
            DmsValidation validation = docValidateManager.validateCopyAction(sourceDocument, destinDocument, newDmsDocument, options);
            if (!validation.isSuccess()) {
                this.addError(request, "errors.dms.fail_to_copy", docRetrievalManager.getLocationPath(TextUtility.parseIntegerObj(tmpSelectedID)));
                if (validation.isInvalidName()) {
                    String sysInvalidCharacters = SystemParameterFactory.getSystemParameter(SystemParameterConstant.INVALID_CHARACTER);
                    addError(request, "errors.framework.character_error", sysInvalidCharacters);
                } else if (validation.isDuplicateName()) {
                    addError(request, "errors.dms.duplicate_document_name");
                } else if (validation.isUnderSameNode()) {
                    addError(request, "errors.dms.location_under_same_node");
                } else if (validation.isNotEnoughDiskSpace()) {
                    addError(request, "errors.dms.not_enough_disk_space", String.valueOf(validation.getDiskSpaceFree()), String.valueOf(validation.getDiskSpaceRequire()));
                } else if (validation.isNotEnoughStorageSpace()) {
                    addError(request, "errors.dms.no_available_space");
                } else {
                    if (!validation.getLstIdReject().isEmpty()) {
                        List lstIdReject = validation.getLstIdReject();
                        for (int i = 0; i < lstIdReject.size(); i++) {
                            addError(request, "errors.dms.no_permission", docRetrievalManager.getLocationPath((Integer) lstIdReject.get(i)));
                        }
                    }
                    if (!validation.getLstIdlocked().isEmpty()) {
                        List lstIdLocked = validation.getLstIdlocked();
                        for (int i = 0; i < lstIdLocked.size(); i++) {
                            addError(request, "errors.dms.been_checkout", docRetrievalManager.getLocationPath((Integer) lstIdLocked.get(i)));
                        }
                    }
                }
            } else {
                ApplicationContainer container = (ApplicationContainer) request.getSession().getServletContext().getAttribute(GlobalConstant.APPLICATION_CONTAINER_KEY);
                String cacheKey = "C" + sessionContainer.getSessionID() + "" + System.currentTimeMillis();
                try {
                    boolean success = container.checkAndLockDocumentOperationID(cacheKey, validation.getLstIdAccept(), "ACDMU");
                    if (!success) {
                        this.addError(request, "errors.dms.fail_to_copy", docRetrievalManager.getLocationPath(sourceDocument.getID()));
                        this.addError(request, "errors.dms.cannot_edit_now");
                    } else {
                        DmsDocument newTmpDocument = docOperationManager.copyDocument(sourceDocument, destinDocument, newDmsDocument, validation, options);
                        MaintDmsDocumentForm maintDmsDocumentForm = new MaintDmsDocumentForm();
                        MaintDmsDocumentAction maintDmsDocumentAction = new MaintDmsDocumentAction();
                        com.dcivision.user.dao.UserPermissionInheritDAObject userPermInheritDAO = new com.dcivision.user.dao.UserPermissionInheritDAObject(this.getSessionContainer(request), this.getConnection(request));
                        if (Utility.isEmpty(userPermInheritDAO.getObjectByObjectTypeObjectID(sourceDocument.getDocumentType(), sourceDocument.getID()))) {
                            maintDmsDocumentForm.setInheritanceFlag(GlobalConstant.TRUE);
                        } else {
                            maintDmsDocumentForm.setInheritanceFlag(null);
                        }
                        maintDmsDocumentForm.setID(sourceDocument.getID().toString());
                        maintDmsDocumentForm.setObjectID(sourceDocument.getID().toString());
                        maintDmsDocumentAction.selectPermissionObjects(mapping, maintDmsDocumentForm, request, response);
                        String permData = TextUtility.replaceString(maintDmsDocumentForm.getAllPermissionData(), "\r", "");
                        String[] tmpAry = TextUtility.splitString(permData, "\n");
                        String tempPermission = "";
                        if (!Utility.isEmpty(tmpAry)) {
                            for (int t = 0; t < tmpAry.length; t++) {
                                String[] rowAry = TextUtility.splitString(tmpAry[t], "\t");
                                rowAry[7] = "-1";
                                for (int m = 0; m < rowAry.length; m++) {
                                    tempPermission += rowAry[m] + "\t";
                                }
                                tempPermission += "\n";
                            }
                        }
                        maintDmsDocumentForm.setAllPermissionData(tempPermission);
                        maintDmsDocumentForm.setObjectID(newTmpDocument.getID().toString());
                        maintDmsDocumentAction.updateInheritanceFlag(mapping, maintDmsDocumentForm, request, response);
                        maintDmsDocumentAction.updatePermissionSetting(maintDmsDocumentForm, sessionContainer, conn, newTmpDocument.getID());
                        if (!Utility.isEmpty(wfRecordID) && !"null".equals(wfRecordID)) {
                            MaintWorkflowRecordForm recordForm = new MaintWorkflowRecordForm();
                            if (wfRecordID.indexOf("|") > 0) {
                                recordForm.setID(wfRecordID.substring(0, wfRecordID.indexOf("|")));
                            } else {
                                recordForm.setID(wfRecordID);
                            }
                            recordForm.setComment(wfComment);
                            recordForm.setWorkflowObject(newTmpDocument);
                            wfRecordList.add(recordForm);
                        }
                        this.addMessage(request, "dms.message.document_copy_success", docRetrievalManager.getLocationPath(sourceDocument.getID()), String.valueOf(validation.getLstIdAccept().size()));
                    }
                } catch (ApplicationException ex) {
                    throw ex;
                } finally {
                    container.unlockDmsDocumentOperationID(cacheKey);
                    container = null;
                }
            }
        }
        documentForm.setNavMode(GlobalConstant.NAV_MODE_VIEW);
        documentForm.setParentID(destinID.toString());
        documentForm.setRootID(documentForm.getTargetRootID());
        if (!Utility.isEmpty(wfRecordList)) {
            documentForm.setWfRecordList(wfRecordList);
            documentForm.setSubmitSystemWorkflow(GlobalConstant.TRUE);
        }
        conn = null;
        sessionContainer = null;
        docRetrievalManager.release();
        docValidateManager.release();
        docOperationManager.release();
    }

    public void moveMultiple(ActionMapping mapping, AbstractSearchForm form, HttpServletRequest request, HttpServletResponse response) throws ApplicationException {
        ListDmsClipboardForm documentForm = (ListDmsClipboardForm) form;
        SessionContainer sessionContainer = this.getSessionContainer(request);
        Connection conn = this.getConnection(request);
        DocumentRetrievalManager docRetrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
        DocumentOperationManager docOperationManager = new DocumentOperationManager(sessionContainer, conn);
        DocumentValidateManager docValidateManager = new DocumentValidateManager(sessionContainer, conn);
        Integer destinID = documentForm.getDestinationID() != null ? new Integer(documentForm.getDestinationID()) : null;
        DmsDocument destinDocument = docRetrievalManager.getDocument(destinID);
        String[] selectedID = form.getBasicSelectedID();
        List wfRecordList = new ArrayList();
        for (int n = 0; n < selectedID.length; n++) {
            if (Utility.isEmpty(selectedID)) {
                continue;
            }
            String tmpSelectedID = selectedID[n];
            DmsDocument sourceDocument = docRetrievalManager.getDocument(TextUtility.parseIntegerObj(tmpSelectedID));
            DmsDocument newDmsDocument = (DmsDocument) sourceDocument.clone();
            String wfRecordID = request.getParameter("wfRecordID_" + tmpSelectedID);
            String wfComment = request.getParameter("wfRecordComment_" + tmpSelectedID);
            boolean bDocumentActive = false;
            if (!Utility.isEmpty(wfRecordID) && !"null".equals(wfRecordID)) {
                bDocumentActive = true;
            }
            DmsValidation validation = docValidateManager.validateMoveAction(sourceDocument, destinDocument, newDmsDocument);
            if (!validation.isSuccess()) {
                this.addError(request, "errors.dms.fail_to_move", docRetrievalManager.getLocationPath(TextUtility.parseIntegerObj(tmpSelectedID)));
                if (validation.isInvalidName()) {
                    String sysInvalidCharacters = SystemParameterFactory.getSystemParameter(SystemParameterConstant.INVALID_CHARACTER);
                    addError(request, "errors.framework.character_error", sysInvalidCharacters);
                } else if (validation.isDuplicateName()) {
                    addError(request, "errors.dms.duplicate_document_name");
                } else if (validation.isUnderSameNode()) {
                    addError(request, "errors.dms.location_under_same_node");
                } else if (validation.isNotEnoughDiskSpace()) {
                    addError(request, "errors.dms.not_enough_disk_space", String.valueOf(validation.getDiskSpaceFree()), String.valueOf(validation.getDiskSpaceRequire()));
                } else if (validation.isNotEnoughStorageSpace()) {
                    addError(request, "errors.dms.no_available_space");
                } else {
                    if (!validation.getLstIdReject().isEmpty()) {
                        List lstIdReject = validation.getLstIdReject();
                        for (int i = 0; i < lstIdReject.size(); i++) {
                            addError(request, "errors.dms.no_permission", docRetrievalManager.getLocationPath((Integer) lstIdReject.get(i)));
                        }
                    }
                    if (!validation.getLstIdlocked().isEmpty()) {
                        List lstIdLocked = validation.getLstIdlocked();
                        for (int i = 0; i < lstIdLocked.size(); i++) {
                            addError(request, "errors.dms.been_checkout", docRetrievalManager.getLocationPath((Integer) lstIdLocked.get(i)));
                        }
                    }
                    if (!validation.getLstIdArchived().isEmpty()) {
                        List lstIdArchived = validation.getLstIdArchived();
                        for (int i = 0; i < lstIdArchived.size(); i++) {
                            addError(request, "errors.dms.been_archived", docRetrievalManager.getLocationPath((Integer) lstIdArchived.get(i)));
                        }
                    }
                }
            } else {
                ApplicationContainer container = (ApplicationContainer) request.getSession().getServletContext().getAttribute(GlobalConstant.APPLICATION_CONTAINER_KEY);
                String cacheKey = "M" + sessionContainer.getSessionID() + "" + System.currentTimeMillis();
                try {
                    boolean success = container.checkAndLockDocumentOperationID(cacheKey, validation.getLstIdAccept(), "ACDIMU");
                    if (!success) {
                        this.addError(request, "errors.dms.fail_to_move", docRetrievalManager.getLocationPath(sourceDocument.getID()));
                        this.addError(request, "errors.dms.cannot_edit_now");
                    } else {
                        docOperationManager.moveDocument(sourceDocument, destinDocument, newDmsDocument, validation, bDocumentActive);
                        if (!Utility.isEmpty(wfRecordID) && !"null".equals(wfRecordID)) {
                            MaintWorkflowRecordForm recordForm = new MaintWorkflowRecordForm();
                            if (wfRecordID.indexOf("|") > 0) {
                                recordForm.setID(wfRecordID.substring(0, wfRecordID.indexOf("|")));
                            } else {
                                recordForm.setID(wfRecordID);
                            }
                            recordForm.setComment(wfComment);
                            recordForm.setWorkflowObject(sourceDocument);
                            wfRecordList.add(recordForm);
                        }
                        this.addMessage(request, "dms.message.document_move_success", docRetrievalManager.getLocationPath(sourceDocument.getID()), String.valueOf(validation.getLstIdAccept().size()));
                    }
                } catch (ApplicationException ex) {
                    throw ex;
                } finally {
                    container.unlockDmsDocumentOperationID(cacheKey);
                    container = null;
                }
            }
        }
        documentForm.setNavMode(GlobalConstant.NAV_MODE_VIEW);
        if (!Utility.isEmpty(wfRecordList)) {
            documentForm.setWfRecordList(wfRecordList);
            documentForm.setSubmitSystemWorkflow(GlobalConstant.TRUE);
        }
        sessionContainer = null;
        conn = null;
        docValidateManager.release();
        docOperationManager.release();
        docRetrievalManager.release();
    }

    public void executeWorkflowRoutine(ActionMapping mapping, WorkflowActionFormInterface form, HttpServletRequest request, HttpServletResponse response, String opMode, String navMode) throws ApplicationException {
    }

    public StringBuffer getCompoundZip(DmsDocument document, HttpServletRequest request, HttpServletResponse response) throws ApplicationException {
        SessionContainer sessionContainer = this.getSessionContainer(request);
        Connection conn = this.getConnection(request);
        DocumentRetrievalManager docRetrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
        RootRetrievalManager rootRetrievalManager = new RootRetrievalManager(sessionContainer, conn);
        DmsContentManager dmsContentManager = new DmsContentManager(sessionContainer, conn);
        InputStream inputStream = null;
        InputStream dataStream = null;
        String zipfilename = document.getDocumentName() + ".zip";
        StringBuffer comZipFile = new StringBuffer();
        DmsLocMaster locMaster = rootRetrievalManager.getTargetLocMasterByDocument(document);
        String locMasterPath = locMaster.getLocPath();
        comZipFile = comZipFile.append(locMasterPath).append("/").append(zipfilename);
        try {
            ZipOutputStream zipOutStream = new ZipOutputStream(new FileOutputStream(comZipFile.toString()));
            DocumentRetrievalManager docRetrieval = new DocumentRetrievalManager(sessionContainer, conn);
            zipOutStream.setEncoding(docRetrieval.getZipFileDefaultEncode());
            DocumentOperationManager docOperationManager = new DocumentOperationManager(sessionContainer, conn);
            MtmDocumentRelationshipDAObject docRelationshipDAO = new MtmDocumentRelationshipDAObject(sessionContainer, conn);
            Integer targetID = document.getID();
            Integer contentID = null;
            List list = docOperationManager.getSubDocumentByParentID(document.getID());
            if (DmsDocument.COMPOUND_DOC_TYPE.equals(document.getDocumentType())) {
                list.add(document);
            }
            if (!Utility.isEmpty(list)) {
                for (int j = 0; j < list.size(); j++) {
                    DmsDocument dmsDocument = (DmsDocument) list.get(j);
                    String fName = dmsDocument.getDocumentName();
                    DmsVersion sVersion = docRetrievalManager.getTopVersionByDocumentID(dmsDocument.getID());
                    contentID = sVersion.getContentID();
                    if (DmsDocument.DOCUMENT_LINK.equals(dmsDocument.getDocumentType())) {
                        Integer targetDocID = docRelationshipDAO.getTargetDocIDByRelatedDocID(dmsDocument.getID(), dmsDocument.getDocumentType());
                        DmsDocument targetDoc = docRetrievalManager.getDocument(targetDocID);
                        if (targetDoc != null) {
                            targetDoc.setDocumentName(dmsDocument.getDocumentName());
                            targetDoc.setDocumentType(dmsDocument.getDocumentType());
                            targetID = targetDocID;
                            dmsDocument = targetDoc;
                            sVersion = docRetrievalManager.getTopVersionByDocumentID(targetID);
                            contentID = sVersion.getContentID();
                        } else {
                            document.setRecordStatus(GlobalConstant.RECORD_STATUS_INACTIVE);
                        }
                    }
                    if (!GlobalConstant.RECORD_STATUS_INACTIVE.equals(document.getRecordStatus())) {
                        DmsContent docContent = docRetrievalManager.getContentByContentID(contentID);
                        dataStream = dmsContentManager.readDmsDocumentStoreContent(dmsDocument, docContent);
                        ZipEntry theEntry = new ZipEntry(fName);
                        zipOutStream.putNextEntry(theEntry);
                        byte[] buffer = new byte[8192];
                        int length = -1;
                        inputStream = dataStream;
                        while ((length = inputStream.read(buffer, 0, 8192)) != -1) {
                            zipOutStream.write(buffer, 0, length);
                        }
                    }
                }
            }
            zipOutStream.flush();
            zipOutStream.close();
        } catch (Exception e) {
            log.error(e, e);
            throw new ApplicationException(com.dcivision.framework.ErrorConstant.COMMON_FATAL_ERROR, e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignore) {
                inputStream = null;
            }
        }
        return comZipFile;
    }

    protected boolean hasSubDocument(DmsDocument dmsDocument, HttpServletRequest request) throws ApplicationException {
        DocumentOperationManager docOperationManager = new DocumentOperationManager(this.getSessionContainer(request), this.getConnection(request));
        List list = docOperationManager.getSubDocumentByParentID(dmsDocument.getID());
        if (list.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void deleteListData(ActionMapping mapping, AbstractSearchForm form, HttpServletRequest request, HttpServletResponse response) throws ApplicationException {
        SessionContainer sessionContainer = this.getSessionContainer(request);
        Connection conn = this.getConnection(request);
        DocumentValidateManager docValidateManager = new DocumentValidateManager(sessionContainer, conn);
        DocumentOperationManager docOperationManager = new DocumentOperationManager(sessionContainer, conn);
        DocumentRetrievalManager docRetrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
        DmsDocumentDAObject dmsDocumentDAO = new DmsDocumentDAObject(sessionContainer, conn);
        ApplicationContainer container = (ApplicationContainer) request.getSession().getServletContext().getAttribute(GlobalConstant.APPLICATION_CONTAINER_KEY);
        List clipboardList = (List) request.getSession().getAttribute("DMS_CLIPBOARD");
        String[] selectedID = form.getBasicSelectedID();
        selectedID = docValidateManager.validatorFilterDocID(selectedID);
        boolean isIgnoreDeleteShortcut = ((ListDmsDocumentForm) form).isIgnoreDeleteShortcut();
        for (int n = 0; n < selectedID.length; n++) {
            Integer srcDocumentId = TextUtility.parseIntegerObj(selectedID[n]);
            DmsDocument sourceDocument = (DmsDocument) dmsDocumentDAO.getObjectByID(srcDocumentId);
            DmsValidation validation = docValidateManager.validateDeleteAction(sourceDocument, isIgnoreDeleteShortcut);
            String cacheKey = "D" + sessionContainer.getSessionID() + "" + System.currentTimeMillis();
            try {
                List deleteDocumentIds = validation.getLstIdAccept();
                boolean success = container.checkAndLockDocumentOperationID(cacheKey, deleteDocumentIds, "ACDIMU");
                if (!success) {
                    this.addError(request, "errors.dms.fail_to_delete", docRetrievalManager.getLocationPath(srcDocumentId));
                    this.addError(request, "errors.dms.cannot_edit_now");
                } else {
                    docOperationManager.deleteDocument(sourceDocument, validation);
                    List lstIdHasRight = validation.getLstIdAccept();
                    List lstIdMisRight = validation.getLstIdReject();
                    List lstIdBeLocked = validation.getLstIdlocked();
                    List lstIdBeDeleted = validation.getLstIdDeleted();
                    List lstIdBeArchived = validation.getLstIdArchived();
                    List lstIdRelationship = validation.getLstIdHaveRelationship();
                    String locationPath = docRetrievalManager.getLocationPath(srcDocumentId);
                    if (validation.isSuccess()) {
                        this.addMessage(request, "dms.message.document_deleted_success", locationPath, String.valueOf(lstIdHasRight.size()));
                    } else {
                        this.addError(request, "errors.dms.fail_to_delete", locationPath);
                        for (int i = 0; i < lstIdMisRight.size(); i++) {
                            this.addError(request, "errors.dms.no_permission", docRetrievalManager.getLocationPath((Integer) lstIdMisRight.get(i)));
                        }
                        for (int i = 0; i < lstIdBeLocked.size(); i++) {
                            this.addError(request, "errors.dms.been_checkout", docRetrievalManager.getLocationPath((Integer) lstIdBeLocked.get(i)));
                        }
                        for (int i = 0; i < lstIdBeDeleted.size(); i++) {
                            this.addError(request, "errors.dms.been_deleted", docRetrievalManager.getLocationPath((Integer) lstIdBeDeleted.get(i)));
                        }
                        for (int i = 0; i < lstIdBeArchived.size(); i++) {
                            this.addError(request, "errors.dms.been_archived", docRetrievalManager.getLocationPath((Integer) lstIdBeArchived.get(i)));
                        }
                        for (int i = 0; i < lstIdRelationship.size(); i++) {
                            this.addError(request, "errors.dms.has_relationship", docRetrievalManager.getLocationPath((Integer) lstIdRelationship.get(i)));
                        }
                        if (!Utility.isEmpty(lstIdHasRight)) {
                            this.addError(request, "dms.message.document_been_deleted", String.valueOf(lstIdHasRight.size()));
                        }
                    }
                    if (clipboardList != null && !clipboardList.isEmpty()) {
                        for (int i = 0; i < clipboardList.size(); i++) {
                            if (deleteDocumentIds.contains(clipboardList.get(i))) {
                                clipboardList.remove(i--);
                            }
                        }
                    }
                }
            } catch (ApplicationException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new ApplicationException(ex);
            } finally {
                container.unlockDmsDocumentOperationID(cacheKey);
            }
        }
        form.setBasicSelectedID(null);
        request.getSession().setAttribute("DMS_CLIPBOARD", clipboardList);
        form.setNavMode(GlobalConstant.NAV_MODE_VIEW);
        conn = null;
        sessionContainer = null;
        container = null;
        docValidateManager.release();
        docOperationManager.release();
        docRetrievalManager.release();
    }

    public void getInviteData(ActionMapping mapping, AbstractSearchForm form, HttpServletRequest request, HttpServletResponse response) throws ApplicationException {
        SessionContainer sessionContainer = this.getSessionContainer(request);
        Connection conn = this.getConnection(request);
        ListDmsDocumentForm documentForm = (ListDmsDocumentForm) form;
        DocumentOperationManager docOperationManager = null;
        DocumentRetrievalManager docRetrievalManager = null;
        try {
            docOperationManager = new DocumentOperationManager(sessionContainer, conn);
            docRetrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
            String[] selectedID = (form).getBasicSelectedID();
            if (!Utility.isEmpty(selectedID)) {
                for (int i = 0; i < selectedID.length; i++) {
                    if (!Utility.isEmpty(selectedID[i])) {
                        DmsDocument document = docRetrievalManager.getDocument(TextUtility.parseIntegerObj(selectedID[i]));
                        String documentType = document.getDocumentType();
                        request.setAttribute("DocumentType", documentType);
                        Integer docID = document.getID();
                        request.setAttribute("docID", docID);
                        Integer parentID = document.getParentID();
                        request.setAttribute("parentID", parentID);
                        String directURL = documentForm.getDirectURL();
                        request.setAttribute("directURL", directURL);
                        Integer rootID = document.getRootID();
                        request.setAttribute("rootID", rootID);
                    }
                }
            }
        } catch (ApplicationException appEx) {
            throw appEx;
        } catch (Exception e) {
            log.error("Error in deletion", e);
            throw new ApplicationException(com.dcivision.framework.ErrorConstant.COMMON_FATAL_ERROR, e);
        } finally {
            conn = null;
            sessionContainer = null;
            docOperationManager.release();
            docRetrievalManager.release();
            docOperationManager = null;
            docRetrievalManager = null;
        }
    }

    public void checkOutMultiple(ActionMapping mapping, AbstractSearchForm form, HttpServletRequest request, HttpServletResponse response) throws ApplicationException {
        SessionContainer sessionContainer = this.getSessionContainer(request);
        Connection conn = this.getConnection(request);
        DocumentRetrievalManager docRetrievalManager = new DocumentRetrievalManager(sessionContainer, conn);
        String[] idAry = form.getBasicSelectedID();
        ListDmsDocumentForm documentForm = (ListDmsDocumentForm) form;
        DmsRootDAObject dmsRootDAObject = new DmsRootDAObject(sessionContainer, conn);
        try {
            for (int i = 0; i < idAry.length; i++) {
                DmsDocument dmsDocument = docRetrievalManager.getDocument(TextUtility.parseIntegerObj(idAry[i]));
                DmsRoot dmsRoot = (DmsRoot) dmsRootDAObject.getObjectByID((dmsDocument).getRootID());
                String dmsType = dmsRoot.getRootType();
                if (DmsDocument.DOCUMENT_TYPE.equals(dmsDocument.getDocumentType()) || DmsDocument.COMPOUND_DOC_TYPE.equals(dmsDocument.getDocumentType()) || DmsDocument.EMAIL_DOC_TYPE.equals(dmsDocument.getDocumentType())) {
                    if (DmsRoot.PUBLIC_ROOT.equals(dmsType)) {
                        if (TextUtility.parseInteger(SystemParameterFactory.getSystemParameter(SystemParameterConstant.DMS_CHECKOUT_EXPIRE_DAY)) > 0) {
                            java.util.Date triggerTime = null;
                            org.quartz.SimpleTrigger trigger = null;
                            int val = 0;
                            java.sql.Timestamp src = null;
                            val = TextUtility.parseInteger(SystemParameterFactory.getSystemParameter(SystemParameterConstant.DMS_CHECKOUT_EXPIRE_DAY));
                            src = Utility.getCurrentTimestamp();
                            triggerTime = Utility.addDay(src, val);
                            Scheduler sched = SchedulerFactory.getScheduler();
                            JobDataMap dataMap = new JobDataMap();
                            dataMap.put("documentID", dmsDocument.getID().toString());
                            dataMap.put("userID", TextUtility.formatIntegerObj(sessionContainer.getUserRecordID()));
                            org.quartz.JobDetail job = new org.quartz.JobDetail("checkout expiry time " + new java.util.Date(), "CHECKOUT_JOBGROUP" + Math.random(), com.dcivision.dms.core.DmsCheckoutExpiryTime.class);
                            job.setJobDataMap(dataMap);
                            log.info("******** Schedule date: " + triggerTime.toString());
                            trigger = new org.quartz.SimpleTrigger("CheckInExpirt_" + dmsDocument.getID(), "CHECKIN_GROUP" + Math.random(), triggerTime, null, 0, 0);
                            trigger.setMisfireInstruction(org.quartz.SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
                            log.info("******** Trigger created");
                            log.info(job.getFullName() + " will run at: " + trigger.getNextFireTime() + " & repeat: " + trigger.getRepeatCount() + "/" + trigger.getRepeatInterval());
                            sched.scheduleJob(job, trigger);
                            sched = null;
                        }
                        VersionControlManager versionControlManager = new VersionControlManager(sessionContainer, conn);
                        versionControlManager.checkoutDocument(DmsVersion.EXCLUSIVE_LOCK, dmsDocument);
                        versionControlManager.release();
                    }
                }
            }
            documentForm.setNavMode(GlobalConstant.NAV_MODE_VIEW);
        } catch (Exception e) {
            log.error(e, e);
            throw new ApplicationException(com.dcivision.framework.ErrorConstant.COMMON_FATAL_ERROR, e);
        } finally {
        }
    }

    /**
   * resolve the problem duplicate name
   * EIP-438 06/12/29 LEE
   * @param request
   */
    public void regeditDuplicateNameCache(HttpServletRequest request) {
        ApplicationContainer application = this.getSessionContainer(request).getAppContainer();
        if (application != null) {
            application.regeditDuplicateNameCache();
        }
    }

    /**
   * resolve the problem duplicate name
   * EIP-438 06/12/29 LEE
   * @param request
   */
    public void unlockDuplicateNameCache(HttpServletRequest request) {
        ApplicationContainer application = this.getSessionContainer(request).getAppContainer();
        if (application != null) {
            application.unlockDuplicateNameCache();
        }
    }

    public void removeInactiveDocumentIDsAtClipboardSessionAttr(HttpServletRequest request) throws ServletException {
        ArrayList clipboardList = (ArrayList) request.getSession().getAttribute(CLIPBOARD_SESSION_ATTR_NAME);
        DmsDocumentDAObject dmsDocumentDAO = new DmsDocumentDAObject(this.getSessionContainer(request), this.getConnection(request));
        if (Utility.isEmpty(clipboardList)) {
            for (int i = 0; i < clipboardList.size(); i++) {
                if (Utility.isEmpty(dmsDocumentDAO.getObjectByID((Integer) clipboardList.get(i)))) {
                    clipboardList.remove(clipboardList.get(i));
                }
            }
        }
        request.getSession().setAttribute(CLIPBOARD_SESSION_ATTR_NAME, clipboardList);
    }

    /**
   * Remove not active documentID from searchForm BasicSelectedID property
   * 
   * @param mapping
   * @param form
   * @param request
   * @param response
   * @throws ServletException
   */
    public void removeInactiveDocumentsAtSearchForm(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws ServletException {
        ListDmsDocumentForm searchForm = (ListDmsDocumentForm) form;
        ArrayList clipboardList = (ArrayList) request.getSession().getAttribute(CLIPBOARD_SESSION_ATTR_NAME);
        String[] documentIDs = searchForm.getBasicSelectedID();
        ArrayList tempDocumentIDs = new ArrayList();
        if (!Utility.isEmpty(documentIDs)) {
            for (int i = 0; i < documentIDs.length; i++) {
                Integer documentID = TextUtility.parseIntegerObj(documentIDs[i]);
                if (clipboardList.contains(documentID)) {
                    tempDocumentIDs.add(documentID);
                } else {
                    addMessage(request, "");
                }
            }
        }
        String[] filtretedDocumentIDs = null;
        tempDocumentIDs.toArray(filtretedDocumentIDs);
        searchForm.setBasicSelectedID(filtretedDocumentIDs);
    }
}
