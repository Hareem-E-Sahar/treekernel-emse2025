package org.hypergraphdb.peer.xmpp;

import static org.hypergraphdb.peer.Structs.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Future;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.MessageHandler;
import org.hypergraphdb.peer.Messages;
import org.hypergraphdb.peer.NetworkPeerPresenceListener;
import org.hypergraphdb.peer.PeerFilter;
import org.hypergraphdb.peer.PeerFilterEvaluator;
import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.PeerRelatedActivity;
import org.hypergraphdb.peer.PeerRelatedActivityFactory;
import org.hypergraphdb.peer.protocol.Protocol;
import org.hypergraphdb.util.CompletedFuture;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.muc.DefaultParticipantStatusListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * <p>
 * A peer interface implementation based upon the Smack library 
 * (see http://www.igniterealtime.org for more info). 
 * </p>
 * 
 * <p>
 * The connection is configured as a regular chat connection with
 * a server name, port, username and a password. Then peers are either
 * simply all users in this user's roster or all member of a chat room
 * or the union of both.  
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class XMPPPeerInterface implements PeerInterface {

    private String serverName;

    private Number port;

    private String user;

    private String password;

    private String roomId;

    private boolean ignoreRoster = false;

    private boolean anonymous;

    private boolean autoRegister;

    private int fileTransferThreshold;

    private HyperGraphPeer thisPeer;

    private ArrayList<NetworkPeerPresenceListener> presenceListeners = new ArrayList<NetworkPeerPresenceListener>();

    private MessageHandler messageHandler;

    ConnectionConfiguration config = null;

    XMPPConnection connection;

    MultiUserChat room = null;

    FileTransferManager fileTransfer;

    public void configure(Map<String, Object> configuration) {
        serverName = getPart(configuration, "serverUrl");
        port = getOptPart(configuration, 5222, "port");
        user = getPart(configuration, "user");
        password = getPart(configuration, "password");
        roomId = getOptPart(configuration, null, "room");
        ignoreRoster = getOptPart(configuration, false, "ignoreRoster");
        autoRegister = getOptPart(configuration, false, "autoRegister");
        anonymous = getOptPart(configuration, false, "anonymous");
        fileTransferThreshold = getOptPart(configuration, 100 * 1024, "fileTransferThreshold");
        config = new ConnectionConfiguration(serverName, port.intValue());
        config.setRosterLoadedAtLogin(true);
        config.setReconnectionAllowed(true);
        SmackConfiguration.setPacketReplyTimeout(30000);
    }

    private void reconnect() {
        if (connection != null && connection.isConnected()) stop();
        start();
    }

    private void processPeerJoin(String name) {
        for (NetworkPeerPresenceListener listener : presenceListeners) listener.peerJoined(name);
    }

    private void processPeerLeft(String name) {
        for (NetworkPeerPresenceListener listener : presenceListeners) listener.peerLeft(name);
    }

    private void processMessage(Message msg) {
        org.hypergraphdb.peer.Message M = null;
        if (thisPeer != null) thisPeer.getGraph().getTransactionManager().beginTransaction();
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(StringUtils.decodeBase64(msg.getBody()));
            M = new org.hypergraphdb.peer.Message((Map<String, Object>) new Protocol().readMessage(in));
        } catch (Exception t) {
            throw new RuntimeException(t);
        } finally {
            try {
                if (thisPeer != null) thisPeer.getGraph().getTransactionManager().endTransaction(false);
            } catch (Throwable t) {
                t.printStackTrace(System.err);
            }
        }
        messageHandler.handleMessage(M);
    }

    private void initPacketListener() {
        connection.addPacketListener(new PacketListener() {

            private void handlePresence(Presence presence) {
                String user = presence.getFrom();
                Roster roster = connection.getRoster();
                String n = makeRosterName(user);
                if (connection.getUser().equals(n)) return;
                if (roster.getEntry(n) == null && presence.getType() != Presence.Type.unavailable) return;
                if (presence.getType() == Presence.Type.subscribe) {
                    Presence reply = new Presence(Presence.Type.subscribed);
                    reply.setTo(presence.getFrom());
                    connection.sendPacket(reply);
                } else if (presence.getType() == Presence.Type.available) processPeerJoin(user); else if (presence.getType() == Presence.Type.unavailable) processPeerLeft(user);
            }

            private String makeRosterName(String name) {
                if (name.indexOf('/') < 0) return name;
                String first = name.substring(0, name.indexOf('/'));
                String second = name.substring(name.indexOf('/') + 1);
                if (second.length() != 36) return second + "@" + connection.getServiceName();
                try {
                    thisPeer.getGraph().getHandleFactory().makeHandle(second);
                    return first;
                } catch (NumberFormatException ex) {
                    return second;
                }
            }

            public void processPacket(Packet packet) {
                if (packet instanceof Presence) {
                    if (!ignoreRoster) handlePresence((Presence) packet);
                    return;
                }
                processMessage((Message) packet);
            }
        }, new PacketFilter() {

            public boolean accept(Packet p) {
                if (p instanceof Presence) return true;
                if (!(p instanceof Message)) return false;
                Message msg = (Message) p;
                if (!msg.getType().equals(Message.Type.normal)) return false;
                Boolean hgprop = (Boolean) msg.getProperty("hypergraphdb");
                return hgprop != null && hgprop;
            }
        });
    }

    private String roomJidToUser(String jid) {
        String[] A = jid.split("/");
        return A[1] + "@" + connection.getServiceName();
    }

    private void initRoomConnectivity() {
        room = new MultiUserChat(getConnection(), roomId);
        room.addParticipantStatusListener(new DefaultParticipantStatusListener() {

            @Override
            public void joined(String participant) {
                processPeerJoin(roomJidToUser(participant));
            }

            public void kicked(String participant, String actor, String reason) {
                processPeerLeft(roomJidToUser(participant));
            }

            public void left(String participant) {
                processPeerLeft(roomJidToUser(participant));
            }
        });
    }

    private void login() throws XMPPException {
        if (anonymous) connection.loginAnonymously(); else {
            try {
                connection.login(user, password, thisPeer != null && thisPeer.getGraph() != null ? thisPeer.getIdentity().getId().toString() : null);
            } catch (XMPPException ex) {
                if (ex.getMessage().indexOf("authentication failed") > -1 && autoRegister && connection.getAccountManager().supportsAccountCreation()) {
                    connection.getAccountManager().createAccount(user, password);
                    connection.disconnect();
                    connection.connect();
                    connection.login(user, password);
                } else throw ex;
            }
        }
    }

    public void start() {
        assert messageHandler != null : new NullPointerException("MessageHandler not specified.");
        connection = new XMPPConnection(config);
        try {
            connection.connect();
            connection.addConnectionListener(new MyConnectionListener());
            fileTransfer = new FileTransferManager(connection);
            fileTransfer.addFileTransferListener(new BigMessageTransferListener());
            initPacketListener();
            login();
            if (roomId != null && roomId.trim().length() > 0) initRoomConnectivity();
            if (room != null) room.join(user);
            if (!ignoreRoster) {
                final Roster roster = connection.getRoster();
                Presence presence = new Presence(Presence.Type.subscribe);
                for (RosterEntry entry : roster.getEntries()) {
                    presence.setTo(entry.getUser());
                    connection.sendPacket(presence);
                }
            }
        } catch (XMPPException e) {
            if (connection != null && connection.isConnected()) connection.disconnect();
            throw new RuntimeException(e);
        }
    }

    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    public void stop() {
        if (connection != null) try {
            connection.disconnect();
        } catch (Throwable t) {
        }
    }

    public PeerRelatedActivityFactory newSendActivityFactory() {
        return new PeerRelatedActivityFactory() {

            public PeerRelatedActivity createActivity() {
                return new PeerRelatedActivity() {

                    public Boolean call() throws Exception {
                        org.hypergraphdb.peer.Message msg = getMessage();
                        if (getPart(msg, Messages.REPLY_TO) == null) {
                            combine(msg, struct(Messages.REPLY_TO, connection.getUser()));
                        }
                        Protocol protocol = new Protocol();
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        thisPeer.getGraph().getTransactionManager().beginTransaction();
                        try {
                            protocol.writeMessage(out, msg);
                        } catch (Throwable t) {
                            System.err.println("Failed to serialize message " + msg);
                            t.printStackTrace(System.err);
                        } finally {
                            try {
                                thisPeer.getGraph().getTransactionManager().endTransaction(false);
                            } catch (Throwable t) {
                                t.printStackTrace(System.err);
                            }
                        }
                        byte[] data = out.toByteArray();
                        if (data.length > fileTransferThreshold) {
                            OutgoingFileTransfer outFile = fileTransfer.createOutgoingFileTransfer((String) getTarget());
                            outFile.sendStream(new ByteArrayInputStream(data), "", data.length, "");
                            return true;
                        } else {
                            try {
                                Message xmpp = new Message((String) getTarget());
                                xmpp.setBody(StringUtils.encodeBase64(out.toByteArray()));
                                xmpp.setProperty("hypergraphdb", Boolean.TRUE);
                                connection.sendPacket(xmpp);
                                return true;
                            } catch (Throwable t) {
                                t.printStackTrace(System.err);
                                return false;
                            }
                        }
                    }
                };
            }
        };
    }

    public Future<Boolean> send(Object networkTarget, org.hypergraphdb.peer.Message msg) {
        PeerRelatedActivityFactory activityFactory = newSendActivityFactory();
        PeerRelatedActivity act = activityFactory.createActivity();
        act.setTarget(networkTarget);
        act.setMessage(msg);
        if (thisPeer != null) return thisPeer.getExecutorService().submit(act); else {
            try {
                return new CompletedFuture<Boolean>(act.call());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void broadcast(org.hypergraphdb.peer.Message msg) {
        for (HGPeerIdentity peer : thisPeer.getConnectedPeers()) send(thisPeer.getNetworkTarget(peer), msg);
    }

    public HyperGraphPeer getThisPeer() {
        return thisPeer;
    }

    public PeerFilter newFilterActivity(PeerFilterEvaluator evaluator) {
        throw new UnsupportedOperationException();
    }

    public void addPeerPresenceListener(NetworkPeerPresenceListener listener) {
        presenceListeners.add(listener);
    }

    public void removePeerPresenceListener(NetworkPeerPresenceListener listener) {
        presenceListeners.remove(listener);
    }

    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void setThisPeer(HyperGraphPeer thisPeer) {
        this.thisPeer = thisPeer;
    }

    public XMPPConnection getConnection() {
        return connection;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public Number getPort() {
        return port;
    }

    public void setPort(Number port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public boolean isAutoRegister() {
        return autoRegister;
    }

    public void setAutoRegister(boolean autoRegister) {
        this.autoRegister = autoRegister;
    }

    public int getFileTransferThreshold() {
        return fileTransferThreshold;
    }

    public void setFileTransferThreshold(int fileTransferThreshold) {
        this.fileTransferThreshold = fileTransferThreshold;
    }

    private class BigMessageTransferListener implements FileTransferListener {

        public void fileTransferRequest(FileTransferRequest request) {
            if (thisPeer.getIdentity(request.getRequestor()) != null) {
                IncomingFileTransfer inFile = request.accept();
                org.hypergraphdb.peer.Message M = null;
                java.io.InputStream in = null;
                thisPeer.getGraph().getTransactionManager().beginTransaction();
                try {
                    in = inFile.recieveFile();
                    if (inFile.getFileSize() > Integer.MAX_VALUE) throw new Exception("Message from " + request.getRequestor() + " to long with " + inFile.getFileSize() + " bytes.");
                    byte[] B = new byte[(int) inFile.getFileSize()];
                    for (int count = 0; count < inFile.getFileSize(); ) count += in.read(B, count, (int) inFile.getFileSize() - count);
                    M = new org.hypergraphdb.peer.Message((Map<String, Object>) new Protocol().readMessage(new ByteArrayInputStream(B)));
                } catch (Throwable t) {
                    t.printStackTrace(System.err);
                    throw new RuntimeException(t);
                } finally {
                    try {
                        thisPeer.getGraph().getTransactionManager().endTransaction(false);
                    } catch (Throwable t) {
                        t.printStackTrace(System.err);
                    }
                    try {
                        if (in != null) in.close();
                    } catch (Throwable t) {
                        t.printStackTrace(System.err);
                    }
                }
                messageHandler.handleMessage(M);
            } else request.reject();
        }
    }

    private class MyConnectionListener implements ConnectionListener {

        public void connectionClosed() {
        }

        public void connectionClosedOnError(Exception ex) {
            ex.printStackTrace(System.err);
            reconnect();
        }

        public void reconnectingIn(int arg0) {
        }

        public void reconnectionFailed(Exception ex) {
            ex.printStackTrace(System.err);
            reconnect();
        }

        public void reconnectionSuccessful() {
        }
    }

    static {
    }
}
