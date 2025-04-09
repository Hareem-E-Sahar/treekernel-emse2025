package traviaut.gui;

import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JLabel;

public class JLabelURL extends JLabel {

    private final String url;

    public JLabelURL(String u, String text, String color) {
        url = u;
        if (text == null) text = u;
        String col = "";
        if (color != null) {
            col = "color=#\"" + color + "\"";
        }
        String html = "<html><a " + col + " href=\"" + u + "\">" + text + "</a></html>";
        setText(html);
        addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                openURL(url);
            }
        });
    }

    public static void openURL(String url) {
        try {
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(new URI(url));
        } catch (IOException ex) {
        } catch (URISyntaxException ex) {
        } catch (NoClassDefFoundError ncfe) {
        }
    }
}
