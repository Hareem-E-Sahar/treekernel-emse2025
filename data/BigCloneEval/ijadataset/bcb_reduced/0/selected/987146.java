package net.sf.wgfa.struts.action.admin;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.wgfa.struts.form.admin.ExportOntologyForm;
import net.sf.wgfa.util.ModelCache;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 *
 * @author tobias
 */
public class ExportOntologyAction extends AdminAction {

    protected ActionForward doAdmin(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ExportOntologyForm eof = (ExportOntologyForm) form;
        String filename;
        try {
            URL oUrl = new URL(eof.getOntologyURI());
            filename = oUrl.getFile();
        } catch (MalformedURLException mue) {
            filename = eof.getOntologyURI();
        }
        if (eof.getZip() == null || eof.getZip().equals("none")) {
            response.setContentType("application/xml");
            if (eof.getSaveToDisk()) {
                response.setHeader("Content-Disposition", "attachment; filename=" + eof.getOntologyURI() + ".owl");
            }
            response.getWriter().write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
            ModelCache.getSingleton().exportOntology(eof.getOntologyURI(), response.getWriter());
        } else if (eof.getZip().equals("zip") || eof.getZip().equals("gzip")) {
            OutputStream compressedOut;
            if (eof.getZip().equals("zip")) {
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment; filename=" + eof.getOntologyURI() + ".zip");
                ZipOutputStream zipout = new ZipOutputStream(response.getOutputStream());
                zipout.putNextEntry(new ZipEntry(filename));
                compressedOut = zipout;
            } else {
                response.setContentType("application/gzip");
                response.setHeader("Content-Disposition", "attachment; filename=" + eof.getOntologyURI() + ".gz");
                compressedOut = new GZIPOutputStream(response.getOutputStream());
            }
            OutputStreamWriter osw = new OutputStreamWriter(compressedOut);
            osw.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
            ModelCache.getSingleton().exportOntology(eof.getOntologyURI(), osw);
            osw.close();
        }
        return null;
    }
}
