package gui.channels;

import gui.TextStyle;
import gui.components.CustomJScrollPane;
import gui.components.list.ChatSessionMemberListRenderer;
import gui.thirdparty.SortedListModel;
import gui.thirdparty.SortedListModel.SortOrder;
import gui.windows.AgentPopupMenu;
import gui.windows.MainWindow;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.UUID;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import models.requests.RequestGroupSendMessage;
import models.secondlife.Agent;
import models.secondlife.Group;
import models.secondlife.Message;
import whisper.Observable;
import whisper.Observer;
import whisper.Whisper;
import cc.slx.java.string.StringHelper;

/**
 * This class represents a group {@link Channel}. A group channel is used for group chat.
 * 
 * @author Thomas Pedley.
 */
public class ChannelGroup extends JPanel implements Channel, Observer {

    /** The serialisation UID. */
    private static final long serialVersionUID = -4197061677216700180L;

    /** The name of the channel. */
    private String name;

    /** The text pane for displaying the conversation. */
    private JTextPane jtpChannelText = null;

    /** The scroll pane for housing the text pane so that we are able to scroll through the conversation. */
    private CustomJScrollPane jspText = null;

    /** The parent MainWindow (for callbacks). */
    private MainWindow parent;

    /** The list of channel participants. */
    private JList jlParticipants = null;

    /** The scroll pane for housing the participant list. */
    private JScrollPane jspParticipants = null;

    /** Flag to indicate whether the channel can be closed. */
    private boolean canClose;

    /** The UUID of the group chat. */
    private UUID UUID;

    /**
	 * Constructor.
	 * 
	 * @param name The name of the channel.
	 * @param parent The parent MainWindow (for callbacks).
	 * @param canClose True if this channel may be closed, false if not.
	 */
    public ChannelGroup(String name, MainWindow parent, boolean canClose) {
        super();
        this.name = name;
        this.parent = parent;
        this.canClose = canClose;
        initialize();
        receiveInformationalMessage("Started group chat with '" + name + "'.");
    }

    /**
	 * Initialise the public {@link Channel};
	 */
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.add(getJspText(), BorderLayout.CENTER);
        this.add(getJspParticipants(), BorderLayout.EAST);
        TextStyle.setStyles(this);
    }

    /**
	 * Get the participants scroll pane. If it has not been initialised, it is initialised upon first call.
	 * 
	 * @return The groups scroll pane.
	 */
    private JScrollPane getJspParticipants() {
        if (jspParticipants == null) {
            jspParticipants = new JScrollPane();
            jspParticipants.setPreferredSize(new Dimension(200, 0));
            jspParticipants.getViewport().add(getJlParticipants());
        }
        return jspParticipants;
    }

    /**
	 * Get the list of participants. If it has not been initialised, it is initialised upon first call.
	 * 
	 * @return The list of participants.
	 */
    public JList getJlParticipants() {
        if (jlParticipants == null) {
            jlParticipants = new JList(new SortedListModel(new DefaultListModel(), SortOrder.ASCENDING, Agent.comparatorDistance));
            jlParticipants.setCellRenderer(new ChatSessionMemberListRenderer());
            jlParticipants.addMouseListener(new MouseAdapter() {

                /**
				 * Handle the mouse clicking the control.
				 * 
				 * @param e The MouseEvent.
				 */
                @Override
                public void mouseClicked(MouseEvent e) {
                    getJlParticipants().setSelectedIndex(getJlParticipants().locationToIndex(e.getPoint()));
                    if (getJlParticipants().getSelectedIndex() >= 0) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            if (e.getClickCount() >= 2) {
                                Agent tmpAgent = (Agent) getJlParticipants().getSelectedValue();
                                if (tmpAgent.isResolved()) {
                                    ChannelPrivate c = new ChannelPrivate(tmpAgent.getName(), parent, true);
                                    c.setUUID(tmpAgent.getUUID());
                                    parent.addChannel(c, true);
                                    parent.focusTextInput((char) 0);
                                }
                            }
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            AgentPopupMenu apm = new AgentPopupMenu((Agent) (getJlParticipants().getSelectedValue()));
                            apm.show(getJlParticipants(), e.getX(), e.getY());
                        }
                    }
                }
            });
        }
        jlParticipants.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return jlParticipants;
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
            String localText = new String(text);
            String lowerCase = localText.toLowerCase();
            String firstWord = "";
            try {
                firstWord = lowerCase.split("\\s")[0];
            } catch (Exception ex) {
            }
            if (firstWord.charAt(0) == '/') {
                localText = localText.substring(firstWord.length()).trim();
                if (firstWord.length() == 3 && firstWord.startsWith("/me")) {
                    TextStyle.addLocalActionText(localText.trim(), Whisper.getClient().getName(), this);
                }
            } else {
                TextStyle.addLocalChatText(text, Whisper.getClient().getName(), this);
            }
            new RequestGroupSendMessage(Whisper.getClient().getConnection(), getUUID(), text).execute();
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
        if (message.getMessage().length() > 4 && message.getMessage().toLowerCase().startsWith("/me ")) {
            TextStyle.addRemoteActionText(message.getMessage().substring(3), message.getFromName(), this, false, friend);
        } else {
            TextStyle.addRemoteChatText(message.getMessage(), message.getFromName(), this, false, friend);
        }
    }

    /**
	 * Determine whether the channel accepts input or not.
	 * 
	 * @return True if the channel accepts input, false if not.
	 */
    @Override
    public boolean acceptsInput() {
        return true;
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
    @Override
    public void triggerTyping() {
    }

    /**
	 * Add a participant.
	 * 
	 * @param participant The participant to add.
	 */
    private void addParticipant(Agent participant) {
        synchronized (getJlParticipants()) {
            DefaultListModel m = ((SortedListModel) getJlParticipants().getModel()).getUnsortedList();
            m.add(m.size(), participant);
            participant.addObserver(this);
        }
    }

    /**
	 * Clear the list of participants.
	 */
    public void clearParticipants() {
        synchronized (getJlParticipants()) {
            DefaultListModel m = ((SortedListModel) getJlParticipants().getModel()).getUnsortedList();
            for (int i = 0; i < m.getSize(); i++) {
                Agent tmpAgent = (Agent) m.get(i);
                tmpAgent.removeObserver(this);
            }
            m.clear();
            ((SortedListModel) getJlParticipants().getModel()).forceUpdate();
        }
    }

    /**
	 * Remove a participant.
	 * 
	 * @param participant The participant to remove.
	 */
    private void removeParticipant(Agent participant) {
        synchronized (getJlParticipants()) {
            DefaultListModel m = ((SortedListModel) getJlParticipants().getModel()).getUnsortedList();
            for (int i = 0; i < m.getSize(); i++) {
                Agent listedParticipant = (Agent) m.get(i);
                if (listedParticipant.equals(participant)) {
                    m.remove(i);
                    listedParticipant.removeObserver(this);
                    return;
                }
            }
        }
    }

    /**
	 * Determine whether the passed in agent is already listed.
	 * 
	 * @param participant The participant.
	 * @return True if already listed, false if not.
	 */
    private boolean containsParticipant(Agent participant) {
        synchronized (jlParticipants) {
            DefaultListModel m = ((SortedListModel) getJlParticipants().getModel()).getUnsortedList();
            return m.contains(participant);
        }
    }

    /**
	 * Receive an update from the {@link Observable} object.
	 * 
	 * @param purgeFirst True indicates that the observer should purge before updating.
	 */
    @Override
    public void updateObserver(final boolean purgeFirst) {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    /**
					 * Called when the thread is run.
					 */
                    @Override
                    public void run() {
                        updateObserver(purgeFirst);
                    }
                });
            } catch (Exception e) {
                if (Whisper.isDebugging()) {
                    e.printStackTrace();
                }
            }
        } else {
            if (purgeFirst) {
                clearParticipants();
            }
            Group tmpGroup = Whisper.getClient().getGroups().get(getUUID());
            for (Agent tmpAgent : tmpGroup.getChatParticipants().values()) {
                if (!containsParticipant(tmpAgent)) {
                    addParticipant(tmpAgent);
                }
            }
            SortedListModel slm = ((SortedListModel) getJlParticipants().getModel());
            ArrayList<Agent> agentsToRemove = new ArrayList<Agent>();
            for (int i = 0; i < slm.getUnsortedList().size(); i++) {
                boolean found = false;
                Agent listedAgent = (Agent) slm.getUnsortedList().get(i);
                for (Agent tmpAgent : tmpGroup.getChatParticipants().values()) {
                    if (listedAgent.equals(tmpAgent)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    agentsToRemove.add(listedAgent);
                }
            }
            for (Agent tmpAgent : agentsToRemove) {
                removeParticipant(tmpAgent);
            }
            slm.forceUpdate();
        }
    }

    /**
	 * Show or hide the participants list.
	 * 
	 * @param visible True to enable, false to disable.
	 */
    public void showParticipants(boolean visible) {
        getJspParticipants().setVisible(visible);
        parent.invalidate();
        parent.repaint();
    }

    /**
	 * Clear the channel text.
	 */
    public void clearText() {
        getJtpChannelText().setText("");
    }
}
