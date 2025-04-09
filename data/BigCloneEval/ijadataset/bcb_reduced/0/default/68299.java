import utils.*;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.ListIterator;
import world.WorldModel;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.logging.*;

/**
 * This class is for starting a whole Team of Jais' Robocup Agents
 * @author Stephan Diederich
 */
public class JaisTeam {

    private ArrayList m_playerList;

    private String m_teamName;

    private String m_serverName;

    private int m_serverPort;

    private int m_numberOfPlayers;

    private boolean m_commandLineReading = true;

    private static Logger logger = Logger.getLogger("Jais");

    private static FileHandler fh;

    /**
    * @param f_teamName  Our Teamname
    * @param f_serverName  Name of the Server we want to connect to
    * @param f_serverPort  Port of the server
    * @param f_numberOfPlayers Number of Players to connect
    * @roseuid 3CE6898F00E0
    */
    public JaisTeam(String f_teamName, String f_serverName, int f_serverPort, int f_numberOfPlayers) throws xJaisTeam {
        logger.entering("JaisTeam", "JaisTeam");
        m_serverName = f_serverName;
        m_teamName = f_teamName;
        m_serverPort = f_serverPort;
        m_numberOfPlayers = f_numberOfPlayers;
        m_playerList = new ArrayList(f_numberOfPlayers);
        int l_connectedPlayers = 0;
        try {
            if (m_numberOfPlayers > 1) {
                m_playerList.add(new Jais(m_teamName, m_serverName, m_serverPort, true, 1, false));
                l_connectedPlayers++;
            }
            for (int i = l_connectedPlayers; i < m_numberOfPlayers; i++) {
                m_playerList.add(new Jais(m_teamName, m_serverName, m_serverPort, false, i + 1, false));
            }
        } catch (xJais ex) {
            logger.severe("Exception caught while initializing Team ! : " + ex.getMessage() + " ...aborting all...");
            for (ListIterator it = m_playerList.listIterator(); it.hasNext(); ) {
                Jais l_tempJais = (Jais) it.next();
                l_tempJais.quit();
            }
            throw (new xJaisTeam("Error while initializing !"));
        }
        readFromCommandline();
        logger.exiting("JaisTeam", "JaisTeam");
    }

    /**
    * The main method of this class stores all necessary commandline arguments
    * in a Hashtable and then instanciates its class
    * @param args = commandline parameters
    * @roseuid 3CE6898F0112
    */
    public static void main(String[] args) {
        try {
            fh = new FileHandler("JaisTeam.txt");
        } catch (Exception ex) {
            logger.warning("Couldn't open file where to log");
        }
        logger.addHandler(fh);
        fh.setFormatter(new SimpleFormatter());
        Hashtable l_filter = new Hashtable(10);
        l_filter.put("h", "help");
        l_filter.put("l", "loglevel");
        l_filter.put("n", "numberOfPlayers");
        Hashtable l_args = CommandLineParser.Parse(args, l_filter);
        if (l_args != null) {
            logger.setLevel(Level.parse((String) l_args.get("loglevel")));
            System.out.println("Current logging level:" + logger.getLevel());
            try {
                JaisTeam team = new JaisTeam((String) l_args.get("Teamname"), (String) l_args.get("Servername"), Integer.parseInt((String) l_args.get("Serverport")), Integer.parseInt((String) l_args.get("numberOfPlayers")));
            } catch (xJaisTeam xTeam) {
                logger.severe("Exception caught while starting the team ... aborting");
                return;
            }
        } else {
            Usage();
            return;
        }
        System.out.println("JaisTeam says Goodbye and wishes a nice day!");
    }

    /**
    * This method prints only the Usage of JaisTeam to the Commandline
    * @roseuid 3CEBD85202CB
    */
    public static void Usage() {
        System.out.println("\nUSAGE: java JaisTeam [TEAMNAME] [SERVERNAME] [SERVERPORT] -[OPTIONS]\n");
        System.out.println("TEAMNAME = your chosen teamname (default: Jais)");
        System.out.println("SERVERNAME = Server to connect to (default: localhost)");
        System.out.println("SERVERPORT = Port on which to connect (default: 6000)");
        System.out.println("OPTIONS = \n\t-n for the number of players to connect (default: 11) " + "\n\t-h for help " + "\n\t-l [n] loglevel n=0..7;  0 for all; 7 for off");
        System.out.println("");
    }

    /**
    * This method reads all commandline parameters which are typed in while the game
    * is running
    * @roseuid 3CEBF7D0029B
    */
    private void readFromCommandline() {
        BufferedReader l_input = new BufferedReader(new InputStreamReader(System.in));
        String l_message = new String();
        String l_tempString = new String();
        int l_targetPlayer = -1;
        while (m_commandLineReading) {
            System.out.print("\nType help for help \n>");
            try {
                l_message = l_input.readLine();
            } catch (Exception ex) {
                logger.severe("Exception caught in JaisTeam::readFromCommandline:" + ex.getMessage());
            }
            if (l_message.equalsIgnoreCase("Exit") || l_message.equalsIgnoreCase("E")) {
                m_commandLineReading = false;
                for (ListIterator it = m_playerList.listIterator(); it.hasNext(); ) {
                    Jais l_tempJais = (Jais) it.next();
                    l_tempJais.quit();
                }
            } else if (l_message.equalsIgnoreCase("help")) System.out.println("\nCommands are:" + "\n[Playernumber] visualization [ON | OFF]" + "\n[Playernumber] reconnect" + "\n[Playernumber] exit for kicking one player" + "\n\"Exit\" for exiting the game"); else {
                StringTokenizer l_tokenizer = new StringTokenizer(l_message);
                try {
                    l_targetPlayer = Integer.parseInt(l_tokenizer.nextToken());
                    if (!(l_targetPlayer > 0 && l_targetPlayer <= m_numberOfPlayers)) System.out.println("No such Player! (1..n)"); else {
                        l_tempString = "";
                        while (l_tokenizer.hasMoreTokens()) l_tempString += " " + l_tokenizer.nextToken();
                        ((Jais) m_playerList.get(l_targetPlayer - 1)).parseKbInfo(l_tempString);
                        logger.finest("Parsed \"" + l_tempString + "\" to Player " + l_targetPlayer + "\n");
                    }
                } catch (Exception ex) {
                    System.out.println("Didn't understand your command. Please retry (" + ex + ")\n");
                }
            }
        }
    }
}

class xJais extends Throwable {

    public xJais(String f_message) {
        super(f_message);
    }
}
