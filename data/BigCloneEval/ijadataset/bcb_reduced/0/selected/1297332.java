package alx.library.actions;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;

public class WebsiteAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger(WebsiteAction.class.getName());

    public WebsiteAction() {
        super("Website");
    }

    public void actionPerformed(ActionEvent evt) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(new URI("http://alx-library.sourceforge.net/"));
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Error", e);
        }
    }
}
