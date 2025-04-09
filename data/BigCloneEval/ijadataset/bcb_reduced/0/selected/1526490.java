package gui.channels;

import gui.TextStyle;
import gui.components.CustomJScrollPane;
import gui.windows.MainWindow;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.UUID;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import models.requests.RequestInstantMessageSend;
import models.requests.RequestTypingStatusChange;
import models.secondlife.Message;
import whisper.Whisper;
import cc.slx.java.string.StringHelper;

/**
 * This class represents a private {@link Channel}. Private channels are used for 1:1 conversations
 * (e.g. anything that is not public or group).
 * 
 * @author Thomas Pedley.
 */
public class ChannelPrivate extends JPanel implements Channel {

    /** The serialisation UID. */
    private static final long serialVersionUID = -4197061677216700180L;

    /** The name of the channel. */
    private String name;

    /** The text pane for displaying the conversation. */
    private JTextPane jtpChannelText = null;

    /** The scroll pane for housing the text pane so that we are able to scroll through the conversation. */
    private CustomJScrollPane jspText = null;

    /** The UUID associated with the channel. */
    private UUID UUID;

    /** The parent MainWindow (for callbacks). */
    private MainWindow parent;

    /** Flag to indicate whether the channel can be closed. */
    private boolean canClose;

    /** Flag to indicate whether this channel is being typed to. */
    private boolean isTyping;

    /** The time at which typing started. */
    private long typingStartedTime;

    /**
	 * Constructor.
	 * 
	 * @param name The name of the channel.
	 * @param parent The parent MainWindow (for callbacks).
	 * @param canClose True if this channel may be closed, false if not.
	 */
    public ChannelPrivate(String name, MainWindow parent, boolean canClose) {
        super();
        this.name = name;
        this.parent = parent;
        this.canClose = canClose;
        initialize();
        receiveInformationalMessage("Started conversation with '" + name + "'.");
    }

    /**
	 * Initialise the private {@link Channel};
	 */
    private void initialize() {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.weightx = 1.0;
        this.setLayout(new GridBagLayout());
        this.add(getJspText(), gridBagConstraints);
        TextStyle.setStyles(this);
    }

    /**
	 * Get the styled document.
	 * 
	 * @return The styled document.
	 */
    @Override
    public StyledDocument getStyledDocument() {
        return getJtpChannelText().getStyledDocument();
    }

    /**
	 * Get the name of the channel.
	 * 
	 * @return The name of the channel.
	 */
    @Override
    public String getName() {
        return name;
    }

    /**
	 * Get the channel text textpane. If it has not been initialised, it is initialised upon first call.
	 * 	
	 * @return The text pane for the channel.
	 */
    private JTextPane getJtpChannelText() {
        if (jtpChannelText == null) {
            jtpChannelText = new JTextPane();
            jtpChannelText.setAutoscrolls(true);
            jtpChannelText.setEditable(false);
            jtpChannelText.addKeyListener(new KeyAdapter() {

                /**
				 * Called when a key is typed.
				 * 
				 * @param e The KeyEvent.
				 */
                @Override
                public void keyPressed(KeyEvent e) {
                    if ((e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK || (e.getModifiers() & InputEvent.ALT_MASK) == InputEvent.ALT_MASK || (e.getModifiers() & InputEvent.META_MASK) == InputEvent.META_MASK) {
                        return;
                    }
                    parent.focusTextInput(e.getKeyChar());
                }
            });
            jtpChannelText.addMouseListener(new MouseAdapter() {

                /**
				 * Handle the mouse clicking the control.
				 * 
				 * @param e The MouseEvent.
				 */
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!e.isPopupTrigger() && SwingUtilities.isLeftMouseButton(e)) {
                        if (Desktop.isDesktopSupported()) {
                            try {
                                StyledDocument doc = (StyledDocument) getJtpChannelText().getDocument();
                                String url = (String) doc.getCharacterElement(getJtpChannelText().viewToModel(e.getPoint())).getAttributes().getAttribute(TextStyle.IDENTIFIER_URL);
                                if (url != null) {
                                    Desktop.getDesktop().browse(new java.net.URI(StringHelper.addProcotol(url, "http://")));
                                }
                            } catch (Exception ex) {
                            }
                        }
                    }
                }
            });
            jtpChannelText.addMouseMotionListener(new MouseMotionAdapter() {

                /**
				 * Called when the mouse is moved.
				 * 
				 * @param e The MouseEvent.
				 */
                @Override
                public void mouseMoved(MouseEvent e) {
                    try {
                        StyledDocument doc = (StyledDocument) getJtpChannelText().getDocument();
                        String url = (String) doc.getCharacterElement(getJtpChannelText().viewToModel(e.getPoint())).getAttributes().getAttribute(TextStyle.IDENTIFIER_URL);
                        if (url != null) {
                            setCursor(new Cursor(Cursor.HAND_CURSOR));
                        } else {
                            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        }
                    } catch (Exception ex) {
                    }
                }
            });
        }
        return jtpChannelText;
    }

    /**
	 * Get the text scroll pane. If it has not been initialised, it is initialised upon first call.
	 * 	
	 * @return The scroll pane for the channel text.
	 */
    private JScrollPane getJspText() {
        if (jspText == null) {
            jspText = new CustomJScrollPane();
            jspText.setViewportView(getJtpChannelText());
        }
        return jspText;
    }

    /**
	 * Send a message to the channel.
	 * 
	 * @param text The message text to send
	 */
    public void sendMessage(String text) {
        if (text.trim().length() > 0) {
            if (UUID != null) {
                if (text.toLowerCase().startsWith("/me ")) {
                    TextStyle.addLocalActionText(text.substring(3).trim(), Whisper.getClient().getName(), this);
                } else {
                    TextStyle.addLocalChatText(text, Whisper.getClient().getName(), this);
                }
                new RequestInstantMessageSend(Whisper.getClient().getConnection(), getUUID(), text).execute();
                new RequestTypingStatusChange(Whisper.getClient().getConnection(), getUUID(), false).execute();
            }
        }
    }

    /**
	 * Get the channel UUID.
	 */
    @Override
    public UUID getUUID() {
        return UUID;
    }

    /**
	 * Set the UUID associated with the channel.
	 * 
	 * @param UUID the UUID associated with the channel.
	 */
    @Override
    public void setUUID(UUID UUID) {
        this.UUID = UUID;
    }

    /**
	 * Receive a message.
	 * 
	 * @param message The message received.
	 */
    @Override
    public void receiveMessage(Message message) {
        if (message.getMessage() == null || message.getMessage().length() <= 0) return;
        boolean friend = Whisper.getClient().getFriends().containsKey(message.getFromUUID());
        if (!message.getOnline()) {
            if (message.getMessage().length() > 4 && message.getMessage().toLowerCase().startsWith("/me ")) {
                TextStyle.addOfflineActionText(message.getMessage().substring(3), message.getFromName(), this, friend);
            } else {
                TextStyle.addOfflineChatText(message.getMessage(), message.getFromName(), this, friend);
            }
        } else {
            if (message.getMessage().length() > 4 && message.getMessage().toLowerCase().startsWith("/me ")) {
                TextStyle.addRemoteActionText(message.getMessage().substring(3), message.getFromName(), this, false, friend);
            } else {
                TextStyle.addRemoteChatText(message.getMessage(), message.getFromName(), this, false, friend);
            }
        }
    }

    /**
	 * Determine whether the channel accepts input or not.
	 * 
	 * @return True if the channel accepts input, false if not.
	 */
    @Override
    public boolean acceptsInput() {
        return UUID != null;
    }

    /**
	 * Receive an informational message.
	 * 
	 * @param message The message text.
	 */
    @Override
    public void receiveInformationalMessage(String message) {
        TextStyle.addInformationalText(message, this);
    }

    /**
	 * Determine whether the channel can be closed or not.
	 * 
	 * @return True if the channel can be closed, otherwise false.
	 */
    @Override
    public boolean canClose() {
        return canClose;
    }

    /**
	 * Call to trigger that typing has begun.
	 */
    public void triggerTyping() {
        typingStartedTime = System.currentTimeMillis();
        if (!isTyping) {
            isTyping = true;
            new RequestTypingStatusChange(Whisper.getClient().getConnection(), UUID, true).execute();
            new Thread(new Runnable() {

                /**
				 * Called when the thread is started.
				 */
                @Override
                public void run() {
                    while (isTyping) {
                        if (System.currentTimeMillis() > typingStartedTime + 2000) {
                            new RequestTypingStatusChange(Whisper.getClient().getConnection(), UUID, false).execute();
                            isTyping = false;
                            break;
                        }
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }).start();
        }
    }

    /**
	 * Clear the channel text.
	 */
    public void clearText() {
        getJtpChannelText().setText("");
    }
}
