package barrywei.igosyncdocs.bean;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * 
 * 
 * 
 * @author BarryWei
 * @version 1.0, Jul 16, 2010
 * @since JDK1.6
 */
public class JLinkLabel extends JLabel implements MouseListener {

    private static final long serialVersionUID = 7728088768700339578L;

    private String url;

    public JLinkLabel(String text, String url) {
        super(text);
        this.url = url;
        this.addMouseListener(this);
        setForeground(Color.BLUE);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(this.url));
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(this, "Can not open website because " + e1.getMessage(), "iGoSyncDocs", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }
}
