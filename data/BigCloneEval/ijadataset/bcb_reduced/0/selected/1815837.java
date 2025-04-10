package org.mortbay.jetty.openspaces.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.util.FileUtils;

/**
 * Simple plugin that takes a bundled war file, a pu.xml file and the jetty dependencies of this plugin
 * itself to create an artifact suitable for deployment with os:deploy
 * 
 * @goal generate-pu
 * @requiresDependencyResolution runtime
 * @phase package
 * @description Generates an openspaces PU for a war project
 */
public class JOSGeneratePUMojo extends AbstractMojo {

    /**
     * @parameter expression="${project.build.directory}/${project.artifactId}-${project.version}.war"
     */
    private String warFile;

    /**
     * @parameter expression="src/main/openspaces/pu.xml"
     */
    private String puXMLFile;

    /**
     * @parameter expression="${project.build.directory}/${project.artifactId}-${project.version}-pu"
     */
    private String serverDirectory;

    /**
     * @parameter expression="${project.build.directory}/${project.artifactId}-${project.version}.jar"
     */
    private String artifactName;

    /** 
     * @parameter expression="${plugin.artifacts}" 
     */
    private java.util.List pluginArtifacts;

    /**
     * The Jar archiver.
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#jar}"
     * @required
     */
    private JarArchiver jarArchiver;

    /**
     * 
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (puXMLFile == null || !FileUtils.fileExists(puXMLFile)) {
            throw new MojoExecutionException("missing pu.xml file");
        }
        if (warFile == null || !FileUtils.fileExists(warFile)) {
            throw new MojoExecutionException("missing war file");
        }
        try {
            String puTargetDir = serverDirectory + File.separator + "META-INF" + File.separator + "spring";
            String libTargetDir = serverDirectory + File.separator + "lib";
            FileUtils.mkdir(serverDirectory);
            FileUtils.mkdir(puTargetDir);
            FileUtils.mkdir(libTargetDir);
            FileUtils.copyFileToDirectory(new File(puXMLFile), new File(puTargetDir));
            FileUtils.copyFileToDirectory(new File(warFile), new File(serverDirectory));
            for (Iterator artifactIterator = pluginArtifacts.iterator(); artifactIterator.hasNext(); ) {
                Artifact artifact = (Artifact) artifactIterator.next();
                if (artifact.getGroupId().equals("org.mortbay.jetty")) {
                    FileUtils.copyFileToDirectory(artifact.getFile().getPath(), libTargetDir);
                }
            }
            jarArchiver.addDirectory(new File(serverDirectory));
            jarArchiver.setDestFile(new File(artifactName));
            jarArchiver.createArchive();
        } catch (IOException ioe) {
            throw new MojoExecutionException("unable to assemble", ioe);
        } catch (ArchiverException ae) {
            throw new MojoExecutionException("unable to assembly jar", ae);
        }
    }
}
