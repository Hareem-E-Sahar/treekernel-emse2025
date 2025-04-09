import java.util.Hashtable;
import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.List;
import java.util.LinkedList;
import java.util.*;
import java.io.*;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;

/**
 * class Server
 * This class serves several purposes, but all are interrelated. Primarily, the
 * server class listens for incoming connections on the listening port, and
 * dispatched connections to an available node. If no nodes are free, it will
 * inform the user and drop their connection. Thus secondly, the Server class
 * maintains an array of all the nodes in the BBS, and is responsible for their
 * lifetime.
 *
 * @author	$Author: pilat $
 * @version $Revision: 1.12 $
 * @see		User
 */
public class Server extends Thread {

    private final int numNodes = 8;

    private User[] nodes;

    private int numConnections = 0;

    private boolean listen = true;

    private boolean running = true;

    public ServerConfig sc;

    public LinkedList chatrooms = new LinkedList();

    private ServerSocket serverSocket = null;

    /**
	 * public Server
	 * This is the constructor for the Server class. It allocates the node\
	 * array, and sets all the nodes to null, since there's no fucking
	 * pointers in java.
	 *
	 * @author	Mike Pilat
	 */
    public Server(ServerConfig sc) {
        nodes = new User[numNodes];
        this.sc = sc;
        for (int i = 0; i < numNodes; i++) {
            nodes[i] = null;
        }
    }

    /**
	 * public void run()
	 * This is the function that is called when the Server thread is started by
	 * the main class, TBBS. It performs the listening and creating of new
	 * User objects in the nodes[] array.
	 *
	 * @author	Mike Pilat
	 * @see		User
	 * @see		TBBS
	 */
    public void run() {
        try {
            int portNum = 23;
            try {
                serverSocket = new ServerSocket(portNum);
                System.out.println("[SERVER] Listening (Port " + portNum + ")");
            } catch (IOException e) {
                System.err.println(e.getMessage());
                System.exit(-1);
            }
            while (running) {
                Socket s = serverSocket.accept();
                InetAddress iad = s.getInetAddress();
                System.out.println("[SERVER] Client connection from " + iad.getHostName() + " (" + iad.getHostAddress() + ")");
                if (!listen) {
                    PrintWriter p = new PrintWriter(s.getOutputStream(), true);
                    p.println("Not accepting connections at this time. Please try again later. Goodbye.");
                    p.flush();
                    p = null;
                    s.close();
                } else {
                    int i;
                    for (i = 0; i < numNodes; i++) {
                        if (nodes[i] == null) {
                            Calendar cal = new GregorianCalendar();
                            UserData ud = new UserData(this, s, i);
                            ud.userHostName = iad.getHostName();
                            ud.userHostAddr = iad.getHostAddress();
                            ud.connectedSince = cal.getTime().toString();
                            nodes[i] = new User(this, ud);
                            System.out.println("[SERVER] Client from " + iad.getHostName() + " assigned to node" + i);
                            nodes[i].start();
                            numConnections++;
                            break;
                        }
                    }
                    if (i == numNodes) {
                        System.out.println("[SERVER] All nodes full, rejecting connection from " + iad.getHostName());
                        PrintWriter p = new PrintWriter(s.getOutputStream(), true);
                        p.println("Sorry, all nodes (" + numNodes + ") are in use, please try again later. Goodbye.");
                        p.flush();
                        p = null;
                        s.close();
                    }
                }
                iad = null;
            }
            serverSocket.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
	 * public void RemoveUser
	 * Rmoves a user from the nodes[] array, and sets the node to null to
	 * indicate that its "free" again.
	 *
	 * @author	Mike Pilat
	 * @see		User
	 * @see		nodes
	 */
    public void removeUser(int node) {
        System.out.println("[SERVER] Node " + node + " disconnected.");
        nodes[node] = null;
    }

    public Enumeration getUsersOnline() {
        Hashtable h;
        h = new Hashtable();
        for (int i = numNodes - 1; i >= 0; i--) {
            if (nodes[i] != null) {
                Integer t = new Integer(i);
                h.put(nodes[i].ud.userName, t);
            }
        }
        return h.keys();
    }

    /**
	 * public Vector getUserNames
	 * Returns a vector containing all the user names for every node, or "null" if that node
	 * is empty.  The names in the vector are listed in order of the node they are connected
	 * to (i.e. if someone is connected to node 2, their name is listed in cell 2 of the
	 * vector).
	 *
	 * @author	Eric Marscin
	 * @see		UserData
	 * @see		nodes
	 */
    public Vector getUserNames() {
        Vector v = new Vector();
        for (int i = 0; i < numNodes; i++) {
            if (nodes[i] != null) {
                v.add(nodes[i].ud.userName);
            } else {
                v.add("null");
            }
        }
        return v;
    }

    public String getEventHandler(TbbsEvent event) {
        return sc.getEventHandler(event);
    }

    public List getServiceList() {
        return sc.getServiceList();
    }

    public boolean sendEvent(int node, TbbsEvent event) {
        if (nodes[node] != null) {
            nodes[node].addEvent(event);
            return true;
        }
        return false;
    }

    public boolean broadcastEvent(TbbsEvent event) {
        for (int i = 0; i < numNodes; i++) {
            if (nodes[i] != null) {
                nodes[i].addEvent(event);
            }
        }
        return true;
    }

    public boolean setListening(boolean set) {
        boolean temp = listen;
        listen = set;
        return temp;
    }

    public boolean setRunning(boolean set) {
        boolean temp = running;
        running = set;
        try {
            serverSocket.close();
            destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }

    public int getNumNodes() {
        return numNodes;
    }

    public User getUserClass(int node) {
        return nodes[node];
    }

    public int getNumConnections() {
        return numConnections;
    }

    /**
	 * checkUserExists - Checks to see if a certain username exists within the BBS's database.
	 * Returns true if it does, false if it doesn't.
	 *
	 * @author Eric Marscin
	 * @since 1.11
	 */
    public boolean checkUserExists(String username) {
        Context initCtx = null, envCtx = null;
        DataSource ds = null;
        Connection con = null;
        try {
            initCtx = new InitialContext();
            envCtx = (Context) initCtx.lookup("tbbs:comp/env");
            ds = (DataSource) envCtx.lookup("jdbc/tbbsDB");
            con = ds.getConnection();
            java.sql.Statement stmt = con.createStatement();
            ResultSet rset = stmt.executeQuery("SELECT userid FROM user_tbl WHERE userid = " + '"' + username + '"' + ";");
            return rset.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
