package net.esle.sinadura.gui.application;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author zylk.net
 */
public class DesktopHelper {

    private static Logger logger = Logger.getLogger(DesktopHelper.class.getName());

    public static void openDefaultBrowser(String url) {
        Desktop desktop = null;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                URI uri = null;
                try {
                    uri = new URI(url);
                    desktop.browse(uri);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "", e);
                } catch (URISyntaxException e) {
                    logger.log(Level.SEVERE, "", e);
                }
                logger.info(LanguageResource.getLanguage().getString("info.browser.open"));
                LoggingDesktopController.printInfo(MessageFormat.format(LanguageResource.getLanguage().getString("info.browser.open"), url));
            } else {
                logger.severe(LanguageResource.getLanguage().getString("error.no_desktop_support"));
                LoggingDesktopController.printError(LanguageResource.getLanguage().getString("error.no_desktop_support"));
            }
        } else {
            logger.severe(LanguageResource.getLanguage().getString("error.no_desktop_support"));
            LoggingDesktopController.printError(LanguageResource.getLanguage().getString("error.no_desktop_support"));
        }
    }

    public static void openDefaultPDFViewer(String path) {
        if (Desktop.isDesktopSupported()) {
            File file = new File(path);
            if (file.exists()) {
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, LanguageResource.getLanguage().getString("error.no_pdf_viewer_installed"), e);
                    LoggingDesktopController.printError(LanguageResource.getLanguage().getString("error.no_pdf_viewer_installed"));
                }
            } else {
                logger.severe(LanguageResource.getLanguage().getString("error.no_file.exists"));
                LoggingDesktopController.printError(LanguageResource.getLanguage().getString("error.no_file.exists"));
            }
        } else {
            logger.severe(LanguageResource.getLanguage().getString("error.no_desktop_support"));
            LoggingDesktopController.printError(LanguageResource.getLanguage().getString("error.no_desktop_support"));
        }
    }

    public static void openDefaultMailClient(List<String> attachmentList) {
        Desktop desktop = null;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.MAIL)) {
                URI uri = null;
                String attachmentString = "";
                for (String attachment : attachmentList) {
                    attachmentString += "&attachment=" + attachment;
                }
                try {
                    uri = new URI("mailto", "mailto:?SUBJECT=" + attachmentString, null);
                    desktop.mail(uri);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "", e);
                } catch (URISyntaxException e) {
                    logger.log(Level.SEVERE, "", e);
                }
                logger.info(LanguageResource.getLanguage().getString("info.email_client.open"));
                LoggingDesktopController.printInfo(LanguageResource.getLanguage().getString("info.email_client.open"));
            } else {
                logger.severe(LanguageResource.getLanguage().getString("error.no_desktop_support"));
                LoggingDesktopController.printError(LanguageResource.getLanguage().getString("error.no_desktop_support"));
            }
        } else {
            logger.severe(LanguageResource.getLanguage().getString("error.no_desktop_support"));
            LoggingDesktopController.printError(LanguageResource.getLanguage().getString("error.no_desktop_support"));
        }
    }
}
