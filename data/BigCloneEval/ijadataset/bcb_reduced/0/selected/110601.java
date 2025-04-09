package com.foursoft.fourever.variants.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.commons.logging.Log;
import org.springframework.beans.BeansException;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.foursoft.component.Config;
import com.foursoft.component.MessageId;
import com.foursoft.component.exception.ComponentInternalException;
import com.foursoft.fourever.consistency.ContentCorrector;
import com.foursoft.fourever.export.ExportObserver;
import com.foursoft.fourever.objectmodel.EntityInstance;
import com.foursoft.fourever.objectmodel.Instance;
import com.foursoft.fourever.objectmodel.Link;
import com.foursoft.fourever.objectmodel.ObjectModel;
import com.foursoft.fourever.objectmodel.StringInstance;
import com.foursoft.fourever.objectmodel.exception.TargetIndexOutOfBoundsException;
import com.foursoft.fourever.objectmodel.exception.TypeMismatchException;
import com.foursoft.fourever.variants.VariantManager;
import com.foursoft.fourever.variants.Variante;
import com.foursoft.fourever.variants.merging.MergeManager;
import com.foursoft.fourever.variants.modification.ModificationManager;
import com.foursoft.fourever.variants.tailoring.TailorManager;
import com.foursoft.fourever.variants.view.impl.VariantExportDialog;
import com.foursoft.fourever.variants.view.impl.VariantGuiRunner;
import com.foursoft.fourever.xmlfileio.Document;
import com.foursoft.fourever.xmlfileio.XMLFileIOManager;
import com.foursoft.fourever.xmlfileio.exception.DocumentLockedException;
import com.foursoft.fourever.xmlfileio.exception.MissingLocationException;
import com.foursoft.fourever.xmlfileio.exception.SchemaProcessingException;
import com.foursoft.fourever.xmlfileio.exception.XMLProcessingException;

public class VariantManagerImpl implements VariantManager {

    /** singleton component instance */
    private static VariantManager instance = null;

    private static TailorManager tailorManager = null;

    private static MergeManager mergeManager = null;

    private static ModificationManager modificationManager = null;

    private static XMLFileIOManager xmlManager = null;

    private static VariantConfigImpl config = new VariantConfigImpl();

    private static ContentCorrector contentCorrector = null;

    /** log for all component implementation classes */
    public static Log log = null;

    private static MessageSource messageSource = null;

    private List<Variante> varianten;

    private Variante chosenVariante;

    private static ClassPathXmlApplicationContext appContext = null;

    private static File resultDir;

    private static String resultDirString;

    /** Maps instances from the ObjectModel to their proxies */
    public static Map<EntityInstance, Variante> instanceToWrapperMap = null;

    private static VariantGuiRunner guiCreator = null;

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        assert (config instanceof VariantConfigImpl);
        VariantManagerImpl.config = (VariantConfigImpl) config;
    }

    private VariantManagerImpl() {
        this.varianten = new ArrayList<Variante>();
    }

    /**
	 * Create the component manager, thus initializing the component.
	 * 
	 * @param myLog
	 *            the Log to use
	 * @return the singleton instance of the VariantManager
	 */
    public static VariantManager createInstance(XMLFileIOManager xmlManager, TailorManager tailorManager, MergeManager mergeManager, ModificationManager modificationManager, ContentCorrector contentCorrector, Log myLog, MessageSource messageSource) {
        if (instance != null) {
            throw new ComponentInternalException("Could not initialize the component manager - already initialized");
        }
        instance = new VariantManagerImpl();
        VariantManagerImpl.xmlManager = xmlManager;
        VariantManagerImpl.tailorManager = tailorManager;
        VariantManagerImpl.mergeManager = mergeManager;
        VariantManagerImpl.modificationManager = modificationManager;
        VariantManagerImpl.contentCorrector = contentCorrector;
        if (myLog == null) {
            throw new ComponentInternalException("Could not initialize the log");
        }
        log = myLog;
        instanceToWrapperMap = new HashMap<EntityInstance, Variante>();
        guiCreator = new VariantGuiRunner();
        VariantManagerImpl.messageSource = messageSource;
        return instance;
    }

    public static XMLFileIOManager getXMLManager() {
        return xmlManager;
    }

    public void destroy() {
        log.debug("Destroying the VariantManager.");
    }

    public static void main(String[] args) {
        if (args.length != 0 && (args.length != 6 || !args[0].equals("-c") || !args[2].equals("-v") || !args[4].equals("-o"))) {
            System.err.println(Messages.getString("VariantManagerImpl.ProgramUsageLine1"));
            System.err.println(Messages.getString("VariantManagerImpl.ProgramUsageLine2"));
            System.exit(1);
        }
        String configLoc = null;
        String variantName = null;
        String outputLoc = null;
        String rootDir = null;
        boolean noGuiUsage = false;
        if (args.length != 0) {
            configLoc = args[1];
            variantName = args[3];
            outputLoc = addDirDelimiterIfNecessary(args[5]);
            File config = new File(configLoc);
            if (!config.exists() || config.isDirectory() || !config.canRead()) {
                System.err.println(Messages.getString("VariantManagerImpl.NoFileAccess") + configLoc + Messages.getString("VariantManagerImpl.Aborting"));
                System.exit(1);
            }
            rootDir = addDirDelimiterIfNecessary(config.getParent());
            File outDir = new File(outputLoc);
            if (!outDir.exists()) {
                if (!outDir.mkdir()) {
                    System.err.println(Messages.getString("VariantManagerImpl.NoCreateDirectory") + outDir);
                    System.exit(1);
                }
            } else {
                if (!outDir.isDirectory()) {
                    System.err.println(outDir + Messages.getString("VariantManagerImpl.FileExistsButIsNotADirectory"));
                    System.exit(1);
                }
            }
            noGuiUsage = true;
        }
        try {
            appContext = new ClassPathXmlApplicationContext("variants.xml");
            guiCreator.setMessageSource(messageSource);
        } catch (BeansException ex) {
            System.err.println("Could not initialize 4Ever framework - aborting.");
            System.err.println(ex);
            System.exit(1);
        }
        VariantManager vManager = (VariantManager) appContext.getBean("variantmanager");
        boolean canceled = false;
        try {
            if (!noGuiUsage) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    log.warn("Could not set System Look and Feel", e);
                }
                SwingUtilities.invokeLater(guiCreator);
                VariantExportDialog variantChooserDialog = new VariantExportDialog(null, vManager);
                variantChooserDialog.setLocationRelativeTo(null);
                variantChooserDialog.setVisible(true);
                if (!variantChooserDialog.wasCancelButtonPressed()) {
                    guiCreator.setProgressInfo(0, Messages.getString("VariantManagerImpl.VariantSetupChosen"));
                    configLoc = variantChooserDialog.getConfig();
                    variantName = variantChooserDialog.getVariantName();
                    outputLoc = variantChooserDialog.getOutputLocation();
                    guiCreator.setExportDir(outputLoc);
                    guiCreator.setVariant(variantName);
                    File config = new File(configLoc);
                    if (!config.exists() || config.isDirectory() || !config.canRead()) {
                        System.err.println(Messages.getString("VariantManagerImpl.NoFileAccess") + configLoc + Messages.getString("VariantManagerImpl.Aborting"));
                        System.exit(1);
                    }
                    rootDir = addDirDelimiterIfNecessary(config.getParent());
                } else {
                    guiCreator.setProgressInfo(0, Messages.getString("VariantManagerImpl.Canceled"));
                    guiCreator.setDeterminate(100);
                    guiCreator.setProgress(100);
                    guiCreator.setCanceled();
                    canceled = true;
                }
            }
            if (!canceled) {
                vManager.performMerge(rootDir, configLoc, variantName, outputLoc, guiCreator);
            }
        } catch (VariantException e) {
            log.fatal(e);
            e.printStackTrace();
            log.info(Messages.getString("VariantManagerImpl.CleaningTemporaryDirectories"));
            if (!noGuiUsage) JOptionPane.showMessageDialog(null, e.getMessage(), Messages.getString("VariantManagerImpl.Error"), JOptionPane.ERROR_MESSAGE);
            guiCreator.setProgressInfo(90, Messages.getString("VariantManagerImpl.CleaningTemporaryDirectories"));
            try {
                rmdir(resultDir);
            } catch (IOException e2) {
                log.error(Messages.getString("VariantManagerImpl.NotRemoveTemporaryDirectory"));
            }
        } catch (CleanupException e) {
            log.error(Messages.getString("VariantManagerImpl.NotRemoveTemporaryDirectory"));
        }
        appContext.close();
        if (!canceled && !noGuiUsage) guiCreator.setFinished(true);
    }

    public void performMerge(String rootDir, String configLoc, String variantName, String outputLoc) throws VariantException, CleanupException {
        performMerge(rootDir, configLoc, variantName, outputLoc, null);
    }

    public void performMerge(String rootDir, String configLoc, String variantName, String outputLoc, ExportObserver observer) throws VariantException, CleanupException {
        resultDirString = addDirDelimiterIfNecessary(System.getProperty("java.io.tmpdir")).concat("modelmergingresult" + new Random(new Date().getTime()).nextInt(99999));
        resultDir = new File(resultDirString);
        resultDir.mkdir();
        resultDir.deleteOnExit();
        resultDirString = addDirDelimiterIfNecessary(resultDirString);
        if (observer != null) observer.setProgressNote(10, new MessageId("VariantManagerImpl.PreparingMerge"));
        this.loadXMLConfiguration(new File(configLoc));
        chosenVariante = this.getVariante(variantName);
        log.info("Building Variante " + variantName);
        if (observer != null) observer.setProgressNote(20, new MessageId("VariantManagerImpl.MergingDirectories"));
        this.prepareResultDirectory(chosenVariante, rootDir, observer);
        if (observer != null) observer.setProgressNote(50, new MessageId("VariantManagerImpl.PerformMergeSteps"));
        this.performVariantenMerge(chosenVariante, rootDir, observer);
        log.info(Messages.getString("VariantManagerImpl.CopyResultToOutput"));
        if (observer != null) observer.setProgressNote(70, new MessageId("VariantManagerImpl.CopyResultToOutput"));
        File outputDirFile = new File(outputLoc);
        try {
            copyDirectory(resultDir, outputDirFile);
        } catch (IOException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.NoCopyToOutput"), e);
        }
        if (observer != null) observer.setProgressNote(90, new MessageId("VariantManagerImpl.CleaningTemporaryDirectories"));
        cleanUp();
        if (observer != null) observer.setProgressNote(100, new MessageId("VariantManagerImpl.Finished"));
        if (observer != null) observer.setProgress(1);
    }

    public void cleanUp() throws CleanupException {
        log.info(Messages.getString("VariantManagerImpl.CleaningTemporaryDirectories"));
        try {
            if (resultDir != null) rmdir(resultDir);
        } catch (IOException e) {
            throw new CleanupException(Messages.getString("VariantManagerImpl.NotRemoveTemporaryDirectory"), e);
        }
    }

    private static String addDirDelimiterIfNecessary(String string) {
        if (string.charAt(string.length() - 1) != File.separatorChar) {
            string = string + File.separatorChar;
        }
        return string;
    }

    private void addVariante(Variante varKonf) {
        this.varianten.add(varKonf);
    }

    private Variante getVariante(String name) throws VariantException {
        Variante retVal = null;
        for (Variante vk : this.varianten) {
            if (vk.getName().equals(name)) retVal = vk;
        }
        if (retVal == null) {
            throw new VariantException(Messages.getString("VariantManagerImpl.NoVarianteInConfig") + name, new Exception());
        }
        return retVal;
    }

    protected void prepareResultDirectory(Variante vk, String root) throws VariantException {
        prepareResultDirectory(vk, root, null);
    }

    /**
	 * Prepares a result directory copying alle model files into it.
	 * 
	 * @param vk The Variante to be used as Main model.
	 * @param root The root directory to search the models in.
	 * 
	 * @throws VariantException Thrown if creation of the temporary directory was not successful.
	 */
    private void prepareResultDirectory(Variante vk, String root, ExportObserver observer) throws VariantException {
        try {
            if (vk.hasReferenzvariante()) {
                prepareResultDirectory(vk.getReferenzvariante(), root, observer);
                log.info(Messages.getString("VariantManagerImpl.MergingDirectories") + vk.getName() + ".");
            } else {
                log.info(Messages.getString("VariantManagerImpl.CreatingTempDirs"));
            }
            File sourceDir = new File(addDirDelimiterIfNecessary(root + vk.getDirectory()));
            if (!sourceDir.exists() || !sourceDir.isDirectory()) throw new VariantException("Der in der Konfiguration angegebene Pfad ist kein Verzeichnis: " + sourceDir.getAbsolutePath(), new Exception());
            copyDirectory(sourceDir, resultDir);
            File resultVarianteFile = new File(resultDirString + vk.getFileName());
            resultVarianteFile.delete();
            File resultVarianteLockFile = new File(resultDirString + vk.getFileName() + ".lock");
            resultVarianteLockFile.delete();
        } catch (IOException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.NoCopyToTemp"), e);
        }
    }

    protected void performVariantenMerge(Variante vk, String root) throws VariantException {
        performVariantenMerge(vk, root, null);
    }

    private void performVariantenMerge(Variante vk, String root, ExportObserver observer) throws VariantException {
        log.info(Messages.getString("VariantManagerImpl.PerformMergeSteps") + vk.getName() + ".");
        File erweiterungsmodell = new File(addDirDelimiterIfNecessary(root + vk.getDirectory()) + vk.getFileName());
        File mergedFile = new File(resultDirString + "V-Modell-XT.xml");
        this.performAllActionSteps(erweiterungsmodell, mergedFile, observer);
        repairInternalReferences(mergedFile);
    }

    /**
	 * Opens a file with the XMLFileIOManager, saves it, then closes it.
	 * This will update all changed internal links
	 * 
	 * @param mergedFile The file to be treated
	 * @throws VariantException If any error occurs during file management.
	 */
    private void repairInternalReferences(File mergedFile) throws VariantException {
        Document document = null;
        if (contentCorrector == null) return;
        try {
            document = xmlManager.openDocument(mergedFile, false, false);
            contentCorrector.correctContentForWritableFragments(document);
            document.getRootFragment().save();
            xmlManager.closeDocument(document);
        } catch (SchemaProcessingException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.CouldNotReadFile") + mergedFile.getPath(), e);
        } catch (IOException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.CouldNotReadFile") + mergedFile.getPath(), e);
        } catch (XMLProcessingException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.CouldNotReadFile") + mergedFile.getPath(), e);
        } catch (DocumentLockedException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.CouldNotReadFile") + mergedFile.getPath() + Messages.getString("VariantManagerImpl.BecauseOfLocking") + Messages.getString("VariantManagerImpl.PleaseCloseOpenFiles"), e);
        } catch (MissingLocationException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.CouldNotWriteXMLFile") + mergedFile.getPath(), e);
        }
    }

    protected void performAllActionSteps(File sourceFile, File targetFile) throws VariantException {
        performAllActionSteps(sourceFile, targetFile, null);
    }

    /**
	 * Performs the following model manipulation steps:<br/><br/>
	 * 1. Model merge
	 * 2. Modification operations
	 * 3. Vortailoring
	 * 
	 * ad 2.: Modification makes use of a file located in "config/variantmerge/modificationConfig.xml"
	 *        in the directory of the extension model. If there is no such file, the
	 *        {@link VariantConfigImpl} will provide a default configuration provided with
	 *        the VMXT editor installation.
	 * 
	 * @param sourceFile The file to be read in.
	 * @param targetFile The file to be written after model manipulation
	 * @throws VariantException
	 */
    private void performAllActionSteps(File sourceFile, File targetFile, ExportObserver observer) throws VariantException {
        Document document = null;
        try {
            document = xmlManager.openDocument(sourceFile, false, false);
            log.info(Messages.getString("VariantManagerImpl.StatusMerge"));
            if (observer != null) observer.setProgressNote(55, new MessageId("VariantManagerImpl.StatusMerge"));
            mergeManager.doMergeModels(document);
            log.info(Messages.getString("VariantManagerImpl.StatusModification"));
            if (observer != null) observer.setProgressNote(60, new MessageId("VariantManagerImpl.StatusModification"));
            modificationManager.doModification(document, new File(addDirDelimiterIfNecessary(sourceFile.getParent()) + "config/variantmerge/modificationConfig.xml"));
            log.info(Messages.getString("VariantManagerImpl.StatusVortailoring"));
            if (observer != null) observer.setProgressNote(65, new MessageId("VariantManagerImpl.StatusVortailoring"));
            tailorManager.doVortailoring(document);
            File tmpTarget = new File(targetFile.getPath() + "_");
            document.getRootFragment().setLocation(tmpTarget);
            document.getRootFragment().save();
            xmlManager.closeDocument(document);
            this.repairSchemaReference(tmpTarget, targetFile);
            tmpTarget.delete();
            File mustertextXML = new File(targetFile.getParent() + File.separator + "V-Modell-XT-Mustertexte.xml");
            if (mustertextXML.exists() && mustertextXML.isFile()) {
                tmpTarget = new File(mustertextXML.getPath() + "_");
                copyFile(mustertextXML, tmpTarget);
                this.repairXIncludeReference(tmpTarget, mustertextXML, targetFile.getName());
            }
            tmpTarget.delete();
        } catch (SchemaProcessingException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.CouldNotReadFile") + sourceFile.getPath(), e);
        } catch (IOException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.CouldNotReadFile") + sourceFile.getPath(), e);
        } catch (XMLProcessingException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.CouldNotReadFile") + sourceFile.getPath(), e);
        } catch (DocumentLockedException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.CouldNotReadFile") + sourceFile.getPath() + Messages.getString("VariantManagerImpl.BecauseOfLocking") + Messages.getString("VariantManagerImpl.PleaseCloseOpenFiles"), e);
        } catch (MissingLocationException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.CouldNotWriteXMLFile") + targetFile.getPath(), e);
        }
    }

    /**
	 * This method does some dirty refactoring: it opens the result file using line-by-line search and
	 * changes the XML-Attribute xsi:noNamespaceSchemaLocation to a value containing only
	 * "V-Modell-XT-Metamodell.xsd"
	 * If the file contains neither the substring "xsi:noNamespaceSchemaLocation=\"" nor the
	 * substring "V-Modell-XT-Metamodell.xsd" in a single line, the file will only be copied.
	 * 
	 * @param sourceFile The file to be changed.
	 * @param targetFile The file to store the repaired result in
	 * @throws IOException 
	 */
    private void repairSchemaReference(File sourceFile, File targetFile) throws IOException {
        BufferedReader r = new BufferedReader(new FileReader(sourceFile));
        BufferedWriter w = new BufferedWriter(new FileWriter(targetFile));
        String str;
        while (((str = r.readLine()) != null)) {
            if (str.contains("xsi:noNamespaceSchemaLocation=\"")) {
                int schemaOff1 = str.lastIndexOf("xsi:noNamespaceSchemaLocation=\"");
                int schemaOff2 = str.lastIndexOf("V-Modell-XT-Metamodell.xsd");
                if (schemaOff1 < schemaOff2) {
                    str = str.substring(0, schemaOff1 + "xsi:noNamespaceSchemaLocation=\"".length()) + str.substring(schemaOff2);
                }
            }
            w.write(str + System.getProperty("line.separator"));
        }
        r.close();
        w.close();
    }

    /**
	 * This method does some dirty refactoring: it opens the a file using line-by-line search and
	 * changes any XInclude element to point to newref.
	 * 
	 * @param sourceFile The file to be changed.
	 * @param targetFile The file to store the repaired result in
	 * @param newref The value the XInclude is to be set to.
	 * @throws IOException 
	 */
    private void repairXIncludeReference(File sourceFile, File targetFile, String newref) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), "ISO-8859-1"));
        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile), "ISO-8859-1"));
        String str;
        while (((str = r.readLine()) != null)) {
            if (str.contains("xi:include")) {
                int schemaOff1 = str.indexOf("\"");
                int schemaOff2 = str.lastIndexOf("\"");
                if (schemaOff1 < schemaOff2) {
                    str = str.substring(0, schemaOff1 + 1) + newref + str.substring(schemaOff2);
                }
            }
            w.write(str);
            w.newLine();
        }
        r.close();
        w.close();
    }

    public List<String> getVariantNames(File configfile) throws VariantException {
        List<String> list = new ArrayList<String>();
        Document newDoc = null;
        try {
            newDoc = xmlManager.openDocument(configfile, false, false);
        } catch (SchemaProcessingException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.CouldNotReadConfigFile"), e);
        } catch (IOException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.CouldNotReadConfigFile"), e);
        } catch (XMLProcessingException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.CouldNotReadConfigFile"), e);
        } catch (DocumentLockedException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.CouldNotReadConfigFile"), e);
        }
        ObjectModel om = newDoc.getObjectModel();
        Iterator<Link> rootIt = om.getRootLinks();
        EntityInstance rootInstance = null;
        while (rootIt.hasNext()) {
            Link l = rootIt.next();
            try {
                rootInstance = (EntityInstance) l.getTarget(0);
            } catch (TargetIndexOutOfBoundsException e) {
                throw new VariantException(Messages.getString("VariantManagerImpl.NoRootInConfig"), e);
            }
        }
        Iterator<Instance> instIt = null;
        try {
            if (rootInstance != null) {
                instIt = rootInstance.getTargetInstances("Variante");
            }
        } catch (TypeMismatchException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.NoVarianteInVarianten"), e);
        }
        while (instIt != null && instIt.hasNext()) {
            EntityInstance vk = (EntityInstance) instIt.next();
            log.info(Messages.getString("VariantManagerImpl.VarianteFound") + vk.getName() + Messages.getString("VariantManagerImpl.InConfigFile"));
            list.add(vk.getName());
        }
        xmlManager.closeDocument(newDoc);
        return list;
    }

    private void loadXMLConfiguration(File configfile) throws VariantException {
        Document newDoc = null;
        try {
            newDoc = xmlManager.openDocument(configfile, false, false);
        } catch (SchemaProcessingException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.CouldNotReadConfigFile"), e);
        } catch (IOException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.CouldNotReadConfigFile"), e);
        } catch (XMLProcessingException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.CouldNotReadConfigFile"), e);
        } catch (DocumentLockedException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.CouldNotReadConfigFile"), e);
        }
        ObjectModel om = newDoc.getObjectModel();
        Iterator<Link> rootIt = om.getRootLinks();
        EntityInstance rootInstance = null;
        while (rootIt.hasNext()) {
            Link l = rootIt.next();
            try {
                rootInstance = (EntityInstance) l.getTarget(0);
            } catch (TargetIndexOutOfBoundsException e) {
                throw new VariantException(Messages.getString("VariantManagerImpl.NoRootInConfig"), e);
            }
        }
        Iterator<Instance> instIt = null;
        try {
            if (rootInstance != null) {
                instIt = rootInstance.getTargetInstances("Variante");
            }
        } catch (TypeMismatchException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.NoVarianteInVarianten"), e);
        }
        while (instIt != null && instIt.hasNext()) {
            StringInstance varianteDir = null;
            StringInstance varianteFile = null;
            EntityInstance vk = (EntityInstance) instIt.next();
            log.info(Messages.getString("VariantManagerImpl.VarianteFound") + vk.getName() + Messages.getString("VariantManagerImpl.InConfigFile"));
            try {
                varianteDir = (StringInstance) vk.getOutgoingLink("Verzeichnis").getFirstTarget();
                varianteFile = (StringInstance) vk.getOutgoingLink("Dateiname").getFirstTarget();
            } catch (TypeMismatchException e) {
                throw new VariantException(Messages.getString("VariantManagerImpl.NoValidVariante"), e);
            }
            log.info(vk.getName() + ": The Variante is located at " + varianteDir.getValue() + System.getProperty("file.separator") + varianteFile.getValue());
            try {
                Iterator<Instance> it = vk.getTargetInstances("ReferenzvarianteRef");
                if (it.hasNext()) {
                    EntityInstance refReference = (EntityInstance) it.next();
                    if (refReference != null) {
                        log.info(vk.getName() + ": The Referenzmodell is Variantenkonfiguration " + refReference.getName());
                    }
                } else {
                    log.info(vk.getName() + ": This is a root Referenzmodell!");
                }
            } catch (TypeMismatchException e) {
                throw new VariantException(Messages.getString("VariantManagerImpl.NoReferenzvarianteRef"), e);
            }
            VarianteImpl varKonf = new VarianteImpl(vk.getName(), varianteDir.getValue(), varianteFile.getValue());
            this.addVariante(varKonf);
            instanceToWrapperMap.put(vk, varKonf);
        }
        try {
            if (rootInstance != null) {
                instIt = rootInstance.getTargetInstances("Variante");
            }
        } catch (TypeMismatchException e) {
            throw new VariantException(Messages.getString("VariantManagerImpl.NoVarianteInVarianten"), e);
        }
        while (instIt != null && instIt.hasNext()) {
            EntityInstance vk = (EntityInstance) instIt.next();
            Variante vkWrapper = instanceToWrapperMap.get(vk);
            EntityInstance refReference;
            try {
                Iterator<Instance> it = vk.getTargetInstances("ReferenzvarianteRef");
                if (it.hasNext()) {
                    refReference = (EntityInstance) it.next();
                    if (refReference != null) {
                        Variante refVariante = instanceToWrapperMap.get(refReference);
                        vkWrapper.setReferenzvariante(refVariante);
                    }
                }
            } catch (TypeMismatchException e) {
                throw new VariantException(Messages.getString("VariantManagerImpl.NoReferenzvarianteRef"), e);
            }
        }
        xmlManager.closeDocument(newDoc);
    }

    private static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            if (sourceLocation.getName().equals("CVS") || sourceLocation.getName().equals(".svn")) {
                return;
            }
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        } else {
            copyFile(sourceLocation, targetLocation);
        }
    }

    private static void copyFile(File sourceLocation, File targetLocation) throws IOException {
        InputStream in = new FileInputStream(sourceLocation);
        OutputStream out = new FileOutputStream(targetLocation);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    private static void rmdir(File f) throws java.io.IOException {
        if (f.isDirectory()) {
            File[] fs = f.listFiles();
            for (int i = 0; i < fs.length; i++) {
                rmdir(fs[i]);
            }
        }
        if (!(f.delete())) {
            throw new java.io.IOException("cannot delete " + f.getPath());
        }
    }

    public static VariantManager getInstance() {
        return instance;
    }
}
