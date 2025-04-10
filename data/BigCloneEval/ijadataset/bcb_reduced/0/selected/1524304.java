package com.lonedev.gtroot.server;

import com.lonedev.gtroot.shared.RocketServerConstants;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ChannelListener;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.Delivery;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A singleton managed object that will handle the various
 * @author Richard
 */
public class ServerUtils implements ManagedObject, ChannelListener, Serializable {

    private static final String SINGLETON_CLASS_NAME = "SERVER_UTILS";

    private static final int NUM_TABLES = 20;

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(ServerUtils.class.getName());

    private final Set<ManagedReference<RocketTable>> tables = new HashSet<ManagedReference<RocketTable>>();

    private ManagedReference<Channel> serverChatChannelRef;

    public static ServerUtils getInstance() {
        try {
            return (ServerUtils) AppContext.getDataManager().getBinding(SINGLETON_CLASS_NAME);
        } catch (NameNotBoundException ex) {
            ServerUtils db = new ServerUtils();
            AppContext.getDataManager().setBinding(SINGLETON_CLASS_NAME, db);
            return db;
        }
    }

    private ServerUtils() {
    }

    public void initializeServer() {
        logger.log(Level.INFO, "Initializing Rocket Server");
        logger.log(Level.CONFIG, "Creating {0} tables", new Object[] { NUM_TABLES });
        for (int i = 1; i <= NUM_TABLES; i++) {
            RocketTable rt = new RocketTable(i);
            tables.add(AppContext.getDataManager().createReference(rt));
        }
        logger.log(Level.CONFIG, "Tables created");
        logger.log(Level.CONFIG, "Creating server chat channel");
        Channel serverChatChannel = AppContext.getChannelManager().createChannel(RocketServerConstants.SERVER_CHAT_CHANNEL, this, Delivery.RELIABLE);
        serverChatChannelRef = AppContext.getDataManager().createReference(serverChatChannel);
        logger.log(Level.CONFIG, "Rocket Server initialization COMPLETE!");
    }

    public RocketTable getFreeTable() {
        for (ManagedReference<RocketTable> rocketTableRef : tables) {
            RocketTable rt = rocketTableRef.get();
            if (rt.isTableAvailable()) {
                return rt;
            }
        }
        return null;
    }

    public void receivedMessage(Channel channel, ClientSession sender, ByteBuffer message) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addServerChatChannelSession(ClientSession session) {
        logger.log(Level.INFO, "Adding " + session.getName() + " to channel " + RocketServerConstants.SERVER_CHAT_CHANNEL);
        serverChatChannelRef.getForUpdate().join(session);
    }

    public void removeServerChatChannelSession(ClientSession session) {
        logger.log(Level.INFO, "Removing " + session.getName() + " from channel " + RocketServerConstants.SERVER_CHAT_CHANNEL);
        serverChatChannelRef.getForUpdate().leave(session);
    }
}
