package net.thartm.zipdownload.webscripts;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;

public class ZipToOutputStreamWriter {

    private static final int BUFFER_SIZE = 2048;

    private FileFolderService fileFolderService;

    public ZipToOutputStreamWriter(ServiceRegistry serviceRegistry) {
        this.fileFolderService = serviceRegistry.getFileFolderService();
    }

    public void write(FileInfo[] files, OutputStream out) throws IOException {
        ZipOutputStream zout = null;
        try {
            zout = new ZipOutputStream(out);
            streamFiles(files, zout);
        } finally {
            try {
                if (zout != null) {
                    zout.close();
                }
            } catch (IOException e) {
            }
        }
    }

    private void streamFiles(FileInfo[] files, ZipOutputStream zout) throws IOException {
        for (FileInfo fileInfo : files) {
            if (!fileInfo.isFolder()) {
                createNewZipEntry(fileInfo, zout);
            }
        }
    }

    private void createNewZipEntry(FileInfo fileInfo, ZipOutputStream zout) throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        ZipEntry zipEntry = new ZipEntry(fileInfo.getName());
        zout.putNextEntry(zipEntry);
        int len;
        InputStream is = getFileInputStream(fileInfo);
        while ((len = is.read(buf)) > 0) {
            zout.write(buf, 0, len);
        }
    }

    private InputStream getFileInputStream(FileInfo fileInfo) {
        ContentReader reader = fileFolderService.getReader(fileInfo.getNodeRef());
        InputStream is = reader.getContentInputStream();
        return is;
    }
}
