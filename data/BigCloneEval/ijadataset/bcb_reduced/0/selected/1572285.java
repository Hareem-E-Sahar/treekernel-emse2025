package aurora.util;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;

/**
 * Implementation of 
 * @author Alex Kurzhanskiy
 * @version $Id: HyperlinkLabel.java 38 2010-02-08 22:59:00Z akurzhan $
 */
public class HyperlinkLabel extends JLabel implements MouseListener {

    private static final long serialVersionUID = 5167616594614061634L;

    private URL url = null;

    public HyperlinkLabel(String label) {
        super(label);
        addMouseListener(this);
    }

    public HyperlinkLabel(String label, URL url) {
        this(label);
        this.url = url;
        setText("<html><a href=\"\">" + label + "</a></html>");
        setToolTipText("Go to: " + url.getRef());
    }

    public HyperlinkLabel(String label, String tip, URL url) {
        this(label, url);
        setToolTipText(tip);
    }

    public void setURL(URL url) {
        this.url = url;
    }

    public URL getURL() {
        return url;
    }

    public void mouseClicked(MouseEvent e) {
        HyperlinkLabel self = (HyperlinkLabel) e.getSource();
        if (self.url == null) return;
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) try {
                desktop.browse(url.toURI());
                return;
            } catch (Exception exp) {
            }
        }
        JOptionPane.showMessageDialog(this, "Cannot launch browser...\n Please, visit\n" + url.getRef(), "", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    public void mouseEntered(MouseEvent e) {
        e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return;
    }

    public void mouseExited(MouseEvent e) {
        return;
    }

    public void mousePressed(MouseEvent e) {
        return;
    }

    public void mouseReleased(MouseEvent e) {
        return;
    }
}
