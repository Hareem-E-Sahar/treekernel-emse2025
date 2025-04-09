package com.cromoteca.meshcms.server.toolbox;

import com.cromoteca.meshcms.client.toolbox.Path;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Creates a ZIP file from a directory or file.
 */
public class ZipArchiver extends DirectoryParser {

    private ZipOutputStream zout;

    private byte[] buf;

    /**
	 * Instantiates the archiver for the given file and output stream.
	 *
	 * @param contents the file to be archieved
	 * @param out      the OutputStream to write the archive to.
	 */
    public ZipArchiver(File contents, OutputStream out) {
        zout = new ZipOutputStream(out);
        setInitialDir(contents);
        setRecursive(true);
        buf = new byte[IO.BUFFER_SIZE];
    }

    @Override
    protected void postProcess() {
        try {
            zout.finish();
            zout.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected boolean preProcessDirectory(File file, Path path) {
        try {
            ZipEntry ze = new ZipEntry(path + "/");
            ze.setTime(file.lastModified());
            zout.putNextEntry(ze);
            zout.closeEntry();
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void processFile(File file, Path path) {
        try {
            ZipEntry ze = new ZipEntry(path.isRoot() ? file.getName() : path.toString());
            ze.setTime(file.lastModified());
            ze.setSize(file.length());
            zout.putNextEntry(ze);
            InputStream fis = new FileInputStream(file);
            int len;
            while ((len = fis.read(buf)) != -1) {
                zout.write(buf, 0, len);
            }
            fis.close();
            zout.closeEntry();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
