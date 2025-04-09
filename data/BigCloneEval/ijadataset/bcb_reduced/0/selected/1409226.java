package ren.env;

import java.util.Enumeration;
import java.util.Vector;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import ren.gui.ParameterMap;
import ren.util.PO;

/** * @author Rene Wooller * @version 8/10/02 */
public class ValueGraphModel {

    Vector nodes = new Vector(0, 1);

    public static double[] DFLL = new double[] { 2.0, 3.0, 4.0, 6.0, 8.0, 12.0, 16.0, 32.0, 64.0, 128.0, 256.0, 512.0, 1024.0, 2048.0 };

    private boolean muted = true;

    private int nodeAt;

    public Node beg, end;

    private double minVal, maxVal;

    private ParameterMap loopst, looplen;

    /**	 * 	 * @param minVal	 *            minimum of the value (y)	 * @param maxVal	 *            max (y)	 * @param stpt	 *            starting point of the area (x)	 * @param enpt	 *            end of the area (x)	 * 	 */
    public ValueGraphModel() {
    }

    public ValueGraphModel construct() {
        return construct(0, 127);
    }

    public ValueGraphModel construct(double minVal, double maxVal) {
        return construct(minVal, maxVal, 0.0, DFLL[DFLL.length - 1], 0.0, 16.0);
    }

    public ValueGraphModel construct(double minVal, double maxVal, double stpt, double maxenpt, double loopst, double looplen) {
        this.minVal = minVal;
        this.maxVal = maxVal;
        beg = new Node(stpt - DFLL[DFLL.length - 1] * 2.0, (maxVal - minVal) / 2.0 + minVal);
        end = new Node(maxenpt + DFLL[DFLL.length - 1] * 2.0, (maxVal - minVal) / 2.0 + minVal);
        nodes.addElement(beg);
        nodes.addElement(end);
        this.loopst = (new ParameterMap()).construct(0, 4096, 0.0, DFLL[DFLL.length - 1], 0.0, "loop start");
        this.looplen = (new ParameterMap()).construct(new DefaultBoundedRangeModel(0, 0, 0, DFLL.length - 1), DFLL, 16.0, "loop end");
        return this;
    }

    public double getMaxVal() {
        return maxVal;
    }

    public double getMinVal() {
        return minVal;
    }

    public BoundedRangeModel getLoopLengthModel() {
        return looplen.getModel();
    }

    public double getLoopLength() {
        return this.looplen.getValue();
    }

    public void updateBegEnd() {
        int i = 1;
        while (((Node) (nodes.elementAt(i))).hidden()) {
            i++;
        }
        beg.setYPosInt(((Node) (nodes.elementAt(i))).getYPosInt());
        i = nodes.size() - 2;
        while (((Node) (nodes.elementAt(i))).hidden()) {
            i--;
        }
        end.setYPosInt(((Node) (nodes.elementAt(i))).getYPosInt());
    }

    public Node beg() {
        return beg;
    }

    public Node end() {
        return end;
    }

    public int deleteHidden(int pos) {
        int howMany = 0;
        int toSubtract = 0;
        Node[] n = new Node[nodes.size()];
        nodes.copyInto(n);
        for (int i = 0; i < n.length; i++) {
            if (n[i].hidden()) {
                n[i] = null;
                howMany++;
                if (i < pos) toSubtract++;
            }
        }
        int newAmount = nodes.size() - howMany;
        nodes = new Vector(1, 1);
        nodes.setSize(newAmount);
        int nodeCount = 0;
        for (int i = 0; i < n.length; i++) {
            if (n[i] != null) {
                nodes.setElementAt(n[i], nodeCount);
                nodeCount++;
            } else {
            }
        }
        return toSubtract;
    }

    /**	 * swaps the currently selected node with the next node;	 */
    public void swapNext(int pos) {
        Object temp = nodes.elementAt(pos);
        nodes.setElementAt(nodes.elementAt(pos + 1), pos);
        nodes.setElementAt(temp, pos + 1);
    }

    /**	 * swaps the currently selected node with the previous node;	 */
    public void swapPrev(Node n, int pos) {
        Object temp = nodes.elementAt(pos);
        nodes.setElementAt(nodes.elementAt(pos - 1), pos);
        nodes.setElementAt(temp, pos - 1);
    }

    /**	 * returns the next node in the sequence	 */
    public Node next(int pos) {
        if (pos < nodes.size() - 1) {
            return (Node) nodes.elementAt(pos + 1);
        }
        return end();
    }

    /**	 * returns the previous node in the sequence if it is already at the	 * beggining node, it just returns the beggining node	 */
    public Node prev(int pos) {
        if (pos == 0) return beg(); else return (Node) nodes.elementAt(pos - 1);
    }

    public Node prev(Node n) {
        int[] loc = new int[1];
        if (find(n.getXPos(), loc)) {
            if (loc[0] == 0) {
                return beg();
            }
            return (Node) (nodes.elementAt(loc[0] - 1));
        } else {
            System.out.println(" node " + n.toString() + " doesn't exist");
            return null;
        }
    }

    public Enumeration getEnumeration() {
        return nodes.elements();
    }

    public Node[] getNodeArr() {
        Node[] n = new Node[nodes.size()];
        nodes.copyInto(n);
        return n;
    }

    /**	 * adds a node to the vector in the relevant place.	 * 	 * so it must search though the vector until it finds the specified place.	 * if the space is already filled, then the vector doesn't add it, it	 * returns the position.	 */
    public int addNode(Node n) {
        int[] l = new int[1];
        if (!find(n.getXPos(), l)) {
            nodes.insertElementAt(n, ++l[0]);
        } else {
            nodes.setElementAt(n, l[0]);
        }
        return l[0];
    }

    public int addNode(double xpos, double yval) {
        Node n = new Node();
        n.setPos(xpos, yval);
        return this.addNode(n);
    }

    public void addNode(double[] xpos, double[] yval) {
        if (xpos.length != yval.length) {
            Exception e = new Exception(" the lengths of x and y " + "arrays must be the same");
            e.fillInStackTrace();
            try {
                throw e;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        for (int i = 0; i < xpos.length; i++) {
            addNode(xpos[i], yval[i]);
        }
        this.updateBegEnd();
    }

    /**	 * gets the adjacent nodes of the node described at position pos	 */
    public Node[] getAdjacent(int pos) {
        Node[] arr = new Node[2];
        int size = nodes.size() - 1;
        if (size < 1) {
            arr[0] = beg;
            arr[1] = end;
            return arr;
        } else if (pos == 0) {
            arr[0] = beg;
            arr[1] = (Node) nodes.elementAt(pos + 1);
            return arr;
        } else if (pos == size) {
            arr[0] = (Node) nodes.elementAt(pos - 1);
            arr[1] = end;
            return arr;
        } else {
            arr[0] = (Node) nodes.elementAt(pos - 1);
            arr[1] = (Node) nodes.elementAt(pos + 1);
            return arr;
        }
    }

    /**	 * finds the two nodes which are either side of the value at pos.	 * @param pos	 * @return	 */
    public Node[] getAdjacent(double pos) {
        int[] loc = new int[1];
        if (find(pos, loc)) {
            return new Node[] { ((Node) nodes.get(loc[0])) };
        } else {
            if (((Node) nodes.get(loc[0])).getXPos() > pos) loc[0]--;
            return new Node[] { (Node) (nodes.get(loc[0])), this.next(loc[0]) };
        }
    }

    /**	 * removes a node from the vector	 */
    public void removeNode(Node n) {
        nodes.removeElement(n);
    }

    public int size() {
        return nodes.size();
    }

    /**	 * performs a binary search on the vector, given the x value, and returns	 * the closest node. this will be useful for when the mouse is clicked, so	 * that the nearest node can easily be found. Once it is found, operations	 * can be performed upon it.	 * 	 * it is saying: if the node exists already, OR if the node is near either	 * edge, then return the node that is selected using l (location)	 * 	 * otherwise, return the node that is closest to the x value.	 */
    public Node closest(double x, int[] l) {
        Node to;
        if (find(x, l)) {
            to = (Node) nodes.elementAt(l[0]);
        } else {
            int ll = l[0];
            int ul = l[0] + 1;
            Node lower = (Node) (nodes.elementAt(ll));
            Node upper = (Node) (nodes.elementAt(ul));
            while (lower.hidden()) {
                lower = (Node) (nodes.elementAt(--ll));
            }
            while (upper.hidden()) {
                upper = (Node) (nodes.elementAt(++ul));
            }
            if (ul == nodes.size() - 1) {
                to = lower;
                l[0] = ll;
            } else if (ll == 0) {
                to = upper;
                l[0] = ul;
            } else {
                double center = ((upper.getXPos() + lower.getXPos()) / 2);
                if (x >= center) {
                    to = upper;
                    l[0] = ul;
                } else {
                    to = lower;
                    l[0] = ll;
                }
            }
        }
        return to;
    }

    private int compareX(double x1, double x2) {
        if (x1 < x2) return -1;
        if (x1 > x2) return 1;
        return 0;
    }

    private boolean find(double x, int[] location) {
        int last = nodes.size() - 1;
        if (last < 0) {
            location[0] = 0;
            return false;
        }
        int first = 0;
        int i = 0;
        while (true) {
            int position = (last + first) / 2;
            Node current = (Node) nodes.elementAt(position);
            switch(compareX(x, current.getXPos())) {
                case 0:
                    location[0] = position;
                    return true;
                case -1:
                    if (first > last) {
                        location[0] = last;
                        return false;
                    } else if (first == last) {
                        location[0] = position - 1;
                        return false;
                    } else {
                        last = position - 1;
                    }
                    break;
                case 1:
                    if (first == last) {
                        location[0] = position;
                        return false;
                    } else if (first > last) {
                        location[0] = position;
                        return false;
                    } else {
                        first = position + 1;
                    }
                    break;
            }
        }
    }

    private boolean findDown(double x, int[] loc) {
        if (find(x, loc)) {
            return true;
        }
        if (((Node) nodes.elementAt(loc[0])).getXPos() > x) {
            loc[0]--;
        }
        return false;
    }

    private void p(String s) {
        System.out.println(s);
    }

    public void print() {
        Enumeration e = nodes.elements();
        int i = 0;
        while (e.hasMoreElements()) {
            Node n;
            n = (Node) e.nextElement();
            n.out(" node " + i + "     hidden: " + n.hidden());
            i++;
        }
    }

    public void print(int n) {
        p("---" + n);
        print();
    }

    public double getDiff() {
        return this.getMaxVal() - this.getMinVal();
    }

    public ValueGraphModel copySegment(double from, double to, boolean loopOn) {
        if (loopOn) {
            double diff = to - from;
            from = this.loopAt(from);
            to = from + diff;
        }
        this.updateBegEnd();
        ValueGraphModel toRet = new ValueGraphModel();
        toRet.construct(this.minVal, maxVal);
        Node[] bgn = this.getAdjacent(from);
        int at = -1;
        if (bgn.length == 1) {
            toRet.addNode(bgn[0].copyFrom(from));
            at = nodes.indexOf(bgn[0]) + 1;
        } else if (bgn.length == 2) {
            toRet.addNode(0, interpolate(from, bgn));
            at = nodes.indexOf(bgn[1]);
        }
        while (((Node) (nodes.elementAt(at))).getXPos() < to) {
            toRet.addNode(((Node) (nodes.elementAt(at))).copyFrom(from));
            at++;
        }
        if (((Node) (nodes.elementAt(at))).getXPos() == to) {
            toRet.addNode(((Node) (nodes.elementAt(at))).copyFrom(from));
        } else {
            toRet.addNode(to - from, interpolate(to, new Node[] { prev(at), (Node) nodes.elementAt(at) }));
        }
        toRet.updateBegEnd();
        return toRet;
    }

    public double interpolate(double at, Node[] nd) {
        if (nd.length == 1) return nd[0].getYPos();
        double grad = ((nd[1].getYPos() - nd[0].getYPos()) / (nd[1].getXPos() - nd[0].getXPos()));
        double dis = (at - nd[0].getXPos());
        return grad * dis + nd[0].getYPos();
    }

    /**	 * gets the value of the parameter at the specified position, looping it as	 * well, eg if at > looplen+loopst, then it is modulated by looplen	 * @param at position in beats	 * @return	 */
    public double getValAt(double at) {
        return interpolate(at, this.getAdjacent(at));
    }

    public double loopAt(double at) {
        double st = loopst.getValue();
        double len = looplen.getValue();
        if (at > st + len) at = (at - st) % len + st; else if (at < st) {
            at = ((at - st) % len + len) % len + st;
        }
        return at;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("value graph model:\n");
        for (int i = 0; i < nodes.size(); i++) {
            sb.append("node " + i + " x = " + ((Node) nodes.get(i)).getXPos() + " y = " + ((Node) nodes.get(i)).getYPos() + "\n");
        }
        return sb.toString();
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }
}
