package edu.byu.ece.rapidSmith.device;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import com.caucho.hessian.io.Deflation;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import edu.byu.ece.rapidSmith.design.Pin;
import edu.byu.ece.rapidSmith.device.helper.HashPool;
import edu.byu.ece.rapidSmith.device.helper.TileSinks;
import edu.byu.ece.rapidSmith.device.helper.TileSources;
import edu.byu.ece.rapidSmith.device.helper.TileWires;
import edu.byu.ece.rapidSmith.device.helper.WireArray;
import edu.byu.ece.rapidSmith.device.helper.WireArrayConnection;
import edu.byu.ece.rapidSmith.device.helper.WireHashMap;
import edu.byu.ece.rapidSmith.router.Node;
import edu.byu.ece.rapidSmith.util.FamilyType;
import edu.byu.ece.rapidSmith.util.FileTools;
import edu.byu.ece.rapidSmith.util.MessageGenerator;
import edu.byu.ece.rapidSmith.util.PartNameTools;

/**
 * This is the main class that stores information about each Xilinx part.  It contains
 * a 2D grid of Tiles which contain all the routing and primitive sites necessary for 
 * a placer and router.
 * @author Chris Lavin
 * Created on: Apr 22, 2010
 */
public class Device implements Serializable {

    /** Serializable Version */
    private static final long serialVersionUID = 6336619462392618775L;

    /** The current release of the tools */
    public static final String rapidSmithVersion = "0.5.1";

    /** This is the current device file version (saved in file to ensure proper compatibility) */
    public static final String deviceFileVersion = "0.4";

    /** A static reference to eliminate duplicate Device objects in memory */
    private static Device singleton = null;

    /** Number of rows of tiles in the device */
    protected int rows;

    /** Number of columns of tiles in the device */
    protected int columns;

    /** A 2D array of all the tiles in the device */
    protected Tile[][] tiles;

    /** The Xilinx part name of the device (ie. xc4vfx12ff668, omits the speed grade) */
    protected String partName;

    /** Keeps track of all the primitive instances on the device */
    protected HashMap<String, PrimitiveSite> primitiveSites;

    /** Keeps track of which Wire objects have a corresponding PIPRouteThrough */
    protected HashMap<WireConnection, PIPRouteThrough> routeThroughMap;

    /** A Map between a tile name (string) and its actual reference */
    protected HashMap<String, Tile> tileMap;

    /** Created on demand when user calls getPrimitiveSiteIndex(), where the ArrayList index is the ordinal of the PrimitiveType */
    private ArrayList<PrimitiveSite[]> primitiveSiteIndex;

    /** Created on demand when user calls getCompatibleSites(), where the ArrayList index is the ordinal of the PrimitiveType */
    private ArrayList<PrimitiveSite[]> compatibleSiteIndex;

    /** A set of all TileTypes that have switch matrices in them */
    private HashSet<TileType> switchMatrixTypes;

    /** Keeps track of each unique Wire object in the device */
    protected HashPool<WireConnection> wirePool;

    /** Keeps track of each unique Wire[] object in the device */
    protected HashPool<WireArray> wireArrayPool;

    /** Keeps track of each unique WireConnection object in the device */
    protected HashPool<WireArrayConnection> wireConnectionPool;

    /** Keeps track of all PIPRouteThrough objects */
    protected HashPool<PIPRouteThrough> routeThroughPool;

    /** Keeps Track of all unique Sinks that exist in Tiles */
    protected HashPool<TileSinks> tileSinksPool;

    /** Keeps Track of all unique Sources Lists that exist in Tiles */
    protected HashPool<TileSources> tileSourcesPool;

    /** Keeps Track of all unique Wire Lists that exist in Tiles */
    protected HashPool<TileWires> tileWiresPool;

    /** Keeps track of all unique primitive pin HashMaps */
    protected HashPool<PrimitivePinMap> primitivePinPool;

    /**
	 * Constructor, initializes all objects to new, except tile[][]
	 */
    public Device() {
        partName = null;
        primitiveSites = new HashMap<String, PrimitiveSite>();
        routeThroughMap = new HashMap<WireConnection, PIPRouteThrough>();
        switchMatrixTypes = null;
        primitiveSiteIndex = null;
        wirePool = new HashPool<WireConnection>();
        wireArrayPool = new HashPool<WireArray>();
        wireConnectionPool = new HashPool<WireArrayConnection>();
        routeThroughPool = new HashPool<PIPRouteThrough>();
        tileSinksPool = new HashPool<TileSinks>();
        tileSourcesPool = new HashPool<TileSources>();
        tileWiresPool = new HashPool<TileWires>();
        primitivePinPool = new HashPool<PrimitivePinMap>();
    }

    /**
	 * This method is intended to only be called by a special method, util.FileTools.loadDevice(). 
	 * This will either return a populated device of the same part if it already exists in memory
	 * as the singleton instance or a new device ready to be populated from a file.
	 * @param partName The part name of the device to get.
	 * @return If no device has been loaded or the part names do not match the singleton instance, it
	 * returns a new Device, otherwise it will return an existing copy of the device in memory.
	 */
    public static Device getInstance(String partName) {
        if (singleton == null || !partName.equals(singleton.getPartName())) {
            singleton = new Device();
        }
        return singleton;
    }

    /**
	 * Gets the corresponding wire enumerator object for this device.
	 * @return The wire enumerator for this device.
	 */
    public WireEnumerator getWireEnumerator() {
        return WireEnumerator.getInstance(getFamilyType());
    }

    /**
	 * Initializes the tile array and wire pool.  This is done after the tile dimensions have
	 * been parsed from the .xdlrc file.
	 */
    protected void createTileArray() {
        tiles = new Tile[this.rows][this.columns];
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                tiles[i][j] = new Tile();
                tiles[i][j].setColumn(j);
                tiles[i][j].setRow(i);
            }
        }
    }

    /**
	 * Checks if this wire is RouteThrough.
	 * @param w The wire to test.
	 * @return True if the wire is a routeThrough, false otherwise.
	 */
    public boolean isRouteThrough(WireConnection w) {
        return routeThroughMap.get(w) != null;
    }

    /**
	 * Gets the external wire enumeration on the instance pin. 
	 * @param pin The pin to get the external name from.
	 * @return The wire enumeration of the internal pin on the instance primitive of pin.
	 */
    public Integer getPrimitiveExternalPin(Pin pin) {
        String extName = PinMappingPatch.getPinMapping(pin.getInstance().getType(), pin.getName());
        if (extName != null) {
            return WireEnumerator.getInstance(getFamilyType()).getWireEnum(extName);
        }
        Integer extPin = pin.getInstance().getPrimitiveSite().getExternalPinWireEnum(pin.getName());
        if (extPin != null) {
            return extPin;
        }
        return null;
    }

    /**
	 * This will creating a routing node from the pin given.
	 * @param pin The pin to create the routing node from.
	 * @return A new node populated with the pin's tile and wire. The parent 
	 * field is null and level is zero.
	 */
    public Node getNodeFromPin(Pin pin) {
        Integer wire = getPrimitiveExternalPin(pin);
        if (wire == null) return null;
        return new Node(pin.getTile(), wire, null, 0);
    }

    /**
	 * This will creating a routing node from the pin given.
	 * @param pin The pin to create the routing node from.
	 * @return A new node populated with the pin's tile and wire. The parent 
	 * field is null and level is zero.
	 */
    public Node getNodeFromPin(Pin pin, Node parent, int level) {
        Integer wire = getPrimitiveExternalPin(pin);
        if (wire == null) return null;
        return new Node(pin.getTile(), wire, parent, level);
    }

    /**
	 * Gets the PIPRouteThrough object for a wire.
	 * @param w The wire which has a corresponding PIPRouteThrough
	 * @return The PIPRouteThrough or null if it does not exist.
	 */
    public PIPRouteThrough getRouteThrough(WireConnection w) {
        return routeThroughMap.get(w);
    }

    /**
	 * Gets the current tile in the device based on absolute row and column indices
	 * @param row The absolute row index (0 starting at top)
	 * @param column The absolute column index (0 starting at the left)
	 * @return The tile specified by row and column, if the indices are out of bounds it returns null
	 */
    public Tile getTile(int row, int column) {
        if (row < 0 || column < 0 || row > this.rows - 1 || column > this.columns - 1) {
            return null;
        }
        return tiles[row][column];
    }

    /**
	 * This will get a Tile by its name using a HashMap.
	 * @param tile The name of the tile to get.
	 * @return The tile with the name tile, or null if it does not exist.
	 */
    public Tile getTile(String tile) {
        return tileMap.get(tile);
    }

    /**
	 * Each tile in a device can be referenced by a unique integer which is a combination
	 * of its row and column index.  This will get and return the tile with the unique index
	 * provided.
	 * @param uniqueTileNumber The unique tile number of the tile to get.
	 * @return The tile with the uniqueTileNumber, or null if none exists.
	 */
    public Tile getTile(int uniqueTileNumber) {
        int row = uniqueTileNumber / columns;
        int col = uniqueTileNumber % columns;
        return getTile(row, col);
    }

    /**
	 * This gets and returns a primitive site on the device by name.
	 * @param name Name of the primitive site to get.
	 * @return The primitive site with the name, or null if none exists.
	 */
    public PrimitiveSite getPrimitiveSite(String name) {
        return this.primitiveSites.get(name);
    }

    /**
	 * A method to get the corresponding primitive site for current in a different tile.
	 * For example in a Virtex 4, there are 4 slices in a CLB tile, when moving a hard macro
	 * the current slice must go in the same spot in a new CLB tile
	 * @param current The current primitive site of the instance.
	 * @param newSiteTile The tile of the new proposed site.
	 * @return The corresponding site in tile newSite, or null if no corresponding site exists.
	 */
    public static PrimitiveSite getCorrespondingPrimitiveSite(PrimitiveSite current, PrimitiveType type, Tile newSiteTile) {
        if (newSiteTile == null) {
            return null;
        }
        if (newSiteTile.getPrimitiveSites() == null) {
            return null;
        }
        PrimitiveSite[] ps = current.tile.getPrimitiveSites();
        int idx = -1;
        for (int i = 0; i < ps.length; i++) {
            if (current.equals(ps[i])) {
                idx = i;
                break;
            }
        }
        if (idx == -1 || idx >= newSiteTile.getPrimitiveSites().length) {
            return null;
        }
        PrimitiveSite newSite = newSiteTile.getPrimitiveSites()[idx];
        if (!newSite.isCompatiblePrimitiveType(type)) {
            return null;
        }
        return newSite;
    }

    /**
	 * This will take a sink Pin from a design net and determine the
	 * final switch matrix and node or wire which the signal must be routed 
	 * through in order to reach the sink pin.
	 * @param pin The sink pin to find a switch matrix for.
	 * @return A node (a unique tile and wire) of where the signal must be 
	 * routed to reach the sink pin. Returns null, if none exists. 
	 */
    public Node getSwitchMatrixSink(Pin pin) {
        int extPin = getPrimitiveExternalPin(pin);
        Tile tile = pin.getInstance().getTile();
        SinkPin sp = tile.getSinks().get(extPin);
        if (sp == null) return null;
        int y = sp.switchMatrixTileOffset;
        int x = y >> 16;
        y = (y << 16) >> 16;
        Node n = new Node(getTile(tile.getRow() + y, tile.getColumn() + x), sp.switchMatrixSinkWire, null, 0);
        return n;
    }

    /**
	 * This will return a set of all unique TileTypes which are considered
	 * to have a switch matrix or routing switch box in them.
	 * @return A set of all TileTypes which have a switch matrix in them.
	 */
    public HashSet<TileType> getSwitchMatrixTypes() {
        if (switchMatrixTypes == null) {
            switchMatrixTypes = new HashSet<TileType>();
            switchMatrixTypes.add(TileType.INT);
            switchMatrixTypes.add(TileType.INT_SO);
            switchMatrixTypes.add(TileType.INT_SO_DCM0);
        }
        return switchMatrixTypes;
    }

    /**
	 * Gets and returns the number of rows of tiles in this device.
	 * @return the number of rows of tiles in the device.
	 */
    public int getRows() {
        return rows;
    }

    /**
	 * Sets the number of rows of tiles this device has.
	 * @param rows the number of rows of tiles to set in the device.
	 */
    protected void setRows(int rows) {
        this.rows = rows;
    }

    /**
	 * Gets and returns the number of columns of tiles in this device.
	 * @return the number of columns of tiles in the device.
	 */
    public int getColumns() {
        return columns;
    }

    /**
	 * Sets the number of columns of tiles this device has.
	 * @param columns the number of columns of tiles to set in the device
	 */
    protected void setColumns(int columns) {
        this.columns = columns;
    }

    /**
	 * Gets and returns this device's 2D array of tiles that define
	 * the layout of the FPGA.
	 * @return the tiles of this device.
	 */
    public Tile[][] getTiles() {
        return tiles;
    }

    /**
	 * Gets and return the partName (includes package but not speed grade) 
	 * of this device (ex: xc4vfx12ff668).
	 * @return the partName The part name of this device.
	 */
    public String getPartName() {
        return PartNameTools.removeSpeedGrade(partName);
    }

    /**
	 * Gets and returns the all lower case exact Xilinx family type for this  
	 * device (ex: qvirtex4 instead of virtex4). DO NOT use exact family 
	 * methods if it is to be used for accessing device or wire enumeration 
	 * files as RapidSmith does not generate files for devices that have 
	 * XDLRC compatible files.  
	 * @return The exact Xilinx family type for this device.
	 */
    public FamilyType getExactFamilyType() {
        return PartNameTools.getExactFamilyTypeFromPart(partName);
    }

    /**
	 * Gets and returns the base family type for this device. This
	 * ensures compatibility with all RapidSmith files. For differentiating
	 * family types (qvirtex4 rather than virtex4) use getExactFamilyType().
	 * @return The base family type of the part for this device.
	 */
    public FamilyType getFamilyType() {
        return PartNameTools.getFamilyTypeFromPart(partName);
    }

    /**
	 * Sets the part name of this device.  This part name should only have the package
	 * information but not speed grade (ex: xc4vfx12ff668).
	 * @param partName the partName to set.
	 */
    protected void setPartName(String partName) {
        this.partName = partName;
    }

    /**
	 * Gets and returns the HashMap of name to primitive site mappings for this device.
	 * @return The map of name to primitive site mappings for this device.
	 */
    public HashMap<String, PrimitiveSite> getPrimitiveSites() {
        return primitiveSites;
    }

    /**
	 * Gets and returns the HashMap of name to Tile mappings for this device.
	 * @return The tile mappings of name to Tile object for this device.
	 */
    public HashMap<String, Tile> getTileMap() {
        return tileMap;
    }

    /**
	 * Gets and returns the PIP route through map for this device.
	 * @return The mappings between wires and PIP route throughs.
	 */
    public HashMap<WireConnection, PIPRouteThrough> getRouteThroughMap() {
        return routeThroughMap;
    }

    /**
	 * This method will get (create if null) a data structure which stores all 
	 * of the device's primitive sites by type.  To get all of the primitive 
	 * sites of a particular type, use the PrimitiveType.ordinal() method to 
	 * get the representative integer and use that value to index into the 
	 * ArrayList.  This will return an array of all primitive sites of that 
	 * same type.  
	 * @return The data structure which stores all of the primitive sites 
	 * separated by type. 
	 */
    public ArrayList<PrimitiveSite[]> getPrimitiveSiteIndex() {
        if (primitiveSiteIndex == null) {
            createPrimitiveSiteIndex();
        }
        return primitiveSiteIndex;
    }

    /**
	 * This method will get (create if null) a data structure which stores all 
	 * of the compatible primitive sites for each primitive type.  To get all 
	 * the compatible primitive sites of a particular type, use the 
	 * PrimitiveType.ordinal() method to get the representative integer and use
	 * that value to index into the ArrayList.  This will return an array of 
	 * all compatible primitive sites of that same type.  
	 * @return The data structure which stores all compatible primitive sites 
	 * for each primitive type. 
	 */
    public ArrayList<PrimitiveSite[]> getCompatibleSiteIndex() {
        if (compatibleSiteIndex == null) {
            createCompatibleSiteIndex();
        }
        return compatibleSiteIndex;
    }

    /**
	 * This method will get all primitive sites with the same base type as
	 * that passed as a parameter type.  This does not get all
	 * compatible sites for the PrimitiveType type as a SLICEL request would
	 * only return all SLICEL type sites and no SLICEM types which can also 
	 * be a valid site for SLICELs.
	 * @param type The types of sites to retrieve.
	 * @return An array of primitive sites with the same Primitive type as type.
	 */
    public PrimitiveSite[] getAllPrimitiveSitesOfType(PrimitiveType type) {
        return getPrimitiveSiteIndex().get(type.ordinal());
    }

    /**
	 * This method will get all compatible primitive sites for a particular 
	 * primitive type in this device.  For example, a SLICEL can be placed at 
	 * all SLICEL sites AND all SLICEM sites.  If the type given were SLICEL, 
	 * this method would return an array of all SLICEL and SLICEM sites.
	 * @param type The type for which to find compatible primitive sites.
	 * @return An array of compatible sites suitable for placement of a 
	 * primitive of type type.
	 */
    public PrimitiveSite[] getAllCompatibleSites(PrimitiveType type) {
        int size = 0;
        ArrayList<PrimitiveSite[]> compatibleList = new ArrayList<PrimitiveSite[]>();
        PrimitiveSite[] match = getAllPrimitiveSitesOfType(type);
        if (match != null) {
            size += match.length;
            compatibleList.add(match);
        }
        PrimitiveType[] compatibleTypes = PrimitiveSite.compatibleTypesArray[getFamilyType().ordinal()].get(type);
        if (compatibleTypes != null) {
            for (PrimitiveType compatibleType : compatibleTypes) {
                match = getAllPrimitiveSitesOfType(compatibleType);
                if (match != null) {
                    size += match.length;
                    compatibleList.add(match);
                }
            }
        }
        if (compatibleList.size() == 0) {
            return null;
        }
        int i = 0;
        PrimitiveSite[] newArray = new PrimitiveSite[size];
        for (PrimitiveSite[] sites : compatibleList) {
            for (PrimitiveSite site : sites) {
                newArray[i] = site;
                i++;
            }
        }
        return newArray;
    }

    /**
	 * Gets and returns an array of all primitive sites of the given primitive type.
	 * @param type The primitive type of the site to get.
	 * @return An array of all primitive sites in the device with primitive type type.
	 */
    public PrimitiveSite[] getAllSitesOfType(PrimitiveType type) {
        return getPrimitiveSiteIndex().get(type.ordinal());
    }

    /**
	 * This will create a data structure which organizes all primitive sites by types.
	 * The outer ArrayList uses the PrimitiveType.ordinal() value as the index for
	 * each type of primitive site.
	 */
    private void createPrimitiveSiteIndex() {
        ArrayList<ArrayList<PrimitiveSite>> tmp = new ArrayList<ArrayList<PrimitiveSite>>(PrimitiveType.values().length);
        for (int i = 0; i < PrimitiveType.values().length; i++) {
            tmp.add(new ArrayList<PrimitiveSite>());
        }
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                PrimitiveSite[] sites = tiles[i][j].getPrimitiveSites();
                if (sites == null) continue;
                for (PrimitiveSite site : sites) {
                    tmp.get(site.getType().ordinal()).add(site);
                }
            }
        }
        ArrayList<PrimitiveSite[]> index = new ArrayList<PrimitiveSite[]>();
        for (ArrayList<PrimitiveSite> list : tmp) {
            if (list.size() == 0) {
                index.add(null);
            } else {
                PrimitiveSite[] tmpArray = new PrimitiveSite[list.size()];
                index.add(list.toArray(tmpArray));
            }
        }
        this.primitiveSiteIndex = index;
    }

    /**
	 * This will create a data structure which finds all compatible primitive 
	 * sites for a given primitive type.  It populates the compatibleSiteIndex
	 * where the outer ArrayList uses the PrimitiveType.ordinal() value as 
	 * the index of the type to get the compatible sites.
	 */
    private void createCompatibleSiteIndex() {
        int size = 0;
        compatibleSiteIndex = new ArrayList<PrimitiveSite[]>(PrimitiveType.values().length);
        for (PrimitiveType type : PrimitiveType.values()) {
            ArrayList<PrimitiveSite[]> compatibleList = new ArrayList<PrimitiveSite[]>();
            PrimitiveSite[] match = getAllPrimitiveSitesOfType(type);
            if (match != null) {
                size += match.length;
                compatibleList.add(match);
            }
            PrimitiveType[] compatibleTypes = PrimitiveSite.compatibleTypesArray[getFamilyType().ordinal()].get(type);
            if (compatibleTypes != null) {
                for (PrimitiveType compatibleType : compatibleTypes) {
                    match = getAllPrimitiveSitesOfType(compatibleType);
                    if (match != null) {
                        size += match.length;
                        compatibleList.add(match);
                    }
                }
            }
            if (compatibleList.size() == 0) {
                compatibleSiteIndex.set(type.ordinal(), null);
                continue;
            }
            int j = 0;
            PrimitiveSite[] newArray = new PrimitiveSite[size];
            for (PrimitiveSite[] sites : compatibleList) {
                for (PrimitiveSite site : sites) {
                    newArray[j] = site;
                    j++;
                }
            }
            compatibleSiteIndex.set(type.ordinal(), newArray);
        }
    }

    /**
	 * This method populated the tile map after parsing is complete.
	 * @param map A map necessary to make a link between names of tiles and objects.
	 */
    protected void populateTileMap(HashMap<String, Integer> map) {
        tileMap = new HashMap<String, Tile>();
        for (String name : map.keySet()) {
            Integer tileAddr = map.get(name);
            int row = tileAddr >> 16;
            int col = tileAddr & 0xFFFF;
            Tile t = tiles[row][col];
            tileMap.put(name, t);
        }
    }

    /**
	 * This method is used to rebuild the tile map when loading the device 
	 * from a device file.
	 */
    private void reconstructTileMap() {
        tileMap = new HashMap<String, Tile>();
        for (Tile[] tileArray : tiles) {
            for (Tile t : tileArray) {
                tileMap.put(t.getName(), t);
            }
        }
    }

    /**
	 * This method will iterate through all of the sink pins of the device and determine
	 * which switch matrix and wire node a routed path must pass through in order to 
	 * arrive at the sink.
	 * @param we The corresponding wire enumerator for this device.
	 */
    public void populateSinkPins(WireEnumerator we) {
        HashSet<Integer> setOfExternalPrimitivePins = new HashSet<Integer>();
        HashSet<TileType> switchMatrixTileTypes = getSwitchMatrixTypes();
        int watchDog = 0;
        for (String wire : we.getWires()) {
            int w = we.getWireEnum(wire);
            if (we.getWireType(w).equals(WireType.SITE_SINK)) {
                setOfExternalPrimitivePins.add(w);
            }
        }
        for (Tile[] tileArray : tiles) {
            for (Tile tile : tileArray) {
                if (switchMatrixTileTypes.contains(tile.getType())) {
                    if (tile.getWireHashMap() == null) continue;
                    for (Integer wire : tile.getWireHashMap().keySet()) {
                        if (we.getWireType(wire).equals(WireType.INT_SINK)) {
                            int currINTSinkWire = wire;
                            boolean debug = false;
                            if (tile.getName().equals("INT_X12Y1") && currINTSinkWire == we.getWireEnum("IMUX_B18")) {
                                debug = false;
                            }
                            for (WireConnection w : tile.getWireHashMap().get(wire)) {
                                if (w.getColumnOffset() != 0 || w.getRowOffset() != 0) {
                                    Stack<Tile> tileStack = new Stack<Tile>();
                                    Stack<WireConnection> wireStack = new Stack<WireConnection>();
                                    HashSet<Node> visited = new HashSet<Node>();
                                    tileStack.push(tile);
                                    wireStack.push(new WireConnection(wire, 0, 0, true));
                                    if (debug) System.out.println("PUSH: " + tile + " " + we.getWireName(wire));
                                    watchDog = 0;
                                    while (!tileStack.isEmpty() && watchDog < 100) {
                                        watchDog++;
                                        Tile t1 = tileStack.pop();
                                        WireConnection w1 = wireStack.pop();
                                        if (debug) System.out.println("  POP: " + t1 + " " + we.getWireName(w1.getWire()));
                                        WireConnection[] connections = t1.getWireHashMap().get(w1.getWire());
                                        if (connections == null || visited.contains(new Node(t1, w1.getWire(), null, 0))) {
                                            continue;
                                        }
                                        for (WireConnection wire2 : connections) {
                                            if (setOfExternalPrimitivePins.contains(wire2.getWire())) {
                                                SinkPin found = wire2.getTile(t1).getSinks().get(wire2.getWire());
                                                int xOffset = (tile.getColumn() - wire2.getTile(t1).getColumn());
                                                int yOffset = (tile.getRow() - wire2.getTile(t1).getRow());
                                                if (found == null) {
                                                    continue;
                                                }
                                                found.switchMatrixSinkWire = currINTSinkWire;
                                                found.switchMatrixTileOffset = (xOffset << 16) | (yOffset & 0xFFFF);
                                                if (debug) System.out.println("  FOUND: " + wire2.getTile(t1) + " " + we.getWireName(wire2.getWire()) + " xOffset=" + xOffset + " yOffset=" + yOffset);
                                                if (debug) System.out.println("  SECOND: " + wire2.getTile(t1) + " " + we.getWireName(wire2.getWire()));
                                                if (!visited.contains(new Node(t1, w1.getWire(), null, 0))) {
                                                    tileStack.push(wire2.getTile(t1));
                                                    wireStack.push(wire2);
                                                }
                                            } else if (wire2.getTile(t1) != null && !switchMatrixTileTypes.contains(wire2.getTile(t1).getType())) {
                                                if (debug) System.out.println("  POTENTIAL: " + wire2.getTile(t1) + " " + we.getWireName(wire2.getWire()));
                                                if (!visited.contains(new Node(t1, w1.getWire(), null, 0))) {
                                                    tileStack.push(wire2.getTile(t1));
                                                    wireStack.push(wire2);
                                                }
                                            }
                                        }
                                        visited.add(new Node(t1, w1.getWire(), null, 0));
                                    }
                                    tileStack.clear();
                                    wireStack.clear();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
	 * This finds all of the unique tile sink HashMaps.  This must be done at the
	 * end of parsing all the tiles because in order to get the switch matrix 
	 * sink wires, we must have a full routing map in place.
	 */
    protected void removeDuplicateTileSinks(WireEnumerator we) {
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.columns; j++) {
                tiles[i][j].setSinks(tileSinksPool.add(new TileSinks(tiles[i][j].getSinks())).sinks);
            }
        }
    }

    /**
	 * This function helps reduce memory footprint of the design parser by looking for duplicate
	 * objects after each tile object is created.
	 */
    protected void incrementalRemoveDuplicateTileResources(Tile t, WireEnumerator we) {
        t.setSources(tileSourcesPool.add(new TileSources(t.getSources())).sources);
        t.setWireHashMap(tileWiresPool.add(new TileWires(t.getWireHashMap())).wires);
        if (t.getWireHashMap() != null) removeDuplicateWireArrays(t);
    }

    protected void removeDuplicateWireArrays(Tile t) {
        for (Integer key : t.getWires()) {
            WireArray unique = wireArrayPool.add(new WireArray(t.getWireConnections(key)));
            t.getWireHashMap().put(key, unique.array);
        }
    }

    protected void removeDuplicatePrimitivePinMaps() {
        for (PrimitiveSite p : primitiveSites.values()) {
            PrimitivePinMap map = new PrimitivePinMap(p.getPins());
            p.setPins(primitivePinPool.add(map).pins);
        }
    }

    protected void createWireConnectionEnumeration() {
        for (int i = 0; i < getRows(); i++) {
            for (int j = 0; j < getColumns(); j++) {
                if (tiles[i][j].getWireHashMap() == null) continue;
                for (Integer wire : tiles[i][j].getWires()) {
                    WireArrayConnection tmp = new WireArrayConnection(wire, wireArrayPool.getEnumerationValue(new WireArray(tiles[i][j].getWireConnections(wire))));
                    wireConnectionPool.add(tmp);
                }
            }
        }
    }

    public void debugPoolCounts() {
        System.out.println("\n");
        MessageGenerator.printHeader("Unique Object Counts");
        debugPrintUniquePoolCount(wirePool, "Wires");
        debugPrintUniquePoolCount(wireArrayPool, "Wire[]s");
        debugPrintUniquePoolCount(wireConnectionPool, "WireConnections");
        debugPrintUniquePoolCount(routeThroughPool, "PIPRouteThroughs");
        debugPrintUniquePoolCount(tileSinksPool, "TileSinks");
        debugPrintUniquePoolCount(tileSourcesPool, "TileSources");
        debugPrintUniquePoolCount(tileWiresPool, "TileWires");
        debugPrintUniquePoolCount(primitivePinPool, "PrimitivePinMap");
    }

    private void debugPrintUniquePoolCount(@SuppressWarnings("rawtypes") HashPool p, String name) {
        System.out.printf("%10d : Unique %s\n", p.getEnumerations().size(), name);
    }

    private static void debugWritingSize(Hessian2Output hos, FileOutputStream fos, String variableName, Long[] locations) {
        try {
            hos.flush();
            locations[0] = fos.getChannel().position();
            System.out.printf("%10d bytes : %s\n", (locations[0] - locations[1]), variableName);
            locations[1] = locations[0];
        } catch (IOException e) {
            return;
        }
    }

    /**
	 * This function is used to write a compact version of the device to a file. The file can 
	 * only be read by using the corresponding function readDeviceFromCompactFile().
	 * @param fileName Name of the file to create and store data for the device in the compact format
	 * @return True if operation is successful, false otherwise.
	 */
    public boolean writeDeviceToCompactFile(String fileName) {
        for (Tile[] tiles : getTiles()) {
            for (Tile tile : tiles) {
                tile.setDevice(this);
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            Hessian2Output h2os = new Hessian2Output(fos);
            Deflation deflate = new Deflation();
            Hessian2Output hos = deflate.wrap(h2os);
            Long[] locations = new Long[2];
            locations[1] = fos.getChannel().position();
            System.out.println("\n");
            MessageGenerator.printHeader("File Usage Statistics");
            hos.writeString(deviceFileVersion);
            debugWritingSize(hos, fos, "deviceFileVersion", locations);
            hos.writeInt(rows);
            debugWritingSize(hos, fos, "tileRows", locations);
            hos.writeInt(columns);
            debugWritingSize(hos, fos, "tileColumns", locations);
            hos.writeInt(wirePool.getEnumerations().size());
            for (WireConnection w : wirePool.getEnumerations()) {
                int mask = w.isPIP() ? 0x80000000 : 0x0;
                hos.writeInt(mask | (w.getWire()));
                hos.writeInt((w.getRowOffset() << 16) | (w.getColumnOffset() & 0xFFFF));
            }
            debugWritingSize(hos, fos, "wirePool", locations);
            hos.writeInt(wireArrayPool.getEnumerations().size());
            for (WireArray wireArray : wireArrayPool.getEnumerations()) {
                hos.writeInt(wireArray.array.length);
                for (WireConnection w : wireArray.array) {
                    hos.writeInt(wirePool.getEnumerationValue(w));
                }
            }
            debugWritingSize(hos, fos, "wireArrayPool", locations);
            hos.writeInt(wireConnectionPool.getEnumerations().size());
            for (WireArrayConnection wc : wireConnectionPool.getEnumerations()) {
                hos.writeInt(wc.wire);
                hos.writeInt(wc.wireArrayEnum);
            }
            debugWritingSize(hos, fos, "wireConnectionPool", locations);
            hos.writeInt(tileSinksPool.getEnumerations().size());
            for (TileSinks s : tileSinksPool.getEnumerations()) {
                hos.writeInt(s.sinks.size());
                for (Integer key : s.sinks.keySet()) {
                    SinkPin sp = s.sinks.get(key);
                    hos.writeInt(key);
                    hos.writeInt(sp.switchMatrixSinkWire);
                    hos.writeInt(sp.switchMatrixTileOffset);
                }
            }
            debugWritingSize(hos, fos, "tileSinksPool", locations);
            hos.writeInt(tileSourcesPool.getEnumerations().size());
            for (TileSources s : tileSourcesPool.getEnumerations()) {
                FileTools.writeIntArray(hos, s.sources);
            }
            debugWritingSize(hos, fos, "tileSourcesPool", locations);
            hos.writeInt(tileWiresPool.getEnumerations().size());
            for (TileWires tw : tileWiresPool.getEnumerations()) {
                FileTools.writeWireHashMap(hos, tw.wires, wireArrayPool, wireConnectionPool);
            }
            debugWritingSize(hos, fos, "tileWiresPool", locations);
            int index = 0;
            String[] tileNames = new String[rows * columns];
            int[] tileTypes = new int[rows * columns];
            int[] tileSinks = new int[rows * columns];
            int[] tileSources = new int[rows * columns];
            int[] tileWires = new int[rows * columns];
            int[] primitiveSitesCount = new int[rows * columns];
            for (Tile[] tileArray : tiles) {
                for (Tile t : tileArray) {
                    tileNames[index] = t.getName();
                    tileTypes[index] = t.getType().ordinal();
                    tileSinks[index] = tileSinksPool.getEnumerationValue(new TileSinks(t.getSinks()));
                    tileSources[index] = tileSourcesPool.getEnumerationValue(new TileSources(t.getSources()));
                    tileWires[index] = tileWiresPool.getEnumerationValue(new TileWires(t.getWireHashMap()));
                    primitiveSitesCount[index] = t.getPrimitiveSites() == null ? 0 : t.getPrimitiveSites().length;
                    index++;
                }
            }
            FileTools.writeStringArray(hos, tileNames);
            FileTools.writeIntArray(hos, tileTypes);
            FileTools.writeIntArray(hos, tileSinks);
            FileTools.writeIntArray(hos, tileSources);
            FileTools.writeIntArray(hos, tileWires);
            FileTools.writeIntArray(hos, primitiveSitesCount);
            debugWritingSize(hos, fos, "tiles[][]", locations);
            hos.writeString(partName);
            debugWritingSize(hos, fos, "partName", locations);
            hos.writeInt(primitivePinPool.getEnumerations().size());
            for (PrimitivePinMap map : primitivePinPool.getEnumerations()) {
                FileTools.writeHashMap(hos, map.pins);
            }
            debugWritingSize(hos, fos, "primitivePinPool", locations);
            hos.writeInt(primitiveSites.values().size());
            for (Tile[] tileArray : tiles) {
                for (Tile t : tileArray) {
                    if (t.getPrimitiveSites() != null) {
                        for (PrimitiveSite p : t.getPrimitiveSites()) {
                            FileTools.writePrimitiveSite(hos, p, this, primitivePinPool);
                        }
                    }
                }
            }
            debugWritingSize(hos, fos, "primitives", locations);
            hos.writeInt(routeThroughMap.size());
            for (WireConnection w : routeThroughMap.keySet()) {
                PIPRouteThrough p = routeThroughMap.get(w);
                hos.writeInt(p.getType().ordinal());
                hos.writeInt(p.getInWire());
                hos.writeInt(p.getOutWire());
                hos.writeInt(wirePool.getEnumerationValue(w));
            }
            debugWritingSize(hos, fos, "routeThroughMap", locations);
            System.out.println("------------------------------------------");
            System.out.printf("%10d bytes : %s\n\n", (fos.getChannel().position()), "Total");
            hos.close();
            fos.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
	 * This reads from the compact device file to populate all of the essential variables of this 
	 * device.  It can only be used to read files generated with the writeDeviceToCompactFile() method.
	 * @param fileName The name of the compact device file
	 * @return True if operation was successful, false otherwise.
	 */
    public boolean readDeviceFromCompactFile(String fileName) {
        try {
            Hessian2Input his = FileTools.getInputStream(fileName);
            int size;
            String check = his.readString();
            if (!check.equals(deviceFileVersion)) {
                MessageGenerator.briefErrorAndExit("Error, the current version " + "of RAPIDSMITH is not compatible with the device " + "file(s) present on this installation.  Delete the 'device' " + "directory and run the Installer again to regenerate new " + "device files.\nCurrent RAPIDSMITH device file " + "version: " + deviceFileVersion + ", existing device file " + "version: " + check + ".");
            }
            rows = his.readInt();
            columns = his.readInt();
            WireConnection[] wires = new WireConnection[his.readInt()];
            for (int i = 0; i < wires.length; i++) {
                int part1 = his.readInt();
                int part2 = his.readInt();
                wires[i] = new WireConnection(0x7FFFFFFF & part1, part2 >> 16, (part2 << 16) >> 16, (part1 & 0x80000000) == 0x80000000);
            }
            size = his.readInt();
            ArrayList<WireConnection[]> wireArrays = new ArrayList<WireConnection[]>(size);
            for (int i = 0; i < size; i++) {
                int len = his.readInt();
                WireConnection[] tmp = new WireConnection[len];
                for (int j = 0; j < len; j++) {
                    tmp[j] = wires[his.readInt()];
                }
                wireArrays.add(tmp);
            }
            Integer[] allInts = new Integer[getFamilyWireCount(fileName)];
            for (int i = 0; i < allInts.length; i++) {
                allInts[i] = new Integer(i);
            }
            size = his.readInt();
            ArrayList<WireArrayConnection> wireConnections = new ArrayList<WireArrayConnection>();
            for (int i = 0; i < size; i++) {
                wireConnections.add(new WireArrayConnection(his.readInt(), his.readInt()));
            }
            size = his.readInt();
            ArrayList<HashMap<Integer, SinkPin>> sinks = new ArrayList<HashMap<Integer, SinkPin>>();
            for (int i = 0; i < size; i++) {
                int length = his.readInt();
                HashMap<Integer, SinkPin> tmp = new HashMap<Integer, SinkPin>();
                for (int j = 0; j < length; j++) {
                    tmp.put(allInts[his.readInt()], new SinkPin(his.readInt(), his.readInt()));
                }
                sinks.add(tmp);
            }
            size = his.readInt();
            ArrayList<int[]> sources = new ArrayList<int[]>();
            for (int i = 0; i < size; i++) {
                sources.add(FileTools.readIntArray(his));
            }
            size = his.readInt();
            ArrayList<WireHashMap> wireMaps = new ArrayList<WireHashMap>(size);
            for (int i = 0; i < size; i++) {
                wireMaps.add(FileTools.readWireHashMap(his, wireArrays, wireConnections));
            }
            createTileArray();
            tileMap = new HashMap<String, Tile>();
            String[] tileNames = FileTools.readStringArray(his);
            int[] tileTypes = FileTools.readIntArray(his);
            int[] tileSinks = FileTools.readIntArray(his);
            int[] tileSources = FileTools.readIntArray(his);
            int[] tileWires = FileTools.readIntArray(his);
            int[] primitiveSiteCount = FileTools.readIntArray(his);
            TileType[] typeValues = TileType.values();
            int index = 0;
            for (Tile[] tileArray : tiles) {
                for (Tile t : tileArray) {
                    t.setName(tileNames[index]);
                    t.setType(typeValues[tileTypes[index]]);
                    t.setSinks(sinks.get(tileSinks[index]));
                    t.setSources(sources.get(tileSources[index]));
                    t.setWireHashMap(wireMaps.get(tileWires[index]));
                    tileMap.put(tileNames[index], t);
                    t.setDevice(this);
                    index++;
                }
            }
            partName = his.readString();
            size = his.readInt();
            ArrayList<HashMap<String, Integer>> primitivePinMaps = new ArrayList<HashMap<String, Integer>>();
            for (int i = 0; i < size; i++) {
                primitivePinMaps.add(FileTools.readHashMap(his, allInts));
            }
            size = his.readInt();
            PrimitiveType[] typeValues2 = PrimitiveType.values();
            int idx = 0;
            int zeros = 0;
            for (Tile[] tileArray : tiles) {
                for (Tile t : tileArray) {
                    if (primitiveSiteCount[idx] == 0) {
                        t.setPrimitiveSites(null);
                        zeros++;
                    } else {
                        PrimitiveSite[] p = new PrimitiveSite[primitiveSiteCount[idx]];
                        for (int i = 0; i < primitiveSiteCount[idx]; i++) {
                            p[i] = FileTools.readPrimitiveSite(his, this, primitivePinMaps, typeValues2);
                            primitiveSites.put(p[i].getName(), p[i]);
                        }
                        t.setPrimitiveSites(p);
                    }
                    idx++;
                }
            }
            size = his.readInt();
            for (int i = 0; i < size; i++) {
                PIPRouteThrough prt = new PIPRouteThrough(typeValues2[his.readInt()], his.readInt(), his.readInt());
                routeThroughMap.put(wires[his.readInt()], prt);
            }
            reconstructTileMap();
            his.close();
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
	 * This method is used only for debugging purposes.
	 * @param fileName Name of the debugging file.
	 */
    public void writeToDebugFile(String fileName, WireEnumerator we) {
        String nl = System.getProperty("line.separator");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
            bw.write(this.partName + nl);
            WireConnection[] wires = new WireConnection[routeThroughMap.keySet().size()];
            wires = routeThroughMap.keySet().toArray(wires);
            Arrays.sort(wires);
            for (WireConnection w : wires) {
                bw.write("RT: " + w.toString(we) + " " + getRouteThrough(w).toString(we) + nl);
            }
            for (Tile[] tileArray : tiles) {
                for (Tile t : tileArray) {
                    bw.write("Tile: " + t.getName() + " " + t.getColumn() + " " + t.getRow() + " " + nl);
                    if (t.getPrimitiveSites() != null) {
                        for (PrimitiveSite ps : t.getPrimitiveSites()) {
                            bw.write("  PrimitiveSite: " + ps.toString() + " " + ps.getType() + nl);
                            HashMap<String, Integer> tmp = ps.getPins();
                            String[] keys = new String[tmp.size()];
                            keys = tmp.keySet().toArray(keys);
                            Arrays.sort(keys);
                            for (String key : keys) {
                                bw.write("    " + key + " " + we.getWireName(tmp.get(key)) + nl);
                            }
                        }
                    }
                    HashMap<Integer, SinkPin> tmp = t.getSinks();
                    Integer[] keys = new Integer[tmp.size()];
                    keys = tmp.keySet().toArray(keys);
                    Arrays.sort(keys);
                    for (Integer key : keys) {
                        if (key == -1) {
                            bw.write("  Sink: -1" + nl);
                        } else {
                            bw.write("  Sink: " + we.getWireName(key) + " ");
                            if (tmp.get(key).switchMatrixSinkWire == -1) {
                                bw.write("-1" + nl);
                            } else {
                                bw.write(tmp.get(key).toString(we) + nl);
                            }
                        }
                    }
                    int[] srcKeys = t.getSources();
                    if (srcKeys != null) {
                        Arrays.sort(srcKeys);
                        for (int key : srcKeys) {
                            bw.write("  Source: " + we.getWireName(key) + nl);
                        }
                    }
                    WireHashMap tmp2 = t.getWireHashMap();
                    if (tmp2 != null) {
                        Integer[] wireKeys = new Integer[tmp2.size()];
                        wireKeys = tmp2.keySet().toArray(wireKeys);
                        Arrays.sort(wireKeys);
                        for (Integer key : wireKeys) {
                            bw.write("  Wire: " + we.getWireName(key) + nl);
                            WireConnection[] connections = tmp2.get(key);
                            Arrays.sort(connections);
                            for (WireConnection w : connections) {
                                bw.write("    -> " + w.toString(we) + nl);
                            }
                        }
                    }
                }
            }
            bw.close();
        } catch (IOException e) {
            MessageGenerator.briefErrorAndExit("Error writing device debug file");
        }
    }

    private int getFamilyWireCount(String fileName) {
        int end = fileName.lastIndexOf(File.separator);
        int start = fileName.lastIndexOf(File.separator, end - 1);
        FamilyType familyType = FamilyType.valueOf(fileName.substring(start + 1, end).toUpperCase());
        switch(familyType) {
            case KINTEX7:
                return 42123;
            case SPARTAN2:
                return 4041;
            case SPARTAN2E:
                return 4693;
            case SPARTAN3:
                return 3901;
            case SPARTAN3A:
                return 6284;
            case SPARTAN3ADSP:
                return 8842;
            case SPARTAN3E:
                return 6745;
            case SPARTAN6:
                return 46932;
            case VIRTEX:
                return 4081;
            case VIRTEX2:
                return 5283;
            case VIRTEX2P:
                return 23497;
            case VIRTEX4:
                return 57631;
            case VIRTEX5:
                return 72523;
            case VIRTEX6:
                return 46781;
            case VIRTEX7:
                return 53331;
            case VIRTEXE:
                return 4224;
            default:
                return 0;
        }
    }
}
