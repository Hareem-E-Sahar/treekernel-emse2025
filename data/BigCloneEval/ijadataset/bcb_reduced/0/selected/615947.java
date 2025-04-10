package chatserver.service;

import java.util.Map;
import javolution.util.FastMap;
import chatserver.model.ChatClient;
import chatserver.model.message.Message;
import chatserver.network.aion.serverpackets.SM_CHANNEL_MESSAGE;
import chatserver.network.netty.handler.ClientChannelHandler;

public class BroadcastService {

    private Map<Integer, ChatClient> clients;

    public static final BroadcastService getInstance() {
        return SingletonHolder.instance;
    }

    private BroadcastService() {
        clients = new FastMap<Integer, ChatClient>();
    }

    /**
	* 
	* @param client
	*/
    public void addClient(ChatClient client) {
        clients.put(client.getClientId(), client);
    }

    /**
	* 
	* @param client
	*/
    public void removeClient(ChatClient client) {
        clients.remove(client.getClientId());
    }

    /**
	* 
	* @param message
	*/
    public void broadcastMessage(Message message) {
        for (ChatClient client : clients.values()) {
            if (client.isInChannel(message.getChannel())) sendMessage(client, message);
        }
    }

    /**
	* 
	* @param chatClient
	* @param message
	*/
    public void sendMessage(ChatClient chatClient, Message message) {
        ClientChannelHandler cch = chatClient.getChannelHandler();
        cch.sendPacket(new SM_CHANNEL_MESSAGE(message));
    }

    @SuppressWarnings("synthetic-access")
    private static class SingletonHolder {

        protected static final BroadcastService instance = new BroadcastService();
    }
}
