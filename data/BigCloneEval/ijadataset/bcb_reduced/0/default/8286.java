import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jdsl.graph.algo.IntegerDijkstraPathfinder;
import jdsl.graph.api.Edge;
import jdsl.graph.api.EdgeIterator;
import jdsl.graph.api.Vertex;
import jdsl.graph.api.VertexIterator;
import jdsl.graph.ref.IncidenceListGraph;
import org.postgis.MultiLineString;
import org.postgis.Point;

public class MapGraph {

    private Connection _con;

    private PreparedStatement _pstmt;

    private String _sqlStr;

    private ResultSet _rs;

    private IncidenceListGraph _graph;

    private class MyVertex {

        int _nodeId;

        Point _point;

        public MyVertex(int nodeId, Point point) {
            _nodeId = nodeId;
            _point = point;
        }

        public int getNodeId() {
            return _nodeId;
        }

        public void setNodeId(int id) {
            _nodeId = id;
        }

        public Point getPoint() {
            return _point;
        }

        public void setPoint(Point _point) {
            this._point = _point;
        }

        public String toString() {
            return "Vertex " + _nodeId + " : (" + _point.getX() + "," + _point.getY() + ")";
        }
    }

    private class MyEdge {

        int _edgeId;

        double _length;

        MultiLineString _linePoints;

        public MyEdge(int edgeId, double length, MultiLineString linePoints) {
            _edgeId = edgeId;
            _length = length;
            _linePoints = linePoints;
        }

        public int getEdgeId() {
            return _edgeId;
        }

        public void setEdgeId(int edgeId) {
            _edgeId = edgeId;
        }

        public double getLength() {
            return _length;
        }

        public void setLength(double length) {
            _length = length;
        }

        public MultiLineString getLinePoints() {
            return _linePoints;
        }

        public void setLinePoints(MultiLineString points) {
            _linePoints = points;
        }

        public String toString() {
            return "Edge : " + _edgeId + " length : " + _length;
        }
    }

    private class GraphDijkstra extends IntegerDijkstraPathfinder {

        protected int weight(Edge e) {
            return (int) Math.round(((MyEdge) e.element()).getLength());
        }
    }

    public MapGraph() throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://localhost:5432/postgis";
        _con = DriverManager.getConnection(url, "postgres", "ibjk87");
        _graph = new IncidenceListGraph();
    }

    /**
	 * Termination. This method closes resources like statements, connections,
	 * etc. This method DOES NOT DELETE any data from the database.
	 */
    public void terminate() throws SQLException {
        if (_pstmt != null) _pstmt.close();
        _con.close();
        if (_rs != null) _rs.close();
    }

    public void buildGraph() {
        try {
            _sqlStr = "SELECT nodeid, AsText(the_geom) FROM is_jun";
            _pstmt = _con.prepareStatement(_sqlStr);
            _rs = _pstmt.executeQuery();
            while (_rs.next()) {
                _graph.insertVertex(new MyVertex(_rs.getInt(1), new Point(_rs.getString(2))));
            }
            _sqlStr = " SELECT str.fjunction, str.tjunction , str.objid ," + " 	ST_length_spheroid(str.the_geom,'SPHEROID[\"WGS 84\",6378137,298.257223563]' ), " + "   AsText(str.the_geom) " + " FROM is_str str , is_jun jun1, is_jun jun2 " + " WHERE str.fjunction =  jun1.nodeid  AND " + " 		str.tjunction = jun2.nodeid  ";
            _pstmt = _con.prepareStatement(_sqlStr);
            _rs = _pstmt.executeQuery();
            while (_rs.next()) {
                Vertex start = findVertex(_rs.getInt(1));
                Vertex end = findVertex(_rs.getInt(2));
                if (start != null && end != null) {
                    _graph.insertEdge(start, end, new MyEdge(_rs.getInt(3), _rs.getDouble(4), new MultiLineString(_rs.getString(5))));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Vertex findVertex(int vertexId) {
        VertexIterator iter = _graph.vertices();
        Vertex curr;
        while (iter.hasNext()) {
            curr = iter.nextVertex();
            if (((MyVertex) curr.element()).getNodeId() == vertexId) {
                return curr;
            }
        }
        return null;
    }

    public void getShortestPath(int source, int dest) {
        Vertex sourceVertex = findVertex(source);
        Vertex destVertex = findVertex(dest);
        GraphDijkstra dij = new GraphDijkstra();
        dij.execute(_graph, sourceVertex, destVertex);
        System.out.println(dij.distance(destVertex));
        EdgeIterator eIter = dij.reportPath();
        Vertex opposite = sourceVertex;
        while (eIter.hasNext()) {
            Edge currEdge = eIter.nextEdge();
            MyEdge edge = (MyEdge) currEdge.element();
            int numOfPoints = edge.getLinePoints().numPoints();
            System.out.println("numOfPoints : " + numOfPoints);
            int currPoint = 1;
            int delta = 1;
            if (!((MyVertex) opposite.element()).getPoint().equals(edge.getLinePoints().getFirstPoint())) {
                currPoint = numOfPoints - 2;
                delta = -1;
            }
            int i = 0;
            while (i < numOfPoints - 1) {
                System.out.println(edge.getLinePoints().getPoint(currPoint));
                currPoint += delta;
                i++;
            }
            opposite = _graph.opposite(opposite, currEdge);
        }
        exportPathToKml(dij, sourceVertex, destVertex);
    }

    private void exportPathToKml(GraphDijkstra dij, Vertex sourceVertex, Vertex destVertex) {
        try {
            File kmlFile = new File("ShortestPath.kml");
            PrintWriter fwriter = new PrintWriter(kmlFile);
            Point point;
            EdgeIterator eIter = dij.reportPath();
            Vertex opposite = sourceVertex;
            int counter = 1;
            fwriter.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            fwriter.print("<kml xmlns=\"http://earth.google.com/kml/2.2\">\n");
            fwriter.print("<Document>\n");
            fwriter.print("<name>Global Path Planning</name>\n");
            fwriter.print("<description>The estimated path of HANS</description>\n");
            fwriter.print("<ScreenOverlay>\n");
            fwriter.print("<name>Permafrost Legend</name>\n");
            fwriter.print("<color>aaffffff</color>\n");
            fwriter.print("<visibility>1</visibility>\n");
            fwriter.print("<Icon>\n");
            fwriter.print("<href>http://www.cs.huji.ac.il/~keren_ha/hans.jpg</href>\n");
            fwriter.print("</Icon>\n");
            fwriter.print("<overlayXY x=\"0\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/>\n");
            fwriter.print("<screenXY x=\"5\" y=\"5\" xunits=\"pixels\" yunits=\"insetPixels\"/>\n");
            fwriter.print("</ScreenOverlay>\n");
            fwriter.print("<Placemark> \n");
            fwriter.print("<name>Ross Building</name> \n");
            fwriter.print("   <description>Ross Building</description> \n");
            fwriter.print("    <Point> \n");
            fwriter.print("      <coordinates>35.1970416667,31.7750805556</coordinates> \n");
            fwriter.print("      </Point> \n");
            fwriter.print(" </Placemark> \n");
            fwriter.print(" <Placemark> \n");
            fwriter.print("<name>Belgium House</name> \n");
            fwriter.print("<description>Belgium House</description> \n");
            fwriter.print("<Point> \n");
            fwriter.print("<coordinates>35.1962530754,31.77487425</coordinates> \n");
            fwriter.print("</Point> \n");
            fwriter.print("</Placemark> \n");
            fwriter.print("<Placemark> \n");
            fwriter.print("<name>Jewish National and University Library</name> \n");
            fwriter.print("<description>Jewish National and University Library</description> \n");
            fwriter.print("<Point> \n");
            fwriter.print("<coordinates>35.1967426645,31.7759226715</coordinates> \n");
            fwriter.print("</Point> \n");
            fwriter.print("</Placemark> \n");
            point = ((MyVertex) sourceVertex.element()).getPoint();
            printPointToKml(fwriter, point, "Start Point");
            Point lastPoint = point;
            while (eIter.hasNext()) {
                Edge currEdge = eIter.nextEdge();
                MyEdge edge = (MyEdge) currEdge.element();
                int numOfPoints = edge.getLinePoints().numPoints();
                int currPoint = 0;
                int delta = 1;
                if (!((MyVertex) opposite.element()).getPoint().equals(edge.getLinePoints().getFirstPoint())) {
                    currPoint = numOfPoints - 1;
                    delta = -1;
                }
                int i = 0;
                while (i < numOfPoints) {
                    point = edge.getLinePoints().getPoint(currPoint);
                    if (!point.equals(lastPoint)) {
                        if (point.equals(((MyVertex) destVertex.element()).getPoint())) {
                            printPointToKml(fwriter, point, "Destination");
                        } else {
                            printPointToKml(fwriter, point, "" + counter);
                        }
                        ++counter;
                        lastPoint = point;
                    }
                    currPoint += delta;
                    i++;
                }
            }
            fwriter.print("  </Document> \n");
            fwriter.print("</kml>  \n");
            fwriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void printPointToKml(PrintWriter fwriter, Point point, String name) {
        fwriter.print("<Placemark>\n");
        fwriter.print("	<name>" + name + "</name>\n");
        fwriter.print("	<Point>\n");
        fwriter.print("		<coordinates>" + point.getX() + "," + point.getY() + "</coordinates>\n");
        fwriter.print("    </Point>\n");
        fwriter.print("</Placemark>\n");
    }
}
