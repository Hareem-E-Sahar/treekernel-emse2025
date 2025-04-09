package com.kescom.matrix.core.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipStreamExporter implements IStreamExporter {

    private Properties properties = new Properties();

    public void export(IExportSet exportSet, OutputStream os) throws IOException {
        ZipOutputStream out = new ZipOutputStream(os);
        for (IExportUnit eu : exportSet.getExportUnits()) {
            ZipEntry entry = new ZipEntry(eu.getRelativePath(this));
            out.putNextEntry(entry);
            eu.export(out, this);
            out.closeEntry();
        }
        out.close();
    }

    public String getContentType() {
        return "application/zip";
    }

    public String getFilenameExtension() {
        return "zip";
    }

    public Properties getProperties() {
        return properties;
    }
}
