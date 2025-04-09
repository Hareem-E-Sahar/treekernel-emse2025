package net.sourceforge.freecol.server;

import java.lang.Math;
import java.util.*;
import net.sourceforge.freecol.common.FreeColException;
import net.sourceforge.freecol.common.Map;
import net.sourceforge.freecol.common.IndianSettlement;
import net.sourceforge.freecol.common.Tile;
import net.sourceforge.freecol.common.Unit;
import net.sourceforge.freecol.common.Map.Position;

public final class MapGenerator {

    private static final int MAX_SMOOTH_HEIGHT_PASSES = 10000;

    private static final int MAX_FOREST_TRIES = 1000000;

    private static final int BONUS_RATE_PERCENT = 5;

    private static final int UNKNOWN_LAND = 0;

    private static final int VISITED_LAND = 1;

    private static final int CLAIMED_LAND = 2;

    private ServerGame game;

    private int size;

    private int width;

    private int height;

    private int landPercentage;

    private int hillsPercentage;

    private int mountainPercentage;

    private int forestPercentage;

    private int warmBeltSize;

    private int humidityStartingPoint;

    private int smoothHeightPasses;

    private MapValues heightMap;

    private MapValues humidityMap;

    private MapValues indianSettlementMap;

    private Random random;

    private ServerMap map = null;

    private int forestTiles;

    /**
     * The constructor that provides all options.
     * @param game The game object
     * @param size Desired map size
     * @param landPercentage Percentage of world to make land
     * @param humidity Basic humidity
     * @param warmBeltSize Percentage of equator to make warm belt
     * @param smoothHeightPasses Number of passes of height smoothing desired
     * @throws FreeColException If unsupported map size selected
     */
    public MapGenerator(ServerGame game, int size, int landPercentage, int humidity, int warmBeltSize, int smoothHeightPasses) throws FreeColException {
        this.game = game;
        this.size = size;
        if (size == Map.SMALL) {
            width = 30;
            height = 64;
        } else if (size == Map.MEDIUM) {
            width = 60;
            height = 128;
        } else if (size == Map.LARGE) {
            width = 120;
            height = 256;
        } else if (size == Map.HUGE) {
            width = 240;
            height = 512;
        } else throw new FreeColException("Invalid map size argument");
        this.landPercentage = landPercentage;
        this.humidityStartingPoint = humidity;
        this.warmBeltSize = warmBeltSize;
        this.smoothHeightPasses = smoothHeightPasses;
        hillsPercentage = 8;
        mountainPercentage = 5;
        forestPercentage = 55;
    }

    /**
     * The default constructor, which uses defaults for all settings.
     * @param game The game object
     */
    public MapGenerator(ServerGame game) {
        this.game = game;
        size = Map.MEDIUM;
        width = 60;
        height = 128;
        landPercentage = 50;
        hillsPercentage = 8;
        mountainPercentage = 25;
        forestPercentage = 55;
        humidityStartingPoint = 80;
        warmBeltSize = 0;
        smoothHeightPasses = 0;
    }

    /**
     * Creates the world map for a new game, and places the initial
     * points for all players.
     * @param players List of ServerPlayer objects for the players
     * @throws FreeColException if thrown by a called method
     */
    public ServerMap createWorld(Vector players) throws FreeColException {
        map = new ServerMap(size);
        initialize();
        calculateLandHeights();
        createLand();
        calculateHumidity();
        assignTerrainTypes();
        plantForests();
        freezePolarRegions();
        addSeaLanes();
        createBonuses();
        createIndianSettlements();
        createEuropeanUnits(players);
        return map;
    }

    /**
     * Initializes internal objects for map creation.
     */
    private void initialize() {
        heightMap = new MapValues(width, height);
        humidityMap = new MapValues(width, height);
        indianSettlementMap = new MapValues(width, height);
        indianSettlementMap.fill(UNKNOWN_LAND);
        random = new Random();
    }

    /**
     * Calculate land heights for all tiles. The land height determines if
     * a tile becomes land or ocean.
     */
    private void calculateLandHeights() {
        int value;
        Iterator iterator = map.getWholeMapIterator();
        while (iterator.hasNext()) {
            Position position = (Position) iterator.next();
            int x = position.getX();
            int y = position.getY();
            if (x > 3 && x < width - 6 && y > 3 && y < height - 4) {
                value = random.nextInt(40) + ((500 - Math.abs(height / 2 - y)) / 10);
            } else {
                value = 0;
            }
            heightMap.set(x, y, value);
        }
        int numPasses = MAX_SMOOTH_HEIGHT_PASSES;
        for (int i = 0; i < numPasses; i++) {
            heightMap.increment(random.nextInt(width - 9) + 3, random.nextInt(height - 7) + 3, random.nextInt(5000));
            if (smoothHeightPasses != 0 && ((numPasses / smoothHeightPasses) != 0) && (i % (numPasses / smoothHeightPasses)) == 0) smoothHeightMap();
        }
        heightMap.normalize();
    }

    /**
     * Smooth the height map, level out differences in height.
     */
    private void smoothHeightMap() {
        MapValues heights = new MapValues(heightMap);
        Iterator iterator = map.getWholeMapIterator();
        while (iterator.hasNext()) {
            Position position = (Position) iterator.next();
            int x = position.getX();
            int y = position.getY();
            if (x > 3 && x < width - 6 && y > 3 && y < height - 4) {
                int sum = heightMap.get(x, y) * 2;
                int n = 2;
                Iterator adjacentIterator = map.getAdjacentIterator(position);
                while (adjacentIterator.hasNext()) {
                    Position adjacentPos = (Position) adjacentIterator.next();
                    sum += heightMap.get(adjacentPos);
                    n++;
                }
                sum += random.nextInt(61) - 30;
                heights.set(x, y, (sum <= 0 ? 0 : sum / n));
            }
        }
        heightMap = heights;
    }

    /**
     * Create land based on land beight.
     * @throws FreeColException if thrown by a called method
     */
    private void createLand() throws FreeColException {
        int maxHeight = heightMap.getMax();
        int landThreshold = (maxHeight * landPercentage) / 100;
        int total = (width * height * landPercentage) / 100;
        int count;
        for (int i = 0; i < 100; i++) {
            count = heightMap.countLargerThan(landThreshold);
            if (Math.abs(total - count) > (5 * total / 100)) {
                if (count > total) {
                    landThreshold *= 11;
                } else {
                    landThreshold *= 9;
                }
                landThreshold /= 10;
            } else {
                break;
            }
        }
        total = (total * (hillsPercentage + mountainPercentage)) / 100;
        int hillThreshold = landThreshold + (maxHeight - landThreshold) / 4;
        for (int i = 0; i < 100; i++) {
            count = heightMap.countLargerThan(hillThreshold);
            if (Math.abs(total - count) > (total / 100)) {
                if (count > total) hillThreshold *= 11; else hillThreshold *= 9;
                hillThreshold /= 10;
            } else {
                break;
            }
        }
        total = (width * height * landPercentage * mountainPercentage) / 10000;
        int mountainThreshold = hillThreshold + (maxHeight - hillThreshold) / 2;
        for (int i = 0; i < 100; i++) {
            count = heightMap.countLargerThan(mountainThreshold);
            if (Math.abs(total - count) > (total / 100)) {
                if (count > total) {
                    mountainThreshold *= 11;
                } else {
                    mountainThreshold *= 9;
                }
                mountainThreshold /= 10;
            } else {
                break;
            }
        }
        Iterator iterator = map.getWholeMapIterator();
        while (iterator.hasNext()) {
            Position position = (Position) iterator.next();
            int thisHeight = heightMap.get(position);
            int terrainType;
            if (thisHeight < landThreshold) {
                terrainType = Tile.OCEAN;
                heightMap.set(position, 0);
            } else if (height < hillThreshold) {
                terrainType = Tile.GRASSLANDS;
            } else if (height < mountainThreshold) {
                terrainType = Tile.GRASSLANDS;
            } else {
                terrainType = Tile.GRASSLANDS;
            }
            Tile theTile = map.getTile(position);
            theTile.setType(terrainType);
        }
    }

    /**
     * Calculate humidity for all tiles. This should be done when land has been
     * created, as humidity is set to 100% for ocean.
     * @throws FreeColException if thrown by a called method
     */
    private void calculateHumidity() throws FreeColException {
        Iterator iterator = map.getWholeMapIterator();
        while (iterator.hasNext()) {
            Position position = (Position) iterator.next();
            int humidity;
            Tile tile = map.getTile(position);
            int tileType = tile.getType();
            if (tileType == Tile.OCEAN) {
                humidity = 100;
            } else {
                int distanceToWater = map.getDistanceToWater(position.getX(), position.getY(), 5);
                if (distanceToWater > 5) distanceToWater = 5;
                humidity = random.nextInt(humidityStartingPoint - (distanceToWater * 3));
            }
            humidityMap.set(position, humidity);
        }
        smoothHumidityMap();
        smoothHumidityMap();
    }

    /**
     * Smooth the humidity map, level out differences and transfer water
     * from high areas to lowlands.
     */
    private void smoothHumidityMap() {
        MapValues newHumidityMap = new MapValues(humidityMap);
        Iterator iterator = map.getWholeMapIterator();
        while (iterator.hasNext()) {
            Position position = (Position) iterator.next();
            int humidity = humidityMap.get(position);
            if (humidity != 100) {
                int n = 3;
                int sum = humidity * n;
                Iterator adjIterator = map.getBorderAdjacentIterator(position);
                while (adjIterator.hasNext()) {
                    Position position2 = (Position) adjIterator.next();
                    int otherHumidity = humidityMap.get(position2);
                    if (heightMap.get(position) < heightMap.get(position2)) {
                        sum += (otherHumidity - humidity) / 2;
                    } else if (heightMap.get(position) > heightMap.get(position2)) {
                        sum -= (otherHumidity - humidity) / 2;
                    }
                    sum += otherHumidity;
                    n++;
                }
                sum /= n;
                newHumidityMap.set(position, (sum <= 0) ? 0 : (sum > 98 ? 98 : sum));
            }
        }
        humidityMap = newHumidityMap;
    }

    /**
     * Replace some of the obiquitous grassland with other terrain types
     * based on humidity, height, and distance to the equator and poles.
     * @throws FreeColException if thrown by a called method
     */
    private void assignTerrainTypes() throws FreeColException {
        int warmBeltLimit = ((height / 2) * warmBeltSize) / 100;
        Iterator iterator = map.getWholeMapIterator();
        while (iterator.hasNext()) {
            Position position = (Position) iterator.next();
            Tile tile = map.getTile(position);
            if (tile.getType() == Tile.GRASSLANDS) {
                int humidity = humidityMap.get(position);
                if (humidity < 25) {
                    if (Math.abs(height / 2) - position.getY() < warmBeltLimit) {
                        tile.setType(Tile.DESERT);
                    } else {
                        tile.setType(Tile.TUNDRA);
                    }
                } else if (humidity > 65) {
                    if (Math.abs(height / 2) - position.getY() < warmBeltLimit) {
                        tile.setType(random.nextInt(3) > 0 ? Tile.SWAMP : Tile.MARSH);
                    } else {
                        tile.setType(random.nextInt(3) > 0 ? Tile.MARSH : Tile.SWAMP);
                    }
                } else if (humidity > 45) {
                    if (Math.abs(height / 2) - position.getY() < warmBeltLimit) {
                        tile.setType(random.nextInt(3) > 0 ? Tile.SAVANNAH : Tile.GRASSLANDS);
                    } else {
                        tile.setType(random.nextInt(4) > 0 ? Tile.GRASSLANDS : Tile.PLAINS);
                    }
                } else {
                    tile.setType(random.nextInt(humidity) < 20 ? Tile.PRAIRIE : Tile.PLAINS);
                }
            }
        }
    }

    /**
     * Grow forests until the forest percentage is filled.
     * @throws FreeColException if thrown by a called method
     */
    private void plantForests() throws FreeColException {
        int forestSizeInTiles = (width * height * landPercentage * forestPercentage) / 10000;
        forestTiles = 0;
        for (int i = 0; forestTiles < forestSizeInTiles && i < MAX_FOREST_TRIES; i++) {
            int x = random.nextInt(width - 4) + 2;
            int y = random.nextInt(height - 4) + 2;
            Tile tile = map.getTile(x, y);
            if (tile.isLand() && !tile.isForested()) {
                plantForest(x, y, heightMap.get(x, y), 25);
            }
        }
    }

    /**
     * Plants a forest in one tile and spreads it out recursively.
     * @param x X of start of new forest
     * @param y Y of start of new forest
     * @param height Height of starting tile of new forest
     * @param diff How much height can be different and still have spread
     * @throws FreeColException if thrown by a called method
     */
    private void plantForest(int x, int y, int height, int diff) throws FreeColException {
        if (y > 2 && y < height - 2) {
            Tile tile = map.getTile(x, y);
            if (tile.isLand() && !tile.isForested()) {
                tile.setForested(true);
                forestTiles++;
                if (Math.abs(heightMap.get(x, y) - height) < diff) {
                    Position position = new Map.Position(x, y);
                    Iterator iterator = map.getAdjacentIterator(position);
                    while (iterator.hasNext()) {
                        Position nextPosition = (Position) iterator.next();
                        if (random.nextInt(1) > 0) {
                            plantForest(nextPosition.getX(), nextPosition.getY(), height, diff - 5);
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds arctics to the polar caps and turns some of the land in the
     * northern and southern parts to tundra.
     * @throws FreeColException if thrown by a called method
     */
    private void freezePolarRegions() throws FreeColException {
        for (int x = 0; x < width; x++) {
            map.setType(x, 0, Tile.ARCTIC);
            map.setType(x, height - 2, Tile.ARCTIC);
            map.setType(x, height - 1, Tile.ARCTIC);
        }
    }

    /**
     * Adds sea lanes (high sea) to the outskirts of the map.
     * On the eastern edge of the map the sea lane should be one square,
     * on the western edge it should stay three squares from land in
     * all directions.  It should never go deeper than a certain number
     * of squares, though.
     * @throws FreeColException if thrown by a called method
     */
    private void addSeaLanes() throws FreeColException {
        for (int y = 0; y < height; y++) {
            map.setType(0, y, Tile.HIGH_SEAS);
            map.setType(width - 1, y, Tile.HIGH_SEAS);
            for (int x = width - 2; x > (width - 14) && map.getDistanceToLand(x, y, 4) > 4; x--) {
                map.setType(x, y, Tile.HIGH_SEAS);
            }
        }
    }

    /**
     * Creates bonuses for some randomly selected tiles.
     * @throws FreeColException if thrown by a called method
     */
    private void createBonuses() throws FreeColException {
        Iterator iterator = map.getWholeMapIterator();
        while (iterator.hasNext()) {
            Position position = (Position) iterator.next();
            if (map.getDistanceToLand(position.getX(), position.getY(), 4) <= 4 && random.nextInt(100) < BONUS_RATE_PERCENT) {
                map.getTile(position).setBonus(true);
            }
        }
    }

    /**
     * Create the Indian settlements, at least a capital for every nation and
     * random numbers of other settlements.
     * @throws FreeColException if thrown by a called method
     */
    private void createIndianSettlements() throws FreeColException {
        Iterator incaIterator = map.getFloodFillIterator(getRandomStartingPos());
        Iterator aztecIterator = map.getFloodFillIterator(getRandomStartingPos());
        Iterator arawakIterator = map.getFloodFillIterator(getRandomStartingPos());
        Iterator cherokeeIterator = map.getFloodFillIterator(getRandomStartingPos());
        Iterator iroquoisIterator = map.getFloodFillIterator(getRandomStartingPos());
        Iterator siouxIterator = map.getFloodFillIterator(getRandomStartingPos());
        Iterator apacheIterator = map.getFloodFillIterator(getRandomStartingPos());
        Iterator tupiIterator = map.getFloodFillIterator(getRandomStartingPos());
        placeIndianSettlement(new IndianSettlement(IndianSettlement.INCA, IndianSettlement.CITY, true), incaIterator);
        placeIndianSettlement(new IndianSettlement(IndianSettlement.AZTEC, IndianSettlement.CITY, true), aztecIterator);
        placeIndianSettlement(new IndianSettlement(IndianSettlement.ARAWAK, IndianSettlement.VILLAGE, true), arawakIterator);
        placeIndianSettlement(new IndianSettlement(IndianSettlement.CHEROKEE, IndianSettlement.VILLAGE, true), cherokeeIterator);
        placeIndianSettlement(new IndianSettlement(IndianSettlement.IROQUOIS, IndianSettlement.VILLAGE, true), iroquoisIterator);
        placeIndianSettlement(new IndianSettlement(IndianSettlement.SIOUX, IndianSettlement.CAMP, true), siouxIterator);
        placeIndianSettlement(new IndianSettlement(IndianSettlement.APACHE, IndianSettlement.CAMP, true), apacheIterator);
        placeIndianSettlement(new IndianSettlement(IndianSettlement.TUPI, IndianSettlement.CAMP, true), tupiIterator);
        while (incaIterator.hasNext() && aztecIterator.hasNext() && arawakIterator.hasNext() && cherokeeIterator.hasNext() && iroquoisIterator.hasNext() && siouxIterator.hasNext() && apacheIterator.hasNext() && tupiIterator.hasNext()) {
            if (random.nextInt(5) != 0) placeIndianSettlement(new IndianSettlement(IndianSettlement.INCA, IndianSettlement.CITY, false), incaIterator);
            if (random.nextInt(5) != 0) placeIndianSettlement(new IndianSettlement(IndianSettlement.AZTEC, IndianSettlement.CITY, false), aztecIterator);
            if (random.nextInt(3) != 0) placeIndianSettlement(new IndianSettlement(IndianSettlement.ARAWAK, IndianSettlement.VILLAGE, false), arawakIterator);
            if (random.nextInt(4) != 0) placeIndianSettlement(new IndianSettlement(IndianSettlement.CHEROKEE, IndianSettlement.VILLAGE, false), cherokeeIterator);
            if (random.nextInt(4) != 0) placeIndianSettlement(new IndianSettlement(IndianSettlement.IROQUOIS, IndianSettlement.VILLAGE, false), iroquoisIterator);
            if (random.nextInt(4) != 0) placeIndianSettlement(new IndianSettlement(IndianSettlement.SIOUX, IndianSettlement.CAMP, false), siouxIterator);
            if (random.nextInt(3) != 0) placeIndianSettlement(new IndianSettlement(IndianSettlement.APACHE, IndianSettlement.CAMP, false), apacheIterator);
            if (random.nextInt(2) != 0) placeIndianSettlement(new IndianSettlement(IndianSettlement.TUPI, IndianSettlement.CAMP, false), tupiIterator);
        }
    }

    /**
     * Finds a suitable location for a settlement and builds it there. If no
     * location can be found (the iterator is exhausted) the settlement will
     * be discarded.
     * @param settlement The settlement to place
     * @param iterator The nation's iterator to use
     * @throws FreeColException if thrown by a called method
     */
    private void placeIndianSettlement(IndianSettlement indianSettlement, Iterator iterator) throws FreeColException {
        while (iterator.hasNext()) {
            Position position = (Position) iterator.next();
            int radius = indianSettlement.getRadius();
            if (isIndianSettlementCandidate(position, radius + 1) && random.nextInt(2) != 0) {
                System.out.println("Setting indian settlement at " + position.getX() + "x" + position.getY());
                map.getTile(position).setSettlement(indianSettlement);
                indianSettlementMap.set(position, CLAIMED_LAND);
                Iterator circleIterator = map.getCircleIterator(position, true, radius);
                while (circleIterator.hasNext()) {
                    Position adjPos = (Position) circleIterator.next();
                    indianSettlementMap.set(adjPos, CLAIMED_LAND);
                }
                return;
            }
        }
    }

    /**
     * Check to see if it is possible to build an Indian settlement at a
     * given map position. A city (Incas and Aztecs) needs a free radius
     * of two tiles, a village or camp needs one tile in every direction.
     * There must be at least three productive tiles in the area including
     * the settlement tile.
     * @param position Candidate position
     * @param radius necessary radius
     * @return True if position suitable for settlement
     * @throws FreeColException if thrown by a called method
     */
    private boolean isIndianSettlementCandidate(Position position, int radius) throws FreeColException {
        if (indianSettlementMap.get(position) == UNKNOWN_LAND) {
            indianSettlementMap.set(position, VISITED_LAND);
            if (map.getTile(position).isSettleable()) {
                int numSettleableNeighbors = 0;
                Iterator iterator = map.getCircleIterator(position, true, radius);
                while (iterator.hasNext()) {
                    Position adjPos = (Position) iterator.next();
                    if (indianSettlementMap.get(adjPos) == CLAIMED_LAND) {
                        return false;
                    }
                    if (map.getTile(adjPos).isSettleable() && map.getDistance(position.getX(), position.getY(), adjPos.getX(), adjPos.getY()) == 1) {
                        numSettleableNeighbors++;
                    }
                }
                return numSettleableNeighbors >= 2;
            }
        }
        return false;
    }

    /**
     * Select a random position on the map to use as a starting position.
     * @return Position selected
     */
    private Position getRandomStartingPos() {
        int x = random.nextInt(width - 40) + 20;
        int y = random.nextInt(height - 40) + 20;
        return new Map.Position(x, y);
    }

    /**
     * Create two ships, one with a colonist, for each player, and
     * select suitable starting positions.
     * @param players List of players
     * @throws FreeColException if thrown by a called method
     */
    private void createEuropeanUnits(Vector players) throws FreeColException {
        int[] shipYPos = new int[4];
        for (int i = 0; i < 4; i++) shipYPos[i] = 0;
        for (int i = 0; i < players.size(); i++) {
            ServerPlayer player = (ServerPlayer) players.elementAt(i);
            int y = random.nextInt(height - 20) + 10;
            int x = width - 1;
            while (isAShipTooClose(y, shipYPos)) {
                y = random.nextInt(height - 20) + 10;
            }
            shipYPos[i] = y;
            while (map.getTile(x - 1, y).getType() == Tile.HIGH_SEAS) {
                x--;
            }
            player.setEntrySeaLane(y);
            int unitType;
            if (player.getNation() == ServerPlayer.DUTCH) {
                unitType = Unit.MERCHANTMAN;
            } else {
                unitType = Unit.CARAVEL;
            }
            Tile startTile = map.getTile(x, y);
            Unit unit1 = new Unit(startTile, player, unitType, 0, Unit.ACTIVE, game.assignUnitID());
            Unit unit2 = new Unit(unit1, player, Unit.FREE_COLONIST, 0, Unit.SENTRY, game.assignUnitID());
            Unit unit3 = new Unit(startTile, player, Unit.GALLEON, 0, Unit.ACTIVE, game.assignUnitID());
            unit1.resetMovesLeft();
            unit2.resetMovesLeft();
            unit3.resetMovesLeft();
        }
    }

    /**
     * Determine whether a proposed ship starting Y position is "too close"
     * to those already used.
     * @param proposedY Proposed ship starting Y position
     * @param usedYPositions List of already assigned positions
     * @return True if the proposed position is too close
     */
    private boolean isAShipTooClose(int proposedY, int[] usedYPositions) {
        for (int i = 0; i < 4 && usedYPositions[i] != 0; i++) {
            if (Math.abs(usedYPositions[i] - proposedY) < 8) {
                return true;
            }
        }
        return false;
    }
}
