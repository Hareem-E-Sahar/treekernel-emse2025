package meadow.server;

import meadow.server.eval.*;
import meadow.common.*;
import meadow.common.units.*;
import java.awt.Rectangle;
import java.util.Observer;
import java.util.Observable;
import java.util.Date;
import java.util.BitSet;
import java.util.List;
import java.util.ArrayList;
import java.io.*;

/** Base class for classes which control server-side game creation. A Gatherer is used to collect
the network connections, resulting in an array of MasterIOs. These IOs are then used to create a
new MasterSimulation.
<p>This class needs quite a bit of work: it is limited in what it can do, and few options are 
available at the moment. The supporting infrastructure just isn't there.
@see MasterIO
@see Gatherer
@see MasterSimulation*/
abstract class UI implements Observer {

    protected static final String STD_PORT = "5000", STD_PLAYERS = "2", STD_CRATES = "20", STD_UNITS = "2";

    protected MasterSimulation sim;

    protected Gatherer gatherer;

    protected int portNum, numPlayers, crateFreq, numUnits;

    protected String mapName;

    abstract void handleInfo(String s);

    abstract void handleMisc(String s);

    abstract void showMessage(String s);

    abstract void handleStop();

    void readMapData(String filename, List features) throws IOException, ClassNotFoundException {
        FileInputStream file = null;
        ObjectInputStream in = null;
        try {
            file = new FileInputStream(filename);
            in = new ObjectInputStream(file);
            List newFeatures = (List) in.readObject();
            features.clear();
            features.addAll(newFeatures);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
            }
        }
    }

    /** Figure out starting position for the each player's first unit, and the relative
	displacement for each subsequent, then call layoutUnits.
	Warning: this method fails silently when numPlayers>8 */
    private Group layoutPlayers(int numPlayers, int numUnits, TerrainModel terrain) {
        final int margin = 50;
        final int separation = 30;
        int left = margin;
        int right = terrain.getWidth() - margin;
        int top = margin;
        int bottom = terrain.getHeight() - margin;
        int centre = (left + right) / 2;
        int mid = (top + bottom) / 2;
        Group result = new Group();
        result.add(new Resource(100, 300, 0, 20, 1000, terrain));
        if (numPlayers > 0) layoutUnits(0, numUnits, left, top, separation, separation, result, terrain);
        if (numPlayers > 1) layoutUnits(1, numUnits, right, bottom, -separation, -separation, result, terrain);
        if (numPlayers > 2) layoutUnits(2, numUnits, right, top, -separation, separation, result, terrain);
        if (numPlayers > 3) layoutUnits(3, numUnits, left, bottom, separation, -separation, result, terrain);
        if (numPlayers > 4) layoutUnits(4, numUnits, centre, top, 0, separation, result, terrain);
        if (numPlayers > 5) layoutUnits(5, numUnits, centre, bottom, 0, -separation, result, terrain);
        if (numPlayers > 6) layoutUnits(6, numUnits, right, mid, -separation, 0, result, terrain);
        if (numPlayers > 7) layoutUnits(7, numUnits, left, mid, separation, 0, result, terrain);
        return result;
    }

    private void layoutUnits(int player, int numUnits, int x0, int y0, int dx, int dy, Group g, TerrainModel terrainModel) {
        for (int i = 0; i < numUnits; i++) {
            g.add(UnitFactory.createFromType(UnitFactory.REGULAR, x0 + (i * dx), y0 + (i * dy), player, terrainModel));
        }
    }

    void startSim(MasterIO[] ios) {
        BitSet prey = new BitSet();
        prey.set(0);
        List modifiers = new ArrayList();
        modifiers.add(new CrateHandler(crateFreq));
        HierarchicalTerrainModel terrain = new HierarchicalTerrainModel();
        Group startingPieces;
        if (mapName == null || mapName.equals("")) {
            terrain.features.add(new TerrainFeature(new Rectangle(0, 0, 80 * 16, 60 * 16), TerrainModel.GRASS));
            terrain.features.add(new TerrainFeature(new Ellipse(32 * 16, 16 * 16, 32 * 16, 24 * 16), TerrainModel.WATER));
            startingPieces = layoutPlayers(numPlayers, numUnits, terrain);
        } else {
            startingPieces = new Group();
            try {
                List features = new ArrayList();
                readMapData(mapName, features);
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
        sim = new MasterSimulation(this, ios, startingPieces, terrain, new SoleSurviverEvaluator(), new SimpleOrderQueue(), modifiers, 120);
        sim.start();
    }

    public void update(Observable o, Object arg) {
        if (o == gatherer) {
            if (arg != null) {
                startSim((MasterIO[]) arg);
                showMessage(new Date() + ": Game Started.\n");
            } else {
                gatherer = null;
                handleStop();
            }
        } else if (((String) arg).startsWith("Num")) {
            handleInfo((String) arg);
        } else {
            handleMisc(new Date() + ": " + arg + "\n");
        }
    }

    int parseInt(String value, int minimum, int maximum, String fieldName) throws IllegalArgumentException {
        try {
            int num = Integer.parseInt(value);
            if (num >= minimum && num <= maximum) {
                return num;
            } else {
                showMessage(fieldName + " out of bounds - legal values are " + minimum + "..." + maximum);
                throw new IllegalArgumentException();
            }
        } catch (NumberFormatException ex) {
            showMessage(value + " is not a valid integer value for " + fieldName);
            throw new IllegalArgumentException();
        }
    }

    boolean parseData(String portNum, String numPlayers, String crateFreq, String numUnits) {
        try {
            this.portNum = parseInt(portNum, 0, 65536, "Port number");
            this.numPlayers = parseInt(numPlayers, 2, 8, "Number of players");
            this.crateFreq = parseInt(crateFreq, 0, 1000, "Crate Frequency");
            this.numUnits = parseInt(numUnits, 1, 50, "Starting units");
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    void quit() {
        System.exit(0);
    }
}
