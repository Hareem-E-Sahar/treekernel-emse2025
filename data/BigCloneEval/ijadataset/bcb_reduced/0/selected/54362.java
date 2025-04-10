package agentgui.simulationService.distribution;

import jade.util.leap.ArrayList;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import agentgui.core.application.Application;
import agentgui.simulationService.agents.ServerSlaveAgent;
import agentgui.simulationService.ontology.RemoteContainerConfig;

/**
 * This class is only used by the {@link ServerSlaveAgent} of the background
 * system. It enables the agent to start a new process in order to extend
 * a remote JADE platform and join that platform with a new container.
 * 
 * @see ServerSlaveAgent
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg - Essen
 */
public class JadeRemoteStart extends Thread {

    /** Constant for a memory of 16 MB. */
    public static final String jvmMemo0016MB = "16m";

    /** Constant for a memory of 32 MB. */
    public static final String jvmMemo0032MB = "32m";

    /** Constant for a memory of 64 MB. */
    public static final String jvmMemo0064MB = "64m";

    /** Constant for a memory of 128 MB. */
    public static final String jvmMemo0128MB = "128m";

    /** Constant for a memory of 256 MB. */
    public static final String jvmMemo0256MB = "256m";

    /** Constant for a memory of 512 MB. */
    public static final String jvmMemo0512MB = "512m";

    /** Constant for a memory of 1024 MB. */
    public static final String jvmMemo1024MB = "1024m";

    private boolean jvmMemAllocUseDefaults = true;

    private String jvmMemAllocInitial = JadeRemoteStart.jvmMemo0032MB;

    private String jvmMemAllocMaximum = JadeRemoteStart.jvmMemo0128MB;

    private boolean jadeIsRemoteContainer = true;

    private boolean jadeShowGUI = true;

    private String jadeShowGUIAgentName = "rma.";

    private String jadeServices = "jade.core.event.NotificationService;jade.core.mobility.AgentMobilityService;mas.service.SimulationService;";

    private String jadeHost = "localhost";

    private String jadePort = Application.RunInfo.getJadeLocalPort().toString();

    private String jadeContainerName = "remote";

    private ArrayList jadeJarInclude = null;

    private ArrayList jadeJarIncludeClassPath = new ArrayList();

    private File extJarFolder = null;

    private final String pathBaseDir = Application.RunInfo.PathBaseDir();

    /**
	 * Default constructor.
	 *
	 * @param reCoCo the RemoteContainerConfig
	 */
    public JadeRemoteStart(RemoteContainerConfig reCoCo) {
        jadeIsRemoteContainer = reCoCo.getJadeIsRemoteContainer();
        if (reCoCo.getJvmMemAllocInitial() == null && reCoCo.getJvmMemAllocMaximum() == null) {
            this.jvmMemAllocUseDefaults = true;
            this.jvmMemAllocInitial = JadeRemoteStart.jvmMemo0032MB;
            this.jvmMemAllocMaximum = JadeRemoteStart.jvmMemo0128MB;
        } else {
            this.jvmMemAllocUseDefaults = false;
            this.jvmMemAllocInitial = reCoCo.getJvmMemAllocInitial();
            this.jvmMemAllocMaximum = reCoCo.getJvmMemAllocMaximum();
        }
        this.jadeShowGUI = reCoCo.getJadeShowGUI();
        if (reCoCo.getJadeServices() != null) {
            this.jadeServices = reCoCo.getJadeServices();
        }
        if (reCoCo.getJadeHost() != null) {
            this.jadeHost = reCoCo.getJadeHost();
        }
        if (reCoCo.getJadePort() != null) {
            this.jadePort = reCoCo.getJadePort();
        }
        if (reCoCo.getJadeContainerName() != null) {
            this.jadeContainerName = reCoCo.getJadeContainerName();
        }
        if (reCoCo.getJadeJarIncludeList() != null) {
            this.jadeJarInclude = (ArrayList) reCoCo.getJadeJarIncludeList();
            this.handelExternalJars();
        }
    }

    /**
	 * If the request of a remote container contains external jars, download
	 * them and include them in the current CLASSPATH.
	 */
    private void handelExternalJars() {
        String pathSep = Application.RunInfo.AppPathSeparatorString();
        String destinPath = Application.RunInfo.PathDownloads(false);
        String projectSubFolder = null;
        String downloadProtocol = "";
        for (int i = 0; i < jadeJarInclude.size(); i++) {
            String httpJarFile = (String) jadeJarInclude.get(i);
            if (projectSubFolder == null) {
                projectSubFolder = httpJarFile.replace("http://", "");
                int cut = projectSubFolder.indexOf("/") + 1;
                projectSubFolder = projectSubFolder.substring(cut, projectSubFolder.length());
                cut = projectSubFolder.indexOf("/");
                projectSubFolder = projectSubFolder.substring(0, cut);
                projectSubFolder += pathSep;
                destinPath = destinPath + projectSubFolder;
                extJarFolder = new File(destinPath);
                if (extJarFolder.exists()) {
                    deleteFolder(extJarFolder);
                    extJarFolder.delete();
                }
                extJarFolder.mkdir();
            }
            File remoteFile = new File(httpJarFile);
            String destinFile = destinPath + remoteFile.getAbsoluteFile().getName();
            new Download(httpJarFile, destinFile);
            String ClassPathEntry = "./" + destinFile.replace(pathSep, "/") + ";";
            jadeJarIncludeClassPath.add(ClassPathEntry);
            if (downloadProtocol.equals("") == false) {
                downloadProtocol += "|";
            }
            downloadProtocol += remoteFile.getAbsoluteFile().getName();
        }
        if (downloadProtocol.equals("") == false) {
            downloadProtocol = "Download to '" + destinPath + "': " + downloadProtocol + "";
            System.out.println(downloadProtocol);
        }
    }

    /**
 	 * Deletes a folder and all sub elements.
 	 *
 	 * @param directory the directory
 	 */
    private void deleteFolder(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                deleteFolder(file);
            }
            file.delete();
        }
    }

    /**
	 * Action for the Thread-Start. Starts a new JVM with a new Jade-Instance
	 */
    @Override
    public void run() {
        this.startJade();
    }

    /**
	 * This Method starts a Jade-Platform within a new Java Virtual Machine.
	 */
    public void startJade() {
        String os = System.getProperty("os.name");
        String java = "";
        String javaVMArgs = "";
        String classPath = "";
        String jade = "";
        String jadeArgs = "";
        java = "java";
        if (jvmMemAllocUseDefaults == false) {
            javaVMArgs = "-Xms" + jvmMemAllocInitial + " -Xmx" + jvmMemAllocMaximum;
        }
        classPath = getClassPath(jadeServices);
        if (os.toLowerCase().contains("windows") == true) {
        } else if (os.toLowerCase().contains("linux") == true) {
            classPath = classPath.replaceAll(";", ":");
        }
        jade += "agentgui.core.application.Application -jade" + " ";
        if (jadeServices != null) {
            jadeArgs += "-services  " + jadeServices + " ";
        }
        if (jadeIsRemoteContainer) {
            jadeArgs += "-container ";
            jadeArgs += "-container-name " + jadeContainerName + " ";
        }
        jadeArgs += "-host " + jadeHost + " ";
        jadeArgs += "-port " + jadePort + " ";
        if (jadeShowGUI) {
            jadeArgs += jadeShowGUIAgentName + jadeContainerName + ":jade.tools.rma.rma ";
        }
        jadeArgs = jadeArgs.trim();
        String execute = java + " " + javaVMArgs + " " + classPath + " " + jade + " " + jadeArgs;
        execute = execute.replace("  ", " ");
        System.out.println("Execute: " + execute);
        try {
            String[] arg = execute.split(" ");
            ProcessBuilder proBui = new ProcessBuilder(arg);
            proBui.redirectErrorStream(true);
            proBui.directory(new File(pathBaseDir));
            Process p = proBui.start();
            Scanner in = new Scanner(p.getInputStream()).useDelimiter("\\Z");
            Scanner err = new Scanner(p.getErrorStream()).useDelimiter("\\Z");
            while (in.hasNextLine() || err.hasNextLine()) {
                if (in.hasNextLine()) {
                    System.out.println("[" + jadeContainerName + "]: " + in.nextLine());
                }
                if (err.hasNextLine()) {
                    System.err.println("[" + jadeContainerName + "]: " + err.nextLine());
                }
            }
            System.out.println("Killed Container [" + jadeContainerName + "]");
            if (extJarFolder != null) {
                if (extJarFolder.exists() == true) {
                    deleteFolder(extJarFolder);
                    extJarFolder.delete();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
	 * This method configures the CLASSPATH for the remote-container.
	 *
	 * @param ServiceList the service list
	 * @return the new class path
	 */
    private String getClassPath(String ServiceList) {
        String classPath = "";
        classPath += "-classpath ";
        classPath += ".;";
        String agentGuiJar = Application.RunInfo.AppFileRunnableJar(false);
        agentGuiJar = agentGuiJar.replace("\\", "/");
        classPath += "./" + agentGuiJar + ";";
        if (jadeJarIncludeClassPath.size() > 0) {
            for (int i = 0; jadeJarIncludeClassPath.size() > i; i++) {
                String jar = (String) jadeJarIncludeClassPath.get(i);
                classPath += jar;
            }
        }
        return classPath;
    }

    /**
	 * Checks if is memory allocation for the new JVM will be the default one.
	 *
	 * @return the jvmMemAllocUseDefaults
	 */
    public boolean isJVMMemAllocUseDefaults() {
        return jvmMemAllocUseDefaults;
    }

    /**
	 * Sets the memory allocation for the new JVM to be the default value or not.
	 *
	 * @param useDefaults the new jVM mem alloc use defaults
	 */
    public void setJVMMemAllocUseDefaults(boolean useDefaults) {
        this.jvmMemAllocUseDefaults = useDefaults;
    }

    /**
	 * @return the jvmMemAllocInitial
	 */
    public String getJVMMemAllocInitial() {
        return jvmMemAllocInitial;
    }

    /**
	 * @param jvmMemAllocInitial the jvmMemAllocInitial to set
	 */
    public void setJVMMemAllocInitial(String jvmMemAllocInitial) {
        this.jvmMemAllocInitial = jvmMemAllocInitial;
    }

    /**
	 * @return the jvmMemAllocMaximum
	 */
    public String getJVMMemAllocMaximum() {
        return jvmMemAllocMaximum;
    }

    /**
	 * @param jvmMemAllocMaximum the jvmMemAllocMaximum to set
	 */
    public void setJVMMemAllocMaximum(String jvmMemAllocMaximum) {
        this.jvmMemAllocMaximum = jvmMemAllocMaximum;
    }

    /**
	 * @return the jadeIsRemoteContainer
	 */
    public boolean isJADEIsRemoteContainer() {
        return jadeIsRemoteContainer;
    }

    /**
	 * @param jadeIsRemoteContainer the jadeIsRemoteContainer to set
	 */
    public void setJADEIsRemoteContainer(boolean jadeIsRemoteContainer) {
        this.jadeIsRemoteContainer = jadeIsRemoteContainer;
    }

    /**
	 * @return the jadeShowGUI
	 */
    public boolean isJADEShowGUI() {
        return jadeShowGUI;
    }

    /**
	 * @param jadeShowGUI the jadeShowGUI to set
	 */
    public void setJADEShowGUI(boolean jadeShowGUI) {
        this.jadeShowGUI = jadeShowGUI;
    }

    /**
	 * @return the jadeShowGUIAgentName
	 */
    public String getJadeShowGUIAgentName() {
        return jadeShowGUIAgentName;
    }

    /**
	 * @param jadeShowGUIAgentName the jadeShowGUIAgentName to set
	 */
    public void setJadeShowGUIAgentName(String jadeShowGUIAgentName) {
        this.jadeShowGUIAgentName = jadeShowGUIAgentName;
    }

    /**
	 * @return the jadeHost
	 */
    public String getJADEHost() {
        return jadeHost;
    }

    /**
	 * @param jadeHost the jadeHost to set
	 */
    public void setJADEHost(String jadeHost) {
        this.jadeHost = jadeHost;
    }

    /**
	 * @return the jadePort
	 */
    public String getJADEPort() {
        return jadePort;
    }

    /**
	 * @param jadePort the jadePort to set
	 */
    public void setJADEPort(String jadePort) {
        this.jadePort = jadePort;
    }

    /**
	 * @return the jadeContainerName
	 */
    public String getJADEContainerName() {
        return jadeContainerName;
    }

    /**
	 * @param jadeContainerName the jadeContainerName to set
	 */
    public void setJADEContainerName(String jadeContainerName) {
        this.jadeContainerName = jadeContainerName;
    }

    /**
	 * @param jadeServices the jadeServices to set
	 */
    public void setJADEServices(String jadeServices) {
        this.jadeServices = jadeServices;
    }

    /**
	 * @return the jadeServices
	 */
    public String getJADEServices() {
        return jadeServices;
    }
}
