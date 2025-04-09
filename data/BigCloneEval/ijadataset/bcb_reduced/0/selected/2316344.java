package fr.irisa.asap.debug.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import fr.irisa.asap.debug.change.Change_global;
import fr.irisa.asap.debug.change.Change_local;
import fr.irisa.asap.debug.debugGUI.ISpyReplayListener;
import fr.irisa.asap.debug.observation.Comparison;
import fr.irisa.asap.debug.observation.Verification;
import fr.irisa.asap.debug.utility.Config;
import fr.irisa.asap.debug.utility.IOFileManager;
import fr.irisa.asap.debug.utility.ReseqLocalLog;

public class Manager implements IManager {

    /** Log4j logger for the current class */
    private static final Logger LOG4J = Logger.getLogger(Manager.class);

    public static final int SPY = 0;

    public static final int ALL_REPLAY_ON_TIME = 1;

    public static final int ONE_REPLAY_ON_TIME = 2;

    public static final int ALL_REPLAY_STEP = 3;

    public static final int ONE_REPLAY_STEP = 4;

    /** to know the constants that the user should enter*/
    private static Vector<String> cstes;

    /** to know the constants of the application with their values*/
    private static Map<String, String> cstesValues;

    /*********************Members**********************/
    private static Manager instance = null;

    private Scenario currentScenario;

    private Scenario referenceScenario;

    private int executionNumber;

    private int replayNumber;

    private boolean finished;

    private int nbFinished;

    private boolean stopped;

    private Thread detectEnd;

    /**
	 * Constructor
	 * 
	 */
    private Manager() {
        cstes = new Vector<String>();
        cstes.add("app.browser");
        cstes.add("spy.proxy.port");
        cstes.add("spy.proxy.nbChild");
        cstes.add("pclistalllinux");
        cstes.add("portManager");
        cstes.add("portArret");
        cstes.add("nameManager");
        cstes.add("pathResRemoteLinux");
        cstes.add("resChangeGlobal");
        cstes.add("pathjardebuggerlinux");
        cstes.add("path.scripts");
        cstes.add("pathToDirectoryPub");
        cstes.add("referencePathLinux");
    }

    /**
	 * Since it's a class Singleton, this method return the only instance
	 * 
	 * @return the instance
	 */
    public static Manager instance() {
        if (instance == null) {
            instance = new Manager();
        }
        return instance;
    }

    public void setMapConfiguration(Map<String, String> map) {
        cstesValues = map;
        String workingDir = System.getProperty("user.dir");
        IOFileManager.createFileConfig(map, workingDir);
        String pathres = getPathRes();
        File fileTmp = new File(pathres);
        if (!fileTmp.exists()) fileTmp.mkdir();
        initializeExecutionNumber(pathres);
    }

    public Scenario getCurrentScenario() {
        return currentScenario;
    }

    public Vector<String> getConstantsName() {
        return cstes;
    }

    /**
	 * Write a scenario as the current Scenario and to the specified path given in parameter
	 * @param scenario which represent the scenario to write in the specified path
	 * @throws ScenarioException 
	 * @throws Exception if the current scenario has not been saved or if the writing of the log file has failed
	 */
    private void writeCurrentScenario(String path) throws ScenarioException {
        if (CurrentScenarioExists()) {
            try {
                IOFileManager.writeSerializableScenario(currentScenario, path);
                IOFileManager.writeStringScenario(currentScenario, path);
            } catch (FileNotFoundException e) {
                LOG4J.error("the path of the file is not correct in the function writeCurrentScenario\n" + path);
                e.printStackTrace();
            } catch (IOException e) {
                LOG4J.error("impossible to open the file in the directory\n" + path);
                e.printStackTrace();
            }
        } else {
            throw new ScenarioException("error, the current scenario is not initialized");
        }
    }

    private void computeNumbers(boolean mode) {
        if (mode) {
            if (executionNumber == 0) executionNumber = 1; else executionNumber++;
            replayNumber = 1;
        } else {
            String path = referenceScenario.getLogLink();
            String[] decompname = path.split("_");
            int numexec = new Integer(decompname[1]);
            int numrepl = 0;
            executionNumber = numexec;
            File res = new File(getPathRes());
            if (res.isDirectory()) {
                File[] tabFile = res.listFiles();
                for (int i = 0; i < tabFile.length; i++) {
                    File file = tabFile[i];
                    if (file.isDirectory()) {
                        String name = file.getName();
                        String[] decompnamecomp = name.split("_");
                        int numexeccomp = new Integer(decompnamecomp[1]);
                        int numreplcomp = new Integer(decompnamecomp[2]);
                        if (numexec == numexeccomp) {
                            if (numreplcomp > numrepl) {
                                numrepl = numreplcomp;
                            }
                        }
                    }
                }
            }
            replayNumber = numrepl;
            replayNumber++;
        }
    }

    private void createDirRes(boolean mode) {
        computeNumbers(mode);
        String pathres = getPathRes();
        String pathexec = pathres + "exec_" + executionNumber + "_" + replayNumber + "/";
        File direxec = new File(pathexec);
        direxec.mkdir();
        currentScenario.setLogLink(pathexec);
        HashMap<String, LinkedList<String>> listpc = currentScenario.getPcList();
        for (int i = 1; i <= listpc.size(); i++) {
            String pathhost = pathexec + "host" + i;
            File dirhost = new File(pathhost);
            dirhost.mkdir();
        }
    }

    private String createDirVue() {
        String pathvue = getPathVue();
        File filevue = new File(pathvue);
        filevue.mkdir();
        HashMap<String, LinkedList<String>> listpc = currentScenario.getPcList();
        for (int i = 1; i <= listpc.size(); i++) {
            String pathhost = pathvue + "/host" + i;
            File dirhost = new File(pathhost);
            dirhost.mkdir();
        }
        return pathvue;
    }

    private void writeMachineCsteValues() {
        cstesValues.put("nbHost", String.valueOf(currentScenario.getPcNumber()));
        HashMap<String, LinkedList<String>> listpc = currentScenario.getPcList();
        Set<String> vect = listpc.keySet();
        Iterator it = vect.iterator();
        int num = 1;
        while (it.hasNext()) {
            String name = (String) it.next();
            LinkedList<String> listport = listpc.get(name);
            String res = "";
            for (int i = 0; i < listport.size(); i++) {
                res += listport.get(i);
            }
            cstesValues.put("machines.host" + num, name);
            cstesValues.put("machines.host" + num + ".ports", res);
            num++;
        }
        String workingDir = System.getProperty("user.dir");
        IOFileManager.createFileConfig(cstesValues, workingDir);
    }

    private void createFileConfDebugger() {
        TreeMap<String, String> tabConfig = new TreeMap<String, String>();
        tabConfig.put("spy.proxy.port", cstesValues.get("spy.proxy.port"));
        tabConfig.put("spy.proxy.nbChild", cstesValues.get("spy.proxy.nbChild"));
        tabConfig.put("portManager", cstesValues.get("portManager"));
        tabConfig.put("portArret", cstesValues.get("portArret"));
        tabConfig.put("nameManager", cstesValues.get("nameManager"));
        tabConfig.put("nbHost", cstesValues.get("nbHost"));
        tabConfig.put("pathResRemoteLinux", cstesValues.get("pathResRemoteLinux"));
        cstesValues.put("nbHost", String.valueOf(currentScenario.getPcNumber()));
        HashMap<String, LinkedList<String>> listpc = currentScenario.getPcList();
        Set<String> vect = listpc.keySet();
        Iterator it = vect.iterator();
        int num = 1;
        while (it.hasNext()) {
            String name = (String) it.next();
            LinkedList<String> listport = listpc.get(name);
            String res = "";
            for (int i = 0; i < listport.size(); i++) {
                res += listport.get(i);
            }
            tabConfig.put("machines.host" + num, name);
            tabConfig.put("machines.host" + num + ".ports", res);
            cstesValues.put("machines.host" + num, name);
            cstesValues.put("machines.host" + num + ".ports", res);
            num++;
        }
        String workingDir = System.getProperty("user.dir");
        IOFileManager.createFileConfig(cstesValues, workingDir);
        for (int i = 1; i <= listpc.size(); i++) {
            String pathvue = getPathVue();
            String pathfile = pathvue + "/host" + i;
            tabConfig.put("spy.matterId", String.valueOf(i));
            tabConfig.put("nbHost", String.valueOf(currentScenario.getPcNumber()));
            IOFileManager.createFileConfig(tabConfig, pathfile);
        }
    }

    private void copyJars(String nameJar) {
        HashMap<String, LinkedList<String>> listpc = currentScenario.getPcList();
        for (int i = 1; i <= listpc.size(); i++) {
            String pathvue = getPathVue();
            String pathfilejardistributed = pathvue + "/host" + i + "/dist-app.jar";
            String pathfilejardebuggeur = pathvue + "/host" + i + "/spy.jar";
            File sourcedistributed = new File(nameJar);
            File sourcedebuggeur = new File(Config.getString("pathjardebuggerlinux"));
            File destinationdistributed = new File(pathfilejardistributed);
            File destinationdebuggeur = new File(pathfilejardebuggeur);
            if (destinationdistributed.exists()) {
                destroyFile(destinationdistributed);
            }
            copier(sourcedistributed, destinationdistributed);
            if (destinationdebuggeur.exists()) {
                destroyFile(destinationdebuggeur);
            }
            copier(sourcedebuggeur, destinationdebuggeur);
            String pathPub = pathvue + "/host" + i + "/pub";
            File dirpub = new File(pathPub);
            dirpub.mkdir();
            String rootPub = Config.getString("pathToDirectoryPub");
            File root = new File(rootPub);
            File[] tabFile = root.listFiles();
            for (int q = 0; q < tabFile.length; q++) {
                File fi = tabFile[q];
                if (fi.isFile()) {
                    File des = new File(pathPub + "/" + fi.getName());
                    copier(fi, des);
                }
            }
        }
    }

    public void deploy(String nameJar) throws SSHException, ScenarioException {
        createDirRes(true);
        String pathvue = createDirVue();
        createFileConfDebugger();
        copyJars(nameJar);
        HashMap<String, LinkedList<String>> listpc = currentScenario.getPcList();
        currentScenario.setAppliName(nameJar, String.valueOf(executionNumber + "_" + replayNumber));
        SSHManager.deployDirectory(listpc, pathvue, Config.getString("pathResRemoteLinux"));
        File filevue = new File(pathvue);
        destroyFile(filevue);
    }

    public void deployReplayAllTime(String nameJar) throws SSHException, ScenarioException {
        createDirRes(false);
        String pathvue = createDirVue();
        createFileConfDebugger();
        copyTheReferenceFile();
        copyJars(nameJar);
        HashMap<String, LinkedList<String>> listpc = currentScenario.getPcList();
        currentScenario.setAppliName(nameJar, String.valueOf(executionNumber + "_" + replayNumber));
        SSHManager.deployDirectory(listpc, pathvue, Config.getString("pathResRemoteLinux"));
        File filevue = new File(pathvue);
        destroyFile(filevue);
    }

    private void copyTheReferenceFile() {
        Change_local ch = new Change_local();
        String str = referenceScenario.getLogLink();
        String[] tab = str.split("/");
        str = tab[tab.length - 1];
        tab = str.split("_");
        String ss = tab[1] + "_" + tab[2];
        ch.launchScript(referenceScenario.getLogLink(), ss);
        HashMap<String, LinkedList<String>> listpc = currentScenario.getPcList();
        for (int i = 1; i <= listpc.size(); i++) {
            String pathvue = getPathVue();
            String vuehost = pathvue + "/host" + i + "/replayFileStation" + i + ".xml";
            String pathref = referenceScenario.getLogLink();
            String refhost = pathref + "host" + i + "/replayFileStation" + i + ".xml";
            File fsource = new File(refhost);
            File fdest = new File(vuehost);
            copier(fsource, fdest);
        }
    }

    /**
	 * Destroy a directory and all its under-directory
	 * @param path the root of the directory
	 */
    private void destroyFile(File path) {
        if (path.isDirectory()) {
            File[] tab = path.listFiles();
            for (int i = 0; i < tab.length; i++) {
                destroyFile(tab[i]);
            }
            path.delete();
        } else {
            path.delete();
        }
    }

    public void save() throws SSHException, ScenarioException {
        String pathRoot = getPathRoot();
        String pathTmp = pathRoot + "tmp";
        File fileTmp = new File(pathTmp);
        fileTmp.mkdir();
        SSHManager.getLog(pathTmp, Config.getString("pathResRemoteLinux"), currentScenario.getPcList());
        String pathRes = getPathRes();
        String pathExec = pathRes + "exec_" + executionNumber + "_" + replayNumber + "/";
        currentScenario.setLogLink(pathExec);
        writeCurrentScenario(pathExec);
        File[] tabFile = fileTmp.listFiles();
        int ff = 0;
        while (ff < tabFile.length) {
            File source = tabFile[ff];
            if (source.isFile()) {
                String nameFile = source.getName();
                int num = Character.getNumericValue(nameFile.charAt(14));
                File destination = new File(pathExec + "host" + num + "/" + nameFile);
                copier(source, destination);
                source.delete();
            }
            ff++;
        }
        fileTmp.delete();
    }

    public void initScenario() {
        currentScenario = new Scenario();
    }

    public void addPcList(String pcName) throws Exception {
        currentScenario.addPcName(pcName);
    }

    public void setPcListFromFile(String fileName) throws Exception {
        currentScenario.setPcListFromFile(fileName);
    }

    public void removePcFromList(String pcname) {
        currentScenario.removePcFromList(pcname);
    }

    public void loadReferenceScenario(String path) throws FileNotFoundException, IOException, ClassNotFoundException {
        referenceScenario = readScenario(path);
    }

    public void loadCurrentScenario(String path) throws FileNotFoundException, IOException, ClassNotFoundException {
        currentScenario = readScenario(path);
    }

    /**
	 * read the Scenario in the corresponding path
	 * @param path designe the path where the Scenario to read will be found
	 * @throws ClassNotFoundException if the class of the serialized object is not found 
	 * @throws IOException if the file can't be opened
	 * @throws FileNotFoundException if the file is not found
	 */
    private Scenario readScenario(String path) throws FileNotFoundException, IOException, ClassNotFoundException {
        return (IOFileManager.readSerializableScenario(path));
    }

    public void launchDistantDebuggerAndApplication(int mod, ISpyReplayListener lis) throws SSHException, ScenarioException {
        if (CurrentScenarioExists()) {
            switch(mod) {
                case SPY:
                    break;
                case ALL_REPLAY_ON_TIME:
                    break;
                case ALL_REPLAY_STEP:
                    LOG4J.warn("not implemented for this option - ALL_REPLAY_STEP");
                    break;
                case ONE_REPLAY_ON_TIME:
                    LOG4J.warn("not implemented for this option - ONE_REPLAY_ON_TIME");
                    break;
                case ONE_REPLAY_STEP:
                    LOG4J.warn("not implemented for this option - ONE_REPLAY_STEP");
                    break;
            }
            finished = false;
            nbFinished = 0;
            detectEnd = new Thread() {

                @Override
                public void run() {
                    try {
                        ServerSocket s = new ServerSocket(Config.getInt("portManager"));
                        while (!(finished || stopped)) {
                            System.out.println("manager va attendre fin");
                            Socket soc = s.accept();
                            System.out.println("une fin de +");
                            nbFinished++;
                            if (nbFinished == currentScenario.getPcNumber()) {
                                finished = true;
                            }
                            soc.close();
                        }
                        System.out.println("tout le monde il est mort");
                        s.close();
                    } catch (IOException e) {
                        LOG4J.fatal(e);
                    }
                }
            };
            detectEnd.start();
            try {
                SSHManager.launchDistantDebuggerAndApplication(mod, currentScenario.getPcList());
                detectEnd.join();
                if (!stopped) {
                    save();
                    if (mod == SPY) {
                        ReseqLocalLog rql = new ReseqLocalLog();
                        rql.launchScript(currentScenario.getLogLink());
                    }
                    if (lis != null) lis.EndOfSpy();
                } else {
                    throw new SSHException(null, null);
                }
            } catch (SSHException e) {
                destroyFile(new File(currentScenario.getLogLink()));
                initializeExecutionNumber(getPathRes());
                stopped = false;
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new ScenarioException("error, the current scenario is not initialized");
        }
    }

    public void launchVerification(String path, Vector<String> pclist) throws FileNotFoundException, IOException, ClassNotFoundException {
        Change_global c = new Change_global();
        String s = String.valueOf(executionNumber + "_" + replayNumber);
        c.launchScript(currentScenario.getLogLink(), s);
        writeMachineCsteValues();
        IOFileManager.writePcListFileVectorPid(currentScenario.getPcList(), pclist, Config.getString("pclistalllinux"));
        Verification.launchScript(Config.getString("pclistalllinux"), currentScenario.getLogLink());
    }

    public void launchComparison(Vector<String> pclist) throws FileNotFoundException, IOException, ClassNotFoundException {
        Change_global c1 = new Change_global();
        String s = String.valueOf(executionNumber + "_" + replayNumber);
        c1.launchScript(currentScenario.getLogLink(), s);
        Change_global c2 = new Change_global();
        String str = referenceScenario.getLogLink();
        String[] tab = str.split("/");
        str = tab[tab.length - 1];
        tab = str.split("_");
        String ss = tab[1] + "_" + tab[2];
        c2.launchScript(referenceScenario.getLogLink(), ss);
        Properties sys = System.getProperties();
        String os = sys.getProperty("os.name");
        if (os.endsWith("NT") || os.endsWith("2000") || os.endsWith("XP")) {
            IOFileManager.writePcListFileVectorPid(currentScenario.getPcList(), pclist, Config.getString("pclistallwindows"));
            Comparison.launchScript(Config.getString("pclistallwindows"), currentScenario.getLogLink(), referenceScenario.getLogLink());
        } else {
            writeMachineCsteValues();
            IOFileManager.writePcListFileVectorPid(currentScenario.getPcList(), pclist, Config.getString("pclistalllinux"));
            Comparison.launchScript(Config.getString("pclistalllinux"), currentScenario.getLogLink(), referenceScenario.getLogLink());
        }
    }

    public boolean CurrentScenarioExists() {
        return (currentScenario != null);
    }

    public void StopExecution() throws Exception {
        stopped = true;
        HashMap<String, LinkedList<String>> vect = currentScenario.getPcList();
        IOFileManager.writePcListFile(vect, Config.getString("pclistalllinux"));
        Process process;
        String cmd = "sh " + Config.getString("path.scripts") + "clean_all.sh " + Config.getString("pclistalllinux") + " " + Config.getString("pathResRemoteLinux");
        System.out.println(cmd);
        process = Runtime.getRuntime().exec(cmd);
        process.waitFor();
        detectEnd.interrupt();
        Socket soc = new Socket(Config.getString("nameManager"), Config.getInt("portManager"));
        OutputStream os = soc.getOutputStream();
        ObjectOutputStream objs = new ObjectOutputStream(os);
        try {
            objs.writeObject("FINISHED");
            objs.flush();
            objs.close();
        } catch (SocketException e) {
        }
        os.close();
        soc.close();
    }

    public void eraseCurrentScenario() {
        currentScenario = null;
    }

    public Vector<String> getStations() {
        HashMap<String, LinkedList<String>> map = currentScenario.getPcList();
        Set<String> s = map.keySet();
        return new Vector<String>(s);
    }

    /**
	 * This function permits to init the execution and replay nimber when we re launch the program.
	 * In reading the directory we can search these numbers.
	 * @param path the path where we should find the numbers.
	 */
    private void initializeExecutionNumber(String path) {
        int numexecref = 0;
        int numreplayref = 0;
        File dirTmp = new File(path);
        if (dirTmp.isDirectory()) {
            File[] tabFile = dirTmp.listFiles();
            for (int i = 0; i < tabFile.length; i++) {
                File file = tabFile[i];
                if (file.isDirectory()) {
                    String name = file.getName();
                    String[] decompname = name.split("_");
                    int numexec = new Integer(decompname[1]);
                    if (numexec > numexecref) {
                        numexecref = numexec;
                        numreplayref = 1;
                    }
                }
            }
            executionNumber = numexecref;
            replayNumber = numreplayref;
        } else {
            LOG4J.fatal("error in the function initializeExecutionNumber, the path is not a directory !!!");
        }
    }

    /**
	 * This function returns the path of the main directory<br>
	 * @return the path .
	 */
    private String getPathRoot() {
        Properties sys = System.getProperties();
        String os = sys.getProperty("os.name");
        String refPath = "";
        if (os.endsWith("NT") || os.endsWith("2000") || os.endsWith("XP")) {
            refPath = Config.getString("referencePathWindows");
        } else {
            refPath = Config.getString("referencePathLinux");
        }
        return refPath;
    }

    /**
	 * This function returns the path where we should stored the files.<br>
	 * @return the path where to put the files.
	 */
    private String getPathRes() {
        Properties sys = System.getProperties();
        String os = sys.getProperty("os.name");
        String refPath = "";
        if (os.endsWith("NT") || os.endsWith("2000") || os.endsWith("XP")) {
            refPath = Config.getString("referencePathWindows");
        } else {
            refPath = Config.getString("referencePathLinux");
        }
        String pathres = refPath + "res/";
        return pathres;
    }

    /**
	 * This function returns the path where we should stored the files.<br>
	 * @return the path where to put the files.
	 */
    private String getPathVue() {
        Properties sys = System.getProperties();
        String os = sys.getProperty("os.name");
        String refPath = "";
        if (os.endsWith("NT") || os.endsWith("2000") || os.endsWith("XP")) {
            refPath = Config.getString("referencePathWindows");
        } else {
            refPath = Config.getString("referencePathLinux");
        }
        String pathres = refPath + "vue";
        return pathres;
    }

    /**
	 * This function copies a file in an other file
	 * @param source the first file
	 * @param destination the second file
	 * @return true if the copying is a success
	 */
    private boolean copier(File source, File destination) {
        boolean resultat = false;
        java.io.FileInputStream sourceFile = null;
        java.io.FileOutputStream destinationFile = null;
        try {
            destination.createNewFile();
            sourceFile = new java.io.FileInputStream(source);
            destinationFile = new java.io.FileOutputStream(destination);
            byte buffer[] = new byte[512 * 1024];
            int nbLecture;
            while ((nbLecture = sourceFile.read(buffer)) != -1) {
                destinationFile.write(buffer, 0, nbLecture);
            }
            resultat = true;
        } catch (java.io.FileNotFoundException f) {
        } catch (java.io.IOException e) {
        } finally {
            try {
                sourceFile.close();
            } catch (Exception e) {
            }
            try {
                destinationFile.close();
            } catch (Exception e) {
            }
        }
        return (resultat);
    }

    public void setReferenceScenario(Scenario sce) {
        referenceScenario = sce;
    }

    public static void main(String[] args) throws Exception {
        DOMConfigurator.configure("pub/log4j.xml");
        Manager m = Manager.instance();
        m.initScenario();
        m.addPcList("pion.educ.insa:45678");
        m.addPcList("pat.educ.insa:45678");
        m.deployReplayAllTime("/home/grobinea/jaressai/dist-app.jar");
        m.launchDistantDebuggerAndApplication(Manager.ALL_REPLAY_ON_TIME, null);
        m.save();
        System.out.println("finreplay");
    }
}
