package com.google.code.javastorage.cli.cmd;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;
import com.google.code.javastorage.StorageFile;

/**
 * 
 * @author thomas.scheuchzer@gmail.com
 * 
 */
public class Open extends Get {

    @Override
    protected File getOutFile(StorageFile file) {
        try {
            File f = File.createTempFile("java-storage_" + file.getName(), ".tmp." + StringUtils.substringAfterLast(file.getName(), "."));
            f.deleteOnExit();
            return f;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void afterSave(File outFile) {
        Desktop desktop = null;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
            try {
                desktop.open(outFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
