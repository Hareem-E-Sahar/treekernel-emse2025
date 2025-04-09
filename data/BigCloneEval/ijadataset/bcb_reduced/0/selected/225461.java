package gcr.mmm2.util;

import gcr.mmm2.rdb.RDBConnection;
import gcr.mmm2.rdb.ResultSetWrapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

/**
 * @author Benjamin
 * 
 */
public class ExportUtils {

    /**
     * Zips up files
     * 
     * @param outFilename
     *            output filename, should end in .zip
     * @param filenames
     *            source file names on disk
     */
    public static void MakeZip(final String outFilename, final String[] filenames) {
        try {
            final OutputStream out = new FileOutputStream(outFilename);
            MakeZip(out, filenames);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param outs
     * @param filenames
     */
    public static void MakeZip(final OutputStream outs, final String[] filenames) {
        final byte[] buf = new byte[2048];
        try {
            final ZipOutputStream out = new ZipOutputStream(outs);
            out.setLevel(ZipOutputStream.STORED);
            for (int i = 0; i < filenames.length; i++) {
                File file = new File(filenames[i]);
                if (file.canRead()) {
                    final FileInputStream in = new FileInputStream(file);
                    try {
                        out.putNextEntry(new ZipEntry(file.getName()));
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        out.closeEntry();
                    } catch (ZipException z) {
                        System.err.println("EXPORT ERROR (Continuing):" + z.toString());
                    }
                    in.close();
                } else {
                    System.out.println("Skipping file during zip:" + filenames[i]);
                }
            }
            out.close();
        } catch (IOException e) {
            System.err.println("EXPORT FATAL ERROR:" + e.toString());
            System.err.println(e);
        }
    }

    /**
     * @param query
     *            SQL query with one column returned
     * @return Array of string results
     */
    public static String[] queryToArray(String query) {
        LinkedList results = new LinkedList();
        ResultSetWrapper rsw = RDBConnection.executeQuery(query);
        try {
            ResultSet rs = rsw.getResultSet();
            while (rs.next()) {
                results.addLast(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            rsw.closeAll();
        }
        return (String[]) results.toArray(new String[0]);
    }
}
