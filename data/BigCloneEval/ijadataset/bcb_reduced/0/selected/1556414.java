package org.mushroomdb.service;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.mushroomdb.util.Properties;
import org.mushroomdb.util.PropertiesHelper;

/**
 * 
 * This class loads and manages all the services.
 * 
 * @author Matu
 *
 */
public class ServiceManager {

    private static ServiceManager instanceReference;

    private List services;

    private Logger logger;

    /**
	 * Obtains the ServiceManager instance.
	 * @return
	 */
    public static ServiceManager getInstance() {
        if (ServiceManager.instanceReference == null) {
            ServiceManager.instanceReference = new ServiceManager();
        }
        return ServiceManager.instanceReference;
    }

    /**
	 * Private constructor.
	 */
    private ServiceManager() {
        this.services = new LinkedList();
        this.logger = Logger.getLogger(this.getClass());
        this.loadServices();
    }

    /**
	 * Return all the loaded services.
	 * @return
	 */
    public Iterator getServices() {
        return this.services.listIterator();
    }

    /**
	 * Returns the service implementation corresponding to the specified class. 
	 * @param clazz
	 * @return
	 */
    public Service getService(Class clazz) {
        Iterator iterator = this.getServices();
        while (iterator.hasNext()) {
            Service service = (Service) iterator.next();
            if (clazz.getName().equals(service.getClass().getName())) {
                return service;
            }
        }
        return null;
    }

    /**
	 * Loads the services.
	 */
    private void loadServices() {
        int index = 1;
        String serviceClass = null;
        do {
            serviceClass = Properties.getProperty(PropertiesHelper.SERVICE + "." + index);
            if (serviceClass != null) {
                Service service = this.getServiceInstance(serviceClass);
                if (service != null) {
                    this.services.add(service);
                    logger.info("Service " + serviceClass + " registered.");
                } else {
                    logger.warn("Service " + serviceClass + " was not registered.");
                }
            }
            index++;
        } while (serviceClass != null);
    }

    /**
	 * Instantiates the service from it's class.
	 * @param className
	 * @return
	 */
    private Service getServiceInstance(String className) {
        try {
            Class clazz = Class.forName(className);
            return (Service) clazz.getConstructor(new Class[] {}).newInstance(new Object[] {});
        } catch (ClassNotFoundException e) {
            this.logger.error(className, e);
            return null;
        } catch (SecurityException e) {
            this.logger.error(className, e);
            return null;
        } catch (NoSuchMethodException e) {
            this.logger.error(className, e);
            return null;
        } catch (IllegalArgumentException e) {
            this.logger.error(className, e);
            return null;
        } catch (InstantiationException e) {
            this.logger.error(className, e);
            return null;
        } catch (IllegalAccessException e) {
            this.logger.error(className, e);
            return null;
        } catch (InvocationTargetException e) {
            this.logger.error(className, e);
            return null;
        }
    }
}
