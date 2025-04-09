package swingextras.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 *
 * @author Joao Leal
 */
public class PrettyIcon extends JComponent implements MouseListener {

    private static final long serialVersionUID = 1L;

    private static RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    private ImageIcon rawImage;

    private BufferedImage bufferedImage;

    private URI uri;

    public PrettyIcon() {
        super();
        addMouseListener(this);
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        if (bufferedImage != null) {
            return new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight());
        } else {
            return new Dimension(16, 16);
        }
    }

    @Override
    public Dimension getMinimumSize() {
        if (isMinimumSizeSet()) {
            return super.getMinimumSize();
        }
        if (bufferedImage != null) {
            return new Dimension(bufferedImage.getWidth(), bufferedImage.getHeight());
        } else {
            return new Dimension(16, 16);
        }
    }

    public void setImage(ImageIcon rawImage) {
        ImageIcon oldImage = this.rawImage;
        this.rawImage = rawImage;
        firePropertyChange("Image", oldImage, rawImage);
        if (rawImage != oldImage) {
            prepareBuffer();
            if ((rawImage == null) || (oldImage == null) || (rawImage.getIconWidth() != oldImage.getIconWidth()) || (rawImage.getIconHeight() != oldImage.getIconHeight())) {
                revalidate();
            }
            repaint();
        }
    }

    public void setTarget(URL url) {
        if (url != null) {
            try {
                setTarget(url.toURI());
            } catch (URISyntaxException ex) {
                Logger.getLogger(PrettyIcon.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        } else {
            setTarget((URL) null);
        }
    }

    public void setTarget(URI uri) {
        URI oldUri = this.uri;
        this.uri = uri;
        firePropertyChange("Target", oldUri, uri);
        if (uri != null) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) ((g == null) ? null : g.create());
        try {
            if (bufferedImage == null) {
                return;
            }
            g2D.setRenderingHints(renderHints);
            g2D.drawImage(bufferedImage, null, 0, 0);
        } finally {
            g2D.dispose();
        }
    }

    private static final Color opac1 = new Color(1.0f, 1.0f, 1.0f, 0.5f);

    private static final Color opac2 = new Color(1.0f, 1.0f, 1.0f, 1.0f);

    private static final AffineTransform reflectTransform = AffineTransform.getScaleInstance(1.0, -1.0);

    private void prepareBuffer() {
        if (rawImage == null) {
            bufferedImage = null;
            return;
        }
        BufferedImage gradient = null;
        int height = rawImage.getIconHeight();
        int width = rawImage.getIconWidth();
        Graphics2D g2D = null;
        bufferedImage = new BufferedImage(width, height << 1, BufferedImage.TYPE_INT_ARGB);
        try {
            gradient = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            g2D = gradient.createGraphics();
            GradientPaint painter = new GradientPaint(0.0f, 0.0f, opac1, 0.0f, height / 2.0f, opac2);
            g2D.setPaint(painter);
            g2D.fill(new Rectangle2D.Double(0, 0, width, height));
            g2D.dispose();
            gradient.flush();
            g2D = bufferedImage.createGraphics();
            g2D.drawImage(rawImage.getImage(), null, null);
            g2D.translate(0, height << 1);
            g2D.drawImage(rawImage.getImage(), reflectTransform, null);
            g2D.translate(0, -(height << 1));
            g2D.setComposite(AlphaComposite.DstOut);
            g2D.drawImage(gradient, null, 0, height);
        } finally {
            if (gradient != null) {
                gradient.flush();
            }
            if (g2D != null) {
                g2D.dispose();
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (uri != null && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(uri);
            } catch (IOException ex) {
                Logger.getLogger(PrettyIcon.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
