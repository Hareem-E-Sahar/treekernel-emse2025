package PRISM.RobotCtrl.Activities;

import java.util.Vector;
import PRISM.RobotCtrl.DataSet;
import PRISM.RobotCtrl.RangeDataSet;
import PRISM.RobotCtrl.VRWEvent;

public class RangeFinderDriver extends SensorDriver {

    int data[] = null;

    int beginRangeObstacle = -1;

    int endRangeObstacle = -1;

    int beginIndexObstacle = -1;

    int endIndexObstacle = -1;

    int beginRangeOpening = -1;

    int endRangeOpening = -1;

    int beginIndexOpening = -1;

    int endIndexOpening = -1;

    boolean senseObject = false;

    public RangeFinderDriver(String strName, String strDescription, DataSet ds) {
        super(strName, strDescription, ds);
        System.out.println("RangeFinderDriver ctor...");
    }

    public Vector<VRWEvent> process() {
        RangeDataSet ds = (RangeDataSet) getDataSet();
        int data[] = ds.getData();
        int resolution = ds.getResolution();
        boolean observation = false;
        beginIndexObstacle = -1;
        endIndexObstacle = -1;
        for (int i = 1; i < getDataSet().getLength(); i++) {
            if ((data[i] < (data[i - 1] - 100)) && (beginIndexObstacle == -1)) {
                beginRangeObstacle = data[i];
                beginIndexObstacle = i;
            }
            if ((Math.abs(data[i] - beginRangeObstacle) > 20) && (beginIndexObstacle != -1)) {
                endRangeObstacle = data[i - 1];
                endIndexObstacle = i - 1;
            } else continue;
            long distance = (beginRangeObstacle + endRangeObstacle) / 2;
            double width = endIndexObstacle - beginIndexObstacle;
            width = Math.sin(width * resolution * 3.1416 / 1800) * distance;
            if ((width < 5) || (width > 100)) {
                beginRangeObstacle = -1;
                endRangeObstacle = -1;
                beginIndexObstacle = -1;
                endIndexObstacle = -1;
                continue;
            }
            int middleIndex = (beginIndexObstacle + endIndexObstacle) / 2;
            int bearing = middleIndex * ds.getResolution();
            if (!ds.isClockwiseData()) {
                bearing = ds.getStartAngle() + bearing;
            } else bearing = ds.getStartAngle() + ds.getLength() * ds.getResolution() - bearing;
            observation = true;
        }
        if (observation) {
            senseObject = true;
        } else {
            if (senseObject) getObjectTracker().lost("unknown");
            senseObject = false;
        }
        return super.process();
    }
}
