package org.turms.ui.feed;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import org.turms.entity.Feed;

/**
 * @author spe_ra (raffaele@speraprojects.com)
 *
 */
public class FeedWindow extends JFrame implements HyperlinkListener {

    private static final long serialVersionUID = -5118448368796516683L;

    private final SimpleDateFormat formatter = new SimpleDateFormat("dd MM yyyy HH:mm");

    public FeedWindow(Feed feed) {
        setSize(800, 600);
        setLocationByPlatform(true);
        setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        String published = feed.getPublishedDate() != null ? formatter.format(feed.getPublishedDate()) : "";
        JLabel dateLabel = new JLabel(published);
        panel.add(dateLabel, BorderLayout.WEST);
        JLabel categoryLabel = new JLabel(feed.getSource().getCategory().getName());
        panel.add(categoryLabel, BorderLayout.EAST);
        add(panel, BorderLayout.SOUTH);
        String description = feed.getDescription();
        JTextPane desc = new JTextPane();
        desc.setEditable(false);
        HTMLEditorKit kit = new HTMLEditorKit();
        desc.setEditorKit(kit);
        desc.addHyperlinkListener(this);
        desc.setText(description);
        JScrollPane scroll = new JScrollPane(desc);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setBorder(BorderFactory.createTitledBorder(feed.getTitle()));
        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (EventType.ACTIVATED.equals(e.getEventType())) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
