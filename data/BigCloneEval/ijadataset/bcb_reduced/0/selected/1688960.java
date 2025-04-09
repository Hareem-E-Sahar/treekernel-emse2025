package org.openacs.vendors;

import bsh.util.Util;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openacs.Configurator;

/**
 *
 * @author Administrator
 */
public class Vendor {

    protected String hwversion;

    public static final int CFG_UPDATE_METHOD_1 = 1;

    public static final int CFG_UPDATE_METHOD_2 = 2;

    private static HashMap<String, Class> vendors = new HashMap<String, Class>();

    static {
        vendors.put("00147F", Thomson.class);
        vendors.put("0090D0", Thomson.class);
    }

    public String[] CheckConfig(String filename, String name, String version, String cfg) {
        if (cfg.contains("[ env.ini ]")) {
            Thomson v = new Thomson();
            v.hwversion = this.hwversion;
            return v.CheckConfig(filename, name, version, cfg);
        }
        if (Broadcomm.DetectFromConfig(cfg)) {
        }
        return null;
    }

    public String UpdateConfig(String filename, String name, String version, String cfg) {
        if (Broadcomm.DetectFromConfig(cfg)) {
            Vendor v = new Broadcomm();
            return v.UpdateConfig(filename, name, version, cfg);
        }
        return null;
    }

    public static Vendor getVendor(String oui, String hardwareClass, String hardwareVersion) {
        try {
            Class c = vendors.get(oui);
            Vendor v;
            if (c == null) {
                v = new Vendor();
            } else {
                Constructor<?> m = c.getConstructor();
                v = (Vendor) m.newInstance();
            }
            v.hwversion = hardwareVersion;
            return v;
        } catch (Exception ex) {
            Logger.getLogger(Vendor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new Vendor();
    }

    public int getConfigUpdateMethod() {
        return CFG_UPDATE_METHOD_1;
    }
}
