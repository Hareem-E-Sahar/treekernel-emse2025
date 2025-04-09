package net.sf.aoscat.plugin.output.report;

import static org.junit.Assert.assertEquals;
import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import net.sf.aoscat.blackboard.AbstractBlackboardFactory;
import net.sf.aoscat.blackboard.IBlackboard;
import net.sf.aoscat.core.IControllable;
import net.sf.aoscat.core.configuration.APersistentConfiguration;
import net.sf.aoscat.core.configuration.CProjectConfiguration;
import net.sf.aoscat.core.configuration.CProjectConfiguration.EProjectOptions;
import net.sf.aoscat.exceptions.CErrorException;
import net.sf.aoscat.i18n.EErrorMessages;
import net.sf.aoscat.plugin.output.report.configuration.CReportOutputterConfiguration;
import net.sf.aoscat.plugin.output.report.configuration.CReportOutputterConfiguration.EConfigurationOptions;
import net.sf.aoscat.plugin.output.report.generated.Part;
import net.sf.aoscat.plugin.output.report.generated.Preprocess;
import net.sf.aoscat.plugin.output.report.generated.Report;
import net.sf.aoscat.plugin.output.report.transformationstrategies.CTransformationContext;
import net.sf.aoscat.plugin.output.report.transformationstrategies.EStrategies;
import net.sf.aoscat.utils.CFileUtils;
import net.sf.aoscat.utils.IOutputReader;
import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.junit.Test;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;

/**
 * This class is the 'main' class for the report generation plugin.
 * 
 * @author dreichelt
 *
 */
public class CReportOutputter implements IControllable {

    private CProjectConfiguration projectConfig;

    private final CReportOutputterConfiguration config = new CReportOutputterConfiguration();

    private static final transient Logger logger = Logger.getLogger(CReportOutputter.class);

    /** The transformation context to use -- determines the transformation strategy.*/
    private LinkedList<CTransformationContext> transformationContexts;

    /** The final transformation context -- the one for the integration. Needs different input.*/
    private CTransformationContext integrationContext;

    private volatile int vJobsRunning = 0;

    private final CFileUtils fileUtils = new CFileUtils();

    /** Representation of the report definition -- gets constructed upon call of setup().*/
    private Report report;

    /**
	 * @see net.sf.aoscat.core.IControllable#getName()
	 */
    @Override
    public String getName() {
        return "report";
    }

    /**
	 * Returns a string representation of the report outputter -- the name of the plugin.
	 * @see CReportOutputter#getName()
	 * @see java.lang.Object#toString()
	 */
    public String toString() {
        return getName();
    }

    /**
	 * @see net.sf.aoscat.core.IControllable#getDependencies()
	 */
    @Override
    public Set<String> getDependencies() {
        return new HashSet<String>(config.getStringListOption(EConfigurationOptions.DEPENDENCIES));
    }

    /**
	 * @see net.sf.aoscat.core.IControllable#run()
	 */
    @Override
    public void run() throws CErrorException {
        final String vProject = projectConfig.getOption(EProjectOptions.PROJECTNAME);
        final IBlackboard vBlackboard = AbstractBlackboardFactory.getDefaultFactory().getBlackboard();
        HashMap<String, String> vParams = new HashMap<String, String>();
        vParams.put("workroot", new File(projectConfig.getWorkingPath(this)).getParentFile().toURI().toString());
        vParams.put("sourcefolder", new File(projectConfig.getOption(EProjectOptions.SOURCEFOLDER)).toURI().toString());
        vParams.put("name", vProject);
        vParams.put("outfolder", new File(getOutputFile()).getParentFile().toURI().toString());
        String vOutFile = getOutputFile();
        File vTargetFile = new File(vOutFile);
        List<Thread> vThreads = new LinkedList<Thread>();
        CTransformationContext vJob = transformationContexts.pollFirst();
        while (vJob != null) {
            while (vJobsRunning >= config.getIntOption(EConfigurationOptions.MAXTHREADS)) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
            }
            logger.info("Creating report part '" + vJob.getOutputFile().getAbsolutePath() + "'");
            InputStream vStream = new BufferedInputStream(vBlackboard.getAllInformationForAsStream(vProject));
            Thread t = new Thread(new CJobRunner(vStream, vJob, config.getOption(EConfigurationOptions.TRANSFORMATIONSTRATEGY), config.getOption(EConfigurationOptions.ARCHIVEDIR) + File.separator + projectConfig.getOption(EProjectOptions.PROJECTNAME) + File.separator, this, vParams));
            t.setName("Generating " + vJob.getOutputFile());
            t.start();
            vThreads.add(t);
            vJobsRunning++;
            vJob = transformationContexts.pollFirst();
        }
        for (Thread t : vThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            File vReportDefinition = new File(config.getOption(EConfigurationOptions.REPORTDEFINITION));
            logger.info("Integrating report parts");
            integrationContext.runXSLT(new FileInputStream(vReportDefinition), vParams);
        } catch (FileNotFoundException e1) {
            throw new CErrorException(EErrorMessages.COULDNOTOPEN, e1, config.getOption(EConfigurationOptions.REPORTDEFINITION));
        }
        if (config.getBoolOption(EConfigurationOptions.OPENREPORT)) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(vTargetFile);
                } catch (IOException e) {
                    throw new CErrorException(EErrorMessages.COULDNOTOPEN, e, vTargetFile.getAbsolutePath());
                }
            }
        }
    }

    /**
	 * Small helper method to obtain the full path to the output file.
	 * @return the path of the output file
	 */
    private String getOutputFile() {
        return config.getOption(EConfigurationOptions.OUTPUT_ROOT) + File.separator + projectConfig.getOption(EProjectOptions.PROJECTNAME) + ".html";
    }

    /**
	 * @see net.sf.aoscat.core.IControllable#setup(net.sf.aoscat.core.configuration.CProjectConfiguration)
	 */
    @Override
    public void setup(CProjectConfiguration aConfig) throws CErrorException {
        logger.info("Setting up the Report Outputter");
        this.projectConfig = aConfig;
        String vPath = aConfig.getWorkingPath(this);
        logger.info("Cleaning work directory " + vPath);
        try {
            fileUtils.provideCleanedDirectory(vPath);
        } catch (CErrorException e) {
            logger.error("", e);
        }
        logger.info("done");
        report = this.setupTemplates();
        logger.info("Creating Transformation Contexts");
        transformationContexts = new LinkedList<CTransformationContext>();
        for (Part vPart : report.getPart()) {
            CTransformationContext vJob = new CTransformationContext(EStrategies.valueOf(config.getOption(EConfigurationOptions.TRANSFORMATIONSTRATEGY)));
            List<File> vTemplates = new LinkedList<File>();
            for (Preprocess p : vPart.getPreprocess()) {
                vTemplates.add(new File(config.getOption(EConfigurationOptions.TEMPLATEDIRECTORY) + File.separator + p.getTemplate()));
            }
            vTemplates.add(new File(config.getOption(EConfigurationOptions.TEMPLATEDIRECTORY) + File.separator + vPart.getTemplate()));
            vJob.addTemplates(vTemplates.toArray(new File[0]));
            vJob.setOutput(new File(aConfig.getWorkingPath(this) + File.separator + vPart.getResult()));
            String vDiffFile = vPart.getDiff();
            if (vDiffFile != null) {
                vJob.setDiffFile(aConfig.getWorkingPath(this) + File.separator + vDiffFile);
            }
            transformationContexts.add(vJob);
        }
        integrationContext = new CTransformationContext(EStrategies.valueOf(config.getOption(EConfigurationOptions.TRANSFORMATIONSTRATEGY)));
        File vTemplate = new File(config.getOption(EConfigurationOptions.INTEGRATIONTEMPLATE));
        vTemplate = fileUtils.copyFileToDir(vTemplate, aConfig.getWorkingPath(this) + File.separator);
        integrationContext.addTemplates(vTemplate);
        integrationContext.setOutput(new File(this.getOutputFile()));
    }

    /**
	 * This method creates all required template files if they do not exist yet or if the default templates should always be used.
	 * @throws CErrorException in case the templates could not be set up
	 */
    private Report setupTemplates() throws CErrorException {
        Report vRet = null;
        List<File> vTemplates = new LinkedList<File>();
        StringTokenizer vTok = new StringTokenizer(this.config.getOption(EConfigurationOptions.FILESTOCOPY));
        while (vTok.hasMoreTokens()) {
            String vCurFile = vTok.nextToken();
            File vFile = new File(this.config.getOption(EConfigurationOptions.OUTPUT_ROOT) + File.separator + "res" + File.separator + vCurFile);
            vTemplates.add(vFile);
        }
        File vReportDefinition = new File(this.config.getOption(EConfigurationOptions.REPORTDEFINITION));
        vTemplates.add(vReportDefinition);
        File vIntegrationTemplate = new File(this.config.getOption(EConfigurationOptions.INTEGRATIONTEMPLATE));
        vTemplates.add(vIntegrationTemplate);
        initializeFiles(vTemplates);
        vTemplates.removeAll(vTemplates);
        logger.info("Reading report definition file");
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance("net.sf.aoscat.plugin.output.report.generated");
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            vRet = (Report) unmarshaller.unmarshal(vReportDefinition);
            for (Part vPart : vRet.getPart()) {
                for (Preprocess p : vPart.getPreprocess()) {
                    vTemplates.add(new File(config.getOption(EConfigurationOptions.TEMPLATEDIRECTORY) + File.separator + p.getTemplate()));
                }
                vTemplates.add(new File(config.getOption(EConfigurationOptions.TEMPLATEDIRECTORY) + File.separator + vPart.getTemplate()));
            }
        } catch (JAXBException e) {
            throw new CErrorException(EErrorMessages.INVALIDTEMPLATE, e, vReportDefinition.getAbsolutePath());
        }
        initializeFiles(vTemplates);
        return vRet;
    }

    /**
	 * This methods deletes the given files if configured to always use the default templates,
	 * then creates the default templates if they do not exist.
	 * @param vTemplates the template files to produce in initialized state
	 * @throws CErrorException in case the default template could not be created
	 */
    private void initializeFiles(List<File> vTemplates) throws CErrorException {
        if (config.getBoolOption(EConfigurationOptions.ALWAYSUSEDEFAULTTEMPLATES)) {
            for (File vFile : vTemplates) {
                if (vFile.exists()) {
                    vFile.delete();
                }
            }
        }
        for (File vFile : vTemplates) {
            if (!vFile.exists()) {
                logger.info("Creating default template " + vFile.getAbsolutePath());
                fileUtils.createDefaultTemplateFile(vFile);
            }
        }
    }

    /**
	 * @see net.sf.aoscat.core.IControllable#setup(net.sf.aoscat.core.configuration.CProjectConfiguration, net.sf.aoscat.utils.IOutputReader, net.sf.aoscat.utils.IOutputReader)
	 */
    @Override
    public void setup(CProjectConfiguration aConfig, IOutputReader aOut, IOutputReader aErr) throws CErrorException {
        this.setup(aConfig);
    }

    /**
	 * Signalizes that a scheduled job was completed.
	 */
    synchronized void jobDone() {
        vJobsRunning--;
    }

    /**
	 * Gets called by a job if an exception occured during the execution.
	 * @param e the exception that occurred
	 */
    synchronized void exceptionWhileTransforming(CErrorException e) {
        logger.error("", e);
    }

    /**
	 * This method tests whether the sheer size of the data associated with Firefox3 can be handled
	 * when creating reports.
	 * @throws CErrorException in case anything went wrong
	 */
    @Test
    public void testGenerateReportForMySQL() throws CErrorException {
        final CProjectConfiguration vConfig = CProjectConfiguration.getDefaultProjConf("G:\\AOSCAT\\TESTDATA\\SOURCECODETOANALYZE\\mysql-5.1.30");
        this.setup(vConfig);
        this.run();
    }

    /**
	 * This method tests whether the sheer size of the data associated with Firefox3 can be handled
	 * when creating reports.
	 * @throws CErrorException in case anything went wrong
	 */
    @Test
    public void testGenerateReportForFirefox3() throws CErrorException {
        final CProjectConfiguration vConfig = CProjectConfiguration.getDefaultProjConf("G:\\AOSCAT\\TESTDATA\\SOURCECODETOANALYZE\\firefox3");
        this.setup(vConfig);
        this.run();
    }

    /**
	 * Shortcut test method to resume a failed report generation for firefox-evolution
	 * when creating reports.
	 * @throws CErrorException in case anything went wrong
	 */
    @Test
    public void testGenerateEvolutionReportForFirefox3() throws CErrorException {
        final CProjectConfiguration vConfig = CProjectConfiguration.getDefaultProjConf("G:\\AOSCAT\\TESTDATA\\firefox\\firefox-evolution");
        this.setup(vConfig);
        this.run();
    }

    /**
	 * This test case creates a report for 'wordpad', ensuring that no exceptions are thrown.
	 * @throws Exception hopefully not.
	 */
    @Test
    public void testReportWordpad() throws Exception {
        final CProjectConfiguration vConfig = CProjectConfiguration.getDefaultProjConf("G:\\AOSCAT\\TESTDATA\\SOURCECODETOANALYZE\\wordpad");
        this.setup(vConfig);
        this.run();
    }

    /**
	 * This test case creates a report for all the sample open source projects available,
	 * ensuring that no exceptions occur.
	 * @throws Exception hopefully not.
	 */
    @Test
    public void testReportAll() throws Exception {
        String[] vPaths = { "G:\\AOSCAT\\TESTDATA\\SOURCECODETOANALYZE\\wordpad", "G:\\AOSCAT\\TESTDATA\\SOURCECODETOANALYZE\\cccc-3.1.4", "G:\\AOSCAT\\TESTDATA\\SOURCECODETOANALYZE\\DCPlusPlus-0.7091-src", "G:\\AOSCAT\\TESTDATA\\SOURCECODETOANALYZE\\doxygen-1.5.9", "G:\\AOSCAT\\TESTDATA\\SOURCECODETOANALYZE\\firefox3", "G:\\AOSCAT\\TESTDATA\\SOURCECODETOANALYZE\\HelloWorld", "G:\\AOSCAT\\TESTDATA\\SOURCECODETOANALYZE\\jikes-1.22", "G:\\AOSCAT\\TESTDATA\\SOURCECODETOANALYZE\\mysql-5.1.30", "G:\\AOSCAT\\TESTDATA\\SOURCECODETOANALYZE\\obfuscated", "G:\\AOSCAT\\TESTDATA\\SOURCECODETOANALYZE\\relations", "G:\\AOSCAT\\TESTDATA\\SOURCECODETOANALYZE\\sapdb-source-7.3.00.48", "G:\\AOSCAT\\TESTDATA\\SOURCECODETOANALYZE\\WinMerge-2.10.2-src" };
        for (String vStr : vPaths) {
            CProjectConfiguration vConfig = CProjectConfiguration.getDefaultProjConf(vStr);
            this.setup(vConfig);
            this.run();
        }
    }

    /**
	 * This test case creates a report for mysql using all available strategies.
	 * @throws Exception hopefully not
	 */
    @SuppressWarnings("unchecked")
    @Test
    public void testStrategies() throws Exception {
        long start, end;
        List<File> vProducedFiles = new LinkedList<File>();
        this.config.setBoolOption(EConfigurationOptions.OPENREPORT, false);
        HashMap<EStrategies, Long> times = new HashMap<EStrategies, Long>();
        for (EStrategies vStrategy : EStrategies.values()) {
            start = System.currentTimeMillis();
            CProjectConfiguration vConfig = CProjectConfiguration.getDefaultProjConf("G:\\AOSCAT\\TESTDATA\\SOURCECODETOANALYZE\\mysql-5.1.30");
            this.setup(vConfig);
            File vFileForStrategy = new File(getOutputFile().replace(".html", "-" + vStrategy + ".html"));
            this.config.setOption(EConfigurationOptions.TRANSFORMATIONSTRATEGY, vStrategy.name());
            this.run();
            end = System.currentTimeMillis();
            times.put(vStrategy, end - start);
            File vOut = new File(getOutputFile());
            fileUtils.copyFile(vOut, vFileForStrategy);
            vProducedFiles.add(vFileForStrategy);
        }
        logger.info("Times:");
        logger.info(times);
        DetailedDiff myDiff;
        Tidy tidy1 = new Tidy();
        Tidy tidy2 = new Tidy();
        tidy1.setXmlOut(true);
        tidy2.setXmlOut(true);
        logger.info("Checking for XML differences in the output files");
        String vReference = vProducedFiles.get(0).toURI().toString();
        for (int i = 1; i < vProducedFiles.size(); i++) {
            File vCurFile = vProducedFiles.get(i);
            if (!vCurFile.exists()) {
                logger.fatal("No output file " + vCurFile.getAbsolutePath());
                continue;
            }
            String vCurFileURI = vCurFile.toURI().toString();
            logger.info(String.format("Comparing %s to %s", vReference, vCurFileURI));
            if (config.getBoolOption(EConfigurationOptions.USEJTIDY)) {
                PipedOutputStream vOutFromTidy1 = new PipedOutputStream();
                PipedInputStream vInFromTidy1 = new PipedInputStream(vOutFromTidy1);
                tidy1.parse(new BufferedInputStream(new FileInputStream(vReference)), vOutFromTidy1);
                PipedOutputStream vOutFromTidy2 = new PipedOutputStream();
                PipedInputStream vInFromTidy2 = new PipedInputStream(vOutFromTidy2);
                tidy2.parse(new BufferedInputStream(new FileInputStream(vCurFile)), vOutFromTidy2);
                myDiff = new DetailedDiff(new Diff(new InputSource(vInFromTidy1), new InputSource(vInFromTidy2)));
            } else {
                myDiff = new DetailedDiff(new Diff(new InputSource(vReference), new InputSource(vCurFileURI)));
            }
            List allDifferences = myDiff.getAllDifferences();
            assertEquals(myDiff.toString(), 0, allDifferences.size());
        }
        logger.info("All good :)");
    }

    /**
	 * @see net.sf.aoscat.core.IControllable#getConfiguration()
	 */
    @Override
    public APersistentConfiguration getConfiguration() {
        return config;
    }
}
