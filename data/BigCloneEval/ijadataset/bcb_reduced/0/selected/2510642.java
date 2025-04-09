package desknow.ui.stuff;

import desknow.resources.ConfigManager;
import desknow.resources.pdf.PdfDocument;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class ResultsWindow extends JFrame implements HyperlinkListener {

    private LoginWindow loginWindow;

    private HashMap results;

    private HashMap keys;

    private PdfDocument pdfDocument;

    private boolean hasReserved = false;

    private boolean canHaveReserved;

    private JEditorPane versionEditorPane;

    private JEditorPane urlsEditorPane;

    private JEditorPane registerEditorPane;

    private int urlsCount = 0;

    private ConfigManager configManager;

    private MenuBar menuBar;

    public ResultsWindow(HashMap winResults, HashMap winKeys, boolean canHaveReserved, ConfigManager configMgr, PdfDocument winFile) {
        this.results = winResults;
        this.keys = winKeys;
        this.canHaveReserved = canHaveReserved;
        this.configManager = configMgr;
        this.pdfDocument = winFile;
        menuBar = new MenuBar(configManager, pdfDocument, this);
        setJMenuBar(menuBar);
        String fontBlack = "<font face='Geneva, Arial, Helvetica, sans-serif'>";
        String fontRed = "<font face='Geneva, Arial, Helvetica, sans-serif' color='red'>";
        String keyHtmlTable = "<table>";
        String versionHtmlTable = "";
        String generalText = fontBlack;
        String urlsHtmlList = "";
        LogoPanel logo = new LogoPanel();
        Insets insets = getInsets();
        add(logo);
        logo.setBounds(150, 0, 300, 80);
        String newOrLast = "<table><tr>";
        String currentVersion = keys.get("VersionNow").toString();
        String newVersion = results.get("VersionNow").toString();
        if (newVersion.equals(currentVersion)) {
            newOrLast += "<td WIDTH=180>&nbsp;</td><td><b>" + fontRed + "You got the last version!</font></b></td></tr>";
        } else {
            newOrLast += "<td WIDTH=200>&nbsp;</td><td><b>" + fontRed + "New version found!</font></b></td></tr>";
        }
        newOrLast += "</font></b></td></tr></table><br>";
        keyHtmlTable += "<tr><td WIDTH=10></td><td  align='right' >" + fontBlack + "<b>PublisherNow:</b></font></td><td></td><td>" + fontRed + "<b>" + results.get("PublisherNow") + "</b></font></td>" + "<td WIDTH=70></td><td  align='right' >" + fontBlack + "<b>DocumentNow:</b></td>  <td></td><td>" + fontRed + "<b>" + results.get("DocumentNow") + "</b></font></td>" + "<td WIDTH=70></td><td  align='right' >" + fontBlack + "<b>VersionNow:</b></td><td></td><td>" + fontRed + "<b>" + results.get("VersionNow") + "</b></font></td></tr>" + "<tr></tr></table>";
        versionHtmlTable += "<table><tr><td  align='right'VALIGN='top' >" + fontBlack + "<b>Title:</b></td><td></td><td VALIGN='top' WIDTH=450>" + fontBlack + results.get("versionTitle") + "</td></tr>" + "<tr><td  align='right' VALIGN='top'>" + fontBlack + "<b>Description:</b></td><td></td><td VALIGN='top' WIDTH=450><i>" + fontBlack + results.get("versionDescription") + "</i></td></tr></table>" + "<table><tr><td WIDTH=30></td><td  align='right'VALIGN='top'>" + fontBlack + "<b>Subject:</b></td><td></td><td VALIGN='top'  WIDTH=200 >" + fontBlack + results.get("versionSubject") + "</td><td WIDTH=70></td>" + "<td  align='right'VALIGN='top' >" + fontBlack + "<b>Protocol:</b></td><td></td><td VALIGN='top' WIDTH=100>" + fontBlack + results.get("customProtocol") + "</td></tr></table>" + "</table>";
        generalText += newOrLast + keyHtmlTable + versionHtmlTable;
        versionEditorPane = new JEditorPane("text/html", generalText + "</font>");
        versionEditorPane.setEditable(false);
        versionEditorPane.setOpaque(false);
        JPanel versionPanel = new JPanel();
        versionPanel.add(versionEditorPane);
        add(versionPanel);
        int minHeight = versionPanel.getPreferredSize().height;
        if (minHeight > 550) {
            minHeight = 550;
        }
        versionPanel.setBounds(5 + insets.left, 80 + insets.top, 555, minHeight);
        Iterator iter = results.keySet().iterator();
        HashMap urls = new HashMap();
        while (iter.hasNext()) {
            Object element = iter.next();
            if (element.toString().startsWith("url:")) {
                urls = (HashMap) results.get(element);
                String separator = "";
                String link = "";
                if (urls != null) {
                    if (!urls.isEmpty()) {
                        if (urls.get("reserved").equals("true") && canHaveReserved == true) {
                            if (hasReserved == false) {
                                separator = "<b>" + fontBlack + "Reserved URLs found!</font></b>";
                                String url = urls.get("url").toString();
                                String login = "<a href='login'>login</a>";
                                String copy = "<a href='copyUrl:" + url + "'>copy</a>";
                                link = "<a href='" + url + "'>this link</a>";
                                urlsHtmlList += separator + fontBlack + "&nbsp;&nbsp;Follow " + link + " (" + copy + ") or " + login + " in order to see details<br>Make sure you are authorized to see them!</font>";
                                hasReserved = true;
                            }
                        } else {
                            urlsCount += 1;
                            final String realUrl = urls.get("url").toString();
                            String url = realUrl;
                            if (realUrl.length() > 40) {
                                url = realUrl.substring(0, 40) + "...";
                            }
                            link = "<a href='" + realUrl + "'>" + url + "</a>";
                            String urlsHtmlTable = "<table>";
                            String copyUrl = "<a href='copyUrl:" + realUrl + "'>copy to clipboard</a>";
                            urlsHtmlTable += "<tr><td  align='right' VALIGN='top'>" + fontBlack + "<b><u>Url " + urlsCount + ":</u></b></font></td><td></td><td VALIGN='top' WIDTH=330>" + fontBlack + link + "</font></td>" + "<td  align='right' VALIGN='top'>" + fontBlack + "<b>&nbsp;</b></font></td><td></td><td VALIGN='top'>" + fontBlack + copyUrl + "</font></td></tr></table>" + "<table><tr><td  align='right' WIDTH=100 VALIGN='top'>" + fontBlack + "<b>Language:</b></font></td><td></td><td WIDTH=200 VALIGN='top'>" + fontBlack + urls.get("language") + "</font></td>" + "<td  align='right' VALIGN='top'>" + fontBlack + "<b>File name:</b></font></td><td></td><td VALIGN='top' WIDTH=135>" + fontBlack + urls.get("fileName") + "</font></td></tr></table>" + "<table><tr><td  align='right' WIDTH=100 VALIGN='top'>" + fontBlack + "<b>File format:</b></font></td><td></td><td WIDTH=200 VALIGN='top'>" + fontBlack + urls.get("fileFormat") + "</font></td>" + "<td  align='right' VALIGN='top' >" + fontBlack + "<b>File size:</b></font></td><td></td><td align='right' VALIGN='top'>" + fontBlack + urls.get("fileSize") + "</font></td></tr>" + "</table><br>";
                            urlsHtmlList += separator + urlsHtmlTable;
                        }
                    }
                } else {
                    if (hasReserved = false) {
                        separator = "<b>" + fontBlack + "Reserved URLs found!</font></b>";
                        String login = "<a href='login'>login</a>";
                        urlsHtmlList += separator + fontBlack + "&nbsp;&nbsp;Please, " + login + " in order to see details<br>Make sure you are authorized to see them!</font>";
                    }
                }
                urlsEditorPane = new JEditorPane("text/html", urlsHtmlList + "</font>");
            }
        }
        urlsEditorPane.setEditable(false);
        urlsEditorPane.setOpaque(false);
        urlsEditorPane.addHyperlinkListener(this);
        JPanel resultsPanel = new JPanel();
        resultsPanel.add(urlsEditorPane);
        JScrollPane scroller = new JScrollPane(resultsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroller.getVerticalScrollBar().setUnitIncrement(16);
        int minHeight2 = scroller.getPreferredSize().height;
        if (minHeight2 > 250) {
            minHeight2 = 250;
        }
        add(scroller);
        int scrollPos = versionPanel.getBounds().height + 80 + insets.top;
        scroller.setBounds(5 + insets.left, scrollPos, 560, minHeight2);
        String goRegister = "<table><tr><td>&nbsp;</td><td>&nbsp;</td><td>" + fontBlack + "&nbsp; If you have not an account yet, <a href='http://www.whereisnow.com'>" + "join now!</a></font></td></tr></table>";
        registerEditorPane = new JEditorPane("text/html", goRegister);
        registerEditorPane.setEditable(false);
        registerEditorPane.setOpaque(false);
        registerEditorPane.addHyperlinkListener(this);
        JPanel registerPane = new JPanel();
        registerPane.add(registerEditorPane);
        add(registerPane);
        int registerPos = scroller.getHeight() + versionPanel.getHeight() + 73;
        registerPane.setBounds(insets.left - 10, registerPos, 560, 30);
        int winHeight = scroller.getHeight() + versionPanel.getHeight() + 160;
        setSize(scroller.getWidth() + 20, winHeight);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("images/link.png"));
        setIconImage(image);
        setTitle("Desk.Now - Please visit http://www.whereisnow.com");
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int xPos = ((int) screenSize.getWidth() / 2) - (getWidth() / 2);
        int yPos = ((int) screenSize.getHeight() / 2) - (getHeight() / 2);
        setBounds(xPos, yPos, getWidth(), getHeight());
        setLayout(null);
        setResizable(false);
        setVisible(true);
    }

    public void hyperlinkUpdate(HyperlinkEvent event) {
        URL url = event.getURL();
        if (event.getEventType() == HyperlinkEvent.EventType.ENTERED && !event.getDescription().equals("login") && !event.getDescription().startsWith("copyUrl:")) {
            urlsEditorPane.setToolTipText(url.toString());
        } else if (event.getEventType() == HyperlinkEvent.EventType.EXITED) {
            urlsEditorPane.setToolTipText(null);
        } else if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if (event.getDescription().equals("login")) {
                if (loginWindow == null) {
                    loginWindow = new LoginWindow(this, keys, configManager, pdfDocument);
                } else {
                    loginWindow.setVisible(true);
                }
            } else if (event.getDescription().startsWith("copyUrl:")) {
                String toCopy = event.getDescription().replaceFirst("copyUrl:", "");
                StringSelection data = new StringSelection(toCopy);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(data, data);
            } else {
                String browser = configManager.getBrowser();
                if (!Desktop.isDesktopSupported()) {
                    try {
                        if (!browser.equals("")) {
                            Runtime.getRuntime().exec(browser + " " + url.toString());
                        } else {
                            Runtime.getRuntime().exec("firefox " + url.toString());
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Unable to find a web browser, " + "please set up one on settings window", "Web browser error", JOptionPane.WARNING_MESSAGE);
                    }
                }
                try {
                    Desktop desktop = Desktop.getDesktop();
                    URI uri = new URI(url.toString());
                    desktop.browse(uri);
                } catch (Exception e) {
                    return;
                }
            }
        }
    }
}
