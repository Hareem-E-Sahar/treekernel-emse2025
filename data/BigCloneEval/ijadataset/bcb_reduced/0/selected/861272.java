package com.antwerkz.qwicket.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.antwerkz.qwicket.QwicketException;
import com.antwerkz.qwicket.model.Project;
import com.antwerkz.qwicket.model.PersistenceLayer;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created Jun 29, 2006
 *
 * @author <a href="mailto:jlee@antwerkz.com">Justin Lee</a>
 */
public class ProjectBuilder {

    private static final Logger log = LoggerFactory.getLogger(ProjectBuilder.class);

    private String root;

    private String templateDir;

    private List<QwicketTemplate> javaFiles;

    private List<QwicketTemplate> otherFiles;

    private VelocityContext context;

    private String identifier;

    private Project project;

    private VelocityEngine engine;

    public ProjectBuilder() {
        try {
            engine = new VelocityEngine();
            engine.init();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new QwicketException(e.getMessage());
        }
    }

    private void configureTemplates() {
        Map<String, Object> properties = new HashMap<String, Object>();
        String name = project.getName();
        properties.put("ApplicationName", name);
        identifier = name.replaceAll(" ", "");
        properties.put("ApplicationIdentifier", identifier);
        properties.put("ApplicationIdentifierLower", identifier.toLowerCase());
        properties.put("package", project.getBasePackage());
        context = new VelocityContext(properties);
    }

    public byte[] outputProject(String directory, Project proj) throws IOException {
        File rootDir = null;
        try {
            root = System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID() + "/";
            rootDir = new File(root);
            rootDir.mkdirs();
            rootDir.deleteOnExit();
            project = proj;
            configureTemplates();
            if (proj.getPersistence() == PersistenceLayer.HIBERNATE) {
                context.put("Hibernate", true);
            }
            processCommonTemplates(directory);
            return zipProject();
        } finally {
            if (rootDir != null) {
                delete(rootDir);
            }
        }
    }

    private void processCommonTemplates(String path) throws IOException {
        templateDir = new File(path + "/common/").getCanonicalPath();
        javaFiles = new ArrayList<QwicketTemplate>();
        otherFiles = new ArrayList<QwicketTemplate>();
        findTemplates(new File(templateDir));
        processFiles(javaFiles, true);
        processFiles(otherFiles, false);
    }

    protected void delete(File rootDir) {
        for (File file : rootDir.listFiles()) {
            if (file.isFile()) {
                file.delete();
            } else {
                delete(file);
                file.delete();
            }
        }
        rootDir.delete();
    }

    private byte[] zipProject() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zip = null;
        try {
            zip = new ZipOutputStream(baos);
            for (File file : new File(root).listFiles()) {
                addToZip(zip, file);
            }
        } finally {
            if (zip != null) {
                zip.close();
            }
        }
        return baos.toByteArray();
    }

    private void addToZip(ZipOutputStream zip, File path) throws IOException {
        if (path.isDirectory()) {
            for (File file : path.listFiles()) {
                addToZip(zip, file);
            }
        } else {
            addFile(zip, path);
        }
    }

    private void addFile(ZipOutputStream zip, File file) throws IOException {
        byte[] buf = new byte[4096];
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            String path = getPath(file);
            zip.putNextEntry(new ZipEntry(path));
            int len;
            while ((len = in.read(buf)) > 0) {
                zip.write(buf, 0, len);
            }
            zip.closeEntry();
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private String getPath(File file) {
        String path = file.getPath().substring(new File(root).getPath().length());
        if (path.startsWith("/") || path.startsWith("\\")) {
            path = path.substring(1);
        }
        return path;
    }

    private void findTemplates(File rootDir) {
        File[] files = rootDir.listFiles(new NoSvnFilter());
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String path = file.getPath().substring(templateDir.length() + 1);
                    if (path.contains("java")) {
                        javaFiles.add(new QwicketTemplate(path, file));
                    } else {
                        otherFiles.add(new QwicketTemplate(path, file));
                    }
                } else {
                    findTemplates(file);
                }
            }
        }
    }

    @SuppressWarnings({ "StringContatenationInLoop" })
    private void processFiles(List<QwicketTemplate> files, boolean replacePackage) {
        for (QwicketTemplate src : files) {
            Writer out = null;
            try {
                StringWriter buffer = new StringWriter();
                engine.evaluate(context, buffer, src.getPath(), readFile(src));
                if (buffer.getBuffer().length() != 0) {
                    String pathname = root + src.getPath().replaceAll("\\.template", "").replaceAll("Qwicket", identifier);
                    if (replacePackage) {
                        pathname = pathname.replaceAll("java" + (File.separatorChar == '/' ? "/" : "\\\\") + "qwicket", "java/" + project.getBasePackage().replaceAll("\\.", "/"));
                    }
                    File file = new File(pathname);
                    file.getParentFile().mkdirs();
                    out = new FileWriter(file);
                    out.write(buffer.toString());
                    out.flush();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new QwicketException(e.getMessage());
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        throw new QwicketException(e.getMessage());
                    }
                }
            }
        }
    }

    private String readFile(QwicketTemplate src) throws IOException {
        StringBuilder builder = new StringBuilder();
        FileReader fileReader = null;
        BufferedReader reader = null;
        try {
            fileReader = new FileReader(src.getSource());
            reader = new BufferedReader(fileReader);
            while (reader.ready()) {
                builder.append(reader.readLine());
                builder.append("\n");
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (fileReader != null) {
                fileReader.close();
            }
        }
        return builder.toString();
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String path) {
        root = path;
    }

    private static class NoSvnFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            return !name.contains(".svn");
        }
    }
}
