package twjcalc;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 *
 */
public class JumpTo {

    public static final String BASE_URL = "http://twjcalc.sourceforge.net/2.10/";

    public static final String HELP_URL = "UserGuide/help/Help.html";

    public static final String HELP_EDIT_JAVASCRIPT_URL = "UserGuide/help/HelpEditJavaScript.html";

    public static final String HELP_EDIT_SCALEPATTERN_URL = "UserGuide/help/HelpEditScalePatterns.html";

    public static final String HELP_EDIT_DRILLSIZES_URL = "UserGuide/help/HelpEditDrillSet.html";

    public static final String HELP_EDIT_BASEFREQUENCY_URL = "UserGuide/help/HelpEditBaseFrequency.html";

    public static final String USER_GUIDE_URL = "UserGuide/UserGuide.html";

    public static final String VERSION = "Whistle Calculator, version 2.10 (test)";

    public static final void url(final String str) {
        if (java.awt.Desktop.isDesktopSupported()) {
            final java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            java.net.URI uri;
            try {
                final String strUrl = JumpTo.BASE_URL + str;
                uri = new java.net.URI(strUrl);
                desktop.browse(uri);
            } catch (final URISyntaxException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }
}
