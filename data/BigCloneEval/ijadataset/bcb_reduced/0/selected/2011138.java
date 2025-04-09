package molmaster.gui.input;

import molmaster.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.GraphicsConfiguration;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.behaviors.mouse.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.geometry.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.util.*;
import molmaster.*;
import molmaster.gui.*;

public class AttachPoints {

    public static Map bondMap = new HashMap();

    public static void attach(double x1, double y1, double z1, double x2, double y2, double z2, Group group, Atom start, Atom end, Bond bondIn) {
        TransformGroup tg = new TransformGroup();
        tg.setTransform(getTransform(x1, x2, y1, y2, z1, z2));
        tg.setCapability(tg.ALLOW_TRANSFORM_READ);
        tg.setCapability(tg.ALLOW_TRANSFORM_WRITE);
        tg.clearCapabilityIsFrequent(tg.ALLOW_TRANSFORM_READ);
        tg.clearCapabilityIsFrequent(tg.ALLOW_TRANSFORM_WRITE);
        if (bondIn == null) {
            bondIn = new Bond(start, end, Bond.BOND_TYPE_NORMAL);
        }
        BondView cylinder = new BondView(bondIn, tg);
        BranchGroup g = new BranchGroup();
        g.setCapability(g.ALLOW_DETACH);
        g.addChild(tg);
        group.addChild(g);
        bondMap.put(cylinder, g);
    }

    public static Transform3D getTransform(double x1, double x2, double y1, double y2, double z1, double z2) {
        Transform3D transform = new Transform3D();
        double cx = (x1 + x2) / 2;
        double cy = (y1 + y2) / 2;
        double cz = (z1 + z2) / 2;
        Vector3d vector = new Vector3d(cx, cy, cz);
        transform.setTranslation(vector);
        double dx = (double) (x2 - x1);
        double dy = (double) (y2 - y1);
        double dz = (double) (z2 - z1);
        float s = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        double rotZ = 0;
        if (dx == 0) {
            rotZ = 0;
        } else if (dy == 0) {
            rotZ = Math.PI / 2;
            if (dx < 0) {
                rotZ = -rotZ;
            }
        } else if (dx != 0) {
            rotZ = -Math.atan(Math.abs(dx) / Math.abs(dy));
            if (dy < 0) {
                if (dx > 0) {
                    rotZ = Math.PI + rotZ;
                } else {
                    rotZ = Math.PI * 3 - rotZ;
                }
            } else {
                if (dx > 0) {
                    rotZ = -rotZ;
                }
            }
        }
        rotZ = -rotZ;
        double degrees = rotZ * (180d / Math.PI);
        double rotX = Math.atan(Math.abs(z2 - cz) / (0.5 * Math.sqrt(dx * dx + dy * dy)));
        rotX = -rotX;
        if (z2 - cz > 0) {
            rotX = -rotX;
        }
        degrees = rotX * (180d / Math.PI);
        Transform3D rotationZ = new Transform3D();
        Transform3D rotationX = new Transform3D();
        rotationZ.rotZ(rotZ);
        rotationX.rotX(rotX);
        transform.mul(rotationZ);
        transform.mul(rotationX);
        return transform;
    }
}
