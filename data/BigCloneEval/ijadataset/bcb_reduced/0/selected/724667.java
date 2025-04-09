package org.pixory.pxfoundation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Enumeration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

public class PXZip extends Object {

    private static final Log LOG = LogFactory.getLog(PXZip.class);

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private static PXZip _instance;

    public static PXZip getInstance() {
        if (_instance == null) {
            _instance = new PXZip();
        }
        return _instance;
    }

    private PXZip() {
    }

    /**
	 * unzip the sourceFile into the targetDirectory . N.B. this does *not* make
	 * any attempt to preserve any path information: the unzipped files are
	 * placed directly into (beneath) the targetDirectory. duplicate filenames
	 * means clobbering, where last one in wins. If an error is encountered in
	 * unzipping an entry, this method will not stop, but keep going until it has
	 * tried all entries. Then at the end it will throw if there were any errors
	 * 
	 * @return the number of files extracted from the archive
	 */
    public int unzip(PXJob.StatusCheck statusCheck, File sourceFile, File targetDirectory) throws IOException, InterruptedException {
        int unzip = 0;
        if ((sourceFile != null) && (targetDirectory != null)) {
            ZipFile aZipFile = new ZipFile(sourceFile, "UTF-8");
            Enumeration anEntryEnumeration = aZipFile.getEntries();
            IOException aFirstException = null;
            if (!targetDirectory.isDirectory()) {
                targetDirectory.mkdir();
            }
            while (anEntryEnumeration.hasMoreElements()) {
                if (statusCheck != null) {
                    statusCheck.check();
                }
                ZipEntry aZipEntry = (ZipEntry) anEntryEnumeration.nextElement();
                String aFilePath = aZipEntry.getName();
                long aFileSize = aZipEntry.getSize();
                String aFileName = PXPathUtility.lastPathComponent(aFilePath, "/");
                File anOutfile = new File(targetDirectory, aFileName);
                try {
                    InputStream aZipInputStream = aZipFile.getInputStream(aZipEntry);
                    unzipToFile(aZipInputStream, anOutfile);
                    unzip++;
                } catch (IOException anException) {
                    LOG.warn(null, anException);
                    if (aFirstException == null) {
                        aFirstException = anException;
                    }
                }
            }
            aZipFile.close();
            if (aFirstException != null) {
                throw aFirstException;
            }
        } else {
            throw new IllegalArgumentException("PXZip.unzip() does not accept null args");
        }
        return unzip;
    }

    /**
	 * N.B. the zip file format specification does not provide a way to specify
	 * the character encoding used for filenames-- the assumption is that they
	 * are ASCII. So this method converts all Unicode filenames to ASCII
	 * 
	 * @return the zip archive
	 */
    public File zip(PXJob.StatusCheck statusCheck, File[] sourceFiles, File archive) throws IOException, InterruptedException {
        File zip = null;
        if ((sourceFiles != null) && (sourceFiles.length > 0) && (archive != null)) {
            FileOutputStream aFileOutStream = new FileOutputStream(archive);
            BufferedOutputStream anOutStream = new BufferedOutputStream(aFileOutStream);
            ZipOutputStream aZipOut = new ZipOutputStream(anOutStream);
            int aPrefixSize = ((int) (Math.log(sourceFiles.length) / Math.log(10.0))) + 1;
            DecimalFormat aPrefixFormat = new DecimalFormat();
            aPrefixFormat.setMaximumIntegerDigits(aPrefixSize);
            aPrefixFormat.setGroupingUsed(false);
            for (int i = 0; i < sourceFiles.length; i++) {
                if (statusCheck != null) {
                    statusCheck.check();
                }
                String aPrefix = aPrefixFormat.format(i);
                File aFile = sourceFiles[i];
                String aFileName = aFile.getName();
                String anEntryName = aPrefix + "_" + PXStringUtility.toAscii(aFileName);
                addZipEntry(aZipOut, aFile, anEntryName);
            }
            aZipOut.close();
            zip = archive;
        } else {
            throw new IllegalArgumentException("PXZip.unzip() does not accept null args");
        }
        return zip;
    }

    /**
	 * if entryName is not null, then the directory entry in the archive uses
	 * that name. Otherwise, the filename from file argument is used
	 */
    private static boolean addZipEntry(ZipOutputStream zipOutputStream, File file, String entryName) throws IOException {
        boolean addZipEntry = false;
        if ((zipOutputStream != null) && (file != null) && (file.isFile())) {
            BufferedInputStream anInStream = null;
            try {
                FileInputStream aFileStream = new FileInputStream(file);
                anInStream = new BufferedInputStream(aFileStream);
                String anEntryName = entryName;
                if (anEntryName == null) {
                    anEntryName = file.getName();
                }
                ZipEntry aZipEntry = new ZipEntry(anEntryName);
                zipOutputStream.putNextEntry(aZipEntry);
                int aBytesRead = -1;
                byte[] aBuffer = new byte[DEFAULT_BUFFER_SIZE];
                while ((aBytesRead = anInStream.read(aBuffer, 0, aBuffer.length)) != -1) {
                    zipOutputStream.write(aBuffer, 0, aBytesRead);
                }
                addZipEntry = true;
            } catch (Exception anException) {
                LOG.warn(null, anException);
            } finally {
                if (anInStream != null) {
                    anInStream.close();
                }
                zipOutputStream.closeEntry();
            }
        }
        return addZipEntry;
    }

    /**
	 * unzips a single entry into the specified file
	 */
    private static void unzipToFile(InputStream entryStream, File destinationFile) throws IOException {
        if ((entryStream != null) && (destinationFile != null)) {
            OutputStream anOutputStream = new FileOutputStream(destinationFile);
            anOutputStream = new BufferedOutputStream(anOutputStream);
            int aBytesRead = -1;
            byte[] aBuffer = new byte[DEFAULT_BUFFER_SIZE];
            while ((aBytesRead = entryStream.read(aBuffer, 0, aBuffer.length)) != -1) {
                anOutputStream.write(aBuffer, 0, aBytesRead);
            }
            anOutputStream.close();
        } else {
            throw new IllegalArgumentException("PXZip.unzipToFile does not accept null args");
        }
    }
}
