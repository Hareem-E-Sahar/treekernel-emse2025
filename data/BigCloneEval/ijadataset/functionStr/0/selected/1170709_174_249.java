public class Test {    public void testConcurrentCachedDirtyQueries() throws Exception {
        if (getDialect().doesReadCommittedCauseWritersToBlockReaders()) {
            reportSkip("write locks block readers", "concurrent queries");
            return;
        }
        SimpleJtaTransactionManagerImpl.getInstance().begin();
        Session s = openSession();
        Map foo = new HashMap();
        foo.put("name", "Foo");
        foo.put("description", "a big foo");
        s.persist("Item", foo);
        Map bar = new HashMap();
        bar.put("name", "Bar");
        bar.put("description", "a small bar");
        s.persist("Item", bar);
        SimpleJtaTransactionManagerImpl.getInstance().commit();
        synchronized (this) {
            wait(1000);
        }
        getSessions().getStatistics().clear();
        getSessions().evictEntity("Item");
        SimpleJtaTransactionManagerImpl.getInstance().begin();
        Session s4 = openSession();
        Transaction tx4 = SimpleJtaTransactionManagerImpl.getInstance().suspend();
        SimpleJtaTransactionManagerImpl.getInstance().begin();
        Session s1 = openSession();
        List r1 = s1.createCriteria("Item").addOrder(Order.asc("description")).setCacheable(true).list();
        assertEquals(r1.size(), 2);
        foo = (Map) r1.get(0);
        foo.put("description", "a big red foo");
        s1.flush();
        Transaction tx1 = SimpleJtaTransactionManagerImpl.getInstance().suspend();
        SimpleJtaTransactionManagerImpl.getInstance().begin();
        Session s2 = openSession();
        List r2 = s2.createCriteria("Item").addOrder(Order.asc("description")).setCacheable(true).list();
        assertEquals(r2.size(), 2);
        SimpleJtaTransactionManagerImpl.getInstance().commit();
        assertEquals(getSessions().getStatistics().getSecondLevelCacheHitCount(), 0);
        assertEquals(getSessions().getStatistics().getSecondLevelCacheMissCount(), 0);
        assertEquals(getSessions().getStatistics().getEntityLoadCount(), 4);
        assertEquals(getSessions().getStatistics().getEntityFetchCount(), 0);
        assertEquals(getSessions().getStatistics().getQueryExecutionCount(), 2);
        assertEquals(getSessions().getStatistics().getQueryCachePutCount(), 2);
        assertEquals(getSessions().getStatistics().getQueryCacheHitCount(), 0);
        assertEquals(getSessions().getStatistics().getQueryCacheMissCount(), 2);
        SimpleJtaTransactionManagerImpl.getInstance().resume(tx1);
        tx1.commit();
        SimpleJtaTransactionManagerImpl.getInstance().begin();
        Session s3 = openSession();
        s3.createCriteria("Item").addOrder(Order.asc("description")).setCacheable(true).list();
        SimpleJtaTransactionManagerImpl.getInstance().commit();
        assertEquals(getSessions().getStatistics().getSecondLevelCacheHitCount(), 0);
        assertEquals(getSessions().getStatistics().getSecondLevelCacheMissCount(), 0);
        assertEquals(getSessions().getStatistics().getEntityLoadCount(), 6);
        assertEquals(getSessions().getStatistics().getEntityFetchCount(), 0);
        assertEquals(getSessions().getStatistics().getQueryExecutionCount(), 3);
        assertEquals(getSessions().getStatistics().getQueryCachePutCount(), 3);
        assertEquals(getSessions().getStatistics().getQueryCacheHitCount(), 0);
        assertEquals(getSessions().getStatistics().getQueryCacheMissCount(), 3);
        SimpleJtaTransactionManagerImpl.getInstance().resume(tx4);
        List r4 = s4.createCriteria("Item").addOrder(Order.asc("description")).setCacheable(true).list();
        assertEquals(r4.size(), 2);
        tx4.commit();
        assertEquals(getSessions().getStatistics().getSecondLevelCacheHitCount(), 2);
        assertEquals(getSessions().getStatistics().getSecondLevelCacheMissCount(), 0);
        assertEquals(getSessions().getStatistics().getEntityLoadCount(), 6);
        assertEquals(getSessions().getStatistics().getEntityFetchCount(), 0);
        assertEquals(getSessions().getStatistics().getQueryExecutionCount(), 3);
        assertEquals(getSessions().getStatistics().getQueryCachePutCount(), 3);
        assertEquals(getSessions().getStatistics().getQueryCacheHitCount(), 1);
        assertEquals(getSessions().getStatistics().getQueryCacheMissCount(), 3);
        SimpleJtaTransactionManagerImpl.getInstance().begin();
        s = openSession();
        s.createQuery("delete from Item").executeUpdate();
        SimpleJtaTransactionManagerImpl.getInstance().commit();
    }
}