package mobac.gui.actions;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import mobac.program.Logging;

public class OpenInWebbrowser implements ActionListener, MouseListener {

    URI uri;

    public OpenInWebbrowser(URI uri) {
        super();
        this.uri = uri;
    }

    public OpenInWebbrowser(String uri) throws URISyntaxException {
        super();
        this.uri = new URI(uri);
    }

    public void actionPerformed(ActionEvent event) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(uri);
            } catch (IOException e) {
                Logging.LOG.error("Failed to open web browser", e);
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        actionPerformed(null);
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }
}
