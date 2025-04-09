package com.mrroman.linksender.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Stack;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import com.mrroman.linksender.Main;
import com.mrroman.linksender.gui.actions.SendMessageDialogAction;
import com.mrroman.linksender.ioc.ObjectStore;

public class MessagePopup extends JPanel {

    private static final Logger logger = Logger.getLogger("MessagePopup");

    private static final int MINIMUM_WIDTH = 150;

    private Popup popup;

    private JEditorPane label;

    private int position;

    private static final Border BORDER_BLINK = BorderFactory.createLineBorder(Color.RED, 2);

    private static final Border BORDER_NORMAL = BorderFactory.createEtchedBorder();

    private static Stack<MessagePopup> popupStack = new Stack<MessagePopup>();

    private Runnable blinker = new Runnable() {

        @Override
        public void run() {
            try {
                while (isVisible()) {
                    setBorder(BORDER_BLINK);
                    Thread.sleep(250);
                    setBorder(BORDER_NORMAL);
                    Thread.sleep(250);
                }
            } catch (InterruptedException e) {
            }
            setBorder(BORDER_NORMAL);
        }
    };

    private MouseListener mouseListener = new MouseListener() {

        @Override
        public void mouseClicked(MouseEvent e) {
            popup.hide();
            setVisible(false);
            popupStack.remove(MessagePopup.this);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
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
                            System.out.println(e.getURL());
                            Desktop.getDesktop().browse(e.getURL().toURI());
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }
    };

    public MessagePopup() {
        setBackground(Color.WHITE);
        label = new JEditorPane();
        label.setContentType("text/html");
        label.setEditable(false);
        label.setBackground(getBackground());
        label.addHyperlinkListener(hyperlinkListener);
        label.addMouseListener(mouseListener);
        setLayout(new BorderLayout());
        add(label, BorderLayout.CENTER);
        add(new JButton(ObjectStore.getObject(SendMessageDialogAction.class)), BorderLayout.EAST);
        addMouseListener(mouseListener);
        setBorder(BorderFactory.createEtchedBorder());
    }

    private void setText(String text) {
        label.setText(text.replaceAll("https?\\:\\/\\/[^\\s]*", "<a href=\"$0\">$0</a>"));
    }

    private void setPopup(Popup popup) {
        this.popup = popup;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    private void startBlink() {
        new Thread(blinker).start();
    }

    public static void showPopup(String text) {
        MessagePopup messagePopup = new MessagePopup();
        messagePopup.setText(text);
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle max = graphicsEnvironment.getMaximumWindowBounds();
        Dimension pref = messagePopup.getPreferredSize();
        if (pref.width < MINIMUM_WIDTH) {
            pref.width = MINIMUM_WIDTH;
            messagePopup.setPreferredSize(pref);
        }
        messagePopup.setMaximumSize(max.getSize());
        PopupFactory popupFactory = new PopupFactory();
        int posx = pref.width;
        int posy = pref.height;
        if (!popupStack.empty()) {
            posy += popupStack.peek().position;
        }
        Popup popup = popupFactory.getPopup(null, messagePopup, max.width - posx, max.height - posy);
        messagePopup.setPopup(popup);
        popup.show();
        messagePopup.startBlink();
        messagePopup.setPosition(posy);
        popupStack.push(messagePopup);
    }
}
