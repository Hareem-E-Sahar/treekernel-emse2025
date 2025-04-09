package org.itracker.web.actions.admin.attachment;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.itracker.model.IssueAttachment;
import org.itracker.services.IssueService;
import org.itracker.services.util.UserUtilities;
import org.itracker.web.actions.base.ItrackerBaseAction;

public class ExportAttachmentsAction extends ItrackerBaseAction {

    private static final Logger log = Logger.getLogger(DownloadAttachmentAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ActionMessages errors = new ActionMessages();
        if (!hasPermission(UserUtilities.PERMISSION_USER_ADMIN, request, response)) {
            return mapping.findForward("unauthorized");
        }
        try {
            IssueService issueService = getITrackerServices().getIssueService();
            List<IssueAttachment> attachments = issueService.getAllIssueAttachments();
            if (attachments.size() > 0) {
                response.setHeader("Content-Disposition", "attachment; filename=\"ITracker_attachments.zip\"");
                ServletOutputStream out = response.getOutputStream();
                ZipOutputStream zipOut = new ZipOutputStream(out);
                try {
                    for (int i = 0; i < attachments.size(); i++) {
                        log.debug("Attempting export for: " + attachments.get(i));
                        byte[] attachmentData = issueService.getIssueAttachmentData(attachments.get(i).getId());
                        if (attachmentData.length > 0) {
                            ZipEntry zipEntry = new ZipEntry(attachments.get(i).getFileName());
                            zipEntry.setSize(attachmentData.length);
                            zipEntry.setTime(attachments.get(i).getLastModifiedDate().getTime());
                            zipOut.putNextEntry(zipEntry);
                            zipOut.write(attachmentData, 0, attachmentData.length);
                            zipOut.closeEntry();
                        }
                    }
                    zipOut.close();
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    log.error("Exception while exporting attachments.", e);
                }
                return null;
            }
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("itracker.web.error.noattachments"));
        } catch (Exception e) {
            log.error("Exception while exporting attachments.", e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("itracker.web.error.system"));
        }
        if (!errors.isEmpty()) {
            saveErrors(request, errors);
        }
        return mapping.findForward("error");
    }
}
