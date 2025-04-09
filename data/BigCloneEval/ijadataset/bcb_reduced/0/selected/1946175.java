package org.jprovocateur.basisweb.controller.view;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.jprovocateur.basis.ApplicationContextProvider;
import org.jprovocateur.basis.datalayer.GenericDaoDBInt;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.View;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;

public class ZipView implements View, BeanFactoryAware {

    private BeanFactory beanFactory;

    static final int BUFFER = 2048;

    private static org.apache.log4j.Logger log = Logger.getLogger(ZipView.class.getName());

    public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws InvocationTargetException, NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        response.setContentType(getContentType());
        response.setHeader("Content-Disposition", "attachment; filename=GeneratedFiles.zip");
        TemplateObject to = new TemplateObject();
        to.setEntityname("BasisClass");
        to.setEntitypackage("org.jprovocateur.basis.objectmodel.accessrights.impl");
        to.setServicepackage("com.myproject.businesslayer.test");
        ApplicationContext cont = ApplicationContextProvider.getApplicationContext();
        GenericDaoDBInt dao = (GenericDaoDBInt) cont.getBean("genericDaoDB");
        Constructor<?> constructur = Class.forName(to.getEntitypackage() + "." + to.getEntityname()).getConstructor();
        Object obj = (Object) constructur.newInstance();
        String idProperty = dao.getIdentifier(obj);
        to.setPkName(idProperty);
        List<File> files = new ArrayList<File>();
        files.add(generateFile("generated/services/createService.ftl", to.getEntityname() + "CreateService.java", to));
        files.add(generateFile("generated/services/updateService.ftl", to.getEntityname() + "UpdateService.java", to));
        files.add(generateFile("generated/services/deleteService.ftl", to.getEntityname() + "DeleteService.java", to));
        files.add(generateFile("generated/services/listService.ftl", to.getEntityname() + "ListService.java", to));
        files.add(generateFile("generated/controller/dataController.ftl", "Data" + to.getEntityname() + "Controller.java", to));
        files.add(generateFile("generated/controller/viewController.ftl", "View" + to.getEntityname() + "Controller.java", to));
        zipFiles(files, response);
    }

    private void zipFiles(List<File> files, HttpServletResponse response) {
        try {
            BufferedInputStream origin = null;
            ZipOutputStream out = new ZipOutputStream(response.getOutputStream());
            byte data[] = new byte[BUFFER];
            for (int i = 0; i < files.size(); i++) {
                FileInputStream fi = new FileInputStream(files.get(i));
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(files.get(i).getName());
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
        } catch (Exception e) {
            log.error(e);
        }
    }

    public String getContentType() {
        return "application/octet-stream";
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    private File generateFile(String templateFileName, String fileName, TemplateObject to) {
        try {
            File f = new File(fileName);
            FileWriter fstream = new FileWriter(f);
            BufferedWriter out = new BufferedWriter(fstream);
            Configuration cfg = new Configuration();
            cfg.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
            cfg.setClassForTemplateLoading(this.getClass(), "/");
            Template tpl = cfg.getTemplate(templateFileName);
            Writer outWrite = new StringWriter();
            Map root = new HashMap();
            root.put("servicepackage", to.getServicepackage());
            root.put("entityname", to.getEntityname());
            root.put("entitypackage", to.getEntitypackage());
            root.put("pkname", to.getPkName());
            tpl.process(root, outWrite);
            out.write(outWrite.toString());
            out.close();
            return f;
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }

    class TemplateObject {

        private String servicepackage;

        private String entityname;

        private String entitypackage;

        private String pkName;

        public String getPkName() {
            return pkName;
        }

        public void setPkName(String pkName) {
            this.pkName = pkName;
        }

        public String getServicepackage() {
            return servicepackage;
        }

        public void setServicepackage(String servicepackage) {
            this.servicepackage = servicepackage;
        }

        public String getEntityname() {
            return entityname;
        }

        public void setEntityname(String entityname) {
            this.entityname = entityname;
        }

        public String getEntitypackage() {
            return entitypackage;
        }

        public void setEntitypackage(String entitypackage) {
            this.entitypackage = entitypackage;
        }
    }
}
