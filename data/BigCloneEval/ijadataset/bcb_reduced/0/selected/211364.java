package net.sf.rmoffice.ui.actions;

import java.awt.Desktop.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opens the browser with the given uri string.
 */
public class DesktopAction implements ActionListener {

    private static final Logger log = LoggerFactory.getLogger(DesktopAction.class);

    private final URI uri;

    private final File file;

    /**
	 * 
	 * @param uri the {@link URI} for browse
	 */
    public DesktopAction(URI uri) {
        this.uri = uri;
        this.file = null;
    }

    /**
	 * 
	 * @param file the {@link File} to open
	 */
    public DesktopAction(File file) {
        this.uri = null;
        this.file = file;
    }

    @Override
    public void actionPerformed(ActionEvent action) {
        doPerform();
    }

    public void doPerform() {
        if (!java.awt.Desktop.isDesktopSupported()) {
            log.error("Desktop is not supported (fatal)");
        } else {
            final java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            Action actionType = Action.BROWSE;
            if (file != null) {
                actionType = Action.OPEN;
            }
            if (!desktop.isSupported(actionType)) {
                log.error("Desktop doesn't support the " + actionType.name() + " action (fatal)");
            } else {
                try {
                    if (uri != null) {
                        desktop.browse(uri);
                    } else if (file != null) {
                        desktop.open(file);
                    }
                } catch (Exception e1) {
                    log.error(e1.getMessage());
                }
            }
        }
    }
}
