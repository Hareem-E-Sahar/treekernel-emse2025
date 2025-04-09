package se.sics.cooja;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.apache.log4j.Logger;

/**
 * A positioner is used for determining initial positions of motes.
 *
 * @author Fredrik Osterlind
 */
public abstract class Positioner {

    private static Logger logger = Logger.getLogger(Positioner.class);

    /**
   * This method creates an instance of the given class with the given interval
   * information as constructor arguments. Instead of calling the constructors
   * directly this method may be used.
   *
   * @param positionerClass
   *          Positioner class
   * @param totalNumberOfMotes
   *          Total number of motes that should be generated using this
   *          positioner
   * @param startX
   *          Lowest X value of positions generated using returned positioner
   * @param endX
   *          Highest X value of positions generated using returned positioner
   * @param startY
   *          Lowest Y value of positions generated using returned positioner
   * @param endY
   *          Highest Y value of positions generated using returned positioner
   * @param startZ
   *          Lowest Z value of positions generated using returned positioner
   * @param endZ
   *          Highest Z value of positions generated using returned positioner
   * @return Postioner instance
   */
    public static final Positioner generateInterface(Class<? extends Positioner> positionerClass, int totalNumberOfMotes, double startX, double endX, double startY, double endY, double startZ, double endZ) {
        try {
            Constructor<? extends Positioner> constr = positionerClass.getConstructor(new Class[] { int.class, double.class, double.class, double.class, double.class, double.class, double.class });
            return constr.newInstance(new Object[] { totalNumberOfMotes, startX, endX, startY, endY, startZ, endZ });
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                logger.fatal("Exception when creating " + positionerClass + ": " + e.getCause());
            } else {
                logger.fatal("Exception when creating " + positionerClass + ": " + e.getMessage());
            }
            return null;
        }
    }

    /**
   * Returns the next mote position.
   *
   * @return Position
   */
    public abstract double[] getNextPosition();
}
