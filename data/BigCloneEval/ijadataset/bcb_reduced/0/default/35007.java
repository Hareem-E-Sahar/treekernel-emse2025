import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import ann.gui.CloseableFrame;

public class BB extends CloseableFrame {

    private static final long serialVersionUID = 1L;

    static TreeMap<String, Team> teams = new TreeMap<String, Team>();

    static Vector<Game> games = new Vector<Game>();

    JComboBox team1Combo = new JComboBox();

    JComboBox team2Combo = new JComboBox();

    JButton compareButton = new JButton("Compare");

    JButton loadXML = new JButton("Load XML File");

    JPanel buttonPanel = new JPanel();

    JTextArea textArea = new JTextArea("\n  Select teams and Compare!");

    public static class Game {

        Team team1;

        int team1score;

        Team team2;

        int team2score;

        String date;

        char label;

        Game(String T1, int T1score, String T2, int T2score, String gameDate) {
            if (!teams.containsKey(T1)) {
                Team team1 = new Team(T1);
                teams.put(T1, team1);
            }
            if (!teams.containsKey(T2)) {
                Team team2 = new Team(T2);
                teams.put(T2, team2);
            }
            team1 = teams.get(T1);
            team2 = teams.get(T2);
            team1score = T1score;
            team2score = T2score;
            date = gameDate;
            if (team1score > team2score) {
                team1.avgMov = ((team1.avgMov * team1.wins) + (team1score - team2score)) / (team1.wins + 1);
                team1.wins++;
                team2.losses++;
            } else {
                team2.avgMov = ((team2.avgMov * team2.wins) + (team2score - team1score)) / (team2.wins + 1);
                team1.losses++;
                team2.wins++;
            }
            team1.numGames++;
            team2.numGames++;
            team1.games.add(this);
            team2.games.add(this);
            label = 'U';
        }
    }

    public static class Team {

        String name;

        Vector<Game> games;

        int wins;

        int losses;

        int numGames;

        double avgMov;

        int totalWinPts;

        Team parent = null;

        char tLabel;

        int movToParent;

        Team(String teamname) {
            name = teamname;
            wins = losses = numGames = totalWinPts = 0;
            avgMov = 0.0;
            games = new Vector<Game>();
        }
    }

    public BB() {
        super("Derek Slenk's Probably Accurate Basketball Predictor");
        this.getContentPane().setLayout(new BorderLayout());
        for (String team : teams.keySet()) {
            team1Combo.addItem(team);
            team2Combo.addItem(team);
        }
        buttonPanel.add(loadXML);
        buttonPanel.add(team1Combo);
        buttonPanel.add(team2Combo);
        buttonPanel.add(compareButton);
        java.awt.Container contentPane = getContentPane();
        contentPane.add(buttonPanel, BorderLayout.NORTH);
        contentPane.add(textArea, BorderLayout.CENTER);
        compareButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                compare();
            }
        });
        loadXML.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                loadGames();
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void compare() {
        String team1 = (String) team1Combo.getSelectedItem();
        String team2 = (String) team2Combo.getSelectedItem();
        basicTeamInfo(team1, team2);
        head2headInfo(team1, team2);
        getStrengths(team1, team2);
        ArrayList<Game> headToHeadGames = removeHeadToHead(team1, team2);
        BFS(team1, team2);
        if (headToHeadGames.size() > 0) {
            replaceHeadToHead(headToHeadGames, team1);
        }
        java.util.Enumeration e = games.elements();
        for (int i = 0; i < games.size(); i++) {
            Game g = (Game) e.nextElement();
            g.team1.parent = null;
            g.team2.parent = null;
            g.team1.tLabel = 'U';
            g.team2.tLabel = 'U';
        }
    }

    /**
	 * This function finds the basic information: win/loss record and mov for
	 * the two teams that are passed in
	 * 
	 * @param String
	 *            t1name - name of the first team
	 * @param String
	 *            t2name - name of the second team
	 * @author Derek Slenk
	 */
    private void basicTeamInfo(String t1name, String t2name) {
        NumberFormat nf = new DecimalFormat("00.00");
        String mov1 = nf.format(teams.get(t1name).avgMov);
        String mov2 = nf.format(teams.get(t2name).avgMov);
        textArea.setText("Comparing ");
        textArea.append(t1name);
        textArea.append(" and ");
        textArea.append(t2name);
        textArea.append("\n");
        textArea.append(t1name + "\n");
        textArea.append("\t" + " Win/Loss Record: " + teams.get(t1name).wins + "/" + teams.get(t1name).losses);
        textArea.append("\n");
        textArea.append("\t" + "Average MOV: " + mov1);
        textArea.append("\n");
        textArea.append(t2name + "\n");
        textArea.append("\t" + " Win/Loss Record: " + teams.get(t2name).wins + "/" + teams.get(t2name).losses);
        textArea.append("\n");
        textArea.append("\t" + "Average MOV: " + mov2);
        textArea.append("\n");
        textArea.append("Expected Winner based on MOV: " + movWinner(t1name, t2name));
        textArea.append("\n");
    }

    /**
	 * This takes the two teams, grabs their movs, calculates the winner, and
	 * returns a predertimined string
	 * 
	 * @param t1name
	 * @param t2name
	 * @return a String, either the team name that won or a tie
	 */
    private static String movWinner(String t1name, String t2name) {
        if (teams.get(t1name).avgMov > teams.get(t2name).avgMov) {
            return t1name;
        } else if (teams.get(t2name).avgMov > teams.get(t1name).avgMov) {
            return t2name;
        } else {
            return "Tie";
        }
    }

    /**
	 * This function gets the results of the two teams head-to-head match-up
	 * info and appends the information to the text area
	 * 
	 * @param t1name
	 * @param t2name
	 */
    @SuppressWarnings("unchecked")
    private void head2headInfo(String t1name, String t2name) {
        int team1h2hwins = 0;
        int team2h2hwins = 0;
        String teamh2hwinner;
        textArea.append("\n");
        textArea.append("Head-to-Head Competitions: ");
        textArea.append("\n");
        Vector<Game> teamGames = teams.get(t1name).games;
        java.util.Enumeration e = teamGames.elements();
        while (e.hasMoreElements()) {
            Game g = (Game) e.nextElement();
            if (g.team1.name == teams.get(t2name).name || g.team2.name == teams.get(t2name).name) {
                textArea.append("    " + g.team1.name + " " + g.team1score);
                textArea.append(" " + g.team2.name + " " + g.team2score);
                textArea.append(" -- " + g.date + "\n");
                if (g.team1score > g.team2score && g.team1.name == t1name) {
                    team1h2hwins++;
                } else if (g.team1score > g.team2score && g.team1.name == t2name) {
                    team2h2hwins++;
                } else if (g.team2score > g.team1score && g.team2.name == t1name) {
                    team1h2hwins++;
                } else if (g.team2score > g.team1score && g.team2.name == t2name) {
                    team2h2hwins++;
                } else {
                    System.out.println("They tied....");
                }
            }
        }
        if (team1h2hwins > team2h2hwins) {
            teamh2hwinner = t1name;
        } else if (team2h2hwins > team1h2hwins) {
            teamh2hwinner = t2name;
        } else {
            teamh2hwinner = "No Determinable Winner";
        }
        textArea.append("Expected Winner based on Head-to-Head Games: " + teamh2hwinner + "\n");
    }

    /**
	 * This routine calculates a team's strength using that team's winning
	 * percentage and the average margin of victory. it is another approach at
	 * guessing the outcome of a game
	 * 
	 * @param t1name
	 *            - the name of team 1
	 * @param t2name
	 *            - the name of team 2
	 */
    private void getStrengths(String t1name, String t2name) {
        String report = "Strength Report:\n";
        double team1Mov = teams.get(t1name).avgMov;
        double team1NumGames = teams.get(t1name).games.size();
        double team1Wins = teams.get(t1name).wins;
        double team2Mov = teams.get(t2name).avgMov;
        double team2NumGames = teams.get(t2name).games.size();
        double team2Wins = teams.get(t2name).wins;
        double team1Strength = Math.floor((team1Wins / team1NumGames) * team1Mov);
        double team2Strength = Math.floor((team2Wins / team2NumGames) * team2Mov);
        report += t1name + ": " + team1Strength + "  " + t2name + ": " + team2Strength + "\n" + "Expected Winner based on Strength: ";
        if (team1Strength > team2Strength) {
            report += t1name + "\n";
        } else if (team1Strength < team2Strength) {
            report += t2name + "\n";
        } else {
            if (team1Mov > team2Mov) {
                report += t1name + "(their M.O.V. is higher)\n";
            } else if (team1Mov < team2Mov) {
                report += t2name + "(their M.O.V. is higher)\n";
            } else {
                report += "neither teams! It will likely be a draw\n." + "Their Strength and M.O.V. are equal!\n";
            }
        }
        textArea.append("\n" + report);
    }

    /**
	 * This executes a breadth first search on the tree that is created from the
	 * matchups in the XML file. This uses a queue data structure based off of a
	 * linked list. It uses node and edge labels to mark where we have been and
	 * where we need to go. Starts at the node of team1, and goes to team2.
	 * Appends the trail that is greater than length 1
	 * 
	 * @param t1name
	 *            - the name of team 1
	 * @param t2name
	 *            - the name of team 2
	 */
    @SuppressWarnings("unchecked")
    private void BFS(String t1name, String t2name) {
        Vector<Game> currentTeamGames = teams.get(t1name).games;
        Queue<Team> BFSqueue = new LinkedList<Team>();
        boolean foundChain = true;
        Team tempTeam = null;
        Game tempGame = null;
        teams.get(t1name).tLabel = 'V';
        BFSqueue.offer(teams.get(t1name));
        while (tempTeam != teams.get(t2name)) {
            if (BFSqueue.isEmpty()) {
                foundChain = false;
                break;
            }
            tempTeam = BFSqueue.poll();
            currentTeamGames = tempTeam.games;
            java.util.Enumeration e = currentTeamGames.elements();
            for (int i = 0; i < tempTeam.games.size(); i++) {
                tempGame = (Game) e.nextElement();
                if (tempGame.team1 == tempTeam) {
                    if (tempGame.team2.tLabel != 'V') {
                        BFSqueue.offer(tempGame.team2);
                        tempGame.team2.tLabel = 'V';
                        tempGame.team2.parent = tempTeam;
                        tempGame.team2.movToParent = tempGame.team2score - tempGame.team1score;
                    }
                } else if (tempGame.team2 == tempTeam) {
                    if (tempGame.team1.tLabel != 'V') {
                        BFSqueue.offer(tempGame.team1);
                        tempGame.team1.tLabel = 'V';
                        tempGame.team1.parent = tempTeam;
                        tempGame.team1.movToParent = tempGame.team1score - tempGame.team2score;
                    }
                }
            }
        }
        if (foundChain == true) {
            String firstTeam = tempTeam.name;
            textArea.append("\nThe shortest path is: " + tempTeam.name);
            int expectedMov = tempTeam.movToParent;
            while (tempTeam.parent != null) {
                tempTeam = tempTeam.parent;
                expectedMov += tempTeam.movToParent;
                textArea.append(" who played " + tempTeam.name);
            }
            textArea.append("\n  The expected margin of victory for " + firstTeam + " vs. " + tempTeam.name + " is " + expectedMov + " points");
        } else {
            textArea.append("\nNo path discovered");
        }
    }

    /**
	 * This routine replaces the head to head games that were taken out so the
	 * breadth first search would not return any paths of length 1.
	 * 
	 * @param gamesToAdd
	 *            - an array of game objects to add back into team 1's
	 *            collection
	 * @param t1name
	 *            - the name of team 1
	 */
    private void replaceHeadToHead(ArrayList<Game> gamesToAdd, String t1name) {
        for (int j = 0; j < gamesToAdd.size(); j++) {
            Game g = gamesToAdd.get(j);
            teams.get(t1name).games.add(g);
        }
    }

    /**
	 * This function goes through team 1's games and removes and head-to-head
	 * matches with team 2. We do this so when we do the breadth first search,
	 * we don't get matchups of length 1
	 * 
	 * @param t1name
	 *            - the name of team 1
	 * @param t2name
	 *            - the name of team 2
	 * @return headToHeadGames - this is an array of the games that were
	 *         removed. we will insert them back into the vector after
	 *         calculating the shortest path
	 */
    @SuppressWarnings("unchecked")
    private ArrayList<Game> removeHeadToHead(String t1name, String t2name) {
        Vector<Game> teamGames = teams.get(t1name).games;
        ArrayList<Game> headToHeadGames = new ArrayList<Game>();
        java.util.Enumeration e2 = teamGames.elements();
        while (e2.hasMoreElements()) {
            Game g = (Game) e2.nextElement();
            if ((g.team1.name == t1name && g.team2.name == t2name) || (g.team1.name == t2name && g.team2.name == t1name)) {
                headToHeadGames.add(g);
            }
        }
        if (headToHeadGames.size() != 0) {
            for (int k = 0; k < headToHeadGames.size(); k++) {
                teamGames.remove(headToHeadGames.get(k));
            }
        }
        return headToHeadGames;
    }

    public void loadGames() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.CANCEL_OPTION) return;
        File gameFile = chooser.getSelectedFile();
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(gameFile);
            doc.getDocumentElement().normalize();
            NodeList listOfGames = doc.getElementsByTagName("game");
            for (int g = 0; g < listOfGames.getLength(); g++) {
                Node gameNode = listOfGames.item(g);
                if (gameNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element gameElement = (Element) gameNode;
                    String team1 = gameElement.getElementsByTagName("team1").item(0).getTextContent();
                    String team1Score = gameElement.getElementsByTagName("team1Score").item(0).getTextContent();
                    String team2 = gameElement.getElementsByTagName("team2").item(0).getTextContent();
                    String team2Score = gameElement.getElementsByTagName("team2Score").item(0).getTextContent();
                    String date = gameElement.getElementsByTagName("date").item(0).getTextContent();
                    games.add(new Game(team1, Integer.parseInt(team1Score), team2, Integer.parseInt(team2Score), date));
                }
            }
            for (String team : teams.keySet()) {
                team1Combo.addItem(team);
                team2Combo.addItem(team);
            }
        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());
        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void loadGames(String gameFile) {
        System.out.println("In loadGames\n");
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(gameFile));
            doc.getDocumentElement().normalize();
            NodeList listOfGames = doc.getElementsByTagName("game");
            System.out.println("Total games: " + listOfGames.getLength());
            for (int g = 0; g < listOfGames.getLength(); g++) {
                Node gameNode = listOfGames.item(g);
                if (gameNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element gameElement = (Element) gameNode;
                    String team1 = gameElement.getElementsByTagName("team1").item(0).getTextContent();
                    String team1Score = gameElement.getElementsByTagName("team1Score").item(0).getTextContent();
                    String team2 = gameElement.getElementsByTagName("team2").item(0).getTextContent();
                    String team2Score = gameElement.getElementsByTagName("team2Score").item(0).getTextContent();
                    String date = gameElement.getElementsByTagName("date").item(0).getTextContent();
                    games.add(new Game(team1, Integer.parseInt(team1Score), team2, Integer.parseInt(team2Score), date));
                }
            }
        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());
        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static void dumpGames() {
        for (String team : teams.keySet()) {
            System.out.println(team + " " + teams.get(team).tLabel);
            Vector<Game> teamGames = teams.get(team).games;
            java.util.Enumeration e = teamGames.elements();
            while (e.hasMoreElements()) {
                Game g = (Game) e.nextElement();
                System.out.print("  " + g.team1.name + " " + g.team1score);
                System.out.print(" " + g.team2.name + " " + g.team2score);
                System.out.println(" -- " + g.date + "---" + g.label);
            }
        }
    }

    public static void main(String args[]) {
        JFrame f = new BB();
        f.setBounds(50, 50, 800, 600);
        f.setVisible(true);
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        f.addWindowListener(new WindowAdapter() {

            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });
    }
}
