package org.hibernate.test.cache.jbc2.query;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import junit.framework.AssertionFailedError;
import org.hibernate.cache.CacheDataDescription;
import org.hibernate.cache.QueryResultsRegion;
import org.hibernate.cache.Region;
import org.hibernate.cache.StandardQueryCache;
import org.hibernate.cache.jbc2.BasicRegionAdapter;
import org.hibernate.cache.jbc2.CacheInstanceManager;
import org.hibernate.cache.jbc2.JBossCacheRegionFactory;
import org.hibernate.cache.jbc2.query.QueryResultsRegionImpl;
import org.hibernate.cfg.Configuration;
import org.hibernate.test.cache.jbc2.AbstractGeneralDataRegionTestCase;
import org.hibernate.test.util.CacheTestUtil;
import org.jboss.cache.Cache;
import org.jboss.cache.Fqn;
import org.jboss.cache.notifications.annotation.CacheListener;
import org.jboss.cache.notifications.annotation.NodeVisited;
import org.jboss.cache.notifications.event.NodeVisitedEvent;
import org.jboss.cache.transaction.BatchModeTransactionManager;

/**
 * Tests of QueryResultRegionImpl.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 1 $
 */
public class QueryRegionImplTestCase extends AbstractGeneralDataRegionTestCase {

    /**
     * Create a new EntityRegionImplTestCase.
     * 
     * @param name
     */
    public QueryRegionImplTestCase(String name) {
        super(name);
    }

    @Override
    protected Region createRegion(JBossCacheRegionFactory regionFactory, String regionName, Properties properties, CacheDataDescription cdd) {
        return regionFactory.buildQueryResultsRegion(regionName, properties);
    }

    @Override
    protected String getStandardRegionName(String regionPrefix) {
        return regionPrefix + "/" + StandardQueryCache.class.getName();
    }

    @Override
    protected Cache getJBossCache(JBossCacheRegionFactory regionFactory) {
        CacheInstanceManager mgr = regionFactory.getCacheInstanceManager();
        return mgr.getQueryCacheInstance();
    }

    @Override
    protected Fqn getRegionFqn(String regionName, String regionPrefix) {
        return BasicRegionAdapter.getTypeLastRegionFqn(regionName, regionPrefix, QueryResultsRegionImpl.TYPE);
    }

    public void testPutDoesNotBlockGetOptimistic() throws Exception {
        putDoesNotBlockGetTest("optimistic-shared");
    }

    public void testPutDoesNotBlockGetPessimistic() throws Exception {
        putDoesNotBlockGetTest("pessimistic-shared");
    }

    private void putDoesNotBlockGetTest(String configName) throws Exception {
        Configuration cfg = createConfiguration(configName);
        JBossCacheRegionFactory regionFactory = CacheTestUtil.startRegionFactory(cfg, getCacheTestSupport());
        avoidConcurrentFlush();
        final QueryResultsRegion region = regionFactory.buildQueryResultsRegion(getStandardRegionName(REGION_PREFIX), cfg.getProperties());
        region.put(KEY, VALUE1);
        assertEquals(VALUE1, region.get(KEY));
        final CountDownLatch readerLatch = new CountDownLatch(1);
        final CountDownLatch writerLatch = new CountDownLatch(1);
        final CountDownLatch completionLatch = new CountDownLatch(1);
        final ExceptionHolder holder = new ExceptionHolder();
        Thread reader = new Thread() {

            public void run() {
                try {
                    BatchModeTransactionManager.getInstance().begin();
                    assertTrue(VALUE2.equals(region.get(KEY)) == false);
                    BatchModeTransactionManager.getInstance().commit();
                } catch (AssertionFailedError e) {
                    holder.a1 = e;
                    rollback();
                } catch (Exception e) {
                    holder.e1 = e;
                    rollback();
                } finally {
                    readerLatch.countDown();
                }
            }
        };
        Thread writer = new Thread() {

            public void run() {
                try {
                    BatchModeTransactionManager.getInstance().begin();
                    region.put(KEY, VALUE2);
                    writerLatch.await();
                    BatchModeTransactionManager.getInstance().commit();
                } catch (Exception e) {
                    holder.e2 = e;
                    rollback();
                } finally {
                    completionLatch.countDown();
                }
            }
        };
        reader.setDaemon(true);
        writer.setDaemon(true);
        writer.start();
        assertFalse("Writer is blocking", completionLatch.await(100, TimeUnit.MILLISECONDS));
        reader.start();
        assertTrue("Reader finished promptly", readerLatch.await(100, TimeUnit.MILLISECONDS));
        writerLatch.countDown();
        assertTrue("Reader finished promptly", completionLatch.await(100, TimeUnit.MILLISECONDS));
        assertEquals(VALUE2, region.get(KEY));
        if (holder.a1 != null) throw holder.a1; else if (holder.a2 != null) throw holder.a2;
        assertEquals("writer saw no exceptions", null, holder.e1);
        assertEquals("reader saw no exceptions", null, holder.e2);
    }

    public void testGetDoesNotBlockPutOptimistic() throws Exception {
        getDoesNotBlockPutTest("optimistic-shared");
    }

    public void testGetDoesNotBlockPutPessimistic() throws Exception {
        getDoesNotBlockPutTest("pessimistic-shared");
    }

    public void testGetDoesNotBlockPutPessimisticRepeatableRead() throws Exception {
        getDoesNotBlockPutTest("pessimistic-shared-repeatable");
    }

    private void getDoesNotBlockPutTest(String configName) throws Exception {
        Configuration cfg = createConfiguration(configName);
        JBossCacheRegionFactory regionFactory = CacheTestUtil.startRegionFactory(cfg, getCacheTestSupport());
        avoidConcurrentFlush();
        final QueryResultsRegion region = regionFactory.buildQueryResultsRegion(getStandardRegionName(REGION_PREFIX), cfg.getProperties());
        region.put(KEY, VALUE1);
        assertEquals(VALUE1, region.get(KEY));
        final Fqn rootFqn = getRegionFqn(getStandardRegionName(REGION_PREFIX), REGION_PREFIX);
        final Cache jbc = getJBossCache(regionFactory);
        final CountDownLatch blockerLatch = new CountDownLatch(1);
        final CountDownLatch writerLatch = new CountDownLatch(1);
        final CountDownLatch completionLatch = new CountDownLatch(1);
        final ExceptionHolder holder = new ExceptionHolder();
        Thread blocker = new Thread() {

            public void run() {
                Fqn toBlock = new Fqn(rootFqn, KEY);
                GetBlocker blocker = new GetBlocker(blockerLatch, toBlock);
                try {
                    jbc.addCacheListener(blocker);
                    BatchModeTransactionManager.getInstance().begin();
                    region.get(KEY);
                    BatchModeTransactionManager.getInstance().commit();
                } catch (Exception e) {
                    holder.e1 = e;
                    rollback();
                } finally {
                    jbc.removeCacheListener(blocker);
                }
            }
        };
        Thread writer = new Thread() {

            public void run() {
                try {
                    writerLatch.await();
                    BatchModeTransactionManager.getInstance().begin();
                    region.put(KEY, VALUE2);
                    BatchModeTransactionManager.getInstance().commit();
                } catch (Exception e) {
                    holder.e2 = e;
                    rollback();
                } finally {
                    completionLatch.countDown();
                }
            }
        };
        blocker.setDaemon(true);
        writer.setDaemon(true);
        boolean unblocked = false;
        try {
            blocker.start();
            writer.start();
            assertFalse("Blocker is blocking", completionLatch.await(100, TimeUnit.MILLISECONDS));
            writerLatch.countDown();
            assertTrue("Writer finished promptly", completionLatch.await(100, TimeUnit.MILLISECONDS));
            blockerLatch.countDown();
            unblocked = true;
            if ("PESSIMISTIC".equals(jbc.getConfiguration().getNodeLockingSchemeString()) && "REPEATABLE_READ".equals(jbc.getConfiguration().getIsolationLevelString())) {
                assertEquals(VALUE1, region.get(KEY));
            } else {
                assertEquals(VALUE2, region.get(KEY));
            }
            if (holder.a1 != null) throw holder.a1; else if (holder.a2 != null) throw holder.a2;
            assertEquals("blocker saw no exceptions", null, holder.e1);
            assertEquals("writer saw no exceptions", null, holder.e2);
        } finally {
            if (!unblocked) blockerLatch.countDown();
        }
    }

    @CacheListener
    public class GetBlocker {

        private CountDownLatch latch;

        private Fqn fqn;

        GetBlocker(CountDownLatch latch, Fqn fqn) {
            this.latch = latch;
            this.fqn = fqn;
        }

        @NodeVisited
        public void nodeVisisted(NodeVisitedEvent event) {
            if (event.isPre() && event.getFqn().equals(fqn)) {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    log.error("Interrupted waiting for latch", e);
                }
            }
        }
    }

    private class ExceptionHolder {

        Exception e1;

        Exception e2;

        AssertionFailedError a1;

        AssertionFailedError a2;
    }
}
