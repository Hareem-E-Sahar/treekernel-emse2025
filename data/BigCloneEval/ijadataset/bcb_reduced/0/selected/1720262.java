package org.mobicents.eclipslee.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.util.FileUtils;

/**
 SleeJar defines common behaviour amongst all the various types of deployable jars.
 Specifically construction of classpaths, properties that define whether the task should
 use deployment descriptor info to deduce what classes should be included etc.
 <p>
 A default name for the deployment descriptor is assumed if one is not provided, which means
 in the common case the 'foo'jarxml property can safely be ommitted. This task also defines
 an extxml property to allow oc extension descriptors to be defined.
*/
public abstract class SleeJar extends org.apache.tools.ant.taskdefs.Jar implements Component {

    public SleeJar(String archiveType, String emptyBehaviour) {
        super();
        this.archiveType = archiveType;
        this.emptyBehavior = emptyBehavior;
        this.jarXmlStr = archiveType + ".xml";
    }

    public void setMetainfbase(String metainfbase) {
        this.metainfbase = new File(metainfbase);
    }

    public void setMetaInfBase(File metainfbase) {
        this.metainfbase = metainfbase;
    }

    public File getComponentFile(Project project) throws BuildException {
        if (generateName && zipFile == null) {
            try {
                zipFile = File.createTempFile(getComponentType(), ".jar");
                zipFile.deleteOnExit();
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
                ZipEntry ze = new ZipEntry("META-INF/");
                ze.setSize(0);
                ze.setMethod(ZipEntry.STORED);
                ze.setCrc(new CRC32().getValue());
                zos.putNextEntry(ze);
                zos.close();
            } catch (IOException ioe) {
                throw new BuildException(ioe);
            }
        }
        return zipFile;
    }

    public void execute() throws BuildException {
        if (autoinclude && super.isInUpdateMode()) throw new BuildException("update mode not supported when autoinclude=true");
        if (null == jarXmlStr) throw new BuildException(getJarXmlName() + " attribute is required", getLocation());
        if (autoinclude) {
            includeClasses();
            autoinclude = false;
        }
        getComponentFile(getProject());
        super.execute();
    }

    public void setClasspath(Path newClasspath) {
        if (this.classpath == null) this.classpath = newClasspath; else this.classpath.append(newClasspath);
    }

    public Path createClasspath() {
        if (classpath == null) classpath = new Path(getProject());
        return classpath.createPath();
    }

    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }

    public void setGeneratename(boolean onoff) {
        this.generateName = onoff;
    }

    public void setAutoinclude(boolean onoff) {
        this.autoinclude = onoff;
    }

    protected abstract void includeTypeSpecificClasses() throws BuildException;

    protected abstract String getComponentType();

    protected abstract String getJarXmlName();

    private void includeClasses() throws BuildException {
        if (classpathFileList == null) {
            if (classpath == null) throw new BuildException("autoinclude set, but classpath attribute not set");
            String[] classpathList = classpath.list();
            classpathFileList = new File[classpathList.length];
            classpathZipList = new ZipFile[classpathList.length];
            for (int i = 0; i < classpathList.length; ++i) classpathFileList[i] = fileUtils.normalize(classpathList[i]);
        }
        processJarXml();
        includeTypeSpecificClasses();
    }

    protected final void includeClass(String className) throws BuildException {
        String osPath = className.replace('.', File.separatorChar) + ".class";
        String urlPath = className.replace('.', '/') + ".class";
        for (int i = 0; i < classpathFileList.length; ++i) {
            File cpBase = classpathFileList[i];
            if (!cpBase.exists()) continue;
            if (cpBase.isDirectory()) {
                File testFile = fileUtils.resolveFile(cpBase, osPath);
                if (testFile.exists()) {
                    FileSet fileSet = new FileSet();
                    fileSet.setDir(cpBase);
                    fileSet.setIncludes(osPath);
                    super.addFileset(fileSet);
                    return;
                }
                continue;
            }
            if (cpBase.isFile()) {
                if (classpathZipList[i] == null) {
                    try {
                        classpathZipList[i] = new ZipFile(cpBase);
                    } catch (IOException ioe) {
                        classpathZipList[i] = null;
                        continue;
                    }
                }
                if (classpathZipList[i].getEntry(urlPath) != null) {
                    ZipFileSet zipFileSet = new ZipFileSet();
                    zipFileSet.setSrc(classpathFileList[i]);
                    zipFileSet.setIncludes(urlPath);
                    super.addZipfileset(zipFileSet);
                    return;
                }
                continue;
            }
        }
        throw new BuildException("Cannot locate class in classpath: " + className);
    }

    protected final void setJarXml(String jarXmlStr) {
        this.jarXmlStr = jarXmlStr;
    }

    public final void setExtjarxml(String extJarXmlStr) {
        this.extJarXmlStr = extJarXmlStr;
    }

    private void processJarXml() {
        jarxml = getAbsoluteFile((null == metainfbase) ? new File(jarXmlStr) : new File(metainfbase, jarXmlStr));
        processDescriptor(jarxml, archiveType + ".xml");
        if (null != extJarXmlStr) {
            File extXml = getAbsoluteFile((null == metainfbase) ? new File(extJarXmlStr) : new File(metainfbase, extJarXmlStr));
            processDescriptor(extXml, extXml.getName());
        }
    }

    private void processDescriptor(File xmlDtor, String xmlDtorName) {
        if (!xmlDtor.exists()) throw new BuildException("Deployment descriptor: " + xmlDtor + " does not exist.");
        ZipFileSet fs = new ZipFileSet();
        fs.setFile(xmlDtor);
        fs.setFullpath("META-INF/" + xmlDtorName);
        super.addFileset(fs);
    }

    protected void cleanUp() {
        if (classpathZipList != null) {
            for (int i = 0; i < classpathZipList.length; ++i) {
                if (classpathZipList[i] != null) {
                    try {
                        classpathZipList[i].close();
                    } catch (IOException e) {
                    }
                }
            }
            classpathFileList = null;
            classpathZipList = null;
        }
    }

    protected File getAbsoluteFile(File file) {
        if (!file.isAbsolute()) return new File(getProject().getProperty("basedir"), file.toString()); else return file;
    }

    protected final File getJarXml() {
        return jarxml;
    }

    private String jarXmlStr;

    private String extJarXmlStr;

    private File metainfbase = null;

    private File jarxml = null;

    private Path classpath;

    private boolean autoinclude = true;

    private boolean generateName;

    private File[] classpathFileList;

    private ZipFile[] classpathZipList;

    protected static final FileUtils fileUtils = FileUtils.newFileUtils();

    protected static final SleeDTDResolver entityResolver = new SleeDTDResolver();
}
