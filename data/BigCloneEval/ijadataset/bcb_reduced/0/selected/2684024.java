package de.fuh.xpairtise.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import de.fuh.xpairtise.common.LogConstants;
import de.fuh.xpairtise.common.XPLog;
import de.fuh.xpairtise.common.replication.elements.ReplicatedProjectInfo;

/**
 * This utility class offers different methods to create zipped snapshots of
 * selected files or to restore from existing snapshots.
 */
public class SnapshotUtils {

    private static final String SEPARATOR = "/";

    private static final int BUFFER = 2048;

    private static final String PROJECT_INFO_NAME = ".XPairtise_ProjectInfo";

    private static final String PROJECT_MEMBER_LIST_NAME = ".XPairtise_Member_List";

    private SnapshotUtils() {
    }

    /**
   * Dumps the files included in the given list with paths relative to the given
   * base directory to a byte array.
   * 
   * @param projectInfo
   *          a <code>ReplicatedProjectInfo</code> describing the project the
   *          dumped files belong to or <code>null</code>
   * @param memberList
   *          a list of the project-relative paths of all member resources of
   *          the project the dumped files belong to or <code>null</code>
   * @param dumpList
   *          a list containing the paths of the files to dump. The paths are
   *          expected to be relative to the given base directory
   * @param base
   *          the base directory the paths to dump are expected to be relative
   *          to
   * @param progress
   *          a <code>IProgressRelay</code> to report progress to or
   *          <code>null</code>
   * 
   * @return a byte array containing the snapshot of the files or
   *         <code>null</code>
   */
    public static byte[] dumpToByteArray(ReplicatedProjectInfo projectInfo, ArrayList<String> memberList, ArrayList<String> dumpList, String base, IProgressRelay progress) {
        if ((dumpList == null || dumpList.isEmpty()) && memberList == null && projectInfo == null) {
            if (XPLog.isDebugEnabled()) {
                XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "Nothing to dump.");
            }
            return null;
        }
        if (dumpList == null) {
            dumpList = new ArrayList<String>();
        }
        if (XPLog.isDebugEnabled()) {
            XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "Dumping snapshot of " + dumpList.size() + " files to byte array.");
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        boolean result = dumpToStream(projectInfo, memberList, dumpList, base, stream, progress);
        if (result) {
            byte[] output = new byte[stream.size()];
            output = stream.toByteArray();
            stream.reset();
            return output;
        }
        return null;
    }

    /**
   * Dumps the files included in the given list with paths relative to the given
   * base directory to the given target file.
   * 
   * @param projectInfo
   *          a <code>ReplicatedProjectInfo</code> describing the project the
   *          dumped files belong to or <code>null</code>
   * @param memberList
   *          a list of the project-relative paths of all member resources of
   *          the project the dumped files belong to or <code>null</code>
   * @param dumpList
   *          a list containing the paths of the files to dump. The paths are
   *          expected to be relative to the given base directory
   * @param base
   *          the base directory the paths to dump are expected to be relative
   *          to
   * @param file
   *          the absolute path of the file to dump to. If this file already
   *          exists, it will be overwritten.
   * @param progress
   *          a <code>IProgressRelay</code> to report progress to or
   *          <code>null</code>
   * 
   * @return true if the dump was successful or there was nothing to dump, false
   *         otherwise
   */
    public static boolean dumpToFile(ReplicatedProjectInfo projectInfo, ArrayList<String> memberList, ArrayList<String> dumpList, String base, String file, IProgressRelay progress) {
        if ((dumpList == null || dumpList.isEmpty()) && memberList == null && projectInfo == null) {
            if (XPLog.isDebugEnabled()) {
                XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "Nothing to dump.");
            }
            return true;
        }
        File output = null;
        if (file == null) {
            if (XPLog.isDebugEnabled()) {
                XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "Invalid file.");
            }
            return false;
        } else {
            output = new File(file);
        }
        if (dumpList == null) {
            dumpList = new ArrayList<String>();
        }
        if (XPLog.isDebugEnabled()) {
            XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "Dumping snapshot of " + dumpList.size() + " files to file: " + file);
        }
        try {
            FileOutputStream stream = new FileOutputStream(output);
            boolean result = dumpToStream(projectInfo, memberList, dumpList, base, stream, progress);
            stream.close();
            return result;
        } catch (IOException f) {
            if (XPLog.isDebugEnabled()) {
                XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "Failed to write file.");
            }
            return false;
        }
    }

    /**
   * Restores the content of a snapshot provided by the given byte array to the
   * given target directory.
   * 
   * @param input
   *          the byte array providing the snapshot to restore
   * @param target
   *          the absolute path of the target directory to restore to. This
   *          directory is expected to exist already
   * @param progress
   *          a <code>IProgressRelay</code> to report progress to or
   *          <code>null</code>
   * @return a list of successfully restored files and directories
   */
    public static ArrayList<String> restoreFromByteArray(byte[] input, String target, IProgressRelay progress) {
        if (input == null || input.length == 0) {
            if (XPLog.isDebugEnabled()) {
                XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "Nothing to restore.");
                return null;
            }
        }
        if (XPLog.isDebugEnabled()) {
            XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "Restoring snapshot from byte array to directory: " + target);
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(input);
        return restoreFromStream(stream, target, progress);
    }

    /**
   * Restores the snapshot provided by the given file to the given target
   * directory.
   * 
   * @param input
   *          the input file to restore the snapshot from
   * @param target
   *          the absolute path of the directory to restore to. This directory
   *          is expected to exist already
   * @param progress
   *          a <code>IProgressRelay</code> to report progress to or
   *          <code>null</code>
   * @return a list of successfully restored files and directories
   */
    public static ArrayList<String> restoreFromFile(File input, String target, IProgressRelay progress) {
        if (input == null || !input.exists()) {
            if (XPLog.isDebugEnabled()) {
                XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "Nothing to restore.");
            }
        }
        if (XPLog.isDebugEnabled()) {
            XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "Restoring snapshot from file: " + input.getAbsolutePath() + " to directory: " + target);
        }
        try {
            if (progress != null) {
                int count = new ZipFile(input).size();
                progress.beginTask("Restoring snapshot from file: " + input.getAbsolutePath(), count);
            }
            FileInputStream stream = new FileInputStream(input);
            ArrayList<String> list = restoreFromStream(stream, target, progress);
            stream.close();
            return list;
        } catch (IOException f) {
            if (XPLog.isDebugEnabled()) {
                XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "Failed to open input file: " + input.getAbsolutePath());
            }
        }
        return null;
    }

    /**
   * Dumps the given files to the given OutputStream. The caller has to close
   * the OutputStream when he's done.
   * 
   * @param projectInfo
   *          a <code>ReplicatedProjectInfo</code> describing the project the
   *          dumped files belong to or <code>null</code>
   * @param memberList
   *          a list of the project-relative paths of all member resources of
   *          the project the dumped files belong to or <code>null</code>
   * @param dumpList
   *          a list containing the paths of the files to dump. The paths are
   *          expected to be relative to the given base directory
   * @param base
   *          the base directory the paths to dump are expected to be relative
   *          to
   * @param output
   *          an open output stream to write the snapshot to
   * @param progress
   *          a <code>IProgressRelay</code> to report progress to or
   *          <code>null</code>
   * 
   * @return true if the dump was successful or there was nothing to dump, false
   *         otherwise
   */
    public static boolean dumpToStream(ReplicatedProjectInfo projectInfo, ArrayList<String> memberList, ArrayList<String> dumpList, String base, OutputStream output, IProgressRelay progress) {
        String prefix = base;
        if ((dumpList == null || dumpList.isEmpty()) && memberList == null && projectInfo == null) {
            return true;
        }
        if (output == null) {
            return false;
        }
        if (dumpList == null) {
            dumpList = new ArrayList<String>();
        }
        if (XPLog.isDebugEnabled()) {
            XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "Dumping snapshot of " + dumpList.size() + " files to output stream: " + output.getClass().getSimpleName());
        }
        if (progress != null) {
            progress.beginTask("Dumping " + dumpList.size() + " files.", dumpList.size());
        }
        if (prefix == null) {
            prefix = SEPARATOR;
        } else if (!prefix.endsWith(SEPARATOR)) {
            prefix = prefix.concat(SEPARATOR);
        }
        CheckedOutputStream checked = new CheckedOutputStream(output, new Adler32());
        BufferedOutputStream buffered = new BufferedOutputStream(checked, BUFFER);
        ZipOutputStream stream = new ZipOutputStream(buffered);
        BufferedInputStream origin = null;
        byte[] buffer = new byte[BUFFER];
        String fullPath = null;
        File currentFile = null;
        try {
            if (projectInfo != null) {
                ZipEntry entry = new ZipEntry(PROJECT_INFO_NAME);
                stream.putNextEntry(entry);
                ObjectOutputStream oos = new ObjectOutputStream(stream);
                oos.writeObject(projectInfo);
                oos.flush();
                stream.closeEntry();
            }
            if (memberList != null) {
                ZipEntry entry = new ZipEntry(PROJECT_MEMBER_LIST_NAME);
                stream.putNextEntry(entry);
                ObjectOutputStream oos = new ObjectOutputStream(stream);
                oos.writeObject(memberList);
                oos.flush();
                stream.closeEntry();
            }
            for (String fileName : dumpList) {
                if (progress != null) {
                    progress.subTask("Dumping: " + fileName);
                }
                fullPath = prefix.concat(fileName);
                currentFile = new File(fullPath);
                if (currentFile.exists()) {
                    if (currentFile.isFile()) {
                        if (XPLog.isDebugEnabled()) {
                            XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "adding file: " + fullPath);
                        }
                        try {
                            ZipEntry entry = new ZipEntry(fileName);
                            stream.putNextEntry(entry);
                            FileInputStream fi = new FileInputStream(currentFile);
                            origin = new BufferedInputStream(fi, BUFFER);
                            int count;
                            while ((count = origin.read(buffer, 0, BUFFER)) != -1) {
                                stream.write(buffer, 0, count);
                            }
                            stream.closeEntry();
                        } finally {
                            origin.close();
                        }
                    } else if (currentFile.isDirectory()) {
                        if (XPLog.isDebugEnabled()) {
                            XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "adding directory: " + fullPath);
                        }
                        ZipEntry entry = new ZipEntry(fileName.endsWith(SEPARATOR) ? fileName : fileName.concat(SEPARATOR));
                        if (entry != null) {
                            stream.putNextEntry(entry);
                            stream.closeEntry();
                        }
                    }
                } else {
                    if (XPLog.isDebugEnabled()) {
                        XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "File: " + fullPath + " doesn't exist.");
                    }
                    return false;
                }
                if (progress != null) {
                    progress.worked(1);
                }
            }
            stream.flush();
            stream.finish();
            buffered.flush();
            checked.flush();
            output.flush();
            if (progress != null) {
                progress.done();
            }
        } catch (IOException i) {
            if (XPLog.isDebugEnabled()) {
                XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "An error occurred during dump: " + i.getMessage());
            }
            return false;
        }
        return true;
    }

    /**
   * Restores the Snapshot provided by the given InputStream to the given target
   * directory.
   * 
   * @param input
   *          an open InputStream providing the snapshot data to restore. The
   *          caller is expected to close it after this call
   * @param target
   *          the target directory to restore to. This directory is expected to
   *          exist already
   * @param progress
   *          a <code>IProgressRelay</code> to report progress to or
   *          <code>null</code>
   * @return the list of successfully restored files and directories or
   *         <code>null</code> if a problem occurred
   */
    public static ArrayList<String> restoreFromStream(InputStream input, String target, IProgressRelay progress) {
        if (input == null) {
            if (XPLog.isDebugEnabled()) {
                XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "Nothing to restore");
            }
            return null;
        }
        File targetDir = new File(target);
        if (!targetDir.exists() || !targetDir.isDirectory()) {
            if (XPLog.isDebugEnabled()) {
                XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "Invalid target directory: " + target);
            }
            return null;
        }
        if (XPLog.isDebugEnabled()) {
            XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "Restoring snapshot from input stream: " + input.getClass().getSimpleName() + " to directory: " + target);
        }
        if (!target.endsWith(SEPARATOR)) {
            target = target.concat(SEPARATOR);
        }
        CheckedInputStream checked = new CheckedInputStream(input, new Adler32());
        BufferedInputStream buffered = new BufferedInputStream(checked);
        ZipInputStream stream = new ZipInputStream(buffered);
        ZipEntry entry = null;
        File currentFile = null;
        String fullPath = null;
        ArrayList<String> list = new ArrayList<String>();
        try {
            while ((entry = stream.getNextEntry()) != null) {
                if (entry.getName().equals(PROJECT_INFO_NAME) || entry.getName().equals(PROJECT_MEMBER_LIST_NAME)) {
                    try {
                        ObjectInputStream ois = new ObjectInputStream(stream);
                        Object o = ois.readObject();
                        if (o instanceof ReplicatedProjectInfo) {
                            if (XPLog.isDebugEnabled()) {
                                XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "Skipping project info.");
                            }
                            continue;
                        } else if (o instanceof ArrayList) {
                            @SuppressWarnings("unchecked") ArrayList memberList = (ArrayList) o;
                            if (!memberList.isEmpty() && memberList.get(0) instanceof String) {
                                if (XPLog.isDebugEnabled()) {
                                    XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "Skipping project member list.");
                                }
                                continue;
                            }
                        }
                    } catch (Exception e) {
                    }
                }
                if (progress != null) {
                    progress.subTask("Restoring: " + entry.getName());
                }
                fullPath = target.concat(entry.getName());
                if (entry.isDirectory()) {
                    if (XPLog.isDebugEnabled()) {
                        XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "Extracting directory: " + entry);
                    }
                    currentFile = new File(fullPath);
                    if (!currentFile.exists()) {
                        currentFile.mkdirs();
                    }
                    if (currentFile.exists() && currentFile.isDirectory()) {
                        list.add(entry.getName());
                    } else {
                        if (XPLog.isDebugEnabled()) {
                            XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "Failed to restore directory: " + currentFile.getAbsolutePath());
                        }
                    }
                } else {
                    if (XPLog.isDebugEnabled()) {
                        XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "Extracting file: " + entry);
                    }
                    int count;
                    byte data[] = new byte[BUFFER];
                    currentFile = new File(fullPath);
                    FileOutputStream fos = new FileOutputStream(currentFile);
                    BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
                    try {
                        while ((count = stream.read(data, 0, BUFFER)) != -1) {
                            dest.write(data, 0, count);
                        }
                        dest.flush();
                        fos.flush();
                        if (currentFile.exists()) {
                            list.add(entry.getName());
                        } else {
                            if (XPLog.isDebugEnabled()) {
                                XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + " Failed to restore file: " + currentFile.getAbsolutePath());
                            }
                        }
                    } finally {
                        dest.close();
                        fos.close();
                    }
                }
                if (progress != null) {
                    progress.worked(1);
                }
            }
            if (progress != null) {
                progress.done();
            }
        } catch (IOException i) {
            if (XPLog.isDebugEnabled()) {
                XPLog.printDebug(LogConstants.LOG_PREFIX_SNAPSHOTUTILS + "An error occurred while resoring the snapshot: " + i.getMessage());
            }
        }
        return list;
    }

    /**
   * Returns the project info entry at the beginning of the given dump, if it
   * exists.
   * 
   * @param file
   *          the file to read the project info from
   * @return the <code>ReplicatedProjectInfo</code> or <code>null</code>
   */
    public static ReplicatedProjectInfo getProjectInfo(File file) {
        if (file != null && file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                ZipInputStream zis = new ZipInputStream(fis);
                ZipEntry entry = zis.getNextEntry();
                if (entry != null && entry.getName().equals(PROJECT_INFO_NAME)) {
                    ObjectInputStream ois = new ObjectInputStream(zis);
                    try {
                        Object o = ois.readObject();
                        if (o != null && o instanceof ReplicatedProjectInfo) {
                            return (ReplicatedProjectInfo) o;
                        }
                    } finally {
                        ois.close();
                    }
                }
            } catch (IOException i) {
            } catch (ClassNotFoundException c) {
            }
        }
        return null;
    }

    /**
   * Returns the member list entry at the beginning of the given dump, if it
   * exists.
   * 
   * @param file
   *          the file to read the member list from
   * @return the list of member resource of the dumped project or
   *         <code>null</code>
   */
    @SuppressWarnings("unchecked")
    public static ArrayList<String> getProjectMemberList(File file) {
        if (file != null && file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                ZipInputStream zis = new ZipInputStream(fis);
                ObjectInputStream ois;
                Object o = null;
                ZipEntry entry;
                for (int i = 0; i < 2; i++) {
                    entry = zis.getNextEntry();
                    ois = new ObjectInputStream(zis);
                    if (entry != null && entry.getName().equals(PROJECT_MEMBER_LIST_NAME)) {
                        try {
                            o = ois.readObject();
                        } finally {
                            ois.close();
                        }
                        if (o != null && o instanceof ArrayList) {
                            ArrayList list = (ArrayList) o;
                            if (!list.isEmpty() && list.get(0) instanceof String) {
                                return (ArrayList<String>) list;
                            }
                        }
                    }
                }
            } catch (IOException i) {
                XPLog.printDebug("IOException: " + i.getMessage());
                i.printStackTrace();
            } catch (ClassNotFoundException c) {
            }
        }
        return null;
    }
}
