package org.peertrust.demo.client.applet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import org.jgraph.JGraph;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.Port;
import org.peertrust.event.AnswerEvent;
import org.peertrust.event.PTEvent;
import org.peertrust.event.PTEventListener;
import org.peertrust.event.QueryEvent;
import org.peertrust.meta.Proof;
import org.peertrust.meta.Trace;
import org.peertrust.net.Answer;
import org.peertrust.net.Peer;
import org.peertrust.net.Query;
import org.peertrust.tnviz.app.TNEdge;
import org.peertrust.tnviz.app.TNNode;

/**
 * A modification of TNTreeDiagramm to get ride of the
 * dependencies to Graphics and TNGui
 */
public class TestTreeDiagramm {

    private static Logger log = Logger.getLogger(TestTreeDiagramm.class);

    TNNode root = null;

    int nodeBoundsX = 80;

    int nodeBoundsY = 20;

    int nRadius = 80;

    double fScale = 1;

    private JGraph graph;

    private GraphModel model;

    private Hashtable graphElements;

    private Vector graphPath;

    private List listVisibleNodes;

    private List listFalseQueries;

    private List listFalseAnswers;

    private List listEdges;

    private final int MIN_X_ABSTAND = 110;

    private final int MIN_Y_ABSTAND = 40;

    private FontMetrics fontmetrics;

    public PTEventListener ptListener = new PTEventListener() {

        public void event(PTEvent ptEvent) {
            if (ptEvent instanceof AnswerEvent) {
                addAnswer(((AnswerEvent) ptEvent).getAnswer());
                calculateGraphLayout();
            } else if (ptEvent instanceof QueryEvent) {
                addQuery(((QueryEvent) ptEvent).getQuery());
                calculateGraphLayout();
            } else {
            }
            return;
        }
    };

    /**
     * The constructor of this class. It requires the graphics object with which
     * this diagramm will be used.
     * @param graphics The graphics object.
     */
    public TestTreeDiagramm() {
        graph = new JGraph();
        model = new DefaultGraphModel();
        graphElements = new Hashtable();
        graphPath = new Vector();
        listVisibleNodes = new Vector();
        listFalseQueries = new Vector();
        listFalseAnswers = new Vector();
        listEdges = new Vector();
        graph.setModel(model);
        graph.setGraphLayoutCache(new GraphLayoutCache(model, new DefaultCellViewFactory(), true));
        fontmetrics = graph.getFontMetrics(graph.getFont());
    }

    /**
     * Cleans the graph by creating a new graph object and by resetting
     * status information like graphPath and graphElements.
     */
    public void wipeGraph() {
        System.out.println("====>wipeGraph()");
        graph = new JGraph();
        model = new DefaultGraphModel();
        graphElements = new Hashtable();
        graphPath = new Vector();
        listVisibleNodes = new Vector();
        graph.setModel(model);
        graph.setGraphLayoutCache(new GraphLayoutCache(model, new DefaultCellViewFactory(), true));
        fontmetrics = graph.getFontMetrics(graph.getFont());
    }

    /**
     * Adds a query. The query information is being read out and new nodes and
     * edges are created according to the sequence diagramm structure.
     * @param query The new query.
     */
    public void addQuery(Query query) {
        if (query.getSource() == null || query.getTarget() == null) {
            return;
        }
        String sourceAddress = query.getSource().getAddress();
        String sourceAlias = query.getSource().getAlias();
        int sourcePort = query.getSource().getPort();
        String targetAddress = query.getTarget().getAddress();
        String targetAlias = query.getTarget().getAlias();
        int targetPort = query.getTarget().getPort();
        long reqQueryId = query.getReqQueryId();
        String goal = query.getGoal();
        if (root == null) {
            TNNode source = (TNNode) getElement(createNode("source", -1, "", "source", -1));
            graph.getGraphLayoutCache().setVisible(new Object[] { source }, false);
            source.setInvisible(true);
            graph.getGraphLayoutCache().reload();
            root = source;
        }
        Trace trace = query.getTrace();
        log.debug(query.getTrace().printTrace());
        if (trace.isEmptyTrace()) return;
        Vector strTrace = trace.getTrace();
        if (strTrace.size() == 1) {
            log.debug(query.getTrace().printTrace() + "length 1");
            TNNode source = root;
            TNNode target = (TNNode) getElement(createNode(sourceAlias, reqQueryId, sourceAddress, sourceAlias, sourcePort));
            TNEdge edge = (TNEdge) getElement(connectNodes(source, target, "", "", -1, true, false, -1, ""));
            graphPath.add(edge);
            graphPath.add(target);
            TNNode target2 = (TNNode) getElement(createNode(targetAlias, reqQueryId, targetAddress, targetAlias, targetPort));
            edge = (TNEdge) getElement(connectNodes(target, target2, goal + "?", goal, reqQueryId, true, false, -1, ""));
            graphPath.add(edge);
            listEdges.add(edge);
            graphPath.add(target2);
            graph.getGraphLayoutCache().setVisible(new Object[] { root }, false);
            while (listFalseQueries.size() > 0) addQuery((Query) listFalseQueries.remove(0));
            while (listFalseAnswers.size() > 0) addAnswer((Answer) listFalseAnswers.remove(0));
        } else {
            TNNode node = null;
            for (int i = 0; i < strTrace.size() - 1; i++) {
                if ((i == 0) && (listEdges.size() == 0)) {
                    if (!listFalseQueries.contains(query)) listFalseQueries.add(query);
                    return;
                }
                List list_edges = (i == 0) ? listEdges : getEdges(node);
                for (int j = 0; j < list_edges.size(); j++) {
                    if (((TNEdge) list_edges.get(j)).getLabel().equals(strTrace.elementAt(i))) {
                        node = ((TNEdge) list_edges.get(j)).getTargetNode();
                        break;
                    }
                    if (j == list_edges.size() - 1) {
                        if (!listFalseQueries.contains(query)) listFalseQueries.add(query);
                        return;
                    }
                }
            }
            TNNode target = (TNNode) getElement(createNode(targetAlias, reqQueryId, targetAddress, targetAlias, targetPort));
            TNEdge edge = (TNEdge) getElement(connectNodes(node, target, goal + "?", goal, reqQueryId, true, false, -1, ""));
            graphPath.add(edge);
            graphPath.add(target);
            while (listFalseQueries.size() > 0) addQuery((Query) listFalseQueries.remove(0));
            while (listFalseAnswers.size() > 0) addAnswer((Answer) listFalseAnswers.remove(0));
        }
    }

    /**
     * Adds an answer. The answer information is being read out and new nodes and
     * edges are created according to the sequence diagramm structure.
     * @param answer The new answer.
     */
    public void addAnswer(Answer answer) {
        if (answer.getStatus() != Answer.FAILURE) {
            if (answer.getSource() == null || answer.getTarget() == null) {
                return;
            }
            String targetAddress = answer.getTarget().getAddress();
            String targetAlias = answer.getTarget().getAlias();
            int targetPort = answer.getTarget().getPort();
            String goal = "<" + answer.getGoal() + ">";
            long reqQueryId = answer.getReqQueryId();
            int status = answer.getStatus();
            String proof = answer.getProof().toString();
            Trace trace = answer.getTrace();
            if (trace.isEmptyTrace()) return;
            Vector strTrace = trace.getTrace();
            if (strTrace.size() == 1) return;
            TNNode node = null;
            for (int i = 0; i < strTrace.size() - 1; i++) {
                if ((i == 0) && (listEdges.size() == 0)) {
                    if (!listFalseAnswers.contains(answer)) listFalseAnswers.add(answer);
                    return;
                }
                List list_edges = (i == 0) ? listEdges : getEdges(node);
                for (int j = 0; j < list_edges.size(); j++) {
                    if (((TNEdge) list_edges.get(j)).getLabel().equals(strTrace.elementAt(i))) {
                        node = ((TNEdge) list_edges.get(j)).getTargetNode();
                        break;
                    }
                    if (j == list_edges.size() - 1) {
                        if (!listFalseAnswers.contains(answer)) listFalseAnswers.add(answer);
                        return;
                    }
                }
            }
            TNNode target = (TNNode) getElement(createNode(targetAlias, reqQueryId, targetAddress, targetAlias, targetPort));
            TNEdge edge = (TNEdge) getElement(connectNodes(node, target, goal, goal, reqQueryId, false, true, status, proof));
            if ((graphPath.size() == 0) || (!graphPath.lastElement().equals(node))) {
                graphPath.add(node);
            }
            graphPath.add(edge);
            graphPath.add(target);
            while (listFalseQueries.size() > 0) addQuery((Query) listFalseQueries.remove(0));
            while (listFalseAnswers.size() > 0) addAnswer((Answer) listFalseAnswers.remove(0));
        }
    }

    /**
     * Returns the node with the given id.
     * @param id The id of the node.
     * @return The according node.
     */
    public Object getElement(String id) {
        return graphElements.get(id);
    }

    /**
     * Creates a new node with the given information.
     * @param object The label object.
	* @param x The x-position in pixels.
	* @param y The y-position in pixels.
	* @param reQueryId The reqQueryId.
     * @param peerAddress The peer address.
     * @param peerAlias The peer alias.
     * @param peerPort The peer port.
     * @return The id of the new node.
     */
    private String createNode(Object object, int x, int y, long reqQueryId, String peerAddress, String peerAlias, int peerPort) {
        TNNode node = new TNNode(object, graph);
        setNodeInformation(node, object.toString(), "node:" + reqQueryId + ":" + peerAlias + ":" + peerAddress + ":" + peerPort, peerAddress, peerAlias, peerPort);
        graphElements.put(node.getId(), node);
        DefaultPort port = new DefaultPort();
        node.add(port);
        node.setPort(port);
        Map nodeAttributes = new Hashtable();
        Rectangle nodeBounds = new Rectangle(x, y, nodeBoundsX, nodeBoundsY);
        GraphConstants.setBounds(nodeAttributes, nodeBounds);
        GraphConstants.setMoveable(nodeAttributes, false);
        GraphConstants.setBendable(nodeAttributes, false);
        GraphConstants.setSizeable(nodeAttributes, false);
        GraphConstants.setEditable(nodeAttributes, false);
        GraphConstants.setBorderColor(nodeAttributes, Color.BLACK);
        GraphConstants.setBackground(nodeAttributes, Color.WHITE);
        GraphConstants.setOpaque(nodeAttributes, true);
        Map attributes = new Hashtable();
        attributes.put(node, nodeAttributes);
        graph.getGraphLayoutCache().insert(new Object[] { node }, attributes, null, null, null);
        return node.getId();
    }

    /**
     * Creates a new node with the given information.
     * @param object The label object.
	* @param reQueryId The reqQueryId.
     * @param peerAddress The peer address.
     * @param peerAlias The peer alias.
     * @param peerPort The peer port.
     * @return The id of the new node.
     */
    public String createNode(Object object, long reqQueryId, String peerAddress, String peerAlias, int peerPort) {
        return createNode(object, 0, 0, reqQueryId, peerAddress, peerAlias, peerPort);
    }

    /**
     * Connects the two given nodes with a new edge with the given information.
     * For this purpose, new invisible nodes and new visible edges are being
     * created to construct the sequence diagramm structure.
     * @param nodeSource The source node.
     * @param nodeTarget The target node.
     * @param object The label object.
     * @param goal The goal.
     * @param reqQueryId The reqQueryId.
     * @param query True, if the connection represents a query, otherwise false.
     * @param answer True, if the connection represents an answer, otherwise false.
     * @param status The status.
     * @param proof The proof.
     * @return The id of the new edge.
     */
    private String connectNodes(TNNode nodeSource, TNNode nodeTarget, Object object, String goal, long reqQueryId, boolean query, boolean answer, int status, String proof) {
        DefaultPort portSource = (DefaultPort) nodeSource.getPort();
        DefaultPort portTarget = (DefaultPort) nodeTarget.getPort();
        TNEdge edge = new TNEdge(object);
        edge.setLabel(object.toString());
        Map edgeAttributes = new Hashtable();
        int arrow;
        if (query) {
            arrow = GraphConstants.ARROW_SIMPLE;
        } else {
            arrow = GraphConstants.ARROW_CLASSIC;
        }
        GraphConstants.setLineEnd(edgeAttributes, arrow);
        GraphConstants.setEndFill(edgeAttributes, true);
        GraphConstants.setLabelAlongEdge(edgeAttributes, true);
        GraphConstants.setMoveable(edgeAttributes, true);
        GraphConstants.setConnectable(edgeAttributes, true);
        GraphConstants.setDisconnectable(edgeAttributes, true);
        GraphConstants.setBendable(edgeAttributes, true);
        GraphConstants.setSizeable(edgeAttributes, true);
        GraphConstants.setEditable(edgeAttributes, true);
        GraphConstants.setLineColor(edgeAttributes, Color.BLUE);
        Map attributes = new Hashtable();
        attributes.put(edge, edgeAttributes);
        ConnectionSet cs = new ConnectionSet();
        cs.connect(edge, portSource, portTarget);
        setEdgeInformation(edge, edge.getLabel(), "edge:" + reqQueryId + ":" + goal, goal, reqQueryId, query, answer, status, proof);
        edge.setSourceNode(nodeSource);
        edge.setTargetNode(nodeTarget);
        graphElements.put(edge.getId(), edge);
        graph.getGraphLayoutCache().insert(new Object[] { edge }, attributes, cs, null, null);
        return edge.getId();
    }

    /**
     * Updated the given node with the given information.
     * @param node The node which will be updated.
     * @param title The title.
     * @param id The id.
     * @param peerAddress The peer address.
     * @param peerAlias The peer alias.
     * @param peerPort The peer port.
     */
    private void setNodeInformation(TNNode node, String title, String id, String peerAddress, String peerAlias, int peerPort) {
        node.setTitle(title);
        node.setId(id);
        node.setPeerAddress(peerAddress);
        node.setPeerAlias(peerAlias);
        node.setPeerPort(peerPort);
    }

    /**
     * Updated the given edge with the given information.
     * @param edge The edge which will be updated.
     * @param label The label.
     * @param id The id.
     * @param goal The goal.
     * @param reqQueryId The reqQueryId.
     * @param True, if the edge represents a query, otherwise false.
     * @param answer True, if the edge represents an answer, otherwise false.
     * @param status The status.
     * @param proof The proof.
     */
    private void setEdgeInformation(TNEdge edge, String label, String id, String goal, long reqQueryId, boolean query, boolean answer, int status, String proof) {
        edge.setId(id);
        edge.setLabel(label);
        edge.setGoal(goal);
        edge.setReqQueryId(reqQueryId);
        edge.setQuery(query);
        edge.setAnswer(answer);
        edge.setStatus(status);
        edge.setProof(proof);
    }

    /**
     * Returns the JGraph object on which this diagramm is operating.
     * @return The JGraph object.
     */
    public JGraph getGraph() {
        return graph;
    }

    /**
     * Refreshed the graphs. Should be called after changes were made.
     */
    public void refreshGraph() {
        graph.getGraphLayoutCache().setVisible(new Object[] { root }, false);
        graph.getGraphLayoutCache().reload();
        graph.repaint();
    }

    /**
     * Returns a hashtable with all graph elements. The keys are the ids of the
     * elements, the value the according objects.
     * @return A hashtable with the graph elements.
     */
    public Hashtable getGraphElements() {
        return graphElements;
    }

    /**
     * Sets the graph elements to the given hashtable.
     * @param graphElements The new graph elements table.
     */
    public void setGraphElements(Hashtable graphElements) {
        this.graphElements = graphElements;
    }

    /**
     * Returns the graphPath of this diagramm. The graphPath vector contains
     * the graph elements in the order in which they were created.
     * Additional graph elements which were automatically created for the
     * structure of the diagramm are not included.
     * @return A vector with the graph elements.
     */
    public Vector getGraphPath() {
        return graphPath;
    }

    /**
     * Sets the graphPath to the given vector.
     * @param graphPath The new graphPath.
     */
    public void setGraphPath(Vector graphPath) {
        this.graphPath = graphPath;
    }

    /**
     * Positions the given node. Subsequently, the graph will be refreshed.
     * @param node The node.
     * @param x The new x coordinate.
     * @param y The new y coordinate.
     */
    private void positionNode(TNNode node, int x, int y) {
        positionNode(node, x, y, nodeBoundsX, nodeBoundsY);
        refreshGraph();
    }

    /**
     * Positions the given node. Subsequently, the graph will be refreshed.
     * @param node The node.
     * @param x The new x coordinate.
     * @param y The new y coordinate.
     * @param width The width.
     * @param height The height.
     */
    private void positionNode(TNNode node, int x, int y, int width, int height) {
        int real_width = Math.min(width, node.getLabelWidth());
        int real_x = x;
        if (real_width < width) {
            real_x = x + (width - real_width) / 2;
            width = real_width;
        }
        Rectangle nodeBounds = new Rectangle(real_x, y, width, height);
        Map nodeAttributes = node.getAttributes();
        GraphConstants.setBounds(nodeAttributes, nodeBounds);
        node.setX(x);
        node.setY(y);
        refreshGraph();
    }

    /**
	 * Calculates the layout of the tree diagram.
	 */
    public void calculateGraphLayout() {
        final int width = (graph.getRootPane() != null) ? graph.getRootPane().getWidth() : 500;
        final int height = (graph.getRootPane() != null) ? graph.getRootPane().getHeight() : 800;
        if (root == null) return;
        getVisibleNodes();
        fScale = 1.0;
        int stufen[] = getStufen(), a;
        int y_radius = 35;
        int paneHeight = height;
        int y_delta = (stufen.length > 1) ? (paneHeight - y_radius) / (stufen.length - 1) : 0;
        if (y_delta < MIN_Y_ABSTAND) {
            y_delta = MIN_Y_ABSTAND;
        }
        int anzahl, x_delta;
        TNNode node;
        System.out.println("width:" + width + " height:" + height);
        for (int i = 0; i < stufen.length; i++) {
            anzahl = (i < stufen.length - 1) ? stufen[i + 1] - stufen[i] : listVisibleNodes.size() - stufen[i];
            x_delta = (height - nRadius) / anzahl;
            if (x_delta < MIN_X_ABSTAND) {
                a = x_delta;
                x_delta -= 2 * nRadius;
                nRadius = (MIN_X_ABSTAND - a) / 2;
                x_delta += 2 * nRadius;
                if (x_delta < MIN_X_ABSTAND) fScale = (double) (anzahl * MIN_X_ABSTAND) / (double) width;
            }
            for (int j = stufen[i]; j < stufen[i] + anzahl; j++) {
                node = (TNNode) listVisibleNodes.get(j);
                positionNode(node, (j - stufen[i] + 1) * width / (anzahl + 1) - nRadius / 2, (node.getStufe() - 2) * y_delta);
            }
        }
        for (int i = 0; i < listVisibleNodes.size(); i++) {
            node = (TNNode) listVisibleNodes.get(i);
            System.out.println("\n=============>node:" + node.getPeerAlias() + " x:" + node.getX() + " y:" + node.getY());
            positionNode(node, (int) (fScale * node.getX()), node.getY(), nRadius, y_radius);
        }
        refreshGraph();
    }

    /**
	 * Returns all visible nodes sorted after theirs depth tree level.
	 */
    private void getVisibleNodes() {
        listVisibleNodes.clear();
        int new_index = 1;
        root.setStufe(1);
        listVisibleNodes.add(root);
        listVisibleNodes.add(root);
        TNNode actualnode, child;
        Vector children;
        while (new_index > 0) {
            actualnode = (TNNode) listVisibleNodes.get(0);
            if (actualnode.getExpanded()) {
                children = getChildren(actualnode);
                if (children != null) for (int i = 0; i < children.size(); i++) {
                    child = (TNNode) children.get(i);
                    child.setStufe(actualnode.getStufe() + 1);
                    listVisibleNodes.add(new_index, child);
                    new_index++;
                    listVisibleNodes.add(child);
                }
            }
            new_index--;
            listVisibleNodes.remove(0);
        }
    }

    /**
	 * Returns an array of indices which represent the different depth tree levels.
	 * return int[] Beginning-Indices of depth levels.
	 */
    private int[] getStufen() {
        int max = 0;
        for (int i = 0; i < listVisibleNodes.size(); i++) max = Math.max(max, ((TNNode) listVisibleNodes.get(i)).getStufe());
        if (max <= 1) return new int[0];
        int stufen[] = new int[max - 1];
        stufen[0] = 1;
        for (int i = 2; i < listVisibleNodes.size(); i++) if (((TNNode) listVisibleNodes.get(i)).getStufe() > ((TNNode) listVisibleNodes.get(i - 1)).getStufe()) stufen[((TNNode) listVisibleNodes.get(i)).getStufe() - 2] = i;
        return stufen;
    }

    /**
	 * Returns all children of a node.
	 * @param node The node.
	 * @return Vector The children of the node.
	 */
    private Vector getChildren(TNNode node) {
        Vector result = new Vector();
        List children = node.getChildren();
        Object object = null;
        DefaultPort port = null;
        Object[] edges = null;
        TNEdge edge = null;
        Object target = null;
        DefaultPort targetPort = null;
        TNNode childNode = null;
        for (int i = 0; i < children.size(); i++) {
            object = children.get(i);
            if (object instanceof Port) {
                port = (DefaultPort) object;
                edges = port.getEdges().toArray();
                for (int j = 0; j < edges.length; j++) {
                    edge = (TNEdge) edges[j];
                    target = edge.getTarget();
                    if (target instanceof Port) {
                        targetPort = (DefaultPort) target;
                        childNode = (TNNode) targetPort.getParent();
                        if ((!childNode.equals(node)) && (!result.contains(node))) {
                            result.add(childNode);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
	 * Returns all edges of a node.
	 * @param node The node.
	 * @return Vector The edges of the node.
	 */
    private Vector getEdges(TNNode node) {
        Vector result = new Vector();
        DefaultPort port = null;
        Object[] edges = null;
        TNEdge edge = null;
        port = (DefaultPort) node.getPort();
        edges = port.getEdges().toArray();
        for (int i = 0; i < edges.length; i++) {
            edge = (TNEdge) edges[i];
            result.add(edge);
        }
        return result;
    }

    /**
	 * Collapse a node, that means make all children and child-trees invisible.
	 * @param node The node.
	 */
    public void collapse(TNNode node) {
        node.setExpanded(false);
        TNNode childNode = null;
        List children = getChildren(node);
        Vector visited = new Vector();
        Vector invisible = new Vector();
        visited.add(node);
        invisible.add(node);
        for (int i = 0; i < children.size(); i++) {
            childNode = (TNNode) children.get(i);
            if (!visited.contains(childNode)) {
                visited.add(childNode);
                collapseChild(childNode, visited, invisible);
            } else if (!invisible.contains(childNode)) {
                collapseChild(childNode, visited, invisible);
            }
        }
        if (children.size() > 0) {
            String nodeLabel = node.getUserObject().toString();
            if (!nodeLabel.startsWith("(+)")) {
                node.setObject("(+) " + nodeLabel);
            }
        }
        graph.getGraphLayoutCache().reload();
        calculateGraphLayout();
    }

    /**
	 * Helper function for collapsing child nodes.
	 * @param child The child node.
	 * @param visited The vector that contains all visited nodes.
	 * @param invisble The vector that contains all invisible nodes.
	 */
    private void collapseChild(TNNode child, Vector visited, Vector invisible) {
        TNNode childNode = null;
        List children = getChildren(child);
        if (!invisible.contains(child)) {
            graph.getGraphLayoutCache().setVisible(new Object[] { child }, false);
            child.setInvisible(true);
            invisible.add(child);
        }
        for (int i = 0; i < children.size(); i++) {
            childNode = (TNNode) children.get(i);
            if (!visited.contains(childNode)) {
                visited.add(childNode);
                collapseChild(childNode, visited, invisible);
            } else if (!invisible.contains(childNode)) {
                collapseChild(childNode, visited, invisible);
            }
        }
        Vector edges = getEdges(child);
        for (int i = 0; i < edges.size(); i++) {
            TNEdge edge = (TNEdge) edges.get(i);
            edge.setInvisible(true);
        }
    }

    /**
	 * Expands a node, that means make all children and child-trees visible.
	 * @param node The node.
	 */
    public void expand(TNNode node) {
        node.setExpanded(true);
        TNNode childNode = null;
        List children = getChildren(node);
        Vector visited = new Vector();
        Vector visible = new Vector();
        visited.add(node);
        visible.add(node);
        for (int i = 0; i < children.size(); i++) {
            childNode = (TNNode) children.get(i);
            if (!visited.contains(childNode)) {
                visited.add(childNode);
                expandChild(childNode, visited, visible);
            } else if (!visible.contains(childNode)) {
                expandChild(childNode, visited, visible);
            }
        }
        String nodeLabel = node.getUserObject().toString();
        if (nodeLabel.startsWith("(+)")) {
            node.setObject(nodeLabel.substring(4, nodeLabel.length()));
        }
        graph.getGraphLayoutCache().reload();
        calculateGraphLayout();
    }

    /**
	 * Helper function for expanding child nodes.
	 * @param child The child node.
	 * @param visited The vector that contains all visited nodes.
	 * @param invisble The vector that contains all invisible nodes.
	 */
    private void expandChild(TNNode child, Vector visited, Vector visible) {
        TNNode childNode = null;
        List children = getChildren(child);
        if (!visible.contains(child)) {
            graph.getGraphLayoutCache().setVisible(new Object[] { child }, true);
            child.setInvisible(false);
            visible.add(child);
        }
        if (!child.getExpanded()) {
            return;
        }
        for (int i = 0; i < children.size(); i++) {
            childNode = (TNNode) children.get(i);
            if (!visited.contains(childNode)) {
                visited.add(childNode);
                expandChild(childNode, visited, visible);
            } else if (!visible.contains(childNode)) {
                expandChild(childNode, visited, visible);
            }
        }
        Vector edges = getEdges(child);
        for (int i = 0; i < edges.size(); i++) {
            TNEdge edge = (TNEdge) edges.get(i);
            edge.setInvisible(false);
        }
        String nodeLabel = child.getUserObject().toString();
        if (nodeLabel.startsWith("(+)")) {
            child.setObject(nodeLabel.substring(4, nodeLabel.length()));
        }
        graph.getGraphLayoutCache().reload();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(500, 800);
        final TestTreeDiagramm td = new TestTreeDiagramm();
        final JPanel container = new JPanel();
        Container c = frame.getContentPane();
        c.setLayout(new BorderLayout());
        JPanel bPanel = new JPanel();
        c.add(bPanel, BorderLayout.WEST);
        bPanel.setLayout(new GridLayout(3, 1));
        ActionListener toggleActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                container.add(td.graph);
                container.validate();
            }
        };
        JButton toggleButton = new JButton("toggle VIZ");
        toggleButton.addActionListener(toggleActionListener);
        bPanel.add(toggleButton);
        container.setLayout(new GridLayout(1, 1));
        c.add(container, BorderLayout.CENTER);
        final Peer elearnPeer = new Peer("elearn", "127.0.0.1", 7703);
        final Peer alicePeer = new Peer("alice", "127.0.0.1", 7704);
        final Query query = new Query("ieeeMember(alice)", elearnPeer, alicePeer, 0, null);
        final Proof proof = new Proof("blablaProof");
        final Trace trace = new Trace();
        final Answer answer = new Answer("ieeeMember(alice)", 0, proof, 0, 0, elearnPeer, alicePeer, trace);
        JButton addQButton = new JButton("addQuery");
        ActionListener addQActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                td.addQuery(query);
                td.addAnswer(answer);
                td.calculateGraphLayout();
                td.refreshGraph();
            }
        };
        addQButton.addActionListener(addQActionListener);
        bPanel.add(addQButton);
        frame.setVisible(true);
        frame.validate();
        frame.repaint();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
