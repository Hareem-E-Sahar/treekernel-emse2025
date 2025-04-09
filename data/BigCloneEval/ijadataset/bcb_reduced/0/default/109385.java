import cytoscape.util.CytoscapeAction;
import cytoscape.Cytoscape;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.CyNetwork;
import javax.swing.JFileChooser;
import java.io.File;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

public class LoadFileAction extends CytoscapeAction {

    /**
     * The constructor sets the text that should appear on the menu item.
     */
    public LoadFileAction() {
        super("Load ReConn file...");
    }

    /**
     * This method is called when the user selects the menu item.
     */
    public void actionPerformed(ActionEvent ae) {
        CytoscapeDesktop desktop = Cytoscape.getDesktop();
        ArrayList reactomeIDs = new ArrayList();
        Hashtable edges = new Hashtable();
        JFileChooser chooser = new JFileChooser();
        int returnValue = chooser.showOpenDialog(desktop);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document xmlFile = db.parse(file);
                NodeList nodes = xmlFile.getElementsByTagName("Node");
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    NamedNodeMap values = node.getAttributes();
                    Node id = values.getNamedItem("id");
                    reactomeIDs.add(id.getNodeValue());
                }
                NodeList edgesList = xmlFile.getElementsByTagName("Edge");
                for (int i = 0; i < edgesList.getLength(); i++) {
                    Node edge = edgesList.item(i);
                    NamedNodeMap values = edge.getAttributes();
                    Node start = values.getNamedItem("start");
                    Node end = values.getNamedItem("end");
                    Node popular = values.getNamedItem("popular");
                    DaedalusEdge daedalusEdge = new DaedalusEdge(start.getNodeValue(), end.getNodeValue());
                    edges.put(daedalusEdge, popular.getNodeValue());
                }
                CyNetwork network = Cytoscape.createNetwork(file.getName());
                AbstractDatabaseConnect dbConnect = (AbstractDatabaseConnect) new DatabaseConnect();
                dbConnect.getReactionsByID(reactomeIDs, network.getIdentifier());
                dbConnect.addPathEdges(network, edges);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}
