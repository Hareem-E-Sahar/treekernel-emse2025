package org.jbjf.core;

import java.util.HashMap;
import java.util.zip.*;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * The <code>ZipItem</code> class provides a simple interface to
 * the standard Java zip/compression components.
 * 
 * <pre>
 * -------------- 
 * <b>History </b>: Begin 
 * -------------- 
 * &lt;history&gt;
 * &nbsp;&nbsp;&lt;change&gt; 
 *     1.0.0; ASL; Jul 24, 2006
 *     Initial version created and customized for the ...
 *     Naming Conventions 
 *     ------------------
 *     Scope Conventions
 *       >> g - global
 *       >> m - module/class
 *       >> l - local/method/function
 *     Variable Conventions
 *       >> str - string, text, character
 *       >> lng - integer, long, numeric
 *       >> flt - real, floating point
 *       >> the - object, class, module
 *     Examples
 *       >> lstrName - local string to contain name
 *       >> glngVerbose - global integer indicator for verbose mode
 *       >> mtheScanner - class/module for a document scanner
 * &nbsp;&nbsp;&lt;/change&gt; 
 * &lt;/history&gt; 
 * -------------- 
 * <b>History </b>: End 
 * -------------- 
 * </pre>
 * @author Adym S. Lincoln<br>
 *         Copyright (C) 2007. JBJF All
 *         rights reserved.
 * @version 1.0.0
 * @since 1.0.0
 */
public class ZipItem {

    /** 
     * Class property that contains a temporary file during zip
     * archive processing.
     */
    private String mstrTempFile;

    /** 
     * Class property that contains the path and/or filename of 
     * the zip archive.
     */
    private String mstrOutputFile;

    /** 
     * Class property that is used to transfer bytes between a
     * file and the zip archive.
     */
    private byte[] mbfrBytes;

    /** Class property that stores the zip archive object.  */
    private ZipOutputStream mzipOutput;

    /** Class property that stores the filenames in the zip archive.    */
    private HashMap mtheFilePaths;

    /**
     * Standard constructor that accepts the path and/or filename
     * of the zipfile archive.
     * 
     * @param pstrOutputFile    Path and/or filename of where to
     *                          create the zip archive.
     * @throws Exception
     */
    public ZipItem(String pstrOutputFile) throws Exception {
        mstrTempFile = pstrOutputFile.replaceAll(".zip", "") + "_temp.zip";
        this.mstrOutputFile = pstrOutputFile;
        mtheFilePaths = new HashMap();
        mbfrBytes = new byte[1024];
        mzipOutput = new ZipOutputStream(new FileOutputStream(mstrTempFile));
    }

    /**
     * Create new zip from existing zip file (for adding more
     * entries)
     * 
     * @param pstrFilename
     * @return open zip file
     * @throws Exception
     */
    public static ZipItem load(String pstrFilename) throws Exception {
        ZipItem lzipResults = new ZipItem(pstrFilename);
        ZipInputStream lzipInput = new ZipInputStream(new FileInputStream(pstrFilename));
        ZipEntry zipEntry = lzipInput.getNextEntry();
        while (zipEntry != null) {
            lzipResults.mtheFilePaths.put(zipEntry.getName(), "");
            lzipResults.addStream(zipEntry, lzipInput);
            zipEntry = lzipInput.getNextEntry();
        }
        lzipInput.close();
        return lzipResults;
    }

    /**
     * Adds a file to the zipfile.
     * 
     * @param pstrFilename      Path and/or filename to add.
     * @throws Exception    Any problems, escalate to the parent.
     */
    public void addFile(String pstrFilename) throws Exception {
        if (mtheFilePaths.get(pstrFilename) == null) {
            FileInputStream lfileInput = new FileInputStream(pstrFilename);
            addStream(new ZipEntry(pstrFilename), lfileInput);
            lfileInput.close();
            mtheFilePaths.put(pstrFilename, "");
        } else {
            throw new Exception(pstrFilename + "already exists in zip file");
        }
    }

    /**
     * Specialized add function that takes an <code>InputStream</code>
     * as a paramater instead of the tradition filename.
     * <p>
     * @param pzipEntry  A standard zipfile entry.
     * @param pfileInput        An <code>InputStream</code> object that is
     *                  allocated to the file to add.
     * @throws Exception    Any problems, escalate to the parent.
     */
    private void addStream(ZipEntry pzipEntry, InputStream pfileInput) throws Exception {
        mzipOutput.putNextEntry(pzipEntry);
        int llngLength = pfileInput.read(mbfrBytes);
        while (llngLength > 0) {
            mzipOutput.write(mbfrBytes, 0, llngLength);
            llngLength = pfileInput.read(mbfrBytes);
        }
        mzipOutput.closeEntry();
    }

    /**
     * Closes a given zip archive.
     * 
     * @throws Exception
     */
    public void close() throws Exception {
        mzipOutput.flush();
        mzipOutput.close();
        File lfileTemp = new File(mstrTempFile);
        File lfileOutput = new File(mstrOutputFile);
        lfileOutput.delete();
        lfileTemp.renameTo(lfileOutput);
    }

    /**
     * Traditional getter method that returns a <code>String[]</code>
     * object with the filenames currently in the zip archive.
     * 
     * @return String[] array of fully qualified filenames.
     */
    public String[] getFiles() {
        return (String[]) mtheFilePaths.keySet().toArray(new String[mtheFilePaths.size()]);
    }
}
