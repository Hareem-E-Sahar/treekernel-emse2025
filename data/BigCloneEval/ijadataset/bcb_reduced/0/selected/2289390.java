package com.textflex.txtfl;

import java.io.*;
import java.util.*;

/** A lookup table for grouping the various ways of naming a team.
 * This class includes functions for sorting teams and identifying
 * them by their various components.
 * 
 * <p>Team names are found from a file in <code>players/draft/TeamLookup.txt</code>.
 * New teams can be added on separate lines in the following form: 
 * <code>Hometown|TeamName|Abbreviation</code>.  For example,
 * The San Francisco 49ers would be added as
 * <code>San Francisco|49ers|SFO</code>.
 */
public class TeamLookup {

    /** Filename of the team lookup table.
	 */
    public static final String FILENAME_TEAM_LOOKUP = "TeamLookup.txt";

    private static final String NEWLINE = System.getProperty("line.separator");

    private String hometown = "";

    private String teamName = "";

    private String abbr = "";

    /** Creates an empty TeamLookup object.
	 */
    public TeamLookup() {
    }

    /** Creates a TeamLookup object with team names as parameters.
	 * @param aHometown the team city
	 * @param aTeamName the name of the team
	 * @param aAbbr the abbreviated name of the team
	 */
    public TeamLookup(String aHometown, String aTeamName, String aAbbr) {
        setHometown(aHometown);
        setTeamName(aTeamName);
        setAbbr(aAbbr);
    }

    /** Creates an array of team lookups from the team lookup file.
	 * @return an array the size of the number of teams found in the file,
	 * consisting of teams and the various ways of naming them
	 */
    public static TeamLookup[] createTeamLookups() {
        BufferedReader teamLookupsReader = null;
        TeamLookup[] teamLookups = new TeamLookup[100];
        int teamLookupsi = 0;
        try {
            teamLookupsReader = new BufferedReader(new InputStreamReader(LibTxtfl.class.getResourceAsStream(getTeamLookupPath())));
            String line = "";
            while ((line = LibTxtfl.getNextLine(teamLookupsReader)) != null) {
                String[] splitLine = line.split("\\|");
                if (splitLine.length >= 3) {
                    teamLookups[teamLookupsi++] = new TeamLookup(splitLine[0], splitLine[1], splitLine[2]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("The team lookup file might not be in the proper " + NEWLINE + "location or format.");
        } finally {
            try {
                teamLookupsReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return (TeamLookup[]) LibTxtfl.truncateArray(teamLookups, teamLookupsi);
    }

    /** Sorts the team lookup table by the full name of the team.
	 * @param array lookup table
	 * @param length length of filled elements in the table
	 * @see #getFullName
	 */
    public static TeamLookup[] sortByFullName(TeamLookup[] array, int length) {
        int start = 0;
        int end = 0;
        int gap = 0;
        int n = length;
        TeamLookup tmp = null;
        for (gap = n / 2; gap > 0; gap /= 2) {
            for (end = gap; end < n; end++) {
                for (start = end - gap; start >= 0 && (array[start].getFullName().compareToIgnoreCase(array[start + gap].getFullName()) > 0); start -= gap) {
                    tmp = array[start];
                    array[start] = array[start + gap];
                    array[start + gap] = tmp;
                }
            }
        }
        return array;
    }

    /** Gets the index of a string from an array sorted by the teams'
	 * full names.
	 * Returns the first instance found if more than one instance of the given
	 * string exists.
	 * @param arrayList array to sort
	 * @param len number of filled elements in the array
	 * @param quarry the full name of the team to search for
	 * @return the index of the found quarry, or -1 if no such string is found
	 * @see #sortByFullName
	 * @see #getFullName
    */
    public static int getByFullName(TeamLookup[] arrayList, int len, String quarry) {
        if (quarry == null) return -1;
        int start = 0;
        int end = len - 1;
        int mid = end / 2;
        int found = -1;
        String s = "";
        while (start <= end && found == -1) {
            if ((s = arrayList[mid].getFullName()).equalsIgnoreCase(quarry)) {
                found = mid;
            } else if (quarry.compareToIgnoreCase(s) < 0) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
            mid = (start + end) / 2;
        }
        return found;
    }

    /** Sorts the team lookup table by the team name of the team.
	 * @param array lookup table
	 * @param length length of filled elements in the table
	 * @see #getTeamName
	 */
    public static TeamLookup[] sortByTeamName(TeamLookup[] array, int length) {
        int start = 0;
        int end = 0;
        int gap = 0;
        int n = length;
        TeamLookup tmp = null;
        for (gap = n / 2; gap > 0; gap /= 2) {
            for (end = gap; end < n; end++) {
                for (start = end - gap; start >= 0 && (array[start].getTeamName().compareToIgnoreCase(array[start + gap].getTeamName()) > 0); start -= gap) {
                    tmp = array[start];
                    array[start] = array[start + gap];
                    array[start + gap] = tmp;
                }
            }
        }
        return array;
    }

    /** Gets the index of a string from an array sorted by the teams' names.
	 * Returns the first instance found if more than one instance of the given
	 * string exists.
	 * @param arrayList array to sort
	 * @param len number of filled elements in the array
	 * @param quarry the name of the team to search for
	 * @return the index of the found quarry, or -1 if no such string is found
	 * @see #sortByTeamName
	 * @see #getTeamName
    */
    public static int getByTeamName(TeamLookup[] arrayList, int len, String quarry) {
        if (quarry == null) return -1;
        int start = 0;
        int end = len - 1;
        int mid = end / 2;
        int found = -1;
        String s = "";
        while (start <= end && found == -1) {
            if ((s = arrayList[mid].getTeamName()).equalsIgnoreCase(quarry)) {
                found = mid;
            } else if (quarry.compareToIgnoreCase(s) < 0) {
                end = mid - 1;
            } else {
                start = mid + 1;
            }
            mid = (start + end) / 2;
        }
        return found;
    }

    /** Checks whether a given string matches the abbreviation, hometown, 
	 * or name of a given team.
	 * @param lookups table of team lookups
	 * @param len length of filled elements in the lookup table
	 * @param name team name to lookup
	 * @param query string to match against entries in the given team lookup
	 * @return the index of the lookup if a match is found; otherwise, -1
	 */
    public static int isTeam(TeamLookup[] lookups, int len, String name, String query) {
        int i = getByTeamName(lookups, len, name);
        if (name.equals(query)) return i;
        if (i >= 0) {
            TeamLookup lookup = lookups[i];
            if (lookup.getAbbr().equals(query) || lookup.getHometown().equals(query) || lookup.getTeamName().equals(query)) {
                return i;
            }
        }
        return -1;
    }

    /** Gets the hometown of the team.
	 * @return hometown
	 */
    public String getHometown() {
        return hometown;
    }

    /** Gets the name of the team.
	 * @return team name
	 */
    public String getTeamName() {
        return teamName;
    }

    /** Gets the abbreviation of the team.
	 * @return team abbreviation
	 */
    public String getAbbr() {
        return abbr;
    }

    /** Gets the full name of the team, which consists of the 
	 * team hometown followed by the team name, separated 
	 * by a space.
	 * @return the full name of the team
	 */
    public String getFullName() {
        return getHometown() + " " + getTeamName();
    }

    public static String getTeamLookupPath() {
        return "/" + Draft.DRAFT_DIR + "/" + FILENAME_TEAM_LOOKUP;
    }

    /** Sets the hometown of the team.
	 * @param s hometown
	 */
    public void setHometown(String s) {
        hometown = s;
    }

    /** Sets the name of the team.
	 * @param s team name
	 */
    public void setTeamName(String s) {
        teamName = s;
    }

    /** Sets the abbreviation of the team.
	 * @param s team abbreviation
	 */
    public void setAbbr(String s) {
        abbr = s;
    }
}
