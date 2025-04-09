package com.jcorporate.expresso.core.misc.upload;

import com.jcorporate.expresso.core.misc.StringUtil;
import org.apache.struts.upload.FormFile;
import javax.activation.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * <p> This class represents a file that was received by Turbine using
 * <code>multipart/form-data</code> POST request.
 *
 * <p> After retrieving an instance of this class from the {@link
 * org.apache.turbine.util.ParameterParser ParameterParser} (see
 * {@link org.apache.turbine.util.ParameterParser#getFileItem(String)
 * ParameterParser.getFileItem(String)} and {@link
 * org.apache.turbine.util.ParameterParser#getFileItems(String)
 * ParameterParser.getFileItems(String)}) you can use it to acces the
 * data that was sent by the browser.  You may either request all
 * contents of file at once using {@link #get()} or request an {@link
 * java.io.InputStream InputStream} with {@link #getStream()} and
 * process the file without attempting to load it into memory, which
 * may come handy with large files.
 *
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @version $Id: FileItem.java 3 2006-03-01 11:17:08Z gpolancic $
 */
public class FileItem implements FormFile, DataSource {

    /**
     * The maximal size of request that will have it's elements stored
     * in memory.
     */
    public static final int DEFAULT_UPLOAD_SIZE_THRESHOLD = 10240;

    /** The original filename in the user's filesystem. */
    protected String fileName;

    /**
     * The content type passed by the browser or <code>null</code> if
     * not defined.
     */
    protected String contentType;

    /** Cached contents of the file. */
    protected byte[] content;

    /** Temporary storage location. */
    protected File storeLocation = null;

    /** Temporary storage for in-memory files. */
    protected ByteArrayOutputStream byteStream;

    /**
     * Constructs a new <code>FileItem</code>.
     *
     * <p>Use {@link #newInstance(String,String,String,int)} to
     * instantiate <code>FileItems</code>.
     *
     * @param fileName The original filename in the user's filesystem.
     * @param contentType The content type passed by the browser or
     * <code>null</code> if not defined.
     */
    protected FileItem(String fileName, String contentType) {
        this.fileName = fileName;
        this.contentType = contentType;
    }

    /**
     * Returns the original filename in the user's filesystem.
     *
     * @return The original filename in the user's filesystem.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the content type passed by the browser or
     * <code>null</code> if not defined.
     *
     * @return The content type passed by the browser or
     * <code>null</code> if not defined.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Provides a hint if the file contents will be read from memory.
     *
     * @return <code>True</code> if the file contents will be read
     * from memory.
     */
    public boolean inMemory() {
        return (content != null || byteStream != null);
    }

    /**
     * Returns the size of the file.
     *
     * @return The size of the file.
     */
    public long getSize() {
        if (storeLocation != null) {
            return storeLocation.length();
        } else if (byteStream != null) {
            return byteStream.size();
        } else {
            return content.length;
        }
    }

    /**
     * Returns the contents of the file as an array of bytes.  If the
     * contents of the file were not yet cached int the memory, they
     * will be loaded from the disk storage and chached.
     *
     * @return The contents of the file as an array of bytes.
     */
    public byte[] get() {
        if (content == null) {
            if (storeLocation != null) {
                content = new byte[(int) getSize()];
                try {
                    FileInputStream fis = new FileInputStream(storeLocation);
                    fis.read(content);
                } catch (Exception e) {
                    content = null;
                }
            } else {
                content = byteStream.toByteArray();
                byteStream = null;
            }
        }
        return content;
    }

    /**
     * Returns the contents of the file as a String, using default
     * encoding.  This method uses {@link #get()} to retrieve the
     * contents of the file.
     *
     * @return The contents of the file.
     */
    public String getString() {
        return new String(get());
    }

    /**
     * Returns the contents of the file as a String, using specified
     * encoding.  This method uses {@link #get()} to retireve the
     * contents of the file.<br>
     *
     * @param encoding The encoding to use.
     * @return The contents of the file.
     * @exception UnsupportedEncodingException
     */
    public String getString(String encoding) throws UnsupportedEncodingException {
        return new String(get(), encoding);
    }

    /**
     * Returns an {@link java.io.InputStream InputStream} that can be
     * used to retrieve the contents of the file.
     *
     * @return An {@link java.io.InputStream InputStream} that can be
     * used to retrieve the contents of the file.
     * @exception Exception A generic exception.
     */
    public InputStream getStream() throws Exception {
        if (content == null) {
            if (storeLocation != null) {
                try {
                    return new FileInputStream(storeLocation);
                } catch (FileNotFoundException e) {
                    throw new Exception("FileItem: stored item was lost");
                }
            } else {
                content = byteStream.toByteArray();
                byteStream = null;
            }
        }
        return new ByteArrayInputStream(content);
    }

    /**
     * Returns the {@link java.io.File} objects for the FileItems's
     * data temporary location on the disk.  Note that for
     * <code>FileItems</code> that have their data stored in memory
     * this method will return <code>null</code>.  When handling large
     * files, you can use {@link java.io.File#renameTo(File)} to
     * move the file to new location without copying the data, if the
     * source and destination locations reside within the same logical
     * volume.
     *
     * @return A File.
     */
    public File getStoreLocation() {
        return storeLocation;
    }

    /**
     * Returns an {@link java.io.OutputStream OutputStream} that can
     * be used for storing the contensts of the file.
     *
     * @return an {@link java.io.OutputStream OutputStream} that can be
     * used for storing the contensts of the file.
     * @exception IOException
     */
    public OutputStream getOutputStream() throws IOException {
        if (storeLocation == null) {
            return byteStream;
        } else {
            return new FileOutputStream(storeLocation);
        }
    }

    /**
     * Instantiates a FileItem.  It uses <code>requestSize</code> to
     * decide what temporary storage approach the new item should
     * take.  The largest request that will have its items cached in
     * memory can be configured in
     * <code>TurbineResources.properties</code> in the entry named
     * <code>file.upload.size.threshold</code>
     *
     * @param path A String.
     * @param name The original filename in the user's filesystem.
     * @param contentType The content type passed by the browser or
     * <code>null</code> if not defined.
     * @param requestSize The total size of the POST request this item
     * belongs to.
     * @param storeAsFile Set to true if you want it stored as a local file.
     * @return A FileItem.
     */
    public static FileItem newInstance(String path, String name, String contentType, int requestSize, boolean storeAsFile) {
        FileItem item = new FileItem(name, contentType);
        if (storeAsFile) {
            try {
                name = StringUtil.replace(name, "\\", "/");
                File tmpFile = new File(name);
                File tempDir = new File(path);
                tempDir.mkdirs();
                item.storeLocation = File.createTempFile("upload", "-" + tmpFile.getName(), tempDir);
            } catch (IOException de) {
                throw new IllegalArgumentException("Unable to create upload temp file " + de.getMessage());
            }
        } else {
            item.byteStream = new ByteArrayOutputStream();
        }
        return item;
    }

    /**
     * Set the content type for this file
     * @param contentType The content type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Get the size of this file
     * @return An int representing the size of the file in bytes
     */
    public int getFileSize() {
        return new Long(getSize()).intValue();
    }

    /**
     * Set the file size
     * @param filesize An int reprsenting the size of the file in bytes
     */
    public void setFileSize(int filesize) {
    }

    /**
     * Set the filename of this file
     * @param newFileName The name of the file
     */
    public void setFileName(String newFileName) {
        fileName = newFileName;
    }

    /**
     * Get the data in byte array for for this file.  Note that this can be
     * a very hazardous method, files can be large enough to cause
     * OutOfMemoryErrors.  Short of being deprecated, it's strongly recommended
     * that you use {@link #getInputStream() getInputStream} to get the file
     * data.
     *
     * @exception FileNotFoundException If some sort of file representation
     *                                  cannot be found for the FormFile
     * @exception IOException If there is some sort of IOException
     * @return An array of bytes representing the data contained
     *         in the form file
     */
    public byte[] getFileData() throws FileNotFoundException, IOException {
        byte[] b = null;
        getInputStream().read(b);
        return b;
    }

    /**
     * Get an InputStream that represents this file.  This is the preferred
     * method of getting file data.
     * @exception FileNotFoundException If some sort of file representation
     *                                  cannot be found for the FormFile
     * @exception IOException If there is some sort of IOException
     * @return an InputStream object.
     */
    public InputStream getInputStream() throws FileNotFoundException, IOException {
        return new FileInputStream(getFileName());
    }

    /**
     * Destroy all content for this form file.
     * Implementations should remove any temporary
     * files or any temporary file data stored somewhere
     */
    public void destroy() {
        File theFile = new File(getFileName());
        theFile.delete();
    }

    /**
     * Returns the name of the FileItem
     * @return java.lang.String
     */
    public String getName() {
        return this.fileName;
    }
}
