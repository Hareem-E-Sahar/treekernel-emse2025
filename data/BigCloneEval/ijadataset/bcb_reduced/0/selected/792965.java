package de.mpiwg.vspace.filehandler.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import de.mpiwg.vspace.extension.ExceptionHandlingService;

/**
 * Class for simply file handling. 
 * @author Julia Damerow
 *
 */
public class FileHandler {

    /**
	 * Retrieve a file object with absolute path from a project.
	 * @param pluginID The id of the project, the file is located in.
	 * @param path The path of the file relative to the project.
	 * @return The requested file.
	 */
    public static File getAbsoluteFileFromProject(String pluginID, String path) {
        URL url = FileLocator.find(Platform.getBundle(pluginID), new Path(path), null);
        if (url == null) return null;
        return getAbsoluteFileFromRelativeUrl(url);
    }

    public static URL getRelativeURLFromProject(String pluginID, String path) {
        URL url = FileLocator.find(Platform.getBundle(pluginID), new Path(path), null);
        return url;
    }

    /**
	 * Retrieve all files in a given folder.
	 * @param pluginIDThe id of the project, the folder is located in.
	 * @param path The path of the folder relative to the project.
	 * @return Array of files that are contained in the folder.
	 */
    public static File[] getMultipleAbsoluteFilesFromProject(String pluginID, String path) {
        URL url = FileLocator.find(Platform.getBundle(pluginID), new Path(path), null);
        if (url == null) return null;
        File folder = getAbsoluteFileFromRelativeUrl(url);
        if (folder == null) return null;
        File[] files = folder.listFiles();
        if (files == null) return null;
        List<File> fileList = new ArrayList<File>();
        for (File file : files) {
            if (!file.getName().equals(".svn")) fileList.add(file);
        }
        return fileList.toArray(new File[fileList.size()]);
    }

    /**
	 * Retrieve a file via a project relative URL.
	 * @param url The project relative url of the file.
	 * @return The requested file with an absolute path.
	 */
    public static File getAbsoluteFileFromRelativeUrl(URL url) {
        URL convertedUrl = null;
        try {
            convertedUrl = FileLocator.toFileURL(url);
        } catch (IOException e2) {
            ExceptionHandlingService.INSTANCE.handleException(e2);
            return null;
        }
        IPath ipath = null;
        try {
            String convertedUrlStr = convertedUrl.toString().replace(" ", "%20");
            URL encodedURL = new URL(convertedUrlStr);
            ipath = new Path(encodedURL.toURI().toString());
        } catch (URISyntaxException e2) {
            ExceptionHandlingService.INSTANCE.handleException(e2);
            return null;
        } catch (MalformedURLException e) {
            ExceptionHandlingService.INSTANCE.handleException(e);
            return null;
        }
        if (ipath.getDevice() != null) {
            ipath = ipath.setDevice(null);
        }
        if (System.getProperty("os.name").equals("Mac OS X")) {
            if (ipath.toString().startsWith("file:")) {
                ipath = ipath.removeFirstSegments(1);
            }
        }
        String absolutePath = ipath.toOSString();
        if (!absolutePath.startsWith(File.separator)) absolutePath = File.separator + absolutePath;
        absolutePath = absolutePath.replace("%20", " ");
        return new File(absolutePath);
    }

    /**
	 * Retrieve all files of a folder with the given file extension.
	 * @param pluginId The id of the project, the folder is located in.
	 * @param path The path of the folder.
	 * @param fileExtension The file extension. If the file extension does not start
	 * with a dot, all files with an extension that ends with the given extension are found
	 * too (e.g. ".html" return all html-files, "html" also returns all xhtml files).
	 * @return An array of the found files.
	 */
    public static File[] findFiles(String pluginId, String path, String fileExtension) {
        URL[] urls = FileLocator.findEntries(Platform.getBundle(pluginId), new Path(path));
        if ((urls == null) || (urls.length == 0)) return null;
        File folder = getAbsoluteFileFromRelativeUrl(urls[0]);
        File[] filesInFolder = folder.listFiles();
        if ((filesInFolder == null) || (filesInFolder.length == 0)) return null;
        List<File> files = new ArrayList<File>();
        for (File file : filesInFolder) {
            if (file.getPath().endsWith(fileExtension)) files.add(file);
        }
        return files.toArray(new File[files.size()]);
    }

    public static String getAbsolutePath(String pluginId, String relativePath) {
        File file = getAbsoluteFileFromProject(pluginId, relativePath);
        if (file == null) return "";
        return file.getAbsolutePath();
    }

    /**
	 * Reads a text file in UTF-8 encoding.
	 * @param file The file to be read.
	 * @return Content of file as String.
	 */
    public static String readTextFile(File file) {
        StringBuilder contents = new StringBuilder();
        try {
            Charset charset = Charset.forName("UTF-8");
            BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset.newDecoder()));
            try {
                String line = null;
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ExceptionHandlingService.INSTANCE.handleException(ex);
        }
        return contents.toString();
    }

    public static File writeTextFile(File file, String content) {
        if (!file.exists()) try {
            file.createNewFile();
        } catch (IOException e1) {
            ExceptionHandlingService.INSTANCE.handleException(e1);
            return null;
        }
        BufferedWriter bufferedWriter = null;
        Charset charset = Charset.forName("UTF-8");
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset.newEncoder()));
            bufferedWriter.write(content);
            bufferedWriter.flush();
            bufferedWriter.close();
            return file;
        } catch (IOException e) {
            ExceptionHandlingService.INSTANCE.handleException(e);
        }
        return null;
    }

    public static File writeStreamToFile(File file, InputStream stream) {
        BufferedInputStream in = null;
        BufferedOutputStream outWriter = null;
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e1) {
                ExceptionHandlingService.INSTANCE.handleException(e1);
                return null;
            }
        }
        try {
            in = new BufferedInputStream(stream);
            outWriter = new BufferedOutputStream(new FileOutputStream(file));
            int c;
            while ((c = in.read()) != -1) outWriter.write(c);
            in.close();
            outWriter.close();
        } catch (FileNotFoundException e) {
            ExceptionHandlingService.INSTANCE.handleException(e);
            return null;
        } catch (IOException e) {
            ExceptionHandlingService.INSTANCE.handleException(e);
            return null;
        }
        return file;
    }

    /**
	 * Method for copying files from one location to another. 
	 * 
	 * @param fileToCopy The file to be copied.
	 * @param copiedFile File-handle to the new location of the file. 
	 * The underlying file will be created if it doesn't exist.
	 * @return The file-handle to the new location.
	 */
    public static File copyFile(File fileToCopy, File copiedFile) {
        BufferedInputStream in = null;
        BufferedOutputStream outWriter = null;
        if (!copiedFile.exists()) {
            try {
                copiedFile.createNewFile();
            } catch (IOException e1) {
                ExceptionHandlingService.INSTANCE.handleException(e1);
                return null;
            }
        }
        try {
            in = new BufferedInputStream(new FileInputStream(fileToCopy), 4096);
            outWriter = new BufferedOutputStream(new FileOutputStream(copiedFile), 4096);
            int c;
            while ((c = in.read()) != -1) outWriter.write(c);
            in.close();
            outWriter.close();
        } catch (FileNotFoundException e) {
            ExceptionHandlingService.INSTANCE.handleException(e);
            return null;
        } catch (IOException e) {
            ExceptionHandlingService.INSTANCE.handleException(e);
            return null;
        }
        return copiedFile;
    }

    public static File downloadFile(String url, File targetFile) {
        URL uURL;
        InputStream is = null;
        DataInputStream dis = null;
        BufferedOutputStream outWriter = null;
        if (!targetFile.exists()) try {
            targetFile.createNewFile();
        } catch (IOException e) {
            ExceptionHandlingService.INSTANCE.handleException(e);
            return null;
        }
        try {
            uURL = new URL(url);
            is = uURL.openStream();
            dis = new DataInputStream(new BufferedInputStream(is));
            outWriter = new BufferedOutputStream(new FileOutputStream(targetFile), 4096);
            int c;
            while ((c = dis.read()) != -1) outWriter.write(c);
        } catch (MalformedURLException mue) {
            ExceptionHandlingService.INSTANCE.handleException(mue);
        } catch (IOException ioe) {
            ExceptionHandlingService.INSTANCE.handleException(ioe);
        } finally {
            try {
                if (is != null) is.close();
                if (dis != null) dis.close();
                if (outWriter != null) outWriter.close();
            } catch (IOException ioe) {
            }
        }
        return targetFile;
    }
}
