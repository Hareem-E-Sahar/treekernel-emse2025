package jsync;

import java.io.File;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import jsync.helpers.CommonHelper;
import jsync.helpers.DeviceHelper;
import jsync.helpers.FileHelper;
import jsync.helpers.ConfigHelper;
import jsync.helpers.LogHelper;
import jsync.helpers.HashHelper;
import jsync.io.FolderFilter;
import jsync.io.FileFilter;
import jFileLib.common.Path;

public class DirectoryManager {

    /**
	 * List of relative file paths 
	 */
    private List<String> Folders = new ArrayList<String>();

    /**
	 * Process File synchronization
	 * @param src Source Path
	 * @param dest Destination Path
	 * @param excludes List of File Excludes(currently full paths)
	 */
    public void syncFiles(String src, String dest, List<String> excludes) {
        dest = Path.getCorrectPath(dest);
        File srcFile = new File(src);
        if (srcFile.exists() == false) return;
        if (ConfigHelper.useSpaceCheck() == true) {
            if (hasFreeSpace(src, dest) == false) return;
        }
        if (srcFile.isFile()) {
            String destPath = Path.combine(dest, srcFile.getName());
            File destFile = new File(destPath);
            if (ConfigHelper.useHashing()) {
                String name = srcFile.getName();
                if (HashHelper.isHashFile(name) == true) return;
                String fileHash = HashHelper.getCheckSum(src);
                String currentHash = null;
                if (ConfigHelper.useHashFile() == true) currentHash = HashHelper.getHashFromFile(dest, name); else currentHash = HashHelper.getCheckSum(src);
                if (fileHash.equals(currentHash) == false) {
                    if (currentHash != null && ConfigHelper.isDebugMode() == true) LogHelper.writeInfoFormat("hashChanged", fileHash, src, currentHash, dest);
                    HashHelper.writeHashToFile(dest, name, fileHash);
                    FileManager.copyFile(srcFile, destFile);
                }
            } else if (fileChanged(srcFile, destFile) == true) {
                FileManager.copyFile(srcFile, destFile);
            }
            return;
        }
        if (srcFile.isDirectory()) {
            src = Path.getCorrectPath(src);
            File destFile = new File(dest);
            if (destFile.exists() == false) {
                if (FileHelper.createDestinationFolder(dest) == false) {
                    LogHelper.writeErrorFormat("directoryNotCreated", dest);
                    return;
                }
            }
            StringBuilder srcBuilder = new StringBuilder(src);
            StringBuilder destBuilder = new StringBuilder(dest);
            listRelativeFolderPaths(dest);
            while (Folders.size() > 0) {
                srcBuilder = resetStringBuilder(srcBuilder, src);
                destBuilder = resetStringBuilder(destBuilder, dest);
                String folder = Folders.remove(0);
                String srcPath = srcBuilder.append(folder).toString();
                String destPath = destBuilder.append(folder).toString();
                srcFile = new File(srcPath);
                destFile = new File(destPath);
                if (srcFile.exists() == false) {
                    deleteDirectory(destFile);
                    continue;
                }
                List<String> files = listFiles(destPath, dest);
                if (files == null) continue;
                while (files.size() > 0) {
                    srcBuilder = resetStringBuilder(srcBuilder, src);
                    destBuilder = resetStringBuilder(destBuilder, dest);
                    String relFilePath = files.remove(0);
                    srcPath = srcBuilder.append(relFilePath).toString();
                    destPath = destBuilder.append(relFilePath).toString();
                    srcFile = new File(srcPath);
                    destFile = new File(destPath);
                    if (srcFile.exists() == false || srcFile.isFile() == false) {
                        deleteFile(destFile);
                        continue;
                    } else {
                        if (ConfigHelper.useHashing()) {
                            if (HashHelper.isHashFile(destFile.getName()) == false) {
                                deleteFile(destFile);
                                continue;
                            }
                        } else {
                            if (filesAreEqual(srcFile, destFile) == false) {
                                deleteFile(destFile);
                                continue;
                            }
                        }
                    }
                }
            }
            if (ConfigHelper.useSpaceCheck() == true) {
                if (hasFreeSpace(src, dest) == false) return;
            }
            listRelativeFolderPaths(src);
            while (Folders.size() > 0) {
                CommonHelper.sleep(1);
                srcBuilder = resetStringBuilder(srcBuilder, src);
                destBuilder = resetStringBuilder(destBuilder, dest);
                String folder = Folders.remove(0);
                String srcPath = srcBuilder.append(folder).toString();
                String destPath = destBuilder.append(folder).toString();
                srcFile = new File(srcPath);
                destFile = new File(destPath);
                if (isExcludePath(srcFile, excludes)) continue;
                if (ConfigHelper.isDebugMode()) LogHelper.writeInfoFormat("checkDirectory", srcPath);
                if (destFile.exists() == false) {
                    if (FileHelper.createDestinationFolder(destFile) == false) LogHelper.writeErrorFormat("directoryNotCreated", destPath);
                }
                if (srcFile.canRead() == false) {
                    LogHelper.writeErrorFormat("directoryNotReadable", srcPath);
                    continue;
                }
                List<String> files = listFiles(srcPath, src);
                if (files == null) continue;
                while (files.size() > 0) {
                    CommonHelper.sleep(1);
                    srcBuilder = resetStringBuilder(srcBuilder, src);
                    destBuilder = resetStringBuilder(destBuilder, dest);
                    String relFilePath = files.remove(0);
                    srcPath = srcBuilder.append(relFilePath).toString();
                    destPath = destBuilder.append(relFilePath).toString();
                    srcFile = new File(srcPath);
                    destFile = new File(destPath);
                    if (ConfigHelper.isDebugMode()) LogHelper.writeInfoFormat("checkFile", srcPath);
                    if (srcFile.canRead() == false) {
                        LogHelper.writeErrorFormat("fileNotReadable", srcPath);
                        continue;
                    }
                    if (ConfigHelper.useHashing() == true) {
                        if (HashHelper.isHashFile(srcFile.getName()) == true) continue;
                        String currentHash = null;
                        String fileHash = HashHelper.getCheckSum(srcPath);
                        String destCheckParentPath = destFile.getParent();
                        if (ConfigHelper.useHashFile() == true) currentHash = HashHelper.getHashFromFile(destCheckParentPath, srcFile.getName()); else currentHash = HashHelper.getCheckSum(destPath);
                        if (fileHash.equals(currentHash) == false) {
                            if (currentHash != null && ConfigHelper.isDebugMode() == true) LogHelper.writeInfoFormat("hashChanged", fileHash, destPath, currentHash, destCheckParentPath);
                            HashHelper.writeHashToFile(dest, srcFile.getName(), fileHash);
                            FileManager.copyFile(srcFile, destFile);
                            continue;
                        }
                    } else if (fileChanged(srcFile, destFile) == true) {
                        FileManager.copyFile(srcFile, destFile);
                        continue;
                    }
                }
            }
        }
    }

    /**
	 * Checks if the File is in the exclude paths
	 * @param file
	 * @param excludes
	 * @return
	 */
    private boolean isExcludePath(File file, List<String> excludes) {
        if (excludes == null) return false;
        for (String path : excludes) {
            File checkFile = new File(path);
            if (file.equals(checkFile)) return true;
        }
        return false;
    }

    /**
	 * Deletes the selected directory and containing files
	 * @param dir Target Directory that should deleted
	 */
    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            if (dir.delete() == false) {
                LogHelper.writeErrorFormat("directoryNotRemoved", dir.getAbsolutePath());
                return;
            }
        } else {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (file.delete() == false) {
                        LogHelper.writeErrorFormat("fileNotRemoved", file.getAbsolutePath());
                    }
                }
            }
            if (dir.delete() == false) {
                LogHelper.writeErrorFormat("directoryNotRemoved", dir.getAbsolutePath());
                return;
            }
        }
    }

    /**
	 * Deletes the selected file
	 * @param file File that should be deleted
	 */
    private void deleteFile(File file) {
        if (file == null) return;
        try {
            if (file.exists()) {
                if (file.delete() == false) {
                    LogHelper.writeErrorFormat("fileNotRemoved", file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            LogHelper.writeException(e);
        }
    }

    /**
	 * Checks if the source and destination file equals in length, file type, lastModified Timestamp and if they exists 
	 * @param source Source File
	 * @param target Destination File
	 * @return true if equals, false if not
	 */
    private boolean filesAreEqual(File source, File target) {
        if (source == null || target == null) return false;
        if (source.exists() == false) return false;
        if (source.isDirectory() != target.isDirectory()) return false;
        if (source.isFile() != target.isFile()) return false;
        if (source.lastModified() > target.lastModified()) return false;
        if (source.length() != target.length()) return false;
        return true;
    }

    /**
	 * Checks if the files changed in length or lastModified Timestamp
	 * @param srcFile
	 * @param destFile
	 * @return true if changed or false if not
	 */
    private boolean fileChanged(File srcFile, File destFile) {
        if (destFile.exists() == false) return true;
        if (srcFile.length() != destFile.length() || srcFile.lastModified() > destFile.lastModified()) return true; else return false;
    }

    /**
	 *  Checks if free space left on the target device
	 * @param src Source Path
	 * @param dest Destination Path
	 * @return true if free space left or false if not
	 */
    private boolean hasFreeSpace(String src, String dest) {
        if (ConfigHelper.isDebugMode()) LogHelper.writeInfoFormat("beginCheckTargetSpace", src, dest);
        boolean hasTargetFreeSpace = DeviceHelper.hasTargetFreeSpace(src, dest);
        if (ConfigHelper.isDebugMode()) LogHelper.writeInfoFormat("finishedCheckTargetSpace", src, dest);
        if (hasTargetFreeSpace == false) LogHelper.writeErrorFormat("noUsableSpace", dest);
        return hasTargetFreeSpace;
    }

    /**
	 * Lists all relative file paths in the internal path list
	 * @param rootPath The root folder that should listed
	 */
    private void listRelativeFolderPaths(String rootPath) {
        File rootFolder = new File(rootPath);
        if (rootFolder.exists() == false) return;
        if (rootFolder.isDirectory() == false) return;
        List<File> subFolders = listSubFolders(rootPath);
        if (subFolders == null) return;
        for (int i = 0; i < subFolders.size(); i++) {
            File subFolder = subFolders.get(i);
            List<File> tempSubFolders = listSubFolders(subFolder);
            if (tempSubFolders != null) subFolders.addAll(tempSubFolders);
            String relPath = getRelativePath(subFolder.getAbsolutePath(), rootPath);
            Folders.add(relPath);
        }
        subFolders = null;
        Collections.sort(Folders);
    }

    /**
	 * Gets the relative path from the current path
	 * @param path Full Path of the current File
	 * @param rootPath Root of the source/destination setting
	 * @return The relative path
	 */
    private String getRelativePath(String path, String rootPath) {
        if (path.equals(rootPath)) return null;
        return path.substring(rootPath.length());
    }

    /**
	 * Lists the subfolder paths
	 * @param path Path of the root folder
	 * @return List of the Subfolders or null if non avaible
	 */
    private List<File> listSubFolders(String path) {
        File folder = new File(path);
        return listSubFolders(folder);
    }

    /**
	 * Lists the subfolder paths
	 * @param folder Path of the root folder
	 * @return List of subfolders or null if non avaible
	 */
    private List<File> listSubFolders(File folder) {
        File[] subFolders = folder.listFiles(new FolderFilter());
        if (subFolders == null) return null;
        List<File> folders = new ArrayList<File>();
        for (int i = 0; i < subFolders.length; i++) {
            File f = subFolders[i];
            folders.add(f);
        }
        return folders;
    }

    /**
	 * Lists the relative file paths 
	 * @param path Full path to the selected folder
	 * @param rootPath Root path for relative paths 
	 * @return List of relative file paths or null if non files availabe or folder not exists
	 */
    private List<String> listFiles(String path, String rootPath) {
        File folder = new File(path);
        if (folder.exists() == false) return null;
        File[] files = folder.listFiles(new FileFilter());
        if (files == null) return null;
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String relPath = getRelativePath(file.getAbsolutePath(), rootPath);
            list.add(relPath);
        }
        Collections.sort(list);
        return list;
    }

    /**
	 * Resets the StringBuilder to the root path length
	 * @param builder StringBuilder to clear
	 * @param rootPath Root path for length reset 
	 * @return the StringBuilder with the new length
	 */
    private StringBuilder resetStringBuilder(StringBuilder builder, String rootPath) {
        builder.setLength(rootPath.length());
        return builder;
    }
}
