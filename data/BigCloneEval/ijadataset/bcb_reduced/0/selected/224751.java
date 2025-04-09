package com.mrroman.linksender.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import com.mrroman.linksender.Configuration;
import com.mrroman.linksender.MessageParser;
import com.mrroman.linksender.ioc.In;
import com.mrroman.linksender.ioc.Init;
import com.mrroman.linksender.ioc.Locales;
import com.mrroman.linksender.ioc.Log;
import com.mrroman.linksender.ioc.Name;
import com.mrroman.linksender.ioc.ObjectStore;
import com.mrroman.linksender.ioc.Prototype;
import com.mrroman.linksender.sender.Message;

/**
 *
 * @author mrozekon
 */
@Name("gui.PopupLabel")
@Prototype
public class PopupLabel extends JPanel {

    private List<ActionListener> actionListeners = new ArrayList<ActionListener>();

    @Locales
    private ResourceBundle messages;

    @In
    private Configuration config;

    @In
    private ResourceStore resourceStore;

    @Log
    private Logger logger;

    @In
    private MessageParser messageParser;

    private JLabel authorLabel;

    private JLabel dateLabel;

    private JEditorPane messageLabel;

    private JScrollPane scrollPane;

    private Message message;

    private boolean parseable;

    @Init
    public void init() {
        setLayout(new BorderLayout());
        setOpaque(false);
        parseable = true;
        add(upperPanel(), BorderLayout.NORTH);
        add(messagePanel(), BorderLayout.CENTER);
        add(buttonsPanel(), BorderLayout.SOUTH);
    }

    @Override
    public void setBackground(Color bg) {
        setOpaque(true);
        super.setBackground(bg);
    }

    public void setMessage(Message message) {
        this.message = message;
        authorLabel.setText(message.getSender());
        dateLabel.setText(SimpleDateFormat.getDateTimeInstance().format(message.getDate()));
        messageLabel.setContentType(parseable ? "text/html" : "text/plain");
        messageLabel.setText(parseable ? messageParser.parseMessage(message, false) : messageParser.replaceSenderInMessage(message));
        class LinkResolver extends SwingWorker<String, String> {

            private Message message;

            LinkResolver(Message message) {
                this.message = message;
            }

            @Override
            protected String doInBackground() throws Exception {
                return messageParser.parseMessage(message, true);
            }

            @Override
            protected void done() {
                try {
                    messageLabel.setText(get());
                } catch (Exception ex) {
                }
            }
        }
        if (parseable) {
            new LinkResolver(message).execute();
        }
    }

    public Message getMessage() {
        return message;
    }

    public void addActionListener(ActionListener actionListener) {
        actionListeners.add(actionListener);
    }

    public void removeActionListener(ActionListener actionListener) {
        actionListeners.remove(actionListener);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, 80);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(1000, 80);
    }

    public void setScrollable(boolean scrollable) {
        if (scrollable) {
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        } else {
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        }
    }

    public void setFocusableLabel(boolean focusable) {
        messageLabel.setFocusable(focusable);
    }

    public void setParseable(boolean parseable) {
        this.parseable = parseable;
        messageLabel.setContentType(parseable ? "text/html" : "text/plain");
    }

    private void triggerAction(String action) {
        ActionEvent event = new ActionEvent(this, 0, action);
        for (ActionListener actionListener : actionListeners) {
            actionListener.actionPerformed(event);
        }
    }

    private Component upperPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        panel.add(authorLabel = new JLabel(), BorderLayout.CENTER);
        panel.add(dateLabel = new JLabel(), BorderLayout.EAST);
        authorLabel.setFont(resourceStore.getAuthorLabelFont());
        authorLabel.setForeground(Color.WHITE);
        dateLabel.setFont(resourceStore.getDateLabelFont());
        dateLabel.setForeground(Color.WHITE);
        panel.setBackground(Color.GRAY);
        panel.addMouseListener(mouseListener);
        return panel;
    }

    private Component buttonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel open = new JLabel(resourceStore.getOpenIcon());
        open.setToolTipText(messages.getString("open_message"));
        open.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                triggerAction("OPEN");
            }
        });
        panel.add(open);
        JLabel reply = new JLabel(resourceStore.getReplyIcon());
        reply.setToolTipText(messages.getString("reply_message"));
        reply.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                triggerAction("REPLY");
            }
        });
        panel.add(reply);
        JLabel copy = new JLabel(resourceStore.getCopyIcon());
        copy.setToolTipText(messages.getString("copy_message"));
        copy.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                triggerAction("COPY");
            }
        });
        panel.add(copy);
        JLabel remove = new JLabel(resourceStore.getRemoveIcon());
        remove.setToolTipText(messages.getString("remove_tip_message"));
        remove.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                triggerAction("REMOVE");
            }
        });
        panel.add(remove);
        panel.addMouseListener(mouseListener);
        panel.setOpaque(false);
        return panel;
    }

    private Component messagePanel() {
        messageLabel = new JEditorPane();
        messageLabel.setContentType("text/html");
        messageLabel.setEditable(false);
        messageLabel.setFocusable(false);
        messageLabel.setBackground(getBackground());
        messageLabel.setOpaque(false);
        ((HTMLDocument) messageLabel.getDocument()).getStyleSheet().addRule(String.format("body { width: %dpx; font-family: %s; font-size: %dpx; }", getWidth(), config.getMessageLabelFontName(), config.getMessageLabelFontSize()));
        messageLabel.addHyperlinkListener(hyperlinkListener);
        messageLabel.addMouseListener(mouseListener);
        scrollPane = new JScrollPane(messageLabel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        setScrollable(false);
        return scrollPane;
    }

    private MouseListener mouseListener = new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) triggerAction("REMOVE");
        }
    };

    private HyperlinkListener hyperlinkListener = new HyperlinkListener() {

        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                JEditorPane pane = (JEditorPane) e.getSource();
                if (e instanceof HTMLFrameHyperlinkEvent) {
                    HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
                    HTMLDocument doc = (HTMLDocument) pane.getDocument();
                    doc.processHTMLFrameHyperlinkEvent(evt);
                } else {
                    try {
                        if (Desktop.isDesktopSupported() && (Desktop.getDesktop() != null)) {
                            Desktop.getDesktop().browse(e.getURL().toURI());
                            triggerAction("REMOVE");
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }
    };

    public static void main(String[] args) {
        ObjectStore.getInstance().setMessageBundle(ResourceBundle.getBundle("com.mrroman.linksender.resources.locale"));
        final JFrame frame = new JFrame("Aaa");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        final PopupLabel label = ObjectStore.getObject(PopupLabel.class);
        label.setScrollable(false);
        label.setParseable(false);
        label.setMessage(new Message("sender", "message"));
        label.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("REMOVE")) {
                    System.out.println(e.getActionCommand());
                    System.exit(0);
                }
            }
        });
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.add(label);
        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
}
