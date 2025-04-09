package de.mogwai.layout;

import de.mogwai.layout.graph.Element;
import de.mogwai.layout.graph.Spring;
import java.awt.Dimension;
import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Layout based on physics.
 * <p/>
 * Layout based on physics of elements and springs. Every element
 * has an electric load, making elements moving away from each other.
 * Springs make elements move together. The whole model as a mass and a center
 * of gravity preventing the elements from moving to far away from the center
 * <p/>
 * The following laws are used:
 * <p/>
 * Coulombs law: F = K x Q1 x Q1 / r^2
 * <p/>
 * Hookes law : F = -D x L
 * <p/>
 * Newtons Law of Gravity: * F = -G x m1 x m2 / r^2
 * <p/>
 * <p/>
 *
 * @param <T> the element type
 * @param <U> the user type for springs.
 * @author msertic
 */
public abstract class ElectricSpringLayout<T extends Element, U extends Object> {

    private static final double GRAVITY_CONSTANT = -1;

    private static final double SPRING_FORCE_FACTOR = -1;

    private static final double ELECTRIC_FIELD_CONSTANT = 1;

    private static final double FORCE_DIVISOR = 800;

    public ElectricSpringLayout() {
    }

    public abstract List<T> getElements();

    public abstract List<Spring<U, T>> getSprings();

    public boolean preEvolveLayout() {
        return true;
    }

    public Map<T, Force> evolveLayout() {
        List<T> theElements = getElements();
        List<Spring<U, T>> theSprings = getSprings();
        Map<T, Force> theForces = new HashMap<T, Force>();
        double theTotalMass = 0;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (T theElement : theElements) {
            Dimension theSize = theElement.getSize();
            theTotalMass += theSize.width * theSize.height;
            Point theForcePoint = theElement.getForcePoint();
            minX = Math.min(minX, theForcePoint.x);
            minY = Math.min(minY, theForcePoint.y);
            maxX = Math.max(maxX, theForcePoint.x);
            maxX = Math.max(maxY, theForcePoint.y);
        }
        int massCenterX = minX + (maxX - minX) / 2;
        int massCenterY = minY + (maxY - minY) / 2;
        for (T theElement : theElements) {
            Force theCurrentForce = theForces.get(theElement);
            if (theCurrentForce == null) {
                theCurrentForce = new Force();
                theForces.put(theElement, theCurrentForce);
            }
            Distance theGravityDistance = theElement.computeDistanceTo(new Point(massCenterX, massCenterY));
            Dimension theElementSize = theElement.getSize();
            double theGravityDistanceC = theGravityDistance.computeDistance();
            double theGravityForce = GRAVITY_CONSTANT * theTotalMass * theElementSize.width * theElementSize.height / (theGravityDistanceC * theGravityDistanceC);
            double theGravityAngle = theGravityDistance.computeAngle();
            theCurrentForce.addForce(Math.cos(theGravityAngle) * theGravityForce, Math.sin(theGravityAngle) * theGravityForce);
            double theLoad = computeElectricLoad(theElement);
            for (T theOtherElement : theElements) {
                if (theOtherElement != theElement) {
                    Distance theDistance = theElement.computeDistanceTo(theOtherElement);
                    double theRealDistance = theDistance.computeDistance();
                    if (theElement.overlaps(theOtherElement)) {
                        double theForce = -theElement.computeRadius() / 2;
                        double theAngle = Math.toRadians(Math.random() * 360);
                        if (theRealDistance >= 1) {
                            theAngle = theDistance.computeAngle();
                        }
                        theCurrentForce.addForce(Math.cos(theAngle) * theForce, Math.sin(theAngle) * theForce);
                    } else {
                        double theOtherLoad = computeElectricLoad(theOtherElement);
                        double theForce = computeRepulsion(theLoad, theRealDistance, theOtherLoad);
                        theForce = -theForce;
                        double theAngle = theDistance.computeAngle();
                        theCurrentForce.addForce(Math.cos(theAngle) * theForce, Math.sin(theAngle) * theForce);
                    }
                }
            }
            for (Spring<U, T> theSpring : theSprings) {
                T theOtherElement = null;
                if (theSpring.getFrom() == theElement) {
                    theOtherElement = theSpring.getTo();
                }
                if (theSpring.getTo() == theElement) {
                    theOtherElement = theSpring.getFrom();
                }
                if (theOtherElement != null) {
                    double theOtherLoad = computeElectricLoad(theOtherElement);
                    Distance theDistance = theElement.computeDistanceTo(theOtherElement);
                    double theRealDistance = theDistance.computeDistance();
                    double theForce = -SPRING_FORCE_FACTOR * theRealDistance * 0.5;
                    theForce = theForce / FORCE_DIVISOR;
                    double theAngle = theDistance.computeAngle();
                    theCurrentForce.addForce(Math.cos(theAngle) * theForce, Math.sin(theAngle) * theForce);
                }
            }
        }
        for (Map.Entry<T, Force> theEntry : theForces.entrySet()) {
            Point theOld = theEntry.getKey().getLocation();
            Force theForce = theEntry.getValue();
            int mx = (int) theForce.fx;
            int my = (int) theForce.fy;
            if ((Math.abs(mx) >= 1) || (Math.abs(my) >= 1)) {
                theOld.x += mx;
                theOld.y += my;
                evolvePosition(theEntry.getKey(), mx, my);
            }
        }
        return theForces;
    }

    public double computeRepulsion(double aLoad, double aRealDistance, double aOtherLoad) {
        double theForce = ELECTRIC_FIELD_CONSTANT * aLoad * aOtherLoad / (aRealDistance * aRealDistance) * 0.5;
        theForce = theForce / FORCE_DIVISOR;
        return theForce;
    }

    public double computeElectricLoad(T aElement) {
        Dimension theSize = aElement.getSize();
        return theSize.getWidth() * theSize.getHeight();
    }

    public abstract void evolvePosition(T aElement, int aMovementX, int aMovementY);

    public void postEvolveLayout() {
    }
}
