package cn.edu.thss.iise.beehivez.server.petrinetwithdata;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.TransitionCluster;
import org.processmining.framework.ui.About;
import org.processmining.framework.util.StringNormalizer;
import att.grappa.Edge;
import att.grappa.Element;
import att.grappa.Grappa;
import att.grappa.GrappaLine;
import att.grappa.GrappaPoint;
import att.grappa.Node;
import att.grappa.Subgraph;

/**
 * change PnmlWriter from ProM
 * 
 * data operations are considered in Petri net.
 * In addition to the PNML schema, the following tags are supported as shown in the examples.
 * 
 * <data id="data_1">
 *   <name>x</name>
 * </data>
 * 
 * <datawriting>
 *   <writing>
 *     <transition>tran_1</transition>
 *     <data>data_1</data>
 *     <data>data_2</data>
 *   </writing>
 *   <writing>
 *     <transition>tran_2</transition>
 *     <data>data_2</data>
 *   </writing>
 * </datawriting>
 * 
 * <datareading>
 *   <reading>
 *     <transition>tran_1</transition>
 *     <data>data_1</data>
 *     <data>data_2</data>
 *   </reading>
 *   <reading>
 *     <transition>tran_2</transition>
 *     <data>data_2</data>
 *   </reading>
 * </datareading>
 * 
 * @author Tao Jin
 * 
 * @date 2012-4-27
 * 
 */
public class DPnmlWriter {

    public static final String basicPntdUri = "http://www.informatik.hu-berlin.de/top/pnml/basicPNML.rng";

    public static final String workflowPntdUri = "http://www.processmining.org/workflownet1.0";

    private static int Y;

    private static int X;

    private DPnmlWriter() {
    }

    public static synchronized void write(boolean PNKernel, boolean withSpline, PetriNetWithData dpn, BufferedWriter bw) throws IOException {
        PetriNet net = dpn.getPetriNet();
        ModelGraphPanel panel = net.getGrappaVisualization();
        Subgraph graph = panel.getSubgraph();
        Iterator it;
        int i = 0;
        bw.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
        bw.write("<pnml>\n");
        if (!PNKernel) {
            bw.write("<net id=\"workflownet\" type=\"" + basicPntdUri + "\">\n");
        } else {
            bw.write("<net id=\"workflownet\" type=\"PTNet\">\n");
        }
        Y = Math.abs((int) graph.getBoundingBox().getY());
        X = Math.abs((int) graph.getBoundingBox().getX());
        writePlaces(PNKernel, graph, bw, 0);
        writeTransitions(PNKernel, graph, bw, 0);
        writeArcs(withSpline, graph, bw, 0);
        writeVariables(dpn.getVariables(), bw, 0);
        writeDataWriting(dpn.getDataWriting(), bw);
        writeDataReading(dpn.getDataReading(), bw);
        if ((!PNKernel) && (net.getClusters() != null) && (net.getClusters().size() > 0)) {
            it = net.getClusters().iterator();
            bw.write("  <toolspecific tool=\"ProM\" version=\"" + About.VERSION + "\">\n");
            while (it.hasNext()) {
                TransitionCluster tc = (TransitionCluster) it.next();
                bw.write("    <cluster name=\"" + StringNormalizer.escapeXMLCharacters(tc.getLabel()) + "\">\n");
                Iterator it2 = tc.iterator();
                while (it2.hasNext()) {
                    Transition t = (Transition) it2.next();
                    bw.write("      <trans>trans_" + t.getNumber() + "</trans>\n");
                }
                bw.write("    </cluster>\n");
            }
            bw.write("  </toolspecific>\n");
        }
        bw.write("</net>\n");
        bw.write("</pnml>");
        graph.clearSubgraph();
        graph.clearElement();
        graph = null;
        panel.clearModelGraphPanel();
        panel.clearGrappaPanel();
        panel = null;
    }

    private static int writeArcs(boolean withSpline, Subgraph graph, BufferedWriter bw, int i) throws IOException {
        Enumeration e = graph.edgeElements();
        while (e.hasMoreElements()) {
            Edge edge = (Edge) e.nextElement();
            Node head = edge.getHead(), tail = edge.getTail();
            String source, target;
            if (edge.goesForward()) {
                source = tail.object instanceof Transition ? "trans_" + ((Transition) tail.object).getNumber() : "place_" + ((Place) tail.object).getNumber();
                target = head.object instanceof Transition ? "trans_" + ((Transition) head.object).getNumber() : "place_" + ((Place) head.object).getNumber();
            } else {
                source = head.object instanceof Transition ? "trans_" + ((Transition) head.object).getNumber() : "place_" + ((Place) head.object).getNumber();
                target = tail.object instanceof Transition ? "trans_" + ((Transition) tail.object).getNumber() : "place_" + ((Place) tail.object).getNumber();
            }
            bw.write("    <arc id=\"arc_" + i + "\" " + "source=\"" + source + "\" " + "target=\"" + target + "\">\n");
            if (withSpline) {
                GrappaLine line = (GrappaLine) edge.getAttributeValue(Grappa.POS_ATTR);
                GrappaPoint[] set = line.getPointSet();
                int index = 0, num = set.length;
                String tag;
                bw.write("        <toolspecific tool=\"ProM\" version=\"" + About.VERSION + "\">\n");
                bw.write("            <spline>\n");
                for (index = 0; index < set.length; index++) {
                    if (index == 0 && (line.getArrowType() & GrappaLine.HEAD_ARROW_EDGE) != 0) {
                        tag = "start";
                    } else if (index == set.length - 1 && (line.getArrowType() & GrappaLine.TAIL_ARROW_EDGE) != 0) {
                        tag = "end";
                    } else {
                        tag = "point";
                    }
                    bw.write("                <" + tag + " x=\"" + (((int) set[index].x) + X) + "\" " + "y=\"" + (((int) set[index].y) + Y) + "\" />\n");
                }
                bw.write("            </spline>\n");
                bw.write("        </toolspecific>\n");
            }
            bw.write("    </arc>\n");
            i++;
        }
        e = graph.subgraphElements();
        while (e.hasMoreElements()) {
            i = writeArcs(withSpline, (Subgraph) e.nextElement(), bw, i);
        }
        return i;
    }

    private static int writeVariables(ArrayList<DataItem> vars, BufferedWriter bw, int i) throws IOException {
        for (DataItem di : vars) {
            di.setId("data_" + i);
            bw.write("    <data id=\"data_" + i + "\">\n" + "        <name>" + di.getName() + "</name>\n" + "    </data>");
            bw.newLine();
            i++;
        }
        return i;
    }

    private static void writeDataWriting(HashMap<Transition, HashSet<DataItem>> datawriting, BufferedWriter bw) throws IOException {
        bw.write("    <datawriting>\n");
        for (Transition t : datawriting.keySet()) {
            bw.write("        <writing>\n");
            bw.write("            <transition>tran_" + t.getNumber() + "</transition>\n");
            HashSet<DataItem> dis = datawriting.get(t);
            for (DataItem di : dis) {
                bw.write("            <data>" + di.getId() + "</data>\n");
            }
            bw.write("        </writing>\n");
        }
        bw.write("    </datawriting>");
        bw.newLine();
    }

    private static void writeDataReading(HashMap<Transition, HashSet<DataItem>> datareading, BufferedWriter bw) throws IOException {
        bw.write("    <datareading>\n");
        for (Transition t : datareading.keySet()) {
            bw.write("        <reading>\n");
            bw.write("            <transition>tran_" + t.getNumber() + "</transition>\n");
            HashSet<DataItem> dis = datareading.get(t);
            for (DataItem di : dis) {
                bw.write("            <data>" + di.getId() + "</data>\n");
            }
            bw.write("        </reading>\n");
        }
        bw.write("    </datareading>");
        bw.newLine();
    }

    private static int writePlaces(boolean PNKernel, Subgraph graph, BufferedWriter bw, int i) throws IOException {
        Enumeration e = graph.nodeElements();
        while (e.hasMoreElements()) {
            Element el = (Element) e.nextElement();
            if (el.object != null && el.object instanceof Place) {
                Node n = (Node) el;
                Place p = (Place) el.object;
                int x = (int) n.getCenterPoint().getX();
                int y = (int) n.getCenterPoint().getY();
                double w = ((Double) n.getAttributeValue(Grappa.WIDTH_ATTR)).doubleValue();
                double h = ((Double) n.getAttributeValue(Grappa.HEIGHT_ATTR)).doubleValue();
                p.setNumber(i);
                i++;
                bw.write("    <place id=\"place_" + p.getNumber() + "\">\n" + "        <graphics>\n" + "            <position" + (PNKernel ? " " + (PNKernel ? " page=\"1\" " : "") + " " : "") + " x=\"" + (x + X) + "\" y=\"" + (y + Y) + "\" />\n" + "            <dimension x=\"" + ((int) (w * 72.0)) + "\" y=\"" + ((int) (h * 72.0)) + "\" />\n" + "        </graphics>\n");
                if (!PNKernel) {
                    bw.write("        <name>\n" + "            <text>" + StringNormalizer.escapeXMLCharacters(p.getIdentifier()) + "</text>\n" + "         </name>\n");
                    if (p.getNumberOfTokens() > 0) {
                        bw.write("        <initialMarking>\n" + "            <text>" + p.getNumberOfTokens() + "</text>\n" + "         </initialMarking>\n");
                    }
                }
                bw.write("    </place>");
                bw.newLine();
            }
        }
        e = graph.subgraphElements();
        while (e.hasMoreElements()) {
            i = writePlaces(PNKernel, (Subgraph) e.nextElement(), bw, i);
        }
        return i;
    }

    private static int writeTransitions(boolean PNKernel, Subgraph graph, BufferedWriter bw, int i) throws IOException {
        Enumeration e = graph.nodeElements();
        while (e.hasMoreElements()) {
            Element el = (Element) e.nextElement();
            if (el.object != null && el.object instanceof Transition) {
                Node n = (Node) el;
                Transition t = (Transition) el.object;
                int x = (int) n.getCenterPoint().getX();
                int y = (int) n.getCenterPoint().getY();
                double w = ((Double) n.getAttributeValue(Grappa.WIDTH_ATTR)).doubleValue();
                double h = ((Double) n.getAttributeValue(Grappa.HEIGHT_ATTR)).doubleValue();
                t.setNumber(i);
                i++;
                bw.write("    <transition id=\"trans_" + t.getNumber() + "\">\n" + "        <graphics>\n" + "            <position " + (PNKernel ? " page=\"1\" " : "") + " x=\"" + (x + X) + "\" y=\"" + (y + Y) + "\" />\n" + "            <dimension x=\"" + ((int) (w * 72.0)) + "\" y=\"" + ((int) (h * 72.0)) + "\" />\n" + "        </graphics>\n");
                if (PNKernel) {
                    String name;
                    if (t.getLogEvent() != null) {
                        name = t.getLogEvent().getModelElementName() + " (" + t.getLogEvent().getEventType() + ")";
                    } else {
                        name = t.getIdentifier();
                    }
                    bw.write("        <name>\n" + "            <graphics>\n" + "                <offset  page=\"1\" x=\"0\" y=\"0\" />\n" + "            </graphics>\n" + "            <value>" + StringNormalizer.escapeXMLCharacters(name) + "</value>\n" + "         </name>\n");
                }
                if (!PNKernel) {
                    bw.write("        <name>\n" + "            <text>" + t.getIdentifier() + "</text>\n" + "         </name>\n");
                    bw.write("        <toolspecific tool=\"ProM\" version=\"" + About.VERSION + "\">\n");
                    if (t.getLogEvent() != null) {
                        bw.write("            <logevent>\n" + "                <name>" + StringNormalizer.escapeXMLCharacters(t.getLogEvent().getModelElementName()) + "</name>\n" + "                <type>" + StringNormalizer.escapeXMLCharacters(t.getLogEvent().getEventType()) + "</type>\n" + "            </logevent>\n");
                    }
                    bw.write("        </toolspecific>\n");
                }
                bw.write("    </transition>");
                bw.newLine();
            }
        }
        e = graph.subgraphElements();
        while (e.hasMoreElements()) {
            i = writeTransitions(PNKernel, (Subgraph) e.nextElement(), bw, i);
        }
        return i;
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) {
    }
}
