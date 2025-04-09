package org.piuframework.persistence.dao.impl;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.piuframework.config.ConfigProperties;
import org.piuframework.context.PersistenceContext;
import org.piuframework.persistence.config.DAOFactoryConfig;
import org.piuframework.persistence.config.PersistenceLayerConfig;
import org.piuframework.persistence.dao.BaseDAO;
import org.piuframework.persistence.dao.DAOFactory;
import org.piuframework.persistence.dao.DAOFactoryException;
import org.piuframework.util.ClassUtils;

/**
 * DAOFactory implementation
 * 
 * @see org.piuframework.context.PersistenceContext
 * @see org.piuframework.persistence.PersistenceLayer
 * @see org.piuframework.persistence.dao.BaseDAO
 *
 * @author Dirk Mascher
 */
public class DAOFactoryImpl implements DAOFactory {

    public static final String PROPERTY_DAO_CLASS_NAME = "dao.class.name";

    public static final String DEFAULT_DAO_CLASS_NAME = "${dao.interface.package.name}.impl.${dao.interface.shortName}Impl";

    public static final String PROPERTIES_META_SUBST_PREFIX = "dao";

    private static final Log log = LogFactory.getLog(DAOFactoryImpl.class);

    private PersistenceContext context;

    private Map daoClassMap = new HashMap();

    private int configHashcode = -1;

    public DAOFactoryImpl(PersistenceContext context) {
        this.context = context;
    }

    /**
     * Internal helper method. Returns the top-level config object.
     * @return Config object
     */
    private PersistenceLayerConfig getPersistenceLayerConfig() {
        return context.getPersistenceLayerConfig();
    }

    public BaseDAO createDAO(Class daoInterface) throws DAOFactoryException {
        BaseDAO dao = null;
        try {
            log.debug("creating DAO for interface " + daoInterface.getName());
            int hashcode = getPersistenceLayerConfig().hashCode();
            if (configHashcode != -1 && hashcode != configHashcode) {
                synchronized (this) {
                    hashcode = getPersistenceLayerConfig().hashCode();
                    if (hashcode != configHashcode) {
                        log.debug("config object has changed, clearing DAO class object cache");
                        daoClassMap.clear();
                        configHashcode = hashcode;
                    }
                }
            }
            Class daoClass = (Class) daoClassMap.get(daoInterface.getName());
            if (daoClass == null) {
                synchronized (this) {
                    daoClass = (Class) daoClassMap.get(daoInterface.getName());
                    if (daoClass == null) {
                        daoClass = loadDAOClass(daoInterface);
                        daoClassMap.put(daoInterface.getName(), daoClass);
                    }
                }
            }
            Constructor constructor = daoClass.getConstructor(new Class[] { PersistenceContext.class });
            dao = (BaseDAO) constructor.newInstance(new Object[] { context });
        } catch (Throwable t) {
            throw new DAOFactoryException(daoInterface, t);
        }
        return dao;
    }

    private Class loadDAOClass(Class daoInterface) throws ClassNotFoundException {
        Class daoClass = null;
        DAOFactoryConfig config = getPersistenceLayerConfig().getDAOFactoryConfig();
        if (config == null || config.getProperties() == null) {
            String daoClassName = ClassUtils.substClassProperties(PROPERTIES_META_SUBST_PREFIX, daoInterface, DEFAULT_DAO_CLASS_NAME);
            log.debug("fully qualified class name of DAO " + daoClassName);
            daoClass = ClassUtils.forName(daoClassName);
        } else {
            ConfigProperties properties = config.getProperties();
            Map daoClassNameMap = properties.getTypedPropertyValueMap(PROPERTY_DAO_CLASS_NAME);
            if (daoClassNameMap == null || daoClassNameMap.isEmpty()) {
                String daoClassName = properties.getProperty(PROPERTY_DAO_CLASS_NAME, DEFAULT_DAO_CLASS_NAME);
                daoClassName = ClassUtils.substClassProperties(PROPERTIES_META_SUBST_PREFIX, daoInterface, daoClassName);
                log.debug("fully qualified class name of DAO " + daoClassName);
                daoClass = ClassUtils.forName(daoClassName);
            } else {
                for (Iterator i = daoClassNameMap.values().iterator(); i.hasNext(); ) {
                    String daoClassName = (String) i.next();
                    daoClassName = ClassUtils.substClassProperties(PROPERTIES_META_SUBST_PREFIX, daoInterface, daoClassName);
                    log.debug("fully qualified class name of DAO " + daoClassName);
                    try {
                        daoClass = ClassUtils.forName(daoClassName);
                        break;
                    } catch (ClassNotFoundException e) {
                        log.debug("DAO class not found, trying next definition");
                    }
                }
            }
        }
        if (daoClass == null) {
            log.error("DAO class not found for domain class " + daoInterface.getName());
            throw new ClassNotFoundException("DAO class not found for interface " + daoInterface.getName());
        }
        return daoClass;
    }
}
