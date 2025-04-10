package org.nightlabs.jfire.servermanager.ra;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.ConnectionManager;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.transaction.UserTransaction;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.config.Config;
import org.nightlabs.config.ConfigException;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.InvokeUtil;
import org.nightlabs.jfire.base.JFireBasePrincipal;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.base.JFireServerLocalLoginManager;
import org.nightlabs.jfire.base.PersistenceManagerProvider;
import org.nightlabs.jfire.base.SimplePrincipal;
import org.nightlabs.jfire.classloader.CLRegistrar;
import org.nightlabs.jfire.classloader.CLRegistrarFactory;
import org.nightlabs.jfire.classloader.CLRegistryCfMod;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jdo.cache.CacheCfMod;
import org.nightlabs.jfire.jdo.cache.CacheManagerFactory;
import org.nightlabs.jfire.jdo.notification.persistent.PersistentNotificationManagerFactory;
import org.nightlabs.jfire.module.ModuleType;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisationinit.OrganisationInitException;
import org.nightlabs.jfire.organisationinit.OrganisationInitManager;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.AuthorizedObjectRef;
import org.nightlabs.jfire.security.Role;
import org.nightlabs.jfire.security.RoleConstants;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.RoleGroupRef;
import org.nightlabs.jfire.security.RoleRef;
import org.nightlabs.jfire.security.RoleSet;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.UndeployedRoleGroupAuthorityUserRecord;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.AuthorizedObjectRefID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.jfire.security.listener.SecurityChangeController;
import org.nightlabs.jfire.server.Server;
import org.nightlabs.jfire.serverconfigurator.ServerConfigurator;
import org.nightlabs.jfire.serverinit.ServerInitManager;
import org.nightlabs.jfire.servermanager.DuplicateOrganisationException;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.NoServerAdminException;
import org.nightlabs.jfire.servermanager.OrganisationNotFoundException;
import org.nightlabs.jfire.servermanager.RoleImportSet;
import org.nightlabs.jfire.servermanager.config.CreateOrganisationConfigModule;
import org.nightlabs.jfire.servermanager.config.DatabaseCf;
import org.nightlabs.jfire.servermanager.config.J2eeServerTypeRegistryConfigModule;
import org.nightlabs.jfire.servermanager.config.JDOCf;
import org.nightlabs.jfire.servermanager.config.JFireServerConfigModule;
import org.nightlabs.jfire.servermanager.config.OrganisationCf;
import org.nightlabs.jfire.servermanager.config.OrganisationConfigModule;
import org.nightlabs.jfire.servermanager.config.ServerCf;
import org.nightlabs.jfire.servermanager.config.J2eeServerTypeRegistryConfigModule.J2eeLocalServer;
import org.nightlabs.jfire.servermanager.createorganisation.BusyCreatingOrganisationException;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationProgress;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationProgressID;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationStatus;
import org.nightlabs.jfire.servermanager.createorganisation.CreateOrganisationStep;
import org.nightlabs.jfire.servermanager.db.DatabaseAdapter;
import org.nightlabs.jfire.servermanager.deploy.DeployOverwriteBehaviour;
import org.nightlabs.jfire.servermanager.deploy.DeployedFileAlreadyExistsException;
import org.nightlabs.jfire.servermanager.deploy.DeploymentJarItem;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;
import org.nightlabs.jfire.servermanager.j2ee.JMSConnectionFactoryLookup;
import org.nightlabs.jfire.servermanager.j2ee.ServerStartNotificationListener;
import org.nightlabs.jfire.servermanager.xml.AuthorityTypeDef;
import org.nightlabs.jfire.servermanager.xml.EARApplicationMan;
import org.nightlabs.jfire.servermanager.xml.EJBJarMan;
import org.nightlabs.jfire.servermanager.xml.JFireSecurityMan;
import org.nightlabs.jfire.servermanager.xml.ModuleDef;
import org.nightlabs.jfire.servermanager.xml.RoleDef;
import org.nightlabs.jfire.servermanager.xml.RoleGroupDef;
import org.nightlabs.jfire.servermanager.xml.XMLReadException;
import org.nightlabs.jfire.shutdownafterstartup.ShutdownAfterStartupManager;
import org.nightlabs.jfire.shutdownafterstartup.ShutdownControlHandle;
import org.nightlabs.math.Base62Coder;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.IOUtil;
import org.nightlabs.util.Util;
import org.xml.sax.SAXException;

/**
 * @author marco schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class JFireServerManagerFactoryImpl implements ConnectionFactory, JFireServerManagerFactory, PersistenceManagerProvider, ServerStartNotificationListener {

    /**
	 * The serial version of this class.
	 */
    private static final long serialVersionUID = 1L;

    /**
	 * LOG4J logger used by this class
	 */
    private static final Logger logger = Logger.getLogger(JFireServerManagerFactoryImpl.class);

    private final ManagedConnectionFactoryImpl mcf;

    private final ConnectionManager cm;

    private Reference ref;

    private volatile boolean upAndRunning = false;

    private volatile boolean shuttingDown = false;

    protected J2eeServerTypeRegistryConfigModule j2eeServerTypeRegistryConfigModule;

    protected J2eeServerTypeRegistryConfigModule.J2eeLocalServer j2eeLocalServerCf;

    protected OrganisationConfigModule organisationConfigModule;

    protected CreateOrganisationConfigModule createOrganisationConfigModule;

    protected CacheCfMod cacheCfMod;

    private CLRegistrarFactory clRegistrarFactory;

    public JFireServerManagerFactoryImpl(final ManagedConnectionFactoryImpl mcf, final ConnectionManager cm) throws ResourceException {
        if (logger.isDebugEnabled()) logger.debug(this.getClass().getName() + ": CONSTRUCTOR");
        this.mcf = mcf;
        this.cm = cm;
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                shuttingDown = true;
            }
        });
        Config config = getConfig();
        boolean saveConfig = config.getConfigModule(OrganisationConfigModule.class, false) == null || config.getConfigModule(CreateOrganisationConfigModule.class, false) == null || config.getConfigModule(J2eeServerTypeRegistryConfigModule.class, false) == null || config.getConfigModule(CacheCfMod.class, false) == null;
        try {
            organisationConfigModule = config.createConfigModule(OrganisationConfigModule.class);
        } catch (ConfigException e) {
            logger.log(Level.FATAL, "Getting/creating OrganisationConfigModule failed!", e);
            throw new ResourceException(e.getMessage());
        }
        try {
            createOrganisationConfigModule = config.createConfigModule(CreateOrganisationConfigModule.class);
        } catch (ConfigException e) {
            logger.log(Level.FATAL, "Getting/creating CreateOrganisationConfigModule failed!", e);
            throw new ResourceException(e.getMessage());
        }
        try {
            j2eeServerTypeRegistryConfigModule = config.createConfigModule(J2eeServerTypeRegistryConfigModule.class);
        } catch (ConfigException e) {
            logger.log(Level.FATAL, "Getting/creating J2eeServerTypeRegistryConfigModule failed!", e);
            throw new ResourceException(e.getMessage());
        }
        try {
            cacheCfMod = config.createConfigModule(CacheCfMod.class);
        } catch (Exception e) {
            logger.error("Creating CacheCfMod failed!", e);
            throw new ResourceException(e.getMessage());
        }
        if (saveConfig) {
            try {
                config.save(false);
            } catch (ConfigException e) {
                logger.fatal("Saving configuration failed!", e);
                throw new ResourceException(e.getMessage());
            }
        }
        System.setProperty(IDGenerator.PROPERTY_KEY_ID_GENERATOR_CLASS, "org.nightlabs.jfire.idgenerator.IDGeneratorServer");
        String j2eeServerType = null;
        ServerCf localServerCf = mcf.getConfigModule().getLocalServer();
        if (localServerCf != null) {
            j2eeServerType = localServerCf.getJ2eeServerType();
        }
        if (j2eeServerType == null) {
            logger.warn("No configuration existing! Assuming that this is a 'jboss32x'. If you change the server type, you must restart!");
            j2eeServerType = Server.J2EESERVERTYPE_JBOSS32X;
        }
        j2eeLocalServerCf = null;
        for (Iterator<J2eeLocalServer> it = j2eeServerTypeRegistryConfigModule.getJ2eeLocalServers().iterator(); it.hasNext(); ) {
            J2eeServerTypeRegistryConfigModule.J2eeLocalServer jls = it.next();
            if (j2eeServerType.equals(jls.getJ2eeServerType())) {
                j2eeLocalServerCf = jls;
                break;
            }
        }
        if (j2eeLocalServerCf == null) throw new ResourceException("JFireServerConfigModule: localServer.j2eeServerType: This serverType is not registered in the J2eeServerTypeRegistryConfigModule!");
        try {
            this.clRegistrarFactory = new CLRegistrarFactory(this.getConfig().createConfigModule(CLRegistryCfMod.class));
        } catch (Exception e) {
            logger.error("Creating CLRegistrarFactory failed!", e);
            throw new ResourceException(e.getMessage());
        }
        InitialContext initialContext = null;
        try {
            initialContext = new InitialContext();
        } catch (Exception e) {
            logger.error("Obtaining JNDI InitialContext failed!", e);
            throw new ResourceException(e.getMessage());
        }
        try {
            try {
                initialContext.createSubcontext("java:/jfire");
            } catch (NameAlreadyBoundException e) {
            }
            try {
                initialContext.createSubcontext("java:/jfire/system");
            } catch (NameAlreadyBoundException e) {
            }
            try {
                initialContext.createSubcontext("jfire");
            } catch (NameAlreadyBoundException e) {
            }
            try {
                initialContext.createSubcontext("jfire/system");
            } catch (NameAlreadyBoundException e) {
            }
            String rootOrganisationID = getJFireServerConfigModule().getRootOrganisation().getOrganisationID();
            try {
                initialContext.bind(Organisation.ROOT_ORGANISATION_ID_JNDI_NAME, rootOrganisationID);
            } catch (NameAlreadyBoundException e) {
                initialContext.rebind(Organisation.ROOT_ORGANISATION_ID_JNDI_NAME, rootOrganisationID);
            }
            try {
                initialContext.bind(JMSConnectionFactoryLookup.QUEUECF_JNDI_LINKNAME, "UIL2ConnectionFactory");
            } catch (NameAlreadyBoundException e) {
                initialContext.rebind(JMSConnectionFactoryLookup.QUEUECF_JNDI_LINKNAME, "UIL2ConnectionFactory");
            }
            try {
                initialContext.bind(JMSConnectionFactoryLookup.TOPICCF_JNDI_LINKNAME, "UIL2ConnectionFactory");
            } catch (NameAlreadyBoundException e) {
                initialContext.rebind(JMSConnectionFactoryLookup.TOPICCF_JNDI_LINKNAME, "UIL2ConnectionFactory");
            }
        } catch (Exception e) {
            logger.error("Binding some config settings into JNDI failed!", e);
            throw new ResourceException(e.getMessage());
        }
        J2EEAdapter j2EEAdapter;
        try {
            j2EEAdapter = getJ2EEVendorAdapter();
        } catch (ModuleException e) {
            logger.error("Creating J2EEAdapter failed!", e);
            throw new ResourceException(e.getMessage());
        }
        try {
            try {
                initialContext.bind(J2EEAdapter.JNDI_NAME, j2EEAdapter);
            } catch (NameAlreadyBoundException nabe) {
                initialContext.rebind(J2EEAdapter.JNDI_NAME, j2EEAdapter);
            }
        } catch (Exception e) {
            logger.error("Binding J2EEAdapter into JNDI failed!", e);
            throw new ResourceException(e.getMessage());
        }
        try {
            SecurityReflector userResolver = j2EEAdapter.getSecurityReflector();
            if (userResolver == null) throw new NullPointerException("J2EEVendorAdapter " + j2EEAdapter.getClass() + ".getUserResolver() returned null!");
            try {
                initialContext.bind(SecurityReflector.JNDI_NAME, userResolver);
            } catch (NameAlreadyBoundException e) {
                initialContext.rebind(SecurityReflector.JNDI_NAME, userResolver);
            }
        } catch (Exception e) {
            logger.error("Creating SecurityReflector and binding it into JNDI failed!", e);
            throw new ResourceException(e.getMessage());
        }
        try {
            JFireServerLocalLoginManager m = new JFireServerLocalLoginManager();
            try {
                initialContext.bind(JFireServerLocalLoginManager.JNDI_NAME, m);
            } catch (NameAlreadyBoundException e) {
                initialContext.rebind(JFireServerLocalLoginManager.JNDI_NAME, m);
            }
        } catch (Exception e) {
            logger.error("Creating JFireServerLocalLoginManager and binding it into JNDI failed!", e);
            throw new ResourceException(e.getMessage());
        }
        String property_CacheManagerFactoryCreate_key = CacheManagerFactory.class.getName() + ".create";
        String property_CacheManagerFactoryCreate_value = System.getProperty(property_CacheManagerFactoryCreate_key);
        if ("false".equals(property_CacheManagerFactoryCreate_value)) {
            logger.warn("The system property \"" + property_CacheManagerFactoryCreate_key + "\" has been set to \"" + property_CacheManagerFactoryCreate_value + "\"; the CacheManagerFactory will *not* be created!");
        } else {
            for (Iterator<OrganisationCf> it = organisationConfigModule.getOrganisations().iterator(); it.hasNext(); ) {
                OrganisationCf organisation = it.next();
                String organisationID = organisation.getOrganisationID();
                try {
                    new CacheManagerFactory(this, initialContext, organisation, cacheCfMod, new File(mcf.getSysConfigDirectory()));
                } catch (Exception e) {
                    logger.error("Creating CacheManagerFactory for organisation \"" + organisationID + "\" failed!", e);
                    throw new ResourceException(e.getMessage());
                }
            }
        }
        try {
            initialContext.close();
        } catch (Exception e) {
            logger.warn("Closing InitialContext failed!", e);
        }
        try {
            getJ2EEVendorAdapter().registerNotificationListenerServerStarted(this);
        } catch (Exception e) {
            logger.error("Registering NotificationListener (for notification on server start) failed!", e);
        }
    }

    /**
	 * This method configures the server using the currently configured server configurator.
	 * @param delayMSec In case a reboot is necessary, the shutdown will be delayed by this time in milliseconds.
	 * @return Returns whether a reboot was necessary (and thus a shutdown was/will be initiated).
	 */
    public boolean configureServerAndShutdownIfNecessary(final long delayMSec) throws ModuleException {
        try {
            boolean rebootRequired = ServerConfigurator.configureServer(mcf.getConfigModule());
            if (rebootRequired) {
                shuttingDown = true;
                logger.warn("*** REBOOT REQUIRED ***");
                logger.warn("*** REBOOT REQUIRED ***");
                logger.warn("*** REBOOT REQUIRED ***");
                logger.warn("*** REBOOT REQUIRED ***");
                logger.warn("*** REBOOT REQUIRED ***");
                logger.warn("*** REBOOT REQUIRED ***");
                logger.warn("*** REBOOT REQUIRED ***");
                logger.warn("*** REBOOT REQUIRED ***");
                logger.warn("*** REBOOT REQUIRED ***");
                logger.warn("The invoked Server Configurator indicates that the server needs to be rebooted! Hence, I will shutdown the server NOW!");
                logger.warn("If this is an error and prevents your JFire Server from starting up correctly, you must exchange the ServerConfigurator in the config module " + JFireServerConfigModule.class.getName());
                logger.warn("*** REBOOT REQUIRED ***");
                logger.warn("*** REBOOT REQUIRED ***");
                logger.warn("*** REBOOT REQUIRED ***");
                logger.warn("*** REBOOT REQUIRED ***");
                logger.warn("*** REBOOT REQUIRED ***");
                logger.warn("*** REBOOT REQUIRED ***");
                logger.warn("*** REBOOT REQUIRED ***");
                logger.warn("*** REBOOT REQUIRED ***");
                logger.warn("*** REBOOT REQUIRED ***");
                Thread thread = new Thread() {

                    @Override
                    public void run() {
                        if (delayMSec > 0) try {
                            Thread.sleep(delayMSec);
                        } catch (InterruptedException ignore) {
                        }
                        try {
                            getJ2EEVendorAdapter().reboot();
                            logger.warn("*** REBOOT initiated ***");
                        } catch (Throwable e) {
                            logger.error("Shutting down server failed!", e);
                        }
                    }
                };
                thread.setDaemon(false);
                thread.start();
                return true;
            }
            return false;
        } catch (Throwable x) {
            throw new ModuleException(x);
        }
    }

    /**
	 * The creation of an organisation is not allowed, before the datastore inits are run.
	 * If you, dear reader, believe that this is a problem, please tell me. Marco :-)
	 */
    private boolean createOrganisationAllowed = false;

    @Override
    public void serverStarted() {
        logger.info("Caught SERVER STARTED event!");
        try {
            final InitialContext ctx = new InitialContext();
            try {
                if (configureServerAndShutdownIfNecessary(0)) return;
                final ServerInitManager serverInitManager = new ServerInitManager(this, mcf, getJ2EEVendorAdapter());
                final OrganisationInitManager datastoreInitManager = new OrganisationInitManager(this, mcf, getJ2EEVendorAdapter());
                logger.info("Performing early server inits...");
                serverInitManager.performEarlyInits(ctx);
                String asyncStartupString = System.getProperty(org.nightlabs.jfire.servermanager.JFireServerManagerFactory.class.getName() + ".asyncStartup");
                boolean asyncStartup = !Boolean.FALSE.toString().equals(asyncStartupString);
                if (logger.isDebugEnabled()) logger.debug(org.nightlabs.jfire.servermanager.JFireServerManagerFactory.class.getName() + ".asyncStartup=" + asyncStartupString);
                if (!asyncStartup) logger.info(org.nightlabs.jfire.servermanager.JFireServerManagerFactory.class.getName() + ".asyncStartup is false! Initialising one organisation after the other (not parallel).");
                int initOrganisationThreadCount = mcf.getConfigModule().getJ2ee().getInitOrganisationOnStartupThreadCount();
                final Set<Runnable> unfinishedInitialisations = new HashSet<Runnable>();
                ThreadPoolExecutor threadPoolExecutor = asyncStartup ? new ThreadPoolExecutor(initOrganisationThreadCount, initOrganisationThreadCount, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {

                    private int nextThreadGroupID = 0;

                    private int nextThreadID = 0;

                    @Override
                    public synchronized Thread newThread(Runnable r) {
                        ThreadGroup group = new ThreadGroup("InitOrgThreadGroup-" + (nextThreadGroupID++));
                        Thread thread = new Thread(group, r);
                        thread.setName("InitOrgThread-" + (nextThreadID++));
                        return thread;
                    }
                }) : null;
                for (OrganisationCf org : new ArrayList<OrganisationCf>(organisationConfigModule.getOrganisations())) {
                    final String organisationID = org.getOrganisationID();
                    getPersistenceManagerFactory(organisationID).getPersistenceManager().close();
                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            try {
                                logger.info("Importing roles and rolegroups into organisation \"" + organisationID + "\"...");
                                try {
                                    UserTransaction userTransaction = getJ2EEVendorAdapter().getUserTransaction(ctx);
                                    boolean doCommit = false;
                                    userTransaction.begin();
                                    try {
                                        LoginContext loginContext = new LoginContext(LoginData.DEFAULT_SECURITY_PROTOCOL, new CallbackHandler() {

                                            @Override
                                            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                                                for (int i = 0; i < callbacks.length; ++i) {
                                                    Callback cb = callbacks[i];
                                                    if (cb instanceof NameCallback) {
                                                        ((NameCallback) cb).setName(User.USER_ID_SYSTEM + User.SEPARATOR_BETWEEN_USER_ID_AND_ORGANISATION_ID + organisationID);
                                                    } else if (cb instanceof PasswordCallback) {
                                                        ((PasswordCallback) cb).setPassword(jfireSecurity_createTempUserPassword(organisationID, User.USER_ID_SYSTEM).toCharArray());
                                                    } else throw new UnsupportedCallbackException(cb);
                                                }
                                            }
                                        });
                                        loginContext.login();
                                        try {
                                            RoleImportSet roleImportSet = roleImport_prepare(organisationID);
                                            roleImport_commit(roleImportSet, null);
                                        } finally {
                                            loginContext.logout();
                                        }
                                        doCommit = true;
                                    } finally {
                                        if (doCommit) userTransaction.commit(); else userTransaction.rollback();
                                    }
                                    logger.info("Import of roles and rolegroups into organisation \"" + organisationID + "\" done.");
                                } catch (Exception x) {
                                    logger.error("Role import into organisation \"" + organisationID + "\" failed!", x);
                                }
                                PersistenceManagerFactory pmf = null;
                                try {
                                    CacheManagerFactory cmf = CacheManagerFactory.getCacheManagerFactory(ctx, organisationID);
                                    pmf = getPersistenceManagerFactory(organisationID);
                                    cmf.setupJdoCacheBridge(pmf);
                                } catch (NameAlreadyBoundException e) {
                                } catch (Exception e) {
                                    logger.error("Setting up CacheManagerFactory for organisation \"" + organisationID + "\" failed!", e);
                                }
                                if (pmf != null) {
                                    try {
                                        String createString = System.getProperty(PersistentNotificationManagerFactory.class.getName() + ".create");
                                        boolean create = !Boolean.FALSE.toString().equals(createString);
                                        if (logger.isDebugEnabled()) logger.debug(PersistentNotificationManagerFactory.class.getName() + ".create=" + createString);
                                        if (!create) logger.info(PersistentNotificationManagerFactory.class.getName() + ".create is false! Will not create PersistentNotificationManagerFactory for organisation \"" + organisationID + "\"!"); else {
                                            new PersistentNotificationManagerFactory(ctx, organisationID, JFireServerManagerFactoryImpl.this, getJ2EEVendorAdapter().getUserTransaction(ctx), pmf);
                                        }
                                    } catch (NameAlreadyBoundException e) {
                                    } catch (Exception e) {
                                        logger.error("Creating PersistentNotificationManagerFactory for organisation \"" + organisationID + "\" failed!", e);
                                    }
                                    logger.info("Initialising datastore of organisation \"" + organisationID + "\"...");
                                    try {
                                        datastoreInitManager.initialiseOrganisation(JFireServerManagerFactoryImpl.this, mcf.getConfigModule().getLocalServer(), organisationID, jfireSecurity_createTempUserPassword(organisationID, User.USER_ID_SYSTEM));
                                        logger.info("Datastore initialisation of organisation \"" + organisationID + "\" done.");
                                    } catch (Exception x) {
                                        logger.error("Datastore initialisation of organisation \"" + organisationID + "\" failed!", x);
                                    }
                                }
                            } finally {
                                synchronized (unfinishedInitialisations) {
                                    unfinishedInitialisations.remove(this);
                                    unfinishedInitialisations.notifyAll();
                                }
                            }
                        }
                    };
                    synchronized (unfinishedInitialisations) {
                        unfinishedInitialisations.add(runnable);
                    }
                    if (asyncStartup) threadPoolExecutor.execute(runnable); else runnable.run();
                }
                if (asyncStartup) {
                    synchronized (unfinishedInitialisations) {
                        while (!unfinishedInitialisations.isEmpty()) {
                            try {
                                unfinishedInitialisations.wait(60000);
                            } catch (InterruptedException x) {
                            }
                        }
                    }
                    threadPoolExecutor.shutdown();
                }
                createOrganisationAllowed = true;
                logger.info("Performing late server inits...");
                serverInitManager.performLateInits(ctx);
            } finally {
                ctx.close();
            }
            logger.info("*** JFireServer is up and running! ***");
            upAndRunning = true;
            ShutdownControlHandle shutdownControlHandle = shutdownAfterStartupManager.createShutdownControlHandle();
            shutdownAfterStartupManager.shutdown(shutdownControlHandle);
        } catch (Throwable x) {
            logger.fatal("Problem in serverStarted()!", x);
        }
    }

    private ShutdownAfterStartupManager shutdownAfterStartupManager = new ShutdownAfterStartupManager(this);

    protected ShutdownControlHandle shutdownAfterStartup_createShutdownControlHandle() {
        return shutdownAfterStartupManager.createShutdownControlHandle();
    }

    protected void shutdownAfterStartup_shutdown(ShutdownControlHandle shutdownControlHandle) {
        shutdownAfterStartupManager.shutdown(shutdownControlHandle);
    }

    @Override
    public Connection getConnection() throws ResourceException {
        if (logger.isDebugEnabled()) logger.debug(this.getClass().getName() + ": getConnection()");
        JFireServerManagerImpl ismi = (JFireServerManagerImpl) cm.allocateConnection(mcf, null);
        ismi.setJFireServerManagerFactory(this);
        return ismi;
    }

    @Override
    public Connection getConnection(ConnectionSpec cs) throws ResourceException {
        if (logger.isDebugEnabled()) logger.debug(this.getClass().getName() + ": getConnection(ConnectionSpec cs): cs = " + cs);
        return getConnection();
    }

    @Override
    public RecordFactory getRecordFactory() throws ResourceException {
        if (logger.isDebugEnabled()) logger.debug(this.getClass().getName() + ": getRecordFactory()");
        return null;
    }

    @Override
    public ResourceAdapterMetaData getMetaData() throws ResourceException {
        throw new ResourceException("NYI");
    }

    @Override
    public void setReference(Reference _ref) {
        if (logger.isDebugEnabled()) logger.debug(this.getClass().getName() + ": setReference(Reference ref): ref = " + _ref);
        this.ref = _ref;
    }

    @Override
    public Reference getReference() throws NamingException {
        return ref;
    }

    @Override
    public JFireServerManager getJFireServerManager() {
        try {
            return (JFireServerManager) getConnection();
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JFireServerManager getJFireServerManager(JFirePrincipal jfirePrincipal) {
        try {
            JFireServerManager ism = (JFireServerManager) getConnection();
            if (jfirePrincipal != null) ((JFireServerManagerImpl) ism).setJFirePrincipal(jfirePrincipal);
            return ism;
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean isNewServerNeedingSetup() {
        return mcf.getConfigModule().getLocalServer() == null;
    }

    /**
	 * @return Returns a clone of the internal config module.
	 * @throws ModuleException
	 */
    protected JFireServerConfigModule getJFireServerConfigModule() {
        JFireServerConfigModule cfMod = mcf.getConfigModule();
        cfMod.acquireReadLock();
        try {
            return (JFireServerConfigModule) cfMod.clone();
        } finally {
            cfMod.releaseLock();
        }
    }

    protected void setJFireServerConfigModule(JFireServerConfigModule cfMod) throws ConfigException {
        if (cfMod.getLocalServer() == null) throw new NullPointerException("localServer of config module must not be null!");
        if (cfMod.getDatabase() == null) throw new NullPointerException("database of config module must not be null!");
        if (cfMod.getJdo() == null) throw new NullPointerException("jdo of config module must not be null!");
        mcf.testConfiguration(cfMod);
        JFireServerConfigModule orgCfMod = mcf.getConfigModule();
        orgCfMod.acquireWriteLock();
        try {
            if (orgCfMod.getLocalServer() != null) {
                if (cfMod.getLocalServer().getServerID() == null) cfMod.getLocalServer().setServerID(orgCfMod.getLocalServer().getServerID()); else if (!orgCfMod.getLocalServer().getServerID().equals(cfMod.getLocalServer().getServerID())) throw new IllegalArgumentException("Cannot change serverID after it has been set once!");
            } else if (cfMod.getLocalServer().getServerID() == null) throw new NullPointerException("localServer.serverID must not be null at first call to this method!");
            try {
                BeanUtils.copyProperties(orgCfMod, cfMod);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } finally {
            orgCfMod.releaseLock();
        }
        getConfig().save(true);
        try {
            InitialContext initialContext = new InitialContext();
            try {
                String newRootOrganisationID = cfMod.getRootOrganisation().getOrganisationID();
                String oldRootOrganisationID = Organisation.getRootOrganisationID(initialContext);
                if (!newRootOrganisationID.equals(oldRootOrganisationID)) {
                    initialContext.rebind(Organisation.ROOT_ORGANISATION_ID_JNDI_NAME, newRootOrganisationID);
                }
            } finally {
                initialContext.close();
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    protected J2EEAdapter j2eeVendorAdapter = null;

    public synchronized J2EEAdapter getJ2EEVendorAdapter() throws ModuleException {
        if (j2eeVendorAdapter == null) {
            try {
                String j2eeVendorAdapterClassName = j2eeLocalServerCf.getJ2eeVendorAdapterClassName();
                Class<?> j2eeVendorAdapterClass = Class.forName(j2eeVendorAdapterClassName);
                j2eeVendorAdapter = (J2EEAdapter) j2eeVendorAdapterClass.newInstance();
            } catch (Exception e) {
                throw new ModuleException(e);
            }
        }
        return j2eeVendorAdapter;
    }

    protected synchronized void j2ee_flushAuthenticationCache() throws ModuleException {
        try {
            getJ2EEVendorAdapter().flushAuthenticationCache();
        } catch (ModuleException e) {
            throw e;
        } catch (Exception e) {
            throw new ModuleException(e);
        }
    }

    protected RoleImportSet roleImport_prepare(String organisationID) {
        File startDir = new File(mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory());
        JFireSecurityMan globalSecurityMan = new JFireSecurityMan();
        Map<String, Throwable> exceptions = new HashMap<String, Throwable>();
        roleImport_prepare_collect(startDir, globalSecurityMan, exceptions);
        return new RoleImportSet(organisationID, globalSecurityMan, exceptions);
    }

    private static class FileFilterDirectories implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            File f = new File(dir, name);
            return f.isDirectory();
        }
    }

    private static FileFilterDirectories fileFilterDirectories = null;

    private static class FileFilterJARs implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(JAR_SUFFIX);
        }
    }

    private static String JAR_SUFFIX = ".jar";

    private static FileFilterJARs fileFilterJARs = null;

    private void roleImport_prepare_collect(File directory, JFireSecurityMan globalEJBRoleGroupMan, Map<String, Throwable> exceptions) {
        if (fileFilterDirectories == null) fileFilterDirectories = new FileFilterDirectories();
        String[] directories = directory.list(fileFilterDirectories);
        if (directories != null) {
            for (int i = 0; i < directories.length; ++i) roleImport_prepare_collect(new File(directory, directories[i]), globalEJBRoleGroupMan, exceptions);
        }
        if (fileFilterJARs == null) fileFilterJARs = new FileFilterJARs();
        String[] jars = directory.list(fileFilterJARs);
        if (jars != null) {
            for (int i = 0; i < jars.length; ++i) {
                File jar = new File(directory, jars[i]);
                try {
                    JarFile jf = new JarFile(jar, true);
                    try {
                        roleImport_prepare_readJar(globalEJBRoleGroupMan, jar, jf);
                    } finally {
                        jf.close();
                    }
                } catch (Exception x) {
                    String jarFileName;
                    try {
                        jarFileName = jar.getCanonicalPath();
                        logger.warn("Processing Jar \"" + jarFileName + "\" failed!", x);
                    } catch (IOException e) {
                        jarFileName = jar.getPath();
                        logger.warn("Processing Jar \"" + jarFileName + "\" failed!", x);
                        logger.warn("Getting canonical path for \"" + jarFileName + "\" failed!", e);
                    }
                    exceptions.put(jarFileName, x);
                }
            }
        }
    }

    private void roleImport_prepare_readJar(JFireSecurityMan globalSecurityMan, File jar, JarFile jf) throws SAXException, IOException, XMLReadException {
        JarEntry ejbJarXML = jf.getJarEntry("META-INF/ejb-jar.xml");
        EJBJarMan ejbJarMan;
        if (ejbJarXML == null) {
            logger.info("Jar \"" + jar.getCanonicalPath() + "\" does not contain \"META-INF/ejb-jar.xml\"!");
            ejbJarMan = new EJBJarMan(jar.getName());
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("*****************************************************************");
                logger.debug("Jar \"" + jar.getCanonicalPath() + "\": ejb-jar.xml:");
            }
            InputStream in = jf.getInputStream(ejbJarXML);
            try {
                ejbJarMan = new EJBJarMan(jar.getName(), in);
                if (logger.isDebugEnabled()) {
                    for (Iterator<RoleDef> it = ejbJarMan.getRoles().iterator(); it.hasNext(); ) {
                        RoleDef roleDef = it.next();
                        logger.debug("roleDef.roleID = " + roleDef.getRoleID());
                    }
                }
            } finally {
                in.close();
            }
            if (logger.isDebugEnabled()) logger.debug("*****************************************************************");
        }
        JarEntry roleGroupXML = jf.getJarEntry("META-INF/jfire-security.xml");
        JFireSecurityMan securityMan;
        if (roleGroupXML == null) {
            logger.info("Jar \"" + jar.getCanonicalPath() + "\" does not contain \"META-INF/jfire-security.xml\"!");
            securityMan = new JFireSecurityMan(ejbJarMan);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("*****************************************************************");
                logger.debug("Jar \"" + jar.getCanonicalPath() + "\": jfire-security.xml:");
            }
            InputStream in = jf.getInputStream(roleGroupXML);
            try {
                securityMan = new JFireSecurityMan(ejbJarMan, in);
                if (logger.isDebugEnabled()) {
                    for (RoleGroupDef roleGroupDef : securityMan.getRoleGroups().values()) {
                        logger.debug("roleGroupDef.roleGroupID = " + roleGroupDef.getRoleGroupID());
                        for (String includedRoleGroupID : roleGroupDef.getIncludedRoleGroupIDs()) {
                            logger.debug("  includedRoleGroupID = " + includedRoleGroupID);
                        }
                        for (String roleID : roleGroupDef.getRoleIDs()) {
                            logger.debug("  roleID = " + roleID);
                        }
                    }
                }
            } finally {
                in.close();
            }
            if (logger.isDebugEnabled()) logger.debug("*****************************************************************");
        }
        securityMan.createFallbackRoleGroups();
        globalSecurityMan.mergeSecurityMan(securityMan);
    }

    /**
	 * @param roleImportSet
	 * @param pm can be <tt>null</tt>. If <tt>null</tt>, it will be obtained according to <tt>roleImportSet.getOrganisationID()</tt>.
	 * @throws ModuleException
	 */
    protected void roleImport_commit(RoleImportSet roleImportSet, PersistenceManager pm) {
        if (roleImportSet.getOrganisationID() == null) throw new IllegalArgumentException("roleImportSet.organisationID is null! Use roleImport_prepare(...) to generate a roleImportSet!");
        JFireSecurityMan securityMan = roleImportSet.getSecurityMan();
        securityMan.resolve();
        if (!roleImportSet.getJarExceptions().isEmpty()) logger.warn("roleImportSet.jarExceptions is not empty! You should execute roleImportSet.clearJarExceptions()!", new ModuleException("roleImportSet.jarExceptions is not empty."));
        boolean localPM = pm == null;
        if (localPM) pm = getPersistenceManager(roleImportSet.getOrganisationID());
        try {
            boolean successful = false;
            SecurityChangeController.beginChanging();
            try {
                String datastoreOrgaID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();
                if (!datastoreOrgaID.equals(roleImportSet.getOrganisationID())) throw new IllegalArgumentException("Parameter pm does not match organisationID of given roleImportSet!");
                Set<String> newRoleGroupIDs = securityMan.getRoleGroups().keySet();
                Set<String> oldRoleGroupIDs = new HashSet<String>();
                for (Iterator<RoleGroup> it = pm.getExtent(RoleGroup.class).iterator(); it.hasNext(); ) {
                    RoleGroup roleGroup = it.next();
                    oldRoleGroupIDs.add(roleGroup.getRoleGroupID());
                    if (!newRoleGroupIDs.contains(roleGroup.getRoleGroupID())) {
                        for (UndeployedRoleGroupAuthorityUserRecord r : UndeployedRoleGroupAuthorityUserRecord.getUndeployedRoleGroupAuthorityUserRecordForRoleGroup(pm, roleGroup)) {
                            pm.deletePersistent(r);
                        }
                        pm.flush();
                        UndeployedRoleGroupAuthorityUserRecord.createRecordsForRoleGroup(pm, roleGroup);
                    }
                }
                AuthorityType authorityType_organisation = (AuthorityType) pm.getObjectById(AuthorityType.AUTHORITY_TYPE_ID_ORGANISATION);
                for (AuthorityTypeDef authorityTypeDef : securityMan.getAuthorityTypes().values()) {
                    authorityTypeDef.updateAuthorityType(pm, !authorityType_organisation.getAuthorityTypeID().equals(authorityTypeDef.getAuthorityTypeID()));
                }
                for (RoleGroupDef roleGroupDef : securityMan.getRoleGroups().values()) {
                    RoleGroup roleGroupJDO = roleGroupDef.updateRoleGroup(pm);
                    authorityType_organisation.addRoleGroup(roleGroupJDO);
                }
                {
                    Set<String> currentRoleIDs = securityMan.getRoles().keySet();
                    Query q = pm.newQuery(Role.class);
                    pm.flush();
                    for (Object o : new HashSet<Object>((Collection<?>) q.execute())) {
                        Role role = (Role) o;
                        if (currentRoleIDs.contains(role.getRoleID())) continue;
                        for (RoleGroup roleGroup : new HashSet<RoleGroup>(role.getRoleGroups())) roleGroup.removeRole(role);
                        pm.deletePersistent(role);
                        pm.flush();
                    }
                }
                {
                    Collection<RoleGroup> roleGroups = CollectionUtil.castCollection((Collection<?>) pm.newQuery(RoleGroup.class).execute());
                    roleGroups = new HashSet<RoleGroup>(roleGroups);
                    for (Iterator<RoleGroup> it = roleGroups.iterator(); it.hasNext(); ) {
                        RoleGroup roleGroup = it.next();
                        if (!newRoleGroupIDs.contains(roleGroup.getRoleGroupID())) {
                            pm.deletePersistent(roleGroup);
                            pm.flush();
                        }
                        if (!oldRoleGroupIDs.contains(roleGroup.getRoleGroupID())) {
                            for (UndeployedRoleGroupAuthorityUserRecord r : UndeployedRoleGroupAuthorityUserRecord.getUndeployedRoleGroupAuthorityUserRecordForRoleGroup(pm, roleGroup)) {
                                if (r.getAuthority() != null && r.getAuthorizedObject() != null) {
                                    RoleGroupRef roleGroupRef = r.getAuthority().createRoleGroupRef(roleGroup);
                                    AuthorizedObjectRef authorizedObjectRef = r.getAuthority().createAuthorizedObjectRef(r.getAuthorizedObject());
                                    authorizedObjectRef.addRoleGroupRef(roleGroupRef);
                                }
                                pm.deletePersistent(r);
                                pm.flush();
                            }
                        }
                    }
                }
                successful = true;
            } finally {
                SecurityChangeController.endChanging(successful);
            }
        } finally {
            if (localPM) pm.close();
        }
    }

    private transient Object createOrganisation_mutex = new Object();

    /**
	 * This method generates a database-name out of the organisationID. Therefore,
	 * it replaces all characters which are not allowed in a database name by '_'.
	 * <p>
	 * <b>Warning:</b> This method allows name clashes, because e.g. both "a.b" and "a-b"
	 * are translated to "a_b".
	 * </p>
	 *
	 * @param organisationID The organisationID to be translated.
	 * @return the database name resulting from the given <code>organisationID</code>.
	 */
    protected String createDatabaseName(String organisationID) {
        StringBuffer databaseName = new StringBuffer((int) (1.5 * organisationID.length()));
        databaseName.append(organisationID.replaceAll("[^A-Za-z0-9_]", "_"));
        DatabaseCf dbCf = mcf.getConfigModule().getDatabase();
        databaseName.insert(0, dbCf.getDatabasePrefix());
        databaseName.append(dbCf.getDatabaseSuffix());
        return databaseName.toString();
    }

    private Map<CreateOrganisationProgressID, CreateOrganisationProgress> createOrganisationProgressMap = Collections.synchronizedMap(new HashMap<CreateOrganisationProgressID, CreateOrganisationProgress>());

    private CreateOrganisationProgressID createOrganisationProgressID = null;

    private transient Object createOrganisationProgressID_mutex = new Object();

    protected CreateOrganisationProgressID createOrganisationAsync(final String organisationID, final String organisationName, final String userID, final String password, final boolean isServerAdmin) throws BusyCreatingOrganisationException {
        synchronized (createOrganisationProgressID_mutex) {
            if (createOrganisationProgressID != null) {
                String busyOrganisationID = createOrganisationProgressMap.get(createOrganisationProgressID).getOrganisationID();
                throw new BusyCreatingOrganisationException(organisationID, CollectionUtil.array2HashSet(new String[] { busyOrganisationID }));
            }
            final CreateOrganisationProgress createOrganisationProgress = new CreateOrganisationProgress(organisationID);
            createOrganisationProgressMap.put(createOrganisationProgress.getCreateOrganisationProgressID(), createOrganisationProgress);
            createOrganisationProgressID = createOrganisationProgress.getCreateOrganisationProgressID();
            Thread thread = new Thread() {

                @Override
                public void run() {
                    try {
                        createOrganisation(createOrganisationProgress, organisationID, organisationName, userID, password, isServerAdmin);
                    } catch (Throwable e) {
                        logger.error("createOrganisationAsync.Thread.run: creating organisation \"" + organisationID + "\" failed!", e);
                    }
                }
            };
            thread.start();
            return createOrganisationProgress.getCreateOrganisationProgressID();
        }
    }

    protected CreateOrganisationProgress getCreateOrganisationProgress(CreateOrganisationProgressID createOrganisationProgressID) {
        return createOrganisationProgressMap.get(createOrganisationProgressID);
    }

    protected void createOrganisationProgress_addCreateOrganisationStatus(CreateOrganisationProgressID createOrganisationProgressID, CreateOrganisationStatus createOrganisationStatus) {
        CreateOrganisationProgress createOrganisationProgress = createOrganisationProgressMap.get(createOrganisationProgressID);
        if (createOrganisationProgress == null) throw new IllegalArgumentException("No CreateOrganisationProgress known with this id: " + createOrganisationProgressID);
        createOrganisationProgress.addCreateOrganisationStatus(createOrganisationStatus);
    }

    /**
	 * This method creates a new organisation. What exactly happens, is documented in our wiki:
	 * https://www.jfire.org/modules/phpwiki/index.php/NewOrganisationCreation
	 * @param createOrganisationProgress an instance of {@link CreateOrganisationProgress} in order to track the status.
	 * @param organisationID The ID of the new organsitation, which must not be <code>null</code>. Example: "RioDeJaneiro.NightLabs.org"
	 * @param organisationName The "human" name of the organisation. Example: "NightLabs GmbH, Rio de Janeiro"
	 * @param userID The userID of the first user to be created. This will be the new organisation's administrator.
	 * @param password The password of the organisation's first user.
	 * @param isServerAdmin Whether the organisation's admin will have server-administrator privileges. This must be <tt>true</tt> if you create the first organisation on a server.
	 */
    protected void createOrganisation(CreateOrganisationProgress createOrganisationProgress, String organisationID, String organisationName, String userID, String password, boolean isServerAdmin) throws ModuleException {
        if (!createOrganisationAllowed) throw new IllegalStateException("This method cannot be called yet. The creation of organisations is not allowed, before the datastore inits are run. If you get this exception in an early-server-init, you should switch to a late-server-init.");
        if (createOrganisationProgress == null) throw new IllegalArgumentException("createOrganisationProgress must not be null!");
        if (organisationID == null) throw new IllegalArgumentException("organisationID must not be null!");
        if ("".equals(organisationID)) throw new IllegalArgumentException("organisationID must not be an empty string!");
        if (organisationID.indexOf('.') < 0) throw new IllegalArgumentException("organisationID is invalid! Must have domain-style form (e.g. \"jfire.nightlabs.de\")!");
        if (!Organisation.isValidOrganisationID(organisationID)) throw new IllegalArgumentException("organisationID is not valid! Make sure it does not contain special characters. It should have a domain-style form!");
        if (organisationID.length() > 50) throw new IllegalArgumentException("organisationID has " + organisationID.length() + " chars and is too long! Maximum is 50 characters.");
        if (organisationName == null) throw new IllegalArgumentException("organisationName must not be null!");
        if ("".equals(organisationName)) throw new IllegalArgumentException("organisationName must not be an empty string!");
        if (!organisationID.equals(createOrganisationProgress.getOrganisationID())) throw new IllegalArgumentException("organisationID does not match createOrganisationProgress.getOrganisationID()!");
        OrganisationInitManager datastoreInitManager;
        try {
            datastoreInitManager = new OrganisationInitManager(this, mcf, getJ2EEVendorAdapter());
        } catch (OrganisationInitException e) {
            logger.error("Creation of OrganisationInitManager failed!", e);
            throw new ModuleException(e);
        }
        int stepsBeforeDatastoreInit = 10;
        int stepsDuringDatastoreInit = 2 * datastoreInitManager.getInits().size();
        createOrganisationProgress.setStepsTotal(stepsBeforeDatastoreInit + stepsDuringDatastoreInit);
        try {
            synchronized (createOrganisation_mutex) {
                synchronized (createOrganisationProgressID_mutex) {
                    if (createOrganisationProgressID != null && !createOrganisationProgressID.equals(createOrganisationProgress.getCreateOrganisationProgressID())) {
                        String busyOrganisationID = createOrganisationProgressMap.get(createOrganisationProgressID).getOrganisationID();
                        BusyCreatingOrganisationException x = new BusyCreatingOrganisationException(organisationID, CollectionUtil.array2HashSet(new String[] { busyOrganisationID }));
                        logger.error("THIS SHOULD NEVER HAPPEN!", x);
                        throw x;
                    }
                    createOrganisationProgressMap.put(createOrganisationProgress.getCreateOrganisationProgressID(), createOrganisationProgress);
                    createOrganisationProgressID = createOrganisationProgress.getCreateOrganisationProgressID();
                }
                try {
                    if (userID == null) throw new IllegalArgumentException("userID must not be null!");
                    if ("".equals(userID)) throw new IllegalArgumentException("userID must not be an empty string!");
                    if (!ObjectIDUtil.isValidIDString(userID)) throw new IllegalArgumentException("userID is not a valid ID! Make sure it does not contain special characters!");
                    if (userID.length() > 50) throw new IllegalArgumentException("userID has " + userID.length() + " chars and is too long! Maximum is 50 characters.");
                    if (password == null) throw new IllegalArgumentException("password must NOT be null!");
                    if (password.length() < 4) throw new IllegalArgumentException("password is too short! At least 4 characters are required! At least 8 characters are recommended!");
                    if (!UserLocal.isValidPassword(password)) throw new IllegalArgumentException("password is not valid!");
                    if (isNewServerNeedingSetup()) throw new IllegalStateException("This server is not yet set up! Please complete the basic setup before creating organisations!");
                    if (!isServerAdmin && organisationConfigModule.getOrganisations().isEmpty()) throw new NoServerAdminException("You create the first organisation, hence 'isServerAdmin' must be true! " + "Otherwise, you would end up locked out, without any possibility to " + "create another organisation or change the server-configuration.");
                    Map<String, OrganisationCf> organisationCfsCloned = getOrganisationCfsCloned();
                    if (organisationCfsCloned.get(organisationID) != null) throw new DuplicateOrganisationException("An organisation with the name \"" + organisationID + "\" already exists on this server!");
                    InitialContext initialContext = new InitialContext();
                    try {
                        if (Organisation.hasRootOrganisation(initialContext)) {
                        }
                        File jdoConfigDir = null;
                        DatabaseAdapter databaseAdapter = null;
                        boolean dropDatabase = false;
                        OrganisationCf organisationCf = null;
                        boolean doCommit = false;
                        try {
                            DatabaseCf dbCf = mcf.getConfigModule().getDatabase();
                            JDOCf jdoCf = mcf.getConfigModule().getJdo();
                            try {
                                Class.forName(dbCf.getDatabaseDriverName_noTx());
                            } catch (ClassNotFoundException e) {
                                throw new ConfigException("Database driver class (no-tx) \"" + dbCf.getDatabaseDriverName_noTx() + "\" could not be found!", e);
                            }
                            try {
                                Class.forName(dbCf.getDatabaseDriverName_localTx());
                            } catch (ClassNotFoundException e) {
                                throw new ConfigException("Database driver class (local-tx) \"" + dbCf.getDatabaseDriverName_localTx() + "\" could not be found!", e);
                            }
                            try {
                                Class.forName(dbCf.getDatabaseDriverName_xa());
                            } catch (ClassNotFoundException e) {
                                throw new ConfigException("Database driver class (xa) \"" + dbCf.getDatabaseDriverName_xa() + "\" could not be found!", e);
                            }
                            String databaseName = createDatabaseName(organisationID);
                            String dbURL = dbCf.getDatabaseURL(databaseName);
                            createOrganisationProgress.addCreateOrganisationStatus(new CreateOrganisationStatus(CreateOrganisationStep.JFireServerManagerFactory_createOrganisation_createDatabase_begin, databaseName, dbURL));
                            databaseAdapter = dbCf.instantiateDatabaseAdapter();
                            try {
                                databaseAdapter.createDatabase(mcf.getConfigModule(), dbURL);
                                dropDatabase = true;
                            } catch (Exception x) {
                                throw new ModuleException("Creating database with DatabaseAdapter \"" + databaseAdapter.getClass().getName() + "\" failed!", x);
                            }
                            createOrganisationProgress.addCreateOrganisationStatus(new CreateOrganisationStatus(CreateOrganisationStep.JFireServerManagerFactory_createOrganisation_createDatabase_end, databaseName, dbURL));
                            createOrganisationProgress.addCreateOrganisationStatus(new CreateOrganisationStatus(CreateOrganisationStep.JFireServerManagerFactory_createOrganisation_deployJDO_begin, databaseName, dbURL));
                            jdoConfigDir = new File(jdoCf.getJdoConfigDirectory(organisationID)).getAbsoluteFile();
                            File datasourceDSXML = new File(jdoConfigDir, dbCf.getDatasourceConfigFile(organisationID));
                            File jdoDSXML = new File(jdoConfigDir, jdoCf.getJdoDeploymentDescriptorFile(organisationID));
                            String persistenceConfig = jdoCf.getJdoPersistenceConfigurationFile(organisationID);
                            File jdoPersistenceConfigurationFile = "".equals(persistenceConfig) ? null : new File(jdoConfigDir, persistenceConfig);
                            String persistenceConfigTemplate = jdoCf.getJdoPersistenceConfigurationTemplateFile();
                            File jdoPersistenceConfigurationTemplateFile = "".equals(persistenceConfigTemplate) ? null : new File(persistenceConfigTemplate);
                            if (jdoPersistenceConfigurationFile != null && jdoPersistenceConfigurationTemplateFile != null) {
                                createDeploymentDescriptor(organisationID, jdoPersistenceConfigurationFile, jdoPersistenceConfigurationTemplateFile, null, DeployOverwriteBehaviour.EXCEPTION);
                            }
                            createDeploymentDescriptor(organisationID, datasourceDSXML, new File(dbCf.getDatasourceTemplateDSXMLFile()), null, DeployOverwriteBehaviour.EXCEPTION);
                            createDeploymentDescriptor(organisationID, jdoDSXML, new File(jdoCf.getJdoDeploymentDescriptorTemplateFile()), null, DeployOverwriteBehaviour.EXCEPTION);
                            organisationCf = organisationConfigModule.addOrganisation(organisationID, organisationName);
                            if (userID != null && isServerAdmin) organisationCf.addServerAdmin(userID);
                            resetOrganisationCfs();
                            try {
                                getConfig().save(true);
                            } catch (ConfigException e) {
                                logger.fatal("Saving config failed!", e);
                            }
                            logger.info("Empty organisation \"" + organisationID + "\" (\"" + organisationName + "\") has been created. Waiting for deployment...");
                            {
                                PersistenceManagerFactory pmf = null;
                                int tryCount = createOrganisationConfigModule.getWaitForPersistenceManager_tryCount();
                                int tryNr = 0;
                                while (pmf == null) {
                                    ++tryNr;
                                    try {
                                        pmf = waitForPersistenceManagerFactory(OrganisationCf.PERSISTENCE_MANAGER_FACTORY_PREFIX_ABSOLUTE + organisationID);
                                    } catch (ModuleException x) {
                                        if (tryNr >= tryCount) throw x;
                                        logger.info("Obtaining PersistenceManagerFactory failed! Touching jdo-ds-file and its directory and trying it again...");
                                        long now = System.currentTimeMillis();
                                        datasourceDSXML.setLastModified(now);
                                        jdoDSXML.setLastModified(now);
                                        jdoConfigDir.setLastModified(now);
                                    }
                                }
                                logger.info("PersistenceManagerFactory of organisation \"" + organisationID + "\" (\"" + organisationName + "\") has been deployed.");
                            }
                            createOrganisationProgress.addCreateOrganisationStatus(new CreateOrganisationStatus(CreateOrganisationStep.JFireServerManagerFactory_createOrganisation_deployJDO_end, databaseName, dbURL));
                            int tryCount = 0;
                            boolean successful = false;
                            while (!successful) {
                                try {
                                    ServerCf localServerCf = mcf.getConfigModule().getLocalServer();
                                    Properties props = InvokeUtil.getInitialContextProperties(this, localServerCf, organisationID, User.USER_ID_SYSTEM, jfireSecurity_createTempUserPassword(organisationID, User.USER_ID_SYSTEM));
                                    InitialContext initCtx = new InitialContext(props);
                                    try {
                                        Object bean = InvokeUtil.createBean(initCtx, "jfire/ejb/JFireBaseBean/OrganisationManager");
                                        Method beanMethod = bean.getClass().getMethod("internalInitializeEmptyOrganisation", new Class[] { CreateOrganisationProgressID.class, ServerCf.class, OrganisationCf.class, String.class, String.class });
                                        beanMethod.invoke(bean, new Object[] { createOrganisationProgress.getCreateOrganisationProgressID(), localServerCf, organisationCf, userID, password });
                                        InvokeUtil.removeBean(bean);
                                    } finally {
                                        initCtx.close();
                                    }
                                    successful = true;
                                } catch (Exception x) {
                                    if (++tryCount > 2) throw x; else logger.warn("Calling OrganisationManager.internalInitializeEmptyOrganisation(...) failed! Will retry again.", x);
                                }
                            }
                            jfireSecurity_flushCache();
                            j2ee_flushAuthenticationCache();
                            try {
                                CacheManagerFactory cmf = new CacheManagerFactory(this, initialContext, organisationCf, cacheCfMod, new File(mcf.getSysConfigDirectory()));
                                PersistenceManagerFactory pmf = getPersistenceManagerFactory(organisationID);
                                cmf.setupJdoCacheBridge(pmf);
                                String createString = System.getProperty(PersistentNotificationManagerFactory.class.getName() + ".create");
                                boolean create = !Boolean.FALSE.toString().equals(createString);
                                if (logger.isDebugEnabled()) logger.debug(PersistentNotificationManagerFactory.class.getName() + ".create=" + createString);
                                if (!create) logger.info(PersistentNotificationManagerFactory.class.getName() + ".create is false! Will not create PersistentNotificationManagerFactory for organisation \"" + organisationID + "\"!"); else {
                                    new PersistentNotificationManagerFactory(initialContext, organisationID, this, getJ2EEVendorAdapter().getUserTransaction(initialContext), pmf);
                                }
                            } catch (Exception e) {
                                logger.error("Creating CacheManagerFactory or PersistentNotificationManagerFactory for organisation \"" + organisationID + "\" failed!", e);
                                throw new ResourceException(e.getMessage());
                            }
                            doCommit = true;
                        } finally {
                            if (doCommit) {
                            } else {
                                try {
                                    if (dropDatabase && databaseAdapter != null) databaseAdapter.dropDatabase();
                                } catch (Throwable t) {
                                    logger.error("Dropping database failed!", t);
                                }
                                try {
                                    if (jdoConfigDir != null) {
                                        if (!IOUtil.deleteDirectoryRecursively(jdoConfigDir)) logger.error("Deleting JDO config directory \"" + jdoConfigDir.getAbsolutePath() + "\" failed!");
                                        ;
                                    }
                                } catch (Throwable t) {
                                    logger.error("Deleting JDO config directory \"" + jdoConfigDir.getAbsolutePath() + "\" failed!", t);
                                }
                                if (organisationCf != null) {
                                    try {
                                        if (!organisationConfigModule.removeOrganisation(organisationCf.getOrganisationID())) throw new IllegalStateException("Organisation was not registered in ConfigModule!");
                                        resetOrganisationCfs();
                                        organisationConfigModule._getConfig().save();
                                    } catch (Throwable t) {
                                        logger.error("Removing organisation \"" + organisationCf.getOrganisationID() + "\" from JFire server configuration failed!", t);
                                    }
                                }
                            }
                            databaseAdapter.close();
                            databaseAdapter = null;
                        }
                    } finally {
                        initialContext.close();
                        initialContext = null;
                    }
                } catch (RuntimeException x) {
                    createOrganisationProgress.addCreateOrganisationStatus(new CreateOrganisationStatus(CreateOrganisationStep.JFireServerManagerFactory_createOrganisation_error, x));
                    throw x;
                } catch (ModuleException x) {
                    createOrganisationProgress.addCreateOrganisationStatus(new CreateOrganisationStatus(CreateOrganisationStep.JFireServerManagerFactory_createOrganisation_error, x));
                    throw x;
                } catch (Exception x) {
                    createOrganisationProgress.addCreateOrganisationStatus(new CreateOrganisationStatus(CreateOrganisationStep.JFireServerManagerFactory_createOrganisation_error, x));
                    throw new ModuleException(x);
                }
            }
            try {
                datastoreInitManager.initialiseOrganisation(this, mcf.getConfigModule().getLocalServer(), organisationID, jfireSecurity_createTempUserPassword(organisationID, User.USER_ID_SYSTEM), createOrganisationProgress);
            } catch (ModuleException e) {
                logger.error("Datastore initialization for new organisation \"" + organisationID + "\" failed!", e);
            }
        } finally {
            synchronized (createOrganisationProgressID_mutex) {
                if (Util.equals(createOrganisationProgressID, createOrganisationProgress.getCreateOrganisationProgressID())) createOrganisationProgressID = null;
                createOrganisationProgress.done();
            }
        }
    }

    /**
	 * @throws OrganisationNotFoundException If the organisation does not exist.
	 */
    protected OrganisationCf getOrganisationConfig(String organisationID) throws OrganisationNotFoundException {
        OrganisationCf org = getOrganisationCfsCloned().get(organisationID);
        if (org == null) throw new OrganisationNotFoundException("No organisation with [master]organisationID=\"" + organisationID + "\" existent!");
        return org;
    }

    protected void addServerAdmin(String organisationID, String userID) throws ModuleException {
        OrganisationCf org = null;
        for (Iterator<OrganisationCf> it = organisationConfigModule.getOrganisations().iterator(); it.hasNext(); ) {
            OrganisationCf o = it.next();
            if (organisationID.equals(o.getOrganisationID())) {
                org = o;
                break;
            }
        }
        if (org == null) throw new OrganisationNotFoundException("No organisation with [master]organisationID=\"" + organisationID + "\" existent!");
        org.addServerAdmin(userID);
        resetOrganisationCfs();
    }

    protected boolean removeServerAdmin(String organisationID, String userID) throws ModuleException {
        OrganisationCf org = null;
        for (Iterator<OrganisationCf> it = organisationConfigModule.getOrganisations().iterator(); it.hasNext(); ) {
            OrganisationCf o = it.next();
            if (organisationID.equals(o.getOrganisationID())) {
                org = o;
                break;
            }
        }
        if (org == null) throw new OrganisationNotFoundException("No organisation with [master]organisationID=\"" + organisationID + "\" existent!");
        boolean res = org.removeServerAdmin(userID);
        resetOrganisationCfs();
        return res;
    }

    protected Config getConfig() {
        return mcf.getConfig();
    }

    protected boolean isOrganisationCfsEmpty() {
        return organisationConfigModule.getOrganisations().isEmpty();
    }

    protected synchronized List<OrganisationCf> getOrganisationCfs(boolean sorted) {
        ArrayList<OrganisationCf> l = new ArrayList<OrganisationCf>(getOrganisationCfsCloned().values());
        if (sorted) Collections.sort(l);
        return l;
    }

    public synchronized void flushModuleCache() {
        cachedModules = null;
    }

    /**
	 * key: ModuleType moduleType<br/>
	 * value: List modules
	 */
    protected Map<ModuleType, List<ModuleDef>> cachedModules = null;

    public synchronized List<ModuleDef> getModules(ModuleType moduleType) throws ModuleException {
        try {
            if (cachedModules == null) cachedModules = new HashMap<ModuleType, List<ModuleDef>>();
            List<ModuleDef> modules = cachedModules.get(moduleType);
            if (modules == null) {
                File startDir = new File(mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory());
                modules = new ArrayList<ModuleDef>();
                findModules(startDir, moduleType, modules);
                Collections.sort(modules);
                cachedModules.put(moduleType, modules);
            }
            return modules;
        } catch (Exception x) {
            if (x instanceof ModuleException) throw (ModuleException) x;
            throw new ModuleException(x);
        }
    }

    private static class FileFilterDirectoriesExcludingEARs implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            if (name.endsWith(".ear")) return false;
            File f = new File(dir, name);
            return f.isDirectory();
        }
    }

    private static FileFilterDirectoriesExcludingEARs fileFilterDirectoriesExcludingEARs = null;

    public static class FileFilterEARs implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".ear");
        }
    }

    private static FileFilterEARs fileFilterEARs = null;

    private void findModules(File directory, ModuleType moduleType, List<ModuleDef> modules) throws XMLReadException {
        if (fileFilterDirectoriesExcludingEARs == null) fileFilterDirectoriesExcludingEARs = new FileFilterDirectoriesExcludingEARs();
        String[] directories = directory.list(fileFilterDirectoriesExcludingEARs);
        if (directories != null) {
            for (int i = 0; i < directories.length; ++i) findModules(new File(directory, directories[i]), moduleType, modules);
        }
        if (fileFilterEARs == null) fileFilterEARs = new FileFilterEARs();
        String[] ears = directory.list(fileFilterEARs);
        if (ears != null) {
            for (int i = 0; i < ears.length; ++i) {
                File ear = new File(directory, ears[i]);
                findModulesInEAR(ear, moduleType, modules);
            }
        }
    }

    private void findModulesInEAR(File ear, ModuleType moduleType, List<ModuleDef> modules) throws XMLReadException {
        if (!ear.isDirectory()) {
            logger.warn("Deployed EAR \"" + ear.getAbsolutePath() + "\" is ignored, because only EAR directories are supported!");
            return;
        }
        EARApplicationMan earAppMan = new EARApplicationMan(ear, moduleType);
        for (Iterator<ModuleDef> it = earAppMan.getModules().iterator(); it.hasNext(); ) {
            ModuleDef md = it.next();
            modules.add(md);
        }
    }

    /**
	 * This map holds clones of the real OrganisationCf instances within
	 * the ConfigModule.
	 * <br/><br/>
	 * key: String organisationID / String masterOrganisationID<br/>
	 * value: OrganisationCf org
	 */
    private Map<String, OrganisationCf> organisationCfsCloned = null;

    protected synchronized void resetOrganisationCfs() {
        organisationCfsCloned = null;
    }

    @Override
    public boolean containsOrganisation(String organisationID) {
        return getOrganisationCfsCloned().containsKey(organisationID);
    }

    protected synchronized Map<String, OrganisationCf> getOrganisationCfsCloned() {
        if (organisationCfsCloned == null) {
            Map<String, OrganisationCf> organisationCfsCloned = new HashMap<String, OrganisationCf>();
            organisationConfigModule.acquireReadLock();
            try {
                for (Iterator<OrganisationCf> it = organisationConfigModule.getOrganisations().iterator(); it.hasNext(); ) {
                    OrganisationCf org = (OrganisationCf) it.next().clone();
                    org.makeReadOnly();
                    organisationCfsCloned.put(org.getOrganisationID(), org);
                }
            } finally {
                organisationConfigModule.releaseLock();
            }
            this.organisationCfsCloned = Collections.unmodifiableMap(organisationCfsCloned);
        }
        return organisationCfsCloned;
    }

    public void undeploy(File deployment) throws IOException {
        if (deployment.isAbsolute()) logger.warn("deployment should not be an absolute file: " + deployment.getPath(), new IllegalArgumentException("deployment should not be an absolute file: " + deployment.getPath()));
        if (!deployment.isAbsolute()) {
            deployment = new File(new File(mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory()).getAbsoluteFile().getParentFile(), deployment.getPath());
        }
        if (!deployment.exists()) {
            logger.warn("deployment does not exist: " + deployment.getPath(), new IllegalArgumentException("deployment does not exist: " + deployment.getPath()));
            return;
        }
        if (!IOUtil.deleteDirectoryRecursively(deployment)) {
            if (deployment.exists()) throw new IOException("The deployment could not be undeployed: " + deployment.getPath()); else logger.warn("deleting deployment failed, but it does not exist anymore (which is fine): " + deployment.getPath(), new IOException("deleting deployment failed, but it does not exist anymore (which is fine): " + deployment.getPath()));
        }
    }

    public void createDeploymentJar(String organisationID, File deploymentJar, Collection<DeploymentJarItem> deploymentJarItems, DeployOverwriteBehaviour deployOverwriteBehaviour) throws IOException {
        if (deploymentJar.isAbsolute()) logger.warn("deploymentJar should not be an absolute file: " + deploymentJar.getPath(), new IllegalArgumentException("deploymentJar should not be an absolute file: " + deploymentJar.getPath()));
        if (!deploymentJar.isAbsolute()) {
            deploymentJar = new File(new File(mcf.getConfigModule().getJ2ee().getJ2eeDeployBaseDirectory()).getAbsoluteFile().getParentFile(), deploymentJar.getPath());
        }
        if (deploymentJar.exists()) {
            switch(deployOverwriteBehaviour) {
                case EXCEPTION:
                    throw new DeployedFileAlreadyExistsException(deploymentJar);
                case KEEP:
                    logger.warn("File " + deploymentJar + " already exists. Will not change anything!");
                    return;
                case OVERWRITE:
                    break;
                default:
                    throw new IllegalStateException("Unknown deployOverwriteBehaviour: " + deployOverwriteBehaviour);
            }
        }
        logger.info("Creating deploymentJar: \"" + deploymentJar.getAbsolutePath() + "\"");
        File tmpDir;
        do {
            tmpDir = new File(IOUtil.getTempDir(), "jfire_" + Base62Coder.sharedInstance().encode(System.currentTimeMillis(), 1) + '-' + Base62Coder.sharedInstance().encode((int) (Math.random() * Integer.MAX_VALUE), 1) + ".tmp");
        } while (tmpDir.exists());
        if (!tmpDir.mkdirs()) throw new IOException("Could not create temporary directory: " + tmpDir);
        try {
            File manifestFileRelative = new File("META-INF/MANIFEST.MF");
            boolean createManifest = true;
            for (DeploymentJarItem deploymentJarItem : deploymentJarItems) {
                if (manifestFileRelative.equals(deploymentJarItem.getDeploymentJarEntry())) createManifest = false;
                File deploymentDescriptorFile = new File(tmpDir, deploymentJarItem.getDeploymentJarEntry().getPath());
                createDeploymentDescriptor(organisationID, deploymentDescriptorFile, deploymentJarItem.getTemplateFile(), deploymentJarItem.getAdditionalVariables(), DeployOverwriteBehaviour.EXCEPTION);
            }
            if (createManifest) {
                File manifestFile = new File(tmpDir, manifestFileRelative.getPath());
                if (!manifestFile.getParentFile().mkdirs()) throw new IOException("Could not create META-INF directory: " + manifestFile.getParentFile());
                Manifest manifest = new Manifest();
                manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
                manifest.getMainAttributes().putValue("Created-By", "JFire - http://www.jfire.org");
                FileOutputStream out = new FileOutputStream(manifestFile);
                try {
                    manifest.write(out);
                } finally {
                    out.close();
                }
            }
            File deploymentDirectory = deploymentJar.getParentFile();
            if (!deploymentDirectory.exists()) {
                logger.info("deploymentDirectory does not exist. Creating it: " + deploymentDirectory.getAbsolutePath());
                if (!deploymentDirectory.mkdirs()) logger.error("Creating deploymentDirectory failed: " + deploymentDirectory.getAbsolutePath());
            }
            if (deploymentJar.exists()) {
                logger.warn("deploymentJar already exists. Replacing it: " + deploymentJar.getAbsolutePath());
                if (!deploymentJar.delete()) throw new IOException("Deleting deploymentJar failed: " + deploymentJar.getAbsolutePath());
            }
            IOUtil.zipFolder(deploymentJar, tmpDir);
        } finally {
            IOUtil.deleteDirectoryRecursively(tmpDir);
        }
    }

    /**
	 * @param organisationID The organisation for which a new deployment-descriptor is created.
	 * @param deploymentDescriptorFile The deployment-descriptor-file (relative recommended) that shall be created. The parent-directories are implicitely created.
	 *		If this is relative, it will be created inside the deploy-directory of the jee server (i.e. within a subdirectory, if it contains a path, and as sibling
	 *		to JFire.last).
	 * @param templateFile The template file.
	 * @param additionalVariables Additional variables that shall be available besides the default variables. They override default values, if they contain colliding keys.
	 * @param deployOverwriteBehaviour TODO
	 * @throws IOException If writing/reading fails.
	 */
    public void createDeploymentDescriptor(String organisationID, File deploymentDescriptorFile, File templateFile, Map<String, String> additionalVariables, DeployOverwriteBehaviour deployOverwriteBehaviour) throws IOException {
        JFireServerConfigModule cfMod = mcf.getConfigModule();
        DatabaseCf dbCf = cfMod.getDatabase();
        if (!deploymentDescriptorFile.isAbsolute()) {
            deploymentDescriptorFile = new File(new File(cfMod.getJ2ee().getJ2eeDeployBaseDirectory()).getAbsoluteFile().getParentFile(), deploymentDescriptorFile.getPath());
        }
        if (deploymentDescriptorFile.exists()) {
            switch(deployOverwriteBehaviour) {
                case EXCEPTION:
                    throw new DeployedFileAlreadyExistsException(deploymentDescriptorFile);
                case KEEP:
                    logger.warn("File " + deploymentDescriptorFile + " already exists. Will not change anything!");
                    return;
                case OVERWRITE:
                    logger.warn("File " + deploymentDescriptorFile + " already exists. Will overwrite this file!");
                    break;
                default:
                    throw new IllegalStateException("Unknown deployOverwriteBehaviour: " + deployOverwriteBehaviour);
            }
        }
        String databaseName = createDatabaseName(organisationID);
        String dbURL = dbCf.getDatabaseURL(databaseName);
        String datasourceJNDIName_relative = OrganisationCf.DATASOURCE_PREFIX_RELATIVE + organisationID;
        String datasourceJNDIName_absolute = OrganisationCf.DATASOURCE_PREFIX_ABSOLUTE + organisationID;
        String jdoPersistenceManagerFactoryJNDIName_relative = OrganisationCf.PERSISTENCE_MANAGER_FACTORY_PREFIX_RELATIVE + organisationID;
        String jdoPersistenceManagerFactoryJNDIName_absolute = OrganisationCf.PERSISTENCE_MANAGER_FACTORY_PREFIX_ABSOLUTE + organisationID;
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("organisationID", organisationID);
        variables.put("datasourceJNDIName_relative_noTx", datasourceJNDIName_relative + "/no-tx");
        variables.put("datasourceJNDIName_absolute_noTx", datasourceJNDIName_absolute + "/no-tx");
        variables.put("datasourceJNDIName_relative_localTx", datasourceJNDIName_relative + "/local-tx");
        variables.put("datasourceJNDIName_absolute_localTx", datasourceJNDIName_absolute + "/local-tx");
        variables.put("datasourceJNDIName_relative_xa", datasourceJNDIName_relative + "/xa");
        variables.put("datasourceJNDIName_absolute_xa", datasourceJNDIName_absolute + "/xa");
        variables.put("datasourceMetadataTypeMapping", dbCf.getDatasourceMetadataTypeMapping());
        variables.put("jdoPersistenceManagerFactoryJNDIName_relative", jdoPersistenceManagerFactoryJNDIName_relative);
        variables.put("jdoPersistenceManagerFactoryJNDIName_absolute", jdoPersistenceManagerFactoryJNDIName_absolute);
        variables.put("databaseDriverName_noTx", dbCf.getDatabaseDriverName_noTx());
        variables.put("databaseDriverName_localTx", dbCf.getDatabaseDriverName_localTx());
        variables.put("databaseDriverName_xa", dbCf.getDatabaseDriverName_xa());
        variables.put("databaseURL", dbURL);
        variables.put("databaseName", databaseName);
        variables.put("databaseUserName", dbCf.getDatabaseUserName());
        variables.put("databasePassword", dbCf.getDatabasePassword());
        variables.put("deploymentDescriptorDirectory", deploymentDescriptorFile.getParent());
        variables.put("deploymentDescriptorDirectory_absolute", deploymentDescriptorFile.getParent());
        variables.put("deploymentDescriptorDirectory_relative", IOUtil.getRelativePath(new File("."), deploymentDescriptorFile.getParent()));
        variables.put("deploymentDescriptorFileName", deploymentDescriptorFile.getName());
        if (additionalVariables != null) variables.putAll(additionalVariables);
        _createDeploymentDescriptor(deploymentDescriptorFile, templateFile, variables);
    }

    private static enum ParserExpects {

        NORMAL, BRACKET_OPEN, VARIABLE, BRACKET_CLOSE
    }

    /**
	 * Generate a -ds.xml file (or any other deployment descriptor) from a template.
	 *
	 * @param deploymentDescriptorFile The file (absolute!) that shall be created out of the template.
	 * @param templateFile The template file to use. Must not be <code>null</code>.
	 * @param variables This map defines what variable has to be replaced by what value. The
	 *				key is the variable name (without brackets "{", "}"!) and the value is the
	 *				value for the variable to replace. This must not be <code>null</code>.
	 */
    private void _createDeploymentDescriptor(File deploymentDescriptorFile, File templateFile, Map<String, String> variables) throws IOException {
        if (!deploymentDescriptorFile.isAbsolute()) throw new IllegalArgumentException("deploymentDescriptorFile is not absolute: " + deploymentDescriptorFile.getPath());
        logger.info("Creating deploymentDescriptor \"" + deploymentDescriptorFile.getAbsolutePath() + "\" from template \"" + templateFile.getAbsolutePath() + "\".");
        File deploymentDirectory = deploymentDescriptorFile.getParentFile();
        if (!deploymentDirectory.exists()) {
            logger.info("deploymentDirectory does not exist. Creating it: " + deploymentDirectory.getAbsolutePath());
            if (!deploymentDirectory.mkdirs()) logger.error("Creating deploymentDirectory failed: " + deploymentDirectory.getAbsolutePath());
        }
        FileReader fr = new FileReader(templateFile);
        try {
            StreamTokenizer stk = new StreamTokenizer(fr);
            stk.resetSyntax();
            stk.wordChars(0, Integer.MAX_VALUE);
            stk.ordinaryChar('$');
            stk.ordinaryChar('{');
            stk.ordinaryChar('}');
            stk.ordinaryChar('\n');
            FileWriter fw = new FileWriter(deploymentDescriptorFile);
            try {
                String variableName = null;
                StringBuffer tmpBuf = new StringBuffer();
                ParserExpects parserExpects = ParserExpects.NORMAL;
                while (stk.nextToken() != StreamTokenizer.TT_EOF) {
                    String stringToWrite = null;
                    if (stk.ttype == StreamTokenizer.TT_WORD) {
                        switch(parserExpects) {
                            case VARIABLE:
                                parserExpects = ParserExpects.BRACKET_CLOSE;
                                variableName = stk.sval;
                                tmpBuf.append(variableName);
                                break;
                            case NORMAL:
                                stringToWrite = stk.sval;
                                break;
                            default:
                                parserExpects = ParserExpects.NORMAL;
                                stringToWrite = tmpBuf.toString() + stk.sval;
                                tmpBuf.setLength(0);
                        }
                    } else if (stk.ttype == '\n') {
                        stringToWrite = new String(new char[] { (char) stk.ttype });
                        if (parserExpects != ParserExpects.NORMAL) {
                            parserExpects = ParserExpects.NORMAL;
                            stringToWrite = tmpBuf.toString() + stringToWrite;
                            tmpBuf.setLength(0);
                        }
                    } else if (stk.ttype == '$') {
                        if (parserExpects != ParserExpects.NORMAL) {
                            stringToWrite = tmpBuf.toString();
                            tmpBuf.setLength(0);
                        }
                        tmpBuf.append((char) stk.ttype);
                        parserExpects = ParserExpects.BRACKET_OPEN;
                    } else if (stk.ttype == '{') {
                        switch(parserExpects) {
                            case NORMAL:
                                stringToWrite = new String(new char[] { (char) stk.ttype });
                                break;
                            case BRACKET_OPEN:
                                tmpBuf.append((char) stk.ttype);
                                parserExpects = ParserExpects.VARIABLE;
                                break;
                            default:
                                parserExpects = ParserExpects.NORMAL;
                                stringToWrite = tmpBuf.toString() + (char) stk.ttype;
                                tmpBuf.setLength(0);
                        }
                    } else if (stk.ttype == '}') {
                        switch(parserExpects) {
                            case NORMAL:
                                stringToWrite = new String(new char[] { (char) stk.ttype });
                                break;
                            case BRACKET_CLOSE:
                                parserExpects = ParserExpects.NORMAL;
                                tmpBuf.append((char) stk.ttype);
                                if (variableName == null) throw new IllegalStateException("variableName is null!!!");
                                stringToWrite = variables.get(variableName);
                                if (stringToWrite == null) {
                                    logger.warn("Variable " + tmpBuf.toString() + " occuring in template \"" + templateFile + "\" is unknown!");
                                    stringToWrite = tmpBuf.toString();
                                }
                                tmpBuf.setLength(0);
                                break;
                            default:
                                parserExpects = ParserExpects.NORMAL;
                                stringToWrite = tmpBuf.toString() + (char) stk.ttype;
                                tmpBuf.setLength(0);
                        }
                    }
                    if (stringToWrite != null) fw.write(stringToWrite);
                }
            } finally {
                fw.close();
            }
        } finally {
            fr.close();
        }
    }

    public static PersistenceManagerFactory getPersistenceManagerFactory(String organisationID) {
        PersistenceManagerFactory pmf;
        try {
            InitialContext initCtx = new InitialContext();
            try {
                pmf = (PersistenceManagerFactory) initCtx.lookup(OrganisationCf.PERSISTENCE_MANAGER_FACTORY_PREFIX_ABSOLUTE + organisationID);
            } finally {
                initCtx.close();
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        return pmf;
    }

    public PersistenceManager getPersistenceManager(String organisationID) {
        PersistenceManagerFactory pmf = getPersistenceManagerFactory(organisationID);
        PersistenceManager pm = pmf.getPersistenceManager();
        NLJDOHelper.setThreadPersistenceManager(pm);
        return pm;
    }

    protected PersistenceManagerFactory waitForPersistenceManagerFactory(String persistenceManagerJNDIName) throws ModuleException {
        try {
            InitialContext initCtx = new InitialContext();
            try {
                PersistenceManagerFactory pmf = null;
                long waitStartDT = System.currentTimeMillis();
                int timeout = createOrganisationConfigModule.getWaitForPersistenceManager_timeout();
                int checkPeriod = createOrganisationConfigModule.getWaitForPersistenceManager_checkPeriod();
                while (pmf == null) {
                    try {
                        pmf = (PersistenceManagerFactory) initCtx.lookup(persistenceManagerJNDIName);
                    } catch (NamingException x) {
                        if (System.currentTimeMillis() < waitStartDT) {
                            waitStartDT = System.currentTimeMillis();
                            logger.warn("While waiting for deployment of PersistenceManagerFactory \"" + persistenceManagerJNDIName + "\", the system time has been changed. Resetting wait time.");
                        }
                        if (System.currentTimeMillis() - waitStartDT > timeout) {
                            logger.fatal("PersistenceManagerFactory \"" + persistenceManagerJNDIName + "\" has not become accessible in JNDI within timeout (\"" + timeout + "\" msec).");
                            throw x;
                        } else try {
                            logger.info("PersistenceManagerFactory \"" + persistenceManagerJNDIName + "\" is not yet accessible in JNDI. Waiting " + checkPeriod + " msec.");
                            Thread.sleep(checkPeriod);
                        } catch (InterruptedException e) {
                            logger.error("Sleeping has been interrupted!", e);
                        }
                    }
                }
                return pmf;
            } finally {
                initCtx.close();
            }
        } catch (Exception x) {
            throw new ModuleException(x);
        }
    }

    protected CLRegistrar getCLRegistrar(JFireBasePrincipal principal) throws ModuleException {
        return clRegistrarFactory.getCLRegistrar(principal);
    }

    /**
	 * key: UserID userID<br/>
	 * value: String password
	 */
    private Map<UserID, String> jfireSecurity_tempUserPasswords = new HashMap<UserID, String>();

    /**
	 * @deprecated Use {@link #jfireSecurity_checkTempUserPassword(UserID, String)} instead.
	 */
    @Deprecated
    protected boolean jfireSecurity_checkTempUserPassword(String organisationID, String userID, String password) {
        return jfireSecurity_checkTempUserPassword(UserID.create(organisationID, userID), password);
    }

    protected boolean jfireSecurity_checkTempUserPassword(UserID userID, String password) {
        String pw;
        synchronized (jfireSecurity_tempUserPasswords) {
            pw = jfireSecurity_tempUserPasswords.get(userID);
            if (pw == null) return false;
        }
        return pw.equals(password);
    }

    /**
	 * @deprecated Use {@link #jfireSecurity_createTempUserPassword(UserID)} instead!
	 */
    @Deprecated
    protected String jfireSecurity_createTempUserPassword(String organisationID, String userID) {
        return jfireSecurity_createTempUserPassword(UserID.create(organisationID, userID));
    }

    protected String jfireSecurity_createTempUserPassword(UserID userID) {
        synchronized (jfireSecurity_tempUserPasswords) {
            String pw = jfireSecurity_tempUserPasswords.get(userID);
            if (pw == null) {
                pw = UserLocal.createMachinePassword(15, 20);
                jfireSecurity_tempUserPasswords.put(userID, pw);
            }
            return pw;
        }
    }

    /**
	 * This Map caches all the roles for all the users. It does NOT expire, because
	 * it relies on that {@link #jfireSecurity_flushCache()} or {@link #jfireSecurity_flushCache(String, String)}
	 * is executed whenever access rights change!
	 *
	 * key: String userID + @ + organisationID<br/>
	 * value: SoftReference of RoleSet roleSet
	 */
    protected Map<String, SoftReference<RoleSet>> jfireSecurity_roleCache = new HashMap<String, SoftReference<RoleSet>>();

    protected void jfireSecurity_flushCache(UserID _userID) {
        if (User.USER_ID_OTHER.equals(_userID.userID)) {
            jfireSecurity_flushCache();
            return;
        }
        String userPK = _userID.userID + '@' + _userID.organisationID;
        synchronized (jfireSecurity_roleCache) {
            jfireSecurity_roleCache.remove(userPK);
        }
    }

    protected void jfireSecurity_flushCache() {
        synchronized (jfireSecurity_roleCache) {
            jfireSecurity_roleCache.clear();
        }
    }

    protected static final Principal loginWithoutWorkstationRolePrincipal = new SimplePrincipal(org.nightlabs.jfire.workstation.RoleConstants.loginWithoutWorkstation.roleID);

    protected static final Principal serverAdminRolePrincipal = new SimplePrincipal(RoleConstants.serverAdmin.roleID);

    protected static final Principal systemRolePrincipal = new SimplePrincipal(User.USER_ID_SYSTEM);

    protected static final Principal guestRolePrincipal = new SimplePrincipal(RoleConstants.guest.roleID);

    /**
	 * Get the roles that are assigned to a certain user.
	 *
	 * @param pm The PersistenceManager to be used to access the datastore. Can be <code>null</code> (in this case, the method will obtain and close a PM itself).
	 * @param organisationID The organisationID of the user.
	 * @param userID The userID of the user.
	 * @return the role-set of the specified user.
	 * @throws ModuleException if sth. goes wrong
	 */
    protected RoleSet jfireSecurity_getRoleSet(PersistenceManager pm, String organisationID, String userID) throws ModuleException {
        String userPK = userID + User.SEPARATOR_BETWEEN_USER_ID_AND_ORGANISATION_ID + organisationID;
        RoleSet roleSet = null;
        synchronized (jfireSecurity_roleCache) {
            SoftReference<RoleSet> ref = jfireSecurity_roleCache.get(userPK);
            if (ref != null) roleSet = ref.get();
        }
        if (roleSet != null) return roleSet;
        roleSet = new RoleSet();
        roleSet.addMember(new SimplePrincipal(RoleConstants.guest.roleID));
        boolean closePM = false;
        if (pm == null) {
            closePM = true;
            pm = getPersistenceManager(organisationID);
        }
        try {
            if (Organisation.DEV_ORGANISATION_ID.equals(organisationID) && User.USER_ID_ANONYMOUS.equals(userID)) {
            } else if (User.USER_ID_SYSTEM.equals(userID)) {
                roleSet.addMember(loginWithoutWorkstationRolePrincipal);
                roleSet.addMember(serverAdminRolePrincipal);
                roleSet.addMember(systemRolePrincipal);
                for (Iterator<?> it = pm.getExtent(Role.class, true).iterator(); it.hasNext(); ) {
                    Role role = (Role) it.next();
                    roleSet.addMember(new SimplePrincipal(role.getRoleID()));
                }
            } else {
                pm.getExtent(AuthorizedObjectRef.class, true);
                if (getOrganisationConfig(organisationID).isServerAdmin(userID)) roleSet.addMember(new SimplePrincipal(RoleConstants.serverAdmin.roleID));
                AuthorizedObjectRef authorizedObjectRef;
                try {
                    authorizedObjectRef = (AuthorizedObjectRef) pm.getObjectById(AuthorizedObjectRefID.create(organisationID, Authority.AUTHORITY_ID_ORGANISATION, UserLocalID.create(organisationID, userID, organisationID).toString()));
                } catch (JDOObjectNotFoundException x) {
                    try {
                        authorizedObjectRef = (AuthorizedObjectRef) pm.getObjectById(AuthorizedObjectRefID.create(organisationID, Authority.AUTHORITY_ID_ORGANISATION, UserLocalID.create(organisationID, User.USER_ID_OTHER, organisationID).toString()));
                    } catch (JDOObjectNotFoundException e) {
                        authorizedObjectRef = null;
                    }
                }
                if (authorizedObjectRef != null) {
                    for (Iterator<RoleRef> it = authorizedObjectRef.getRoleRefs().iterator(); it.hasNext(); ) {
                        RoleRef roleRef = it.next();
                        roleSet.addMember(roleRef.getRolePrincipal());
                    }
                }
            }
        } finally {
            if (closePM) pm.close();
        }
        synchronized (jfireSecurity_roleCache) {
            jfireSecurity_roleCache.put(userPK, new SoftReference<RoleSet>(roleSet));
        }
        return roleSet;
    }

    @Override
    public List<J2eeServerTypeRegistryConfigModule.J2eeRemoteServer> getJ2eeRemoteServers() {
        return Collections.unmodifiableList(j2eeLocalServerCf.getJ2eeRemoteServers());
    }

    @Override
    public J2eeServerTypeRegistryConfigModule.J2eeRemoteServer getJ2eeRemoteServer(String j2eeServerType) {
        return j2eeLocalServerCf.getJ2eeRemoteServer(j2eeServerType);
    }

    @Override
    public String getInitialContextFactory(String j2eeServerTypeRemote, boolean throwExceptionIfUnknownServerType) {
        J2eeServerTypeRegistryConfigModule.J2eeRemoteServer j2eeRemoteServerCf = j2eeLocalServerCf.getJ2eeRemoteServer(j2eeServerTypeRemote);
        if (j2eeRemoteServerCf == null) {
            if (throwExceptionIfUnknownServerType) throw new IllegalArgumentException("No configuration for remote j2eeServerType \"" + j2eeServerTypeRemote + "\"!");
            return null;
        }
        return j2eeRemoteServerCf.getInitialContextFactory();
    }

    @Override
    public ServerCf getLocalServer() {
        return (ServerCf) mcf.getConfigModule().getLocalServer().clone();
    }

    @Override
    public boolean isUpAndRunning() {
        return upAndRunning;
    }

    @Override
    public boolean isShuttingDown() {
        return shuttingDown;
    }
}
