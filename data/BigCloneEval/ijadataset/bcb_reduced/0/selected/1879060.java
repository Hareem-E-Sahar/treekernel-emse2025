package game.gui.vis;

import game.gui.structure3d.JK;
import game.gui.visj3d.VNetJ3D.*;
import game.neurons.Neuron;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class VNet3D {

    public static final int IN = 0, OUT = 1, HIDDEN = 2;

    public double NODE_RADIUS = 0.02;

    protected static double NODE_DISTANCE = 0.2;

    public Color NODE_COLOR_IN = Color.YELLOW;

    public Color NODE_COLOR_OUT = Color.RED;

    public Color NODE_COLOR_HIDDEN = Color.GREEN;

    public static final int LINK_TYPE_FORWARD = 1000;

    public static final int LINK_TYPE_BACK = 1001;

    public static final int LINK_TYPE_LATERAL = 1002;

    public static final int LINK_TYPE_SELF = 1003;

    public Color LINK_COLOR_FORWARD = Color.GRAY;

    public Color LINK_COLOR_BACK = Color.GRAY;

    public Color LINK_COLOR_LATERAL = Color.BLUE;

    public Color LINK_COLOR_SELF = Color.ORANGE;

    public double LINK_MIN_RADIUS = 0.0005;

    public double LINK_MAX_RADIUS = 0.006;

    private double LAYER_DISTANCE = 0.6;

    public double LINK_MIDDLE_R_SHIFT = 0.15;

    public double LINK_MIDDLE_Z_SHIFT = 0.1;

    public double LINK_MIDDLE_Z_SHIFT_CORRECTION = 0.0;

    public double LINK_MIDDLE_X_SHIFT = 0.05;

    public double LINK_MIDDLE_LAYER_ANGLE = 20.0;

    public double LINK_MIDDLE_R_LAYER_SHIFT = 0.3;

    public double LINK_MIDDLE_R_LATERAL_SHIFT = 0.3;

    public double LINK_MIDDLE_R_LATERAL_SHIFT_CORRECTION = 0.03;

    public static final int MAX_WIDTH = 200;

    public static final int MAX_HEIGHT = 65;

    public static boolean SHOW_NODE_MARKS = true;

    private HashMap input;

    private HashMap output;

    private HashMap hidden;

    protected HashMap all;

    private ArrayList layers;

    private double[] layerXPosition;

    private LinkedList inputLayer;

    protected LinkedList hiddenLayer;

    private LinkedList outputLayer;

    protected LinkedList allNodes;

    protected LinkedList allLinks;

    protected JK jk;

    protected Canvas can;

    private double minWeight;

    private double maxWeight;

    protected TextArea info;

    protected InputNames inputsInfo;

    protected ScrollPane scrollArea;

    private int outputID;

    protected Button refreshBtn;

    protected Button inspectBtn;

    protected Checkbox displayResponses;

    protected Checkbox displayNodes;

    protected Checkbox displayLinks;

    protected ErrorGraph errorInfo;

    protected Node lastSelectedNode;

    protected Link lastSelectedLink;

    protected Node selectedOutputNode;

    protected VNet3D() {
        input = new HashMap();
        output = new HashMap();
        hidden = new HashMap();
        all = new HashMap();
        inputLayer = new LinkedList();
        hiddenLayer = new LinkedList();
        outputLayer = new LinkedList();
        allNodes = new LinkedList();
        allLinks = new LinkedList();
        lastSelectedNode = null;
        lastSelectedLink = null;
        selectedOutputNode = null;
    }

    int codeNode(int oid) {
        return 2 * oid;
    }

    protected int decodeNode(int oid) {
        return oid / 2;
    }

    int codeLink(int oid) {
        return 2 * oid + 1;
    }

    protected int decodeLink(int oid) {
        return (oid - 1) / 2;
    }

    protected abstract Node newNode(int oid, int otype, Neuron realRef);

    protected abstract Node newNode(int oid, int otype, double othreshold, Neuron realRef);

    protected abstract Link newLink(int oid, Node ofrom, Node oto, double oweight);

    /**
     * TODO check correctness
     *
     * @param oid
     * @param otype
     * @param olayer
     * @param realRef
     */
    public void addNode(int oid, int otype, int olayer, Neuron realRef) {
        int tid = codeNode(oid);
        Node n = newNode(tid, otype, realRef);
        if (otype == IN) {
            input.put(tid, n);
            inputLayer.add(n);
        } else if (otype == HIDDEN) {
            n.setLayer(olayer);
            hidden.put(tid, n);
            hiddenLayer.add(n);
        } else if (otype == OUT) {
            output.put(tid, n);
            outputLayer.add(n);
        }
        if (n.getId() != oid && realRef != null) realRef.setId(oid);
        all.put(tid, n);
        allNodes.add(n);
    }

    /**
     * TODO check correctness
     *
     * @param oid
     * @param otype
     * @param othreshold
     * @param olayer
     * @param realRef
     */
    public void addNode(int oid, int otype, double othreshold, int olayer, Neuron realRef) {
        int tid = codeNode(oid);
        Node n = newNode(tid, otype, othreshold, realRef);
        if (otype == IN) {
            input.put(tid, n);
            inputLayer.add(n);
        } else if (otype == HIDDEN) {
            n.setLayer(olayer);
            hidden.put(tid, n);
            hiddenLayer.add(n);
        } else if (otype == OUT) {
            output.put(tid, n);
            outputLayer.add(n);
        }
        if (n.getId() != oid) realRef.setId(oid);
        all.put(tid, n);
        allNodes.add(n);
    }

    public void setOutputID(int outputID) {
        this.outputID = outputID;
    }

    public int getOutputID() {
        return outputID;
    }

    public HashMap getHidden() {
        return hidden;
    }

    public void setNodeThreshold(int oid, double othreshold) {
        Node n = (Node) (all.get(new Integer(2 * oid)));
        n.setThreshold(othreshold);
    }

    /**
     * TODO check correctness
     *
     * @param oid
     * @param ofrom
     * @param oto
     * @param oweight
     */
    public void addLink(int oid, int ofrom, int oto, double oweight) {
        Node f = (Node) all.get(new Integer(codeNode(ofrom)));
        Node t = (Node) all.get(new Integer(codeNode(oto)));
        Link l = newLink(codeLink(oid), f, t, oweight);
        f.addOutLink(l);
        t.addInLink(l);
        allLinks.add(l);
        all.put(codeLink(oid), l);
    }

    /**
     * TODO check for IN->IN
     */
    public void createLayers() {
        LinkedList layerQueue = new LinkedList();
        layers = new ArrayList();
        layers.add(inputLayer);
        Node tn;
        Iterator it = hiddenLayer.iterator();
        int lastLay = 0;
        while (it.hasNext()) {
            tn = (Node) it.next();
            LinkedList tnewLayer;
            if (layers.size() <= tn.getLayer()) {
                lastLay++;
                tnewLayer = new LinkedList();
                tnewLayer.add(tn);
                layers.add(tnewLayer);
            }
            tnewLayer = (LinkedList) layers.get(lastLay);
            tnewLayer.add(tn);
            layers.set(lastLay, tnewLayer);
        }
        layers.add(outputLayer);
        it = outputLayer.iterator();
        while (it.hasNext()) {
            tn = (Node) (it.next());
            tn.setLayer(layers.size() - 1);
        }
    }

    public void setNodePositionsInLayer() {
        Iterator it = layers.iterator();
        while (it.hasNext()) {
            LinkedList tlayer = (LinkedList) (it.next());
            Iterator it2 = tlayer.iterator();
            int counter = 0;
            while (it2.hasNext()) {
                Node tn = (Node) it2.next();
                tn.setPositionInLayer(counter++);
            }
        }
    }

    public void setNodePositions() {
        layerXPosition = new double[layers.size()];
        double layerX = -0.5 * (layers.size() - 1) * LAYER_DISTANCE;
        for (int i = 0; i < layers.size(); i++) {
            layerXPosition[i] = layerX;
            layerX += LAYER_DISTANCE;
            LinkedList tlayer = (LinkedList) (layers.get(i));
            Iterator it = tlayer.iterator();
            double nodeYPosition = -0.5 * (tlayer.size() - 1) * NODE_DISTANCE;
            while (it.hasNext()) {
                Node tn = (Node) (it.next());
                tn.setPosition(layerXPosition[i], nodeYPosition, 0.0);
                nodeYPosition += NODE_DISTANCE;
            }
        }
    }

    public void setLinkPositionsInLayer() {
        Iterator it = layers.iterator();
        while (it.hasNext()) {
            LinkedList tlay = (LinkedList) it.next();
            Iterator it2 = tlay.iterator();
            Node[] nodes = new Node[tlay.size()];
            int count = 0;
            while (it2.hasNext()) {
                Node tn = (Node) it2.next();
                nodes[count++] = tn;
            }
            ArrayList toHigher = new ArrayList();
            ArrayList toLower = new ArrayList();
            ArrayList actualDirection;
            toHigher.add(new LinkedList());
            toLower.add(new LinkedList());
            for (Node node : nodes) {
                Iterator it3 = node.getOutLinks().iterator();
                while (it3.hasNext()) {
                    Link tl = (Link) it3.next();
                    if (tl.from.getLayer() == tl.to.getLayer() && tl.from.getPositionInLayer() != tl.to.getPositionInLayer() && tl.from.getPositionInLayer() != tl.to.getPositionInLayer() + 1 && tl.from.getPositionInLayer() != tl.to.getPositionInLayer() - 1) {
                        if (tl.from.getPositionInLayer() < tl.to.getPositionInLayer()) {
                            actualDirection = toHigher;
                        } else {
                            actualDirection = toLower;
                        }
                        boolean placed = false;
                        int actual = 0;
                        while (!placed) {
                            Iterator it4 = ((LinkedList) actualDirection.get(actual)).iterator();
                            boolean fits = true;
                            while (it4.hasNext()) {
                                Link checkLink = (Link) it4.next();
                                if (tl.checkLateralCrossing(checkLink)) {
                                    fits = false;
                                    break;
                                }
                            }
                            if (fits) {
                                ((LinkedList) actualDirection.get(actual)).add(tl);
                                if (actual % 2 == 0) actual = -actual / 2; else if (actual % 2 == 1) actual = (actual + 1) / 2;
                                tl.setLateralAngle(actual);
                                tl.setLateralLevel(Math.abs(tl.to.getPositionInLayer() - tl.from.getPositionInLayer()) - 1);
                                placed = true;
                            } else {
                                actual++;
                                actualDirection.add(new LinkedList());
                            }
                        }
                    }
                }
            }
        }
    }

    public void getMinMaxWeights() {
        minWeight = Double.POSITIVE_INFINITY;
        maxWeight = Double.NEGATIVE_INFINITY;
        Iterator it = allLinks.iterator();
        while (it.hasNext()) {
            Link tl = (Link) (it.next());
            if (minWeight > tl.getWeight()) minWeight = tl.getWeight();
            if (maxWeight < tl.getWeight()) maxWeight = tl.getWeight();
        }
    }

    protected void drawNodes() {
        Iterator it = allNodes.iterator();
        while (it.hasNext()) {
            Node tn = (Node) (it.next());
            tn.draw(can);
        }
    }

    protected void drawLinks() {
        Iterator it = allLinks.iterator();
        while (it.hasNext()) {
            Link tl = (Link) (it.next());
            tl.draw(can, minWeight, maxWeight);
        }
    }

    protected void resetColors() {
        if (lastSelectedNode != null) {
            lastSelectedNode.setDefaultColor();
            Iterator it;
            if (lastSelectedNode.getType() == 0) {
                it = lastSelectedNode.getOutLinks().iterator();
            } else {
                it = lastSelectedNode.getInLinks().iterator();
            }
            while (it.hasNext()) {
                Link tout = (Link) it.next();
                tout.setDefaultColor();
            }
            lastSelectedNode = null;
        }
        if (lastSelectedLink != null) {
            lastSelectedLink.setDefaultColor();
            lastSelectedLink = null;
        }
        if (selectedOutputNode != null) {
            selectedOutputNode.setDefaultColor();
        }
    }

    public void setLocation(int ox, int oy) {
        jk.setLocation(ox, oy);
    }

    public void deleteAllNodes() {
        input.clear();
        output.clear();
        hidden.clear();
        all.clear();
        inputLayer.clear();
        hiddenLayer.clear();
        outputLayer.clear();
        allNodes.clear();
        allLinks.clear();
        lastSelectedNode = null;
        lastSelectedLink = null;
        selectedOutputNode = null;
    }

    public abstract void draw(int owidth, int oheight);
}
