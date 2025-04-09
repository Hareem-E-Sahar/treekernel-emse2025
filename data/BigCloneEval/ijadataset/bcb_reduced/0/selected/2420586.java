package com.abb.util;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.*;

/** This class can be used to handle zip files
 */
public class ZipFileHandler {

    /** the zipfile to which the handler is currently assigned */
    private ZipFile zf;

    /** adds a file/directory to the given zip stream
	@param zos      the (opened) zip stream
	@param baseDir  the part of the directory that should not appear
	                in the zip file (non-empty without trailing
			separator)
	@param relDir   the (visible) path, relative to the <tt>baseDir</tt>
	                (maybe empty - if not it must contain a trailing
			path separator)
	@param filename the name of the file or directory to be added
	@exception IOException in case of any error
     */
    public static void addFileToZipStream(ZipOutputStream zos, String baseDir, String relDir, String filename) throws IOException {
        File f = new File(baseDir + File.separatorChar + relDir + filename);
        if (f.isDirectory()) {
            String[] files = f.list();
            for (int i = 0; i < files.length; i++) {
                addFileToZipStream(zos, baseDir, relDir + filename + File.separatorChar, files[i]);
            }
        } else {
            ZipEntry e = new ZipEntry(relDir + filename);
            zos.putNextEntry(e);
            zos.write(new FileContents(f.getPath()).contents);
            zos.closeEntry();
        }
    }

    /** adds a file/directory to the given zip stream
	@param zipname the name of the archive to be created
	@param baseDir the part of the directory that should not appear
	               in the zip file (must be a valid directory name
		       without a trailing separator, e.g. ".")
	@param files   the names of the files to be added
	@exception IOException in case of any error
     */
    public static void createZipFile(String zipname, String baseDir, String[] files) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipname));
        zos.setMethod(ZipOutputStream.DEFLATED);
        zos.setLevel(9);
        for (int i = 0; i < files.length; i++) {
            addFileToZipStream(zos, baseDir, "", files[i]);
        }
        zos.close();
    }

    /** creates an unassigned zip file handler */
    public ZipFileHandler() {
    }

    /** creates a ZipFileHandler which is assigned to a certain zip file
	@param zf the zip file to which the handler should be assigned
    */
    public ZipFileHandler(ZipFile zf) {
        this();
        assign(zf);
    }

    /** assigns the handler to the given file
	@param zf the zip file the handler should be assigned to
    */
    public void assign(ZipFile zf) {
        this.zf = zf;
    }

    /** adds a file (content) to the assigned zip file
	@param fc the file content object
     */
    public void addFileContent(FileContents fc) throws IOException {
        if ((zf != null) && (fc != null)) {
            System.err.println("adding files to a zip archive is currently not implemented");
            throw new RuntimeException();
        }
    }

    /** adds a file (content) to the assigned zip file
	@param entry the fc the file content object
	@param compress use compression?
	@return an object that contains the (uncompressed) file data or
	        null if the entry represents a directory
     */
    public FileContents extractFileContent(ZipEntry entry) throws IOException {
        if ((zf == null) && (entry == null)) {
            return null;
        } else {
            if (entry.isDirectory()) {
                return null;
            } else {
                return new FileContents(entry.getName(), zf.getInputStream(entry), false);
            }
        }
    }
}
