package playground.lnicolas;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import org.matsim.facilities.Facilities;
import org.matsim.facilities.MatsimFacilitiesReader;
import org.matsim.gbl.Gbl;
import org.matsim.matrices.Matrices;
import org.matsim.matrices.MatsimMatricesReader;
import org.matsim.network.Link;
import org.matsim.network.MatsimNetworkReader;
import org.matsim.network.NetworkLayer;
import org.matsim.network.NetworkWriter;
import org.matsim.network.Node;
import org.matsim.network.algorithms.NetworkCleaner;
import org.matsim.plans.MatsimPlansReader;
import org.matsim.plans.Plans;
import org.matsim.plans.PlansReaderI;
import org.matsim.plans.PlansWriter;
import org.matsim.plans.Route;
import org.matsim.world.MatsimWorldReader;
import playground.lnicolas.kml.PopulationExportToKML;
import playground.lnicolas.network.algorithm.NetworkCutter;
import playground.lnicolas.network.algorithm.NetworkReducer;

public class MyRuns {

    protected static NetworkLayer network = null;

    public static void main(final String[] args) {
        String[] configArgs = new String[2];
        configArgs[0] = args[0];
        configArgs[1] = args[1];
        init(configArgs);
        readWorld();
        readNetwork();
        readMatrices();
    }

    protected static void writePlans(final Plans plans, final String outFilename) {
        System.out.println("Writing plans to " + outFilename);
        PlansWriter plansWriter = new PlansWriter(plans, outFilename, Gbl.getConfig().plans().getOutputVersion());
        plansWriter.write();
        System.out.println("done");
    }

    private static void exportPopulationToKML(final Plans plans, final String dir) {
        System.out.println("Running export algorithm");
        new PopulationExportToKML(dir).run(plans);
        System.out.println("Done");
    }

    private static void printLinkLengthDistr() {
        double avgLinkLength = 0;
        double maxLinkLength = 0;
        int linkCount = 0;
        for (Link link : network.getLinks().values()) {
            double l = link.getLength();
            if (l > maxLinkLength) {
                maxLinkLength = l;
            }
            avgLinkLength = ((avgLinkLength * linkCount) + l) / (linkCount + 1);
            linkCount++;
        }
        System.out.println("Avg link length = " + avgLinkLength + ", max. link length = " + maxLinkLength);
        double binLength = 10;
        ArrayList<Integer> lengthDistr = new ArrayList<Integer>();
        double binCount = 0;
        while (binCount <= maxLinkLength) {
            lengthDistr.add(0);
            binCount += binLength;
        }
        for (Link link : network.getLinks().values()) {
            double l = link.getLength();
            int c = lengthDistr.get((int) (l / binLength));
            c++;
            lengthDistr.set((int) (l / binLength), c);
        }
        String lengthsString = "";
        String countString = "";
        for (int i = 0; i < lengthDistr.size(); i++) {
            countString += lengthDistr.get(i) + ",";
            lengthsString += (i * binLength) + "-" + ((i + 1) * binLength) + ",";
        }
        BufferedWriter out;
        String filename = "/home/lnicolas/linkLengthDistr.csv";
        try {
            out = new BufferedWriter(new FileWriter(filename));
            out.write(lengthsString);
            out.newLine();
            out.write(countString);
            out.newLine();
            out.close();
        } catch (IOException e) {
            System.out.println("Error writing to file " + filename + ": " + e.getMessage());
        }
        System.out.println("Link length distribution written to " + filename);
    }

    private static void testThreads() {
        final int cpuCount = 15;
        TestThread[] cpus = new TestThread[cpuCount];
        int currentCpu = 0;
        System.out.println("Creating " + cpuCount + " threads for testing");
        for (; currentCpu < cpuCount; currentCpu++) {
            cpus[currentCpu] = new TestThread(currentCpu);
        }
        System.out.println("Starting " + cpuCount + " threads for testing");
        for (Thread cpu : cpus) {
            cpu.start();
        }
        System.out.println("Waiting for " + cpuCount + " threads to end");
        try {
            for (Thread cpu : cpus) {
                cpu.join();
            }
        } catch (InterruptedException e) {
        }
        System.out.println("Done");
    }

    private static void getMaxMinNodes() {
        Node maxNode = null;
        Node minNode = null;
        double minCoordSum = Double.MAX_VALUE;
        double maxCoordSum = Double.MIN_VALUE;
        for (Node n : network.getNodes().values()) {
            double coordSum = n.getCoord().getX() + n.getCoord().getY();
            if (coordSum > maxCoordSum) {
                maxNode = n;
                maxCoordSum = coordSum;
            }
            if (coordSum < minCoordSum) {
                minNode = n;
                minCoordSum = coordSum;
            }
        }
        System.out.println("Max coordSum: " + maxCoordSum + ", maxNode: " + maxNode.getId() + ", Min coordSum: " + minCoordSum + ", minNode: " + minNode.getId());
    }

    private static void generateKPaths(final int fromNodeID, final int toNodeID) {
        KShortestPathGenerator gen = new KShortestPathGenerator(network);
        Node fromNode = network.getNode("" + fromNodeID);
        Node toNode = network.getNode("" + toNodeID);
        int startTime = 0;
        Route[] routes = gen.calcCheapRoutes(fromNode, toNode, startTime, 100);
    }

    private static void cutNetwork() {
        NetworkCutter cutter = new NetworkCutter(671712, 243883, 700837, 265656);
        cutter.run(network);
        NetworkCleaner cleaner = new NetworkCleaner();
        cleaner.run(network);
        NetworkWriter network_writer = new NetworkWriter(network);
        network_writer.write();
        System.out.println("Wrote network to " + Gbl.getConfig().network().getOutputFile());
    }

    private static void reduceNetwork() {
        double xCenter = (640000 + 740000) / 2;
        double yCenter = (200000 + 310000) / 2;
        NetworkReducer reducer = new NetworkReducer(xCenter, yCenter, 100000);
        reducer.run(network);
        NetworkCleaner cleaner = new NetworkCleaner();
        cleaner.run(network);
        NetworkWriter network_writer = new NetworkWriter(network);
        network_writer.write();
        System.out.println("Wrote network to " + Gbl.getConfig().network().getOutputFile());
    }

    protected static Plans readPlans() {
        Plans plans = new Plans();
        PlansReaderI plansReader = new MatsimPlansReader(plans);
        plansReader.readFile(Gbl.getConfig().plans().getInputFile());
        return plans;
    }

    protected static Facilities readFacilities() {
        System.out.println("  reading facilities xml file... ");
        Facilities facilities = (Facilities) Gbl.getWorld().createLayer(Facilities.LAYER_TYPE, null);
        new MatsimFacilitiesReader(facilities).readFile(Gbl.getConfig().facilities().getInputFile());
        System.out.println("  done.");
        return facilities;
    }

    public static void init(final String[] args) {
        Gbl.createConfig(args);
    }

    protected static void readWorld() {
        System.out.println("  reading world xml file... ");
        final MatsimWorldReader worldReader = new MatsimWorldReader(Gbl.getWorld());
        worldReader.readFile(Gbl.getConfig().world().getInputFile());
        System.out.println("  done.");
    }

    protected static Matrices readMatrices() {
        System.out.println("  reading matrices xml file... ");
        MatsimMatricesReader reader = new MatsimMatricesReader(Matrices.getSingleton());
        reader.readFile(Gbl.getConfig().matrices().getInputFile());
        System.out.println("  done.");
        return Matrices.getSingleton();
    }

    protected static void readNetwork() {
        System.out.println("  reading the network...");
        network = (NetworkLayer) Gbl.getWorld().createLayer(NetworkLayer.LAYER_TYPE, null);
        new MatsimNetworkReader(network).readFile(Gbl.getConfig().network().getInputFile());
        System.out.println("  done.");
    }

    /**
	 * an internal routine to generated some (nicely?) formatted output. This
	 * helps that status output looks about the same every time output is
	 * written.
	 *
	 * @param header
	 *            the header to print, e.g. a module-name or similar. If empty
	 *            <code>""</code>, no header will be printed at all
	 * @param action
	 *            the status message, will be printed together with a timestamp
	 */
    protected static final void printNote(final String header, final String action) {
        if (header != "") {
            System.out.println("");
            System.out.println("===============================================================");
            System.out.println("== " + header);
            System.out.println("===============================================================");
        }
        if (action != "") {
            System.out.println("== " + action + " at " + (new Date()));
        }
        if (header != "") {
            System.out.println("");
        }
    }
}

class TestThread extends Thread {

    int id = -1;

    double d = 0;

    public TestThread(final int currentCpu) {
        this.id = currentCpu;
    }

    @Override
    public void run() {
        String[] resultStrings = new String[1000];
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            this.d += ((Math.round(Math.pow(i % 634, this.id) / Math.sqrt(Math.PI * this.id))));
            this.d %= 63409324;
            resultStrings[i % resultStrings.length] = Double.toString(this.d);
        }
    }
}
