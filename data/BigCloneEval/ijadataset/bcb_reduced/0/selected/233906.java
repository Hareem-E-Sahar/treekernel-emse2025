package jlib.Helpers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

/**
 * Class containing helpers for creating ZIP archives from folders, files and list of files.
 */
public class ZipHelper {

    /**
 * Zips the content of the specified folder.
 * The create Zip archive preserves the same structure as the specified folder. If the
 * Zip archive already exists, it is overwritten.
 * @param sourceFolder The folder whose content is to be archived.
 * @param destinationZipArchive The zip archive where to archive the folder content.
 * @param moveFiles If <i>true</i>, archived files are erased from the source folder. The source folder
 * itself is not removed.
 */
    public static void zipFolder(File sourceFolder, File destinationZipArchive, boolean moveFiles) throws Exception {
        FileOutputStream fos;
        ZipOutputStream zos;
        try {
            if (!sourceFolder.exists()) throw new Exception("The specified source folder doesn't exist.");
            if (!sourceFolder.isDirectory()) throw new Exception("The specified source folder isn't a folder.");
            if (destinationZipArchive.exists()) {
                if (destinationZipArchive.isDirectory()) throw new Exception("The specified zip archive is a folder.");
                destinationZipArchive.delete();
            }
            fos = new FileOutputStream(destinationZipArchive);
            zos = new ZipOutputStream(fos);
            _zipFolder(sourceFolder, "", zos);
            zos.close();
            fos.close();
            if (moveFiles) _deleteFolder(sourceFolder, "");
        } catch (Exception e) {
            String s1, s2;
            if (sourceFolder == null) s1 = "null"; else s1 = sourceFolder.getAbsolutePath();
            if (destinationZipArchive == null) s2 = "null"; else s2 = destinationZipArchive.getAbsolutePath();
            throw new Exception(ParseError.parseError("ApplicationHelper.zipFolder('" + s1 + "','" + s2 + "'," + moveFiles + ")", e));
        }
    }

    private static void _deleteFolder(File baseFolder, String path) throws Exception {
        File currentFolder;
        File[] contents;
        int n, nn;
        File content;
        try {
            currentFolder = new File(baseFolder, path);
            if (!currentFolder.exists()) throw new Exception("Folder '" + baseFolder + "' doesn't exist.");
            if (!currentFolder.isDirectory()) throw new Exception("'" + currentFolder + "' is not a folder.");
            if (path == null) path = "";
            if (path.length() > 0) path += "/";
            contents = currentFolder.listFiles();
            nn = contents.length;
            for (n = 0; n < nn; n++) {
                content = contents[n];
                if (content.isDirectory()) {
                    _deleteFolder(baseFolder, path + content.getName());
                    content.delete();
                } else content.delete();
            }
        } catch (Exception e) {
            String s1;
            if (baseFolder == null) s1 = "null"; else s1 = baseFolder.getAbsolutePath();
            throw new Exception(ParseError.parseError("ApplicationHelper._deleteFolder('" + s1 + "','" + path + "')", e));
        }
    }

    private static void _zipFolder(File baseFolder, String path, ZipOutputStream zos) throws Exception {
        File currentFolder;
        File[] contents;
        int n, nn;
        File content;
        ZipEntry ze;
        BufferedInputStream bis;
        byte buffer[] = new byte[1000];
        int bufferSize = buffer.length;
        int bytesRead;
        try {
            currentFolder = new File(baseFolder, path);
            if (!currentFolder.exists()) throw new Exception("Folder '" + baseFolder + "' doesn't exist.");
            if (!currentFolder.isDirectory()) throw new Exception("'" + currentFolder + "' is not a folder.");
            if (path == null) path = "";
            if (path.length() > 0) path += "/";
            contents = currentFolder.listFiles();
            nn = contents.length;
            for (n = 0; n < nn; n++) {
                content = contents[n];
                if (content.isDirectory()) {
                    _zipFolder(baseFolder, path + content.getName(), zos);
                } else {
                    ze = new ZipEntry(path + content.getName());
                    try {
                        zos.putNextEntry(ze);
                        bis = new BufferedInputStream(new FileInputStream(content));
                        for (; ; ) {
                            bytesRead = bis.read(buffer, 0, bufferSize);
                            if (bytesRead <= 0) break;
                            zos.write(buffer, 0, bytesRead);
                        }
                        bis.close();
                        zos.closeEntry();
                    } catch (ZipException e) {
                        if (!e.getMessage().startsWith("duplicate entry")) throw new Exception(ParseError.parseError("Error zipping '" + content.getAbsolutePath() + "': ", e));
                    }
                }
            }
        } catch (Exception e) {
            String s1;
            if (baseFolder == null) s1 = "null"; else s1 = baseFolder.getAbsolutePath();
            throw new Exception(ParseError.parseError("ApplicationHelper._zipFolder('" + s1 + "','" + path + "',ZipOutputStream)", e));
        }
    }

    /**
 * Zips the specified file.
 * @param file The file to archive.
 * @param destinationZipArchive Specifies the zip archive where to store the specified 
 * file. If the zip archive already exists, it is deleted before starting
 * the process. Folders and subfolders are created if needed. If the specified file
 * already exists and it is a folder, an exception is raised.
 * @param moveFile If <i>true</i> the specified file is deleted from its original
 * location after the zip archive has been successfully created.
 */
    public static void zipFile(File file, File destinationZipArchive, boolean moveFile) throws Exception {
        try {
            ArrayList<File> list = new ArrayList<File>();
            list.add(file);
            zipFiles(list, destinationZipArchive, moveFile);
        } catch (Exception e) {
            String sFile = "null";
            if (file != null) sFile = file.getAbsolutePath();
            String sDestinationZipArchive = "null";
            if (destinationZipArchive != null) sDestinationZipArchive = destinationZipArchive.getAbsolutePath();
            throw new Exception(ParseError.parseError("ZipHelper.zipFile('" + sFile + "','" + sDestinationZipArchive + "'," + moveFile, e));
        }
    }

    /**
 * Zips the specified collection of files.
 * The created Zip archive is flat, without any folder hierarchy. This is an
 * example of use:
 * <pre>
 * 	ArrayList<String> list=new ArrayList<String>();
 * 	File []files=FileHelper.getFileList("C:\\MyFolder\\*.*");
 * 	for (int n=0;n&lt;files.length;n++)
 * 		if (files[n].isFile())
 * 			list.add(files[n].getAbsolutePath());
 * 
 * 	ApplicationHelper.zipFiles(list,new File("C:\\MyFolder\MyArchive.zip"),false);
 * </pre>
 * @param files The list of files to add to the zip archive. Entries in the list
 * can be either {@link String} containing full paths to the files, or {@link File}
 * objects. Any other class raise an exception. All files have to exist, and can't be
 * folders (an exception is raised in either case).
 * @param destinationZipArchive Specifies the zip archive where to store the files
 * specified in the list. If the zip archive already exists, it is deleted before starting
 * the process. Folders and subfolders are created if needed. If the specified file
 * already exists and it is a folder, an exception is raised.
 * @param moveFiles If <i>true</i> the specified files are deleted from their original
 * location after the zip archive has been successfully created. No file is deleted
 * before the zip archive is completed. 
 */
    public static void zipFiles(List files, File destinationZipArchive, boolean moveFiles) throws Exception {
        int nFiles, nnFiles;
        String fileName;
        File file;
        FileOutputStream fos;
        ZipOutputStream zos;
        ZipEntry ze;
        FileInputStream fis;
        BufferedInputStream bis;
        byte buffer[] = new byte[1000];
        int bufferSize = buffer.length;
        int bytesRead;
        try {
            if (destinationZipArchive.exists()) {
                if (destinationZipArchive.isDirectory()) throw new Exception("The specified zip archive '" + destinationZipArchive + "' already exists, and it is a folder."); else destinationZipArchive.delete();
            } else {
                File destinationFolder = destinationZipArchive.getParentFile();
                if (!destinationFolder.exists()) if (!destinationFolder.mkdirs()) throw new Exception("Could not create the folder for '" + destinationZipArchive.getAbsolutePath() + "'.");
            }
            fos = new FileOutputStream(destinationZipArchive);
            zos = new ZipOutputStream(fos);
            nnFiles = files.size();
            for (nFiles = 0; nFiles < nnFiles; nFiles++) {
                Object o = files.get(nFiles);
                if (o instanceof String) {
                    fileName = (String) o;
                    file = new File(fileName);
                } else if (o instanceof File) {
                    file = (File) o;
                } else {
                    throw new Exception("Only 'String' and 'File' instances can be specified in the files list.");
                }
                if (!file.exists()) throw new Exception("File '" + file.getAbsolutePath() + "', specified in the files list, doesn't exist.");
                if (file.isDirectory()) throw new Exception("File '" + file.getAbsolutePath() + "', specified in the files list, is actually a folder.");
                ze = new ZipEntry(file.getName());
                try {
                    zos.putNextEntry(ze);
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis);
                    for (; ; ) {
                        bytesRead = bis.read(buffer, 0, bufferSize);
                        if (bytesRead <= 0) break;
                        zos.write(buffer, 0, bytesRead);
                    }
                    fis.close();
                    bis.close();
                    zos.closeEntry();
                } catch (ZipException e) {
                    if (!e.getMessage().startsWith("duplicate entry")) throw new Exception(ParseError.parseError("Error zipping '" + file.getAbsolutePath() + "':", e));
                }
            }
            zos.close();
            fos.close();
            if (moveFiles) {
                nnFiles = files.size();
                for (nFiles = 0; nFiles < nnFiles; nFiles++) {
                    Object o = files.get(nFiles);
                    if (o instanceof String) {
                        fileName = (String) o;
                        file = new File(fileName);
                    } else if (o instanceof File) {
                        file = (File) o;
                    } else {
                        throw new Exception("Only 'String' and 'File' instances can be specified in the files list.");
                    }
                    if (file.exists()) if (file.isFile()) {
                        if (file.delete()) System.out.println("Deleted " + file.getAbsolutePath() + "."); else System.out.println("Could not delete " + file.getAbsolutePath() + ".");
                    }
                }
            }
        } catch (Exception e) {
            String sFiles = "null";
            if (files != null) sFiles = files.size() + " elements.";
            String sDestinationZipArchive = "null";
            if (destinationZipArchive != null) sDestinationZipArchive = destinationZipArchive.getAbsolutePath();
            throw new Exception(ParseError.parseError("ZipHelper.zipFiles('" + sFiles + "','" + sDestinationZipArchive + "'," + moveFiles, e));
        }
    }
}
