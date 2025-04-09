package mano.view;

import java.awt.Desktop;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 *
 * @author aesnault
 */
public class AboutWindow extends JEditorPane {

    public AboutWindow() {
        this.setContentType("text/html");
        this.setText("<html><h2 text-align:center>Mano v1.05</h2>Author: Antoine Esnault<br />Licence: GNU General Public Licence<br />" + "Home Page: <a href='http://m4no.sourceforge.net/'>http://m4no.sourceforge.net/</a>");
        this.setEditable(false);
        this.setOpaque(false);
        this.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent hle) {
                if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().browse(hle.getURL().toURI());
                        } catch (Exception ex) {
                            Logger.getLogger(Navigator.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                        }
                    }
                }
            }
        });
    }
}
