package com.danga.MemCached;

import java.util.*;
import java.util.zip.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;

/** 
 * This class is a connection pool for maintaning a pool of persistent connections<br/>
 * to memcached servers.
 *
 * The pool must be initialized prior to use. This should typically be early on<br/>
 * in the lifecycle of the JVM instance.<br/>
 * <br/>
 * <h3>An example of initializing using defaults:</h3>
 * <pre>
 *
 *	static {
 *		String[] serverlist = { "cache0.server.com:12345", "cache1.server.com:12345" };
 *
 *		SockIOPool pool = SockIOPool.getInstance();
 *		pool.setServers(serverlist);
 *		pool.initialize();	
 *	}
 * </pre> 
 * <h3>An example of initializing using defaults and providing weights for servers:</h3>
 *  <pre>
 *	static {
 *		String[] serverlist = { "cache0.server.com:12345", "cache1.server.com:12345" };
 *		Integer[] weights   = { new Integer(5), new Integer(2) };
 *		
 *		SockIOPool pool = SockIOPool.getInstance();
 *		pool.setServers(serverlist);
 *		pool.setWeights(weights);	
 *		pool.initialize();	
 *	}
 *  </pre> 
 * <h3>An example of initializing overriding defaults:</h3>
 *  <pre>
 *	static {
 *		String[] serverlist     = { "cache0.server.com:12345", "cache1.server.com:12345" };
 *		Integer[] weights       = { new Integer(5), new Integer(2) };	
 *		int initialConnections  = 10;
 *		int minSpareConnections = 5;
 *		int maxSpareConnections = 50;	
 *		long maxIdleTime        = 1000 * 60 * 30;	// 30 minutes
 *		long maxBusyTime        = 1000 * 60 * 5;	// 5 minutes
 *		long maintThreadSleep   = 1000 * 5;			// 5 seconds
 *		int	socketTimeOut       = 1000 * 3;			// 3 seconds to block on reads
 *		int	socketConnectTO     = 1000 * 3;			// 3 seconds to block on initial connections.  If 0, then will use blocking connect (default)
 *		boolean failover        = false;			// turn off auto-failover in event of server down	
 *		boolean nagleAlg        = false;			// turn off Nagle's algorithm on all sockets in pool	
 *		boolean aliveCheck      = false;			// disable health check of socket on checkout
 *
 *		SockIOPool pool = SockIOPool.getInstance();
 *		pool.setServers( serverlist );
 *		pool.setWeights( weights );	
 *		pool.setInitConn( initialConnections );
 *		pool.setMinConn( minSpareConnections );
 *		pool.setMaxConn( maxSpareConnections );
 *		pool.setMaxIdle( maxIdleTime );
 *		pool.setMaxBusyTime( maxBusyTime );
 *		pool.setMaintSleep( maintThreadSleep );
 *		pool.setSocketTO( socketTimeOut );
 *		pool.setNagle( nagleAlg );	
 *		pool.setHashingAlg( SockIOPool.NEW_COMPAT_HASH );
 *		pool.setAliveCheck( true );
 *		pool.initialize();	
 *	}
 *  </pre> 
 * The easiest manner in which to initialize the pool is to set the servers and rely on defaults as in the first example.<br/> 
 * After pool is initialized, a client will request a SockIO object by calling getSock with the cache key<br/>
 * The client must always close the SockIO object when finished, which will return the connection back to the pool.<br/> 
 * <h3>An example of retrieving a SockIO object:</h3>
 * <pre>
 *		SockIOPool.SockIO sock = SockIOPool.getInstance().getSock( key );
 *		try {
 *			sock.write( "version\r\n" );	
 *			sock.flush();	
 *			System.out.println( "Version: " + sock.readLine() );	
 *		}
 *		catch (IOException ioe) { System.out.println( "io exception thrown" ) };	
 *
 *		sock.close();	
 * </pre> 
 *
 * @author greg whalin <greg@whalin.com> 
 * @version 1.5
 */
public class SockIOPool {

    private static Logger log = Logger.getLogger(SockIOPool.class.getName());

    private static Map<String, SockIOPool> pools = new HashMap<String, SockIOPool>();

    public static final int NATIVE_HASH = 0;

    public static final int OLD_COMPAT_HASH = 1;

    public static final int NEW_COMPAT_HASH = 2;

    public static final long MAX_RETRY_DELAY = 10 * 60 * 1000;

    private MaintThread maintThread;

    private boolean initialized = false;

    private int maxCreate = 1;

    private Map<String, Integer> createShift;

    private int poolMultiplier = 4;

    private int initConn = 3;

    private int minConn = 3;

    private int maxConn = 10;

    private long maxIdle = 1000 * 60 * 3;

    private long maxBusyTime = 1000 * 60 * 5;

    private long maintSleep = 1000 * 5;

    private int socketTO = 1000 * 10;

    private int socketConnectTO = 1000 * 3;

    private boolean aliveCheck = false;

    private boolean failover = true;

    private boolean failback = true;

    private boolean nagle = true;

    private int hashingAlg = NATIVE_HASH;

    private final ReentrantLock hostDeadLock = new ReentrantLock();

    private String[] servers;

    private Integer[] weights;

    private List<String> buckets;

    private Map<String, Date> hostDead;

    private Map<String, Long> hostDeadDur;

    private Map<String, Map<SockIO, Long>> availPool;

    private Map<String, Map<SockIO, Long>> busyPool;

    protected SockIOPool() {
    }

    /** 
	 * Factory to create/retrieve new pools given a unique poolName. 
	 * 
	 * @param poolName unique name of the pool
	 * @return instance of SockIOPool
	 */
    public static synchronized SockIOPool getInstance(String poolName) {
        if (pools.containsKey(poolName)) return pools.get(poolName);
        SockIOPool pool = new SockIOPool();
        pools.put(poolName, pool);
        return pool;
    }

    /** 
	 * Single argument version of factory used for back compat.
	 * Simply creates a pool named "default". 
	 * 
	 * @return instance of SockIOPool
	 */
    public static SockIOPool getInstance() {
        return getInstance("default");
    }

    /** 
	 * Sets the list of all cache servers. 
	 * 
	 * @param servers String array of servers [host:port]
	 */
    public void setServers(String[] servers) {
        this.servers = servers;
    }

    /** 
	 * Returns the current list of all cache servers. 
	 * 
	 * @return String array of servers [host:port]
	 */
    public String[] getServers() {
        return this.servers;
    }

    /** 
	 * Sets the list of weights to apply to the server list.
	 *
	 * This is an int array with each element corresponding to an element<br/>
	 * in the same position in the server String array. 
	 * 
	 * @param weights Integer array of weights
	 */
    public void setWeights(Integer[] weights) {
        this.weights = weights;
    }

    /** 
	 * Returns the current list of weights. 
	 * 
	 * @return int array of weights
	 */
    public Integer[] getWeights() {
        return this.weights;
    }

    /** 
	 * Sets the initial number of connections per server in the available pool. 
	 * 
	 * @param initConn int number of connections
	 */
    public void setInitConn(int initConn) {
        this.initConn = initConn;
    }

    /** 
	 * Returns the current setting for the initial number of connections per server in
	 * the available pool. 
	 * 
	 * @return number of connections
	 */
    public int getInitConn() {
        return this.initConn;
    }

    /** 
	 * Sets the minimum number of spare connections to maintain in our available pool. 
	 * 
	 * @param minConn number of connections
	 */
    public void setMinConn(int minConn) {
        this.minConn = minConn;
    }

    /** 
	 * Returns the minimum number of spare connections in available pool. 
	 * 
	 * @return number of connections
	 */
    public int getMinConn() {
        return this.minConn;
    }

    /** 
	 * Sets the maximum number of spare connections allowed in our available pool. 
	 * 
	 * @param maxConn number of connections
	 */
    public void setMaxConn(int maxConn) {
        this.maxConn = maxConn;
    }

    /** 
	 * Returns the maximum number of spare connections allowed in available pool. 
	 * 
	 * @return number of connections
	 */
    public int getMaxConn() {
        return this.maxConn;
    }

    /** 
	 * Sets the max idle time for threads in the available pool.
	 * 
	 * @param maxIdle idle time in ms
	 */
    public void setMaxIdle(long maxIdle) {
        this.maxIdle = maxIdle;
    }

    /** 
	 * Returns the current max idle setting. 
	 * 
	 * @return max idle setting in ms
	 */
    public long getMaxIdle() {
        return this.maxIdle;
    }

    /** 
	 * Sets the max busy time for threads in the busy pool.
	 * 
	 * @param maxBusyTime idle time in ms
	 */
    public void setMaxBusyTime(long maxBusyTime) {
        this.maxBusyTime = maxBusyTime;
    }

    /** 
	 * Returns the current max busy setting. 
	 * 
	 * @return max busy setting in ms
	 */
    public long getMaxBusy() {
        return this.maxBusyTime;
    }

    /** 
	 * Set the sleep time between runs of the pool maintenance thread.
	 * If set to 0, then the maint thread will not be started. 
	 * 
	 * @param maintSleep sleep time in ms
	 */
    public void setMaintSleep(long maintSleep) {
        this.maintSleep = maintSleep;
    }

    /** 
	 * Returns the current maint thread sleep time.
	 * 
	 * @return sleep time in ms
	 */
    public long getMaintSleep() {
        return this.maintSleep;
    }

    /** 
	 * Sets the socket timeout for reads.
	 * 
	 * @param socketTO timeout in ms
	 */
    public void setSocketTO(int socketTO) {
        this.socketTO = socketTO;
    }

    /** 
	 * Returns the socket timeout for reads.
	 * 
	 * @return timeout in ms
	 */
    public int getSocketTO() {
        return this.socketTO;
    }

    /** 
	 * Sets the socket timeout for connect.
	 * 
	 * @param socketConnectTO timeout in ms
	 */
    public void setSocketConnectTO(int socketConnectTO) {
        this.socketConnectTO = socketConnectTO;
    }

    /** 
	 * Returns the socket timeout for connect.
	 * 
	 * @return timeout in ms
	 */
    public int getSocketConnectTO() {
        return this.socketConnectTO;
    }

    /** 
	 * Sets the failover flag for the pool.
	 *
	 * If this flag is set to true, and a socket fails to connect,<br/>
	 * the pool will attempt to return a socket from another server<br/>
	 * if one exists.  If set to false, then getting a socket<br/>
	 * will return null if it fails to connect to the requested server.
	 * 
	 * @param failover true/false
	 */
    public void setFailover(boolean failover) {
        this.failover = failover;
    }

    /** 
	 * Returns current state of failover flag.
	 * 
	 * @return true/false
	 */
    public boolean getFailover() {
        return this.failover;
    }

    /** 
	 * Sets the failback flag for the pool.
	 *
	 * If this is true and we have marked a host as dead,
	 * will try to bring it back.  If it is false, we will never
	 * try to resurrect a dead host.
	 *
	 * @param failback true/false
	 */
    public void setFailback(boolean failback) {
        this.failback = failback;
    }

    /** 
	 * Returns current state of failover flag.
	 * 
	 * @return true/false
	 */
    public boolean getFailback() {
        return this.failback;
    }

    /**
	 * Sets the aliveCheck flag for the pool.
	 *
	 * When true, this will attempt to talk to the server on
	 * every connection checkout to make sure the connection is
	 * still valid.  This adds extra network chatter and thus is
	 * defaulted off.  May be useful if you want to ensure you do
	 * not have any problems talking to the server on a dead connection.
	 *
	 * @param aliveCheck true/false
	 */
    public void setAliveCheck(boolean aliveCheck) {
        this.aliveCheck = aliveCheck;
    }

    /**
	 * Returns the current status of the aliveCheck flag.
	 *
	 * @return true / false
	 */
    public boolean getAliveCheck() {
        return this.aliveCheck;
    }

    /** 
	 * Sets the Nagle alg flag for the pool.
	 *
	 * If false, will turn off Nagle's algorithm on all sockets created.
	 * 
	 * @param nagle true/false
	 */
    public void setNagle(boolean nagle) {
        this.nagle = nagle;
    }

    /** 
	 * Returns current status of nagle flag
	 * 
	 * @return true/false
	 */
    public boolean getNagle() {
        return this.nagle;
    }

    /** 
	 * Sets the hashing algorithm we will use.
	 *
	 * The types are as follows.
	 *
	 * SockIOPool.NATIVE_HASH (0)     - native String.hashCode() - fast (cached) but not compatible with other clients
	 * SockIOPool.OLD_COMPAT_HASH (1) - original compatibility hashing alg (works with other clients)
	 * SockIOPool.NEW_COMPAT_HASH (2) - new CRC32 based compatibility hashing algorithm (fast and works with other clients)
	 * 
	 * @param alg int value representing hashing algorithm
	 */
    public void setHashingAlg(int alg) {
        this.hashingAlg = alg;
    }

    /** 
	 * Returns current status of customHash flag
	 * 
	 * @return true/false
	 */
    public int getHashingAlg() {
        return this.hashingAlg;
    }

    /** 
	 * Internal private hashing method.
	 *
	 * This is the original hashing algorithm from other clients.
	 * Found to be slow and have poor distribution.
	 * 
	 * @param key String to hash
	 * @return hashCode for this string using our own hashing algorithm
	 */
    private static int origCompatHashingAlg(String key) {
        int hash = 0;
        char[] cArr = key.toCharArray();
        for (int i = 0; i < cArr.length; ++i) {
            hash = (hash * 33) + cArr[i];
        }
        return hash;
    }

    /** 
	 * Internal private hashing method.
	 *
	 * This is the new hashing algorithm from other clients.
	 * Found to be fast and have very good distribution. 
	 *
	 * UPDATE: This is dog slow under java
	 * 
	 * @param key 
	 * @return 
	 */
    private static int newCompatHashingAlg(String key) {
        CRC32 checksum = new CRC32();
        checksum.update(key.getBytes());
        int crc = (int) checksum.getValue();
        return (crc >> 16) & 0x7fff;
    }

    /** 
	 * Initializes the pool. 
	 */
    public void initialize() {
        synchronized (this) {
            if (initialized && (buckets != null) && (availPool != null) && (busyPool != null)) {
                log.error("++++ trying to initialize an already initialized pool");
                return;
            }
            buckets = new ArrayList<String>();
            availPool = new HashMap<String, Map<SockIO, Long>>(servers.length * initConn);
            busyPool = new HashMap<String, Map<SockIO, Long>>(servers.length * initConn);
            hostDeadDur = new HashMap<String, Long>();
            hostDead = new HashMap<String, Date>();
            createShift = new HashMap<String, Integer>();
            maxCreate = (poolMultiplier > minConn) ? minConn : minConn / poolMultiplier;
            log.debug("++++ initializing pool with following settings:");
            log.debug("++++ initial size: " + initConn);
            log.debug("++++ min spare   : " + minConn);
            log.debug("++++ max spare   : " + maxConn);
            if (servers == null || servers.length <= 0) {
                log.error("++++ trying to initialize with no servers");
                throw new IllegalStateException("++++ trying to initialize with no servers");
            }
            for (int i = 0; i < servers.length; i++) {
                if (weights != null && weights.length > i) {
                    for (int k = 0; k < weights[i].intValue(); k++) {
                        buckets.add(servers[i]);
                        log.debug("++++ added " + servers[i] + " to server bucket");
                    }
                } else {
                    buckets.add(servers[i]);
                    log.debug("++++ added " + servers[i] + " to server bucket");
                }
                log.debug("+++ creating initial connections (" + initConn + ") for host: " + servers[i]);
                for (int j = 0; j < initConn; j++) {
                    SockIO socket = createSocket(servers[i]);
                    if (socket == null) {
                        log.error("++++ failed to create connection to: " + servers[i] + " -- only " + j + " created.");
                        break;
                    }
                    addSocketToPool(availPool, servers[i], socket);
                    log.debug("++++ created and added socket: " + socket.toString() + " for host " + servers[i]);
                }
            }
            this.initialized = true;
            if (this.maintSleep > 0) this.startMaintThread();
        }
    }

    /** 
	 * Returns state of pool. 
	 * 
	 * @return <CODE>true</CODE> if initialized.
	 */
    public boolean isInitialized() {
        return initialized;
    }

    /** 
	 * Creates a new SockIO obj for the given server.
	 *
	 * If server fails to connect, then return null and do not try<br/>
	 * again until a duration has passed.  This duration will grow<br/>
	 * by doubling after each failed attempt to connect. 
	 * 
	 * @param host host:port to connect to
	 * @return SockIO obj or null if failed to create
	 */
    protected SockIO createSocket(String host) {
        SockIO socket = null;
        hostDeadLock.lock();
        try {
            if (failback && hostDead.containsKey(host) && hostDeadDur.containsKey(host)) {
                Date store = hostDead.get(host);
                long expire = hostDeadDur.get(host).longValue();
                if ((store.getTime() + expire) > System.currentTimeMillis()) return null;
            }
        } finally {
            hostDeadLock.unlock();
        }
        try {
            socket = new SockIO(this, host, this.socketTO, this.socketConnectTO, this.nagle);
            if (!socket.isConnected()) {
                log.error("++++ failed to get SockIO obj for: " + host + " -- new socket is not connected");
                try {
                    socket.trueClose();
                } catch (Exception ex) {
                    log.error("++++ failed to close SockIO obj for server: " + host);
                    log.error(ex.getMessage(), ex);
                    socket = null;
                }
            }
        } catch (Exception ex) {
            log.error("++++ failed to get SockIO obj for: " + host);
            log.error(ex.getMessage(), ex);
            socket = null;
        }
        hostDeadLock.lock();
        try {
            if (socket == null) {
                Date now = new Date();
                hostDead.put(host, now);
                long expire = (hostDeadDur.containsKey(host)) ? (((Long) hostDeadDur.get(host)).longValue() * 2) : 1000;
                if (expire > MAX_RETRY_DELAY) expire = MAX_RETRY_DELAY;
                hostDeadDur.put(host, new Long(expire));
                log.debug("++++ ignoring dead host: " + host + " for " + expire + " ms");
                clearHostFromPool(availPool, host);
            } else {
                log.debug("++++ created socket (" + socket.toString() + ") for host: " + host);
                if (hostDead.containsKey(host) || hostDeadDur.containsKey(host)) {
                    hostDead.remove(host);
                    hostDeadDur.remove(host);
                }
            }
        } finally {
            hostDeadLock.unlock();
        }
        return socket;
    }

    /** 
	 * @param key 
	 * @return 
	 */
    public String getHost(String key) {
        return getHost(key, null);
    }

    /** 
	 * Gets the host that a particular key / hashcode resides on. 
	 * 
	 * @param key 
	 * @param hashcode 
	 * @return 
	 */
    public String getHost(String key, Integer hashcode) {
        SockIO socket = getSock(key, hashcode);
        String host = socket.getHost();
        socket.close();
        return host;
    }

    /** 
	 * Returns appropriate SockIO object given
	 * string cache key.
	 * 
	 * @param key hashcode for cache key
	 * @return SockIO obj connected to server
	 */
    public SockIO getSock(String key) {
        return getSock(key, null);
    }

    /** 
	 * Returns appropriate SockIO object given
	 * string cache key and optional hashcode.
	 *
	 * Trys to get SockIO from pool.  Fails over
	 * to additional pools in event of server failure.
	 * 
	 * @param key hashcode for cache key
	 * @param hashCode if not null, then the int hashcode to use
	 * @return SockIO obj connected to server
	 */
    public SockIO getSock(String key, Integer hashCode) {
        log.debug("cache socket pick " + key + " " + hashCode);
        if (!this.initialized) {
            log.error("attempting to get SockIO from uninitialized pool!");
            return null;
        }
        if (buckets.size() == 0) return null;
        if (buckets.size() == 1) {
            SockIO sock = getConnection((String) buckets.get(0));
            if (sock != null && sock.isConnected()) {
                if (aliveCheck) {
                    if (!sock.isAlive()) {
                        sock.close();
                        try {
                            sock.trueClose();
                        } catch (IOException ioe) {
                            log.error("failed to close dead socket");
                        }
                        sock = null;
                    }
                }
            } else {
                sock = null;
            }
            return sock;
        }
        int bucketSize = buckets.size();
        boolean[] triedBucket = new boolean[bucketSize];
        Arrays.fill(triedBucket, false);
        int bucket = getBucket(key, hashCode);
        int tries = 0;
        while (tries++ < bucketSize) {
            SockIO sock = getConnection((String) buckets.get(bucket));
            log.debug("cache choose " + buckets.get(bucket) + " for " + key);
            if (sock != null && sock.isConnected()) {
                if (aliveCheck) {
                    if (sock.isAlive()) {
                        return sock;
                    } else {
                        sock.close();
                        try {
                            sock.trueClose();
                        } catch (IOException ioe) {
                            log.error("failed to close dead socket");
                        }
                        sock = null;
                    }
                } else {
                    return sock;
                }
            } else {
                sock = null;
            }
            if (!failover) return null;
            triedBucket[bucket] = true;
            boolean needRehash = false;
            for (boolean b : triedBucket) {
                if (!b) {
                    needRehash = true;
                    break;
                }
            }
            if (needRehash) {
                log.debug("we need to rehash as we want to failover and we still have servers to try");
                int rehashTries = 0;
                while (triedBucket[bucket]) {
                    int keyTry = tries + rehashTries;
                    String newKey = String.format("%s%s", keyTry, key);
                    log.debug("rehashing with: " + keyTry);
                    bucket = getBucket(newKey, null);
                    rehashTries++;
                }
            }
        }
        return null;
    }

    /** 
	 * Returns a bucket to check for a given key. 
	 * 
	 * @param key String key cache is stored under
	 * @return int bucket
	 */
    public int getBucket(String key, Integer hashCode) {
        int hc;
        if (hashCode != null) {
            hc = hashCode.intValue();
        } else {
            switch(hashingAlg) {
                case NATIVE_HASH:
                    hc = key.hashCode();
                    break;
                case OLD_COMPAT_HASH:
                    hc = origCompatHashingAlg(key);
                    break;
                case NEW_COMPAT_HASH:
                    hc = newCompatHashingAlg(key);
                    break;
                default:
                    hc = key.hashCode();
                    hashingAlg = NATIVE_HASH;
                    break;
            }
        }
        int bucket = hc % buckets.size();
        if (bucket < 0) bucket *= -1;
        return bucket;
    }

    /** 
	 * Returns a SockIO object from the pool for the passed in host.
	 *
	 * Meant to be called from a more intelligent method<br/>
	 * which handles choosing appropriate server<br/>
	 * and failover. 
	 * 
	 * @param host host from which to retrieve object
	 * @return SockIO object or null if fail to retrieve one
	 */
    public SockIO getConnection(String host) {
        if (!this.initialized) {
            log.error("attempting to get SockIO from uninitialized pool!");
            return null;
        }
        if (host == null) return null;
        synchronized (this) {
            if (availPool != null && !availPool.isEmpty()) {
                Map<SockIO, Long> aSockets = availPool.get(host);
                if (aSockets != null && !aSockets.isEmpty()) {
                    for (Iterator<SockIO> i = aSockets.keySet().iterator(); i.hasNext(); ) {
                        SockIO socket = i.next();
                        if (socket.isConnected()) {
                            log.debug("++++ moving socket for host (" + host + ") to busy pool ... socket: " + socket);
                            i.remove();
                            addSocketToPool(busyPool, host, socket);
                            return socket;
                        } else {
                            log.error("++++ socket in avail pool is not connected: " + socket.toString() + " for host: " + host);
                            socket = null;
                            i.remove();
                        }
                    }
                }
            }
            Integer cShift = createShift.get(host);
            int shift = (cShift != null) ? cShift.intValue() : 0;
            int create = 1 << shift;
            if (create >= maxCreate) {
                create = maxCreate;
            } else {
                shift++;
            }
            createShift.put(host, new Integer(shift));
            log.debug("++++ creating " + create + " new SockIO objects");
            for (int i = create; i > 0; i--) {
                SockIO socket = createSocket(host);
                if (socket == null) break;
                if (i == 1) {
                    addSocketToPool(busyPool, host, socket);
                    return socket;
                } else {
                    addSocketToPool(availPool, host, socket);
                }
            }
        }
        return null;
    }

    /** 
	 * Adds a socket to a given pool for the given host.
	 * THIS METHOD IS NOT THREADSAFE, SO BE CAREFUL WHEN USING!
	 *
	 * Internal utility method. 
	 * 
	 * @param pool pool to add to
	 * @param host host this socket is connected to
	 * @param socket socket to add
	 */
    protected void addSocketToPool(Map<String, Map<SockIO, Long>> pool, String host, SockIO socket) {
        if (pool.containsKey(host)) {
            Map<SockIO, Long> sockets = pool.get(host);
            if (sockets != null) {
                sockets.put(socket, new Long(System.currentTimeMillis()));
                return;
            }
        }
        Map<SockIO, Long> sockets = new HashMap<SockIO, Long>();
        sockets.put(socket, new Long(System.currentTimeMillis()));
        pool.put(host, sockets);
    }

    /** 
	 * Removes a socket from specified pool for host.
	 * THIS METHOD IS NOT THREADSAFE, SO BE CAREFUL WHEN USING!
	 *
	 * Internal utility method. 
	 * 
	 * @param pool pool to remove from
	 * @param host host pool
	 * @param socket socket to remove
	 */
    protected void removeSocketFromPool(Map<String, Map<SockIO, Long>> pool, String host, SockIO socket) {
        if (pool.containsKey(host)) {
            Map<SockIO, Long> sockets = pool.get(host);
            if (sockets != null) sockets.remove(socket);
        }
    }

    /** 
	 * Closes and removes all sockets from specified pool for host. 
	 * THIS METHOD IS NOT THREADSAFE, SO BE CAREFUL WHEN USING!
	 * 
	 * Internal utility method. 
	 *
	 * @param pool pool to clear
	 * @param host host to clear
	 */
    protected void clearHostFromPool(Map<String, Map<SockIO, Long>> pool, String host) {
        if (pool.containsKey(host)) {
            Map<SockIO, Long> sockets = pool.get(host);
            if (sockets != null && sockets.size() > 0) {
                for (Iterator<SockIO> i = sockets.keySet().iterator(); i.hasNext(); ) {
                    SockIO socket = i.next();
                    try {
                        socket.trueClose();
                    } catch (IOException ioe) {
                        log.error("++++ failed to close socket: " + ioe.getMessage());
                    }
                    i.remove();
                    socket = null;
                }
            }
        }
    }

    /** 
	 * Checks a SockIO object in with the pool.
	 *
	 * This will remove SocketIO from busy pool, and optionally<br/>
	 * add to avail pool.
	 *
	 * @param socket socket to return
	 * @param addToAvail add to avail pool if true
	 */
    public void checkIn(SockIO socket, boolean addToAvail) {
        String host = socket.getHost();
        log.debug("++++ calling check-in on socket: " + socket.toString() + " for host: " + host);
        synchronized (this) {
            log.debug("++++ removing socket (" + socket.toString() + ") from busy pool for host: " + host);
            removeSocketFromPool(busyPool, host, socket);
            if (addToAvail && socket.isConnected()) {
                log.debug("++++ returning socket (" + socket.toString() + " to avail pool for host: " + host);
                addSocketToPool(availPool, host, socket);
            }
        }
    }

    /** 
	 * Returns a socket to the avail pool.
	 *
	 * This is called from SockIO.close().  Calling this method<br/>
	 * directly without closing the SockIO object first<br/>
	 * will cause an IOException to be thrown.
	 * 
	 * @param socket socket to return
	 */
    public void checkIn(SockIO socket) {
        checkIn(socket, true);
    }

    /** 
	 * Closes all sockets in the passed in pool.
	 *
	 * Internal utility method. 
	 * 
	 * @param pool pool to close
	 */
    protected void closePool(Map<String, Map<SockIO, Long>> pool) {
        for (Iterator<String> i = pool.keySet().iterator(); i.hasNext(); ) {
            String host = i.next();
            Map<SockIO, Long> sockets = pool.get(host);
            for (Iterator<SockIO> j = sockets.keySet().iterator(); j.hasNext(); ) {
                SockIO socket = j.next();
                try {
                    socket.trueClose();
                } catch (IOException ioe) {
                    log.error("++++ failed to trueClose socket: " + socket.toString() + " for host: " + host);
                }
                j.remove();
                socket = null;
            }
        }
    }

    /** 
	 * Shuts down the pool.
	 *
	 * Cleanly closes all sockets.<br/>
	 * Stops the maint thread.<br/>
	 * Nulls out all internal maps<br/>
	 */
    public void shutDown() {
        synchronized (this) {
            log.debug("++++ SockIOPool shutting down...");
            if (maintThread != null && maintThread.isRunning()) stopMaintThread();
            log.debug("++++ closing all internal pools.");
            closePool(availPool);
            closePool(busyPool);
            availPool = null;
            busyPool = null;
            buckets = null;
            hostDeadDur = null;
            hostDead = null;
            initialized = false;
            log.debug("++++ SockIOPool finished shutting down.");
        }
    }

    /** 
	 * Starts the maintenance thread.
	 *
	 * This thread will manage the size of the active pool<br/>
	 * as well as move any closed, but not checked in sockets<br/>
	 * back to the available pool.
	 */
    protected void startMaintThread() {
        if (maintThread != null) {
            if (maintThread.isRunning()) {
                log.error("main thread already running");
            } else {
                maintThread.start();
            }
        } else {
            maintThread = new MaintThread(this);
            maintThread.setInterval(this.maintSleep);
            maintThread.start();
        }
    }

    /** 
	 * Stops the maintenance thread.
	 */
    protected void stopMaintThread() {
        if (maintThread != null && maintThread.isRunning()) maintThread.stopThread();
    }

    /** 
	 * Runs self maintenance on all internal pools.
	 *
	 * This is typically called by the maintenance thread to manage pool size. 
	 */
    protected void selfMaint() {
        log.debug("++++ Starting self maintenance....");
        synchronized (this) {
            for (Iterator<String> i = availPool.keySet().iterator(); i.hasNext(); ) {
                String host = i.next();
                Map<SockIO, Long> sockets = availPool.get(host);
                log.debug("++++ Size of avail pool for host (" + host + ") = " + sockets.size());
                if (sockets.size() < minConn) {
                    int need = minConn - sockets.size();
                    log.debug("++++ Need to create " + need + " new sockets for pool for host: " + host);
                    for (int j = 0; j < need; j++) {
                        SockIO socket = createSocket(host);
                        if (socket == null) break;
                        addSocketToPool(availPool, host, socket);
                    }
                } else if (sockets.size() > maxConn) {
                    int diff = sockets.size() - maxConn;
                    int needToClose = (diff <= poolMultiplier) ? diff : (diff) / poolMultiplier;
                    log.debug("++++ need to remove " + needToClose + " spare sockets for pool for host: " + host);
                    for (Iterator<SockIO> j = sockets.keySet().iterator(); j.hasNext(); ) {
                        if (needToClose <= 0) break;
                        SockIO socket = j.next();
                        long expire = sockets.get(socket).longValue();
                        if ((expire + maxIdle) < System.currentTimeMillis()) {
                            log.debug("+++ removing stale entry from pool as it is past its idle timeout and pool is over max spare");
                            try {
                                socket.trueClose();
                            } catch (IOException ioe) {
                                log.error("failed to close socket");
                                log.error(ioe.getMessage(), ioe);
                            }
                            j.remove();
                            socket = null;
                            needToClose--;
                        }
                    }
                }
                createShift.put(host, new Integer(0));
            }
            for (Iterator<String> i = busyPool.keySet().iterator(); i.hasNext(); ) {
                String host = i.next();
                Map<SockIO, Long> sockets = busyPool.get(host);
                log.debug("++++ Size of busy pool for host (" + host + ")  = " + sockets.size());
                for (Iterator<SockIO> j = sockets.keySet().iterator(); j.hasNext(); ) {
                    SockIO socket = j.next();
                    long hungTime = sockets.get(socket).longValue();
                    if ((hungTime + maxBusyTime) < System.currentTimeMillis()) {
                        log.error("+++ removing potentially hung connection from busy pool ... socket in pool for " + (System.currentTimeMillis() - hungTime) + "ms");
                        try {
                            socket.trueClose();
                        } catch (IOException ioe) {
                            log.error("failed to close socket");
                            log.error(ioe.getMessage(), ioe);
                        }
                        j.remove();
                        socket = null;
                    }
                }
            }
        }
        log.debug("+++ ending self maintenance.");
    }

    /** 
	 * Class which extends thread and handles maintenance of the pool.
	 * 
	 * @author greg whalin <greg@meetup.com>
	 * @version 1.5
	 */
    protected static class MaintThread extends Thread {

        private SockIOPool pool;

        private long interval = 1000 * 3;

        private boolean stopThread = false;

        private boolean running;

        protected MaintThread(SockIOPool pool) {
            this.pool = pool;
            this.setDaemon(true);
        }

        public void setInterval(long interval) {
            this.interval = interval;
        }

        public boolean isRunning() {
            return this.running;
        }

        /** 
		 * sets stop variable
		 * and interupts any wait 
		 */
        public void stopThread() {
            this.stopThread = true;
            this.interrupt();
        }

        /** 
		 * Start the thread.
		 */
        public void run() {
            this.running = true;
            while (!this.stopThread) {
                try {
                    Thread.sleep(interval);
                    if (pool.isInitialized()) pool.selfMaint();
                } catch (Exception e) {
                    break;
                }
            }
            this.running = false;
        }
    }

    /** 
	 * MemCached Java client, utility class for Socket IO.
	 *
	 * This class is a wrapper around a Socket and its streams.
	 *
	 * @author greg whalin <greg@meetup.com> 
	 * @author Richard 'toast' Russo <russor@msoe.edu>
	 * @version 1.5
	 */
    public static class SockIO {

        private static Logger log = Logger.getLogger(SockIO.class.getName());

        private SockIOPool pool;

        private String host;

        private Socket sock;

        private DataInputStream in;

        private BufferedOutputStream out;

        /** 
		 * creates a new SockIO object wrapping a socket
		 * connection to host:port, and its input and output streams
		 * 
		 * @param pool Pool this object is tied to
		 * @param host host to connect to
		 * @param port port to connect to
		 * @param timeout int ms to block on data for read
		 * @param connectTimeout timeout (in ms) for initial connection
		 * @param noDelay TCP NODELAY option?
		 * @throws IOException if an io error occurrs when creating socket
		 * @throws UnknownHostException if hostname is invalid
		 */
        public SockIO(SockIOPool pool, String host, int port, int timeout, int connectTimeout, boolean noDelay) throws IOException, UnknownHostException {
            this.pool = pool;
            sock = getSocket(host, port, connectTimeout);
            if (timeout >= 0) sock.setSoTimeout(timeout);
            sock.setTcpNoDelay(noDelay);
            in = new DataInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());
            this.host = host + ":" + port;
        }

        /** 
		 * creates a new SockIO object wrapping a socket
		 * connection to host:port, and its input and output streams
		 * 
		 * @param host hostname:port
		 * @param timeout read timeout value for connected socket
		 * @param connectTimeout timeout for initial connections
		 * @param noDelay TCP NODELAY option?
		 * @throws IOException if an io error occurrs when creating socket
		 * @throws UnknownHostException if hostname is invalid
		 */
        public SockIO(SockIOPool pool, String host, int timeout, int connectTimeout, boolean noDelay) throws IOException, UnknownHostException {
            this.pool = pool;
            String[] ip = host.split(":");
            sock = getSocket(ip[0], Integer.parseInt(ip[1]), connectTimeout);
            if (timeout >= 0) sock.setSoTimeout(timeout);
            sock.setTcpNoDelay(noDelay);
            in = new DataInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());
            this.host = host;
        }

        /** 
		 * Method which spawns thread to get a connection and then enforces a timeout on the initial
		 * connection.
		 *
		 * This should be backed by a thread pool.  Any volunteers?
		 * 
		 * @param host host to establish connection to
		 * @param port port on that host
		 * @param timeout connection timeout in ms
		 * @return connected socket
		 * @throws IOException if errors connecting or if connection times out
		 */
        protected static Socket getSocket(String host, int port, int timeout) throws IOException {
            Socket sock = new Socket();
            sock.connect(new InetSocketAddress(host, port), timeout);
            return sock;
        }

        /** 
		 * returns the host this socket is connected to 
		 * 
		 * @return String representation of host (hostname:port)
		 */
        String getHost() {
            return this.host;
        }

        /** 
		 * closes socket and all streams connected to it 
		 *
		 * @throws IOException if fails to close streams or socket
		 */
        void trueClose() throws IOException {
            log.debug("++++ Closing socket for real: " + toString());
            boolean err = false;
            StringBuilder errMsg = new StringBuilder();
            if (in == null || out == null || sock == null) {
                err = true;
                errMsg.append("++++ socket or its streams already null in trueClose call");
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    log.error("++++ error closing input stream for socket: " + toString() + " for host: " + getHost());
                    log.error(ioe.getMessage(), ioe);
                    errMsg.append("++++ error closing input stream for socket: " + toString() + " for host: " + getHost() + "\n");
                    errMsg.append(ioe.getMessage());
                    err = true;
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ioe) {
                    log.error("++++ error closing output stream for socket: " + toString() + " for host: " + getHost());
                    log.error(ioe.getMessage(), ioe);
                    errMsg.append("++++ error closing output stream for socket: " + toString() + " for host: " + getHost() + "\n");
                    errMsg.append(ioe.getMessage());
                    err = true;
                }
            }
            if (sock != null) {
                try {
                    sock.close();
                } catch (IOException ioe) {
                    log.error("++++ error closing socket: " + toString() + " for host: " + getHost());
                    log.error(ioe.getMessage(), ioe);
                    errMsg.append("++++ error closing socket: " + toString() + " for host: " + getHost() + "\n");
                    errMsg.append(ioe.getMessage());
                    err = true;
                }
            }
            if (sock != null) pool.checkIn(this, false);
            in = null;
            out = null;
            sock = null;
            if (err) throw new IOException(errMsg.toString());
        }

        /** 
		 * sets closed flag and checks in to connection pool
		 * but does not close connections
		 */
        void close() {
            log.debug("++++ marking socket (" + this.toString() + ") as closed and available to return to avail pool");
            pool.checkIn(this);
        }

        /** 
		 * checks if the connection is open 
		 * 
		 * @return true if connected
		 */
        boolean isConnected() {
            return (sock != null && sock.isConnected());
        }

        boolean isAlive() {
            if (!isConnected()) return false;
            try {
                this.write("version\r\n".getBytes());
                this.flush();
                String response = this.readLine();
            } catch (IOException ex) {
                return false;
            }
            return true;
        }

        /** 
		 * reads a line
		 * intentionally not using the deprecated readLine method from DataInputStream 
		 * 
		 * @return String that was read in
		 * @throws IOException if io problems during read
		 */
        String readLine() throws IOException {
            if (sock == null || !sock.isConnected()) {
                log.error("++++ attempting to read from closed socket");
                throw new IOException("++++ attempting to read from closed socket");
            }
            byte[] b = new byte[1];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            boolean eol = false;
            while (in.read(b, 0, 1) != -1) {
                if (b[0] == 13) {
                    eol = true;
                } else {
                    if (eol) {
                        if (b[0] == 10) break;
                        eol = false;
                    }
                }
                bos.write(b, 0, 1);
            }
            if (bos == null || bos.size() <= 0) {
                throw new IOException("++++ Stream appears to be dead, so closing it down");
            }
            return bos.toString().trim();
        }

        /** 
		 * reads up to end of line and returns nothing 
		 * 
		 * @throws IOException if io problems during read
		 */
        void clearEOL() throws IOException {
            if (sock == null || !sock.isConnected()) {
                log.error("++++ attempting to read from closed socket");
                throw new IOException("++++ attempting to read from closed socket");
            }
            byte[] b = new byte[1];
            boolean eol = false;
            while (in.read(b, 0, 1) != -1) {
                if (b[0] == 13) {
                    eol = true;
                    continue;
                }
                if (eol) {
                    if (b[0] == 10) break;
                    eol = false;
                }
            }
        }

        /** 
		 * reads length bytes into the passed in byte array from dtream
		 * 
		 * @param b byte array
		 * @throws IOException if io problems during read
		 */
        void read(byte[] b) throws IOException {
            if (sock == null || !sock.isConnected()) {
                log.error("++++ attempting to read from closed socket");
                throw new IOException("++++ attempting to read from closed socket");
            }
            int count = 0;
            while (count < b.length) {
                int cnt = in.read(b, count, (b.length - count));
                count += cnt;
            }
        }

        /** 
		 * flushes output stream 
		 * 
		 * @throws IOException if io problems during read
		 */
        void flush() throws IOException {
            if (sock == null || !sock.isConnected()) {
                log.error("++++ attempting to write to closed socket");
                throw new IOException("++++ attempting to write to closed socket");
            }
            out.flush();
        }

        /** 
		 * writes a byte array to the output stream
		 * 
		 * @param b byte array to write
		 * @throws IOException if an io error happens
		 */
        void write(byte[] b) throws IOException {
            if (sock == null || !sock.isConnected()) {
                log.error("++++ attempting to write to closed socket");
                throw new IOException("++++ attempting to write to closed socket");
            }
            out.write(b);
        }

        /** 
		 * use the sockets hashcode for this object
		 * so we can key off of SockIOs 
		 * 
		 * @return int hashcode
		 */
        public int hashCode() {
            return (sock == null) ? 0 : sock.hashCode();
        }

        /** 
		 * returns the string representation of this socket 
		 * 
		 * @return string
		 */
        public String toString() {
            return (sock == null) ? "" : sock.toString();
        }
    }
}
