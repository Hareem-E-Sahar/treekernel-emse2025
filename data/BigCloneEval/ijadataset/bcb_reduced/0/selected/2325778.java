package org.gvsig.graph.core.loaders;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import org.gvsig.graph.core.EdgePair;
import org.gvsig.graph.core.GvEdge;
import org.gvsig.graph.core.GvGraph;
import org.gvsig.graph.core.GvNode;
import org.gvsig.graph.core.IGraph;
import org.gvsig.graph.core.INetworkLoader;

/**
 * @author fjp
 * 
 * Useful when working with a network based on a jdbc layer (random access very slow).
 * The idea is to put in memory theGeom and every field that we may need later (street name,
 * length, cost, etc). 
 * 
 * Primero vienen los arcos, y luego los nodos. En la cabecera, 3 enteros
 * con el numero de tramos, el de arcos y el de nodos.
 * 
 * TODO: TODO
 *
 */
public class NetworkMemoryRedLoader implements INetworkLoader {

    private File netFile = new File("c:/ejes.red");

    public IGraph loadNetwork() {
        long t1 = System.currentTimeMillis();
        int numArcs;
        int numEdges;
        int numNodes;
        short sentidoDigit;
        RandomAccessFile file;
        try {
            file = new RandomAccessFile(netFile.getPath(), "r");
            FileChannel channel = file.getChannel();
            MappedByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            buf.order(ByteOrder.LITTLE_ENDIAN);
            numArcs = buf.getInt();
            numEdges = buf.getInt();
            numNodes = buf.getInt();
            GvGraph g = new GvGraph(numArcs, numEdges, numNodes);
            buf.position(36 * numEdges + 12);
            for (int i = 0; i < numNodes; i++) {
                GvNode node = readNode(buf);
                g.addNode(node);
            }
            buf.position(12);
            for (int i = 0; i < numEdges; i++) {
                GvEdge edge = readEdge(buf);
                edge.setIdEdge(i);
                g.addEdge(edge);
                GvNode nodeOrig = g.getNodeByID(edge.getIdNodeOrig());
                nodeOrig.addOutputLink(edge);
                GvNode nodeEnd = g.getNodeByID(edge.getIdNodeEnd());
                nodeEnd.addInputLink(edge);
                EdgePair edgePair = g.getEdgesByIdArc(edge.getIdArc());
                if (edgePair == null) {
                    edgePair = new EdgePair();
                    g.addEdgePair(edge.getIdArc(), edgePair);
                }
                if (edge.getDirec() == 1) edgePair.setIdEdge(i); else edgePair.setIdInverseEdge(i);
            }
            long t2 = System.currentTimeMillis();
            System.out.println("Tiempo de carga: " + (t2 - t1) + " msecs");
            System.out.println("NumEdges = " + g.numEdges());
            return g;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private GvNode readNode(MappedByteBuffer buf) {
        GvNode node = new GvNode();
        node.setIdNode(buf.getInt());
        node.setX(buf.getDouble());
        node.setY(buf.getDouble());
        return node;
    }

    private GvEdge readEdge(MappedByteBuffer buf) {
        GvEdge edge = new GvEdge();
        edge.setIdArc(buf.getInt());
        edge.setDirec(buf.getInt());
        edge.setIdNodeOrig(buf.getInt());
        edge.setIdNodeEnd(buf.getInt());
        edge.setType(buf.getInt());
        edge.setDistance(buf.getDouble());
        edge.setWeight(buf.getDouble());
        return edge;
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        NetworkMemoryRedLoader redLoader = new NetworkMemoryRedLoader();
        redLoader.loadNetwork();
    }

    public File getNetFile() {
        return netFile;
    }

    public void setNetFile(File netFile) {
        this.netFile = netFile;
    }
}
