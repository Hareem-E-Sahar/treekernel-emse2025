package com.dukesoftware.utils.cache;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

public class LRUCache<K, V> implements Cache<K, V> {

    private Logger log = Logger.getLogger("LRUCache");

    protected final ConcurrentHashMap<K, LRUCacheEntry<V>> cache;

    private final List<K> usedList;

    private int cacheSize = 1024;

    private long cacheLifetime = 1000 * 60 * 60;

    private long minAverage = 1000 * 60;

    private int minCount = 3;

    private static class LRUCacheEntry<V> extends CacheEntry<V> {

        private int cnt = 0;

        private long lastAccessTime = -1;

        private long average = 0;

        public LRUCacheEntry(V value, Date timestamp) {
            super(value, timestamp);
        }

        public V getValue() {
            cnt++;
            long t = lastAccessTime;
            lastAccessTime = System.currentTimeMillis();
            t = lastAccessTime - t;
            average = (average + t) / 2;
            return super.getValue();
        }

        public long getAverage() {
            return average;
        }

        public int getCnt() {
            return cnt;
        }

        public long getLastAccessTime() {
            return lastAccessTime;
        }
    }

    public LRUCache(int cacheSize, long cacheLifetime, int minCount, long minAverage) {
        this.cacheSize = cacheSize;
        usedList = new LinkedList<K>();
        this.cache = new ConcurrentHashMap<K, LRUCacheEntry<V>>(cacheSize);
        this.minCount = minCount;
        this.minAverage = minAverage;
    }

    public Map<K, V> read(Collection<K> keys) throws CacheException {
        Map<K, V> result = new HashMap<K, V>();
        for (K key : keys) {
            V value = null;
            if ((value = getEntry(key)) != null) {
                result.put(key, value);
            } else {
                result.put(key, null);
            }
        }
        return result;
    }

    public V read(K key) throws CacheException {
        return getEntry(key);
    }

    public synchronized void setCacheSize(int size) {
        cacheSize = size;
    }

    public synchronized int getCacheSize() {
        return cacheSize;
    }

    public synchronized V getEntry(K key) {
        if (cache.containsKey(key)) {
            LRUCacheEntry<V> entry = cache.get(key);
            if ((entry.getTimestamp().getTime() + cacheLifetime) > System.currentTimeMillis()) {
                if (usedList.contains(key)) {
                    usedList.remove(key);
                }
                log.debug("add used list... " + key);
                usedList.add(key);
                return entry.getValue();
            } else {
                log.debug("delete entry... " + key);
                deleteEntry(key);
            }
        }
        return null;
    }

    public synchronized boolean addEntry(K key, V value) {
        log.debug("addEntry... " + cache.size());
        if (cache.size() >= cacheSize) {
            K k = usedList.get(usedList.size() - 1);
            LRUCacheEntry<V> entry = cache.get(k);
            if (entry.getCnt() <= minCount || entry.getAverage() <= minAverage) {
                deleteEntry(k);
            }
        }
        if (cache.size() < cacheSize) {
            cache.put(key, new LRUCacheEntry<V>(value, new Date()));
            usedList.add(key);
            return true;
        }
        return false;
    }

    public synchronized void replaceEntry(K key, V value) {
        log.debug("replaceEntry... " + key);
        if (usedList.size() == 0) {
            addEntry(key, value);
        }
        K topKey = usedList.get(0);
        usedList.remove(topKey);
        cache.remove(key);
        cache.put(key, new LRUCacheEntry<V>(value, new Date()));
        usedList.add(key);
    }

    public synchronized void deleteEntry(K key) {
        log.debug("deleteEntry... " + key);
        if (cache.containsKey(key)) {
            cache.remove(key);
        }
        if (usedList.contains(key)) {
            usedList.remove(key);
        }
    }

    public long getCacheLifetime() {
        return cacheLifetime;
    }

    public void setCacheLifetime(long cacheLifetime) {
        this.cacheLifetime = cacheLifetime;
    }

    public long getMinAverage() {
        return minAverage;
    }

    public void setMinAverage(long maxAverage) {
        this.minAverage = maxAverage;
    }

    public int getMinCount() {
        return minCount;
    }

    public void setMinCount(int minCount) {
        this.minCount = minCount;
    }

    public static void main(String[] args) {
        LRUCache<String, String> cache = new LRUCache<String, String>(100, 1000, 10, 20);
        cache.addEntry("A", "1");
    }
}
