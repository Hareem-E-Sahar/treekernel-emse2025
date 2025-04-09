package org.jwaim.core.util;

import java.lang.reflect.Constructor;
import org.jwaim.core.ModuleCommunicationLine;
import org.jwaim.core.interfaces.Module;
import org.jwaim.core.logger.JWAIMLogger;

/**
 * Utility methods to handle indirect/dynamic class loading with class name.  
 */
public final class DynamicClassLoading {

    /**
	 * Tries to instantiate module with given class name. Class must have constructor matching that of Module.
	 * @param className class name of the object to instantiate
	 * @param logger 
	 * @param moduleCommunicationLine 
	 * @return Instantiated object
	 * @throws Exception
	 */
    @SuppressWarnings("unchecked")
    public static final Module instantiateModule(String className, ModuleCommunicationLine moduleCommunicationLine, JWAIMLogger logger) throws Exception {
        Class c = Class.forName(className);
        Constructor cons = c.getConstructor(ModuleCommunicationLine.class, JWAIMLogger.class);
        Object cf = cons.newInstance(moduleCommunicationLine, logger);
        return (Module) cf;
    }
}
