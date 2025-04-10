package net.sf.l2j.gameserver.pathfinding.geonodes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.pathfinding.AbstractNodeLoc;
import net.sf.l2j.gameserver.pathfinding.Node;
import net.sf.l2j.gameserver.pathfinding.PathFinding;

/**
 * @author -Nemesiss-
 */
public class GeoPathFinding extends PathFinding {

    private static Logger _log = Logger.getLogger(GeoPathFinding.class.getName());

    private static GeoPathFinding _instance;

    private static Map<Short, ByteBuffer> _pathNodes = new FastMap<Short, ByteBuffer>();

    private static Map<Short, IntBuffer> _pathNodesIndex = new FastMap<Short, IntBuffer>();

    public static GeoPathFinding getInstance() {
        if (_instance == null) _instance = new GeoPathFinding();
        return _instance;
    }

    /**
	 * @see net.sf.l2j.gameserver.pathfinding.PathFinding#PathNodesExist(short)
	 */
    @Override
    public boolean pathNodesExist(short regionoffset) {
        return _pathNodesIndex.containsKey(regionoffset);
    }

    /**
	 * @see net.sf.l2j.gameserver.pathfinding.PathFinding#FindPath(int, int, short, int, int, short)
	 */
    @Override
    public List<AbstractNodeLoc> findPath(int gx, int gy, short z, int gtx, int gty, short tz) {
        Node start = readNode(gx, gy, z);
        Node end = readNode(gtx, gty, tz);
        if (start == null || end == null) return null;
        if (start == end) return null;
        return searchByClosest(start, end);
    }

    /**
	 * @see net.sf.l2j.gameserver.pathfinding.PathFinding#ReadNeighbors(short, short)
	 */
    @Override
    public Node[] readNeighbors(short node_x, short node_y, int idx) {
        short regoffset = getRegionOffset(getRegionX(node_x), getRegionY(node_y));
        ByteBuffer pn = _pathNodes.get(regoffset);
        List<Node> Neighbors = new FastList<Node>(8);
        Node newNode;
        short new_node_x, new_node_y;
        byte neighbor = pn.get(idx);
        idx++;
        if (neighbor > 0) {
            neighbor--;
            new_node_x = node_x;
            new_node_y = (short) (node_y - 1);
            newNode = readNode(new_node_x, new_node_y, neighbor);
            if (newNode != null) Neighbors.add(newNode);
        }
        neighbor = pn.get(idx);
        idx++;
        if (neighbor > 0) {
            neighbor--;
            new_node_x = (short) (node_x + 1);
            new_node_y = (short) (node_y - 1);
            newNode = readNode(new_node_x, new_node_y, neighbor);
            if (newNode != null) Neighbors.add(newNode);
        }
        neighbor = pn.get(idx);
        idx++;
        if (neighbor > 0) {
            neighbor--;
            new_node_x = (short) (node_x + 1);
            new_node_y = node_y;
            newNode = readNode(new_node_x, new_node_y, neighbor);
            if (newNode != null) Neighbors.add(newNode);
        }
        neighbor = pn.get(idx);
        idx++;
        if (neighbor > 0) {
            neighbor--;
            new_node_x = (short) (node_x + 1);
            new_node_y = (short) (node_y + 1);
            newNode = readNode(new_node_x, new_node_y, neighbor);
            if (newNode != null) Neighbors.add(newNode);
        }
        neighbor = pn.get(idx);
        idx++;
        if (neighbor > 0) {
            neighbor--;
            new_node_x = node_x;
            new_node_y = (short) (node_y + 1);
            newNode = readNode(new_node_x, new_node_y, neighbor);
            if (newNode != null) Neighbors.add(newNode);
        }
        neighbor = pn.get(idx);
        idx++;
        if (neighbor > 0) {
            neighbor--;
            new_node_x = (short) (node_x - 1);
            new_node_y = (short) (node_y + 1);
            newNode = readNode(new_node_x, new_node_y, neighbor);
            if (newNode != null) Neighbors.add(newNode);
        }
        neighbor = pn.get(idx);
        idx++;
        if (neighbor > 0) {
            neighbor--;
            new_node_x = (short) (node_x - 1);
            new_node_y = node_y;
            newNode = readNode(new_node_x, new_node_y, neighbor);
            if (newNode != null) Neighbors.add(newNode);
        }
        neighbor = pn.get(idx);
        idx++;
        if (neighbor > 0) {
            neighbor--;
            new_node_x = (short) (node_x - 1);
            new_node_y = (short) (node_y - 1);
            newNode = readNode(new_node_x, new_node_y, neighbor);
            if (newNode != null) Neighbors.add(newNode);
        }
        Node[] result = new Node[Neighbors.size()];
        return Neighbors.toArray(result);
    }

    private Node readNode(short node_x, short node_y, byte layer) {
        short regoffset = getRegionOffset(getRegionX(node_x), getRegionY(node_y));
        if (!pathNodesExist(regoffset)) return null;
        short nbx = getNodeBlock(node_x);
        short nby = getNodeBlock(node_y);
        int idx = _pathNodesIndex.get(regoffset).get((nby << 8) + nbx);
        ByteBuffer pn = _pathNodes.get(regoffset);
        byte nodes = pn.get(idx);
        idx += layer * 10 + 1;
        if (nodes < layer) _log.warning("SmthWrong!");
        short node_z = pn.getShort(idx);
        idx += 2;
        return new Node(new GeoNodeLoc(node_x, node_y, node_z), idx);
    }

    private Node readNode(int gx, int gy, short z) {
        short node_x = getNodePos(gx);
        short node_y = getNodePos(gy);
        short regoffset = getRegionOffset(getRegionX(node_x), getRegionY(node_y));
        if (!pathNodesExist(regoffset)) return null;
        short nbx = getNodeBlock(node_x);
        short nby = getNodeBlock(node_y);
        int idx = _pathNodesIndex.get(regoffset).get((nby << 8) + nbx);
        ByteBuffer pn = _pathNodes.get(regoffset);
        byte nodes = pn.get(idx);
        idx++;
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
        return new Node(new GeoNodeLoc(node_x, node_y, last_z), idx2);
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
        }
    }

    private void LoadPathNodeFile(byte rx, byte ry) {
        String fname = "./data/pathnode/" + rx + "_" + ry + ".pn";
        short regionoffset = getRegionOffset(rx, ry);
        _log.info("PathFinding Engine: - Loading: " + fname + " -> region offset: " + regionoffset + "X: " + rx + " Y: " + ry);
        File Pn = new File(fname);
        int node = 0, size, index = 0;
        try {
            FileChannel roChannel = new RandomAccessFile(Pn, "r").getChannel();
            size = (int) roChannel.size();
            MappedByteBuffer nodes;
            if (Config.FORCE_GEODATA) nodes = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size).load(); else nodes = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
            IntBuffer indexs = IntBuffer.allocate(65536);
            while (node < 65536) {
                byte layer = nodes.get(index);
                indexs.put(node, index);
                node++;
                index += layer * 10 + 1;
            }
            _pathNodesIndex.put(regionoffset, indexs);
            _pathNodes.put(regionoffset, nodes);
        } catch (Exception e) {
            e.printStackTrace();
            _log.warning("Failed to Load PathNode File: " + fname + "\n");
        }
    }
}
