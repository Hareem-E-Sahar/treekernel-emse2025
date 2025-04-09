package org.datanucleus.jta;

import java.lang.reflect.Constructor;
import javax.transaction.TransactionManager;
import org.datanucleus.ClassConstants;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.NucleusContext;
import org.datanucleus.PropertyNames;

/**
 * Entry point for locating a JTA TransactionManager.
 */
public class TransactionManagerFinder {

    /** The NucleusContext. */
    NucleusContext nucleusContext;

    /** List of available locator class names. */
    static String[] locators = null;

    /** Locator class to use (if any) */
    String locator = null;

    /**
     * Constructor.
     * @param ctx Context for persistence
     */
    public TransactionManagerFinder(NucleusContext ctx) {
        if (locators == null) {
            locators = ctx.getPluginManager().getAttributeValuesForExtension("org.datanucleus.jta_locator", null, null, "class-name");
        }
        String jtaLocator = ctx.getPersistenceConfiguration().getStringProperty(PropertyNames.PROPERTY_TRANSACTION_JTA_LOCATOR);
        if (jtaLocator != null) {
            locator = ctx.getPluginManager().getAttributeValueForExtension("org.datanucleus.jta_locator", "name", jtaLocator, "class-name");
        }
        nucleusContext = ctx;
    }

    /**
     * Accessor for the accessible JTA transaction manager.
     * @param clr ClassLoader resolver
     * @return The JTA manager found (if any)
     */
    public TransactionManager getTransactionManager(ClassLoaderResolver clr) {
        if (locator != null) {
            TransactionManager tm = getTransactionManagerForLocator(clr, locator);
            if (tm != null) {
                return tm;
            }
        } else {
            for (int i = 0; i < locators.length; i++) {
                TransactionManager tm = getTransactionManagerForLocator(clr, locators[i]);
                if (tm != null) {
                    return tm;
                }
            }
        }
        return null;
    }

    /**
     * Convenience method to get the TransactionManager for the specified locator class.
     * @param clr ClassLoader resolver
     * @param locatorClassName Class name for the locator
     * @return The TransactionManager (if found)
     */
    protected TransactionManager getTransactionManagerForLocator(ClassLoaderResolver clr, String locatorClassName) {
        try {
            Class cls = clr.classForName(locatorClassName);
            if (cls != null) {
                TransactionManagerLocator loc = null;
                try {
                    Class[] params = new Class[] { ClassConstants.NUCLEUS_CONTEXT };
                    Constructor ctor = cls.getConstructor(params);
                    Object[] args = new Object[] { nucleusContext };
                    loc = (TransactionManagerLocator) ctor.newInstance(args);
                } catch (NoSuchMethodException nsme) {
                    loc = (TransactionManagerLocator) cls.newInstance();
                }
                if (loc != null) {
                    TransactionManager tm = loc.getTransactionManager(clr);
                    if (tm != null) {
                        return tm;
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
}
