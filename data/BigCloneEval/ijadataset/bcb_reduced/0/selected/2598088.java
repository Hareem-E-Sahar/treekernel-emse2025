package persistence;

import java.io.*;
import java.util.*;
import java.sql.*;
import java.lang.reflect.*;

public class SQLPersistence extends XMLMapper implements PersistenceManager {

    private Connection conn;

    private SQLGenerator gene;

    public SQLPersistence(String filename, Connection cnx, SQLGenerator g) {
        this.conn = cnx;
        load(filename);
        this.gene = g;
    }

    public Object loadObject(String clsn, String id) {
        try {
            ClassDef cd = (ClassDef) listCD.get(clsn);
            String qr = gene.getSelect(cd, cd.getPKName() + "=" + id);
            System.out.println(qr);
            Statement stmt = conn.createStatement();
            ResultSet res = stmt.executeQuery(qr);
            if (res == null) return null;
            if (res.next()) {
                Class cls = Class.forName(clsn);
                Class[] cprm = {};
                Object[] pprm = {};
                Object resobj = cls.getConstructor(cprm).newInstance(pprm);
                resobj = fillObj(cd, resobj, res);
                res.close();
                stmt.close();
                return resobj;
            } else {
                res.close();
                stmt.close();
                return null;
            }
        } catch (SQLException e) {
            System.err.println("SQL error trying to load an object of type : " + clsn);
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found during trying to load an object of type : " + clsn);
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e) {
            System.err.println("Method not found during trying to load an object of type : " + clsn);
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("Exception during trying to load an object of type : " + clsn);
            e.printStackTrace();
            return null;
        }
    }

    private Object fillObj(ClassDef cd, Object resobj, ResultSet res) throws Exception {
        Hashtable cln = cd.getAttributs();
        Enumeration ek = cln.keys();
        while (ek.hasMoreElements()) {
            Attribut atr = (Attribut) cln.get(ek.nextElement());
            Class[] prmt = new Class[1];
            Object[] prm = new Object[1];
            if (atr.type.equals("String")) {
                prm[0] = res.getString(atr.columnName);
                prmt[0] = String.class;
            } else if (atr.type.equals("double")) {
                prm[0] = new Double(res.getDouble(atr.columnName));
                prmt[0] = double.class;
            } else if (atr.type.equals("int")) {
                prm[0] = new Integer(res.getInt(atr.columnName));
                prmt[0] = int.class;
            }
            System.out.println("atr.setMethod : " + atr.setMethod);
            Method m = resobj.getClass().getMethod(atr.setMethod, prmt);
            m.invoke(resobj, prm);
        }
        if (cd.getExtend() == null) return resobj; else return fillObj(cd.getExtend(), resobj, res);
    }

    public void saveObject(Object obj) {
        try {
            ClassDef cd = getCD(obj.getClass().getName());
            updateTable(cd, obj);
        } catch (Exception ioe) {
            System.err.println("Type : " + ioe.getClass().getName());
            System.err.println("message : " + ioe.getMessage());
            ioe.printStackTrace();
        }
    }

    public void insertObject(Object obj) {
        try {
            ClassDef cd = getCD(obj.getClass().getName());
            insertTable(cd, obj);
        } catch (Exception ioe) {
            System.err.println("Type : " + ioe.getClass().getName());
            System.err.println("message : " + ioe.getMessage());
            ioe.printStackTrace();
        }
    }

    private void insertTable(ClassDef cd, Object obj) throws SQLException {
        if (cd.getExtend() != null) insertTable(cd.getExtend(), obj);
        System.err.println("*** under coding ***");
        Hashtable vals = getVals(cd, obj);
        String qr = gene.getInsert(cd, vals);
        System.out.println("QR :" + qr);
    }

    private void updateTable(ClassDef cd, Object obj) throws SQLException {
        String pk = cd.getPKName();
        Hashtable vals = getVals(cd, obj);
        String qr = gene.getUpdate(cd, vals);
        System.out.println("QR :" + qr);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(qr);
        if (cd.getExtend() != null) updateTable(cd.getExtend(), obj);
    }

    public void deleteObject(Object obj) {
    }

    public void begin() {
    }

    public void commit() {
    }

    public void rollback() {
    }

    private ClassDef getCD(String namecd) {
        return (ClassDef) listCD.get(namecd);
    }

    private Hashtable getVals(ClassDef cd, Object obj) {
        try {
            Hashtable vals = new Hashtable();
            Hashtable cln = cd.getAttributs();
            Enumeration ek = cln.keys();
            while (ek.hasMoreElements()) {
                Attribut att = (Attribut) cln.get(ek.nextElement());
                String getCmd = att.getMethod;
                Class[] prmt = {};
                Object[] prm = {};
                Method m = obj.getClass().getMethod(getCmd, prmt);
                Object j = m.invoke(obj, prm);
                vals.put(att.name, j);
            }
            return vals;
        } catch (Exception e) {
            System.err.println("Error getting values from an object ! ");
            System.err.println("Type : " + cd.getName());
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
}
