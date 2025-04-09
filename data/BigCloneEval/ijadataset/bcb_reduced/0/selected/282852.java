package com.textflex.txtfl;

import java.io.*;

/** Filters filenames to select only files with particular endings.
 */
public class PlayerFileManager implements FilenameFilter {

    private String firstName = "";

    private String lastName = "";

    private File file = null;

    private Player player = null;

    /** Creates an empty player file manager object.
	 */
    public PlayerFileManager() {
    }

    /** Creates a player file manager with a file.
	 * @param aFile player file spec
	 */
    public PlayerFileManager(File aFile) {
        setFile(aFile);
    }

    /** Creates a player file manager with names and a spec file.
	 * @param aFirstName player first name
	 * @param aLastName player last name
	 * @param aFile player file spec
	 */
    public PlayerFileManager(String aFirstName, String aLastName, File aFile) {
        setFirstName(aFirstName);
        setLastName(aLastName);
        setFile(aFile);
    }

    /** Sorts an array of player file managers by player first names.
	 * @param array array of player file managers
	 * @param length number of filled elements in the array
	 * @return sorrted player file manager array
	 */
    public static PlayerFileManager[] sortByFirstName(PlayerFileManager[] array, int length) {
        return sortByName(array, length, true);
    }

    /** Sorts an array of player file managers by player last names.
	 * @param array array of player file managers
	 * @param length number of filled elements in the array
	 * @return sorrted player file manager array
	 */
    public static PlayerFileManager[] sortByLastName(PlayerFileManager[] array, int length) {
        return sortByName(array, length, false);
    }

    /** Sorts an array of player file managers by player full name, which
	 * consists of the first name followed by the last, separated by a space, or
	 * vice versa.
	 * @param array array of player file managers
	 * @param length number of filled elements in the array
	 * @param firstNameFirst true if the first name is placed prior to the last name
	 * during sorting
	 * @return sorrted player file manager array
	 */
    public static PlayerFileManager[] sortByName(PlayerFileManager[] array, int length, boolean firstNameFirst) {
        int start = 0;
        int end = 0;
        int gap = 0;
        int n = length;
        PlayerFileManager tmp = null;
        for (gap = n / 2; gap > 0; gap /= 2) {
            for (end = gap; end < n; end++) {
                for (start = end - gap; start >= 0 && (combineNames(array[start], firstNameFirst).compareToIgnoreCase(combineNames(array[start + gap], firstNameFirst)) > 0); start -= gap) {
                    tmp = array[start];
                    array[start] = array[start + gap];
                    array[start + gap] = tmp;
                }
            }
        }
        return array;
    }

    /** Combines names simply by appending last name to first, or vice versa.
	 * @param manager player file manager, assumed to contain first and
	 * last names
	 * @param firstNameFirst if true, the first name precedes the last, separated
	 * by a space
	 * @preturn the combined first name
	 */
    private static String combineNames(PlayerFileManager manager, boolean firstNameFirst) {
        return firstNameFirst ? manager.getFirstName() + " " + manager.getLastName() : manager.getLastName() + " " + manager.getFirstName();
    }

    /** Gets the index of a player from an array of managers sorted by
	 * last name and then first name.
	 * The player's name should be given as a space-delimited string
	 * and is case-insensitive.
	 * @param arrayList the sorted array of player file managers to search
	 * @param len number of filled elements in the array
	 * @param quarry the string to search for, which should contain the
	 * last name followed by the first name, separated by a single space.
	 * If the first name is an initial, <code>startsWith</code> should 
	 * be true.
	 * @param startsWith if true, a match will be found as long as an element
	 * starts with the quarry string.  Each element will also parse the last
	 * name plus the first name, separated by a space.  For example, "Ajay Lee"
	 * will be translated to "Lee Ajay" when comparing with the quarry.
	 * Useful when the array is sorted by last names followed by first names, 
	 * and the quarry is a last name but only a first name initial.
	 * @return the index of the found quarry, or -1 if no such string is found
    */
    public static int getByLastName(PlayerFileManager[] arrayList, int len, String quarry, boolean startsWith) {
        if (quarry == null) return -1;
        quarry = quarry.toLowerCase();
        int start = 0;
        int end = len - 1;
        int mid = end / 2;
        int found = -1;
        String s = "";
        while (start <= end && found == -1) {
            s = combineNames(arrayList[mid], false).toLowerCase();
            if ((startsWith && s.startsWith(quarry)) || s.equalsIgnoreCase(quarry)) {
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

    /** Gets the index of a player who matches both a player and team name
	 * from an array of managers sorted by
	 * last name and then first name.
	 * This method further ensures a match, since players may have the same
	 * last name and first initial but could be distinguished by team.
	 * The player's name should be given as a space-delimited string
	 * and is case-insensitive.  If a player match is found, a team name match is
	 * checked by comparing the given team name with the various team names
	 * provided by the team lookup table for the player's team.
	 * If no such match is found, the search continues, starting with another
	 * player name match.
	 *
	 * <p>As a side effect, also adds the player object of any checked
	 * player into the PlayerFileManager during team comparison checking,
	 * as a way to prevent unnessary player object creation if the manager
	 * is accessed later.
	 * 
	 * @param arrayList the sorted array of player file managers to search
	 * @param len number of filled elements in the array
	 * @param quarry the string to search for, which should contain the
	 * last name followed by the first name, separated by a single space.
	 * If the first name is an initial, <code>startsWith</code> should 
	 * be true.
	 * @param startsWith if true, a match will be found as long as an element
	 * starts with the quarry string.  Each element will also parse the last
	 * name plus the first name, separated by a space.  For example, "Ajay Lee"
	 * will be translated to "Lee Ajay" when comparing with the quarry.
	 * Useful when the array is sorted by last names followed by first names, 
	 * and the quarry is a last name but only a first name initial.
	 * @param team the quarry's team, which any player matched by
	 * name is also required to match by team
	 * @param lookups a team lookup table
	 * @return the index of the found quarry, or -1 if no such string is found
    */
    public static PlayerFileManager getByNameAndTeam(PlayerFileManager[] arrayList, int len, String quarry, boolean startsWith, String team, TeamLookup[] lookups) {
        int candidate = getByLastName(arrayList, len, quarry, startsWith);
        if (candidate < 0) return null;
        int check = candidate;
        Player p = null;
        String name = "";
        PlayerFileManager manager = null;
        do {
            manager = arrayList[check];
            p = manager.getPlayer();
            if (TeamLookup.isTeam(lookups, lookups.length, team, p.getTeam()) >= 0) {
                manager.setPlayer(p);
                return manager;
            } else {
                check++;
            }
        } while (check < len && ((startsWith && (name = combineNames(arrayList[check], false)).startsWith(quarry)) || name.equalsIgnoreCase(quarry)));
        check = candidate - 1;
        while (check >= 0 && ((startsWith && (name = combineNames(arrayList[check], false)).startsWith(quarry)) || name.equalsIgnoreCase(quarry))) {
            manager = arrayList[check];
            p = manager.getPlayer();
            if (TeamLookup.isTeam(lookups, lookups.length, team, p.getTeam()) >= 0) {
                manager.setPlayer(p);
                return manager;
            } else {
                check--;
            }
        }
        return null;
    }

    /** Filters player files.
	 * @param playersDir directory to filter for player spec files, usually
	 * the player files directory
	 * @return the list of files that match the typical player spec file extension
	 * @see #accept
	 */
    public File[] listPlayerFiles(File playersDir) {
        File[] files = playersDir.listFiles(this);
        return files;
    }

    /** Aceepts the file if its name ends withe specified string, 
	<code>endsWith</code>.
	@param file file to check
	@param name name to check
    */
    public boolean accept(File file, String name) {
        String endsWith = ".txt";
        return (name.toLowerCase().endsWith(endsWith)) && name.indexOf("_") != -1;
    }

    /** Creates player file managers for each of the files that 
	 * match the player spec file extensions, filling the file and the name
	 * fields according to the filenames
	 * If the filename consists of less than two names, no name is added.
	 * @param playersDir directory to filter for player spec files, usually
	 * the player files directory
	 * @return player file managers for each of the found player spec files
	 */
    public static PlayerFileManager[] createPlayerFileManagers(File playersDir) {
        PlayerFileManager seed = new PlayerFileManager();
        File[] files = seed.listPlayerFiles(playersDir);
        PlayerFileManager[] managers = new PlayerFileManager[files.length];
        for (int i = 0; i < managers.length; i++) {
            File file = files[i];
            String filename = file.getName();
            String[] names = filename.split("[_.]");
            if (names.length > 2) {
                managers[i] = new PlayerFileManager(names[0], names[1], file);
            } else {
                managers[i] = new PlayerFileManager(file);
            }
        }
        return managers;
    }

    /** Sets the player first name.
	 * @param aFirstName the first name
	 */
    public void setFirstName(String aFirstName) {
        firstName = aFirstName;
    }

    /** Sets the player last name.
	 * @param aLastName the last name
	 */
    public void setLastName(String aLastName) {
        lastName = aLastName;
    }

    /** Sets the file for the manager.
	 * @param aFile player spec file
	 */
    public void setFile(File aFile) {
        file = aFile;
    }

    /** Sets the player.
	 * @param aPlayer the player object
	 */
    public void setPlayer(Player aPlayer) {
        player = aPlayer;
    }

    /** Gets the first name of the player.
	 * @return first name
	 */
    public String getFirstName() {
        return firstName;
    }

    /** Gets the last name of the player.
	 * @return last name
	 */
    public String getLastName() {
        return lastName;
    }

    /** Gets the player spec file.
	 * @return spec file
	 */
    public File getFile() {
        return file;
    }

    /** Gets the player object corresponding to the player spec file
	 * stored in the manager.
	 * @return player object
	 */
    public Player getPlayer() {
        if (player == null) {
            File playerFile = getFile();
            if (playerFile == null) return null;
            try {
                player = new Player(getFile());
            } catch (IOException e) {
                System.out.println("The player file " + playerFile.getName() + " could not be read as a player.");
            }
        }
        return player;
    }
}
