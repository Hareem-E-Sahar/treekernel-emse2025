package gleam.executive.webapp.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.springframework.util.StringUtils;
import gleam.executive.webapp.action.BaseAction;
import gleam.executive.Constants;
import gleam.executive.model.Corpus;
import gleam.executive.model.Document;
import gleam.executive.model.WebAppBean;
import gleam.executive.service.DocServiceManager;
import gleam.executive.service.ResourceManager;
import gleam.executive.webapp.form.CorpusForm;
import gleam.executive.webapp.wrapp.ProcessInstanceWrapper;
import gleam.executive.workflow.manager.WorkflowManager;

/**
 * Implementation of <strong>Action</strong> that interacts with the
 * {@link CorpusForm} and retrieves values. It interacts with the
 * {@link DocServiceManager} to retrieve/persist values to the datastore.
 * 
 * @struts.action name="corpusForm" path="/corpora" scope="request"
 *                validate="false" parameter="method" input="mainMenu"
 * @struts.action name="corpusForm" path="/addCorpus" scope="request"
 *                validate="false" parameter="method" input="list"
 * @struts.action name="corpusForm" path="/saveCorpus" scope="request"
 *                validate="false" parameter="method" input="edit"
 * @struts.action path="/corpus" scope="request"
 *                validate="false" parameter="method" input="list"
 * @struts.action path="/datastore" scope="request"
 *                validate="false" parameter="method" input="list"
 * @struts.action-forward name="edit" path="/WEB-INF/pages/corpusForm.jsp"
 * @struts.action-forward name="list" path="/WEB-INF/pages/corpusList.jsp"
 * @struts.action-forward name="view"
 *                        path="/documentsInCorpus.html?method=search"
 * 
 */
public final class CorpusAction extends BaseAction {

    /**
	 * ActionForward that is invoked in addCorpus.html(corpusForm.jsp),
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
    public ActionForward cancel(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return mapping.findForward("viewCorpora");
    }

    /**
	 * ActionForward that is invoked in addCorpus.html(corpusForm.jsp), which is
	 * used to prepare for the creation of a corpus
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
    public ActionForward add(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Entering 'add corpus' method");
        }
        Corpus corpus = new Corpus();
        CorpusForm corpusForm = (CorpusForm) convert(corpus);
        updateFormBean(mapping, request, corpusForm);
        return mapping.findForward("edit");
    }

    public ActionForward edit(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Entering 'edit Corpus' method");
        }
        return mapping.findForward("edit");
    }

    /**
	 * ActionForward that is invoked from corpusList.jsp(corpora.html), which
	 * calls docSercie to delete a corpus from the datastore
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
    public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Entering 'delete corpus' method");
        }
        ActionMessages messages = new ActionMessages();
        CorpusForm corpusForm = (CorpusForm) form;
        DocServiceManager mgr = (DocServiceManager) getBean("docServiceManager");
        mgr.deleteCorpus(corpusForm.getCorpusID());
        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("corpus.deleted"));
        saveMessages(request.getSession(), messages);
        return mapping.findForward("viewCorpora");
    }

    /**
	 * ActionForward that is invoked from createCorpus.html, which calls
	 * docServiceManager to create a corpus and save it in the datastore.
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Entering 'save corpus' method");
        }
        ActionMessages messages = new ActionMessages();
        CorpusForm corpusForm = (CorpusForm) form;
        DocServiceManager mgr = (DocServiceManager) getBean("docServiceManager");
        String corpusID = request.getParameter("corpusID");
        String corpusName = corpusForm.getCorpusName();
        if (corpusID.equals("")) {
            corpusID = mgr.createCorpus(corpusName);
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("corpus.created"));
            saveMessages(request.getSession(), messages);
        } else {
            mgr.setCorpusName(corpusID, corpusName);
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("corpus.updated"));
            saveMessages(request.getSession(), messages);
        }
        request.setAttribute("corpusID", corpusID);
        request.setAttribute("corpusName", corpusName);
        return mapping.findForward("view");
    }

    /**
	 * ActionForward that is invoked from corpusList.jsp(corpora.html), which
	 * calls docServiceManager to list all the corpora in the datastore
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
    public ActionForward search(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Entering 'corpus search' method");
        }
        DocServiceManager mgr = (DocServiceManager) getBean("docServiceManager");
        request.setAttribute(Constants.CORPUS_LIST, mgr.listCorpora());
        request.setAttribute(Constants.docServiceURL, mgr.getDocServiceURL());
        String userName = request.getRemoteUser();
        if (log.isDebugEnabled()) {
            log.debug("Entering 'listByUserId' method - user: " + userName);
        }
        WorkflowManager workflowManager = (WorkflowManager) getBean("workflowManager");
        WebAppBean webAppBean = (WebAppBean) getBean("webAppBean");
        List processInstances = workflowManager.findAllProcessInstancesByProcessDefinitionNameExcludingSubProcessInstances(Constants.MAIN_PROCESS_DEFINITION_NAME);
        Map<String, List<ProcessInstanceWrapper>> corpusProcessInstanceWrapperMap = new HashMap<String, List<ProcessInstanceWrapper>>();
        Iterator it = processInstances.iterator();
        while (it.hasNext()) {
            ProcessInstance processInstance = (ProcessInstance) it.next();
            ProcessInstanceWrapper piw = new ProcessInstanceWrapper(processInstance);
            String corpusId = piw.getCorpusId();
            log.debug("corpusId: " + corpusId);
            List<ProcessInstanceWrapper> list = corpusProcessInstanceWrapperMap.get(corpusId);
            if (list == null) {
                list = new ArrayList<ProcessInstanceWrapper>();
            }
            list.add(piw);
            corpusProcessInstanceWrapperMap.put(corpusId, list);
        }
        request.setAttribute("corpusProcessMap", corpusProcessInstanceWrapperMap);
        ResourceManager resourceManager = (ResourceManager) getBean("resourceManager");
        List processInstancesUsingRoles = resourceManager.getRolesWithResource(Constants.PROCESS_INSTANCE_PATTERN);
        String processInstancesUsingRolesString = StringUtils.collectionToCommaDelimitedString(processInstancesUsingRoles);
        log.debug("processInstancesUsingRoles " + processInstancesUsingRoles);
        request.setAttribute("processInstancesUsingRoles", processInstancesUsingRolesString);
        return mapping.findForward("list");
    }

    public ActionForward unspecified(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return search(mapping, form, request, response);
    }

    /**
	 * ActionForward that is invoked from corpusList.jsp(corpora.html), which
	 * calls docServiceManager to load all the docs in a given corpus and zip
	 * them for downloading.
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
    public ActionForward downloadCorpus(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Entering 'corpus download' method");
        }
        DocServiceManager mgr = (DocServiceManager) getBean("docServiceManager");
        String corpusID = request.getParameter("corpusID");
        String corpusName = mgr.getCorpusName(corpusID);
        List<Document> docsInCorpus = mgr.listDocuments(corpusID);
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + corpusName + ".zip\"");
        ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
        try {
            CRC32 crc = new CRC32();
            for (Document doc : docsInCorpus) {
                String fileName = doc.getDocumentName();
                if (!fileName.endsWith(".xml")) {
                    fileName += ".xml";
                }
                byte[] docData = mgr.getDocXML(doc.getDocumentID());
                ZipEntry entry = new ZipEntry(fileName);
                entry.setSize(docData.length);
                crc.reset();
                crc.update(docData);
                entry.setCrc(crc.getValue());
                zipOut.putNextEntry(entry);
                try {
                    zipOut.write(docData);
                } finally {
                    zipOut.closeEntry();
                }
            }
        } finally {
            zipOut.close();
        }
        return null;
    }

    /**
	 * ActionForward that is invoked from corpusList.jsp(corpora.html), which
	 * calls docServiceManager to load all the docs in the datastore and zip
	 * them for downloading.
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
    public ActionForward downloadDS(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Entering 'datastore download' method");
        }
        DocServiceManager mgr = (DocServiceManager) getBean("docServiceManager");
        List<Document> docsInDS = mgr.listDocuments();
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + "safe-ds.zip\"");
        ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
        try {
            CRC32 crc = new CRC32();
            for (Document doc : docsInDS) {
                String fileName = doc.getDocumentID();
                if (!fileName.endsWith(".xml")) {
                    fileName += ".xml";
                }
                byte[] docData = mgr.getDocXML(doc.getDocumentID());
                ZipEntry entry = new ZipEntry(fileName);
                entry.setSize(docData.length);
                crc.reset();
                crc.update(docData);
                entry.setCrc(crc.getValue());
                zipOut.putNextEntry(entry);
                try {
                    zipOut.write(docData);
                } finally {
                    zipOut.closeEntry();
                }
            }
        } finally {
            zipOut.close();
        }
        return null;
    }

    public ActionForward deleteMultipleCorpora(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Entering 'delete multiple corpora' method");
        }
        ActionMessages messages = new ActionMessages();
        DocServiceManager mgr = (DocServiceManager) getBean("docServiceManager");
        String toDelete = request.getParameter("toDelete");
        String[] corporaIds = request.getParameterValues("rowId");
        if (corporaIds == null) {
            ActionMessages errors = new ActionMessages();
            errors.add("errors.detail", new ActionMessage("corpus.noCorpusSelected"));
            saveErrors(request, errors);
            return dispatchMethod(mapping, form, request, response, "search");
        }
        if (toDelete != null) {
            for (String corpusId : corporaIds) {
                mgr.deleteCorpus(corpusId);
            }
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("corpora.deleted"));
        }
        saveMessages(request.getSession(), messages);
        return dispatchMethod(mapping, form, request, response, "search");
    }
}
