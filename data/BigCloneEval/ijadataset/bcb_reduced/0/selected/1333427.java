package persistent;

import java.util.ArrayList;
import java.util.Iterator;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.Constructor;
import java.lang.System;
import model.Ficha;

public class ManagerOnLSD {

    private ManagerOnLSD instance = null;

    private String engine = null;

    private static ArrayList supportedEngines = new ArrayList(2);

    private Connection connection = null;

    private String defaultEngine = "Access";

    public ManagerOnLSD() {
        loadEngines();
        if (this.isSupported(this.defaultEngine)) {
            this.setEngine(this.defaultEngine);
        }
    }

    public ManagerOnLSD(String engine) {
        loadEngines();
        if (this.isSupported(engine)) {
            this.setEngine(engine);
        }
    }

    public ManagerOnLSD(String engine, String host, String name, String user, String pass) {
        loadEngines();
        if (this.isSupported(engine)) {
            this.setEngine(engine);
        }
    }

    private Class getDriver() {
        try {
            return Class.forName("persistent." + this.engine);
        } catch (Exception e) {
            return null;
        }
    }

    private Method getEngineMethod(String method) {
        try {
            Class<?> driver = this.getDriver();
            return driver.getMethod(method, new Class<?>[] {});
        } catch (Exception e) {
            System.out.println(this.getDriver().isInstance(null));
            return null;
        }
    }

    private Object invokeEngineMethod(String method) {
        Method methodToInvoke = this.getEngineMethod(method);
        Object driverInstance = null;
        try {
            try {
                driverInstance = this.getDriver().newInstance();
            } catch (Exception e) {
            }
            return methodToInvoke.invoke(driverInstance, (Object) methodToInvoke.getParameterTypes());
        } catch (InvocationTargetException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }

    public String getUrl() {
        try {
            Class driver = this.getDriver();
            Method getUrl = this.getEngineMethod("getUrl");
            Object driverInstance = driver.getConstructor(new Class[] {}).newInstance(new Object[] {});
            return (String) getUrl.invoke(driverInstance, new Object[] {});
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public ManagerOnLSD getManager() {
        if (this.instance == null) {
            if (this.getEngine() == null) {
                this.instance = new ManagerOnLSD();
            } else {
                this.instance = new ManagerOnLSD(this.getEngine());
            }
        }
        return this.instance;
    }

    private static void loadEngines() {
        supportedEngines.add("Mysql");
        supportedEngines.add("Access");
    }

    private Connection getConnection() {
        if (this.connection == null) {
            this.connection = this.connect();
        }
        return this.connection;
    }

    private Connection connect() {
        try {
            Class driver = this.getDriver();
            Method connect = this.getEngineMethod("connect");
            Connection conn = (Connection) connect.invoke(driver.newInstance(), new Object[] {});
            return conn;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public void pullThePlug() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            System.out.println("Error al cerrar la conexión.");
        }
    }

    public String getEngine() {
        return this.engine;
    }

    public void setEngine(String engine) {
        if (this.isSupported(engine)) {
            this.engine = engine;
        }
    }

    private boolean isSupported(String engine) {
        if (supportedEngines.contains(engine)) {
            return true;
        }
        return false;
    }

    private void query(String sql) {
        try {
            Statement st = this.getConnection().createStatement();
            st.executeUpdate(sql);
            st.close();
        } catch (SQLException e) {
            System.out.println("Error al ejecutar sql.\n" + e.getMessage());
        }
    }

    private ResultSet queryRs(String sql) {
        ResultSet rs = null;
        try {
            Statement st = this.getConnection().createStatement();
            rs = st.executeQuery(sql);
        } catch (SQLException e) {
            System.out.println("Error al ejecutar sql.\n" + e.getMessage());
        }
        return rs;
    }

    public Class getPersistentObject(Object object) {
        try {
            return Class.forName("persistent." + object.getClass().getSimpleName());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public Object invokeMethodOn(String method, Object object, Class[] paramsClass, Object[] paramsObject) {
        try {
            Method objectMethod = object.getClass().getMethod(method, paramsClass);
            return (Object) objectMethod.invoke(object, paramsObject);
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }

    public Object invokeMethodOn(String method, Class object) {
        return this.invokeMethodOn(method, object, new Class[] { null }, new Object[] { null });
    }

    public boolean save(Object toSave) {
        Object persistentObject;
        try {
            persistentObject = this.getPersistentObject(toSave).newInstance();
        } catch (Exception e) {
            persistentObject = null;
        }
        try {
            this.query(this.invokeMethodOn("save", persistentObject, new Class[] { toSave.getClass() }, new Object[] { toSave }).toString());
        } catch (Exception e) {
            System.out.println(e);
        }
        return true;
    }

    public boolean save(Object toSave, Object[] extraData) {
        Class persistentObject = this.getPersistentObject(toSave);
        this.query(this.invokeMethodOn("save", persistentObject, new Class[] { extraData.getClass() }, extraData).toString());
        return true;
    }

    public boolean delete(Object toDelete) {
        Class persistentObject = this.getPersistentObject(toDelete);
        this.query(this.invokeMethodOn("delete", persistentObject, new Class[] { null }, new Object[] { null }).toString());
        return false;
    }

    private ArrayList getAsArrayList(String sql) {
        System.out.println(sql);
        ResultSet rs = this.queryRs(sql);
        ArrayList resultados = new ArrayList();
        Integer i = 0;
        try {
            System.out.println(rs.getFetchSize());
            if (!rs.wasNull() && rs.getFetchSize() > 0) {
                while (rs.next()) {
                    resultados.add(rs.getArray(i));
                    i++;
                }
                System.out.println(resultados);
            }
        } catch (SQLException e) {
            System.out.println(e + ": " + e.getMessage());
        }
        return resultados;
    }

    public ArrayList get(String className, Integer classId) {
        String sql = "SELECT * FROM " + className.toLowerCase() + " WHERE id=" + classId + ";";
        return this.getAsArrayList(sql);
    }

    public ArrayList get(String className, String[] campos, String[] valores) {
        String sqlCampos = "";
        for (int i = 0; i < campos.length; i++) {
            if (sqlCampos == "") {
                sqlCampos = campos[i] + "='" + valores[i] + "'";
            } else {
                sqlCampos += " AND " + campos[i] + "='" + valores[i] + "'";
            }
        }
        String sql = "SELECT * FROM " + className.toLowerCase() + " WHERE " + sqlCampos + ";";
        return this.getAsArrayList(sql);
    }

    public ArrayList get(String className, String campo, String valor) {
        String sql = "SELECT * FROM " + className + " WHERE " + campo + "='" + valor + "';";
        return this.getAsArrayList(sql);
    }
}
