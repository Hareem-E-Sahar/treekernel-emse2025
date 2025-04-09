package org.kablink.teaming.portlet.administration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.FilterOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.kablink.teaming.domain.Definition;
import org.kablink.teaming.util.NLT;
import org.kablink.teaming.util.SpringContextUtil;
import org.kablink.teaming.util.TempFileUtil;
import org.kablink.teaming.util.XmlFileUtil;
import org.kablink.teaming.web.WebKeys;
import org.kablink.teaming.web.portlet.SAbstractController;
import org.kablink.teaming.web.tree.DomTreeBuilder;
import org.kablink.teaming.web.util.PortletRequestUtils;
import org.kablink.teaming.web.util.WebUrlUtil;
import org.kablink.util.FileUtil;
import org.kablink.util.Validator;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.portlet.ModelAndView;

public class LogFileController extends SAbstractController {

    public void handleActionRequestAfterValidation(ActionRequest request, ActionResponse response) throws Exception {
        Map formData = request.getParameterMap();
        response.setRenderParameters(formData);
    }

    public ModelAndView handleRenderRequestAfterValidation(RenderRequest request, RenderResponse response) throws Exception {
        Map model = new HashMap();
        File tempFile = TempFileUtil.createTempFile("logfiles");
        FileOutputStream fo = new FileOutputStream(tempFile);
        ZipOutputStream zipOut = new ZipOutputStream(fo);
        FilterOutputStream wrapper = new FilterOutputStream(zipOut) {

            public void close() {
            }
        };
        File logDirectory = new File(SpringContextUtil.getServletContext().getRealPath("/WEB-INF/logs"));
        for (String logFile : logDirectory.list(new FilenameFilter() {

            public boolean accept(File file, String filename) {
                return filename.startsWith("ssf.log");
            }
        })) {
            zipOut.putNextEntry(new ZipEntry(logFile));
            FileCopyUtils.copy(new FileInputStream(new File(logDirectory, logFile)), wrapper);
        }
        zipOut.finish();
        model.put(WebKeys.DOWNLOAD_URL, WebUrlUtil.getServletRootURL(request) + WebKeys.SERVLET_VIEW_FILE + "?viewType=zipped&fileId=" + tempFile.getName() + "&" + WebKeys.URL_FILE_TITLE + "=logfiles.zip");
        return new ModelAndView("administration/close_button", model);
    }
}
