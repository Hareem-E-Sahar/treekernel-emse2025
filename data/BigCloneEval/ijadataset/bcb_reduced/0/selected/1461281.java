package org.kablink.teaming.servlet.administration;

import java.util.Iterator;
import java.util.Map;
import javax.activation.FileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.dom4j.Document;
import org.kablink.teaming.util.XmlFileUtil;
import org.kablink.teaming.web.servlet.SAbstractController;
import org.kablink.util.Validator;
import org.springframework.web.servlet.ModelAndView;

public abstract class ZipDownloadController extends SAbstractController {

    protected abstract String getFilename();

    protected abstract NamedDocument getDocumentForId(String defId);

    private FileTypeMap mimeTypes;

    protected FileTypeMap getFileTypeMap() {
        return mimeTypes;
    }

    public void setFileTypeMap(FileTypeMap mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    protected ModelAndView handleRequestAfterValidation(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String filename = getFilename();
        response.setContentType(mimeTypes.getContentType(filename));
        response.setHeader("Cache-Control", "private");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
        String encoding = "UTF-8";
        zipOut.setEncoding(encoding);
        Map formData = request.getParameterMap();
        Iterator itFormData = formData.entrySet().iterator();
        while (itFormData.hasNext()) {
            Map.Entry me = (Map.Entry) itFormData.next();
            if (((String) me.getKey()).startsWith("id_")) {
                String defId = ((String) me.getKey()).substring(3);
                if (defId.startsWith("%")) defId = defId.substring(1);
                if (Validator.isNotNull(defId)) {
                    try {
                        NamedDocument doc = getDocumentForId(defId);
                        zipOut.putNextEntry(new ZipEntry(Validator.replacePathCharacters(doc.name) + ".xml"));
                        XmlFileUtil.writeFile(doc.doc, zipOut);
                    } catch (Exception ex) {
                    }
                }
            }
        }
        zipOut.finish();
        return null;
    }

    protected class NamedDocument {

        public String name;

        public Document doc;

        public NamedDocument(String name, Document doc) {
            this.name = name;
            this.doc = doc;
        }
    }
}
