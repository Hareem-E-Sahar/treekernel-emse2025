package org.tapestrycomponents.tassel.dlservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.tapestry.ApplicationRuntimeException;
import org.apache.tapestry.IComponent;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.engine.AbstractService;
import org.apache.tapestry.engine.IEngineServiceView;
import org.apache.tapestry.engine.ILink;
import org.apache.tapestry.request.ResponseOutputStream;
import org.objectstyle.cayenne.exp.ExpressionFactory;
import org.objectstyle.cayenne.query.SelectQuery;
import org.tapestrycomponents.tassel.Visit;
import org.tapestrycomponents.tassel.domain.Component;
import org.tapestrycomponents.tassel.domain.ComponentDL;
import org.tapestrycomponents.tassel.domain.ComponentElement;
import org.tapestrycomponents.tassel.domain.User;
import org.tapestrycomponents.tassel.utilities.SimpleMailAPI;

public class TasselComponentDownloadService extends AbstractService {

    private static final String ADMIN_LOGIN = "robertz";

    private static Logger log = Logger.getLogger(TasselComponentDownloadService.class);

    public static final String SERVICE_NAME = "tasseldls.componentdl";

    public String getName() {
        return SERVICE_NAME;
    }

    public void service(IEngineServiceView engineServiceView, IRequestCycle cycle, ResponseOutputStream outputStream) throws ServletException, IOException {
        log.setLevel(Level.DEBUG);
        Object[] parameters = getParameters(cycle);
        if (parameters == null || parameters.length < 1) {
            throw new ApplicationRuntimeException("Invalid Link Specification");
        }
        String componentid = (String) parameters[0];
        Visit v = (Visit) cycle.getEngine().getVisit(cycle);
        SelectQuery sq = new SelectQuery(Component.class);
        sq.setQualifier(ExpressionFactory.matchExp("id", componentid));
        List results = v.getDataContext().performQuery(sq);
        if (results.size() < 1) {
            throw new ApplicationRuntimeException("Invalid Component");
        }
        Component comp = (Component) results.get(0);
        List elements = comp.getComponentElements();
        if (elements == null) {
            throw new ApplicationRuntimeException("Invalid component dl: no files!");
        }
        ComponentDownload dl = createElementsFile(elements, cycle, comp.getName());
        HttpServletResponse response = cycle.getRequestContext().getResponse();
        byte[] fileBuffer;
        FileInputStream is = new FileInputStream(dl.getFile());
        fileBuffer = new byte[is.available()];
        is.read(fileBuffer);
        response.setHeader("Content-Disposition", "attachment;filename=" + dl.getFileName());
        outputStream.setContentType(dl.getContentType());
        outputStream.write(fileBuffer);
        recordTransaction(comp, v);
    }

    private void recordTransaction(Component comp, Visit v) {
        ComponentDL compdl = (ComponentDL) v.getDataContext().createAndRegisterNewObject(ComponentDL.class);
        if (v.getUser() != null) {
            compdl.setDownloadingUser(v.getUser());
        } else {
            SelectQuery sq = new SelectQuery(User.class);
            sq.setQualifier(ExpressionFactory.matchExp("login", ADMIN_LOGIN));
            List results = v.getDataContext().performQuery(sq);
            if (results.size() > 0) {
                compdl.setDownloadingUser((User) results.get(0));
            }
        }
        compdl.setDownloadedComponent(comp);
        compdl.setDldate(new Date());
        if (comp.getEmailSubmitterOnDownload().booleanValue() && (comp.getComponentSubmitter().getEmail() != null)) {
            SimpleMailAPI api;
            try {
                SimpleMailAPI.sendMail("Tassel Component Download Notification", comp.getComponentSubmitter().getEmail(), "This e-mail is to notify you that a component submitted by you has been downloaded. Component: " + comp.getName() + " was downloaded.");
            } catch (Exception e) {
                log.setLevel(Level.DEBUG);
                log.debug("caught exception: " + e.toString());
                log.debug(e);
            }
        }
        v.getDataContext().commitChanges();
    }

    public ILink getLink(IRequestCycle cycle, IComponent component, Object[] parameters) {
        return constructLink(cycle, SERVICE_NAME, null, parameters, true);
    }

    private ComponentDownload createElementsFile(List elements, IRequestCycle cycle, String name) throws IOException {
        byte[] buffer = new byte[1024];
        String basePath = cycle.getRequestContext().getServlet().getServletContext().getRealPath("/");
        if (elements.size() == 1) {
            ComponentElement ce = (ComponentElement) elements.get(0);
            ce.setBasePath(basePath);
            File retFile = null;
            try {
                retFile = ce.getElementFile();
            } catch (Exception e) {
                throw new ApplicationRuntimeException("Baaad. Bad. ce.getElementFile threw an exception =( " + e.toString());
            }
            return new ComponentDownload(retFile, ce.getFileName(), ce.getContentType());
        }
        File f = File.createTempFile(name, "zip", null);
        f.deleteOnExit();
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
        Iterator i = elements.iterator();
        while (i.hasNext()) {
            ComponentElement ce = (ComponentElement) i.next();
            FileInputStream in = new FileInputStream(basePath + ce.getPath());
            out.putNextEntry(new ZipEntry(ce.getFileName()));
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.closeEntry();
            in.close();
        }
        out.close();
        return new ComponentDownload(f, name + ".zip", "application/zip");
    }

    private File getUniqueZip(String basePath, String compName) {
        String path = basePath + "/WEB-INF/" + compName;
        Random r = new Random();
        path += r.nextInt() + ".zip";
        File f = new File(path);
        if (f.exists()) {
            return getUniqueZip(basePath, compName);
        }
        return f;
    }
}

class ComponentDownload {

    private File file;

    private String filename;

    private String contentType;

    public ComponentDownload(File f, String name, String content) {
        contentType = content;
        file = f;
        filename = name;
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }
}
