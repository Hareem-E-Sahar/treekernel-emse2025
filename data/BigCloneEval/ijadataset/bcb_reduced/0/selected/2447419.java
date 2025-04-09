package net.sourceforge.mapcraft.map;

import net.sourceforge.mapcraft.xml.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import javax.swing.ImageIcon;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.image.*;
import net.sourceforge.mapcraft.map.elements.Area;
import net.sourceforge.mapcraft.map.elements.Thing;
import net.sourceforge.mapcraft.map.interfaces.ITileSet;
import net.sourceforge.mapcraft.map.tilesets.TileSet;
import net.sourceforge.mapcraft.utils.Options;

/**
 * A class object which describes a region as one or more maps.
 * Each view of the map is held as a TileSet. The "root" TileSet
 * is the top level map, normally at the largest scale. Other
 * TileSets can be stored as part of the same map object, all
 * views, possibly at higher resolution, of regions within the
 * "root" TileSet.
 *
 * This class does not provide any display functionality, it
 * is merely a holder for the map data.
 *
 * @author  Samuel Penn
 * @version $Revision: 1.83 $
 */
public class Map extends MapBean implements Cloneable {

    /**
     * Create a new Map object from an old one.
     */
    public Map(Map map) {
        super(map);
    }

    /**
     * Create a brand new map, of the given size and scale. Details such
     * as the tile shape and terrain definitions should be given later
     * using the relevent setter methods.
     *
     * @param name      Name of the map.
     * @param width     Width of the map, in tiles.
     * @param height    Height of the map, in tiles.
     * @param scale     Map scale - what each tile represents (either km or m).
     */
    public Map(String name, int width, int height, int scale) throws MapException {
        super(name, width, height, scale);
    }

    /**
     * Create a new map, based on a world of a given radius. The map
     * will be a hexagonal world map. Height and width are calculated
     * from the scale, and the radius of the world. A sinusoidal
     * projection is used, so the corners of the map will be blank.
     *
     * @param name      Name of the map.
     * @param radius    World radius, in km.
     * @param scale     Tile width, in km.
     */
    public Map(String name, int radius, int scale) throws MapException {
        setName(name);
        setId(name + "." + System.currentTimeMillis());
        setParent("none");
        setAuthor("Unknown");
        int height = (int) (radius * Math.PI) / scale;
        int width = (int) (radius * 2 * Math.PI) / scale;
        tileSets = new TileSet[1];
        tileSets[0] = new TileSet("root", width, height, scale);
        int y, x;
        for (y = 0; y < height; y++) {
            double angle = (180.0 * (y + 0.5)) / height;
            double t = radius / scale;
            double r = t * Math.sin(Math.toRadians(angle));
            int w = (int) Math.round(2.0 * r * Math.PI);
            int s = (int) Math.round((width / 2.0) - (w / 2.0));
            for (x = 0; x < width; x++) {
                try {
                    if (x >= s && x <= s + w) {
                        tileSets[0].setTerrain(x, y, terrainSet.getTerrain((short) 1));
                        tileSets[0].setWritable(x, y, true);
                    } else {
                        tileSets[0].setTerrain(x, y, terrainSet.getTerrain((short) 0));
                        tileSets[0].setWritable(x, y, false);
                    }
                } catch (MapOutOfBoundsException mobe) {
                }
            }
        }
    }

    /**
     * This map becomes a child of the original.
     * Should be called <b>before</b> performing some operation such as
     * crop(), so we keep a reference to the original map. This enables
     * the new map to be merged back into the parent at a later date.
     *
     * The id of the map is appended with a new identifier, to
     * distinguish it from the original.
     */
    public void fork() {
        for (int i = 0; i < tileSets.length; i++) {
            tileSets[i].setParent(tileSets[i].getScale(), 0, 0);
        }
        setParent(getId());
        setId(getId() + "." + System.currentTimeMillis());
    }

    public void unwrapWorld() {
        int y, x;
        int width, height;
        TileSet unwrapped = null;
        System.out.println("Unwrapping");
        width = tileSets[0].getMapWidth();
        height = tileSets[0].getMapHeight();
        try {
            unwrapped = new TileSet("root", width, height, tileSets[0].getScale());
        } catch (InvalidArgumentException ie) {
            ie.printStackTrace();
        }
        for (y = 0; y < height; y++) {
            int w = 0;
            int start = -1;
            for (x = 0; x < width; x++) {
                try {
                    if (tileSets[0].getTerrain(x, y).getId() != 0) {
                        w++;
                        if (start == -1) {
                            start = x;
                        }
                    }
                } catch (MapOutOfBoundsException me) {
                    me.printStackTrace();
                }
            }
            for (x = 0; x < width; x++) {
                int i = (int) (x * (1.0 * w / width));
                try {
                    unwrapped.copy(tileSets[0], start + i, y, x, y);
                } catch (MapOutOfBoundsException moobe) {
                    moobe.printStackTrace();
                }
            }
        }
        tileSets[0] = unwrapped;
    }

    /**
     * Load a map from an existing XML file and construct
     * the necessary data sets.
     *
     * @param filename  Filename of map to load.
     * @throws          MapException
     */
    public Map(String filename) throws MapException {
        super(filename);
    }

    public Object clone() throws CloneNotSupportedException {
        Map m = null;
        try {
            m = new Map(getName(), tileSets[0].getMapWidth(), tileSets[0].getMapHeight(), tileSets[0].getScale());
            m.setFilename("/tmp/clone.map");
            m.terrainSet = (TerrainSet) terrainSet.clone();
            m.thingSet = (TerrainSet) thingSet.clone();
            m.featureSet = (TerrainSet) featureSet.clone();
            m.areaSet = (AreaSet) areaSet.clone();
        } catch (MapException e) {
        }
        return (Object) m;
    }

    /**
     * Retrieve settings from loaded XML terrain set. Terrain, things
     * and features are copied, as is the type of map and the shape
     * of the tiles.
     *
     * @param xml    Document containing terrainset definitions.
     */
    private void loadTerrainSet(MapXML xml) throws XMLException {
        terrainSet = xml.getTerrainSet("basic");
        thingSet = xml.getTerrainSet("things");
        featureSet = xml.getTerrainSet("features");
        System.out.println("Shape [" + xml.getTileShape() + "] Type [" + xml.getType() + "]");
        setTileShape(xml.getTileShape());
        setType(xml.getType());
        setImageDir(xml.getImageDir());
    }

    /**
     * Load a terrain set from the specified file, and copy information
     * into the current map.
     *
     * @param filename    Pathname to file containing terrainset.
     */
    public void loadTerrainSet(String filename) {
        MapXML xml;
        try {
            xml = new MapXML(filename);
            loadTerrainSet(xml);
        } catch (XMLException e) {
            System.out.println("Invalid XML in terrain set (" + e.getMessage() + ") from file [" + filename + "]");
        } catch (MapException e) {
            System.out.println("Failed to load terrain set (" + e.getMessage() + ") from file [" + filename + "]");
        }
    }

    /**
     * Load a terrain set from the specified URL, and copy information
     * into the current map. Used to retrieve terrainset from Jar file.
     *
     * @param url    URL to file containing terrainset.
     */
    public void loadTerrainSet(URL url) {
        MapXML xml;
        try {
            xml = new MapXML(url);
            loadTerrainSet(xml);
        } catch (XMLException e) {
            System.out.println("Invalid XML in terrain set (" + e.getMessage() + ") from URL [" + url + "]");
        } catch (MapException e) {
            System.out.println("Failed to load terrain set (" + e.getMessage() + ") from URL [" + url + "]");
        }
    }

    /**
     * Sets the scale of the map, in km per tile object.
     * The scale should be a power of two.
     * This merely changes the value of the scale, and does
     * not cause any other change to the map.
     *
     * @param scale     Width of each tile, in km. Must be a power of two.
     */
    public void setScale(int scale) {
        tileSets[getCurrentSet()].setScale(scale);
    }

    /**
     * Rescale the current tileset to the new scale.
     * Unlike setScale(), the map is physically changed to represent the
     * new scale. If 1 hex = 25km before, and it is rescaled to 5km, then
     * the map will be zoomed so there are 25x as many hexes.
     *
     * @param newScale  New scale to set the map to.
     */
    public void rescale(int newScale) {
        tileSets[getCurrentSet()].rescale(newScale);
    }

    /**
     * Resize the map. The width and height of the map is changed,
     * with blank columns and rows being added as necessary.
     *
     * @param newWidth  New width of the map, in tiles.
     * @param newHeight New height of the map, in tiles.
     * @param atRight   If true, new columns are inserted/removed on the right,
     *                  otherwise inserted/removed on the left.
     * @param atBottom  If true, new columns are inserted/removed on the bottom,
     *                  otherwise inserted/removed on the top.
     */
    public void resize(int newWidth, int newHeight, boolean atLeft, boolean atTop) throws InvalidArgumentException {
        ITileSet resized = null;
        ITileSet original = tileSets[getCurrentSet()];
        int dx, dy;
        dx = newWidth - original.getMapWidth();
        dy = newHeight - original.getMapHeight();
        if (dx < 0) {
            throw new InvalidArgumentException("New width must not be smaller than old width");
        }
        if (dy < 0) {
            throw new InvalidArgumentException("New height must not be smaller than old height");
        }
        if (dx == 0 && dy == 0) {
            return;
        }
        resized = new TileSet(tileSets[getCurrentSet()].getName(), newWidth, newHeight, tileSets[getCurrentSet()].getScale());
        int xOffset = 0;
        int yOffset = 0;
        if (atLeft) {
            xOffset = dx;
        }
        if (atTop) {
            yOffset = dy;
        }
        int x, y;
        for (y = 0; y < original.getMapHeight(); y++) {
            for (x = 0; x < original.getMapWidth(); x++) {
                try {
                    resized.copy(original, x, y, x + xOffset, y + yOffset);
                } catch (MapOutOfBoundsException oobe) {
                }
            }
        }
        tileSets[getCurrentSet()] = resized;
    }

    /**
     * Crop the specified tileset to the given rectangle.
     */
    public boolean crop(int x, int y, int w, int h) {
        try {
            tileSets[getCurrentSet()].crop(x, y, w, h);
        } catch (MapOutOfBoundsException moobe) {
            return false;
        }
        return true;
    }

    /**
     * Crop the specified tileset to the given area. A rectangle is
     * calculated to surround all tiles which match the given area.
     * If a positive margin is specified, the rectangle is grown in
     * each direction by the margin size.
     */
    public boolean cropToArea(Area area, int margin) {
        try {
            tileSets[getCurrentSet()].cropToArea(area, margin);
        } catch (MapOutOfBoundsException moobe) {
            return false;
        }
        return true;
    }

    public boolean cropToHighlighted() {
        try {
            tileSets[getCurrentSet()].cropToHighlighted();
        } catch (MapOutOfBoundsException moobe) {
            return false;
        }
        return true;
    }

    public boolean cropToThing(String name, short radius) {
        try {
            tileSets[getCurrentSet()].cropToThing(name, radius);
        } catch (MapException e) {
            return false;
        }
        return true;
    }

    public boolean cropToPath(String name, short margin) {
        try {
            tileSets[getCurrentSet()].cropToPath(name, margin);
        } catch (MapException e) {
            return false;
        }
        return true;
    }

    /**
     * Load a map from an image file. The pixels are converted
     * into tiles.
     */
    public void loadImage(String filename) throws IOException {
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image image = toolkit.getImage(filename);
            ImageIcon icon = new ImageIcon(image);
            PixelGrabber pg;
            int w, h;
            w = icon.getIconWidth();
            h = icon.getIconHeight();
            System.out.println("Got image size of " + w + " x " + h);
            icon = null;
            int pixels[] = new int[w * h];
            pg = new PixelGrabber(image, 0, 0, w, h, pixels, 0, w);
            System.out.println("Grabbed pixels");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Return the distance in tiles between any two tiles. The tiles are
     * specified by their x and y coordinates.
     *
     * This does not yet work.
     */
    public int distance(int x0, int y0, int x1, int y1) {
        int d = 0;
        if (getTileShape() == SQUARE) {
            int x = Math.abs(x0 - x1);
            int y = Math.abs(y0 - y1);
            d = x + Math.abs(x - y) + y;
            d = (d + 1) / 2;
        } else if (getTileShape() == HEXAGONAL) {
            y0 = y0 - (x0) / 2;
            y1 = y1 - (x1) / 2;
            if (x1 <= x0) {
                int x = x0;
                int y = y0;
                x0 = x1;
                y0 = y1;
                x1 = x;
                y1 = y;
            }
            if (y1 > y0) {
                d = x1 - x0 + y1 - y0;
            } else if (x0 + y0 > x1 + y1) {
                d = y0 - y1;
            } else {
                d = x1 - x0;
            }
        }
        return d;
    }

    /**
     * Returns true if the two tiles are either the same or next to each
     * other, false otherwise.
     */
    public static boolean isNextTo(int x0, int y0, int x1, int y1) {
        if (x0 == x1 && y0 == y1) {
            return true;
        }
        if (x0 == x1) {
            if (Math.abs(y0 - y1) <= 1) {
                return true;
            }
        }
        if (Math.abs(x0 - x1) == 1) {
            if (y0 == y1) {
                return true;
            }
            if (x0 % 2 == 0) {
                if (y0 == y1 + 1) {
                    return true;
                }
            } else {
                if (y0 == y1 - 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return the (zero based) index for the nearest Thing to the given
     * x/y coordinate. If 'max' is non-zero, then any Things more than
     * 'max' away are ignored.
     */
    public int getNearestThingIndex(int x, int y, int max) {
        Thing[] things = tileSets[0].getThings();
        Thing thing = null;
        int index = -1;
        int sx = 0, sy = 0;
        int min = -1;
        int d = 0;
        System.out.println("getNearestThingIndex: " + x + "," + y + " (" + max + ")");
        max = max * max;
        for (int i = 0; i < things.length; i++) {
            thing = things[i];
            sx = x - thing.getX();
            sy = y - thing.getY();
            d = sx * sx + sy * sy;
            System.out.println("thing: " + sx + ", " + sy + " " + d);
            if ((min == -1 || d < min) && (max == 0 || d < max)) {
                min = d;
                index = i;
                System.out.println("Nearest is " + index + " at " + min);
            }
        }
        return index;
    }

    /**
     * Return nearest Thing for the given coordinates.
     * @see getNearestThingIndex
     */
    public Thing getNearestThing(int x, int y, int max) {
        Thing thing = null;
        int index = getNearestThingIndex(x, y, max);
        if (index >= 0) {
            thing = tileSets[0].getThings()[index];
        }
        return thing;
    }

    /**
     * Merges the supplied map into this map. Both maps must have the same
     * parent, and must be the same scale. It is assumed that they are both
     * from different regions on the parent map, but that they overlap to
     * some extent. Differences in the supplied map overwrite this map.
     * The supplied map is always left unchanged.
     *
     * @param merge    The map to merge from.
     */
    public boolean merge(Map merge) throws MapException {
        System.out.println("Merging maps");
        if (merge == null) {
            throw new MapException("Cannot merge from a null map");
        }
        if (!getParent().equals(merge.getParent())) {
            System.out.println("Maps do not have the same parent");
            throw new MapException("Maps can only be merged if they have the same parent");
        }
        ITileSet alpha = tileSets[0];
        ITileSet beta = merge.tileSets[0];
        if (alpha.getScale() != beta.getScale()) {
            System.out.println("Maps are not the same scale");
            throw new MapException("Maps can only be merged if they are the same scale");
        }
        int ax, ay, aw, ah;
        int bx, by, bw, bh;
        ax = alpha.getParentsXOffset();
        ay = alpha.getParentsYOffset();
        aw = alpha.getMapWidth();
        ah = alpha.getMapHeight();
        bx = beta.getParentsXOffset();
        by = beta.getParentsYOffset();
        bw = beta.getMapWidth();
        bh = beta.getMapHeight();
        int dx = ax - bx;
        int dy = ay - by;
        dx = dx * alpha.getParentsScale() / alpha.getScale();
        dy = dy * alpha.getParentsScale() / alpha.getScale();
        System.out.println("merge: dx " + dx + " dy " + dy);
        return true;
    }

    /**
     * Returns statistics on the specified area, returning an XML string
     * describing it.
     *
     * <area name="Foo">
     *     <size>45</size>
     * </area>
     */
    public String getAreaStats(String name) {
        StringBuffer buffer = new StringBuffer("");
        int set = 0;
        try {
            Area area = getAreaByName(name);
            int id = area.getId();
            int tiles = 0;
            for (int x = 0; x < tileSets[0].getMapWidth(); x++) {
                for (int y = 0; y < tileSets[0].getMapHeight(); y++) {
                    Area a = tileSets[0].getArea(x, y);
                    if (a != null && (a.getId() == id || a.getParent().equals(area))) {
                        tiles++;
                    }
                }
            }
            buffer.append("<area name=\"" + name + "\"><size>" + tiles + "</size></area>");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    public String getAreaStats() {
        StringBuffer buffer = new StringBuffer("");
        try {
            buffer.append("<areas>\n");
            Iterator iter = areaSet.iterator();
            while (iter.hasNext()) {
                Area area = (Area) iter.next();
                buffer.append(getAreaStats(area.getName()) + "\n");
            }
            buffer.append("</areas>\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    /**
     * Find all occurrences of an area on a map, and delete them. Also
     * remove the area from the AreaSet for the map. If the area has a
     * parent, then the map locations are set to be the parent area.
     * 
     * @param area      Id of area to be deleted.
     */
    public void deleteArea(Area area) {
        tileSets[getCurrentSet()].changeArea(area, area.getParent());
        getAreaSet().deleteArea(area);
    }

    public static void main(String args[]) {
        Map map = null;
        Options options;
        try {
            map = new Map("/home/sam/src/forge/mapcraft/mapcraft/maps/island.map");
            System.exit(0);
            options = new Options(args);
            if (options.isOption("-create")) {
                int width = options.getInt("-width");
                int height = options.getInt("-height");
                int scale = options.getInt("-scale");
                String name = options.getString("-create");
                String terrain = options.getString("-terrain");
                boolean square = options.isOption("-square");
                System.out.println("Creating map " + name + " " + width + "x" + height);
                map = new Map(name, width, height, scale);
                if (square) {
                    map.setTileShape(Map.SQUARE);
                }
                map.loadTerrainSet(terrain);
                map.save(name + ".map");
            } else if (options.isOption("-load")) {
                map = new Map(options.getString("-load"));
            } else if (options.isOption("-rewrite")) {
                map = new Map(options.getString("-rewrite"));
                map.save("new.map");
            } else if (options.isOption("-resize")) {
                int w = options.getInt("-width");
                int h = options.getInt("-height");
                String filename = options.getString("-resize");
                map = new Map(filename);
                map.resize(w, h, false, false);
                map.save("resized.map");
            } else if (options.isOption("-euressa")) {
                map = new Map("maps/Euressa.map");
                map.resize(91, 28, false, false);
                map.resize(176, 88, true, true);
                map.save("new.map");
            } else if (options.isOption("-world")) {
                map = new Map("Earth", 6400, 125);
                map.loadTerrainSet("terrain/hexagonal.xml");
                map.setImageDir("hexagonal/standard");
                map.save("earth.map");
            }
            if (map != null && options.isOption("-areastats")) {
                System.out.println(map.getAreaStats());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
