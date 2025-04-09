package de.banh.bibo.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;
import de.banh.bibo.exceptions.InitializationException;

public class ManagerFactory {

    public static final String PROVIDER_CLASS = "de.banh.bibo.model.BiboManager.providerClass";

    private static Manager instance;

    private static Logger logger = Logger.getLogger(Manager.class.getName());

    private ManagerFactory() {
    }

    /**
	 * Gibt die Instanz des BiboManagers zurück. Diese Methode ruft die Funktion create auf, welche diesen dann erstellt.
	 * 
	 * @return
	 */
    public static Manager createManager() {
        if (instance == null) {
            synchronized (Manager.class) {
                if (instance == null) {
                    String providerClass = System.getProperty(PROVIDER_CLASS);
                    if (providerClass == null) {
                        throw new IllegalStateException("Kein Provider für BiboManager spezifiziert! Bitte System-Einstellung " + PROVIDER_CLASS + " setzen.");
                    }
                    try {
                        logger.info("Lade Provider: " + providerClass);
                        Manager newInstance = create(providerClass);
                        instance = newInstance;
                    } catch (InitializationException e) {
                        logger.throwing(Manager.class.getName(), "createManager", e);
                        throw new RuntimeException("System-Exception im Konstruktor der  constructor of RecipeDBMgr provider class (" + providerClass + ")", e);
                    }
                }
            }
        }
        return instance;
    }

    /**
	 * Läd dynamisch zur Laufzeit den übergebenene Provider und erzeugt eine Instanz davon.
	 * 
	 * @param mgrProviderClass
	 * @param properties
	 * @return
	 * @throws InitializationException
	 */
    @SuppressWarnings("unchecked")
    private static Manager create(String mgrProviderClass) throws InitializationException {
        instance = null;
        try {
            @SuppressWarnings("rawtypes") Class providerClass = Class.forName(mgrProviderClass, true, Thread.currentThread().getContextClassLoader());
            Constructor<Manager> constructor = providerClass.getConstructor();
            instance = constructor.newInstance();
        } catch (InstantiationException e) {
            falscheImplementierung(mgrProviderClass, e);
        } catch (IllegalAccessException e) {
            falscheImplementierung(mgrProviderClass, e);
        } catch (NoSuchMethodException e) {
            falscheImplementierung(mgrProviderClass, e);
        } catch (ClassNotFoundException e) {
            String msg = "Provider-Klasse (" + mgrProviderClass + ") wurde nicht gefunden!";
            throw new InitializationException(msg, e);
        } catch (InvocationTargetException e) {
            String msg = "Konstruktor der Provider-Klasse (" + mgrProviderClass + ") erzeugte eine Exception: " + e.getMessage();
            logger.throwing(Manager.class.getName(), "create", e);
            throw new InitializationException(msg, e);
        }
        return instance;
    }

    /**
	 * Erzeugt eine RuntimeException  bei falscher Implentierung des Providers aus. Vorher werden noch Statusinformationen ausgegeben.
	 *  
	 * @param classname
	 * @param origin
	 */
    private static void falscheImplementierung(String classname, Exception origin) {
        logger.throwing(Manager.class.getName(), "create", origin);
        String msg = "Provider-Klasse (" + classname + ") hat eine falsche Implementation!";
        throw new RuntimeException(msg, origin);
    }
}
