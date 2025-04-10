package com.tivo.kmttg.gui;

import java.awt.Component;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import com.tivo.kmttg.main.config;
import com.tivo.kmttg.util.debug;

public class help {

    private static JDialog dialog = null;

    private static JPanel content = null;

    private static JEditorPane pane = null;

    static void showHelp() {
        debug.print("");
        if (dialog == null) {
            dialog = new JDialog(config.gui.getJFrame(), "About kmttg");
            content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            pane = new JEditorPane();
            pane.setContentType("text/html");
            String version = getVersion();
            String text = "<html>";
            text += "<h2 style=\"text-align: center;\">" + config.kmttg + "</h2>";
            if (version != null) {
                text += "<h3 style=\"text-align: center;\">Latest version: <a href=\"http://kmttg.googlecode.com/files/kmttg_";
                text += version + ".zip\">" + version + "</a></h3>";
            }
            text += "<p>LINKS:</p>";
            text += "<table>";
            text += "<tr><td><a href=\"http://code.google.com/p/kmttg/\">kmttg Home Page</a></td>";
            text += "<td><a href=\"http://code.google.com/p/kmttg/downloads/list\">kmttg downloads</a></td></tr>";
            text += "<tr><td><a href=\"http://code.google.com/p/kmttg/wiki/release_notes\">Release Notes</a></td>";
            text += "<td><a href=\"http://code.google.com/p/kmttg/wiki/configuring_kmttg\">kmttg configuration</a></td></tr>";
            text += "<tr><td><a href=\"http://code.google.com/p/kmttg/wiki/using_kmttg\">kmttg operation</a></td>";
            text += "<td><a href=\"http://code.google.com/p/kmttg/wiki/auto_transfers\">Setting up Auto Transfers</a></td></tr>";
            text += "<tr><td><a href=\"http://code.google.com/p/kmttg/wiki/windows_installation\">Windows Installation</a></td>";
            text += "<td><a href=\"http://code.google.com/p/kmttg/wiki/mac_osx_installation\">Mac OSX Installation</a></td></tr>";
            text += "<tr><a href=\"http://code.google.com/p/kmttg/wiki/linux_installation\">Linux Installation</a></tr>";
            text += "</table></html>";
            pane.setText(text);
            pane.setEditable(false);
            class Hyper implements HyperlinkListener {

                public void hyperlinkUpdate(HyperlinkEvent e) {
                    debug.print("e=" + e);
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        showInBrowser(e.getURL().toString());
                    }
                }
            }
            pane.addHyperlinkListener(new Hyper());
            pane.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(pane);
            JButton ok = new JButton("OK");
            ok.setMargin(new Insets(1, 100, 1, 100));
            ok.setAlignmentX(Component.CENTER_ALIGNMENT);
            ok.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    dialog.setVisible(false);
                }
            });
            content.add(ok);
            dialog.getContentPane().add(content);
            dialog.setLocationRelativeTo(config.gui.getJFrame().getJMenuBar().getComponent(0));
            dialog.pack();
        }
        dialog.setVisible(true);
    }

    private static String getVersion() {
        debug.print("");
        String version = null;
        String version_url = "http://kmttg.googlecode.com/svn/trunk/version";
        try {
            URL url = new URL(version_url);
            URLConnection con = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) version = inputLine;
            in.close();
        } catch (Exception ex) {
            version = null;
        }
        return version;
    }

    private static boolean showInBrowser(String url) {
        debug.print("url=" + url);
        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();
        try {
            if (os.indexOf("win") >= 0) {
                String[] cmd = new String[4];
                cmd[0] = "cmd.exe";
                cmd[1] = "/C";
                cmd[2] = "start";
                cmd[3] = url;
                rt.exec(cmd);
            } else if (os.indexOf("mac") >= 0) {
                rt.exec("open " + url);
            } else {
                String[] browsers = { "epiphany", "firefox", "mozilla", "opera", "konqueror", "netscape", "links", "lynx" };
                StringBuffer cmd = new StringBuffer();
                for (int i = 0; i < browsers.length; i++) cmd.append((i == 0 ? "" : " || ") + browsers[i] + " \"" + url + "\" ");
                rt.exec(new String[] { "sh", "-c", cmd.toString() });
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
