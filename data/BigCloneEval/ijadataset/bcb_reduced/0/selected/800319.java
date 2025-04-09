package hu.aitia.qcg.statnetds.core;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import hu.aitia.qcg.statnetds.tools.EdgeListNetworkGenerator;
import uchicago.src.sim.engine.AbstractGUIController;
import uchicago.src.sim.engine.BatchController;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.FruchGraphLayout;
import uchicago.src.sim.gui.GraphLayout;
import uchicago.src.sim.gui.Network2DDisplay;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.network.DefaultDrawableEdge;

/**
*
* @author Gabor Szemes
* @version 1.0
*/
public abstract class AbstractModelBatchWithSnapshots extends StatNetMaster {

    protected boolean displayNet = true;

    private GraphLayout netGraphLayout;

    private Network2DDisplay display;

    private SimGraphics simGraphics;

    private BufferedImage bim;

    protected int GUI_update = 1;

    public abstract int getDrawTypeOfAgent(int index);

    public Class<?> getDrawableEdgeClass() {
        return new DefaultDrawableEdge().getClass();
    }

    @Override
    public void buildModel() {
        super.buildModel();
        this.getController().setExitOnExit(false);
        if (displayNet) {
            long start = System.currentTimeMillis();
            println("...Building drawable local network... ");
            try {
                agentList = EdgeListNetworkGenerator.exportToStatNetGUI(edgeListNetwork, mapping, getDrawableEdgeClass());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
            updateGlobalGraph();
            netGraphLayout = new FruchGraphLayout(agentList, 300, 300);
            display = new Network2DDisplay(netGraphLayout);
            netGraphLayout.updateLayout();
            bim = new BufferedImage(display.getSize().width, display.getSize().height, BufferedImage.TYPE_INT_RGB);
            simGraphics = new SimGraphics();
            simGraphics.setGraphics((Graphics2D) bim.getGraphics());
            println("...Done...  TIME: " + (System.currentTimeMillis() - start));
        }
    }

    static int imageCount = 0;

    @Override
    public void postStep() {
        super.postStep();
        if (displayNet) {
            if (getTickCount() % this.GUI_update == 0) {
                updateGlobalGraph();
                netGraphLayout.updateLayout();
                bim = new BufferedImage(display.getSize().width, display.getSize().height, BufferedImage.TYPE_INT_RGB);
                simGraphics = new SimGraphics();
                simGraphics.setGraphics((Graphics2D) bim.getGraphics());
                display.drawDisplay(simGraphics);
                try {
                    ImageIO.write(bim, "PNG", new File("network_run" + ((BatchController) getController()).getRunCount() + "_tick" + (int) getTickCount() + ".png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bim.flush();
                imageCount++;
            }
        }
    }

    protected void updateGlobalGraph() {
        for (int i = 0; i < workers.size(); i++) {
            Hashtable<Integer, StateInterface> states = workers.get(i).getNodeStates();
            Enumeration<Integer> e = states.keys();
            while (e.hasMoreElements()) {
                int id = e.nextElement();
                ((DrawableNodeOfMaster) agentList.get(id)).setState(states.get(id));
            }
        }
        println("..GLOBAL GRAPH for snapshots was updated sucessfully..");
        for (int i = 0; i < agentList.size(); i++) {
            DrawableNodeOfMaster h = (DrawableNodeOfMaster) agentList.get(i);
            h.setDrawType(getDrawTypeOfAgent(i));
        }
    }

    public static void createZipArchiveOfPictures() {
        println("..Create ZIP archive from PNGs..");
        File f = new File(".");
        String fileList[] = f.list();
        Vector<String> fileVector = new Vector<String>();
        for (int i = 0; i < fileList.length; i++) {
            if ((fileList[i].contains(".png")) || (fileList[i].contains(".PNG"))) {
                fileVector.add(fileList[i]);
                System.out.println(fileList[i]);
            }
        }
        zipFiles("pictures.zip", fileVector);
    }

    public static void zipFiles(String outputFileName, Vector<String> fns) {
        byte[] buf = new byte[100000];
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFileName));
            for (int i = 0; i < fns.size(); i++) {
                println("Zipping file: " + fns.get(i));
                FileInputStream in = new FileInputStream(fns.get(i));
                out.putNextEntry(new ZipEntry(fns.get(i)));
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
        } catch (IOException e) {
            System.err.println("Error in zipping...");
        }
        for (int i = 0; i < fns.size(); i++) {
            File f = new File(fns.get(i));
            boolean del = f.delete();
            if (del) println(fns.get(i) + " deleted..."); else System.err.println("Error: failed to delete " + fns.get(i));
        }
        fns.clear();
    }

    /**
     * use startStatNetBATCH_SNAPSHOTS instead.
     */
    @Deprecated
    public static void startTemplate1BATCH_SNAPSHOTS(AbstractModelBatchWithSnapshots model, String[] args) {
        startStatNetBATCH_SNAPSHOTS(model, args);
    }

    public static void startStatNetBATCH_SNAPSHOTS(AbstractModelBatchWithSnapshots model, String[] args) {
        System.out.println("Starting a Template1 model in Batch mode with SNAPSHOTS..");
        for (int i = 0; i < args.length; i++) {
            System.out.println("Argument " + i + " = " + (String) args[i]);
        }
        if (args.length < 2) {
            System.out.println("ERROR: Cannot start model witout arguments\nUSAGE: ModelBatch.java PAdescriptor.xml paramFile.txt");
            System.exit(1);
        }
        loadPADescriptor(args[0]);
        SimInit init = new SimInit();
        init.loadModel(model, args[1], true);
        AbstractGUIController.CONSOLE_ERR = false;
        AbstractGUIController.CONSOLE_OUT = false;
        createZipArchiveOfPictures();
        PAClose();
    }
}
