package discordia;

import tristero.*;
import com.tm.xmlrpc.*;
import java.util.*;
import java.lang.reflect.*;

public class ModuleLoader implements Runnable {

    protected static Thread processThread;

    protected static ModuleLoader instance = null;

    Vector modules = new Vector();

    public static void init() {
        if (instance == null) {
            instance = new ModuleLoader();
            processThread = new Thread(instance);
            processThread.start();
        }
    }

    public static ModuleLoader getInstance() {
        if (instance == null) init();
        return instance;
    }

    public String sayHi() {
        return "Hi!!";
    }

    public ModuleLoader(String s) {
    }

    public ModuleLoader() {
    }

    public boolean instantiate(String className, String filename, String binding) throws Exception {
        ModuleLoader ml = ModuleLoader.getInstance();
        if (!this.equals(ml)) return ml.instantiate(className, filename, binding); else {
            Class c = Class.forName(className);
            Class[] carr = new Class[] { String.class };
            Constructor con = c.getConstructor(carr);
            Object[] oarr = new Object[] { filename };
            Object o = con.newInstance(oarr);
            Config.addHandler(binding, o);
            addModule((Module) o);
            return true;
        }
    }

    public void addModule(Module m) {
        modules.addElement(m);
    }

    public void run() {
        while (true) {
            Enumeration iterator = modules.elements();
            while (iterator.hasMoreElements()) {
                try {
                    Thread.currentThread().sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
