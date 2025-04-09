package com.indigen.victor.export.project;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.w3c.dom.Node;
import com.indigen.victor.actions.VictorAction;
import com.indigen.victor.export.StatefulExporter;
import com.indigen.victor.util.XmlUtils;

public class ProjectExporter extends StatefulExporter {

    public ProjectExporter(VictorAction action) {
        super(action);
        shouldCheckMaxExports = false;
        repositoryRootPath = getProperty("repository-root-path", "/web/victor/archives");
    }

    public String getExporterName() {
        return "project";
    }

    public String getExporterTitle() {
        return "Project";
    }

    public Map getNextPageSrc(Map bizData, String nextPage) {
        if (state == STATE_REQ_DATA && nextPage.startsWith("about-to-start")) {
            String exportName = action.getStringFromXPath("/project/info/name");
            File repositoryDir = new File(getRepositoryPath());
            if (repositoryDir.exists() && new File(repositoryDir, exportName + ".zip").exists()) {
                int i = 2;
                for (; ; i++) {
                    if (new File(repositoryDir, exportName + "-" + i + ".zip").exists() == false) break;
                }
                exportName = exportName + "-" + i;
            }
            bizData.put("exportname", exportName);
        }
        return super.getNextPageSrc(bizData, nextPage);
    }

    protected boolean doExport(Node data, Map exportOut) {
        String exportName = XmlUtils.getStringFromXPath(data, "name");
        File baseDir = new File(getRepositoryPath());
        if (baseDir.exists() == false) {
            if (baseDir.mkdirs() == false) {
                lastError = "error.cannotcreateoutputdirectory";
                return false;
            }
        }
        File zipFile = new File(baseDir, exportName + ".zip");
        if (zipFile.exists()) {
            lastError = "error.alreadyexist";
            return false;
        }
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(zipFile);
        } catch (FileNotFoundException e) {
            lastError = "error.filenotfound";
            return false;
        }
        ZipOutputStream zos = new ZipOutputStream(fos);
        Node projectNode = action.getNodeFromXPath("/project");
        String projectXml = "<!DOCTYPE project PUBLIC \"" + VictorAction.VICTOR_PUBLIC_ID + "\" \"http://dtd.indigen.com/victor.dtd\">\n" + XmlUtils.serialize(projectNode);
        ByteArrayInputStream bais;
        try {
            bais = new ByteArrayInputStream(projectXml.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            return false;
        }
        try {
            zos.putNextEntry(new ZipEntry("project.xml"));
            XmlUtils.copy(bais, zos);
            bais.close();
            zos.closeEntry();
            File[] dataFiles = action.getBaseDirectory().listFiles();
            for (int i = 0; i < dataFiles.length; i++) {
                zos.putNextEntry(new ZipEntry(dataFiles[i].getName()));
                FileInputStream fis = new FileInputStream(new File(dataFiles[i].getAbsolutePath()));
                XmlUtils.copy(fis, zos);
                fis.close();
                zos.closeEntry();
            }
            zos.close();
        } catch (IOException e) {
            action.getActionLogger().error("doExport failed on " + zipFile.getAbsolutePath() + ": " + e);
            lastError = "error.ioerror";
            return false;
        }
        return true;
    }

    public List getInstances() {
        return new Vector();
    }

    public String getRepositoryPath() {
        return repositoryRootPath;
    }
}
