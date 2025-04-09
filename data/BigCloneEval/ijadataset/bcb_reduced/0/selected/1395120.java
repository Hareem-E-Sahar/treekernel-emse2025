package org.hooliguns.ninja.telnet.phiPiMod.test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

/**
 * A simple stand-alone PhiPi modded Ninja interface that generates random
 * movements every 6 seconds. A very helpful tool while setting com port. This
 * was also the first experiment before the whole server implementation was
 * written.
 * 
 * @author Manish Pandya (July 1 2008)
 * 
 */
public class RandomMovementTestForPhipiMods {

    /**
	 * maximum x position
	 */
    public static final int XMAX = 1800;

    /**
	 * minimum x position
	 */
    public static final int XMIN = -1800;

    /**
	 * maximum y position
	 */
    public static final int YMAX = 550;

    /**
	 * minimum y position
	 */
    public static final int YMIN = -1200;

    /**
	 * maximum velocity
	 */
    public static final int VMAX = 63;

    /**
	 * minimum velocity
	 */
    public static final int VMIN = 0;

    /**
	 * Should carry on? false will quit movement thread.
	 */
    private volatile boolean keepOn = true;

    /**
	 * The terminal output stream that we print co-ordinates to.
	 */
    private OutputStream out = null;

    /**
	 * the shutdown hook
	 */
    private ShutdownHook shook = null;

    /**
	 * The initialization method that sets up the comm port, adds shutdown hook
	 * and starts the random movement thread. It uses gnu.io from RXTX.org but can
	 * readily converted to use javax.comm API.
	 * 
	 * @param portName
	 *          the com port
	 * @throws Exception
	 *           when bad things happen
	 */
    void connect(String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        } else {
            CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);
            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(4800, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                out = serialPort.getOutputStream();
                shook = new ShutdownHook(this);
                (new Thread(new RandomMover(this))).start();
            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }

    /**
	 * A class that generates random co-ordinates and moves Ninja to them.
	 * 
	 * @author Manish Pandya (July 1 2008)
	 * 
	 */
    public static class RandomMover implements Runnable {

        /**
		 * the instance of test class
		 */
        RandomMovementTestForPhipiMods rm;

        /**
		 * a typical constructor
		 * 
		 * @param rm
		 *          the instance of the test class
		 */
        public RandomMover(RandomMovementTestForPhipiMods rm) {
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
            executeAndWait("c", rm.out);
            while (rm.keepOn) {
                x = r.nextInt(XMAX - XMIN + 1) + XMIN;
                y = r.nextInt(YMAX - YMIN + 1) + YMIN;
                v = r.nextInt(VMAX + 1);
                v = VMAX;
                String command = "x" + x + "y" + y + "v" + v + "m\r";
                System.out.printf("   x: %5d     y: %5d \n", x, y);
                executeAndWait(command, rm.out);
            }
        }

        public void run() {
            doMovements();
        }
    }

    /**
	 * A method that writes the randomly generated commands to the serial port and
	 * waits for about 6 seconds before returning.
	 * 
	 * @param command
	 *          the command to be written
	 * @param out
	 *          the com port output stream where command for ninja is to be
	 *          written
	 */
    private static void executeAndWait(String command, OutputStream out) {
        try {
            out.write(command.getBytes());
            try {
                Thread.sleep(6500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
	 * The shutdown hook that breaks the infinite loop of execution
	 * 
	 * @author Manish Pandya (July 1 2008)
	 * 
	 */
    public class ShutdownHook extends Thread {

        /**
		 * the test instance
		 */
        RandomMovementTestForPhipiMods rms;

        /**
		 * A method that breaks the execution loop and centers the unit as last
		 * execution
		 * 
		 * @param rms
		 *          the instance of the running test
		 */
        public ShutdownHook(RandomMovementTestForPhipiMods rms) {
            this.rms = rms;
        }

        @Override
        public void run() {
            System.out.println("Control-C caught. Shutting down...");
            keepOn = false;
            executeAndWait("c", rms.out);
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
            RandomMovementTestForPhipiMods rm = new RandomMovementTestForPhipiMods();
            rm.connect(args[0]);
            Runtime.getRuntime().addShutdownHook(rm.shook);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
