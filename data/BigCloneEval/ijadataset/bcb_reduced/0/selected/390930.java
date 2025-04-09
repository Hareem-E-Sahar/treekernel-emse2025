package org.moyoman.framework;

import org.moyoman.log.*;
import org.moyoman.util.*;
import java.io.*;
import java.util.*;

/** This class saves and restores games using the file system.
  * It implements the Persister interface, and uses the
  * Java serialization functionality to accomplish this.
  */
class FSPersister implements Persister {

    /** The directory under which temporary games are stored.*/
    private static final String TEMPORARY_DIR;

    /** The directory under which permanent games are stored.*/
    private static final String PERMANENT_DIR;

    /** This string is used as part of the filename of a temporary game at a particular move.*/
    private static final String MOVE_PREFIX = "move_";

    /** This is the suffix appended to a persisted game name to get the file name.*/
    private static String PG_SUFFIX = ".pg";

    /** This is the name of the last game file.*/
    private static String LAST_GAME_FILE_NAME = "last_game";

    /** The ServerConfig object.*/
    private static final ServerConfig sc;

    /** The top level directory where the moyoman distribution is installed.*/
    private static final String topDir;

    static {
        sc = ServerConfig.getServerConfig();
        topDir = sc.getTopDirectory();
        TEMPORARY_DIR = getTemporaryDirectory();
        PERMANENT_DIR = getPermanentDirectory();
    }

    /** Create a new FSPersister object.
	  * This operation deletes all temporary games that have expired.
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    FSPersister() throws InternalErrorException {
        checkTempGames(sc);
    }

    /** Get the name of the directory where the temporary games are stored.
	  * @return a String which is the name of the temporary directory.
	  */
    private static String getTemporaryDirectory() {
        String str = topDir + "tmp_games" + File.separator + "Default" + File.separator;
        return str;
    }

    /** Get the name of the directory where the permanent games are stored.
	  * @return a String which is the name of the permanent directory.
	  */
    private static String getPermanentDirectory() {
        String str = topDir + "games" + File.separator + "Default" + File.separator;
        return str;
    }

    /** Delete all temporary games which have expired.
	  * This is based on the HOURS_TEMP_FILES_EXPIRED configuration parameter.
	  * @param sc The ServerConfig object.
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    private static void checkTempGames(ServerConfig sc) throws InternalErrorException {
        long currtime = System.currentTimeMillis();
        String str = sc.getProperty("HOURS_TEMP_FILES_EXPIRE");
        str = str.trim();
        int expireparam = Integer.parseInt(str);
        long cutoff = currtime - (expireparam * 3600 * 1000);
        checkTempGames(cutoff);
    }

    /** Delete all temporary games which have expired.
	  * @param cutoff The time before which files should be deleted.
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    private static void checkTempGames(long cutoff) throws InternalErrorException {
        try {
            File dir = new File(getTemporaryDirectory());
            File[] games = dir.listFiles();
            for (int i = 0; i < games.length; i++) {
                long modtime = games[i].lastModified();
                if (modtime < cutoff) {
                    if (games[i].isDirectory()) {
                        File[] delfiles = games[i].listFiles();
                        int errcount = 0;
                        for (int j = 0; j < delfiles.length; j++) {
                            if (!delfiles[j].delete()) errcount++;
                        }
                        if (!games[i].delete()) errcount++;
                        if (errcount > 0) {
                            SystemLog.warning("Unable to delete temporary game " + games[i].getAbsolutePath());
                        }
                    } else {
                        if (games[i].getName().equals(LAST_GAME_FILE_NAME)) games[i].delete();
                    }
                }
            }
        } catch (Exception e) {
            InternalErrorException iee = new InternalErrorException(e);
            throw iee;
        }
    }

    /** Delete a game in permanent storage.
	  * @param game The game to delete.
	  * @throws PermissionsException Thrown if the delete operation fails.
	  * @throws NoSuchDataException Thrown if the file does not exist. 
	  */
    public void delete(PersistedGame game) throws InternalErrorException, PermissionsException, NoSuchDataException {
        String pg = getSavedGameFileName(game.getName());
        String[] id = new String[2];
        id[0] = getPersistedGameId(pg, Color.BLACK);
        id[1] = getPersistedGameId(pg, Color.WHITE);
        for (int i = 0; i < id.length; i++) {
            String fname = getPermanentFileName(id[i]);
            File f = new File(fname);
            if (!f.exists()) {
                throw new NoSuchDataException("Could not delete game");
            }
            boolean flag = f.delete();
            if (!flag) {
                SystemLog.error("Could not delete file " + fname);
                throw new PermissionsException("Could not delete game " + id[i]);
            }
        }
        File pgfile = new File(pg);
        pgfile.delete();
    }

    /** Delete a game in temporary storage.
	  * @param game The game to delete.
	  * @throws PermissionsException Thrown if the delete operation fails.
	  * @throws NoSuchDataException Thrown if the file does not exist. 
	  */
    public void deleteTemporaryState(PersistedGame game) throws InternalErrorException, PermissionsException, NoSuchDataException {
        String pgfname = getTemporaryGameFileName(game.getName());
        String bid = getPersistedGameId(pgfname, Color.BLACK);
        String wid = getPersistedGameId(pgfname, Color.WHITE);
        File pgfile = new File(pgfname);
        pgfile.delete();
        GameId bgid = new GameId(bid, false);
        GameId wgid = new GameId(wid, false);
        deleteTemporaryState(bgid);
        deleteTemporaryState(wgid);
    }

    private void deleteTemporaryState(GameId id) throws PermissionsException, NoSuchDataException {
        String fname = getTemporaryDirectoryName(id);
        File f = new File(fname);
        if ((!f.exists()) || (!f.isDirectory())) {
            SystemLog.error("Invalid game id " + id);
            throw new NoSuchDataException("Invalid game id");
        }
        boolean flag = true;
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (!files[i].delete()) flag = false;
        }
        if (flag) flag = f.delete();
        if (!flag) {
            SystemLog.error("Could not delete one or more files for game " + id);
            throw new PermissionsException("Could not delete game");
        }
    }

    /** Restore a game from permanent storage.
	  * This involves reading in the serialized
	  * objects, and constructing a Controller object.
	  * @param id The id of the game.
	  * @return The Controller object representing the
	  * game in its current state.
	  * @throws NoSuchDataException Thrown if the saved game does not exist. 
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    public Controller restore(GameId id) throws NoSuchDataException, InternalErrorException {
        String fname = getPermanentFileName(id);
        File file = new File(fname);
        if (!file.exists()) throw new NoSuchDataException("File does not exist");
        Controller cont = getController(fname);
        GameId oldid = cont.getId();
        String tempname = getTemporaryDirectoryName(oldid);
        File f = new File(tempname);
        if (f.exists()) try {
            deleteTemporaryState(oldid);
        } catch (PermissionsException pe) {
            SystemLog.error(pe);
            throw new InternalErrorException(pe);
        }
        return cont;
    }

    /** Restore a game from temporary storage to the latest saved state.
	  * This involves reading in the serialized
	  * objects, and constructing a Controller object.
	  * @param id The id of the game.
	  * @return The controller object representing the
	  * game in its current state.
	  * @throws NoSuchDataException Thrown if the temporary game does not exist. 
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    public Controller restoreTemporaryState(GameId id) throws NoSuchDataException, InternalErrorException {
        String fname = getTemporaryFileName(id);
        Controller cont = getController(fname);
        return cont;
    }

    /** Restore the most recent game in temporary storage.
	  * This method is used to restart a game if the software
	  * exited unexpectedly, as if the computer crashed.
	  * @param color The color of the player to restore.
	  * @return A Controller object.
	  * @throws NoSuchDataException Thrown if no temporary game exists. 
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    public Controller restoreMostRecentTemporaryState(Color color) throws NoSuchDataException, InternalErrorException {
        long t = 0l;
        PersistedGame last = null;
        PersistedGame[] pgarr = listTemporaryGames();
        for (int i = 0; i < pgarr.length; i++) {
            String fname = getTemporaryGameFileName(pgarr[i].getName());
            File f = new File(fname);
            long t2 = f.lastModified();
            if (t2 > t) {
                last = pgarr[i];
                t = t2;
            }
        }
        if (last == null) throw new NoSuchDataException("No active games to restore");
        GameId gid;
        if (color.equals(Color.BLACK)) {
            gid = last.getBlackId();
        } else {
            gid = last.getWhiteId();
        }
        return restoreTemporaryState(gid);
    }

    /** Restore a game from temporary storage to the specified move.
	  * This would be used for taking back one or more moves.
	  * @param id The id of the game.
	  * @param number The move number to restore to.
	  * @return The Controller object representing the
	  * game in its current state.
	  * @throws NoSuchDataException Thrown if the information for the temporary game and move do not exist. 
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    public Controller restoreTemporaryState(GameId id, int number) throws NoSuchDataException, InternalErrorException {
        String fname = getTemporaryFileName(id, number);
        Controller cont = getController(fname);
        updateLastMove(id, number);
        return cont;
    }

    /** Save the game to permanent storage.
	  * @param id The name to save the game as.
	  * @param cont The controller with the game information.
	  * @throws DataAlreadyExistsException Thrown if the id is already used.
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    private void save(GameId id, Controller cont) throws DataAlreadyExistsException, InternalErrorException {
        String fname = getPermanentFileName(id);
        File f = new File(fname);
        if (f.exists()) throw new DataAlreadyExistsException("New file name already exists");
        saveController(cont, fname);
    }

    public void save(PersistedGame game) throws DataAlreadyExistsException, InternalErrorException {
        GameId bid = game.getBlackId();
        GameId wid = game.getWhiteId();
        Controller bc = Controller.getController(bid);
        Controller wc = Controller.getController(wid);
        save(bid, bc);
        save(wid, wc);
        writeSavedGameFile(game);
    }

    public void saveTemporaryGame(GameId black, GameId white) throws InternalErrorException {
        writeLastGameFile(black, white);
    }

    /** Save the game to temporary storage for a particular move.
	  * This operation will be called frequently throughout
	  * the game, after every move or every other move.
	  * Therefore, this method is also responsible for deleting
	  * the state information for older moves, depending on
	  * how many previous moves are to be saved as defined in
	  * the configuration files.
	  * @param id The id of the game
	  * @param cont The Controller with the state information.
	  * @param number The move number to save as.
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    public void saveTemporaryState(GameId id, Controller cont, int move) throws InternalErrorException {
        String fname = getTemporaryFileName(id, move);
        String dirname = getTemporaryDirectoryName(id);
        File f = new File(dirname);
        if (f.exists()) f.setLastModified(System.currentTimeMillis()); else f.mkdir();
        saveController(cont, fname);
        updateLastMove(id, move);
        ServerConfig sc = ServerConfig.getServerConfig();
        String his = sc.getProperty("MOVE_HISTORY");
        his = his.trim();
        int history = Integer.parseInt(his);
        int cutoff = move - history;
        dirname = getTemporaryDirectoryName(id);
        File dirfile = new File(dirname);
        File[] movefiles = dirfile.listFiles();
        for (int i = 0; i < movefiles.length; i++) {
            String name = movefiles[i].getName();
            if (name.startsWith(MOVE_PREFIX)) {
                int index = name.indexOf(".");
                String substr = name.substring(MOVE_PREFIX.length(), index);
                int num = Integer.parseInt(substr);
                if (num < cutoff) {
                    movefiles[i].delete();
                }
            }
        }
    }

    /** Update the last_move file.
	  * This file is only used for temporary storage.  When restoring a game,
	  * the file with the largest move number is not always the last move.
	  * For example, if the user has taken back one or more moves, then the
	  * higher numbers are no longer valid.  Another example is if the
	  * software crashed in the middle of saving the state.  This method is
	  * called after the serialization operation has completed, so if this
	  * file is updated with a move, it is known to be valid.
	  * @throws NoSuchDataException Thrown if the information for the temporary game and move do not exist. 
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    private void updateLastMove(GameId id, int move) throws NoSuchDataException, InternalErrorException {
        String dname = getTemporaryDirectoryName(id);
        String fname = dname + File.separator + "last_move";
        File f = new File(dname);
        if (!f.exists()) throw new NoSuchDataException("Directory does not exist");
        try {
            FileOutputStream fos = new FileOutputStream(fname);
            String str = Integer.toString(move);
            fos.write(str.getBytes());
        } catch (Exception e) {
            InternalErrorException iee = new InternalErrorException(e);
            throw iee;
        }
    }

    public boolean permanentStateExists(GameId id) {
        String fname = getPermanentFileName(id);
        File f = new File(fname);
        return f.exists();
    }

    /** Return whether the game exists in permanent storage.
	  * @param game The game being checked.
	  * @return true if the game exists, or false.
	  */
    public boolean permanentStateExists(PersistedGame game) {
        if (permanentStateExists(game.getBlackId())) return true;
        if (permanentStateExists(game.getWhiteId())) return true;
        return false;
    }

    public boolean temporaryStateExists(GameId id) throws InternalErrorException {
        try {
            String fname = getTemporaryFileName(id);
            File f = new File(fname);
            return f.exists();
        } catch (Exception e) {
            return false;
        }
    }

    /** Return whether the game exists in temporary storage.
	  * @param game The game being checked.
	  * @return true if the game exists, or false.
	  */
    public boolean temporaryStateExists(PersistedGame game) throws InternalErrorException {
        if (temporaryStateExists(game.getBlackId())) return true;
        if (temporaryStateExists(game.getWhiteId())) return true;
        return false;
    }

    /** Return whether the game exists in temporary storage for the given move.
	  * @param id The id of the game.
	  * @param move The move number of the game.
	  * @return true if the game exists for the given move, or false.
	  */
    public boolean temporaryStateExists(GameId id, int move) {
        String fname = getTemporaryFileName(id, move);
        File f = new File(fname);
        return f.exists();
    }

    /** Serialize the Controller object and write it out to disk.
	  * @param c The Controller to be serialized.
	  * @param fname The filename of the serialized file.
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    private void saveController(Controller c, String fname) throws InternalErrorException {
        try {
            FileOutputStream fos = new FileOutputStream(fname);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(c);
        } catch (Exception e) {
            SystemLog.error(e);
        }
    }

    /** Deserialize the file and create a Controller object.
	  * @param fname The file name of the serialized file.
	  * @return A Controller object created from the serialized file.
	  * @throws NoSuchDataException Thrown if the serialized file does not exist. 
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    private Controller getController(String fname) throws NoSuchDataException, InternalErrorException {
        try {
            FileInputStream fis = new FileInputStream(fname);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Controller c = (Controller) ois.readObject();
            return c;
        } catch (Exception e) {
            SystemLog.error(e);
            InternalErrorException iee = new InternalErrorException(e);
            throw iee;
        }
    }

    /** Get the filename for the game id in permanent storage.
	  * Unlike temporary games, the permanently saved game is
	  * not saved for multiple moves, that is why a file name
	  * is returned instead of a directory name.
	  * @param id The game id.
	  * @return A String which is the file name.
	  */
    private String getPermanentFileName(String id) {
        String fname = PERMANENT_DIR + id + ".ser";
        return fname;
    }

    private String getPermanentFileName(GameId id) {
        return getPermanentFileName(id.get());
    }

    /** Get the directory name for the game id in temporary storage.
	  * A game saved to temporary storage has a different file for different
	  * moves which are stored under a single directory.  That is why
	  * a directory name is returned, instead of a file name as for the permanently
	  * stored games.
	  * @param id The game id.
	  * @return A String which is the file name.
	  */
    private String getTemporaryDirectoryName(String id) {
        String dirname = TEMPORARY_DIR + id;
        return dirname;
    }

    private String getTemporaryDirectoryName(GameId id) {
        return getTemporaryDirectoryName(id.get());
    }

    /** Get the filename for the game id in temporary storage.
	  * @param id The game id.
	  * @return A String which is the file name.
	  */
    private String getTemporaryFileName(GameId id, int move) {
        String fname = getTemporaryDirectoryName(id) + File.separator + MOVE_PREFIX + move + ".ser";
        return fname;
    }

    /** Get the name of the most recent file for the particular game.
	  * @param id The game id.
	  * @return A String which is a file name of the serialized file.
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    private String getTemporaryFileName(GameId id) throws NoSuchDataException, InternalErrorException {
        try {
            String fname = TEMPORARY_DIR + id.get() + File.separator + "last_move";
            FileInputStream fis = new FileInputStream(fname);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String str = br.readLine();
            int move = Integer.parseInt(str);
            fname = getTemporaryFileName(id, move);
            return fname;
        } catch (FileNotFoundException fnfe) {
            NoSuchDataException nsde = new NoSuchDataException(fnfe.toString());
            throw nsde;
        } catch (Exception e) {
            SystemLog.error(e);
            InternalErrorException iee = new InternalErrorException(e);
            throw iee;
        }
    }

    /** Return an array of all the games in temporary storage.
	  * @return An array of GameId objects.
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    public PersistedGame[] listTemporaryGames() throws InternalErrorException {
        return listGames(TEMPORARY_DIR);
    }

    private PersistedGame[] listGames(String fname) throws InternalErrorException {
        File gamedir = new File(fname);
        File[] files = gamedir.listFiles();
        ArrayList al = new ArrayList();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                String name = files[i].getName();
                if (name.endsWith(PG_SUFFIX)) {
                    int index = name.indexOf(PG_SUFFIX);
                    String nm = name.substring(0, index);
                    String black = getPersistedGameId(files[i].getAbsolutePath(), Color.BLACK);
                    GameId bid = new GameId(black, false);
                    String white = getPersistedGameId(files[i].getAbsolutePath(), Color.WHITE);
                    GameId wid = new GameId(white, false);
                    PersistedGame pg = new PersistedGame(nm, bid, wid);
                    al.add(pg);
                }
            }
        }
        PersistedGame[] pgarr = new PersistedGame[al.size()];
        al.toArray(pgarr);
        return pgarr;
    }

    /** Return an array of all the games in permanent storage.
	  * @return An array of GameId objects.
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    public PersistedGame[] listPermanentGames() throws InternalErrorException {
        return listGames(PERMANENT_DIR);
    }

    private String getTemporaryGameFileName(String name) {
        String str = getTemporaryDirectory() + name + PG_SUFFIX;
        return str;
    }

    private String getLastGameFileName() {
        return getTemporaryGameFileName(LAST_GAME_FILE_NAME);
    }

    private String getSavedGameFileName(String name) {
        String str = getPermanentDirectory() + name + PG_SUFFIX;
        return str;
    }

    public void writeLastGameInformation(GameId black, GameId white) {
        writeLastGameFile(black, white);
    }

    private void writeLastGameFile(GameId black, GameId white) {
        String fname = getLastGameFileName();
        writeGameFile(fname, black, white);
    }

    private void writeSavedGameFile(PersistedGame game) {
        String fname = getSavedGameFileName(game.getName());
        writeGameFile(fname, game.getBlackId(), game.getWhiteId());
    }

    private void writeGameFile(String fname, GameId black, GameId white) {
        try {
            FileWriter fw = new FileWriter(fname);
            PrintWriter pw = new PrintWriter(fw);
            pw.println("Black " + black.get());
            pw.println("White " + white.get());
            pw.close();
        } catch (Exception e) {
            SystemLog.error(e);
        }
    }

    private String getPersistedGameId(String fname, Color color) throws NoSuchDataException, InternalErrorException {
        PersistedGame pg = getPersistedGame(fname);
        if (color.equals(Color.BLACK)) return pg.getBlackId().get(); else return pg.getWhiteId().get();
    }

    private PersistedGame getPersistedGame(String fname) throws NoSuchDataException, InternalErrorException {
        try {
            File f = new File(fname);
            if (!f.exists()) {
                System.out.println("fname is " + fname + " not found");
                throw new NoSuchDataException("persisted game does not exist");
            }
            String name = f.getName();
            int index = name.indexOf(PG_SUFFIX);
            name = name.substring(0, index);
            String black;
            String white;
            FileInputStream fis = new FileInputStream(fname);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String str = br.readLine();
            StringTokenizer st = new StringTokenizer(str);
            String first = st.nextToken();
            if (first.equalsIgnoreCase("Black")) {
                black = st.nextToken();
            } else throw new InternalErrorException("Cannot parse game file");
            str = br.readLine();
            st = new StringTokenizer(str);
            first = st.nextToken();
            if (first.equalsIgnoreCase("White")) {
                white = st.nextToken();
            } else throw new InternalErrorException("Cannot parse game file");
            GameId bid = new GameId(black, false);
            GameId wid = new GameId(white, false);
            PersistedGame pg = new PersistedGame(name, bid, wid);
            return pg;
        } catch (Exception e) {
            SystemLog.error(e);
            throw new NoSuchDataException("Cannot read id for file name and color");
        }
    }

    public PersistedGame getTemporaryPersistedGame(String name) throws NoSuchDataException, InternalErrorException {
        String fname = getTemporaryGameFileName(name);
        return getPersistedGame(fname);
    }

    public PersistedGame getSavedPersistedGame(String name) throws NoSuchDataException, InternalErrorException {
        String fname = getSavedGameFileName(name);
        return getPersistedGame(fname);
    }

    /** Delete all temporary game files.
	  * @throws InternalErrorException Thrown if the operation fails for any reason.
	  */
    public void cleanup() throws InternalErrorException {
        long currtime = System.currentTimeMillis();
        checkTempGames(currtime);
        File f = new File(getLastGameFileName());
        f.delete();
    }
}
