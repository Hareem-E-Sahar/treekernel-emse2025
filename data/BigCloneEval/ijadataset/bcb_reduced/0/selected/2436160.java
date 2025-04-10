package agorum.blender.yadra.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import agorum.commons.zip.ZipUtils;

public class DownloadServlet extends BaseServlet {

    /**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "inline; filename=" + "images.zip");
        try {
            File basePath = new File(YadraWebServer.config.getWorkPath());
            response.setStatus(HttpServletResponse.SC_OK);
            String workPath = request.getParameter("workPath");
            File outputPath = new File(YadraWebServer.config.getWorkPath() + "/" + workPath + "/output");
            File zipPath = new File(YadraWebServer.config.getWorkPath() + "/" + workPath);
            File outFile = new File(zipPath, "images.zip");
            if (outFile.exists()) {
                outFile.delete();
            }
            ZipUtils zipUtils = new ZipUtils();
            zipUtils.compressFiles(outputPath, outFile, null);
            OutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream(outFile);
            int amnt = 1460;
            byte[] buffer = new byte[amnt];
            while (true) {
                int read = fis.read(buffer, 0, amnt);
                if (read >= 0) {
                    os.write(buffer, 0, read);
                } else {
                    break;
                }
            }
            os.flush();
            fis.close();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            e.printStackTrace(System.err);
            ;
        }
    }
}
