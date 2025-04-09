package elcod;

import core.ElcodModel;
import elcod.db.Database;
import elcod.ui.UI;
import elcod.util.PluginManager;
import elcod.util.Settings;
import elcod.util.StringUtil;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Elcod {

    public ElcodModel elcodModel;

    private static Elcod app = null;

    private static java.util.TreeSet<String> elcodDbDriverList = null;

    private Elcod() {
    }

    public static Elcod newInstance(String[] args) {
        if (app == null) {
            app = new Elcod();
            app.elcodModel = new ElcodModel();
            Elcod.elcodDbDriverList = new java.util.TreeSet();
            String pluginDir = Settings.getGeneralPref("pluginDir", "plugin");
            String ui = Settings.getUIType();
            String db = Settings.getDBType();
            PluginManager plgMgr = new PluginManager(pluginDir);
            try {
                plgMgr.loadPlugins();
            } catch (Exception e) {
            }
            app.loadDB(db + "." + StringUtil.capitalize(db) + "DB");
            app.loadUI(ui + "." + StringUtil.capitalize(ui) + "UI");
        }
        return app;
    }

    public void loadUI(String uiType) {
        Class uiClass;
        UI ui;
        try {
            uiClass = Class.forName("elcod.ui." + uiType);
            java.lang.reflect.Constructor co = uiClass.getConstructor(new Class[] { ElcodModel.class });
            ui = (UI) co.newInstance(this.elcodModel);
            ui.initUi();
        } catch (Exception ex) {
            Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, ex.toString());
        }
    }

    public void loadDB(String dbType) {
        Class dbClass;
        Database db;
        try {
            dbClass = Class.forName("elcod.db." + dbType);
            db = (Database) dbClass.newInstance();
            db.connect();
            db.setElcodModel(this.elcodModel);
            db.loadModel();
        } catch (Exception ex) {
            Logger.getLogger(PluginManager.class.getName()).log(Level.SEVERE, ex.toString());
        }
    }

    public static Elcod getApp() {
        return app;
    }

    public static void addDbDriver(String driverName) {
        Elcod.elcodDbDriverList.add(driverName);
    }

    public static java.util.TreeSet<String> getDbDriverList() {
        return Elcod.elcodDbDriverList;
    }
}
