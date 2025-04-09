package org.dyn4j.sandbox.dialogs;

import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Window;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.dyn4j.Version;
import org.dyn4j.sandbox.Sandbox;
import org.dyn4j.sandbox.icons.Icons;
import org.dyn4j.sandbox.resources.Messages;

/**
 * Dialog showing the about information.
 * @author William Bittle
 * @version 1.0.1
 * @since 1.0.0
 */
public class AboutDialog extends JDialog {

    /** The version id */
    private static final long serialVersionUID = -5188464720880815365L;

    /**
	 * Full constructor.
	 * @param owner the dialog owner
	 */
    private AboutDialog(Window owner) {
        super(owner, Messages.getString("dialog.about.title"), ModalityType.APPLICATION_MODAL);
        this.setIconImage(Icons.ABOUT.getImage());
        this.setPreferredSize(new Dimension(450, 500));
        Container container = this.getContentPane();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        JLabel icon = new JLabel();
        icon.setIcon(Icons.SANDBOX_128);
        icon.setText(MessageFormat.format(Messages.getString("dialog.about.text"), Sandbox.VERSION, Version.getVersion()));
        JTextPane text = new JTextPane();
        text.setEditable(false);
        try {
            text.setPage(this.getClass().getResource(Messages.getString("dialog.about.html")));
        } catch (IOException e) {
            text.setText(Messages.getString("dialog.about.html.error"));
        }
        text.addHyperlinkListener(new HyperlinkListener() {

            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        if (desktop.isSupported(Desktop.Action.BROWSE)) {
                            try {
                                URI uri = e.getURL().toURI();
                                desktop.browse(uri);
                            } catch (URISyntaxException ex) {
                                System.err.println(MessageFormat.format(Messages.getString("dialog.about.uri.error"), e.getURL()));
                            } catch (IOException ex) {
                                System.err.println(Messages.getString("dialog.about.navigate.error"));
                            }
                        }
                    }
                }
            }
        });
        JScrollPane scroller = new JScrollPane(text);
        container.add(icon);
        container.add(scroller);
        this.pack();
    }

    /**
	 * Shows the about dialog.
	 * @param owner the dialog owner
	 */
    public static final void show(Window owner) {
        AboutDialog dialog = new AboutDialog(owner);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }
}
