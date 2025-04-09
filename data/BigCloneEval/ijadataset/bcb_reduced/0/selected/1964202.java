package com.swing.dispatch;

import com.swing.utils.GenericDispatcher;
import com.swing.json.JSONObject;
import com.swing.utils.DbConnection;
import com.swing.utils.DbSource;
import java.lang.reflect.Constructor;
import org.apache.log4j.Logger;

/**
 * <b>Primitve</b> class
 *
 * <p>Dispatch business method </p>
 *
 * @author swiss info
 *
 */
public class Primitive extends GenericDispatcher {

    private static Logger logger = Logger.getLogger(Primitive.class);

    /**
     *
     * @param o
     * @return
     */
    public String getPrimitiveDB(String cls, String render, Object oParam[]) {
        String getPrimitiveDB = "";
        try {
            cObject = null;
            Object o = null;
            Class c = getSwingServicesClass("com.swing.model.dal." + cls);
            Constructor cx = c.getConstructor(DbConnection.class);
            o = cx.newInstance(DbSource.getConnection());
            cObject = invokeMethod(o, oParam, render);
            if (cObject != null) {
                getPrimitiveDB = cObject.toString();
            }
            cObject = null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            release();
        }
        return getPrimitiveDB;
    }

    public String getPrimitiveDB(String cls, String method, JSONObject row) {
        Object oParam[] = new Object[1];
        oParam[0] = row;
        return getPrimitiveDB(cls, method, oParam);
    }

    public String getPrimitiveDB(String cls, String method) {
        Object oParam[] = new Object[0];
        return getPrimitiveDB(cls, method, oParam);
    }

    public String getPrimitiveDB(String cls, String render, String param1) {
        Object oParam[] = new Object[1];
        oParam[0] = param1;
        return getPrimitiveDB(cls, render, oParam);
    }

    public String getPrimitiveDB(String cls, String render, String param1, String param2) {
        Object oParam[] = new Object[2];
        oParam[0] = param1;
        oParam[1] = param2;
        return getPrimitiveDB(cls, render, oParam);
    }

    public String getPrimitiveDB(String cls, String render, String param1, String param2, String param3) {
        Object oParam[] = new Object[3];
        oParam[0] = param1;
        oParam[1] = param2;
        oParam[2] = param3;
        return getPrimitiveDB(cls, render, oParam);
    }
}
