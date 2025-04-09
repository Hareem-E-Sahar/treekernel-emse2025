package mediathek.beobachter;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

public class BeobWeb implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Desktop.isDesktopSupported()) {
            Desktop d = Desktop.getDesktop();
            try {
                if (d.isSupported(Desktop.Action.BROWSE)) {
                    d.browse(new URI("http://zdfmediathk.sourceforge.net/"));
                }
            } catch (Exception ex) {
            }
        }
    }
}
