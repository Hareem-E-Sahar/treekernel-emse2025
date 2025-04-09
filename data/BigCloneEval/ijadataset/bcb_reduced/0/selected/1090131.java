package de.blitzcoder.collide.gui;

import de.blitzcoder.collide.Config;
import de.blitzcoder.collide.Interface;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import de.blitzcoder.collide.icons.Icon;
import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 *
 * @author blitzcoder
 */
public class StartupDialog extends IDEDialog implements HyperlinkListener {

    private JLabel logo;

    protected JTextPane text;

    private JLabel copyright;

    /** Creates a new instance of StartupDialog */
    public StartupDialog() {
        super(Interface.get(), "Willkommen bei CollIDE");
        Interface.get().setEnabled(false);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        setResizable(false);
        logo = new JLabel();
        logo.setIcon(Icon.load("logo_small.png"));
        text = new JTextPane();
        text.setContentType("text/html");
        text.setEditable(false);
        text.setText("<html>" + "Willkommen bei CollIDE. CollIDE befindet sich momentan " + "im Betastadium. Das bedeutet insbesondere, dass es noch " + "Probleme mit der Stabilität und Geschwindigkeit geben " + "kann. Ich bitte Sie, Bugs und andere Probleme auf" + " <a href='www.blitz-coder.de/collide'>www.blitz-coder.de/collide</a> zu berichten." + "<br><br>" + "Viel Spaß mit CollIDE!" + "</html>");
        text.setPreferredSize(new Dimension(400, 200));
        copyright = new JLabel("" + "Copyright (©) 2007-2009 by Johannes Wotzka. All rights reserved");
        text.addHyperlinkListener(this);
        text.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0.0;
        c.weightx = 1.0;
        add(logo, c);
        c.gridy = 1;
        c.weighty = 1.0;
        c.weightx = 0.0;
        c.fill = c.BOTH;
        add(text, c);
        c.gridy = 2;
        c.weighty = 0.0;
        add(copyright, c);
        logo.setBackground(Color.WHITE);
        addWindowListener();
    }

    public static void init() {
        if (Config.getBooleanProperty("system.firstRun")) {
            StartupDialog dialog = new StartupDialog();
            dialog.pack();
            dialog.setVisible(true);
        }
    }

    protected void addWindowListener() {
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent w) {
                Interface.get().setEnabled(true);
            }
        });
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        URL url = e.getURL();
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(url.toURI());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
