import java.util.*;
import java.awt.*;
import java.applet.*;
import java.io.*;
import vrml.external.field.*;
import vrml.external.Node;
import vrml.external.Browser;
import vrml.external.exception.*;
import netscape.security.*;

/***
* GraphicalLogAnalyzer Class is a prototype of a type of EAI applet that
* can be used to graphically depict log entries
* written by Lindsey Lack
* course NPS MV4205
*/
public class GraphicalLogAnalyzer extends Applet implements EventOutObserver {

    TextArea output = null;

    boolean error = false;

    float increment = 0;

    private Browser browser;

    private Node root;

    EventInMFNode addChildren;

    private EventInSFVec3f transformTranslation;

    float[] position = { 0, 0, 0 };

    EventOutSFTime touchTime = null;

    private Vector records;

    protected int getBrowserIndexParameter() {
        try {
            return Integer.parseInt(getParameter("BrowserIndex"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void init() {
        add(new Button("Add Row"));
        add(new Button("Get File"));
        output = new TextArea(5, 40);
        add(output);
    }

    public void start() {
        output.append("\nGraphicalLogAnalyzer starting\n");
        int index = getBrowserIndexParameter();
        for (int i = 0; i < 20; i++) {
            browser = (Browser) vrml.external.Browser.getBrowser(this, null, index);
            if (browser != null) break;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                break;
            }
        }
        output.append("GraphicalLogAnalyzer sucessfully acquired browser object\n");
        try {
            root = browser.getNode("ROOT");
            addChildren = (EventInMFNode) root.getEventIn("addChildren");
            transformTranslation = (EventInSFVec3f) (root.getEventIn("translation"));
        } catch (InvalidNodeException e) {
            output.append("PROBLEMS!: " + e + "\n");
            error = true;
        } catch (InvalidEventInException e) {
            output.append("PROBLEMS!: " + e + "\n");
            error = true;
        } catch (InvalidVrmlException e) {
            output.append("PROBLEMS!: " + e + "\n");
            error = true;
        }
        if (error == false) output.append("Initialization OK.\n");
    }

    public void callback(EventOut who, double when, Object which) {
        output.append("Object clicked:\n");
        JVShape calledShape = (JVShape) which;
        output.append(calledShape.sayHello() + "\n");
    }

    private void createShape(int row, double size, int color, int shape) {
        JVShape newShape = new JVShape(size, color, shape);
        Node sensor = newShape.getTouchNode();
        touchTime = (EventOutSFTime) sensor.getEventOut("touchTime");
        touchTime.advise(this, newShape);
        Node[] item = newShape.getShape();
        Node[] scene = browser.createVrmlFromString("Transform {translation " + (row - 1) + " 0 " + -increment + "}");
        EventInMFNode nodesIn = (EventInMFNode) scene[0].getEventIn("addChildren");
        nodesIn.setValue(item);
        addChildren.setValue(scene);
    }

    private void incrementShapes() {
        increment = increment - (float) 0.25;
        position[2] = position[2] - (float) 0.25;
        transformTranslation.setValue(position);
    }

    public boolean action(Event event, Object what) {
        if (event.target instanceof Button) {
            Button b = (Button) event.target;
            if (b.getLabel() == "Add Row") {
                for (int i = 0; i < 3; i++) {
                    createShape(i, Math.random(), (int) (Math.random() * 3 + 1), (int) (Math.random() * 2 + 1));
                }
                incrementShapes();
            } else if (b.getLabel() == "Get File") {
                records = new Vector();
                try {
                    PrivilegeManager privilegeManager = PrivilegeManager.getPrivilegeManager();
                    privilegeManager.enablePrivilege("UniversalFileAccess");
                    BufferedReader in = new BufferedReader(new FileReader("C:/GraphicalLogAnalyzerInputFile.txt"));
                    String oneRecord;
                    while ((oneRecord = in.readLine()) != null) {
                        records.addElement(new String(oneRecord));
                    }
                } catch (EOFException eofex) {
                    output.append("No more record in file\n");
                } catch (Exception ex) {
                    output.append("Error while reading from file\n");
                    output.append("Error is:\n" + ex);
                }
                output.append("# Records: " + records.size() + "\n");
                for (int r = 0; r < records.size(); r++) {
                    String test = (String) records.elementAt(r);
                    StringTokenizer st = new StringTokenizer(test);
                    for (int i = 0; i < 3; i++) {
                        int tempSizeInt = 0;
                        double tempSize = 0;
                        int tempColor = 0;
                        int tempShape = 0;
                        String tempSizeS = "";
                        String tempColorS = "";
                        String tempShapeS = "";
                        try {
                            if (st.hasMoreTokens()) {
                                tempSizeS = st.nextToken();
                                tempSizeInt = Integer.parseInt(tempSizeS);
                                tempSize = tempSizeInt / (double) 100.0;
                            } else output.append("Token error");
                            if (st.hasMoreTokens()) {
                                tempColorS = st.nextToken();
                                tempColor = Integer.parseInt(tempColorS);
                            } else output.append("Token error");
                            if (st.hasMoreTokens()) {
                                tempShapeS = st.nextToken();
                                tempShape = Integer.parseInt(tempShapeS);
                            } else output.append("Token error");
                        } catch (NumberFormatException nfe) {
                            output.append("Num format exception");
                        }
                        createShape(i, tempSize, tempColor, tempShape);
                    }
                    incrementShapes();
                }
            }
        }
        return true;
    }

    class JVShape {

        private float height;

        private int color;

        private int shapeType;

        private Node[] shape;

        private Node[] touchNode;

        private String colorDesc;

        public JVShape(double inHeight, int inColor, int inShapeType) {
            height = (float) inHeight;
            color = inColor;
            shapeType = inShapeType;
            String shapeStr;
            String sizeStr;
            String colorStr;
            switch(color) {
                case 0:
                    colorStr = "0.8 0.8 0.2";
                    colorDesc = "Yellow";
                    break;
                case 1:
                    colorStr = "0.2 0.8 0.2";
                    colorDesc = "Green";
                    break;
                case 2:
                    colorStr = "0.8 0.2 0.2";
                    colorDesc = "Red";
                    break;
                default:
                    colorStr = "0.8 0.8 0.2";
                    colorDesc = "Yellow";
                    break;
            }
            switch(shapeType) {
                case 0:
                    shapeStr = "Box";
                    sizeStr = "size 0.2 " + height + " 0.2";
                    break;
                case 1:
                    shapeStr = "Cone";
                    sizeStr = "bottomRadius 0.1\n height " + height;
                    break;
                default:
                    shapeStr = "Box";
                    sizeStr = "size 0.2 " + height + " 0.2";
                    break;
            }
            String newNodeStr = "Transform {translation 0 " + height / 2 + " 0	\n" + "  children [									\n" + "	Shape {										\n" + "	  appearance Appearance {					\n" + "	    material Material {						\n" + "	      diffuseColor " + colorStr + "\n" + "	    }										\n" + "	  }											\n" + "	  geometry " + shapeStr + " {					\n" + sizeStr + "\n" + "	  }											\n" + "	}											\n" + "  ]											\n" + "}												\n";
            touchNode = browser.createVrmlFromString("TouchSensor {}");
            shape = browser.createVrmlFromString(newNodeStr);
            EventInMFNode nodesIn = (EventInMFNode) shape[0].getEventIn("addChildren");
            nodesIn.setValue(touchNode);
        }

        public Node[] getShape() {
            return shape;
        }

        public Node getTouchNode() {
            return touchNode[0];
        }

        public String sayHello() {
            return ("Object color: " + colorDesc + "  Object height: " + height);
        }
    }
}
