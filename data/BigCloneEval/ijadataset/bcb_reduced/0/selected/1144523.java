package reportingModule;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintHyperlink;
import net.sf.jasperreports.view.JRHyperlinkListener;

/**
 *
 * @author nuwan
 */
public class ReportHyperlinkListner implements JRHyperlinkListener {

    /**
     * HyperLink Listener for the jasper Report
     * @param jrph
     * @throws JRException
     */
    public void gotoHyperlink(JRPrintHyperlink jrph) throws JRException {
        String hyperlink = jrph.getHyperlinkReference();
        if (hyperlink != null) {
            URI uri = null;
            try {
                uri = new URI(hyperlink);
            } catch (URISyntaxException ex) {
            }
            this.open(uri);
        }
    }

    /**
     * Open the url in the browser
     * @param uri
     */
    public static void open(URI uri) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(uri);
            } catch (IOException e) {
            }
        } else {
        }
    }
}
