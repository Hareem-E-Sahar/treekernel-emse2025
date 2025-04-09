package org.jbjf.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.jbjf.core.AbstractTask;
import java.text.SimpleDateFormat;

/**
 * <p>
 * The <code>BackupDirectory</code> is a JBJF task class that will 
 * zip backup a given directory and sub-directories to a target 
 * directory.  The source and backup directories are supplied via
 * traditional JBJF &lt;resource&gt; elements along with a backup
 * filename pattern.  Optional date/time stamps, relative versus
 * absolute directory paths and whether to traverse the directory 
 * tree can also be indicated via &lt;resource&gt; elements.
 * </p>
 * <p>
 * The task is available for sub-tasking, so all the &lt;resource&gt;
 * elements are pre-processed in the <code>initTask()</code> method override.  The
 * task provides getter()/setter() methods for all the class properties
 * that are used in the <code>runTask()</code> method.
 * </p>
 * <p>
 * <h3>Dependencies:</h3>
 * <ul>
 * <li>JBJF 1.3.2(+)</li>
 * <li>JRE/JDK 6(+)</li>
 * </ul>
 * <h3>Resources:</h3>
 * <code>BackupDirectory</code> depends on the following &lt;resource&gt; 
 * elements and/or class properties to function correctly:
 * <ul>
 * <li>source-directory - The top level directory where the files/contents
 * to backup are located.  The XML element should contain
 * an absolute/relative directory path ( /usr/apps/my-app, D:\\usr\\apps\\my-app ).
 * </li>
 * <li>backup-directory - The top level directory where you wish
 * to place the backup zipfile.    The XML element should contain
 * an absolute/relative directory path ( /usr/apps/my-app, D:\\usr\\apps\\my-app ).
 * </li>
 * <li>
 * backup-filename - The filename to save as the zipfile.  When 
 * a datestamp &lt;resource&gt; is supplied, the date/time stamp is
 * appended to the end of the filename, but before the *.zip file
 * extension.
 * </li>
 * <li>datestamp - An optional &lt;resource&gt; element that contains
 * the date/time stamp format to apply to the backup zipfile name.
 * When it's not supplied, the default will be no date/time stamp
 * only the "backup-filename".zip.
 * </li>
 * <li>
 * relative-directory - An optional resource that allows you to control
 * whether to include the fully qualified directory path or only the
 * partial directory path beginning with the source-directory.  Valid
 * values are true/false, yes/no, 0/1, y/n.
 * </li>
 * <li>
 * traverse-directory - An optional resource that allows you to control
 * whether to traverse the entire directory tree from the source-directory
 * on down, or only the source-directory and files within it.  Valid
 * values are true/false, yes/no, 0/1, y/n.
 * </li>
 * </ul>
 * </p>
 * <p>
 * <h3>Details</h3>
 * <hr>
 * <h4>Input Resources</h4>
 * <table border='1' width='100%'>
 * <thead>
 *  <tr>
 *      <td width='15%'>Location</td>
 *      <td width='1%'>&nbsp;</td>
 *      <td width='15%'>Id/Name</td>
 *      <td width='1%'>&nbsp;</td>
 *      <td width='20%'>Type</td>
 *      <td width='1%'>&nbsp;</td>
 *      <td width='8%'>Required</td>
 *      <td width='1%'>&nbsp;</td>
 *      <td>Description/Comments</td>
 *  </tr>
 * </thead>
 *  <tr valign='top'>
 *      <td>&lt;task&gt;</td>
 *      <td>&nbsp;</td>
 *      <td>source-directory</td>
 *      <td>&nbsp;</td>
 *      <td>String</td>
 *      <td>&nbsp;</td>
 *      <td>True</td>
 *      <td>&nbsp;</td>
 *      <td>
 *      The absolute or relative directory path of where the 
 *      source files to backup reside.
 *      </td>
 *  </tr>
 *  <tr valign='top'>
 *      <td>&lt;task&gt;</td>
 *      <td>&nbsp;</td>
 *      <td>backup-directory</td>
 *      <td>&nbsp;</td>
 *      <td>String</td>
 *      <td>&nbsp;</td>
 *      <td>True</td>
 *      <td>&nbsp;</td>
 *      <td>
 *      The absolute or relative directory path of where to store 
 *      the backup zipfile.
 *      </td>
 *  </tr>
 *  <tr valign='top'>
 *      <td>&lt;task&gt;</td>
 *      <td>&nbsp;</td>
 *      <td>backup-filename</td>
 *      <td>&nbsp;</td>
 *      <td>String</td>
 *      <td>&nbsp;</td>
 *      <td>True</td>
 *      <td>&nbsp;</td>
 *      <td>
 *      The filename to use (or prefix) for the backup zipfile.  For
 *      a prefix situation, the date/time stamp will be appended 
 *      to the backup-filename.
 *      </td>
 *  </tr>
 *  <tr valign='top'>
 *      <td>&lt;task&gt;</td>
 *      <td>&nbsp;</td>
 *      <td>datestamp</td>
 *      <td>&nbsp;</td>
 *      <td>String</td>
 *      <td>&nbsp;</td>
 *      <td>False</td>
 *      <td>&nbsp;</td>
 *      <td>
 *      A date/time stamp formatting pattern to use when naming the
 *      backup-filename.
 *      </td>
 *  </tr>
 * </table>
 * </p>
 * <p>
 * The following is an example XML &lt;task&gt; element:
 * </p>
 * <p>
 * <pre>
 *     &lt;jbjf-tasks&gt;
 *         &lt;task name="t001" order="1" active="true"&gt;
 *             &lt;class&gt;org.jbjf.tasks.BackupDirectory&lt;/class&gt;
 *             &lt;resource type="source-directory"&gt;/usr/apps/my-app&lt;/resource&gt;
 *             &lt;resource type="backup-directory"&gt;/usr/apps/backups&lt;/resource&gt;
 *             &lt;resource type="backup-filename"&gt;my-app-backup&lt;/resource&gt;
 *             &lt;resource type="datestamp"&gt;MM-dd-yyyy-HH-mm-ss&lt;/resource&gt;
 *         &lt;/task&gt;
 * </pre>
 * </p>
 * <p>
 * @author Adym S. Lincoln<br>
 *         Copyright (C) 2007-2011. JBJF All rights reserved.
 * @version 1.3.3
 * @since   1.3.3
 * </p>
 */
public class BackupDirectory extends AbstractTask {

    /**
     * Stores a fully qualified class name. Used for debugging and auditing.
     * 
     * @since 1.0.0
     */
    public static final String ID = BackupDirectory.class.getName();

    /**
     * Stores the class name, primarily used for debugging and so forth. Used
     * for debugging and auditing.
     * 
     * @since 1.0.0
     */
    private String SHORT_NAME = "BackupDirectory()";

    /**
     * Stores a <code>SYSTEM IDENTITY HASHCODE</code>. Used for debugging and
     * auditing.
     * 
     * @since 1.0.0
     */
    private String SYSTEM_IDENTITY = String.valueOf(System.identityHashCode(this));

    /**
     * Class property that stores the source directory.
     */
    private String sourceDirectory = null;

    /**
     * Class property that stores the backup directory.
     */
    private String backupDirectory = null;

    /**
     * Class property that stores the date/time stamp format...All the typical
     * Java formats are accepted: MM - Month dd - Day yyyy - Year mm - Minutes
     * ss - Seconds hh - Hours
     */
    private String dateStamp = null;

    /**
     * Class property that stores the backup filename. You do not need to
     * include the *.zip file extension.
     */
    private String backupFilename = null;

    /**
     * Class property that stores the option of whether to trim up
     * directory paths to the last one or not...Default is to trim, 
     * otherwise the fully qualified directory path is added to
     * every file entry.
     */
    private boolean relativeDirectory = true;

    /**
     * Class property that stores the option of whether to traverse
     * the entire directory tree or just the top-level directory
     * supplied in the "source-directory" &lt;resource&gt; element.  The 
     * default is TO traverse.
     */
    private boolean traverseDirectory = true;

    /**
     * Default constructor. Sets the required &lt;resource&gt; elements.
     */
    public BackupDirectory() {
        if (getRequiredResources() == null) {
            setRequiredResources(new ArrayList());
        }
        getRequiredResources().add("source-directory");
        getRequiredResources().add("backup-directory");
        getRequiredResources().add("backup-filename");
        setSubTask(false);
    }

    /**
     * Custom constructor. Allows you to create the task in a sub-task
     * mode of operation.  No need to explicitly set the sub-task
     * indicator.
     */
    public BackupDirectory(boolean subTask) {
        if (getRequiredResources() == null) {
            setRequiredResources(new ArrayList());
        }
        getRequiredResources().add("source-directory");
        getRequiredResources().add("backup-directory");
        getRequiredResources().add("backup-filename");
        setSubTask(subTask);
    }

    @Override
    public void runTask(HashMap jobStack) throws Exception {
        getLog().info(SHORT_NAME + "...Start...");
        if (hasRequiredResources(isSubTask())) {
            try {
                File fileSource = new File(getSourceDirectory());
                if (fileSource.isDirectory()) {
                    FileOutputStream fosZipfile = new FileOutputStream(getZipFilename());
                    ZipOutputStream zosZipfile = new ZipOutputStream(fosZipfile);
                    int lnumEntries = addDirectory(zosZipfile, fileSource, "");
                    zosZipfile.close();
                }
                getLog().debug("Zip file has been created! [" + getZipFilename() + "]");
            } catch (IOException ioe) {
                if (ioe.getMessage().contains("at least one")) {
                    getLog().warn(ioe);
                } else {
                    throw new IOException("Task [" + SHORT_NAME + "] Encountered IOException " + ioe.getMessage());
                }
            }
        }
        getLog().info(SHORT_NAME + "...Complete...");
    }

    /**
     * <p>
     * Utility method that accepts a zipfile output stream and the
     * top level source directory.  All files and folders from that
     * source directory are then added and zipped into the zipfile
     * output stream.
     * </p>
     * @param zosFile   The Zipfile output stream...should already
     * be created prior to calling this method.
     * @param dirSource The top level source directory <code>File</code>
     * object.  Should already be created and allocated.
     * @throws Exception    Elevate any issues upward.
     */
    public int addDirectory(ZipOutputStream zosFile, File dirSource, String preSource) throws Exception {
        String ldirSource = dirSource.getName();
        String lpreSource = preSource;
        int lcntEntries = 0;
        if (!(isRelativeDirectory())) {
            ldirSource = dirSource.getAbsolutePath();
            lpreSource = "";
        } else {
            ldirSource = dirSource.getName();
            lpreSource = preSource;
        }
        File[] files = dirSource.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory() && isTraverseDirectory()) {
                if (isRelativeDirectory()) {
                    lpreSource = lpreSource + File.separator + ldirSource;
                    getLog().debug("Adding directory " + lpreSource + File.separator + files[i].getName());
                    addDirectory(zosFile, files[i], lpreSource);
                    lpreSource = preSource;
                } else {
                    getLog().debug("Adding directory " + ldirSource);
                    addDirectory(zosFile, files[i], "");
                }
            } else if (!(files[i].isDirectory())) {
                try {
                    byte[] buffer = new byte[1024];
                    FileInputStream fin = new FileInputStream(files[i]);
                    if (isRelativeDirectory()) {
                        getLog().debug("Adding file [" + lpreSource + File.separator + ldirSource + File.separator + files[i].getName() + "]");
                        zosFile.putNextEntry(new ZipEntry(lpreSource + File.separator + ldirSource + File.separator + files[i].getName()));
                    } else {
                        getLog().debug("Adding file [" + ldirSource + File.separator + files[i].getName() + "]");
                        zosFile.putNextEntry(new ZipEntry(ldirSource + File.separator + files[i].getName()));
                    }
                    int length;
                    while ((length = fin.read(buffer)) > 0) {
                        zosFile.write(buffer, 0, length);
                    }
                    zosFile.closeEntry();
                    fin.close();
                } catch (IOException ioe) {
                    getLog().fatal(ioe);
                    throw ioe;
                } catch (Exception ltheXcp) {
                    getLog().fatal(ltheXcp);
                    throw ltheXcp;
                }
            }
            lcntEntries++;
        }
        return lcntEntries;
    }

    @Override
    public void initTask(HashMap jobStack) throws Exception {
        super.initTask(jobStack);
        setSourceDirectory(resolveResource("source-directory"));
        setBackupDirectory(resolveResource("backup-directory"));
        setBackupFilename(resolveResource("backup-filename"));
        setDateStamp((String) getResources().get("datestamp"));
        setRelativeDirectory((String) getResources().get("relative-directory"));
        setTraverseDirectory((String) getResources().get("traverse-directory"));
        if (hasOptionalResource("datestamp")) {
            setDateStamp((String) getResources().get("datestamp"));
        } else {
            setDateStamp(null);
        }
    }

    /**
     * Utility method that takes in a <code>String</code> name of a
     * &lt;directory&gt; element and returns the XML value from that element.
     * <p>
     * 
     * @param keyResource
     *            String value of the resource to find.
     * @return Returns a <code>String</code> value of the &lt;directory&gt;
     *         element.
     */
    public String resolveResource(String keyResource) throws Exception {
        getLog().debug("resolveResource resource key [" + keyResource + "]");
        String lstrResults = null;
        if (getResources().containsKey(keyResource)) {
            lstrResults = (String) getResources().get(keyResource);
            if ((lstrResults != null) && (lstrResults.length() > 0)) {
                getLog().debug("resolveResource found [" + keyResource + "] as [" + lstrResults + "]");
            } else {
                throw new Exception("Task [" + SHORT_NAME + "] found a resource [" + keyResource + "]," + " but no corresponding <resource> value.  Please" + " double-check the values and links in the" + " JBJF Batch Definition file.");
            }
        } else {
            throw new Exception("Task [" + SHORT_NAME + "] could not locate a resource" + " of type='" + keyResource + "'...<resource type='" + keyResource + "'>" + " Please make sure this resource is coded in the JBJF" + " Batch Definition file.");
        }
        getLog().debug("resolveResource found [" + lstrResults + "]");
        return lstrResults;
    }

    /**
     * Checks to see if a given optional resource exists.
     * 
     * @return True/False indicator on whether it passed.
     */
    public boolean hasOptionalResource(String nameResource) {
        return getResources().containsKey(nameResource);
    }

    /**
     * <p>
     * Traditional getter() method that...
     * </p>
     * 
     * @return The sourceDirectory
     */
    public String getSourceDirectory() {
        return sourceDirectory;
    }

    /**
     * <p>
     * Traditional setter() method that...
     * </p>
     * 
     * @param sourceDirectory
     *            the sourceDirectory to set
     */
    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    /**
     * <p>
     * Traditional getter() method that...
     * </p>
     * 
     * @return The backupDirectory
     */
    public String getBackupDirectory() {
        return backupDirectory;
    }

    /**
     * <p>
     * Traditional setter() method that...
     * </p>
     * 
     * @param backupDirectory
     *            the backupDirectory to set
     */
    public void setBackupDirectory(String backupDirectory) {
        this.backupDirectory = backupDirectory;
    }

    /**
     * <p>
     * Traditional getter() method that...
     * </p>
     * 
     * @return The dateStamp
     */
    public String getDateStamp() {
        return dateStamp;
    }

    /**
     * <p>
     * Traditional setter() method that...
     * </p>
     * 
     * @param dateStamp
     *            the dateStamp to set
     */
    public void setDateStamp(String dateStamp) {
        this.dateStamp = dateStamp;
    }

    /**
     * <p>
     * Traditional getter() method that...
     * </p>
     * 
     * @return The backupFilename
     */
    public String getBackupFilename() {
        return backupFilename;
    }

    /**
     * <p>
     * Traditional setter() method that...
     * </p>
     * 
     * @param backupFilename
     *            the backupFilename to set
     */
    public void setBackupFilename(String backupFilename) {
        this.backupFilename = backupFilename;
    }

    /**
     * <p>
     * Custom getter() method that creates a fully qualified Zipfile
     * name from the following components:
     * <li>backup-directory</li>
     * <li>backup-filename</li>
     * <li>datestamp (if supplied)</li>
     * </p>
     * @return A fully qualified path and filename for the zipfile
     * location and an optional date/time stamp format if supplied.
     */
    public String getZipFilename() {
        String lstrResults = "";
        if (getBackupDirectory() != null) {
            lstrResults = lstrResults + getBackupDirectory();
        }
        if (getBackupFilename() != null) {
            lstrResults = lstrResults + File.separator + getBackupFilename();
        }
        if (getDateStamp() != null) {
            SimpleDateFormat lfmt24Hour = new SimpleDateFormat(getDateStamp());
            Date ldateNow = new Date();
            getLog().debug("Formatted date...[" + lfmt24Hour.format(ldateNow) + "]");
            lstrResults = lstrResults + "-" + lfmt24Hour.format(ldateNow);
        }
        lstrResults = lstrResults + ".zip";
        return lstrResults;
    }

    /**
     * <p>
     * Traditional getter() method that...
     * </p>
     * @return The traverseDirectory
     */
    public boolean isTraverseDirectory() {
        return traverseDirectory;
    }

    /**
     * <p>
     * Traditional setter() method that...
     * </p>
     * @param traverseDirectory the traverseDirectory to set
     */
    public void setTraverseDirectory(boolean traverseDirectory) {
        this.traverseDirectory = traverseDirectory;
    }

    public void setTraverseDirectory(String traverseDirectory) {
        if (traverseDirectory == null) {
            this.traverseDirectory = true;
        } else {
            if (traverseDirectory.equalsIgnoreCase("false") || traverseDirectory.equalsIgnoreCase("n") || traverseDirectory.equalsIgnoreCase("no") || traverseDirectory.equalsIgnoreCase("0")) {
                this.traverseDirectory = false;
            } else {
                this.traverseDirectory = true;
            }
        }
    }

    /**
     * <p>
     * Traditional getter() method that...
     * </p>
     * @return The relativeDirectory
     */
    public boolean isRelativeDirectory() {
        return relativeDirectory;
    }

    /**
     * <p>
     * Traditional setter() method that...
     * </p>
     * @param relativeDirectory the relativeDirectory to set
     */
    public void setRelativeDirectory(boolean relativeDirectory) {
        this.relativeDirectory = relativeDirectory;
    }

    public void setRelativeDirectory(String relativeDirectory) {
        if (relativeDirectory == null) {
            this.relativeDirectory = true;
        } else {
            if (relativeDirectory.equalsIgnoreCase("false") || relativeDirectory.equalsIgnoreCase("n") || relativeDirectory.equalsIgnoreCase("no") || relativeDirectory.equalsIgnoreCase("0")) {
                this.relativeDirectory = false;
            } else {
                this.relativeDirectory = true;
            }
        }
    }
}
