package geovista.jts.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * File-related utility functions.
 */
public class FileUtil {

    /**
     * Reads a text file.
     * 
     * @param textFileName
     *                   the pathname of the file to open
     * @return the lines of the text file
     * @throws FileNotFoundException
     *                    if the text file is not found
     * @throws IOException
     *                    if the file is not found or another I/O error occurs
     */
    public static List getContents(String textFileName) throws FileNotFoundException, IOException {
        List contents = new ArrayList();
        FileReader fileReader = new FileReader(textFileName);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = bufferedReader.readLine();
        while (line != null) {
            contents.add(line);
            line = bufferedReader.readLine();
        }
        return contents;
    }

    /**
     * Saves the String to a file with the given filename.
     * 
     * @param textFileName
     *                   the pathname of the file to create (or overwrite)
     * @param contents
     *                   the data to save
     * @throws IOException
     *                    if an I/O error occurs.
     */
    public static void setContents(String textFileName, String contents) throws IOException {
        FileWriter fileWriter = new FileWriter(textFileName, false);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(contents);
        bufferedWriter.flush();
        bufferedWriter.close();
        fileWriter.close();
    }

    public static List getContents(InputStream inputStream) throws IOException {
        ArrayList contents = new ArrayList();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        try {
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            try {
                String line = bufferedReader.readLine();
                while (line != null) {
                    contents.add(line);
                    line = bufferedReader.readLine();
                }
            } finally {
                bufferedReader.close();
            }
        } finally {
            inputStreamReader.close();
        }
        return contents;
    }

    /**
     * Saves the List of Strings to a file with the given filename.
     * 
     * @param textFileName
     *                   the pathname of the file to create (or overwrite)
     * @param lines
     *                   the Strings to save as lines in the file
     * @throws IOException
     *                    if an I/O error occurs.
     */
    public static void setContents(String textFileName, List lines) throws IOException {
        String contents = "";
        for (Iterator i = lines.iterator(); i.hasNext(); ) {
            String line = (String) i.next();
            contents += (line + System.getProperty("line.separator"));
        }
        setContents(textFileName, contents);
    }

    public static void zip(Collection files, File zipFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(zipFile);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            try {
                ZipOutputStream zos = new ZipOutputStream(bos);
                try {
                    for (Iterator i = files.iterator(); i.hasNext(); ) {
                        File file = (File) i.next();
                        zos.putNextEntry(new ZipEntry(file.getName()));
                        FileInputStream fis = new FileInputStream(file);
                        try {
                            BufferedInputStream bis = new BufferedInputStream(fis);
                            try {
                                while (true) {
                                    int j = bis.read();
                                    if (j == -1) {
                                        break;
                                    }
                                    zos.write(j);
                                }
                            } finally {
                                bis.close();
                            }
                        } finally {
                            fis.close();
                            zos.closeEntry();
                        }
                    }
                } finally {
                    zos.close();
                }
            } finally {
                bos.close();
            }
        } finally {
            fos.close();
        }
    }

    public static String getExtension(File f) {
        String ext = "";
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if ((i > 0) && (i < (s.length() - 1))) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public static File addExtensionIfNone(File file, String extension) {
        if (getExtension(file).length() > 0) {
            return file;
        }
        String path = file.getAbsolutePath();
        if (!path.endsWith(".")) {
            path += ".";
        }
        path += extension;
        return new File(path);
    }
}
