package org.baselinetest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.surefire.booter.output.ForkingStreamConsumer;
import org.apache.maven.surefire.booter.output.OutputConsumer;
import org.apache.maven.surefire.booter.output.StandardOutputConsumer;
import org.apache.maven.surefire.booter.output.SupressFooterOutputConsumerProxy;
import org.apache.maven.surefire.booter.output.SupressHeaderOutputConsumerProxy;
import org.apache.maven.surefire.booter.shade.org.codehaus.plexus.util.cli.CommandLineUtils;
import org.apache.maven.surefire.booter.shade.org.codehaus.plexus.util.cli.Commandline;
import org.apache.maven.surefire.booter.shade.org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Mojo for running and validating the results of baseline tests. Defaults the
 * minimum and maximum memory to 128m.
 * <p/>
 * Baseline tests are launched in a separate VM using the Classworlds launcher.
 * The Classworlds launcher uses a configuration file of jars which prevents
 * line too long errors.
 * </p>
 * 
 * @goal test
 * @phase test
 * @requiresDependencyResolution test
 */
public class TestMojo extends AbstractMojo {

    private static final String CLASSWORLDS_LAUNCHER_CLASSNAME = "org.codehaus.classworlds.Launcher";

    private static final String TESTRUNNER_CLASSNAME = "org.baselinetest.TestRunner";

    private static final String TESTVALIDATOR_CLASSNAME = "org.baselinetest.TestValidator2";

    private static final String CLASSWORLDS_CONFLICT_ID = "classworlds:classworlds:jar";

    /**
     * Artifacts to be placed in the system classpath of the form
     * "groupId:artifactId:type". Each artifact should be in its own "<value>:.
     * <p/>
     * Ex. <systemArtifacts> <param>group1:artifact1:jar</param>
     * <param>group1:artifact2:jar</param> </systemArtifacts>
     * 
     * @parameter
     */
    private Set<String> systemArtifacts;

    /**
     * Comma delimited list of directories to be placed in the system classpath.
     * Each directory should be in its own "<value>:.
     * <p/>
     * Ex. <systemDirectories> <param>target/classes</param>
     * <param>target/test-classes</param> </systemDirectories>
     * 
     * @parameter
     */
    private Set<String> systemDirectories;

    /**
     * @parameter expression="${project.testDependencies}"
     * @required
     * @readonly
     */
    private List<Dependency> testDependencies;

    /**
     * Comma delimited list of test contexts for use with command line plugin
     * invocation. Specifying a context list here will override the test
     * configuration in the POM.
     * 
     * @parameter expression="${baselinetest.contexts}"
     */
    private String contexts;

    /**
     * Comma delimited list of test suites for use with command line plugin
     * invocation. Specifying a suite list here will override the test
     * configuration in the pom.
     * 
     * @parameter expression="${baselinetest.suites}"
     */
    private String suites;

    /**
     * List of TestProfiles to use.
     * 
     * @parameter
     */
    private List<TestProfile> testProfiles;

    /**
     * Boolean for skipping the baseline tests.
     * 
     * @parameter expression="${baselinetest.skip}" default-value="false"
     */
    private boolean skip;

    /**
     * The base directory of the project being tested.
     * 
     * @parameter expression="${basedir}"
     * @required
     * @readonly
     */
    private File basedir;

    /**
     * Plugin artifacts. Used to find the classworlds library.
     * 
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    private Collection<Artifact> pluginArtifacts;

    /**
     * @parameter expression="${settings.localRepository}"
     * @required
     * @readonly
     */
    private String localRepository;

    /**
     * Test classpath elements. Used to generate the classworlds conf of the
     * forked test process.
     * 
     * @parameter expression="${project.testClasspathElements}"
     * @required
     * @readonly
     */
    private List<String> testClasspathElements;

    /**
     * Other VM args for forked java TestRunner process for use with command
     * line plugin invocation.
     * 
     * @parameter expression="${baselinetest.run.vmargs}"
     */
    private String runVmArgs;

    /**
     * Enable debugging by specifying the debugPort.
     * 
     * @parameter expression="${baselinetest.run.debugPort}"
     */
    private String debugPort;

    /**
     * Initial memory for forked java TestRunner process for use with command
     * line plugin invocation.
     * 
     * @parameter expression="${baselinetest.run.initalmemory}"
     */
    private String runInitialMemory;

    /**
     * Max memory for forked java TestRunner process for use with command line
     * plugin invocation.
     * 
     * @parameter expression="${baselinetest.run.maxmemory}"
     */
    private String runMaxMemory;

    /**
     * Other VM args for forked java TestValidator process for use with command
     * line plugin invocation.
     * 
     * @parameter expression="${baselinetest.validate.vmargs}"
     */
    private String validateVmArgs;

    /**
     * Initial memory for forked java TestValidator process for use with command
     * line plugin invocation.
     * 
     * @parameter expression="${baselinetest.validate.initalmemory}"
     */
    private String validateInitialMemory;

    /**
     * Max memory for forked java TestValidator process for use with command
     * line plugin invocation.
     * 
     * @parameter expression="${baselinetest.validate.maxmemory}"
     */
    private String validateMaxMemory;

    /**
     * Option to specify the jvm (or path to the java executable) to use with
     * the forking options. For the default, the jvm will be the same as the one
     * used to run Maven.
     * 
     * @parameter expression="${jvm}"
     */
    private String jvm;

    /**
     * Classworlds configuration file for the TestRunner. This file will be
     * generated by the plugin.
     * 
     * @parameter expression="${project.build.directory}/runnerclassworlds.conf"
     */
    private File runnerClassworldsConfFile;

    /**
     * Classworlds configuration file for the TestValidator. This file will be
     * generated by the plugin.
     * 
     * @parameter 
     *            expression="${project.build.directory}/validatorclassworlds.conf"
     */
    private File validatorClassworldsConfFile;

    /**
     * Zip file to archive failed test results.
     * 
     * @parameter expression=
     *            "${project.build.directory}/${project.artifactId}-test-results.zip"
     */
    private File failedTestResults;

    /**
     * Boolean for creating a zip file of the results directory on failure.
     * 
     * @parameter expression="${baselinetest.archiveOnFail}"
     *            default-value="false"
     */
    private boolean archiveOnFail;

    private File testDir;

    private String classpath = null;

    private Commandline cli;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!skip) {
            if (contexts != null || suites != null || debugPort != null) {
                testProfiles = new ArrayList<TestProfile>(1);
                TestProfile manualProfile = new TestProfile();
                manualProfile.contexts = contexts;
                manualProfile.suites = suites;
                manualProfile.runInitialMemory = runInitialMemory;
                manualProfile.runMaxMemory = runMaxMemory;
                manualProfile.runVmArgs = runVmArgs;
                manualProfile.validateInitialMemory = validateInitialMemory;
                manualProfile.validateMaxMemory = validateMaxMemory;
                manualProfile.validateVmArgs = validateVmArgs;
                manualProfile.debugPort = debugPort;
                testProfiles.add(manualProfile);
            }
            if (testProfiles == null) {
                getLog().info("No contexts or suites specified.  Skipping baselines.");
                return;
            }
            if (jvm == null || "".equals(jvm)) {
                jvm = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                getLog().debug("Using JVM: " + jvm);
            }
            localRepository = localRepository.replace('/', File.separatorChar);
            Set<String> systemPaths = new HashSet<String>();
            getSystemArtifacts(systemPaths);
            getSystemDirectories(systemPaths);
            testDir = new File(basedir.getAbsolutePath() + "/target/test");
            testDir.mkdirs();
            PrintWriter runnerConfWriter, validatorConfWriter;
            PrintWriter validations;
            try {
                runnerConfWriter = new PrintWriter(new FileWriter(runnerClassworldsConfFile));
                validatorConfWriter = new PrintWriter(new FileWriter(validatorClassworldsConfFile));
                File validation = new File(runnerClassworldsConfFile.getParentFile(), "validations.txt");
                validations = new PrintWriter(new FileWriter(validation));
            } catch (IOException e) {
                throw new MojoExecutionException("Error opening classworlds conf file", e);
            }
            runnerConfWriter.println("main is " + TESTRUNNER_CLASSNAME + " from app");
            validatorConfWriter.println("main is " + TESTVALIDATOR_CLASSNAME + " from app");
            runnerConfWriter.println("[app]");
            validatorConfWriter.println("[app]");
            for (Iterator<String> iter = testClasspathElements.iterator(); iter.hasNext(); ) {
                Object element = iter.next();
                String testClasspathElement = element.toString();
                File testClasspathElementFile = new File(testClasspathElement);
                if (!testClasspathElementFile.exists()) {
                    getLog().warn("Skipping non-existent dependency : " + testClasspathElement);
                } else if (systemPaths.contains(testClasspathElement)) {
                    getLog().info("Excluding system dependency from standard dependencies: " + testClasspathElement);
                } else {
                    runnerConfWriter.println("load " + testClasspathElement);
                    validatorConfWriter.println("load " + testClasspathElement);
                }
            }
            runnerConfWriter.close();
            validatorConfWriter.close();
            classpath = null;
            for (Iterator<Artifact> iter = pluginArtifacts.iterator(); iter.hasNext(); ) {
                Artifact artifact = iter.next();
                if (CLASSWORLDS_CONFLICT_ID.equals(artifact.getDependencyConflictId())) {
                    classpath = artifact.getFile().getAbsolutePath();
                }
            }
            if (classpath == null) {
                throw new MojoExecutionException("Unable to locate classworlds jar file");
            }
            StringBuffer classpathBuffer = new StringBuffer(classpath);
            Iterator<String> systemDependencyIterator = systemPaths.iterator();
            while (systemDependencyIterator.hasNext()) {
                String systemDependency = systemDependencyIterator.next();
                classpathBuffer.append(File.pathSeparator);
                classpathBuffer.append(systemDependency);
            }
            classpath = classpathBuffer.toString();
            cli = new Commandline();
            cli.setExecutable(jvm);
            cli.setWorkingDirectory(testDir.getAbsolutePath());
            Iterator<TestProfile> profileIter = testProfiles.iterator();
            while (profileIter.hasNext()) {
                Object obj = profileIter.next();
                if (!(obj instanceof TestProfile)) {
                    throw new MojoFailureException("Test Profile element was not of type TestProfile");
                }
                TestProfile profile = (TestProfile) obj;
                profile.discoverSuites(testDir);
                if (profile.runInitialMemory == null) {
                    profile.runInitialMemory = "128m";
                }
                if (profile.runMaxMemory == null) {
                    profile.runMaxMemory = "128m";
                }
                if (profile.validateInitialMemory == null) {
                    profile.validateInitialMemory = "128m";
                }
                if (profile.validateMaxMemory == null) {
                    profile.validateMaxMemory = "128m";
                }
                if (profile.contexts == null && profile.suites == null) {
                    getLog().info("Test profile contains no contexts and no suites.  Skipping profile.");
                    return;
                }
                if (profile.contexts == null || profile.suites == null) {
                    throw new MojoFailureException("Test profile missing value for contexts or suites");
                }
                String[] contextArray = profile.contexts.split(",");
                forkJavaProcesses(jvm, runnerClassworldsConfFile, contextArray, profile.discoveredSuites, profile.runVmArgs, profile.runInitialMemory, profile.runMaxMemory, profile.debugPort);
                validateProfile(validations, contextArray, profile.discoveredSuites);
            }
            validations.close();
            forkJavaProcess(jvm, validatorClassworldsConfFile);
        } else {
            getLog().info("Baselines are skipped.");
        }
    }

    private void validateProfile(PrintWriter validations, String[] contextArray, List<String> suites) {
        File parent = new File(validatorClassworldsConfFile.getParentFile(), "test");
        for (int contextI = 0; contextI < contextArray.length; contextI++) {
            for (Iterator<String> suiteI = suites.iterator(); suiteI.hasNext(); ) {
                String currentSuite = (String) suiteI.next();
                File contextFile = new File(parent, "../../src/test/contexts/" + contextArray[contextI].trim());
                validations.println(contextFile.getAbsolutePath());
                File suiteFile = new File(parent, "../../src/test/suites/" + currentSuite);
                validations.println(suiteFile.getAbsolutePath());
                File resultFile = new File(parent, "results/" + currentSuite);
                validations.println(resultFile.getAbsolutePath());
            }
        }
    }

    private void getSystemArtifacts(Set<String> systemPaths) throws MojoExecutionException {
        if (systemArtifacts != null) {
            Iterator<String> artifactIterator = systemArtifacts.iterator();
            while (artifactIterator.hasNext()) {
                String artifactString = (String) artifactIterator.next();
                String ARTIFACT_DELIMITER = ":";
                StringTokenizer tokenizer = new StringTokenizer(artifactString, ARTIFACT_DELIMITER);
                int NUM_ARTIFACT_TOKENS = 3;
                if (tokenizer.countTokens() != NUM_ARTIFACT_TOKENS) {
                    throw new MojoExecutionException("Invalid system artifact list: " + artifactString);
                }
                String groupId = tokenizer.nextToken();
                String artifactId = tokenizer.nextToken();
                String type = tokenizer.nextToken();
                Iterator<Dependency> testDependencyIterator = testDependencies.iterator();
                while (testDependencyIterator.hasNext()) {
                    Dependency artifact = (Dependency) testDependencyIterator.next();
                    String artifactGroupId = artifact.getGroupId();
                    String artifactArtifactId = artifact.getArtifactId();
                    String artifactType = artifact.getType();
                    boolean match = true;
                    match &= groupId.equals(artifactGroupId);
                    match &= artifactId.equals(artifactArtifactId);
                    match &= type.equals(artifactType);
                    if (match) {
                        String version = artifact.getVersion();
                        String groupPath = groupId.replace('.', File.separatorChar);
                        StringBuffer artifactPathBuffer = new StringBuffer();
                        artifactPathBuffer.append(localRepository);
                        artifactPathBuffer.append(File.separator);
                        artifactPathBuffer.append(groupPath);
                        artifactPathBuffer.append(File.separator);
                        artifactPathBuffer.append(artifactId);
                        artifactPathBuffer.append(File.separator);
                        artifactPathBuffer.append(version);
                        artifactPathBuffer.append(File.separator);
                        artifactPathBuffer.append(artifactId);
                        artifactPathBuffer.append('-');
                        artifactPathBuffer.append(version);
                        if ("test-jar".equals(type)) {
                            artifactPathBuffer.append("-tests.jar");
                        } else if ("ejb".equals(type)) {
                            artifactPathBuffer.append(".jar");
                        } else if ("ejb-client".equals(type)) {
                            artifactPathBuffer.append("-client.jar");
                        } else {
                            artifactPathBuffer.append('.');
                            artifactPathBuffer.append(type);
                        }
                        String artifactPath = artifactPathBuffer.toString();
                        getLog().info("Adding system artifact: " + artifactPath);
                        systemPaths.add(artifactPath);
                        break;
                    }
                }
            }
        }
    }

    private void getSystemDirectories(Set<String> systemPaths) throws MojoExecutionException {
        if (systemDirectories != null) {
            Iterator<String> directoryIterator = systemDirectories.iterator();
            while (directoryIterator.hasNext()) {
                String directoryString = directoryIterator.next();
                File dependencyFile = new File(basedir, directoryString);
                if (!dependencyFile.exists()) {
                    String errorString = "System directory does not exist: " + directoryString;
                    getLog().error(errorString);
                    throw new MojoExecutionException(errorString);
                }
                String dependencyPath = dependencyFile.getAbsolutePath();
                getLog().info("Adding system directory: " + dependencyPath);
                systemPaths.add(dependencyPath);
            }
        }
    }

    private void forkJavaProcess(String jvm, File classworldsConfFile) throws MojoFailureException {
        cli.clearArgs();
        cli.createArg().setValue("-classpath");
        cli.createArg().setValue(classpath);
        cli.createArg().setValue("-enableassertions");
        cli.createArg().setValue("-Dclassworlds.conf=" + classworldsConfFile.getAbsolutePath());
        cli.createArg().setValue(CLASSWORLDS_LAUNCHER_CLASSNAME);
        File validationFile = new File(classworldsConfFile.getParent(), "validations.txt");
        cli.createArg().setValue(validationFile.getAbsolutePath());
        Log log = getLog();
        if (log.isDebugEnabled()) {
            log.debug(cli.toString());
        }
        int returnCode;
        try {
            StreamConsumer out = getForkingStreamConsumer(false, false);
            StreamConsumer err = getForkingStreamConsumer(false, false);
            returnCode = CommandLineUtils.executeCommandLine(cli, out, err);
        } catch (Throwable e) {
            MojoFailureException excep = new MojoFailureException("Error launching baseline");
            e.printStackTrace();
            excep.initCause(e);
            throw excep;
        }
        if (returnCode != 0) {
            if (archiveOnFail) {
                archiveTestResults();
            }
            throw new MojoFailureException("Error executing baseline (return code " + returnCode + ")");
        }
    }

    /**
     * Iterates over contexts and suites and forks a java process.
     * 
     * @param jvm
     * @param classworldsConfFile
     * @param contextArray
     * @param suites
     * @param vmArgs
     * @param initialMemory
     * @param maxMemory
     * 
     * @throws MojoFailureException
     */
    private void forkJavaProcesses(String jvm, File classworldsConfFile, String[] contextArray, List<String> suites, String vmArgs, String initialMemory, String maxMemory, String debugPort) throws MojoFailureException {
        for (int contextI = 0; contextI < contextArray.length; contextI++) {
            for (Iterator<String> suiteI = suites.iterator(); suiteI.hasNext(); ) {
                String currentSuite = (String) suiteI.next();
                cli.clearArgs();
                cli.createArg().setValue("-classpath");
                cli.createArg().setValue(classpath);
                cli.createArg().setValue("-enableassertions");
                if (debugPort != null) {
                    cli.createArg().setValue("-Xdebug");
                    cli.createArg().setValue("-Djava.compiler=NONE");
                    cli.createArg().setValue("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=" + debugPort.toString());
                }
                if (vmArgs != null && !vmArgs.equals("")) {
                    StringTokenizer tokenizer = new StringTokenizer(vmArgs, " ");
                    while (tokenizer.hasMoreTokens()) {
                        cli.createArg().setValue(tokenizer.nextToken());
                    }
                }
                cli.createArg().setValue("-Xms" + initialMemory);
                cli.createArg().setValue("-Xmx" + maxMemory);
                cli.createArg().setValue("-Dclassworlds.conf=" + classworldsConfFile.getAbsolutePath());
                cli.createArg().setValue(CLASSWORLDS_LAUNCHER_CLASSNAME);
                cli.createArg().setValue("../../src/test/contexts/" + contextArray[contextI].trim());
                cli.createArg().setValue("../../src/test/suites/" + currentSuite);
                cli.createArg().setValue("results/" + currentSuite);
                Log log = getLog();
                if (log.isDebugEnabled()) {
                    log.debug(cli.toString());
                }
                int returnCode;
                try {
                    StreamConsumer out = getForkingStreamConsumer(false, false);
                    StreamConsumer err = getForkingStreamConsumer(false, false);
                    returnCode = CommandLineUtils.executeCommandLine(cli, out, err);
                } catch (Throwable e) {
                    MojoFailureException excep = new MojoFailureException("Error launching baseline");
                    e.printStackTrace();
                    excep.initCause(e);
                    throw excep;
                }
                if (returnCode != 0) {
                    if (archiveOnFail) {
                        archiveTestResults();
                    }
                    throw new MojoFailureException("Error executing baseline (return code " + returnCode + ")");
                }
            }
        }
    }

    private StreamConsumer getForkingStreamConsumer(boolean showHeading, boolean showFooter) {
        OutputConsumer outputConsumer = new StandardOutputConsumer();
        if (!showHeading) {
            outputConsumer = new SupressHeaderOutputConsumerProxy(outputConsumer);
        }
        if (!showFooter) {
            outputConsumer = new SupressFooterOutputConsumerProxy(outputConsumer);
        }
        StreamConsumer consumer = new ForkingStreamConsumer(outputConsumer);
        return consumer;
    }

    private void archiveTestResults() {
        try {
            getLog().info("Archiving failed test results to " + failedTestResults.getPath());
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(failedTestResults));
            File resultsDir = new File(basedir.getPath() + "/target/test/results");
            archiveDirectory(out, resultsDir, "results" + File.separator);
            out.close();
        } catch (IOException e) {
            getLog().error("Error archiving test results", e);
        }
    }

    /**
     * Recursively archives a directory and its contents. Will create entries
     * for empty directories.
     * 
     * @param out
     *            zip output stream
     * @param dir
     *            directory to add to the zip
     * @param path
     *            relative path to use constructing the zip entry ending in
     *            File.separator
     * 
     * @throws IOException
     */
    private void archiveDirectory(ZipOutputStream out, File dir, String path) throws IOException {
        byte[] buf = new byte[16384];
        File[] files = dir.listFiles();
        if (files.length > 0) {
            for (int x = 0; x < files.length; x++) {
                if (files[x].isFile()) {
                    FileInputStream in = new FileInputStream(files[x]);
                    out.putNextEntry(new ZipEntry(path + files[x].getName()));
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                    in.close();
                } else {
                    archiveDirectory(out, files[x], path + files[x].getName() + File.separator);
                }
            }
        } else {
            out.putNextEntry(new ZipEntry(path));
            out.closeEntry();
        }
    }
}
