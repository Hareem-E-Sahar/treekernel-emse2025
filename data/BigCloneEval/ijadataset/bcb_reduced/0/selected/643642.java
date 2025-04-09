package org.apache.harmony.archive.tests.java.util.jar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import tests.support.Support_Exec;
import tests.support.resource.Support_Resources;

/**
 * 
 * tests for various cases of java -jar ... execution with .zip files as args
 * some tests are just copy of JarExecTest ones 
 */
public class ZipExecTest extends junit.framework.TestCase {

    public void test_1562() throws Exception {
        Manifest man = new Manifest();
        Attributes att = man.getMainAttributes();
        att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        att.put(Attributes.Name.MAIN_CLASS, "foo.bar.execjartest.Foo");
        File outputZip = File.createTempFile("hyts_", ".zip");
        outputZip.deleteOnExit();
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(outputZip));
        File resources = Support_Resources.createTempFolder();
        for (String zipClass : new String[] { "Foo", "Bar" }) {
            zout.putNextEntry(new ZipEntry("foo/bar/execjartest/" + zipClass + ".class"));
            zout.write(getResource(resources, "hyts_" + zipClass + ".ser"));
        }
        zout.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
        man.write(zout);
        zout.close();
        String[] args = new String[] { "-jar", outputZip.getAbsolutePath() };
        String res = Support_Exec.execJava(args, null, false);
        assertTrue("Error executing ZIP : result returned was incorrect.", res.startsWith("FOOBAR"));
    }

    /**
	 * tests Class-Path entry in manifest
	 * @throws Exception in case of troubles
	 */
    public void test_zip_class_path() throws Exception {
        File fooZip = File.createTempFile("hyts_", ".zip");
        File barZip = File.createTempFile("hyts_", ".zip");
        fooZip.deleteOnExit();
        barZip.deleteOnExit();
        Manifest man = new Manifest();
        Attributes att = man.getMainAttributes();
        att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        att.put(Attributes.Name.MAIN_CLASS, "foo.bar.execjartest.Foo");
        att.put(Attributes.Name.CLASS_PATH, barZip.getName());
        File resources = Support_Resources.createTempFolder();
        ZipOutputStream zoutFoo = new ZipOutputStream(new FileOutputStream(fooZip));
        zoutFoo.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
        man.write(zoutFoo);
        zoutFoo.putNextEntry(new ZipEntry("foo/bar/execjartest/Foo.class"));
        zoutFoo.write(getResource(resources, "hyts_Foo.ser"));
        zoutFoo.close();
        ZipOutputStream zoutBar = new ZipOutputStream(new FileOutputStream(barZip));
        zoutBar.putNextEntry(new ZipEntry("foo/bar/execjartest/Bar.class"));
        zoutBar.write(getResource(resources, "hyts_Bar.ser"));
        zoutBar.close();
        String[] args = new String[] { "-jar", fooZip.getAbsolutePath() };
        String res = Support_Exec.execJava(args, null, false);
        assertTrue("Error executing JAR : result returned was incorrect.", res.startsWith("FOOBAR"));
        att.put(Attributes.Name.CLASS_PATH, "xx yy zz " + barZip.getName());
        zoutFoo = new ZipOutputStream(new FileOutputStream(fooZip));
        zoutFoo.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
        man.write(zoutFoo);
        zoutFoo.putNextEntry(new ZipEntry("foo/bar/execjartest/Foo.class"));
        zoutFoo.write(getResource(resources, "hyts_Foo.ser"));
        zoutFoo.close();
        res = Support_Exec.execJava(args, null, false);
        assertTrue("Error executing JAR : result returned was incorrect.", res.startsWith("FOOBAR"));
        att.put(Attributes.Name.CLASS_PATH, ".." + File.separator + barZip.getParentFile().getName() + File.separator + barZip.getName());
        zoutFoo = new ZipOutputStream(new FileOutputStream(fooZip));
        zoutFoo.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
        man.write(zoutFoo);
        zoutFoo.putNextEntry(new ZipEntry("foo/bar/execjartest/Foo.class"));
        zoutFoo.write(getResource(resources, "hyts_Foo.ser"));
        zoutFoo.close();
        res = Support_Exec.execJava(args, null, false);
        assertTrue("Error executing JAR : result returned was incorrect.", res.startsWith("FOOBAR"));
    }

    public void test_zip_jar_mix() throws Exception {
        File fooJar = File.createTempFile("hyts_", ".jar");
        File barZip = File.createTempFile("hyts_", ".zip");
        fooJar.deleteOnExit();
        barZip.deleteOnExit();
        Manifest man = new Manifest();
        Attributes att = man.getMainAttributes();
        att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        att.put(Attributes.Name.MAIN_CLASS, "foo.bar.execjartest.Foo");
        att.put(Attributes.Name.CLASS_PATH, barZip.getName());
        File resources = Support_Resources.createTempFolder();
        JarOutputStream joutFoo = new JarOutputStream(new FileOutputStream(fooJar), man);
        joutFoo.putNextEntry(new JarEntry("foo/bar/execjartest/Foo.class"));
        joutFoo.write(getResource(resources, "hyts_Foo.ser"));
        joutFoo.close();
        ZipOutputStream zoutBar = new ZipOutputStream(new FileOutputStream(barZip));
        zoutBar.putNextEntry(new ZipEntry("foo/bar/execjartest/Bar.class"));
        zoutBar.write(getResource(resources, "hyts_Bar.ser"));
        zoutBar.close();
        String[] args = new String[] { "-jar", fooJar.getAbsolutePath() };
        String res = Support_Exec.execJava(args, null, false);
        assertTrue("Error executing JAR : result returned was incorrect.", res.startsWith("FOOBAR"));
    }

    public void test_zip_jar_mix_1() throws Exception {
        File fooZip = File.createTempFile("hyts_", ".zip");
        File barJar = File.createTempFile("hyts_", ".jar");
        fooZip.deleteOnExit();
        barJar.deleteOnExit();
        Manifest man = new Manifest();
        Attributes att = man.getMainAttributes();
        att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        att.put(Attributes.Name.MAIN_CLASS, "foo.bar.execjartest.Foo");
        att.put(Attributes.Name.CLASS_PATH, barJar.getName());
        File resources = Support_Resources.createTempFolder();
        ZipOutputStream zoutFoo = new ZipOutputStream(new FileOutputStream(fooZip));
        zoutFoo.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
        man.write(zoutFoo);
        zoutFoo.putNextEntry(new ZipEntry("foo/bar/execjartest/Foo.class"));
        zoutFoo.write(getResource(resources, "hyts_Foo.ser"));
        zoutFoo.close();
        JarOutputStream joutBar = new JarOutputStream(new FileOutputStream(barJar));
        joutBar.putNextEntry(new ZipEntry("foo/bar/execjartest/Bar.class"));
        joutBar.write(getResource(resources, "hyts_Bar.ser"));
        joutBar.close();
        String[] args = new String[] { "-jar", fooZip.getAbsolutePath() };
        String res = Support_Exec.execJava(args, null, false);
        assertTrue("Error executing ZIP : result returned was incorrect.", res.startsWith("FOOBAR"));
    }

    /**
	 * tests case when Main-Class is not in the zip launched but in another zip referenced by Class-Path
	 * @throws Exception in case of troubles
	 */
    public void test_main_class_in_another_zip() throws Exception {
        File fooZip = File.createTempFile("hyts_", ".zip");
        File barZip = File.createTempFile("hyts_", ".zip");
        fooZip.deleteOnExit();
        barZip.deleteOnExit();
        Manifest man = new Manifest();
        Attributes att = man.getMainAttributes();
        att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        att.put(Attributes.Name.MAIN_CLASS, "foo.bar.execjartest.Foo");
        att.put(Attributes.Name.CLASS_PATH, fooZip.getName());
        File resources = Support_Resources.createTempFolder();
        ZipOutputStream zoutFoo = new ZipOutputStream(new FileOutputStream(fooZip));
        zoutFoo.putNextEntry(new ZipEntry("foo/bar/execjartest/Foo.class"));
        zoutFoo.write(getResource(resources, "hyts_Foo.ser"));
        zoutFoo.close();
        ZipOutputStream zoutBar = new ZipOutputStream(new FileOutputStream(barZip));
        zoutBar.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
        man.write(zoutBar);
        zoutBar.putNextEntry(new ZipEntry("foo/bar/execjartest/Bar.class"));
        zoutBar.write(getResource(resources, "hyts_Bar.ser"));
        zoutBar.close();
        String[] args = new String[] { "-jar", barZip.getAbsolutePath() };
        String res = Support_Exec.execJava(args, null, false);
        assertTrue("Error executing JAR : result returned was incorrect.", res.startsWith("FOOBAR"));
    }

    private static byte[] getResource(File tempDir, String resourceName) throws IOException {
        Support_Resources.copyFile(tempDir, null, resourceName);
        File resourceFile = new File(tempDir, resourceName);
        resourceFile.deleteOnExit();
        byte[] resourceBody = new byte[(int) resourceFile.length()];
        FileInputStream fis = new FileInputStream(resourceFile);
        fis.read(resourceBody);
        fis.close();
        return resourceBody;
    }
}
