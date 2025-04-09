package wood.model.map;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import lawu.math.Vector;
import org.apache.log4j.Logger;
import wood.controller.GameFlow;
import wood.model.ae.LavaMapTeleport;
import wood.model.ae.TeleportEffect;
import wood.model.ae.WaterMapTeleport;
import wood.model.manager.Model;
import wood.model.manager.Updateable;
import wood.model.map.Map.Tile;
import wood.model.tileobject.TileObject;
import wood.util.ResourceLoader;

/**
 * Reads files of the form:
 * 
 * Map Display Name (e.g. "Water World")
 * Map Image File Path (e.g. "images/maps/lavamap.jpg")
 * Avatar Default Start location (e.g. "0,0")
 * (a list of Items and their locations, e.g. "Mine 0,2")
 * (a list of Items and their locations, e.g. "River 0,2")
 */
public class MapConfigFileReader {

    private static Logger logger = Logger.getLogger("wood.model.mapf");

    private String mapName;

    private String backgroundPath;

    private Vector vector;

    private Model model;

    private ArrayList<Tile> tiles;

    private ArrayList<TileObject> tileObjects;

    private GameFlow flow;

    public MapConfigFileReader(String path, Map enclosingMap, Model model, GameFlow flow) {
        String line;
        logger.info(String.format("Loading map config %s", path));
        this.flow = flow;
        this.model = model;
        this.tiles = new ArrayList<Tile>();
        this.tileObjects = new ArrayList<TileObject>();
        InputStream stream = ResourceLoader.getInstance().getStream(path);
        Scanner s = new Scanner(stream);
        mapName = s.nextLine();
        backgroundPath = s.nextLine();
        line = s.nextLine();
        String[] arr = line.split(",");
        int x = Integer.parseInt(arr[0]);
        int y = Integer.parseInt(arr[1]);
        vector = new Vector(x, y);
        if (enclosingMap != null) {
            while (s.hasNextLine()) {
                line = s.nextLine();
                if (line.isEmpty()) continue;
                arr = line.split(" ");
                x = Integer.parseInt(arr[1].split(",")[0]);
                y = Integer.parseInt(arr[1].split(",")[1]);
                Tile t = enclosingMap.getTile(new Vector(x, y));
                TileObject obj = null;
                try {
                    if (arr.length == 2) obj = (TileObject) Class.forName(arr[0].trim()).getConstructor(Tile.class).newInstance(t); else {
                        HexDirection dir = HexDirection.valueOf(arr[2]);
                        Integer i = Integer.parseInt(arr[3]);
                        obj = (TileObject) Class.forName(arr[0].trim()).getConstructor(Tile.class, HexDirection.class, Integer.class).newInstance(t, dir, i);
                    }
                    tileObjects.add(obj);
                    if (obj instanceof Updateable) model.addUpdateable((Updateable) obj);
                    t.registerTileObject(obj);
                    this.tiles.add(t);
                } catch (Exception e) {
                    logger.error("Fail reading cfg line.", e);
                }
            }
        }
        s.close();
        logger.info(String.format("Map image is: %s", backgroundPath));
    }

    public ArrayList<Tile> getLoadedTiles() {
        makeTeleports();
        makeMultiLevelTeleports();
        makeWaterLevelTeleport();
        return tiles;
    }

    public void makeTeleports() {
        ArrayList<TeleportEffect> teleports = new ArrayList<TeleportEffect>();
        for (TileObject i : tileObjects) {
            if (i instanceof TeleportEffect) {
                teleports.add((TeleportEffect) i);
            }
        }
        for (TeleportEffect i : teleports) {
            i.registerTeleports(teleports);
        }
    }

    public void makeMultiLevelTeleports() {
        ArrayList<LavaMapTeleport> multiMapTeleports = new ArrayList<LavaMapTeleport>();
        for (TileObject i : tileObjects) {
            if (i instanceof LavaMapTeleport) {
                multiMapTeleports.add((LavaMapTeleport) i);
            }
        }
        for (LavaMapTeleport i : multiMapTeleports) {
            i.registerTeleports(multiMapTeleports);
            i.setTheControlsForTheHeartOfTheSun(model, flow);
        }
    }

    public void makeWaterLevelTeleport() {
        ArrayList<WaterMapTeleport> multiMapTeleports = new ArrayList<WaterMapTeleport>();
        for (TileObject i : tileObjects) {
            if (i instanceof WaterMapTeleport) {
                multiMapTeleports.add((WaterMapTeleport) i);
            }
        }
        for (WaterMapTeleport i : multiMapTeleports) {
            i.registerTeleports(multiMapTeleports);
            i.setTheControlsForTheHeartOfTheSun(model, flow);
        }
    }

    public Vector getAvatarVector() {
        return vector;
    }

    public String getMapName() {
        return mapName;
    }

    public String getBackgroundPath() {
        return backgroundPath;
    }
}
