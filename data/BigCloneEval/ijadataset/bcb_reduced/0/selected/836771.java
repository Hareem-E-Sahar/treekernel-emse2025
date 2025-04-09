package org.collada.xml_walker;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;
import imi.loaders.Collada;

/**
 * Creates walker objects from collada schema objects
 *
 * @author paulby
 */
public class ProcessorFactory {

    private static Logger logger = Logger.getLogger("org.collada.xml_walker");

    private static final String walkerPackage = "org.collada.xml_walker.";

    /**
     * Create a procesor to handle this schemaObject
     * 
     * @param collada
     * @param schemaObj
     * @param parentProcessor
     * @return Processor or null
     */
    public static Processor createProcessor(Collada collada, Object schemaObj, Processor parentProcessor) {
        if (schemaObj == null) return null;
        Class colladaClass = collada.getClass();
        Class schemaClass = schemaObj.getClass();
        String schemaClassName = schemaClass.getName();
        String schemaObjName = schemaClassName.substring(schemaClassName.lastIndexOf('.') + 1);
        if (schemaObjName.indexOf('$') != 0) schemaObjName = schemaObjName.substring(schemaObjName.lastIndexOf('$') + 1);
        try {
            Class walkerClass = Class.forName(walkerPackage + schemaObjName + "Processor");
            Constructor con = walkerClass.getConstructor(new Class[] { colladaClass, schemaClass, Processor.class });
            return (Processor) con.newInstance(collada, schemaObj, parentProcessor);
        } catch (ClassNotFoundException ex) {
            logger.warning("No Handler for " + schemaClass + "  looking for " + schemaObjName);
        } catch (NoSuchMethodException ex) {
            logger.warning("No constructor " + schemaObjName + "(" + schemaClassName + ")");
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.getCause().printStackTrace();
            ex.printStackTrace();
        }
        return null;
    }
}
