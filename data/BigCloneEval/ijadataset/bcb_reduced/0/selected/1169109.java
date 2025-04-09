package OpenOfficeNow.gui;

import OpenOfficeNow.OpenOfficeNow;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class WinResultsWindow extends JFrame implements HyperlinkListener {

    private WinLoginWindow win;

    private HashMap results;

    private HashMap keys;

    private boolean hasReserved = false;

    private boolean canHaveReserved;

    private JEditorPane resultsText;

    private int urlsCount = 0;

    private String reposPath;

    private OpenOfficeNow extension;

    public WinResultsWindow(OpenOfficeNow extension, HashMap results, HashMap keys, boolean canHaveReserved, String reposPath) {
        this.results = results;
        this.keys = keys;
        this.canHaveReserved = canHaveReserved;
        this.reposPath = reposPath;
        this.extension = extension;
        this.extension.setRunning(true);
        String fontBlack = "<font face='Geneva, Arial, Helvetica, sans-serif'>";
        String fontRed = "<font face='Geneva, Arial, Helvetica, sans-serif' color='red'>";
        String descriptionTable = "<table >";
        String generalText = fontBlack;
        String urlsText = "";
        WinLogoPanel logo = new WinLogoPanel();
        String versionTitle = "<table><tr><td WIDTH=170>&nbsp;</td><td><b>" + fontRed;
        String currentVersion = this.keys.get("VersionNow").toString();
        String newVersion = this.results.get("VersionNow").toString();
        if (newVersion.equals(currentVersion)) {
            versionTitle += "You got the last version!</font></b></td></tr>";
        } else {
            versionTitle += "New version found!</font></b></td></tr>";
        }
        versionTitle += "</font></b></td></tr></table><br>";
        descriptionTable += "<tr><td  align='right' >" + fontBlack + "<b>PublisherNow:</b></font></td><td></td><td>" + fontRed + "<b>" + this.results.get("PublisherNow") + "</b></font></td></tr>";
        descriptionTable += "<tr><td  align='right' >" + fontBlack + "<b>DocumentNow:</b></td>  <td></td><td>" + fontRed + "<b>" + this.results.get("DocumentNow") + "</b></font></td></tr>";
        descriptionTable += "<tr><td  align='right' >" + fontBlack + "<b>VersionNow:</b></td><td></td><td>" + fontRed + "<b>" + this.results.get("VersionNow") + "</b></font></td></tr>";
        descriptionTable += "<tr></tr>";
        descriptionTable += "<tr><td  align='right' >" + fontBlack + "<b>Version title:</b></td><td></td><td>" + fontBlack + this.results.get("versionTitle") + "</td></tr>";
        descriptionTable += "<tr><td  align='right' VALIGN='top'>" + fontBlack + "<b>Version description:</b></td><td></td><td VALIGN='top' WIDTH=340><i>" + fontBlack + this.results.get("versionDescription") + "</i></td></tr>";
        descriptionTable += "<tr><td  align='right' >" + fontBlack + "<b>Version subject:</b></td><td></td><td>" + fontBlack + this.results.get("versionSubject") + "</td></tr>";
        descriptionTable += "<tr><td  align='right' >" + fontBlack + "<b>Custom protocol:</b></td><td></td><td>" + fontBlack + this.results.get("customProtocol") + "</td></tr>";
        descriptionTable += "</table>";
        generalText += versionTitle + descriptionTable;
        Iterator iter = this.results.keySet().iterator();
        HashMap urls = new HashMap();
        while (iter.hasNext()) {
            Object element = iter.next();
            if (element.toString().startsWith("url:")) {
                urls = (HashMap) this.results.get(element);
                String separator = "";
                String link = "";
                if (urls.get("reserved").equals("yes") && canHaveReserved == true) {
                    if (hasReserved == false) {
                        separator = "<b><br>" + fontBlack + "Reserved URLs found!</font></b>";
                        String url = urls.get("url").toString();
                        String login = "<a href='login'>login</a>";
                        String copy = "<a href='copyUrl:" + url + "'>copy</a>";
                        link = "<a href='" + url + "'>this link</a>";
                        urlsText += separator + fontBlack + "&nbsp;&nbsp;Follow " + link + " (" + copy + ") or " + login + " in order to see details</font>";
                        hasReserved = true;
                    }
                } else {
                    urlsCount += 1;
                    final String realUrl = urls.get("url").toString();
                    String url = realUrl;
                    if (realUrl.length() > 50) {
                        url = realUrl.substring(0, 50) + "...";
                    }
                    link = "<a href='" + realUrl + "'>" + url + "</a>";
                    String urlsTable = "<br><table>";
                    String copyUrl = "<a href='copyUrl:" + realUrl + "'>copy link to clipboard</a>";
                    urlsTable += "<tr><td  align='right' VALIGN='top'>" + fontBlack + "<b><u>Url #" + urlsCount + ":</u></b></font></td><td></td><td VALIGN='top' WIDTH=330>" + fontBlack + link + "</font></td></tr>";
                    urlsTable += "<tr><td  align='right' VALIGN='top'>" + fontBlack + "<b>&nbsp;</b></font></td><td></td><td VALIGN='top' WIDTH=330>" + fontBlack + copyUrl + "</font></td></tr>";
                    urlsTable += "<tr><td  align='right' >" + fontBlack + "<b>Language:</b></font></td><td></td><td>" + fontBlack + urls.get("language") + "</font></td></tr>";
                    urlsTable += "<tr><td  align='right' >" + fontBlack + "<b>File name:</b></font></td><td></td><td>" + fontBlack + urls.get("fileName") + "</font></td></tr>";
                    urlsTable += "<tr><td  align='right' >" + fontBlack + "<b>File format:</b></font></td><td></td><td>" + fontBlack + urls.get("fileFormat") + "</font></td></tr>";
                    urlsTable += "<tr><td  align='right' >" + fontBlack + "<b>File size:</b></font></td><td></td><td>" + fontBlack + urls.get("fileSize") + "</font></td></tr>";
                    urlsTable += "</table>";
                    urlsText += separator + urlsTable;
                }
                resultsText = new JEditorPane("text/html", generalText + urlsText + "</font>");
            }
        }
        resultsText.setEditable(false);
        resultsText.setOpaque(false);
        resultsText.addHyperlinkListener(this);
        Insets insets = getInsets();
        add(logo);
        logo.setBounds(100, 10, 332, 95);
        JPanel resultsPanel = new JPanel();
        resultsPanel.add(resultsText);
        JScrollPane scroller = new JScrollPane(resultsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroller.getVerticalScrollBar().setUnitIncrement(16);
        int minHeight = scroller.getPreferredSize().height;
        if (minHeight > 550) {
            minHeight = 550;
        }
        add(scroller);
        scroller.setBounds(5 + insets.left, 120 + insets.top, 555, minHeight);
        setSize(scroller.getWidth() + 20, scroller.getHeight() + 155);
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("images/link.png"));
        setIconImage(image);
        setTitle("OpenOffice.Now - Please visit http://www.whereisnow.com");
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();
        int frameWidth = getWidth();
        int frameHeight = getHeight();
        int xPos = (screenWidth / 2) - (frameWidth / 2);
        int yPos = (screenHeight / 2) - (frameHeight / 2);
        setBounds(xPos, yPos, frameWidth, frameHeight);
        setLayout(null);
        setResizable(false);
        setVisible(true);
        addWindowListener(new WinCloseEvent(this.extension, this));
    }

    public void hyperlinkUpdate(HyperlinkEvent event) {
        URL url = event.getURL();
        if (event.getEventType() == HyperlinkEvent.EventType.ENTERED && !event.getDescription().equals("login") && !event.getDescription().startsWith("copyUrl:")) {
            this.resultsText.setToolTipText(url.toString());
        } else if (event.getEventType() == HyperlinkEvent.EventType.EXITED) {
            this.resultsText.setToolTipText(null);
        } else if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if (event.getDescription().equals("login")) {
                if (this.win == null) {
                    this.win = new WinLoginWindow(this.extension, this, this.keys, this.reposPath);
                } else {
                    this.win.setVisible(true);
                }
            } else if (event.getDescription().startsWith("copyUrl:")) {
                String toCopy = event.getDescription().replaceFirst("copyUrl:", "");
                StringSelection data = new StringSelection(toCopy);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(data, data);
            } else {
                if (!Desktop.isDesktopSupported()) {
                    try {
                        Runtime.getRuntime().exec("firefox " + url.toString());
                    } catch (Exception ex) {
                        return;
                    }
                }
                try {
                    Desktop desktop = Desktop.getDesktop();
                    if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                        return;
                    }
                    URI uri = new URI(url.toString());
                    desktop.browse(uri);
                } catch (Exception e) {
                }
            }
        }
    }
}
