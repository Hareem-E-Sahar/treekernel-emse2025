package org.hooliguns.ninja.telnet.teensyninja.test;

import java.util.Random;

/**
 * A simple stand-alone TeensyNinja modded Ninja interface that generates a new set of random
 * movements after previous is executed. A very helpful tool while setting com port and finding
 * corner cases.
 * 
 * @author Manish Pandya (March 1 2009)
 * 
 */
public class RandomMovementTestForTeensyNinja extends TestFrameworkForTeensyNinja {

    /**
   * A class that generates random co-ordinates and moves Ninja to them.
   * 
   * @author Manish Pandya (March 1 2009)
   * 
   */
    protected static class RandomMover implements Runnable {

        /**
     * the instance of test class
     */
        RandomMovementTestForTeensyNinja rm;

        /**
     * a typical constructor
     * 
     * @param rm
     *          the instance of the test class
     */
        protected RandomMover(RandomMovementTestForTeensyNinja rm) {
            this.rm = rm;
        }

        /**
     * Method that generates random movements and commands Ninja to perform them
     */
        void doMovements() {
            Random r = new Random();
            int x = 0;
            int y = 0;
            int v = 0;
            String retstr = new String(executeAndGetResponse("c", rm.out, rm.in));
            System.out.println(retstr);
            while (rm.keepOn) {
                while (isInMotion(rm.out, rm.in)) {
                    try {
                        Thread.sleep(1200);
                        System.out.print('.');
                    } catch (InterruptedException e) {
                    }
                }
                System.out.println();
                x = r.nextInt(XMAX - XMIN + 1) + XMIN;
                y = r.nextInt(YMAX - YMIN + 1) + YMIN;
                v = r.nextInt(VMAX + 1);
                System.out.printf("   x: %5d     y: %5d     v: %2d\n", x, y, v);
                retstr = new String(executeAndGetResponse("x" + x, rm.out, rm.in));
                System.out.println(retstr);
                retstr = new String(executeAndGetResponse("y" + y, rm.out, rm.in));
                System.out.println(retstr);
                retstr = new String(executeAndGetResponse("v" + v, rm.out, rm.in));
                System.out.println(retstr);
                retstr = new String(executeAndGetResponse("m", rm.out, rm.in));
                System.out.println(retstr);
            }
        }

        public void run() {
            doMovements();
        }
    }

    /**
   * A typical main method that starts up the test
   * 
   * @param args
   *          first string (arg[0]) is the name of the com port
   */
    public static void main(String[] args) {
        try {
            RandomMovementTestForTeensyNinja rm = new RandomMovementTestForTeensyNinja();
            rm.connect(args[0], new RandomMover(rm));
            Runtime.getRuntime().addShutdownHook(rm.shook);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
