package com.magicpwd._util;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.net.URI;
import java.net.URL;

/**
 *
 * @author Amon
 */
public class Desk {

    public static boolean browse(String url) {
        if (!Desktop.isDesktopSupported()) {
            return false;
        }
        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Action.BROWSE)) {
            return false;
        }
        try {
            desktop.browse(new URI(url));
            return true;
        } catch (Exception exp) {
            Logs.exception(exp);
            return false;
        }
    }

    public static boolean browse(URL url) {
        if (!Desktop.isDesktopSupported()) {
            return false;
        }
        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Action.BROWSE)) {
            return false;
        }
        try {
            desktop.browse(url.toURI());
            return true;
        } catch (Exception exp) {
            Logs.exception(exp);
            return false;
        }
    }

    public static boolean open(java.io.File file) {
        if (!Desktop.isDesktopSupported()) {
            return false;
        }
        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Action.OPEN)) {
            return false;
        }
        try {
            desktop.open(file);
            return true;
        } catch (Exception exp) {
            Logs.exception(exp);
            return false;
        }
    }

    public static boolean mail(String mailto) {
        Logs.log(mailto);
        if (!Desktop.isDesktopSupported()) {
            return false;
        }
        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Action.MAIL)) {
            return false;
        }
        try {
            if (!mailto.toLowerCase().startsWith("mailto:")) {
                mailto = "mailto:" + mailto;
            }
            desktop.mail(new URI(mailto));
            return true;
        } catch (Exception exp) {
            Logs.exception(exp);
            return false;
        }
    }
}
