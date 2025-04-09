package com.textflex.txtfl;

import java.io.*;
import java.util.*;

/**The football players.
 * Each player includes his default position, hometown, priority according
 * to the depth charts, and stats-based ability values for passing, blocking, 
 * and the like.  
 *
 * <p>Players range from quarterbacks
 * to kickers to defensive ends, and it's a good idea to have at least one of all of
 * them, often two.  tXtFL uses a slightly modified position scheme to more
 * specifically define where each player plays by default.  Rather than merely "WR"
 * for "wide receiver," positions often include an "R" or "L" appended to the front.
 * For example, a wide receiver who lines up on the right side of the field by default
 * receives the designation, "RWR," for "right wide receiver."  Positions that do
 * not have a left and right pair, such as "QB" (quarterback )or "SS" (strong safety)
 * do not have such designations.  See Draft for a list of these
 * designations.
 * 
 * <p>Each team should have at least one of each type of player.  The players
 * available are located in the <code>players</code> directory, located in the 
 * base tXtFL directory.  By listing the name of the file, minus the ".txt" extension,
 * in the team spec after the keyline "players", the player will be included on 
 * the team at the position and depth order as in the player's spec.  To override 
 * the position or depth order, the team spec can list the new position and order,
 * respectively, after the player's name.  The keyword "starter" designates that
 * the player is by default a starter for the given position.  "-" for either the
 * position or order tells the team to use the player's value.  For example, to 
 * specify that a player is a starter in his or her default position, the line should
 * read, "player_name - starter".
 *
 * <p>All players have the same set of ability values to allow 
 * straightforward comparison among players; for example, a kicker could 
 * be assigned to be a wide receiver, and a defensive end could be 
 * assigned to perform the kickoff, and the stats would still translate into
 * reasonable ability values.  These values should normally fall within 
 * a 0-100 point range, inclusive, though higher or lower values are permitted
 * for greater versatility.
 *
*/
public class Player implements Cloneable, Serializable {

    private static final String NEWLINE = System.getProperty("line.separator");

    public static final String FILENAME_TEMPLATE_PLAYER = "template-player.txt";

    public static final String BIOGRAPHICAL = "Biographical";

    public static final String STATS = "Stats";

    public static final String PLAYER = "Player";

    public static final String NAME = "Name";

    public static final String TEAM = "Team";

    public static final String POSITION = "Pos";

    public static final String POSITION_ALT = "Tm.";

    public static final String DEPTH = "Depth";

    public static final int STARTER_DEPTH = 1;

    public static final String HANDS = "Hands";

    public static final String FEET = "Feet";

    public static final String BUILD = "Build";

    public static final String SMARTS = "Smarts";

    public static final int DEFAULT_RATING = 50;

    private File spec = null;

    private boolean written = false;

    private int pass = 0;

    private int receive = 0;

    private int run = 0;

    private int block = 0;

    private int kickDist = 0;

    private int kickWidth = 0;

    private int punt = 0;

    private String firstName = "-";

    private String middleName = "-";

    private String lastName = "-";

    private String team = "-";

    private double hands = 1;

    private double feet = 1;

    private double build = 1;

    private double smarts = 1;

    private int year = 0;

    private int pay = 0;

    private int id = -1;

    private int health = 100;

    private String name = "";

    private String position = "";

    private String specPosition = "";

    private String[] specPositions = new String[20];

    private int specPositionsIdx = 0;

    private boolean starter = false;

    private int depth = 1;

    private int specDepth = 1;

    private PlayerYearStat[] yearStats = new PlayerYearStat[50];

    private int yearStatsIdx = 0;

    private PlayerYearStat totStats = new PlayerYearStat();

    private PlayerYearStat currGameStats = new PlayerYearStat();

    /** Constructs a player to set later.
     */
    public Player() {
    }

    /** Constructs a player for a given position.
	The rest of the player's values are to be set later.
	@param aPosition position of player
    */
    public Player(String aPosition) {
        position = aPosition;
    }

    /** Constructs a player and sets his complete description, including
	position and ability values.
	All ability values are based on a 0-100 scale.
	@param aName name
	@param aPosition position
	@see #getPosition()
	@param aStarter <code>true</code> if the player plays at the start of games
	@param aPass pass ability
	@param aReceive catch ability
	@param aRun run ability
	@param aBlock blocking ability
	@param aKickDist tee-kicking ability
	@param aKickWidth place-holding ability, such as that for 
	width or field-goals
	@param aPunt drop-kick ability
    */
    public Player(String aName, String aPosition, boolean aStarter, int aPass, int aReceive, int aRun, int aBlock, int aKickDist, int aKickWidth, int aPunt) {
        name = aName;
        position = aPosition;
        starter = aStarter;
        pass = aPass;
        receive = aReceive;
        run = aRun;
        block = aBlock;
        kickDist = aKickDist;
        kickWidth = aKickWidth;
        punt = aPunt;
    }

    /** Creates a player from a player file spec.
	 * @param f player file spec
	 */
    public Player(File f) throws IOException {
        loadPlayer(f);
    }

    public void initializePlayer(PlayerYearStat stat) {
        setTotStats(stat);
        ratePlayer();
        setStarter(isStarter());
        setPosition(getSpecPosition(0));
    }

    /**Loads a player from a spec file.
	 * The spec file is assumed to conform to the format that 
	 * <code>writeUpdatedSpec(File)</code> outputs.
	 * The spec file consists of biographical and statistical histories and
	 * as of tXtFL v.0.9.1 includes player skill values, a measure of raw
	 * talent.  {@link #ratePlayer()} calculates these
	 * ability values based on comparisons with average, generic stats.
	 * @param spec the player's spec file, which is
	 * the player's first and last name, in that order, separated by an 
	 * underscore, with <code>.txt</code> appended to the end to 
	 * open automatically in a text editor
	 * @throws if the spec file cannot be opened, closed, or does
	 * not conform to the standard tXtFL player format
	*/
    public void loadPlayer(File spec) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(spec)));
            setSpec(spec);
            String line = "";
            String[] splitLine = null;
            String currSection = "";
            while ((line = LibTxtfl.getNextLine(reader)) != null) {
                line = line.trim();
                if (line.indexOf(LibTxtfl.SECTION) == 0) {
                    splitLine = line.split(" ");
                    if (splitLine.length > 1) currSection = splitLine[1];
                } else if (currSection.equalsIgnoreCase(BIOGRAPHICAL)) {
                    loadBiographical(line);
                } else if (currSection.equalsIgnoreCase(STATS)) {
                    loadStat(line);
                }
            }
            sortYearStats();
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** Loads a player from a given player spec filename found in
	 * a given directory.
	 * @param playersDir directroy where the spec is found, usually
	 * the main players directory
	 * @param filename the filename of the player spec
	 */
    public void loadPlayer(File playersDir, String filename) throws IOException {
        loadPlayer(new File(playersDir, filename));
    }

    /** Loads a line from the biographical section of the play spec.
	 * Usually called repeatedly for each line in the biographical section.
	 * @param line a line in the bio section
	 */
    public void loadBiographical(String line) {
        String[] splitLine = line.split("=");
        if (splitLine.length < 2) return;
        String keyword = splitLine[0].trim();
        String value = splitLine[1].trim();
        try {
            if (keyword.equalsIgnoreCase(NAME)) {
                setNames(value);
            } else if (keyword.equalsIgnoreCase(TEAM)) {
                team = value;
            } else if (keyword.equalsIgnoreCase(POSITION)) {
                setSpecPositions(value);
                if (specPositionsIdx > 0) position = specPositions[0];
            } else if (keyword.equalsIgnoreCase(DEPTH)) {
                setDepth(value);
                setSpecDepth(depth);
            } else if (keyword.equalsIgnoreCase(HANDS)) {
                hands = Double.parseDouble(value);
            } else if (keyword.equalsIgnoreCase(FEET)) {
                feet = Double.parseDouble(value);
            } else if (keyword.equalsIgnoreCase(BUILD)) {
                build = Double.parseDouble(value);
            } else if (keyword.equalsIgnoreCase(SMARTS)) {
                smarts = Double.parseDouble(value);
            }
        } catch (NumberFormatException e) {
            String altText = "Sorry, but there appears to be a problem with the formatting" + NEWLINE + "of a number somewhere.  Please check the spec file, " + NEWLINE + spec.getName() + ", and reload the team." + NEWLINE;
            Txtfl.printlnLocal(altText);
        }
    }

    /** Loads a line of player statistics.
	 * The line should contain space-delimited stats for a given year.
	 * @param line line of player year stats
	 */
    public void loadStat(String line) {
        PlayerYearStat stat = new PlayerYearStat();
        String[] splitLine = line.split(" ");
        if (splitLine.length < 20) return;
        String statType = splitLine[0];
        stat.setGames(Integer.parseInt(splitLine[1]));
        stat.setPassAtt(Integer.parseInt(splitLine[2]));
        stat.setPassCmp(Integer.parseInt(splitLine[3]));
        stat.setPassYds(Integer.parseInt(splitLine[4]));
        stat.setPassTD(Integer.parseInt(splitLine[5]));
        stat.setPassInt(Integer.parseInt(splitLine[6]));
        stat.setReceiveAtt(Integer.parseInt(splitLine[7]));
        stat.setReceiveYds(Integer.parseInt(splitLine[8]));
        stat.setReceiveTD(Integer.parseInt(splitLine[9]));
        stat.setRunAtt(Integer.parseInt(splitLine[10]));
        stat.setRunYds(Integer.parseInt(splitLine[11]));
        stat.setRunTD(Integer.parseInt(splitLine[12]));
        stat.setBlockTk(Double.parseDouble(splitLine[13]));
        stat.setKickAtt(Integer.parseInt(splitLine[14]));
        stat.setKickYds(Integer.parseInt(splitLine[15]));
        stat.setKickFGAtt(Integer.parseInt(splitLine[16]));
        stat.setKickFG(Integer.parseInt(splitLine[17]));
        stat.setPuntAtt(Integer.parseInt(splitLine[18]));
        stat.setPuntYds(Integer.parseInt(splitLine[19]));
        if (!isTotal(statType)) {
            stat.setYear(Integer.parseInt(statType));
            yearStats[yearStatsIdx++] = stat;
        } else {
            stat.setYear(-1);
            totStats = stat;
        }
    }

    /**Rates the player, giving the player ability points based on 
	 * a comparison with average, generic values.
	 * In any category that the player has an exact 0 value, the player
	 * likely has no data for the category rather than extremely poor
	 * performance and therefore receives a 50 rating, the average
	 * ability value.
	*/
    public void ratePlayer() {
        double f = 0;
        pass = (int) ((f = totStats.getPassRating()) == 0 ? DEFAULT_RATING : f / 55 * DEFAULT_RATING * hands * smarts);
        receive = (int) ((f = totStats.getReceiveAvgYds()) == 0 ? DEFAULT_RATING : f / 11 * DEFAULT_RATING * hands);
        run = (int) ((f = totStats.getRunAvgYds()) == 0 ? DEFAULT_RATING : f / 4 * DEFAULT_RATING * feet);
        block = (int) ((f = totStats.getBlockAvgTk()) == 0 ? DEFAULT_RATING : f / 3.5 * DEFAULT_RATING * build);
        kickDist = (int) ((f = totStats.getKickAvgYds()) == 0 ? DEFAULT_RATING : f / 60 * DEFAULT_RATING * feet);
        kickWidth = (int) ((f = totStats.getKickFGPct()) == 0 ? DEFAULT_RATING : f / 75 * DEFAULT_RATING * smarts);
        punt = (int) ((f = totStats.getPuntAvgYds()) == 0 ? DEFAULT_RATING : f / 40 * DEFAULT_RATING * feet);
    }

    /**Updates the stored statistics for the current game.
	 * This player's statistics update with data from the current play.
	 * The data is assumed to be relevant to this specific player; for example,
	 * a <code>true</code> for the <code>success</code> parameter means
	 * that this player performed the give <code>playType</code> successfully.
	 * @param playType the type of play that the player performed
	 * @param success <code>true</code> if the player performed successfully
	 * for the play type, such as making a complete pass during a pass attempt
	 * @param yardage the yardage that the player advanced the ball, whether
	 * the distance that a passer throws the ball, the yards that a receiver runs it,
	 * or some other relevant distance
	 * @param result the specific outcome of the play, such as "interception"
	 * @param td <code>true</code> if this player scored a touchdown during
	 * the play
	*/
    public void updateCurrGameStats(String playType, boolean success, int yardage, String result, boolean td) {
        if (currGameStats.getGames() < 1) currGameStats.incrementGames();
        if (playType.equalsIgnoreCase(LibTxtfl.PASS)) {
            currGameStats.incrementPassAtt();
            if (success) {
                currGameStats.incrementPassCmp();
                currGameStats.addPassYds(yardage);
            }
            if (result.equalsIgnoreCase(LibTxtfl.INTERCEPTION)) currGameStats.incrementPassInt();
            if (td) currGameStats.incrementPassTD();
        } else if (playType.equalsIgnoreCase(LibTxtfl.RECEPTION)) {
            currGameStats.incrementReceiveAtt();
            currGameStats.addReceiveYds(yardage);
            if (td) currGameStats.incrementReceiveTD();
        } else if (playType.equalsIgnoreCase(LibTxtfl.RUSH)) {
            currGameStats.incrementRunAtt();
            currGameStats.addRunYds(yardage);
            if (td) currGameStats.incrementRunTD();
        } else if (LibTxtfl.isPuntType(playType)) {
            currGameStats.incrementPuntAtt();
            currGameStats.addPuntYds(yardage);
        } else if (LibTxtfl.isKickOffType(playType)) {
            currGameStats.incrementKickAtt();
            currGameStats.addKickYds(yardage);
        } else if (playType.equalsIgnoreCase(LibTxtfl.FIELD_GOAL)) {
            currGameStats.incrementKickFGAtt();
            if (success) currGameStats.incrementKickFG();
        }
    }

    /**Merges the stats for the current game with the stats for the current
	 * year.
	 * @param year the year, in <code>yyyy</code> format
	*/
    public void mergeCurrStats(int year) {
        PlayerYearStat stat = null;
        if (yearStatsIdx - 1 > 0) {
            stat = yearStats[yearStatsIdx - 1];
        }
        if (stat == null || stat.getYear() != year) {
            if (yearStatsIdx >= yearStats.length) {
                yearStats = (PlayerYearStat[]) LibTxtfl.growArray(yearStats);
            }
            stat = yearStats[yearStatsIdx++] = new PlayerYearStat(year);
        }
        mergeStats(stat, currGameStats);
    }

    /**Merges stats from one year with that from another.
	 * @param summedStat total stats for a given year
	 * @param statToAdd values to add to the <code>summedStats</code>
	*/
    public PlayerYearStat mergeStats(PlayerYearStat summedStat, PlayerYearStat statToAdd) {
        return mergeStats(summedStat, statToAdd, false);
    }

    /** Merges stats from two players or two versions of the same player.
	 * Stats are only merged if the years are the same, or one of the years os "0".
	 * If the onlyIsNew flag is set to true, each parameter will only be added if the
	 * parameter equals 0 to prevent summing stats that aren't meant to be cumulative
	 * between two snapshots of the same year.  For example, when updating stats
	 * between games, the final rushing stats for the previous game may have been
	 * 500 yds, and 514 yds for the next game--stats that should not be summed.
	 * This function is thus most useful for adding new stats, such as RB rushing
	 * and RB receiving stats.  Note that the year in the summedStat will be change
	 * to that of the statToAdd if the summedStat has a year of 0.
	 * @param summedStat the final, summed stat
	 * @param statToAdd the stat to add to summedStat to create a summation of stats
	 * @param onlyIfNew true if each parameter within the year should only be added
	 * if the parameter is equal to 0
	 * @return the merged player year stat
	 */
    public static PlayerYearStat mergeStats(PlayerYearStat summedStat, PlayerYearStat statToAdd, boolean onlyIfNew) {
        int summedStatYr = summedStat.getYear();
        int statToAddYr = statToAdd.getYear();
        if (summedStatYr == statToAddYr || summedStatYr == 0 || statToAddYr == 0) {
            if (summedStatYr == 0) {
                summedStat.setYear(statToAddYr);
            }
            if (summedStat.getGames() == 0 || !onlyIfNew) summedStat.addGames(statToAdd.getGames());
            if (summedStat.getPassAtt() == 0 || !onlyIfNew) summedStat.addPassAtt(statToAdd.getPassAtt());
            if (summedStat.getPassCmp() == 0 || !onlyIfNew) summedStat.addPassCmp(statToAdd.getPassCmp());
            if (summedStat.getPassYds() == 0 || !onlyIfNew) summedStat.addPassYds(statToAdd.getPassYds());
            if (summedStat.getPassTD() == 0 || !onlyIfNew) summedStat.addPassTD(statToAdd.getPassTD());
            if (summedStat.getPassInt() == 0 || !onlyIfNew) summedStat.addPassInt(statToAdd.getPassInt());
            if (summedStat.getReceiveAtt() == 0 || !onlyIfNew) summedStat.addReceiveAtt(statToAdd.getReceiveAtt());
            if (summedStat.getReceiveYds() == 0 || !onlyIfNew) summedStat.addReceiveYds(statToAdd.getReceiveYds());
            if (summedStat.getReceiveTD() == 0 || !onlyIfNew) summedStat.addReceiveTD(statToAdd.getReceiveTD());
            if (summedStat.getRunAtt() == 0 || !onlyIfNew) summedStat.addRunAtt(statToAdd.getRunAtt());
            if (summedStat.getRunYds() == 0 || !onlyIfNew) summedStat.addRunYds(statToAdd.getRunYds());
            if (summedStat.getRunTD() == 0 || !onlyIfNew) summedStat.addRunTD(statToAdd.getRunTD());
            if (summedStat.getBlockTk() == 0 || !onlyIfNew) summedStat.addBlockTk(statToAdd.getBlockTk());
            if (summedStat.getKickAtt() == 0 || !onlyIfNew) summedStat.addKickAtt(statToAdd.getKickAtt());
            if (summedStat.getKickYds() == 0 || !onlyIfNew) summedStat.addKickYds(statToAdd.getKickYds());
            if (summedStat.getKickFGAtt() == 0 || !onlyIfNew) summedStat.addKickFGAtt(statToAdd.getKickFGAtt());
            if (summedStat.getKickFG() == 0 || !onlyIfNew) summedStat.addKickFG(statToAdd.getKickFG());
            if (summedStat.getPuntAtt() == 0 || !onlyIfNew) summedStat.addPuntAtt(statToAdd.getPuntAtt());
            if (summedStat.getPuntYds() == 0 || !onlyIfNew) summedStat.addPuntYds(statToAdd.getPuntYds());
        }
        return summedStat;
    }

    /** Merges two players, usually two different time points of the same player.
	 * The latest player is used for the default values, except for the positions,
	 * where the original player's positions take precedence since it's assumed
	 * that the original player had the more up-to-date position assignments, while
	 * the latest player maybe is being merged to incorporate a new set of
	 * stats for a less-utilized position.
	 * @param origPlayer the original, often older time-point of the player
	 * @param latestPlayer the final, often new time-point of the player
	 */
    public static void mergePlayers(Player origPlayer, Player latestPlayer) {
        mergeStats(latestPlayer.getLatestYearStat(), origPlayer.getLatestYearStat(), true);
        String[] latestPositions = latestPlayer.getSpecPositions();
        for (int i = 0; i < latestPositions.length; i++) {
            origPlayer.addSpecPosition(latestPositions[i]);
        }
        latestPlayer.setSpecPositions(origPlayer.getSpecPositions());
    }

    /**Writes the player spec from the latest player data, assuming the
	 * spec file to be the same as the player's currently set file.
	*/
    public void writeUpdatedSpec() throws IOException {
        writeUpdatedSpec(spec);
    }

    /**Writes the player spec from the latest player data to the given
	 * file location.
	 * Assumes that one and only one line of stats exists in the template.
	 * @param f file spec location, whether already created or not
	*/
    public void writeUpdatedSpec(File f) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(f), true);
        File draftDir = Draft.getDraftDir();
        File templateFile = new File(draftDir, FILENAME_TEMPLATE_PLAYER);
        BufferedReader templateReader = LibTxtfl.openFile(templateFile);
        String line = "";
        String[] splitLine = null;
        String currSection = "";
        while ((line = templateReader.readLine()) != null) {
            line = line.trim();
            if (line.indexOf(LibTxtfl.SECTION) == 0) {
                splitLine = line.split(" ");
                if (splitLine.length > 1) {
                    currSection = splitLine[1];
                    writer.println(line);
                }
            } else if (line.equals("") || line.indexOf("#") == 0) {
                writer.println(line);
            } else if (currSection.equalsIgnoreCase(BIOGRAPHICAL)) {
                writeBiographical(line, writer);
            } else if (currSection.equalsIgnoreCase(STATS)) {
                writeStats(writer);
            } else if (currSection.equalsIgnoreCase(LibTxtfl.VERSION)) {
                LibTxtfl.writeVersion(line, writer);
            }
        }
        writer.close();
    }

    /** Writes biographical information.
	 * @param line a template line of bio info
	 * @param writer output writer
	 */
    public void writeBiographical(String line, PrintWriter writer) {
        String[] splitLine = line.split("=");
        if (splitLine.length < 1) return;
        String keyword = splitLine[0].trim();
        String value = splitLine[1].trim();
        writer.print(keyword + "=");
        if (keyword.equalsIgnoreCase(NAME)) {
            writer.println(firstName + " " + middleName + " " + lastName);
        } else if (keyword.equalsIgnoreCase(TEAM)) {
            writer.println(team);
        } else if (keyword.equalsIgnoreCase(POSITION)) {
            writer.println(getSpecPositionsString());
        } else if (keyword.equalsIgnoreCase(DEPTH)) {
            writer.println(specDepth + "");
        } else if (keyword.equalsIgnoreCase(HANDS)) {
            writer.println(hands + "");
        } else if (keyword.equalsIgnoreCase(FEET)) {
            writer.println(feet + "");
        } else if (keyword.equalsIgnoreCase(BUILD)) {
            writer.println(build + "");
        } else if (keyword.equalsIgnoreCase(SMARTS)) {
            writer.println(smarts + "");
        }
    }

    /** Writes all player year stats.
	 * @param writer output writer
	 */
    public void writeStats(PrintWriter writer) {
        PlayerYearStat newTotStats = new PlayerYearStat();
        for (int i = 0; i < yearStatsIdx; i++) {
            writer.println(yearStats[i].getSummary(false));
            mergeStats(newTotStats, yearStats[i]);
        }
        writer.println(newTotStats.getSummary(true));
    }

    /**Creates the working player name, which is the first and last name,
	 * in that order, even if they do not exist (a "-").
	*/
    public void createName() {
        name = (firstName + " " + lastName).trim();
    }

    /** Sorts the roster alphabetically by name.
	Assumes that only one of any given name exists in the list; 
	otherwise, the multiple players with a name will be randomly sorted.
     */
    public static void sortPlayers(Player[] roster, int len) {
        int start = 0;
        int end = 0;
        int gap = 0;
        int n = len;
        Player tmp = null;
        for (gap = n / 2; gap > 0; gap /= 2) {
            for (end = gap; end < n; end++) {
                for (start = end - gap; start >= 0 && (roster[start].getName().compareToIgnoreCase(roster[start + gap].getName())) > 0; start -= gap) {
                    tmp = roster[start];
                    roster[start] = roster[start + gap];
                    roster[start + gap] = tmp;
                }
            }
        }
    }

    /** Gets the player year stats for the given year.
	 * @param players an array of players, assumed to be sorted alphabetically
	 * by full name, first name first
	 * @param len the number of elements to be searched in the array
	 * @param name the full name of the player to be found
	 * @return the index of the year stats for the given year
	 */
    public static int getPlayerIndex(Player[] players, int len, String name) {
        int start = 0;
        int end = len - 1;
        int mid = end / 2;
        int found = -1;
        String s = "";
        while (start <= end && found == -1) {
            if ((s = players[mid].getName()).equalsIgnoreCase(name)) {
                found = mid;
            } else if (name.compareToIgnoreCase(s) < 0) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
            mid = (start + end) / 2;
        }
        return found;
    }

    /** Sorts the year stats by year.
	 */
    public void sortYearStats() {
        int start = 0;
        int end = 0;
        int gap = 0;
        int n = yearStatsIdx;
        PlayerYearStat tmp = null;
        for (gap = n / 2; gap > 0; gap /= 2) {
            for (end = gap; end < n; end++) {
                for (start = end - gap; start >= 0 && yearStats[start].getYear() > yearStats[start + gap].getYear(); start -= gap) {
                    tmp = yearStats[start];
                    yearStats[start] = yearStats[start + gap];
                    yearStats[start + gap] = tmp;
                }
            }
        }
    }

    /** Gets the player year stats for the given year.
	 * @param year year of stats to retreive
	 * @return the index of the year stats for the given year
	 */
    public int getYearStatIndex(int year) {
        int start = 0;
        int end = yearStatsIdx - 1;
        int mid = end / 2;
        int found = -1;
        int n = -1;
        while (start <= end && found == -1) {
            if ((n = yearStats[mid].getYear()) == year) {
                found = mid;
            } else if (year < n) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
            mid = (start + end) / 2;
        }
        return found;
    }

    /** Adds a year stat to the array of stats.
	 * @param stat stat to add
	 * @param merge true if stats of the same year should be merged
	 */
    public void addYearStat(PlayerYearStat stat, boolean merge) {
        int foundYearStatsIdx = -1;
        if ((foundYearStatsIdx = getYearStatIndex(stat.getYear())) != -1) {
            yearStats[foundYearStatsIdx] = merge ? mergeStats(yearStats[foundYearStatsIdx], stat) : stat;
        } else {
            yearStats = (PlayerYearStat[]) LibTxtfl.addArrayElement(yearStats, stat, yearStatsIdx++);
            sortYearStats();
        }
    }

    /** Sorts an array of players by depth, from first to last rank.
	 */
    public static Player[] sortByDepth(Player[] players, int len) {
        int start = 0;
        int end = 0;
        int gap = 0;
        int n = len;
        Player tmp = null;
        for (gap = n / 2; gap > 0; gap /= 2) {
            for (end = gap; end < n; end++) {
                for (start = end - gap; start >= 0 && players[start].getDepth() > players[start + gap].getDepth(); start -= gap) {
                    tmp = players[start];
                    players[start] = players[start + gap];
                    players[start + gap] = tmp;
                }
            }
        }
        return players;
    }

    /**Checks if this player has been assigned as a starter.
	 * Starters by default play their position from the game's outset.
	 * A starter is defined as a player who has a depth of "1".  The priority
	 * order of depth assignments is player spec < team spec < game play
	 * (eg from {@link Team#fillWithGeneralizedPositions }.  The spec does
	 * not have a "starter" field, only a "depth" field.
	 * @return <code>true</code> if the player is a starter
	*/
    public boolean isStarter() {
        return depth == STARTER_DEPTH;
    }

    /**Checks if the given field indicates that the statistic is a total, the 
	 * sum of previous statistics, such as the total values for the player's
	 * yearly history.
	 * @param s string to test for "total" indication
	 * @return <code>true</code> if the string reads, "total", case ignored
	*/
    public boolean isTotal(String s) {
        return s.equalsIgnoreCase("total");
    }

    public static boolean isPosition(String cat) {
        return (cat.equalsIgnoreCase(POSITION) || cat.equalsIgnoreCase(POSITION_ALT));
    }

    /** Checks whether the player file has been written.
	 * @return true if the player spec file has been written
	 */
    public boolean isWritten() {
        return written;
    }

    /** Gets the player file specification.
		 * @return the file specifying the player.
		 */
    public File getSpec() {
        return spec;
    }

    /** Gets the player's name, which consists of the first followed by the 
	 * last name, separated by a space.
	@return player's name
    */
    public String getName() {
        return name;
    }

    /** Gets the first name of the player.
	 * @return first name
	 */
    public String getFirstName() {
        return firstName;
    }

    /** Gets the last name of the player.
	 * @return last name of the player
	 */
    public String getLastName() {
        return lastName;
    }

    /** Gets the name of the player's team.
	 * @return the name of the team
	 */
    public String getTeam() {
        return team;
    }

    /** Gets the player's position.
	* Positions include:
	* <p><i>Offense:</i>
	* <br>QB (quarterback), RB (running back), FB (fullback)
	* <br>RWR (right wide receiver), LWR (left wide receiver)
	 * <br>RTE (right tight end), LTE (left tight end)
	 * <br>RT (right tackle), LT (left tackle)
	* <br>RG (right guard), LG (left guard)
	* <br>C (center)
	* 
	* <p><i>Defense:</i>
	* <br>RCB (right corner back), LCB (left corner back)
	* <br>RDE (right defensive end), LDE (left defensive end)
	* <br>RDT (rigth defensive tackle), LDT (left defensive tackle)
	* <br>NT (nose tackle)
	* <br>FS (free safety), SS (strong safety)
	*@return the player's position
    */
    public String getPosition() {
        return position;
    }

    /** Gets the current player's pass ability.
	@return pass ability
    */
    public int getPass() {
        return pass;
    }

    /** Gets the current player's pass ability.
	@return pass ability
    */
    public int getReceive() {
        return receive;
    }

    /** Gets the current player's catch ability.
	@return catch ability
    */
    public int getRun() {
        return run;
    }

    /** Gets the current player's blocking ability.
	@return blocking ability
    */
    public int getBlock() {
        return block;
    }

    /** Gets the current player's kickDist ability.
	@return kickDist ability
    */
    public int getKickDist() {
        return kickDist;
    }

    /** Gets the current player's KickWidth/field-goal ability.
	@return KickWidth/field-goal ability
    */
    public int getKickWidth() {
        return kickWidth;
    }

    public int getPunt() {
        return punt;
    }

    /** Gets the player's depth order rank.
	 * @return the depth order
	 */
    public int getDepth() {
        return depth;
    }

    public int getSpecDepth() {
        return specDepth;
    }

    /** Gets the player's latest year stat.
	 * @return the stat from the latest year the player played;
	 * if no stats are available, returns an empty player year stat
	 */
    public PlayerYearStat getLatestYearStat() {
        if (yearStatsIdx > 0) return yearStats[yearStatsIdx - 1];
        return new PlayerYearStat();
    }

    /** Gets the spec positions. 
	 * @return the spec positions an array the length of the total number of
	 * spec positions.
	 */
    public String[] getSpecPositions() {
        return (String[]) LibTxtfl.truncateArray(specPositions, specPositionsIdx);
    }

    /** Gets the spec positions as a single string.
	 * @return the spec positions as a single, space-delimited string
	 */
    public String getSpecPositionsString() {
        String positions = "";
        for (int i = 0; i < specPositionsIdx; i++) {
            positions += specPositions[i] + " ";
        }
        return positions.trim();
    }

    /** Gets a spec position at the given index, in lower case.
	 * @return the spec position, all lower case
	 */
    public String getSpecPosition(int i) {
        if (i < specPositionsIdx && i >= 0 && specPositions[i] != null) return specPositions[i].toLowerCase();
        return "";
    }

    /** Gets the player hand skill value.
	 * @return hand skills, where 1.0 is average
	 */
    public double getHands() {
        return hands;
    }

    /** Gets the player feet skill value.
	 * @return feet skills, where 1.0 is average
	 */
    public double getFeet() {
        return feet;
    }

    /** Gets the player build skill value.
	 * @return build skills, where 1.0 is average
	 */
    public double getBuild() {
        return build;
    }

    /** Gets the player smarts skill value.
	 * @return hand smarts, where 1.0 is average
	 */
    public double getSmarts() {
        return smarts;
    }

    public PlayerYearStat[] getYearStats() {
        return yearStats;
    }

    public int getYear() {
        return year;
    }

    public int getPay() {
        return pay;
    }

    public int getID() {
        return id;
    }

    public int getHealth() {
        return health;
    }

    public PlayerYearStat getCurrGameStats() {
        return currGameStats;
    }

    public PlayerYearStat getTotStats() {
        return totStats;
    }

    public String printSummary() {
        String summary = "name: " + name + ", hands: " + hands + ", feet: " + feet + ", build: " + build + ", smarts: " + smarts + ", year: " + year + ", pay: " + pay + ", id: " + id + ", position: " + position + ", depth: " + depth;
        System.out.println(summary);
        return summary;
    }

    /** Sets the player's name
	@param aName name to set player to
    */
    public void setName(String aName) {
        name = aName;
    }

    /** Parses and sets the first, middle, and last names from
	 * a space-delimited line.
	 * If no words are provided, the default first name is "default".
	 * The last word is taken to be the last name.  Any words 
	 * in-between are taken to be the middle name, including
	 * spaces between them.  The "name" of the player is set to 
	 * be the first and last names, separated by a space.
	 * @param line space-delimited line of the player's names
	 */
    public void setNames(String line) {
        String[] splitName = line.split(" ");
        if (splitName.length < 1) name = "Default";
        firstName = splitName[0];
        for (int i = 1; i < splitName.length - 1; i++) {
            middleName = splitName[i] + " ";
        }
        middleName = middleName.trim();
        lastName = splitName[splitName.length - 1];
        name = firstName + " " + lastName;
    }

    /** Sets the player's positon.
	@param aPosition position to set player to
	@see #getPosition()
    */
    public void setPosition(String aPosition) {
        position = aPosition;
    }

    /** Sets the player's pass ability.
	@param aPass pass ability
    */
    public void setPass(int aPass) {
        pass = aPass;
    }

    /** Sets the player's catch ability.
	@param aReceive catch ability
    */
    public void setReceive(int aReceive) {
        receive = aReceive;
    }

    /** Sets the player's run ability.
	@param aRun run ability
    */
    public void setRun(int aRun) {
        run = aRun;
    }

    /** Sets the player's block ability.
	@param aBlock block ability
    */
    public void setBlock(int aBlock) {
        block = aBlock;
    }

    /** Sets the player's kickDist ability.
	@param aKickDist kickDist ability
    */
    public void setKickDist(int aKickDist) {
        kickDist = aKickDist;
    }

    /** Sets the player's KickWidth/field-goal ability.
	@param aKickWidth KickWidth/field-goal ability
    */
    public void setKickWidth(int aKickWidth) {
        kickWidth = aKickWidth;
    }

    /** Sets the player's punt ability.
	@param aPunt punt ability
    */
    public void setPunt(int aPunt) {
        punt = aPunt;
    }

    /**Sets the spec file.
	 * @param f the new spec file location
	*/
    public void setSpec(File f) {
        spec = f;
    }

    public void setWritten(boolean b) {
        written = b;
    }

    /**Sets the total stats, the summation of the player's yearly stats.
	 * @param t the stats
	*/
    public void setTotStats(PlayerYearStat t) {
        totStats = t;
    }

    /**Sets the player's first name.
	 * If the first name has any spaces, underscores are assumed to substitute
	 * for them.
	 * @param s the first name
	*/
    public void setFirstName(String s) {
        firstName = s;
    }

    /**Sets the player's middle name.
	 * This name can have spaces, but for clarity they should be substituted
	 * with underscores.
	 * @param s the middle name
	*/
    public void setMiddleName(String s) {
        middleName = s;
    }

    /**Sets the player's last name.
	 * If the first name has any spaces, underscores are assumed to substitute
	 * for them.
	 * @param s the last name
	*/
    public void setLastName(String s) {
        lastName = s;
    }

    /**Sets the player's hometown.
	 * This name can have spaces.
	 * @param s the name of the player's hometown
	*/
    public void setTeam(String s) {
        team = s;
    }

    /**Sets the indicator for the player's starter position.
	 * @param b if <code>true</code> the player is considered
	 * the default starter player for the position
	*/
    public void setStarter(boolean b) {
        starter = b;
    }

    /**Sets whether the player is a starter according to the given value.
	 * @param n the depth order, where 1 is "first string," the highest
	 * priority position, 2 is "second string," the next highest, and so on.
	*/
    public void setDepth(int n) {
        depth = n;
    }

    /**Sets whether the player is a starter according to the given value.
	 * @param s the depth order, where "1" is "first string," the highest
	 * priority position, "2" is "second string," the next highest, and so on.
	*/
    public void setDepth(String s) {
        depth = Integer.parseInt(s);
    }

    /**Sets the depth order according to the player spec file, regardless
	 * of any overrides from the team spec file.
	 * @param n the depth order, where 1 is "first string," the highest
	 * priority position, 2 is "second string," the next highest, and so on.
	*/
    public void setSpecDepth(int n) {
        specDepth = n;
    }

    /**Sets the depth order according to the player spec file, regardless
	 * of any overrides from the team spec file.
	 * @param s the depth order, where "1" is "first string," the highest
	 * priority position, "2" is "second string," the next highest, and so on.
	*/
    public void setSpecDepth(String s) {
        specDepth = Integer.parseInt(s);
    }

    /**Sets position according to the player spec file, regardless
	 * of any overrides from the team spec file.
	 * @param s the position, such as "wr", "qb", etc.
	*/
    public void setSpecPosition(String s) {
        specPosition = s;
    }

    public void addSpecPosition(String s) {
        if (s == null || s.equals("")) return;
        char endChar = s.charAt(s.length() - 1);
        if (Character.isDigit(endChar)) {
            s = s.substring(0, s.length() - 1);
            setSpecDepth(Character.toString(endChar));
        }
        int i = 0;
        while (i < specPositionsIdx && specPositions[i] != null && !specPositions[i].equalsIgnoreCase(s)) {
            i++;
        }
        if (i >= specPositionsIdx) specPositionsIdx++;
        if (specPositionsIdx >= specPositions.length) {
            specPositions = (String[]) LibTxtfl.growArray(specPositions);
        }
        specPositions[i] = s;
    }

    public void setSpecPositions(String line) {
        specPositions = line.split(" ");
        specPositionsIdx = specPositions.length;
    }

    public void setSpecPositions(String[] aSpecPositions) {
        specPositions = aSpecPositions;
        specPositionsIdx = specPositions.length;
    }

    /** Sets the player hand skills.
	 * @param aHands hand skills, where 1.0 is average
	 */
    public void setHands(double aHands) {
        hands = aHands;
    }

    /** Sets the player feet skills.
	 * @param aFeet feet skills, where 1.0 is average
	 */
    public void setFeet(double aFeet) {
        feet = aFeet;
    }

    /** Sets the player build skills.
	 * @param aBuild hand build, where 1.0 is average
	 */
    public void setBuild(double aBuild) {
        build = aBuild;
    }

    /** Sets the player smarts skills.
	 * @param aSmarts hand smarts, where 1.0 is average
	 */
    public void setSmarts(double aSmarts) {
        smarts = aSmarts;
    }

    public void setYear(int aYear) {
        year = aYear;
    }

    public void setPay(int aPay) {
        pay = aPay;
    }

    public void setID(int aID) {
        id = aID;
    }

    public void setHealth(int aHealth) {
        health = aHealth;
    }

    public void setCurrGameStats(PlayerYearStat aCurrGameStats) {
        currGameStats = aCurrGameStats;
    }

    /** Clones the current object of this class.
	@return an object clone
    */
    public Object clone() {
        try {
            Player cloned = (Player) super.clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
