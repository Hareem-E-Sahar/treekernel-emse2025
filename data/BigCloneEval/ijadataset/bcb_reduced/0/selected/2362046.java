package org.in4ama.editor.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.in4ama.documentengine.generator.DocumentEngine;
import org.in4ama.editor.exception.EditorException;

public class FOPCompiler {

    private ProjectHandler projHandler;

    private String fopConfigPath, fopConfigDir;

    /** Returns the path to the compressed file 
	 * @throws EditorException */
    public String compileFOP() throws EditorException {
        fopConfigPath = DocumentEngine.DEFAULT_FOP_CONFIG_FILE;
        File file = new File(fopConfigPath);
        fopConfigDir = file.getParentFile().getAbsolutePath();
        String filename = fopConfigDir + File.separator + "fop.zip";
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(filename));
        } catch (Exception ex) {
            String msg = "Unable to create a zip output stream.";
            throw new EditorException(msg, ex);
        }
        addDir(out, fopConfigDir);
        try {
            out.close();
        } catch (Exception ex) {
            String msg = "Unable to close the input stream.";
            throw new EditorException(msg, ex);
        }
        return filename;
    }

    public void addDir(ZipOutputStream out, String dirname) throws EditorException {
        byte[] buf = new byte[1024];
        File dir = new File(dirname);
        String dName = dir.getName();
        if (dir.exists()) {
            try {
                File file = new File(dirname);
                File[] children = file.listFiles();
                for (int i = 0; i < children.length; i++) {
                    if ((children[i].isFile()) && (!children[i].getName().endsWith("fop.zip"))) {
                        FileInputStream fis = new FileInputStream(children[i]);
                        out.putNextEntry(new ZipEntry(dName + "/" + children[i].getName()));
                        int len;
                        while ((len = fis.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        out.closeEntry();
                        fis.close();
                    }
                }
            } catch (IOException e) {
                String msg = "Unable to add a directory to the ZIP output.";
                throw new EditorException(msg, e);
            }
        }
    }

    public void addFile(ZipOutputStream out, String filename) throws EditorException {
    }
}
