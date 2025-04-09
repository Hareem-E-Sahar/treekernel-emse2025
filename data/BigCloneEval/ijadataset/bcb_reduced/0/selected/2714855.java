package net.sourceforge.huntforgold.model;

import net.sourceforge.huntforgold.util.Configuration;
import net.sourceforge.huntforgold.xml.Ships;
import net.sourceforge.huntforgold.xml.Ship;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.Logger;

/**
 * Factory for the Ship class
 */
public class ShipFactory implements Serializable {

    /** The logger */
    private static Logger log = Logger.getLogger(ShipFactory.class);

    /** Singleton instance */
    private static ShipFactory instance = null;

    /** An array of known ship types */
    private String[] shipTypes;

    /** Map of ships */
    private Map shipMap;

    /** Random generator */
    private Random random;

    /** An unique id */
    private int counter;

    /**
   * Constructs a new ShipFactory
   */
    private ShipFactory() {
        random = new Random(System.currentTimeMillis());
        counter = 0;
        shipMap = new HashMap();
        try {
            Configuration conf = Configuration.getConfiguration();
            Ships ships = (Ships) conf.getHuntforgold().getShips();
            shipTypes = new String[ships.getShip().size()];
            Iterator it = ships.getShip().iterator();
            while (it.hasNext()) {
                Ship s = (Ship) it.next();
                String name = s.getName();
                shipTypes[s.getIndex()] = name;
                shipMap.put(new Integer(s.getIndex()), s);
            }
        } catch (Exception e) {
            log.fatal("ShipFactory could not be initialized", e);
            System.exit(1);
        }
    }

    /**
   * Get the instance of the ShipFactory
   *
   * @return The ShipFactory
   */
    public static synchronized ShipFactory getShipFactory() {
        if (instance == null) {
            instance = new ShipFactory();
        }
        return instance;
    }

    /**
   * Get names of the known ship types
   *
   * @return A list of known ship types; otherwise null
   */
    public String[] getShipTypes() {
        return shipTypes;
    }

    /**
   * Create a ship based upon a type
   *
   * @param index The type index of the ship
   * @param nationality The nationality of the ship
   * @return The ship if exists; otherwise null
   */
    public synchronized net.sourceforge.huntforgold.model.Ship createShip(int index, int nationality) {
        net.sourceforge.huntforgold.xml.Ship result = (net.sourceforge.huntforgold.xml.Ship) shipMap.get(new Integer(index));
        if (result != null) {
            counter = counter + 1;
            int uniqueId = counter;
            String name = result.getName();
            String pngImage = result.getPngImage();
            int typicalCrew = result.getTypicalCrew();
            int maxCrew = result.getMaxCrew();
            int maxCargo = result.getMaxCargo();
            int maxHitPoints = result.getMaxHitPoints();
            int maxSpeed = result.getMaxSpeed();
            int typicalCannons = result.getTypicalCannons();
            int maxCannons = result.getMaxCannons();
            double turnSpeed = result.getTurnSpeed();
            net.sourceforge.huntforgold.model.Ship ship = new net.sourceforge.huntforgold.model.Ship(uniqueId, name, pngImage, typicalCrew, maxCrew, maxCargo, maxHitPoints, maxSpeed, typicalCannons, maxCannons, turnSpeed, nationality);
            int crew = random.nextInt(maxCrew + 1);
            if (crew < 8) {
                crew = 8;
            }
            ship.setCrew(crew);
            ship.setCaptain("Enemy captain");
            ship.setFood(0);
            ship.setGoods(0);
            ship.setSugar(0);
            int cannons = random.nextInt(maxCannons + 1);
            if (cannons % 2 == 1) {
                cannons -= 1;
            }
            ship.setCannons(cannons);
            int gold = random.nextInt(51) * 20;
            ship.setGold(gold);
            return ship;
        } else {
            return null;
        }
    }
}
