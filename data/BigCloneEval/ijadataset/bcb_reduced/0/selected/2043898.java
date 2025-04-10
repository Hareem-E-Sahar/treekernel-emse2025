package games.strategy.engine.chat;

import games.strategy.engine.lobby.server.IModeratorController;
import games.strategy.engine.lobby.server.ModeratorController;
import games.strategy.engine.message.IChannelMessenger;
import games.strategy.engine.message.IRemoteMessenger;
import games.strategy.engine.message.MessageContext;
import games.strategy.engine.message.RemoteName;
import games.strategy.net.IMessenger;
import games.strategy.net.INode;
import games.strategy.net.Messengers;
import games.strategy.util.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * chat logic.
 * <p>
 * 
 * A chat can be bound to multiple chat panels.
 * <p>
 * 
 * @author Sean Bridges
 */
public class Chat {

    private final List<IChatListener> m_listeners = new CopyOnWriteArrayList<IChatListener>();

    private final Messengers m_messengers;

    private final String m_chatChannelName;

    private final String m_chatName;

    private final SentMessagesHistory m_sentMessages;

    private long m_chatInitVersion = -1;

    private final Object m_mutex = new Object();

    private List<INode> m_nodes;

    private List<Runnable> m_queuedInitMessages = new ArrayList<Runnable>();

    private final List<ChatMessage> m_chatHistory = new ArrayList<ChatMessage>();

    private final StatusManager m_statusManager;

    private final ChatIgnoreList m_ignoreList = new ChatIgnoreList();

    private final HashMap<INode, LinkedHashSet<String>> m_notesMap = new HashMap<INode, LinkedHashSet<String>>();

    private void addToNotesMap(final INode node, final String note) {
        LinkedHashSet<String> current = m_notesMap.get(node);
        if (current == null) current = new LinkedHashSet<String>();
        current.add(note);
        m_notesMap.put(node, current);
    }

    public String getNotesForNode(final INode node) {
        final LinkedHashSet<String> notes = m_notesMap.get(node);
        if (notes == null) return null;
        final StringBuilder sb = new StringBuilder("");
        for (final String note : notes) {
            sb.append(" ");
            sb.append(note);
        }
        return sb.toString();
    }

    /** Creates a new instance of Chat */
    public Chat(final String chatName, final Messengers messengers) {
        m_messengers = messengers;
        m_statusManager = new StatusManager(messengers);
        m_chatChannelName = ChatController.getChatChannelName(chatName);
        m_chatName = chatName;
        m_sentMessages = new SentMessagesHistory();
        init();
    }

    public Chat(final IMessenger messenger, final String chatName, final IChannelMessenger channelMessenger, final IRemoteMessenger remoteMessenger) {
        this(chatName, new Messengers(messenger, remoteMessenger, channelMessenger));
    }

    public SentMessagesHistory getSentMessagesHistory() {
        return m_sentMessages;
    }

    public void addChatListener(final IChatListener listener) {
        m_listeners.add(listener);
        updateConnections();
    }

    public StatusManager getStatusManager() {
        return m_statusManager;
    }

    public void removeChatListener(final IChatListener listener) {
        m_listeners.remove(listener);
    }

    public Object getMutex() {
        return m_mutex;
    }

    private void init() {
        final IChatController controller = (IChatController) m_messengers.getRemoteMessenger().getRemote(ChatController.getChatControlerRemoteName(m_chatName));
        m_messengers.getChannelMessenger().registerChannelSubscriber(m_chatChannelSubscribor, new RemoteName(m_chatChannelName, IChatChannel.class));
        final Tuple<List<INode>, Long> init = controller.joinChat();
        synchronized (m_mutex) {
            m_chatInitVersion = init.getSecond().longValue();
            m_nodes = init.getFirst();
            final IModeratorController moderatorController = (IModeratorController) m_messengers.getRemoteMessenger().getRemote(ModeratorController.getModeratorControllerName());
            if (moderatorController != null) {
                for (final INode node : m_nodes) {
                    final boolean admin = moderatorController.isPlayerAdmin(node);
                    if (admin) addToNotesMap(node, "[Mod]");
                }
            }
            for (final Runnable job : m_queuedInitMessages) {
                job.run();
            }
            m_queuedInitMessages = null;
        }
        updateConnections();
    }

    /**
	 * Stop receiving events from the messenger.
	 */
    public void shutdown() {
        m_messengers.getChannelMessenger().unregisterChannelSubscriber(m_chatChannelSubscribor, new RemoteName(m_chatChannelName, IChatChannel.class));
        if (m_messengers.getMessenger().isConnected()) {
            final RemoteName chatControllerName = ChatController.getChatControlerRemoteName(m_chatName);
            final IChatController controller = (IChatController) m_messengers.getRemoteMessenger().getRemote(chatControllerName);
            controller.leaveChat();
        }
    }

    public void sendSlap(final String playerName) {
        final IChatChannel remote = (IChatChannel) m_messengers.getChannelMessenger().getChannelBroadcastor(new RemoteName(m_chatChannelName, IChatChannel.class));
        remote.slapOccured(playerName);
    }

    void sendMessage(final String message, final boolean meMessage) {
        final IChatChannel remote = (IChatChannel) m_messengers.getChannelMessenger().getChannelBroadcastor(new RemoteName(m_chatChannelName, IChatChannel.class));
        if (meMessage) remote.meMessageOccured(message); else remote.chatOccured(message);
        m_sentMessages.append(message);
    }

    private void updateConnections() {
        synchronized (m_mutex) {
            if (m_nodes == null) return;
            final List<INode> playerNames = new ArrayList<INode>(m_nodes);
            Collections.sort(playerNames);
            for (final IChatListener listener : m_listeners) {
                listener.updatePlayerList(playerNames);
            }
        }
    }

    public void setIgnored(final INode node, final boolean isIgnored) {
        if (isIgnored) {
            m_ignoreList.add(node.getName());
        } else {
            m_ignoreList.remove(node.getName());
        }
    }

    public boolean isIgnored(final INode node) {
        return m_ignoreList.shouldIgnore(node.getName());
    }

    public INode getLocalNode() {
        return m_messengers.getMessenger().getLocalNode();
    }

    public INode getServerNode() {
        return m_messengers.getMessenger().getServerNode();
    }

    private final List<INode> m_playersThatLeft_Last10 = new ArrayList<INode>();

    public List<INode> GetPlayersThatLeft_Last10() {
        return new ArrayList<INode>(m_playersThatLeft_Last10);
    }

    public List<INode> GetOnlinePlayers() {
        return new ArrayList<INode>(m_nodes);
    }

    private final IChatChannel m_chatChannelSubscribor = new IChatChannel() {

        private void assertMessageFromServer() {
            final INode senderNode = MessageContext.getSender();
            final INode serverNode = m_messengers.getMessenger().getServerNode();
            if (senderNode == null) return;
            if (!senderNode.equals(serverNode)) throw new IllegalStateException("The node:" + senderNode + " sent a message as the server!");
        }

        public void chatOccured(final String message) {
            final INode from = MessageContext.getSender();
            if (isIgnored(from)) {
                return;
            }
            synchronized (m_mutex) {
                m_chatHistory.add(new ChatMessage(message, from.getName(), false));
                for (final IChatListener listener : m_listeners) {
                    listener.addMessage(message, from.getName(), false);
                }
                while (m_chatHistory.size() > 1000) {
                    m_chatHistory.remove(0);
                }
            }
        }

        public void meMessageOccured(final String message) {
            final INode from = MessageContext.getSender();
            if (isIgnored(from)) {
                return;
            }
            synchronized (m_mutex) {
                m_chatHistory.add(new ChatMessage(message, from.getName(), true));
                for (final IChatListener listener : m_listeners) {
                    listener.addMessage(message, from.getName(), true);
                }
            }
        }

        public void speakerAdded(final INode node, final long version) {
            assertMessageFromServer();
            synchronized (m_mutex) {
                if (m_chatInitVersion == -1) {
                    m_queuedInitMessages.add(new Runnable() {

                        public void run() {
                            speakerAdded(node, version);
                        }
                    });
                    return;
                }
                if (version > m_chatInitVersion) {
                    m_nodes.add(node);
                    final IModeratorController moderatorController = (IModeratorController) m_messengers.getRemoteMessenger().getRemote(ModeratorController.getModeratorControllerName());
                    final boolean admin = moderatorController.isPlayerAdmin(node);
                    if (admin) addToNotesMap(node, "[Mod]");
                    updateConnections();
                    for (final IChatListener listener : m_listeners) {
                        listener.addStatusMessage(node.getName() + " has joined");
                    }
                }
            }
        }

        public void speakerRemoved(final INode node, final long version) {
            assertMessageFromServer();
            synchronized (m_mutex) {
                if (m_chatInitVersion == -1) {
                    m_queuedInitMessages.add(new Runnable() {

                        public void run() {
                            speakerRemoved(node, version);
                        }
                    });
                    return;
                }
                if (version > m_chatInitVersion) {
                    m_nodes.remove(node);
                    m_notesMap.remove(node);
                    updateConnections();
                    for (final IChatListener listener : m_listeners) {
                        listener.addStatusMessage(node.getName() + " has left");
                    }
                    m_playersThatLeft_Last10.add(node);
                    if (m_playersThatLeft_Last10.size() > 10) m_playersThatLeft_Last10.remove(0);
                }
            }
        }

        public void slapOccured(final String to) {
            final INode from = MessageContext.getSender();
            if (isIgnored(from)) {
                return;
            }
            synchronized (m_mutex) {
                if (to.equals(m_messengers.getChannelMessenger().getLocalNode().getName())) {
                    for (final IChatListener listener : m_listeners) {
                        final String message = "You were slapped by " + from.getName();
                        m_chatHistory.add(new ChatMessage(message, from.getName(), false));
                        listener.addMessage(message, from.getName(), false);
                    }
                } else if (from.equals(m_messengers.getChannelMessenger().getLocalNode())) {
                    for (final IChatListener listener : m_listeners) {
                        final String message = "You just slapped " + to;
                        m_chatHistory.add(new ChatMessage(message, from.getName(), false));
                        listener.addMessage(message, from.getName(), false);
                    }
                }
            }
        }
    };

    /**
	 * 
	 * While using this, you should synchronize on getMutex().
	 * 
	 * @return the messages that have occured so far.
	 */
    public List<ChatMessage> getChatHistory() {
        return m_chatHistory;
    }
}

class ChatMessage {

    private final String m_message;

    private final String m_from;

    private final boolean m_isMeMessage;

    public ChatMessage(final String message, final String from, final boolean isMeMessage) {
        m_message = message;
        m_from = from;
        m_isMeMessage = isMeMessage;
    }

    public String getFrom() {
        return m_from;
    }

    public boolean isMeMessage() {
        return m_isMeMessage;
    }

    public String getMessage() {
        return m_message;
    }
}
