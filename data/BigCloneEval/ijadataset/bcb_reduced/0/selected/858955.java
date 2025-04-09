package com.textflex.txtfl;

import java.io.*;

/**The statistics storer for a given type of play.
 * For example, this class groups together all the players who have made a pass
 * attempt and shows their resulting statistics involved in the pass.
 */
public class PlayTypeStat implements Cloneable, Serializable {

    private int season = -1;

    private String playType = "";

    private String team = "";

    private PlayTypePlayerStat[] players = new PlayTypePlayerStat[10];

    private int playersIdx = 0;

    /** Lists the players in a given play type statistic.
	 * @return list of players, delimited by commas
	 */
    public String listPlayers() {
        String s = "";
        for (int i = 0; i < playersIdx; i++) {
            s += players[i].getPlayer() + ", ";
        }
        return s;
    }

    /**Constructs a storage array for a given play type.
	 * @param aPlayType the type of play that this storage object holds
	*/
    public PlayTypeStat(String aPlayType) {
        playType = aPlayType;
    }

    /**Constructs a storage array for a given play type and team
	 * @param aPlayType the type of play that this storage object holds
	 * @param aTeam the team whose play statistics this object holds
	*/
    public PlayTypeStat(String aPlayType, String aTeam) {
        this(aPlayType);
        team = aTeam;
    }

    /**Adds a statistic for a player.
	 * If the player does not have a storage spot, it is created.
	 * @param player the name of the player to add.  Names conform to the
	 * <code>Player.getPlayer()</code> return value.
	 * @param attempts the total number of attempts that the player
	 * has made for the given type of play
	 * @param successes the total number of success that the player has
	 * made for this type of play
	 * @param yards the total yardage that the player has gained in this
	 * type of play
	*/
    public void addPlayerStat(String player, int attempts, int successes, int yards) {
        if (player == null || player.equals("")) return;
        int playerIdx = -1;
        if ((playerIdx = getPlayersIdx(player)) == -1) {
            if (playersIdx >= players.length) players = (PlayTypePlayerStat[]) LibTxtfl.growArray(players);
            players[playersIdx++] = new PlayTypePlayerStat(player, attempts, successes, yards);
            sortPlayers();
        } else {
            players[playerIdx].update(attempts, successes, yards);
        }
    }

    /**Adds one play's statistics for a player.
	 * @param player the player who completed this type of play
	 * @param successful <code>true</code> if the player was successful
	 * in the play
	 * @param yards the yardage that the player gained during this play
	*/
    public void addPlayerStat(String player, boolean successful, int yards) {
        addPlayerStat(player, 1, successful ? 1 : 0, yards);
    }

    /**Merges the players and their data from one play type stat with the players
	 * from this stat.
	*/
    public void addPlayerStats(PlayTypePlayerStat[] stats) {
        for (int i = 0; i < stats.length && stats[i] != null; i++) {
            addPlayerStat(stats[i].getPlayer(), stats[i].getAttempts(), stats[i].getSuccesses(), stats[i].getYards());
        }
    }

    /**Increases the touchdowns for the given player by one.
	 * @param player the player with the value will increase
	*/
    public void incrementTouchdowns(String player) {
        int i = getPlayersIdx(player);
        if (i != -1) {
            players[getPlayersIdx(player)].incrementTouchdowns();
        } else {
            System.out.println("Player " + player + " not found");
        }
    }

    /**Increases the interceptions for the given player by one.
	 * @param player the player with the value will increase
	*/
    public void incrementInterceptions(String player) {
        int i = getPlayersIdx(player);
        if (i != -1) {
            players[getPlayersIdx(player)].incrementInterceptions();
        } else {
            System.out.println("Player " + player + " not found");
        }
    }

    /**Increases the sacks for the given player by one.
	 * @param player the player with the value will increase
	*/
    public void incrementSacks(String player) {
        int i = getPlayersIdx(player);
        if (i != -1) {
            players[getPlayersIdx(player)].incrementSacks();
        } else {
            System.out.println("Player " + player + " not found");
        }
    }

    /**Increases the fumbles for the given player by one.
	 * @param player the player with the value will increase
	*/
    public void incrementFumbles(String player) {
        int i = getPlayersIdx(player);
        if (i != -1) {
            players[getPlayersIdx(player)].incrementFumbles();
        } else {
            System.out.println("Player " + player + " not found");
        }
    }

    /**Increases the touchdowns for the given player by one.
	 * @param player the player with the value will increase
	*/
    public void incrementFumblesLost(String player) {
        int i = getPlayersIdx(player);
        if (i != -1) {
            players[getPlayersIdx(player)].incrementFumblesLost();
        } else {
            System.out.println("Player " + player + " not found");
        }
    }

    /**Sorts the players by name, as given by the <code>Player.getName()
	 * return value.
	*/
    public void sortPlayers() {
        int start = 0;
        int end = 0;
        int gap = 0;
        int n = playersIdx;
        PlayTypePlayerStat tmp = null;
        for (gap = n / 2; gap > 0; gap /= 2) {
            for (end = gap; end < n; end++) {
                for (start = end - gap; start >= 0 && (players[start].getPlayer().compareToIgnoreCase(players[start + gap].getPlayer())) > 0; start -= gap) {
                    tmp = players[start];
                    players[start] = players[start + gap];
                    players[start + gap] = tmp;
                }
            }
        }
    }

    /** Clones the object in compliance with the Cloneable interface.
	@return this object as a clone
    */
    public Object clone() {
        try {
            PlayTypeStat cloned = (PlayTypeStat) super.clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**Sets the play type, such as "pass" or "fumble".
	 * @param aPlayType the type of play
	*/
    public void setPlayType(String aPlayType) {
        playType = aPlayType;
    }

    /**Gets the play type.
	 * @return the type of play
	*/
    public String getPlayType() {
        return playType;
    }

    /**Gets the array of players from the given team who have generated a
	 * statistic for this type of play.
	 * Each element of the array consists of the player's name as well as the
	 * player's stastics for the given play type.
	 * @return array of players and their stats for this type of play
	*/
    public PlayTypePlayerStat[] getPlayers() {
        return (PlayTypePlayerStat[]) players.clone();
    }

    /**Gets the index of the player in this play type's player array.
	 * @param player the name of the player, as given by the player's
	 * <code>Player.getName()</code> return value, case ignored
	 * @return the index of the player, or -1 if the player is not in the array
	*/
    public int getPlayersIdx(String player) {
        if (player == null || player == "") return -1;
        int start = 0;
        int end = playersIdx - 1;
        int mid = end / 2;
        int found = -1;
        String s = "";
        while (start <= end && found == -1) {
            if ((s = players[mid].getPlayer()).equalsIgnoreCase(player)) {
                found = mid;
            } else if (player.compareToIgnoreCase(s) < 0) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
            mid = (start + end) / 2;
        }
        return found;
    }
}

/**Storage class for player statistics in a given type of play.
 * <code>PlayTypeStat</code> uses objects from this class to
 * store player data.
*/
class PlayTypePlayerStat implements Cloneable, Serializable {

    private String player = "";

    private int attempts = 0;

    private int successes = 0;

    private int yards = 0;

    private int touchdowns = 0;

    private int interceptions = 0;

    private int sacks = 0;

    private int fumbles = 0;

    private int fumblesLost = 0;

    /**Constructs a storage object for a given player in a particular,
	 * unspecified type of play.
	 * @param aPlayer the name of the player, as given by the return
	 * value of <code>Player.getName()</code>
	 * @param aAttempts the number of attempts the player made
	 * @param aSuccesses number of sucessful attempts
	 * @param aYards yardage gained
	*/
    public PlayTypePlayerStat(String aPlayer, int aAttempts, int aSuccesses, int aYards) {
        player = aPlayer;
        attempts = aAttempts;
        successes = aSuccesses;
        yards = aYards;
    }

    /**Constructs a storage object for a given player in a particular,
	 * unspecified type of play.
	 * Assumes that that player has made his or her first attempt at this
	 * play type.
	 * @param aPlayer the name of the player, as given by the return
	 * value of <code>Player.getName()</code>
	 * @param isSuccessful <code>true</code> if the player completed
	 * this type of play successfully
	 * @param aYards yardage gained
	*/
    public PlayTypePlayerStat(String aPlayer, boolean aSuccessful, int aYards) {
        player = aPlayer;
        attempts++;
        if (aSuccessful) successes++;
        yards = aYards;
    }

    /**Updates the player's statistics in this storage object.
	 * @param aAttempts the number of attempts to add to the current
	 * value
	 * @param aSuccesses the number of successful attempts to add
	 * to the current value
	*/
    public void update(int aAttempts, int aSuccesses, int aYards) {
        attempts += aAttempts;
        successes += aSuccesses;
        yards += aYards;
    }

    /**Updates the player's statistics in this storage object.
	 * @param aAttempts the number of attempts to add to the current
	 * value
	 * @param aSuccesses the number of successful attempts to add
	 * to the current value
	 * @param aYards the number of yards to add to the current value
	*/
    public void update(boolean aAttempts, boolean aSuccesses, int aYards) {
        if (aAttempts) attempts++;
        if (aSuccesses) successes++;
        yards += aYards;
    }

    /**Updates the player's statistics in this storage object.
	 * @param aSuccesses the number of successful attempts to add
	 * to the current value
	*/
    public void update(int aYards) {
        attempts++;
        yards += aYards;
    }

    /** Clones the object in compliance with the Cloneable interface.
	@return this object as a clone
    */
    public Object clone() {
        try {
            PlayTypePlayerStat cloned = (PlayTypePlayerStat) super.clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**Increases the number of attempts by one.
	*/
    public void incrementAttempts() {
        attempts++;
    }

    /**Increases the number of success by one.
	*/
    public void incrementSuccesses() {
        successes++;
    }

    /**Increases the number of yards by one.
	 * Not a particularly useful function...
	*/
    public void incrementYards() {
        yards++;
    }

    /**Increases the number of touchdowns by one.
	*/
    public void incrementTouchdowns() {
        touchdowns++;
    }

    /**Increases the number of interceptions by one.
	*/
    public void incrementInterceptions() {
        interceptions++;
    }

    /**Increases the number of sacks by one.
	*/
    public void incrementSacks() {
        sacks++;
    }

    /**Increases the number of fumbles by one.
	*/
    public void incrementFumbles() {
        fumbles++;
    }

    /**Increases the number of fumbles lost by one.
	*/
    public void incrementFumblesLost() {
        fumblesLost++;
    }

    /**Sets the number of attempts that the player has made for 
	 * the set type of play.
	 * @param aAttempts number of attempts
	*/
    public void setAttempts(int aAttempts) {
        attempts = aAttempts;
    }

    /**Sets the number of successes that the player has made for 
	 * the set type of play.
	 * @param aSuccesses number of successes
	*/
    public void setSuccesses(int aSuccesses) {
        successes = aSuccesses;
    }

    /**Sets the number of yards that the player has made for 
	 * the set type of play.
	 * @param aYards number of yards
	*/
    public void setYards(int aYards) {
        yards = aYards;
    }

    /**Sets the number of touchdowns that the player has made for 
	 * the set type of play.
	 * @param aTouchdowns number of touchdowns
	*/
    public void setTouchdowns(int aTouchdowns) {
        touchdowns = aTouchdowns;
    }

    /**Sets the number of interceptions that the player has made for 
	 * the set type of play.
	 * @param aInterceptions number of interceptions
	*/
    public void setInterceptions(int aInterceptions) {
        interceptions = aInterceptions;
    }

    /**Sets the number of sacks that the player has made for 
	 * the set type of play.
	 * @param aSacks number of sacks
	*/
    public void setSacks(int aSacks) {
        sacks = aSacks;
    }

    /**Sets the number of fumbles that the player has made for 
	 * the set type of play.
	 * @param aFumbles number of fumbles
	*/
    public void setFumbles(int aFumbles) {
        fumbles = aFumbles;
    }

    /**Sets the number of lost fumbles that the player has made for 
	 * the set type of play.
	 * @param aFumblesLost number of lost fumbles
	*/
    public void setFumblesLost(int aFumblesLost) {
        fumblesLost = aFumblesLost;
    }

    /**Gets the name of the player.
	 * @return the name of the player
	*/
    public String getPlayer() {
        return player;
    }

    /**Gets the name of the player.
	 * @return the name of the player
	*/
    public int getAttempts() {
        return attempts;
    }

    /**Gets the name of the player.
	 * @return the name of the player
	*/
    public int getSuccesses() {
        return successes;
    }

    /**Gets the yards that the player has gained.
	 * @return the yards of the player
	*/
    public int getYards() {
        return yards;
    }

    /**Gets the number of touchdowns that the player has gained.
	 * @return the touchdowns of the player
	*/
    public int getTouchdowns() {
        return touchdowns;
    }

    /**Gets the number of interceptions that the player has gained.
	 * @return the interceptions of the player
	*/
    public int getInterceptions() {
        return interceptions;
    }

    /**Gets the number of sacks that the player has made.
	 * @return the sacks that the player has made
	*/
    public int getSacks() {
        return sacks;
    }

    /**Gets the fumbles that the player has committed.
	 * @return the fumbles of the player
	*/
    public int getFumbles() {
        return fumbles;
    }

    /**Gets the fumbles that the player has lost.
	 * @return the lost fumbles of the player
	*/
    public int getFumblesLost() {
        return fumblesLost;
    }
}
