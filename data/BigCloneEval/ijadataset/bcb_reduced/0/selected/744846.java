package org.in4ama.editor.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Class that takes care of gathering project files into a single zip file for
 * deployment to the web service.
 * 
 * @author Val Cassidy, Jakub Jonik
 */
public class ProjectCompiler {

    private ProjectHandler projHandler;

    public void compileProject() {
        projHandler = ProjectHandler.getInstance();
        String path = projHandler.getProjectPath();
        String filename = path + File.separator + "deploy.zip";
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(filename));
            addFile(out, "in4ama.xml");
            addFile(out, "datasets.xml");
            addFile(out, "datasourceset.cfg.xml");
            addFile(out, "documents.xml");
            addFile(out, "packs.xml");
            addFile(out, "variables.xml");
            addFile(out, "projectconfig.xml");
            addDir(out, "images");
            addDir(out, "acroforms");
            addDir(out, "acroforms" + File.separator + "bindings");
            addDir(out, "emails" + File.separator + "xhtml");
            addDir(out, "fragments" + File.separator + "xsl-fo");
            addDir(out, "letters" + File.separator + "xsl-fo");
            addDir(out, "ooacroforms" + File.separator + "pdf");
            addDir(out, "ooacroforms" + File.separator + "pdf" + File.separator + "bindings");
            addDir(out, "templates" + File.separator + "tables" + File.separator + "xsl-fo");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addDir(ZipOutputStream out, String dirname) {
        byte[] buf = new byte[1024];
        File dir = new File(projHandler.getProjectPath() + File.separator + dirname);
        if (dir.exists()) {
            try {
                File file = new File(projHandler.getProjectPath() + File.separator + dirname);
                File[] children = file.listFiles();
                for (int i = 0; i < children.length; i++) {
                    if (children[i].isFile()) {
                        FileInputStream fis = new FileInputStream(children[i]);
                        out.putNextEntry(new ZipEntry(dirname.replace('\\', '/') + '/' + children[i].getName()));
                        int len;
                        while ((len = fis.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        out.closeEntry();
                        fis.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addFile(ZipOutputStream out, String filename) {
        try {
            byte[] buf = new byte[1024];
            String filePath = projHandler.getProjectPath() + File.separator + filename;
            File file = new File(filePath);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(filePath);
                out.putNextEntry(new ZipEntry(filename));
                int len;
                while ((len = fis.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                fis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
