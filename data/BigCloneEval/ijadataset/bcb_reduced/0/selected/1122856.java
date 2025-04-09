package org.oclc.da.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.oclc.da.exceptions.DAException;
import org.oclc.da.exceptions.DAExceptionCodes;
import org.oclc.da.logging.Logger;

/**
 * ZipFileCreator
 * 
 * This is a class which creates a zip file when the zip filename
 * and the file is specified.
 * 
 * @author SR
 * @version 1.0,
 * @created 02/14/2007
 */
public class ZipFileCreator {

    /** Logger instance. */
    private static final Logger logger = Logger.newInstance();

    /** default constructor <p>. */
    public ZipOutputStream m_zipOutputStream;

    /** The m_zip file name. */
    public String m_zipFileName;

    /** The m_batch number. */
    public String m_batchNumber;

    /** Standard prefix for object folder. */
    private static final String OBJECT_DIR_PREFIX = "Object";

    /**
     * Instantiates a new zip file creator.
     * 
     * @param zipFileName the zip file name
     * @param batchNumber the batch number
     */
    public ZipFileCreator(String zipFileName, String batchNumber) {
        m_zipFileName = zipFileName;
        m_batchNumber = batchNumber;
    }

    /**
     * Init.
     * 
     * @throws DAException the DA exception
     */
    public void init() throws DAException {
        try {
            m_zipOutputStream = new ZipOutputStream(new FileOutputStream(m_zipFileName));
        } catch (IOException e) {
            DAException ex = new DAException(DAExceptionCodes.ERROR_CREATING, new String[] { "Error creating ZipOutputStream", m_zipFileName });
            logger.log(DAExceptionCodes.IO_ERROR, this, "addDir", "Error while adding file to zip", ex);
            logger.log(this, "init", null, ex);
            throw ex;
        }
    }

    /**
     * Close.
     * 
     * @throws DAException the DA exception
     */
    public void close() throws DAException {
        if (m_zipOutputStream != null) {
            try {
                m_zipOutputStream.close();
            } catch (IOException e) {
                DAException ex = new DAException(DAExceptionCodes.CLOSING_ERROR, new String[] { "Error closing ZipOutputStream", m_zipFileName });
                logger.log(this, "close", null, ex);
                throw ex;
            }
        }
    }

    /**
     * Add a file to the zip
     * The ZipOutputStream should be created as
     * ZipOutputStream out = new ZipOutputStream(new FileOutputStream(m_zipFileName));
     * The object id should be null in case not present.
     * 
     * @param file the file
     * @param objectId the object id
     * 
     * @throws DAException the DA exception
     */
    public void addFile(File file, String objectId) throws DAException {
        try {
            String entryString = "";
            FileInputStream in = new FileInputStream(file.getAbsolutePath());
            System.out.println(" Adding: " + file.getAbsolutePath());
            if (objectId != null) {
                entryString = m_batchNumber + File.separator + OBJECT_DIR_PREFIX + objectId + File.separator + file.getName();
            } else {
                entryString = m_batchNumber + File.separator + file.getName();
            }
            m_zipOutputStream.putNextEntry(new ZipEntry(entryString));
            byte[] tmpBuf = new byte[1024];
            int len;
            while ((len = in.read(tmpBuf)) > 0) {
                m_zipOutputStream.write(tmpBuf, 0, len);
            }
            m_zipOutputStream.closeEntry();
            in.close();
        } catch (IOException ioEx) {
            DAException ex = new DAException(DAExceptionCodes.IO_ERROR, new String[] { ioEx.getMessage(), file.getAbsolutePath() });
            logger.log(DAExceptionCodes.IO_ERROR, this, "addFile", "Error while adding file to zip", ex);
            throw ex;
        }
    }

    /**
     * Add files from the directory to the zip
     * The ZipOutputStream should be created as
     * ZipOutputStream out = new ZipOutputStream(new FileOutputStream(m_zipFileName));.
     * 
     * @param dirObj the dir obj
     * 
     * @throws DAException the DA exception
     */
    public void addDir(File dirObj) throws DAException {
        try {
            if (!dirObj.isDirectory()) {
                DAException ex = new DAException(DAExceptionCodes.IO_ERROR, new String[] { "Input is not a directory" });
                logger.log(this, "addDir", null, ex);
                throw ex;
            }
            File[] files = dirObj.listFiles();
            byte[] tmpBuf = new byte[1024];
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    addDir(files[i]);
                    continue;
                }
                FileInputStream in = new FileInputStream(files[i].getAbsolutePath());
                System.out.println(" Adding: " + files[i].getAbsolutePath());
                m_zipOutputStream.putNextEntry(new ZipEntry(files[i].getAbsolutePath()));
                int len;
                while ((len = in.read(tmpBuf)) > 0) {
                    m_zipOutputStream.write(tmpBuf, 0, len);
                }
                m_zipOutputStream.closeEntry();
                in.close();
            }
            m_zipOutputStream.close();
        } catch (IOException ioExcp) {
            DAException ex = new DAException(DAExceptionCodes.IO_ERROR, new String[] { ioExcp.getMessage() });
            logger.log(DAExceptionCodes.IO_ERROR, this, "addDir", "Error while adding file to zip", ex);
            throw ex;
        }
    }

    /**
     * The main method.
     * 
     * @param args the arguments
     */
    public static void main(String[] args) {
    }
}
