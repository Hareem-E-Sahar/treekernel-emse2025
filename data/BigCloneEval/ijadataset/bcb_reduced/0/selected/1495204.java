package net.sf.wgfa.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * Utility to generate a download with optional compression
 *
 * @author tobias
 */
public class CompressedDownload {

    /**
     * Initiates a download.
     * The data may be compressed with zip or gzip
     * @param type The content type of the file (not used with compression)
     * @param zip The zip method to use ("zip","gzip" or "none")
     * @param filename A filename for the output
     * @param savetodisk Make the browser display the output as a download
     */
    public static OutputStream createDownload(HttpServletResponse response, String type, String zip, String filename, boolean savetodisk) throws IOException {
        if (zip == null || zip.equals("none")) {
            response.setContentType(type);
            if (savetodisk) {
                response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            }
            return response.getOutputStream();
        } else {
            response.setHeader("Content-Disposition", "attachment; filename=" + filename + "." + zip);
            if (zip.equals("zip")) {
                response.setContentType("application/zip");
                ZipOutputStream zipout = new ZipOutputStream(response.getOutputStream());
                zipout.putNextEntry(new ZipEntry(filename));
                return zipout;
            } else if (zip.equals("gzip")) {
                return new GZIPOutputStream(response.getOutputStream());
            } else {
                throw new IllegalArgumentException("Invalid zip method");
            }
        }
    }
}
