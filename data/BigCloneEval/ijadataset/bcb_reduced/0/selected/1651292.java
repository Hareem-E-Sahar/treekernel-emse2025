package net.redlightning.dht.kad;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.redlightning.dht.kad.DHT.DHTtype;
import net.redlightning.dht.kad.utils.ByteWrapper;
import net.redlightning.dht.kad.utils.SHA1;
import net.redlightning.dht.kad.utils.ThreadLocalUtils;

/**
 * @author Damokles
 * 
 */
public class Database {

    private ConcurrentMap<Key, Set<DBItem>> items;

    private DatabaseStats stats;

    private AtomicLong timestampCurrent = new AtomicLong();

    private volatile long timestampPrevious;

    private static byte[] sessionSecret = new byte[20];

    static {
        ThreadLocalUtils.getThreadLocalRandom().nextBytes(sessionSecret);
    }

    Database() {
        stats = new DatabaseStats();
        items = new ConcurrentHashMap<Key, Set<DBItem>>(3000);
    }

    /**
	 * Store an entry in the database
	 * 
	 * @param key
	 *            The key
	 * @param dbi
	 *            The DBItem to store
	 */
    public void store(Key key, DBItem dbi) {
        Set<DBItem> keyEntries = null;
        Set<DBItem> insertCanidate = new HashSet<DBItem>();
        keyEntries = items.putIfAbsent(key, insertCanidate);
        if (keyEntries == null) {
            keyEntries = insertCanidate;
            stats.setKeyCount(items.size());
        }
        synchronized (keyEntries) {
            if (!keyEntries.remove(dbi)) stats.setItemCount(stats.getItemCount() + 1);
            keyEntries.add(dbi);
        }
    }

    /**
	 * Get max_entries items from the database, which have the same key, items
	 * are taken randomly from the list. If the key is not present no items will
	 * be returned, if there are fewer then max_entries items for the key, all
	 * entries will be returned
	 * 
	 * @param key
	 *            The key to search for
	 * @param dbl
	 *            The list to store the items in
	 * @param max_entries
	 *            The maximum number entries
	 */
    List<DBItem> sample(Key key, int max_entries, DHTtype forType, boolean preferPeers) {
        Set<DBItem> keyEntry = null;
        List<DBItem> dbl = null;
        keyEntry = items.get(key);
        if (keyEntry == null) return null;
        synchronized (keyEntry) {
            dbl = new ArrayList<DBItem>(keyEntry);
        }
        List<DBItem> peerlist = new ArrayList<DBItem>(dbl.size());
        List<DBItem> seedlist = preferPeers ? new ArrayList<DBItem>(dbl.size() >> 1) : null;
        Collections.shuffle(dbl);
        for (DBItem item : dbl) {
            if (!(item instanceof PeerAddressDBItem)) continue;
            PeerAddressDBItem it = (PeerAddressDBItem) item;
            if (it.getAddressType() != forType.PREFERRED_ADDRESS_TYPE) continue;
            if (preferPeers && it.isSeed()) seedlist.add(it); else peerlist.add(it);
        }
        if (preferPeers) peerlist.addAll(seedlist);
        return peerlist.subList(0, Math.min(peerlist.size(), max_entries));
    }

    BloomFilterBEP33 createScrapeFilter(Key key, boolean seedFilter) {
        Set<DBItem> dbl = items.get(key);
        if (dbl == null || dbl.isEmpty()) return null;
        BloomFilterBEP33 filter = new BloomFilterBEP33();
        synchronized (dbl) {
            for (DBItem item : dbl) {
                if (!(item instanceof PeerAddressDBItem)) continue;
                PeerAddressDBItem it = (PeerAddressDBItem) item;
                if (seedFilter == it.isSeed()) filter.insert(it.getInetAddress());
            }
        }
        return filter;
    }

    /**
	 * Expire all items older than 30 minutes
	 * 
	 * @param now
	 *            The time it is now (we pass this along so we only have to
	 *            calculate it once)
	 */
    void expire(long now) {
        int itemCount = 0;
        for (Set<DBItem> dbl : items.values()) {
            List<DBItem> tmp = null;
            synchronized (dbl) {
                tmp = new ArrayList<DBItem>(dbl);
                Collections.sort(tmp, DBItem.ageOrdering);
                while (dbl.size() > 0 && tmp.get(0).expired(now)) {
                    dbl.remove(tmp.remove(0));
                }
                itemCount += dbl.size();
            }
        }
        for (Iterator<Set<DBItem>> it = items.values().iterator(); it.hasNext(); ) {
            if (it.next().isEmpty()) it.remove();
        }
        stats.setKeyCount(items.size());
        stats.setItemCount(itemCount);
    }

    boolean insertForKeyAllowed(Key target) {
        Set<DBItem> entries = items.get(target);
        if (entries == null) return true;
        if (entries.size() < DHTConstants.MAX_DB_ENTRIES_PER_KEY) return true;
        return false;
    }

    ThreadLocal<SHA1> hashStore = new ThreadLocal<SHA1>();

    private SHA1 getHasher() {
        SHA1 hasher = hashStore.get();
        if (hasher == null) {
            hasher = new SHA1();
            hashStore.set(hasher);
        } else hasher.reset();
        return hasher;
    }

    /**
	 * Generate a write token, which will give peers write access to the DB.
	 * 
	 * @param ip
	 *            The IP of the peer
	 * @param port
	 *            The port of the peer
	 * @return A Key
	 */
    ByteWrapper genToken(InetAddress ip, int port, Key lookupKey) {
        updateTokenTimestamps();
        byte[] tdata = new byte[ip.getAddress().length + 2 + 8 + Key.SHA1_HASH_LENGTH + sessionSecret.length];
        ByteBuffer bb = ByteBuffer.wrap(tdata);
        bb.put(ip.getAddress());
        bb.putShort((short) port);
        bb.putLong(timestampCurrent.get());
        bb.put(lookupKey.getHash());
        bb.put(sessionSecret);
        return new ByteWrapper(getHasher().digest(bb));
    }

    private void updateTokenTimestamps() {
        long current = timestampCurrent.get();
        long now = System.nanoTime();
        while (TimeUnit.NANOSECONDS.toMillis(now - current) > DHTConstants.TOKEN_TIMEOUT) {
            if (timestampCurrent.compareAndSet(current, now)) {
                timestampPrevious = current;
                break;
            }
            current = timestampCurrent.get();
        }
    }

    /**
	 * Check if a received token is OK.
	 * 
	 * @param token
	 *            The token received
	 * @param ip
	 *            The ip of the sender
	 * @param port
	 *            The port of the sender
	 * @return true if the token was given to this peer, false other wise
	 */
    boolean checkToken(ByteWrapper token, InetAddress ip, int port, Key lookupKey) {
        updateTokenTimestamps();
        boolean valid = checkToken(token, ip, port, lookupKey, timestampCurrent.get()) || checkToken(token, ip, port, lookupKey, timestampPrevious);
        return valid;
    }

    private boolean checkToken(ByteWrapper token, InetAddress ip, int port, Key lookupKey, long timeStamp) {
        byte[] tdata = new byte[ip.getAddress().length + 2 + 8 + Key.SHA1_HASH_LENGTH + sessionSecret.length];
        ByteBuffer bb = ByteBuffer.wrap(tdata);
        bb.put(ip.getAddress());
        bb.putShort((short) port);
        bb.putLong(timeStamp);
        bb.put(lookupKey.getHash());
        bb.put(sessionSecret);
        return token.equals(new ByteWrapper(getHasher().digest(bb)));
    }

    boolean contains(Key key) {
        return items.containsKey(key);
    }

    /**
	 * @return the stats
	 */
    public DatabaseStats getStats() {
        return stats;
    }
}
