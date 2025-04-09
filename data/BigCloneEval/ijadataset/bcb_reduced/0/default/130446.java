import java.lang.reflect.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/**
 * @author Massimo Bartoletti
 * @version 1.1
 */
public class CGTree extends JTree {

    private final String lineStyle = "Angled";

    private CGTutorial tutorial = null;

    public CGTree(CGTutorial tutorial) {
        this.tutorial = tutorial;
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Computational Geometry");
        createNodes(top);
        DefaultTreeModel model = (DefaultTreeModel) getModel();
        model.setRoot(top);
        putClientProperty("JTree.lineStyle", lineStyle);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
                if (node == null) return;
                CGDemoModule currentDemo = getTutorial().getDemo();
                if (currentDemo != null) currentDemo.dispose();
                Object nodeInfo = node.getUserObject();
                if (nodeInfo instanceof CGDemoModule) {
                    CGDemoModule demo = (CGDemoModule) nodeInfo;
                    getTutorial().setDemo(demo);
                    demo.preload();
                } else {
                    getTutorial().setDemo(null);
                }
            }
        });
    }

    private CGTutorial getTutorial() {
        return tutorial;
    }

    private void createNodes(DefaultMutableTreeNode top) {
        createDataStructuresNodes(top);
        createAlgorithmsNodes(top);
    }

    /************************************************************
	 *                    Data Structures
	 ************************************************************/
    private void createDataStructuresNodes(DefaultMutableTreeNode top) {
        DefaultMutableTreeNode dataStructures = new DefaultMutableTreeNode("Data Structures");
        top.add(dataStructures);
        DefaultMutableTreeNode dataStructure = null;
        CGDemoModule demo = null;
        demo = new CGDemoModule(getTutorial(), "Shape");
        dataStructure = new DefaultMutableTreeNode(demo);
        dataStructures.add(dataStructure);
        demo = new CGDemoModule(getTutorial(), "Point");
        dataStructure = new DefaultMutableTreeNode(demo);
        dataStructures.add(dataStructure);
        demo = new CGDemoModule(getTutorial(), "PointComparator");
        dataStructure = new DefaultMutableTreeNode(demo);
        dataStructures.add(dataStructure);
        demo = new CGDemoModule(getTutorial(), "Segment");
        dataStructure = new DefaultMutableTreeNode(demo);
        dataStructures.add(dataStructure);
        demo = new CGDemoModule(getTutorial(), "Ray");
        dataStructure = new DefaultMutableTreeNode(demo);
        dataStructures.add(dataStructure);
        demo = new CGDemoModule(getTutorial(), "HorizontalRay");
        dataStructure = new DefaultMutableTreeNode(demo);
        dataStructures.add(dataStructure);
        demo = new CGDemoModule(getTutorial(), "Polygon");
        dataStructure = new DefaultMutableTreeNode(demo);
        dataStructures.add(dataStructure);
        demo = new CGDemoModule(getTutorial(), "SimplePolygon");
        dataStructure = new DefaultMutableTreeNode(demo);
        dataStructures.add(dataStructure);
        demo = new CGDemoModule(getTutorial(), "ConvexPolygon");
        dataStructure = new DefaultMutableTreeNode(demo);
        dataStructures.add(dataStructure);
        demo = new CGDemoModule(getTutorial(), "Triangle");
        dataStructure = new DefaultMutableTreeNode(demo);
        dataStructures.add(dataStructure);
        demo = new CGDemoModule(getTutorial(), "Rectangle");
        dataStructure = new DefaultMutableTreeNode(demo);
        dataStructures.add(dataStructure);
        demo = new CGDemoModule(getTutorial(), "BSPTree");
        dataStructure = new DefaultMutableTreeNode(demo);
        dataStructures.add(dataStructure);
    }

    /************************************************************
	 *                      Algorithms
	 ************************************************************/
    private void createAlgorithmsNodes(DefaultMutableTreeNode top) {
        CGDemoModule demo = null;
        DefaultMutableTreeNode problems = new DefaultMutableTreeNode("Algorithms");
        top.add(problems);
        DefaultMutableTreeNode problem = null;
        DefaultMutableTreeNode subproblem = null;
        DefaultMutableTreeNode subsubproblem = null;
        DefaultMutableTreeNode algorithm = null;
        demo = loadDemo("BasicTests");
        problem = new DefaultMutableTreeNode(demo);
        problems.add(problem);
        demo = loadDemo("PolygonArea");
        algorithm = new DefaultMutableTreeNode(demo);
        problem.add(algorithm);
        demo = loadDemo("TwoSegmentIntersection");
        algorithm = new DefaultMutableTreeNode(demo);
        problem.add(algorithm);
        demo = loadDemo("ConvexHull");
        problem = new DefaultMutableTreeNode(demo);
        problems.add(problem);
        demo = loadDemo("JarvisMarch");
        algorithm = new DefaultMutableTreeNode(demo);
        problem.add(algorithm);
        demo = loadDemo("GrahamScan");
        algorithm = new DefaultMutableTreeNode(demo);
        problem.add(algorithm);
        demo = loadDemo("Triangulation");
        problem = new DefaultMutableTreeNode(demo);
        problems.add(problem);
        demo = loadDemo("SlowTriangulation");
        algorithm = new DefaultMutableTreeNode(demo);
        problem.add(algorithm);
        demo = loadDemo("EarCutTriangulation");
        algorithm = new DefaultMutableTreeNode(demo);
        problem.add(algorithm);
        demo = loadDemo("QuickEarCutTriangulation");
        algorithm = new DefaultMutableTreeNode(demo);
        problem.add(algorithm);
        demo = loadDemo("MonotoneTriangulation");
        algorithm = new DefaultMutableTreeNode(demo);
        problem.add(algorithm);
        demo = loadDemo("PointInPolygon");
        problem = new DefaultMutableTreeNode(demo);
        problems.add(problem);
        demo = loadDemo("PointInSimplePolygon");
        subproblem = new DefaultMutableTreeNode(demo);
        problem.add(subproblem);
        demo = loadDemo("RayCrossings");
        algorithm = new DefaultMutableTreeNode(demo);
        subproblem.add(algorithm);
        demo = loadDemo("PointInConvexPolygon");
        subsubproblem = new DefaultMutableTreeNode(demo);
        subproblem.add(subsubproblem);
        demo = loadDemo("SlowPointInConvexPolygon");
        algorithm = new DefaultMutableTreeNode(demo);
        subsubproblem.add(algorithm);
        demo = loadDemo("FastPointInConvexPolygon");
        algorithm = new DefaultMutableTreeNode(demo);
        subsubproblem.add(algorithm);
        demo = loadDemo("SegmentIntersection");
        problem = new DefaultMutableTreeNode(demo);
        problems.add(problem);
        demo = loadDemo("AnySegmentIntersection");
        subproblem = new DefaultMutableTreeNode(demo);
        problem.add(subproblem);
        demo = loadDemo("SlowAnySegmentIntersection");
        algorithm = new DefaultMutableTreeNode(demo);
        subproblem.add(algorithm);
        demo = loadDemo("SweepingLineAnySegmentIntersection");
        algorithm = new DefaultMutableTreeNode(demo);
        subproblem.add(algorithm);
        demo = loadDemo("AllSegmentIntersections");
        subproblem = new DefaultMutableTreeNode(demo);
        problem.add(subproblem);
        demo = loadDemo("SlowAllSegmentIntersections");
        algorithm = new DefaultMutableTreeNode(demo);
        subproblem.add(algorithm);
        demo = loadDemo("SweepingLineAllSegmentIntersections");
        algorithm = new DefaultMutableTreeNode(demo);
        subproblem.add(algorithm);
        demo = loadDemo("HVSegmentsWindowing");
        problem = new DefaultMutableTreeNode(demo);
        problems.add(problem);
        demo = loadDemo("SlowHVSegmentsWindowing");
        algorithm = new DefaultMutableTreeNode(demo);
        problem.add(algorithm);
        demo = loadDemo("FastHVSegmentsWindowing");
        algorithm = new DefaultMutableTreeNode(demo);
        problem.add(algorithm);
        demo = loadDemo("RayCrossSegmentSet");
        problem = new DefaultMutableTreeNode(demo);
        problems.add(problem);
        demo = loadDemo("SlowRayCrossSegmentSet");
        algorithm = new DefaultMutableTreeNode(demo);
        problem.add(algorithm);
        demo = loadDemo("BSPTreeRayCrossSegmentSet");
        algorithm = new DefaultMutableTreeNode(demo);
        problem.add(algorithm);
        demo = loadDemo("DelaunayTriangulation");
        problem = new DefaultMutableTreeNode(demo);
        problems.add(problem);
        demo = loadDemo("Knuth");
        algorithm = new DefaultMutableTreeNode(demo);
        problem.add(algorithm);
        demo = loadDemo("VisibilityGraph");
        problem = new DefaultMutableTreeNode(demo);
        problems.add(problem);
        demo = loadDemo("DijkstraVisibility");
        algorithm = new DefaultMutableTreeNode(demo);
        problem.add(algorithm);
        demo = loadDemo("MeshGeneration");
        problem = new DefaultMutableTreeNode(demo);
        problems.add(problem);
        demo = loadDemo("NonUniformMeshGeneration");
        algorithm = new DefaultMutableTreeNode(demo);
        problem.add(algorithm);
        demo = loadDemo("VoronoiDiagram");
        problem = new DefaultMutableTreeNode(demo);
        problems.add(problem);
        demo = loadDemo("Fortune");
        algorithm = new DefaultMutableTreeNode(demo);
        problem.add(algorithm);
        demo = loadDemo("PointLocation");
        problem = new DefaultMutableTreeNode(demo);
        problems.add(problem);
        demo = loadDemo("Kirkpatrick");
        algorithm = new DefaultMutableTreeNode(demo);
        problem.add(algorithm);
        demo = loadDemo("ChainsMethod");
        algorithm = new DefaultMutableTreeNode(demo);
        problem.add(algorithm);
        demo = loadDemo("Rendering3D");
        problem = new DefaultMutableTreeNode(demo);
        problems.add(problem);
        demo = loadDemo("BSPPainter");
        algorithm = new DefaultMutableTreeNode(demo);
        problem.add(algorithm);
        demo = loadDemo("RectanglesUnion");
        problem = new DefaultMutableTreeNode(demo);
        problems.add(problem);
        demo = loadDemo("MeasureAndPerimeterRectanglesUnion");
        algorithm = new DefaultMutableTreeNode(demo);
        problem.add(algorithm);
    }

    public CGDemoModule loadDemo(String resourceName) {
        CGTutorial tutorial = getTutorial();
        CGDemoModule demo = null;
        try {
            String demoName = tutorial.getString(resourceName + ".class");
            Class demoClass = Class.forName(demoName);
            Constructor demoConstructor = demoClass.getConstructor(new Class[] { tutorial.getClass() });
            Object[] args = new Object[] { tutorial };
            demo = (CGDemoModule) demoConstructor.newInstance(args);
        } catch (Exception ex) {
            ex.printStackTrace();
            tutorial.setStatus("Cannot load demo: " + ex);
            System.err.println("Hint: check if each line in manifest ends with a space, and that computational.jar is updated!");
        }
        return demo;
    }
}
