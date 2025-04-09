package org.thirdstreet.blogger.writer.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.thirdstreet.blogger.Blog;
import org.thirdstreet.blogger.writer.BlogFileFilter;
import org.thirdstreet.blogger.writer.IBloggerWriter;

/**
 * Writer to create a zip file of our blog
 * 
 * @author John Bramlett
 */
public class ZipBloggerWriter implements IBloggerWriter {

    private static final Log logger = LogFactory.getLog(ZipBloggerWriter.class);

    protected String filename;

    /**
	 * Constructor
	 */
    public ZipBloggerWriter() {
        super();
    }

    public void write(Blog blog) {
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(getFilename()));
            out.setLevel(Deflater.DEFAULT_COMPRESSION);
            File dir = new File(blog.getBlogDirectory());
            String[] xmlFiles = dir.list(new BlogFileFilter(blog.getBlogId()));
            zipFiles(out, blog.getBlogDirectory(), xmlFiles);
            File commentDir = new File(blog.getBlogCommentDirectory());
            String[] commentFiles = commentDir.list();
            zipFiles(out, commentDir.getAbsolutePath(), commentFiles);
            File imageDir = new File(blog.getBlogImageDirectory());
            String[] imageFiles = imageDir.list();
            zipFiles(out, imageDir.getAbsolutePath(), imageFiles);
            out.close();
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
	 * Writes the set of files to the given zip
	 * 
	 * @param out The zip file
	 * @param dir The directory for the files we are zipping
	 * @param files The file list
	 */
    protected void zipFiles(ZipOutputStream out, String dir, String[] files) {
        for (int i = 0; i < files.length; i++) {
            zipFile(out, dir + File.separator + files[i]);
        }
    }

    /**
	 * Adds a file to our zip
	 * @param out
	 * @param file
	 */
    protected void zipFile(ZipOutputStream out, String file) {
        byte[] buffer = new byte[18024];
        try {
            logger.debug("Adding " + file + " to zip");
            FileInputStream in = new FileInputStream(file);
            out.putNextEntry(new ZipEntry(file));
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.closeEntry();
            in.close();
        } catch (Exception e) {
            logger.error("Failed to add file " + file + " to zip!", e);
            throw new RuntimeException("Failed to add file " + file + " to zip!", e);
        }
    }

    /**
	 * Gets the filename
	 * 
	 * @return the filename
	 */
    public String getFilename() {
        return filename;
    }

    /**
	 * Sets the filename
	 * 
	 * @param filename_ the filename to set
	 */
    public void setFilename(String filename_) {
        filename = filename_;
    }
}
