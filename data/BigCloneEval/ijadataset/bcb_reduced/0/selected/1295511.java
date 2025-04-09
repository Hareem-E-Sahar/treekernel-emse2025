package org.primordion.xholon.samples;

import java.util.Vector;
import org.primordion.xholon.base.IXholon;
import org.primordion.xholon.base.XholonWithPorts;
import org.primordion.xholon.util.IJavaTypes;
import org.primordion.xholon.util.Misc;

/**
 * Collisions. This is the detailed behavior of a sample Xholon application.
 * @author <a href="mailto:ken@primordion.com">Ken Webb</a>
 * @see <a href="http://www.primordion.com/Xholon">Xholon Project website</a>
 * @since 0.1 (Created on August 11, 2005)
 */
public class XhCollisions extends XholonWithPorts {

    private static final int P_POP = 0;

    private static final int P_POP_TOTAL = 1;

    public int val = 0;

    public float rate = 0.0f;

    public float proportionLicensed = 0.0f;

    public int avgVehicleKilometers = 0;

    public String roleName = null;

    /** Constructor. */
    public XhCollisions() {
    }

    public void initialize() {
        super.initialize();
        val = 0;
        rate = 0.0f;
        proportionLicensed = 0.0f;
        avgVehicleKilometers = 0;
        roleName = null;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public float getProportionLicensed() {
        return proportionLicensed;
    }

    public void setProportionLicensed(float proportionLicensed) {
        this.proportionLicensed = proportionLicensed;
    }

    public int getAvgVehicleKilometers() {
        return avgVehicleKilometers;
    }

    public void setAvgVehicleKilometers(int avgVehicleKilometers) {
        this.avgVehicleKilometers = avgVehicleKilometers;
    }

    public double getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public int setAttributeVal(String attrName, String attrVal) {
        int classType = super.setAttributeVal(attrName, attrVal);
        if (classType == IJavaTypes.JAVACLASS_UNKNOWN) {
            if ("rate".equals(attrName)) {
                rate = Misc.atof(attrVal, 0);
                classType = IJavaTypes.JAVACLASS_float;
            } else if ("proportionLicensed".equals(attrName)) {
                proportionLicensed = Misc.atof(attrVal, 0);
                classType = IJavaTypes.JAVACLASS_float;
            } else if ("avgVehicleKilometers".equals(attrName)) {
                avgVehicleKilometers = Misc.atoi(attrVal, 0);
                classType = IJavaTypes.JAVACLASS_int;
            }
        }
        return classType;
    }

    public void configure() {
        if (xhc.getParentNode().getName().equals("DriverGmGf")) {
            if (!(((XhCollisions) parentNode).getXhc().getName().equals("Drivers"))) {
                String targetBecName = "Pop" + ((XhCollisions) parentNode).getXhc().getName().substring(6);
                IXholon node = getXPath().evaluate("../../../Populations/" + targetBecName + "/PopGmGf", this);
                if (xhc.getName().equals("DriverGm")) {
                    port[P_POP] = (XhCollisions) node;
                } else {
                    port[P_POP] = (XhCollisions) node.getNextSibling();
                }
                node = getXPath().evaluate("../../../Populations/PopA6599", this);
                node = node.getNextSibling();
                if (xhc.getName().equals("DriverGm")) {
                    port[P_POP_TOTAL] = (XhCollisions) node;
                } else {
                    port[P_POP_TOTAL] = (XhCollisions) node.getNextSibling();
                }
            }
        }
        super.configure();
    }

    public void preAct() {
        int i;
        if (xhc.getParentNode().getName().equals("PopGmGf")) {
            if (((XhCollisions) parentNode).getXhc().getName().equals("Populations")) {
                int totalPop = 0;
                Vector v = parentNode.getChildNodes(false);
                XhCollisions node;
                for (i = 0; i < 7; i++) {
                    node = (XhCollisions) v.elementAt(i);
                    if (xhc.getName().equals("PopGm")) {
                        node = (XhCollisions) node.getFirstChild();
                    } else {
                        node = (XhCollisions) node.getFirstChild().getNextSibling();
                    }
                    totalPop += node.val;
                }
                val = totalPop;
            }
        }
        super.preAct();
    }

    public void act() {
        if (xhc.getParentNode().getName().equals("PopGmGf")) {
            val += val * rate;
        }
        super.act();
    }

    public String toString() {
        String outStr = getName();
        if ((port != null) && (port.length > 0)) {
            outStr += " [";
            for (int i = 0; i < port.length; i++) {
                if (port[i] != null) {
                    outStr += " port:" + port[i].getName();
                }
            }
            outStr += "]";
        }
        outStr += " ";
        if (xhc.hasAncestor("Population")) {
            outStr += " [val:" + val + " rate:" + rate + "]";
        } else if (xhc.hasAncestor("Driver")) {
            outStr += " [proportionLicensed:" + proportionLicensed + " avgVehicleKilometers:" + avgVehicleKilometers + "]";
        } else if (xhc.getName().equals("Scenario")) {
            outStr += " [rate:" + rate + "]";
        }
        return outStr;
    }

    /**
	 * Utility function to determine the rate that will convert startVal into expectedEndVal.
	 * @param startVal Starting value.
	 * @param expectedEndVal Expected end value.
	 * @param numYears Number of years or other units of time.
	 * @return The rate.
	 */
    private float converge(int startVal, int expectedEndVal, int numYears) {
        int TOLERANCE = 2;
        float LOW_RATE = 0.0f;
        float HIGH_RATE = 0.1f;
        float rate = (LOW_RATE + HIGH_RATE) / 2;
        float prevLowRate = LOW_RATE;
        float prevHighRate = HIGH_RATE;
        int computedEndVal;
        boolean done = false;
        boolean isNeg = false;
        if (startVal > expectedEndVal) {
            isNeg = true;
            int temp = startVal;
            startVal = expectedEndVal;
            expectedEndVal = temp;
        }
        while (!done) {
            computedEndVal = startVal;
            for (int i = 0; i < numYears; i++) {
                computedEndVal += computedEndVal * rate;
            }
            if (Math.abs(computedEndVal - expectedEndVal) > TOLERANCE) {
                if (computedEndVal > expectedEndVal) {
                    prevHighRate = rate;
                    rate = (prevLowRate + rate) / 2;
                } else {
                    prevLowRate = rate;
                    rate = (rate + prevHighRate) / 2;
                }
            } else {
                done = true;
            }
        }
        if (isNeg) {
            rate *= -1.0f;
        }
        return rate;
    }

    /**
	 * main
	 * @param args none
	 */
    public static void main(String[] args) {
        XhCollisions col = new XhCollisions();
        System.out.println(col.converge(4038600, 4343300, 25));
        System.out.println(col.converge(3831300, 4108300, 25));
        System.out.println();
        System.out.println(col.converge(1066900, 1065800, 25));
        System.out.println(col.converge(1023200, 1022100, 25));
        System.out.println();
        System.out.println(col.converge(2195300, 2410700, 25));
        System.out.println(col.converge(2143200, 2343900, 25));
        System.out.println();
        System.out.println(col.converge(2656400, 2617400, 25));
        System.out.println(col.converge(2628900, 2565300, 25));
        System.out.println();
        System.out.println(col.converge(2236600, 2503400, 25));
        System.out.println(col.converge(2251000, 2447700, 25));
        System.out.println();
        System.out.println(col.converge(1432400, 2500000, 25));
        System.out.println(col.converge(1478800, 2500200, 25));
        System.out.println();
        System.out.println(col.converge(1677200, 3666200, 25));
        System.out.println(col.converge(2253900, 4345900, 25));
    }
}
