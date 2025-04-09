package org.oclc.da.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.oclc.da.exceptions.DAExceptionCodes;
import org.oclc.da.exceptions.DASystemException;
import org.oclc.da.logging.Logger;

/**
 * DynamicLoader
 *
 * A class that handles dynamically loading classes based on a full qualified
 * class name. This class also has the capability of loading classes with
 * constructors.
 * 
 * @author JCG
 *
 */
public class DynamicLoader {

    private String className = null;

    private Logger logger = Logger.newInstance();

    /** Construct a DynamicLoader instance. 
     * @param className The fully qualified name of the class to load, including
     *                  the package (e.g. "org.oclc.da.common.GenericRef")
     */
    public DynamicLoader(String className) {
        this.className = className;
    }

    /** Load the class using the default constructor. 
     * @return  The new dynamically created object.
     */
    public Object load() {
        return load(null, null);
    }

    /**
     * Load an instance of a class using the current class name and the
     * constructor parameters specified.
     * 
     * @param classes   A list of classes that one of the constructors takes
     * as parameters. Pass null if the constructor takes none.
     * @param args      The list of associated values for the classes specified.
     * 
     * @return the object
     */
    public Object load(Class[] classes, Object[] args) {
        try {
            Class theClass = Class.forName(className);
            if (classes == null) {
                classes = new Class[0];
                args = new Object[0];
            }
            Constructor constructor = theClass.getConstructor(classes);
            return constructor.newInstance(args);
        } catch (ClassNotFoundException e) {
            logger.log(DAExceptionCodes.MUST_EXIST, this, "load", null, e);
            throw new DASystemException(DAExceptionCodes.MUST_EXIST, new String[] { "class", className });
        } catch (InstantiationException e) {
            logger.log(DAExceptionCodes.MUST_EXIST, this, "load", null, e);
            throw new DASystemException(DAExceptionCodes.MUST_EXIST, new String[] { "class", className });
        } catch (IllegalAccessException e) {
            logger.log(DAExceptionCodes.MUST_EXIST, this, "load", null, e);
            throw new DASystemException(DAExceptionCodes.MUST_EXIST, new String[] { "class", className });
        } catch (NoSuchMethodException e) {
            logger.log(DAExceptionCodes.MUST_EXIST, this, "load", null, e);
            throw new DASystemException(DAExceptionCodes.MUST_EXIST, new String[] { "constructor for class", className });
        } catch (InvocationTargetException e) {
            logger.log(DAExceptionCodes.INVOCATION_ERROR, this, "load", null, e);
            throw new DASystemException(DAExceptionCodes.INVOCATION_ERROR, new String[] { "constructor", className });
        }
    }
}
