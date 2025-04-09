package com.sitescape.team.portlet.administration;

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
import org.springframework.util.FileCopyUtils;
import org.springframework.web.portlet.ModelAndView;
import com.sitescape.team.domain.Definition;
import com.sitescape.team.util.NLT;
import com.sitescape.team.util.SpringContextUtil;
import com.sitescape.team.util.TempFileUtil;
import com.sitescape.team.util.XmlFileUtil;
import com.sitescape.team.web.WebKeys;
import com.sitescape.team.web.portlet.SAbstractController;
import com.sitescape.team.web.tree.DomTreeBuilder;
import com.sitescape.team.web.util.PortletRequestUtils;
import com.sitescape.team.web.util.WebUrlUtil;
import com.sitescape.util.FileUtil;
import com.sitescape.util.Validator;

public class LogFileController extends SAbstractController {

    public void handleActionRequestAfterValidation(ActionRequest request, ActionResponse response) throws Exception {
        Map formData = request.getParameterMap();
        response.setRenderParameters(formData);
    }

    public ModelAndView handleRenderRequestInternal(RenderRequest request, RenderResponse response) throws Exception {
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
