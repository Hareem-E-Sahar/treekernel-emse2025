package org.ladybug.gui.toolbox.launchers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.ladybug.log.LogEngine;

/**
 * @author Aurelian Pop
 */
class HelpLauncher extends AbstractLauncher {

    private static final String HELP_URI = "http://lady-bug.sourceforge.net/help.html";

    @Override
    public void runCode() {
        if (Desktop.isDesktopSupported()) {
            final Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(HELP_URI));
            } catch (final IOException e) {
                LogEngine.error("Could not launch the default browser for your system", e);
            } catch (final URISyntaxException e) {
                LogEngine.error("Invalid URI " + HELP_URI, e);
            }
        } else {
            LogEngine.inform("Unfortunately your system doesn't support Java SE 6 Desktop API", null);
        }
    }
}
