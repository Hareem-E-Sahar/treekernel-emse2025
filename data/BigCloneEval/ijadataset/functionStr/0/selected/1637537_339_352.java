public class Test {        GraphConstants.setMoveable(edgeAttributes, graphics.getEdgeMovable());
        GraphConstants.setConnectable(edgeAttributes, graphics.getEdgeMovable());
        GraphConstants.setDisconnectable(edgeAttributes, graphics.getEdgeMovable());
        GraphConstants.setBendable(edgeAttributes, graphics.getEdgeMovable());
        GraphConstants.setSizeable(edgeAttributes, graphics.getEdgeMovable());
        GraphConstants.setEditable(edgeAttributes, graphics.getEdgeEditable());
        GraphConstants.setLineColor(edgeAttributes, graphics.getEdgeColor());
        Map attributes = new Hashtable();
        attributes.put(edge, edgeAttributes);
        ConnectionSet cs = new ConnectionSet();
        cs.connect(edge, portSource, portTarget);
        setEdgeInformation(edge, edge.getLabel(), "edge:" + reqQueryId + ":" + goal, goal, reqQueryId, query, answer, status, proof);
        edge.setSourceNode(nodeSource);
        edge.setTargetNode(nodeTarget);
}