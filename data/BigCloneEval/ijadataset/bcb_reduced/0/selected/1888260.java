package com.somoconsulting.cbsc.urllauncher;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.log4j.Logger;

/**
 * cBSC - collaborative balanced scorecard � a web based tool for collaboration on strategy development 
 * Copyright (C) 2009 SOMO Consulting GmbH   <http://www.somo-consulting.com/>
 * 
 * This program comes with ABSOLUTELY NO WARRANTY; it is distributed under GNU General Public License. 
 * 
 */
public class URLLauncherDesktop implements IURLLauncher {

    private static final Logger LOG = Logger.getLogger(URLLauncherDesktop.class);

    /**
     * Java 6 standard (Desktop) launch.
     */
    @Override
    public void launchURL(final String pURL, final String pTarget) {
        LOG.debug("Launching URL with Java 6 \"" + pURL + "\", target \"" + pTarget + "\"");
        if (Desktop.isDesktopSupported()) {
            try {
                final URI lUri = new URI(pURL);
                Desktop.getDesktop().browse(lUri);
            } catch (final URISyntaxException e) {
                LOG.error("Invalid URL", e);
            } catch (final IOException e) {
                LOG.error("Error!", e);
            }
        } else {
            LOG.error("Desktop not supported");
        }
    }
}
