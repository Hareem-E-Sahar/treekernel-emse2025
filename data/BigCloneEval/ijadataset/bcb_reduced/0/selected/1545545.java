package org.jcrpg.threed.scene;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import org.jcrpg.apps.Jcrpg;
import org.jcrpg.space.Cube;
import org.jcrpg.threed.J3DCore;
import org.jcrpg.threed.engine.RenderedCubePool;
import org.jcrpg.world.place.Boundaries;
import org.jcrpg.world.place.World;
import org.jcrpg.world.time.Time;

public class RenderedArea {

    public HashMap<Long, RenderedCube> worldCubeCacheThreadSafeCopy = new HashMap<Long, RenderedCube>();

    private HashMap<Long, RenderedCube> worldCubeCache = new HashMap<Long, RenderedCube>();

    private HashMap<Long, RenderedCube> worldCubeCacheNext = new HashMap<Long, RenderedCube>();

    public HashMap<Long, RenderedCube> worldCubeCache_FARVIEW = new HashMap<Long, RenderedCube>();

    public HashMap<Long, RenderedCube> worldCubeCacheNext_FARVIEW = new HashMap<Long, RenderedCube>();

    int renderDistance, renderDistanceFarview;

    public boolean isInProcess = false;

    public int numberOfProcesses = 0;

    public boolean haltCurrentProcess = false;

    public RenderedArea() {
    }

    public RenderedArea(int renderDistance, int renderDistanceFarview) {
        this.renderDistance = renderDistance;
        this.renderDistanceFarview = renderDistanceFarview;
    }

    public World lastRenderedWorld = null;

    public int lastX = -99999999;

    public int lastY = -99999999;

    public int lastZ = -99999999;

    public static int[][][] ALL_COORDINATES = new int[0][][];

    public class Period {

        public int min, max;

        public Period(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public boolean isInside(int value) {
            if (min <= value && max >= value) return true;
            return false;
        }

        public boolean overlaps(Period p) {
            return isInside(p.min) || isInside(p.max);
        }

        public boolean isParameterGreater(Period p) {
            if (p.min > min) return true;
            return false;
        }
    }

    boolean CUBE_POOLING = true;

    public int[][][] getNeededCoordinateIntervals(World world, int x, int y, int z, boolean old) {
        if ((lastX == -99999999) && old) {
            return null;
        }
        int newXMin = x - renderDistance;
        int newXMax = x + renderDistance;
        Period pNewX = new Period(newXMin, newXMax);
        int newYMin = y - Math.min(renderDistance, 15);
        int newYMax = y + Math.min(renderDistance, 15);
        Period pNewY = new Period(newYMin, newYMax);
        int newZMin = z - renderDistance;
        int newZMax = z + renderDistance;
        Period pNewZ = new Period(newZMin, newZMax);
        if ((lastX == -99999999 || world != lastRenderedWorld || lastRenderedWorld == null) && !old) {
            return new int[][][] { { { newXMin, newXMax }, { newYMin, newYMax }, { newZMin, newZMax } } };
        }
        int oldXMin = lastX - renderDistance;
        int oldXMax = lastX + renderDistance;
        Period pOldX = new Period(oldXMin, oldXMax);
        int oldYMin = lastY - Math.min(renderDistance, 15);
        int oldYMax = lastY + Math.min(renderDistance, 15);
        Period pOldY = new Period(oldYMin, oldYMax);
        int oldZMin = lastZ - renderDistance;
        int oldZMax = lastZ + renderDistance;
        Period pOldZ = new Period(oldZMin, oldZMax);
        if ((lastRenderedWorld == null || world != lastRenderedWorld) && old) {
            return new int[][][] { { { oldXMin, oldXMax }, { oldYMin, oldYMax }, { oldZMin, oldZMax } } };
        }
        int midX, midY, midZ;
        boolean XGreater, YGreater, ZGreater;
        if (old) {
            XGreater = pOldX.isParameterGreater(pNewX);
            if (XGreater) midX = pNewX.min; else midX = pNewX.max;
            YGreater = pOldY.isParameterGreater(pNewY);
            if (YGreater) midY = pNewY.min; else midY = pNewY.max;
            ZGreater = pOldZ.isParameterGreater(pNewZ);
            if (ZGreater) midZ = pNewZ.min; else midZ = pNewZ.max;
        } else {
            XGreater = pNewX.isParameterGreater(pOldX);
            if (XGreater) midX = pOldX.min; else midX = pOldX.max;
            YGreater = pNewY.isParameterGreater(pOldY);
            if (YGreater) midY = pOldY.min; else midY = pOldY.max;
            ZGreater = pNewZ.isParameterGreater(pOldZ);
            if (ZGreater) midZ = pOldZ.min; else midZ = pOldZ.max;
        }
        if (!pOldX.overlaps(pNewX) || !pOldY.overlaps(pNewY) || !pOldZ.overlaps(pNewZ)) {
            if (old) return new int[][][] { { { oldXMin, oldXMax }, { oldYMin, oldYMax }, { oldZMin, oldZMax } } };
            return new int[][][] { { { newXMin, newXMax }, { newYMin, newYMax }, { newZMin, newZMax } } };
        }
        if (old) {
            int[][][] returnArray = new int[7][][];
            int counter = 0;
            if (XGreater || YGreater || ZGreater) {
                int[][] limiters = new int[][] { { oldXMin, midX }, { oldYMin, midY }, { oldZMin, midZ } };
                returnArray[counter++] = limiters;
            }
            if (!XGreater || YGreater || ZGreater) {
                int[][] limiters = new int[][] { { midX + 1, oldXMax }, { oldYMin, midY }, { oldZMin, midZ } };
                returnArray[counter++] = limiters;
            }
            if (XGreater || !YGreater || ZGreater) {
                int[][] limiters = new int[][] { { oldXMin, midX }, { midY + 1, oldYMax }, { oldZMin, midZ } };
                returnArray[counter++] = limiters;
            }
            if (XGreater || YGreater || !ZGreater) {
                int[][] limiters = new int[][] { { oldXMin, midX }, { oldYMin, midY }, { midZ + 1, oldZMax } };
                returnArray[counter++] = limiters;
            }
            if (!XGreater || !YGreater || ZGreater) {
                int[][] limiters = new int[][] { { midX + 1, oldXMax }, { midY + 1, oldYMax }, { oldZMin, midZ } };
                returnArray[counter++] = limiters;
            }
            if (!XGreater || YGreater || !ZGreater) {
                int[][] limiters = new int[][] { { midX + 1, oldXMax }, { oldYMin, midY }, { midZ + 1, oldZMax } };
                returnArray[counter++] = limiters;
            }
            if (XGreater || !YGreater || !ZGreater) {
                int[][] limiters = new int[][] { { oldXMin, midX }, { midY + 1, oldYMax }, { midZ + 1, oldZMax } };
                returnArray[counter++] = limiters;
            }
            if (!XGreater || !YGreater || !ZGreater) {
                int[][] limiters = new int[][] { { midX + 1, oldXMax }, { midY + 1, oldYMax }, { midZ + 1, oldZMax } };
                returnArray[counter++] = limiters;
            }
            return returnArray;
        } else {
            int[][][] returnArray = new int[7][][];
            int counter = 0;
            if (XGreater || YGreater || ZGreater) {
                int[][] limiters = new int[][] { { newXMin, midX }, { newYMin, midY }, { newZMin, midZ } };
                returnArray[counter++] = limiters;
            }
            if (!XGreater || YGreater || ZGreater) {
                int[][] limiters = new int[][] { { midX + 1, newXMax }, { newYMin, midY }, { newZMin, midZ } };
                returnArray[counter++] = limiters;
            }
            if (XGreater || !YGreater || ZGreater) {
                int[][] limiters = new int[][] { { newXMin, midX }, { midY + 1, newYMax }, { newZMin, midZ } };
                returnArray[counter++] = limiters;
            }
            if (XGreater || YGreater || !ZGreater) {
                int[][] limiters = new int[][] { { newXMin, midX }, { newYMin, midY }, { midZ + 1, newZMax } };
                returnArray[counter++] = limiters;
            }
            if (!XGreater || !YGreater || ZGreater) {
                int[][] limiters = new int[][] { { midX + 1, newXMax }, { midY + 1, newYMax }, { newZMin, midZ } };
                returnArray[counter++] = limiters;
            }
            if (!XGreater || YGreater || !ZGreater) {
                int[][] limiters = new int[][] { { midX + 1, newXMax }, { newYMin, midY }, { midZ + 1, newZMax } };
                returnArray[counter++] = limiters;
            }
            if (XGreater || !YGreater || !ZGreater) {
                int[][] limiters = new int[][] { { newXMin, midX }, { midY + 1, newYMax }, { midZ + 1, newZMax } };
                returnArray[counter++] = limiters;
            }
            if (!XGreater || !YGreater || !ZGreater) {
                int[][] limiters = new int[][] { { midX + 1, newXMax }, { midY + 1, newYMax }, { midZ + 1, newZMax } };
                returnArray[counter++] = limiters;
            }
            return returnArray;
        }
    }

    public ArrayList<int[][]> joinLimiters(int[][][] limiterList) {
        ArrayList<int[][]> joinedLimiters = new ArrayList<int[][]>();
        for (int count = 0; count < limiterList.length; count++) {
            int[][] limiters = limiterList[count];
            if (limiters[1][0] > limiters[1][1]) continue;
            boolean add = true;
            for (int[][] checker : joinedLimiters) {
                if (checker[1][0] == limiters[1][1] + 1) {
                    if (checker[0][0] == limiters[0][0] && checker[0][1] == limiters[0][1] && checker[2][0] == limiters[2][0] && checker[2][1] == limiters[2][1]) {
                        add = false;
                        checker[1][0] = limiters[1][0];
                        break;
                    }
                }
                if (checker[1][1] + 1 == limiters[1][0]) {
                    if (checker[0][0] == limiters[0][0] && checker[0][1] == limiters[0][1] && checker[2][0] == limiters[2][0] && checker[2][1] == limiters[2][1]) {
                        add = false;
                        checker[1][1] = limiters[1][1];
                        break;
                    }
                }
            }
            if (add) {
                joinedLimiters.add(limiters);
            }
        }
        return joinedLimiters;
    }

    boolean USE_ZONES = true;

    boolean USE_ZONES_REMOVE = false;

    private Object mutex = new Object();

    @SuppressWarnings("unchecked")
    public RenderedCube[][] getRenderedSpace(World world, int x, int y, int z, int direction, boolean farViewEnabled, boolean rerender) {
        synchronized (mutex) {
            isInProcess = true;
            numberOfProcesses++;
            Time wtime = world.engine.getWorldMeanTime();
            world.perf_eco_t0 = 0;
            world.perf_climate_t0 = 0;
            world.perf_flora_t0 = 0;
            world.perf_geo_t0 = 0;
            world.perf_surface_t0 = 0;
            world.perf_water_t0 = 0;
            long time1 = System.currentTimeMillis();
            if (J3DCore.LOGGING()) Jcrpg.LOGGER.finer("WCC START : " + worldCubeCache.size());
            if (rerender) lastRenderedWorld = null;
            int[][][] toRemoveCoordinates = getNeededCoordinateIntervals(world, x, y, z, true);
            int[][][] toAddCoordinates = getNeededCoordinateIntervals(world, x, y, z, false);
            RenderedCube[] toAdd = new RenderedCube[0];
            RenderedCube[] toRemove = new RenderedCube[0];
            int worldXS, worldZS;
            if (J3DCore.SETTINGS.CONTINUOUS_LOAD) {
                {
                    long time = System.currentTimeMillis();
                    try {
                        worldCubeCacheThreadSafeCopy = (HashMap<Long, RenderedCube>) worldCubeCache.clone();
                    } catch (ConcurrentModificationException cme) {
                        Jcrpg.LOGGER.severe("RenderedArea #### CONCURRENCY !! #### " + cme);
                        isInProcess = false;
                        haltCurrentProcess = false;
                        numberOfProcesses--;
                        return null;
                    }
                    if (J3DCore.LOGGING()) Jcrpg.LOGGER.finer("clone time: " + (System.currentTimeMillis() - time));
                }
            }
            if (toRemoveCoordinates != null) {
                if (toRemoveCoordinates == ALL_COORDINATES) {
                    if (J3DCore.LOGGING()) Jcrpg.LOGGER.finer("REMOVING ALL COORDINATES");
                    toRemove = worldCubeCache.values().toArray(new RenderedCube[0]);
                    worldCubeCache.clear();
                } else {
                    ArrayList<RenderedCube> removed = new ArrayList<RenderedCube>();
                    ArrayList<int[][]> joinedLimiters = joinLimiters(toRemoveCoordinates);
                    for (int count = 0; count < joinedLimiters.size(); count++) {
                        int[][] limiters = joinedLimiters.get(count);
                        if (limiters[1][0] > limiters[1][1]) continue;
                        for (int worldX = limiters[0][0]; worldX <= limiters[0][1]; worldX++) {
                            for (int worldZ = limiters[2][0]; worldZ <= limiters[2][1]; worldZ++) {
                                if (USE_ZONES_REMOVE) {
                                    int[][] zones = world.getFilledZonesOfY(worldX, worldZ, limiters[1][0], limiters[1][1]);
                                    if (zones != null) for (int[] zone : zones) {
                                        long key = 0;
                                        boolean getKey = true;
                                        for (int worldY = zone[0]; worldY <= zone[1]; worldY++) {
                                            if (haltCurrentProcess) {
                                                isInProcess = false;
                                                haltCurrentProcess = false;
                                                numberOfProcesses--;
                                                return null;
                                            }
                                            worldXS = world.shrinkToWorld(worldX);
                                            worldZS = world.shrinkToWorld(worldZ);
                                            if (worldY < 0 || worldXS != worldX || worldZS != worldZ) getKey = true;
                                            if (getKey) {
                                                key = Boundaries.getKey(worldXS, worldY, worldZS);
                                                getKey = false;
                                            } else {
                                                key++;
                                            }
                                            {
                                                RenderedCube c = worldCubeCache.remove(key);
                                                if (c != null) removed.add(c);
                                            }
                                        }
                                    }
                                } else {
                                    long key = 0;
                                    boolean getKey = true;
                                    for (int worldY = limiters[1][0]; worldY <= limiters[1][1]; worldY++) {
                                        if (haltCurrentProcess) {
                                            isInProcess = false;
                                            haltCurrentProcess = false;
                                            numberOfProcesses--;
                                            return null;
                                        }
                                        worldXS = world.shrinkToWorld(worldX);
                                        worldZS = world.shrinkToWorld(worldZ);
                                        if (worldY < 0 || worldXS != worldX || worldZS != worldZ) getKey = true;
                                        if (getKey) {
                                            key = Boundaries.getKey(worldXS, worldY, worldZS);
                                            getKey = false;
                                        } else {
                                            key++;
                                        }
                                        {
                                            RenderedCube c = worldCubeCache.remove(key);
                                            if (c != null) removed.add(c);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    toRemove = removed.toArray(new RenderedCube[0]);
                }
            }
            if (toAddCoordinates != null) {
                {
                    ArrayList<RenderedCube> added = new ArrayList<RenderedCube>();
                    RenderedCube c = null;
                    ArrayList<int[][]> joinedLimiters = joinLimiters(toAddCoordinates);
                    for (int count = 0; count < joinedLimiters.size(); count++) {
                        int[][] limiters = joinedLimiters.get(count);
                        if (limiters[1][0] > limiters[1][1]) continue;
                        for (int worldX = limiters[0][0]; worldX <= limiters[0][1]; worldX++) {
                            for (int worldZ = limiters[2][0]; worldZ <= limiters[2][1]; worldZ++) {
                                if (USE_ZONES) {
                                    int[][] zones = world.getFilledZonesOfY(worldX, worldZ, limiters[1][0], limiters[1][1]);
                                    if (zones != null) for (int[] zone : zones) {
                                        long key = 0;
                                        boolean getKey = true;
                                        for (int worldY = zone[0]; worldY <= zone[1]; worldY++) {
                                            if (haltCurrentProcess) {
                                                isInProcess = false;
                                                haltCurrentProcess = false;
                                                numberOfProcesses--;
                                                return null;
                                            }
                                            int wX = worldX;
                                            int wZ = worldZ;
                                            worldXS = world.shrinkToWorld(worldX);
                                            worldZS = world.shrinkToWorld(worldZ);
                                            if (worldY < 0 || worldXS != worldX || worldZS != worldZ) getKey = true;
                                            if (getKey) {
                                                key = Boundaries.getKey(worldXS, worldY, worldZS);
                                                getKey = false;
                                            } else {
                                                key++;
                                            }
                                            c = null;
                                            if (!worldCubeCache.containsKey(key)) {
                                                Cube cube = world.getCube(wtime, key, worldXS, worldY, worldZS, false);
                                                if (cube != null) {
                                                    c = RenderedCubePool.getInstance(cube, wX - x, worldY - y, z - wZ);
                                                    c.world = world;
                                                    added.add(c);
                                                }
                                                worldCubeCache.put(key, c);
                                            }
                                        }
                                    }
                                } else {
                                    long key = 0;
                                    boolean getKey = true;
                                    for (int worldY = limiters[1][0]; worldY <= limiters[1][1]; worldY++) {
                                        if (haltCurrentProcess) {
                                            isInProcess = false;
                                            haltCurrentProcess = false;
                                            numberOfProcesses--;
                                            return null;
                                        }
                                        int wX = worldX;
                                        int wZ = worldZ;
                                        worldXS = world.shrinkToWorld(worldX);
                                        worldZS = world.shrinkToWorld(worldZ);
                                        if (worldY < 0 || worldXS != worldX || worldZS != worldZ) getKey = true;
                                        if (getKey) {
                                            key = Boundaries.getKey(worldXS, worldY, worldZS);
                                            getKey = false;
                                        } else {
                                            key++;
                                        }
                                        c = null;
                                        Cube cube = world.getCube(wtime, key, worldXS, worldY, worldZS, false);
                                        if (cube != null) {
                                            c = RenderedCubePool.getInstance(cube, wX - x, worldY - y, z - wZ);
                                            c.world = world;
                                            added.add(c);
                                        }
                                        worldCubeCache.put(key, c);
                                    }
                                }
                            }
                        }
                    }
                    toAdd = added.toArray(new RenderedCube[0]);
                }
            }
            worldCubeCacheThreadSafeCopy = worldCubeCache;
            lastRenderedWorld = world;
            lastX = x;
            lastY = y;
            lastZ = z;
            if (J3DCore.LOGGING()) Jcrpg.LOGGER.finer("-- eco = " + world.perf_eco_t0);
            if (J3DCore.LOGGING()) Jcrpg.LOGGER.finer("-- geo = " + world.perf_geo_t0);
            if (J3DCore.LOGGING()) Jcrpg.LOGGER.finer("-- flo = " + world.perf_flora_t0);
            if (J3DCore.LOGGING()) Jcrpg.LOGGER.finer("-- cli = " + world.perf_climate_t0);
            if (J3DCore.LOGGING()) Jcrpg.LOGGER.finer("-- wat = " + world.perf_water_t0);
            if (J3DCore.LOGGING()) Jcrpg.LOGGER.finer("-- sur = " + world.perf_surface_t0);
            if (J3DCore.LOGGING()) Jcrpg.LOGGER.finer("WORLDCUBECACHE = " + worldCubeCache.size() + " ADD: " + toAdd.length + " REM: " + toRemove.length);
            if (J3DCore.LOGGING()) Jcrpg.LOGGER.finer("FULL TIME = " + (System.currentTimeMillis() - time1));
            isInProcess = false;
            numberOfProcesses--;
            return new RenderedCube[][] { toAdd, toRemove, new RenderedCube[0], new RenderedCube[0] };
        }
    }

    public RenderedCube getCubeAtPosition(World world, int worldX, int worldY, int worldZ) {
        worldX = world.shrinkToWorld(worldX);
        worldZ = world.shrinkToWorld(worldZ);
        long key = Boundaries.getKey(worldX, worldY, worldZ);
        synchronized (worldCubeCache) {
            return worldCubeCacheThreadSafeCopy.get(key);
        }
    }

    public RenderedCube getCubeAtPosition(World world, int worldX, int worldY, int worldZ, boolean farview) {
        worldX = world.shrinkToWorld(worldX);
        worldZ = world.shrinkToWorld(worldZ);
        long key = Boundaries.getKey(worldX, worldY, worldZ);
        if (farview) return worldCubeCache_FARVIEW.get(key);
        synchronized (worldCubeCache) {
            return worldCubeCacheThreadSafeCopy.get(key);
        }
    }

    /**
	 * big update is being done, clear out all cached cubes.
	 */
    public void fullUpdateClear() {
        worldCubeCache.clear();
        worldCubeCache_FARVIEW.clear();
        worldCubeCacheNext.clear();
        worldCubeCacheNext_FARVIEW.clear();
    }

    public void setRenderDistance(int renderDistance) {
        this.renderDistance = renderDistance;
    }

    public void setRenderDistanceFarview(int renderDistanceFarview) {
        this.renderDistanceFarview = renderDistanceFarview;
    }

    public synchronized void clear() {
        lastRenderedWorld = null;
        for (RenderedCube c : worldCubeCacheThreadSafeCopy.values()) {
            if (c != null) c.clear();
        }
        worldCubeCache.clear();
        worldCubeCache_FARVIEW.clear();
        worldCubeCacheNext.clear();
        worldCubeCacheNext_FARVIEW.clear();
        worldCubeCacheThreadSafeCopy.clear();
    }

    public String toString() {
        return "RenderedArea: ts " + worldCubeCacheThreadSafeCopy.size() + " / c " + worldCubeCache.size() + " / n " + worldCubeCacheNext.size();
    }
}
