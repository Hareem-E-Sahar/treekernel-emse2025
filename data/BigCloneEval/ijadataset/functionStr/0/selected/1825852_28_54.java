public class Test {    public static CacheConcurrencyStrategy createCache(final String concurrencyStrategy, String regionName, final boolean mutable, final Settings settings, final Properties properties) throws HibernateException {
        if (concurrencyStrategy == null || !settings.isSecondLevelCacheEnabled()) return null;
        String prefix = settings.getCacheRegionPrefix();
        if (prefix != null) regionName = prefix + '.' + regionName;
        if (log.isDebugEnabled()) log.debug("instantiating cache region: " + regionName + " usage strategy: " + concurrencyStrategy);
        final CacheConcurrencyStrategy ccs;
        if (concurrencyStrategy.equals(READ_ONLY)) {
            if (mutable) log.warn("read-only cache configured for mutable class: " + regionName);
            ccs = new ReadOnlyCache();
        } else if (concurrencyStrategy.equals(READ_WRITE)) {
            ccs = new ReadWriteCache();
        } else if (concurrencyStrategy.equals(NONSTRICT_READ_WRITE)) {
            ccs = new NonstrictReadWriteCache();
        } else if (concurrencyStrategy.equals(TRANSACTIONAL)) {
            ccs = new TransactionalCache();
        } else {
            throw new MappingException("cache usage attribute should be read-write, read-only, nonstrict-read-write or transactional");
        }
        final Cache impl;
        try {
            impl = settings.getCacheProvider().buildCache(regionName, properties);
        } catch (CacheException e) {
            throw new HibernateException("Could not instantiate cache implementation", e);
        }
        ccs.setCache(impl);
        return ccs;
    }
}