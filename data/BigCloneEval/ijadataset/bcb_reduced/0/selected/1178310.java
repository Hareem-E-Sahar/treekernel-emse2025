package com.topq.qtptc;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import javax.imageio.ImageIO;
import jsystem.framework.IgnoreMethod;
import jsystem.framework.report.Reporter;
import jsystem.framework.report.ReporterHelper;
import jsystem.framework.system.SystemObjectImpl;
import jsystem.utils.FileUtils;
import jsystem.utils.exec.Command;
import jsystem.utils.exec.Execute;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.sun.org.apache.xpath.internal.XPathAPI;
import com.topq.monitor.ProcessMonitor;
import com.topq.remotemachine.PSExecRemoteProcess;
import com.topq.remotemachine.RemoteMachine;

@SuppressWarnings("restriction")
public class QtpTcManager extends SystemObjectImpl {

    public static final int INITIAL_INDEX = -1;

    public static enum QtpTcStatus {

        Pass, Fail, TimeOut, Warning;

        public static QtpTcStatus valueOfIgnoreCase(String value) {
            for (QtpTcStatus v : values()) {
                if (v.name().equalsIgnoreCase(value)) {
                    return v;
                }
            }
            return null;
        }
    }

    public static final int DefaultTimeOut = 60 * 5;

    private ProcessMonitor processManager;

    private File executeFile = null;

    private File resultFile = null;

    private File settingsFile = null;

    private boolean debug = false;

    private Document executeDoc = null;

    private Document resultDoc = null;

    private Document settingsDoc = null;

    private int sampleTime = 1000;

    private int defaultTimeout = DefaultTimeOut;

    private int commandId = 0;

    private ExecutionCmd currentCmd = null;

    private ExecutionResult currentResult = null;

    private File toolPath;

    private File toolProjectSuitePath;

    private String toolProjectName;

    private boolean exitOnStop;

    private StringBuffer logger;

    private String host = "127.0.0.1";

    private String user = "Administrator";

    private String password = "123456";

    private PSExecRemoteProcess process;

    private boolean clearInterfaceOnInit = true;

    @Override
    public void init() throws Exception {
        super.init();
        if (clearInterfaceOnInit) {
            report(getTitle("Clearing data from XML interface files"));
            clearInterface(INITIAL_INDEX);
        }
        logger = new StringBuffer();
    }

    @Override
    public void close() {
        report(getTitle("Bridge commands logger"), logger.toString(), true);
        super.close();
    }

    private void initData() throws Exception {
        executeDoc = FileUtils.readDocumentFromFile(getExecuteFile());
        resultDoc = FileUtils.readDocumentFromFile(resultFile);
        settingsDoc = FileUtils.readDocumentFromFile(settingsFile);
        Node commands = XPathAPI.selectSingleNode(executeDoc, "//commands");
        commandId = Integer.parseInt(commands.getAttributes().getNamedItem("currentIndex").getTextContent());
    }

    /**
	 * Execute a QTP/TC command
	 * 
	 * @param cmd
	 * @throws Exception
	 */
    public void executeCmd(ExecutionCmd cmd) throws Exception {
        if (debug) {
            return;
        }
        currentCmd = cmd;
        commandId++;
        currentCmd.setId(commandId);
        currentResult = new ExecutionResult();
        writeExecutionCmd();
        setDefaultTimeout(cmd.getTimeOut());
        readExecutionResult();
        setDefaultTimeout(DefaultTimeOut);
        ArrayList<QtpTcReport> reports = currentResult.getReportList();
        for (QtpTcReport r : reports) {
            ArrayList<String> attachements = r.getAttachements();
            for (String attachement : attachements) {
                report.addLink(attachement, attachement);
            }
        }
        report(getTitle("Command: " + currentCmd.getName() + " Return status: " + currentResult.getStatus()), currentCmd.toString() + "\n\n\n\n" + currentResult.toString(), currentResult.getStatusAsInt());
        String logPath;
        int beginIndex = getCurrentResult().toString().indexOf("link:") + 5;
        int endIndex = getCurrentResult().toString().indexOf("endoflink");
        if (beginIndex >= 0 && endIndex >= 0) {
            logPath = getCurrentResult().toString().substring(beginIndex, endIndex);
            if (!isLocalHost()) {
                logPath = "\\\\" + getHost() + "\\" + logPath.replaceFirst(":", "\\$");
            }
            File logDirectory = new File(logPath);
            String[] logDirectoryList = logDirectory.list();
            if (logDirectoryList.length > 0) {
                String mylogFile1 = "";
                String mylogFile2 = "";
                String mylogFile3 = "";
                long myDate1 = 0;
                long myDate2 = 0;
                long myDate3 = 0;
                for (String logFile : logDirectoryList) {
                    if (logFile.contains(".png")) {
                        File myFile = new File(logPath + logFile);
                        long myCurrentDate = myFile.lastModified();
                        if (myCurrentDate > myDate3) {
                            myDate3 = myCurrentDate;
                            mylogFile3 = logPath + logFile;
                        }
                    }
                }
                for (String logFile : logDirectoryList) {
                    if (logFile.contains(".png")) {
                        File myFile = new File(logPath + logFile);
                        long myCurrentDate = myFile.lastModified();
                        if ((myCurrentDate > myDate2) && (myCurrentDate < myDate3)) {
                            myDate2 = myCurrentDate;
                            mylogFile2 = logPath + logFile;
                        }
                    }
                }
                for (String logFile : logDirectoryList) {
                    if (logFile.contains(".png")) {
                        File myFile = new File(logPath + logFile);
                        long myCurrentDate = myFile.lastModified();
                        if ((myCurrentDate > myDate1) && (myCurrentDate < myDate2)) {
                            myDate1 = myCurrentDate;
                            mylogFile1 = logPath + logFile;
                        }
                    }
                }
                if (mylogFile1 != "") {
                    ReporterHelper.copyFileToReporterAndAddLink(report, new File(mylogFile1), getTitle("Screenshot"));
                }
                if (mylogFile2 != "") {
                    ReporterHelper.copyFileToReporterAndAddLink(report, new File(mylogFile2), getTitle("Screenshot"));
                }
                if (mylogFile3 != "") {
                    ReporterHelper.copyFileToReporterAndAddLink(report, new File(mylogFile3), getTitle("Screenshot"));
                }
            } else {
                report(getTitle("No file was found in the log Directory"));
            }
        }
    }

    /**
	 * Get the last execution result
	 * 
	 * @return
	 * @throws Exception
	 */
    public ExecutionResult getResult() throws Exception {
        return currentResult;
    }

    /**
	 * Clear all interface files and start new log file
	 * 
	 * @throws Exception
	 */
    public void clearInterface(int index) throws Exception {
        if (debug) {
            return;
        }
        writeSharedFiles(XmlUtils.parseString("<execute>\n\t<commands currentIndex=\"" + index + "\"/>\n</execute>"), getExecuteFile());
        writeSharedFiles(XmlUtils.parseString("<results/>"), getResultFile());
        writeSharedFiles(XmlUtils.parseString("<settings/>"), getSettingsFile());
        initData();
    }

    private void writeExecutionCmd() throws Exception {
        Node commands = XPathAPI.selectSingleNode(executeDoc, "//commands");
        commands.appendChild(currentCmd.toXml(executeDoc));
        commands.getAttributes().getNamedItem("currentIndex").setNodeValue("" + commandId);
        writeSharedFiles(executeDoc, getExecuteFile());
        logger.append(XmlUtils.documentToString(executeDoc));
    }

    private void writeSharedFiles(Document document, File file) throws Exception {
        int retry = 30;
        while (retry > 0) {
            try {
                FileUtils.saveDocumentToFile(document, file);
                return;
            } catch (Exception e) {
                if (retry-- == 0) {
                    throw new Exception("Failed to write 3 times. " + e.getMessage());
                }
                Thread.sleep(1000);
            }
        }
    }

    public void launchTestComplete(boolean silentMode, boolean exitOnStop) throws Exception {
        launchTestComplete(getToolProjectSuitePath().getName().replace(".pjs", ""), silentMode, exitOnStop);
    }

    /**
	 * Launch TestComplete with the requested project, if not already running.
	 * 
	 * @throws Exception
	 *             If TestCenter is already running but with different project or if launch failed.
	 */
    public void launchTestComplete(String projectName, boolean silentMode, boolean exitOnStop) throws Exception {
        this.exitOnStop = exitOnStop;
        if (isProcessRunning()) {
            restartTestComplete(projectName, silentMode, exitOnStop);
        } else {
            startTestComplete(projectName, silentMode, exitOnStop);
        }
    }

    private boolean isProcessRunning() throws Exception {
        processManager = new ProcessMonitor();
        processManager.init();
        processManager.wmic.setHost(getHost());
        processManager.wmic.setUser(getUser());
        processManager.wmic.setPassword(getPassword());
        return processManager.wmic.isProcessRunning(getToolPath().getName());
    }

    private void startTestComplete(String projectName, boolean silentMode, boolean exitOnStop) throws Exception {
        if (isLocalHost()) {
            Command command = new Command();
            command.setCmd(new String[] { getToolPath().getAbsolutePath(), getToolProjectSuitePath().getAbsolutePath(), "/r", "/ns", silentMode ? "/SilentMode" : "", "/p:" + projectName, exitOnStop ? "/exit" : "" });
            report(getTitle("Launch TestComplete, load project \"" + projectName + "\" and run"), command.getCommandAsString(), true);
            Execute.execute(command, false);
        } else {
            String params = getToolProjectSuitePath().getAbsolutePath() + " /r /ns" + (silentMode ? " /SilentMode" : "") + " /p:" + projectName + (exitOnStop ? " /exit" : "");
            RemoteMachine remote = new RemoteMachine(getHost(), getUser(), getPassword());
            remote.init();
            process = new PSExecRemoteProcess("TestComplete", remote, getToolPath(), "c:\\Program Files\\PsTools\\PsExec.exe");
            process.setParameters(params);
            process.setSessionID(1);
            process.launchProcess();
        }
        if (getTestCompleteStatus() != QtpTcStatus.Pass) {
            throw new Exception("Failed to launch TestComplete");
        }
        report(getTitle("TestComplete is up and running."));
    }

    public void restartTestComplete(String projectName, boolean silentMode, boolean exitOnStop) throws Exception {
        activateTestComplete();
        if (getTestCompleteStatus() != QtpTcStatus.Pass) {
            throw new Exception("TestComplete already running but project cannot be retrieved");
        }
        String curProject = getCurrentResult().getParamList().get(0).getValue();
        if (!curProject.equalsIgnoreCase(projectName)) {
            throw new Exception("TestComplete already running with different project - " + curProject);
        }
        report(getTitle("TestComplete already running"));
    }

    public void closeTestComplete() throws Exception {
        processManager = new ProcessMonitor();
        processManager.init();
        processManager.wmic.setHost(getHost());
        processManager.wmic.setUser(getUser());
        processManager.wmic.setPassword(getPassword());
        processManager.wmic.killProcess(getToolPath().getName());
        if (!waitForTcToExit()) {
            throw new Exception(getTitle("TestComplete thread is still running even after kill"));
        }
    }

    public void stopProject() throws Exception {
        report(getTitle("Stopping TestComplete project..."));
        ExecutionCmd cmd = new ExecutionCmd("stopProject");
        cmd.setDescription("Stop TestComplete project");
        cmd.setTimeOut(30);
        executeCmd(cmd);
        if (exitOnStop) {
            if (!waitForTcToExit()) {
                closeTestComplete();
            }
        }
    }

    private boolean waitForTcToExit() throws Exception {
        report(getTitle("Wait for TestComplete to exit..."));
        int TIMEOUT = 60;
        int timeout = TIMEOUT;
        while (timeout > 0) {
            if (!isProcessRunning()) {
                report(getTitle("TestComplete is down after " + (TIMEOUT - timeout)));
                return true;
            }
            Thread.sleep(1000);
            timeout--;
        }
        report(getTitle("TestComplete thread is still running after " + TIMEOUT + " seconds"));
        return false;
    }

    private void activateTestComplete() throws Exception {
        if (isLocalHost()) {
            report(getTitle("Activating TestComplete..."));
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_SHIFT);
            robot.keyPress(KeyEvent.VK_F3);
            robot.keyRelease(KeyEvent.VK_F3);
            robot.keyRelease(KeyEvent.VK_SHIFT);
        }
    }

    private void deactivateTestComplete() throws Exception {
        if (isLocalHost()) {
            report(getTitle("De-Activating TestComplete..."));
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_SHIFT);
            robot.keyPress(KeyEvent.VK_F2);
            robot.keyRelease(KeyEvent.VK_F2);
            robot.keyRelease(KeyEvent.VK_SHIFT);
        }
    }

    public QtpTcStatus getTestCompleteStatus() throws Exception {
        ExecutionCmd cmd = new ExecutionCmd("testCompleteStatus");
        cmd.setDescription("Get TestComplete status");
        cmd.setTimeOut(90);
        executeCmd(cmd);
        return currentResult.getStatus();
    }

    public void takeScreenshot(String title) throws Exception {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String time = sdf.format(cal.getTime());
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRectangle = new Rectangle(screenSize);
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(screenRectangle);
        File f = new File(report.getCurrentTestFolder(), "screenshot_" + time + ".png");
        f.createNewFile();
        ImageIO.write(image, "png", f);
        report.addLink(getTitle("Screenshot"), "file:///" + f.getAbsolutePath());
    }

    private void readExecutionResult() throws Exception {
        String resultXpath = "//result[@id='" + commandId + "']";
        boolean gotResultFile = false;
        long startTime = System.currentTimeMillis();
        int timeout = defaultTimeout * 1000;
        while (System.currentTimeMillis() - startTime < timeout) {
            Thread.sleep(sampleTime);
            try {
                resultDoc = FileUtils.readDocumentFromFile(resultFile);
                NodeList reportList = XPathAPI.selectNodeList(resultDoc, resultXpath + "/reports/report");
                if (reportList.getLength() > 0) {
                    gotResultFile = true;
                    break;
                }
            } catch (Exception se) {
            }
        }
        if (gotResultFile) {
            currentResult.setId(commandId);
            NodeList reportList = XPathAPI.selectNodeList(resultDoc, resultXpath + "/reports/report");
            for (int i = 0; i < reportList.getLength(); i++) {
                Node n = reportList.item(i);
                if (n instanceof Element) {
                    String id = n.getAttributes().getNamedItem("id").getTextContent();
                    Node text = XPathAPI.selectSingleNode(resultDoc, resultXpath + "/reports/report[@id='" + id + "']/text");
                    Node status = XPathAPI.selectSingleNode(resultDoc, resultXpath + "/reports/report[@id='" + id + "']/status");
                    QtpTcReport r = new QtpTcReport(Integer.parseInt(id), text.getTextContent(), QtpTcStatus.valueOfIgnoreCase(status.getTextContent()));
                    currentResult.addResultReport(r);
                }
            }
            NodeList paramList = XPathAPI.selectNodeList(resultDoc, resultXpath + "/parameters/parameter");
            for (int i = 0; i < paramList.getLength(); i++) {
                Node n = paramList.item(i);
                if (n instanceof Element) {
                    String id = n.getAttributes().getNamedItem("id").getTextContent();
                    Node name = XPathAPI.selectSingleNode(resultDoc, resultXpath + "/parameters/parameter[@id='" + id + "']/name");
                    Node description = XPathAPI.selectSingleNode(resultDoc, resultXpath + "/parameters/parameter[@id='" + id + "']/description");
                    Node type = XPathAPI.selectSingleNode(resultDoc, resultXpath + "/parameters/parameter[@id='" + id + "']/type");
                    Node value = XPathAPI.selectSingleNode(resultDoc, resultXpath + "/parameters/parameter[@id='" + id + "']/value");
                    QtpTcParameter p = new QtpTcParameter(Integer.parseInt(id), name.getTextContent(), description.getTextContent(), type.getTextContent(), value.getTextContent());
                    currentResult.addResultParameter(p);
                }
            }
            logger.append("\n");
            logger.append(XmlUtils.documentToString(resultDoc));
            logger.append("\n\n");
            clearInterface(commandId);
            currentResult.evaluateResult();
        } else {
            currentResult.setStatus(QtpTcStatus.Warning);
            report(getTitle("Result timeout expired, trying to recover"), Reporter.FAIL);
            takeScreenshot("Recovery screenshot");
            deactivateTestComplete();
            Thread.sleep(10000);
            activateTestComplete();
        }
    }

    public File getExecuteFile() {
        return executeFile;
    }

    /**
	 * Full path to execution interface file
	 */
    public void setExecuteFile(File executeFile) {
        this.executeFile = executeFile;
    }

    public File getResultFile() {
        return resultFile;
    }

    /**
	 * Full path to interface results file
	 */
    public void setResultFile(File resultFile) {
        this.resultFile = resultFile;
    }

    public File getSettingsFile() {
        return settingsFile;
    }

    /**
	 * Full path to interface settings file
	 */
    public void setSettingsFile(File settingsFile) {
        this.settingsFile = settingsFile;
    }

    public int getSampleTime() {
        return sampleTime;
    }

    /**
	 * Result sample timeout in ms.
	 * 
	 * @param sampleTime
	 */
    public void setSampleTime(int sampleTime) {
        this.sampleTime = sampleTime;
    }

    public ExecutionResult getCurrentResult() {
        return currentResult;
    }

    @IgnoreMethod
    public void setCurrentResult(ExecutionResult currentResult) {
        this.currentResult = currentResult;
    }

    public boolean isDebug() {
        return debug;
    }

    /**
	 * Is debug mode on ?
	 */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public int getDefaultTimeout() {
        return defaultTimeout;
    }

    /**
	 * Default communication error timeout (sec)
	 */
    public void setDefaultTimeout(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public File getToolPath() {
        return toolPath;
    }

    public void setToolPath(File toolPath) {
        this.toolPath = toolPath;
    }

    public File getToolProjectSuitePath() {
        return toolProjectSuitePath;
    }

    public void setToolProjectSuitePath(File toolProjectSuitePath) {
        this.toolProjectSuitePath = toolProjectSuitePath;
    }

    public String getToolProjectName() {
        return toolProjectName;
    }

    public void setToolProjectName(String toolProjectName) {
        this.toolProjectName = toolProjectName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isClearInterfaceOnInit() {
        return clearInterfaceOnInit;
    }

    public void setClearInterfaceOnInit(boolean clearInterfaceOnInit) {
        this.clearInterfaceOnInit = clearInterfaceOnInit;
    }

    private boolean isLocalHost() {
        return (getHost().equals("127.0.0.1") || getHost().equals("localhost"));
    }

    protected String getTitle(String title) {
        if (!isLocalHost()) {
            title = getHost() + ": " + title;
        }
        return title;
    }
}
