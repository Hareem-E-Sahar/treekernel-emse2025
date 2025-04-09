package Script.UAT.Framework;

import java.util.*;
import java.lang.reflect.*;

public class ObjectEngine {

    private AutoConfig _config;

    private ITestObjectPool _objPool;

    private HashMap _objectCache;

    private String[] _objectTypes;

    private static boolean _useCache;

    private String _url;

    private int _cacheStep;

    public ObjectEngine() {
        _useCache = true;
        this._objectCache = new HashMap();
        this._config = AutoConfig.GetAutoConfigIns();
        this._objPool = GetITestObjectPool();
    }

    public void UseCache(boolean isCache) {
        _useCache = isCache;
    }

    public static boolean GetCacheStatus() {
        return _useCache;
    }

    public Object GetTestObject(String item, String property) {
        Object temp = null;
        if (item == null && item.length() < 1) {
            return null;
        }
        if (property == null) {
            property = "";
        }
        if (property.equalsIgnoreCase("")) {
            temp = this._objPool.GetObjectByAI(item);
        } else if (property.startsWith(".")) {
            temp = this._objPool.GetObjectByProperties(new String[] { property }, new String[] { item });
        } else if (property.equalsIgnoreCase("index")) {
            temp = this._objPool.GetObjectByIndex(Integer.parseInt(item));
            return temp;
        } else {
            String[] objectTypes = this._objPool.GetObjectTypes();
            for (int i = 0; i < objectTypes.length; i++) {
                if (objectTypes[i].equalsIgnoreCase(property)) {
                    int objIndex = 0;
                    try {
                        objIndex = Integer.parseInt(item);
                        temp = this._objPool.GetObjectByType(property, Integer.parseInt(item));
                    } catch (NumberFormatException e) {
                        temp = this._objPool.GetObjectByType(property, item);
                    }
                }
                if (temp != null) {
                    break;
                }
            }
        }
        return temp;
    }

    private ITestObjectPool GetITestObjectPool() {
        ITestObjectPool objPool = null;
        String projectType = _config.GetProjectType();
        String dll = _config.GetRelateObjectByType(projectType);
        try {
            if (dll.indexOf(".") < 0) {
                dll = "Script.UAT.Framework." + dll;
            }
            Class cls = Class.forName(dll);
            Class partypes[] = new Class[1];
            partypes[0] = Object.class;
            Constructor ct = cls.getConstructor(partypes);
            Object arglist[] = new Object[1];
            arglist[0] = TestJob.GetRootObject();
            objPool = (ITestObjectPool) (ct.newInstance(arglist));
        } catch (Exception e) {
            throw new RuntimeException("Class not found.");
        }
        return objPool;
    }
}
