package oldmcdata.analyzers.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Title:
 * Description:
 *Copyright: 2000 - 2005
 *  
 * @author Stephen C. Upton
 * @version 1.0
 */
public class StatisticFactory {

    private StatisticFactory() {
    }

    public static Statistic getInstance(String name) {
        Statistic statistic = null;
        Constructor<?> ctor = null;
        String fullyQualifiedClassName = "oldmcdata.analyzers.utils." + name;
        try {
            Class<?> c = Class.forName(fullyQualifiedClassName);
            ctor = c.getConstructor((Class[]) null);
            statistic = (Statistic) ctor.newInstance((Object[]) null);
        } catch (ClassNotFoundException cfne) {
            handleException(cfne, "Class: " + fullyQualifiedClassName + " Does Not Exist, or not on CLASSPATH");
        } catch (NoSuchMethodException nsme) {
            handleException(nsme, "Class: " + fullyQualifiedClassName + " has no constructor with 0 arguments");
        } catch (InvocationTargetException ite) {
            handleException(ite, "Can not construct instance of Class: " + fullyQualifiedClassName);
        } catch (IllegalAccessException iae) {
            handleException(iae, "Can not access: " + fullyQualifiedClassName + " - not public");
        } catch (InstantiationException ie) {
            handleException(ie, "Class: " + fullyQualifiedClassName + " can not be constructed as it is an interface or abstract class");
        }
        return statistic;
    }

    /** convenience method for consistently handling messages from the
     *  getInstance() method.
     */
    private static void handleException(Exception ex, String message) {
        System.out.println(message);
        ex.printStackTrace(System.out);
        System.exit(-1);
    }
}
