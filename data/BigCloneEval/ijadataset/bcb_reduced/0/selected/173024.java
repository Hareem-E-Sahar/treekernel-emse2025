package imogenart.cp;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class Link {

    boolean printok = false;

    private void p(String s) {
        if (printok) System.out.println("Link:: " + s);
    }

    public JButton GoLink(String url, String embeddingtext, String linktext, String followtext) throws URISyntaxException {
        final URI uri = new URI(url);
        JButton button = new JButton();
        button.setText("<HTML>" + embeddingtext + " <FONT color=\"#000099\"><U>" + linktext + "</U></FONT>" + followtext + "</HTML>");
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setBackground(Color.WHITE);
        button.setToolTipText(uri.toString());
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                open(uri);
            }
        });
        return button;
    }

    public JButton buttonLink(String url, String embeddingtext, String linktext, String followtext, Font smallerfont) throws URISyntaxException {
        final URI uri = new URI(url);
        JButton button = new JButton();
        button.setName(url);
        button.setText("<HTML>" + embeddingtext + " <FONT color=\"#000099\"><U>" + linktext + "</U></FONT>" + followtext + "</HTML>");
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(smallerfont);
        button.setForeground(Color.red);
        button.setToolTipText(uri.toString());
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                open(uri);
            }
        });
        return button;
    }

    private void open(URI uri) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(uri);
            } catch (IOException e) {
                p("IOException ");
                e.printStackTrace();
            }
        } else {
            System.out.println("Desktop.isDesktopSupported() - NOT");
        }
    }

    public void openBrowser(String url) {
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException us) {
            System.out.println("URI syntax exception in " + url);
            us.printStackTrace();
        }
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                if (uri != null) desktop.browse(uri);
            } catch (IOException e) {
                p("IOException ");
                e.printStackTrace();
            }
        } else {
            System.out.println("Desktop.isDesktopSupported() - NOT");
        }
    }

    public JPanel getLinkbutton(String url, String embeddingtext, String linktext, String followtext, Font font) {
        JPanel J = new JPanel();
        J.setBackground(Color.white);
        try {
            JButton B = GoLink(url, embeddingtext, linktext, followtext);
            B.setFont(font);
            B.setBackground(Color.white);
            J.add(B);
            p("Added button for " + url + " ie " + B);
        } catch (Exception e) {
            p("Could not make link beacuse: " + e);
            e.printStackTrace();
        }
        p("panel with link is " + J);
        return J;
    }

    public Font smallerfont() {
        Font smallerfont;
        Font oldfont = new JButton().getFont();
        String fontstring = new JButton("hello").toString();
        if (fontstring.contains("apple")) {
            smallerfont = new Font(oldfont.getFamily(), oldfont.getStyle(), (oldfont.getSize() - 2));
        } else {
            smallerfont = oldfont;
        }
        return smallerfont;
    }
}
