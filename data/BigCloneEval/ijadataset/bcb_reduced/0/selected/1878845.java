package org.nodevision.portal.struts.portlets;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.lob.SerializableBlob;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.nodevision.portal.hibernate.om.NvUserportlets;
import org.nodevision.portal.om.portletdefinition.PortletApplicationDefinition;
import org.nodevision.portal.om.portletdefinition.PortletDefinition;
import org.nodevision.portal.om.portletregistry.RegisteredPortlet;
import org.nodevision.portal.om.portletregistry.WebApplications;
import org.nodevision.portal.om.portletregistry.Webapplication;
import org.nodevision.portal.repositories.RepositoryBasic;
import org.nodevision.portal.struts.portlets.forms.PortletsForm;
import org.nodevision.portal.utils.Constants;
import org.nodevision.portal.utils.HibernateUtil;
import org.nodevision.portal.utils.NoOpEntityResolver;
import org.nodevision.portal.utils.UserPortletsHolder;
import org.xml.sax.InputSource;

public class DisplayPortlets extends Action {

    private static final String dirDelim = System.getProperty("file.separator");

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PortletsForm portletsform = (PortletsForm) form;
        if ("delete".equalsIgnoreCase(request.getParameter("action"))) {
            ActionErrors errors = new ActionErrors();
            try {
                String webappid = request.getParameter("webapp");
                RepositoryBasic.getInstance(getServlet().getServletContext());
                Webapplication deleteWebapp = RepositoryBasic.getWebapplications().getWebapplication(webappid);
                WebApplications apps = RepositoryBasic.getWebapplications();
                ArrayList temp = apps.getWebApplications();
                for (int i = 0; i < temp.size(); i++) {
                    Webapplication tempApp = (Webapplication) temp.get(i);
                    if (tempApp.getIdWebapplication().equalsIgnoreCase(deleteWebapp.getIdWebapplication())) {
                        temp.remove(i);
                    }
                }
                apps.setWebApplications(temp);
                RepositoryBasic.getInstance(getServlet().getServletContext()).savePortlets(apps);
                Session hbsession = HibernateUtil.currentSession();
                Transaction tx = hbsession.beginTransaction();
                Query query = hbsession.createQuery("delete from org.nodevision.portal.hibernate.om.NvPreferences where webapp_id =:webappid");
                query.setString("webappid", webappid);
                query = hbsession.createQuery("from org.nodevision.portal.hibernate.om.NvUserportlets");
                Iterator it = query.iterate();
                while (it.hasNext()) {
                    NvUserportlets up = (NvUserportlets) it.next();
                    SerializableBlob blob = (SerializableBlob) up.getPortletsList();
                    if (blob.length() > 0) {
                        DataInputStream in = new DataInputStream(blob.getBinaryStream());
                        ByteArrayOutputStream bout = new ByteArrayOutputStream();
                        int c;
                        while ((c = in.read()) != -1) {
                            bout.write(c);
                        }
                        final ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
                        final ObjectInputStream oin = new ObjectInputStream(bin);
                        ArrayList portlets = (ArrayList) oin.readObject();
                        boolean changed = false;
                        for (int z = 0; z < portlets.size(); z++) {
                            UserPortletsHolder tempPortlet = (UserPortletsHolder) portlets.get(z);
                            if (tempPortlet.getWebapplication().equalsIgnoreCase(webappid)) {
                                portlets.remove(z);
                                changed = true;
                            }
                        }
                        if (changed) {
                            bout = new ByteArrayOutputStream();
                            ObjectOutputStream oout = new ObjectOutputStream(bout);
                            oout.writeObject(portlets);
                            oout.flush();
                            bout.close();
                            oout.close();
                            up.setPortletsList(Hibernate.createBlob(bout.toByteArray()));
                            hbsession.update(up);
                            hbsession.flush();
                            if (!hbsession.connection().getAutoCommit()) {
                                tx.commit();
                            }
                        }
                    }
                }
            } catch (Exception sqle) {
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("portlets.error", sqle.toString()));
                saveErrors(request, errors);
                sqle.printStackTrace();
            } finally {
                HibernateUtil.closeSession();
            }
        }
        if (portletsform.getWarFile() != null) {
            ActionErrors errors = new ActionErrors();
            boolean hasWebXml = false;
            boolean hasPortletXml = false;
            try {
                ZipInputStream zipInputStream = new ZipInputStream(portletsform.getWarFile().getInputStream());
                ZipEntry zipEntry;
                StringTokenizer tokenizer = new StringTokenizer(portletsform.getWarFile().getFileName(), ".");
                String prefix = tokenizer.nextToken();
                String suffix = tokenizer.nextToken();
                File f = File.createTempFile(prefix, "." + suffix);
                f.deleteOnExit();
                ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(f));
                ArrayList portlets = new ArrayList();
                PortletApplicationDefinition appDefs = null;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    if (zipEntry.getName().toLowerCase().endsWith("/portlet.xml")) {
                        hasPortletXml = true;
                        File portletxml = File.createTempFile("portlet", ".xml");
                        FileOutputStream pout = new FileOutputStream(portletxml);
                        zipOut.putNextEntry(new ZipEntry(zipEntry.getName()));
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = zipInputStream.read(buf)) > 0) {
                            zipOut.write(buf, 0, len);
                            pout.write(buf, 0, len);
                        }
                        pout.close();
                        zipOut.flush();
                        zipOut.closeEntry();
                        appDefs = createPortletList(zipEntry, portletxml);
                        portlets = appDefs.getPortletdefinitions();
                    } else if (zipEntry.getName().toLowerCase().endsWith("/web.xml")) {
                        hasWebXml = true;
                        zipOut.putNextEntry(new ZipEntry("/WEB-INF/web.xml"));
                        FileInputStream fin = new FileInputStream(new File(readWebXml(zipEntry, zipInputStream)));
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = fin.read(buf)) > 0) {
                            zipOut.write(buf, 0, len);
                        }
                        zipOut.flush();
                        zipOut.closeEntry();
                    } else {
                        zipOut.putNextEntry(new ZipEntry(zipEntry.getName()));
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = zipInputStream.read(buf)) > 0) {
                            zipOut.write(buf, 0, len);
                        }
                        zipOut.flush();
                        zipOut.closeEntry();
                    }
                }
                if (!hasWebXml || !hasPortletXml) {
                    errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("portlets.nowebxml"));
                    saveErrors(request, errors);
                    this.setApps(request);
                    return mapping.getInputForward();
                }
                String pathToLib = getServlet().getServletContext().getRealPath("/WEB-INF/lib/nvportlet-tags.jar");
                zipOut.putNextEntry(new ZipEntry("WEB-INF/lib/nvportlet-tags.jar"));
                FileInputStream fin = new FileInputStream(pathToLib);
                byte[] buf = new byte[1024];
                int len;
                while ((len = fin.read(buf)) > 0) {
                    zipOut.write(buf, 0, len);
                }
                fin.close();
                pathToLib = getServlet().getServletContext().getRealPath("/WEB-INF/lib/nvlib.jar");
                zipOut.putNextEntry(new ZipEntry("WEB-INF/lib/nvlib.jar"));
                fin = new FileInputStream(pathToLib);
                buf = new byte[1024];
                while ((len = fin.read(buf)) > 0) {
                    zipOut.write(buf, 0, len);
                }
                fin.close();
                zipOut.flush();
                zipOut.closeEntry();
                zipOut.close();
                zipInputStream.close();
                String dest = "";
                dest = getServlet().getServletContext().getRealPath("/");
                dest = dest.substring(0, dest.length() - 1);
                dest = dest.substring(0, dest.lastIndexOf(dirDelim) + 1);
                if (portletsform.isExploded()) {
                    if (!RepositoryBasic.getConfig().getFilesystem().getExplodedDir().equalsIgnoreCase("")) {
                        dest = RepositoryBasic.getConfig().getFilesystem().getExplodedDir() + dirDelim;
                    }
                    dest += prefix;
                    dest += dirDelim;
                    new File(dest).mkdirs();
                    InputStream in = new BufferedInputStream(new FileInputStream(f));
                    ZipInputStream zin = new ZipInputStream(in);
                    ZipEntry e;
                    while ((e = zin.getNextEntry()) != null) {
                        if (e.isDirectory()) {
                            new File(dest + e.getName()).mkdirs();
                        } else {
                            unzip(zin, dest, e);
                        }
                    }
                    zin.close();
                } else {
                    if (!RepositoryBasic.getConfig().getFilesystem().getWarDir().equalsIgnoreCase("")) {
                        dest = RepositoryBasic.getConfig().getFilesystem().getWarDir() + dirDelim;
                    }
                    dest += prefix + "." + suffix;
                    InputStream in = new FileInputStream(f);
                    OutputStream out = new FileOutputStream(dest);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                    in.close();
                    out.close();
                }
                errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionError("portlets.deployed", prefix));
                RepositoryBasic.getInstance(getServlet().getServletContext());
                Webapplication oldWebapp = RepositoryBasic.getWebapplications().getWebapplication(prefix);
                ArrayList rportlets = new ArrayList();
                if (oldWebapp == null) {
                    Webapplication newApp = new Webapplication();
                    newApp.setContext("/" + prefix);
                    newApp.setIdWebapplication(prefix);
                    newApp.setApplicationDefinition(appDefs);
                    for (int z = 0; z < portlets.size(); z++) {
                        PortletDefinition def = (PortletDefinition) portlets.get(z);
                        RegisteredPortlet regPortlet = new RegisteredPortlet();
                        regPortlet.setNamePortlet(def.getPortletName());
                        regPortlet.setProvideRequest(Boolean.FALSE);
                        regPortlet.setProvideRequest(Boolean.FALSE);
                        regPortlet.setPortletDefinition(def);
                        rportlets.add(regPortlet);
                    }
                    newApp.setPortlets(rportlets);
                    WebApplications apps = RepositoryBasic.getWebapplications();
                    ArrayList temp = apps.getWebApplications();
                    temp.add(newApp);
                    apps.setWebApplications(temp);
                    RepositoryBasic.getInstance(getServlet().getServletContext()).savePortlets(apps);
                } else {
                    for (int z = 0; z < portlets.size(); z++) {
                        PortletDefinition def = (PortletDefinition) portlets.get(z);
                        RegisteredPortlet regPortlet = new RegisteredPortlet();
                        regPortlet.setPortletDefinition(def);
                        regPortlet.setNamePortlet(def.getPortletName());
                        rportlets.add(regPortlet);
                        getServlet().getServletContext().setAttribute(Constants.PORTLET_INSTANCE + "." + prefix + "." + def.getPortletName(), null);
                    }
                    oldWebapp.setPortlets(rportlets);
                    WebApplications apps = RepositoryBasic.getWebapplications();
                    RepositoryBasic.getInstance(getServlet().getServletContext()).savePortlets(apps);
                }
                saveErrors(request, errors);
            } catch (Exception e) {
                e.printStackTrace();
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("portlets.error", e.toString()));
                saveErrors(request, errors);
            }
        }
        this.setApps(request);
        return mapping.getInputForward();
    }

    private String readWebXml(ZipEntry zipEntry, ZipInputStream zipInputStream) throws Exception {
        try {
            File f = File.createTempFile("web", ".xml");
            f.deleteOnExit();
            FileOutputStream fout = new FileOutputStream(f);
            byte[] buf = new byte[1024];
            int len;
            while ((len = zipInputStream.read(buf)) > 0) {
                fout.write(buf, 0, len);
            }
            fout.close();
            SAXBuilder builder = new SAXBuilder(false);
            builder.setValidation(false);
            builder.setEntityResolver(new NoOpEntityResolver());
            Document doc = builder.build(f);
            Element root = doc.getRootElement();
            List servlets = root.getChildren("servlet");
            Element servletElement = new Element("servlet");
            Element servletName = new Element("servlet-name");
            servletName.setText("SessionProvider");
            Element servletClass = new Element("servlet-class");
            servletClass.setText("org.nodevision.portal.utils.SessionProvider");
            servletElement.addContent(servletName);
            servletElement.addContent(servletClass);
            servlets.add(servletElement);
            List mappings = root.getChildren("servlet-mapping");
            Element mappingElement = new Element("servlet-mapping");
            Element urlPattern = new Element("url-pattern");
            urlPattern.setText("/SessionProvider");
            servletName = new Element("servlet-name");
            servletName.setText("SessionProvider");
            mappingElement.addContent(servletName);
            mappingElement.addContent(urlPattern);
            mappings.add(mappingElement);
            fout = new FileOutputStream(f);
            XMLOutputter fmt = new XMLOutputter();
            fmt.setFormat(Format.getPrettyFormat());
            fmt.output(doc, fout);
            fout.close();
            return f.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static void unzip(ZipInputStream zin, String s, ZipEntry zipEntry) throws IOException {
        File temp = new File(s + zipEntry.getName());
        new File(temp.getParent()).mkdirs();
        FileOutputStream out = new FileOutputStream(temp);
        byte[] b = new byte[512];
        int len = 0;
        while ((len = zin.read(b)) != -1) {
            out.write(b, 0, len);
        }
        out.close();
    }

    private PortletApplicationDefinition createPortletList(ZipEntry zipEnry, File portletxml) throws Exception {
        String DEFAULT_MAPPING_PORTLETXML = getServlet().getServletContext().getRealPath("/WEB-INF/config/mappings/castor/portletdefinition_mapping.xml");
        Mapping mappingPortlet = new Mapping();
        mappingPortlet.loadMapping(new InputSource(new FileInputStream(DEFAULT_MAPPING_PORTLETXML)));
        Unmarshaller unmarshallerPortlet = new Unmarshaller(mappingPortlet);
        unmarshallerPortlet.setValidation(false);
        unmarshallerPortlet.setIgnoreExtraElements(true);
        unmarshallerPortlet.setIgnoreExtraAttributes(true);
        PortletApplicationDefinition defintion = (PortletApplicationDefinition) unmarshallerPortlet.unmarshal(new InputSource(new FileInputStream(portletxml)));
        return defintion;
    }

    private void setApps(HttpServletRequest request) {
        RepositoryBasic.getInstance(getServlet().getServletContext());
        ArrayList webapps = RepositoryBasic.getWebapplications().getWebApplications();
        ArrayList list = new ArrayList();
        for (int i = 0; i < webapps.size(); i++) {
            Webapplication app = (Webapplication) webapps.get(i);
            HashMap temp = new HashMap();
            temp.put("context", app.getContext());
            temp.put("view", app.getIdWebapplication() + " (" + app.getContext() + ")");
            temp.put("id", app.getIdWebapplication());
            list.add(temp);
        }
        request.setAttribute("webapps", list);
    }
}
