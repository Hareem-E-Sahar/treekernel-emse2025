package se.kth.speech.skatta.player;

import java.io.*;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Fileresultwriter provides a simple database of results which are to be
 * written to file
 *
 * @author jonas, John
 */
public class FileResultWriter {

    private File myFile;

    private String myNameInZip;

    private String[] myResultClasses;

    private LinkedList<String[]>[] myResults;

    /**
     * Creates a list where result lines can be added.
     *
     * @param childPath     The path where files will be written. Filenames will be in the
     *                      specified formats so only provide a directory path.
     * @param resultClasses The header for each ResponsDevice
     * @param numberOfRows  the maximum number of rows that can be added to the file,
     *                      not including the resultclasses.
     */
    @SuppressWarnings("unchecked")
    public FileResultWriter(File file, String[] resultClasses, int numberOfRows) {
        String path = file.getPath();
        if (path.contains(".zip/")) {
            int split = path.indexOf(".zip/") + 4;
            myNameInZip = path.substring(split + 1);
            path = path.substring(0, split);
            myFile = new File(path);
        } else myFile = file;
        myResultClasses = resultClasses;
        myResults = (LinkedList<String[]>[]) new LinkedList[numberOfRows];
    }

    /**
     * Add one line of results to be written Caller: ResponsDeviceList,
     * RecorderThread
     *
     * @param results    Array of Strings in the same order as the headers of the file
     * @param pageNumber The number of the page that generated this data.
     */
    public void addResults(String[] results, int pageNumber) {
        if (pageNumber < myResults.length && pageNumber >= 0) {
            if (myResults[pageNumber] == null) myResults[pageNumber] = new LinkedList<String[]>();
            myResults[pageNumber].add(results);
        }
    }

    public void setResults(String[] results, int pageNumber) {
        deleteResults(pageNumber);
        addResults(results, pageNumber);
    }

    /**
     * Writes all current data to disk Caller: Test
     *
     * @return True if it could write sucessfully False if it could not write
     *         (no write access or similar)
     */
    public boolean write() {
        try {
            if (myFile.getParentFile() != null) myFile.getParentFile().mkdirs();
            BufferedWriter writer;
            if (isZip()) {
                ZipOutputStream out = null;
                if (myFile.exists()) {
                    List<byte[]> entries = new LinkedList<byte[]>();
                    List<ZipEntry> headers = new LinkedList<ZipEntry>();
                    ZipFile zf = new ZipFile(myFile);
                    Enumeration<? extends ZipEntry> enumeration = zf.entries();
                    while (enumeration.hasMoreElements()) {
                        ZipEntry ze = enumeration.nextElement();
                        byte[] data = new byte[(int) ze.getSize()];
                        zf.getInputStream(ze).read(data, 0, data.length);
                        headers.add(ze);
                        entries.add(data);
                    }
                    out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(myFile)));
                    ListIterator<ZipEntry> header_iter = headers.listIterator();
                    for (byte[] data : entries) {
                        ZipEntry ze = header_iter.next();
                        out.putNextEntry(new ZipEntry(ze.getName()));
                        out.write(data, 0, data.length);
                        out.closeEntry();
                    }
                } else out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(myFile)));
                out.putNextEntry(new ZipEntry(myNameInZip));
                writer = new BufferedWriter(new OutputStreamWriter(out));
            } else writer = new BufferedWriter(new FileWriter(myFile));
            for (String resultClass : myResultClasses) {
                if (resultClass != null) writer.write(resultClass + ";");
            }
            writer.write(System.getProperty("line.separator"));
            int columns = myResultClasses.length;
            for (LinkedList<String[]> page : myResults) {
                for (String[] row : page) {
                    int column = 0;
                    if (row != null) {
                        for (String result : row) {
                            if (result != null) writer.write(result + ";");
                            if (++column >= columns) {
                                writer.write(System.getProperty("line.separator"));
                                column = 0;
                            }
                        }
                    }
                }
            }
            writer.close();
        } catch (IOException exc) {
            System.err.println(exc);
            return false;
        }
        return true;
    }

    /**
     * Gets the result row from the database of lines to be written. Caller:
     * ResponsDeviceList, RecorderThread
     *
     * @param pageNumber The number of the page the results from which are to be
     *                   retrieved.
     */
    public String[] getLastResults(int pageNumber) {
        if (pageNumber < myResults.length && pageNumber >= 0 && myResults[pageNumber] != null) return myResults[pageNumber].getLast(); else return null;
    }

    /**
     * Deletes all results associated with a certain page.
     *
     * @param pageNumber The number of the page the results of which are to be deleted.
     */
    public void deleteResults(int pageNumber) {
        myResults[pageNumber] = null;
    }

    /**
     * Sets the savepath to be used by the writer. Used by Test.actionPerformed
     * when changing from initial page.
     *
     * @param path The new path.
     */
    public void setPath(String path) {
        myFile = new File(path);
    }

    public void setName(String name) {
        if (isZip()) myNameInZip = name; else myFile = new File(myFile.getParentFile(), name);
    }

    public String getPath() {
        return myFile.getAbsolutePath();
    }

    private boolean isZip() {
        return myFile.getName().endsWith(".zip");
    }
}
