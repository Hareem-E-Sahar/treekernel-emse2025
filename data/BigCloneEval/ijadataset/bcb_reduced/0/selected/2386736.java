package net.sf.ovanttasks.ovanttasks;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

/**
 * @version 1.0
 * @since 11.08.2003
 * @author lars.gersmann@orangevolt.com
 * 
 * (c) Copyright 2005 Orangevolt (www.orangevolt.com).
 */
public class SFXTask extends AbstractStubTask {

    private static final String __MODE_ = "__MODE__";

    private static final String __EXECUTABLE__PATH__ = "__EXECUTABLE__PATH__";

    private static final String __EXECUTABLE__NAME__ = "__EXECUTABLE__NAME__";

    private void createJavaStub() {
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output));
            ZipEntry ze = new ZipEntry(stub.class.getName().replace('.', '/') + "16x16.gif");
            zos.putNextEntry(ze);
            copy(iconFile != null ? new FileInputStream(iconFile) : stub.class.getResourceAsStream("stub16x16.gif"), zos);
            ze = new ZipEntry(stub.class.getName().replace('.', '/') + ".class");
            zos.putNextEntry(ze);
            copy(stub.class.getResourceAsStream("stub.class"), zos);
            ze = new ZipEntry(stub.class.getName().replace('.', '/') + ".properties");
            zos.putNextEntry(ze);
            Properties stubProperties = new Properties();
            stubProperties.put(__MODE_, isConsole ? "console" : "gui");
            stubProperties.put(__EXECUTABLE__NAME__, execute);
            stubProperties.put(__EXECUTABLE__PATH__, workingDirectory);
            stubProperties.store(zos, "ORANGEVOLT stub 1.0 properties");
            zos.close();
        } catch (Exception e) {
            throw new BuildException("Unable to copy jar stub", e);
        }
        org.apache.tools.ant.taskdefs.Jar jar = new org.apache.tools.ant.taskdefs.Jar();
        jar.setCompress(true);
        jar.setUpdate(true);
        ZipFileSet zfs = new ZipFileSet();
        zfs.setSrc(archive);
        jar.addZipfileset(zfs);
        jar.setDestFile(output);
        try {
            Manifest manifest = Manifest.getDefaultManifest();
            Manifest.Section section = manifest.getMainSection();
            section.addConfiguredAttribute(new Manifest.Attribute("Main-Class", "net.sf.ovanttasks.ovanttasks.stub"));
            jar.addConfiguredManifest(manifest);
            jar.setOwningTarget(getOwningTarget());
            jar.setProject(getProject());
            jar.setTaskName(getTaskName());
            jar.execute();
        } catch (ManifestException ex) {
            throw new BuildException("ManifestException occured.", ex);
        }
    }

    @Override
    public void execute() throws BuildException {
        super.execute();
        Properties p = new Properties();
        log("Generating " + mode + " executable " + output);
        if (mode.equals("unix")) {
            p.put(__EXECUTABLE__NAME__, prepareUnixCommand(execute));
            p.put(__EXECUTABLE__PATH__, prepareUnixCommand(workingDirectory));
            createShellStub(p);
        } else {
            p.put(__EXECUTABLE__PATH__, workingDirectory);
            p.put(__EXECUTABLE__NAME__, execute);
            if (mode.equals("java")) {
                createJavaStub();
            } else {
                if (mode.equals("win32")) {
                    createWin32Stub(p);
                } else throw new BuildException("Unknown mode " + mode);
            }
        }
    }

    @Override
    protected InputStream getWin32ConsoleLessStub() {
        return SFXTask.class.getResourceAsStream("stub.exe");
    }

    @Override
    protected InputStream getWin32ConsoleStub() {
        return SFXTask.class.getResourceAsStream("stub-console.exe");
    }

    @Override
    protected InputStream getShellStub() {
        return SFXTask.class.getResourceAsStream("stub.sh");
    }

    @Override
    protected int getTokenLength() {
        return __EXECUTABLE__NAME__.length();
    }
}
