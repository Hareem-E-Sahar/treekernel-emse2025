package org.commentator.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;

/**
 *
 * @author Trilarion
 */
public class JHyperlinkLabel extends JLabel {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(JHyperlinkLabel.class.getName());

    private final Color linkColor = Color.blue;

    private boolean underline;

    private String fixedLink;

    private final MouseListener mouseListener = new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
            String link = fixedLink;
            if (link == null) {
                link = JHyperlinkLabel.this.getText();
            }
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(URI.create(link));
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            } else {
            }
        }

        @Override
        public void mouseEntered(MouseEvent me) {
            underline = true;
            repaint();
        }

        @Override
        public void mouseExited(MouseEvent me) {
            underline = false;
            repaint();
        }
    };

    /**
     * 
     */
    public JHyperlinkLabel() {
        this("");
    }

    /**
     *
     * @param label
     */
    public JHyperlinkLabel(String label) {
        super(label);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setForeground(linkColor);
        addMouseListener(mouseListener);
    }

    /**
     * 
     * @param link
     */
    public void setLink(String link) {
        fixedLink = link;
    }

    /**
     *
     * @param g
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (underline) {
            g.setColor(getForeground());
            Insets insets = getInsets();
            int left = insets.left;
            if (getIcon() != null) {
                left += getIcon().getIconWidth() + getIconTextGap();
            }
            g.drawLine(left, getHeight() - 1 - insets.bottom, (int) getPreferredSize().getWidth() - insets.right, getHeight() - 1 - insets.bottom);
        }
    }
}
