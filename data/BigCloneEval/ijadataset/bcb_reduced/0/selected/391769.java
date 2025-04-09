package com.bbn.vessel.author.workspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import com.bbn.vessel.author.util.IOHelper;

public class AutoSaveFileManager extends FolderFileManager {

    private static String autosaveTempFolder = "logs/autosave/autosave-temp";

    protected static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.SSS");

    private final Logger logger = Logger.getLogger(AutoSaveFileManager.class);

    private final int workspaceId;

    private final String workspaceDataSourceName;

    public AutoSaveFileManager(int workspaceId, String dataSourceName) throws IOException {
        this.workspaceId = workspaceId;
        this.workspaceDataSourceName = dataSourceName;
        setDataSource(autosaveTempFolder);
    }

    /**
     * zip the autosave folder up and delete it.
     *
     */
    public void saveOffZip() {
        final String autosaveTimestamp = sdf.format(System.currentTimeMillis());
        File tempFile = new File("logs/autosave/autosave-temp.zip");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(tempFile);
            ZipOutputStream zos = new ZipOutputStream(fos);
            for (File f : folder.listFiles()) {
                zos.putNextEntry(new ZipEntry(f.getName()));
                FileInputStream fis = new FileInputStream(f);
                byte[] buf = new byte[102400];
                int len = 0;
                while ((len = fis.read(buf)) > 0) {
                    zos.write(buf, 0, len);
                }
                zos.flush();
                zos.closeEntry();
                fis.close();
            }
            zos.flush();
            fos.getFD().sync();
            zos.close();
            StringBuilder fileNameBuilder = new StringBuilder();
            fileNameBuilder.append("logs/autosave/autosave-w").append(workspaceId).append('-');
            if (workspaceDataSourceName != null) {
                fileNameBuilder.append(workspaceDataSourceName).append('-');
            }
            fileNameBuilder.append(autosaveTimestamp).append(".zip");
            tempFile.renameTo(new File(fileNameBuilder.toString()));
            if (!IOHelper.recursivelyDeleteFile(folder)) {
                throw new IOException("couldn't delete " + folder);
            }
        } catch (IOException e) {
            logger.error(e, e);
        }
    }
}
