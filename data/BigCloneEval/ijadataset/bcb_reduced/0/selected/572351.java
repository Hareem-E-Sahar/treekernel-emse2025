package org.hibernate.cache.jbc2.builder;

import java.util.Properties;
import javax.transaction.TransactionManager;
import org.jboss.cache.Cache;
import org.jboss.cache.CacheManager;
import org.jboss.cache.CacheManagerImpl;
import org.jboss.cache.CacheStatus;
import org.jboss.cache.config.Configuration;
import org.jgroups.ChannelFactory;
import org.jgroups.JChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.jbc2.CacheInstanceManager;
import org.hibernate.cache.jbc2.util.CacheHelper;
import org.hibernate.cfg.Settings;
import org.hibernate.transaction.TransactionManagerLookup;
import org.hibernate.util.PropertiesHelper;

/**
 * Allows building separate {@link Cache} instances for each type of region,
 * with the expectation that a single multiplexed JGroups resource (i.e. a 
 * multiplexed channel or a shared transport channel) will be shared between 
 * the caches.<p/>
 * 
 * @author Steve Ebersole
 * @author Brian Stansberry
 */
public class MultiplexingCacheInstanceManager implements CacheInstanceManager {

    private static final Logger log = LoggerFactory.getLogger(MultiplexingCacheInstanceManager.class);

    /** 
     * Classpath or filesystem resource containing JBoss Cache 
     * configurations the factory should use.
     * 
     * @see #DEF_CACHE_FACTORY_RESOURCE
     */
    public static final String CACHE_FACTORY_RESOURCE_PROP = "hibernate.cache.region.jbc2.configs";

    /**
     * Classpath or filesystem resource containing JGroups protocol
     * stack configurations the <code>org.jgroups.ChannelFactory</code>
     * should use.
     * 
     * @see #DEF_JGROUPS_RESOURCE
     */
    public static final String CHANNEL_FACTORY_RESOURCE_PROP = "hibernate.cache.region.jbc2.jgroups.stacks";

    /**
     * Name of the configuration that should be used for entity caches.
     * 
     * @see #DEF_ENTITY_RESOURCE
     */
    public static final String ENTITY_CACHE_RESOURCE_PROP = "hibernate.cache.region.jbc2.cfg.entity";

    /**
     * Name of the configuration that should be used for collection caches.
     * No default value, as by default we try to use the same JBoss Cache
     * instance we use for entity caching.
     * 
     * @see #ENTITY_CACHE_RESOURCE_PROP
     * @see #DEF_ENTITY_RESOURCE
     */
    public static final String COLLECTION_CACHE_RESOURCE_PROP = "hibernate.cache.region.jbc2.cfg.collection";

    /**
     * Name of the configuration that should be used for timestamp caches.
     * 
     * @see #DEF_TS_RESOURCE
     */
    public static final String TIMESTAMP_CACHE_RESOURCE_PROP = "hibernate.cache.region.jbc2.cfg.ts";

    /**
     * Name of the configuration that should be used for query caches.
     * 
     * @see #DEF_QUERY_RESOURCE
     */
    public static final String QUERY_CACHE_RESOURCE_PROP = "hibernate.cache.region.jbc2.cfg.query";

    /**
     * Default value for {@link #CACHE_FACTORY_RESOURCE_PROP}. Specifies
     * the "jbc2-configs.xml" file in this package.
     */
    public static final String DEF_CACHE_FACTORY_RESOURCE = "org/hibernate/cache/jbc2/builder/jbc2-configs.xml";

    /**
     * Default value for {@link #CHANNEL_FACTORY_RESOURCE_PROP}. Specifies
     * the "jgroups-stacks.xml" file in this package.
     */
    public static final String DEF_JGROUPS_RESOURCE = "org/hibernate/cache/jbc2/builder/jgroups-stacks.xml";

    /**
     * Default value for {@link #ENTITY_CACHE_RESOURCE_PROP}.
     */
    public static final String DEF_ENTITY_RESOURCE = "optimistic-entity";

    /**
     * Default value for {@link #TIMESTAMP_CACHE_RESOURCE_PROP}.
     */
    public static final String DEF_TS_RESOURCE = "timestamps-cache";

    /**
     * Default value for {@link #ENTITY_CACHE_RESOURCE_PROP}.
     */
    public static final String DEF_QUERY_RESOURCE = "local-query";

    /** Cache for entities */
    private Cache jbcEntityCache;

    /** Cache for collections */
    private Cache jbcCollectionCache;

    /** Cache for timestamps */
    private Cache jbcTsCache;

    /** Cache for queries */
    private Cache jbcQueryCache;

    /** Name of config used for entities. */
    private String entityConfig = null;

    /** Name of config used for collections. */
    private String collectionConfig = null;

    /** Name of config used for queries. */
    private String queryConfig = null;

    /** Name of config used for timestamps. */
    private String tsConfig = null;

    /** Our cache factory */
    private CacheManager jbcFactory;

    /** Our channel factory */
    private ChannelFactory channelFactory;

    /** 
     * Did we create the factory ourself and thus can assume we are not
     * sharing it (and the caches) with other users?
     */
    private boolean selfCreatedFactory;

    /**
     * Create a new MultiplexingCacheInstanceManager.
     */
    public MultiplexingCacheInstanceManager() {
    }

    /**
     * Create a new MultiplexingCacheInstanceManager using the provided {@link Cache}s.
     * <p/>
     * If this constructor is used, the {@link #start(Settings, Properties)}
     * method will make no attempt to create a cache factory or obtain caches
     * from it.  Only the <code>Cache</code>s passed as arguments to this
     * constructor will be available.
     *
	 * @param jbcEntityCache The entity cache
	 * @param jbcCollectionCache the collection cache
	 * @param jbcTsCache The timestamps cache
	 * @param jbcQueryCache The query cache
     */
    public MultiplexingCacheInstanceManager(Cache jbcEntityCache, Cache jbcCollectionCache, Cache jbcTsCache, Cache jbcQueryCache) {
        this.jbcEntityCache = jbcEntityCache;
        this.jbcCollectionCache = jbcCollectionCache;
        this.jbcTsCache = jbcTsCache;
        this.jbcQueryCache = jbcQueryCache;
    }

    /**
	 * Getter for property 'cacheFactory'.
	 * @see #setCacheFactory
	 *
	 * @return Value for property 'cacheFactory'.
	 */
    public CacheManager getCacheFactory() {
        return jbcFactory;
    }

    /**
	 * Setter for property 'cacheFactory'.
	 * @see #getCacheFactory
	 *
	 * @param factory Value to set for property 'cacheFactory'.
	 */
    public void setCacheFactory(CacheManager factory) {
        this.jbcFactory = factory;
    }

    /**
	 * Getter for property 'channelFactory'.
	 * @see #setChannelFactory
	 *
	 * @return Value for property 'channelFactory'.
	 */
    public ChannelFactory getChannelFactory() {
        return channelFactory;
    }

    /**
	 * Setter for property 'channelFactory'.
	 * @see #getChannelFactory
	 *
	 * @param factory Value to set for property 'channelFactory'.
	 */
    public void setChannelFactory(ChannelFactory factory) {
        this.channelFactory = factory;
    }

    /**
     * {@inheritDoc}
     */
    public Cache getEntityCacheInstance() {
        return jbcEntityCache;
    }

    /**
     * {@inheritDoc}
     */
    public Cache getCollectionCacheInstance() {
        return jbcCollectionCache;
    }

    /**
     * {@inheritDoc}
     */
    public Cache getQueryCacheInstance() {
        if (jbcQueryCache != null && jbcTsCache == null) {
            throw new CacheException("Timestamps cache must be configured if a query cache is used");
        }
        return jbcQueryCache;
    }

    /**
     * {@inheritDoc}
     */
    public Cache getTimestampsCacheInstance() {
        if (jbcTsCache != null && CacheHelper.isClusteredInvalidation(jbcTsCache)) {
            throw new CacheException("Clustered invalidation not supported for timestamps cache");
        }
        return jbcTsCache;
    }

    /**
     * {@inheritDoc}
     */
    public void start(Settings settings, Properties properties) throws CacheException {
        try {
            TransactionManagerLookup tml = settings.getTransactionManagerLookup();
            TransactionManager tm = (tml == null ? null : tml.getTransactionManager(properties));
            boolean buildCaches = jbcEntityCache == null && jbcCollectionCache == null && jbcTsCache == null && jbcQueryCache == null;
            if (buildCaches && jbcFactory == null) {
                if (channelFactory == null) {
                    String muxStacks = PropertiesHelper.getString(CHANNEL_FACTORY_RESOURCE_PROP, properties, DEF_JGROUPS_RESOURCE);
                    if (muxStacks != null) {
                        channelFactory = new JChannelFactory();
                        channelFactory.setMultiplexerConfig(muxStacks);
                    }
                }
                String factoryRes = PropertiesHelper.getString(CACHE_FACTORY_RESOURCE_PROP, properties, DEF_CACHE_FACTORY_RESOURCE);
                jbcFactory = new CacheManagerImpl(factoryRes, channelFactory);
                ((CacheManagerImpl) jbcFactory).start();
                selfCreatedFactory = true;
            }
            if (settings.isSecondLevelCacheEnabled()) {
                if (buildCaches) {
                    entityConfig = PropertiesHelper.getString(ENTITY_CACHE_RESOURCE_PROP, properties, DEF_ENTITY_RESOURCE);
                    jbcEntityCache = jbcFactory.getCache(entityConfig, true);
                    collectionConfig = PropertiesHelper.getString(COLLECTION_CACHE_RESOURCE_PROP, properties, entityConfig);
                    if (entityConfig.equals(collectionConfig)) {
                        jbcCollectionCache = jbcEntityCache;
                    } else {
                        jbcCollectionCache = jbcFactory.getCache(collectionConfig, true);
                    }
                }
                if (jbcEntityCache != null) {
                    configureTransactionManager(jbcEntityCache, tm, false);
                    jbcEntityCache.start();
                }
                if (jbcCollectionCache != null) {
                    configureTransactionManager(jbcCollectionCache, tm, false);
                    jbcCollectionCache.start();
                }
            } else {
                jbcEntityCache = null;
                jbcCollectionCache = null;
            }
            if (settings.isQueryCacheEnabled()) {
                if (buildCaches) {
                    String dfltQueryResource = (entityConfig == null ? DEF_QUERY_RESOURCE : entityConfig);
                    queryConfig = PropertiesHelper.getString(QUERY_CACHE_RESOURCE_PROP, properties, dfltQueryResource);
                    if (queryConfig.equals(entityConfig)) {
                        jbcQueryCache = jbcEntityCache;
                    } else if (queryConfig.equals(collectionConfig)) {
                        jbcQueryCache = jbcCollectionCache;
                    } else {
                        jbcQueryCache = jbcFactory.getCache(queryConfig, true);
                    }
                    tsConfig = PropertiesHelper.getString(TIMESTAMP_CACHE_RESOURCE_PROP, properties, DEF_TS_RESOURCE);
                    if (tsConfig.equals(queryConfig)) {
                        jbcTsCache = jbcQueryCache;
                    } else if (tsConfig.equals(entityConfig)) {
                        jbcTsCache = jbcEntityCache;
                    } else if (tsConfig.equals(collectionConfig)) {
                        jbcTsCache = jbcCollectionCache;
                    } else {
                        jbcTsCache = jbcFactory.getCache(tsConfig, true);
                    }
                }
                if (jbcQueryCache != null) {
                    configureTransactionManager(jbcQueryCache, tm, false);
                    jbcQueryCache.start();
                }
                if (jbcTsCache != null) {
                    configureTransactionManager(jbcTsCache, tm, true);
                    jbcTsCache.start();
                }
            } else {
                jbcTsCache = null;
                jbcQueryCache = null;
            }
        } catch (CacheException ce) {
            throw ce;
        } catch (Throwable t) {
            throw new CacheException("Unable to start region factory", t);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {
        releaseCaches();
        if (selfCreatedFactory) {
            ((CacheManagerImpl) jbcFactory).stop();
        }
    }

    /**
     * Injects the given TransactionManager into the cache.
     * 
     * @param cache    the cache. cannot be <code>null</code>
     * @param tm        the transaction manager Hibernate recognizes
     *                  May be <code>null</code>
     * @param allowNull whether we accept a null transaction manager in the cache
     *                  if <code>tm</code> is not <code>null</code>
     * 
     * @throws CacheException if <code>cache</code> is already started and is 
     *                        configured with a different TransactionManager
     *                        than the one we would inject
     */
    private void configureTransactionManager(Cache cache, TransactionManager tm, boolean allowNull) {
        Configuration cacheConfig = cache.getConfiguration();
        TransactionManager cacheTm = cacheConfig.getRuntimeConfig().getTransactionManager();
        if (!safeEquals(tm, cacheTm)) {
            if (cache.getCacheStatus() != CacheStatus.INSTANTIATED) {
                if (!allowNull && cacheTm == null) {
                    throw new CacheException("JBoss Cache is already started with no transaction manager configured");
                } else {
                    log.debug("JBoss Cache is already started with a transaction manager (" + cacheTm + ") that is not equal to our own (" + tm + ")");
                }
            } else {
                cacheConfig.getRuntimeConfig().setTransactionManager(tm);
                if (tm == null) {
                    cacheConfig.setTransactionManagerLookupClass(null);
                }
            }
        }
    }

    /**
     * Notify cache factory that we are no longer using the caches.  
     */
    private void releaseCaches() {
        if (jbcEntityCache != null && entityConfig != null) {
            try {
                jbcFactory.releaseCache(entityConfig);
                jbcEntityCache = null;
                if (entityConfig.equals(collectionConfig)) collectionConfig = null;
                if (entityConfig.equals(queryConfig)) queryConfig = null;
                if (entityConfig.equals(tsConfig)) tsConfig = null;
                entityConfig = null;
            } catch (Throwable t) {
                log.info("Unable to release entity cache instance", t);
            }
        }
        if (jbcCollectionCache != null && collectionConfig != null) {
            try {
                jbcFactory.releaseCache(collectionConfig);
                jbcCollectionCache = null;
                if (collectionConfig.equals(queryConfig)) queryConfig = null;
                if (collectionConfig.equals(tsConfig)) tsConfig = null;
                collectionConfig = null;
            } catch (Throwable t) {
                log.info("Unable to stop collection cache instance", t);
            }
        }
        if (jbcQueryCache != null && queryConfig != null) {
            try {
                jbcFactory.releaseCache(queryConfig);
                jbcQueryCache = null;
                if (queryConfig.equals(tsConfig)) tsConfig = null;
                queryConfig = null;
            } catch (Throwable t) {
                log.info("Unable to stop query cache instance", t);
            }
        }
        if (jbcTsCache != null && tsConfig != null) {
            try {
                jbcFactory.releaseCache(tsConfig);
                jbcTsCache = null;
                tsConfig = null;
            } catch (Throwable t) {
                log.info("Unable to stop timestamp cache instance", t);
            }
        }
    }

    private boolean safeEquals(Object a, Object b) {
        return (a == b || (a != null && a.equals(b)));
    }
}
