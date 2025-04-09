package jblip.gui.components.view;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import jblip.gui.data.FormattedUpdate;
import jblip.util.UpdateTag;

public class BrowserLaunchHyperlinkListener implements HyperlinkListener {

    private final Desktop desktop;

    private final FormattedUpdate update;

    public BrowserLaunchHyperlinkListener(final FormattedUpdate formatted_update) {
        Desktop d = null;
        if (Desktop.isDesktopSupported()) {
            d = Desktop.getDesktop();
            if (!d.isSupported(Desktop.Action.BROWSE)) {
                d = null;
            }
        }
        desktop = d;
        update = formatted_update;
    }

    @Override
    public void hyperlinkUpdate(final HyperlinkEvent e) {
        if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
            return;
        }
        final UpdateTag tag = update.getTag(e.getDescription());
        if (!tag.hasURI()) {
            return;
        }
        URI uri = tag.getURI();
        if (uri != null) {
            try {
                desktop.browse(uri);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
