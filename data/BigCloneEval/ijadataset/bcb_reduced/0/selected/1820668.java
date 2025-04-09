package src.multiplayer.xmlProcessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import src.eleconics.Utilities;
import src.exceptions.MultiplayerException;
import src.menus.pregame.Map;
import src.menus.pregame.preferences.Configs;
import src.menus.pregame.wrappers.PlayerAndFleetWrapper;
import src.multiplayer.email.Email;
import src.multiplayer.players.Player;
import src.multiplayer.players.Players;
import src.multiplayer.xmlParser.EleconicsXmlParser;
import src.world.Fleet;
import src.world.World;

/**
 * Class 'PlayByEmail' manages the generation of turn files
 * to be sent to the remote players, by email.
 * 
 * @author Forrest Dillaway
 */
public class PlayByEmail extends MultiplayerXmlProcessor {

    /**
	 * Default game data location
	 */
    private static final String GAME_DATA_FILENAME = "data/emailGameData.xml";

    /**
	 * Default turn data location
	 */
    private static final String TURN_DATA_FILENAME = "data/emailTurnData.xml";

    /**
	 * The default message body
	 */
    private static final String DEFAULT_MESSAGE_BODY = "This e-mail has been automatically sent to you by your enemy in the world of Eleconics.  Are you strong enough to respond?!?!";

    /**
	 * The number of bytes to read ahead when marking the
	 * input stream for the Turn Actions.
	 */
    private static final int READ_AHEAD_LIMIT = 256000;

    /**
	 * The zip file containing the game data
	 */
    private ZipFile zipFile;

    /**
	 * True if a mark has been set in the stream after the first
	 * Turn Actions block.
	 */
    private boolean streamNotMarked = true;

    /**
	 * The file name for the zip data
	 */
    private String zipDataFileName = "data/emailZipData.game";

    /**
	 * @see XmlProcessor#XmlProcessor()
	 */
    public PlayByEmail(String[] playerFleetData, String gameFileName, Map map) throws IOException {
        super();
        zipDataFileName = "games/" + gameFileName + ".game";
        transmitWorldDataAfterNextTurnBlock = true;
        createNewGameDataFiles(playerFleetData, map);
    }

    /**
	 * @see XmlProcessor#XmlProcessor()
	 */
    public PlayByEmail(String zipDataFileName) throws IOException {
        super();
        transmitWorldDataAfterNextTurnBlock = true;
        this.zipDataFileName = zipDataFileName;
    }

    /**
	 * Create the game data files in the Xml Stream.
	 * @param players The Players playing in the game.
	 * @param world The World in which the game is played.
	 * @throws IOException In case of an I/O error while creating the
	 *    game data file and the blank turn file.
	 */
    private void createNewGameDataFiles(String[] dataFiles, Map map) throws IOException {
        String tmpGameFile = GAME_DATA_FILENAME, tmpTurnFile = TURN_DATA_FILENAME;
        Player[] playerObjects = new Player[dataFiles.length];
        Hashtable fleets = new Hashtable();
        Fleet fleet = null;
        PlayerAndFleetWrapper playerFleet;
        try {
            EleconicsXmlParser xmlParser = null;
            for (int file = 0; file < dataFiles.length; file++) {
                xmlParser = new EleconicsXmlParser(new InputStreamReader(new FileInputStream(dataFiles[file])));
                playerFleet = new PlayerAndFleetWrapper(xmlParser);
                playerObjects[file] = playerFleet.getPlayer();
                playerObjects[file].setFleetNumber(file + 1);
                fleet = playerFleet.getFleet();
                fleet.setFleetNumber(file + 1);
                fleet.shiftLocation(map.getLocation(file + 1));
                fleet.rotate(map.getAxisAngle(file + 1), map.getLocation(file + 1));
                fleets.put(playerFleet.getFleet().fleetName(), playerFleet.getFleet());
                xmlParser.close();
            }
        } catch (MultiplayerException e) {
            Utilities.popUp(e.getMessage());
            e.printStackTrace();
        }
        Players players = new Players(playerObjects);
        world = new World(fleets);
        XmlProcessor xmlProcessor = new XmlProcessor();
        xmlProcessor.startElement(MultiplayerXmlProcessor.GAME_TAG);
        players.ToXml(xmlProcessor);
        world.ToXml(xmlProcessor);
        xmlProcessor.endElement();
        BufferedWriter out = new BufferedWriter(new FileWriter(tmpGameFile, false));
        out.write(xmlProcessor.stream.toString());
        out.close();
        xmlProcessor.resetWriter();
        xmlProcessor.startElement(MultiplayerXmlProcessor.TURN_TAG);
        xmlProcessor.XmlNode(MultiplayerXmlProcessor.VIEWING_PLAYER_NUMBER_TAG, "1");
        xmlProcessor.XmlNode(MultiplayerXmlProcessor.FIRST_EXECUTING_ACTIONS_PLAYER_NUMBER_TAG, "1");
        xmlProcessor.XmlNode(MultiplayerXmlProcessor.TURN_ACTIONS_TAG, "");
        xmlProcessor.XmlNode(MultiplayerXmlProcessor.START_TURN_TAG, "true");
        xmlProcessor.endElement();
        out = new BufferedWriter(new FileWriter(tmpTurnFile, false));
        out.write(xmlProcessor.stream.toString());
        out.close();
        byte b[] = new byte[512];
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipDataFileName));
        InputStream in = new FileInputStream(tmpGameFile);
        ZipEntry e = new ZipEntry(tmpGameFile.replace(File.separatorChar, '/'));
        zout.putNextEntry(e);
        int len = 0;
        while ((len = in.read(b)) != -1) {
            zout.write(b, 0, len);
        }
        zout.closeEntry();
        in = new FileInputStream(tmpTurnFile);
        e = new ZipEntry(tmpTurnFile.replace(File.separatorChar, '/'));
        zout.putNextEntry(e);
        len = 0;
        while ((len = in.read(b)) != -1) {
            zout.write(b, 0, len);
        }
        zout.closeEntry();
        zout.close();
    }

    /**
	 * Save the World data so that it can be reloaded
	 * and shown to the next players
	 * @throws IOException In the case of a file I/O Error
	 * @see src.multiplayer.xmlProcessor.XmlProcessor#transmitActionData(LinkedList)
	 */
    protected void transmitWorldData() throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(GAME_DATA_FILENAME, false));
        out.write(stream.toString());
        out.close();
    }

    /**
	 * Transmit the Action Block data
	 * @throws Exception
	 */
    protected void transmitActionBlockEnd() throws IOException {
    }

    /**
	 * Write the turn actions to the turn
	 * Actions data file.
	 * @throws IOException In the case that there is a
	 *    file I/O Error while closing turn file or 
	 *    zipping game and turn data, or an error while
	 *    emailing data to the next player.
	 */
    protected void transmitTurnEnd() throws Exception {
        XmlNode(START_TURN_TAG, Boolean.toString(true));
    }

    /**
	 * @see src.multiplayer.xmlProcessor.XmlProcessor#acknowledgeActions()
	 */
    public void acknowledgeActions() throws Exception {
        super.acknowledgeActions();
        if (streamNotMarked) {
            xmlParser.mark(READ_AHEAD_LIMIT);
            streamNotMarked = false;
        }
    }

    /**
	 * @see src.multiplayer.xmlProcessor.XmlProcessor#acknowledgeTurns()
	 */
    public void acknowledgeTurns() throws Exception {
        super.acknowledgeTurns();
        zipFile.close();
    }

    /**
	 * Return a FileReader that reads the game data file
	 * specified initially.
	 * @throws FileNotFoundException If game data file
	 *    is not found.
	 * @throws ZipException - if a ZIP format error has occurred 
	 * @throws IOException - if an I/O error has occurred 
	 * @throws IllegalStateException - if the zip file has been closed
	 * @see src.multiplayer.xmlProcessor.XmlProcessor#getWorldInputStream()
	 */
    protected InputStream getWorldInputStream() throws FileNotFoundException, ZipException, IOException, IllegalStateException {
        zipFile = new ZipFile(zipDataFileName);
        return zipFile.getInputStream(zipFile.getEntry(GAME_DATA_FILENAME));
    }

    /**
	 * Return a FileReader that reads the turn data file
	 * specified initially.
	 * @throws FileNotFoundException If turn data file
	 *    is not found.
	 * @throws ZipException - if a ZIP format error has occurred 
	 * @throws IOException - if an I/O error has occurred 
	 * @throws IllegalStateException - if the zip file has been closed
	 * @see src.multiplayer.xmlProcessor.XmlProcessor#getTurnInputStream()
	 */
    protected InputStream getTurnInputStream() throws FileNotFoundException, ZipException, IOException, IllegalStateException {
        return zipFile.getInputStream(zipFile.getEntry(TURN_DATA_FILENAME));
    }

    /**
	 * Zip the game and turn data.
	 * Reference http://www.rgagnon.com/javadetails/java-0065.html
	 */
    private void zipAndEmailData() throws IOException {
        byte b[] = new byte[512];
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipDataFileName));
        InputStream in = new FileInputStream(GAME_DATA_FILENAME);
        ZipEntry e = new ZipEntry(GAME_DATA_FILENAME.replace(File.separatorChar, '/'));
        zout.putNextEntry(e);
        int len = 0;
        while ((len = in.read(b)) != -1) {
            zout.write(b, 0, len);
        }
        zout.closeEntry();
        in = new FileInputStream(TURN_DATA_FILENAME);
        e = new ZipEntry(TURN_DATA_FILENAME.replace(File.separatorChar, '/'));
        zout.putNextEntry(e);
        len = 0;
        while ((len = in.read(b)) != -1) {
            zout.write(b, 0, len);
        }
        zout.closeEntry();
        zout.close();
        new Email(null, players.getNextViewingPlayer().address(), "Player " + players.viewingPlayerNumber() + " has completed his turn", DEFAULT_MESSAGE_BODY, zipDataFileName, Configs.readConfigsFromFile(), false);
    }

    /**
	 * Print the compression ratios for each file added
	 * to the zip archive
	 * @param e An entry added to the Zip Archive.
	 */
    public static void print(ZipEntry e) {
        System.out.print("added " + e.getName());
        if (e.getMethod() == ZipEntry.DEFLATED) {
            long size = e.getSize();
            if (size > 0) {
                long csize = e.getCompressedSize();
                long ratio = ((size - csize) * 100) / size;
                System.out.println(" (deflated " + ratio + "%)");
            } else {
                System.out.println(" (deflated 0%)");
            }
        } else {
            System.out.println(" (stored 0%)");
        }
    }

    /**
	 * @throws IOException in case of an I/O error while
	 *    initializing the XmlProcessor for the Turn.
	 * @throws MultiplayerException in case of a problem with
	 *    the structure of the Turn file data.
	 * @see src.multiplayer.xmlProcessor.XmlProcessor#initializeTurnData()
	 */
    protected void initializeTurnData() throws IOException, MultiplayerException {
        xmlParser.reset();
        stream.append(xmlParser.getRestOfStream(START_TURN_TAG, true));
    }

    /**
	 * In the case of Play By Email, the Turn data is written to file,
	 * zipped with the World game data, and emailed to the next player.
	 */
    protected void finalizeXmlProcessing() throws Exception {
        BufferedWriter out = new BufferedWriter(new FileWriter(TURN_DATA_FILENAME, false));
        out.write(stream.toString());
        out.close();
        zipAndEmailData();
    }

    /**
	 * Delete the zip files and the intermediate xml files.
	 * @see src.multiplayer.xmlProcessor.XmlProcessor#cleanUp()
	 */
    protected void cleanUp() {
        File intermediateGameData = new File(GAME_DATA_FILENAME);
        File intermediateTurnData = new File(TURN_DATA_FILENAME);
        File zipData = new File(zipDataFileName);
        boolean gameExists = intermediateGameData.exists();
        System.out.println((gameExists) ? "Intermediate Game Data Found" : "No Intermediate Game Data");
        boolean turnExists = intermediateTurnData.exists();
        System.out.println((turnExists) ? "Intermediate Turn Data Found" : "No Intermediate Turn Data");
        boolean zipExists = zipData.exists();
        System.out.println((zipExists) ? "Intermediate Zip Data Found" : "No Intermediate Zip Data");
        boolean gameDeleted = intermediateGameData.delete();
        System.out.println((gameDeleted) ? "Intermediate Game Data Deleted" : "Failed to Delete Game Data");
        boolean turnDeleted = intermediateTurnData.delete();
        System.out.println((turnDeleted) ? "Intermediate Turn Data Deleted" : "Failed to Delete Turn Data");
        boolean zipDeleted = zipData.delete();
        System.out.println((zipDeleted) ? "Intermediate Zip Data Deleted" : "Failed to Delete Zip Data");
        if (gameDeleted && turnDeleted && zipDeleted) System.out.println("Successfully cleaned up intermediate resources."); else {
            System.out.println("Failed to clean up intermediate resources.");
            System.out.println("Calling for delete-on-exit.");
            if (!gameDeleted) intermediateGameData.deleteOnExit(); else if (!turnDeleted) intermediateTurnData.deleteOnExit(); else if (!zipDeleted) zipData.deleteOnExit();
        }
        System.out.println("End clean up.");
    }
}
