package org.esoul.server.runtime;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.esoul.common.constants.CommunicationCodes;
import org.esoul.common.dictionary.DictionaryDatabase;
import org.esoul.common.users.UserDatabase;
import org.esoul.server.configuration.ServerConfiguration;
import org.esoul.server.configuration.ServerConfigurationReader;
import org.xml.sax.SAXException;

/**
 * This class contains functionality for server used to interact with dictionary and user databases.
 * Also it has functionality to accept client's requests and has methods for sending server information 
 * to the clients. 
 * 
 * @author nsn
 *
 */
public class DictServer {

    /**
	 * Logger
	 */
    private static final Logger logger = Logger.getLogger(DictServer.class.getName());

    /**
	 * Clients' threads
	 */
    private List<ClientThread> clients = new ArrayList<ClientThread>();

    /**
	 * Counter of clients' requests
	 */
    private int clientsRequests = 0;

    /**
	 * Server configuration
	 */
    private ServerConfiguration config = null;

    /**
	 * Dictionary database
	 */
    private DictionaryDatabase dictDb = null;

    /**
	 * User database
	 */
    private UserDatabase userDb = null;

    /** 
	 * Listens for a connection to be made to the socket and creates client threads for every 
	 * of the clients which are trying to connect the server if the limit of connections is not 
	 * reached. If the maximum is reached message to the current client is send.
	 */
    public void start(String[] args) {
        try {
            if (args.length > 0) {
                initialize(args);
                ServerSocket serverSocket = new ServerSocket(config.getPort());
                logger.info("Server started.");
                while (true) {
                    Socket socket = serverSocket.accept();
                    if (clients.size() < config.getMaxClients()) {
                        ClientThread client = new ClientThread(this, socket);
                        client.start();
                    } else {
                        handleServerBusy(socket);
                    }
                }
            } else {
                System.out.println("You have to specify full path to configuration file.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Problem during server initialization.", e);
        }
    }

    /**
	 * Initializes the configuration, dictionary and user database, logger file.
	 * @param args Input arguments.
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
    private void initialize(String[] args) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, ParserConfigurationException, SAXException, IOException {
        ServerConfigurationReader configReader = new ServerConfigurationReader(args[0]);
        config = configReader.readServerConfiguration();
        String dictDbClassName = config.getDictionaryDatabase();
        dictDb = (DictionaryDatabase) createInstance(dictDbClassName);
        String userDbClassName = config.getUserDatabase();
        userDb = (UserDatabase) createInstance(userDbClassName);
        FileHandler handler = new FileHandler(config.getLogFile(), true);
        logger.addHandler(handler);
    }

    /**
	 * Creates instance by full class name.
	 * @param className Full class name.
	 * @return Current instance.
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
    private Object createInstance(String className) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> clazz = Class.forName(className);
        Constructor<?> constructor = clazz.getConstructor();
        return constructor.newInstance();
    }

    /**
	 * Adds client to the list of clients which are connected to the server and notify the client
	 * for the number of all connected clients.
	 * @param client Client thread.
	 */
    protected void login(ClientThread client) {
        synchronized (clients) {
            clients.add(client);
            for (ClientThread c : clients) {
                c.updateClientsNumber(clients.size());
            }
        }
    }

    /**
	 * Removes client from the list of clients which are connected to the server and notifies the client
	 * for the number of all connected clients.
	 * @param client Client thread.
	 */
    protected void logout(ClientThread client) {
        synchronized (clients) {
            clients.remove(client);
            for (ClientThread c : clients) {
                c.updateClientsNumber(clients.size());
            }
        }
    }

    /**
	 * Increases the counter of the clients requests and notifies all of the clients connected to the server.
	 */
    protected void triggerClientsRequestsUpdate() {
        clientsRequests++;
        for (ClientThread c : clients) {
            c.updateClientsRequests(clientsRequests);
        }
    }

    /**
	 * Notifies the client for the number of clients requests.
	 */
    protected void showClientsRequests() {
        for (ClientThread c : clients) {
            c.updateClientsRequests(clientsRequests);
        }
    }

    /**
	 * Notifies the client that server is busy.
	 * @param socket Socket
	 * @throws IOException
	 */
    private void handleServerBusy(Socket socket) throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeUTF(CommunicationCodes.SERVER_BUSY);
        out.close();
        socket.close();
    }

    /**
	 * Returns the current dictionary database.
	 * @return Returns the current dictionary database.
	 */
    protected DictionaryDatabase getDictionaryDatabase() {
        return dictDb;
    }

    /**
	 * Returns the current user database.
	 * @return Returns the current user database.
	 */
    protected UserDatabase getUserDatabase() {
        return userDb;
    }

    /**
	 * Start point of the program.
	 * @param args Input arguments entered when the program is started.
	 */
    public static void main(String[] args) {
        new DictServer().start(args);
    }
}
