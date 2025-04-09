package org.sourceforge.jwld;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class LogDownloadServlet extends AbstractLogServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String fileName = req.getParameter("name");
        getServletContext().log("Download file: " + fileName);
        resp.setHeader("Content-disposition", "attachment; filename=" + fileName + ".zip");
        resp.setContentType("application/zip");
        writeFile(fileName, resp.getOutputStream());
    }

    /**
	 * @param fileName
	 * @param outputStream
	 * @throws IOException
	 */
    protected void writeFile(String fileName, OutputStream outputStream) throws IOException {
        File f = new File(getLogRootPath(), fileName);
        FileInputStream is = new FileInputStream(f);
        java.util.zip.ZipOutputStream zipOutputStream = new java.util.zip.ZipOutputStream(outputStream);
        java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(f.getName());
        zipOutputStream.putNextEntry(entry);
        byte buf[] = new byte[10000];
        int len;
        while ((len = is.read(buf)) > 0) {
            zipOutputStream.write(buf, 0, len);
        }
        is.close();
        zipOutputStream.flush();
        zipOutputStream.close();
    }
}
