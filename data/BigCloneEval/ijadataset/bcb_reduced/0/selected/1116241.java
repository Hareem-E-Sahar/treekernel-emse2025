package com.foursoft.component.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Some file utilities.
 * 
 * @version $Revision: 1.18 $
 * 
 * 
 */
public class FileUtilities {

    /** Field BUFFER_SIZE */
    private static final int BUFFER_SIZE = 4096;

    /**
	 * Copy the contents of the source reader to the destination writer.
	 * 
	 * @param source
	 *            Source reader.
	 * @param destination
	 *            Destination writer.
	 * 
	 * @throws IOException
	 *             the file couldn't be copied
	 */
    public static void copy(Reader source, Writer destination) throws IOException {
        char[] buffer = new char[BUFFER_SIZE];
        int n;
        while ((n = source.read(buffer)) > 0) {
            destination.write(buffer, 0, n);
            destination.flush();
        }
        destination.close();
        source.close();
    }

    /**
	 * Copy the contents from the source input stream to the destination output
	 * stream.
	 * 
	 * @param source
	 *            Source input stream.
	 * @param destination
	 *            Destination output stream.
	 * 
	 * @throws IOException
	 *             the file couldn't be copied
	 */
    public static void copy(InputStream source, OutputStream destination) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int n;
        while ((n = source.read(buffer)) > 0) {
            destination.write(buffer, 0, n);
            destination.flush();
        }
        destination.close();
        source.close();
    }

    /**
	 * Copy the contents of the source file to the destination file. The
	 * implementation is based on readers and writers.
	 * 
	 * @param source
	 *            Source file.
	 * @param destination
	 *            Destination file.
	 * 
	 * @throws IOException
	 *             the file couldn't be copied
	 */
    public static void copyTextFile(File source, File destination) throws IOException {
        copy(new FileReader(source), new FileWriter(destination));
    }

    /**
	 * Copy the contents of the source file to the destination file. The
	 * implementation is based on input and output streams.
	 * 
	 * @param source
	 *            Source file.
	 * @param destination
	 *            Destination file.
	 * 
	 * @throws IOException
	 *             the file couldn't be copied
	 */
    public static void copyBinaryFile(File source, File destination) throws IOException {
        copy(new FileInputStream(source), new FileOutputStream(destination));
    }

    /**
	 * Copy the source directory to the target directoy recursively. Source and
	 * target dir have to be directories otherwise nothing is copied. If the
	 * target directory does not exist, it will be created.
	 * 
	 * @param source
	 *            Source directory.
	 * @param target
	 *            Target directory.
	 * @param overwrite
	 *            should the target file be overwritten?
	 * 
	 * @throws IOException
	 *             the directory couldn't be copied
	 */
    public static void copyDirectoryRecursively(File source, File target, boolean overwrite) throws IOException {
        assert source != null;
        assert target != null;
        copyDirectoryRecursively(source, target, overwrite, AllFileFilter.getInstance(), false);
    }

    /**
	 * Copy the source directory to the target directoy recursively. Source and
	 * target dir have to be directories otherwise nothing is copied. FileFilter
	 * is used for copy. If the target directory does not exist, it will be
	 * created.
	 * 
	 * @param source
	 *            Source directory.
	 * @param target
	 *            Target directory.
	 * @param overwrite
	 *            Should the target file be overwritten?
	 * @param fileFilter
	 *            The file filter to use
	 * @param filter
	 *            A FileFilter for choosing files/folder tom copy.
	 * @param temporary
	 *            True, if copies should be delete on exit, see
	 *            File#deleteOnExit()
	 * 
	 * @throws IOException
	 *             the directory couldn't be copied
	 */
    public static void copyDirectoryRecursively(File source, File target, boolean overwrite, FileFilter fileFilter, boolean temporary) throws IOException {
        assert source != null;
        assert target != null;
        final FileFilter filter = fileFilter != null ? fileFilter : AllFileFilter.getInstance();
        if (source.equals(target)) {
            return;
        }
        if (!source.isDirectory()) {
            throw new IOException("Couldn't copy, the source '" + source.getAbsolutePath() + "' is not a directory");
        }
        File[] acceptedFiles = source.listFiles(filter);
        File[] leftChildFolders = source.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                return !filter.accept(pathname) && DirectoryFileFilter.getInstance().accept(pathname);
            }
        });
        if (acceptedFiles.length > 0 && !target.exists() && !target.mkdirs()) {
            throw new IOException("Couldn't create target directory: " + target.getAbsolutePath());
        }
        for (int i = 0; i < acceptedFiles.length; i++) {
            if (acceptedFiles[i].isDirectory()) {
                File newTarget = new File(target.getPath(), acceptedFiles[i].getName());
                if (temporary) {
                    newTarget.deleteOnExit();
                }
                copyDirectoryRecursively(acceptedFiles[i], newTarget, overwrite, AllFileFilter.getInstance(), temporary);
            } else if (acceptedFiles[i].isFile()) {
                File src = acceptedFiles[i].getAbsoluteFile();
                File dst = new File(target + File.separator + acceptedFiles[i].getName());
                if (src.equals(dst)) {
                    continue;
                }
                if (dst.exists()) {
                    if (overwrite) {
                        dst.delete();
                    } else {
                        continue;
                    }
                }
                if (temporary) {
                    dst.deleteOnExit();
                }
                FileUtilities.copyBinaryFile(src, dst);
            } else {
                throw new IOException(acceptedFiles[i] + "' isn't file or directory, what else?");
            }
        }
        for (int i = 0; i < leftChildFolders.length; i++) {
            File newTarget = new File(target.getPath(), leftChildFolders[i].getName());
            if (temporary) {
                newTarget.deleteOnExit();
            }
            copyDirectoryRecursively(leftChildFolders[i], newTarget, overwrite, filter, temporary);
        }
    }

    /**
	 * break a path down into individual elements and add to a list. example :
	 * if a path is /a/b/c/d.txt, the breakdown will be [d.txt,c,b,a]
	 * 
	 * @param f
	 *            input file
	 * @return a List collection with the individual elements of the path in
	 *         reverse order
	 */
    private static List<String> getPathList(File f) {
        List<String> l = new ArrayList<String>();
        File r;
        try {
            r = f.getCanonicalFile();
            while (r != null) {
                l.add(r.getName());
                r = r.getParentFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
            l = null;
        }
        return l;
    }

    /**
	 * figure out a string representing the relative path of 'f' with respect to
	 * 'r'
	 * 
	 * @param r
	 *            home path
	 * @param f
	 *            path of file
	 * @return the string
	 */
    private static String matchPathLists(List<String> r, List<String> f) {
        assert r != null;
        assert f != null;
        int i;
        int j;
        String s = new String();
        i = r.size() - 1;
        j = f.size() - 1;
        while ((i >= 0) && (j >= 0) && (r.get(i).equals(f.get(j)))) {
            i--;
            j--;
        }
        for (; i >= 0; i--) {
            s += (".." + File.separator);
        }
        for (; j >= 1; j--) {
            s += (f.get(j) + File.separator);
        }
        s += f.get(j);
        return s;
    }

    /**
	 * get relative path of File 'f' with respect to 'home' directory example :
	 * home = /a/b/c/ f = /a/d/e/x.txt s = getRelativePath(home,f) =
	 * ../../d/e/x.txt
	 * 
	 * @param home
	 *            base path, should be a directory, not a file, or it doesn't
	 *            make sense
	 * @param f
	 *            file to generate path for
	 * @return path from home to f as a string
	 */
    public static String getRelativePath(File home, File f) {
        List<String> homelist;
        List<String> filelist;
        String s;
        File myHome = new File(home.getAbsolutePath());
        if (!myHome.isDirectory()) {
            try {
                myHome = myHome.getCanonicalFile();
                myHome = myHome.getParentFile();
            } catch (IOException ex) {
                return null;
            }
        }
        f = new File(f.getAbsolutePath());
        try {
            f = f.getCanonicalFile();
        } catch (IOException ex) {
            return null;
        }
        homelist = getPathList(myHome);
        filelist = getPathList(f);
        s = matchPathLists(homelist, filelist);
        return s;
    }

    /**
	 * Deletes a given directory or file recursively.
	 * 
	 * @param f
	 *            the directory or file to be deleted.
	 */
    public static void deleteFileRecursively(File f) {
        if (f.exists()) {
            if (f.isDirectory()) {
                File[] filearray = f.listFiles();
                if (filearray.length > 0) {
                    for (int i = 0; i < filearray.length; i++) {
                        deleteFileRecursively(filearray[i]);
                    }
                }
            }
            f.delete();
        }
    }

    /**
	 * Clean any relative file parts from a file path.
	 * 
	 * @param file
	 * @return the cleaned path
	 ***/
    public static File cleanRelativePartsFromPath(File file) {
        File returnFile = file;
        if (returnFile != null) {
            try {
                returnFile = returnFile.getCanonicalFile();
            } catch (IOException ex) {
            }
            assert ((returnFile.getAbsolutePath().indexOf("../") == -1) && (returnFile.getAbsolutePath().indexOf("..\\") == -1));
        }
        return returnFile;
    }

    /**
	 * Get the common path between two files. i.e. if file (a) is
	 * C://temp/dirx/dirM/DirN and file (b) is C://temp/dirx/dirA/DirB/DirC the
	 * common directory would be the last point that they have in common which
	 * is C://temp/dirx
	 * 
	 * @param fileOne
	 *            first file
	 * @param fileTwo
	 *            second file
	 * @return the common path
	 **/
    public static String getCommonBasePath(File fileOne, File fileTwo) {
        List<String> pathForFileOne = getPathList(fileOne);
        List<String> pathForFileTwo = getPathList(fileTwo);
        String driveForOne = getDrive(fileOne);
        String driveForTwo = getDrive(fileTwo);
        pathForFileOne.add(driveForOne);
        pathForFileTwo.add(driveForTwo);
        Collections.reverse(pathForFileOne);
        Collections.reverse(pathForFileTwo);
        String commonBasePath = "";
        int smallestListLength = (pathForFileOne.size() < pathForFileTwo.size()) ? pathForFileOne.size() : pathForFileTwo.size();
        for (int i = 0; i < smallestListLength; i++) {
            String itemOne = pathForFileOne.get(i);
            String itemTwo = pathForFileTwo.get(i);
            if ((itemOne.trim().length() == 0) && (itemTwo.trim().length() == 0)) {
                continue;
            }
            if (itemOne.equals(itemTwo)) {
                commonBasePath += (itemOne + "/");
            }
        }
        commonBasePath = commonBasePath.replace('\\', '/');
        return commonBasePath;
    }

    /**
	 * does the passed file begin with the the file protocol string. This is the
	 * prefix "file:///"
	 * 
	 * @param filename
	 *            the filename to be checked
	 * @return true, if this is a file-URL
	 * **/
    public static boolean hasFileProtocolPrefix(String filename) {
        boolean hasProtocolPrefix = false;
        if (filename != null) {
            filename = filename.trim();
            hasProtocolPrefix = filename.startsWith("file:///");
        }
        return hasProtocolPrefix;
    }

    /**
	 * remove the file protocol string from the passed file name. This is the
	 * prefix "file:///"
	 * 
	 * @param filename
	 *            the filename
	 * @return the new filename
	 **/
    public static String removeFileProtocolPrefix(String filename) {
        String withoutProtocol = filename;
        if ((filename != null) && hasFileProtocolPrefix(filename)) {
            withoutProtocol = filename.trim().substring("file:///".length());
        }
        return withoutProtocol;
    }

    /**
	 * Return the drive for this file
	 * 
	 * @param file
	 *            the given file
	 * @return the drive information
	 **/
    public static String getDrive(File file) {
        File safeFile = cleanRelativePartsFromPath(file);
        while (safeFile.getParentFile() != null) {
            safeFile = safeFile.getParentFile();
        }
        return safeFile.getAbsolutePath();
    }

    /**
	 * Searches into a directory for a file with matching file name (case
	 * insensitive).
	 * 
	 * @param startFile
	 *            directory to start searching.
	 * @param searchedFileName
	 *            the name of the file to find.
	 * @param recursiveSearch
	 *            set recursive search enabled/disabled
	 * @return the file with matching file name, otherwise null.
	 */
    public static File find(File startFile, String searchedFileName, boolean recursiveSearch) {
        assert startFile != null;
        assert searchedFileName != null && searchedFileName.length() > 0;
        final Stack<File> dirs = new Stack<File>();
        if (startFile.isDirectory()) {
            dirs.push(startFile);
        } else if (startFile.isFile() && startFile.getName().equalsIgnoreCase(searchedFileName)) {
            return startFile;
        }
        while (dirs.size() > 0) {
            for (File file : dirs.pop().listFiles()) {
                if (file.isDirectory() && recursiveSearch) dirs.push(file); else if (file.getName().equalsIgnoreCase(searchedFileName)) return file;
            }
        }
        return null;
    }

    /**
	 * Filters all directories.
	 */
    private static class DirectoryFileFilter implements FileFilter {

        private static FileFilter _self = null;

        /**
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
        public boolean accept(File pathname) {
            if (pathname != null && pathname.exists() && pathname.isDirectory()) {
                return true;
            }
            return false;
        }

        /**
		 * @return the FileFilter instance
		 */
        public static FileFilter getInstance() {
            if (_self == null) {
                _self = new DirectoryFileFilter();
            }
            return _self;
        }
    }

    /**
	 * Filters all files/directories.
	 */
    private static class AllFileFilter implements FileFilter {

        private static FileFilter _self = null;

        /**
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
        public boolean accept(File pathname) {
            if (pathname != null && pathname.exists()) {
                return true;
            }
            return false;
        }

        /**
		 * @return the FileFilter instance
		 */
        public static FileFilter getInstance() {
            if (_self == null) {
                _self = new AllFileFilter();
            }
            return _self;
        }
    }
}
