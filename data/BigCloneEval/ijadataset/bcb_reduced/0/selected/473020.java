package com.l2jserver.gameserver.pathfinding.geonodes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.l2jserver.Config;
import com.l2jserver.gameserver.GeoData;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.pathfinding.AbstractNode;
import com.l2jserver.gameserver.pathfinding.AbstractNodeLoc;
import com.l2jserver.gameserver.pathfinding.PathFinding;
import com.l2jserver.gameserver.pathfinding.utils.FastNodeList;
import javolution.util.FastList;
import javolution.util.FastMap;

/**
 *
 * @author -Nemesiss-
 */
public class GeoPathFinding extends PathFinding {

    private static Logger _log = Logger.getLogger(GeoPathFinding.class.getName());

    private static Map<Short, ByteBuffer> _pathNodes = new FastMap<Short, ByteBuffer>();

    private static Map<Short, IntBuffer> _pathNodesIndex = new FastMap<Short, IntBuffer>();

    public static GeoPathFinding getInstance() {
        return SingletonHolder._instance;
    }

    /**
	 * @see com.l2jserver.gameserver.pathfinding.PathFinding#PathNodesExist(short)
	 */
    @Override
    public boolean pathNodesExist(short regionoffset) {
        return _pathNodesIndex.containsKey(regionoffset);
    }

    /**
	 * @see com.l2jserver.gameserver.pathfinding.PathFinding#FindPath(int, int, short, int, int, short)
	 */
    @Override
    public List<AbstractNodeLoc> findPath(int x, int y, int z, int tx, int ty, int tz, int instanceId, boolean playable) {
        int gx = (x - L2World.MAP_MIN_X) >> 4;
        int gy = (y - L2World.MAP_MIN_Y) >> 4;
        short gz = (short) z;
        int gtx = (tx - L2World.MAP_MIN_X) >> 4;
        int gty = (ty - L2World.MAP_MIN_Y) >> 4;
        short gtz = (short) tz;
        GeoNode start = readNode(gx, gy, gz);
        GeoNode end = readNode(gtx, gty, gtz);
        if (start == null || end == null) return null;
        if (Math.abs(start.getLoc().getZ() - z) > 55) return null;
        if (Math.abs(end.getLoc().getZ() - tz) > 55) return null;
        if (start == end) return null;
        Location temp = GeoData.getInstance().moveCheck(x, y, z, start.getLoc().getX(), start.getLoc().getY(), start.getLoc().getZ(), instanceId);
        if ((temp.getX() != start.getLoc().getX()) || (temp.getY() != start.getLoc().getY())) return null;
        temp = GeoData.getInstance().moveCheck(tx, ty, tz, end.getLoc().getX(), end.getLoc().getY(), end.getLoc().getZ(), instanceId);
        if ((temp.getX() != end.getLoc().getX()) || (temp.getY() != end.getLoc().getY())) return null;
        return searchByClosest2(start, end);
    }

    public List<AbstractNodeLoc> searchByClosest2(GeoNode start, GeoNode end) {
        FastNodeList visited = new FastNodeList(550);
        LinkedList<GeoNode> to_visit = new LinkedList<GeoNode>();
        to_visit.add(start);
        int targetX = end.getLoc().getNodeX();
        int targetY = end.getLoc().getNodeY();
        int dx, dy;
        boolean added;
        int i = 0;
        while (i < 550) {
            GeoNode node;
            try {
                node = to_visit.removeFirst();
            } catch (Exception e) {
                return null;
            }
            if (node.equals(end)) return constructPath2(node); else {
                i++;
                visited.add(node);
                node.attachNeighbors();
                GeoNode[] neighbors = node.getNeighbors();
                if (neighbors == null) continue;
                for (GeoNode n : neighbors) {
                    if (!visited.containsRev(n) && !to_visit.contains(n)) {
                        added = false;
                        n.setParent(node);
                        dx = targetX - n.getLoc().getNodeX();
                        dy = targetY - n.getLoc().getNodeY();
                        n.setCost(dx * dx + dy * dy);
                        for (int index = 0; index < to_visit.size(); index++) {
                            if (to_visit.get(index).getCost() > n.getCost()) {
                                to_visit.add(index, n);
                                added = true;
                                break;
                            }
                        }
                        if (!added) to_visit.addLast(n);
                    }
                }
            }
        }
        return null;
    }

    public List<AbstractNodeLoc> constructPath2(AbstractNode node) {
        LinkedList<AbstractNodeLoc> path = new LinkedList<AbstractNodeLoc>();
        int previousDirectionX = -1000;
        int previousDirectionY = -1000;
        int directionX;
        int directionY;
        while (node.getParent() != null) {
            directionX = node.getLoc().getNodeX() - node.getParent().getLoc().getNodeX();
            directionY = node.getLoc().getNodeY() - node.getParent().getLoc().getNodeY();
            if (directionX != previousDirectionX || directionY != previousDirectionY) {
                previousDirectionX = directionX;
                previousDirectionY = directionY;
                path.addFirst(node.getLoc());
            }
            node = node.getParent();
        }
        return path;
    }

    public GeoNode[] readNeighbors(GeoNode n, int idx) {
        int node_x = n.getLoc().getNodeX();
        int node_y = n.getLoc().getNodeY();
        short regoffset = getRegionOffset(getRegionX(node_x), getRegionY(node_y));
        ByteBuffer pn = _pathNodes.get(regoffset);
        List<AbstractNode> Neighbors = new FastList<AbstractNode>(8);
        GeoNode newNode;
        short new_node_x, new_node_y;
        byte neighbor = pn.get(idx++);
        if (neighbor > 0) {
            neighbor--;
            new_node_x = (short) node_x;
            new_node_y = (short) (node_y - 1);
            newNode = readNode(new_node_x, new_node_y, neighbor);
            if (newNode != null) Neighbors.add(newNode);
        }
        neighbor = pn.get(idx++);
        if (neighbor > 0) {
            neighbor--;
            new_node_x = (short) (node_x + 1);
            new_node_y = (short) (node_y - 1);
            newNode = readNode(new_node_x, new_node_y, neighbor);
            if (newNode != null) Neighbors.add(newNode);
        }
        neighbor = pn.get(idx++);
        if (neighbor > 0) {
            neighbor--;
            new_node_x = (short) (node_x + 1);
            new_node_y = (short) node_y;
            newNode = readNode(new_node_x, new_node_y, neighbor);
            if (newNode != null) Neighbors.add(newNode);
        }
        neighbor = pn.get(idx++);
        if (neighbor > 0) {
            neighbor--;
            new_node_x = (short) (node_x + 1);
            new_node_y = (short) (node_y + 1);
            newNode = readNode(new_node_x, new_node_y, neighbor);
            if (newNode != null) Neighbors.add(newNode);
        }
        neighbor = pn.get(idx++);
        if (neighbor > 0) {
            neighbor--;
            new_node_x = (short) node_x;
            new_node_y = (short) (node_y + 1);
            newNode = readNode(new_node_x, new_node_y, neighbor);
            if (newNode != null) Neighbors.add(newNode);
        }
        neighbor = pn.get(idx++);
        if (neighbor > 0) {
            neighbor--;
            new_node_x = (short) (node_x - 1);
            new_node_y = (short) (node_y + 1);
            newNode = readNode(new_node_x, new_node_y, neighbor);
            if (newNode != null) Neighbors.add(newNode);
        }
        neighbor = pn.get(idx++);
        if (neighbor > 0) {
            neighbor--;
            new_node_x = (short) (node_x - 1);
            new_node_y = (short) node_y;
            newNode = readNode(new_node_x, new_node_y, neighbor);
            if (newNode != null) Neighbors.add(newNode);
        }
        neighbor = pn.get(idx++);
        if (neighbor > 0) {
            neighbor--;
            new_node_x = (short) (node_x - 1);
            new_node_y = (short) (node_y - 1);
            newNode = readNode(new_node_x, new_node_y, neighbor);
            if (newNode != null) Neighbors.add(newNode);
        }
        GeoNode[] result = new GeoNode[Neighbors.size()];
        return Neighbors.toArray(result);
    }

    private GeoNode readNode(short node_x, short node_y, byte layer) {
        short regoffset = getRegionOffset(getRegionX(node_x), getRegionY(node_y));
        if (!this.pathNodesExist(regoffset)) return null;
        short nbx = getNodeBlock(node_x);
        short nby = getNodeBlock(node_y);
        int idx = _pathNodesIndex.get(regoffset).get((nby << 8) + nbx);
        ByteBuffer pn = _pathNodes.get(regoffset);
        byte nodes = pn.get(idx);
        idx += layer * 10 + 1;
        if (nodes < layer) {
            _log.warning("SmthWrong!");
        }
        short node_z = pn.getShort(idx);
        idx += 2;
        return new GeoNode(new GeoNodeLoc(node_x, node_y, node_z), idx);
    }

    private GeoNode readNode(int gx, int gy, short z) {
        short node_x = getNodePos(gx);
        short node_y = getNodePos(gy);
        short regoffset = getRegionOffset(getRegionX(node_x), getRegionY(node_y));
        if (!this.pathNodesExist(regoffset)) return null;
        short nbx = getNodeBlock(node_x);
        short nby = getNodeBlock(node_y);
        int idx = _pathNodesIndex.get(regoffset).get((nby << 8) + nbx);
        ByteBuffer pn = _pathNodes.get(regoffset);
        byte nodes = pn.get(idx++);
        int idx2 = 0;
        short last_z = Short.MIN_VALUE;
        while (nodes > 0) {
            short node_z = pn.getShort(idx);
            if (Math.abs(last_z - z) > Math.abs(node_z - z)) {
                last_z = node_z;
                idx2 = idx + 2;
            }
            idx += 10;
            nodes--;
        }
        return new GeoNode(new GeoNodeLoc(node_x, node_y, last_z), idx2);
    }

    private GeoPathFinding() {
        LineNumberReader lnr = null;
        try {
            _log.info("PathFinding Engine: - Loading Path Nodes...");
            File Data = new File("./data/pathnode/pn_index.txt");
            if (!Data.exists()) return;
            lnr = new LineNumberReader(new BufferedReader(new FileReader(Data)));
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error("Failed to Load pn_index File.");
        }
        String line;
        try {
            while ((line = lnr.readLine()) != null) {
                if (line.trim().length() == 0) continue;
                StringTokenizer st = new StringTokenizer(line, "_");
                byte rx = Byte.parseByte(st.nextToken());
                byte ry = Byte.parseByte(st.nextToken());
                LoadPathNodeFile(rx, ry);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error("Failed to Read pn_index File.");
        } finally {
            try {
                lnr.close();
            } catch (Exception e) {
            }
        }
    }

    private void LoadPathNodeFile(byte rx, byte ry) {
        if (rx < Config.WORLD_X_MIN || rx > Config.WORLD_X_MAX || ry < Config.WORLD_Y_MIN || ry > Config.WORLD_Y_MAX) {
            _log.warning("Failed to Load PathNode File: invalid region " + rx + "," + ry + "\n");
            return;
        }
        String fname = "./data/pathnode/" + rx + "_" + ry + ".pn";
        short regionoffset = getRegionOffset(rx, ry);
        _log.info("PathFinding Engine: - Loading: " + fname + " -> region offset: " + regionoffset + "X: " + rx + " Y: " + ry);
        File Pn = new File(fname);
        int node = 0, size, index = 0;
        FileChannel roChannel = null;
        try {
            roChannel = new RandomAccessFile(Pn, "r").getChannel();
            size = (int) roChannel.size();
            MappedByteBuffer nodes;
            if (Config.FORCE_GEODATA) nodes = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size).load(); else nodes = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
            IntBuffer indexs = IntBuffer.allocate(65536);
            while (node < 65536) {
                byte layer = nodes.get(index);
                indexs.put(node++, index);
                index += layer * 10 + 1;
            }
            _pathNodesIndex.put(regionoffset, indexs);
            _pathNodes.put(regionoffset, nodes);
        } catch (Exception e) {
            _log.log(Level.WARNING, "Failed to Load PathNode File: " + fname + " : " + e.getMessage(), e);
        } finally {
            try {
                roChannel.close();
            } catch (Exception e) {
            }
        }
    }

    @SuppressWarnings("synthetic-access")
    private static class SingletonHolder {

        protected static final GeoPathFinding _instance = new GeoPathFinding();
    }
}
