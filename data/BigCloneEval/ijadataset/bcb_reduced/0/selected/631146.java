package org.dbe.composer.wfengine.bpel.server.engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import javax.naming.InitialContext;
import org.apache.log4j.Logger;
import org.dbe.composer.wfengine.SdlException;
import org.dbe.composer.wfengine.bpel.SdlBusinessProcessException;
import org.dbe.composer.wfengine.bpel.config.ISdlEngineConfiguration;
import org.dbe.composer.wfengine.bpel.impl.ISdlAlarmManager;
import org.dbe.composer.wfengine.bpel.impl.ISdlBusinessProcessEngineInternal;
import org.dbe.composer.wfengine.bpel.impl.ISdlLockManager;
import org.dbe.composer.wfengine.bpel.impl.ISdlProcessManager;
import org.dbe.composer.wfengine.bpel.impl.ISdlQueueManager;
import org.dbe.composer.wfengine.bpel.server.IServiceDeploymentProvider;
import org.dbe.composer.wfengine.bpel.server.addressing.ISdlPartnerAddressing;
import org.dbe.composer.wfengine.bpel.server.addressing.SdlPartnerAddressing;
import org.dbe.composer.wfengine.bpel.server.addressing.pdef.ISdlPartnerAddressingFactory;
import org.dbe.composer.wfengine.bpel.server.addressing.pdef.ISdlPartnerAddressingProvider;
import org.dbe.composer.wfengine.bpel.server.addressing.pdef.SdlPartnerAddressingFactory;
import org.dbe.composer.wfengine.bpel.server.admin.IServiceEngineAdministration;
import org.dbe.composer.wfengine.bpel.server.admin.rdebug.server.IBpelAdmin;
import org.dbe.composer.wfengine.bpel.server.admin.rdebug.server.ServiceRemoteDebugImpl;
import org.dbe.composer.wfengine.bpel.server.deploy.IServiceDeploymentHandlerFactory;
import org.dbe.composer.wfengine.bpel.server.engine.storage.SdlPersistentStoreFactory;
import org.dbe.composer.wfengine.bpel.server.engine.storage.SdlStorageException;
import org.dbe.composer.wfengine.bpel.server.engine.storage.sql.SdlDataSource;
import org.dbe.composer.wfengine.bpel.server.engine.transaction.IServiceTransactionManagerFactory;
import org.dbe.composer.wfengine.bpel.server.logging.ISdlDeploymentLoggerFactory;
import org.dbe.composer.wfengine.bpel.server.service.IServiceCatalog;
import org.dbe.composer.wfengine.timer.SdlTimerManager;
import org.dbe.composer.wfengine.util.SdlUtil;
import org.dbe.composer.wfengine.work.SdlWorkManager;
import commonj.timers.TimerManager;
import commonj.work.Work;
import commonj.work.WorkException;
import commonj.work.WorkManager;

/**
 * Maintains a singleton instance of the engine.
 */
public class SdlEngineFactory {

    private static final Logger logger = Logger.getLogger(SdlEngineFactory.class.getName());

    /** The deployment provider which manages all process deployments */
    private static IServiceDeploymentProvider sDeploymentProvider;

    /** The singleton engine instance */
    private static SdlBpelEngine sEngine;

    /** The singleton admin instance */
    private static IServiceEngineAdministration sAdmin;

    /** The logger for creating process log files */
    private static IProcessLogger sProcessLogger;

    /** The partner addressing layer */
    private static ISdlPartnerAddressing sPartnerAddressing;

    /** WorkManager impl for asynchronous work */
    private static WorkManager sWorkManager;

    /** Timer Manager impl for scheduling alarms */
    private static TimerManager sTimerManager;

    /** The current configuration settings */
    private static ISdlEngineConfiguration sConfig;

    /** Provides mappings between principals and partners */
    private static ISdlPartnerAddressingProvider sAddressProvider;

    /** Deployment handler factory. */
    private static IServiceDeploymentHandlerFactory sDeploymentHandlerFactory;

    /** Global sdl and wsdl catalog. */
    private static IServiceCatalog sServiceCatalog;

    /** Deployment logger factory. */
    private static ISdlDeploymentLoggerFactory sDeploymentLoggerFactory;

    /** The singleton remote debug engine instance */
    private static IBpelAdmin sRemoteDebugImpl;

    /** Flag indicating the persistent store is available in the configuration. */
    private static boolean sPersistentStoreConfiguration;

    /** Flag indicating the persistent store is ready for use. */
    private static boolean sPersistentStoreReadyForUse;

    /** String indicating the error message from the persistent store if it is not ready for use. */
    private static String sPersistentStoreError;

    /** The singleton transaction manager factory. */
    private static IServiceTransactionManagerFactory sTransactionManagerFactory;

    /**
     * Initialize the BPEL engine.
     * @param aConfig engine config settings
     * @throws Exception
     */
    public static void init(ISdlEngineConfiguration aConfig) throws SdlException {
        logger.debug("init(ISdlEngineConfiguration)");
        initializeStorage(aConfig);
        initializeWorkManager();
        initializeTimerManager();
        ISdlProcessManager processManager = createSdlProcessManager();
        ISdlQueueManager queueManager = createSdlQueueManager();
        ISdlAlarmManager alarmManager = createAlarmManager();
        ISdlLockManager lockManager = createLockManager();
        sAdmin = createSdlEngineAdmin();
        sRemoteDebugImpl = createRemoteDebugImpl(aConfig.getMapEntry(ISdlEngineConfiguration.REMOTE_DEBUG_ENTRY));
        sEngine = createNewSdlEngine(queueManager, processManager, alarmManager, lockManager);
        SdlPartnerAddressing addressLayer = new SdlPartnerAddressing();
        ISdlPartnerAddressingFactory factory = SdlPartnerAddressingFactory.newInstance();
        sAddressProvider = factory.getProvider();
        addressLayer.setProvider(sAddressProvider);
        sPartnerAddressing = addressLayer;
        IServiceDeploymentProvider provider = (IServiceDeploymentProvider) createConfigObject(ISdlEngineConfiguration.DEPLOYMENT_PROVIDER);
        sDeploymentProvider = provider;
        sEngine.setPlanManager(sDeploymentProvider);
        processManager.setPlanManager(sDeploymentProvider);
        sProcessLogger = createSdlProcessLogger();
        sProcessLogger.setEngine(getSdlEngine());
        sServiceCatalog = (IServiceCatalog) createConfigObject(ISdlEngineConfiguration.SERVICE_CATALOG_ENTRY);
        sDeploymentLoggerFactory = (ISdlDeploymentLoggerFactory) createConfigObject(ISdlEngineConfiguration.DEPLOYMENT_LOG_ENTRY);
        sDeploymentHandlerFactory = (IServiceDeploymentHandlerFactory) createConfigObject(ISdlEngineConfiguration.DEPLOYMENT_HANDLER_ENTRY);
    }

    /**
     * Publicly accessible method to initialize storage component of the engine
     * from an engine configuration object.
     *
     * @throws SdlStorageException
     */
    public static void initializeStorage(ISdlEngineConfiguration aConfig) throws SdlStorageException {
        logger.debug("initializeStorage(ISdlEngineConfiguration)");
        setEngineConfig(aConfig);
        IServiceTransactionManagerFactory factory;
        try {
            factory = (IServiceTransactionManagerFactory) createConfigObject(ISdlEngineConfiguration.TRANSACTION_MANAGER_FACTORY_ENTRY);
        } catch (SdlException e) {
            logger.error("Error: " + e);
            throw new SdlStorageException(e);
        }
        setTransactionManagerFactory(factory);
        initializePersistentStoreFactory();
    }

    /**
     * Create an object instance from config params.
     * @param aKey Key to top level entry with sub-entry that specifies the class.
     * @throws SdlException
     */
    protected static Object createConfigObject(String aKey) throws SdlException {
        logger.debug("createConfigObject() key=" + aKey);
        Map entryParams = getSdlEngineConfig().getMapEntry(aKey);
        if (entryParams == null || entryParams.isEmpty()) {
            logger.error("Error in createConfigObject as entryParams null or empty");
            throw new SdlException("Fatal error starting engine " + aKey);
        }
        return createConfigSpecificClass(entryParams);
    }

    /**
     * Creates the class that listens for process events and logs them.
     */
    protected static IProcessLogger createSdlProcessLogger() throws SdlException {
        logger.debug("createProcessLogger()");
        Map configMap = getSdlEngineConfig().getMapEntry(ISdlEngineConfiguration.PROCESS_LOGGER_ENTRY);
        return (IProcessLogger) createConfigSpecificClass(configMap);
    }

    /**
     * Creates the new engine admin instance.
     * @return A new engine admin instance.
     * @throws SdlException
     */
    protected static IServiceEngineAdministration createSdlEngineAdmin() throws SdlException {
        logger.debug("createSdlEngineAdmin(IServiceEngineAdministration)");
        String engineAdminClass = getSdlEngineConfig().getEntry(ISdlEngineConfiguration.ENGINE_ADMIN_IMPL_ENTRY, SdlEngineAdministration.class.getName());
        try {
            Class clazz = Class.forName(engineAdminClass);
            return (IServiceEngineAdministration) clazz.newInstance();
        } catch (Exception e) {
            logger.error("Error creating engine admin");
            throw new SdlException("Error creating engine admin.", e);
        }
    }

    /**
     * Creates the new remote debug engine instance.
     * @param aMap
     * @return IBpelAdmin
     * @throws SdlException
     */
    protected static IBpelAdmin createRemoteDebugImpl(Map aMap) throws SdlException {
        logger.debug("createRemoteDebugImpl(Map)");
        try {
            Class c;
            String debugClassName;
            String eventLocatorClass;
            String bpLocatorClass;
            String defaultRDebugClass = ServiceRemoteDebugImpl.class.getName();
            String defaultEventLocatorClass = "org.dbe.composer.wfengine.bpel.webserver.rdebug.client.ServiceEventHandlerLocator";
            String defaultBpLocatorClass = "org.dbe.composer.wfengine.bpel.webserver.rdebug.client.ServiceBreakpointHandlerLocator";
            if (aMap == null || aMap.isEmpty()) {
                debugClassName = defaultRDebugClass;
                eventLocatorClass = defaultEventLocatorClass;
                bpLocatorClass = defaultBpLocatorClass;
            } else {
                debugClassName = (String) aMap.get(ISdlEngineConfiguration.REMOTE_DEBUG_IMPL_ENTRY);
                if (SdlUtil.isNullOrEmpty(debugClassName)) {
                    debugClassName = defaultRDebugClass;
                }
                eventLocatorClass = (String) aMap.get(ISdlEngineConfiguration.EVENT_HANDLER_LOCATOR_ENTRY);
                if (SdlUtil.isNullOrEmpty(eventLocatorClass)) {
                    eventLocatorClass = defaultEventLocatorClass;
                }
                bpLocatorClass = (String) aMap.get(ISdlEngineConfiguration.BREAKPOINT_HANDLER_LOCATOR_ENTRY);
                if (SdlUtil.isNullOrEmpty(bpLocatorClass)) {
                    bpLocatorClass = defaultBpLocatorClass;
                }
            }
            c = Class.forName(debugClassName);
            Constructor constructor = c.getConstructor(new Class[] { String.class, String.class });
            return (IBpelAdmin) constructor.newInstance(new Object[] { eventLocatorClass, bpLocatorClass });
        } catch (Exception e) {
            logger.error("Error: " + e);
            throw new SdlException("Error creating remote debug engine implementation.", e);
        }
    }

    /**
     * Creates the new engine instance. This provides a means of changing the underlying
     * engine class based on the config file which is something that we do in order
     * to easily swap in our clustered version of the engine.
     * @param aQueueManager
     * @param aProcessManager
     * @param aAlarmManager
     * @param aLockManager
     */
    protected static SdlBpelEngine createNewSdlEngine(ISdlQueueManager aQueueManager, ISdlProcessManager aProcessManager, ISdlAlarmManager aAlarmManager, ISdlLockManager aLockManager) throws SdlException {
        logger.debug("createNewEngine(ISdlQueueManager, ISdlProcessManager, ISdlAlarmManager, ISdlLockManager)");
        String engineClass = getSdlEngineConfig().getEntry(ISdlEngineConfiguration.ENGINE_IMPL_ENTRY, SdlBpelEngine.class.getName());
        try {
            Class clazz = Class.forName(engineClass);
            Constructor cons = clazz.getConstructor(new Class[] { ISdlEngineConfiguration.class, ISdlQueueManager.class, ISdlProcessManager.class, ISdlAlarmManager.class, ISdlLockManager.class });
            return (SdlBpelEngine) cons.newInstance(new Object[] { getSdlEngineConfig(), aQueueManager, aProcessManager, aAlarmManager, aLockManager });
        } catch (Exception e) {
            logger.error("Error: " + e);
            throw new SdlException("Error creating engine.", e);
        }
    }

    /**
     * This method initializes the work manager used by the engine. We will first attempt
     * to lookup the work manager from the JNDI location specified in the engine config
     * file. If not specified or unable to load, then we will use default work manager.
     */
    protected static void initializeWorkManager() {
        logger.debug("initializeWorkManager()");
        sWorkManager = null;
        Map workMgrConfigMap = getSdlEngineConfig().getMapEntry(ISdlEngineConfiguration.WORK_MANAGER_ENTRY);
        if (!SdlUtil.isNullOrEmpty(workMgrConfigMap)) {
            String workMgrLocation = (String) workMgrConfigMap.get(ISdlEngineConfiguration.WM_JNDI_NAME_ENTRY);
            if (!SdlUtil.isNullOrEmpty(workMgrLocation)) {
                try {
                    InitialContext ic = new InitialContext();
                    sWorkManager = (WorkManager) ic.lookup(workMgrLocation);
                    SdlException.info("Installing work manager from location=" + workMgrLocation);
                } catch (Exception e) {
                    logger.error("Error: " + e);
                    SdlException.info("Error loading work manager from location: " + workMgrLocation);
                }
            }
        }
        if (sWorkManager == null) {
            SdlException.info("Installing default work manager.");
            sWorkManager = new SdlWorkManager();
        }
    }

    /**
     * This method initializes the persistent store factory.
     */
    public static void initializePersistentStoreFactory() {
        logger.debug("inititalizePersistentStoreFactory");
        SdlDataSource.MAIN = null;
        Map storeConfigMap = getSdlEngineConfig().getMapEntry(ISdlEngineConfiguration.PERSISTENT_STORE_ENTRY);
        if (!SdlUtil.isNullOrEmpty(storeConfigMap)) {
            sPersistentStoreConfiguration = true;
            try {
                SdlPersistentStoreFactory.init(storeConfigMap);
                sPersistentStoreReadyForUse = true;
            } catch (SdlStorageException ex) {
                logger.error("Error: " + ex);
                SdlException.logWarning("");
                ex.logError();
                SdlException.logWarning("");
                setPersistentStoreError(ex.getLocalizedMessage());
                sPersistentStoreReadyForUse = false;
            }
        }
    }

    /**
     * This method initializes the timer manager used by the engine. We will first attempt
     * to lookup the timer manager from the JNDI location specified in the engine config
     * file. If not specified or unable to load, then we will use default timer manager.
     */
    protected static void initializeTimerManager() {
        logger.debug("initialzeTimerManager()");
        sTimerManager = null;
        Map timerMgrConfigMap = getSdlEngineConfig().getMapEntry(ISdlEngineConfiguration.TIMER_MANAGER_ENTRY);
        if (!SdlUtil.isNullOrEmpty(timerMgrConfigMap)) {
            String timerMgrLocation = (String) timerMgrConfigMap.get(ISdlEngineConfiguration.TM_JNDI_NAME_ENTRY);
            if (!SdlUtil.isNullOrEmpty(timerMgrLocation)) {
                try {
                    InitialContext ic = new InitialContext();
                    sTimerManager = (TimerManager) ic.lookup(timerMgrLocation);
                    SdlException.info("Installing timer manager from location=" + timerMgrLocation);
                } catch (Exception e) {
                    logger.warn("failed to initializeTimerManager() ");
                    SdlException.info("Error loading timer manager from location=" + timerMgrLocation);
                }
            }
        }
        if (sTimerManager == null) {
            SdlException.info("Installing default timer manager.");
            sTimerManager = new SdlTimerManager();
        }
    }

    /**
     * Factory method for creating the queue manager for the engine.  The type
     * of manager to use will be determined based on information found in the
     * engine configuration.
     *
     * @return A queue manager.
     */
    protected static ISdlQueueManager createSdlQueueManager() throws SdlException {
        logger.debug("createQueueManager()");
        Map configMap = getSdlEngineConfig().getMapEntry(ISdlEngineConfiguration.QUEUE_MANAGER_ENTRY);
        return (ISdlQueueManager) createConfigSpecificClass(configMap);
    }

    /**
     * Factory method for creating the process manager for the engine.  The type
     * of manager to use will be determined based on information found in the
     * engine configuration.
     *
     * @return A process manager.
     */
    protected static ISdlProcessManager createSdlProcessManager() throws SdlException {
        Map configMap = getSdlEngineConfig().getMapEntry(ISdlEngineConfiguration.PROCESS_MANAGER_ENTRY);
        logger.debug("createSdlProcessManager() for " + configMap.toString());
        return (ISdlProcessManager) createConfigSpecificClass(configMap);
    }

    /**
     * Factory method for creating the alarm manager for the engine.  The type
     * of manager to use will be determined based on information found in the
     * engine configuration.
     *
     * @return An alarm manager.
     */
    private static ISdlAlarmManager createAlarmManager() throws SdlException {
        Map configMap = getSdlEngineConfig().getMapEntry(ISdlEngineConfiguration.ALARM_MANAGER_ENTRY);
        logger.debug("createAlarmManager() for " + configMap.toString());
        return (ISdlAlarmManager) createConfigSpecificClass(configMap);
    }

    /**
     * Factory method for creating the lock manager for the engine.  The type
     * of manager to use will be determined based on information found in the
     * engine configuration.
     *
     * @return A lock manager.
     */
    private static ISdlLockManager createLockManager() throws SdlException {
        Map configMap = getSdlEngineConfig().getMapEntry(ISdlEngineConfiguration.LOCK_MANAGER_ENTRY);
        logger.debug("createLockManager() for " + configMap.toString());
        return (ISdlLockManager) createConfigSpecificClass(configMap);
    }

    /**
     * This method takes a configuration map for a manager and instantiates that
     * manager.  This involves some simple java reflection to find the proper
     * constructer and then calling that constructor.
     *
     * @param aConfig The engine configuration map for the manager.
     * @return An engine manager (alert, queue, etc...).
     */
    public static Object createConfigSpecificClass(Map aConfig) throws SdlException {
        if (SdlUtil.isNullOrEmpty(aConfig)) {
            logger.error("Error creating manager, configuration does not exist");
            throw new SdlException("Error creating manager, configuration does not exist.");
        }
        String className = (String) aConfig.get("Class");
        if (className == null) {
            logger.error("className is null");
            throw new SdlException("Error creating the config class, no class specified in the config.");
        }
        try {
            Class clazz = Class.forName(className);
            Constructor cons = clazz.getConstructor(new Class[] { Map.class });
            Object obj = cons.newInstance(new Object[] { aConfig });
            return obj;
        } catch (InvocationTargetException ite) {
            logger.error("InvocationTargetException: " + aConfig.keySet().toString() + "\n" + ite);
            SdlException.logError(ite, "Error instantiating " + className);
            throw new SdlException("Error instantiating " + className, ite);
        } catch (Exception e) {
            logger.error("Exception: Error instantiating " + className + ": " + e);
            SdlException.logError(e, "Error instantiating " + className);
            throw new SdlException("Error instantiating " + className, e);
        }
    }

    /**
     * Gets the partner addressing
     */
    public static ISdlPartnerAddressing getPartnerAddressing() {
        return sPartnerAddressing;
    }

    /**
     * Gets a ref to the administration API
     */
    public static IServiceEngineAdministration getSdlEngineAdministration() {
        return sAdmin;
    }

    /**
     * Returns the error message if the persistent store is not ready for use.
     */
    public static String getPersistentStoreError() {
        return sPersistentStoreError;
    }

    /**
     * Getter for the deployment descriptor.
     */
    public static IServiceDeploymentProvider getSdlDeploymentProvider() {
        return sDeploymentProvider;
    }

    /**
     * Gets the process logger.
     */
    public static IProcessLogger getSdlLogger() {
        return sProcessLogger;
    }

    /**
     * Getter for the engine.
     */
    public static ISdlBusinessProcessEngineInternal getSdlEngine() {
        return sEngine;
    }

    /**
     * Gets the installed work manager.
     */
    public static WorkManager getWorkManager() {
        return sWorkManager;
    }

    /**
     * Gets the installed timer manager.
     */
    public static TimerManager getTimerManager() {
        return sTimerManager;
    }

    /**
     * Returns the singleton transaction manager factory.
     */
    public static IServiceTransactionManagerFactory getTransactionManagerFactory() {
        return sTransactionManagerFactory;
    }

    /**
     * Convenience method that schedules work to be done and translates any work exceptions
     * into our standard business process exception.
     * @param aWork
     */
    public static void schedule(Work aWork) throws SdlBusinessProcessException {
        logger.debug("schedule(Work)");
        try {
            getWorkManager().schedule(aWork);
        } catch (WorkException e) {
            logger.error("Error: " + e);
            throw new SdlBusinessProcessException("error scheduling work ", e);
        }
    }

    /**
     * Convenience method for stopping the work manager.
     */
    public static void shutDownWorkManager() {
        if (getWorkManager() instanceof SdlWorkManager) {
            logger.debug("shutDownWorkManager()");
            ((SdlWorkManager) getWorkManager()).stop();
        }
    }

    /**
     * Convenience method for stopping the timer manager.
     */
    public static void shutDownTimerManager() {
        if (getTimerManager() instanceof SdlTimerManager) {
            ((SdlTimerManager) getTimerManager()).stop();
        }
    }

    /**
     * Set the engine configuration settings.
     */
    protected static void setEngineConfig(ISdlEngineConfiguration aConfig) {
        sConfig = aConfig;
    }

    /**
     * Sets the singleton transaction manager factory.
     */
    protected static void setTransactionManagerFactory(IServiceTransactionManagerFactory aFactory) {
        sTransactionManagerFactory = aFactory;
    }

    /**
     * Sets the error message if the persistent store is not ready for use.
     */
    public static void setPersistentStoreError(String aString) {
        sPersistentStoreError = aString;
    }

    /**
     * Accessor for engine configuration settings.
     */
    public static ISdlEngineConfiguration getSdlEngineConfig() {
        return sConfig;
    }

    /**
     * Accessor fot the partner addressing provider.
     */
    public static ISdlPartnerAddressingProvider getPartnerAddressProvider() {
        return sAddressProvider;
    }

    /**
     * Accessor for the global sdl and wsdl catalog.
     */
    public static IServiceCatalog getServiceCatalog() {
        return sServiceCatalog;
    }

    /**
     * Accessor for deployment logger factory.
     */
    public static ISdlDeploymentLoggerFactory getDeploymentLoggerFactory() {
        return sDeploymentLoggerFactory;
    }

    /**
     * Access the deployment handler factory.
     */
    public static IServiceDeploymentHandlerFactory getSdlDeploymentHandlerFactory() {
        return sDeploymentHandlerFactory;
    }

    /**
     * Gets the remote debug engine instance.
     * @return IBpelAdmin
     */
    public static IBpelAdmin getRemoteDebugImpl() {
        return sRemoteDebugImpl;
    }

    /**
     * Returns true if the current configuration contains a persistent store.
     */
    public static boolean isPersistentStoreConfiguration() {
        return sPersistentStoreConfiguration;
    }

    /**
     * If engine storage is not already in ready state then we will
     * check it again before returning status.
     */
    public static boolean isEngineStorageReadyRetest() {
        if (!isEngineStorageReady()) {
            initializePersistentStoreFactory();
        }
        return isEngineStorageReady();
    }

    /**
     * Returns true if the engine storage system is ready, either not persistent
     * or the persistent and the storage is ready.
     */
    public static boolean isEngineStorageReady() {
        if (!isPersistentStoreConfiguration()) {
            return true;
        }
        return isPersistentStoreReadyForUse();
    }

    /**
     * Returns true if the persistent storage is ready for use.
     */
    public static boolean isPersistentStoreReadyForUse() {
        return sPersistentStoreReadyForUse;
    }

    /**
     * Start the BPEL engine.  Should not be called until all of
     * the expected deployments have been completed (as previously persisted
     * processes will assume their resources are available as soon as they
     * start up again).
     * @throws SdlBusinessProcessException
     */
    public static void start() throws SdlBusinessProcessException {
        if (isEngineStorageReadyRetest()) {
            sEngine.start();
        } else {
            logger.error("Error in start()");
            throw new SdlBusinessProcessException("External Storage not configured for engine - not starting");
        }
    }
}
