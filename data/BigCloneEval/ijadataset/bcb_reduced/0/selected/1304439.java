package cz.cuni.mff.ksi.jinfer.welcome;

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
 *
 * @author sviro
 */
public class JHyperlinkLabel extends JLabel {

    private final String uri;

    public JHyperlinkLabel(final String uri) {
        super();
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new HyperlinkLabelMouseAdapter());
        this.uri = uri;
    }

    public class HyperlinkLabelMouseAdapter extends MouseAdapter {

        @Override
        public void mouseClicked(final MouseEvent e) {
            if (Desktop.isDesktopSupported()) {
                final Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(new URI(uri));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (URISyntaxException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
