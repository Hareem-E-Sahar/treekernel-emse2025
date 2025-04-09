package com.juanfrivaldes.cio2005.gestion;

import java.io.*;
import java.util.zip.*;
import java.util.List;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import com.juanfrivaldes.cio2005.domain.Ponencia;

/**
 * @author root
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class CrearZipAction extends GestionAction {

    private static Log log = LogFactory.getLog(CrearZipAction.class);

    private static final int BUFFER_SIZE = 8192;

    private static final String resumenesPath = "/cio2005-resumenes/";

    private static final String finalesPath = "/cio2005-ponencias/";

    private static final String showZipPath = "/";

    private String filePath;

    private void rellenarForm(CrearZipForm zipForm, List ponencias) {
        String[] ids = new String[ponencias.size()];
        for (int i = 0; i < ponencias.size(); i++) {
            ids[i] = new Integer(((Ponencia) ponencias.get(i)).getId()).toString();
        }
        zipForm.setSeleccionados(ids);
    }

    private String comprimirPonencias(String[] ponencias) throws IOException {
        BufferedInputStream origin = null;
        Random random = new Random();
        String shortFileName = "ponencias_" + random.nextInt(10000) + ".zip";
        String zipRelativeName = showZipPath + shortFileName;
        String zipName = getServlet().getServletContext().getRealPath(zipRelativeName);
        FileOutputStream dest = new FileOutputStream(zipName);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        byte[] data = new byte[BUFFER_SIZE];
        for (int i = 0; i < ponencias.length; i++) {
            String fileName = filePath + ponencias[i] + ".pdf";
            FileInputStream fi = new FileInputStream(fileName);
            origin = new BufferedInputStream(fi, BUFFER_SIZE);
            ZipEntry entry = new ZipEntry(ponencias[i] + ".pdf");
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
        }
        out.close();
        return shortFileName;
    }

    protected ActionForward protectedExecute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        CrearZipForm zipForm = (CrearZipForm) form;
        if (request.getParameter("action") != null) {
            if (request.getParameter("action").equals("No seleccionar ninguna ponencia")) {
                log.trace("limpiados las ponencias marcadas");
                zipForm.setSeleccionados(null);
            } else if (request.getParameter("action").equals("Seleccionar todas las ponencias")) {
                log.trace("seleccionadas todas las ponencias");
                if (zipForm.getTipo().equals("final")) {
                    rellenarForm(zipForm, (List) request.getSession().getAttribute("finales"));
                } else {
                    rellenarForm(zipForm, (List) request.getSession().getAttribute("ponencias"));
                }
            } else if (request.getParameter("action").equals("Crear ZIP con ponencias marcadas")) {
                log.trace("vamos a crear un zip");
                if (zipForm.getSeleccionados() != null && zipForm.getSeleccionados().length > 0) {
                    if (zipForm.getTipo().equals("final")) {
                        filePath = finalesPath;
                    } else {
                        filePath = resumenesPath;
                    }
                    String destino = this.comprimirPonencias(zipForm.getSeleccionados());
                    ActionForward forward = new ActionForward();
                    forward.setRedirect(true);
                    forward.setPath(destino);
                    return forward;
                }
            }
        }
        return mapping.findForward(zipForm.getTipo());
    }
}
