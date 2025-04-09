package gothwag.armies;

import gothwag.gui.ArmyBuilderWindow;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.swing.*;

/**
 * This entirely static class contains automatically built references to all
 * ArmyKits. These references are obtained at startup through reflection, by
 * seeking all ArmyKit subclasses in the armies package.
 * 
 * @author Gian Piero Favini
 *
 */
public class ArmyCompendium extends Object {

    static Vector armyKits;

    /**
	 * Automatically loads all the included ArmyKits from their classes via reflection,
	 * generates an instance of each and stores it in the main Vector.
	 */
    public static void init() {
        armyKits = new Vector();
        try {
            Class armyKitClass = Class.forName("gothwag.armies.ArmyKit");
            ClassLoader cl = armyKitClass.getClassLoader();
            URL u = cl.getResource("gothwag/armies");
            System.out.println(u);
            File theFile = new File(new URI(u.toString()));
            File[] children = theFile.listFiles();
            for (int k = 0; k < children.length; k++) {
                String name = children[k].getName();
                if (name.endsWith(".class")) {
                    name = name.substring(0, name.length() - 6);
                    Class newClass = Class.forName("gothwag.armies." + name);
                    if (newClass.getSuperclass().equals(armyKitClass)) {
                        armyKits.add(newClass.getConstructor(new Class[0]).newInstance(new Object[0]));
                        System.out.println("Adding ArmyKit: " + name);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
	 * Fetches an ArmyKit based on its sequence index.
	 * @param index
	 * @return
	 */
    public static ArmyKit getArmyKit(int index) {
        return ((ArmyKit) armyKits.get(index));
    }

    /**
	 * Returns the number of armies in the compendium.
	 * @return the number of armies.
	 */
    public static int armyKitNumber() {
        return armyKits.size();
    }

    /**
	 * Returns an ArmyKit based on its internal name.
	 * @param name
	 * @return
	 */
    public static ArmyKit getArmyKit(String name) {
        for (int k = 0; k < armyKitNumber(); k++) if (getArmyKit(k).getKitName().equals(name)) return getArmyKit(k);
        return null;
    }

    public static void main(String args[]) {
        init();
        JFrame j = new JFrame("Gothwag");
        j.getContentPane().setLayout(null);
        j.setSize(800, 600);
        j.getContentPane().add(new ArmyBuilderWindow("panthalia", 2000, new gothwag.gui.ResourceManager()));
        j.show();
    }
}
