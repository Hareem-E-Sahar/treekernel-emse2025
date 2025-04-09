package net.sf.frozen.utils.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Utility methods for handling Reflection.
 * 
 * @author Inácio Ferrarini (inacioferrarini at users.sourceforge.net)
 */
public class ReflectionHelper {

    /**
	 * Instantiates <tt>cls</tt>, using the cls´ constructor wich contains the parameters
	 * contained in <tt>paramTypes</tt>. If <tt>cls</tt> doesn´t contains a Constructor with the
	 * same types (and in the same order), the instantiation will fail.
	 * 
	 * @param cls
	 *           the class to be instantiated
	 * @param paramTypes
	 *           an Class[] containg the types of the constructor´s parameters
	 * @param paramValues
	 *           an Object[] containin the values of the constructor´s parameters
	 * @return the instantiated object as java.lang.Object
	 * @throws IllegalAccessException
	 *            If the Constructor is not visible or accessible from ReflectionHelper
	 * @throws IllegalArgumentException
	 *            If any specified type is incompatible with the waited type
	 * @throws InstantiationException
	 *            If <tt>cls</tt> is not instanciable (such interface or an abstract class)
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 *            If the specified constructor was not found in the <tt>cls</tt> class
	 * @throws SecurityException
	 *            If any security violation occurs
	 */
    public static Object instantiateObject(Class cls, Class[] paramTypes, Object[] paramValues) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
        Constructor ct = cls.getConstructor(paramTypes);
        Object o = ct.newInstance(paramValues);
        return o;
    }
}
