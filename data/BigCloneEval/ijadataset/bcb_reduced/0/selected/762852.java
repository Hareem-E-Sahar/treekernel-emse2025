package gov.lanl.Database;

import org.apache.log4j.Logger;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

/**
 * Implementation of PersistentObjectFactory
 * Definition of objects is contained in a property file
 */
public class DBObjectFactory implements PersistentObjectFactory {

    static Logger cat = Logger.getLogger(DBObjectFactory.class.getName());

    private Properties props;

    private Hashtable hash;

    /**
	 * Constructor declaration
	 *
	 *
	 * @see
	 */
    public DBObjectFactory() {
        props = new Properties();
        hash = new Hashtable();
        init("persist.properties");
    }

    /**
	 * Constructor declaration
	 *
	 *
	 * @param persistFile
	 *
	 * @see
	 */
    public DBObjectFactory(String persistFile) {
        props = new Properties();
        hash = new Hashtable();
        if (persistFile == null) {
            init("persist.properties");
        } else {
            init(persistFile);
        }
    }

    /**
	 * Initialize DBObjectFactory from a properties file
	 *
	 *
	 * @param persistFile
	 *
	 * @see
	 */
    private void init(String persistFile) {
        InputStream thefile = null;
        try {
            thefile = gov.lanl.Utility.IOHelper.getInputStream(persistFile);
        } catch (Exception fe) {
            try {
                thefile = gov.lanl.Utility.IOHelper.getInputStream(System.getProperty("telemed.defaults"));
                props.load(thefile);
                thefile = gov.lanl.Utility.IOHelper.getInputStream(props.getProperty("persist.properties"));
                props = new Properties();
            } catch (Exception fe2) {
                cat.error("No property file found! " + fe2);
                System.exit(1);
            }
        }
        try {
            props.load(thefile);
        } catch (IOException ie) {
            cat.error("Failed to load " + thefile + " ", ie);
            System.exit(1);
        }
        for (Enumeration e = props.propertyNames(); e.hasMoreElements(); ) {
            String name = (String) e.nextElement();
            String persistName = props.getProperty(name);
            if (cat.isDebugEnabled()) {
                cat.debug("Mapping '" + name + "' to '" + persistName + "'");
            }
            try {
                Class CorbaClass = Class.forName(name);
                Class dbClass = Class.forName(persistName);
                Class[] params = new Class[] { CorbaClass };
                Constructor construct = dbClass.getConstructor(params);
                Object obj = CorbaClass.newInstance();
                Object[] conParams = new Object[] { obj };
                construct.newInstance(conParams);
                hash.put(name, construct);
            } catch (ClassNotFoundException ex) {
                cat.error("Can't find class for name '" + persistName, ex);
                System.exit(1);
            } catch (NoSuchMethodException ex) {
            } catch (InstantiationException ex) {
                cat.error("InstantiationException for class '" + persistName, ex);
                System.exit(1);
            } catch (IllegalAccessException ex) {
                cat.error("IllegalAccessException for class '" + persistName, ex);
                System.exit(1);
            } catch (java.lang.reflect.InvocationTargetException ex) {
                cat.error("InvocationTargetException for class '" + persistName, ex);
                System.exit(1);
            } catch (Exception ex) {
                cat.error("Exception for class '" + persistName, ex);
                System.exit(1);
            }
        }
    }

    /**
	 * Method to create a PersistentObject
	 * @param obj is object to be converted to a persistent object
	 */
    public PersistentObject createPersistentObject(Object obj) {
        String name = obj.getClass().getName();
        Constructor construct = (Constructor) hash.get(name);
        Object dbObject;
        if (construct != null) {
            try {
                Object[] conParams = new Object[] { obj };
                dbObject = construct.newInstance(conParams);
                return (PersistentObject) dbObject;
            } catch (Exception e1) {
                cat.error("constructor failed for " + name, e1);
                return null;
            }
        }
        return null;
    }

    /**
	*  get the Properties associated with DBObjectFactory
	*  @return Properties
	*/
    public Properties getProperties() {
        return props;
    }
}
