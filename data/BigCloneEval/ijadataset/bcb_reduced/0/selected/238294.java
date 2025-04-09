package com.vayoodoot.local;

import org.apache.log4j.Logger;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import com.vayoodoot.security.*;
import com.vayoodoot.security.SecurityManager;

/**
 * Created by IntelliJ IDEA.
 * User: Sachin Shetty
 * Date: Sep 17, 2007
 * Time: 8:23:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class DesktopManager {

    private static Logger logger = Logger.getLogger(DesktopManager.class);

    private static boolean isJDIC = true;

    public static void open(File file) {
        try {
            if (Desktop.isDesktopSupported() && (SecurityManager.isFileLaunchable(file.getName()) || file.isDirectory())) {
                logger.info("Launching file: " + file.getAbsolutePath());
                if (isJDIC) {
                    try {
                        org.jdesktop.jdic.desktop.Desktop.open(file);
                        isJDIC = true;
                        logger.info("File Opened Successfully using JDIC: " + file.getAbsolutePath());
                    } catch (Throwable e) {
                        logger.warn("JDIC not available, will try ti use JDK's Desktop: " + e);
                        isJDIC = false;
                    }
                }
                if (!isJDIC) {
                    Desktop.getDesktop().open(file);
                    logger.info("File Opened Successfully using JDK: " + file.getAbsolutePath());
                }
            } else {
                logger.warn("Launching File: " + file.getName() + "is disabled");
            }
        } catch (IOException e) {
            logger.fatal("Err in opening the file: " + e, e);
        }
    }

    public static void main(String args[]) {
        System.setProperty("java.library.path", "C:\\sachin\\work\\shoonya\\svn\\trunk\\fileshare\\lib\\x86");
        open(new File("C:\\SHARE2\\resps\\EquinixInstance1.xls"));
    }
}
