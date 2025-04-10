package tac.tilestore.berkeleydb;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import tac.exceptions.TileStoreException;
import tac.tilestore.TileStore;
import tac.tilestore.TileStoreEntry;
import tac.tilestore.TileStoreInfo;
import tac.tilestore.berkeleydb.TileDbEntry.TileDbKey;
import tac.utilities.TACExceptionHandler;
import tac.utilities.Utilities;
import tac.utilities.file.DeleteFileFilter;
import tac.utilities.file.DirInfoFileFilter;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentLockedException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

/**
 * The new database based tile store implementation.
 */
public class BerkeleyDbTileStore extends TileStore {

    /**
	 * Max count of tile stores opened
	 */
    private static final int MAX_CONCURRENT_ENVIRONMENTS = 5;

    private EnvironmentConfig envConfig;

    private Map<String, TileDatabase> tileDbMap;

    private FileLock tileStoreLock = null;

    public BerkeleyDbTileStore() throws TileStoreException {
        super();
        acquireTileStoreLock();
        tileDbMap = new TreeMap<String, TileDatabase>();
        envConfig = new EnvironmentConfig();
        envConfig.setTransactional(false);
        envConfig.setLocking(true);
        envConfig.setExceptionListener(TACExceptionHandler.getInstance());
        envConfig.setAllowCreate(true);
        envConfig.setSharedCache(true);
        envConfig.setCachePercent(50);
    }

    protected void acquireTileStoreLock() throws TileStoreException {
        try {
            File file = new File(tileStoreDir, "lock");
            FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
            tileStoreLock = channel.tryLock();
            if (tileStoreLock == null) throw new TileStoreException("Unable to obtain tile store lock - " + "another instance of TrekBuddy Atlas Creator is running!");
        } catch (Exception e) {
            log.error("", e);
            throw new TileStoreException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public TileStoreEntry createNewEntry(int x, int y, int zoom, byte[] data, long timeLastModified, long timeExpires, String eTag) {
        return new TileDbEntry(x, y, zoom, data, timeLastModified, timeExpires, eTag);
    }

    private TileDatabase getTileDatabase(MapSource mapSource) throws DatabaseException {
        TileDatabase db;
        synchronized (tileDbMap) {
            db = tileDbMap.get(mapSource.getStoreName());
        }
        if (db != null) return db;
        try {
            synchronized (tileDbMap) {
                cleanupDatabases();
                db = tileDbMap.get(mapSource.getStoreName());
                if (db == null) {
                    db = new TileDatabase(mapSource);
                    db.lastAccess = System.currentTimeMillis();
                    tileDbMap.put(mapSource.getStoreName(), db);
                }
                return db;
            }
        } catch (Exception e) {
            log.error("Error creating tile store db \"" + mapSource.getStoreName() + "\"", e);
            throw new TileStoreException(e);
        }
    }

    @Override
    public TileStoreInfo getStoreInfo(MapSource mapSource) throws InterruptedException {
        int tileCount = getNrOfTiles(mapSource);
        long storeSize = getStoreSize(mapSource);
        return new TileStoreInfo(storeSize, tileCount);
    }

    @Override
    public void putTileData(byte[] tileData, int x, int y, int zoom, MapSource mapSource) throws IOException {
        this.putTileData(tileData, x, y, zoom, mapSource, -1, -1, null);
    }

    @Override
    public void putTileData(byte[] tileData, int x, int y, int zoom, MapSource mapSource, long timeLastModified, long timeExpires, String eTag) throws IOException {
        if (!mapSource.allowFileStore()) return;
        TileDbEntry tile = new TileDbEntry(x, y, zoom, tileData, timeLastModified, timeExpires, eTag);
        TileDatabase db = null;
        try {
            if (log.isTraceEnabled()) log.trace("Saved " + mapSource.getStoreName() + " " + tile);
            db = getTileDatabase(mapSource);
            db.put(tile);
        } catch (Exception e) {
            if (db != null) db.close();
            log.error("Faild to write tile to tile store \"" + mapSource.getStoreName() + "\"", e);
        }
    }

    @Override
    public void putTile(TileStoreEntry tile, MapSource mapSource) {
        if (!mapSource.allowFileStore()) return;
        TileDatabase db = null;
        try {
            if (log.isTraceEnabled()) log.trace("Saved " + mapSource.getStoreName() + " " + tile);
            db = getTileDatabase(mapSource);
            db.put((TileDbEntry) tile);
        } catch (Exception e) {
            if (db != null) db.close();
            log.error("Faild to write tile to tile store \"" + mapSource.getStoreName() + "\"", e);
        }
    }

    @Override
    public TileStoreEntry getTile(int x, int y, int zoom, MapSource mapSource) {
        if (!mapSource.allowFileStore()) return null;
        TileDatabase db = null;
        try {
            db = getTileDatabase(mapSource);
            TileStoreEntry tile = db.get(new TileDbKey(x, y, zoom));
            if (log.isTraceEnabled()) {
                if (tile == null) log.trace("Tile store cache miss: (x,y,z)" + x + "/" + y + "/" + zoom + " " + mapSource.getStoreName()); else log.trace("Loaded " + mapSource.getStoreName() + " " + tile);
            }
            return tile;
        } catch (Exception e) {
            if (db != null) db.close();
            log.error("failed to retrieve tile from tile store \"" + mapSource.getStoreName() + "\"", e);
            return null;
        }
    }

    public boolean contains(int x, int y, int zoom, MapSource mapSource) {
        try {
            return getTileDatabase(mapSource).contains(new TileDbKey(x, y, zoom));
        } catch (DatabaseException e) {
            log.error("", e);
            return false;
        }
    }

    public void prepareTileStore(MapSource mapSource) {
        if (!mapSource.allowFileStore()) return;
        try {
            getTileDatabase(mapSource);
        } catch (DatabaseException e) {
        }
    }

    public void clearStore(MapSource mapSource) {
        File databaseDir = getStoreDir(mapSource);
        TileDatabase db;
        synchronized (tileDbMap) {
            db = tileDbMap.get(mapSource.getStoreName());
            if (db != null) db.close(false);
            if (databaseDir.exists()) {
                DeleteFileFilter dff = new DeleteFileFilter();
                databaseDir.listFiles(dff);
                databaseDir.delete();
                log.debug("Tilestore " + mapSource.getStoreName() + " cleared: " + dff);
            }
            tileDbMap.remove(mapSource.getStoreName());
        }
    }

    /**
	 * This method returns the amount of tiles in the store of tiles which is
	 * specified by the {@link MapSource} object.
	 * 
	 * @param mapSource
	 *            the store to calculate number of tiles in
	 * @return the amount of tiles in the specified store.
	 * @throws InterruptedException
	 */
    public int getNrOfTiles(MapSource mapSource) throws InterruptedException {
        try {
            File storeDir = getStoreDir(mapSource);
            if (!storeDir.isDirectory()) return 0;
            TileDatabase db = getTileDatabase(mapSource);
            int tileCount = (int) db.entryCount();
            db.close();
            return tileCount;
        } catch (DatabaseException e) {
            log.error("", e);
            return -1;
        }
    }

    public long getStoreSize(MapSource mapSource) throws InterruptedException {
        File tileStore = getStoreDir(mapSource);
        if (tileStore.exists()) {
            DirInfoFileFilter diff = new DirInfoFileFilter();
            try {
                tileStore.listFiles(diff);
            } catch (RuntimeException e) {
                throw new InterruptedException();
            }
            return diff.getDirSize();
        } else {
            return 0;
        }
    }

    protected void cleanupDatabases() {
        if (tileDbMap.size() < MAX_CONCURRENT_ENVIRONMENTS) return;
        synchronized (tileDbMap) {
            List<TileDatabase> list = new ArrayList<TileDatabase>(tileDbMap.values());
            Collections.sort(list, new Comparator<TileDatabase>() {

                public int compare(TileDatabase o1, TileDatabase o2) {
                    if (o1.lastAccess == o2.lastAccess) return 0;
                    return (o1.lastAccess < o2.lastAccess) ? -1 : 1;
                }
            });
            for (int i = 0; i < list.size() - 2; i++) list.get(i).close();
        }
    }

    public void closeAll(final boolean shutdown) {
        Thread t = new DelayedInterruptThread("DBShutdown") {

            @Override
            public void run() {
                log.debug("Closing all tile databases...");
                synchronized (tileDbMap) {
                    for (TileDatabase db : tileDbMap.values()) {
                        db.close(false);
                    }
                    tileDbMap.clear();
                    if (shutdown) {
                        tileDbMap = null;
                        try {
                            tileStoreLock.release();
                        } catch (IOException e) {
                            log.error("", e);
                        }
                    }
                }
                log.debug("All tile databases has been closed");
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }

    /**
	 * Returns <code>true</code> if the tile store directory of the specified
	 * {@link MapSource} exists.
	 * 
	 * @param mapSource
	 * @return
	 */
    public boolean storeExists(MapSource mapSource) {
        File tileStore = getStoreDir(mapSource);
        return (tileStore.isDirectory()) && (tileStore.exists());
    }

    /**
	 * @param mapSource
	 * @return directory used for storing the tile database belonging to
	 *         <code>mapSource</code>
	 */
    protected File getStoreDir(MapSource mapSource) {
        return new File(tileStoreDir, "db-" + mapSource.getStoreName());
    }

    protected class TileDatabase {

        final MapSource mapSource;

        final Environment env;

        final EntityStore store;

        final PrimaryIndex<TileDbKey, TileDbEntry> tileIndex;

        boolean dbClosed = false;

        long lastAccess;

        public TileDatabase(MapSource mapSource) throws IOException, EnvironmentLockedException, DatabaseException {
            DelayedInterruptThread t = (DelayedInterruptThread) Thread.currentThread();
            try {
                t.pauseInterrupt();
                this.mapSource = mapSource;
                lastAccess = System.currentTimeMillis();
                File storeDir = getStoreDir(mapSource);
                Utilities.mkDirs(storeDir);
                env = new Environment(storeDir, envConfig);
                StoreConfig storeConfig = new StoreConfig();
                storeConfig.setAllowCreate(true);
                storeConfig.setTransactional(false);
                store = new EntityStore(env, "TilesEntityStore", storeConfig);
                tileIndex = store.getPrimaryIndex(TileDbKey.class, TileDbEntry.class);
            } finally {
                if (t.interruptedWhilePaused()) close();
                t.resumeInterrupt();
            }
            log.debug("Opened tile store db: \"" + mapSource.getStoreName() + "\"");
        }

        public boolean isClosed() {
            return dbClosed;
        }

        public long entryCount() throws DatabaseException {
            return tileIndex.count();
        }

        public void put(TileDbEntry tile) throws DatabaseException {
            DelayedInterruptThread t = (DelayedInterruptThread) Thread.currentThread();
            try {
                t.pauseInterrupt();
                tileIndex.put(tile);
            } finally {
                if (t.interruptedWhilePaused()) close();
                t.resumeInterrupt();
            }
        }

        public boolean contains(TileDbKey key) throws DatabaseException {
            return tileIndex.contains(key);
        }

        public TileDbEntry get(TileDbKey key) throws DatabaseException {
            return tileIndex.get(key);
        }

        protected void purge() {
            try {
                store.sync();
                env.cleanLog();
            } catch (DatabaseException e) {
                log.error("database compression failed: ", e);
            }
        }

        public void close() {
            close(true);
        }

        public void close(boolean removeFromMap) {
            if (dbClosed) return;
            if (removeFromMap) {
                synchronized (tileDbMap) {
                    TileDatabase db2 = tileDbMap.get(mapSource.getStoreName());
                    if (db2 == this) tileDbMap.remove(mapSource.getStoreName());
                }
            }
            DelayedInterruptThread t = (DelayedInterruptThread) Thread.currentThread();
            try {
                t.pauseInterrupt();
                try {
                    log.debug("Closing tile store db \"" + mapSource.getStoreName() + "\"");
                    if (store != null) store.close();
                } catch (Exception e) {
                    log.error("", e);
                }
                try {
                    env.close();
                } catch (Exception e) {
                    log.error("", e);
                } finally {
                    dbClosed = true;
                }
            } finally {
                if (t.interruptedWhilePaused()) close();
                t.resumeInterrupt();
            }
        }

        @Override
        protected void finalize() throws Throwable {
            close();
            super.finalize();
        }
    }
}
