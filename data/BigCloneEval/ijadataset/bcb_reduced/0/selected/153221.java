package com.izforge.izpack.installer;

import com.izforge.izpack.CustomData;
import com.izforge.izpack.ExecutableFile;
import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Panel;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.IXMLParser;
import com.izforge.izpack.adaptator.impl.XMLParser;
import com.izforge.izpack.installer.DataValidator.Status;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.OsConstraint;
import com.izforge.izpack.util.VariableSubstitutor;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

/**
 * Runs the install process in text only (no GUI) mode.
 * 
 * @author Jonathan Halliday <jonathan.halliday@arjuna.com>
 * @author Julien Ponge <julien@izforge.com>
 * @author Johannes Lehtinen <johannes.lehtinen@iki.fi>
 */
public class AutomatedInstaller extends InstallerBase {

    private TreeMap<String, Integer> panelInstanceCount;

    /**
     * The automated installation data.
     */
    private AutomatedInstallData idata = new AutomatedInstallData();

    /**
     * The result of the installation.
     */
    private boolean result = false;

    /**
     * Constructing an instance triggers the install.
     *
     * @param inputFilename Name of the file containing the installation data.
     * @throws Exception Description of the Exception
     */
    public AutomatedInstaller(String inputFilename) throws Exception {
        super();
        File input = new File(inputFilename);
        loadInstallData(this.idata);
        this.idata.xmlData = getXMLData(input);
        this.idata.localeISO3 = this.idata.xmlData.getAttribute("langpack", "eng");
        InputStream in = getClass().getResourceAsStream("/langpacks/" + this.idata.localeISO3 + ".xml");
        this.idata.langpack = new LocaleDatabase(in);
        this.idata.setVariable(ScriptParser.ISO3_LANG, this.idata.localeISO3);
        ResourceManager.create(this.idata);
        addCustomLangpack(this.idata);
        this.panelInstanceCount = new TreeMap<String, Integer>();
        loadConditions(this.idata);
        loadInstallerRequirements();
        loadDynamicVariables();
    }

    /**
     * Writes the uninstalldata. <p/> Unfortunately, Java doesn't allow multiple inheritance, so
     * <code>AutomatedInstaller</code> and <code>InstallerFrame</code> can't share this code ...
     * :-/ <p/> TODO: We should try to fix this in the future.
     */
    private boolean writeUninstallData() {
        try {
            UninstallData udata = UninstallData.getInstance();
            List files = udata.getUninstalableFilesList();
            ZipOutputStream outJar = this.idata.uninstallOutJar;
            if (outJar == null) {
                return true;
            }
            System.out.println("[ Writing the uninstaller data ... ]");
            outJar.putNextEntry(new ZipEntry("install.log"));
            BufferedWriter logWriter = new BufferedWriter(new OutputStreamWriter(outJar));
            logWriter.write(this.idata.getInstallPath());
            logWriter.newLine();
            Iterator iter = files.iterator();
            while (iter.hasNext()) {
                logWriter.write((String) iter.next());
                if (iter.hasNext()) {
                    logWriter.newLine();
                }
            }
            logWriter.flush();
            outJar.closeEntry();
            outJar.putNextEntry(new ZipEntry("jarlocation.log"));
            logWriter = new BufferedWriter(new OutputStreamWriter(outJar));
            logWriter.write(udata.getUninstallerJarFilename());
            logWriter.newLine();
            logWriter.write(udata.getUninstallerPath());
            logWriter.flush();
            outJar.closeEntry();
            outJar.putNextEntry(new ZipEntry("executables"));
            ObjectOutputStream execStream = new ObjectOutputStream(outJar);
            iter = udata.getExecutablesList().iterator();
            execStream.writeInt(udata.getExecutablesList().size());
            while (iter.hasNext()) {
                ExecutableFile file = (ExecutableFile) iter.next();
                execStream.writeObject(file);
            }
            execStream.flush();
            outJar.closeEntry();
            Map<String, Object> additionalData = udata.getAdditionalData();
            if (additionalData != null && !additionalData.isEmpty()) {
                Iterator<String> keys = additionalData.keySet().iterator();
                HashSet<String> exist = new HashSet<String>();
                while (keys != null && keys.hasNext()) {
                    String key = keys.next();
                    Object contents = additionalData.get(key);
                    if ("__uninstallLibs__".equals(key)) {
                        Iterator nativeLibIter = ((List) contents).iterator();
                        while (nativeLibIter != null && nativeLibIter.hasNext()) {
                            String nativeLibName = (String) ((List) nativeLibIter.next()).get(0);
                            byte[] buffer = new byte[5120];
                            long bytesCopied = 0;
                            int bytesInBuffer;
                            outJar.putNextEntry(new ZipEntry("native/" + nativeLibName));
                            InputStream in = getClass().getResourceAsStream("/native/" + nativeLibName);
                            while ((bytesInBuffer = in.read(buffer)) != -1) {
                                outJar.write(buffer, 0, bytesInBuffer);
                                bytesCopied += bytesInBuffer;
                            }
                            outJar.closeEntry();
                        }
                    } else if ("uninstallerListeners".equals(key) || "uninstallerJars".equals(key)) {
                        ArrayList<String> subContents = new ArrayList<String>();
                        Iterator listenerIter = ((List) contents).iterator();
                        while (listenerIter.hasNext()) {
                            byte[] buffer = new byte[5120];
                            long bytesCopied = 0;
                            int bytesInBuffer;
                            CustomData customData = (CustomData) listenerIter.next();
                            if (customData.listenerName != null) {
                                subContents.add(customData.listenerName);
                            }
                            Iterator<String> liClaIter = customData.contents.iterator();
                            while (liClaIter.hasNext()) {
                                String contentPath = liClaIter.next();
                                if (exist.contains(contentPath)) {
                                    continue;
                                }
                                exist.add(contentPath);
                                try {
                                    outJar.putNextEntry(new ZipEntry(contentPath));
                                } catch (ZipException ze) {
                                    Debug.trace("ZipException in writing custom data: " + ze.getMessage());
                                    continue;
                                }
                                InputStream in = getClass().getResourceAsStream("/" + contentPath);
                                if (in != null) {
                                    while ((bytesInBuffer = in.read(buffer)) != -1) {
                                        outJar.write(buffer, 0, bytesInBuffer);
                                        bytesCopied += bytesInBuffer;
                                    }
                                } else {
                                    Debug.trace("custom data not found: " + contentPath);
                                }
                                outJar.closeEntry();
                            }
                        }
                        outJar.putNextEntry(new ZipEntry(key));
                        ObjectOutputStream objOut = new ObjectOutputStream(outJar);
                        objOut.writeObject(subContents);
                        objOut.flush();
                        outJar.closeEntry();
                    } else {
                        outJar.putNextEntry(new ZipEntry(key));
                        if (contents instanceof ByteArrayOutputStream) {
                            ((ByteArrayOutputStream) contents).writeTo(outJar);
                        } else {
                            ObjectOutputStream objOut = new ObjectOutputStream(outJar);
                            objOut.writeObject(contents);
                            objOut.flush();
                        }
                        outJar.closeEntry();
                    }
                }
            }
            ArrayList<String> unInstallScripts = udata.getUninstallScripts();
            Iterator<String> unInstallIter = unInstallScripts.iterator();
            ObjectOutputStream rootStream;
            int idx = 0;
            while (unInstallIter.hasNext()) {
                outJar.putNextEntry(new ZipEntry(UninstallData.ROOTSCRIPT + Integer.toString(idx)));
                rootStream = new ObjectOutputStream(outJar);
                String unInstallScript = (String) unInstallIter.next();
                rootStream.writeUTF(unInstallScript);
                rootStream.flush();
                outJar.closeEntry();
            }
            outJar.flush();
            outJar.close();
            return true;
        } catch (Exception err) {
            err.printStackTrace();
            return false;
        }
    }

    /**
     * Runs the automated installation logic for each panel in turn.
     *
     * @throws Exception
     */
    protected void doInstall() throws Exception {
        if (!checkInstallerRequirements(this.idata)) {
            Debug.log("not all installerconditions are fulfilled.");
            System.exit(-1);
            return;
        }
        System.out.println("[ Starting automated installation ]");
        Debug.log("[ Starting automated installation ]");
        try {
            this.result = true;
            VariableSubstitutor substitutor = new VariableSubstitutor(this.idata.getVariables());
            for (Panel p : this.idata.panelsOrder) {
                if (p.hasCondition() && !this.idata.getRules().isConditionTrue(p.getCondition(), this.idata.variables)) {
                    Debug.log("Condition for panel " + p.getPanelid() + "is not fulfilled, skipping panel!");
                    if (this.panelInstanceCount.containsKey(p.className)) {
                        this.panelInstanceCount.put(p.className, this.panelInstanceCount.get(p.className) + 1);
                    } else {
                        this.panelInstanceCount.put(p.className, 1);
                    }
                    continue;
                }
                if (!OsConstraint.oneMatchesCurrentSystem(p.osConstraints)) {
                    continue;
                }
                PanelAutomation automationHelper = getPanelAutomationHelper(p);
                if (automationHelper == null) {
                    executePreValidateActions(p, null);
                    validatePanel(p);
                    executePostValidateActions(p, null);
                    continue;
                }
                IXMLElement panelRoot = updateInstanceCount(p);
                installPanel(p, automationHelper, panelRoot);
                refreshDynamicVariables(substitutor, this.idata);
            }
            writeUninstallData();
            if (this.result) {
                System.out.println("[ Automated installation done ]");
            } else {
                System.out.println("[ Automated installation FAILED! ]");
            }
        } catch (Exception e) {
            this.result = false;
            System.err.println(e.toString());
            e.printStackTrace();
            System.out.println("[ Automated installation FAILED! ]");
        } finally {
            Housekeeper.getInstance().shutDown(this.result ? 0 : 1);
        }
    }

    /**
     * Run the installation logic for a panel.
     * @param p                   The panel to install.
     * @param automationHelper    The helper of the panel.
     * @param panelRoot           The xml element describing the panel.
     * @throws InstallerException if something went wrong while installing.
     */
    private void installPanel(Panel p, PanelAutomation automationHelper, IXMLElement panelRoot) throws InstallerException {
        executePreActivateActions(p, null);
        Debug.log("automationHelperInstance.runAutomated :" + automationHelper.getClass().getName() + " entered.");
        automationHelper.runAutomated(this.idata, panelRoot);
        Debug.log("automationHelperInstance.runAutomated :" + automationHelper.getClass().getName() + " successfully done.");
        executePreValidateActions(p, null);
        validatePanel(p);
        executePostValidateActions(p, null);
    }

    /**
     * Update the panelInstanceCount object with a panel.
     * @see this.panelInstanceCount
     * @param p The panel.
     * @return The xml element which describe the panel.
     */
    private IXMLElement updateInstanceCount(Panel p) {
        String panelClassName = p.className;
        Vector<IXMLElement> panelRoots = this.idata.xmlData.getChildrenNamed(panelClassName);
        int panelRootNo = 0;
        if (this.panelInstanceCount.containsKey(panelClassName)) {
            panelRootNo = this.panelInstanceCount.get(panelClassName);
        }
        IXMLElement panelRoot = panelRoots.elementAt(panelRootNo);
        this.panelInstanceCount.put(panelClassName, panelRootNo + 1);
        return panelRoot;
    }

    /**
     * Try to get the automation helper for the specified panel.
     * @param p The panel to handle.
     * @return The automation helper if possible, null otherwise.
     */
    private PanelAutomation getPanelAutomationHelper(Panel p) {
        Class<PanelAutomation> automationHelperClass = null;
        PanelAutomation automationHelperInstance = null;
        String praefix = "com.izforge.izpack.panels.";
        if (p.className.compareTo(".") > -1) {
            praefix = "";
        }
        String automationHelperClassName = praefix + p.className + "AutomationHelper";
        try {
            Debug.log("AutomationHelper:" + automationHelperClassName);
            automationHelperClass = (Class<PanelAutomation>) Class.forName(automationHelperClassName);
        } catch (ClassNotFoundException e) {
            Debug.log("ClassNotFoundException-skip :" + automationHelperClassName);
        }
        executePreConstructActions(p, null);
        if (automationHelperClass != null) {
            try {
                Debug.log("Instantiate :" + automationHelperClassName);
                automationHelperInstance = automationHelperClass.newInstance();
            } catch (IllegalAccessException e) {
                Debug.log("ERROR: no default constructor for " + automationHelperClassName + ", skipping...");
            } catch (InstantiationException e) {
                Debug.log("ERROR: no default constructor for " + automationHelperClassName + ", skipping...");
            }
        }
        return automationHelperInstance;
    }

    /**
     * Validate a panel.
     *
     * @param p The panel to validate
     * @throws InstallerException thrown if the validation fails.
     */
    private void validatePanel(final Panel p) throws InstallerException {
        String dataValidator = p.getValidator();
        if (dataValidator != null) {
            DataValidator validator = DataValidatorFactory.createDataValidator(dataValidator);
            Status validationResult = validator.validateData(idata);
            if (validationResult != DataValidator.Status.OK) {
                if (validationResult == Status.WARNING && validator.getDefaultAnswer()) {
                    System.out.println("Configuration said, it's ok to go on, if validation is not successfull");
                    return;
                }
                this.result = false;
                throw new InstallerException("Validating data for panel " + p.getPanelid() + " was not successfull");
            }
        }
    }

    /**
     * Loads the xml data for the automated mode.
     *
     * @param input The file containing the installation data.
     * @return The root of the XML file.
     * @throws IOException thrown if there are problems reading the file.
     */
    public IXMLElement getXMLData(File input) throws IOException {
        FileInputStream in = new FileInputStream(input);
        IXMLParser parser = new XMLParser();
        IXMLElement rtn = parser.parse(in, input.getAbsolutePath());
        in.close();
        return rtn;
    }

    /**
     * Get the result of the installation.
     *
     * @return True if the installation was successful.
     */
    public boolean getResult() {
        return this.result;
    }

    private List<PanelAction> createPanelActionsFromStringList(Panel panel, List<String> actions) {
        List<PanelAction> actionList = null;
        if (actions != null) {
            actionList = new ArrayList<PanelAction>();
            for (String actionClassName : actions) {
                PanelAction action = PanelActionFactory.createPanelAction(actionClassName);
                action.initialize(panel.getPanelActionConfiguration(actionClassName));
                actionList.add(action);
            }
        }
        return actionList;
    }

    private void executePreConstructActions(Panel panel, AbstractUIHandler handler) {
        List<PanelAction> preConstructActions = createPanelActionsFromStringList(panel, panel.getPreConstructionActions());
        if (preConstructActions != null) {
            for (PanelAction preConstructAction : preConstructActions) {
                preConstructAction.executeAction(idata, handler);
            }
        }
    }

    private void executePreActivateActions(Panel panel, AbstractUIHandler handler) {
        List<PanelAction> preActivateActions = createPanelActionsFromStringList(panel, panel.getPreActivationActions());
        if (preActivateActions != null) {
            for (PanelAction preActivateAction : preActivateActions) {
                preActivateAction.executeAction(idata, handler);
            }
        }
    }

    private void executePreValidateActions(Panel panel, AbstractUIHandler handler) {
        List<PanelAction> preValidateActions = createPanelActionsFromStringList(panel, panel.getPreValidationActions());
        if (preValidateActions != null) {
            for (PanelAction preValidateAction : preValidateActions) {
                preValidateAction.executeAction(idata, handler);
            }
        }
    }

    private void executePostValidateActions(Panel panel, AbstractUIHandler handler) {
        List<PanelAction> postValidateActions = createPanelActionsFromStringList(panel, panel.getPostValidationActions());
        if (postValidateActions != null) {
            for (PanelAction postValidateAction : postValidateActions) {
                postValidateAction.executeAction(idata, handler);
            }
        }
    }
}
