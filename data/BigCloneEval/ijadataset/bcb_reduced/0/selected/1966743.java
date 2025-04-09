package org.kablink.teaming.samples.remoteapp.web;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ViewTextFileServlet extends HttpServlet {

    private static final int BUFFER_SIZE = 4096;

    private static String filePath;

    private static String charset;

    public void init(ServletConfig config) throws ServletException {
        filePath = config.getInitParameter("filePath");
        charset = config.getInitParameter("charset");
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (filePath != null && !filePath.equals("") && charset != null && !charset.equals("")) {
            String str = readFileAsString();
            res.setContentType("text/html;charset=" + charset);
            res.getWriter().print(str);
        } else {
            res.getWriter().print("We have a problem: ViewTextFileServlet is not configured properly. Correct it in web.xml.");
        }
    }

    private String readFileAsString() throws IOException {
        FileInputStream in = new FileInputStream(filePath);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        return new String(out.toByteArray(), charset);
    }

    private int copy(InputStream in, OutputStream out) throws IOException {
        int byteCount = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
            byteCount += bytesRead;
        }
        out.flush();
        return byteCount;
    }
}
