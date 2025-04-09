package exe;

import gui.MainTableModel;
import gui.StartGui;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import data.AgentData;

/**
 * Import selected Agent from source by  command "tacmd addBundle"
 *
 */
public class Importer {

    private MainTableModel datamodel = null;

    private String source = null;

    private Process process = null;

    private boolean stopflag = false;

    private Logger logger = null;

    /**
	 * Create Instance
	 *
	 * @param model : datamodel
	 * @param source : path to source
	 */
    public Importer(MainTableModel model, String source) {
        super();
        this.datamodel = model;
        this.source = source;
        logger = Logger.getLogger(this.getClass().getName());
    }

    /**
	 * Destroy process tacmd
	 *
	 */
    public void destroy() {
        logger.fine("destroy Importer");
        stopflag = true;
        if (process != null) {
            process.destroy();
        }
    }

    /**
	 * Call command "tacmd removeBundle" for all selected Agent
	 *
	 */
    public void run() {
        if (datamodel != null && source != null) {
            logger.fine("Start Importer");
            final StartGui startgui = StartGui.getSingelton();
            startgui.setIndeterminate(false);
            startgui.setBarValue(0);
            final List<AgentData> selected = datamodel.getSelected();
            final int barstep = 100 / (selected.size() + 1);
            int barvalue = 0;
            stopflag = false;
            for (final AgentData agt : selected) {
                logger.fine("Start import of " + agt.getArchDesc());
                if (stopflag) {
                    logger.fine("stopflag was true");
                    break;
                }
                barvalue += barstep;
                startgui.setBarValue(barvalue);
                final String candlehome = System.getenv("CANDLEHOME");
                if (candlehome == null) {
                    JOptionPane.showMessageDialog(StartGui.getSingelton(), "Environment CANDLEHOME not set!!", "Error", JOptionPane.ERROR_MESSAGE);
                    break;
                }
                boolean noprereq = startgui.getProperty("NoPrereq", "false").equalsIgnoreCase("true");
                final List<String> cmd = new ArrayList<String>();
                cmd.add(candlehome + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "tacmd");
                cmd.add("addBundles");
                cmd.add("-i");
                cmd.add(agt.getDirectory());
                cmd.add("-p");
                cmd.add(agt.getArch());
                cmd.add("-t");
                cmd.add(agt.getProductCode());
                cmd.add("-v");
                cmd.add(agt.getFullVersion());
                if (noprereq) {
                    cmd.add("-n");
                }
                cmd.add("-f");
                String msg = "Import: " + agt.getDesc() + " ( " + agt.getFormatedVersion() + " " + agt.getArchDesc() + " )";
                logger.fine(msg);
                startgui.setStatusLine(msg);
                startgui.console(msg);
                System.out.println("Execute: " + cmd);
                try {
                    final ProcessBuilder pb = new ProcessBuilder(cmd);
                    process = pb.start();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        startgui.console(line);
                        System.out.println(line);
                    }
                    bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    while ((line = bufferedReader.readLine()) != null) {
                        startgui.console(line);
                        System.err.println(line);
                    }
                    process.waitFor();
                } catch (final IOException e) {
                    System.err.println("\nCan't exec: " + cmd);
                } catch (final InterruptedException e) {
                    System.err.println("Interrupted");
                }
            }
            startgui.setBarValue(100);
            startgui.startScan();
            startgui.console("import finish!");
            System.out.println("import finish!");
        } else {
            System.err.println("No datamodel/source given");
        }
    }
}
