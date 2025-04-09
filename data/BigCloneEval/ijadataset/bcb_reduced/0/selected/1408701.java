package org.osmius.webapp.action;

import org.osmius.model.OsmUserscripts;
import org.osmius.service.OsmUserscriptsManager;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileViewerController implements Controller {

    private MessageSource messageSource = null;

    private OsmUserscriptsManager osmUserscriptsManager;

    public void setOsmUserscriptsManager(OsmUserscriptsManager osmUserscriptsManager) {
        this.osmUserscriptsManager = osmUserscriptsManager;
    }

    /**
    * Sets the messge source
    *
    * @param messageSource The message source
    */
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final MessageSourceAccessor text = new MessageSourceAccessor(messageSource, request.getLocale());
        ModelAndView model = new ModelAndView("fileViewerView");
        String typInstance = request.getParameter("typInstance");
        String event = request.getParameter("event");
        OsmUserscripts osmUserscripts = osmUserscriptsManager.getOsmUserscriptById(event);
        byte[] binScript = osmUserscripts.getBinScript();
        try {
            long time = new Date().getTime();
            String path = request.getSession().getServletContext().getRealPath("/WEB-INF/work");
            new File(path + File.separator + time).mkdirs();
            FileOutputStream fos = new FileOutputStream(path + File.separator + time + File.separator + event + ".zip");
            fos.write(binScript);
            fos.flush();
            fos.close();
            unzip(new File(path + File.separator + time + File.separator + event + ".zip"));
            BufferedReader in = new BufferedReader(new FileReader(path + File.separator + time + File.separator + "user/scripts/" + event));
            String str;
            StringBuffer strBuff = new StringBuffer();
            while ((str = in.readLine()) != null) {
                strBuff.append(str).append(System.getProperty("line.separator"));
            }
            in.close();
            deleteDir(new File(path + File.separator + time));
            model.addObject("script", strBuff.toString());
        } catch (FileNotFoundException ex) {
        } catch (IOException ioe) {
        }
        return model;
    }

    private void unzip(File f) throws IOException {
        ZipFile zip;
        zip = new ZipFile(f);
        Enumeration e = zip.entries();
        while (e.hasMoreElements()) {
            ZipEntry zen = (ZipEntry) e.nextElement();
            if (zen.isDirectory()) {
                continue;
            }
            int size = (int) zen.getSize();
            InputStream zis = zip.getInputStream(zen);
            String extractfile = f.getParentFile().getAbsolutePath() + File.separator + zen.getName();
            writeFile(zis, new File(extractfile), size);
            zis.close();
        }
        zip.close();
    }

    private void writeFile(InputStream zis, File file, int size) throws IOException {
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            byte[] byteStream = new byte[(int) size];
            int buf = -1;
            int rb = 0;
            while ((((int) size - rb) > 0)) {
                buf = zis.read(byteStream, rb, (int) size - rb);
                if (buf == -1) {
                    break;
                }
                rb += buf;
            }
            fos.write(byteStream);
        } catch (IOException e) {
            throw new IOException("UNZIP_ERROR");
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    private void deleteDir(File dir) {
        if (dir.isDirectory()) {
            File fileList[] = dir.listFiles();
            for (int index = 0; index < fileList.length; index++) {
                File file = fileList[index];
                deleteDir(file);
            }
        }
        dir.delete();
    }
}
