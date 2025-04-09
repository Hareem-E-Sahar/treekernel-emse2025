package com.sobek.web.shop.listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.perf4j.log4j.AsyncCoalescingStatisticsAppender;

/**
 *
 * @author Alexandra Sobek
 */
public class DemoDBCreationListener implements ServletContextListener {

    private static final Log log = LogFactory.getLog(DemoDBCreationListener.class);

    public void contextInitialized(ServletContextEvent context) {
        File shopDestinationFile = new File(System.getProperty("user.home") + "/.shopdb");
        if (!shopDestinationFile.exists()) {
            try {
                String realPath = context.getServletContext().getRealPath("WEB-INF/.shopdb");
                File demoFile = new File(realPath);
                copyDirectory(demoFile, shopDestinationFile);
                log.info("demo db initialized");
            } catch (IOException ex) {
                log.error(ex);
            }
        }
    }

    public void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        } else {
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    public void contextDestroyed(ServletContextEvent context) {
    }
}
