package gui.channels;

import gui.Resources;
import gui.TextStyle;
import gui.components.CustomJScrollPane;
import gui.components.MapImage;
import gui.components.TruncatingJLabel;
import gui.components.MapImage.MapListener;
import gui.components.list.NearbyAgentListRenderer;
import gui.thirdparty.SortedListModel;
import gui.thirdparty.SortedListModel.SortOrder;
import gui.windows.AgentPopupMenu;
import gui.windows.MainWindow;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.StyledDocument;
import models.requests.RequestChatSend;
import models.secondlife.Agent;
import models.secondlife.Message;
import models.secondlife.Message.Audibility;
import models.secondlife.Message.ChatType;
import models.secondlife.Message.SourceType;
import whisper.Observable;
import whisper.Observer;
import whisper.Whisper;
import cc.slx.java.string.StringHelper;

/**
 * This class represents a public {@link Channel}. A public channel is used for open-area chat.
 * 
 * @author Thomas Pedley.
 */
public class ChannelPublic extends JPanel implements Channel, Observer, MapListener {

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

    /** The label detailing the agent's current location. */
    private TruncatingJLabel tjlCurrentLocation = null;

    /** The panel containing the current location map. */
    private JPanel jpMap = null;

    /** Flag to indicate whether the channel can be closed. */
    private boolean canClose;

    /** Flag to indicate whether the channel is running. */
    private boolean running;

    /**
	 * Constructor.
	 * 
	 * @param name The name of the channel.
	 * @param parent The parent MainWindow (for callbacks).
	 * @param canClose True if this channel may be closed, false if not.
	 */
    public ChannelPublic(String name, MainWindow parent, boolean canClose) {
        super();
        this.name = name;
        this.parent = parent;
        this.canClose = canClose;
        initialize();
    }

    /**
	 * Initialise the public {@link Channel};
	 */
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.add(getJspText(), BorderLayout.CENTER);
        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.add(getJspParticipants(), BorderLayout.CENTER);
        eastPanel.add(getJpMap(), BorderLayout.NORTH);
        this.add(eastPanel, BorderLayout.EAST);
        JPanel northPanel = new JPanel(new GridLayout());
        northPanel.add(getTjlCurrentLocation());
        this.add(northPanel, BorderLayout.NORTH);
        TextStyle.setStyles(this);
        this.running = true;
        new Thread(new Runnable() {

            /**
			 * Called when the thread is run.
			 */
            @Override
            public void run() {
                while (running) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    Agent selectedAgent = (Agent) getJlParticipants().getSelectedValue();
                    ((SortedListModel) getJlParticipants().getModel()).forceUpdate();
                    if (selectedAgent != null) getJlParticipants().setSelectedValue(selectedAgent, true);
                }
            }
        }).start();
    }

    /**
	 * Dispose of the channel.
	 */
    public void dispose() {
        running = false;
    }

    /**
	 * Get the current location label. If it has not been initialised, it is initialised upon first call.
	 * 
	 * @return The current location panel.
	 */
    private TruncatingJLabel getTjlCurrentLocation() {
        if (tjlCurrentLocation == null) {
            tjlCurrentLocation = new TruncatingJLabel(this);
        }
        return tjlCurrentLocation;
    }

    /**
	 * Get the map panel. If it has not been initialised, it is initialised upon first call.
	 * 
	 * @return The current location panel.
	 */
    private JPanel getJpMap() {
        if (jpMap == null) {
            jpMap = new JPanel(new FlowLayout());
        }
        return jpMap;
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
            jlParticipants.setCellRenderer(new NearbyAgentListRenderer());
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
            jlParticipants.addListSelectionListener(new ListSelectionListener() {

                /**
				 * Called when the list value is changed.
				 */
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (getJpMap().getComponentCount() > 0) {
                        MapImage mapImage = (MapImage) getJpMap().getComponent(0);
                        mapImage.repaint();
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
            ChatType chatType = ChatType.NORMAL;
            int channel = 0;
            String firstWord = "";
            try {
                firstWord = lowerCase.split("\\s")[0];
            } catch (Exception ex) {
            }
            if (firstWord.charAt(0) == '/') {
                localText = localText.substring(firstWord.length()).trim();
                if (firstWord.length() == 3 && firstWord.startsWith("/me")) {
                    TextStyle.addLocalActionText(localText.trim(), Whisper.getClient().getName(), this);
                } else if ((firstWord.length() == 6 && firstWord.equals("/shout")) || (firstWord.length() == 3 && firstWord.equals("/s"))) {
                    chatType = ChatType.SHOUT;
                    TextStyle.addLocalActionText("shouts: " + localText, Whisper.getClient().getName(), this);
                    text = text.substring(firstWord.length()).trim();
                } else if ((firstWord.length() == 8 && firstWord.equals("/whisper")) || (firstWord.length() == 4 && firstWord.equals("/w"))) {
                    chatType = ChatType.WHISPER;
                    TextStyle.addLocalActionText("whispers: " + localText, Whisper.getClient().getName(), this);
                    text = text.substring(firstWord.length()).trim();
                } else if (firstWord.length() > 1) {
                    try {
                        channel = Integer.parseInt(firstWord.substring(1));
                        TextStyle.addLocalChatText("(" + channel + ") " + localText, Whisper.getClient().getName(), this);
                        text = text.substring(firstWord.length()).trim();
                    } catch (Exception ex) {
                        channel = 0;
                    }
                }
            } else {
                TextStyle.addLocalChatText(text, Whisper.getClient().getName(), this);
            }
            new RequestChatSend(Whisper.getClient().getConnection(), channel, text, chatType).execute();
        }
    }

    /**
	 * Get the channel UUID.
	 */
    @Override
    public UUID getUUID() {
        return null;
    }

    /**
	 * Set the UUID associated with the channel.
	 * 
	 * @param UUID the UUID associated with the channel.
	 */
    @Override
    public void setUUID(UUID UUID) {
    }

    /**
	 * Receive a message.
	 * 
	 * @param message The message received.
	 */
    @Override
    public void receiveMessage(Message message) {
        if (message.getMessage() == null || message.getMessage().length() <= 0) return;
        if (message.getSourceType().equals(SourceType.AGENT)) {
            String messageText = message.getMessage();
            if (message.getChatType().equals(ChatType.SHOUT)) {
                messageText = "/me shouts: " + messageText;
            } else if (message.getChatType().equals(ChatType.WHISPER)) {
                messageText = "/me whispers: " + messageText;
            }
            boolean friend = Whisper.getClient().getFriends().containsKey(message.getFromUUID());
            if (message.getMessage().toLowerCase().startsWith("/me ")) {
                TextStyle.addRemoteActionText(message.getMessage().substring(3), message.getFromName(), this, !message.getAudibility().equals(Audibility.FULLY), friend);
            } else {
                TextStyle.addRemoteChatText(message.getMessage(), message.getFromName(), this, !message.getAudibility().equals(Audibility.FULLY), friend);
            }
        } else if (message.getSourceType().equals(SourceType.SYSTEM)) {
            TextStyle.addSystemText(message.getMessage(), message.getFromName(), this);
        } else if (message.getSourceType().equals(SourceType.OBJECT)) {
            if (message.getMessage().toLowerCase().startsWith("/me ")) {
                TextStyle.addObjectActionText(message.getMessage().substring(3), message.getFromName(), this, !message.getAudibility().equals(Audibility.FULLY));
            } else {
                TextStyle.addObjectText(message.getMessage(), message.getFromName(), this, !message.getAudibility().equals(Audibility.FULLY));
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
            Agent selectedAgent = (Agent) getJlParticipants().getSelectedValue();
            DefaultListModel m = ((SortedListModel) getJlParticipants().getModel()).getUnsortedList();
            m.add(m.size(), participant);
            participant.addObserver(this);
            if (selectedAgent != null) getJlParticipants().setSelectedValue(selectedAgent, true);
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
            Agent selectedAgent = (Agent) getJlParticipants().getSelectedValue();
            DefaultListModel m = ((SortedListModel) getJlParticipants().getModel()).getUnsortedList();
            for (int i = 0; i < m.getSize(); i++) {
                Agent listedParticipant = (Agent) m.get(i);
                if (listedParticipant.equals(participant)) {
                    m.remove(i);
                    listedParticipant.removeObserver(this);
                    return;
                }
            }
            if (selectedAgent != null && !selectedAgent.equals(participant)) getJlParticipants().setSelectedValue(selectedAgent, true);
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
            for (Agent tmpAgent : Whisper.getClient().getNearbyAgents().values()) {
                if (!containsParticipant(tmpAgent) && (!tmpAgent.getName().toLowerCase().equals(Whisper.getClient().getName().toLowerCase()))) {
                    addParticipant(tmpAgent);
                    tmpAgent.addObserver(this);
                }
            }
            SortedListModel slm = ((SortedListModel) getJlParticipants().getModel());
            ArrayList<Agent> agentsToRemove = new ArrayList<Agent>();
            for (int i = 0; i < slm.getUnsortedList().size(); i++) {
                Agent tmpAgent = (Agent) slm.getUnsortedList().get(i);
                if (!tmpAgent.getPresent()) {
                    agentsToRemove.add(tmpAgent);
                }
            }
            for (Agent tmpAgent : agentsToRemove) {
                removeParticipant(tmpAgent);
            }
        }
    }

    /**
	 * Show or hide the participants list.
	 * 
	 * @param visible True to enable, false to disable.
	 */
    public void showParticipants(boolean visible) {
        getJspParticipants().setVisible(visible);
        if (!visible) {
            if (getJpMap().getComponentCount() > 0) {
                MapImage mapImage = (MapImage) getJpMap().getComponent(0);
                mapImage.cleanUp();
            }
        }
        parent.invalidate();
        parent.repaint();
    }

    /**
	 * Set the current location.
	 * 
	 * @param simName The name of the Sim.
	 * @param parcelName The name of the current parcel.
	 * @param imageUUID The image to display for the current location..
	 */
    public void setCurrentLocation(String simName, String parcelName, UUID imageUUID) {
        StringBuilder sb = new StringBuilder();
        if (simName != null) sb.append(simName.trim());
        if (parcelName != null) sb.append(" - " + parcelName);
        String locationName = sb.toString();
        getTjlCurrentLocation().setText(locationName);
        if (getJpMap().getComponentCount() > 0) {
            MapImage miOld = (MapImage) getJpMap().getComponent(0);
            if (miOld != null) {
                miOld.cleanUp();
                miOld.dispose();
            }
        }
        getJpMap().removeAll();
        MapImage mi = new MapImage(Whisper.getClient().requestImage(imageUUID, new Dimension(200, 200), Resources.IMAGE_LOADING_200), 200, this);
        if (Whisper.getClient().getTrackingState()) {
            mi.plotNearbyAgents(Whisper.getClient().getNearbyAgents().values());
        }
        getJpMap().add(mi);
        mi.setVisible(false);
        mi.setVisible(true);
    }

    /**
	 * Update avatar positions.
	 * 
	 * @param nearbyAgents The nearby agents to plot
	 */
    public void updateAvatarPositions(Collection<Agent> nearbyAgents) {
        if (getJpMap().getComponentCount() > 0) {
            MapImage mapImage = (MapImage) getJpMap().getComponent(0);
            if (mapImage != null) {
                mapImage.cleanUp();
                mapImage.plotNearbyAgents(nearbyAgents);
            }
        }
    }

    /**
	 * Show or hide the map.
	 * 
	 * @param visible True to make the map visible, false to hide.
	 */
    public void showMap(boolean visible) {
        getJpMap().setVisible(visible);
        if (visible) {
            if (getJpMap().getComponentCount() > 0) {
                MapImage mapImage = (MapImage) getJpMap().getComponent(0);
                mapImage.resubmitRequest();
            }
        }
    }

    /**
	 * Clear the channel text.
	 */
    public void clearText() {
        getJtpChannelText().setText("");
    }

    /**
	 * Get the selected participant.
	 * 
	 * @return The selected participant (null if none selected).
	 */
    public Agent getSelectedParticipant() {
        if (getJlParticipants().getSelectedIndex() >= 0) {
            return (Agent) getJlParticipants().getSelectedValue();
        }
        return null;
    }

    /**
	 * Receive scaled hover coordinates. The coordinates that are being hovered over
	 * are scaled to represent coordinates on a Second Life map.
	 * 
	 * @param x The scaled X coordinate.
	 * @param y The scaled Y coordinate.
	 */
    @Override
    public void receiveScaledHoverCoords(int x, int y) {
        for (Agent tmpAgent : Whisper.getClient().getNearbyAgents().values()) {
            if (Math.abs(tmpAgent.getXCoord() - x + 1) <= 6 && Math.abs(tmpAgent.getYCoord() - y - 3) <= 6) {
                getJlParticipants().setSelectedValue(tmpAgent, true);
                break;
            }
        }
    }
}
