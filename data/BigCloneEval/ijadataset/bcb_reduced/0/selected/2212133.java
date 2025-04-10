package org.compiere.apps;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import org.adempiere.plaf.AdempierePLAF;
import org.compiere.util.Env;

/**
 *  Online Help Browser & Link.
 *
 *  @author     Jorg Janke
 *  @version    $Id: OnlineHelp.java,v 1.2 2006/07/30 00:51:27 jjanke Exp $
 *  
 *  globalqss: fix error about null pointer in OnlineHelp.Worker.run
 *             change the URL for online help for connection
 */
public class OnlineHelp extends JEditorPane implements HyperlinkListener {

    /**
	 *  Default Constructor
	 */
    public OnlineHelp() {
        super();
        setEditable(false);
        setContentType("text/html");
        addHyperlinkListener(this);
    }

    /**
	 *  Constructor
	 *  @param url URL to load
	 */
    public OnlineHelp(String url) {
        this();
        try {
            if (url != null && url.length() > 0) setPage(url);
        } catch (Exception e) {
            System.err.println("OnlineHelp URL=" + url + " - " + e);
        }
    }

    /**
	 *  Constructor
	 *  @param loadOnline load online URL
	 */
    public OnlineHelp(boolean loadOnline) {
        this(loadOnline ? BASE_URL : null);
    }

    /** Base of Online Help System      */
    protected static final String BASE_URL = "http://www.adempiere.com/wiki/index.php/OnlineLoginHelp";

    public static void openInDefaultBrowser() {
        Env.startBrowser(BASE_URL);
    }

    /**************************************************************************
	 *	Hyperlink Listener
	 *  @param e event
	 */
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (e instanceof HTMLFrameHyperlinkEvent) {
            HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
            HTMLDocument doc = (HTMLDocument) getDocument();
            doc.processHTMLFrameHyperlinkEvent(evt);
        } else if (e.getURL() == null) scrollToReference(e.getDescription().substring(1)); else {
            try {
                setPage(e.getURL());
            } catch (Throwable t) {
                System.err.println("Help.hyperlinkUpdate - " + t.toString());
                displayError("Error", e.getURL(), t);
            }
        }
        this.setCursor(Cursor.getDefaultCursor());
    }

    /**
	 *  Set Text
	 *  @param text text
	 */
    public void setText(String text) {
        setBackground(AdempierePLAF.getInfoBackground());
        super.setText(text);
        setCaretPosition(0);
    }

    /**
	 *  Load URL async
	 *  @param url url
	 */
    public void setPage(final URL url) {
        setBackground(Color.white);
        Runnable pgm = new Runnable() {

            public void run() {
                loadPage(url);
            }
        };
        new Thread(pgm).start();
    }

    /**
	 *  Load Page Async
	 *  @param url url
	 */
    private void loadPage(URL url) {
        try {
            super.setPage(url);
        } catch (Exception e) {
            displayError("Error: URL not found", url, e);
        }
    }

    /**
	 *  Display Error message
	 *  @param header header
	 *  @param url url
	 *  @param exception exception
	 */
    protected void displayError(String header, Object url, Object exception) {
        StringBuffer msg = new StringBuffer("<HTML><BODY>");
        msg.append("<H1>").append(header).append("</H1>").append("<H3>URL=").append(url).append("</H3>").append("<H3>Error=").append(exception).append("</H3>").append("<p>&copy;&nbsp;Adempiere &nbsp; ").append("<A HREF=\"").append(BASE_URL).append("\">Online Help</A></p>").append("</BODY></HTML>");
        setText(msg.toString());
    }

    /** Online links.
	 *  Key=AD_Window_ID (as String) - Value=URL
	 */
    private static HashMap<String, String> s_links = new HashMap<String, String>();

    static {
        new Worker(BASE_URL, s_links).start();
    }

    /**
	 *  Is Online Help available.
	 *  @return true if available
	 */
    public static boolean isAvailable() {
        return s_links.size() != 0;
    }
}

/**
 *  Online Help Worker
 */
class Worker extends Thread {

    /**
	 *  Worker Constructor
	 *  @param urlString url
	 *  @param links links
	 */
    Worker(String urlString, HashMap<String, String> links) {
        m_urlString = urlString;
        m_links = links;
        setPriority(Thread.MIN_PRIORITY);
    }

    private String m_urlString = null;

    private HashMap<String, String> m_links = null;

    /**
	 *  Worker: Read available Online Help Pages
	 */
    public void run() {
        if (m_links == null) return;
        URL url = null;
        try {
            url = new URL(m_urlString);
        } catch (Exception e) {
            System.err.println("OnlineHelp.Worker.run (url) - " + e);
        }
        if (url == null) return;
        try {
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();
            HTMLEditorKit kit = new HTMLEditorKit();
            HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
            doc.putProperty("IgnoreCharsetDirective", new Boolean(true));
            kit.read(new InputStreamReader(is), doc, 0);
            HTMLDocument.Iterator it = doc.getIterator(HTML.Tag.A);
            Object target = null;
            Object href = null;
            while (it != null && it.isValid()) {
                AttributeSet as = it.getAttributes();
                if (target == null || href == null) {
                    Enumeration en = as.getAttributeNames();
                    while (en.hasMoreElements()) {
                        Object o = en.nextElement();
                        if (target == null && o.toString().equals("target")) target = o; else if (href == null && o.toString().equals("href")) href = o;
                    }
                }
                if (target != null && "Online".equals(as.getAttribute(target))) {
                    String hrefString = (String) as.getAttribute(href);
                    if (hrefString != null) {
                        try {
                            String AD_Window_ID = hrefString.substring(hrefString.indexOf('/', 1), hrefString.lastIndexOf('/'));
                            m_links.put(AD_Window_ID, hrefString);
                        } catch (Exception e) {
                            System.err.println("OnlineHelp.Worker.run (help) - " + e);
                        }
                    }
                }
                it.next();
            }
            is.close();
        } catch (ConnectException e) {
        } catch (UnknownHostException uhe) {
        } catch (Exception e) {
            System.err.println("OnlineHelp.Worker.run (e) " + e);
        } catch (Throwable t) {
            System.err.println("OnlineHelp.Worker.run (t) " + t);
        }
    }

    /**
	 * 	Diagnostics
	 * 	@param doc html document
	 * 	@param tag html tag
	 */
    private void dumpTags(HTMLDocument doc, HTML.Tag tag) {
        System.out.println("Doc=" + doc.getBase() + ", Tag=" + tag);
        HTMLDocument.Iterator it = doc.getIterator(tag);
        while (it != null && it.isValid()) {
            AttributeSet as = it.getAttributes();
            System.out.println("~ " + as);
            it.next();
        }
    }
}
