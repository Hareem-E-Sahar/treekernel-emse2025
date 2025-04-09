package org.sourceforge.kga.simpleGui.actions;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.prefs.*;
import javax.swing.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.sourceforge.kga.*;
import org.sourceforge.kga.simpleGui.*;
import org.sourceforge.kga.translation.*;

public class CheckForUpdate extends KgaAction implements ItemListener {

    private static java.util.logging.Logger log = java.util.logging.Logger.getLogger(Garden.class.getName());

    static String rssFeed = "http://sourceforge.net/api/file/index/project-id/290359/mtime/desc/limit/20/rss";

    public CheckForUpdate(Gui gui) {
        super(gui, "checkforupdate");
    }

    public static void GetLatestVersion(StringBuffer title, StringBuffer link) throws Exception {
        log.info("Get latest version from " + rssFeed);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document document = documentBuilder.parse(rssFeed);
        Element root = document.getDocumentElement();
        NodeList channels = root.getElementsByTagName("channel");
        if (channels.getLength() != 1) throw new Exception("Can not parse RSS document from sourceforge: channel");
        Element channel = (Element) channels.item(0);
        NodeList items = channel.getElementsByTagName("item");
        if (items.getLength() == 0) throw new Exception("Can not parse RSS document from sourceforge: item");
        Element item = (Element) items.item(0);
        NodeList titles = item.getElementsByTagName("title");
        if (titles.getLength() != 1) throw new Exception("Can not parse RSS document from sourceforge: title");
        Element titleNode = (Element) titles.item(0);
        NodeList links = item.getElementsByTagName("link");
        if (links.getLength() != 1) throw new Exception("Can not parse RSS document from sourceforge: link");
        Element linkNode = (Element) links.item(0);
        Preferences prefs = Preferences.userRoot().node("/org/sourceforge/kga/checkforupdate");
        Date now = new Date();
        prefs.putLong("time", now.getTime());
        log.info("Set last check time " + now.toString());
        title.replace(0, title.length(), ((CDATASection) titleNode.getFirstChild()).getData());
        link.replace(0, title.length(), ((Text) linkNode.getFirstChild()).getNodeValue());
    }

    public static boolean AutomaticallyCheck() {
        Preferences prefs = Preferences.userRoot().node("/org/sourceforge/kga/checkforupdate");
        if (!prefs.getBoolean("automatically", true)) {
            log.info("Automatically check for new version is disabled by user");
            return false;
        }
        Date now = new Date();
        Date time = new Date(prefs.getLong("time", 0));
        log.info("Last check time " + time.toString());
        if (time.getTime() + 30L * 24 * 3600 * 1000 > now.getTime()) {
            log.info("Don't check earlier than 30 days");
            return false;
        }
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Translation t = Translation.getPreferred();
        StringBuilder version = new StringBuilder();
        version.append("<html><h1>Kitchen garden aid</h1><br/>");
        JButton download = null;
        try {
            StringBuffer title = new StringBuffer(), link = new StringBuffer();
            GetLatestVersion(title, link);
            final java.net.URI uri = new java.net.URI(link.toString());
            if (title.indexOf(KitchenGardenAid.VERSION) == -1) {
                version.append("<b>" + t.translate("newversionavailable") + ":</b>" + title);
                download = new JButton(t.translate("gotodownload"));
                download.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (Desktop.isDesktopSupported()) {
                            Desktop desktop = Desktop.getDesktop();
                            try {
                                desktop.browse(uri);
                            } catch (Exception ex) {
                            }
                        }
                    }
                });
            } else version.append("<b>" + t.translate("nonewversionavailable") + ":</b>" + title);
        } catch (Exception ex) {
            version.append(ex.toString());
        }
        JLabel label = new JLabel(version.toString());
        label.setFont(new Font(label.getFont().getName(), Font.PLAIN, label.getFont().getSize()));
        Preferences prefs = Preferences.userRoot().node("/org/sourceforge/kga/checkforupdate");
        JCheckBox checkAuto = new JCheckBox(t.translate("automaticallycheck"), prefs.getBoolean("automatically", true));
        checkAuto.addItemListener(this);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));
        panel.add(label);
        if (download != null) panel.add(download);
        panel.add(checkAuto);
        JDialog tutorial = new JOptionPane(panel).createDialog(t.translate("checkforupdate"));
        tutorial.setVisible(true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Preferences prefs = Preferences.userRoot().node("/org/sourceforge/kga/checkforupdate");
        prefs.putBoolean("automatically", e.getStateChange() == ItemEvent.SELECTED);
    }
}
