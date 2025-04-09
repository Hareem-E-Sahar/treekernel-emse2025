package coopnetclient.frames.listeners;

import coopnetclient.ErrorHandler;
import coopnetclient.Globals;
import coopnetclient.protocol.out.Protocol;
import coopnetclient.threads.ErrThread;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;

public class HyperlinkMouseListener extends MouseAdapter {

    private String lastVisitedURL;

    private long listVisitedTS;

    @Override
    public void mouseMoved(MouseEvent ev) {
        JTextPane editor = (JTextPane) ev.getSource();
        Point pt = new Point(ev.getX(), ev.getY());
        int pos = editor.viewToModel(pt);
        if (pos >= 0) {
            Document doc = editor.getDocument();
            DefaultStyledDocument hdoc = (DefaultStyledDocument) doc;
            Element el = hdoc.getCharacterElement(pos);
            AttributeSet a = el.getAttributes();
            final String href = (String) a.getAttribute(HTML.Attribute.HREF);
            if (href != null) {
                editor.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                editor.setCursor(null);
            }
        }
    }

    /**
     * Called for a mouse click event.
     * If the component is read-only (ie a browser) then 
     * the clicked event is used to drive an attempt to
     * follow the reference specified by a link.
     *
     * @param e the mouse event
     * @see MouseListener#mouseClicked
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        JTextPane editor = (JTextPane) e.getSource();
        Point pt = new Point(e.getX(), e.getY());
        int pos = editor.viewToModel(pt);
        if (pos >= 0) {
            Document doc = editor.getDocument();
            if (pos == doc.getLength()) {
                return;
            }
            DefaultStyledDocument hdoc = (DefaultStyledDocument) doc;
            Element el = hdoc.getCharacterElement(pos);
            AttributeSet a = el.getAttributes();
            final String href = (String) a.getAttribute(HTML.Attribute.HREF);
            if (href != null) {
                new ErrThread() {

                    @Override
                    public void handledRun() throws Throwable {
                        String url = href.trim();
                        Globals.addVisitedURL(url);
                        if (lastVisitedURL != null && lastVisitedURL.equals(url)) {
                            if (listVisitedTS + 1000 > System.currentTimeMillis()) {
                                return;
                            } else {
                                listVisitedTS = System.currentTimeMillis();
                            }
                        } else {
                            lastVisitedURL = url;
                        }
                        openURL(url);
                    }
                }.start();
            }
        }
    }

    public static void openURL(String address) {
        if (address != null && address.startsWith("room://")) {
            int idx = address.lastIndexOf("/");
            Protocol.joinRoomByID(address.substring(idx + 1), "");
            return;
        }
        try {
            Desktop desktop = null;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
            }
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                URI uri = null;
                uri = new URI(address);
                desktop.browse(uri);
            }
        } catch (URISyntaxException use) {
        } catch (FileNotFoundException fne) {
        } catch (java.io.IOException ioe) {
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
