package cz.cuni.mff.ksi.jinfer.base.objects;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JLabel;
import org.openide.util.Exceptions;

/**
 * JLabel which behaves like clickable hyperlink. The link is opened in default
 * system browser.
 *
 * @author sviro
 */
public class JHyperlinkLabel extends JLabel {

    private static final long serialVersionUID = 35898975;

    private final String uri;

    /**
   * Default constructor. Parameter is uri where the label is pointing to.
   * @param uri URI where this labels' link is pointing to.
   */
    public JHyperlinkLabel(final String uri) {
        super();
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new HyperlinkLabelMouseAdapter());
        this.uri = uri;
    }

    private class HyperlinkLabelMouseAdapter extends MouseAdapter {

        @Override
        public void mouseClicked(final MouseEvent e) {
            if (Desktop.isDesktopSupported()) {
                final Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(new URI(uri));
                } catch (final IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (final URISyntaxException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
