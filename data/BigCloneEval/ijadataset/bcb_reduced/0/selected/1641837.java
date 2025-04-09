package com.javapda.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import com.javapda.app.camera.gui.CameraApp;

public abstract class FileUtil {

    public static Properties getPropertiesFromClasspathFile(String classpathFileName) {
        Properties props = new Properties();
        InputStream is = CameraApp.class.getClassLoader().getResourceAsStream(classpathFileName);
        if (is == null) {
            System.err.println("Cannot find classpathFileName: " + classpathFileName);
            return props;
        }
        try {
            props.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    /**
	 * Loads a file from an input stream and returns it as a byte array.
	 * 
	 * @param _file
	 *            The file to load
	 * @return The file as a byte array
	 * @throws IOException
	 */
    public static byte[] getFileAsBytes(File _file) throws IOException {
        byte[] fileAsBytes = null;
        InputStream is = null;
        try {
            is = new FileInputStream(_file);
            if (is == null) {
                throw new FileNotFoundException(_file + "...make sure it is on the class path");
            }
            fileAsBytes = convertInputStreamToBytes(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return fileAsBytes;
    }

    /**
	 * finds the provided file on the class path and converts it to a byte
	 * array.
	 * 
	 * @param pathToFileOnClasspath
	 * @return
	 * @throws IOException
	 */
    public static byte[] getClasspathFileAsBytes(String pathToFileOnClasspath) throws IOException {
        byte[] fileAsBytes = null;
        InputStream is = null;
        try {
            is = FileUtil.class.getResourceAsStream(pathToFileOnClasspath);
            if (is == null) {
                throw new FileNotFoundException(pathToFileOnClasspath + "...make sure it is on the class path");
            }
            fileAsBytes = convertInputStreamToBytes(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return fileAsBytes;
    }

    /**
	 * converts the given input stream to a byte array
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
    public static byte[] convertInputStreamToBytes(InputStream is) throws IOException {
        byte[] inputStreamAsBytes = null;
        byte[] buffer = new byte[1000];
        int length;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        inputStreamAsBytes = baos.toByteArray();
        return inputStreamAsBytes;
    }

    /**
	 * Creates or overwrites a file with the provided data.
	 * 
	 * @param data
	 * @param file
	 */
    public static void writeBytesToFile(byte[] data, File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("Cannot write bytes to a directory: " + file.getAbsolutePath());
            } else if (!file.canWrite()) {
                throw new IOException("Cannot write to file: " + file.getAbsolutePath());
            }
        }
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(data);
            bos.close();
        } finally {
            if (bos != null) {
                bos.close();
            }
        }
    }

    /**
	 * 
	 * @param _file
	 * @return
	 * @throws IOException
	 */
    public static byte[] readBytesFromFile(File _file) throws IOException {
        if (!_file.exists()) {
            throw new IOException("File does not exist : " + _file.getAbsolutePath());
        }
        if (_file.isDirectory()) {
            throw new IOException("Cannot read files from a directory : " + _file.getAbsolutePath());
        }
        long length = _file.length();
        if (length > Integer.MAX_VALUE) {
            throw new IOException("The file is too large : " + _file.getAbsolutePath());
        }
        byte[] bytes = new byte[(int) length];
        InputStream is = new FileInputStream(_file);
        readBytesFromInputStream(bytes, is);
        is.close();
        return bytes;
    }

    /**
	 * 
	 * @param _jarFileName
	 * @param _entryName
	 * @return
	 */
    public static byte[] readBytesFromZipEntry(String _jarFileName, String _entryName) throws IOException {
        JarFile jarFile = new JarFile(_jarFileName);
        ZipEntry entry = new ZipEntry(_entryName);
        long length = entry.getSize();
        if (length > Integer.MAX_VALUE) {
            throw new IOException("The file is too large : " + jarFile.getName() + ":" + entry.getName());
        }
        byte[] bytes = new byte[(int) length];
        InputStream is = jarFile.getInputStream(entry);
        readBytesFromInputStream(bytes, is);
        is.close();
        jarFile.close();
        return bytes;
    }

    /**
	 * 
	 * @param _bytes
	 * @param _is
	 * @return
	 * @throws IOException
	 */
    public static byte[] readBytesFromInputStream(byte[] _bytes, InputStream _is) throws IOException {
        int offset = 0;
        int numRead = 0;
        while (offset < _bytes.length && (numRead = _is.read(_bytes, offset, _bytes.length - offset)) >= 0) {
            offset += numRead;
        }
        if (offset < _bytes.length) {
            throw new IOException("Could not completely read from the InputStream");
        }
        return _bytes;
    }

    public static void close(Object obj) {
        if (obj instanceof InputStream) {
            try {
                ((InputStream) obj).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
	 * Delete a directory, subdirs and all contents.
	 * 
	 * @param _dir
	 */
    public static void recurseDirectoryDelete(File _dir) {
        File[] dList = _dir.listFiles();
        for (int i = 0; i < dList.length; i++) {
            if (dList[i].isDirectory()) recurseDirectoryDelete(dList[i]);
            dList[i].delete();
        }
        _dir.delete();
    }

    /**
	 * 
	 * 
	 * @param _dir
	 */
    public static void recurseDirectoryCopy(File _src, File _dest) {
        File[] srcList = _src.listFiles();
        for (int i = 0; i < srcList.length; i++) {
            String name = srcList[i].getName();
            File dest = new File(_dest, name);
            if (srcList[i].isDirectory()) {
                dest.mkdirs();
                recurseDirectoryCopy(srcList[i], dest);
            } else {
                try {
                    writeBytesToFile(getFileAsBytes(new File(_src, name)), dest);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void writeArrayAsPropertiesToFile(File file, Object[] data) throws IOException {
        PrintWriter pw = null;
        FileWriter fw = null;
        fw = new FileWriter(file);
        pw = new PrintWriter(fw);
        for (int i = 0; i < data.length; i++) {
            Object[] d = (Object[]) data[i];
            pw.print(d[0]);
            pw.print(":");
            pw.println(d[1]);
        }
        pw.flush();
        fw.close();
    }
}
