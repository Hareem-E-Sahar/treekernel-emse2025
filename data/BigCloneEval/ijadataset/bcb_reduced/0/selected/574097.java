package net.sourceforge.jaulp.file.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import net.sourceforge.jaulp.file.exceptions.FileDoesNotExistException;
import net.sourceforge.jaulp.file.search.FileSearchUtils;
import net.sourceforge.jaulp.io.StreamUtils;

/**
 * The Class Zipper.
 * 
 * @version 1.0
 * @author Asterios Raptis
 */
public class Zipper implements ZipModel {

    /** The zos. */
    private ZipOutputStream zos = null;

    /** The file length. */
    private long fileLength;

    /** The file counter. */
    private int fileCounter;

    /** The zip level. */
    private int zipLevel = -1;

    /** The compression method. */
    private int compressionMethod = -1;

    /** The zip file comment. */
    private String zipFileComment;

    /** The zip file name. */
    private String zipFileName;

    /** The directory to zip. */
    private File directoryToZip;

    /** The file filter. */
    private FilenameFilter fileFilter;

    /** The zip file. */
    private File zipFile;

    /** The zip file obj. */
    private ZipFile zipFileObj = null;

    /** The dir to start. */
    private String dirToStart;

    /**
     * Instantiates a new zipper.
     * 
     * @param dirToZip
     *            the dir to zip
     * @param zipFile
     *            the zip file
     */
    public Zipper(final File dirToZip, final File zipFile) {
        this.initialize(dirToZip, zipFile, null);
    }

    /**
     * Instantiates a new zipper.
     *
     * @param dirToZip the dir to zip
     * @param zipFile the zip file
     * @param filter the filter
     */
    public Zipper(final File dirToZip, final File zipFile, final FilenameFilter filter) {
        this.initialize(dirToZip, zipFile, filter);
    }

    /**
     * Adds the file.
     * 
     * @param file
     *            the file
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void addFile(final File file) throws IOException {
        this.fileLength += file.length();
        this.fileCounter++;
        final String absolutePath = file.getAbsolutePath();
        final int index = absolutePath.indexOf(this.dirToStart);
        final String zipEntryName = absolutePath.substring(index, absolutePath.length());
        final byte[] b = new byte[(int) (file.length())];
        final ZipEntry cpZipEntry = new ZipEntry(zipEntryName);
        this.zos.putNextEntry(cpZipEntry);
        this.zos.write(b, 0, (int) file.length());
        this.zos.closeEntry();
    }

    /**
     * Gets the compression method.
     *
     * @return the compression method
     * {@inheritDoc}
     * @see net.sourceforge.jaulp.file.zip.ZipModel#getCompressionMethod()
     */
    public int getCompressionMethod() {
        return this.compressionMethod;
    }

    /**
     * Gets the directory to zip.
     *
     * @return the directory to zip
     * {@inheritDoc}
     * @see net.sourceforge.jaulp.file.zip.ZipModel#getDirectoryToZip()
     */
    public File getDirectoryToZip() {
        return this.directoryToZip;
    }

    /**
     * Gets the dir to start.
     *
     * @return the dir to start
     * {@inheritDoc}
     * @see net.sourceforge.jaulp.file.zip.ZipModel#getDirToStart()
     */
    public String getDirToStart() {
        return this.dirToStart;
    }

    /**
     * Gets the file counter.
     *
     * @return the file counter
     * {@inheritDoc}
     * @see net.sourceforge.jaulp.file.zip.ZipModel#getFileCounter()
     */
    public int getFileCounter() {
        return this.fileCounter;
    }

    /**
     * Gets the file filter.
     *
     * @return the file filter
     * {@inheritDoc}
     * @see net.sourceforge.jaulp.file.zip.ZipModel#getFileFilter()
     */
    public FilenameFilter getFileFilter() {
        return this.fileFilter;
    }

    /**
     * Gets the file length.
     *
     * @return the file length
     * {@inheritDoc}
     * @see net.sourceforge.jaulp.file.zip.ZipModel#getFileLength()
     */
    public long getFileLength() {
        return this.fileLength;
    }

    /**
     * Gets the zip file.
     *
     * @return the zip file
     * {@inheritDoc}
     * @see net.sourceforge.jaulp.file.zip.ZipModel#getZipFile()
     */
    public File getZipFile() {
        return this.zipFile;
    }

    /**
     * Gets the zip file comment.
     *
     * @return the zip file comment
     * {@inheritDoc}
     * @see net.sourceforge.jaulp.file.zip.ZipModel#getZipFileComment()
     */
    public String getZipFileComment() {
        return this.zipFileComment;
    }

    /**
     * Gets the zip file name.
     *
     * @return the zip file name
     * {@inheritDoc}
     * @see net.sourceforge.jaulp.file.zip.ZipModel#getZipFileName()
     */
    public String getZipFileName() {
        return this.zipFileName;
    }

    /**
     * Returns the field <code>zipFileObj</code>.
     *
     * @return The field .
     */
    public ZipFile getZipFileObj() {
        return this.zipFileObj;
    }

    /**
     * Gets the zip level.
     *
     * @return the zip level
     * {@inheritDoc}
     * @see net.sourceforge.jaulp.file.zip.ZipModel#getZipLevel()
     */
    public int getZipLevel() {
        return this.zipLevel;
    }

    /**
     * Initialize.
     * 
     * @param dirToZip
     *            the dir to zip.
     * @param zipFile
     *            the zip file.
     * @param filter
     *            the filter.
     */
    private void initialize(final File dirToZip, final File zipFile, final FilenameFilter filter) {
        this.directoryToZip = dirToZip;
        this.dirToStart = this.directoryToZip.getName();
        this.zipFile = zipFile;
        this.fileLength = 0;
        this.fileCounter = 0;
        this.setFileFilter(filter);
    }

    /**
     * Sets the compression method.
     *
     * @param compressionMethod the new compression method
     * {@inheritDoc}
     * @see net.sourceforge.jaulp.file.zip.ZipModel#setCompressionMethod(int)
     */
    public void setCompressionMethod(final int compressionMethod) {
        this.compressionMethod = compressionMethod;
    }

    /**
     * Sets the directory to zip.
     *
     * @param directoryToZip the new directory to zip
     * {@inheritDoc}
     * @see net.sourceforge.jaulp.file.zip.ZipModel#setDirectoryToZip(java.io.File)
     */
    public void setDirectoryToZip(final File directoryToZip) {
        this.directoryToZip = directoryToZip;
    }

    /**
     * Sets the dir to start.
     *
     * @param dirToStart the new dir to start
     * {@inheritDoc}
     * @see net.sourceforge.jaulp.file.zip.ZipModel#setDirToStart(java.lang.String)
     */
    public void setDirToStart(final String dirToStart) {
        this.dirToStart = dirToStart;
    }

    /**
     * Sets the file counter.
     *
     * @param fileCounter the new file counter
     * {@inheritDoc}
     * @see net.sourceforge.jaulp.file.zip.ZipModel#setFileCounter(int)
     */
    public void setFileCounter(final int fileCounter) {
        this.fileCounter = fileCounter;
    }

    /**
     * Sets the file filter.
     *
     * @param fileFilter the new file filter
     * {@inheritDoc}
     * @see net.sourceforge.jaulp.file.zip.ZipModel#setFileFilter(java.io.FilenameFilter)
     */
    public void setFileFilter(final FilenameFilter fileFilter) {
        this.fileFilter = fileFilter;
    }

    /**
     * Sets the file length.
     *
     * @param fileLength the new file length
     * {@inheritDoc}
     * @see net.sourceforge.jaulp.file.zip.ZipModel#setFileLength(long)
     */
    public void setFileLength(final long fileLength) {
        this.fileLength = fileLength;
    }

    /**
     * Sets the zip file.
     *
     * @param zipFile the new zip file
     * {@inheritDoc}
     * @see net.sourceforge.jaulp.file.zip.ZipModel#setZipFile(java.io.File)
     */
    public void setZipFile(final File zipFile) {
        this.zipFile = zipFile;
    }

    /**
     * Sets the zip file comment.
     *
     * @param zipFileComment the new zip file comment
     * {@inheritDoc}
     * @see net.sourceforge.jaulp.file.zip.ZipModel#setZipFileComment(java.lang.String)
     */
    public void setZipFileComment(final String zipFileComment) {
        this.zipFileComment = zipFileComment;
    }

    /**
     * Sets the zip file name.
     *
     * @param zipFileName the new zip file name
     * {@inheritDoc}
     * @see net.sourceforge.jaulp.file.zip.ZipModel#setZipFileName(java.lang.String)
     */
    public void setZipFileName(final String zipFileName) {
        this.zipFileName = zipFileName;
    }

    /**
     * Sets the field <code>zipFileObj</code>.
     * 
     * @param zipFileObj
     *            The <code>zipFileObj</code> to set
     */
    public void setZipFileObj(final ZipFile zipFileObj) {
        this.zipFileObj = zipFileObj;
    }

    /**
     * Sets the zip level.
     *
     * @param zipLevel the new zip level
     * {@inheritDoc}
     * @see net.sourceforge.jaulp.file.zip.ZipModel#setZipLevel(int)
     */
    public void setZipLevel(final int zipLevel) {
        this.zipLevel = zipLevel;
    }

    /**
     * Zip.
     */
    public void zip() {
        FileOutputStream fos = null;
        try {
            if (!this.directoryToZip.exists()) {
                throw new IOException("Directory with the name " + this.directoryToZip.getName() + " does not exist.");
            }
            if (!this.zipFile.exists()) {
                throw new FileDoesNotExistException("Zipfile with the name " + this.zipFile.getName() + " does not exist.");
            }
            fos = new FileOutputStream(this.zipFile);
            this.zos = new ZipOutputStream(fos);
            if (0 < this.zipLevel) {
                this.zos.setLevel(this.zipLevel);
            } else {
                this.zos.setLevel(9);
            }
            if (null != this.zipFileComment) {
                this.zos.setComment(this.zipFileComment);
            }
            if (0 < this.compressionMethod) {
                this.zos.setMethod(this.compressionMethod);
            }
            this.zipFiles(this.directoryToZip);
            this.zos.flush();
            this.zos.finish();
            this.zos.close();
            fos.flush();
            fos.close();
            fos = null;
            this.zos = null;
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            StreamUtils.closeOutputStream(fos);
            StreamUtils.closeOutputStream(this.zos);
        }
    }

    /**
     * Zip files.
     * 
     * @param file
     *            the file
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void zipFiles(final File file) throws IOException {
        if (file.isDirectory()) {
            File[] fList;
            List<File> foundedFiles;
            if (null != this.fileFilter) {
                final File[] tmpfList = file.listFiles(this.fileFilter);
                final List<File> foundedDirs = FileSearchUtils.listDirs(file);
                if (0 < foundedDirs.size()) {
                    final List<File> tmp = Arrays.asList(tmpfList);
                    foundedDirs.addAll(tmp);
                    foundedFiles = foundedDirs;
                } else {
                    final List<File> tmp = Arrays.asList(tmpfList);
                    foundedFiles = tmp;
                }
            } else {
                fList = file.listFiles();
                final List<File> tmp = Arrays.asList(fList);
                foundedFiles = tmp;
            }
            for (int i = 0; i < foundedFiles.size(); i++) {
                this.zipFiles(foundedFiles.get(i));
            }
        } else {
            this.addFile(file);
        }
    }
}
