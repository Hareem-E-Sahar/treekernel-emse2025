package org.jcompany.maven.qa;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.surefire.booter.ForkConfiguration;
import org.apache.maven.surefire.booter.SurefireBooter;
import org.apache.maven.surefire.booter.SurefireBooterForkException;
import org.apache.maven.surefire.booter.SurefireExecutionException;
import org.apache.maven.surefire.report.BriefConsoleReporter;
import org.apache.maven.surefire.report.BriefFileReporter;
import org.apache.maven.surefire.report.ConsoleReporter;
import org.apache.maven.surefire.report.DetailedConsoleReporter;
import org.apache.maven.surefire.report.FileReporter;
import org.apache.maven.surefire.report.ForkingConsoleReporter;
import org.apache.maven.surefire.report.XMLReporter;
import org.codehaus.plexus.util.StringUtils;
import org.jcompany.maven.PlcAbstractMojo;

/**
 * Roda os testes funcionais
 * 
 * @requiresDependencyResolution test
 * @goal funcional
 * 
 */
public class PlcTestFunctionalMojo extends PlcAbstractMojo {

    private static boolean executou = false;

    /**
	 * 
	 * @parameter
	 */
    private String teste;

    /**
	 * Set this to 'true' to bypass unit tests execution, but still compile them. Its use is NOT RECOMMENDED, but quite
	 * convenient on occasion.
	 * 
	 * @parameter expression="${pular.funcional}"
	 */
    private boolean skip;

    /**
	 * Set this to 'true' to bypass unit tests execution, but still compile them. Its use is NOT RECOMMENDED, but quite
	 * convenient on occasion.
	 * 
	 * @parameter
	 */
    private boolean suite;

    /**
	 * Set this to true to ignore a failure during testing. Its use is NOT RECOMMENDED, but quite convenient on
	 * occasion.
	 * 
	 * @parameter expression="${maven.test.failure.ignore}" default-value="true"
	 */
    private boolean testFailureIgnore;

    /**
	 * The base directory of the project being tested. This can be obtained in your unit test by
	 * System.getProperty("basedir").
	 * 
	 * @parameter expression="${basedir}"
	 * @required
	 */
    private File basedir;

    /**
	 * The directory containing generated classes of the project being tested.
	 * 
	 * @parameter expression="${project.build.outputDirectory}"
	 * @required
	 */
    private File classesDirectory;

    /**
	 * The directory containing generated test classes of the project being tested.
	 * 
	 * @parameter expression="${project.build.testOutputDirectory}"
	 * @required
	 */
    private File testClassesDirectory;

    /**
	 * The classpath elements of the project being tested.
	 * 
	 * @parameter expression="${project.testClasspathElements}"
	 * @required
	 * @readonly
	 */
    private List classpathElements;

    /**
	 * Base directory where all reports are written to.
	 * 
	 * @parameter expression="${project.build.directory}/reports-jwebunit"
	 */
    private File reportsDirectory;

    /**
	 * The test source directory containing test class sources.
	 * 
	 * @parameter expression="${project.build.testSourceDirectory}"
	 * @required
	 */
    private File testSourceDirectory;

    /**
	 * Specify this parameter(can be a comma separated list) if you want to use the test pattern matching notation, Ant
	 * pattern matching, to select tests to run. The Ant pattern will be used to create an include pattern formatted
	 * like <code>**&#47;${test}.java</code> When used, the <code>includes</code> and <code>excludes</code>
	 * patterns parameters are ignored.
	 * 
	 * @parameter expression="${test}"
	 */
    private String test;

    /**
	 * List of patterns (separated by commas) used to specify the tests that should be included in testing. When not
	 * specified and when the <code>test</code> parameter is not specified, the default includes will be
	 * <code>**&#47;Test*.java   **&#47;*Test.java   **&#47;*TestCase.java</code>
	 * 
	 * @parameter
	 */
    private List includes;

    /**
	 * List of patterns (separated by commas) used to specify the tests that should be excluded in testing. When not
	 * specified and when the <code>test</code> parameter is not specified, the default excludes will be
	 * <code>**&#47;Abstract*Test.java  **&#47;Abstract*TestCase.java **&#47;*$*</code>
	 * 
	 * @parameter
	 */
    private List excludes;

    /**
	 * ArtifactRepository of the localRepository. To obtain the directory of localRepository in unit tests use
	 * System.setProperty( "localRepository").
	 * 
	 * @parameter expression="${localRepository}"
	 * @required
	 * @readonly
	 */
    private ArtifactRepository localRepository;

    /**
	 * List of System properties to pass to the JUnit tests.
	 * 
	 * @parameter
	 */
    private Properties systemProperties;

    /**
	 * Map of of plugin artifacts.
	 * 
	 * @parameter expression="${plugin.artifactMap}"
	 * @required
	 * @readonly
	 */
    private Map pluginArtifactMap;

    /**
	 * Map of of project artifacts.
	 * 
	 * @parameter expression="${project.artifactMap}"
	 * @required
	 * @readonly
	 */
    private Map projectArtifactMap;

    /**
	 * Option to print summary of test suites or just print the test cases that has errors.
	 * 
	 * @parameter expression="${surefire.printSummary}" default-value="true"
	 */
    private boolean printSummary;

    /**
	 * Selects the formatting for the test report to be generated. Can be set as brief or plain.
	 * 
	 * @parameter expression="${surefire.reportFormat}" default-value="brief"
	 */
    private String reportFormat;

    /**
	 * Option to generate a file test report or just output the test report to the console.
	 * 
	 * @parameter expression="${surefire.useFile}" default-value="true"
	 */
    private boolean useFile;

    /**
	 * When forking, set this to true to redirect the unit test standard output to a file (found in
	 * reportsDirectory/testName-output.txt).
	 * 
	 * @parameter expression="${maven.test.redirectTestOutputToFile}" default-value="false"
	 */
    private boolean redirectTestOutputToFile;

    /**
	 * Option to specify the forking mode. Can be "never", "once" or "always". "none" and "pertest" are also accepted
	 * for backwards compatibility.
	 * 
	 * @parameter expression="${forkMode}" default-value="never"
	 */
    private String forkMode;

    /**
	 * Option to specify the jvm (or path to the java executable) to use with the forking options. For the default, the
	 * jvm will be the same as the one used to run Maven.
	 * 
	 * @parameter expression="${jvm}"
	 */
    private String jvm;

    /**
	 * Arbitrary options to set on the command line.
	 * 
	 * @parameter expression="${argLine}"
	 */
    private String argLine;

    /**
	 * Additional environments to set on the command line.
	 * 
	 * @parameter
	 */
    private Map environmentVariables = new HashMap();

    /**
	 * Command line working directory.
	 * 
	 * @parameter
	 */
    private File workingDirectory;

    /**
	 * When false it makes tests run using the standard classloader delegation instead of the default Maven isolated
	 * classloader. Only used when forking (forkMode is not "none").<br/> Setting it to false helps with some problems
	 * caused by conflicts between xml parsers in the classpath and the Java 5 provider parser.
	 * 
	 * @parameter expression="${childDelegation}" default-value="true"
	 */
    private boolean childDelegation;

    /**
	 * Groups for this test. Only classes/methods/etc decorated with one of the groups specified here will be included
	 * in test run, if specified.
	 * 
	 * @parameter expression="${groups}"
	 */
    private String groups;

    /**
	 * Excluded groups. Any methods/classes/etc with one of the groups specified in this list will specifically not be
	 * run.
	 * 
	 * @parameter expression="${excludedGroups}"
	 */
    private String excludedGroups;

    /**
	 * List of TestNG suite xml file locations, seperated by commas. It should be noted that if suiteXmlFiles is
	 * specified, <b>no</b> other tests will be run, ignoring other parameters, like includes and excludes.
	 * 
	 * @parameter
	 */
    private File[] suiteXmlFiles;

    /**
	 * The attribute thread-count allows you to specify how many threads should be allocated for this execution. Only
	 * makes sense to use in conjunction with parallel.
	 * 
	 * @parameter expression="${threadCount}" default-value="5"
	 */
    private int threadCount;

    /**
	 * When you use the parallel attribute, TestNG will try to run all your test methods in separate threads, except for
	 * methods that depend on each other, which will be run in the same thread in order to respect their order of
	 * execution.
	 * 
	 * @parameter expression="${parallel}" default-value="false"
	 * @todo test how this works with forking, and console/file output parallelism
	 */
    private boolean parallel;

    /**
	 * Whether to trim the stack trace in the reports to just the lines within the test, or show the full trace.
	 * 
	 * @parameter expression="${trimStackTrace}" default-value="true"
	 */
    private boolean trimStackTrace;

    /**
	 * Resolves the artifacts needed.
	 * 
	 * @component
	 */
    private ArtifactResolver artifactResolver;

    /**
	 * Creates the artifact
	 * 
	 * @component
	 */
    private ArtifactFactory artifactFactory;

    /**
	 * The plugin remote repositories declared in the pom.
	 * 
	 * @parameter expression="${project.pluginArtifactRepositories}"
	 */
    private List remoteRepositories;

    /**
	 * For retrieval of artifact's metadata.
	 * 
	 * @component
	 */
    private ArtifactMetadataSource metadataSource;

    private static final String BRIEF_REPORT_FORMAT = "brief";

    private static final String PLAIN_REPORT_FORMAT = "plain";

    private Properties originalSystemProperties;

    /**
	 * Flag to disable the generation of report files in xml format.
	 * 
	 * @parameter expression="${disableXmlReport}" default-value="false"
	 */
    private boolean disableXmlReport;

    /**
	 * Option to pass dependencies to the system's classloader instead of using an isolated class loader when forking.
	 * Prevents problems with JDKs which implement the service provider lookup mechanism by using the system's
	 * classloader.
	 * 
	 * @parameter expression="${surefire.useSystemClassLoader}" default-value="false"
	 */
    private boolean useSystemClassLoader;

    /**
	 * Pega o arquivo de dependencia a partir do nome.
	 * 
	 * @param nomeArtefato
	 *            GroupId + artifactId
	 * @return Arquivo de depencia
	 * @throws MojoExecutionException
	 */
    protected File getDependencia(String nomeArtefato) throws MojoExecutionException {
        for (Iterator iter = this.dependencias.iterator(); iter.hasNext(); ) {
            Artifact artefato = (Artifact) iter.next();
            String tipo = artefato.getType();
            if ("jar".equals(tipo)) {
                String nome = artefato.getGroupId() + "." + artefato.getArtifactId();
                if (nome.equals(nomeArtefato)) {
                    return artefato.getFile();
                }
            }
        }
        throw new MojoExecutionException("A dependencia " + nomeArtefato + " nao foi encontrada.");
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (isSkip()) return;
        if (!isModuloPrincipal() || executou) return;
        executou = true;
        System.setProperty("projeto.atual", basedir.getAbsolutePath());
        if (verifyParameters()) {
            SurefireBooter surefireBooter = constructSurefireBooter();
            getLog().info("Surefire report directory: " + reportsDirectory);
            boolean success;
            try {
                success = surefireBooter.run();
            } catch (SurefireBooterForkException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            } catch (SurefireExecutionException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
            if (originalSystemProperties != null) {
                System.setProperties(originalSystemProperties);
            }
            if (!success) {
                String msg = "There are test failures.";
                if (testFailureIgnore) {
                    getLog().error(msg);
                } else {
                    throw new MojoFailureException(msg);
                }
            }
        }
    }

    private boolean verifyParameters() throws MojoFailureException {
        if (!testClassesDirectory.exists()) {
            getLog().info("No tests to run.");
            return false;
        }
        if (parallel) {
            if (threadCount < 1) {
                throw new MojoFailureException("Must have at least one thread in parallel mode");
            }
        }
        if (useSystemClassLoader && ForkConfiguration.FORK_NEVER.equals(forkMode)) {
            getLog().warn("useSystemClassloader=true setting has no effect when not forking");
        }
        return true;
    }

    private SurefireBooter constructSurefireBooter() throws MojoExecutionException, MojoFailureException {
        SurefireBooter surefireBooter = new SurefireBooter();
        Artifact surefireArtifact = (Artifact) pluginArtifactMap.get("org.apache.maven.surefire:surefire-booter");
        if (surefireArtifact == null) {
            throw new MojoExecutionException("Unable to locate surefire-booter in the list of plugin artifacts");
        }
        surefireArtifact.isSnapshot();
        for (Iterator iter = this.dependencias.iterator(); iter.hasNext(); ) {
            Artifact artefato = (Artifact) iter.next();
            String tipo = artefato.getType();
            if ("test-jar".equals(tipo)) {
                surefireBooter.addSurefireBootClassPathUrl(artefato.getFile().getAbsolutePath());
            }
        }
        Artifact junitArtifact;
        Artifact testNgArtifact;
        try {
            addArtifact(surefireBooter, surefireArtifact);
            junitArtifact = (Artifact) projectArtifactMap.get("junit:junit");
            testNgArtifact = (Artifact) projectArtifactMap.get("org.testng:testng");
            if (testNgArtifact != null) {
                addArtifact(surefireBooter, testNgArtifact);
                VersionRange range = VersionRange.createFromVersionSpec("[4.7,)");
                if (!range.containsVersion(testNgArtifact.getSelectedVersion())) {
                    throw new MojoFailureException("TestNG support requires version 4.7 or above. You have declared version " + testNgArtifact.getVersion());
                }
                addProvider(surefireBooter, "surefire-testng", surefireArtifact.getBaseVersion(), testNgArtifact);
            } else if (junitArtifact != null && junitArtifact.getBaseVersion().startsWith("4")) {
                addProvider(surefireBooter, "surefire-junit4", surefireArtifact.getBaseVersion(), null);
            } else {
                addProvider(surefireBooter, "surefire-junit", surefireArtifact.getBaseVersion(), null);
            }
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException("Unable to locate required surefire provider dependency: " + e.getMessage(), e);
        } catch (InvalidVersionSpecificationException e) {
            throw new MojoExecutionException("Error determining the TestNG version requested: " + e.getMessage(), e);
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("Error to resolving surefire provider dependency: " + e.getMessage(), e);
        }
        if (suiteXmlFiles != null && suiteXmlFiles.length > 0) {
            if (testNgArtifact == null) {
                throw new MojoExecutionException("suiteXmlFiles is configured, but there is no TestNG dependency");
            }
            for (int i = 0; i < suiteXmlFiles.length; i++) {
                File file = suiteXmlFiles[i];
                if (file.exists()) {
                    surefireBooter.addTestSuite("org.apache.maven.surefire.testng.TestNGXmlTestSuite", new Object[] { file, testSourceDirectory.getAbsolutePath() });
                }
            }
        } else {
            List includes;
            List excludes;
            if (test != null) {
                includes = new ArrayList();
                excludes = new ArrayList();
                String[] testRegexes = StringUtils.split(test, ",");
                for (int i = 0; i < testRegexes.length; i++) {
                    includes.add("**/" + testRegexes[i] + ".java");
                }
            } else {
                includes = this.includes;
                excludes = this.excludes;
                if (includes == null || includes.size() == 0) {
                    if (suite) includes = new ArrayList(Arrays.asList(new String[] { "**/*WebTestSuite.java" })); else {
                        includes = new ArrayList(Arrays.asList(new String[] { "**/*WebTest.java" }));
                    }
                }
                if (excludes == null || excludes.size() == 0) {
                    if (suite) excludes = new ArrayList(Arrays.asList(new String[] { "**/Abstract*Test.java", "**/Abstract*TestCase.java", "**/*$*", "**/*WebTest.java" })); else {
                        excludes = new ArrayList(Arrays.asList(new String[] { "**/Abstract*Test.java", "**/Abstract*TestCase.java", "**/*$*" }));
                    }
                }
            }
            if (testNgArtifact != null) {
                surefireBooter.addTestSuite("org.apache.maven.surefire.testng.TestNGDirectoryTestSuite", new Object[] { testClassesDirectory, includes, excludes, groups, excludedGroups, Boolean.valueOf(parallel), new Integer(threadCount), testSourceDirectory.getAbsolutePath() });
            } else {
                String junitDirectoryTestSuite;
                if (junitArtifact != null && junitArtifact.getBaseVersion() != null && junitArtifact.getBaseVersion().startsWith("4")) {
                    junitDirectoryTestSuite = "org.apache.maven.surefire.junit4.JUnit4DirectoryTestSuite";
                } else {
                    junitDirectoryTestSuite = "org.apache.maven.surefire.junit.JUnitDirectoryTestSuite";
                }
                surefireBooter.addTestSuite(junitDirectoryTestSuite, new Object[] { testClassesDirectory, includes, excludes });
            }
        }
        getLog().debug("Test Classpath :");
        for (Iterator i = classpathElements.iterator(); i.hasNext(); ) {
            String classpathElement = (String) i.next();
            getLog().debug("  " + classpathElement);
            surefireBooter.addClassPathUrl(classpathElement);
        }
        ForkConfiguration fork = new ForkConfiguration();
        fork.setForkMode(forkMode);
        processSystemProperties(!fork.isForking());
        if (getLog().isDebugEnabled()) {
            showMap(systemProperties, "system property");
        }
        if (fork.isForking()) {
            fork.setSystemProperties(systemProperties);
            if (jvm == null || "".equals(jvm)) {
                jvm = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                getLog().debug("Using JVM: " + jvm);
            }
            fork.setJvmExecutable(jvm);
            if (workingDirectory != null) {
                fork.setWorkingDirectory(workingDirectory);
            } else {
                fork.setWorkingDirectory(basedir);
            }
            fork.setArgLine(argLine);
            fork.setEnvironmentVariables(environmentVariables);
            if (getLog().isDebugEnabled()) {
                showMap(environmentVariables, "environment variable");
                fork.setDebug(true);
            }
        }
        surefireBooter.setForkConfiguration(fork);
        surefireBooter.setChildDelegation(childDelegation);
        surefireBooter.setUseSystemClassLoader(useSystemClassLoader);
        addReporters(surefireBooter, fork.isForking());
        return surefireBooter;
    }

    private void showMap(Map map, String setting) {
        for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            String value = (String) map.get(key);
            getLog().debug("Setting " + setting + " [" + key + "]=[" + value + "]");
        }
    }

    private void addProvider(SurefireBooter surefireBooter, String provider, String version, Artifact filteredArtifact) throws ArtifactNotFoundException, ArtifactResolutionException {
        Artifact providerArtifact = artifactFactory.createDependencyArtifact("org.apache.maven.surefire", provider, VersionRange.createFromVersion(version), "jar", null, Artifact.SCOPE_TEST);
        ArtifactResolutionResult result = resolveArtifact(filteredArtifact, providerArtifact);
        for (Iterator i = result.getArtifacts().iterator(); i.hasNext(); ) {
            Artifact artifact = (Artifact) i.next();
            getLog().debug("Adding to surefire test classpath: " + artifact.getFile().getAbsolutePath());
            surefireBooter.addSurefireClassPathUrl(artifact.getFile().getAbsolutePath());
        }
    }

    private ArtifactResolutionResult resolveArtifact(Artifact filteredArtifact, Artifact providerArtifact) throws ArtifactResolutionException, ArtifactNotFoundException {
        ArtifactFilter filter = null;
        if (filteredArtifact != null) {
            filter = new ExcludesArtifactFilter(Collections.singletonList(filteredArtifact.getGroupId() + ":" + filteredArtifact.getArtifactId()));
        }
        Artifact originatingArtifact = artifactFactory.createBuildArtifact("dummy", "dummy", "1.0", "jar");
        return artifactResolver.resolveTransitively(Collections.singleton(providerArtifact), originatingArtifact, localRepository, remoteRepositories, metadataSource, filter);
    }

    private void addArtifact(SurefireBooter surefireBooter, Artifact surefireArtifact) throws ArtifactNotFoundException, ArtifactResolutionException {
        ArtifactResolutionResult result = resolveArtifact(null, surefireArtifact);
        for (Iterator i = result.getArtifacts().iterator(); i.hasNext(); ) {
            Artifact artifact = (Artifact) i.next();
            getLog().debug("Adding to surefire booter test classpath: " + artifact.getFile().getAbsolutePath());
            surefireBooter.addSurefireBootClassPathUrl(artifact.getFile().getAbsolutePath());
        }
    }

    protected void processSystemProperties(boolean setInSystem) {
        if (systemProperties == null) {
            systemProperties = new Properties();
        }
        originalSystemProperties = (Properties) System.getProperties().clone();
        systemProperties.setProperty("basedir", basedir.getAbsolutePath());
        systemProperties.setProperty("localRepository", localRepository.getBasedir());
        if (setInSystem) {
            Iterator iter = systemProperties.keySet().iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                String value = systemProperties.getProperty(key);
                System.setProperty(key, value);
            }
        }
    }

    /**
	 * <p>
	 * Adds Reporters that will generate reports with different formatting.
	 * <p>
	 * The Reporter that will be added will be based on the value of the parameter useFile, reportFormat, and
	 * printSummary.
	 * 
	 * @param surefireBooter
	 *            The surefire booter that will run tests.
	 * @param forking
	 */
    private void addReporters(SurefireBooter surefireBooter, boolean forking) {
        Boolean trimStackTrace = Boolean.valueOf(this.trimStackTrace);
        if (useFile) {
            if (printSummary) {
                if (forking) {
                    surefireBooter.addReport(ForkingConsoleReporter.class.getName(), new Object[] { trimStackTrace });
                } else {
                    surefireBooter.addReport(ConsoleReporter.class.getName(), new Object[] { trimStackTrace });
                }
            }
            if (BRIEF_REPORT_FORMAT.equals(reportFormat)) {
                surefireBooter.addReport(BriefFileReporter.class.getName(), new Object[] { reportsDirectory, trimStackTrace });
            } else if (PLAIN_REPORT_FORMAT.equals(reportFormat)) {
                surefireBooter.addReport(FileReporter.class.getName(), new Object[] { reportsDirectory, trimStackTrace });
            }
        } else {
            if (BRIEF_REPORT_FORMAT.equals(reportFormat)) {
                surefireBooter.addReport(BriefConsoleReporter.class.getName(), new Object[] { trimStackTrace });
            } else if (PLAIN_REPORT_FORMAT.equals(reportFormat)) {
                surefireBooter.addReport(DetailedConsoleReporter.class.getName(), new Object[] { trimStackTrace });
            }
        }
        if (!disableXmlReport) {
            surefireBooter.addReport(XMLReporter.class.getName(), new Object[] { reportsDirectory, trimStackTrace });
        }
    }

    /**
	 * @return SurefirePlugin Returns the skipExec.
	 */
    public boolean isSkip() {
        return this.skip;
    }

    /**
	 * @param skipExec
	 *            the skipExec to set
	 */
    public void setSkip(boolean skipExec) {
        this.skip = skipExec;
    }

    public boolean isWebServerAvaliable(String url) {
        long inicial = new Date().getTime();
        HttpURLConnection connection = null;
        try {
            URL urlBase = urlBase = new URL(url);
            getLog().info("Verificando se WebServer esta no ar: " + urlBase.toString());
            connection = (HttpURLConnection) urlBase.openConnection();
            connection.connect();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                getLog().info("Resposta do WebServer: " + connection.getResponseCode());
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            long tfinal = new Date().getTime();
            getLog().info("Tempo esperado: " + ((tfinal - inicial) / 1000) + " segundos!");
        }
        return true;
    }

    public String getTeste() {
        return teste;
    }

    public void setTeste(String teste) {
        this.teste = teste;
    }
}
