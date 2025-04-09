package de.lotk.webftp.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import de.lotk.webftp.Constants;
import de.lotk.webftp.bean.SessionContainer;
import de.lotk.webftp.business.FtpClientConnection;
import de.lotk.webftp.form.DirectoryListingForm;

/**
 * Action, die etwas mit einer Auswahl von Dateien tut.
 * 
 * @author Stephan Sann
 * @version 1.0
 */
public final class MultiFileAction extends BaseFtpAction {

    /**
   * Zipt die selektierten Files in einen OutputStream. Der OutputStream wird
   * von dieser Methode NICHT geschlossen.
   * 
   * @param    selectedFiles     Die gewaehlen Dateien
   * @param    os                Der Ziel-OutputStream
   * @param    sessionContainer  Der aktuelle SessionContainer
   * @return                     <code>true</code>, wenn alles glatt lief;
   *                             <code>false</code>, bei Misserfolg.
   */
    private boolean zipSelectedFilesToOutputStream(String[] selectedFiles, OutputStream os, SessionContainer sessionContainer) throws Exception {
        boolean erfolg = true;
        byte[] buf = new byte[4096];
        int len = 0;
        long now = (new Date()).getTime();
        ByteArrayInputStream bais = null;
        FtpClientConnection ftpClientConnection = sessionContainer.getFtpClientConnection();
        ftpClientConnection.verifyConnection(sessionContainer.getLoginData());
        ZipOutputStream zos = new ZipOutputStream(os);
        zos.setMethod(ZipOutputStream.DEFLATED);
        zos.setLevel(Deflater.BEST_COMPRESSION);
        for (int ww = 0; ww < selectedFiles.length; ww++) {
            String actRemoteFile = this.assembleRemoteFileName(sessionContainer, selectedFiles[ww]);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            erfolg = ftpClientConnection.retrieveFile(actRemoteFile, baos);
            baos.close();
            byte[] fileContent = baos.toByteArray();
            if (!erfolg) {
                return (false);
            }
            CRC32 crc32 = new CRC32();
            bais = new ByteArrayInputStream(fileContent);
            while ((len = bais.read(buf)) > 0) {
                crc32.update(buf, 0, len);
            }
            bais.close();
            ZipEntry zipEntry = new ZipEntry(selectedFiles[ww]);
            zipEntry.setSize(fileContent.length);
            zipEntry.setTime(now);
            zipEntry.setCrc(crc32.getValue());
            zos.putNextEntry(zipEntry);
            bais = new ByteArrayInputStream(fileContent);
            while ((len = bais.read(buf)) > 0) {
                zos.write(buf, 0, len);
            }
            bais.close();
            zos.closeEntry();
        }
        zos.finish();
        return (erfolg);
    }

    /**
   * Loescht die selektierten Files auf dem FTP-Server
   * 
   * @param    selectedFiles     Die gewaehlen Dateien
   * @param    sessionContainer  Der aktuelle SessionContainer
   * @return                     <code>true</code>, wenn alles glatt lief;
   *                             <code>false</code>, bei Misserfolg.
   */
    private boolean deletSelectedFiles(String[] selectedFiles, SessionContainer sessionContainer) throws Exception {
        for (int qq = 0; qq < selectedFiles.length; qq++) {
            selectedFiles[qq] = this.assembleRemoteFileName(sessionContainer, selectedFiles[qq]);
        }
        FtpClientConnection ftpClientConnection = sessionContainer.getFtpClientConnection();
        ftpClientConnection.verifyConnection(sessionContainer.getLoginData());
        return (ftpClientConnection.deleteMultipleFiles(selectedFiles));
    }

    /**
   * Process the specified HTTP request, and create the corresponding HTTP
   * response (or forward to another web component that will create it).
   * Return an <code>ActionForward</code> instance describing where and how
   * control should be forwarded, or <code>null</code> if the response has
   * already been completed.
   *
   * @param mapping The ActionMapping used to select this instance
   * @param form The optional ActionForm bean for this request (if any)
   * @param request The HTTP request we are processing
   * @param response The HTTP response we are creating
   *
   * @exception Exception if business logic throws an exception
   */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
        SessionContainer sessionContainer = (SessionContainer) session.getAttribute(Constants.ATTRIB_SESSION_CONTAINER);
        DirectoryListingForm theForm = (DirectoryListingForm) form;
        String[] selectedFiles = theForm.getSelectedFiles();
        if (theForm.isButtonDownloadZipClicked() && (selectedFiles.length >= 1)) {
            String contentDisposition = "attachment; filename=\"yawebftp.zip\"";
            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition", contentDisposition);
            try {
                boolean erfolg = this.zipSelectedFilesToOutputStream(selectedFiles, response.getOutputStream(), sessionContainer);
                if (!erfolg) {
                    throw (new Exception("Download-Archiv konnte nicht erstellt werden."));
                }
            } catch (Exception e) {
                e.printStackTrace();
                ActionErrors errors = new ActionErrors();
                errors.add(ActionErrors.GLOBAL_ERROR, (new ActionError("error.download.asZip.failed")));
                saveErrors(request, errors);
                return (mapping.getInputForward());
            }
            return (null);
        } else if (theForm.isButtonDeleteClicked() && (selectedFiles.length >= 1)) {
            try {
                boolean erfolg = this.deletSelectedFiles(selectedFiles, sessionContainer);
                if (!erfolg) {
                    throw (new Exception("Gewaehlte Files wurden nicht geloescht."));
                }
            } catch (Exception e) {
                e.printStackTrace();
                ActionErrors errors = new ActionErrors();
                errors.add(ActionErrors.GLOBAL_ERROR, (new ActionError("error.delete.files.failed")));
                saveErrors(request, errors);
                return (mapping.getInputForward());
            }
        }
        return (mapping.findForward("directory-listing"));
    }
}
