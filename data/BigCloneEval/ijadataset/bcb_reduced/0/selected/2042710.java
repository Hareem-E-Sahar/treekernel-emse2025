package net.sf.opengroove.client.ui;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import javax.swing.JOptionPane;
import com.jidesoft.swing.JideButton;

/**
 * A button that looks like a hyperlink. When clicked, it will open the user's
 * default browser to the URL specified when creating the link button.
 * 
 * @author Alexander Boyd
 * 
 */
public class WebsiteButton extends JideButton {

    private URI uri;

    public WebsiteButton(String text, URI uri) {
        super(text);
        this.uri = uri;
        this.setButtonStyle(this.HYPERLINK_STYLE);
        this.setAlwaysShowHyperlink(true);
        this.setForeground(Color.BLUE);
        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!Desktop.isDesktopSupported()) {
                    JOptionPane.showMessageDialog(WebsiteButton.this, "OpenGroove can't open the URL \"" + WebsiteButton.this.uri + "\" in your default browser. Try typing " + "in the uri yourself.");
                    return;
                }
                try {
                    Desktop.getDesktop().browse(WebsiteButton.this.uri);
                } catch (IOException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(WebsiteButton.this, "OpenGroove can't open the URL \"" + WebsiteButton.this.uri + "\" in your default browser. Try typing " + "in the uri yourself.");
                    return;
                }
            }
        });
    }
}
