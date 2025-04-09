package com.umc.gui.components;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import com.umc.gui.content.IUmcTab;
import com.umc.helper.UMCConstants;

public class ChangelogPanel extends JPanel implements IUmcTab {

    private static final long serialVersionUID = -9124932428595947177L;

    public static final String ID = "CHANGELOG";

    private JEditorPane jta;

    private JScrollPane jScrollPane1;

    public ChangelogPanel() {
        initComponents();
    }

    private void initComponents() {
        FormListener formlistener = new FormListener();
        jScrollPane1 = new JScrollPane();
        jta = new JEditorPane("text/html", "");
        jta.addHyperlinkListener(formlistener);
        jScrollPane1.setBorder(null);
        jta.setBorder(null);
        jta.setEditable(false);
        jta.setFocusable(false);
        setLayout(new BorderLayout());
        HTMLEditorKit kit = new HTMLEditorKit();
        HTMLDocument myDoc = (HTMLDocument) (kit.createDefaultDocument());
        StyleSheet myStyle = myDoc.getStyleSheet();
        myStyle.addRule("body {margin:10px;background: #28293B;color:#aaaaaa}");
        myStyle.addRule("b {color:#ffffff;font-size:10px;font-weight:bold}");
        myStyle.addRule("a {color:#ffffff;text-decoration:none;}");
        jta.setDocument(myDoc);
        StringBuffer sb = new StringBuffer();
        sb.append("<html><body>");
        try {
            BufferedReader changelog = new BufferedReader(new FileReader(System.getProperty("user.dir") + UMCConstants.fileSeparator + "Changelog.txt"));
            String line;
            Pattern p = Pattern.compile("\\[(.*) (\\d+)\\].+\\[(.*)\\]", Pattern.CASE_INSENSITIVE);
            String s = null;
            String url = null;
            while ((line = changelog.readLine()) != null) {
                line = line.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
                Matcher matcher = p.matcher(line);
                if (matcher.find()) {
                    s = matcher.group(2);
                    if (matcher.group(1).equals("Bug")) {
                        url = "<a href=\"https://sourceforge.net/tracker/index.php?func=detail&aid=" + s + "&group_id=240304&atid=1112682\">&nbsp;&nbsp;&nbsp;[" + s + "] </a>" + matcher.group(3);
                    } else if (matcher.group(1).equals("Feature")) {
                        url = "<a href=\"http://sourceforge.net/tracker/index.php??func=detail&aid=" + s + "&group_id=240304&atid=1112685\">&nbsp;&nbsp;&nbsp;[" + s + "] </a>" + matcher.group(3);
                    } else {
                        url = line;
                    }
                    sb.append(url + "<br>");
                } else {
                    if (line.startsWith("Changelog")) line = "<b>" + line + "</b>"; else if (line.startsWith("FIXED") || line.startsWith("CHANGED") || line.startsWith("OPTIMIZED") || line.startsWith("ADDED")) line = "<b>" + line + "</b>";
                    sb.append(line + "<br>");
                }
            }
        } catch (Exception exc) {
            jta.setText("---");
        }
        sb.append("</body></html>");
        jta.setText(sb.toString());
        jta.setCaretPosition(0);
        jScrollPane1.setViewportView(jta);
        add(jScrollPane1, BorderLayout.CENTER);
    }

    private class FormListener implements HyperlinkListener {

        FormListener() {
        }

        public void hyperlinkUpdate(HyperlinkEvent evt) {
            if (evt.getSource() == jta) {
                if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(new URI(evt.getURL().toString()));
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(null, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (URISyntaxException e2) {
                        JOptionPane.showMessageDialog(null, e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    @Override
    public boolean tabClosing() {
        return true;
    }
}
