package org.apache.naming.resources;

import java.util.HashMap;
import java.util.Random;

/**
 * Implements a special purpose cache.
 * 
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @version $Revision: 467222 $
 */
public class ResourceCache {

    public ResourceCache() {
    }

    /**
     * Random generator used to determine elements to free.
     */
    protected Random random = new Random();

    /**
     * Cache.
     * Path -> Cache entry.
     */
    protected CacheEntry[] cache = new CacheEntry[0];

    /**
     * Not found cache.
     */
    protected HashMap notFoundCache = new HashMap();

    /**
     * Max size of resources which will have their content cached.
     */
    protected int cacheMaxSize = 10240;

    /**
     * Max amount of removals during a make space.
     */
    protected int maxAllocateIterations = 20;

    /**
     * Entry hit ratio at which an entry will never be removed from the cache.
     * Compared with entry.access / hitsCount
     */
    protected long desiredEntryAccessRatio = 3;

    /**
     * Spare amount of not found entries.
     */
    protected int spareNotFoundEntries = 500;

    /**
     * Current cache size in KB.
     */
    protected int cacheSize = 0;

    /**
     * Number of accesses to the cache.
     */
    protected long accessCount = 0;

    /**
     * Number of cache hits.
     */
    protected long hitsCount = 0;

    /**
     * Return the access count.
     * Note: Update is not synced, so the number may not be completely 
     * accurate.
     */
    public long getAccessCount() {
        return accessCount;
    }

    /**
     * Return the maximum size of the cache in KB.
     */
    public int getCacheMaxSize() {
        return cacheMaxSize;
    }

    /**
     * Set the maximum size of the cache in KB.
     */
    public void setCacheMaxSize(int cacheMaxSize) {
        this.cacheMaxSize = cacheMaxSize;
    }

    /**
     * Return the current cache size in KB.
     */
    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * Return desired entry access ratio.
     */
    public long getDesiredEntryAccessRatio() {
        return desiredEntryAccessRatio;
    }

    /**
     * Set the desired entry access ratio.
     */
    public void setDesiredEntryAccessRatio(long desiredEntryAccessRatio) {
        this.desiredEntryAccessRatio = desiredEntryAccessRatio;
    }

    /**
     * Return the number of cache hits.
     * Note: Update is not synced, so the number may not be completely 
     * accurate.
     */
    public long getHitsCount() {
        return hitsCount;
    }

    /**
     * Return the maximum amount of iterations during a space allocation.
     */
    public int getMaxAllocateIterations() {
        return maxAllocateIterations;
    }

    /**
     * Set the maximum amount of iterations during a space allocation.
     */
    public void setMaxAllocateIterations(int maxAllocateIterations) {
        this.maxAllocateIterations = maxAllocateIterations;
    }

    /**
     * Return the amount of spare not found entries.
     */
    public int getSpareNotFoundEntries() {
        return spareNotFoundEntries;
    }

    /**
     * Set the amount of spare not found entries.
     */
    public void setSpareNotFoundEntries(int spareNotFoundEntries) {
        this.spareNotFoundEntries = spareNotFoundEntries;
    }

    public boolean allocate(int space) {
        int toFree = space - (cacheMaxSize - cacheSize);
        if (toFree <= 0) {
            return true;
        }
        toFree += (cacheMaxSize / 20);
        int size = notFoundCache.size();
        if (size > spareNotFoundEntries) {
            notFoundCache.clear();
            cacheSize -= size;
            toFree -= size;
        }
        if (toFree <= 0) {
            return true;
        }
        int attempts = 0;
        int entriesFound = 0;
        long totalSpace = 0;
        int[] toRemove = new int[maxAllocateIterations];
        while (toFree > 0) {
            if (attempts == maxAllocateIterations) {
                return false;
            }
            if (toFree > 0) {
                int entryPos = -1;
                boolean unique = false;
                while (!unique) {
                    unique = true;
                    entryPos = random.nextInt(cache.length);
                    for (int i = 0; i < entriesFound; i++) {
                        if (toRemove[i] == entryPos) {
                            unique = false;
                        }
                    }
                }
                long entryAccessRatio = ((cache[entryPos].accessCount * 100) / accessCount);
                if (entryAccessRatio < desiredEntryAccessRatio) {
                    toRemove[entriesFound] = entryPos;
                    totalSpace += cache[entryPos].size;
                    toFree -= cache[entryPos].size;
                    entriesFound++;
                }
            }
            attempts++;
        }
        java.util.Arrays.sort(toRemove, 0, entriesFound);
        CacheEntry[] newCache = new CacheEntry[cache.length - entriesFound];
        int pos = 0;
        int n = -1;
        if (entriesFound > 0) {
            n = toRemove[0];
            for (int i = 0; i < cache.length; i++) {
                if (i == n) {
                    if ((pos + 1) < entriesFound) {
                        n = toRemove[pos + 1];
                        pos++;
                    } else {
                        pos++;
                        n = -1;
                    }
                } else {
                    newCache[i - pos] = cache[i];
                }
            }
        }
        cache = newCache;
        cacheSize -= totalSpace;
        return true;
    }

    public CacheEntry lookup(String name) {
        CacheEntry cacheEntry = null;
        CacheEntry[] currentCache = cache;
        accessCount++;
        int pos = find(currentCache, name);
        if ((pos != -1) && (name.equals(currentCache[pos].name))) {
            cacheEntry = currentCache[pos];
        }
        if (cacheEntry == null) {
            try {
                cacheEntry = (CacheEntry) notFoundCache.get(name);
            } catch (Exception e) {
            }
        }
        if (cacheEntry != null) {
            hitsCount++;
        }
        return cacheEntry;
    }

    public void load(CacheEntry entry) {
        if (entry.exists) {
            if (insertCache(entry)) {
                cacheSize += entry.size;
            }
        } else {
            int sizeIncrement = (notFoundCache.get(entry.name) == null) ? 1 : 0;
            notFoundCache.put(entry.name, entry);
            cacheSize += sizeIncrement;
        }
    }

    public boolean unload(String name) {
        CacheEntry removedEntry = removeCache(name);
        if (removedEntry != null) {
            cacheSize -= removedEntry.size;
            return true;
        } else if (notFoundCache.remove(name) != null) {
            cacheSize--;
            return true;
        }
        return false;
    }

    /**
     * Find a map elemnt given its name in a sorted array of map elements.
     * This will return the index for the closest inferior or equal item in the
     * given array.
     */
    private static final int find(CacheEntry[] map, String name) {
        int a = 0;
        int b = map.length - 1;
        if (b == -1) {
            return -1;
        }
        if (name.compareTo(map[0].name) < 0) {
            return -1;
        }
        if (b == 0) {
            return 0;
        }
        int i = 0;
        while (true) {
            i = (b + a) / 2;
            int result = name.compareTo(map[i].name);
            if (result > 0) {
                a = i;
            } else if (result == 0) {
                return i;
            } else {
                b = i;
            }
            if ((b - a) == 1) {
                int result2 = name.compareTo(map[b].name);
                if (result2 < 0) {
                    return a;
                } else {
                    return b;
                }
            }
        }
    }

    /**
     * Insert into the right place in a sorted MapElement array, and prevent
     * duplicates.
     */
    private final boolean insertCache(CacheEntry newElement) {
        CacheEntry[] oldCache = cache;
        int pos = find(oldCache, newElement.name);
        if ((pos != -1) && (newElement.name.equals(oldCache[pos].name))) {
            return false;
        }
        CacheEntry[] newCache = new CacheEntry[cache.length + 1];
        System.arraycopy(oldCache, 0, newCache, 0, pos + 1);
        newCache[pos + 1] = newElement;
        System.arraycopy(oldCache, pos + 1, newCache, pos + 2, oldCache.length - pos - 1);
        cache = newCache;
        return true;
    }

    /**
     * Insert into the right place in a sorted MapElement array.
     */
    private final CacheEntry removeCache(String name) {
        CacheEntry[] oldCache = cache;
        int pos = find(oldCache, name);
        if ((pos != -1) && (name.equals(oldCache[pos].name))) {
            CacheEntry[] newCache = new CacheEntry[cache.length - 1];
            System.arraycopy(oldCache, 0, newCache, 0, pos);
            System.arraycopy(oldCache, pos + 1, newCache, pos, oldCache.length - pos - 1);
            cache = newCache;
            return oldCache[pos];
        }
        return null;
    }
}
