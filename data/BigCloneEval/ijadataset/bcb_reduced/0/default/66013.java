import java.io.File;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import net.gachet.vfshook.ModifiedFileSensor;
import net.gachet.vfshook.ModifiedPropertiesSensor;
import net.gachet.vfshook.Monitor;
import net.gachet.vfshook.NewFileSensor;
import net.gachet.vfshook.RemovedFileSensor;
import net.gachet.vfshook.SensorEvent;
import net.gachet.vfshook.SensorListener;

/**
 * Sample assembler class
 * @author  <a href="mailto:alexandre@gachet.net>Alexandre Gachet</a>
 * @version $Revision: 1.10 $ ($Date: 2005/09/09 09:15:03 $)
 */
public class VFSHook implements SensorListener {

    /** anonymous logger for this package */
    protected static Logger logger = Logger.getLogger("VFSHook");

    /** Creates a new instance of <code>GrooveHook</code> */
    public VFSHook() {
    }

    /**
     * Entry point
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            usage();
            System.exit(0);
        }
        VFSHook hook = new VFSHook();
        String cmd = args[0].toLowerCase();
        if (cmd.equals("-m")) {
            hook.monitor(args[1]);
        } else if (cmd.equals("-p")) {
            JFileChooser chooser = new JFileChooser();
            int returnVal = chooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                f.renameTo(new File(args[1] + "/" + f.getName()));
            }
            System.exit(0);
        } else {
            usage();
        }
    }

    private static void usage() {
        logger.log(Level.SEVERE, "VFSHook {-m|-p} {path}");
    }

    private void monitor(String dir) {
        Monitor monitor = new Monitor(new File(dir));
        NewFileSensor nfs = new NewFileSensor();
        nfs.addSensorListener(this);
        monitor.addSensor(nfs);
        RemovedFileSensor rfs = new RemovedFileSensor();
        rfs.addSensorListener(this);
        monitor.addSensor(rfs);
        ModifiedFileSensor mfs = new ModifiedFileSensor();
        mfs.addSensorListener(this);
        monitor.addSensor(mfs);
        ModifiedPropertiesSensor mps = new ModifiedPropertiesSensor();
        mps.addSensorListener(this);
        monitor.addSensor(mps);
        Thread t = new Thread(monitor);
        t.start();
    }

    /**
     * Method invoked by <code>Sensor</code> objects this listener is 
     * registered with when events of interest happen.
     * @param event describes the phenomenon that just happened in the
     * monitored <code>File</code>
     */
    public void eventSensed(SensorEvent event) {
        logger.info("Event received from sensor: " + event.getSource().getClass());
        Set keys = event.keySet();
        if (keys == null) return;
        Object next = null;
        for (Iterator i = keys.iterator(); i.hasNext(); ) {
            next = i.next();
            logger.info(next.toString() + event.get(next).toString());
        }
    }
}
