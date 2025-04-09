package ist.ac.simulador.application;

import ist.ac.simulador.confguis.GuiModuleProperties;
import java.io.*;
import javax.swing.*;
import javax.swing.tree.TreePath;
import ist.ac.simulador.nucleo.*;
import ist.ac.simulador.modules.*;
import ist.ac.simulador.gef.GefEventCatcher;
import ist.ac.simulador.gef.Modulo;
import ist.ac.simulador.gef.EdgeRectiline;
import ist.ac.simulador.gef.Porto;
import ist.ac.simulador.gef.*;
import java.net.URL;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import org.tigris.gef.graph.GraphModel;
import org.tigris.gef.graph.presentation.DefaultGraphModel;
import org.tigris.gef.graph.presentation.NetNode;
import org.tigris.gef.util.Localizer;
import org.tigris.gef.util.ResourceLoader;
import org.tigris.gef.base.*;
import org.tigris.gef.presentation.FigEdge;
import org.tigris.gef.presentation.FigPoly;
import org.tigris.toolbar.toolbutton.ResourceLocator;
import javax.xml.parsers.*;

/**
 *
 * @author  cnr
 */
public final class AppSimulador extends javax.swing.JFrame implements ISimulatorOrchestrator {

    private static String configFilename = "acSimulator.cfg";

    private AppConfig cAppConfig = null;

    private SCompoundModule cm = null;

    private static final String TUTORIAL = "tutorial";

    private SimulatorBuilder sb = null;

    private DefaultListModel fBreakpoints = null;

    private DefaultListModel fWaitingEvents = null;

    private DefaultListModel emptyModel = null;

    private SwingWorker worker = null;

    private boolean workerExecuting = false;

    private java.util.Vector guis = new java.util.Vector();

    private IMessager messager;

    private RepaintGuis repaintMsg;

    private XTree xTree;

    private String prefixCCVText = "CCV: ";

    private String prefixCPVText = "CPV: ";

    private String hintCCVText = "Current Connection Value";

    private String hintCPVText = "Current Port Value";

    private SConnection currentConnection;

    private CmdCreateNode CMDnovo = null;

    /**
     * gef vars
     */
    private GraphModel gm;

    private SimulatorGraph _graph;

    public SimulatorGraph getJGraph() {
        return _graph;
    }

    GefEventCatcher _gefActions;

    private ZoomSliderButton ZoomButton;

    public ZoomSliderButton getZoom() {
        return ZoomButton;
    }

    private String internalModulesPath = "ist.ac.simulador.modules.";

    /**
     * Lista com todos os nomes de todos os m�dulos fornecidos internamente.
     */
    private String[] internalModules = new String[] { "ModuleADD", "ModuleAND", "ModuleANDBus", "ModuleBufferTri", "ModuleClock", "ModuleCounter", "ModuleDecoder", "ModuleElevador", "ModuleInput", "ModuleInputTri", "ModuleInvTri", "ModuleIST01", "ModuleIST01c", "ModuleJanelaTexto", "ModuleLatch", "ModuleLeds", "ModuleMemoryBank", "ModuleMicroPepe", "ModuleMux", "ModuleNAND", "ModuleOutput", "ModuleOR", "ModuleORBus", "ModuleP3", "ModuleP3c", "ModulePepe", "ModuleProgAnd", "ModulePROM", "ModulePushButtons", "ModuleRAM", "ModuleRAM2", "ModuleRAM3", "ModuleRegister", "ModuleReset", "ModuleRTClock", "ModuleSwitch", "ModuleTrack", "ModuleXOR", "ModuleXORBus" };

    /**
     * Lista com todos os m�dulos que podem ser inserido no simulador.
     */
    private DefaultListModel fModulesAvailable = new DefaultListModel();

    /** Creates new form ATestFrame */
    public AppSimulador() {
        inicializaGef();
        initConfig();
        try {
            xTree = new XTree();
            xTree.setCellRenderer(new XTreeRenderer());
        } catch (ParserConfigurationException ex) {
        }
        initSimConfig();
        updateTitle();
    }

    /**********************************************
     * Configuration methods
     ***********************************************/
    private void initConfig() {
        configFilename = System.getProperty("user.home") + File.separator + configFilename;
        File f = new File(configFilename);
        if (!f.exists() || !f.isFile()) {
            cAppConfig = new AppConfig();
            cm = new SCompoundModule("Arch Root");
            return;
        }
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
            cAppConfig = (AppConfig) in.readObject();
        } catch (Exception e) {
            cAppConfig = new AppConfig();
            cm = new SCompoundModule("Arch Root");
            return;
        }
        if (cAppConfig.get("AppConfig", "CurrentArch") == null || !(f = new File(cAppConfig.get("AppConfig", "CurrentArch"))).exists() || !f.isFile()) {
            cAppConfig.remove("AppConfig", "CurrentArch");
            cm = new SCompoundModule("Arch Root");
            return;
        }
        try {
            cm = SCompoundModule.load(new FileInputStream(f));
            if (cm == null) cm = new SCompoundModule("Arch Root"); else readGefFromCM(cm);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE);
            cm = new SCompoundModule("Arch Root");
        }
    }

    private void initSimConfig() {
        sb = new SimulatorBuilder(cm);
        fBreakpoints = new DefaultListModel();
        fWaitingEvents = sb.getSimulator().getEventListModel();
        initComponents();
        ScrollPaneGef.setTransferHandler(new ModuleTransferHandler());
        sb.getSimulator().setMessager(messager = new ThreadSafeMessager(messagesTextArea));
        spModulesAvailable.getViewport().add(xTree);
        xTree.setToolTipText("xpto");
        pack();
        emptyModel = new DefaultListModel();
        repaintMsg = new RepaintGuis();
    }

    private void getAvailableModules(DefaultListModel fModulesAvailable) {
        for (int i = 0; i < internalModules.length; i++) fModulesAvailable.addElement(internalModules[i]);
    }

    /**
     *  gef
     *    - Initializes gef
     */
    private void inicializaGef() {
        gm = new DefaultGraphModel();
        _graph = new SimulatorGraph(gm);
        Localizer.addResource("GefBase", "org.tigris.gef.base.BaseResourceBundle");
        Localizer.addResource("GefPres", "org.tigris.gef.presentation.PresentationResourceBundle");
        Localizer.addLocale(Locale.getDefault());
        Localizer.switchCurrentLocale(Locale.getDefault());
        ResourceLoader.addResourceLocation("/images/");
        ResourceLoader.addResourceExtension("gif");
        ResourceLocator.getInstance().addResourcePath("/images/");
        org.apache.log4j.BasicConfigurator.configure();
        Globals.curEditor().document(_graph);
        _gefActions = new GefEventCatcher(this);
        LayerManager lm = _graph.getEditor().getLayerManager();
        LayerPerspective lay = (LayerPerspective) lm.getActiveLayer();
        Globals.curEditor().getGraphModel().addGraphEventListener(_gefActions);
        _graph.addMouseListener(_gefActions);
        _graph.addMouseMotionListener(_gefActions);
        _graph.addModeChangeListener(_gefActions);
        _graph.addGraphSelectionListener(_gefActions);
        _graph.addKeyListener(_gefActions);
        _graph.setDrawingSize(5000, 5000);
        ZoomButton = new ZoomSliderButton();
    }

    /************************************************************************
     * Serialization methods
     ***********************************************************************/
    private class FilterCfg extends javax.swing.filechooser.FileFilter {

        private String extension;

        private String description;

        public FilterCfg(String ext, String desc) {
            extension = ext;
            description = desc;
        }

        public boolean accept(File file) {
            if (file.isDirectory()) return true;
            if (file.getName().endsWith(extension)) return true;
            return false;
        }

        public String getDescription() {
            return description;
        }
    }

    private void saveArch(File f) {
        if (cm.isDirty()) {
            cm.clear();
            Iterator iter = Globals.curEditor().getGraphModel().getEdges().iterator();
            writeGefOnCM(cm);
        }
        try {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), "Cp1252"));
            cm.save(out);
            out.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    private void updateTitle() {
        String title = cAppConfig.get("AppConfig", "CurrentArch");
        if (title == null) {
            title = "Untitled";
        } else {
            int end = title.lastIndexOf('.');
            if (end == -1) title = title.substring(title.lastIndexOf('\\') + 1); else title = title.substring(title.lastIndexOf('\\') + 1, end);
        }
        setTitle("Architecture Simulator [" + title + "]");
    }

    private void saveArchAs(String path) {
        JFileChooser chooser;
        if (path == null) {
            chooser = new JFileChooser();
        } else {
            chooser = new JFileChooser(path);
        }
        chooser.setFileFilter(new FilterCfg(".cmod", "Compound Module Files"));
        int returnVal = chooser.showSaveDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        if (f.getName().indexOf('.') == -1) f = new File(f.getAbsolutePath() + ".cmod");
        saveArch(f);
        cAppConfig.set("AppConfig", "CurrentArch", f.getAbsolutePath());
        updateTitle();
    }

    private void save() {
        File f;
        try {
            f = new File(cAppConfig.get("AppConfig", "CurrentArch"));
            if (f.exists() && f.isFile()) {
                saveArch(f);
            } else {
                saveArchAs(cAppConfig.get("AppConfig", "CurrentArch"));
            }
        } catch (Exception e) {
            saveArchAs(cAppConfig.get("AppConfig", "CurrentArch"));
        }
    }

    private void load() {
        String currpath = cAppConfig.get("AppConfig", "CurrentArch");
        JFileChooser chooser = currpath == null ? new JFileChooser() : new JFileChooser(currpath);
        chooser.setFileFilter(new FilterCfg(".cmod", "Compound Module Files"));
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) return;
        try {
            cm = SCompoundModule.load(new FileInputStream(chooser.getSelectedFile()));
            if (cm == null) return;
            readGefFromCM(cm);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE);
            return;
        }
        cAppConfig.set("AppConfig", "CurrentArch", chooser.getSelectedFile().getAbsolutePath());
        sb.setCompoundModule(cm);
        cm.dirty();
        updateTitle();
    }

    /****************************************************************************
     * Menu function methods
     ***************************************************************************/
    private void exitApp() {
        File f = null;
        int save = JOptionPane.showConfirmDialog(null, "Save Arch Module ?", "Question", JOptionPane.YES_NO_OPTION);
        if (save == JOptionPane.YES_OPTION) {
            try {
                f = new File(cAppConfig.get("AppConfig", "CurrentArch"));
                if (f.exists() && f.isFile()) {
                    saveArch(f);
                } else {
                    saveArchAs(cAppConfig.get("AppConfig", "CurrentArch"));
                }
            } catch (Exception e) {
                saveArchAs(cAppConfig.get("AppConfig", "CurrentArch"));
            }
        }
        f = new File(configFilename);
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
            out.writeObject(cAppConfig);
            out.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE);
        }
        System.exit(0);
    }

    private void about() {
        ImageIcon acIcon = null;
        URL iconURL = ClassLoader.getSystemResource("images/ac.gif");
        if (iconURL != null) {
            acIcon = new ImageIcon(iconURL);
        }
        String version = "";
        try {
            Package pack = this.getClass().getPackage();
            version = pack.getSpecificationTitle() + "\n" + pack.getSpecificationVendor() + "\n" + "version " + pack.getSpecificationVersion() + "\n" + pack.getImplementationVersion();
        } catch (Exception e) {
        }
        JOptionPane.showMessageDialog(this, version, "About", JOptionPane.INFORMATION_MESSAGE, acIcon);
    }

    private void newArch() {
        cm.clear();
        readGefFromCM(cm);
        cAppConfig.remove("AppConfig", "CurrentArch");
        updateTitle();
    }

    private void showHelp(String help) {
        java.net.URL helpFile = ClassLoader.getSystemResource("html/" + help + ".htm");
        if (helpFile == null) {
            JOptionPane.showMessageDialog(null, "Help file for " + help + " could not be found", "Error", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
            JEditorPane helpPanel = new JEditorPane(helpFile);
            helpPanel.setEditable(false);
            JScrollPane sp = new JScrollPane(helpPanel);
            JFrame helpFrame = new JFrame("Help on " + help);
            helpFrame.getContentPane().add(sp);
            helpFrame.pack();
            helpFrame.setVisible(true);
        } catch (IOException e) {
        }
    }

    public XTree getXTree() {
        return this.xTree;
    }

    public CmdCreateNode getCmdNovo() {
        return CMDnovo;
    }

    public void resetCmdNovo() {
        CMDnovo = null;
    }

    private void activateSimulation() {
        _gefActions.setModoDesign(false);
        if (!cm.isDirty()) return;
        cm.clear();
        writeGefOnCM(cm);
        guis.clear();
        reset();
        try {
            updateGui(cm);
            sb.commitConfig();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE);
        }
        workerExecuting = false;
        startButton.setEnabled(true);
        resetButton.setEnabled(true);
        stepButton.setEnabled(true);
        interruptButton.setEnabled(true);
        statusField.setText("");
    }

    private void activateDesign() {
        _gefActions.setModoDesign(true);
    }

    public SConnection connect(SPort sportOrig, SPort sportDest) throws Exception {
        SConnection sconn;
        if (sportOrig.getConnection() != null) {
            sconn = sportOrig.getConnection();
            if (sportDest.getConnection() != null) if (sportOrig.getConnection() != sportDest.getConnection()) {
                mergeConn(sportOrig.getConnection(), sportDest.getConnection());
            }
        } else if (sportDest.getConnection() != null) sconn = sportDest.getConnection(); else {
            int maxbits = 0;
            maxbits = sportOrig.getBits() > sportDest.getBits() ? sportOrig.getBits() : sportDest.getBits();
            java.util.Random rand = new java.util.Random();
            while (true) {
                try {
                    Thread.sleep(10);
                    String name = Integer.toHexString(rand.nextInt() & 0xFFFF);
                    cm.add(sconn = new SConnection("C" + name, maxbits));
                    break;
                } catch (SDuplicateElementException e) {
                    continue;
                }
            }
        }
        if (!((sportOrig.getConnection() != null) && (sportOrig.getConnection() == sconn))) sconn.addPort(sportOrig);
        if (!((sportDest.getConnection() != null) && (sportDest.getConnection() == sconn))) sconn.addPort(sportDest);
        return sconn;
    }

    protected void mergeConn(SConnection s1, SConnection s2) throws Exception {
        if (s1.getBits() < s2.getBits()) {
            SConnection aux = s1;
            s1 = s2;
            s2 = aux;
        }
        Object[] ports = s2.getSubElements();
        for (int i = 0; i < ports.length; i++) {
            s1.addPort((ILink) ports[i]);
        }
        Object[] segs = s2.getSegments();
        for (int j = 0; j < segs.length; j++) {
            s1.addSegment((ASegment) segs[j]);
        }
    }

    public void connect(SConnection sconn, SPort sport) throws Exception {
        sconn.addPort(sport);
    }

    public SConnection newConnection(String name, int nbits) {
        SConnection newconn = null;
        newconn = new SConnection(name, nbits);
        try {
            cm.add(newconn);
        } catch (SDuplicateElementException e) {
            JOptionPane.showMessageDialog(null, "There is already a connection with that name", "Error", JOptionPane.INFORMATION_MESSAGE);
        }
        return newconn;
    }

    public SModule newModule(String moduletype, String modulename) {
        moduletype = moduletype.trim();
        String config = "";
        if (modulename == null) modulename = "";
        SModule newmodule = null;
        String className = getModuleAbsName(moduletype);
        try {
            Constructor moduleConst = Class.forName(className).getConstructor(new Class[] { String.class, String.class });
            newmodule = (SModule) moduleConst.newInstance(new Object[] { modulename, config });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Exception: " + e, "Could not instantiate module", JOptionPane.ERROR_MESSAGE);
            return newmodule;
        }
        try {
            cm.add(newmodule);
        } catch (SDuplicateElementException e) {
            JOptionPane.showMessageDialog(null, "There is already a module with that name", "Error", JOptionPane.INFORMATION_MESSAGE);
        }
        return newmodule;
    }

    private String getModuleAbsName(String name) {
        if (name.startsWith(internalModulesPath)) return name;
        return internalModulesPath + name;
    }

    public void showPropertiesPanel(SModule sMod) {
        if (sMod.getConfigGui() != null) {
            sMod.getConfigGui().setVisible(true);
            makeDirty();
        }
    }

    public void makeDirty() {
        cm.dirty();
    }

    public void updateConnectionValue(SConnection sconn) {
        currentConnection = sconn;
    }

    public void updatePortValue(SPort sport) {
    }

    /********************************************************************
 * Interface com o GEF
 *******************************************************************/
    private boolean isRepetida(Porto a, Porto b) {
        Iterator iter = Globals.curEditor().getGraphModel().getEdges().iterator();
        while (iter.hasNext()) {
            EdgeRectiline lig = (EdgeRectiline) iter.next();
            if (lig.getSourcePort().equals(a) && lig.getDestPort().equals(b) || lig.getSourcePort().equals(b) && lig.getDestPort().equals(a)) return true;
        }
        return false;
    }

    public void ClearGef() {
        _graph.getEditor().getGraphModel().getNodes().clear();
        _graph.getEditor().getGraphModel().getEdges().clear();
        CmdSelectAll cmdall = new CmdSelectAll();
        cmdall.doIt();
        CmdRemoveFromGraph cmdrem = new CmdRemoveFromGraph();
        cmdrem.doIt();
    }

    private ASegment DescobreSegmento(ILink sentrada, ILink ssaida, Object[] os) {
        for (int i = 0; i < os.length; i++) {
            if ((((ASegment) os[i]).lOrig == sentrada) && (((ASegment) os[i]).lDest == ssaida)) return (ASegment) os[i];
            if ((((ASegment) os[i]).lOrig == sentrada) && (((ASegment) os[i]).lOrig == ssaida)) return (ASegment) os[i];
        }
        return null;
    }

    private void readGefFromCM(SCompoundModule cm) {
        ClearGef();
        List nodes = gm.getNodes();
        DefaultGraphModel dgm = (DefaultGraphModel) _graph.getGraphModel();
        List _sportList = new ArrayList();
        List _portoList = new ArrayList();
        Enumeration cmModul = cm.getModules();
        while (cmModul.hasMoreElements()) {
            SModule smod = (SModule) cmModul.nextElement();
            Module module = null;
            try {
                module = (Module) Module.class.newInstance();
            } catch (java.lang.IllegalAccessException ignore) {
                ;
            } catch (java.lang.InstantiationException ignore) {
                ;
            }
            Hashtable hash = new Hashtable();
            hash.put("className", Module.class);
            hash.put("SimModule", smod);
            hash.put("Coordenadas", new Point(smod.getPositionX(), smod.getPositionY()));
            module.initialize(hash);
            dgm.addNode(module);
            for (int iq = 0; iq < smod.getPortCount(); iq++) {
                SPort por = (SPort) smod.getPorts()[iq];
                _sportList.add(por);
                _portoList.add(module.getPorts().get(iq));
                switch(por.getFixedValue()) {
                    case -1:
                        por.setPortFree();
                        break;
                    case 0:
                        por.setPort2Gnd();
                        break;
                    default:
                        por.setPort2Vcc();
                        break;
                }
            }
        }
        Enumeration enuma = cm.getConnections();
        while (enuma.hasMoreElements()) {
            SConnection sconn = (SConnection) enuma.nextElement();
            Object[] segs = sconn.getSegments();
            for (int s = 0; s < segs.length; s++) {
                ASegment seg = (ASegment) segs[s];
                if (seg.lOrig == null || seg.lDest == null) continue;
                Port srcPort = (Port) ((SElement) seg.lOrig).getGUI();
                Port destPort = (Port) ((SElement) seg.lDest).getGUI();
                Connection connection = new Connection(seg.x, seg.y, seg.y.length);
                connection.setSConnection(sconn);
                connection.setSourcePort(srcPort);
                connection.setDestPort(destPort);
                dgm.addEdge(connection);
            }
        }
    }

    private EdgeRectiline getEdgeFromSConnection(SConnection sc) {
        Iterator iterEdges = Globals.curEditor().getGraphModel().getEdges().iterator();
        while (iterEdges.hasNext()) {
            EdgeRectiline edge = (EdgeRectiline) iterEdges.next();
            if (edge.getSConnection() == sc) return edge;
        }
        return null;
    }

    public void FLigacao(String idLigacao, Color cor) {
        List listaArestas = Globals.curEditor().getGraphModel().getEdges();
        Iterator iter = listaArestas.iterator();
        while (iter.hasNext()) {
            EdgeRectiline figu = ((EdgeRectiline) iter.next());
            if (figu.getId().equals(idLigacao)) {
                figu.getFigEdg().setColor(cor);
            }
        }
    }

    public void UpdateEdgeColors() {
        List listaArestas = Globals.curEditor().getGraphModel().getEdges();
        Iterator iter = listaArestas.iterator();
        while (iter.hasNext()) {
            Connection connection = ((Connection) iter.next());
            if (connection == null || connection.getSConnection() == null) return;
            if (connection.getSConnection().getBits() == 1) {
                connection.getGConnection().setColor(connection.getSConnection().getColorValue());
                connection.getGConnection().endTrans();
            }
        }
    }

    private void writeGefOnCM(SCompoundModule cm) {
        cm.clear();
        GraphModel gm = Globals.curEditor().getGraphModel();
        Iterator iterModul = gm.getNodes().iterator();
        while (iterModul.hasNext()) {
            try {
                Module mod = (Module) iterModul.next();
                SModule smod = mod.getSModule();
                smod.setPosition((int) mod.getGModule().getLocation().getX(), (int) mod.getGModule().getLocation().getY());
                cm.add(smod);
            } catch (SDuplicateElementException ex) {
                System.out.println(ex.getMessage());
            }
        }
        List lModulos = Globals.curEditor().getGraphModel().getNodes();
        Iterator iter = lModulos.iterator();
        while (iter.hasNext()) {
            Module module = (Module) iter.next();
            ArrayList<Port> ports = module.getPorts();
            for (Port port : ports) {
                SPort sPort = port.getSPort();
                if (sPort.getConnection() != null) {
                    sPort.removeConnection(sPort.getConnection());
                }
            }
        }
        Iterator iterLigac = gm.getEdges().iterator();
        while (iterLigac.hasNext()) {
            Connection er = (Connection) iterLigac.next();
            Port prtSource = (Port) er.getSourcePort();
            SPort sPrtSource = prtSource.getSPort();
            Port prtDest = (Port) er.getDestPort();
            SPort sPrtDest = prtDest.getSPort();
            try {
                er.getId();
                SConnection sconn = connect(sPrtSource, sPrtDest);
                ASegment newsegment = new ASegment(sPrtSource, sPrtDest, er.getGConnection().getXs(), er.getGConnection().getYs());
                sconn.addSegment(newsegment);
                er.setSConnection(sconn);
            } catch (Exception ex) {
                System.out.println("Excep��o a fazer connect de dois Portos: " + ex.getMessage() + " Causa: " + ex.getStackTrace());
            }
        }
    }

    /** Gets the global configuration object.
 * @return The Configuration object
 */
    public AppConfig getConfig() {
        return cAppConfig;
    }

    /**
 * Devolve uma refer�ncia para a lista de breakpoints do simulador.
 * @return lista de breakpoints do simulador.
 */
    public DefaultListModel getBreakPointListModel() {
        return fBreakpoints;
    }

    /** Limpa todos os breakpoints.
 */
    public void clearAllBreakpoitns() {
        fBreakpoints.clear();
    }

    /**
 * Testa se existem breakpoints activos no simulador.
 *
 * @return <code>true</code> caso existam breakpoints activos.<br>
 *         <code>false</code> caso n�o existam breakpoints activos.
 */
    public boolean isBreak() {
        java.util.Enumeration breaklist = fBreakpoints.elements();
        while (breaklist.hasMoreElements()) {
            IBreakpoint testBreak = (IBreakpoint) breaklist.nextElement();
            if (testBreak.isActive()) return true;
        }
        return false;
    }

    /**
 * Devolve o breakpoint activo.
 *
 * @return <code>�ndice</code> do breakpoint activo.<br>
 *         <code>-1</code> caso n�o existam breakpoints activos.
 */
    public int getActiveBreak() {
        for (int i = 0; i < fBreakpoints.size(); i++) {
            if (((IBreakpoint) fBreakpoints.get(i)).isActive()) return i;
        }
        return -1;
    }

    private class RepaintGuis implements Runnable {

        public void run() {
            wakeGuis();
        }
    }

    /**
 * Executa ciclos de simulador at� este ficar sem eventos, atingir um breakpoint
 * ou ser interrompido.
 */
    private Object runSim(boolean step, boolean events) {
        boolean endSim = false;
        boolean interrup = false;
        Simulator sim = sb.getSimulator();
        boolean stop;
        do {
            sim.cycle();
            stop = (step || isBreak() || (interrup = Thread.interrupted()));
            if (!stop && !events) interrup = sim.waitForEvents(repaintMsg);
            endSim = (events && !sim.isEvent());
            stop = stop || interrup || endSim;
        } while (!stop);
        return !endSim ? "Simulation end" : step ? "Simulation stepped" : interrup ? "Simulation stopped" : "Breakpoint reached";
    }

    /** Main execution method.
 * Starts simulation within another thread.
 * @param step true if stops at the next instant.
 */
    public void run() {
        run(false, false);
    }

    public void step() {
        run(true, false);
    }

    public void runEvents() {
        run(false, true);
    }

    public void run(final boolean step, final boolean events) {
        if (workerExecuting) return;
        workerExecuting = true;
        jTabbedPane1.setEnabled(false);
        jMenu1.setEnabled(false);
        stepButton.setEnabled(false);
        startButton.setEnabled(false);
        resetButton.setEnabled(false);
        interruptButton.setEnabled(true);
        statusField.setText("Simulating...");
        lEvents.setModel(emptyModel);
        worker = new SwingWorker() {

            public Object construct() {
                return runSim(step, events);
            }

            public void finished() {
                workerExecuting = false;
                jTabbedPane1.setEnabled(true);
                jMenu1.setEnabled(true);
                startButton.setEnabled(true);
                resetButton.setEnabled(true);
                stepButton.setEnabled(true);
                interruptButton.setEnabled(false);
                statusField.setText(get().toString());
                if (isBreak()) {
                    lBreakPointList.setSelectedIndex(getActiveBreak());
                }
                lEvents.setModel(sb.getSimulator().getEventListModel());
                wakeGuis();
            }
        };
        worker.start();
    }

    /** Stops simulation. Signals the thread to stop.
 */
    public void stop() {
        if (worker != null) worker.interrupt();
    }

    /** Waits for the thread to stop.
 */
    public void join() {
        if (worker != null) worker.join();
    }

    /** Resets, the root compound module which in turn resets the all application.
 */
    public void reset() {
        fBreakpoints.clear();
        try {
            sb.getSimulator().reset();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Register a GUI to be waked.
 * @param gui The GUI to wake.
 */
    public void wakeThis(Gui gui) {
        if (gui != null) guis.add(gui);
    }

    private void wakeGuis() {
        tfTime.setText(String.valueOf(sb.getSimulator().getTime()));
        for (int i = 0; i < guis.size(); i++) ((Gui) guis.elementAt(i)).wake();
        UpdateEdgeColors();
    }

    /** Returns the object messager where messages can be written to the user.
 * @return The object messager.
 */
    public IMessager getMessager() {
        return messager;
    }

    private void updateGuis(Enumeration modules) throws GException {
        while (modules.hasMoreElements()) {
            updateGui((SModule) modules.nextElement());
        }
    }

    private void updateGui(SModule mod) throws GException {
        Gui gui = (Gui) mod.getGUI();
        if (gui != null) {
            gui.setParent(this);
        }
        if (SCompoundModule.class.isAssignableFrom(mod.getClass())) updateGuis(((SCompoundModule) mod).getModules());
    }

    public void showGui(SModule module) {
        Gui gui = (Gui) module.getGUI();
        if (gui != null) gui.setVisible(true);
    }

    private void initComponents() {
        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        pModulesAvailable = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        tModulesAvailable = new javax.swing.JLabel();
        spModulesAvailable = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        startButton = new javax.swing.JButton();
        interruptButton = new javax.swing.JButton();
        stepButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        tfTime = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel5 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jPanel111 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        bAddBreak = new javax.swing.JButton();
        cbSignal = new javax.swing.JComboBox();
        connValue = new javax.swing.JTextField();
        bRemBreak = new javax.swing.JButton();
        bClearBreak = new javax.swing.JButton();
        jSeparator12 = new javax.swing.JSeparator();
        jScrollPane5 = new javax.swing.JScrollPane();
        lBreakPointList = new javax.swing.JList();
        jPanel4 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        statusField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        lEvents = new javax.swing.JList();
        JPanelGEF = new javax.swing.JPanel();
        ToolBarGef = new javax.swing.JToolBar();
        jButtonNew = new javax.swing.JButton();
        jButtonOpen = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        jButtonDelete = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        jButtonToFront = new javax.swing.JButton();
        jButtonToBack = new javax.swing.JButton();
        jButtonForward = new javax.swing.JButton();
        jButtonBackward = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JSeparator();
        jButtonPrint = new javax.swing.JButton();
        ScrollPaneGef = new javax.swing.JScrollPane();
        messagesPane = new javax.swing.JPanel();
        messagesLabel = new javax.swing.JLabel();
        messagesScrollPane = new javax.swing.JScrollPane();
        messagesTextArea = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        miNew = new javax.swing.JMenuItem();
        miLoad = new javax.swing.JMenuItem();
        miSave = new javax.swing.JMenuItem();
        miSaveAsModule = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        setTitle("Architecture Simulator");
        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jTabbedPane1.setMinimumSize(new java.awt.Dimension(250, 0));
        jTabbedPane1.addComponentListener(new java.awt.event.ComponentAdapter() {

            public void componentShown(java.awt.event.ComponentEvent evt) {
                tpMainPaneComponentShown(evt);
            }
        });
        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {

            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });
        pModulesAvailable.setLayout(new java.awt.BorderLayout());
        jPanel11.setLayout(new java.awt.BorderLayout());
        tModulesAvailable.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tModulesAvailable.setText(" Modules Available");
        tModulesAvailable.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        tModulesAvailable.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel11.add(tModulesAvailable, java.awt.BorderLayout.SOUTH);
        pModulesAvailable.add(jPanel11, java.awt.BorderLayout.NORTH);
        pModulesAvailable.add(spModulesAvailable, java.awt.BorderLayout.CENTER);
        jTabbedPane1.addTab("Design", pModulesAvailable);
        jPanel1.setLayout(new java.awt.BorderLayout());
        jPanel3.setLayout(new java.awt.BorderLayout());
        jPanel3.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel13.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 1, 1));
        jPanel13.setPreferredSize(new java.awt.Dimension(200, 22));
        startButton.setFont(new java.awt.Font("Dialog", 0, 10));
        startButton.setText("Start");
        startButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        startButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        startButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });
        jPanel13.add(startButton);
        interruptButton.setFont(new java.awt.Font("Dialog", 0, 10));
        interruptButton.setText("Stop");
        interruptButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        interruptButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                interruptButtonActionPerformed(evt);
            }
        });
        jPanel13.add(interruptButton);
        stepButton.setFont(new java.awt.Font("Dialog", 0, 10));
        stepButton.setText("Step");
        stepButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        stepButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        stepButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stepButtonActionPerformed(evt);
            }
        });
        jPanel13.add(stepButton);
        resetButton.setFont(new java.awt.Font("Dialog", 0, 10));
        resetButton.setText("Reset");
        resetButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        resetButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        resetButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });
        jPanel13.add(resetButton);
        jLabel5.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel5.setText("Time:");
        jLabel5.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jPanel13.add(jLabel5);
        tfTime.setEditable(false);
        tfTime.setFont(new java.awt.Font("Dialog", 0, 10));
        tfTime.setMinimumSize(new java.awt.Dimension(40, 21));
        tfTime.setPreferredSize(new java.awt.Dimension(50, 21));
        jPanel13.add(tfTime);
        jPanel3.add(jPanel13, java.awt.BorderLayout.NORTH);
        jPanel3.add(jSeparator2, java.awt.BorderLayout.SOUTH);
        jPanel1.add(jPanel3, java.awt.BorderLayout.NORTH);
        jPanel5.setLayout(new java.awt.BorderLayout());
        jPanel10.setLayout(new java.awt.BorderLayout());
        jPanel111.setLayout(new java.awt.BorderLayout());
        jLabel31.setText("Breakpoints");
        jPanel12.add(jLabel31);
        bAddBreak.setFont(new java.awt.Font("Dialog", 0, 10));
        bAddBreak.setText("Add");
        bAddBreak.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bAddBreak.setMargin(new java.awt.Insets(0, 0, 0, 0));
        bAddBreak.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bAddBreakActionPerformed(evt);
            }
        });
        jPanel12.add(bAddBreak);
        cbSignal.setFont(new java.awt.Font("Dialog", 0, 10));
        cbSignal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "==", ">", "<", ">=", "<=" }));
        cbSignal.setMinimumSize(new java.awt.Dimension(25, 25));
        jPanel12.add(cbSignal);
        connValue.setFont(new java.awt.Font("Dialog", 0, 10));
        connValue.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        connValue.setText("0");
        connValue.setPreferredSize(new java.awt.Dimension(30, 20));
        jPanel12.add(connValue);
        bRemBreak.setFont(new java.awt.Font("Dialog", 0, 10));
        bRemBreak.setText("Remove");
        bRemBreak.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bRemBreak.setMargin(new java.awt.Insets(0, 0, 0, 0));
        bRemBreak.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bRemBreakActionPerformed(evt);
            }
        });
        jPanel12.add(bRemBreak);
        bClearBreak.setFont(new java.awt.Font("Dialog", 0, 10));
        bClearBreak.setText("Clear");
        bClearBreak.setMargin(new java.awt.Insets(0, 0, 0, 0));
        bClearBreak.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bClearBreakActionPerformed(evt);
            }
        });
        jPanel12.add(bClearBreak);
        jPanel111.add(jPanel12, java.awt.BorderLayout.WEST);
        jPanel111.add(jSeparator12, java.awt.BorderLayout.NORTH);
        jPanel10.add(jPanel111, java.awt.BorderLayout.NORTH);
        lBreakPointList.setModel(fBreakpoints);
        jScrollPane5.setViewportView(lBreakPointList);
        jPanel10.add(jScrollPane5, java.awt.BorderLayout.CENTER);
        jPanel5.add(jPanel10, java.awt.BorderLayout.CENTER);
        jPanel5.add(jPanel4, java.awt.BorderLayout.SOUTH);
        jPanel1.add(jPanel5, java.awt.BorderLayout.CENTER);
        jPanel6.setLayout(new java.awt.BorderLayout());
        jPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        jLabel4.setText("Waiting Events");
        jPanel7.add(jLabel4);
        jLabel6.setText("      Status");
        jPanel7.add(jLabel6);
        statusField.setEditable(false);
        statusField.setFont(new java.awt.Font("Dialog", 0, 10));
        statusField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        statusField.setPreferredSize(new java.awt.Dimension(100, 20));
        jPanel7.add(statusField);
        jPanel6.add(jPanel7, java.awt.BorderLayout.NORTH);
        lEvents.setModel(fWaitingEvents);
        lEvents.setVisibleRowCount(6);
        jScrollPane1.setViewportView(lEvents);
        jPanel6.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        jPanel1.add(jPanel6, java.awt.BorderLayout.SOUTH);
        jTabbedPane1.addTab("Simulation", jPanel1);
        jSplitPane2.setLeftComponent(jTabbedPane1);
        JPanelGEF.setLayout(new java.awt.BorderLayout());
        JPanelGEF.setBackground(new java.awt.Color(51, 51, 0));
        JPanelGEF.setOpaque(false);
        ToolBarGef.add(new ZoomSliderButton());
        ImageIcon acIcon = null;
        URL iconURL = ClassLoader.getSystemResource("images/new.gif");
        if (iconURL != null) {
            acIcon = new ImageIcon(iconURL);
            jButtonNew.setIcon(acIcon);
        }
        jButtonNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/new.gif")));
        jButtonNew.setText("New");
        jButtonNew.setToolTipText("Novo");
        jButtonNew.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButtonNew.setMaximumSize(new java.awt.Dimension(25, 30));
        jButtonNew.setMinimumSize(new java.awt.Dimension(25, 30));
        jButtonNew.setName("New");
        jButtonNew.setText("");
        jButtonNew.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNewActionPerformed(evt);
            }
        });
        ToolBarGef.add(jButtonNew);
        ImageIcon acIcon2 = null;
        URL iconURL2 = ClassLoader.getSystemResource("images/Open.gif");
        if (iconURL2 != null) {
            acIcon2 = new ImageIcon(iconURL2);
            jButtonOpen.setIcon(acIcon2);
        }
        jButtonOpen.setText("Open");
        jButtonOpen.setToolTipText("Open");
        jButtonOpen.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButtonOpen.setMaximumSize(new java.awt.Dimension(25, 30));
        jButtonOpen.setMinimumSize(new java.awt.Dimension(25, 30));
        jButtonOpen.setName("Open");
        jButtonOpen.setText("");
        jButtonOpen.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenActionPerformed(evt);
            }
        });
        ToolBarGef.add(jButtonOpen);
        ImageIcon acIcon3 = null;
        URL iconURL3 = ClassLoader.getSystemResource("images/Save.gif");
        if (iconURL3 != null) {
            acIcon3 = new ImageIcon(iconURL3);
            jButtonSave.setIcon(acIcon3);
        }
        jButtonSave.setText("Save");
        jButtonSave.setToolTipText("Save");
        jButtonSave.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButtonSave.setMaximumSize(new java.awt.Dimension(25, 30));
        jButtonSave.setMinimumSize(new java.awt.Dimension(25, 30));
        jButtonSave.setName("Save");
        jButtonSave.setText("");
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });
        ToolBarGef.add(jButtonSave);
        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator3.setMaximumSize(new java.awt.Dimension(5, 30));
        jSeparator3.setMinimumSize(new java.awt.Dimension(5, 30));
        ToolBarGef.add(jSeparator3);
        ImageIcon acIcon4 = null;
        URL iconURL4 = ClassLoader.getSystemResource("images/Delete.gif");
        if (iconURL4 != null) {
            acIcon4 = new ImageIcon(iconURL4);
            jButtonDelete.setIcon(acIcon4);
        }
        jButtonDelete.setText("Delete");
        jButtonDelete.setToolTipText("Apaga Elemento Seleccionado");
        jButtonDelete.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButtonDelete.setMaximumSize(new java.awt.Dimension(25, 30));
        jButtonDelete.setMinimumSize(new java.awt.Dimension(25, 30));
        jButtonDelete.setName("Delete");
        jButtonDelete.setText("");
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });
        ToolBarGef.add(jButtonDelete);
        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator4.setMaximumSize(new java.awt.Dimension(5, 30));
        jSeparator4.setMinimumSize(new java.awt.Dimension(5, 30));
        ToolBarGef.add(jSeparator4);
        ImageIcon acIcon5 = null;
        URL iconURL5 = ClassLoader.getSystemResource("images/ToFront.gif");
        if (iconURL5 != null) {
            acIcon5 = new ImageIcon(iconURL5);
            jButtonToFront.setIcon(acIcon5);
        }
        jButtonToFront.setText("ToFront");
        jButtonToFront.setToolTipText("Faz ToFront do Elemento Seleccionado");
        jButtonToFront.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButtonToFront.setMaximumSize(new java.awt.Dimension(25, 30));
        jButtonToFront.setMinimumSize(new java.awt.Dimension(25, 30));
        jButtonToFront.setName("ToFront");
        jButtonToFront.setText("");
        jButtonToFront.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonToFrontActionPerformed(evt);
            }
        });
        ToolBarGef.add(jButtonToFront);
        ImageIcon acIcon6 = null;
        URL iconURL6 = ClassLoader.getSystemResource("images/ToBack.gif");
        if (iconURL6 != null) {
            acIcon6 = new ImageIcon(iconURL6);
            jButtonToBack.setIcon(acIcon6);
        }
        jButtonToBack.setText("ToBack");
        jButtonToBack.setToolTipText("Faz ToBack do Elemento Seleccionado");
        jButtonToBack.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButtonToBack.setMaximumSize(new java.awt.Dimension(25, 30));
        jButtonToBack.setMinimumSize(new java.awt.Dimension(25, 30));
        jButtonToBack.setName("ToBack");
        jButtonToBack.setText("");
        jButtonToBack.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonToBackActionPerformed(evt);
            }
        });
        ToolBarGef.add(jButtonToBack);
        ImageIcon acIcon7 = null;
        URL iconURL7 = ClassLoader.getSystemResource("images/Forward.gif");
        if (iconURL7 != null) {
            acIcon7 = new ImageIcon(iconURL7);
            jButtonForward.setIcon(acIcon7);
        }
        jButtonForward.setText("Forward");
        jButtonForward.setToolTipText("Faz Forward do Elemento Seleccionado");
        jButtonForward.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButtonForward.setMaximumSize(new java.awt.Dimension(25, 30));
        jButtonForward.setMinimumSize(new java.awt.Dimension(25, 30));
        jButtonForward.setName("Forward");
        jButtonForward.setText("");
        jButtonForward.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonForwardActionPerformed(evt);
            }
        });
        ToolBarGef.add(jButtonForward);
        ImageIcon acIcon8 = null;
        URL iconURL8 = ClassLoader.getSystemResource("images/Backward.gif");
        if (iconURL8 != null) {
            acIcon8 = new ImageIcon(iconURL8);
            jButtonBackward.setIcon(acIcon8);
        }
        jButtonBackward.setText("Backward");
        jButtonBackward.setToolTipText("Faz Backward do Elemento Seleccionado");
        jButtonBackward.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButtonBackward.setMaximumSize(new java.awt.Dimension(25, 30));
        jButtonBackward.setMinimumSize(new java.awt.Dimension(25, 30));
        jButtonBackward.setName("Backward");
        jButtonBackward.setText("");
        jButtonBackward.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBackwardActionPerformed(evt);
            }
        });
        ToolBarGef.add(jButtonBackward);
        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator5.setMaximumSize(new java.awt.Dimension(5, 30));
        jSeparator5.setMinimumSize(new java.awt.Dimension(5, 30));
        ToolBarGef.add(jSeparator5);
        ImageIcon acIcon9 = null;
        URL iconURL9 = ClassLoader.getSystemResource("images/Print.gif");
        if (iconURL9 != null) {
            acIcon9 = new ImageIcon(iconURL9);
            jButtonPrint.setIcon(acIcon9);
        }
        jButtonPrint.setText("Print");
        jButtonPrint.setToolTipText("Imprime");
        jButtonPrint.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButtonPrint.setMaximumSize(new java.awt.Dimension(25, 30));
        jButtonPrint.setMinimumSize(new java.awt.Dimension(25, 30));
        jButtonPrint.setName("Print");
        jButtonPrint.setText("");
        jButtonPrint.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintActionPerformed(evt);
            }
        });
        ToolBarGef.add(jButtonPrint);
        JPanelGEF.add(ToolBarGef, java.awt.BorderLayout.NORTH);
        ScrollPaneGef.setEnabled(false);
        ScrollPaneGef.setFocusable(false);
        ScrollPaneGef.setMinimumSize(new java.awt.Dimension(400, 400));
        ScrollPaneGef.setPreferredSize(new java.awt.Dimension(400, 400));
        ScrollPaneGef.setRequestFocusEnabled(false);
        ScrollPaneGef.setVerifyInputWhenFocusTarget(false);
        ScrollPaneGef.setViewportView(_graph);
        JPanelGEF.add(ScrollPaneGef, java.awt.BorderLayout.CENTER);
        jSplitPane2.setRightComponent(JPanelGEF);
        jSplitPane1.setLeftComponent(jSplitPane2);
        messagesPane.setMinimumSize(new java.awt.Dimension(2, 100));
        messagesPane.setPreferredSize(new java.awt.Dimension(2, 100));
        messagesLabel.setText("Messages");
        messagesTextArea.setColumns(20);
        messagesTextArea.setEditable(false);
        messagesTextArea.setRows(5);
        messagesScrollPane.setViewportView(messagesTextArea);
        org.jdesktop.layout.GroupLayout messagesPaneLayout = new org.jdesktop.layout.GroupLayout(messagesPane);
        messagesPane.setLayout(messagesPaneLayout);
        messagesPaneLayout.setHorizontalGroup(messagesPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(messagesLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 694, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(messagesScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 808, Short.MAX_VALUE));
        messagesPaneLayout.setVerticalGroup(messagesPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(messagesPaneLayout.createSequentialGroup().add(messagesLabel).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(messagesScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)));
        jSplitPane1.setRightComponent(messagesPane);
        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);
        jMenu1.setText("File");
        miNew.setText("New");
        miNew.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miNewActionPerformed(evt);
            }
        });
        jMenu1.add(miNew);
        miLoad.setText("Load");
        miLoad.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miLoadActionPerformed(evt);
            }
        });
        jMenu1.add(miLoad);
        miSave.setText("Save");
        miSave.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSaveActionPerformed(evt);
            }
        });
        jMenu1.add(miSave);
        miSaveAsModule.setText("Save As");
        miSaveAsModule.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSaveAsModuleActionPerformed(evt);
            }
        });
        jMenu1.add(miSaveAsModule);
        jMenuBar1.add(jMenu1);
        jMenu3.setText("Help");
        jMenuItem1.setText("Tutorial");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem1);
        jCheckBoxMenuItem1.setText("Grid");
        jCheckBoxMenuItem1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItem1ActionPerformed(evt);
            }
        });
        jCheckBoxMenuItem1.addAncestorListener(new javax.swing.event.AncestorListener() {

            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }

            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                jCheckBoxMenuItem1AncestorAdded(evt);
            }

            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        jMenu3.add(jCheckBoxMenuItem1);
        jMenuItem2.setText("About");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem2);
        jMenuBar1.add(jMenu3);
        setJMenuBar(jMenuBar1);
        pack();
    }

    private void jCheckBoxMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (jCheckBoxMenuItem1.isSelected()) Globals.curEditor().setGuide(new GuideGrid(5)); else Globals.curEditor().setGuide(null);
    }

    private void jCheckBoxMenuItem1AncestorAdded(javax.swing.event.AncestorEvent evt) {
    }

    private void jButtonToFrontActionPerformed(java.awt.event.ActionEvent evt) {
        CmdReorder reor = new CmdReorder(CmdReorder.BRING_TO_FRONT);
        reor.doIt();
    }

    private void jButtonToBackActionPerformed(java.awt.event.ActionEvent evt) {
        CmdReorder reor = new CmdReorder(CmdReorder.SEND_TO_BACK);
        reor.doIt();
    }

    private void jButtonBackwardActionPerformed(java.awt.event.ActionEvent evt) {
        CmdReorder reor = new CmdReorder(CmdReorder.SEND_BACKWARD);
        reor.doIt();
    }

    private void jButtonForwardActionPerformed(java.awt.event.ActionEvent evt) {
        CmdReorder reor = new CmdReorder(CmdReorder.BRING_FORWARD);
        reor.doIt();
    }

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {
        save();
    }

    private void jButtonOpenActionPerformed(java.awt.event.ActionEvent evt) {
        load();
    }

    private void jButtonNewActionPerformed(java.awt.event.ActionEvent evt) {
        newArch();
    }

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {
        CmdDeleteFromModel del = new CmdDeleteFromModel();
        del.doIt();
    }

    private void jButtonPrintActionPerformed(java.awt.event.ActionEvent evt) {
        CmdPrint prt = new CmdPrint();
        CmdSelectAll all = new CmdSelectAll();
        all.doIt();
        prt.doPageSetup();
        prt.doIt();
    }

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {
        JTabbedPane tabSource = (JTabbedPane) evt.getSource();
        String tab = tabSource.getTitleAt(tabSource.getSelectedIndex());
        if (tab.equals("Design")) {
            activateDesign();
        }
        if (tab.equals("Simulation")) {
            activateSimulation();
        }
    }

    private void bClearBreakActionPerformed(java.awt.event.ActionEvent evt) {
        fBreakpoints.clear();
    }

    private void bRemBreakActionPerformed(java.awt.event.ActionEvent evt) {
        if (lBreakPointList.isSelectionEmpty()) return;
        fBreakpoints.remove(lBreakPointList.getSelectedIndex());
    }

    private void bAddBreakActionPerformed(java.awt.event.ActionEvent evt) {
        if (currentConnection != null) {
            fBreakpoints.addElement(new ValueBreakpoint(currentConnection, cbSignal.getSelectedIndex(), Integer.parseInt(connValue.getText(), 16)));
        }
    }

    private void tpMainPaneComponentShown(java.awt.event.ComponentEvent evt) {
        activateSimulation();
    }

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {
        reset();
    }

    private void stepButtonActionPerformed(java.awt.event.ActionEvent evt) {
        step();
    }

    private void interruptButtonActionPerformed(java.awt.event.ActionEvent evt) {
        stop();
    }

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {
        run();
    }

    private void refreshTreeActionPerformed(java.awt.event.ActionEvent evt) {
        if (xTree != null) xTree.readTree();
        pack();
    }

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {
        new AHelpScreen(TUTORIAL).setVisible(true);
    }

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {
        about();
    }

    private void miNewActionPerformed(java.awt.event.ActionEvent evt) {
        jTabbedPane1.setSelectedIndex(0);
        newArch();
    }

    private void miSaveAsModuleActionPerformed(java.awt.event.ActionEvent evt) {
        saveArchAs(cAppConfig.get("AppConfig", "CompoundModulesPath"));
    }

    private void miSaveActionPerformed(java.awt.event.ActionEvent evt) {
        save();
    }

    private void miLoadActionPerformed(java.awt.event.ActionEvent evt) {
        jTabbedPane1.setSelectedIndex(0);
        load();
    }

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {
        exitApp();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new AppSimulador().setVisible(true);
    }

    public JToolBar getToolBarGef() {
        return ToolBarGef;
    }

    private javax.swing.JPanel JPanelGEF;

    private javax.swing.JScrollPane ScrollPaneGef;

    private javax.swing.JToolBar ToolBarGef;

    private javax.swing.JButton bAddBreak;

    private javax.swing.JButton bClearBreak;

    private javax.swing.JButton bRemBreak;

    private javax.swing.JComboBox cbSignal;

    private javax.swing.JTextField connValue;

    private javax.swing.JButton interruptButton;

    private javax.swing.JButton jButtonBackward;

    private javax.swing.JButton jButtonDelete;

    private javax.swing.JButton jButtonForward;

    private javax.swing.JButton jButtonNew;

    private javax.swing.JButton jButtonOpen;

    private javax.swing.JButton jButtonPrint;

    private javax.swing.JButton jButtonSave;

    private javax.swing.JButton jButtonToBack;

    private javax.swing.JButton jButtonToFront;

    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;

    private javax.swing.JLabel jLabel31;

    private javax.swing.JLabel jLabel4;

    private javax.swing.JLabel jLabel5;

    private javax.swing.JLabel jLabel6;

    private javax.swing.JMenu jMenu1;

    private javax.swing.JMenu jMenu3;

    private javax.swing.JMenuBar jMenuBar1;

    private javax.swing.JMenuItem jMenuItem1;

    private javax.swing.JMenuItem jMenuItem2;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JPanel jPanel10;

    private javax.swing.JPanel jPanel11;

    private javax.swing.JPanel jPanel111;

    private javax.swing.JPanel jPanel12;

    private javax.swing.JPanel jPanel13;

    private javax.swing.JPanel jPanel3;

    private javax.swing.JPanel jPanel4;

    private javax.swing.JPanel jPanel5;

    private javax.swing.JPanel jPanel6;

    private javax.swing.JPanel jPanel7;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JScrollPane jScrollPane5;

    private javax.swing.JSeparator jSeparator12;

    private javax.swing.JSeparator jSeparator2;

    private javax.swing.JSeparator jSeparator3;

    private javax.swing.JSeparator jSeparator4;

    private javax.swing.JSeparator jSeparator5;

    private javax.swing.JSplitPane jSplitPane1;

    private javax.swing.JSplitPane jSplitPane2;

    private javax.swing.JTabbedPane jTabbedPane1;

    private javax.swing.JList lBreakPointList;

    private javax.swing.JList lEvents;

    private javax.swing.JLabel messagesLabel;

    private javax.swing.JPanel messagesPane;

    private javax.swing.JScrollPane messagesScrollPane;

    private javax.swing.JTextArea messagesTextArea;

    private javax.swing.JMenuItem miLoad;

    private javax.swing.JMenuItem miNew;

    private javax.swing.JMenuItem miSave;

    private javax.swing.JMenuItem miSaveAsModule;

    private javax.swing.JPanel pModulesAvailable;

    private javax.swing.JButton resetButton;

    private javax.swing.JScrollPane spModulesAvailable;

    private javax.swing.JButton startButton;

    private javax.swing.JTextField statusField;

    private javax.swing.JButton stepButton;

    private javax.swing.JLabel tModulesAvailable;

    private javax.swing.JTextField tfTime;

    private javax.swing.JLabel jConnectionLabel;

    private javax.swing.JTextField connectionValue;
}
