package de.psychomatic.mp3db.gui.utils;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import de.psychomatic.applicationtools.Version;

public class Updater {

    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(Updater.class);

    public static void checkUpdate(final JFrame parent, final Version currentVersion) {
        try {
            final Version srvVersion = UpdateUtils.getAvailableRelease();
            if (srvVersion != null) {
                if (-1 == currentVersion.compareTo(srvVersion)) {
                    final StringBuilder version = new StringBuilder(GuiStrings.getInstance().getString("message.checkupdate.found.1"));
                    version.append(srvVersion);
                    version.append("\n");
                    if (Desktop.isDesktopSupported()) {
                        version.append(GuiStrings.getInstance().getString("message.checkupdate.found.2"));
                        final int answer = JOptionPane.showConfirmDialog(parent, version.toString(), GuiStrings.getInstance().getString("title.checkupdate"), JOptionPane.YES_NO_OPTION);
                        if (answer == JOptionPane.YES_OPTION) {
                            try {
                                Desktop.getDesktop().browse(new URL("http://mp3db.sourceforge.net").toURI());
                            } catch (final URISyntaxException e1) {
                                LOG.error("checkUpdate(JFrame, Version)", e1);
                            }
                        }
                    } else {
                        version.append(GuiStrings.getInstance().getString("message.checkupdate.found.3"));
                        version.append("\n");
                        version.append("http://mp3db.sourceforge.net");
                        JOptionPane.showMessageDialog(parent, version.toString(), GuiStrings.getInstance().getString("title.checkupdate"), JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(parent, GuiStrings.getInstance().getString("message.checkupdate.notfound"), GuiStrings.getInstance().getString("title.checkupdate"), JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(parent, GuiStrings.getInstance().getString("message.checkupdate.notfound"), GuiStrings.getInstance().getString("title.checkupdate"), JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (final IOException e1) {
            LOG.error("checkUpdate(JFrame, Version)", e1);
            JOptionPane.showMessageDialog(parent, GuiStrings.getInstance().getString("message.checkupdate.error"), GuiStrings.getInstance().getString("title.checkupdate"), JOptionPane.ERROR_MESSAGE);
        } catch (final ParserConfigurationException e1) {
            LOG.error("checkUpdate(JFrame, Version)", e1);
        } catch (final SAXException e1) {
            LOG.error("checkUpdate(JFrame, Version)", e1);
        }
    }
}
