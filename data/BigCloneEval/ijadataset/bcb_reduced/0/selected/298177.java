package de.fuh.xpairtise.plugin.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import de.fuh.xpairtise.common.LogConstants;
import de.fuh.xpairtise.common.XPLog;

/**
 * This class implements methods for dumping and restoring Eclipse projects. The
 * backup directory exists at the same level as the local Eclipse workspace
 * directory.
 * 
 * <br>
 * &nbsp;<br>
 * 
 * Sample usage:
 * 
 * <pre>
 * LocalSnapshot snapshot = new LocalSnapshot();
 * boolean rc = snapshot.dump(&quot;test1&quot;, ILocalSnapshotConstants.LOCAL_SNAPSHOT_DIR,
 *     &quot;001&quot;, false);
 * </pre>
 */
public class LocalSnapshot {

    private IWorkspace workspace = null;

    private IWorkspaceRoot root = null;

    private WorkspaceSpy spy = null;

    /**
   * Creates a new <code>LocalSnapshot</code> instance.
   * 
   */
    public LocalSnapshot() {
        if (XPLog.isDebugEnabled()) {
            XPLog.printDebug(LogConstants.LOG_PREFIX_LOCALSNAPSHOT + "starting.");
        }
        this.spy = new WorkspaceSpy();
        if (spy != null) {
            this.workspace = spy.getEclipseWorkspace();
            this.root = spy.getEclipseWorkspaceRoot();
        }
    }

    /**
   * Creates a ZIP archive under the given directory -- it exists at the same
   * level as the workspace directory -- and adds the members of the project to
   * it.
   * 
   * @param project
   *          the project to be saved
   * @param snapshotDirName
   *          the name of the snapshot directory
   * @param snapshotId
   *          the unique ID of the snapshot
   * @param ignoreDerivedResources
   *          set to <code>true</code> if derived resources such as
   *          <code>.class</code> files should not be added to the ZIP archive
   * @return <code>true</code> - if the project was saved, otherwise
   *         <code>false</code>
   */
    public synchronized boolean dump(String project, String snapshotDirName, String snapshotId, boolean ignoreDerivedResources) {
        String errorMsg = "Problems encountered while trying to create local snapshot.";
        boolean rc = false;
        boolean oldSpyIgnoreDerivedResources = spy.isIgnoreDerivedResources();
        if ((project != null) && (snapshotDirName != null) && (snapshotId != null)) {
            try {
                IProject proj = null;
                ArrayList<String> fileList = null;
                workspace.save(true, null);
                if (spy.exists(project)) {
                    proj = root.getProject(project);
                    if (!proj.isOpen()) proj.open(null);
                    spy.setIgnoreDerivedResources(ignoreDerivedResources);
                    fileList = spy.listProjectMemberFiles(proj);
                    if (XPLog.isDebugEnabled()) {
                        XPLog.printDebug(LogConstants.LOG_PREFIX_LOCALSNAPSHOT + "project members: " + fileList);
                    }
                } else {
                    if (XPLog.isDebugEnabled()) {
                        XPLog.printDebug(LogConstants.LOG_PREFIX_LOCALSNAPSHOT + "project \"" + project + "\" does not exist in the workspace \"" + root.getName() + "\"!");
                    }
                    return false;
                }
                try {
                    BufferedInputStream origin = null;
                    IPath destArchivePath = getSnapshotPath(proj.getName(), ILocalSnapshotConstants.LOCAL_SNAPSHOT_DIR, snapshotId);
                    File destDir = new File((destArchivePath.removeLastSegments(1)).toOSString());
                    if (!(destDir.exists())) {
                        destDir.mkdirs();
                    }
                    try {
                        renameSnapshot(destArchivePath, ILocalSnapshotConstants.SNAPSHOT_SEPARATOR);
                    } catch (NullPointerException npe) {
                        logException(0, errorMsg, npe);
                    } catch (SecurityException se) {
                        logException(0, errorMsg, se);
                    }
                    FileOutputStream dest = new FileOutputStream(destArchivePath.toOSString());
                    CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
                    ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(checksum));
                    byte data[] = new byte[ILocalSnapshotConstants.BUFFER];
                    String files[] = new String[fileList.toArray().length];
                    fileList.toArray(files);
                    try {
                        IPath rootDir = proj.getLocation();
                        try {
                            for (int i = 0; i < files.length; i++) {
                                IPath aPath = rootDir.append(files[i]);
                                String entryPathName = (new Path(files[i])).toString();
                                File file = aPath.toFile();
                                if (file.exists()) {
                                    if (XPLog.isDebugEnabled()) {
                                        XPLog.printDebug(LogConstants.LOG_PREFIX_LOCALSNAPSHOT + "adding:" + aPath.toOSString());
                                    }
                                    if (file.isFile()) {
                                        try {
                                            ZipEntry entry = new ZipEntry(entryPathName);
                                            out.putNextEntry(entry);
                                            FileInputStream fi = new FileInputStream(aPath.toOSString());
                                            origin = new BufferedInputStream(fi, ILocalSnapshotConstants.BUFFER);
                                            int count;
                                            while ((count = origin.read(data, 0, ILocalSnapshotConstants.BUFFER)) != -1) {
                                                out.write(data, 0, count);
                                            }
                                        } finally {
                                            origin.close();
                                        }
                                    } else if (file.isDirectory()) {
                                        ZipEntry entry = null;
                                        if (entryPathName.endsWith(ILocalSnapshotConstants.FILE_SEPARATOR)) {
                                            entry = new ZipEntry(entryPathName);
                                        } else {
                                            entry = new ZipEntry(entryPathName + ILocalSnapshotConstants.FILE_SEPARATOR);
                                        }
                                        if (entry != null) {
                                            out.putNextEntry(entry);
                                        }
                                    }
                                } else {
                                    if (XPLog.isDebugEnabled()) {
                                        XPLog.printDebug(LogConstants.LOG_PREFIX_LOCALSNAPSHOT + "file does not exist:" + aPath.toOSString());
                                    }
                                }
                            }
                            out.flush();
                        } finally {
                            out.close();
                        }
                        rc = true;
                        if (XPLog.isDebugEnabled()) {
                            XPLog.printDebug(LogConstants.LOG_PREFIX_LOCALSNAPSHOT + "checksum: " + checksum.getChecksum().getValue());
                        }
                    } catch (ZipException e) {
                        logException(0, errorMsg, e);
                    } catch (FileNotFoundException e) {
                        logException(0, errorMsg, e);
                    } catch (IOException e) {
                        logException(0, errorMsg, e);
                    }
                } catch (FileNotFoundException e) {
                    logException(0, errorMsg, e);
                }
            } catch (CoreException e) {
                logException(0, errorMsg, e);
            } catch (Exception e) {
                logException(0, errorMsg, e);
            } finally {
                spy.setIgnoreDerivedResources(oldSpyIgnoreDerivedResources);
            }
        }
        return rc;
    }

    /**
   * Restores a project. The corresponding snapshot, a ZIP archive, will be
   * extracted to the local Eclipse workspace directory.
   * 
   * @param project
   *          the project to be restored
   * @param snapshotDirName
   *          the name of the snapshot directory
   * @param snapshotId
   *          the unique ID of the snapshot
   * @return <code>true</code> - if the project was restored, otherwise
   *         <code>false</code>
   */
    public synchronized boolean restore(String project, String snapshotDirName, String snapshotId) {
        String errorMsg = "Problems encountered while trying to restore local snapshot.";
        boolean rc = false;
        if ((project != null) && (snapshotDirName != null) && (snapshotId != null)) {
            try {
                boolean projectWasClosed = false;
                IProject proj = null;
                ArrayList<String> projectResources = null;
                boolean autoBuildChanged = spy.setAutoBuild(false);
                if (spy.exists(project)) {
                    proj = root.getProject(project);
                    if (!proj.isOpen()) {
                        proj.open(null);
                        projectWasClosed = true;
                    }
                    if (!(proj.isSynchronized(IResource.DEPTH_INFINITE))) {
                        proj.refreshLocal(IResource.DEPTH_INFINITE, null);
                    }
                    projectResources = spy.listProjectMemberFiles(proj);
                } else {
                    if (XPLog.isDebugEnabled()) {
                        XPLog.printDebug(LogConstants.LOG_PREFIX_LOCALSNAPSHOT + "project \"" + project + "\" does not exist in the workspace \"" + root.getLocation() + "\"! Creating it.");
                    }
                    proj = root.getProject(project);
                    proj.create(null);
                    proj.open(null);
                }
                FileInputStream fis = new FileInputStream(getSnapshotPath(project, snapshotDirName, snapshotId).toOSString());
                CheckedInputStream checksum = new CheckedInputStream(fis, new Adler32());
                ZipInputStream zis = new ZipInputStream(new BufferedInputStream(checksum));
                ZipEntry entry;
                try {
                    while ((entry = zis.getNextEntry()) != null) {
                        if (XPLog.isDebugEnabled()) {
                            XPLog.printDebug(LogConstants.LOG_PREFIX_LOCALSNAPSHOT + "extracting: " + entry);
                        }
                        if (entry.isDirectory()) {
                            writeFolder(proj, entry.getName());
                        } else {
                            int count;
                            byte data[] = new byte[ILocalSnapshotConstants.BUFFER];
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            BufferedOutputStream dest = new BufferedOutputStream(bos, ILocalSnapshotConstants.BUFFER);
                            try {
                                while ((count = zis.read(data, 0, ILocalSnapshotConstants.BUFFER)) != -1) {
                                    dest.write(data, 0, count);
                                }
                                dest.flush();
                                bos.flush();
                            } finally {
                                dest.close();
                                bos.close();
                            }
                            writeFile(proj, entry.getName(), bos.toByteArray());
                        }
                        if (projectResources != null) {
                            IResource r = proj.findMember(entry.getName());
                            if (r != null) {
                                if (XPLog.isDebugEnabled()) {
                                    XPLog.printDebug(LogConstants.LOG_PREFIX_LOCALSNAPSHOT + "keeping: " + r.getProjectRelativePath().toOSString());
                                }
                                projectResources.remove(r.getProjectRelativePath().toOSString());
                            }
                        }
                    }
                } finally {
                    zis.close();
                }
                rc = true;
                if (XPLog.isDebugEnabled()) {
                    XPLog.printDebug(LogConstants.LOG_PREFIX_LOCALSNAPSHOT + "checksum: " + checksum.getChecksum().getValue());
                }
                if (projectResources != null) {
                    for (String s : projectResources) {
                        if (XPLog.isDebugEnabled()) {
                            XPLog.printDebug(LogConstants.LOG_PREFIX_LOCALSNAPSHOT + "deleting: " + s);
                        }
                        IResource r = proj.findMember(s);
                        if (r != null) {
                            r.delete(true, null);
                        }
                    }
                }
                if (!(root.isSynchronized(IResource.DEPTH_INFINITE))) {
                    root.refreshLocal(IResource.DEPTH_INFINITE, null);
                }
                proj.build(IncrementalProjectBuilder.CLEAN_BUILD, null);
                if (projectWasClosed) {
                    proj.close(null);
                }
                if (autoBuildChanged) {
                    spy.setAutoBuild(true);
                }
            } catch (ZipException e) {
                logException(0, errorMsg, e);
            } catch (FileNotFoundException e) {
                logException(0, errorMsg, e);
            } catch (IOException e) {
                logException(0, errorMsg, e);
            } catch (CoreException e) {
                logException(0, errorMsg, e);
            } catch (Exception e) {
                logException(0, errorMsg, e);
            }
        }
        return rc;
    }

    /**
   * Tests whether a local snapshot of the specified project exists.
   * 
   * @param project
   *          the project to be saved
   * @param snapshotDirName
   *          the name of the snapshot directory
   * @param snapshotId
   *          the unique ID of the snapshot
   * @return <code>true</code> if the local snapshot exists,
   *         <code>false</code> otherwise
   */
    public boolean exists(String project, String snapshotDirName, String snapshotId) {
        IPath aPath = root.getLocation().removeLastSegments(1).append(snapshotDirName).append(project + ILocalSnapshotConstants.SNAPSHOT_SEPARATOR + snapshotId + ILocalSnapshotConstants.SNAPSHOT_EXTENSION);
        File file = aPath.toFile();
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    /**
   * Returns the absolute path in the local file system to the local snapshot,
   * or <code>null</code> if no path can be determined.
   * 
   * @param project
   *          the name of the shared project
   * @param snapshotDirName
   *          the name of the snapshot directory
   * @param snapshotId
   *          the unique ID of the snapshot
   * @return the absolute path in the local file system to the local snapshot,
   *         or <code>null</code> if no path can be determined
   */
    public IPath getSnapshotPath(String project, String snapshotDirName, String snapshotId) {
        IPath aPath = root.getLocation().removeLastSegments(1).append(snapshotDirName).append(project + ILocalSnapshotConstants.SNAPSHOT_SEPARATOR + snapshotId + ILocalSnapshotConstants.SNAPSHOT_EXTENSION);
        return aPath;
    }

    /**
   * Renames the file denoted by the specified abstract pathname. The
   * destination file name consists of the old one plus the time the source file
   * was last modified.
   * 
   * @param source
   *          the path of the source file to be renamed
   * @param separator
   *          the <code>String</code> prefixing the segment that makes the
   *          destination file name unique
   * @return <code>true</code> if the renaming succeeded, <code>false</code>
   *         otherwise
   */
    public boolean renameSnapshot(IPath source, String separator) {
        File sourceFile = source.toFile();
        if (sourceFile.exists()) {
            IPath destFilePath = null;
            File destFile = null;
            Date date = new Date(sourceFile.lastModified());
            SimpleDateFormat debugDateFormat = new SimpleDateFormat("yyMMddHHmmss");
            String extension = source.getFileExtension();
            if (extension != null) {
                destFilePath = source.removeFileExtension();
                destFile = new File(destFilePath.toOSString() + separator + debugDateFormat.format(date) + "." + extension);
            } else {
                destFile = new File(source.toOSString() + separator + debugDateFormat.format(date));
            }
            if (XPLog.isDebugEnabled()) {
                XPLog.printDebug(LogConstants.LOG_PREFIX_LOCALSNAPSHOT + "renaming old snapshot: source=" + source.toOSString() + ", dest=" + destFile.getAbsolutePath());
            }
            return sourceFile.renameTo(destFile);
        } else {
            return false;
        }
    }

    /**
   * Returns the <code>WorkspaceSpy</code> instance.
   * 
   * @return the spy
   */
    public WorkspaceSpy getSpy() {
        return spy;
    }

    private void writeFolder(IProject project, String path) {
        IProject sessionProject = project;
        if (XPLog.isDebugEnabled()) {
            XPLog.printDebug(LogConstants.LOG_PREFIX_LOCALSNAPSHOT + "writing folder: " + path);
        }
        if (sessionProject.exists()) {
            IFolder folder = sessionProject.getFolder(path);
            if (!folder.exists()) {
                try {
                    folder.create(true, true, null);
                } catch (CoreException c) {
                    logException(0, "Problems encountered while trying to write folder '" + folder.getProjectRelativePath() + "'.", c);
                }
            }
        } else {
            if (XPLog.isDebugEnabled()) {
                XPLog.printDebug(LogConstants.LOG_PREFIX_LOCALSNAPSHOT + "project \"" + sessionProject.getName() + "\" is not valid.");
            }
        }
    }

    private void writeFile(IProject project, String path, byte[] content) {
        IProject sessionProject = project;
        if (XPLog.isDebugEnabled()) {
            XPLog.printDebug(LogConstants.LOG_PREFIX_LOCALSNAPSHOT + "writing file: " + path);
        }
        if (sessionProject.exists()) {
            IFile file = sessionProject.getFile(path);
            try {
                if (file.exists()) {
                    file.setContents(new ByteArrayInputStream(content), true, false, null);
                } else {
                    file.create(new ByteArrayInputStream(content), true, null);
                }
            } catch (CoreException c) {
                logException(0, "Problems encountered while trying to write file '" + file.getProjectRelativePath() + "'.", c);
            }
        } else {
            if (XPLog.isDebugEnabled()) {
                XPLog.printDebug(LogConstants.LOG_PREFIX_LOCALSNAPSHOT + "project \"" + sessionProject.getName() + "\" is not valid.");
            }
        }
    }

    private void logException(int errorCode, String message, Throwable e) {
        ClientXPLog.logException(errorCode, "Local Snapshot", message, e, false);
    }
}
