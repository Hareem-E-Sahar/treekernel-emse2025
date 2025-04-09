package ro.codemart.installer.core.utils.file;

import ro.codemart.commons.StreamHelper;
import ro.codemart.installer.core.I18nKey;
import ro.codemart.installer.core.InstallerException;
import ro.codemart.installer.core.operation.AbstractLongOperation;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.List;
import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 * This operation creates a ZIP file from the given folder
 *
 * @author marius.ani
 */
public class ZipOperation extends AbstractLongOperation {

    private static final Logger log = Logger.getLogger(ZipOperation.class);

    public static final String ZIP_MESSAGE = "ZipMessage";

    public static final String DEFAULT_ZIP_MESSAGE = "Compressing file {0} to {1}";

    public static final String ZIP_INFO = "ZipInfo";

    public static final String DEFAULT_ZIP_INFO = "Compressing file";

    /**
     * The folder to be compressed
     */
    protected File sourceFile;

    /**
     * The destination zip file
     */
    protected File destZip;

    /**
     * Useful when filtering the files to be added to the archive
     */
    protected FileFilter fileFilter;

    /**
     * The list of excluded files. The path of these files is relative to the source file
     */
    protected List<String> excludedFiles = new ArrayList<String>();

    /**
     * The source input stream to be compressed
     */
    protected InputStream inputStream;

    /**
     * The folder - source size in bytes
     */
    private long size;

    public ZipOperation(File f, File destZip) {
        this.sourceFile = f;
        this.destZip = destZip;
    }

    /**
     * Creates a zip operation
     *
     * @param is      source input stream
     * @param destZip destination archive
     */
    public ZipOperation(InputStream is, File destZip) {
        inputStream = is;
        this.destZip = destZip;
    }

    /**
     * Creates a zip operation
     *
     * @param f             the source file to be archived
     * @param destZip       the destination archive
     * @param excludedFiles the list of names for the excluded files. The name of excluded files is relative to source
     */
    public ZipOperation(File f, File destZip, List<String> excludedFiles) {
        this(f, destZip);
        if (excludedFiles != null) {
            this.excludedFiles = excludedFiles;
        }
    }

    /**
     * Creates a zip operation
     *
     * @param id      operation id
     * @param f       file source, to be archived
     * @param destZip the destination archive
     */
    public ZipOperation(String id, File f, File destZip) {
        super(id);
        this.sourceFile = f;
        this.destZip = destZip;
    }

    /**
     * Creates a zip operation
     *
     * @param id      operation id
     * @param is      source input stream to be archived
     * @param destZip the destination archive
     */
    public ZipOperation(String id, InputStream is, File destZip) {
        super(id);
        this.inputStream = is;
        this.destZip = destZip;
    }

    /**
     * Creates a zip operation
     *
     * @param id            the unique id of the operation
     * @param f             the source file to be archived
     * @param destZip       the destination archive
     * @param excludedFiles the list of names for the excluded files. The name of excluded files  is relative to source
     */
    public ZipOperation(String id, File f, File destZip, List<String> excludedFiles) {
        this(id, f, destZip);
        if (excludedFiles != null) {
            this.excludedFiles = excludedFiles;
        }
    }

    public void execute() throws InstallerException {
        ZipOutputStream zipOutputStream = null;
        try {
            zipOutputStream = createZipOutputStream();
            if (sourceFile != null) {
                if (!sourceFile.exists()) {
                    throw new InstallerException(InstallerException.FILE_NOT_FOUND_ERROR, new Object[] { sourceFile.getAbsolutePath() });
                }
                setDescription(new I18nKey(ZIP_MESSAGE, DEFAULT_ZIP_MESSAGE), getLanguage(), sourceFile.getName(), destZip.getName());
                setInfo(new I18nKey(ZIP_INFO, DEFAULT_ZIP_INFO), getLanguage(), sourceFile.getName(), destZip.getName());
                size = sourceFile.isDirectory() ? FileActionUtilities.getFolderSize(sourceFile) : sourceFile.length();
                try {
                    if (sourceFile.isFile()) {
                        compressFile(zipOutputStream, sourceFile.getParentFile().getCanonicalPath(), getLanguage(), sourceFile);
                    } else if (sourceFile.isDirectory()) {
                        compress(zipOutputStream, sourceFile, sourceFile.getCanonicalPath(), getLanguage());
                    }
                } catch (FileNotFoundException e) {
                    log.error(e.getMessage(), e);
                    throw new InstallerException(InstallerException.FILE_NOT_FOUND_ERROR, new Object[] { destZip.getAbsolutePath() }, e);
                }
            } else {
                size = inputStream.available();
                copyToZipStream(inputStream, zipOutputStream);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new InstallerException(InstallerException.IO_ERROR, new Object[] { e.getMessage() }, e.getCause());
        } finally {
            StreamHelper.silentlyCloseStream(zipOutputStream);
        }
    }

    /**
     * Creates the outputstream to be used for writing the data and compressing it
     *
     * @return the output stream
     * @throws IOException if any error
     */
    protected ZipOutputStream createZipOutputStream() throws IOException {
        return new ZipOutputStream(new FileOutputStream(destZip));
    }

    /**
     * Adds the contents of the given folder to the ZIP output stream
     *
     * @param zipOutputStream the destination zip stream
     * @param folder          the folder whose content is to be added to the zip
     * @param basePath        the base path
     * @param lang            the locale. Would be something like "en", "ro", "de"
     * @throws IOException if any errors
     */
    private void compress(ZipOutputStream zipOutputStream, File folder, String basePath, String lang) throws IOException {
        File[] files = folder.listFiles(getFileFilter());
        for (File file : files) {
            if (file.isFile()) {
                compressFile(zipOutputStream, basePath, lang, file);
            } else if (file.isDirectory()) {
                String entryName = getEntryName(basePath, file) + "/";
                ZipEntry zipEntry = createEntry(entryName);
                zipEntry.setTime(file.lastModified());
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.flush();
                zipOutputStream.closeEntry();
                compress(zipOutputStream, file, basePath, lang);
            }
        }
    }

    /**
     * Adds a single file to the ZIP output stream
     *
     * @param zipOutputStream the destination zip stream
     * @param basePath        the base path
     * @param lang            the locale. Would be something like "en", "ro", "de"
     * @param file            the file to be added
     * @throws IOException if any errors
     */
    private void compressFile(ZipOutputStream zipOutputStream, String basePath, String lang, File file) throws IOException {
        String entryName = getEntryName(basePath, file);
        setDescription(new I18nKey(ZIP_MESSAGE, DEFAULT_ZIP_MESSAGE), lang, file.getName(), destZip.getName());
        setInfo(new I18nKey(ZIP_INFO, DEFAULT_ZIP_INFO), lang);
        ZipEntry zipEntry = createEntry(entryName);
        zipEntry.setTime(file.lastModified());
        zipOutputStream.putNextEntry(zipEntry);
        FileInputStream is = new FileInputStream(file);
        copyToZipStream(is, zipOutputStream);
        zipOutputStream.flush();
        zipOutputStream.closeEntry();
    }

    /**
     * Copies a source stream into a destination stream
     *
     * @param is source stream
     * @param zipOutputStream destination stream
     * @throws IOException if any errors
     */
    private void copyToZipStream(InputStream is, ZipOutputStream zipOutputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            zipOutputStream.write(buffer, 0, bytesRead);
            setCompletedPercentage(getCompletedPercentage() + bytesRead / size);
        }
        is.close();
    }

    /**
     * Returns the relative entry name of the file to be added to the archive
     *
     * @param basePath the absolute path of the file
     * @param file the file to be added
     * @return the relative name of the file to be added
     * @throws IOException if canonical path could not be created
     */
    protected String getEntryName(String basePath, File file) throws IOException {
        String entry = file.getCanonicalPath().substring(basePath.endsWith("/") || basePath.endsWith("\\") ? basePath.length() : basePath.length() + 1);
        return entry.replaceAll("\\\\", "/");
    }

    /**
     * Creates a ZipEnty  with the given name
     * 
     * @param entryName
     * @return
     */
    protected ZipEntry createEntry(String entryName) {
        ZipEntry zipEntry = new ZipEntry(entryName);
        return zipEntry;
    }

    /**
     * Accepts the entries to be added to archive
     *
     * @return the fileFilter
     */
    private FileFilter getFileFilter() {
        if (fileFilter == null) {
            fileFilter = new FileFilter() {

                public boolean accept(File pathname) {
                    return acceptsEntry(pathname);
                }
            };
        }
        return fileFilter;
    }

    /**
     * Filters the files to be added to the archive.
     * 
     * @param pathName the file to be added
     * @return <code>true</code> if the file can be added
     */
    protected boolean acceptsEntry(File pathName) {
        try {
            String entryName = getEntryName(sourceFile.getCanonicalPath(), pathName);
            return !excludedFiles.contains(entryName);
        } catch (IOException e) {
            return false;
        }
    }
}
